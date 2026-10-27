package ensdfparser.test;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ensdfparser.calc.BXLBase;
import ensdfparser.calc.BriccsWrap;
import ensdfparser.calc.MCErrorPropagation;
import ensdfparser.calc.ResultOfUncertainties;
import ensdfparser.calc.XDX;
import ensdfparser.check.RUL;
import ensdfparser.check.SpinParityParser;
import ensdfparser.calc.MCErrorPropagation.Param;
import ensdfparser.ensdf.Comment;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.FindValue;
import ensdfparser.ensdf.JPI;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.ensdf.T12;
import ensdfparser.ensdf.XDX2SDS;
import ensdfparser.ensdf.XRef;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;


@SuppressWarnings("unused")
public class Test {
	public static void main(String[] args) throws Exception {
		//test6();
		//test7();
		//test8();
		//test9();
		
		SDS2XDX sx=null;
		//sx=SDS2XDX.eval("12.3(2)+pow(1.34(2),2)",35);
		
		//sx=SDS2XDX.eval("12.3(2)+exp(1.23(12))",35);
		//SDS2XDX.eval("12.3(2)+log(1.34(2))",35);
		
		//sx=new SDS2XDX("6140","10");
		//sx=sx.divided("3.4E-3","34");
		
		//sx=new SDS2XDX("266522","GT");
		//System.out.println(sx.canGetDX()+" isLimit="+sx.isLimits());
		
		//sx=sx.log10();
		
		//sx=EnsdfUtil.evalExpression("a/b","a=12(2),b=10(3)", 99,"");
		//sx=EnsdfUtil.findWidthOfResonance("239(25)","meV","0.55(3)",2.5,"MaxMin");
		//sx=EnsdfUtil.findT12OfResonance("239(25)","meV","0.55(3)",2.5,"MaxMin");
		//sx=SDS2XDX.parse("0.239(25)");
		//System.out.println(sx.s()+"  "+sx.ds()+"####");
		
		sx=EnsdfUtil.findT12OfResonance("0.60(15)","meV","21(2)",2.5,"MaxMin");
		System.out.println(sx.s()+"  "+sx.ds()+"####");
		sx=EnsdfUtil.findWidthOfResonance("13(2)","meV","0.41(14)",2.5,"MaxMin");
		System.out.println(sx.s()+"  "+sx.ds()+"####");
	}
	
	public static void test9() {
		double C1=56.59;
		
		SDS2XDX.setIsAllowMaxMinEPByDefault(true);
		
		SDS2XDX one=new SDS2XDX("1.00","0");
		
		SDS2XDX CC=new SDS2XDX("0","0");
		SDS2XDX EG=new SDS2XDX("14","2");
		SDS2XDX BR=new SDS2XDX("1.00","0");//for g+ce
		
		SDS2XDX BXL=new SDS2XDX();
		
		SDS2XDX T=new SDS2XDX("2.0E-12","2");//in units of second
		
		//to get EG^5
		
		//SDS2XDX EG5=null;
		
		//SDS2XDX EG5=EG.pow(5);		
		//SDS2XDX EG5=EG.log10();
		
		//SDS2XDX EG5=EG.multiply(EG).multiply(EG).multiply(EG).multiply(EG);
		
		//System.out.println(EG5.s()+"  "+EG5.ds());
		//BXL=one.multiply(C1).multiply(BR).divided(one.add(CC)).divided(T).divided(EG5);
				
		//System.out.println("#### 1 "+BXL.x()+"  "+ BXL.s()+"   #"+BXL.ds());	
		
		//BXL=BXL.divided(T);
		
		//System.out.println("#### 2 "+BXL.x()+"  "+ BXL.s()+"   #"+BXL.ds());
		
		//BXL=new SDS2XDX("2.83E13","+32-26");
		//EG5=new SDS2XDX("2.430E12","81");
		//BXL=BXL.divided(EG5);
		BXL=SDS2XDX.exp(EG);
		System.out.println("#### 3 "+BXL.x()+"  "+ BXL.s()+"   #"+BXL.ds()+" "+BXL.dsu()+" "+BXL.dsl());
	
	}
	
