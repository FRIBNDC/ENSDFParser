package ensdfparser.test;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ensdfparser.calc.XDX;
import ensdfparser.check.JPIMatcher;
import ensdfparser.check.JPIPattern;
import ensdfparser.ensdf.Comment;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.FindValue;
import ensdfparser.ensdf.History;
import ensdfparser.ensdf.JPI;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.ensdf.XDX2SDS;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;
import ensdfparser.nsr.BaseNSR;
import ensdfparser.nsr.NSRAbstract;

public class Test1 {
	
	  public static void main(String[] args) throws Exception {
		  //test2();
		  
		  //XDX2SDS.setIsForcePrecision(false);
		  //XDX2SDS xs=new XDX2SDS(788.4323641301353,4.999999924987242E-4,35);
		  XDX2SDS xs=new XDX2SDS();
		  xs.setErrorLimit(999);
		  //xs.setValues(250.20208907697446,154.48002669554097,99.04541567994734);
		  xs.setValues(788.4230990121149,4.999999999881766E-4);
		  System.out.println(xs.s()+" "+xs.ds()+" "+xs.dsu()+" "+xs.dsl());
		  
		  //xs.setErrorLimit(99);
		  //xs.setValues(250.20208907697446,99.04541567994734,99.04541567994734);
		  //System.out.println(xs.s()+" "+xs.ds()+" "+xs.dsu()+" "+xs.dsl());
		  
		  //String s=Str.roundByNsig("0",7);
		  //System.out.println(s);
		  
		  //System.out.println("@"+Str.findNSig("00.01000"));
		  //System.out.println("#"+Str.removeEndZeros("0"));
		  /*
		  SDS2XDX sx=new SDS2XDX("1354.1","1");
		  
		  //sx.setIsKeepSameUncNDigit(true);
		  
		  //sx.setIsKeepSameNDAfterDot(true);
		  
		  SDS2XDX sx1=new SDS2XDX("0.9","1");
		  sx1.setIsKeepSameNDAfterDot(true);
		  
		  sx=sx.multiply(sx1);
		  
		  System.out.println(sx.s()+"  "+sx.ds());
		  
		  //sx=sx.add("8647.834","5");
		  
		  //System.out.println(sx.s()+"  "+sx.ds());
		  */
		  
		  /*
		  String s1="GE3/2";
		  String s2="LE5/2";
		  
		  String[] temp1=EnsdfUtil.parseJPI(s1);
		  String[] temp2=EnsdfUtil.parseJPI(s2);
		  
		  
		  for(int i=0;i<temp1.length;i++)
			  System.out.println(temp1[i]);
		  
		  
		  System.out.println("######");
		  for(int i=0;i<temp2.length;i++)
			  System.out.println(temp2[i]);
		  
		  System.out.println(EnsdfUtil.isLargeDJ(s1, s2, 2));
		  */
		  /*
		  String line=" 34CL  G 1426.5    72100.0   12(M1+E2)   -0.11   5                      ";
		  
		  line=EnsdfUtil.replaceFieldOfRecordLine(line,21,10,"95.3",29,2,"13");
		  line=EnsdfUtil.replaceFieldOfRecordLine(line,31,10,"M1+E2");
		  System.out.println(line);
		  */
		  /*
		  //String cs=" 78RB cL T$from |t=0.61 ps {I+10-8}";
		  String cs=" 78RB cL T$from effective limit |t<0.64 ps";
		  String valName="|t";
		  
			int k=cs.indexOf("$");
			if(k<0)
				k=-1;
			
			cs=cs.substring(k+1).trim();
			if(cs.toUpperCase().startsWith("FROM "))
				cs=cs.substring(5).trim();
			
			k=cs.indexOf(valName);
			if(k>0) {
				String s=cs.substring(0,k).trim();
				if(Str.isLetters(s))
					cs=cs.substring(k).trim();
			}
			
			System.out.println(cs);
		  FindValue fv=new FindValue();
		  fv.findValueInEntry(cs,valName);			
				
		  String s=fv.s();
			
		  String ds=fv.ds();
			
		  String unit=fv.units();
				
		  fv.findValueInEntry(cs,valName);
		  System.out.println("cs="+cs+" valName="+valName+"  s="+s+" ds="+ds+" "+unit);
          */
		  
		  /*
		    double ef=933.839154457491, def=0.003870447589812546;
		    int errorLimit=35;
			XDX2SDS x2s=new XDX2SDS(ef,def,errorLimit);
			String ES=x2s.s();
			String DES=x2s.ds();
			
			System.out.println("EF="+ef+"  "+def+" ES="+ES+"  ### "+DES);
			
		  //System.out.println(JPI.isValidJPS("0+"));
		  //System.out.println(JPI.buildJPIPatternRegex());
		  */
		  
		  /*
		  SDS2XDX sx1=new SDS2XDX("4.3","1");
		  SDS2XDX constant=new SDS2XDX("0.03","");
	    	SDS2XDX temp=constant.divided(100);
        	temp=sx1.multiply(temp);//this gives the extra uncertainty to add
        	//temp.setValues(0.0,temp.x());
            //sx1=sx1.multiply("1.3","9");
            //sx1.setValues(sx1.x(),temp.x());
            //sx1=sx1.roundToNDigitsAfterDot(1);
            
            */
		  
        	/*
        	String s=sx1.s();
        	double x=sx1.x();
        	int n=Str.findNDigitsAfterDot(s);
        	temp.setValues(sx1.x(),temp.x());

        	temp=temp.roundToNDigitsAfterDot(n);
        	if(temp==null) {
        		sx1.setValues(s, "1");
        		sx1.setX(x);
        	}else {
        		sx1.setValues(temp);
        	}
        	*/
        	//sx1=sx1.add("0.00","3");
            //System.out.println(sx1.s()+"  # "+sx1.ds());
            
		  /*
		  String body = JPI.buildJPIPatternRegex();

		  //Pattern JPI_AFTER_EG_PATTERN=Pattern.compile("(to|from)\\s+(?:J(\\|p)?=|\\d+(?:\\.\\d+)?,\\s*|.*?with\\s+J(?:\\|p)?=)?(" + body + ")");
		  
	      String s1 = "(to|from)\\s+(?:J(?:\\|p)?=|\\d+(?:\\.\\d+)?,\\s*)?(" + body + ")";
	      String s2 = "(to|from)\\s+(?:.*?with\\s+J(?:\\|p)?=)(" + body + ")";
	      //Pattern JPI_AFTER_EG_PATTERN=Pattern.compile(s1+"|"+s2);
	      JPIPattern JPI_AFTER_EG_PATTERN=new JPIPattern(s1+"|"+s2);
		  //String jpiWindow=" to 0+";
	      //String jpiWindow=" M1 to 71.6 level with J=(1)";
	      //String jpiWindow="290|g D to (14), 242|g from 3559 level with J=(16)";
	      String jpiWindow=" from 13 level with J=(16)";
	      
		  JPIMatcher jpiMatcher = JPI_AFTER_EG_PATTERN.matcher(jpiWindow);
          if (jpiMatcher.find()) {
            String jpiTrigger=jpiMatcher.trigger();
          	String jpi = jpiMatcher.jpi();        // JPI only
            System.out.println(" jpi="+jpi+" jpiTrigger="+jpiTrigger+" "+jpiMatcher.getRawMatcher().groupCount());
            System.out.println(jpiMatcher.getRawMatcher().group(3)+" "+jpiMatcher.getRawMatcher().group(4));
          }
		  */
		  
		        
		  /*
		  Vector<String> keynumbers=new Vector<String>();
		  keynumbers.add("2006OG05");
		  
		  String outputS=BaseNSR.downloadNSROutputStringByKeynumbersV100_URLC(keynumbers,"exchange");
		  
		  System.out.println(outputS);
		  */
		  
		  //test2();
		  //SDS2XDX sx=new SDS2XDX("1234.56","+5000-3");
		  //System.out.println(sx.roundToNDigitsAfterDot(1).toString());
		  //Translator.init();
		  //String line=EnsdfUtil.cleanENSDFLine("133SN DB EAV,LOGFT$FROM BetaShape v2.4 (Jun-2024) 2023MO21",true);
		  //System.out.println(line);
		  
		  //XDX2SDS xs=new XDX2SDS(0.95,0.47,0.21, 35);
		  //System.out.println(xs.s()+" "+xs.dsu()+" "+xs.dsl()+" "+xs.ds());
		  
		  
		  //for f=A*B*C, if one is limit and the other two are not,
		  //the order of the limit one in the operation matters when useMaxMin=false (then use Taylors expansion)
		  //e.g., if B is the limit one, for A*B, max and min is used even though useMaxMin=fase, then (A*B)*C use
		  //max and min method also for error propagation. 
		  //But if f is calculated as A*C*B, calculation of A*C uses Taylor's expansion, but then (A*C)*B use max 
		  //and min, the resulting limit value will be different from that of A*B*C
		  //However, if useMaxMin=true, then the order doesn't matter, since all use max and min approach for 
		  //error propagation. So if there is one limit value in a multiple-step operations (>2), put it at the end
		  //of the operation
		  //SDS2XDX.setIsUseMaxMinEPByDefault(true);
		  //SDS2XDX sx1=new SDS2XDX("10","2");
		  //SDS2XDX sx2=new SDS2XDX("0.00050","+0-49");
		  //SDS2XDX sx2=new SDS2XDX("10","2");
		  
		  //SDS2XDX sx3=new SDS2XDX("10","LT");
		  //sx1.setValues(0.05,0.0,0.04989995,99);
		  //sx1=sx1.multiply("0.05","LT");
		  //sx1=sx1.divided("0.5","LT");
		 
		  //sx1=sx1.multiply(sx3);
		  //sx1=sx1.multiply(sx2);
		  
		  //SDS2XDX sx1=new SDS2XDX();
		  //sx1.setValues(1580.0,1.5799984198038232E9,120.0000000000001,99);
		  //System.out.println("#### "+sx1.s()+" $"+sx1.dsu()+" $"+sx1.dsl()+" $"+sx1.ds()+" "+sx1.canGetDX()+"  "+sx1.isLimits()+" "+sx1.x()+" "+sx1.dxu()+" "+sx1.dxl());
		  
		  //XDX xd1=new XDX(0.79,0.06);
		  //XDX xd2=new XDX("0.0005","LT");
		  //xd1=xd1.multiply(xd2);
		  
		  //System.out.println("### xd2="+xd2.x+" dxu="+xd2.dxu+" dxl="+xd2.dxl+" s="+xd2.s+" dsu="+xd2.dsu+" dsl="+xd2.dsl);
	      //System.out.println("### x="+xd1.x+" dxu="+xd1.dxu+" dxl="+xd1.dxl+" s="+xd1.s+" dsu="+xd1.dsu+" dsl="+xd1.dsl);
		  
		  
		  //System.out.println("#### "+sx1.s()+" "+sx1.dsu()+" "+sx1.dsl()+" "+sx1.ds()+" "+sx1.canGetDX()+"  "+sx1.isLimits());
		  //System.out.println("  "+sx2.isLimits());
		  
		  //sx1=sx1.divided(100.0,0);
		  
		  
		  //System.out.println(sx1.s()+" "+sx1.dsu()+" "+sx1.dsl()+" "+sx1.ds());
		  
		  //System.out.println(Str.printNumberInGoodFormat(-908.3, 0.001,1000,4));
		  
		  /*
		  String crEntry="%EC+%B+=2E-7 CA";
  		  FindValue fv=new FindValue();
  		  fv.findValueInEntry(crEntry);
  		  String name=fv.name();
  		
  		  //if(name.equals("DCO(D)"))
  		  System.out.println(" entry="+crEntry+" fv.name="+fv.name()+" NAME="+name+" s="+fv.s()+" ds="+fv.ds() );
  		  */
	  }
		

