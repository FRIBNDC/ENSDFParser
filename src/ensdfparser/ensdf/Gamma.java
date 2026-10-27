 package ensdfparser.ensdf;

import java.util.*;

public class Gamma extends Record{

  protected String level_s;
  protected int level_i;
  protected boolean isPlaced;

  protected String ji_s="",jf_s="";//spin-paries of parent and daughter levels

  protected String e_s;  protected String de_s; protected String epl_s; 
  protected String e_ref;//NSR keynumber for gamma-ray energy, used for selecting dataset for average
  
  protected float e_f, de_f, er_f,der_f, ecm_f, decm_f;  


  /* intensities as they appeared in the ENSDF file */

  protected String ri_s, dri_s; protected double ri_d, dri_d; 
  
//for alternative ri: if ri given it is the same as ri, if ri not given but ti given it is deduced from ti
  protected String ri_alt_s,dri_alt_s;protected double ri_alt_d,dri_alt_d;

  protected String ti_alt_s,dti_alt_s;protected double ti_alt_d,dti_alt_d;

  /* symmetrized intensities */

  protected double ri_sy_d, dri_sy_d; 

  /* absolute gamma intensities */

  protected String ai_s, dai_s; protected double ai_d, dai_d, daill_d; /* daill_d= a lower limit for the absolute intensity uncertainty */

  /* branching ratios, so that the largest intensity is assigned br_d=100 */

  protected String rel_br_s,drel_br_s; protected double rel_br_d, drel_br_d;
  
  protected String rel_tbr_s,drel_tbr_s; protected double rel_tbr_d, drel_tbr_d;//for g+ce total, relative branching, relative to strongest gamma 

  /* branching ratios, so that the sum of tbr_d is equal to 1 */

  protected double br_d_2unity, dtbr_d_2unity;

  protected String m_s; protected int gj; protected int gp;
  protected String alt_m_s;//assumed MULT from deltaJPI

  protected String mr_s; protected String dmr_s;

  /* total intensities as they appeared in the ENSDF file */

  protected String ti_s, dti_s; 
protected double ti_d, dti_d;

  /* symmetrized total intensities */

  protected double ti_sy_d, dti_sy_d; 

  /* absolute total intensities */

  protected double ati_d, dati_d;


  protected String cc_s, dcc_s, cc_symbol; protected float cc_f, dcc_f;  


  protected boolean resetCC;

  //previously fl_s and FLS() were reserved for "FL=" record only if given.
  //but now fl_s is also used for the searched final level
  //and hasFL is added to indicate if "FL=" record is given.
  
  protected String fl_s; protected int fl_i;
  protected String FL_record;//for "FL=" record only
  
  protected boolean hasFL=false,hasFlagCR=false;
  
  protected String flag_s; 
  protected String iflag_s; 

  protected String coin_s; 

  protected String q_s;


  
 
  protected String a2_s,da2_s,a4_s,da4_s,a6_s,da6_s,a2_ref,a4_ref,a6_ref,a2_symbol,a4_symbol,a6_symbol;
  
  protected String IG_s,dIG_s; protected float IG_f,dIG_f;//for %IG in continuation record in decay datasets, calculated using GABS
  //for dose
  protected String dose_s, ddose_s; protected float dose_f, ddose_f;
  
  
   
  protected String dco_s, ddco_s, dco_symbol, dco_ref;
  protected String dco1_s, ddco1_s, dco1_symbol, dco1_ref;
  protected String dco2_s, ddco2_s, dco2_symbol, dco2_ref;
  protected String dco3_s, ddco3_s, dco3_symbol, dco3_ref;
  protected String pol_s, dpol_s, pol_symbol, pol_ref;
  
  protected String kot_s, dkot_s, kot_symbol; protected float kot_f,dkot_f;
  protected String lot_s, dlot_s, lot_symbol; protected float lot_f,dlot_f;
  protected String mot_s, dmot_s, mot_symbol; protected float mot_f,dmot_f;
  protected String not_s, dnot_s, not_symbol; protected float not_f,dnot_f;
  protected String oot_s, doot_s, oot_symbol; protected float oot_f,doot_f;
  protected String pot_s, dpot_s, pot_symbol; protected float pot_f,dpot_f;
  protected String qot_s, dqot_s, qot_symbol; protected float qot_f,dqot_f;
  protected String npot_s, dnpot_s, npot_symbol; protected float npot_f,dnpot_f;
  
  protected String be1_s, dbe1_s, be1_symbol, be1_ref,
                 be2_s, dbe2_s, be2_symbol, be2_ref, 
                 be3_s, dbe3_s, be3_symbol, be3_ref, 
                 be4_s, dbe4_s, be4_symbol, be4_ref, 
                 be5_s, dbe5_s, be5_symbol, be5_ref,
                 be6_s, dbe6_s, be6_symbol, be6_ref,
                 be1w_s, dbe1w_s, be1w_symbol, be1w_ref,
                 be2w_s, dbe2w_s, be2w_symbol, be2w_ref, 
                 be3w_s, dbe3w_s, be3w_symbol, be3w_ref,
                 be4w_s, dbe4w_s, be4w_symbol, be4w_ref, 
                 be5w_s, dbe5w_s, be5w_symbol, be5w_ref,
                 be6w_s, dbe6w_s, be6w_symbol, be6w_ref,
                 bm1_s, dbm1_s, bm1_symbol, bm1_ref, 
                 bm2_s, dbm2_s, bm2_symbol, bm2_ref, 
                 bm3_s, dbm3_s, bm3_symbol, bm3_ref, 
                 bm4_s, dbm4_s, bm4_symbol, bm4_ref, 
                 bm5_s, dbm5_s, bm5_symbol, bm5_ref,
                 bm6_s, dbm6_s, bm6_symbol, bm6_ref,
                 bm1w_s, dbm1w_s, bm1w_symbol, bm1w_ref, 
                 bm2w_s, dbm2w_s, bm2w_symbol, bm2w_ref, 
                 bm3w_s, dbm3w_s, bm3w_symbol, bm3w_ref, 
                 bm4w_s, dbm4w_s, bm4w_symbol, bm4w_ref, 
                 bm5w_s, dbm5w_s, bm5w_symbol, bm5w_ref,
                 bm6w_s, dbm6w_s, bm6w_symbol, bm6w_ref;

  protected String kc_s, dkc_s, kc_symbol; protected float kc_f, dkc_f;

  protected String lc_s, dlc_s, lc_symbol; protected float lc_f, dlc_f, l1c_f, dl1c_f, l2c_f, dl2c_f, l3c_f, dl3c_f;

  protected String mc_s, dmc_s, mc_symbol; protected float mc_f, dmc_f;

  protected String nc_s, dnc_s, nc_symbol; protected float nc_f, dnc_f;

  protected String oc_s, doc_s, oc_symbol; protected float oc_f, doc_f;

  protected String pc_s, dpc_s, pc_symbol; protected float pc_f, dpc_f;

  protected String qc_s, dqc_s, qc_symbol; protected float qc_f, dqc_f;

  protected String npc_s, dnpc_s, npc_symbol; protected float npc_f, dnpc_f;

  protected String ecc_s, decc_s, ecc_symbol, ecc_ref;protected float ecc_f,decc_f;

  protected String ekc_s, dekc_s, ekc_symbol, ekc_ref;protected float ekc_f,dekc_f;

  protected String elc_s, delc_s, elc_symbol, elc_ref;protected float elc_f,delc_f;

  protected String emc_s, demc_s, emc_symbol, emc_ref;protected float emc_f,demc_f;

  protected String enc_s, denc_s, enc_symbol, enc_ref;protected float enc_f,denc_f;

  protected String eoc_s, deoc_s, eoc_symbol, eoc_ref;protected float eoc_f,deoc_f;

  protected String epc_s, depc_s, epc_symbol, epc_ref;protected float epc_f,depc_f;

  protected String eqc_s, deqc_s, eqc_symbol, eqc_ref;protected float eqc_f,deqc_f;

  protected String enpc_s, denpc_s, enpc_symbol, enpc_ref;protected float enpc_f,denpc_f;



  protected Vector<CE> ceV=new Vector<CE>();



  protected int revgamlink; protected Vector<Integer> gamlinkV=new Vector<Integer>();

  protected String warning;

  protected boolean lightGamma;

  protected int order; 


  protected String contRecord;


  /* Plotting variables: */

  protected boolean isPlotted; protected float xIPlot; protected float xFPlot; protected float yIPlot; protected float yFPlot; 

  /* Recommended Values: */

  protected String re_s;  protected String dre_s;

  protected String rbr_s;  protected String drbr_s;
  protected boolean isPrimary=false;//for (n,g)
 
  /* Comments */

