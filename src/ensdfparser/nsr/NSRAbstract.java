package ensdfparser.nsr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import ensdfparser.ensdf.Author;
import ensdfparser.ensdf.DatasetID;
import ensdfparser.ensdf.Nucleus;
import ensdfparser.nds.util.Str;

public class NSRAbstract {
	
	public String keyno="";
	public boolean isMeasured=true;

	public HashMap<String,String> NSRInfoMap=new HashMap<String,String>();

	public Vector<Keyword> keywordsV=new Vector<Keyword>();
	public Vector<Selector> selectorsV=new Vector<Selector>();
	public Vector<Reference> referencesV=new Vector<Reference>();//one keynumber could has two references (double publication)
	public Reference translatedRef=null;
	
	public NSRAbstract() {
	}
	
	public String keyno() {return keyno;}
	public boolean isMeasured() {return isMeasured;}   
	
	public String CODEN(){return get("CODEN");}
	public String Title(){return get("TITLE");}
	public String Reference(){return get("REFERENCE");}
	public String Authors(){return get("AUTHORS");}
	public String Keywords(){return get("KEYWORDS");}
	public String Selectors(){return get("SELECTORS");}
	public String History(){return get("HISTORY");}
	public String DOI() {return get("DOI");}
	
	public Reference getTranslatedRef() {return translatedRef;}
	
	private String get(String key) {
	    try {
	        String v=NSRInfoMap.get(key);
	        if(v==null)
	            return "";
	        
	        return v.trim();
	    }catch(Exception e) {
	        
	    }
	    return "";
	}
	public void parseAll() {
		String refString=Reference();
		if(refString==null)
		    refString="";
		else
		    refString=refString.trim();
		
		String authors=Authors();
		if(authors==null)
		    authors="";
		else
		    authors=authors.trim();
		
		//if(authors.startsWith("K. T. Haak"))
		//System.out.println(" keyno="+keyno+" ref="+refString+" authors="+authors);
		
		if(refString.length()>0 || authors.length()>0) {	
            
			Vector<Reference > refsV=parseReferences();
			

			for(Reference tempRef:refsV) {
				if(referencesV.contains(tempRef))
					continue;
				
	            //if(keyno.toUpperCase().equals("1959PE27"))
            	//	System.out.println(tempRef.journalName+"  "+tempRef.refS);

	            
				boolean toAdd=true;
				for(Reference ref:referencesV) {
					if(tempRef.nsrKeynumber.equalsIgnoreCase(ref.nsrKeynumber) && 
							(tempRef.journalName.isEmpty()||tempRef.journalName.equals(ref.journalName))) {
						toAdd=false;
						ref.merge(tempRef);
						break;
					}
				}
				
				if(toAdd)
					referencesV.add(tempRef);
			}
			
			
			Vector<Author> authorsV=parseAuthors();
			
			for(Reference ref:referencesV) {
			    if(ref.authorsV.isEmpty())
			        ref.authorsV.addAll(authorsV);
			}
		}

		String s=Selectors();
		if(s==null)
		    s="";
		else
		    s=s.trim();
		
		if(s.length()>0) {
			
		}
	}
	private Vector<Author> parseAuthors() {
		return parseAuthors(Authors());
	}
	
	public static Vector<Author> parseAuthors(String authors) {
		return parseAuthors(authors,"[,]+|\\sand\\s|,and\\s|\\sAND\\s|,AND\\s");
	}
	public static Vector<Author> parseAuthors(String authors,String delims) {
	    Vector<Author> authorsV=new Vector<Author>();
	    try {
	        String[] temp=authors.split(delims);
	        if(temp.length==0)
	            return authorsV;
	        
	        String s="",s1="",s2="";
	        for(int i=0;i<temp.length;i++) {
	            s=temp[i].trim();
	            if(s.isEmpty())
	                continue;
	            
	            Author a=new Author();
	            a.name=s;//e.gl,  H.S.Jung
	            
	            //S.K.Allison, P.G.Murphy, E.Norbeck, Jr.
	            //or S.K.Allison, P.G.Murphy, E.Norbeck Jr.
	            //or S.K.Allison, P.G.Murphy, E.Norbeck, Jr
	            //or S.K.Allison, P.G.Murphy, E.Norbeck Jr
	            
	            s1=s.toUpperCase();
	            
	            String postfix="";
	            if(s1.endsWith("JR.")) {
	            	postfix=s.substring(s.length()-3);
	            	s=s.substring(0,s.length()-3).trim();
	            }else if(s1.endsWith("JR")) {
	            	postfix=s.substring(s.length()-2);
	            	s=s.substring(0,s.length()-2).trim();
	            }
	            	
	            //Jr. in S.K.Allison, P.G.Murphy, E.Norbeck, Jr.
	            //or Jr in S.K.Allison, P.G.Murphy, E.Norbeck, Jr
	            if(s.isEmpty() && !postfix.isEmpty()) {
	            	if(authorsV.size()>0) {
	                    a=authorsV.lastElement();
	                    s=a.lastName+" "+postfix;
	                    s=s.trim();
	                    a.lastName=s;
	            	}
	            	
	            	continue;
	            }
	            
	            String delim="";
	            if(s.contains(".")) {
	                delim=".";  
	            }else if(s.contains(" ")){//not initial
	                delim=" ";
	            }
	            
            
                int n1=s.lastIndexOf(delim);  
                if(delim.isEmpty()) {
                	s1="";
                	s2=s;
                }else {
                    s1=s.substring(0,n1+1).trim();
                    s2=s.substring(n1+1).trim();
                }
                
                s2=s2+" "+postfix;
                
                a.lastName=s2.trim();
                
                //if(s2.toUpperCase().startsWith("FRIED")) System.out.println(s2+"  s="+s+" authors="+Authors());
                
                n1=s1.indexOf(delim);
                if(s1.isEmpty() || n1<0 || n1==s1.length()-1) {
                    a.firstName=s1;
                }else {
                    a.firstName=s1.substring(0,n1+1).trim();
                    a.middleName=s1.substring(n1+1).trim();
                }	            
                    
                if(a.lastName.toUpperCase().startsWith("COLLABOR")) {
                	if(a.firstName.toUpperCase().equals("FOR"))
                		a.lastName=s.substring(3).trim();
                	else
                		a.lastName=s;
                	
                	a.firstName="";
                	a.middleName="";
                }
                
                authorsV.add(a);
	        }
	    }catch(Exception e) {
	        
	    }
	    
	    return authorsV;
	}
	
