package ensdfparser.test;

import java.util.HashMap;
import java.util.Map;

import ensdfparser.calc.ExpressionCalculatorOld;
import ensdfparser.ensdf.SDS2XDX;

public class TestExprCalc {

    public static void main(String[] args) {
    	test6();
    }
    public static void test6() {
        System.out.println("=========================================================");
        System.out.println("TEST: b*(a+1)^2/3.0/a with a=1.3(4), b=0.00060(15)");
        System.out.println("Using FULL Optimization (not just boundary)");
        System.out.println("=========================================================\n");
        
        ExpressionCalculatorOld calc = new ExpressionCalculatorOld(35);
        
        String expr = "b*(a+1)^2/3.0/a";
        String params = "a=1.3(4);b=0.00060(15)";
        
        System.out.println("Expression: " + expr);
        System.out.println("Parameters: " + params);
        System.out.println();
        
        // Create parameters
        SDS2XDX a = new SDS2XDX("1.3", "4");
        SDS2XDX b = new SDS2XDX("0.00060", "15");
        
        System.out.println("a = " + a.s() + "(" + a.ds() + ")");
        System.out.println("  Value: " + a.x());
        if (a.dx() >= 0) {
            System.out.println("  Uncertainty: " + a.dx());
        } else {
            System.out.println("  Uncertainty: +" + a.dxu() + " / -" + a.dxl());
        }
        System.out.println("  Range: [" + a.xLV() + ", " + a.xUV() + "]");
        System.out.println();
        
        System.out.println("b = " + b.s() + "(" + b.ds() + ")");
        System.out.println("  Value: " + b.x());
        if (b.dx() >= 0) {
            System.out.println("  Uncertainty: " + b.dx());
        } else {
            System.out.println("  Uncertainty: +" + b.dxu() + " / -" + b.dxl());
        }
        System.out.println("  Range: [" + b.xLV() + ", " + b.xUV() + "]");
        System.out.println();
        
        // Create ranges
        Map<String, ExpressionCalculatorOld.ParameterRange> ranges = new HashMap<>();
        ranges.put("a", new ExpressionCalculatorOld.ParameterRange(a.xLV(), a.xUV(), a));
        ranges.put("b", new ExpressionCalculatorOld.ParameterRange(b.xLV(), b.xUV(), b));
        
        // ====== USE FULL OPTIMIZATION ======
        System.out.println("Using FULL optimization (random sampling + gradient refinement)...");
        System.out.println();
        
        ExpressionCalculatorOld.OptimizationResult maxResult = calc.findMax(expr, ranges, 100000, 1000);
        ExpressionCalculatorOld.OptimizationResult minResult = calc.findMin(expr, ranges, 100000, 1000);
        
        // Calculate using the calculator (Taylor method + Max-Min)
        SDS2XDX result = calc.calculate(expr, params);
        SDS2XDX maxMinResult = calc.getMaxMinResult();
        
        System.out.println("=========================================================");
        System.out.println("RESULTS");
        System.out.println("=========================================================");
        
        // Taylor Expansion method
        System.out.println("\n【Taylor Expansion Method (Derivative Propagation)】");
        if (result != null) {
            System.out.println("  Value:        " + result.x());
            printUncertainty(result);
        }
        
        // Max-Min method
        System.out.println("\n【Max-Min Method (FULL Optimization)】");
        if (maxMinResult != null) {
            System.out.println("  Value:        " + maxMinResult.x());
            printUncertainty(maxMinResult);
            System.out.println("  Range:        [" + maxMinResult.xLV() + ", " + maxMinResult.xUV() + "]");
        }
        
        // Where max and min are achieved
        System.out.println("\n=========================================================");
        System.out.println("WHERE MAX AND MIN ARE ACHIEVED");
        System.out.println("=========================================================");
        
        if (maxResult != null) {
            System.out.println("\n【Maximum】");
            System.out.println("  Value: " + maxResult.getValue());
            System.out.println("  Method: " + maxResult.getMethod());
            System.out.println("  Achieved at:");
            Map<String, Double> maxParams = maxResult.getParameters();
            for (Map.Entry<String, Double> entry : maxParams.entrySet()) {
                System.out.println("    " + entry.getKey() + " = " + entry.getValue());
            }
        }
        
        if (minResult != null) {
            System.out.println("\n【Minimum】");
            System.out.println("  Value: " + minResult.getValue());
            System.out.println("  Method: " + minResult.getMethod());
            System.out.println("  Achieved at:");
            Map<String, Double> minParams = minResult.getParameters();
            for (Map.Entry<String, Double> entry : minParams.entrySet()) {
                System.out.println("    " + entry.getKey() + " = " + entry.getValue());
            }
        }
        
        // Comparison
        System.out.println("\n=========================================================");
        System.out.println("COMPARISON");
        System.out.println("=========================================================");
        
        if (result != null && maxMinResult != null) {
            double taylorUnc = result.dx() >= 0 ? result.dx() : (result.dxu() + result.dxl()) / 2.0;
            double mmUnc = maxMinResult.dx() >= 0 ? maxMinResult.dx() : (maxMinResult.dxu() + maxMinResult.dxl()) / 2.0;
            
            System.out.printf("  Taylor uncertainty:  %.6f\n", taylorUnc);
            System.out.printf("  Max-Min uncertainty: %.6f (upper: %.6f, lower: %.6f)\n", 
                              mmUnc, maxMinResult.dxu(), maxMinResult.dxl());
            System.out.printf("  Ratio (Max-Min/Taylor): %.2f\n", mmUnc / taylorUnc);
            
            if (mmUnc > taylorUnc * 1.05) {
                System.out.println("\n  ✓ Max-Min gives LARGER uncertainty (more conservative)");
            } else if (mmUnc < taylorUnc * 0.95) {
                System.out.println("\n  ✓ Taylor gives LARGER uncertainty (more conservative)");
            } else {
                System.out.println("\n  ✓ Both methods give similar uncertainties");
            }
        }
        
        System.out.println("\n=========================================================");
        System.out.println("INTERPRETATION");
        System.out.println("=========================================================");
        System.out.println("  Taylor Method: Linear approximation using derivatives");
        System.out.println("  Max-Min Method: Searches actual min/max within ranges");
        System.out.println("  For non-monotonic functions, Max-Min is more accurate");
        System.out.println("\n  Note: The minimum at a=1.0 is an INTERIOR point.");
        System.out.println("  Boundary-only methods would miss this!");
        System.out.println("  Full optimization finds it via random sampling + gradient.");
        System.out.println("\n  Note: For asymmetric uncertainties, dx() = -1.0 indicates");
        System.out.println("  you should use dxu() and dxl() instead.");
    }
    
