package ensdfparser.ensdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ensdfparser.nds.util.Str;

/*
 * for parsing a single spin-parity string without any bracket
 */
public class JPI{
	private String js="";
    private String spin="";
    private String parity="";
    private int parityCode=0;
    private float spinValue=-1;
    private boolean isUniqueSpin=false;//not numerical not unique: e.g., LT 2; 1/2+,3/2+
    private boolean isFirmSpin=false;//firm spin: spin is not in parentheses
    private boolean isFirmParity=false;//firm parity: parity is not in parentheses
    private boolean isNA=false;//no JPI can be assigned
    
	private static String regexIntegerSingle="\\([1-9][0-9]?\\)[+-]?|\\([1-9][0-9]?[+-]\\)|[1-9][0-9]?\\([+-]\\)|[1-9][0-9]?[+-]?";
	private static String regexHalfSingle="\\([1-9]?[13579]/2\\)[+-]?|\\([1-9]?[13579]/2[+-]\\)|[1-9]?[13579]/2\\([+-]\\)|[1-9]?[13579]/2[+-]?";
	
	//character before a JPI string must be any of " .,;=)" or JPI string is the start
	//character after a JPI string must be any of " .,;)" or JPI string is the end
	private static String regexHalf="("+"(^("+regexHalfSingle+"))"+"|"+"([\\s.,;=(]("+regexHalfSingle+"))"+")"
	                               +"("+"(,("+regexHalfSingle+"))*$"+"|"+"(,("+regexHalfSingle+"))*[\\s.,;\\)]"+")";
	private static String regexInteger="("+"(^("+regexIntegerSingle+"))"+"|"+"([\\s.,;=(]("+regexIntegerSingle+"))"+")"
	                               +"("+"(,("+regexIntegerSingle+"))*$"+"|"+"(,("+regexIntegerSingle+"))*[\\s.,;\\)]"+")";
    
    public JPI(String js){
    	setValue(js);
    }
    
    public boolean equals(JPI jp) {
    	try {
        	if(jp.js.equals(js))
        		return true;
    	}catch(Exception e) {
    		
    	}

    	
    	return false;
    }
    
    public boolean equalsIgnoreBrackets(JPI jp) {
    	if(this.spinValue==jp.spinValue && this.parityCode==jp.parityCode)
    		return true;
    	
    	return false;
    }
    public void setValue(String js){
  
    	this.js=js;
    	
    	if(js.equalsIgnoreCase("NA")) {
    		isNA=true;
    		return;
    	}
    	
    	js=js.replace("(", "").replace(")","").trim();
    	
        if(js.endsWith("+")) {
            setParityCode(+1);
            setParity("+");
            setSpin(js.replace("+", "").trim());
        }else if(js.endsWith("-")) {
            setParityCode(-1);
            setParity("-");
            setSpin(js.replace("-", "").trim());
        }else
            setSpin(js);
        
        if(spin.endsWith("/2")) {
            String tempJS=spin().replace("/2", "").trim();
            if(Str.isInteger(tempJS)) {
                int tempJ=Integer.parseInt(tempJS);
                if(tempJ%2==1) {
                    setSpinValue(tempJ/2.0f);
                    isUniqueSpin=true;
                }
            }
        }else if(Str.isInteger(spin)) {
            setSpinValue(Float.parseFloat(spin));
            isUniqueSpin=true;
        }
            
        if(!spin.isEmpty() && !isUniqueSpin) {
            setSpin("");
        }  
        
        String s="";
        if(isUniqueSpin) {
        	s=this.js;
        	s=s.replace("+", "").replace("-","").trim();
        	s=s.replace("/2", "").trim();
        	s=s.replace("()","");//e.g., js=0(+)
        	if(Str.isInteger(s))
        		isFirmSpin=true;
        }
        
        s=this.js;
        s=s.replace(spin, "").trim();
        if(s.equals("+") || s.equals("-"))
        	isFirmParity=true;
    }

    public String js() {return js;}
      
	public String spin() {
		return spin;
	}

	public void setSpin(String spin) {
		this.spin = spin;
	}

	public int parityCode() {
		return parityCode;
	}

	public void setParityCode(int parityCode) {
		this.parityCode = parityCode;
	}

	public String parity() {
		return parity;
	}

	public void setParity(String parity) {
		this.parity = parity;
	}

	public float spinValue() {
		return spinValue;
	}

	public void setSpinValue(float spinValue) {
		this.spinValue = spinValue;
	}
	
	public boolean isNA() {return isNA;}
	
	public boolean isUniqueSpin() {return isUniqueSpin;}
	public void setIsUniqueSpin(boolean b) {isUniqueSpin=b;}
	
	public boolean isFirmSpin() {return isFirmSpin;}
	public void setIsFirmSpin(boolean b) {isFirmSpin=b;}
	
	public boolean isFirmParity() {return isFirmParity;}
	public void setIsFirmParity(boolean b) {isFirmParity=b;}
	
	public static void sortJSV(Vector<String> JSV) {
	    try {
	        int n=JSV.size();
	        String[] a=new String[n];
	        for(int i=0;i<=n-1;i++){
	            a[i]=JSV.get(i);
	        }	
	        Arrays.sort(a,new JPIComparator());
	        
            JSV.clear();
            for(int i=0;i<a.length;i++){             
                JSV.addElement(a[i]);
            }	        
	    }catch(Exception e) {
	        
	    }

	}
	
	public static void sortJPV(Vector<JPI> jpV) {
	    try {
	        int n=jpV.size();
	        JPI[] a=new JPI[n];
	        for(int i=0;i<=n-1;i++){
	            a[i]=jpV.get(i);
	        }	
	        Arrays.sort(a,new JPIComparator());
	        
            jpV.clear();
            
            for(int i=0;i<a.length;i++){             
                jpV.addElement(a[i]);
            }	        
	    }catch(Exception e) {
	        
	    }

	}
	public static void cleanJSV(Vector<String> JSV,boolean toMergeParity) {simplifyJSV(JSV,toMergeParity);}
	public static void cleanJSV(Vector<String> JSV) {simplifyJSV(JSV);}

	public static void cleanJPV(Vector<JPI> jpV,boolean toMergeParity) {simplifyJPV(jpV,toMergeParity);}
	public static void cleanJPV(Vector<JPI> jpV) {simplifyJPV(jpV);}
	
	public static void simplifyJSV(Vector<String> JSV) {simplifyJSV(JSV,true);}
	/*
	 * merge j+ and j- to J 
	 * insert j and remove j+ and j-
	 */
	public static void simplifyJSV(Vector<String> JSV,boolean toMergeParity) {		
		try {
			Vector<String> out=new Vector<String>();
			Vector<JPI> jpV=new Vector<JPI>();		
			for(String js:JSV)
				jpV.add(new JPI(js));

			simplifyJPV(jpV);			
			for(JPI jp:jpV)
				out.add(jp.js);
			
	        JSV.clear();
	        JSV.addAll(out);
		}catch(Exception e) {
			
		}		
	}

	public static void simplifyJPV(Vector<JPI> JPV) {simplifyJPV(JPV,true);}
	
