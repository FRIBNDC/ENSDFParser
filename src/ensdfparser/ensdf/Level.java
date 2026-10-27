package ensdfparser.ensdf;

import java.util.*;

import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

public class Level extends Record{

  protected int index;//level index

  
  protected String e_s, de_s;

  protected float e_f, de_f;
  
  protected float e_off;//offset, "X+123.5"

  protected float er_f, der_f, erp_f, derp_f;

  protected String nne_s, ne_s;

  protected String epl_s, eplMev_s;

  protected boolean belongs2Band;
 
  //for E=1234.5+X if true value of X is known, like 1234.5+SN, SN is given and read from the dataset
  protected boolean isTrueERPF=false;
  
  protected String jpi_s;  
  protected String alt_jpi_s;//for storing alternative jpis, or jpis indicated by target jpi and L-transfer in transfer dataset
  
  protected int jcount; 
  protected Vector<Float> jVal=new Vector<Float>(); protected Vector<String> piVal=new Vector<String>(); protected Vector<Integer> piiVal=new Vector<Integer>();

  protected String t12_s, t12_whole_s, t12unit_s, dt12_s, t12_CR_s;//t12_CR_s: t12 string (value+unit+unc) from continuation record 

  protected double t12_d, dt12_d, t12_uv_d, t12_lv_d;

  protected String t12unicode_s, t12unifull_s;

  protected boolean isTFromCR, dt12IsNumber;
  
  protected boolean hasContinuedXREF,isFake;
  
  
  protected String l_s;
  protected String s_s; protected String ds_s;
  protected String ms_s;  
  protected String flag_s;
  protected String q_s;
  
  protected String bandFlag="",bandType="";

  //xref_s:orignal level XREF string after "XREF=" in file, consisting of letters or special characters 
  //       (one-letter or one-character tag), as well as "(",")","*","?", and energy value inside "()" 
  //xref_new: new level XREF using new tag letters
  //xref_new_formatted:new level XREF using new tag letters with a "." inserted for each dataset whole tag is not in XREF
  //in both xref_new and xref_new_formatted, tag after index=25 is two-letter tag, like "AA","AB"
  protected String xref_s,xref_new, xref_new_formatted;
  
  protected Vector<String> xrefsV=new Vector<String>();
  
  protected Vector<String> xrefsV_new=new Vector<String>();
  protected Vector<String> xrefsV_new_formatted=new Vector<String>();//store all XREFS, including empty ones, corresponding "." in xref_new_formatted
  
  protected boolean isCaptureState=false;

/*
  protected String mme1_s, dmme1_s, mme1Ref_s,
                 mme2_s, dmme2_s, mme2Ref_s,
                 mmm1_s, dmmm1_s, mmm1Ref_s,
                 mmm2_s, dmmm2_s, mmm2Ref_s,
                 be1_s, dbe1_s, be1Ref_s,
                 be2_s, dbe2_s, be2Ref_s,
                 be3_s, dbe3_s, be3Ref_s,
                 b2_s, db2_s, b2Ref_s,
                 b3_s, db3_s, b3Ref_s; 

  protected String conf_s, confRef_s,
                 g_s, dg_s, gRef_s,
                 ispin_s, ispinRef_s,
                 ispinz_s, ispinzRef_s, 
                 width_s, dwidth_s, widthRef_s,
                 widthg_s, dwidthg_s, widthgRef_s,
                 widthg0_s, dwidthg0_s, widthg0Ref_s,
                 widthn_s, dwidthn_s, widthnRef_s,
                 widthp_s, dwidthp_s, widthpRef_s,
                 widtha_s, dwidtha_s, widthaRef_s;
*/

  /* net feeding intensities */
 
  protected float nf_aif, nf_daif; protected String nf_ais, nf_dais;
  protected float nf_rif, nf_drif; protected String nf_ris, nf_dris;

  protected Vector<Integer> posgV=new Vector<Integer>();
  //protected Vector<Integer> poscV=new Vector<Integer>();

  protected Vector<Gamma> gamV=new Vector<Gamma>();

  //protected Vector<Comment> commentV=new Vector<Comment>();

  protected Vector<DecayMode> dmV=new Vector<DecayMode>();

  protected Vector<Integer> posbV=new Vector<Integer>();
  protected Vector<Beta> betV=new Vector<Beta>();

  protected Vector<Integer> poseV=new Vector<Integer>();
  protected Vector<ECBP> ecbV=new Vector<ECBP>();

  protected Vector<Integer> posaV=new Vector<Integer>();
  protected Vector<Alpha> alpV=new Vector<Alpha>();

  protected Vector<Integer> posdpV=new Vector<Integer>();
  protected Vector<DParticle> dpV=new Vector<DParticle>();

  protected Vector<Integer> dslinkV=new Vector<Integer>(); protected Vector<Integer> levlinkV=new Vector<Integer>(); protected boolean flink; 
  protected Vector<Integer> revLevLinkV=new Vector<Integer>();

  protected boolean lightLevel;

  @SuppressWarnings("unused")
  protected String contRecord;
  
  
  //protected Vector<String> contRecNameV;//for default pre-defined continuation record names
  
/* Plotting variables: */

  protected int pseudoBandNum;  protected int bandNum;
  protected boolean isPlotted; protected float xIPlot; protected float xFPlot; protected float yPlot;  
  protected boolean isFinalLevel; protected float xMinDash; protected float xMaxDash;
  protected boolean isHighlighted;
  protected boolean isFeedingLevel; protected boolean isFedLevel;

/* */

  protected Vector<String> junkV=new Vector<String>();

  protected boolean sysOut;

private boolean isPseudo;

/* Constructor: */

  //constructor used to create a handy level object containing just an energy
  public Level(float f,String ds,String JPi){
	  this();
	  
      e_f=f; e_s=Float.toString(f); 
      de_s=ds;
      if(e_f<0) e_f=-e_f;
      jpi_s=JPi;
  }
  
  public Level(String s,String ds,String JPi){
	  this();
	  
	  //System.out.println("#####"+s+"$$  "+JPi);
      e_s=s; e_f=Float.parseFloat(s); 
      de_s=ds;
      if(e_f<0) e_f=-e_f;
      jpi_s=JPi;
  }
  
