package ensdfparser.nds.latex;

import java.util.Vector;

import ensdfparser.ensdf.Comment;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;

/* Wrap of all footnote comments
 * contains information for writing footnote comments 
 */
public class Footnotes {

    private Vector<Comment> footnoteComments=new Vector<Comment>();
    private Vector<FlagSet> flagSets=new Vector<FlagSet>();
    private Vector<String> linkIDs=new Vector<String>();
    
    Vector<String> flagSymbols = new Vector<String>();
    Vector<String> usedSymbols=new Vector<String>();
    
	private ENSDF ens;
	
    @SuppressWarnings("unused")
	private TableControl tc;
    private String flagsInTable="";
    
    //These variables are used to add footnotes
    String let="abcdefghijklmnopqrstuvwyz123456789!~|/<>";//"^" flag for unplaced gamma, see ENSDF.java Line913
                                                          //"x" reserved for unplaced gamma in output   
    
    //extra symbols for footnote marks
    //private String[] symbs={"\\alpha","\\beta","\\eta","\\lambda","\\varphi","\\chi","\\psi","\\omega","\\gamma","\\delta","\\epsilon",
    //		"\\theta","\\rho","\\mu","\\nv","\\zeta","\\Delta","\\Theta","\\kappa","\\sigma","\\upsilon"};
    
    public Footnotes(){
        footnoteComments.clear();
        flagSets.clear();
        flagSymbols.clear();
        usedSymbols.clear();
        
        /*
        flagSymbols.addElement("\\dag");
        flagSymbols.addElement("\\ddag");
        flagSymbols.addElement("\\S");
        flagSymbols.addElement("\\&");//\& and @ occupy more space than others, but one can use makebox[width]{text} to make a box with fixed width
        flagSymbols.addElement("@");
        flagSymbols.addElement("\\#");
        
        for(int i=0;i<let.length();i++)
        	flagSymbols.addElement(""+let.charAt(i));
        
        for(int i=0;i<symbs.length;i++){
        	if(!flagSymbols.contains(symbs[i]))
        		flagSymbols.addElement(symbs[i]);
        }
        */
        
        if(NDSControl.useLetterFootnoteMarkFirst) {
        	for(int i=0;i<NDSControl.footmarkSymbsLetterFirst.length;i++)
        		flagSymbols.addElement(NDSControl.footmarkSymbsLetterFirst[i]);
        }else {
        	for(int i=0;i<NDSControl.footmarkSymbs.length;i++)
        		flagSymbols.addElement(NDSControl.footmarkSymbs[i]);
        }
    }
    

    public Footnotes(ENSDF ens){
    	this();
    	setENSDF(ens);
    }
    
    public Footnotes(ENSDF ens,TableControl tc){
    	this();
    	setENSDF(ens);
    	setTableControl(tc);
    }
    
	public Footnotes(Vector<Comment> coms){   
    	this();
        setFootnotes(coms);
    }
    
	public Footnotes(ENSDF ens,Vector<Comment> coms){   
    	this(ens);
        setFootnotes(coms);
    }

	public void setENSDF(ENSDF ens){
		this.ens=ens;		
	}
	
	public void setTableControl(TableControl tc){
		this.tc=tc;		
	}
	
    public boolean isEmpty(){
    	if(footnoteComments.size()<=0)
    		return true;
    	
    	return false;
    }
    
    public boolean hasFootmark(String flags,String type){
    	String tempFlags=flags;
    	int n=flags.length();
    	if(n<=0){
    		n=1;
    		tempFlags=" ";
    	}
    	
    	for(int i=0;i<n;i++){
    		String flag=""+tempFlags.charAt(i);
    		flag=flag.trim();
        	for(int j=0;j<flagSets.size();j++){
        		FlagSet fs=flagSets.elementAt(j);
        		if(flag.equals(fs.flag)&&type.equals(fs.type))
        			return true;
        	}
    	}
    	
    	return false;
    }
  
    //find number of footnote marks of given type in given flags
    //when flags is empty, it is for heading of given type.
    public int nMarks(String flags,String type){
    	int count=0;
    	
    	String tempFlags=flags;
    	int n=flags.length();
    	if(n<=0){
    		n=1;
    		tempFlags=" ";
    	}
    	
    	for(int i=0;i<n;i++){
    		String flag=""+tempFlags.charAt(i);
    		flag=flag.trim();//flag="" for heading marks
        	for(int j=0;j<flagSets.size();j++){
        		FlagSet fs=flagSets.elementAt(j);
        		if(flag.equals(fs.flag)&&type.equals(fs.type))
        			count++;
        	}
    	}
    	
    	//debug
    	//System.out.println("In Footnotes: line 135: flags="+flags+" type="+type+" count="+count);
    	return count;
    }
    
    
    public String getFlagSymbols(String flags,String type){
    	String symbols="";
    	String tempFlags=flags;
    	int n=flags.length();
    	if(n<=0){
    		n=1;
    		tempFlags=" ";
    	}
    	
    	for(int i=0;i<n;i++){
    		String flag=""+tempFlags.charAt(i);
    		flag=flag.trim();
    		
        	for(int j=0;j<flagSets.size();j++){
        		FlagSet fs=flagSets.elementAt(j);
        		if(flag.equals(fs.flag)&&type.equals(fs.type)){
        			int index=usedSymbols.indexOf(fs.symbol);
        			String linkID="";
        			if(index>=0)
        				linkID=getLinkID(index);
        			
        			if(linkID.length()>0)
        				symbols+="\\hyperlink{"+linkID+"}{"+fs.symbol+"}";
        			else
        				symbols+="{"+fs.symbol+"}";
        		}
        	}
    	}
    	    	
    	return symbols;
    }
    
