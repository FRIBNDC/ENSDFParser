 package ensdfparser.ensdf;

/*
   ENSDF class
   It makes use of a large number of auxiliary classes
   Stores all of the data in a dataset (one single dataset)
 * All of the other files in the ENSDF package are used by this object to get the data
 *                         
*/

import java.util.*;

import ensdfparser.calc.UncertaintyParser;
import ensdfparser.ensdf.Atomic.AtomicData;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

//last update: 02/07/2022 by Jun Chen

public class ENSDF{

	
  /** a referece to parent mass chain*/
  protected MassChain parentMassChain=null;
	
  protected int id=-1;
  
  protected boolean silent=false;
  
  protected Vector<String> lines=new Vector<String>();
  protected Vector<Integer> poslev= new Vector<Integer>();  /* level positions */
  
  protected Vector <Level> levelsV= new Vector<Level>(); /* Vector containing instances of the Level class */
  protected Vector<Gamma> gammasV= new Vector<Gamma>();
  
  protected Vector<Beta>  betasV  = new Vector<>();
  protected Vector<ECBP>  ecbpsV  = new Vector<>();
  protected Vector<Alpha> alphasV = new Vector<>();
  protected Vector<DParticle> dpsV = new Vector<>();
  
  //In some cases with levels like 1234.5+X, 1346.8+Y, those levels (ERPF) could be not in order of energies
  //sortedLevelsV stores all levels ordered by ERPF()
  protected Vector <Level> sortedLevelsV= new Vector<Level>(); 
  
  protected Vector<String> bandTagV=new Vector<String>(); /* Vector with the band tags */
  protected Vector<String> bandCommentV=new Vector<String>(); /* Vector with the band comments */
  protected Vector<Band> bandV=new Vector<Band>(); /* Vector with the band instances */

  protected Vector<String> lline=new Vector<String>(); /* a reusable Vector */

  protected Vector<String> xrefOldTagsV= new Vector<String>();
  protected Vector<String> xrefTagsV= new Vector<String>();
  protected Vector<String> xrefDSIDsV= new Vector<String>();
  protected Vector<XRef> xrefsV= new Vector<XRef>();
 
  protected Atomic atomicRad=new Atomic();
  
  protected Vector<Parent> parentsV=new Vector<Parent>(); /* Vector containing instances of the Parent class */

  protected Vector<Integer> poscom=new Vector<Integer>(); /* comment positions */
  protected Vector<Comment> comV=new Vector<Comment>(); /* Vector containing instances of the Comment class */
  protected Vector<Comment> docV=new Vector<Comment>();
  
  protected HashMap<String,String> labelMap=new HashMap<String,String>(); /* map for relabeled fields */
  
  protected int njlev;        /* Total Number of levels with a non-zero JPI value */
  protected int ntlev;        /* Total Number of levels with a non-zero T12 value */
  protected int nllev;        /* Total Number of levels with a non-empty L-transfer */
  protected int nclev;        /* Total Number of levels comments */
  protected int ncrlev;        /* Total Number of levels continuation records */
  protected int ndmlev;        /* Total Number of levels with a non-gamma decay mode */
  protected int nNonBandLevels; /* Total Number of levels that don't belong to a band */

  protected int llbl; /* last line before levels */
  protected int furl; /* first unassigned radiation line */

  protected int ntotgam; /* Total number of gamma rays */
  protected int nigam; /* Total number of gamma rays with non-zero intensity */
  protected int ntigam; /* Total number of gamma rays with non-zero total intensity */
  protected int nmgam; /* Total number of gamma rays with non-zero multipolarity */
  protected int nmrgam; /* Total number of gamma rays with non-zero mixing ratio */
  protected int nccgam; /* Total number of gamma rays with non-zero convesion coefficient */
  protected int nnzflgam; /* Total number of gamma rays with non-zero final level */
  protected int nflgam; /* Total number of gamma rays with final level */
  protected int ngsgam; /* Total number of gamma rays to g.s. */
  protected int nmaxgaml; /* maximum number of gammas per level */
  protected int ncgam; /* Total number of gamma rays comments */
  protected int ncrgam; /* Total number of gamma rays continuation records */
  protected int ntotgamp; /* Total number of gamma rays paths */

  protected int ntotalp=0; /* Total number of Alpha particles */
  protected int nflalp;/* Total number of alphas with non-zero daughter level */
  protected int nialp; /* Total number of alphas with non-zero intensity */
  protected int nhfalp; /* Total number of alphas with non-zero hindrance factors */
  protected int ncalp; /* Total number of alphas comments */
  protected int ncralp; /* Total number of alphas continuation records */

  protected int ntotbet=0; /* Total number of betam particles */
  protected int nflbet;/* Total number of betas with non-zero daughter level */
  protected int nibet; /* Total number of betas with non-zero intensity */
  protected int nlbet; /* Total number of betas with non-zero logfts */
  protected int ncbet; /* Total number of betas comments */
  protected int ncrbet; /* Total number of betas continuation records */

  protected int ntotecbp=0; /* Total number of ecbps */
  protected int nflecbp;/* Total number of ecbps with non-zero daughter level */
  protected int nitecbp; /* Total number of ecbps with non-zero total intensity */
  protected int nieecbp; /* Total number of ecbps with non-zero ec intensity */
  protected int nibecbp; /* Total number of ecbps with non-zero bp intensity */
  protected int nlecbp; /* Total number of ecbps with non-zero logfts */
  protected int ncecbp; /* Total number of ecbps comments */
  protected int ncrecbp; /* Total number of ecbps continuation records */

  protected int ntotdp;  /* Total number of (delayed) particles */
  protected int nedp; /* Total number of (delayed) particles with non-zero energy, also for cases where no energy given */
  protected int nidp; /* Total number of (delayed) particles with non-zero intensity */
  protected int neidp; /* Total number of (delayed) particles with non-zero intermediate energy */
  protected int ntdp; /* Total number of (delayed) particles with non-zero width */
  protected int nldp; /* Total number of (delayed) particles with non-zero transferred angular momentum */
  protected int ncdp; /* Total number of (delayed) particles comments */
  protected int ncrdp; /* Total number of (delayed) particles continuation records */

  
  protected Nucleus target=new Nucleus();
  
  protected Nucleus nucleus=new Nucleus();
  protected Nucleus midNucleus=new Nucleus();//intermediate nucleus in delayed-particle dataset
  
  protected QValue qv=new QValue();
  protected Normal norm=new Normal();
 
  protected Vector<Normal> normsV=new Vector<Normal>();
  
  protected HashMap<String,String> offsetMap0=new HashMap<String,String>();//offset of ERPF for levels, like "X+1234.5", "Y+2345.6"
  
  protected HashMap<String,SDS2XDX> labelOffsetMap=new HashMap<String,SDS2XDX>();//offset of ERPF for levels, like "X+1234.5", "Y+2345.6"
  
  protected boolean hasLevelXref=false;//there are cases in which no level XREFs in Adopted Levels table

  protected String dsid_s="", dsRef_s="", dsPub_s="", dsDate_s="", dsType_s=""; 
  
  protected String dsid0_s="";//holding dsid part in the first line
  
  protected String decayTypeInDSID="";
  
  protected String decayRecordType="";//column 7-8 
  
  protected String bp_me_s="",bp_dme_s="";

 
  /* RadList variables */

  protected boolean canDoRadList;
  protected Vector<RadList> rlV=new Vector<RadList>();      /* Vector containing instances of the RadList class */
  protected int nxr=6;
  protected String xrtype[]=new String[nxr];

  protected Sum egSum;
  protected Sum eceSum;
  protected Sum exrSum;
  protected Sum eaSum;
  protected Sum eeSum;
  protected Sum epSum;
  @SuppressWarnings("unused")
protected Sum enSum;
  protected Sum eauSum;
  protected Sum eneutrinoSum;
  protected Sum erecoilSum;

  protected Sum elpSum;
  protected Sum eemSum;
  protected Sum ehpSum;

  protected Sum ebmSum;

  protected Sum ebpSum;

  @SuppressWarnings("unused")
protected double qEffective, dQEffective, missingEnergy, dMissingEnergy, missingEnergyRatio;
  protected Sum totalEnergySum;

  protected float missEnergyRatio;

  protected String bm_me_s, bm_dme_s;

  protected Sum giSum=new Sum();
  protected Sum gaiSum=new Sum();
  protected Sum gtiSum=new Sum();
  protected Sum gatiSum=new Sum();

  protected Sum biSum=new Sum();
  protected Sum baiSum=new Sum();

  protected Sum aiSum=new Sum();
  protected Sum aaiSum=new Sum();

  protected Sum ecbpiSum=new Sum();
  protected Sum ecbpaiSum=new Sum();

  protected Sum dpiSum=new Sum();
  protected Sum dpaiSum=new Sum();
 
  protected float totalDose;
  protected float dtotalDose;
  protected int nRadTotalDose;

  protected boolean lightEds; 
  
  protected String levelFlags="",gammaFlags="",decayFlags="",delayFlags="",xrayFlags="",augerFlags="";
  
  protected StringBuilder errorMsg=new StringBuilder();

/* The unassigned radiation */

  protected Vector<Integer> posugV= new Vector<Integer>();  /* unassigned gamma positions */
  protected Vector<Integer> posubV= new Vector<Integer>();  /* unassigned beta positions */
  protected Vector<Integer> posueV= new Vector<Integer>();  /* unassigned ec positions */
  protected Vector<Integer> posuaV= new Vector<Integer>();  /* unassigned a positions */
  protected Vector<Integer> posudpV= new Vector<Integer>();  /* unassigned dparticle positions */
  
  protected Vector<Gamma> ugV=new Vector<Gamma>(); /* Vector containing instances of the gamma class */
  protected Vector<Beta> ubV=new Vector<Beta>(); /* Vector containing instances of the beta class */
  protected Vector<ECBP> ueV=new Vector<ECBP>(); /* Vector containing instances of the ecbp class */
  protected Vector<Alpha> uaV=new Vector<Alpha>(); /* Vector containing instances of the alpha class */
  protected Vector<DParticle> udpV=new Vector<DParticle>(); /* Vector containing instances of the dparticle class */
  protected String delayType;
  protected String particleType;
  protected String promptOrDelayMarker;
  protected int DPNumber=0;/*number of emitted particles in delayed-particle decays, like B-N, number=1; B-2N number=2

/* History Vector */

  protected Vector<History> hisV=new Vector<History>();
  protected Vector<Integer> poshis=new Vector<Integer>();


/* Gamma Paths Vector: */

  protected Vector<TableRecord> tableV=new Vector<TableRecord>();

  protected boolean sysOut=true;
  
  //true if TI can be obtained for all gamma rays, either from TI record or from RI(1+CC)
  //not including unplaced gammas and those gammas that don't have TI or RI
  //this is used for finding the implicit PN option
  protected boolean canGetAllTI=true;

/* ---------------------------------------------------- */

  public ENSDF()  { 

    //System.gc();
    lines.clear();
    poslev.clear();
    levelsV.clear();
    bandTagV.clear();
    parentsV.clear();
    normsV.clear();
    rlV.clear();

    hasLevelXref=false;
    
    lightEds=false;
    hisV.clear();
    comV.clear();
    
    offsetMap0.clear();
    labelOffsetMap.clear();
    
    xrtype[0]="ka1";xrtype[1]="ka2";xrtype[2]="kb1";xrtype[3]="kb2";xrtype[4]="kb3"; xrtype[5]="l";

    errorMsg.setLength(0);
  }

  public void clear()  { 

    System.gc();
    lines.clear();
    poslev.clear();
    levelsV.clear();
    gammasV.clear();
    betasV.clear();
    ecbpsV.clear();
    alphasV.clear();
    dpsV.clear();
    
    bandTagV.clear();
    parentsV.clear();
    normsV.clear();
    offsetMap0.clear();
    labelOffsetMap.clear();
    rlV.clear();

    hasLevelXref=false;
    
    lightEds=false;
    comV.clear();
    
    errorMsg.setLength(0);

  }

/* -------------------------- set methods, transferring the data -------------------------- */

  public void setLight(){ lightEds=true; }

  public void setValues( int n , String [] a) throws Exception {

/* transfer the array: */

    lines.clear();
    for(int counter=1; counter<= n; counter++){ lines.addElement( a[counter] ); }
    if( lines.size() > 0 ) doWork();
  }

  /** 
   * Parse lines of ENSDF dataset into objects that deal with these lines.
   * option: "HEADER": parse header information only
   *         "LITTLE": parse header and some basic information, see in ENSDF.java: setValues()
   *         ""     :  parse all information (default)
   */
  public void setValues( Vector<String> v,String option) throws Exception {
      String s=option.toUpperCase();
      if(s.isEmpty())
          setValues(v);   
      else if(s.equals("HEADER"))
          setValuesHeaderOnly(v);
      else if(s.equals("LITTLE"))
          this.setLittleValues(v);
      else if(s.equals("LITTLELITTLE"))
          this.setLittleLittleValues(v);
      else if(s.equals("COMMENT"))
          this.setValuesCommentOnly(v);
      else 
          this.setLittleValues(v);

  }

  public void setValues( Vector<String> v) throws Exception {
      /* transfer the vector: */

        lines.clear();
        lines.addAll(v); 
        if( lines.size() > 0 ) doWork();
  }
  
  /*when setLittleValues has been run and the ENSDF object already has the lines,
   * run this to get the rest of the data */
 public void setValues()throws Exception{
     doWork();
 } 
 
  /*some systems don't have enough memory to hold all the data for 50+ datasets
   * we can use this object to make a little ensdf object to hold all the data, and not
   * make a big object until we're about to print
   */
  public void setLittleValues(Vector<String> v)throws Exception{
      lines.clear();
      lines.addAll(v);
      if(lines.size()>0) doLittleWork();
  }
  
  
  private void doLittleWork()throws Exception{
      //sysOut=false;
      
      fixLines();
      nucleus.parseNUCID( ( (String) lines.elementAt(0) ).substring(0,5) );
        dsid_s=( (String) lines.elementAt(0) ).substring(9,39).trim();
        
        dsid0_s=dsid_s.substring(0);
        llbl=lines.size();
        
       //LG();
       
       parseQValues();
       parseHeader();
       parseParent();

  }
  
  public void setValuesCommentOnly(Vector<String> v) throws Exception{
      lines.clear();
      lines.addAll(v);
      if(lines.size()>0) 
          doWorkCommentOnly();
  }
  
  private void doWorkCommentOnly()throws Exception{
      fixLines();

      nucleus.parseNUCID( ( (String) lines.elementAt(0) ).substring(0,5) );

      dsid_s=( (String) lines.elementAt(0) ).substring(9,39).trim();

      dsid0_s=( (String) lines.elementAt(0) ).substring(9,39).trim();
      
      parseHeader();
      
      comV.clear();

      Vector<String> clines=new Vector<String>();
      String line="";
      char c5=0,c6=0;
      char currCType=0,prevCType=0;
      for(int i=1;i<lines.size();i++) {
          line=lines.get(i);
          c6=line.toUpperCase().charAt(6);
          
          if(line.length()<10 || c6!='C')
              continue;
          
          c5=line.charAt(5);
          currCType=line.charAt(7);
          
          if(currCType!=prevCType || c5==' ') {
              if(!clines.isEmpty()) {
                  Comment c=new Comment();
                  c.setValues(clines);
                  comV.add(c);
                  
                  //DO NOT use clines.clear(), it will then also clear lines 
                  //in the comment since they point to the same reference
                  clines=new Vector<String>();
              }
          }
          
          clines.add(line);
          prevCType=currCType;        
      }
      
      if(!clines.isEmpty()) {
          Comment c=new Comment();
          c.setValues(clines);
          comV.add(c);

      }
  }
  
  public void setValuesHeaderOnly(Vector<String> v)throws Exception{
      lines.clear();
      lines.addAll(v);
      if(lines.size()>0) doWorkHeaderOnly();
  }
  
  private void doWorkHeaderOnly()throws Exception{
      fixLines();

      nucleus.parseNUCID( ( (String) lines.elementAt(0) ).substring(0,5) );

      dsid_s=( (String) lines.elementAt(0) ).substring(9,39).trim();

      dsid0_s=( (String) lines.elementAt(0) ).substring(9,39).trim();
      
      parseHeader();
  }
  
  public void setLittleLittleValues(Vector<String> v){
      lines.clear();
      lines.addAll(v);
      if(lines.size()>0) 
          doLittleLittleWork();
  }
  
  private void doLittleLittleWork(){
      fixLines();
      
      llbl=lines.size();
      TableRecords();
  }
 
  
/* -------------------------- Doing the Work -------------------------- */

  private void doWork()throws Exception{

   sysOut=false;

   if(sysOut) System.out.println( "Begin to do the work, size="+lines.size());

   fixLines();

    /* Nucleus */

   nucleus.parseNUCID( ( (String) lines.elementAt(0) ).substring(0,5) );

   if(sysOut) System.out.println("Eds nucleus: "+nucleus.nameTUC()+" DSID="+( (String) lines.elementAt(0) ).substring(9,39).trim()+" size="+lines.size() );


    levelFlags="";
    gammaFlags="";
    decayFlags="";
    delayFlags="";
    xrayFlags="";
    augerFlags="";

    /* parsing Q record lines: */
    parseQValues();//S(n) is needed for calculating level energy of E(level) given as SN+0.123
    
    if(sysOut) System.out.println("Done with QV" );
    
    /* parsing levels and gammas: */
    parseLevelsAndGammas();

    if(sysOut) System.out.println( "Done with the levels, number="+nLevels() );

    /* The last line before levels (llbl) */

    llbl=lines.size();
    if( poslev.size() > 0 ) llbl= ( (Integer) poslev.elementAt(0) ).intValue() ;
   
    
    //must be after llbl is determined
    parseHeader();//dsid, dsid0, decayTypeInDSID, etc, are assigned here
    if(sysOut) System.out.println("Done with Authors" );
    
    parseAllXREFs(); //including parsing XREF summary and XREF of each level in Adopted dataset
    if(sysOut) System.out.println("Done with Xrefs" );
    
    
    /* Calculating the final level for all gammas: */

    findFinalLevelsForGammas();//need XREF in this call
  
    //atomic data
    AtomicRad();
    
    if(sysOut) System.out.println( "Done with the gamma final levels");
   
    //called before calculateControlVariables
    parseUnplacedRadiations();  
    
    if(sysOut)   System.out.println( "Done with unplaced radiation ");
    
        
    calculateControlVariables();
    
    if(sysOut)   System.out.println( "Done calculating a bunch of useful control variables");

   
    ReLabels();   
  
    parseNorm();
   
    if(sysOut) System.out.println("Done with N" );

   
    /* comments call must come after Normalization and unplaced radiations */
    parseComments();
   
    if(sysOut) System.out.println("Done with Comments" );

    //for(Comment c:this.comV)
    //	System.out.println(c.type()+" "+c.body());
   
    /* Bands call must come after comments */
    parseBands();
   
    if(sysOut) System.out.println("Done with Bands" );

   if(sysOut) System.out.println("Done with levels" );

   checkGamma();
   if(sysOut) System.out.println("Done with GammaCheck" );
   
   checkJPIOrder();
   if(sysOut) System.out.println("Done with JPiOrder" );
   
   parseParent();//must be called after parsing levels
   if(sysOut)  System.out.println("Done with P" );

   TableRecords();
   if(sysOut) System.out.println("Done with Tables" );


   orderRadiations();
   if(sysOut) System.out.println("Done with orderRad" );

      
/* clearing the vectors to save ram */

//   lines.clear();
//   poslev.clear();

   
   //parse uncertainty for the intensity (usually =100 with no unc) of the record used for intensity normalization

   //for gamma intensities
   String os=norm.OS();
   if(this.nGamWI()>0 && !os.equals("7")) {	
	   Vector<Gamma> tempV=new Vector<Gamma>();
	   if(os.equals("6")) {//branching ratio of each level
		   for(Level lev:this.levelsV) {
			   Gamma gam=UncertaintyParser.findAndSetDRIOfNormRecord(lev.gamV,true);
			   
			   //if altDRIS="0" is set for the return gamma
			   if(gam!=null && gam.altDRIS100.equals("0"))
				   gam.setDRID(0);
			   
		   }
	   }else {
		   tempV.addAll(gammasV);
		   tempV.removeAll(ugV);
		   
		   Gamma gam=UncertaintyParser.findAndSetDRIOfNormRecord(tempV,true);
		   
		   if(gam!=null && gam.altDRIS100.equals("0"))
			   gam.setDRID(0);
	   }

   }
   
   //particle intensities
   
   
   if(this.nAlpWI()>0) {//alphas
	   Vector<Alpha> tempV=new Vector<Alpha>();
	   
	   //tempV.addAll(this.unpAlphas());
	   
	   for(Level lev:this.levelsV) {
		   tempV.addAll(lev.alpV);		   
	   }
	   
	   
	   Alpha alp=UncertaintyParser.findAndSetDRIOfNormRecord(tempV,true);
	   
	   //if altDRIS="0" is set for the return gamma
	   if(alp!=null && alp.altDRIS100.equals("0"))
		   alp.setDID(0);
   }else if(this.nDPWI()>0) {//delayed particles
	   Vector<DParticle> tempV=new Vector<DParticle>();
	   
	   //tempV.addAll(this.unpDParticles());
	   
	   for(Level lev:this.levelsV) {
		   tempV.addAll(lev.dpV);		   
	   }
	   
	   DParticle dp=UncertaintyParser.findAndSetDRIOfNormRecord(tempV,true);
	   
	   //if altDRIS="0" is set for the return gamma
	   if(dp!=null && dp.altDRIS100.equals("0"))
		   dp.setDID(0);
   }
   
   //find ref (NSR keynumber) for each gamma EG
   parseEGRef();
   
/* End of doWork */
     if(sysOut) System.out.println("Done with ENSDF" );
     
     if(errorMsg.length()>0 && !silent){
         String NUCID=nucleus.A()+nucleus.En();
         errorMsg.insert(0,"In dataset "+NUCID+": "+dsid0_s+"\n");
         
         System.out.println(errorMsg);
     }
     
  }
  
  //for simple comment like "From 1981Ba40,  "
  //this doesn't parse complicated comment like "Levels up to 5050 from 1981Ba40; above that from 1979He18"
  @SuppressWarnings("unchecked")
protected void parseEGRef() {
	  try {
		  String commonRef="";//from top comments
		  HashMap<String,String> flagRefMap=new HashMap<String,String>();
		  
		  String line="", s="", tempRef="";
		  
		  //System.out.println(comV.size());
		  for(Comment c:this.comV) {
			  
			  /*
			  if(c.headV().size()>0) 
				  System.out.println(c.type()+"$"+c.headV().get(0)+"#####"+c.body());
			  else
				  System.out.println(c.type()+"$head="+c.head()+"####"+c.body());
			  */
			  
			  if(!c.type().equals("G ") || !c.headV().contains("E")) 
				  continue;

			  line=Str.firstSentence(c.body()).toUpperCase().trim();


			  //System.out.println(line);
			  //System.out.println(c.type()+"$head="+c.head()+"####"+c.body());
			  
			  int headIndex=c.headV().indexOf("E");
			  Vector<String> flagsV=c.flagVV().get(headIndex);

			  int n=line.indexOf("FROM");
			  if(n==0){
				  s=line.substring(n+5).trim();
				  if(s.length()>=8 && EnsdfUtil.isKeynumber(s.substring(0,8))) {
					  tempRef=s.substring(0,8);
					  if(flagsV.size()==0) {
						  commonRef=tempRef;
					  }else {
						  for(String flag:flagsV) {
							  if(!flagRefMap.containsKey(flag))
								  flagRefMap.put(flag, tempRef);
						  }
					  }
				  }
			  }
		  }
		  
		  for(Gamma gam:this.gammasV) {
			  String flags=gam.flag();

			  boolean done=false;
			  for(int i=0;i<flags.length();i++) {
				  String flag=flags.charAt(i)+"";
				  if(flagRefMap.containsKey(flag)) {
					  //System.out.println("### "+gam.ES()+" flag="+flag+"  flags="+flags);
					  
					  gam.setEGRef(flagRefMap.get(flag));
					  done=true;;
					  break;
				  }
			  }
			  if(done)
				  continue;

			  for(Comment c:gam.commentV) {
				  if(!c.headV().contains("E")) 
					  continue;
				  
				  line=Str.firstSentence(c.body()).toUpperCase().trim();

				  int n=line.indexOf("FROM");
				  if(n==0){
					  s=line.substring(n+5).trim();
					  if(s.length()>=8 && EnsdfUtil.isKeynumber(s.substring(0,8))) {
						   gam.setEGRef(s.substring(0,8));
						   done=true;
						   break;
					  }
				  }
			  }
			  
			  if(done)
				  continue;
			  
			  if(commonRef.length()>0)
				  gam.setEGRef(commonRef);
		  }
		  
	  }catch(Exception e) {
		  e.printStackTrace();
	  }
  }
  
  @SuppressWarnings("unused")
protected void parseEGRef_old() {
	  try {
		  String commonRef="";//from top comments
		  HashMap<String,String> flagRefMap=new HashMap<String,String>();
		  LinkedHashMap<Double,String> uptoEGRefMap=new LinkedHashMap<Double,String>();
		  
		  for(Comment c:this.comV) {
			  if(!c.headV().contains("E")) 
				  continue;
			  
			  String line=Str.firstSentence(c.body()).toUpperCase().trim();

			  
			  String s1="",s2="", tempRef="";
			  String s=line;
			  int n=s.indexOf("FROM");
			  while(n>=0) {
				  s1=s.substring(0,n).trim();
				  s2=s.substring(n+5).trim();
				  
				  if(s2.length()>=8 && EnsdfUtil.isKeynumber(s2.substring(0,8))) {
					  tempRef=s2.substring(0,8);
					  int n1=s1.indexOf("UP TO");
					  if(n1<0)
						  n1=s1.indexOf("BELOW");
						  
					  if(n1>=0) {
						  //Levels up to 5050 from 1981Ba40; above that from 1979He18
						  String ns=Str.findFirstNumberText(s1.substring(n1+5).trim());
						  int nsIndex=s1.indexOf(ns);						  
						  int sepIndex=findFirstDelimiterPosition(s1);
						  
						  double uptoEG=EnsdfUtil.s2f(ns);
						  if(uptoEG>0 && (sepIndex<0||sepIndex>nsIndex)) {
							  uptoEGRefMap.put(uptoEG, tempRef);
						  }else{
							  break;
						  }
	
						  
					  }else {
						  //From 1981Ba40 up to 5040 level; above that from 1979He18
						  n1=s2.indexOf("UP TO");
						  if(n1<0)
							  n1=s2.indexOf("BELOW");
						  
						  if(n1>0) {
							  String ns=Str.findFirstNumberText(s2.substring(n1+5).trim());
							  int nsIndex=s2.indexOf(ns);						  
							  int sepIndex=findFirstDelimiterPosition(s2);
							  
							  double uptoEG=EnsdfUtil.s2f(ns);
							  if(uptoEG>0 && (sepIndex<0||sepIndex>nsIndex)) {
								  uptoEGRefMap.put(uptoEG, tempRef);
							  }else{
								  break;
							  }
						  }else {
							  commonRef=tempRef;
							  break;
						  }
						  
					  }
				  }else {
					  break;
				  }
				  				  
			  }
			  if(line.startsWith("FROM")){
				  
			  }else {
				  
			  }
		  }
		  
	  }catch(Exception e) {
		  
	  }
  }
  @SuppressWarnings("unused")
private int findFirstDelimiterPosition(String s, int fromIndex) {
	    if (s == null) return -1;
	    if (fromIndex < 0) fromIndex = 0;
	    if (fromIndex >= s.length()) return -1;
	    
	    int commaPos = s.indexOf(',', fromIndex);
	    int semicolonPos = s.indexOf(';', fromIndex);
	    int andPos = s.indexOf("AND", fromIndex);
	    
	    int firstPos = -1;
	    if (commaPos >= 0) firstPos = commaPos;
	    if (semicolonPos >= 0 && (firstPos == -1 || semicolonPos < firstPos)) firstPos = semicolonPos;
	    if (andPos >= 0 && (firstPos == -1 || andPos < firstPos)) firstPos = andPos;
	    
	    return firstPos;
	}
  
  private int findFirstDelimiterPosition(String s) {
	    if (s == null) return -1;
	    
	    int commaPos = s.indexOf(',');
	    int semicolonPos = s.indexOf(';');
	    int andPos = s.indexOf(" AND ");
	    
	    // Find the smallest positive position
	    int firstPos = -1;
	    if (commaPos >= 0) firstPos = commaPos;
	    if (semicolonPos >= 0 && (firstPos == -1 || semicolonPos < firstPos)) firstPos = semicolonPos;
	    if (andPos >= 0 && (firstPos == -1 || andPos < firstPos)) firstPos = andPos;
	    
	    return firstPos;
	}
  
