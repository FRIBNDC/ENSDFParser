/* note, continuation records are not done!!! */

package ensdfparser.ensdf;

import java.util.*;

public class Alpha extends Decay{

  @SuppressWarnings("unused")
  protected String level_s;
  @SuppressWarnings("unused")
  protected int level_i;
  protected boolean isPlaced;

  protected String flevel_s,flevel_jpis;
  protected int flevel_i;
  
  protected final String decayType="A";
 
  protected String e_s,e_s0;  protected String de_s,de_s0; protected float e_f; protected float de_f; 

  protected String recoilE_s; protected String drecoilE_s; protected float recoilE_f; protected float drecoilE_f;

  protected String i_s;  protected String di_s; protected double i_d; protected double di_d; 

  protected double i_sy_d; protected double di_sy_d;//symmetrized intensity

  protected String ai_s; protected String dai_s; protected double ai_d; protected double dai_d, daill_d; /* daill_d= a lower limit for the absolute intensity uncertainty */  

  protected String aiu_s; 
  protected String daiu_s;

  protected String dose_s, ddose_s; protected float dose_f, ddose_f;

  protected String recoil_dose_s, recoil_ddose_s; protected float recoil_dose_f, recoil_ddose_f;

  protected String hf_s; protected String dhf_s;  

  protected String flag_s; 

  protected String coin_s;

  protected String q_s;

  protected int order; 

  public Alpha(){

    lines.clear();
    commentV.clear();
    
    crLineV.clear();
    crV.clear();
    
    isPlaced=true;

    e_s=""; de_s=""; e_f=(float)-1.0; de_f=(float)-1.0; recoilE_s=""; drecoilE_s=""; recoilE_f=(float)-1.0; drecoilE_f=(float)-1.0;
    e_s0=""; de_s0="";
    i_s=""; di_s=""; i_d=-1.0; di_d=-1.0; 

    i_sy_d=-1.0; di_sy_d=-1.0;

    ai_s=""; dai_s=""; ai_d=-1.0; dai_d=-1.0; daill_d=-1.0; aiu_s=""; daiu_s="";

    hf_s=""; dhf_s="";

    flag_s=""; coin_s="";

    q_s="";

    dose_s=""; ddose_s=""; dose_f=(float) -1.0;  ddose_f=(float) -1.0;

    recoil_dose_s=""; recoil_ddose_s=""; recoil_dose_f=(float) -1.0;  recoil_ddose_f=(float) -1.0;

    order=-1; 

  }

  public void setValues(Vector<String> v) throws Exception{
   lines.clear();
   lines.addAll(v);
   if(lines.size() > 0 ) doWork();
  }

