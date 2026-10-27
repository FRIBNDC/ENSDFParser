package ensdfparser.nds.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ensdfparser.calc.BriccsWrap;

/**
 * Various assorted string operations.  All methods are static.
 */
public class Str {
    
    /*
     * fix the line length to be the given length by adding 
     * white spaces at the end
     */
    public static String fixLineLength(String line,int length){
        String s=line;
        if(s.length()<length ){
            for( int k=s.length(); k<length ; k++)
                s=s+" ";
        }else{
            s=line.substring(length).trim();
            if(s.isEmpty())
                s=line.substring(0, length);
            else
                s=line;
        }
        return s;
    }
    
    public static <T> Vector<T> a2v(T[] array){
        Vector<T> out=new Vector<T>();
        try {
            ArrayList<T> list=new ArrayList<T>(Arrays.asList(array));
            out.addAll(list);
        }catch(Exception e) {
            
        }
        return out;
    }
    
    
    /*
     * add given number of white spaces at tail
     */
    public static String addTailSpace(String line,int nSpaces){
        String s=line;
        for(int i=0;i<nSpaces;i++)
            s+=" ";
        
        return s;
    }
    
    /*
     * add given number of white spaces at beginning
     */
    public static String addHeadSpace(String line,int nSpaces){
        String s="";
        for(int i=0;i<nSpaces;i++)
            s+=" ";
        
        return s+line;
    }
    
    public static String addMarginSpace(String lines,int nSpaces){
    	if(nSpaces<=0)
    		return lines;
    	
        String s=repeat(" ",nSpaces);
        return addMarginHead(lines,s);
    }
    
    /*
     * Example:
     * for a string with multiple lines
     *          ="*** T1/2 result is for actual T/g\n"
     *           "    Width0 and Width values are for actual width/g"
     *add a head=" NOTE:" as margin head
     * NOTE: *** T1/2 result is for actual T/g
     *           Width0 and Width values are for actual width/g
     */
    public static String addMarginHead(String lines,String head){
    	try {
              if(head.isEmpty())
            	return lines;
            
              //note that all trailing empties (e.g., multiple \n) are not included, but heading empty is
              //e.g., "\ntest\n" will be split into two lines:
              //         first is empty
              //         second=test
            String[] temp=lines.split("\n");
            if(temp.length==1) {
            	return head+lines;
            }
            
            String out="";
            String space=repeat(" ", head.length());
            
            boolean added=false;
            String s="";
            for(int i=0;i<temp.length;i++) {
            	s=temp[i];
            	

            	if(s.isEmpty()) {
            		out+="\n";
            	}else if(s.equals("\r") || s.equals("\t")) {
            		if(i==0)
            			out+=s;
            		else
            			out+="\n"+s;
            	}else if(!added) {
            		if(i==0)
            			out+=head+s;
            		else
            			out+="\n"+head+s;
            			
            		added=true;
            	}else {
                	out+="\n"+space+s;
            	}
            	//System.out.println("############# s="+s+"$\n"+out+"#####"+added+"  "+s.equals("\r")+"  "+s.equals("\t"));
            }
            s=lines;
            while(s.endsWith("\n")) {
            	out+="\n";
            	s=s.substring(0,s.length()-1);
            }
            return out;
    	}catch(Exception e) {
    		
    	}
    	
    	return lines;

    }
    
    /*
     * for limited space, in the ENSDF dataset, sometimes the heading zero before "." is removed.
     * This function is to add back that zero if it is missing.
     */
    public static String addMissingZero(String s){
        String out="";
        s=s.trim();
        int p=s.indexOf(".");
        if(p<0 || p==s.length()-1)
            return s;
        
        do{
            if( (p>0 && Character.isDigit(s.charAt(p-1))) || !Character.isDigit(s.charAt(p+1)) ){
                out+=s.substring(0,p+1);
            }else{
                out+=s.substring(0,p)+"0.";
            } 
            
            s=s.substring(p+1);
            p=s.indexOf(".");   
        }while(p>=0 && p<s.length()-1);

        out+=s;
        
        return out;
    }
    
    /** Optimized split core routine */
    protected static String[] split_core(char[] str,char[] delim){
        Vector<String> v = new Vector<String>();
        String tmp="";
        for (int x=0; x<str.length; x++){
            boolean use = true;
            for (int y=0; y<delim.length; y++){
                if (str[x]==delim[y]){
                    use=false;
                    v.add(tmp);
                    tmp = "";
                    break;
                }
            }
            if (use)
                tmp+=str[x];
        }
        if (tmp.length()>0)
            v.add(tmp);
        String[] ret = new String[v.size()];
        v.toArray( ret );
        return ret;
    }
    /**
     * Split a string into an array of substrings with a list of
     * characters to use a delimeters.  To be used instead of
     * String.split in simple cases.
     */
    public static String[] split(String str,String delim){
        return Str.split_core(str.toCharArray(),delim.toCharArray());
    }
    /// optimize split routine by avoiding repeated object creation
    protected final static char[] whitespace = {' ','\r','\n','\t'};
    /**
     * Split a string into an array of substrings using common whitespace
     * characters.
     */
    public static String[] split(String str){
        return Str.split_core(str.toCharArray(),whitespace);
    }
    /// Point to string in cm.
    public static String point(float x,float y){
        return "("+str(x)+"cm,"+str(y)+"cm)";
    }
    /// Point to string, user defines units.
    public static String point(float x,float y,String unit){
        return "("+str(x)+unit+","+str(y)+unit+")";
    }
    
    protected static java.text.NumberFormat nf=null;
    
    /// Convert float to metapost readable string
    public static String str(float val){
        String out="";
        if (nf==null){
            nf = java.text.NumberFormat.getInstance();
            nf.setMaximumFractionDigits(4);
            nf.setMinimumFractionDigits(1);
            nf.setRoundingMode(RoundingMode.HALF_UP);
            nf.setGroupingUsed(false);
        }
        
        out=nf.format(val);
        
        //there are cases in some computers (some country), "," instead of "." is used
        //as decimal separator
        out=out.replace(",", ".");
        
        return out;
    }

    /*right-align a string for a given width*/
    public static String right(int width,String str) {
        return String.format("%" + width  + "s", str);
    }
    
    /*right-align a substring in a given string*/
    public static String rightAlignSubtring(String str,int beginIndex,int endIndex) {
        try {
            String sub=str.substring(beginIndex,endIndex).trim();
            int len=endIndex-beginIndex;
            sub=right(len,sub);
            return replace(str, beginIndex,len,sub);
        }catch(Exception e) {
            return str;
        }

    }
    
    protected static java.text.NumberFormat nfrnd=null;
    /// Convert float to rounded string
    public static String roundToStr(float val){
        if (nfrnd==null){
        	/*In Java, when you use NumberFormat (such as NumberFormat.getInstance().format(616.5)), 
        	 * the default rounding mode is HALF_EVEN (also known as "bankers' rounding"). This means:
        	 * If the fractional part is exactly .5 (or .xx5), it rounds to the nearest even integer.
        	 */
            nfrnd = java.text.NumberFormat.getInstance();
            nfrnd.setMaximumFractionDigits(0);
            nfrnd.setMinimumFractionDigits(0);
            nfrnd.setRoundingMode(RoundingMode.HALF_UP);
            nfrnd.setGroupingUsed(false);
        }
        String s=nfrnd.format(val).trim();
        if(s.endsWith("."))
        	s=s.substring(0,s.length()-1);
        
        return s;
    }
    
    //uncertainty in ENSDF format: after-decimal>=0.345 should be round up, e.g., 0.35, 0.36 (0.34 rounded down)
    //e.g., 2.4 to 3, 2.3 to 2, 2.35 to 3, 2.345 to 3, 2.344 to 2
    public static long roundUncertainty(double unc) {
        return round(unc,0.155f);
    }
    
    // note that Math.round() rounds 1.5 (1.4999999) to 1 not 2
    public static long round(double v,double roundingOffset) {
    	String sign="";
    	if(v<0) {
    		sign="-";
    		v=-v;
    	}
        long result=Math.round(v+roundingOffset);
        if(!sign.isEmpty())
        	result=-result;
        
        return result;
    } 
	
	public static int romanToInt(String s) {
		try {
			if(isInteger(s))
				return Integer.parseInt(s);
			
		    HashMap<Character,Integer> numbersMap = new HashMap<>();
		    numbersMap.put('I',1);
		    numbersMap.put('V',5);
		    numbersMap.put('X',10);
		    numbersMap.put('L',50);
		    numbersMap.put('C',100);   
		    numbersMap.put('D',500);   
		    numbersMap.put('M',1000);
		    
		    int result=0;
	        
		    for(int i=0;i<s.length();i++)
		    {
		      char ch = s.charAt(i);      // Current Roman Character
		      
		      if(i<s.length()-1 && numbersMap.get(ch)<numbersMap.get(s.charAt(i+1))) {
		    	  result-=numbersMap.get(ch);
		      }else {
		    	  result+=numbersMap.get(ch);
		      }
		    }
		        
		    return result;
		}catch(Exception e) {
			
		}
		
		return -1;
	}
	
    public static long round(double v) {
      double roundingOffset=0.0;//0.05, for rounding up 1.45 to 2, but rounding down 1.44 to 1
      return round(v,roundingOffset);
    }
    
    public static String roundToNDigitsAfterDot(String s,int n){
        return roundToNDigitsAfterDot(s,n,0);
    }
    
    /*
     * round up a number string by keeping n digits at the end of the number string after decimal point
     * using scientific format if rounding up beyond ".",
     * with base within (1.0 to 9.9)
     * If n is so large that base<1.0, return original number string without rounding
     * offset: added to control rounding up or down
     * e.g., for value, add 0.55 to round up xxx.45, (note xxx.44 will be rounded down)
     *       for uncertainty, add, 0.7 to round up x.3
     */
    public static String roundToNDigitsAfterDot(String s,int n,float offset){
        if(!isNumeric(s) || n<0)
            return s;
        
        int idot=s.indexOf(".");
        int nDigitsAfterDot=0;
        int nDigitsToRoundOff=0;
        
        if(idot>=0) {
            nDigitsAfterDot=s.substring(idot+1).trim().length();
            nDigitsToRoundOff=nDigitsAfterDot-n;
        }
        
        if(nDigitsToRoundOff<=0)
            return s;
        

        return roundUpByNDigits(s,nDigitsToRoundOff,offset);
    }    
    
    public static String roundUpByNDigits(String s,int n){
        return roundUpByNDigits(s,n,0);
    }
    /*
     * round up a number string by removing n digits at the end of the number string after decimal point
     * using scientific format if rounding up beyond ".",
     * with base within (1.0 to 9.9)
     * If n is so large that base<1.0, return original number string without rounding
     * offset: added to control rounding up or down
     * e.g., for value, add 0.55 to round up xxx.45, (note xxx.44 will be rounded down)
     *       for uncertainty, add, 0.7 to round up x.3
     */
    public static String roundUpByNDigits(String s,int n,float offset){
        if(!isNumeric(s) || n<=0)
            return s;
        
        String s0=s;
        String powerS="";
        int i=s.toUpperCase().indexOf("E");
        if(i>0){
            powerS=s.substring(i);
            s=s.substring(0, i);
        }
            
        s=roundUpByNDigits0(s,n,offset);

        if(s.contains("E") && powerS.length()>0)
            return s0;
        
        return s+powerS;
    }
    
    /*
     * round regular number string (not scientific format)
     * used only in roundNumberString(s,n)
     * here n>0, number of digits to round up (remove) at the end of the number string
     * offset: added to control rounding up or down, added to the value=(original value)*pow(10,nDigitsAfterDot-n)
     * e.g., for value, add 0.55 to round up xxx.45, (note xxx.44 will be rounded down)
     *       for uncertainty, add, 0.7 to round up x.3
     *       for offset=0.55, rounding following the rule:
     *       a value<1.0-0.55=0.45 is rounded down 
     *       a value>=0.45 is rounded up 
     *       so 0.44 is rounded down to 0.4 by 1 digit off, to 0 by 2 digits off
     *          0.45 is rounded up to 0.5 by 1 digit off, to 1 by 2 digits off
     *       if offset=0, rounding is by default (build-in rounding), which is 
     *       equivalent to set offset=0.5, so that 0.499 will be rounded down and 0.500
     *       will be rounded up
     */
    private static String roundUpByNDigits0(String s,int n,float offset){  
        String temp=s.replace(".", "");
        if(n>temp.length()-1)
            return s;
        
        int idot=s.indexOf(".");
        int nDigitsAfterDot=0;
        if(idot>=0)
            nDigitsAfterDot=s.substring(idot+1).trim().length();
        
        double f=Double.parseDouble(s);
        String sign="";
        if(f<0 && s.startsWith("-")) {
        	sign="-";
        	f=-f;
        }
        long intVal=-1;
        if(n<=nDigitsAfterDot){
            //"." position (idot) remains the same, just remove n digits at the end and round up
            //unless it keeps rounding up all the way beyond "." and increase by one digit of integer, 
            //like, 9.9995 rounded up by one digit to become 10.000
            nDigitsAfterDot=nDigitsAfterDot-n;
            if(offset<=0) {
                intVal=(long)Math.round(f*Math.pow(10,nDigitsAfterDot));//regular rounding

            }else {
                //rounding based on offset, rounding down decimal part<1.0-offset
                //e.g., offset=0.55, rounding own decimal part<1.0-0.55=0.45
                intVal=(long)(f*Math.pow(10,nDigitsAfterDot)+offset);
            }
            
            //System.out.println(" #1  s="+s+" f="+f+" offset="+offset+" intVal="+intVal+" n="+n+"  nDigitsAfterDot="+nDigitsAfterDot+" f="+f+"  "+(f*Math.pow(10,nDigitsAfterDot)+offset));

            s=String.valueOf(intVal);

            //debug
            //if(s.contains("522"))
            //  System.out.println(" #2   s="+s+" intVal="+intVal+" n="+n+"  nDigitsAfterDot="+nDigitsAfterDot+" f="+f+"  "+(f*Math.pow(10,nDigitsAfterDot)+offset));
            
            if(nDigitsAfterDot>0){
                idot=s.length()-nDigitsAfterDot;
                if(idot>0)
                    s=s.substring(0,idot)+"."+s.substring(idot);
                else
                    s="0."+Str.repeat("0", -idot)+s;
            }
        }else if(idot<0 || idot>1){//then use scientific format
            String powerS="";
            if(idot>1)
                powerS="E"+(idot-1);
            else
                powerS="E"+(s.length()-1);
            
            s=s.replace(".", "");
            s=s.substring(0,1)+"."+s.substring(1);
            s=roundUpByNDigits0(s,n,offset)+powerS;
        }
        
        return sign+s;
    }

    /*
     * round a string of a float value to nsig significant digits
     * if value>pow(10,nsig+1), return the rounded integer
     */
    public static String roundByNsig(String str,int nsig){
        try{
            Double.parseDouble(str);
        }catch(Exception e){
          return str;  
        }
        
        //System.out.println("Str 501: str="+str+" nsig="+nsig);
        
        String s=str;
        String sign="";String basePartS="";String expoPartS="";String integerPartS=""; String floatPartS="";

        int nsig0=Str.findNSig(str);
        if(nsig0<nsig)
        	nsig=nsig0;
        
        int isign=s.indexOf("-");
        if( isign == 0 ) { sign="-"; s=s.substring(1); }
        
        int iE=s.indexOf("E");
        if( iE > 0 ){ 
        	expoPartS=s.substring(iE); 
        	s=s.substring(0,iE);         	
        }

        int idot=s.indexOf(".");
        if( idot < 0 ) 
        	basePartS=s;

        //System.out.println("Str 516: str="+str+" nsig="+nsig+" basePartS="+basePartS);
        
        if( idot >= 0) { 
        	 if(s.endsWith("."))
        		 s+="0";
             integerPartS=s.substring(0,idot); 
             floatPartS=s.substring(idot);

             //System.out.println("Str 537: s="+s+"   floatPartS="+floatPartS);
             
             double floatPartX=Double.parseDouble(floatPartS);
             double integerPartX=0;
             if(!integerPartS.isEmpty())
            	 integerPartX=Double.parseDouble(integerPartS);
             
             int nsigAfterDot=nsig;
             
             //System.out.println(" Str line 529 Str="+str+" floatPartS="+floatPartS+"  integerPartS="+integerPartS);
             
             if( floatPartX > 0 ){
               double xlogf=-1.*Math.log(floatPartX)/Math.log(10.);
               int nzerosAfterDot=(int) xlogf;

               //System.out.println(" Str line 533 Str="+str+" floatPartS="+floatPartS+"  nzerosAfterDot="+nzerosAfterDot+"  integerPartS="+integerPartS+" xlogf="+xlogf);
               
               //if the part after decimal is too small, eg, <(integer part/1000), then
               //ignore it
               double r=integerPartX/floatPartX;
               int ndigitsAfterDot=0;

               
               int ilen=integerPartS.trim().length();
               if(r!=0){
                 ndigitsAfterDot=nsig-ilen;
                 if(ndigitsAfterDot<=0){
                     ndigitsAfterDot=0;
                     floatPartS="";
                     integerPartS=String.valueOf((int)(integerPartX+round(floatPartX)));
                 }
               }else
            	   ndigitsAfterDot=nzerosAfterDot+nsigAfterDot;
               
               //System.out.println(" Str line 662 floatPartS="+floatPartS+"  ndigitsAfterDot="+ndigitsAfterDot+" nzerosAfterDot="+nzerosAfterDot+" integerPartS="+integerPartS+" nsig="+nsig+" ilen="+ilen);
               
               if(floatPartS.length()>(ndigitsAfterDot+1) ) {
                  floatPartX=floatPartX*Math.pow(10.,ndigitsAfterDot);
                  long imf=round(floatPartX); 
                  
                  if(imf>0){
                      int p1=(int)(Math.log(imf)/Math.log(10.));//still p1=2 when imf=1000, 2.99999999
                      int p2=(int)(Math.log(floatPartX)/Math.log(10.));
                 
                      //System.out.println("1 floatX="+floatPartX+" imf="+imf+" p1="+p1+"  p2="+p2+" Math.log(floatPartX)/Math.log(10.)="+Math.log(floatPartX)/Math.log(10.));
                      
                      String p1s=Long.toString(imf);
                      String p2s=Integer.toString((int)floatPartX);
                      p1=p1s.length();
                      p2=p2s.length();
                      
                      //note that for case like 33.099, imf=1, p1s=1,p2s=0, p1=1,p2=1, but it should be processed as p1>p2 below
                      //System.out.println("2 floatX="+floatPartX+" imf="+imf+" p1s="+p1s+" p2s="+p2s+" p1="+p1+"  p2="+p2);
                      if(p1s.equals("1") && p2s.equals("0")) {
                    	  p1=1;
                    	  p2=0;
                      }                   	  
                      
                      if(p1==p2) {
                        floatPartS=floatPartS.substring(0,nzerosAfterDot+1)+Long.toString(imf);
                      }else if(p1>p2){//if rounding results in one-digit increment, p1>p2
                        if(floatPartS.charAt(1)=='0'){//equivalent to nzerosAfterDot>0, note that charAt(0)=='.'
                            floatPartS=floatPartS.substring(0,nzerosAfterDot)+Long.toString(imf);
                        }
                        else{//equivalent to nzerosAfterDot=0, increment 1 in integerPart number, e.g., floatPart=.999
                            int i1=Integer.parseInt(integerPartS);
                            int i2=(Math.abs(i1)+1);
                            if(i1<0)
                                i2=-i2;
                            
                            integerPartS=Integer.toString(i2);      
                            floatPartS="."+Long.toString(imf).substring(1);
                        }
                      }  
                      
                      //System.out.println("3 floatS="+floatPartS+" integerPartS="+integerPartS);
                      
                  }else{//1.00 floatPartS=".00"
                    floatPartS=floatPartS.substring(0,ndigitsAfterDot+1);
                  }                                      
//               System.out.println("imf: "+imf+" smf= "+smf);
               }  
               
               basePartS=integerPartS+floatPartS;    
               
               //System.out.println("3.5 floatS="+floatPartS+" integerPartS="+integerPartS+" base="+basePartS);
               
               if(integerPartX>0) {
                   while(basePartS.contains(".") && basePartS.endsWith("0") && (basePartS.length()-1)>nsig ) { 
                       basePartS=basePartS.substring(0,basePartS.length()-1);                  
                     }
               }
   
               //System.out.println("4 floatS="+floatPartS+" integerPartS="+integerPartS+" base="+basePartS);
               
               if(basePartS.endsWith(".")){
                 if(integerPartX<1000 && integerPartS.length()<nsig)
                     basePartS+="0";
                 else
                     basePartS=basePartS.substring(0,basePartS.length()-1);
               }
               
               if(integerPartX>0 && expoPartS.isEmpty() && basePartS.replace(".","").length()>nsig)
                	   basePartS=Str.toSciFormat(basePartS,nsig);
                              
             }else if(integerPartX>0) {
            	 //System.out.println(" Str line 621 Str="+str+" floatPartS="+floatPartS+"  integerPartS="+integerPartS);
            	 
            	 int n=integerPartS.length();
            	 if(n<=nsig) {
                     if(integerPartX<1000 && n<nsig){          	            
                    	 basePartS=integerPartS+".0";
                    	 n++;
                    	 while(n<nsig) {
                    		 basePartS+="0";
                    		 n++;
                    	 }
                     }else{
                    	 basePartS=integerPartS;
                     }
            	 }else if(expoPartS.isEmpty()) {//convert to scientific format if it is not
            		 basePartS=Str.toSciFormat(integerPartS, nsig);
            	 }else {
            		 basePartS=integerPartS;
            	 }

             }else {
            	 basePartS=integerPartS;
             }
             //System.out.println("5 floatS="+floatPartS+" integerPartS="+integerPartS+" base="+basePartS);
//             System.out.println("smi: "+smi+" smf= "+smf);
        }else if(Str.isInteger(basePartS) && expoPartS.isEmpty() && basePartS.length()>nsig){    	
        	basePartS=Str.toSciFormat(basePartS, nsig);
        }
        s=sign+basePartS+expoPartS;
        
        //System.out.println("Str 649: s="+s);
        return s;
    }
    /**
     * Take a file name and add a new extension to it
     */
    public static String copyFileName(String fn,String ext){
        int ldot = fn.lastIndexOf(".");
        if (ldot<=0){
            return fn + "." + ext;
        }
        String nfn = fn.substring(0,ldot);
        return nfn + ext;
    }
    