    /**
     * Print uncertainty correctly handling symmetric vs asymmetric
     */
    private static void printUncertainty(SDS2XDX sds) {
        if (sds.dx() >= 0) {
            // Symmetric uncertainty
            System.out.println("  Uncertainty:  " + sds.dx());
            System.out.println("  Format:       " + sds.s() + "(" + sds.ds() + ")");
        } else {
            // Asymmetric uncertainty - use dxu and dxl
            System.out.println("  Uncertainty (asymmetric):");
            System.out.println("    +" + sds.dxu() + " / -" + sds.dxl());
            System.out.println("  Format:       " + sds.s() + "(+" + sds.dsu() + "-" + sds.dsl() + ")");
        }
    }

    public static void test5() {
        System.out.println("=========================================================");
        System.out.println("SIMPLE TEST: a/(a+b) with a=12(1), b=13(2)");
        System.out.println("=========================================================\n");
        
        ExpressionCalculatorOld calc = new ExpressionCalculatorOld(35);
        
        // METHOD 1: Using the string parameter method
        String expr = "a/(a+b)";
        String params = "a=12(1);b=13(2)";
        
        System.out.println("Expression: " + expr);
        System.out.println("Parameters: " + params);
        System.out.println();
        
        SDS2XDX result1 = calc.calculate(expr, params);
        System.out.println("Result from string method: " + result1);
        if (result1 != null) {
            System.out.println("  Value: " + result1.x());
            System.out.println("  Uncertainty: " + result1.dx());
        }
        System.out.println();
        
        // METHOD 2: Manual calculation to verify
        SDS2XDX a = new SDS2XDX("12", "1");
        SDS2XDX b = new SDS2XDX("13", "2");
        
        SDS2XDX numerator = a;
        SDS2XDX denominator = a.add(b);
        SDS2XDX result2 = numerator.divided(denominator);
        
        System.out.println("Manual calculation: " + result2);
        System.out.println("  Value: " + result2.x());
        System.out.println("  Uncertainty: " + result2.dx());
        System.out.println();
        
        // METHOD 3: Using the calculator with explicit map
        Map<String, SDS2XDX> paramMap = new HashMap<>();
        paramMap.put("a", a);
        paramMap.put("b", b);
        
        SDS2XDX result3 = calc.calculate(expr, paramMap);
        System.out.println("Result from map method: " + result3);
        if (result3 != null) {
            System.out.println("  Value: " + result3.x());
            System.out.println("  Uncertainty: " + result3.dx());
        }
        
        // Expected values
        System.out.println("\nExpected:");
        System.out.println("  Value: 0.48");
        System.out.println("  Uncertainty: ~0.044");
    }
    