	public static void test8() {
		//SDS2XDX sx1=new SDS2XDX("0.3565","");
		//sx1=sx1.multiply("100","0");
		//sx1.setDS("0");
		//sx1.setDX(0);
	
		//sx1=sx1.multiply(100);
		//System.out.println("\n###1 x="+sx1.x()+" dx="+sx1.dx()+" $$$$s="+sx1.s()+" ds="+sx1.ds());
		
		System.out.println(Str.shiftDecimalPoint("1.0",2,"RIGHT"));
		//String tempS=Str.roundByNsig("35.65", 6);
		//System.out.println(tempS);
		

		//System.out.println(Str.findBestStringOfNumber(-4743.2,5));
	}
	public static void test7() {
		//System.out.println(Str.findNSigOfExactProduct("0.10","0.01"));
		//System.out.println(Str.findExactDivision("2","0.71"));
		//System.out.println(Str.findBestNSigOfDivision("0.10","0.01"));
		//System.out.println(Str.findBestStringOfDivision("0.10","0.01"));
		//System.out.println(Str.findBestNSigOfDivision("2","0.71"));
		//System.out.println(Str.findBestStringOfDivision("2","0.71"));
		//System.out.println(Str.findBestNSigOfProduct("2","0.71"));
		//System.out.println(Str.findBestStringOfProduct("2","0.71"));
		//System.out.println(Str.findBestNSigOfProduct("0.3565","100"));
		//System.out.println(Str.findBestStringOfProduct("0.3565","100"));
		//System.out.println(Str.findBestStringOfProduct("0.4733","-1.1"));
		
		//System.out.println(Str.findBestNSigOfAddition("2","0.0071"));
		//System.out.println(Str.findBestStringOfAddition("2","0.0071"));
		//System.out.println("###"+Str.findBestNSigOfSubtraction("5315","4743"));
		//System.out.println("###"+Str.findBestStringOfAddition("5315","4743.1"));
		//System.out.println("###"+Str.findBestStringOfSubtraction("7800","5315"));
	}
	public static void test6() throws Exception {
		SDS2XDX sx1=new SDS2XDX("5315","");//("7800","");
		SDS2XDX sx2=new SDS2XDX("4743","");//("5315","");
		double ratio=0.9999909697617103;
		
		for(int i=1;i<=2;i++) {
			if(i==1) {
				sx1=new SDS2XDX("5315","");//("7800","");
				sx2=new SDS2XDX("4743","");//("5315","");
				ratio=0.9999909697617103;
			}else if(i==2) {
				sx1=new SDS2XDX("7800","");
				sx2=new SDS2XDX("5315","");
				ratio=0.999960771351727;
	
			}			

			sx1=sx1.subtract(sx2);
			
			System.out.println("\n###1 x="+sx1.x()+" dx="+sx1.dx()+" s="+sx1.s()+" ds="+sx1.ds());

			//int nd=0;
			//sx1=sx1.multiply(ratio);
			//System.out.println("###2  s="+sx1.s()+" ds="+sx1.ds()+"  nd="+nd+" errorLimit="+sx1.getErrorLimit()+" ratio="+ratio);
				
			//sx1=SDS2XDX.checkUncertainty(sx1.s(), sx1.ds(), nd,sx1.getErrorLimit());
			
			//if(Math.abs(sx1.x()-2352)<1)
			//System.out.println("###3  x="+sx1.x()+" dx="+sx1.dx()+" s="+sx1.s()+" ds="+sx1.ds());
		}

		
	}
	public static void test5() throws Exception {
		String s=" 80GE cG E$other: 2948.4 {I6} from     \n"
				+ " 80GE2cG {+80}Ga |b{+-} decay (1.3 s)                                           ";
		//String s1=" 80SE cL E$2819 from 1986Og01 and 2828 from from 1974MuZB could be a composite\n"
		//		+ " 80SE2cL peak including 2814.5,(2+) level and 2827.0,(2+) level in Adopted";
				//+ " 80SE3cL levels";
		String s2="E(level): 2819 from 1986Og01 and 2828 from from 1974MuZB could be a composite peak including 2814.5,(2+) level and 2827.0,(2+) level in Adopted levels.";
		
		String s1=" 78ZN C  2000DA07 (ALSO 1999LE68,1999DaZQ thesis): 78ZN ISOMER IDENTIFIED IN    \n"
				+ " 78ZN2C  NI(86KR,X), E=60.5 MEV/A; MEASURED EG, IG, IG(T) AND ISOMER HALF-LIFE. \n"
				+ " 78ZN3C  THE ASSIGNMENT AND ORDERING OF TRANSITIONS WAS BASED ON SYSTEMATICS.  ";
		Translator.init();
		
		//s=EnsdfUtil.wrapENSDFLines(s);
		//System.out.println("#"+s+"#");
		
		String[] temp=s1.split("[\\n]+");
		Vector<String> linesV=new Vector<String>();
		for(int i=0;i<temp.length;i++) {
			System.out.println(temp[i]);
			linesV.add(temp[i]);
		}
		Comment c=new Comment();
		c.setValues(linesV);
		System.out.println(c.cbody());
	}
	