  /*
   * called after processing main body of the datasets including levels, gammas, decays and delays
   */
  protected void calculateControlVariables() {
        /* Calculating a bunch of useful control variables: */  
        
        nllev=0;
        njlev=0;
        ntlev=0;
        nclev=0;
        ncrlev=0;
        ndmlev=0;
        ntotgam=0;
        nigam=0;
        ntigam=0;
        nmgam=0;
        nmrgam=0;
        nccgam=0;
        nnzflgam=0;
        nflgam=0;
        ngsgam=0;
        nmaxgaml=0;
        ncgam=0;
        ncrgam=0;

        ntotalp=0;
        nflalp=0;
        nialp=0;
        nhfalp=0;
        ncalp=0;
        ncralp=0;

        ntotbet=0;
        nflbet=0;
        nibet=0;
        nlbet=0;
        ncbet=0;
        ncrbet=0;

        ntotecbp=0;
        nflecbp=0;
        nitecbp=0;
        nieecbp=0;
        nibecbp=0;
        nlecbp=0;
        ncecbp=0;
        ncrecbp=0;

        ntotdp=0;
        nedp=0;
        nidp=0;  
        neidp=0; 
        ntdp=0;  
        nldp=0;  
        ncdp=0;  
        ncrdp=0;  
        DPNumber=0;
        particleType="";
        promptOrDelayMarker="";
        delayType="";
        
        for( int j=0; j < levelsV.size() ; j++ ){
            Level l=(Level) levelsV.elementAt(j);
            if( l.JPiS().length() > 0 ) njlev++;
            if( l.halflife().length() > 0 ) ntlev++;
            if(l.lS().length()>0) nllev++;
            
            nclev=nclev+l.nComments();
            ncrlev=ncrlev+l.nContRecords();
            if( l.nDecayModes() > 0 ) ndmlev++;
            if( l.nGammas() >= nmaxgaml ) nmaxgaml=l.nGammas();

            ntotgam=ntotgam+l.nGammas();
            ntotalp=ntotalp+l.nAlphas();
            ntotbet=ntotbet+l.nBetas();
            ntotecbp=ntotecbp+l.nECBPs();
            ntotdp=ntotdp+l.nDParticles();
            
            nflalp=nflalp+l.nAlphas();
            nflbet=nflbet+l.nBetas();
            nflecbp=nflecbp+l.nECBPs();
            
            for( int k=0; k < l.nGammas() ; k++ ){
                Gamma g=l.gammaAt(k);
                if( g.RIS().length() > 0 ) nigam++;
                if( g.TIS().length() > 0 ) ntigam++;
                if( g.MS().length() > 0 ) nmgam++;
                if( g.MRS().length() > 0 ) nmrgam++; 
                if( g.CCS().length() > 0 ) nccgam++; 
                if( g.FLI() > 0 ) {
                	nnzflgam++; 
                	nflgam++;
                }else if(g.FLI()==0) {
                	ngsgam++;
                	nflgam++;
                }
                
                ncgam=ncgam+g.nComments();
                ncrgam=ncrgam+g.nContRecords();
                
                if(g.RIS().length()>0 && g.TIS().length()==0 && g.CCS().length()==0)
                    canGetAllTI=false;
                    
                //Jun
                for(int m=0;m<g.flag().length();m++){
                    String ch=g.flag().substring(m,m+1);

                    if(gammaFlags.contains(ch))
                        continue;
                  
                    gammaFlags+=ch;
                }      

            }
            
            for( int k=0; k < l.nAlphas() ; k++ ){
                Alpha a=l.alphaAt(k);
                if( a.RIS().length() > 0 ) nialp++;
                if( a.HFS().length() > 0 ) nhfalp++;
                ncalp=ncalp+a.nComments();
                ncralp=ncralp+a.nContRecords();
                
                //Jun
                for(int m=0;m<a.flag().length();m++){
                    String ch=a.flag().substring(m,m+1);
                    if(decayFlags.contains(ch))
                        continue;
                  
                    decayFlags+=ch;
                }   
            }
            
            for( int k=0; k < l.nBetas() ; k++ ){
                Beta b=l.betaAt(k);
                if( b.RIS().length() > 0 ) nibet++;
                if( b.LOGFTS().length() > 0 ) nlbet++;
                ncbet=ncbet+b.nComments();
                ncrbet=ncrbet+b.nContRecords();
                
                //Jun
                for(int m=0;m<b.flag().length();m++){
                    String ch=b.flag().substring(m,m+1);
                    if(decayFlags.contains(ch))
                        continue;
                  
                    decayFlags+=ch;
                }  
            }
            for( int k=0; k < l.nECBPs() ; k++ ){
                ECBP e=l.ECBPAt(k);
                if( e.IES().length() > 0 ) nieecbp++;
                if( e.ITS().length() > 0 ) nitecbp++;
                if( e.IBS().length() > 0 ) nibecbp++;
                if( e.LOGFTS().length() > 0 ) nlecbp++;
                ncecbp=ncecbp+e.nComments();
                ncrecbp=ncrecbp+e.nContRecords();
                
                //Jun
                for(int m=0;m<e.flag().length();m++){
                    String ch=e.flag().substring(m,m+1);
                    if(decayFlags.contains(ch))
                        continue;
                  
                    decayFlags+=ch;
                }  
            }
            for( int k=0; k < l.nDParticles() ; k++ ){
                DParticle p=l.DParticleAt(k);
                if( p.ES().length()>0) nedp++;
                if( p.RIS().length() > 0 ) nidp++;  
                if( p.EIS().length() > 0 ) neidp++; 
                if( p.TS().length() > 0 ) ntdp++;  
                if( p.LS().length() > 0 ) nldp++;  
                ncdp=ncdp+p.nComments();
                ncrdp=ncrdp+p.nContRecords();
                if( particleType.length() == 0 ) particleType=p.particle();
                if( promptOrDelayMarker.length() == 0 ) promptOrDelayMarker=p.delayed();
                
                //Jun
                for(int m=0;m<p.flag().length();m++){
                    String ch=p.flag().substring(m,m+1);
                    if(decayFlags.contains(ch))
                        continue;
                  
                    delayFlags+=ch;
                }  
             }
        }

        //unplaced radiations
        analyzeUnplacedRadiations();
        
        for( int k=0; k <atomicRad.xRaysV().size() ; k++ ){
            AtomicData ad=atomicRad.xRaysV().get(k);

            for(int m=0;m<ad.flag().length();m++){
                String ch=ad.flag().substring(m,m+1);
                if(xrayFlags.contains(ch))
                    continue;
              
                xrayFlags+=ch;
            }       
        }
        
        for( int k=0; k <atomicRad.AugersV().size() ; k++ ){
            AtomicData ad=atomicRad.AugersV().get(k);

            for(int m=0;m<ad.flag().length();m++){
                String ch=ad.flag().substring(m,m+1);
                if(augerFlags.contains(ch))
                    continue;
              
                augerFlags+=ch;
            }      
        }
        
        if(particleType.length()>0) {
            if(particleType.equalsIgnoreCase("P")){midNucleus.setZA(nucleus.Z()+1,nucleus.A()+1);}
            else if(particleType.equalsIgnoreCase("N")){midNucleus.setZA(nucleus.Z(),nucleus.A()+1);}
            else if(particleType.equalsIgnoreCase("T")){midNucleus.setZA(nucleus.Z()+1,nucleus.A()+3);}
            else if(particleType.equalsIgnoreCase("D")){midNucleus.setZA(nucleus.Z()+1,nucleus.A()+2);}
            else if(particleType.equalsIgnoreCase("A")) {midNucleus.setZA(nucleus.Z()+2,nucleus.A()+4);}
        }
        
        if(this.ntotbet>0)
           decayRecordType="B";
        else if(this.ntotecbp>0)
           decayRecordType="E";
        else if(this.ntotalp>0)
           decayRecordType="A";
        else if(this.ntotdp>0){
           decayRecordType=promptOrDelayMarker+this.particleType;
              
           if(promptOrDelayMarker.trim().isEmpty()){
               delayType="";
           }else{
               String s=this.dsid0_s;
               int n=s.indexOf("DECAY");
               if(n>0){
                   s=s.substring(0,n).trim();
                   n=s.lastIndexOf(' ');
                   if(n>0)
                       s=s.substring(n).trim();
               }    
               
               if(s.indexOf("B-")==0)
                   delayType="B-";
               else if(s.indexOf("B+")==0)
                   delayType="B+";
               else if(s.indexOf("EC")==0)
                   delayType="EC";
               else if(s.indexOf("A")==0)
                   delayType="A";
               else 
                   delayType=s.replace(particleType,"").replace("+","");
           }
           
           //System.out.println("dsid0_s="+dsid0_s+"  DPDelayed="+DPDelayed+"  DelayType="+delayType);

        }else if(!decayTypeInDSID.isEmpty()) {//for decay or delay dataset with no decay record
            //note that at this point, parent record has not be parsed yet

            parseDecayTypeFromDSID();
        }     
  }

  protected void parseDecayTypeFromDSID() {
	  String s=decayTypeInDSID;   
	  
      if(s.indexOf("B-")==0) {
          delayType="B-";
          decayRecordType="B";
       }else if(s.indexOf("B+")==0) {
          delayType="B+";
          decayRecordType="E";
       }else if(s.indexOf("EC")==0) {
          delayType="EC";
          decayRecordType="E";
       }else if(s.indexOf("A")==0) {
          delayType="A";
          decayRecordType="A";
       }else if(s.equals("P") || s.equals("N")) {
    	   delayType="";
    	   decayRecordType=s;
       }
       
       if(decayRecordType.length()>0) {
           if(s.equals(delayType)) {//E,B,A type decay
               delayType="";
           }else if(s.length()-delayType.length()==1 && !delayType.isEmpty()) {//E,B,A type delayed-particle, like B-N, B+P, B-A
               promptOrDelayMarker="D";
               DPNumber=1;
               particleType=s.substring(delayType.length());
               decayRecordType=promptOrDelayMarker+particleType;
           }else if(s.length()-delayType.length()==2 && Character.isDigit(s.charAt(s.length()-2)) && !delayType.isEmpty()) {//E,B,A type delayed-particle, like B-2N
               promptOrDelayMarker="D";
               DPNumber=2;
               particleType=s.substring(delayType.length()+1);
               decayRecordType=promptOrDelayMarker+particleType;
           }else if(s.equals(decayRecordType)) {//prompt particle decay
        	   promptOrDelayMarker=" ";
        	   DPNumber=1;
        	   particleType=s;
        	   decayRecordType=" "+s;
           }else {
               delayType="";
               decayRecordType="";
           }
               
       }	  
  }
/* -------------------------- A Few helpful Methods -------------------------- */

  protected void checkGamma(){

/* Performing some checks */

    for( int j=0; j < levelsV.size() ; j++ ){
        Level l=(Level) levelsV.elementAt(j);
       for( int k=0; k < l.nGammas() ; k++ ){
            
            Gamma g=l.gammaAt(k);

            if( g.FLI() >= 0 ){
               Level lf= (Level) levelsV.elementAt( g.FLI() );
               CheckGammaM cgm=new CheckGammaM();
               cgm.setValues(l,g,lf ); 
               if( ! cgm.canDo() ) g.setWarning( cgm.warning() );
            }
        }
   }

  }

/* --------------------------------------------------------------------------- */

  protected void checkJPIOrder(){

/* Calculating the ordering in the jpi's, only if there is a single value of J */

    for( int j=0; j < levelsV.size() ; j++ ){
      int count;
      if( ( (Level) levelsV.elementAt(j) ).nJs() == 1){
        count=1;
        for( int k=0; k < j ; k++){
          if( ( (Level) levelsV.elementAt(k) ).nJs() == 1){

            if( ( (Level) levelsV.elementAt(k) ).jValAt(0)  == ( (Level) levelsV.elementAt(j) ).jValAt(0) &&  
                ( (Level) levelsV.elementAt(k) ).piValAt(0).equals(( (Level) levelsV.elementAt(j) ).piValAt(0) ) )
                count++;
          }
        }
      }
      else{
        count=-1;
      }
      ( (Level) levelsV.elementAt(j) ).setJCount( count );
    }

  }

  /*
   * find offset defined in top comments including general and footnote comments
   */
  private String[] findOffsetFromTopComments(String key,String name) {
      String[] out=new String[4];
      out[0]="";//s, or lower bound
      out[1]="";//ds, or upper bound or limit op symbol like "<"
      out[2]="";//"R" for range
      out[3]="";//line
      
      try{
          //System.out.println("  poslev.get(0)="+poslev.get(0)+" key="+key+" name="+name);
         
          String line="",s="",ds="",rangeMark="",lineType="";
          if(poslev==null || poslev.size()==0)
        	  return out;
          
          for(int i=0;i<poslev.get(0);i++) {
              line=lines.get(i);       
              lineType=line.substring(5,7);
              
              //System.out.println("ENSDF line 860 line="+line+" key="+key+" name="+name+" lineType="+lineType+"*");
              
              if(line.length()>16 && lineType.toUpperCase().charAt(1)=='C') {
                  char c=line.charAt(7);
                  
                  if(c==' '||c=='L') {
                      line=Translator.procGenCom(line, 0, false);
                      
                      //System.out.println("ENSDF line 862 line="+line+" key="+key+" name="+name);
                      
                      String[] temp=EnsdfUtil.findOffsetInLine(line,name);
                      s=temp[0];
                      ds=temp[1];
                      rangeMark=temp[2];
                      
                      //System.out.println("ENSDF line 865 line="+line+" key="+key+" value="+s);

                      if(!s.isEmpty()) { 
                          out[0]=s;
                          out[1]=ds;
                          out[2]=rangeMark;
                          out[3]=line;
                          
                          break;        
                      }
                  }
              }
          }          
      }catch(Exception e) {
          e.printStackTrace();
      }

      return out;
  }
  
  private String[] findOffsetFromLevelComments(Level lev,String key,String name) {
      String[] out=new String[4];
      out[0]="";//s, or lower bound
      out[1]="";//ds, or upper bound or limit op symbol like "<"
      out[2]="";//"R" for range
      out[3]="";//line
      
      
      try{
          String line="",s="",ds="",rangeMark="";

          String es=lev.ES();//note SN in field has been translated into S(n) in ES
          if(!es.contains(key) && !es.contains(name))
              return out;
      
          for(Comment c:lev.commentV) {
              line=c.body();
              if(!line.contains(name) && line.contains(key))
                  line=Translator.procGenCom(line,0,false);
              
              //System.out.println("ENSDF line 1052 line="+line+" key="+key+" name="+name+" line.indexOf(type+=)="+line.indexOf(name+"="));

              String[] temp=EnsdfUtil.findOffsetInLine(line,name);
              s=temp[0];
              ds=temp[1];
              rangeMark=temp[2];

              //System.out.println("ENSDF line 1070 line="+line+" key="+key+" value="+s);

              if(!s.isEmpty()) {                  
                  out[0]=s;
                  out[1]=ds;
                  out[2]=rangeMark;
                  out[3]=line;
                  break;
              }
          }   
          
      }catch(Exception e) {
          
      }
  
      return out;      
  }
  /*
   * find offset defined in level comments
   * return:
   *   out[0]: offset
   *   out[1]: the line where the offset is from. It can be used for further 
   *           search of additional offset, like SN+X
   */
  private String[] findOffsetFromLevelComments(String key,String name) {
      return findOffsetFromLevelComments(levelsV,key,name);
  }
  
  private String[] findOffsetFromLevelComments(Vector<Level> levsV,String key,String name) {
      String[] out=new String[4];
      out[0]="";//s, or lower bound
      out[1]="";//ds, or upper bound or limit op symbol like "<"
      out[2]="";//"R" for range
      out[3]="";//line
           
      try{
          for(int i=0;i<levsV.size();i++){
              Level l=levsV.elementAt(i);

              out=findOffsetFromLevelComments(l,key,name);  
              
              if(!out[0].isEmpty())
                  break;
          }   
          
      }catch(Exception e) {
          
      }
  
      return out;
  }
  
  private String[] findOffsetFromComments(String key,String name) {
      String[] out=new String[4];
      out[0]="";//s, or lower bound
      out[1]="";//ds, or upper bound or limit op symbol like "<"
      out[2]="";//"R" for range
      out[3]="";//line
      
      out=findOffsetFromTopComments(key,name);
      
      String s=out[0];
      //if offset not found,continue to search for it in level comments
      if(s.isEmpty())
          out=findOffsetFromLevelComments(key,name);
      
      return out;
  }
  
  /*
   * search by order:
   *   1. current level comments 
   *   2. top comments
   *   3. other level comments
   */
  private String[] findOffsetFromComments(Level lev,String key,String name) {
      String[] out=new String[4];
      out[0]="";//s, or lower bound
      out[1]="";//ds, or upper bound or limit op symbol like "<"
      out[2]="";//"R" for range
      out[3]="";//line
      
      String s="";
      
      out=findOffsetFromLevelComments(lev,key,name);
      s=out[0];
      
      if(s.isEmpty()) {
          out=findOffsetFromTopComments(key,name);
          s=out[0];
          
          //if offset not found,continue to search for it in level comments
          if(s.isEmpty()) {
              Vector<Level> tempV=new Vector<Level>();
              tempV.addAll(levelsV);
              tempV.remove(lev);
              
              out=findOffsetFromLevelComments(tempV,key,name);   
          }
      }

      
      return out;
  }
  
  
  /*
   * levels
   */
  private void parseLevelsAndGammas() throws Exception{
        poslev.clear();
        
        for( int i=0 ; i<lines.size() ; i++){
          String s=((String) lines.elementAt(i));   
          if( s.substring(5,9).equals("  L ") ) {
              poslev.addElement( new Integer(i) );                            
          }          
        }

        
        if(sysOut) System.out.println(nucleus.nameTUC()+": Begin to do levels, poslev size="+poslev.size());

        levelsV.clear();
        gammasV.clear();
        betasV.clear();
        ecbpsV.clear();
        alphasV.clear();
        dpsV.clear();
        
        for( int i=0 ; i < poslev.size() ; i++){

          int ki= ((Integer) poslev.elementAt(i)).intValue(); 
          int kf=lines.size()-1; 
          if( i < ( poslev.size()-1 ) ) kf=((Integer) poslev.elementAt(i+1)).intValue() -1;
          lline.clear();
          
          for( int j=ki; j<=kf; j++){ lline.addElement( (String) lines.elementAt(j)); }
          
          Level l=new Level();
          l.setSilent(silent);
          
          if( lightEds ) l.setLight();
          
          
          
          l.setValues(lline,levelsV.size());
           
          //debug
          //System.out.println("In ENSDF line 370: l="+l.ES()+" index="+levelsV.size()+" JPI="+l.JPiS());
          
          
          String flags=l.flag().trim();
          for(int j=0;j<flags.length();j++){
              String ch=flags.substring(j, j+1);
              if(!levelFlags.contains(ch))
                  levelFlags+=ch;
          }
          
          if(levelsV.size()>0) {
              Level lastLev=levelsV.lastElement();
              if(l.ES().equals(lastLev.ES())&&l.DES().equals(lastLev.DES())) {
            	  l.isMultipletMember=true;
            	  lastLev.isMultipletMember=true;
            	  if(lastLev.indexInMultiplet<0) {
            		  lastLev.indexInMultiplet=0;
            	  }
            	  
            	  l.indexInMultiplet=lastLev.indexInMultiplet+1;
              }
          }
   
          levelsV.addElement(l);
          gammasV.addAll(l.GammasV());
          betasV.addAll(l.BetasV());
          ecbpsV.addAll(l.ECBPsV());
          alphasV.addAll(l.AlphasV());
          dpsV.addAll(l.DParticlesV());
          

        }
        
        checkAndsortLevelEnergies();
  }
  
  private String[] parseOffsetFromComment(String[] offsetResults) {
	  String[] out=new String[offsetResults.length];
	  for(int i=0;i<out.length;i++)
		  out[i]=offsetResults[i];
	  
	  String s=out[0];
	  String ds=out[1];
	  String rangeMark=out[2];
	  
      if(rangeMark.equals("R") && Str.isNumeric(s) && Str.isNumeric(ds)){//like "30 to 50"
    	  float v=Float.parseFloat(s);
    	  float dv=Float.parseFloat(ds);
    	  v=(v+dv)/2;
    	  dv=v;
    	  
    	  XDX2SDS xs=new XDX2SDS(v,dv);
    	  s=xs.s();
    	  ds=xs.ds();
    	  
    	  out[0]=s;
    	  out[1]=ds;
      }
	  return out;
  }
  
  protected void checkAndsortLevelEnergies() throws Exception {
      //added by Jun 8/7/2015 to correctly assign level energy
      float prevLevEF=-1;
      float nextLevEF=-1;
      float levE=-1;
      float maxLevEF=0;
      float offset=0;
      
      Vector<String> keysWithTrueOffset=new Vector<String>();
      
      HashMap<String,String> EFOffsetMap=new HashMap<String,String>();//temporary offset map for EF. Note offsetMap is for ERPF
      //NOTE that offset is for getting ERPF NOT for EF and ERF
      
      for(int i=0;i<levelsV.size();i++){
          Level l=levelsV.elementAt(i);
          offset=0;
          
          if(prevLevEF<0){
              prevLevEF=0.0f;
              //continue;
          }
          
          float currLevEF=-1;
          
          //System.out.println("In ENSDF:l.NNES="+l.NNES()+" l.ES="+l.ES()+" EF="+l.EF()+" qv.SPF="+qv.SPF());
          
          //System.out.println(" SN="+Translator.procGenCom("SN", 0, false)+"  X="+Translator.procGenCom("X", 0, false));
          
          if(Str.isNumeric(l.ES()))
              currLevEF=l.EF();
          else{
              String key=l.NNES();                
              if(!offsetMap0.containsKey(key)){
                  
                  String name="";
                  if(key.length()==1)
                      name=key.toLowerCase();
                  else
                      name=Translator.procGenCom(key,0,false);
                  
                  String[] out=new String[2];
                  out[0]="";
                  out[1]="";
                  

                  out=findOffsetFromComments(l,key,name);
                  out=parseOffsetFromComment(out);
                  
                  String s=out[0];
                  String ds=out[1];
                  
                  if(EnsdfUtil.s2f(s)<prevLevEF) {
                	  s="";
                	  ds="";
                  }
                  //if(l.ES().equals("b")) 
                  //    System.out.println("ENSDF 1305: l.es="+l.ES()+" name="+name+" ERF="+l.ERF()+" key="+key+" s="+s+" ds="+ds);
                  
                  if(l.ES().equals(name)){//l.ES()=X, l.NNES()=X, l.ES=S(n) for NNES=SN
                      //debug
                      //System.out.println("In ENSDF: for Offset1: key="+key+" offset="+(prevLevEF+0.1f)+" prevLevEF="+prevLevEF+" s="+s+"*");
    
                      if(!s.isEmpty()) {
                    	  addOffset(key,s,ds);
                          keysWithTrueOffset.add(key);
                      }else {
                    	  addOffset(key,String.valueOf(prevLevEF+0.1f),"0");
                      }

                      EFOffsetMap.put(key, String.valueOf(prevLevEF+0.1f));
                      
                      currLevEF=prevLevEF+0.1f; 
                      
                  }else if(l.ERF()>=0 || l.ES().indexOf("-")>0){//l.ES()=X+1234.5, l.NNES()=X; SN-0.02, l.NNES()=SN
                      offset=prevLevEF+0.1f-l.ERF();
                      if(offset<=0)
                          offset=0.1f;

                      //debug
                      //System.out.println("In ENSDF 166: for Offset2: key="+key+" ES="+l.ES()+" name="+name+" offset="+(prevLevEF+0.1f)+" prevLevEF="+prevLevEF+"  s="+s+" offset="+offset);

                      if(l.ERF()>prevLevEF)
                          currLevEF=l.ERF()+offset;
                      else
                          currLevEF=prevLevEF+0.1f;          
                      
                      if(!s.isEmpty()) {
                    	  addOffset(key,s,ds);
                          keysWithTrueOffset.add(key);
                      }else {
                          addOffset(key, String.valueOf(offset),"0"); 
                      }
                      
                      EFOffsetMap.put(key, String.valueOf(offset));
                      
                  }else {//SN+X, NNES=SN
                      String line=out[1];
                      
                      if(!s.isEmpty()) {
                    	  addOffset(key,s,ds);
                          keysWithTrueOffset.add(key);
                          
                          String nes=l.NES().replace("+","");
                          
                          s="";
                          String key1=nes.toUpperCase();
                          String[] temp=new String[0];
                          if(line.contains(nes.toLowerCase()+"=")) {
                        	  //[0] for s, or lower bound for case like "x=30-50" or "x=30 to 50"
                        	  //[1] for ds, or upper bound for case like "x=30-50" or "x=30 to 50", or "<", ">" for range
                        	  //[2]="R" for range for case like "x>..", "x<..", "x=30 to 50"
                              temp=EnsdfUtil.findOffsetInLine(line,nes.toLowerCase());
                              temp=parseOffsetFromComment(temp);

                          }else if(line.contains(nes.toUpperCase()+"=")) {
                              temp=EnsdfUtil.findOffsetInLine(line,nes.toUpperCase());
                              temp=parseOffsetFromComment(temp);
                          }else {
                              out=findOffsetFromComments(key1,nes.toLowerCase());
                              if(out[0].isEmpty())
                                  out=findOffsetFromComments(key1,nes.toUpperCase());
                              
                              temp=parseOffsetFromComment(out);
                          }
                          
                          if(temp.length>0) {
                              s=temp[0];
                              ds=temp[1];
                          }
                          //debug
                          //System.out.println("In ENSDF 1264: key="+key+" key1="+key1+" ES="+l.ES()+" name="+name+" nes="+nes+" s="+s);
                          
                          if(!s.isEmpty()) {                               
                        	  addOffset(key1,s,ds);
                              keysWithTrueOffset.add(key1); 
                          }
                      }else {
                          offset=prevLevEF+500;                      
                          addOffset(key, String.valueOf(offset),"0");
                      }
                      
                      EFOffsetMap.put(key, String.valueOf(prevLevEF+500));
                      
                      currLevEF=prevLevEF+500;
                      
                  }

              }else{
                  //if(!Str.isNumeric(EFOffsetMap.get(key)))
                  //        System.out.println(" ###### ENSDF 1284  key="+key+" l.es="+l.ES()+" dsid="+lines.get(0)+"   "+EFOffsetMap.get(key)+" SP="+this.qv.SPF());
              
                  EFOffsetMap.put(key, offsetMap0.get(key));
                
                  offset=Float.parseFloat(EFOffsetMap.get(key));

                  
                  // E < PRECEDING LEVEL ENERGY, e.g.,
                  //244AM  L SN+0.07804                                                               
                  //244AM  L SN+0.79821          //error that FMTCHK can't catch                                                     
                  //244AM  L SN+0.08040                                                              
      
                  if(l.ERF()>0)
                      currLevEF=l.ERF()+offset;  
                  else {
                      if(l.NES().length()>0) {//SN+Y, SN already in the offset map, but NES=Y not
                          String nes=l.NES().replace("+","");
                          
                          String s="",ds="";
                          key=nes.toUpperCase();
                          
                          String[] out=findOffsetFromComments(l,key,nes.toLowerCase());
                          if(out[0].isEmpty())
                              out=findOffsetFromComments(l,key,nes.toUpperCase());
                          
                          out=this.parseOffsetFromComment(out);
                          if(out.length>0) {
                              s=out[0];
                              ds=out[1]; 
                          }

                          if(!s.isEmpty()) {   
                        	  addOffset(key,s,ds);
                              keysWithTrueOffset.add(key); 
                          }
                      }
                      
                      currLevEF=prevLevEF+0.1f;
                  }

                  //if(currLevEF<prevLevEF){//reset offset
                  //  offset=prevLevEF+0.1f-l.ERF();
                  //  offsetMap.put(key, String.valueOf(offset));
                  //}
                  //debug
                  //System.out.println("In ENSDF: for Offset3: l.ES="+l.ES()+"  key=l.NNES="+l.NNES()+" offset="+offsetMap.get(l.NNES())
                  //   +" prevLevEF="+prevLevEF+" l.ERF()="+l.ERF()+" currentEF="+currLevEF);

              }
              
              //l.setEF(currLevEF, 0);
          }
          
          if(currLevEF<0)
              currLevEF=prevLevEF+0.1f;
          else if(currLevEF<prevLevEF){
              String s=levelsV.get(i-1).ES();
              
              String msg="***Warning: "+l.ES()+" < preceding level="+s;
              errorMsg.append(msg+"\n");
          } 
          
          //if(l.ES().equals("b") || l.ES().contains("a"))
          //    System.out.println("In ENSDF 1303: for Offset4: l.ES="+l.ES()+"  key=l.NNES="+l.NNES()+" offset="+offsetMap.get(l.NNES())+" currLevEF="+currLevEF+" prevLevEF="+prevLevEF+" l.ERF()="+l.ERF()+" maxLevEF="+maxLevEF);

          prevLevEF=currLevEF;
          if(prevLevEF>maxLevEF)
              maxLevEF=prevLevEF;
          
          //debug
          //if(l.ES().equals("b") || l.ES().contains("a"))
          //    System.out.println("In ENSDF 1311: for Offset5: l.ES="+l.ES()+"  key=l.NNES="+l.NNES()+" offset="+offsetMap.get(l.NNES())+" currLevEF="+currLevEF+" prevLevEF="+prevLevEF+" l.ERF()="+l.ERF()+" maxLevEF="+maxLevEF);
      }
      
      maxLevEF+=0.1f;
      
      
      /*
      //checked S(p) and S(n) in comments
      //NOTE that comments have not been parsed yet and last line before levels (llbl) 
      //has not been set yet at this point.
      String key="",type="";
      if(offsetMap.containsKey("SN") && qv.SNF()<=0) {
          key="SN";
          type="S(n)";
      }else if(offsetMap.containsKey("SP") && qv.SPF()<=0) {
          key="SP";
          type="S(p)";
      }
      
      //System.out.println("ENSDF line 991 key="+key+" type="+type);
      
      if(!key.isEmpty()) {

          String[] out=findOffsetFromComments(key,type);
          String s=out[0];
          String line=out[1];
          
          //System.out.println("ENSDF line 1125: line="+line+" key="+key+" type="+type+"  offset="+s+" from map="+offsetMap.get(key));
          
          if(!s.isEmpty()) {
              offsetMap.put(key, s);                
              keysWithTrueOffset.add(key);
              
              //further search X,Y offsets in E(level)=SN+X, SN+Y
              String[] types= {"X","Y","Z"};
              s="";
              for(int i=0;i<types.length;i++) {
                  type=types[i];
                  if(line.contains(type+"="))
                      s=findOffsetInLine(line,type);
                  else if(line.contains(type.toLowerCase()+"="))
                      s=findOffsetInLine(line,type.toLowerCase());
                  else
                      continue;
                  
                  //System.out.println("ENSDF line 1137: key="+key+" type="+type+"  offset="+s);
                  
                  if(!s.isEmpty()) {
                      offsetMap.put(type, s);                          
                      keysWithTrueOffset.add(key);
                  } 
              }                
          }           
      }
      */
      
      
      //for level 1234.5+X
      // ERF() return 1234.5
      // EF()   return estimated energy based on levels before and after current level
      //        so all return values are in order of energies, but differences between 
      //        levels of such formats are not the difference of ERF()
      // ERPF() return values of 1234.5+(offset for X), the differences between levels
      //        of the same format are the same as differences of ERF(), but
      //        all return values of all levels could not be in order.
      //        If true values of X (offset) is known, ERPF is the true value of the
      //        level energy. For numerical E(level), ERPF=EF
      //        Note that ERPF always >0
      //          
      //So, for ordering purpose, use EF() 
      //    for level-energy differences, use ERP()
      
      prevLevEF=-1;
      
      float minGap=1000000;
      for(int i=0;i<levelsV.size();i++){
          Level l=levelsV.elementAt(i);

          levE=l.EF();//by default, l.EF()=-1 if l.ES() is not a number, like X
          

          //debug
          //System.out.println("In ENSDF 1574: size="+levelsV.size()+" level["+i+"]="+" EF="+l.EF()+" ERF="+l.ERF()+" ES="+l.ES()+" NNES="+l.NNES());
          //System.out.println("In ENSDF 1574: size="+levelsV.size()+" level["+i+"]="+"  ES="+l.ES()+" NNES="+l.NNES()+" prevEF="+prevLevEF);
          
          if(levE<0 || !Str.isNumeric(l.ES())){//NOTE: SN+1234.5, l.EF() returns 1234.5 (only for SN+,SP+,SA+, see Level.setValues())
              if(prevLevEF<0){
                  prevLevEF=0.1f;
                  
                  float erf=l.ERF();
                  if(erf<=0) {
                      l.setEF(0.1f, 0f);
                  }else {
                      l.setEF(0.1f+erf, l.DERF());//ERPF is set afterwards
                  }
                      
              }
                  
              int j=i;
              Level nl;
              do{
                  j++;
                  if(j<levelsV.size()) {
                      nl=levelsV.elementAt(j);
                      nextLevEF=nl.EF(); 
                      
                      //System.out.println("    ## nextEF="+nextLevEF+" ES="+nl.ES()+" maxEF="+maxLevEF);
                      if(!Str.isNumeric(nl.ES()))
                          nextLevEF=-1;
                      else
                          nl.setIsTrueERPF(true);//next level will not be processed elsewhere (e.g., in the outmost "else" part where levE>0)
                  }
                  else{
                      nextLevEF=maxLevEF;
                      break;
                  }
                                  
              }while(nextLevEF<0);//next level energy is still not a number, continue to read next level
                  
              
              float egap=(nextLevEF-prevLevEF)/(j-i);
              
              float tempPrevLevERPF=prevLevEF;
              float tempPrevLevEF=prevLevEF;
              
              if(offsetMap0.containsKey(l.NNES()))
                  offset=Float.parseFloat(offsetMap0.get(l.NNES()));
              else//should not happen
                  offset=tempPrevLevERPF+0.1f;

              //debug
              //System.out.println("In ENSDF2: i="+i+" j="+j+" level="+l.EF()+" "+l.ERF()+" "+l.ES()+" prevE="+prevLevEF+" nextE="+nextLevEF+" maxLevEF="+maxLevEF+" offset="+offset+" egap="+egap);

              
              boolean isFakeLevel=false;
              int iFakeLevel=-1;
              for(int k=i;k<j;k++){
                  Level lk=levelsV.elementAt(k);
                  
                  isFakeLevel=false;
                  if(lk.msS().equals("R")){
                      isFakeLevel=true;
                      iFakeLevel+=1;
                  }
                  
                  float diff=0;
                  float def=lk.DEF();
                  if(def<0)
                      def=0;
                  
                  if(!isFakeLevel){
                      if(k>0 && (lk.ERF()>=0 || lk.NNES().indexOf("-")>0)){
                          Level pl=levelsV.elementAt(k-1);
                          if(Str.isNumeric(pl.ES())) {
                              if(lk.NNES().length()>1 && lk.NNES().startsWith("S"))
                                  diff=500;
                              else
                                  diff=0.1f;
                          }else if(lk.NNES().equals(pl.NNES()) && (pl.ERF()>=0 || pl.NNES().indexOf("-")>0)){
                              diff=lk.ERF()-pl.ERF();                          
                              if(lk.ERF()==0 && diff<0)
                                  diff=0.1f;
                          } 
                          
                          if(lk.ERF()>tempPrevLevEF)
                              diff=lk.ERF()-tempPrevLevEF+0.1f;
                      }
                  }

                  //System.out.println(" ES="+l.ES()+"  l.ERF="+l.ERF()+" diff="+diff+" tempPrevLevEF="+tempPrevLevEF);
                  
                  if(diff>0)
                      lk.setEF(tempPrevLevEF+diff, def);
                  else if(k>0){//for k=0, EF is already set at beginning
                      if(egap<10 || (lk.NNES().length()>1&&lk.NNES().startsWith("S")))
                          lk.setEF(tempPrevLevEF+egap, def);
                      else 
                          lk.setEF(tempPrevLevEF+10, def);
                      
                      //if(lk.ES().indexOf("-")>=0)
                      //  lk.setEF(tempPrevLevEF+lk.ERF(), 0);
                      
                      /*
                      if(lk.NNES().length()>1 && lk.ERF()<0){//SN+X,SP+X,SA+X,S2N+X
                          if(lk.NNES().equals("S2N") || lk.NNES().equals("S2P"))
                              lk.setEF(tempPrevLevEF*1.05f, 0);
                          else
                              lk.setEF(tempPrevLevEF*1.2f, 0);
                      }
                      */
                      if(iFakeLevel>=0){
                          float le=0;
                          float gap=0;
                          
                          if(iFakeLevel==0)//first fake level marked with "R"
                              le=tempPrevLevEF*1.2f;
                          else if(iFakeLevel>0)//2nd, 3rd, ..., fake levels 
                              le=tempPrevLevEF*1.1f;  
                                  
                          if(k>1){ 
                              float avgGap=0;
                              avgGap=tempPrevLevEF/(k-1);
                              gap=le-tempPrevLevEF;
                              if(gap<(5*avgGap)){
                                  if(avgGap<(2*minGap))
                                      le=tempPrevLevEF+5*avgGap;
                                  else if(gap<(2.5f*minGap+2.5f*avgGap))
                                      le=tempPrevLevEF+2.5f*minGap+2.5f*avgGap;
                              }
                              
                              //System.out.println("In ENSDF2.0: level["+k+"] j="+j+" le="+le+" gap="+gap+" minGap="+minGap+" avgGap="+avgGap+" tempPrevLevEF="+tempPrevLevEF);
                              
                          }else if(k==1)
                              le=tempPrevLevEF+1000;
                          
                          
                          
                          lk.setEF(le, def);
                      }

                  }
                  
                  //debug
                  //System.out.println("In ENSDF2.0: level["+k+"] l.EF="+l.EF()+" l.ERF="+l.ERF()+" l.ES="+l.ES()+" offset="+offset+" diff="+diff+" nextEF="+nextLevEF);
                  
                  if(lk.EF()>=nextLevEF && k<levelsV.size()-1 && !isFakeLevel)
                      lk.setEF((tempPrevLevEF+nextLevEF)/2.0f, def);
                  
                  //debug
                  //System.out.println("In ENSDF2.1: level["+k+"] l.EF="+l.EF()+" l.ERF="+l.ERF()+" l.ES="+l.ES()+" offset="+offset+" tempPrevLevEF="+tempPrevLevEF);
                  
                  if(offsetMap0.containsKey(lk.NNES()))
                      offset=Float.parseFloat(offsetMap0.get(lk.NNES()));
                  
                  if(lk.ERF()>=0 || lk.ES().indexOf("-")>=0) {
                      lk.setERPF(offset+lk.ERF(), def);                      
                  }else{//E(level)=X,Y,Z,SN,SP,S2N,SN+X    
                      
                      //System.out.println("ENSDF 1360: ES="+lk.ES()+" NNES="+lk.NNES()+" NES="+lk.NES()+" offset="+offset+" EF="+lk.EF()+" ERF="+lk.ERF()+" ERPF="+lk.ERPF());
                      
                      if(lk.NNES().length()>1){//SN,SP,SA,S2N,SN+X
                          if(offsetMap0.containsKey(lk.NNES())) {
                              String nes=lk.NES();//SN+X, NNES=SN, NES=+X
                              nes=nes.replace("+","");
                              if(offsetMap0.containsKey(nes)) {
                                  //System.out.println("ENSDF 1335: ES="+lk.ES()+" NNES="+lk.NNES()+" NES="+lk.NES()+" offset="+offset+"  EF="+lk.EF()+" ERF="+lk.ERF()+" ERPF="+lk.ERPF()+" nes="+nes);
                                  
                                  float offset1=Float.parseFloat(offsetMap0.get(nes));
                                  lk.setERPF(offset+offset1,def);
                                  
                                  //System.out.println("ENSDF 1370: ES="+lk.ES()+" NNES="+lk.NNES()+" NES="+lk.NES()+" offset="+offset+" offset1="+offset1+" EF="+lk.EF()+" ERF="+lk.ERF()+" ERPF="+lk.ERPF());
                              }else
                                  lk.setERPF(offset+500, def);
                          }else {
                              lk.setERPF(tempPrevLevERPF+500, def);
                          }
                      }else{//E(level)=X,Y,Z,....
                          if(offsetMap0.containsKey(lk.NNES()))
                              lk.setERPF(offset, def);
                          else
                              lk.setERPF(tempPrevLevERPF+10, def);
                      }
                      
                      //System.out.println("ENSDF 1383: ES="+lk.ES()+" NNES="+lk.NNES()+" NES="+lk.NES()+" offset="+offset+"  EF="+lk.EF()+" ERF="+lk.ERF()+" ERPF="+lk.ERPF());
                  }
                  
                  if(keysWithTrueOffset.contains(lk.NNES()))
                      lk.setIsTrueERPF(true);
                  
                  //System.out.println("  i="+i+" j="+j+" k="+k+" E(k)="+l.ES()+" isTrueERPF="+l.isTrueERPF()+" nextLevel="+nextLevEF);
                  //if(i<levelsV.size()) System.out.println("      E(i)="+levelsV.get(i).ES()+" isTrueERPF="+levelsV.get(i).isTrueERPF());
                  //if(j<levelsV.size()) System.out.println("      E(j)="+levelsV.get(j).ES()+" isTrueERPF="+levelsV.get(j).isTrueERPF());
                  
                  
                  tempPrevLevEF=lk.EF();
                  tempPrevLevERPF=lk.ERPF();

                  //debug
                  //System.out.println("In ENSDF2.2: level["+k+"] l.ES="+l.ES()+" l.EF="+l.EF()+" l.ERF="+l.ERF()+" l.ERPF="+l.ERPF()+" offset="+offset+" tempPrevLevEF="+tempPrevLevEF);
                  
              }
              
              i=j;
                              
              prevLevEF=nextLevEF;
              
              if(egap>0 && egap<minGap)
                  minGap=egap;
              
          }
          else{
              l.setERPF(l.EF(),l.DEF());
              
              l.setIsTrueERPF(true);
              
              //System.out.println("  EL="+l.ES()+" isTrueERPF="+l.isTrueERPF());
              
              if(prevLevEF>0 && minGap>(levE-prevLevEF))
                  minGap=levE-prevLevEF;
              
              prevLevEF=levE;
          }
          //System.out.println("In ENSDF 1791: size="+levelsV.size()+" level["+i+"]="+" EF="+l.EF()+" ERF="+l.ERF()+" ES="+l.ES()+" NNES="+l.NNES());
          
          //debug
          //System.out.println("In ENSDF3: level="+l.ES()+" EF="+l.EF()+" ERF="+l.ERF()+" ERPF="+l.ERPF());
      }
      
      //for(Level l:levelsV) {
    	//  System.out.println("ENSDF 1798: level="+l.ES()+" EF="+l.EF()+" ERF="+l.ERF()+" ERPF="+l.ERPF());;
      //}
      
      //Note that numerical levels in levelsV is already sorted
      sortedLevelsV.clear();
      
      Vector<Level> unsorted=new Vector<Level>();
      for(Level li:levelsV) {
        
          if(Str.isNumeric(li.ES())) {
              EnsdfUtil.insertRecordByEnergy(li,sortedLevelsV);//sort levels, in case some level has energy <preceding level 
              //sortedLevelsV.add(li);

          }else
              unsorted.add(li);
          
          //System.out.println("  EL="+li.ES()+" isTrueERPF="+li.isTrueERPF());
          
      }
      
      for(Level li:unsorted) {
          //System.out.println("   ENSDF 1161: unsorted level="+li.ES());

          EnsdfUtil.insertRecordByEnergy(li,sortedLevelsV);

          //debug
          //if(li.ES().contains("2554")) {
          //  for(Level l:sortedLevelsV)
          //    System.out.println("#2 l="+l.ES()+" ERPF="+l.ERPF());
          //}
      }   
  }
  
