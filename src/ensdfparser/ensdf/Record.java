package ensdfparser.ensdf;

import java.util.HashMap;
import java.util.Vector;

import ensdfparser.nds.ensdf.EnsdfUtil;

public abstract class Record {

	//an reference to the ENSDF object that contains the record
	protected ENSDF parentENSDF=null;
	
	protected Nucleus nuc=new Nucleus();
	protected String dsid="";  
	
	protected boolean silent=false;
	
	protected String dataSourceID=""; //DSID that the current level belongs to
	
	//for store alternative RIS, for case like RI=100 without unc, 
	//its unc should be set as 0 if it is the reference RI in a decay dataset 
	protected String altDRIS100="";
	
	protected Vector<String> lines=new Vector<String>();//Note that line is cleared to be empty at the end of setValues to save ram
	protected String recordLine="";
	protected int id=-1;
	protected Vector<String> crLineV=new Vector<String>();
	protected Vector<ContRecord> crV=new Vector<ContRecord>();
	
    protected HashMap<String,Vector<ContRecord>> crMap=new HashMap<String,Vector<ContRecord>>();
	   
	protected Vector<Integer> poscV=new Vector<Integer>();
	protected Vector<Comment> commentV=new Vector<Comment>();
	protected Vector<Comment> documentV=new Vector<Comment>();
	
	protected boolean isOffsetRange=false;
	protected boolean isOffsetLimit=false;
	
	protected boolean isGotoAdopted=false;//indicate if the record will be included in Adopted dataset, for level and gamma records only
	
	protected boolean isMultipletMember=false;
	protected int indexInMultiplet=-1;
	
	abstract public String ES();
	abstract public String DES();
	abstract public float EF();
	abstract public float DEF();

	
    abstract public String RIS();
    abstract public String DRIS();   
    abstract public String flag();

    public String altDRIS100() {return altDRIS100;}
    public void setAltDRIS100(String dris) {altDRIS100=dris;}
    
    abstract public String q();
    abstract public String coinS();
    
    abstract public void addFlag(String s);
    
    
    //////////////////////////////////////////////////////////////////////
    //for level 1234.5+X
    // ERF() return 1234.5
    // EF()   return estimated energy based on levels before and after current level
    //        so all return values are in order of energies, but differences between 
    //        levels of such formats are not the difference of ERF()
    // ERPF() return values of 1234.5+(offset for X), the differences between levels
    //        of the same format are the same as differences of ERF(), but
    //        all return values of all levels could not be in order
	public float ERPF() {return EF();}
	public float DERPF() {return DEF();}

	public float ERF() {return EF();}
	public float DERF() {return DEF();}
	
	public boolean isTrueERPF() {return false;}//overridden and =true only set in Level class
	public boolean isLevel() {return false;}
	public boolean isGamma() {return false;}
	
	public boolean isOffsetRange() {return isOffsetRange;}
	public boolean isOffsetLimit() {return isOffsetLimit;}
	
	public boolean isMultipletMember() {return isMultipletMember;}
	public int indexInMultiplet() {return indexInMultiplet;}

	public void setIsOffsetRange(boolean b) {isOffsetRange=b;}
	public void setIsOffsetLimit(boolean b) {isOffsetLimit=b;}
	
    public int nComments(){ return commentV.size(); }
    public Comment commentAt(int k){ return (Comment) commentV.elementAt(k); }
    public Vector<Comment> commentV(){ return commentV; }
    
    public int nDocuments(){ return documentV.size(); }
    public Comment documentAt(int k){ return (Comment) documentV.elementAt(k); }
    public Vector<Comment> documentV(){ return documentV; }
    
    public int nContRecords(){return crV.size();}
    
    public ContRecord contRecordAt(int k){
    	return (ContRecord)crV.get(k);  
    	//return contRecsV().get(k);
    }

    public Vector<ContRecord> contRecsV(){
    	
    	return crV;  	
    }
    