	public static void simplifyJPV(Vector<JPI> jpV,boolean toCombineParity) {
	    Vector<JPI> tempV=new Vector<JPI>();
	    Vector<JPI> out=new Vector<JPI>();
	    tempV.addAll(jpV);
	    
    	//for(int i=0;i<tempV.size();i++)
    	//	System.out.println(" 1 "+tempV.get(i).js());
    	
	    while(tempV.size()>0) {
        	JPI jp1=tempV.get(0);
        	
        	//System.out.println(" jp1="+jp1.js+" "+tempV.size());
        
        	Vector<JPI> tempV1=new Vector<JPI>();
        	tempV1.addAll(tempV);
        	tempV1.remove(0);
        	
        	JPI betterOne=jp1;
        	for(int j=1;j<tempV.size();j++) {
        		JPI jp2=tempV.get(j);
        		JPI temp=makeBetterOneOfSameSpinValue(betterOne,jp2,toCombineParity);//merge j+ and j- to j, since jps in jpV represent all possibilities
        		
        		//System.out.println("     jp2="+jp2.js+" temp=null "+(temp==null));
        		
        		if(temp==null)
        			continue;
        		
        		betterOne=temp;
        		tempV1.remove(jp2);
        		
        		
        		//System.out.println("  jp1="+jp1.js+"   jp2="+jp2.js+" betterOne="+temp.js);
        		
        	}
    		//System.out.println(" iBetter="+iBetter+" tempV.size()="+tempV.size());
        	
        	out.add(betterOne); 
        	
            tempV.clear();
            tempV.addAll(tempV1); 
	    }
        
	   	//for(int i=0;i<out.size();i++)
    	//	System.out.println(" 2 "+out.get(i).js());
	   	
        jpV.clear();
        jpV.addAll(out);
	}
	public static Vector<JPI> mergeJPV(Vector<JPI> jpV1,Vector<JPI> jpV2) {return mergeJPV(jpV1,jpV2,true);}
	