  private void printToErrorMsg(StringBuilder msg,String option,String s) {
      String ePrefix="***  Error: ";
      String wPrefix="***Warning: ";
      String emptyPrefix="            "; 
      String prefix="";
      
      option=option.toUpperCase();
      if(option.equals("E"))
          prefix=ePrefix;
      else if(option.equals("W"))
          prefix=wPrefix;
      else
          prefix=emptyPrefix;
      
      if(msg.length()>0)
          prefix=prefix.replace("*"," ");
        
      msg.append(prefix+s);        
  }
  
  /* find final levels of gammas */
  @SuppressWarnings("unused")
protected void findFinalLevelsForGammas() {
      
        HashMap<Integer,Integer> tempGamFLMap=new HashMap<Integer,Integer>();

        float ei=-1,ef=-1,recoilE=0,dei=0,def=0;
        String eis="";
        
        for( int j=0; j < levelsV.size() ; j++ ){
            Level li=(Level) levelsV.elementAt(j);
            ei=li.ERPF();
            dei=EnsdfUtil.de(li);               
            
            tempGamFLMap.clear();
            
            for( int k=0; k < li.nGammas() ; k++ ){
                Gamma g=li.gammaAt(k);

                float eg=0,deg=0,assumedDEG=0,diff=0;
                
                int fl_i=-1;
                int possible_fl_i=-1;//for adopted levels: energy match, but XREFs not, possibly due to misprint
                String closestEFS="";
                StringBuilder msg=new StringBuilder();
                
                String egs=g.ES().trim();
                if(egs.startsWith("(")) {
                    egs=egs.replace("(","").replace(")","").trim();
                }
                //debug
                //if(g.ES().equals("248.580")) {
                //System.out.println(" test****"+g.ES()+"  g.EF="+g.EF()+"  "+Str.isNumeric(g.ES())+"  "+g.hasFLRecord());                   
                //}
                
                /* first, looking for the "FL=" gamma continuation record */
                if(Str.isNumeric(egs)) {
                    recoilE=(float) 0.5*g.EF()*g.EF()/931500/nucleus.a();
                    eg= ( g.EF() + recoilE );
                    assumedDEG=EnsdfUtil.assumedDEG(egs);
                    
                    if(g.DEF()>0)  
                        deg=g.DEF();             
                    else
                        deg=assumedDEG;                    
                }
                
                
                if( g.hasFLRecord() && !g.FLRecord().equals("?") ){//if FL=?, it means there are multiple candidates for final level,choose the closest one for drawing
                    Level lf=null;
                    String fls=g.FLRecord();
                    float fle=-1;
                    if(Str.isNumeric(fls))
                        fle=Float.parseFloat(fls);
                    
                    for( int l=0; l < j; l++){
                        lf=(Level) levelsV.elementAt(l);
                        if( lf.ES().equalsIgnoreCase(g.FLRecord() ) ){ 
                            fl_i=l; 
                            break;                        
                        }   
                        //debug
                        //System.out.println("In ENSDF: g.FLRecord()="+g.FLRecord()+"  g.EF="+g.EF()+" li.EF="+li.EF()+" li.ES="+li.ES()+" lf.ES="+lf.ES());
                      
                    }
                    
                    //debug
                    //System.out.println("In ENSDF #1: g.FLRecord()="+g.FLRecord()+"  g.EF="+g.EF()+" li.EF="+li.EF()+" li.ES="+li.ES()+" fl_i="+fl_i);
                    
                    //no FLS=FL found, it could be the actual FLS is slightly different from FL record
                    if(fl_i<0) {
                        
                        float minDiff=1000,minDE=0;
                        int imin=-1;
                        Level minLev=null;
                        for( int l=0; l < j; l++){
                            lf=(Level) levelsV.elementAt(l);                            
                            if(Str.isNumeric(lf.ES()) && fle>0) {
                                float d=Math.abs(lf.EF()-fle);
                                float u=EnsdfUtil.de(lf);
                                
                                //debug
                                //if(egs.contains("248.58")) System.out.println(" in ENSDF 1754: EG="+egs+" EI="+li.ES()+" EF="+lf.ES()+"  FL="+fle+"  diff="+d+"  de="+u);
                                
                                if(d<minDiff) {
                                    imin=l;
                                    minDiff=d;
                                    minDE=u;
                                    minLev=lf;
                                }
                            }
                            //debug
                            //System.out.println("In ENSDF: g.FLRecord()="+g.FLRecord()+"  g.EF="+g.EF()+" li.EF="+li.EF()+" li.ES="+li.ES()+" lf.ES="+lf.ES());
                          
                        }                 
                        
                        float de=2.0f;
                        if(egs.contains("."))
                            de=0.5f;
                        
                        //if(minDE>0)
                        //    de=Math.min(de,10*minDE);
                        
                        //if(egs.contains("248.58")) 
                        //    System.out.println(" in ENSDF 1801: EG="+egs+" EI="+li.ES()+" EF="+lf.ES()+"  FL="+fle+"  minDiff="+minDiff+"  de="+de);
                        
                        if(minLev!=null) {
                            if(imin>=0 && minDiff<de) {
                                fl_i=imin;
                                printToErrorMsg(msg,"W","no exact match found for FL="+String.format("%-8s",fls)+" for gamma="+String.format("%-8s", g.ES())+" from level="+String.format("%-8s",li.ES())+"\n");
                                printToErrorMsg(msg," ","       assigned final level="+String.format("%-8s %-2s",minLev.ES(),minLev.DES())+"  diff="+String.format("%-8.3f",minDiff)+"\n");    
        
                            }else {
                                printToErrorMsg(msg,"E","no match found for FL="+String.format("%-8s",fls)+" for gamma="+String.format("%-8s", g.ES())+" from level="+String.format("%-8s",li.ES())+"\n");
                                printToErrorMsg(msg," ","        closest level="+String.format("%-8s %-2s",minLev.ES(),minLev.DES())+"  diff="+String.format("%-8.3f",minDiff)+"\n");                                              
                            }                            
                        }

                    }
                    
                    //System.out.println("   In ENSDF #2: g.FLRecord()="+g.FLRecord()+"  g.EF="+g.EF()+" li.EF="+li.EF()+" li.ES="+li.ES()+" fl_i="+fl_i);
                    
                    //check if gamma energy match level-energy difference
                    if(fl_i>=0 && eg>0 && li.isTrueERPF() && lf.isTrueERPF()) {     
                        lf=levelsV.elementAt(fl_i);
                        
                        ef=lf.ERPF();  
                        def=EnsdfUtil.de(lf);
                         
                        float sigma=(float) Math.sqrt(deg*deg+dei*dei+def*def);
                        
                        float de=10.0f;
                        if(g.ES().contains("."))
                            de=5.0f;
                        
                        diff=Math.abs(ei-ef-eg);
                        
                        float eiegDiff=ei-eg;
                        float deiegDiff=(float) Math.sqrt(dei*dei+deg*deg);

                        //if(g.ES().contains("883.88")) 
                        //    System.out.println("ENDF 1715: ** sigma="+sigma+"  ** diff="+diff+" deg="+deg+" ei="+li.ES()+" dei="+dei+" ef="+lf.ES()+" def="+def);
                        
                        String tempMsg="",option="";
                        if(diff>de && diff>10*sigma) {
                            tempMsg+="FL="+String.format("%-8s",lf.ES())+" for gamma="+String.format("%-8s", g.ES())+" from level="+String.format("%-8s",li.ES())
                            +" not consistent with EL-EG=";
                            
                            option="E";
                            //fl_i=-1;
                        }else if(diff>5*sigma) {
                            tempMsg+="FL="+String.format("%-8s",lf.ES())+" for gamma="+String.format("%-8s", g.ES())+" from level="+String.format("%-8s",li.ES())
                            +" not consistent with EL-EG="; 
                            option="W";
                        }
                        
                        
                        if(tempMsg.length()>0) {                           
                            XDX2SDS x2s=new XDX2SDS(eiegDiff,deiegDiff);
                            tempMsg+=x2s.s()+" "+x2s.ds()+"\n";
                            
                            printToErrorMsg(msg,option,tempMsg);                            
                        }
                        
                    }
                }else if(Str.isNumeric(egs)){ /* if the continuation record doesn't exist, will get it from minimum energy difference */
                    float bigSigma=0,sigma=0,minSigma=0;
                    float minDiff=1000; 
                    int lmin=-1;
                    String temp="";
                                                            
                    Level closestFLev=(Level)EnsdfUtil.findClosestByEnergyValue(li.ERPF()-eg, sortedLevelsV);
                    
                    int start=0,end=j;
                    int N=10;
                    int iclosest=-1;

                    diff=0;
                    if(closestFLev!=null) {
                        closestEFS=closestFLev.ES();
                        
                        iclosest=levelsV.indexOf(closestFLev);
                        start=iclosest-N;
                        end=iclosest+N;
                        if(start<0)
                            start=0;
                        if(end>j)
                            end=j;
                    }
                    
                    
                    //debug
                    //if(g.ES().contains("248.580")) {
                    //    System.out.println(" li="+li.ES()+" li.ERPF()="+li.ERPF()+" gam="+g.ES()+" start="+start+" end="+end+" closestFL="+closestFLev.ES());
                        //for(Level l:sortedLevelsV)
                        //    System.out.println("     l="+l.ES()+" ERPF="+l.ERPF()+" EF="+l.EF());
                    //}
                    
                    //first, find a match under loose conditions (within a large range around the closest)
                    for( int l=start; l < end; l++){
                       
                       Level lf=(Level) levelsV.elementAt(l);

                       temp=lf.NNES().trim()+li.NNES().trim();
                                        
                       temp=temp.toUpperCase().replace("SN", "E").replace("SP", "E").replace("QA", "E");
                       temp=temp.toUpperCase().replace("S2N", "E").replace("S2P", "E");                     
                       
                       /* 
                       //debug
                       if(g.ES().equals("6193.5")) {
                       System.out.println("### In ENSDF 1664: find final level: temp="+temp+" EI="+li.ERPF()+" EIS="+li.ES()+" EF="+lf.ERPF()+" EFS="+lf.ES()+" diff="+diff+" xmin="+minDiff+" lmin="+lmin+" l="+l+" size="+levelsV.size());
                       System.out.println("     iclosest="+iclosest+" start="+start+" end="+end);
                       System.out.println("                            g.EF="+g.EF()+" g.ES="+g.ES()+" li.NNES="+li.NNES()+" lf.NNES="+lf.NNES()+"  li.ERF="+li.ERF()+" lf.ERF="+lf.ERF());
                       System.out.println("                   ei="+ei+" ef="+ef+" eg="+g.ES()+" recoilE="+recoilE+" li.TrueERPF="+li.isTrueERPF()+" lf.TrueERPF="+lf.isTrueERPF());
                       }
                       */
                       
                       if( lf.NNES().equals( li.NNES() ) || temp.toUpperCase().equals("E") || (li.isTrueERPF()&&lf.isTrueERPF()) ){                                                      
                           ef=lf.ERPF(); 
                           def=EnsdfUtil.de(lf);
                           
                           diff=Math.abs(ei-ef-eg);
                           
                           sigma=(float)Math.sqrt(deg*deg+dei*dei+def*def);
                           
                           float de=5.0f;
                           if(egs.contains("."))
                               de=2.0f;
                           
                           //find a match (lmin) within a big range
                           //////////////////////////////////////////////
                           bigSigma=Math.max(10*sigma, 50*assumedDEG);
                           bigSigma=Math.max(bigSigma, de);
                           //////////////////////////////////////////////
                           
                           if(diff<bigSigma && diff<minDiff) {
                               minDiff=diff ; 
                               lmin=l; 
                               minSigma=sigma;
                           }

                           
                           /*
                           //debug
                           if(g.ES().contains("399.022")) {
                           System.out.println("@@@ In ENSDF 1695: find final level: temp="+temp+" EI="+li.ERPF()+" EIS="+li.ES()+" EF="+lf.ERPF()+" EFS="+lf.ES()+" diff="+diff+" xmin="+minDiff+" lmin="+lmin+" l="+l+" size="+levelsV.size());
                           //System.out.println("     iclosest="+iclosest+" ES="+levelsV.elementAt(iclosest).ES()+" start="+start+" end="+end+" assumedDEG="+assumedDEG);
                           System.out.println("                            g.EF="+g.EF()+" g.ES="+g.ES()+" li.NNES="+li.NNES()+" lf.NNES="+lf.NNES()+"  li.ERF="+li.ERF()+" lf.ERF="+lf.ERF());
                           System.out.println("                   ei="+ei+" ef="+ef+" eg="+g.ES()+" recoilE="+recoilE+" sigma="+sigma+" bigSigma="+bigSigma+" minSigma="+minSigma+" assumeddDEG="+assumedDEG);
                           }
                           */
                           
                       }
                                           
                    }
                    
                    //then, refine conditions to find the good match around the previously found one                    
                    int nsigma=3;
                    if( lmin >= 0){
                        boolean goodFL=false;

                        float minDiff0=minDiff;
                        float minSigma0=minSigma;
       
                        int index=-1, goodIndex=-1,minGoodIndex=-1;
                        
                        int n=3;//number of levels to check to the left and right of lmin, if lmin is not the final level
                        int[] indexOffsets=new int[n*2];
                        for(int i=0;i<n;i++) { 
                            indexOffsets[2*i]=(i+1);  //1,-1,2,-2,3,-3;
                            indexOffsets[2*i+1]=-(i+1); 
                        }
                        
                        int ntries=0;
                        
                        sigma=0;
                        minDiff=1000;
                        minSigma=0;
                        index=lmin;
                        while(ntries<=6) {
                            try {
                                if(index<0) {
                                    index=lmin+indexOffsets[ntries];
                                    ntries++; 
                                    continue;
                                }
                                
                                Level lf=(Level) levelsV.elementAt(index);
   
                                ef=lf.ERPF();
                                def=EnsdfUtil.de(lf);
                                
                                diff=Math.abs(ei-ef-eg);
                                                                 
                                sigma=(float)Math.sqrt(deg*deg+dei*dei+def*def);
                                
      

                                
                                goodFL=false;
                                if(diff<2*nsigma*sigma)
                                    goodFL=true;
                                
                                /*
                                //debug
                                if(g.ES().contains("239.21")) {
                                System.out.println("@@@ In ENSDF 1739: find final level: temp="+temp+" EI="+li.ERPF()+" EIS="+li.ES()+" EF="+lf.ERPF()+" EFS="+lf.ES()+" diff="+diff+" xmin="+minDiff+" lmin="+lmin);
                                //System.out.println("     iclosest="+iclosest+" ES="+levelsV.elementAt(iclosest).ES()+" start="+start+" end="+end+" assumedDEG="+assumedDEG);
                                System.out.println("                            g.EF="+g.EF()+" g.ES="+g.ES()+" li.NNES="+li.NNES()+" lf.NNES="+lf.NNES()+"  li.ERF="+li.ERF()+" lf.ERF="+lf.ERF());
                                System.out.println("                   ei="+ei+" ef="+ef+" eg="+g.ES()+" recoilE="+recoilE+" sigma="+sigma+" assumeddDEG="+assumedDEG+"  goodFL="+goodFL+" index="+index);
                                }
                                */
                                
                                if(goodFL) {                        
                                    //further check XREF for adopted dataset
                                    String si=li.newXREFS();
                                    String sf=lf.newXREFS();
                                    
                                    goodIndex=index;
                                    
                                    //debug
                                    //if(g.ES().contains("1321")) 
                                    //      System.out.println("1 g="+g.ES()+" li="+li.ES()+"  si="+si+" lf="+lf.ES()+" sf="+sf+" lmin="+lmin+" index="+index+" level size="+levelsV.size()+" ntries="+ntries+" indexOffsets[ntries]="+indexOffsets[ntries]);
                                    
                                    
                                    //check if XREF match for Adotped Levels
                                    if(si.length()>0 && sf.length()>0 && !Str.containAny(si,sf)) {//for Adopted Levels, Gammas, no common XREF, li and lf not from the same dataset
                                        goodFL=false;
                                        if(goodIndex==lmin)
                                            possible_fl_i=lmin;//for cases where XREF not matching due to misprint
                                    
                                    }else {                                        
                                        if(goodIndex==lmin) {
                                            minDiff=diff;
                                            minGoodIndex=goodIndex;
                                            minSigma=sigma;
                                            
                                            break;
                                        }
                                        
                                        if(diff<minDiff) {
                                            minDiff=diff;
                                            minGoodIndex=goodIndex;
                                            minSigma=sigma;
                                        }  

                                    }                                   
                                
                                }//end outer if(goodFL)
                                
                                index=lmin+indexOffsets[ntries];
                                ntries++; 
                                
                                
                            }catch(Exception e) {
                                break;
                            }

                        }//end while
                            
                        
                        //debug
                        //if(g.ES().equals("1411.3")) {
                        //System.out.println("In ENSDF line 1327: g="+g.ES()+" recoilE="+recoilE+" xmin="+xmin+" lmin="+lmin+" index="+index+" sigma="+sigma);
                        //}
                           
                        if(minGoodIndex>=0){                          
                            fl_i=minGoodIndex;                            
                            if(minDiff>nsigma*minSigma && minSigma>0){
                                Level lf=(Level) levelsV.elementAt(fl_i);
                                printToErrorMsg(msg,"W","no final level within "+nsigma+"*sigma="+String.format("%.4f", nsigma*minSigma)+" keV for gamma="+String.format("%-8s", g.ES())+" from level="+li.ES()+"\n");
                                printToErrorMsg(msg," ","    assigned final level="+String.format("%-8s",lf.ES())+"    E(level) diff="+String.format("%-8.3f",li.ERPF()-lf.ERPF())+"\n");
                            }                   
                        }else {
                            //if(g.ES().contains("46.52")) System.out.println(" minDiff0="+minDiff0+" minSigma0="+minSigma0);
                            float de=5.0f;
                            if(egs.contains("."))
                                de=2.0f;
                            
                            if(minDiff0>2*nsigma*minSigma0 && minDiff0<de) {
                                fl_i=lmin;
                                Level lf=(Level) levelsV.elementAt(fl_i);
                                printToErrorMsg(msg,"W","no final level within "+(2*nsigma)+"*sigma="+String.format("%.4f", 2*nsigma*minSigma0)+" keV for gamma="+String.format("%-8s", g.ES())+" from level="+li.ES()+"\n");
                                printToErrorMsg(msg," ","    assigned final level="+String.format("%-8s",lf.ES())+"    E(level) diff="+String.format("%-8.3f",li.ERPF()-lf.ERPF())+"\n");                               
                            }else
                                fl_i=-1;
                        }

                    }  

                    /* if still not found... */
                    
                    if( fl_i < 0 ){

                      /* The case of "2578.2+X" going to "2578.2" with an "X" gamma without the FL="2578.2" */

                      if( li.NNES().length() > 0 ){
                        int l=0;
                        while( l < nLevels() && fl_i < 0 ){
                          Level lf=(Level) levelsV.elementAt(l);
                          
                          //System.out.println("In ENSDF line 1243: li.NNES="+li.NNES()+"  li.NES="+li.NES()+"  lf.ES="+lf.ES());
                          
                          if( lf.ES().equals( li.NES() ) ) fl_i=l; 
                          l++;           
                        }

                      }
                    }
                }else{//EG="X"
                    egs=egs.toUpperCase();
                    eis=li.ES().toUpperCase();//1234.5+X, FL should be 1234.5

                    fl_i=-1;
                    if(egs.length()==1 && Character.isLetter(egs.charAt(0)) && eis.contains(egs)) {
                        String s=eis.replace(egs,"").replace("+","").trim();
                        if(s.isEmpty()) {
                            if(levelsV.get(0).ERPF()==0) {
                                fl_i=0;
                            }
                        }else {
                            for(int l=j-1;l>=0;l--) {
                                Level lf=(Level) levelsV.elementAt(l);
                                if( lf.ES().toUpperCase().equals(s) ) {
                                    fl_i=l;     
                                    break;
                                }else {
                                    try {
                                        float e1=Float.parseFloat(lf.ES());
                                        float e2=Float.parseFloat(s);
                                        if(Math.abs(e1-e2)<Math.max(3.0,lf.DEF())) {
                                            fl_i=l;
                                            break;
                                        }
                                        
                                    }catch(Exception e) {
                                        
                                    }
                                }
                            }                            
                        }
  
                    }                       
                }
                
                g.setFLI( fl_i );
                
                if(fl_i>=0){
                    Level fLev=levelsV.elementAt(fl_i);
                    
                    boolean isGood=false;
                    if(tempGamFLMap.containsValue(fl_i)) {
                        //there is already a gamma (with close but lower E) of this level which has been assigned the same final level.
                        //Than means they should have final levels with close E
                        
                        //System.out.println(" ENSDF 1860: EL="+li.ES()+"  EG="+g.ES()+" EF="+fLev.ES()+" F(L+1)="+levelsV.elementAt(fl_i+1).ES());//+" F(L-1)="+levelsV.elementAt(fl_i-1).ES());
                        //System.out.println(" isComparable with F(L+1): "+EnsdfUtil.isComparableEnergyValue(li.ERPF()-g.EF(), levelsV.elementAt(fl_i+1).ERPF(), 10));
                        //System.out.println(" isComparable with F(L-1): "+EnsdfUtil.isComparableEnergyValue(li.ERPF()-g.EF(), levelsV.elementAt(fl_i-1).ERPF(), 10));
                        //System.out.println(" tempGamFLMap.containsValue(fl_i+1)="+tempGamFLMap.containsValue(fl_i+1));
                        
                        Gamma prevGam=null;
                        for(int key:tempGamFLMap.keySet()) {
                            int tempFLI=tempGamFLMap.get(key);
                            if(tempFLI==fl_i) {
                                prevGam=li.gammaAt(key);
                                break;
                            }
                                
                        }
                        
                        try {
                            
                            Level nextFLev=levelsV.elementAt(fl_i+1);
                            Level prevFLev=null;
                            if(fl_i>0)
                                prevFLev=levelsV.elementAt(fl_i-1);
                            
                            float nextDE=EnsdfUtil.de(g,li,nextFLev);
                            float prevDE=EnsdfUtil.de(g,li,prevFLev);
                            
                            if(EnsdfUtil.isComparableEnergyValue(li.ERPF()-g.EF(), nextFLev.ERPF(), 3*nextDE)) {
                               
                                g.setFLS(fLev.ES());
                                g.setJFS(fLev.JPiS());
                                
                                fLev.setIsFinalLevel(); 
                                
                                if(!tempGamFLMap.containsValue(fl_i+1)) {
                                    Level tempFLev=levelsV.elementAt(fl_i+1);
                                    for(int key:tempGamFLMap.keySet()) {
                                        int tempFLI=tempGamFLMap.get(key);
                                        if(tempFLI==fl_i) {
                                            
                                            //get smaller gam which has been assigned to fl_i and re-assign fl_i+1 to it
                                            Gamma gam=li.gammaAt(key);
                                            gam.setFLS(tempFLev.ES());
                                            gam.setJFS(tempFLev.JPiS());
                                            gam.setFLI(fl_i+1);
                                            
                                            tempFLev.setIsFinalLevel(); 
                                            
                                            tempGamFLMap.put(key, fl_i+1);
                                            tempGamFLMap.put(k, fl_i);
                                            
                                            isGood=true;
                                            break;
                                        }
                                            
                                    }
                                }
    
                            }else if(fl_i>0 && EnsdfUtil.isComparableEnergyValue(li.ERPF()-g.EF(), prevFLev.ERPF(), 3*prevDE)) {
                                if(!tempGamFLMap.containsValue(fl_i-1)) {
                                    Level tempFLev=levelsV.elementAt(fl_i-1);
                                    
                                    g.setFLS(tempFLev.ES());
                                    g.setJFS(tempFLev.JPiS());
                                    g.setFLI(fl_i-1);
                                    
                                    tempFLev.setIsFinalLevel(); 
                                    
                                    tempGamFLMap.put(k, fl_i-1);
                                    
                                    isGood=true;
                                }
    
                            }
                        }catch(Exception e) {}
                        
                        if(!isGood) {
       
                            float diff1=Math.abs(ei-fLev.ERPF()-prevGam.EF());//prevGam has been assigned to fl_i
                            float diff2=Math.abs(ei-fLev.ERPF()-g.EF());
                            if(diff2<diff1) {
                                //re-assign current gam to fl_i, and no fl for prevGam
                                g.setFLS(fLev.ES());
                                g.setJFS(fLev.JPiS());
                                g.setFLI(fl_i);
                                
                                prevGam.setFLS("");
                                prevGam.setJFS("");
                                prevGam.setFLI(-1);
                                
                                printToErrorMsg(msg,"E","no final level found for gamma="+String.format("%-8s", prevGam.ES())+" from level="+li.ES()+"\n");
                                printToErrorMsg(msg," ","  the closest final level is "+fLev.ES()+" but is re-assigned to gamma="+g.ES()+"\n");
                                   
                            }else {
                                printToErrorMsg(msg,"E","no final level found for gamma="+String.format("%-8s", g.ES())+" from level="+li.ES()+"\n");
                                printToErrorMsg(msg," ","  the closest final level is "+fLev.ES()+" but has been assigned to gamma="+prevGam.ES()+"\n");
                                
                                g.setFLI(-1);                                
                            }

                        }else {
                            
                            int tempFLI=tempGamFLMap.get(k);
                            Level tempFLev=levelsV.elementAt(tempFLI);
                            
                            printToErrorMsg(msg,"W","multiple final levels found for gamma="+g.ES()+". FL="+tempFLev.ES()+" is assigned.\n");
                            printToErrorMsg(msg," ","  Please check it and use \"FL=\" to specify the final level\n");
                        }

                    }else {
                        
                        tempGamFLMap.put(k, fl_i);
                                                
                        g.setFLS(fLev.ES());
                        g.setJFS(fLev.JPiS());
                        
                        fLev.setIsFinalLevel(); 
                    }

                    

                }else {//fl_i<0
                    if(possible_fl_i>=0) {
                        Level fLev=levelsV.elementAt(possible_fl_i);

                        printToErrorMsg(msg,"W","no final level found for gamma="+String.format("%-8s", g.ES())+" from level="+li.ES()+"\n");
                        printToErrorMsg(msg," ","  the closest final level is "+fLev.ES()+", but XREF does not match. Please check if XREF is missing\n");      
                        
                        g.setFLI(possible_fl_i);
                    }else {                      
                        String tempMsg="no final level found for gamma="+String.format("%-8s", g.ES())+" from level="+String.format("%-8s", li.ES());
                        if(li.isTrueERPF() && ei-eg>0)
                            tempMsg+=" EL-EG="+String.format("%-8.1f",ei-eg);
                        
                        if(closestEFS.length()>0)
                            tempMsg+=" closest FL="+closestEFS+"\n";     
                        else
                            tempMsg+="\n";
                        
                        printToErrorMsg(msg,"E",tempMsg);
                    }

                }
                
                if(msg.length()>0)
                    errorMsg.append(msg);
                
                //if(g.ES().contains("487.3")) System.out.println(" fli="+fl_i);
                
            }//end gamma loop
        }//end level loop
        
        
        
        //debug
        //for(Level l:levelsV)
        //    for(Gamma g:l.GammasV())
        //        System.out.println(" l="+l.ES()+" g="+g.ES()+" FL="+levelsV.get(g.FLI()).ES());
  }
  
/* --------------------------------------------------------------------------- */