  //protected Vector<Integer> poscV=new Vector<Integer>();
  //protected Vector<Comment> commentV=new Vector<Comment>();
  //protected Vector<String> line=new Vector<String>();

  
  public Gamma(){

    lines.clear();
    commentV.clear();
    
    contRecord="";

    crLineV.clear();
    crV.clear();

    level_s="";
    level_i=-1;
    isPlaced=true;

    isPrimary=false;
    
    ji_s="";jf_s="";
    
    e_s=""; de_s=""; epl_s=""; e_f=(float)-1.0; de_f=(float)-1.0;
    er_f=(float)-1.0; der_f=(float)-1.0;

    e_ref="";
    ri_s=""; dri_s=""; ri_d=-1.0; dri_d=-1.0;
   
    ri_sy_d=-1.0; dri_sy_d=-1.0;

    ri_alt_s="";dri_alt_s="";ti_alt_s="";dti_alt_s="";
    ri_alt_d=-1.0;dri_alt_d=-1.0;ti_alt_d=-1.0;dti_alt_d=-1.0;
    
    ai_s=""; dai_s="";ai_d=-1.0; dai_d=-1.0;

    rel_br_s=""; drel_br_s=""; rel_br_d=-1.0; drel_br_d=-1.0;
    
    rel_tbr_s=""; drel_tbr_s=""; rel_tbr_d=-1.0; drel_tbr_d=-1.0;

    m_s=""; alt_m_s=""; gj=0; gp=0;

    mr_s=""; dmr_s="";

    ti_s=""; dti_s=""; ti_d=-1.; dti_d=-1.; ti_sy_d=-1.; dti_sy_d=-1.;

    cc_s=""; dcc_s=""; cc_f=0.0f; dcc_f=0.0f;  
    
    cc_symbol="";
    
    resetCC=false;
    

    fl_s=""; fl_i=-1;
    FL_record="";//for "FL=" record only
    hasFL=false;

    flag_s=""; iflag_s="";

    coin_s="";

    q_s="";
    
    dose_s=""; ddose_s=""; dose_f=-1.0f;  ddose_f=-1.0f;

    kc_s=""; dkc_s="";  kc_f=-1.0f; dkc_f=-1.0f;

    lc_s=""; dlc_s=""; lc_f=-1.0f; dlc_f=-1.0f;

    mc_s=""; dmc_s=""; mc_f=-1.0f; dmc_f=-1.0f;

    nc_s=""; dnc_s=""; nc_f=-1.0f; dnc_f=-1.0f;

    oc_s=""; doc_s=""; oc_f=-1.0f; doc_f=-1.0f;

    pc_s=""; dpc_s=""; pc_f=-1.0f; dpc_f=-1.0f;

    qc_s=""; dqc_s=""; qc_f=-1.0f; dqc_f=-1.0f;

    npc_s=""; dnpc_s=""; npc_f=-1.0f; dnpc_f=-1.0f;

    kc_symbol="";lc_symbol="";mc_symbol="";nc_symbol="";npc_symbol="";

    ecc_s=""; decc_s=""; ecc_symbol=""; ecc_ref="";ecc_f=-1.0f; decc_f=-1.0f;
    ekc_s=""; dekc_s=""; ekc_symbol=""; ekc_ref="";ekc_f=-1.0f; dekc_f=-1.0f;
    elc_s=""; delc_s=""; elc_symbol=""; elc_ref="";elc_f=-1.0f; delc_f=-1.0f;
    emc_s=""; demc_s=""; emc_symbol=""; emc_ref="";emc_f=-1.0f; demc_f=-1.0f;
    enc_s=""; denc_s=""; enc_symbol=""; enc_ref="";enc_f=-1.0f; denc_f=-1.0f;
    eoc_s=""; deoc_s=""; eoc_symbol=""; eoc_ref="";eoc_f=-1.0f; deoc_f=-1.0f;
    epc_s=""; depc_s=""; epc_symbol=""; epc_ref="";eoc_f=-1.0f; depc_f=-1.0f;
    eqc_s=""; deqc_s=""; eqc_symbol=""; eqc_ref="";eqc_f=-1.0f; deqc_f=-1.0f;
    enpc_s=""; denpc_s=""; enpc_symbol=""; enpc_ref="";enpc_f=-1.0f; denpc_f=-1.0f;

    kot_s=""; dkot_s=""; kot_symbol=""; kot_f=-1.0f;dkot_f=-1.0f;
    lot_s=""; dlot_s=""; lot_symbol=""; lot_f=-1.0f;dlot_f=-1.0f;
    mot_s=""; dmot_s=""; mot_symbol=""; mot_f=-1.0f;dmot_f=-1.0f;
    not_s=""; dnot_s=""; not_symbol=""; not_f=-1.0f;dnot_f=-1.0f;
    oot_s=""; doot_s=""; oot_symbol=""; oot_f=-1.0f;doot_f=-1.0f;
    pot_s=""; dpot_s=""; pot_symbol=""; pot_f=-1.0f;dpot_f=-1.0f;
    qot_s=""; dqot_s=""; qot_symbol=""; qot_f=-1.0f;dqot_f=-1.0f;
    npot_s=""; dnpot_s=""; npot_symbol=""; npot_f=-1.0f;dnpot_f=-1.0f;

    IG_s="";dIG_s="";IG_f=-1.0f;dIG_f=-1.0f;

    be1_s=""; dbe1_s=""; be1_symbol=""; be1_ref="";
    be2_s=""; dbe2_s=""; be2_symbol=""; be2_ref="";
    be3_s=""; dbe3_s=""; be3_symbol=""; be3_ref="";
    be4_s=""; dbe4_s=""; be4_symbol=""; be4_ref=""; 
    be5_s=""; dbe5_s=""; be5_symbol=""; be5_ref="";

    be1w_s=""; dbe1w_s=""; be1w_symbol=""; be1w_ref=""; 
    be2w_s=""; dbe2w_s="";  be2w_symbol=""; be2w_ref="";  
    be3w_s=""; dbe3w_s="";  be3w_symbol=""; be3w_ref=""; 
    be4w_s=""; dbe4w_s="";  be4w_symbol=""; be4w_ref="";  
    be5w_s=""; dbe5w_s="";  be5w_symbol=""; be5w_ref=""; 

    bm1_s=""; dbm1_s=""; bm1_symbol=""; bm1_ref="";  
    bm2_s=""; dbm2_s=""; bm2_symbol=""; bm2_ref=""; 
    bm3_s=""; dbm3_s=""; bm3_symbol=""; bm3_ref=""; 
    bm4_s=""; dbm4_s=""; bm4_symbol=""; bm4_ref=""; 
    bm5_s=""; dbm5_s=""; bm5_symbol=""; bm5_ref=""; 

    bm1w_s=""; dbm1w_s="";  bm1w_symbol=""; bm1w_ref=""; 
    bm2w_s=""; dbm2w_s=""; bm2w_symbol=""; bm2w_ref=""; 
    bm3w_s=""; dbm3w_s=""; bm3w_symbol=""; bm3w_ref=""; 
    bm4w_s=""; dbm4w_s=""; bm4w_symbol=""; bm4w_ref=""; 
    bm5w_s=""; dbm5w_s=""; bm5w_symbol=""; bm5w_ref=""; 

    dco_s=""; ddco_s=""; dco_symbol=""; dco_ref="";
    dco1_s=""; ddco1_s=""; dco1_symbol=""; dco1_ref="";
    dco2_s=""; ddco2_s=""; dco2_symbol=""; dco2_ref="";
    dco3_s=""; ddco3_s=""; dco3_symbol=""; dco3_ref="";
    
    pol_s=""; dpol_s=""; pol_symbol=""; pol_ref="";

    a2_s=""; da2_s=""; a2_symbol=""; a2_ref="";
    a4_s=""; da4_s=""; a4_symbol=""; a4_ref="";
    a6_s=""; da6_s=""; a6_symbol=""; a6_ref="";

    
    
    revgamlink=-1; gamlinkV.clear();

    warning="";

    lightGamma=false;

    order=-1;

    re_s=""; dre_s=""; rbr_s=""; drbr_s="";

    isPlotted=true; xIPlot=(float)-100.0; xFPlot=(float)-100.0; yIPlot=(float)-100.0; yFPlot=(float)-100.0; 
    
    isGotoAdopted=true;

  }

  public void setLight(){lightGamma=true;}

  public void setValues(Vector<String> v)throws Exception{
   lines.clear();
   lines.addAll(v);
   if(lines.size() > 0 ) doWork();
  }

  public void isBlank(){ e_s="new Gamma";}
  
  public void setUnplaced(){ isPlaced=false;}
  public boolean isPlaced(){return isPlaced;}

  protected void parseEG() {
	    SDS2XDX e=new SDS2XDX();
	    e.setValues(e_s,de_s);
	    e_f=(float) e.X();
	    de_f=(float) e.DX();

	    //could be reset in Level.java
	    er_f=e_f;
	    der_f=de_f;
	    
	    ecm_f=e_f + (float) 0.5*e_f*e_f/931500/nuc.a();

	    decm_f=(float) ( 1+0.5*e_f/931500/nuc.a() )*de_f;	
	    
	    
	    /* cheap way of obtaining a not-so precise energy value: */

        if( e_f > 0 ){
	          float fjunk=(float) 10.0;
	          int e_i=Math.round((float) e_f*fjunk); 
	          epl_s=""+ (float) e_i/fjunk ;
	          while( epl_s.endsWith("0") ){ epl_s=epl_s.substring(0,epl_s.length()-1); }
	          if(  epl_s.endsWith(".") ) epl_s=epl_s.substring(0,epl_s.length()-1);
	       
        }else{ epl_s=e_s;}

//	     System.out.println("Gamma e="+e_s+" "+lineAt(0).length() );	    
  }
  protected void parseRI() {

/*    if( e_s.equals("1309.1") ) i_s="16.3"; di_s="9"; */

    SDS2XDX i=new SDS2XDX();
    i.setValues(ri_s,dri_s);
    ri_d=i.X();
    dri_d=i.DX();
    
    ri_sy_d=i.XS();
    dri_sy_d=i.DXS();	  
  }
  protected void parseTI() {
	    SDS2XDX ti=new SDS2XDX();
	    ti.setValues(ti_s,dti_s);
	    ti_d=ti.X();
	    dti_d=ti.DX();
	    ti_sy_d=ti.XS();
	    dti_sy_d=ti.DXS();	  
  }
  protected void parseAI() {
	    SDS2XDX ti=new SDS2XDX();
	    ti.setValues(ai_s,dai_s);
	    ai_d=ti.X();
	    dai_d=ti.DX(); 
}
  
  protected void parseCC() {
	    if( cc_s.length() > 0 && dcc_s.indexOf("G") < 0 && dcc_s.indexOf("L") < 0 && dcc_s.indexOf("A") < 0 ) cc_symbol="="; 

	    SDS2XDX cc=new SDS2XDX();
	    cc.setValues(cc_s,dcc_s);

	    /* Note, the SYMMETRIZED ERRORS are used for numeric version the conversion coefficients: */

	    cc_f=(float) cc.XS();
	    dcc_f=(float) cc.DXS();
	    if( cc_f < 0 ) cc_f=0; if( dcc_f < 0 ) dcc_f=0;  	  
  }
  
