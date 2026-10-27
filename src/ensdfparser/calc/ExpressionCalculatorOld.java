package ensdfparser.calc;

import java.util.*;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.nds.util.Str;

/**
 * Calculates mathematical expressions with variables that have uncertainties (SDS2XDX objects).
 * Handles proper uncertainty propagation and accounts for correlations when the same
 * variable appears multiple times in an expression.
 * 
 * NOT USED
 */
public class ExpressionCalculatorOld {
    
    private int errorLimit = 35; // Default error limit
    
    
    private SDS2XDX result=null;
    private SDS2XDX MaxMinResult=null;
    
    /**
     * Creates a new calculator with default error limit (35)
     */
    public ExpressionCalculatorOld() {
    }
    
    /**
     * Creates a new calculator with specified error limit
     * @param errorLimit Maximum number of significant digits for uncertainties
     */
    public ExpressionCalculatorOld(int errorLimit) {
        this.errorLimit = errorLimit;
    }
    
    /**
     * Sets the error limit for calculations
     * @param errorLimit Maximum number of significant digits for uncertainties
     */
    public void setErrorLimit(int errorLimit) {
        this.errorLimit = errorLimit;
    }
    
    
    public SDS2XDX getResult() {return result;}
    public SDS2XDX getMaxMinResult() {return MaxMinResult;}
    
    
    /**
     * Calculates the result of a mathematical expression with SDS2XDX parameters
     * using hybrid optimization for accurate Max-Min uncertainty propagation.
     */
    public SDS2XDX calculate(String expression, Map<String, SDS2XDX> parameters) {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                return null;
            }
            
            String cleanExpr = expression.replaceAll("\\s+", "").replace("**", "^").toUpperCase();

            ExpressionNode ast = parse(cleanExpr, parameters.keySet());
            if (ast == null) {
                System.err.println("Failed to parse expression: " + cleanExpr);
                return null;
            }
            
            // Create parameter ranges using lower and upper bounds (xLV, xUV)
            Map<String, ParameterRange> ranges = new HashMap<>();
            for (Map.Entry<String, SDS2XDX> entry : parameters.entrySet()) {
                SDS2XDX sds = entry.getValue();
                ranges.put(entry.getKey(), 
                    new ParameterRange(sds.xLV(), sds.xUV(), sds));
            }
            
            // Replaced boundary calls with hybrid optimization (finds interior extrema)
            OptimizationResult maxResult = findMax(cleanExpr, ranges);
            OptimizationResult minResult = findMin(cleanExpr, ranges);

            result = ast.evaluate(parameters, errorLimit);
           
            if (result != null && Str.isNumericUnc(result.ds()) && maxResult != null && minResult != null) {
                double x = result.x();
                
                // Upper and lower uncertainties derived from extrema
                double dxu = Math.max(0.0, maxResult.getValue() - x);
                double dxl = Math.max(0.0, x - minResult.getValue());
                
                MaxMinResult = new SDS2XDX();
                MaxMinResult.setErrorLimit(errorLimit);
                MaxMinResult.setValues(x, dxu, dxl);
            } else {
                MaxMinResult = null;
            }

            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Quick calculate with parameters in string format: "a=12(1);b=13(2);c=2.5"
     */
    public SDS2XDX calculate(String expression, String paramString) {
        Map<String, SDS2XDX> params = new HashMap<>();
        
        if (paramString != null && !paramString.trim().isEmpty()) {
            String[] pairs = paramString.split(";");
            for (String pair : pairs) {
                pair = pair.trim();
                if (pair.isEmpty()) continue;
                
                String[] parts = pair.split("=");
                if (parts.length != 2) {
                    System.err.println("Invalid parameter format: " + pair);
                    continue;
                }
                
                String name = parts[0].trim().toUpperCase();
                String value = parts[1].trim();
                
                SDS2XDX sds;
                if (value.contains("(") && value.contains(")")) {
                    // Format like "12(1)" or "12.5(3)"
                    sds = new SDS2XDX(value.substring(0, value.indexOf('(')), 
                                       value.substring(value.indexOf('(') + 1, value.indexOf(')')));
                } else {
                    // Just a number, assume zero uncertainty
                    sds = new SDS2XDX(value, "0");
                }
                sds.setErrorLimit(errorLimit);
                params.put(name, sds);
            }
        }

        return calculate(expression, params);
    }

    /**
     * Calculates the result with double values
     */
    public SDS2XDX calculateWithDoubles(String expression, Map<String, Double> doubleParams) {
        Map<String, SDS2XDX> sdsParams = new HashMap<>();
        for (Map.Entry<String, Double> entry : doubleParams.entrySet()) {
            SDS2XDX sds = new SDS2XDX();
            sds.setValues(entry.getValue(), 0.0);
            sds.setErrorLimit(errorLimit);
            sdsParams.put(entry.getKey().toUpperCase(), sds);
        }
        return calculate(expression, sdsParams);
    }
    
    /**
     * Calculates the result with string values in ENSDF format
     */
    public SDS2XDX calculateWithStrings(String expression, Map<String, String> stringParams) {
        Map<String, SDS2XDX> sdsParams = new HashMap<>();
        for (Map.Entry<String, String> entry : stringParams.entrySet()) {
            String value = entry.getValue();
            SDS2XDX sds;
            
            try {
                if (value.contains("(") && value.contains(")")) {
                    int openParen = value.indexOf('(');
                    String numPart = value.substring(0, openParen);
                    String uncPart = value.substring(openParen + 1, value.indexOf(')'));
                    sds = new SDS2XDX(numPart, uncPart);
                } else {
                    sds = new SDS2XDX(value, "0");
                }
                sds.setErrorLimit(errorLimit);
                sdsParams.put(entry.getKey().toUpperCase(), sds);
            } catch (Exception e) {
                System.err.println("Error parsing parameter " + entry.getKey() + " = " + value);
            }
        }
        return calculate(expression, sdsParams);
    }
    