  private void parseComments() throws Exception{

/* The Comments */
    poscom=extractTopComments("C");
    
    extractTopComments("D");//document
    
    if(sysOut) System.out.println("Done with the regular comments");

    analyzeComments();

  }
  
  /*
   * option="D" for document, otherwise for comments. Both savd as comment objects
   */
  private Vector<Integer> extractTopComments(String option) throws Exception {
        Vector<Integer> poscom=new Vector<Integer>(); /* comment positions */
        option=option.toUpperCase();
        if(!option.equals("D"))
            option="C";
        
        String lineType="";
        for( int i=0 ; i < furl ; i++){     
                                                                   
          /* finding comments positions */
          lineType=lineAt(i).substring(5,7).toUpperCase();
          if( lineType.equals(" "+option)) 
              poscom.addElement( new Integer(i) );
        }
        
        for( int i=0 ; i < poscom.size() ; i++){

          int ki= ((Integer) poscom.elementAt(i)).intValue(); 
          int kf=furl; 
          if( i < ( poscom.size()-1 ) ) kf=((Integer) poscom.elementAt(i+1)).intValue() ;
          Vector<String> cline=new Vector<String>();
          for( int j=ki; j < kf; j++){ 

                lineType=lineAt(j).substring(6,7).toUpperCase();
                if(lineType.equals(option)){

                      /* Comment lines will be processed only if there is more than ONE trivial character: */

                      //if( lineAt(j).trim().length() > 10  ) 
                          cline.addElement( lineAt(j) );   
                }
          }
          if( cline.size() > 0 ){      /* if it is not an empty comment */
              
            
            Comment c=new Comment();
            c.setValues(cline);
            
            if(option.equals("D"))
                docV.addElement(c);
            else
                comV.addElement(c);
     
            //debug
            //System.out.println("In ENSDF: bigC="+c.isBigC()+" head="+c.head()+" nHeads="+c.nHeads()+" body="+c.body());

          }

        }   
        
        return poscom;
  }
  
  protected void analyzeComments() throws Exception {
      
      //regular comments

      String pseudoLevelFlags="";
      boolean isAllPseudo=false,hasCheckedAllPseudo=false;  
      String omittedLevelFlags="",omittedGammaFlags="";//for levels/gammas not to be included in Adopted dataset
      boolean isAllLevelOmitted=false,isAllGammaOmitted=false;
        
      float minELOfPseudo=-1;
      
      for(int i=0;i<comV.size();i++) {
          Comment c=comV.get(i);
           
          String type=c.type().trim();                       
          if(c.headV().contains("E")) {
              String s=c.rawBody().toUpperCase();    
              String s1=Str.firstSentence(s);
              String s2=s.substring(s1.length());
              
                if(type.equals("L")) {
                    
                    if(s.contains("PSEUDO LEVEL")||s.contains("PSEUDO-LEVEL")||s.contains("PSEUDOLEVEL")) {
                        boolean isPseudo=true;
                        int n1=s.indexOf("EXCEPT");
                        if(n1>0) {
                            int n2=s.indexOf("PSEUDO");
                            if(n2>n1) {
                                s=s.substring(n1+6,n2);
                                s=s.replace("THE", "").replace("FOR", "").replace("THOSE","").trim();//EXCEPT FOR THE PSEUDO LEVELS
                                if(s.isEmpty())
                                    isPseudo=false;
                            }
                        }else if(s.contains("ABOVE") || s.contains("UP TO")) {
                            isAllPseudo=false;
                            hasCheckedAllPseudo=true;
                            
                            //there could be pseudo levels with a flag or all levels above a EL are pseudo levels, in the wording of EL comment
                            n1=s.indexOf("UP TO");
                            if(n1>0) {
                            	String ns=s.substring(n1+5).trim();
                            	if(ns.length()>3 && Str.isDigits(ns.substring(0,3)) && ns.contains("PSEUDO") ){
                            		ns=Str.getLeadingNumber(ns, 0);
                            		minELOfPseudo=EnsdfUtil.s2f(ns);
                            	}
                            }
                            
                            
                            if(minELOfPseudo<0) {
                            	n1=s.indexOf("ABOVE");
                                if(n1>0) {
                                	String ns=s.substring(n1+5).trim();
                                	if(ns.length()>3 && Str.isDigits(ns.substring(0,3)) && ns.contains("PSEUDO") ){
                                		ns=Str.getLeadingNumber(ns, 0);
                                		minELOfPseudo=EnsdfUtil.s2f(ns);
                                	}
                                }
                            }
                        }
                        
                        if(isPseudo) {
                            for(int k=0;k<c.headV().size();k++) {
                                String h=c.headAt(k);
                                if(!h.startsWith("E"))
                                    continue;
                                
                                String flag=c.flagV().get(k);
                                if(!flag.isEmpty()) {
                                    pseudoLevelFlags+=flag;
                                    isAllPseudo=false;
                                    hasCheckedAllPseudo=true;
                                }else if(!hasCheckedAllPseudo) {
                                    isAllPseudo=true;
                                    break;
                                }
                            }
                        }
                    }           
                }

               
                //if(this.dsid0_s.contains("17.66"))
                //  System.out.println("ENSDF 2820: isAllLevelOmitted="+isAllLevelOmitted+" isAllPseudo="+isAllPseudo+" com="+c.body());
                
                
                //check if the "E" comment says this record will be not included in adopted
                if(!isAllLevelOmitted && type.equals("L")) {
 
                    if(s1.contains("NOT") && s1.contains("ADOPTED")) {
                          s1=s1.replace(" THE ", " ").replace(" BE ", " ").replace("  ", " ").trim();
                          String[] a= {"INCLUDED","USED","ADOPTED"};

                          boolean isOmitted=false;
                          for(int k=0;k<a.length;k++) {
                              if(s1.contains("NOT "+a[k]+" IN ADOPTED")) {
                                  isOmitted=true;
                                  break;
                              }
                          }               
                          
                          if(isOmitted) {
                              for(int k=0;k<c.headV().size();k++) {
                                String h=c.headAt(k);
                                if(!h.startsWith("E"))
                                    continue;
                                
                                String flag=c.flagV().get(k);
                                if(!flag.isEmpty())
                                    omittedLevelFlags+=flag;
                                else {
                                    isAllLevelOmitted=true;
                                    break;
                                }
                              }
                          }
                    }else if(s2.contains("ALL LEVELS") && s2.contains("NOT") && s2.contains("ADOPTED")) {
                        s2=s2.replace(" THE ", " ").replace(" BE ", " ").replace("  ", " ").trim();
                        String[] a= {"INCLUDED","USED","ADOPTED"};

                        boolean isOmitted=false;
                        for(int k=0;k<a.length;k++) {
                            if(s2.contains("NOT "+a[k]+" IN ADOPTED")) {
                                isOmitted=true;
                                break;
                            }
                        }               
                        
                        if(isOmitted) {
                            for(int k=0;k<c.headV().size();k++) {
                              String h=c.headAt(k);
                              if(!h.startsWith("E"))
                                  continue;
                              
                              String flag=c.flagV().get(k);
                              if(!flag.isEmpty())
                                  omittedLevelFlags+=flag;
                              else {
                                  isAllLevelOmitted=true;
                                  break;
                              }
                            }
                        }
                    }

                }
                //if(this.dsid0_s.contains("17.66"))
                //    System.out.println("ENSDF 2856: isAllLevelOmitted="+isAllLevelOmitted+" isAllPseudo="+isAllPseudo);

                if(!isAllGammaOmitted && type.equals("G")) {
           
                    if(s1.contains("NOT") && s1.contains("ADOPTED")) {
                          s1=s1.replace(" THE ", " ").replace(" BE ", " ").replace("  ", " ").trim();
                          String[] a= {"INCLUDED","USED","ADOPTED"};

                          boolean isOmitted=false;
                          for(int k=0;k<a.length;k++) {
                              if(s1.contains("NOT "+a[k]+" IN ADOPTED")) {
                                  isOmitted=true;
                                  break;
                              }
                          }               
                          
                          if(isOmitted) {
                              for(int k=0;k<c.headV().size();k++) {
                                String h=c.headAt(k);
                                if(!h.startsWith("E"))
                                    continue;
                                
                                String flag=c.flagV().get(k);
                                if(!flag.isEmpty())
                                    omittedGammaFlags+=flag;
                                else {
                                    isAllGammaOmitted=true;
                                    break;
                                }
                              }
                          }
                    }           
                }    
          }else if(c.head().equals("S") && c.rawBody().startsWith("LABEL")) {
        	  c.setImplicit();
          }
          
      }
      
        /* The intensity gamma Comments: */

        int ngifa=0;
        int ngifb=0;
        int ngifc=0;
        int gq=0;
        int bq=0;
        int eq=0;
        int aq=0;
        int dpq=0;

        
        for( int j=0; j < nLevels() ; j++ ){
            Level l=levelAt(j);
             
            SDS2XDX offset=null;
            boolean addRangeFlag=false;
            
            if(l.ms_s.equals("R")) {
            	addRangeFlag=true;
            }else if(!Str.isNumeric(l.ES()) && l.ES().contains("+") && l.NNES().length()>0){
            	String key=l.NNES();
            	offset=labelOffsetMap.get(key);
            	
                if(offset==null || !Str.isNumeric(offset.s()) ) 
                	addRangeFlag=true;
                else if (Str.isNumeric(offset.ds()) && offset.dx()>=offset.x()){
                	addRangeFlag=true;
                }else if(!Str.isNumeric(offset.ds())&&offset.ds().length()>0) {
                	addRangeFlag=true;
                }
            }
        
            if(isAllPseudo || Str.containAny(l.flag(), pseudoLevelFlags) || (minELOfPseudo>0&&l.EF()>minELOfPseudo) )
                l.setIsPseudo(true);

            if(isAllLevelOmitted || Str.containAny(l.flag(), omittedLevelFlags) || l.isPseudo())
                l.setIsGotoAdopted(false);
            
            /*
            if(this.dsid0_s.contains("17.66")) {
            //if(j==0)
              System.out.println("ENSDF 2991: ES="+l.ES()+" isAllLevelOmitted="+isAllLevelOmitted+" isAllPseudo="+isAllPseudo
            		  +"  contain pseudo flag="+Str.containAny(l.flag(), pseudoLevelFlags)+" contain omitted flag="+Str.containAny(l.flag(), omittedLevelFlags));
              System.out.println("   lev is Pseudo="+l.isPseudo()+" isGoToAdopted="+l.isGotoAdopted);
            }
            */
            for( int k=0; k < l.nGammas() ; k++ ){
                Gamma g=l.gammaAt(k);
                if( g.flag().indexOf("*") >=0 ){
                   ngifa++;
                }
                else if( g.flag().indexOf("&") >=0  ){
                   ngifb++;
                }
                else if( g.flag().indexOf("@") >=0   ){
                   ngifc++;
                }
                if( g.q().equals("?")){
                    gq++;
                    g.addFlag("?");
                    if(!gammaFlags.contains("?"))
                            gammaFlags+="?";
                }
                
                if(!l.isGotoAdopted() || isAllGammaOmitted || Str.containAny(g.flag(), omittedGammaFlags))
                    g.setIsGotoAdopted(false);
            }
            
            for( int k=0; k< l.nBetas() ; k++ ){
                Beta b=l.betaAt(k);
                if(b.q().equals("?")){
                    bq++;
                    b.addFlag("?");
                    if(!decayFlags.contains("?"))
                        decayFlags+="?";
                }
                
                //System.out.println(b.ES());
                
                if(!Str.isNumeric(b.ES()) && addRangeFlag){
                    b.addFlag("_");//reserved "_" flag for level like SN+X, 1234.5+X
                    if(!decayFlags.contains("_"))
                        decayFlags+="_";
                }
            }
            for( int k=0; k< l.nECBPs() ; k++ ){
                ECBP e=l.ECBPAt(k);
                if(e.q().equals("?")){
                    eq++;
                    e.addFlag("?");
                    if(!decayFlags.contains("?"))
                        decayFlags+="?";
                }
                
                if(!Str.isNumeric(e.ES()) && addRangeFlag){
                    e.addFlag("_");//reserved "_" flag for level like SN+X, 1234.5+X
                    if(!decayFlags.contains("_"))
                        decayFlags+="_";
                }
            }
            for( int k=0; k< l.nAlphas() ; k++ ){
                Alpha a=l.alphaAt(k);
                if(a.q().equals("?")){
                    aq++;
                    a.addFlag("?");
                    if(!decayFlags.contains("?"))
                        decayFlags+="?";
                }
                
                if(!Str.isNumeric(a.ES()) && addRangeFlag){
                    a.addFlag("_");//reserved "_" flag for level like SN+X, 1234.5+X
                    if(!decayFlags.contains("_"))
                        decayFlags+="_";
                }
            }
            
            for(int k=0;k<l.nDParticles();k++){
                DParticle dp=l.DParticleAt(k);
                if(dp.q().equals("?")){
                    dpq++;
                    dp.addFlag("?");
                    
                    if(!delayFlags.contains("?"))
                        delayFlags+="?";
                }
            }
        }
        
        
        //for unplaced radiations
        
        for(int i=0;i<ugV.size();i++){
            Gamma g=ugV.elementAt(i);

            if( g.flag().indexOf("*") >=0 ){
                ngifa++;
             }
             else if( g.flag().indexOf("&") >=0  ){
                ngifb++;
             }
             else if( g.flag().indexOf("@") >=0   ){
                ngifc++;
             }
             if( g.q().equals("?")){
                 gq++;
                 g.addFlag("?");
                 if(!gammaFlags.contains("?"))
                        gammaFlags+="?";
             }
             
             g.addFlag("^");//reserve "^" flag for unplaced radiations
             if(!gammaFlags.contains("^"))
                gammaFlags+="^";
             
            
            for(int m=0;m<g.flag().length();m++){
                String ch=g.flag().substring(m,m+1);

                if(gammaFlags.contains(ch))
                    continue;
              
                gammaFlags+=ch;
            }
        }
        
        for( int k=0; k< ubV.size() ; k++ ){
            Beta b=ubV.elementAt(k);
            if(b.q().equals("?")){
                bq++;
                b.addFlag("?");
                if(!decayFlags.contains("?"))
                    decayFlags+="?";
            }
            
            for(int m=0;m<b.flag().length();m++){
                String ch=b.flag().substring(m,m+1);
                if(decayFlags.contains(ch))
                    continue;
              
                decayFlags+=ch;
            } 
        }
        for( int k=0; k< ueV.size() ; k++ ){
            ECBP e=ueV.elementAt(k);
            if(e.q().equals("?")){
                eq++;
                e.addFlag("?");
                if(!decayFlags.contains("?"))
                    decayFlags+="?";
            }
            
            for(int m=0;m<e.flag().length();m++){
                String ch=e.flag().substring(m,m+1);
                if(decayFlags.contains(ch))
                    continue;
              
                decayFlags+=ch;
            } 
        }
        for( int k=0; k< uaV.size() ; k++ ){
            Alpha a=uaV.elementAt(k);;
            if(a.q().equals("?")){
                aq++;
                a.addFlag("?");
                if(!decayFlags.contains("?"))
                    decayFlags+="?";
            }
            
            for(int m=0;m<a.flag().length();m++){
                String ch=a.flag().substring(m,m+1);
                if(decayFlags.contains(ch))
                    continue;
              
                decayFlags+=ch;
            } 
        }
        
        for(int k=0;k<udpV.size();k++){
            DParticle dp=udpV.elementAt(k);
            if(dp.q().equals("?")){
                dpq++;
                dp.addFlag("?");
                
                if(!delayFlags.contains("?"))
                    delayFlags+="?";
            }
            
            for(int m=0;m<dp.flag().length();m++){
                String ch=dp.flag().substring(m,m+1);
                if(delayFlags.contains(ch))
                    continue;
              
                delayFlags+=ch;
            } 
        }
        
        //add generic footnote comment for the column of total conversion coefficients from BrIcc
        if(nccgam>0 && !isRelabeled("G","CC")){
            boolean skip=false;
        
            for(Comment c:comV) {
                String head=c.head().replace(" ", "");
                int n=head.indexOf("CC");
                
                //if there is already a general footnote (not flagged) for CC, then not print the generic footnote for CC below
                if(n>=0 && (head.length()==n+2 || head.charAt(n+2)!='(')) {
                    skip=true;
                    break;
                }
            }
             
            if(!skip) {
                
                Vector<String> lines=new Vector<String>();
                
                
                //String s=" CG CC$Total conversion coefficients, unless explicitly states, are the values from BrIcc with Frozen Orbital Approximation (2008Ki07) based on assigned multipolarities, "
                //      + "missing ratios, |g-ray energies.";
                //lines.addElement(nucleus().nameENSDF()+s);
                
                //lines.addElement(nucleus().nameENSDF()+" CG CC$Total conversion coefficients, unless explicitly stated, are the");
                //lines.addElement(nucleus().nameENSDF()+"2CG values from BrIcc with Frozen Orbital Approximation (2008Ki07) based");
                //lines.addElement(nucleus().nameENSDF()+"3CG on assigned multipolarities, mixing ratios, and |g-ray energies.");

                  lines.addElement(nucleus().nameENSDF()+" CG CC$Total theoretical internal conversion coefficients, calculated");
                  lines.addElement(nucleus().nameENSDF()+"2CG using the BrIcc code (2008Ki07) with \"Frozen Orbitals\" approximation"); 
                  lines.addElement(nucleus().nameENSDF()+"3CG based on |g-ray energies, assigned multipolarities, and mixing");
                  lines.addElement(nucleus().nameENSDF()+"4CG ratios, unless otherwise specified.");
                  
                  Comment c=new Comment(); c.setValues(lines); c.setImplicit(); comV.addElement(c);             

            }
        }
        
        if( ngifa > 0 ){
          Vector<String> vjunk=new Vector<String>();
          vjunk.addElement(nucleus().nameENSDF()+" CG E(*)$Multiply placed" );
          Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c); 
        }
        if( ngifb > 0 ){
          Vector<String> vjunk=new Vector<String>();
          vjunk.addElement(nucleus().nameENSDF()+" CG E(&),RI(&)$Multiply placed with undivided intensity" );
          Comment ce=new Comment(); ce.setValues(vjunk);  ce.setImplicit(); comV.addElement(ce);
          //vjunk.clear();
          //vjunk.addElement(nucleus().nameENSDF()+" CG RI(&)$Multiply placed with undivided intensity" );
          //Comment ci=new Comment(); ci.setValues(vjunk);  ci.setImplicit(); comV.addElement(ci);
        }
        if( ngifc > 0 ){
          Vector<String> vjunk=new Vector<String>();
          vjunk.addElement(nucleus().nameENSDF()+" CG E(@),RI(@)$Multiply placed with intensity suitably divided" );
          Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
          //vjunk.clear();
          //vjunk.addElement(nucleus().nameENSDF()+" CG RI(@)$Multiply placed with intensity suitably divided" );
          //Comment ci=new Comment(); ci.setValues(vjunk); ci.setImplicit(); comV.addElement(ci);
        }   
        
        if( gq > 0){
            Vector<String> vjunk=new Vector<String>();
            vjunk.addElement(nucleus().nameENSDF()+" CG E(?)$Placement of transition in the level scheme is uncertain");
            Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
        }
        
        if(ugV.size()>0){
            Vector<String> vjunk=new Vector<String>();
            vjunk.addElement(nucleus().nameENSDF()+" cG E(^)$|g ray not placed in level scheme");
            Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c);   
        }
        
        if( bq > 0){
            Vector<String> vjunk=new Vector<String>();
            vjunk.addElement(nucleus().nameENSDF()+" CB E(?)$Existence of this branch is questionable");
            Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
        }
        if( eq > 0){
            Vector<String> vjunk=new Vector<String>();
            vjunk.addElement(nucleus().nameENSDF()+" CE E(?)$Existence of this branch is questionable");
            Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
        }
        if( aq > 0){
            Vector<String> vjunk=new Vector<String>();
            vjunk.addElement(nucleus().nameENSDF()+" CA E(?)$Existence of this branch is questionable");
            Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
        }
        
        if( dpq > 0){
            Vector<String> vjunk=new Vector<String>();
            
            vjunk.addElement(nucleus().nameENSDF()+" C"+promptOrDelayMarker+particleType+"E(?)$Placement of transition in the level scheme is uncertain");
            Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
        }
        
        if(decayFlags.contains("_")){
            Vector<String> vjunk=new Vector<String>();
            vjunk.addElement(nucleus().nameENSDF()+" C"+decayRecordType+" E(_)$Estimated for a range of levels");
            Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
        }
        
        /* The comment symbols */

        int nSups=56;
        String t[]=new String[nSups];

        t[0]="*"; t[1]="@"; t[2]="!";t[3]="&";t[4]="+";t[5]="a";t[6]="b";t[7]="c";t[8]="d";t[9]="e";
        t[10]="f"; t[11]="g"; t[12]="h";t[13]="i";t[14]="j";t[15]="k";t[16]="l";t[17]="m";t[18]="n";t[19]="o";
        t[20]="p"; t[21]="q"; t[22]="r";t[23]="s";t[24]="t";t[25]="u";t[26]="v";t[27]="w";t[28]="y";t[29]="z";t[30]="A";
        t[31]="B";t[32]="C";t[33]="D";t[34]="E";t[35]="F";t[36]="G";t[37]="H";t[38]="I";t[39]="J";
        t[40]="K";t[41]="L";t[42]="M";t[43]="N";t[44]="O";t[45]="P";t[46]="Q";t[47]="R";t[48]="S";t[49]="T";
        t[50]="U";t[51]="V";t[52]="W";t[53]="X";t[54]="Y";t[55]="Z";

        int nLevFN=0;  /* Number of levels footnotes */
        int nGamFN=0;
        int nAlpFN=0;
        int nBetFN=0;
        int nECBPFN=0;
        int nDPFN=0;

        for( int j=0; j<nComments();j++){
           if( commentAt(j).type().equals("L ") && commentAt(j).nHeads() > 0){
              commentAt(j).setSymbol( t[nLevFN] );
              nLevFN++;
              if(nLevFN >= nSups ) System.out.println("Problems with the Lev number of superscripts!!!!");
           }
           if( commentAt(j).type().equals("G ") && commentAt(j).nHeads() > 0){
              commentAt(j).setSymbol( t[nGamFN] );
              nGamFN++;
              if(nGamFN >= nSups ) System.out.println("Problems with the Gam number of superscripts!!!!");
           }
           if( commentAt(j).type().equals("A ") && commentAt(j).nHeads() > 0){
              commentAt(j).setSymbol( t[nAlpFN] );
              nAlpFN++;
              if(nAlpFN >= nSups ) System.out.println("Problems with the Alp number of superscripts!!!!");
           }
           if( commentAt(j).type().equals("B ") && commentAt(j).nHeads() > 0){
              commentAt(j).setSymbol( t[nBetFN] );
              nBetFN++;
              if(nBetFN >= nSups ) System.out.println("Problems with the Bet number of superscripts!!!!");
           }
           if( commentAt(j).type().equals("E ") && commentAt(j).nHeads() > 0){
              commentAt(j).setSymbol( t[nECBPFN] );
              nECBPFN++;
              if(nECBPFN >= nSups ) System.out.println("Problems with the ECBP number of superscripts!!!!");
           }
           if( commentAt(j).type().substring(1).trim().length() > 0 && commentAt(j).nHeads() > 0){
              commentAt(j).setSymbol( t[nDPFN] );
              nDPFN++;
              if(nDPFN >= nSups ) System.out.println("Problems with the DP number of superscripts!!!!");
           }
        }     
  }
  
/* --------------------------------------------------------------------------- */
  private void TableRecords(){ 

         String temp;
         int length=0;
         
         boolean inTable=false;
         tableV.clear();
        
         Vector<String> hline=new Vector<String>();  
         TableRecord t;
          
          //debug
          //System.out.println("In ENSDF: TableRecord: lastline="+lineAt(llbl));
          
          //llbl=the line# of the first  level
         int n=llbl;
         if(n==lines.size())
             n=n-1;
         
         int fixLength=0;
         
         for( int i=1 ; i <= n ; i++){
             
             
             //Note that each line has had trailing spaces added to make length=80 in fixLines() before this function call
             length=lineAt(i).trim().length();

             //debug
             //System.out.println("In ENSDF: TableRecord A: line="+lineAt(i)+" "+i+" length="+length);
             
             if(length<=5)          
                 continue;
             
             
             //Note that only when charAt(5)=='+', the line is concatenated to previous line and even for
             //lines of "2tL", "3tL", ..., new lines are started and heading spaces are kept. This is
             //different from the use of "2cL", "3cL", ..., for which the lines are concatenated to previous
             //lines
             if( lineAt(i).substring(6,7).toUpperCase().equals("T") ){
                 
                 if(!inTable){            
                     hline=new Vector<String>();
                     inTable=true;
                 }
                 
                 if(length>9){
                     char c=lineAt(i).charAt(5);
                     if(c=='+'){//concatenated as is (including heading whitespace) to previous line
                         fixLength=70;
                         temp=Str.rtrim(lineAt(i).substring(9));
                         
                         if(temp.charAt(0)=='$'){ 
                             temp=temp.substring(1);
                             fixLength--;
                         }
                         String newLine="";
                         if(hline.size()>0)
                             newLine=((String)hline.elementAt(hline.size()-1));
                         
                         if(newLine.trim().endsWith("\\")){
                             newLine=Str.rtrim(newLine);
                             newLine=newLine.substring(0, newLine.length()-1);
                         }
                         
                         newLine=newLine+fixLine(temp,fixLength);
                         hline.setElementAt(newLine, hline.size()-1);
                         
                         //debug
                         //System.out.println("In ENSDF.java: tableRecords: line 1273: line="+temp);
                         
                     }else{//start new lines if charAt(5)!='+', even for "2tL","3tL",... lines
                         
                         if(inTable && !lineAt(i).substring(7,8).equals(lineAt(i-1).substring(7,8))){                                
                             if( hline.size() > 0 ){                  
                                 t=new TableRecord();
                                 t.setValues(hline);
                                 tableV.addElement(t);
                                 hline=new Vector<String>();       
                                 //debug
                                 //System.out.println("In ENSDF: TableRecord: table.size1="+tableV.size());                    
                             }                 
                         }
                         
                        
                         temp=Str.rtrim(lineAt(i));
                         //debug
                         //System.out.println("In ENSDF.java: tableRecords: line 1273: line="+temp);
                         
                         fixLength=80;
                         if(temp.charAt(9)=='$'){
                             if(temp.length()>10)
                                 temp=temp.substring(0,9)+temp.substring(10);    
                             else
                                 temp=temp.substring(0,9); 
                             
                             fixLength--;
                         }
                         
                         
                         //add a new line and keep heading spaces for " tL","2tL", "3tL","AtL",...                 
                         
                         hline.addElement(fixLine(temp,fixLength));
                          
                         //debug
                         //System.out.println("In ENSDF: TableRecord: temp="+lineAt(i));                          
                     }
                 }else{//empty table record line
                     hline.addElement(lineAt(i));
                 }  
             }else{                      
                 if(inTable){                
                     inTable=false;
                     if( hline.size() > 0 ){
                         t=new TableRecord();
                         t.setValues(hline);
                         tableV.addElement(t);                  
                         hline=new Vector<String>();   
                         
                         //debug
                         //System.out.println("In ENSDF: TableRecord: table.size2="+tableV.size());                   
                     }
                 }
             }
             
             //debug
             //if(hline.size()>0) System.out.println("In ENSDF: TableRecord: hline.lastline="+hline.lastElement()+" "+i+" "+llbl+" size="+hline.size());
         }
            
        if(hline.size()>0 && inTable){
            t=new TableRecord();
            t.setValues(hline);
            tableV.addElement(t);
            //debug
            //System.out.println("In ENSDF: TableRecord: table.size3="+tableV.size());
        }
            

     }
  
 @SuppressWarnings("unused")