	public static void test4() {
		
		//String s="B(E2)(FROM 2+)=0.0302 {I20} (1974BA80)";     
		String s="$M1:M2:M3:M4:M5:N1:N2:N3:(N4+N5):O1:(O2+O3)= 100:36.8 {I14}:38.0 {I14}:0.9 {I2}:1.0 {I2}:22.1 {I6}:6.4 {I3}:7.6 "
				+ "{I2}:0.20 {I2}: 3.7 {I1}:2.08 {I6} (1968Ca06); M1:M2:M3:(M4+M5)=100:31 {I3}:34 {I3}: |?1.2, N1:N2:N3=100:25 "
				+ "{I3}:41 {I5}, M/N=4.2 {I5}, N/O=6.4 {I10} (1971MaXG). Other: (|a(M)exp + |a(N)exp)=106 {I6} "
				+ "(from |bce coin (1959Ch31)); value is inconsistent with adopted multipolarity.";

		Vector<FindValue> fvs=EnsdfUtil.findDataEntriesInLine(s);
        
        System.out.println(" #### fvs size="+fvs.size());
		for(FindValue fv:fvs) {
			String name=fv.name();
			String tempRef=fv.ref().toUpperCase().trim();

			System.out.println(" name="+name+" fv txt="+fv.txt()+" s="+fv.s()+" ds="+fv.ds()+" ref="+fv.ref());
		}
		
		/*
		FindValue fv=new FindValue();
		fv.findValueInEntry("CIT=NDS 107, 1923 (2006)");
		String name=fv.name();

		System.out.println(" fv.name="+fv.name()+" NAME="+name+"  "+fv.txt() );
		*/
		
		/*
		SDS2XDX sx1=new SDS2XDX("0.97116166","");
		SDS2XDX sx2=new SDS2XDX("115.2","2");
		
		sx2=sx2.multiply(sx1);
		System.out.println("sx2="+sx2.toString()+" s="+sx2.s()+" ds="+sx2.ds());
		
		SDS2XDX sx3=new SDS2XDX("1270.232","");
		SDS2XDX sx4=new SDS2XDX("6985.84","4");
		sx4=sx3.add(sx4);
		System.out.println("sx4="+sx4.toString()+" s="+sx4.s()+" ds="+sx4.ds());
		
		
		
		//SDS2XDX sx=SDS2XDX.eval("1.58(2)*0.98",35);
		//SDS2XDX sx=SDS2XDX.eval("115.2(3)*0.97116166+6584.84(4)",35);
		//System.out.println(" eval sx="+sx.toString()+" s="+sx.s()+" ds="+sx.ds()+"  x="+sx.x()+"  dx="+sx.dx());
		
		//T12 t1=T12.parseTS("155 ns");
		//T12 t2=T12.parseTS(">2 us");
		
		//System.out.println("   t1="+t1.valueInSeconds());
		//System.out.println("   t2="+t2.valueInSeconds());
		//sx=SDS2XDX.eval("115.2(2)*0.97116166+6584.84(4)",35);
		
		String jpis="1/2:7/2(+)";
		//String jpis="1/2:5/2,7/2-";
		//String jpis="1/2:7/2+";
		//System.out.println(EnsdfUtil.pi(jpis));
		String[] out=EnsdfUtil.parseJPI(jpis);
		System.out.println(out.length);
		for(int i=0;i<out.length;i++)System.out.println(out[i]);
		*/
		//System.out.println(" eval sx="+sx.toString()+" s="+sx.s()+" ds="+sx.ds());
		
		/*
		String regexIntegerSingle="\\([1-9][0-9]?\\)[+-]?|\\([1-9][0-9]?[+-]\\)|[1-9][0-9]?\\([+-]\\)|[1-9][0-9]?[+-]?";
		String regexHalfSingle="\\([1-9]?[13579]/2\\)[+-]?|\\([1-9]?[13579]/2[+-]\\)|[1-9]?[13579]/2\\([+-]\\)|[1-9]?[13579]/2[+-]?";
		
		String regexHalf="("+regexHalfSingle+")"+"("+"(,("+regexHalfSingle+"))*$"+"|"+"(,("+regexHalfSingle+"))*[\\s.,;\\)]"+")";
		String regexInteger="("+regexIntegerSingle+")"+"("+"(,("+regexIntegerSingle+"))*$"+"|"+"(,("+regexIntegerSingle+"))*[\\s.,;\\)]"+")";
		
		
		//String regexHalf="(,("+regexHalfSingle+"))";
		
		String textHalf="1/2+";
		//String text="(11/2-),(13/2)-,9/2(+)";	
		String textInteger="test ( 1,3,5 )+";
		
		Pattern pattern = Pattern.compile(regexHalf, Pattern.CASE_INSENSITIVE);
		Matcher matcher=pattern.matcher(textHalf);
		
		//Pattern pattern = Pattern.compile(regexInteger, Pattern.CASE_INSENSITIVE);
		//Matcher matcher=pattern.matcher(textInteger);
		
		int prevPos=-1;
		while (matcher.find()) {
			int pos=-1;
			try {
				pos=matcher.start();
			}catch(Exception e) {
				
			}
			System.out.println("Found match: " + matcher.group() + " at position " + pos+" group count="+matcher.groupCount()+" "+matcher.matches());

			if(pos==prevPos)
				break;
			
			prevPos=pos;
		}
		*/
		
		//String text="(   11/2-,13/2-,9/2)";
		//String text="E,2";
		//System.out.println(JPI.extractJPS(text));

		//System.out.println("hello");
		//System.out.println(EnsdfUtil.pi("1/2(-):5/2(-)"));
		
		//String[] A=EnsdfUtil.splitEntryLine("J=5/2(-),7/2(-),9/2(-),SPIN=1,2");
		//for(int i=0;i<A.length;i++)
		//	System.out.println(A[i]);
		
		/*
		FindValue fv=new FindValue();

		//String s="19NE2 L WIDTHG=101E-3 EV +91-55 (2016AK05) ";
		String s="151GS2 L J=5/2(-),7/2(-),9/2(-), spin=2";
		fv.findValueInLine(s, "J");
		System.out.println("###name="+fv.name()+" vs="+fv.v()+" s="+fv.s()+" ds="+fv.ds()
		+"\n unit="+fv.units()+" ref="+fv.ref()+" txt="+fv.txt()+" v="+fv.v()+" ##"+fv.vNoSymbolNoRef()+"  @@"+fv.vNoSymbol());
		*/
		
		//System.out.println(Str.removeUnmatchedBrackets("BROA. (GRID "));
		
		/*
		XDX sx=new XDX(6.214308161435154, -1,  -1);
		
		sx.dxu0=1.4895742959410072;
		sx.dxl0=1.4895742959410072;
		
		//XDX sx=new XDX("7.23","");

		sx=sx.multiply(new XDX("0.944","0.004"));
		System.out.println(sx.s()+"  "+sx.dsu()+" sx.dxu="+sx.dxu+" sx.dxu0="+sx.dxu0);
		*/
        

		
		/*
		SDS2XDX sx=new SDS2XDX("0.10","LT");
		//SDS2XDX sx=new SDS2XDX("15","2");
		sx=sx.multiply("5000","");
		//SDS2XDX sx=new SDS2XDX("200","");
		//SDS2XDX sx=new SDS2XDX("0.3","");
		
		//double f=1.0E-12;
		
		//sx=sx.add(0,2.5);
		//sx=sx.add("0.00","25");
		//sx=sx.add("0","1");
		System.out.println(sx.s()+"  "+sx.ds()+" dx="+sx.dx()+" dx0="+sx.dx0()+"  "+sx.dxu0());
		*/
		
		/*
		SDS2XDX constant=new SDS2XDX("1","");
    	SDS2XDX temp=constant.divided(100);
    	temp=sx.multiply(temp);//this gives the extra uncertainty to add
    	temp.setValues(0.0,temp.x());
        sx=sx.add(temp);
        */
        //sx=sx.multiply("1.00","2");
        
		//sx=sx.multiply(0.011);
		//sx=sx.multiply("0.944","4");
		//sx=sx.add("11","LT");
		//sx=sx.divided("0.1","");		
		//sx=sx.divided(new SDS2XDX("101.2","34"));
		//System.out.println(sx.s()+"  "+sx.ds()+" dx="+sx.dx()+" dx0="+sx.dx0()+"  "+sx.dxu0());
		
		
		/*
		Vector<String> out=new Vector<String>();
		for(int i=0;i<5;i++) {
			out.add("test"+i);
		}
		
		for(String s:out) {
			System.out.println("##1"+s);
			if(s.endsWith("3"))
				out.remove(s);
			System.out.println("##2"+s);
		}
		*/
		/*
		String s="181OS cL T$Unless otherwise stated, T{-1/2} are from |g|g(t)                    \n"
				+ "181OS2cL for half-lives in the nanosecond region and                            \n"
				+ "181OS2cL from Doppler-shift attenuation method (DSAM) in the ps region          \n"
				+ "181OS3cL (2003Cu03). No evidence was found for higher-lying isomers with        \n"
				+ "181OS4cL T{-1/2}>5 ns above the K|p=35/2- isomer    ";
		
		Vector<String> tempV=EnsdfUtil.parseT12MethodsV(s);
		System.out.println(tempV.size());
		for(String s1:tempV)
			System.out.println(s1);
		
		
		System.out.println("####"+EnsdfUtil.parseT12Method(s)+ "FROM B(sfasdf".replace("FROM B(",""));
		System.out.println(EnsdfUtil.isSimilarTMethod("delayed-coin", "time-distribution"));
		*/
		
		//String text="(R+1.23(4))/(1+CC*RI)/RI";
		//String text="1.23*(1+CC)";
		//System.out.println(Str.containWholeWord(text,"R"));
		//Vector<Entry<String,Integer>> out=Str.matchesAndIndexes(text, "\\bR\\b");
		
		//Vector<Entry<String,Integer>> out=Str.matchesAndIndexes(text, "\\b[A-Z]{2}\\b");
		//for(Entry<String,Integer> entry:out)
		//	System.out.println(entry.getKey()+"  index="+entry.getValue());
		
		//System.out.println(Str.replaceWholeWord(text,"RE","#"));
		//System.out.println(Str.hasMatch(text,"[+\\-*/]{2,}"));
		//System.out.println(Str.hasMatch(text,"[^0-9a-zA-Z.\\+\\-\\*/\\(\\)]"));
		//System.out.println(Str.hasUnmatchedBracketAny(text));
		//System.out.println(Str.match("114|g-231|g-coin", "\\|g").size());
		
 
		
		/*
		String regex="\\bR\\b";
		String text="(R+1.23(4))*(1+CC*RI)/MR";
		Pattern pattern=Pattern.compile(regex);
		Matcher matcher=pattern.matcher(text);
		while (matcher.find()) {
			System.out.println("Found match: " + matcher.group() + " at position " + matcher.start()+" group count="+matcher.groupCount()+matcher.matches());
		}
		*/
		
		
		//String expression="1140.0(5)";
		//String expression="902.00032222(12)";
		//String expression="902.0(12)*0.97116166+6985.84(4)";
		//String valPatternStr="[1-9]+[0-9]*[.]?[0-9]*";
		//String valPatternStr="("+"(0.[0-9]+|[1-9]+[0-9]?[0-9.]?[0-9]*)([Ee][+\\-]?[1-9])?"+")";
		//String uncPatternStr="("+"\\(([1-9][0-9]*)\\)"+"|"+"\\((L[TE]|G[TE]|AP|SY)\\)"+")";		    		    
		//String operatorPatternStr="[+\\-*/()]";		   
		//Pattern pattern = Pattern.compile( (valPatternStr+uncPatternStr)+"|"+valPatternStr+"|"+operatorPatternStr);	   
		/*
		Pattern pattern = Pattern.compile(valPatternStr+uncPatternStr);	  
		
		Matcher matcher = pattern.matcher(expression);		
		
	    while (matcher.find()) {
	        String token = matcher.group();
	        System.out.println("### Token="+token);
        	for(int i=0;i<matcher.groupCount();i++)
        		System.out.println("i="+i+"  "+matcher.group(i));
	    }
	    */
		/*
		double t1=1E-10;//in units of second

		float eg=3200;
		int A=150;
		
		t1=5E-14;
		//System.out.println(BXLBase.printMinEGForAllRULs(t1));
		//System.out.println(BXLBase.printMinEGForAllRULsOfA(t1,A));
		//System.out.println(BXLBase.findPossibleMULForEG(eg+"", A, t1));
		
		System.out.println();
		
		double t2=1E-9;
		System.out.println(BXLBase.printMinEGForAllRULs(t2));
		
		eg=1201;
		//System.out.println(BXLBase.printMinEGForAllRULsOfA(t2,A));
		System.out.println(BXLBase.findPossibleMULForEG(eg+"", -1, t2));

		Vector<JPI> tempV= SpinParityParser.findJPVFromGammaNoMULTo("(3/2-)",eg+"", -1, t2);
		for(JPI jp:tempV)
			System.out.println(jp.js());
		
		*/
		/*
		Translator.init();
		
		String text="IB$From |g+ce intensity balance at each level for excited levels. All  \n"
				+ " 65CO2cB |b feedings should be considered as upper limits and thus associated   \n"
				+ " 65CO3cB log| {Ift} values as lower limits, due to possible missing |g rays in  \n"
				+ " 65CO4cB this incomplete decay scheme. Original values from 2009Pa16 are given  \n"
				+ " 65CO5cB under comments.    ";
		
		String s=EnsdfUtil.wrapENSDFLines(text);
		System.out.println(s);
		*/
		/*
		//String line="65COHTYP=FUL$AUT=Jun Chen$CIT=ENSDF$CUT=30-SEP-2024$      ";
		//String line=" 65CO DB EAV,LOGFT$FROM BetaShape v2.4 (Jun-2024) 2023MO21.   ";
		//String line="65CO c  Adapted from the XUNDL dataset for 2009Pa16 compiled by by B. Karamy  ";
		String line="   J$(9/2-) in 2009Pa16. See comments in Adopted Levels  ";
		String[] out=EnsdfUtil.findENSDFLineInfo(line);

		

		System.out.println("    NUCID="+out[0]+"*");
		System.out.println("Line Type="+out[1]+"*");
		System.out.println("Line Body="+out[2]+"*");
		*/
		/*
		XRef xr=new XRef();
		xr.setValues("A", "A", "151YB EC+B+ DECAY (1.62456123 S):XUNDL-12");
		*/
		
		/*
		SDS2XDX sx1=new SDS2XDX("1.23","");
		SDS2XDX sx2=new SDS2XDX("0.834","2");
		*/
		
		/*
		XDX sx1=new XDX("1.23","");
		XDX sx2=new XDX("0.834","0.002");
		//sx1=sx1.add(sx2);
		sx1=sx1.multiply(sx2);
		
		
		System.out.println(" s="+sx1.s()+" ds="+sx1.dsu()+" x="+sx1.x()+" dx="+sx1.dxu());
		*/
		/*
		Translator.init();
		String text="From analogy to J|p=8-, 96-min";
		for(int i=0;i<text.length();i++) {
			System.out.println(" text="+text+"   "+Translator.isPI(text,i)+" index="+i+" ch="+text.charAt(i));
		}
		*/
		
		//String s="A=31 &Z=40,|| AND &N=50";
		//String[] temp=s.split("[,]+|\\|\\||OR");
		//String[] temp=s.split("[,]+|[\\|]+|OR");
		
		/*
		String s="A=94 AND Z=40";
		String[] temp=s.split("[&]+|AND");
		for(int i=0;i<temp.length;i++)
			System.out.println(i+" "+temp[i]+"#");
       */
		/*
		String s1="*** T1/2 result is for actual T/g with g=(2Jx+1)/(2J0+1), since g is unknown ***\n";
    	      s1+="    Width0 and Width values are for acutal width/g\n";
    	
    	String[] temp=s1.split("\n");
    	//System.out.println("####"+temp.length);
    	//for(int i=0;i<temp.length;i++) {
    	//	System.out.println("$"+temp[i]+"$"+"  length="+temp[i].length()+"  "+temp[i].trim().length()+"   "+(temp[i].equals("\t")));
    	//}
    	
    	String s=Str.addMarginHead(s1, "  NOTE:");
    	System.out.println(s+"###");
    	*/
		
		/*
		SDS2XDX s1=new SDS2XDX();
		s1.setErrorLimit(99);
		//s1.setValues(2.975463860939496E-13, 1.5869138914720728E-13,  7.678616022699995E-14);		
		//s1=s1.divided(0.0046,0.0096,0.0038);

		
		s1.setValues("3.1","13");
		
		s1.setIsUseNormEP(true);
		s1=s1.multiply("20.2","29");
		System.out.println(s1.s()+"  "+s1.dsu()+"  "+s1.dsl());
		*/
		//XDX2SDS xs=new XDX2SDS(6.73,0.1399,0.1100,6);
		//XDX2SDS xs=new XDX2SDS(6.78,0.1399,0.1100,6);
		//XDX2SDS xs=new XDX2SDS(5.33,0.04,0.03,6);
		//XDX2SDS xs=new XDX2SDS(6.66,0.14,6);
		//System.out.println("s="+xs.s()+" ds="+xs.ds());
		//XDX2SDS xs1=new XDX2SDS(4.881,0.034,0.032,6);
		//System.out.println("s="+xs1.s()+" ds="+xs1.ds());
		/*
		XDX2SDS xs=new XDX2SDS(96.7400013850689, 2.7459060435491964);
		SDS2XDX sx0=new SDS2XDX();
		sx0.setValues(xs);
		SDS2XDX sx=sx0.add(4.4,4.4,4.399999);
		System.out.println("1 x="+sx.x()+" dxu="+sx.dxu()+" dxl="+sx.dxl()+" s="+sx.s()+" dsu="+sx.dsu()+" dsl="+sx.dsl());
		
		XDX ai=new XDX(4.4,4.4,4.3999994754791265);
		SDS2XDX sx1=sx0.add(ai.x,ai.dxu,ai.dxl);
		System.out.println("2 x="+sx.x()+" dxu="+sx.dxu()+" dxl="+sx.dxl()+" s="+sx.s()+" dsu="+sx.dsu()+" dsl="+sx.dsl());
		
		*/
		
		
		//test3();
		//test2();
		//System.out.println(JPI.JPIminusL(new JPI("5/2+,7/2+"), 3).js());
		//System.out.println(JPI.JPIplusL(new JPI("5/2+,7/2+"), 2).js());
		//Vector<JPI> jpV=JPI.JPICoupling("5/2+,9/2+","2+");
		//for(JPI jp:jpV)
		//	System.out.println(jp.js());
		
		/*
		String s="(666,2+ TO 1960,2+)=-(0.10 {I+4-3}) (1995KA29)";//physics sign not arithmetic sign
		FindValue fv=new FindValue();
		fv.findValueInEntry(s);
	 
		System.out.println(" fv txt="+fv.txt()+" s="+fv.s()+"  ds="+fv.ds()+"  ref="+fv.ref()+" separateSign="+fv.separateSign());
		System.out.println(" v=$"+fv.v()+"$ vNoSymbol()=$"+fv.vNoSymbol()+"$  vNoSymbolNoref=$"+fv.vNoSymbolNoRef());
		*/
		
		/*
		String line="246AM cL E$X<30 ; calculated value for the energy difference of ";
		String[] out=EnsdfUtil.findOffsetInLine(line, "X");
		
		System.out.println(out[0]+" $"+out[1]+" $"+out[2]);
		*/
		
		//String url="https://www.nndc.bnl.gov/nsr/knum_act.jsp?ofrml=Exchange&keylst=1999MA21%0D%0A2002IS03%0D%0A2004PO06%0D%0A2006REZX%0D%0A&getkl=Search";
		
		//url=NSR.convertToNewURL2024(url);
		
		//System.out.println(url);
		
		/*
		String s=" 80GE cG E$other: 2948.4 {I6} from     \n"
				+ " 80GE2cG {+80}Ga |b{+-} decay (1.3 s)                                           ";
		String s1=" 80SE cL E$2819 from 1986Og01 and 2828 from from 1974MuZB could be a composite\n"
				+ " 80SE2cL peak including 2814.5,(2+) level and 2827.0,(2+) level in Adopted";
				//+ " 80SE3cL levels";
		String s2="E(level): 2819 from 1986Og01 and 2828 from from 1974MuZB could be a composite peak including 2814.5,(2+) level and 2827.0,(2+) level in Adopted levels.";
		Translator.init();
		
		//s=EnsdfUtil.wrapENSDFLines(s);
		//System.out.println("#"+s+"#");
		
		String[] temp=s1.split("[\\n]+");
		Vector<String> linesV=new Vector<String>();
		for(int i=0;i<temp.length;i++) {
			System.out.println(temp[i]);
			linesV.add(temp[i]);
		}
		Comment c=new Comment();
		c.setValues(linesV);
		System.out.println(c.body()+" \n "+c.getTranslated());
        */
		
		//String tempBody="composite peak including 2814.5,(2+) level and 2827.0,(2+) level in Adopted";
		//System.out.println(Translator.translate(s2, 68,68+tempBody.length()-1));
	
		/*
    	Vector<String> lines=new Vector<String>();
        lines.add(" 80GE cG E,RI$From {+80}Ga |b{+-} decay; and ({+82}Se,X|g) above that, unless otherwise stated. ");                
        
        Translator.init();
        Comment c=new Comment();
        c.setValues(lines);
        
        HashMap<String,HashMap<String,Vector<String>>> map0=EnsdfUtil.findAllRecordSourcesMapFromComment(c);
        for(String key0:map0.keySet()) {
        	HashMap<String,Vector<String>> map1=map0.get(key0);
        	System.out.println(" key0="+key0);
        	for(String key1:map1.keySet())
        		System.out.println("        key1="+key1+"$ "+map1.get(key1));
        }
        */
		
		
	    //double x=0.0,dx=0.0;
	    //double x1=7.351850691121727E7,dx1=37.160095;
	    //double x1=2828.0,dx1=-1.0;
	    //SDS2XDX sx=new SDS2XDX("0","0");
	    //sx=sx.add(x1,dx1);
	    //SDS2XDX sx2=new SDS2XDX("1.4E3","14");
		//sx=sx.subtract(sx2);
		//System.out.println("  "+sx.x()+" dx="+sx.dx()+" "+sx2.x());
		
	    //System.out.println("result.x= "+sx.x()+" "+sx.DX()+" dxu="+sx.dxu()+" dxl="+sx.dxl()+" result.s="+sx.s()+" "+sx.ds()+" x1="+(float)x1+" dx1="+(float)dx1);
	    
		
		//double x=3.528061463605497, dxu=3.3166841202092456E-4,dxl=3.3371864030629084E-4;
		//double x=0.983680, dxu=3.3166841202092456E-4,dxl=3.3371864030629084E-4;
		
		//XDX2SDS xs=new XDX2SDS(x,dxu,dxl,0);
   
		//System.out.println(" x="+x+" "+dxu+" "+dxl+" s="+xs.s()+" dsl="+xs.dsl()+" dsu="+xs.dsu());
				
		//String entry="from B(E2) (from 2+,560)=0.0290 {I36} (2009VA01,2003VA02),0.0286 {I+58-51} (2023IL02)";
		//String entry="E(p)(lab)=3787, IAS(1292 {+63}Ni) from 1976Ar01 in (p,p) and 1972Sz01";
		//String entry="|G=4.6|*10{+-3} EV {I2}";
		//String entry="g|G(0){+2}/|G=6.5|*10{+-3} eV {I7}";
		
		/*
		String entry="G|G(0){+2}/|G<0.24E-3 EV (1976SW01)";
		FindValue fv0=new FindValue();
		fv0.findValueInEntry(entry);
		System.out.println("@@@name="+fv0.name()+"  v="+fv0.v()+"---"+fv0.vNoSymbol()+"---"+fv0.vNoSymbolNoRef()+"---"+fv0.canGetV()+"  ref="+fv0.ref());
		System.out.println("        s="+fv0.s()+" ds="+fv0.ds()+" unit="+fv0.units());
		*/
		
		//String line="from B(E2) (from 2+,560)=0.0290 {I36} (2009VA01,2003VA02),0.0286 {I+58-51} (2023IL02)";
		/*
		String line="148SM cL T         from B(E2)|^=0.03 {I1} and 1664|g branching=0.34 {I1}.  ";
		Vector<FindValue> tempV=EnsdfUtil.findDataEntriesInLine(line);
		
		System.out.println(tempV.size());
		

		for(FindValue fv:tempV) {
			//fv.findValueInEntry(entry, "B(E2)_");
			System.out.println("name="+fv.name()+"  v="+fv.v()+"$---"+fv.vNoSymbol()+"---"+fv.vNoSymbolNoRef()+"---"+fv.canGetV()+"  ref="+fv.ref());
		}
        */
	  /*	
	  SDS2XDX sx1=new SDS2XDX("2576.6","");     
  	  SDS2XDX sx2=new SDS2XDX("0.984237","0");


      //System.out.println("@@ s2: x="+sx2.x()+" "+ sx2.getErrorLimit());   
      sx1=sx1.multiply(sx2);  
      System.out.println("## s="+sx1.s()+"  x="+sx1.x()+" limit="+ sx1.getErrorLimit()+" ds="+sx1.ds()+" dx="+sx1.dx());
    
  	  sx2=new SDS2XDX("6122.40","6"); 
      sx1=sx1.add(sx2);  
      System.out.println("@@ s="+sx1.s()+"  x="+sx1.x()+" limit="+ sx1.getErrorLimit()+" ds="+sx1.ds()+" dx="+sx1.dx());
      */
		
		//XDX temp=new XDX(-0.2,5.0,5.0);
		//System.out.println(" temp="+temp.x+" "+temp.dxu+" "+temp.dxl);
		//temp=temp.multiply(0.14,0.02);
		//System.out.println(" temp="+temp.x+" "+temp.dxu+" "+temp.dxl);
		
		//XDX t12=new XDX(6.721768799999999E12,5.04921600000001E11, 8.20497599999999E11);
		//XDX decayBR=new XDX(0.003,  2.0E-4, 2.0E-4);
		//XDX pT12=t12.divided(decayBR);
		//System.out.println("## pT12="+pT12.x+" "+pT12.dxu+" "+pT12.dxl);
		//System.out.println(" t12="+t12.x+"  "+t12.dxu+" "+t12.dxl+"  decayBR="+decayBR.x+"  "+decayBR.dxu+" "+decayBR.dxl);
		
		//XDX2SDS xs=new XDX2SDS(3.700000047683716E-13,0.37,0.0);
		//XDX2SDS xs=new XDX2SDS(1.85E-13,1.8499E-13,1.850000001E-13);
		//XDX2SDS xs1=new XDX2SDS(3.7E-13,5.0487097934144756E-29,-5.0487097934144756E-29);
		//XDX2SDS xs1=new XDX2SDS(5.6927872669169,0.00000023,5.692787266910581);
		//System.out.println(" 1 "+xs1.isLimits()+" s="+xs1.s()+" dsu="+xs1.dsu()+" dsl="+xs1.dsl()+" dxl="+xs1.dxl()+" ds="+xs1.ds()+" printEntry: "+xs1.printEntry());
		
		//XDX2SDS xs2=new XDX2SDS(3.7E-13,0,0);
		//System.out.println(" 2 "+xs2.isLimits()+" s="+xs2.s()+" dsu="+xs2.dsu()+" dsl="+xs2.dsl()+" dxl="+xs2.dxl()+" ds="+xs2.ds()+" printEntry: "+xs2.printEntry());
		
		/*
        double t=3.7E-13;
        double dtl=0.0;
        double probG=100.0;
        double dprobGU=0.0;
        double br=100.0;
        double dbru_ex=0.0;
        
        double pt=t*(100/br)*100/probG;
        double dptl_ex=pt-(t-dtl)*100/(br+dbru_ex)*100/(probG+dprobGU);
        System.out.println(" pt="+pt+" dptl_ex="+dptl_ex);
        */
		
		
		/*
		Translator.init();
		String text="52CR(18O,AP3NAG)";
		boolean cap=false;
        String text2 = Translator.procGenCom(text,0,cap);
        System.out.println("In Translator: process 2: text2="+text2);     
        */
		
        //String text3 = Translator.translate(text2);
        //System.out.println("In Translator: process 3: text3="+text3);
        
		//entry="B(E2)=12 {I3} (2022Ab11); 14 {I2} (2023Ab12). B(M1)=0.0286 {I+58-51} (2023IL02)";
		//String[] temp=EnsdfUtil.splitEntryLine(entry);
		//for(int i=0;i<temp.length;i++)
		//	System.out.println(" i="+i+"--"+temp[i]);
		
		//System.out.println(Str.romanToInt("12"));
    	//test0();
		//test2();
	    //System.out.println(" "+EnsdfUtil.isGoodValueStr("1234.5")+"  "+EnsdfUtil.isGoodUncertaintyStr("SY"));
		
		//SDS2XDX sx1=new SDS2XDX("","");
		//sx1.setValues(1.42422988,1.4675621, 0.085910962,195);
		//SDS2XDX sx2=new SDS2XDX("10.2","2");
		
		//System.out.println("SDS2XDX sx1: "+sx1.s()+" ds="+sx1.ds()+" dx="+sx1.dxu()+" "+sx1.dxl());
		//System.out.println("SDS2XDX sx2: "+sx2.s()+" ds="+sx2.ds()+" dx="+sx2.dxu()+" "+sx2.dxl());
		//SDS2XDX sx2=sx1.add("100","");
		//sx1=sx1.add(sx2);
		//System.out.println("SDS2XDX add: "+sx1.s()+" ds="+sx1.ds()+" dx="+sx1.dxu()+" "+sx1.dxl());
		
		//SDS2XDX sx2=sx1.multiply("100","");
		//System.out.println("SDS2XDX multiply: "+sx2.s()+" ds="+sx2.ds()+" dx="+sx2.dx()+" ");
		
		/*
		sx2=sx1.divided("1.2","1");
		System.out.println("SDS2XDX divided: "+sx2.s()+" ds="+sx2.ds()+" dx="+sx2.dx()+" ");
		
		sx2=sx1.divided0_new(new SDS2XDX("1.2","1"));
		System.out.println("SDS2XDX divided0_new: "+sx2.s()+" ds="+sx2.ds()+" dx="+sx2.dx()+" ");
		
		float dx=0.1f;
		XDX xdx1=new XDX("1.2","0.1");
		XDX xdx2=xdx1.add(1.2,dx);
		System.out.println("\nXDX add: "+xdx2.s+" ds="+xdx2.dsl+"  dx="+xdx2.dxl);
		
		xdx2=xdx1.multiply(1.2,dx);
		System.out.println("XDX multiply: "+xdx2.s+" ds="+xdx2.dsl+"  dx="+xdx2.dxl);
		
		xdx2=xdx1.divided(1.2,dx);
		System.out.println("XDX divided: "+xdx2.s+" ds="+xdx2.dsl+"  dx="+xdx2.dxl);
		*/
		
		/*
		XDX bk=new XDX(57.4855,5.000000000023874E-4);
		XDX AIT=new XDX(0.083, 0.005);
		XDX CK=new XDX(0.052,-1.0);
        XDX tempE=bk.multiply(AIT,true).multiply(CK.x, CK.dxu);
        
        XDX totalDecayE=new XDX(5.243944458007813,  0.34958122306684025);
        totalDecayE=totalDecayE.subtract(tempE);
        
        System.out.println("   BK "+bk.x+" "+bk.dxl+" CK="+CK.x+" "+CK.dxu+" AI="+ AIT.x+" "+AIT.dxl);
        //System.out.println("2 total E="+totalDecayE.x+" EC E="+totalECE.x);
        
    	XDX xdx1=totalDecayE;
    	System.out.println("DecayDataset 1217: totalE x="+xdx1.x+"  "+xdx1.dxl+" ds="+xdx1.dsl+" tempE="+tempE.x+" "+tempE.dxl);
    	*/
		
	}
	
	
	//checked NSR results file
	static void test3() throws Exception {
		
        String filePath="",fileDir="";
        String os=System.getProperty("os.name").toLowerCase();
        if(os.contains("mac"))
            fileDir="/Users/chenj/work/evaluation/ENSDF/check/";
        else
            fileDir="H:\\work\\evaluation\\ENSDF\\check\\";
        
        filePath=fileDir+"check.ens";
        
        int count=0;
        Vector<String> keynosV=new Vector<String>();
        File f=new File(filePath);
        Vector<String> linesV=Str.readFile(f);
        for(String line:linesV) {
        	String s=line.trim();
        	if(s.length()<8)
        		continue;
        	
        	s=s.substring(0,8).trim();
        	if(!EnsdfUtil.isKeynumber(s))
        		continue;
        	
        	keynosV.add(s);
        	count++;
        	
        }
        
        System.out.println("number of keynumbers="+count);
        
	}
	static void test2() throws Exception {
		Translator.init();
		
        String filePath="",fileDir="";
        String os=System.getProperty("os.name").toLowerCase();
        if(os.contains("mac"))
            fileDir="/Users/chenj/work/evaluation/ENSDF/check/";
        else
            fileDir="H:\\work\\evaluation\\ENSDF\\check\\";
        
        filePath=fileDir+"check.ens";
        
        File f=new File(filePath);
        
        ENSDF ens=new ENSDF();
        ens.setValues(Str.readFile(f));
        
        System.out.println(ens.errorMsg());
        
        for(int i=0;i<ens.nLevels();i++) {
        	Level l=ens.levelAt(i);
        	//if(!Str.isNumeric(l.ES()))
        	//	System.out.println(String.format("i=%3d  %12s  EF=%8.2f  ERP=%8.2f  ERPF=%8.2f   JS=%-12s   NES=%10s   NNES=%-6s",
        	//			i,l.ES(),l.EF(),l.ERF(),l.ERPF(),l.JPiS(),l.NES(),l.NNES()) );
        	
        	if(l.ES().equals("2828")) {
        		for(Comment c:l.commentV())
        			System.out.println(c.body()+" ##"+c.getTranslated());
        
        	}
        }
        
	}
	