	private Vector<Reference> parseReferences() {
	    //<REFRENCE>Phys.Lett. 218B, 137 (1989)
	    //REFRENCE>Phys.Rev. C 105, L051302 (2022)
	    Vector<Reference> refsV=new Vector<Reference>();
	    
	    try {
	        String referencesString=Reference();     
	        String codenString=CODEN();
	        String doi=DOI();
	        String yearS="";
	        
	        //reference type
	        String type="";//JOUR,CONF,REPT,THESIS,PC (private communication), PREPRINT   
	        Reference ref=null;
	        String[] temp;
	        
	        Reference tempRef=this.parseCODENString(codenString);
	        
	        //if(codenString.contains("JNABA")) 
	        //	System.out.println("### "+codenString+"  ref="+referencesString);	        	
	        
	        
	        if(referencesString.isEmpty()) {
	        	//there are cases for which there is no reference string but there is reference information in the CODEN string
	        	ref=new Reference();
	        	
	            ref.nsrKeynumber=keyno;
	            ref.articleTitle=Title();
	            
	            ref.journalName=tempRef.journalName;
	            ref.nameFromCODEN=tempRef.nameFromCODEN;
	            
	            ref.type=tempRef.type;
	            ref.volume=tempRef.volume;
	            ref.page=tempRef.page;
	            ref.pageStr=tempRef.pageStr;
	            ref.pageMark=tempRef.pageMark;
	            
	            ref.doi=doi;
	            	           
	            if(!keyno.isEmpty() && keyno.length()==8) {
	            	yearS=keyno.substring(0,4);
	            	if(Str.isInteger(yearS))
	            		ref.year=Integer.parseInt(yearS);
	            }
	         
	            String s=ref.journalName;
	            s=s.trim();
	            if(s.startsWith(","))
	            	s=s.substring(1).trim();
	            
	            ref.citeS=ref.journalName;
	            if(ref.journalName.length()>0 && ref.volume.length()>0 && ref.page.length()>0 && ref.year>0) {
	            	String pageS=ref.page;
	            	if(ref.pageMark.length()>0) {
	            		if(ref.pageStr.startsWith(ref.pageMark))
	            			pageS=ref.pageMark+pageS;
	            		else
	            			pageS=pageS+ref.pageMark;
	            	}
	            	
	            	s+=" "+ref.volume+", "+pageS+" ("+yearS+")";
	                ref.citeS=s;
	            }
	            
	            ref.refS=ref.citeS;
	            if(Title().length()>0) {
	            	ref.refS=Title()+", "+s;
	            }
	            
	            refsV.add(ref);
	            return refsV;
	        }else {
		        referencesString=referencesString.trim();		        
		        temp=referencesString.split("[;]+");
		        
		        String refString="";

		        
		        Vector<String> tempV=new Vector<String>();
		        for(int i=0;i<temp.length;i++) {
		            refString=temp[i].trim();
		            if(refString.isEmpty())
		                continue;
		            
		        	int n=refString.lastIndexOf(",");
		        	boolean done=false;
		        	if(n>0) {
		                String s1=refString.substring(0,n).trim();//supposed to be string of journal name+volume if journal
		                String s2=refString.substring(n+1).trim();//supposed to be page+year if journal
		                
			            int b1=s1.indexOf("(");
			            int b2=s1.indexOf(")");
			            
			            //if(b1>0 &&  b2>b1) System.out.println(refString+"  b1="+b1+" b2="+b2+" "+s1.substring(b1+1,b2).trim()+"*");
			            
			            if(b1>0 && b1<b2 && Str.isInteger(s1.substring(b1+1,b2).trim()) ){
			            	String tempS=s1.substring(b1+1,b2).trim();
			            	if(tempS.length()==4) {
			            		s1=refString.substring(0,b2+1).trim();
			            		s2=refString.substring(b2+1).trim();
			            		//e.g., Zhur.Eksptl.i Teoret.Fiz. 38, 1654 (1960); Soviet Phys.JETP 11, 1191 (1960)
			            		if(s2.startsWith(","))
			            			s2=s2.substring(1).trim();
			            		
			            		tempV.add(s1);
			            		tempV.add(s2);
			            		done=true;
			            	}
			            }
		        	}
		        	
		        	if(!done)
		        		tempV.add(refString);

		        }
		        
		        boolean hasMatchCODEN=false;
		        
		        for(int i=0;i<tempV.size();i++) {
		            refString=tempV.get(i).trim();
		            if(refString.isEmpty())
		                continue;


		            ref=parseNSRJournalString(refString);
		            
		            if(ref!=null) {
		            	//ref.journalName=refString;//including volume, page, year
               
	                    ref.nsrKeynumber=keyno;
	                    ref.articleTitle=Title();
	                    
	                    //unify journal name.
	                    if(tempRef!=null) {                    	 
	                    	ref.nameFromCODEN=tempRef.nameFromCODEN;
	                    	
	                    	//There are cases where the CODEN information is incomplete: e.g., JOUR AAFPA 128, for 1963GR08
	                    	//resulting in the volume and page from CODEN and from journal string don't match
	                    	if(tempV.size()==1 || (ref.volume.equals(tempRef.volume) && ref.page.equals(tempRef.page)) ) {
	                    		
	                    		if(ref.journalName.isEmpty())//add 3/19/2026 to use original journal name
	                    			ref.journalName=tempRef.journalName;
	                    		
	                    		hasMatchCODEN=true;
	                    	}
	                    }
	                   
	                    
	                    //debug
	                    //if(refString.toUpperCase().contains("NATURE(LONDON) 620")){
	                    //if(ref.nsrKeynumber.equalsIgnoreCase("2023KO15")) {
	                    //System.out.println("##########################"+ref.nsrKeynumber+"  "+ref.journalName+"   "+tempRef.journalName+"   ");
	                    //System.out.println("          "+ref.volume+"    "+ref.page+"     temp: "+tempRef.volume+"   "+tempRef.page);
	                    //}
	                    
	                    
	                    ref.type=type;
	                    if(type.equals("JOUR") && (ref.year<=0||ref.page.isEmpty()) ) {
	                        ref.type="";
	                    }
	                    
	                    ref.citeS=refString;
	                    ref.refS=ref.citeS;
	                    if(Title().length()>0&&refString.length()>0)
	                    	ref.refS=Title()+" ,"+refString;
	                    
	                    ref.doi=doi;
	                    refsV.add(ref);
		            }

		        }
		        
		        //No match has been found in the journal string (multiple versions of journals) that matches the CODEN
		        //assign the coden journal name to the first ref
		        if(!hasMatchCODEN && tempRef!=null && refsV.size()>0) {
		        	Reference ref0=refsV.get(0);
		        	if(ref0.journalName.isEmpty())//add 3/19/2026 to use original journal name
		        		ref0.journalName=tempRef.journalName;
		        }
	        }
	        
         
	    }catch(Exception e) {
	        e.printStackTrace();
	    }
	    
	    
	    if(refsV.size()>1) {
	    	int translatedIndex=-1;
	    	ArrayList<String> foreignNames=new ArrayList<String>(Arrays.asList(
	    			"NAUK","FIZ","SSSR","AKAD","YAD"
	    			));
	    	ArrayList<String> translatedNames=new ArrayList<String>(Arrays.asList(
	    			"SOV.","SOVIET","USSR"
	    			));
	    	for(int i=0;i<refsV.size();i++) {
	    		Reference ref=refsV.get(i);
	    		
	    		//note that ref.journalName is the name from CODEN if exising
	    		//ref.citeS stores the original source string of the reference
	    		String journalSource=ref.citeS.toUpperCase();
	    		if(Str.containAnyPattern(journalSource, foreignNames)) {
	    			if(i>0)
	    				translatedIndex=i-1;
	    			else
	    				translatedIndex=i+1;
	    			
	    			break;
	    		}else if(Str.containAnyPattern(journalSource, translatedNames)) {
	    			translatedIndex=i;
	    			
	    			break;
	    		}
	    	}
	    	
	    	if(translatedIndex>=0) {
	    		Reference ref=refsV.get(translatedIndex);
	    		ref.isTranslated=true;
	    		//refsV.remove(ref);
	    		//refsV.add(0, ref);
	    		
	    		translatedRef=ref;
	    	}
	    }
	    
	    return refsV;
	}
	