private void TableRecords_old(){// 5/9/2018 

     String temp;
     int length=0;
     
     boolean inTable=false;
     tableV.clear();
    
     Vector<String> hline=new Vector<String>();  
     TableRecord t;
      
      //debug
      //System.out.println("In ENSDF: TableRecord: lastline="+lineAt(llbl));
      
      //llbl=the line# of the first  level
     int n=llbl;
     if(n==lines.size())
         n=n-1;
     
     int fixLength=0;
     
     for( int i=1 ; i <= n ; i++){
         
         
         //Note that each line has had trailing spaces added to make length=80 in fixLines() before this function call
         length=lineAt(i).trim().length();

         //debug
         //System.out.println("In ENSDF: TableRecord A: line="+lineAt(i)+" "+i+" length="+length);
         
         if(length<=5)          
             continue;
         
         
         //Note that only when charAt(5)=='+', the line is concatenated to previous line and even for
         //lines of "2tL", "3tL", ..., new lines are started but just with some indentation. This is
         //different from the use of "2cL", "3cL", ..., for which the lines are concatenated to previous
         //lines
         if(length>9 &&  lineAt(i).substring(6,7).toUpperCase().equals("T") ){
             char c=lineAt(i).charAt(5);
             if(c=='+'){//concatenated as is (including heading whitespace) to previous line
                 fixLength=70;
                 temp=Str.rtrim(lineAt(i).substring(9));
                 
                 if(temp.charAt(0)=='$'){ 
                     temp=temp.substring(1);
                     fixLength--;
                 }
                 String newLine=((String)hline.elementAt(hline.size()-1));
                 if(newLine.trim().endsWith("\\")){
                     newLine=Str.rtrim(newLine);
                     newLine=newLine.substring(0, newLine.length()-1);
                 }
                 
                 newLine=newLine+fixLine(temp,fixLength);
                 hline.setElementAt(newLine, hline.size()-1);
                 
                 //debug
                 //System.out.println("In ENSDF.java: tableRecords: line 1273: line="+temp);
                 
             }else{//start new lines if charAt(5)!='+', even for "2tL","3tL",... lines
                 
                 if(!inTable){            
                     hline=new Vector<String>();
                     inTable=true;
                 }else if(!lineAt(i).substring(7,8).equals(lineAt(i-1).substring(7,8))){                             
                     if( hline.size() > 0 ){                  
                         t=new TableRecord();
                         t.setValues(hline);
                         tableV.addElement(t);
                         hline=new Vector<String>();       
                         //debug
                         //System.out.println("In ENSDF: TableRecord: table.size1="+tableV.size());                    
                     }                 
                 }
                 
                
                 temp=Str.rtrim(lineAt(i));
                 //debug
                 //System.out.println("In ENSDF.java: tableRecords: line 1273: line="+temp);
                 
                 fixLength=80;

                 //add two indent space for "2tL", "3tL",...
                 if(temp.length()>5 && Str.isNumeric(temp.substring(5,6)) && Integer.parseInt(temp.substring(5,6))>=2){
                     if(temp.length()>10){
                         temp=temp.substring(0,9)+"  "+temp.substring(9);    
                         
                         fixLength+=2;
                     }
                 }              
                 else if(temp.charAt(9)=='$'){//for a new "t" line with c==' ', heading spaces are skipped by default unless the line starts with "$"
                     
                     fixLength--;
                     
                     if(temp.length()>10)
                         temp=temp.substring(0,9)+temp.substring(10);    
                     else
                         temp=temp.substring(0,9); 

                 }else if(temp.charAt(9)==' '){//remove 10 heading spaces
                     
                     //if there are more than 10 heading white-spaces and line doesn't start with "$", all 
                     //the heading spaces will be removed and appended at the end of the line. In other
                     //cases, the heading white-spaces will be kept
                     if(temp.length()>10){
                         String temp1=temp.substring(10).trim();
                         if(temp.indexOf(temp1)>20)
                             temp=temp.substring(0,9)+temp.substring(19);
                         else
                             temp=temp.substring(0,9)+temp1;    
                     }
                     else
                         temp=temp.substring(0,9); 
                 }
                 
                 
                 hline.addElement(fixLine(temp,fixLength));
                  
                 //debug
                 //System.out.println("In ENSDF: TableRecord: temp="+lineAt(i));                          
             }                 
         }else if(!lineAt(i).substring(6,7).equalsIgnoreCase("T")){                      
             if(inTable){                
                 inTable=false;
                 if( hline.size() > 0 ){
                     t=new TableRecord();
                     t.setValues(hline);
                     tableV.addElement(t);                  
                     hline=new Vector<String>();   
                     //debug
                     //System.out.println("In ENSDF: TableRecord: table.size2="+tableV.size());                   
                 }
             }
         }else{
             hline.addElement(lineAt(i));
         }
         
         //debug
         //if(hline.size()>0) System.out.println("In ENSDF: TableRecord: hline.lastline="+hline.lastElement()+" "+i+" "+llbl);
     }
        
    if(hline.size()>0 && inTable){
        t=new TableRecord();
        t.setValues(hline);
        tableV.addElement(t);
        //debug
        //System.out.println("In ENSDF: TableRecord: table.size3="+tableV.size());
    }
        

 }

/* --------------------------------------------------------------------------- */

 protected void parseBands(){

/* The bands, first looking at the level comments to identify the band flags: */
    //sort band and sequence: band first, then sequence
     
    bandTagV.clear();
    bandV.clear();
    int lastBandIndex=-1;
    int index=-1;
    for( int i=0; i< comV.size(); i++){
      Comment c= (Comment) comV.elementAt(i);
      if( c.type().equals("L ") && c.nHeads() == 1){
         //if( c.headAt(0).equals("BAND") && c.nFlagsAt(0) > 0 ){  
         if( c.hasBandHead() && c.nFlagsAt(0) > 0 ){  
           String type="";
           if(c.headAt(0).equals("BAND")){
               index=lastBandIndex+1;
               lastBandIndex=index;
               type="BAND";
           }else{//for sequence, added at the end
               index=bandV.size();
               type="SEQ";
           }
           
           
           bandTagV.add(index, c.flagAt(0,0) ); 
           bandCommentV.add(index, c.body() );
           Band b=new Band();
           
           b.setType(type);
           b.setComment(c);
           bandV.add(index,b);//including both bands and sequences
         }
      } 
    }

    if( sysOut ) System.out.println("Done with the bands tags");

/* Now looking at the levels, to see which ones are band levels */

    nNonBandLevels=0;
    for( int i=0; i < levelsV.size() ; i++ ){
       Level l=(Level) levelsV.elementAt(i);

       for( int j=0; j < bandV.size(); j++){
          Band b=(Band) bandV.elementAt(j);
          Comment cb=b.comment();
          for( int n=0; n < cb.nHeads(); n++){
            for( int k=0; k < cb.nFlagsAt(n); k++){
               if( l.flag().indexOf( cb.flagAt(n,k) ) >= 0 ){ 
                  l.setBelongs2Band(true);
                  l.setBandNum(j);
                  b.addLevel(l);

                  l.setBandFlag(cb.flagAt(n,k));
                  l.setBandType(b.type());
                  
                  break;
               }
            }
          }
       }
       if( ! l.belongs2Band() ) nNonBandLevels++;

    }
    

    Band[] bands=(Band[])bandV.toArray(new Band[bandV.size()]);
    Object[] bandTags=bandTagV.toArray();
    Object[] bandBodys=bandCommentV.toArray();

    
    for( int j=0; j < bands.length; j++){
       Band b=(Band) bands[j];
       String tag=(String)bandTags[j];
       String body=(String)bandBodys[j];
       
       
       if( b.nLevels() == 0 ){ 
           bandV.removeElement(b);
           bandTagV.removeElement(tag);
           bandCommentV.removeElement(body);       
       }
    }
    
    //reset band number of levels
    for(int i=0;i<bandV.size();i++){
        Band b=(Band) bandV.elementAt(i);
        int ngcol=0;
        for(int j=0;j<b.nLevels();j++){
            Level l=(Level)b.levelAt(j);
            l.setBandNum(i);   
                       
            //set number of gamma columns in this band
            int ngammas=l.nGammas();
            for(int k=0;k<ngammas;k++){
                
                int fli=l.gammaAt(k).FLI();
                                
                if(fli<0){
                    ngammas--;
                    continue;
                }
                    
                Level fl=levelsV.elementAt(fli);
                if(!EnsdfUtil.contains(b, fl)){
                    
                    //It could be the case the fli is not found correctly where there are other levels 
                    //with identical energies as the real final level. The band label can be used to 
                    //further distinguish and identify those levels with identical energies.
                    Level lev=EnsdfUtil.findBandLevel(b, fl.ES());
                    if(lev==null)                   
                        ngammas--;
                    else
                        l.gammaAt(k).setFLI(levelsV.indexOf(lev));//reset the final level for 
                }
            }
            
            if(ngcol<ngammas)
              ngcol=ngammas;
        }
        
        b.setNColumns(ngcol);
    }
    
    //rename bands
    Vector<String> newTags=new Vector<String>();
    
    //Here bandV should have been sorted with BAND first and then SEQUENCE   
    int letterCount=0;
    
    //for BAND
    for (int x=0; x<bandV.size(); x++)
    {
        Comment c=( (Band) bandV.elementAt(x)).comment();
        //if(!c.headAt(0).equals("BAND"))
        //  continue;
        
        char letter;
        
        if(letterCount<26) 
            letter=(char)(65+letterCount);
        else 
            letter=(char)(71+letterCount);
                
        letterCount++;
        
        String newTag=Character.toString(letter);//uppercase
        String oldTag=bandTagV.get(x);
        
        //for signature split bands
        if(Character.isLowerCase(oldTag.charAt(0))){
            int n=bandTagV.indexOf(oldTag.toUpperCase());
            if(n>=0 && n<x){
                newTag=newTags.get(n).toLowerCase();
                letterCount--;
            }
            else if(n>x)//if n<0, not signature split band
                newTag=newTag.toLowerCase();
                
        }else{
            int n=bandTagV.indexOf(oldTag.toLowerCase());
            if(n>=0 && n<x){
                newTag=newTags.get(n).toUpperCase();
                letterCount--;
            }
        }
        
       
        newTags.add(newTag);        
        c.setFlagAt(0,newTag);
        
    }
    
    //for SEQUENCE
    /*
    letterCount=0;
    for (int x=0; x<bandV.size(); x++)
    {
        Comment c=( (Band) bandV.elementAt(x)).comment();
        if(c.headAt(0).equals("BAND"))
            continue;
        
        char letter;
        
        if(letterCount<26) 
            letter=(char)(65+letterCount);
        else 
            letter=(char)(71+letterCount);
                
        letterCount++;
        
        String newTag=Character.toString(letter);//uppercase
        
       
        newTags.add(newTag);        
        c.setFlagAt(0,newTag);
        
    }
    */
 }
 
/* --------------------------------------------------------------------------- */

  private void parseHeader() throws Exception{

    poshis.clear();
    for( int j=0; j< llbl; j++){
       if( ( (String) lines.elementAt(j) ).substring(5,8).equals("  H")  ){ 
          poshis.addElement( new Integer(j) ); 
       }
    }

    hisV.clear();
    for( int i=0 ; i < poshis.size() ; i++){

      int ki= ((Integer) poshis.elementAt(i)).intValue(); 
      int kf=llbl-1; 
      if( i < ( poshis.size()-1 ) ) kf=((Integer) poshis.elementAt(i+1)).intValue() -1;
      Vector<String> hline=new Vector<String>();  
      for( int j=ki; j<=kf; j++){ 
        if(  ((String) lines.elementAt(j)).substring(7,8).equals("H")  ){
            hline.addElement( (String) lines.elementAt(j));  
        }
      }
      if( hline.size() > 0 ){
        History h=new History();
        h.setValues(hline);
        hisV.addElement(h);
      }

    }

/* The first line variables: */

    dsid_s=( (String) lines.elementAt(0) ).substring(9,39).trim();
    
    dsid0_s=( (String) lines.elementAt(0) ).substring(9,39).trim();
    
    dsRef_s=( (String) lines.elementAt(0) ).substring(39,65).trim();
    dsPub_s=( (String) lines.elementAt(0) ).substring(65,74).trim();
    dsDate_s=( (String) lines.elementAt(0) ).substring(74).trim();

    if(lines.size()>1) {
    	String line2=lines.get(1);
    	if(line2.length()>10) {
    		String lineType=line2.substring(5,9);
    		char c1=lineType.charAt(0);
    		char c2=lineType.charAt(1);
    		char c3=lineType.charAt(2);
    		char c4=lineType.charAt(3);
    		if(c1==' ' && c3==' ' && c4==' ' && Str.isDigit(c2)) {
    			//continuation line of the first DSID line
    			dsid_s+=Str.rtrim(line2.substring(9));
    		}
    	}
    }
    
   /* Getting the dataset type: */

   analyzeHeader();
    
    
    //previously, used col 10-39 of the first line for DSID
    
    //DSID in continuation line
    if(dsType_s.length()>0 && lines.size()>1){
        int i=1;
        String dsidLine=lines.elementAt(i);
        
        while(dsidLine.length()>9 && dsidLine.substring(6,9).equals("   ")){
            dsid_s=dsid_s.trim()+dsidLine.substring(9).trim();
            i++;
            
            dsidLine="";
            if(i<lines.size())
                dsidLine=lines.elementAt(i);
            
        }   
    }
    

  }

  protected void analyzeHeader() {
        dsType_s="unknown";
        if( dsid_s.startsWith("ADOPTED") ) dsType_s="adopted";
        if( dsid_s.startsWith("COMMENTS") ) dsType_s="comments";
        if( dsid_s.startsWith("REFERENCES") ) dsType_s="references";
        
        
        if( dsid_s.indexOf("DECAY")  > 0  && dsid_s.indexOf("ADOPTED") < 0 ) {
            dsType_s="decay";
            
            int index=dsid_s.indexOf("DECAY") ;
            String temp=dsid_s.substring(0,index).trim();
            index=temp.lastIndexOf(" ");
            decayTypeInDSID=temp.substring(index+1).trim();
            
            parseDecayTypeFromDSID();
        }

        if( dsid_s.indexOf("(") >= 0 && dsid_s.indexOf(",") > 0 && dsid_s.indexOf(")") > 0){
            dsType_s="reaction" ;
            
            String targetS=dsid_s.substring(0,dsid_s.indexOf("(")).trim();
            try{
                target.parseNUCID(targetS);
            }catch(Exception e){
            }
            
        }

        if( dsid_s.indexOf("COULOMB EXCITATION") >= 0 ) dsType_s="coulex" ; 
        
        if( dsid_s.indexOf("(N,G)") >= 0 ) dsType_s="neutron" ; 
        
        if( dsid_s.indexOf("INELASTIC") >= 0 ) dsType_s="inelastic" ;     
        
        
  }
  
/* ---------------------------------------------------- */

  private void parseQValues(){

    /* Qvalue */

    Vector<String> qline=new Vector<String>();
    for( int k=0; k < lines.size() ; k++){
       String line=(String) lines.elementAt(k);
       if( line.substring(7,8).equals("Q") ) { 
           qline.addElement( (String) lines.elementAt(k) );
           
           //System.out.println(" Qline="+lines.elementAt(k));
       }
       
      if(line.substring(5,8).equals("  L"))
          break;
    }

    if( qline.size() > 0 ){
       if( ( (String) qline.elementAt(0) ).substring(6,8).equals(" Q") ){
          qv.setValues(qline);
      }
    }

    //System.out.println(" q.SN="+qv.SNS()+" q.SP="+qv.SPS());
    
    analyzeQValues();
   
  }

  protected void analyzeQValues() {
        if(qv.SNF()>0) addOffset("SN", qv.SNS(),qv.DSNS());
        if(qv.S2NF()>0) addOffset("S2N", qv.S2NS(),qv.DS2NS());
        if(qv.SPF()>0) addOffset("SP", qv.SPS(),qv.DSPS());
        if(qv.S2PF()>0) addOffset("S2P", qv.S2PS(),qv.DS2PS());
        if(qv.QAF()>0) addOffset("QA", qv.QAS(),qv.DQAS());
  }
  
  public HashMap<String,SDS2XDX> offsetMap(){return labelOffsetMap;}
  
  protected void addOffset(String name,String s,String ds) {
	  offsetMap0.put(name, s);
	  labelOffsetMap.put(name, new SDS2XDX(s,ds));
  }
  
