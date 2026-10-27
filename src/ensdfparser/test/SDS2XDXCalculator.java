package ensdfparser.test;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ensdfparser.ensdf.SDS2XDX;

//import ensdfparser.ensdf.SDS2XDX;

public class SDS2XDXCalculator {


    public static void main(String[] args) {
    	SDS2XDX sx1=new SDS2XDX("15","+5-6");
    	SDS2XDX sx2=new SDS2XDX("0.693","+6-18");
    	sx1.setErrorLimit(99);
    	sx1=sx1.multiply(sx2);
    	System.out.println(sx1.toString()+" result: "+sx1.s()+" "+sx1.ds());
    	
        /*
    	// Example usage
        SDS2XDX cc = new SDS2XDX("0.50", "1");
        //String expression = "(1.22e-4(2)*(2.23+CC*4.56(9)))";
        String expression = "(1.22(3)/(1-4(2)))";
        //SDS2XDX result = parseExpression(expression, cc);
        HashMap<String,SDS2XDX> map=new HashMap<String,SDS2XDX>();
        map.put("CC", cc);
        SDS2XDX result=SDS2XDX.eval(expression,map,35);
        
        //result=(new SDS2XDX("0","0")).add(new SDS2XDX("5.50E-4","12"));
        System.out.println("Result: " + result.s()+"  "+result.ds());
        */
    }
    
    public static SDS2XDX parseExpression(String expression, SDS2XDX cc) {
        // Replace "CC" with the actual value and uncertainty of the cc object
        expression = expression.replace("CC", cc.s() + "(" + cc.ds() + ")");

        
        // Use a stack to evaluate the expression
        Stack<SDS2XDX> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        char c0=expression.charAt(0);
        if(c0=='*'||c0=='/')
        	values.push(new SDS2XDX("1.0","0"));
        else if(c0=='+' || c0=='-') {
        	values.push(new SDS2XDX("0.0","0"));
        }
        
         // 1. [\\+\\-]?: Matches an optional + or - sign. The \\ is used to escape the + and - characters in the regular expression.
         // 2. ((0.[0-9]+|[1-9]+[0-9.][0-9]*)): Matches a numeric value, which can be:
         //    0.[0-9]+: A decimal number starting with 0. followed by one or more digits.
         //    [1-9]+[0-9.][0-9]*: A number starting with a non-zero digit, followed by zero or more digits or a decimal point, and optionally followed by zero or more digits.
         // 3. \\(: Matches the opening parenthesis (. The \\ is used to escape the parenthesis in the regular expression.
         // 4. ([1-9][0-9]*): Matches one or more digits (1-9 followed by zero or more digits 0-9). This captures the uncertainty part of the SDS2XDX object.
         // 5. \\): Matches the closing parenthesis ). The \\ is used to escape the parenthesis in the regular expression.
         // 6. |: Acts as an OR operator, allowing the pattern to match either the previous part or the following part.
         // 7. [+\\-*/()]: Matches any single character that is a +, -, *, /, (, or ).
        
        
        //Note: number of group depends on how many pairs of parentheses (non-escaped ones)
        //group() or group(0) always the whole match
        //String valPatternStr="("+"[\\+\\-]?"+"(0.[0-9]+|[1-9]+[0-9.]?[0-9]*)"+")";
        
        String valPatternStr="("+"(0.[0-9]+|[1-9]+[0-9.]?[0-9]*)([Ee][+\\-]?[1-9])?"+")";
        String uncPatternStr="("+"\\(([1-9][0-9]*)\\)"+"|"+"\\((L[TE]|G[TE]|AP|SY)\\)"+")";
        
        String operatorPatternStr="[+\\-*/()]";
        Pattern pattern = Pattern.compile( (valPatternStr+uncPatternStr)+"|"+valPatternStr+"|"+operatorPatternStr);
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String token = matcher.group();

            if (token.matches(valPatternStr+uncPatternStr)) {
            	System.out.println("1 token="+token+"  "+matcher.groupCount());
            	for(int i=0;i<matcher.groupCount();i++)
            		System.out.println("i="+i+"  "+matcher.group(i));
            	
            	//value+uncertainty
                String value = matcher.group(1);
                String uncertainty = matcher.group(5);
                
                values.push(new SDS2XDX(value, uncertainty));
            } else if (token.matches(valPatternStr)) {
            	//value without uncertainty
            	
            	System.out.println("2 token="+token+"  "+matcher.groupCount());
            	for(int i=0;i<matcher.groupCount();i++)
            		System.out.println("i="+i+"  "+matcher.group(i));
            	
                String value = matcher.group(0);
                String uncertainty = "0";
                
                values.push(new SDS2XDX(value, uncertainty));
            } else if (token.equals("(")) {
            	System.out.println("3 token="+token+"  "+matcher.groupCount());
            	for(int i=0;i<matcher.groupCount();i++)
            		System.out.println("i="+i+"  "+matcher.group(i));
            	
                operators.push('(');
            } else if (token.equals(")")) {
            	System.out.println("4 token="+token+"  "+matcher.groupCount());
            	for(int i=0;i<matcher.groupCount();i++)
            		System.out.println("i="+i+"  "+matcher.group(i));
            	
                while (operators.peek() != '(') {
                	calculate(values,operators);
                }
                operators.pop();
            } else if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
            	
            	System.out.println("5 token="+token+"  "+matcher.groupCount());
            	for(int i=0;i<matcher.groupCount();i++)
            		System.out.println("i="+i+"  "+matcher.group(i));
            	
                while (!operators.isEmpty() && hasPrecedence(operators.peek(),token.charAt(0))) {
                	System.out.println(operators.peek()+"  "+token.charAt(0));
                	calculate(values,operators);
                }
                operators.push(token.charAt(0));
            }
        }

        while (!operators.isEmpty()) {
        	calculate(values,operators);
        }

        return values.pop();
    }

    private static void calculate(Stack<SDS2XDX> values,Stack<Character> operators) {
    	try {
        	//for a+b or a/b, b is popped out first
        	
        	if(values.size()>=1) {
        		SDS2XDX b=values.pop();
        		SDS2XDX a=null;
        		
        		Character op=operators.pop();
        		
        		if(values.size()>=1) {
        			a=values.pop();
        		}else {
        			if(op=='*' || op=='/')
        				a=new SDS2XDX("1.0","0");
        			else if(op=='+' || op=='-')
        				a=new SDS2XDX("0","0");
        			else 
        				return;
        		}
        		
            	SDS2XDX result=applyOperator(a,b,op);
            	
            	//System.out.println(" #### b="+b+" a="+a+" op="+op+"  Result: " + result);
            	
                values.push(result);
        	}else {
        		operators.pop();
        	}
    	}catch(Exception e) {
    		
    	}
    }
    /*
     * return true if op1 has higher priority than op2, otherwise return false
     */
    private static boolean hasPrecedence(char op1, char op2) {
    	//"(" and ")" should not be compared with other operators, so return false
        if (op1 == '(' || op1 == ')') {
            return false;
        }
        if ((op2 == '*' || op2 == '/') && (op1 == '+' || op1 == '-')) {
            return false;
        }
        return true;
    }

    private static SDS2XDX applyOperator(SDS2XDX a, SDS2XDX b,char operator) {
        switch (operator) {
            case '+':
                return a.add(b);
            case '-':
                return a.subtract(b);
            case '*':
                return a.multiply(b);
            case '/':
                return a.divided(b);
        }
        return null;
    }

}
//SDS2XDX class for reference
class SDS2XDX1 {
 private String value;
 private String uncertainty;

