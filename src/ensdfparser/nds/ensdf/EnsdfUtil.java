
package ensdfparser.nds.ensdf;

import ensdfparser.calc.UncertaintyParser;
import ensdfparser.calc.UncertaintyPropagator;
import ensdfparser.calc.XDX;
import ensdfparser.check.SpinParityParser;
import ensdfparser.check.TransferInfo;
import ensdfparser.ensdf.*;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.ensdf.EnsdfConfig.EntryInfo;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;
import ensdfparser.nsr.BaseNSR;
import ensdfparser.nsr.BaseNSR.SearchParameter;
import ensdfparser.nsr.NSRAbstract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Various static utility functions dealing with objects in the
 * ensdf package.
 */
public class EnsdfUtil {
    
    public final static ArrayList<String> relation_OPs=new ArrayList<String>(Arrays.asList("<","<=",">",">=","~","="));
    public final static ArrayList<String> ENSDF_relation_OPs=new ArrayList<String>(Arrays.asList("LT","LE","GT","GE","AP","CA","SY"," "));
    public final static ArrayList<String> ENSDF_relation_OPs_lower=new ArrayList<String>(Arrays.asList("<","|<",">","|>","~","CA","SY"," "));
   
    public final static ArrayList<String> ENSDF_relation_string_all_OPs=
    		new ArrayList<String>(Arrays.asList("=","|?","|<","<","|>",">","~"," LT "," LE "," GT "," GE "," AP "," CA "," SY "));
    
    private static HashMap<String,String> others=null,beams=null,ejectiles=null;//Z=-1 is for electron and other particles with negative charge

    private static HashMap<String,Vector<EntryInfo>> recordFieldsVMap=new HashMap<String,Vector<EntryInfo>>();

    public static final double hbar=6.58212E-19;//[eV*s]
    
    /*
     * assumed gamma energy uncertainty based on decimal point
     */
    public static float assumedDEG(String ES) {
        try {
            ES=ES.trim();
            if(!Str.isNumeric(ES))
                return -1;

            int pDot=ES.indexOf(".");
            int pExp=ES.indexOf("E");
            
            int power=0;
            
            if(pExp>0) {
                power=Integer.parseInt(ES.substring(pExp+1));
                ES=ES.substring(0,pExp);
            }
                
            int nDigitsAfterDot=-1;
            if(pDot>=0) 
                nDigitsAfterDot=ES.length()-pDot-1;
                
            float DE=-1;
            if(nDigitsAfterDot<0)//assume DE=1 keV for integer energy value
                DE=1.0f;
            else if(nDigitsAfterDot==0)
                DE=0.5f;
            else
                DE=(float) (0.5f/Math.pow(10, nDigitsAfterDot-1));
            
            DE=(float) (DE*Math.pow(10, power));
            
                
            return DE;
        }catch(Exception e) {

        }
                
        return -1;
    }

    public static float de(Record r) {
        try {
            return r.DEF()>0?r.DEF():EnsdfUtil.assumedDE(r.ES());            
        }catch(Exception e) {
            
        }
        
        return 0;   
    }
    public static float de(Record r1,Record r2) {
        try {
            float de1=0,de2=0,de3=0;
            de1=r1.DEF()>0?r1.DEF():EnsdfUtil.assumedDE(r1.ES());
            de2=r2.DEF()>0?r2.DEF():EnsdfUtil.assumedDE(r2.ES());
            
            return (float) Math.sqrt(de1*de1+de2*de2+de3*de3);              
        }catch(Exception e) {
            
        }
        
        return 0;

    }
    
    public static float de(Record r1,Record r2,Record r3) {
        try {
            float de1=0,de2=0,de3=0;
            de1=r1.DEF()>0?r1.DEF():EnsdfUtil.assumedDE(r1.ES());
            de2=r2.DEF()>0?r2.DEF():EnsdfUtil.assumedDE(r2.ES());
            de3=r3.DEF()>0?r3.DEF():EnsdfUtil.assumedDE(r3.ES());
            
            return (float) Math.sqrt(de1*de1+de2*de2+de3*de3);              
        }catch(Exception e) {
            
        }
        
        return 0;

    }
      
    /*
     * assumed energy uncertainty in general based on decimal point
     */
    public static float assumedDE(String ES) {
        try {
            ES=ES.trim();
            if(!Str.isNumeric(ES))
                return -1;

            int pDot=ES.indexOf(".");
            int pExp=ES.indexOf("E");
            
            int power=0;
            
            if(pExp>0) {
                power=Integer.parseInt(ES.substring(pExp+1));
                ES=ES.substring(0,pExp);
            }
                
            int nDigitsAfterDot=-1;
            if(pDot>=0) 
                nDigitsAfterDot=ES.length()-pDot-1;
                
            float DE=-1;
            if(nDigitsAfterDot<0) //assume DE=5 keV for integer energy value
                DE=5.0f;
            else if(nDigitsAfterDot==0)
                DE=0.5f;
            else
                DE=(float) (0.5f/Math.pow(10, nDigitsAfterDot-1));
            
            DE=(float) (DE*Math.pow(10, power));
                            
            return DE;
        }catch(Exception e) {

        }
                
        return -1;
    }
    
    public static void cleanENSDFFile(String filename,boolean isSetLowerCaseComment){
        cleanENSDFFile(filename,filename,isSetLowerCaseComment);
    }
    
    public static void cleanENSDFFile(String oldfilename,String newfilename,boolean isSetLowerCaseComment){
        try{

            Vector<String> lines=Str.readFile(new File(oldfilename));
            int nlines=lines.size();
            if(nlines==0)
                return;
            
            cleanENSDFLines(lines,isSetLowerCaseComment);
    
            PrintWriter out=new PrintWriter(new File(newfilename));
            String line="";
            for(int i=0;i<lines.size();i++){
                line=lines.get(i);
                
                out.write(line+"\n");
            }
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /*
     * clean up ENSDF lines by fixing the total length of each line to be 80, and
     * if isSetLowerCaseComment=true (default), translate and convert upper-case "C" 
     * comments to lower-case "c" comments; if false, do nothing for that.
     * Added 1/9/2020: split long table record line to multiple liens (also for long comments)
     */
    public static void cleanENSDFLines(Vector<String> linesV,boolean isSetLowerCaseComment){
        Vector<String> tempV=new Vector<String>();
        String line="";
        
        tempV.addAll(linesV);
        linesV.clear();
        for(int i=0;i<tempV.size();i++){
            line=Str.fixLineLength(tempV.get(i),80);
            linesV.add(line);
        }
                
        if(isSetLowerCaseComment){
            
            int i=0;
            int size=linesV.size();
            
            tempV.clear();
            boolean isDoc=false;
            while(i<size){
                line=linesV.get(i);
                String lineType=line.substring(5,7).toUpperCase();

                //process comment lines
                if(lineType.equals(" C") || lineType.equals(" D")){
                    
                    lineType=lineType.trim();
                    if(lineType.equals("D"))
                        isDoc=true;
                    else
                        isDoc=false;
                    
                    Vector<String> cline=new Vector<String>();
                    do{
                        cline.add(line);
                        i++;
                        
                        try{
                            line=linesV.get(i);
                        }catch(Exception e){
                            break;
                        }
                    }while(line.toUpperCase().substring(5,7).trim().indexOf(lineType)==1);
                    
                      
                    if(cline.size()>0 ){                                                      
                        Comment c=new Comment();        
                        try {
    
                            //temporarily convert doc lines to com lines for processing
                            if(isDoc) {
                                for(int j=0;j<cline.size();j++) {
                                    String l=cline.get(j);
                                    if(l.charAt(6)=='d')
                                        l=l.substring(0,6)+"c"+l.substring(7);
                                    else if(l.charAt(6)=='D')
                                        l=l.substring(0,6)+"C"+l.substring(7);
                                    
                                    cline.remove(j);
                                    cline.insertElementAt(l, j);
                                    
                                }
                            }
                            
                            c.setValues(cline,false);//option=false, not to add a period "." at the end of last line
                            
                            /*
                            //debug
                            if(c.body().toLowerCase().contains("identified")){
                              System.out.println(" isBigC="+c.isBigC()+" isMixC="+c.isMixC()+""
                              		+ "\n  body="+c.body()
                              		+ "\n cbody="+c.cbody()+"\n"
                              		+ " head="+c.head());
                                
                              for(String s0:cline)                                 
                            	  System.out.println("   "+s0);
                            }
                            */
                            
                            String head=c.head();
                            String line0=cline.get(0);
                            int n1=line0.indexOf(head)+head.length();
                            int n2=line0.indexOf("$",n1);
                            boolean hasSep=true;//$ separator
                            if(n2<0 || line0.substring(n1,n2).trim().length()>0)
                            	hasSep=false;
                            
                            if(c.isBigC() || c.isMixC() || !hasSep){
                                String s=cline.get(0);
                                String prefix=s.substring(0,5)+s.substring(5,7).toLowerCase()+s.substring(7,9);
                                String body=c.cbody();
                                if(s.charAt(7)!=' '){
                                    body="$"+body;
                                    if(c.head().length()>0)
                                        body=c.head()+body;
                                }
                                    
                                s=Str.wrapString(body,prefix,80,5);//cbody is in lower-case "c" format
                                
                                //System.out.println(s+"\n ### body="+body);
                                
                                String[] a=s.split("\n");
                                
                                cline.clear();
                                for(int j=0;j<a.length;j++){
                                    cline.add(a[j]);
                                }
                            }
                            
                            //restore doc lines from com lines after processing
                            if(isDoc) {
                                for(int j=0;j<cline.size();j++) {
                                    String l=cline.get(j);
                                    if(l.charAt(6)=='c')
                                        l=l.substring(0,6)+"d"+l.substring(7);
                                    
                                    cline.remove(j);
                                    cline.insertElementAt(l, j);
                                    
                                }
                            }
                            
                            tempV.addAll(cline);
                            
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }                                     
                    }   
                    
                    continue;                   
                }
                
                tempV.add(line);
                i++;
            }
            
            if(tempV.size()>0){
                linesV.clear();
                linesV.addAll(tempV);
            }
        }
        
        tempV=splitLongTableRecordLines(linesV);
        
        linesV.clear();
        linesV.addAll(tempV);

    }
    
    /*
     * line must be a valid ENSDF line
     */
    public static String cleanENSDFLine(String line,boolean isSetLowerCaseComment){
    	try {
        	Vector<String> linesV=new Vector<String>();
        	linesV.add(line);
        	cleanENSDFLines(linesV,isSetLowerCaseComment);
        	
        	return linesV.get(0);
    	}catch(Exception e) {
    		
    	}

    	return null;
    }
    
    /*
     * check the position of * marker in XREF record, eg. XREF=A(1234*)
     * Java-NDS accept "*" either at beginning or end
     * But FMTCHK report errors for "*" at the end
     * This functions will move "*" at the end to the beginning
     */
    public static void checkStarMarkerInXREF(String oldfilename,String newfilename){
        try{

            Vector<String> lines=Str.readFile(new File(oldfilename));
            int nlines=lines.size();
            if(nlines==0)
                return;
    
            if(!lines.get(0).contains("ADOPTED LEVELS"))
                return;
            
            PrintWriter out=new PrintWriter(new File(newfilename));
            String line="";
            for(int i=0;i<lines.size();i++){
                line=lines.get(i);
                if(line.contains("X L XREF=") && line.replace("(*)","").contains("*)")) {
                    int n=line.indexOf("XREF=");
                    String xref0=line.substring(n+5);
                    String xref=xref0;
                    
                    xref=xref.replace("(*)","(#)");
                    
                    int rb=xref.indexOf("*)");
                    while(rb>0) {
                        try {
                            int lb=xref.substring(0,rb).lastIndexOf("(");
                            String s=xref.substring(lb+1,rb+1).trim();//content in side ()
                            xref=xref.replace(s, "#"+s.replace("*", "").trim());//temporarily put a "#" at the beginning for "*"            
                        
                            rb=xref.indexOf("*)");
                        }catch(Exception e) {
                            e.printStackTrace();
                            break;
                        }

                    }
                    
                    xref=xref.replace("#", "*");
                    
                    line=line.replace(xref0, xref);
                }
                
                out.write(line+"\n");
            }
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static Vector<String> splitLongTableRecordLines (Vector<String> linesV){
        Vector<String> out=new Vector<String>();
        String line="",lineType="";
        int length=0;
        for(int i=0;i<linesV.size();i++) {
             line=linesV.get(i);
             length=line.trim().length();
             lineType=line.substring(6,7).toUpperCase();
             if(length<=80 || !lineType.equals("T")) {   
                 out.add(line);
                 continue;
             }
             
             String temp="";
             String s=Str.rtrim(line.substring(9));//keep heading indentation space
                         
             int count=0;
             while(s.length()>71) {
                 temp=s.substring(0,71);
                 int p=temp.lastIndexOf(' ');
                 if(p<40)
                     p=70;
                 temp=s.substring(0,p+1);
                 if(p<70) 
                     temp+="\\";//preserve original spacing between parts

                 if(count==0)
                     temp=line.substring(0,9)+temp;
                 else
                     temp=line.substring(0,5)+"+"+line.substring(6,9)+temp;
                 
                 out.add(temp);
                 
                 s=s.substring(p+1);
                 count++;
             }
             
             if(s.length()>0) {
                 temp=line.substring(0,5)+"+"+line.substring(6,9)+s;
                 out.add(temp);
             }
        }
        
        return out;
    }
    
    public static boolean startsWithNumber(String s) {
    	try {
    		s=s.trim();
    		if(s.length()==0)
    			return false;
    		
    		char c=s.charAt(0);
    		if(!Character.isDigit(c)&&!"+-.".contains(c+""))
    			return false;
    		
    		int n=s.indexOf(" ");
    		if(n>0)
    			s=s.substring(0,n).trim();
    		
    		if(Str.isNumeric(s))
    			return true;
    		
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    /*
     * an ENSDF entry: [name=]value [unc] [(reference)]
     *      where [] is optional; unc could be like "12" or "{I12}" or "LT";
     *          reference is like "(2022Ab11)" or "(2022Ab11,2023Ab12)"
     * an entry line contains one or multiple entries separated by "," or ";" or "$"
     * and it could be a full ENSDF comment line like " 32S  cG $B(E2)=12 {I3} (2022Ab11); 14 {I2} (2023Ab12), B(M1)=....."
     * 
     */
    public static String[] splitEntryLine(String entryLine){
    	String[] out=new String[0];
    	try {
      		String s="";
    		
      		/*
    		int nb=0;
  		
    		for(int i=0;i<entryLine.length();i++) {
    			char c=entryLine.charAt(i);
    			if(c=='(' || c=='{' || c=='[') {
    				nb++;
    			}else if(c==')' || c=='}' || c==']') {
    				nb--;
    			}else if(c==','&&nb==0) {
    				s+="$";
    				continue;
    			}
    			
    			s+=c;
    		}
    		entryLine=s;
    		*/
    		
    		out=Str.specialSplitAllDelims(entryLine, "[;$.]"); 
    		//out=Str.specialSplitAllDelims(entryLine, "[,;$.]");    	
    		
    		//System.out.println(entryLine);
    		//System.out.println(out.length);
    		
    		Vector<String> tempV=new Vector<String>();
    		if(out.length>0) {
    			for(int i=0;i<out.length;i++) {
    				s=out[i].trim();
    				//System.out.println("### i="+i+" s="+s);
    				if(s.isEmpty())
    					continue;
    				
    				if(s.contains(",")) {
    					int n=Str.indexOfNotInBrackets(",", s);
    	
						while(n>0) {
							String s1=s.substring(n+1);
							int n1=Str.indexOfNotInBrackets(",", s,n+1);
							if(n1>0)
								s1=s.substring(n+1,n1);
							
							//System.out.println(n+"  "+n1+"  s1="+s1+"  "+startsWithNumber(s1));
							
							if(s1.indexOf("=")>0 || Str.containAnyPattern(s1,EnsdfUtil.ENSDF_relation_string_all_OPs) || startsWithNumber(s1)){
								tempV.add(s.substring(0,n).trim());
								s=s.substring(n+1).trim();
								n=Str.indexOfNotInBrackets(",", s);
								
								//System.out.println(s+"  n="+n);
								
								continue;
							}else {
								n=n1;
								continue;
							}
						}
						if(s.length()>0)
							tempV.add(s);
    					
    				}else {
    					tempV.add(s);
    				}
    				
    				//System.out.println("### s="+s);
    			}
    		}
    		out=new String[tempV.size()];
    		tempV.toArray(out);

    		//System.out.println("#### s="+s+" "+tempV.size()+" "+entryLine);
    		
    		s=entryLine;
    		String s1="",s2="";
    		if(s.toUpperCase().contains(" AND ") || s.toUpperCase().contains(" OR ")) {

    			//for(String s3:tempV)
    			//	System.out.println("    "+s3);

    	  		tempV=new Vector<String>();
        		for(int j=0;j<out.length;j++) {
        			s=out[j].trim();
        			s1=s.toUpperCase();
        			while(s1.length()>0) {
            			int nl=s1.indexOf("(");
            			int nr=s1.indexOf(")");
            			int na=s1.indexOf("AND");
            			if(na<0)
            				na=s1.indexOf("OR");
            			
            			if(na>0) {
            				if(nl>0 && nr>nl && na>nl && na<nr) {
            					na=-1;           					
            					tempV.add(s.substring(0,nr));
            					s1=s1.substring(nr+1).trim();
            					s=s.substring(nr+1).trim();
            					continue;           					
            				}else {
            					s1=s.substring(0,na).trim();
            					s2=s.substring(na+3).trim();

            					//System.out.println(s1+" @ "+s2);
            					
            					tempV.add(s1);
            					s=s2;
            					s1=s.toUpperCase();
            					continue;
            				}
            			}else {
            				tempV.add(s);
            				break;
            			}
        			}      			
        		}

    			//for(String s3:tempV)
    			//	System.out.println("    "+s3);
    			
        		out=new String[tempV.size()];
        		for(int i=0;i<tempV.size();i++)
        			out[i]=tempV.get(i);
    		}
    		//System.out.println("#### "+tempV.size());
    		//check if there are more than one values in each entry
	  		tempV=new Vector<String>();
	  		ArrayList<String> separaters=new ArrayList<String>(Arrays.asList(",","$"," "));
    		for(int j=0;j<out.length;j++) {
    			s=out[j].trim();
    			LinkedHashMap<Integer,String> indexPatternMap=Str.findIndexesOfPatternMap(s, ENSDF_relation_string_all_OPs);
    			
    			//System.out.println(" j="+j+"  s="+s+"  "+indexPatternMap.size());
    			
    			int count=0, offset=0;
    			if(indexPatternMap.size()>1) {
    				for(Integer index:indexPatternMap.keySet()) {
    					int n=index.intValue();
    					
    					//String op=indexPatternMap.get(index);
    					//System.out.println("  index="+n+"  op="+op);
    					
    					if(count==0) {
    						count++;
    						continue;
    					}
    					//Example, M1:M2:M3:(M4+M5)=100:31 {I3}:34 {I3}: |?1.2  2
    					//indexPatternMap size=2
    					//but the second operation pattern=|? should not be counted as a separte entry 
    					
    					String tempS=s.substring(0,n).trim();
    					if(tempS.endsWith(":")||tempS.endsWith("/"))
    						continue;
    					
    					n=Str.lastIndexOfPatterns(s, separaters,n);
    					
    					//System.out.println("   n="+n);
    					
    					if(n>offset) {
    						tempV.add(s.substring(offset,n));
    						offset=n+1;
    					}
    					count++;
    				}
    				
    				if(offset<s.length())
    					tempV.add(s.substring(offset));
    			}else{
    				tempV.add(s);
    			}
    			
    		}

    		//System.out.println(tempV.size()+"###"+ entryLine);
			//for(String s3:tempV)
			//	System.out.println("$$$$$    "+s3);
			if(tempV.size()>0) {
	    		out=new String[tempV.size()];
	    		for(int i=0;i<tempV.size();i++)
	    			out[i]=tempV.get(i);
			}

    	}catch(Exception e) {
    		
    	}
 
    	return out;
    }
    
    /**
     * Returns level object with energy closest to provided value.
     */
    public static Level closest(ENSDF ens,float elev){
       Level best = null;
       for (int x=0; x<ens.nLevels(); x++){
           Level lev = ens.levelAt(x);
           if (best==null)
               best = lev;
           if (Math.abs(lev.ERPF()-elev) < Math.abs(best.ERPF()-elev))
               best = lev;
       }
       return best;
    }
    /**
     * Return final level of gamma 'g' which is from level 'lev'
     */
    public static Level finalLevel_old(ENSDF ens,Level lev,Gamma g){
       //if (true)
       // return closest(ens,lev.ERPF()-g.E.ERPF()
       String fls = g.FLRecord();
       // if there is a final level string to make life easy
       if (fls.length()>0){
           for (int x=0; x<ens.nLevels(); x++){
               Level lev2 = ens.levelAt(x);
               if (fls.equals(lev2.ES()))
                   return lev2;
           }
       }
       //there isn't a final level string or it was wrong
       
       // target energy
       float elev = EnsdfUtil.e(lev) - g.EF();
       
       //System.out.println(elev);
       
       // if there is an energy variable on the level the finding code is
       // different
       String var = lev.NNES();
       //var = var.substring(var.length()-1,1);
       if (var.length()>0){
           Level best = null;
           float bestdif = 1e6f;
           for (int x=0; x<ens.nLevels(); x++){
               Level lev2 = ens.levelAt(x);
               String var2 = lev2.NNES();
               if (var.equalsIgnoreCase(var2)){
                   float dif = Math.abs(EnsdfUtil.e(lev2)-elev);
                   if (dif < bestdif){
                       best = lev2;
                       bestdif = dif;
                   }
               }
           }
           //System.out.printf("%s %s %s %f %f\n",
           //    lev.ES(),best.ES(),g.ES(),new Float(bestdif),new Float(elev));
           return best;
       }
       
       // no final level specified, no energy variable
       Level best = null;
       for (int x=0; x<ens.nLevels(); x++){
           Level lev2 = ens.levelAt(x);
           if (best==null)
               best = lev2;
           //System.out.printf("%d: %f %f\n",x,elev,Ens.e(lev2));
           if (Math.abs(EnsdfUtil.e(lev2)-elev) < Math.abs(EnsdfUtil.e(best)-elev))
               best = lev2;
       }
       return best;
    }
    
    public static Vector<FindValue> findDataEntriesInComment(Comment com) {
        return findDataEntriesInLine(com.rawBody());
    }
    
   
    /*
     * data entry here is an entry with name and value [reference optional]
     * note that the input str does not necessarily contain only data entries.
     * There could be non-entry text mixed with data entries, like entries buried 
     * in a comment
     * 
     * splitEntryLine() simply split the input line by delims=";,$"
     */
    public static Vector<FindValue> findDataEntriesInLine_old(String str) {
        Vector<FindValue> fvs=new Vector<FindValue>();
        str=str.trim();
        if(str.isEmpty())
        	return fvs;
        
        String s0="";
        int n0=str.indexOf("$");
        if(n0>0) {
        	s0=str.substring(0,n0).trim();
        	s0=s0.replace(",", "").replace("(", "").replace(")","").trim();//if s0 is comment head
        	if(s0.isEmpty() || Str.isLetters(s0))
        		str=str.substring(n0+1).trim();
        }
        
        s0=str;
        
        String name="";
        ArrayList<String> ops=new ArrayList<String>(Arrays.asList(">=","<=","|<","|>","=",">","<","~","|?","?","|J","|v","|=","AP","GT","GE","LT","LE"));
        
        int i=0,offset=0;
        char c=0,c1=0;
        
        while(str.contains("  ")) {
            str=str.replace("  "," ");
        }
        
        
        int nb=0;
        String tempS="";
        for(i=0;i<str.length();i++) {
            c=str.charAt(i);
            if(c=='('||c=='[')
                nb++;
            else if(c==')'||c==']')
                nb--;
            else if(c==','&&nb==0)
                c=';';
            
            tempS+=c;
        }
        
        str=tempS;
        
        String marker="@#&";
        str=str.replace("$",marker).replace(";",marker);
        
        String s=str;
        int n=-1;
        
        tempS="";
        while((n=s.indexOf("."))>0) {
        	try {
        		//System.out.println(" n="+n+" s="+s+" tempStr="+tempS);
        		
        		
            	c=s.charAt(n-1);
            	c1=s.charAt(n+1);    
            	if(!Character.isDigit(c) || !Character.isDigit(c1)) 
            		tempS+=s.substring(0,n)+marker;
            	else
            		tempS+=s.substring(0,n+1);
            	
            	s=s.substring(n+1);
        	}catch(Exception e) {
        		//e.printStackTrace();
        		break;
        	}
        }
       
        str=tempS+s;
        
        //System.out.println(str+" tempStr="+tempS+" s="+s);
        
        String prevRef="",prevName="";
        String tempEntryStr="";
        
        boolean isStartOfNextEntry=false;
        i=0;n=-1;
        c=0;c1=0;
        s="";
        offset=0;
        
       
        while(i<str.length()-1) {
        	c=str.charAt(i);
        	c1=str.charAt(i+1);

        	
        	if(isStartOfNextEntry && fvs.size()>0 && prevName.length()>0) {//e.g., the 2nd entry in B(E2)=12 {I3}, 14 {I5}, but with no name prefix
        		isStartOfNextEntry=false;

        		n=offset;
        		
        		boolean isGoodEntry=false;
        		if(Character.isDigit(c) || ops.contains(c+"") || ops.contains(c+c1+"")) {
            		int nextMarkerIndex=str.indexOf(marker,i);
            		if(nextMarkerIndex<0)
            			nextMarkerIndex=str.length();
            		
            		s=str.substring(i,nextMarkerIndex).trim();
      
            		FindValue fv=new FindValue();
            		String op="";
            		if(Character.isDigit(c))
            			op="=";
            		
            		tempEntryStr=prevName+op+s;
            		fv.findValueInEntry(tempEntryStr);
            		
            		
            		if(Str.isNumeric(s)) {
            			isGoodEntry=true;
            		}else if(Str.isNumeric(fv.s())) {
            			String ds=fv.ds();
            			if(Str.isNumeric(ds) || ds.contains("+")) {
            				isGoodEntry=true;
            			}else if(fv.ds().isEmpty()) {     				
            				if(fv.units().length()>0 || fv.ref().length()>0) {
            					isGoodEntry=true;
            				}
            			}else if(Str.isLetters(fv.ds()) && fv.ds().length()==2 && ops.contains(fv.ds())) {
            				isGoodEntry=true;
            			}
            		}
            		
        		}
       		
        		if(isGoodEntry){
        			name=prevName;
        		}else{
        			i++;
        			continue;
        		}
        	}else if(!ops.contains(c+c1+"") && !ops.contains(c+"")) {
        		i++;
        		continue;
        	}else {
            	s=str.substring(offset);
            	
            	name=str.substring(offset,i);
            	
            	n=offset;
            	
            	/*
            	n=str.substring(offset,i).lastIndexOf(" ");//here i is pos of the op symbol
           	
            	
                System.out.println(" ## i="+i+" c="+c+" c1="+c1+" s="+s+" str="+str+" name="+name+" offset="+offset+" n="+n);
                
            	if(n<0)
            		n=offset;
            	else
            		n=n+offset;
            	name=str.substring(n,i);
            	*/
            	
            	name=name.replace(marker,"").trim();
        	}
            
            //System.out.println("$$$$name="+name);
            
        	if(name.length()>0 && !Str.isNumeric(name)) {
        		int n1=str.indexOf(marker,i);
        		if(n1<0)
        			n1=str.length();
        		
        		try {
            		FindValue fv=new FindValue();
            		s=str.substring(n,n1).replace(marker,"").trim();
            		if(!s.startsWith(name))
            			s=tempEntryStr;
            		
            		//System.out.println(" i="+i+" c="+c+" c1="+c1+" s="+s+" str="+str+" name="+name+" offset="+offset+" n="+n+" n1="+n1);
            		//System.out.println(s);
            		
            		fv.findValueInEntry(s);
            		
            		//System.out.println(" fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" fv.ref="+fv.ref()+" name="+name);
            		
            		boolean isGood=true;
            		
            		s=fv.s();
            		
        			if(name.contains("/")) {
        				if(s.contains("/") || s.contains(":")) {
        					s=fv.txt();
        					s=s.replace("/", ":");
        					name=name.replace("/", ":");
        					fv.findValueInEntry(s);
        				}
        			}else if(s.contains("/") && name.contains(":")) {
    					s=fv.txt();
    					s=s.replace("/", ":");
    					fv.findValueInEntry(s);
        			}
        			s=fv.s();
        			
            		if(name.contains(":")) {
            			if(!s.contains(":") && !Str.isNumeric(s))
            				isGood=false;
            		}else if(!Str.isNumeric(s)){

            			String[] temp=s.split(s);
            			if(temp.length>1) {
                			s=name+fv.symbol()+temp[0];
                			if(!fv.ref().isEmpty())
                				s+=" ("+fv.ref()+")";
                			
                			fv.findValueInEntry(s);
                			if(!Str.isNumeric(fv.s()))
                				isGood=false;            				
            			}else
            				isGood=false;

            		}
            		
            		
            		//System.out.println(" fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" fv.ref="+fv.ref()+" isGood="+isGood+" name="+name);
            		
            		if(isGood) {
            		    
            		    if(prevRef.isEmpty() && !fv.ref().isEmpty() && fvs.size()>0) {
            		        
            		        //System.out.println("1 fv s0="+s0+"  txt="+fv.txt()+" isGood="+isGood+" name="+name+" ref="+fv.ref()+" prevRef="+prevRef);
            		        
            		        Vector<FindValue> tempV=new Vector<FindValue>();
            		        tempV.addAll(fvs);
            		        while(tempV.size()>0) {
                                FindValue prevFV=tempV.lastElement();
                                tempS=prevFV.txt();
                                int p=s0.indexOf(tempS);
                        
                                if(p>=0) {
                                    p=p+tempS.length();
                                    tempS=s0.substring(p).trim();
                                    if(tempS.startsWith(",")) {
                                        tempS=prevFV.txt()+"("+fv.ref()+")";
                                        prevFV.findValueInEntry(tempS);    
                                        tempV.remove(prevFV);
                                        
                                        //System.out.println("2 fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" isGood="+isGood+" name="+name+" ref="+fv.ref()+" prevRef="+prevRef);
                                    }else
                                        break;
                                }else
                                    break;
            		        }
  
            		    }
            		    
                        //System.out.println(" fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" isGood="+isGood+" name="+name+" ref="+fv.ref()+" prevRef="+prevRef);
                        
            		    prevRef=fv.ref();
            		    prevName=name;
            		    
            			fvs.add(fv);
   
            			//for(FindValue f:fvs)
            			//    System.out.println("###"+i+" fv s="+f.s()+" ds="+f.ds()+" txt="+f.txt()+" isGood="+isGood+" name="+name+" ref="+f.ref()+" offset="+offset);
            		}
        		}catch(Exception e) {
        		}
        		
        		offset=n1+marker.length();
        		i=offset;
        		
        		isStartOfNextEntry=true;
        		//System.out.println("offset="+offset+" txt="+str+" next ="+str.substring(offset));
        		continue;
        	}
        	
        	i++;
        	
        	isStartOfNextEntry=false;
        }
        
        return fvs;
    }
    
    public static Vector<FindValue> findDataEntriesInLine(String str) {
        Vector<FindValue> fvs=new Vector<FindValue>();
        str=str.trim();
        if(str.isEmpty())
        	return fvs;
        
        String s0="";
        int n0=str.indexOf("$");
        if(n0>0) {
        	s0=str.substring(0,n0).trim();
        	s0=s0.replace(",", "").replace("(", "").replace(")","").trim();//if s0 is comment head
        	if(s0.isEmpty() || Str.isLetters(s0))
        		str=str.substring(n0+1).trim();
        }
        
        s0=str;
        
        String name="";
        ArrayList<String> ops=new ArrayList<String>(Arrays.asList(">=","<=","|<","|>","=",">","<","~","|?","?","|J","|v","|=","AP","GT","GE","LT","LE"));
        
        while(str.contains("  ")) {
            str=str.replace("  "," ");
        }
        
        
        String[] entries=splitEntryLine(str);
                
        String s="";
        String prevRef="",prevName="";

        //System.out.println("EnsdfUtil 922: "+str+"  "+entries.length);
        
        for(int i=0;i<entries.length;i++) {
        	String entry=entries[i].trim();
        	
        	//System.out.println("#"+entry+"#");
        	
        	if(entry.isEmpty())
        		continue;
        	
    		FindValue fv=new FindValue();
    		fv.findValueInEntry(entry);
    		name=fv.name().trim();
    		
    		String tempEntryStr="";
        	
    		//System.out.println(" i="+i+" fvs size="+fvs.size()+" entry="+entry+" name="+name+" prevName="+prevName+" fv.s="+fv.s()+" fv.ds="+fv.ds()+" unit="+fv.units());
        	
        	if(name.isEmpty() && fvs.size()>0 && prevName.length()>0) {
        		boolean isGoodEntry=false;
        		
            	char c=entry.charAt(0);
            	char c1=' ';
            	if(entry.length()>1 )
            		c1=entry.charAt(1);
            	              
            	//System.out.println("   "+fvs.size()+" entry="+entry+"     prevName="+prevName);
            	
        		if(Character.isDigit(c) || ( (c=='.'||c=='+'||c=='-')&&Character.isDigit(c1)) || ops.contains(c+"") 
        				|| ops.contains(c+c1+"")) {      
             		
        			//System.out.println("###   "+fvs.size()+" entry="+entry+"     prevName="+prevName);
        			
            		String op="";
            		if(Character.isDigit(c) || c=='.' || c=='+' || c=='-')
            			op="=";
            		
            		tempEntryStr=prevName+op+entry;
            		
            		fv=new FindValue();
            		fv.findValueInEntry(tempEntryStr);
            		           		
            		
            		if(Str.isNumeric(entry)) {
            			isGoodEntry=true;
            		}else if(Str.isNumeric(fv.s())) {
            			String ds=fv.ds();
            			if(Str.isNumeric(ds) || ds.contains("+")) {
            				isGoodEntry=true;
            			}else if(fv.ds().isEmpty()) {     				
            				if(fv.units().length()>0 || fv.ref().length()>0) {
            					isGoodEntry=true;
            				}
            			}else if(Str.isLetters(fv.ds()) && fv.ds().length()==2 && ops.contains(fv.ds())) {
            				isGoodEntry=true;
            			}
            		}
            		
            		//System.out.println(tempEntryStr+"-----"+entry+" "+isGoodEntry+" prevName="+prevName);
        		}
       		
        		if(isGoodEntry){
        			name=prevName;
        		}else{
        			continue;
        		}
        	}
        	             	
        	if(name.length()>0 && !Str.isNumeric(name)) {
        		
        		try {
            		
        			if(tempEntryStr.length()>0)
        				s=tempEntryStr;
        			else
        				s=entry;
            		
            		fv=new FindValue();
            		fv.findValueInEntry(s);
            		
            		//System.out.println("entry="+entry+"  s="+s+" fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" fv.ref="+fv.ref()+" name="+name);
            		
            		boolean isGood=true;
            		
            		s=fv.s();
            		
        			if(name.contains("/")) {
        				if(s.contains("/") || s.contains(":")) {
        					s=fv.txt();
        					s=s.replace("/", ":");
        					name=name.replace("/", ":");
        					fv.findValueInEntry(s);
        				}
        			}else if(s.contains("/") && name.contains(":")) {
    					s=fv.txt();
    					s=s.replace("/", ":");
    					fv.findValueInEntry(s);
        			}
        			s=fv.s();
        			
            		if(name.contains(":")) {
            			if(!s.contains(":") && !Str.isNumeric(s))
            				isGood=false;
            		}else if(!Str.isNumeric(s) && !s.isEmpty()){

            			String[] temp=s.split("[\\s]+");
            			if(temp.length>1) {
                			s=name+fv.symbol()+temp[0];
                			if(!fv.ref().isEmpty())
                				s+=" ("+fv.ref()+")";
                			
                			fv.findValueInEntry(s);
                			if(!Str.isNumeric(fv.s()))
                				isGood=false;            				
            			}else
            				isGood=false;

            		}
            		
            		
            		//System.out.println(" fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" fv.ref="+fv.ref()+" isGood="+isGood+" name="+name);
            		
            		if(isGood) {
            		    
            		    if(prevRef.isEmpty() && !fv.ref().isEmpty() && fvs.size()>0) {
            		        
            		        //System.out.println("1 fv s0="+s0+"  txt="+fv.txt()+" isGood="+isGood+" name="+name+" ref="+fv.ref()+" prevRef="+prevRef);
            		        
            		        Vector<FindValue> tempV=new Vector<FindValue>();
            		        tempV.addAll(fvs);
            		        while(tempV.size()>0) {
                                FindValue prevFV=tempV.lastElement();
                                String tempS=prevFV.txt();
                                int p=s0.indexOf(tempS);
                        
                                if(p>=0) {
                                    p=p+tempS.length();
                                    tempS=s0.substring(p).trim();
                                    if(tempS.startsWith(",")) {
                                        tempS=prevFV.txt()+"("+fv.ref()+")";
                                        prevFV.findValueInEntry(tempS);    
                                        tempV.remove(prevFV);
                                        
                                        //System.out.println("2 fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" isGood="+isGood+" name="+name+" ref="+fv.ref()+" prevRef="+prevRef);
                                    }else
                                        break;
                                }else
                                    break;
            		        }
  
            		    }
            		    
                        //System.out.println(" fv s="+fv.s()+" ds="+fv.ds()+" txt="+fv.txt()+" isGood="+isGood+" name="+name+" ref="+fv.ref()+" prevRef="+prevRef);
                        
            		    prevRef=fv.ref();
            		    prevName=name;
            		    
            			fvs.add(fv);
   
            			//for(FindValue f:fvs)
            			//    System.out.println("###"+i+" fv s="+f.s()+" ds="+f.ds()+" txt="+f.txt()+" isGood="+isGood+" name="+name+" ref="+f.ref()+" offset="+offset);
            		}
        		}catch(Exception e) {
        			e.printStackTrace();
        		}
        	}

        }
        
        return fvs;
    }
    
    public static Level finalLevel(ENSDF ens,Gamma g){
        int fli=g.FLI();
        if(fli<0)
            return null;
        
        return ens.levelAt(fli);
        
     }
    
    public static Vector<Gamma> findFeedingGammas(ENSDF ens,Level lev){
        Vector<Gamma> out=new Vector<Gamma>();
        int nLevels=ens.nLevels();
        int index=ens.levelsV().indexOf(lev);
        if(index<0 || index==nLevels-1)
            return out;
        
        for(int i=index+1;i<nLevels;i++){
            Level l=ens.levelAt(i);
            for(int j=0;j<l.nGammas();j++){
                Gamma g=l.gammaAt(j);
                if(g.FLI()==index){
                    out.add(g);
                    break;
                }
            }
        }
        
        return out;
    }
    
    public static Vector<Level> findGammaFeedingLevels(ENSDF ens,Level lev){
        Vector<Level> out=new Vector<Level>();
        Vector<Gamma> feedingGammas=findFeedingGammas(ens,lev);
        
        for(int i=0;i<feedingGammas.size();i++){
            Gamma g=feedingGammas.get(i);
            if(g.ILI()>0)
                out.add(ens.levelAt(g.ILI()));
        }
        
        return out;
    }
    
    public static Vector<Level> findGammaFedLevels(ENSDF ens,Level lev){
        Vector<Level> out=new Vector<Level>();
        for(int i=lev.nGammas()-1;i>=0;i--){
            Gamma g=lev.gammaAt(i);
            int fli=g.FLI();
            if(fli>=0)
                out.add(ens.levelAt(fli));
        }
        return out;
    }
    
    /*
     * find a band level by energy
     */
    public static Level findBandLevel(Band b,String es){
        String s=es.trim();
        if(s.length()==0)
            return null;
        
        for(int i=0;i<b.nLevels();i++)
            if(b.levelAt(i).ES().equals(s))
                return b.levelAt(i);
        
        return null;
    }
   
    public static String replaceFieldOfRecordLineExactPos(String line,int sStart,int sLen,String S,int dsStart,int dsLen,String DS){
        String newLine=line;
        try {
            newLine=Str.replace(newLine, sStart, sLen, S);
            newLine=Str.replace(newLine, dsStart, dsLen, DS);
        }catch(Exception e) {
        	
        }

        return newLine;
    }
    
    public static String replaceFieldOfRecordLineExactPos(String line,int sStart,int sLen,String S){
        return replaceFieldOfRecordLineExactPos(line,sStart,sLen,S,0,0,"");
    }

    
    public static String replaceFieldOfRecordLine(String line,int sStart,int sLen,String S,int dsStart,int dsLen,String DS){
        String newLine=line;
        try {
        	if(DS.length()>dsLen && dsLen>0) {
        		SDS2XDX sx=new SDS2XDX(S,DS);
        		if(Str.isInteger(DS)) {
        			int limit=0;
        			String limitS="";
        			for(int i=0;i<dsLen;i++)
        				limitS+="9";
        			limit=Integer.parseInt(limitS);
        			
        			sx.setErrorLimit(limit);
        			sx=sx.multiply("1.0","0");
        			S=sx.s();
        			DS=sx.ds();
        		}else if(DS.contains("+") && DS.contains("-")) {
        			if(sx.dxu()>sx.dxl())
        				DS=sx.dsu();
        			else
        				DS=sx.dsl();
        		}else if(Str.isLetters(DS)) {
        			DS=DS.substring(0,dsLen);
        		}
        	}
        	
        	if(sStart>0 && newLine.charAt(sStart-1)!=' ' && S.length()<sLen) 
        		newLine=Str.replace(newLine,sStart, sLen, " "+S);
        	else
        		newLine=Str.replace(newLine, sStart, sLen, S);
        	
        	if(dsStart>0 && newLine.charAt(dsStart-1)!=' ' && DS.length()<dsLen) 
        		newLine=Str.replace(newLine,dsStart, dsLen, " "+DS);
        	else
        		newLine=Str.replace(newLine, dsStart, dsLen, DS);

        }catch(Exception e) {
        	
        }

        return newLine;
    }
    public static String replaceFieldOfRecordLine(String line,int sStart,int sLen,String S){
        return replaceFieldOfRecordLine(line,sStart,sLen,S,0,0,"");
    }
    
    public static String replaceEnergyOfRecordLine(String line,String ES,String DES){
    	return replaceFieldOfRecordLine(line,9,10,ES,19,2,DES);
    }

    public static String replaceRIOfRecordLine(String line,String IS,String DIS){
    	return replaceFieldOfRecordLine(line,21,8,IS,29,2,DIS);
    }
 
    /*
     * replace RI record with branching ratios (strongest=100)  
     */
    public static String replaceRIOfGammaLineWithBR(Gamma g){
        if(g.RIS().isEmpty())
            return g.recordLine();
        else
            return replaceRIOfGammaLine(g.recordLine(),g.RelBRS(),g.DRelBRS());
    }
    
    public static String replaceRIOfGammaLine(String line,String IS,String DIS){
    	return replaceFieldOfRecordLine(line,21,8,IS,29,2,DIS);
    }

    public static String replaceMULTOfGammaLine(String line,String MS){
    	return replaceFieldOfRecordLine(line,31,10,MS);
    }
    
    public static String replaceMROfGammaLine(String line,String MRS,String DMRS){
    	return replaceFieldOfRecordLine(line,41,8,MRS,49,6,DMRS);
    }

    public static String replaceCCOfGammaLine(String line,String CCS,String DCCS){
    	return replaceFieldOfRecordLine(line,55,7,CCS,62,2,DCCS);
    }

    public static String replaceTIOfGammaLine(String line,String TIS,String DTIS){
    	return replaceFieldOfRecordLine(line,64,10,TIS,74,2,DTIS);
    }

    //for levels
    public static String replaceHalflifeOfLevelLine(String line,String TS,String DTS,String unit){
        TS=TS+" "+unit.toUpperCase();
        TS=TS.trim();
        
        return replaceFieldOfRecordLine(line,39,10,TS,49,6,DTS);
    }
    
    public static String replaceJPIOfLevelLine(String line,String JPIS){
    	return replaceFieldOfRecordLine(line,21,18,JPIS);
    }

    public static String replaceLOfLevelLine(String line,String LS){
    	return replaceFieldOfRecordLine(line,55,9,LS);
    }
    
    public static String replaceC2SOfLevelLine(String line,String S,String DS){
    	return replaceFieldOfRecordLine(line,64,10,S,74,2,DS);
    }

    public static String replaceQPOfParentLine(String line,String S,String DS){
    	return replaceFieldOfRecordLine(line,64,10,S,74,2,DS);
    }
    
    //for decay
    public static String replaceIEOfECBPLine(String line,String IES,String DIES){
    	return replaceFieldOfRecordLine(line,31,8,IES,39,2,DIES);
    }
    
    public static String replaceHFOfAlphaLine(String line,String HFS,String DHFS){
        return replaceIEOfECBPLine(line,HFS,DHFS);
    }
    
    
    public static String replaceLOGFTOfBetaLine(String line,String LOGFTS,String DLOGFTS){
        return replaceMROfGammaLine(line,LOGFTS,DLOGFTS);
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Vector reverse(Vector in) {
		try {
			Vector out=new Vector();
			for(int i=in.size()-1;i>=0;i--)
				out.add(in.get(i));

			return out;
		}catch(Exception e) {
			
		}
		return in;
	}
	
    public static float findMaxRI(Vector<Gamma> gammasV){
        float maxRI=-1;
        for(Gamma g:gammasV){
            float RI=(float)g.RID();
            if(RI>maxRI)
                maxRI=RI;
        }
        
        return maxRI;
    }
                
    
    /*
     * lines: ENSDF lines
     * recordType:
     *    one-letter type: type character at column 8: L,G,B,E,A,D,N,P,...
     *                     return all lines with column8 letter matching the type, including comments
     *                     continuation records, and so on.
     *    two-letter type: column 7-8, e.g., cL,cG, PN
     *                     return all lines with column7-8 string matching the type
     *  three-letter type: column6-8, e.g., "B G",...
     *                     return all lines with column6-8 matching the type
     *   four-letter type: column6-9, e.g., "  DP",
     *                     return all lines with column6-9 matching the type.
     * Example, to get only the level record lines,  use recordType="  L", three-letter type
     *             get only the level comment lines, use recordType="cL" or "CL", two-letter type
     *             
     *                     
     */
    public static Vector<Vector<String>> findRecordLinesVV(Vector<String> lines,String recordType){
        Vector<Vector<String>> out=new Vector<Vector<String>>();
        if(lines.isEmpty() || recordType.trim().length()==0)
            return out;
        
        String type=recordType.toUpperCase();//DO NOT TRIM
        int sizeOfType=type.length();
        int p1=-1,p2=-1;//indexes in a data line for getting line type
        String firstLineType=""; //column6-9, size=4
        
        if(sizeOfType==1){
            firstLineType="  "+type+" ";
            p1=7;p2=p1+1;
        }else if(sizeOfType==2){
            firstLineType=" "+type+" ";
            p1=6;p2=p1+2;
        }else if(sizeOfType==3){
            firstLineType=type+" ";
            p1=5;p2=p1+3;
        }else if(sizeOfType==4){
            firstLineType=type;
            p1=5;p2=p1+4;
        }else
            return out;
        
        
        Vector<String> recordLines=new Vector<String>();
        for(int i=0;i<lines.size();i++){
            String line=lines.get(i);
            String lineType=line.substring(p1,p2).toUpperCase();
            if(lineType.equals(type)){
                if(line.substring(5,9).equalsIgnoreCase(firstLineType)){
                    if(recordLines.size()>0)
                        out.add(recordLines);
                    recordLines=new Vector<String>();
                }
                recordLines.add(line);
            }
        }
        
        if(recordLines.size()>0)
            out.add(recordLines);
                
        return out;
    }
    
    //find all lines of a record from lineIndex=fromIndex and before next record (despite of type)
    //fromIndex must be the index of the record line
    public static Vector<String> findRecordLinesV(Vector<String> linesV,int fromIndex){
    	Vector<String> out=new Vector<String>();
    	try {
   		    if(fromIndex<0)
   		    	fromIndex=0;
   		    
   		    String line="",lineType="";
   		    char c1=0,c2=0,c3=0;
    		for(int i=fromIndex;i<linesV.size();i++) {
				line=linesV.get(i);
				if(line.trim().isEmpty()) {
					fromIndex+=1;
					continue;
				}
				line=Str.fixLineLength(line, 80);
				lineType=line.substring(5,8);
				c1=lineType.charAt(0);
				c2=lineType.charAt(1);
				c3=lineType.charAt(2);
				
				//first line is not a record line
				if(c1!=' ' || c2!=' ' || c3==' ' || !"LGBEAPNQ".contains(c3+""))
					return out;
				if(!line.trim().isEmpty())
					break;
    		}
    		
    		out.add(line);
    		
   		    char recType=linesV.get(fromIndex).charAt(7);

    		for(int i=fromIndex+1;i<linesV.size();i++) {
				line=linesV.get(i);
				if(line.trim().isEmpty())
					continue;
				
				line=Str.fixLineLength(line, 80);
				lineType=line.substring(5,8);
				c1=lineType.charAt(0);
				c2=lineType.charAt(1);
				c3=lineType.charAt(2);
				
				if(c3!=recType || lineType.substring(0,2).equals("  ")) 
					break;
				
				out.add(line);
    		}

    	}catch(Exception e) {
    		
    	}
	
    	return out;
    }
    
    public static EntryInfo getFieldInfo(String fieldName,String recordLineType) {
        Vector<EntryInfo> recordsV=null;
        try{
        	String s=recordLineType;
        	if(s.length()==2 && "DP".contains(s.charAt(0)+"") && "PNDT".contains(s.charAt(1)+"")){
        		recordLineType=s.charAt(0)+"P";//DP for delayed particle, "PP" for prompt particle
        	}
        		
            recordsV=recordFieldsVMap.get(recordLineType);
            recordsV.get(0);
        }catch(Exception e){
            if(recordFieldsVMap==null || recordFieldsVMap.size()==0){
                initENSDFRecordMap();
                recordsV=recordFieldsVMap.get(recordLineType);
            }
        }
        //System.out.println(fieldName);
        

        try {
            fieldName=fieldName.toUpperCase().trim();
            if(fieldName.isEmpty())
            	return null;
            
            if(fieldName.equals("E"+recordLineType))
            	fieldName="E";
            	
            String name="";
            for(int i=0;i<recordsV.size();i++){
                name=recordsV.get(i).name;     
                
               //System.out.println("*******"+name);
               
                if(name.equals(fieldName.trim())) {
                    
                	//System.out.println(name+"  "+recordsV.get(i).name+" "+recordsV.get(i).pos);
                	
                    return recordsV.get(i);
                }
            }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        
        return null;
    }
    
    public static EntryInfo getFieldInfo(String fieldName) {
        Vector<String> lineTypes=new Vector<String>();
        
        try{
            lineTypes.addAll(recordFieldsVMap.keySet());
            lineTypes.get(0);
        }catch(Exception e){
            if(recordFieldsVMap==null || recordFieldsVMap.size()==0){
                initENSDFRecordMap();
                lineTypes.addAll(recordFieldsVMap.keySet());
            }
        }
        try {
            fieldName=fieldName.toUpperCase().trim();
            if(fieldName.isEmpty())
            	return null;
            
            if(fieldName.startsWith("E") && !fieldName.equals("EI"))
            	fieldName="E";
            
            EntryInfo entryInfo=null;
            for(String type:lineTypes) {
                entryInfo=getFieldInfo(fieldName,type);
                if(entryInfo!=null)
                    return entryInfo;
            }
        }catch(Exception e) {
        	
        }

        
        return null;
    }
    
    /*
     * Get the string of the ENSDF field in the record line 
     * (level/gamma/decay/delay/parent/norm) by fieldName
     * e.g., getField(lev,"E")  return value string of "lev" energy
     *       getField(lev,"DE") return uncertainty string of "lev" energy
     *       getField(lev,"T") return value+unit string of "lev" T1/2
     *       getField(lev,"DT" return uncertainty string of "lev" T1/2
     */
    public static <T extends Record> String getField(T rec,String fieldName) {
        
        String out="";
        try{
            String recordType=getENSDFRecordType(rec.recordLine());
            EntryInfo entryInfo=getFieldInfo(fieldName,recordType);
            int i1=entryInfo.pos;
            int i2=i1+entryInfo.len;
            out=rec.recordLine().substring(i1,i2).trim();
            
        }catch(Exception e){
        }
        
        return out;
    }
    
    /*
     * field names can be names for value fields and uncertainty field, like "DE", "DT"
     */
    public static <T extends Record> Vector<T> findRecordsWithNonEmptyField(Vector<T> recordsV,String fieldName) {
        Vector<T> out=new Vector<T>();
        fieldName=fieldName.toUpperCase().trim();

        if(recordsV.size()==0 || fieldName.isEmpty())
            return out;
        
        try {           
            EntryInfo entryInfo=getFieldInfo(fieldName);    
            
            //System.out.println(" ES="+recordsV.get(0).ES()+"  filename="+fieldName+"  "+(entryInfo=null));
            
            int i1=entryInfo.pos;
            int i2=i1+entryInfo.len;
            
            for(int i=0;i<recordsV.size();i++) {
                try {
                    T rec=(T)recordsV.get(i);
                    String S=rec.recordLine().substring(i1,i2).trim();
                                        
                    if(!S.isEmpty())
                        out.add(rec);
                }catch(Exception e) {
                }
            }
        }catch(Exception e) {
            
        }
    
        return out;
            
        
    }
    
    /*
     * find DSIDs of source datasets for a given data record type (L/G/B/E/A/DP/DN) from a general comment
     * recordType: "L" for level, "G" for gamma, "DP" for delayed proton, "P" for prompt proton (not parent)
     * return map: <flag, Vector of DSIDs>, flag=" " for general comment
     */
    
    public static HashMap<String,HashMap<String,Vector<String>>> findAllRecordSourcesMapFromComment(Comment c){
    	HashMap<String,HashMap<String,Vector<String>>> out=new HashMap<String,HashMap<String,Vector<String>>>();
    	try {
			String bs=c.body();
	
			String s="";
			
			Vector<String> DSIDsV=new Vector<String>();
			
			bs=Str.firstSentence(bs);
			if(bs.toUpperCase().contains("FROM")) {
				if(bs.endsWith(".")) 
					bs=bs.substring(0,bs.length()-1).trim();
	
				String bs0=bs;				
				bs=bs.toUpperCase();
						
				int n=-1,n1=-1,n2=-1,n3=-1;
				int noffset=0;
				boolean toBreak=false;
				int ntries=0;
				
				while((n1=bs.indexOf("FROM",n+1))>=0 || (n2=bs.indexOf(" AND",n+1))>=0 || (n3=bs.indexOf(";",n+1))>=0) {
					if(n1>=0) {
						n=n1;
						noffset=4;
					}else if(n2>=0) {
						n=n2;
						noffset=4;
					}else if(n3>=0) {
						n=n3;
						noffset=1;
					}
					
					//System.out.println("EnsdfUtil 1470: bs="+bs+" n1="+n1+" n2="+n2+" n3="+n3);
					
					n1=bs.indexOf(")",n+1);
					n2=bs.indexOf("FROM",n+1);
					if(n2<0)
						n2=bs.indexOf(" AND",n);
					if(n2<0)
						n2=bs.indexOf(";",n);
					
					s="";
					
					if(n2>n && n2<n1) {//from Coulex for xxx and from A(B,C) for yyy
						String tempS=bs.substring(n+noffset,n2);
						
						n3=tempS.indexOf(" FOR ");
						if(n3<0)
							n3=tempS.indexOf(" UP ");
						
						if(n3>0) {
							s=bs0.substring(n+noffset,n+noffset+n3).trim();
						}else {
							s=bs0.substring(n+noffset,n2).trim();
						}
						
						n=n2-1;	
						
						//System.out.println("EnsdfUtil 1600: n="+n+" n2="+n2);
					}else if(n1>n+10) {//reaction A(B,C)
						if(n1==bs0.length()-1) {
							s=bs0.substring(n+noffset).trim();
							toBreak=true;
						}else {
							char c0=bs0.charAt(n1+1);
							if(c0==' ' && !bs0.substring(n1+2).startsWith("E=")) {//e.g., "Values for levels up to from A(B,C) and others from D(E,F) 
								s=bs0.substring(n+noffset,n1+1).trim();
								n=n1+1;
							}else {
								//e.g., "Values for levels up to from A(B,C):xxx and others from D(E,F):yyy 
								n2=bs0.indexOf(" ",n1+2);//skip n1+1
								if(n2>0) {
    								s=bs0.substring(n+noffset,n2).trim();
    								n=n2;
								}else if(n2==bs0.length()-1) {
    								s=bs0.substring(n+noffset).trim();
    								toBreak=true;
								}
							}
						}
						//System.out.println("EnsdfUtil 1622: n="+n+" n2="+n2);
					}else if(n1<0) {//e.g., "from Coulomb excitation up to xxx,unless otherwise noted"
						String tempS=bs.substring(n+noffset);
						n3=tempS.indexOf(" FOR ");
						if(n3<0)
							n3=tempS.indexOf(" UP ");
						if(n3<0)
							n3=tempS.indexOf(",");
						
						if(n3>0) {
							s=bs0.substring(n+noffset,n+noffset+n3).trim();
						}else {
							s=bs0.substring(n+noffset).trim();
						}

						if(n2<0)
							toBreak=true;
						else
							n=n2-1;

						//System.out.println("EnsdfUtil 1642: bs="+bs+" len="+bs.length()+" n="+n+" n2="+n2+" n3="+n3);
					}

					if(s.endsWith(","))
						s=s.substring(0,s.length()-1).trim();
					if(s.endsWith(";"))
						s=s.substring(0,s.length()-1).trim();
					
					if(s.length()>0 && !DSIDsV.contains(s)) {
						
						DSIDsV.add(s);
					}
					
					//System.out.println("EnsdfUtil 1542s: bs="+bs+" n="+n+"  n1="+n1+" n2="+n2+" n3="+n3);
					
					//System.out.println("EnsdfUtil 1541: s="+s);
					
					if(toBreak)
						break;
					
					
					n1=-1;
					n2=-1;
					n3=-1;
					
					ntries++;
					
					//avoid unexpected infinite loop
					if(ntries>50)
						break;
				}
				
				for(int i=0;i<c.headV().size();i++) {//headers in "E,E(A),RI(C,D)" is E,E,RI and flag in flagV are "","(A)","(C,D)"
					String recordType=c.headAt(i);
					
					HashMap<String,Vector<String>> tempMap=new HashMap<String,Vector<String>>();
					
					if(c.flagV().get(i).isEmpty()) {
						tempMap.put(" ", DSIDsV);
					}else {
						for(int j=0;j<c.flagVV().get(i).size();j++) {
							String flag=c.flagAt(i, j).trim();
							if(flag.isEmpty())
								flag=" ";
							
							tempMap.put(flag, DSIDsV);
						}
					}

					out.put(recordType, tempMap);
				}
			}
    	}catch(Exception e) {
    		
    	}
    	
    	return out;
    }
    
    public static HashMap<String,Vector<String>> findRecordFlagSourceDSIDsMapFromComment(Comment c,String recordType){
    	HashMap<String,Vector<String>> out=new HashMap<String,Vector<String>>();
    	try {
    		HashMap<String,HashMap<String,Vector<String>>> baseMap=findAllRecordSourcesMapFromComment(c);
    		recordType=recordType.toUpperCase().trim();
    		if(baseMap.containsKey(recordType))
    			out=baseMap.get(recordType);
    		
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return out;
    }
    
    /*
     * field names are names for value field only
     */
    public static <T extends Record> Vector<T> findRecordsByFieldWithValue(Vector<T> recordsV,String fieldName) {
        Vector<T> out=new Vector<T>();
        fieldName=fieldName.toUpperCase().trim();

        if(recordsV.size()==0 || fieldName.isEmpty())
            return out;

        int i1=0,i2=0;
        if(fieldName.equals("E")) {
            i1=9;
            i2=19;
        }else if("RI,IB,IA,IP".indexOf(fieldName)%3==0 && fieldName.length()==2) {
            i1=21;
            i2=29;
        }else if(fieldName.equals("IE") || fieldName.equals("HF")) {
            i1=31;
            i2=39;
        }else if(fieldName.equals("MR") || fieldName.equals("LOGFT")) {
            i1=41;
            i2=49;
        }else if(fieldName.equals("T")) {
            i1=39;
            i2=49;
            
        }
        
        for(int i=0;i<recordsV.size();i++) {
            try {
                T rec=(T)recordsV.get(i);
                String S=rec.recordLine().substring(i1,i2).trim();
                if(Str.isNumeric(S))
                    out.add(rec);
                else if(!S.isEmpty() && fieldName.equals("T")){//for T1/2 value+unit
                    S=S.trim();
                    int p=S.indexOf(" ");
                    if(p>0 && Str.isNumeric(S.substring(0,p))) {
                        out.add(rec);
                    }
                }
            }catch(Exception e) {
            }

        }
        
        return out;
    }
    
    /*
     * with numerical uncertainty (so uncertainty=LT,GT,LE,GE,AP,CA excluded)
     * field names are names for value fields only
     */
    public static <T extends Record> Vector<T> findRecordsByFieldWithUncertainty(Vector<T> recordsV,String fieldName) {
        Vector<T> out=new Vector<T>();
        fieldName=fieldName.toUpperCase().trim();

        if(recordsV.size()==0 || fieldName.isEmpty())
            return out;

        int i1=0,i2=0;
        if(fieldName.equals("E")) {
            i1=19;
            i2=21;
        }else if("RI,IB,IA,IP".indexOf(fieldName)%3==0 && fieldName.length()==2) {
            i1=29;
            i2=31;
        }else if(fieldName.equals("IE") || fieldName.equals("HF")) {
            i1=39;
            i2=41;
        }else if(fieldName.equals("T") || fieldName.equals("MR") || fieldName.equals("LOGFT")) {
            i1=49;
            i2=55;
        }else if(fieldName.equals("TI") || fieldName.equals("S")) {
        	i1=44;
        	i2=46;
        }
        
        for(int i=0;i<recordsV.size();i++) {
            try {
                T rec=(T)recordsV.get(i);
                String DS=rec.recordLine().substring(i1,i2).trim();
                if(Str.isNumeric(DS))
                    out.add(rec);
                else if(DS.contains("+") || DS.contains("-")){
                	DS=DS.replace("+", "").replace("-", "").trim();
                	if(Str.isNumeric(DS))
                		out.add(rec);
                }
            }catch(Exception e) {
            }

        }
        
        return out;
    }
    
    /**
     * Does this band contain the specified level?
     */
    public static boolean contains(Band b,Level lev){
        
        if(lev==null)
            return false;
        
        if(b.levels().contains(lev))
            return true;
        
        /*
        for (int x=0; x<b.nLevels(); x++){
            Level l = b.levelAt(x);
            if (l.ES().equals(lev.ES()))
                return true;
        }
        */
        return false;
    }
    
    
    /*
     * to be improved
     */
    @SuppressWarnings("rawtypes")
	public static Level findAdoptedLevel(Level lev,ENSDF adopted,int nsigma) {
    	try {
    		if(!lev.nucleus().nameENSDF().equals(adopted.nucleus().nameENSDF()))
    			return null;
    		
    		Vector<Level> matchedLevels=new Vector<Level>();
    		
    		
        	float delta=10;
    		Vector temp=findMatchesByEnergyEntry(lev, adopted.levelsV(), delta,nsigma);

    		
    		for(int i=0;i<temp.size();i++) {
    			matchedLevels.add((Level)temp.get(i));  	
    			//System.out.println(lev.ES()+" $#  "+((Level)temp.get(i)).ES());
    		}
    		Level adoptedLev=(Level) findClosestByEnergyValue(lev,adopted.levelsV());
    		//System.out.println(lev.ES()+"  aopdted="+adoptedLev.ES());
    		
    		float e0=lev.EF();
    		float e1=adoptedLev.EF();
    		String jp0=lev.JPiS().replace("(","").replace(")","");
    		String jp1=adoptedLev.JPiS().replace("(","").replace(")","");
    		
			if(!jp0.isEmpty() && jp0.equals(jp1))
				return adoptedLev;
			
			Level best=null;
    		if(matchedLevels.size()<=1) {
    			if(Math.abs(e0-e1)>20)
    				return null;
    					
    			return adoptedLev;
    		}else if(!jp0.isEmpty()){
    			float minDiff=10000;
    			float diff=0;
    			for(Level l:matchedLevels) {
    				String tempJP=l.JPiS().replace("(","").replace(")","");
    				if(l!=adoptedLev && tempJP.equals(jp0) && isGammasConsistent(lev, l, delta, false,nsigma)) {
    					diff=Math.abs(l.EF()-e0);
    					if(diff<minDiff) {
    						best=l;
    						minDiff=diff;
    					}
      				}
    			}
    		}
  
    		if(best==null) {
    			float minDiff=10000;
    			float diff=0;
    			for(Level l:matchedLevels) {
    				if(isGammasConsistent(lev, l, delta, false,nsigma)) {
    					diff=Math.abs(l.EF()-e0);
    					if(diff<minDiff) {
    						best=l;
    						minDiff=diff;
    					}
      				}
    			}
    		}

			if(best!=null)
				return best;
			
    		return adoptedLev;
    	}catch(Exception e) {
    		
    	}

    	return null;
    }
    
    /*
     * find all keynumbers with valid 8-character format
     */
    public static Vector<String> findKeyNumbers(String line){
        Vector<String> keynumbers=new Vector<String>();
        int i=0;
        char c;
        while(i<=line.length()-8){
            c=line.charAt(i);
            if(Character.isDigit(c) && c!='0'){
                if(!Character.isDigit(line.charAt(i+1))){i+=2;continue;}
                else if(!Character.isDigit(line.charAt(i+2))){i+=3;continue;}
                else if(!Character.isDigit(line.charAt(i+3))){i+=4;continue;}
                else if(!Character.isLetter(line.charAt(i+4))){i+=5;continue;}
                else if(!Character.isLetter(line.charAt(i+5))){i+=6;continue;}  
                else if(!Character.isDigit(line.charAt(i+6)) && !Character.isLetter(line.charAt(i+6))){i+=7;continue;}
                else if(!Character.isDigit(line.charAt(i+7)) && !Character.isLetter(line.charAt(i+7))){i+=8;continue;}
                
                int n=Integer.parseInt(line.substring(i, i+4));
                if(n<1900 || n>2100){
                    i=i+8;
                    continue;
                }
                
                if( (i<line.length()-8) && (Character.isDigit(line.charAt(i+8)) || Character.isLetter(line.charAt(i+8))) ) {
                    i=i+8;
                    continue;
                }
                
                keynumbers.add(line.substring(i,i+8));
                i=i+8;
                continue;
            }
            i++;
        }
        
        return keynumbers;
    }
    
    /*
     * out[0]: NUCID
     * out[1]: line Type
     * out[2]: line body
     */
    public static String[] findENSDFLineInfo(String line) {
    	String[] out=new String[3];
    	out[0]="";
    	out[1]="";
    	out[2]=line;
    	try {
    		char c1=line.charAt(5);
    		char c2=line.charAt(8);
    		
    		if(c1==' ' && c2==' ' && !line.substring(0,5).trim().isEmpty() && !line.substring(5,9).trim().isEmpty()) {
    			//normal ENSDF line
    			out[0]=line.substring(0,5);
    			out[1]=line.substring(6,9);
    			out[2]=line.substring(9).trim();
    			return out;
    		}
    		
    		//line is not a normal ENSDF line, e.g., beginning spaces are trimmed or no space between NUCID and comment body 
        	line=line.trim();
        	
        	int k=0;
        	String AS="";
        	while(Character.isDigit(line.charAt(k)) && k<3) {
        		AS+=line.charAt(k);
        		k++;
        	}
        	
        	//System.out.println("AS="+AS+" line="+line);
        	
        	//no AS, then the whole line is treated as the line body
        	if(!Str.isInteger(AS)) {
        		return out;
        	}
        	
        	String EN="",NUCID="",lineType="",lineBody="";
        	int count=0;
        	while(Character.isLetter(line.charAt(k)) && count<2) {
        		EN+=line.charAt(k);
        		k++;
        		count++;
        	}
        	       	
        	if(EN.length()>0) {
        		NUCID=AS+EN;
        	}else {
        		//has AS but no EN, this is treated as a line in a mass-chain comment
        		NUCID=AS;
        	}
        	//System.out.println("NUCID="+NUCID+"*");
        	
        	String s0=line.substring(k).trim();
        	
    		String s=s0.toUpperCase();
    		c1=s.charAt(0);
    		c2=s.charAt(1);
    		
    		char c3=s.charAt(2);
    		char c4=s.charAt(3);
    		
    		
        	//System.out.println(c1+"$"+c2+"$"+c3+"$"+c4);
        	
    		if(c2==' ') {
    			if(EnsdfConfig.recordLineType.contains(c1+"") || (c1=='C'||c1=='D')) {
    				lineType=s0.substring(0,2);
    			}else if( (c3!=' '&&EnsdfConfig.recordLineType.contains(c3+"")) || (c3==' '&&(c4=='P')||c4=='N') ) {//the latter for prompt particle decay
        			//continuation line
    				if(c3==' ' || (c3=='D'&&"PNT".contains(c4+"")) || (c3=='P'&&c4=='N') ) {
    					lineType=s0.substring(0,4);
    				}else {
    					lineType=s0.substring(0,3);
    				}
    			}else {
    				return out;
    			}
    		}else {
    			//comment line
    			//if( (c1=='C'||c1=='D') && EnsdfConfig.recordLineType.contains(c2+"") ) {
    			if( (c1=='C'||c1=='D') ) {
    				//e.g., CL, DL
    				if(c2=='P' && c3=='N') {
    					lineType=s0.substring(0,3);
    				}else if(c2=='D' && "PNT".contains(c3+"")) {
    					lineType=s0.substring(0,3);
    				}else if(EnsdfConfig.recordLineType.contains(c2+"")) {
    					lineType=s0.substring(0,2);
    				}else {
    					lineType=s0.substring(0,1);
    				}
    			//}else if( (c2=='C'||c2=='D') && EnsdfConfig.recordLineType.contains(c3+"") ) {
    			}else if( (c2=='C'||c2=='D') ) {
    				//e.g., 2CL, xDL
       				if(c3=='P' && c4=='N') {
    					lineType=s0.substring(0,4);
    				}else if(c3=='D' && "PNT".contains(c4+"")) {
    					lineType=s0.substring(0,4);
    				}else if(EnsdfConfig.recordLineType.contains(c3+"")) {
    					lineType=s0.substring(0,3);
    				}else {
    					lineType=s0.substring(0,2);
    				}
    			}else {
    				return out;
    			}
    		}
            
    		if(lineType.length()>0) {
    			lineBody=s0.substring(lineType.length()).trim();
    		}else {
    			lineBody=s0.trim();
    		}
    		
    		//System.out.println("line="+line+" NUCID="+NUCID+" type="+lineType+" body="+lineBody);
    		
        	out[0]=NUCID;
        	out[1]=lineType;
        	out[2]=lineBody;
    	}catch(Exception e) {
    		
    	}
    	
    	return out;
    }

    
    public static String findFirstKeyNumber(String line){
        String keynumber="";
        int i=0;
        char c;
        while(i<=line.length()-8){
            c=line.charAt(i);
            if(Character.isDigit(c) && c!='0'){
                if(!Character.isDigit(line.charAt(i+1))){i+=2;continue;}
                else if(!Character.isDigit(line.charAt(i+2))){i+=3;continue;}
                else if(!Character.isDigit(line.charAt(i+3))){i+=4;continue;}
                else if(!Character.isLetter(line.charAt(i+4))){i+=5;continue;}
                else if(!Character.isLetter(line.charAt(i+5))){i+=6;continue;}  
                else if(!Character.isDigit(line.charAt(i+6)) && !Character.isLetter(line.charAt(i+6))){i+=7;continue;}
                else if(!Character.isDigit(line.charAt(i+7)) && !Character.isLetter(line.charAt(i+7))){i+=8;continue;}
                
                int n=Integer.parseInt(line.substring(i, i+4));
                if(n<1900 || n>2100){
                    i=i+8;
                    continue;
                }
                
                if( (i<line.length()-8) && (Character.isDigit(line.charAt(i+8)) || Character.isLetter(line.charAt(i+8))) ) {
                    i=i+8;
                    continue;
                }
                
                return line.substring(i,i+8);
            }
            i++;
        }
        
        return keynumber;
    }
    
    
    public static String findKeynumberByHistoryLine(History hist) {
    	String keyno="";
		try {
			
			if(!Translator.hasInit())
				Translator.init();
			
			//System.out.println(hist.aut());
			//System.out.println(hist.authorsV().size());
			//System.out.println(hist.cit());
			
			String line0=hist.lines().get(0);
			String AS=line0.substring(0,3).trim();
			String cit=hist.cit();
			
			String yearS="";
			
			int n1=cit.indexOf("(");
			int n2=cit.indexOf(")",n1);
			if(n1>0 && n2>0) {
				yearS=cit.substring(n1+1,n2).trim();
			}
			//System.out.println(n1+"   "+n2+"  year="+yearS);
			//for(Author a:hist.authorsV()) {
			//	System.out.println("Author: last="+a.lastName);
			//}
			if(yearS.isEmpty() || yearS.length()!=4 || !Str.isInteger(yearS))
				return "";
			
			if(hist.authorsV().size()==0)
				return "";
			
			String firstAuthor=hist.authorsV().get(0).lastName;
			if(firstAuthor.isEmpty())
				return "";
			
			
			SearchParameter sp=new SearchParameter();
			sp.addSearchCondition("nuclide","a="+AS);
			//sp.addSearchCondition("topic","A-M");
			sp.addSearchCondition("year-min",yearS);
			sp.addSearchCondition("year-max",yearS);
			sp.addSearchCondition("first-author",firstAuthor);
			sp.measuredOnly=false;
			
			Vector<String> keynosV=BaseNSR.downloadKeynumbersByParam(sp);
			
			//Vector<String> keynosV1=BaseNSR.downloadKeynumbersOfMassByNuclide("74Ni",2022);
			//System.out.println(keynosV1.size());
			//for(String key:keynosV) {
				//System.out.println(key);
				
			//	String s=BaseNSR.NSRLinkManager(key,'B');
				//System.out.println(s);
			//}
			
			if(keynosV.size()>0) {
				keyno=keynosV.get(0);
			}
		}catch(Exception e) {
			
		}
    	
    	return keyno;
    }
    
    public static NSRAbstract findNSRAbstractByHistoryLine(History hist) {
    	NSRAbstract nsrAbs=null;
		try {
			
			if(!Translator.hasInit())
				Translator.init();
			
			//System.out.println(hist.aut());
			//System.out.println(hist.authorsV().size());
			//System.out.println(hist.cit());
			
			String line0=hist.lines().get(0);
			String AS=line0.substring(0,3).trim();
			String cit=hist.cit();
			
			String yearS="";
			
			int n1=cit.indexOf("(");
			int n2=cit.indexOf(")",n1);
			if(n1>0 && n2>0) {
				yearS=cit.substring(n1+1,n2).trim();
			}
			//System.out.println(n1+"   "+n2+"  year="+yearS);
			//for(Author a:hist.authorsV()) {
			//	System.out.println("Author: last="+a.lastName);
			//}
			if(yearS.isEmpty() || yearS.length()!=4 || !Str.isInteger(yearS))
				return null;
			
			if(hist.authorsV().size()==0)
				return null;
			
			String firstAuthor=hist.authorsV().get(0).lastName;
			if(firstAuthor.isEmpty())
				return null;
			
			
			SearchParameter sp=new SearchParameter();
			sp.addSearchCondition("nuclide","a="+AS);
			//sp.addSearchCondition("topic","A-M");
			sp.addSearchCondition("year-min",yearS);
			sp.addSearchCondition("year-max",yearS);
			sp.addSearchCondition("first-author",firstAuthor);
			sp.measuredOnly=false;
			
			Vector<NSRAbstract> nsrAbsV=BaseNSR.downloadAndParseNSRsbyParam(sp);
			
			if(nsrAbsV.size()>0) {
				nsrAbs=nsrAbsV.get(0);
			}
		}catch(Exception e) {
			
		}
    	
    	return nsrAbs;
    }
    /**
     * Returns true if the band contains the two levels and lev1
     * is the level directly above lev2.
     */
    public static boolean isNext(Band b,Level lev1,Level lev2){
        
        /*
        int x;
        Level nlev=null,lev=null;
        for (x=0; x<b.nLevels()-1; x++){
            lev  = b.levelAt(x);
            nlev = b.levelAt(x+1);
            if (lev1.ES().equals(nlev.ES()) && 
                    lev2.ES().equals(lev.ES())){
                return true;
            }
        }
        */
        
        int i1=b.levels().indexOf(lev1);
        int i2=b.levels().indexOf(lev2);
        
        if(i1==(i2+1) && i2>=0)
            return true;
        
        return false;
    }
    
    /**
     * Level objects return their energy in different places.  Simplify access.
     */
    public static float e(Level lev){
        
        /*NOTE that: the energy for such level like "123.4+x" has been assigned based on energy of previous level*/
        
        float el = lev.EF();
        String NNES=lev.NNES();
        
        //debug
        //if(lev.NNES().length()>0) System.out.println("In Ens.java: level ES="+lev.ES()+" E="+lev.EF()+" erf="+lev.ERF()+" erfp="+lev.ERPF()+" lev.NNES()="+lev.NNES());
        
        if (el==-1f || (NNES.length()>0 && !NNES.equals("E"))){
            // if EF is negative, it's probably a 123.4+X style level--obsolete
            //level like 123.4+X is assigned an estimated energy in ENSDF.java
            //the level-energy difference of estimated energies could be not proportional to the actual differences
            //Refer to ENSDF.java for details, line 521
            
            //erf=EF if level energy is numerical, erf=0 if level="X", "SN", erf=numerical number, eg, 123.4, in level string if level="123.4+X"
            //Refer to ENSDF.java for details
            
            //ERPF==offset+erf, eg, offset=energy of previous level of "X"+0.1 for in "123.4+X", erf=123.4
            //Refer to ENSDF.java for details
            el = lev.ERPF();
            if (el==-1f){
                return 0f;
                // if ERF is negative, it's probably a 'X' style label
                /*System.out.printf("%s %f %f\n",
                    lev.ES(),
                    new Float(lev.EF()),
                    new Float(lev.ERF()));*/
            }
        }

        
        
        return el;
    }
    
    //recoil correction from measured EG
    //EG:   in unit of keV
    //Mass: in unit of u (1u=931.494 MeV)
    //return corrected EG in keV (=level-energy difference)
    public static double EGRecoilCorr(double EG,double mass) {
    	EG=EG/1000;
    	mass=mass*931.494;
    	//System.out.println(EG+"  "+mass);
    	return 1000*EG*EG/(Math.sqrt(mass*mass+EG*EG)+mass)+1000*EG;
    }
    
    //de-correct recoil correction from already-corrected EG (=level-energy difference)
    //return de-corrected EG (equivalent to measured)
    public static double EGRecoilDeCorr(double EG,double mass) {
    	EG=EG/1000;
    	return 1000*EG-500*EG*EG/(EG+mass*931.494);
    }
    
    public static char pi(String jpis){
        try{
        	String s=jpis.trim();
        	if(s.contains(",") || s.contains(":") || s.toUpperCase().contains("TO")) {
        		//System.out.println("###"+s);
        		
        		String[] jpA=parseJPI(s);   
        		
        		if(jpA.length==1 && jpA[0].equals(s)) {
        			//avoid infinite loop when something is wrong
        			return ' ';
        		}
        		//System.out.println("EnsdfUtil 2331 @@@"+s+"  "+jpA.length);      		
        		
        		char p=' ';
        		for(int i=0;i<jpA.length;i++) {
        		
        			char p1=pi(jpA[i]);

        			if(p1==' ' || (p!=' '&&p1!=p) )
        				return ' ';
        			
        			p=p1;
        		}
        		
        		return p;
        	}else {
                String js=s.replace("(","").replace(")","").trim();
                if(js.endsWith("+"))
                    return '+';
                else if(js.endsWith("-"))
                    return '-';
        	}       	 	
        	
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        return ' ';
    }
    
    public static char pi(Level lev){
        try{
            return pi(lev.JPiS());
        }catch(Exception e){}
        
        return ' ';
    }
    
    /*
     * 1 for +, -1 for -, 0 for others
     */
    public static int parityNumber(Level lev){
        char p=pi(lev);
        if(p=='+')
            return 1;
        else if(p=='-')
            return -1;
        else
            return 0;
    }
    
    public static int parityNumber(String js){
        char p=pi(js);
        if(p=='+')
            return 1;
        else if(p=='-')
            return -1;
        else
            return 0;
    }
    

    /*
     * parityChange: 1 for no, -1 for yes
     * 
     * set pc=(parityChange-1)/2, then 
     *     0 for no
     *    -1 for yes
     *    
     * if (L+pc) is odd,  then MUL=ML
     * if (L+pc) is even, then MUL=EL 
     */
    public static String MUL(int L,int parityChange){
        if(parityChange!=1 && parityChange!=-1)
            return MUL(L);  
        
        int pc=(parityChange-1)/2;
        if((L+pc)%2==0)
            return "E"+L;
        else
            return "M"+L;
    }
    
    
    public static String MUL(int L){
        String[] ms={"","D","Q","O","S"};
        try{
            return ms[L];
        }catch(Exception e){}
            
        return "";
    }
    
    public static Vector<String> MULs(int L){
        Vector<String> out=new Vector<String>();
        out.add(MUL(L,1));
        out.add(MUL(L,-1));
        out.add(MUL(L));
        
        return out;
    }
    
    /**
     * Simplify api.
     */
    public static float e(Gamma g){
       return g.EF();
    }
    /**
     * Get energy string.  Maintains original presentation.
     */
    //public static String str(Level l){
    //    String s = l.lineAt(0);
    //    return s.substring(10,10).trim();
    //}
    
    

    
    /**
     * Get an array of all levels not in the supplied array
     */
    public static Level[] inverse(ENSDF ens,Level[] lev){
        Vector<Level> v = new Vector<Level>();
        for (int x=0; x<ens.nLevels(); x++){
            Level tmp = ens.levelAt(x);
            boolean found=false;
            for (int y=0; y<lev.length; y++){
                if (tmp.equals(lev[y])){
                    found=true;
                    break;
                }
            }
            if (!found)
                v.add(tmp);
        }
        Level[] la = new Level[v.size()];
        v.copyInto(la);
        System.out.printf("inverse %d -> %d\n",lev.length,la.length);
        return la;
    }
    
    public static String DSID2Filename(String dsid){
        String filename="";
        
        String dsType=findDSIDType(dsid);

        if(dsType.equals("reaction")){
            int p1=dsid.indexOf("(");
            int p2=dsid.indexOf(")");
            filename=dsid.substring(p1+1,p2).trim().replace(",","_").toLowerCase();
            String temp=dsid.substring(p2).trim();
            if(temp.length()>0){

                temp=Str.makeFilenameString(temp);
                
                if(temp.length()>0 && temp.charAt(0)!='_')
                    filename+='_'+temp;
                else
                    filename+=temp;
            }

            filename=filename.replace(" ","_");
            
            temp=dsid.substring(0,p1).trim();
            if(temp.length()>0)
                filename=temp.toLowerCase()+'_'+filename;
                
        }else if(dsType.equals("decay")){
            filename="";
            int n=-1;
            
            String decayType=findDecayTypeInDSID(dsid);
            if(decayType.equals("B-"))
                decayType="beta";
            else if(decayType.equals("B+") || decayType.equals("EC"))
                decayType="ec";
            else if(decayType.equals("A"))
                decayType="a";
            else{
                n=dsid.indexOf(decayType);
                if(n>0){
                    filename+=Str.makeFilenameString(dsid.substring(0,n)).trim();
                    if(filename.length()>0 && filename.charAt(filename.length()-1)!='_')
                        filename+='_';
                }
                
                if(decayType.equals("FISSION"))
                    decayType="f";          
                else
                    decayType=decayType.toLowerCase();
            }
            

            filename+=decayType+"_decay";
            
            n=dsid.indexOf("DECAY");
            String temp=dsid.substring(n+5).trim();
            if(temp.length()>0){
                temp=Str.makeFilenameString(temp);
                
                if(temp.length()>0 && temp.charAt(0)!='_')
                    filename+='_'+temp;
                else
                    filename+=temp;
            }
            
        }else if(dsType.equals("neutron")){
            filename="ng";
            int n=dsid.indexOf("(N,G)");
            
            String temp=dsid.substring(n+5).trim();
            if(temp.length()>0){
                temp=Str.makeFilenameString(temp);
                
                if(temp.length()>0 && temp.charAt(0)!='_')
                    filename+='_'+temp;
                else
                    filename+=temp;
            }
            
            /*
            int p1=dsid.indexOf(":");
            int p2=-1;
            if(p1<0){
                p1=dsid.indexOf("(",n+5);
            }
            
            if(p1>0){
                p2=dsid.indexOf(",",p1);
                if(p2<0)
                    p2=dsid.length();
                temp="_"+dsid.substring(p1+1, p2).toLowerCase();
                temp=temp.replace("e=","E_");
                temp=temp.replace(")","");
                temp=temp.replace("(","");
                temp=temp.trim();
                if(temp.length()>1)
                    filename+=temp;
            }
            */
        }else if(dsType.length()>0){
            //adopted,coulex,inelastic,comments,references, and others
            String temp="";
            String type=dsType;
            int n=-1;
            if((n=dsid.indexOf("COULOMB EXCITATION"))>=0){
                temp=dsid.substring(n+18).trim();
            }else if((n=dsid.indexOf("INELASTIC SCATTERING"))>=0){
                temp=dsid.substring(n+20).trim();
            }else if(dsid.indexOf("ADOPTED")==0 && type.equals("adopted")){
                temp="";
            }else{
                type="";
                temp=dsid.trim();
            }
                        
            filename=type;
            if(temp.length()>0){
                temp=Str.makeFilenameString(temp);
                
                if(filename.length()>0 && temp.length()>0 && temp.charAt(0)!='_')
                    filename+='_'+temp;
                else
                    filename+=temp;
            }
            
        }else{
            filename=Str.makeFilenameString(dsid.trim());
        }       
        
        filename=filename.replaceAll("'", "P");
        return filename;
    }
    
    public static String makeDocumentFilenameByDSID(String dsid){
        String filename="";

        filename=EnsdfUtil.DSID2Filename(dsid).trim();
        if(filename.length()==0)
            return "";
        
        return filename+"_documents";
    }
    
    //make filename using DSID from the first line of a data block (dataset)
    public static String makeDocumentFilename(Vector<String> block){    
        if(block==null || block.size()==0)
            return "";
        
        String firstLine=block.elementAt(0);
        String DSID=EnsdfUtil.getDSIDFromLine(firstLine);
        String NUCID=firstLine.substring(0,5).trim();
        String s="";
        if(NUCID.length()>0)
            s=NUCID.toLowerCase()+"_";
        
        return s+makeDocumentFilenameByDSID(DSID);
    }
    
    public static String makeLabelFromDSID(String dsid){
    	return makeLabelFromDSID(dsid,false);
    }
    //make a label in lower-case "c" comment converted from DSID0 
    public static String makeLabelFromDSID(String dsid,boolean keepTargetID){
        String out="";
        if(dsid.contains("DECAY")){
            try {
                out = Translator.procGenCom(dsid,0,false);
            } catch (Exception e) {
                out=dsid.toLowerCase();
                e.printStackTrace();
            }
            return out.replaceAll("~", " ");
        }
        
        int lb=dsid.indexOf("(");
        int rb=dsid.indexOf(")");
        int ic=dsid.indexOf(",");
        if(!keepTargetID && lb>=0 && lb<ic && ic<rb){
            int is=dsid.indexOf(":");
            try{
                if(is==(rb+1) || dsid.contains("E="))
                    return Translator.procGenCom(dsid.substring(lb),0,false);
                else {
                    return Translator.procGenCom(dsid.substring(lb,rb+1),0,false);
                }
            }catch(Exception e){
                out=dsid.toLowerCase();
                e.printStackTrace();
            }

        }
            
        try {
            out=Translator.procGenCom(dsid,0,false);
        } catch (Exception e) {
            out=dsid.toLowerCase();
            e.printStackTrace();
        }
        
        return out;
    }
    
    public static String findDSIDType(String dsid){
        String dsType="";

        
        if( dsid.startsWith("ADOPTED") ) 
            return "adopted";
        if( dsid.startsWith("COMMENTS") ) 
            return "comments";
        if( dsid.startsWith("REFERENCES") ) 
            return "references";
        if( dsid.indexOf("COULOMB EXCITATION") >= 0 ) 
            return "coulex" ;        
        if( dsid.indexOf("(N,G)") >= 0 ) 
            return "neutron" ; 
        if( dsid.indexOf("INELASTIC") >= 0 ) 
            return "inelastic" ; 
        if(dsid.indexOf("(HI,XNG")>=0 || dsid.indexOf("(HI,XG")>=0)
            return "reaction";
        
        int p=dsid.indexOf("DECAY");
        int plb=dsid.indexOf("(");
        int prb=dsid.indexOf(")");
        int p1=dsid.indexOf(",");
        int p2=dsid.indexOf(":");
        
        if(plb>0 && plb<p1 && p1<prb)
            return "reaction";
        
        if(p>0 && dsid.indexOf("ADOPTED") < 0 ){
            if(p2<0 || p<p2)
                return "decay";
        }
                  
        return dsType;
    }
    
    public static String findDecayTypeInDSID(String dsid){
        String decayTypeInDSID="";
        String dsType=findDSIDType(dsid);
        if(!dsType.equals("decay"))
            return "";
        
        if( dsid.indexOf("DECAY")  > 0  && dsid.indexOf("ADOPTED") < 0 ) {
            
            int index=dsid.indexOf("DECAY") ;
            String temp=dsid.substring(0,index).trim();
            index=temp.lastIndexOf(" ");
            decayTypeInDSID=temp.substring(index+1).trim();
            
        }
        
        return decayTypeInDSID;
    }
    
    //Note: tag here is the new tag displayed in the output,
    //      not the old tag in the input file
    //      tag() and newTag() of XRef returns upper-case letter(s) 
    //      oldTag() of XRef returns original character in the XREF list of input file
    public static XRef findXRef(Vector<XRef> XRefV,String tag){

        if(XRefV.size()<=0 || tag.length()<=0)
            return null;
        
        XRef xR;
        for(int i=0;i<XRefV.size();i++){
            xR=XRefV.get(i);
            if(xR.tag().equals(tag.toUpperCase()))//xR.tag() return newTag
                return xR;
        }
        
        return null;
    }
    
    public static XRef findXRefByOldXTag(Vector<XRef> XRefV,String oldTag){

        if(XRefV.size()<=0 || oldTag.length()<=0)
            return null;
        
        XRef xR;
        for(int i=0;i<XRefV.size();i++){
            xR=XRefV.get(i);
            
            //System.out.println("In EnsdfUtil line 503: oldTag="+oldTag+" xR.newTag="+xR.newTag()+" xR.oldTag="+xR.oldTag()+" DSID0"+xR.DSId0());
            
            if(xR.oldTag().equals(oldTag))
                return xR;
        }
        
        return null;
    }
    
    public static String findDSID0ByOldXTag(ENSDF adopted,String oldTag){
        Vector<XRef> XRefV=adopted.XRefV();
        XRef xR=findXRefByOldXTag(XRefV,oldTag);
        if(xR!=null)
            return xR.DSId0();
        
        return "";
    }
    
    //return XREF tag of individual dataset in Adopted
    public static String findXTagInAdopted(ENSDF ens, ENSDF adopted){
        int nxref=0;
        XRef xref;
        if(ens.lineAt(0).indexOf("ADOPTED LEVELS")==9) 
            return "";
        
        if(adopted!=null){
            nxref=adopted.nXRefs();
            for(int i=0;i<nxref;i++){
                xref=adopted.xRefAt(i);
                if(xref.DSId0().equals(ens.DSId0()))
                    return xref.newTag();
            }
        }
            
        return "";    
    }
    
    public static String toStandardOP(String op) {
   
        int indexOP=relation_OPs.indexOf(op);
        if(indexOP<0){
            indexOP=ENSDF_relation_OPs_lower.indexOf(op);
            if(indexOP<0)
                indexOP=ENSDF_relation_OPs.indexOf(op);
   
            if(indexOP>=0)
            	op=relation_OPs.get(indexOP);
        }

        return op;
    }
    
    public static String toLowercaseEnsdfOP(String op) {
    	   
        int indexOP=ENSDF_relation_OPs_lower.indexOf(op);
        if(indexOP<0){
            indexOP=relation_OPs.indexOf(op);
            if(indexOP<0)
                indexOP=ENSDF_relation_OPs.indexOf(op);
   
            if(indexOP>=0)
            	op=ENSDF_relation_OPs_lower.get(indexOP);
        }

        return op;
    }
    
    public static String toUppercaseEnsdfOP(String op) {
 	   
        int indexOP=ENSDF_relation_OPs.indexOf(op);
        if(indexOP<0){
            indexOP=relation_OPs.indexOf(op);
            if(indexOP<0)
                indexOP=ENSDF_relation_OPs_lower.indexOf(op);

            if(indexOP>0)
            	op=ENSDF_relation_OPs.get(indexOP);
        }

        return op;
    }
    
    public static Record makeRecordWithNewEnergy(String oldRecordLine,double e,double de) {
        if(de<=0)
            return null;
        
        try {
            String s="",ds="",line="";
            XDX2SDS x2s=EnsdfUtil.x2s((float)e, (float)de);
            s=x2s.s();
            ds=x2s.ds();
            
            line=oldRecordLine.substring(0,9)+String.format("%-10s", s)+String.format("%-2s", ds);
            line=Str.fixLineLength(line,80);
            Vector<String> linesV=new Vector<String>();
            linesV.add(line);
            
            
            //System.out.println("  Average="+x+"  "+dx+" line="+line);
            
            char type=line.charAt(7);
            if(type!='G'){  
                Level lev=new Level();
                lev.setValues(linesV,0);
                return lev;
            }else{
                Gamma gam=new Gamma();
                gam.setValues(linesV);

                return gam;
            }           
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    
    }
    
    //make a link address used in the LaTeX hyperlink of a pdf file 
    public static String makePDFLink(String filename){
        String link="";
        String name=filename.trim();
        int n=name.toLowerCase().indexOf(".pdf");
        if(n>0 && n==name.length()-4)
            link=name;
        else
            link=name+".pdf";
        
        if(NDSConfig.linkDir!=null && NDSConfig.linkDir.length()>0)
            link=NDSConfig.linkDir+"/"+link;
        
        return link;
    }
    
    public static String getDSIDFromLine(String line){      
        if(line.length()>39)
            return line.substring(9,39).trim();
        else if(line.length()>9)
            return line.substring(9).trim();
        
        return "";
    }
    
    public static String getDataBlockDSID(Vector<String> block){    
        if(block==null || block.size()==0)
            return "";
        
        String dsid=getDSIDFromLine(block.get(0));
        
        //DSID in continuation line
        int i=1;
        String dsidLine=block.elementAt(i);
        while(dsidLine.length()>9 && dsidLine.substring(6,9).equals("   ")){
            dsid=dsid.trim()+dsidLine.substring(9).trim();
            i++;
            
            dsidLine="";
            if(i<block.size())
                dsidLine=block.elementAt(i);
            
        }
                
        return dsid;
        
    }
    
    /* find names of all continuation records in an ENSDF line**/
    public static Vector<String> findContRecordNames(String line){
        Vector<String> names=new Vector<String>();
        if(line.length()<10)
            return names;
        
        String[] entries=line.substring(9).split("\\$");
        for(int i=0;i<entries.length;i++){
            String entry=entries[i].trim();
            if(entry.length()==0)
                continue;
            
            String name=findContRecordName(entry);
            //if(name.length()>0)
                names.add(name);
        }
        
        return names;
    }

    /* find name of a continuation record entry**/
    public static String findContRecordName_old(String entry){
        String name="";
        String delims="=|>|<|\\|>|\\|<|~| AP | GT | GE | LT | LE |\\?|\\|";
        if(entry.length()<=0)
            return "";
        
        name=entry.split(delims, 2)[0];     
        return name;
    }
    
    
    /* find name of a continuation record entry or in a comment string in general*/
    public static String findContRecordName(String entry){
        String name="";
        ArrayList<String> ops=new ArrayList<String>(Arrays.asList(">=","<=","|<","|>","=",">","<","~","|?","?","|J","|v","|=","AP","GT","GE","LT","LE"));
        if(entry.length()<=0)
            return "";
        
        for(int i=0;i<ops.size();i++){
            String op=ops.get(i);
            if(Str.isLetter(op.charAt(0)))
            	op=" "+op+" ";
            
            int p=entry.indexOf(op);
            if(p>0){
                String s1=entry.substring(0,p).trim();
                String s2=entry.substring(p+op.length()).trim();
                if((op.contains(">")||op.contains("<")) && Str.isNumeric(s1) ){
                    String c1=s1.charAt(0)+"";
                    int p1=-1;
                  
                    //12<BM1W<24
                    if( (p1=s2.indexOf(op))>0 || (p1=s2.indexOf(c1))>0) {
                        s1=s2.substring(0,p1).trim();
                        s2=s2.substring(p1+1).trim();
                        if(s1.endsWith("|"))
                            s1=s1.substring(0,s1.length()-1);
                        if(Str.isDigit(s2.charAt(0)) )
                            s2=s2.substring(1);
                              
                        name=s1;
                    }else
                        name="";
                    
                }else if((op.startsWith(" L")||op.startsWith(" G")) && (s1+s2).contains(" ")) {
                    //BM1W LT 24 GT 12 
                    //   s1=BM1W, s2=24 GT 12
                    //or s1=BM1W LT 24, s2=12
                	//exception case:
                    //%B-N LT 8 (1995So03)
                	//   s1=%B-N, s2=8 (1995So03)
                	//WIDTH LT 14 KEV (2022AAAA)
                    String s0="LG";
                    s0=s0.replace(op.trim().charAt(0)+"","");
                    
                    if(s2.contains(" ")) {
                        //String[] temp=s2.split("[\\s]+");
                        //if(temp.length==3 && Str.isNumeric(temp[0]) && Str.isNumeric(temp[2]) && temp[1].startsWith(s0) && ops.contains(temp[1])) {
                        //    name=s1;
                        //}else 
                        //    name=s1;  
                        name=s1;
                    }else if(Str.isNumeric(s2)){//s1.contains(" ")
                        String[] temp=s1.split("[\\s]+");
                        if(temp.length==3 && Str.isNumeric(temp[2]) && temp[1].startsWith(s0) && ops.contains(temp[1])) {
                            name=temp[0];
                        }else
                            name=""; 
                    }else
                        name="";

                }else
                    name=s1;
                
                break;
            }
        }
        
        //for a comment line, it could be "from B(M1)=0.289 {I12}" or "1985Ab12 gives B(M1)=0.289 {I12}" for which cases name should be "B(M1)"
        //                    for line="B(M1) (from 2+)=....", name should be "B(M1) (from 2+)"
        
        if(name.contains(" ")) {
        	String[] temp=Str.specialSplitAllDelims(name,"[\\s]+");//not split at whitespace inside brackets
        	int n=temp.length;
        	
        	String lastPart=temp[n-1].trim();
        	if((lastPart.endsWith(")")&&lastPart.startsWith("(")) || (lastPart.endsWith("[")&&lastPart.startsWith("]"))
        			|| (lastPart.endsWith("}")&&lastPart.startsWith("{"))) {
        		if(n>1) {
        			n=name.lastIndexOf(temp[n-2], name.length()-lastPart.length());
        			
        			name=name.substring(n).trim();// "B(M1) (from 2+)" 
        		}
        		
        	}else {
        		name=temp[temp.length-1].trim();
        	}

        }
        
        //System.out.println(entry+"#####"+name);
        
        return name;
    }
    
    public static ArrayList<String> allOPs(){
        return new ArrayList<String>(Arrays.asList(">=","<=","|<","|>","=",">","<","~","|?","?","|J","|v","|=","AP","GT","GE","LT","LE"));
    }
    
    /* find entries of all continuation records in an ENSDF line**/
    public static Vector<String> findContRecordEntries(String line){
        Vector<String> entries=new Vector<String>();
        if(line.length()<10)
            return entries;
        
        String[] temp=line.substring(9).split("\\$");
        for(int i=0;i<temp.length;i++){
            String entry=temp[i].trim();
            
            if(entry.length()==0)
                continue;
            
            entries.add(entry);

        }
        
        return entries;
    }
    
    //make a ContRecord object from the string of a continuation record entry 
    public static ContRecord makeContRecord(String entry,String contType,boolean isSuppressed, boolean isNewLine){
        ContRecord cr=new ContRecord();
        if(entry.length()==0)
            return cr;
        
        String name=findContRecordName(entry);
        name=name.trim();
        if(name.length()<=0)
            return cr;
        
        FindValue fv=new FindValue();

        fv.findValueInEntry(entry,name); 
        //fv.findValueInEntry(entry);
        
        //System.out.println("@@@name="+name+"  "+fv.canGetV()+" entry="+entry);
        if(fv.canGetV()) {
        	EntryInfo ei=getFieldInfo(name);
        	boolean isExtension=false;
        	if(ei!=null)
        		isExtension=true;
        		
        	//System.out.println("EnsdfUtil 2413: fv name="+fv.name()+" txt="+fv.txt());

            cr.setValues(name,fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),fv.txt(),contType,isSuppressed,isNewLine,isExtension);
        }
        
        return cr;
    }
    
    
    /* find all continuation records in an ENSDF line**/
    public static Vector<ContRecord> findContRecords(String line){
        Vector<ContRecord> contRecords=new Vector<ContRecord>();
        if(line.length()<10)
            return contRecords;
        
        Vector<String> entries=findContRecordEntries(line);
        
        boolean isNewLine=true;
        boolean isSuppressed=false;
        
        String contType=line.substring(5,6);
        if(contType.contains("S"))
            isSuppressed=true;
        
        for(int i=0;i<entries.size();i++){
            isSuppressed=false;
            String entry=entries.get(i);
            if(entry.length()==0)
                continue;
            
            ContRecord cr=new ContRecord(); 
            cr=makeContRecord(entry,contType,isSuppressed,isNewLine);
            isNewLine=false;
            
            if(!cr.isEmpty())
                contRecords.add(cr);
        }
        
        return contRecords;
    }
    
    /*
     * type: any of "()",")","(" for round
     *       any of "[]","]","[" for round
     *       any of "{}","}","{" for round
     */
    public static int findNumberOfUnmatchBrackets(String str,String type) {
    	try {
   
    		
    		char[] lbA= {'(','[','{'};
    		char[] rbA= {')',']','}'};
    		char c=' ';
    		
    		int count=0;
    		
    		int[] indexA=new int[lbA.length];
    		Arrays.fill(indexA,-1);
    		
    		type=type.trim().toUpperCase();
    		if(type.isEmpty() ||type.equals("ALL")) {
    			count=lbA.length;
    			for(int i=0;i<lbA.length;i++)
    				indexA[i]=i;
    		}else {
    			for(int i=0;i<type.length();i++) {
    				c=type.charAt(i);
    				if(c=='(' || c==')') {
    					indexA[count]=0;
    					count++;
    					type=type.replace("(", " ").replace(")","");  					
    				}else if(c=='[' || c==']') {
    					indexA[count]=1;
    					count++;
    					type=type.replace("[", " ").replace("]","");  					
    				}else if(c=='{' || c=='}') {
    					indexA[count]=2;
    					count++;
    					type=type.replace("{", " ").replace("}","");  					
    				}
    				
    				if(count>=indexA.length)
    					break;
    			}
    		}
    		int nb=0,total=0;
    		for(int i=0;i<count;i++) {
    			int index=indexA[i];
    			if(index<0)
    				break;
    			
    			char lb=lbA[index];
    			char rb=rbA[index];
    			
    			//System.out.println(lb+" "+rb+" str="+str);
    			
        		int m=0;
        		nb=0;
        		while(m<str.length()) {
        			c=str.charAt(m);
        			if(c==lb)
        				nb++;
        			else if(c==rb)
        				nb--;	
        			m++;
        		}
        		
        		total+=Math.abs(nb);
    		}

    		if(count==1)
    		    return nb;
    		
    		return total;
    	}catch(Exception e) {
    		
    	}
    	return 0;
    }
    public static boolean containsBXLW(String line){
        String s=removeBXLW(line);
        if(s.trim().length()==line.trim().length())
            return false;
        
        return true;
    }
    

    /*
     * Table of critical reduced chi2 value of chi2 distribution
     */
    public static double criticalReducedChi2(int ndf) {
        
        if(ndf>200)
            return 1.0+2.33*Math.sqrt(2.0/ndf);
        if(ndf<1)
            return -1;
        
        double[] criticalChi2=new double[]{
        6.635, 4.605, 3.780, 3.319, 3.017, 2.802, 2.639, 2.511, 2.407,         
        2.321, 2.248, 2.185, 2.130, 2.082, 2.039, 2.000, 1.965, 1.934,         
        1.905, 1.872, 1.851, 1.831, 1.811, 1.793, 1.775, 1.758, 1.742,         
        1.727, 1.713, 1.699, 1.686, 1.673, 1.661, 1.649, 1.639, 1.628,         
        1.618, 1.609, 1.599, 1.591, 1.582, 1.574, 1.567, 1.560, 1.553,         
        1.546, 1.539, 1.533, 1.527, 1.522, 1.516, 1.511, 1.506, 1.501,         
        1.496, 1.491, 1.487, 1.482, 1.478, 1.474, 1.470, 1.466, 1.462,         
        1.458, 1.455, 1.451, 1.448, 1.444, 1.441, 1.437, 1.434, 1.431,         
        1.428, 1.425, 1.422, 1.419, 1.416, 1.413, 1.410, 1.407, 1.404,         
        1.401, 1.399, 1.396, 1.393, 1.391, 1.388, 1.385, 1.383, 1.380,         
        1.378, 1.375, 1.373, 1.370, 1.368, 1.366, 1.363, 1.361, 1.359,         
        1.357, 1.355, 1.353, 1.350, 1.348, 1.346, 1.344, 1.343, 1.341,         
        1.339, 1.337, 1.335, 1.334, 1.332, 1.330, 1.329, 1.327, 1.325,         
        1.324, 1.323, 1.321, 1.320, 1.318, 1.317, 1.316, 1.315, 1.313,         
        1.312, 1.311, 1.310, 1.309, 1.308, 1.307, 1.306, 1.305, 1.304,         
        1.303, 1.302, 1.301, 1.300, 1.299, 1.299, 1.298, 1.297, 1.296,         
        1.295, 1.294, 1.293, 1.293, 1.292, 1.291, 1.290, 1.289, 1.288,         
        1.287, 1.287, 1.286, 1.285, 1.284, 1.283, 1.282, 1.281, 1.280,         
        1.279, 1.278, 1.276, 1.275, 1.274, 1.273, 1.272, 1.271, 1.270,         
        1.268, 1.267, 1.266, 1.265, 1.263, 1.262, 1.261, 1.260, 1.258,         
        1.257, 1.256, 1.255, 1.254, 1.253, 1.251, 1.250, 1.250, 1.249,         
        1.248, 1.247, 1.247, 1.246, 1.246, 1.246, 1.246, 1.246, 1.246,         
        1.247, 1.248
        };
        
        return criticalChi2[ndf-1];
        
    }
    
	public static SDS2XDX reformatSDSForENSDFField(SDS2XDX sx) {
		try {
			String s=sx.s();
			String ds=sx.ds();
			
			if(ds.length()>2 && Str.isNumeric(ds)) {
				XDX2SDS temp=new XDX2SDS(sx.x(),sx.dx(),99);
				s=temp.s();
				ds=temp.ds();
				
				return new SDS2XDX(s,ds);
			}

		}catch(Exception e) {
			
		}
		
		return sx;
	}

	public static SDS2XDX reformatSDSForENSDFComment(SDS2XDX sx) {
		try {
			String s=sx.s();
			String ds=sx.ds();
			
			if(Str.isNumeric(ds) && ds.length()>2) {
				double dx=sx.dx();
				int limit=99;
				if(dx>99) {
					if(dx<999)
						limit=(int)(dx)+1;
					else
						limit=999;
					
				}

				XDX2SDS xs=new XDX2SDS(sx.x(),sx.dx(),limit);
				s=xs.s();
				ds=xs.ds();
				
				return new SDS2XDX(s,ds);
			}

		}catch(Exception e) {
			
		}
		
		return sx;
	}
    
    /*
     * remove BXLW values from a continuation line in ENSDF file
     * if BXLW values are the only values in the line, return an empty 
     * string
     */
    public static String removeBXLW(String line){
        String[] types={"E","M"};
        if(line.length()<=10 || !line.substring(6,8).equals(" G") || line.charAt(5)==' ' || line.substring(9).trim().isEmpty())
            return line;
        
        String s=line;
        String bs="";
        String s1="",s2="";
        int p1=-1,p2=-1;
        for(int i=0;i<types.length;i++){
            for(int j=1;j<=6;j++){
                bs="B"+types[i]+j+"W";
                p1=s.indexOf(bs);
                if(p1<0)
                    continue;
                
                s1="";s2="";
                
                s1=s.substring(0,p1);
                if(s1.length()<9)
                    return line;
                
                s1=s1.substring(0, 9);
                
                p2=s.indexOf("$",p1+bs.length());
                if(p2>0)
                    s2=s.substring(p2+1).trim();
                
                s=s1+s2;
                
                //debug
                //System.out.println("type="+bs+" s="+s+" len="+s.length()+" s1="+s1+" s2="+s2+" line="+line);
            }
        }
        
        if(s.length()<=10 || s.substring(9).trim().isEmpty())
            return "";
        
        return s;
        
    }
    
    public static String removeContinuedXREFLinks(String line) {
        String out="";
        String fontsize_str="";
        String linkType1="\\hyperlink";
        String linkType2="\\href";
        
        if(line.indexOf(linkType1)<0 && line.indexOf(linkType2)<0)
            return line;
        
        String s=line;
        while(s.length()>0){
            int nskip=0;
            if(s.indexOf(linkType1)!=0 && s.indexOf(linkType2)!=0){
                out+=s.charAt(0);
                s=s.substring(1);
                continue;
            }

            nskip=s.indexOf("\\texttt{")+8;
            s=s.substring(nskip).trim();
            int rb=s.indexOf("}");
            String tag=s.substring(0, rb).replaceFirst(fontsize_str, "").trim();
            out+=tag;
            
            if(s.length()>rb+2)
                s=s.substring(rb+2);
            else
                s="";
        }
        return out;
    }
    
    //insert a record to a vector of sorted records by energy
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static int insertRecordByEnergy(Record rec,Vector recordsV) {
    	int insertIndex=-1;
        try {
            if(recordsV!=null && recordsV.size()==0) {
                recordsV.add(rec);
                return 0;
            }
            
            Record closest=findClosestByEnergyValue(rec,recordsV);
      
            //if(closest==null)
            //System.out.println(rec.DSID()+" ### rec.ES="+rec.ES()+" "+recordsV.size()+" "+rec.getClass().getName());
            /*
            //debug
            if(closest!=null && rec.ES().contains("4115.3")) {
                System.out.println(" rec="+rec.ES()+" isTrueERPF="+rec.isTrueERPF()+" is level: "+rec.isLevel()+" closest="+closest.ES());
                //for(int i=0;i<recordsV.size();i++) {
                //  Record r=(Record)recordsV.get(i);
                //  System.out.println("    r="+r.ES()+" isTrueERPF="+r.isTrueERPF()+" is level: "+r.isLevel());
                //}
            }
            */
            
            int index=recordsV.indexOf(closest);
            Record r=closest;
            if(rec.ERPF()<closest.ERPF()) {
            	index--;
                while(index>=0) {
                	r=(Record) recordsV.get(index);
                    if(rec.ERPF()>=r.ERPF()) 
                    	break;
                    else
                        index--;
                }
                
                index=index+1;
                if(index<0)
                	index=0;
            }else {           	
            	index++;
                while(index<recordsV.size()) {
                	r=(Record) recordsV.get(index);
                    if(rec.ERPF()<=r.ERPF()) 
                    	break;
                    else
                        index++;
                }
                
            }
            recordsV.insertElementAt(rec, index);
            insertIndex=index;
        }catch(Exception e) {
            if(recordsV!=null && recordsV.size()==0) {
                recordsV.add(rec);
                insertIndex=0;
            }else {
                e.printStackTrace();
                return -1;
            }
        }
        
        return insertIndex;        
    }
    
    public static <T extends Record> boolean sortRecordsByEnergy(Vector<T> recordsV) {
        try {
            Vector<T> out=new Vector<T>();

            for(int i=0;i<recordsV.size();i++){
                insertRecordByEnergy((T)recordsV.get(i),out);

                /*
                //debug
                if(recordsV.get(i).ES().contains("4115.3")) {
                	System.out.println("$$$$$");
                	for(Record r:out)
                		System.out.println("  "+r.ES());
                }
                */
            }           

            recordsV.clear();
            recordsV.addAll(out);
        }catch(Exception e) {
            return false;
        }
        
        return true;        
    }
    
    public static <T extends Record> Vector<T> sortRecords(Vector<T> recordsV,String dataType, String orderOption) {
        ArrayList<T> list=new ArrayList<T>();
        list.addAll(recordsV);
        
        Collections.sort(list, new RecordComparator(dataType,orderOption));
        
        Vector<T> out=new Vector<T>();
        out.addAll(list);
        
        return out;
    } 
    
    //comment is a string of ENSDF comment lines separated by "\n"
    public static void insertComment(String comment,String ensfilePath) {
    	try {   		
    		File f=new File(ensfilePath);
    		Vector<String> linesV=Str.readFile(f);
    		
    		String[] comLines=comment.split("\n");
    		String firstComLine=comLines[0];
    		String comLineType=firstComLine.charAt(7)+"";
    		String comHeader="";
    		
    		int n=firstComLine.indexOf("$");
    		if(n>9 && firstComLine.length()>9)
    			comHeader=firstComLine.substring(9,n);
    		
    		int insertIndex=-1;
    		for(int i=0;i<linesV.size();i++) {
    			String line=linesV.get(i);
    			String lineType=line.substring(5,8).trim().toUpperCase();
    			if(lineType.equals("G")||lineType.equals("L"))
    				break;
    			
    			if(!lineType.startsWith("C"+comLineType)) {
    				if(insertIndex>0)
    					break;
    				
    				continue;
    			}
    			
    			insertIndex=i;
    			if(comHeader.isEmpty() || comHeader.equals("E")) {
    				break;
    			}else {
    				String lineHeader="";
    				n=line.indexOf("$");
    				if(n>9) {
    					lineHeader=line.substring(9,n);
    					if(lineHeader.contains(comHeader))
    						break;
    				}
    			}
    		}
    		
    		if(insertIndex>=0) {
    			Vector<String> comLinesV=new Vector<String>(Arrays.asList(comLines));
    			linesV.addAll(insertIndex, comLinesV);
    			
    			Str.write(ensfilePath, linesV);
    		}
    	}catch(Exception e) {
    		
    	}
    }
  
    //insert XREF links for XREF labels in additional XREFs in comments: "XREF: Others: AA, BB, CC,..."
    public static String insertContinuedXREFLinks(String line,HashMap<String,String> linkMap, ENSDF ens) {
        String out="";
        String linkID="";
        String fontsize_str="";
        
        String temp=line.substring(0);
        String prefix="";
        for(int i=0;i<temp.length();i++){
            char c=temp.charAt(i);
            if(Character.isLetter(c)){
                prefix=temp.substring(0, i);
                temp=temp.substring(i);
                break;
            }
        }
        
        temp=temp.replace("XREF","").replace("Others","").replaceAll(":", "").trim();
                
        String[] tags=temp.split(",");
        if(tags.length==0)
            return line;
        
        if(line.indexOf("XREF: Others:")>=0)
            out+="XREF: Others: ";
        
        for(int i=0;i<tags.length;i++){
            String tag=tags[i].trim();
            if(tag.length()==0)
                continue;
            
            XRef xR=findXRef(ens.XRefV(),tag);
            linkID=linkMap.get(tag);
            
            if(xR==null){
                out+=tag;
            }else if(linkID!=null && linkID.trim().length()>0){
                out+="\\hyperlink{"+linkID+"}{\\texttt{"+fontsize_str+tag+"}}";
            }
            else{//linked dataset is not present then link to the pdf file of that dataset in the same folder
                String href=EnsdfUtil.DSID2Filename(xR.DSId0())+".pdf";
                href=EnsdfUtil.makePDFLink(href);
                out+="\\href{"+href+"}{\\texttt{"+fontsize_str+xR.tag()+"}}";
            }
            
            if(i<tags.length-1)
                out+=", ";
        }
        
        if(line.trim().endsWith(",") && !out.trim().endsWith(","))
            out+=",";

        return prefix+out;
    }
    
    public static String opSymbol(String in){
        String symbol;
            //fix symbols
            if(in.equals("AP") || in.equals("?")) symbol="~";
            else if(in.equals("GT")) symbol=">";
            else if(in.equals("GE")) symbol=">=";
            else if(in.equals("LT")) symbol="<";
            else if(in.equals("LE")) symbol="<=";
            else symbol=in;
                
        return symbol;
        
    }
    
    /*
     * converting record like %B=1.2 LT to %B<1.2
     * no change for other cases
     */
    public static String printContRecord(String crS) {
        String s=crS;

        int p1=s.indexOf("=");

        if(p1>0) {
            String s1=s.substring(p1+1).trim();
            String[] temp=s1.split("[\\s]+");
            if(temp.length>=2 && Str.isNumeric(temp[0]) && !Str.isNumeric(temp[1])) {
           	 String s2=temp[1].trim();
           	 int p2=s.indexOf(s2,p1);
           	 int p3=s2.indexOf("(");
           	 if(p3>0)
           		 s2=s2.substring(0,p3);
           	 
           	 p2=p2+s2.length();
           	 String s3=s.substring(p2).trim();
           	 
           	 //System.out.println(" s="+s+" s1="+s1+" s2="+s2+" s3="+s3);
           	 
           	 if(!Str.isNumeric(s2) && s2.length()==2 && "LT,LE,GT,GE,AP,CA,SY".contains(s2)) {
           		 if(s2.equals("CA") || s2.equals("SY")) {
           			 s=s.substring(0,p1+1)+temp[0]+" "+s2;
           		 }else {

           			 s=s.substring(0,p1).trim()+" "+s2+" "+temp[0];
           		 }
           		 
           		 if(s3.length()>0)
           			 s=s+" "+s3;
           	 }
           	 
            }
        } 
        
        return s;
    }
    /*
     * nLimit: set maximum number of decay modes to be printed out
     */
    public static String printDecayMode(Level l,int nLimit) {
        try {
            int nModes=l.nDecayModes();
            
            if(nModes>nLimit) 
                nModes=nLimit;
            
            String name="",line="";
            DecayMode dm=null;
            boolean moreThanOneMode=false;
            
            for(int j=0;j<nModes;j++){
                dm=l.decayModeAt(j);
                name=dm.name();

                
                if(moreThanOneMode)
                    line+="; ";
                else 
                    moreThanOneMode=true;
                
                line+="%"+name;
                
                String tempsymbol=opSymbol(dm.symbol());

                if(tempsymbol.length()<1){
                    if(!opSymbol(dm.unc()).equals(dm.unc())) 
                        tempsymbol=opSymbol(dm.unc());
                    else 
                        tempsymbol="=";
                }
                   
                line+=tempsymbol;
                   
                //debug
                //System.out.println("in SkeletonChart:"+line+" dm.name="+dm.name()+" name="+name+" symbol="+dm.symbol()+"  tempSymbol="+tempsymbol);
                //System.out.println("          j="+j+" nModes="+modes);
                //System.out.println("    dm.unc()="+dm.unc()+" dm.txt="+dm.txt());
                //System.out.println("In SkeletonChart: %EC+%B+"+Translator.process("%EC+%B+",false)+"  "+Translator.process("%EC+%B+=100",false));
                
                if(dm.value().length()>0)
                    line+=dm.value();
                else
                    line+="?";
                
                if(dm.unc().trim().length()>0 && !tempsymbol.equals(opSymbol(dm.unc())))
                    line+=" "+dm.unc();
               
            }
            if(nModes<l.nDecayModes()) 
                line+="...";
            
            return line;
        }catch(Exception e) {
            
        }
        
        return "";
    }
    public static String printKeynumber(String keyNo){
        String s=keyNo.trim().toUpperCase();
        if(s.length()!=8)
            return keyNo;
        
        return s.substring(0, 5)+s.substring(5,6).toLowerCase()+s.substring(6);
    }
    
    public static String printValueAsComment(String name,String val,String unc) throws IOException{
        return printValueAsComment(name,val,unc,"",true);
    }
    
    public static String printValueAsComment(String name,String val,String unc,String unit) throws IOException{
        return printValueAsComment(name,val,unc,unit,true);
    }
    
    public static String printValueAsComment(String name,String val,String unc,String unit,boolean isLowerCase) throws IOException{

        
        String out="";
        
        name=name.trim();
        val=val.trim();
        unc=unc.toUpperCase().trim();
        unit=unit.trim();
        if((val+unc+unit).trim().isEmpty())
            return out;
        
        if(val.isEmpty())
            throw new IOException("value is empty but uncertainty or unit is not!");
        
        if(unc.equals("0"))
            unc="";
        
        String op="";
        int indexOP=ENSDF_relation_OPs.indexOf(unc);
        if(indexOP<0){
            indexOP=ENSDF_relation_OPs_lower.indexOf(unc);
            if(indexOP<0)
                indexOP=relation_OPs.indexOf(unc);
        }
        
        if(unit.length()>0)
            unit=" "+unit;
        
        int n=val.toUpperCase().indexOf("E");
        if(n>0 && n<val.length()-1 && isLowerCase) {
            String s1=val.substring(0,n).trim();
            String s2=val.substring(n+1).trim();
            char c=s2.charAt(0);
            int npow=0;
            try {
                if(c=='+'||c=='-') { 
                    npow++;
                    c=s2.charAt(npow);               
                }
                
                while(Character.isDigit(c)) {
                    npow++;
                    c=s2.charAt(npow);
                }
                
            }catch(Exception e) {
                
            }

            if(npow>0)
                val=s1+"|*10{+"+s2.substring(0,npow)+"}"+s2.substring(npow);
                
        }
            
        if(indexOP>=0){
            op=ENSDF_relation_OPs_lower.get(indexOP);
            if(op.equals("CA") || op.equals("SY")){
                if(name.length()>0)
                    name=name+"=";
                
                out=name+val+unit+" "+op;
            }else if(isLowerCase){
                out=name+op+val+unit;
            }else{
                op=ENSDF_relation_OPs.get(indexOP);
                out=name+" "+op+" "+val+unit;   
            }
        }else{
            if(name.length()>0)
                name=name+"=";
            
            if(unc.length()==0)
                out=name+val+unit;
            else if(isLowerCase)
                out=name+val+unit+" {I"+unc+"}";
            else
                out=name+val+unit+" "+unc;
        }

        return out.trim();
    }
    
    public static String printValueAsContinuation(String name,String val,String unc,String unit) throws IOException{
        return printValueAsComment(name,val,unc,unit,false);
    }
    
    public static String makeHyperLink(String s,String linkID){
        return "\\hyperlink{"+linkID+"}{"+s+"}";
    }
    
    public static String makeHyperTarget(String s,String linkID){
        return "\\hyperTarget{"+linkID+"}{"+s+"}";
    }
    
    public static String makeAdoptedLevelHyperLink(String s,Nucleus nucleus){
        String linkID=nucleus.nameENSDF().trim().toUpperCase()+"_LEVEL";
        return "\\hyperlink{"+linkID+"}{"+s+"}";
    }
    
    public static String makeAdoptedLevelHyperTarget(String s,Nucleus nucleus){
        String linkID=nucleus.nameENSDF().trim().toUpperCase()+"_LEVEL";
        return "\\hypertarget{"+linkID+"}{"+s+"}";
    }
    
    public static String makeAdoptedGammaHyperLink(String s,Nucleus nucleus){
        String linkID=nucleus.nameENSDF().trim().toUpperCase()+"_GAMMA";
        return "\\hyperlink{"+linkID+"}{"+s+"}";
    }
    
    public static String makeAdoptedGammaHyperTarget(String s,Nucleus nucleus){
        String linkID=nucleus.nameENSDF().trim().toUpperCase()+"_GAMMA";
        
        //String out="\\hypertarget{"+linkID+"}{"+s+"}";
        //if(!isPortrait)
        //  out="\\raisebox{\\ht\\strutbox}{"+out+"}"; //shift link target position to correct position for landscape mode, but it didn't work well 
        
        return "\\hypertarget{"+linkID+"}{"+s+"}";
    }
    
    public static String getCommentLineHead(String line){

        String body="",head="";
        
        if(line.length()<10)
            return head;
        
        body=line.substring(9);

        int id=body.indexOf("$");
        
        if( id < 0 ){

          int ih=body.indexOf("   ");
          if (ih>=10 || ih<0 || body.substring(ih).trim().length()<1) //ih=10 is ncol=19 (start from 0)
              ih=0;
             
          String phead=body.substring(0,ih).trim();
          String pbody=body.substring(ih).trim();


          if( phead.indexOf(" ") > 0 ){
             head="";
             body=body.trim();
          }
          
          if( pbody.length() == 0 ){
            head="";
            body=body.trim();
          }
          
          if( phead.indexOf(" ") < 0 ){
            String pphead=phead;//.toUpperCase();
            if( pphead.equals(phead) && phead.indexOf("{")<0){
              body=pbody;
              head=phead;
            }
            else{
              head="";
              body=body.trim();
            }
          }
        }
        else if( id == 0 ){
          head="";
          body=body.substring(1).trim();
        }
        else if( id > 0 ){
          head=body.substring(0,id).trim();
          body=body.substring(id+1).trim();
        }
        
        return head;
    }
    
    //public static Vector<Record> findMatchesByEnergyEntry(Record entry,Vector records,float delta,boolean forceDelta){
    //	return findMatchesByEnergyEntry(entry,records,delta,forceDelta,-1);
    //}
    
    /*
     * find all matches in records within the given range of energy value, 
     * considering uncertainties in energy entries, unless forceDetal=true
     * an energy entry is a pair of energy value and uncertainty
     * 
     * delta is the maximum error
     * if forceDelta=true and delta>0, force to used given delta,
     * otherwise,
     * if two entries in comparisons have smaller errors or delta<0, (sum of the two errors)*scale_factor will be used
     * as the actual error for comparisons.
     * See isComprableE()
     */
    @SuppressWarnings({ "rawtypes" })
    public static Vector<Record> findMatchesByEnergyEntry(Record entry,Vector records,float delta,boolean forceDelta,float nsigmaForMatchingEL){
        Vector<Record> out=new Vector<Record>();
        if(entry==null || records.size()==0)
        	return out;
        
        Record closest=null; 
        
        if(!Str.isNumeric(entry.ES())){
            if(entry instanceof Level) {
                Level lev=(Level)entry;
                
                if(lev.isTrueERPF()) {
                    //note that level that has non-numerical ES but has true ERPF (offset) will also be processed from here
                    closest=findClosestByEnergyValue(entry,records);
                    
                    if(!isComparableEnergyEntry(entry,closest,delta,forceDelta,nsigmaForMatchingEL))
                        closest=null;
                }
                
                //debug
                //if(lev.ES().equals("2191.2+x"))
                //    System.out.println("EnsdfUtil 2744: es="+lev.ES()+" closest==null:"+(closest==null)+" "+closest.ES());
                
                    
                if(!lev.isTrueERPF() || closest==null) {//if lev has true ERPF, process it as a regular level 
                    for(int i=0;i<records.size();i++){
                        try {                       
                            Level r=(Level)records.get(i);
                            //System.out.println("EnsdfUtil 2737: es1="+r.ES()+" e1="+r.ERF()+" nnes1="+r.NNES()+"  es2="+lev.ES()+" e2="+lev.ERF()+" nnes2="+lev.NNES());

                            if(!Str.isNumeric(r.ES()) ) {
                                if(r.NNES().equalsIgnoreCase(lev.NNES())) {                                   
                                    float e1=r.ERF();
                                    float e2=lev.ERF();
                                    
                                    //debug
                                    //if(r.ES().equals("2191.2+x"))
                                    //    System.out.println("EnsdfUtil 2744: es1="+r.ES()+" e1="+e1+" nnes1="+r.NNES()+" j1="+r.JPiS()+" erpf1="
                                    //+r.ERPF()+" es2="+lev.ES()+" e2="+e2+" nnes2="+lev.NNES()+" j2="+lev.JPiS()+" erpf2="+lev.ERPF());
                                    
                                    if(e1>0 && e2>0 && Math.abs(e1-e2)<2.0)
                                        out.add(r);
                                    else if(e1==e2)
                                        out.add(r);
                                    else if(lev.ES().equals(r.ES()) && Str.isNumeric(r.NES()) && Float.parseFloat(r.NES())>0)
                                        out.add(r);
                                    
                               
                                }else if(lev.NES().equals(r.NES()) && lev.DES().equals(r.DES()) && r.NNES().length()==1 && lev.NNES().length()==1){
                                    if(lev.nGammas()==r.nGammas() && isGammasConsistent(lev, r, 0.01f,true,nsigmaForMatchingEL)) {
                                        out.add(r);
                                    }
                                }
                                
                                //debug
                                //if(r.ES().equals("2191.2+x"))
                                //    System.out.println("In EnsdfUtil 2771: out size="+out.size());
                            }
                        }catch(Exception e) {
                        }

                    }
       
                    return out;
                }

            }else {
                for(int i=0;i<records.size();i++){
                    Record r=(Record)records.get(i);
                    if(r.ES().equals(entry.ES()))
                        out.add(r);
                }
                
                return out;
            }
        
            //System.out.println("   **** out.size="+out.size());                   
        }
        
        //note that level that has non-numerical ES but has ture ERPF (offset) will also be processed from here
        closest=findClosestByEnergyValue(entry,records);

        if(closest==null)
            return out;

        	
        int size=records.size();
        Record[] entries=new Record[size];
        for(int i=0;i<size;i++)
            entries[i]=(Record)records.get(i);
        
        int closestIndex=records.indexOf(closest);
        
        if(isComparableEnergyEntry(entry,closest,delta,forceDelta,nsigmaForMatchingEL))
            out.add(closest);


        //if(entry.ES().contains("3100"))
		//		System.out.println("EnsdfUtil: closest="+closest.ES()+"  size="+out.size());
        
        int i=closestIndex-1;
        int j=closestIndex+1;

        /*
        //debug
        if(Math.abs(entry.ERPF()-4060)<1){
            System.out.println("EnsdfUtil 2757 #1:  entry.ES="+entry.ES()+"  closest="+closest.ES()+" index="+closestIndex+" entry.isTrueERPF="+entry.isTrueERPF()+" closest.isTrueERPF="+closest.isTrueERPF());
            System.out.println(" out: "+out.size()+" isComparableEnergyEntry="+isComparableEnergyEntry(entry,closest,delta,forceDelta)+" delta="+delta+" forceD="+forceDelta);
            //for(Record r:out) System.out.println("     "+r.ES());
            
            for(int k=0;k<records.size();k++) {
              Record r=(Record)records.get(k);
              System.out.println("  k="+k+" E="+r.ES()+" isTrueERPF="+r.isTrueERPF()); 
            }
        }
        */
        
        boolean EComparable=false;
        int totalCount=0,nNoMatch=0;
        
        Vector<Record> tempV=new Vector<Record>();
        
        while(i>=0){

           if(entries[i].ERPF()<0){
               i=i-1;
               continue;
           }
           
           EComparable=isComparableEnergyEntry(entry,entries[i],delta,forceDelta,nsigmaForMatchingEL);
           
           /*
           //debug
           if(Math.abs(entry.ERPF()-4060)<1){
           //if(Math.abs(entry.ERPF()-8820)<10){
              System.out.println("EnsdfUtil 2629:  entry.ES="+entry.ES()+"  closest="+closest.ES()+" index="+closestIndex+" i="+i+" ES="+entries[i].ES()+" comparable="+EComparable+" forceDelta="+forceDelta);
           }
           */
           
           tempV.insertElementAt(entries[i],0);
           
           if(!EComparable) {       	   
        	   nNoMatch++;
        	   
        	   if(nNoMatch>2 || totalCount>5 )
        	   //if(nNoMatch>2 || !Str.isNumeric(entries[i].DES()) || totalCount>5 )
        		   break;
           }else
        	   out.insertElementAt(entries[i],0);
           
           i=i-1;         
           totalCount++;
        }

        totalCount=0;
        nNoMatch=0;
        while(j<entries.length){     
            if(entries[j].ERPF()<0){
                j=j+1;
               continue;
            }
            
           EComparable=isComparableEnergyEntry(entry,entries[j],delta,forceDelta,nsigmaForMatchingEL);     
           
           /*
           //debug
           if(Math.abs(entry.ERPF()-8820)<10){
                System.out.println("EnsdfUtil 2649:  entry.ES="+entry.ES()+"  closest="+closest.ES()+" index="+closestIndex+" j="+j+" ES="+entries[j].ES()+" comparable="+EComparable+" forceDelta="+forceDelta);
                System.out.println(" out.size="+out.size());
           }
           */
           
           tempV.add(entries[j]);
           
           if(!EComparable) {       	   
        	   nNoMatch++;
        	   
        	   if(nNoMatch>2 || totalCount>5 )
        	   //if(nNoMatch>2 || !Str.isNumeric(entries[j].DES()) || totalCount>5 )
        		   break;
           }else       
        	   out.add(entries[j]);
           
           j=j+1;
           
           totalCount++;
        }
        
        /*
        //debug
        if(entry.ES().contains("24E3")) {
        //if(Math.abs(entry.ERPF()-8820)<10){
            System.out.println("EnsdfUtil 2689 #1:  entry.ES="+entry.ES()+"  closest="+closest.ES()+" index="+closestIndex+" entry.isTrueERPF="+entry.isTrueERPF()+" closest.isTrueERPF="+closest.isTrueERPF());
            System.out.println(" out: "+out.size());
            for(Record r:out) System.out.println("     "+r.ES());
            
            for(int k=0;k<records.size();k++) {
              Record r=(Record)records.get(k);
              System.out.println("  k="+k+" E="+r.ES()+" isTrueERPF="+r.isTrueERPF()); 
            }
        }
        */
        
        
        if(out.size()>0) {
        	int closestIndexUp=closestIndex;
        	int closestIndexDown=closestIndex;
        	if(out.contains(closest)) {
        		closestIndexUp+=1;
        		if(closestIndexUp>records.size()-1)
        			closestIndexUp=records.size()-1;
        		
        		closestIndexDown-=1;
        		if(closestIndexDown<0)
        			closestIndexDown=0;
        	}
        	
        	boolean toSort=false;
        	for(int m=0;m<out.size();m++) {
        		Record r=out.get(m);
        		int nr=records.indexOf(r);
        		if(nr>closestIndexUp) {
        			for(int k=closestIndexUp;k<nr;k++) {
        				if(!out.contains(records.get(k))) {
        					out.add((Record) records.get(k));
        					toSort=true;
        				}
        				
        			}
        				
        		}else if(nr<closestIndexDown) {
        			for(int k=nr+1;k<=closestIndexUp;k++) {
        				if(!out.contains(records.get(k))) {
        					out.add((Record) records.get(k));
        					toSort=true;
        				}
        			}
        		}   		
        	}
        	
        	if(toSort) 
        		sortRecordsByEnergy(out);     
        }
        
        /*
        //debug
        if(entry.ES().contains("24E3")) {
        //if(Math.abs(entry.ERPF()-8820)<10){
            System.out.println("EnsdfUtil 2738 #2:  entry.ES="+entry.ES()+"  closest="+closest.ES()+" index="+closestIndex+" entry.isTrueERPF="+entry.isTrueERPF()+" closest.isTrueERPF="+closest.isTrueERPF());
            System.out.println(" out: "+out.size());
            for(Record r:out) System.out.println("     "+r.ES());
            
            for(int k=0;k<records.size();k++) {
              Record r=(Record)records.get(k);
              System.out.println("  k="+k+" E="+r.ES()+" isTrueERPF="+r.isTrueERPF()); 
            }
        }
        */
        
        return out;
    }
    
    @SuppressWarnings({ "rawtypes" })
    public static Vector<Record> findMatchesByEnergyEntry(Record entry,Vector records,float delta,float nsigmaForMatchingEL){
        return findMatchesByEnergyEntry(entry,records,delta,false,nsigmaForMatchingEL);
    }
    
    @SuppressWarnings({ "rawtypes" })
    public static Vector<Record> findMatchesByEnergyEntry(Record entry,Vector records,int nsigma){
        return findMatchesByEnergyEntry(entry,records,-1,nsigma);
    }
    

    /*
     * find all matches in records within the given range of energy value, without
     * considering uncertainties
     * 
     * delta is the actual error used for comparisons
     * Any two entries having ES (string of energy value) consistent within in this error are
     * a pair of match
     * See isComprableES()
     */
    @SuppressWarnings({ "rawtypes" })
    public static Vector<Record> findMatchesByEnergyValue(float en,Vector records,float delta){
        Vector<Record> out=new Vector<Record>();
        if(en<0 || records.size()==0)
        	return out;
        
        Record closestMatch=findClosestByEnergyValue(en,records);
        
        if(closestMatch==null)
            return out;
        
        int size=records.size();
        Record[] entries=new Record[size];
        for(int i=0;i<size;i++)
            entries[i]=(Record)records.get(i);
        
        int closestIndex=records.indexOf(closestMatch);
        
        //there could be cases that energy is close but not comparable, eg, non-numerical uncertainty
        //the search around to find other matching possibilities
        if(isComparableEnergyValue(en,closestMatch.ERPF(),delta))
            out.add(closestMatch);

        int i=closestIndex-1;
        int j=closestIndex+1;

      
        boolean ESComparable=false;

        while(i>=0){
           if(entries[i].ERPF()<0){
               i=i-1;
               continue;
           }
           
           ESComparable=isComparableEnergyValue(en,entries[i].ERPF(),delta);
           if(ESComparable)
               out.insertElementAt(entries[i],0);
           else
              break;
 
           i=i-1;
        }

        while(j<entries.length){      
            if(entries[j].ERPF()<0){
               j=j+1;
               continue;
            }
            
           ESComparable=isComparableEnergyValue(en,entries[j].ERPF(),delta);    
           if(ESComparable)
               out.add(entries[j]);
           else
              break;
           
           j=j+1;
        }

      

        return out;
    }
    @SuppressWarnings({ "rawtypes"})
    public static Record findClosestByEnergyValue(Record entry,Vector records){
        Record out=null;
        if(entry==null)
            return null;
        
        out=findClosestByEnergyValue(entry.ERPF(),records);       
        
        if(!Str.isNumeric(entry.ES()) && entry instanceof Level && (out==null||Str.isNumeric(out.ES())) ) {
            Level lev=(Level)entry;           
            try {
                Record foundRecord=null;
                float minDiff=100000,diff=-1;      
                //Vector<Record> tempV=new Vector<Record>();
                
                int start=0,end=records.size()-1;
                if(out!=null) {
                    int index=records.indexOf(out);
                    start=Math.max(start, index-5);
                    end=Math.min(end, index+5);
                }
                
                for(int i=start;i<=end;i++) {
                    Level l=(Level)records.get(i);

                    /*
                    //debug
                    if(entry.ES().equals("0+x")) {
                        System.out.println("EnsdfUtil 3167: E="+entry.ES()+" i="+i+"  l.es="+l.ES()+" out="+out.ES()+" lev.NNES())="+lev.NNES()+" l.NNES()="+l.NNES());
                        System.out.println("     lev.ERPF="+lev.ERPF()+"  l.ERPF="+l.ERPF());
                    }
                    */
                    
                    if(Str.isNumeric(l.ES()) || !l.NNES().equals(lev.NNES()))
                        continue;
                
                    
                    float e1=EnsdfUtil.s2f(lev.NES());
                    float e2=EnsdfUtil.s2f(l.NES());
                    
                    //if(lev.ERPF()==l.ERPF())//the offset might be different. So DO NOT use this condition
                    if(e1==e2 && e1>=0)
                        return l;
                    
                    diff=Math.abs(lev.ERPF()-l.ERPF());
                    
                    //System.out.println("EnsdfUtil 3176: E="+entry.ES()+" i="+i+"  l.es="+l.ES()+" diff="+diff+" minDiff="+minDiff+" size="+records.size());
                    
                    
                    if(diff<=minDiff) {
                        minDiff=diff;
                        foundRecord=l;
                    }else {
                        break;
                    }
                    
                    
                    //tempV.add(l);
                }
                
                //foundRecord=findClosestByEnergyValue(entry.ERPF(),records)
                
                if(foundRecord!=null) {
                    if(out==null)
                        out=foundRecord;
                    else {//Str.isNumeric(out.ES())=true
                        
                        if(EnsdfUtil.isComparableEnergyEntry(lev, foundRecord,-1) && EnsdfUtil.isOverlapJPI(lev.JPiS(),((Level)foundRecord).JPiS()) ){
                            out=foundRecord;
                        }
                                
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }

        }       
        
        return out;
    }
    
    //wrong
    @SuppressWarnings({ "rawtypes", "unchecked"})
    public static Record findClosestByEnergyValue_old(Record entry,Vector records){   
        
        
        Vector temp=new Vector();   
        temp.addAll(records);
        try {           
            //note isTrueERPF=true only happens in Level class
            //isTrueERPF=false for level means level=1235.5+X or X, X (offset) is unknown
            Record r=(Record) records.get(0);
            if(!Str.isNumeric(entry.ES()) && entry.isLevel() && r.isLevel()) {
                temp.clear();
                
                Level lev=(Level)entry;
                for(int i=0;i<records.size();i++) {
                    Level l=(Level)records.get(i);
                    
                    //if(lev.NNES().equals(l.NNES()))
                    //  System.out.println(" lev="+lev.ES()+" isTrueERPF="+lev.isTrueERPF()+" lev.NNES="+lev.NNES()+" l.ES="+l.ES()+" isTrueERPF="+l.isTrueERPF()+" l.NNES="+l.NNES());
                    
                    if(lev.NNES().equals(l.NNES()) || Str.isNumeric(l.ES()))
                        temp.add(l);
                }
                
                if(temp.isEmpty())
                    temp.addAll(records);   
            }
            
        }catch(Exception e) {}
        
        
        //debug
        //temp.addAll(records); 
        //Record r=findClosestByEnergyValue(entry.ERPF(),temp);
        //System.out.println(" lev="+entry.ES()+" isTrueERPF="+entry.isTrueERPF()+" r.ES="+r.ES()+" isTrueERPF="+r.isTrueERPF());
        
        return findClosestByEnergyValue(entry.ERPF(),temp);
    }  
    
    //by default, filter out nothing
    @SuppressWarnings("rawtypes")
    public static Record findClosestByEnergyValue(float e,Vector records){
        return findClosestByEnergyValue(e,records,false);
    }
    
    /*
     * find closest match by energy value only, without considering energy uncertainty
     * forceTrueERPF=true is to filter out **LEVEL** records with isTrueERPF=false
     *              =false to filter out nothing
     *     it doesn't affect other types of record other than LEVEL
     */
    @SuppressWarnings("rawtypes")
    public static Record findClosestByEnergyValue(float e,Vector records,boolean forceTrueERPFLevel){
        
        int size=records.size();
        if(size==0)
           return null;
        
        Record[] entries=new Record[size];
        int count=0;
        
        Record r=(Record)records.get(0);
        if(r.isLevel()) {
            for(int i=0;i<size;i++){
                r=(Record)records.get(i);
                
                //debug
               //if(e<9448 && e>9447)
                //if(Math.abs(e-9447)<1 && r.ES().contains("+x"))
                //	System.out.println("EnsdfUtil 3115: rec ES="+r.ES()+" isTrueERPF="+r.isTrueERPF()+" ERPF="+r.ERPF()+" forceTrueERPFLevel="+forceTrueERPFLevel);               	
                
                
                if(!r.isTrueERPF() && forceTrueERPFLevel)
                    continue;
                
                entries[count]=r;
                count++;
            }
        }else {
            for(int i=0;i<size;i++){
                r=(Record)records.get(i);
                if(r.ERPF()<0)
                    continue;
                
                entries[count]=r;
                count++;
            }
        }

        
        size=count;
        
        if(size==0)
            return null;
        
        int start=0;
        int end=size-1;
        int middle=-1;

        while(true){
           boolean to_break=false;
           
           middle=(start+end)/2;
          

           float em=entries[middle].ERPF();
           float ei=entries[start].ERPF();
           float ef=entries[end].ERPF();

           
           /*
           if(Math.abs(e-9447)<0.2) {
                 System.out.println("In ENsdfUtil line 3104: e="+e+" middle record="+entries[middle].ES()+" len="+entries.length+" middle="+middle+" start="+start+" "+end+" size="+size);
                 System.out.println("               em="+em+" s="+entries[middle].ES()+"  ei="+ei+" s="+entries[start].ES()+" ef="+ef+" s="+entries[end].ES());
           }
           */
           
           if(middle==start)//in this case, start+1=end; eg, start=1,end=2, then middle=
              to_break=true;

           if(e>=ef){
              start=end;
              to_break=true;
           }
           else if(e<ei){
              end=start;
              to_break=true;
           }
           else if(e>em)
              start=middle;
           else if(e<em)
              end=middle;
           else{
              start=middle;
              end=middle;
              to_break=true;
           }
  
           //if(Math.abs(e-4060.8)<0.1)
           //      System.out.println("  e="+e+" em="+em+" ei="+ei+" ef="+ef+" start="+start+" end="+end+" middle="+middle+"  record(m)="+entries[middle].ES());
           
           if(to_break)
              break;
           else
              continue;
        }

        float diff1=Math.abs(entries[start].ERPF()-e);
        float diff2=Math.abs(entries[end].ERPF()-e);

        int min_index=start;
   
        if(diff2<diff1){
           min_index=end;
        }
  
        //if(Math.abs(e-21192.2)<0.1)
        //	 System.out.println("       e="+e+" closest="+entries[min_index].ES());
        
        return entries[min_index];     

    }
    
    //example offsetLabel: 1234.5+X, offsetLabel=X
    public static Level findClosestLevelByEnergyValue(float e,Vector<Level> records,String offsetLabel){
        Vector<Level> entries=new Vector<Level>();
        if(offsetLabel.trim().length()>0) {
            for(Level lev:records){
                if(lev.NNES().equals(offsetLabel))
                    entries.add(lev);
            }
        }else {
            entries.addAll(records);
        }
        
        return (Level) findClosestByEnergyValue(e,entries);
        
    }
    
    //two records have to match within in given error range
    @SuppressWarnings("rawtypes")
    public static Record findClosestByEnergyValue (float en,Vector records,float delta){  
        
        Record closestMatch=null;
        if(en<0 || records.size()==0)
            return null;
        
        Vector<Record> matches=findMatchesByEnergyValue(en,records,delta);
        
        if(matches==null || matches.size()==0)
            return null;
        
        float min_diff=1000000;
        float diff=0;
        
        try{           
            //results in matches already match in energy by isComparableE()
            //isComparableES(ES1,ES2,error_range): compare if the difference of given ES is within in given error range
            //isComparableE(entry1,entry2,error_range) : compare if the energies of two records match within in given error range 
            //                                           or the sum of their uncertainties, whichever is smaller 
            for(int i=0;i<matches.size();i++){
                Record rec=matches.get(i);
                
                diff=Math.abs(en-rec.ERPF());

                if(diff<min_diff){
                   min_diff=diff;
                   closestMatch=rec;
                }
            }   
        }catch(Exception e){
            
        }

        return closestMatch;
    }
    
    //two records have to match within in given error range
    //also considering energy uncertainties in energy entries
    //an energy entry is a pair of energy value and uncertainty
    @SuppressWarnings("rawtypes")
    public static Record findClosestByEnergyEntry(Record entry,Vector records){
        return findClosestByEnergyEntry(entry,records,-1);
    }
    
    @SuppressWarnings("rawtypes")
    public static Record findClosestByEnergyEntry(Record entry,Vector records,float delta){
        return findClosestByEnergyEntry(entry,records,delta,false,-1);
    }
    
    //two records have to match within in given error range
    @SuppressWarnings({ "rawtypes", "unused" })
    public static Record findClosestByEnergyEntry(Record entry,Vector records,float delta,boolean forceDelta,float nsigmaForMatchingEG){      
        
        Record closestMatch=null;
        
        Vector<Record> matches=findMatchesByEnergyEntry(entry,records,delta,forceDelta,nsigmaForMatchingEG);

        /*
        //debug
        if(entry.ES().contains("4829.") && entry instanceof Level && matches.size()>0){ 
        //if(entry.ES().equals("761.12")) {
        	System.out.println("EnsdfUtil 3278: "+entry.ES()+" "+entry.DES()+" matches.size="+matches.size()+" delta="+delta+" forceDelta="+forceDelta);
        	System.out.println("    erpf="+entry.ERPF());
        	//for(int i=0;i<records.size();i++) {
        	//	Record r=(Record) records.get(i);
        	//	System.out.println("   r.ES="+r.ES()+"  "+r.DES());
        	//}
        	for(int i=0;i<matches.size();i++) {
        		Record r=(Record) matches.get(i);
        		System.out.println("   r.ES="+r.ES()+"  "+r.DES()+" erpf="+r.ERPF());
        	}
        }
        */
        
        if(matches==null || matches.size()==0)
            return null;
        
        float min_diff=1000000;
        float diff=0;
        
        float e=entry.ERPF();
        float erf=entry.ERF();
        
        //results in matches already match in energy by isComparableE()
        //isComparableES(ES1,ES2,error_range): compare if the difference of given ES is within in given error range
        //isComparableE(entry1,entry2,error_range) : compare if the energies of two records match within in given error range 
        //                                           or the sum of their uncertainties, whichever is smaller 
        for(int i=0;i<matches.size();i++){
            Record rec=matches.get(i);
            
            diff=Math.abs(e-rec.ERPF());

            if(diff<min_diff){
               min_diff=diff;
               closestMatch=rec;
            }
        }
        /*
        if(entry.ES().contains("4829.") && entry instanceof Level && matches.size()>0){ 
        //if(entry.ES().equals("761.12")) {
        	System.out.println("EnsdfUtil 3852: "+entry.ES()+" "+entry.DES()+" matches.size="+matches.size()+" delta="+delta+" forceDelta="+forceDelta);
        	System.out.println("    erpf="+entry.ERPF()+" isTrueERPF="+entry.isTrueERPF());
        	System.out.println("  closestMatch erpf="+closestMatch.ERPF()+" erf="+closestMatch.ERF()+" ES="+closestMatch.ES()+" isTrueERPF="+closestMatch.isTrueERPF());        	
        }
        */
        
        return closestMatch;
    }

    

    //The following code is for Level entry only
    //find a closest match in energy, then 
    //search around to find the closest match with overlaping JPI
    @SuppressWarnings("rawtypes")
    public static Level findClosestMatchLevel(Record entry,Vector records,float delta,boolean forceDelta){
        
        Level closestMatch=null;
        Level JPIMatch=null;
        
        Vector<Record> matches=findMatchesByEnergyEntry(entry,records,delta,forceDelta,-1);
        
        /*
        if(entry.ES().equals("0+x")) {
            System.out.println(" matched="+matches.size());
            for(int i=0;i<matches.size();i++) {
                Record rec=(Record)matches.get(i);
                System.out.println("      "+rec.ES());
            }
        }
        */
        
        if(matches==null || matches.size()==0)
            return null;
        
        float min_diff=1000000;
        boolean JPIoverlap=false;
        float diff=0;
        
        float e=entry.ERPF();
 
        String es=entry.ES();
        
        Level lev=(Level)entry;
        
        //results in matches already match in energy by isComparableE()
        //isComparableES(ES1,ES2,error_range): compare if the difference of given ES is within in given error range
        //isComparableE(entry1,entry2,error_range) : compare if the energies of two records match within in given error range 
        //                                           or the sum of their uncertainties, whichever is smaller 
        for(int i=0;i<matches.size();i++){
            Level li=(Level)matches.get(i);
            
            if(!Str.isNumeric(es) && !Str.isNumeric(li.ES()) && lev.NNES().equals(li.NNES())) {
                float e1=s2f(lev.NES());
                float e2=s2f(li.NES());
                diff=Math.abs(e1-e2);
            }else {
                diff=Math.abs(e-li.ERPF());
            }
            
            JPIoverlap=isOverlapJPI(lev.JPiS(),li.JPiS());

            //if(entry.ES().contains("734")) System.out.println("  li.ES"+li.ES()+"  JPOverlap="+JPIoverlap);
            
            
            if(diff>delta || !JPIoverlap )
               continue;

            if(!lev.NNES().isEmpty() && !lev.NNES().equals(li.NNES()))
                continue;
            
            if(!lev.JPiS().isEmpty() && lev.JPiS().equals(li.JPiS())) {
                if(JPIMatch==null || diff<Math.abs(e-JPIMatch.ERPF()))
                    JPIMatch=li;
            }
            
            if(diff<min_diff){
               min_diff=diff;
               closestMatch=li;
            }
        }


        if(JPIMatch!=null && closestMatch!=null && closestMatch.JPiS().isEmpty()) {
            return JPIMatch;
        }
        
        return closestMatch;
        
    }
           
    @SuppressWarnings("rawtypes")
    public static Record findClosestMatchLevel(Record entry,Vector records,float delta){
        return findClosestMatchLevel(entry,records,delta,false);
    }
    
    //check if two entries has comparable ESs
    //if one is number and one is not, return false
    //otherwise, 
    //if both es have decimal digits, use smaller delta rather than the input delta
    //and if diff<delta, return true and return false otherwise
    public static boolean isComparableEnergyValue_old(String es1,String es2,float deltaE){
        
        if(!Str.isNumeric(es1) || !Str.isNumeric(es2))
           return false;

        float e1=Float.parseFloat(es1);
        float e2=Float.parseFloat(es2);

        float diff=Math.abs(e1-e2);
        if(diff>deltaE)
           return false;

        //if both es have decimal digits, use smaller delta rather than the input delta

        if(es1.indexOf(".")>=0 && es2.indexOf(".")>=0){
           if(es1.toUpperCase().indexOf("E")>=0 || es2.toUpperCase().indexOf("E")>=0)
              return true;

           float small_delta=2.0f;

           if(diff>small_delta)
              return false;
           else
              return true;
        }     

        return true;
    }
    
    // =====================================================================
    // Generic close-energy clustering for any Record subclass
    // (Level, Gamma, Beta, Alpha, Parent, DecayMode, ...)
    //
    // Sorting precondition: the input Vector must already be sorted by
    // ERPF() ascending (equals EF() for non-Level records).
    // =====================================================================

    /**
     * Find all clusters of records whose energies agree within the tolerance
     * defined by (deltaE, forceDelta, nSigma) with EVERY other member of the
     * cluster.
     *
     * Tolerance rule (from EnsdfUtil.isComparableEnergyEntry):
     *   forceDelta = true  -> tolerance is always deltaE
     *   forceDelta = false -> tolerance is nSigma * sqrt(sigma_i^2 + sigma_j^2),
     *                         falling back to deltaE when uncertainties are
     *                         missing or non-numerical
     *
     * PRECONDITION: recordsV is sorted by ERPF() ascending.
     *
     * Records with ERPF() < 0 are skipped.
     * Only clusters of size >= 2 are returned; indexes within each cluster
     * are in ascending order (same as recordsV).
     */
    public static Vector<Vector<Integer>> findCloseEnergyClusters(
            Vector<? extends Record> recordsV,
            float deltaE, boolean forceDelta, float nSigma) {

        Vector<Vector<Integer>> out = new Vector<Vector<Integer>>();
        if (recordsV == null || recordsV.size() < 2) return out;

        final int n = recordsV.size();
        Vector<Integer> group = new Vector<Integer>();

        for (int i = 0; i < n; i++) {
            Record ri = recordsV.get(i);
            float ei = ri.ERPF();

            // Unknown energy: close current group, skip
            if (ei < 0) {
                if (group.size() >= 2) out.add(group);
                group = new Vector<Integer>();
                continue;
            }

            if (group.isEmpty()) {
                group.add(i);
                continue;
            }

            // Must agree with EVERY member already in the group
            boolean agreesWithAll = true;
            for (Integer gi : group) {
                Record rj = recordsV.get(gi);
                if (!EnsdfUtil.isComparableEnergyEntry(rj, ri, deltaE, forceDelta, nSigma)) {
                    agreesWithAll = false;
                    break;
                }
            }

            if (agreesWithAll) {
                group.add(i);
            } else {
                if (group.size() >= 2) out.add(group);
                group = new Vector<Integer>();
                group.add(i);
            }
        }
        if (group.size() >= 2) out.add(group);

        return out;
    }

    /**
     * Flat list of indexes into recordsV that belong to at least one cluster
     * returned by findCloseEnergyClusters(...).
     *
     * Each index appears exactly once, in ascending order.
     */
    public static Vector<Integer> findCloseEnergyIndexes(
            Vector<? extends Record> recordsV,
            float deltaE, boolean forceDelta, float nSigma) {

        Vector<Integer> out = new Vector<Integer>();
        if (recordsV == null) return out;

        Vector<Vector<Integer>> clusters =
                EnsdfUtil.findCloseEnergyClusters(recordsV, deltaE, forceDelta, nSigma);

        boolean[] seen = new boolean[recordsV.size()];
        for (Vector<Integer> g : clusters) {
            for (Integer idx : g) {
                if (idx >= 0 && idx < seen.length && !seen[idx]) {
                    seen[idx] = true;
                    out.add(idx);
                }
            }
        }
        Collections.sort(out);
        return out;
    }


    // ---------------------------------------------------------------------
    // Convenience overloads - cluster finder
    // ---------------------------------------------------------------------

    /** Default: 3-sigma, no fallback tolerance. */
    public static Vector<Vector<Integer>> findCloseEnergyClusters(
            Vector<? extends Record> recordsV) {
        return EnsdfUtil.findCloseEnergyClusters(recordsV, 0f, false, 3.0f);
    }

    /** 3-sigma with a fallback tolerance for missing uncertainties. */
    public static Vector<Vector<Integer>> findCloseEnergyClusters(
            Vector<? extends Record> recordsV, float deltaE) {
        return EnsdfUtil.findCloseEnergyClusters(recordsV, deltaE, false, 3.0f);
    }

    /** Given sigma, no fallback tolerance. */
    public static Vector<Vector<Integer>> findCloseEnergyClusters(
            Vector<? extends Record> recordsV, float deltaE, float nSigma) {
        return EnsdfUtil.findCloseEnergyClusters(recordsV, deltaE, false, nSigma);
    }

    /** Pure absolute window: ignore uncertainties entirely. */
    public static Vector<Vector<Integer>> findCloseEnergyClustersByDelta(
            Vector<? extends Record> recordsV, float deltaE) {
        return EnsdfUtil.findCloseEnergyClusters(recordsV, deltaE, true, -1f);
    }

    // ---------------------------------------------------------------------
    // Convenience overloads - flat index list
    // ---------------------------------------------------------------------

    /** Default: 3-sigma, no fallback tolerance. */
    public static Vector<Integer> findCloseEnergyIndexes(
            Vector<? extends Record> recordsV) {
        return EnsdfUtil.findCloseEnergyIndexes(recordsV, 0f, false, 3.0f);
    }

    /** 3-sigma with a fallback tolerance for missing uncertainties. */
    public static Vector<Integer> findCloseEnergyIndexes(
            Vector<? extends Record> recordsV, float deltaE) {
        return EnsdfUtil.findCloseEnergyIndexes(recordsV, deltaE, false, 3.0f);
    }

    /** Given sigma, no fallback tolerance. */
    public static Vector<Integer> findCloseEnergyIndexes(
            Vector<? extends Record> recordsV, float deltaE, float nSigma) {
        return EnsdfUtil.findCloseEnergyIndexes(recordsV, deltaE, false, nSigma);
    }

    /** Pure absolute window: ignore uncertainties entirely. */
    public static Vector<Integer> findCloseEnergyIndexesByDeltaE(
            Vector<? extends Record> recordsV, float deltaE) {
        return EnsdfUtil.findCloseEnergyIndexes(recordsV, deltaE, true, -1f);
    }
    
    //similar to isComparableES(ES1,ES2,deltaE), but error range is always the given
    //deltaE even if two ES have decimal digits
    public static boolean isComparableEnergyValue(float e1,float e2,float deltaE){

        if(e1<0 || e2<0)
            return false;
        
        float diff=Math.abs(e1-e2);
        
        if(diff>deltaE*1.1)
           return false;    

        return true;
    }
    
    
    //check if two entries have comparable energies within given uncertainty delta,
    //also considering uncertainties in energy entries, unless forceDelta=true
    //an energy entry is a pair of energy value and uncertainty
    //
    //if forceDelta=true and delta>0, force to use the given delta
    //otherwise,
    //if 5*(sigma1+sigma2)<delta (>0) (sigma=energy uncertainty)
    //or given delta<0, use 5*(sigma1+sigma2) for delta instead.
    public static boolean isComparableEnergyEntry(Record r1,Record r2,float delta,boolean forceDelta,float nsigmaForMatchingEG){
    
        //if(Math.abs(r1.ERPF()-750)<33 && r1 instanceof Level){
        //    System.out.println("EnsdfUtil line 2824:   es1="+r1.ES()+" es2="+r2.ES()+" r1.isTrueERPF()="+r2.isTrueERPF()+" r2.isTrueERPF()="+r2.isTrueERPF());
        //}
        
        if(r1==null || r2==null)
            return false;
        
        String es1=r1.ES();
        String es2=r2.ES();
        if(es1.equals(es2))
            return true;

        
        /*
        if(Math.abs(r1.ERPF()-16600)<200 && Math.abs(r2.ERPF()-17400)<200 && (r1 instanceof Level)){
          System.out.println("EnsdfUtil line 3186 #1:   es1="+es1+" es2="+es2+" r1.isTrueERPF()="+r2.isTrueERPF()+" r2.isTrueERPF()="+r2.isTrueERPF());
        }
        */
        
        /*
        if(r1.ES().startsWith("411")&&r2.ES().startsWith("411")) {
            System.out.println("EnsdfUtil line 5025 #2:   es1="+es1+" des1="+r1.DES()+" es2="+es2+" des2="+r2.DES()+"  "+r1.isTrueERPF() +"  "+r1.isTrueERPF() +"  r1.ERPF="+r1.ERPF()+
                    " r2.ERPF="+r2.ERPF());
          }
        */
        
        
        //new condition
        if(!r1.isTrueERPF() || !r2.isTrueERPF()) {//true in Level class only; always false in other daughter-classes of Record class
            //old condition
            boolean isNum1=Str.isNumeric(es1);
            boolean isNum2=Str.isNumeric(es2);
            
            if(isNum1!=isNum2)
                return false;
            if((!isNum1&&r1.ERF()<0) || (!isNum2&&r2.ERF()<0))
                return false;
        }
     
        float de1=findDefaultEnergyUncertainty(r1,nsigmaForMatchingEG);
        float de2=findDefaultEnergyUncertainty(r2,nsigmaForMatchingEG);
        
        /*
        //debug
        //if(!forceDelta)
        //if((Math.abs(r1.ERPF()-3119)<0.0001 || Math.abs(r1.ERPF()-3500)<0.0001) && r1.DES().isEmpty()) {
        //if(Math.abs(r1.ERPF()-3119)<1 && (r1 instanceof Level)){
        //if(Math.abs(r1.ERPF()-16200)<10 && Math.abs(r2.ERPF()-17400)<200 && (r1 instanceof Level)){
        if(r1.ES().startsWith("411")&&r2.ES().startsWith("411")) {
          System.out.println("EnsdfUtil line 5047 #2:   es1="+es1+" des1="+r1.DES()+" es2="+es2+" des2="+r2.DES()+"  de1="+de1+"   de2="+de2+" forceDelta="+forceDelta+" r1.ERPF="+r1.ERPF()+
                  " r2.ERPF="+r2.ERPF());
          System.out.println("   EF1="+r1.EF()+" EF2="+r2.EF()+" ERPF1="+r1.ERPF()+" ERPF2="+r2.ERPF()+"  "+r1.isTrueERPF() +"  "+r1.isTrueERPF()+" delta="+delta);
        }
        */
        
        
        if(de1<0 || de2<0) {
        	if(r1 instanceof Level){
        		Level lev1=(Level)r1;
        		Level lev2=(Level)r2;
        		if(lev1.NNES().length()>0 && lev1.NNES().equals(lev2.NNES())) {
                	float de=0.5f;
                	float tempDE1=lev1.DERF();
                	float tempDE2=lev2.DERF();
                	
                	if(tempDE1<0) tempDE1=0;
                	if(tempDE2<0) tempDE2=0;
                	
                	de=Math.max(de, (float)Math.sqrt(tempDE1*tempDE1+tempDE2*tempDE2));
                	de=Math.min(de,5.0f);
                	
                	if(r1.isTrueERPF()==r2.isTrueERPF() && Math.abs(r1.ERF()-r2.ERF())<de && Math.abs(r1.ERPF()-r2.ERPF())<de)
                		return true;
        		}

        	}

        	
            return false;
        }
        
        //float e1=Math.abs(Float.parseFloat(es1));
        //float e2=Math.abs(Float.parseFloat(es2));
        float e1=r1.ERPF();
        float e2=r2.ERPF();
        float deltaE=delta;
        
        if(!forceDelta || delta<0){
            if(de1<delta)
                de1=findUncertaintyForComparison(e1,de1,nsigmaForMatchingEG);
            if(de2<delta)
                de2=findUncertaintyForComparison(e2,de2,nsigmaForMatchingEG);
            
           
            //deltaE=(de1+de2); //range too large  
            deltaE=(float) Math.sqrt(de1*de1+de2*de2);
            
            if(deltaE>delta && delta>0) {
                if(r1.ERPF()<0 || r2.ERPF()<0)
                    deltaE=delta;
            }
        }

        float diff=Math.abs(e1-e2);
        
        /*
        //debug
        //if(!forceDelta)
        //if(Math.abs(r1.ERPF()-16200)<10 && (r1 instanceof Level)){	
        //if(Math.abs(r1.ERPF()-3119)<1 && Math.abs(r2.ERPF()-3086)<1 && (r1 instanceof Level)){
        //if((Math.abs(r1.ERPF()-3119)<0.0001 || Math.abs(r1.ERPF()-3500)<0.0001) && r1.DES().isEmpty()) {
        //if(r1.ES().equals("3119")&& Math.abs(r2.ERPF()-3086)<1 && (r1 instanceof Level)){
        if(r1.ES().startsWith("411")&&r2.ES().startsWith("411")) {
          System.out.println("##EnsdfUtil line 5115:   es1="+es1+" des1="+r1.DES()+" es2="+es2+" des2="+r2.DES()+"  forceDelta="+forceDelta);
          System.out.println("                                   de1="+de1+"           de2="+de2+" diff="+diff+" deltaE="+deltaE+" delta="+delta);
        }
        */
        
        float factor=1.1f;
        if(deltaE>delta)
            factor=1.0f;
        
        if(diff>deltaE*factor)
           return false;    

        
        return true;
    }
    
    /*
     * correct format: 1111AaAA or 1111Aa11 in "c" type
     *                 1111AAAA or 1111AAAA in "C" type
     */
    public static boolean isKeynoCorrectCase(String keyno,char commentType){
    	try{
        	char c1=keyno.charAt(4);
        	char c2=keyno.charAt(5);
        	char c3=keyno.charAt(6);
        	char c4=keyno.charAt(7);


           	
        	//the 1st, 3rd, and 4th characters must be in uppercase no matter commentType="C" or "c"
        	//e.g., 2012Wa38, 2014StZZ
        	String s=""+c1+c3+c4;

           	//System.out.println(" key="+keyno+" s="+s+"  s.toUpperCase()="+s.toUpperCase()+" type="+commentType+" "+Character.isLowerCase(commentType)+" "+Character.isLowerCase(c2));

           	
         	if(!s.toUpperCase().equals(s))
        		return false;
 
           	//System.out.println(" key="+keyno+" s="+s+"  s.toUpperCase()="+s.toUpperCase()+" type="+commentType+" "+Character.isLowerCase(commentType)+" "+Character.isLowerCase(c2));

        	if(Character.isLowerCase(commentType) && !Character.isLowerCase(c2))
        		return false;
 
        	
    	}catch(Exception e){
    		return false;
    	}
 		  	
    	return true;
    }
    
    public static boolean isKeynoValidFormat(String keyno){
    	if(keyno.length()!=8)
    		return false;
    	
    	String s1=keyno.substring(0,4);
    	String s2=keyno.substring(4,6);
    	String s3=keyno.substring(6);
    	
    	for(int i=0;i<s1.length();i++)
    		if(!Character.isDigit(s1.charAt(i)))
    			return false;
    	
    	for(int i=0;i<s2.length();i++)
    		if(!Character.isLetter(s2.charAt(i)))
    			return false;
    	
    	char c1=s3.charAt(0);
    	char c2=s3.charAt(1);
    	if(Character.isDigit(c1)&&!Character.isDigit(c2))
    		return false;
    	
    	if(Character.isLetter(c1)&&!Character.isLetter(c2))
    		return false;
    	
    	return true;
    }
    
    private static float stepDown(float x,float stepX){
        if(x<=stepX)
            return 1;
        
        return 0;
    }

    public static float findALargeEnergyUncertainty(float ef) {
    	ef=Math.abs(ef);
    	int n=0;
    	try {
			if(ef>1)
				n=(int)Math.log10(ef);
    	}catch(Exception e) {
    		
    	}
    	
    	return (float) (ef/Math.pow(2,n));
    }
    
    public static float findDefaultEnergyUncertainty(Record r,float nsigmaForMatchingEG){
    	try {
    		return findDefaultEnergyUncertainty(r.ES(),r.DES(),nsigmaForMatchingEG);
    	}catch(Exception e) {
    		
    	}  	
    	return -1;
    }
    //public static float findDefaultEnergyUncertainty(Record r){
    //	return findDefaultEnergyUncertainty(r,-1);
    //}
    
    //public static float findDefaultEnergyUncertainty(String es,String des){
    //	return findDefaultEnergyUncertainty(es,des,-1);
    //}
    public static float findDefaultEnergyUncertainty(String es,String des, float nsigmaForMatchingEG){
    	try {
            des=des.toUpperCase().trim();
            es=es.trim();
            
            SDS2XDX s2x=new SDS2XDX(es,des);
            
            /*
            String nns=s2x.nns();
            String ns=s2x.ns();
            if(nns.length()>0 && Str.isNumeric(ns))
            	es=ns;
            */
            
            if(nsigmaForMatchingEG<=0) {
                if(!Str.isNumeric(des) && Str.isNumeric(es)){
                    float e=Math.abs(Float.parseFloat(es));
                    
                    //float c1=5.0f;
                    float df2=25,df1=25;       
                    int npower=0;
                    String baseS=es,powerS="";
                    
                    boolean hasExpo=false;
                    if(es.toUpperCase().contains("E")) {
                    	String tempS=es.toUpperCase().trim();
                    	
                    	es=Str.deformatExpo(es);//convert to a regular number 
                        
                    	int n=tempS.indexOf("E");
                    	
                    	baseS=tempS.substring(0,n).trim();
                    	powerS=tempS.substring(n+1).trim();
                    	
                    	if(powerS.length()>0 && Str.isInteger(powerS)) 
                    		npower=Integer.parseInt(powerS);
                    	
                    	hasExpo=true;
                    }
                    
                    int p=baseS.indexOf(".");
                    if(p>=0) {
                        String s=baseS.substring(p+1).trim();
                        int n=s.length();
                        npower=npower-n;                
                    }
                    
                    if(npower<-1) {
                    	df1=(float) (50.0*Math.pow(10,npower));
                    }else if(npower<=0) {
                    	df1=2.0f;
                    }else {
                    	df1=(float)(5*Math.pow(10, npower));
                    }
                        
                    //if(Math.abs(e-17400)<100)
                    //	System.out.println(" es="+es+"  df1="+df1+" npower="+npower);
                    
                    df1=(float) Math.min(df1,0.1*e);
                    
                    
                    if(p>0 || hasExpo)
                    	return df1;
                    
                    /*
                    if(r instanceof Gamma) {
                        c1=2.5f;
                        c2=25.0f;
                    }
                    */
                    

                    if(es.endsWith("000"))
                        df2=100;
                    else if(es.endsWith("00"))
                        df2=50;
                    else if(es.endsWith("0"))
                    	df2=20;
                    else 
                    	df2=10;
                   
                    float ef=(float) s2x.x();
                    //if(des.length()==0 && es.indexOf(".")>0 && es.indexOf("E")<0)
                    //    return Math.min(c1,ef*0.01f);
                    
                    if(des.length()==0)
                        return Math.min(df2,ef*0.05f);
                    
                    if(des.equals("AP") || des.equals("CA"))
                        return Math.min(100,ef*0.2f);
                    
                    if(des.charAt(0)=='L')
                        return Math.min(200,ef/2.0f);

                }
                
                return (float) s2x.dx();    
            }else {
            	if(s2x.dx()>0)
            		return (float) (nsigmaForMatchingEG*s2x.dx());
            	else if(s2x.dxu()>0 && s2x.dxl()>=0) 
            		return (float) Math.max(s2x.dxu(), s2x.dxl());
            }

		
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
     
    } 
    
    /*
     * used for comparing energies of different records
     */
    public static float findUncertaintyForComparison(float e,float de){
        if(de<=0)
            return Math.abs(e*0.01f);
        if(e==0)
            return 1.0f;
        
        e=Math.abs(e);
        if(e<=de)
            return e;
        
        float r=de/e;
        //int p=(int)Math.log10(e/de);
        float p=(float) Math.log10(e/de); 
        if(p==0)
            return de;
            
        //if(p>1)
            p=p-1;
        
        //Below is the best result after lots of trials of different combinations
        float stepR=stepDown(r,0.1f);
        float stepDE=stepDown(de,0.1f);
        float de1=(float) (Math.pow(10,-p)*stepR/9.0f)*e+(1-stepR)*e/90f;//the latter is the value for r>0.1, equal to value of former at r=0.1 (p=1);
        float de2=(float) (stepR*(stepDE+1)*p)*de+(1-stepR)*(stepDE+1)*de;//the latter is the value for r>0.1, equal to value of former at r=0.1 (p=1);
        
        //construct an uncertainty function of e and de varying smoothly with e and de 
        //float delta=de1+de2;//de1+de2 too large
        float delta=(float) Math.sqrt(de1*de1+de2*de2);
       
        /*
        //debug
        if(Math.abs(e-3119)<0.0001 || Math.abs(e-3500)<0.0001)
        System.out.println("EnsdfUtil line 4793:  e="+String.format("%10f",e)
                +"  de="+String.format("%10f",de)+" de1="+String.format("%10f",de1)
                +" de2="+String.format("%10f",de2)+" delta="+String.format("%10f",delta)+"  r="+(delta/de)+" p="+p+" stepR="+stepR+" stepDE="+stepDE);
        */
        
        return delta;
    }
    
    public static float findUncertaintyForComparison(float e,float de,float nsigmaForMatchingEG){
    	if(nsigmaForMatchingEG<=0)
    		return findUncertaintyForComparison(e,de);
    	
    	return de*nsigmaForMatchingEG;
    }
    
    public static FindValue findValueInCommentByName(Record r, String name) {
        FindValue fv=new FindValue();
        for(Comment c:r.commentV()) {
            fv=findValueInCommentByName(c,name);
            if(fv.canGetX())
                return fv;
        }
        
        return fv;
    }
    
    public static FindValue findValueInCommentByName(Comment com, String name) {
        FindValue fv=new FindValue();
        fv.findValueInLine(com.rawBody(), name);
        
        return fv;
    }
    
    public static boolean isComparableEnergyEntry(Record r1,Record r2,float delta,float nsigmaForMatchingEL){
        return isComparableEnergyEntry(r1,r2,delta,false,nsigmaForMatchingEL);
    }
    
    public static boolean isComparableEnergyEntry(Record r1,Record r2,float nsigmaForMatchingEL){ 
        return isComparableEnergyEntry(r1,r2,-1,nsigmaForMatchingEL);
    }
    
    public static boolean isGammasConsistent(Level l1,Level l2,float deltaEG,boolean forceDelta, float nsigmaForMatchingEG){
        
        int numOfMatch=findNumberOfConsistentGammas(l1,l2,deltaEG,forceDelta,nsigmaForMatchingEG);//number of gamma that has no match in the level which has less gammas
        int size=Math.min(l1.nGammas(), l2.nGammas());
        
        /*
        //debug     
        if(Math.abs(l1.ERPF()-2068.7)<0.1){
            System.out.println("In EnsdfUtil line 1824: lev.es="+l1.ES()+"  e="+l1.ERPF()+" jpi="+l1.JPiS()+" deltaEG="+deltaEG+" forceDelta="+forceDelta);
            System.out.println("   numOfMath="+numOfMatch+"   size="+size);
        }
        */
        
        if(numOfMatch>=(size+1)/2)
            return true;
        
        return false;
    }
    
    public static boolean isGoodValueStr(String valStr) {
        ArrayList<String> goodList=new ArrayList<String>(Arrays.asList("SP","SN","SA"));
        valStr=valStr.toUpperCase().trim();
        if(Str.isNumeric(valStr) || (valStr.length()==1&&Str.isLetters(valStr)) || goodList.contains(valStr))
            return true;
        
  
        String[] temp;
        String s1="",s2="";
        if(valStr.contains("+")) { 
            temp=valStr.split("[+]");
            if(temp.length==2) {
                s1=temp[0].trim();
                s2=temp[1].trim();
                //1234.5+X, or X+1234.5
                
                boolean isNum1=Str.isNumeric(s1);
                boolean isNum2=Str.isNumeric(s2);
                
                String tempS="";               
                if(!isNum1&&isNum2) {
                    //convert X+1234.5 to 1234.5+X
                    tempS=s1;
                    s1=s2;
                    s2=tempS;
                    
                    boolean tempB=isNum1;
                    isNum1=isNum2;
                    isNum2=tempB;
                }
                
                boolean isLett1=Str.isLetters(s1);
                boolean isLett2=Str.isLetters(s2);
                
                if(isNum1 && isNum2)
                    return false;
                
                if(isLett1 && isLett2) {
                    //X+SN
                    if(s1.length()<s2.length()) {
                        tempS=s1;
                        s1=s2;
                        s2=tempS;
                    }
                                    
                    if(s1.length()==s2.length() && s1.length()==1 && !s1.equals(s2))//X+Y
                        return true;
                    else if(goodList.contains(s1) && s2.length()==1)//SN+X
                        return true;
                    
                }else if(isNum1 && isLett2) {
                    if(s2.length()==1 || goodList.contains(s2))
                        return true;
                }
            }
        }else if(valStr.contains("-") && valStr.startsWith("S")) {//SN-0.123
            temp=valStr.split("[-]");
            if(temp.length==2) {
                s1=temp[0].trim();
                s2=temp[1].trim();
                
                if(goodList.contains(s1)&&Str.isNumeric(s2))
                    return true;              
            }
        }
        
        return false;
    }
    public static boolean isGoodUncertaintyStr(String uncStr) {
        ArrayList<String> goodList=new ArrayList<String>(Arrays.asList("LT","LE","GT","GE","AP","SY","CA",">","|>",">=","<","|<","<=","|?","~"));
        uncStr=uncStr.toUpperCase().trim();
        if(Str.isInteger(uncStr) || goodList.contains(uncStr))
            return true;
        
        String[] temp;
        String s1="",s2="";
        if(uncStr.contains("+") && uncStr.contains("-")) {//+16-12
            if(uncStr.startsWith("+"))
                temp=uncStr.substring(1).split("[-]");
            else if(uncStr.startsWith("-"))
                temp=uncStr.substring(1).split("[+]");
            else
                return false;
            
            
            //System.out.println(uncStr+"  "+temp.length);
            //for(int i=0;i<temp.length;i++)
            //    System.out.println(temp[i]);
            
            if(temp.length==2) {
                s1=temp[0].trim();
                s2=temp[1].trim();
                
                boolean isInt1=Str.isInteger(s1);
                boolean isInt2=Str.isInteger(s2);
                
                if(isInt1&&isInt2)
                    return true;
                if((isInt1&&s2.equals("|@")) || (isInt2&&s1.equals("|@")) )//|@ infinity
                    return true;
            }
                
        }
        
        return false;
    }
    
    //find all comments of the same record after lineIndex=afterIndex and before next record
    public static Vector<Comment> findRecordComments(Vector<String> linesV,int afterIndex){
    	Vector<Comment> out=new Vector<Comment>();
    	try {
   		    if(afterIndex<0)
   		    	afterIndex=0;
   		    
   		    char recType=linesV.get(afterIndex+1).charAt(7);

   		    Vector<String> tempV=new Vector<String>();
    		for(int i=afterIndex+1;i<linesV.size();i++) {
				String line=linesV.get(i);
				if(line.trim().isEmpty())
					continue;
				
				line=Str.fixLineLength(line, 80);
				String lineType=line.substring(5,8);
				char c1=lineType.charAt(0);
				char c2=lineType.charAt(1);
				char c3=lineType.charAt(2);
				
				if(c3!=recType || lineType.substring(0,2).equals("  ")) 
					break;
				if(c2!='c' && c2!='C')
					continue;
				
				if(c1==' ') {
					if(tempV.size()>0) {
						Comment c=new Comment();
						c.setValues(tempV);
						out.add(c);
						
						tempV.clear();
					}				
				}
				
				tempV.add(line);
    		}
    		
        	
    		if(tempV.size()>0) {
    			Comment c=new Comment();
    			c.setValues(tempV);
    			out.add(c);
    		}
    	}catch(Exception e) {
    		
    	}
	
    	return out;
    }
    
    //Note: here no ENSDF object is used, since it consumes memory when processling many ENSDF blocks of lines 
    public static Vector<Comment> findTopComments(Vector<String> linesV){
    	Vector<Comment> out=new Vector<Comment>();
    	try {
    		Vector<String> tempV=new Vector<String>();
			for(int i=1;i<linesV.size();i++) {
				String line=linesV.get(i);
				if(line.trim().isEmpty())
					continue;
				
				line=Str.fixLineLength(line, 80);
				String lineType=line.substring(5,8);
				char c1=lineType.charAt(0);
				char c2=lineType.charAt(1);
				char c3=lineType.charAt(2);
				
				if(lineType.substring(0,2).equals("  ") && c3!=' ' && "LGBEA".contains(c3+"")) 
					break;
				if(c2!='c' && c2!='C')
					continue;
		
				if(c1==' ') {
					if(tempV.size()>0) {
						Comment c=new Comment();
						c.setValues(tempV);
						out.add(c);
						
						tempV.clear();
					}				
				}
				
				tempV.add(line);
			}
			
			if(tempV.size()>0) {
				Comment c=new Comment();
				c.setValues(tempV);
				out.add(c);
			}	
    	}catch(Exception e) {
    		
    	}
    	return out;
    }
    
    //find top comment by keynumber: description of a measurement
    public static Comment findTopCommentByKeynumber(Vector<String> linesV,String keyno){

    	try {
    		
    		keyno=keyno.toUpperCase().trim();
    		if(keyno.isEmpty())
    			return null;
    		
    		Vector<String> tempV=new Vector<String>();
    		boolean found=false,done=false,moveToFront=true;
    		int nMoveToFront=0;
    		
    		Vector<Comment> comsV=new Vector<Comment>();
			for(int i=1;i<linesV.size();i++) {
				String line=linesV.get(i);
				if(line.trim().isEmpty())
					continue;
				
				line=Str.fixLineLength(line, 80);
				String lineType=line.substring(5,8);
				char c1=lineType.charAt(0);
				char c2=lineType.charAt(1);
				char c3=lineType.charAt(2);
				
				if(lineType.substring(0,2).equals("  ") && c3!=' ' && "LGBEA".contains(c3+"")) 
					break;
				
				if((c2!='c' && c2!='C') || c3!=' ' )
					continue;
		
				
				if(c1==' ') {
					String s=line.substring(9).toUpperCase().trim();

					if(tempV.size()>0 && found) {
						Comment c=new Comment();
						c.setValues(tempV);
						if(done)
							return c;
		
						if(moveToFront)
							comsV.add(comsV.size()-nMoveToFront,c);
						else
							comsV.add(c);
						
						moveToFront=true;
						found=false;
					}
					tempV.clear();

					
					int n=s.indexOf(":");
					String s1="";
					if(n>0) {
						s1=s.substring(0,n).trim();
						if(s1.length()<8 || EnsdfUtil.isKeynumber(s1.substring(0,8)))
							s1="";
					}
					
					if(s1.length()>0) {
						s=s1;
	
						if(s.startsWith(keyno)) {
							found=true;
							done=true;
						}else if(s.contains(keyno)) {
							found=true;
							//not done yet, will continue to search
							//because the exact match could be found further down
						}else if(s.startsWith(keyno.substring(0,3))) {

							//String[] maxMatch=Str.findMaxPartialMatchPair(keyno, s.substring(0,8));
							n=Str.countMatchingCharsInMaxPartialMatch(keyno, s.substring(0,8));
							if(n==7) {
								found=true;
							}							
						}
					}else { 
					    n=s.indexOf(keyno);
						if(n>=0 && comsV.size()==0) {
							//some old evaluations have:
							// 49V  c  1972Ki06 measured |g's and |g(|q). DSAM    
							//or
							// 46TI c  E=7 MeV (1972As01)
							
							found=true;
							if(n>10) {
								moveToFront=false;
								nMoveToFront++;
							}

						}

					}
				}
				
				tempV.add(line);
			}
			
			//System.out.println("EnsdfUtil 5159: keyno="+keyno+" #"+done+" "+found+"  "+comsV.size());
			
			if(tempV.size()>0 && found) {
				Comment c=new Comment();
				c.setValues(tempV);
				if(done || comsV.size()==0)
					return c;
			}else if(comsV.size()>0) {
				return comsV.get(0);
			}
			
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
	/**
	 * Return comments in comV whose head matches headerName.
	 * Pure: reads comV, returns a new Vector. No JSON involved.
	 */
	public static Vector<Comment> findCommentsByHead(Vector<Comment> comV, String headerName) {
	    Vector<Comment> out=new Vector<Comment>();
	    if(comV == null || comV.isEmpty() || headerName == null)
	        return out;

	    for(Comment c : comV) {
	        if(c == null) continue;
	        int n=c.nHeads();
	        for(int i=0;i<n;i++) {
	            String h=c.headAt(i);
	            if(h == null) continue;
	            if(h.trim().equalsIgnoreCase(headerName)) {
	                out.add(c);
	                break;
	            }
	        }
	    }
	    return out;
	}
	
    public static Gamma[][] findConsistentGammas(Level l1,Level l2,float deltaEG,boolean forceDelta,float nsigmaForMatchingEG){
    	Gamma[][] out=new Gamma[2][0];
    	
        if(l1==null || l2==null || l1.nGammas()==0 || l2.nGammas()==0)
            return out;

        Level lev1=l1;
        Level lev2=l2;
        if(l1.nGammas()>l2.nGammas()){
            lev1=l2;
            lev2=l1;
        }
        
        Vector<Gamma> gV1=new Vector<Gamma>();
        Vector<Gamma> gV2=new Vector<Gamma>();
        
        int start=0;
        for(int i=0;i<lev1.nGammas();i++){
            Gamma g1=lev1.gammaAt(i);
            
            for(int j=start;j<lev2.nGammas();j++){
                Gamma g2=lev2.gammaAt(j);
                
                /*
                //debug     
                if(Math.abs(l1.ERPF()-4430)<1 && Math.abs(l2.ERPF()-4430)<1){
                    System.out.println("In EnsdfUtil line 3014: lev.es="+lev1.ES()+"  e="+lev1.ERPF()+" jpi="+lev1.JPiS()+" g1="+g1.ES()+" deltaEG="+deltaEG+" forceDelta="+forceDelta);
                    System.out.println("    l2="+lev2.ERPF()+" g2="+g2.ES()+" isEnergyMatched="+isComparableEnergyEntry(g1, g2,deltaEG,forceDelta));
                }
                */
                
                if(isComparableEnergyEntry(g1, g2,deltaEG,forceDelta,nsigmaForMatchingEG)){
                    start=j+1;
                    gV1.add(g1);
                    gV2.add(g2);
                    break;
                }               
            }

        }
        
        if(gV1.size()>0) {
        	out=new Gamma[2][gV1.size()];
        	if(l1.nGammas()>l2.nGammas()){
            	for(int i=0;i<gV1.size();i++) {
            		out[0][i]=gV2.get(i);
            		out[1][i]=gV1.get(i);
            	}
        	}else {
            	for(int i=0;i<gV1.size();i++) {
            		out[0][i]=gV1.get(i);
            		out[1][i]=gV2.get(i);
            	}
        	}

        }
        return out;
    }
    
    public static int findNumberOfConsistentGammas(Level l1,Level l2,float deltaEG,boolean forceDelta,float nsigmaForMatchingEG){
    	Gamma[][] out=findConsistentGammas(l1,l2,deltaEG,forceDelta,nsigmaForMatchingEG);
    	
    	return out[0].length;
    }
    
    /*
     * make a nucleus map for beam particles, ejectiles, and other particles in DSID
     * key=particle string in DSID
     * value=Z+":"+A  ----Z and A could be arbitrary for particles like MU,PI,...
     */
    public static void initNucMap(){
        if(beams==null) {
            others=new HashMap<String,String>();
            beams=new HashMap<String,String>();
            ejectiles=new HashMap<String,String>();
        }else if(!beams.isEmpty())
            return;
        
        others.put("G",   "0:0");
        others.put("G'",  "0:0.01");
        others.put("NU",  "0:0.05");
        others.put("E",   "-1:0.1");
        others.put("E'",  "-1:0.11");
        others.put("E+",  "1:0.12");
        others.put("E-",  "-1:0.13");
        others.put("MU0", "0:0.2");
        others.put("MU",  "-1:0.2");
        others.put("MU-", "-1:0.2");
        others.put("MU-'","-1:0.21");
        others.put("MU+", "1:0.3");
        others.put("MU+'","1:0.31");
        others.put("PI0", "0:0.4");
        others.put("PI",  "1:0.4");
        others.put("PI-", "-1:0.4");
        others.put("PI-'","-1:0.41");
        others.put("PI+", "1:0.5");
        others.put("PI+'","1:0.51");
        others.put("K0", "0:0.6");
        others.put("K-", "-1:0.6");
        others.put("K-'","-1:0.61");
        others.put("K+", "1:0.7");
        others.put("K+'","1:0.71");
           

        beams.put("G", "0:0");
        beams.put("E", "1:0.1");
        beams.put("P", "1:1");
        beams.put("N", "0:1");
        beams.put("D", "1:2");
        beams.put("T", "1:3");
        beams.put("A", "2:4");    
        beams.put("POL G", "0:0");
        beams.put("POL E", "-1:0.1");
        beams.put("POL P", "1:1");
        beams.put("POL N", "0:1");
        beams.put("POL D", "1:2");
        beams.put("POL T", "1:3");
        beams.put("POL A", "2:4");

        ejectiles.put("P",  "1:1");
        ejectiles.put("P'", "1:1.1");
        ejectiles.put("PG", "1:1.2");
        ejectiles.put("P'G","1:1.3");
        ejectiles.put("N",  "0:1");
        ejectiles.put("N'", "0:1.1");
        ejectiles.put("NG", "0:1.2");
        ejectiles.put("N'G","0:1.3");
        ejectiles.put("D",  "1:2");
        ejectiles.put("D'", "1:2.1");
        ejectiles.put("DG", "1:2.2");
        ejectiles.put("D'G","1:2.3");
        ejectiles.put("T",  "1:3");
        ejectiles.put("T'", "1:3.1");
        ejectiles.put("TG", "1:3.2");
        ejectiles.put("T'G","1:3.3");
        ejectiles.put("A",  "2:4");
        ejectiles.put("A'", "2:4.1");
        ejectiles.put("AG", "2:4.2");
        ejectiles.put("A'G","2:4.3");
    }
    
    /*
     * nucType: TARGET, BEAM, EJECTILE. Default=TARGET
     * sortOption:  ='A' or 'MASS' sort by mass first, 
                    ='Z' by proton number first
       default by proton number first
     */
    public static float compareNucleus(String nuc1,String nuc2,String nucType,String sortOption){
        float Z1=-1,Z2=-1,A1=-1,A2=-1;
        
        try{
            String s1=getZA(nuc1,nucType);
            String[] sv1=s1.split(":");
            if(Str.isNumeric(sv1[0]))
                Z1=Float.parseFloat(sv1[0]);
            if(Str.isNumeric(sv1[1]))
                A1=Float.parseFloat(sv1[1]);
        }catch(Exception e){
        }
        
        try{
            String s2=getZA(nuc2,nucType);
            String[] sv2=s2.split(":");
            if(Str.isNumeric(sv2[0]))
                Z2=Float.parseFloat(sv2[0]);
            if(Str.isNumeric(sv2[1]))
                A2=Float.parseFloat(sv2[1]);
        }catch(Exception e){
        }
        
        //debug
        //System.out.println("nuc1="+nuc1+" "+Z1+" "+A1+" nuc2="+nuc2+" "+Z2+" "+A2+" nucType="+nucType);
        
        if(sortOption.equalsIgnoreCase("A") || sortOption.equalsIgnoreCase("MASS")){
            if(A1>=0 && A2>=0){
                if(A1!=A2)
                    return A1-A2;        
                else
                    return Z1-Z2;
            }else if(A1>=0)
                return -1;
            else if(A2>=0)
                return 1;
            else
                return Z1-Z2;           
        }else{
            if(Z1>=0 && Z2>=0){
                if(Z1!=Z2)
                    return Z1-Z2;        
                else
                    return A1-A2;
            }else if(Z1>=0)
                return 1;
            else if(Z2>=0)
                return -1;
            else
                return A1-A2;   
        }
        
    }

    public static float compareNucleus(Nucleus nuc1,Nucleus nuc2,String sortOption){
        float Z1=nuc1.Z();
        float Z2=nuc2.Z();
        float A1=nuc1.A();
        float A2=nuc2.A();
                
        //debug
        //if(Z1<0 || Z2<0) System.out.println("nuc1="+nuc1.nameEnsdf()+" "+Z1+" "+A1+" nuc2="+nuc2.nameEnsdf()+" "+Z2+" "+A2+" sortOption="+sortOption);
        
        if(sortOption.equalsIgnoreCase("A") || sortOption.equalsIgnoreCase("MASS")){
            if(A1>=0 && A2>=0){
                if(A1!=A2)
                    return A1-A2;        
                else
                    return Z1-Z2;
            }else if(A1>=0)
                return -1;
            else if(A2>=0)
                return 1;
            else
                return Z1-Z2;           
        }else{
            if(Z1>=0 && Z2>=0){
                if(Z1!=Z2)
                    return Z1-Z2;        
                else
                    return A1-A2;
            }else if(Z1>=0)
                return 1;
            else if(Z2>=0)
                return -1;
            else
                return A1-A2;   
        }
        
    }


    public static int getBeamZ(String nucStr) {
        return (int) getBeamZA(nucStr)[0];
    }
    //A could be arbitrary numerical float value set in initNucMap
    public static float[] getBeamZA(String nucStr) {
        float[] out=new float[2];
        Arrays.fill(out,-1);
        try {
            String s=getBeamZAstr(nucStr);
            String[] temp=s.split(":");
            float Z=Integer.parseInt(temp[0]);
            float A=Float.parseFloat(temp[1]) ;
            
            out[0]=Z;
            out[1]=A;
        }catch(Exception e) {
            
        }
        return out;
    }
    //Z and A separated by ":"
    public static String getBeamZAstr(String nucStr) {
        return getZA(nucStr,"BEAM");
    }
    
    private static String getZA0(String nucStr){
        Nucleus nuc=new Nucleus();
        try{
            nuc.parseNUCID(nucStr);
            return nuc.Z()+":"+nuc.A();
        }catch(Exception e){
            return "-10000:10000";//Z=-10000, A=10000
        }
    }
    
    private static String getZA(String nucStr,String nucType){
        
        String s=nucStr.trim();
        if(s.isEmpty())
            return "";
        
        if(nucType.equalsIgnoreCase("TARGET") || nucType.trim().isEmpty()){
            return getZA0(s);
        }

        try {
            if(others.containsKey(s))
                return others.get(s);
        }catch(NullPointerException e) {
            initNucMap();
            return getZA(nucStr,nucType);
        }

        
        //System.out.println(" nucStr="+nucStr+" type="+nucType+" beams.containsKey="+beams.containsKey(s));
        
        if(nucType.equalsIgnoreCase("BEAM")){
            if(beams.containsKey(s))
                return beams.get(s);
            else
                return getZA0(s);
        }
        
        if(nucType.equalsIgnoreCase("EJECTILE")){
            if(ejectiles.containsKey(s))
                return ejectiles.get(s);
            
            if(getZA0(s).trim().length()>0)
                return getZA0(s);
            
            char c=s.charAt(0);
            if(Character.isLetter(c))
                return "-10000:-10000";
            else if(Character.isDigit(c))
                return "-10000:-10000";
            else
                return "-10000:10000";
        }
        
        return getZA0(s);
            
    }
    
	public static String getShortDSID(String dsid) {
		try {
	        int lb=dsid.indexOf("(");
	        int rb=dsid.indexOf(")");
	        int ic=dsid.indexOf(",");
	        if(lb>=0 && lb<ic && ic<rb){
	            int is=dsid.indexOf(":");
                if(is==(rb+1) || dsid.contains("E="))
                    return dsid.substring(lb).trim();
                else {
                    return dsid.substring(lb,rb+1).trim();
                }

	        }
		}catch(Exception e) {
			
		}
		return dsid;
	}
	
	public static XDX2SDS calculateDecayEnergy(ENSDF ens,Level l) throws Exception{
		return calculateDecayEnergy(ens,l,"PARTICLE");
	}
	
	public static XDX2SDS calculateDecayEnergy(ENSDF ens,Level l,String option) throws Exception{
		return calculateDecayEnergy(ens,l,option,null);
	}
	
    //option="TOTAL" for total decay energy
    //      ="PARTICLE" for decay particle energy
	public static XDX2SDS calculateDecayEnergy(ENSDF ens,Level l,String option,HashMap<String,String> masses) throws Exception{
    	
	    float x,dx=0;
	    float factor=1.0f;
	    String ES="";
        XDX2SDS e=new XDX2SDS();
              
        boolean isSyst=false,isLimit=false;;
        boolean isRange=false;//true for level like SN+X, used in estimation for a range of levels
        
        Parent p=ens.parentAt(0);
        
        float pmass=p.nucleus().m();//in amu, multiply 931494 to convert to keV
        float mass=ens.nucleus().m();


	    if(!Str.isNumeric(l.ES())){
	    	e.setValues(0,0);
	    	e.setS("");
	    	e.setDS("");
    		
	    	if(l.ES().contains("+")){//SN+X,SP+X,SA+X, 6913.0+X
	    		int pos=l.ES().indexOf("+");
	    		String SN=l.ES().substring(0,pos);
	    		String X=l.ES().substring(pos+1);
	    		
	    		
	    		if(X.trim().length()==1 && Character.isLetter(X.trim().charAt(0))){
	    			//do nothing
	    		}else if(SN.trim().length()==1 && Character.isLetter(SN.trim().charAt(0))){
	    			String tempS=X;
	    			X=SN;
	    			SN=tempS;
	    		}else
	    			return e;
	    			
	    		//NOTE: a generic footnote comment is generated for decay energy of levels like this when parsing the input ENSDF file 
	    		if(Str.isNumeric(SN)){//6345.6+X
    				x=(p.level().EF()+p.QF()-Float.parseFloat(SN))/2; //set X=(Q-SN)/2, DX=X, estimation of decay energy for decay-heat calculation   			
	    		}else{//SN+X, for this case, calculate SN using masses
	    			Nucleus nucd;//daughter nucleus for SN or SP or SA
	    			Nucleus nuce;//ejectile in decay, proton or neutron or alpha
	    			Nucleus nuc=ens.nucleus();
                    
	    			int np=1;
	    			if(SN.equals("SN") || SN.equals("S(n)")){
	    				nucd=new Nucleus(nuc.z(),nuc.A()-1);
	    				nuce=new Nucleus(0,1);
	    			}else if(SN.equals("SP") || SN.equals("S(p)")){
	    				nucd=new Nucleus(nuc.z()-1,nuc.A()-1);
	    				nuce=new Nucleus(1,1);
	    				
	    			}else if(SN.equals("S2N") || SN.equals("S(2n)")){
	    				nucd=new Nucleus(nuc.z(),nuc.A()-2);
	    				nuce=new Nucleus(0,1);
	    				np=2;
	    				
	    			}else if(SN.equals("S2P") || SN.equals("S(2p)")){
	    				nucd=new Nucleus(nuc.z()-2,nuc.A()-2);
	    				nuce=new Nucleus(1,1);
	    				np=2;
	    			}else if(SN.equals("SA") || SN.contains("alpha")){
	    				nucd=new Nucleus(nuc.z()-2,nuc.A()-4);
	    				nuce=new Nucleus(2,4);
	    			}else{
		    			float ef=p.level().EF()+p.QF();
		    			ES=String.format("%.1f", ef);
		    			ES=ES+"-"+SN.trim()+"-"+X.trim().toLowerCase();
			    		ES="("+ES+")";
			    		e.setS(ES);
			    		return e;
	    			}
	    			
	    			float massd=findMass(nucd.A(), nucd.findElementSymbol(),masses).xf();
	    			float masse=findMass(nuce.A(), nuce.findElementSymbol(),masses).xf();
	    			float sn=(float) ((massd+np*masse-mass)*931494.32);//mass in unit of amu, energy in keV
	    			
	    			x=(p.level().EF()+p.QF()-sn)/2;
	    			
	    		}
	    		
	    		isRange=true;
	    		    			
	    	}else{
	    		return e;
	    	}

	    }else{//for regular level energy
	    	x=p.level().EF()+p.QF()-l.EF();//in keV
	    }
	    			                
        
        //debug
        //System.out.println("*****In DecayTableMaker A: p.Q="+p.QF()+" El="+l.EF()+"  x="+x);
        
        if(option.toUpperCase().equals("PARTICLE"))
        	factor=(float) (mass/pmass+(x+2*l.EF())/(2*pmass*931494.32));//relativistic for decay particle energy
        else
        	factor=1.0f;//for total energy released
        
        x=x*factor;
        
        
        //if(tempms.contains("#")||p.DQS().toUpperCase().contains("SY")){
        if(p.DQS().toUpperCase().contains("SY")){
        	dx=0;
        	isSyst=true;
        }
        
        if(!isSyst){
        	float defP=p.level().DEF();
        	float defQ=p.DQF();
        	float defL=l.DEF();
        	if(defP<0 && p.level().DES().isEmpty())
        		defP=0;
        	if(defQ<0 && p.DQS().isEmpty())
        		defQ=0;
        	if(defL<0 && l.DES().isEmpty())
        		defL=0;
        	
        	if(isRange)
        		dx=x;
        	else if(defP>=0 && defQ>=0 && defL>=0)
        		dx=factor*(float)Math.sqrt(Math.pow(p.level().DEF(),2)+Math.pow(p.DQF(),2)+Math.pow(l.DEF(),2));
        	else
        		isLimit=true;
        }
        
        e.setValues(x,dx);
        
        if(isSyst)
        	e.setDS("SY");
        else if(isLimit) {
        	if(p.level().DEF()<0){
        		e.setDS(p.level().DES());
        	}else if (p.DQF()<0)
                e.setDS(p.DQS());
            else if(l.DEF()<0) {
            	String ds=l.DES();
            	if(ds.startsWith("L"))
            		ds="G"+ds.substring(1);
            	else if(ds.startsWith("G"))
            		ds="L"+ds.substring(1);
            	
            	e.setDS(ds);
            }
        }else if(dx==0) {
        	e.setS(""+Float.valueOf(x).intValue());
        	e.setDS("");
        }

        return e;
    }    

    public static SDS2XDX findMass(int A,String name,HashMap<String,String> masses){
    	SDS2XDX result=new SDS2XDX();
    	XDX2SDS temp=new XDX2SDS();
    	boolean found=true;
    	String key;
    	String[] values;
    	String val_str="",unc_str="";
    	float val,unc;

    	try{
        	key=A+name.trim();
        	
            values=masses.get(key.toUpperCase()).split("\\s+");
            
            //debug
            //System.out.println("In MassChain: findMass: A="+A+" name="+name+" mass="+masses.get(key)+" v="+values[0]+" u="+values[1]);
            
            val_str=values[0].trim();
            unc_str=values[1].trim();
    	}catch(Exception e){
            val_str="";
            unc_str="";
            found=false;
    	}
    	
    	if(val_str.length()>0){
    		try{
        		val=Float.parseFloat(val_str.replace("#","").trim());
        		if(unc_str.length()==0)
        			unc=0f;
        		else
        			unc=Float.parseFloat(unc_str.replace("#","").trim());
       		                
                val=val/1000000;
                unc=unc/1000000;
                
                //debug
                //System.out.println("In MassChain: findMass: A="+A+" name="+name+" val="+val+" unc="+unc+" val_str="+val_str+" unc_str="+unc_str);

                
        		temp.setValues(val, unc);
        		
                //debug
                //System.out.println("In MassChain: findMass: A="+A+" name="+name+" temp.s="+temp.s()+" temp.ds="+temp.ds());
                
        		result.setValues(temp);
        		
        		if(Math.abs(1-Float.parseFloat(result.s())/A)>0.1){
        			System.out.println("Error in findMass(): something is wrong, probably overflow.");
        			found=false;
        		}
        		
        		if(val_str.contains("#")) result.setS(temp.s()+"#");
        		if(unc_str.length()>0 && unc_str.contains("#")) result.setDS(temp.ds()+"#");
        		
                //debug
                //System.out.println("In MassChain: findMass: A="+A+" name="+name+" result.s="+result.s()+" result.ds="+result.ds());
    
        		
    		}catch(Exception e){
    			found=false;
    		}
    	}  
    	
    	if(!found){
    		System.out.println("Warning: can't find mass for "+A+name+". will use A as mass.");
    		temp.setValues(A+0f,0f);
    		result.setValues(temp.S(), temp.DS());
    	}
    	
    	return result;
    }	
	
    /*
     * compare two ENSDF datasets of the same nucleus for ordering purpose
     * put adopted dataset the first
     * negative for ens1<ens2
     * positive for ens1>ens2
     * smaller one is placed first (from top)
     * option='A' sort by mass first,='Z' sort by proton number first
     */


    public static float compareENSDF(ENSDF ens1,ENSDF ens2,String sortOption){
        DatasetID dsid1=new DatasetID(ens1);
        DatasetID dsid2=new DatasetID(ens2);
        Nucleus nuc1=ens1.nucleus();
        Nucleus nuc2=ens2.nucleus();
        
        float f=compareNucleus(nuc1,nuc2,sortOption);
        if(f!=0)
            return f;
        
        if(dsid1.dsid.contains("ADOPTED") && dsid2.dsid.contains("ADOPTED"))
            return 0;
        if(dsid1.dsid.contains("ADOPTED"))
            return -1;
        if(dsid2.dsid.contains("ADOPTED"))
            return 1;
                
        
        String s1=dsid1.dsid;
        String s2=dsid2.dsid;
        
        if(s1.length()>30)
        	s1=dsid1.dsid.substring(0,30);
        if(s2.length()>30)
        	s2=dsid2.dsid.substring(0,30);
        
        if(dsid1.decayTypeOrder>=0 && dsid2.decayTypeOrder>=0){
            if(dsid1.decayTypeOrder!=dsid2.decayTypeOrder)
                return dsid1.decayTypeOrder-dsid2.decayTypeOrder;
            else{
                //System.out.println(" DSID1="+dsid1.dsid+" DSID2="+dsid2.dsid+" p1="+ens1.nParents()+" "+ens2.nParents());
                
                float e1=ens1.parentAt(0).level().ERPF();
                float e2=ens2.parentAt(0).level().ERPF();
                if(dsid1.decayParent.equals(dsid2.decayParent)){
                    if(e1>=0 && e2>=0)
                        return e1-e2;
                    else if(e1>=0)
                        return -1;
                    else if(e2>=0)
                        return 1;
                    else
                        return 0;
                }else{
                    int Z1=ens1.parentAt(0).nucleus().z();
                    int Z2=ens2.parentAt(0).nucleus().z();
                    int A1=ens1.parentAt(0).nucleus().A();
                    int A2=ens2.parentAt(0).nucleus().A();
                    if(Z1!=Z2)
                        return Z1-Z2;
                    else
                        return A1-A2;
                }

            }
        }else if(dsid1.decayTypeOrder>=0){
            return -1;
        }else if(dsid2.decayTypeOrder>=0){
            return 1;
        }else if(!dsid1.target.isEmpty() && !dsid2.target.isEmpty()){
            f=compareNucleus(dsid1.target,dsid2.target,"TARGET",sortOption);
            if(f!=0)
                return f;
            
            f=compareNucleus(dsid1.beam,dsid2.beam,"BEAM",sortOption);
            if(f!=0)
                return f;
            
            f=compareNucleus(dsid1.ejectile,dsid2.ejectile,"EJECTILE",sortOption);
            if(f!=0)
                return f;
            
            //here target, beam and ejectile all the same (NOTE "POL P" and "P" are considered the same)
            //But there are cases like DSID1=194PT(POL P,P'), DSID2=194PT(P,P'),(D,D') for two different datasets
            //For such cases, continue to compare the length of the DSID

                       
            int n1=s1.indexOf("),(");
            int n2=s2.indexOf("),(");
            
            if(n1<0 && n2<0) {
                n1=s1.indexOf(")");
                n2=s2.indexOf(")");
                
                s1=s1.substring(n1+1).trim();
                s2=s2.substring(n2+1).trim();

            }else if(n1<0) {
            	return -1;
            }else if(n2<0) {
            	return 1;
            }else {
                s1=s1.substring(n1+2).trim();
                s2=s2.substring(n2+2).trim();
            }
            
            return s1.compareTo(s2);
            
        }else if(!dsid1.target.isEmpty()){
            return -1;
        }else if(!dsid2.target.isEmpty()){
            return 1;
        }
        
        return s1.compareTo(s2);
    }
    
     
    public static float compareDSID(String dsid1Str,String dsid2Str,String sortOption){
        DatasetID dsid1=new DatasetID(dsid1Str);
        DatasetID dsid2=new DatasetID(dsid2Str);
       
        Nucleus nuc1=null, nuc2=null;
       
        try {
            if(dsid1.nucid.length()>0 )
            	nuc1=new Nucleus(dsid1.nucid);
            if(dsid2.nucid.length()>0)
            	nuc2=new Nucleus(dsid2.nucid);
        }catch(Exception e) {
        	
        }

        
        float f=0;
        if(nuc1!=null && nuc1.A()>0 && nuc2!=null && nuc2.A()>0) {
            f=compareNucleus(nuc1,nuc2,sortOption);
            if(f!=0)
                return f;
        }

        if(dsid1.dsid.contains("ADOPTED") && dsid2.dsid.contains("ADOPTED"))
            return 0;
        if(dsid1.dsid.contains("ADOPTED"))
            return -1;
        if(dsid2.dsid.contains("ADOPTED"))
            return 1;
                
        
        String s1=dsid1.dsid;
        String s2=dsid2.dsid;
        
        if(s1.length()>30)
        	s1=dsid1.dsid.substring(0,30);
        if(s2.length()>30)
        	s2=dsid2.dsid.substring(0,30);
        
        if(dsid1.decayTypeOrder>=0 && dsid2.decayTypeOrder>=0){
                return dsid1.decayTypeOrder-dsid2.decayTypeOrder;
        }else if(dsid1.decayTypeOrder>=0){
            return -1;
        }else if(dsid2.decayTypeOrder>=0){
            return 1;
        }else if(!dsid1.target.isEmpty() && !dsid2.target.isEmpty()){
            f=compareNucleus(dsid1.target,dsid2.target,"TARGET",sortOption);
            if(f!=0)
                return f;
            
            f=compareNucleus(dsid1.beam,dsid2.beam,"BEAM",sortOption);
            if(f!=0)
                return f;
            
            f=compareNucleus(dsid1.ejectile,dsid2.ejectile,"EJECTILE",sortOption);
            if(f!=0)
                return f;
            
            //here target, beam and ejectile all the same (NOTE "POL P" and "P" are considered the same)
            //But there are cases like DSID1=194PT(POL P,P'), DSID2=194PT(P,P'),(D,D') for two different datasets
            //For such cases, continue to compare the length of the DSID

                       
            int n1=s1.indexOf("),(");
            int n2=s2.indexOf("),(");
            
            if(n1<0 && n2<0) {
                n1=s1.indexOf(")");
                n2=s2.indexOf(")");
                
                s1=s1.substring(n1+1).trim();
                s2=s2.substring(n2+1).trim();

            }else if(n1<0) {
            	return -1;
            }else if(n2<0) {
            	return 1;
            }else {
                s1=s1.substring(n1+2).trim();
                s2=s2.substring(n2+2).trim();
            }
            
            return s1.compareTo(s2);
            
        }else if(!dsid1.target.isEmpty()){
            return -1;
        }else if(!dsid2.target.isEmpty()){
            return 1;
        }
        
        return s1.compareTo(s2);
    }
    
    /*
     * sort ENSDF datasets in ensdfsV according to DSID (decay,reaction,...)
     * sortOption:  ='A' or 'MASS' sort by mass first, 
                    ='Z' by proton number first
     * default by proton number first
     */
    
    public static void sortENSDFs(List<ENSDF> ensdfsV,String sortOption){
        int n=ensdfsV.size();
        if(n<=1)
            return;
        
        initNucMap();
        
        ENSDF[] ens=new ENSDF[n];
        ensdfsV.toArray(ens);
        for(int i=0;i<n-1;i++){
            for(int j=i+1;j<n;j++){
                if(EnsdfUtil.compareENSDF(ens[i], ens[j], sortOption)>0){
                	
                    ENSDF temp=ens[i];
                    ens[i]=ens[j];
                    ens[j]=temp;

                }
            }
        }
        
        ensdfsV.clear();
        for(int i=0;i<n;i++)
            ensdfsV.add(ens[i]);
    }
    
    public static void sortDSIDs(List<String> dsids,String sortOption){
        int n=dsids.size();
        if(n<=1)
            return;
        
        initNucMap();
        
        String[] dsidArray=new String[n];
        dsids.toArray(dsidArray);
        for(int i=0;i<n-1;i++){
            for(int j=i+1;j<n;j++){
                if(EnsdfUtil.compareDSID(dsidArray[i], dsidArray[j], sortOption)>0){
                	
                    String temp=dsidArray[i];
                    dsidArray[i]=dsidArray[j];
                    dsidArray[j]=temp;

                }
            }
        }
        
        dsids.clear();
        for(int i=0;i<n;i++)
            dsids.add(dsidArray[i]);
    }
    
    public static void sortKeynosV(Vector<String> keynosV) {

        sortKeynosV(keynosV,"ASCENDING");
    }
    
    /*
     * order=ASCENDING by default
     *       DESCending ro order keynumbers by descending order of year but still ascending order of letters and last two numbers
     */
    public static void sortKeynosV(Vector<String> keynosV,String order) {

        if(keynosV==null || keynosV.size()<=1)
            return;
        
        String[] keynos=new String[keynosV.size()];
        keynosV.toArray(keynos);
        
        order=order.toUpperCase().trim();
        if(order.startsWith("D")) {
            Arrays.sort(keynos,new Comparator<Object>() {
                public int compare(Object one,Object two){
                    String a=(String)one;
                    String b=(String)two;
                    
                    String ap1=a,ap2="",bp1=b,bp2="";
                    if(a.length()>=4) {
                    	ap1=a.substring(0,4);
                    	ap2=a.substring(4);
                    			
                    }
                    if(b.length()>=4) {
                    	bp1=b.substring(0,4);
                    	bp2=b.substring(4);
                    			
                    }
                   
                    int n=ap1.compareToIgnoreCase(bp1);
                    if(n!=0)
                    	return -n;//reversed order of natural order (ascending)
                    
                    n=ap2.compareToIgnoreCase(bp2);
                    return n;//natural order for part 2 (e.g., for "Wa16" in 2021Wa16)
                }
            });
        }else {
            Arrays.sort(keynos,new Comparator<Object>() {
                public int compare(Object one,Object two){
                    String a=(String)one;
                    String b=(String)two;
                    
                    int out=a.compareToIgnoreCase(b);
                    return out;
                }
            });
        }

        keynosV.clear();
        
        for(int i=0;i<keynos.length;i++)
            keynosV.add(keynos[i]);
    }
    
    public static <T extends Record> Vector<T> orderRecordsByFieldPrecision(Vector<T> recordsV,String fieldName){
        Vector<T> out=new Vector<T>();
        fieldName=fieldName.toUpperCase().trim();

        if(recordsV.size()==0 || fieldName.isEmpty())
            return out;

        int si1=0,si2=0,dsi1=0,dsi2=0;
        if(fieldName.equals("E")) {
            si1=9;
            si2=19;
            dsi1=19;
            dsi2=21;
        }else if("RI,IB,IA,IP".indexOf(fieldName)%3==0 && fieldName.length()==2) {
            si1=21;
            si2=29;
            dsi1=29;
            dsi2=31;
        }else if(fieldName.equals("IE") || fieldName.equals("HF")) {
            si1=31;
            si2=39;
            dsi1=39;
            dsi2=41;
        }else if(fieldName.equals("T")) {
            si1=39;
            si2=49;
            dsi1=49;
            dsi2=55;
        }else if(fieldName.equals("MR") || fieldName.equals("LOGFT")) {
            si1=41;
            si2=49;
            dsi1=49;
            dsi2=55;
        }else if(fieldName.equals("TI") || fieldName.equals("S")) {
            si1=64;
            si2=74;
            dsi1=74;
            dsi2=76;
        }
        
        Vector<T> tempV=new Vector<T>();
    
        for(int i=0;i<recordsV.size();i++) {
            try {
                T rec=(T)recordsV.get(i);
                String S1=rec.recordLine().substring(si1,si2).trim();
                String DS1=rec.recordLine().substring(dsi1,dsi2).trim();
                 
                
                boolean isNum1=Str.isNumeric(DS1.replace("+", "").replace("-", "").trim());
                if(!S1.isEmpty() && (isNum1 || DS1.isEmpty())) {

                    int j=0;
                    int n1=S1.indexOf(".");
                    if(n1>0) 
                        n1=S1.length()-n1-1;
                    
                    SDS2XDX sx1=s2x(S1,DS1);
                    
                    for(j=0;j<out.size();j++) {
                        
                        String S2=out.get(j).recordLine().substring(si1,si2).trim();
                        String DS2=out.get(j).recordLine().substring(dsi1,dsi2).trim();
                        int n2=S2.indexOf(".");
                        if(n2>0) 
                            n2=S2.length()-n2-1;
                        
                        SDS2XDX sx2=s2x(S2,DS2);

                        double r1=0,r2=0;
                        if(sx1.dx()>0) {
                        	r1=Math.abs(sx1.dx()/sx1.x());
                        }else if(sx1.dxu()>0) {
                        	double dx=Math.min(sx1.dxu(), sx1.dxl());
                        	r1=Math.abs(dx/sx1.x());
                        }
                        if(sx2.dx()>0) {
                        	r2=Math.abs(sx2.dx()/sx2.x());
                        }else if(sx2.dxu()>0) {
                        	double dx=Math.min(sx2.dxu(), sx2.dxl());
                        	r2=Math.abs(dx/sx2.x());
                        }

                        if(r1>0 && r2>0) {
                            if(Math.abs(r1/r2-1)<0.05) {//rec1 and rec2 has similar precision, then number of significant digits of DS
                                if(DS1.length()>DS2.length())//rec1 is more precise than rec2, insert rec1 before rec2
                                    break;
                                else if(DS1.length()<DS2.length() || r1>r2)//rec1 is less precise than rec2, continue to compare next record
                                    continue;
                                else 
                                    break;                              
                            }else if(r1>r2) //rec1 is less precise than rec2
                                continue;
                            else //rec1 is more precise than rec2, insert rec1 before rec2
                                break;
                                
                        }else if(r1>0) {//rec2 has no uncertainty but rec1 has, insert rec1 before rec2
                            break;
                        }else if(r2>0) {//rec1 has no uncertainty but rec2 has, continue to next record in ordered records
                            continue;
                        }else {//r1==r2==0, no uncertainty (empty), then compare position of "."
                            if(n1<n2)
                                continue;
                            else
                                break;
                        }
                    }
                    
                    out.add(j,rec); 
                }else
                    tempV.add(rec);//contains record with uncertainty like LT, GT, ...
            }catch(Exception e) {               
            }

        }
        
        //sort tempV: 
        Vector<T> V1=new Vector<T>();
        Vector<T> V2=new Vector<T>();
        Vector<T> V3=new Vector<T>();
        Vector<T> V4=new Vector<T>();
        HashMap<T,String> recValueMap=new HashMap<T,String>();
        for(int i=0;i<tempV.size();i++) {
        	T rec=(T)recordsV.get(i);
        	String S=rec.recordLine().substring(si1,si2).trim();
            String DS=rec.recordLine().substring(dsi1,dsi2).trim();
            if(Str.isNumeric(S)) {
                if(DS.startsWith("L") || DS.equals("AP")) {
                	V1.add(rec);
                }else if(DS.startsWith("G")) {
                	V2.add(rec);
                }else {
                	V3.add(rec);
                }
            }else {
            	V4.add(rec);
            }  
            
            recValueMap.put(rec, S+" "+DS);
        }
        
        V1=orderRecordsByValueUp(V1,recValueMap);
        V2=orderRecordsByValueDown(V2,recValueMap);
        V3=orderRecordsByValueUp(V3,recValueMap);
        
        out.addAll(V1);
        out.addAll(V2);
        out.addAll(V3);
        out.addAll(V4);
        
        return out;
    }
    
    private static <T extends Record> Vector<T> orderRecordsByValueUp(Vector<T> V,HashMap<T,String> valueMap){
    	Vector<T> out=new Vector<T>();
    	try {
    		
    		if(V.size()==0 || valueMap.size()==0)
    			return out;
    		
    		int imin=-1;
    		double min=1E12;
    		String[] temp=null;
    		T minRec=null;
    		
        	for(int i=0;i<V.size()-1;i++) {
        		T r1=V.get(i);
        		String s1=valueMap.get(r1);
        		String ds1="";
        		temp=s1.split("[\\s]+");
        		if(temp.length>0) {
        			s1=temp[0];
        			if(temp.length>1)
        				ds1=temp[1];
        		}
        		
        		if(!Str.isNumeric(s1))
        			continue;
        		
        		min=Double.parseDouble(s1);
        		minRec=r1;
        		imin=i;
        		
        		for(int j=i+1;j<V.size();j++) {
        			T r2=V.get(j);
        			String s2=valueMap.get(r2);
            		String ds2="";
            		temp=s2.split("[\\s]+");
            		if(temp.length>0) {
            			s2=temp[0];
            			if(temp.length>1)
            				ds2=temp[1];
            		}
            		
            		if(!Str.isNumeric(s2))
            			continue;
            		
            		double v2=Double.parseDouble(s2);
            		
            		if(v2<min) {
            			min=v2;
            			imin=j;
            			minRec=r2;
            		}else if(v2==min) {
            			if(!ds2.equals(ds1) && ds2.startsWith("L")) {
                			min=v2;
                			imin=j;
                			minRec=r2;
            			}
            		}
 
        		}
        		
           		
        		if(minRec!=r1) {
        			V.remove(imin);
        			V.add(imin,r1);
        			
        			V.remove(i);
        			V.add(i,minRec);
        			
        		}
        			
        		out.add(minRec);
        	}
    	}catch(Exception e) {
    		
    	}

    	
    	return out;
    }
    
    private static <T extends Record> Vector<T> orderRecordsByValueDown(Vector<T> V,HashMap<T,String> valueMap){
    	Vector<T> out=new Vector<T>();
    	try {
    		int imax=-1;
    		double max=1E-6;
    		String[] temp=null;
    		T maxRec=null;
    		
        	for(int i=0;i<V.size()-1;i++) {
        		T r1=V.get(i);
        		String s1=valueMap.get(r1);
        		String ds1="";
        		temp=s1.split("[\\s]+");
        		if(temp.length>0) {
        			s1=temp[0];
        			if(temp.length>1)
        				ds1=temp[1];
        		}
        		
        		if(!Str.isNumeric(s1))
        			continue;
        		
        		max=Double.parseDouble(s1);
        		maxRec=r1;
        		imax=i;
        		
        		for(int j=i+1;j<V.size();j++) {
        			T r2=V.get(j);
        			String s2=valueMap.get(r2);
            		String ds2="";
            		temp=s2.split("[\\s]+");
            		if(temp.length>0) {
            			s2=temp[0];
            			if(temp.length>1)
            				ds2=temp[1];
            		}
            		
            		if(!Str.isNumeric(s2))
            			continue;
            		
            		double v2=Double.parseDouble(s2);
            		
            		if(v2>max) {
            			max=v2;
            			imax=j;
            			maxRec=r2;
            		}else if(v2==max) {
            			if(!ds2.equals(ds1) && ds1.startsWith("L")) {
                			max=v2;
                			imax=j;
                			maxRec=r2;
            			}
            		}
 
        		}
        		
           		
        		if(maxRec!=r1) {
        			V.remove(imax);
        			V.add(imax,r1);
        			
        			V.remove(i);
        			V.add(i,maxRec);
        			
        		}
        			
        		out.add(maxRec);
        	}
    	}catch(Exception e) {
    		
    	}

    	
    	return out;
    }
    
    public static void sortENSDFs(List<ENSDF> ensdfsV){
        sortENSDFs(ensdfsV,"Z");
    }
    
    public static void sortDSIDs(List<String> dsids){
        sortDSIDs(dsids,"Z");
    }
    
    public static Vector<ENSDF> sortEnsdfFiles(Vector<File> filesV){
        Vector<ENSDF> ensdfsV=new Vector<ENSDF>();
        Vector<String> lines=new Vector<String>();
        HashMap<ENSDF,File> ensdfsMap=new HashMap<ENSDF,File>();
        
        for(int i=0;i<filesV.size();i++){
            File f=filesV.get(i);
            ENSDF ens=null;
            MassChain chain=null;
            try{
                chain=new MassChain();
                chain.load(f);
                
                for(int j=0;j<chain.nBlocks();j++) {
                    lines=chain.getBlockAt(j);

                    try {
                        ens=new ENSDF();
                        ens.setValues(lines,"LITTLE");   
                    }catch(Exception e) {
                        continue;
                    }

                    ensdfsV.add(ens);
                    ensdfsMap.put(ens, f);
                }

            }catch(Exception e){
                continue;
            }

        }
        
        sortENSDFs(ensdfsV);
               
        Vector<File> newFilesV=new Vector<File>();
        for(int i=0;i<ensdfsV.size();i++){
            File f=ensdfsMap.get(ensdfsV.get(i));
            if(!newFilesV.contains(f)) {
                newFilesV.add(f);
                filesV.remove(f);
            }
        }
        
        filesV.addAll(newFilesV);
        
        return ensdfsV;
    }
    
    
    public static String wrapENSDFLines(String ENSDFtext) {
        try{            
            if(ENSDFtext==null || ENSDFtext.trim().isEmpty())
                return "";
           
            String[] lines=ENSDFtext.split("[\n\r]");
            if(lines.length<=0)
            	return "";
            
            Vector<String> linesV=new Vector<String>();
            for(int i=0;i<lines.length;i++) {
            	if(lines[i].length()>0)
            		linesV.add(lines[i]);
            }
            return wrapENSDFLinesToStr(linesV);
        }catch(Exception e){
                e.printStackTrace();
        } 
        
        return "";
    }
    
    
    public static String wrapENSDFLinesToStr(Vector<String> ENSDFlinesV) {
    	Vector<String> newLinesV=wrapENSDFLines(ENSDFlinesV);
    	String out="";

    	for(int i=0;i<newLinesV.size();i++) {
    		out+=newLinesV.get(i)+"\n";
    	}
    	if(out.length()>0)
    		out=out.substring(0,out.length()-1);
    	
    	return out;
    }
    
    public static Vector<String> wrapENSDFLines(Vector<String> ENSDFLinesV){
    	Vector<String> newLinesV=new Vector<String>();
    	
    	try {
            String lineType="cX";
            String NUCID0="xxxxx";
            String NUCID=NUCID0;
            String line="";

            if(ENSDFLinesV.size()<=0)
            	return newLinesV;
            
            //for(String s:ENSDFLinesV)
            //	System.out.println(s);
            
            int i=0;
            while(i<ENSDFLinesV.size() && ENSDFLinesV.get(i).length()>10) {
            	line=ENSDFLinesV.get(i).trim();
  
            	String[] out=EnsdfUtil.findENSDFLineInfo(line);
            	
              	//System.out.println(ENSDFLinesV.size()+" NUCID="+out[0].length()+"$ lineType="+lineType+" line="+line);
            	if(out[0].length()>0) {
            		NUCID=out[0];
            		lineType=out[1];
            		
            		//System.out.println("NUCID="+NUCID+" lineType="+lineType+" line="+line);
            		
            		break;
            	}
            	
            	i++;
            }
            
            String prefix=Str.makeENSDFLinePrefix(NUCID, lineType);//length=9
            
            //System.out.println("NUCID="+NUCID+" lineType="+lineType+" prefix="+prefix);
            
            Vector<String> linesV=new Vector<String>();
     
            for(i=0;i<ENSDFLinesV.size();i++) {
            	line=ENSDFLinesV.get(i);
            	if(line.trim().isEmpty())
            		continue;
            	
            	String[] out=EnsdfUtil.findENSDFLineInfo(line);
            	
            	String tempNUCID=out[0];
            	String lineBody=out[2];
            	Nucleus nuc=new Nucleus(tempNUCID);
            	String AS=nuc.A()+"";
            	String EN=nuc.EN();
            	
            	//System.out.println("NUCID="+NUCID+" lineType="+lineType+" body="+lineBody+" prefix="+prefix+" line="+line);
            			
            	if(tempNUCID.trim().equalsIgnoreCase(NUCID.trim()) ) {
            		//do nothing
            		line=prefix+lineBody; 
            	}else if(Str.isInteger(AS) && (EN.isEmpty()||Str.isLetters(EN))) {//ENSDF line with different NUCID
            		if(NUCID.equals(NUCID0))
            			NUCID=tempNUCID;
            		
            		line=prefix+lineBody;          		
            	}else {//line without NUCID
            		line=prefix+line.trim();
            	}
            	
            	linesV.add(line);
            	
            	//System.out.println("NUCID="+NUCID+" tempNUCID="+tempNUCID+" AS="+AS+" EN="+EN+" prefix="+prefix+" line="+line);
            }

            Comment c=new Comment();
            c.setValues(linesV);
           
            if(c.cbody().length()>0){  
               
                String str=c.cbody();
                if(c.head().length()>0)
                	str=c.head()+"$"+str;
                else {                  
                    //String firstLine=linesV.get(0);
                    //if(firstLine.substring(9).trim().startsWith("$"))
                	
                	if(c.type().trim().isEmpty()) {
                		if(str.startsWith("$"))
                			str=str.substring(1);
                	}else {
                		if(!str.startsWith("$"))
                        str="$"+str;
                	}

                }
                
                //System.out.println(str);
                newLinesV=Str.wrapStringToLines(str,prefix,80,5);
   
            }
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return newLinesV;
    }
    /*
     * wrap input ENSDF lines (comments) to new lines filled up to 
     * 80 column in each line with long lines being split up
     */
    public static Vector<String> wrapENSDFLines_old(Vector<String> ENSDFlinesV) {
    	
    	Vector<String> newLinesV=new Vector<String>();
    	
        try{

        	if(ENSDFlinesV==null || ENSDFlinesV.size()<=0)
        		return newLinesV;

            String lineType="cX";
            String NUCID0="xxxxx";
            String NUCID=NUCID0;
            String line="";
            int i=0;
            
            while(i<ENSDFlinesV.size() && ENSDFlinesV.get(i).length()>10) {
            	line=ENSDFlinesV.get(i);
            	char c=line.toUpperCase().charAt(6);
            	String s=line.substring(0,5);
            	try {
                	Nucleus nuc=new Nucleus(s);
                	if(nuc.A()>0 && c=='C') {
                		NUCID=s;
                		lineType=line.substring(6,9);
                		
                		break;
                	} 
            	}catch(Exception e) {
            		
            	}
          		i++;
            }
            
            String prefix=Str.makeENSDFLinePrefix(NUCID, lineType);//length=9

            Vector<String> linesV=new Vector<String>();
            for(i=0;i<ENSDFlinesV.size();i++) {
            	line=ENSDFlinesV.get(i);
            	if(line.trim().isEmpty())
            		continue;
            	
            	//System.out.println(" line="+line+" NUCID="+NUCID+" lineType="+lineType);
            	
            	String tempNUCID=line.substring(0,5);
            	String AS=tempNUCID.substring(0,3).trim();
            	String EN=tempNUCID.substring(3).trim();
            	if(tempNUCID.equals(NUCID)) {
            		//do nothing
            	}else if(Str.isInteger(AS) && (EN.isEmpty()||Str.isLetters(EN)) && line.charAt(8)==' ') {//ENSDF line with different NUCID
            		if(NUCID.equals(NUCID0))
            			NUCID=tempNUCID;
            		else
            			line=prefix+line.substring(9);          		
            	}else {//line without NUCID
            		line=prefix+line.trim();
            	}
            	
            	linesV.add(line);
            	
            	//System.out.println("NUCID="+NUCID+" tempNUCID="+tempNUCID+" AS="+AS+" EN="+EN+" prefix="+prefix+" line="+line);
            }

            Comment c=new Comment();
            c.setValues(linesV);
	
            String str="";
            if(c.cbody().length()>0){                 
                str=c.cbody();

                
                if(c.head().length()>0)
                	str=c.head()+"$"+str;
                else {                  
                    String firstLine=ENSDFlinesV.get(0);
                    if(firstLine.substring(9).trim().startsWith("$"))
                    	str="$"+str;
                }
                
                newLinesV=Str.wrapStringToLines(str,prefix,80,5);

            }

        }catch(Exception e){
                e.printStackTrace();
        } 
        
        return newLinesV;
    }
    
    public static SDS2XDX widthToT12(SDS2XDX width,String unit,int errorLimit) {
    	try {
    		return widthToLifetime(width,unit,errorLimit).multiply(Math.log(2.0),0);
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public static SDS2XDX widthToLifetime(SDS2XDX width,String unit,int errorLimit) {
    	ArrayList<String> units=new ArrayList<String>(Arrays.asList("ueV","meV","eV","keV","MeV","GeV"));
    	double[] factors= {1.0E-6,1.0E-3,1.0,1000,1.0E6,1.0E9};
    	try {
    		String temp=unit.toUpperCase();
    		if(!temp.endsWith("EV"))
    			return null;
 
    		char c1=temp.charAt(0);
    		if(c1=='G') {
    			unit="GeV";
    		}else if(c1!='M') {
    			unit=unit.substring(0,unit.length()-2).toLowerCase()+"eV";
    		}else {
    			unit=unit.charAt(0)+"eV";
    		}

    		int index=units.indexOf(unit);
    		double f=1.0;
    		if(index>=0)
    			f=factors[index];
    		
    		width.setErrorLimit(errorLimit);
    		width=width.multiply(f, 0);
    		
    		SDS2XDX T=new SDS2XDX();
    		double x=hbar*1000;
    		T.setErrorLimit(errorLimit);
    		
    		T.setValues(x,0);
    		T=T.divided(width);
    		return T;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    /*
     * ds is ENSDF-fomrat uncertainty
     * output T is in unit of second
     */
    public static SDS2XDX widthToT12(String s,String ds,String unit,int errorLimit) {
    	try {
    		return widthToLifetime(s,ds,unit,errorLimit).multiply(Math.log(2.0),0);
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public static SDS2XDX widthToLifetime(String s,String ds,String unit,int errorLimit) {
    	ArrayList<String> units=new ArrayList<String>(Arrays.asList("ueV","meV","eV","keV","MeV","GeV"));
    	double[] factors= {1.0E-6,1.0E-3,1.0,1000,1.0E6,1.0E9};
    	try {
    		String temp=unit.toUpperCase();
    		if(!temp.endsWith("EV"))
    			return null;
 
    		char c1=temp.charAt(0);
    		if(c1=='G') {
    			unit="GeV";
    		}else if(c1!='M') {
    			unit=unit.substring(0,unit.length()-2).toLowerCase()+"eV";
    		}else {
    			unit=unit.charAt(0)+"eV";
    		}

    		int index=units.indexOf(unit);
    		double f=1.0;
    		if(index>=0)
    			f=factors[index];
    		
    		SDS2XDX width=new SDS2XDX(s,ds);
    		width.setErrorLimit(errorLimit);
    		width=width.multiply(f, 0);
    		
    		SDS2XDX T=new SDS2XDX();
    		double x=hbar*1000;
    		T.setErrorLimit(errorLimit);
    		
    		T.setValues(x,0);
    		T=T.divided(width);
    		return T;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    

    
    /*
     * output width is in unit of eV
     */
    public static SDS2XDX T12ToWidth(SDS2XDX t12,String unit,int errorLimit) {
    	try {
    		return lifetimeToWidth(t12,unit,errorLimit).multiply(Math.log(2.0),0);
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public static SDS2XDX lifetimeToWidth(SDS2XDX t12,String unit,int errorLimit) {
    	try {
    		if(!EnsdfUtil.isHalflifeUnit(unit))
    			return null;
    		
    		t12.setErrorLimit(errorLimit);
    		
    		if(!unit.toUpperCase().equals("S")) {
        		//T12 t12=new T12();
        		//t12.setValues(s, unit, ds);
    			double f=EnsdfUtil.T12UnitMultiplier(unit);
    			t12=t12.multiply(f,0);
    		}

    		SDS2XDX width=new SDS2XDX();
    		width.setErrorLimit(errorLimit);
    		double x=hbar*1000;
    		width.setValues(x,0);
    		width=width.divided(t12);
    		return width;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    /*
     * output width is in unit of eV
     */
    public static SDS2XDX T12ToWidth(String s,String ds,String unit,int errorLimit) {
    	try {
    		return lifetimeToWidth(s,ds,unit,errorLimit).multiply(Math.log(2.0),0);
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    public static SDS2XDX lifetimeToWidth(String s,String ds,String unit,int errorLimit) {
    	try {
    		if(!EnsdfUtil.isHalflifeUnit(unit))
    			return null;
    		
    		SDS2XDX T=new SDS2XDX(s,ds);
    		T.setErrorLimit(errorLimit);
    		
    		if(!unit.toUpperCase().equals("S")) {
        		//T12 t12=new T12();
        		//t12.setValues(s, unit, ds);
    			double f=EnsdfUtil.T12UnitMultiplier(unit);
    			T=T.multiply(f,0);
    		}

    		SDS2XDX width=new SDS2XDX();
    		width.setErrorLimit(errorLimit);
    		double x=hbar*1000;
    		width.setValues(x,0);
    		width=width.divided(T);
    		return width;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    /*
     * wg=resonance strength=(2J+1)/2*widthG*widthP/(widthG+widthP)
     * g2pRatio=widthG/widthP
     * spin=J of resonance
     * 
     * result in unit of second
     */
    public static SDS2XDX findT12OfResonance(SDS2XDX wg,String unit,SDS2XDX g2pRatio,double spin,String uncOption) {
       	ArrayList<String> units=new ArrayList<String>(Arrays.asList("ueV","meV","eV","keV","MeV","GeV"));
    	double[] factors= {1.0E-6,1.0E-3,1.0,1000,1.0E6,1.0E9};//convert width value of given unit to value in eV
    	
    	try {
    		/*
    		 * let a=g2pRatio=widthG/widthP
    		 *     b=(2J+1)/2*widthG*widthP/(widthG+widthP)
    		 * then
    		 *     width=2/(2J+1)*b*(a+1)^2/a
    		 *     lifetime=hbar*(2J+1)/2*a/[(a+1)^2*b]
    		 */

    		String temp=unit.toUpperCase();
    		if(!temp.endsWith("EV"))
    			return null;
 
    		char c1=temp.charAt(0);
    		if(c1=='G') {
    			unit="GeV";
    		}else if(c1!='M') {
    			unit=unit.substring(0,unit.length()-2).toLowerCase()+"eV";
    		}else {
    			unit=unit.charAt(0)+"eV";
    		}

    		int index=units.indexOf(unit);
    		double f=1.0;
    		if(index>=0)
    			f=factors[index];
    		
    		String expr="a/(a+1)^2/b";
    		String varStr="a="+g2pRatio.sds()+", b="+wg.sds();
    		
    		double hbar=6.58212E-16;//[eV*s], hbar
    		f=hbar*(2*spin+1)/2.0/f*Math.log(2);

    		
    		//System.out.println("wg="+wg.s()+" "+wg.ds()+" ratio="+g2pRatio.s()+" "+g2pRatio.ds()+" unit="+unit+" f="+f);
    		
    		int errorLimit=99;
    		
    		SDS2XDX result=EnsdfUtil.evalExpression(expr,varStr,errorLimit,uncOption);
    		result=result.multiply(f);
    		
    		return result;
    		
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public static SDS2XDX findT12OfResonance(String wgS,String unit,String g2pRatioS,double spin,String uncOption) {
    	try {
    		
    		SDS2XDX wg=SDS2XDX.parse(wgS);

    		SDS2XDX g2pRatio=SDS2XDX.parse(g2pRatioS);
  
    		if(wg!=null && g2pRatio!=null) {
    			return findT12OfResonance(wg,unit,g2pRatio,spin,uncOption);
    		}
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    //result in unit of eV
    public static SDS2XDX findWidthOfResonance(SDS2XDX wg,String unit,SDS2XDX g2pRatio,double spin,String uncOption) {
       	ArrayList<String> units=new ArrayList<String>(Arrays.asList("ueV","meV","eV","keV","MeV","GeV"));
    	double[] factors= {1.0E-6,1.0E-3,1.0,1000,1.0E6,1.0E9};//convert width value of given unit to value in eV
    	
    	try {
    		/*
    		 * let a=g2pRatio=widthG/widthP
    		 *     b=(2J+1)/2*widthG*widthP/(widthG+widthP)
    		 * then
    		 *     width=2/(2J+1)*b*(a+1)^2/a
    		 *     lifetime=hbar*(2J+1)/2*a/[(a+1)^2*b]
    		 */


    		String temp=unit.toUpperCase();
    		if(!temp.endsWith("EV"))
    			return null;
 
    		char c1=temp.charAt(0);
    		if(c1=='G') {
    			unit="GeV";
    		}else if(c1!='M') {
    			unit=unit.substring(0,unit.length()-2).toLowerCase()+"eV";
    		}else {
    			unit=unit.charAt(0)+"eV";
    		}

    		int index=units.indexOf(unit);
    		double f=1.0;
    		if(index>=0)
    			f=factors[index];
    		
    		String expr="b*(a+1)^2/a";
    		String varStr="a="+g2pRatio.sds()+", b="+wg.sds();
    		
    		f=f*2.0/(2*spin+1);   		
    		int errorLimit=99;
    		
    		SDS2XDX result=EnsdfUtil.evalExpression(expr,varStr,errorLimit,uncOption);
    		result=result.multiply(f);
    		
    		return result;
    		
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public static SDS2XDX findWidthOfResonance(String wgS,String unit,String g2pRatioS,double spin,String uncOption) {
    	try {
    		SDS2XDX wg=SDS2XDX.parse(wgS);
    		SDS2XDX g2pRatio=SDS2XDX.parse(g2pRatioS);
    		if(wg!=null && g2pRatio!=null) {
    			return findWidthOfResonance(wg,unit,g2pRatio,spin,uncOption);
    		}
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    /*
     * Example:
     * expr: b*(a+1)^2/a
     * varStr: a=21(2), b=0.55(3) in ENSDF style
     * uncOption=Taylor or T, Normal or N, Derivative or D, for propagating unc using Taylor's expansion
     *          =MaxMin or Max or Min or M, Extreme or E for propagating unc using max&min approach
     *          Default is max&min
     */
    public static SDS2XDX evalExpression(String expr,String varStr,int errorLimit,int nsig,String uncOption) {
    	try {
    		UncertaintyPropagator uncCalc=new UncertaintyPropagator();
    		uncCalc.calc(expr, varStr);
    		
    		double x=uncCalc.getValue();
    		double dxl=uncCalc.getDeltaLower();
    		double dxu=uncCalc.getDeltaUpper();
    		uncOption=uncOption.toUpperCase().trim();
    		if(uncOption.startsWith("T") || uncOption.startsWith("N") || uncOption.startsWith("D")) {
    			dxl=uncCalc.getTaylorError();
    			dxu=dxl;
    		}
    		
    		SDS2XDX result=new SDS2XDX();
    		result.setErrorLimit(errorLimit);  	
    		result.setNsig(nsig);
    		result.setValues(x, dxu, dxl);
     		
    		return result;
    	}catch(Exception e) {
    		
    	}
    	
    	return null;
    }
    
    public static SDS2XDX evalExpression(String expr,String varStr,int errorLimit,String uncOption) {
    	return evalExpression(expr,varStr,errorLimit,3,uncOption);
    }
    public static Vector<Integer> vectorDeltaJ(String s1,String s2){
        Vector<Integer> out=new Vector<Integer>();
        
        if(s1.trim().length()==0 || s2.trim().length()==0)
            return out;

         String[] jv1=parseJPI(s1);
         String[] jv2=parseJPI(s2);
           
         //System.out.println("  in deltaJ: "+s1+"   "+jv1.length+"    "+s2+"   "+jv2.length+" "+Str.isNumeric("5/2"));
         
         float factor1=1,factor2=1;
         
         for(int i=0;i<jv1.length;i++){
           String jp1=jv1[i].replace("+","").replace("-","").trim();
           if(jp1.contains("/2")) {
               jp1=jp1.replace("/2", "").trim();
               factor1=2;
           }

           if(!Str.isNumeric(jp1))
               continue;
           
           for(int j=0;j<jv2.length;j++){
               String jp2=jv2[j].replace("+","").replace("-","").trim();
               if(jp2.contains("/2")) {
                   jp2=jp2.replace("/2", "").trim();
                   factor2=2;
               }
     
               if(!Str.isNumeric(jp2))
                   continue;
              
               if(factor1!=factor2)
                   return out;
              
              try{
                float j1=jValue(jp1);
                float j2=jValue(jp2);
                
                float minDjf=Math.abs(j1-j2)/factor1;
                int minDji=(int)(minDjf+0.001);       
                                
                if(j1<0 || j2<0 || Math.abs(minDji-minDjf)>0.001)
                    return new Vector<Integer>();
                
                float maxDjf=Math.abs(j1+j2)/factor1;
                int maxDji=(int)(maxDjf+0.001);       
                                
                if(j1<0 || j2<0 || Math.abs(maxDji-maxDjf)>0.001)
                    return new Vector<Integer>();
                
                for(int k=minDji;k<=maxDji;k++) {
                    if(!out.contains(k))
                        out.add(k);
                }

              }catch(Exception e){
                e.printStackTrace();
              }
           }
         }
         
         if(out.size()>0)
             Collections.sort(out);
         
         return out;    	
    }
    //calculate all possible differences between two JPI strings (scaler)
    public static Vector<Integer> deltaJ(String s1,String s2){
        Vector<Integer> out=new Vector<Integer>();
        
        if(s1.trim().length()==0 || s2.trim().length()==0)
            return out;

         String[] jv1=parseJPI(s1);
         String[] jv2=parseJPI(s2);
           
         //System.out.println("  in deltaJ: "+s1+"   "+jv1.length+"    "+s2+"   "+jv2.length+" "+Str.isNumeric("5/2"));
         
         float factor1=1,factor2=1;
         
         for(int i=0;i<jv1.length;i++){
           String jp1=jv1[i].replace("+","").replace("-","").trim();
           if(jp1.contains("/2")) {
               jp1=jp1.replace("/2", "").trim();
               factor1=2;
           }

           if(!Str.isNumeric(jp1))
               continue;
           
           for(int j=0;j<jv2.length;j++){
               String jp2=jv2[j].replace("+","").replace("-","").trim();
               if(jp2.contains("/2")) {
                   jp2=jp2.replace("/2", "").trim();
                   factor2=2;
               }
     
               if(!Str.isNumeric(jp2))
                   continue;
              
               if(factor1!=factor2)
                   return out;
              
              try{
                float j1=jValue(jp1);
                float j2=jValue(jp2);
                float djf=Math.abs(j1-j2)/factor1;
                int dji=(int)(djf+0.001);       
                                
                if(j1<0 || j2<0 || Math.abs(dji-djf)>0.001)
                    return new Vector<Integer>();
                
                if(!out.contains(dji))
                    out.add(dji);
              }catch(Exception e){
                e.printStackTrace();
              }
           }
         }
         
         if(out.size()>0)
             Collections.sort(out);
         
         return out;
    }
    
    //check if two levels has overlapping JPI values
    //if either JPI is empty, return true 
    public static boolean isOverlapJPI(String s1,String s2){
         if(s1.trim().length()==0 || s2.trim().length()==0)
            return true;

         //s1=1/2, s2=11/2
         if(s1.contains(s2) || s2.contains(s1)) {
        	 String ss1=s1;
        	 String ss2=s2;
        	 if(s2.contains(s1)) {
        		 ss1=s2;
        		 ss2=s1;
        	 }
        	 
        	 int n=ss1.indexOf(ss2);
        	 while(n>=0) {
        		 char c1=' ',c2=' ';
        		 if(n>0)
        			 c1=ss1.charAt(n-1);
        		 
        		 n+=ss2.length();
        		 if(n<ss1.length())
        			 c2=ss1.charAt(n);
        		 
        		 if(!Character.isDigit(c1)&&Character.isDigit(c2)) {
        			 return true;
        		 }
        		 
        		 n=ss1.indexOf(ss2,n);
        	 }       		 
         }
         
         s1=s1.toUpperCase().replace("J1","J").replace("J2","J").replace("J3","J");
         s2=s2.toUpperCase().replace("J1","J").replace("J2","J").replace("J3","J");
         if(s1.contains("J+") && s2.contains("J+") && s1.equals(s2))
             return true;
         
         String[] jv1=parseJPI(s1);
         String[] jv2=parseJPI(s2);
           
         for(int i=0;i<jv1.length;i++){
           String jp1=jv1[i];
           for(int j=0;j<jv2.length;j++){
              String jp2=jv2[j];
              if(jp1.equals(jp2))
                 return true;

              String temp1=jp1;
              String temp2=jp2;
              
              temp1=jp1.replace("+","").replace("-","").trim();
              temp2=jp2.replace("+","").replace("-","").trim();


              if(temp1.isEmpty() && (jp2.contains(jp1)||!Str.containAny(jp2, "+-")))
                  return true;
              if(temp2.isEmpty() && (jp1.contains(jp2)||!Str.containAny(jp1, "+-")))
                  return true;
              
              if(temp1.isEmpty() || temp2.isEmpty())
                  continue;
              
              if(jp1.indexOf(jp2)>=0 || jp2.indexOf(jp1)>=0){
                if(temp1.equals(temp2))
                   return true;
              }
           }
         }
         return false;   
    }
    
    //check if two levels has close JPI values (DJ<=DJLImit
    //if either JPI is empty, return true 
    public static boolean isCloseJPI(String s1,String s2,int DJLimit){
        if(s1.trim().length()==0 || s2.trim().length()==0)
            return true;

        //s1=1/2, s2=11/2
        if(s1.contains(s2) || s2.contains(s1)) {
          	 String ss1=s1;
           	 String ss2=s2;
           	 if(s2.contains(s1)) {
           		 ss1=s2;
           		 ss2=s1;
           	 }
           	 
           	 int n=ss1.indexOf(ss2);
           	 while(n>=0) {
           		 char c1=' ',c2=' ';
           		 if(n>0)
           			 c1=ss1.charAt(n-1);
           		 
           		 n+=ss2.length();
           		 if(n<ss1.length())
           			 c2=ss1.charAt(n);
           		 
           		 if(!Character.isDigit(c1)&&!Character.isDigit(c2)) {
           			 return true;
           		 }
           		 
           		 n=ss1.indexOf(ss2,n);
           	 }       		 
        }
        
         String[] jv1=parseJPI(s1);
         String[] jv2=parseJPI(s2);
           
         s1=s1.toUpperCase().replace("J1","J").replace("J2","J").replace("J3","J");
         s2=s2.toUpperCase().replace("J1","J").replace("J2","J").replace("J3","J");
         if(s1.contains("J+") && s2.contains("J+") && s1.equals(s2))
             return true;
         
        //System.out.println("  in isClosEJPI: "+s1+"   "+jv1.length+"    "+s2+"   "+jv2.length);
        //if(s1.contains("3-")&&s2.contains("1+,2+"))
        //  System.out.println("#####s1="+s1+" s2="+s2);

         for(int i=0;i<jv1.length;i++){
           String jp1=jv1[i];
           for(int j=0;j<jv2.length;j++){
              String jp2=jv2[j];
              if(jp1.equals(jp2))
                 return true;
              
              String temp1=jp1;
              String temp2=jp2;
              
              temp1=jp1.replace("+","").replace("-","").trim();
              temp2=jp2.replace("+","").replace("-","").trim();

              //System.out.println(" temp1="+temp1+" temp2="+temp2+" jp1="+jp1+" jp2="+jp2);
              
              if(temp1.isEmpty() && (jp2.contains(jp1)||!Str.containAny(jp2, "+-")))
                  return true;
              if(temp2.isEmpty() && (jp1.contains(jp2)||!Str.containAny(jp1, "+-")))
                  return true;
              
              if(temp1.isEmpty() || temp2.isEmpty())
                  continue;
              
              try{
                float j1=jValue(temp1);
                float j2=jValue(temp2);
                
                
                boolean samePI=true;
                if(jp1.contains("+")&&jp2.contains("-"))
                    samePI=false;
                else if(jp1.contains("-")&&jp2.contains("+"))
                    samePI=false;
                     
                //debug
                //if(s1.contains("3-")&&s2.contains("1+,2+"))
                //      System.out.println(" In EnsdfUtil 3152  i="+i+" j="+j+"  j1s="+jp1+" j1="+j1+" j2s="+jp2+" j2="+j2+" DJ Limit="+DJLimit+" temp1="+temp1+" temp2="+temp2+" samePI="+samePI);

                
                if(samePI && j1>=0 && j2>=0 && Math.abs(j1-j2)<DJLimit+0.01)
                    return true;
                
              }catch(Exception e){
                e.printStackTrace();
              }
           }
         }
         
         return false;   
    }
    
    /*
     * check if str is for a valid JPI value
     */
    public static boolean isJPIStr(String str) {
        str=str.trim();
        if(str.isEmpty())
            return true;
        
        String[] JPV=parseJPI(str);
        for(int i=0;i<JPV.length;i++) {
            String JPS=JPV[i].trim();
            if(JPS.isEmpty())
                continue;
            
            float jp=jValue(JPS);
            if(jp<0 || jp>100)
                return false;
        }
        
        return true;
    }
    
    
    public static float j(Level lev){
        return jValue(lev.JPiS());     
    }
    
    /*
     * return the numerical value (always>=0 if valid) of a SPIN
     */
    public static float jValue(String JS){
        try{
            JS=JS.replace("+","").replace("-","").replace("(", "").replace(")","").trim();
            
            int n=JS.indexOf("/");
            float jvalue=-1000;
            if(n>0){
                jvalue=Float.parseFloat(JS.substring(0,n).trim())/2;
                JS=JS.substring(n+1);
                if(!JS.equals("2"))
                    jvalue=-1000;
            }else if(n<0){
                jvalue=Integer.parseInt(JS);
            }
            
            return jvalue;
        }catch(Exception e){    
        }
        
        return -1000;
    }
    
    public static float spinValue(String js){   
        String s=js.replace("(","").replace(")","").replace("+","").replace("-","").trim();
        if(Str.isInteger(s))
            return Integer.parseInt(s);
        
        s=s.replace("/2","").trim();
        if(Str.isInteger(s))
            return Integer.parseInt(s)/2.0f;
        
        return -1000;
    }
    
    public static Vector<String> findJPIsFromL(String jpis,int L){
    	jpis=jpis.replace("(", "").replace(")","").trim();
    	String[] ja=jpis.split("[,\\s]+");
    	
    	Vector<String> out=new Vector<String>();
    	for(int i=0;i<ja.length;i++) {
        	JPI jpi=new JPI(ja[i]);
        	Vector<String> temp=findJPIsFromL(jpi,L);
        	for(String s:temp) {
        		if(!out.contains(s))
        			out.add(s);
        	}
    	}

    	simplifyJSV(out);
    	sortJSV(out);
    	
    	return out;
    	
    }
    
    /*
     * find all possible JPIs of a vector sum of a unique js and L (L could be L-transfer or photon angular momentum)
     */
    public static Vector<String> findJPIsFromL(JPI jpi,int L) {
        Vector<String> allowedJSV=new Vector<String>();
      
        try {
                        
            if(L<0 || jpi.spin().isEmpty() || !jpi.isUniqueSpin())
                return allowedJSV;
      
            
            String targetSpin=jpi.spin();
            int targetParityCode=jpi.parityCode();
            
            int thisParityCode=-1;//+1 for positive parity, -1 for negative, 0 for unknown

            int JT=-1;

            //final J=JT (vector+) L (vector+)
            
            if(targetSpin.equals("0")) {
                allowedJSV.add(L+"");
            }else {
                
                //System.out.println(" id="+id.dsid+"  targetJ="+targetSpin+" transferJ="+transferJS+" L="+L);
                
                try {
                    if(targetSpin.contains("/2"))
                        JT=Integer.parseInt(targetSpin.replace("/2", ""));
                    else
                        JT=Integer.parseInt(targetSpin)*2;
                       
                    int maxJF=JT+2*L;
                    int minJF=Math.abs(JT-2*L);
                    
                    if(maxJF%2==0 && minJF%2==0) {
                        minJF=minJF/2;
                        maxJF=maxJF/2;
                        for(int i=minJF;i<=maxJF;i++)
                            allowedJSV.add(i+"");
                    }else if(maxJF%2==1 && minJF%2==1){
                        for(int i=minJF;i<=maxJF;i++) {
                            if(i%2==1)
                                allowedJSV.add(i+"/2");
                        }
                    }
                }catch(Exception e) {
                    
                }
            }
            
            
            //parity
            thisParityCode=targetParityCode*(1-2*(L%2));    
            String parity="";
            if(thisParityCode==1)
                parity="+";
            else if(thisParityCode==-1)
                parity="-";
            
            if(!parity.isEmpty()) {
                for(int i=0;i<allowedJSV.size();i++) {
                    String js=allowedJSV.get(i)+parity;
                    allowedJSV.remove(i);
                    allowedJSV.add(i, js);
                }                    
            }

        }catch(Exception e) {}
        
        return allowedJSV;
    }
    

	
	public static void sortJSV(Vector<String> JSV) {
		JPI.sortJSV(JSV);
	}
	
	/*
	 * merge j+ and j- to J 
	 * insert j and remove j+ and j-
	 */
	public static void simplifyJSV(Vector<String> JSV) {
		JPI.simplifyJSV(JSV);
	}
	
	public static void cleanJSV(Vector<String> JSV) {
		JPI.simplifyJSV(JSV);
	}
    /*
     * find JPIS from target JPI and L-transfers for all levels in a transfer dataset
     */
    public static boolean parseJPIsFromLTransfer_old(ENSDF ens) {
        
        if(ens==null || ens.nLevels()==0)
            return false;
        
        TransferInfo tr=new TransferInfo(ens);
        
        //System.out.println("dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+" spin="+tr.targetSpin()+" isEvenEven="+ens.target().isEvenEven()+" isTransfer="+tr.isTransfer());
        
        if(tr!=null && tr.isTransfer()) {
            Vector<String> jpV=null;
            for(Level lev:ens.levelsV()) {
                jpV=tr.findLevelJPSFromLtransfer(lev);
                String jps="";
                for(String s:jpV)
                    jps+=s+",";
                
                if(jps.endsWith(","))
                    jps=jps.substring(0,jps.length()-1);
                
                //System.out.println("dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+" spin="+tr.targetSpin()+" isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+jps);
                
                lev.setAltJPiS(jps.trim());
            }

        }
        
        return tr.isTransfer();

    }
   
   public static boolean parseJPIsFromLTransfer(ENSDF ens) {
        
        if(ens==null || ens.nLevels()==0)
            return false;
        
        SpinParityParser jpiParser=new SpinParityParser(ens);
        jpiParser.parseJPIsFromLTransfers();
        
        //System.out.println("EnsdfUtil 7014: DSID="+ens.DSId()+" isTransfer="+jpiParser.isTransfer());
        
        return jpiParser.isTransfer();
        
    }
   
    /*
     * find JPIS from de-excitation and feeding gammas for all levels
     */
    public static boolean parseJPIsFromGammas(ENSDF ens) {
        if(ens==null || ens.nLevels()==0 || ens.nGamWFL()==0)
            return false;
      
        
        //System.out.println("dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+" spin="+tr.targetSpin()+" isEvenEven="+ens.target().isEvenEven()+" isTransfer="+tr.isTransfer());
        
        SpinParityParser jpiParser=new SpinParityParser(ens);
        return jpiParser.parseJPIsFromGammas();     
    }    
    
    public static boolean parseJPIsFromDeays(ENSDF ens) {
        if(ens==null || ens.nLevels()==0 || ens.decayRecordType().isEmpty())
            return false;
      
        
        //System.out.println("dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+" spin="+tr.targetSpin()+" isEvenEven="+ens.target().isEvenEven()+" isTransfer="+tr.isTransfer());
        
        SpinParityParser jpiParser=new SpinParityParser(ens);
        return jpiParser.parseJPIsFromDecays();     
    }
    
    public static String[] parseExactJPI(String js){
    	return JPI.parseJPS2Array(js);
    }
    
    public static boolean isEqualJPS(String js1,String js2) {
        return JPI.isEqualJPS(js1,js2);
    }
    
    public static <T extends Record> boolean isEqualES(T r1,T r2) {
    	try {
    	    String s1=r1.ES();
    	    String ds1=r1.DES();
    	    
            String s2=r2.ES();
            String ds2=r2.DES();
            
    		return isEqualSDS(s1,ds1,s2,ds2);
    	}catch(Exception e) {
    		
    	}
    	return false;
    	
    }
    
    public static boolean isEqualLevelES(Level lev1,Level lev2) {
        try {
            String s1=lev1.ES();
            String ds1=lev1.DES();
            
            String s2=lev2.ES();
            String ds2=lev2.DES();
            
            if(lev1.NNES().equals(lev2.NNES()) && lev1.NNES().length()>0) {
                float e1=s2f(lev1.NES());
                float e2=s2f(lev2.NES());
                if(e1==e2)
                    return true;
                else 
                    return false;
            }
            
            return isEqualSDS(s1,ds1,s2,ds2);
        }catch(Exception e) {
            
        }
        return false;
        
    }
    
    public static boolean isEqualHalflife(Level lev1,Level lev2) {
    	try {
        	double f1=T12UnitMultiplier(lev1.T12Unit());
        	double f2=T12UnitMultiplier(lev1.T12Unit());
        	
        	int pOffset1=(int)Math.log10(f1);
        	int pOffset2=(int)Math.log10(f2);
        	
        	String s1=lev1.T12S();
        	String ds1=lev1.DT12S();
        	
        	String s2=lev2.T12S();
        	String ds2=lev2.DT12S();
        	
        	return isEqualSDS(s1,ds1,pOffset1,s2,ds2,pOffset2);
    	}catch(Exception e) {
    		
    	}

    	return false;
    }
    public static boolean isEqualSDS(String s1,String ds1,String s2,String ds2) {
    	return isEqualSDS(s1,ds1,0,s2,ds2,0);
    }
    public static boolean isEqualSDS(String s1,String ds1,int pOffset1,String s2,String ds2,int pOffset2) {
    	boolean isEqual=false;
    	
		if(ds1.equals(ds2)) {
			if(s1.equals(s2)) {
				isEqual=true;
			}else {
				try {
					float e1=Float.parseFloat(s1);
					float e2=Float.parseFloat(s2);
					if(e1==0 && e2==0 && ds1.isEmpty())
						isEqual=true;
					else{
						
					    //9.35E3 30, and 935E1 30
	    				int n1=s1.indexOf("E");
	    				int n2=s2.indexOf("E");
	    				int p1=0,p2=0;
	    				if(n1<0)
	    					n1=s1.length();
	    				else {
	    					p1=Integer.parseInt(s1.substring(n1+1));
	    					s1=s1.substring(0,n1);
	    				}
	    				
	    				if(n2<0)
	    					n2=s2.length();
	    				else {
	    					p2=Integer.parseInt(s2.substring(n2+1));
	    					s2=s2.substring(0,n2);
	    				}
	    				
	    				int d1=s1.length(),d2=s2.length();//default position of decimal point for integer
	    				if(s1.contains("."))
	    					d1=s1.indexOf(".");
	    				if(s2.contains("."))
	    					d2=s2.indexOf(".");
	    				
	    				s1=s1.replace(".", "");
	    				s2=s2.replace(".", "");
	    				
	    				p1=p1+d1+pOffset1;
	    				p2=p2+d2+pOffset2;
	    				
	    				//System.out.println(" s1="+s1+" s2="+s2+" p1="+p1+" p2="+p2);
	    				if(p1==p2) {
		    				if(s1.equals(s2))
		    					isEqual=true;
		    				else if(!Str.isNumeric(ds1)) {//9530 SY, 953E1 SY or 953.0 SY, 95.3E1 SY
		    					String tempS="";
		    					if(s1.startsWith(s2))
		    						tempS=s1.substring(s2.length());
		    					else if(s2.startsWith(s1))
		    						tempS=s2.substring(s1.length());
		    					
		    					tempS=tempS.replace("0", "");
		    					if(tempS.isEmpty())
		    						isEqual=true;
		    				}
	    				}

				    }
				}catch(Exception e) {
					
				}

			    	
			}
		}
		
		return isEqual;
	}

	public static boolean isTentativeSpin(String js) {
        boolean isTentativeJ=false;
        String s=js.replace("+","").replace("-","").replace("()","");
        
        if(s.contains("(") || js.contains("["))
            isTentativeJ=true;

        
        return isTentativeJ;
	}
	
	public static boolean isTentativeParity(String js) {
        if(js.contains("+")==js.contains("-") || js.endsWith(")") || js.contains("["))
        	return true;
        
        String sign="+";
        if(js.contains("-"))
        	sign="-";
        
        String[] jpA=EnsdfUtil.parseJPI(js);
        for(int i=0;i<jpA.length;i++) {
        	String s=jpA[i];
        	if(!s.endsWith(sign)) {
        		return true;
        	}
        }

        return false;
	}
	
	public static boolean isUniqueSpin(String js) {
		try {
		   	js=js.replace("(", "").replace(")","").trim();
	    	js=js.replace("+", "").trim();
	    	js=js.replace("-", "").trim();
	    	
		    if(js.endsWith("/2")) {
		        String tempJS=js.replace("/2", "").trim();
		        if(Str.isInteger(tempJS)) {
		            int tempJ=Integer.parseInt(tempJS);
		            if(tempJ%2==1) {
		                return true;
		            }
		        }
		    }else if(Str.isInteger(js)) {
		        return true;
		    }
		}catch(Exception e) {
			
		}
		
		return false;
 
	}
	
	public static boolean isUniqueParity(String js) {
		try {
		   	js=js.replace("(", "").replace(")","").trim();
	        String[] jpA=EnsdfUtil.parseJPI(js);
	        if(jpA.length==1) {
	        	if(jpA[0].endsWith("+") || jpA[0].endsWith("-"))
	        		return true;
	        }else if(jpA.length>1) {
	        	String s=jpA[0];
	        	if(jpA[0].endsWith("+"))
	        		s="+";
	        	else if(jpA[0].endsWith("-"))
	        		s="-";
	        	else 
	        		return false;
	        	
		        for(int i=1;i<jpA.length;i++) {
		        	if(!jpA[i].endsWith(s)) {
		        		return false;
		        	}
		        }
	        	
		        return true;
	        }

		}catch(Exception e) {
			
		}
		
		return false;
 
	}
	
    public static String[] parseJPI(String js){
        String[] jv=new String[0];
        String s=js.trim();
        if(s.isEmpty())
           return jv;

        boolean defaultSplit=false;

        s=s.replace("&", ",");
        
        String s0=s;
        if(s.startsWith("("))
            s0=s.substring(1).trim();
        
        String limitTypes="LT,LE,GT,GE,AP";
        String limitType="";
        if(s0.length()>=2) {
            limitType=s0.substring(0,2).replace(",","").toUpperCase();
            char c=limitType.charAt(0);
            if(!Str.isLetter(c) && !Str.isDigit(c)) {
                if(limitType.equals("|>")) {
                    limitType="GE";
                    s=s.replace("|>", "GE");
                }else if(limitType.equals("|<")) {
                    limitType="LE";
                    s=s.replace("|<", "LE");
                }else if (limitType.equals("|?")) {
                    limitType="AP";
                    s=s.replace("|?", "AP");
                }else if(c=='<') {
                    limitType="LT";
                    s=s.replace("<", "LT");
                }else if(c=='>') {
                    limitType="GT";
                    s=s.replace(">", "GT");
                }
            }

        }
        
        
        if(limitType.length()==2 && limitTypes.contains(limitType)) {
            int n=s.indexOf(limitType);
            int n1=s.indexOf("(");
            int n2=s.indexOf(")");
            String prefix="",postfix="";
            if(n1>=0){
                if(n1<n)
                    prefix=s.substring(0,n1+1);
                else if(n1>n+2) {
                    postfix=s.substring(n1);
                    n2=n1;
                    n1=-1;
                }
            }else
                n1=-1;
            
            if(postfix.isEmpty()) {
                if(n1>=0 && n2>0 && n2>n+2)
                    postfix=s.substring(n2);
                else
                    n2=s.length();
            }

            
            String s1=s.substring(n1+1,n2);//like LE4+
                      
            try {
                String s2=s1.substring(2).trim();//eg, s2=4+ from s1=LE4+
                String parity="";
                if(s2.endsWith("+") || s2.endsWith("-")) {//"LE4+"="0,1,2,3,4+"
                    parity=s2.charAt(s2.length()-1)+"";
                    s2=s2.substring(0,s2.length()-1);//eg, s2=4 from s1=LE4+
                }
                
                //System.out.println("  s1="+s1+" s2="+s2+" parity="+parity);
                
                int jp=-1;
                int maxJ=-1,minJ=-1;
                int step=1;
                if(Str.isInteger(s2)) {
                    minJ=0;
                    step=1;
                    jp=Integer.parseInt(s2);
                }else if(s2.endsWith("/2")) {
                    minJ=1;
                    step=2;
                    jp=Integer.parseInt(s2.substring(0,s2.length()-2));
                }

                if(limitType.equals("LE")) {                      
                    maxJ=jp;
                }else if(limitType.equals("LT")) {
                    maxJ=jp-step;
                    parity="";
                }else if(limitType.equals("GE")) {
                    minJ=jp;
                    maxJ=jp+4*step;
                }else if(limitType.equals("GT")) {
                    minJ=jp+step;
                    maxJ=jp+5*step;
                    parity="";
                }else {//AP
                    if(jp>step)
                        minJ=jp-step;
 
                    maxJ=jp+step;
                    parity="";
                }
                
                //if(s.startsWith("GE")) System.out.println(" s="+s+" s0="+s0+"  limitType="+limitType+" minJ="+minJ+" maxJ="+maxJ);
                
                s2="";
                String extra="";
                if(step==2)
                    extra="/2";
                
                for(int j=minJ;j<maxJ;j=j+step)
                    s2+=""+j+extra+",";
                
                if(maxJ>=0)
                    s2+=maxJ+extra+parity;
                
                if(s2.length()>0) 
                    s=prefix+s2+postfix;

                //if(s0.startsWith("GE")) System.out.println(" s1="+s1+" s2="+s2+" s="+s+" prefix="+prefix+" postfix="+postfix);
                
            }catch(Exception e) {
            	//System.out.println("EnsdfUtil: 5625: s="+s+" s1="+s1);
            	
                e.printStackTrace();
            }
        }
        
        if(s.indexOf(":")>0)
           jv=s.split(":");
        else
           jv=s.toUpperCase().split("TO");
        
        
       
        if(jv.length==1) {
            jv=s.split(",");
        }else if(jv.length==2){
            String s1=jv[0].replace("(","").replace(")","").replace("+","").replace("-","").trim();
            String s2=jv[1].replace("(","").replace(")","").replace("+","").replace("-","").trim();
              
            if(Str.isInteger(s1) && Str.isInteger(s2)){
               int j1=Integer.parseInt(s1);
               int j2=Integer.parseInt(s2);

               s=jv[0]+',';
               for(int i=j1+1;i<j2;i++)
                 s+=String.valueOf(i)+',';

               s+=jv[1];

               jv=s.split(",");
            }else if(s1.indexOf("/2")>0 && s2.indexOf("/2")>0){
               s1=s1.replace("/2","").trim();
               s2=s2.replace("/2","").trim();
               
               if(Str.isInteger(s1) && Str.isInteger(s2)){
                   int j1=Integer.parseInt(s1);
                   int j2=Integer.parseInt(s2);

                  s=jv[0]+',';
                  for(int i=j1+2;i<j2;i=i+2)
                    s+=String.valueOf(i)+"/2,";

                  s+=jv[1];
                  jv=s.split(",");  
               }else
                  defaultSplit=true;       
            }else
               defaultSplit=true;
         }else
            defaultSplit=true;

         if(defaultSplit){
            //s=s.replace("(","").replace(")","").replace(',',' ').replace("+","").replace("-","");
            jv=s.split(" ");
         }
         
         boolean needPI=false;
         int start=0;

         //for(int i=0;i<jv.length;i++) System.out.println("  i="+i+"  j="+jv[i]);
         
         boolean[] isEmpty=new boolean[jv.length];
         
         for(int i=0;i<jv.length;i++){
           s=jv[i];
           jv[i]=jv[i].replace("(","").replace(")","").trim();
           jv[i]=jv[i].replace("[", "").replace("]","").trim();
           
           isEmpty[i]=false;
           if(jv[i].isEmpty()) {
               isEmpty[i]=true;
               continue;
           }
           
           int nlb=Str.countPattern(s, "(");
           int nrb=Str.countPattern(s, ")");
           
           if(s.indexOf("(")==0 && nlb>nrb){
              if(s.indexOf("+")<0 && s.indexOf("-")<0){
                 needPI=true;
                 start=i;
              }
           }else if(s.indexOf(")")>0 && nrb>nlb){  
              char c=' ';
              if(jv[i].length()>0) {
                  int len=jv[i].length();
                  c=jv[i].charAt(len-1);
              }
              
               if(needPI && (c=='+'||c=='-')){
                   if(s.indexOf(")+")>0 || s.indexOf(")-")>0 || s.indexOf(")(+")>0 || s.indexOf(")(-")>0)
                      for(int j=start;j<i;j++)
                         jv[j]+=c;
               }
              
              needPI=false;
           }

           //System.out.println("## s="+s+" jv[i]="+jv[i]);

         }
   
         Vector<String> tempV=new Vector<String>();
         for(int i=0;i<jv.length;i++){
             if(!isEmpty[i])
                 tempV.add(jv[i]);
         }

         jv=new String[tempV.size()];
         tempV.toArray(jv);

         return jv;
    }

    public static String cleanMUL(String ms) {
    	try {
            String s=ms.trim().toUpperCase();
            if(s.isEmpty())
               return s;

            s.replace("&", ",");
            s=s.replace("(","").replace(")","").trim();
            s=s.replace("[","").replace("]","").trim();
            s=s.replace("E2+M1","M1+E2").replace("M2+E1","E1+M2").replace("M3+E2","E2+M3").replace("E3+M2", "M2+E3");
            s=s.replace("Q+D", "D+Q").replace("O+Q", "Q+O");
            
            s=s.replace("E2,M1", "M1,E2").replace("E3,M2", "M2,E3").replace("M2,E1", "E1,M2");
            s=s.replace("Q,D", "D,Q").replace("O,Q", "Q,O");
            
            return s;
    	}catch(Exception e) {
    		
    	}

    	return "";
    }
    
    public static String[] parseMUL(String ms){
        String[] mv=new String[0];
        try {
    		String s=ms.toUpperCase().trim();
            if(s.isEmpty())
               return mv;

            s=cleanMUL(s);
            
            mv=s.split("[,/+]+");

            Vector<String> tempV=new Vector<String>();
            for(int i=0;i<mv.length;i++){
            	if(mv[i].trim().length()>0)
            		tempV.add(mv[i].trim());
            }
            
            if(tempV.size()==0)
            	return new String[0];
            
            mv=new String[tempV.size()];
            tempV.toArray(mv);
            
            int[] numbers=new int[mv.length];
            
            for(int i=0;i<tempV.size();i++){
            	numbers[i]=100;
            	if(mv[i].trim().length()>0){
            		s=mv[i].trim();
            		if(s.equals("D"))
            			numbers[i]=1;
            		else if(s.equals("Q"))
            			numbers[i]=2;
            		else if(s.equals("O"))
            			numbers[i]=3;
            		else{
            			try{
            				int len=s.length();
            				numbers[i]=Integer.parseInt(s.substring(len-1));
            			}catch(NumberFormatException e){}
            		}
            	}
            }
            
    		
            //debug
    		//for(int i=0;i<mv.length;i++)
    		//	System.out.println(" @ i="+i+" "+mv[i]+" size="+mv.length+" ms="+ms);
    		        
            for(int i=0;i<mv.length-1;i++){
            	for(int j=i+1;j<mv.length;j++){
            		if(numbers[i]>numbers[j]){
            			int tempNum=numbers[i];
            			numbers[i]=numbers[j];
            			numbers[j]=tempNum;
            			
            			String tempS=mv[i];
            			mv[i]=mv[j];
            			mv[j]=tempS;
            		}
            	}     	    	
            }
            
            //debug
    		//for(int i=0;i<mv.length;i++)
    		//	System.out.println(" # i="+i+" "+mv[i]+" size="+mv.length+" "+tempV.size());
        }catch(Exception e) {
        	
        }
        
        return mv; 
    }

	public static String[] expandEMs(String[] EMs,String MUL) {
		String[] mv=new String[0];
        String ms=MUL;
        
		try {
			ms=cleanMUL(ms);
			
			Vector<String> tempV=new Vector<String>();
			for(int j=0;j<EMs.length;j++){
				String EM=EMs[j];
				
				if(EM.equals("D")) {
					if(!tempV.contains("E1")) {
						int n=tempV.indexOf("M1");
						if(n<0) {
							n=tempV.indexOf("E2");
							if(n<0)
								n=0;
						}

						tempV.add(n, "E1");
					}
					if(!tempV.contains("M1")) {
						int n=tempV.indexOf("E1");
						if(n<0) {
							n=tempV.indexOf("E2");
							if(n<0)
								n=0;
						}else {
							n=n+1;
						}

						tempV.add(n, "M1");
					}
				}else if(EM.equals("Q")) {
					tempV.add("E2");
				}else if(EM.equals("O")) {
					tempV.add("M3");
				}else {
					tempV.add(EM);
				}
			}
			
			if(tempV.size()>0) {
				mv=new String[tempV.size()];
		    	tempV.toArray(mv); 
			}

		}catch(Exception e) {
			
		}
		
		return mv;
	}
	
	//extract all NUCID from a string expression
	//for example: (1.2*35Cl+1H)/(35CL+3He)
	public static List<String> extractNuclides(String expression) {
        List<String> nuclides = new ArrayList<String>();

        if (expression == null || expression.trim().isEmpty()) {
            return nuclides;
        }

        // Regex explanation:
        // (?<![\d.])            : Prevents matching decimals (e.g., skips "2" in "1.2")
        // ([12]?[0-9]{1,2}|300) : Matches mass number A from 1 to 300
        // ([A-Za-z]{1,2})       : Matches element symbol En (1 or 2 letters, case-insensitive)
        // \b                    : Word boundary
        String regex = "(?<![\\d.])([12]?[0-9]{1,2}|300)([A-Za-z]{1,2})\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String matchedNuclide = matcher.group(0);
            if (!nuclides.contains(matchedNuclide)) {
                nuclides.add(matchedNuclide);
            }
        }

        return nuclides;
    }
	
	public static boolean isGoodEMComponent(String singlePol){
		String[] MS={"E","M"};
		String s=singlePol.toUpperCase().trim();
		if(s.length()!=2)
			return false;
		
		for(int i=0;i<MS.length;i++){
			for(int j=1;j<=10;j++){
				if(s.equals(MS[i]+j))
					return true;
			}
		}
		
		return false;
	}
	
	/*
	 * find L of experimental multipolarity
	 */
	public static Vector<String> findGammaPoleExp(String MUL) {
		Vector<String> out=new Vector<String>();

		
		if(MUL.isEmpty() || MUL.contains("["))
			return out;

	    String[] multA=EnsdfUtil.parseMUL(MUL);//all parentheses have been removed and all Mults are in order
	    String s="",parity="",lbracket="",rbracket="";
	    if(multA.length>0) {

	    	if(multA.length==1) {
	    		if(MUL.startsWith("(")) {
	    			lbracket="(";
	    			rbracket=")";
	    		}
	    			
	    		s=multA[0];
	    		int L=-1;
	    		if(s.equals("D")) {
	    			out.add(lbracket+"0"+rbracket);
	    			out.add(lbracket+"1"+rbracket);
	    		}else if(s.equals("Q")) {
	    			out.add(lbracket+"2"+rbracket);
	    		}else if(s.equals("O")) {
	    			out.add(lbracket+"3"+rbracket);
	    		}else if(s.startsWith("M")) {
	    			s=s.substring(1);
	    			if(Str.isInteger(s)) {
	    				L=Integer.parseInt(s);
	    				if(L%2==0)
	    					parity="-";//even L for M changes parity
	    				else
	    					parity="+";//odd L for M does not change parity
	    				
	    				int n=1;
	    				int offset=0;
	    				if(L==1) {
	    					n=2;
	    					offset=-1;
	    				}
	    				
	    				for(int i=0;i<n;i++) {
	    					s=L+offset+i+parity;
		    				s=lbracket+s+rbracket;
			    				
		    				out.add(s);
	    				}

	    			}
	    		}else if(s.startsWith("E")) {
	    			s=s.substring(1);
	    			if(Str.isInteger(s)) {
	    				L=Integer.parseInt(s);
	    				if(L%2==1)
	    					parity="-";//odd L for E changes parity
	    				else
	    					parity="+";//even L for M does not change parity
	    				
	    				int n=1;
	    				int offset=0;
	    				if(L==1) {
	    					n=2;
	    					offset=-1;
	    				}
	    				
	    				for(int i=0;i<n;i++) {
	    					s=L+offset+i+parity;
		    				s=lbracket+s+rbracket;
			    				
		    				out.add(s);
	    				}
	    			}
	    		}
	    				
	    	}else {
	    		s=EnsdfUtil.cleanMUL(MUL);
	    		String mult=multA[0];
	    		int p=MUL.indexOf("(");
	    		if(p>=0 && p<MUL.indexOf(mult) && !mult.contains("("))
	    			mult="("+mult+")";
	    		
	    		out=findGammaPoleExp(mult);
	    		for(int i=1;i<multA.length;i++) {
	    			mult=multA[i];
	    			int n=s.indexOf(mult);
	    			if(n>0) {
	    				char c=s.charAt(n-1);
	    				if(c=='+')//eg, Mult=M1+E2, M1 has been processed with L=1+, and no need to process E2
	    					continue;
	    				
	    				if(c==',') {//eg, Mult=M1,E2, meaning it could be pure E2 (L=2), need to process E2
	    					int nlb=MUL.indexOf("(");
	    					int nrb=MUL.indexOf(")");
	    					if(nlb>=0 && n<nrb && !mult.contains("("))
	    						mult="("+mult+")";
	    					
	    					out.addAll(findGammaPoleExp(mult));
	    				}
	    			}
	    		}
	    	}
	    }
	    
	    return out;
	}
	
    public static int findIndexOfName(String line,String name) {
        int n=line.indexOf(name);
        int len=name.length();
        //check to make sure it is not like, name=x, line="ax=1.0"
        while(n>0) {
            char c=line.charAt(n-1);
            if(Character.isLetterOrDigit(c) || c=='|')
                n=line.indexOf(name,n+len);
            else
                break;
        }
        

        return n;
    }
    
   //find offset with the given name from a string line
    //[0]=s
    //[1]=ds
    //[2]="R": for range: with [0]=lower [1]=upper
   //not able to parse offset range
  public static String[] findOffsetInLine(String line,String name) {
	  
	    String[] out=new String[3];
	    out[0]="";
	    out[1]="";
	    out[2]="";
	    
        int n=-1;
        String s="",ds="";
        boolean isRange=false;
        
        int len=name.length();
        int offset=1;
        line=line.trim();
        
        if(name.length()==1 && Str.isLetters(name)) {
        	name=name.toUpperCase();
        	line=line.toUpperCase();
        }
        
        ArrayList<String> list=new ArrayList<String>(Arrays.asList("=","|?",">","|>","<","|<"));
        for(String op:list) {
            do {
                line=line.replace(" "+op,op);
            }while(line.indexOf(" "+op)>0);
        }

        String opSymbol="";
        for(String op:list) {
            n=findIndexOfName(line,name+op);
            if(n>=0) {
            	offset=op.length();
            	opSymbol=op;
            	break;
            }
        }

        
        String tempLine="";
        if(n>=0) {
            tempLine=line.substring(n+len+offset);                                                                                       
        }else if((n=findIndexOfName(line,name+"("))>=0) {
            
        	int n1=-1;
            for(String op:list) {
            	offset=2;
                n1=line.indexOf(")"+op,n);
                if(n1>=0) {                 
                    offset=op.length()+1;
                    opSymbol=op;
                    break;
                }
            }
            
            if(n1>0) {
                String nuc=line.substring(n+len+1,n1).trim().toUpperCase();
                nuc=nuc.replace("{", "").replace("}", "").replace("I", "").trim();
                //if(line0.substring(0,5).trim().equals(nuc))
                    tempLine=line.substring(n1+offset);
            }
        }
        
        if(opSymbol.contains(">") || opSymbol.contains("<"))
        	isRange=true;
        
        //System.out.println("  tempLine="+tempLine+" s="+s);
        
        if(tempLine.length()>0) {
            s=Str.extractHeadingNumber(tempLine); 
            
            //System.out.println("  tempLine="+tempLine+" s="+s);
            
            float factor=-1;
            
            //check unit, keV by default
            if(s.length()>0) {
            	float v=Float.parseFloat(s);
            	
                tempLine=tempLine.replace("{","").replace("}","").replace("(","").replace(")","").trim();
                n=s.length();

                String tempS=tempLine.substring(n).trim().toUpperCase();
                try {

                    //System.out.println(" ENSDF 4417: s="+s+" tempS="+tempS);

                    if(tempS.startsWith("EV")) { 
                        factor=1000;
                        tempS=tempS.substring(2).trim();
                    }else if(tempS.startsWith("MEV")) {
                        factor=0.001f;
                        tempS=tempS.substring(3).trim();
                    }else if(tempS.startsWith("KEV")) {
                    	factor=1.0f;
                    	tempS=tempS.substring(3).trim();
                    }

                    if(factor>0) {
                        v=v/factor;
                        //s=String.valueOf(v);
                    } 
                                
                }catch(Exception e) {
                    
                }               

                int n0=0;
                if(tempS.startsWith("TO")) {
                	n0=2;
                }else if(tempS.startsWith("-")) {
                	n0=1;
                }
                if(n0>0) {
                	tempS=tempS.substring(n0).trim();
                	String s1=Str.extractHeadingNumber(tempS);
                	if(s1.length()>0) {
                		float dv=Float.parseFloat(s1);
                		
                		isRange=true;
                		
                		tempS=tempS.substring(s1.length()).trim();
                        try {
                            float factor1=-1;
                            
                            //System.out.println(" ENSDF 4453: s="+s+" tempS="+tempS+" v="+v+"  v1="+v1);
                            
                            ds=s1;
                            
                            if(tempS.startsWith("EV")) { 
                                factor1=1000;
                            }else if(tempS.startsWith("MEV")) {
                                factor1=0.001f;
                            }else if(tempS.startsWith("KEV")) {
                            	factor1=1.0f;
                            }
                            
                            if(factor1>0) {
                                dv=dv/factor1;
                                ds=String.valueOf(dv);
                                
                                if(factor<0) {
                                    v=v/factor1;
                                    s=String.valueOf(v);
                                }
                            }              
                        }catch(Exception e) {
                            
                        }                 		
                		
                	}
                }else if(tempS.startsWith("I")){//30 {I10}, with {} being removed
                	tempS=tempS.substring(1).trim();
                	ds=Str.extractHeadingNumber(tempS);
                }else if(isRange) {
                	ds=opSymbol;
                }
            }
        }

        out[0]=s;
        out[1]=ds;
        
        if(isRange)
        	out[2]="R";
        
        //System.out.println("ENSDF 1068 findOffsetInLine: line="+line+" name="+name+" n="+n+" value="+s);
        
        return out;
    }
  
  
  public static XDX parseDecayModeBranching(DecayMode dm) {
  	XDX out=new XDX(0,0);
  	if(dm==null)
  		return out;
  	
  	try {
  		
  		double p=100,dpu=0,dpl=0;
  		
          String ps=dm.value();
          String dps=dm.unc();
          
          p=Double.parseDouble(ps);

          if(Str.isNumeric(dps) || (dps.contains("+")&&dps.contains("-"))){
              SDS2XDX s2x=new SDS2XDX(); 
              s2x.setValues( ps,dps);
              
              dpu=s2x.dxu();
              dpl=s2x.dxl();

          }else if(dps.length()>0){//AP,CA,GT,LT,...

              
              XDX parsedXDX=UncertaintyParser.findParsedXDX(ps,dps,dps);
              p=parsedXDX.x();
              dpu=parsedXDX.dxu();
              dpl=parsedXDX.dxl();
              
              //for GT, GE, an upper uncertainty of 10000 times of value is already assumed in findParsedXDX()
              //But for cases of T12, we need more for that limit
              if(dps.charAt(0)=='G'){
                  dpu=100-p;
              }

              //System.out.println("   t="+t+"  dtl="+dtl+"  dtu="+dtu+"   T12Multiplier="+T12Multiplier);
          }
          
          
          out=new XDX(p,dpu,dpl);
          
  	}catch(Exception e) {
  		
  	}
  	
  	return out;
  }
  
    public static void parseEoffsetInComments(Parent p) {
    	try {
        	parseEoffsetInComments(p.level(),p.commentV());
        	
        	p.setIsOffsetLimit(p.level().isOffsetLimit());
        	p.setIsOffsetRange(p.level().isOffsetRange());    		
    	}catch(Exception e) {
    		e.printStackTrace();
    	}

    }
    public static void parseEoffsetInComments(Level lev) {
    	parseEoffsetInComments(lev,lev.commentV());
    }
    
    //find offset value or offset range set in comments, and reset level energies
    //based on found offsets
    public static void parseEoffsetInComments(Level lev,Vector<Comment> commentV) {
    	try {
 	 	   //parse x value from comment if parent energy is given as 123.3+X
 	 	   String es=lev.ES().trim().toUpperCase();
 	 	   boolean isOffsetRange=false;
 	 	   boolean isOffsetLimit=false;
 	 	   
 	 	   if(!Str.isNumeric(es)) {
 	 	       String label="";
 	 	       float e=lev.EF();
 	 	       float de=lev.DEF();
 	 	       float xl=0,xu=0;
 	 	       
 	 	       if(es.length()==1 && Character.isLetter(es.charAt(0))) {
 	 	           label=es;
 	 	       }else if(es.contains("+")) {
 	 	           int n=es.indexOf("+");
 	 	           String s1=es.substring(0,n).trim();
 	 	           String s2=es.substring(n+1).trim();
 	 	           boolean isNum1=Str.isNumeric(s1);
 	 	           boolean isNum2=Str.isNumeric(s2);

 	 	           if(s1.length()>0 && s2.length()>0 && isNum1!=isNum2) {
 	 	               if(isNum1) {
 	 	                   e=Float.parseFloat(s1);
 	 	                   label=s2;
 	 	               }else {
 	 	                   e=Float.parseFloat(s2);
 	 	                   label=s1;
 	 	               }
 	 	           }
 	 	       }

 	 	       
 	 	       if(label.length()>0) {
 	 	           for(Comment c:commentV) {
 	 	               String head=c.head().trim();
 	 	               if(head.equals("E")) {
 	 	                   String s=c.body().toUpperCase();
 	 	                   String[] temp=s.split("[.,;]+");
 	 	                   s=temp[0].trim();
 	 	                   if(s.startsWith(label)) {
 	 	                       s=s.substring(1).trim();
 	 	                       s=s.replace("KEV", "").trim();
 	 	                       if(s.startsWith("=") || s.startsWith("|?")) {
 	 	                           s=s.replace("=","").replace("|?", "").trim();
 	 	                           s=s.replace("TO", " ");
 	 	                           if(s.indexOf("-")>0)
 	 	                               s=s.replace("-", " ");
 	 	                           
 	 	                           temp=s.split("[\\s]+");
 	 	                           int n=temp.length;
 	 	                           if(Str.isNumeric(temp[0]))
 	 	                               xl=Float.parseFloat(temp[0]);
 	 	                           
 	 	                           
 	 	                           if (n>1 && Str.isNumeric(temp[1])) {
 	 	                               xu=Float.parseFloat(temp[1]);
 	 	                           }else
 	 	                               xu=xl;
 	 	                           
 	 	                           float d=(xu-xl)/2;
 	 	                           if(xu+xl>0 && d>=0) {
 	 	                               e=e+(xu+xl)/2;                              
 	 	                               if(de>=0)
 	 	                                   de=(float) Math.sqrt(de*de+d*d);
 	 	                               else if(lev.DES().isEmpty())
 	 	                                   de=d;
 	 	                               
 	 	                               lev.setEF(e, de);
 	 	                               lev.setERPF(e,de);
 	 	                               
 	 	                               if(d>0)
 	 	                                   isOffsetRange=true;
 	 	                               
 	 	                               break;
 	 	                           }
 	 	                       }else if(s.startsWith(">") || s.startsWith("|>")) {
 	 	                           s=s.replace("|>", "").replace(">", "").trim();
 	 	                           temp=s.split("[\\s]+");
 	 	                           if(Str.isNumeric(temp[0])) {
 	 	                               xl=Float.parseFloat(temp[0]);
 	 	                               e=e+xl;
 	 	                               de=-1;
 	 	                               lev.setEF(e, de);
 	 	                               lev.setERPF(e,de);
 	 	                             
 	 	                               lev.setDES("GT");
 	 	                               
 	 	                               isOffsetLimit=true;
 	 	                               break;
 	 	                           }
 	 	                       }else if(s.startsWith("<") || s.startsWith("|<")) {
 	 	                           s=s.replace("|<", "").replace("<", "").trim();
 	 	                           temp=s.split("[\\s]+");
 	 	                           if(Str.isNumeric(temp[0])) {
 	 	                               xu=Float.parseFloat(temp[0]);
 	 	                               e=e+xu;
 	 	                               de=-1;
 	 	                               lev.setEF(e, de);
 	 	                               lev.setDES("LT");
 	 	                               
 	 	                               lev.setERPF(e,de);
 	 	                               
 	 	                               isOffsetLimit=true;
 	 	                               break;
 	 	                           }
 	 	                       }
 	 	                   }
 	 	               }
 	 	           }
 	 	       }
 	 	       
 	 	       
 	 	       
 	 	       lev.setIsOffsetLimit(isOffsetLimit);
 	 	       lev.setIsOffsetRange(isOffsetRange);
 	 	       //debug
 	 	       //System.out.println(" parent ES="+lev.ES()+"  "+lev.DES()+"  EF="+lev.EF()+"  "+lev.DEF());

 	 	   }      		
    	}catch(Exception e) {
    		e.printStackTrace();
    	}

    }
    /*
     * check if standard ENSDF T1/2 unit
     */
    public static boolean isHalflifeUnit(String s) {
        ArrayList<String> units=new ArrayList<String>(Arrays.asList("fs","ps","ns","us","|ms","ms","s","second","m","min","h","hour","d","day","y","year"));
        s=s.toLowerCase().trim();
        
        return units.contains(s);
    }
    
    /*
     * [0]: next unit
     * [1]: string of the factor for multiplying the original T1/2 value for the new unit
     */
    @SuppressWarnings("unused")
	public static String[] nextHalflifeUnit(String s,String option) {
    	String[] out=new String[0];
    	try {
    		
    		String nextUnit="";
    		String factorS="";
    		float factor=1;
    		
    		ArrayList<String> units=new ArrayList<String>(Arrays.asList("fs","ps","ns","us","ms","s","m","h","d","y"));
    		option=option.trim().toUpperCase();
    		String s1=s.toLowerCase();
    		
    		if(!units.contains(s1))
    			return out;
    		
    		int index=units.indexOf(s1);
    		
    		if(option.startsWith("BIG")) {
    			index=index+1;
    			if(index>=units.size())
    				return out;
    			
    			nextUnit=units.get(index).toUpperCase();
    			
        		switch(nextUnit) {
        		  case "M"://from unit=s
        			  factor=factor/60;
        		  case "H"://from M
        			  factor=factor/60;
        		  case "D"://from H
        			  factor=factor/24;
        		  case "Y"://from D
        			  factor=factor/365.24219f;//adopted at 2017 NSDD for a tropical year
        		  default:
        			  factor=factor/1000;//for second and sub  
        		}

    		}else if(option.startsWith("SMA")) {
    			index=index-1;
    			if(index<0)
    				return out;
    			
    			nextUnit=units.get(index).toUpperCase();
    			
        		switch(nextUnit) {
        		  case "S": //from unit=M
        			  factor=factor*60;
      		      case "M"://from unit=H
      			      factor=factor*60;
      		      case "H"://from D
      			      factor=factor*24;
      		      case "D"://from Y
      			      factor=factor*365.24219f;//adopted at 2017 NSDD for a tropical year
      		      default:
      			      factor=factor*1000;//for second and sub        		
        		}
    		}
    		
    		if(nextUnit.length()>0) {
    			out=new String[2];
    			out[0]=nextUnit.toLowerCase();
    			out[1]=String.valueOf(factor);
    			
    			return out;
    		}
    	}catch(Exception e) {
    		
    	}
    	
    	return out;
    }
    
    public static boolean isWidthUnit(String s) {
        ArrayList<String> units=new ArrayList<String>(Arrays.asList("|mev","mev","ev","kev","gev"));
        s=s.toLowerCase().trim();
        
        return units.contains(s);
    }
    
    public static boolean isKeynumber(String keynumber){
        
        int i;

        if(keynumber.length()!=8)
            return false;

        for(i=0;i<8;i++)
        {
            if(i<=3)
            {
                if(!Character.isDigit(keynumber.charAt(i)))
                    break;
                continue;
            }
            else if(i<=5)
            {
                if(!Character.isLetter(keynumber.charAt(i)))
                    break;
                continue;
            }
            else
            {
                if(!Character.isLetter(keynumber.charAt(i)) && !Character.isDigit(keynumber.charAt(i)))
                    break;
                continue;
            }

        }

        if(i<=7)
            return false;
        
        return true;

    }
    
    public static boolean isKeynumbers(String keynumbers) {
    	try {
    		String[] temp=keynumbers.split("[,:;\\s]+");
    		for(int i=0;i<temp.length;i++) {
    			if(!isKeynumber(temp[i])) {
    				return false;
    			}
    		}
    		
    		return true;
    	}catch(Exception e) {
    		
    	}
    	
    	return false;
    }
    public static boolean isLargeDJ(String JFS,String JIS,int limit){
            
        String[] JFsV=parseJPI(JFS);
        String[] JIsV=parseJPI(JIS);

        float spinI=-1;
        float spinF=-1;
        boolean isLarge=true;
        for(int i=0;i<JFsV.length;i++){
            spinF=spinValue(JFsV[i]); 
            
            if(spinF<0)
                return false;
            
            isLarge=true;
            for(int j=0;j<JIsV.length;j++){
                spinI=spinValue(JIsV[j]);
                if(spinI<0)
                    return false;
                
                int DJ=(int)(Math.abs(spinF-spinI)+0.001);
                //System.out.println("spinF="+spinF+" spinI="+spinI+" DJ="+DJ+" limit="+limit);
                if(DJ<=limit){
                    isLarge=false;
                    break;
                }
                
            }
            
            if(isLarge && JIsV.length>0)
                return true;    
        }
        
        for(int i=0;i<JIsV.length;i++){
            spinI=spinValue(JIsV[i]);   
            
            isLarge=true;
            for(int j=0;j<JFsV.length;j++){
                spinF=spinValue(JFsV[j]);
                
                int DJ=(int)(Math.abs(spinF-spinI)+0.001);
                if(DJ<=limit){
                    isLarge=false;
                    break;
                }           
            }
            
            if(isLarge && JFsV.length>0)
                return true;    
        }
        
        return false;
        
    }
    
    public static int[] findMinAndMaxDJ(String JFS,String JIS){
        int[] out=new int[2];
        int min=1000,max=-1;
        
        String[] JFsV=parseJPI(JFS);
        String[] JIsV=parseJPI(JIS);

        float spinI=-1;
        float spinF=-1;
        
        for(int i=0;i<JFsV.length;i++){
            spinF=spinValue(JFsV[i]);       
            for(int j=0;j<JIsV.length;j++){
                spinI=spinValue(JIsV[j]);
                if(spinF>=0 && spinI>=0){
                    int DJ=(int)(Math.abs(spinF-spinI)+0.001);
                    if(DJ>max)
                        max=DJ;
                    if(DJ<min)
                        min=DJ;
                }
            }
        }
        
        out[0]=min;
        out[1]=max;
        
        return out;
    }
    
    public static Vector<String> findExpectedMULs(String JFS,String JIS){
        Vector<String> expectedMULsV=new Vector<String>();
        
        if(JFS.trim().isEmpty() || JIS.trim().isEmpty())
            return expectedMULsV;
        
        String[] JFsV=parseJPI(JFS);
        String[] JIsV=parseJPI(JIS);

        float spinI=-1;
        float spinF=-1;
        int PI=0;//1 for parity='+',-1 for '-', 0 otherwise
        int PF=0;
        
        String JF="",JI="";
        
        for(int i=0;i<JFsV.length;i++){
            JF=JFsV[i];
            PF=parityNumber(JF);
            spinF=spinValue(JF);
            
            for(int j=0;j<JIsV.length;j++){
                JI=JIsV[j];
                PI=parityNumber(JI);
                spinI=spinValue(JI);
                
                Vector<String> temp=findExpectedMULs(spinF,spinI,PF,PI);
                for(String s:temp)
                    if(!expectedMULsV.contains(s))
                        expectedMULsV.add(s);
            }
        }
                        
        return expectedMULsV;

    }
    
    public static Vector<String> findExpectedMULs(float JF,float JI,int PF,int PI){
        Vector<String> expectedMULsV=new Vector<String>();
    
        int DP=PI*PF;//parity change, +1 for yes, -1 for no, 0 for unknown
        int minL=-1,maxL=-1;
        String mul="";
        
        if(Math.abs(PF)!=1 || Math.abs(PI)!=1)
            DP=0;
        
        float min=Math.abs(JF-JI);
        float max=Math.abs(JF+JI);
        minL=(int)(min+0.001);//spin change
        maxL=(int)(max+0.001);
        
        if(Math.abs(minL-min)>0.0 || Math.abs(maxL-max)>0.0)
            return expectedMULsV;
        
        if(JF<0 || JI<0){
            minL=0;
            maxL=2;
        }
    
        for(int k=minL;k>=0 && k<=maxL;k++){
            if(DP==1 || DP==-1){
                mul=MUL(k,DP).trim();
                if(!mul.isEmpty() && !expectedMULsV.contains(mul))
                    expectedMULsV.add(mul);
            }else{
                mul=MUL(k,1).trim();
                if(!mul.isEmpty() && !expectedMULsV.contains(mul))
                    expectedMULsV.add(mul);
                
                mul=MUL(k,-1).trim();
                if(!mul.isEmpty() && !expectedMULsV.contains(mul))
                    expectedMULsV.add(mul);
            }
            
            //debug
            //System.out.println("   EnsdfUtil line 2531: L="+k+"  DP="+DP+"  MUL(L,DP)="+mul);
            
            mul=MUL(k).trim();
            if(!mul.isEmpty() && !expectedMULsV.contains(mul))
                expectedMULsV.add(mul);
        }
        
        expectedMULsV.remove("M0");
        
        return expectedMULsV;
    }
    
    
    /*
     * parse uncertainty: set a uncertainty if uncertainty string is non-numerical
     * factor: new uncertainty=valueOf(s)*factor 
     * NOTE:
     *     the return uncertainty is not the true uncertainty if uncertainty string
     *     ds is in ENSDF format. DO NOT USE the return value for uncertainty for t
     *     such case.
     */
    public static float[] parseNonNumericalUnc(String valS,String uncS,float factor){
        float val=-1;
        float uncUP=-1,uncDW=-1;
        boolean goodVal=false,goodUnc=false;
        String ds=uncS.toUpperCase().trim();
        
        try{
            val=Float.parseFloat(valS);
            goodVal=true;
            uncUP=Float.parseFloat(ds);
            uncDW=uncUP;
            goodUnc=true;
        }catch (NumberFormatException e){           
        }
        
        if(goodVal && !goodUnc && factor>0){
            val=Math.abs(val);
            if(ds.isEmpty() || ds.equals("AP") || ds.equals("SY") || ds.equals("CA")){
                uncUP=factor*val;
                uncDW=uncUP;
            }else if(ds.equals("LT") || ds.equals("LE")){
                if(factor>=1)//if factor>1, force to set it to be 0.5
                    factor=0.5f;
                
                uncUP=factor*val;
                val=val-uncUP;
                uncDW=val*0.999f;
            }else if(ds.equals("GT") || ds.equals("GE")){
                uncUP=factor*val;
                uncDW=0;
            }       
        }

        float[] out=new float[3];
        out[0]=val;
        out[1]=uncUP;
        out[2]=uncDW;
        
        return out;
    }
    
    public static String parseLevelXREF(Level lev){
        try{
            String levelXREFs="";
            for(int i=0;i<lev.nXRefsV();i++)
                levelXREFs+=lev.XRefsV().get(i);
            
            return levelXREFs;
        }catch(Exception e){
        }
        
        return "";  
    }
    
    private static Vector<String> tempKeywordsV=new Vector<String>();
    
    public static Vector<String> parseT12MethodsV(String str){
    	Vector<String> out=new Vector<String>();
    	try {
    		tempKeywordsV.clear();
    		int n=0;
    		
    		//String s0=str;
    		str=str.toUpperCase();
    		while(n<5) {
    			String tempMethod=parseT12Method(str);
    			if(tempMethod.isEmpty())
    				break;
    			
    			if(!out.contains(tempMethod)) {
    				out.add(tempMethod);
    			}
    			
    			str=str.replace(tempMethod.toUpperCase(),"");
				if(tempMethod.equals("gg(t)"))
					str=str.replace("|G|G(T)","");
				else if(tempMethod.equals("g(t)"))
					str=str.replace("|G(T)","");
				
				for(String s:tempKeywordsV) {
					//System.out.println(s);
					str=str.replace(s, "");
				}
								
				str=str.trim();
				
				//System.out.println("#### n="+n+" tempMethod="+tempMethod+" "+ tempKeywordsV.size()+"  s="+str);
				
				tempKeywordsV.clear();
				
    			n++;
    				
    		}
    		
    		//System.out.println(out.size()+"  "+!isKeynumber(out.get(0)));
    		
    		if(out.size()>1 && !isKeynumber(out.get(0))) {
    			//String m0=out.get(0);
    			
    			Vector<String> tempV=new Vector<String>();
    			//System.out.println("-----------------"+out.size());
    			//for(String s:out)
    			//	System.out.println(out.size()+"### "+s+"   $str="+s0);
    			
    			for(int i=out.size()-1;i>=1;i--) {
    				if(isKeynumber(out.get(i))) {
    					continue;
    				}else {
    					boolean skip=false;
    					for(int j=i-1;j>=0;j--) {
    						//System.out.println("       i="+i+"  "+out.get(i)+"  j="+j+"  "+out.get(j)+"   "+isSimilarTMethod(out.get(i),out.get(j)));
    						
    						if(isSimilarTMethod(out.get(i),out.get(j))) {
    							skip=true;
    							break;
    						}
    					}
    					if(skip)
    						continue;
    				}
    				
    				//System.out.println(out.get(i));
    				tempV.add(0,out.get(i));
    			}
    			
    			if(isSimilarTMethod("DELAYED-COIN",out.get(0))) {
    				//tempV.add(0,"delayed-coin");
    				tempV.add(0,out.get(0));
    			}else {
    				tempV.add(0,out.get(0));
    			}
    			
    			out.clear();
    			out.addAll(tempV);
    			
    			//for(String s:out)
    			//	System.out.println(out.size()+"### "+s+"   $str="+s0);
    		}
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return out;
    }
    
    public static boolean isSimilarTMethod(String s1,String s2) {
    	try {
    		s1=s1.toUpperCase();
    		s2=s2.toUpperCase();
    		if(s1.isEmpty() || s2.isEmpty())
    			return false;
    		
    		if(s1.equals(s2))
    			return true;
    		
    		if(s1.endsWith("COIN")&&s2.endsWith("(T)")){
    			s2=s2.replace("(T)","");
    			if(s2.equals("GG"))
    				return true;
    					
    			s2=s2.replace("(","").replace(")","").replace("G","");
    			if(s2.length()>0) {
    				Nucleus nuc=new Nucleus(s2);
    				if(nuc.Z()<=2)
    					return true;
    			}
    		}else if(s1.endsWith("(T)")&&s2.endsWith("COIN")) {
    			s1=s1.replace("(T)","");
    			if(s1.equals("GG"))
    				return true;
    					
    			s1=s1.replace("(","").replace(")","").replace("G","");
    			if(s1.length()>0) {
    				Nucleus nuc=new Nucleus(s1);
    				if(nuc.Z()<=2)
    					return true;
    			}
    		}else if(s1.endsWith("COIN") && s2.endsWith("COIN")) {
    			if(s1.startsWith("DELAYED") || s2.startsWith("DELAYED"))
    				return true;
    		}else if(containsAll("LINESHAPE,DSAM",new String[] {s1,s2}) ) {
    			return true;
    		}else if(containsAll("ELAYED-COIN,GG(T),XG(T),AG(T),CENTROID-SHIFT",new String[] {s1,s2})) {
    			return true;
    		}
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    private static boolean contains0(String s,String keyword) {
    	try {
    		boolean b=s.contains(keyword);
    		if(b && !tempKeywordsV.contains(keyword)) {
    			tempKeywordsV.add(keyword);
    		}

    		return b;
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    private static boolean startsWith0(String s,String keyword) {
    	try {
    		boolean b=s.startsWith(keyword);
    		if(b && !tempKeywordsV.contains(keyword)) {
    			tempKeywordsV.add(keyword);
    		}

    		return b;
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    private static boolean containsAny0(String s,String[] keywords) {
    	try {
    		boolean found=false;
    		for(int i=0;i<keywords.length;i++) {
    			String keyword=keywords[i];
    			if(s.contains(keyword)) {
    				found=true;
    				break;
    			} 			
    		}
    		
    		if(found) {
    			for(int i=0;i<keywords.length;i++) {
    				String keyword=keywords[i];
    	    		if(!tempKeywordsV.contains(keyword)) {
    	    			tempKeywordsV.add(keyword);
    	    		}
    			}
    		}

    		return found;
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean containsAny(String s,String[] keywords) {
    	try {
    		for(int i=0;i<keywords.length;i++) {
    			String keyword=keywords[i];
    			if(s.contains(keyword)) {
    				return true;
    			} 			
    		}
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    private static boolean containsAll0(String s,String[] keywords) {
    	try {
    		boolean found=true;
    		for(int i=0;i<keywords.length;i++) {
    			String keyword=keywords[i];
    			if(!s.contains(keyword)) {
    				found=false;
    				break;
    			} 			
    		}
   		
    		if(found) {
    			for(int i=0;i<keywords.length;i++) {
    				String keyword=keywords[i];
    	    		if(!tempKeywordsV.contains(keyword)) {
    	    			tempKeywordsV.add(keyword);
    	    		}
    			}
    		}

    		return found;
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean containsAll(String s,String[] keywords) {
    	try {
    		boolean found=true;
    		for(int i=0;i<keywords.length;i++) {
    			String keyword=keywords[i];
    			if(!s.contains(keyword)) {
    				found=false;
    				break;
    			} 			
    		}
   		
    		return found;
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    /*
     * input s must be in normal case, that is, it should not 
     * be converted to upper-case before calling
     */
    public static String parseT12Method(String s) {
    	try {
			//boolean isGood=true;
    		String s0=s;
    	    if(!s.equals(s.toUpperCase()))
    	    	s=s.replace("|G","WIDTH");
    		
    		s=s.toUpperCase().trim();
    		s=s.replace("'","");

    		
    		//if(s.contains("1974DA31")) System.out.println(s+"$"+s0);
    		
			String method="";

			if(containsAny0(s,new String[]{"RECOIL DISTANCE","RECOIL-DISTANCE","RDM", "RDDS","PLUNGER"})) {
				method="RDM";
			}else if(containsAny0(s,new String[]{"DSAM","DSA"}) || 
					(containsAny0(s,new String[]{"DOPPLER"}) && containsAny0(s,new String[]{"ATTENUATION","FRACTION","BROADENING"})) ) {
				method="DSAM";
			}else if(containsAny0(s,new String[]{"LINESHAPE","LINE-SHAPE","LINE SHAPE"})) {
			    method="lineshape";
			}else if(containsAny0(s,new String[]{"SLOPE METHOD"})) {
			    method="slope";
			}else if(containsAny0(s,new String[]{"CENTROID SHIFT","CENTROID-SHIFT"})) {
			    method="centroid-shift";
			}else if(containsAny0(s,new String[]{"DECAY CURVE","DECAY-CURVE"})) {
			    method="decay-curve";
			}else if(containsAny0(s,new String[]{"DELAY", "COIN"})) {
			    method="delayed-coin";
			}else if(containsAny0(s,new String[]{"FLUORESC"})) {
			    method="nuc. fluorescence";
			}else if(containsAll0(s,new String[]{"TIME","PERTURBED","ANGULAR"})) {
				method="TDPAD";
			}else if(containsAny0(s,new String[]{"(T)", "(|DT)"})) {
				s=s.replace("(|DT)","(T)");
				int n=s.indexOf("(T)");
				int n1=s.lastIndexOf(" ",n-1);
				if(n1==n-1) {
					s=s.substring(0,n1)+s.substring(n1+1);
					n1=s.lastIndexOf(" ",n-1);
				}
					
				if(n1>=0 && n1<n) {
					//do nothing
				}else {
					n1=0;
				}
				method=s.substring(n1,n+3).trim();
				
				//System.out.println(method+" $"+s);
				
				method=method.replace("|G","g").replace("|B","b").replace("|A","a").replace("CE","ce").replace(",","");
				method=method.toLowerCase();


				n=method.length()-1;
				int nb=0;
				if(method.contains("{")) {
					String tempS="";
					while(n>=0) {
						char c=method.charAt(n);
						
						if(c=='}') {
							nb++;

							n1=method.lastIndexOf("{",n);
							if(n1>=0 && nb==1) {
								
								String s1=method.substring(n1+1,n).trim();
								//System.out.println("###"+method+" $$$"+s1+"  "+s1.startsWith("+")+"  "+Str.isInteger(s1));
								if(s1.startsWith("+") && Str.isInteger(s1.substring(1))) {
									tempS=s1.substring(1)+tempS;
									n=n1-1;
									nb--;
									continue;
								}
							}
						}else if(c=='{') {
							nb--;
						}else if(nb==0) {
							tempS=c+tempS;
						}
						
						n--;
					}
	
					method=tempS;
				}
	
				
				n=method.indexOf("(t)")-1;	
				  
				//if(method.contains("x-rays)(t)"))
				//	System.out.println("1  n="+n+" method="+method+"  s="+s);
				//System.out.println(method+"$ "+s);
				
				nb=0;
				while(n>=0) {
					char c=method.charAt(n);

					if(c==')')
						nb++;
					else if(c=='(')
						nb--;
					
					char nextChar=method.charAt(n+1);
					if(Character.isDigit(c) && (nextChar=='g'||nextChar==')') && !Str.removeAllBrackets(method.substring(n+1).trim()).equals("t")) {
						String s1=method.substring(0,n);
						method=method.substring(n+1).trim();	
						if(nextChar=='g') {
							if(s1.contains("ce"))
								method="ce-g(t)";
							else if(s1.contains("g"))
								method="gg(t)";
							else if(s1.contains("b"))
								method="bg(t)";
							else if(s1.contains("x-ray") || s1.contains("x ray"))
								method="(x ray)-g(t)";
							else if(Str.removeAllBrackets(s1).length()>0)
								method="delayed-g(t)";
							else
								method="g(t)";
						}
		
						
						break;
					}
					
					if(!Character.isLetter(c)&&c!='-'&&c!='\'' && !method.substring(n+1).trim().equals("(t)") && ((nb==0&&c!='(')||(c=='('&&nb==-1)) ) {	
						String s1=method.substring(0,n);
						method=method.substring(n+1).trim();
						//if(method.contains("x-rays)(t)"))
						//	System.out.println("c="+c+" n="+n+" method="+method+"  s="+s+" nb="+nb);
						if(s1.contains("ce"))
							method="ce"+method;
						else if(s1.contains("|g"))
							method="g"+method;
						else if(s1.contains("x-ray") || s1.contains("x ray"))
							method="(x ray)-"+method;
						else if(Str.removeAllBrackets(s1).length()>0)
							method="delayed-"+method;
					
						break;
					}
					n--;
				}
				
				if(nb==1)
					method="("+method;
				
				method=method.replace("(g)","g").replace("()","").replace("gg(|q)","");
				n=method.indexOf("gg");
				if(n>0) {
					n=method.lastIndexOf(")",n);
					if(n>0)
						method=method.substring(n+1).trim();
				}
				if(method.startsWith("ray"))
					method="x-"+method;
				
			}else if(containsAny0(s,new String[]{"(T,"})) {
				int n=s.indexOf("(T,");
				int n1=s.lastIndexOf(" ",n-1);
				if(n1>=0 && n1<n) {
					//do nothing
				}else {
					n1=0;
				}
				int n2=s.indexOf(" ",n+4);
				if(n2<0)
					n2=s.length();
				
				String s1=s.substring(n1,n2).trim();
				if(containsAny0(s1,new String[] {"|G,|B,|A,CE,"}) && containsAny0(s1,new String[] {"|Q,H"})) {
					method=s1;
					method=method.replace("|G","g").replace("|B","b").replace("|A","a").replace("CE","ce").replace(",|Q","").replace(",H","").replace(",","");
					method=method.toLowerCase();
					
					n=method.indexOf("(t")-1;
					
					int nb=0;
					while(n>=0) {
						char c=method.charAt(n);
						if(c==')')
							nb++;
						else if(c=='(')
							nb--;
						
						if(!Character.isLetter(c)&&c!='-'&&c!='\'') {
							method=method.substring(n+1).trim();
							break;
						}
						n--;
					}
					if(nb==1)
						method="("+method;
				}else {
					s=s.replace("(T,", "");
					return parseT12Method(s);
				}
			}else if(containsAny0(s,new String[]{",T)"})) {
				int n=s.indexOf(",T)");
				int n1=s.lastIndexOf(" ",n-1);
				if(n1>=0 && n1<n) {
					//do nothing
				}else {
					n1=0;
				}
				int n2=s.indexOf(" ",n+3);
				if(n2<0)
					n2=s.length();
				
				String s1=s.substring(n1,n2).trim();
				if(containsAny0(s1,new String[] {"|G,|B,|A,CE,"}) && containsAny0(s1,new String[] {"|Q,H"})) {
					method=s1;
					method=method.replace("|G","g").replace("|B","b").replace("|A","a").replace("CE","ce").replace("|Q,","").replace("H,","").replace(",","");
					method=method.toLowerCase();
					
					int nb=0;
					n=method.length()-1;
					while(n>=0) {
						char c=method.charAt(n);
						if(c==')')
							nb++;
						else if(c=='(')
							nb--;
						n--;
					}
					if(nb==1)
						method="("+method;
					
					n=method.indexOf("t)")-2;
					while(n>=0) {
						char c=method.charAt(n);
						if(!Character.isLetter(c)&&c!='-'&&c!='\'') {
							method=method.substring(n+1).trim();
							break;
						}
						n--;
					}
				}else {
					s=s.replace(",T)", "");
					return parseT12Method(s);
				}
			}else if(containsAny0(s,new String[]{"|B-COUNTING", "|B COUNTING"})) {
			    method="beta-counting";
			}else if(containsAny0(s,new String[]{"WIDTH"})) {// || containsAny(s0,new String[]{"|G"})) {
			    method="level-width";
			}else if(containsAny0(s,new String[]{"TIMING"})) {
			    if(containsAny0(s,new String[]{"GAMMA", "|G"}))
			        method="g(t)";
			    else if(containsAny0(s,new String[]{"CONVERSION"}))
			        method="ce(t)";
			    else if(containsAny0(s,new String[]{"X RAY", "X-RAY"}))
			        method="X-ray(t)";
			    else if(containsAny0(s,new String[]{"|A"}))
			        method="a(t)";
			    else
			        method="direct-timing";
			}else if(containsAny0(s,new String[]{"FROM B(E2","FROM B(M1","FROM B(E1"}) || startsWith0(s0,"B(E2") ||
					startsWith0(s0,"B(M1") || startsWith0(s0,"B(E1") || 
					(containsAny0(s0,new String[]{"B(E2","B(M1","B(E1"})&&!s.contains("ADOPTED B("))) {
			    int n1=s.indexOf("B(");
			    int n2=s.indexOf(")",n1+2);
			    if(n2<0)
			        n2=s.length();
			    s=s.substring(n1,n2+1).trim();
			    n1=s.indexOf("=");
			    if(n1>0)
			        method=s.substring(0,n1).trim();
			    else
			        method=s;
			}else if(containsAny0(s,new String[]{"QUADRUPOLE MOMENT"}) ){
				method="B(E2)";
			}else if(!s.startsWith("FROM ADOPTED")&&!s.startsWith("OTHER")&&!s.startsWith("FROM THE ADOPTED")) {
			    String s1=Str.firstSentence(s);
			    if(!s.startsWith("FROM") && !s.startsWith("DEDUCED") && !s.startsWith("ESTIMATE") && !containsAny0(s,new String[]{"AVERAGE"})) {
			        
			    }else if(s.startsWith("FROM")) {
			        s=s.substring(4).trim();
			    }else if(s.startsWith("DEDUCED")) {
			        s=s.substring(7).trim();
			    }
			
			    if(containsAny0(s, new String[]{"PULSED-BEAM", "PULSED BEAM"})) {
			        method = "pulsed-beam";
			    } else if(containsAny0(s, new String[]{"RESONANCE"})) {
			        method = "resonance";
			    } else if(containsAny0(s, new String[]{"TIME DISTRIBUTION", "TIME-DISTRIBUTION"})) {
			        method = "time-distribution";
			    } else if(containsAny0(s, new String[]{"TIME INTERVAL", "TIME-INTERVAL"})) {
			        method = "time-interval";
			    } else if(containsAny0(s, new String[]{"TIME DIFFERENCE", "TIME-DIFFERENCE", "TIME VARIATION", "TIME-VARIATION"})) {
			        method = "time-difference";
			    } else if(containsAll0(s, new String[]{"MATRIX ELEMENT","TRANSITION"})) {
			        if(containsAny0(s, new String[]{"E2"})) {
			            method = "B(E2)";
			        } else if(containsAny0(s, new String[]{"M1"})) {
			            method = "B(M1)";
			        } else if(containsAny0(s, new String[]{"E1"})) {
			            method = "B(E1)";
			        } else {
			            method = "B(E2)";
			        }
			    } else if(containsAny0(s, new String[]{"|A|G-COIN", "|A|G COIN", "|A|G", "|A-|G"})) {
			        method = "delayed-coin";
			    } else if(containsAny0(s, new String[]{"CE-COIN", "CE COIN"})) {
			        int n2 = s.indexOf("CE-COIN");
			        if(n2 < 0)
			            n2 = s.indexOf("CE COIN");
			        int n1 = s.lastIndexOf(" ", n2);
			        if(n1 < 0)
			            n1 = 0;
			        method = s.substring(n1 + 1, n2 + 7).toLowerCase();
			    } else if(containsAll0(s, new String[]{"CORRELATION","TIME"})) {
			        method = "time-correlation";
			    } else if(containsAny0(s, new String[]{"|G COIN", "|G-COIN", "|G|G"})) {
			        method = "delayed-coin";
			    } else if(containsAll0(s1, new String[]{"RECOIL","DISTANCE"})) {
			        method = "RDM";
			    } else if(containsAny0(s1, new String[]{"FISSION"})) {
			        method = "fission";
			    } else {		        
			        String keyno = EnsdfUtil.findFirstKeyNumber(s1);
                    
			        String s2=parseMethodWithKeyno(s1,keyno);
			        if(s2.length()>0) {
			        	method=s2;
			        }else {						
				        if(s0.contains("|g") || s0.contains("|b") || s0.contains("|a") || s0.contains("ce")) {
				            s0=Str.removeAllBrackets(s0).replace("-","");
				            if(containsAny(s0,new String[] {"|g|g","|b|g","|a|g","ce|g","bce"})){
				            	method="delayed-coin";
				            }else {
				            	method=keyno;
				            }
				        } else {
				            method = keyno;
				        }
			        }

			    }
		
				//method=(s.length()>=20?s.substring(0,20):s);
			}else {
				method="";
			}
			//if(s.contains("1974VA19")) System.out.println(s+"$"+method);
			
			if(method.contains("ce(")) {
				s=method;
				int n=0;
				while((n=s.indexOf("ce(",n))>=0) {
					int n1=s.indexOf(")",n);
					if(n1>n) {
						String s1=s.substring(n+3,n1).toUpperCase();
						if(s1.length()>0 && Str.isDigit(s1.charAt(0))) {
							s=s.substring(0,n+3)+s1+s.substring(n1);
						}
						n=n1;
					}else {
						break;
					}
				}
				method=s;
			}
			
			method=method.replace("()","").replace("{}","");
			method=Str.removeUnmatchedBrackets(method);
			
			return method;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return "";
    }
    private static String parseMethodWithKeyno(String s,String keyno) {
    	try {
    		String method=keyno;
			if(EnsdfUtil.containsAny(s,new String[] {"METHOD","TECHNIQUE"}) ) {
				int n=s.indexOf("METHOD");
				if(n<0)
					n=s.indexOf("TECHNIQUE");
				
				String s1=s.substring(0,n).trim();
				s1=parseMethodByPreposition(s1);
				if(s1.length()>0)
					method=s1;
			}else if(s.startsWith("FROM")&&(s.endsWith("IN "+method)||s.endsWith("("+method+")")||s.endsWith("BY"+method))) {
				int n=s.length()-method.length()-2;
				if(n>4) {
					String s1=s.substring(4,n);
					if(!s1.contains("=") && s1.contains(">") && s1.contains("<") && s1.contains("ADOPTED")) {
						if(s1.length()<=20) {
							method=s1;
						}else {
							s1=parseMethodByPreposition(s1);
							if(s1.length()>0)
								method=s1;
						}
					}

				}									
			}	
			method=Str.removeUnmatchedBrackets(method);
			
			return method;
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    private static String parseMethodByPreposition(String line) {
    	try {
    		String s1=line;
			String s2="";
			int n=s1.length()-1;
			int n1=0;
			while(n>=0 && (n1=s1.lastIndexOf(" ",n))>0 && s2.length()<=20) {
				String tempS=s1.substring(n1,n+1).toUpperCase();
				if(tempS.equals("FROM") || tempS.equals("BY") || tempS.equals("WITH") || tempS.equals("THROUGH")
						||tempS.equals("VIA") || EnsdfUtil.isKeynumber(tempS)) {
					break;
				}
				if(tempS.length()>10 && !tempS.contains("-"))
					tempS=tempS.substring(0,5)+".";
				
				if((tempS+s2).length()>20)
					break;
				
				s2=tempS+s2;
				n=n1-1;
			}

			if(!s2.contains("=") && s2.contains(">") && s2.contains("<") && s2.contains("ADOPTED"))
				return s2.trim();
    	}catch(Exception e) {
    		
    	}
    	
    	return "";
    }
    public static String parseT12Method_old(String s) {
    	try {
			//boolean isGood=true;
    		String s0=s;

    		s=s.toUpperCase().trim();
    		s=s.replace("'","");
    		
    		//if(s.contains("1974DA31")) System.out.println(s+"$"+s0);
    		
			String method="";

			if(contains0(s,"RECOIL DISTANCE") || contains0(s,"RECOIL-DISTANCE") || contains0(s,"RDM") || 
					contains0(s,"RDDS") || contains0(s,"PLUNGER")) {
				method="RDM";
			}else if(contains0(s,"DOPPLER") || contains0(s,"DSAM") || contains0(s,"DSA") ) {
				method="DSAM";
			}else if(contains0(s,"LINESHAPE") || contains0(s,"LINE-SHAPE") || contains0(s,"LINE SHAPE")) {
				method="lineshape";
			}else if(contains0(s,"SLOPE METHOD")) {
				method="slope";
			}else if(contains0(s,"CENTROID SHIFT") || contains0(s,"CENTROID-SHIFT")) {
				method="centroid-shift";
			}else if(contains0(s,"DECAY CURVE") || contains0(s,"DECAY-CURVE")) {
				method="decay-curve";
			}else if(contains0(s,"DELAY") && contains0(s,"COIN")) {
				method="delayed-coin";
			}else if(contains0(s,"FLUORESC")) {
				method="nuc. fluorescence";
			}else if(contains0(s,"(T)") || contains0(s,"(|DT)")) {
				s=s.replace("(|DT)","(T)");
				int n=s.indexOf("(T)");
				int n1=s.lastIndexOf(" ",n-1);
				if(n1==n-1) {
					s=s.substring(0,n1)+s.substring(n1+1);
					n1=s.lastIndexOf(" ",n-1);
				}
					
				if(n1>=0 && n1<n) {
					//do nothing
				}else {
					n1=0;
				}
				method=s.substring(n1,n+3).trim();
				method=method.replace("|G","g").replace("|B","b").replace("|A","a").replace("CE","ce").replace(",","");
				method=method.toLowerCase();

				n=method.length()-1;
				int nb=0;
				if(method.contains("{")) {
					String tempS="";
					while(n>=0) {
						char c=method.charAt(n);
						if(c=='}') {
							nb++;
						}else if(c=='{') {
							nb--;
						}else if(nb==0)
							tempS=c+tempS;
						n--;
					}
					
					method=tempS;
				}

				
				n=method.indexOf("(t)")-1;	
				  
				//if(method.contains("x-rays)(t)"))
				//	System.out.println("1  n="+n+" method="+method+"  s="+s);

				nb=0;
				while(n>=0) {
					char c=method.charAt(n);

					if(c==')')
						nb++;
					else if(c=='(')
						nb--;
					
					char nextChar=method.charAt(n+1);
					if(Character.isDigit(c) && (nextChar=='g'||nextChar==')') && !Str.removeAllBrackets(method.substring(n+1).trim()).equals("t")) {
						method=method.substring(n+1).trim();					
						break;
					}
					if(!Character.isLetter(c)&&c!='-'&&c!='\'' && !method.substring(n+1).trim().equals("(t)") && ((nb==0&&c!='(')||(c=='('&&nb==-1)) ) {	
						method=method.substring(n+1).trim();
						//if(method.contains("x-rays)(t)"))
						//	System.out.println("c="+c+" n="+n+" method="+method+"  s="+s+" nb="+nb);
						
						break;
					}
					n--;
				}
				
				if(nb==1)
					method="("+method;
				
				method=method.replace("(g)","g").replace("()","").replace("gg(|q)","");
			}else if(contains0(s,"(T,")) {
				int n=s.indexOf("(T,");
				int n1=s.lastIndexOf(" ",n-1);
				if(n1>=0 && n1<n) {
					//do nothing
				}else {
					n1=0;
				}
				int n2=s.indexOf(" ",n+4);
				if(n2<0)
					n2=s.length();
				method=s.substring(n1,n2).trim();
				method=method.replace("|G","g").replace("|B","b").replace("|A","a").replace("CE","ce").replace(",|Q","").replace(",H","").replace(",","");
				method=method.toLowerCase();
				
				n=method.indexOf("(t")-1;
				
				int nb=0;
				while(n>=0) {
					char c=method.charAt(n);
					if(c==')')
						nb++;
					else if(c=='(')
						nb--;
					
					if(!Character.isLetter(c)&&c!='-'&&c!='\'') {
						method=method.substring(n+1).trim();
						break;
					}
					n--;
				}
				if(nb==1)
					method="("+method;
			}else if(contains0(s,",T)")) {
				int n=s.indexOf(",T)");
				int n1=s.lastIndexOf(" ",n-1);
				if(n1>=0 && n1<n) {
					//do nothing
				}else {
					n1=0;
				}
				int n2=s.indexOf(" ",n+3);
				if(n2<0)
					n2=s.length();
				method=s.substring(n1,n2).trim();
				method=method.replace("|G","g").replace("|B","b").replace("|A","a").replace("CE","ce").replace("|Q,","").replace("H,","").replace(",","");
				method=method.toLowerCase();
				
				int nb=0;
				n=method.length()-1;
				while(n>=0) {
					char c=method.charAt(n);
					if(c==')')
						nb++;
					else if(c=='(')
						nb--;
					n--;
				}
				if(nb==1)
					method="("+method;
				
				n=method.indexOf("t)")-2;
				while(n>=0) {
					char c=method.charAt(n);
					if(!Character.isLetter(c)&&c!='-'&&c!='\'') {
						method=method.substring(n+1).trim();
						break;
					}
					n--;
				}
			}else if(contains0(s,"|B-COUNTING") || contains0(s,"|B COUNTING")) {
				method="beta-counting";
			}else if(contains0(s,"WIDTH") || contains0(s0,"|G")) {
				method="level-width";
			}else if(contains0(s,"TIMING")) {
				if(contains0(s,"GAMMA") || contains0(s,"|G"))
					method="g(t)";
				else if(contains0(s,"CONVERSION"))
					method="ce(t)";
				else if(contains0(s,"X RAY")||contains0(s,"X-RAY"))
					method="X-ray(t)";
				else if(contains0(s,"|A"))
					method="a(t)";
				else
					method="timing";
			}else if(contains0(s,"FROM B(") || s0.startsWith("B(") || (contains0(s0,"B(")&&!contains0(s,"ADOPTED B(")) ) {
				int n1=s.indexOf("B(");
				int n2=s.indexOf(")",n1+2);
				if(n2<0)
					n2=s.length();
				s=s.substring(n1,n2+1).trim();
				n1=s.indexOf("=");
				if(n1>0)
					method=s.substring(0,n1).trim();
				else
					method=s;
			}else if(!s.startsWith("FROM ADOPTED")&&!s.startsWith("OTHER")&&!s.startsWith("FROM THE ADOPTED")) {
				String s1=Str.firstSentence(s);
				if(!s.startsWith("FROM") && !s.startsWith("DEDUCED") && !s.startsWith("ESTIMATE") && !contains0(s,"AVERAGE") ) {
					
				}else if(s.startsWith("FROM")) {
					s=s.substring(4).trim();
				}else if(s.startsWith("DEDUCED")) {
					s=s.substring(7).trim();
				}
				
				if(contains0(s,"PULSED-BEAM") || contains0(s,"PULSED BEAM")) {
					method="pulsed-beam";
				}else if(contains0(s,"RESONANCE")) {
					method="resonance";
				}else if(contains0(s,"TIME DISTRIBUTION") || contains0(s,"TIME-DISTRIBUTION")) {
					method="time-distribution";
				}else if(contains0(s,"TIME INTERVAL") || contains0(s,"TIME-INTERVAL")) {
					method="time-interval";
				}else if(contains0(s,"TIME DIFFERENCE") || contains0(s,"TIME-DIFFERENCE") ||
						contains0(s,"TIME VARIATION") || contains0(s,"TIME-VARIATION") ) {
					method="time-difference";
				}else if(contains0(s,"MATRIX ELEMENT") && contains0(s,"TRANSITION")) {
					if(contains0(s,"E2"))
						method="B(E2)";
					else if(contains0(s,"M1"))
						method="B(M1)";
					else if(contains0(s,"E1"))
						method="B(E1)";
					else
						method="B(E2)";
				}else if(contains0(s,"|A|G-COIN") || contains0(s,"|A|G COIN") || contains0(s,"|A|G") || contains0(s,"|A-|G")) {
					method="ag-coin";
				}else if(contains0(s,"CE-COIN") || contains0(s,"CE COIN")) {
					int n2=s.indexOf("CE-COIN");
					if(n2<0)
						n2=s.indexOf("CE COIN");
					int n1=s.lastIndexOf(" ",n2);
					if(n1<0)
						n1=0;
					
					method=s.substring(n1+1,n2+7).toLowerCase();
				}else if(contains0(s,"CORRELATION")) {
					method="time-correlation";
				}else if(contains0(s,"|G COIN") || contains0(s,"|G-COIN") || contains0(s,"|G|G")) {
					method="delayed-coin";
				}else if(s1.startsWith("FROM") && s1.contains("COIN") && (s1.contains("X RAY") || s1.contains("X-RAY") || s1.contains("|G") || s1.contains("|A"))) {
					int n2=s1.indexOf("COIN");
	
					int n1=s1.lastIndexOf(" ",n2-2);
					if(n1<0)
						n1=0;
					
					method=s1.substring(n1+1,n2+4).replace("|G","g").replace("|B","b").replace("|A","a");
					method=method.toLowerCase();
				}else if(s1.contains("RECOIL") && s1.contains("DISTANCE")) {
					method="RDM";
				}else if(s1.contains("FISSION")) {
					method="fission";
				}else {
					s=Str.firstSentence(s);
					
					s1=EnsdfUtil.findFirstKeyNumber(s);
					if(s1.length()>0) {
						int n=s.indexOf("("+s1+")");
						if(n>0) {
							s=s.substring(0,n).trim();
						}
					}
					
					//if(s.contains("1972SE06")) 
					//	System.out.println(s+"$$$"+s1);
					
					if(s0.contains("|g")||s0.contains("|g")||s0.contains("|a")||s0.contains("ce")) {
						int n=0;
						
						
						if(contains0(s,"|G")) {
							n+=Str.match(s0, "\\|g").size();
						}
						if(contains0(s,"|B")) n++;
						if(contains0(s,"|A")) n++;
						if(contains0(s,"CE")) n++;
						
						//if(s.contains("1974DA31")) System.out.println("s="+s+" s1="+s1+" n="+n);
						
						if(n<=1)
							method=s1;
						else
							method="delayed-coin";
					}else
						method=s1;
				}
		
				//method=(s.length()>=20?s.substring(0,20):s);
			}else {
				method="";
			}
			//if(s.contains("1974VA19")) System.out.println(s+"$"+method);
			return method;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return "";
    }
    
    /* Parse a string of nuclide name and create a nucleus object
     * from it. It can takes nuclide string in either format:
     * (mass+name) or (name+mass) or name only
     * Note that the parse method in Nucleus class only takes
     * NUCID string in ENSDF format (mass+name) or element name
     * (no mass number)
     */
    public static Nucleus parseNucleus(String nucName){
        String name=nucName.trim();
        String eName="";
        int len=name.length();
        int A=-1;

        for(int j=0;j<len;j++){
            if(j<len-1){
                boolean isDigit1=Character.isDigit(name.charAt(j));
                boolean isDigit2=Character.isDigit(name.charAt(j+1));
                if(isDigit1!=isDigit2){
                    if(isDigit1){
                        A=Integer.parseInt(name.substring(0, j+1));
                        eName=name.substring(j+1);
                    }else{
                        try{
                            A=Integer.parseInt(name.substring(j+1));
                        }catch(Exception e){}
                        
                        eName=name.substring(0,j+1);
                    }
                    
                    break;
                }
            }
        }
        
        if(eName.isEmpty()){//nucName=(mass) or (element name)
            try{
                A=Integer.parseInt(name);
            }catch(Exception e){
                eName=name;
            }
        }
        
        Nucleus nuc=new Nucleus();
        nuc.setZA(-1,A);
        nuc.setEN(eName);
        
        //System.out.println("In parseNucleus: name="+name+" A="+A+" eName="+eName+" nuc: name="+nuc.En()+" A="+nuc.a()+" Z="+nuc.z());
        
        return nuc;
    }
    
    /*
     * parse and set target JPS in an ENSDF dataset of transfer reaction
     */
    public static String parseTargetJPS(ENSDF ens) {
        Nucleus targetNuc=ens.target();
        String JPS="";
        if(targetNuc!=null) {
            if(targetNuc.isEvenEven()) {
                targetNuc.setJPS("0+");
                return "0+";
            }
            
            //search in general comments for comment like " 24NA  c Target J|p=3/2+", or " 24NA c J|p({+23}Na g.s.)=3/2+"
            for(int i=0;i<ens.comV().size();i++) {
                Comment c=ens.commentAt(i);
                String s=c.body();
            
                //System.out.println("In ENSDFUtil line 3249: s1="+s+" c.type().isEmpty()="+c.type().isEmpty()+" c.head().isEmpty()="+c.head().isEmpty());
                
                if(!c.type().trim().isEmpty() || !c.head().isEmpty())//note c.type()=" " for general comments
                    continue;

                int n=s.indexOf("=");
                if(n<=0)
                    continue;

                String s0=s.substring(0,n); 
                
                if(c.isBigC() && !s0.contains("JPI"))
                    continue;
                if(!c.isBigC() && !s0.contains("J|p") && !s0.contains("J{+|p}"))
                    continue;
                
                String s1=s.substring(n+1).replace(".","").trim();
                               
                if(s1.isEmpty() || !isJPIStr(s1))
                    continue;
                
                s0=s0.toUpperCase().replace("{", "").replace("}", "").trim();
                
                if(s0.contains("TARGET") || s0.contains(targetNuc.nameENSDF().trim())) {
                    JPS=s1;
                    targetNuc.setJPS(s1);
                    
                    
                    break;
                }
            }
        }

        return JPS;
    }
    
    public static Vector<String> makeNewXTags(Vector<ENSDF> ensV){  
        int count=0;
        Vector<String> out=new Vector<String>();
        for(int i=0;i<ensV.size();i++){
            if(ensV.get(i).fullDSId().contains("ADOPTED"))
                out.add("");
            else{
                out.add(makeNewXTag(count));
                count++;
            }
        }
        return out;
    }
    
    public static Vector<String> makeXTags(Vector<ENSDF> ensV){  
        int count=0;
        Vector<String> out=new Vector<String>();
        for(int i=0;i<ensV.size();i++){
            if(ensV.get(i).fullDSId().contains("ADOPTED"))
                out.add("");
            else{
                out.add(makeXTag(count));
                count++;
            }
        }
        return out;
    }
    
    public static Vector<String> makeNewXTags(int nENSDFs){
        Vector<String> out=new Vector<String>();
        for(int i=0;i<nENSDFs;i++)
            out.add(makeNewXTag(i));
        
        return out;
    }
    
    public static Vector<String> makeXTags(int nENSDFs){
        Vector<String> out=new Vector<String>();
        for(int i=0;i<nENSDFs;i++)
            out.add(makeXTag(i));
        
        return out;
    }
    
    //one- or two-letter tag (in PDF out)
    public static String makeNewXTag(int i){
        String[] t=new String[100];
        t[0]="A"; t[1]="B"; t[2]="C";t[3]="D";t[4]="E";t[5]="F";t[6]="G";t[7]="H";t[8]="I";t[9]="J";
        t[10]="K";t[11]="L";t[12]="M";t[13]="N";t[14]="O";t[15]="P";t[16]="Q";t[17]="R";t[18]="S";
        t[19]="T";t[20]="U";t[21]="V";t[22]="W";t[23]="X";t[24]="Y";t[25]="Z";       
        for(int x=26;x<t.length;x++){
            int y=x/26-1;
            t[x]=t[y]+t[x%26];
        }
        
        if(i<0 || i>=100)
            return "@@";
        
        return t[i];
    }

    //one-character tag (old tag in ENSDF file)
    public static String makeXTag(int i){
        String[] t={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
                    "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
                    "0","1","2","3","4","5","6","7","8","9","@","#","$","%","^","&","*","(",")","!","[","]","<",">","~","/"};
        
        if(i<0 || i>t.length)
            return "?";
        
        return t[i];
    }
    
    public static Vector<String> mergeENSDFDatasets(Vector<ENSDF> ensdfsV){
        Vector<String> out=new Vector<String>();
        
        sortENSDFs(ensdfsV);
        
        Vector<String> lines;
        try{
            for(int i=0;i<ensdfsV.size();i++){
                ENSDF ens=ensdfsV.get(i);
                if(ens==null)
                    continue;
                
                lines=Str.trimV(ens.lines());
                out.addAll(lines);
                if(lines.size()>0)
                    out.add(Str.repeat(" ", 80));
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
                
        return out;
        
    }
    
    public static Vector<String> mergeDatasets_old(Vector<File> filesV){
        Vector<String> out=new Vector<String>();
            
        try{
            sortEnsdfFiles(filesV);
            
            Vector<String> lines;
            for(int i=0;i<filesV.size();i++){
                File f=filesV.get(i);
                if(f==null)
                    continue;
                
                lines=Str.readFile(f);
                lines=Str.trimV(lines);
                out.addAll(lines);
                if(lines.size()>0)
                    out.add(Str.repeat(" ", 80));
            }
            
            if(out.size()>0)
                out.remove(out.size()-1);
            
        }catch(Exception e){
            e.printStackTrace();
        }
                
        return out;
    }
    
    public static Vector<String> mergeDatasets(Vector<File> filesV){
        Vector<String> out=new Vector<String>();
            
        try{
            Vector<ENSDF> ensdfsV=sortEnsdfFiles(filesV);
            out=mergeENSDFDatasets(ensdfsV);
        }catch(Exception e){
            e.printStackTrace();
        }
                
        return out;
    }
    
    public static Vector<String> mergeENSDFDatasetOnly(MassChain data){
        Vector<String> out=new Vector<String>();

        int nENSDF=data.nENSDF();
        if(nENSDF==0)
            return out;

        Vector<ENSDF> ensdfsV=new Vector<ENSDF>();
        
        for(int i=0;i<nENSDF;i++)
            ensdfsV.add(data.getENSDF(i));
        
        return mergeENSDFDatasets(ensdfsV);
    }

    public static Vector<String> mergeAllDatasets(MassChain data){
        Vector<String> out=new Vector<String>();

        int nBlocks=data.nENSDF();
        if(nBlocks==0)
            return out;

        Vector<ENSDF> ensdfsV=new Vector<ENSDF>();
        Vector<String> lines=null;
        
        //data.getENSDF() is real ENSDF dataset with real NUCID and DSID, not including comment and other datasets
        for(int i=0;i<data.nENSDF();i++) 
            ensdfsV.add(data.getENSDF(i));
        
        //for non-ENSDF datasets
        for(int i=0;i<data.nBlocks();i++) {
            lines=data.getBlockAt(i);
            String blockType=data.getBlockTypeAt(i);
            if(blockType.equals("ENSDF"))
                continue;
            
            ENSDF ens=null;
            try {
                ens=new ENSDF();               
                //here it could be a comment dataset 
                ens.setValues(lines,"LITTLE");  
            }catch(Exception e) {
                continue;
            }

            ensdfsV.add(ens);
        }
        
        return mergeENSDFDatasets(ensdfsV);
    }

    
    public static String dirSeparator(){
        String dirSeparator="\\";
        String temp=System.getProperty("os.name");       
        if(!temp.toLowerCase().contains("windows")) 
            dirSeparator="/";
        
        return dirSeparator;
    }
    
        
    public static String saveENSDF(Vector<String> lines,String filename)throws FileNotFoundException{
        PrintWriter out;
        String msg="";
        if(lines.size()<=0)
            return msg;
        
        File f=new File(filename);
        
        File parentFile=f.getAbsoluteFile().getParentFile();
        if(!parentFile.exists()){
            File dir=new File(parentFile.getAbsolutePath());
            if(!dir.mkdir()){
                msg+="Failed to create directory:"+parentFile.getAbsolutePath()+"\n";
                msg+="  Data set is not saved: "+f.getName()+"\n";
                return msg;
            }
        }
        
        out=new PrintWriter(new File(filename));
        for(int i=0;i<lines.size();i++){
            String line=lines.elementAt(i);
            line=line.replace("\n", "");
            
            line=Str.rtrim(line);
            line=Str.fixLineLength(line, 80);
            
            out.write(line+"\n");
        }
        
        //add a 80-column blank line at the end
        if(!lines.lastElement().trim().isEmpty()) {
            out.write(Str.repeat(" ", 80)+"\n");
        }
        
        out.close();
        return filename+"\n"+msg;
    }
    
    
    //save multiple ENSDF datasets into a file
    public static String saveENSDFs(Vector<ENSDF> ensV,String filename) throws FileNotFoundException{
        Vector<ENSDF> tempV=new Vector<ENSDF>();
        tempV.addAll(ensV);
        sortENSDFs(tempV);
        
        Vector<String> lines=new Vector<String>();
        for(ENSDF ens:tempV) {
            
            if(ens.lines().size()>0) {
                lines.addAll(ens.lines());
                lines.add(Str.repeat(" ",80));
            }
        }
        
        String msg=saveENSDF(lines,filename);
        return msg;
    }
    
    //save an ENSDF dataset into a file
    public static String saveENSDF(ENSDF ens,String filename) throws FileNotFoundException{
        return saveENSDF(ens.lines(),filename);
    }
    
    public static String saveENSDF(ENSDF ens,String path,String extension)throws FileNotFoundException{
        String msg="";
        String nucleus=ens.nucleus().En()+ens.nucleus().A();
        String filename=nucleus+"_"+DSID2Filename(ens.DSId0())+"."+extension;
        String dirSeparator=dirSeparator(); 
        String tempPath=path+dirSeparator;
        
        filename=tempPath+filename;
        
        msg+=saveENSDF(ens,filename);
        
        return msg;
    }
    
    public static String saveAllENSDF(MassChain data,String path)throws FileNotFoundException{
        return saveAllENSDF(data,path,"");
    }
    
    public static String saveAllENSDF(MassChain data,String path,String extension)throws FileNotFoundException{
        int A=data.getA();
        String msg="";
        String nucleus="";
        String dirSeparator=dirSeparator();
        
        String tempPath=path+dirSeparator;
        
        extension=extension.trim();
        if(extension.isEmpty())
            extension="ens";
        
        String filename=tempPath+"A"+data.getA()+"_cover."+extension;
        
        Vector<String> cover=data.getComBlock();

        
        if(cover.size()>0){
            msg+=saveENSDF(cover,filename);
        }
        for(int i=0;i<data.nENSDF();i++){
            ENSDF ens=data.getENSDF(i);
            if(ens.nLines()<=0)
                continue;
            
            nucleus=ens.nucleus().En()+A;
            filename=nucleus+"_"+DSID2Filename(ens.DSId0())+"."+extension;
            filename=tempPath+filename;
            
            msg+=saveENSDF(ens,filename);
        }
        
        return msg;
    }
    
    public static String saveAllENSDFByNuclide(MassChain data,String path,String extension)throws FileNotFoundException{
        int A=data.getA();
        String msg="";
        String nuclide="";
        String dirSeparator=dirSeparator();
        
        String tempPath=path+dirSeparator;
        

        extension=extension.trim();
        if(extension.isEmpty())
            extension="ens";
        
        String filename=tempPath+"A"+data.getA()+"_cover."+extension;
        
        Vector<String> cover=data.getComBlock();

        
        if(cover.size()>0){
            msg+=saveENSDF(cover,filename);
        }
        LinkedHashMap<String,Vector<ENSDF>> nuclideENSDFMap=new LinkedHashMap<String,Vector<ENSDF>>();
        
        for(int i=0;i<data.nENSDF();i++){
            ENSDF ens=data.getENSDF(i);
            if(ens.nLines()<=0)
                continue;
            
            nuclide=ens.nucleus().En()+A;
            if(!nuclideENSDFMap.containsKey(nuclide)) 
                nuclideENSDFMap.put(nuclide,new Vector<ENSDF>());
            
            nuclideENSDFMap.get(nuclide).add(ens);
        }
        
        for(String key:nuclideENSDFMap.keySet()){
 
            nuclide=key;
            filename=nuclide+"."+extension;
            filename=tempPath+filename;
            
            msg+=saveENSDFs(nuclideENSDFMap.get(key),filename);
        }
        
        return msg;
    }
    
    public static String setupEvaluation(MassChain data,String path)throws FileNotFoundException{
        return setupEvaluation(data,path,"old");
    }
    
    /*
     * Set up and create folders for a new mass-chain evaluation from the input mass-chain file
     */
    public static String setupEvaluation(MassChain data,String path,String extension)throws FileNotFoundException{
        String msg="";
        int A=data.getA();
        String nucName="";
        String dirSeparator=dirSeparator();
        
        String basePath0=path+dirSeparator+"A"+A+dirSeparator;
        try {
            File f=new File(path);
            String dirName=f.getName();          
            if(dirName.equals("A"+A)) {
            	basePath0=path+dirSeparator;
            }
            
        }catch(Exception e) {
        	
        }

        String basePath=basePath0+"finished"+dirSeparator;
        
        if(!Str.mkdir(basePath0) || !Str.mkdir(basePath)){
            msg+="*** can't create folder: "+basePath+"\n";
            return msg;
        }   
        
        msg+="   *** create top folder under path ---> path"+dirSeparator+"A"+A+dirSeparator+"finished\n\n";
        
        String filename=basePath+"A"+A+"_cover."+extension;
        
        Vector<String> cover=data.getComBlock();

        if(cover.size()>0)
            msg+=saveENSDF(cover,filename);
        
        for(int i=0;i<data.nBlocks();i++){
            Vector<String> block=data.getBlockAt(i);
            if(block.size()<=0 || block==cover)
                continue;
            
            Nucleus nucleus;
            try {
                nucleus = new Nucleus(block.get(0).substring(0,5));
                nucName=nucleus.En();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            filename=DSID2Filename(block.get(0).substring(9,39))+"."+extension;
            if(nucName.isEmpty()){
                filename=basePath+"A"+A+"_"+filename;               
            }else{
                String nucPath=basePath+nucName+A+dirSeparator;
                filename=nucPath+nucName+A+"_"+filename;
                if(!Str.isFileExist(nucPath+"new"))
                    Str.mkdirs(nucPath+"new");
                if(!Str.isFileExist(nucPath+"old"))
                    Str.mkdirs(nucPath+"old");
            }
            
            msg+=saveENSDF(block,filename);
        }   
        
        return msg;
        
    }
    
    /*
     * Set up and create folders for a new mass-chain evaluation from a list of nuclide name
     * Note that the mass number in the first name will be used and is forced to apply to 
     * the following no matther if their mass numbers are the same or not, because the
     * evaluation folders are created for a mass chain of nuclides with the same mass number and
     * it doesn't make any sense to create folders with different mass numbers.
     * So it is also ok that the following nuclide names have no mass number, as long as the first
     * one has.
     */
    public static String setupEvaluation(Vector<String> nuclideNames,String path)throws FileNotFoundException{
        String msg="";
        int A=-1;
        String nucName="";
        String dirSeparator=dirSeparator();
        
        Vector<Nucleus> nuclei=new Vector<Nucleus>();
        for(int i=0;i<nuclideNames.size();i++){
            nucName=nuclideNames.get(i);
            Nucleus nuc=parseNucleus(nucName);

            if(A<=0 && nuc.A()>0)
                A=nuc.A();
            
            if(nuc.Z()>0) 
            	nuclei.add(nuc);        
        }

        if(A<0 || nuclei.size()<=0){
            msg+="***  wrong nuclide name in the nuclide list or empty list.\n";
            return msg;
        }
        
        String basePath0=path+dirSeparator+"A"+A+dirSeparator;
        String basePath=basePath0+"finished"+dirSeparator;
        if(!Str.mkdir(basePath0) || !Str.mkdir(basePath)){
            msg+="*** can't create folder: "+basePath+"\n";
            return msg;
        }   

        
        for(int i=0;i<nuclei.size();i++){
            try {
                Nucleus nucleus =nuclei.get(i);
                nucName=nucleus.En();
                
                //System.out.println("  name="+nucName+" A="+nucleus.A()+" Z="+nucleus.Z()+" eName="+nucleus.En());
                
                if(nucName.trim().isEmpty())
                    continue;
                
                String nucPath=basePath+nucName+A+dirSeparator;
                if(!Str.isFileExist(nucPath+"new"))
                    Str.mkdirs(nucPath+"new");
                if(!Str.isFileExist(nucPath+"old"))
                    Str.mkdirs(nucPath+"old");
                
            }catch (Exception e) {
                e.printStackTrace();
            }

        }   
        
        return msg;
        
    }
    
    public static SDS2XDX scaleSDS(SDS2XDX s2x,float scale) {

        String s="",ds="";
        double x=-1,dxl=-1,dxu=-1;

        if(s2x==null)
            return null;
        
        x=s2x.x()*scale;
        
        if(x<1E-3)
            s=String.format("%.2E",x);
        else
            s=String.format("%.2f",x);
        
        ds=s2x.ds();
        if(!ds.isEmpty()) {
            
            dxl=s2x.dxl()*Math.abs(scale);
            dxu=s2x.dxu()*Math.abs(scale);
            
            if(Str.isNumeric(ds)) {                 
                ensdfparser.ensdf.XDX2SDS x2s=EnsdfUtil.x2s(x, dxl);
                s=x2s.s();
                ds=x2s.ds();
            }else if(dxu>0 && dxl>0){
                if(scale<0) {
                    double temp=dxl;
                    dxl=dxu;
                    dxu=temp;
                }
                
                ensdfparser.ensdf.XDX2SDS x2s=EnsdfUtil.x2s(x,dxu,dxl);                 
                s=x2s.s();
                ds="+"+x2s.dsu()+"-"+x2s.dsl();
            }else if(scale<0){          
                if(ds.startsWith("G"))
                    ds="L"+ds.substring(1);
                else if(ds.startsWith("L"))
                    ds="G"+ds.substring(1);
            }
        }
        
        return new SDS2XDX(s,ds);
    }
    
    //convert real value and uncertainty to ENSFD-style strings 
    public static XDX2SDS x2s(double x,double dxu,double dxl){
        XDX2SDS x2s=new XDX2SDS();
        x2s.setValues(x,dxu,dxl);
        return x2s;
    }
    
    public static XDX2SDS x2s(double x,double dx){
        XDX2SDS x2s=new XDX2SDS();
        x2s.setValues(x,dx);
        return x2s;
    }
    
    //convert ENSDF-style strings s and ds to real values 
    public static SDS2XDX s2x(String s,String ds){
        SDS2XDX s2x=new SDS2XDX();
        s2x.setValues(s,ds);
        return s2x;
    }
    
    public static float s2f(String str){
        try{
            return Float.parseFloat(str);
        }catch(NumberFormatException e){
            String s=str.trim();
            int i=s.indexOf('/');
            if(i==s.length()-1)
                s=s+" ";
            
            if(i>0){
                float a=Float.parseFloat(s.substring(0, i));
                float b=Float.parseFloat(s.substring(i+1));
                return a/b;
            }
        }   
        return -1;
    }
    
    public static int s2i(String str){
        try{
            return Integer.parseInt(str);
        }catch(NumberFormatException e){
        }   
        return -1;
    }
    
    public static boolean isRecordLine(String line,String recordType) {
    	try {
    		//" P" is a valid record type for prompt proton decay, so DO NOT trim
    		recordType=recordType.toUpperCase();
    		recordType=Str.rtrim(recordType);
    		
    		if(line.trim().length()<10 || recordType.trim().isEmpty() || recordType.length()>2)
    			return false;
    		
    		//col 5-8, e.g. "2cL ", " PN ", "   P" (prompt proton), "  DP" (delayed proton), "  G "
    		String lineType=getENSDFLineType(line);
    		char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		char c3=lineType.charAt(2);
    		char c4=lineType.charAt(3);
    		if(c1!=' ')
    			return false;
    		
    		if(recordType.length()==1) {
    			if(c2==' ' && c3==recordType.charAt(0) && c4==' ')
    				return true;
    			else
    				return false;
    		}else {
    			if(recordType.equals("PN") && recordType.equals(c2+c3+"") && c4==' ')
    				return true;
    			else if(c1==' ' && c2==' ' && (c3==' '||c3=='D') && "PNT".contains(c4+""))
    				return true;
    		}

    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    public static boolean isRecordLine(String line) {
    	try {
 
    		if(line.trim().length()<10)
    			return false;
    		
    		String lineType=getENSDFLineType(line);//col 5-8, e.g. "2cL ", " PN ", "   P" (prompt proton), "  G "
    		char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		char c3=lineType.charAt(2);
    		char c4=lineType.charAt(3);
    		if(c1!=' ')
    			return false;
    		
    		if(c2==' ') {
    			if(c3!=' ' &&c4==' ')
    				return true;
    			else if(c3==' ' && "PNT".contains(c4+""))
    				return true;
    		}else if("PN".equals(c2+c3+"") && c4==' ') {
    			return true;
    		}
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean isCommentLine(String line,String recordType) {
    	try {
 
    		if(line.trim().length()<10 || recordType.trim().isEmpty() || recordType.length()>2)
    			return false;
    		
    		String lineType=getENSDFLineType(line).toUpperCase();//col 5-8, e.g. "2cL ", " PN ", "   P" (prompt proton), "  G "
    		//char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		char c3=lineType.charAt(2);
    		char c4=lineType.charAt(3);
    		if(c2!='C')
    			return false;

    		if(recordType.trim().isEmpty())
    			return true;
    		
    		if(recordType.length()==1 && c3==recordType.charAt(0) && c4==' ') {
    			return true;
    		}else if(recordType.equals(c3+c4+"")) {//" P", "DP"
    			return true;
    		}
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean isCommentLine(String line) {
    	try {
 
    		if(line.trim().length()<10)
    			return false;
    		
    		String lineType=getENSDFLineType(line).toUpperCase();//col 5-8, e.g. "2cL ", " PN ", "   P" (prompt proton), "  G "
    		//char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		//char c3=lineType.charAt(2);
    		//char c4=lineType.charAt(3);
    		if(c2=='C')
    			return true;

    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean isDocumentLine(String line,String recordType) {
    	try {
 
    		if(line.trim().length()<10 || recordType.trim().isEmpty() || recordType.length()>2)
    			return false;
    		
    		String lineType=getENSDFLineType(line).toUpperCase();//col 5-8, e.g. "2cL ", " PN ", "   P" (prompt proton), "  G "
    		//char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		char c3=lineType.charAt(2);
    		char c4=lineType.charAt(3);
    		if(c2!='D')
    			return false;

    		if(recordType.trim().isEmpty())
    			return true;
    		
    		if(recordType.length()==1 && c3==recordType.charAt(0) && c4==' ') {
    			return true;
    		}else if(recordType.equals(c3+c4+"")) {//" P", "DP"
    			return true;
    		}
    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static boolean isDocumentLine(String line) {
    	try {
 
    		if(line.trim().length()<10)
    			return false;
    		
    		String lineType=getENSDFLineType(line).toUpperCase();//col 5-8, e.g. "2cL ", " PN ", "   P" (prompt proton), "  G "
    		//char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		//char c3=lineType.charAt(2);
    		//char c4=lineType.charAt(3);
    		if(c2=='D')
    			return true;

    	}catch(Exception e) {
    		
    	}
    	return false;
    }
    
    public static char[] getLineTypeChars(String line) {
    	try {
    		if(line.length()<10 || line.substring(0,5).trim().isEmpty())
    			return null;
    		
    		String lineType=getENSDFLineType(line).toUpperCase();//col 5-8, e.g. "2cL ", " PN ", "   P" (prompt proton), "  G "
    		char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		char c3=lineType.charAt(2);
    		char c4=lineType.charAt(3);
    		
    		char[] array=new char[4];
    		array[0]=c1;
    		array[1]=c2;
    		array[2]=c3;
    		array[3]=c4;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    public static ENSDF getENSDF(String ensFilePath) {
    	try {
    		File f=new File(ensFilePath);
    		if(f.exists()) {
                MassChain data=new MassChain();
                data.load(f);
                if(data.nENSDF()>=1) 
                	return data.getENSDF(0);
  
     		}
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    public static String getENSDFLineAt(ENSDF ens,int row) {
        try {
            return ens.lineAt(row);
        }catch(Exception e) {
            return null;
        }
    }
    
    public static String getLineAt(String filepath,int row) {
        try {
            Vector<String> lines=Str.readFile(new File(filepath));
            return lines.get(row);
        }catch(Exception e) {
            return null;
        }
    }
    
    /*
     * type=col 5-8 (col starts from 0)
     */
    public static String getENSDFLineType(String line) {
        String lineType="";
        try{
        	if(line.length()<10)
        		line=line+"                      ";
            lineType=line.substring(5,9);
            
        }catch(Exception e){
        	//System.out.println("line="+line+"$");
            e.printStackTrace();
        }
    
        return lineType;
    }
    
    /*
     * type=col 7, "LGBAEPNHX" 
     * type="PN" for "PN" record
     * type="P"+col 8 for prompt particle decay
     * 
     */
    public static String getENSDFRecordType(String line) {
        String recordType="";
        try{
        	String lineType=getENSDFLineType(line);
        	char c3=lineType.charAt(2);
        	char c4=lineType.charAt(3);
        	if(c3!=' ') {
        		recordType=c3+""+c4+"";
            
        		recordType=recordType.trim();
        	}else if("PNDT".contains(c4+"")) {
        		//recordType="P"+c4;//prompt
        		recordType=" "+c4;
        	}

            
        }catch(Exception e){
            e.printStackTrace();
        }
    
        return recordType;
    }
    
    public static double[] symmetrize(double x,double dxu,double dxl){
  	  double[] out=new double[2];
  	  out[0]=x;out[1]=dxu;
  	  if(dxu>=0 && dxl>=0 && dxu!=dxl){
  		  double f=(1-2.0/Math.PI)*(dxu-dxl)*(dxu-dxl)+dxu*dxl;
  		  out[0]=x+0.64*(dxu-dxl);
  		  out[1]=Math.sqrt(f);
  	  }
  	  
  	  return out;
    }
    
    /*
     * trim an ENSDF line by removing heading spaces in each data field
     * option="L","G","A","B","E","P"
     *       ="N" for n
     * must call initENSDFRecordMap() before use
     * line must be an ENSDF line
     */
    public static String trimENSDFLine(String line){
        String out=line;
        String lineType="";
        
        try{
            if(out.charAt(5)!=' ')
                return out;
            if(out.charAt(8)!=' ')
                lineType="DP";
            else
                lineType=line.substring(5,9).trim();
            
            Vector<EntryInfo> recordsV=null;
            try{
                recordsV=recordFieldsVMap.get(lineType);
                recordsV.get(0);
            }catch(Exception e){
                if(recordFieldsVMap==null || recordFieldsVMap.size()==0){
                    initENSDFRecordMap();
                    recordsV=recordFieldsVMap.get(lineType);
                }
            }
            
            int pos=-1,len=0;
            String part="",name="";
            for(int i=0;i<recordsV.size();i++){
                name=recordsV.get(i).name;
                
                pos=recordsV.get(i).pos;
                len=recordsV.get(i).len;
                part=out.substring(pos,pos+len).trim();
                
                if("J,RI".contains(name) && part.length()<len)
                    out=out.substring(0,pos)+String.format("%-"+len+"s", " "+part)+out.substring(pos+len);
                else
                    out=out.substring(0,pos)+String.format("%-"+len+"s", part)+out.substring(pos+len);
                
                //System.out.println("  record="+recordsV.get(i).name+"   pos="+pos+"  len="+len);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return out;
    }
    
    /*
     * truncate lines of an ENSDF dataset to desired contents
     * inputs:
     *     linesV: all lines of an ENSDF dataset to be truncated
     *     nLevels: number of levels to be included in the truncated lines
     *     skipComments: true to skip all comment lines
     */
    public static Vector<String> truncateDataset(Vector<String> linesV,int nLevels,boolean skipComments){
    	Vector<String> out=new Vector<String>();
    	try {
    		int nLev=0;
    		for(int i=0;i<linesV.size();i++) {
    			String line=linesV.get(i);
    			String lineType=line.substring(5,9);//e.g., "  L " for L record line, "2cL " for level comment line
    			if(lineType.equals("  L ")) {
    				nLev++;
    				if(nLev>nLevels)
    					break;
    			}else {
    				//char c1=lineType.charAt(0);
    				char c2=lineType.charAt(1);
    				//char c3=lineType.charAt(2);
    				//char c4=lineType.charAt(3);
    				if(c2==' ') {
    					//other record lines (H,P,G,D,..)or,
    					//continuation lines
    					//do nothing
    				}else if(skipComments && c2!='P') {
    					//comment or document lines, except for PN record line
    					continue;
    				}
    				
    			}
    			
    			out.add(line);
  
    		}
    		
    		//return empty if there is no level at all
    		if(nLev==0)
    			out=new Vector<String>();
    		
    	}catch(Exception e) {
    		
    	}
    	return out;
    }
    
    
     public static double T12UnitMultiplier(String unit){
            double mult=1;
            
            unit=unit.toUpperCase().trim();
            
            if( unit.equals("MEV") ) mult=4.557E-22;
            else if( unit.equals("KEV") ) mult=4.557E-19;
            else if( unit.equals("EV") ) mult=4.557E-16;
            else if( unit.equals("FS") ) mult=1e-15; 
            else if( unit.equals("PS") ) mult=1e-12; 
            else if( unit.equals("NS") ) mult=1e-9; 
            else if( unit.equals("US") || unit.equals("|MS") ) mult=1e-6; 
            else if( unit.equals("MS") ) mult=1e-3; 
            else if( unit.equals("S") ) mult=1; 
            else if( unit.equals("M") ) mult=60; 
            else if( unit.equals("H") ) mult=60*60;
            else if( unit.equals("D") ) mult=24*60*60;  
            else if( unit.equals("Y") ) mult=365.25*24*60*60;  
            else if( unit.equals("KY") ) mult=(10E+3)*365.25*24*60*60;  
            else if( unit.equals("MY") ) mult=(10E+6)*365.25*24*60*60;  
            else if( unit.equals("GY") ) mult=(10E+9)*365.25*24*60*60;
            
            return mult;                         
     }
         
    public static void initENSDFRecordMap(){
        Vector<EntryInfo> recordsV=null;
        
        recordFieldsVMap.clear();
        
        //Level
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(     "E",  9, 10, "L" ));
        recordsV.add(new EntryInfo(    "DE", 19,  2, "L" ));
        recordsV.add(new EntryInfo(     "J", 21, 18, "L" ));
        recordsV.add(new EntryInfo(     "T", 39, 10, "L" ));
        recordsV.add(new EntryInfo(    "DT", 49,  2, "L" ));
        recordsV.add(new EntryInfo(     "L", 55,  9, "L" ));
        recordsV.add(new EntryInfo(     "S", 64, 10, "L" ));
        recordsV.add(new EntryInfo(    "DS", 74,  2, "L" ));
        recordsV.add(new EntryInfo(     "F", 76,  1, "L" ));
        recordsV.add(new EntryInfo(    "MS", 77,  2, "L" ));
        recordsV.add(new EntryInfo(     "Q", 79,  1, "L" ));
        recordFieldsVMap.put("L",recordsV);
        
        //Gamma
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(     "E",  9, 10, "G" ));
        recordsV.add(new EntryInfo(    "DE", 19,  2, "G" ));
        recordsV.add(new EntryInfo(    "RI", 21,  8, "G" ));
        recordsV.add(new EntryInfo(   "DRI", 29,  2, "G" ));
        recordsV.add(new EntryInfo(     "M", 31, 10, "G" ));
        recordsV.add(new EntryInfo(    "MR", 41,  8, "G" ));
        recordsV.add(new EntryInfo(   "DMR", 49,  6, "G" ));
        recordsV.add(new EntryInfo(    "CC", 55,  7, "G" ));
        recordsV.add(new EntryInfo(   "DCC", 62,  2, "G" ));
        recordsV.add(new EntryInfo(    "TI", 64, 10, "G" ));
        recordsV.add(new EntryInfo(   "DTI", 74,  2, "G" ));
        recordsV.add(new EntryInfo(     "F", 76,  1, "G" ));
        recordsV.add(new EntryInfo(  "COIN", 77,  1, "G" ));
        recordsV.add(new EntryInfo(     "Q", 79,  1, "G" ));
        recordFieldsVMap.put("G",recordsV);

        //Beta
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(     "E",  9, 10, "B" ));
        recordsV.add(new EntryInfo(    "DE", 19,  2, "B" ));
        recordsV.add(new EntryInfo(    "IB", 21,  8, "B" ));
        recordsV.add(new EntryInfo(   "DIB", 29,  2, "B" ));
        recordsV.add(new EntryInfo( "LOGFT", 41,  8, "B" ));
        recordsV.add(new EntryInfo("DLOGFT", 49,  6, "B" ));
        recordsV.add(new EntryInfo(     "F", 76,  1, "B" ));
        recordsV.add(new EntryInfo(    "UN", 77,  2, "B" ));
        recordsV.add(new EntryInfo(     "Q", 79,  1, "B" ));
        recordFieldsVMap.put("B",recordsV);

        //EC/B+
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(     "E",  9, 10, "E" ));
        recordsV.add(new EntryInfo(    "DE", 19,  2, "E" ));
        recordsV.add(new EntryInfo(    "IB", 21,  8, "E" ));
        recordsV.add(new EntryInfo(   "DIB", 29,  2, "E" ));
        recordsV.add(new EntryInfo(    "IE", 31,  8, "E" ));
        recordsV.add(new EntryInfo(   "DIE", 39,  2, "E" ));
        recordsV.add(new EntryInfo( "LOGFT", 41,  8, "E" ));
        recordsV.add(new EntryInfo("DLOGFT", 49,  6, "E" ));
        recordsV.add(new EntryInfo(    "TI", 64, 10, "E" ));
        recordsV.add(new EntryInfo(   "DTI", 74,  2, "E" ));
        recordsV.add(new EntryInfo(     "F", 76,  1, "E" ));
        recordsV.add(new EntryInfo(    "UN", 77,  2, "E" ));
        recordsV.add(new EntryInfo(     "Q", 79,  1, "E" ));
        recordFieldsVMap.put("E",recordsV);

        //Alpha
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(     "E",  9, 10, "A" ));
        recordsV.add(new EntryInfo(    "DE", 19,  2, "A" ));
        recordsV.add(new EntryInfo(    "IA", 21,  8, "A" ));
        recordsV.add(new EntryInfo(   "DIA", 29,  2, "A" ));
        recordsV.add(new EntryInfo(    "HF", 31,  8, "A" ));
        recordsV.add(new EntryInfo(   "DHF", 39,  2, "A" ));
        recordsV.add(new EntryInfo(     "F", 76,  1, "A" ));
        recordsV.add(new EntryInfo(     "Q", 79,  1, "A" ));
        recordFieldsVMap.put("A",recordsV);

        
        //beta/EC-delayed particle
        //also for prompt particle decay is no delay
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(     "E",  9, 10, "DP"));
        recordsV.add(new EntryInfo(    "DE", 19,  2, "DP"));
        recordsV.add(new EntryInfo(    "IP", 21,  8, "DP"));
        recordsV.add(new EntryInfo(   "DIP", 29,  2, "DP"));
        recordsV.add(new EntryInfo(    "EI", 31,  8, "DP"));//for intermediate nuclide
        recordsV.add(new EntryInfo(     "T", 39, 10, "DP"));
        recordsV.add(new EntryInfo(    "DT", 49,  2, "DP"));
        recordsV.add(new EntryInfo(     "L", 55,  9, "DP"));
        recordsV.add(new EntryInfo(     "F", 76,  1, "DP"));
        recordsV.add(new EntryInfo(  "COIN", 77,  1, "DP" ));
        recordsV.add(new EntryInfo(     "Q", 79,  1, "DP"));
        recordFieldsVMap.put("DP",recordsV);
        
        Vector<EntryInfo> tempV=new Vector<EntryInfo>();
        tempV.addAll(recordsV);
        recordFieldsVMap.put("PP",tempV);//prompt
        
        //Parent
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(     "E",  9, 10, "P" ));
        recordsV.add(new EntryInfo(    "DE", 19,  2, "P" ));
        recordsV.add(new EntryInfo(     "J", 21, 18, "P" ));
        recordsV.add(new EntryInfo(     "T", 39, 10, "P" ));
        recordsV.add(new EntryInfo(    "DT", 49,  2, "P" ));
        recordsV.add(new EntryInfo(    "QP", 64, 10, "P" ));
        recordsV.add(new EntryInfo(   "DQP", 74,  2, "P" ));
        recordsV.add(new EntryInfo(   "ION", 76,  4, "P" ));
        recordFieldsVMap.put("P",recordsV);
        
        //Q-Value
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(    "Q-",  9, 10, "Q" ));
        recordsV.add(new EntryInfo(   "DQ-", 19,  2, "Q" ));
        recordsV.add(new EntryInfo(    "SN", 21,  8, "Q" ));
        recordsV.add(new EntryInfo(   "DSN", 29,  2, "Q" ));
        recordsV.add(new EntryInfo(    "SP", 31,  8, "Q" ));
        recordsV.add(new EntryInfo(   "DSP", 39,  2, "Q" ));
        recordsV.add(new EntryInfo(    "QA", 41,  8, "Q" ));
        recordsV.add(new EntryInfo(   "DQA", 49,  6, "Q" ));
        recordsV.add(new EntryInfo(  "QREF", 55, 25, "Q" ));
        recordFieldsVMap.put("Q",recordsV);
        
        //Norm 
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(    "NR",  9, 10, "N" ));
        recordsV.add(new EntryInfo(   "DNR", 19,  2, "N" ));
        recordsV.add(new EntryInfo(    "NT", 21,  8, "N" ));
        recordsV.add(new EntryInfo(   "DNT", 29,  2, "N" ));
        recordsV.add(new EntryInfo(    "BR", 31,  8, "N" ));
        recordsV.add(new EntryInfo(   "DBR", 39,  2, "N" ));
        recordsV.add(new EntryInfo(    "NB", 41,  8, "N" ));
        recordsV.add(new EntryInfo(   "DNB", 49,  6, "N" ));
        recordsV.add(new EntryInfo(    "NP", 55,  7, "N" ));
        recordsV.add(new EntryInfo(   "DNP", 62,  2, "N" ));
        recordFieldsVMap.put("N",recordsV);

        //PNorm 
        recordsV=new Vector<EntryInfo>();
        recordsV.add(new EntryInfo(  "NRBR",  9, 10, "PN" ));
        recordsV.add(new EntryInfo( "DNRBR", 19,  2, "PN" ));
        recordsV.add(new EntryInfo(  "NTBR", 21,  8, "PN" ));
        recordsV.add(new EntryInfo( "DNTBR", 29,  2, "PN" ));
        recordsV.add(new EntryInfo(  "NBBR", 41,  8, "PN" ));
        recordsV.add(new EntryInfo( "DNBBR", 49,  6, "PN" ));
        recordsV.add(new EntryInfo(    "NP", 55,  7, "PN" ));
        recordsV.add(new EntryInfo(   "DNP", 62,  2, "PN" ));
        recordFieldsVMap.put("PN",recordsV);

    }

	public static boolean isExactUnity(String s, String ds) {
		try {
			float f=Float.parseFloat(s);
			if(ds.isEmpty() && f==1.0 && s.startsWith("1")) {	
				if(s.startsWith("1.")) {
					s=s.substring(2).trim();
					if(s.isEmpty()) 
						return true;
					
					if(s.startsWith("0") && s.replace("0","").isEmpty())
						return true;
					
				}else if(s.length()==1)
					return true;
			}
		}catch(Exception e) {
			
		}
		
		return false;
	}
	public static boolean isUnity(String s, String ds) {
		return isUnity(s,ds,1E-4);
	}
	
	public static boolean isUnity(String s, String ds,double precision) {
		try {
			float f=Float.parseFloat(s);
			if(precision>0.1)//precision set to be 0.1 if the input is worse than 0.1
				precision=0.1;
			
			if(ds.isEmpty() && Math.abs(f-1.0)<precision) 
				return true;
			
		}catch(Exception e) {
			
		}
		
		return false;
	}
	
	public static boolean isUseTIForBranching(Gamma gam) {
		try {
			String tis=gam.TIS();
			String dtis=gam.DTIS();
			String ris=gam.RIS();
			String dris=gam.DRIS();
			
			if(tis.isEmpty() || !Str.isNumeric(tis))
				return false;
			
			//if tis has numerical uncertainty, always use TI
			if(ris.isEmpty() || Str.isNumeric(dtis))
				return true;
			
			//now both ris and tis are not empty and dtis is non-numeric
			if(Str.isNumeric(ris)) 
				return false;

			//now both dris and dtis are non-numeric
			if(!dris.isEmpty() || dtis.isEmpty())//dris is AP, CA, LT, GT, ..., or dris=empty and dtis=empty, use TI
				return true;
			
			//dris=empty, but dtis is AP, CA, LT, GT, ...
			//return false;
            
		}catch(Exception e) {
			
		}
		return false;
	}
}

class RecordComparator implements Comparator<Object>{
	boolean byEnergy=true,byRI=false;
	boolean isAscending=true;
    int valueIfOneLessThanTwo=-1;
    
	public RecordComparator(String dataType, String order) {
		dataType=dataType.toUpperCase().trim();
		if(dataType.equals("RI") || dataType.startsWith("INTEN")) {
			byEnergy=false;
			byRI=true;
		}
		
		order=order.toUpperCase().trim();
		if(order.startsWith("DESC"))
			isAscending=false;
		      
        if(!isAscending) {
        	//actual value: one < two
        	//for descending order, two shoul dbe placed before one
        	//while comparator always sort entries by "ascending" that compare() defines: -1 for one first, 1 for two first
        	//if we want two first when one<two, compare() should return 1
        	valueIfOneLessThanTwo=1;
        }
	}
	
    public int compare(Object one,Object two){

        if(byRI)
        	return compareRI(one,two);
        
        return compareEnergy(one,two);
    }
    
    private int compareEnergy(Object one,Object two){
        Record a=(Record)one;
        Record b=(Record)two;
        int out=0;
        

        if(a.EF()<b.EF())
        	out=valueIfOneLessThanTwo;
        else if(a.EF()>b.EF())
        	out=-valueIfOneLessThanTwo;
        else{           
            if(a.isGamma()) {
                String IL1=((Gamma)a).ILS();
                String IL2=((Gamma)b).ILS();
                float el1=-1,el2=-1;
                if(Str.isNumeric(IL1)) el1=Float.parseFloat(IL1);
                if(Str.isNumeric(IL2)) el2=Float.parseFloat(IL2);
                
                if(el1<el2)
                    out=valueIfOneLessThanTwo;
                else if(el1>el2)
                    out=-valueIfOneLessThanTwo;
                else
                    out=0;

            }else
                out=0;

        }
        
        return out;
    }
    
    private int compareRI(Object one,Object two){
        Record a=(Record)one;
        Record b=(Record)two;
        int out=0;
        
        try {
        	double ria=-1,rib=-1;
        	if(Str.isNumeric(a.RIS()))
        		ria=Double.parseDouble(a.RIS());
        	if(Str.isNumeric(b.RIS()))
        		rib=Double.parseDouble(b.RIS());
        	
            if(ria<rib)
            	out=valueIfOneLessThanTwo;
            else if(ria>rib)
            	out=-valueIfOneLessThanTwo;
            else{           
                if(a.isGamma()) {
                    String IL1=((Gamma)a).ILS();
                    String IL2=((Gamma)b).ILS();
                    float el1=-1,el2=-1;
                    if(Str.isNumeric(IL1)) el1=Float.parseFloat(IL1);
                    if(Str.isNumeric(IL2)) el2=Float.parseFloat(IL2);
                    if(el1<el2)
                        out=valueIfOneLessThanTwo;
                    else if(el1>el2)
                        out=-valueIfOneLessThanTwo;
                    else
                        out=0;
                }else
                    out=0;

            }
        }catch(Exception e) {
        	
        }

        
        return out;
    }
}