/* ---------------------------------------------------- */

  private void parseNorm()throws Exception{

    /* Normalization */

    Vector<Vector<String>> normLinesVV=new Vector<Vector<String>>();//for multiple N or PN records
    String temp="";
    Vector<String> normLinesV=new Vector<String>();//for a single N or PN record

    int index=0;
    for( int k=0; k < llbl ; k++){
       temp=((String) lines.elementAt(k) ).substring(7,9);
       if(temp.substring(0,1).equals("N") ) {
           if(temp.substring(1).equals(" ") && normLinesVV.size()==0) {//for single N and PN record
               normLinesV.addElement( (String) lines.elementAt(k) );           
           }else{
               try{
                   if(Character.isDigit(temp.charAt(1)))   
                       index=Integer.valueOf(temp.substring(1));//must be integer>0 for multiple N and PN records, -1 for common (single) PN record 
                   else
                       index=-1;
                   
                   if(temp.substring(1).equals(" ") && lines.elementAt(k).charAt(6)=='P'){//common PN record for multiple N records
                       if(lines.elementAt(k).toUpperCase().charAt(5)=='C'){//comments in comment PN record is only added with the first N records
                           if(normLinesVV.size()>0)
                               normLinesVV.get(0).add((String) lines.elementAt(k));
                       }
                       else{
                           for(int m=0;m<normLinesVV.size();m++)//PN record must follow N record, so PN can not be added with an empty N record
                               if(normLinesVV.get(m).size()>0)
                                   normLinesVV.get(m).add((String) lines.elementAt(k));
                       }

                   }else if(index>=normLinesVV.size()){
                       for(int m=normLinesVV.size();m<=index;m++)//use number in "P1","P2" as index, fill the gap with empty norm lines
                           normLinesVV.add(new Vector<String>());
                       
                       normLinesVV.get(index).add((String) lines.elementAt(k));
                   }else if(index>0){                      
                       normLinesVV.get(index).add((String) lines.elementAt(k));
                   }
               }catch(NumberFormatException e){
                   e.printStackTrace();
               }catch(Exception e){
                   e.printStackTrace();            
               }
           }
       }
    }
    
    for(int i=0;i<normLinesVV.size();i++){
        if(normLinesVV.get(i).size()==0)
            continue;
        
        Normal normal=new Normal();
        normal.setValues(normLinesVV.elementAt(i));
        normal.setIndexS(Integer.toString(i));//i is the number following "N" or "PN" record in the ENSDF file
        normsV.add(normal);
    }
    

    if(normLinesV.size()>0){//This is for single N or PN record, default ID=""
        norm.setValues(normLinesV);//note that normLines gets cleared in this call
        if(normLinesVV.size()==0) 
            normsV.add(norm);
        
    }
    
    if( sysOut ) System.out.println("Done with reading the normalization");
    
    analyzeNorm();
 }

  protected void analyzeNorm() throws Exception {
        if(normsV.size()>0)
            norm=normsV.elementAt(0);
        else {
        	norm=new Normal();//in case N records are not given
        	normsV.add(norm);
        }

        /* Renormalizing: */

        //Only when there is only one N record (one parent), can it be normalized.
        //It makes no sense to normalized the intensities to absolute ones when there 
        //are more than one parents, because the decaying parent is a mixture of different
        //nuclei and their mixture ratio in the source can vary.

        if(normsV.size()==1){

            /* patching... */
        	//if( norm().BMRenD() <= 0 &&  nBetWI() > 0 && nBetWLFT() > 0 ){
            if( norm().BMRenD() <= 0 &&  nBetWI() > 0){
//              System.out.println("Before="+norm().BMRenD());
                norm.setBMRenD( 1.0, 0.0 );
//              System.out.println("After="+norm().BMRenD());
            }
            
            //if( norm().ECBPRenD() <= 0 &&  nECBPWTI() > 0 && nECBPWLFT() > 0 ){
            if( norm().ECBPRenD() <= 0 && (nECBPWTI()>0||nECBPWIB()>0||nECBPWIE()>0) ){
//              System.out.println("Before="+norm().ECBPRenD());
                norm.setECBPRenD( 1.0, 0.0 );
//              System.out.println("After="+norm().ECBPRenD());
            }

            if(norm().GRenD()<=0 && this.nGamWI()>0 && norm.pnopt_s.length()>0) {
            	try {
            		int pn=Integer.parseInt(norm.pnopt_s);
            		if(pn>1 && pn<=4)
            			norm.setGRenD(1.0, 0.0);
            	}catch(Exception e) {
            		
            	}
            	
            }

            /* patching NP */ //removed 9/21/2016 Jun
            //if( norm().NPD() <= 0 && norm().BRD() > 0  ) norm().setNPD(norm().BRD(),norm().DBRD());

            for( int j=0; j < nLevels(); j++){

              Level l=levelAt(j);

              /* calculating gammas absolute intensities and doses */

              for( int k=0; k< l.nGammas() ; k++){

                 Gamma g=l.gammaAt(k);

                 AbsInt ai=new AbsInt(); AbsInt ait=new AbsInt();
                 Dose d=new Dose();

                 if( norm.TIRenD() > 0 ){                     
                     if(!norm.isTIRenUnity() && g.DTID()>=0 && norm.DTIRenD()>=0)
                         ait.setValues(norm.TIRenD(), norm.DTIRenD() , g.TID() , g.DTID() );
                     else
                         ait.setValues(norm.TIRenS(), norm.DTIRenS() , g.TIS() , g.DTIS() ); 
                 }
                 else if( norm.GRenD() > 0 ){                           
                     if(!norm.isGRenUnity() && g.DTID()>=0 && norm.DGRenD()>=0)
                         ait.setValues(norm.GRenD(), norm.DGRenD() , g.TID() , g.DTID() );
                     else
                         ait.setValues(norm.GRenS(), norm.DGRenS() , g.TIS() , g.DTIS() ); /* The total intensity */
                 }

                if( norm.GRenD() > 0 ){      
                     if(!norm.isGRenUnity() && g.DRID()>=0 && norm.DGRenD()>=0)//to keep precision
                         ai.setValues(norm.GRenD(), norm.DGRenD() , g.RID() , g.DRID() ); 
                     else
                         ai.setValues(norm.GRenS(), norm.DGRenS() , g.RIS() , g.DRIS() ); /* The symmetric values and errors are used in RadList */
                 }else  if( norm.TIRenD() > 0 ){
                     if(!norm.isTIRenUnity() && g.DRID()>=0 && norm.DTIRenD()>=0)//to keep precision
                         ai.setValues(norm.TIRenD(), norm.DTIRenD() , g.RID() , g.DRID() ); 
                     else
                         ai.setValues(norm.TIRenS(), norm.DTIRenS() , g.RIS() , g.DRIS() ); 
                 }
                 
                 
                 /* the absolute gamma intensities: */

                 //if(g.ES().contains("68.92"))
                 //	 System.out.println(" ### "+norm.GRenD()+"  "+norm.isGRenUnity+"  "+ai.AIs()+"  "+norm.TIRenD() );
                 
                 g.setAID( ai.AId() ); g.setDAID( ai.DAId() ); g.setAIS( ai.AIs() ); g.setDAIS( ai.DAIs() ); g.setDAILLD( ai.DAILLd() );

                 /* the absolute total intensities: */

                 g.setATID( ait.AId() ); g.setDATID( ait.DAId() );  

                 /* the gamma doses: */

                 d.setValues( g.EF() , g.DEF() , g.AID() , g.DAID() );
                 g.setDoseS( d.MirdDoseS() ); g.setDDoseS( d.MirdDDoseS() ); g.setDoseF( d.MirdDoseF() ); g.setDDoseF( d.MirdDDoseF() ); 

                 if( g.DRIS().length() == 0 ){ g.setDAID(0.0); g.setDAIS(""); g.setDATID( 0.0 ); g.setDDoseS(""); g.setDDoseF( 0.0f ); }

                 /* calculating the conversion electrons absolute intensities and doses */

                 double sumces=0;
                 for(int m=0; m < g.nConvElecs() ; m++){
                   CE c=g.convElecAt(m);
                   AbsInt ac=new AbsInt(); 
                   if( norm.TIRenD() > 0 ) {
                       if(!norm.isTIRenUnity() && c.DID()>=0 && norm.DTIRenD()>=0)
                           ac.setValues( norm.TIRenD(), norm.DTIRenD() , c.ID() , c.DID() );
                       else
                           ac.setValues( norm.TIRenS(), norm.DTIRenS() , c.IS() , c.DIS() );
                   } else if( norm.GRenD() > 0 ) {
                       if(!norm.isGRenUnity() && c.DID()>=0 && norm.DGRenD()>=0)
                           ac.setValues( norm.GRenD(), norm.DGRenD() , c.ID() , c.DID() );
                       else
                           ac.setValues( norm.GRenS(), norm.DGRenS() , c.IS() , c.DIS() );
                   }


                     c.setAID( ac.AId()  ); 
                     c.setDAID( ac.DAId() );
                     c.setAIS( ac.AIs() ); 
                     c.setDAIS( ac.DAIs() );
                   
                   
                   Dose dc=new Dose();
                   dc.setValues( c.EF() , c.DEF() , c.AID() , c.DAID() );
                     c.setDoseS( dc.MirdDoseS() ); 
                     c.setDDoseS( dc.MirdDDoseS() ); 
                     c.setDoseF( dc.MirdDoseF() ); 
                     c.setDDoseF( dc.MirdDDoseF() ); 

                   if( g.DRIS().length() == 0 ){ c.setDAID(0.0); c.setDAIS(""); c.setDDoseS(""); c.setDDoseF( 0.0f ); }
                    
                   if( c.AID() > 0 ){ sumces=sumces+c.AID(); }
                 }
                   if( sumces > 102 && !g.silent){ 
                	   System.out.println("Problems, sum(ce)="+(float)sumces+" >100: "
                	   		+ "dataset=<"+lineAt(0).substring(0,39).trim()+"> at E(gamma)="+g.ES()+" Z="+nucleus().z());              
                   }
              }

              /* calculating betas absolute intensities and doses */

              for( int k=0; k< levelAt(j).nBetas() ; k++){

                 Beta b=l.betaAt(k);

                 AbsInt ai=new AbsInt(); 
                 if(!norm.isBMRenUnity() && norm.DBMRenD()>=0 && b.DID()>=0)
                     ai.setValues(norm.BMRenD(), norm.DBMRenD() , b.ID() ,b.DID() );
                 else
                     ai.setValues(norm.BMRenS(), norm.DBMRenS() , b.RIS() ,b.DRIS() ); /* The symmetric values and errors are used in RadList */
                 
                   b.setAID( ai.AId() ); 
                   b.setDAID( ai.DAId() );
                   b.setAIS( ai.AIs() ); 
                   b.setDAIS( ai.DAIs() ); 
                   b.setDAILLD( ai.DAILLd() );

                 Dose d=new Dose();
                 d.setValues( b.EAvF() , b.DEAvF() , b.AID() , b.DAID() );
                   b.setDoseS( d.MirdDoseS() ); 
                   b.setDDoseS( d.MirdDDoseS() ); 
                   b.setDoseF( d.MirdDoseF() ); 
                   b.setDDoseF( d.MirdDDoseF() ); 

                 if( b.DRIS().length() == 0 ){ b.setDAID(0.0); b.setDAIS(""); b.setDDoseS(""); b.setDDoseF( 0.0f ); }
              }

              /* calculating ECBP absolute intensities and doses */
              
              for( int k=0; k< levelAt(j).nECBPs() ; k++){

                ECBP e=l.ECBPAt(k);

                AbsInt ai=new AbsInt(); 

                /* Beta-plus absolute intensities: */
                
                 //ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.IBSD() , e.DIBSD() ); 
                if(!norm.isECBPRenUnity() && norm.DECBPRenD()>=0 && e.DIBD()>=0)
                    ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.IBD() , e.DIBD() ); 
                else
                    ai.setValues(norm.ECBPRenS(), norm.DECBPRenS() , e.IBS() , e.DIBS() ); 
                
                 e.setAIBD( ai.AId() ); e.setDAIBD( ai.DAId() ); e.setAIBS( ai.AIs() ); e.setDAIBS( ai.DAIs() );  e.setDAIBLLD( ai.DAILLd() ); 

                 //System.out.println(" ENSDF 3668: ibs="+e.IBS()+"  "+e.DIBS()+" ECBPRens="+norm.ecbpren_s+"  "+norm.decbpren_s+"  ais="+e.AIBS()+"  "+e.DAIBS());

                /* Beta-plus Doses: */

                 Dose d=new Dose(); d.setValues( e.EAvF() , e.DEAvF() , e.AIBD() , e.DAIBD() );
                 e.setDoseS( d.MirdDoseS() ); e.setDDoseS( d.MirdDDoseS() ); e.setDoseF( d.MirdDoseF() ); e.setDDoseF( d.MirdDDoseF() ); 

                /* Electron Capture absolute intensities: */

                 if(!norm.isECBPRenUnity() && norm.DECBPRenD()>=0 && e.DIED()>=0)
                     ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.IED() , e.DIED() ); 
                 else
                     ai.setValues(norm.ECBPRenS(), norm.DECBPRenS() , e.IES() , e.DIES() ); 
                 
                 e.setAIED( ai.AId() ); e.setDAIED( ai.DAId() ); e.setAIES( ai.AIs() ); e.setDAIES( ai.DAIs() );  e.setDAIELLD( ai.DAILLd() );

                 
                 //System.out.println("ENSDF 3773: AIB="+e.AIBD()+" AIE="+e.AIED());
                 
                /* The total intensity: */

                 
                 if( e.syITD() > 0 ){
                     if(!norm.isECBPRenUnity() && norm.DECBPRenD()>=0 && e.altDITD()>=0)
                         ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.altITD() , e.altDITD() ); 
                     else
                         ai.setValues(norm.ECBPRenS(), norm.DECBPRenS() , e.altITS() , e.altDITS() ); 
                     
                     
                     //System.out.println("ENSDF 3768: EL="+e.getFLS()+" AID="+ai.AId()+" "+ai.DAId()+" AITS="+ai.AIs()+" "+ai.DAIs());
                     //System.out.println("  altITD: "+e.altITD()+" DITD="+e.altDITD()+" ITS="+e.altITS()+" DS="+e.altDITS());
                     
                   e.setAITD( ai.AId() ); e.setDAITD( ai.DAId() ); e.setAITS( ai.AIs() ); e.setDAITS( ai.DAIs() ); e.setDAITLLD( ai.DAILLd() );
                   
                   //System.out.println("ENSDF 3771: AID="+e.AITD()+" "+e.DAITD()+" AITS="+e.AITS()+" "+e.DAITS());
                 }
                 else{
                   double ti=0;
                   double dti=0;
                   if(e.AIBD()>0)
                       ti+=e.AIBD();
                   if(e.AIED()>0)
                       ti+=e.AIED();
                   
                   if( e.DAIBD() > 0 ) dti=dti+ Math.pow( e.DAIBD() ,2 );
                   if( e.DAIED() > 0 ) dti=dti+ Math.pow( e.DAIED() ,2 );
                   if( dti > 0 ) dti=Math.pow( dti , 0.5 );

                   if(ti>0) {
                	   if(dti>=0) {
                           XDX2SDS xi=new XDX2SDS(); xi.setValues(ti,dti);
                           e.setAITD( ti ); e.setDAITD( dti ); e.setAITS( xi.S() ); e.setDAITS( xi.DS() ); 
                	   }else {
                		   SDS2XDX sx1=new SDS2XDX(e.AIBS(),e.DAIBS());
                		   SDS2XDX sx2=new SDS2XDX(e.AIES(),e.DAIES());
                		   sx1=sx1.add(sx2);
                		   e.setAITD(sx1.x()); e.setDAITD(sx1.dx()); e.setAITS( sx1.S() ); e.setDAITS( sx1.DS() ); 
                	   }
                	   //System.out.println("ENSDF 4007: AITD="+e.AITD()+" "+e.DAITD()+" AITS="+e.AITS()+" "+e.DAITS());
                   }
                 }
                 
                 if( e.DIBS().length() == 0 ){ 
                    e.setDAIBD( 0.0 ); e.setDAIBS( "" ); e.setDDoseS( "" ); e.setDDoseF( 0.0f ); 
                 }

                 if( e.DIES().length() == 0 ){ 
                    e.setDAIED( 0.0 ); e.setDAIES( ""); 
                 }
               }

              /* calculating Alphas absolute intensities and doses */
              
              for( int k=0; k< l.nAlphas() ; k++){

                 Alpha a=l.alphaAt(k);
                 AbsInt ai=new AbsInt(); 
                 if(!norm.isBRUnity() && norm.DBRD()>=0 && a.DID()>=0)
                     ai.setValues( norm.BRD() , norm.DBRD() , a.ID() ,a.DID() ); 
                 else
                     ai.setValues( norm.BRS() , norm.DBRS() , a.RIS() ,a.DRIS() ); 

                   a.setAID( ai.AId() ); 
                   a.setDAID( ai.DAId() );
                   a.setAIS( ai.AIs() ); 
                   a.setDAIS( ai.DAIs() );
                   a.setDAILLD( ai.DAILLd() );

                 Dose d=new Dose();
                 d.setValues( a.EF() , a.DEF() , a.AID() , a.DAID() );
                   a.setDoseS( d.MirdDoseS() ); 
                   a.setDDoseS( d.MirdDDoseS() ); 
                   a.setDoseF( d.MirdDoseF() ); 
                   a.setDDoseF( d.MirdDDoseF() ); 

                 Dose dr=new Dose();
                 dr.setValues( a.recoilEF() , a.recoilDEF() , a.AID() , a.DAID() );
                   a.setRecoilDoseS( dr.MirdDoseS() ); 
                   a.setRecoilDDoseS( dr.MirdDDoseS() ); 
                   a.setRecoilDoseF( dr.MirdDoseF() ); 
                   a.setRecoilDDoseF( dr.MirdDDoseF() ); 

                 if( a.DRIS().length() == 0 ){ a.setDAID(0.0); a.setDAIS(""); a.setDDoseS(""); a.setDDoseF( 0.0f ); a.setRecoilDDoseS( ""); a.setRecoilDDoseF( 0.0f );}
              }

              /* calculating Delayed Particle absolute intensities */

              for( int k=0; k< l.nDParticles() ; k++){

                 DParticle dp=l.DParticleAt(k);
                 AbsInt ai=new AbsInt(); 
                 if(!norm.isNPUnity() && norm.DNPD()>=0 && dp.DID()>=0)
                     ai.setValues( norm.NPD() , norm.DNPD() , dp.ID() ,dp.DID() ); 
                 else 
                     ai.setValues( norm.NPS() , norm.DNPS() , dp.RIS() ,dp.DRIS() ); 
                 
                   dp.setAID( ai.AId() ); 
                   dp.setDAID( ai.DAId() );
                   dp.setAIS( ai.AIs() ); 
                   dp.setDAIS( ai.DAIs() );

                 Dose d=new Dose();
                 d.setValues( dp.EF() , dp.DEF() , dp.AID() , dp.DAID() );
                   dp.setDoseS( d.MirdDoseS() );  dp.setDDoseS( d.MirdDDoseS() ); dp.setDoseF( d.MirdDoseF() );  dp.setDDoseF( d.MirdDDoseF() ); 


                 Dose dr=new Dose();
                 dr.setValues( dp.recoilEF() , dp.recoilDEF() , dp.AID() , dp.DAID() );
                   dp.setRecoilDoseS( dr.MirdDoseS() );  dp.setRecoilDDoseS( dr.MirdDDoseS() );  dp.setRecoilDoseF( dr.MirdDoseF() );  dp.setRecoilDDoseF( dr.MirdDDoseF() ); 

                if( dp.DRIS().length() == 0 ){ dp.setDAID(0.0); dp.setDAIS(""); dp.setDDoseS(""); dp.setDDoseF( 0.0f ); }

              }

            }
            
            
            //for unplaced
            /* calculating gammas absolute intensities and doses */

            for(Gamma g:this.unpGammas()){

               AbsInt ai=new AbsInt(); AbsInt ait=new AbsInt();
               Dose d=new Dose();

               if( norm.TIRenD() > 0 ){
                   
                     if(!norm.isTIRenUnity() && g.DRID()>=0 && norm.DTIRenD()>=0)//to keep precision
                         ai.setValues(norm.TIRenD(), norm.DTIRenD() , g.RID() , g.DRID() ); 
                     else
                         ai.setValues(norm.TIRenS(), norm.DTIRenS() , g.RIS() , g.DRIS() ); 
                     
                     if(!norm.isTIRenUnity() && g.DTID()>=0 && norm.DTIRenD()>=0)
                         ait.setValues(norm.TIRenD(), norm.DTIRenD() , g.TID() , g.DTID() );
                     else
                         ait.setValues(norm.TIRenS(), norm.DTIRenS() , g.TIS() , g.DTIS() ); 
               }
               else if( norm.GRenD() > 0 ){        
                     if(!norm.isGRenUnity() && g.DRID()>=0 && norm.DGRenD()>=0)//to keep precision
                         ai.setValues(norm.GRenD(), norm.DGRenD() , g.RID() , g.DRID() ); 
                     else
                         ai.setValues(norm.GRenS(), norm.DGRenS() , g.RIS() , g.DRIS() ); /* The symmetric values and errors are used in RadList */
                     
                     if(!norm.isGRenUnity() && g.DTID()>=0 && norm.DGRenD()>=0)
                         ait.setValues(norm.GRenD(), norm.DGRenD() , g.TID() , g.DTID() );
                     else
                         ait.setValues(norm.GRenS(), norm.DGRenS() , g.TIS() , g.DTIS() ); /* The total intensity */               
               }

               /* the absolute gamma intensities: */

               g.setAID( ai.AId() ); g.setDAID( ai.DAId() ); g.setAIS( ai.AIs() ); g.setDAIS( ai.DAIs() ); g.setDAILLD( ai.DAILLd() );

               /* the absolute total intensities: */

               g.setATID( ait.AId() ); g.setDATID( ait.DAId() );  

               /* the gamma doses: */

               d.setValues( g.EF() , g.DEF() , g.AID() , g.DAID() );
               g.setDoseS( d.MirdDoseS() ); g.setDDoseS( d.MirdDDoseS() ); g.setDoseF( d.MirdDoseF() ); g.setDDoseF( d.MirdDDoseF() ); 

               if( g.DRIS().length() == 0 ){ g.setDAID(0.0); g.setDAIS(""); g.setDATID( 0.0 ); g.setDDoseS(""); g.setDDoseF( 0.0f ); }

               /* calculating the conversion electrons absolute intensities and doses */

               double sumces=0;
               for(int m=0; m < g.nConvElecs() ; m++){
                 CE c=g.convElecAt(m);
                 AbsInt ac=new AbsInt(); 
                 if( norm.TIRenD() > 0 ) {
                       if(!norm.isTIRenUnity() && c.DID()>=0 && norm.DTIRenD()>=0)
                           ac.setValues( norm.TIRenD(), norm.DTIRenD() , c.ID() , c.DID() );
                       else
                           ac.setValues( norm.TIRenS(), norm.DTIRenS() , c.IS() , c.DIS() );                 
                 } else if( norm.GRenD() > 0 ) {
                       if(!norm.isGRenUnity() && c.DID()>=0 && norm.DGRenD()>=0)
                           ac.setValues( norm.GRenD(), norm.DGRenD() , c.ID() , c.DID() );
                       else
                           ac.setValues( norm.GRenS(), norm.DGRenS() , c.IS() , c.DIS() );               
                 }


                   c.setAID( ac.AId()  ); 
                   c.setDAID( ac.DAId() );
                   c.setAIS( ac.AIs() ); 
                   c.setDAIS( ac.DAIs() );
                 
                 
                 Dose dc=new Dose();
                 dc.setValues( c.EF() , c.DEF() , c.AID() , c.DAID() );
                   c.setDoseS( dc.MirdDoseS() ); 
                   c.setDDoseS( dc.MirdDDoseS() ); 
                   c.setDoseF( dc.MirdDoseF() ); 
                   c.setDDoseF( dc.MirdDDoseF() ); 

                 if( g.DRIS().length() == 0 ){ c.setDAID(0.0); c.setDAIS(""); c.setDDoseS(""); c.setDDoseF( 0.0f ); }
                  
                 if( c.AID() > 0 ){ sumces=sumces+c.AID(); }
               
               }
               if( sumces > 102 && !g.silent){ 
            	   System.out.println("Problems, sum(ce)="+(float)sumces+" >100: "
            	   		+ "dataset=<"+lineAt(0).substring(0,39).trim()+"> at E(gamma)="+g.ES()+" Z="+nucleus().z());              
               }
            }

            /* calculating betas absolute intensities and doses */

            for(Beta b:this.unpBetas()){

               AbsInt ai=new AbsInt(); 
               if(!norm.isBMRenUnity() && norm.DBMRenD()>=0 && b.DID()>=0)
                 ai.setValues(norm.BMRenD(), norm.DBMRenD() , b.ID() ,b.DID() );
               else
                 ai.setValues(norm.BMRenS(), norm.DBMRenS() , b.RIS() ,b.DRIS() ); /* The symmetric values and errors are used in RadList */

                 b.setAID( ai.AId() ); 
                 b.setDAID( ai.DAId() );
                 b.setAIS( ai.AIs() ); 
                 b.setDAIS( ai.DAIs() ); 
                 b.setDAILLD( ai.DAILLd() );

               Dose d=new Dose();
               d.setValues( b.EAvF() , b.DEAvF() , b.AID() , b.DAID() );
                 b.setDoseS( d.MirdDoseS() ); 
                 b.setDDoseS( d.MirdDDoseS() ); 
                 b.setDoseF( d.MirdDoseF() ); 
                 b.setDDoseF( d.MirdDDoseF() ); 

               if( b.DRIS().length() == 0 ){ b.setDAID(0.0); b.setDAIS(""); b.setDDoseS(""); b.setDDoseF( 0.0f ); }
            }

            /* calculating ECBP absolute intensities and doses */

            for(ECBP e:this.unpECBPs()){

              AbsInt ai=new AbsInt(); 

              /* Beta-plus absolute intensities: */
              
               //ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.IBSD() , e.DIBSD() ); 
              if(!norm.isECBPRenUnity() && norm.DECBPRenD()>=0 && e.DIBD()>=0)
                ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.IBD() , e.DIBD() ); 
              else
                ai.setValues(norm.ECBPRenS(), norm.DECBPRenS() , e.IBS() , e.DIBS() );   
              
               e.setAIBD( ai.AId() ); e.setDAIBD( ai.DAId() ); e.setAIBS( ai.AIs() ); e.setDAIBS( ai.DAIs() );  e.setDAIBLLD( ai.DAILLd() ); 


              /* Beta-plus Doses: */

               Dose d=new Dose(); d.setValues( e.EAvF() , e.DEAvF() , e.AIBD() , e.DAIBD() );
               e.setDoseS( d.MirdDoseS() ); e.setDDoseS( d.MirdDDoseS() ); e.setDoseF( d.MirdDoseF() ); e.setDDoseF( d.MirdDDoseF() ); 

              /* Electron Capture absolute intensities: */
               if(!norm.isECBPRenUnity() && norm.DECBPRenD()>=0 && e.DIED()>=0)
                 ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.IED() , e.DIED() ); 
               else
                 ai.setValues(norm.ECBPRenS(), norm.DECBPRenS() , e.IES() , e.DIES() ); 
               
               e.setAIED( ai.AId() ); e.setDAIED( ai.DAId() ); e.setAIES( ai.AIs() ); e.setDAIES( ai.DAIs() );  e.setDAIELLD( ai.DAILLd() );

              /* The total intensity: */

               if( e.syITD() > 0 ){
                 if(!norm.isECBPRenUnity() && norm.DECBPRenD()>=0 && e.altDITD()>=0)
                     ai.setValues(norm.ECBPRenD(), norm.DECBPRenD() , e.altITD() , e.altDITD() ); 
                 else
                     ai.setValues(norm.ECBPRenS(), norm.DECBPRenS() , e.altITS() , e.altDITS() );              
     
                 e.setAITD( ai.AId() ); e.setDAITD( ai.DAId() ); e.setAITS( ai.AIs() ); e.setDAITS( ai.DAIs() ); e.setDAITLLD( ai.DAILLd() );
               }
               else{
                 double ti=e.AIBD()+e.AIED();
                 double dti=0;
                 if( e.DAIBD() > 0 ) dti=dti+ Math.pow( e.DAIBD() ,2 );
                 if( e.DAIED() > 0 ) dti=dti+ Math.pow( e.DAIED() ,2 );
                 if( dti > 0 ) dti=Math.pow( dti , 0.5 );

          	     if(dti>=0) {
                     XDX2SDS xi=new XDX2SDS(); xi.setValues(ti,dti);
                     e.setAITD( ti ); e.setDAITD( dti ); e.setAITS( xi.S() ); e.setDAITS( xi.DS() ); 
        	     }else {
        		     SDS2XDX sx1=new SDS2XDX(e.AIBS(),e.DAIBS());
        		     SDS2XDX sx2=new SDS2XDX(e.AIES(),e.DAIES());
        		     sx1=sx1.add(sx2);
        		     e.setAITD(sx1.x()); e.setDAITD(sx1.dx()); e.setAITS( sx1.S() ); e.setDAITS( sx1.DS() ); 
        	     }

               }
               if( e.DIBS().length() == 0 ){ 
                  e.setDAIBD( 0.0 ); e.setDAIBS( "" ); e.setDDoseS( "" ); e.setDDoseF( 0.0f ); 
               }

               if( e.DIES().length() == 0 ){ 
                  e.setDAIED( 0.0 ); e.setDAIES( ""); 
               }
             }

            /* calculating Alphas absolute intensities and doses */
            
            for(Alpha a:this.unpAlphas()){

               
               AbsInt ai=new AbsInt(); 
               if(!norm.isBRUnity() && norm.DBRD()>=0 && a.DID()>=0)
                 ai.setValues( norm.BRD() , norm.DBRD() , a.ID() ,a.DID() ); 
               else
                 ai.setValues( norm.BRS() , norm.DBRS() , a.RIS() ,a.DRIS() ); 
               
                 a.setAID( ai.AId() ); 
                 a.setDAID( ai.DAId() );
                 a.setAIS( ai.AIs() ); 
                 a.setDAIS( ai.DAIs() );
                 a.setDAILLD( ai.DAILLd() );

               Dose d=new Dose();
               d.setValues( a.EF() , a.DEF() , a.AID() , a.DAID() );
                 a.setDoseS( d.MirdDoseS() ); 
                 a.setDDoseS( d.MirdDDoseS() ); 
                 a.setDoseF( d.MirdDoseF() ); 
                 a.setDDoseF( d.MirdDDoseF() ); 

               Dose dr=new Dose();
               dr.setValues( a.recoilEF() , a.recoilDEF() , a.AID() , a.DAID() );
                 a.setRecoilDoseS( dr.MirdDoseS() ); 
                 a.setRecoilDDoseS( dr.MirdDDoseS() ); 
                 a.setRecoilDoseF( dr.MirdDoseF() ); 
                 a.setRecoilDDoseF( dr.MirdDDoseF() ); 

               if( a.DRIS().length() == 0 ){ a.setDAID(0.0); a.setDAIS(""); a.setDDoseS(""); a.setDDoseF( 0.0f ); a.setRecoilDDoseS( ""); a.setRecoilDDoseF( 0.0f );}
            }

            /* calculating Delayed Particle absolute intensities */

            for( DParticle dp:this.unpDParticles()){

              
               AbsInt ai=new AbsInt(); 
               if(!norm.isNPUnity() && norm.DNPD()>=0 && dp.DID()>=0)
                 ai.setValues( norm.NPD() , norm.DNPD() , dp.ID() ,dp.DID() ); 
               else 
                 ai.setValues( norm.NPS() , norm.DNPS() , dp.RIS() ,dp.DRIS() ); 

                 dp.setAID( ai.AId() ); 
                 dp.setDAID( ai.DAId() );
                 dp.setAIS( ai.AIs() ); 
                 dp.setDAIS( ai.DAIs() );

               Dose d=new Dose();
               d.setValues( dp.EF() , dp.DEF() , dp.AID() , dp.DAID() );
                 dp.setDoseS( d.MirdDoseS() );  dp.setDDoseS( d.MirdDDoseS() ); dp.setDoseF( d.MirdDoseF() );  dp.setDDoseF( d.MirdDDoseF() ); 


               Dose dr=new Dose();
               dr.setValues( dp.recoilEF() , dp.recoilDEF() , dp.AID() , dp.DAID() );
                 dp.setRecoilDoseS( dr.MirdDoseS() );  dp.setRecoilDDoseS( dr.MirdDDoseS() );  dp.setRecoilDoseF( dr.MirdDoseF() );  dp.setRecoilDDoseF( dr.MirdDDoseF() ); 

              if( dp.DRIS().length() == 0 ){ dp.setDAID(0.0); dp.setDAIS(""); dp.setDDoseS(""); dp.setDDoseF( 0.0f ); }

            }

        }  

        if( sysOut ) System.out.println("Done renormalizating all particles ");

        /* The levels net feeding */

        for( int j=0; j<nLevels(); j++){
          Sum nf=new Sum();
          Level l=levelAt(j);

          /* Adding the absolute total intensities of the gammas depleting the level */
     
          for( int k=0; k<l.nGammas(); k++){
            nf.addAnyElement( l.gammaAt(k).ATID() ,l.gammaAt(k).DATID() );
          }

          /* Substracting the absolute total intensities of the gammas feeding the level */

          for( int jp=0; jp<nLevels(); jp++){
            Level lp=levelAt(jp);
            for( int kp=0; kp<lp.nGammas(); kp++){
              Gamma gp=lp.gammaAt(kp);
              if( gp.FLI() == j ){
                nf.addAnyElement( -gp.ATID() , gp.DATID() );
              }
            }
          }

          /* the absolute net feedings: */

          float nf_aif=nf.f(); float nf_daif= nf.df(); String nf_ais= nf.s(); String nf_dais= nf.ds();

          /* the relative net feedings: */

          float nf_rif=-1.0f; float nf_drif=-1.0f; String nf_ris=""; String nf_dris="";
          double normd=-1.0; double dnormd=-1.0;

          if( nTotBet() > 0 && norm.BMRenD()> 0){ normd=norm.BMRenD(); dnormd=norm.DBMRenD();}
          else if( nTotECBP() > 0 && norm.ECBPRenD()> 0){ normd=norm.ECBPRenD(); dnormd=norm.DECBPRenD();}

          if( normd > 0 ){ 
            nf_rif=(float)( nf_aif/normd );
            if( nf_daif >= 0 ) nf_drif=nf_drif+ (float) Math.pow(nf_daif/normd ,2);
            if( dnormd >= 0 ) nf_drif=nf_drif+ (float) Math.pow( nf_rif*dnormd/Math.pow(normd,2) ,2) ;
            if( nf_drif >=0 ) nf_drif=(float) Math.pow( nf_drif, 0.5 );

            XDX2SDS x2s=new XDX2SDS(); x2s.setValues(nf_rif,nf_drif); nf_ris=x2s.S(); nf_dris=x2s.DS();
          }

          l.setNetFeeding( nf_aif, nf_daif, nf_ais, nf_dais, nf_rif, nf_drif, nf_ris, nf_dris );
        }

        if( sysOut ) System.out.println("Done with levels net feeding ");

       /* Calculating the sum of gamma intensities to the GS */

        for( int j=0; j < levelsV.size() ; j++ ){
            Level l=(Level) levelsV.elementAt(j);
            for( int k=0; k < l.nGammas() ; k++ ){
                Gamma g=l.gammaAt(k);
                if( g.FLI() == 0 ) giSum.addElement( g.RID(), g.DRID());
                if( g.FLI() == 0 ) gaiSum.addElement( g.AID(), g.DAID());
                if( g.FLI() == 0 ) gtiSum.addElement( g.syTID(), g.DsyTID());
                if( g.FLI() == 0 ) gatiSum.addElement( g.ATID(), g.DATID());
            }
        } 

        /* calculating the intensity sum of the different decay particles */

        for( int j=0; j < nLevels(); j++){
          for( int k=0; k< levelAt(j).nBetas() ; k++){
             biSum.addElement( levelAt(j).betaAt(k).syID() , levelAt(j).betaAt(k).DsyID());
             baiSum.addElement( levelAt(j).betaAt(k).AID() , levelAt(j).betaAt(k).DAID());
          }
          for( int k=0; k< levelAt(j).nAlphas() ; k++){
             aiSum.addElement( levelAt(j).alphaAt(k).syID() , levelAt(j).alphaAt(k).DsyID());
             aaiSum.addElement( levelAt(j).alphaAt(k).AID() , levelAt(j).alphaAt(k).DAID());
          }
          for( int k=0; k< levelAt(j).nECBPs() ; k++){
             ecbpiSum.addElement( levelAt(j).ECBPAt(k).syITD() , levelAt(j).ECBPAt(k).DsyITD() );
             ecbpaiSum.addElement( levelAt(j).ECBPAt(k).AITD() , levelAt(j).ECBPAt(k).DAITD() );
          }
          for( int k=0; k< levelAt(j).nDParticles() ; k++){
             dpiSum.addElement( levelAt(j).DParticleAt(k).syID() , levelAt(j).DParticleAt(k).DsyID() );
             dpaiSum.addElement( levelAt(j).DParticleAt(k).AID() , levelAt(j).DParticleAt(k).DAID() );
          }
        }

      /* the absolute normalization comments */
    
        //NOTE: in normal.java, GRenD=BR*NR>0 only when both NR()>0 and BR()>0 (nr and br fields are not blank)
        //      but for neutron capture, by default branching ratio=1, GRenD shouble set to NR, even if br field is blank

        String DSID=dsid0_s;
        String type="";
        
        if(DSID.indexOf("(N,G)")>0){
            norm.setGRenD(norm.NRD(), norm.DNRD());
            norm.setGRenS(norm.NRS(), norm.DNRS());
            norm.isGRenUnity=EnsdfUtil.isUnity(norm.NRS(), norm.DNRS());
            type="neutron";
        }
           
        //debug
        //System.out.println("In ENSDF 2561: norm.GRenD="+norm.GRenD()+" nrom.GRenS="+norm.GRenS()+" norm.DGRens="+norm.DGRenS()+"  s="+nds.latex.Translator.impComVal(norm.GRenS(),norm.DGRenS())+" norm.NR="+norm.NRD());
        
        if( norm.GRenD() > 0 && (Math.abs(norm.TIRenD()-norm.GRenD())<5E-4 || norm.TIRenD()<=0)){//NR=NT or NT is empty
            Vector<String> vjunk=new Vector<String>();
            String s=ensdfparser.nds.latex.Translator.impComVal(norm.GRenS(),norm.DGRenS());
            //String s=norm.GRenS();
            //if( norm.DGRenS().length() > 0 ) s=s+" {I"+norm.DGRenS()+"}";      
           
            String space=" ";
            if(norm.DGRenS().length()>0&&!Str.isNumeric(norm.DGRenS())) 
                space="{ } ";//since all space before "LE","LT","GE","GT","AP" will be removed, use { } to force adding space
            
            boolean isAbsRI=true;
            String head=""; 
            if(s.length()>0 ) {
                if(!norm.isGRenUnity())
                    isAbsRI=false;
                
                if(!isRelabeled("G","RI") || !isAbsRI)
                    head="RI";
                
                if(!isRelabeled("G","TI") || !isAbsRI){
                    if(head.length()>0)
                        head+=",TI";
                    else
                        head="TI";
                }
                
            } 
     
            if(head.length()>0){
                //NOTE: in XDX2SDS: setValues(x,dx), when dx=0, x=1.0000, all non-significant zeros will be removed in s, s="1"

                if(!isAbsRI ) {      
                //if(s.length()>0 && (Math.abs(norm.GRenD()-1)>5E-4&&Float.parseFloat(norm.GRenS())!=1) ){
                    if(type.equals("neutron"))
                        vjunk.addElement( nucleus().nameENSDF()+" CG "+head+"$For intensity per 100 neutron captures, multiply by"+space+s+".");
                    else
                        vjunk.addElement( nucleus().nameENSDF()+" CG "+head+"$For absolute intensity per 100 decays, multiply by"+space+s+".");

                }else{ 
                    if(type.equals("neutron"))
                        vjunk.addElement( nucleus().nameENSDF()+" CG "+head+"$Intensity per 100 neutron captures.");
                    else
                        vjunk.addElement( nucleus().nameENSDF()+" CG "+head+"$Absolute intensity per 100 decays.");

                }
                
                Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c);
                
                //debug
                //System.out.println(c.body());
            }
        }
        else if(norm.GRenD() > 0 && norm.TIRenD()>0){//NR and NT is different
            
            //////////////////////////////////////
            //write normalization footnote for RI
            Vector<String> vjunk=new Vector<String>();
            String s=ensdfparser.nds.latex.Translator.impComVal(norm.GRenS(),norm.DGRenS());
           
            String space=" ";
            if(norm.DGRenS().length()>0&&!Str.isNumeric(norm.DGRenS())) space="{ } ";//since all space before "LE","LT","GE","GT","AP" will be removed, use { } to force adding space
            
            boolean isAbsRI=true;
            String head=""; 
            if(s.length()>0 ) {
                if(!norm.isGRenUnity())
                    isAbsRI=false;
                
                if(!isRelabeled("G","RI") || !isAbsRI)
                    head="RI";            
            }
            
            if(head.length()>0){
                //NOTE: in XDX2SDS: setValues(x,dx), when dx=0, x=1.0000, all non-significant zeros will be removed in s, s="1"
                if(!isAbsRI ) {   
                //if(s.length()>0 && Math.abs(norm.GRenD()-1)>5E-4 && Float.parseFloat(norm.GRenS())!=1){
                    if(type.equals("neutron"))
                      vjunk.addElement( nucleus().nameENSDF()+" CG RI$For intensity per 100 neutron captures, multiply by"+space+s+".");
                  else
                      vjunk.addElement( nucleus().nameENSDF()+" CG RI$For absolute intensity per 100 decays, multiply by"+space+s+".");

                }else{ 
                  if(type.equals("neutron"))
                      vjunk.addElement( nucleus().nameENSDF()+" CG RI$Intensity per 100 neutron captures.");
                  else
                      vjunk.addElement( nucleus().nameENSDF()+" CG RI$Absolute intensity per 100 decays.");

                }
                       
                Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c);   
            }
     
            
            //////////////////////////////////////////
            //write normalization footnote for TI
            s=ensdfparser.nds.latex.Translator.impComVal(norm.TIRenS(),norm.DTIRenS());
            vjunk.clear();
            
            space=" ";
            if(norm.DTIRenS().length()>0&&!Str.isNumeric(norm.DTIRenS())) space="{ } ";//since all space before "LE","LT","GE","GT","AP" will be removed, use { } to force adding space
            
            boolean isAbsTI=true;
            head=""; 
            if(s.length()>0 ) {
                if(!norm.isTIRenUnity())
                    isAbsTI=false;
                
                if(!isRelabeled("G","TI") || !isAbsTI)
                    head="TI";            
            }
            
            if(head.length()>0){
                //NOTE: in XDX2SDS: setValues(x,dx), when dx=0, x=1.0000, all non-significant zeros will be removed in s, s="1"
                if(!isAbsTI ) {   
                //if(s.length()>0 && Math.abs(norm.TIRenD()-1)>5E-4 && Float.parseFloat(norm.TIRenS())!=1){
                  if(type.equals("neutron"))
                      vjunk.addElement( nucleus().nameENSDF()+" CG TI$For intensity per 100 neutron captures, multiply by"+space+s+".");
                  else
                      vjunk.addElement( nucleus().nameENSDF()+" CG TI$For absolute intensity per 100 decays, multiply by"+space+s+".");

                }else{ 
                  if(type.equals("neutron"))
                      vjunk.addElement( nucleus().nameENSDF()+" CG TI$Intensity per 100 neutron captures.");
                  else
                      vjunk.addElement( nucleus().nameENSDF()+" CG TI$Absolute intensity per 100 decays.");

                }
                       
                Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c);
            }


        }   
        
        
        if( sysOut ) System.out.println("Done with gammas norm implicit comments ");

        
        if( ntotalp  > 0 && norm.BRS().trim().length() > 0 && norm.BMRenD()> 0){
          Vector<String> vjunk=new Vector<String>();
          //String s=nds.latex.Translator.impComVal(norm.BRS(),norm.DBRS());
          String s=ensdfparser.nds.latex.Translator.impComVal(norm.BMRenS(),norm.DBMRenS());//use the same as for Beta/EC decay
          
          //String s=norm.BRS();
          //if( norm.DBRS().length() > 0 ) s=s+" {I"+norm.DBRS()+"}";
          
          String space=" ";
          //if(norm.DBRS().length()>0&&!Str.isNumeric(norm.DBRS())) space="{ } ";     
          if(norm.DBMRenS().length()>0&&!Str.isNumeric(norm.DBMRenS())) space="{ } ";//use the same as for Beta/EC decay
          
          boolean isAbsIA=true;
          String head=""; 
          if(s.length()>0 ) {
            if(!norm.isBMRenUnity())
                isAbsIA=false;

              
              if(!isRelabeled("A","IA") || !isAbsIA)
                  head="IA";            
          }
          
          if(head.length()>0){
              //if(s.length()>0 && norm.BRD()!=1)
              if(!isAbsIA )  
              //if(s.length()>0 && Math.abs(norm.BMRenD()-1)>5E-4 && Float.parseFloat(norm.BMRenS())!=1)      
                  vjunk.addElement( nucleus().nameENSDF()+" CA IA$For absolute intensity per 100 decays, multiply by"+space+s+".");
              else 
                  vjunk.addElement( nucleus().nameENSDF()+" CA IA$Absolute intensity per 100 decays.");
              
              Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c); 
          }
          

        }

        if( sysOut ) System.out.println("Done with alphas norm implicit comments ");

        //added by Jun Chen for beta/EC decay    
        if( (ntotbet > 0 || ntotecbp>0) && norm.BMRenD()> 0 ){
            Vector<String> vjunk=new Vector<String>();
            String s=ensdfparser.nds.latex.Translator.impComVal(norm.BMRenS(),norm.DBMRenS());
                    
            String decayType="";
            if(ntotbet>0) decayType="B";
            else decayType="E";
            
            boolean isAbsIB=true;
            String head=""; 
            if(s.length()>0 ) {
                if(!norm.isBMRenUnity())
                    isAbsIB=false;
                
                if(!isRelabeled(decayType,"IB") || !isAbsIB)
                    head="IB";

                if(nieecbp>0 && (!isRelabeled(decayType,"IE")||!isAbsIB) ){
                    if(head.length()>0)
                        head+=",IE";
                    else
                        head="IE";
                }
                
                if(nitecbp>0 && (!isRelabeled(decayType,"TI")||!isAbsIB) ){
                    if(head.length()>0)
                        head+=",TI";
                    else
                        head="TI";
                }
            }
            



            
            //debug
            //System.out.println("In ENSDF: norm.GRens="+norm.GRenS()+"  s="+s);
            
            String prefix=nucleus().nameENSDF()+" C"+decayType+" "+head;
            
            String space=" ";
            if(norm.DBMRenS().length()>0&&!Str.isNumeric(norm.DBMRenS())) space="{ } ";
            
            if(head.length()>0){
                if(!isAbsIB)  
                //if(s.length()>0 && Math.abs(norm.BMRenD()-1)>5E-4 && Float.parseFloat(norm.BMRenS())!=1)
                    vjunk.addElement( prefix+"$For absolute intensity per 100 decays, multiply by"+space+s+".");
                else 
                    vjunk.addElement( prefix+"$Absolute intensity per 100 decays.");
                
                Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c); 
            }

          }

          
        if( sysOut ) System.out.println("Done with BETA/EC norm implicit comments ");
          
        if( ntotdp  > 0 && norm.NPD()> 0 ){
          Vector<String> vjunk=new Vector<String>();
          String s=ensdfparser.nds.latex.Translator.impComVal(norm.NPS(),norm.DNPS());
          //String s=norm.BRS();
          //if( norm.DBRS().length() > 0 ) s=s+" {I"+norm.DBRS()+"}";
          
          String space=" ";
          if(norm.DNPS().length()>0&&!Str.isNumeric(norm.DNPS())) space="{ } ";
          
          boolean isAbsIP=true;
          String head=""; 
          if(s.length()>0 ) {
            if(!norm.isNPUnity())
                isAbsIP=false;         
             
            if(!isRelabeled("D","IP") || !isAbsIP)           
                head="IP";            
          }
          
          if(head.length()>0){
              if(!isAbsIP)  
              //if(s.length()>0 && Math.abs(norm.NPD()-1)>5E-4 && Float.parseFloat(norm.NPS())!=1)
                  vjunk.addElement( nucleus().nameENSDF()+" C"+promptOrDelayMarker+particleType+"IP$For absolute intensity per 100 decays, multiply by"+space+s+".");
              else 
                  vjunk.addElement( nucleus().nameENSDF()+" C"+promptOrDelayMarker+particleType+"IP$Absolute intensity per 100 decays.");
              Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); comV.addElement(c); 
          }

        }

        if( sysOut ) System.out.println("Done with DPs norm implicit comments ");

        /* Normalizing the unassigned gammas */

        for( int k=0; k< ugV.size() ; k++){

                 Gamma g=(Gamma) ugV.elementAt(k);

                 AbsInt ai=new AbsInt(); AbsInt ait=new AbsInt();
                 Dose d=new Dose();

                 if( norm.GRenD() > 0 ){       
                     if(!norm.isGRenUnity() && g.DRID()>=0 && norm.DGRenD()>=0)//to keep precision
                         ai.setValues(norm.GRenD(), norm.DGRenD() , g.RID() , g.DRID() ); 
                     else
                         ai.setValues(norm.GRenS(), norm.DGRenS() , g.RIS() , g.DRIS() ); /* The symmetric values and errors are used in RadList */
                     
                     if(!norm.isGRenUnity() && g.DTID()>=0 && norm.DGRenD()>=0)
                         ait.setValues(norm.GRenD(), norm.DGRenD() , g.TID() , g.DTID() );
                     else
                         ait.setValues(norm.GRenS(), norm.DGRenS() , g.TIS() , g.DTIS() ); /* The total intensity */ 
                 
                 }

                 else if( norm.TIRenD() > 0 ){
                     
                     if(!norm.isTIRenUnity() && g.DRID()>=0 && norm.DTIRenD()>=0)//to keep precision
                         ai.setValues(norm.TIRenD(), norm.DTIRenD() , g.RID() , g.DRID() ); 
                     else
                         ai.setValues(norm.TIRenS(), norm.DTIRenS() , g.RIS() , g.DRIS() ); 
                     
                     if(!norm.isTIRenUnity() && g.DTID()>=0 && norm.DTIRenD()>=0)
                         ait.setValues(norm.TIRenD(), norm.DTIRenD() , g.TID() , g.DTID() );
                     else
                         ait.setValues(norm.TIRenS(), norm.DTIRenS() , g.TIS() , g.DTIS() );  
                 }

                 /* the absolute gamma intensities: */

                 g.setAID( ai.AId() ); g.setDAID( ai.DAId() ); g.setAIS( ai.AIs() ); g.setDAIS( ai.DAIs() ); g.setDAILLD( ai.DAILLd() );

                 /* the absolute total intensities: */

                 g.setATID( ait.AId() ); g.setDATID( ait.DAId() );  

                 /* the gamma doses: */

                 d.setValues( g.EF() , g.DEF() , g.AID() , g.DAID() );
                 g.setDoseS( d.MirdDoseS() ); g.setDDoseS( d.MirdDDoseS() ); g.setDoseF( d.MirdDoseF() ); g.setDDoseF( d.MirdDDoseF() ); 

                 if( g.DRIS().length() == 0 ){ g.setDAID(0.0); g.setDAIS(""); g.setDATID( 0.0 ); g.setDDoseS(""); g.setDDoseF( 0.0f ); }
        }


        if(norm.OS().trim().length()==0)
           findImplicitPN();      
  }
  
  private void findImplicitPN(){
            
        if(canGetAllTI && (norm.TIRenD()>0 || norm.GRenD()>0)){
            norm.setImplicitPN(3);//PN=3, absolute I(g+ce) per 100 parent decays
            return;
        }
        
        if(norm.GRenD()>0){ 
            norm.setImplicitPN(4);//PN=4, absolute I(g) per 100 parent decays
            return;
        }
        
        if(canGetAllTI && (norm.NTD()>0 || norm.NRD()>0)){
            norm.setImplicitPN(2);//PN=2, absolute I(g+ce) per 100 parent decays via this branch
            return;
        }
        
        if(canGetAllTI){
            norm.setImplicitPN(1);//PN=1, relative I(g+ce)
            return;
        }
  }
  
