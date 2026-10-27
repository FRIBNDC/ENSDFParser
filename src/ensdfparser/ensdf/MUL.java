package ensdfparser.ensdf;
import java.util.Vector;

import ensdfparser.nds.ensdf.EnsdfUtil;


public class MUL {
	private String ms="";
    private String[] poleA=new String[0];
    private String[] mulA=new String[0];//MUL components
    private boolean isStretched=false;
    private boolean isMixed=false;
    private boolean[] isFirmA=new boolean[0];//isFirm for each component, e.g., D(+Q), isFirmMComps[0]=true, isFirmMComps[1]=false;
  
    
    public MUL(String ms){
    	setValue(ms);
    }
    
    public boolean equals(MUL mul) {
    	if(mul.ms.equals(ms))
    		return true;
    	
    	return false;
    }
    
    public void setValue(String ms){
  
    	this.ms=ms;
    	
    	ms=ms.replace("(", "").replace(")","").trim();
    	
        mulA=EnsdfUtil.parseMUL(ms);
        isFirmA=new boolean[mulA.length];
        Vector<String> tempV=EnsdfUtil.findGammaPoleExp(ms);
        poleA=new String[mulA.length];
        tempV.toArray(poleA);
        
        if(ms.contains("+") && mulA.length>1) {
        	isMixed=true;
        	isStretched=false;
        }else if(mulA.length==1) {
        	isMixed=false;
        }

        for(int i=0;i<mulA.length;i++) {
        	String s=mulA[i];
        	if(ms.contains(s+")")||ms.contains("("+s)||ms.contains("["+s)||ms.contains(s+"]")) {
        		isFirmA[i]=false;
        	}else {
        		isFirmA[i]=true;
        	}
        }
    }

    public String ms() {return ms;}
      
    public String[] poleArray() {return poleA;}
    
    public boolean[] isFirmArray() {return isFirmA;}

    public boolean isMixed() {return isMixed;}
    
    public boolean isStretched() {return isStretched;}
}
