 package ensdfparser.ensdf;

 /*
  * class for delayed particle decay, also including prompt particle decay
  */
import java.util.*;

public class DParticle extends Record{

  protected String level_s,level_jpis;
  protected int level_i;
  protected boolean isPlaced;

  protected String d_s;

  protected String part_s;

  protected String e_s;  protected String de_s; protected float e_f; protected float de_f;

  protected String er_s; protected String der_s; protected float er_f; protected float der_f;
  
  protected String erp_s; protected String derp_s; protected float erp_f; protected float derp_f;//fake

  
  protected String i_s;  protected String di_s; protected double i_d; protected double di_d; 

  protected double i_sy_d; protected double di_sy_d; 

  protected String ai_s; protected String dai_s; protected double ai_d; protected double dai_d;  protected String aiu_s;
  protected String daiu_s;

  protected String dose_s, ddose_s; protected float dose_f, ddose_f;

  protected String recoil_dose_s, recoil_ddose_s; protected float recoil_dose_f, recoil_ddose_f;

  protected String flag_s; 

  protected String q_s;

  protected String ei_s,dei_s; protected float ei_f,dei_f;

  protected String width_s; protected String dwidth_s, tunit_s; protected String l_s;

  protected String coin_s; 

  protected int order; 

  
  public DParticle(){

    lines.clear();
    isPlaced=true;

    commentV.clear();

    d_s=""; part_s="";

    e_s=""; de_s=""; e_f= -1.0f; de_f= -1.0f; er_s=""; der_s=""; er_f=(float)-1.0; der_f=(float)-1.0;

    erp_s=""; derp_s=""; erp_f=(float)-1.0; derp_f=(float)-1.0;
    
    i_s=""; di_s=""; i_d=-1.0; di_d=-1.0; 

    i_sy_d=-1.0; di_sy_d=-1.0;

    ai_s=""; dai_s=""; ai_d=-1.0; dai_d=-1.0; aiu_s=""; daiu_s="";

    flag_s="";

    q_s=""; coin_s="";

    ei_s=""; dei_s=""; ei_f=-1.0f; dei_f=-1.0f; width_s=""; dwidth_s=""; tunit_s=""; l_s="";

    dose_s=""; ddose_s=""; dose_f=(float) -1.0;  ddose_f=(float) -1.0;

    recoil_dose_s=""; recoil_ddose_s=""; recoil_dose_f=(float) -1.0;  recoil_ddose_f=(float) -1.0;
    level_s=""; level_jpis="";
    order=-1; 

  }

  public void setValues(Vector<String> v) throws Exception{
	  lines.clear();
   lines.addAll(v);

//   System.out.println("New dparticle");
   if(lines.size() > 0 ) doWork();
  }

  protected void parseEP() {

	    SDS2XDX e=new SDS2XDX();
	    e.setValues(e_s,de_s);
	    e_f=(float) e.X();
	    de_f=(float) e.DX();	 
	    
	    erp_f=e_f;
	    derp_f=de_f;
	    erp_s=e_s;
	    derp_s=e_s;
	    
	    /* The energy of the recoiling nucleus */

	    if( isProton() || isNeutron() ){
	      er_f=e_f/(1+nuc.a() );
	      der_f=de_f/(1+nuc.a() );
	    }
	    if( isAlpha() ){
	      er_f=e_f*4/(4+nuc.a() );
	      der_f=de_f*4/(4+nuc.a() );
	    }

	    XDX2SDS xr=new XDX2SDS(); 
	    xr.setValues(er_f,der_f);
	    er_s=xr.S(); der_s=xr.DS();

	    if(e_s.trim().length()==0){//for dp with empty energy record    	
	    }
  }
  protected void parseED() {
	    SDS2XDX ei=new SDS2XDX();
	    ei.setValues(ei_s, dei_s);
	    ei_f=(float) ei.X();	
	    dei_f=(float) ei.DX();	
  }
  
  protected void parseIP() {
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
		recordLine= (String) lines.elementAt(0);
		

	    String nucName=( (String) lines.elementAt(0) ).substring(0,5).trim();
	    nuc=new Nucleus();
	    nuc.setGen(nucName);
	    
	    d_s=( (String) lines.elementAt(0) ).substring(7,8);

	    part_s=( (String) lines.elementAt(0) ).substring(8,9);

	    e_s=( (String) lines.elementAt(0) ).substring(9,19).trim();
	    de_s=( (String) lines.elementAt(0) ).substring(19,21).trim();

	    i_s=( (String) lines.elementAt(0) ).substring(21,29).trim();
	    di_s=( (String) lines.elementAt(0) ).substring(29,31).trim();

	    ei_s=( (String) lines.elementAt(0) ).substring(31,39).trim();	    

	    width_s=( (String) lines.elementAt(0) ).substring(39,49).trim();
	    dwidth_s=( (String) lines.elementAt(0) ).substring(49,55).trim();
	    
	    
	    l_s=( (String) lines.elementAt(0) ).substring(55,64).trim();

	    flag_s=( (String) lines.elementAt(0) ).substring(76,77).trim();
	    q_s=( (String) lines.elementAt(0) ).substring(79).trim();
	    coin_s=( (String) lines.elementAt(0) ).substring(77,78).trim();
	}catch(Exception e) {
		
	}


    parseEP();
    parseED();
    parseIP();
    
   
    String s=width_s;
    int is=s.indexOf(" ");
    if( is > 0 ){ 
       width_s=s.substring(0,is).trim();
       tunit_s=s.substring(is).trim();
    }
    else{
       width_s=s;
       tunit_s="";
    }

    
/* The Comments: */