/* ---------------------------------------------------- */

  private void parseParent()throws Exception{        

    /* The parent record */

    String dm_name_s="";
    int id=dsid_s.indexOf(" DECAY");
    if( id > 0 ){
       String s=dsid_s.substring(0,id).trim();
       int is=s.lastIndexOf(" ");
       if(is > 0 ) dm_name_s=s.substring(is).trim();
    }

    /* The position of the parent record */

    Vector<Integer> pospar=new Vector<Integer>();
    for( int j=0; j < llbl ; j++){
       if( lineAt(j).substring(5,8).equals("  P") ){ pospar.addElement( new Integer(j) ); }
    }

    parentsV.clear();
    for( int i=0 ; i < pospar.size() ; i++){
      int ki= ((Integer) pospar.elementAt(i)).intValue(); 
      int kf=llbl; 
      if( i < ( pospar.size()-1 ) ) kf=((Integer) pospar.elementAt(i+1)).intValue();
      Vector<String> pline=new Vector<String>();
      for( int k=ki; k < kf; k++){ if( lineAt(k).substring(7,8).equals("P") ) 
          pline.addElement( lineAt(k) ); }
      
      Parent p=new Parent();

      /* Setting the decay mode name */

      p.setValues(pline); 
      p.setDMName(dm_name_s); 
            
      String ID=pline.get(0).substring(8,9);     
      p.setIndexS(ID);//i is the number or empty space following "P" record in the ENSDF file

      parentsV.addElement(p);

    }

    analyzeParent();
    
  }
  
  
  protected void analyzeParent() throws Exception {
      for(int i=0;i<parentsV.size();i++) {
          Parent p=parentsV.get(i);
          int ID=i+1;
          String dm_name_s=p.DMName();
          
          //correct parent level energy for energy="0+X" in IT decay. 8/16/2017
          if(dm_name_s.equals("IT") && !Str.isNumeric(p.level().ES())){
              if(this.levelsV.size()>0){//NOTE that energy="0+X" for regular levels is already corrected before this point
                  Level lastLevel=levelsV.lastElement();
                  float tempEF=lastLevel.ERPF();
                  if(!p.level().ES().toUpperCase().equals(lastLevel.ES().toUpperCase()))
                      tempEF+=50;
                  
                  p.level().setEF(tempEF,p.level().DEF());
                  p.level().setERPF(tempEF,p.level().DEF());
              }
          }
          
          
          /* Setting the decay mode percentual branching ratio */
          //System.out.println(normsV.size());
          
          Normal normal=norm;
          for(int j=0;j<normsV.size();j++){
              //System.out.println("i="+i+" j="+j+" indexS="+normsV.get(j).getIndexS()+" norm BR="+normsV.get(j).BRS()+"  "+normsV.get(j).getIndexS().equals(ID));
              
              if(normsV.get(j).getIndexS().equals(ID+"")){
                  normal=normsV.get(j);
                  break;
              }
          }

          //System.out.println("ENSDF 4626: norm BRD: "+normal.BRD()+" dBR="+normal.DBRD()+" BRS="+normal.BRS()+" ds="+normal.DBRS()+" ID="+ID+" dsid="+this.DSId());
          
          //double brd=normal.BRD()*100;
          //double dbrd=normal.DBRD()*100;
          
        
          if(normal!=null && normal.BRD() >= 0 ){

            /*
            int errorLimit=35;
            //System.out.println(" norm: "+normal.DBRD()+" ds="+normal.DBRS()+" dsid="+this.DSId());
            if( normal.DBRD() > 0 && !normal.DBRS().isEmpty())
                errorLimit=Integer.parseInt(normal.DBRS());
                
            XDX2SDS xs=new XDX2SDS(brd,dbrd,errorLimit);
            
            System.out.println("   brd="+brd+" dbrd="+dbrd+"  xs.s="+xs.s()+"  xs.ds="+xs.DS()+"  limit="+errorLimit);
            */
              
            String s=normal.BRS();
            String ds=normal.DBRS();
            
            /*
            //by default nsigma=3 is set for rounding all ENSDF values without numerical uncertainties,
            //but here we don't want rounding the branching ratio (but using their own number of significant digits).
            //After it is done, we need to set the nsigma back (since it is a static variable, once set, it will be
            //the same for all SDS2XDX instances, until it is reset)
            SDS2XDX.setNoMaxNsigmaLimit();
            SDS2XDX sx=new SDS2XDX();
            
            int errorLimit=35;
            if(Str.isInteger(ds)) {          	
            	errorLimit=Integer.parseInt(ds);
            	if(errorLimit<99)
            		errorLimit+=1;
            	else
            		errorLimit=35;
            }
            
            sx.setErrorLimit(errorLimit);
            if(Str.isNumericUnc(ds))
            	sx.setValues(s,ds);
            else
            	sx.setValues(s,"0");

            //System.out.println(sx.s());
            //System.out.println(" before  s="+s+" ds="+ds+" normal.BRS()="+normal.BRS()+" sx.s="+sx.s()+"  sx.ds="+sx.DS());
            
            sx=sx.multiply(100);
            
            SDS2XDX.resetDefaultMaxNsigma();
            */
            
            //System.out.println(" after  s="+s+" ds="+ds+" normal.BRS()="+normal.BRS());
            
            s=Str.shiftDecimalPoint(s, 2, "RIGHT");
            
            p.setDMValue(s); 
            p.setDMUnc(ds);
            /*
            if(!Str.isNumeric(ds))
            	p.setDMUnc(ds);
            else
            	p.setDMUnc(sx.ds());
            */
          }
          else{
            p.setDMValue( "" );  
            p.setDMUnc( "" );

          }
          
          EnsdfUtil.parseEoffsetInComments(p);        
      }
    

        
        if(parentsV.size()==1) {
            Parent p=parentsV.get(0);
            Level pl=p.level();
            if(!Str.isNumeric(pl.ES()) && p.isOffsetRange()) {//energy and uncertainty of parent level 123.4+x has been parsed if a range of x is given under the parent comments
                Vector<String> vjunk=new Vector<String>();
                vjunk.addElement(nucleus().nameENSDF()+" C"+decayRecordType+" E$Estimated for the possible range of parent level energy");
                Comment ce=new Comment(); ce.setValues(vjunk); ce.setImplicit(); comV.addElement(ce);
            }
                
        }
        
        
        EPE();    
  }
  
/* ---------------------------------------------------- */

  public void EPE(){        


    /* Calculating End Point Energies, which should be done after levels */

    if( parentsV.size() == 1 ){  /* Only if there is one parent */

        for( int j=0; j < nLevels() ; j++){

           for( int k=0; k < levelAt(j).nBetas() ; k++ ){

               EPE e=new EPE();

               /* Note that we are using relative energies for the end-point... */

               e.setValues( parentAt(0).QF()  , parentAt(0).DQF() , parentAt(0).level().ERPF() , parentAt(0).level().DEF() ,
                            levelAt(j).ERPF() , levelAt(j).DEF() );

               if( e.EPEF() > 0 ){

                 levelAt(j).betaAt(k).setEPES( e.EPES() ); levelAt(j).betaAt(k).setDEPES( e.DEPES() );
                 levelAt(j).betaAt(k).setEPEF( e.EPEF() ); levelAt(j).betaAt(k).setDEPEF( e.DEPEF() );

                 /* the neutrino energies */

                 if( levelAt(j).betaAt(k).EAvF() > 0 ){
                   EPE en=new EPE();
                   en.setValues( e.EPEF(), e.DEPEF(), 0.0f, 0.0f, levelAt(j).betaAt(k).EAvF(),  levelAt(j).betaAt(k).DEAvF() );
                   levelAt(j).betaAt(k).setENeutrino( en.EPES(), en.DEPES() ,en.EPEF() , en.DEPEF() );
                 }
                 else{
                   levelAt(j).betaAt(k).setENeutrino( "", "" , 0.0f , 0.0f );
                 }


               }
               else{

                 levelAt(j).betaAt(k).setEPES( "" ); levelAt(j).betaAt(k).setDEPES( "" );
                 levelAt(j).betaAt(k).setEPEF( 0.0f ); levelAt(j).betaAt(k).setDEPEF( 0.0f );
                 levelAt(j).betaAt(k).setENeutrino( "", "" , 0.0f , 0.0f );

               }

           }

           for( int k=0; k < levelAt(j).nECBPs() ; k++ ){

               /* first the BP end point energy */

               EPE e=new EPE();  

               /* Note that we are using relative energies for the end-point... */

               e.setValues( parentAt(0).QF() , parentAt(0).DQF() , parentAt(0).level().ERF() , parentAt(0).level().DERF() ,
                            levelAt(j).ERF() + 1021.998f  , levelAt(j).DERF() );

               /* reporting positron end point energies only if they are positive */

               if( e.EPEF() > 0 ){

                 levelAt(j).ECBPAt(k).setEPES_BP( e.EPES() ); levelAt(j).ECBPAt(k).setDEPES_BP( e.DEPES() );
                 levelAt(j).ECBPAt(k).setEPEF_BP( e.EPEF() ); levelAt(j).ECBPAt(k).setDEPEF_BP( e.DEPEF() );

                 /* the neutrino energies, look at the average positron energy */

                 if(levelAt(j).ECBPAt(k).EAvF() > 0 ){
                   EPE en=new EPE();
                   en.setValues( e.EPEF(), e.DEPEF(), 0.0f, 0.0f, levelAt(j).ECBPAt(k).EAvF(),  levelAt(j).ECBPAt(k).DEAvF() );
                   levelAt(j).ECBPAt(k).setBPENeutrino( en.EPES(), en.DEPES() , en.EPEF(), en.DEPEF() );
                 }
                 else{
                   levelAt(j).ECBPAt(k).setBPENeutrino( "","",0.0f,0.0f );
                 }
               }
               else{
                 levelAt(j).ECBPAt(k).setEPES_BP( "" ); levelAt(j).ECBPAt(k).setDEPES_BP( "" );
                 levelAt(j).ECBPAt(k).setEPEF_BP( 0.0f ); levelAt(j).ECBPAt(k).setDEPEF_BP( 0.0f );
                 levelAt(j).ECBPAt(k).setBPENeutrino( "","",0.0f,0.0f );
               }

               /* then the EC end point energy ignoring the e- atomic binding */

               EPE ee=new EPE(); 

               ee.setValues( parentAt(0).QF() , parentAt(0).DQF() , parentAt(0).level().ERF() , parentAt(0).level().DERF() ,
                            levelAt(j).ERF(), levelAt(j).DERF() );

               if( ee.EPEF() > 0 ){
                 levelAt(j).ECBPAt(k).setEPES_EC( ee.EPES() ); levelAt(j).ECBPAt(k).setDEPES_EC( ee.DEPES() );
                 levelAt(j).ECBPAt(k).setEPEF_EC( ee.EPEF() ); levelAt(j).ECBPAt(k).setDEPEF_EC( ee.DEPEF() );  

                 /* the neutrino energy is the end-point energy, that is, all the decay energy goes to the neutrino */

                 levelAt(j).ECBPAt(k).setECENeutrino( ee.EPES(), ee.DEPES() , ee.EPEF(), ee.DEPEF() );
               }
               else{
                 levelAt(j).ECBPAt(k).setEPES_EC( "" ); levelAt(j).ECBPAt(k).setDEPES_EC( "" );
                 levelAt(j).ECBPAt(k).setEPEF_EC( 0.0f  ); levelAt(j).ECBPAt(k).setDEPEF_EC( 0.0f  );
                 levelAt(j).ECBPAt(k).setECENeutrino( "","",0.0f,0.0f  );
               }

           }

           /* In a departure from the tradition, we are calculating end point energies for delayed particles ... */

/* --- decided not to do it, 01/18/05

           for( int k=0; k < levelAt(j).nDParticles() ; k++ ){
             if( levelAt(j).DParticleAt(k).ES().trim().length() == 0 ){   
                EPE e=new EPE();

                e.setValues( parentAt(0).QF() , parentAt(0).DQF() , parentAt(0).level().ERPF() , parentAt(0).level().DEF() ,
                             levelAt(j).ERPF()  , levelAt(j).DEF() );

                levelAt(j).DParticleAt(k).setES( e.EPES() );
                levelAt(j).DParticleAt(k).setDES( e.DEPES() );
                levelAt(j).DParticleAt(k).setEF( e.EPEF() );
                levelAt(j).DParticleAt(k).setDEF( e.DEPEF() );
             }
           }
*/

        }
    }
  }

/* ---------------------------------------------------- */

  private void parseAllXREFs() throws java.lang.Exception{

    /* Obtaining the Cross References: */

    /* Reading the xrefs */
    parseXREFList();

    /* Working on the levels xrefs */
    parseLevelXREFs();
  }

 private void parseXREFList() {
        String t[]=new String[100];

        
        t[0]="A"; t[1]="B"; t[2]="C";t[3]="D";t[4]="E";t[5]="F";t[6]="G";t[7]="H";t[8]="I";t[9]="J";
        t[10]="K";t[11]="L";t[12]="M";t[13]="N";t[14]="O";t[15]="P";t[16]="Q";t[17]="R";t[18]="S";
        t[19]="T";t[20]="U";t[21]="V";t[22]="W";t[23]="X";t[24]="Y";t[25]="Z";
        
        /*
        t[26]="AA";t[27]="AB";
        t[28]="AC";t[29]="AD";t[30]="AE";t[31]="AF";t[32]="AG";t[33]="AH";t[34]="AI";t[35]="AJ";t[36]="AK";
        t[37]="AL";t[38]="AM";t[39]="AN";t[40]="AO";t[41]="AP";t[42]="AQ";t[43]="AR";t[44]="AS";t[45]="AT";
        t[46]="AU";t[47]="AV";t[48]="AW";t[49]="AX";t[50]="AY";t[51]="AZ";
        */
        
        for(int x=26;x<t.length;x++){
            int y=x/26-1;
            t[x]=t[y]+t[x%26];
        }

        xrefOldTagsV.clear();
        xrefTagsV.clear();
        xrefDSIDsV.clear();
        xrefsV.clear();

        int nxrefs=0;
        
        for( int k=0; k < llbl ; k++){

            if(  lineAt(k).substring(6,8).equals(" X")){
                
               xrefOldTagsV.addElement( lineAt(k).substring(8,9) );
               xrefTagsV.addElement( t[nxrefs] );
               xrefDSIDsV.addElement( lineAt(k).substring(9).trim() );
               XRef xrn=new XRef();
               xrn.setValues( lineAt(k).substring(8,9)  , t[nxrefs] ,
                              lineAt(k).substring(9).trim() ); 

               //debug
               //System.out.println("In ENSDF line 2669: xref="+lineAt(k).substring(8,9)+" *"+t[nxrefs]+" *"+lineAt(k).substring(9).trim());
               
               xrefsV.addElement(xrn);
               nxrefs++;

            }
         }   
 }
  
    /*
     * parseXREFList() must be called before this call
     */
 
  protected void parseLevelXREFs() {
        try {
            
            String xrefN; /* level xref, reordered according to how they appear in the ensdf file */
            String xrefP; /* reordered level xref, hoewever, In this one, we put a '.' per missing xref */
            
            Vector<String> lxrefsV=new Vector<String>();/* original level xref*/
            Vector<String> lxrefsV_new=new Vector<String>();/* new level xref*/
            Vector<String> lxrefsV_new_formatted=new Vector<String>();
            
            /* Working on the levels xrefs */
            String sblank="";
            
            
            int nxrefs=XRefV().size();
            String[] t=new String[nxrefs];      
            
            //debug
            //System.out.println("In ENSDF: line 5496: dsid="+dsid_s+" nxrefs="+nxrefs+" len="+t.length+" t[k]"+nLevels());
            
            xrefTagsV.toArray(t);
            
            for( int k=0; k < nxrefs; k++){ 
                //debug
                //System.out.println("In ENSFD: line 5116: dsid="+dsid_s+" nxrefs="+nxrefs+" xrefTagV.get(k)="+xrefTagV.get(k)+" len="+t.length+" k="+k+" t[k]="+t[k]+"$ "+sblank.length());
                
                sblank=sblank+repeat(" ",xrefTagsV.get(k).length());
                
            }

            //System.out.println(sblank+"$ "+sblank.length());
            
            for( int j=0; j < nLevels() ; j++){

               xrefP=sblank;
               xrefN="";
               String impComm="";

               String s=levelAt(j).XREFS().trim();
               
               lxrefsV.clear();
               lxrefsV_new.clear();
               lxrefsV_new_formatted.clear();
               
               //System.out.println("In ENSDF line 5033: XREF="+s+"*nxrefs="+nxrefs+s.equals("+"));
               int p=0;
               if( s.equals("+") ){
                  hasLevelXref=true;
                  
                  p=0;
                  for( int m=0; m< nxrefs ; m++ ){
                      //System.out.println("In ENSDF line 2609: XREF="+s+"*nxrefs="+nxrefs+" t[m]="+t[m]);
                      xrefP=xrefP.substring(0,p)+t[m]+xrefP.substring(p+t[m].length()); 
                      xrefN+=t[m];
                      p+=t[m].length();
                      
                      lxrefsV.add(xrefOldTagsV.get(m));
                      lxrefsV_new.add(t[m]);
                      lxrefsV_new_formatted.add(t[m]);
                      
                      //System.out.println("In ENSDF line 2615: XREF="+s+"*nxrefs="+nxrefs+" t[m]="+t[m]);
                      
                  }
               }

               else if( s.startsWith("-(") && s.endsWith(")") ){
                 hasLevelXref=true;
                   
                 p=0;
                 for( int m=0; m < xrefOldTagsV.size() ; m++){ 
                    String tjunk=(String) xrefOldTagsV.elementAt(m);
                    if( s.indexOf(tjunk) < 0 ){ 
                        xrefP=xrefP.substring(0,p)+t[m]+xrefP.substring(p+t[m].length()); 
                        xrefN+=t[m];
                        
                        lxrefsV.add(xrefOldTagsV.get(m));
                        lxrefsV_new.add(t[m]);
                        lxrefsV_new_formatted.add(t[m]);
                        
                    }
                    else{ 
                        xrefP=xrefP.substring(0,p)+repeat(".",t[m].length())+xrefP.substring(p+t[m].length());       
                        lxrefsV_new_formatted.add("");
                    }
                    p+=t[m].length();
                 }
               }else if(s.trim().length()>0){
               
                   hasLevelXref=true;
                   
                   Comment c=new Comment();
                   Vector<String> vjunk=new Vector<String>();
                   String cs="";
                   
                   p=0; 
                   
                   String s1=s;
                   int lbIndex=s1.indexOf("(");
                   
                   while(lbIndex>0) {
                	   int rbIndex=s1.indexOf(")",lbIndex);
                	   String tempS=s1.substring(lbIndex,rbIndex+1);
                	   if(rbIndex>lbIndex) {
                		   s1=s1.substring(0,lbIndex)+Str.repeat(" ", tempS.length())+s1.substring(rbIndex+1);
                		   lbIndex=s1.indexOf("(",rbIndex);
                	   }else {
                		   break;
                	   }
                   }
                   
                   //System.out.println(s);
                   //System.out.println(s1);
                   
                   for( int m=0; m < xrefOldTagsV.size() ; m++){ 
                       String tjunk=(String) xrefOldTagsV.elementAt(m);
                       int is=s1.indexOf(tjunk);

                       //System.out.println("ENSDF 5194: xrefs="+s+" m="+m+" oldTag="+tjunk+" index in xrefs="+is);
                       
                       if(is>0&&tjunk.equals("E")&&s.charAt(is-1)>='0'&&s.charAt(is-1)<='9'){//deal with records of the form ABCD(3.4E3)E, etc
                           boolean goodEindex=false;
                           while(is>0&&is<s.length()){
                               is=s.indexOf(tjunk,is+1);
                               if(is>0&&(s.charAt(is-1)<'0'||s.charAt(is-1)>'9')){
                                   goodEindex=true;
                                   break;
                               }
                           }
                           
                           //System.out.println(" "+goodEindex);
                           if(!goodEindex) {//"E" in "3.4E3"
                               xrefP=xrefP.substring(0,p)+repeat(".",t[m].length())+xrefP.substring(p+t[m].length()); 
                               lxrefsV_new_formatted.add("");
                               p+=t[m].length();
                           	continue;
                           }
                       }
                       if( is >= 0 ){ //if this character is in the XREF
                           String temp;
                           String add=t[m];
                           if(is<s.length()-1){
                               if(s.charAt(is+1)=='('){ //if record of form AB(*) or AB(113.4)
                                   int index=s.indexOf(')', is+1);
                                   if(index>is+1)
                                       temp=s.substring(is+1,index+1).trim();
                                   else
                                       temp=s.substring(is+1).trim();
                                   
                                   //if asterisk, insert lower case letter instead of upper case
                                   if(temp.indexOf('*')>0)
                                       add=add.toLowerCase();
                                   
                                   if(!temp.equals("(*)")){//if level energy (not "(*)"), include comment    
                                       if(cs.length()<=0)
                                           cs=nucleus.nameENSDF()+" cL XREF$ ";
                                       
                                       temp=temp.replace("*", "");
                                       cs+=add+temp;
                                       
                                       //debug
                                       //System.out.println("In ENSDF.java: XREF comment="+nucleus.A()+nucleus.EN()+" cL XREF$ "+t[m]+": "+temp+" c.body="+c.body()+" c.isBigC="+c.isBigC()+" translated="+c.getRegTranslated());
                                   }
                               }
                           }
                            xrefP=xrefP.substring(0,p)+add+xrefP.substring(p+t[m].length()); 
                            xrefN=xrefN+add;
                            
                            //System.out.println("In ENSDF line 5234: xrefP="+xrefP+" xrefN="+xrefN);
                            //System.out.println(s+"##"+xrefOldTagV.get(m)+" m="+m+" tjunk="+tjunk);
                            
                            lxrefsV.add(xrefOldTagsV.get(m));
                            lxrefsV_new.add(t[m]);
                            lxrefsV_new_formatted.add(t[m]);

                       } /* if is >= 0 */
                       else{ 
                          xrefP=xrefP.substring(0,p)+repeat(".",t[m].length())+xrefP.substring(p+t[m].length()); 
                          lxrefsV_new_formatted.add("");
                          
                          //System.out.println("In ENSDF line 5253: xrefP="+xrefP+" xrefN="+xrefN);
                       }
                                  
                       p+=t[m].length();
                       
                    } /* for old tags */
                    
                    if(cs.length()>0){
                       vjunk.add(cs);
                       c.setValues(vjunk);
                       c.removePeriod();
                       c.setImplicit();;
                       levelAt(j).commentV().insertElementAt(c,0);
                    }
               }
               //debug
               //System.out.println("In ENSDF line 5253: xrefP="+xrefP+" xrefN="+xrefN);
               
               levelAt(j).setNewXREFSFormatted(xrefP);
               levelAt(j).setNewXREFS(xrefN);
               
               //for(String s2:lxrefsV)
               //	   System.out.print(s2);
               //System.out.println();
               
               levelAt(j).setXRefsV(lxrefsV);  
               levelAt(j).setNewXRefsV(lxrefsV_new);    
               levelAt(j).setNewXRefsVFormatted(lxrefsV_new_formatted);

               /* adding the implicit comment */

               if( impComm.length() > 0 ){
                  Vector<String> vjunk=new Vector<String>();
                  vjunk.addElement( impComm );
                  Comment c=new Comment(); c.setValues(vjunk); c.setImplicit(); //comV.addElement(c); 
                  levelAt(j).addComment(c);
                  
                  //debug
                  //if(levelAt(j).ES().equals("4178.9")) System.out.println(" ENSDF xrefN="+xrefN+" xrefP="+xrefP+" for level="+levelAt(j).ES()+" "+c.body());
               }
           
               //debug 
               //if(levelAt(j).ES().equals("4178.9")) System.out.println(" ENSDF xrefN="+xrefN+" xrefP="+xrefP+" for level="+levelAt(j).ES());

            } /* for nLevels */
        }catch(Exception e) {
            e.printStackTrace();
        }
    }  
  