    public static void test4() {
    	ExpressionCalculatorOld calc = new ExpressionCalculatorOld(35);
    	
    	// Simple example
    	SDS2XDX result1 = calc.calculate("a/(a + b)", "a=12(1);b=13(2)");
    	System.out.println(result1.s()+" "+result1.ds()); // Should be 25(2) or similar

    	// Your complex expression
    	String expr = "(A + G * C + POW(D,2)) / (E + EXP(F) + LOG(G))";
    	String params = "A=1.0(1);B=2.0(2);C=3.0(3);D=4.0(4);E=5.0(5);F=0.5(5);G=2.0(2)";

    	SDS2XDX result2 = calc.calculate(expr, params);
    	System.out.println(result2.s()+"  "+result2.ds());

    	// With numbers only (no uncertainties)
    	SDS2XDX result3 = calc.calculate("x*y + z", "x=2.5;y=3.0;z=4.0");
    	System.out.println(result3.s()+" "+result3.ds()); // 11.5(0)
    }
    public static void test3() {
        System.out.println("=========================================================");
        System.out.println("TESTING FOR CORRELATION BUG - Central Value Should Not Change");
        System.out.println("=========================================================\n");
        
        ExpressionCalculatorOld calc = new ExpressionCalculatorOld(35);
        
        // Create parameters
        SDS2XDX a = new SDS2XDX("1.0", "1");    // 1.0 ± 0.1
        SDS2XDX b = new SDS2XDX("2.0", "2");    // 2.0 ± 0.2
        SDS2XDX c = new SDS2XDX("3.0", "3");    // 3.0 ± 0.3
        SDS2XDX d = new SDS2XDX("4.0", "4");    // 4.0 ± 0.4
        SDS2XDX e = new SDS2XDX("5.0", "5");    // 5.0 ± 0.5
        SDS2XDX f = new SDS2XDX("0.5", "5");    // 0.5 ± 0.5
        SDS2XDX g = new SDS2XDX("2.0", "2");    // 2.0 ± 0.2 (same value as B)
        
        // CASE 1: Using G (different variable, same value)
        System.out.println("CASE 1: Using G (different variable, same value as B)");
        System.out.println("-----------------------------------------------------");
        
        Map<String, SDS2XDX> params1 = new HashMap<>();
        params1.put("A", a);
        params1.put("B", b);
        params1.put("C", c);
        params1.put("D", d);
        params1.put("E", e);
        params1.put("F", f);
        params1.put("G", g);  // G is different object, same value as B
        
        String expr1 = "(A + B * C + POW(D,2)) / (E + EXP(F) + LOG(G))";
        SDS2XDX result1 = calc.calculate(expr1, params1);
        
        System.out.println("Expression: " + expr1);
        System.out.println("Result: " + result1);
        System.out.println("  Value: " + result1.x());
        System.out.println("  Uncertainty: " + result1.dx());
        System.out.println();
        
        // CASE 2: Replacing G with B (same variable appears twice)
        System.out.println("CASE 2: Using B instead of G (same variable appears twice)");
        System.out.println("----------------------------------------------------------");
        
        Map<String, SDS2XDX> params2 = new HashMap<>();
        params2.put("A", a);
        params2.put("B", b);  // B will appear twice
        params2.put("C", c);
        params2.put("D", d);
        params2.put("E", e);
        params2.put("F", f);
        params2.put("G", g);  // G is different object, same value as B
        // No G - using B in denominator instead
        
        String expr2 = "(A + G * C + POW(D,2)) / (E + EXP(F) + LOG(G))";  // B instead of G
        SDS2XDX result2 = calc.calculate(expr2, params2);
        
        System.out.println("Expression: " + expr2);
        System.out.println("Result: " + result2);
        System.out.println("  Value: " + result2.x());
        System.out.println("  Uncertainty: " + result2.dx());
        System.out.println();
        
        // COMPARISON
        System.out.println("=========================================================");
        System.out.println("VERIFICATION");
        System.out.println("=========================================================");
        System.out.printf("Case 1 (using G): %s\n", result1.s());
        System.out.printf("Case 2 (using B): %s\n", result2.s());
        System.out.printf("Value difference: %.10f\n", Math.abs(result1.x() - result2.x()));
        
        if (Math.abs(result1.x() - result2.x()) < 1e-10) {
            System.out.println("✓ PASS: Central values are identical (good!)");
        } else {
            System.out.println("✗ FAIL: Central values differ - BUG DETECTED!");
            System.out.println("  Correlation should NOT affect central value");
        }
    }
   
