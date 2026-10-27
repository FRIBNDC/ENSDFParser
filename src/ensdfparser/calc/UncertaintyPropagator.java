package ensdfparser.calc;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UncertaintyPropagator {

    private final Map<String, Variable> variables = new LinkedHashMap<>();
    private String expression;
    private double result;
    private double taylorErr;
    private double minVal;
    private double maxVal;
    private Map<String, Double> minPoint;
    private Map<String, Double> maxPoint;
    private boolean hasSingularity = false;
    private Map<String, Double> singularityPoint;
    private String monotonicitySummary = "";
    private String inputSummary = "";

    private static class Variable {
        String name;
        double value;
        double uncertainty;
        String originalValue;
        String originalUnc;
        int decimalPlaces;
        String warning;

        Variable(String name, double value, double uncertainty, String originalValue, String originalUnc, String warning) {
            this.name = name;
            this.value = value;
            this.uncertainty = uncertainty;
            this.originalValue = originalValue;
            this.originalUnc = originalUnc;
            this.decimalPlaces = getDecimalPlaces(originalValue);
            this.warning = warning;
        }

        private int getDecimalPlaces(String s) {
            if (!s.contains(".")) return 0;
            return s.length() - s.indexOf(".") - 1;
        }

        String formatValue(double val) {
            return String.format("%." + decimalPlaces + "f", val);
        }
    }

    // ========== Parsing Logic ==========

    private static double convertENSDFUncertainty(String valueStr, String uncStr) {
        double uncertainty = Double.parseDouble(uncStr);
        int decimalPos = valueStr.indexOf('.');
        if (decimalPos == -1) {
            return uncertainty;
        }

        String decimalPart = valueStr.substring(decimalPos + 1);
        int totalDecimalPlaces = decimalPart.length();

        return uncertainty / Math.pow(10, totalDecimalPlaces);
    }

    public void parseVariables(String varStr) {
        parseVaraibles(varStr, true);
    }

    public void parseVaraibles(String varStr, boolean clearPrevious) {
        if (clearPrevious) {
            variables.clear();
            this.inputSummary = varStr;
        } else {
            this.inputSummary += "\n" + varStr;
        }

        if (varStr == null || varStr.trim().isEmpty()) return;

        String cleaned = varStr.replaceAll("(?i)^input:\\s*", "")
                            .replace("\r\n", "\n")
                            .replace("\r", "\n");

        String[] parts = cleaned.split("[,;\\n]+");

        Pattern p1 = Pattern.compile("^([a-zA-Z_]\\w*)\\s*=\\s*([\\d.]+)\\s+([\\d.]+)$");         
        Pattern p2 = Pattern.compile("^([a-zA-Z_]\\w*)\\s*=\\s*([\\d.]+)\\s*\\(([\\d.]+)\\)$");   
        Pattern p3 = Pattern.compile("^([a-zA-Z_]\\w*)\\s*=\\s*([\\d.]+)$");                     

        for (String rawPart : parts) {
            String part = rawPart.trim();
            
            if (part.isEmpty() || part.toLowerCase().startsWith("expr=")) continue;

            String[] subParts = part.split("(?<=\\)|\\d)\\s+(?=[a-zA-Z_])");

            for (String sub : subParts) {
                sub = sub.trim();
                if (sub.isEmpty()) continue;

                Matcher m1 = p1.matcher(sub);
                Matcher m2 = p2.matcher(sub);
                Matcher m3 = p3.matcher(sub);

                String name;
                String valueStr;
                String uncStr;
                boolean isConstant = false;

                if (m1.matches()) {
                    name = m1.group(1);
                    valueStr = m1.group(2);
                    uncStr = m1.group(3);
                } else if (m2.matches()) {
                    name = m2.group(1);
                    valueStr = m2.group(2);
                    uncStr = m2.group(3);
                } else if (m3.matches()) {
                    name = m3.group(1);
                    valueStr = m3.group(2);
                    uncStr = "0";
                    isConstant = true;
                } else {
                    continue; 
                }

                double value = Double.parseDouble(valueStr);
                double uncertainty;
                String warning = "";

                if (isConstant) {
                    uncertainty = 0.0;
                } else if (uncStr.contains(".")) {
                    warning = "Uncertainty '" + uncStr + "' for variable '" + name +
                            "' is in decimal format.\n    ENSDF format expects integers (e.g., 15 not 0.00015).";
                    uncertainty = Double.parseDouble(uncStr);
                } else {
                    uncertainty = convertENSDFUncertainty(valueStr, uncStr);
                }

                variables.put(name, new Variable(name, value, uncertainty, valueStr, uncStr, warning));
            }
        }
    }

    // ========== Calculation ==========
    
    public void calc(String expr, String varStr) {
        parseVariables(varStr);
        calc(expr);
    }
    
    public void calc(String expr) {
        if (expr != null && expr.toLowerCase().startsWith("expr=")) {
            expr = expr.substring(5).trim();
        }
        this.expression = expr;

        String[] names = variables.keySet().toArray(new String[0]);
        double[] values = variables.values().stream()
            .mapToDouble(v -> v.value)
            .toArray();

        result = eval(expr, names, values);

        double[] deriv = derivatives(expr, names, values);
        taylorErr = 0;
        for (int i = 0; i < deriv.length; i++) {
            double u = variables.get(names[i]).uncertainty;
            taylorErr += Math.pow(deriv[i] * u, 2);
        }
        taylorErr = Math.sqrt(taylorErr);

        findMinMax(expr, names);
    }

    // ========== Monotonicity & Min-Max ==========

    private void findMinMax(String expr, String[] names) {
        double[][] ranges = new double[names.length][2];
        for (int i = 0; i < names.length; i++) {
            Variable v = variables.get(names[i]);
            ranges[i][0] = v.value - v.uncertainty;
            ranges[i][1] = v.value + v.uncertainty;
        }

        // Reset status and tracking
        this.hasSingularity = false;
        this.singularityPoint = null;

        // Analytical boundary pre-check for trigonometric asymptotes (prevents grid-step misses)
        for (int i = 0; i < names.length; i++) {
            double min = ranges[i][0];
            double max = ranges[i][1];
            
            if (expr.contains("cos(" + names[i] + ")")) {
                double[] trigRoots = { Math.PI / 2, -Math.PI / 2, 3 * Math.PI / 2, -3 * Math.PI / 2, 5 * Math.PI / 2, -5 * Math.PI / 2 };
                for (double root : trigRoots) {
                    if (min <= root && root <= max) {
                        this.hasSingularity = true;
                        this.singularityPoint = new LinkedHashMap<>();
                        this.singularityPoint.put(names[i], root);
                        this.minVal = Double.NaN;
                        this.maxVal = Double.NaN;
                        this.minPoint = null;
                        this.maxPoint = null;
                        return;
                    }
                }
            }
            if (expr.contains("sin(" + names[i] + ")") && expr.contains("/")) {
                double[] trigRoots = { 0.0, Math.PI, -Math.PI, 2 * Math.PI, -2 * Math.PI, 3 * Math.PI, -3 * Math.PI };
                for (double root : trigRoots) {
                    if (min <= root && root <= max) {
                        this.hasSingularity = true;
                        this.singularityPoint = new LinkedHashMap<>();
                        this.singularityPoint.put(names[i], root);
                        this.minVal = Double.NaN;
                        this.maxVal = Double.NaN;
                        this.minPoint = null;
                        this.maxPoint = null;
                        return;
                    }
                }
            }
        }

        int samples = 50;
        double[] current = new double[names.length];
        if (checkSingularityRecursiveGrid(expr, names, ranges, 0, current, samples)) {
            this.hasSingularity = true;
            this.minVal = Double.NaN;
            this.maxVal = Double.NaN;
            this.minPoint = null;
            this.maxPoint = null;
            return;
        }
        
        boolean[] increasing = new boolean[names.length];
        boolean allMonotonic = true;
        int monotonicCount = 0;

        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            int mono = checkMonotonicity(expr, names, ranges, i);
            if (i > 0) summary.append(", ");
            if (mono == 0) {
                allMonotonic = false;
                summary.append(names[i]).append(": NON-MONOTONIC");
            } else {
                increasing[i] = (mono == 1);
                monotonicCount++;
                summary.append(names[i]).append(": ").append(mono == 1 ? "INCREASING" : "DECREASING");
            }
        }
        monotonicitySummary = summary.toString();

        if (allMonotonic) {
            evaluateCorners(expr, names, ranges, increasing);
            return;
        }

        if (monotonicCount > 0) {
            findMinMaxReduced(expr, names, ranges, increasing);
            return;
        }

        findMinMaxFull(expr, names, ranges);
    }

    private boolean checkSingularityRecursiveGrid(String expr, String[] names, double[][] ranges,
                                                  int depth, double[] current, int samples) {
        if (depth == names.length) {
            try {
                double val = eval(expr, names, current);
                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    this.singularityPoint = new LinkedHashMap<>();
                    for (int i = 0; i < names.length; i++) {
                        if (Math.abs(current[i]) < 1e-7 || Math.abs(Math.cos(current[i])) < 1e-7 || Math.abs(Math.sin(current[i])) < 1e-7) {
                            double exactVal = current[i];
                            if (Math.abs(exactVal - Math.PI / 2) < 1e-4) exactVal = Math.PI / 2;
                            else if (Math.abs(exactVal - (-Math.PI / 2)) < 1e-4) exactVal = -Math.PI / 2;
                            else if (Math.abs(exactVal - (3 * Math.PI / 2)) < 1e-4) exactVal = 3 * Math.PI / 2;
                            else if (Math.abs(exactVal - Math.PI) < 1e-4) exactVal = Math.PI;
                            else if (Math.abs(exactVal) < 1e-4) exactVal = 0.0;
                            this.singularityPoint.put(names[i], exactVal);
                        }
                    }
                    if (this.singularityPoint.isEmpty()) {
                        this.singularityPoint = toMap(names, current);
                    }
                    return true;
                }
            } catch (Exception e) {
                this.singularityPoint = new LinkedHashMap<>();
                for (int i = 0; i < names.length; i++) {
                    if (Math.abs(current[i]) < 1e-7 || Math.abs(Math.cos(current[i])) < 1e-7 || Math.abs(Math.sin(current[i])) < 1e-7) {
                        double exactVal = current[i];
                        if (Math.abs(exactVal - Math.PI / 2) < 1e-4) exactVal = Math.PI / 2;
                        else if (Math.abs(exactVal - (-Math.PI / 2)) < 1e-4) exactVal = -Math.PI / 2;
                        else if (Math.abs(exactVal - (3 * Math.PI / 2)) < 1e-4) exactVal = 3 * Math.PI / 2;
                        else if (Math.abs(exactVal - Math.PI) < 1e-4) exactVal = Math.PI;
                        else if (Math.abs(exactVal) < 1e-4) exactVal = 0.0;
                        this.singularityPoint.put(names[i], exactVal);
                    }
                }
                if (this.singularityPoint.isEmpty()) {
                    this.singularityPoint = toMap(names, current);
                }
                return true;
            }
            return false;
        }
        
        List<Double> testPoints = new ArrayList<>();
        double min = ranges[depth][0];
        double max = ranges[depth][1];
        
        for (int i = 0; i < samples; i++) {
            double t = (double) i / (samples - 1);
            testPoints.add(min + t * (max - min));
        }
        
        if (!testPoints.contains(min)) testPoints.add(min);
        if (!testPoints.contains(max)) testPoints.add(max);
        if (min < 0 && max > 0 && !testPoints.contains(0.0)) {
            testPoints.add(0.0);
        }
        
        double[] universalTrigRoots = {
            0.0, Math.PI / 2, -Math.PI / 2, 
            Math.PI, -Math.PI, 
            3 * Math.PI / 2, -3 * Math.PI / 2,
            2 * Math.PI, -2 * Math.PI
        };
        for (double root : universalTrigRoots) {
            if (root >= min && root <= max) {
                testPoints.add(root);
                testPoints.add(root + 1e-11);
                testPoints.add(root - 1e-11);
            }
        }

        for (double pt : testPoints) {
            current[depth] = pt;
            if (checkSingularityRecursiveGrid(expr, names, ranges, depth + 1, current, samples)) {
                return true;
            }
        }
        
        return false;
    }

    private int checkMonotonicity(String expr, String[] names, double[][] ranges, int idx) {
        if (ranges[idx][0] == ranges[idx][1]) {
            return 1;
        }

        int points = 25;
        double[] central = getCentralValues(names);

        Double prevDeriv = null;
        boolean hasPositive = false;
        boolean hasNegative = false;

        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            double val = ranges[idx][0] + t * (ranges[idx][1] - ranges[idx][0]);

            double deriv = numericalDerivative(expr, names, central, idx, val);

            if (Math.abs(deriv) < 1e-10) return 0;
            if (prevDeriv != null && deriv * prevDeriv < 0) return 0;

            if (deriv > 0) hasPositive = true;
            if (deriv < 0) hasNegative = true;

            prevDeriv = deriv;
        }

        if (hasPositive && !hasNegative) return 1;
        if (hasNegative && !hasPositive) return -1;
        return 0;
    }

    private double numericalDerivative(String expr, String[] names, double[] central, int idx, double value) {
        double h = 1e-6;
        double[] currentPlus = central.clone();
        double[] currentMinus = central.clone();

        currentPlus[idx] = value + h;
        currentMinus[idx] = value - h;

        double fPlus = eval(expr, names, currentPlus);
        double fMinus = eval(expr, names, currentMinus);

        return (fPlus - fMinus) / (2 * h);
    }

    private double[] getCentralValues(String[] names) {
        double[] values = new double[names.length];
        for (int i = 0; i < names.length; i++) {
            values[i] = variables.get(names[i]).value;
        }
        return values;
    }

    private void evaluateCorners(String expr, String[] names, double[][] ranges, boolean[] increasing) {
        int n = names.length;
        double[] minP = new double[n];
        double[] maxP = new double[n];

        for (int i = 0; i < n; i++) {
            if (increasing[i]) {
                minP[i] = ranges[i][0];
                maxP[i] = ranges[i][1];
            } else {
                minP[i] = ranges[i][1];
                maxP[i] = ranges[i][0];
            }
        }

        minVal = eval(expr, names, minP);
        maxVal = eval(expr, names, maxP);

        this.minPoint = toMap(names, minP);
        this.maxPoint = toMap(names, maxP);
    }

    private void findMinMaxReduced(String expr, String[] names, double[][] ranges, boolean[] increasing) {
        int n = names.length;
        minVal = Double.POSITIVE_INFINITY;
        maxVal = Double.NEGATIVE_INFINITY;
        this.minPoint = new HashMap<>();
        this.maxPoint = new HashMap<>();
        searchFull(expr, names, ranges, 0, new double[n], 10);
    }

    private void findMinMaxFull(String expr, String[] names, double[][] ranges) {
        int n = names.length;
        int samples = n <= 3 ? 20 : 12;

        minVal = Double.POSITIVE_INFINITY;
        maxVal = Double.NEGATIVE_INFINITY;
        this.minPoint = new HashMap<>();
        this.maxPoint = new HashMap<>();
        searchFull(expr, names, ranges, 0, new double[n], samples);
    }

    private void searchFull(String expr, String[] names, double[][] ranges,
                            int depth, double[] current, int samples) {
        if (depth == names.length) {
            double val = eval(expr, names, current);
            updateMinMax(val, current);
            return;
        }

        for (int i = 0; i < samples; i++) {
            double t = (double) i / (samples - 1);
            current[depth] = ranges[depth][0] + t * (ranges[depth][1] - ranges[depth][0]);
            searchFull(expr, names, ranges, depth + 1, current, samples);
        }
    }

    private void updateMinMax(double val, double[] point) {
        String[] names = variables.keySet().toArray(new String[0]);
        if (val < minVal) {
            minVal = val;
            this.minPoint = toMap(names, point);
        }
        if (val > maxVal) {
            maxVal = val;
            this.maxPoint = toMap(names, point);
        }
    }

    private Map<String, Double> toMap(String[] names, double[] values) {
        Map<String, Double> map = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], values[i]);
        }
        return map;
    }

    private double[] derivatives(String expr, String[] names, double[] values) {
        double[] deriv = new double[names.length];
        double h = 1e-6;

        for (int i = 0; i < names.length; i++) {
            double[] vPlus = values.clone();
            vPlus[i] += h;
            double fPlus = eval(expr, names, vPlus);

            double[] vMinus = values.clone();
            vMinus[i] -= h;
            double fMinus = eval(expr, names, vMinus);

            deriv[i] = (fPlus - fMinus) / (2 * h);
        }
        return deriv;
    }

    // ========== Substitution & Evaluation ==========

    private double eval(String expr, String[] names, double[] values) {
        Map<String, String> valMap = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            valMap.put(names[i], java.math.BigDecimal.valueOf(values[i]).toPlainString());
        }

        List<String> sortedNames = new ArrayList<>(valMap.keySet());
        sortedNames.sort((s1, s2) -> Integer.compare(s2.length(), s1.length()));

        String e = expr;
        for (String name : sortedNames) {
            String valStr = valMap.get(name);
            e = e.replaceAll("\\b" + Pattern.quote(name) + "\\b", "(" + valStr + ")");
        }

        try {
            return ExpressionEvaluator.evaluate(e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to evaluate substituted expression: [" + e + "] from original: [" + expr + "]", ex);
        }
    }

    // ========== Reporting & Getters ==========

    public double getValue() { return result; }
    public double getTaylorError() { return taylorErr; }
    public double getMin() { return minVal; }
    public double getMax() { return maxVal; }
    public double getDeltaLower() { return result - minVal; }
    public double getDeltaUpper() { return maxVal - result; }
    public Map<String, Double> getMinPoint() { return minPoint; }
    public Map<String, Double> getMaxPoint() { return maxPoint; }
    public boolean hasSingularity() { return hasSingularity; }
    public Map<String, Double> getSingularityPoint() { return singularityPoint; }
    public String getMonotonicitySummary() { return monotonicitySummary; }
    public String getInputSummary() { return inputSummary; }
    public Map<String, Variable> getVariables() { return variables; }

    public String report() {
        StringBuilder sb = new StringBuilder();
        String line = "==================================================";

        sb.append(line).append("\n");
        sb.append("EXPRESSION: ").append(expression).append("\n");
        sb.append(line).append("\n\n");

        sb.append("INPUT: ").append(inputSummary).append("\n\n");

        boolean hasWarnings = false;
        for (Variable v : variables.values()) {
            if (v.warning != null && !v.warning.isEmpty()) {
                if (!hasWarnings) {
                    sb.append("WARNINGS:\n");
                    hasWarnings = true;
                }
                sb.append("  ").append(v.warning).append("\n");
            }
        }

        if (hasWarnings) sb.append("\n");

        // Check for singularity using status flag
        if (hasSingularity) {
            sb.append("WARNING: Division by zero possible within parameter ranges.\n");
            sb.append("   Min-Max bounds are undefined (function has singularities).\n");
            
            if (singularityPoint != null && !singularityPoint.isEmpty()) {
                sb.append("   Singularity encountered at: ");
                for (Map.Entry<String, Double> e : singularityPoint.entrySet()) {
                    sb.append(e.getKey()).append("=").append(e.getValue()).append(" ");
                }
                sb.append("\n");
            }
            
            sb.append("\n").append(line).append("\n");
            return sb.toString();
        }

        // Otherwise, continue with the full normal report
        sb.append("MONOTONICITY: ").append(monotonicitySummary).append("\n");
        sb.append("(within range)").append("\n");
        sb.append(line).append("\n");

        sb.append("RESULT: ");
        sb.append(String.format("%10.6f +-%.6f (Taylor's expansion)\n", result, taylorErr));
        sb.append("        ");
        sb.append(String.format("%10s +%.6f -%.6f (Max-Min)\n", "", maxVal - result, result - minVal));
        sb.append("\n");

        if (minPoint != null) {
            sb.append("Min at: ");
            for (Map.Entry<String, Double> e : minPoint.entrySet()) {
                Variable v = variables.get(e.getKey());
                sb.append(e.getKey()).append("=").append(v.formatValue(e.getValue())).append(" ");
            }
            sb.append("\n");
        }
        
        if (maxPoint != null) {
            sb.append("Max at: ");
            for (Map.Entry<String, Double> e : maxPoint.entrySet()) {
                Variable v = variables.get(e.getKey());
                sb.append(e.getKey()).append("=").append(v.formatValue(e.getValue())).append(" ");
            }
            sb.append("\n");
        }

        sb.append("\n").append(line).append("\n");
        return sb.toString();
    }
}