  //usual constructor
  public Level(){

    e_s=""; de_s=""; e_f=(float)-1.0; de_f=(float)-1.0; epl_s=""; eplMev_s="";
    nne_s=""; ne_s=""; er_f=(float)-1.0; der_f=(float)-1.0; erp_f=(float)-1.0; derp_f=(float)-1.0;
    jpi_s="";alt_jpi_s="";
    t12_s=""; t12_whole_s=""; t12unit_s=""; dt12_s=""; t12unicode_s="";
    t12_d=-1.0;t12_uv_d=-1.0;t12_lv_d=-1.0; isTFromCR=false; dt12IsNumber=false; hasContinuedXREF=false;
    l_s=""; s_s=""; ds_s=""; ms_s=""; flag_s=""; q_s=""; xref_s=""; xref_new=""; xref_new_formatted="";
    
    bandFlag="";
    bandType="";
/*
    mme1_s=""; dmme1_s=""; mme2_s=""; dmme2_s=""; mmm1_s=""; dmmm1_s=""; mmm2_s=""; dmmm2_s=""; 
    be1_s=""; dbe1_s=""; be2_s=""; dbe2_s=""; be3_s=""; dbe3_s=""; b2_s=""; db2_s=""; b3_s=""; db3_s="";
    conf_s=""; g_s=""; dg_s=""; ispin_s=""; ispinz_s=""; width_s=""; dwidth_s="";
    widthg_s=""; dwidthg_s=""; widthg0_s=""; dwidthg0_s=""; widthn_s=""; dwidthn_s=""; 
    widthp_s=""; dwidthp_s=""; widtha_s=""; dwidtha_s="";
    contRecord="";
*/
    nf_aif=-1.0f; nf_daif=-1.0f; nf_ais=""; nf_dais="";
    nf_rif=-1.0f; nf_drif=-1.0f; nf_ris=""; nf_dris="";

    isPlotted=false;xIPlot=(float) -100.0; xFPlot=(float) -100.0;  yPlot=(float) -100.0;  
    isFinalLevel=false;
    isHighlighted=false;
    isFeedingLevel=false;isFedLevel=false;
    
    isTrueERPF=false;
    
    isPseudo=false;//pseudo level for proposed high-lying levels in continuum region in beta decay
    isFake=false;//fake level is added in beta-decay dataset to account for beta-delayed emission probabilities
    isGotoAdopted=true;
    
    belongs2Band=false;
    pseudoBandNum=-1; bandNum=-1;

    dslinkV.clear(); levlinkV.clear(); flink=false; revLevLinkV.clear();

    lines.clear();
    jVal.clear();
    piVal.clear();
    gamV.clear();
    posgV.clear();
    commentV.clear();
    dmV.clear();

    posbV.clear();
    betV.clear();

    poseV.clear();
    ecbV.clear();

    posaV.clear();
    alpV.clear();

    lightLevel=false;

    crLineV.clear();
    crV.clear();
    
    xrefsV.clear();
    xrefsV_new.clear();
    xrefsV_new_formatted.clear();

    /*
    contRecNameV=new Vector<String>();
    contRecNameV.addElement("MOME1");
    contRecNameV.addElement("MOME2");
    contRecNameV.addElement("MOMM1");
    contRecNameV.addElement("MOMM2");
    contRecNameV.addElement("BE1");
    contRecNameV.addElement("BE2");
    contRecNameV.addElement("BE3");
    contRecNameV.addElement("BE4");
    contRecNameV.addElement("BE5");
    contRecNameV.addElement("BE6");
    contRecNameV.addElement("BE1W");
    contRecNameV.addElement("BE2W");
    contRecNameV.addElement("BE3W");
    contRecNameV.addElement("BE4W");
    contRecNameV.addElement("BE5W");
    contRecNameV.addElement("BE6W");
    contRecNameV.addElement("BE1UP");
    contRecNameV.addElement("BE2UP");
    contRecNameV.addElement("BE3UP");
    contRecNameV.addElement("BE4UP");
    contRecNameV.addElement("BE5UP");
    contRecNameV.addElement("BE6UP");
    contRecNameV.addElement("BM1");
    contRecNameV.addElement("BM2");
    contRecNameV.addElement("BM3");
    contRecNameV.addElement("BM4");
    contRecNameV.addElement("BM5");
    contRecNameV.addElement("BM6");
    contRecNameV.addElement("BM1W");
    contRecNameV.addElement("BM2W");
    contRecNameV.addElement("BM3W");
    contRecNameV.addElement("BM4W");
    contRecNameV.addElement("BM5W");
    contRecNameV.addElement("BM6W");
    contRecNameV.addElement("BM1UP");
    contRecNameV.addElement("BM2UP");
    contRecNameV.addElement("BM3UP");
    contRecNameV.addElement("BM4UP");
    contRecNameV.addElement("BM5UP");
    contRecNameV.addElement("BM6UP");
    contRecNameV.addElement("B2");
    contRecNameV.addElement("B3");
    contRecNameV.addElement("B4");
    contRecNameV.addElement("B5");
    contRecNameV.addElement("B6");
    contRecNameV.addElement("B7");
    contRecNameV.addElement("CONF");
    contRecNameV.addElement("G");
    contRecNameV.addElement("ISPIN");
    contRecNameV.addElement("ISPINZ");
    contRecNameV.addElement("WIDTH");
    contRecNameV.addElement("WIDTHG");
    contRecNameV.addElement("WIDTHG0");
    contRecNameV.addElement("WIDTHG'");
    contRecNameV.addElement("WIDTHP");
    contRecNameV.addElement("WIDTHP0");
    contRecNameV.addElement("WIDTHP1");
    contRecNameV.addElement("WIDTHP2");
    contRecNameV.addElement("WIDTHP'");
    contRecNameV.addElement("WIDTHN");
    contRecNameV.addElement("WIDTHN0");
    contRecNameV.addElement("WIDTHN1");
    contRecNameV.addElement("WIDTHN'");
    contRecNameV.addElement("WIDTHA");
    contRecNameV.addElement("WIDTHA0");
    contRecNameV.addElement("WIDTHA1");
    contRecNameV.addElement("WIDTHA2");
    contRecNameV.addElement("WIDTHA3");
    contRecNameV.addElement("WIDTHA4");

*/
    
  }

  public void resetPlot(){
    isPlotted=false;
    xIPlot=(float) -1.0;
    xFPlot=(float) -1.0;
    yPlot=(float) -1.0;
    xMinDash=(float) -1.0;
    xMaxDash=(float) -1.0;
    isFinalLevel=false;
    isHighlighted=false;
    isFeedingLevel=false;
    isFedLevel=false;
    bandNum=-1;
    pseudoBandNum=-1;
  }

  public void setLight(){lightLevel=true;}

  public void setValues(Vector<String> v,int i)throws Exception{
   lines.clear();
   lines.addAll(v);
   index=i;
   if(lines.size() > 0 ) doWork();
  }
  public void isBlank(){
    e_s="new Level";
  }
  
  protected void parseEL() {
	    SDS2XDX s2x=new SDS2XDX();
	    s2x.setValues(e_s, de_s);
	    e_f=s2x.Xf();
	    de_f=s2x.DXf();

	    //debug
	    //System.out.println("In Level 1 line 320: es,ef="+e_s+" "+e_f+" line.length="+line.elementAt(0).length()+" ms_s="+ms_s);
	    
	    nne_s="";
	    ne_s="";

	    String se=e_s.toUpperCase().trim(); 
	  
	    if(!Str.isNumeric(se) && !se.isEmpty()) {
	        if( se.indexOf("SP") >=0 ){ se=se.replace('S',' ').replace('P',' '); nne_s="SP";}
	        else if( se.indexOf("SN") >=0 ){ se=se.replace('S',' ').replace('N',' '); nne_s="SN";}
	        else if( se.indexOf("SA") >=0 ){ se=se.replace('S',' ').replace('A',' '); nne_s="SA";}
	        else if( se.indexOf("S2N") >=0 ){ se=se.replace("S2N"," "); nne_s="S2N";}
	        else if( se.indexOf("S2P") >=0 ){ se=se.replace("S2P"," "); nne_s="S2P";}
	        else if(Character.isLetter(se.charAt(0))) {
	            nne_s=se.substring(0,1);
	            se=se.substring(1);
	        }else if(Character.isLetter(se.charAt(se.length()-1))) {
	            nne_s=se.substring(se.length()-1);
	            se=se.substring(0,se.length()-1);
	        }       
	    }

	    

	    
	    se=se.trim();

	    if( se.endsWith("+") || se.endsWith("-")) se=se.substring(0,se.length()-1);
	  

	    ne_s=se;
	    SDS2XDX er=new SDS2XDX();
	    er.setValues(se, de_s);

	    er_f=er.Xf();
	    der_f=er.DXf();
	    
	    if(se.length()==0 && nne_s.length()>0){//for level="X","Y","Z","SN"
	    	er_f=0f;
	    	der_f=0f;
	    }

	    erp_f=e_f;
	    derp_f=de_f;
	    //debug
	    //if(se.contains("+")) 
	    //System.out.println("In Level 2: es,ef="+e_s+" "+e_f+" se, erf,nne_s="+se+" "+er_f+" "+nne_s+"  des="+de_s);

	    /*
	    if(er_f>=0) {
	        if( e_s.indexOf("R") >=0 ||
	                e_s.indexOf("S") >=0 ||
	                e_s.indexOf("T") >=0 ||
	                e_s.indexOf("U") >=0 ||
	                e_s.indexOf("V") >=0 ||
	                e_s.indexOf("W") >=0 ||
	                e_s.indexOf("X") >=0 ||
	                e_s.indexOf("Y") >=0 ||
	                e_s.indexOf("Z") >=0 ||
	                e_s.indexOf("A") >=0 ||
	                e_s.indexOf("B") >=0 ||
	                e_s.indexOf("C") >=0 ||
	                e_s.indexOf("D") >=0 ||
	                e_s.indexOf("E") >=0 ||
	                e_s.indexOf("F") >=0 ||
	                e_s.indexOf("G") >=0 ||
	                e_s.indexOf("H") >=0 ){

	            erp_f=200+er_f; // we add 200 keV to unknown energy levels for plotting purposes only 
	            derp_f=der_f;

	           
	        }
	    }
	    */


	   
	    //System.out.println(" es="+e_s+" ef="+e_f+" er_f="+er_f+" erp_f="+erp_f);
	    
	/* cheap way of obtaining a not-so precise energy value: */

	    if( er_f > 0 ){
	      float fjunk=(float) 10.0;
	      if( nne_s.length() > 0) fjunk=(float) 1.0; 
	      int e_i=Math.round((float) er_f*fjunk); 

	      epl_s=""+ (float) e_i/fjunk ;
	      while( epl_s.endsWith("0") ){ epl_s=epl_s.substring(0,epl_s.length()-1); }
	      if(  epl_s.endsWith(".") ) epl_s=epl_s.substring(0,epl_s.length()-1);
	      if( nne_s.length() > 0 ) epl_s=nne_s+"+"+epl_s;

	      e_i=Math.round((float) er_f*fjunk); 
	      eplMev_s=""+ (float) e_i/(fjunk*1000) ;
	      while( eplMev_s.endsWith("0") ){ eplMev_s=eplMev_s.substring(0,eplMev_s.length()-1); }
	      if(  eplMev_s.endsWith(".") ) eplMev_s=eplMev_s.substring(0,eplMev_s.length()-1);
	      if( nne_s.length() > 0 ) eplMev_s=nne_s+"+"+eplMev_s;
	    }
	    else{ epl_s=e_s; eplMev_s=e_s; }

	    //fix SN, etc
	    e_s=e_s.toUpperCase();
	    e_s=e_s.replace("SN","S(n)");
	    e_s=e_s.replace("SP","S(p)");
	    e_s=e_s.replace("S2N","S(2n)");
	    e_s=e_s.replace("S2P","S(2p)");
	    e_s=e_s.replace("SA","S($\\alpha$)");
	    e_s=e_s.replace("EN", "E(n)");
	    e_s=e_s.replace("EP","E(p)");
	    e_s=e_s.replace("EA","E($\\alpha$)");
	    if( sysOut) System.out.println( "Done with cheap way of obtaining a not-so precise energy value= "+epl_s);	  
  }
  