  protected void parseMUL() {
	    /* Multipolarities: */

	    String ms=m_s.replace('(',' ').replace(')',' ').replace('[',' ').replace(']',' ').trim();

	    if( ms.equals("E0") ){ gj=0; gp=1; }
	    else if( ms.equals("E1") ){ gj=1; gp=-1; }
	    else if( ms.equals("E2") ){ gj=2; gp=1; }
	    else if( ms.equals("E3") ){ gj=3; gp=-1; }
	    else if( ms.equals("E4") ){ gj=4; gp=1; }
	    else if( ms.equals("E5") ){ gj=5; gp=-1; }

	    else if( ms.equals("M1") ){ gj=1; gp=1; }
	    else if( ms.equals("M2") ){ gj=2; gp=-1; }
	    else if( ms.equals("M3") ){ gj=3; gp=1; }
	    else if( ms.equals("M4") ){ gj=4; gp=-1; }
	    else if( ms.equals("M5") ){ gj=5; gp=1; }

	    else if( ms.indexOf("M1+") >= 0){ gj=1; gp=1; }
	    else if( ms.indexOf("M2+") >= 0){ gj=2; gp=-1; }
	    else if( ms.indexOf("M3+") >= 0){ gj=3; gp=1; }
	    else if( ms.indexOf("M4+") >= 0){ gj=4; gp=-1; }
	    else if( ms.indexOf("M5+") >= 0){ gj=5; gp=1; }

	    else if( ms.indexOf("E0+") >= 0){ gj=0; gp=1; }
	    else if( ms.indexOf("E1+") >= 0){ gj=1; gp=-1; }
	    else if( ms.indexOf("E2+") >= 0){ gj=2; gp=1; }
	    else if( ms.indexOf("E3+") >= 0){ gj=3; gp=-1; }
	    else if( ms.indexOf("E4+") >= 0){ gj=4; gp=1; }
	    else if( ms.indexOf("E5+") >= 0){ gj=5; gp=-1; }

	    else if( ms.indexOf("M1,") >= 0){ gj=1; gp=1; }

	    else if( ms.equals("D") ){ gj=1; gp=0; }
	    else if( ms.equals("Q") ){ gj=2; gp=0; }

	    else if( ms.indexOf("D+") >= 0){ gj=1; gp=1; }
	    else if( ms.indexOf("Q+") >= 0){ gj=2; gp=1; }	  
  }
  private void doWork()throws Exception{
	  try {
		    nuc.parseNUCID( ( (String) lines.elementAt(0) ).substring(0,5) );

		    recordLine=(String) lines.elementAt(0);
		    
		    e_s=( (String) lines.elementAt(0) ).substring(9,19).trim(); 
		    de_s=( (String) lines.elementAt(0) ).substring(19,21).trim();
		    parseEG();

		    ri_s=( (String) lines.elementAt(0) ).substring(21,29).trim(); 
		    dri_s=( (String) lines.elementAt(0) ).substring(29,31).trim(); /* i_s=""; di_s=""; */

		    parseRI();

		    /* Multipolarity: */

		    m_s=( (String) lines.elementAt(0) ).substring(31,41).trim().replace("NOT", "not");
		    parseMUL();
		    
		    /* Mixing Ratio: */

		    mr_s=( (String) lines.elementAt(0) ).substring(41,49).trim();
		    dmr_s=( (String) lines.elementAt(0) ).substring(49,55).trim();

		    /* flag and q */

		    flag_s=( (String) lines.elementAt(0) ).substring(76,77).trim();

		    coin_s=( (String) lines.elementAt(0) ).substring(77,78).trim();

		    q_s=( (String) lines.elementAt(0) ).substring(79).trim(); 

		    /* conversion coefficients */

		    cc_s=( (String) lines.elementAt(0) ).substring(55,62).trim();
		    dcc_s=( (String) lines.elementAt(0) ).substring(62,64).trim();
		    parseCC();

		    ti_s=( (String) lines.elementAt(0) ).substring(64,74).trim();
		    dti_s=( (String) lines.elementAt(0) ).substring(74,76).trim();
		    parseTI();
	  }catch(Exception e) {
		  
	  }



    /* The Gamma Comments: */

    parseComments("G");  
    
/*
    poscV.clear();

    for( int k=0; k < line.size(); k++){

       if( ( lineAt(k).substring(5,9).equals(" CG ") || 
           lineAt(k).substring(5,9).equals(" cG ") ) &&
           lineAt(k).substring(9).trim().length() > 0
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
        if(  ( (String) line.elementAt(j) ).substring(6,8).toUpperCase().equals("CG") ){
          cline.addElement( (String) line.elementAt(j)); 
          }
      }
     Comment c=new Comment();
     c.setValues(cline);
     commentV.addElement(c);
    }
*/
    parseContinuationRecords();
     
   //debug
   //System.out.println("In Gamma.java line 854: energy="+ES()+" A2="+A2S()+" DA2="+DA2S()+" A4="+A4S()+" DA4="+DA4S());              

    /* The Conversion electrons */
    doConversionCoeff();

    /* clearing the vectors to save ram */
     poscV.clear();
     lines.clear();

   /* End of dowork: */

  }

  protected void parseContinuationRecords() {
	   /* The Gamma continuation records: */
	    boolean isNewCRLine=false;
	    boolean isSuppressed=false;
	    
	    //isExtension is for extended values of records in the record line and the CR will be used only when the record in record line is empty
	    boolean isExtension=false;
	    
	    Vector<String> entries;
	    ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("E","RI","M","MR","CC","TI"));
	    
	    String line="";
	    