    public void makeContRecordsVMap() {
        crMap.clear();
        for(ContRecord cr:crV) {
            String name=cr.name();
            Vector<ContRecord> temp=crMap.get(name);
            if(temp==null) {
                temp=new Vector<ContRecord>();
                crMap.put(name,temp);
            }
            
            temp.add(cr);
        }
    }
    
	public HashMap<String,Vector<ContRecord>> contRecordsVMap(){return crMap;}
	//abstract public LinkedHashMap<String,ContRecord> contRecordMap();
	
    public int nContRecordLines(){return crLineV.size();}
    public String contRecordLineAt(int k){return (String) crLineV.elementAt(k);}
    public Vector<String> contRecsLineV(){return crLineV;}
        

	
	
    /* find names of all continuation records in an ENSDF line**/
    protected Vector<String> findContRecordNames(String line){
    	return EnsdfUtil.findContRecordNames(line);
    }

   
    /* find name of a continuation record entry**/
    protected String findContRecordName(String entry){
    	return EnsdfUtil.findContRecordName(entry);
    }
    
    /* find entries of all continuation records in an ENSDF line**/
    protected Vector<String> findContRecordEntries(String line){
    	return EnsdfUtil.findContRecordEntries(line);
    }
    
    //make a ContRecord object from the string of a continuation record entry 
    protected ContRecord makeContRecord(String entry,String contType,boolean isSuppressed, boolean isNewLine){
    	return EnsdfUtil.makeContRecord(entry, contType, isSuppressed, isNewLine);
    }
    
    
    /* find all continuation records in an ENSDF line**/
    protected Vector<ContRecord> findContRecords(String line){
    	return EnsdfUtil.findContRecords(line);
    }
    
    protected void parseComments(String typeS) throws Exception{
    	
    	if(typeS.trim().length()==0)
    		return;
    	
    	char type=typeS.trim().charAt(0);
    	
        poscV.clear();

        String lineType="",ptype="";
        int typeEndIndex=8;
        if(type=='D' && lines.size()>0){
        	String particle=lines.elementAt(0).substring(8,9).trim();
        	String delayedS=lines.elementAt(0).substring(7,8).trim();
        	if(delayedS.isEmpty() && particle.length()>0)//for particle decay
        		type=' ';
        	
        	ptype=particle;
        	typeEndIndex=9;
        }

        for( int k=0; k < lines.size(); k++){
           
           lineType=lines.elementAt(k).substring(5,typeEndIndex);
           
           //System.out.println("In AbstractRecrod: line 76: lineType="+lineType);
           
           if( ( lineType.toUpperCase().equals(" C"+type+ptype) || lineType.toUpperCase().equals(" T"+type+ptype) )
           //   && ( (String) line.elementAt(k) ).substring(9).trim().length() > 0  //removed to allow to print a blank line, 11/8/2017
           )
           {          
        	   poscV.add( new Integer(k) );    
           }
        }

        for(int k=0; k< poscV.size(); k++){
          int ki= ((Integer) poscV.elementAt(k)).intValue(); 
          int kf=lines.size()-1; 
          if( k < ( poscV.size()-1 ) ) kf=((Integer) poscV.elementAt(k+1)).intValue() -1;
          Vector<String> cline=new Vector<String>();
          for( int j=ki; j <= kf; j++){ 
        	lineType=lines.elementAt(j).substring(6,typeEndIndex);
        	
            if(lineType.toUpperCase().equals("C"+type+ptype) ){
               cline.addElement( (String) lines.elementAt(j)); 
            }else if(lineType.toUpperCase().equals("T"+type+ptype) ) {
               String temp=lines.elementAt(j);
               if(lineType.charAt(0)=='T')
            	   temp=temp.substring(0,6)+"C"+temp.substring(7);
               else
               	   temp=temp.substring(0,6)+"c"+temp.substring(7);
 
               cline.addElement(temp);
            }
          }
         
          Comment c=new Comment();       
          c.setValues(cline);         
          commentV.addElement(c);
          
          //check if the "E" comment says this record will be not included in adopted
          if("LG".contains(type+"")) {
        	  String s=c.rawBody().toUpperCase();
        	  if(s.contains("NOT") && s.contains("ADOPTED")) {
        		  s=s.replace(" THE ", " ").replace(" BE ", " ").replace("  ", " ").trim();
        		  String[] a= {"INCLUDED","USED","ADOPTED"};

        		  for(int i=0;i<a.length;i++) {
        			  if(s.contains("NOT "+a[i]+" IN ADOPTED")) {
        				  isGotoAdopted=false;
        				  break;
        			  }
        		  }
        	  }
          }
        }
        
        parseDocuments(typeS);
    }
    