  protected void parseJPI() {	    
	  if(! lightLevel ){
	        MJPi mjp=new MJPi();
	        mjp.setValues(jpi_s);
	        for( int k=1; k<= mjp.nJ(); k++){
	           jVal.addElement( new Float(mjp.j(k)) ); 
	           piVal.addElement( mjp.pi(k) );         
	           piiVal.addElement( new Integer( mjp.piI(k) ) );         
	        }
	     
	  }	 

	  if( sysOut) System.out.println( "Done with Working with jpi");
  }
  
  protected void parseT12() {

	    t12_whole_s=( t12_s+" "+t12unit_s+" "+dt12_s ).trim();
	    T12 t=new T12();
	    t.setValues( t12_s , t12unit_s , dt12_s );
	    t12_d=t.ValueInSeconds();
	    t12_uv_d=t.UVInSeconds();
	    t12_lv_d=t.LVInSeconds();
	    dt12_d=t.uncInSeconds();
	    t12unicode_s=t.t12Unicode();
	    t12unifull_s=t.t12UnicodeFull();
	    isTFromCR=false;
	    dt12IsNumber=t.dt12IsNumber();
	    t12_CR_s="";

	    if( sysOut) System.out.println( "Done with Working with T12");
  }
  
  private void doWork()throws Exception{
 
	  
    sysOut=false;

    /* Reading */

    try {
        recordLine=(String) lines.elementAt(0);
        
        nuc.parseNUCID( recordLine.substring(0,5) );

        e_s=recordLine.substring(9,19).trim();
        de_s=recordLine.substring(19,21).trim();
       /*   if( e_s.equals("40.0") || e_s.equals("35.8") ){ e_s="35.78"; de_s="13";}   */ 

        jpi_s=recordLine.substring(21,39).trim(); 
    /*     if( jpi_s.equals("(7-)") ){ e_s="438.636"; de_s="18";}  */

        l_s=recordLine.substring(55,64).trim();
        s_s=recordLine.substring(64,74).trim();
        ds_s=recordLine.substring(74,76).trim();
        flag_s=recordLine.substring(76,77).trim();
        ms_s=recordLine.substring(77,79).trim();
        q_s=recordLine.substring(79).trim();
        
        if(recordLine.charAt(77)=='R' && e_s.contains("+"))
        	isFake=true;
        
    }catch(Exception e) {}

    
   
/* Working with the energy */
    parseEL();

/* Working with jpi */
    parseJPI();

/* working with half-life */

    try {
        String tjunk=( (String) lines.elementAt(0) ).substring(39,49).trim();
        int is=tjunk.indexOf(" ");
        if( is > 0 ){ 
           t12_s=tjunk.substring(0,is).trim();
           t12unit_s=tjunk.substring(is).trim();
        }
        else{
           t12_s=tjunk;
           t12unit_s="";
        }

        dt12_s=( (String) lines.elementAt(0) ).substring(49,55).trim();
    }catch(Exception e) {
    	
    }

    parseT12();

    /* The Gamma rays */

    posgV.clear();
    for( int k=0; k < lines.size(); k++){
        if( ( (String) lines.elementAt(k) ).substring(5,9).equals("  G ") ){ posgV.addElement(new Integer(k));}
    }
    if( sysOut) System.out.println( "Done with PosG ");
    
    
    
    for(int k=0; k< posgV.size(); k++){
      int ki= ((Integer) posgV.elementAt(k)).intValue(); 
      int kf=lines.size()-1; 
      if( k < ( posgV.size()-1 ) ) kf=((Integer) posgV.elementAt(k+1)).intValue() -1;
      Vector<String> gline=new Vector<String>();
      for( int j=ki; j<=kf; j++){ if( ( (String) lines.elementAt(j) ).substring(7,8).equals("G") ) gline.addElement( (String) lines.elementAt(j)); }
      
      Gamma g=new Gamma();
      g.setSilent(silent);
      
      if( lightLevel ) g.setLight();

      g.dsid=dsid;
      g.setValues(gline);

      if(gamV.size()>0) {
          Gamma lastGam=gamV.lastElement();
          if(g.ES().equals(lastGam.ES())&&g.DES().equals(lastGam.DES())) {
        	  g.isMultipletMember=true;
        	  lastGam.isMultipletMember=true;
        	  if(lastGam.indexInMultiplet<0) {
        		  lastGam.indexInMultiplet=0;
        	  }
        	  
        	  g.indexInMultiplet=lastGam.indexInMultiplet+1;
          }
      }

      
      gamV.addElement(g);
      
    }

    analyzeGammas();
    
   if( sysOut) System.out.println( "Done with gammas");

   if( ! lightLevel ){

    /* The comments */
	   
	parseComments("L");
	
	for(Comment c:this.commentV) {
		String h=c.head();
		String b=c.body();
		if(h.equals("E") && (b.startsWith("S(n)")||b.startsWith("S(p)")) && this.q_s.equals("S")){
			isCaptureState=true;
			
			for(Gamma g:gamV)
				g.setIsPrimary(true);
			
			break;
		}
	}
	
	

/*
    poscV.clear();

    String recordType="";
    for( int k=0; k < line.size(); k++){
       
       recordType=line.elementAt(k).substring(5,9);
       if( ( recordType.toUpperCase().equals(" CL ") || 
           recordType.toUpperCase().equals(" TL ") ) &&
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
    	recordType=line.elementAt(j).substring(6,8);
        if(recordType.toUpperCase().equals("CL") ){
           cline.addElement( (String) line.elementAt(j)); 
        }else if(recordType.toUpperCase().equals("TL") ){
           String temp=line.elementAt(j);
           if(recordType.charAt(0)=='T')
        	   temp=temp.substring(0,1);
        }
      }
     Comment c=new Comment();
     c.setValues(cline);
     commentV.addElement(c);
    }
*/
    if( sysOut) System.out.println( "Done with comments");

     /* The continuation records */
     parseContinuationRecords();
     
     if( sysOut) System.out.println( "Done with continuation records");

 	
 	if(!t12_whole_s.isEmpty()) {
 		for(Comment com:this.commentV) {
 			if(com.headV().contains("T")) {
 				if(EnsdfUtil.isHalflifeUnit(t12unit_s)) {
 					com.setTag("T12");
 				}else if(EnsdfUtil.isWidthUnit(t12unit_s)) {
 					com.setTag("WIDTH");
 				}
 			}
 		}

 	}else {
 		//check if it is pseudo level
 		if(!isPseudo && nGammas()==0 && jpi_s.isEmpty()) {
 			for(Comment com:this.commentV) {
 				String s=com.rawBody().toUpperCase();
 				if(s.contains("PSEUDO LEVEL") || s.contains("PSEUDO-LEVEL") || s.contains("PSEUDOLEVEL") ){
 					isPseudo=true;
 					break;
 				}
 			}		
 		}
 	}
 	
    /* The Betas */

    for( int k=0; k < lines.size(); k++){
        if( ( (String) lines.elementAt(k) ).substring(5,9).equals("  B ") ){ posbV.addElement(new Integer(k));}
    }
    for(int k=0; k< posbV.size(); k++){
      int ki= ((Integer) posbV.elementAt(k)).intValue(); 
      int kf=lines.size()-1; 
      if( k < ( posbV.size()-1 ) ) kf=((Integer) posbV.elementAt(k+1)).intValue() -1;
      Vector<String> bline=new Vector<String>();
      for( int j=ki; j<=kf; j++){  if( ( (String) lines.elementAt(j) ).substring(7,8).equals("B") )  bline.addElement( (String) lines.elementAt(j)); }
      Beta b=new Beta();
      b.setValues(bline);
      b.setFLI(index);
      b.setFLS(ES());  
      b.setJFS(jpi_s);
      
      if(betV.size()>0) {
          Beta lastBet=betV.lastElement();
          if(b.ES().equals(lastBet.ES())&&b.DES().equals(lastBet.DES())) {
        	  b.isMultipletMember=true;
        	  lastBet.isMultipletMember=true;
        	  if(lastBet.indexInMultiplet<0) {
        		  lastBet.indexInMultiplet=0;
        	  }
        	  
        	  b.indexInMultiplet=lastBet.indexInMultiplet+1;
          }
      }
      betV.addElement(b);
      
      if(ms_s.equals("R")) {//fake level in B- decay which is used to represent a range of levels that will go though subsequent particle decay
          parseDParticleType(b,"n");
      }
    }

    /* The ECBP */

    for( int k=0; k < lines.size(); k++){
        if( ( (String) lines.elementAt(k) ).substring(5,9).equals("  E ") ){ poseV.addElement(new Integer(k));}
    }
    for(int k=0; k< poseV.size(); k++){
      int ki= ((Integer) poseV.elementAt(k)).intValue(); 
      int kf=lines.size()-1; 
      if( k < ( poseV.size()-1 ) ) kf=((Integer) poseV.elementAt(k+1)).intValue() -1;
      Vector<String> eline=new Vector<String>();
      for( int j=ki; j<=kf; j++){  if( ( (String) lines.elementAt(j) ).substring(7,8).equals("E") )  eline.addElement( (String) lines.elementAt(j)); }
      ECBP e=new ECBP();
      e.setValues(eline);
      e.setFLI(index);
      e.setFLS(ES());  
      e.setJFS(jpi_s);
      
      if(ecbV.size()>0) {
          ECBP lastECBP=ecbV.lastElement();
          if(e.ES().equals(lastECBP.ES())&&e.DES().equals(lastECBP.DES())) {
        	  e.isMultipletMember=true;
        	  lastECBP.isMultipletMember=true;
        	  if(lastECBP.indexInMultiplet<0) {
        		  lastECBP.indexInMultiplet=0;
        	  }
        	  
        	  e.indexInMultiplet=lastECBP.indexInMultiplet+1;
          }
      }
      
      ecbV.addElement(e);
      
      if(ms_s.equals("R")) {//fake level in EC/B+ decay which is used to represent a range of levels that will go though subsequent particle decay
          parseDParticleType(e,"p");
      }
    }

   /* The Alphas */

    for( int k=0; k < lines.size(); k++){
        if( ( (String) lines.elementAt(k) ).substring(5,9).equals("  A ") ){ posaV.addElement(new Integer(k));}
    }
    for(int k=0; k< posaV.size(); k++){
      int ki= ((Integer) posaV.elementAt(k)).intValue(); 
      int kf=lines.size()-1; 
      if( k < ( posaV.size()-1 ) ) kf=((Integer) posaV.elementAt(k+1)).intValue() -1;
      Vector<String> aline=new Vector<String>();
      for( int j=ki; j<=kf; j++){ if( ( (String) lines.elementAt(j) ).substring(7,8).equals("A") ) aline.addElement( (String) lines.elementAt(j)); }
      Alpha a=new Alpha();
      a.setValues(aline);
      a.setFLI(index);
      a.setFLS(ES());  
      a.setJFS(jpi_s);
      
      if(alpV.size()>0) {
          Alpha lastAlpha=alpV.lastElement();
          if(a.ES().equals(lastAlpha.ES())&&a.DES().equals(lastAlpha.DES())) {
        	  a.isMultipletMember=true;
        	  lastAlpha.isMultipletMember=true;
        	  if(lastAlpha.indexInMultiplet<0) {
        		  lastAlpha.indexInMultiplet=0;
        	  }
        	  
        	  a.indexInMultiplet=lastAlpha.indexInMultiplet+1;
          }
      }
      
      alpV.addElement(a);
    }

    /* The (delayed) Particles */

    
    Vector<String> dptV=new Vector<String>();
    for( int k=0; k < lines.size(); k++){
    	char part=lineAt(k).charAt(8);
        if(   lineAt(k).substring(5,7).equals("  ") &&
            ( lineAt(k).substring(7,8).equals(" ") || lineAt(k).substring(7,8).equals("D") ) &&
            ( part=='P' || part=='N' || part=='A' || part=='T' || part=='D') ){ 

             posdpV.addElement(new Integer(k));
             dptV.addElement( lineAt(k).substring(7,9) );
        }
    }

    for(int k=0; k< posdpV.size(); k++){
      int ki= ((Integer) posdpV.elementAt(k)).intValue(); 
      int kf=lines.size()-1; 
      if( k < ( posdpV.size()-1 ) ) kf=((Integer) posdpV.elementAt(k+1)).intValue() -1;
      Vector<String> dpline=new Vector<String>();
      for( int j=ki; j<=kf; j++){ 
         if( lineAt(j).substring(7,9).equals( (String) dptV.elementAt(k) )  ){
           dpline.addElement( (String) lines.elementAt(j)); 
         }
      }
      DParticle dp=new DParticle();
      dp.setValues(dpline);
      dp.setLEVI(index);
      dp.setLEVS(ES());   
      dp.setJFS(jpi_s);
      
      if(dpV.size()>0) {
          DParticle lastDP=dpV.lastElement();
          if(dp.ES().equals(lastDP.ES())&&dp.DES().equals(lastDP.DES())) {
        	  dp.isMultipletMember=true;
        	  lastDP.isMultipletMember=true;
        	  if(lastDP.indexInMultiplet<0) {
        		  lastDP.indexInMultiplet=0;
        	  }
        	  
        	  dp.indexInMultiplet=lastDP.indexInMultiplet+1;
          }
      }
      
      dpV.addElement(dp);

      
    }       

  } /* if it is not light */

/* clearing the vectors to save ram */

     poscV.clear();
     posgV.clear();
     posbV.clear();
     poseV.clear();
     posaV.clear();
     posdpV.clear();
     junkV.clear();
     lines.clear();

/* End of dowork: */
   
  }