	/*
	 * Note that there is no year in a CODEN string
	 * Year needs to be set afterwards.
	 * 
	 * Only journal name, volume and page information are
	 * extracted for the returned Reference object
	 */
	public Reference parseCODENString(String codenString) {
		try {
			
			Reference ref=new Reference();
			
			//example journal reference: JOUR CPCHC 45 030003
			//JOUR BAPSA 8 318,G9,Reber for Bull.Am.Phys.Soc. 8, No.4, 318, G9 (1963); Reber is the last name of the first author
			//JOUR PRVCA 90 014320 for Phys.Rev. C 90, 014320 (2014)
			//JOUR IANFA 53 2125 for Izv.Akad.Nauk SSSR, Ser.Fiz. 53, 2125 (1989); Bull.Acad.Sci.USSR, Phys. Ser. 53, No.11, 63 (1989)
			
			//CONF Birmingham(83rd meeting)P403,Aston
			
			//here only the part before the "," if existing is needed 
			
			codenString=codenString.trim();
			if(codenString.isEmpty())
				return null;
			
	        //reference type
	        String type="";//JOUR,CONF,REPT,THESIS,PC (private communication), PREPRINT   
	        String refInCoden="";
	        String[] temp=codenString.split("[,]+");
	        codenString=temp[0];
	        
	        temp=codenString.split("[\\s]+");	  
	        if(temp.length<=1)
	        	return null;
	        
            type=temp[0].trim();
            refInCoden=codenString.substring(type.length()+1).trim();//including volume, page, but no year
	        
            if(type.equals("JOUR")) {
            	String coden=temp[1].trim().toUpperCase();
            	String name=CODEN.getJournalByCoden(coden);
            	
            	if(name.isEmpty()) {
            		ref.journalName=refInCoden;
            	}else {
            		ref.journalName=name;
            		ref.nameFromCODEN=name;
            		try {
            			ref.volume=temp[2];
            			                        
            			if(ref.volume.length()>1 && !Str.isDigits(ref.volume)) {
            				//e.g., 26B
            				String s=ref.volume;
            				String lett="";
            				if(Str.isLetter(s.charAt(0))) {
            					lett=s.charAt(0)+"";
            					s=s.substring(1).trim();
            				}else if(Str.isLetter(s.charAt(s.length()-1))) {
            					lett=s.charAt(s.length()-1)+"";
            					s=s.substring(0,s.length()-1).trim();
            				}
            				
            				if(lett.length()==1 && Str.isDigits(s) && lett.equals(lett.toUpperCase())) {
            					ref.volume=s;
                				
                				if(!name.endsWith("."+lett) && !name.endsWith(" "+lett)) {//Phys. Lett. B 26B, Phys.Rev. C C9
                					name+=" "+lett;
                            		ref.journalName=name;
                            		ref.nameFromCODEN=name;
                				}
            				}
 
            			}
            			
            			String pageS=temp[3];
                        char c0=pageS.charAt(0);
                        int n=0;
                        
                        ref.pageStr=pageS;
                        if(Character.isDigit(c0)) {
                            while(n<pageS.length() && Character.isDigit(pageS.charAt(n)) ){
                                n++;
                            }               
                            pageS=pageS.substring(0,n).trim();
                            
                            ref.pageMark=pageS.substring(n).trim();
                        }else {
                            while(n<pageS.length()){
                                if(Character.isDigit(pageS.charAt(n)))
                                    break;
                                        
                                n++;
                            }    
                            
                            ref.pageMark=pageS.substring(0,n).trim();  
                            pageS=pageS.substring(n).trim();

                        }
                        
                        ref.page=pageS;
            		}catch(Exception e) {
            			//e.printStackTrace();
            		}
            	}
            }else{          	
	        	//CONF Birmingham(Delayed Neutron Properties) 
	        	String s=codenString.substring(type.length()+1).trim();
	        	int nb=0;
	        	int i=0;
	        	while(i<s.length()) {
	        		char c=s.charAt(i);
	        		if("([{".contains(c+"")){
	        			nb++;
	        		}else if(")]}".contains(c+"")){
	        			nb--;
	        		}else if(" ,;".contains(c+"") && nb==0) {
	        			s=s.substring(0,i).trim();
	        			break;
	        		}
	        		i++;
	        	}
	        	
            	String coden=s.toUpperCase().trim();
            	int n=coden.indexOf(")");
            	if(n>0 && n<coden.length()-1)
            		coden=coden.substring(0,n+1).trim();
            	
            	String name=CODEN.getJournalByCoden(coden);
            	if(name.isEmpty()) {
            		ref.journalName=type+"-"+coden;
            	}else {
            		ref.journalName=name;
            		ref.nameFromCODEN=name;
            	}
            }
           
            return ref;
		}catch(Exception e) {}
		
		return null;
	}
	
