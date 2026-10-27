 package ensdfparser.ensdf;

import java.util.*;

import ensdfparser.nds.util.Str;

public class Parent extends Record{

    
  protected String dm_name_s;
  @SuppressWarnings("unused")

  protected Nucleus nuc=new Nucleus();
  protected Level lev=new Level();
  protected String q_s;
  protected String dq_s;
  protected float q_f;
  protected float dq_f;
  protected String ion_s;
  protected String qMev_s;

  protected DecayMode dm=new DecayMode();


  protected String indexS="";//used to identify Norm where there are multiple Norm records 
  //it is the number following "N" or "PN" or "P" records
  //same as N and PN records
  
  public Parent(){
    lines.clear();
    commentV.clear();
  }

  public void setValues(Vector<String> v)throws Exception{
	 
	  lines.clear();
   
	  lines.addAll(v);
   
	  if( lines.size() > 0 ) doWork();
  }

  public void doWork()throws Exception{

	recordLine= (String) lines.elementAt(0);
	
	q_s=( ( (String) lines.elementAt(0) ).substring(64,74) ).trim();
	dq_s=( ( (String) lines.elementAt(0) ).substring(74,76) ).trim();
	    
    String scrap="                                               ";
    String line="";
    Vector<String> vjunk=new Vector<String>(); 
    vjunk.addElement( ( (String) lines.elementAt(0)).substring(0,56)+scrap );
    
    //continuation record for parent level
    String qEntry="";
    for(int k=1;k<lines.size();k++) {
    	line=Str.fixLineLength((String) lines.elementAt(k),80);

    	String contType=line.substring(5,6);
    	
    	qEntry="";
        
    	if( line.substring(6,8).equals(" P") && !line.substring(5,6).equals(" ")) { 

        	String s="";

        	int n1=line.indexOf("QP");
        	
        	if(n1>0) {      		
        		int n2=line.indexOf("$",n1);
        		if(n2>0)
        			s=line.substring(n1,n2+1);
        		else
        			s=line.substring(n1);
        		
        		line=line.replace(s, "");
        		qEntry=s.replace("$", "").trim();
        	}
        	s=line.substring(9).trim();
        	if(!s.isEmpty()) {
        		line=line.substring(0,9)+s;
        		vjunk.add(line);
        	}
         } 
        
    	boolean isSuppressed=true;
    	boolean isNewCRLine=false;
    	boolean isExtension=true;
        if(qEntry.length()>0) {
        	FindValue fv=new FindValue();
        	fv.findValueInEntry(qEntry); 
        	String name=fv.name();
      
            if(fv.canGetV()) {
                ContRecord cr=new ContRecord();
                String crName=name;
                String crText=fv.txt();
                cr.setValues(crName,fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),crText,contType,isSuppressed,isNewCRLine,isExtension);
                crV.add(cr);  
                
                if(q_s.isEmpty()) {
                   	q_s=cr.s();
                    dq_s=cr.ds();
                }
            }           

        }
    }
    
    lev.setValues( vjunk,0 );


    /* q_s="7358"; dq_s="70"; */

    ion_s=( ( (String) lines.elementAt(0) ).substring(76) ).trim();

    parseQV();



/* The comments */

   for( int k=1; k< lines.size(); k++){
       Vector<String> com=new Vector<String>();
       if( ( ((String) lines.elementAt(k)).substring(5,8).equals(" CP") || 
           ((String) lines.elementAt(k)).substring(5,8).equals(" cP") )
           //&& ((String) line.elementAt(k)).substring(9).trim().length() > 0
          ){
          com.addElement(lines.elementAt(k));
           int il=1;
             while ( (k+il) < lines.size()  && 
                     ( ((String) lines.elementAt(k+il)).substring(6,8).equals("CP") || 
                       ((String) lines.elementAt(k+il)).substring(6,8).equals("cP") ) && 
                     !((String) lines.elementAt(k+il)).substring(5,6).equals(" ") 
                    ){
                    com.addElement(lines.elementAt(k+il));
                 il++;
             }
             Comment c=new Comment();
             c.setValues(com);
            commentV.add(c);  
       }   
   }
   
   makeContRecordsVMap();  
   
   lines.clear();
/* End of doWork */

  }
  
  protected void parseQV() {
	    
	  if( q_s.length() > 0 ){
	       
		  SDS2XDX s=new SDS2XDX();
	      s.setValues(q_s,dq_s);
	      q_f=(float) s.X();
	      dq_f=(float) s.DX();
	      if( dq_s.length() == 0 ) dq_f=-1.0f;
	      
	  }else{
	        q_f=-1.0f; dq_f=-1.0f;
	      
	  }	 

	  if(q_f!=-1) {
		  qMev_s=""+ (float) ( Math.round((float) q_f*10) )/(10*1000) ;
		  while( qMev_s.endsWith("0") ){ qMev_s=qMev_s.substring(0,qMev_s.length()-1); }
		  if(  qMev_s.endsWith(".") ) qMev_s=qMev_s.substring(0,qMev_s.length()-1);
	  }

  }
  
  @SuppressWarnings("unused")
protected String rTrim(String s){
     String st=s.trim();
     int i=s.indexOf(st);
     String srt=s.substring(0,i)+st; 
     return srt;
  }

/* -------------------- Public methods -------------------- */

  public void setDMName(String s){dm_name_s=s; dm.setName(s); }
  public void setDMValue(String s){ dm.setValue(s); }
  public void setDMUnc(String s){ dm.setUnc(s); }

  public DecayMode DM(){return dm;}
  public String DMName(){return dm_name_s;}

  public Nucleus nucleus(){return lev.nucleus();}
  public Level level(){return lev;}
  public String QS(){ return q_s;}
  public String DQS(){ return dq_s;}
  public String QUnicode(){
    String qu_s="";
    if(q_s.length() > 0 ){ 
      if( dq_s.equals("GT") ) qu_s=">"+q_s+" keV";
      else if( dq_s.equals("GE") ) qu_s='\u2265'+q_s+" keV";
      else if( dq_s.equals("LT") ) qu_s=">"+q_s+" keV";
      else if( dq_s.equals("LE") ) qu_s='\u2264'+q_s+" keV";
      else if( dq_s.equals("AP") ) qu_s='\u2248'+q_s+" keV";
      else qu_s=q_s+" keV "+dq_s;
    }
    return qu_s;

  }
  public float QF(){ return q_f;}
  public float DQF(){ return dq_f;}
  public void setDQF(float f){dq_f=f;}
  public String ionS(){ return ion_s;}

  public String qMeVS(){ return qMev_s;}
  
  public String getIndexS(){return indexS;}
  public void setIndexS(String s){indexS=s;}

  @Override
  public String ES() {
  	// TODO Auto-generated method stub
  	return "";
  }

  @Override
  public String DES() {
  	// TODO Auto-generated method stub
  	return "";
  }

  @Override
  public float EF() {
  	// TODO Auto-generated method stub
  	return 0;
  }

  @Override
  public float DEF() {
  	// TODO Auto-generated method stub
  	return 0;
  }

  @Override
  public String RIS() {
  	// TODO Auto-generated method stub
  	return "";
  }

  @Override
  public String DRIS() {
  	// TODO Auto-generated method stub
  	return "";
  }

  @Override
  public String flag() {
  	// TODO Auto-generated method stub
  	return "";
  }

  @Override
  public String q() {
  	// TODO Auto-generated method stub
  	return "";
  }

  @Override
  public String coinS() {
  	// TODO Auto-generated method stub
  	return "";
  }

@Override
public void addFlag(String s) {
	// TODO Auto-generated method stub
	
}
}