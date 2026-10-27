package ensdfparser.nds.ensdf;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import ensdfparser.ensdf.*;
import ensdfparser.ensdf.Atomic.AtomicData;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.latex.Translator;

import java.io.*;
/**
*Stores an ENSDF object for one single dataset, as well as all of the user settings on how to draw the tables for that ENSDF dataset
 */
public class EnsdfTableData {
    private ENSDF ens;
    private TableControl levelTableControl;
    private TableControl gammaTableControl;
    private TableControl decayTableControl;
    private TableControl delayTableControl;
    private TableControl unplcTableControl;
    
    private TableControl XRayTableControl;
    private TableControl AugerTableControl;
    
    private boolean isDrawn,showSuppressed;
    private boolean isSortGammas;
    

    
    private boolean update;
    private String updCom;
    private boolean isNewPage;
    @SuppressWarnings("unused")
    private boolean isAdopted;
    private int nXRefs;
    private boolean isCombineLG;
    private String altID;
    private DrawingControl decayDrawingControl;
    private DrawingControl bandDrawingControl;
    
    private int NID=0;//index of this etd in MassChain
    private String linkID="";
    
    private HashMap<String,String> dict=new HashMap<String,String>();
    
    /** Sets up defaults, to be changed by a control file 
     * @throws Exception */
    public EnsdfTableData(ENSDF e) throws Exception{
        int nrecords=0;
        
        ens=e;
        
        //ensdfFieldNames are used to identify the fields that can have footnote comments
        //eg, for comment like "cG BE2W$....", the head="BE2W", which can not have footnote
        //comments, and it is not a standard record field. All such comments with head not in
        //ensdfFieldNames go to the top of tables with the head also printed.
        levelTableControl=new TableControl(new String[]{"E","J","T","L","S","XREF","BAND"},"L");//parameters:reserved ENSDF field names and table type
        gammaTableControl=new TableControl(new String[]{"E","RI","M","MR","CC","TI"},"G");
        decayTableControl=new TableControl(new String[]{"E","IB","IE","IA","TI","HF","LOGFT"},"D");
        delayTableControl=new TableControl(new String[]{"E","IP","EI","T","L"},"P");

        XRayTableControl=new TableControl(new String[] {"NAME","E","IM"},"M");
        AugerTableControl=new TableControl(new String[] {"NAME","E","IM"},"M");
        
        unplcTableControl=new TableControl();
        update=false;
        
        showSuppressed=true;
       

        //set columns that must display even if they are empty
        //settings are different for level columns, see TableMaker->findBreaks(), 
        //when the only column is "COMM", columns from other panel will be used
        levelTableControl.setDefaultFixedColumns(new String[] {});
        gammaTableControl.setDefaultFixedColumns(new String[] {"E","LEV"});
        decayTableControl.setDefaultFixedColumns(new String[] {"E","LEV"});
        delayTableControl.setDefaultFixedColumns(new String[] {"E","LEV"});


        /*
        if(ens.DSId().contains("DECAY") || ens.DSId().contains("UNPLACED"))
        { isSortGammas=true;}
        else 
        {isSortGammas=false; }
        */
        
        
        //sort gamma according PN option
        int PN=0;
        if(!ens.norm().OS().isEmpty())
            PN=Integer.valueOf(ens.norm().OS());

        if(PN==6 || PN==7)//intensity values are branching or percentage from each level, not sort gamma (sort by level)
            isSortGammas=false;
        else if(PN==0 && ens.fullDSId().contains("ADOPTED LEVELS"))//if PN is not given, force sorting gammas by levels in Adopted dataset
            isSortGammas=false;
        else
            isSortGammas=true;
        
        gammaTableControl.setSortGammas(isSortGammas);
        
        isCombineLG=false;
        
        //find out which tables to draw
        isDrawn=true;
        if(ens.nLevels()>0) {
            levelTableControl.setDrawn(true);
            levelTableControl.setIsEmpty(false);
        }
        else{ 
            levelTableControl.setDrawn(false);
            levelTableControl.setIsEmpty(true);
        }
        
        if(ens.nTotGam()>0){ 
            gammaTableControl.setDrawn(true);
            gammaTableControl.setIsEmpty(false);
        }
        else{ 
            gammaTableControl.setDrawn(false);
            gammaTableControl.setIsEmpty(true);
        }
        
        if(ens.nTotBet()>0||ens.nTotAlp()>0||ens.nTotECBP()>0){    
            decayTableControl.setDrawn(true);
            decayTableControl.setIsEmpty(false);
        }
        else{ 
            decayTableControl.setDrawn(false);
            decayTableControl.setIsEmpty(true);
        }
        if(ens.nTotDP()>0){
            delayTableControl.setDrawn(true);
            delayTableControl.setIsEmpty(false);
        }
        else{ 
            delayTableControl.setDrawn(false);
            delayTableControl.setIsEmpty(true);
        }
  
        unplcTableControl.setDrawn(false);
          
        if(!ens.atomicRad().isEmpty() && NDSControl.showAtomicData) {
            XRayTableControl.setDrawn(true);
            XRayTableControl.setIsEmpty(false);
            
            AugerTableControl.setDrawn(true);
            AugerTableControl.setIsEmpty(false);
        }else {
            XRayTableControl.setDrawn(false);
            XRayTableControl.setIsEmpty(true);
            
            AugerTableControl.setDrawn(false);
            AugerTableControl.setIsEmpty(true);
        }
        
        //decide whether this dataset should start on a new page
        isAdopted=false;
        isNewPage=false;
        if(ens.fullDSId().contains("ADOPTED LEVELS")){
            isNewPage=true;
            isAdopted=true;
        }
        
        nXRefs=ens.nXRefs();
        if(nXRefs>26)
            nXRefs=26;
        
        //set up column defaults based on what data is in the ENSDF file for levels
        
        levelTableControl.getDefaultColumns().add("E");nrecords+=1;
        levelTableControl.getDefaultColumns().add("DE");
        //levelControl.columns.add("J");nrecords+=1;
        boolean isT12=false,isCom=false,isL=false,isS=false, isJPi=false;
        for(int i=0;i<ens.nLevels();i++){
            if(ens.levelAt(i).halflife().length()>0){
                isT12=true;
            }
            if(ens.levelAt(i).nComments()>0||ens.levelAt(i).nContRecords()>0||ens.levelAt(i).nDecayModes()>0) 
                isCom=true;
            else if(ens.nXRefs()>nXRefs)//print additional XREFs as comments
                isCom=true;
            
            
            if(ens.levelAt(i).lS().length()>0) isL=true;
            if(ens.levelAt(i).sS().length()>0) isS=true;
            if(ens.levelAt(i).JPiS().length()>0) isJPi=true;
            if(isCom&&isT12&&isL&&isS&&isJPi) break;
        }
        if(isJPi) {levelTableControl.getDefaultColumns().add("J");nrecords+=1;}
        if(isT12) {
            levelTableControl.getDefaultColumns().add("T");nrecords+=1;
            levelTableControl.getDefaultColumns().add("DT");
            
        }
        if(isL) {levelTableControl.getDefaultColumns().add("L");nrecords+=1;}
        if(isS) {
            levelTableControl.getDefaultColumns().add("S");nrecords+=1;
            levelTableControl.getDefaultColumns().add("DS");
            
        }
        if(ens.nXRefs()>0 && ens.hasLevelXref()) {levelTableControl.getDefaultColumns().add("XREF");nrecords+=1;}
        
        if(ens.nBands()>0 && NDSControl.printBandSeq) 
        {
            levelTableControl.getDefaultColumns().add("BND");
            nrecords+=1;
            
        }
        
        if(isCom) levelTableControl.getDefaultColumns().add("COMM");
        levelTableControl.setNRecords(nrecords);
        
        //set up column defaults for gammas
        nrecords=0;
        boolean isTI=false,isFinalLevel=false,isJF=false,isJI=false;
        isCom=false;
        for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nGammas();j++){
                if(!ens.levelAt(i).gammaAt(j).TIS().isEmpty())  isTI=true;  
                if(ens.levelAt(i).JPiS().length()>0) isJI=true;
                int FLI=ens.levelAt(i).gammaAt(j).FLI();
                if(FLI>=0) {
                    isFinalLevel=true;
                    if(ens.levelAt(FLI).JPiS().length()>0)
                        isJF=true;
                }
   
                if(ens.levelAt(i).gammaAt(j).nComments()>0)isCom=true;
                for(int k=0;k<ens.levelAt(i).gammaAt(j).nContRecords();k++){
                    ContRecord cr=ens.levelAt(i).gammaAt(j).contRecordAt(k);
                	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                		isCom=true;
                }
            }
            if(isTI&&isCom&&isFinalLevel)break;
        }
        
        for(int j=0;j<ens.unpGammas().size();j++){
            Gamma g=ens.unpGammaAt(j);
            if(g.TIS().length()>0) isTI=true;
            if(g.nComments()>0)isCom=true;
            for(int k=0;k<g.nContRecords();k++) {
                ContRecord cr=g.contRecordAt(k);
            	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
            		isCom=true;
 
            }
            if(isTI&&isCom) break;
        }
        
        if(isSortGammas) {
        //if(ens.DSId().contains("DECAY") || isSortGammas) {
            gammaTableControl.getDefaultColumns().add("E");
            gammaTableControl.getDefaultColumns().add("DE");
            nrecords+=1;
            
            if(ens.nGamWI()>0) {
                gammaTableControl.getDefaultColumns().add("RI");
                gammaTableControl.getDefaultColumns().add("DRI");           
                nrecords+=1;
            }
        }
        
        if(ens.nLevels()>0){
            gammaTableControl.getDefaultColumns().add("LEV");nrecords+=1;
            gammaTableControl.getDefaultColumns().add("DLEV");
            if(isJI)
                gammaTableControl.getDefaultColumns().add("JI");nrecords+=1;    
        }


        if(!gammaTableControl.getDefaultColumns().contains("E")) {//for tables in order of levels, gamma column is printed after level column
            gammaTableControl.getDefaultColumns().add("E");
            gammaTableControl.getDefaultColumns().add("DE");
            nrecords+=1;
            
            if(ens.nGamWI()>0) {
                gammaTableControl.getDefaultColumns().add("RI");
                gammaTableControl.getDefaultColumns().add("DRI");           
                nrecords+=1;
            }
        }
                
        if(isFinalLevel){
            gammaTableControl.getDefaultColumns().add("LEVF");nrecords+=1;
            gammaTableControl.getDefaultColumns().add("DLEVF");
            if(isJF)
                gammaTableControl.getDefaultColumns().add("JF");nrecords+=1;
        }
        
        if(ens.nGamWM()>0) {gammaTableControl.getDefaultColumns().add("M");nrecords+=1;}
        if(ens.nGamWMR()>0) {
            gammaTableControl.getDefaultColumns().add("MR");
            gammaTableControl.getDefaultColumns().add("DMR");
            nrecords+=1;
        }
        if(ens.nGamWCC()>0) {
            gammaTableControl.getDefaultColumns().add("CC");nrecords+=1;
            gammaTableControl.getDefaultColumns().add("DCC");
        }
        if(isTI) {
            gammaTableControl.getDefaultColumns().add("TI");nrecords+=1;
            gammaTableControl.getDefaultColumns().add("DTI");
            
        }
        if(isCom) gammaTableControl.getDefaultColumns().add("COMM");
        gammaTableControl.setNRecords(nrecords);
        
        //set up column Defaults for decay datasets
        boolean isI=false, isI2=false, done=false;
        isCom=false;
        isTI=false;
        isL=false;
        nrecords=0;
                
        /*find out what columns are going to be printed*/
        //see if there are comments
        for(int i=0;i<ens.nLevels();i++){
            Level l=ens.levelAt(i);
            if(ens.decayRecordType().equals("B")){
                for(int j=0;j<l.nBetas();j++){
                    Beta b=l.betaAt(j);
                    if(b.RIS().length()>0)isI=true;
                    if(b.nComments()>0)isCom=true;
                    for(int k=0;k<b.nContRecords();k++) { 
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                    		isCom=true;
                    }
                    if(b.LOGFTS().length()>0) isL=true;
                    if(isL&&isCom) done=true;
                }
            }else if(ens.decayRecordType().equals("A")){
                for(int j=0;j<l.nAlphas();j++){
                    Alpha b=l.alphaAt(j);
                    if(b.RIS().length()>0) isI=true;
                    if(b.nComments()>0)isCom=true;
                    for(int k=0;k<b.nContRecords();k++) { 
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                    		isCom=true;    
                    }
                    if(b.HFS().length()>0) isL=true;
                    if(isCom&&isL) done=true;

                }                
            }else if(ens.decayRecordType().equals("E")){
                for(int j=0;j<l.nECBPs();j++){
                    ECBP b=l.ECBPAt(j);
                    if(b.nComments()>0)isCom=true;
                    for(int k=0;k<b.nContRecords();k++) {
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                    		isCom=true;
                    }
                    if(b.LOGFTS().length()>0) isL=true;
                    if(b.IBS().length()>0) isI=true;
                    if(b.IES().length()>0) isI2=true;
                    if(b.ITS().length()>0) isTI=true;
                    if(isL&&isCom&&isI&&isI2&&isTI) done=true;

                }                
            }
            if(done) break;
        }
 
        decayTableControl.getDefaultColumns().add("E");nrecords+=1;
        decayTableControl.getDefaultColumns().add("DE");//added for alignment purpose (by decimal point), event if no uncertainty is given
        
        if(ens.nLevels()>0){
            decayTableControl.getDefaultColumns().add("LEV");
            decayTableControl.getDefaultColumns().add("DLEV");//added for alignment purpose (by decimal point), event if no uncertainty is given
            nrecords+=1;
        }
        
        if(ens.fullDSId().contains("DECAY")){
            if(ens.decayRecordType().equals("B")){
                for(int j=0;j<ens.unpBetas().size();j++){
                    Beta b=ens.unpBetaAt(j);
                    if(b.RIS().length()>0) isI=true;
                    if(b.nComments()>0)isCom=true;
                    for(int k=0;k<b.nContRecords();k++) { 
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                    		isCom=true;
                    }
                    if(b.LOGFTS().length()>0) isL=true;
                }
            }else if(ens.decayRecordType().equals("A")){
                for(int j=0;j<ens.unpAlphas().size();j++){
                    Alpha b=ens.unpAlphaAt(j);
                    if(b.RIS().length()>0) isI=true;
                    if(b.nComments()>0)isCom=true;
                    for(int k=0;k<b.nContRecords();k++) { 
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                    		isCom=true;
                    }
                    if(b.HFS().length()>0) isL=true;
                }                
            }else if(ens.decayRecordType().equals("E")){
                for(int j=0;j<ens.unpECBPs().size();j++){
                    ECBP b=ens.unpECBPAt(j);
                    if(b.nComments()>0)isCom=true;
                    for(int k=0;k<b.nContRecords();k++) {
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                    		isCom=true;
                    }
                    if(b.LOGFTS().length()>0) isL=true;
                    if(b.IBS().length()>0) isI=true;
                    if(b.IES().length()>0) isI2=true;
                    if(b.ITS().length()>0) isTI=true;
                }                
            }
        }

        
        if(isTI){//EC only
            double qf=-1;
            if(ens.parentsV().size()>0)
            	qf=ens.parentAt(0).QF(); 
            
            double diff=qf-1022;
            
            if(qf<=1022)
                isTI=false;
            else if(diff<200 && ens.nECBPWIE()==ens.nECBPWTI()){
                            
                Vector<ECBP> allECBPs=new Vector<ECBP>();
                for(int i=0;i<ens.nLevels();i++){
                    Level l=ens.levelAt(i);
                    allECBPs.addAll(l.ECBPsV());
                    if(i<3 && diff<l.EF()) {
                        isTI=false;
                    }
                }

                if(!isTI) {
                    allECBPs.addAll(ens.unpECBPs());
                    for(ECBP ec:allECBPs) {
                        if(ec.ITS().length()>0 && !ec.ITS().equals(ec.IES())){
                            isTI=true;
                            break;
                        }
                    }              
                }                       
            }

        }
        
        if(isI){
            if(ens.decayRecordType().equals("B") || ens.decayRecordType().equals("E")){ 
                decayTableControl.getDefaultColumns().add("IB");
                decayTableControl.getDefaultColumns().add("DIB");
            }
            else if(ens.decayRecordType().equals("A")){ 
                decayTableControl.getDefaultColumns().add("IA");
                decayTableControl.getDefaultColumns().add("DIA");
            }
            else{ 
                decayTableControl.getDefaultColumns().add("IP");
                decayTableControl.getDefaultColumns().add("DIP");
            }
            

            nrecords+=1;
        }
        
        if(isI2) {
            decayTableControl.getDefaultColumns().add("IE");nrecords+=1;
            decayTableControl.getDefaultColumns().add("DIE");           
        }
        if(isL){
            if(ens.fullDSId().contains(" A ") || ens.nTotAlp()>0){
                decayTableControl.getDefaultColumns().add("HF");
                decayTableControl.getDefaultColumns().add("DHF");
            }
            else{
                decayTableControl.getDefaultColumns().add("LOGFT");
                decayTableControl.getDefaultColumns().add("DLOGFT");
            }
            nrecords+=1;
        }
        if(isTI) {
            decayTableControl.getDefaultColumns().add("TI");nrecords+=1;
            decayTableControl.getDefaultColumns().add("DTI");           
        }
        if(isCom) decayTableControl.getDefaultColumns().add("COMM");

        
        //debug
        //for(String s:decayTableControl.getDefaultColumns())
        //  System.out.println("##### "+s+isTI);
        
        decayTableControl.setNRecords(nrecords);
        
        //gets what columns are needed for the delayed particle table
        nrecords=0;
        
        //debug
        //System.out.println("In EnsdfTableData: ens.nDPWE="+ens.nDPWE()+" ens.nDPWI="+ens.nDPWI()+" isCom="+isCom);
        
        if(ens.nDPWE()>0) {//There are cases where E(p) are not given but I(p) are given. 
            delayTableControl.getDefaultColumns().add("E");
            delayTableControl.getDefaultColumns().add("DE");
            nrecords+=1;
            }
        if(ens.nLevels()>0) {
            delayTableControl.getDefaultColumns().add("LEV");nrecords+=1;
            delayTableControl.getDefaultColumns().add("DLEV");
        }
        if(ens.nDPWI()>0) {
            delayTableControl.getDefaultColumns().add("IP");nrecords+=1;
            delayTableControl.getDefaultColumns().add("DIP");
        }
        if(ens.nDPWEI()>0) {delayTableControl.getDefaultColumns().add("EI");nrecords+=1;}
        if(ens.nDPWT()>0) {
            delayTableControl.getDefaultColumns().add("T");nrecords+=1;
            delayTableControl.getDefaultColumns().add("DT");
            }
        if(ens.nDPWL()>0) {delayTableControl.getDefaultColumns().add("L");nrecords+=1;}
       
        
        for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nDParticles();j++){
                if(!delayTableControl.getDefaultColumns().contains("COMM")&&ens.levelAt(i).DParticleAt(j).nComments()>0)
                    delayTableControl.getDefaultColumns().add("COMM");
                if(!delayTableControl.getDefaultColumns().contains("COMM")&&ens.levelAt(i).DParticleAt(j).nContRecords()>0)
                    delayTableControl.getDefaultColumns().add("COMM");
            }
        }
        for(int i=0;i<ens.unpDParticles().size();i++){
            DParticle dp=ens.unpDParticles().get(i);
            if(!delayTableControl.getDefaultColumns().contains("COMM")&&dp.nComments()>0)
                delayTableControl.getDefaultColumns().add("COMM");
            if(!delayTableControl.getDefaultColumns().contains("COMM")&&dp.nContRecords()>0)
                delayTableControl.getDefaultColumns().add("COMM");
        }
        
        delayTableControl.setNRecords(nrecords);
        
        delayTableControl.setSortDelays(false);
        
        
        //for atomic table control
        nrecords=0;
        if(!ens.atomicRad().isEmpty()) {
            XRayTableControl.getDefaultColumns().add("NAME");nrecords+=1;
            
            XRayTableControl.getDefaultColumns().add("E");nrecords+=1;
            XRayTableControl.getDefaultColumns().add("DE");
            
            XRayTableControl.getDefaultColumns().add("IM");nrecords+=1;
            XRayTableControl.getDefaultColumns().add("DIM");
                    
            AugerTableControl.getDefaultColumns().add("NAME");nrecords+=1;
            
            AugerTableControl.getDefaultColumns().add("E");nrecords+=1;
            AugerTableControl.getDefaultColumns().add("DE");
            
            AugerTableControl.getDefaultColumns().add("IM");nrecords+=1;
            AugerTableControl.getDefaultColumns().add("DIM");
            
            for(int i=0;i<ens.atomicRad().xRaysV().size();i++) {
                AtomicData ad=ens.atomicRad().xRaydAt(i);
                if(XRayTableControl.getDefaultColumns().contains("COMM") && ad.nComments()>0)
                    XRayTableControl.getDefaultColumns().add("COMM");
            }
            
            for(int i=0;i<ens.atomicRad().AugersV().size();i++) {
                AtomicData ad=ens.atomicRad().AugerAt(i);
                if(AugerTableControl.getDefaultColumns().contains("COMM") && ad.nComments()>0)
                    AugerTableControl.getDefaultColumns().add("COMM");
            }
        }
        
        
        sortComments();
        
        
        levelTableControl.setShowDE(true);
        gammaTableControl.setShowDE(false);
        decayTableControl.setShowDE(false);

        XRayTableControl.setShowDE(false);
        AugerTableControl.setShowDE(false);

        //default comment-column widths, which can be set in control file or when making tables if autoAdjust=true.
        levelTableControl.setCommentColumnWidths(17-2.0f*levelTableControl.getNRecords());
        gammaTableControl.setCommentColumnWidths(17-2.0f*gammaTableControl.getNRecords());
        delayTableControl.setCommentColumnWidths(17-2.0f*delayTableControl.getNRecords());
        decayTableControl.setCommentColumnWidths(17-2.0f*decayTableControl.getNRecords());
       
        if(levelTableControl.getCommentColumnWidth()<NDSConfig.MIN_COMMENT_WIDTH) levelTableControl.setCommentColumnWidths(NDSConfig.MIN_COMMENT_WIDTH);
        if(gammaTableControl.getCommentColumnWidth()<NDSConfig.MIN_COMMENT_WIDTH) gammaTableControl.setCommentColumnWidths(NDSConfig.MIN_COMMENT_WIDTH);
        if(decayTableControl.getCommentColumnWidth()<NDSConfig.MIN_COMMENT_WIDTH) decayTableControl.setCommentColumnWidths(NDSConfig.MIN_COMMENT_WIDTH);
        if(delayTableControl.getCommentColumnWidth()<NDSConfig.MIN_COMMENT_WIDTH) delayTableControl.setCommentColumnWidths(NDSConfig.MIN_COMMENT_WIDTH);
       
        altID="";

        //set up defaults for drawings
        if(NDSControl.autoAdjust) {
            levelTableControl.setIsNewCommentWidth(false);
            gammaTableControl.setIsNewCommentWidth(false);
            decayTableControl.setIsNewCommentWidth(false);
            delayTableControl.setIsNewCommentWidth(false);  
            
            levelTableControl.setUseAutoComWidth(true);
            gammaTableControl.setUseAutoComWidth(true);
            decayTableControl.setUseAutoComWidth(true);
            delayTableControl.setUseAutoComWidth(true);
        }else{
            levelTableControl.setUseAutoComWidth(false);
            gammaTableControl.setUseAutoComWidth(false);
            decayTableControl.setUseAutoComWidth(false);
            delayTableControl.setUseAutoComWidth(false);
        }
        
        
        //initialize decay/level drawing control settings
        decayDrawingControl=new DrawingControl();//for level and decay scheme       
        decayDrawingControl.setDrawn(false);
        if(gammaTableControl.isDrawn() && levelTableControl.isDrawn()) 
            decayDrawingControl.setDrawn(true);
        
        if(decayTableControl.isDrawn() || delayTableControl.isDrawn())
            decayDrawingControl.setDrawn(true);
        
        String decayType=ens.decayRecordType();
        String decayNameInDSID=ens.decayTypeInDSID().trim();
        
        //it could be that there is no decay record in the decay dataset
        if(decayType.trim().length()==0 && decayNameInDSID.length()>0){
            if(decayNameInDSID.equals("EC") || decayNameInDSID.equals("B+") || decayNameInDSID.equals("EC+B+"))
                decayType="E";
            else if(decayNameInDSID.equals("B-"))
                decayType="B";
            else
                decayType=decayNameInDSID;
        }
        
        //for inset table in decay scheme
        float offset=0;
        float colW=29;//in point
        float arrowW=20;//width of arrow, should be consistent with the number in DecayChart
        
        //offset is from the edge of the daughter levels to the left edge of the cloest column
        //for E and A decay, table is placed at right;
        //for B it is at left
        if(decayType.equals("E")){
            offset=arrowW+colW;
        }else if(decayType.equals("A")){
            offset=arrowW+colW*1.5f;
        }else if(decayType.equals("B")){//
            offset=-(arrowW+29*1.1f);
        }else
            offset=29f;
        

        decayDrawingControl.setDecayTableXOffset(offset*NDSConfig.POINT2CM);
        
        
        //initialize band drawing control settings
        bandDrawingControl=new DrawingControl();
        if(ens.nBands()>0)
            bandDrawingControl.setDrawn(true);
        else
            bandDrawingControl.setDrawn(false);
        
               
    }
    
    public void setTableControl(String name,TableControl tc){
        if(name.toUpperCase().equals("LEVEL"))
            levelTableControl=tc.clone();
        else if(name.toUpperCase().equals("GAMMA"))
            gammaTableControl=tc.clone();
        else if(name.toUpperCase().equals("DECAY"))
            decayTableControl=tc.clone();
        else if(name.toUpperCase().equals("DELAY"))
            delayTableControl=tc.clone();       
    }
    
    public void setDrawingControl(String name,DrawingControl dc){
        if(name.toUpperCase().equals("BAND"))
            bandDrawingControl=dc.clone();
        else if(name.toUpperCase().equals("DECAY") || name.toUpperCase().equals("LEVEL"))
            decayDrawingControl=dc.clone();     
    }
    
    /**interprets a control file and sets the appropriate variables 
     * @throws Exception */
    public void setTableControls(Vector<String> v,boolean sup) throws Exception{
        //sets everything to false 
        isDrawn=true;
        showSuppressed=!sup;
        levelTableControl.setDrawn(false); 
        decayTableControl.setDrawn(false);
        gammaTableControl.setDrawn(false);
        decayDrawingControl.setDrawn(false);
        unplcTableControl.setDrawn(false);
        //if(s) decayDrawing.drawn=true;
        isNewPage=false;
        isSortGammas=true;
        isCombineLG=false;
        String temp,sub;
        for(int i=0;i<v.size();i++){
            temp=(String)v.elementAt(i);
            sub=temp.substring(0,3);
            if(sub.equals("LEV")){
                levelTableControl.setSuppressStatus(sup);
                levelTableControl.loadControlLine(temp);
            }else if(sub.equals("GAM")){
                gammaTableControl.setSuppressStatus(sup);
                gammaTableControl.loadControlLine(temp);
                suppressGammasCont(sup);
            }else if(sub.equals("UNP")){
                unplcTableControl.setSuppressStatus(sup);
                unplcTableControl.setDrawn(true);
                unplcTableControl.loadControlLine(temp);
            }else if(sub.equals("BET")||sub.equals("ALP")||sub.equals("ELE")){
                decayTableControl.setSuppressStatus(sup);
                decayTableControl.loadControlLine(temp);
                suppressDecaysCont(sup);
            }else if(sub.equals("PAR")){
                delayTableControl.setSuppressStatus(sup);
                delayTableControl.loadControlLine(temp);                
            }else if(sub.equals("COM")){
                isCombineLG=true;
            }else if(sub.equals("DRA")){
                decayDrawingControl.loadControlLine(temp);
            }else if(sub.equals("BAN")){
                if(!bandDrawingControl.isDrawn()) bandDrawingControl.loadControlLine(temp);
            }else if(sub.equals("ALT")){
                altID=temp.substring(5).trim();
            }else if(sub.equals("PAG")){
                isNewPage=true;
            }else if(sub.equals("REO")){
                isSortGammas=false;
                gammaTableControl.setSortGammas(false);
            }else if(sub.equals("XRE")){
                try{nXRefs=Integer.parseInt(temp.substring(4).trim());}
                catch(Exception e){}
                if(nXRefs<=0){
                    levelTableControl.getDefaultColumns().removeElement("XREF");
                }
            }
        }
    }
    
    public void suppressContRrecords(boolean sup){
        suppressGammasCont(sup);
        suppressDecaysCont(sup);
    }
    
    /** If the gammas continuation records are suppressed, this stops the suppressed ones from getting printed*/
    private boolean suppressGammasCont(boolean sup){
        boolean suppressComment=true;
        showSuppressed=!sup;
        for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nGammas();j++){
                if(ens.levelAt(i).gammaAt(j).nComments()>0) 
                	suppressComment=false;
                for(int k=0;k<ens.levelAt(i).gammaAt(j).nContRecords();k++){
                    ContRecord cr=ens.levelAt(i).gammaAt(j).contRecordAt(k);
                	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                    	suppressComment=false;
                }
            }
            if(!suppressComment)break;
        }
        
        for(int i=0;i<ens.nUnpGammas();i++){
            if(ens.unpGammaAt(i).nComments()>0) suppressComment=false;
            for(int k=0;k<ens.unpGammaAt(i).nContRecords();k++){
            	ContRecord cr=ens.unpGammaAt(i).contRecordAt(k);
            	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                	suppressComment=false;
            }
            if(!suppressComment)break;
        }
        
        return gammaTableControl.refreshColumns(suppressComment);
    }
    /** If the decay continuation records are suppressed, this stops the suppressed ones from getting printed */
    private boolean suppressDecaysCont(boolean sup){
        boolean suppressComment=true;
        showSuppressed=!sup;
        for(int i=0;i<ens.nLevels();i++){
            Level l=ens.levelAt(i);
            if(ens.decayRecordType().equals("B")){
                for(int j=0;j<l.nBetas();j++){
                    Beta b=l.betaAt(j);
                    if(b.nComments()>0) suppressComment=false;
                    for(int k=0;k<b.nContRecords();k++) {
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                        	suppressComment=false;
                    }
                }
            }else if(ens.decayRecordType().equals("A")){
                for(int j=0;j<l.nAlphas();j++){
                    Alpha b=l.alphaAt(j);
                    if(b.nComments()>0) suppressComment=false;
                    for(int k=0;k<b.nContRecords();k++) { 
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                        	suppressComment=false;
                    }
                }                
            }else if(ens.decayRecordType().equals("E")){
                for(int j=0;j<l.nECBPs();j++){
                    ECBP b=l.ECBPAt(j);
                    if(b.nComments()>0) suppressComment=false;
                    for(int k=0;k<b.nContRecords();k++) { 
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                        	suppressComment=false;
                    }
                }                
            }
            if(!suppressComment) break;
        }
        
        if(ens.fullDSId().contains("DECAY")){
            if(ens.decayRecordType().equals("B")){
                for(int j=0;j<ens.nUnpBetas();j++){
                    Beta b=ens.unpBetaAt(j);
                    if(b.nComments()>0) suppressComment=false;
                    for(int k=0;k<b.nContRecords();k++) { 
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                        	suppressComment=false;
                    }
                }
            }else if(ens.decayRecordType().equals("A")){
                for(int j=0;j<ens.nUnpAlphas();j++){
                    Alpha b=ens.unpAlphaAt(j);
                    if(b.nComments()>0) suppressComment=false;
                    for(int k=0;k<b.nContRecords();k++) {
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                        	suppressComment=false;
                    }
                }                
            }else if(ens.decayRecordType().equals("E")){
                for(int j=0;j<ens.nUnpECBPs();j++){
                    ECBP b=ens.unpECBPAt(j);
                    if(b.nComments()>0) suppressComment=false;
                    for(int k=0;k<b.nContRecords();k++) {
                        ContRecord cr=b.contRecordAt(k);
                    	if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) )
                        	suppressComment=false;
                    }
                }                
            }
        }

       
        return decayTableControl.refreshColumns(suppressComment);
    }    
    
    /**sorts the ensdf comments into the different comment types
     * @throws Exception */
    private void sortComments() throws Exception{
        int indexOfLastExpGCom=0;//index of last explicit gamma comment
        int indexOfLastExpDCom=0;//index of last explicit decay comment
        int indexOfLastExpPCom=0;//index of last explicit delay particle comment
        int indexOfLastExpMXCom=0;//index of last explicit X ray atomic radiation comment
        int indexOfLastExpMACom=0;//index of last explicit Auger electron atomic radiation comment
        
        Vector<Comment> levelComments=levelTableControl.getComments();
        Vector<Comment> gammaComments=gammaTableControl.getComments();
        Vector<Comment> decayComments=decayTableControl.getComments();
        Vector<Comment> delayComments=delayTableControl.getComments();
        Vector<Comment> XRayComments=XRayTableControl.getComments();
        Vector<Comment> AugerComments=AugerTableControl.getComments();
        
        UserDictionary.clear();//clear user-defined dictionary after each dataset; it is local.
        
        for(int i=0;i<ens.nComments();i++){
            Comment c=ens.commentAt(i);
            String type=c.type();
            //debug
            //System.out.println("In EnsdfTableData: com head="+c.head()+" body="+c.body()+" type="+c.type());
            
            String temp="";
            if(c.nHeads()>0 && c.headAt(0).toUpperCase().equals("DICT")){//user-defined dictionary, override the translation in built-in dictionary
                UserDictionary.addEntry(c.body());       
            }
            else if(type.equals("L ")) 
                levelComments.add(c);
            else if(type.equals("G ")){ 
                if(c.isExplicit()){
                    gammaComments.insertElementAt(c,indexOfLastExpGCom);
                    indexOfLastExpGCom++;
                }
                else
                    gammaComments.addElement(c);            
            }
            else if(type.equals("B ")||type.equals("E ")||type.equals("A ")){
                if(c.isExplicit()){
                    decayComments.insertElementAt(c,indexOfLastExpDCom);
                    indexOfLastExpDCom++;
                }
                else
                    decayComments.addElement(c); 
            }
            else if(type.charAt(0)=='D'|| (type.charAt(0)==' '&& (type.charAt(1)+"").equals(ens.decayTypeInDSID()) ) ){ 
                if(c.isExplicit()){
                    delayComments.insertElementAt(c,indexOfLastExpPCom);
                    indexOfLastExpPCom++;
                }
                else
                    delayComments.addElement(c); 
            }else if(type.charAt(0)=='M') {
                char atomicType=type.charAt(1);
                
                //System.out.println("EnsdfTableData 843: c.isExplicit()="+c.isExplicit()+" atomic type="+atomicType+" indexOfLastExpMXCom="+indexOfLastExpMXCom+" indexOfLastExpMACom="+indexOfLastExpMACom);
                
                if(c.isExplicit()){
                    if(atomicType=='X') {
                        XRayComments.insertElementAt(c,indexOfLastExpMXCom);
                        indexOfLastExpMXCom++;
                    }else if(atomicType=='A') {
                        AugerComments.insertElementAt(c,indexOfLastExpMACom);
                        indexOfLastExpMACom++;
                    }

                }
                else {
                    if(atomicType=='X') 
                        XRayComments.addElement(c); 
                    else if(atomicType=='A')
                        AugerComments.addElement(c); 
                }
            }
            else if(type.charAt(0)=='N'){//comments for N record: NR and NT as general comments above gamma table, NB and NP for decay or delay table 
                Comment c1;
                
                if(c.head().contains("NR") || c.head().contains("NT")){
                    c1=c.clone();
                    c1.headV().clear();
                    
                    int n1=c.headV().indexOf("NR");
                    int n2=c.headV().indexOf("NT");
                    String h1="I|g normalization";
                    String h2="I(|g+ce) normalization";                 
                    int ntemp=-1;
                    String htemp="";
                    if(n1>n2){
                        ntemp=n1;
                        htemp=h1;
                        n1=n2;
                        h1=h2;
                        n2=ntemp;
                        h2=htemp;
                    }
                    
                    if(n1>=0){
                        c1.headV().add(c.headV().get(n1));
                        c1.headV().add(c.headV().get(n2));
                        temp=h1+", "+h2+": ";
                    }
                    else if(n2>=0){
                        c1.headV().add(c.headV().get(n2));
                        temp=h2+": ";
                    }

                    temp+=c1.body().trim();
                    c1.setBody(temp);
                    //gammaComments.add(c1);//this comment is printed above gamma table, so it does not affect the print order of footnote comments 
                    int pos=0;
                    for(int j=0;j<gammaComments.size();j++){
                        String head=gammaComments.get(j).head();
                        if(!head.contains("NR") && !head.contains("NT")){
                            pos=j;
                            break;
                        }
                    }
                    gammaComments.insertElementAt(c1, pos);//put normalization comment on top
                    indexOfLastExpGCom++;
                }

                if(c.head().contains("NB")){
                    c1=c.clone();
                    c1.headV().clear();
                    c1.headV().add("NB");
                    temp="I|b normalization: ";                 
                    temp+=c1.body().trim();
                    c1.setBody(temp);                   
                    //decayComments.add(c1);
                    decayComments.insertElementAt(c1, 0);
                    indexOfLastExpDCom++;
                    
                    //System.out.println(" "+c1.body());
                }
                if(c.head().contains("NP")){
                    c1=c.clone();
                    c1.headV().clear();
                    c1.headV().add("NP");
                    temp="Particle normalization: ";                
                    temp+=c1.body().trim();
                    c1.setBody(temp);
                    //delayComments.add(c1);
                    delayComments.insertElementAt(c1, 0);
                    indexOfLastExpPCom++;
                }
                if(c.head().contains("BR")){
                    c1=c.clone();
                    c1.headV().clear();
                    c1.headV().add("BR");
                    if(type.charAt(1)==' ')
                        ens.parentAt(0).commentV().add(c1);                  
                    else{
                        String ID=type.substring(1);
                        for(int j=0;j<ens.nParents();j++){
                            if(ens.parentAt(j).getIndexS().equals(ID)){
                                ens.parentAt(j).commentV().add(c1);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        parseReLABEL(levelTableControl);
        parseReLABEL(gammaTableControl);
        parseReLABEL(decayTableControl);
        parseReLABEL(delayTableControl);
        parseReLABEL(XRayTableControl);
        parseReLABEL(AugerTableControl);
        
        //store local dictionary
        if(!UserDictionary.isEmpty())
            setDICT(UserDictionary.getDICT());
    }
    
    private void parseReLABEL(TableControl tc) throws Exception {
        //see if any of the columns need to be relabelled
        int ix;
        Comment c;
        String temp="",tempLabel="",head="";;
        
        HashMap<String,String> labelMap=tc.getRelabelMap();
        HashMap<String,String> translatedLabelMap=tc.getTranslatedLabelMap();
        
        for(int i=0;i<tc.getComments().size();i++){
            c=tc.getComments().elementAt(i);
            head=c.head();
            temp=c.body();
            
            //System.out.println("EnsdfTableData 1057: com type="+c.type()+" head="+head+" body="+temp);
            
            ix=temp.indexOf("LABEL=");
            if(ix>=0) 
                temp=temp.substring(ix+6);
            else 
                continue;
            
            tempLabel=temp.trim();
            
            if(c.isBigC())
                temp=Translator.process(temp);
            else
                temp=Translator.translate(temp);
            
            labelMap.put(head,tempLabel);
            translatedLabelMap.put(head,temp);
        }
    }
    
    public int getNID(){return NID;}
    public String getLinkID(){return linkID;}
    
    public ENSDF getENSDF(){return ens;}
    public int getNXRefs(){return nXRefs;}
    public String getAltID(){return altID;}  
    public TableControl getLevelTableControl(){return levelTableControl;}
    public TableControl getGammaTableControl(){return gammaTableControl;}
    public TableControl getDecayTableControl(){return decayTableControl;}
    public TableControl getDelayTableControl(){return delayTableControl;}
    public TableControl getUnplcTableControl(){return unplcTableControl;}
    
    public TableControl getXRayTableControl(){return XRayTableControl;}
    public TableControl getAugerTableControl(){return AugerTableControl;}
    
    public DrawingControl getDecayDrawingControl(){return decayDrawingControl;}
    public DrawingControl getBandDrawingControl(){return bandDrawingControl;}
    public void setDecayDrawn(boolean b){decayDrawingControl.setDrawn(b);}
    
    public void setNID(int id){NID=id;}
    public void setLinkID(String link){linkID=link;}
    
    public void setDrawn(boolean b){isDrawn=b;}
    public void setCombineLG(boolean b){isCombineLG=b;}
    public void setSortGammas(boolean b){isSortGammas=b;gammaTableControl.setSortGammas(b);}
    public void setNewPage(boolean b){isNewPage=b;}    
    public void setShowSuppressed(boolean b){showSuppressed=b;}
    public void setUpdate(boolean b,String s){update=b;updCom=s;}
    public void setAltID(String s){altID=s;}
    public void setNXrefs(int n){nXRefs=n;}

    //local dictionary
    public void setDICT(HashMap<String,String> d){
        dict.clear();
        for(Map.Entry<String,String> e:d.entrySet())
            dict.put(e.getKey(),e.getValue());

    }
    
    public HashMap<String,String> getDICT(){return dict;}
    
    public boolean isDrawn(){return isDrawn;}
    public boolean isShowSuppressed(){return showSuppressed;}
    public boolean isCombineLG(){return isCombineLG;}
    public boolean isUpdate(){return update;}
    public String updCom(){return updCom;}
    public boolean isNewPage(){return isNewPage;}   
    public boolean isSortGammas_old(){return isSortGammas;}
    public boolean isSortGammas(){return gammaTableControl.isSortGammas();} 
    public boolean isSortDelays(){return delayTableControl.isSortDelays();}
    

    
    
    public void writeControl(java.io.Writer out,String type)throws IOException{
        if(isDrawn){
            out.write(ens.lineAt(0)+"\n");
            if(levelTableControl.isDrawn()){
                out.write("LEVEL");
                levelTableControl.writeControlLine(out);
            }
            if(gammaTableControl.isDrawn()){
                out.write("GAMMA");
                gammaTableControl.writeControlLine(out);
            }
            if(unplcTableControl.isDrawn()){
                out.write("UNPLACED");
                unplcTableControl.writeControlLine(out);
            }
            if(decayTableControl.isDrawn()) {
                if(ens.nTotBet()>0) out.write("BETA");
                else if(ens.nTotECBP()>0) out.write("ELECTRON");
                else if(ens.nTotAlp()>0) out.write("ALPHA");
                decayTableControl.writeControlLine(out);
            }
            if(delayTableControl.isDrawn()){
                out.write("PARTICLE");
                delayTableControl.writeControlLine(out);
            }
            if(XRayTableControl.isDrawn()){
                out.write("XRAY");
                XRayTableControl.writeControlLine(out);
            }
            if(AugerTableControl.isDrawn()){
                out.write("AUGER");
                AugerTableControl.writeControlLine(out);
            }
            if(decayDrawingControl.isDrawn()||type.equals("FULL")){
                out.write("DRAW");
                decayDrawingControl.writeControlLine(out);
            }
            if(bandDrawingControl.isDrawn()){
                out.write("BAND");
                bandDrawingControl.writeControlLine(out);
            }        
            if(isNewPage) out.write("PAGE\n");
            if(!gammaTableControl.isSortGammas())out.write("REORDER\n");
            if(isCombineLG)out.write("COMBINE\n");
            if(nXRefs!=25) out.write("XREF "+nXRefs+"\n");
            if(altID.length()>0) out.write("ALTID "+altID);
        }
    }
}