	public Reference parseNSRJournalString (String refString) {
		try {
			
			Reference ref=new Reference();
			
			//example journal reference: Phys.Rev. C 97, 035501 (2018)
			refString=refString.trim();
			if(refString.endsWith("."))
				refString=refString.substring(0,refString.length()-1).trim();
			
			//but there is case like 1954GH24, <REFERENCE>=Phys.Rev. 94 1081 (1954)
			int n=refString.lastIndexOf(",");
			int n1=-1,n2=-1;
			String s="",s1="",s2="";
			String yearS="",pageS="";
			
			
			//System.out.println(refString);
			
            if(n<=0) {
            	ref.journalName=refString;
            			
            	n1=refString.indexOf("(");
            	n2=refString.indexOf(")");
            	
            	boolean isGood=false;
            	s=refString;
            	while(n1>0 && n2>n1) {
            		//Phys.Rev. 51, 383 (51) (1936)
            		//Phys.Rev. 51, (51) 383 (1937) 
            		yearS=s.substring(n1+1,n2).trim();
            		if(yearS.length()==4 && Str.isInteger(yearS)) {
            			ref.year=Integer.parseInt(yearS);
            			s=refString.substring(0,n1).trim();
            			
            			ref.journalName=s;
            			
            			n1=s.toUpperCase().lastIndexOf(" NO");
            			if(n1>0 && n1<s.length()-3 && !Str.isLetter(s.charAt(n1+3))) {
            				n=n1;
            				isGood=true;
            			}else if((n1=s.lastIndexOf(" "))>0) {
            				n=n1;
            				isGood=true;
            			}  
            			break;
            		}else {
                    	n1=refString.indexOf("(",n2);
                    	n2=refString.indexOf(")",n1);
            		}
            	}
            	
            	//System.out.println("**** "+refString+"  n1="+n1+" n2="+n2+" isGood="+isGood+" yearS="+yearS);
            	
            	if(!isGood)          		
                    return ref;            	
            }
            
            s1=refString.substring(0,n).trim();//supposed to be string of journal name+volume if journal
            s2=refString.substring(n+1).trim();//supposed to be page+year if journal

            //page and year
            n1=s2.lastIndexOf("(");
            n2=s2.lastIndexOf(")");
            if(n1>=0 && n2>n1) {
                pageS=s2.substring(0,n1).trim();
                yearS=s2.substring(n1+1,n2).trim();

        		//"Phys.Rev. 51, 383 (51) (1936)"
                
        		if(yearS.length()==4 && Str.isInteger(yearS)) {
        			ref.year=Integer.parseInt(yearS);
        		}else {
        			yearS="";
                    n1=s2.lastIndexOf("(",n2);
                    n2=s2.lastIndexOf(")",n1);
                    if(n1>=0 && n2>n1) {
                        yearS=s2.substring(n1+1,n2).trim();
                        if(yearS.length()==4 && Str.isInteger(yearS)) {
                			ref.year=Integer.parseInt(yearS);
                        }
                    }
        		}
                //System.out.println(s2+" "+pageS+"  "+yearS);
		        	            
        		ref.pageStr=pageS;
        		
                char c0=pageS.charAt(0);
                n=0;
                if(Character.isDigit(c0)) {
                    while(n<pageS.length() && Character.isDigit(pageS.charAt(n)) ){
                        n++;
                    }               

                    ref.pageMark=pageS.substring(n);
                    pageS=pageS.substring(0,n).trim();
                    
    	            //if(keyno.equalsIgnoreCase("2026SU06"))
    	            //	System.out.println(refString+"$"+ref.pageStr+"$"+ref.pageMark+"$"+pageS);
                }else {
                    while(n<pageS.length()){
                        if(Character.isDigit(pageS.charAt(n)))
                            break;
                                
                        n++;
                    }    
                    
                    ref.pageMark=pageS.substring(0,n);  
                    pageS=pageS.substring(n).trim();

                }
                
                ref.page=pageS;
                
            }else {
                try {
                    ref.year=Integer.parseInt(s2);
                }catch(Exception e) {
                    ref.page=s2;
                    ref.pageStr=s2;
                }
            }
            
            //Phys.Rev. 51, (51) 383 (1937) 
            n1=ref.page.indexOf(")");
            if(n1>0 && Str.isInteger(ref.page.substring(n1+1).trim()))
            	ref.page=ref.page.substring(n1+1).trim();
            
            //System.out.println(ref.page);
            
            //journal name and volume
            if(ref.year<=0 || ref.page.isEmpty()) {
            	ref.journalName=refString;
            	
            }else {
                n=-1;
                   
                //s1 here supposed to be string of journal name+volume
                for(int j=0;j<s1.length();j++) {
                    if(Character.isDigit(s1.charAt(j))) {
                        n=j;
                        break;
                    }
                }

                
                if(n>0) {
                    String jname=s1.substring(0,n).trim();
                    String volumeS=s1.substring(n).trim();
                    String issueS="";
                    
                    //System.out.println("1 s1="+s1+" s2="+s2+" n="+n+" name="+jname+" volume="+volumeS);
                    String[] temp=volumeS.split("[,]+");
                    if(temp.length==2 && temp[1].toUpperCase().contains("NO.")) {//issue
                    	volumeS=temp[0].trim();
                    	issueS=temp[1].trim();
                    }else if(temp.length>=2) {
                    	volumeS="";
                    }

                    if(volumeS.length()>0) {
                        n=1;
                        while(n<volumeS.length() && Character.isDigit(volumeS.charAt(n)) ){
                            n++;
                        }
                        String mark=volumeS.substring(n);
                        volumeS=volumeS.substring(0,n);
                        
                        jname+=" "+mark;
                        jname=jname.trim();
                        
                        //System.out.println("2 s1="+s1+" s2="+s2+" n="+n+" name="+jname+" volume="+volumeS);
                    }

                    ref.journalName=jname;
                    ref.volume=volumeS;
                    ref.issue=issueS;
                    
                }else {
                    ref.journalName=s1;
                }            	
            }

            return ref;
		}catch(Exception e) {}
		
		return null;
	}
	/*
	private Reference parseProceeding(String s) {	
		try {
			
		}catch(Exception e) {}
		
		return null;
	}
	private Reference parseLabReport(String s) {	
		try {
			
		}catch(Exception e) {}
		
		return null;
	}
	private Reference parseThesis(String s) {	
		try {
			
		}catch(Exception e) {}
		
		return null;
	}
	private Reference parsePreprint(String s) {	
		try {
			
		}catch(Exception e) {}
		
		return null;
	}
	*/
	
	 
	public Vector<Selector> parseSelectors(Vector<String> selectorStrV){
        Vector<Selector> out=new Vector<Selector>();
        for(String s:selectorStrV) {
            Vector<Selector> tempV=parseSelectors(s);
            if(tempV!=null)
                out.addAll(tempV);
        }
        
        return out;
	  
	}
	
	
	   
	   
    //a selector string contains multiple selector rule according to
    //the "link variable" (or identification tag letter; see NSR help page).
    //Also note that the same nuclide name following "N" could appear in
    //multiple places followed by different tag letters in the selector string, 
    //e.g., after reaction name, or in the "deduced" sentence. Those 
    //information of the same nuclide should be merged into one selector.
    //Also note that multiple nuclides and reactions could be labeled by the
    //same tag and thus they should be set in the same selector
    //
    //an example selector string (generated from keywords string)
    /*
      <KEYNO   >1989FA02
  <SELECTRS>T:130TE;A. R:(26MG,6N);A. N:150GD;A. T:130TE;B. R:(27AL,6N);B. N:151TB
  ;B. M:G-SPECTRA;A. M:G-SPECTRA;B. N:150GD;C. N:151TB;C. D:LEVEL-PROP;C. D:SUP-DE
  F;C.     

Format of each entry: type:value;link variable (tag).
Entries are separated by ". " following the tag letter
   
explanations:
   T: target
   R: reaction, only the part containing beam and ejectile
   N: nucleus under study (NUCID); parsed from reaction
   M: measured
   D: deduced
   C: calculated
   X: analyzed ?
   S: special; specification; spectrum ??? (used to specify the decay type; two-nucleon transfer; charge-exchange, etc)
   P: parent of decay
   G: daughter (note that "G" here not for gamma)
   A: mass (number or range)
   
     */	  
	public Vector<Selector> parseSelectors(String selectorStr){
	    Vector<Selector> out=new Vector<Selector>(); 
        String type="",value="",tag="";        
        HashMap<String,Selector> tagSelectorMap=new HashMap<String,Selector>();

        String[] entries=selectorStr.split("[.]+");
        
        //System.out.println(" entries length="+entries.length);
        
        for(int j=0;j<entries.length;j++) {
            try {
                String entry=entries[j].trim();//format: type:value;tag
                if(entry.isEmpty())
                    continue;
                
                int n1=entry.indexOf(":");
                int n2=entry.indexOf(";");
               
                type=entry.substring(0,n1).trim();
                value=entry.substring(n1+1,n2).trim();
                tag=entry.substring(n2+1).trim();
                
                if(!tagSelectorMap.containsKey(tag)) {
                    tagSelectorMap.put(tag, new Selector());
                }
                
                //System.out.println(" tag="+tag+" type="+type+" value="+value+" tagSelectorMap.contains tag="+tagSelectorMap.containsKey(tag));
                
                Selector sel=tagSelectorMap.get(tag);
                sel.addValue(type, value);
                
            }catch(Exception e) {
                
            }
        }
        
        //System.out.println(" selectors size="+tagSelectorMap.size());
        
        //remove fake decay DatasetID (see notes below in addValue())
        out.addAll(cleanSelectors(tagSelectorMap));
            
        //System.out.println(" out size="+out.size());
        
        return out;	       
	  
	  
	}
	