	   for( int k=0; k < lines.size(); k++){

	        line=lines.elementAt(k);
	        
	        if( line.substring(6,9).equals(" G ") && !line.substring(5,6).equals(" ") ) { 
	        	isNewCRLine=true;
	        	
	            crLineV.addElement(line);
	            
	            entries=findContRecordEntries(line);//find entries of all continuation records in current line  
	            
	            FindValue fv=new FindValue();

	            isNewCRLine=true;
	            
	            String contType=line.substring(5,6);
	            
	            isSuppressed=contType.contains("S");
	            
	            boolean skip=false;

	            //debug
	            //System.out.println("In Gamma.java line 485: line="+line+" entries.size="+entries.size());
	            
	            for(int m=0;m<entries.size();m++){
	                String entry=entries.get(m);
	                String name=findContRecordName(entry);
	                
	                fv.findValueInEntry(entry); 
	                
	                //debug
	                //if(entry.indexOf("K/LM")>=0)
	                //  System.out.println("In Gamma.java line 603: fv.canGetV="+fv.canGetV()+" entry="+entry+" name="+name);
	                
	                if(!fv.canGetV())
	                    continue;
	                
	                ContRecord cr=new ContRecord();
	                String crName=name;
	                String crText=fv.txt();
	     
	                
	                if((name.indexOf("BE")==0 || name.indexOf("BM")==0) && name.length()==3 && Character.isDigit(name.charAt(2))){
	                    crName=name+"DWN";
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
	                
	                cr.setValues(crName,fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),crText,contType,isSupTemp,isNewCRLine,isExtension);                    
	                crV.add(cr);
	                
	                //debug
	                //System.out.println("In Gamma.java line 497: cr.name="+cr.name()+" name="+name+" cr.s="+cr.s());
	                
	                if(name.equals("FL")){
	                    FL_record=fv.s().toLowerCase().replace("e", "E").replace("s", "S");//keep consistency with level.ES()                      
	                    crV.remove(cr);//not continuation record data with format of (s ds)
	                    if(FL_record.trim().length()>0)
	                        hasFL=true;
	                    else
	                        hasFL=false;
	                    skip=true;
	                }else if(name.equals("FLAG")){
	                    flag_s=flag_s+fv.s(); 
                        if(fv.s().length()>0)
                        	hasFlagCR=true;
                        else
                        	hasFlagCR=false;
                        
	                    crV.remove(cr);//not continuation record data with format of (s ds)
	                    skip=true;
	                }else if(name.equals("CC")){
	                    cc_s=fv.s(); dcc_s=fv.ds(); cc_symbol=fv.symbol();
	                    cc_f=(float) fv.x(); dcc_f=(float) fv.dx();
	                    if( cc_f < 0 ) cc_f=0; if( dcc_f < 0 ) dcc_f=0; 
	                    
	                    //debug
	                    //System.out.println("In Gamma line 554: cc_s="+cc_s+" dcc_s="+dcc_s+" cc_symbol="+cc_symbol);
	                }

	                else if(name.equals("ECC")){
	                    ecc_s=fv.s(); decc_s=fv.ds(); ecc_symbol=fv.symbol(); ecc_ref=fv.ref();     
	                    ecc_f=(float) fv.x(); decc_f=(float) fv.dx();
	                    if( ecc_f < 0 ) ecc_f=0; if( decc_f < 0 ) decc_f=0;
	                }                    
	                else if(name.equals("KC")){
	                    kc_s=fv.s();
	                    kc_symbol=fv.symbol();
	                    dkc_s=fv.ds();
	                    kc_f=fv.xf();
	                    dkc_f=fv.dxf(); 
	                    
	                    //debug
	                    //System.out.println("In Gamma line 567: kc_s="+kc_s+" dkc_s="+dkc_s+" kc_symbol="+kc_symbol);
	                }
	                else if(name.equals("K/T=")){

	                    kot_s=fv.s(); dkot_s=fv.ds(); kot_symbol=fv.symbol(); kot_f=fv.xf(); dkot_f=fv.dxf(); 


	                    // Important, the values of kc will be preferentially those from the "KC=" string,  
	                    //  if they are not given, those from "K/T=" and Gamma_cc_f will be used  

	                    if( kc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() );

	                                       
	                        //  kc_s=a.AIs();
	                        //  dkc_s=a.DAIs();
	                        

	                        kc_f=a.AIf();
	                        dkc_f=a.DAIf();
	                    }
	                }
	                else if(name.equals("EKC")){
	                    ekc_s=fv.s(); dekc_s=fv.ds();  ekc_symbol=fv.symbol(); ekc_ref=fv.ref();
	                    ekc_f=(float) fv.x(); dekc_f=(float) fv.dx();
	                    if( ekc_f < 0 ) ekc_f=0; if( dekc_f < 0 ) dekc_f=0;
	                    //  Important, the values of kc used to calculate the kc_f will be prefferentially 
	                    //    the calculated ones, if they are not given, the experimental ones will be used  

	                    if( kc_f <= 0 && fv.xf() > 0 ){
	                      kc_f=fv.xf();
	                      dkc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("LC")){
	                    lc_s=fv.s();
	                    lc_symbol=fv.symbol();
	                    dlc_s=fv.ds();
	                    lc_f=fv.xf();
	                    dlc_f=fv.dxf(); 
	                }                    
	                else if(name.equals("L/T=")){
	                    lot_s=fv.s(); dlot_s=fv.ds(); lot_symbol=fv.symbol(); lot_f=fv.xf(); dlot_f=fv.dxf(); 

	                    if( lc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() ); 
	               
	                        // lc_s=a.AIs();
	                        // dlc_s=a.DAIs(); 

	                        lc_f=a.AIf();
	                        dlc_f=a.DAIf();
	                    }
	                }                    
	                else if(name.equals("ELC")){
	                    elc_s=fv.s(); delc_s=fv.ds();  elc_symbol=fv.symbol(); elc_ref=fv.ref();
	                    elc_f=(float) fv.x(); delc_f=(float) fv.dx();
	                    if( elc_f < 0 ) elc_f=0; if( delc_f < 0 ) delc_f=0;
	                    
	                    if( lc_f <= 0 && fv.xf() > 0 ){
	                      lc_f=fv.xf();
	                      dlc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("MC")){
	                    mc_s=fv.s();
	                    mc_symbol=fv.symbol();
	                    dmc_s=fv.ds();
	                    mc_f=fv.xf();
	                    dmc_f=fv.dxf(); 
	                }                    
	                else if(name.equals("M/T=")){
	                    mot_s=fv.s(); dmot_s=fv.ds(); mot_symbol=fv.symbol(); mot_f=fv.xf(); dmot_f=fv.dxf(); 

	                    if( mc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() );               

	                        // mc_s=a.AIs();
	                        // dmc_s=a.DAIs(); 

	                        mc_f=a.AIf();
	                        dmc_f=a.DAIf();
	                    }

	                }                    
	                else if(name.equals("EMC")){
	                    emc_s=fv.s(); demc_s=fv.ds(); emc_symbol=fv.symbol(); emc_ref=fv.ref();
	                    emc_f=(float) fv.x(); demc_f=(float) fv.dx();
	                    if( emc_f < 0 ) emc_f=0; if( demc_f < 0 ) demc_f=0;
	                    if( mc_f <= 0 && fv.xf() > 0 ){
	                      mc_f=fv.xf();
	                      dmc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("NC")){
	                    nc_s=fv.s();
	                    nc_symbol=fv.symbol();
	                    dnc_s=fv.ds();
	                    nc_f=fv.xf();
	                    dnc_f=fv.dxf(); 
	                }
	                else if(name.equals("N/T=")){
	                    not_s=fv.s(); dnot_s=fv.ds(); not_symbol=fv.symbol(); not_f=fv.xf(); dnot_f=fv.dxf(); 
	                    if( nc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() );               
	                        // nc_s=a.AIs();
	                        // dnc_s=a.DAIs(); 
	                        nc_f=a.AIf();
	                        dnc_f=a.DAIf();
	                    }
	   
	                }                    
	                else if(name.equals("ENC")){
	                    enc_s=fv.s(); denc_s=fv.ds(); enc_symbol=fv.symbol(); enc_ref=fv.ref();
	                    enc_f=(float) fv.x(); denc_f=(float) fv.dx();
	                    if( enc_f < 0 ) enc_f=0; if( denc_f < 0 ) denc_f=0;
	                    if(  nc_f <= 0 && fv.xf() > 0 ){
	                      nc_f=fv.xf();
	                      dnc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("OC")){
	                    oc_s=fv.s();
	                    oc_symbol=fv.symbol();
	                    doc_s=fv.ds();
	                    oc_f=fv.xf();
	                    doc_f=fv.dxf(); 
	                }
	                else if(name.equals("O/T=")){
	                    oot_s=fv.s(); doot_s=fv.ds(); oot_symbol=fv.symbol(); oot_f=fv.xf(); doot_f=fv.dxf(); 
	                    if( oc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() );               
	                        // oc_s=a.AIs();
	                        //doc_s=a.DAIs(); 
	                        oc_f=a.AIf();
	                        doc_f=a.DAIf();
	                    }
	                }                    
	                else if(name.equals("EOC")){
	                    eoc_s=fv.s(); deoc_s=fv.ds(); eoc_symbol=fv.symbol(); eoc_ref=fv.ref(); 
	                    eoc_f=(float) fv.x(); deoc_f=(float) fv.dx();
	                    if( eoc_f < 0 ) eoc_f=0; if( deoc_f < 0 ) deoc_f=0;
	                    if(  oc_f <= 0 && fv.xf() > 0 ){
	                      oc_f=fv.xf();
	                      doc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("PC")){
	                    pc_s=fv.s();
	                    pc_symbol=fv.symbol();
	                    dpc_s=fv.ds();
	                    pc_f=fv.xf();
	                    dpc_f=fv.dxf(); 
	                }                    
	                else if(name.equals("P/T=")){
	                    pot_s=fv.s(); dpot_s=fv.ds(); pot_symbol=fv.symbol(); pot_f=fv.xf(); dpot_f=fv.dxf(); 
	                    if( pc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() );               
	                        // pc_s=a.AIs();
	                        // dpc_s=a.DAIs(); 
	                        pc_f=a.AIf();
	                        dpc_f=a.DAIf();
	                    }
	                }                    
	                else if(name.equals("EPC")){
	                    epc_s=fv.s(); depc_s=fv.ds(); epc_symbol=fv.symbol(); epc_ref=fv.ref();
	                    epc_f=(float) fv.x(); depc_f=(float) fv.dx();
	                    if( epc_f < 0 ) epc_f=0; if( depc_f < 0 ) depc_f=0;
	                    if(  pc_f <= 0 && fv.xf() > 0 ){
	                      pc_f=fv.xf();
	                      dpc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("QC")){
	                    qc_s=fv.s();
	                    qc_symbol=fv.symbol();
	                    dqc_s=fv.ds();
	                    qc_f=fv.xf();
	                    dqc_f=fv.dxf(); 
	                }                    
	                else if(name.equals("Q/T=")){
	                    qot_s=fv.s(); dqot_s=fv.ds(); qot_symbol=fv.symbol(); qot_f=fv.xf(); dqot_f=fv.dxf(); 
	                    if( qc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() );               
	                        // qc_s=a.AIs();
	                        // dqc_s=a.DAIs(); 
	                        qc_f=a.AIf();
	                        dqc_f=a.DAIf();
	                    }
	                }                    
	                else if(name.equals("EQC")){
	                    eqc_s=fv.s(); deqc_s=fv.ds(); eqc_symbol=fv.symbol(); eqc_ref=fv.ref();
	                    eqc_f=(float) fv.x(); deqc_f=(float) fv.dx();
	                    if( eqc_f < 0 ) eqc_f=0; if( deqc_f < 0 ) deqc_f=0;
	                    if(  qc_f <= 0 && fv.xf() > 0 ){
	                      qc_f=fv.xf();
	                      dqc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("NC+")){ 
	                    npc_s=fv.s();
	                    npc_symbol=fv.symbol();
	                    dnpc_s=fv.ds();
	                    npc_f=fv.xf();
	                    dnpc_f=fv.dxf(); 
	                }
	                else if(name.equals("N+")){ 
	                    npc_s=fv.s();
	                    dnpc_s=fv.ds();
	                    npc_symbol=fv.symbol();
	                    npc_f=fv.xf();
	                    dnpc_f=fv.dxf(); 
	                }                    
	                else if(name.equals("N+/T=")){ 
	                    npot_s=fv.s(); dnpot_s=fv.ds(); npot_symbol=fv.symbol(); npot_f=fv.xf(); dnpot_f=fv.dxf(); 
	                    if( npc_s.length() == 0 && cc_f > 0 && fv.canGetX() ){
	                        AbsInt a=new AbsInt(); 
	                        a.setValues( (cc_f+1), dcc_f, fv.x(), fv.dx() );               
	                        // npc_s=a.AIs();
	                        //dnpc_s=a.DAIs(); 
	                        npc_f=a.AIf();
	                        dnpc_f=a.DAIf();
	                    }
	                }                  
	                else if(name.equals("ENC+")){ 
	                    enpc_s=fv.s(); denpc_s=fv.ds(); enpc_symbol=fv.symbol(); enpc_ref=fv.ref();
	                    enpc_f=(float) fv.x(); denpc_f=(float) fv.dx();
	                    if( enpc_f < 0 ) enpc_f=0; if( denpc_f < 0 ) denpc_f=0;
	                    if( npc_f <= 0 && fv.xf() > 0 ){
	                      npc_f=fv.xf();
	                      dnpc_f=fv.dxf();
	                    }
	                }
	                else if(name.equals("BE1")){ 
	                    be1_s=fv.s(); dbe1_s=fv.ds(); be1_symbol=fv.symbol(); be1_ref=fv.ref();
	                }
	                else if(name.equals("BE2")){ 
	                    be2_s=fv.s(); dbe2_s=fv.ds(); be2_symbol=fv.symbol(); be2_ref=fv.ref();
	                    //debug
	                    //System.out.println("In Gamma line 797: be2_s="+be2_s+" dbe2_s="+dbe2_s+" be2_symbol="+cc_symbol);
	                }
	                else if(name.equals("BE3")){ 
	                    be3_s=fv.s(); dbe3_s=fv.ds(); be3_symbol=fv.symbol(); be3_ref=fv.ref();
	                }
	                else if(name.equals("BE4")){ 
	                    be4_s=fv.s(); dbe4_s=fv.ds(); be4_symbol=fv.symbol(); be4_ref=fv.ref();
	                }
	                else if(name.equals("BE5")){ 
	                    be5_s=fv.s(); dbe5_s=fv.ds(); be5_symbol=fv.symbol(); be5_ref=fv.ref();
	                }
	                else if(name.equals("BE6")){ 
	                    be6_s=fv.s(); dbe6_s=fv.ds(); be6_symbol=fv.symbol(); be6_ref=fv.ref();
	                }
	                else if(name.equals("BE1W")){ 
	                    be1w_s=fv.s(); dbe1w_s=fv.ds(); be1w_symbol=fv.symbol(); be1w_ref=fv.ref();
	                }
	                else if(name.equals("BE2W")){ 
	                    be2w_s=fv.s(); dbe2w_s=fv.ds(); be2w_symbol=fv.symbol(); be2w_ref=fv.ref();
	                }
	                else if(name.equals("BE3W")){ 
	                    be3w_s=fv.s(); dbe3w_s=fv.ds(); be3w_symbol=fv.symbol(); be3w_ref=fv.ref();
	                }
	                else if(name.equals("BE4W")){ 
	                    be4w_s=fv.s(); dbe4w_s=fv.ds(); be4w_symbol=fv.symbol(); be4w_ref=fv.ref();
	                }
	                else if(name.equals("BE5W")){ 
	                    be5w_s=fv.s(); dbe5w_s=fv.ds(); be5w_symbol=fv.symbol(); be5w_ref=fv.ref();
	                }
	                else if(name.equals("BE6W")){ 
	                    be6w_s=fv.s(); dbe6w_s=fv.ds(); be6w_symbol=fv.symbol(); be6w_ref=fv.ref();
	                }
	                else if(name.equals("BM1")){ 
	                    bm1_s=fv.s(); dbm1_s=fv.ds(); bm1_symbol=fv.symbol(); bm1_ref=fv.ref();
	                }
	                else if(name.equals("BM2")){ 
	                    bm2_s=fv.s(); dbm2_s=fv.ds(); bm2_symbol=fv.symbol(); bm2_ref=fv.ref();
	                }
	                else if(name.equals("BM3")){ 
	                    bm3_s=fv.s(); dbm3_s=fv.ds(); bm3_symbol=fv.symbol(); bm3_ref=fv.ref();
	                }
	                else if(name.equals("BM4")){ 
	                    bm4_s=fv.s(); dbm4_s=fv.ds(); bm4_symbol=fv.symbol(); bm4_ref=fv.ref();
	                }
	                else if(name.equals("BM5")){ 
	                    bm5_s=fv.s(); dbm5_s=fv.ds(); bm5_symbol=fv.symbol(); bm5_ref=fv.ref();
	                }
	                else if(name.equals("BM6")){ 
	                    bm6_s=fv.s(); dbm6_s=fv.ds(); bm6_symbol=fv.symbol(); bm6_ref=fv.ref();
	                }
	                else if(name.equals("BM1W")){ 
	                    bm1w_s=fv.s(); dbm1w_s=fv.ds(); bm1w_symbol=fv.symbol(); bm1w_ref=fv.ref();
	                }
	                else if(name.equals("BM2W")){ 
	                    bm2w_s=fv.s(); dbm2w_s=fv.ds(); bm2w_symbol=fv.symbol(); bm2w_ref=fv.ref();
	                }
	                else if(name.equals("BM3W")){ 
	                    bm3w_s=fv.s(); dbm3w_s=fv.ds(); bm3w_symbol=fv.symbol(); bm3w_ref=fv.ref();
	                }
	                else if(name.equals("BM4W")){ 
	                    bm4w_s=fv.s(); dbm4w_s=fv.ds(); bm4w_symbol=fv.symbol(); bm4w_ref=fv.ref();
	                }
	                else if(name.equals("BM5W")){ 
	                    bm5w_s=fv.s(); dbm5w_s=fv.ds(); bm5w_symbol=fv.symbol(); bm5w_ref=fv.ref();
	                }
	                else if(name.equals("BM6W")){ 
	                    bm6w_s=fv.s(); dbm6w_s=fv.ds(); bm6w_symbol=fv.symbol(); bm6w_ref=fv.ref();
	                }
	                else if(name.equals("DCO")){ 
	                    dco_s=fv.s(); ddco_s=fv.ds(); dco_symbol=fv.symbol(); dco_ref=fv.ref();
	                } 
	                else if(name.equals("POL")){ 
	                    pol_s=fv.s(); dpol_s=fv.ds(); pol_symbol=fv.symbol(); pol_ref=fv.ref();
	                } 
	                else if(name.equals("DCO1")){ 
	                    dco1_s=fv.s(); ddco1_s=fv.ds(); dco1_symbol=fv.symbol(); dco1_ref=fv.ref();
	                } 
	                else if(name.equals("DCO2")){ 
	                    dco2_s=fv.s(); ddco2_s=fv.ds(); dco2_symbol=fv.symbol(); dco2_ref=fv.ref();
	                } 
	                else if(name.equals("DCO3")){ 
	                    dco3_s=fv.s(); ddco3_s=fv.ds(); dco3_symbol=fv.symbol(); dco3_ref=fv.ref();
	                } 
	                else if(name.equals("A2")){
	                    if(a2_s.equals("")){
	                        a2_s=fv.s(); da2_s=fv.ds(); a2_symbol=fv.symbol(); a2_ref=fv.ref();
	                    }

	                }                           
	                else if(name.equals("A4")){
	                    if(a4_s.equals("") && (fv.ref().equals(a2_ref) || a2_ref.equals(""))){
	                        a4_s=fv.s(); da4_s=fv.ds(); a4_symbol=fv.symbol(); a4_ref=fv.ref();
	                    }

	                }                 
	                else if(name.equals("A6")){
	                    if(a6_s.equals("") && (fv.ref().equals(a4_ref) || a4_ref.equals(""))){
	                        a6_s=fv.s(); da6_s=fv.ds(); a6_symbol=fv.symbol(); a6_ref=fv.ref();
	                    }

	                }else if(name.equals("%IG")) {
	                    IG_s=fv.s(); dIG_s=fv.ds(); 
	                    IG_f=(float) fv.x(); dIG_f=(float) fv.dx();
	                }else if(name.equals("E") && de_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                	e_s=cr.s();
	                	de_s=cr.ds();
	                	parseEG();
	                }else if(name.equals("RI") && dri_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                	ri_s=cr.s();
	                	dri_s=cr.ds();
	                	parseRI();
	                }else if( (name.equals("M")||name.equals("MUL")) && m_s.isEmpty()) {
	                    m_s=cr.s();
	                	parseMUL();
	                }else if( name.equals("MR") && dmr_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                 	mr_s=cr.s();
	                	dmr_s=cr.ds();
	                }else if( name.equals("TI") && dti_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {
	                	ti_s=cr.s();
	                	dti_s=cr.ds();
	                	parseTI();
	                }
	           
	                
	                if(!skip)//the first entry in current line has been processed (not skipped), and the rest are in the same line instead of a new line
	                    isNewCRLine=false;             

	            }
	                

	        }
	        
	   }
	  
	   makeContRecordsVMap();  	  
  }
  
