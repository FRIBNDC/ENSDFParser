 package ensdfparser.ensdf;

import java.util.*;

public class Beta extends Decay{

  @SuppressWarnings("unused")
  protected String level_s;
  protected int level_i;
  protected String flevel_s,flevel_jpis;
  protected int flevel_i;
  
  protected boolean isPlaced;

  protected final String decayType="B";
  
  protected String e_s,e_s0;  protected String de_s,de_s0; protected float e_f; protected float de_f;
  protected String recoilE_s;  protected String drecoilE_s;

  protected String eav_s;  protected String deav_s; protected float eav_f; protected float deav_f;

  protected String epe_s; protected String depe_s; protected float epe_f; protected float depe_f;

  protected String i_s;  protected String di_s; protected double i_d; protected double di_d;

  protected double i_sy_d; protected double di_sy_d; 

  protected String ai_s, dai_s; protected double ai_d, dai_d, daill_d; /* daill_d= a lower limit for the absolute intensity uncertainty */

  protected String dose_s, ddose_s; protected float dose_f, ddose_f;

  protected String logft_s; protected String dlogft_s;

  protected String flag_s; 

  protected String coin_s; 

  protected String u_s; 

  protected String q_s;

  protected int order; 

  protected String dpType="";//for the type of subsequent particle decay if available, like 1n, 2n, 3n
  
  /* Comments */



  /* the neutrino energies */

  protected String enu_s, denu_s;

  protected float enu_f, denu_f;

  public Beta(){

    lines.clear();
    commentV.clear();

    crLineV.clear();
    crV.clear();
    
    e_s=""; de_s=""; e_f=(float)-1.0; de_f=(float)-1.0;
    e_s0=""; de_s0=""; 
    
    eav_s=""; deav_s=""; eav_f=(float)-1.0; deav_f=(float)-1.0;

    epe_s=""; depe_s=""; epe_f=(float)-1.0; depe_f=(float)-1.0;

    i_s=""; di_s=""; i_d=-1.0; di_d=-1.0;

    i_sy_d=-1.0; di_sy_d=-1.0;

    ai_s=""; dai_s=""; ai_d=-1.0; dai_d=-1.0; daill_d=-1.0;

    logft_s=""; dlogft_s="";

    flag_s=""; coin_s="";

    u_s="";

    q_s="";

    dose_s=""; ddose_s=""; dose_f=(float) -1.0;  ddose_f=(float) -1.0;
   
    dpType="";
    
    order=-1;
    isPlaced=true;

  }

  public void setValues(Vector<String> v) throws Exception{
    lines.clear();
   lines.addAll(v);
   if(lines.size() > 0 ) doWork();
  }

  protected void parseEB() {

  SDS2XDX e=new SDS2XDX();
  e.setValues(e_s,de_s);
  e_f=(float) e.X();
  de_f=(float) e.DX();	  
  }
  
