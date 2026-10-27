 package ensdfparser.ensdf;

import java.util.*;

public class ECBP extends Decay{


protected String level_s;
  protected int level_i;
  protected boolean isPlaced;
  
  protected String flevel_s,flevel_jpis;
  protected int flevel_i;
  
  protected final String decayType="E";

  protected String e_s,e_s0;  protected String de_s,de_s0; protected float e_f; protected float de_f;
  protected String er_s;  protected String der_s; 
  protected String eav_s;  protected String deav_s; protected float eav_f; protected float deav_f; /* positron average energy */

  protected String epe_s; protected String depe_s; protected float epe_f; protected float depe_f; /* positron end point energy=EC end-point energy - 1022 */

  protected String epe_ec_s; protected String depe_ec_s; protected float epe_ec_f; protected float depe_ec_f; /* electron capture end point energy */

  protected String ib_s, dib_s; protected double ib_d, dib_d, ib_sy_d, dib_sy_d;//ib_sy_d: symmetrized IB intensity

  protected String ie_s, die_s; protected double ie_d, die_d, ie_sy_d, die_sy_d;

  protected String it_s, dit_s; protected double it_d, dit_d, it_sy_d, dit_sy_d;
  
  protected String it_alt_s,dit_alt_s;protected double it_alt_d,dit_alt_d;//alternative it from ib+ie, if it not given

  protected String aib_s, daib_s; protected double aib_d, daib_d, daibll_d;

  protected String aie_s, daie_s; protected double aie_d, daie_d, daiell_d;

  protected String ait_s, dait_s; protected double ait_d, dait_d, daitll_d;

  protected String dose_s, ddose_s; protected float dose_f, ddose_f; /* positron dose */

  protected String logft_s; protected String dlogft_s;

  protected String flag_s; 
  protected String coin_s; 

  protected String u_s; 

  protected String q_s;

  protected String ck_s, dck_s; protected float ck_f, dck_f; /* Fraction of capture to k-shell */
  protected String cl_s, dcl_s; protected float cl_f, dcl_f; /* Fraction of capture to l-shell */
  protected String cm_s, dcm_s; protected float cm_f, dcm_f; /* Fraction of capture to m-shell */
  protected String captFract;

  
  protected int orderEC, orderBP; 

  protected String dpType="";//for the type of subsequent particle decay if available, like 1p, 2p, 3p
  
  /* Comments */



  protected String enu_bp_s, denu_bp_s, enu_ec_s, denu_ec_s;
  protected float enu_bp_f, denu_bp_f, enu_ec_f, denu_ec_f;

  public ECBP(){

    lines.clear();
    commentV.clear();

    crLineV.clear();
    crV.clear();

    e_s=""; de_s=""; e_f=(float)-1.0; de_f=(float)-1.0;
    e_s0=""; de_s0=""; 
    
    eav_s=""; deav_s=""; eav_f=(float)-1.0; deav_f=(float)-1.0;

    epe_s=""; depe_s=""; epe_f=(float)-1.0; depe_f=(float)-1.0;

    ib_s=""; dib_s=""; ib_d=-1.0; dib_d=-1.0;ib_sy_d=-1.0; dib_sy_d=-1.0;

    ie_s=""; die_s=""; ie_d=-1.0; die_d=-1.0;ie_sy_d=-1.0; die_sy_d=-1.0;

    it_s=""; dit_s=""; it_d=-1.0; dit_d=-1.0;it_sy_d=-1.0; dit_sy_d=-1.0;

    it_alt_s="";dit_alt_s="";it_alt_d=-1.0; dit_alt_d=-1.0;
    
    aib_s=""; daib_s=""; aib_d=-1.0; daib_d=-1.0; daibll_d=-1.0;
    aie_s=""; daie_s=""; aie_d=-1.0; daie_d=-1.0; daiell_d=-1.0;
    ait_s=""; dait_s=""; ait_d=-1.0; dait_d=-1.0; daitll_d=-1.0;

    logft_s=""; dlogft_s="";

    flag_s="";
    coin_s="";

    u_s="";

    q_s="";

    dose_s=""; ddose_s=""; dose_f=(float) -1.0;  ddose_f=(float) -1.0;

    orderEC=-1;orderBP=-1;
    isPlaced=true;
    ck_s="";dck_s="";cl_s="";dcl_s="";cm_s="";dcm_s="";
    captFract="";

    dpType="";
    
    enu_bp_s=""; denu_bp_s=""; enu_ec_s=""; denu_ec_s="";
    enu_bp_f=-1.0f; denu_bp_f=-1.0f; enu_ec_f=-1.0f; denu_ec_f=-1.0f;

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
	    if( ib_s.length() == 0 ){
	        ib_d=-1.0;
	        dib_d=-1.0;
	        ib_sy_d=-1.0;
	        dib_sy_d=-1.0;
	      }
	      else{
	        SDS2XDX ib=new SDS2XDX();
	        ib.setValues(ib_s,dib_s);
	        ib_d=ib.X();
	        dib_d=ib.DX();
	        ib_sy_d=ib.XS();
	        dib_sy_d=ib.DXS();
	      }	  
  }
  protected void parseIE() {
	    if( ie_s.length() == 0 ){
	        ie_d=-1.0;
	        die_d=-1.0;
	        ie_sy_d=-1.0;
	        die_sy_d=-1.0;
	      }
	      else{
	        SDS2XDX ie=new SDS2XDX();
	        ie.setValues(ie_s,die_s);
	        ie_d=ie.X();
	        die_d=ie.DX();
	        ie_sy_d=ie.XS();
	        die_sy_d=ie.DXS();
	      }	  
  }
  