	//1.remove fake decay DatasetID in a selector with wrong assignment of decayType (see notes below in addValue())
	//2.add missing nuclide in DatasetIDs using the nuclide of the preceeding DatasetID 
	private Vector<Selector> cleanSelectors(HashMap<String,Selector> tagSelectorMap){
	    Vector<Selector> out=new Vector<Selector>();
        if(tagSelectorMap==null || tagSelectorMap.isEmpty())
            return out;
        
	    for(String tag:tagSelectorMap.keySet()) {
	        Selector sel=tagSelectorMap.get(tag);
	        
	        Vector<DatasetID> tempV=new Vector<DatasetID>();
	        tempV.addAll(sel.datasetIDsV);
	        
	        //System.out.println(" selector="+tag+"  dsid size="+tempV.size());
	        
	        for(int i=0;i<tempV.size();i++) {
	            DatasetID dsid=tempV.get(i);
	            if(!dsid.target.isEmpty() && !dsid.beam.isEmpty() && dsid.nucid.isEmpty()) {
	                if(i>0) {
	                    dsid.nucid=tempV.get(i-1).nucid;
	                }
	            }else if(!dsid.decayParent.isEmpty() && !dsid.decayType.isEmpty() && dsid.nucid.isEmpty()) {//here dsid.nucid is the daughter
	                sel.datasetIDsV.remove(dsid);
	            }
	        }
	    }
	    out.addAll(tagSelectorMap.values());
	    
	    return out;
	}
	
	public class Selector{
	    
	    //measured,deduced, calculated and analyzed entries are for all datasetIDs	    
		private Vector<DatasetID> datasetIDsV=new Vector<DatasetID>();//each DatasetID contains beam, target, nucid, reaction, decay, etc.
		private Vector<String> measured=new Vector<String>();//contains names of measured quantities (refer to NSR nomenclature)
		private Vector<String> deduced=new Vector<String>();//contains names of deduced quantities
		private Vector<String> calculated=new Vector<String>();
		private Vector<String> analyzed=new Vector<String>();
		
		private String getDecayDaughter(String parentNUCID,String decayMode) {
		    try {
		        Nucleus pNuc=new Nucleus(parentNUCID);
		        int A=pNuc.A();
		        int Z=pNuc.Z();
		        if(A>0 && A>=Z) {
		            decayMode=decayMode.toUpperCase().trim();
		            if(decayMode.equals("B+")||decayMode.equals("EC")) {
		                Z=Z-1;
		            }else if(decayMode.equals("B-")){
		                Z=Z+1;
		            }else if(decayMode.equals("A")) {
		                A=A-4;
		                Z=Z-2;
		            }else if(decayMode.equals("P")) {
		                A=A-1;
		                Z=Z-1;
		            }else if(decayMode.equals("N")) {
		                A=A-1;
		            }else if(decayMode.equals("PI")) {
		                Z=Z+1;
		            }else
		                return "";
		            
		            Nucleus dNuc=new Nucleus(Z,A);
		            if(dNuc!=null && dNuc.nameENSDF().length()>0)
		                return dNuc.nameENSDF().trim();
		        }
		        
		    }catch(Exception e) {
		        
		    }
		    
		    return "";
		}
		