    /**
     * Calculates the derivative of an expression
     */
    public SDS2XDX derivative(String expression, String variable, Map<String, SDS2XDX> parameters) {
        try {
            String cleanExpr = expression.replaceAll("\\s+", "").toUpperCase();
            String var = variable.toUpperCase();
            
            ExpressionNode ast = parse(cleanExpr, parameters.keySet());
            if (ast == null) return null;
            
            // Use numerical differentiation to avoid recursion issues
            return numericalDerivative(ast, var, parameters);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Numerical differentiation to avoid recursion issues
     */
    private SDS2XDX numericalDerivative(ExpressionNode node, String variable, Map<String, SDS2XDX> parameters) {
        SDS2XDX varValue = parameters.get(variable);
        if (varValue == null) return null;
        
        double x = varValue.x();
        double h = Math.max(1e-6, 0.001 * Math.abs(x));
        if (Math.abs(x) < 1e-10) h = 1e-6;
        
        // Create copies of parameters with perturbed values
        Map<String, SDS2XDX> paramsPlus = new HashMap<>();
        Map<String, SDS2XDX> paramsMinus = new HashMap<>();
        
        for (Map.Entry<String, SDS2XDX> entry : parameters.entrySet()) {
            String key = entry.getKey();
            SDS2XDX original = entry.getValue();
            
            // Clone the original using setValues(SDS2XDX)
            SDS2XDX plus = new SDS2XDX();
            plus.setValues(original);
            paramsPlus.put(key, plus);
            
            SDS2XDX minus = new SDS2XDX();
            minus.setValues(original);
            paramsMinus.put(key, minus);
        }
        
        // Perturb the variable - create new SDS2XDX with perturbed value
        SDS2XDX plusVar = paramsPlus.get(variable);
        double plusX = x + h;
        SDS2XDX newPlus = new SDS2XDX();
        
        // Use appropriate setValues method based on uncertainty type
        if (plusVar.dsIsAsymm()) {
            newPlus.setValues(plusX, plusVar.dxu(), plusVar.dxl());
        } else {
            newPlus.setValues(plusX, plusVar.dx());
        }
        newPlus.setErrorLimit(plusVar.getErrorLimit());
        paramsPlus.put(variable, newPlus);
        
        SDS2XDX minusVar = paramsMinus.get(variable);
        double minusX = x - h;
        SDS2XDX newMinus = new SDS2XDX();
        
        // Use appropriate setValues method based on uncertainty type
        if (minusVar.dsIsAsymm()) {
            newMinus.setValues(minusX, minusVar.dxu(), minusVar.dxl());
        } else {
            newMinus.setValues(minusX, minusVar.dx());
        }
        newMinus.setErrorLimit(minusVar.getErrorLimit());
        paramsMinus.put(variable, newMinus);
        
        // Evaluate at perturbed points
        SDS2XDX fPlus = node.evaluate(paramsPlus, errorLimit);
        SDS2XDX fMinus = node.evaluate(paramsMinus, errorLimit);
        
        if (fPlus == null || fMinus == null) return null;
        
        double deriv = (fPlus.x() - fMinus.x()) / (2 * h);
        
        // Estimate uncertainty in derivative using propagation
        double dx = getUncertainty(varValue);
        double derivUnc = Math.abs(deriv) * (dx / Math.abs(x)) * 2; // Rough estimate
        
        SDS2XDX result = new SDS2XDX();
        result.setErrorLimit(errorLimit);
        result.setValues(deriv, derivUnc);
        return result;
    }
    
    /**
     * Helper method to get uncertainty from SDS2XDX
     */
    private double getUncertainty(SDS2XDX sds) {
        if (sds == null) return 0.0;
        if (sds.dx() > 0) return sds.dx();
        if (sds.dxu() > 0 && sds.dxl() > 0) 
            return (sds.dxu() + sds.dxl()) / 2.0;
        return 0.01 * Math.abs(sds.x()); // Default 1% if no uncertainty
    }
    
    /**
     * Calculates all first derivatives
     */
    public Map<String, SDS2XDX> allDerivatives(String expression, Map<String, SDS2XDX> parameters) {
        Map<String, SDS2XDX> derivatives = new HashMap<>();
        
        try {
            String cleanExpr = expression.replaceAll("\\s+", "").toUpperCase();
            
            ExpressionNode ast = parse(cleanExpr, parameters.keySet());
            if (ast == null) return derivatives;
            
            for (String var : parameters.keySet()) {
                derivatives.put(var, numericalDerivative(ast, var, parameters));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return derivatives;
    }
    
    //-------------------------------------------------------------------------
    // Monotonicity Analysis
    //-------------------------------------------------------------------------

    /**
     * Checks monotonicity of an expression with respect to a specific variable.
     * 
     * @param expression The mathematical expression (with variables)
     * @param varName The variable name to check monotonicity for
     * @param varMin The minimum value of the variable range
     * @param varMax The maximum value of the variable range
     * @param otherParams Map of other parameter names to their fixed nominal values
     * @param numPoints Number of points to sample (default 25)
     * @return 1 if strictly increasing, -1 if strictly decreasing, 0 if non-monotonic or flat
     */
    public int checkMonotonicity(String expression, String varName, 
                                  double varMin, double varMax, 
                                  Map<String, Double> otherParams, int numPoints) {
        if (varMin == varMax) {
            return 1;
        }
        
        if (numPoints < 2) numPoints = 2;
        if (numPoints > 1000) numPoints = 1000;
        
        try {
            String cleanExpr = expression.replaceAll("\\s+", "").replace("**", "^").toUpperCase();
            
            Set<String> allVars = new HashSet<>(otherParams.keySet());
            allVars.add(varName);
            
            ExpressionNode ast = parse(cleanExpr, allVars);
            if (ast == null) {
                return 0;
            }
            
            // Build parameter map for evaluation (with nominal values for other params)
            Map<String, SDS2XDX> baseParams = new HashMap<>();
            for (Map.Entry<String, Double> entry : otherParams.entrySet()) {
                SDS2XDX sds = new SDS2XDX();
                sds.setValues(entry.getValue(), 0.0);
                sds.setErrorLimit(errorLimit);
                baseParams.put(entry.getKey(), sds);
            }
            
            Double prevDeriv = null;
            boolean hasPositive = false;
            boolean hasNegative = false;
            
            double step = (varMax - varMin) / numPoints;
            
            // Get the scale of the function to determine tolerance
            Map<String, SDS2XDX> nominalParams = new HashMap<>(baseParams);
            SDS2XDX nominalSDS = new SDS2XDX();
            nominalSDS.setValues((varMin + varMax) / 2.0, 0.0);
            nominalSDS.setErrorLimit(errorLimit);
            nominalParams.put(varName, nominalSDS);
            SDS2XDX nominalResult = ast.evaluate(nominalParams, errorLimit);
            double nominalValue = (nominalResult != null) ? Math.abs(nominalResult.x()) : 1.0;
            double tolerance = Math.max(1e-12, 1e-10 * nominalValue);
            
            for (int i = 0; i <= numPoints; i++) {
                double val = varMin + i * step;
                double deriv = numericalDerivativeAtPoint(ast, varName, val, otherParams);
                
                // Use tolerance that scales with the function value
                if (Math.abs(deriv) < tolerance) {
                    return 0;
                }
                
                if (prevDeriv != null && deriv * prevDeriv < 0) {
                    return 0;
                }
                
                if (deriv > 0) hasPositive = true;
                if (deriv < 0) hasNegative = true;
                
                prevDeriv = deriv;
            }
            
            if (hasPositive && !hasNegative) return 1;
            if (hasNegative && !hasPositive) return -1;
            return 0;
            
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Overload with default 25 points.
     */
    public int checkMonotonicity(String expression, String varName, 
                                  double varMin, double varMax, 
                                  Map<String, Double> otherParams) {
        return checkMonotonicity(expression, varName, varMin, varMax, otherParams, 25);
    }

    /**
     * Checks monotonicity using SDS2XDX parameters (uses xLV/xUV as range).
     */
    public int checkMonotonicity(String expression, String varName, 
                                  Map<String, SDS2XDX> parameters, int numPoints) {
        SDS2XDX varSDS = parameters.get(varName);
        if (varSDS == null) return 0;
        
        Map<String, Double> otherParams = new HashMap<>();
        for (Map.Entry<String, SDS2XDX> entry : parameters.entrySet()) {
            if (!entry.getKey().equals(varName)) {
                // Use the nominal value (x()) for other parameters
                otherParams.put(entry.getKey(), entry.getValue().x());
            }
        }
        
        // For variables with very small values (like b=0.00060), 
        // check monotonicity more carefully
        int result = checkMonotonicity(expression, varName, 
                                        varSDS.xLV(), varSDS.xUV(), 
                                        otherParams, numPoints);
        
        // If the result is 0 (non-monotonic), do a secondary check
        // by sampling more points to confirm
        if (result == 0 && numPoints < 50) {
            result = checkMonotonicity(expression, varName, 
                                        varSDS.xLV(), varSDS.xUV(), 
                                        otherParams, 50);
        }
        
        return result;
    }

    /**
     * Overload with default 25 points.
     */
    public int checkMonotonicity(String expression, String varName, 
                                  Map<String, SDS2XDX> parameters) {
        return checkMonotonicity(expression, varName, parameters, 25);
    }

    /**
     * Numerical derivative at a specific point with adaptive step size.
     * Uses relative step size to maintain precision across different scales.
     */
    private double numericalDerivativeAtPoint(ExpressionNode ast, String varName, 
                                               double x, Map<String, Double> otherParams) {
        // Adaptive step size: sqrt(eps) * max(|x|, 1) for better precision
        double h = Math.sqrt(2.2e-16) * Math.max(Math.abs(x), 1.0);
        // Ensure h is not too small
        h = Math.max(h, 1e-10);
        
        // Evaluate at x + h and x - h
        double fPlus = evaluateAtPoint(ast, varName, x + h, otherParams);
        double fMinus = evaluateAtPoint(ast, varName, x - h, otherParams);
        
        double deriv = (fPlus - fMinus) / (2 * h);
        
        // If derivative is extremely small, do a secondary check with larger step
        if (Math.abs(deriv) < 1e-14) {
            double h2 = h * 10;
            double fPlus2 = evaluateAtPoint(ast, varName, x + h2, otherParams);
            double fMinus2 = evaluateAtPoint(ast, varName, x - h2, otherParams);
            double deriv2 = (fPlus2 - fMinus2) / (2 * h2);
            if (Math.abs(deriv2) < 1e-14) {
                return 0.0;
            }
            return deriv2;
        }
        
        return deriv;
    }

    /**
     * Evaluates the expression at a specific point.
     */
    private double evaluateAtPoint(ExpressionNode ast, String varName, 
                                    double varValue, Map<String, Double> otherParams) {
        try {
            Map<String, SDS2XDX> evalParams = new HashMap<>();
            
            for (Map.Entry<String, Double> entry : otherParams.entrySet()) {
                SDS2XDX sds = new SDS2XDX();
                sds.setValues(entry.getValue(), 0.0);
                sds.setErrorLimit(errorLimit);
                evalParams.put(entry.getKey(), sds);
            }
            
            SDS2XDX varSDS = new SDS2XDX();
            varSDS.setValues(varValue, 0.0);
            varSDS.setErrorLimit(errorLimit);
            evalParams.put(varName, varSDS);
            
            SDS2XDX result = ast.evaluate(evalParams, errorLimit);
            return result != null ? result.x() : 0.0;
            
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    //-------------------------------------------------------------------------
    // Abstract Syntax Tree Nodes (SIMPLIFIED)
    //-------------------------------------------------------------------------
    
    private interface ExpressionNode {
        SDS2XDX evaluate(Map<String, SDS2XDX> variables, int errorLimit);
        Set<String> getVariables();
    }
    
    private static class ConstantNode implements ExpressionNode {
        private double value;
        
        ConstantNode(double value) {
            this.value = value;
        }
        
        @Override
        public SDS2XDX evaluate(Map<String, SDS2XDX> variables, int errorLimit) {
            SDS2XDX result = new SDS2XDX();
            result.setValues(value, 0.0, errorLimit);
            return result;
        }
        
        @Override
        public Set<String> getVariables() {
            return Collections.emptySet();
        }
    }
    
    private static class VariableNode implements ExpressionNode {
        private String name;
        
        VariableNode(String name) {
            this.name = name;
        }
        
        @Override
        public SDS2XDX evaluate(Map<String, SDS2XDX> variables, int errorLimit) {
            SDS2XDX value = variables.get(name);
            if (value == null) {
                throw new IllegalArgumentException("Missing value for variable: " + name);
            }
            SDS2XDX clone = new SDS2XDX();
            clone.setValues(value);
            clone.setErrorLimit(errorLimit);
            return clone;
        }
        
        @Override
        public Set<String> getVariables() {
            return Collections.singleton(name);
        }
    }
    
    private static class BinaryOpNode implements ExpressionNode {
        private String op;
        private ExpressionNode left;
        private ExpressionNode right;
        
        BinaryOpNode(String op, ExpressionNode left, ExpressionNode right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }
        
        @Override
        public SDS2XDX evaluate(Map<String, SDS2XDX> variables, int errorLimit) {
            // FIRST: Get the actual evaluated values for the mean
            SDS2XDX leftVal = left.evaluate(variables, errorLimit);
            SDS2XDX rightVal = right.evaluate(variables, errorLimit);
            
            if (leftVal == null || rightVal == null) return null;
            
            // Calculate the mean value
            double meanValue = applyOperator(leftVal.x(), rightVal.x(), op);
            
            // Calculate uncertainty using numerical differentiation on the WHOLE expression
            Set<String> allVars = new HashSet<>(left.getVariables());
            allVars.addAll(right.getVariables());
            
            double uncertainty = calculateUncertainty(this, variables, allVars, errorLimit);
            
            SDS2XDX result = new SDS2XDX();
            result.setErrorLimit(errorLimit);
            result.setValues(meanValue, uncertainty);
            return result;
        }
        
        /**
         * Calculate uncertainty for any expression node using numerical differentiation
         * This automatically handles correlations because it perturbs each variable once
         */
        private double calculateUncertainty(ExpressionNode node, Map<String, SDS2XDX> variables, 
                                            Set<String> allVars, int errorLimit) {
            
            // Get the value at the current point
            Map<String, Double> point = new HashMap<>();
            for (String var : allVars) {
                point.put(var, variables.get(var).x());
            }
            
            double f = evaluateAtPoint(node, variables, point);
            
            double variance = 0.0;
            double h = 1e-6;
            
            // Calculate partial derivatives for ALL variables
            for (String var : allVars) {
                SDS2XDX varValue = variables.get(var);
                double dx = getUncertainty(varValue);
                double x = point.get(var);
                
                Map<String, Double> pointPlus = new HashMap<>(point);
                pointPlus.put(var, x + h);
                
                double fPlus = evaluateAtPoint(node, variables, pointPlus);
                double df_dx = (fPlus - f) / h;
                
                variance += Math.pow(df_dx * dx, 2);
            }
            
            return Math.sqrt(Math.abs(variance));
        }
        
        
         
        /**
         * ✅ NEW METHOD: Evaluates a node at a specific point (for derivatives)
         */
        private double evaluateAtPoint(ExpressionNode node, Map<String, SDS2XDX> variables, 
                                        Map<String, Double> point) {
            if (node instanceof ConstantNode) {
                return ((ConstantNode) node).value;
            } else if (node instanceof VariableNode) {
                String name = ((VariableNode) node).name;
                return point.containsKey(name) ? point.get(name) : variables.get(name).x();
            } else if (node instanceof BinaryOpNode) {
                BinaryOpNode bop = (BinaryOpNode) node;
                double leftVal = evaluateAtPoint(bop.left, variables, point);
                double rightVal = evaluateAtPoint(bop.right, variables, point);
                return applyOperator(leftVal, rightVal, bop.op);
            } else if (node instanceof FunctionNode) {
                FunctionNode fnode = (FunctionNode) node;
                double argVal = evaluateAtPoint(fnode.arguments.get(0), variables, point);
                return applyFunction(argVal, fnode.function);
            }
            return 0.0;
        }
        
        private double applyOperator(double left, double right, String op) {
            switch (op) {
                case "+": return left + right;
                case "-": return left - right;
                case "*": return left * right;
                case "/": return left / right;
                case "^": return Math.pow(left, right);
                default: return 0.0;
            }
        }
        
        private double applyFunction(double arg, String func) {
            switch (func) {
                case "SIN": return Math.sin(arg);
                case "COS": return Math.cos(arg);
                case "TAN": return Math.tan(arg);
                case "EXP": return Math.exp(arg);
                case "LOG": return Math.log(arg);
                case "LOG10": return Math.log10(arg);
                case "SQRT": return Math.sqrt(arg);
                case "ABS": return Math.abs(arg);
                default: return arg;
            }
        }
        
        private double getUncertainty(SDS2XDX sds) {
            if (sds == null) return 0.0;
            if (sds.dx() > 0) return sds.dx();
            if (sds.dxu() > 0 && sds.dxl() > 0) 
                return (sds.dxu() + sds.dxl()) / 2.0;
            return 0.01 * Math.abs(sds.x());
        }
        
        
        @Override
        public Set<String> getVariables() {
            Set<String> vars = new HashSet<>(left.getVariables());
            vars.addAll(right.getVariables());
            return vars;
        }
    }
    
    private static class FunctionNode implements ExpressionNode {
        private String function;
        private List<ExpressionNode> arguments;
        
        FunctionNode(String function, ExpressionNode argument) {
            this.function = function;
            this.arguments = new ArrayList<>();
            this.arguments.add(argument);
        }
        
        FunctionNode(String function, List<ExpressionNode> arguments) {
            this.function = function;
            this.arguments = arguments;
        }
        
        @Override
        public SDS2XDX evaluate(Map<String, SDS2XDX> variables, int errorLimit) {
            if (function.equals("POW") && arguments.size() == 2) {
                SDS2XDX base = arguments.get(0).evaluate(variables, errorLimit);
                SDS2XDX exponent = arguments.get(1).evaluate(variables, errorLimit);
                
                if (base == null || exponent == null) return null;
                
                try {
                    double exp = Double.parseDouble(exponent.s());
                    return base.pow(exp);
                } catch (Exception e) {
                    return null;
                }
            } else if (arguments.size() == 1) {
                SDS2XDX arg = arguments.get(0).evaluate(variables, errorLimit);
                if (arg == null) return null;
                
                switch (function) {
                    case "EXP": return arg.exp();
                    case "LOG": return arg.log();
                    case "LOG10": return arg.log10();
                    case "SQRT": return arg.pow(0.5);
                    case "SIN":
                    case "COS":
                    case "TAN":
                    case "ABS":
                        return applyMathFunction(arg, function, errorLimit);
                    default:
                        return null;
                }
            }
            
            return null;
        }
        
        private SDS2XDX applyMathFunction(SDS2XDX arg, String func, int errorLimit) {
            try {
                double x = arg.x();
                double dxu = arg.dxu() > 0 ? arg.dxu() : arg.dx();
                double dxl = arg.dxl() > 0 ? arg.dxl() : arg.dx();
                
                double resultX;
                double resultDXU;
                double resultDXL;
                
                switch (func) {
                    case "SIN":
                        resultX = Math.sin(x);
                        resultDXU = Math.abs(Math.cos(x)) * dxu;
                        resultDXL = Math.abs(Math.cos(x)) * dxl;
                        break;
                    case "COS":
                        resultX = Math.cos(x);
                        resultDXU = Math.abs(Math.sin(x)) * dxu;
                        resultDXL = Math.abs(Math.sin(x)) * dxl;
                        break;
                    case "TAN":
                        resultX = Math.tan(x);
                        double sec2 = 1.0 / (Math.cos(x) * Math.cos(x));
                        resultDXU = sec2 * dxu;
                        resultDXL = sec2 * dxl;
                        break;
                    case "ABS":
                        resultX = Math.abs(x);
                        if (x > 0) {
                            resultDXU = dxu;
                            resultDXL = dxl;
                        } else if (x < 0) {
                            resultDXU = dxl;
                            resultDXL = dxu;
                        } else {
                            double max = Math.max(Math.abs(x + dxu), Math.abs(x - dxl));
                            resultDXU = max - resultX;
                            resultDXL = resultX - 0;
                        }
                        break;
                    default:
                        return null;
                }
                
                SDS2XDX result = new SDS2XDX();
                result.setErrorLimit(errorLimit);
                result.setValues(resultX, resultDXU, resultDXL);
                return result;
                
            } catch (Exception e) {
                return null;
            }
        }
        
        @Override
        public Set<String> getVariables() {
            Set<String> vars = new HashSet<>();
            for (ExpressionNode arg : arguments) {
                vars.addAll(arg.getVariables());
            }
            return vars;
        }
    }
    
  //-------------------------------------------------------------------------
    // Parser
    //-------------------------------------------------------------------------
    
    private ExpressionNode parse(String expression, Set<String> knownVariables) {
        // 1. Remove white space
        String expr = expression.replaceAll("\\s+", "");
        
        // 2. Pre-process "**" to "^" for uniform exponentiation handling
        expr = expr.replace("**", "^");
        
        try {
            ParseResult result = parseExpression(expr, 0, knownVariables);
            if (result != null && result.nextPos == expr.length()) {
                return result.node;
            }
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
        }
        
        return null;
    }
    
    private ParseResult parseExpression(String expr, int pos, Set<String> knownVariables) {
        ParseResult left = parseTerm(expr, pos, knownVariables);
        if (left == null) return null;
        
        pos = left.nextPos;
        
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '+' || c == '-') {
                pos++;
                ParseResult right = parseTerm(expr, pos, knownVariables);
                if (right == null) return null;
                pos = right.nextPos;
                left = new ParseResult(new BinaryOpNode(String.valueOf(c), left.node, right.node), pos);
            } else {
                break;
            }
        }
        
        return left;
    }
    
    private ParseResult parseTerm(String expr, int pos, Set<String> knownVariables) {
        // Delegates to parseExponent first to enforce higher precedence
        ParseResult left = parseExponent(expr, pos, knownVariables);
        if (left == null) return null;
        
        pos = left.nextPos;
        
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '*' || c == '/') {
                pos++;
                ParseResult right = parseExponent(expr, pos, knownVariables);
                if (right == null) return null;
                pos = right.nextPos;
                left = new ParseResult(new BinaryOpNode(String.valueOf(c), left.node, right.node), pos);
            } else {
                break;
            }
        }
        
        return left;
    }
    
    /**
     * Handles exponentiation ('^') with right-associativity.
     * Precedence: parseTerm (*, /) -> parseExponent (^) -> parseFactor (atoms)
     */
    private ParseResult parseExponent(String expr, int pos, Set<String> knownVariables) {
        ParseResult left = parseFactor(expr, pos, knownVariables);
        if (left == null) return null;

        int currentPos = left.nextPos;

        if (currentPos < expr.length() && expr.charAt(currentPos) == '^') {
            currentPos++;
            // Right-associative recursive call (e.g., 2^3^2 evaluates as 2^(3^2))
            ParseResult right = parseExponent(expr, currentPos, knownVariables);
            if (right == null) return null;

            return new ParseResult(new BinaryOpNode("^", left.node, right.node), right.nextPos);
        }

        return left;
    }
    
    /**
     * Parses a factor (number, variable, function, or parenthesized expression)
     */
    private ParseResult parseFactor(String expr, int pos, Set<String> knownVariables) {
        if (pos >= expr.length()) return null;
        
        char c = expr.charAt(pos);
        
        // Handle unary minus (e.g., -(X-2))
        if (c == '-' && pos + 1 < expr.length() && expr.charAt(pos + 1) == '(') {
            pos++; // Skip the '-'
            ParseResult inner = parseExpression(expr, pos, knownVariables);
            if (inner == null) return null;
            
            // Create a constant -1 and multiply with the inner expression
            ExpressionNode minusOne = new ConstantNode(-1.0);
            ExpressionNode product = new BinaryOpNode("*", minusOne, inner.node);
            return new ParseResult(product, inner.nextPos);
        }
        
        // Handle negative numbers
        if (c == '-' && pos + 1 < expr.length() && 
            (Character.isDigit(expr.charAt(pos + 1)) || expr.charAt(pos + 1) == '.')) {
            pos++;
            int start = pos - 1;
            while (pos < expr.length()) {
                char current = expr.charAt(pos);
                if (Character.isDigit(current) || current == '.' || 
                    current == 'E' || current == 'e') {
                    pos++;
                } else if ((current == '+' || current == '-') && 
                           (expr.charAt(pos-1) == 'E' || expr.charAt(pos-1) == 'e')) {
                    pos++;
                } else {
                    break;
                }
            }
            String numStr = expr.substring(start, pos);
            try {
                double value = Double.parseDouble(numStr);
                return new ParseResult(new ConstantNode(value), pos);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Handle positive unary plus (ignore it)
        if (c == '+' && pos + 1 < expr.length() && expr.charAt(pos + 1) == '(') {
            pos++; // Skip the '+'
            return parseExpression(expr, pos, knownVariables);
        }
        
        // Handle parentheses
        if (c == '(') {
            pos++;
            ParseResult inner = parseExpression(expr, pos, knownVariables);
            if (inner == null) return null;
            
            if (inner.nextPos < expr.length() && expr.charAt(inner.nextPos) == ')') {
                return new ParseResult(inner.node, inner.nextPos + 1);
            }
            return null;
        }
        
        // Handle numbers
        if (Character.isDigit(c) || c == '.') {
            int start = pos;
            boolean hasExponent = false;
            
            while (pos < expr.length()) {
                char current = expr.charAt(pos);
                if (Character.isDigit(current) || current == '.') {
                    pos++;
                } else if ((current == 'E' || current == 'e') && !hasExponent) {
                    hasExponent = true;
                    pos++;
                    if (pos < expr.length() && (expr.charAt(pos) == '+' || expr.charAt(pos) == '-')) {
                        pos++;
                    }
                } else {
                    break;
                }
            }
            
            String numStr = expr.substring(start, pos);
            try {
                double value = Double.parseDouble(numStr);
                return new ParseResult(new ConstantNode(value), pos);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Handle identifiers
        if (Character.isLetter(c)) {
            int start = pos;
            while (pos < expr.length() && (Character.isLetterOrDigit(expr.charAt(pos)) || expr.charAt(pos) == '_')) {
                pos++;
            }
            String identifier = expr.substring(start, pos);
            String upperId = identifier.toUpperCase();
            
            // Check for function call
            if (pos < expr.length() && expr.charAt(pos) == '(') {
                pos++;
                
                List<ExpressionNode> args = new ArrayList<>();
                
                if (expr.charAt(pos) != ')') {
                    while (true) {
                        ParseResult arg = parseExpression(expr, pos, knownVariables);
                        if (arg == null) return null;
                        args.add(arg.node);
                        pos = arg.nextPos;
                        
                        if (pos < expr.length() && expr.charAt(pos) == ',') {
                            pos++;
                        } else {
                            break;
                        }
                    }
                }
                
                if (pos < expr.length() && expr.charAt(pos) == ')') {
                    pos++;
                    
                    if (isValidFunction(upperId)) {
                        if (upperId.equals("POW") && args.size() == 2) {
                            return new ParseResult(new FunctionNode(upperId, args), pos);
                        } else if (args.size() == 1) {
                            return new ParseResult(new FunctionNode(upperId, args.get(0)), pos);
                        }
                    }
                }
                return null;
            }
            
            // Check for variable
            for (String var : knownVariables) {
                if (var.equalsIgnoreCase(identifier)) {
                    return new ParseResult(new VariableNode(var), pos);
                }
            }
        }
        
        return null;
    }
    
    private boolean isValidFunction(String name) {
        return name.equals("SIN") || name.equals("COS") || name.equals("TAN") ||
               name.equals("EXP") || name.equals("LOG") || name.equals("LOG10") ||
               name.equals("SQRT") || name.equals("ABS") || name.equals("POW");
    }
    
    private static class ParseResult {
        ExpressionNode node;
        int nextPos;
        
        ParseResult(ExpressionNode node, int nextPos) {
            this.node = node;
            this.nextPos = nextPos;
        }
    }
    
    /**
     * Represents a parameter range with min and max values
     */
    public static class ParameterRange {
        private double min;
        private double max;
        private SDS2XDX nominalValue; // Optional nominal value with uncertainty
        
        public ParameterRange(double min, double max) {
            this.min = min;
            this.max = max;
            this.nominalValue = null;
        }
        
        public ParameterRange(double min, double max, SDS2XDX nominalValue) {
            this.min = min;
            this.max = max;
            this.nominalValue = nominalValue;
        }
        
        public double getMin() { return min; }
        public double getMax() { return max; }
        public SDS2XDX getNominalValue() { return nominalValue; }
        public boolean hasNominalValue() { return nominalValue != null; }
        
        public double getRandomValue(Random random) {
            return min + random.nextDouble() * (max - min);
        }
    }

    /**
     * Result of optimization containing the optimal value and parameters
     */
    public static class OptimizationResult {
        private double value;
        private double uncertainty;
        private Map<String, Double> parameters;
        private Map<String, SDS2XDX> parameterValues;
        private String method; // Track which method found the result
        
        public OptimizationResult(double value, Map<String, Double> parameters) {
            this(value, 0.0, parameters, new HashMap<>(), "boundary");
        }
        
        public OptimizationResult(double value, double uncertainty, Map<String, Double> parameters) {
            this(value, uncertainty, parameters, new HashMap<>(), "boundary");
        }
        
        public OptimizationResult(double value, Map<String, Double> parameters, 
                                  Map<String, SDS2XDX> parameterValues) {
            this(value, 0.0, parameters, parameterValues, "boundary");
        }
        
        public OptimizationResult(double value, double uncertainty, 
                                  Map<String, Double> parameters, 
                                  Map<String, SDS2XDX> parameterValues,
                                  String method) {
            this.value = value;
            this.uncertainty = uncertainty;
            this.parameters = parameters;
            this.parameterValues = parameterValues;
            this.method = method;
        }
        
        public double getValue() { return value; }
        public double getUncertainty() { return uncertainty; }
        public Map<String, Double> getParameters() { return parameters; }
        public Map<String, SDS2XDX> getParameterValues() { return parameterValues; }
        public String getMethod() { return method; }
        
        public SDS2XDX getParameterAsSDS2XDX(String name) {
            return parameterValues.get(name);
        }
        
        public SDS2XDX getResultAsSDS2XDX() {
            SDS2XDX result = new SDS2XDX();
            if (uncertainty > 0) {
                result.setValues(value, uncertainty);
            } else {
                result.setValues(value, 0.0);
            }
            return result;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Method: ").append(method).append("\n");
            if (uncertainty > 0) {
                sb.append(String.format("Value: %.6g ± %.3g\n", value, uncertainty));
            } else {
                sb.append(String.format("Value: %.6g\n", value));
            }
            sb.append("Parameters:\n");
            for (Map.Entry<String, Double> entry : parameters.entrySet()) {
                sb.append(String.format("  %s = %.6g", entry.getKey(), entry.getValue()));
                if (parameterValues.containsKey(entry.getKey())) {
                    sb.append(" (" + parameterValues.get(entry.getKey()).s() + 
                              "(" + parameterValues.get(entry.getKey()).ds() + "))");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }


    /**
     * Core optimization method using random sampling + gradient refinement
     */
    private OptimizationResult optimize(String expression, Map<String, ParameterRange> parameterRanges,
                                         boolean maximize, int numSamples, int numIterations) {
        try {
            // Parse the expression once
            String cleanExpr = expression.replaceAll("\\s+", "").toUpperCase();
            ExpressionNode ast = parse(cleanExpr, parameterRanges.keySet());
            if (ast == null) {
                throw new IllegalArgumentException("Invalid expression: " + expression);
            }
            
            List<String> variables = new ArrayList<>(parameterRanges.keySet());
            Random random = new Random();
            
            // Phase 1: Random sampling
            double bestValue = maximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            Map<String, Double> bestParams = new HashMap<>();
            
            System.out.println("Phase 1: Random sampling with " + numSamples + " samples...");
            
            for (int i = 0; i < numSamples; i++) {
                Map<String, SDS2XDX> sampleParams = new HashMap<>();
                Map<String, Double> sampleDoubleParams = new HashMap<>();
                
                // Generate random values within ranges
                for (String var : variables) {
                    ParameterRange range = parameterRanges.get(var);
                    double value = range.getRandomValue(random);
                    sampleDoubleParams.put(var, value);
                    
                    // Create SDS2XDX with nominal uncertainty if available
                    SDS2XDX sds = new SDS2XDX();
                    if (range.hasNominalValue()) {
                        SDS2XDX nominal = range.getNominalValue();
                        if (nominal.dsIsAsymm()) {
                            sds.setValues(value, nominal.dxu(), nominal.dxl());
                        } else if (nominal.canGetDX()) {
                            sds.setValues(value, nominal.dx());
                        } else {
                            String s = String.valueOf(value);
                            String ds = nominal.ds();
                            sds.setValues(s, ds);
                        }
                    } else {
                        sds.setValues(value, 0.0);
                    }
                    sds.setErrorLimit(errorLimit);
                    sampleParams.put(var, sds);
                }
                
                try {
                    SDS2XDX result = ast.evaluate(sampleParams, errorLimit);
                    if (result != null) {
                        double val = result.x();
                        
                        if (maximize && val > bestValue) {
                            bestValue = val;
                            bestParams = new HashMap<>(sampleDoubleParams);
                        } else if (!maximize && val < bestValue) {
                            bestValue = val;
                            bestParams = new HashMap<>(sampleDoubleParams);
                        }
                    }
                } catch (Exception e) {
                    // Skip invalid evaluations
                }
            }
            
            System.out.println("Best after sampling: " + bestValue);
            System.out.println("Phase 2: Gradient refinement...");
            
            // Phase 2: Gradient-based refinement
            bestParams = refineOptimization(ast, variables, parameterRanges, bestParams, 
                                            maximize, numIterations);
            
            // Re-evaluate at the optimal point to get final value with uncertainty
            Map<String, SDS2XDX> finalParams = new HashMap<>();
            for (String var : variables) {
                double value = bestParams.get(var);
                ParameterRange range = parameterRanges.get(var);
                
                SDS2XDX sds = new SDS2XDX();
                if (range.hasNominalValue()) {
                    SDS2XDX nominal = range.getNominalValue();
                    if (nominal.dsIsAsymm()) {
                        sds.setValues(value, nominal.dxu(), nominal.dxl());
                    } else if (nominal.canGetDX()) {
                        sds.setValues(value, nominal.dx());
                    } else {
                        String s = String.valueOf(value);
                        String ds = nominal.ds();
                        sds.setValues(s, ds);
                    }
                } else {
                    sds.setValues(value, 0.0);
                }
                sds.setErrorLimit(errorLimit);
                finalParams.put(var, sds);
            }
            
            SDS2XDX finalResult = ast.evaluate(finalParams, errorLimit);
            double finalValue = finalResult != null ? finalResult.x() : bestValue;
            double finalUnc = finalResult != null ? getUncertainty(finalResult) : 0.0;
            
            // FIXED: Use the correct constructor that matches the signature
            // The constructor expects (double value, double uncertainty, Map<String, Double> parameters, Map<String, SDS2XDX> parameterValues, String method)
            return new OptimizationResult(finalValue, finalUnc, bestParams, finalParams, "gradient");
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fast boundary-based optimization - evaluates all combinations of min/max values
     * This is O(2^n) where n is the number of parameters, so use with caution for many parameters
     * 
     * @param expression The mathematical expression to optimize
     * @param parameterRanges Map of parameter names to their min/max ranges
     * @param maximize true for maximum, false for minimum
     * @return OptimizationResult containing the extreme value and parameters
     */
    public OptimizationResult optimizeByBoundary(String expression, 
                                                  Map<String, ParameterRange> parameterRanges,
                                                  boolean maximize) {
        try {
            String cleanExpr = expression.replaceAll("\\s+", "").toUpperCase();
            ExpressionNode ast = parse(cleanExpr, parameterRanges.keySet());
            if (ast == null) {
                throw new IllegalArgumentException("Invalid expression: " + expression);
            }
            
            List<String> variables = new ArrayList<>(parameterRanges.keySet());
            int n = variables.size();
            
            // Generate all combinations of min/max (2^n combinations)
            int numCombinations = 1 << n; // 2^n
            double bestValue = maximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            Map<String, Double> bestParams = new HashMap<>();
            Map<String, SDS2XDX> bestSDSParams = new HashMap<>();
            
            //System.out.println("Boundary search: testing " + numCombinations + " combinations...");
            
            for (int i = 0; i < numCombinations; i++) {
                Map<String, SDS2XDX> evalParams = new HashMap<>();
                Map<String, Double> paramValues = new HashMap<>();
                
                // Set each parameter to either min or max based on bit pattern
                for (int j = 0; j < n; j++) {
                    String var = variables.get(j);
                    ParameterRange range = parameterRanges.get(var);
                    
                    // Use (i >> j) & 1 to determine min (0) or max (1)
                    boolean useMax = ((i >> j) & 1) == 1;
                    double value = useMax ? range.getMax() : range.getMin();
                    paramValues.put(var, value);
                    
                    // Create SDS2XDX with nominal uncertainty if available
                    SDS2XDX sds = new SDS2XDX();
                    if (range.hasNominalValue()) {
                        SDS2XDX nominal = range.getNominalValue();
                        if (nominal.dsIsAsymm()) {
                            sds.setValues(value, nominal.dxu(), nominal.dxl());
                        } else if (nominal.canGetDX()) {
                            sds.setValues(value, nominal.dx());
                        } else {
                            String s = String.valueOf(value);
                            String ds = nominal.ds();
                            sds.setValues(s, ds);
                        }
                    } else {
                        sds.setValues(value, 0.0);
                    }
                    sds.setErrorLimit(errorLimit);
                    evalParams.put(var, sds);
                }
                
                try {
                    SDS2XDX result = ast.evaluate(evalParams, errorLimit);
                    if (result != null) {
                        double val = result.x();
                        
                        if (maximize && val > bestValue) {
                            bestValue = val;
                            bestParams = new HashMap<>(paramValues);
                            bestSDSParams = new HashMap<>(evalParams);
                        } else if (!maximize && val < bestValue) {
                            bestValue = val;
                            bestParams = new HashMap<>(paramValues);
                            bestSDSParams = new HashMap<>(evalParams);
                        }
                    }
                } catch (Exception e) {
                    // Skip invalid combinations
                }
            }
            
            double uncertainty = 0.0;
            if (!bestSDSParams.isEmpty()) {
                SDS2XDX finalResult = ast.evaluate(bestSDSParams, errorLimit);
                if (finalResult != null) {
                    uncertainty = getUncertainty(finalResult);
                }
            }
            
            // FIXED: Use the correct constructor
            return new OptimizationResult(bestValue, uncertainty, bestParams, bestSDSParams, "boundary");
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fast boundary-based maximum finder
     */
    public OptimizationResult findMaxByBoundary(String expression, 
                                                 Map<String, ParameterRange> parameterRanges) {
        return optimizeByBoundary(expression, parameterRanges, true);
    }

    /**
     * Fast boundary-based minimum finder
     */
    public OptimizationResult findMinByBoundary(String expression, 
                                                 Map<String, ParameterRange> parameterRanges) {
        return optimizeByBoundary(expression, parameterRanges, false);
    }

  //-------------------------------------------------------------------------
    // Optimization Methods (Max-Min / Boundary & Interior Turning Search)
    //-------------------------------------------------------------------------

    /**
     * Hybrid approach: tries initial boundary estimate, then refines using 
     * projected gradient descent with normalized directions to find exact interior turning points.
     */
    public OptimizationResult optimizeHybrid(String expression, 
                                              Map<String, ParameterRange> parameterRanges,
                                              boolean maximize,
                                              int numSamples, 
                                              int numIterations,
                                              double tolerance) {
        
        // 1. Obtain an initial estimate from boundary check
        OptimizationResult boundaryResult = optimizeByBoundary(expression, parameterRanges, maximize);
        
        if (boundaryResult != null) {
            Map<String, Double> startPoint = new HashMap<>(boundaryResult.getParameters());
            
            try {
                String cleanExpr = expression.replaceAll("\\s+", "").replace("**", "^").toUpperCase();
                ExpressionNode ast = parse(cleanExpr, parameterRanges.keySet());
                
                if (ast != null) {
                    // Refine using Projected Gradient Descent (finds exact turning points like a = 1.0)
                    Map<String, Double> refined = refineOptimization(ast, 
                        new ArrayList<>(parameterRanges.keySet()), 
                        parameterRanges, 
                        startPoint, 
                        maximize, 
                        numIterations);
                    
                    // Evaluate refined result
                    Map<String, SDS2XDX> evalParams = buildEvalParams(refined, parameterRanges);
                    SDS2XDX refinedResult = ast.evaluate(evalParams, errorLimit);
                    
                    if (refinedResult != null) {
                        double refinedValue = refinedResult.x();
                        double refinedUnc = getUncertainty(refinedResult);
                        
                        double initialValue = boundaryResult.getValue();
                        boolean improved = maximize ? 
                            refinedValue > initialValue + tolerance :
                            refinedValue < initialValue - tolerance;
                        
                        if (improved) {
                            return new OptimizationResult(refinedValue, refinedUnc, refined, 
                                                          evalParams, "hybrid_refined");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Refinement error: " + e.getMessage());
            }
        }
        
        return boundaryResult;
    }

    /**
     * Finds the maximum value of an expression within given parameter ranges
     */
    public OptimizationResult findMax(String expression, Map<String, ParameterRange> parameterRanges,
                                       int numSamples, int numIterations) {
        return optimizeHybrid(expression, parameterRanges, true, numSamples, numIterations, 1e-9);
    }

    /**
     * Finds the maximum value with default sampling parameters
     */
    public OptimizationResult findMax(String expression, Map<String, ParameterRange> parameterRanges) {
        return findMax(expression, parameterRanges, 100000, 1000);
    }

    /**
     * Finds the minimum value of an expression within given parameter ranges
     */
    public OptimizationResult findMin(String expression, Map<String, ParameterRange> parameterRanges,
                                       int numSamples, int numIterations) {
        return optimizeHybrid(expression, parameterRanges, false, numSamples, numIterations, 1e-9);
    }

    /**
     * Finds the minimum value with default sampling parameters
     */
    public OptimizationResult findMin(String expression, Map<String, ParameterRange> parameterRanges) {
        return findMin(expression, parameterRanges, 100000, 1000);
    }

    /**
     * Refines optimization using component-wise, range-scaled gradient steps.
     * Prevents variables with small absolute values/gradients (like b ~ 1e-4)
     * from suppressing step sizes of unit-scale variables (like a ~ 1.0).
     */
    private Map<String, Double> refineOptimization(ExpressionNode ast, List<String> variables,
                                                    Map<String, ParameterRange> parameterRanges,
                                                    Map<String, Double> startPoint,
                                                    boolean maximize, int maxIterations) {
        
        Map<String, Double> currentPoint = new HashMap<>(startPoint);
        double learningRate = 0.05;
        double minStep = 1e-12;
        double h = 1e-5;
        
        for (int iter = 0; iter < maxIterations; iter++) {
            Map<String, SDS2XDX> evalParams = buildEvalParams(currentPoint, parameterRanges);
            SDS2XDX currentResult = ast.evaluate(evalParams, errorLimit);
            if (currentResult == null) break;
            
            double currentValue = currentResult.x();
            Map<String, Double> gradient = new HashMap<>();
            
            // Compute central / boundary-aware finite differences
            for (String var : variables) {
                double val = currentPoint.get(var);
                ParameterRange range = parameterRanges.get(var);
                
                double valPlus = Math.min(range.getMax(), val + h);
                double valMinus = Math.max(range.getMin(), val - h);
                double actualH = valPlus - valMinus;
                
                if (actualH < 1e-12) {
                    gradient.put(var, 0.0);
                    continue;
                }
                
                // Evaluate f(x + h)
                Map<String, SDS2XDX> paramsPlus = new HashMap<>(evalParams);
                paramsPlus.put(var, createSDS(valPlus));
                SDS2XDX resPlus = ast.evaluate(paramsPlus, errorLimit);
                
                // Evaluate f(x - h)
                Map<String, SDS2XDX> paramsMinus = new HashMap<>(evalParams);
                paramsMinus.put(var, createSDS(valMinus));
                SDS2XDX resMinus = ast.evaluate(paramsMinus, errorLimit);
                
                if (resPlus != null && resMinus != null) {
                    double grad = (resPlus.x() - resMinus.x()) / actualH;
                    gradient.put(var, grad);
                } else {
                    gradient.put(var, 0.0);
                }
            }
            
            // Backtracking line search with parameter-range scaling
            boolean improved = false;
            double step = learningRate;
            
            while (step > minStep && !improved) {
                Map<String, Double> candidatePoint = new HashMap<>();
                
                for (String var : variables) {
                    double grad = gradient.getOrDefault(var, 0.0);
                    ParameterRange range = parameterRanges.get(var);
                    double rangeSpan = range.getMax() - range.getMin();
                    
                    // Step direction based on gradient sign, scaled by variable's range span
                    double dir = Math.signum(grad);
                    double delta = (maximize ? 1.0 : -1.0) * step * dir * rangeSpan;
                    
                    double newVal = currentPoint.get(var) + delta;
                    
                    // Box projection onto bounds
                    newVal = Math.max(range.getMin(), Math.min(range.getMax(), newVal));
                    
                    candidatePoint.put(var, newVal);
                }
                
                Map<String, SDS2XDX> candEvalParams = buildEvalParams(candidatePoint, parameterRanges);
                SDS2XDX candResult = ast.evaluate(candEvalParams, errorLimit);
                
                if (candResult != null) {
                    double candValue = candResult.x();
                    boolean isBetter = maximize ? candValue > currentValue : candValue < currentValue;
                    
                    if (isBetter) {
                        currentPoint = candidatePoint;
                        improved = true;
                    }
                }
                
                step *= 0.5; // Backtrack step size if no improvement
            }
            
            if (!improved) {
                break; // Local stationarity / minimum reached
            }
        }
        
        return currentPoint;
    }

    /**
     * Finds the global maximum using multiple starting points
     */
    public OptimizationResult findGlobalMax(String expression, Map<String, ParameterRange> parameterRanges,
                                             int numRestarts, int numSamplesPerRestart, int numIterations) {
        OptimizationResult bestResult = null;
        
        for (int restart = 0; restart < numRestarts; restart++) {
            OptimizationResult result = findMax(expression, parameterRanges, 
                                                numSamplesPerRestart, numIterations);
            
            if (result != null && (bestResult == null || result.getValue() > bestResult.getValue())) {
                bestResult = result;
            }
        }
        
        return bestResult;
    }

    /**
     * Finds the global minimum using multiple starting points
     */
    public OptimizationResult findGlobalMin(String expression, Map<String, ParameterRange> parameterRanges,
                                             int numRestarts, int numSamplesPerRestart, int numIterations) {
        OptimizationResult bestResult = null;
        
        for (int restart = 0; restart < numRestarts; restart++) {
            OptimizationResult result = findMin(expression, parameterRanges, 
                                                numSamplesPerRestart, numIterations);
            
            if (result != null && (bestResult == null || result.getValue() < bestResult.getValue())) {
                bestResult = result;
            }
        }
        
        return bestResult;
    }

    //-------------------------------------------------------------------------
    // Helpers
    //-------------------------------------------------------------------------

    private Map<String, SDS2XDX> buildEvalParams(Map<String, Double> point, Map<String, ParameterRange> ranges) {
        Map<String, SDS2XDX> evalParams = new HashMap<>();
        for (Map.Entry<String, Double> entry : point.entrySet()) {
            String var = entry.getKey();
            double val = entry.getValue();
            evalParams.put(var, createSDS(val));
        }
        return evalParams;
    }

    private SDS2XDX createSDS(double val) {
        SDS2XDX sds = new SDS2XDX();
        sds.setValues(val, 0.0);
        sds.setErrorLimit(errorLimit);
        return sds;
    } 
}