/* ---------------------------------------------------- */

  private void fixLines(){
    for(int j=0; j < lines.size() ; j++){
      String s=(String) lines.elementAt(j);
      if( s.length() <=80 ){
         s=fixLine(s,80);
      }else if(s.substring(80).trim().isEmpty()) {
          s=s.substring(0,80);  
      }
      
      lines.setElementAt(s,j);
    }  

  }
  
  private String fixLine(String line,int length){
      
      String s=line;
      if(s.length() < length ){
          for( int k=s.length() ; k<length ; k++){ s=s+" ";}
       }
      
      return s;
  }
  
  @SuppressWarnings("unused")
private String fixLine(String line){
      return fixLine(line,80);
  }

/* ---------------------------------------------------- */

  
  /* must come before Normalization, unplaced radiations, comments */
  
  private void ReLabels()throws Exception{
      labelMap.clear();
      ArrayList<String> recordTypes=new ArrayList<String>(Arrays.asList("L","G","B","E","A","D"));
      
      for( int k=0; k < llbl ; k++){
          String line=lines.elementAt(k);
          int p=line.indexOf("LABEL=");
          if(p<10 || line.toUpperCase().charAt(6)!='C')
              continue;
          
          String type=line.substring(7,8);
          if(!recordTypes.contains(type))
              continue;
          
          String oldHead=line.substring(9,p).replace("$", "").trim();
          String newHead=line.substring(p+6).trim();
          labelMap.put(type+oldHead, newHead);
          
          //System.out.println(" type="+type+" old head="+oldHead+" new head="+newHead);
      }
      
  }
  /*
   * type="L","G","B","E","A","D";
   * recordName="E","T","S","L" for level type="L"
   *            "E","RI","MR","CC","TI" for gamma type="G"
   *            ...
   */
  public boolean isRelabeled(String type,String recordName){
      ArrayList<String> recordTypes=new ArrayList<String>(Arrays.asList("L","G","B","E","A","D"));
      String key=type.toUpperCase().trim();
      
      if(!recordTypes.contains(key))
          return false;
      
      key=key+recordName.toUpperCase().trim();
      if(labelMap.containsKey(key))
          return true;
      
      return false;
  }
  
  
  //aded 2/12/2020 for processing atomic data
  //Jun
  private void AtomicRad()throws Exception{

        /* Reading atomic data */

        Vector<String> lines=new Vector<String>();
        for( int k=0; k < llbl ; k++){
               String line=lineAt(k);
               if(  line.substring(7,8).equals("M")){                  
                  lines.add(line);
               }    
        }
        
        atomicRad.setValues(lines);

  }
  
  private void parseUnplacedRadiations()throws Exception{

/* Calculating the unassigned radiation */

    posugV.clear();
    posubV.clear();
    posueV.clear();
    posuaV.clear();
    posudpV.clear();

/* The Gammas */

    for( int k=0; k < llbl ; k++){
        if( ( (String) lines.elementAt(k) ).substring(5,9).equals("  G ") ){ posugV.addElement(new Integer(k));}
    }

    for(int k=0; k< posugV.size(); k++){
      int ki= ((Integer) posugV.elementAt(k)).intValue(); 
      int kf=llbl-1; 
      if( k < ( posugV.size()-1 ) ) kf=((Integer) posugV.elementAt(k+1)).intValue() -1;
      Vector<String> gline=new Vector<String>();
      for( int j=ki; j<=kf; j++){ if( ( (String) lines.elementAt(j) ).substring(7,8).equals("G") ) gline.addElement( (String) lines.elementAt(j)); }
      Gamma g=new Gamma();
      g.setSilent(silent);
      
      g.setValues(gline); g.setUnplaced();
      ugV.addElement(g);             
    }
    
/* The Betas */

    for( int k=0; k < llbl ; k++){
        if( ( (String) lines.elementAt(k) ).substring(5,9).equals("  B ") ){ posubV.addElement(new Integer(k));}
    }
    for(int k=0; k< posubV.size(); k++){
      int ki= ((Integer) posubV.elementAt(k)).intValue(); 
      int kf=llbl-1; 
      if( k < ( posubV.size()-1 ) ) kf=((Integer) posubV.elementAt(k+1)).intValue() -1;
      Vector<String> bline=new Vector<String>();
      for( int j=ki; j<=kf; j++){  if( ( (String) lines.elementAt(j) ).substring(7,8).equals("B") )  bline.addElement( (String) lines.elementAt(j)); }
      Beta b=new Beta();
      b.setValues(bline); b.setUnplaced();
      ubV.addElement(b);
    }

/* The ECBP */

    for( int k=0; k < llbl ; k++){
        if( ( (String) lines.elementAt(k) ).substring(5,9).equals("  E ") ){ posueV.addElement(new Integer(k));}
    }
    for(int k=0; k< posueV.size(); k++){
      int ki= ((Integer) posueV.elementAt(k)).intValue(); 
      int kf=llbl-1; 
      if( k < ( posueV.size()-1 ) ) kf=((Integer) posueV.elementAt(k+1)).intValue() -1;
      Vector<String> eline=new Vector<String>();
      for( int j=ki; j<=kf; j++){  if( ( (String) lines.elementAt(j) ).substring(7,8).equals("E") )  eline.addElement( (String) lines.elementAt(j)); }
      ECBP e=new ECBP();
      e.setValues(eline); e.setUnplaced();
      ueV.addElement(e);
 
    }


/* The Alphas */

    for( int k=0; k < llbl ; k++){
        if( lineAt(k).substring(5,9).equals("  A ") ){ posuaV.addElement(new Integer(k));}
    }
    for(int k=0; k< posuaV.size(); k++){
      int ki= ((Integer) posuaV.elementAt(k)).intValue(); 
      int kf=llbl-1; 
      if( k < ( posuaV.size()-1 ) ) kf=((Integer) posuaV.elementAt(k+1)).intValue() -1;
      Vector<String> aline=new Vector<String>();
      for( int j=ki; j<=kf; j++){ if( lineAt(j).substring(7,8).equals("A") ) aline.addElement( lineAt(j) ); }
      Alpha a=new Alpha(); 
      a.setValues(aline); a.setUnplaced();
      uaV.addElement(a);
    }


/* The (Delayed) Particles */

    
    Vector<String> dptV=new Vector<String>();
    for( int k=0; k < llbl ; k++){
        if( lineAt(k).substring(5,7).equals("  ") &&  ( lineAt(k).substring(7,8).equals(" ") || lineAt(k).substring(7,8).equals("D") ) ){
            if( lineAt(k).substring(8,9).equals("P") || lineAt(k).substring(8,9).equals("N") || lineAt(k).substring(8,9).equals("A") || lineAt(k).substring(8,9).equals("T")  ){
                posudpV.addElement(new Integer(k));
                dptV.addElement( lineAt(k).substring(7,9) );
            }
        }
    }

    for(int k=0; k< posudpV.size(); k++){
      String dpt=(String) dptV.elementAt(k);
      int ki= ((Integer) posudpV.elementAt(k)).intValue(); 
      int kf=llbl-1;
      
      if( k < ( posudpV.size()-1 ) ) kf=((Integer) posudpV.elementAt(k+1)).intValue() -1;
      Vector<String> dpline=new Vector<String>();
      for( int j=ki; j<=kf; j++){ 
         if( lineAt(j).substring(7,9).equals(dpt) ) dpline.addElement( lineAt(j) ); 
         
      }
      DParticle dp=new DParticle(); 
      dp.setValues(dpline); dp.setUnplaced();
      dp.setLEVI(-1);
      udpV.addElement(dp);
    }

    furl=llbl;
    if( posugV.size() > 0 ){
     if( furl > ( (Integer) posugV.elementAt(0) ).intValue() ) furl = ( (Integer) posugV.elementAt(0) ).intValue();
    }
    if( posubV.size() > 0 ){
     if( furl > ( (Integer) posubV.elementAt(0) ).intValue() ) furl = ( (Integer) posubV.elementAt(0) ).intValue();
    }
    if( posueV.size() > 0 ){
     if( furl > ( (Integer) posueV.elementAt(0) ).intValue() ) furl = ( (Integer) posueV.elementAt(0) ).intValue();
    }
    if( posuaV.size() > 0 ){
     if( furl > ((Integer) posuaV.elementAt(0)).intValue() ) furl = ( (Integer) posuaV.elementAt(0) ).intValue();
    }
    if( posudpV.size() > 0 ){
     if( furl > ((Integer) posudpV.elementAt(0)).intValue() ) furl = ( (Integer) posudpV.elementAt(0) ).intValue();
    }

    if( sysOut ) System.out.println( "Done with urad, furl="+furl+", llbl="+llbl);

  }

  
  protected void analyzeUnplacedRadiations() {
      
      //gamma
      for(int i=0;i<ugV.size();i++) {
            
          Gamma g=ugV.get(i);
          //added by Jun Chen 8/7/2015
          if( g.RIS().length() > 0 ) nigam++;
          if( g.TIS().length() > 0 ) ntigam++;
          if( g.MS().length() > 0 ) nmgam++;
          if( g.MRS().length() > 0 ) nmrgam++; 
          if( g.CCS().length() > 0 ) nccgam++; 

          ncgam=ncgam+g.nComments();
          ncrgam=ncrgam+g.nContRecords();                     
      }
           
      ntotgam=ntotgam+ugV.size();    
      gammasV.addAll(ugV);
      

      //beta
      for(int i=0;i<ubV.size();i++) {
          Beta b=ubV.get(i);
          if( b.RIS().length() > 0 ) nibet++;
          if( b.LOGFTS().length() > 0 ) nlbet++;
        
          ncbet=ncbet+b.nComments();
          ncrbet=ncrbet+b.nContRecords();
      }
       
      ntotbet=ntotbet+ubV.size();  
      betasV.addAll(ubV);
      
      //ecbp
      for(int i=0;i<ueV.size();i++) {
          ECBP e=ueV.get(i);
          if( e.IES().length() > 0 ) nieecbp++;
          if( e.ITS().length() > 0 ) nitecbp++;
          if( e.IBS().length() > 0 ) nibecbp++;
          if( e.LOGFTS().length() > 0 ) nlecbp++;
          ncecbp=ncecbp+e.nComments();
          ncrecbp=ncrecbp+e.nContRecords();
      }
        
      ntotecbp=ntotecbp+ueV.size();
      ecbpsV.addAll(ueV);
      
      //alpha
      for(int i=0;i<uaV.size();i++) {
          Alpha a=uaV.get(i);
          if( a.RIS().length() > 0 ) nialp++;
          if( a.HFS().length() > 0 ) nhfalp++;
          ncalp=ncalp+a.nComments();
          ncralp=ncralp+a.nContRecords();
      } 
         
      ntotalp=ntotalp+uaV.size();
      alphasV.addAll(uaV);
      
      //delayed particle      
      for(int i=0;i<udpV.size();i++) {
          DParticle dp=udpV.get(i);
          if( dp.ES().length()>0) nedp++;
          if( dp.RIS().length() > 0 ) nidp++;  
          if( dp.EIS().length() > 0 ) neidp++; 
          if( dp.TS().length() > 0 ) ntdp++;  
          if( dp.LS().length() > 0 ) nldp++;  
          ncdp=ncdp+dp.nComments();
          ncrdp=ncrdp+dp.nContRecords();
          if( particleType.length() == 0 ) particleType=dp.particle();
          if( promptOrDelayMarker.length() == 0 ) promptOrDelayMarker=dp.delayed();       
      }

      ntotdp=ntotdp+udpV.size();
      dpsV.addAll(udpV);
  }

/* ---------------------------------------------------- */

  protected void orderRadiations(){


/* ordering the gamma rays */

      for( int i=0; i <  ntotgam; i++){
        float egmin=100000.0f; int jmin=-1; int kmin=-1;

        for( int j=0; j < levelsV.size() ; j++){
          Level l=(Level) levelsV.elementAt(j);
          for( int k=0; k < l.nGammas() ; k++ ){
             Gamma g=l.gammaAt(k);
             if( g.getOrder() < 0){
                if( g.EF() < egmin ){ egmin=g.EF(); kmin=k; jmin=j; }
             }
          }
        }
        for( int k=0; k < ugV.size(); k++){
           Gamma g=unpGammaAt(k);
           if( g.getOrder() < 0){
              if( g.EF() < egmin ){ egmin=g.EF(); kmin=k; jmin=-1; }
           }
        }

        if( kmin >=0 && jmin >=0 ){
           ( (Level) levelsV.elementAt(jmin) ).gammaAt(kmin).setOrder(i);
        }
        else if( kmin >=0 && jmin < 0 ) ( (Gamma) ugV.elementAt(kmin) ).setOrder(i);
        
      }

/* ordering the alphas */

      for( int i=0; i < ntotalp; i++){
        float eamin=100000.0f; int jmin=-1; int kmin=-1;

        for( int j=0; j < nLevels() ; j++){
          Level l=levelAt(j);
          for( int k=0; k < l.nAlphas() ; k++ ){
             Alpha a=l.alphaAt(k);
             if( a.getOrder() < 0){
                if( a.EF() < eamin ){ eamin=a.EF(); kmin=k; jmin=j; }
             }
          }
        }

        for( int k=0; k < uaV.size(); k++){
           Alpha a=unpAlphaAt(k);
           if( a.getOrder() < 0){
              if( a.EF() < eamin ){ eamin=a.EF(); kmin=k; jmin=-1; }
           }
        }

        if( kmin >=0 && jmin >=0 ) ( (Level) levelsV.elementAt(jmin) ).alphaAt(kmin).setOrder(i);
        else if( kmin >=0 && jmin < 0 ) ( (Alpha) uaV.elementAt(kmin) ).setOrder(i);
      }
      
      /* ordering the betas */

      for( int i=0; i < ntotbet; i++){
        float ebmin=100000.0f; int jmin=-1; int kmin=-1;

        for( int j=0; j < nLevels() ; j++){
          Level l=levelAt(j);
          for( int k=0; k < l.nBetas() ; k++ ){
             Beta b=l.betaAt(k);
             if( b.getOrder() < 0){
                if( b.EPEF() < ebmin ){ ebmin=b.EPEF(); kmin=k; jmin=j; }
             }
          }
        }

        for( int k=0; k < ubV.size(); k++){
           Beta b=unpBetaAt(k);
           if( b.getOrder() < 0){
              if( b.EPEF() < ebmin ){ ebmin=b.EPEF(); kmin=k; jmin=-1; }
           }
        }

        if( kmin >=0 && jmin >=0 ) ( (Level) levelsV.elementAt(jmin) ).betaAt(kmin).setOrder(i);
        else if( kmin >=0 && jmin < 0 ) ( (Beta) ubV.elementAt(kmin) ).setOrder(i);
      }
   
      /* ordering the ecbps that have no-null intensities, and that are placed */   

      for( int i=0; i < nieecbp; i++){  

        float eemin=100000.0f; int jmin=-1; int kmin=-1;

        for( int j=0; j < nLevels() ; j++){
          Level l=levelAt(j);
          for( int k=0; k < l.nECBPs() ; k++ ){
             ECBP ec=l.ECBPAt(k);
             if( ec.getOrderEC() < 0 && ec.IES().length() > 0 ){
                if( ec.EPEF_EC() < eemin ){ eemin=ec.EPEF_EC(); kmin=k; jmin=j; }
             }
          }
        }

        if( kmin >=0 && jmin >=0 ) ( (Level) levelsV.elementAt(jmin) ).ECBPAt(kmin).setOrderEC(i);
      }

      for( int i=0; i < nibecbp; i++){

        float eemin=100000.0f; int jmin=-1; int kmin=-1;

        for( int j=0; j < nLevels() ; j++){
          Level l=levelAt(j);
          for( int k=0; k < l.nECBPs() ; k++ ){
             ECBP ec=l.ECBPAt(k);
             if( ec.getOrderBP() < 0 && ec.IBS().length() > 0 ){
                if( ec.EPEF_BP() < eemin ){ eemin= ec.EPEF_BP(); kmin=k; jmin=j; }
             }
          }
        }

        if( kmin >=0 && jmin >=0 ) ( (Level) levelsV.elementAt(jmin) ).ECBPAt(kmin).setOrderBP(i);
      }

      /* ordering the dps */
      Vector<DParticle> dpV=new Vector<DParticle>();     
      for( int i=0; i < ntotdp; i++){

        float edpmin=100000.0f; int jmin=-1; int kmin=-1;

        for( int j=0; j < nLevels() ; j++){
          Level l=levelAt(j);
          for( int k=0; k < l.nDParticles() ; k++ ){
             DParticle dp=l.DParticleAt(k);
             dpV.add(dp);
             if( dp.getOrder() < 0){
                if( dp.EF() < edpmin ){ edpmin=dp.EF(); kmin=k; jmin=j; }
             }
          }
        }

        for( int k=0; k < udpV.size(); k++){
           DParticle dp=unpDParticleAt(k);          
           if( dp.getOrder() < 0){
              if( dp.EF() < edpmin ){ edpmin=dp.EF(); kmin=k; jmin=-1; }
           }
        }

        if( kmin >=0 && jmin >=0 ) ( (Level) levelsV.elementAt(jmin) ).DParticleAt(kmin).setOrder(i);
        else if( kmin >=0 && jmin < 0 ) ( (DParticle) udpV.elementAt(kmin) ).setOrder(i);
      
      }
         
      if( sysOut ) System.out.println("Done with OrderRad");
  }

/* -------------------------- The public Methods -------------------------- */

  public void setSilent(boolean b) {silent=b;}
  
  public String fullDSIdS(){ return dsid_s;}
  
  public String fullDSId(){ return dsid_s;}//including DSID continuation 
  public String fullDSID(){ return dsid_s;}//including DSID continuation 

  
  /**
   * Previously (before 2026), it was the full DSID and now has been changed to 
   * return the same as DSId0. Now it always refers to the DSID in the first line. 
   * If there is continuation DSID which is rare and should be avoided, use fullDSId()
   * to get the complete DSID
   * @return the complete DSID of a dataset, combining the part in the first line and
   *  continuation DSID lines
   */
  public String DSId(){return dsid0_s;}//still kept considering there could be external programs which use this function
  public String DSID(){return dsid0_s;}//newly added and suggested
  
  /**
   * @return only the DSID in the very first line
   */
  public String DSId0(){return dsid0_s;}
  public String DSID0(){return dsid0_s;}
  
  public String DSRefS(){ return dsRef_s;}
  public String DSPubS(){ return dsPub_s;}
  public String DSDateS(){ return dsDate_s;}
  public String DSTypeS(){ return dsType_s;}

  
  public String levelFlags(){return levelFlags;}
  public String gammaFlags(){return gammaFlags;}
  public String decayFlags(){return decayFlags;}
  public String delayFlags(){return delayFlags;}
  public String xrayFlags(){return xrayFlags;}
  public String augerFlags(){return augerFlags;}
  
  public int nLines(){ return lines.size();}
  public String lineAt(int j){ return (String) lines.elementAt(j);}
  public Vector<String> lines(){return lines;}

  public int nHis(){return hisV.size();}
  public Vector<History> histV(){return hisV;}
  public History historyAt(int k){ return (History) hisV.elementAt(k);}

  public Nucleus nucleus(){ return nucleus;}
  public Nucleus midNucleus(){ return midNucleus;}
  
  public Nucleus target(){return target;}
  
  public QValue qv(){ return qv; }

  public Atomic atomicRad(){return atomicRad;}

  
  public Vector<XRef> XRefV(){return xrefsV;}
  public int nXRefs(){ return xrefsV.size(); }
  public XRef xRefAt(int k){return (XRef) xrefsV.elementAt(k); }
  
  public boolean hasLevelXref(){return hasLevelXref;}

  public Vector<Level> sortedLevelsV(){ return levelsV; }
  
  public int nLevels(){ return levelsV.size();} 
  public Vector<Level> levelsV(){ return levelsV; }
  public Level levelAt(int k){ return (Level) levelsV.elementAt(k); }
  public Level firstLevel(){return (Level) levelsV.firstElement(); }
  public Level lastLevel(){return (Level) levelsV.lastElement(); }

  public Vector<Gamma> gammasV() {return gammasV;}
  public Vector<Beta> betasV()  {return betasV;}
  public Vector<ECBP> ecbpsV() {return ecbpsV;}
  public Vector<Alpha> alphasV()  {return alphasV;}
  public Vector<DParticle> dpsV()     {return dpsV;}
  
  public int nLevWL() {return nllev;}
  public int nLevWJ(){ return njlev;} 
  public int nLevWT(){ return ntlev;} 
  public int nLevWDM(){ return ndmlev;} 
  public int nLevWC(){ return nclev;} 
  public int nLevWCR(){ return ncrlev;} 
  public int nNonBandLevels(){ return nNonBandLevels;} 

  public int nTotPlGam(){ return (ntotgam-ugV.size());}  
  public int nTotGam(){ return ntotgam;}  
  public int nGamWI(){ return nigam;} 
  public int nGamWTI(){return ntigam;}
  public int nGamWM(){ return nmgam;} 
  public int nGamWMR(){ return nmrgam;} 
  public int nGamWCC(){ return nccgam;} 
  public int nGamWNonZeroFL(){ return nnzflgam;} 
  public int nGamWFL(){ return nflgam;} 
  public int nGamGS(){ return ngsgam;}  
  public int nGamWC(){ return ncgam;} 
  public int nGamWCR(){ return ncrgam;} 
  public int nTotGamPaths(){ return ntotgamp;} 
  public int nMaxGamPerLevel(){return nmaxgaml;}

  public int nTotBet(){ return ntotbet;} 
  public int nBetWDL(){ return nflbet;} 
  public int nBetWI(){ return nibet;} 
  public int nBetWLFT(){ return nlbet;} 
  public int nBetWC(){ return ncbet;} 
  public int nBetWCR(){ return ncrbet;} 

  public int nTotECBP(){ return ntotecbp;} 
  public int nECBPWDL(){ return nflecbp;} 
  public int nECBPWIE(){ return nieecbp;} 
  public int nECBPWIB(){ return nibecbp;} 
  public int nECBPWTI(){ return nitecbp;} 
  public int nECBPWLFT(){ return nlecbp;} 
  public int nECBPWC(){ return ncecbp;} 
  public int nECBPWCR(){ return ncrecbp;} 

  public int nTotAlp(){ return ntotalp;} 
  public int nAlpWDL(){ return nflalp;} 
  public int nAlpWI(){ return nialp;} 
  public int nAlpWHF(){ return nhfalp;} 
  public int nAlpWC(){ return ncalp;} 
  public int nAlpWCR(){ return ncralp;} 

  public int nTotDP(){ return ntotdp;}
  public int nDPWE(){return nedp;}
  public int nDPWI(){ return nidp;} 
  public int nDPWEI(){ return neidp;} 
  public int nDPWT(){ return ntdp;} 
  public int nDPWL(){ return nldp;} 
  public int nDPWC(){ return ncdp;} 
  public int nDPWCR(){ return ncrdp;} 
  public String particleType(){ return particleType;}
  public String promptOrDelayMarker(){ return promptOrDelayMarker;}
  public String delayType(){ return delayType;}
  public int DPNumber() {return DPNumber;} 

  public int nParents(){ return parentsV.size();} 
  public Vector<Parent> parentsV(){ return parentsV; }
  public Parent parentAt(int k){ return (Parent) parentsV.elementAt(k); }

  public Normal norm(){return norm;}

  public int nNorms(){ return normsV.size();} 
  public Vector<Normal> normsV(){ return normsV; }
  public Normal normAt(int k){ return (Normal) normsV.elementAt(k); }
  
  public int nComments(){ return comV.size();} 
  public Vector<Comment> comV(){ return comV; }
  public Comment commentAt(int k){ return (Comment) comV.elementAt(k); }
 
  public int nDocuments(){ return docV.size();} 
  public Vector<Comment> docV(){ return docV; }
  public Comment documentAt(int k){ return (Comment) docV.elementAt(k); }
  
  public int nBands(){ return bandV.size();} 
  public Band bandAt(int k){ return (Band) bandV.elementAt(k); }
  public String bandTagAt(int k){ return (String) bandTagV.elementAt(k); }
  public String bandCommentAt(int k){ return (String) bandCommentV.elementAt(k); }
  public Vector<Band> bandsV() {return bandV;}
  public Vector<String> bandTagsV(){return bandTagV;}

  
  public int nRadLists(){ return rlV.size();}
  public RadList radListAt(int k){ return (RadList) rlV.elementAt(k);}
  public boolean canDoRadList(){ return canDoRadList;}

  public int nUnpGammas(){ return ugV.size();} 
  public Gamma unpGammaAt(int k){return (Gamma) ugV.elementAt(k); }

  public int nUnpBetas(){ return ubV.size();} 
  public Beta unpBetaAt(int k){return (Beta) ubV.elementAt(k); }

  public int nUnpECBPs(){ return ueV.size();} 
  public ECBP unpECBPAt(int k){return (ECBP) ueV.elementAt(k); }

  public int nUnpAlphas(){ return uaV.size();} 
  public Alpha unpAlphaAt(int k){return (Alpha) uaV.elementAt(k); }

  public int nUnpDParticles(){ return udpV.size();} 
  public DParticle unpDParticleAt(int k){return (DParticle) udpV.elementAt(k); }

  public Vector<Gamma> unpGammas(){return ugV;}
  public Vector<Beta> unpBetas(){return ubV;}
  public Vector<ECBP> unpECBPs(){return ueV;}
  public Vector<Alpha> unpAlphas(){return uaV;}
  public Vector<DParticle> unpDParticles(){return udpV;}
  
  
  public Vector<?extends Decay> unpDecaysV(){
      if(ubV.size()>0)
          return ubV;
      else if(uaV.size()>0)
          return uaV;
      else if(ueV.size()>0)
          return ueV;     
      else 
          return new Vector<Decay>();
  }
  
  public double sumGI2GS(){return giSum.d(); }
  public double dSumGI2GS(){return giSum.dd(); }

  public double sumGAI2GS(){return gaiSum.d(); }
  public double dSumGAI2GS(){return gaiSum.dd(); }

  public double sumGTI2GS(){return gtiSum.d(); }
  public double dSumGTI2GS(){return gtiSum.dd(); }

  public double sumGATI2GS(){return gatiSum.d(); }
  public double dSumGATI2GS(){return gatiSum.dd(); }

  public double sumBetInt(){return biSum.d(); }
  public double dSumBetInt(){return biSum.dd(); }
  public double sumBetAbsInt(){return baiSum.d(); }
  public double dSumBetAbsInt(){return baiSum.dd(); }

  public double sumAlpInt(){return aiSum.d(); }
  public double dSumAlpInt(){return aiSum.dd(); }
  public double sumAlpAbsInt(){return aaiSum.d(); }
  public double dSumAlpAbsInt(){return aaiSum.dd(); }

  public double sumECBPInt(){return ecbpiSum.d(); }
  public double dSumECBPInt(){return ecbpiSum.dd(); }
  public double sumECBPAbsInt(){return ecbpaiSum.d(); }
  public double dSumECBPAbsInt(){return ecbpaiSum.dd(); }

  public double sumDPInt(){return dpiSum.d(); }
  public double dSumDPInt(){return dpiSum.dd(); }
  public double sumDPAbsInt(){return dpaiSum.d(); }
  public double dSumDPAbsInt(){return dpaiSum.dd(); }

/* Some radlistpart */

  public String betaMinusMeanEne(){return bm_me_s;}
  public String betaMinusMeanEneUnc(){return bm_dme_s;}

  public String ECBPMeanEne(){return bp_me_s;}
  public String ECBPMeanEneUnc(){return bp_dme_s;}

  public Sum eGammaSum(){return egSum;}
  public Sum eCESum(){return eceSum;}
  public Sum eXRSum(){return exrSum;}
  public Sum eBMSum(){return ebmSum;}
  public Sum eBPSum(){return ebpSum;}
  public Sum eProtonSum(){return epSum;}
  public Sum eAlphaSum(){return eaSum;}
  public Sum eAugerSum(){return eauSum;}
  public Sum eNeutrinoSum(){return eneutrinoSum;}
  public Sum eRecoilSum(){return erecoilSum;}
  public Sum totalEnergySum(){return totalEnergySum;}

  public Sum eEMSum(){return eemSum;}
  public Sum eLPSum(){return elpSum;}
  public Sum eHPSum(){return ehpSum;}

  public double eem(){return eemSum.d();}
  public double deem(){return eemSum.dd();}
  public double elp(){return elpSum.d();}
  public double delp(){return elpSum.dd();}
  public double ehp(){return ehpSum.d();}
  public double dehp(){return ehpSum.dd();}

  public float totalDose(){return totalDose;}
  public float dTotalDose(){return dtotalDose;}
  public int nRadTotalDose(){return nRadTotalDose;}

  public float missingEnergyRatio(){return missEnergyRatio;}

  public double qEffective(){return qEffective;}
  public double dQEffective(){return dQEffective;}
  public double missingEnergy(){return missingEnergy;}
  public double dMissingEnergy(){return dMissingEnergy;}

  
  public int firstURadLineNo(){ return furl;}
  public int firstLevelLineNo(){return llbl;}//llbl is actually the line no. of the first level line
  
  public Vector<Integer> poslev(){return poslev;}
  
  public float maxJ(){
    float maxJ=-1.0f;
    for( int j=0; j < nLevels(); j++){
      if( levelAt(j).nJs() == 1 ){
         if( levelAt(j).jValAt(0) > maxJ ) maxJ=levelAt(j).jValAt(0);
      }
    }
    return maxJ;
  }

  protected String repeat(String s,int n){
    String out="";
    if(s==null)
        return null;
    
    for(int i=0;i<n;i++)
        out+=s;
    
    return out;
  }
  
  /* coincidence stuff */

  public int nTableRecords(){return tableV.size();}
  public TableRecord tableAt(int j){return (TableRecord) tableV.elementAt(j);}
  public String decayTypeInDSID(){return decayTypeInDSID;}
  public String decayRecordType(){return decayRecordType;}
  
  
  public String errorMsg(){return errorMsg.toString();}
  public void clearErrorMsg(){errorMsg.setLength(0);}
  
  public int id() {return id;}
  public void setID(int i) {id=i;}
  
  public void setParentMassChain(MassChain data) { this.parentMassChain = data; }
  public MassChain getParentMassChain() { return parentMassChain; }
  
  public static <T extends Record> Vector<T> sortRecords(Vector<T> recordsV) {
      ArrayList<T> list=new ArrayList<T>();
      list.addAll(recordsV);
      
      Collections.sort(list, new RecordComparator());
      
      Vector<T> out=new Vector<T>();
      out.addAll(list);
      
      return out;
  } 
  
/* -------------------------- End of ENSDFds class: -------------------------- */

}


class RecordComparator implements Comparator<Object>{
    public int compare(Object one,Object two){
        Record a=(Record)one;
        Record b=(Record)two;
        int out;
        if(a.EF()<b.EF())out=-1;
        else if(a.EF()>b.EF())out=1;
        else{           
            if(a.isGamma()) {
                String IL1=((Gamma)a).ILS();
                String IL2=((Gamma)b).ILS();
                float el1=-1,el2=-1;
                if(Str.isNumeric(IL1)) el1=Float.parseFloat(IL1);
                if(Str.isNumeric(IL2)) el2=Float.parseFloat(IL2);
                if(el1<el2)
                    out=-1;
                else if(el1>el2)
                    out=1;
                else
                    out=0;
            }else
                out=0;

        }
        
        return out;
    }
}