		public void addValue(String type,String value) {
		    if(type==null || value==null)
		        return;
		    
		    type=type.toUpperCase().trim();
		    value=value.toUpperCase().trim();
		    
		    DatasetID datasetID=null;
		    
		    //note that each new dataset (reaction or decay) starts with "T" (target in reaction)
		    //or "P" (parent in decay) entry, or "N" if no reaction or decay specified
		    switch(type){
		    case "T":
		        datasetID=new DatasetID();
		        datasetIDsV.add(datasetID);
		        
		        //System.out.println(" target="+value+" datasetIDsV size="+datasetIDsV.size());
		        
		        datasetID.target=value;
		        break;
		    case "R"://reaction, only the part containing beam and ejectile, e.g.,(17C,16B)	   
		        //first, check if some DSID has NUCID while some DSIDs after that have no.
		        //for such cases, assign the last NUCID to all DSIDs after it.
		        //If all have or have no NUCIDE, do nothing.
	            for(int i=0;i<datasetIDsV.size();i++) {
	                DatasetID dsid=datasetIDsV.get(i);
	                if(!dsid.target.isEmpty() && !dsid.beam.isEmpty() && dsid.nucid.isEmpty()) {
	                    if(i>0) {
	                        dsid.nucid=datasetIDsV.get(i-1).nucid;
	                    }
	                }
	            }
	            
		        if(datasetIDsV.isEmpty() || !datasetIDsV.lastElement().beam.isEmpty()) {
		            //T:9BE;A. R:(A,A);A. N:9BE;A. R:(A,A');A. R:(A,N);A. N:12C;A., 2017LO04 
		            Vector<DatasetID> tempV=new Vector<DatasetID>();
		            
		            datasetID=new DatasetID();
		            datasetID.target=datasetIDsV.lastElement().target;
		            tempV.add(datasetID);

		            String beamPlusEjectile0=datasetIDsV.lastElement().beam+"+"+datasetIDsV.lastElement().ejectile;
		            int i=datasetIDsV.size()-2;
		            while(i>=0) {
		                DatasetID dsid=datasetIDsV.get(i);
		                String beamPlusEjectile=dsid.beam+"+"+dsid.ejectile;
		                if(!beamPlusEjectile.equals(beamPlusEjectile0))
		                    break;
		                
	                    datasetID=new DatasetID();
	                    datasetID.target=dsid.target;
	                    tempV.add(0,datasetID);	
	                    i--;
		            }
		            
		            if(tempV.size()>0)
		                datasetIDsV.addAll(tempV);
		            
		        }
		        
		        datasetID=datasetIDsV.lastElement();
		        if(value.startsWith("(")&&value.endsWith(")")&&value.contains(",")) {
		            int n=value.indexOf(",");
		            datasetID.beam=value.substring(1,n).trim();
		            datasetID.ejectile=value.substring(n+1,value.length()-1).trim();
		            
		            for(int i=datasetIDsV.size()-2;i>=0;i--) {
		                DatasetID dsid=datasetIDsV.get(i);
		                if(dsid.beam.length()>0 || dsid.ejectile.length()>0 || dsid.decayParent.length()>0) 
		                    break;
		                
		                dsid.beam=datasetID.beam;
		                dsid.ejectile=datasetID.ejectile;
		                
		            }
		        }
		        break;
		    case "N":            
		        if(datasetIDsV.isEmpty() || (!datasetIDsV.lastElement().nucid.isEmpty()&&!datasetIDsV.lastElement().ejectile.equals("X")) ) {	                    
		            datasetIDsV.add(new DatasetID());
		            
                    datasetID=datasetIDsV.lastElement();
                    datasetID.nucid=value;
                    
                    //System.out.println(" nucid="+value+" datasetIDs size="+datasetIDsV.size());
                    
		        }else {
		            //then this nucid must be for one of the previous dataset, go find it
		            //Also note that, a same reaction (beam,ejectile) could be shared by
		            //different targets and nuclides, which must be in order and in pair,
		            //e.g., T:12C;A. T:13C;A. R:(A,A');A. N:12C;A. N:13C;A..
		            //Also in cases, like T:9BE;A. R:(A,A);A. N:9BE;A. R:(A,A');A. R:(A,N);A. N:12C;A. 
		            //the same target and nuclide are shared by different reactions, for such cases,
		            //only the first DatasetID is assigned with the nucide, and the rest need to 
		            //be checked and assigned afterwards.See cleanSelectors()
		            
		            if(datasetIDsV.lastElement().ejectile.equals("X")) {
		                //there could be multiple ejectiles from the same beam and target
		                //for such cases, make a DatasetID for each ejectile,e.g.,
		                //<KEYWORDS>NUCLEAR REACTIONS Be({+20}Ne,X),({+40}Ar,X){+12}C/{+13}C/{+14}C/{+15}/{+16}C/{+17}C/{+18}C/{+19}C, 2016KA37
		                //<SELECTRS>T:BE;A. R:(20NE,X);A. R:(40AR,X);A. N:12C;A. N:13C;A. N:14C;A. N:15C;A. N:16C;A. N:17C;A. N:18C;A. N:19C:A.
		                
		                Vector<DatasetID> tempV=new Vector<DatasetID>();
		                Vector<String> targetPlusBeamsV=new Vector<String>();
		                
                        for(int i=datasetIDsV.size()-1;i>=0;i--) {
                            DatasetID dsid=datasetIDsV.get(i);
                            String s=dsid.target+"+"+dsid.beam;
                            
                            //System.out.println("In NSRAbstract 618 s="+s+" nucid="+dsid.nucid);
                            
                            if((!dsid.ejectile.equals("X")&&!dsid.nucid.isEmpty()) || (dsid.ejectile.equals("X")&&targetPlusBeamsV.contains(s)) )
                                break;
                            
                            if(dsid.nucid.isEmpty()) {
                                dsid.nucid=value;
                            }else {
                                datasetID=new DatasetID();
                                datasetID.target=dsid.target;
                                datasetID.beam=dsid.beam;
                                datasetID.ejectile=dsid.ejectile;
                                datasetID.nucid=value;
                                
                                tempV.add(0, datasetID);
                            }

                            targetPlusBeamsV.add(s);
                            
                        } 
		                
                        if(tempV.size()>0)
                            datasetIDsV.addAll(tempV);
		            }else {
                        DatasetID match=null;
                        
                        //from right to left, find the last DatasetID without nuclide assigned
                        for(int i=datasetIDsV.size()-1;i>=0;i--) {
                            DatasetID dsid=datasetIDsV.get(i);
                            if(dsid.nucid.isEmpty() && !dsid.beam.isEmpty() ){
                                if(i==0) 
                                    match=dsid;
         
                                continue;
                            }else if(i<datasetIDsV.size()-1){
                                match=datasetIDsV.get(i+1);
                                break;
                            }   
                        } 
                        
                        if(match!=null)
                            match.nucid=value; 
		            }

		        }

		        break;
            case "P":
                datasetID=new DatasetID();   
                datasetIDsV.add(datasetID);
                
                datasetID.decayParent=value;
                break;		        
            case "G"://parent and decay type should have set already but need to find which parent if there are multiple
                //note that there could be cases where only P and G are shown with no decay modes, for such cases,
                //the decay mode is the same as previous one, see below the cases for 152EU and 64CU parents at "S"
                
                for(int i=datasetIDsV.size()-1;i>=0;i--) {
                    DatasetID dsid=datasetIDsV.get(i);
                    if(!dsid.decayParent.isEmpty()&&dsid.decayType.isEmpty()) 
                        continue;
                    
                    if(!dsid.decayType.isEmpty()) {
                        for(int j=i+1;j<datasetIDsV.size();j++) {
                            DatasetID dsid1=datasetIDsV.get(j);
                            if(!dsid1.decayParent.isEmpty() && dsid1.decayType.isEmpty())
                                datasetIDsV.get(j).decayType=dsid.decayType;//set decays with no decay type specified with previous type
                            
                            //System.out.println("i="+i+" j ="+j+" pa="+dsid1.decayParent+" decay type="+dsid1.decayType+" datasetIDsV size="+datasetIDsV.size());

                        }
                    }
                    
                    break;                    
                }
                
                for(int i=datasetIDsV.size()-1;i>=0;i--) {
                    DatasetID dsid=datasetIDsV.get(i);
                    if(dsid.decayParent.isEmpty() || !dsid.nucid.isEmpty()) 
                        break;
                      
       
                    
                    //"P:198AU;A. S:B--DECAY;A. S:B-DECAY;A. G:198HG;A. P:152EU;A. P:64CU;A. \n"
                    //"G:152GD;A. G:64ZN;A. S:EC-DECAY;A. G:152SM;A. G:64NI;A. P:106AG;A. P:13N;A. G:10\n"
                    //"6PD;A. G:13C;A. M:B-SPECTRA;A. D:G-SPECTRA;A. D:T1/2;A. D:CE;A.";
                    
                    //System.out.println("i ="+i+" pa="+dsid.decayParent+" decay type="+dsid.decayType+" getdaughter="
                    //+getDecayDaughter(dsid.decayParent,dsid.decayType));
                    
                    if(value.equals(getDecayDaughter(dsid.decayParent,dsid.decayType))) {
                        dsid.nucid=value;//note: for decay "G" is used in place of "N" for DSID     
                        break;
                    }                             
                }
  
                break;
            case "S"://for decay type, or other special label like polarization, two-nucleon transfer; 
                if(value.equals("B-DECAY")) {
                    //skip, since it always appear together with "B--DECAY" or "B+-DECAY"
                }else if(value.contains("-DECAY")) {
                    int n=value.indexOf("-DECAY");
                    String s=value.substring(0,n);
                    boolean hasSet=false;
                    for(int i=datasetIDsV.size()-1;i>=0;i--) {
                        DatasetID dsid=datasetIDsV.get(i);
                        if(dsid.decayParent.isEmpty() || !dsid.decayType.isEmpty()) 
                            break;
                        
                        dsid.decayType=s;   
                        hasSet=true;
                    }
                    if(!hasSet) {
                        //for additional decay modes of the same parents
                        String nextType="",thisType="";
                        Vector<DatasetID> tempV=new Vector<DatasetID>();
                        for(int i=datasetIDsV.size()-1;i>=0;i--) {
                            DatasetID dsid=datasetIDsV.get(i);
                            thisType=dsid.decayType;
                            
                            //System.out.println("this decay: parent="+dsid.decayParent+" s="+s+" this type="+dsid.decayType+" next type="+nextType);
                            
                            if(dsid.decayParent.isEmpty() || thisType.equals(s) || (!nextType.isEmpty()&&!thisType.equals(nextType)) )
                                break;
                            
                            //note that fake ones could be created (e.g., decay mode incorrectly assigned to a parent), see below
                            //"S:EC-DECAY" will be assigned to 198AU,152EU,62CU, but it is only for the last two parents.
                            //It will be seen later that there is no corresponding daughter for 198AU EC decay. So it can be removed
                            //later by checking if the daughter exists
                            //
                            //<SELECTRS>P:198AU;A. S:B--DECAY;A. S:B-DECAY;A. G:198HG;A. P:152EU;A. P:64CU;A. 
                            //G:152GD;A. G:64ZN;A. S:EC-DECAY;A. G:152SM;A. G:64NI;A.
                            
                            datasetID=new DatasetID();
                            tempV.add(0,datasetID);
                            
                            datasetID.decayType=s;   
                            datasetID.decayParent=dsid.decayParent;
                            
                            //System.out.println("new DSID with new decaymode value="+value+" decayType="+s+" parent="+dsid.decayParent);
                            
                            nextType=thisType;
                        }
                        
                        if(tempV.size()>0)
                            datasetIDsV.addAll(tempV);
                    }
                }else {//special label
                    for(int i=datasetIDsV.size()-1;i>=0;i--) {
                        DatasetID dsid=datasetIDsV.get(i);
                        if(!dsid.otherLabel.isEmpty()) 
                            break;
                        
                        dsid.otherLabel=value;                       
                    }
                }
                
                break;
            case "M":
                if(!measured.contains(value))
                    measured.add(value);
                break;
            case "D":
                if(!deduced.contains(value))
                    deduced.add(value);
                break;
            case "C":
                if(!calculated.contains(value))
                    calculated.add(value);
                break;
            case "X":
                if(!analyzed.contains(value))
                    analyzed.add(value);
                break;
            default:
		        break;
		        
		    }
		}
		/*
	       T: target
	       R: reaction, only the part containing beam and ejectile
	       N: nucleus under study (NUCID); parsed from reaction
	       M: measured
	       D: deduced
	       C: calculated
	       X: analyzed ?
	       S: special; specification; spectrum ??? (used to specify the decay type; two-nucleon transfer; charge-exchange, etc)
	       P: parent of decay
	       G: daughter (note that "G" here not for gamma)	
	    
	       */
		