  protected void parseContinuationRecords() {
	     boolean isNewCRLine=false;
	     boolean isSuppressed=false;
	     
	     //isExtension is for extended values of records in the record line and the CR will be used only when the record in record line is empty
	     boolean isExtension=false;
	     
	     Vector<String> entries;
	     ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("E","J","JPI","T","L","S"));
	     
	     String line="";
	     for( int k=0; k< lines.size(); k++){
	    	
	    	line=lines.elementAt(k);
	    	
	        if( line.substring(6,9).equals(" L ") && ! line.substring(5,6).equals(" ")) { 
	            
	        	isNewCRLine=true;
	    	    crLineV.addElement(line);
	    	
	            entries=findContRecordEntries(line);//find entries of all continuation records in current line including decay modes
	          

	            /* The decay modes */               
	            //find decay modes which are parsed differently and separately
	            //Here only decay modes with default pre-defined names are recognized and processed; non-defined decay modes will
	            //be still processed as continuation records
	            Vector<String> decayModeRecNames=new Vector<String>();
	            boolean isNewDMLine=false;
	            if( line.indexOf("%") >= 9){
	               DecayModeString dmst=new DecayModeString(); 
	               dmst.setValues( line);
	               isNewDMLine=true;
	               
	               //debug
	               //System.out.println("In Level.java line 647: dmst.size="+dmst.nameV().size());
	               
	               for( int m=0 ; m < dmst.nameV().size() ; m++){
	                  DecayMode dm=new DecayMode();
	                  dm.setValues( (String) dmst.nameV().elementAt(m) , (String) dmst.symbolV().elementAt(m) ,
	                                (String) dmst.valueV().elementAt(m) , (String) dmst.uncV().elementAt(m) ,
	                                (String) dmst.refV().elementAt(m),(String) dmst.textV().elementAt(m),isNewDMLine );

	/*
	                  dm.setName( (String) dmst.nameV().elementAt(m) );
	                  dm.setSymbol( (String) dmst.symbolV().elementAt(m) );
	                  dm.setValue( (String) dmst.valueV().elementAt(m) );
	                  dm.setUnc( (String) dmst.uncV().elementAt(m) );
	                  dm.setRef( (String) dmst.refV().elementAt(m) );
	*/
	                  isNewDMLine=false;
	                  dmV.addElement(dm);

	                  //debug
	                  //System.out.println("In Level.java line 662: dm.name="+dm.name());
	                  
	                  decayModeRecNames.add("%"+dm.name());
	               }
	            }
	            
	            FindValue fv=new FindValue(); 
	            String contType=line.substring(5,6);
	            
	            isSuppressed=contType.contains("S");
	            
	            boolean skip=false;

	            for(int m=0;m<entries.size();m++){
	            	String entry=entries.get(m);
	            	
	            	fv.clear();
	            	fv.findValueInEntry(entry); 
	            	String name=fv.name();
	            	         
	                //debug
	                //System.out.println("In Level.java line 800: fv.canGetV="+fv.canGetV()+" entry="+entry+" name="+name+" v="+fv.v());                
	                //for (String s:decayModeRecNames) System.out.println(s);
	                
	            	if(decayModeRecNames.contains(name))//continuation records for default decay modes are already processed separately
	            		continue;
	            	           	
	                //debug
	                //System.out.println("In Level.java line 704: fv.canGetV="+fv.canGetV()+" entry="+entry+" name="+name+" v="+fv.v());

	                
	                if(!fv.canGetV())
	                	continue;
	                
	                ContRecord cr=new ContRecord();
	                String crName=name;
	                String crText=fv.txt();
	                
	                if((name.indexOf("BE")==0 || name.indexOf("BM")==0) && name.length()==3 && Character.isDigit(name.charAt(2))){
	                	crName=name+"UP";
	                	crText=fv.txt().replaceFirst(name, crName);
	                }
	                
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
	                
	            	
	                //debug
	                //System.out.println("In Level.java line 830: cr.name="+cr.name()+" name="+name+" cr.s="+cr.s()+" cr.txt="+cr.txt()+" fv.txt()="+fv.txt()+" ref="+cr.ref());
	         
	                /* T1/2 from level continuation records, only cases like T=120 NS 40 are accepted at the
	                   moment, one should not use it for cases like T AP 120 NS */


	                if(name.equals("XREF")){
	                	xref_s=fv.vNoSymbol(); 
	                	crV.remove(cr);//not continuation record data with format of (s ds)
	                    skip=true;
	                }else if(name.equals("XREF+")){//additional XREF for very long XREF that can't be put in one line
	                	xref_s+=fv.vNoSymbol(); 
	                	crV.remove(cr);//not continuation record data with format of (s ds)
	                    skip=true;
	                }else if(name.equals("FLAG")){
	                	String s=fv.vNoSymbol();
	                	for(int i=0;i<s.length();i++) {
	                		String flag=s.charAt(i)+"";
	                		if(flag.trim().length()>0 && !flag_s.contains(flag)) {
	                			flag_s+=flag;
	                		}
	                	}
	                	
	                	crV.remove(cr);//not continuation record data with format of (s ds)
	                    skip=true;
	                }else if(name.equals("G")){//g-factor
	                	cr.setText(cr.txt().substring(0, 1).toLowerCase()+cr.txt().substring(1));
	                }else if(name.trim().indexOf("%")==0){//non-default decay modes have been processed as continuation records
	                	if(!silent) {
		                	System.out.println("***Warning: decay mode "+name+" is not among the default decay modes and should be added in.");
		                	System.out.println("   It will be processed as a continuation record and displayed in a new line.");
	                	}

	                }else if(name.equals("E") && de_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                	e_s=cr.s();
	                	de_s=cr.ds();
	                	parseEL();
	                }else if( (name.equals("J")||name.equals("JPI")) && jpi_s.isEmpty()) {
	                	jpi_s=cr.s();
	                	//System.out.println(jpi_s);
	                }else if(name.equals("T") && dt12_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                    t12_s=cr.s();
	                    dt12_s=cr.ds();
	                    t12unit_s=cr.units();

	                	parseT12();
	                	
	                    isTFromCR=true;
	                    t12_CR_s=fv.v();
	                }
	                /*
	                else if(name.equals("T")){
		                  t12_CR_s=fv.v();

		                  t12_whole_s=( fv.s() +" "+fv.units()+" "+fv.ds() ).trim();
		                  T12 tn=new T12();
		                  tn.setValues( fv.s() , fv.units() , fv.ds() );

		                  t12_d=tn.ValueInSeconds();
		                  t12_uv_d=tn.UVInSeconds();
		                  t12_lv_d=tn.LVInSeconds();
		                  dt12_d=0.5*( tn.UVInSeconds() - tn.LVInSeconds() );
		                  t12unicode_s=tn.t12Unicode();
		                  t12unifull_s=tn.t12UnicodeFull();
		                  isTFromCR=true;		                
	                }*/
	                else if( name.equals("L") && l_s.isEmpty()) {
	                	l_s=cr.s();
	                }else if( name.equals("S") && ds_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                	s_s=cr.s();
	                	ds_s=cr.ds();
	                }
	                
	                
	            	if(!skip)//the first entry in current line has been processed (not skipped), and the rest are in the same line instead of a new line
	            		isNewCRLine=false;     
	            }
	        
	        }
	   
	     }
	     