  protected void parseIB() {
 
	    if( i_s.length() > 0 ){
	      SDS2XDX i=new SDS2XDX();
	      i.setValues(i_s,di_s);
	      i_d=i.X();
	      di_d=i.DX();
	      i_sy_d=i.XS();
	      di_sy_d=i.DXS();
	    }else{
	      i_d=-1.0;
	      di_d=-1.0;
	      i_sy_d=-1.0;
	      di_sy_d=-1.0;
	    }	  
  }
private void doWork() throws Exception{
    try {
        recordLine=(String) lines.elementAt(0);   

        String nucName=( (String) lines.elementAt(0) ).substring(0,5).trim();
        nuc=new Nucleus();
        nuc.setGen(nucName);
        
        e_s=( (String) lines.elementAt(0) ).substring(9,19).trim();
        de_s=( (String) lines.elementAt(0) ).substring(19,21).trim();
        i_s=( (String) lines.elementAt(0) ).substring(21,29).trim();
        di_s=( (String) lines.elementAt(0) ).substring(29,31).trim(); 
        
        e_s0=e_s;
        de_s0=de_s;
        
        logft_s=( (String) lines.elementAt(0) ).substring(41,49).trim();
        dlogft_s=( (String) lines.elementAt(0) ).substring(49,55).trim();
        

        flag_s=( (String) lines.elementAt(0) ).substring(76,77).trim();
        if( flag_s.equals("C") || flag_s.equals("?") ){
          coin_s=flag_s;
          flag_s="";
        }
        u_s=( (String) lines.elementAt(0) ).substring(77,79).trim();
        q_s=( (String) lines.elementAt(0) ).substring(79).trim();
    }catch(Exception e) {
    	
    }

    
    parseEB();
    parseIB();


/* The Comments: */

    parseComments("B");
    
    
/*
    poscV.clear();

    for( int k=0; k < line.size(); k++){

       if( ( ((String) line.elementAt(k)).substring(5,9).equals(" CB ") || 
           ((String) line.elementAt(k)).substring(5,9).equals(" cB ") ) &&
           ( (String) line.elementAt(k) ).substring(9).trim().length() > 0
          ){
             poscV.add( new Integer(k) ); 
           }
    }

    for(int k=0; k< poscV.size(); k++){
      int ki= ((Integer) poscV.elementAt(k)).intValue(); 
      int kf=line.size()-1; 
      if( k < ( poscV.size()-1 ) ) kf=((Integer) poscV.elementAt(k+1)).intValue() -1;
      Vector<String> cline=new Vector<String>();
      for( int j=ki; j <= kf; j++){ 
        if(  ( (String) line.elementAt(j) ).substring(6,8).toUpperCase().equals("CB") ){
          cline.addElement( (String) line.elementAt(j)); 
          }
      }
     Comment c=new Comment();
     c.setValues(cline);
     
     //debug
     //System.out.println("In Beta: comment="+cline.elementAt(0)+" c.head="+c.head()+" c.body="+c.body());
     
     commentV.addElement(c);
    }
*/
    
/* The continuation records: */
   parseContinuationRecords();

   lines.clear();
/* End of dowork: */

  }

  protected void parseContinuationRecords() {
	   boolean isNewCRLine=false;
	   
	   boolean isExtension=false;
	   
	   Vector<String> entries;
	   ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("E","IB","LOGFT"));
	   String line="";
	   for( int k=0; k< lines.size(); k++){

		   line=lines.elementAt(k);
		   
	       if( line.substring(6,9).equals(" B ") && !line.substring(5,6).equals(" ")) { 

	    	   isNewCRLine=true;
	    	   
	           FindValue fv=new FindValue();

	           crLineV.addElement(line);
	           
	           entries=findContRecordEntries(line);//find entries of all continuation records in current line including decay modes
	           
	           //find out if this beta is suppressed or not
	           boolean isSuppressed=line.substring(5,6).equals("S");

	           boolean skip=false;
	           
	           String contType=line.substring(5,6);
	           
	           for(int m=0;m<entries.size();m++){
	           	String entry=entries.get(m);
	           	
	           	fv.clear();
	           	fv.findValueInEntry(entry); 
	           	String name=fv.name();
	         
	               if(!fv.canGetV())
	               	continue;
	               
	               ContRecord cr=new ContRecord();
	               String crName=name;
	               String crText=fv.txt();
	               
	               
	               skip=false;
	               
	               isExtension=false;
	               
	               boolean isSupTemp=isSuppressed;
	               if(recordNames.contains(crName)) {
	                   isSupTemp=false;
	                   isExtension=true;
	                   skip=true;
	               }

	               //cr.setValues(crName,fv.symbol(),fv.s(),"",fv.ds(),"",crText,isSuppressed,isNewline);  
	               cr.setValues(crName,fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),crText,contType,isSupTemp,isNewCRLine,isExtension);
	               crV.add(cr);

	               if(name.equals("FLAG")){
	               	flag_s=flag_s+fv.v(); 
	               	crV.remove(cr);//not continuation record data with format of (s ds)
	                   skip=true;
	               }else if(name.equals("EAV")){     
	                   eav_s=fv.s(); deav_s=fv.ds(); eav_f=fv.xf(); deav_f=fv.dxf();
	                   isNewCRLine=false;                      
	               }else if(name.equals("E") && de_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	            	   e_s=cr.s();
	            	   de_s=cr.ds();  
	            	   e_s0=e_s;
	            	   de_s0=de_s;
	            	   
	            	   parseEB();
	               }else if(name.equals("IB") && di_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	            	   i_s=cr.s();
	            	   di_s=cr.ds();        	
	            	   parseIB();
	               }else if( name.equals("LOGFT") && dlogft_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                  	logft_s=cr.s();
	                   	dlogft_s=cr.ds();
	               }
	               
	              	if(!skip)//the first entry in current line has been processed (not skipped), and the rest are in the same line instead of a new line
	               		isNewCRLine=false;     
	           }

	       }
	  
	   }
	   
	   makeContRecordsVMap(); 
	   
	  /*   if( eav_s.indexOf("416.") >= 0 ){ eav_s="273.0"; deav_s="1"; eav_f=273.0f; deav_f=0.1f;}  */
  }
  @SuppressWarnings("unused")
  protected String rTrim(String s){
     String st=s.trim();
     int i=s.indexOf(st);
     String srt=s.substring(0,i)+st; 
     return srt;
  }