    parseComments("D");
    
/*
    poscV.clear();

    for( int k=0; k < line.size(); k++){

       if( ( ((String) line.elementAt(k)).substring(5,7).equals(" C") || 
           ((String) line.elementAt(k)).substring(5,7).equals(" c") ) &&
           ( (String) line.elementAt(k) ).substring(8).trim().length() > 0
          ){
             poscV.add( new Integer(k) ); 
           }
    }

    for(int k=0; k< poscV.size(); k++){
      int ki= ((Integer) poscV.elementAt(k)).intValue(); 
      int kf=line.size()-1; 
      if( k < ( poscV.size()-1 ) ) kf=((Integer) poscV.elementAt(k+1)).intValue() -1;
      Vector cline=new Vector();
      for( int j=ki; j <= kf; j++){ 
        if(  ( (String) line.elementAt(j) ).substring(6,7).toUpperCase().equals("C") ){
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
	    ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("E","IP","ED","WIDTH","T","L"));
	    String line="";
	    
	   for( int k=1; k< lines.size(); k++){
		   line=lines.elementAt(k);
		   char c0=line.charAt(6);
		   char c1=line.charAt(7);//D for delayed particle decay, empty for particle decay
		   char c2=line.charAt(8);//particle symbol
		   
	       if(c2!=' ' && (c1=='D'||c1==' ') && c0==' ' && !line.substring(5,6).equals(" ")) { 
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
	                }else if(name.equals("E") && de_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	              	   e_s=cr.s();
	              	   de_s=cr.ds();        	
	              	   parseEP();
	                 }else if(name.equals("IP") && di_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	              	   i_s=cr.s();
	              	   di_s=cr.ds();        	
	              	   parseIP();
	                 }else if( (name.equals("ED")||name.equals("EI")) && ei_s.isEmpty() && !cr.s().isEmpty()) {
	                     ei_s=cr.s();
	                     dei_s=cr.ds();
	                     parseED();
	                 }else if( (name.equals("WIDTH")||name.equals("T")) && dwidth_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                     width_s=cr.s();
	                     dwidth_s=cr.ds();
	                     tunit_s=cr.units();
	                 }else if( name.equals("L") && l_s.isEmpty()) {
	                 	l_s=cr.s();
	                 }
	                
	             	if(!skip)//the first entry in current line has been processed (not skipped), and the rest are in the same line instead of a new line
	               		isNewCRLine=false;     
	           }  
	       }
	   }
	   makeContRecordsVMap();  
  }
/*  public methods */

  public String delayed(){ return d_s;}
  public boolean isDelayed(){ if( d_s.equals("D") ) return true; else return false;}
  public String particle(){ return part_s;}
  public boolean isProton(){ if( part_s.equals("P") ) return true; else return false;}
  public boolean isNeutron(){ if( part_s.equals("N") ) return true; else return false;}
  public boolean isAlpha(){ if( part_s.equals("A") ) return true; else return false;}

  public String ES()
  { 	  
	  return e_s.toLowerCase().replace("e", "E").replace("s(", "S(");
  }
  
  public String DES(){ return de_s;}
  public float EF(){ return e_f;}
  public float DEF(){ return de_f;}

  public String ERS(){ return er_s;}
  public String DERS(){ return der_s;}
  public float recoilEF(){ return er_f;}
  public float recoilDEF(){ return der_f;}

  public String ERPS(){ return erp_s;}
  public String DERPS(){ return derp_s;}
  public float ERPF(){ return erp_f;}
  public float DERPF(){ return derp_f;}
  
  public String RIS(){ return i_s;}
  public String DRIS(){ return di_s;}
  public double ID(){ return i_d;}
  public double DID(){ return di_d;}
  public double syID(){ return i_sy_d;}
  public double DsyID(){ return di_sy_d;}

  public void setID(double ri){ i_d=ri;}
  public void setDID(double dri){ di_d=dri;}
  
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

  public String EIS(){ return ei_s;}
  public String DEIS(){ return dei_s;}
  public float EIF(){ return ei_f;}
  public float DEIF(){ return dei_f;}
  public String TS(){ return width_s;}
  public String DTS(){ return dwidth_s;}
  public String TUNITS() {return tunit_s;}
  
  public String LS(){ return l_s;}

  public String flag(){ return flag_s;}
  public void addFlag(String s){flag_s+=s;}
  public String coinS(){ return coin_s;}
  public String q(){ return q_s;}

  
  public void addComment(String s) throws Exception{Comment c=new Comment(); c.setBody(s); commentV.addElement(c);}
 
  public void setUnplaced(){ isPlaced=false;}
  public boolean isPlaced(){return isPlaced;}

  public void setOrder(int i){order=i;}
  public int getOrder(){return order;}

  public void setERPS(String s){ erp_s=s;}
  public void setDERPS(String s){ derp_s=s;}

  public void setERPF(float f){ erp_f=f;}
  public void setDERPF(float f){ derp_f=f;}
  
  public void setES(String s){ e_s=s;}
  public void setDES(String s){ de_s=s;}

  public void setEF(float f){ e_f=f;}
  public void setDEF(float f){ de_f=f;}
  public void setLEVI(int l){level_i=l;}
  public int getLEVI(){return level_i;}
  public void setLEVS(String s){level_s=s;}
  public String getLEVS(){return level_s;}
  
  public void setJFS(String JFS) {level_jpis=JFS;}
  public String getJFS() {return level_jpis;}

}