	     makeContRecordsVMap(); 	  
  }
  
  protected void analyzeGammas() {
	  
	  int ng=nGammas();	    
	  float prevERF=0;
	  for(int k=0; k < ng ; k++){  
		  Gamma g=gammaAt(k);
	      
	      if(g.EF()<0){
	    	  g.setERF(prevERF+0.1f, 0);
	      }else{
	          g.setERF(g.EF(), g.DEF());
	      }

	      prevERF=g.ERF();
	      
	      //debug
	      //System.out.println("In Level line 508: l="+ES()+" index="+index+" JPI="+JPiS()+" g="+g.ES());
	      
	      g.setLevI(index);
	      g.setLevS(ES());
	      g.setJIS(JPiS());
	  }
	  
	  /* The Gamma rays branching ratios */

	    //usually, all Ig values are normalized to the strongest gamma that has numerical uncertainty and 
	    //is not questionable, unless other gamma is specified to be used as normalization gamma
	    //Use "N" at col 79 to specify such gamma (Jun added 1/22/2019)
	    
	    double igmax=0,ignorm=0,igmaxAll=0;//igmaxAll=max of Ig among all gammas including those with ? mark
	    
	    if(ng==1){/* If there is only 1 gamma we assign it a br=100 and tbr=1 */
	        Gamma g=gammaAt(0);
	        g.setRelBRS( "100" ); 
	        g.setDRelBRS( "" ); 
	        g.setRelBRD( 100.0 );
	        g.setDRelBRD( 0.0 );
	        g.setBRD2Unity( 1.0 );
	        g.setDBRD2Unity( 0.0 );
	    }
	    
	    Sum igSum=new Sum();
	    boolean hasQMark=false;
	    boolean hasDIG=true;
	    boolean isCombinedIG=false;//combined IG for multiplet
	    char c;
	    for(int k=0; k < ng ; k++){
	       
	        Gamma g=gammaAt(k);
	        
	    	try{
	    	   
	           hasQMark=g.q().equals("?");
	           hasDIG=Str.isNumeric(g.DRIS().trim()+"0");
	           isCombinedIG=g.flag().contains("&");
	           if(g.RID()>igmax && !hasQMark && hasDIG && !isCombinedIG){
	        	   igmax=g.RID();     	   
	           } /* If there is more than 1 gamma we get the largest intensity */

	           if(g.RID()>igmaxAll)
	        	   igmaxAll=g.RID();
	           
	    	   //if not specified, igmax will be used for normalization
	    	   //if multiple are specified (probably by mistake), the first one will be used
	    	   c=g.recordLine.charAt(79);   	       	   
	    	   if((c=='N'||c=='%') && ignorm<=0){
	    		   ignorm=g.RID();
	    	   }
	    	   
	    	   //debug
	    	   //if(g.recordLine.contains("223.")) System.out.println("g="+g.ES()+" line="+g.recordLine+" c="+c+" ignorm="+ignorm+" g.ID()="+g.ID());

	       }catch(Exception e){}
	       
	       igSum.addPosElement( g.RID(),g.DRID() );
	    }
	    
	    if(ignorm<=0){
	    	if(igmax>0)
	    		ignorm=igmax;
	    	else if(igmaxAll>0)//all gammas have "?" mark
	    		ignorm=igmaxAll;
	    }
	    
	    double igsum=igSum.d(); 
	    double digsum=igSum.dd();
	 
	    if( ignorm > 0 && ng>1){

	        for(int k=0; k < gamV.size() ; k++){

	            Gamma g=gammaAt(k);

	            if( g.RID() >= 0 && g.RIS().length() > 0 ){

	                double junkX=g.RID()*100.0/ignorm;
	                double junkDX=junkX*0.1; 
	                if( g.DRID() > 0 ) junkDX=g.DRID()*100.0/ignorm; 
	                XDX2SDS x2s=new XDX2SDS();
	                x2s.setValues(junkX,junkDX);

	                g.setRelBRS( x2s.S() );
	                g.setRelBRD( junkX );

	                if( g.DRIS().equals("AP") || g.DRIS().equals("LT") || g.DRIS().equals("LE") ||
	                    g.DRIS().equals("SY") || g.DRIS().equals("CA") || g.DRIS().length() == 0 ||
	                    g.DRIS().equals("GT") || g.DRIS().equals("GE") ){

	                    g.setDRelBRS( ( (Gamma) gamV.elementAt(k) ).DRIS() );
	                    g.setDRelBRD( 0.0 );
	                    
	                    if(x2s.s().contains("E") && junkX<=999 && junkX>1) {
	                    	g.setRelBRS(Math.round(junkX)+"");
	                    }
	                }
	                else{ 
	                    g.setDRelBRS( x2s.DS() ); 
	                    g.setDRelBRD( junkDX );
	                }
	            } 
	            
	            if(g.TID()>0 && g.TIS().length()>0) {
	                double junkX=g.TID()*100.0/ignorm;
	                double junkDX=junkX*0.1; 
	                if( g.DTID() > 0 ) junkDX=g.DTID()*100.0/ignorm; 
	                XDX2SDS x2s=new XDX2SDS();
	                x2s.setValues(junkX,junkDX);

	                g.setRelTBRS( x2s.S() );
	                g.setRelTBRD( junkX );

	                if( g.DTIS().equals("AP") || g.DTIS().equals("LT") || g.DTIS().equals("LE") ||
	                    g.DTIS().equals("SY") || g.DTIS().equals("CA") || g.DTIS().length() == 0 ||
	                    g.DTIS().equals("GT") || g.DTIS().equals("GE") ){

	                    g.setDRelTBRS( ( (Gamma) gamV.elementAt(k) ).DTIS() );
	                    g.setDRelTBRD( 0.0 );
	                    
	                    if(x2s.s().contains("E") && junkX<=999 && junkX>1) {
	                    	g.setRelTBRS(Math.round(junkX)+"");
	                    }
	                }
	                else{ 
	                    g.setDRelTBRS( x2s.DS() ); 
	                    g.setDRelTBRD( junkDX );
	                }
	            }
	            
	        }
	     }
	    
	     if( ng> 1 ){
	       if( igsum > 0 ){
	         for(int k=0; k < nGammas() ; k++){
	            Gamma g= gammaAt(k);
	            double junkX=g.RID()/igsum;
	            double junkDX=0;
	            if( g.DRID() > 0 ) junkDX=Math.pow( g.DRID()/igsum, 2 );
	            if( digsum > 0 ) junkDX=junkDX+Math.pow( g.RID()*digsum/Math.pow(igsum,2) , 2 );
	            if( junkDX > 0 ) junkDX=Math.pow( junkDX ,0.5 );
	            g.setBRD2Unity( junkX ); g.setDBRD2Unity( junkDX );
	         }
	       } 
	     }	  
  }
  
  /*
   * parse comments of decay to find the delayed-particle type, like "1n","2n"
   * type="n" for B- decay
   *     ="p" for B+/EC decay
   */
  protected void parseDParticleType(Decay d,String type) {
      String[] ns= {"","1","2","3"};
      
      String decayType=d.decayType();
      if(decayType.equals("B"))
          decayType="B-";
      else if(decayType.equals("E"))
          decayType="EC";
      
      try {
        decayType=Translator.procGenCom(decayType,0,false);
      } catch (Exception e) {}
      
      for(int i=0;i<d.nComments();i++) {
          Comment c=d.commentAt(i);
          String head=c.head();
          if(!head.contains("IB") && !head.contains("TI")) 
              continue;
          
          String s=c.cbody();
          
          //System.out.println("s="+s+" type="+type+" decayType="+decayType);
         
          for(int j=0;j<ns.length;j++) {
              String ts1="S("+ns[j]+type+")";
              String ts2=decayType+ns[j]+type;

              if(s.contains(ts1) || s.contains(ts2)) {
                  d.setDPType(ns[j]+type);
                  return;
              }
          }
                   
      }
  }
  
  private String lineAt(int k){return (String) lines.elementAt(k);}
  
