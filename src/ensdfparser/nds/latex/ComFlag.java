package ensdfparser.nds.latex;

import java.util.Vector;

import ensdfparser.nds.control.NDSControl;

/*contains information for writing comment headers */
public class ComFlag {
    private Vector<String> headV;
    @SuppressWarnings("rawtypes")
	private Vector<Vector> flagVV;
    private Vector<String> flags;
    private Vector<String> noflagHeads;;
    
    private String symbol;
    private static int nFlags;
    
    private int index;
    
  //\& and @ occupy more space than other symbols, but one can use makebox[width]{text} to make a box with fixed width
    
    //private String[] symbs={"\\dagger","\\ddagger","\\#","@","\\&","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
    //		"1","2","3","4","5","6","7","8","9","!","~","|","/","<",">",
    //		"\\alpha","\\beta","\\eta","\\lambda","\\varphi","\\chi","\\psi","\\omega","\\gamma","\\delta","\\epsilon","\\theta","\\rho","\\mu","\\nv","\\S","\\zeta",
    //		"\\Delta","\\Theta","\\kappa","\\sigma","\\upsilon"};
    //private String[] symbs={"\\dagger","\\ddagger","\\S","\\#","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

    //private String[] symbsLetterFirst={"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
    //		"1","2","3","4","5","6","7","8","9","!","~","|","/","<",">","\\dagger","\\ddagger","\\#","@","\\&",
    //		"\\alpha","\\beta","\\eta","\\lambda","\\varphi","\\chi","\\psi","\\omega","\\gamma","\\delta","\\epsilon","\\theta","\\rho","\\mu","\\nv","\\S","\\zeta",
    //		"\\Delta","\\Theta","\\kappa","\\sigma","\\upsilon"};

    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public ComFlag(Vector<String> h,Vector<Vector> f){
        flags=new Vector<String>();
        noflagHeads=new Vector<String>();
        		
        headV=h;
        flagVV=f;
        
        if(NDSControl.useLetterFootnoteMarkFirst)
        	symbol=NDSControl.footmarkSymbsLetterFirst[nFlags];
        else
        	symbol=NDSControl.footmarkSymbs[nFlags];
        
        Vector<String> temp;
        for(int i=0;i<f.size();i++){
            temp=(Vector)f.elementAt(i);
            if(temp.isEmpty())
            	noflagHeads.add(headV.elementAt(i));
            
            for(int j=0;j<temp.size();j++) 
            	flags.add(temp.elementAt(j));
        }
        
        index=nFlags;
        
        nFlags++;
    }
    
    public Vector<String> headV(){return headV;}
    public int nHeads(){return headV.size();}
    public String flags(){
    	String flagStr="";
    	for(String s:flags)
    		flagStr+=s;
    	
    	return flagStr;
    }
    
    public Vector<String> flagV(){
    	return flags;
    }
    
    public int getIndex(){return index;}
    public void setIndex(int i){index=i;}
    
    public static void setNumFlags(int i){ nFlags=i; }
    public static int getNumFlags(){return nFlags;}
    public boolean hasFlags(){ 
        if(flags.size()>0) return true;
        return false;
    }
    
    public boolean hasHead(String head){
       String temp;
        for(int i=0;i<headV.size();i++){
           temp=(String)headV.elementAt(i);
           if(head.equals(temp)) return true;
       }
       return false;
    }
    
    public boolean hasHead(){
    	if(headV.size()>0)
    		return true;
    	
    	return false;
    }
    
    public boolean hasBandHead(){
        String temp;
        for(int i=0;i<headV.size();i++){
           temp=(String)headV.elementAt(i);
           if(temp.equals("BAND")) 
        	   return true;
           if(temp.toUpperCase().indexOf("SEQ")>=0)
        	   return true;
       }
    	return false;
    }
    
    public String getSymbol(){ return symbol;}
    
    public Vector<String> getFlags(){return flags;}//Jun, 4/27/2016
    
    //check if it contains any flag in string flagStr
    public boolean hasFlagAny(String flagStr){
        for(int i=0;i<flags.size();i++){
            if(flagStr.contains((String)flags.elementAt(i))) return true;
        }
        return false;
    }
    
    //check if head has any flag in string flagStr
    @SuppressWarnings("rawtypes")
	public boolean hasFlagAny(String head,String flagStr){//there could be cases like, "E,E(A)$"

    	for(int i=0;i<headV.size();i++){
    		//debug
    		//if(head.equals("BAND")) System.out.println("In ComFlag line 106: head="+head+" flagStr="+flagStr+" headV"+i+"="+headV.elementAt(i));
    		
    		if(head.equals(headV.elementAt(i))){
    			Vector flagV=flagVV.elementAt(i);
    			for(int j=0;j<flagV.size();j++){
    				
    				//debug
    				//if(head.equals("BAND")) System.out.println("In ComFlag line 111: head="+head+" flagStr="+flagStr+" flagV"+j+"="+flagV.elementAt(j));
    				
    				if(flagStr.contains((String)flagV.elementAt(j)))
    						return true;
    			}

    		}
    	}
    	   	
    	return false;
    	
    }
    
    
    public boolean hasNoFlagHead(String head){
    	for(int i=0;i<headV.size();i++){
    		if(head.equals(headV.elementAt(i)) && flagVV.elementAt(i).isEmpty())
    			return true;
    	}
    	
    	return false;
    }
    
    public boolean hasNoFlagHead(){
    	if(headV.size()<=0)
    		return false;
    	
    	for(int i=0;i<headV.size();i++){
    		if(flagVV.elementAt(i).isEmpty())
    			return true;
    	}
    	
    	return false;
    }
    
    //check if head has any flag
    public boolean checkHead(String head){//there could be cases like, "E,E(A)$"
    	for(int i=0;i<headV.size();i++){
    		if(head.equals(headV.elementAt(i)) && !flagVV.elementAt(i).isEmpty())
    			return true;
    	}
    	
    	return false;
    }
    
    @SuppressWarnings("rawtypes")
	public boolean[] checkHead(String head,String flagStr){//there could be cases like, "E,E(A)$"
		boolean[] hasFlag={false,false,false,false,false,false};
		int count=0;
    	for(int i=0;i<headV.size();i++){
    		if(head.equals(headV.elementAt(i))){
    			Vector flagV=flagVV.elementAt(i);
    			for(int j=0;j<flagV.size();j++){
    				if(flagStr.contains((String)flagV.elementAt(j))){
    						hasFlag[count]=true;
    						break;
    				}
    			}
    			
    			count++;
    			if(count>=6)
    				break;

    		}
    	}
    	
    	boolean[] hf=new boolean[count];
    	for(int i=0;i<count;i++)
    		hf[i]=hasFlag[i];
    	
    	return hf;
    	
    }
    
    public int[] nflagsOfHead(String head){//there could be cases like, "E,E(A)$"
		int[] nflags={0,0,0,0,0,0};
		int count=0;
		
    	for(int i=0;i<headV.size();i++){
    		if(head.equals(headV.elementAt(i))){
    			nflags[count]=flagVV.elementAt(i).size();
    			count++;
    			if(count>=6)
    				break;
    		}
    	}
    	
    	int[] nfs=new int[count];
    	for(int i=0;i<count;i++)
    		nfs[i]=nflags[i];
    	
    	return nfs;
    }
}