  protected void parseTI() {

	    if( it_s.length() == 0 ){
	      it_d=-1.0;
	      dit_d=-1.0;
	      it_sy_d=-1.0;
	      dit_sy_d=-1.0;
	    }
	    else{
	      SDS2XDX it=new SDS2XDX();
	      it.setValues(it_s,dit_s);
	      it_d=it.X();
	      dit_d=it.DX();
	      it_sy_d=it.XS();
	      dit_sy_d=it.DXS();
	    }

	    it_alt_s=it_s;
	    dit_alt_s=dit_s;
	    
	    it_alt_d=it_d;
	    dit_alt_d=dit_d;
	    
	    if( ( ib_s.length() > 0 || ie_s.length() > 0 ) && it_s.length() == 0 ){
	    
	     
	      SDS2XDX sx1=new SDS2XDX(ib_s,dib_s);
	      sx1.setErrorLimit(99);
	      
	      if(ib_s.isEmpty())
	    	  sx1=new SDS2XDX("0","0");
	      
	      SDS2XDX sx2=new SDS2XDX(ie_s,die_s);
	      if(ie_s.isEmpty())
	    	  sx2=new SDS2XDX("0","0");
	      
	      //System.out.println("ECBP 199: EL="+this.getFLS());
	      //System.out.println("@@ "+ie_s+"  "+sx2.s()+" ds="+sx2.ds()+" x="+sx2.x()+" "+ sx2.getErrorLimit());
	      
	      sx1=sx1.add(sx2);
	      
	      //System.out.println("## "+ie_s+"  "+sx1.s()+" "+sx1.ds()+" x="+ sx1.x()+" "+ sx1.getErrorLimit());
	      
	      it_alt_d=sx1.x();dit_alt_d=sx1.dx();
	      it_alt_s=sx1.s();dit_alt_s=sx1.ds();
	      
	      //symmetric value
	      it_sy_d=sx1.XS();dit_sy_d=sx1.DXS();    
	      
	      /*
	      //old
	      Sum si=new Sum();
	      si.addPosElement( ibs_d, dibs_d );
	      si.addPosElement( ies_d, dies_d );

	      its_d=si.d(); it_d=si.d(); dit_d=si.dd();
	      dits_d=si.dd();
	       
	      XDX2SDS x2s=new XDX2SDS(); x2s.setValues(si.d(),si.dd()); it_s=x2s.s(); dit_s=x2s.ds(); 
	      */
	        
	      //System.out.println("  sx1.s="+sx1.s()+" sx2.s="+sx2.s()+" ib_s="+ib_s+" dib_s="+dib_s+" ie_s="+ie_s+" die_s="+die_s+" it_s="+it_s+"  *"+dit_s);
	      //System.out.println(" it_d="+it_d+" dit_d="+dit_d+"  ib_s="+ib_s+" dib_s="+dib_s+" ie_s="+ie_s+" die_s="+die_s+" it_s="+it_s+"  *"+dit_s);
	    }	  
  }
  