/* ---------------------------------------------------- */

/* The public methods */




  public Nucleus nucleus(){ return nuc; }

  public String RIS(){return "";};
  public String DRIS(){return "";};
  
  public String ES()
  { 
	  if(e_s.toUpperCase().equals("UNPLACED"))
		  return "unplaced";
	  else
		  return e_s.toLowerCase().replace("e", "E").replace("s(", "S(");
  }
  
  public String coinS() {return "";}
  
  
  public String DES(){ return de_s;}
  
  public void setES(String s) {e_s=s;}
  public void setDES(String ds) {de_s=ds;}
  
  public float EF(){ return e_f;}
  public float DEF(){ return de_f;}
  public void setEF(float ef,float def){e_f=ef;de_f=def;}
  public void setERPF(float erpf,float derpf){erp_f=erpf;derp_f=derpf;}
  
  public float EOFF(){return e_off;}
  public void setEOFF(float eo){e_off=eo;
  }
  public float ERF(){ return er_f;}
  public float DERF(){ return der_f;}
  public float ERPF(){ return erp_f;}
  public float DERPF(){ return derp_f;}
  public String NNES(){ return nne_s;} /* non-numerical energy, if e_s="100+X" nne_s="X" */
  public String NES(){ return ne_s;} /* numerical energy if e_s="100+X" ne_s="100" */
  public String EPlotS(){ return epl_s;} /* energy used in plotting */
  public String EPlotMeVS(){ return eplMev_s;} /* energy used in plotting in MIRD */

  public void setIndex(int i){index=i;}
  public int getIndex(){return index;}
  
  public String JPiS(){ return jpi_s;}

  public String altJPiS(){ return alt_jpi_s;}//for jps deduced from target jps and L-transfer in transfer dataset
  public void setAltJPiS(String s) {alt_jpi_s=s;}
  
  public int nJs(){ return jVal.size();}
  public Vector<Float> jVal(){ return jVal; }
  public float jValAt(int k){ return ( (Float) jVal.elementAt(k) ).floatValue(); }
  public Vector<String> piVal(){ return piVal; }
  public int nPis(){ return piVal.size(); }
  public String piValAt(int k){ return (String) piVal.elementAt(k); }
  public Vector<Integer> piiVal(){ return piiVal; }
  public int piIValAt(int k){ return ( (Integer) piiVal.elementAt(k) ).intValue(); }
  public void setJCount(int i){ jcount=i; }
  public int jCount(){ return jcount;}

  public String XREFS(){return xref_s;}//original xref string after "XREF="
  public String newXREFS(){ return xref_new;}
  public String newXREFSFormatted(){ return xref_new_formatted;}

  
  public void setXREFS(String s){xref_s=s;}
  public void setNewXREFS(String s){xref_new=s;}
  public void setNewXREFSFormatted(String s){xref_new_formatted=s;}
  
  
  
  public Vector<String> XRefsV(){return xrefsV;}
  public Vector<String> newXRefsV(){return xrefsV_new;}
  public Vector<String> newXRefsVFormatted(){return xrefsV_new_formatted;}
  
  public int nXRefsV(){return xrefsV.size();}
  public int nNewXRefsV(){return xrefsV_new.size();}
  public int nNewXRefsVFormatted(){return xrefsV_new_formatted.size();}
  
  
  public void setXRefsV(Vector<String> s){xrefsV.clear();xrefsV.addAll(s);}
  public void setNewXRefsV(Vector<String> s){xrefsV_new.clear();xrefsV_new.addAll(s);}
  public void setNewXRefsVFormatted(Vector<String> s){xrefsV_new_formatted.clear();xrefsV_new_formatted.addAll(s);}
  
  public String T12S(){ return t12_s;}
  public String T12Unit(){ return t12unit_s;}
  public String DT12S(){ return dt12_s;}
  public String halflife(){ return t12_whole_s;}
  public String T12Unicode(){ return t12unicode_s;}
  public String T12UnicodeFull(){ return t12unifull_s;}

  public double T12D(){ return t12_d;}
  public double DT12D(){ return dt12_d;}
  public double T12UpperD(){ return t12_uv_d;}
  public double T12LowerD(){ return t12_lv_d;}
  public boolean isTFromCR(){ return isTFromCR;}
  public boolean dt12IsNumber(){return dt12IsNumber;}
  public String t12CR(){return t12_CR_s;}

  public void setT12(String it12v,String it12u,String idt12){

    t12_s=it12v.trim();
    t12unit_s=it12u.trim();
    dt12_s=idt12.trim();

    t12_whole_s=( t12_s+" "+t12unit_s+" "+dt12_s ).trim();
    T12 t=new T12();
    t.setValues( t12_s , t12unit_s , dt12_s );
    t12_d=t.ValueInSeconds();
    t12_uv_d=t.UVInSeconds();
    t12_lv_d=t.LVInSeconds();
    dt12_d=t.uncInSeconds();
    t12unicode_s=t.t12Unicode();
    t12unifull_s=t.t12UnicodeFull();
    isTFromCR=false;
    t12_CR_s="";

  }

  public String lS(){ return l_s;}
  public String sS(){ return s_s;}
  public String dsS(){ return ds_s;}
  public String msS(){ return ms_s;}
  public String flag(){ return flag_s;}
  public String q(){ return q_s;}

  public void addFlag(String s){flag_s=flag_s+s;}
  
  public void addFlag0(String s){flag_s=s+flag_s;}
  
  public Vector<Gamma> GammasV(){ return gamV; }
  public int nGammas(){ return gamV.size(); }
  public Gamma gammaAt(int k){return (Gamma) gamV.elementAt(k);}

  public Vector<DecayMode> decayModesV(){return dmV;}
  
  public int nDecayModes(){ return dmV.size(); }
  public DecayMode DMAt(int k){ return (DecayMode) dmV.elementAt(k); }
  public void addDecayMode(DecayMode d){dmV.add(d);}
  public DecayMode decayModeAt(int k){ return (DecayMode) dmV.elementAt(k); }
  public DecayMode decayMode(String name){
		for(int i=0;i<dmV.size();i++){
			DecayMode dm=dmV.elementAt(i);
			if(dm.name().toUpperCase().equals(name.trim().toUpperCase()))
					return dm;
		}
		
		return null;	 
  }
  
  public boolean hasDecayMode(String s){
	for(int i=0;i<dmV.size();i++){
		DecayMode dm=dmV.elementAt(i);
		if(dm.name().toUpperCase().equals(s.trim().toUpperCase()))
				return true;
	}
	
	return false;
  }
  
  public String sumDecayModes(String separator, boolean doReference){
    String sdm="";
    for( int j=0; j< nDecayModes(); j++){
      DecayMode dm=decayModeAt(j);

      if( dm.symbol().equals("=") || dm.symbol().equals(">") || dm.symbol().equals("<") ) sdm=sdm+"%"+dm.name()+dm.symbol()+dm.value();
      else  sdm=sdm+"%"+dm.name()+" "+dm.symbol()+" "+dm.value();

      if( dm.unc().length() > 0 ) sdm=sdm+" "+dm.unc();
      if( doReference ){ if( dm.ref().length() > 0 ) sdm=sdm+" ("+dm.ref()+") "; }
      if( j < (nDecayModes() - 1) )  sdm=sdm+separator;
    }
    return sdm.trim();
  }

  public Vector<Beta> BetasV(){ return betV; }
  public int nBetas(){ return betV.size(); }
  public Beta betaAt(int k){return (Beta) betV.elementAt(k);}

  public Vector<ECBP> ECBPsV(){ return ecbV; }
  public int nECBPs(){ return ecbV.size(); }
  public ECBP ECBPAt(int k){return (ECBP) ecbV.elementAt(k);}

  public Vector<Alpha> AlphasV(){ return alpV; }
  public int nAlphas(){ return alpV.size(); }
  public Alpha alphaAt(int k){return (Alpha) alpV.elementAt(k);}

  public int nDecays(String type){
	  if(type.toUpperCase().equals("BETA"))
		  return nBetas();
	  else if(type.toUpperCase().equals("ALPH"))
		  return nAlphas();
	  else if(type.toUpperCase().equals("ECBP"))
		  return nECBPs();	  
	  else 
		  return 0;
  }
  
  public Decay decayAt(int k,String type){
	  if(k>=nDecays(type))
		  return null;
	  
	  if(type.toUpperCase().equals("BETA"))
		  return betaAt(k);
	  else if(type.toUpperCase().equals("ALPH"))
		  return alphaAt(k);
	  else if(type.toUpperCase().equals("ECBP"))
		  return ECBPAt(k);	  
	  else 
		  return null;
  }
  

  public Vector<?extends Decay> DecaysV(String type){
	  if(type.toUpperCase().equals("BETA"))
		  return (Vector<Beta>)BetasV();
	  else if(type.toUpperCase().equals("ALPH"))
		  return (Vector<Alpha>)AlphasV();
	  else if(type.toUpperCase().equals("ECBP"))
		  return (Vector<ECBP>)ECBPsV();	  
	  else 
		  return null;
  }
   
  public Vector<?extends Decay> DecaysV(){
	  if(betV.size()>0)
		  return betV;
	  else if(alpV.size()>0)
		  return alpV;
	  else if(ecbV.size()>0)
		  return ecbV;	  
	  else 
		  return new Vector<Decay>();
  }
  
  
  //Note: betV, alpV and ecbV are not null
  public Vector<?extends Decay> DecaysV0(){
	  Vector<Decay> decaysV=new Vector<Decay>();
	  
	  decaysV.addAll(betV);
	  decaysV.addAll(alpV);
	  decaysV.addAll(ecbV);
	  
	  return decaysV;
  }
  
  public int nDParticles(){ return dpV.size(); }
  public DParticle DParticleAt(int k){return (DParticle) dpV.elementAt(k);}
  public Vector<DParticle> DParticlesV(){return dpV;}

  public void addComment(Comment c){ commentV.addElement(c);}


  public void addLink(int i, int j){ dslinkV.addElement( new Integer(i)); levlinkV.addElement( new Integer(j)); }

  public int DSLinkAt(int i){ return ( (Integer) dslinkV.elementAt(i) ).intValue(); }
  public int LevLinkAt(int i){ return ( (Integer) levlinkV.elementAt(i) ).intValue(); }

  public int nDSlinks(){return dslinkV.size();}

  public void setFoundLink(boolean b){ flink=b;}
  public boolean foundLink(){return flink;}

  public void addRevLevLink(int i){ revLevLinkV.addElement( new Integer(i));}
  public int revLevLink(){
    if( revLevLinkV.size() == 0 ) return -1;
    else return ( (Integer) revLevLinkV.elementAt(0) ).intValue();
  }
  public int nRevLevLinks(){return revLevLinkV.size();}
  public int revLevLinkAt(int j){return ( (Integer) revLevLinkV.elementAt(j) ).intValue();}
  public void resetRevLevLink(int j){ revLevLinkV.clear(); revLevLinkV.addElement(new Integer(j));}

  public void setNetFeeding(float aif,float daif,String ais,String dais,float rif,float drif,String ris,String dris){
    nf_aif=aif; nf_daif=daif; nf_ais=ais; nf_dais=dais;
    nf_rif=rif; nf_drif=drif; nf_ris=ris; nf_dris=dris;
  }
  public float netFeedingAIF(){return nf_aif;}
  public float netFeedingDAIF(){return nf_daif;}
  public String netFeedingAIS(){return nf_ais;}
  public String netFeedingDAIS(){return nf_dais;}

  public float netFeedingRIF(){return nf_rif;}
  public float netFeedingDRIF(){return nf_drif;}
  public String netFeedingRIS(){return nf_ris;}
  public String netFeedingDRIS(){return nf_dris;}

  public void setXIPlot(float f){ xIPlot=f; isPlotted=true;}
  public void setXFPlot(float f){ xFPlot=f; isPlotted=true;}
  public void setYPlot(float f){ yPlot=f; isPlotted=true;}

  public boolean isPlotted(){ return isPlotted;} 
  public float xIPlot(){ return xIPlot;}
  public float xFPlot(){ return xFPlot;}
  public float yPlot(){ return yPlot;}

  public void reachBy(float f){
    if( isFinalLevel ){
      if( f <= xMinDash ) xMinDash=f;
      if( f >= xMaxDash ) xMaxDash=f;
    }
    if(! isFinalLevel ){
      isFinalLevel=true;
      xMinDash=f;
      xMaxDash=f;
    }
  }
  
  public boolean isLevel() {return true;}
  
  public boolean isTrueERPF() {return isTrueERPF;}
  public void setIsTrueERPF(boolean b) {isTrueERPF=b;}
  
  public boolean isFinalLevel(){return isFinalLevel;}
  public void setIsFinalLevel(){isFinalLevel=true;}
  
  public float xMinD(){return xMinDash;}
  public float xMaxD(){return xMaxDash;}

  public void setBelongs2Band(boolean b){belongs2Band=b;}
  public boolean belongs2Band(){return belongs2Band;}

  public void setPseudoBandNum(int i){pseudoBandNum=i;}
  public int pseudoBandNum(){return pseudoBandNum;}

  public void setBandNum(int i){bandNum=i;}
  public int bandNum(){return bandNum;}

  public void setHighlight(){isHighlighted=true;}
  public boolean isHighlighted(){return isHighlighted;}

  public void setFeedingLevel(){isFeedingLevel=true;}
  public boolean isFeedingLevel(){return isFeedingLevel;}
  public void setFedLevel(){isFedLevel=true;}
  public boolean isFedLevel(){return isFedLevel;}

  public String bandFlag() {return bandFlag;}
  public void setBandFlag(String flag) {bandFlag=flag;}
  
  public String bandType() {return bandType;}
  public void setBandType(String s) {bandType=s;}
  
  /* The continuation record is built from the individual variables */
  /*
  public String contRecord(){
    contRecord="";

    for( int j=0; j < nContRecords(); j++){
      builContRec( contRecordAt(j).name() , contRecordAt(j).symbol() , contRecordAt(j).s() , 
                   contRecordAt(j).ds() , contRecordAt(j).units(),contRecordAt(j).ref() );
    }

    builContRec( mme1_s,dmme1_s,mme1Ref_s,"MOME1");
    builContRec( mme2_s,dmme2_s,mme2Ref_s,"MOME2");
    builContRec( mmm1_s,dmmm1_s,mmm1Ref_s,"MOMM1");
    builContRec( mmm2_s,dmmm2_s,mmm2Ref_s,"MOMM2");
    builContRec( be1_s,dbe1_s,be1Ref_s,"BE1");
    builContRec( be2_s,dbe2_s,be2Ref_s,"BE2");
    builContRec( be3_s,dbe3_s,be3Ref_s,"BE3");
    builContRec( b2_s,db2_s,b2Ref_s,"B2");
    builContRec( b3_s,db3_s,b3Ref_s,"B3");

    builContRec(conf_s ,"","","CONF");
    builContRec( g_s,dg_s,gRef_s,"G");
    builContRec( ispin_s,"",ispinRef_s,"ISPIN");
    builContRec( ispinz_s,"",ispinzRef_s,"ISPINZ");
    builContRec( width_s,dwidth_s,widthRef_s,"WIDTH");
    builContRec( widthg_s,dwidthg_s,widthgRef_s,"WIDTHG");
    builContRec( widthg0_s,dwidthg0_s,widthg0Ref_s,"WIDTHG0");
    builContRec( widthp_s,dwidthp_s,widthpRef_s,"WIDTHP");
    builContRec( widthn_s,dwidthn_s,widthnRef_s,"WIDTHN");
    builContRec( widtha_s,dwidtha_s,widthaRef_s,"WIDTHA");

    return contRecord;
  }
  

  
  private void builContRec( String sName, String sSymbol, String sValue, String sUnits, String sUnc, String sRef){

    if( sName.trim().length() > 0 && sValue.trim().length() > 0 ){

         if( contRecord.length() > 0 ) contRecord=contRecord+"$";

         contRecord=contRecord+sName;
         if( sSymbol.equals("=") ) contRecord=contRecord+sSymbol+sValue;
         else contRecord=contRecord+" "+sSymbol+" "+sValue;
         if( sUnits.length() > 0 ) contRecord=contRecord+" "+sUnits;
         if( sUnc.length() > 0 ) contRecord=contRecord+" "+sUnc;
         if( sRef.length() > 0 ) contRecord=contRecord+" ("+sRef+") ";
    }
  }
    */
  
  
  /**
   * Return number of lines of ensdf.
   *
   * Adding acces to core ensdf for certain cases.
   * RZ
   */
  public int nLines(){
      return lines.size();
  }

  public void setHasContinuedXREF(boolean b) {
	// TODO Auto-generated method stub
	hasContinuedXREF=b;
  }
  
  public boolean hasContinuedXREF() {
	return hasContinuedXREF;
  }

  public boolean isPseudo() {
		return isPseudo;	
  }

  public void setIsPseudo(boolean b) {isPseudo=b;}

  public boolean isFake() {
	return isFake;
  }

}