		public Vector<String> nucidsV() { 
		    Vector<String> out=new Vector<String>();
		    for(DatasetID datasetID:datasetIDsV)
		        out.add(datasetID.nucid);
		    
		    return out;		    
		} 
        public Vector<String> beamsV() { 
            Vector<String> out=new Vector<String>();
            for(DatasetID datasetID:datasetIDsV)
                out.add(datasetID.beam);
            
            return out;         
        }
        
        public Vector<String> targetsV() { 
            Vector<String> out=new Vector<String>();
            for(DatasetID datasetID:datasetIDsV)
                out.add(datasetID.target);
            
            return out;         
        }
        public Vector<String> datasetTypesV() { 
            Vector<String> out=new Vector<String>();
            for(DatasetID datasetID:datasetIDsV)
                out.add(datasetID.datasetType);
            
            return out;         
        }     
        public Vector<String> parentsV() { 
            Vector<String> out=new Vector<String>();
            for(DatasetID datasetID:datasetIDsV)
                out.add(datasetID.decayParent);
            
            return out;         
        }
        public Vector<String> dsidsV() { 
            Vector<String> out=new Vector<String>();
            for(DatasetID datasetID:datasetIDsV)
                out.add(datasetID.dsid);
            
            return out;         
        }
        
        public Vector<DatasetID> datasetIDsV() { 
            return datasetIDsV;     
        }
	
        public DatasetID datasetAt(int i) {
            try {
                return datasetIDsV.get(i);
            }catch(Exception e) {}
            
            return null;
        }
        
        public Vector<String> deduced(){return deduced;}
        public Vector<String> measured(){return measured;}
        public Vector<String> calculated(){return calculated;}
        public Vector<String> analyzed(){return analyzed;}
	
	}
	
    public class Keyword{
    }
    
    
    
	public class Reference{
		
	    public String type="";//JOUR,CONF,REPT,THESIS,PC (private communication), PREPRINT  
	    public String nsrKeynumber="";
		public String articleTitle="";
		public String journalName="";
		public String nameFromCODEN="";
		public String volume="";
		public String issue="";
		public String page="";
		public String authors="";
		public String abs="";
		public int year=-1;
		public String pageMark="";//like "R" for rapid communication, "L" for letter
		public String pageStr="";//the whole page string including page mark
		public String doi="";
		public String web="";
		public String refS="";//title+cite
		public String citeS="";//journal name+volume+page+year
	    public Vector<Author> authorsV=new Vector<Author>();
	    
	    //usually, Russian paper have two versions: Russian and English translation
	    //and in a NSR abstract, two journals are presented in the reference entry of NSR
	    //and only in such cases the English version is flagged as translated.
	    //So when isTranslated=true, there must be a corresponding original version
	    public boolean isTranslated=false;
	    