  @SuppressWarnings("unused")
protected String rTrim(String s){
     String st=s.trim();
     int i=s.indexOf(st);
     String srt=s.substring(0,i)+st; 
     return srt;
  }


  /*  public methods */

  
  public void doConversionCoeff(){

    ceV.clear();

    //for storing original IGS and DIGS, either from RI field or deduced from TI if RI is empty
    ri_alt_s=ri_s;
    dri_alt_s=dri_s;
    ri_alt_d=ri_d;
    dri_alt_d=dri_d;
    
    ti_alt_s=ti_s;
    dti_alt_s=dti_s;
    ti_alt_d=ti_d;
    dti_alt_d=dti_d;
    
    if(! m_s.equals("E0") ){

      if( kc_f > 0 || lc_f > 0 || mc_f > 0 || nc_f > 0 || npc_f > 0 || oc_f > 0 || pc_f > 0){  

        if( resetCC || cc_f <= 0){
           Sum ccSum=new Sum();
           ccSum.addPosElement( kc_f, dkc_f);
           ccSum.addPosElement( lc_f, dlc_f);
           ccSum.addPosElement( mc_f, dmc_f);
           ccSum.addPosElement( nc_f, dnc_f);
           ccSum.addPosElement( oc_f, doc_f);
           ccSum.addPosElement( pc_f, dpc_f);
           ccSum.addPosElement( qc_f, dqc_f);

           cc_f=ccSum.f(); dcc_f=ccSum.df();
        }

        // If total intensity and conversion coefficients are given: 
        
        if( ti_s.length() > 0 ){
          if(ri_s.length()==0) {
              ri_sy_d=ti_sy_d/(1.+cc_f);
              dri_sy_d=0;
              if( dti_sy_d > 0 ) dri_sy_d=dri_sy_d+Math.pow( dti_sy_d/(1.+cc_f) , 2);
              if( dcc_f > 0 )  dri_sy_d=dri_sy_d+Math.pow( dcc_f*ti_sy_d/( (1.+cc_f)*(1.+cc_f) ) , 2);
              if(dri_sy_d > 0 ) dri_sy_d=Math.pow(dri_sy_d,0.5);
             
              ri_d=ri_sy_d; dri_d=dri_sy_d;     
              
              SDS2XDX sx1=new SDS2XDX(ti_s,dti_s);
              SDS2XDX sx2=new SDS2XDX(cc_s,dcc_s);
              sx2=sx2.add(1);
              sx1=sx1.divided(sx2,true);
              ri_alt_s=sx1.s();
              dri_alt_s=sx2.ds();
              
              ri_alt_d=sx1.x();
              dri_alt_d=sx1.dx();
              
          }

        } 

        // If intensity and conversion coefficients are given given, but not the total intensity : 

        else if( ri_s.length() > 0 && ti_s.length() == 0 ){
     
          ti_sy_d=ri_sy_d*(1.+cc_f);
          dti_sy_d=0;
          if( dri_sy_d > 0 ) dti_sy_d=dti_sy_d+ Math.pow( dri_sy_d*(1.+cc_f) , 2 );
          if( dcc_f > 0 ) dti_sy_d=dti_sy_d+ Math.pow( ri_sy_d*dcc_f , 2 );
          if( dti_sy_d > 0 ) dti_sy_d=Math.pow( dti_sy_d , 0.5 );
          
          SDS2XDX sx1=new SDS2XDX(ri_s,dri_s);
          SDS2XDX sx2=new SDS2XDX(cc_s,dcc_s);
          sx2=sx2.add(1);
          sx1=sx1.multiply(sx2,true);
          ti_alt_s=sx1.s();
          dti_alt_s=sx2.ds();
          
          ti_alt_d=sx1.x();
          dti_alt_d=sx1.dx();
        }

        ABE abe=new ABE( nuc.Z() );       // The atomic binding energies 

        if( kc_f > 0 ){  
            CE ck=new CE();  
            ck.setValues( e_f , de_f , abe.Bkf() , abe.DBkf() , ri_sy_d , dri_sy_d , kc_f , dkc_f, "k" );
            ck.setOriginalIGS(ri_alt_s,dri_alt_s);
            ck.setFracOverTotal(kot_f, dkot_f);
            
            ck.setEGS(e_s, de_s);
            ck.setZ(nuc.Z());
            ck.setElement(nuc.EN());
            
            //reset I(ce) if TI and fraction-over-total are given
            //since I(ce)=RI*CC with RI from TI/(1+CCT) and CC from CCT*fraction-over-total 
            //overestimate uncertainty with the addition from twice counting of DCCT
            if(ti_d>0 && kot_f>0)
                ck.setIceFromTI(ti_d, dti_d, kot_f, dkot_f);
            
            ceV.addElement( ck );
        }
        if( lc_f > 0 ) { 
            CE cl=new CE();  
            cl.setValues( e_f , de_f , abe.Blf() , abe.DBlf() , ri_sy_d , dri_sy_d , lc_f , dlc_f, "l" );
            cl.setOriginalIGS(ri_alt_s, dri_alt_s);
            cl.setFracOverTotal(lot_f, dlot_f);
            if(ti_d>0 && lot_f>0)
                cl.setIceFromTI(ti_d, dti_d, lot_f, dlot_f);
            
            cl.setEGS(e_s, de_s);
            cl.setZ(nuc.Z());
            cl.setElement(nuc.EN());
            
            ceV.addElement( cl );
        }
        if( mc_f > 0 ) { 
            CE cm=new CE();  
            cm.setValues( e_f , de_f , abe.Bmf() , abe.DBmf() , ri_sy_d , dri_sy_d , mc_f , dmc_f, "m" );
            cm.setOriginalIGS(ri_alt_s, dri_alt_s);
            cm.setFracOverTotal(mot_f, dmot_f);
            if(ti_d>0 && mot_f>0)
                cm.setIceFromTI(ti_d, dti_d, mot_f, dmot_f);
            
            cm.setEGS(e_s, de_s);
            cm.setZ(nuc.Z());
            cm.setElement(nuc.EN());
            
            ceV.addElement( cm );
        }
        if( nc_f > 0 ) { 
            CE cn=new CE();  
            cn.setValues( e_f , de_f , abe.Bnf() , abe.DBnf() , ri_sy_d , dri_sy_d , nc_f , dnc_f, "n" );
            cn.setOriginalIGS(ri_alt_s, dri_alt_s);
            cn.setFracOverTotal(not_f, dnot_f);
            if(ti_d>0 && not_f>0)
                cn.setIceFromTI(ti_d, dti_d, not_f, dnot_f);
            
            cn.setEGS(e_s, de_s);
            cn.setZ(nuc.Z());
            cn.setElement(nuc.EN());
            
            ceV.addElement( cn );
        }
        if( npc_f > 0 && oc_f <=0 && pc_f <= 0) { 
            CE cnp=new CE();  
            float bnp=abe.Bnf()+abe.Bof()+abe.Bpf();
            float dbnp=(float) Math.sqrt(abe.DBnf()*abe.DBnf()+abe.DBof()*abe.DBof()+abe.DBpf()*abe.DBpf());
            cnp.setValues( e_f , de_f , bnp,dbnp , ri_sy_d , dri_sy_d , npc_f , dnpc_f, "np" );
            cnp.setOriginalIGS(ri_alt_s, dri_alt_s);
            cnp.setFracOverTotal(npot_f, dnpot_f);
            if(ti_d>0 && npot_f>0)
                cnp.setIceFromTI(ti_d, dti_d, npot_f, dnpot_f);
            
            cnp.setEGS(e_s, de_s);
            cnp.setZ(nuc.Z());
            cnp.setElement(nuc.EN());
            
            ceV.addElement( cnp );
        }
        if( oc_f > 0 && abe.Bof() > 0 ) { 
            CE co=new CE();  
            co.setValues( e_f , de_f , abe.Bof() , abe.DBof() , ri_sy_d , dri_sy_d , oc_f , doc_f, "o" );
            co.setOriginalIGS(ri_alt_s, dri_alt_s);
            co.setFracOverTotal(oot_f, doot_f);
            if(ti_d>0 && oot_f>0)
                co.setIceFromTI(ti_d, dti_d, oot_f, doot_f);
            
            co.setEGS(e_s, de_s);
            co.setZ(nuc.Z());
            co.setElement(nuc.EN());
            
            ceV.addElement( co );
        }
        if( pc_f > 0 && abe.Bpf() > 0 ) { 
            CE cp=new CE();  
            cp.setValues( e_f , de_f , abe.Bpf() , abe.DBpf() , ri_sy_d , dri_sy_d , pc_f , dpc_f, "p" );
            cp.setOriginalIGS(ri_alt_s, dri_alt_s);
            cp.setFracOverTotal(pot_f, dpot_f);
            if(ti_d>0 && pot_f>0)
                cp.setIceFromTI(ti_d, dti_d, pot_f, dpot_f);
            
            cp.setEGS(e_s, de_s);
            cp.setZ(nuc.Z());
            cp.setElement(nuc.EN());
            
            ceV.addElement( cp );
        }
        if( qc_f > 0 && abe.Bqf() > 0 ) { 
            CE cq=new CE();  
            cq.setValues( e_f , de_f , abe.Bqf() , abe.DBqf() , ri_sy_d , dri_sy_d , qc_f , dqc_f, "q" );
            cq.setOriginalIGS(ri_alt_s, dri_alt_s);
            cq.setFracOverTotal(qot_f, dqot_f);
            if(ti_d>0 && qot_f>0)
                cq.setIceFromTI(ti_d, dti_d, qot_f, dqot_f);
            
            cq.setEGS(e_s, de_s);
            cq.setZ(nuc.Z());
            cq.setElement(nuc.EN());
            
            ceV.addElement( cq );
        }
        
      }
        
    }
    if(  m_s.equals("E0") ){
        ABE abe=new ABE( nuc.Z() );        
        if( kot_f > 0 ){  
            CE ck=new CE();  
            ck.setValues( e_f , de_f , abe.Bkf() , abe.DBkf() , ti_sy_d , dti_sy_d , kot_f , dkot_f, "k" );
            ceV.addElement( ck );
//        System.out.println("E0: "+e_f +"*"+ de_f +"*"+ abe.Bkf() +"*"+tis_d +"*"+ dtis_d +"*"+ kot_f +"*"+dkot_f);
        }
//     System.out.println("ceV.size="+ceV.size() );

    }
  }


