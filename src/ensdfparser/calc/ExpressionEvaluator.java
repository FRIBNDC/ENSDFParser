package ensdfparser.calc;

public class ExpressionEvaluator {

    public static double evaluate(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return 0.0;
        }

        // Standardize exponentiation operator and strip whitespace
        final String cleanExpr = expr.replace("**", "^").replaceAll("\\s+", "");

        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < cleanExpr.length()) ? cleanExpr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < cleanExpr.length()) {
                    throw new RuntimeException("Unexpected character: " + (char) ch + " at position " + pos);
                }
                return x;
            }

            // Grammar: Expression = Term ('+' Term | '-' Term)*
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            // Grammar: Term = Factor ('*' Factor | '/' Factor)*
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            // Grammar: Factor = Unary ('^' Factor)*
            double parseFactor() {
                if (eat('+')) return +parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    // Read numbers including scientific notation (e.g., 6.0E-4 or 6.0e-4)
                    while ((ch >= '0' && ch <= '9') || ch == '.' || ch == 'E' || ch == 'e') {
                        if (ch == 'E' || ch == 'e') {
                            nextChar();
                            if (ch == '+' || ch == '-') nextChar();
                        } else {
                            nextChar();
                        }
                    }
                    String numStr = cleanExpr.substring(startPos, this.pos);
                    x = Double.parseDouble(numStr);
                } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '√') {
                    // Supported functions
                    while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '√') nextChar();
                    String func = cleanExpr.substring(startPos, this.pos);
                    x = parseFactor();

                    if (func.equalsIgnoreCase("sqrt") || func.equals("√")) x = Math.sqrt(x);
                    else if (func.equalsIgnoreCase("sin"))  x = Math.sin(x);
                    else if (func.equalsIgnoreCase("cos"))  x = Math.cos(x);
                    else if (func.equalsIgnoreCase("tan"))  x = Math.tan(x);
                    else if (func.equalsIgnoreCase("log"))  x = Math.log(x);
                    else if (func.equalsIgnoreCase("log10"))  x = Math.log10(x);
                    else if (func.equalsIgnoreCase("ln"))   x = Math.log(x);
                    else if (func.equalsIgnoreCase("exp"))  x = Math.exp(x);
                    else if (func.equalsIgnoreCase("abs"))  x = Math.abs(x);
                    else throw new RuntimeException("Unknown function or unreplaced variable: " + func);
                } else {
                    throw new RuntimeException("Unexpected character: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }
}