    public static void test2() {
        System.out.println("=========================================================");
        System.out.println("COMPARISON: ExpressionCalculator vs SDS2XDX.eval()");
        System.out.println("=========================================================\n");
        
        // Create calculator
        ExpressionCalculatorOld calc = new ExpressionCalculatorOld(35);
        
        // Define parameters with uncertainties
        Map<String, SDS2XDX> params = new HashMap<>();
        params.put("A", new SDS2XDX("1.0", "1"));    // 1.0 ± 0.1
        params.put("B", new SDS2XDX("2.0", "2"));    // 2.0 ± 0.2
        params.put("C", new SDS2XDX("3.0", "3"));    // 3.0 ± 0.3
        params.put("D", new SDS2XDX("4.0", "4"));    // 4.0 ± 0.4
        params.put("E", new SDS2XDX("5.0", "5"));    // 5.0 ± 0.5
        params.put("F", new SDS2XDX("0.5", "5"));    // 0.5 ± 0.5
        params.put("G", new SDS2XDX("2.0", "2"));    // 2.0 ± 0.2
        
        System.out.println("Parameters:");
        for (Map.Entry<String, SDS2XDX> entry : params.entrySet()) {
            System.out.printf("  %s = %s\n", entry.getKey(), entry.getValue());
        }
        System.out.println();
        
        /*
        // SIMPLER TEST 1: Basic arithmetic to verify both work
        System.out.println("TEST 1: Simple Addition (should be 3.0)");
        System.out.println("----------------------------------------");
        
        Map<String, SDS2XDX> simpleParams = new HashMap<>();
        simpleParams.put("X", new SDS2XDX("1.0", "1"));
        simpleParams.put("Y", new SDS2XDX("2.0", "2"));
        
        Map<String, String> simpleStringParams = new HashMap<>();
        simpleStringParams.put("X", "1.0(1)");
        simpleStringParams.put("Y", "2.0(2)");
        
        SDS2XDX result1a = calc.calculate("X + Y", simpleParams);
        SDS2XDX result1b = SDS2XDX.eval("X + Y", (HashMap<String, SDS2XDX>) simpleParams, 35);
        
        System.out.println("resulta="+result1a.s()+" "+result1a.ds());
        System.out.println("resultb="+result1b.s()+" "+result1b.ds());
        
        System.out.println("ExpressionCalculator: " + (result1a != null ? result1a : "null"));
        System.out.println("SDS2XDX.eval():        " + (result1b != null ? result1b : "null"));
        System.out.println();
        
        // TEST 2: Correlation test - X * X
        System.out.println("TEST 2: Correlation Test - X * X (X=10.0±0.1)");
        System.out.println("-----------------------------------------------");
        System.out.println("Expected correlated: 100.0(20)");
        System.out.println("Expected uncorrelated: 100.0(14)\n");
        
        Map<String, SDS2XDX> xParams = new HashMap<>();
        xParams.put("X", new SDS2XDX("10.0", "1"));
        
        Map<String, String> xStringParams = new HashMap<>();
        xStringParams.put("X", "10.0(1)");
        
        // Test with different methods
        SDS2XDX mult = xParams.get("X").multiply(xParams.get("X"));
        SDS2XDX pow = xParams.get("X").pow(2);
        SDS2XDX exprCalc = calc.calculate("X * X", xParams);
        SDS2XDX sdsEval = SDS2XDX.eval("X * X", (HashMap<String, SDS2XDX>) xParams, 35);
        
        System.out.println("SDS2XDX.multiply():   " + mult);
        System.out.println("SDS2XDX.pow(2):       " + pow);
        System.out.println("ExpressionCalculator: " + exprCalc.s()+" "+exprCalc.ds());
        System.out.println("SDS2XDX.eval():       " + sdsEval.s()+" "+sdsEval.ds());
        System.out.println();
        */
        // TEST 3: Complex expression
        System.out.println("TEST 3: Complex Expression");
        System.out.println("---------------------------");
        
        Map<String, String> complexStringParams = new HashMap<>();
        for (Map.Entry<String, SDS2XDX> entry : params.entrySet()) {
            complexStringParams.put(entry.getKey(), entry.getValue().s() + "(" + entry.getValue().ds() + ")");
        }
        
        String complexExpr = "(A + G * C + POW(D,2)) / (E + EXP(F) + LOG(G))";
        System.out.println("Expression: " + complexExpr);
        System.out.println();
        
        SDS2XDX exprCalcComplex = calc.calculate(complexExpr, params);
        
        SDS2XDX.setIsAllowMaxMinEPByDefault(true);
        SDS2XDX sdsEvalComplex = SDS2XDX.eval(complexExpr,(HashMap<String, SDS2XDX>) params, 35);
        //SDS2XDX sdsEvalComplex = SDS2XDX.eval("(1.0(1)+2.0(2)*3.0(3)+pow(4.0(4),2))/(5.0(5)+exp(0.5(5))+log(2.0(2)))", 35);
        
        System.out.println("ExpressionCalculator: " + exprCalcComplex);
        if (exprCalcComplex != null) {
            System.out.println("  Value: " + exprCalcComplex.x() + " s=" + exprCalcComplex.s());
            System.out.println("  Uncertainty: " + exprCalcComplex.dx() + " " + 
                               exprCalcComplex.dxu() + "  " + exprCalcComplex.dxl() + 
                               " ds=" + exprCalcComplex.ds());
            
            
            // Create parameter ranges from the SDS2XDX objects' own ranges (xLV, xUV)
            Map<String, ExpressionCalculatorOld.ParameterRange> ranges = new HashMap<>();
            for (Map.Entry<String, SDS2XDX> entry : params.entrySet()) {
                SDS2XDX sds = entry.getValue();
                ranges.put(entry.getKey(), 
                    new ExpressionCalculatorOld.ParameterRange(sds.xLV(), sds.xUV(), sds));
            }
            
            // Find max and min of the expression within these ranges
            System.out.println("\n  Finding max/min within parameter ranges:");
            //ExpressionCalculator.OptimizationResult maxResult = calc.findMax(complexExpr, ranges);
            //ExpressionCalculator.OptimizationResult minResult = calc.findMin(complexExpr, ranges);
            
            ExpressionCalculatorOld.OptimizationResult maxResult = calc.findMaxByBoundary(complexExpr, ranges);
            ExpressionCalculatorOld.OptimizationResult minResult = calc.findMinByBoundary(complexExpr, ranges);
     
          
            //System.out.println("  MAX = " + maxResult);
            //System.out.println("  MIN = " + minResult);
            double x=exprCalcComplex.x();
            System.out.println("  EP ="+x+"  "+(maxResult.getValue()-x)+"  "+(x-minResult.getValue()));
            
             
            
            SDS2XDX result=exprCalcComplex;//result using regular error propagation method (Taylor's expansion)
            SDS2XDX MaxMinResult=calc.getMaxMinResult();//result using max-min method
            
            System.out.println("  MAX = " + MaxMinResult.xUV());
            System.out.println("  MIN = " + MaxMinResult.xLV());

            System.out.println("   s="+MaxMinResult.s()+" dsu="+MaxMinResult.dsu()+" dsl="+MaxMinResult.dsl());
            System.out.println("   x="+MaxMinResult.x()+" dxu="+MaxMinResult.dxu()+" dxl="+MaxMinResult.dxl());
        }
        
        System.out.println("\nSDS2XDX.eval():       " + sdsEvalComplex);
        if (sdsEvalComplex != null) {
            System.out.println("  Value: " + sdsEvalComplex.x()+" s="+sdsEvalComplex.s());
            System.out.println("  Uncertainty: " + sdsEvalComplex.dx()+" ds="+sdsEvalComplex.ds());
            System.out.println("    dxu="+sdsEvalComplex.dxu()+"  dxl="+sdsEvalComplex.dxl());
        }
    }
    