	    public String toString() {return refS;}
	    
	    public boolean isValidRef() {
	    	if(journalName.isEmpty() && web.isEmpty())
	    		return false;
	    	
	    	return true;
	    };
	    
	    public String printAuthors() {
	    	try {
	    		int n=authorsV.size();
	    		if(n==0)
	    			return "";
	    		
	    		if(n==1) {
	    			
	    		}
	    	}catch(Exception e) {
	    		
	    	}
	    	return "";
	    }
	    
	    public void merge(Reference ref) {
	    	if(ref==null)
	    		return;
	    	
	    	String s1=this.nsrKeynumber.toUpperCase().trim();
	    	String s2=ref.nsrKeynumber.toUpperCase().trim();
	    	if(s1.length()>0 && s2.length()>0 && !s1.equals(s2))
	    		return;
	    	
	    	if(ref.type.length()>0) this.type=ref.type;  
		    if(ref.nsrKeynumber.length()>0) this.nsrKeynumber=ref.nsrKeynumber;
			if(ref.articleTitle.length()>0) this.articleTitle=ref.articleTitle;
			if(ref.journalName.length()>0) this.journalName=ref.journalName;
			if(ref.nameFromCODEN.length()>0) this.nameFromCODEN=ref.nameFromCODEN;
			if(ref.volume.length()>0) this.volume=ref.volume;
			if(ref.issue.length()>0) this.issue=ref.issue;
			if(ref.page.length()>0) this.page=ref.page;
			if(ref.authors.length()>0) this.authors=ref.authors;
			if(ref.abs.length()>0) this.abs=ref.abs;

			
			if(ref.pageMark.length()>0) this.pageMark=ref.pageMark;//like "R" for rapid communication, "L" for letter
			if(ref.pageStr.length()>0) this.pageStr=ref.pageStr;
			if(ref.doi.length()>0) this.doi=ref.doi;
			if(ref.web.length()>0) this.web=ref.web;
			if(ref.refS.length()>0) this.refS=ref.refS;//title+cite
			if(ref.citeS.length()>0) this.citeS=ref.citeS;//journal name+volume+page+year
			
			if(ref.year>0) this.year=ref.year;
		    
		    if(ref.authorsV.size()>0) {
		    	for(Author tempAut:ref.authorsV) {
		    		if(this.authorsV.contains(tempAut))
		    			continue;
		    		
		    		boolean toAdd=true;
		    		for(Author aut:this.authorsV) {
		    			if(tempAut.getFullName().equals(aut.getFullName())) {
			    		    if(tempAut.orcid.length()>0) aut.orcid=tempAut.orcid;
			    		    if(tempAut.email.length()>0) aut.email=tempAut.email;
			    		    if(tempAut.gender.length()>0) aut.gender=tempAut.gender;
			    		    toAdd=false;
			    		    break;
		    			}
		    		}
		    		
		    		if(toAdd)
		    			this.authorsV.add(tempAut);
		    	}
		    }
	    }
	    
	    public String makeBibtexEntry(String citeLabel) {
	    	try {
	    		if(citeLabel.trim().isEmpty() || !isValidRef())
	    			return "";
	    		
	    		String s="";
	    		String authorS=printAuthors();
	    		
	    		//if(!journalName.isEmpty() && !volume.isEmpty() && !page.isEmpty())
	    		if(type.equals("JOUR")) {
		    		
	    			s="@article{"+citeLabel;
		    		if(!authorS.isEmpty())
		    			s+=", author={{"+authorS+"}}";
		    		if(!journalName.isEmpty())
		    			s+=", journal={"+journalName+"}";
		    		if(year>0)
		    			s+=", year={"+year+"}";
		    		if(!volume.isEmpty())
		    			s+=", volume={"+volume+"}";
		    		if(!page.isEmpty())
		    			s+=", page={"+pageMark+page+"}";
	    		}else {
		    		s="@misc{"+citeLabel;
		    		
		    		String s1="";
		    		if(!authorS.isEmpty())
		    			s1+=authorS+", ";
		    		if(!refS.isEmpty())
		    			s1+=refS;
		    		
		    		s1=s1.trim();
		    		if(!s1.isEmpty())
		    			s+=", note={{"+s1+"}}";
	    		}

	    		return s;
	    	}catch(Exception e) {
	    	}
	   
	    	
	    	return "";
	    }

	}
	
/*
 example

<KEYNO   >1989FA02
<HISTORY >A19890512 M19920619
<CODEN   >JOUR PYLBB 218 137
<REFRENCE>Phys.Lett. 218B, 137 (1989)
<AUTHORS >P.Fallon, P.Alderson, M.A.Bentley, A.M.Bruce, P.D.Forsyth, D.Howe, J.W
.Roberts, J.F.Sharpey-Schafer, P.J.Twin, F.A.Beck, T.Byrski, D.Curien, C.Schuck
<TITLE   >Superdeformed Bands in {+150}Gd and {+151}Tb: Evidence for the influen
ce of high-N intruder states at large deformations
<KEYWORDS>NUCLEAR REACTIONS {+130}Te({+26}Mg,6n),E=145 MeV; {+130}Te({+27}Al,6n)
,E=150 MeV; measured |g|g-coin,|g|g(|q). {+150}Gd,{+151}Tb deduced levels,J,|p,s
uperdeformed band structure,shapes,moments of inertia.
<SELECTRS>T:130TE;A. R:(26MG,6N);A. N:150GD;A. T:130TE;B. R:(27AL,6N);B. N:151TB
;B. M:G-SPECTRA;A. M:G-SPECTRA;B. N:150GD;C. N:151TB;C. D:LEVEL-PROP;C. D:SUP-DE
F;C.
<DOI     >10.1016/0370-2693(89)91408-1

Each line in the exchange format has a fixed length of 80 characters, and the different parts of the entry are separated with tags such as <AUTHORS >. 
The <HISTORY > field indicates that this entry was added to NSR on May 12, 1989, and last modified on June 19, 1992. The <SELECTRS> field is generated
from the keywords, and contains most of the indexed quantities used for searching.

Each element in the selector field consists of a type ("T" for target, "N" for nuclide, etc.), followed by a value ("130TE", "SUP-DEF"), followed by a
"link variable". The link variables are used to distinguish between sections of the keywords in boolean searches. For example, a search on "Nuclide=150Gd 
AND Reaction=(26MG,6N)" would retrieve the above reference, while "Nuclide=151Tb AND Reaction=(26MG,6N)" would not, because 151Tb is associated with link 
variable "B" and (26MG,6N) is associated with link variable "A".

The DOI field contains publisher-supplied "digital object identifier" information that allows the construction of a web link to the article.  	

CODEN - This is a five-letter code associated with a given journal. For instance, the coden for Phys.Rev. C is PRVCA. "Browse" returns a list of available 
codens, arranged alphabetically by journal name. This list also lists a year range for each coden, because a given journal may have had different codens in 
different years. 
 */
}