  public Nucleus nucleus(){return nuc;}
  
  public String EGRef() {return e_ref;}
  public void setEGRef(String ref) {e_ref=ref;}
  
  public String ES()
  { 
      return e_s.toLowerCase().replace("e", "E").replace("s(", "S(");
  }
  
  public boolean isGamma() {return true;}
  
  public String DES(){ return de_s;}
  public float EF(){ return e_f;}
  public float DEF(){ return de_f;}
  
  public float ERF(){ return er_f;}
  public float DERF(){ return der_f;}

  public float ECMF(){ return ecm_f;}
  public float DECMF(){ return decm_f;}

  public String EPlotS(){ return epl_s;} /* energy used in plotting */

  public String RIS(){ return ri_s;}
  public String DRIS(){ return dri_s;}
  
  public String altRIS() {return ri_alt_s;}
  public String altDRIS() {return dri_alt_s;}
  public double altRID(){ return ri_alt_d;}
  public double altDRID(){ return dri_alt_d;}
  
  public double RID(){ return ri_d;}
  public double DRID(){ return dri_d;}
  public void setRID(double d){ri_d=d;}
  public void setDRID(double d){dri_d=d;}
  
  public double syRID(){ return ri_sy_d;}
  public double DsyRID(){ return dri_sy_d;}
  public void setsyRID(double d){ri_sy_d=d;}
  public void setDsyRID(double d){dri_sy_d=d;}

  public void setAIS(String s){ ai_s=s;}
  public void setDAIS(String s){ dai_s=s;}
  public String AIS(){ return ai_s;}
  public String DAIS(){ return dai_s;}

  public void setAID(double d){ ai_d=d;}
  public void setDAID(double d){ dai_d=d;}
  public double AID(){ return ai_d;}
  public double DAID(){ return dai_d;}

  /* a lower limit for the absolute intensity uncertainty */

  public void setDAILLD(double d){ daill_d=d;}
  public double DAILLD(){ return daill_d;} 

  public void setRelBRS(String s){ rel_br_s=s;}
  public void setDRelBRS(String s){ drel_br_s=s;}
  public String RelBRS(){ return rel_br_s;}
  public String DRelBRS(){ return drel_br_s;}

  public void setRelBRD(double d){ rel_br_d=d;}
  public void setDRelBRD(double d){ drel_br_d=d;}
  public double RelBRD(){ return rel_br_d;}
  public double DRelBRD(){ return drel_br_d;}

  public void setBRD2Unity(double d){ br_d_2unity=d;}
  public void setDBRD2Unity(double d){ dtbr_d_2unity=d;}
  public double BRD2Unity(){ return br_d_2unity;}
  public double DBRD2Unity(){ return dtbr_d_2unity;}

  public void setRelTBRS(String s){ rel_tbr_s=s;}
  public void setDRelTBRS(String s){ drel_tbr_s=s;}
  public String RelTBRS(){ return rel_tbr_s;}
  public String DRelTBRS(){ return drel_tbr_s;}

  public void setRelTBRD(double d){ rel_tbr_d=d;}
  public void setDRelTBRD(double d){ drel_tbr_d=d;}
  public double RelTBRD(){ return rel_tbr_d;}
  public double DRelTBRD(){ return drel_tbr_d;}
  
  public String MS(){ return m_s;}
  public String altMS() {return alt_m_s;}//assumed MULT from deltaJPI
  public int L(){ return gj;}
  public int piI(){ return gp;}
  public void setMultipolarity(String s){m_s=s;}
  public void setAltMultipolarity(String s){alt_m_s=s;}
  