    public static void test1() {
        System.out.println("==========================================");
        System.out.println("EXPRESSION CALCULATOR TEST");
        System.out.println("==========================================\n");
        
        ExpressionCalculatorOld calc = new ExpressionCalculatorOld(35);
        
        //-----------------------------------------------------------------------
        // TEST 1: Basic arithmetic with no correlations
        //-----------------------------------------------------------------------
        System.out.println("TEST 1: Basic Arithmetic");
        System.out.println("------------------------");
        
        Map<String, SDS2XDX> params = new HashMap<>();
        params.put("A", new SDS2XDX("10.0", "1"));   // 10.0 ± 0.1
        params.put("B", new SDS2XDX("5.0", "5"));    // 5.0 ± 0.5
        params.put("C", new SDS2XDX("2.0", "0"));    // 2.0 ± 0.0
        
        System.out.println("A = " + printSDS2XDX(params.get("A")));
        System.out.println("B = " + printSDS2XDX(params.get("B")));
        System.out.println("C = " + printSDS2XDX(params.get("C")) + "\n");
        
        printDetailedResult(calc, "A + B", params);
        printDetailedResult(calc, "A - B", params);
        printDetailedResult(calc, "A * C", params);
        printDetailedResult(calc, "A / B", params);
        printDetailedResult(calc, "A + B * C", params);
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 2: Correlated variables
        //-----------------------------------------------------------------------
        System.out.println("TEST 2: Correlated Variables");
        System.out.println("-----------------------------");
        
        Map<String, SDS2XDX> params2 = new HashMap<>();
        params2.put("X", new SDS2XDX("10.0", "1"));  // 10.0 ± 0.1
        
        System.out.println("X = " + printSDS2XDX(params2.get("X")) + "\n");
        
        printDetailedResult(calc, "X * X", params2);
        printDetailedResult(calc, "X^2", params2);
        printDetailedResult(calc, "X + X", params2);
        printDetailedResult(calc, "X^2 + X", params2);
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 3: Multiple variables
        //-----------------------------------------------------------------------
        System.out.println("TEST 3: Multiple Variables");
        System.out.println("---------------------------");
        
        Map<String, SDS2XDX> params3 = new HashMap<>();
        params3.put("X", new SDS2XDX("10.0", "1"));  // 10.0 ± 0.1
        params3.put("Y", new SDS2XDX("5.0", "5"));   // 5.0 ± 0.5
        
        System.out.println("X = " + printSDS2XDX(params3.get("X")));
        System.out.println("Y = " + printSDS2XDX(params3.get("Y")) + "\n");
        
        printDetailedResult(calc, "X^2 + Y^2", params3);
        printDetailedResult(calc, "(X+Y)*(X-Y)", params3);
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 4: Functions
        //-----------------------------------------------------------------------
        System.out.println("TEST 4: Functions");
        System.out.println("-----------------");
        
        printDetailedResult(calc, "SIN(X)", params3);
        printDetailedResult(calc, "COS(X)", params3);
        printDetailedResult(calc, "EXP(0.5)", params3);
        printDetailedResult(calc, "LOG(X)", params3);
        printDetailedResult(calc, "LOG10(X)", params3);
        printDetailedResult(calc, "SQRT(X)", params3);
        printDetailedResult(calc, "ABS(-5)", params3);
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 5: Power function
        //-----------------------------------------------------------------------
        System.out.println("TEST 5: Power Function");
        System.out.println("----------------------");
        
        printDetailedResult(calc, "POW(X,3)", params3);
        printDetailedResult(calc, "POW(X,0.5)", params3);
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 6: Nested expressions
        //-----------------------------------------------------------------------
        System.out.println("TEST 6: Nested Expressions");
        System.out.println("---------------------------");
        
        printDetailedResult(calc, "SIN(X^2) + COS(Y)", params3);
        printDetailedResult(calc, "EXP(SIN(X) + COS(Y))", params3);
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 7: Derivatives
        //-----------------------------------------------------------------------
        System.out.println("TEST 7: Derivatives");
        System.out.println("-------------------");
        
        SDS2XDX deriv = calc.derivative("X * X", "X", params3);
        System.out.println("d/dX (X^2) at X=10 = " + (deriv != null ? printSDS2XDX(deriv) : "null"));
        
        deriv = calc.derivative("SIN(X)", "X", params3);
        System.out.println("d/dX SIN(X) at X=10 = " + (deriv != null ? printSDS2XDX(deriv) : "null"));
        
        Map<String, SDS2XDX> allDerivs = calc.allDerivatives("X^2 + Y^2", params3);
        System.out.println("\nAll derivatives of X^2 + Y^2:");
        for (Map.Entry<String, SDS2XDX> entry : allDerivs.entrySet()) {
            System.out.println("  d/d" + entry.getKey() + " = " + printSDS2XDX(entry.getValue()));
        }
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 8: Double parameters
        //-----------------------------------------------------------------------
        System.out.println("TEST 8: Double Parameters");
        System.out.println("-------------------------");
        
        Map<String, Double> doubleParams = new HashMap<>();
        doubleParams.put("A", 2.5);
        doubleParams.put("B", 3.0);
        doubleParams.put("C", 4.0);
        
        System.out.println("a = " + doubleParams.get("A"));
        System.out.println("b = " + doubleParams.get("B"));
        System.out.println("c = " + doubleParams.get("C") + "\n");
        
        SDS2XDX res = calc.calculateWithDoubles("A * B + C", doubleParams);
        System.out.println("a*b + c = " + printSDS2XDX(res));
        
        res = calc.calculateWithDoubles("POW(A,2) + SQRT(B*C)", doubleParams);
        System.out.println("a^2 + sqrt(b*c) = " + printSDS2XDX(res));
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 9: String parameters
        //-----------------------------------------------------------------------
        System.out.println("TEST 9: String Parameters");
        System.out.println("-------------------------");
        
        Map<String, String> stringParams = new HashMap<>();
        stringParams.put("E", "123.4(5)");
        stringParams.put("F", "45.6(12)");
        stringParams.put("G", "7.8(3)");
        
        System.out.println("E = " + stringParams.get("E"));
        System.out.println("F = " + stringParams.get("F"));
        System.out.println("G = " + stringParams.get("G") + "\n");
        
        res = calc.calculateWithStrings("E + F", stringParams);
        System.out.println("E + F = " + printSDS2XDX(res));
        
        res = calc.calculateWithStrings("E * G", stringParams);
        System.out.println("E * G = " + printSDS2XDX(res));
        System.out.println();
        
        //-----------------------------------------------------------------------
        // TEST 10: Complex expression
        //-----------------------------------------------------------------------
        System.out.println("TEST 10: Complex Expression");
        System.out.println("---------------------------");
        
        Map<String, SDS2XDX> complexParams = new HashMap<>();
        complexParams.put("A", new SDS2XDX("1.0", "1"));    // 1.0 ± 0.1
        complexParams.put("B", new SDS2XDX("2.0", "2"));    // 2.0 ± 0.2
        complexParams.put("C", new SDS2XDX("3.0", "3"));    // 3.0 ± 0.3
        complexParams.put("D", new SDS2XDX("4.0", "4"));    // 4.0 ± 0.4
        complexParams.put("E", new SDS2XDX("5.0", "5"));    // 5.0 ± 0.5
        complexParams.put("F", new SDS2XDX("0.5", "5"));    // 0.5 ± 0.5
        complexParams.put("G", new SDS2XDX("2.0", "2"));    // 2.0 ± 0.2
        
        System.out.println("Parameters:");
        for (Map.Entry<String, SDS2XDX> entry : complexParams.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + printSDS2XDX(entry.getValue()));
        }
        System.out.println();
        
