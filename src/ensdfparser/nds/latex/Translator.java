/*
 * Translater.java
 *
 */

package ensdfparser.nds.latex;

import java.io.*;
import java.util.*;

import ensdfparser.ensdf.Nucleus;
import ensdfparser.nds.ensdf.UserDictionary;
import ensdfparser.nds.util.Str;
import ensdfparser.nds.util.Word;
import ensdfparser.nds.util.numanl;

import java.net.*;

/**
 * Translate the rich text format found in ENSDF to LaTeX.
 *
 * The translation files should be inserted into the final JAR 
 * executable.  If you put them in the build/classes/ directory
 * NetBeans will do this for you.
 *
 * Produced latex require AMS Math package, stick the code
 * \ usepackage{amsmath} in your files.
 */
public class Translator {
    // REACTION key,value pairs
    protected static String[] Rkey,Rvalue;
    // NUCLEIDE key,value pairs
    protected static String[] Nkey,Nvalue;
    // WORD key,value pairs
    protected static String[] Wkey,Wvalue;
    // LATEX key,value pairs
    protected static String[] Tkey,Tvalue;
    
    private static boolean hasInit=false;
    
    private static ArrayList<String> operators=new ArrayList<String>(Arrays.asList("LT","LE","GT","GE","AP"));
    
    public static boolean canBeTranslatedAsDSID=true;//set for translating DSID only, changed only in procGenCom()
    
    private static boolean isTextFormat=false;
    
    protected static String line="";
    
    public Translator()throws Exception{
        init();
    }
    
    
    /**
     * Load latex translation file.
     */
    public static void initTex(InputStream is) throws IOException{
        BufferedReader br= new BufferedReader(
            new InputStreamReader(is));
        String line;
        Vector<String> k = new Vector<String>();
        Vector<String> v = new Vector<String>();
        while(true){
            line=br.readLine();
            if (line==null) break;
            if (line.length()<20) continue;
            if (line.charAt(0)=='!') continue;
            String ks,vs;
            ks = line.substring(0,19).trim();
            vs = line.substring(19,line.length()).trim();
            if (ks.length()>0 && vs.length()>0){
                k.add(ks);
                v.add(vs);
            }
        }
        br.close();
        Tkey   = new String[v.size()];
        Tvalue = new String[v.size()];
        k.copyInto(Tkey);
        v.copyInto(Tvalue);
    }
    /**
     * Load dictionary files and setup all needed variables.
     * nsddic table in ensdf database
     */
    
    public static void initEns()throws IOException{
            initEnsText();
    }
    
   /**
     * Load dictionary files and setup all needed variables.
     * Use user supplied dictionary file.
     */
    public static void initEnsText(String filePath) throws IOException{
    	InputStream is=getInputStream(filePath);

        BufferedReader br= new BufferedReader(
            new InputStreamReader(is));
        
        String sreac = br.readLine();
        String snucl = br.readLine();
        String sword = br.readLine();
        int rbeg = Integer.parseInt(sreac.substring(11,16).trim());
        int rend = Integer.parseInt(sreac.substring(16,22).trim());
        int nbeg = Integer.parseInt(snucl.substring(11,16).trim());
        int nend = Integer.parseInt(snucl.substring(16,22).trim());
        int wbeg = Integer.parseInt(sword.substring(11,16).trim());
        int wend = Integer.parseInt(sword.substring(16,22).trim());
        String line;
        int x;
        Vector<String> k = new Vector<String>();
        Vector<String> v = new Vector<String>();
        for (x=0; x<(rend-rbeg+1); x++){
            line=br.readLine();
            String p1 = line.substring(0,19).trim();
            String p2 = line.substring(19,line.length()).trim();
            if (p2.equals(".....")) continue;
            k.add(p1);
            v.add(p2);
        }
        Rkey   = new String[v.size()];
        Rvalue = new String[v.size()];
        k.copyInto(Rkey);
        v.copyInto(Rvalue);
        k = new Vector<String>();
        v = new Vector<String>();
        for (x=0; x<(nend-nbeg+1); x++){
            line=br.readLine();
            String p1 = line.substring(0,19).trim();
            String p2 = line.substring(19,line.length()).trim();
            if (p2.equals(".....")) continue;
            
            k.add(p1);
            v.add(p2);
        }
        Nkey   = new String[v.size()];
        Nvalue = new String[v.size()];
        k.copyInto(Nkey);
        v.copyInto(Nvalue);
        k = new Vector<String>();
        v = new Vector<String>();
        for (x=0; x<(wend-wbeg+1); x++){
            line=br.readLine();
            String p1 = line.substring(0,19).trim();
            String p2 = line.substring(19,line.length()).trim();
            if (p2.equals(".....")) continue;
            k.add(p1);
            v.add(p2);
        }
        Wkey   = new String[v.size()];
        Wvalue = new String[v.size()];
        k.copyInto(Wkey);
        v.copyInto(Wvalue);
        for (int i=Wkey.length-1; i>0; i--){
            for (int j=0; j<i; j++){
                if (Wkey[j].length()<Wkey[j+1].length()){
                    String tmp = Wkey[j];
                    Wkey[j]=Wkey[j+1];
                    Wkey[j+1]=tmp;
                    tmp = Wvalue[j];
                    Wvalue[j]=Wvalue[j+1];
                    Wvalue[j+1]=tmp;
                }
            }
        }
        br.close();
    }
     
    /**
     * Load default dictionary files and setup all needed variables.
     * Use user supplied dictionary file.
     */
    public static void initEnsText() throws IOException{
    	initEnsText("ensdf_dic2.dat");
    }
    
    @SuppressWarnings("unused")
    public static boolean hasInit0() {
        try {
            String s="";
            
           // REACTION key,value pairs
            s=Rkey[0];
            s=Rvalue[0];
            // NUCLEIDE key,value pairs
            s=Nkey[0];
            s=Nvalue[0];
            
            // WORD key,value pairs
            s=Wkey[0];
            s=Wvalue[1];
            
            // LATEX key,value pairs
            s=Tkey[0];
            s=Tvalue[0];
            
            return true;
        }catch(Exception e) {
            
        }

        return false;
    }
    
    public static boolean hasInit() {return hasInit;}
    
    /**
     * Load dictionary files and setup all needed variables.
     */
    public static void init() throws IOException{
        if(hasInit)
            return;

        initEns();

    	InputStream is=getInputStream("latex_dic.dat");
        if (is!=null){
            try{
                initTex(is);
                hasInit=true;
            }catch(Exception e){
                System.out.print("Error: ");
                System.out.println(e.getLocalizedMessage());
            }
        }else{
            System.out.println("Error: LaTeX translation file not found.");
        }

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
    		is = Translator.class
    				.getClassLoader()   	    	        
    				.getResourceAsStream(filePath);
    		
            if(is==null)
            	System.out.println("Error: Translation file not found: "+filePath);
        }
        