/*  getter methods */

  public String ES0()
  {       
      return e_s0.toLowerCase().replace("e", "E").replace("s(", "S(");
  }
  
  public String DES0(){ return de_s0;}
  
  public String ES()
  {       
      return e_s.toLowerCase().replace("e", "E").replace("s(", "S(");
  }
  
  public String DES(){ return de_s;}
  public float EF(){ return e_f;}
  public float DEF(){ return de_f;}

  public String EAvS(){ return eav_s;}
  public String DEAvS(){ return deav_s;}
  public float EAvF(){ return eav_f;}
  public float DEAvF(){ return deav_f;}
  public void setEAvF(float f){eav_f=f;}
  public void setDEAvF(float f){deav_f=f;}

  public void setES(String ES){e_s=ES;}
  public void setDES(String DES){de_s=DES;}
  public void setEF(float ef){e_f=ef;}
  public void setDEF(float def){de_f=def;}
  
  public void setEPES(String s){ epe_s=s;}
  public void setDEPES(String s){ depe_s=s;}
  public String EPES(){ return epe_s;}
  public String DEPES(){ return depe_s;}

  public void setEPEF(float f){ epe_f=f;}
  public void setDEPEF(float f){ depe_f=f;}
  public float EPEF(){ return epe_f;}
  public float DEPEF(){ return depe_f;}

  public String RIS(){ return i_s;}
  public String DRIS(){ return di_s;}
  public double ID(){ return i_d;}
  public double DID(){ return di_d;}
  public double syID(){ return i_sy_d;}
  public double DsyID(){ return di_sy_d;}
  public void setsyID(double d){i_sy_d=d;}
  public void setDsyID(double d){di_sy_d=d;}

  public void setAIS(String s){ ai_s=s;}
  public void setDAIS(String s){ dai_s=s;}
  public String AIS(){ return ai_s;}
  public String DAIS(){ return dai_s;}

  public String AIUnicode(){
    String aiu_s="";
    if(ai_s.length() > 0 ){ 
      if( di_s.equals("GT") ) aiu_s=aiu_s+">"+ai_s;
      else if( di_s.equals("GE") ) aiu_s=aiu_s+'\u2265'+ai_s;
      else if( di_s.equals("LT") ) aiu_s=aiu_s+">"+ai_s;
      else if( di_s.equals("LE") ) aiu_s=aiu_s+'\u2264'+ai_s;
      else if( di_s.equals("AP") ) aiu_s=aiu_s+'\u2248'+ai_s;
      else aiu_s=ai_s;
//      aiu_s=aiu_s+" % ";
    }
    return aiu_s;
  }

  public void setAID(double d){ ai_d=d;}
  public void setDAID(double d){ dai_d=d;}
  public double AID(){ return ai_d;}
  public double DAID(){ return dai_d;}

  /* a lower limit for the absolute intensity uncertainty */

  public void setDAILLD(double d){ daill_d=d;}
  public double DAILLD(){ return daill_d;} 

  public void setDoseS(String s){ dose_s=s;}
  public void setDDoseS(String s){ ddose_s=s;}
  public String DoseS(){ return dose_s;}
  public String DDoseS(){ return ddose_s;}

  public void setDoseF(float f){ dose_f=f;}
  public void setDDoseF(float f){ ddose_f=f;}
  public float DoseF(){ return dose_f;}
  public float DDoseF(){ return ddose_f;}

  public String LOGFTS(){ return logft_s;}
  public String DLOGFTS(){ return dlogft_s;}

  public String LogftUnicode(){ 
    String logftu_s="";
    if(logft_s.length() > 0 ){ 
      if( dlogft_s.equals("GT") ) logftu_s=">"+logft_s;
      else if( dlogft_s.equals("GE") ) logftu_s='\u2265'+logft_s;
      else if( dlogft_s.equals("LT") ) logftu_s=">"+logft_s;
      else if( dlogft_s.equals("LE") ) logftu_s='\u2264'+logft_s;
      else if( dlogft_s.equals("AP") ) logftu_s='\u2248'+logft_s;
      else logftu_s=logft_s;
      logftu_s=logftu_s+" ";
    }
    return logftu_s;
  }

  public String flagS(){ return flag_s;}
  public void addFlag(String s){flag_s+=s;}
  public String flag(){ return flag_s;}
  public String coinS(){ return coin_s;}
  public String QS(){ return q_s;}
  public String q(){ return q_s;}
  public String US(){ return u_s;}
  public String unique(){ return u_s;}

  public String recoilES(){return recoilE_s;}
  public String recoilDES(){return drecoilE_s;}

  
  public void setOrder(int i){order=i;}
  public int getOrder(){return order;}

  public void setUnplaced(){ isPlaced=false;}
  public boolean isPlaced(){return isPlaced;}

  public void setENeutrino(String s, String ds, float f, float df){ enu_s=s;denu_s=ds;enu_f=f;denu_f=df;}
  
  public String ENuS(){ return enu_s;}
  public String DENuS(){ return denu_s;}

  public float ENuF(){ return enu_f;}
  public float DENuF(){ return denu_f;}
  public void isBlank(){ e_s="new Beta";}
  
  public void setFLI(int FLI){flevel_i=FLI;}
  public void setFLS(String FLS){flevel_s=FLS;}
  public void setJFS(String JFS) {flevel_jpis=JFS;}
  public int getFLI(){return flevel_i;}
  public String getFLS(){return flevel_s;}
  public String getJFS() {return flevel_jpis;}
  
  public int getLevelIndex(){return level_i;}
  
  public void setRecoilES(String ers){recoilE_s=ers;}
  public void setRecoilDES(String ders){drecoilE_s=ders;}
  
  //IES and ITS for ECBP, HF for ALPHA
  public String IES(){return "";}
  public String DIES(){return "";}
  public String ITS(){return "";}
  public String DITS(){return "";}
  public String HFS(){return "";}
  public String DHFS(){return "";}

  public String decayType(){return decayType;}
  public String dpType() {return dpType;}
  
  public void setDPType(String type) {dpType=type;}
  
  @Override
  public void setEAvS(String s) {
      eav_s=s;
  }

  @Override
  public void setDEAvS(String s) {
      deav_s=s;
  }


@Override
public void setRecoilDoseS(String s) {
    // TODO Auto-generated method stub
    
}

@Override
public void setRecoilDDoseS(String s) {
    // TODO Auto-generated method stub
    
}

@Override
public String recoilDoseS() {
    // TODO Auto-generated method stub
    return "";
}

@Override
public String recoilDDoseS() {
    // TODO Auto-generated method stub
    return "";
}

@Override
public void setRecoilDoseF(float f) {
    // TODO Auto-generated method stub
    
}

@Override
public void setRecoilDDoseF(float f) {
    // TODO Auto-generated method stub
    
}

@Override
public float recoilDoseF() {
    // TODO Auto-generated method stub
    return 0;
}

@Override
public float recoilDDoseF() {
    // TODO Auto-generated method stub
    return 0;
}

}