    public String getFlagSymbols(String flags,String type,String linkID){
    	String symbols="";
    	String tempFlags=flags;
    	int n=flags.length();
    	if(n<=0){
    		n=1;
    		tempFlags=" ";
    	}
    	
    	for(int i=0;i<n;i++){
    		String flag=""+tempFlags.charAt(i);
    		flag=flag.trim();
    		
        	for(int j=0;j<flagSets.size();j++){
        		FlagSet fs=flagSets.elementAt(j);
        		if(flag.equals(fs.flag)&&type.equals(fs.type))
        			symbols+="\\hyperlink{"+linkID+"}{"+fs.symbol+"}";
        	}
    	}
    	    	
    	return symbols;
    }
    
    //for footnote without flags, used for column heading
    public String getFlagSymbols(String type){
    	return getFlagSymbols("",type);
    }
    
      
    
    public String getFlagSymbolsLatex(String flags,String type){
    	String symbols=getFlagSymbols(flags,type);
    	if(symbols.length()<=0)
    		return "";
    	
    	return "\\ensuremath{^{"+symbols+"}}";
    }
    
    public String getFlagSymbolsLatex(String flags,String type,String linkID){
    	String symbols=getFlagSymbols(flags,type,linkID);
    	if(symbols.length()<=0)
    		return "";
    	
    	
    	return "\\ensuremath{^{"+symbols+"}}";
    }
    
    public Comment commentAt(int n){ 
    	if(n>=footnoteComments.size())
    		return null;
    				
    	return footnoteComments.elementAt(n);		
    }
    
    public String getLinkID(int n){
    	if(n>=linkIDs.size())
    		return "";
    	
    	return linkIDs.get(n);
    				
    }
    public String symbolAt(int n){
    	if(n>=footnoteComments.size())
    		return "";
    	
    	return usedSymbols.elementAt(n);
    }
    
    //used for footnote, including alignment using makebox
    public String footnoteMarkAt(int n){
    	String symbol=symbolAt(n);
    	if(symbol.length()<=0)
    		return "";
    	
    	String linkID=getLinkID(n);  	
    	if(linkID.length()>0)
    		return "\\makebox[1ex]{\\ensuremath{^{\\hypertarget{"+linkID+"}{"+symbol+"}}}}";
    	else
    		return "\\makebox[1ex]{\\ensuremath{^{"+symbol+"}}}";
    }
    
    //used for footnote, including alignment using makebox
    public String footnoteMarkAt(int n,String linkID){
    	String symbol=symbolAt(n);
    	if(symbol.length()<=0)
    		return "";
    	   	
    	return "\\makebox[1ex]{\\ensuremath{^{\\hypertarget{"+linkID+"}{"+symbol+"}}}}";
    }
    
    public String latexSymbolAt(int n){
    	String symbol=symbolAt(n);
    	if(symbol.length()<=0)
    		return "";
    	
    	return "\\ensuremath{^{"+symbol+"}}";
    }
    
    public int size(){return footnoteComments.size();}
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    //NOTE: all comments in coms should have comment heads
	public void setFootnotes(Vector<Comment> coms){
    	int nexception=0;
   	    	
    	footnoteComments.addAll(coms);
    	
        for(int i=0;i<coms.size();i++){
        	Comment com=coms.elementAt(i);
        	Vector<String> headV=com.headV();
        	Vector<Vector> flagVV=com.flagVV();
        	
        	Vector<String> flagV;
        	String symbol;
        	try{
        		if(com.head().contains("E(^)"))
        			symbol="x";
        		else{
            		symbol=flagSymbols.elementAt(0);
            		flagSymbols.removeElementAt(0);
        		}

        	}catch (Exception e){
        		symbol=""+let.toUpperCase().charAt(nexception);
        		nexception++;
        	}
        	
        	usedSymbols.addElement(symbol);
        	
            for(int j=0;j<flagVV.size();j++){//in comment, flagCV.size=headV.size
                flagV=(Vector<String>)flagVV.elementAt(j);
                String head=headV.elementAt(j);
                if(flagV.size()<=0){
                	flagSets.addElement(new FlagSet("",head,symbol));
                	continue;
                }
                	
                for(int k=0;k<flagV.size();k++){
                	String flag=(String)flagV.elementAt(k);
                	flagSets.addElement(new FlagSet(flag,head,symbol));
                }
            }
        }
        	
    }
    