    	return is;
    }
    
    /**
     * Load a user-defined ENSDF dictionary file and setup all needed variables.
     */
    public static void loadENSDFDict(String filePath) throws Exception{
    	if(filePath.trim().length()==0){
    		System.out.println("using ENSDF translation dictionary from ensdf_dic2.dat");
    		initEnsText();
    	}
    	else{
        	System.out.println("using ENSDF translation dictionary from "+filePath);
            initEnsText(filePath);
    	}
    }
    
    /**
     * Load a user-defined ENSDF dictionary file and setup all needed variables.
     */
    public static void loadLatexDict(String filePath) throws Exception{
    	String tempPath=filePath;
    	
    	if(filePath.trim().length()==0){
        	System.out.println("using LaTeX translation dictionary from latex_dic.dat");
            tempPath="latex_dic.dat";
    	}else{
        	System.out.println("using LaTeX translation dictionary from "+filePath);
            tempPath=filePath.trim();
    	}
    	
        InputStream is=getInputStream(tempPath);
        if (is!=null){
            try{
                initTex(is);
            }catch(Exception e){
                System.out.print("Error: ");
                System.out.println(e.getLocalizedMessage());
            }
        }else{
            System.out.println("Error: LaTeX translation file not found: "+tempPath);
        }

    }
    
    /**
    *first turns "C" comments into "c" comments
     * then converts to latex 
     * @throws Exception 
     * 
     */
    public static String processDot(String text) throws Exception{
        text=process(text);
        if(text.charAt(text.length()-1)!='.')text+=".";
        return text;
    }

    public static String process(String text) throws Exception{
        String text3=process(text,true);
        return text3;
    }
    /**Same as above but with a few options 
     * @throws Exception */
    public static String process(String text,boolean cap) throws Exception{
        //debug
        //if(text.contains("BAND"))
        //	System.out.println("In Translator: process 1: text="+text);
        
        canBeTranslatedAsDSID=true;
    	
        String text2 = procGenCom(text,0,cap);
        
        //debug
        //if(text.contains("BAND"))
        //	System.out.println("In Translator: process 2: text2="+text2);
        
        String text3 = translate(text2);
        
        ////debug
        //if(text.contains("D,P"))
        //	System.out.println("In Translator: process 3: text3="+text3);
        
        return text3;
    }
    /** match s1 against a substring of s2.  Use _ as a signifier
    /// of whitespace or  comma or period. */
    protected static boolean match(String s1,String s2,int ch){
        if (s1==null || s2==null || ch<0) return false;
        if (s2.length() < (s1.length()+ch)) return false;
        for (int x=0; x<s1.length(); x++){
            if (s1.charAt(x)=='_'){
                char c = s2.charAt(x+ch);
                if (c==' '||c=='.'||c==','||c=='\n'
                    ||c=='\t'||c=='\r'){
                }else return false;
            }else if (s1.charAt(x)!=s2.charAt(x+ch))
                return false;
        }
        return true;
    }
    
    private static String printTXT(String txt,boolean inMath) {
    	if(!inMath){
    		if(txt.contains("-") &&  txt.contains("+"))//minus sign "-" is printed correctly in output only in math mode. It is printed as a short dash in text mode.
    			txt=txt.replace("-","{\\textminus}");  //textminus prints a regular minus sign in text mode, only when both "+" and "-" show up, e.g., 12.3 {I+5-4}
    	}else{
    		if(txt.equals("-") || isTextFormat) {//textit, textbf, underline: text is already in text format, no need to put it in textnormal
    			isTextFormat=false;
    		}else{
        		if(txt.indexOf("-")>=0 && !txt.contains("--"))
        			txt=txt.replace("-", "$-$");              		
        		

        		txt= "\\textnormal{"+txt+"}";
    		}
    	}   
    	
    	return txt;
    }
    public static String translate(String text){
    	return translate(text,0,text.length()-1);
    }
    
    /**
     * Converts 'c' comments into latex output
     * If the users are restricted
     * to only using the 'c' comments
     * (which I would highly recommend) then this is the only
     * function you need to call.
     *
     * The produced latex code relies on the "amsmath" package. It
     * will compile withought it but won't look quite right.
     *
     * The latex produced here is far more verbose then you 
     * would get by hand translation.
     * 
     * 
     */
    @SuppressWarnings("unused")
	public static String translate(String text,int start,int end) {

        int index=start,x;
        // keep track of nesting constucts like {+a{-b}}
        int nest=0;
        String output="";//\\ensuremath{";
        String txt="";
        char c=0;
        
        // math nesting
        boolean inMath=false,endMath=false;
        boolean isAfterSuper=false,isAfterSub=false;//mark the pos right after super- or sub-script
        
		boolean isSub=false,isSuper=false;
        int mathnest=-1,scriptNest=-1;
        int nsingleQuote=0;
        int ndoubleQuote=0;
        
        int iFirstMathNest=-1;//index of the first math "{" among all nests for an expression containing "{"
        
        //System.out.println("In translator0: text="+text+" output="+output+ "endMath="+endMath+" inMath="+inMath+" txt="+txt+" start="+start+" end="+end);
        
        if(start>end)
        	return text;
        
        isTextFormat=false;
        
        while (index<text.length() && index>=start && index<=end){
        	//debug
        	//if(text.contains("{+56}Fe")) 
        	//System.out.println("In Translator 385: translate: ch="+ch+" "+text.charAt(ch)+" text="+text+" inMath="+inMath+" nest="+nest+" mathnest="+mathnest+" txt="+txt+" output="+output);
        	
            c=text.charAt(index);
            if((c=='J'||c=='K') && "|p".regionMatches(0,text,index+1,2)){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }
                
                if (endMath){
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }
                
                if(inMath)
                    output+=c+"^{\\pi}";
                else
                    output+="\\ensuremath{"+c+"^{\\pi}}";
                
                index+=3;
                continue;        	    
        	}
            
        	if ("{+".regionMatches(0,text,index,2)){
            //if ("{+".regionMatches(0,text,ch,2)&&scriptNest<=0){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    //output += "\\textnormal{"+txt+"}";
                    txt="";
                }
                endMath=false;
                if (!inMath){
                    output+="\\ensuremath{";
                    inMath=true;
                    endMath=false;
                    mathnest=nest+1;
                    
                    iFirstMathNest=nest;
                }else
                	mathnest++;
      
                
                //code to prevent double subscripting
                if(isAfterSuper) {
                	//output+="}\\ensuremath{";
                	output+="~";
                }
                
                isSuper=true;
                if(scriptNest>0)
                	scriptNest++;
                else
                	scriptNest=1;
                
                output+="^{";
                nest++; index+=2;
                continue;
            }
            
        	if ("{-".regionMatches(0,text,index,2)){
            //if ("{-".regionMatches(0,text,ch,2)&&scriptNest<=0){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    //output += "\\textnormal{"+txt+"}";
                    txt="";
                }
                endMath=false;
                if (!inMath){
                    output+="\\ensuremath{";
                    inMath=true;
                    endMath=false;
                    mathnest=nest+1;
                    
                    iFirstMathNest=nest;
                }else
                	mathnest++;
                               
                //code to prevent double subscripting
                if(isAfterSub) {
                	//output+="}\\ensuremath{";
                	output+="~";
                }
                
                isSub=true;
                if(scriptNest>0)
                	scriptNest++;
                else
                	scriptNest=1;
                
                output+="_{";
                nest++; index+=2;
                continue;
            }
            if(" ".regionMatches(0,text,index,1)||"=".regionMatches(0, text, index, 1)){
            	/*
                int l=checkXDX(text.substring(ch)).length();            	
                if(l>0){
                    text=text.substring(ch,ch+1)+text.replace(text.substring(ch+1,ch+l-3), checkXDX(text.substring(ch)).substring(1));
                    ch++;
                    continue;
                }
                */
            }
            
            if("{I".regionMatches(0,text,index,2)||"{i".regionMatches(0,text,index,2)){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }
                if(scriptNest>0) scriptNest++;
                
        
                if (endMath){
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }
                
                isTextFormat=true;
                
                output+="\\textit{";
                nest++;
                index+=2;
                
                continue;
            }
            if("{B".regionMatches(0,text,index,2)||"{b".regionMatches(0,text,index,2)){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }
                if(scriptNest>0) scriptNest++;
                
                if (endMath){
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }
                
                isTextFormat=true;
                output+="\\textbf{";
                nest++;
                index+=2;
                continue;
            }
            if("{U".regionMatches(0,text,index,2)||"{u".regionMatches(0,text,index,2)){
           
            	//System.out.println(text);
            	
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }
                if(scriptNest>0) scriptNest++;
                
                if (endMath){
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }
                
                isTextFormat=true;
                output+=" \\underline{";
                nest++;
                index+=2;
                continue;
            }
            if("^{".regionMatches(0, text, index, 2)){//print text between "^{" and matching "}" in LaTeX math mode, text=LaTeX text
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }
                
                int p=Str.indexesOfMatchingBrackets(text,"{", index)[1];
        
                //System.out.println("###text="+text+" ###p="+p);
                
                if(p>(index+2)){
                	String s=text.substring(index+2, p);
                	
                    //check if s is accent. Note that accent like "\~{a}" will not work in math mode
                	
                	if(isAccent(s)){
                		output+=s;
                	}else {
                		output+="\\ensuremath{"+s+"}";
                	}
                	
                	
                	//System.out.println("****"+s+"*****"+output);
                	
                	index=p+1;
                	continue;
                }
            }
            if("^[".regionMatches(0, text, index, 2)){//print text between "^[" and matching "]" as is in LaTeX, text=LaTeX text
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }
                
                int p=Str.indexesOfMatchingBrackets(text,"[", index)[1];
        
                if(p>(index+2)){
                	String s=text.substring(index+2, p);
                	
                	output+=s;
                	
                	//System.out.println("****"+s+"*****"+output);
                	
                	index=p+1;
                	continue;
                }
            }
            
            if("\"".regionMatches(0, text, index, 1)){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }
                
                if(index>1 && text.charAt(index-1)==' ')
                	ndoubleQuote=0;
                else
                	ndoubleQuote=1;
                
            	if(ndoubleQuote==0){
            		txt+="``";
            		ndoubleQuote++;
            	}
            	else{
            		txt+="''";
            		ndoubleQuote--;
            	}
            	
            	index++;
            	continue;
            	
            }
            
            // for |" translated as \\ensuremath{\bar{ and put the first letter following it in brackets.
            //NOTE that in the translation in the dictionary, the brackets are open and need to be closed here.
            if("|\"{".regionMatches(0,text,index,3)){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    //output += "\\textnormal{"+txt+"}";
                    txt="";
                }
                endMath=false;
                if (!inMath){
                    output+="\\ensuremath{";
                    inMath=true;
                    endMath=false;
                    mathnest=nest+1;
                    
                    iFirstMathNest=nest;
                }
                //code to prevent double subscripting
                //if(isSuper) output+="}\\ensuremath{";
                //isSuper=true;
                //scriptNest=1;
                
                if(scriptNest>0) scriptNest++;
                
                output+="\\bar{";
                nest++; index+=3;
                continue;
            }
            if("|%{".regionMatches(0,text,index,3) || "|6{".regionMatches(0,text,index,3)){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    //output += "\\textnormal{"+txt+"}";
                    txt="";
                }
                endMath=false;
                if (!inMath){
                    output+="\\ensuremath{";
                    inMath=true;
                    endMath=false;
                    mathnest=nest+1;
                    
                    iFirstMathNest=nest;
                }
                //code to prevent double subscripting
                //if(isSuper) output+="}\\ensuremath{";
                //isSuper=true;
                //scriptNest=1;
                
                if(scriptNest>0) scriptNest++;
                
                output+="\\sqrt{";
                nest++; index+=3;
                continue;
            }
            if("|%[".regionMatches(0,text,index,3) || "|6[".regionMatches(0,text,index,3)){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    //output += "\\textnormal{"+txt+"}";
                    txt="";
                }
                endMath=false;
                if (!inMath){
                    output+="\\ensuremath{";
                    inMath=true;
                    endMath=false;
                    mathnest=nest+1;
                    
                    iFirstMathNest=nest;
                }
                //code to prevent double subscripting
                //if(isSuper) output+="}\\ensuremath{";
                //isSuper=true;
                //scriptNest=1;
                
                output+="\\sqrt[";
                //nest++; 
                index+=3;
                continue;
            }
            
            //translate single quote in (e,e') and (e,e'|g) to prime
            if("'".regionMatches(0,text,index,1)){
            	
            	boolean inDSID=false;
            	
            	//check if it is a prime symbol in a reaction DSID
            	
            	//method 1
            	List<String> list=Arrays.asList("')","'|g","'n)","'p)","'|a)","'2p","'2n","'3p","'3n");
            	ArrayList<String> primes=new ArrayList<String>(list);
            	for(int i=0;i<primes.size();i++){
            		if(text.indexOf(primes.get(i), index)==index){
            			inDSID=true;
            			break;
            		}
            			
            	}
            	
                if(index>1 && text.charAt(index-1)==' ')
                	inDSID=false;
                
            	//method 2
            	//int p0=text.indexOf('(',ch);
            	//int p1=text.indexOf(')',ch);
            	//int p2=text.indexOf(' ',ch);           	
            	//if(p1>ch && (p2<0 || p2>p1) && (p0<0 || p0>p1))
            	//	inDSID=true;
            		
            	if(inDSID){
                    if (txt.length()>0){
                        output+=printTXT(txt,inMath);
                        txt="";
                    }

                    if (endMath){
                        inMath=false;
                        endMath=false;
                        mathnest=-1;
                        iFirstMathNest=-1;
                        output+="}";
                    }
                    if(inMath)
                    	output+="'";
                    else
                    	output+="\\ensuremath{'}";
                    
                    index++;
                    continue;
            	}else{
                    //if (txt.length()>0){
                    //    output+=txt;
                    //    txt="";
                    //}
                    
                    if(index>1 && text.charAt(index-1)==' '){
                		txt+="`";
                		nsingleQuote++;
                    }else if(nsingleQuote>0){
                		txt+="'";
                		nsingleQuote--;
                    }else{
                    	txt+="$'$";
                    }
         
                	
                	index++;
                	continue;
            	}
            	

            }
            
            //debug
            //System.out.println("In Translator line 819: text="+text+"   "+isPI(text,index)+"   "+text.charAt(index)+" txt="+txt+" index="+index);
            
            if(isPI(text,index)){//translate JPI in comments

            	char prevChar=0,nextChar=0;
            	String bl="",br="";
            	
            	if(index>0) 
            		prevChar=text.charAt(index-1);
            	if(index<text.length()-1)
            		nextChar=text.charAt(index+1);           		
            	           	
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }

                if (endMath){
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }
                
                //debug
                //System.out.println("In translator line 500: prevC="+prevChar+" nextChar="+nextChar+" output="+output+" endmath="+endMath+" inMath="+inMath);
                
                if(prevChar=='(' && nextChar==')' && output.charAt(output.length()-1)=='('){
                	bl="(";
                	br=")";
                	output=output.substring(0, output.length()-1);
                	
                }
                if(inMath)
                	output+="^{"+bl+text.charAt(index)+br+"}";
                else
                	output+="\\ensuremath{^{"+bl+text.charAt(index)+br+"}}";
                
                index++;
                if(br.length()==1)
                	index++;
                
                continue;
            }
            
            //System.out.println("1 Text="+text+" output="+output);
            
            if(text.charAt(index)=='\\' && index<text.length()-1 && (text.charAt(index+1)=='{'||text.charAt(index+1)==' ')){
            	index++;
            	continue;
            }
            		
            if (index<text.length() && text.charAt(index)=='{'){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    //output += "\\textnormal{"+txt+"}";
                    txt="";
                }
                if (endMath){
                    isSuper=false;
                    isSub=false;
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }else if (inMath){
                    mathnest++;
                }
                
                if(scriptNest>0) scriptNest++;
                
                output+="{";
                nest++; index++;
                
                //System.out.println("In Translator line 844: nest="+nest+" scriptNest="+scriptNest+" mathNest="+mathnest+" output="+output+" text="+text+" inMath="+inMath+" endMath="+endMath);
                
                continue;
            }
            
            
            //System.out.println("2 Text="+text+" output="+output);
            
            if (index<text.length() && text.charAt(index)=='^'){
                if (txt.length()>0){
                    output+=printTXT(txt,inMath);
                    txt="";
                }

                if (endMath){
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }
                if(inMath){
                	if(index<text.length()-1 && text.charAt(index+1)=='{')
                		output+="\\hat";
                	else
                		output+="\\hat{}";
                }
                else
                	output+="\\^";
                
                index++;
                continue;
            }
            
            //System.out.println("3 Text="+text+" output="+output);
            
            if (index<text.length() && text.charAt(index)=='}'){

                if (txt.length()>0){
                	
                	/*
                	//debug
                    if(text.contains("He,d) could be a doublet")) {
                	System.out.println("In Translator line 967: nest="+nest+" scriptNest="+scriptNest+" mathNest="+mathnest+" output="+output);
                    System.out.println("      isTextFormat="+isTextFormat+"###	 text="+text+" inMath="+inMath+" endMath="+endMath+" txt="+txt);
                    }
                    */
                	
                	if(!inMath){
                		if(txt.contains("-") &&  txt.contains("+"))//minus sign "-" is printed correctly in output only in math mode. It is printed as a short dash in text mode.
                			txt=txt.replace("-","{\\textminus}");  //textminus prints a regular minus sign in text mode, only when both "+" and "-" show up, e.g., 12.3 {I+5-4}
                		
                		output+=txt;

                	}   
                	else{
                		if(txt.equals("-") || isTextFormat) {//textit, textbf, underline: text is already in text format, no need to put it in textnormal
                			output+=txt;
                			isTextFormat=false;
                		}else{
                    		if(txt.indexOf("-")>=0 && !txt.contains("--"))
                    			txt=txt.replace("-", "$-$");              		
                    		

                    		output += "\\textnormal{"+txt+"}";
                    		
                    		//System.out.println(output+"  txt="+txt);
                		}
                	}
                	
                    txt="";
                }
                
                //endMath, inMath for sup and sub
                //inMath : set true in sup or sub, and set false when reaching a non-sup or non-sub "{" see above
                //         or after closing "{I" or similar others, which is here
                //endMath: set false in sup or sub or after reading or closing other "{"
                //         set true when a "{" in sup or sub is closed, see below. It will be kept true until a non-sup or non-sub "{" is reached or
                //         "{" and similar others are closed, or a regular text is read after a "{" in sub or sup is closed (end=true) (see below)
                if (endMath){
                    inMath=false;
                    endMath=false;
                    mathnest=-1;
                    iFirstMathNest=-1;
                    output+="}";
                }
                
                //nest: for all {} pairs, +1 for "{", -1 for "}"; when=0, all closed;
                //mathnest: for math-mode {} pairs, (mathnest-nest)=unclosed  math "{" at this point, 
                //          >=0 meaning there is no non-math "{" to close and all that type of "{" should have been closed at this point;
                //          if>0, "}" at this point is to close a math-mode "{";
                //scriptNest: for {} pairs only in sup- or sub-script

                //iFirstMathNest is the index of the first math "{" among all nests.
                //So the "}" that matches it should be the "iFirstMathNest"th "}" counted from the last one in the remaining nests,
                //that means the last n=iFirstMathNest nests are all non-math "}". So when the number of remaining nests decreases to iFirstMathNext+1,
                //the "}" at that point is to match the first math "{" and then math mode ends.
                if (inMath && nest<=iFirstMathNest+1){
                    endMath=true;//end math of the closed "{}", but still in math mode inMath=true
                }
                
                //System.out.println("In Translator line 926: nest="+nest+" scriptNest="+scriptNest+" mathNest="+mathnest+" output="+output+" text="+text+" inMath="+inMath+" endMath="+endMath);
                
                // If nest<=0 the closing bracket is omitted to
                // ensure latex compilation. It'll just look wrong.
                if (nest>0){
                    output+="}";
                    nest--;
                    
                    if(nest==0 && isTextFormat)
                    	isTextFormat=false;
                }
                
                if(scriptNest>0){
                	scriptNest--;
                	
                	if(scriptNest==0) {//right at close of sup or sub
                		if(isSuper)
                			isAfterSuper=true;
                		if(isSub)
                			isAfterSub=true;
                	}
                }
                index++;
                continue;
            }
            
            //System.out.println(" text="+text+" #CHAR="+text.charAt(index)+"# isAfterSub="+isAfterSub+" endMath="+endMath+" output="+output+" txt="+txt);
            
            //translate regular text
            if (endMath){
                inMath=false;
                endMath=false;
                isSuper=false;
                isSub=false;
                
                isAfterSuper=false;
                isAfterSub=false;
                
                mathnest=-1;
                iFirstMathNest=-1;
                output+="}";
            }
            boolean match=false;
            for (x=0; x<Tkey.length; x++){
                if (Tkey[x].regionMatches(0,text,index,Tkey[x].length())){
                    if (txt.length()>0){
                        output+=printTXT(txt,inMath);
                        //output += "\\textnormal{"+txt+"}";
                        txt="";
                    }
                    
                    String key=Tkey[x];
                    String val=Tvalue[x];
                    boolean isGoodMatch=true;
                    
                    if((index+Tkey[x].length()-1)>end) {//e.g., "...Adopted levels", end index is at "d" in "Adopted", 'Adopted levels' as a whole has a match
                    	String tempS=text.substring(index,end+1);
                    	if(key.charAt(tempS.length())==' ' && key.length()==val.length() && val.charAt(tempS.length())==' ') {
                    		key=key.substring(0,tempS.length());
                    		val=val.substring(0,tempS.length());
                    	}else
                    		isGoodMatch=false;
                    }
                    
                    if(isGoodMatch) {
                        output += val;
                        
                        index += key.length();
                        match=true;                   
                    
                        //System.out.println("4.5 index="+index+" end="+end+" Text="+text+" \n## output="+output+"\n@@ Tkey[x]="+Tkey[x]+" Tvalue[x]="+Tvalue[x]);
                        
                        break;
                    }

                }
            }
            
            if (match) continue;
            
            if(index<text.length()) {
            	c=text.charAt(index);
            		
            	if(c=='-'){
                	char prevChar=0;
                	char nextChar=0;
                	boolean isMinusSign=false;
                	
                	if(index>0)
                		prevChar=text.charAt(index-1);
                	if(index<text.length()-1)
                		nextChar=text.charAt(index+1);
                	
                	if(prevChar==' ' && nextChar==' ')
                		isMinusSign=true;
                	else if(prevChar=='(' && nextChar==')')
                		isMinusSign=true;
                	else if(prevChar=='[' && nextChar==']')
                		isMinusSign=true;
                	else if(Character.isLetter(prevChar) || Character.isLetter(nextChar)){//126.5-keV, where '-' is a hyphen not a minus sign
                		isMinusSign=false;
                		String ops="=><~?";
                		int i=index-1;
                		char op=' ';
                		while(i>=0){//for exceptions, like, =100-I|b{+-}(g.s.), where '-' is minus not hyphen
                			char cc=text.charAt(i);
                   			if(cc==' ' || (index-i)>30)
                				break;
                   			
                			if(ops.indexOf(cc)>=0){
                				op=cc;
                				break;               		
                			}
                 			
                			i--;
                		}
                		
                		if(op!=' ')
                			isMinusSign=true;

                	}
                	else if(Character.isDigit(prevChar)!=Character.isDigit(nextChar)){
                		isMinusSign=true;
                		
            			int i=index-2;
            			String s="";
            			while(i>=0){
            				if(!Character.isDigit(text.charAt(i)) && text.charAt(i)!='.') {//123.4-, 125.4- and 126.5-keV, where '-' in 123.4- and 125.4- is a hyphen not a minus sign
                				s=text.substring(i+1,index);
            					break;
            				}
            				
            				i--;
            			}
            			int count=(index-1)-i;
            			
            			if((count>=3||Str.isNumeric(s)) && (nextChar==',' || nextChar==' ' || nextChar=='|'))//assuming J<100, that is, 100-,101- are not JPI values	
            				isMinusSign=false;
                        
            			//System.out.println("s="+s+Str.isNumeric(s)+" isMinusSign="+isMinusSign+" i="+i+" prevChar="+prevChar+" nextChar="+nextChar+"  "+(nextChar==',')+" text="+text);
            			//System.out.println(" count="+count+" s="+s+"$"+isMinusSign+" inMath="+inMath);
                	
                	}else if(prevChar=='=')
                		isMinusSign=true;
                	
                	//System.out.println("isMinusSign="+isMinusSign+"  prevChar="+prevChar+" nextChar="+nextChar+"   "+(nextChar==',')+" text="+text);
        			
                	if(isMinusSign && !inMath)
                 	    txt +="{\\textminus}";
                	else{
                		txt +=c;
                	}
            	}
            	else
            		txt += c;
            }
            index++;
        }
        if (txt.length()>0)
            output+=printTXT(txt,inMath);
        
        //System.out.println("In translator3: text="+text+" output="+output+ "endMath="+endMath+" inMath="+inMath+" txt="+txt);
        
        // add some closing brackets if needed
        for ( ; nest>0; nest--) output+="}";
        if (inMath) output+="}";
        
        output=output.replace("\\ensuremath{}", "");
        
        output=Str.fillLongSpace(output);
        
        return output;
    }

    //For implicit comments
    public static String impComVal(String x, String dx){
        if(dx==null||dx.isEmpty())return x;
        if(isNumericUnc(dx.trim().charAt(0)))return x+"  {I"+dx+"}";
        else return (dx+" "+x);
    }
    
    public static boolean isAccent(String s) {
        // A combined string of all possible accent-trigger characters
        String accents = "~'`^\"=.Hvucdrbk";//\ here is to escape "
        s=s.trim();
        if(s.startsWith("\\")&& s.length()>1) {
        	//this is single "\" not a double "\\". Here the 
        	//first "\" is to escape the second "\"
        	if(accents.indexOf(s.charAt(1)+"")>=0) {
        		return true;
        	}
        }
        		
        return false;
    }
    
    public static boolean isNumericNum(char x){
        char[] nums={'1','2','3','4','5','6','7','8','9','0','-','+','E','(',')','.','x'};
        for(int i=0;i<nums.length;i++){if(nums[i]==x)return true;}
        return false;
    }

    public static boolean isNumericUnc(char x){
        char[] nums={'1','2','3','4','5','6','7','8','9','0','-','+'};
        for(int i=0;i<nums.length;i++){if(nums[i]==x)return true;}
        return false;
    }

    public static boolean containsDigits(String x){
        if(x.contains("0")||x.contains("1")||x.contains("2")||x.contains("3")||
                x.contains("4")||x.contains("5")||x.contains("6")||x.contains("7")||
                x.contains("8")||x.contains("9"))return true;
        else return false;
    }

    public static int unitLength(String s){
        String[] units={" S ", " M ", " H ", " D ", " Y ", " MS ", " US ", " MEV ", " KEV ",
         " EV ", " NS ", " PS ", " FS ", " MIN ", " MB ", " KOE ", " UG ", " MG ", " FM ",
          " MILLI ", " CM ", " CM**2 ", " CM2 ", " FM**2 ", " SR ", " B ", " AS "," DEG ",
          " min ", " |ms "," eV ", " |mg "," m ", " cm{+2} ", " cm{+2} ", " fm{+2} "," |' "};
        for(int i=0;i<units.length;i++){
            if(s.toLowerCase().contains(units[i].toLowerCase()))return (units[i].length()-1);
        }
        return 0;
    }

    public static String checkXDX(String line){
        int ch=0;
        int d=0;
        for(int i=1;i<line.length();i++){
            ch=i;
            if(!isNumericNum(line.charAt(ch)))break;
        }
        if(line.charAt(ch)!=' '||!containsDigits(line.substring(0,ch)))
        	return "";
        
        String temp=line.substring(0,ch).replace("(", "");
        temp=temp.replace(")","").trim();
        if(!Str.isNumeric(temp))
        	return "";
        
        if(ch+8<=line.length())
        	ch+=unitLength(line.substring(ch,ch+8));
        else 
        	ch+=unitLength(line.substring(ch));
        
        d=ch;
        for(int i=ch+1;i<line.length();i++){
            ch=i;
            if(!isNumericUnc(line.charAt(ch)))
            	break;
        }

        if(ch>=line.length()||!containsDigits(line.substring(d,ch)))
        	return "";
        
        if(!(line.charAt(ch)==' '||line.charAt(ch)=='.'||line.charAt(ch)==','||line.charAt(ch)==';'))                
        	return "";
        
        if(line.charAt(ch)=='.'&&ch<line.length()-1&&Character.isDigit(line.charAt(ch+1)))
        	return "";
        
        line=line.substring(0,ch);

        StringBuffer sb= new StringBuffer(line);
                sb.insert(d+1,"{I");
        line=sb.toString();

        line+="}";
        return line;
    }

    /// Nice printable version of standard ensdf halflife record
    public static String halfLife(String s)throws java.io.IOException{
        String val="",unt="",unc="";
        String[] part = s.split("[\\ ]+");
        if (part.length>0)
            val = part[0];
        if (part.length>1)
            unt = part[1];
        if (part.length>2)
            unc = part[2];
        
       return halfLife(val.trim(),unc.trim(),unt.trim());
    }
    
    
    public static String halfLife(String ts,String dts,String tus)throws java.io.IOException{
        return printNumber(ts,dts,tus);
       
    }
    
    public static String printNumber(String x,String dx,String unit)throws java.io.IOException{
    	return printNumber(x,dx,unit,false);
    }
    
    
    /**prints numbers if no unit is given */
    public static String printNumber(String x,String dx)throws java.io.IOException{
        return printNumber(x,dx,"");
    }   
    
    /**prints numbers where characters like "<" need to be replaced */
    public static String printNumber(String x,String dx,boolean equals)throws java.io.IOException{
       return printNumber(x,dx,"",equals);
                
    }
    
    public static String printNumber(String x,String dx,String unit,boolean equals)throws java.io.IOException{
        String symbol="",s,ds,out="";
        s=Str.addMissingZero(x);
        ds=dx;
        if(s.toUpperCase().equals("STABLE")) 
        	return s.toLowerCase();

        
        if(ds.length()<=0){
            s=s.replace("<","$<$");
            s=s.replace(">", "$>$");
        }
        
        if(equals) {symbol="=";}
        
        if(ds.equals("LE")){ ds=""; symbol="$\\leq$";}
        else if(ds.equals("GE")){ ds=""; symbol="$\\geq$";}
        else if(ds.equals("LT")){ ds=""; symbol="$<$";}
        else if(ds.equals("GT")){ ds=""; symbol="$>$";}
        else if(ds.equals("AP")){ ds=""; symbol="$\\approx$";}
        else if(!ds.isEmpty() && !Str.isNumeric(ds.replace("+","").replace("-",""))) {
            try {
                ds=Translator.process(ds);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        out+=symbol+Translator.exponential(s);
        if(unit.length()>0)
            out+=" "+halfLifeUnits(unit);
        if(ds.length()>0)
            out+=" {\\it "+ds.replace("-", "\\textminus")+"}";
        
        return out.trim();	    	
    }
          
    /**prints numbers and uncertainties in an easy way */
    public static String printNumberAsIs(String x,String dx,String unit)throws java.io.IOException{
        return printNumberAsIs(x,dx,unit,false);
    }
       
    
    /**prints numbers as-is if no unit is given */
    public static String printNumberAsIs(String x,String dx)throws java.io.IOException{
        return printNumberAsIs(x,dx,"");
    }
    
    
    public static String printNumberAsIs(String x,String dx,boolean equals)throws java.io.IOException{
        return printNumberAsIs(x,dx,"",equals);
                 
     }
    
    /**prints numbers as is where characters like "<" need to be replaced */
    public static String printNumberAsIs(String x,String dx,String unit,boolean equals)throws java.io.IOException{
        String symbol="",s,ds,out="";
        s=Str.addMissingZero(x);
        ds=dx;
        if(s.toUpperCase().equals("STABLE")) return s.toLowerCase();
        
        if(equals) 
        	symbol="=";
        
        if(ds.length()>0 && !Str.isNumeric(ds) && !ds.equalsIgnoreCase("SY")){
        	symbol=ds;
        	ds="";
        }
                   
        out+=symbol+s;
        
        if(unit.length()>0)
            out+=" "+unit;
        if(ds.length()>0)
            out+=" "+ds;
                
        return out.trim();
    }
    
    /**print number with uncertainty in superscript (and subscript for "-" uncertainty)*/
    public static String printNumberSuperscipt(String x,String dx,String unit,boolean equals)throws java.io.IOException{
        String symbol="",s,ds,out="";
        s=Str.addMissingZero(x);
        ds=dx;
        if(s.toUpperCase().equals("STABLE")) return s.toLowerCase();
        
        if(ds.equals("LE")){ ds=""; symbol="$\\leq$";}
        else if(ds.equals("GE")){ ds=""; symbol="$\\geq$";}
        else if(ds.equals("LT")){ ds=""; symbol="$<$";}
        else if(ds.equals("GT")){ ds=""; symbol="$>$";}
        else if(ds.equals("AP")){ ds=""; symbol="$\\approx$";}
        else if(equals) {symbol="=";}
        
        if(symbol.length()>0)
            out+=symbol;
        out+=Translator.exponential(s);
        if(unit.length()>0){
            out+=" "+unit+" ";
            if(ds.length()>0) out+="{\\it "+ds+"}";
        }else{
            if(ds.length()>0){
               if(ds.contains("+")&&ds.contains("-")){
                   int plus=ds.indexOf("+"),minus=ds.indexOf("-");
                   out+="\\ensuremath{^{"+ds.substring(plus+1,minus)+"}_{"+ds.substring(minus+1)+"}}";
               }else    
            	   out+=" \\ensuremath{^{"+ds+"}}";
            }
        }
        return out.trim();
    }
        
    
    /**takes a unit for a half-life and returns it in little-C comment form
    *there are a lot of parts of the ensdf package that convert units to unicode, etc, but I thint little-c is the
    *best because then you can just use a dictionary to convert to latex, unicode, etc */
    public static String halfLifeUnits(String s){
    	
    	if(s.equals("meV") || s.equals("mEV"))
    		return "meV";
    	
    	s=s.toUpperCase();
        if(s.equals("US")) return "\\ensuremath{\\mu}s";
        else if(s.equals("M")) return "min";
        else if(s.equals("EV")) return "eV";
        else if(s.equals("KEV")) return "keV";
        else if(s.equals("MEV")) return "MeV";
        else return s.toLowerCase();
    }
    
    public static String halfLifeUnitsLowerCase(String s){
    	
    	if(s.equals("meV") || s.equals("mEV"))
    		return "meV";
    	
    	s=s.toUpperCase();
        if(s.equals("US")) return "|ms";
        else if(s.equals("M")) return "min";
        else if(s.equals("EV")) return "eV";
        else if(s.equals("KEV")) return "keV";
        else if(s.equals("MEV")) return "MeV";
        else return s.toLowerCase();
    }
    
    /**
     * Return either only the value or the value plus the uncertainty
     * if it is a GT,LT,GE,LE,AP
     */
    public static String value(String val,String unc){
        return value(val,unc,2,-2);
    }
   
    /**
     * Return either only the value or the value plus the uncertainty
     * if it is a GT,LT,GE,LE,AP
     * 
     * If val is in exponential format, return the latex translation for exponential format
     * If val is plain format, return the value in plain format
     * 
     * Note: it is as-is format, not return val as is
     */
    public static String valueAsIs(String val,String unc){
        String ret = "";
        val=Str.addMissingZero(val);
        
        if (unc.equals("LT"))
            ret += "\\ensuremath{<}";
        else if (unc.equals("LE"))
            ret += "\\ensuremath{\\leq}";
        else if (unc.equals("GT"))
            ret += "\\ensuremath{>}";
        else if (unc.equals("GE"))
            ret += "\\ensuremath{\\geq}";
        else if (unc.equals("AP"))
            ret += "\\ensuremath{\\approx}";
        
        return ret+exponential(val);
    }
    
    /**
     * Return either only the value or the value plus the uncertainty
     * if it is a GT,LT,GE,LE,AP
     * if(positive expo<minPositivePower or negative expo>maxPositivePower) return value without format
     * 
     * minPositivePower must be positive integer, default=2 if input is negative or 0
     * maxNegativePower must be negative integer, default=-2 if input is positive or 0
     */
    public static String value(String val,String unc,int minPositivePower,int maxNegativePower){

        int n=val.toUpperCase().indexOf("E");
        if(n<=0 || n==val.length()-1)
        	return plainValue(val,unc);
        
        String expS=val.substring(n+1).trim();
        if(!Str.isInteger(expS))
        	return valueAsIs(val,unc);

        n=Integer.parseInt(expS);
        
        if(minPositivePower<=0)
        	minPositivePower=2;
        if(maxNegativePower>=0)
        	maxNegativePower=-2;
        
        if((n<0&&n>maxNegativePower) || (n>=0&& n<minPositivePower) )
        	return plainValue(val,unc);
        
        return valueAsIs(val,unc);
    }
    
    //note that (val,unc) pair is in ENSDF format, which means len(val)<=10,len(unc)<=2
    //output of plainValue(val,unc) for an exponential format is a value in general number format (no exponential)
    public static String plainValue(String val,String unc,int maxLen){
        String out=plainValue(val,unc);
        int pe=val.toUpperCase().indexOf("E");
        
        if(pe<=0)
        	return out;
        
        int len=val.length()+2;//"E" is printed as "x10"
        if(!Str.isNumeric(unc)&&unc.length()>0)
        	len+=1;
        
        if(len<=maxLen)
        	return out;
        
        return value(val,unc);
    }
    
    /*
     * length of a value in PDF output
     * note that "E" in exponential format is printed as "x10" in pdf output
     */
    public static int plainValueLen(String val,String unc){
    	int len=val.length();
    	if(!Str.isNumeric(unc)&&unc.length()>0)
    		len+=1;
    	if(val.toUpperCase().indexOf("E")>0)
    		len+=2;
    	
    	return len;
    }
    /**
     * Return either only the value printed without format (exponential) or the value plus the uncertainty
     * if it is a GT,LT,GE,LE,AP
     * 
     * No matter if val is in exponential or plain format, convert it to if not and return plain format
     * 
     */
    public static String plainValue(String val,String unc){
        String ret = "";
        val=Str.addMissingZero(val);
        
        if (unc.equals("LT"))
            ret += "\\ensuremath{<}";
        else if (unc.equals("LE"))
            ret += "\\ensuremath{\\leq}";
        else if (unc.equals("GT"))
            ret += "\\ensuremath{>}";
        else if (unc.equals("GE"))
            ret += "\\ensuremath{\\geq}";
        else if (unc.equals("AP"))
            ret += "\\ensuremath{\\approx}";
        
        int pe=val.toUpperCase().indexOf("E");
        if(pe<=0)
        	return ret+val;
        
        int pp=val.indexOf(".");
        int pow=0;
        String base=val.substring(0,pe);
        try{
        	pow=Integer.parseInt(val.substring(pe+1));
        }catch (Exception e){
        	return ret+exponential(val);
        }
        
        if(pp<0)
        	pp=pe;
        
        base=base.replace(".","");
        pp+=pow;
        
        //debug
        //System.out.println("In Translator: line 923: base="+base+" pp="+pp+" pe="+pe+" pow="+pow);
        
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
        
        //debug
        //System.out.println("In Translator: line 923: val="+val+" plain value="+ret+base);
        
        return ret+base;
    }
    
    /**prints orbitals in latex-readable format 
     * e.g., H11/2 to h{-11/2}
     * */
    public static String orbital(String orbitalS){
    	String out="";
    	
    	String s=orbitalS.toUpperCase();
    	if(!orbitalS.equals(s))
    		return orbitalS;
    	
    	s=s.trim();//e.g., S1/2, length=4; H11/2, length=5
    	if(s.length()<4)
    		return orbitalS;
    	
    	String shell="";//orbital shell names, S, P, D, F, G, H, I
    	String js="";//orbital angular momentum number  
    	boolean isJS=false;
    	for(int i=0;i<s.length();i++){
    		char c=s.charAt(i);

    		isJS=false;
    		if(Character.isDigit(c) || c=='/'){
				js+=c;
				isJS=true;
				if(i<s.length()-1)
					continue;
    		}
    		
			if(js.length()>0 && shell.length()>0){
				out+=shell.toLowerCase()+"{-"+js+"}";
			}else if(js.length()>0){//e.g., "2" before H11/2 in "2H11/2"
				out+=js;
			}else if(shell.length()>0){//e.g., "2" before H11/2 in "2H11/2"
				out+=shell;
			}
			
			shell="";
			js="";
    		if(Character.isLetter(c))
    			shell=c+"";
    		else if(!isJS)//other non-letter characters
    			out+=c;
    	}

    	return out;
    	
    }
    
    /**prints JPis in latex-readable format(handles &'s and converts ; to 'to' ; parity in superscipt   */
    public static String spin(String spinText){
        String out="";
        //String[] op={"LE","GE","LT","GT","AP"};
               
        String text=spinText.trim();
        
        String psymbol="+";
        String msymbol="-";
        char nextChar=0;
        char prevChar=0;
        
        for(int i=0;i<text.length();i++){
        	if(i>0)
        		prevChar=text.charAt(i-1);
        	
        	if(i<text.length()-1)
        		nextChar=text.charAt(i+1);
        	else
        		nextChar=0;
        	
            if(text.charAt(i)=='&')
            	out+="\\&";
            else if(i<text.length()-3&&text.substring(i,i+3).equalsIgnoreCase("and")){
                    out+="\\&";
                    i+=2;
            }
            else if(i<text.length()-2&&text.substring(i,i+2).equalsIgnoreCase("or")){
                    out+="or";
                    i+=1;
            }
            else if(text.charAt(i)==';'||text.charAt(i)==':') 
            	out+=" to ";
            else if((text.charAt(i)=='+' || text.charAt(i)=='-') && i<text.length()-1){
            	if(i==0 || (Character.isLetterOrDigit(nextChar) && Character.isLetterOrDigit(prevChar))){//eg, JPI=1+3
            		if(text.charAt(i)=='+')
            			out+="PSYMBOL";
            		else
            			out+="MPSYMBOL";
            	}else if(text.charAt(i)=='+' && !isPIplus(spinText,i)){//eg, JPI=1++3-, that is 1+ plus 3-, or JPI=1+(+3-), JPI=1(+)+3-, JPI=1(+)(+3-)
            		out+="PSYMBOL";
            	}else
            		out+=text.charAt(i);
            }else
            	out+=text.charAt(i);
        }
             	
        out=out.replace("|<","LE");
        out=out.replace("|>","GE");
        
        out=out.replace("<","LT");
        out=out.replace(">","GT");
        
        out=replaceAndSqueeze(out,"LE","\\ensuremath{\\leq}");
        out=replaceAndSqueeze(out,"GE","\\ensuremath{\\geq}");
        out=replaceAndSqueeze(out,"LT","\\ensuremath{<}");
        out=replaceAndSqueeze(out,"GT","\\ensuremath{>}");
        out=replaceAndSqueeze(out,"AP","\\ensuremath{\\approx}");
        
        out=out.replace("TO","to");
        out=out.replace("To","to");
        
        if(text.lastIndexOf("(+)")>0)
        	out=out.replace("(+)","(P)");
        
        out=out.replace("+","\\ensuremath{^{+}}");
        out=out.replace("(P)","\\ensuremath{^{(+)}}");
        

        
        if(text.lastIndexOf("(-)")>0)
        	out=out.replace("(-)","(M)");
        
        out=out.replace("-","\\ensuremath{^{-}}");
        out=out.replace("(M)","\\ensuremath{^{(-)}}");
        
        
        out=out.replaceAll("PSYMBOL", psymbol);
        out=out.replaceAll("MSYMBOL", msymbol);
        
        //out=out.replace("-", "{\\textminus}"); //minus sign is printed correctly in output only in math mode. It is a short dash in text mode.
        
       
        out.replace("NOT","not");
        return out;
    }
    
    
    /**prints JPis in latex-readable format(handles &'s and converts ; to 'to'    */
    public static String plainSpin(String text){
        String out="";
        //String[] op={"LE","GE","LT","GT","AP"};
               
        
        for(int i=0;i<text.length();i++){
            if(text.charAt(i)=='&') out+="\\&";
            else if(i<text.length()-3&&text.charAt(i)=='a'){
                if(text.substring(i,i+3).equalsIgnoreCase("and")){
                    out+="\\&";
                    i+=2;
                }
            }else if(text.charAt(i)==';'||text.charAt(i)==':') out+=" to ";
            else out+=text.charAt(i);
        }

                	
        out=out.replace("|<","LE");
        out=out.replace("|>","GE");
        
        out=out.replace("<","LT");
        out=out.replace(">","GT");
        
        out=replaceAndSqueeze(out,"LE","\\ensuremath{\\leq}");
        out=replaceAndSqueeze(out,"GE","\\ensuremath{\\geq}");
        out=replaceAndSqueeze(out,"LT","\\ensuremath{<}");
        out=replaceAndSqueeze(out,"GT","\\ensuremath{>}");
        out=replaceAndSqueeze(out,"AP","\\ensuremath{\\approx}");
        
        out=out.replace("TO","to");
        out=out.replace("To","to");        
        
        //out=out.replace("-", "{\\textminus}"); //minus sign is printed correctly in output only in math mode. It is a short dash in text mode.
        
        return out;
    }
    
    /**replace old substring with new one and also remove all space around*/ 
    public static String replaceAndSqueeze(String str,String oldWord,String newWord){
    	String out="",temp=str;

    	int p=str.indexOf(oldWord);
    	if(p<0)
    		return str;
    	
    	do{
        	out+=temp.substring(0, p).trim()+newWord;
        	temp=temp.substring(p+oldWord.length()).trim();	
        	
        	if(temp.length()==0)
        		return out;
        	
        	p=temp.indexOf(oldWord);
    	}while(p>=0);
    	
    	if(temp.length()>0)
    		out+=temp;
    	
    	return out;
    }
    
    /**removes keynumbers from a comment, useful when doing band drawings*/
    public static String removeKeynos(String text){
        String out="";
        boolean isKeyno=false;
        int keynoLength=0;
       for(int i=0;i<text.length();i++){
           keynoLength=0;
           if(i<text.length()-8){//check if its a length 8 keynumber
               isKeyno=true;
               keynoLength=7;
               if(text.charAt(i)!='1'&&text.charAt(i)!='2') isKeyno=false;
               for(int j=1;j<=3;j++) 
                   if(text.charAt(i+j)<'0'||text.charAt(i+j)>'9')isKeyno=false;
               if(text.charAt(i+4)<'A'||text.charAt(i+4)>'Z') isKeyno=false;
               if((text.charAt(i+5)<'A'||text.charAt(i+5)>'Z')&&(text.charAt(i+5)<'a'||text.charAt(i+5)>'z')) isKeyno=false;
               for(int j=6;j<=7;j++) 
                   if((text.charAt(i+j)<'0'||text.charAt(i+j)>'9')&&(text.charAt(i+j)<'A'||text.charAt(i+j)>'Z')) isKeyno=false;
               
           }
            if(i<text.length()-6&&!isKeyno){//check if its a length 6 keynumber
                isKeyno=true;
                keynoLength=5;
               for(int j=0;j<=1;j++) 
                   if(text.charAt(i+j)<'0'||text.charAt(i+j)>'9')isKeyno=false;
               if(text.charAt(i+2)<'A'||text.charAt(i+2)>'Z') isKeyno=false;
               if((text.charAt(i+3)<'A'||text.charAt(i+3)>'Z')&&(text.charAt(i+3)<'a'||text.charAt(i+3)>'z')) isKeyno=false;
               for(int j=4;j<=5;j++) 
                   if((text.charAt(i+j)<'0'||text.charAt(i+j)>'9')&&(text.charAt(i+j)<'A'||text.charAt(i+j)>'Z')) isKeyno=false;
            }else if(i>=text.length()-6){
                out+=text.substring(i);
                break;
            }
           if(isKeyno){
               i+=keynoLength;
           }else{
               out+=text.charAt(i);
           }
        }
        return out;
    }
    /*prints a number in exponential format in good latex*/
    public static String exponential(String text){
        int x=text.indexOf("E");
        String out=text;
        String base="",expo="";
       
        if(x>0&&x<text.length()-1){
            if(text.charAt(x+1)!='+'&&text.charAt(x+1)!='-'&&(text.charAt(x+1)<'0'||text.charAt(x+1)>'9')) 
            	return text;
            base=text.substring(0,x).replace("-", "$-$");
            expo=text.substring(x+1);
            if(expo.charAt(0)=='+') 
            	expo=expo.substring(1);
            
            while(expo.length()>0 && expo.charAt(0)=='0')
            	expo=expo.substring(1);
            
            if(expo.length()>0)
            	out=base+"\\ensuremath{\\times10^{"+expo+"}}";
            else
            	out=base;
        }
        out=out.trim();
        
        if(out.length()>0 && out.charAt(0)=='-')
        	out="$-$"+out.substring(1);
        
        return out;
    }
    /**makes a basic substitution of a nucleus in C comments (eg 208PB) into c comments({+208}Pb)*/
    public static String getTransNuc(String s){
        boolean foundSomething=false;
        String output="";
        String text=s;
        
        //translations in user-defined dictionary supersede those in built-in dictionary
        output=UserDictionary.getTranslation(s);
        if(output.trim().length()>0)
        	return output;
        	
        output="";
        for(int x=0;x<Nkey.length;x++){
            if(text.equals(Nkey[x].toUpperCase())){
                return Nvalue[x];
            }
        }
        if(!foundSomething) return " ";    
        return output;
    }
    /**same as above but for reactions*/
    public static String getTransRxn(String text){
        int ch=0;
        boolean mat=false;
        boolean foundSomething=false;
        boolean canBeTranslated=false;
        String output="";
        
        //translations in user-defined dictionary supersede those in built-in dictionary
        output=UserDictionary.getTranslation(text);
        
        //debug
        //System.out.println("In Translator line 1159 Rxn text="+text+" output of local dict=*"+output+"*");
        
        if(output.trim().length()>0)
        	return output;
        
        output="";	
        
        int imat=-1;
        while(ch<text.length()){
            mat=false;
            imat=-1;
            //System.out.println("Rxn: "+ch+" "+text.length()+" "+text+" "+mat);
            for (int x=0; x<Rkey.length; x++){
                if (match(Rkey[x],text,ch)){
                    imat=ch;
                    
                    output+=Rvalue[x];
                    ch+=Rkey[x].length();
                    mat=true;
                    foundSomething=true;
                    
                    //debug
                    //if(Rkey[x].contains("E,E'"))
                    //	System.out.println("In Translator line 1175 Rxn: "+imat+" "+text.length()+" "+text+" "+mat+" "+Rvalue[x]);
                    
                    break;
                }

            }
            
            //considered as a reaction name only if the whole text has a match in dictionary when it begins with "("
            if(foundSomething && imat==0)
            	canBeTranslated=true;
            else
            	canBeTranslated=false;
            
            if(mat) continue;
            output+=text.charAt(ch);
            ch++;
        }
        if(!foundSomething) return " ";
        if(!canBeTranslated) return " ";
        return output;
    }
    /**same as above but for all words*/
    public static String getTransWord(String s){
        boolean foundSomething=false;
        String output="";
        String text=s;
        
        //translations in user-defined dictionary supersede those in built-in dictionary
        output=UserDictionary.getTranslation(s);
        
        
        if(output.trim().length()>0)
        	return output;
        		
        output="";
        for(int x=0;x<Wkey.length;x++){
            if(text.equals(Wkey[x].toUpperCase())){
                return Wvalue[x];
            }
        }
        if(!foundSomething) return " ";    
        return output;
    }
    public static void printKey(){
        for(int i=0;i<Wkey.length;i++){
            System.out.println(Wkey[i]+" "+Wkey[i].length());
        }
    } 
    
    public static String procGenCom_test(String s,int start,boolean cap) throws Exception{
    	try {
    		String s1=procGenCom(s,start,cap);
    		System.out.println("@@@@ s="+s+" start="+start+" cap="+cap);
    		return s1;
    	}catch(StackOverflowError e) {
    		System.out.println("####"+s+" start="+start+" cap="+cap);
    	}
    	return " ";
    }
    /**Turns 'C' comments into 'c' comments
     * 
     * relies on numanl.java and Word.java to rin
     *as line 80 of enspub.f
    //s is the string to be processed, cap is whether or not to capitalize the first character
    //start is which character to start processing at (likely 0 if you're calling this method not-recursively)*/
    public static String procGenCom(String s,int start,boolean cap) throws Exception{
           	
        if(start>=s.length()) return "";
        
        
        //debug
        //System.out.println("@@@@@@@@@@In Translator: procGenCom0: at beginning1: line="+s+" start="+start);
        
        String text=s.substring(start);
        
        //System.out.println("@@@@@@@@@@In Translator: procGenCom0.1: at beginning1: line="+s+" start="+start);
        
        String out="";
        String temp;
        String translated="";
        
        if(text.length()<1) return "";

        
        //System.out.println("@@@@@@@@@@In Translator: procGenCom0.2: at beginning1: line="+s+" start="+start);
        
        if(text.length()>1000){
        	Vector<String> sv=splitInHalf(text);
        	
        	//debug
        	//System.out.println("In Translator: "+line.length()+" s1="+sv.get(0));
        	//System.out.println("               s2="+sv.get(1));
        	
        	return procGenCom(sv.get(0),0,cap)+procGenCom(sv.get(1),0,false);
        }
        
        //System.out.println("@@@@@@@@@@In Translator: procGenCom0.3: at beginning1: line="+s+" start="+start);
        
        //try local dictionary first
        if(!UserDictionary.isEmpty() && text.charAt(0)!=' '){
        	
        	//System.out.println("@@@@@@@@@@In Translator: procGenCom0.4: at beginning1: line="+s+" start="+start);
        	
        	temp=getFirstWordBySpace(text);
        	
        	//System.out.println("@@@@@@@@@@In Translator: procGenCom0.5: at beginning1: line="+s+" start="+start);
        	
        	translated=UserDictionary.getTranslation(temp);
        	
        	if(!translated.equals(" "))
        		return translated+procGenCom(text,temp.length(),false);
        	
        }
        
        //debug
        //System.out.println("@@@@@@@@@@In Translator: procGenCom0: at beginning2: line="+s+" start="+start);
        
        Word one=new Word(text,0);

        //debug
        //if(s.indexOf("60.5")>=0 && s.indexOf("60.5")<=1) 
        //System.out.println("In Translator: procGenCom1: "+s+" start="+start+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out
        //		+"\n*cap="+cap+" text="+text+" one.length="+one.length());
        //System.out.println("               text="+text);
        
        //if the first character is just a delimiter, add (skip) the delimiter and continue, except for those delimiters below
        //(see ensdf_dic2.dat file)
        if(one.length()<1){
        	
            //capitalize the next word if the delimiter is a period
            if(one.getDelim()=='.'){
                return one.getDelim()+" "+procGenCom(text,2,true);
            }else if(one.getDelim()=='^'){//skip this word
            	int i=1;
            	if(text.length()>1){
                	if(text.charAt(i)=='*')//skip * character, note * alone is translated as |*, 
                		i+=1;
                	else{
                    	while(text.charAt(i)!=' ' && Character.isLetter(text.charAt(i))){
                    		i++;
                    		if(i==text.length())
                    			break;
                    	}
                	}	
            	}
                
                //debug
                //if(line.contains("GT")) System.out.println("In Translator: line="+line+" i="+i);
                
                //one=new Word(line,1);
                //out+=line.substring(1,one.length()+1)+procGenCom(line,one.length()+1,false);   
            	
            	//debug
            	//System.out.println("In Translator: line="+line+" i="+i+" out="+out+" text="+text);
            	
            	out+=text.substring(1,i)+procGenCom(text,i,false);  
            	
                return out;
            }else if(one.getDelim()=='|'){
                if(text.indexOf('|')==text.length()-1) 
                	out+="|";
                else 
                	out+=text.substring(0,2)+procGenCom(text,2,false);
                
                return out;
            }else if(one.getDelim()=='}'){//for translating {+19}Ne in "C" comment              
                String preWord=s.substring(0,start);
                one=new Word(text,1);
                
               
                boolean skip=true;
                if(one.getWord().length()>0&&Str.isNumeric(preWord)){//if this word is nucleus name
                	temp=getTransNuc(preWord+one.getWord().toUpperCase());
                	if(!temp.equals(" "))
                		skip=false;
                }
                			               	
                if(skip){//skip this word if it is not nucleus name
                	temp=getTransWord(one.getWord());
                	if(temp.equals(" "))
                		temp=one.getWord();
                	
                	//System.out.println("In Translator: procGenCom1.1: temp="+temp+"*line="+line+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out+"*");
                	
                	out+=text.substring(0,1)+temp+procGenCom(text,one.length()+1,false);
                	
                }
                else{
                	temp=one.getWord().substring(0,1)+one.getWord().substring(1).toLowerCase();
                	out+=text.substring(0,1)+temp+procGenCom(text,one.length()+1,false);
                }
                	
                //debug
                //if(line.contains("505")) 
                //  System.out.println("In Translator: procGenCom1.2: skip="+skip+" line="+line+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out+"*");
                
                return out;
            }else if(one.getDelim()==' '){
            	
            	Word next=new Word(text.substring(1).trim(),0);
                String lastWord=s.substring(0,start).trim();
                String nextWord=next.getWord();
                
                //System.out.println("In Translator: procGenCom1.25: line="+line+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out+"*nextword="+nextWord+"*lastword="+lastWord);
                
                if(isOperator(nextWord))
                	return procGenCom(text,text.indexOf(nextWord),false);                
                else if(isOperator(lastWord))
                	return procGenCom(Str.ltrim(text),0,false);
                else if(lastWord.equals("DELTA"))
                	return procGenCom(Str.ltrim(text),0,false);
                else if(text.trim().indexOf("\\\\ ")==0)// " \\ " is used to concatenate two words                	
                	return procGenCom(Str.ltrim(text.replace("\\\\ ", "")),0,false);
                else
                	return one.getDelim()+procGenCom(text,1,false);
                
                
            }else if(one.getDelim()=='\'' || one.getDelim()=='%'){ //e.g., %RI, %SF, %3HE, %IT, ...
            	Word next=new Word(text,1);
                String nextWord=next.getWord();
                translated=getTransWord(one.getDelim()+nextWord);	
                //debug
            	//if(text.contains("%B+A")) 
                //System.out.println("In Translator: procGenCom1.3: line="+line+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out+"*nextword="+nextWord+" trans="+translated);

                
                if(!translated.equals(" "))
                	return translated+procGenCom(text,nextWord.length()+1,false);                
                else
                	return one.getDelim()+procGenCom(text,1,false);
            }else if(one.getDelim()=='<'){
            	int p=text.indexOf('>');
            	if(p>1 && p<20){
                	temp=text.substring(0,p+1);
                    translated=getTransWord(temp);	
                    
                    //System.out.println("In Translator: procGenCom1.4: line="+text+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out+"*temp="+temp+" trans="+translated);
                    
                    if(!translated.equals(" "))
                    	return translated+procGenCom(text,temp.length(),false);   
            	}
            		
            	return one.getDelim()+procGenCom(text,1,false);
            		
            }else if(one.getDelim()=='/' && text.length()>1) {
            	
            	if(text.substring(1).startsWith("MN")) {//by default, MN is translated as element Mn                  
            		//else if(one.getDelim()=='/') {//by default, MN is translated as element Mn          	
            		//System.out.println(text+" start="+start);    
            		return one.getDelim()+"MN"+procGenCom(text,3,false);
            	}else if(start>=3 && s.substring(start-2,start).equals("EV")) {
            		Word tempWord=new Word(text,1);
            		if(tempWord.getWord().equals("A")) {
            			return one.getDelim()+"A"+procGenCom(text,2,false);
            		}else {
            			return one.getDelim()+procGenCom(text,1,false);
            		}
            	}else {
            		return one.getDelim()+procGenCom(text,1,false);
            	}
            }else{
            	//debug
            	//if(line.contains("WERE OBSERVED")) 
            	//System.out.println("In Translator: procGenCom1.3: line="+line+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out
            	//		+"\n* start="+start+"  text="+text);
            	
            	return one.getDelim()+procGenCom(text,1,false);
            }
        }else if(one.length()==1) {    
            temp=one.getWord();
            
            //System.out.println("In Translator: procGenCom1.31: line="+line+" *word="+one.getWord()+" delim="+one.getDelim()+"* out=*"+out+"*");
            
            if(one.getDelim()=='.'){             
                if(temp.equals("A") || temp.equals("B") || temp.equals("G")){//avoid incorrect translations of "A","B","G"
                    return temp+procGenCom(text,1,false);
                }
            }else if(one.getDelim()==' ' && (temp.equals("P")||temp.equals("N"))) {
                String nextWord=text.substring(1);
                if(nextWord.trim().startsWith("DECAY")) {
                    int n=text.indexOf("DECAY");
                    translated=getTransWord(temp+" DECAY");
                    
                    //System.out.println("In Translator: procGenCom1.32: line="+line+" *word="+one.getWord()+" delim="+one.getDelim()+" translated=*"+translated+"*");
                    if(!translated.equals(" "))
                        return translated+procGenCom(text,n+5,false);
                }
            }
        }
        
        text=text.trim();
        
        //debug
        //if(line.contains("505")) 
        //    System.out.println("In Translator: procGenCom2: "+line+" out=*"+out+"*"+" text="+text);
        
        
        if(text.indexOf("CONF=")==0 && text.substring(5).trim().indexOf("(")==0){
        	int nbracket=0;
        	int i=text.indexOf("(");
        	String conf="";
        	while(i<text.length()){
        		char c=text.charAt(i);
        		if(c=='(')
        			nbracket++;
        		if(c==')')
        			nbracket--;
        		conf+=c;
        		if(nbracket==0){
        			try{
        				char c0=text.substring(i+1).trim().charAt(0);
        				if(c0!='(')
        					break;
        			}catch(Exception e){
               			break;
        			}
        		}
        		
        		i++;
        	}
        	
        	out+="Configuration="+procCONF(conf)+procGenCom(text,i+1,false);
        	return out;
        	
        }
        
        if(text.charAt(0)>='0'&&text.charAt(0)<='9'){
            //if this general comments starts with a number
            numanl n=new numanl(text,0);
            
            if(n.getLength()>0){
            	out+=n.getLine();
                //debug
                //if(text.contains("11/2")) 
                //	System.out.println("In Translator: line="+text+"\n     procGenCom2.5:"+n.getLine()+" n.getLength="+n.getLength()+" out=*"+out+"*"+" text="+text);

                //debug
                //if(text.contains("11/2"))
            	//if(n.getLength()<0 || n.getNExtraSpaces()<0)
                //System.out.println("In Translator 2198: n.getNExtraSpaces()="+n.getNExtraSpaces()
                //+"\n     procGenCom2.6:"+n.getLine()+" n.getLength="+n.getLength()+" out=*"+out+"*");

           
                //if(n.isNilsson()) out="{"+out+"}";
                out+=procGenCom(text,n.getLength()+n.getNExtraSpaces(),false);
                                
                return out;
            }
        }
            
            
        if(text.charAt(0)=='('){
            //a bracket is first
         //get all the text until the next bracket...if the brackets don't close then just get everything
            int nBrackets=1,size=0;
            String inBrackets="";
            for(int i=1;i<text.length();i++){
                if(text.charAt(i)=='(') nBrackets++;
                else if(text.charAt(i)==')') nBrackets--;
                if(nBrackets<=0){
                    inBrackets=text.substring(1,i);
                    size=i;
                    break;
                }
                if(i==text.length()-1){
                    size=i;
                    inBrackets=text.substring(1);
                }
            }
            

            
            //first see if whatever's in the bracket is a reaction
            String sb="("+inBrackets+")";
            translated=getTransRxn(sb.replace("^", ""));//(P,^X), (G,N^X)
            
            //debug
            //System.out.println("In Translator 2321: procGenCom3: "+inBrackets+" "+translated+" out=*"+out+"* text="+text);
            
            if(!translated.equals(" ")) {
                out+=translated+procGenCom(text,size+1,false);
            }else{
                translated=getTransWord("("+inBrackets+")");
                
                //debug
                //System.out.println("In Translator 2329: procGenCom3.1: "+inBrackets+" translated="+translated+" size="+size);
                
                if(!translated.equals(" ")){
                    out+=translated+procGenCom(text,size+1,false);
                }else if(size>0){
                	
                	//handle cases including reaction names that mixes with upper and lower case
                    String[] parts=inBrackets.split("[,]");
                    
                    //System.out.println("Translator 2264: parts.len="+parts.length+" translated="+translated+"$text="+text);                   
                    
                    
                    //if(inBrackets.length()>=6&&inBrackets.length()<=8&&inBrackets.charAt(0)>='1'&&inBrackets.charAt(0)<='9'){
                	if(parts!=null && parts.length==2 && parts[0].trim().length()>0 && parts[1].trim().length()>1 && text.contains(")")){
                		boolean couldBeDSID=true;
                        
                		String s1=parts[0].trim();
                		String s2=parts[1].trim();
                		
                		//System.out.println("s1="+s1+"##s2="+s2);
                		
                		int n1=s1.length();
                		if(s2.length()>10 || !Str.isLettersOrDigits(s2) || !Str.isLettersOrDigits(s1)) {
                			couldBeDSID=false;
                		}else if(n1==1) {
                			s1=s1.toUpperCase();
                			if(!"ADPNTG".contains(s1))
                				couldBeDSID=false;
                			
                		}else if(s1.charAt(0)>='1'&&s1.charAt(0)<='9'){
                			Nucleus nuc=new Nucleus(s1);
                			if(nuc.Z()<=0 || nuc.A()<=0)
                				couldBeDSID=false;
                		}else if(Str.isLetters(s1)) {
                			if(s1.length()>2)
                				couldBeDSID=false;
                			else {
                				Nucleus nuc=new Nucleus(s1);
                				if(nuc.Z()<=0)
                					couldBeDSID=false;
                			}
                		}else if(Str.isDigits(s1)||Str.isDigits(s2)) {
                			couldBeDSID=false;
                		}
                		
                		//System.out.println("Translator 2284: inBrackes="+inBrackets+" couldBeDSID="+couldBeDSID);                	
                		if(couldBeDSID) {
                			//in all cases where DSID can not be recognized (translated), it is the second part that is not recognizable
                			//and part2 is combination of digits and letters, or something like "XG"
                			Vector<String> tempV=new Vector<String>();
                			int i=0;
                			int letterCount=0;
                			String partS="";
                			char prevChar=0;
                			
                			
                			while(i<s2.length()) {
                				char c=s2.charAt(i);
                				
                				//System.out.println("Translator 2351: s2="+s2+" i="+i+" c="+c+"## partS="+
                				//partS+"##"+Character.isDigit(prevChar)+" prevChar="+prevChar+"##");
                				
                				if(Character.isDigit(c)) {
                					if(!Character.isDigit(prevChar) && partS.length()>0) {
                						tempV.add(partS);
                						partS="";
                						letterCount=0;
                					}
               
                				}else {
                					if(Str.isLetters(partS)) {
                						//prev scan is only a letter, like, A, P, N,D
                						tempV.add(partS);
                						partS="";
                						letterCount=0;
                					}else if(Str.isDigits(partS)) {
                					    //do nothing	
                					}else if(partS.length()>0) {                						
                						if(letterCount>=2) {
                							tempV.add(partS);
                							partS="";
                							letterCount=0;
                						}else {
                    						//check if it is a nucleus or just something like 2P, 3N
                							if(Nucleus.isNucleus(partS+c)) {
                    							tempV.add(partS+c);
                    							
                    							partS="";
                    							letterCount=0;
                    							
                    							prevChar=c;
                    							i++;
                    							continue;
                    						}else{
                    							tempV.add(partS);
                    							partS="";
                    							letterCount=0;
                    						}
                						}
                					}
                					
                					letterCount++;
                				}
                				
                				//System.out.println("Translator 2394: s2="+s2+" i="+i+" c="+c+"## partS="+partS+"##");
                				
                				partS+=c;
                				prevChar=c;
                				
                				//System.out.println("Translator 2399: s2="+s2+" i="+i+" c="+c+"## partS="+partS+"##");
                				
                				i++;
                			}
                			
                			//System.out.println("Translator 2404: s2="+s2+" partS="+partS);
                			
                			if(partS.length()>0)
                				tempV.add(partS);
                			
                			String tempTranslated="";
                			
                			int index=0;
                			for(String ps:tempV) {
                				String tempS="";
       
                				if(ps.length()==1 && "PDTN".contains(ps)) {
                					tempS=ps.toLowerCase();
                				}else if(ps.length()>0){
                					if(ps.equals("X")&&index==0)
                						tempS=ps;
                					else
                						tempS=procGenCom(ps,0,false);
                				}else
                					tempS=ps;

                				//System.out.println("Translator 2416: ps="+ps+" translated="+tempS);
                				
                				if(tempS.trim().length()>0)
                					tempTranslated+=tempS;
                				else {
                					tempTranslated="";
                					break;
                				}
                				index++;
                				//System.out.println(ps+" -- "+tempS);
                			}
                			
                			
                			if(tempTranslated.length()>0 && parts.length>0 && parts[0].length()>0) {
                				translated=tempTranslated;
                				canBeTranslatedAsDSID=true;
                				
                				out+="("+procGenCom(parts[0],0,false)+","+tempTranslated+procGenCom(text,inBrackets.length()+1,false);
                			}
                			
                		}
                		
                		
                    }
                    
                	//System.out.println("Translated="+translated+"$"+translated.equals(" "));
                    
                    //out+="("+procGenCom(inBrackets,0,false);                 
                    //if(!inBrackets.equals(text.substring(1))) out+=")"+procGenCom(text,size+1,false);//charAt(size)==')' if existing
                    if(translated.equals(" ")) {
                        canBeTranslatedAsDSID=false;
                        out+="("+procGenCom(text,1,false);
                    }

                    //debug
                    //System.out.println("In Translator 2270: procGenCom: "+inBrackets+" "+translated+" out=*"+out+"* text="+text);
                }else
                    out+=procGenCom(text,size+1,false);
            }            
        }else if(text.charAt(0)=='^'||text.charAt(0)=='|'){
            //a ^ symbol means skip the next word, already taken care of above so do
            //also |'s are taken care of above
        	//debug
        	//System.out.println("should not happen");
        }else{//try to translate the rest

        	//System.out.println("*****regular text****"+text);

        	//if the delimiter is not a ' ' or '$', then see if including more text will cause a translation
            Word w1=new Word(text,0);
            char c=w1.getDelim();
            temp=w1.getWord();
            int endOfLastWord=w1.length();
            
            //System.out.println("Translator 2505: temp="+temp+" c="+c+" text="+text);
            
        	int ntries=3;
        	Word nextWord;
        	String goodTranslation="";
        	String goodLastWord=temp;

        	char goodLastDelim=c;
        	int goodEndOfLastWord=endOfLastWord;
        	

        	boolean matchFound=false;
        	int nMatches=0;
        	
            //keep trying to translate until you hit an end of word, or it translates
            if(c!=' '&&c!='$'&&c!='n'&&c!='!'&&endOfLastWord<text.length()){           	           	                        	
            	for(int i=0;i<ntries;i++){
            		matchFound=false;
            		
                	//search for translation after adding next word
            		if(endOfLastWord>text.length()-1)
            			break;
            		
            		//System.out.println("In Translator: tries#="+i+" endOfLastWord="+endOfLastWord+" textLen="+text.length());
            		
                	if(c=='('){
                        nextWord=new Word(text,endOfLastWord);//Do not skip '('
                        temp+=nextWord.getWord();
                        c=nextWord.getDelim();
                        endOfLastWord+=nextWord.length();
                        
                        //System.out.println("Translator 2536: temp="+temp+" next word="+nextWord.getWord()+" new c="+c+" text="+text);
                	}else{
                		nextWord=new Word(text,endOfLastWord+1);//skip last delim
                		
                		temp+=c+nextWord.getWord();
                        
                        //debug
                        //if(text.contains("B+A")) {
                    	//  System.out.println("In Translator text 1839: text1="+text+" c="+c+" nextWord="+nextWord.getWord()+" len="+nextWord.length()+" temp="+temp+" translated(temp)="+getTransWord(temp)+" goodTrans="+goodTranslation);
                        //  System.out.println("                i="+i+" endOfLastWord="+endOfLastWord+" text.substring(endoflastword)="+text.substring(endOfLastWord)+" out=*"+out+"*");                    	                       
                        //}
                        
                        endOfLastWord+=1+nextWord.length();
                       
                        c=nextWord.getDelim();
                        //debug
                        //if(text.contains("B+A")) {
                        //	System.out.println("In Translator text 1845: text2="+text+" c="+c+" nextWord="+nextWord.getWord()+" temp="+temp+" translated(temp)="+getTransWord(temp)+" goodTrans="+goodTranslation);
                        //    System.out.println("                i="+i+" endOfLastWord="+endOfLastWord+" text.substring(endoflastword)="+text.substring(endOfLastWord)+" out=*"+out+"*");
                        //}

                	}
                	
                    translated=getTransWord(temp);
                    if(!translated.equals(" ")){
                    	goodTranslation=translated;
                    	goodEndOfLastWord=endOfLastWord;
                    	matchFound=true;
                    	nMatches++;
                    }
                              
                    //System.out.println("Translator 2567: temp="+temp+" translated="+translated+" c="+c);
                    
                    //continue to search for translation after adding next delim
                    if(c!='!'&&c!=' '&&c!='n') {
                    	translated=getTransWord(temp+c);
                    	if(!translated.equals(" ")){
                    		goodTranslation=translated;
                    		goodEndOfLastWord=endOfLastWord+1;
                    		temp+=c;
                    		matchFound=true;
                    		nMatches++;
                    	}
                    }
                    
                    //System.out.println("Translator 2581: temp="+temp+" translated="+translated+" c="+c);
                    
                    if(matchFound){
                    	if(c!='!'&&c!=' '&&c!='n') 
                    		goodLastDelim=c;

                    	goodLastWord=temp;
                    }
            	}
                
                //if no match found by adding next words, set original values
                if(nMatches==0){
                    temp=w1.getWord();
                    c=w1.getDelim();
                    endOfLastWord=w1.length();
                }                
                else{
                	temp=goodLastWord;
                	c=goodLastDelim;		
                	endOfLastWord=goodEndOfLastWord;
                }
              //  else eoLastWord++;
                
            }
            
            
            //debug
            
            //if(text.contains("MEV")){ 
            //	System.out.println("In Translator: line2="+text+" c="+c+" temp="+temp+" translated(temp)="+getTransWord(temp)+" goodTrans="+goodTranslation+" cap="+cap);
            //    System.out.println("               endOfLastWord="+endOfLastWord+" text.substring(endoflastword)="+text.substring(endOfLastWord)+" out=*"+out+"*");
            //    System.out.println("               translate(temp+c)="+getTransWord(temp+c)+" nMatches="+nMatches);
            //}
            
            
            /////////////////////////
            //translate final result
            /////////////////////////
            Word w3;
            

            if(nMatches==0){//check for nucID with no AAA
                translated=getTransWord(temp);//temp=w1.getWord()
                String translatedNuc=getTransNuc(temp);
                boolean isNuc=false;
                if(temp.length()<=2){
                	if(translated.equals(" ")&&!translatedNuc.equals(" ")&&c=='('){
                		translated=translatedNuc;
                		isNuc=true;
                	}
                }
 
               
                //System.out.println("Translator 2634: translated="+translated+" isNuc="+isNuc+" temp="+temp+" c="+c);

                if(!isNuc){
                    //check for w1.getWord()+w1.getDelim()
                	String tempTranslated=getTransWord(temp+c);
                	
                	//System.out.println("####"+tempTranslated.equals(" ")+" "+translated.equals(" ")+" temp="+temp);
                	
                	if(!tempTranslated.equals(" ")){
                		boolean isSetOriginal=false;
                		
                		//for cases like, ELC+EMC= vs ELC+=, if reaching here, ELC+EMC not in dictionary, but ELC+ and ELC both are in dictionary
                		//so do the following to avoid translating "ELC+" in ECL+EMC as "ELC+" alone which has a different translation=|a(L+...)exp
                		//note that for cases like "B+P", all "B","B+","B+P" are in the dictionary and so it has been handled above with B+P translated
                		//as a whole
                		if(!translated.equals(" ") && c=='+'){
                			nextWord=new Word(text,endOfLastWord+1);//skip delim=+
                			String ns=nextWord.getWord().trim();
                			if(!ns.isEmpty() && !getTransWord(ns).equals(" ")){
                				isSetOriginal=true;
                			}
                		}
                   		
                		//System.out.println(" Line 2197: translated="+translated+" tempTranslated="+tempTranslated+" isoriginal="+isSetOriginal);
                   		
                		if(!isSetOriginal){
                    		translated=tempTranslated;
                    		endOfLastWord++;	
                		}

                	}else if(translated.equals(" ")){
                        //if it cant be translated, try translating it as a reaction
                        translated=getTransRxn(temp);
                        
                        //System.out.println(" Line 2668: translated="+translated+" tempTranslated="+tempTranslated+" temp="+temp);
                        
                        if(translated.equals(" ")) { 
                        	translated=translatedNuc;
                        	
                        	//for "IN" in "IN THIS REACTION" and reaction "IN(48CA,X)"
                        	if(translated.length()>0 && translated.length()<=2 && Str.isLetters(translated) && c!='(') {
                        		translated=" ";
                        	}
                        }
                        //System.out.println(" Line 2604: translated="+translated+" tempTranslated="+tempTranslated);
                        
                        //check if it is like ALPHA7LI in DSID: (10BE,ALPHA7LI)
                        if(translated.equals(" ") && c==')'){
                        	String s1=" ",s2=" ";
                        	for(int i=0;i<temp.length();i++){
                        		if(Character.isDigit(temp.charAt(i)) && i>0){
                        			s1=temp.substring(0,i);
                        			s2=temp.substring(i);
                        			break;
                        		}
                        	}
                        	
                        	s1=getTransWord(s1);
                        	s2=getTransNuc(s2);

                        	
                        	if(!s1.equals(" ") && !s2.equals(" ")){
                        		translated=s1+s2;
                        	}
                        }
                        
                        //if(text.contains(",^X")) 
                        //    System.out.println("In Translator 2627: procGenCom4: w1="+w1.getWord()+" temp="+temp+" translated="+translated+" translated(...)"+getTransWord(temp)+" cap="+cap);

                        
                        if(translated.equals(" ")){
                            boolean allChars=true;//if the string is all capital letters
                            for(int i=0;i<temp.length();i++){
                                if(temp.charAt(i)<'A'||temp.charAt(i)>'Z') 
                                	allChars=false;
                            }
                            
                            //debug
                            //if(text.contains(",^X")) 
                            //    System.out.println("In Translator 2639: procGenCom5: w1="+w1.getWord()+" temp="+temp+" translated="+translated+" translated(...)"+getTransWord(temp)+" allchar="+allChars);
                            
                            //if(allChars && temp.length()>1) temp=temp.toLowerCase();//fix the problem that {+19}F in "C" mode is printed as {+19}f. obsolete, fixed somewhere else

                            //Note: text=s.substring(start), temp is the word read from line
                            //if s[0]='(' and the delim (c) after temp is c=')', temp is the whole thing in '()' and 
                            //not in a reaction DSID
                            boolean inDSIDBrackets=false;
                            if(allChars && temp.length()>0){ 
                            	char ch=temp.charAt(temp.length()-1);
                            	
                            	//System.out.println("In Translator line 1784: s="+s+"  temp="+temp+" c="+c);
                            	
                            	if(c==')' && ch=='X' && s.charAt(0)!='('){
                            		inDSIDBrackets=true;
                            		if(temp.equals("AX"))
                            			temp="|aX";
                            		else if(temp.length()>1)//DX,PX,TX,NX
                            			temp=temp.substring(0,temp.length()-1).toLowerCase()+'X';                          			
                            	}else                   		
                            		temp=temp.toLowerCase();//fix the problem that(35AR,D) is printed as ({+35}Ar,D)
                            }
                            
                            if(cap &&!inDSIDBrackets){
                            	if(allChars)
                            		temp=temp.substring(0,1).toUpperCase()+temp.substring(1);
                            	else if(Str.isUpperCase(text.charAt(0)) && text.toLowerCase().charAt(0)==temp.charAt(0))
                            		temp=temp.substring(0,1).toUpperCase()+temp.substring(1);
                            }
                            	
                            
                            translated=temp;
                            
                            //System.out.println("In Translator 2373: procGenCom6: w1="+w1.getWord()+" temp="+temp+" translated="+translated+" translated(...)"+getTransWord(temp)+" allchar="+allChars);
                            
                        }
                    }
                }  
                 	
            }else//nMatches>0
            	translated=goodTranslation;
            
            
            if(translated.equals("@@@")){
                if(endOfLastWord+1<text.length()){
                    w3=new Word(text,endOfLastWord+1);
                    String two=temp+c+w3.getWord();
                    translated=getTransWord(two);
                    if(!translated.equals(" ")) 
                    	endOfLastWord+=w3.length()+1;
                    else{
                        if(cap) 
                        	temp=temp.substring(0,1).toUpperCase()+temp.substring(1);
                        else
                        	temp=temp.toLowerCase();
                        
                    	translated=temp;
                    }
                    //debug
                    //System.out.println("IN Translator: line 1377: temp="+temp+" translated="+translated+" w3.word="+w3.getWord()+" two="+two);
                }else
                	translated=temp;                    
            }
            

            
            //debug
            //if(text.contains("ALPHA")){ 
            //    System.out.println("In Translator 2781: procGenCom4: w1="+w1.getWord()+" temp="+temp+" c="+c+" translated="+translated+" endOfLastWord="+endOfLastWord+" translated(temp)="+getTransWord(temp)+" line="+text);
            //    System.out.println("               goodTranslation="+goodTranslation+" nMatches="+nMatches+" out=*"+out+"*"+" cap="+" text="+text);      
            //}
            //else eoLastWord++;
            out+=translated;
            out+=procGenCom(text,endOfLastWord,false);
        }
        
        //TODO: configuration code
        return out;
    }
    
    
    //use iteration in converting "C" comments to "c" comments
    //instead of recursion which require much more "stack" memory 
    //and could cause StarckOverflow error when processing long comments.
    public static String procGenCom2(String s,int start,boolean cap){
    	String out="";
    	String[] results;
    	
    	String text=s.substring(start);
    	boolean c=cap;
    	String firstWord="";
    	String prevWord=s.substring(0,start);
    	
    	while(text.length()>0){
    		
    		//[0]=translated firstword, [1]=firstword as is (raw), [2]=remaining text (could have extra spaces after firstword removed)
    		results=procGenComFirstWord(text,prevWord,c);
    		
    		firstWord=results[0];
    		out+=firstWord;

    		c=false;
    		if(firstWord.equals(". "))
    			c=true;
    		
    		prevWord=results[1];
    		text=results[2];
    		
    		//System.out.println("In Translator: 2180: preWord="+prevWord+"*firstWord="+firstWord+"*cap="+cap+"*text="+text);
    		//System.out.println("              out="+out+"**");
    	}
    	
    	return out;
    }
    
    private static String[] makeArray(String translated,String firstWord,String restText){
    	String[] out=new String[3];

    	out[0]=translated;
    	out[1]=firstWord;
    	out[2]=restText;
    	
    	return out;
    }
    
    private static String[] makeEmptyArray(){
    	return makeArray("","","");
    }
    
    public static String[] procGenComFirstWord(String s,String prevWord,boolean cap){

    	
        String text=s.substring(0);
        
        String translated="",firstWord="";
         
        String temp="";

        
        if(text.length()<1) return makeEmptyArray();
        
        
        //try local dictionary first
        if(!UserDictionary.isEmpty() && text.charAt(0)!=' '){
        	temp=getFirstWordBySpace(text);
        	translated=UserDictionary.getTranslation(temp);
        	if(!translated.equals(" ")){
        		text=text.substring(temp.length());
        		return makeArray(translated,temp,text);
        	}
        }
        
        Word one=new Word(text,0);

        //debug
        //if(text.contains("B- DECAY")) 
        //  System.out.println("In Translator: procGenCom1: "+text+" *word="+one.getWord()+" delim="+one.getDelim());
        
        //if the string contains just a delimiter, add the delimiter and continue
        if(one.length()<1){
            //capitalize the next word if the delimiter is a period
            if(one.getDelim()=='.'){
            	if(text.length()>2){ 
            		text=text.substring(2);
            		temp=one.getDelim()+" ";
            		return makeArray(temp,temp,text);
            	}
            	else{
            		text="";
            		temp=one.getDelim()+"";
            		return makeArray(temp,temp,text);
            	}
            }else if(one.getDelim()=='^'){//skip this word
            	int i=1;
            	if(text.length()>1){
                	if(text.charAt(i)=='*')//skip * character, note * alone is translated as |*, 
                		i+=1;
                	else{
                    	while(text.charAt(i)!=' ' && Character.isLetter(text.charAt(i))){
                    		i++;
                    		if(i==text.length())
                    			break;
                    	}
                	}	
            	}
                
                //debug
                //if(text.contains("GT")) System.out.println("In Translator: text="+text+" i="+i);
                
                //one=new Word(text,1);
                //out+=text.substring(1,one.length()+1)+procGenCom(text,one.length()+1,false);   
            	translated+=text.substring(1,i);
            	text=text.substring(i);

            	return makeArray(translated,translated,text);
            	
            }else if(one.getDelim()=='|'){
                if(text.indexOf('|')==text.length()-1) 
                	translated+="|";
                else {
                	if(text.length()>2){
                    	translated+=text.substring(0,2);
                    	text=text.substring(2);
                	}
                	else{
                		translated+=text;
                		text="";
                	}
                }
                
                return makeArray(translated,translated,text);
                
            }else if(one.getDelim()=='}'){//for translating {+19}Ne in "C" comment              
                one=new Word(text,1);
                boolean skip=true;
                
                if(one.getWord().length()>0&&Str.isNumeric(prevWord)){//if this word is nucleus name
                	temp=getTransNuc(prevWord+one.getWord().toUpperCase());
                	if(!temp.equals(" "))
                		skip=false;
                }
                			               	
                if(skip){//skip this word if it is not nucleus name
                	temp=getTransWord(one.getWord());
                	if(temp.equals(" "))
                		temp=one.getWord();
                	
                	translated+=text.substring(0,1)+temp; 
                	if(text.length()>one.length()+1){    
                		firstWord=text.substring(0,one.length()+1);
                    	text=text.substring(one.length()+1);                   	
                	}else{
                		firstWord=text;
                 		text="";
                	}

                }
                else{
                	temp=one.getWord().substring(0,1)+one.getWord().substring(1).toLowerCase();
                	if(text.length()>1){
                    	translated+=text.substring(0,1)+temp;
                    	text=text.substring(one.length()+1);
                    	firstWord=text.substring(0,one.length()+1);
                	}
                	else{
                		translated+=text;
                		text="";
                		firstWord=translated;
                	}

                }
                	
                
                return makeArray(translated,firstWord,text);
                
            }else if(one.getDelim()==' '){
            	Word next=new Word(text.substring(1).trim(),0);

                String nextWord=next.getWord();
                if(isOperator(nextWord)){
                	text=text.substring(text.indexOf(nextWord));
                	return makeArray("","",text);  
                }
                else if(isOperator(prevWord)){
                	text=Str.ltrim(text);
                	return makeArray("","",text);
                }
                else if(prevWord.equals("DELTA")){
                	text=Str.ltrim(text);
                	return makeArray("","",text);
                }
                else if(text.trim().indexOf("\\\\ ")==0){// " \\ " is used to concatenate two words 
                	text=Str.ltrim(text.replace("\\\\ ", ""));
                	
                	return makeArray("","",text);
                }
                else{
                	if(text.length()>1)
                		text=text.substring(1);
                	else
                		text="";
                	
                	temp=one.getDelim()+"";
                	return makeArray(temp,temp,text);
                }
                
                
            }else if(one.getDelim()=='\'' || one.getDelim()=='%'){ //e.g., %RI, %SF, %3HE, %IT, ...
            	Word next=new Word(text.substring(1).trim(),0);
                String nextWord=next.getWord();
                translated=getTransWord(one.getDelim()+nextWord);
                if(!translated.equals(" ")){
                	firstWord=text.substring(0,nextWord.length()+1);
                	text=text.substring(nextWord.length()+1); 
                }
                else{
                	firstWord=one.getDelim()+"";
                	text=text.substring(1); 
                }
                
                return makeArray(translated,firstWord,text);
            }else if(one.getDelim()=='<'){
            	int p=text.indexOf('>');
            	if(p>1 && p<20){
                	temp=text.substring(0,p+1);
                    translated=getTransWord(temp);	
                    
                    //System.out.println("In Translator: procGenCom1.4: line="+text+" *word="+one.getWord()+" delim="+one.getDelim()+" out=*"+out+"*temp="+temp+" trans="+translated);
                    
                    if(!translated.equals(" ")) {
                    	firstWord=temp;  
                    	text=text.substring(p+1);
                    }else {
                    	firstWord=one.getDelim()+"";
                    	text=text.substring(1);
                    }
            	}
            		           	
            	return makeArray(translated,firstWord,text);
            		
            }else{
            	if(text.length()>1)
            		text=text.substring(1);
            	else 
            		text="";
            	
            	temp=one.getDelim()+"";
            	return makeArray(temp,temp,text);
            }
        }else if(one.length()==1) {    
            temp=one.getWord();
            if(one.getDelim()=='.'){             
                if(temp.equals("A") || temp.equals("B") || temp.equals("G")){//avoid incorrect translations of "A","B","G"
                    return makeArray(temp,temp,text.substring(1));
                }
            }else if(one.getDelim()==' ' && (temp.equals("P")||temp.equals("N"))) {
                String nextWord=text.substring(1);
                if(nextWord.trim().startsWith("DECAY")) {
                    int n=text.indexOf("DECAY");
                    temp=text.substring(0,n+5);
                    translated=getTransWord(temp+" DECAY");
                    return makeArray(temp,translated,text.substring(n+5));
                }
            }
        }
        
        text=text.trim();
        
        if(text.length()<=0)
        	return makeEmptyArray();
        
        //debug
        //if(text.contains("using the calculated")) 
        //    System.out.println("In Translator: procGenCom2: "+text);
        
        if(text.indexOf("CONF=")==0 && text.substring(5).trim().indexOf("(")==0){
        	int nbracket=0;
        	int i=text.indexOf("(");
        	String conf="";
        	while(i<text.length()){
        		char c=text.charAt(i);
        		if(c=='(')
        			nbracket++;
        		if(c==')')
        			nbracket--;
        		conf+=c;
        		
        		if(nbracket==0){
        			try{
        				char c0=text.substring(i+1).trim().charAt(0);
        				if(c0!='(')
        					break;
        			}catch(Exception e){
               			break;
        			}
        		}
        		
        		i++;
        	}
        	
        	if(i+1<text.length()){
        		firstWord=text.substring(0,i+1);
        		text=text.substring(i+1);
        	}
        	else{
        		firstWord=text;
        		text="";
        	}
        	
        	translated+="Configuration="+procCONF2(conf);
        	
        	return makeArray(translated,firstWord,text);
        	
        }
        
        if(text.charAt(0)>='0'&&text.charAt(0)<='9'){
            //if this general comments starts with a number
            numanl n=new numanl(text,0);
            
            if(n.getLength()>0){
            	translated+=n.getLine();
                //debug
                //if(text.contains("2P")) 
                //	System.out.println("In Translator: text="+text+"\n     procGenCom2.5:"+n.gettext()+" n.getLength="+n.getLength());

                //if(n.isNilsson()) out="{"+out+"}";
            	firstWord=text.substring(0,n.getLength()+n.getNExtraSpaces());
            	text=text.substring(n.getLength()+n.getNExtraSpaces());

                return makeArray(translated,firstWord,text);
            }
        }
            
            
        if(text.charAt(0)=='('){
            //a bracket is first
         //get all the text until the next bracket...if the brackets don't close then just get everything
            int nBrackets=1,size=0;
            String inBrackets="";
            for(int i=1;i<text.length();i++){
                if(text.charAt(i)=='(') nBrackets++;
                else if(text.charAt(i)==')') nBrackets--;
                if(nBrackets<=0){
                    inBrackets=text.substring(1,i);
                    size=i;
                    break;
                }
                if(i==text.length()-1){
                    size=i;
                    inBrackets=text.substring(1);
                }
            }
            

            
            //first see if whatever's in the bracket is a reaction
            translated=getTransRxn("("+inBrackets+")");
            
            //debug
            //System.out.println("In Translator: procGenCom3: "+inBrackets+" "+translated);
            
            if(!translated.equals(" ")) {
            	firstWord=text.substring(0,size+1);
            	text=text.substring(size+1);
            }else{
                translated=getTransWord("("+inBrackets+")");
                
                //debug
                //System.out.println("In Translator: procGenCom3.1: "+inBrackets+" "+translated+" size="+size);
                
                if(!translated.equals(" ")){
                	firstWord=text.substring(0,size+1);
                	text=text.substring(size+1);
                }else if(size>0){
                	
                	//handle cases including reaction names that mixes with upper and lower case
                    if(inBrackets.length()>=6&&inBrackets.length()<=8&&inBrackets.charAt(0)>='1'&&inBrackets.charAt(0)<='9'){
                    }
                    
                    //out+="("+procGenCom(inBrackets,0,false);                 
                    //if(!inBrackets.equals(text.substring(1))) out+=")"+procGenCom(text,size+1,false);//charAt(size)==')' if existing
                    text=text.substring(1);
                    
                    translated="(";
                    firstWord="(";
                    
                    canBeTranslatedAsDSID=false;
                    
                }else{
                	firstWord=text.substring(0,size+1);
                	text=text.substring(size+1);
                }
                
                //System.out.println("In Translator: procGenCom3.1: "+inBrackets+" "+translated+" size="+size+" translated="+translated+"*firstWord="+firstWord+"*text="+text);
                
                return makeArray(translated,firstWord,text);
            }                        
        }else if(text.charAt(0)=='^'||text.charAt(0)=='|'){
            //a ^ symbol means skip the next word, already taken care of above so do
            //also |'s are taken care of above
        }else{//try to translate the rest

            //if the delimiter is not a ' ' or '$', then see if including more text will cause a translation
            Word w1=new Word(text,0);
            char c=w1.getDelim();
            temp=w1.getWord();
            int endOfLastWord=w1.length();
            
        	int ntries=3;
        	Word nextWord;
        	String goodTranslation="";
        	String goodLastWord=temp;

        	char goodLastDelim=c;
        	int goodEndOfLastWord=endOfLastWord;
        	

        	//debug
        	//System.out.println("In Translator line 2689: w1="+temp+" delim="+c);
        	
        	
        	boolean matchFound=false;
        	int nMatches=0;
        	
            //keep trying to translate until you hit an end of word, or it translates
            if(c!=' '&&c!='$'&&c!='n'&&endOfLastWord<text.length()){           	           	                        	
            	for(int i=0;i<ntries;i++){
            		matchFound=false;
            		
                	//search for translation after adding next word
            		if(endOfLastWord>text.length()-1)
            			break;
            		
                	if(c=='('){
                        nextWord=new Word(text,endOfLastWord);//Do not skip '('
                        temp+=nextWord.getWord();
                        c=nextWord.getDelim();
                        endOfLastWord+=nextWord.length();
                	}else{
                        nextWord=new Word(text,endOfLastWord+1);//skip c=w1.getDelim()
                        temp+=c+nextWord.getWord();
                        
                        c=nextWord.getDelim();
                        endOfLastWord+=nextWord.length()+1;
                        
                        //debug
                        //if(text.contains("T)")) 
                        //	System.out.println("In Translator: text="+text+" nextWord="+nextWord.getWord()+" temp="+temp+" translated(temp)="+getTransWord(temp)+" goodTrans="+goodTranslation);
                	}
                	
                    translated=getTransWord(temp);
                    if(!translated.equals(" ")){
                    	goodTranslation=translated;
                    	goodEndOfLastWord=endOfLastWord+1;
                    	matchFound=true;
                    	nMatches++;
                    }
                                       
                    //continue to search for translation after adding next delim
                    if(c!='!'&&c!=' '&&c!='n') {
                    	translated=getTransWord(temp+c);
                    	if(!translated.equals(" ")){                   		
                    		goodTranslation=translated;
                    		goodEndOfLastWord=endOfLastWord+1;
                    		temp+=c;
                    		matchFound=true;
                    		nMatches++;
                    	}
                    }
                    
                    if(matchFound){
                    	if(c!='!'&&c!=' '&&c!='n') 
                    		goodLastDelim=c;

                    	goodLastWord=temp;
                    }
            	}
                
                //if no match found, set original values
                if(nMatches==0){
                    temp=w1.getWord();
                    c=w1.getDelim();
                    endOfLastWord=w1.length();
                }                
                else{
                	temp=goodLastWord;
                	c=goodLastDelim;		
                	endOfLastWord=goodEndOfLastWord;
                }
              //  else eoLastWord++;
                
            }
            
            /////////////////////////
            //translate final result
            /////////////////////////
            Word w3;
            //debug


            if(nMatches==0){//check for nucID with no AAA
                translated=getTransWord(temp);//temp=w1.getWord()
                String translatedNuc=getTransNuc(temp);
                boolean isNuc=false;
                if(temp.length()<=2){
                	if(!translated.equals(" ")&&!translatedNuc.equals(" ")&&c=='('){
                		translated=translatedNuc;
                		isNuc=true;
                	}
                }
 
               
                if(!isNuc){
                    //check for w1.getWord()+w1.getDelim()
                	String tempTranslated=getTransWord(temp+c);
                	if(!translatedNuc.equals(" ")){
                		
                		boolean isSetOriginal=false;
                		
                		//for cases like, ELC+EMC= vs ELC+=, if reaching here, ELC+EMC not in dictionary, but ELC+ and ELC both are in dictionary
                		//so do the following to avoid translating "ELC+" in ECL+EMC as "ELC+" alone which has a different translation=|a(L+...)exp
                		//note that for cases like "B+P", all "B","B+","B+P" are in the dictionary and so it has been handled above with B+P translated
                		//as a whole
                		if(!translated.equals(" ") && c=='+'){
                			nextWord=new Word(text,endOfLastWord+1);//skip delim=+
                			String ns=nextWord.getWord().trim();
                			if(!ns.isEmpty() && !getTransWord(ns).equals(" ")){
                				isSetOriginal=true;
                			}
                		}
                		
                		if(!isSetOriginal){
                    		translated=tempTranslated;
                    		endOfLastWord++;	
                		}
                		
     
                	}else if(translated.equals(" ")){
                        //if it cant be translated, try translating it as a reaction
                        translated=getTransRxn(temp);
                        if(translated.equals(" ")) 
                        	translated=translatedNuc;
                                     
                        //check if it is like ALPHA7LI in DSID: (10BE,ALPHA7LI)
                        if(translated.equals(" ") && c==')'){
                        	String s1=" ",s2=" ";
                        	for(int i=0;i<temp.length();i++){
                        		if(Character.isDigit(temp.charAt(i)) && i>0){
                        			s1=temp.substring(0,i);
                        			s2=temp.substring(i);
                        			break;
                        		}
                        	}
                        	
                        	s1=getTransWord(s1);
                        	s2=getTransNuc(s2);

                        	
                        	if(!s1.equals(" ") && !s2.equals(" ")){
                        		translated=s1+s2;
                        	}
                        }
                        
                        if(translated.equals(" ")){
                            boolean allChars=true;//if the string is all capital letters
                            for(int i=0;i<temp.length();i++){
                                if(temp.charAt(i)<'A'||temp.charAt(i)>'Z') 
                                	allChars=false;
                            }
                            
                            //debug
                            //if(text.contains("NX")) 
                            //    System.out.println("In Translator: procGenCom5: w1="+w1.getWord()+" temp="+temp+" translated="+translated+" translated(...)"+getTransWord(temp)+" allchar="+allChars);
                            
                            //if(allChars && temp.length()>1) temp=temp.toLowerCase();//fix the problem that {+19}F in "C" mode is printed as {+19}f. obsolete, fixed somewhere else

                            //Note: text=s.substring(start), temp is the word read from line
                            //if s[0]='(' and the delim (c) after temp is c=')', temp is the whole thing in '()' and 
                            boolean inDSIDBrackets=false;
                            if(allChars && temp.length()>0){ 
                            	char ch=temp.charAt(temp.length()-1);
                            	                          
                            	if(c==')' && ch=='X' && s.charAt(0)!='('){
                            		inDSIDBrackets=true;
                            		if(temp.equals("AX"))
                            			temp="|aX";
                            		else if(temp.length()>1)//DX,PX,TX,NX
                            			temp=temp.substring(0,temp.length()-1).toLowerCase()+'X';                          			
                            	}else                   		
                            		temp=temp.toLowerCase();//fix the problem that(35AR,D) is printed as ({+35}Ar,D)
                            }
                            
                            if(cap &&!inDSIDBrackets){
                            	if(allChars)
                            		temp=temp.substring(0,1).toUpperCase()+temp.substring(1);
                            	else if(Str.isUpperCase(text.charAt(0)) && text.toLowerCase().charAt(0)==temp.charAt(0))
                            		temp=temp.substring(0,1).toUpperCase()+temp.substring(1);
                            }
                            
                            translated=temp;
                            
                        }
                    }
                }  
                 	
            }else//nMatches>0
            	translated=goodTranslation;
            
            
            if(translated.equals("@@@")){
                if(endOfLastWord+1<text.length()){
                    w3=new Word(text,endOfLastWord+1);
                    String two=temp+c+w3.getWord();
                    translated=getTransWord(two);
                    if(!translated.equals(" ")) 
                    	endOfLastWord+=w3.length()+1;
                    else{
                        if(cap) 
                        	temp=temp.substring(0,1).toUpperCase()+temp.substring(1);
                        else
                        	temp=temp.toLowerCase();
                        
                    	translated=temp;
                    }
                    //debug
                    //System.out.println("IN Translator: line 1377: temp="+temp+" translated="+translated+" w3.word="+w3.getWord()+" two="+two);
                }else
                	translated=temp;     
                

            }
                        
            //debug
            //if(text.contains("W(THETA)")){ 
            //    System.out.println("In Translator: procGenCom4: w1="+w1.getWord()+" temp="+temp+" c="+c+" translated="+translated+" endOfLastWord="+endOfLastWord+" translated(temp)="+getTransWord(temp)+" line="+text);
            //    System.out.println("               goodTranslation="+goodTranslation+" nMatches="+nMatches);      
            //}
            //else eoLastWord++;
            
            if(endOfLastWord<text.length()){
            	firstWord=text.substring(0,endOfLastWord);
               	text=text.substring(endOfLastWord);
            }
            else{
            	firstWord=text;
            	text="";
            }
        }
        
        //TODO: configuration code
        return makeArray(translated,firstWord,text);
    }
    
    public static String JPI(String jpi){
        if(jpi.contains(","))jpi=jpi.substring(0,jpi.indexOf(","));
        if(jpi.contains(":"))jpi=jpi.substring(0,jpi.indexOf(":"));
        if(jpi.startsWith("(")&&!jpi.contains(")"))jpi+=")";
        return jpi;
    }
    
    //for translate string following "CONF=", in the format like (N,H11/2,2) or (P,H11/2)
    //without using procGenCom()
    public static String procCONF2(String confS){
    	boolean hasType=false,hasOrbital=false;
    	String out="";
    	
    	String conf=confS;
    	if(!conf.contains("(") && !conf.contains(")"))
    		return conf;
    	
    	int i=0;
    	int ispace=-1;
    	while(i<conf.length()){
    		char c=conf.charAt(i);
    		if(c==')'){
    			hasType=false;
    			hasOrbital=false;
    		}
    		else if((c=='P' || c=='p') && !hasType){
    			out+="|p";
    			hasType=true;
    			i++;
    			if(i<conf.length() && conf.toUpperCase().charAt(i)=='I')
    				i++;
    			if(i<conf.length() && conf.charAt(i)==' '){
        			String s1=conf.substring(i).trim();
        			if(s1.charAt(0)!=',')
        				conf=conf.substring(0, i)+','+conf.substring(i+1);
    			}

    			continue;
    		}
    		else if((c=='N' || c=='n') && !hasType){
    			out+="|n";
    			hasType=true;
    			i++;
    			if(i<conf.length() && conf.toUpperCase().charAt(i)=='U')
    				i++;
    			if(i<conf.length() && conf.charAt(i)==' '){
        			String s1=conf.substring(i).trim();
        			if(s1.charAt(0)!=',')
        				conf=conf.substring(0, i)+','+conf.substring(i+1);
    			}
    			continue;
    		}
    		else if(c==','){
    			if(hasType && hasOrbital){//process ",2" part in "(N,H11/2,2)"
    				i=i+1;
    				String s="";
    				while(i<conf.length()){
    					c=conf.charAt(i);
    					if(Character.isDigit(c))
    						s+=conf.charAt(i);
    					else if(c!=' ')
    						break;
    				
    					i++;
    				}
    				if(c==')'){
    	    			hasType=false;
    	    			hasOrbital=false;
    	    			out+="){+"+s+"}";
    				}
    				i=i+1;
    				continue;
    				
    			}
    			else if(hasType && !hasOrbital){//process ",H11/2" part in "(N,H11/2,2)"
    				i=i+1;
    				String s="";
    				while(i<conf.length()){
    					c=conf.charAt(i);

    					if( (c==' '&&s.trim().length()>0 ) || c==',' || c==')')
    						break;
    				
    					s+=c;   					
    					i++;
    				}
    				
    				if(s.trim().length()>0){
    					//out+=" "+procGenCom(s.trim(),0,false);	
    					out+=" "+orbital(s.trim());
    					
    					hasOrbital=true;
    				}
    				continue;
    				
    			}else{
    				out+=" ";
    				i++;
    				continue;
    			}    				    			
    		}else if(c==' '){
    	
    			if(!hasType && !hasOrbital){
    				if(i>ispace+1)
    					out+=" ";
    			}
    			
    			ispace=i;
    			i++;
    			continue;
    		}
    		
    		out+=c;
    		i++;
    	}
    	
    	return out;
    			
    }
    
    //for translate string following "CONF=", in the format like (N,H11/2,2) or (P,11/2)
    public static String procCONF(String confS) throws Exception{
    	boolean hasType=false,hasOrbital=false;
    	String out="";
    	
    	String conf=confS;
    	if(!conf.contains("(") && !conf.contains(")"))
    		return conf;
    	
    	if(conf.contains("|n") || conf.contains("|p"))
    		return conf;
    	
    	int i=0;
    	int ispace=-1;
    	while(i<conf.length()){
    		char c=conf.charAt(i);
    		if(c==')'){
    			hasType=false;
    			hasOrbital=false;
    		}
    		else if((c=='P' || c=='p') && !hasType){
    			out+="|p";
    			hasType=true;
    			i++;
    			if(i<conf.length() && conf.toUpperCase().charAt(i)=='I')
    				i++;
    			if(i<conf.length() && conf.charAt(i)==' '){
        			String s1=conf.substring(i).trim();
        			if(s1.charAt(0)!=',')
        				conf=conf.substring(0, i)+','+conf.substring(i+1);
    			}

    			continue;
    		}
    		else if((c=='N' || c=='n') && !hasType){
    			out+="|n";
    			hasType=true;
    			i++;
    			if(i<conf.length() && conf.toUpperCase().charAt(i)=='U')
    				i++;
    			if(i<conf.length() && conf.charAt(i)==' '){
        			String s1=conf.substring(i).trim();
        			if(s1.charAt(0)!=',')
        				conf=conf.substring(0, i)+','+conf.substring(i+1);
    			}
    			continue;
    		}
    		else if(c==','){
    			if(hasType && hasOrbital){
    				i=i+1;
    				String s="";
    				while(i<conf.length()){
    					c=conf.charAt(i);
    					if(Character.isDigit(c) || "+-".indexOf(c)>=0)
    						s+=conf.charAt(i);
    					else if(c!=' ')
    						break;
    				
    					i++;
    				}
    				if(c==')'){
    	    			hasType=false;
    	    			hasOrbital=false;
    	    			out+="){+"+s+"}";
    				}
    				i=i+1;
    				continue;
    				
    			}
    			else if(hasType && !hasOrbital){
    				i=i+1;
    				String s="";
    				while(i<conf.length()){
    					c=conf.charAt(i);

    					if( (c==' '&&s.trim().length()>0 ) || c==',' || c==')')
    						break;
    				
    					s+=c;   					
    					i++;
    				}
    				
    				s=s.trim();
    				if(s.length()>0){
    					if(s.indexOf("{")<0)//in case something like, 1g9/2, which is supposed to be given as 1G9/2
    						s=s.toUpperCase();
    					
    					out+=" "+procGenCom(s.trim(),0,false);	
    					hasOrbital=true;
    				}
    				continue;
    				
    			}else{
    				out+=" ";
    				i++;
    				continue;
    			}    				    			
    		}else if(c==' '){
    	
    			if(!hasType && !hasOrbital){
    				if(i>ispace+1)
    					out+=" ";
    			}
    			
    			ispace=i;
    			i++;
    			continue;
    		}
    		
    		out+=c;
    		i++;
    	}
    	
    	return out;
    			
    }
    public static boolean isOperator(String s){
    	return operators.contains(s);
    }
    
    public static void test()throws Exception{
    	FileOutputStream os;
    	String filename="TranslatorTest.txt";
    	os=new FileOutputStream(filename);
    	
        PrintStream ps=new PrintStream(os);       
        System.setOut(ps);
        String match="";
        String translation="";
        /*
    	for(int i=0;i<Rkey.length;i++){
    		match="";
    		translation=procGenCom(Rkey[i],0,false);
    		if(!translation.equals(Rvalue[i])) match="NO";
    		System.out.println(String.format("%20s %20s %20s %10s", Rkey[i],Rvalue[i],translation,match));
    	}
    	for(int i=0;i<Nkey.length;i++){
    		match="";
    		translation=procGenCom(Nkey[i],0,false);
    		if(!translation.equals(Nvalue[i])) match="NO";
    		System.out.println(String.format("%20s %20s %20s %10s", Nkey[i],Nvalue[i],translation,match));
    	}
    	*/
    	for(int i=0;i<Wkey.length;i++){
    		match="";
    		translation=procGenCom(Wkey[i],0,false);
    		if(!translation.equals(Wvalue[i])) match="NO";
    		System.out.println(String.format("%20s %20s %20s %10s", Wkey[i],Wvalue[i],translation,match));
    	}
    }

    public static boolean canBeTranslated(String s){       
        String output=UserDictionary.getTranslation(s);
        if(output.trim().length()>0)
        	return true;
        
        output="";
    	for(int i=0;i<Rkey.length;i++)
    		if(s.equals(Rkey[i])) 
    			return true;
    	
    	
    	for(int i=0;i<Nkey.length;i++)
    		if(s.equals(Nkey[i]))
    			return true;
    	
    	for(int i=0;i<Wkey.length;i++)
    		if(s.equals(Wkey[i])) 
    			return true;

        
    	return false;	
    }
    
    /*get the first words separated by space*/
    public static String getFirstWordBySpace(String s){
    	String temp=s.trim();
    	if(temp.length()<=0)
    		return "";
    	
    	int p=temp.indexOf(' ');
    	if(p<=0)
    		return temp;
    	
    	return temp.substring(0, p);

    }
    
    /*split a string approximately in half by a whilespace*/
    public static Vector<String> splitInHalf(String s){
    	Vector<String> out=new Vector<String>();
    	int len=s.length();

        int p=0;
    	int middle=len/2-1;
    	
    	for(int i=0;i<=middle;i++){
    		p=0;
    		if(s.charAt(middle-i)==' ')
    			p=middle-i;
    		else if(s.charAt(middle+i)==' ')
    			p=middle+i;
    	    
    		if(p>0){
    			String s1=s.substring(0,p);
    			if(s1.lastIndexOf('{')>s1.lastIndexOf('}') || s1.lastIndexOf('(')>s1.lastIndexOf(')'))
    				continue;

    			break;
    		}

    	}
    	
    	
    	if(Math.abs(len-2*p-2)<(len/3)){
    		out.add(s.substring(0, p));
    		out.add(s.substring(p));
    	}
    	else
    		out.add(s);
    	
    	return out;
    }
    
    /* check if text.charAt(n) is a parity symbol "+" or "-" for
     * determining if they should be in superscript
     */
    public static boolean isPI(String text,int n){
    	int len=text.length();
    	if(len<=0 || n>len-1 || n<=0)
    		return false;

    	char c=text.charAt(n);
    	if(c!='+' && c!='-')
    		return false;
    	
    	char[] delims={' ',',','.',';',':',')',']','+','~'};
    	char prevChar=0,nextChar=0;
    	boolean isPI=false;
    	boolean isAfterNumbers=false;
    	
		prevChar=text.charAt(n-1);
   		
		if(Character.isLetter(prevChar))
			isAfterNumbers=false;
		else if(Character.isDigit(prevChar))
			isAfterNumbers=true;
		else if(prevChar==')'){
			if(n>1 && Character.isDigit(text.charAt(n-2)))//e.g., (1/2,3/2)+
				isAfterNumbers=true;
		}else if(prevChar=='('){
			if(n>1){
				if(Character.isDigit(text.charAt(n-2)))//e.g., 1/2(+)
					isAfterNumbers=true;
				else if(text.charAt(n-2)==')' && n>2 && Character.isDigit(text.charAt(n-3)) )//e.g., (1/2,3/2)(-), even though it should be wirrten as (1/2-,3/2-)
					isAfterNumbers=true;
			}
		}else
			isAfterNumbers=false;

    	if(isAfterNumbers){
        	if(n==len-1)
        		isPI=true;
        	else{
        		nextChar=text.charAt(n+1);

        		
        		if(nextChar=='(' && n<len-2 && text.charAt(n+2)=='+')
        			isPI=true;
        		else if(nextChar=='[' && n<len-2 && Character.isDigit(text.charAt(n+2))){ 
        			if(n<len-5 && text.charAt(n+5)==']' && Str.isNumeric(text.substring(n+2, n+5)))//e.g, 3/2+[411] for configuration
        				isPI=true;
        		}else if(nextChar=='|' && n<len-3) {
        			if("()45@><".contains(text.charAt(n+2)+"")){
        				isPI=true;
        			}
        		}else if(nextChar==' ' && c=='-' && n<len-4) {//hypen like, 90- and 120-|mg/cm{+2}
        		    isPI=true;
        			
            		
            		//System.out.println(text.substring(n+2)+"$  n="+n+" c="+c+" nextChar="+nextChar);
            		
        		    if(text.substring(n+2).startsWith("and ")){
        				
        				int n1=text.indexOf("-",n+5);
        				if(n1>0) {
            				String s1=text.substring(n+5,n1).trim();
            				//System.out.println("### "+s1+"  "+Str.isDigits(s1.replace(".", "")) );
            				if(Str.isDigits(s1.replace(".", "")) ){
            					isPI=false;
            				}	
        				}
      
        			}
        		}else if(!Character.isLetterOrDigit(nextChar)){
            		for(int i=0;i<delims.length;i++){
            			if(nextChar==delims[i]){
            				isPI=true;
            				break;
            			}
            		} 
        		}    		
        	}
    	}

    	
		if(isPI && text.charAt(n)=='-' && Character.isDigit(prevChar)){//check for cases like, 1234-, 2345- and 3456-keV levels
			int i=n-2;
			while(i>=0){
				if(!Character.isDigit(text.charAt(i)) && text.charAt(i)!='.')//for cases like 123.4-
					break;
				i--;
			}

			int count=(n-1)-i;
			//if(count>=3 && (nextChar==',' || text.indexOf(" and")==(n+1))//assuming J<100, that is, 100-,101- are not JPI values
			//System.out.println(""+c+" "+isPI+" i="+i+" count="+count+" "+text.charAt(i));
			
			if(count>=3) {	
				isPI=false;
			}else {
				String prevPart=text.substring(0,i+1);
				String tempS=prevPart.replace("=", "").replace("|>", "").replace("|<", "").replace("|?", "").trim();
				
				if(tempS.endsWith("J|p") || tempS.endsWith(",J") ||tempS.endsWith(" J")||tempS.endsWith("K|p") 
						|| tempS.endsWith(",K") || tempS.endsWith(" K")) {
					isPI=true;
				}else if(nextChar==',' || nextChar==' ') {
					//for cases with JPI-like expressions, like, FWHM=18-, 20- and 80-keV for E{-t}=15- and 23.5-MeV
					//but there is problem that:
					//when processing a long comment, first the comment is truncated and split to multiple lines for given length,
					//and then each line is translated separately. So if the truncate happens to be in between the JPI-like expressions
					//like above, let say, after "20-" in "18-, 20- and 80-keV", the program can't do the following check for the
					//translation before, since the part of information containing "80-keV" is missing in current translation.
					
					String nextPart="";
					try {
						nextPart=text.substring(n+1,n+11).trim();
					}catch(IndexOutOfBoundsException e) {
						nextPart=text.substring(n+1).trim();
					}				
			    	
					if(nextPart.contains("-")) {
						int end=text.indexOf(".",n);
						if(end<0)
							end=text.length();
						else if(end<text.length()-1 && Character.isDigit(text.charAt(end+1))) {
							end=text.indexOf(" ",end);
							if(end<0)
								end=text.length();
						}
						
						//System.out.println(" n="+n+" end="+end);
						
						String s=text.substring(n+1,end);

				    	//System.out.println(""+c+" "+isPI+" i="+i+" count="+count+" s="+s);
				    	
						int p=s.indexOf("-");
						while(p>=0) {
							
							//System.out.println(" s="+s+" p="+p+" text="+text);
							
							if(p<s.length()-1 && Character.isLetter(s.charAt(p+1))){
								s=s.substring(0,p).replace(",", "").replace("-", "").replace(" ", "");
								s=s.toLowerCase().replace("and", "").trim();
								
								//System.out.println("2 s="+s+" p="+p+" Str.isInteger(s)="+Str.isNumeric(s));
								
								if(Str.isNumeric(s)) 
									isPI=false;
								
								break;
							}
							
							p=s.indexOf("-",p+1);
						}

					}
				}
			}
			
		}
		
    	return isPI;
    }
    
    /*check if text.charAt(n) in a string of spin-party is a parity symbol "+"*/
    //check cases like, JPI=1++3-, that is 1+ plus 3-, or JPI=1+(+3-), JPI=1(+)+3-, JPI=1(+)(+3-)
    public static boolean isPIplus(String text,int n){
    	int len=text.length();
    	if(len<=0 || n>len-1 || n<=0)
    		return false;

    	char c=text.charAt(n);
    	if(c!='+')
    		return false;
    	
    	char[] delims={' ',',','.',';',':',')',']','+','&'};
    	char prevChar=0,nextChar=0;
    	boolean isPIplus=false;
    	
		prevChar=text.charAt(n-1);
		
   		
    	if(Character.isDigit(prevChar) || prevChar==')' || prevChar=='('){
        	if(n==text.length()-1)
        		isPIplus=true;
        	else{
        		nextChar=text.charAt(n+1);
        		String temp=text.substring(n+1).trim().toLowerCase();
        		
        		if(nextChar=='(' && n<len-2 && text.charAt(n+2)=='+')
        			isPIplus=true;
        		else if(temp.indexOf("and")==0 || temp.indexOf("or")==0)
        			isPIplus=true;
        		else if(!Character.isLetterOrDigit(nextChar)){
            		for(int i=0;i<delims.length;i++){
            			if(nextChar==delims[i]){
            				isPIplus=true;
            				break;
            			}
            		} 
        		}
        	}
    	}
    	
		
    	return isPIplus;
    }
    
    
    public static String translateQName(String name) {
    	if(name.equals("Q-") || name.equals("QBM") || name.equals("QB-") || name.equals("QB")) return "Q(\\ensuremath{\\beta^-})";
    	if(name.equals("Q+") || name.equals("QBP") || name.equals("QB+")) return "Q(\\ensuremath{\\beta^+})";
    	if(name.equals("QEC")) return "Q(\\ensuremath{\\varepsilon})";
    	if(name.equals("SN")) return "S(n)";
    	if(name.equals("SP")) return "S(p)";
    	if(name.equals("QA")) return "Q(\\ensuremath{\\alpha})";
    	if(name.equals("S2N")) return "S(2n)";
    	if(name.equals("S2P")) return "S(2p)";
    	if(name.equals("QEP") || name.equals("QECP")) return "Q(\\ensuremath{\\varepsilon}p)";
    	if(name.equals("QBN") || name.equals("QB-N") || name.equals("QBMN")) return "Q(\\ensuremath{\\beta^-}n)";
    	if(name.equals("QBM2N") || name.equals("QB-2N") || name.equals("QB2N")) return "Q(\\ensuremath{\\beta^-}2n)";
    	if(name.equals("QBP2P") || name.equals("QB+2P") || name.equals("QB2P")) return "Q(\\ensuremath{\\beta^+}2p)";
    	if(name.equals("Q2B")) return "Q(2\\ensuremath{\\beta^-})";
    	if(name.equals("Q4B")) return "Q(4\\ensuremath{\\beta^-})";
    	if(name.equals("QDA")) return "Q(d,\\ensuremath{\\alpha})";
    	if(name.equals("QPA")) return "Q(p,\\ensuremath{\\alpha})";
    	if(name.equals("QNA")) return "Q(n,\\ensuremath{\\alpha})";
    	  	
    	return name;
    	
    }
}