  private void doWork() throws Exception{

	  try {
//	    System.out.println( (String) line.elementAt(0) );

		    recordLine=(String) lines.elementAt(0);

		    String nucName=( (String) lines.elementAt(0) ).substring(0,5).trim();
		    nuc=new Nucleus();
		    nuc.setGen(nucName);
		    
		    e_s=( (String) lines.elementAt(0) ).substring(9,19).trim();
		    de_s=( (String) lines.elementAt(0) ).substring(19,21).trim();

		    e_s0=e_s;
		    de_s0=de_s;
		    
		    ib_s=( (String) lines.elementAt(0) ).substring(21,29).trim();
		    dib_s=( (String) lines.elementAt(0) ).substring(29,31).trim();
		    
		    ie_s=( (String) lines.elementAt(0) ).substring(31,39).trim();
		    die_s=( (String) lines.elementAt(0) ).substring(39,41).trim();
		    
		    it_s=( (String) lines.elementAt(0) ).substring(64,74).trim();
		    dit_s=( (String) lines.elementAt(0) ).substring(74,76).trim();
		    
		    logft_s=( (String) lines.elementAt(0) ).substring(41,49).trim();
		    dlogft_s=( (String) lines.elementAt(0) ).substring(49,55).trim();
		    
		    flag_s=( (String) lines.elementAt(0) ).substring(76,77).trim();
		    u_s=( (String) lines.elementAt(0) ).substring(77,79).trim();
		    q_s=( (String) lines.elementAt(0) ).substring(79).trim();
	  }catch(Exception e) {
		  
	  }

    parseEB();
    parseIB();
    parseIE();
    parseTI();

    if( flag_s.equals("C") || flag_s.equals("?") ){
       coin_s=flag_s;
       flag_s="";
    }



    //continuation records
    parseContinuationRecords();
    


    /* The Comments: */

   parseComments("E");
   
   /*
    poscV.clear();

    for( int k=0; k < line.size(); k++){

       if( ( ((String) line.elementAt(k)).substring(5,9).equals(" CE ") || 
           ((String) line.elementAt(k)).substring(5,9).equals(" cE ") ) &&
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
        if(  ( (String) line.elementAt(j) ).substring(6,8).toUpperCase().equals("CE") ){
          cline.addElement( (String) line.elementAt(j)); 
          }
      }
     Comment c=new Comment();
     c.setValues(cline);
     commentV.addElement(c);
    }
*/

   lines.clear();
  /* End of dowork: */

  }
  
  
  protected void parseContinuationRecords() {
	   /* The Capture fractions: */
	    boolean isNewCRLine=false;
	    
	    boolean isExtension=false;
	    
	    Vector<String> entries;
	    ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("E","IB","IE","LOGFT","TI"));
	    String line="";
	    
