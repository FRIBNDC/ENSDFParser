package ensdfparser.nds.ensdf;

public class EnsdfLine {
	private String NUCID="";//col#0-4 (five characters including blank)
	private String lineType="";//col#5-8 (four characters including blank)
	private String body="";//col#9 to line end (including heading spaces)
	private String line="";
	
	private char[] typeChars=null;
	//private boolean isENSDFLine=false;
	
	private char comIndexChar=0;//col#5, " ", "1", "a", "x"
	private char comTypeChar=0;//col#6, " ", "c" (comment), "d" (document), "t" (table record)
	private char recTypeChar=0;//col#7
	private char extraRecTypeChar=0;//col#8, like "2cDP", " c P"
	
	public EnsdfLine(String line) {
		this.line=line;
		parseENSDFLine(line);
	}
	
	private void parseENSDFLine(String line) {
		int n=line.length();
		if(line.trim().isEmpty() || n<5)
			return;

		NUCID=line.substring(0,5);
		if(n<9) {
			String tempLine=line+"            ";
			lineType=tempLine.substring(5,9);
		}else {
			lineType=line.substring(5,9);
			body=line.substring(9);
		}
		
		comIndexChar=lineType.charAt(0);
		comTypeChar=lineType.charAt(1);
		recTypeChar=lineType.charAt(2);
		extraRecTypeChar=lineType.charAt(3);
		
		typeChars=new char[] {comIndexChar,comTypeChar,recTypeChar,extraRecTypeChar};
		
	}
	
	public String NUCID() {return NUCID;}//including heading space
	public String lineType() {return lineType;}
	public String body() {return body;}
	public String line() {return line;}
	
	public char[] typeChars() {return typeChars;}
	
	public char comIndexChar() {return comIndexChar;}//col#5, " ", "1", "a", "x"
	public char comTypeChar() {return comTypeChar;}//col#6, " ", "c" (comment), "d" (document), "t" (table record)
	public char recTypeChar() {return recTypeChar;}//col#7
	public char extraRecTypeChar() {return extraRecTypeChar;}//col#8, like "2cDP", " c P"
	
	//public boolean isENSDFLine() {return isENSDFLine;} 
	
	public static EnsdfLine parse(String line) {
		return new EnsdfLine(line);
	}
}