        String complexExpr = "(A + B * C + POW(D,2)) / (E + EXP(F) + LOG(G))";
        System.out.println("Expression: " + complexExpr);
        
        res = calc.calculate(complexExpr, complexParams);
        System.out.println("Result = " + printSDS2XDX(res));
        
        SDS2XDX derivA = calc.derivative(complexExpr, "A", complexParams);
        System.out.println("d/dA = " + (derivA != null ? printSDS2XDX(derivA) : "null"));
        
      //-----------------------------------------------------------------------
     // TEST 11: Optimization Methods Comparison
     //-----------------------------------------------------------------------
     System.out.println("\nTEST 11: Optimization Methods Comparison");
     System.out.println("----------------------------------------");

     // Define parameter ranges
     Map<String, ExpressionCalculatorOld.ParameterRange> ranges = new HashMap<>();
     ranges.put("X", new ExpressionCalculatorOld.ParameterRange(-10, 10));
     ranges.put("Y", new ExpressionCalculatorOld.ParameterRange(-10, 10));

     // Test 11a: Simple parabola - maximum at boundary? Actually at X=2 (interior)
     String optExpr1 = "-(X-2)^2 + 5";  // Maximum at X=2 (interior), value=5
     System.out.println("Function 1: " + optExpr1 + " (maximum at interior point X=2)");