	  public static void splitTest() throws IOException {
		  String test = "Line1 \n\n\nLine2\r\rLine3\r\n\r\n Line4";
		  String[] temp1=test.split("[\\s]+"); 
		  String[] temp2=test.split("[\\r?\\n|\\r]+");
		  String[] temp3=test.split("(\\r?\\n|\\r)+");
		  String[] temp4=test.split("\\r?\\n|\\r");
		  String[] temp5=test.split("\\R");
		  
		  String test1="testtest";
		  String[] temp6=test1.split("[ts]+");
		  String[] temp7=test1.split("[ts]");
		  String[] temp8=test1.split("[ts]",-1);
		  print(temp1);
		  print(temp2);
		  print(temp3);
		  print(temp4);
		  print(temp5);
		  print(temp6);
		  print(temp7);
		  print(temp8);
		  
	  }
	  
	  private static void print(String[] temp) {
		  System.out.print("count="+temp.length+"---");
		  for(int i=0;i<temp.length;i++)
			  System.out.print(String.format("%-10s","$"+temp[i]+"$"));
		  System.out.println();
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
	        
	        
	        for(Level lev:ens.levelsV()) {
	        	System.out.println(lev.ES()+" XREF="+lev.XREFS());
	        	for(String s:lev.XRefsV())
	        		System.out.print(s+"-");
	        	System.out.println();
	        }
	        	
	        
	        //System.out.println(ens.errorMsg());
	        
	        /*
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
	        
	        for(History hist:ens.histV()) {
		        //String link=EnsdfUtil.findKeynumberByHistoryLine(hist);
	        	NSRAbstract abs=EnsdfUtil.findNSRAbstractByHistoryLine(hist);
	        	String link=abs.DOI();
		        System.out.println("cit="+hist.cit()+"  LINK="+link);
	        }
            */
	        
	        SDS2XDX AIA=new SDS2XDX();
	        
            AIA.setValues(86.0,14.0,35.0,35);

            //System.out.println("1 AIA x="+AIA.x()+" "+AIA.dxu()+" "+AIA.dxl()+" s="+AIA.s()+" ds="+AIA.dsu()+" "+AIA.dsl());
            
            //System.out.println(AIA.dx()+" "+AIA.dxu());
            
            AIA=AIA.divided(100.0,0);
            
            //System.out.println("2 AIA x="+AIA.x()+" "+AIA.dxu()+" "+AIA.dxl()+" s="+AIA.s()+" ds="+AIA.dsu()+" "+AIA.dsl());

		}
		