	   for( int k=0; k < lines.size(); k++){
	       line=lines.elementAt(k);
	       
	        if( line.substring(6,9).equals(" E ") && !line.substring(5,6).equals(" ")) { 

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
	                }else if(name.equals("CK")) {
	                    ck_s=fv.s(); 
	                    dck_s=fv.ds();
	                    ck_f=fv.xf();
	                    dck_f=fv.dxf(); 

	                    isNewCRLine=false;
	                }else if(name.equals("CL")) {
	                    cl_s=fv.s();
	                    dcl_s=fv.ds();
	                    cl_f=fv.xf();
	                    dcl_f=fv.dxf(); 

	                    isNewCRLine=false;
	                }else if(name.equals("CM+")) {
	                    cm_s=fv.s();
	                    dcm_s=fv.ds();
	                    cm_f=fv.xf();
	                    dcm_f=fv.dxf();  

	                    isNewCRLine=false;
	                }else if(name.equals("E") && de_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	             	   e_s=cr.s();
	             	   de_s=cr.ds();    
	             	   e_s0=e_s;
	             	   de_s0=de_s;
	             	   
	             	   parseEB();
	                }else if(name.equals("IB") && dib_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	             	   ib_s=cr.s();
	             	   dib_s=cr.ds();        	
	             	   parseIB();
	                }else if(name.equals("IE") && die_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	              	   ie_s=cr.s();
	              	   die_s=cr.ds();        	
	              	   parseIE();
	              	   
	                 }else if(name.equals("TI") && dit_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
	               	   it_s=cr.s();
	               	   dit_s=cr.ds();        	
	               	   parseTI();
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
	   
	   captFract="";
	   if( ck_s.length() > 0 ){ 
	       captFract="CK="+ck_s;
	       if( dck_s.length() > 0 ) captFract=captFract+" "+dck_s;
	   }
	   if( cl_s.length() > 0 ){ 
	       if( captFract.length() > 0 ) captFract=captFract+"$";
	       captFract=captFract+"CL="+cl_s;
	       if( dcl_s.length() > 0 ) captFract=captFract+" "+dcl_s;
	   }
	   if( cm_s.length() > 0 ){ 
	       if( captFract.length() > 0 ) captFract=captFract+"$";
	       captFract=captFract+"CM+="+cm_s;
	       if( dcm_s.length() > 0 ) captFract=captFract+" "+dcm_s;
	   }
  }
  
  protected String rTrim(String s){
     String st=s.trim();
     int i=s.indexOf(st);
     String srt=s.substring(0,i)+st; 
     return srt;
  }
 
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

  public void setES(String ES){e_s=ES;}
  public void setDES(String DES){de_s=DES;}
  public void setEF(float ef){e_f=ef;}
  public void setDEF(float def){de_f=def;}
  
  public String recoilDES(){ return der_s;}
  public String recoilES(){ return er_s;}
  public void setRecoilES(String ERS){er_s=ERS;}
  public void setRecoilDES(String DERS){der_s=DERS;}
  
  public void setEAvF(float f){eav_f=f;}
  public void setDEAvF(float f){deav_f=f;}

  public void setEPES_BP(String s){ epe_s=s;}
  public void setDEPES_BP(String s){ depe_s=s;}
  public String EPES_BP(){ return epe_s;}
  public String DEPES_BP(){ return depe_s;}

  public void setEPEF_BP(float f){ epe_f=f;} /* The beta+ end-point energies */
  public void setDEPEF_BP(float f){ depe_f=f;}
  public float EPEF_BP(){ return epe_f;}
  public float DEPEF_BP(){ return depe_f;}

  public void setEPES_EC(String s){ epe_ec_s=s;}  /* The electron capture end-point energies */
  public void setDEPES_EC(String s){ depe_ec_s=s;}
  public String EPES_EC(){ return epe_ec_s;}
  public String DEPES_EC(){ return depe_ec_s;}

  public void setEPEF_EC(float f){ epe_ec_f=f;}
  public void setDEPEF_EC(float f){ depe_ec_f=f;}
  public float EPEF_EC(){ return epe_ec_f;}
  public float DEPEF_EC(){ return depe_ec_f;}

  public void setBPENeutrino(String s, String ds, float f, float df){ enu_bp_s=s; denu_bp_s=ds; enu_bp_f=f; denu_bp_f=df;}//for B+
  public void setECENeutrino(String s, String ds, float f, float df){ enu_ec_s=s; denu_ec_s=ds; enu_ec_f=f; denu_ec_f=df;}//for EC
  public float ENuF_BP(){ return enu_bp_f;}
  public float DENuF_BP(){ return denu_bp_f;}
  public float ENuF_EC(){ return enu_ec_f;}
  public float DENuF_EC(){ return denu_ec_f;}

  public String RIS(){return IBS();}
  public String DRIS(){return DIBS();}
  public double ID(){return IBD();}
  public double DID(){return DIBD();}
  
  public String IBS(){ return ib_s;}
  public String DIBS(){ return dib_s;}
  public double IBD(){ return ib_d;}
  public double DIBD(){ return dib_d;}
  public double syIBD(){ return ib_sy_d;}
  public double DsyIBD(){ return dib_sy_d;}

  public void setsyIBD(double d){ib_sy_d=d;}
  public void setDsyIBD(double d){dib_sy_d=d;}

  public String IES(){ return ie_s;}
  public String DIES(){ return die_s;}
  public double IED(){ return ie_d;}
  public double DIED(){ return die_d;}
  public double syIED(){ return ie_sy_d;}
  public double DsyIED(){ return die_sy_d;}

  public void setsyIED(double d){ie_sy_d=d;}
  public void setDsyIED(double d){die_sy_d=d;}

  public String ITS(){ return it_s;}
  public String DITS(){ return dit_s;}
  public double ITD(){ return it_d;}
  public double DITD(){ return dit_d;}
  public double syITD(){ return it_sy_d;}
  public double DsyITD(){ return dit_sy_d;}

  public String altITS() {return it_alt_s;}
  public String altDITS() {return dit_alt_s;}
  public double altITD(){ return it_alt_d;}
  public double altDITD(){ return dit_alt_d;}
  
  public void setsyITD(double d){it_sy_d=d;}
  public void setDsyITD(double d){dit_sy_d=d;}

  public void setAIS(String s){ aib_s=s;}
  public void setDAIS(String s){ daib_s=s;}
  public String AIS(){ return aib_s;}
  public String DAIS(){ return daib_s;}
  
  public void setAIBS(String s){ aib_s=s;}
  public void setDAIBS(String s){ daib_s=s;}
  public String AIBS(){ return aib_s;}
  public String DAIBS(){ return daib_s;}

  public void setAIES(String s){ aie_s=s;}
  public void setDAIES(String s){ daie_s=s;}
  public String AIES(){ return aie_s;}
  public String DAIES(){ return daie_s;}

  public void setAITS(String s){ ait_s=s;}
  public void setDAITS(String s){ dait_s=s;}
  public String AITS(){ return ait_s;}
  public String DAITS(){ return dait_s;}

  public String AITUnicode(){
    String aiu_s="";
    if(ait_s.length() > 0 ){ 
      if( dait_s.equals("GT") ) aiu_s=aiu_s+">"+ait_s;
      else if( dait_s.equals("GE") ) aiu_s=aiu_s+'\u2265'+ait_s;
      else if( dait_s.equals("LT") ) aiu_s=aiu_s+">"+ait_s;
      else if( dait_s.equals("LE") ) aiu_s=aiu_s+'\u2264'+ait_s;
      else if( dait_s.equals("AP") ) aiu_s=aiu_s+'\u2248'+ait_s;
      else aiu_s=ait_s;
//      aiu_s=aiu_s+" % ";
    }
    return aiu_s;
  }

  public void setAID(double d){ aib_d=d;}
  public void setDAID(double d){ daib_d=d;}
  public double AID(){ return aib_d;}
  public double DAID(){ return daib_d;}
  
  public void setAIBD(double d){ aib_d=d;}
  public void setDAIBD(double d){ daib_d=d;}
  public double AIBD(){ return aib_d;}
  public double DAIBD(){ return daib_d;}

  public void setDAIBLLD(double d){ daibll_d=d;} /* lower limit value */
  public double DAIBLLD(){ return daibll_d;}

  public void setAIED(double d){ aie_d=d;}
  public void setDAIED(double d){ daie_d=d;}
  public double AIED(){ return aie_d;}
  public double DAIED(){ return daie_d;}

  public void setDAIELLD(double d){ daiell_d=d;}  /* lower limit value */
  public double DAIELLD(){ return daiell_d;}

  public void setAITD(double d){ ait_d=d;}
  public void setDAITD(double d){ dait_d=d;}
  public double AITD(){ return ait_d;}
  public double DAITD(){ return dait_d;}

  public void setDAITLLD(double d){ daitll_d=d;}  /* lower limit value */
  public double DAITLLD(){ return daitll_d;}

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
  public String US(){ return u_s;}
  public String q(){ return q_s;}
  public String unique(){ return u_s;}

  public String CKS(){ return ck_s;}
  public String DCKS(){ return dck_s;}
  public float CKF(){ return ck_f;}
  public float DCKF(){ return dck_f;}

  public String CLS(){ return cl_s;}
  public String DCLS(){ return dcl_s;}
  public float CLF(){ return cl_f;}
  public float DCLF(){ return dcl_f;}

  public String CMS(){ return cm_s;}
  public String DCMS(){ return dcm_s;}
  public float CMF(){ return cm_f;}
  public float DCMF(){ return dcm_f;}

  public String captFract(){ return captFract; }

  public int nComments(){ return commentV.size(); }
  public Comment commentAt(int k){ return (Comment) commentV.elementAt(k); }
  public Vector<Comment> commentV(){ return commentV; }

  public void setOrderBP(int i){orderBP=i;}
  public int getOrderBP(){return orderBP;}

  public void setOrderEC(int i){orderEC=i;}
  public int getOrderEC(){return orderEC;}


  public void setUnplaced(){ isPlaced=false;}
  public boolean isPlaced(){return isPlaced;}

  
  public void setFLI(int FLI){flevel_i=FLI;}
  public void setFLS(String FLS){flevel_s=FLS;}
  public void setJFS(String JFS) {flevel_jpis=JFS;}
  public int getFLI(){return flevel_i;}
  public String getFLS(){return flevel_s;}
  public String getJFS() {return flevel_jpis;}
  
  //HF for ALPHA
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