 public SDS2XDX1(String value, String uncertainty) {
     this.value = value;
     this.uncertainty = uncertainty;
 }

 public String s() {
     return value;
 }

 public String ds() {
     return uncertainty;
 }

 public SDS2XDX1 add(SDS2XDX1 other) {
     // Implement addition logic here
     // For simplicity, this is a placeholder
     return new SDS2XDX1("("+this.value + "+" + other.value+")", this.uncertainty + "+" + other.uncertainty);
 }

 public SDS2XDX1 subtract(SDS2XDX1 other) {
     // Implement subtraction logic here
     // For simplicity, this is a placeholder
     return new SDS2XDX1("("+this.value + "-" + other.value+")", this.uncertainty + "-" + other.uncertainty);
 }

 public SDS2XDX1 multiply(SDS2XDX1 other) {
     // Implement multiplication logic here
     // For simplicity, this is a placeholder
     return new SDS2XDX1("("+this.value + "*" + other.value+")", this.uncertainty + "*" + other.uncertainty);
 }

 public SDS2XDX1 divided(SDS2XDX1 other) {
     // Implement division logic here
     // For simplicity, this is a placeholder
     return new SDS2XDX1("("+this.value + "/" + other.value+")", this.uncertainty + "/" + other.uncertainty);
 }

 @Override
 public String toString() {
     return "SDS2XDX{" +
             "value='" + value + '\'' +
             ", uncertainty='" + uncertainty + '\'' +
             '}';
 }
}