	  public static void test0() {
		  //String line="M1,E2 from ce data and D+Q with "
		  //		+ "|d(Q/D)=+0.18 {I+20-14} (1981Ya07), +2.5 {I+14-9} from |g|g(|q) in 1981Ya07, and -0.24 {I5}.";
		  
		  String line="I(ceK)|>0.10 {I5} (1964Be10) gives |a(K)exp>0.039 {I21}";
		  
		  /*
		  Vector<FindValue> fvs=EnsdfUtil.findDataEntriesInLine(line);			
		  System.out.println(" line="+line+"  fvs size="+fvs.size());
		  for(FindValue fv:fvs)
			  System.out.println("  fv="+fv.txt()+" #s="+fv.s()+" #ds="+fv.ds());
		  */
		
		  /*
		  ArrayList<Integer> indexes=Str.findAllIndexesOfPattern(line, "=");
		  for(int i=0;i<indexes.size();i++) {
			  System.out.println("i="+i+" index="+indexes.get(i).intValue());
			  
		  }
		  
		  LinkedHashMap <Integer,String> indexPatternMap=Str.findIndexesOfPatternMap(line, EnsdfUtil.ENSDF_relation_string_all_OPs);
		  for(Integer index:indexPatternMap.keySet())
			  System.out.println("At index="+index.intValue()+" pattern=<"+indexPatternMap.get(index)+">");
		  */
		  
		  String[] out=EnsdfUtil.splitEntryLine(line);
		  for(int i=0;i<out.length;i++)
			  System.out.println(" i="+i+"   entry="+out[i]);
		  
	  }
	  
	  public static void test1() {
		    java.text.NumberFormat nfrnd=null;
	        if (nfrnd==null){
	        	/*In Java, when you use NumberFormat (such as NumberFormat.getInstance().format(616.5)), 
	        	 * the default rounding mode is HALF_EVEN (also known as "bankers' rounding"). This means:
	        	 * If the fractional part is exactly .5 (or .xx5), it rounds to the nearest even integer.
	        	 */
	            nfrnd = java.text.NumberFormat.getInstance();
	            nfrnd.setMaximumFractionDigits(1);
	            nfrnd.setMinimumFractionDigits(0);
	            //nfrnd.setRoundingMode(RoundingMode.HALF_UP);
	            nfrnd.setGroupingUsed(false);
	        }
	        System.out.println(nfrnd.format(2.25));
	  }
}