     long startTime = System.currentTimeMillis();
     ExpressionCalculatorOld.OptimizationResult boundaryMax = calc.findMaxByBoundary(optExpr1, ranges);
     long boundaryTime = System.currentTimeMillis() - startTime;
     System.out.println("Boundary method: " + boundaryMax);
     System.out.println("Time: " + boundaryTime + " ms");

     startTime = System.currentTimeMillis();
     ExpressionCalculatorOld.OptimizationResult globalMax = calc.findGlobalMax(optExpr1, ranges, 3, 10000, 500);
     long globalTime = System.currentTimeMillis() - startTime;
     System.out.println("Global optimization: " + globalMax);
     System.out.println("Time: " + globalTime + " ms");

     startTime = System.currentTimeMillis();
     ExpressionCalculatorOld.OptimizationResult hybridMax = calc.optimizeHybrid(optExpr1, ranges, true, 10000, 500, 1e-6);
     long hybridTime = System.currentTimeMillis() - startTime;
     System.out.println("Hybrid method: " + hybridMax);
     System.out.println("Time: " + hybridTime + " ms");

     // Test 11b: Linear function - maximum at boundary
     String optExpr2 = "2*X + Y";  // Maximum at X=10, Y=10 (boundary)
     System.out.println("\nFunction 2: " + optExpr2 + " (maximum at boundary)");