    public void setFootnotes(Vector<Comment> coms, TableControl tc){
    	filterComments(coms,tc);
    	setFootnotes(coms);
    }
    
    public void findFootnotes(Vector<Comment> coms){

    	Vector<Comment> footnoteComments=new Vector<Comment>();
    	
    	for(int i=0;i<coms.size();i++){
    		Comment com=coms.elementAt(i);
    		if(com.nHeads()==0||com.headAt(0).isEmpty() || com.headAt(0).contains("N"))//comments at top of table
    			continue;
    		
    		footnoteComments.add(com);
    	}
    	
    	setFootnotes(footnoteComments);
    	
    }
        
    
    public void findFootnotes(TableControl tc){
    	Vector<Comment> coms=tc.getComments();
    	Vector<Comment> footnoteComments=new Vector<Comment>();
    	
    	for(int i=0;i<coms.size();i++){
    		Comment com=coms.elementAt(i);
    		
    		//debug
    		//System.out.println(com.body());

    		
    		if(com.nHeads()==0||com.headAt(0).isEmpty() || com.headAt(0).contains("N") || com.body().contains("LABEL="))//comments at top of table
    			continue;

    		footnoteComments.add(com);
    	}
    	
    	setFootnotes(footnoteComments,tc);
    }
    
    public void setLinkID(int i,String linkID){
    	if(i<=linkIDs.size()){
    		linkIDs.remove(i);
    		linkIDs.insertElementAt(linkID, i);
    	}
    }
    
    public void setLinkIDs(String linkID){
    	linkIDs.clear();
    	for(int i=0;i<size();i++){
    		linkIDs.add(linkID+i);
    	}
    }
    
    //filter out comments with no corresponding column data or flag comment with no flagged data
    //or comments with all heads not the standard table heads that have column data
    private void filterComments(Vector<Comment> coms,TableControl tc){
    	Vector<Comment> tempComs=new Vector<Comment>();
    	

		
    	if(coms.size()>0 && ens!=null){
    		Comment c=coms.elementAt(0);
    		if(c.type().contains("L"))
    			flagsInTable=ens.levelFlags();
    		else if(c.type().contains("G"))
    			flagsInTable=ens.gammaFlags();
    		else if(c.type().length()==2) {
    			if(c.type().startsWith("D"))
    				flagsInTable=ens.delayFlags();
    			else if(c.type().equals("MX"))
    				flagsInTable=ens.xrayFlags();
    			else if(c.type().equals("MA"))
    				flagsInTable=ens.augerFlags();
    		}else
    			flagsInTable=ens.decayFlags();
    	}
    	
    	for(int i=0;i<coms.size();i++){
    		Comment c=coms.elementAt(i);
    		
    		//debug
    		//System.out.println("In Footnotes: filterComments1 footnote head="+c.head()+" flags="+c.flags()+" body="+c.body()+" type="+c.type());
    		//debug
    		//System.out.println("            flagsInTable="+flagsInTable+" isGhost:"+isGhostComment(c,tc));                      
    		
    		if(!isGhostComment(c,tc))
    			tempComs.add(c);    			
    	}
    	coms.clear();
    	coms.addAll(tempComs);
    	  	
    }
    
    //filter out comments with no corresponding column data or flag comment with no flagged data
    //or comments with all heads not the standard table heads that have column data
    //NOTE that: there could be comment like, "cG BE2W$....", which is recognized as a ghost
    //comment but should be actually treated as a general comment at top of table 
    @SuppressWarnings("rawtypes")    
	private boolean isGhostComment(Comment c, TableControl tc){
    	boolean print=false;

    	//for(String h:tc.getDefaultColumns())
    	//	System.out.println(" com="+c.body()+" head="+c.head()+" default column="+h);
    	
    	for(int k=0;k<c.headV().size();k++){//filter out comments with no corresponding column data
    		
    		if(tc.getDefaultColumns().contains(c.headAt(k))){//column is in table
    			int n=c.flagVV().elementAt(k).size();
    			if(n==0)//comment has no flag, print it
    				print=true;
    			else{//comment has flags, if flag is also in data line, print it
					Vector ff= c.flagVV().elementAt(k);
    				for(int m=0;m<n;m++){
    					String f=(String)ff.elementAt(m); 
    					
    					//System.out.println(" com="+c.body()+" head="+c.head()+" c.headAt(k)="+c.headAt(k)+" n="+n+" print="+print+" f="+f+"  flagsInTable="+flagsInTable);
    					
    					if(flagsInTable.contains(f))
    						print=true;          					
    				}
    			}
    			
        		
        		
    			if(print) break;
    		}          		
    	}
    	
    	return !print;
    }
    
    
}
    
class FlagSet{
	String flag;
	String type;
	String symbol;
	
	public FlagSet(String f,String t,String s){
		flag=f;
		type=t;
		symbol=s;
	}

}