    static void test0() throws Exception {
    	
    	Translator.init();
    	
    	try {
    		
    		SDS2XDX s2x=SDS2XDX.checkUncertainty("-0.549", "30", 2,35);
    		System.out.println("1 s2x.x="+s2x.x()+" "+s2x.dx()+" s="+s2x.s()+" "+s2x.ds());
    		//s2x=new SDS2XDX("-0.549", "30");
    		//System.out.println("2 s2x.x="+s2x.x()+" "+s2x.dx()+" s="+s2x.s()+" "+s2x.ds());
    		
    		//XDX2SDS xs=new XDX2SDS(1.89450912666018E-4,8.26695990045321E-5,1.5880368903793872E-4,500);
    		
        	//XDX2SDS xs=new XDX2SDS(0.0000049,0.000765,25);
    		//XDX2SDS xs=new XDX2SDS(0.5487733915561722, 0.02844689496718579,  0.028054145990605555);
    		//System.out.println("1 s="+xs.s()+"  ds="+xs.ds()+" dsu="+xs.dsu()+" dsl="+xs.dsl()+" isLimit="+xs.isLimits());
    		//xs=new XDX2SDS(-0.5487733915561722, 0.02844689496718579,  0.028054145990605555);
    		//System.out.println("2 s="+xs.s()+"  ds="+xs.ds()+" dsu="+xs.dsu()+" dsl="+xs.dsl()+" isLimit="+xs.isLimits());
    		
    		//System.out.println(xs.getNumberFormat("0.0018966E-4")+"  "+xs.getExpoFormat("0.0018966", 0));
    		
    		//XDX t12=new XDX(0.0515,0.0003);
    		//XDX decayBR=new XDX("0.006","LT");
    		//XDX pT12=t12.divided(decayBR);
    		//XDX logPT=XDX.log10(pT12);
    		//System.out.println("$$$ t12="+t12.s+" dx="+t12.dxu+" ds="+t12.dsu+" decayBR="+decayBR.s+"  dx="+decayBR.dxu+" ds="+decayBR.dsu+" pT12="+pT12.s+" ds="+pT12.dsu
    		//		+"  "+pT12.dsl+"  dx="+pT12.dxu+"  "+pT12.dxl+" logPT="+logPT.x+" dx="+logPT.dxu+" "+logPT.dxl+"  dsu="+logPT.dsu);
    	    //SDS2XDX sx1=new SDS2XDX("0.04","+3-2");
    	    //SDS2XDX sx2=new SDS2XDX("0.00100","1");
    	    //sx1=sx1.multiply(sx2);
    	    //sx1=sx1.add(sx2);
    	    //System.out.println(" x="+sx1.x()+"  dx="+sx1.dx()+" dxl="+sx1.dxl()+" dxu="+sx1.dxu()+" s="+sx1.s()+"  ds="+sx1.ds()+" dsl="+sx1.dsl()+" dsu="+sx1.dsu());
    	    
            //SDS2XDX sx3=sx1.multiply(sx2);
    	    //System.out.println(" x="+sx3.x()+"  dx="+sx3.dx()+"  s="+sx3.s()+"  ds="+sx3.ds());
    	    
    		//String[] out=EnsdfUtil.parseJPI(">3/2");
    		//for(int i=0;i<out.length;i++)
    		//	System.out.println(out[i]);
    		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    static void test1() throws Exception {
                
        try {
            MCErrorPropagation MCEP=new MCErrorPropagation(1000);

            MCErrorPropagation.FunctionMD f1=(pars)->{
                double C=0.04386490834904011;
                double A=C*pars[0]/Math.pow(pars[1],2);
                if((1.0-4*A)<0)
                    return 0;
                
                return (1.0-Math.sqrt(1.0-4*A))/2;
            };
            
            MCErrorPropagation.FunctionMD f2=(pars)->{
                double C=0.04386490834904011;
                double A=C*pars[0]/Math.pow(pars[1],2);
                if((1.0-4*A)<0)
                    return 0;
                
                return (1.0+Math.sqrt(1.0-4*A))/2;
            };
            
            double r=0.0259,dru=0.0016,drl=0.0016;
            double b=0.073,dbu=0.012,dbl=0.014;
            MCEP.addParam("rho2",r,dru,drl,Math.max(0,r-100*drl),r+100*dru,false);
            MCEP.addParam("db2",b,dbu,dbl,Math.max(b/100.,b-100*dbl),b+100*dbu,false);
            
            MCEP.setFunction(f1);
            MCEP.doWork();        
            
            double x=MCEP.getResult().v;
            double dxu=MCEP.getResult().dvu;
            double dxl=MCEP.getResult().dvl;
            
            System.out.println("x1="+x+"  dxu="+dxu+" dxl="+dxl);

            MCEP.setFunction(f2);
            MCEP.doWork();        
            
            x=MCEP.getResult().v;
            dxu=MCEP.getResult().dvu;
            dxl=MCEP.getResult().dvl;
            System.out.println("x2="+x+"  dxu="+dxu+" dxl="+dxl);
                
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }    
}