     boundaryMax = calc.findMaxByBoundary(optExpr2, ranges);
     System.out.println("Boundary method: " + boundaryMax);

     globalMax = calc.findGlobalMax(optExpr2, ranges, 3, 10000, 500);
     System.out.println("Global optimization: " + globalMax);

     // Test 11c: Rosenbrock function - minimum at (1,1) (interior)
     String optExpr3 = "(1-X)^2 + 100*(Y-X^2)^2";  // Minimum at (1,1)
     System.out.println("\nFunction 3: " + optExpr3 + " (Rosenbrock, minimum at interior)");

     ExpressionCalculatorOld.OptimizationResult boundaryMin = calc.findMinByBoundary(optExpr3, ranges);
     System.out.println("Boundary method: " + boundaryMin);

     ExpressionCalculatorOld.OptimizationResult hybridMin = calc.optimizeHybrid(optExpr3, ranges, false, 20000, 1000, 1e-6);
     System.out.println("Hybrid method: " + hybridMin);
        
        System.out.println("\n==========================================");
        System.out.println("TESTS COMPLETED");
        System.out.println("==========================================");
    }
    
    /**
     * Prints a detailed result showing both the expression and its calculated value
     */
    private static void printDetailedResult(ExpressionCalculatorOld calc, String expr, 
                                            Map<String, SDS2XDX> params) {
        SDS2XDX result = calc.calculate(expr, params);
        String resultStr = (result != null) ? printSDS2XDX(result) : "null";
        System.out.println(expr + " = " + resultStr);
    }
    
    /**
     * Formats an SDS2XDX object for display, showing both value and uncertainty
     */
    private static String printSDS2XDX(SDS2XDX sds) {
        if (sds == null) return "null";
        
        if(sds.ds().length()>0)
        	return sds.s()+"("+sds.ds()+")";
        else
        	return sds.s();
    }
}