    private void parseDocuments(String typeS) throws Exception{
    	
    	if(typeS.trim().length()==0)
    		return;
    	
    	char type=typeS.trim().charAt(0);
    	
        Vector<Integer> posdV=new Vector<Integer>();

        String lineType="",ptype="";
        int typeEndIndex=8;
        if(type=='D' && lines.size()>0){
        	String particle=lines.elementAt(0).substring(8,9).trim();
        	String delayedS=lines.elementAt(0).substring(7,8).trim();
        	if(delayedS.isEmpty() && particle.length()>0)//for particle decay
        		type=' ';
        	
        	ptype=particle;
        	typeEndIndex=9;
        }

        for( int k=0; k < lines.size(); k++){
           
           lineType=lines.elementAt(k).substring(5,typeEndIndex);
           
           //System.out.println("In AbstractRecrod: line 76: lineType="+lineType);
           if( ( lineType.toUpperCase().equals(" D"+type+ptype) )
           //if( ( lineType.toUpperCase().equals(" D"+type+ptype) || lineType.toUpperCase().equals(" T"+type+ptype) )
           //   && ( (String) line.elementAt(k) ).substring(9).trim().length() > 0  //removed to allow to print a blank line, 11/8/2017
           )
           {          
        	   posdV.add( new Integer(k) );    
           }
        }

        for(int k=0; k< posdV.size(); k++){
          int ki= ((Integer) posdV.elementAt(k)).intValue(); 
          int kf=lines.size()-1; 
          if( k < ( posdV.size()-1 ) ) kf=((Integer) posdV.elementAt(k+1)).intValue() -1;
          Vector<String> cline=new Vector<String>();
          for( int j=ki; j <= kf; j++){ 
        	lineType=lines.elementAt(j).substring(6,typeEndIndex);
        	
            if(lineType.toUpperCase().equals("D"+type+ptype) ){
               cline.addElement( (String) lines.elementAt(j));   
            }
            /*
            else if(lineType.toUpperCase().equals("T"+type+ptype) ) {
               String temp=lines.elementAt(j);
               if(lineType.charAt(0)=='T')
            	   temp=temp.substring(0,6)+"D"+temp.substring(7);
               else
               	   temp=temp.substring(0,6)+"d"+temp.substring(7);
 
               cline.addElement(temp);
            }
            */
          }
         
          Comment c=new Comment();       
          c.setValues(cline);   
          
          //if(c.type().trim().equals("G") && c.body().toUpperCase().contains("ICE("))
          //System.out.println("Record 288: "+c.body());
          
          documentV.addElement(c);
        }
    }
    
	public String recordLine(){return recordLine;}
	public Vector<String> lines() {return lines;}
	
	public int id() {return id;}
	public void setID(int i) {id=i;}
	
	public String DSID() {return dsid;}
	
	public boolean isGotoAdopted() {return isGotoAdopted;}//for level and gamma only
	public void setIsGotoAdopted(boolean b) {isGotoAdopted=b;}
	
	public void setSilent(boolean b) {silent=b;}
	
	  
	// Add these methods to your Level class
	  
	public String getDataSourceID() { return dataSourceID; }  
	public void setDataSourceID(String dataSourceID) { this.dataSourceID = dataSourceID;} 
	
    public void setParentENSDF(ENSDF parent) { this.parentENSDF = parent; }
    public ENSDF getParentENSDF() { return parentENSDF; }
}