  public String MRS(){ return mr_s;}
  public String DMRS(){ return dmr_s;}

  public double TID(){ return ti_d;}
  public double DTID(){ return dti_d;}
  
  public double altTID(){ return ti_alt_d;}
  public double altDTID(){ return dti_alt_d;}
  
  public String TIS(){ return ti_s;}
  public String DTIS(){ return dti_s;}
  
  public String altTIS() {return ti_alt_s;}
  public String altDTIS() {return dti_alt_s;}
  
  public double syTID(){ return ti_sy_d;}
  public double DsyTID(){ return dti_sy_d;}

  public void setATID(double d){ ati_d=d;}
  public void setDATID(double d){ dati_d=d;}
  public double ATID(){ return ati_d;}
  public double DATID(){ return dati_d;}

  public void addFlag(String s){flag_s=flag_s+s;}
  public String flag(){ return flag_s;}
  public String iFlag(){ return iflag_s;}
  public String coinS(){ return coin_s;}
  public String q(){ return q_s;}
  
  public String ILS(){return level_s;}
  public void setILS(String s){level_s=s;}
  public int ILI(){return level_i;}
  public void setILI(int i){level_i=i;}

  public void setLevS(String s){level_s=s; }
  public void setLevI(int i){ level_i=i; }
   
  public String FLRecord(){return FL_record;}//for "FL=" record only
  public void setFLRecord(String s) {FL_record=s;hasFL=true;}
  
  public String FLS(){ return fl_s;  }
  
  public void setFLS(String s){fl_s=s;}
  public int FLI(){ return fl_i;}
  public void setFLI(int i){ fl_i=i;}
  
  public String JIS(){ return ji_s;  }
  public void setJIS(String s){ji_s=s;}
  
  public String JFS(){ return jf_s;  }
  public void setJFS(String s){jf_s=s;}
  
  public boolean hasFLRecord(){return hasFL;}  
  public boolean hasFlagRecord() {return hasFlagCR;}
  
  public String CCS(){ return cc_s;}
  public String DCCS(){ return dcc_s;}
  public float CCF(){ return cc_f;}
  public float DCCF(){ return dcc_f;}
  
  public String ECCS(){ return ecc_s;}
  public String DECCS(){ return decc_s;}
  public float ECCF(){ return ecc_f;}
  public float DECCF(){ return decc_f;}
  
  public void setDoseS(String s){ dose_s=s;}
  public void setDDoseS(String s){ ddose_s=s;}
  public String DoseS(){ return dose_s;}
  public String DDoseS(){ return ddose_s;}

  public void setDoseF(float f){ dose_f=f;}
  public void setDDoseF(float f){ ddose_f=f;}
  public float DoseF(){ return dose_f;}
  public float DDoseF(){ return ddose_f;}



  public String KCS(){ return kc_s;}
  public String DKCS(){ return dkc_s;}
  public float KCF(){ return kc_f;}
  public float DKCF(){ return dkc_f;}
  
  public String EKCS(){ return ekc_s;}
  public String DEKCS(){ return dekc_s;}
  public float EKCF(){ return ekc_f;}
  public float DEKCF(){ return dekc_f;}
  
  public String LCS(){ return lc_s;}
  public String DLCS(){ return dlc_s;}
  public float LCF(){ return lc_f;}
  public float DLCF(){ return dlc_f;}
  
  public String ELCS(){ return elc_s;}
  public String DELCS(){ return delc_s;}
  public float ELCF(){ return elc_f;}
  public float DELCF(){ return delc_f;}
  
  public float L1CF(){ return l1c_f;}
  public float DL1CF(){ return dl1c_f;}
  public float L2CF(){ return l2c_f;}
  public float DL2CF(){ return dl2c_f;}
  
  public float L3CF(){ return l3c_f;}
  public float DL3CF(){ return dl3c_f;}

  public String MCS(){ return mc_s;}
  public String DMCS(){ return dmc_s;}
  public float MCF(){ return mc_f;}
  public float DMCF(){ return dmc_f;}
  
  public String EMCS(){ return emc_s;}
  public String DEMCS(){ return demc_s;}
  public float EMCF(){ return emc_f;}
  public float DEMCF(){ return demc_f;}
  
  public String NCS(){ return nc_s;}
  public String DNCS(){ return dnc_s;}
  public float NCF(){ return nc_f;}
  public float DNCF(){ return dnc_f;}
  
  public String ENCS(){ return enc_s;}
  public String DENCS(){ return denc_s;}
  public float ENCF(){ return enc_f;}
  public float DENCF(){ return denc_f;}
  
  public String OCS(){ return oc_s;}
  public String DOCS(){ return doc_s;}
  public float OCF(){ return oc_f;}
  public float DOCF(){ return doc_f;}
  
  public String EOCS(){ return eoc_s;}
  public String DEOCS(){ return deoc_s;}
  public float EOCF(){ return eoc_f;}
  public float DEOCF(){ return deoc_f;}

  public String PCS(){ return pc_s;}
  public String DPCS(){ return dpc_s;}
  public float PCF(){ return pc_f;}
  public float DPCF(){ return dpc_f;}
  
  public String EPCS(){ return epc_s;}
  public String DEPCS(){ return depc_s;}
  public float EPCF(){ return epc_f;}
  public float DEPCF(){ return depc_f;}

  public String QCS(){ return qc_s;}
  public String DQCS(){ return dqc_s;}
  public float QCF(){ return qc_f;}
  public float DQCF(){ return dqc_f;}
  
  public String EQCS(){ return eqc_s;}
  public String DEQCS(){ return deqc_s;}
  public float EQCF(){ return eqc_f;}
  public float DEQCF(){ return deqc_f;}

  public String NPCS(){ return npc_s;}
  public String DNPCS(){ return dnpc_s;}
  public float NPCF(){ return npc_f;}
  public float DNPCF(){ return dnpc_f;}
  public String ENPCS(){ return enpc_s;}
  public String DENPCS(){ return denpc_s;}
  public float ENPCF(){ return enpc_f;}
  public float DENPCF(){ return denpc_f;}

  public String KOTS(){ return kot_s;}
  public String DKOTS(){ return dkot_s;}
  public float KOTF(){ return kot_f;}
  public float DKOTF(){ return dkot_f;}
  
  public String LOTS(){ return lot_s;}
  public String DLOTS(){ return dlot_s;}
  public float LOTF(){ return lot_f;}
  public float DLOTF(){ return dlot_f;}
  
  public String MOTS(){ return mot_s;}
  public String DMOTS(){ return dmot_s;}
  public float MOTF(){ return mot_f;}
  public float DMOTF(){ return dmot_f;}
  
  public String NOTS(){ return not_s;}
  public String DNOTS(){ return dnot_s;}
  public float NOTF(){ return not_f;}
  public float DNOTF(){ return dnot_f;}
  
  public String OOTS(){ return oot_s;}
  public String DOOTS(){ return doot_s;}
  public float OOTF(){ return oot_f;}
  public float DOOTF(){ return doot_f;}
  
  public String POTS(){ return pot_s;}
  public String DPOTS(){ return dpot_s;}
  public float POTF(){ return pot_f;}
  public float DPOTF(){ return dpot_f;}
  
  public String QOTS(){ return qot_s;}
  public String DQOTS(){ return dqot_s;}
  public float QOTF(){ return qot_f;}
  public float DQOTF(){ return dqot_f;}
  
  public String NPOTS(){ return npot_s;}
  public String DNPOTS(){ return dnpot_s;}
  public float NPOTF(){ return npot_f;}
  public float DNPOTF(){ return dnpot_f;}
  
  public String IGS() {return IG_s;}
  public String DIGS() {return dIG_s;}
  public float IG() {return IG_f;}
  public float DIG() {return dIG_f;}
  
  public void setKCF(float f1, float f2){ kc_f=f1; dkc_f=f2; resetCC=true;} 
  public void setLCF(float f1, float f2){ lc_f=f1; dlc_f=f2; resetCC=true;} 
  public void setL1CF(float f1, float f2){ l1c_f=f1; dl1c_f=f2;} 
  public void setL2CF(float f1, float f2){ l2c_f=f1; dl2c_f=f2;} 
  public void setL3CF(float f1, float f2){ l3c_f=f1; dl3c_f=f2;} 
  public void setMCF(float f1, float f2){ mc_f=f1; dmc_f=f2; resetCC=true;} 
  public void setNCF(float f1, float f2){ nc_f=f1; dnc_f=f2; resetCC=true;} 
  public void setOCF(float f1, float f2){ oc_f=f1; doc_f=f2; resetCC=true;} 
  public void setPCF(float f1, float f2){ pc_f=f1; dpc_f=f2; resetCC=true;} 
  public void setQCF(float f1, float f2){ qc_f=f1; dqc_f=f2; resetCC=true;} 

  
  
  public void setEF(float ef,float def){e_f=ef;de_f=def;}
  public void setERF(float erf,float derf){er_f=erf;der_f=derf;}
  
  public int nConvElecs(){ return ceV.size();}
  public CE convElecAt(int k){return (CE) ceV.elementAt(k);}
  public Vector<CE> convElecV(){return ceV;}


  
  public String BE1S(){ return be1_s;}
  public String DBE1S(){ return dbe1_s;}

  public String BE2S(){ return be2_s;}
  public String DBE2S(){ return dbe2_s;}

  public String BE3S(){ return be3_s;}
  public String DBE3S(){ return dbe3_s;}

  public String BE4S(){ return be4_s;}
  public String DBE4S(){ return dbe4_s;}

  public String BE5S(){ return be5_s;}
  public String DBE5S(){ return dbe5_s;}
  
  public String BE6S(){ return be6_s;}
  public String DBE6S(){ return dbe6_s;}

  public String BE1WS(){ return be1w_s;}
  public String DBE1WS(){ return dbe1w_s;}

  public String BE2WS(){ return be2w_s;}
  public String DBE2WS(){ return dbe2w_s;}

  public String BE3WS(){ return be3w_s;}
  public String DBE3WS(){ return dbe3w_s;}

  public String BE4WS(){ return be4w_s;}
  public String DBE4WS(){ return dbe4w_s;}

  public String BE5WS(){ return be5w_s;}
  public String DBE5WS(){ return dbe5w_s;}
  