  protected void parseIA() {
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
  
  protected void parseEA() {
      
	  /* The energy of the recoiling nucleus */
  
	    SDS2XDX e=new SDS2XDX();
	    e.setValues(e_s,de_s);
	    e_f=(float) e.X();
	    de_f=(float) e.DX();



	    /* old, incorrect
	    recoilE_f=e_f*4/(4+nuc.a() );
	    drecoilE_f=de_f*4/(4+nuc.a() );
	    */
	    
	    //more precise values using masses will be set externally
	    recoilE_f=e_f*4/nuc.a();
	    drecoilE_f=de_f*4/nuc.a();

	    
	    XDX2SDS xr=new XDX2SDS(); 
	    xr.setValues(recoilE_f,drecoilE_f);
	    recoilE_s=xr.S(); drecoilE_s=xr.DS();	  
  }
  
private void doWork() throws Exception{

	try {
		   /* The alpha energy */
	    recordLine=(String) lines.elementAt(0);   


		  String nucName=( (String) lines.elementAt(0) ).substring(0,5).trim();
		  nuc=new Nucleus();
		  nuc.setGen(nucName);
		  
	    e_s=( (String) lines.elementAt(0) ).substring(9,19).trim();
	    de_s=( (String) lines.elementAt(0) ).substring(19,21).trim();

	    e_s0=e_s;
	    de_s0=de_s;
	    
	    /* The alpha intensity */

	    i_s=( (String) lines.elementAt(0) ).substring(21,29).trim();
	    di_s=( (String) lines.elementAt(0) ).substring(29,31).trim(); 
	}catch(Exception e) {
	}
 
 
    parseEA();
    parseIA();
    
    
//    System.out.println("Alpha: "+e_s+" "+i_d+" "+di_d);

    hf_s=( (String) lines.elementAt(0) ).substring(31,39).trim();
    dhf_s=( (String) lines.elementAt(0) ).substring(39,41).trim();

    flag_s=( (String) lines.elementAt(0) ).substring(76,77).trim();
    if( flag_s.equals("C") || flag_s.equals("?") ){
      coin_s=flag_s;
      flag_s="";
    }
    q_s=( (String) lines.elementAt(0) ).substring(79).trim();

/* The Comments: */
    
    parseComments("A");
    
/*
    poscV.clear();

    for( int k=0; k < line.size(); k++){

       if( ( ((String) line.elementAt(k)).substring(5,9).equals(" CA ") || 
           ((String) line.elementAt(k)).substring(5,9).equals(" cA ") ) &&
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
        if(  ( (String) line.elementAt(j) ).substring(6,8).toUpperCase().equals("CA") ){
          cline.addElement( (String) line.elementAt(j)); 
          }
      }
     Comment c=new Comment();
     c.setValues(cline);
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
	    ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("E","IA","HF"));
	    String line="";
	    
	   for( int k=0; k< lines.size(); k++){
	       line=lines.elementAt(k);

	       if( line.substring(6,9).equals(" A ") && !line.substring(5,6).equals(" ")) { 

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
	            	
	            	//System.out.println(" line="+line+" entry="+entry+" fv.s()="+fv.s()+"  "+fv.ds()+" dsu="+fv.dsu()+" dxu="+fv.dxu());
	            	
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
	                }else if(name.equals("E") && de_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty() ) {                
	             	   e_s=cr.s();
	             	   de_s=cr.ds();  
	             	   
	             	   e_s0=e_s;
	             	   de_s0=de_s;
	             	   
	             	   parseEA();
	                }else if(name.equals("IA") && di_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	             	   i_s=cr.s();
	             	   di_s=cr.ds();        	
	             	   parseIA();
	                }else if( name.equals("HF") && dhf_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                   	hf_s=cr.s();
	                    dhf_s=cr.ds();
	                }
	                
	            	if(!skip)//the first entry in current line has been processed (not skipped), and the rest are in the same line instead of a new line
	            		isNewCRLine=false;     
	            }  
	         }
	   }
	   makeContRecordsVMap(); 
  }

  protected String rTrim(String s){
     String st=s.trim();
     int i=s.indexOf(st);
     String srt=s.substring(0,i)+st; 
     return srt;
  }

/*  public methods */
  
  //original ES 
  public String ES0(){ return e_s0;}
  public String DES0(){ return de_s0;}
  
  public String ES(){ return e_s;}
  public String DES(){ return de_s;}
  public float EF(){ return e_f;}
  public float DEF(){ return de_f;}
  
  public String recoilES(){ return recoilE_s;}
  public String recoilDES(){ return drecoilE_s;}
  public float recoilEF(){ return recoilE_f;}
  public float recoilDEF(){ return drecoilE_f;}

  public String RIS(){ return i_s;}
  public String DRIS(){ return di_s;}
  public double ID(){ return i_d;}
  public double DID(){ return di_d;}
  public double syID(){ return i_sy_d;}
  public double DsyID(){ return di_sy_d;}
  
  public void setID(double ri){ i_d=ri;}
  public void setDID(double dri){ di_d=dri;}
  
  public void setES(String ES){e_s=ES;}
  public void setDES(String DES){de_s=DES;}
  public void setEF(float ef){e_f=ef;}
  public void setDEF(float def){de_f=def;}
  
  public void setAIS(String s){ ai_s=s;}
  public void setDAIS(String s){ dai_s=s;}
  public String AIS(){ return ai_s;}
  public String DAIS(){ return dai_s;}

  public String AIUnicodeS(){ 
    aiu_s="";
    if(ai_s.length() > 0 ){ 
      if( di_s.equals("GT") ) aiu_s=aiu_s+">"+ai_s;
      else if( di_s.equals("GE") ) aiu_s=aiu_s+'\u2265'+ai_s;
      else if( di_s.equals("LT") ) aiu_s=aiu_s+">"+ai_s;
      else if( di_s.equals("LE") ) aiu_s=aiu_s+'\u2264'+ai_s;
      else if( di_s.equals("AP") ) aiu_s=aiu_s+'\u2248'+ai_s;
      else aiu_s=ai_s;
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

  public void setRecoilDoseS(String s){ recoil_dose_s=s;}
  public void setRecoilDDoseS(String s){ recoil_ddose_s=s;}
  public String recoilDoseS(){ return recoil_dose_s;}
  public String recoilDDoseS(){ return recoil_ddose_s;}

  public void setRecoilDoseF(float f){ recoil_dose_f=f;}
  public void setRecoilDDoseF(float f){ recoil_ddose_f=f;}
  public float recoilDoseF(){ return recoil_dose_f;}
  public float recoilDDoseF(){ return recoil_ddose_f;}

  public String HFS(){ return hf_s;}
  public String DHFS(){ return dhf_s;}

  public String HFUnicode(){ 
    String hfu_s="";
    if(hf_s.length() > 0 ){ 
      if( dhf_s.equals("GT") ) hfu_s=">"+hf_s;
      else if( dhf_s.equals("GE") ) hfu_s='\u2265'+hf_s;
      else if( dhf_s.equals("LT") ) hfu_s=">"+hf_s;
      else if( dhf_s.equals("LE") ) hfu_s='\u2264'+hf_s;
      else if( dhf_s.equals("AP") ) hfu_s='\u2248'+hf_s;
      else hfu_s=hf_s;
    }
    return hfu_s;
  }

  public String flagS(){ return flag_s;}
  public void addFlag(String s){flag_s+=s;}
  public String coinS(){ return coin_s;}
  public String QS(){ return q_s;}
  public String flag(){ return flag_s;}
  public String q(){ return q_s;}
  public String US(){return "";}//dummy 
  public String unique(){return "";}//dummy

  
  public void setUnplaced(){ isPlaced=false;}
  public boolean isPlaced(){return isPlaced;}

  public void setOrder(int i){order=i;}
  public int getOrder(){return order;}

  public void setFLI(int FLI){flevel_i=FLI;}
  public void setFLS(String FLS){flevel_s=FLS;}
  public void setJFS(String JFS) {flevel_jpis=JFS;}
  public int getFLI(){return flevel_i;}
  public String getFLS(){return flevel_s;}
  public String getJFS() {return flevel_jpis;}
  
  public void setRecoilES(String ers){recoilE_s=ers;}
  public void setRecoilDES(String ders){drecoilE_s=ders;}
  
  //IES and ITS for ECBP, LOGFT for B- and ECBP
  public String IES(){return "";}
  public String DIES(){return "";}
  public String ITS(){return "";}
  public String DITS(){return "";}
  public String LOGFTS(){return "";}
  public String DLOGFTS(){return "";}
  
  public String decayType(){return decayType;}

  public String dpType() {return "";}
  public void setDPType(String type) {}
  
@Override
public void setEAvS(String s) {

    
}

@Override
public void setDEAvS(String s) {
    // TODO Auto-generated method stub
    
}

@Override
public String EAvS() {
    // TODO Auto-generated method stub
    return "";
}

@Override
public String DEAvS() {
    // TODO Auto-generated method stub
    return "";
}

@Override
public void setEAvF(float f) {
    // TODO Auto-generated method stub
    
}

@Override
public void setDEAvF(float f) {
    // TODO Auto-generated method stub
    
}

@Override
public float EAvF() {
    // TODO Auto-generated method stub
    return -1;
}

@Override
public float DEAvF() {
    // TODO Auto-generated method stub
    return -1;
}
 

}