    /*
     * copy file packed in JAR file to local dir
     */
    public static boolean copyFromResource(String filename,String copyToDir,String option){

        try {
	        //don't work with web app, using getResourceAsStream() instead
			/*
            URL url = ClassLoader.getSystemResource(filename);//get the default file in the classpath if it exists

            if(url==null)//briccs does not exist in the classpath
                return false;

            InputStream is = url.openStream();
            */
        	
	    	InputStream is=BriccsWrap.class.getClassLoader().getResourceAsStream(filename); 
	        if(is==null)
	        	return false;
	        
            String op=option.toUpperCase().trim();
            
            String filePath=copyToDir+dirSeparator()+filename;
            File file=new File(filePath);
            if(file!=null && file.exists() && (op.isEmpty() || op.charAt(0)!='F'))//force to copy only if option="FORCE" or "F" or starts with "F"
                return true;
            
            OutputStream os = new FileOutputStream(filePath);//sets the output stream to a system folder
            
            byte[] b = new byte[2048];
            int length;
            
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
            

            file.setExecutable(true);
            //if (file.exists()) {
            //    System.out.println("read="+file.canRead());
            //    System.out.println("write="+file.canWrite());
            //    System.out.println("Execute="+file.canExecute());
            //}
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    /*
     * copy to dir only if the file doesn't exist. No override
     */
    public static boolean copyToDir(String sourcePath,String copyToDir){
        return copyToDir(sourcePath,copyToDir,"");
    }
    
    
    /*
     * copy file of any type to a destination dir
     */
    public static boolean copyToDir(String sourcePath,String copyToDir,String option){
        
        InputStream is=null;
        OutputStream os=null;
        try {
    
            String op=option.toUpperCase().trim();
            String filename=getNameFromPath(sourcePath);
            
            String newFilePath=copyToDir+File.separator+filename;
            File file=new File(newFilePath);
            if(file!=null && file.exists() && (op.isEmpty() || op.charAt(0)!='F'))//force to copy only if option="FORCE" or "F" or starts with "F"
                return true;
            
            is = new FileInputStream(sourcePath);
            os = new FileOutputStream(newFilePath);//sets the output stream to a system folder
            
            byte[] b = new byte[2048];
            int length;
            
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
            
            file.setExecutable(true);
            //if (file.exists()) {
            //    System.out.println("read="+file.canRead());
            //    System.out.println("write="+file.canWrite());
            //    System.out.println("Execute="+file.canExecute());
            //}
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();        
            return false;
        }
        
        return true;
    }
    
    /*
     * copy to destination file only if the file doesn't exist. No override
     */
    public static boolean copyToFile(String sourcePath,String destinationPath){
        return copyToFile(sourcePath,destinationPath,"");
    }
    
    /*
     * copy file of any type to a destination file
     */
    public static boolean copyToFile(String sourcePath,String destinationPath,String option){
        
        InputStream is=null;
        OutputStream os=null;
        try {
    
            String op=option.toUpperCase().trim();
            
            String newFilePath=destinationPath;
            File file=new File(newFilePath);
            if(file!=null && file.exists() && (op.isEmpty() || op.charAt(0)!='F'))//force to copy only if option="FORCE" or "F" or starts with "F"
                return true;
            
            is = new FileInputStream(sourcePath);
            os = new FileOutputStream(newFilePath);//sets the output stream to a system folder
            
            byte[] b = new byte[2048];
            int length;
            
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
            
            file.setExecutable(true);
            //if (file.exists()) {
            //    System.out.println("read="+file.canRead());
            //    System.out.println("write="+file.canWrite());
            //    System.out.println("Execute="+file.canExecute());
            //}
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();        
            return false;
        }
        
        return true;
    }
    
    public static String getNameFromPath(String filePath) {
        try {
            File f=new File(filePath);
            return f.getName();
        }catch(Exception e) {           
        }
        
        return "";
    }
    
    /*
     * return a valid leading number in s if it starts with a digit.
     * Skip leading blanks
     */
    public static String getLeadingNumber(String s,int fromIndex) {
        try {
            int endIndex=getEndOfLeadingNumber(s,fromIndex);
            if(endIndex<=0)
                return "";
            
            return s.substring(fromIndex,endIndex+1).trim();
        }catch(Exception e) {
            
        }
        return "";
    }
    
    /*
     * return the index of the last digit in the valid leading number 
     * scanned from fromIndex if the first non-blank char from fromIndex
     * is a digit and the scanned number is a valid real number
     */
    @SuppressWarnings("unused")
    public static int getEndOfLeadingNumber(String s,int fromIndex) {
        try {
            boolean hasSign=false,hasDot=false,hasExpo=false;
            
            String s0=s;
            s=s.substring(fromIndex).trim();
            
            if(fromIndex>s.length()-1)
                return -1;
            if(fromIndex<0)
                fromIndex=0;
            
            int tempIndex=fromIndex;
            char c=s.charAt(0);
            if(c=='+' || c=='-') {
                s=s.substring(1);
                hasSign=true;
                tempIndex+=1;
            }else if(c=='.') {
                s=s.substring(1);
                hasDot=true;
                tempIndex+=1;
            }else if(!Character.isDigit(c))
                return -1;
            
            if(s.isEmpty() || !Character.isDigit(s.charAt(0)))
                return -1;
            
            int i=0;
            String numS="";
               
            int p=s0.indexOf(s,tempIndex);
            s=s.toUpperCase();
            
            while(i<s.length()) {
                c=s.charAt(i);
                if(!Character.isDigit(c)&&c!='.'&&c!='E'&&!"+-".contains(c+""))
                    break;
                
                if(c=='E') {
                    if(i==s.length()-1)
                        break;
                    else if("+-".contains(s.charAt(i+1)+"")){
                        if(i==s.length()-2 || !Character.isDigit(s.charAt(i+2))) 
                            break;
                    }else if(!Character.isDigit(s.charAt(i+1))) {
                        break;
                    }
                    
                    hasExpo=true;
                }else if(c=='.') {
                    if(hasDot || hasExpo)
                        break;
                }else if("+-".contains(c+"")) {//can only be sign in exponent
                    if(!hasExpo || s.charAt(i-1)!='E')
                        break;                   
                    
                }
                
                numS+=c;
                i++;
            }
            
            if(!isNumeric(numS))
                return -1;
            
            return p+i-1;
            
        }catch(Exception e) {
            
        }
        return -1;
    }
    
    public static String getParentDir(String filename) {
        String parentDir="";
        
        if(filename==null || filename.trim().isEmpty())
            return "";
        
        filename=filename.trim();
        File f=new File(filename);
        //if(!f.exists())
        //    return "";
   
        File parentFile=f.getAbsoluteFile().getParentFile();
        if(parentFile==null){
            parentDir=".";
        }else{
            parentDir=parentFile.getAbsolutePath();
        }

        return parentDir;
    }
    
    /**
     * Get the first part of the file name.
     */
    public static String fileNamePrefix(String fn){
        if (fn==null)
            return "temp";
        int ldot = fn.lastIndexOf(".");
        if (ldot<=0){
            return fn;
        }
        return fn.substring(0,ldot);
    }
    public static int indexOfNotInBrackets(String word,String str) {
    	return indexOfNotInBrackets(word,str,0);
    }
    public static int indexOfNotInBrackets(String word,String str,int fromIndex) {
    	try {
            Stack<Character> stack = new Stack<>();
            int wordLength = word.length();
            
            if(fromIndex<0)
            	fromIndex=0;
            
            for (int i = fromIndex; i <= str.length() - wordLength; i++) {
                char c = str.charAt(i);
                
                if (c == '(' || c == '[' || c == '{') {
                    stack.push(c);
                } else if (c == ')' || c == ']' || c == '}') {
                    if (!stack.isEmpty()) stack.pop();
                }
                
                if (stack.isEmpty() && str.substring(i, i + wordLength).equals(word)) {
                    return i;
                }
            }
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
    }
    /**
     * Get the first sentence in a string.  this is especially useful to prune
     * down large band comments to make a title.
     */
    public static String firstSentence(String text){
        int idx = indexOfNotInBrackets(".", text);       
        if(idx==1 && Character.isLetterOrDigit(text.charAt(0)))// for case like Band(A)$ 1. 2+, oblate band.
            idx=indexOfNotInBrackets(".", text,2);
        
        // no period, return as is
        if (idx<=0 || idx>=text.length()-1) return text;
        //dont break up the sentence on g.s. or on decimal points
        

        while((text.charAt(idx-1)=='g'&&text.charAt(idx+1)=='s')||(text.charAt(idx-1)=='G'&&text.charAt(idx+1)=='S')||
                (text.charAt(idx-1)>='0'&&text.charAt(idx-1)<='9'&&text.charAt(idx+1)>='0'&&text.charAt(idx+1)<='9')||
                (idx>=3 && text.substring(idx-3,idx).equals("g.s")) || (idx>=3 && text.substring(idx-3,idx).equals("G.S"))){
            //idx=text.indexOf(".",idx+1);
            idx=indexOfNotInBrackets(".", text,idx+1);
            if (idx<0 || idx>=text.length()-1) return text;
        }
        return text.substring(0,idx);
    }
    /**
     * As above, but also stops after first bracket is read
     * Most useful for shortening band names when trying to fit many 
     * bands on one page (for the SD book)
     * @param text
     * @return
     */
    public static String firstSentence2(String text){
        int noFlags=3;
        String [] flags={".","(1","(2"};
        int pos[]=new int[noFlags];
        
        for(int i=0;i<noFlags;i++){
            pos[i]=text.indexOf(flags[i]);
            if(pos[i]>=0 && pos[i]<text.length()) text=firstSentence2(text.substring(0,pos[i]));
        }
        // no period, return as is
        return text;
    }
    
    /**find the first index of all patterns in the patterns ArrayList*/
    public static int firstIndexOfPatterns(String str,ArrayList<String> patterns,int fromIndex){
        if(str.length()<=0 || patterns==null || patterns.size()<=0)
            return -1;
        
        int minIndex=10000,n=-1;
        boolean isGood=false;
        for(int i=0;i<patterns.size();i++) {
        	n=str.indexOf(patterns.get(i),fromIndex);
            if(n>0 && n<minIndex) {
            	minIndex=n;
                isGood=true;
            }
        }
        
        if(isGood)
        	return minIndex;
        
        return -1;
    }
    public static int firstIndexOfPatterns(String str,ArrayList<String> patterns){
    	return firstIndexOfPatterns(str,patterns,0);
    }
    
    /**find the last index of all patterns in the patterns ArrayList*/
    public static int lastIndexOfPatterns(String str,ArrayList<String> patterns,int fromIndex){
        if(str.length()<=0 || patterns==null || patterns.size()<=0)
            return -1;
        
        int maxIndex=-1,n=-1;
        boolean isGood=false;
        for(int i=0;i<patterns.size();i++) {
        	n=str.lastIndexOf(patterns.get(i),fromIndex);
            if(n>0 && n>maxIndex) {
            	maxIndex=n;
                isGood=true;
            }
        }
        
        if(isGood)
        	return maxIndex;
        
        return -1;
    }
    
    public static int lastIndexOfPatterns(String str,ArrayList<String> patterns){
    	return lastIndexOfPatterns(str,patterns,str.length()-1);
    }
    /**find index of each pattern in the str if existing
     * return a linked map of index (>=0) vs pattern at the index
     * key=index, value=pattern at that index 
     * */
    public static LinkedHashMap<Integer,String> findIndexesOfPatternMap(String str,ArrayList<String> patterns){
    	LinkedHashMap<Integer,String> indexMap=new LinkedHashMap<Integer, String>();
        if(str.length()<=0 || patterns==null || patterns.size()<=0)
            return indexMap;
        
        
        String s=str;
        for(String pattern:patterns) {
        	ArrayList<Integer> indexes=findAllIndexesOfPattern(s,pattern);
        	for(Integer index:indexes) {
        		indexMap.put(index,pattern);
        	}   
        	s=s.replace(pattern,Str.repeat("@", pattern.length()));
        }

        // Sort by key (natural order)
        indexMap = indexMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
        
        return indexMap;
    }
    
    
    public static ArrayList<Integer> findAllIndexesOfPattern(String str,String pattern){
    	ArrayList<Integer> indexes=new ArrayList<Integer>();
    	try {
    		if(pattern.isEmpty())
    			return indexes;
    		
    		int n=-1, offset=0;
    		while((n=str.indexOf(pattern,offset))>=0) {
    			indexes.add(n);
    			offset=n+pattern.length();
    		}
    	}catch(Exception e) {
    		
    	}
    	return indexes;
    }
    /*
     * find name, relation operator, value in a relation formula
     * if there are more than one operations, the first one will be used and
     * the rest will be ignored.
     */
    public static String getPartInRelation(String str,String partName){
        String out="";
        
        String[] ops={">=","<=","=",">","<","~"};
        //ArrayList<String> opList=new ArrayList<String>(Arrays.asList(ops));
        
        String s=str.trim();
        if(s.length()==0)
            return "";
        
        String op="";
        String name=s;
        String value=s;
        
        int minIndex=s.length()+100;
        int n=-1;
        
        try{        
            for(int i=0;i<ops.length;i++){
                n=s.indexOf(ops[i]);
                if(n>=0 && n<minIndex){
                    minIndex=n;
                    op=ops[i];
                }
            }
                    
            //if(op.length()>0 && minIndex<s.length()-1){
            //  String temp=s.substring(minIndex,minIndex+2);
            //  if(opList.contains(temp))
            //      op=temp;
            //}
            
            if(op.length()>0){
                name=s.substring(0, minIndex).trim();
                value=s.substring(minIndex+op.length()).trim();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        partName=partName.toUpperCase();
        if(partName.contains("NAME"))
            return name;
        if(partName.contains("OP"))
            return op;
        if(partName.contains("VALUE"))
            return value;
        
        return out;
        
    }
    
    public static String getNameInRelation(String str){
        return getPartInRelation(str,"NAME");
    }
    
    public static String getOpInRelation(String str){
        return getPartInRelation(str,"OP");
    }
    
    public static String getValueInRelation(String str){
        return getPartInRelation(str,"VALUE");
    }
    /**
     * Split a latex-ready String into smaller strings for printing
     */
    public static String [] bandSplit_old(String s,int l){
        String [] flags={"\\ensuremath"," ","-"};
        String [] output=new String[1];
        boolean foundBreakPoint=false;
        int temp=-1,pos, length=0,ensure=0,lastEnsure=0,counter=0;
        int nBrackets=0;
        output[0]=s;
        String [] x;

        //Jeremie: I found the below code more or less incomprehensible, but it was
        //shortening the labels too much so I arbitrarily added 4 to the max length
        l+=4;
        //This can be adjusted if necessary


        for(int i=0;i<output.length;i++){
           //the following increases the amount of characters allowed per line if "\ensuremath" is present
            length=l;
            ensure=0;
            lastEnsure=0;
            
           while(ensure>-1&&ensure<length+1&&counter<10){
               counter++;
               ensure=0;
               ensure=output[i].indexOf("\\ensuremath",lastEnsure+1);
               if(ensure>=0&&ensure<length){//there is an ensuremath present
                //finds the length of characters inside the ensuremath statement
                   nBrackets=1;
                   int m=ensure;
                   m+=11;
                   while(nBrackets>0){
                       m++;
                       if(output[i].charAt(m)=='{') nBrackets++;
                       if(output[i].charAt(m)=='}') nBrackets--;
                   }
//                   System.out.println(m+" "+" "+ensure);
                   length+=m-ensure;
                   lastEnsure=ensure;
               }
              // if(ensure>length-20){ length-=20; lastEnsure=0; break;}
           }
            
           //find the closest delimiter to the end of the line 
           foundBreakPoint=false;
           x=new String[output.length+1];
           pos=0;
            if(output[i].length()>length){
                for(int j=0;j<flags.length;j++){
                    temp=output[i].lastIndexOf(flags[j],length-1);
                    if(temp<length&&temp>0) {
                        if(temp>pos){
                            //tries to put line breaks before superscripts or subscripts, or K\pi
                            if(j==0){
                                if(output[i].charAt(temp+12)=='_') continue;
                                if(output[i].charAt(temp+12)=='^') continue;
                                if(output[i].charAt(temp+12)=='\\'&&output[i].charAt(temp+13)=='p'&&output[i].charAt(temp-1)=='K') continue;
                                
                            }
                            //often hyphens with numbers should have a line broken in them
                            if(j==2){
                                if(output[i].charAt(temp-1)>='0'&&output[i].charAt(temp-1)<='9')continue;
                            }

                            pos=temp;
                            foundBreakPoint=true;
                        }
                    }
                }
                
                //split string
                if(foundBreakPoint==false) continue;
                for(int j=0;j<i;j++) x[j]=output[j];
                x[i]=output[i].substring(0,pos);
                x[i+1]=output[i].substring(pos,output[i].length());
                for(int j=i+2;j<output.length;j++) x[j]=output[j-1];

                output=x;
            }
//               System.out.println(output[i]+" "+output[i].length()+" "+length);
        }
//               System.out.println(output[output.length-1]+" "+output[output.length-1].length()+" "+length);
        
        
        return output;
    }
    
    public static String [] bandSplit(String s,int l){

        String [] output;

        Vector<String> temp=new Vector<String>();
        

        int nBrackets=0;

        l+=4;

        char ch;
        int num=0;
        int prebreak=0;
        int tempbreak=0;
        int ntries=0;
        for(int i=0;i<s.length();i++){
            ch=s.charAt(i);
            if(ch=='^' || ch=='_')
                continue;
            
            if(ch=='{'){
                nBrackets++;
                continue;
            }
            else if(ch=='}'){
                nBrackets--;
                continue;
            }
            
            //skip non-printable characters and not break the string after Greek characters
            if(ch=='\\'){
                if(s.indexOf("\\ensuremath", i)==0){//non-printable
                    i+=11;
                    continue;
                }
                else{//printable LaTex symbol
                    num++;
                    int k=i+1;
                    if(k<s.length()) 
                        ch=s.charAt(k);
                    while(Character.isLetter(ch)){
                        k++;
                        if(k<s.length())
                            ch=s.charAt(k);
                        else
                            break;
                    }
                    
                    if(k<s.length()){//skip special character, eg. Greek symbol
                        i=k-1;
                        continue;
                    }
                }
            }
            
            //break string
            if(nBrackets==0){
                if(num<l){
                    if(ntries==0){
                        if(ch==' ' || ch==',' || ch==';')
                            tempbreak=i;
                    }
                    else{
                        if(ch=='=' || ch=='/' || ch=='(' || ch==')' || ch=='[' || ch==']')
                            tempbreak=i;
                    }
                    
                        
                }
                else{
                    
                    if(ch==' ' || ch==',' || ch==';'){
                        tempbreak=i;
                    }
                    
                    if(tempbreak<=prebreak || (tempbreak-prebreak)<l/2){
                        if(ntries==0){
                            i=prebreak;
                            num=0;
                            ntries++;
                            continue;
                        }
                        else{
                            if(!Character.isLetter(ch) || tempbreak<=prebreak)
                                tempbreak=i;
                        }
                    }
                    
                    temp.add(s.substring(prebreak,tempbreak+1));
                    prebreak=tempbreak+1;
                    i=tempbreak;
                    num=0;
                    ntries=0;
                    continue;
                }
                
            }
            
            num++;
        }
        
        if(prebreak<s.length())
            temp.add(s.substring(prebreak));
        
        output=new String[temp.size()];
        for(int m=0;m<temp.size();m++)
            output[m]=temp.elementAt(m);
        
        
        return output;
    }
    
    public static String ditchPeriods(String test){
        String out="";
        for(int i=0;i<test.length();i++){
            if(test.charAt(i)=='.')
                out+="\\ ";
            else 
                out+=test.charAt(i);
        }
        return out;
    }
    
    /* extract the number showing at the begining of the input string */
    public static String extractHeadingNumber(String str){
        int j=-1;
        String s="";
        
        str=str.trim();
        if(str.startsWith("{"))
            str=str.replace("{","").trim();
        if(str.startsWith("("))
            str=str.replace("(","").trim();

        for(j=0;j<str.length();j++) {
            char c=str.charAt(j);
            if(!Character.isDigit(c)) {
                if(c=='.' && !s.contains(".")) {
                    s+=c;
                    continue;
                }

                break;
            }
            s+=c;
        }

        try {
            Float.parseFloat(s);
        }catch(NumberFormatException e) {
            s="";
        }
        
        return s;
    }
    
    /*left-align a string for a given width*/
    public static String left(int width,String str) {
        return String.format("%-" + width  + "s", str);
    }
    
    /*left-align a substring in a given string*/
    public static String leftAlignSubtring(String str,int beginIndex,int endIndex) {
        try {
            String sub=str.substring(beginIndex,endIndex).trim();
            return replace(str, beginIndex, endIndex-beginIndex,sub);
        }catch(Exception e) {
            return str;
        }

    }
    
    /** given a comma-delimited list of keynumbers, puts the second character into lowercase */
    public static String lowerKeynos(String in){
        Vector <String>keys=new Vector<String>();
        String out="",temp;
        int ix=in.indexOf(',');
        while(ix>=0){
            keys.add(in.substring(0,ix));
            in=in.substring(ix+1);
            ix=in.indexOf(',');
        }
        keys.add(in);
        for(int i=0;i<keys.size();i++){
            temp=keys.elementAt(i);
            if(temp.length()==8) out+=temp.substring(0,5)+temp.substring(5,6).toLowerCase()+temp.substring(6);
            else if(temp.length()==6) out+=temp.substring(0,3)+temp.substring(3,4).toLowerCase()+temp.substring(4);
            else out+=temp;
            if(i<keys.size()-1) out+=",";
        }
        return out;
    }
    
    /**
     * 
     * @param numS1
     * @param numS2
     * @return true if number1 match number2 or math after rounding
     */
    public static boolean isNumbersMatchWithRounding(String numS1, String numS2) {

        if (numS1 == null || numS2 == null) return false;
        if(numS1.equals(numS2))
        	return true;
        
        BigDecimal d1 = new BigDecimal(numS1);
        BigDecimal d2 = new BigDecimal(numS2);

        if (d1.compareTo(d2) == 0) return true; // exact match

        String test=numS1,ref=numS2;
        if(numS1.length()>numS2.length()) {
        	test=numS2;
        	ref=numS1;
        }
        	
        return isNumberMatchRefWithRounding(test,ref);
    }  
    
    /**
     * 
     * @param test
     * @param ref
     * @return true if test number match ref number or rounded value of the ref number
     */
    public static boolean isNumberMatchRefWithRounding(String test, String ref) {

        if (test == null || ref == null) return false;
        if(test.equals(ref))
        	return true;
        
        BigDecimal testD = new BigDecimal(test);
        BigDecimal refD = new BigDecimal(ref);

        if (testD.compareTo(refD) == 0) return true; // exact match

        int scale = findNDigitsAfterDot(test); // round ES to found EG precision
        BigDecimal rounded = refD.setScale(scale, RoundingMode.HALF_UP);
        return rounded.compareTo(testD) == 0;
    }    
    
    @SuppressWarnings("unused")
    public static boolean isNumeric(String str){
          try  
          {  
            double d = Double.parseDouble(str);  
          }  
          catch(NumberFormatException e)  
          {  
            return false;  
          }catch(Exception e) {
              return false;
          }
          return true;  
    }
    
    public static boolean isNumericUnc(String unc){
        String str=unc.trim();
        
        if(isNumeric(str))
            return true;
        
        if(str.indexOf('+')>=0 && str.indexOf('-')>=0){
            String[] s=str.split("[+-]");
            
            if(s.length==3 && s[0].length()==0 && isNumeric(s[1]) && isNumeric(s[2]))
                return true;
        }
        
        return false; 
    }
    
    public static boolean isEmbracedByBrackets(String str,String leftBracket,String rightBracket) {
        str=str.trim();
        if(!str.startsWith(leftBracket) || !str.endsWith(rightBracket))
            return false;
        
        return true;
    }
    
    public static boolean isEmbracedByBrackets(String str) {
        return isEmbracedByBrackets(str,"(",")");
    }
    
    public static boolean isInteger(String str){
          try  
          {  
        	  if(str.startsWith("+"))
        		  str=str.substring(1);
        	  
        	  int d = Integer.parseInt(str);           
        	  if(str.equals(Integer.toString(d)))            
        		  return true;
          }  
          catch(NumberFormatException e)  
          {  
            return false;  
          }  
          return false;  
  }
    /*
     * check if str is an half-integer spin in format like 3/2.
     * "1.5" is not a valid half-inter spin
     */
    public static boolean isHalfIntegerSpin(String str){
        try  
        {  
          str=str.trim();
          int n=str.indexOf("/");
          String s1=str.substring(0,n);
          String s2=str.substring(n+1);
          int d1=Integer.parseInt(s1);
          int d2=Integer.parseInt(s2);
          
          if(d2==2 && d1%2==1 && d1>0)
              return true;
        }  
        catch(Exception e){  
        }  
        return false;  

    }
    
    /*
     *  Helper method to test if content is HTML
     */
    public static boolean isHtmlContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        // Check for any HTML tag pattern: <word> or </word>
        return text.matches("(?s).*</?\\w+\\s*[^>]*>.*");
    }
    
    public static boolean isIntegerSpin(String str){
        try  
        {  
            int d=Integer.parseInt(str);
            if(str.equals(Integer.toString(d)) && d>=0)
                return true;
        }  
        catch(Exception e){  
        }  
        return false;  

    }
    
    public static boolean isDigits(String s) {
        try {
            if(s.isEmpty())
                return false;
            
            for(int i=0;i<s.length();i++) {
                if(!Character.isDigit(s.charAt(i)))
                    return false;
            }
            return true;
        }catch(Exception e) {
        }
        
        return false;
    }
    
    public static boolean isSameNumber(String ns1,String ns2) {
    	try {
    		String s1=ns1.trim().toUpperCase();
    		String s2=ns2.trim().toUpperCase();
    		if(s1.isEmpty() || s2.isEmpty())
    			return false;
    		
    		if(s1.equals(s2))
    			return true;
    		
    		if(s1.contains("E"))
    			s1=Str.removeExpFormat(s1);
    		if(s2.contains("E"))
    			s2=Str.removeExpFormat(s1);
    		
    		String s="";
			if(s1.startsWith(s2)) {
				s=s1.substring(s2.length());
			}else if(s2.startsWith(s1)) {
				s=s2.substring(s1.length());
			}else {
				return false;
			}
    		
			if(s.startsWith("."))
				s=s.substring(1);
			
			if(s.replace("0","").isEmpty())
				return true;
    		
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean isEqualValue(String s1,String s2,double precision) {
        try {
            if(s1.equals(s2))
                return true;
            
            double d1=Double.parseDouble(s1);
            double d2=Double.parseDouble(s2);
            double d1a=Math.abs(d1);
            double d2a=Math.abs(d2);
            
            double diff=Math.abs(d1-d2);
            if(diff<precision && diff<precision*Math.min(d1a,d2a)) {
                //if(s1.charAt(0)==s2.charAt(0))
                    return true;
            }
            
        }catch(Exception e) {}
        
        return false;
    }
    
    public static boolean isLetter(char c) {return Character.isLetter(c);}
    public static boolean isDigit(char c) {return Character.isDigit(c);}
    public static boolean isLetterOrDigit(char c) {return Character.isLetterOrDigit(c);}
    
    public static boolean isLetters(String s) {
        try {
            if(s.isEmpty())
                return false;
            
            for(int i=0;i<s.length();i++) {
                if(!Character.isLetter(s.charAt(i)))
                    return false;
            }
            return true;
        }catch(Exception e) {
        }
        
        return false;
    }
    
    public static boolean isLettersOrDigits(String s) {
        try {
            if(s.isEmpty())
                return false;
            
            for(int i=0;i<s.length();i++) {
                if(!Character.isLetterOrDigit(s.charAt(i)))
                    return false;
            }
            return true;
        }catch(Exception e) {
        }
        
        return false;
    }
    public static float parseFloatEnergy(String str){
        float en=0;
        int pos=-1;
        String valStr="";
        
        str=str.trim();
        
        try{
            en=Float.parseFloat(str);
        }catch(NumberFormatException e){
            if(str.equals("SN+X"))//for a fake last level in decay dataset with IB=100-sum(all reported IB) when sum(all reported IB)<100 
                return 100000;
            else if((pos=str.toUpperCase().indexOf("SN"))>=0 || (pos=str.toUpperCase().indexOf("SP"))>=0 || (pos=str.toUpperCase().indexOf("SA"))>=0){
                if(pos==0)
                    valStr=str.substring(2);
                else
                    valStr=str.substring(0, pos);
                
                if(isNumeric(valStr))
                    return Float.parseFloat(valStr);
            }
            else
                return 0;
        }
        
        return en;
    }
    
    /*capitalize the first letters of each word in a string**/
    public static String capFirstLetters_old(String str){
        
        ArrayList<String> exceptions=new ArrayList<String>(Arrays.asList("WITH","OF","AT","BY","TO","IN","FOR","AND","A"));
        String out="";
        int i=0;
        boolean firstLetter=true;
        int nBrackets=0;
        while(i<str.length()){
            char ch=str.charAt(i);
            if(ch=='(' || ch=='{') 
                nBrackets++;
            else if(ch==')' || ch=='}') 
                nBrackets--;
            
            if(ch>='a' && ch<='z' && firstLetter && nBrackets==0){
                String temp=""+ch;
                
                if(i<str.length()-1 && Character.isLetter(str.charAt(i+1)))
                	out+=temp.toUpperCase();
                else
                	out+=temp;
                
                firstLetter=false;
            }
            else{
                out+=ch;
                
                firstLetter=false;
                if(ch>='A' && ch<='Z')
                    firstLetter=false;
                else if(ch==' ')
                    firstLetter=true;
            }  
            i++;
        }
        
        if(out.length()<=0)
            return "";
        
        String temp="";
        int index=-1;
        for(String s:exceptions){
            temp=" "+s+" ";
            index=out.toUpperCase().indexOf(temp);
            if(index>0)
                out=out.substring(0, index)+out.substring(index, index+temp.length()).toLowerCase()+out.substring(index+temp.length());
        }
        return out;
    }
    
    /*capitalize the first letters of each word with len>1 in a string**/
    public static String capFirstLetters(String str){
        
        ArrayList<String> exceptions=new ArrayList<String>(Arrays.asList("WITH","OF","AT","BY","TO","IN","FOR","AND"));
        String out="";
        int i=0;
        int nBrackets=0;
        String word="";
        boolean goodWord=true;
        while(i<str.length()){
            char ch=str.charAt(i);
            if(ch=='(' || ch=='{') 
                nBrackets++;
            else if(ch==')' || ch=='}') 
                nBrackets--;
            
            word+=ch;
            if(!Character.isLetter(ch)) {  
            	if(nBrackets!=0)
            		goodWord=false;
            	else if(ch!=' ') {
            		//if(ch!='-' || word.startsWith("-"))
            			goodWord=false;
            	}else {
            		if(word.trim().length()>1 && goodWord) {            	
            			String temp=word.toUpperCase().trim();
            			if(!exceptions.contains(temp)) 
            				word=temp.charAt(0)+word.substring(1);
        			}
            			
                	out+=word;           	
            		word="";
            		goodWord=true;	
            	}
            }
            i++;
        }
 
        //for the last word
		if(word.trim().length()>1 && goodWord) {            	
			String temp=word.toUpperCase().trim();
			if(!exceptions.contains(temp)) 
				word=temp.charAt(0)+word.substring(1);
		}
		out+=word; 
		
        return out;
    }
    
    /*fill the space(len>2) with invisible letter**/
    public static String fillLongSpace(String str){
        String out="";
        String[] splits=str.split("\\s\\s");//split by two whitespaces

        //debug
        //System.out.println("In Str line 493: str="+str+" splits.len="+splits.length);
        
        int n=splits.length;
        if(str.length()==0 || n<=1)
            return str;
        
        int nspaces=0;
        String temp="";
        for(int i=0;i<n;i++){
            if(splits[i].length()>0){
                if(nspaces==0)
                    out+=splits[i];
                else{
                    temp="";
                    for(int j=0;j<nspaces-1;j++)
                        temp+="a";
                    
                    out+="\\hphantom{"+temp+"}"+splits[i];
                    nspaces=0;
                }
            }
            nspaces+=2;
                    
        }

        return out;
    }
    
    public static String ltrim(String s) {
    
        if(s==null)
            return null;
                    
        return s.replaceAll("^\\s+","");
        /*
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) 
            i++;
        
        return s.substring(i);
        */
    }

        
    public static String rtrim(String s) {
        if(s==null)
            return null;
       
        s=removeEOL(s);
        
        return s.replaceAll("\\s+$","");
        /*
        int i = s.length() - 1;
        while (i > 0 && Character.isWhitespace(s.charAt(i)))        
            i--;
        
        return s.substring(0, i + 1);
        */
    }
    
    
    public static String repeat(String s,int n){
        String out="";
        try {
            for(int i=0;i<n;i++)
                out+=s;
        }catch(Exception e) {
            return null;
        }
    
        return out;
    }
    
    public static Vector<String> splitString(String s,String pattern){
        String str=s.substring(0);
        
        Vector<String> out=new Vector<String>();
        if(pattern.length()>=str.length()||pattern.length()==0){
            out.add(str);
            return out;
        }
    
        int index=-1;
        String temp;
        while(str.length()>0){
            index=str.indexOf(pattern);
            if(index>=0){
                if(index>0)
                    temp=str.substring(0,index);
                else{
                    temp=" ";//element is NULL
                }
                out.add(temp);
                
                if(str.equals(pattern)||index==str.length()-1){//the last element is NULL
                    out.add(" ");
                    break;
                }
                str=str.substring(index+pattern.length());
            }
            else{
                out.add(str);
                break;
            }               
        }
        
        return out;
    }
    
    public static String[] splitOutsideBrackets(String str, String delimiters) {
    	return splitOutsideBrackets(str,delimiters,false);
    }
    
    public static String[] splitOutsideBrackets(String str, String delimiters, boolean ignoreQuotes) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int bracketDepth = 0;
        boolean inQuotes = false;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            // Handle quotes if enabled
            if (ignoreQuotes && (c == '"' || c == '\'')) {
                inQuotes = !inQuotes;
                current.append(c);
                continue;
            }
            
            // Track bracket depth (only when not in quotes)
            if (!inQuotes) {
                if (c == '(' || c == '[' || c == '{') {
                    bracketDepth++;
                } else if (c == ')' || c == ']' || c == '}') {
                    bracketDepth--;
                }
            }
            
            // Split if character is a delimiter, not inside brackets, and not in quotes
            if (bracketDepth == 0 && !inQuotes && delimiters.indexOf(c) >= 0) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        // Add the last segment
        result.add(current.toString().trim());
        
        return result.toArray(new String[0]);
    }
    
    /*split a sentence by ",", ";", "$", "." (not decimal point)*/
    public static String[] splitSentence(String line) {
        String[] out=new String[0];
        while(line.contains("  ")) {
            line=line.replace("  "," ");
        }

        int nb=0;
        String tempS="";
        char c=0,c1=0;
        for(int i=0;i<line.length();i++) {
            c=line.charAt(i);
            if(c=='('||c=='[')
                nb++;
            else if(c==')'||c==']')
                nb--;
            else if(c==','&&nb==0)
                c=';';
            
            tempS+=c;
        }
        line=tempS;
        
        int n=-1;
        
        tempS="";
        while((n=line.indexOf("."))>0) {
            try {
                //System.out.println(" n="+n+" s="+s+" tempStr="+tempStr);
                
                
                c=line.charAt(n-1);
                c1=line.charAt(n+1);    
                if(!Character.isDigit(c) || !Character.isDigit(c1)) 
                    tempS+=line.substring(0,n)+";";
                else
                    tempS+=line.substring(0,n+1);
                
                line=line.substring(n+1);
            }catch(Exception e) {
                //e.printStackTrace();
                break;
            }
        }
       
        
        line=tempS+line;
        line=line.replace(";;", ";").replace("$$", "$").trim();
        
        out=line.split("[$;]+");
                
        return out;
    }

    /* wrap of String.split(delims)
     * but when "." in delims, not split at decimal point
     *     for other delim, not split if it is inside brackets, () or [] or {}
     */
    public static String[] specialSplitAllDelims(String line,String delims) {
        String[] out=new String[0];

        if(line.contains("(") || line.contains("[") || line.contains("{") || delims.contains(".")) {
            int nb=0;
            String tempS="";
            String marker="";
            char c=0,c1=0;
            
            int n=0;
            
            String letters="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

            HashMap<String,String> delimMarkerMap=new HashMap<String,String>();
            
            tempS=letters;
            String delims0=delims;
            
            if(delims.startsWith("["))
            	delims=delims.substring(1).trim();
            if(delims.endsWith("+"))
            	delims=delims.substring(0,delims.length()-1).trim();
            if(delims.endsWith("]"))
            	delims=delims.substring(0,delims.length()-1).trim();
            
            String tempDelims=delims;
            for(int i=0;i<delims.length();i++) {
            	String delim=delims.charAt(i)+"";
            	 if(delimMarkerMap.containsKey(delim))
            		 continue;
            	
            	 //escaped character
            	 if(delim.equals("\\")&&i<delims.length()-1) {
            		 char nc=delims.charAt(i+1);
            		 if(nc=='s') {
            			 delim=" ";
            			 tempDelims=tempDelims.replace("\\s", " ");
            			 i++;
            		 }else {
            			 delim="\\"+nc;
            			 tempDelims=tempDelims.replace(delim,"");
            			 i++;
            		 }
            	 }

                int count=0;
                marker="";
                n=0;
                while(n<letters.length() && count<3) {
                    c=letters.charAt(n);
          
                    if(!delims.contains(c+"")) {
                        marker+=Str.repeat(c+"",5);
                        tempS=tempS.replace(c+"","");
                        count++;
                    }

                    
                    n++;         
                }
               
                //System.out.println("delims="+delims+" delim="+delim+" marker="+marker); 
                
                delimMarkerMap.put(delim, marker);
                letters=tempS;
            }
            delims=tempDelims;
            
            //System.out.println("delims="+delims+"  letters="+letters+"  "+marker1+"  "+marker2);
            
            tempS="";
            for(int i=0;i<line.length();i++) {
                c=line.charAt(i);
                if(c=='('||c=='['||c=='{')
                    nb++;
                else if(c==')'||c==']'||c=='}')
                    nb--;
                else if(delims.indexOf(c)>=0&&nb!=0) {
                    //System.out.println(" c="+c+"$ marker="+delimMarkerMap.get(c+""));
                    
                    tempS+=delimMarkerMap.get(c+"");
                    continue;
                }
                
                tempS+=c;
            }
            line=tempS;
     
            n=-1;
            tempS="";
            if(delims.contains(".")) {
                while((n=line.indexOf("."))>0) {
                    try {
                        //System.out.println(" n="+n+" s="+s+" tempStr="+tempStr);
                        
                        
                        c=line.charAt(n-1);
                        c1=line.charAt(n+1);    
                        if(Character.isDigit(c) && Character.isDigit(c1)) 
                            tempS+=line.substring(0,n)+delimMarkerMap.get(".");
                        else
                            tempS+=line.substring(0,n+1);
                        
                        line=line.substring(n+1);
                    }catch(Exception e) {
                        //e.printStackTrace();
                        break;
                    }
                }	
            }

           
            
            line=tempS+line;
           
            String[] temp=line.split(delims0);
            out=new String[temp.length];
            for(int i=0;i<temp.length;i++) {
                String s=temp[i];
                for(String delim:delimMarkerMap.keySet())
                	s=s.replace(delimMarkerMap.get(delim),delim);

                out[i]=s;
            } 
           
        }else
            out=line.split(delims);

        return out;
        
    }
    
    /* wrap of String.split(delims)
     * but when "." in delims, not split at decimal point
     *     when "," in delims, not split at "," if it is inside brackets, () or [] or {}
     */
    public static String[] specialSplit(String line,String delims) {
        String[] out=new String[0];

        if(delims.contains(".") || delims.contains(",")) {
            int nb=0;
            String tempS="";
            String marker1="";
            String marker2="";
            char c=0,c1=0;
            
            int n=0;
            
            String letters="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

            tempS=letters;
            int count=0;
            while(n<letters.length() && count<=3) {
                c=letters.charAt(n);
                if(!delims.contains(c+"")) {
                    marker1+=Str.repeat(c+"",5);
                    tempS=tempS.replace(c+"","");
                    count++;
                }

                
                n++;         
            }
            letters=tempS;
            
            count=0;
            n=0;
            while(n<letters.length() && count<=3) {
                c=letters.charAt(n);
                if(!delims.contains(c+"")) {
                    marker2+=Str.repeat(c+"",5);
                    count++;
                }
                
                n++;
            }
            
            //System.out.println("delims="+delims+"  letters="+letters+"  "+marker1+"  "+marker2);
            
            tempS="";
            for(int i=0;i<line.length();i++) {
                c=line.charAt(i);
                if(c=='('||c=='['||c=='{')
                    nb++;
                else if(c==')'||c==']'||c=='}')
                    nb--;
                else if(c==','&&nb!=0) {
                    tempS+=marker1;
                    continue;
                }
                
                tempS+=c;
            }
            line=tempS;
     
            n=-1;
            tempS="";
            while((n=line.indexOf("."))>0) {
                try {
                    //System.out.println(" n="+n+" s="+s+" tempStr="+tempStr);
                    
                    
                    c=line.charAt(n-1);
                    c1=line.charAt(n+1);    
                    if(Character.isDigit(c) && Character.isDigit(c1)) 
                        tempS+=line.substring(0,n)+marker2;
                    else
                        tempS+=line.substring(0,n+1);
                    
                    line=line.substring(n+1);
                }catch(Exception e) {
                    //e.printStackTrace();
                    break;
                }
            }
           
            
            line=tempS+line;
           
            String[] temp=line.split(delims);
            out=new String[temp.length];
            if(marker1.length()>0 && marker2.length()>0) {
                for(int i=0;i<temp.length;i++) {
                    String s=temp[i].replace(marker1, ",").replace(marker2, ".").trim();
                    out[i]=s;
                }            
            }            
        }else
            out=line.split(delims);

        return out;
        
    }
    public static int countPattern(String s,String pattern){
        int count=0;
        String str=s.substring(0);
        if(pattern.length()==0 || str.length()<pattern.length())
            return 0;
        
        int index=-1;
        while(str.length()>0){
            index=str.indexOf(pattern);
            if(index<0)
                break;
            
            count++;
            str=str.substring(index+pattern.length());
        }
        
        return count;
    }
    
    /**count the non-printable characters (ENSDF format) in a ENSDF-formated string
     * adapted from NONPRT function in NDS-PUB fortran code
     * NOTE: this function only applies to a string in ENSDF format 
     */
    public static int countNonPrintableENSDF(String str,int end){
        int count=0;
        char c;
        int n=end;
        
        if(str==null)
            return 0;
        
        if(n>str.length())
            n=str.length();
        
        for(int i=0;i<n;i++){
            c=str.charAt(i);
            if(c=='|' || c=='~' || c=='}' || c=='^')
                count+=1;
            else if(c=='{'){
                count+=1;
                char nc=' ';
                if(i<str.length()-1)
                    nc=str.toUpperCase().charAt(i+1);
                
                String s="+-BIU";
                if(s.indexOf(nc)>=0)
                    count+=1;

            }
            else if(c=='\\')
                count+=2;
            else if(c=='W'||c=='M' || c=='m' ||c=='w')//'W','M' are wider than other letters, assumed to occupy two spaces
                count-=1;
        }
             
        return count;
    }
    
  
    public static int countNonPrintableENSDF(String str){
        return countNonPrintableENSDF(str,str.length());
        
    }
    /**count the non-printable characters (Latex format) in a Latex-formated string
     * NOTE: this function only applies to a string in Latex format 
     */
    public static int countNonPrintableLatex(String str,int end){
        return str.trim().length()-countPrintableLatex(str,end);
    }
    
    
    public static int countPrintableLatex(String str,int end){
        int count=0;
        char c;     
        int n=end;
        
        if(str==null)
            return 0;
        
        if(n>str.length())
            n=str.length();
        
        String s=str.substring(0, n);
        String s1="";
        String temp="";
        int p;
        int unmatched=0;

        String text="";
        
        for(int i=0;i<n;i++){
            c=s.charAt(i);
            if(c!='\\'){
                if(c!='~' && c!='|' && c!='}' && c!='{' && c!=' ')
                    temp+=c;
                
                continue;
            }
            
            s1=s.substring(i);
            p=s1.indexOf("\\multicolumn{");//\multicolumn{ncol}{alignment}{text}
            unmatched=0;
            count=0;
            if(p==0){
                for(int j=0;j<s1.length();j++){
                    c=s1.charAt(j);
                    if(c=='{'){
                        if(j<s1.length()-1) 
                            text=""+s1.charAt(j+1);
                        else
                            text="";
                        
                        unmatched++;
                        continue;
                    }
                    
                    if(c=='}'){
                        unmatched--;
                        if(unmatched==0){
                            count++;
                            if(count==3){
                                temp+=text.trim();
                                s1=s1.substring(j+1);
                                break;
                            }
                        }
                        continue;
                    }
                    
                    text+=c;    
                }
            }
        }

             
        return temp.length();
    }
    
    /**count the non-printable characters (ENSDF format) in a ENSDF-formated string
     * adapted from NONPRT function in NDS-PUB fortran code
     * NOTE: this function only applies to a string in ENSDF format 
     */
    public static int countNonPrintable(String str,int end){
        return countNonPrintableENSDF(str,end);
    }
    
    /**count the non-printable characters (option="ENSDF" or "Latex" format) in a formated string
     */
    public static int countNonPrintable(String str,int end,String option){
        if(option.toUpperCase().equals("ENSDF"))
            return countNonPrintableENSDF(str,end);
        else if(option.toUpperCase().equals("LATEX"))
            return countNonPrintableLatex(str,end);
        else{
            System.out.println("Warning: unknown format option in countNonPrintable()! ENSDF format is assumed!");
            return countNonPrintableENSDF(str,end);
        }
    }

    
    /**find the width of a plain text string*/
    public static float findWidth(String str,float charWidth){
        if(str==null)
            return 0;
                
        return charWidth*str.length();
        
    }
    
    /**check if String Vector sv1 contains any element in String vector sv2*/
    public static boolean containAny(Vector<String> sv1,Vector<String> sv2){
        if(sv1.size()<=0 || sv2.size()<=0)
            return false;
        
        for(int i=0;i<sv2.size();i++)
            if(sv1.contains(sv2.get(i)))
                return true;
            
        return false;

    }
    
    public static boolean containAny(ArrayList<String> list1,ArrayList<String> list2) {
        if(list1.size()<=0 || list2.size()<=0)
            return false;
        
        for(int i=0;i<list2.size();i++)
            if(list1.contains(list2.get(i)))
                return true;
            
        return false;
    }
    
    public static boolean isOverlap(Vector<String> sv1, Vector<String> sv2) {
        return containAny(sv1,sv2);
    }
    
    
    /**check if str1 contains any characters in str2*/
    public static boolean containAny(String str1,String str2){
        if(str1.length()<=0 || str2.length()<=0)
            return false;
        
        for(int i=0;i<str2.length();i++)
            if(str1.contains(""+str2.charAt(i)))
                return true;
        
        
        return false;

    }
    
    public static boolean isOverlap(String str1,String str2){
        return containAny(str1,str2);
    }
    
    /**check if str contains any string of substring of any pattern in the patterns ArrayList*/
    public static boolean containAnyPattern(String str,ArrayList<String> patterns){
        if(str.length()<=0 || patterns==null || patterns.size()<=0)
            return false;
        
        for(int i=0;i<patterns.size();i++)
            if(str.contains(patterns.get(i)))
                return true;
        
        
        return false;

    }
    
    /**check if str starts with any string of substring of any pattern in the patterns ArrayList*/
    public static boolean startWithAnyPattern(String str,ArrayList<String> patterns){
        if(str.length()<=0 || patterns==null || patterns.size()<=0)
            return false;
        
        for(int i=0;i<patterns.size();i++)
            if(str.startsWith(patterns.get(i)))
                return true;
        
        
        return false;

    }
    
    /**check if str ends with any string of substring of any pattern in the patterns ArrayList*/
    public static boolean endWithAnyPattern(String str,ArrayList<String> patterns){
        if(str.length()<=0 || patterns==null || patterns.size()<=0)
            return false;
        
        for(int i=0;i<patterns.size();i++)
            if(str.endsWith(patterns.get(i)))
                return true;
        
        
        return false;

    }
    
    /**
     * 
     * @param s1
     * @param s2
     * @return an array of 2 elements: [0]=max partial match (both end) in s1
     *                                 [1]=corresponding part in s2
     */
    public static String[] findMaxPartialMatchPair(String s1, String s2) {
    	String[] out=null;
    	try {
            if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty()) {
                return out;
            }

            // Start with the longest possible substring and work downwards
            for (int len = Math.min(s1.length(), s2.length()); len > 0; len--) {
                // Check all substrings of this length in s1
                for (int i = 0; i <= s1.length() - len; i++) {
                    String sub1 = s1.substring(i, i + len);
                    char first = sub1.charAt(0);
                    char last = sub1.charAt(len - 1);
                    
                    // Check if s2 has any substring of same length with matching ends
                    for (int j = 0; j <= s2.length() - len; j++) {
                        if (s2.charAt(j) == first && s2.charAt(j + len - 1) == last) {
                        	out=new String[2];
                        	out[0]=sub1;
                        	out[1]=s2.substring(j,j+len);
                            return out; 
                        }
                    }
                }
            }
    	}catch(Exception e) {
    		
    	}

    	return out;
    }
    
    public static int countMatchingCharsInMaxPartialMatch(String s1, String s2) {
        String[] matchPair = findMaxPartialMatchPair(s1, s2);
        if (matchPair[0].isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (int i = 0; i < matchPair[0].length(); i++) {
            if (matchPair[0].charAt(i) == matchPair[1].charAt(i)) {
                count++;
            }
        }
        return count;
    }
    
    public static String findBestStringOfNumber(double val,int nsig) {
    	String s="";
    	try {
    		String s0=String.valueOf(val);
    		
    		double absV=Math.abs(val);
    		int n=0;
    		if(absV>0)
    			n=(int)Math.log10(absV);
    		
    		if(n>=2 && n<6) {
    			s=String.valueOf(Str.round(val));
    			
    			int n1=s.replace("-","").length();
    			if(n1<nsig)
    				s=Str.roundByNsig(s0, nsig);
    		}else {
    			s=Str.roundByNsig(s0, nsig);
    		}
    	}catch(Exception e) {
    		
    	}
    	
    	return s;
    }
    
    public static int findBestNSigOfProduct(String s1,String s2) {return findBestNSigOfProduct(s1,s2,35);}
	public static int findBestNSigOfProduct(String s1,String s2,int errorLimit) {
	   	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println(n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		if(errorLimit<=0)
    			errorLimit=35;
    		
    		//[0]-base string, [1]-npower string
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		String base1=out1[0];
    		String base2=out2[0];
    		
    		//System.out.println(base1+"  "+base2);
    		
    		double x1=Double.parseDouble(base1);//[1,9.999]
    		double x2=Double.parseDouble(base2);//[1,9.999]
 
    		if(x1==0 || x2==0){//product==0
    			return Math.max(n1, n2);
    		}
    		
    		double dx1=Math.abs(x1*0.1);
    		double dx2=Math.abs(x2*0.1);
    		
    		int nd1=Str.findNDigitsAfterDot(base1);
    		int nd2=Str.findNDigitsAfterDot(base2);
    		
    		boolean noDot1=true,noDot2=true;
    		if(base1.contains(".")&&nd1>0) {
    			dx1=Math.pow(10.0, -nd1);
    			noDot1=false;
    		}
    		if(base2.contains(".")&&nd2>0) {
    			dx2=Math.pow(10.0, -nd2);
    			noDot2=false;
    		}
    		//if no "." in base1 and base2, meaning either is an integer between 1 and 9
    		//so their product should also be an integer, meaning its nsig should not exceed 2 (max product=9*9=81)   			
    		
    		double x=x1*x2;
    		
    		if(noDot1 && noDot2) {
				//int d=(int)x;
    			//return String.valueOf(d).length();
    		}
    		
    		dx1=dx1/x1;
    		dx2=dx2/x2;
    		double dx=Math.sqrt(dx1*dx1+dx2*dx2);
    		dx=Math.abs(x*dx);
    		

    		double f=Math.log10(errorLimit/dx);
    		int n=(int)Math.floor(f);//2.3 to 2, -1.2 to -2
    		
    		int d=(int)(x*Math.pow(10, n));
    		d=Math.abs(d);
    		
    		int nsig=String.valueOf(d).length();

    		return nsig;
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
	}
	
    public static String findBestStringOfProduct(String s1,String s2) {return findBestStringOfProduct(s1,s2,35);}
	public static String findBestStringOfProduct(String s1,String s2,int errorLimit) {
		try {
    		double v1=Double.parseDouble(s1);
    		double v2=Double.parseDouble(s2);
    		double v=v1*v2;
    		
			int nsig=findBestNSigOfProduct(s1,s2,errorLimit);
			if(nsig<0)
				return "";
			
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	
			s=Str.roundByNsig(s, nsig);
			return s;
			
		}catch(Exception e) {
			
		}
		
		return "";

	}
	
    public static int findBestNSigOfDivision(String s1,String s2) {return findBestNSigOfDivision(s1,s2,35);}
	public static int findBestNSigOfDivision(String s1,String s2,int errorLimit) {
	   	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println(n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		if(errorLimit<=0)
    			errorLimit=35;
    		
    		//[0]-base string, [1]-npower string
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		String base1=out1[0];
    		String base2=out2[0];
    		
    		double x1=Double.parseDouble(base1);//[1,9.999]
    		double x2=Double.parseDouble(base2);//[1,9.999]
 
            if(x2==0)
            	return -1;
            
    		if(x1==0){
    			return Math.max(n1, n2);
    		}
    		
    		double dx1=Math.abs(x1*0.1);
    		double dx2=Math.abs(x2*0.1);
    		
    		int nd1=Str.findNDigitsAfterDot(base1);
    		int nd2=Str.findNDigitsAfterDot(base2);
    		
    		if(base1.contains(".")&&nd1>0) {
    			dx1=Math.pow(10.0, -nd1);
    		}
    		if(base2.contains(".")&&nd2>0) {
    			dx2=Math.pow(10.0, -nd2);
    		}
    		
    		double x=x1/x2;
    		
    		dx1=dx1/x1;
    		dx2=dx2/x2;
    		double dx=Math.sqrt(dx1*dx1+dx2*dx2);
    		dx=Math.abs(x*dx);


    		double f=Math.log10(errorLimit/dx);
    		int n=(int)Math.floor(f);//2.3 to 2, -1.2 to -2
    		
    		//System.out.println("base1="+base1+" base2="+base2+" x1="+x1+" x2="+x2+" x="+x+" dx="+dx+" f="+f+" n="+n);
    		//System.out.println(" dx1="+dx1+" dx2="+dx2+" nd1="+nd1+" nd2="+nd2);
    		
    		int d=(int)(x*Math.pow(10, n));
    		d=Math.abs(d);
    		
    		int nsig=String.valueOf(d).length();

    		return nsig;
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
	}
	
    public static String findBestStringOfDivision(String s1,String s2) {return findBestStringOfDivision(s1,s2,35);}
	public static String findBestStringOfDivision(String s1,String s2,int errorLimit) {
		try {
    		double v1=Double.parseDouble(s1);
    		double v2=Double.parseDouble(s2);
    		double v=v1/v2;
    		
			int nsig=findBestNSigOfDivision(s1,s2,errorLimit);
			if(nsig<0)
				return "";
			
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	
			s=Str.roundByNsig(s, nsig);
			return s;
			
		}catch(Exception e) {
			
		}
		
		return "";

	}
	
	public static int findBestNSigOfAddition(String s1,String s2) {return findBestNSigOfAddition(s1,s2,35);}
	public static int findBestNSigOfAddition(String s1,String s2,int errorLimit) {
	   	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println("@@"+n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		//[0]-base string, [1]-npower string
    		//out[0]-[1,9.9999]
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		String base1=out1[0];
    		String base2=out2[0];
    		
    		double x1=Double.parseDouble(s1); 
    		double x2=Double.parseDouble(s2); 
    		
    		double dx1=Math.abs(x1*0.1);
    		double dx2=Math.abs(x2*0.1);
    		
    		int nd1=Str.findNDigitsAfterDot(base1);
    		int nd2=Str.findNDigitsAfterDot(base2);
    		
    		if(base1.contains(".")&&nd1>0) {
    			nd1=Str.findNDigitsAfterDot(s1);
    			dx1=Math.pow(10.0, -nd1);
    		}
    		if(base2.contains(".")&&nd2>0) {
    			nd2=Str.findNDigitsAfterDot(s2);
    			dx2=Math.pow(10.0, -nd2);
    		}

    		double x=x1+x2;   		
    		double dx=Math.sqrt(dx1*dx1+dx2*dx2);

    		double f=Math.log10(errorLimit/dx);
    		int n=(int)Math.floor(f);//2.3 to 2, -1.2 to -2
    		
    		int d=(int)(x*Math.pow(10, n));
    		d=Math.abs(d);
    		
    		//System.out.println("base1="+base1+" base2="+base2+" x1="+x1+" x2="+x2+" x="+x+" dx="+dx+" f="+f+" n="+n);
    		//System.out.println(" dx1="+dx1+" dx2="+dx2+" nd1="+nd1+" nd2="+nd2+" d="+d);
    		
    		int nsig=String.valueOf(d).length();

    		String s=Str.roundByNsig(String.valueOf(x), nsig);
    		n1=Str.findNDigitsAfterDot(s1);
    		n2=Str.findNDigitsAfterDot(s2);
    		
    		n1=Math.max(n1, n2);
    		
    		n=Str.findNDigitsAfterDot(s);
    		if(n>n1) {
    			s=Str.roundToNDigitsAfterDot(s, n);
    			nsig=nsig-(n-n1);
    		}
    		
    		return nsig;
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
	}
	
	public static String findBestStringOfAddition(String s1,String s2) {return findBestStringOfAddition(s1,s2,35);}
	public static String findBestStringOfAddition(String s1,String s2,int errorLimit) {
		try {
    		double v1=Double.parseDouble(s1);
    		double v2=Double.parseDouble(s2);
    		double v=v1+v2;
    		
			int nsig=findBestNSigOfAddition(s1,s2,errorLimit);
			if(nsig<0)
				return "";
			
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	
			s=Str.roundByNsig(s, nsig);

        	//System.out.println(" s1="+s1+" s2="+s2+"  s="+s+" nsig="+nsig);
        	
			return s;
			
		}catch(Exception e) {
			
		}
		
		return "";

	}
	
	public static int findBestNSigOfSubtraction(String s1,String s2) {return findBestNSigOfSubtraction(s1,s2,35);}
	public static int findBestNSigOfSubtraction(String s1,String s2,int errorLimit) {
		if(s2.startsWith("-"))
			s2=s2.substring(1);
		else
			s2="-"+s2;
		
		return findBestNSigOfAddition(s1,s2,errorLimit);
	}
	
	public static String findBestStringOfSubtraction(String s1,String s2) {return findBestStringOfSubtraction(s1,s2,35);}
	public static String findBestStringOfSubtraction(String s1,String s2,int errorLimit) {
		if(s2.startsWith("-"))
			s2=s2.substring(1);
		else
			s2="-"+s2;
		
		return findBestStringOfAddition(s1,s2,errorLimit);
	}
	
	/*
	 * each array in the return lsit:
	 *   [0]: delim
	 *   [1]: string of start position index
	 *   [2]: string of end position index
	 */
	public static List<String[]> findDuplicateDelims(String input, String delims) {
	    List<String[]> result = new ArrayList<>();
	    String delimClass = "[" + Pattern.quote(delims) + "]";
	    Pattern pattern = Pattern.compile(delimClass + "\\s*" + delimClass);
	    Matcher m = pattern.matcher(input);
	    
	    while (m.find()) {
	        String delim = m.group(0).trim().substring(0, 1);
	        result.add(new String[]{delim, String.valueOf(m.start()), String.valueOf(m.end() - 1)});
	    }
	    return result;
	}
	
    /*find number of significant digits in the exact number 
     *string of the product of s1 and s2
     *Note that this could an over-counted number for some cases
     *for example, 9.9*1.1
     *if we assume uncertainty according to precision, they 
     *would be 9.9(1)*1.1(1), the exact value is 10.89 and the 
     *uncertainty is about 1.0, so the result should be 10.9(10)  
     */
    public static int findNSigOfExactProduct(String s1,String s2){
    	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println(n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		//[0]-base string, [1]-npower string
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		
    		double v1=Double.parseDouble(out1[0]);//[1,9.999]
    		double v2=Double.parseDouble(out2[0]);//[1,9.999]
    		double v=v1*v2;
    		double absV=Math.abs(v);
    		
    		int npow=(int)Math.log10(absV);//>=0
    		    		
    		//nsig increases by npow for integer part of the result
    		//nsig increases by Math.min(n1-1,n2-1) for the float part,
    		//     with n1-1 and n2-1 the numbers of digits after decimal point, 
    		int nsig=Math.max(n1, n2)+npow+Math.min(n1-1, n2-1);
    		
    		return nsig;
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
    	
    }
    
    public static String findExactProduct(String s1,String s2){
    	try {
    		double v1=Double.parseDouble(s1);//[1,9.999]
    		double v2=Double.parseDouble(s2);//[1,9.999]
    		double v=v1*v2;
    		
        	int nsig=findNSigOfExactProduct(s1,s2);
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	//System.out.println(v+"  #"+String.valueOf(v)+" s="+s);
        	
        	s=Str.roundByNsig(s, nsig);
        	
        	return s;
    	}catch(Exception e) {
    		
    	}
    	
    	return "";

    }
    
    public static int findNSigOfExactDivision(String s1,String s2){
    	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println(n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		//[0]-base string, [1]-npower string
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		
    		double v1=Double.parseDouble(out1[0]);//[1,9.999]
    		double v2=Double.parseDouble(out2[0]);//[1,9.999]
    		if(v2==0)
    			return -1;
    		
    		double v=v1/v2;
    		double absV=Math.abs(v);
    		
    		int npow=Math.abs((int)Math.log10(absV));//>=0
    		    		
    		//nsig increases by npow for integer part of the result
    		//nsig increases by Math.min(n1-1,n2-1) for the float part,
    		//     with n1-1 and n2-1 the numbers of digits after decimal point, 
    		int nsig=Math.max(n1, n2)+npow+Math.min(n1-1, n2-1);
    		
    		return nsig;
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
    	
    }
    
    public static String findExactDivision(String s1,String s2){
    	try {
    		double v1=Double.parseDouble(s1);//[1,9.999]
    		double v2=Double.parseDouble(s2);//[1,9.999]
    		
    		if(v2==0)
    			return "";
    		
    		double v=v1/v2;
    		
        	int nsig=findNSigOfExactProduct(s1,s2);
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	//System.out.println(v+"  #"+String.valueOf(v)+" s="+s);
        	
        	s=Str.roundByNsig(s, nsig);
        	
        	return s;
    	}catch(Exception e) {
    		
    	}
    	
    	return "";

    }
    
    /*find number of significant digits in a value string*/
    public static int findNSig(String valStr){
        String s=valStr.toUpperCase().trim();
        if(!isNumeric(s))
            return 0;
        
        int n=s.indexOf("E");
        if(n==0)
            return 0;
        
        if(n>0)
            s=s.substring(0,n);
        
        s=s.replace("+", "").replace("-","");
        
        n=s.indexOf(".");
        if(n<0)
            n=s.length();
        
        String is=s.substring(0, n).trim();
        String fs=s.substring(n).trim();
        
        //System.out.println(" in findNsig1: valStr="+valStr+" is="+is+"  fs="+fs+"   nsig="+fs.length());
        
        int offset=0;
        
        if(is.startsWith("0")&&is.length()>1) {
            for(n=0;n<is.length();n++){
                if(is.charAt(n)!='0')
                    break;      
                offset++;
            }
            
            //System.out.println(offset+"  "+n+" fs="+fs);
            
            if(offset==n) {
            	//is=0000
            	if(fs.length()>0) {
            		//000.xxx
            		is="";
            	}else {
            		return 1;
            	}
            }else
            	is=is.substring(n).trim();
        }

        //System.out.println(valStr+"###"+is);
        
        if(is.length()>0)
            return s.replace(".", "").length()-offset;
        
        fs=fs.replace(".", "").trim();
        if(fs.length()==0)
            return 0;
        
        for(n=0;n<fs.length();n++){
            if(fs.charAt(n)!='0')
                break;      

        }
        
        fs=fs.substring(n).trim();
        
        if(fs.isEmpty())
        	return 1;
        
        //System.out.println(" in findNsig2: n="+n+" valStr="+valStr+" is="+is+"  fs="+fs+"   nsig="+fs.length());
        
        return fs.length();

    }
    
    /*find number of digits after decimal point in a value string
     *if string is in scientific format like 1.2E-3, add (-power)
     *into the total number of digits 
    */
    public static int findNDigitsAfterDot(String valStr){
        String s=valStr.toUpperCase().trim();
        if(!isNumeric(s))
            return 0;
        
        int n=s.indexOf("E");
        int npower=0;
        if(n==0)
            return 0;
        
        if(n>0) {
            String s0=s;
            s=s0.substring(0,n);
            String powerS=s0.substring(n+1).trim();
            if(isInteger(powerS))
                npower=Integer.parseInt(powerS);
            else
                return 0;
        }
        
        s=s.replace("+", "").replace("-","");
        
        n=s.indexOf(".");
        if(n<0) {
            n=-npower;
        }else {
            String fs=s.substring(n+1).trim();   
            n=fs.length()-npower;
        }

        if(n<0)
        	n=0;
        
        return n;

    }
    
	public static int findSentenceEnd(String line, int from) {
	    int end = line.length();
	    int p1=-1,p2=-1;

	    int i=from;
	    while( (p1=line.indexOf('.', i))>0) {
	    	if(p1==end-1)
	    		break;
	    	if(!Character.isDigit(line.charAt(p1+1)))
	    		break;

	    	i=p1+1;
	    }

	    p2 = line.indexOf(';', from);
	    if (p2 >= 0 && p2 < end) { 
	    	if(p1<0 || p2<=p1)
	    		return p2;
	    	else
	    		return p1;
	    }
	    
	    p2 = line.indexOf(':', from);
	    if (p2 >= 0 && p2 < end) { 
	    	if(p1<0 || p2<=p1)
	    		return p2;
	    	else
	    		return p1;
	    }


	    if(p1>=0)
	    	return p1;
	    
	    return end;
	}
	
    /*
     * find the strings of the base value and power of s after converting it to scientific format
     */
    public static String[] findSciBaseNPower(String s) {
    	try {
    		
    		String s1=Str.toSciFormat(s,false);//removeEndZeros=false

    		//System.out.println(" s="+s+" s1="+s1);
    		
    		int n=s1.indexOf("E");
    		if(n>0) {
    			String baseS=s1.substring(0,n);
    			String powS=s1.substring(n+1);
    			
    			String[] out=new String[2];
    			out[0]=baseS;
    			out[1]=powS;
    			
    			return out;
    		}
    	}catch(Exception e) {
    		
    	}
    	
    	return new String[0];
    }
     
    /*
     * find the power of s after converting it to scientific format
     */
    public static int findNPower(String s) {
    	try {
    		double d=Double.parseDouble(s);
    		String s1=String.format("%E",d).toUpperCase();
    		int n=s1.indexOf("E");
    		if(n>0) {
    			s1=s1.substring(n+1);
    			n=Integer.parseInt(s1);
    			return n;
    		}
    	}catch(Exception e) {
    		
    	}
    	
    	return 0;
    }
    
    /*
     * find the base of s after converting it to scientific format
     */
    public static double findExpBase(String s) {
    	try {
    		double x=Double.parseDouble(s);
            String s1=String.valueOf(x);
            int n=findNPower(s1);
            return x/Math.pow(10, n);
    	}catch(Exception e) {
    		
    	}

    	return 0;
    }
    
    public static String findFirstNumberText(String s) {
    	Pattern NUM_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");
        if (s == null) return null;
        Matcher m = NUM_PATTERN.matcher(s);
        return m.find() ? m.group() : null;
    }
    
    public static String maxString(Vector<String> strings){
        String max="";
        
        for(int i=0;i<strings.size();i++){
            String str=strings.get(i).trim();
            if(str.length()>max.length())
                max=str.trim();
        }
        
        return max;
    }
    
    /*
     * return an array of baseS + powerS if input valS is in exponential format
     * otherwise return an empty array
     */
    public static String[] parseExpValue(String valS) {
    	try {
            String str=valS.trim().toUpperCase();
            String[] out=new String[0];
            
            if(!str.contains("E") || !isNumeric(str))
                return out;     

            int p=str.indexOf("E");           
            if(p<=0 || p==str.length()-1)
                return out;
            
            
            String baseS=str.substring(0, p).trim();
            String powerS=str.substring(p+1).trim();      
            
            out=new String[2];
            out[0]=baseS;
            out[1]=powerS;
            
            return out;
    	}catch(Exception e) {
    		
    	}
    	return new String[0];
    }
    
    public static int getNPowerOfExpo(String value) {
    	try {
        	String[] temp=parseExpValue(value);
        	if(temp.length==0)
        		return 0;
        	
        	return Integer.parseInt(temp[1]); 
    	}catch(Exception e) {
    		
    	}
  	
    	return 0;
    }
    
    public static String getBaseOfExpo(String value) {
    	try {
        	String[] temp=parseExpValue(value);
        	if(temp.length==0)
        		return "";
        	
        	return temp[0]; 
    	}catch(Exception e) {
    		
    	}
  	
    	return "";
    }
    
    /** convert a value string in exponential to a string of regular value
     *  with the same significant digits
     */
    public static String deformatExpo(String value){
        String str=value.trim();
        String out=str;
        
        if(!isNumeric(str))
            return str;     

        int p=str.indexOf("E");
        if(p<0)
            p=str.indexOf("e");
        
        if(p<=0 || p==str.length()-1)
            return out;
        
        
        String baseStr=str.substring(0, p);
        String expoStr=str.substring(p+1);      
        
        String sign="";
        if(baseStr.startsWith("+")||baseStr.startsWith("-")) {
        	sign=baseStr.charAt(0)+"";
        	baseStr=baseStr.substring(1);
        }
        
        int expo=Integer.parseInt(expoStr);
        
        int ndigits_after_decimal=0;
        int decimal_pos=baseStr.indexOf('.');
        if(decimal_pos>=0)
        	ndigits_after_decimal=baseStr.length()-decimal_pos-1;
        
        out=baseStr.replace(".", "");
        if(expo>=0){//dot shifted to right if existing, add tailing "0" if going beyond the end
            int n=expo-ndigits_after_decimal;
            if(n>=0){//reach or go beyond the end, adding tailing "0"               
                for(int i=0;i<n;i++)
                    out+='0';
            }else{//more decimal digits after dot than Nexpo, dot is shifted to right and still remains 
                decimal_pos=out.length()+n;
                out=out.substring(0,decimal_pos)+'.'+out.substring(decimal_pos);
            }
        }else{//dot shifted to left and add heading "0" if going beyond the beginning
            expo=-expo;
            int n=expo;//number of shifts to left
            if(decimal_pos>=0)//here decimal_pos=number of digits before decimal point
            	n-=decimal_pos;//get the extra "0" to be added after new "." 
            
            if(n>=0){//dot shifted beyond the beginning
                for(int i=0;i<n;i++)
                    out='0'+out;
                
                out="0."+out;
            }else{//dot shifted to left and still inside the base number string
                decimal_pos=-n;
                out=out.substring(0,decimal_pos)+'.'+out.substring(decimal_pos);
            }
        }
        
        out=sign+out;
        
        return out;
    }

    //abs(val)<=expFormatLimitSmallNumber or abs(val)>=expFormatLimitLargeNumber is 
    //printed in scientific format, otherwise it is printed in real-format
    //with number=nsig of significant digits ONLY for val with integer part
    //having number of digits<nisg. Minimum value of nsig is 2
    public static String printNumberInGoodFormat(double val, double expFormatLimitSmallNumber, double expFormatLimitLargeNumber, int nsig) {
    	try {
    		if(expFormatLimitSmallNumber<=0 || expFormatLimitLargeNumber<=expFormatLimitSmallNumber)
    			return "";
    		
    		
    		if(nsig<2)
    			nsig=2;
    		
    		if(expFormatLimitSmallNumber>=0.01)
    			expFormatLimitSmallNumber=0.01;
    		
    		if(expFormatLimitLargeNumber<=1000)
    			expFormatLimitLargeNumber=1000;
    		
    		String s=String.valueOf(val);
    		
    		double absVal=Math.abs(val);
    		
    		//System.out.println(absVal+" "+s+" "+expFormatLimitSmallNumber);
    		
    		if(absVal<=expFormatLimitSmallNumber || absVal>=expFormatLimitLargeNumber) {
    			s=Str.toSciFormat(s, nsig);
    		}else{
    			int d=(int)Str.round(val);
    			String ds=String.valueOf(Math.abs(d));
    			
    			//System.out.println(val+" d="+d+" ds="+ds);
    			
    			if(ds.length()>=nsig) {
    				s=d+"";
    			}else {
    				s=Str.roundByNsig(s, nsig);
    			}
    			
    		}
    		
    		return s;
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    public static ArrayList<String> str2list(String listStr){
        ArrayList<String> list=new ArrayList<String>();
        String[] temp=listStr.split("[,;\\ ]+");
        for(int i=0;i<temp.length;i++) {
            //System.out.println("$$$$$$$$$$$$$$$$ str="+listStr+" i="+i+"  temp="+temp[i]);
            list.add(temp[i]);
        }
        return list;
    }
    
    public static boolean containGivenSupScriptOnly(String str,ArrayList<String> list){
        return containGivenScriptOnly(str,list,'^');
    }
    public static boolean containGivenSupScriptOnly(String str,String listStr){
        return containGivenScriptOnly(str,str2list(listStr),'^');
    }
    
    public static boolean containGivenSubScriptOnly(String str,ArrayList<String> list){
        return containGivenScriptOnly(str,list,'_');
    }
    public static boolean containGivenSubScriptOnly(String str,String listStr){
        return containGivenScriptOnly(str,str2list(listStr),'_');
    }
    
    public static boolean containSupScript(String str,ArrayList<String> list){
        return containScript(str,list,'^');
    }
    public static boolean containSupScript(String str,String listStr){
        return containScript(str,str2list(listStr),'^');
    }
    
    public static boolean containSubScript(String str,ArrayList<String> list){
        return containScript(str,list,'_');
    }
    public static boolean containSubScript(String str,String listStr){
        return containScript(str,str2list(listStr),'_');
    }
    
    private static boolean containGivenScriptOnly(String str,ArrayList<String> list,char delim){
        if(!containSupScript(str))
            return false;
        
        String temp=str.substring(0);
        String script="";
        boolean hasScript=false;
        
        while(temp.length()>0){
            script=findScript(temp,delim);
            
            //System.out.println("s="+str+"  temp="+temp+" script="+script);
            
            if(script.length()<=0)
                break;
            else{
                String temp1="";
                temp1=script.replace("{","").trim();
                temp1=temp1.replace("}","").trim();
                temp1=temp1.replace("$","").trim();
                temp1=temp1.replace(""+delim,"").trim();            
                
                for(String s:list) {
                    if(temp1.contains(s))
                        hasScript=true;
                    
                    temp1=temp1.replace(s,"").trim();
                }
                
                if(temp1.length()>0)
                    return false;
                    
                int n=temp.indexOf(script)+script.length();
                temp=temp.substring(n);
                continue;
            }
                        
        }
       
        return hasScript;
    }
    
    private static boolean containScript(String str,ArrayList<String> list,char delim){
        if(!containSupScript(str))
            return false;
        
        String temp=str.substring(0);
        String script="";
        
        temp=str.substring(0);
        script="";
        
        do{
            script=findScript(temp,delim).trim();
            if(script.length()<=0)
                break;
            
            for(String s:list)
                if(script.contains(s)){
                    //System.out.println("    symbol="+s+" script="+script);
                    return true;
                }
            
            int n=temp.indexOf(script)+script.length();
            temp=temp.substring(n);
            
        }while(temp.length()>0);
                
        return false;
    }
    public static boolean containSupScript(String str){
        return containScript(str,'^');
    }
    public static boolean containSubScript(String str){
        return containScript(str,'_');
    }
    /*return ture if the input string contains sup- or sub-script*/
    private static boolean containScript(String str,char delim){
        //if(str.contains("^{")||str.contains("_{"))
        String temp=str.substring(0);
        String script="";
        
        //ArrayList<String> list=new ArrayList<String>(Arrays.asList("x","a","c","e","m","n","o","r","s","u","v","w","z"));
        ArrayList<String> list=new ArrayList<String>(Arrays.asList(""));
    
        while(temp.length()>0){
            script=findScript(temp,delim);
            
            //System.out.println("s="+str+"   script="+script);
            
            if(script.length()<=0)
                return false;
            else{
                String temp1="";
                temp1=script.replace("{","").trim();
                temp1=temp1.replace("}","").trim();
                temp1=temp1.replace("$","").trim();
                temp1=temp1.replace(""+delim,"").trim();            
                
                for(String s:list)
                    temp1=temp1.replace(s,"").trim();
                
                if(temp1.length()>0)
                    return true;
                    
                int n=temp.indexOf(script)+script.length();
                temp=temp.substring(n);
                continue;
            }
                        
        }

        return false;
    }
    
    public static boolean containSpecialSupScript(String str){
        return containSpecialScript(str,'^');
    }
    public static boolean containSpecialSubScript(String str){
        return containSpecialScript(str,'_');
    }
    /*return ture if the input string contains special sup- or sub-script like @,#,...*/
    public static boolean containSpecialScript(String str,char delim){
        ArrayList<String> list=new ArrayList<String>(Arrays.asList("\\dag","\\ddag","\\S","@","\\#","\\$","%","\\&","\\beta","\\gamma","\\lambda",
                "\\varphi","\\delta","\\theta","b","d","f","h","j","k","l","A","B","C","D","E","F","G","H","J","K","L"));
        
        //excluded \rho,\epsilon,\gamma,\omega,\psi,\chi,,\eta,\alpha
        return containScript(str,list,delim);

    }
    
    public static boolean containLargeCharacters(String str){
        ArrayList<String> list=new ArrayList<String>(Arrays.asList("\\dag","\\ddag","\\S","@","\\#","\\$","%","\\&","\\beta","\\gamma","\\lambda",
                "\\varphi","\\delta","\\theta","b","d","f","h","j","k","l","A","B","C","D","E","F","G","H","J","K","L"));
        
        for(int i=0;i<list.size();i++) {
        	String s=list.get(i);
        	if(str.contains(s))
        		return true;
        }
        
        return false;

    }
    
    /* return true if the input string contains sub- and sup-scripts at the same location,
     * e.g., LaTeX translation of ENSDF text=g{-9/2}{+-3}
     * NOT COMPLETED
     */
    public static boolean containDoubleScripts(String s) {
        String temp="^{";
        int n=s.indexOf(temp);
        if(n<0)
            return false;
        
        n=n+temp.length();
        
        int nbrackets=1;
        for(int i=n;i<s.length();i++){
            if(s.charAt(i)=='{')
                nbrackets++;
            if(s.charAt(i)=='}')
                nbrackets--;
            
            if(nbrackets==0){
                temp=temp+s.substring(n, i+1);
            }
                
        }
        
        return false;
    }
    
    /*return ture if the input string contains tall letters in script like @,#,h,k,...*/
    public static boolean containTallSupScript(String str){
        ArrayList<String> list=new ArrayList<String>(Arrays.asList("b","d","f","h","k","l"));
        if(!containSpecialSupScript(str))
            return containSupScript(str,list);
        else
            return true;
    }
    
    public static boolean containJPIScriptOnly(String str){
        String temp1=str;
        
        temp1=temp1.replace("+", "");
        temp1=temp1.replace("-", "");
        temp1=temp1.replace("\\textminus", "");
        temp1=temp1.replace("\\pi", "");

        if(containSupScript(temp1))
            return false;
        
        return true;

    }
    
    /*
     * 
    // Expression 	Description
    //     [abc] 	Find one character from the options between the brackets
    //     [^abc] 	Find one character NOT between the brackets
    //     [0-9] 	Find one character from the range 0 to 9
    
    //     Metacharacter 	Description
    //     | 	Find a match for any one of the patterns separated by | as in: cat|dog|fish
    //     . 	Find just one instance of any character
    //     ^ 	Finds a match as the beginning of a string as in: ^Hello
    //     $ 	Finds a match at the end of the string as in: World$
    //     \d 	Find a digit
    //     \s 	Find a whitespace character
    //     \b 	Find a match at the beginning of a word like this: \bWORD, or at the end of a word like this: WORD\b
    //     \\uxxxx 	Find the Unicode character specified by the hexadecimal number xxxx
    
    //     Quantifier 	Description
    //     n+ 	Matches any string that contains at least one n
    //     n* 	Matches any string that contains zero or more occurrences of n
    //     n? 	Matches any string that contains zero or one occurrences of n
    //     n{x} 	Matches any string that contains a sequence of X n's
    //     n{x,y} 	Matches any string that contains a sequence of X to Y n's
    //     n{x,} 	Matches any string that contains a sequence of at least X n's
    
    //     mather.groupCount() is the count of non-escaped () pairs 
    */
    public static Matcher getMatcher(String text,String regex,boolean isCaseSensitive) {
		Pattern pattern=null;
		if(isCaseSensitive)
			pattern=Pattern.compile(regex);
		else
			pattern=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
		
		Matcher matcher=pattern.matcher(text);
		return matcher;
    }
    public static Matcher getMatcher(String text,String regex) {
		return getMatcher(text,regex,true);
    }
    
    public static boolean containWholeWord(String text,String word) {
    	return containWholeWord(text,word,true);
    }
    public static boolean containWholeWord(String text,String word,boolean isCaseSensitive) {
    	try {
    		String regex="\\b"+word+"\\b";
    		Matcher matcher=getMatcher(text,regex,isCaseSensitive);
    		return matcher.find();
    	}catch(Exception e) {
    		
    	}
    	return false;
    }

    public static String toSciFormat(String input,boolean removeEndZeros) {
    	return toSciFormat(input,-1,removeEndZeros);
    }
    public static String toSciFormat(String input,int nsig) {
    	return toSciFormat(input,nsig,true);
    }
    public static String toSciFormat(String input) {
    	return toSciFormat(input,-1,true);
    }
    public static String toSciFormat(String input, int nsig,boolean removeEndZeros) {
        try {
        	
        	if(nsig<=0) {
        	    String tempS=input;
        	    if(removeEndZeros)
        	    	tempS=Str.removeEndZeros(input);
        		nsig=Str.findNSig(tempS);
        		
        		if(nsig<=0)
        			return tempS;
        		//System.out.println(tempS+"#  "+nsig);
        	}
            // Parse the input string to a BigDecimal
            BigDecimal number = new BigDecimal(input);

            // Use String.format to format the number in scientific notation
            String format = "%." + (nsig - 1) + "E"; // Create format string for scientific notation
            String s=String.format(format, number);
            s=s.replace("E+", "E").replace("E0","E").replace("E-0","E-");
            

    		//System.out.println("## input="+input+" nsig="+nsig+" s="+s+"###");
    		
            return s;
        } catch (NumberFormatException e) {
            // Handle invalid input
            return input;
        }
    }
    
    public static Vector<String> match(String text,String regex){
    	return match(text,regex,true);
    }
    public static Vector<String> match(String text,String regex,boolean isCaseSensitive){
    	Vector<String> out=new Vector<String>();
    	try {
    		Matcher matcher=getMatcher(text,regex,isCaseSensitive);
            while (matcher.find()) {
                String match = matcher.group();
                out.add(match);
            }
    	}catch(Exception e) {

    	}

		return out;
    }
    
    public static Entry<String,Integer> matchAndIndex(String text,String regex){
    	return matchAndIndex(text,regex,true);
    }
    public static Entry<String,Integer> matchAndIndex(String text,String regex,boolean isCaseSensitive){
    	
    	try {
    		Matcher matcher=getMatcher(text,regex,isCaseSensitive);  		
    		if(matcher.find()) {
    			String match = matcher.group();
    			return new SimpleEntry<>(match,matcher.start());
    		}

    	}catch(Exception e) {

    	}

		return new SimpleEntry<>("",-1);
    }
    
    public static Vector<Entry<String,Integer>> matchesAndIndexes(String text,String regex){
    	return matchesAndIndexes(text,regex,true);
    }
    public static Vector<Entry<String,Integer>> matchesAndIndexes(String text,String regex,boolean isCaseSensitive){
    	Vector<Entry<String,Integer>> out=new Vector<Entry<String,Integer>>();
    	try {
    		Matcher matcher=getMatcher(text,regex,isCaseSensitive);
            while (matcher.find()) {
                String match = matcher.group();
                int index = matcher.start();
                out.add(new SimpleEntry<String, Integer>(match, index));
            }
    	}catch(Exception e) {

    	}

		return out;
    }
    
    public static String replaceWholeWord(String text,String word,String replace) {
    	return replaceWholeWord(text,word,replace,true);
    }
    //replace the whole word (not part of a word) in the text with the given text
    //e.g., for a word="R", it will only replace "R" a whole word, not "R" in 
    //      other words like "MR", "RI", "WORD"
    public static String replaceWholeWord(String text,String word,String replace,boolean isCaseSensitive) {
    	
    	try {
    		String s="";
    		String regex="\\b"+word+"\\b";   		
    		Matcher matcher=getMatcher(text,regex,isCaseSensitive);
    		int n1=0,n2=0;
            while (matcher.find()) {
                n2 = matcher.start();
                s+=text.substring(n1,n2)+replace;
                n1=n2+word.length();
            }
            
            if(n1<text.length())
            	s+=text.substring(n1);
            
            return s;
    	}catch(Exception e) {
    		
    	}
    	return text;
    }
    
    public static boolean hasMatch(String text,String regex) {
    	return hasMatch(text,regex,true);
    }
    public static boolean hasMatch(String text,String regex,boolean isCaseSensitive) {
    	try {
    		Matcher matcher=getMatcher(text,regex,isCaseSensitive);
    		return matcher.find();
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean hasUnmatchedBracket(String text, String bracketType) {
    	try {
    	       char openBracket;
    	        char closeBracket;

    	        switch (bracketType) {
    	            case "(":
    	            case ")":
    	            case "()":
    	                openBracket = '(';
    	                closeBracket = ')';
    	                break;
    	            case "[":
    	            case "]":
    	            case "[]":
    	                openBracket = '[';
    	                closeBracket = ']';
    	                break;
    	            case "{":
    	            case "}":
    	            case "{}":
    	                openBracket = '{';
    	                closeBracket = '}';
    	                break;
    	            case "<":
    	            case ">":
    	            case "<>":
    	                openBracket = '<';
    	                closeBracket = '>';
    	                break;
    	            default:
    	                return false;
    	        }

    	        Stack<Character> stack = new Stack<>();

    	        for (char ch : text.toCharArray()) {
    	            if (ch == openBracket) {
    	                stack.push(ch);
    	            } else if (ch == closeBracket) {
    	                if (stack.isEmpty() || stack.pop() != openBracket) {
    	                    return true; // Unmatched closing bracket
    	                }
    	            }
    	        }

    	        return !stack.isEmpty(); // Unmatched opening brackets
    	}catch(Exception e) {
    		
    	}
    	return false;
    	
 
    }
   
    public static boolean hasUnmatchedBracketAny(String text) {
    	try {
    	        Stack<Character> stack = new Stack<>();

    	        for (char ch : text.toCharArray()) {
    	            if (ch == '(' || ch=='[' || ch=='{') {
    	                stack.push(ch);
    	            } else if (ch == ')' || ch==']' || ch=='}') {
    	            	//System.out.println("####"+stack.isEmpty()+"  ch="+ch+"  "+stack.peek()+"  "+isBracketPair(stack.peek(),ch) );
    	                if (stack.isEmpty() || !isBracketPair(stack.pop(),ch) ) {
    	                    return true; // Unmatched closing bracket
    	                }
    	            }
    	        }

    	        return !stack.isEmpty(); // Unmatched opening brackets
    	}catch(Exception e) {
    		
    	}
    	return false;
    	
 
    }
    public static boolean isBracketPair(char openBracket,char closeBracket) {
    	try {
    		
    		//note that char+char returns an integer number
    		String s=openBracket+""+closeBracket+"";
    		//System.out.println("  @ "+s+"  "+s.equals("()"));
    		
    		if(s.equals("()")||s.equals("[]")||s.equals("{}"))
    			return true;
    		
    	}catch(Exception e) {
    		
    	}
    	
    	return false;
    }
    //find the first script (sup or sub) in the given string s
    //delim='^' or '-'
    public static String findScript(String s,char delim){
        String out="";
        String temp=delim+"{";
        int n=s.indexOf(temp);
        if(n<0)
            return "";
        
        n=n+temp.length();
        
        int nbrackets=1;
        for(int i=n;i<s.length();i++){
            if(s.charAt(i)=='{')
                nbrackets++;
            if(s.charAt(i)=='}')
                nbrackets--;
            
            if(nbrackets==0){
                temp=temp+s.substring(n, i+1);
                n=temp.length();
                out="";
                for(int j=n-1;j>=0;j--){
                    char c=temp.charAt(j);
                    if(c=='}')
                        continue;
                    if(c=='{') {
                    	if(out.isEmpty() && i<s.length()-1)
                    		return findScript(s.substring(i+1),delim);
                    	else         				
                            return out;
                    }
                    out=c+out;
                }
            }
                
        }
        
        return out;
    }
    
    public static String getSignificantDigits(String numStr) {
        if (numStr == null || numStr.isEmpty() || numStr.equals("0")) return "0"; // Handle empty and zero cases

        numStr = numStr.toLowerCase();
        numStr = numStr.replaceAll("[eE][+-]?\\d+", ""); // Remove scientific notation
  
        // Remove leading zeros
        numStr = numStr.replaceFirst("^0+", "");
        
        // Remove decimal point
        numStr = numStr.replace(".", "");
        
        return numStr;
    }
    
    //used to get a string for estimating its length in PDF output
    public static String getString(String val,String unc,String unit,String flags){
    
        String out="";
     
        if (unc.equals("LT") || unc.equals("LE")) {
            out += "<";
            unc="";
        }else if (unc.equals("GT") || unc.equals("GE")) {
            out += ">";
            unc="";
        }else if (unc.equals("AP")) {
            out += "~";
            unc="";
        }else if(unc.equals("SY")) {
        	unc="syst";
        }else if(unc.equals("CA")) {
        	unc="calc";
        }else if(!Str.isNumeric(unc) && !unc.contains("+")) {      
        	out +=unc;
        }
        
        out+=val+flags;
        if(unit.trim().length()>0)        
        	out+=" "+unit;
       
        if(Str.isNumeric(unc) || unc.contains("+") || unc.equals("syst") || unc.equals("calc"))      
        	out+=" "+unc;
        
        return out;
    }
    
    //used to get a string for estimating its length in PDF output
    public static String getString(String val,String unc,String unit){
    
        return getString(val,unc,unit,"");
    }
    
    public static String delimLeftString(String s,String delim){
        String out="";
        int p=s.indexOf(delim);
        if(p>=0){
            out=s.substring(0, p);
        }
        
        return out;
    }
    
    public static String delimLeftString(String s,char delim){
        return delimLeftString(s,""+delim);
    }
    
    public static String delimRightString(String s,String delim){
        String out="";
        int p=s.indexOf(delim);
        if(p>=0){
            out=s.substring(p+1);
        }
        
        return out;
    }
    
    public static String delimRightString(String s,char delim){
        return delimRightString(s,""+delim);
    }
    
    public static String[] parseSDS(String str) {
    	String[] out=new String[0];
    	try {
    		str=str.trim();
    		str=str.replace("{I"," ").replace("{"," ").replace("}"," ");
    		str=str.replace("("," ").replace(")"," ");
    		str=str.trim();
    		
    		int n=str.indexOf(" ");
    		
    		String s="",ds="";
    		if(n>0) {
    			s=str.substring(0,n).trim();
    			ds=str.substring(n).trim();
    			n=ds.indexOf(" ");
    			if(n>0)
    				ds=ds.substring(0,n);
    			
    		}else {
    			s=str;
    		}
    		if(s.length()>0) {
    			out=new String[2];
    			out[0]=s;
    			out[1]=ds;
    		}
    	}catch(Exception e) {
    		
    	}
    	return out;
    }
    
    /*
     * parse a string of values separated by any char in delims
     * If parseIntoBrackets=false, everything enclosed in the outermost "()"
     * is considered as a whole, otherwise go into "()" to look for "," or "+"
     */
    
    public static Vector<String> parseValueStr(String s,String delims,boolean parseIntoBrackets){
    	Vector<String> out=new Vector<String>();
    	try {
    		s=s.trim();
    		if(s.isEmpty())
    			return out;
    		
    		if(parseIntoBrackets) {
    			String[] temp=s.split("["+delims+"]");
    			for(int i=0;i<temp.length;i++) {
    				s=temp[i];
    				String s1=s.replace("(", "");
    				String s2=s.replace(")","");
    				int n1=s.length()-s1.length();
    				int n2=s.length()-s2.length();
    				if(n1!=n2) {
    					if(n1>n2) {
    						for(int j=0;j<n1-n2;j++)
    							s=s+")";
    					}else {
    						for(int j=0;j<n2-n1;j++)
    							s="("+s;
    					}
    				}
    				
    				String prevS=s;
    				while(s.startsWith("(")&&s.endsWith(")")){
    					prevS=s;
    					if(s.length()>2) {
    						s=s.substring(1,s.length()-1);
    					}else {
    						s="";
    						break;
    					}
    				}
    				
    				s=prevS;
    				
    				while(s.contains("()")) {
    				    s=s.replace("()","");
    				}
    				out.add(s);
    			}
    		}else {
    			int i=0;
    			int nb=0;
    			String tempS="";
    			while(i<s.length()) {
    				char c=s.charAt(i);

    				if(c=='(')
    					nb++;
    				else if(c==')')
    					nb--;
    				else if(delims.contains(c+"") && nb==0) {
    					out.add(tempS);
    					tempS="";
    					i++;
    					
    					if(i==s.length()) {
    						out.add(tempS);
    						break;
    					}
    					
    					continue;
    				}
    				
    				i++;
    				tempS+=c;
     			}
    			
    			if(tempS.length()>0)
    				out.add(tempS);
    		}
    	}catch(Exception e) {
    		
    	}
    	if(out.isEmpty()&&s!=null)
    		out.add(s);
    	
    	return out;
    }    
    
    /*
     * put input path inside " " if it has any space in it
     */
    public static String path(String path) {
    	try {
    		if(path.contains(" ")) {
    			path="\""+path+"\"";
    		}
    	}catch(Exception e) {
    		
    	}
    	return path;
    }
    
    public static String mkcmd(String commandPath,String filePath1,String filePath2) {
    	try {
    		String s=commandPath.trim();
    		if(s.isEmpty())
    			return "";
    		
    		filePath1=filePath1.trim();
    		if(!filePath1.isEmpty())
    			s+=" "+path(filePath1);
    		
    		filePath2=filePath2.trim();
    		if(!filePath2.isEmpty())
    			s+=" "+path(filePath2);
    		
    		return s;
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    //return non-exponential format of the input number str 
    public static String removeExpFormat(String valStr){
        
        String val=valStr;
        
        String lb="",rb="";
        if(val.charAt(0)=='(' && val.charAt(val.length()-1)==')'){
            lb="(";
            rb=")";
            val=val.substring(1,val.length()-1);
        }
        
        if(!isNumeric(val) || !val.toUpperCase().contains("E"))
            return valStr;
        
    
        int pe=val.toUpperCase().indexOf("E");       
        int pp=val.indexOf(".");
        int pow=0;
        String base=val.substring(0,pe);
        
        try{
            pow=Integer.parseInt(val.substring(pe+1));
        }catch (Exception e){
            return val;
        }
        
        if(pp<0)
            pp=pe;
        
        base=base.replace(".","");
        pp+=pow;
        
        if(pp<=0){
            while(pp<0){
                base="0"+base;
                pp++;
            }
            base="0."+base;
        }
        else if(pp<base.length()){
            base=base.substring(0, pp)+"."+base.substring(pp);
        }
        else{
            int len=base.length();
            while(pp>len){
                base+="0";
                pp--;
            }
        }
        
        return lb+base+rb;
    }
    
    
    /*remove extra trailing zeros if more than one*/
    public static String removeEndZeros(String valStr){
        String s=valStr.substring(0).trim();
        if(s.length()==0 || !s.endsWith("0"))
            return valStr;
        
        while(s.endsWith("0"))
            s=s.substring(0, s.length()-1);
        
        if(s.isEmpty())
        	s="0";
        
        return s;
    }
    
    /*remove extra trailing zeros in (val,unc,unc+,unc-)*/   
    public static String[] removeEndZeros(String s,String ds,String dsu,String dsl) {

        String tempS=s;
        String expS="",sign="";

        //if(s.contains("1.0E2")) System.out.println("1 s="+s+" ds="+ds+" dsu="+dsu+" dsl="+dsl+" tempS="+tempS);
        
        if(s.charAt(0)=='+'||s.charAt(0)=='-'){
            sign+=s.charAt(0);
            tempS=s.substring(1);
        }
        
        int ie=tempS.indexOf("E");
        if(ie>=0) {
      	  expS=tempS.substring(ie).trim();          
      	  tempS=tempS.substring(0,ie).trim();       
        }

        //if(s.contains("1.0E2")) 
        //System.out.println("2 s="+s+" ds="+ds+" dsu="+dsu+" dsl="+dsl+" tempS="+tempS);
        
        //remove redundant ending zeros
        if(tempS.endsWith("0") && tempS.indexOf(".")>=0 ) {
            if(!Str.isNumeric(dsu) || !Str.isNumeric(dsl)) {
                tempS=removeEndZeros(tempS);             
            }else if(dsu.endsWith("0") && dsl.endsWith("0")){
                while(dsu.endsWith("0") && dsl.endsWith("0") && tempS.endsWith("0")) {
                    tempS=tempS.substring(0,tempS.length()-1);
                    dsu=dsu.substring(0,dsu.length()-1);
                    dsl=dsl.substring(0,dsl.length()-1);
                }

                if(dsu.equals(dsl)) {
                    ds=dsu;
                }else {
              	  if((dsu+dsl).isEmpty())
              		  ds="";
              	  else {
              		  if(dsu.isEmpty())
              			  dsu="0";
              		  else if(dsl.isEmpty())
              			  dsl="0";
              		  
              		  ds="+"+dsu+"-"+dsl;
              	  }
                    
                }

            }
            
            //if(s.contains("1.0E2")) 
            //System.out.println("3 s="+s+" ds="+ds+" dsu="+dsu+" dsl="+dsl+" tempS="+tempS);
           
            if(tempS.endsWith("."))
                tempS=tempS.substring(0,tempS.length()-1);
            
            //if(s.contains("1.0E2")) 
            //System.out.println("4 s="+s+" ds="+ds+" dsu="+dsu+" dsl="+dsl+" tempS="+tempS);
            
            s=sign+tempS+expS;
            
            //if(s.contains("1.0E2")) System.out.println("3 s="+s+" ds="+ds+" dsu="+dsu+" dsl="+dsl+" tempS="+tempS);
            //System.out.println("2 s="+s+" ds="+ds+" dsu="+dsu+" dsl="+dsl+" tempS="+tempS);
        }      
        
        String[] out=new String[4];
        out[0]=s;
        out[1]=ds;
        out[2]=dsu;
        out[3]=dsl;
        
        return out;
    }
    
    /*remove extra trailing zeros in (val,unc+,unc-)*/   
    public static String[] removeEndZeros(String s,String dsu,String dsl) {
    	String ds=dsu;
    	String[] temp=removeEndZeros(s,ds,dsu,dsl);
    	String[] out=new String[3];
    	
    	out[0]=temp[0];
    	out[1]=temp[2];
    	out[2]=temp[3];
    	
    	return out;
    }
    
    /*remove extra trailing zeros in (val,unc)*/   
    public static String[] removeEndZeros(String s,String ds) {
    	String dsu=ds;
    	String dsl=ds;
    	String[] temp=removeEndZeros(s,ds,dsu,dsl);
    	String[] out=new String[2];
    	
    	out[0]=temp[0];
    	out[1]=temp[1];
    	
    	return out;
    }
    
    /*remove extra ending zeros after decimal point*/
    //minNisg: min number of significant digits to keep, if length<minNsig, use minNsig=length
    //if minNisg<=0, use minNsig=2
    //eg, 13.0 to 13

    public static String removeExtraZerosAfterDot(String valStr,int minNsig){
        
        if(!isNumeric(valStr) || !valStr.contains("."))
            return valStr;
        
        String s=valStr.substring(0).trim();

        int iExp=s.toUpperCase().indexOf("E");
        
        String expS="";
        if(iExp>0) {
            expS=s.substring(iExp);
            s=s.substring(0,iExp);
         }
        
        if(!s.endsWith("0"))
            return valStr;
        
        if(minNsig<=0)
            minNsig=2;
        
        if(s.length()<minNsig)
            return valStr;
         
        s=roundByNsig(s,minNsig);
        
        s=s+expS;
        
        return s;
    }
    
    //remove extra spaces (>1) and reduce the spaces to single spaces
    //if trim=ture, trim the heading and tailing spaces, otherwise 
    //only remove extra spaces in the middle of the input str
    public static String removeExtraSpaces(String str,boolean trim) {
    	String s=str;
    	if(s.trim().isEmpty()) {
    		if(trim)
    			return "";
    		
    		return s;
    	}
    	
    	try {
    		String s1=s.trim();
    		int n=s.indexOf(s1);
    		String preS=s.substring(0,n);
    		String postS=s.substring(n+s1.length());
    		
    		while(s1.contains("  ")) {
    			s1=s1.replace("  ", " ");
    		}

    		if(trim)
    			s=s1;
    		else
    			s=preS+s1+postS;
    		
    	}catch(Exception e) {
    		
    	}
    	
    	return s;
    }
    
    public static String removeExtraSpaces(String str) {
    	return removeExtraSpaces(str,true);
    }
    //return true if the input number string is in exponential format
    public static boolean isExpFormat(String valStr){
        String val=valStr;
        
        if(val.charAt(0)=='(' && val.charAt(val.length()-1)==')'){
            val=val.substring(1,val.length()-1);
        }
        
        if(!isNumeric(val))
            return false;
            
        int p=val.toUpperCase().indexOf("E");
        if(p<=0 || p==valStr.length()-1)
            return false;
        
        return true;
    }
    
    //return true if the input string contains exponential format
    public static boolean containExpFormat(String valStr){
            
        int p=valStr.toUpperCase().indexOf("E");
        if(p<=0 || p==valStr.length()-1)
            return false;
        if(!Character.isDigit(valStr.charAt(p-1)))
            return false;
        
        if(Character.isDigit(valStr.charAt(p+1)))
            return true;
        if(valStr.charAt(p+1)=='+' || valStr.charAt(p+1)=='-')
            if(p<valStr.length()-2 && Character.isDigit(valStr.charAt(p+2)))
                return true;
                
            
        return false;
    }
    
    public static boolean replace(String filepath,String oldStr,String newStr){
        boolean fail=true;
        
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(filepath), StandardCharsets.UTF_8));

            for (int i = 0; i < fileContent.size(); i++) {
                String line=fileContent.get(i);
                if(!line.contains(oldStr))
                    continue;
                
                //debug
                //System.out.println("In Str line 1162: line="+line);
                
                fileContent.set(i, line.replace(oldStr, newStr));
            }

            Files.write(Paths.get(filepath), fileContent, StandardCharsets.UTF_8);
            fail=false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail=true;
        }
        
        return fail;
    }
    
    public static String replace(String line,int start,int len,String str){
        String s=line;
        try{
            if(line.length()==0 || start<0 || len<=0 || (start+len)>s.length() || str.length()>len)
                return line;
            
            s=line.substring(0, start)+str+Str.repeat(" ", len-str.length())+line.substring(start+len);
        
        }catch (Exception e){
        }
        
        return s;
    }
    
    public static boolean runScript(String path) throws IOException{
        
        String dir="",script="";
        try{
            File file=new File(path);
            dir=file.getAbsoluteFile().getParentFile().getAbsolutePath();
            script=file.getName();
        }catch(Exception e){
            return false;
        }
        
        if(dir.isEmpty() || script.isEmpty())
            return false;
        
        String command="";
        String os=System.getProperty("os.name").toLowerCase();      
        
        Process proc=null;
            
        if(os.contains("linux")||os.contains("mac")){

            
            String shell="/bin/bash";
                                                
            File wd = new File(dir);
            proc = null;
           
            //proc = Runtime.getRuntime().exec(shell+" -c gnome-terminal", null, wd);
            proc = Runtime.getRuntime().exec(shell, null, wd);
            

            if (proc != null) {
                
               BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
               PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);

               
               out.println("source "+script);
               out.println("pwd");
               out.println("exit");

               in.close();
               out.close();
               proc.destroy();
            }  
        }
        else if(os.contains("windows")){
            Runtime rt = Runtime.getRuntime();
            
            //run cmd command in Java   
            //
            //method1: works only when run in the same drive, not work if script in a different drive
            //
            //command="cmd /c cd "+nds.Setup.outdir+"&& start "+script+"&exit";
            //rt.exec(command);


            /*
            //
            //method2: 
            //
            command="cmd /c "+script;             //If you do not want to start your process in it's separate console (that's what start does), 
                                                  //you must wait with p.waitFor() or read it's input stream - otherwise it may silently fail.
            Process proc=rt.exec(command,         //path to executable
                    null,                                  // env vars, null means pass parent env
                    new File(nds.Setup.outdir));

            InputStream is=proc.getInputStream();
            BufferedReader br= new BufferedReader(new InputStreamReader(is));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            out.println("exit");
           
            String line=new String();
            while ((line=br.readLine())!=null) {
                //System.out.println (line);
            }
            br.close();
            proc.destroy();
            */
            
            //
            //method2:easiest way
            //

            
            command="cmd /c start "+script;       //start your process in it's separate console (that's what start does)
            proc=rt.exec(command,         //path to executable
                    null,                         // env vars, null means pass parent env
                    new File(dir));  // working directory 
                
        }

        return true;
    }
    

    public static Vector<String> runCommand(String command,String[] envp,String dir) throws IOException{

        Vector<String> msgs=new Vector<String>();
        
        if(command.trim().isEmpty())
            return msgs;
        
        String os=System.getProperty("os.name").toLowerCase();      
        
        Process proc=null;
        File wd=null;
        if(dir!=null && !dir.isEmpty())
            wd=new File(dir);

        if(os.contains("linux")||os.contains("mac")){       
            proc = Runtime.getRuntime().exec(command, envp, wd);              
        }
        else if(os.contains("windows")){
            Runtime rt = Runtime.getRuntime();
            
            //run cmd command in Java   
            //
            //method1: works only when run in the same drive, not work if script in a different drive
            //
            //command="cmd /c cd "+nds.Setup.outdir+"&& start "+script+"&exit";
            //rt.exec(command);


            /*
            //
            //method2: 
            //
            command="cmd /c "+script;             //If you do not want to start your process in it's separate console (that's what start does), 
                                                  //you must wait with p.waitFor() or read it's input stream - otherwise it may silently fail.
            Process proc=rt.exec(command,         //path to executable
                    null,                                  // env vars, null means pass parent env
                    new File(nds.Setup.outdir));

            InputStream is=proc.getInputStream();
            BufferedReader br= new BufferedReader(new InputStreamReader(is));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            out.println("exit");
           
            String line=new String();
            while ((line=br.readLine())!=null) {
                //System.out.println (line);
            }
            br.close();
            proc.destroy();
            */
            
            //
            //method2:easiest way
            //

            
            command="cmd /c start "+command;       //start your process in it's separate console (that's what start does)
            proc=rt.exec(command,                  //path to executable
                    envp,                          // env vars, null means pass parent env
                    wd);                           // working directory 
                
        }

 
        if(proc!=null){
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));         
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
              
            String line="";                   
            while ((line=in.readLine()) != null) {  
                   msgs.add(line);
                   //run.printMessage(line);               
                   //System.out.println(line);              
                   //if(line.trim().indexOf('?')==0){//try to catch latex error             
                   //      System.out.println("test");                  
                   //      break;                
                   //}                    
            }
               
            //proc.waitFor();
            in.close();
            out.close();           
            proc.destroy();
        }

            
        return msgs;
    }
    
    public static boolean mkdir(String path){
        File dir=new File(path);
        if(!dir.exists() && !dir.mkdir()){
            System.out.println("Failed to create directory:"+path);
            return false;
        }
        
        return true;
    }
    
    public static boolean mkdirs(String path){
        File dir=new File(path);
        if(!dir.exists() && !dir.mkdirs()){
            System.out.println("Failed to create directory:"+path);
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @param s: number string to be shifted
     * @param nsteps: number of steps to shift
     * @param direction: direction to shift to
     *        "UP" or "LEFT": shift to left
     *        "DOWN" or "RIGHT" shift to right
     * @return: number string after shift
     */
    public static String shiftDecimalPoint(String s,int nsteps,String direction) {
    	String out=s;
    	try {

			double x=Double.parseDouble(s);
    		
    		if(nsteps<=0)
    			return out;
    		
    		if(x==0)
    			return out;
    		
    		String sign="";
    		if(s.startsWith("+") || s.startsWith("-")) {
    			sign=s.charAt(0)+"";
    			s=s.substring(1);
    		}
    			
    		String su=s.toUpperCase();
    		
    		direction=direction.toUpperCase().trim();
    		
    		int len=s.length();
    		boolean toLeft=false;
    		if(direction.startsWith("LE")||direction.startsWith("UP"))
    			toLeft=true;
    		else
    			toLeft=false;
    				
    		if(su.contains("E")) {
    			int ie=su.indexOf("E");
    			String baseS=s.substring(0,ie);
    			String powerS=s.substring(ie+1);
    			int power=Integer.parseInt(powerS);
    			if(toLeft) {
    				power-=nsteps;
    			}else {
    				power+=nsteps;
    			}
    			
    			if(power==0) {
    				out=baseS;
    			}else if(power==1) {
    				out=shiftDecimalPoint(baseS,1,"RIGHT");
    			}else if(power==-1) {
    				out=shiftDecimalPoint(baseS,1,"LEFT");
    			}else {
    				if(powerS.startsWith("+")&&power>0)
    					powerS="+"+power;
    				else if(powerS.startsWith("-") && power<0)
    					powerS="-"+Math.abs(power);
    				else
    					powerS=String.valueOf(power);
    				
    				out=baseS+s.charAt(ie)+powerS;
    			}
    		}else {
    			if(s.contains(".")) {
    				int idot=s.indexOf(".");
    				String integerPart=s.substring(0,idot);
    				String decimalPart=s.substring(idot+1);
    				
    				
    				if(toLeft) {
    					len=integerPart.length();
    					if(nsteps<len) {
    						out=integerPart.substring(0,len-nsteps)+"."+integerPart.substring(len-nsteps)+decimalPart;
    					}else {
    						out=integerPart+decimalPart;
    						for(int i=0;i<nsteps-len;i++)
    							out="0"+out;
    						
    						out="0."+out;
    					}
    				}else {
    					len=decimalPart.length();
    					if(integerPart.equals("0"))
    						integerPart="";
    						
    					if(nsteps<len) {
    						out=integerPart+decimalPart.substring(0,nsteps)+"."+decimalPart.substring(nsteps);
    					}else {
    						out=integerPart+decimalPart;
    						for(int i=0;i<nsteps-len;i++)
    							out+="0";
    	
    					} 
    					//System.out.println(s+"  "+out);
    					while(out.length()>1 && out.charAt(0)=='0' && out.charAt(1)!='.') {
    						out=out.substring(1);
    					}
    				}
    			}else {
    				//s is integer, e.g., 35
    				if(toLeft) {
    					if(nsteps<len) {
    						out=s.substring(0,len-nsteps)+"."+s.substring(len-nsteps);
    					}else {
    						out=s;
    						for(int i=0;i<nsteps-len;i++)
    							out="0"+out;
    						
    						out="0."+out;
    					}    										
    				}else {
    					//just add ending zeros
    					for(int i=0;i<nsteps;i++)
    						out+="0";
    				}
    			}
    		}
    		
    		if(out.startsWith(".") && toLeft)
    			out="0"+out;
    		else if(out.endsWith(".") && !toLeft)
    			out=out.substring(0,out.length()-1);
    		
    		out=sign+out;
    	}catch(Exception e) {
    		
    	}
    	return out;
    }

    /*
     * merge files into a single vector of strings
     * with empty lines at the begining and end of each file 
     * being removed
     */
    public static Vector<String> mergeFiles(Vector<File> filesV){
        Vector<String> out=new Vector<String>();
            
        try{
            Vector<String> lines;
            for(int i=0;i<filesV.size();i++){
                File f=filesV.get(i);
                if(f==null)
                    continue;
                
                lines=Str.trimV(readFile(f));
                out.addAll(lines);
                out.add("\n");
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
                
        return out;
    }
    
    /*
     * merge files as is into a single vector of strings
     * with empty lines at the begining and end of each file 
     * being kept
     */
    public static Vector<String> mergeFilesAsIs(Vector<File> filesV){
        Vector<String> out=new Vector<String>();
        
        try{
            Vector<String> lines;
            for(int i=0;i<filesV.size();i++){
                File f=filesV.get(i);
                if(f==null)
                    continue;
                
                lines=readFile(f);
                out.addAll(lines);
                out.add("\n");
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
                
        return out;
    }
    
    public static Vector<String> readLines(java.io.File f,int n) throws IOException{
        Vector<String> text = new Vector<String>();
        
        if(n<=0)
            return text;
        
        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(f));
        }catch(Exception e){
            throw new IOException("Error: file does not exist!\n"+f.getAbsolutePath()+"\n");
        }
              
        String s;
        

        int count=0;
        while(true){
            s = br.readLine();
            if (s==null) break;
            //System.out.println(s);
            text.add(s);
            count++;
            if(count==n)
                break;
        }
        
        if(br!=null)
            br.close();
        
        return text;

    }
    
    public static Vector<String> readFile(java.io.File f) throws IOException{
        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(f));
        }catch(Exception e){
            throw new IOException("Error: file does not exist!\n"+f.getAbsolutePath()+"\n");
        }
              
        String s;
        
        Vector<String> text = new Vector<String>();
        while(true){
            s = br.readLine();
            if (s==null) break;
            //System.out.println(s);
            text.add(s);
        }
        
        if(br!=null)
            br.close();
        
        return text;

    }
    
    public static String readFirstLineOfFile(java.io.File f) throws IOException{
        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(f));
        }catch(Exception e){
            throw new IOException("Error: file does not exist!\n"+f.getAbsolutePath()+"\n");
        }
              
        String s=br.readLine();
        
        if(br!=null)
            br.close();
        
        return s;

    }
    
    public static String readFirstNotEmptyLineOfFile(java.io.File f) throws IOException{
        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(f));
        }catch(Exception e){
            throw new IOException("Error: file does not exist!\n"+f.getAbsolutePath()+"\n");
        }
              
        String s=null;
        while(true){
            s = br.readLine();
            if (s==null || !s.trim().isEmpty()) 
            	break;
        }
        
        if(br!=null)
            br.close();
        
        return s;

    }
    
    public static String readFileToString(java.io.File f) throws IOException {
        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(f));
        }catch(Exception e){
            throw new IOException("Error: file does not exist!\n"+f.getAbsolutePath()+"\n");
        }
              
        String line="",s="";

        while(true){
            line = br.readLine();
            if (line==null) break;
            //System.out.println(s);
            s+=line+"\n";
        }
        
        if(br!=null)
            br.close();
        
        return s;  	
    }
    
    public static String readInputStreamToString(InputStream is) throws IOException {
   	 java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();
   	 byte[] buffer = new byte[1024];
   	 for (int length; (length = is.read(buffer)) != -1; ) {
   	     result.write(buffer, 0, length);
   	 }
   	 // StandardCharsets.UTF_8.name() > JDK 7
   	 return result.toString("UTF-8");   	
   }
    
    public static InputStream getInputStream(String filePath) {
    	InputStream is=null;
    	
    	try {
            URL url = ClassLoader.getSystemResource(filePath);//get the default file in the classpath if it exists
            if(url==null){//if it does not exist in the classpath, try local file
            	File f=new File(filePath);
            	url=f.toURI().toURL();
            }
            
            is=url.openStream();

    	}catch(Exception e) {
    	}

        if(is==null){
    		is = Str.class
    				.getClassLoader()   	    	        
    				.getResourceAsStream(filePath);
    		
            if(is==null)
            	System.out.println("Error: file not found: "+filePath);
        }
        
    	return is;
    }
    
    public static void write(PrintWriter out,Vector<String> lines) throws IOException{

        int n=lines.size();
        if(n==0)
            return;
               
        for(int i=0;i<n;i++){
        	String line=lines.get(i)+"";
            
            line=removeEOL(line)+"\n";
            out.write(line);
        }
    }
    
    public static void write_test(PrintWriter out,Vector<String> lines) throws IOException{

        int n=lines.size();
        if(n==0)
            return;
               
        for(int i=0;i<n;i++){
        	String line=lines.get(i)+"";

            line=removeEOL(line);
            //line=line+"~@"+i+line.indexOf("\r")+line.length()+ "\n";
            line+="\n";
            out.write(line);
        }
    }
    
    public static void write(String filename,Vector<String> lines) throws IOException{

        int n=lines.size();
        if(n==0)
            return;
             
        PrintWriter out=new PrintWriter(new File(filename));
        for(int i=0;i<n;i++){
        	String line=lines.get(i)+"";
            //System.out.println("111###"+line+"222@@@"+n);
            
            line=removeEOL(line)+"\n";
            out.write(line);
        }
        out.flush();
        out.close();
    }
    
    public static void write(String filename,String s) throws IOException{

        if(s.isEmpty())
            return;
             
        PrintWriter out=new PrintWriter(new File(filename));
        out.write(s);
        
        out.flush();
        out.close();
    }
    
    /* insert a string into a file
     * option="B" or "b" for inserting at file beginning
     *        otherwise at the end
     */
    public static void insertIntoFile(String filename,String in,String option)throws IOException{
        
        if(in.length()==0)
            return;
        
        BufferedReader br=null;
        try{
            br = new BufferedReader(new FileReader(new File(filename)));
        }catch(Exception e){
            throw new IOException("Error when inserting a string into file: \n  file <"+filename+"> does not exist!\n");
        }
        
        String s;
        
        Vector<String> text = new Vector<String>();
        while(true){
            s = br.readLine();//with "\r", "\n" removed
            if (s==null) break;
            text.add(s);
        }
        br.close();
        
        
        PrintWriter out=new PrintWriter(new File(filename));
        
        s=Str.removeEOL(in);
        
        boolean insertAtend=true;
        if(option!=null && option.toUpperCase().trim().indexOf("B")==0){
            out.write(s+"\n");
            insertAtend=false;
        }

        for(int i=0;i<text.size();i++){
            out.write(text.get(i)+"\n");        
        }
        
        if(insertAtend)
            out.write(s+"\n");
        
        out.close();       
    }
    
    public static void insertIntoFile(String filename,String in)throws IOException{
        insertIntoFile(filename,in,"end");
    }
    

    public static boolean isFileEmpty(String filepath){
        return isFileEmpty(new File(filepath));
    }
    
    public static boolean isFileEmpty(File f){
        try {
            if(f==null || !f.exists())
                return true;
            
            Vector<String> lines=readFile(f);
            
            lines=trimV(lines);
            if(lines.size()>0)
                return false;
            
        } catch (IOException e) {
        }
        
        return true;
    }
    
    public static boolean isFileExist(String filepath){
        File f=new File(filepath);
        return f.exists();
    }
    
    
    //split a block of strings by empty line
    public static Vector<Vector<String>> splitStringBlock(Vector<String> block){
        Vector<Vector<String>> blockV=new Vector<Vector<String>>();
        String line="";
        Vector<String> subBlock=new Vector<String>();
        for(int i=0;i<block.size();i++){
            line=block.get(i);
            if(line.trim().length()>0){
                subBlock.add(line);
            }
            else{
                if(subBlock.size()>0){
                    blockV.add(subBlock);
                    subBlock=new Vector<String>();
                }else
                    continue;
                    
            }
        }
        
        if(subBlock.size()>0)//for last sub-block
            blockV.add(subBlock);
        
        return blockV;
    }
    
    public static Vector<Vector<String>> splitStringBlock(ArrayList<String> block){
        Vector<String> v=new Vector<String>();
        v.addAll(block);
        return splitStringBlock(v);
    }
    
    public static boolean isValidFilename_old(String filename){
        try {

            Paths.get(filename);

        }catch (InvalidPathException |  NullPointerException ex) {
            return false;
        }

        return true;
    }
    
    public static boolean isValidFilename(final String aFileName) {
        
        boolean isValid = true;
        try {
            File aFile = new File(aFileName);
            if (aFile.createNewFile()) {
                aFile.delete();
            }
        } catch (IOException e) {
            System.out.println("Error: invalid filename:"+aFileName);
            isValid = false;
        } catch(SecurityException e){
            System.out.println("Error: can't write file:"+aFileName);
            isValid=false;
        }
        return isValid;
    }
    
    //clean a file by setting each line to have the same length with
    //white space being added at the end
    public static void cleanFile(String oldfilename,String newfilename,int length) throws IOException{
        Vector<String> lines=readFile(new File(oldfilename));
        int nlines=lines.size();
        if(nlines==0)
            return;
        
        PrintWriter out=new PrintWriter(new File(newfilename));
        String line="";
        for(int i=0;i<nlines;i++){
            line=lines.get(i);
            line=fixLineLength(line,length);
            out.write(line+"\n");
        }
        out.close();
    }
    
    
    public static void cleanFile(String filename,int length) throws IOException{
        cleanFile(filename,filename,length);
    }
    
    /*center a string for a given width*/
    public static String center(int width,String str) {
        return String.format("%-" + width  + "s", String.format("%" + (str.length() + (width - str.length()) / 2) + "s", str));
    }
    
    /*center a substring in a given string*/
    public static String centerSubtring(String str,int beginIndex,int endIndex) {
        try {
            String sub=str.substring(beginIndex,endIndex).trim();
            int len=endIndex-beginIndex;
            sub=center(len,sub);
            return replace(str, beginIndex,len,sub);
        }catch(Exception e) {
            return str;
        }

    }
    
    //remove illegal characters in a filename (not path)
    public static String cleanFilename(String filename){
    	try {
            String out="";
            String legalChars="+-_.";
            
            for(int i=0;i<filename.length();i++){
                char c=filename.charAt(i);
                if(Character.isLetterOrDigit(c) || legalChars.indexOf(c)>=0)
                    out+=c;
                else
                	out+="_";
            }

            out=removeDuplicate(out,"_");
            if(out.endsWith("_"))
            	out=out.substring(0,out.length()-1).trim();
            
            return out;
    	}catch(Exception e) {
    		
    	}

    	return filename;
    }
    
	public static String removeUnmatchedBrackets(String input) {
	      Stack<Integer> stack = new Stack<>();
	        Map<Character, Character> matchingBrackets = new HashMap<>();
	        matchingBrackets.put(')', '(');
	        matchingBrackets.put('}', '{');
	        matchingBrackets.put(']', '[');
	        
	        Set<Integer> toRemove = new HashSet<>();

	        char[] chars = input.toCharArray();

	        for (int i = 0; i < chars.length; i++) {
	            char c = chars[i];

	            if (c == '(' || c == '{' || c == '[') {
	                stack.push(i);
	            } else if (c == ')' || c == '}' || c == ']') {
	                if (!stack.isEmpty() && chars[stack.peek()] == matchingBrackets.get(c)) {
	                    stack.pop(); // matched pair
	                } else {
	                    toRemove.add(i); // unmatched closing bracket
	                }
	            }
	        }

	        // remaining items in stack are unmatched opening brackets
	        while (!stack.isEmpty()) {
	            toRemove.add(stack.pop());
	        }

	        // Build the result string without unmatched brackets
	        StringBuilder result = new StringBuilder();
	        for (int i = 0; i < chars.length; i++) {
	            if (!toRemove.contains(i)) {
	                result.append(chars[i]);
	            }
	        }

	        return result.toString();
	}
	
    public static String removeAllBrackets(String s) {
    	return removeBracketsByType(s,"all");
    }
    
    /*
     * bracketType: ")" or "(", or "()", "]" or "[" or "[]","}" or "{" or "{}"
     * bracketType="" or "all" to remove all types of brackets
     */
    public static String removeBracketsByType(String s,String bracketType) {
    	
    	try {
    		String type=bracketType.trim().toUpperCase();
    		if(type.isEmpty() || type.startsWith("ALL")) {
    			s=s.replace("(","").replace(")","");
    			s=s.replace("[","").replace("]","");
    			s=s.replace("{","").replace("}","");
    			s=s.trim();
    		}else {
        		if(type.contains("(") || type.contains(")")) {
        			s=s.replace("(","").replace(")","");
        		}
        		if(type.contains("[") || type.contains("]")) {
        			s=s.replace("[","").replace("]","");
        		}
        		if(type.contains("{") || type.contains("}")) {
        			s=s.replace("{","").replace("}","");
        		}
        		s=s.trim();
    		}

    	}catch(Exception e) {
    		
    	}
    	
    	return s;
    }
    public static String removeDuplicate(String str,String pattern){
        String out=str;
        String temp="";
        String s=pattern+pattern;
        while(out.indexOf(s)>=0){
            temp=out;
            out=out.replace(s, pattern);
            if(out.equals(temp))
                break;
        }
        
        return out;
    }
    
    public static String removeEndPeriod(String s){
        //String out=s;
        //if(s.length()>0&&s.charAt(s.length()-1)=='.')
        //  out=s.substring(0, s.length()-1);
        
        int n=s.lastIndexOf('.');
        if(n<0)
            return s;
        
        if(n==0 && s.length()==1)
            return "";
        
        String s1=s.substring(0,n);
        String s2="";
        if(n<s.length()-1)
            s2=s.substring(n+1);
        
        if(s2.replace("\n", "").trim().length()==0)
            return s1+s2;
        else
            return s;
        
    }
    
    public static String removeEOL(String s){
        if(s==null)
            return null;

        //s=s.replace("\r\n","\n");

        for(int i=s.length()-1;i>=0;i--){
            char c=s.charAt(i);

            if(c!='\n' && c!='\r') {
                return s.substring(0,i+1);
            }
        }


        return "";
        
    }
    
    //make a valid filename string from str by
    //replacing all chacacters but "+-_.", letters and digits
    //with "_" and removing extra or duplicate character
    public static String makeFilenameString(String str){
        String out="";
        String legalChars="+-_.";
        String filename=str.trim().toLowerCase();
        
        filename=filename.replace("t1/2","t12");
        filename=filename.replace("e=","E_");
        
        for(int i=0;i<filename.length();i++){
            char c=filename.charAt(i);
            if(Character.isLetterOrDigit(c) || legalChars.indexOf(c)>=0)
                out+=c;
            else if(c=='\'')
                out+="P";
            else
                out+="_";
        }

        out=removeDuplicate(out,"_").trim();
        if(out.lastIndexOf("_")>=0 && out.lastIndexOf("_")==out.length()-1)
            out=out.substring(0, out.length()-1);

        return out.trim();  
    }
    
    /*
     * NUCID: size<=5, eg, "1H", "9BE","23NA","119AG"
     *  type: size<=4, eg, "L", "CL","2 L","CDP","2 DP"  
     */
    public static String makeENSDFLinePrefix(String NUCID,String type){
        String s="";
        int n=0;
        if(type.trim().length()>4)
        	return "";
        
        try{
            s=NUCID.trim();
            n=s.length();
            char c=0;
            if(n>1){
                c=s.charAt(n-2);
                if(Character.isDigit(c) && Character.isLetter(s.charAt(n-1)))//NUCID with length=1 for element name
                    s=s+" ";
                else if(isInteger(s))
                	s=s+"  ";//NUCID with no element name
            }   
                        
        }catch(Exception e){
            return "";
        }
        

        NUCID=s;
      
        //type: col5-8, like 
        //"  L ", " cL "," dL ", "2cL ", "xcL ","2 L ","B L " for L/G/B/A/E record
        //"  DP", " cDP"," dDP", "2cDP", "xcDP","2 DP" for delayed particle (N/P/D/T) record
        //"   P", " c P"," d P", "2c P", "xc P","2  P" for prompt particle record
        //" PN ","cPN "
        s=type.trim();
        if(s.length()==0)
        	type="";
        else if(type.length()==4 && type.substring(0,3).trim().isEmpty() && "NPDT".contains(s.toUpperCase())) {//prompt particle decay
        	type=type.toUpperCase();
        }else {
        	String c1=s.toUpperCase().charAt(0)+"";
        	String c2="";

        	if(s.length()>1)
        		c2=s.toUpperCase().charAt(1)+"";
        	
        	String s1="",s2="";
        	if(c1.equals("C") || (c1.equals("D")&&s.length()==1) ) { //comment and document lines, "CL", "D"
        		s1=" "+s.charAt(0);
        		s2=s.substring(1);
        	}else if(c2.equals("C") || (c2.equals("D")&&s.length()==2) ) {//"2CL","2D"
        		s1=" "+s.charAt(1);
        		s2=s.substring(2);
        	}else if(c2.equals(" ")) {//continuation record
        		s1=s.substring(0,2);
        		s2=s.substring(2);
        		if(s2.trim().length()==1 && "NPDT".contains(s2.trim())) {
        			if(c1.equals("D")) {//document for prompt particle record, not continuation record
        				s1=" "+s.charAt(1);
        				s2=" "+s2;
        			}
        		}else {
        			s2=s2.trim();//e.g., type is incorrectly given as "2  L", while it should be "2 L " 
        		}        			
        	}else if(Str.isNumeric(c1) && "NPQLGBEADHT".contains(c2)) {//e.g., type is incorrectly given as "2L", while it should be "2 L"
        		s1=c1+" ";
        		s2=s.substring(1);
        		if("NPDT".contains("c2") && s2.length()==1) {//prompt particle decay record
        			s2=" "+s2;
        		}else if(c2.equals("D") && s.length()>2) {//"2DL", "2DP", document, not continuation record
        			s1=" "+s.charAt(1);
        			s2=s.substring(2);
        		}
        	}else if(s.length()==1) {//NPQLGBEAH record
        		s1="  ";
        		s2=s+" ";
        	}else if (s.length()==2 && c1.equals("D") && "NPDT".contains(c2)) {//delayed-particle record
        		s1="  ";
        		s2=s;
        	}else if( (c1.equals("C")||c1.equals("D")) && "LGBEADHQ".contains(c2)) {//"DL","DE","DG"
                s1=" "+s.charAt(0);
                s2=s.substring(1);
        	}else if( (c2.equals("C")||c2.equals("D")) && s.length()>2 && "LGBEADHQ".contains(s.toUpperCase().charAt(2)+"") ) {
                s1=s.substring(0,2);
                s2=s.substring(2);
        	}
        	
        	if(s2.length()>=2)
        		s2=s2.substring(0,2);
        	else
        		s2=String.format("%-2s", s2);
        	
        	type=s1+s2.toUpperCase();
        }

        return String.format("%5s%4s",NUCID,type);
    }
    
    public static String makeENSDFLinePrefixParticleDecay(String NUCID,String type){
    	String s=type.trim().toUpperCase();
    	if(s.isEmpty())
    		return "";
    	
    	int n=s.length();
    	String c=s.substring(n-1);
    	if(!"NPDT".contains(c))
    		return "";
    	
    	return makeENSDFLinePrefix(NUCID,type);
    }
    
    public static Vector<String> makeENSDFLines(String NUCID,String type,String text) throws Exception{
        String prefix=makeENSDFLinePrefix(NUCID,type);      
        return wrapStringToLines(text, prefix, 80, 5);      
    }
    
    
    public static boolean isUpperCase(char c){
        if(c>='A' && c<='Z')
            return true;
        
        return false;
    }
    
    public static boolean isLowerCase(char c){
        if(c>='a' && c<='z')
            return true;
        
        return false;
    }
    
    public static boolean isUpperCase(String s){
        if(s.trim().length()>0 && s.toUpperCase().equals(s))
            return true;
        
        return false;
    }
    
    public static boolean isLowerCase(String s){
        if(s.trim().length()>0 && s.toLowerCase().equals(s))
            return true;
        
        return false;
    }
    
    public static void debug(String text){
        System.out.println("**Debug**: "+text);
    }

    
    /*
     * create a whitespace string with specified length
     */
    public static String ws(int length)
    {
        if(length<=0)
            return null;
        else
            return String.format("%"+length+"s","");
    }

    /*
     * create a new string of specified length from
     * a string by appending whitespaces at the end
     * of it
     * if specified length is smaller than the length
     * of the old string,return part of the string
     * Similar to StringBuffer setLength() method
     */
    public static String ws(String str,int length) throws Exception
    {
        String tempStr=null;

        if(str==null)
            return "";

        if(str.length()>length)
        {

            throw new Exception("The length "+str.length()+" of the string \""+str+"\" is longer than the specified length="+length+" or specified length less than 0");
            //if(length>0)
            //  tempStr=str.substring(0,length);
            //if(tempStr!=null)
            //  return tempStr;
            //else
                //      return "";
        }
        else if(str.length()==length)
            return str;

        tempStr=str+ws(length-str.length());

        return tempStr;
    }

    public static String wsHTML(int n){
    	String out="";
    	for(int i=0;i<n;i++)
    		out+="&nbsp;";
    	
    	return out;
    }
    
    /*
     * set char value at the specified index 
     * of a string
     * return a new string
     */
    public static String setCharAt(String str,int index,char ch)
    {
        String tempStr=str;
        try{
            tempStr=str.substring(0,index);
            tempStr=tempStr+ch+str.substring(index+1);
        }catch(Exception e){        
        }

        return tempStr;
        
    }

    public static Vector<String> wrapDSID(String dsid,int length){
    	String delims=" :)";//DO NOT USE ,
    	Vector<String> out=wrapString(dsid,length,delims);
    	for(int i=0;i<out.size();i++) {
    		String s=out.get(i);
    		if(s.startsWith(",") && i>0) {
    			s=s.substring(1);
    			out.remove(i);
    			out.add(i,s);
    			
    			s=out.get(i-1);
    			s=s+",";
    			out.remove(i-1);
    			out.add(i-1,s);
    		}
    	}
    	
    	return out;
    }
    
    
    /*
     * length=max length of each line, including prefix and indent
     * prefix: to be added at the beginning of each line; can be used to set the indent of the first line.
     *         It will be always added for the first line and will be also added to other lines if repeatPrefix=true
     * indentAfterFirstLine: indent to be added before the prefix in each, line
     *                       if repeatPrefix=false, it is indent to be added before the text body of each line
     * repeatPrefix: true to repeat the prefix for each line
     * 
     * If repeatPrefix=true and indentAfterFirstLine is set, the total length of each line
     * is len(prefix)+len(indent)+len(text body of each line), which should not exceed length
     * 
     */
    public static String wrapString(String str,int length,String prefix,String indentAfterFirstLine,boolean repeatPrefix) {
    	try {
    		String out="";
    		
    		int prefixLen=prefix.length();
    		int indentLen=indentAfterFirstLine.length();
    		
    		if( (prefixLen+str.length())<=length)
    			return prefix+str;
    		
    		//note that length of text body of the first line is not necessarily the same as those of other lines,
    		//while all other lines should be from wrapping the rest text of input "str" after excluding the first line text body
    		
    		int maxTextLenOfFirstLine=(length-prefixLen);
    		int n=str.indexOf(" ", maxTextLenOfFirstLine+10);
    		String tempStr=str;
    		if(n>0)
    			tempStr=str.substring(0,n);
    		
    		Vector<String> linesV=Str.wrapString(tempStr, maxTextLenOfFirstLine);
    		
    		out="";
    		String line="";
    		if(linesV.size()>0) {
    			line=linesV.get(0);
    			out+=prefix+line+"\n";
    			tempStr=str.substring(line.length());
    		}else {
    			return prefix+str;//should not happen
    		}
    		
        	int maxTextLenAfterFirstLine=0;
        	String tempPrefix="";
    		if(repeatPrefix) {
    			maxTextLenAfterFirstLine=(length-prefixLen-indentLen);
    			tempPrefix=prefix;
    		}else
    			maxTextLenAfterFirstLine=(length-indentLen);
    		
    		if(maxTextLenAfterFirstLine<=20)
    			maxTextLenAfterFirstLine=20;
    		
    		linesV=Str.wrapString(tempStr, maxTextLenAfterFirstLine);
    		
    		
    		for(String s:linesV) {
    			out+=indentAfterFirstLine+tempPrefix+s+"\n";
    		}
    		
    		return out;
    	}catch(Exception e) {
    		
    	}
    	
    	return prefix+str;
    }
    
    public static Vector<String> wrapStringToLines(String str,int length,String prefix,String indentAfterFirstLine,boolean repeatPrefix) {
    	Vector<String> out=new Vector<String>();
    	try {
    		String s=wrapString(str,length,prefix,indentAfterFirstLine,repeatPrefix);
    		String[] temp=s.split("[\\n]+");
    		for(int i=0;i<temp.length;i++) {
    			String line=temp[i];
    			if(line.length()>0)
    				out.add(line);
    		}
    	}catch(Exception e) {
    		
    	}
    	
    	return out;
    }
    
    public static Vector<String> wrapString(String str,int length,String delims){
        return wrapString(str,length,delims,true);
    }
    /** wrap a long string according to the given width, split at given delims.
     *  always use the first delim char to split the string, if no split can be
     *  made to meet the given length, then use the second delim, and so on.
     *  For the next wrap and split, start from the first delim char again.
    */
    public static Vector<String> wrapString(String str,int length,String delims,boolean hasDelimAtEnd){
        int index=-1;
        String s;

        Vector<String> out=new Vector<String>();
        
        if(str==null) return out;  
        s=str.trim();
        
        if(delims==null || delims.isEmpty() || delims.trim().isEmpty())
            delims=" ";

        int offset=1;//by default, when splitting, delim char is put at the end of current substring
        if(!hasDelimAtEnd)
            offset=0;
        
        int goodIndex=-1;
        while(s.length()>length){
            int i=0;
            String s1=s.substring(0,length);
               
            index=s1.lastIndexOf(delims.charAt(i));
            if(index>=0)
            	goodIndex=index;
            
            while(index<length/2 && i<delims.length()-1) { 

                index=s1.lastIndexOf(delims.charAt(i+1));

                if(index>=0 && index>goodIndex)
                	goodIndex=index;
                
                i++;
            }

            if(goodIndex<s1.lastIndexOf("."))
                goodIndex=s1.lastIndexOf(".");
            
            if(s.charAt(length)==' ' || goodIndex<length/2) 
                goodIndex=length;

        	
            goodIndex=goodIndex+offset;
            out.add(s.substring(0, goodIndex));
                        
            s=s.substring(goodIndex,s.length()).trim();

        }
        
        if(s.length()>0)
            out.add(s);
        
        return out;
        
    }
    
    /** wrap a long string according to the given width, delimiter=whitespace*/
    public static Vector<String> wrapString(String str,int length){
        int index;
        String s;

        Vector<String> out=new Vector<String>();
        
        if(str==null) return out;  
        s=str.trim();
        
        
        while(s.length()>length){
            index=s.substring(0,length).lastIndexOf(" ");
            if(index<0) {
                index=s.substring(0,length).lastIndexOf(",");
                if(index<s.substring(0,length).lastIndexOf("."))
                    index=s.substring(0,length).lastIndexOf(".");
            }
            
            if(s.charAt(length)==' ' || index<0) 
                index=length;
            out.add(s.substring(0, index));
            s=s.substring(index,s.length()).trim();
        }
        
        if(s.length()>0)
            out.add(s);
        
        return out;
        
    }
    
    /*
     * wrap a string with specified width and
     * line header
     * @param markPos index of the line mark
     *                in line string
     *                no mark for first line
     */ 
    public static String wrapString(String str,String header,int width,int markPos) throws Exception
    {
         
        //wrap a long string with the specified width and prefix and
        //label each line at label_pos using 0-9 and a-z
        //for ENSDF format, width=80, label_pos=5(start position=0)
        //an example of prefix for a level comment record
        //prefix=' 35AR CL ' with length=9
        // ' 35AR CL'  no label for the first line, for PN comment,use label='2' for the first line
        // ' 35AR2CL'  label='2'
         
        int strLength=0;
        int headerLength=0;
        int i=0;
        char lineNumCh='0';
        String dataStr=null;
        String tempStr=null;

        if(str==null || header==null)
            return null;

        str=str.trim();
        strLength=str.length();

        //Do not trim header!
        headerLength=header.length();

        width=width-headerLength;
        
        String indent="";
        if(header.charAt(headerLength-1)!=' ') {
        	width=width-1;//leaving a space between header and text if there is no space
        	indent=" ";
        }

        if(width<=0)
            throw new Exception("Can't wrap \""+str+"\". Check header!");

            
        if(strLength<=width)
            return header+ws(str,width);

        lineNumCh++;
        while(strLength>width)
        {           
            if(dataStr!=null)
                dataStr+="\n";

            if(lineNumCh>'1') {
                header=setCharAt(header,markPos,lineNumCh)+indent;
                indent="";
            }

            if(str.charAt(width)==' ')
            {
                if(dataStr!=null)
                    dataStr+=header+str.substring(0,width);
                else
                    dataStr=header+str.substring(0,width);

                str=str.substring(width);
            }
            else
            {
                tempStr=str.substring(0,width);
                                
                i=tempStr.lastIndexOf(" ");
                
                if(i<0)
                    i=width;
                
                if(dataStr!=null)
                    dataStr+=header+ws(str.substring(0,i),width);
                else
                    dataStr=header+ws(str.substring(0,i),width);

                str=str.substring(i);
            }
            str=str.trim();
            strLength=str.length();

            if(lineNumCh=='9')
                lineNumCh='a';
            else
                lineNumCh++;
        }

        //last wrap
        header=setCharAt(header,markPos,lineNumCh)+indent;
        dataStr+="\n"+header+ws(str,width);

        return dataStr;
    }

    public static Vector<String> wrapStringToLines(String str,String header,int width,int markPos) throws Exception
    {
        Vector<String> out=new Vector<String>();
        
        String line=wrapString(str,header,width,markPos);
        if(line!=null && !line.isEmpty()) {
            String[] lines=line.split("\n");
            for(int i=0;i<lines.length;i++)
                out.add(lines[i]);
        }
        
        return out;
    }
    
    /*
     * find the index of the first letter in
     * a string
     */
    public static int indexOfLetter(String str)
    {
        int i;
        for(i=0;i<str.length();i++)
        {
            if(Character.isLetter(str.charAt(i)))
                return i;
        }

        return -1;
    }

    /*
     * find the index of the first number in
     * a string
     */
    public static int indexOfNumber(String str)
    {
        int i;
        for(i=0;i<str.length();i++)
        {
            if(Character.isDigit(str.charAt(i)))
                return i;
        }

        return -1;
    }
    
    /*
     * find indexes of the first matching bracket pairs 
     * in a string
     */
    public static int[] indexesOfMatchingBrackets(String str,String leftBracket,int offset){
        ArrayList<String> lbrackets=new ArrayList<String>(Arrays.asList("{","[","(","<"));
        ArrayList<String> rbrackets=new ArrayList<String>(Arrays.asList("}","]",")",">"));
        int[] out=new int[2];
        out[0]=-1;
        out[1]=-1;
        
        int index=lbrackets.indexOf(leftBracket);
        if(str==null || str.isEmpty() || index<0)
            return out;
        
        String rightBracket=(String)rbrackets.get(index);
        int ilb=str.indexOf(leftBracket,offset);
        int irb=-1;
        
        
        int len=leftBracket.length();
        int nBrackets=1;
        if(offset<0)
            offset=0;
        
        for(int i=ilb+len;i<str.length();i++){
            if(leftBracket.regionMatches(0, str, i, len)){
                nBrackets++;
                i=i+(len-1);
            }else if(rightBracket.regionMatches(0, str, i, len)){
                nBrackets--;
                if(nBrackets==0){
                    irb=i;
                    break;
                }
                
                i=i+(len-1);
            }
            
            //System.out.println(ilb+"   "+rightBracket+"  "+nBrackets+" i="+i+"  "+str.charAt(i));

        }
        
        
        out[0]=ilb;
        out[1]=irb;
            
        return out;
    }
    
    public static String removeTabs(String str)
    {
        int i=0;
        if(str==null)
            return null;

        while((i=str.indexOf('\t'))>=0)
        {
            if(i<str.length()-1)
                str=str.substring(0,i)+ws(8-i%8)+str.substring(i+1);
            else
                str=str.substring(0,i)+ws(8-i%8);
        }

        return str;
    }
    
    /*
     * remove anything inside any {}, including the brackets
     */
    public static String removeTextInCurlyBrackets(String s) {
        String out="";
        int i=s.indexOf("{");
        if(i<0)
            return s;
        
        out+=s.substring(0,i);
        
        boolean skip=false;
        
        while(i<s.length()) {
            char c=s.charAt(i);
            if(c=='{') {
                skip=true;
                i++;
                continue;
            }else if(c=='}') {
                skip=false;
                i++;
                continue;
            }
            
            if(!skip)
                out+=c;
            
            i++;
        }
        
        return out;
    }
    
    public static String removeTextInSquareBrackets(String s) {
        String out="";
        int i=s.indexOf("[");
        if(i<0)
            return s;
        
        out+=s.substring(0,i);
        
        boolean skip=false;
        
        while(i<s.length()) {
            char c=s.charAt(i);
            if(c=='[') {
                skip=true;
                i++;
                continue;
            }else if(c==']') {
                skip=false;
                i++;
                continue;
            }
            
            if(!skip)
                out+=c;
            
            i++;
        }
        
        return out;
    }
    
    public static String removeTextInParentheses(String s) {
        String out="";
        int i=s.indexOf("(");
        if(i<0)
            return s;
        
        out+=s.substring(0,i);
        
        boolean skip=false;
        
        while(i<s.length()) {
            char c=s.charAt(i);
            if(c=='(') {
                skip=true;
                i++;
                continue;
            }else if(c==')') {
                skip=false;
                i++;
                continue;
            }
            
            if(!skip)
                out+=c;
            
            i++;
        }
        
        return out;
    }
    
    /*
     * remove zeros at the end of number string
     */
    public static String removeZeros(String str)
    {
        int length;
        int i=0;
        length=str.length();
        for(i=length-1;i>=0;i--)
            if(str.charAt(i)!='0')
                break;
        
        str=str.substring(0, i+1);  
        
        return str;
    }
    
    //return a HTML expression for displaying a string as subscript
    //Feb08
    //Jun
    public static String subscript(String string,String sub){
        return "<HTML><BODY>"+string+"<SUB>"+sub+"</SUB></BODY></HTML>";
    }

    //return a HTML expression for displaying a string as superscript
    //Feb08
    //Jun
    public static String superscript(String string,String sup){
        return "<HTML><BODY>"+string+"<SUP>"+sup+"</SUP></BODY></HTML>";
    }   

    /*
     * wrap of String.substring(beginIndex,endIndex) except that
     * it returns empty string instead of exception when input
     * indexes are invalid
     */
    public String substring(String s,int beginIndex,int endIndex) {
        try {
            return s.substring(beginIndex,endIndex);
        }catch(NullPointerException e) {
            return "";
        }catch(IndexOutOfBoundsException e) {
            if(beginIndex<0)
                beginIndex=0;
            if(endIndex>s.length())
                endIndex=s.length();            
            
            try {
                return s.substring(beginIndex,endIndex);
            }catch(IndexOutOfBoundsException e1) {
                return "";
            }
        }
    }
    
    public static String toString(Vector<String> linesV) {
    	try {
    		String s="";
    		Vector<String> tempV=new Vector<String>();
    		tempV.addAll(linesV);
    		
			s=tempV.get(0);
			tempV.removeElementAt(0);
    		if(linesV.size()<10) {
        		for(String line:tempV)
        			s+="\n"+removeEOL(line);

        		return s;
    		}else {
    			StringBuilder sb=new StringBuilder();
    			sb.append(s);
        		for(String line:tempV)
        			sb.append("\n"+removeEOL(line));
        		
        		return sb.toString();
    		}

    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    public static String toString(String[] lines) {
    	try {
    		Vector<String> linesV=new Vector<String>();
    		for(int i=0;i<lines.length;i++) {
    			linesV.add(lines[i]);
    		}
    		
    		return toString(linesV);
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    /*
     * remove the empty strings at the beginning and the end of a string vectors
     * and return a new vector
     */
    public static Vector<String> trimV(Vector<String> strV){
        Vector<String> out=new Vector<String>();
        out.addAll(strV);
        if(out.size()==0)
            return out;
        
        while(out.size()>0 && out.get(0).trim().isEmpty())
            out.remove(0);

        while(out.size()>0 && out.lastElement().trim().isEmpty())
            out.removeElementAt(out.size()-1);
        
        return out;
    }
    
    public static Vector<String> trimVector(Vector<String> strV){
        return trimV(strV);
    }
    
    /*
     * remove the empty strings at the beginning and the end of a string arrays
     * and return a new array
     */
    public static String[] trimA(String[] strs){
        ArrayList<String> out=new ArrayList<String>();
        int n=strs.length;
        if(n==0)
            return new String[0];
        
        for(int i=0;i<strs.length;i++)
            out.add(strs[i]);

        while(out.size()>0 && out.get(0).trim().isEmpty())
            out.remove(0);

        
        while(out.size()>0 && out.get(out.size()-1).trim().isEmpty())
            out.remove(out.size()-1);
        
        strs=new String[out.size()];
        
        out.toArray(strs);
        
        return strs;
    }
    
    public static String[] trimArray(String[] strs){
        return trimA(strs);
    }
    
    public static String HTMLWrap(String string){

        string=string.replaceAll("<HTML>","");
        string=string.replaceAll("</HTML>","");
        string=string.replaceAll("<BODY>","");
        string=string.replaceAll("</BODY>","");
        
        return "<HTML><BODY>"+string+"</BODY></HTML>";
        
        
    }
    
    public static String htmlToPlainText(String htmlText) {
        if (htmlText == null || htmlText.isEmpty()) {
            return htmlText;
        }
        
        String text = htmlText;
        
        // Convert HTML entities
        text = text.replace("&nbsp;", " ");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&amp;", "&");
        text = text.replace("&quot;", "\"");
        text = text.replace("&apos;", "'");
        
        // Convert line breaks
        text = text.replaceAll("(?i)<br\\s*/?\\s*>", "\n");
        
        // Handle block elements (add newlines)
        text = text.replaceAll("(?i)</p>", "\n");
        text = text.replaceAll("(?i)</div>", "\n");
        text = text.replaceAll("(?i)</li>", "\n");
        text = text.replaceAll("(?i)</tr>", "\n");
        text = text.replaceAll("(?i)<p[^>]*>", "");
        text = text.replaceAll("(?i)<div[^>]*>", "");
        text = text.replaceAll("(?i)<li[^>]*>", "");
        text = text.replaceAll("(?i)<tr[^>]*>", "");
        
        // Remove all other HTML tags but preserve content
        text = text.replaceAll("(?i)<[^>]*>", "");
        
        // Clean up multiple consecutive newlines
        text = text.replaceAll("\n{3,}", "\n\n");
        
        // Trim each line but preserve indentation
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line.replaceAll("\\s+$", "")).append("\n");
        }
        
        return result.toString().trim();
    }
    
    public static void printSystemProperties(){
        java.util.Properties properties=System.getProperties();
        for(Entry<Object, Object> entry:properties.entrySet())
            System.out.println(String.format("%30s : %s", entry.getKey().toString(),entry.getValue().toString()));
    }
    
    public static String dirSeparator(){
        return System.getProperty("file.separator");
    }

	public static String getFileExtension(String filepath) {
		try {
			String extension="";
			int lastDotIndex = filepath.lastIndexOf('.');
			if (lastDotIndex > 0) {
			    extension = filepath.substring(lastDotIndex + 1);
			}
			//extension = FilenameUtils.getExtension(filepath);
			return extension;
		}catch(Exception e) {
			
		}

		return "";
	}


}