  public String BE6WS(){ return be6w_s;}
  public String DBE6WS(){ return dbe6w_s;}

  public String BM1S(){ return bm1_s;}
  public String DBM1S(){ return dbm1_s;}

  public String BM2S(){ return bm2_s;}
  public String DBM2S(){ return dbm2_s;}

  public String BM3S(){ return bm3_s;}
  public String DBM3S(){ return dbm3_s;}

  public String BM4S(){ return bm4_s;}
  public String DBM4S(){ return dbm4_s;}

  public String BM5S(){ return bm5_s;}
  public String DBM5S(){ return dbm5_s;}

  public String BM6S(){ return bm6_s;}
  public String DBM6S(){ return dbm6_s;}
  
  public String BM1WS(){ return bm1w_s;}
  public String DBM1WS(){ return dbm1w_s;}

  public String BM2WS(){ return bm2w_s;}
  public String DBM2WS(){ return dbm2w_s;}

  public String BM3WS(){ return bm3w_s;}
  public String DBM3WS(){ return dbm3w_s;}

  public String BM4WS(){ return bm4w_s;}
  public String DBM4WS(){ return dbm4w_s;}

  public String BM5WS(){ return bm5w_s;}
  public String DBM5WS(){ return dbm5w_s;}

  public String BM6WS(){ return bm6w_s;}
  public String DBM6WS(){ return dbm6w_s;}
  
  public String DCOS(){ return dco_s;}
  public String DDCOS(){ return ddco_s;}
  
  public String DCO1S(){ return dco1_s;}
  public String DDCO1S(){ return ddco1_s;}
  
  public String DCO2S(){ return dco2_s;}
  public String DDCO2S(){ return ddco2_s;}
  
  public String DCO3S(){ return dco3_s;}
  public String DDCO3S(){ return ddco3_s;}
  
  public String POLS(){ return pol_s;}
  public String DPOLS(){ return pol_s;}
  
  public String A2S(){return a2_s;}
  public String DA2S(){return da2_s;}
  public String A2Ref(){return a2_ref;} 
  
  public String A4S(){return a4_s;}
  public String DA4S(){return da4_s;}
  public String A4Ref(){return a4_ref;} 
  
  public String A6S(){return a6_s;}
  public String DA6S(){return da6_s;}
  public String A6Ref(){return a6_ref;} 
  
  public int nGamLinks(){ return  gamlinkV.size(); }
  public int gamLinkAt(int i){ return ( (Integer) gamlinkV.elementAt(i) ).intValue(); }
  public void addGamLink(int i){ gamlinkV.addElement( new Integer(i));}

  public void setRevGamLink(int i){ revgamlink=i;}
  public int revGamLink(){return revgamlink;}

  public void setWarning(String s){ warning=warning+s;}
  public String warning(){return warning;}

  public void setRBRS(String s){ rbr_s=s;}
  public void setDRBRS(String s){ drbr_s=s;}
  public String RBRS(){ return rbr_s;}
  public String DRBRS(){ return drbr_s;}

  public void setRES(String s){ re_s=s;}
  public void setDRES(String s){ dre_s=s;}
  public String RES(){ return re_s;}
  public String DRES(){ return dre_s;}

  public void setXIPlot(float f){ xIPlot=f; isPlotted=true;}
  public void setXFPlot(float f){ xFPlot=f; isPlotted=true;}
  public void setYIPlot(float f){ yIPlot=f; isPlotted=true;}
  public void setYFPlot(float f){ yFPlot=f; isPlotted=true;}

  public boolean isPlotted(){ return isPlotted;}
  public float xIPlot(){ return xIPlot;}
  public float xFPlot(){ return xFPlot;}
  public float yIPlot(){ return yIPlot;}
  public float yFPlot(){ return yFPlot;}

  public void setOrder(int i){order=i;}
  public int getOrder(){return order;}


  public boolean isPrimary() {return isPrimary;}
  public void setIsPrimary(boolean b) {isPrimary=b;}

  
  
  public String icc(){
    contRecord=""; 
    builContRec( "CC",cc_symbol,cc_s,"",dcc_s,"");
    builContRec( "KC",kc_symbol,kc_s,"",dkc_s,"");
    builContRec( "LC",lc_symbol,lc_s,"",dlc_s,"");
    builContRec( "MC",mc_symbol,mc_s,"",dmc_s,"");
    builContRec( "NC",nc_symbol,nc_s,"",dnc_s,"");
    builContRec( "OC",oc_symbol,oc_s,"",doc_s,"");
    builContRec( "PC",pc_symbol,pc_s,"",dpc_s,"");
    builContRec( "QC",qc_symbol,qc_s,"",dqc_s,"");
    builContRec( "NC+",npc_symbol,npc_s,"",dnpc_s,"");
    return contRecord;
  }


  public String beml(){

    contRecord=""; 
    builContRec( "BE1",be1_symbol,be1_s,"",dbe1_s,be1_ref);
    builContRec( "BE2",be2_symbol,be2_s,"",dbe2_s,be2_ref);
    builContRec( "BE3",be3_symbol,be3_s,"",dbe3_s,be3_ref);
    builContRec( "BE4",be4_symbol,be4_s,"",dbe4_s,be4_ref);
    builContRec( "BE5",be5_symbol,be5_s,"",dbe5_s,be5_ref);
    builContRec( "BE6",be6_symbol,be6_s,"",dbe6_s,be6_ref);

    builContRec( "BE1W",be1w_symbol,be1w_s,"",dbe1w_s,be1w_ref);
    builContRec( "BE2W",be2w_symbol,be2w_s,"",dbe2w_s,be2w_ref);
    builContRec( "BE3W",be3w_symbol,be3w_s,"",dbe3w_s,be3w_ref);
    builContRec( "BE4W",be4w_symbol,be4w_s,"",dbe4w_s,be4w_ref);
    builContRec( "BE5W",be5w_symbol,be5w_s,"",dbe5w_s,be5w_ref);
    builContRec( "BE6W",be6w_symbol,be6w_s,"",dbe6w_s,be6w_ref);

    builContRec( "BM1",bm1_symbol,bm1_s,"",dbm1_s,bm1_ref);
    builContRec( "BM2",bm2_symbol,bm2_s,"",dbm2_s,bm2_ref);
    builContRec( "BM3",bm3_symbol,bm3_s,"",dbm3_s,bm3_ref);
    builContRec( "BM4",bm4_symbol,bm4_s,"",dbm4_s,bm4_ref);
    builContRec( "BM5",bm5_symbol,bm5_s,"",dbm5_s,bm5_ref);
    builContRec( "BM6",bm6_symbol,bm6_s,"",dbm6_s,bm6_ref);

    builContRec( "BM1W",bm1w_symbol,bm1w_s,"",dbm1w_s,bm1w_ref);
    builContRec( "BM2W",bm2w_symbol,bm2w_s,"",dbm2w_s,bm2w_ref);
    builContRec( "BM3W",bm3w_symbol,bm3w_s,"",dbm3w_s,bm3w_ref);
    builContRec( "BM4W",bm4w_symbol,bm4w_s,"",dbm4w_s,bm4w_ref);
    builContRec( "BM5W",bm5w_symbol,bm5w_s,"",dbm5w_s,bm5w_ref);
    builContRec( "BM6W",bm6w_symbol,bm6w_s,"",dbm6w_s,bm6w_ref);

    return contRecord.trim();
  }


  
  public String ogcr(){
    contRecord="";
    builContRec( "ECC",ecc_symbol,ecc_s,"",decc_s,ecc_ref);
    builContRec( "EKC",ekc_symbol,ekc_s,"",dekc_s,ekc_ref);
    builContRec( "ELC",elc_symbol,elc_s,"",delc_s,elc_ref);
    builContRec( "EMC",emc_symbol,emc_s,"",demc_s,emc_ref);
    builContRec( "ENC",enc_symbol,enc_s,"",denc_s,enc_ref);
    builContRec( "EOC",eoc_symbol,eoc_s,"",deoc_s,eoc_ref);
    builContRec( "EPC",epc_symbol,epc_s,"",depc_s,epc_ref);
    builContRec( "EQC",eqc_symbol,eqc_s,"",deqc_s,enc_ref);

    builContRec( "K/T",kot_symbol,kot_s,"",dkot_s,"");
    builContRec( "L/T",lot_symbol,lot_s,"",dlot_s,"");
    builContRec( "M/T",mot_symbol,mot_s,"",dmot_s,"");
    builContRec( "N/T",not_symbol,not_s,"",dnot_s,"");
    builContRec( "O/T",oot_symbol,oot_s,"",doot_s,"");
    builContRec( "P/T",pot_symbol,pot_s,"",dpot_s,"");
    builContRec( "Q/T",qot_symbol,qot_s,"",dqot_s,"");
    builContRec( "NP/T",npot_symbol,npot_s,"",dnpot_s,"");

    builContRec( "DCO",dco_symbol,dco_s,"",ddco_s,dco_ref);
    builContRec( "DCO1",dco1_symbol,dco1_s,"",ddco1_s,dco1_ref);
    builContRec( "DCO2",dco2_symbol,dco2_s,"",ddco2_s,dco2_ref);
    builContRec( "DCO3",dco3_symbol,dco3_s,"",ddco3_s,dco3_ref);
    
    builContRec( "POL",pol_symbol,pol_s,"",dpol_s,pol_ref);
    
    return contRecord;
  }

  private void builContRec(String sName, String sSymbol, String sValue, String sUnits, String sUnc, String sRef){

    if( sName.trim().length() > 0 && sValue.trim().length() > 0 ){

         if( contRecord.length() > 0 ) contRecord=contRecord+"$";

         contRecord=contRecord+sName;
         if( sSymbol.trim().equals("=") ) contRecord=contRecord+sSymbol.trim()+sValue;
         else contRecord=contRecord+" "+sSymbol.trim()+" "+sValue;
         if( sUnits.length() > 0 ) contRecord=contRecord+" "+sUnits;
         if( sUnc.length() > 0 ) contRecord=contRecord+" "+sUnc;
         if( sRef.length() > 0 ) contRecord=contRecord+" ("+sRef+") ";
    }

  }


}