	/* Note:
	 *   empty jpV is equivalent to empty JPS, so merge with jpV2 will return jpV2 
	 *   but null jpV is equivalent to no valid JPI, so merge with any jpV will also return null
	 * merge two Vectors of JPI to one vector
	 * toCombineParity=true to combine all JPIs (e.g.,PI=+ and - to PI=nothing) to cover all, like "OR" operation
	 *                =false to reject two JPIs with different spins or same spin but different parities, like "AND" operation
	 */
	public static Vector<JPI> mergeJPV(Vector<JPI> jpV1,Vector<JPI> jpV2,boolean toCombineParity) {

		try {
			Vector<JPI> out=new Vector<JPI>();
			
			if(jpV1.isEmpty() && jpV2.isEmpty())
				return out;
			if(jpV1.isEmpty()) {
				out.addAll(jpV2);
				return out;
			}				
			if(jpV2.isEmpty()) {
				out.addAll(jpV1);
				return out;
			}
			//JPs with same spin but different parity will be merged, j+ and j- to j
			if(toCombineParity) {
				out.addAll(jpV1);
				out.addAll(jpV2);
				simplifyJPV(out);
				sortJPV(out);
				
				return out;
			}

			Vector<JPI> origJPV1=new Vector<JPI>();
			Vector<JPI> origJPV2=new Vector<JPI>();
			origJPV1.addAll(jpV1);
			origJPV2.addAll(jpV2);
			
			//JPs with same spin but different parity will be rejected
			boolean hasResetV1=false;
			boolean isAllFirmSpin1=true,isAllFirmSpin2=true;
			
		    for(int i=0;i<jpV1.size();i++) {
	        	JPI jp1=jpV1.get(i);
	        	if(jp1.isNA)
	        		return null;
	        	
	        	if(!jp1.isFirmSpin) {
	        		isAllFirmSpin1=false;
	        		break;
	        	}
		    }
		    for(int i=0;i<jpV2.size();i++) {
	        	JPI jp2=jpV2.get(i);
	        	if(jp2.isNA)
	        		return null;
	        	
	        	if(!jp2.isFirmSpin) {
	        		isAllFirmSpin2=false;
	        		break;
	        	}
		    }
		    
		    for(int i=0;i<jpV1.size();i++) {
	        	JPI jp1=jpV1.get(i);
	        	
	        	//System.out.println("###i="+i+" jp1="+jp1.js+" parity code="+jp1.parityCode+" jV1 size="+jpV1.size()+" jV2 size="+jpV2.size());
	        	
	           	Vector<JPI> tempV=new Vector<JPI>();
	        	tempV.addAll(jpV2);
	        	
	        	JPI betterOne=null;
	        	boolean firmExclusion=false;
	        	if(jpV2.size()==0) {
	        		firmExclusion=true;
	        	}
	        	for(int j=0;j<jpV2.size();j++) {
	        		JPI jp2=jpV2.get(j);
	        		
	        		//toCombineParity=false here
	        		JPI temp=makeBetterOneOfSameSpinValue(jp1,jp2,toCombineParity);
	        		
	        		//if(temp!=null) System.out.println("  @j="+j+" jp1="+jp1.js+" jp2="+jp2.js+" parityCode="+jp2.parityCode+" temp="+temp.js);        		
	        		
	        		if(temp==null) {//jp1 and jp2 have different spin values or same spin but different parity
	        			//System.out.println("      #j="+j+" jp1="+jp1.js+" jp2="+jp2.js+" parityCode="+jp2.parityCode);        	
	        			
	        			if(!firmExclusion) {
	        				
	        				/*
	        				//debug
	        				String s1=JPI.combineJPVtoStr(origJPV1).replace("(","").replace(")","");
	        				String s2=JPI.combineJPVtoStr(origJPV2).replace("(","").replace(")","");
	        				if(s1.contains("3/2+")  && s2.contains("3/2+") )
	        				System.out.println("JPI 351:    #j="+j+" jp1="+jp1.js+" jp2="+jp2.js+" parityCode="+jp2.parityCode);  
	        				*/
	        				
		        			if(!jp1.spin.equals(jp2.spin)) {
		        				if(jp2.isFirmSpin)
		        				//if(jp1.isFirmSpin && jp2.isFirmSpin)
		        					firmExclusion=true;//for excluding jp1
		        			//}else if(jp1.isFirmParity && jp2.isFirmParity){//jp1 and jp2 must have different parity
		        			}else if(jp2.isFirmParity){//jp1 and jp2 must have different parity
		        				firmExclusion=true;
		        			}
	        			}

	        			continue;
	        		}
	        		betterOne=temp;
		        	tempV.remove(jp2);
		        	
	        		//System.out.println("     good jp2="+jp2.js+" temp="+temp.js);
	        		
	        	}
	    		//System.out.println(" $$$jp1="+jp1.js);
	        	
	        	if(betterOne!=null) {  
	        		
	        		out.add(betterOne); 
	        		
	        		
	        		//if(jpV1.size()>2 && jpV1.get(0).js.equals("(3/2+)")&&jpV1.get(1).js.equals("(5/2)")) {
	        			//System.out.println(" JPI 374: jp1="+jp1.js+" better="+betterOne.js+" parityCode="+betterOne.parityCode); 
	        			//for(int k=0;k<out.size();k++)
	        			//	System.out.println(k+" $$$ jp="+out.get(k).js);
	        		//}
	        		
	        		
	        	}else if(!firmExclusion && !hasResetV1) {//jp1 is excluded
	        		for(JPI tempJP:jpV1) {
	        			tempJP.setIsFirmSpin(false);
	        			hasResetV1=true;
	        			checkJS(tempJP);
	        		}
	        		
	        		for(JPI tempJP:out) {
	     				//debug
        				//String s1=JPI.combineJPVtoStr(origJPV1);
        				//String s2=JPI.combineJPVtoStr(origJPV2);
        				
	        			//System.out.println(" 1 jp1="+jp1.js+" ##"+tempJP.js +" s1="+s1+" s2="+s2);
        				
	        			tempJP.setIsFirmSpin(false);
	        			hasResetV1=true;
	        			checkJS(tempJP);
	        			
	        			//System.out.println(" 2 jp1="+jp1.js+" ##"+tempJP.js);
	        		}
	        	}
 
	        	jpV2=tempV;//NOTE jpV2 here no longer points to the input jpV2
	        	if(jpV1.isEmpty())
	        		break;
		    }	
			//for(int k=0;k<out.size();k++)
			//	System.out.println("JPI 401: "+k+" $$$$$$$$$$ jp="+out.get(k).js);
			
		    if(!isAllFirmSpin1 || !isAllFirmSpin2) {
				//for(int k=0;k<out.size();k++)
				//	System.out.println(k+" $$$ jp="+out.get(k).js +"   js1="+JPI.combineJPVtoStr(origJPV1)+" js2="+JPI.combineJPVtoStr(origJPV2));
				
				int n=Math.min(origJPV1.size(),origJPV2.size());
				boolean isEqual1=false,isEqual2=false;
				if(out.size()==origJPV1.size()){
					isEqual1=true;
					for(int i=0;i<out.size();i++) {
						if(!out.get(i).equals(origJPV1.get(i))) {
							//System.out.println("1##### "+out.get(i).js()+"    "+origJPV1.get(i).js());
							isEqual1=false;
							break;
						}
					}
				}
				if(out.size()==origJPV2.size()){
					isEqual2=true;
					for(int i=0;i<out.size();i++) {
						if(!out.get(i).equals(origJPV2.get(i))) {
							//System.out.println("2##### "+out.get(i).js()+"    "+origJPV1.get(i).js());
							isEqual2=false;
							break;
						}
					}
				}
				//example
				//jpV1: 3/2-,5/2,7/2,9/2+
				//jpV2: 3/2+,5/2+,7/2+
				//out : 5/2+,7/2+
				
    			//System.out.println(" JPI 429: n="+n+" out.size="+out.size()+" isEqual1="+isEqual1 +" isEqual2="+isEqual2); 
    			//for(int k=0;k<out.size();k++)
    			//	System.out.println(k+" $$$ jp="+out.get(k).js);
    			
			    if(out.size()<n || (out.size()==n&&!isEqual1&&!isEqual2) ){
			    	boolean isAllFirmSamePI=true;
			    	
        			//System.out.println(" JPI 433: n="+n+" out.size="+out.size()+" isEqual1="+isEqual1 +" isEqual2="+isEqual2); 
        			//for(int k=0;k<out.size();k++)
        			//	System.out.println(k+" $$$ jp="+out.get(k).js);
        			
			    	String prevPI="";
			    	for(JPI jp:out) {
			    		String parity=jp.parity.replace("(", "").replace(")","");
			    		if(!jp.isFirmParity || (!prevPI.isEmpty()&&!parity.equals(prevPI))) {
			    			isAllFirmSamePI=false;
			    			break;
			    		}
			    		prevPI=parity;
			    	}
			    	//System.out.println("JPI 371: "+isAllFirmSamePI);
			    	
			    	Vector<JPI> tempV=new Vector<JPI>();
			    	if(isAllFirmSamePI) {
				    	for(JPI jp:out) {
				    		String js=jp.spin.replace("(", "").replace(")","");
				    		String parity=jp.parity;;
				    		String s="("+js+")"+parity;
				    		jp=new JPI(s);
				    		tempV.add(jp);
				    	}
			    	}else {
				    	for(JPI jp:out) {
				    		String js=jp.spin.replace("(", "").replace(")","");
				    		String parity=jp.parity.replace("(", "").replace(")","");
				    		String s="("+js+parity+")";
				    		jp=new JPI(s);
				    		tempV.add(jp);
				    	}
			    	}
			    	
			    	if(tempV.size()>0) {
			    		out.clear();
			    		out.addAll(tempV);
			    		
						simplifyJPV(out);
						sortJPV(out);
			    	}
			    	
			    }
		    }

		    if(out.isEmpty() && (!origJPV1.isEmpty()||!origJPV2.isEmpty()))
		    	return null;
		    
		    return out;
		}catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static Vector<String> mergeJSV(Vector<String> jsV1,Vector<String> jsV2) {return mergeJSV(jsV1,jsV2,true);}
	
	public static Vector<String> mergeJSV(Vector<String> jsV1,Vector<String> jsV2,boolean toCombineParity) {
			
		try {
			Vector<String> out=new Vector<String>();
			
			Vector<JPI> jpV1=new Vector<JPI>();
			Vector<JPI> jpV2=new Vector<JPI>();
			
			for(String js:jsV1)
				jpV1.add(new JPI(js));
			for(String js:jsV2)
				jpV2.add(new JPI(js));
			
			Vector<JPI> jpV=mergeJPV(jpV1,jpV2,toCombineParity);	
			
			for(JPI jp:jpV)
				out.add(jp.js);
			
			if(out.isEmpty() && (!jsV1.isEmpty()||jsV2.isEmpty()))
				return null;
			
			return out;
		}catch(Exception e) {
			
		}
		
		return null;
	}
	
	public static String mergeJPS(String jps1 ,String jps2) {return mergeJPS(jps1,jps2,true);}
	
	public static String mergeJPS(String jps1 ,String jps2,boolean toCombineParity) {

		
		try {
			String out="";
			
			Vector<JPI> jpV1=JPI.parseJPS2JPV(jps1);
			Vector<JPI> jpV2=JPI.parseJPS2JPV(jps2);
		
			Vector<JPI> jpV=mergeJPV(jpV1,jpV2,toCombineParity);	
			
			Vector<String> jsV=new Vector<String>();
			for(JPI jp:jpV)
				jsV.add(jp.js);
			
			  
			out=JPI.combineJSVtoStr(jsV);
			if(out.isEmpty() && (!jps1.isEmpty() || !jps2.isEmpty()))
				return "NA";
			
			return out;
		}catch(Exception e) {
			
		}
		
		return "NA";
	}
	
	public static String makeBetterOneOfSameSpinValue(String js1,String js2,boolean toCombineParity) {
		try {
			JPI jp1=new JPI(js1);
			JPI jp2=new JPI(js2);
			
			JPI jp=makeBetterOneOfSameSpinValue(jp1,jp2,toCombineParity);
			if(jp==null)
				return "";
			
			return jp.js;
		}catch(Exception e) {
			
		}
		
		return "NA";

	}
	/*
	 * make a better JPI by combining jp1 and jp2 with the same spin
	 * based on firmness and completeness
	 * param: toCombineParity=true for combining j+ and j- to J to cover all possibilities
	 *                      false for choosing overlapping jps, so j+ and j- considered conflicting, return null 
	 * return a better JPI combining jp1 and jp2
	 * e.g., 1/2 is better than (1/2); 1/2+ better than 1/2 if toCombineParity=false otherwise 1/2 is better
	 */
	public static JPI makeBetterOneOfSameSpinValue(JPI jp1,JPI jp2,boolean toCombineParity) {
		try {
			if(!jp1.isUniqueSpin && !jp2.isUniqueSpin)
				return null;
			if(jp1.isUniqueSpin && !jp2.isUniqueSpin)
				return jp1;
			if(jp2.isUniqueSpin && !jp1.isUniqueSpin)
				return jp2;
			
			if(jp1.spinValue()!=jp2.spinValue() || jp1.isNA || jp2.isNA)
				return null;
			
			
			String parity="";
			boolean isFirmParity=jp1.isFirmParity||jp2.isFirmParity;
			boolean isFirmSpin=jp1.isFirmSpin||jp2.isFirmSpin;
			
			int parityCode=jp1.parityCode()*jp2.parityCode();
			
			//System.out.println(" j1="+jp1.js+" "+jp1.parityCode+" j2="+jp2.js+" "+jp2.parityCode+" final parityCode="+parityCode);
			
			if(parityCode==-1) {
				if(!toCombineParity)//jp1 and jp2 have opposite parities and considered as conflicting since toMergeParity=false, neither is good
					return null;
				else {
					parityCode=0;
					parity="";
					isFirmParity=false;
				}
					
			}else if(parityCode==0 && toCombineParity){
				//one has parity and the one has not.
				//When toMergeParity=true, choose jp with no parity to cover both values
				parity="";
				isFirmParity=false;
			}else {
				parityCode=jp1.parityCode;
				if(parityCode==0)
					parityCode=jp2.parityCode;
				
				parity=jp1.parity;
				if(parity.isEmpty())
					parity=jp2.parity;
			}
				

			String js=jp1.js;
			if(isFirmSpin)
				js=js.replace("(", "").replace(")","");
			
			
			JPI jp=new JPI(js);	
			jp.setParity(parity);
			jp.setParityCode(parityCode);
			
			jp.setIsFirmParity(isFirmParity);
			jp.setIsFirmSpin(isFirmSpin);
			
			checkJS(jp);
			
			return jp;
		}catch(Exception e) {
			
		}
		
		return null;
	
	}
	
	public static void checkJS(JPI jp) {
		if(jp.isNA())
			return;
		
		boolean isFirmSpin=jp.isFirmSpin;
		boolean isFirmParity=jp.isFirmParity;
		String js=jp.spin.replace("(", "").replace(")","");
		String parity=jp.parity.replace("(", "").replace(")","");
		
		if(isFirmSpin&&isFirmParity) {
			jp.js=js+parity;
		}else if(isFirmSpin) {
			if(!parity.isEmpty())
				jp.js=js+"("+parity+")";
			else
				jp.js=js;
		}else if(isFirmParity) {
			if(!js.isEmpty())
				jp.js="("+js+")"+parity;
			else
				jp.js=parity;
		}else {
			js=js+parity;
			if(!js.isEmpty())
				jp.js="("+js+")";
			else
				jp.js="";
		}
	}

	public static Vector<JPI> JPICoupling(String js1,String js2) {
		try {
			String[] jps1=parseJPS2Array(js1);
			String[] jps2=parseJPS2Array(js2);
			
			Vector<JPI> out=new Vector<JPI>();		
			Vector<JPI> temp=new Vector<JPI>();
			
			for(int i=0;i<jps1.length;i++) {
				JPI jp1=new JPI(jps1[i]);
				
				//if(js1.equals("7/2-") && js2.equals("0(+)"))
				//System.out.println(" jp1="+jps1[i]+" parityCode="+jp1.parityCode);
				
				for(int j=0;j<jps2.length;j++) {
					JPI jp2=new JPI(jps2[j]);
					
					//if(jps1[i].equals("7/2-") && js2.equals("0(+)"))
					//System.out.println("   jp2="+jps2[j]+" parityCode="+jp2.parityCode);
					
					temp=JPICoupling(jp1,jp2);
					if(temp==null)
						return null;
				
					//if(jps1[i].equals("7/2-") && js2.equals("0(+)"))
			    	//for(int k=0;k<temp.size();k++)
			    	//	System.out.println("          "+temp.get(k).js()+" parityCode="+temp.get(k).parityCode);
			    	
					out.addAll(temp);
				}
			}
			
	    	//for(int i=0;i<out.size();i++)
	    	//	System.out.println(out.get(i).js()+" parityCode="+out.get(i).parityCode);
			
			//System.out.println("1 size="+out.size());
			
			simplifyJPV(out,true);
			
	    	//for(int i=0;i<out.size();i++)
	    	//	System.out.println(out.get(i).js());
			//System.out.println("2 size="+out.size());
			
			sortJPV(out);

			return out;
		}catch(Exception e) {
			
		}
		return null;

	}
	
	public static Vector<JPI> JPICoupling(Vector<JPI> jpV1,Vector<JPI> jpV2){
		try {
			Vector<JPI> out=new Vector<JPI>();		
			Vector<JPI> temp=new Vector<JPI>();
			
			for(int i=0;i<jpV1.size();i++) {
				JPI jp1=jpV1.get(i);
				
				//System.out.println(" jp1="+jps1[i]+" parityCode="+jp1.parityCode);
				
				for(int j=0;j<jpV2.size();j++) {
					JPI jp2=jpV2.get(j);
					
					//System.out.println("   jp2="+jps2[j]+" parityCode="+jp2.parityCode);
					
					temp=JPICoupling(jp1,jp2);
					if(temp==null)
						return null;
				
			    	//for(int k=0;k<temp.size();k++)
			    	//	System.out.println("          "+temp.get(k).js()+" parityCode="+temp.get(k).parityCode);
			    	
					out.addAll(temp);
				}
			}
			
	    	//for(int i=0;i<out.size();i++)
	    	//	System.out.println(out.get(i).js()+" parityCode="+out.get(i).parityCode);
			
			//System.out.println("1 size="+out.size());
			
			simplifyJPV(out,true);
			
	    	//for(int i=0;i<out.size();i++)
	    	//	System.out.println(out.get(i).js());
			//System.out.println("2 size="+out.size());
			
			sortJPV(out);

			return out;
		}catch(Exception e) {
			
		}
		
		return null;

	}

	public static JPI JPIminusL(JPI jp,int L) {
		try {
			float spinValue=jp.spinValue;
			if(L<0)
				return null;
			if(L==0)
				return new JPI(jp.js);
			
			Vector<JPI> jpV=null;
			
			if(jp.isUniqueSpin) {
				if((L-spinValue)>0.01)//L=2, J=1.99999, L is equal to J
					return null;
				
				jpV=JPICoupling(jp.js,L+"+");
				return jpV.get(0);
			}else {//multiple jp values
				String[] jps=parseJPS2Array(jp.js);
				String s="";
				for(int i=0;i<jps.length;i++) {
					JPI jp1=new JPI(jps[i]);
					jp1=JPIminusL(jp1,L);
					if(jp1==null)
						continue;
					
					s+=jp1.js()+",";
				}
				
				if(s.endsWith(","))
					s=s.substring(0,s.length()-1);
				
				if(s.length()>0)
					return new JPI(s);
				else
					return null;
			}
		}catch(Exception e) {
			
		}

		return null;
	}
	
	public static JPI JPIplusL(JPI jp,int L) {
		try {
			if(L<0)
				return null;
			if(L==0)
				return new JPI(jp.js);
			
			Vector<JPI> jpV=null;
			
			if(jp.isUniqueSpin) {				
				jpV=JPICoupling(jp.js,L+"+");
				return jpV.lastElement();
			}else {//multiple jp values
				String[] jps=parseJPS2Array(jp.js);
				String s="";
				for(int i=0;i<jps.length;i++) {
					JPI jp1=new JPI(jps[i]);
					jp1=JPIplusL(jp1,L);
					if(jp1==null)
						continue;
					
					s+=jp1.js()+",";
				}
				
				if(s.endsWith(","))
					s=s.substring(0,s.length()-1);
				
				if(s.length()>0)
					return new JPI(s);
				else
					return null;
			}
		}catch(Exception e) {
			
		}

		return null;
	}
	
	public static Vector<JPI> JPICoupling(JPI jp1,JPI jp2) {

		try{

			Vector<JPI> out=new Vector<JPI>();
			Vector<String> jsV=new Vector<String>();
			
			if(!jp1.isUniqueSpin() || !jp2.isUniqueSpin())
				return out;
			
			if(jp1.isNA || jp2.isNA)
				return null;
			
            float j1=jp1.spinValue()*2;
            float j2=jp2.spinValue()*2;
            if(j1<0 || j2<0)
                return out;
             
            float minDjf=Math.abs(j1-j2);
            int minDji=(int)(minDjf+0.001);       
                            
            if(Math.abs(minDji-minDjf)>0.001)
                return out;
            
            float maxDjf=Math.abs(j1+j2);
            int maxDji=(int)(maxDjf+0.001);       
                            
            if(Math.abs(maxDji-maxDjf)>0.001)
                return out;
            
            
            String halfLabel="";
            int n=(maxDji-minDji)/2+1;
            int nStep=1;
            if(minDji%2==0) {
            	//js1=1/2,js2=3/2 or js1=1,js2=3, js1 and js2 same type (integer or half-integer)
            	//then (2*js1-2*js2) is always even, and all couplings are integer
            	halfLabel="";
                minDji=minDji/2;
                maxDji=maxDji/2;
                nStep=1;
            }else {
            	//js1 and js2 not same type, e.g., js1 interger, js2 half-integer, js1=1,js2=5/2
            	//then (2*js1-2*js2) is always odd, and all couplings are half interger
            	halfLabel="/2";
            	nStep=2;
            }
            
            boolean isFirmSpin=jp1.isFirmSpin()&&jp2.isFirmSpin();
            boolean isFirmParity=jp1.isFirmParity()&&jp2.isFirmParity();
            int parityCode=jp1.parityCode()*jp2.parityCode();
            String parity="";
            
            /*
            if(jp1.js().equals("7/2-") && jp2.js().equals("0(+)")) {
            System.out.println(" jp1: "+jp1.js+" isFirmSpin="+jp1.isFirmSpin+" isFirmParity="+jp1.isFirmParity+" jp2: "+jp2.js+" isFirmSpin="+jp2.isFirmSpin+" isFirmParity="+jp2.isFirmParity);         
            System.out.println(" j1="+j1+" j2="+j2+" n="+n+" minDji="+minDji+" maxDji="+maxDji+" isFirmSpin="+isFirmSpin+" isFirmParity="+isFirmParity);
            }
            */
            
            if(parityCode==1)
            	parity="+";
            else if(parityCode==-1)
            	parity="-";
            
            for(int i=0;i<n;i++) {
            	int j=minDji+i*nStep;
            	String js=String.valueOf(j)+halfLabel;
            	JPI jp=new JPI(js);
            	jp.setIsFirmParity(isFirmParity);
            	jp.setIsFirmSpin(isFirmSpin);
            	jp.setParity(parity);
            	jp.setParityCode(parityCode);
            	
            	js=js+parity;

       
                if(!jsV.contains(js)) {
    
                	checkJS(jp);
          
                    jsV.add(js);
                    out.add(jp);
                   
                }
            }

          
            return out;
		}catch(Exception e){            
			e.printStackTrace();
		}		
  
		return null;
	}
	
    public static String combineJSVtoStr(Vector<String> jsV) {
    	try {
            String out="";
            for(int i=0;i<jsV.size();i++) {
                String js=jsV.get(i);
                if(js.equalsIgnoreCase("NA"))
                	return "NA";
                
                out=combineTwoJStoStr(out,js);           
            }
     
            return out;
    	}catch(Exception e) {
    		
    	}
    	return "NA";

    
    }
    /*
     * combine a list of JPIs into a single string
     */
    public static String combineJPVtoStr(Vector<JPI> jpV) {
    	try {
            String out="";
            for(int i=0;i<jpV.size();i++) {
                JPI jp=jpV.get(i);
                if(jp.isNA) {
                	return "NA";
                }
                out=combineTwoJStoStr(out,jp.js());
                               
            }

            return out;
    	}catch(Exception e) {
    		
    	}
    	return "NA";

    }
    
    public static String combineTwoJStoStr(String js1,String js2) {
    	try {
            String out="";
            if(js1.isEmpty())
                return js2;
            if(js2.isEmpty())
                return js1;
            
            if(js1.equalsIgnoreCase("NA") || js2.equalsIgnoreCase("NA"))
            	return "NA";
            
            if(!js1.contains("(") || !js2.contains("("))
                return js1+","+js2;
            
            if(js2.startsWith("(")) {
                
                int n1=js1.indexOf(")");
                int n2=js2.indexOf(")");   
                boolean canBeCombined=false;
                if(js1.endsWith(")") && js2.endsWith(")")) {//js1=1/2,(3/2+) js2=(5/2+) combined=1/2,(3/2+,5/2+)
                    canBeCombined=true;
                }else {
                    String p1=js1.substring(n1+1);
                    String p2=js2.substring(n2+1);
                    if(p1.equals(p2) && "+-".contains(p1))//js1=1/2,(3/2)+ js2=(5/2)+ combined=1/2,(3/2,5/2)+
                        canBeCombined=true;
                }
                
                if(canBeCombined) 
                    out=js1.substring(0,n1)+","+js2.substring(1);
                else
                    out=js1+","+js2;
            }else
                out=js1+","+js2;


            return out;
    	}catch(Exception e) {
    		
    	}
    	return "NA";

    }	
    
	/*
	 * determine which one is lower as lower bound based on spin value and firmness of spin and parity
	 *     1. different spin values: return the one with the smaller spin value
	 *     2. same spin values: return the one with the higher firmness of spin and parity
	 *           a. different firmness of spin: return the one with firmer spin (no "()")
	 *           b. same firmness of spin: return the one with parity if the other does have
	 *                                                  or with firmer parity
	 * e.g., 1/2 lower (better) than (1/2), as a lower bound
	 */
    public static JPI betterLowerJPI(JPI jp1,JPI jp2) {
    	if(jp1==null || jp1.isNA())
    		return jp2;
    	if(jp2==null || jp2.isNA())
    		return jp1;
    	
		if(!jp2.isUniqueSpin || !jp1.isUniqueSpin) {
			return null;
		}
		
		int j1=(int)(jp1.spinValue*2+0.00001);
		int j2=(int)(jp2.spinValue*2+0.00001);
		if(j1<j2)
			return jp1;
		if(j1>j2)
			return jp2;
		
		//same spin 					
    	if(jp1.isFirmSpin && !jp2.isFirmSpin)
    		return jp1;
    	if(!jp1.isFirmSpin && jp2.isFirmSpin)
    		return jp2;

    	//here jp1 and jp2 have the same firmness for spin
    	//check parity
    	int p1=jp1.parityCode;
    	int p2=jp2.parityCode;
    	if(p1*p2<0)//same spin but different parity, neither one can be seletected as the lower bound
    		return null;
		
    	if(p1!=0 && p2==0)
    		return jp1;
    	if(p1==0 && p2!=0)
    		return jp2;
    	if(p1==0 && p2==0)
    		return jp1;
    	
    	//same parity, then check parity firmness
    	if(jp1.isFirmParity && !jp2.isFirmParity)
    		return jp1;
    	if(!jp1.isFirmParity && jp2.isFirmParity)
    		return jp2;
    	
    	//here same spin and same parity, with the same firmess
    	return jp1;
    }
    
	/*
	 * determine which one is higher as upper bound based on spin value and firmness of spin and parity
	 * NOTE that the rule for inputs with the same spin is the same as (not opposite to) that of lowerBound
	 *     1. different spin values: return the one with the higher spin value
	 *     2. same spin values: return the one with the higher firmness of spin and parity
	 *           a. different firmness of spin: return the one with firmer spin (no "()")
	 *           b. same firmness of spin: return the one with parity if the other does have
	 *                                                  or with firmer parity
	 * e.g., 1/2 is higher (better) than (1/2) as an upper bound
	 * 
	 * NOTE that for jp1 and jp2 with the same spins, lowerJPI() and upperJPI() return the same
	 */
    public static JPI betterUpperJPI(JPI jp1,JPI jp2) {
    	if(jp1==null || jp1.isNA())
    		return jp2;
    	if(jp2==null || jp2.isNA())
    		return jp1;
    	
		if(!jp2.isUniqueSpin || !jp1.isUniqueSpin) {
			return null;
		}
		
		int j1=(int)(jp1.spinValue*2+0.00001);
		int j2=(int)(jp2.spinValue*2+0.00001);
		if(j1>j2)
			return jp1;
		if(j1<j2)
			return jp2;
		
		//same spin 					
    	if(jp1.isFirmSpin && !jp2.isFirmSpin)
    		return jp1;
    	if(!jp1.isFirmSpin && jp2.isFirmSpin)
    		return jp2;

    	//here jp1 and jp2 have the same firmness for spin
    	//check parity
    	int p1=jp1.parityCode;
    	int p2=jp2.parityCode;
    	if(p1*p2<0)//same spin but different parity, neither one can be seletected as the lower bound
    		return null;
		
    	if(p1!=0 && p2==0)
    		return jp1;
    	if(p1==0 && p2!=0)
    		return jp2;
    	if(p1==0 && p2==0)
    		return jp1;
    	
    	//same parity, then check parity firmness
    	if(jp1.isFirmParity && !jp2.isFirmParity)
    		return jp1;
    	if(!jp1.isFirmParity && jp2.isFirmParity)
    		return jp2;
    	
    	//here same spin and same parity, with the same firmess
    	return jp1;
    } 

	public static String buildJPIPatternRegex() {
        // Base spin types - no leading zeros
        //String halfInt = "(?:[1-9]\\d*)?[13579]/2(?!\\.\\d)";  // 1/2, 3/2, 11/2 (no 01/2, 03/2)
        //String integer = "(?:0|[1-9]\\d*)(?!\\.\\d)";  // 0, 1, 2, 10 (no 01, 001) - but not followed by .digit
        //String integer = "(?:0|[1-9]\\d*)(?![\\d\\.])";
        
       // Negative lookahead prevents matching partial numbers and stops at delimiters (|, space, comma, parens, etc.)
        //String halfInt = "(?:[1-9]\\d*)?[13579]/2(?![\\d\\.\\|])";
        //String integer = "(?:0|[1-9]\\d*)(?![\\d\\.\\|])";
        
		String halfInt = "(?:[1-9]|[1-9]\\d)?[13579]/2(?![\\d\\.\\|])";
        String integer = "(?:0|[1-9]|[1-4][0-9]|50)(?![\\d\\.\\|])";
        
        // Single spin with optional parens/parity	        
        String singleHalf = "(?:(?:\\(" + halfInt + "[+\\-]?\\)|\\(" + halfInt + "\\)[+\\-]?)(?![+\\-]|\\([+\\-]\\))|"
        	    + halfInt + "(?:[+\\-]|\\([+\\-]\\))?)";
       	
        //any integer spin-parity 
        String singleInt = "(?:(?:\\(" + integer + "[+\\-]?\\)|\\(" + integer + "\\)[+\\-]?)(?![+\\-]|\\([+\\-]\\))|"
        	    + integer + "(?:[+\\-]|\\([+\\-]\\))?)";
        
        //any integer spin-parity excluding bare spin like " 33 " which might not be spin
        String singleIntNoBare="(?:(?:\\(" + integer + "[+\\-]?\\)|\\(" + integer + "\\)[+\\-]?)(?![+\\-]|\\([+\\-]\\))|"
        	    + integer + "(?:[+\\-]|\\([+\\-]\\)))";
        
        //bare integer spin that is validated to be valid spin but not something else
        String singleIntBare="(?<=J(?:\\|p)?=)"+integer;
        
        // Group of spins in single (): (1/2,3/2-,5/2)
        String groupHalf = "\\(" + halfInt + "[+\\-]?(?:," + halfInt + "[+\\-]?)+\\)";
        String groupInt = "\\(" + integer + "[+\\-]?(?:," + integer + "[+\\-]?)+\\)";
        
        // Item = either single or group
        String halfItem = "(?:" + groupHalf + "|" + singleHalf + ")";
        String intItemForList = "(?:" + groupInt + "|" + singleInt + ")";
        
        String intItemAlone = "(?:" + groupInt + "|" + singleIntNoBare + "|" + singleIntBare + ")";
        
        // Format1: (spin,spin,...)(+) - shared parity in second ()
        //String multFormat1Half = "\\(" + halfInt + "(?:," + halfInt + ")+\\)"+"\\([+\\-]\\)";
        //String multFormat1Int = "\\(" + integer + "(?:," + integer + ")+\\)"+"\\([+\\-]\\)";
        
        String multFormat1Half = "\\(" + halfInt + "(?:," + halfInt + ")+\\)" + "(?:\\([+\\-]\\)|[+\\-])";
        String multFormat1Int = "\\(" + integer + "(?:," + integer + ")+\\)" + "(?:\\([+\\-]\\)|[+\\-])";
            
        // Format2: multiple spins with spaces allowed around commas
        String multFormat2Half = halfItem + "(?:\\s*,\\s*" + halfItem + ")+";  // all half-integers
        String multFormat2Int = intItemForList + "(?:\\s*,\\s*" + intItemForList + ")+";  // all integers
        
        return "(?:" + 
            multFormat1Half + "|" + multFormat1Int + "|" +
            multFormat2Half + "|" + multFormat2Int + "|" +
            halfItem + "|" + intItemAlone +
        ")";
	}
	
    //return overlapping JPI in corresponding input Vector of JPIs
    //elem#1: overlapping JPI from jpV1
    //elem#2: overlapping JPI from jpV2
    public static Vector<Vector<JPI>> findOverlapJPV(Vector<JPI> jpV1,Vector<JPI> jpV2) {
       	Vector<Vector<JPI>> out=new Vector<Vector<JPI>>();
    	try {
    		Vector<JPI> overlapV1=new Vector<JPI>();
    		Vector<JPI> overlapV2=new Vector<JPI>();
    		for(JPI jp1:jpV1) {
    			for(JPI jp2:jpV2) {
    				if(jp1.spinValue==jp2.spinValue) {
    					overlapV1.add(jp1);
    					overlapV2.add(jp2);
    				}
    			}
    		}
    		
    		if(overlapV1.size()>0) {
    			out.add(overlapV1);
    			out.add(overlapV2);
    		}
    		
    	}catch(Exception e) {
    		
    	}
    	return out;
    }

    
    /**
     * 
     * @param jpV1
     * @param jpV2
     * @return  1 if jpV1 range is better than jpV2
     *         -1 if jpV2 range is better than jpV1
     *          0 if jp1V is exactly the same as jpV2
     *         10 jpV1 and jpV2 overlap, lower of jpV1 to upper of jpV2
     *        -10 jpV1 and jpV2 overlap, lower of jpV2 to upper of jpV1
     *     -10000 jpV1 and jpV2 are not comparable (no overlap)
     */
    //assuming jpV1 and jpV2 are regular ranges of JPIs in ascending orders
    public static int compareJPVRange(Vector<JPI> jpV1,Vector<JPI> jpV2) {
    	try {
    		JPI lower1=jpV1.get(0);
    		JPI upper1=jpV1.lastElement();
    		
    		JPI lower2=jpV2.get(0);
    		JPI upper2=jpV2.lastElement();
    		
    		boolean isBetterLower1=(lower1==betterUpperJPI(lower1,lower2));
    		boolean isBetterUpper1=(upper1==betterLowerJPI(upper1,upper2));
    		if(isBetterLower1 && isBetterUpper1) 
    			return 1;
    		if(!isBetterLower1 && !isBetterUpper1)
    			return -1;
    		if(isBetterLower1) {
    			if(upper2.spinValue>=lower1.spinValue)
    				return 10;
    			else
    				return -10000;//not comparable
    		}else {//isBetterUpper1
    			if(lower2.spinValue<=upper1.spinValue)
    				return -10;
    			else
    				return -10000;//not comparable
    		}
    		
    	}catch(Exception e) {
    		
    	}
    	return -10000;//not comparable
    }
    
    public static int compareJPS(String jps1,String jps2) {
    	try {
    		if(jps1.equalsIgnoreCase(jps2))
    			return 0;
    		
    		String[] jpA1=parseJPS2Array(jps1);
    		String[] jpA2=parseJPS2Array(jps2);
    		
    		Vector<JPI> jpV1=new Vector<JPI>();		
    		Vector<JPI> jpV2=new Vector<JPI>();
    		
    		for(int i=0;i<jpA1.length;i++)
    			jpV1.add(new JPI(jpA1[i]));
    		
    		for(int i=0;i<jpA2.length;i++)
    			jpV2.add(new JPI(jpA2[i]));
    		
    		return compareJPVRange(jpV1,jpV2);
    		
    	}catch(Exception e) {
    		
    	}
    	return -10000;
    }
    
    public static int compareJPSV(Vector<String> jpsV1,Vector<String> jpsV2) {
    	try {
    		
    		Vector<JPI> jpV1=new Vector<JPI>();		
    		Vector<JPI> jpV2=new Vector<JPI>();
    		
    		for(int i=0;i<jpsV1.size();i++)
    			jpV1.add(new JPI(jpsV1.get(i)));
    		
    		for(int i=0;i<jpsV2.size();i++)
    			jpV2.add(new JPI(jpsV2.get(i)));
    		
    		return compareJPVRange(jpV1,jpV2);
    		
    	}catch(Exception e) {
    		
    	}
    	return -10000;
    }
    
    public static Vector<JPI> parseJPS2JPV(String js){
    	Vector<JPI> out=new Vector<JPI>();
    	try {
    		String[] jsA=parseJPS2Array(js);
    		for(int i=0;i<jsA.length;i++) {
    			out.add(new JPI(jsA[i]));
    		}
    	}catch(Exception e) {
    		
    	}
    	return out;
    }
    
    public static Vector<String> parseJPS2JSV(String js){
    	Vector<String> out=new Vector<String>();
    	try {
    		String[] jsA=parseJPS2Array(js);
    		for(int i=0;i<jsA.length;i++) {
    			out.add(jsA[i]);
    		}
    	}catch(Exception e) {
    		
    	}
    	return out;
    }
    public static String[] parseJPS2Array(String js){
        String[] jv=new String[0];
        String s=js.trim();
        if(s.isEmpty())
           return jv;

        boolean defaultSplit=false;

        s=s.replace("&", ",");
        
        
        if(s.startsWith("L") || s.startsWith("(L")) {
            int n=s.indexOf("L");
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

            
            String s1=s.substring(n1+1,n2);
                      
            if(s1.indexOf("LE")==0 || s1.indexOf("LT")==0) {
                try {
                    String s2=s1.substring(2).trim();
                    String parity="";
                    if(s2.endsWith("+") || s2.endsWith("-")) {//"LE4+"="1,2,3,4+"
                        parity=s2.charAt(s2.length()-1)+"";
                        s2=s2.substring(0,s2.length()-1);
                    }
                    
                    //System.out.println("  s1="+s1+" s2="+s2+" parity="+parity);
                    
                    int maxJ=-1;
                    int step=1;
                    if(Str.isInteger(s2)) {
                        maxJ=Integer.parseInt(s2);
                    }else if(s2.endsWith("/2")) {
                        maxJ=Integer.parseInt(s2.substring(0,s2.length()-2));
                        step=2;
                    }
                    
                    if(s1.indexOf("LT")==0) {
                        maxJ=maxJ-step;
                        parity="";
                    }
                    
                    s2="";
                    if(step==1) {
                        for(int j=0;j<maxJ;j++)
                            s2+=""+j+",";
                        
                        if(maxJ>=0)
                            s2+=maxJ+parity;
                    }else if(step==2) {
                        for(int j=1;j<maxJ;j=j+2)
                            s2+=""+j+"/2,";
                        
                        if(maxJ>0)
                            s2+=maxJ+"/2"+parity;
                    }

                    
                    if(s2.length()>0) 
                        s=prefix+s2+postfix;

                    //System.out.println(" s1="+s1+" s2="+s2+" s="+s+" prefix="+prefix+" postfix="+postfix);
                }catch(Exception e) {
                    
                }
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
            s=s.replace("(","").replace(")","").replace(',',' ').replace("+","").replace("-","");
            return s.split(" ");
         }
         
         boolean needBracket=false;
         int start=0;

         //for(int i=0;i<jv.length;i++) System.out.println("  i="+i+"  j="+jv[i]);
             
         String[] out=new String[jv.length];
         
         for(int i=0;i<jv.length;i++){
           s=jv[i];
           jv[i]=jv[i].replace("(","").replace(")","").trim();
           int nlb=Str.countPattern(s, "(");
           int nrb=Str.countPattern(s, ")");
           
           boolean assigned=false;
           
           if(s.indexOf("(")==0 && nlb>nrb){
              needBracket=true;
              start=i;
           }else if(s.indexOf(")")>0 && nrb>nlb){             
              if(needBracket){
                  int p=s.indexOf(")");
                  jv[i]=jv[i].substring(0,p);
                  for(int j=start;j<=i;j++) {
                                          
                      //System.out.println("i="+i+" s="+s+" jv[j]="+jv[j]);
                     
                      out[j]="("+jv[j]+s.substring(p);
                      out[j]=out[j].replace(")(","");
                  }
                  
                  assigned=true;
                  needBracket=false;
              }

           }
           
           //System.out.println("## s="+s+" jv[i]="+jv[i]+" assigned="+assigned);
           
           if(!assigned) {
               //out[i]=jv[i];
               out[i]=s.replace(")(","");
           }
         }
   
        
         return out;
    }
    
    public static boolean isEqualJPS(String js1,String js2) {
        try {
            js1=js1.toUpperCase().trim();
            js2=js2.toUpperCase().trim();
            if(js1.equals(js2))
                return true;
            
            ArrayList<String> jv1=new ArrayList<String>(Arrays.asList(parseJPS2Array(js1)));
            ArrayList<String> jv2=new ArrayList<String>(Arrays.asList(parseJPS2Array(js2)));

            ArrayList<String> tempJV2=new ArrayList<String>();
            tempJV2.addAll(jv2);
            
            for(String s:jv1) {
                jv2.remove(s);
            }
            for(String s:tempJV2) {
                jv1.remove(s);
            }
            
            if(jv2.size()==0 && jv1.size()==0)
                return true;
            
        }catch(Exception e) {
        }
        
        return false;
    }
    
    /*
     * parse string of a single JPI
     */
    public static JPI parseJPS(String js) {
    	try {
    		JPI jp=new JPI(js);
    		return jp;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public static boolean isSingleJPI(String js) {
    	try {
    		JPI jp=parseJPS(js);
    		return jp.isUniqueSpin;
    	}catch(Exception e) {
    		
    	}
    	
    	return false;
    }
    
    public static boolean isValidJPS(String js) {
    	try {
    		js=js.trim();
	        if (js == null || js.trim().isEmpty()) 
	        	return false;
	        
	        boolean isValid=false;
	        
    		String jpiPattern=buildJPIPatternRegex();
    		    		
    		//Pattern pattern=Pattern.compile("^("+jpiPattern+")$");//ensure the entire string matches
    		//isValid=pattern.matcher(js).matches();
    		
    		isValid=Pattern.matches(jpiPattern,js);

    		return isValid;
    	}catch(Exception e) {
    		
    	}
    	
    	return false;
    }
    
    public static String extractJPS(String str) {
    	try {
    		
    		str=Str.removeExtraSpaces(str);
    		str=str.replace(", ",",").replace(" ,",",").replace("( ","(").replace(" )",")");
    		
    		//System.out.println(str);
    		
    		Pattern pattern =null;
    		if(str.contains("/2"))
    			pattern=Pattern.compile(regexHalf, Pattern.CASE_INSENSITIVE);
    		else
    			pattern=Pattern.compile(regexInteger, Pattern.CASE_INSENSITIVE);
    		
    		Matcher matcher=pattern.matcher(str);
    		if(matcher.find()) {
    			String s0=matcher.group();
    			if(s0==null || s0.trim().isEmpty())
    				return "";
    			
    			String s=s0.trim();
    			
    			int p=matcher.start();
    			
    			s=s.trim();
    			char c=s.charAt(s.length()-1);
    			if(".,;".contains(c+""))
    				s=s.substring(0,s.length()-1).trim();
    			c=s.charAt(0);
    			if("=.,;".contains(c+""))
    				s=s.substring(1).trim();
    			
    			String remainingS=str.substring(p+s0.length()).trim();
    			
    			//check parentheses
    			if(Str.hasUnmatchedBracket(s,"()")) {
    				if(s.endsWith(")")) {
    					s="("+s;
    					if(remainingS.length()>0) {
    						c=remainingS.charAt(0);
    						if("+-".contains(c+""))
    							s+=c;
    					}
    					
    				}
    			}else if(str.substring(0,p).trim().endsWith("(") && remainingS.startsWith(")") && !s.startsWith("(")) {
    				s="("+s+")";
					if(remainingS.length()>1) {
						c=remainingS.charAt(1);
						if("+-".contains(c+""))
							s+=c;
					}
    			}
    			return s;
    		}
    	}catch(Exception e) {
    		
    	}
    	
    	return "";
    }
}

class JPIComparator implements Comparator<Object>{
    public int compare(Object one,Object two){
    	JPI a=null,b=null;
    	if(one instanceof JPI)
    		a=(JPI)one;
    	else
    		a=new JPI((String)one);
    		
    	if(two instanceof JPI)
    		b=(JPI)two;
    	else
    		b=new JPI((String)two);

        int out;
        if(a.spinValue()<b.spinValue())
            out=-1;
        else if(a.spinValue()>b.spinValue())
            out=1;
        else if(a.spinValue()<0){//no spin for both
            out=0;
        }else{//spin is the same, then compare parity
            
            if(a.parityCode()<b.parityCode())
                out=-1;
            else if(a.parityCode()>b.parityCode())
                out=1;
            else
                out=0;
        }
        
        //System.out.println(" a="+a.js()+" "+ a.spinValue()+" "+a.parityCode()+" b="+b.js()+" "+b.spinValue()+" "+b.parityCode()+" out="+out);
        return out;
    }
}
