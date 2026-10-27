/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ensdfparser.nds.latex;

import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.ensdf.Record;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.*;
import ensdfparser.nds.util.Str;
/**
 *
 * @author Jeremie Choquette
 * updated by Jun Chen in 2015 
 */
public class DelayedTableMaker extends TableMaker{
    private int currentPanelNo;
    private ensdfparser.ensdf.Nucleus EI=new ensdfparser.ensdf.Nucleus();
    private String dpType="";
    
	TablePanel T;
    
    //private EnsdfTableData etd;
    //@SuppressWarnings("unused")
	//private MassChain data;
    //private TableControl tc;
    
    private boolean pastBreak=false;
    private String leftoverOfLastCom="";
    
    Vector <DParticle> Delays=new Vector<DParticle>();
    
    public DelayedTableMaker(){}
    
    public DelayedTableMaker(EnsdfTableData etd,MassChain data){
        this.etd=etd;
        this.data=data;
        this.tableType="DELAY";
        this.tc=etd.getDelayTableControl();
        
        ENSDF ens=etd.getENSDF();
        if(ens!=null){
        	try {
            	String s=ens.decayTypeInDSID();
            	Nucleus pNuc=ens.parentAt(0).nucleus();
            	int pZ=pNuc.Z();
            	int pA=pNuc.A();
            	if(s.startsWith("B+")||s.startsWith("EC"))
            		EI.setZA(pZ-1,pA);
            	else if(s.startsWith("B-"))
            		EI.setZA(pZ+1,pA); 
            	else if(s.startsWith("A"))
            		EI.setZA(pZ-2,pA-4);
            	
                if(ens.particleType().equalsIgnoreCase("P")){
                	dpType="p";           	        	
                }else if(ens.particleType().equalsIgnoreCase("N")){
                	dpType="n";   	
                }else if(ens.particleType().equalsIgnoreCase("A")){
                	dpType="$\\alpha$";    	
                }else if(ens.particleType().equalsIgnoreCase("T")){
                	dpType="t";    	
                }else if(ens.particleType().equalsIgnoreCase("D")){
                	dpType="d";    	
                }else
                	dpType=ens.particleType().toLowerCase();
                
        	}catch(Exception e) {
        		
        	}

        }
        
        initDelays();
        
        init();
    }
    
    private void initDelays(){
    	ENSDF ens=etd.getENSDF();  

        for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nDParticles();j++)
                Delays.addElement(ens.levelAt(i).DParticleAt(j));
        }
        
        
        if(etd.isSortDelays()) {//for unplaced; placed dps are always sorted by energy
            for(int i=0;i<ens.nUnpDParticles();i++){
                Delays.addElement(ens.unpDParticleAt(i));
            }
        }
        
        DParticle[] TEMP=new DParticle[Delays.size()];

        for(int i=0;i<Delays.size();i++) {
            TEMP[i]=Delays.elementAt(i);
        }
        
        Arrays.sort(TEMP,new DPComparator());
        Delays=new Vector<DParticle>();
        
        for(int i=0;i<TEMP.length;i++)             
            Delays.addElement(TEMP[i]);
            
        //check for dp records with empty energy record 

        float e1=1.0f,e2=2000;
        if(etd.isSortDelays())//Delays include unplaced if isSortDelays=true
        	e1=1.0f;
        else
        	e1=100.0f;
        
        int n=Delays.size();
        for(int i=0;i<n;i++){
        	DParticle dp=Delays.get(i);
        	float e=e1+i*(e2-e1)/n;
        	String es=String.valueOf(e);
        	dp.setERPF(e);
        	dp.setERPS(es);
        }
        
        //if etd.isSortDelays=false, unplaced delays is not included in Delays.
        //ERPF of dp needs to be reset
        if(!etd.isSortDelays()) {

            int nPlaced=Delays.size();
            int nUnp=ens.nUnpDParticles();
            
            Vector<DParticle> tempV=new Vector<DParticle>();
            tempV.addAll(Delays);
            
            for(int i=0;i<nUnp;i++)
                Delays.insertElementAt(ens.unpDParticleAt(i),i);             

            if(nPlaced>0 && nUnp>0) {
                e1=1.0f;               
                e2=tempV.get(0).ERPF();
            }else {
                e1=1.0f;
                e2=100.0f;
            }
            
            for(int i=0;i<nUnp;i++){
                float e=e1+i*(e2-e1)/nUnp;
                DParticle dp=ens.unpDParticleAt(i);
                dp.setERPF(e);
                dp.setERPS(String.valueOf(e));
            }
        }    	
    }
    
    /**Implement the abstract method in parent class*/
    public TablePanel makeTablePanel(int panel,boolean option)throws Exception{
    	return makeTablePanel(panel);
    }
    
    /**Writes a Delayed Particle Table*/
    public TablePanel makeTablePanel(int panel)throws Exception{
    	if(autoAdjust)
    		return makeTablePanel_auto(panel);
    	else
    		//return makeTablePanel_manual(panel);
    		return makeTablePanel_auto(panel);
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TablePanel makeTablePanel_auto(int panel)throws Exception{
		
		ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        Vector<String> Set=new Vector<String>();
        currentPanelNo=panel;
        String row="";
        String particle="";
        
        float maxRowWidth=0;
        float maxCommentWidth=0;
        float maxDataWidth=0;
        float dataHeadWidth=0;//including empty column if existing
        float width=0;
        int lineCount=0;
        float linePos=0;
                
        this.maxCommentWidth=0;
        
        boolean showSuppressed=etd.isShowSuppressed();
       
        int nColumns;
        Vector<String> columns;

        	
        columns=tc.getPanelColumns(panel);

        nColumns=columns.size();
        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getDefaultColumns().contains("COMM"))
        	T.addComLoc(tc.getDefaultColumns().indexOf("COMM"));
        
        float comWidth=tc.getCommentColumnWidth();
        if(comWidth==-1.0f) comWidth=nColumns*1.7f;

        if(ens.particleType().equalsIgnoreCase("P")){dpType="p";particle="Proton";}
        if(ens.particleType().equalsIgnoreCase("N")){dpType="n";particle="Neutron";}
        if(ens.particleType().equalsIgnoreCase("A")){dpType="$\\alpha$";particle="Alpha";}
        
        T.setSpare(particle);
                
        //for(DParticle dp:Delays) 
        //	System.out.println(" DP: "+dp.ERPS()+" EI="+dp.EIS()+" EL="+dp.getLEVS());
        
        /*the vectors which will contain the comments,
         *one goes at the top, one goes to footnotes at bottom
         */

        Vector<Comment> topcom=new Vector<Comment>();
        Vector<Comment> labcom=new Vector<Comment>();


        /*
         * add code for processing NR and NT comments for N record (Jun Chen, 9/30/2015)
         * NOTE: ens.comments contain all comments including comments for N, P and Q records
         *  
         */
        Comment com;
        Vector<String> tempComs;
        
        Footnotes footnotes=new Footnotes(ens);
        footnotes.findFootnotes(tc);//wrap of footnote comments and flag information
        
        //set link ID for each footnote: etd.getLinkID()+"GAMMA"+index
        footnotes.setLinkIDs(etd.getLinkID()+"DELAY");
        
        for(int i=0;i<tc.getComments().size();i++){
            com=(Comment)tc.getComments().elementAt(i);
            
            if(com.body().contains("LABEL=")){labcom.addElement(com);continue;}
            
            if(tc.isTopCommentOnly(com)){
            	if(com.nHeads()>0 && !com.type().equals("N "))//head in "N" comment is not printed
            		topcom.addElement(tc.extractTopComment(com));
            	else
            		topcom.addElement(com);
            }
            else if(tc.isAlsoTopComment(com))
            	topcom.addElement(tc.extractTopComment(com));      
        }
        
        
        for(int i=0; i<topcom.size();i++){
            Comment c = new Comment();
            c=(Comment)topcom.elementAt(i);
        	tempComs=wrapENSDFCommentTranslated(c,HEADNOTE_WIDTH,"","",c.isBigC());  
        	
        	if(tempComs.size()>0){
        		tempComs.insertElementAt("\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+tempComs.elementAt(0),0);
        		tempComs.removeElementAt(1);
        	}
        	
        	Set.addAll(tempComs);
        	lineCount+=tempComs.size();  
        	linePos+=tempComs.size()*NDSConfig.HEAD_COMMENT_LINEWIDTH;
        }
        T.setGenComments(Set);
        
        //one blank line before and after general comment
        if(Set.size()>0){
        	lineCount+=2;
            linePos+=2*NDSConfig.HEAD_COMMENT_LINEWIDTH;;
        }
        
        Vector<String> v;
        Set=new Vector<String>();
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            if(t.type()=='D') {
            	v=makeTableRecords(t,tc.isFormatTables());
            	Set.addAll(v);
            	lineCount+=this.nTableRecordsLines;
            }
        }
        T.setTableRecords(Set);
        T.setNTableRecordsLines(this.nTableRecordsLines);
        T.setTableRecordsHeight(this.tableRecordsHeight);
        
        if(panel<=0){
        	T.setRelHeadLineNo(lineCount+2);//lineCount+(line line space)+(one line headline column names)
        	
        	//linePos+=2*Config.DEFAULT_LINEWIDTH;//if counting lines separately, not using this.tableRecordHeight, slightly different
        	linePos+=tableRecordsHeight+NDSConfig.TABLE_TOP_MARGIN+NDSConfig.HEAD_COMMENT_LINEWIDTH+0.2;//if using this.tableRecordsHeight, more precise
        	
        	T.setRelHeadLinePos(linePos);
        }
        else{
        	T.setRelHeadLineNo(NDSConfig.DEFAULT_HEADLINE_NO);
        	T.setRelHeadLinePos(NDSConfig.DEFAULT_HEADLINE_POS);
        }
                
        Set=new Vector<String>();
        Set.addElement(makeTableHead(footnotes, tc,dpType,EI,ens,labcom));
        T.setFirstHead(Set);
        
        Set=new Vector<String>();

        row+="\\multicolumn{"+String.valueOf(nColumns)+"}{c}{\\underline{Delayed "+particle+"s (continued)}}";
        Set.addElement(row); row="";
        row="\\multicolumn{"+nColumns+"}{c}{~}";
        Set.addElement(row); row="";
        Set.addElement(makeTableHead(footnotes, tc,dpType,EI,ens,labcom));
        T.setHead(Set);
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement("\\multicolumn{"+String.valueOf(nColumns)+"}{c}{Continued on next page (footnotes at end of table)}");
        T.setTail(Set);
        
        Set=new Vector<String>();

        //print out the footnoted comments
        row="";
        String tempHead="",head="";
        
        for(int i=0;i<footnotes.size();i++){
            Comment c = new Comment();
            c=(Comment)footnotes.commentAt(i);
            
        	//System.out.println("     c.flag="+c.flags()+"   ens.gammaFlags="+ens.gammaFlags()+" com="+c.body()+" c.head="+c.head());       	           
            
            tempHead="h ";//arbitrary, each symbol is one character
       
            head=footnotes.footnoteMarkAt(i)+" ";
        	
        	tempComs=wrapENSDFCommentTranslated(c,FOOTNOTE_WIDTH,tempHead,head,c.isBigC(),2);//leave 2-spaces indent after first line      	            
        	Set.addAll(tempComs);
        }
        
        T.setLastTail(Set);
        
        Set=new Vector<String>();
        
        float min=-100000;
        float max=100000;

        if(panel<0){//for printing the whole table in a single panel with no beak and splitting    
        }
        else{       	       	
        	if(currentPanelNo>0&&currentPanelNo<=tc.getBreakPoints().size())
        		min=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo-1).toString());
            if(currentPanelNo<tc.getBreakPoints().size())
        	    max=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo).toString());
        }
      
        lineCount=0;
        this.nTableBodyLines=0;
        this.tableBodyHeight=0;
        boolean isLastDelayInPrevPanel=false;
        float nextEF=0;
        
        boolean pastMax=false,pastMin=false;
        
        this.pastBreak=false;
         
        DParticle firstDelay=null,lastDelay=null;
        
        //System.out.println(" min="+min);
        
        for(int i=0; i<Delays.size();i++){
            DParticle dp=new DParticle();
            dp=Delays.elementAt(i);
            DParticle ndp;//next dp
            
            if(i<Delays.size()-1){
            	ndp=Delays.elementAt(i+1);
           		nextEF=ndp.ERPF();        	
            }
            else{
            	ndp=null;
            	nextEF=dp.ERPF()+100;
            }
            
          //determine whether this dp should be drawn in this panel
            if(dp.ERPF()>=min) {
            	pastMin=true;
            	if(firstDelay==null)
            		firstDelay=dp;
            }
            
            if(dp.ERPF()>=max && panel>=0) {
            	if(!this.pastBreak) {
            		tc.setFirstLineEnergy(panel+1, dp.ERPF()); 
                    if(dp.getLEVI()>=0) 
                    	tc.setFirstLineLevelEnergy(panel+1, ens.levelAt(dp.getLEVI()).EF());
                    else
                    	tc.setFirstLineLevelEnergy(panel+1, -1);
            	}
            	this.pastBreak=true;
            	pastMax=true;
            	
            	if(lastDelay==null && i>0)
            		lastDelay=Delays.elementAt(i-1);
            }
            else if(dp.ERPF()<max && (nextEF>=max||i==Delays.size()-1)) {
            	if(lastDelay==null)
            		lastDelay=dp;
            }
            
            isLastDelayInPrevPanel=false;
            if(dp.ERPF()<min && nextEF>min){//break in the middle of comments of last delay in previous panel, so print the remaining comments in the new panel, only when autoAdjust=true
            	pastMin=true;
            	isLastDelayInPrevPanel=true;
            }
            
            if(pastMax || !pastMin) 
            	continue;
            
            
            if(!(dp.getLEVI()>=0)||dp.getLEVS()==null)
            	dp.setLEVS("Unplaced");
            
            Vector out=new Vector();
            this.nCommentLines=0;//incremented in findMaxCommentWidth() in makeSingleDelayed
            
            if(tc.getFcolumns()==null||checkFColumns(tc,dp))
            	out=makeSingleDelayed_auto(currentPanelNo,dp, etd,T,footnotes, showSuppressed,isLastDelayInPrevPanel,nextEF,min,max);//page breaking in the middle of comment lines is handled in this call
                                                                                                                            //this.nTableBodyLines and this.TableBody height are incremented in this call
            
            
            if(out==null) 
            	continue;
            else{ 
            	Set.addAll(out);
            	lineCount+=out.size();
            	//this.nTableBodyLines+=out.size();
            	
                //if(this.nCommentLines>0)
                //	this.nTableBodyLines+=this.nCommentLines-1;                	
            }
            
            
            	
            width=getEntryLength(dp,tc.getPanelColumns(panel),ens);
            
            if(width>maxDataWidth) 
            	maxDataWidth=width;              
            
        }
        T.setBody(Set);
 
        
        if(Set.size()>0 && panel>=0){
            tc.setFirstLine(panel,Set.firstElement());
            tc.setFirstLineEnergy(panel, Str.parseFloatEnergy(T.getDataLineEnergies().firstElement())); 
            tc.setFirstLineLevelEnergy(panel, Str.parseFloatEnergy(T.getDataLineLevels().firstElement())); 
            
            tc.setLastLine(panel,Set.lastElement());
            tc.setLastLineEnergy(panel, Str.parseFloatEnergy(T.getDataLineEnergies().lastElement()));  
            tc.setLastLineLevelEnergy(panel, Str.parseFloatEnergy(T.getDataLineLevels().lastElement()));   
            
            if(Set.size()>1)
            	tc.setSecondLastLine(panel, Set.elementAt(Set.size()-2));
            
            tc.setLeftoverOfLastCom(panel,leftoverOfLastCom);
        }

        try{
        	//check if the first line is the same as the last line just set here
        	float nextLineE=tc.getFirstLineEnergy(panel+1);
        	float nextLevelE=tc.getFirstLineLevelEnergy(panel+1);        	
        	if(nextLevelE==tc.getLastLineLevelEnergy(panel)){
        		tc.SetIsLastLevelSameAsNextFirst(panel, true);
        		if(nextLineE==tc.getLastLineEnergy(panel)) {
            		tc.setFirstLine(panel+1, null);
            		tc.setFirstLineEnergy(panel+1, nextLineE+Math.min(0.1f,0.001f*nextLineE));
            		tc.setFirstLineLevelEnergy(panel+1, nextLevelE+Math.min(0.1f,0.001f*nextLevelE));
        		}
        	}
        		
        }catch(Exception e){}
        
        
        
        T.setNTableBodyLines(this.nTableBodyLines);
        T.setTableBodyHeight(this.tableBodyHeight);
        T.setTotalDataLines(lineCount);
        
        //maxDataWidth=maxDataWidth*Config.CHAR2CM;//this is inaccurate since there could be different empty columns in different lines
        HashMap<String,String> maxColStrings=findMaxColumnStrings(footnotes,tc.getPanelColumns(panel),Delays,firstDelay,lastDelay);
        HashMap<String,String> headStrings=colHeadStrings(footnotes, tc,dpType,EI,ens,labcom);
        
        maxDataWidth=findMaxDataWidth(maxColStrings,headStrings);
        
        maxCommentWidth=this.maxCommentWidth;//this.maxCommentWidth is determined in makeSingleDelayed()
        
        dataHeadWidth=findDataHeadWidth(maxColStrings,headStrings);
        
        /*
        maxDataWidth=findMaxEntryWidth(tc.getPanelColumns(panel),Delays,firstDelay,lastDelay);

        
        width=colHeadWidth(gencom, tc,dpType,EI,ens,labcom);
        if(maxCommentWidth>0)
        	width=width-"Comments".length()*Config.CHAR2CM;

        if(width>maxDataWidth)
        	maxDataWidth=width;
        */
        
        if(dataHeadWidth>maxDataWidth)
            maxRowWidth=dataHeadWidth+maxCommentWidth;
        else
            maxRowWidth=maxDataWidth+maxCommentWidth;
        
        T.setTotalWidth(maxRowWidth);
        T.setDataWidth(maxDataWidth);
        T.setHeadWidth(maxRowWidth);
        
        T.setDataHeadWidth(dataHeadWidth);
        
        //determine the orientation of just created table based on all columns widths
        //T.setPortrait(tc.isPortrait(T));
        
        return T;

    }
	
	//for manual mode
	//see comments in LevelTableMaker
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TablePanel makeTablePanel_manual(int panel)throws Exception{
		
		ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        Vector<String> Set=new Vector<String>();
        currentPanelNo=panel;
        String row="";
        String particle="";
  
                
        boolean showSuppressed=etd.isShowSuppressed();
       
        int nColumns;
        Vector<String> columns;
        
        if(panel<0){
        	System.out.println("In manual mode for GammaTableMaker, panelNo<0 is not allowed! Please check the code!");
        	System.exit(0);
        }
        
        columns=tc.getPanelColumns(panel);
  
        //debug
        //System.out.println("In DelayedTableMaker: manual mode: panel="+panel+" columns="+columns+" tc.columns="+tc.getDefaultColumns());
        
        nColumns=columns.size();
        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getDefaultColumns().contains("COMM"))
        	T.addComLoc(tc.getDefaultColumns().indexOf("COMM"));
        
        float comWidth=tc.getCommentColumnWidth();
        if(comWidth==-1.0f) comWidth=nColumns*1.7f;

        if(ens.particleType().equalsIgnoreCase("P")){dpType="p";particle="Proton";}
        if(ens.particleType().equalsIgnoreCase("N")){dpType="n";particle="Neutron";}
        if(ens.particleType().equalsIgnoreCase("A")){dpType="$\\alpha$";particle="Alpha";}
        T.setSpare(particle);
        
        Vector <DParticle> Delays=new Vector();
        for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nDParticles();j++)
                Delays.addElement(ens.levelAt(i).DParticleAt(j));
        }
        if(etd.isSortDelays())
            for(int i=0;i<ens.nUnpDParticles();i++){
                Delays.addElement(ens.unpDParticleAt(i));
            }

  
        /*the vectors which will contain the comments,
         *one goes at the top, one goes to footnotes at bottom
         */

        Vector<Comment> topcom=new Vector<Comment>();
        Vector<Comment> labcom=new Vector<Comment>();


        /*
         * add code for processing NR and NT comments for N record (Jun Chen, 9/30/2015)
         * NOTE: ens.comments contain all comments including comments for N, P and Q records
         *  
         */
        Comment com;
        
        Footnotes footnotes=new Footnotes(ens);
        footnotes.findFootnotes(tc);//wrap of footnote comments and flag information
        
        //set link ID for each footnote: etd.getLinkID()+"GAMMA"+index
        footnotes.setLinkIDs(etd.getLinkID()+"DELAY");
        
        for(int i=0;i<tc.getComments().size();i++){
            com=(Comment)tc.getComments().elementAt(i);
            if(tc.isTopCommentOnly(com)){
            	if(com.nHeads()>0 && !com.type().equals("N "))//head in "N" comment is not printed
            		topcom.addElement(tc.extractTopComment(com));
            	else
            		topcom.addElement(com);
            }
            else if(tc.isAlsoTopComment(com))
            	topcom.addElement(tc.extractTopComment(com));               
            
        }
        
        for(int i=0; i<topcom.size();i++){
            Comment c = new Comment();
            c=(Comment)topcom.elementAt(i);
            
            row="\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+c.getRegTranslated();
            Set.addElement(row); 
            row="";
        }
        T.setGenComments(Set);     
       
        Vector<String> v;
        Set=new Vector<String>();
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            if(t.type()=='D') {
            	v=makeTableRecords(t,tc.isFormatTables());
            	Set.addAll(v);
            }
        }
        T.setTableRecords(Set);

                
        Set=new Vector<String>();
        Set.addElement(makeTableHead(footnotes, tc,dpType,EI,ens,labcom));
        T.setFirstHead(Set);
        
        Set=new Vector<String>();

        row+="\\multicolumn{"+String.valueOf(nColumns)+"}{c}{\\underline{Delayed "+particle+"s (continued)}}";
        Set.addElement(row); row="";
        row="\\multicolumn{"+nColumns+"}{c}{~}";
        Set.addElement(row); row="";
        Set.addElement(makeTableHead(footnotes, tc,dpType,EI,ens,labcom));
        T.setHead(Set);
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement("\\multicolumn{"+String.valueOf(nColumns)+"}{c}{Continued on next page (footnotes at end of table)}");
        T.setTail(Set);
        
        Set=new Vector<String>();

        //print out the footnoted comments
        row="";
        String head="";
        for(int i=0;i<footnotes.size();i++){
            Comment c = new Comment();
            c=(Comment)footnotes.commentAt(i);
       
            head=footnotes.footnoteMarkAt(i)+" ";
        	    	            
        	Set.addElement(head+c.getRegTranslated());
        }

        T.setLastTail(Set);
        
        
        DParticle[] TEMP=new DParticle[Delays.size()];

        for(int i=0;i<Delays.size();i++)
        	TEMP[i]=Delays.elementAt(i);
        Arrays.sort(TEMP,new DPComparator());
        Delays=new Vector<DParticle>();

        for(int i=0;i<TEMP.length;i++)Delays.addElement(TEMP[i]);

        //if etd.isSortDelays=true, unplaced delays is already added into delays.
        if(!etd.isSortDelays())
        	for(int i=0;i<ens.nUnpDParticles();i++){
        		Delays.insertElementAt(ens.unpDParticleAt(i),i);
        }
        
        Set=new Vector<String>();
        
        float min=-100000;
        float max=100000;

    	if(currentPanelNo>0&&currentPanelNo<=tc.getBreakPoints().size())
    		min=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo-1).toString());
        if(currentPanelNo<tc.getBreakPoints().size())
    	    max=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo).toString());
 
        
        boolean pastMax=false,pastMin=false;
        
        for(int i=0; i<Delays.size();i++){
            DParticle dp=new DParticle();
            dp=Delays.elementAt(i);
            
          //determine whether this dp should be drawn in this panel
            
            float dpE=dp.ERPF();
            
            if(dpE>=min) {
            	pastMin=true;
            }
            if(dpE>=max) {
            	pastMax=true;
            }
            
            if(pastMax || !pastMin) 
            	continue;
                        
            if(!(dp.getLEVI()>=0)||dp.getLEVS()==null)
            	dp.setLEVS("Unplaced");
            
            Vector out=new Vector();
            
            if(tc.getFcolumns()==null||checkFColumns(tc,dp))
            	out=makeSingleDelayed_manual(currentPanelNo,dp, etd,T,footnotes, showSuppressed);//page breaking in the middle of comment lines is handled in this call
                                                                                                                            //this.nTableBodyLines and this.TableBody height are incremented in this call                       
            if(out==null) 
            	continue;
            else{ 
            	Set.addAll(out);               	
            }                  
        }
        
        T.setBody(Set);
        
        //determine the orientation of just created table based on all columns widths
        //T.setPortrait(tc.isPortrait(T));
        
        return T;

    }
	
    /**Returns the info for a single Delayed Particle (auto mode)*/
    @SuppressWarnings("unused")
	private Vector<String> makeSingleDelayed_auto(int panel,DParticle dp, EnsdfTableData etd,TablePanel T,Footnotes footnotes, boolean showSuppressed,boolean isLastDelayInPrevPanel,
    		float nextEF,float min,float max)throws Exception{
    //Creating gamma and final level objects
        float width=0;
    	Vector<String> out=new Vector<String>();
        String r="";
        String levelEnergy,dataEnergy;
        float levE=-1;
        
        ENSDF ens=etd.getENSDF();          
        TableControl tc=etd.getDelayTableControl();

        Vector<String> coms=new Vector<String>();
        
        if(panel<0) panel=0;
        
        if(isLastDelayInPrevPanel)
        	coms=procComsFormattedPartial(panel,dp,tc,showSuppressed,tc.getLastLine(panel-1),tc.getSecondLastLine(panel-1));
        else
        	coms=procComs(panel,dp,tc,showSuppressed);

       	
        levelEnergy=Float.toString(-1);
        dataEnergy=Float.toString(-1);
        
        if(dp.getLEVI()>=0 && dp.getLEVS()!=null){

            levE=ens.levelAt(dp.getLEVI()).EF();
            //levelEnergy=dp.getLS();
            levelEnergy=String.valueOf(levE);
        }
        
        if(dp.ERPS()!=null)
        	dataEnergy=dp.ERPS(); 
        
        //if(dp.ES()!=null)
        //	dataEnergy=dp.ES();       
        //if(dataEnergy.trim().length()==0)
        //	dataEnergy=dp.ERPS();//fake energy for empty dp energy record
        
        Vector<String> columns=tc.getPanelColumns(currentPanelNo);
        
        r=makeDelayEntry(dp, columns, ens, coms, footnotes);
       
        if(!isLastDelayInPrevPanel){
        	out.addElement(r);       
            this.nTableBodyLines++;
            
            float scale0=1.0f,scale1=1.0f,scale3=1.0f;
            if(Str.containSpecialSupScript(r)){
            	scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*1.10f;
            	scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;      
            	scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;  
            }else if(Str.containSupScript(r, "0,1,2,3,4,5,6,7,8,9")){
                scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*0.2f;
                scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;      
                scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;    
            }else if(Str.containSupScript(r)){
                if(Str.containJPIScriptOnly(r)){
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                }else if(Str.containGivenSupScriptOnly(r,"x")){
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                }else{
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1;
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                }
            }else if(Str.containSubScript(r)) {
                scale0=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                scale1=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                scale3=NDSConfig.TABLE_LINEWIDTH_SCALE4;
            }
            
            
            if(coms.size()==0)
            	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH0*scale0;
            else if(coms.size()==1)
            	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH1*scale1;
            else 
            	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale3;
                       
            T.getDataLineEnergies().add(dataEnergy);                
            T.getDataLineLevels().add(levelEnergy);
            
        }   
        
           
        
        if(tc.getPanelColumns(currentPanelNo).contains("COMM")){
        	String row,tempS;
        	float e1=0,e2,de,ef;
        	int start=0;
        	String es="";
        	
        	start=1;
        	if(isLastDelayInPrevPanel){
        		e1=min;
        		start=0;
        	}
        	
        	if(dp.ERPF()>e1)
        		e1=dp.ERPF();
        	
        	e2=nextEF;
        	
        	de=e2-e1;
        	if(coms.size()>0)
        	    de=(e2-e1)/coms.size();
        	     	
        	int index=-1,size=-1;     
        	float firstLineEF=-1;
        	if(coms.size()>0){
                int[] a=findIndexAndSize(max,e1,e2,coms.size(),10);
                index=a[0];
                size=a[1];
                
                int newindex=0;
                if(de>0 && max>e1 && max<e2)
                	newindex=(int)((max-e1)/de); 
                
                if(index<newindex)
                	index=newindex;
        	}
        	        	
            for(int k=start;k<coms.size();k++){
                row="";
                
               	tempS=coms.elementAt(k);    
               	
                for(int j=0;j<tc.getPanelColumns(panel).indexOf("COMM");j++) 
                	row+="&";
                row+=tempS;
                for(int j=tc.getPanelColumns(panel).indexOf("COMM");j<tc.getPanelColumns(panel).size()-1;j++) 
                	row+="&";
                
                ef=e1+de*k;
                       
                firstLineEF=ef;
                
                //System.out.println(row+" k="+k+" e1="+e1+" e2="+e2+" ef="+ef+" min="+min+" max="+max+" nextEF="+nextEF+" coms.size()="+coms.size());
                
                if(index>0){//break in the middle of comments of a delay, only print part before the break for the panel before this break                      
                	if(k>=index || k==coms.size()-1){//if previous remakeTablePanel split the comment to more lines, it is possible index (previous)>coms.size-1 (current).
                        //For this case, k will reach coms.size-1

                		if(k==coms.size()-1 && k>0 && max<e2){//if break at last comment line, print that line in current panel and move the break point to next gamma
                			firstLineEF=e2;
                			T.setIsBreakAtLastCom(true);
                		}
                    	
                		if(panel>=0){
                           	tc.setFirstLineEnergy(panel+1, firstLineEF);  
                       		tc.setFirstLineLevelEnergy(panel+1, levE);
                        	
                        	this.pastBreak=true;
                        	
                            leftoverOfLastCom="";
                            for(int m=k;m<coms.size();m++) {
                                String line=coms.get(m);
                                if(!isContinuedCommentLine(line))   
                                    break;                               
                                    
                                leftoverOfLastCom+=" "+cleanFormattedCommentLine(line);

                            }
                		}
 
                    	
                    	if(k<coms.size()-1)
                    		break;
                    }	
                }

                
                if(ef<min) continue; //break in the middle of comments of a gamma, only print part after the break for the panel after this break
                
                out.addElement(row);
                
                float scale=1.0f;
                if(Str.containSpecialSupScript(row))
                	scale=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET;              
                else if(Str.containSupScript(row, "0,1,2,3,4,5,6,7,8,9")){   
                    scale=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;    
                }else if(Str.containSupScript(row)){
                    if(Str.containJPIScriptOnly(row))
                        scale=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                    else if(Str.containGivenSupScriptOnly(row,"x"))
                        scale=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                    else
                        scale=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                }else if(Str.containSubScript(row)) {
                    scale=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                }
                
                this.nTableBodyLines++;
                this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale;//the line spacing between comment lines are slight larger due to the use of parbox than when no comment present
                
                //debug
                //System.out.println("@@In DelayTableMaker: nTableBodyLines="+this.nTableBodyLines+" at dp.EF()="+dp.EF()+" "+row);
                //System.out.println("                    tableBodyHeight="+this.tableBodyHeight);
                
                es=String.valueOf(ef);
                
                //artificial data energy (gamma) assigned for comment lines that are not in the same line with level and data record              
                T.getDataLineEnergies().add(es);
                T.getDataLineLevels().add(levelEnergy);
            }

        }
        
        width=findMaxCommentWidth(panel,dp,tc,showSuppressed);
        if(width>this.maxCommentWidth)
        	this.maxCommentWidth=width;
        
        return out;
    }
    
    
    /**Returns the info for a single Delayed Particle (manual mode)*/
    private Vector<String> makeSingleDelayed_manual(int panel,DParticle dp, EnsdfTableData etd,TablePanel T,Footnotes footnotes, boolean showSuppressed)throws Exception{
    //Creating gamma and final level objects
    	Vector<String> out=new Vector<String>();
        String r="";
       
        ENSDF ens=etd.getENSDF();          
        TableControl tc=etd.getDelayTableControl();

        Vector<String> coms=new Vector<String>();
        
        coms=procComs(panel,dp,tc,showSuppressed);
        
        Vector<String> columns=tc.getPanelColumns(currentPanelNo);
        
        r=makeDelayEntry(dp, columns, ens, coms, footnotes);      
        	
        out.addElement(r);       
     
                
        if(columns.contains("COMM")){
        	String row,tempS;
        	     	
            for(int k=1;k<coms.size();k++){
                row="";
                
               	tempS=coms.elementAt(k);                  
                for(int j=0;j<tc.getPanelColumns(panel).indexOf("COMM");j++) 
                	row+="&";
                row+=tempS;
                for(int j=tc.getPanelColumns(panel).indexOf("COMM");j<tc.getPanelColumns(panel).size()-1;j++) 
                	row+="&";
                
                out.addElement(row);
            }

        }       
        
        return out;
    }
    
    
    private String makeDelayEntry(DParticle dp, Vector<String> columns,ENSDF ens, Vector<String> coms, Footnotes footnotes) throws Exception{
    	String r="";
    	String colName;
    	String flag=dp.flag();
    	boolean hasFlag=false;
    	boolean print=false,alignExpo=false;
    	String temp="";
    	
    	String flagsLatex="";
    	
    	int nColumns=columns.size();
  
    	
        for(int c=0;c<nColumns;c++){
        	colName=columns.elementAt(c);
        	String S="";
        	
        	print=false;
        	alignExpo=false;
        	
            flagsLatex="";
            
        	 if((colName.equals("LEV")||colName.equals("DLEV")) && dp.getLEVI()>=0){ 
        		 temp=dp.getLEVS();
        		 if(ens.levelAt(dp.getLEVI()).q().equals("?"))
        			 temp+="?";
        		 
        		 String DES=ens.levelAt(dp.getLEVI()).DES();
                 S=LatexWriter.printNumber(temp,DES);
                 if(S.contains("times10^") && (DES.isEmpty()||!tc.showDE()) )
                	 alignExpo=true;
            	
                 if(!tc.showDE()&&S.contains("{\\it"))
                 	S=S.substring(0,S.indexOf("{\\it")).trim();   
                 
                 if(ens.levelAt(dp.getLEVI()).q().equals("S"))//"S" marked the level energy is to be put in bracket
                 	S="("+S+")";
                 
                 print=true;
        	 }
        	 else if((colName.equals("E")||colName.equals("DE")) && dp.ES().length()>0){
                 S=LatexWriter.printNumber(dp.ES(),dp.DES());
                 S=addFlagsLatex(S, flag, footnotes,"E");
                 flagsLatex=addFlagsLatex("",flag, footnotes,"E");
                 
                 if(dp.q().equals("S"))//"S" marked the energy is to be put in bracket
                 	S="("+S+")";
                 
                 print=true;
        	 }
        	 else if(colName.equals("IP")||colName.equals("DIP")){
             	S=LatexWriter.printNumber(dp.RIS(),dp.DRIS());
                S=addFlagsLatex(S,flag,footnotes,"IP");
                flagsLatex=addFlagsLatex("",flag, footnotes,"IP");
                hasFlag=hasPrintFlag(flag,footnotes, "IP");
                if(dp.RIS().length()>0 || hasFlag)
                	print=true;
        	 }
             else if(colName.equals("T")||colName.equals("DT")){
             	S=LatexWriter.printNumber(dp.TS(),dp.DTS());
                S=addFlagsLatex(S,flag,footnotes,"T");     
                flagsLatex=addFlagsLatex("",flag,footnotes,"T");
                hasFlag=hasPrintFlag(flag,footnotes, "T");
                if(dp.TS().length()>0 || hasFlag)           
                	print=true;
             }else if(colName.equals("EI")){
                 S=LatexWriter.printNumber(dp.EIS(),"");
                 S=addFlagsLatex(S, flag, footnotes,"EI");
                 flagsLatex=addFlagsLatex("",flag, footnotes,"EI");
                 hasFlag=hasPrintFlag(flag,footnotes,"EI");
                 if(dp.EIS().length()>0 || hasFlag)           
                 	print=true;
  
        	 }
       	        	     	 
        	 
        	 //print string
             temp="";
             int n=-1;
             if(columns.contains("D"+colName)){
                 temp="\\multicolumn{1}{r@{}}{";
                 if(S.contains(".") && (!S.contains("times10^")||S.contains("-"))){//exclude case like 1.53X10^3 12, exception 1.54X10^-3
                     temp+=S.substring(0,S.indexOf("."));
                 }else if(alignExpo || (!S.contains(".") && S.contains("times10^") && S.contains("-")) ){//e.g, 1072X10^-6 16, aliged by X
                     temp+=S.substring(0,S.indexOf("\\ensuremath{\\times10^"));
                 }else if(!S.contains(".") && (n=S.indexOf("+"))>0 && n<S.length()-1 && Character.isLetter(S.charAt(n+1))){//e.g, 1234+X
                     temp+=S.substring(0,S.indexOf("+"));
                 }else if(flagsLatex.length()>0){
                     temp+=S.substring(0,S.indexOf(flagsLatex));
                 }else if(S.contains(" ")){
                     temp+=S.substring(0,S.indexOf(" "));
                 }else{
                     temp+=S;
                 }
                 temp+="}";
             }else if(colName.charAt(0)=='D' && columns.contains(colName.substring(1))){
                 temp="\\multicolumn{1}{@{";
                 if(S.contains(".") && (!S.contains("times10^")||S.contains("-"))){
                 	String tempS=S.substring(S.indexOf(".")+1);
                 	if(tempS.length()>0 && tempS.charAt(0)==' ')//for val+unc pair like 3076. 15,val=3076. unc=15
                 		tempS="\\ "+tempS.substring(1);
                 	
                     temp+=".}l}{"+tempS;
                     
                 }else if(alignExpo || (!S.contains(".") && S.contains("times10^") && S.contains("-")) ){
                     temp+="}l}{"+S.substring(S.indexOf("\\ensuremath{\\times10^"));
                 }else if(!S.contains(".") && (n=S.indexOf("+"))>0 && n<S.length()-1 && Character.isLetter(S.charAt(n+1))){//e.g, 1234+X
                     temp+="}l}{"+S.substring(S.indexOf("+"));
                 }else if(flagsLatex.length()>0){
                     temp+="}l}{"+S.substring(S.indexOf(flagsLatex));
                 }else if(S.contains(" ")){
                     temp+=" }l}{"+S.substring(S.indexOf(" ")+1);
                 }else{
                     temp+="}l}{";
                 }
                 temp+="}";
             }else if(colName.equals("EI")){
                //S=LatexWriter.printNumber(dp.EIS(),"");
                //temp="\\multicolumn{1}{l}{"+S+addFlagsLatex("",flag,footnotes,"EI")+"}";        
                temp="\\multicolumn{1}{l}{"+S+"}"; 

        	 }else if(colName.equals("L")){
        		
        		 hasFlag=hasPrintFlag(flag,footnotes,"L");
        		 temp="\\multicolumn{1}{l}{"+dp.LS()+addFlagsLatex("",flag,footnotes,"L")+"}";   
                 if(dp.LS().length()>0 || hasFlag) 
                	print=true;
             }else if(colName.equals("COMM") && coms.size()>0){             	
        		 r+=coms.elementAt(0);
        	 }
             //else
             //	r+=("\\parbox[c]{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright \\vspace{0.1cm}}");//to keep the same line-space for data-entry lines as that between comment lines 
        	 
        	 if(print)
        		 r+=temp;                      
            
        	 if(c<nColumns-1)
        		 r+=("&");
  
        }        

        return r;
    }
    
/**Returns a String corresponding with the appropriate heading for the Delayed Particle Table*/
	private String makeTableHead(Footnotes footnotes, TableControl tc, String type, ensdfparser.ensdf.Nucleus EI, ENSDF ens, Vector<Comment> labcom){
        String h="",colName="";
        boolean wroteLabel=false;
        Vector<String> columns=tc.getPanelColumns(currentPanelNo);
        
        for(int c=0;c<columns.size();c++){
        	colName=(String) columns.elementAt(c);
        	
            if(colName.equals("LEV")){
            	h+="\\multicolumn{2}{c}{";
                h+=("E($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+")}");
                T.addMulticol(c);
            }
            else if(colName.equals("EI")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("EI")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("E($^{"+EI.A()+"}$"+EI.En()+")");
                wroteLabel=false;
                h+=footnotes.getFlagSymbolsLatex("", "EI");//(flag,type)              
            }
            else if(colName.equals("L")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("L")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("L");
                wroteLabel=false;
                h+=footnotes.getFlagSymbolsLatex("", "L");//(flag,type)              
            }
            else if(colName.equals("E")){
            	h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("E")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("E("+type+")");
                wroteLabel=false;
                h+=footnotes.getFlagSymbolsLatex("", "E");//(flag,type)
                
                h+="}";
                T.addMulticol(c);
            }
            else if(colName.equals("IP")){
            	h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("IP")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("I("+type+")");
                wroteLabel=false;
                h+=footnotes.getFlagSymbolsLatex("", "IP");//(flag,type)
                
                h+="}";
                T.addMulticol(c);
            }   
            else if(colName.equals("T")){
            	h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("T")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("$\\Gamma$");
                wroteLabel=false;
                h+=footnotes.getFlagSymbolsLatex("", "T");//(flag,type)
                
                h+="}";
                T.addMulticol(c);
            } 
            else if(colName.equals("COMM"))h+=("Comments");
            
            if(((c<columns.size()-1)&&(colName.indexOf("D")!=0))&&!((c==columns.size()-2)&&(columns.elementAt(c+1).toString().indexOf("D")==0)))
            	h+=("&");
        }
        return h;
    }
	
	@SuppressWarnings("unused")
	private float colHeadWidth(Footnotes footnotes, TableControl tc, String type, ensdfparser.ensdf.Nucleus EI, ENSDF ens, Vector<Comment> labcom){
    	float headWidth=0;
    	HashMap<String,String> colStrings=colHeadStrings(footnotes,tc,type,EI,ens,labcom);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		headWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return headWidth;
	}
	

	private HashMap<String,String> colHeadStrings(Footnotes footnotes, TableControl tc, String dpType, ensdfparser.ensdf.Nucleus EI, ENSDF ens, Vector<Comment> labcom){
        String h="";
        boolean wroteLabel=false;
        HashMap<String,String> colStrings=new HashMap<String,String>();
        String colName="";
        String type=dpType;
        if(type.contains("alpha") || type.contains("$"))
        	type="A";
        
        for(int c=0;c<tc.getPanelColumns(currentPanelNo).size();c++){
        	h="";
        	colName=(String)tc.getPanelColumns(currentPanelNo).elementAt(c);
        	
            if(colName.equals("LEV"))
            	//h=("E(level)("+ens.nucleus().A()+ens.nucleus().En()+")");       
                h=("E("+ens.nucleus().A()+ens.nucleus().En()+")"); 
            else if(colName.equals("EI")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("EI")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                //if(!wroteLabel)h+=("E(level)("+EI.A()+EI.En()+")");
                if(!wroteLabel)h+=("E("+EI.A()+EI.En()+")");
                
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "EI");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }
            else if(colName.equals("E")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("E")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("E("+type+")");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "E");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }
            else if(colName.equals("IP")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("IP")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("I("+type+")");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "IP");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }else if(colName.equals("T")) {
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("T")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("Width("+type+")");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "T");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }
            else if(colName.equals("COMM"))h+=("Comments");
           
            colStrings.put(colName, h);
        }
        return colStrings;
    }
	
/**Checks the Fcolumns, returns true if the entry should be printed*/
    private boolean checkFColumns(TableControl tc, DParticle dp){
        boolean print=false;
        Vector<?> f=tc.getDataAsComments();
        if(f.contains("E")&&!dp.ES().isEmpty())print=true;
        if(f.contains("IP")&&!dp.RIS().isEmpty())print=true;
        if(f.contains("EI")&&!dp.EIS().isEmpty())print=true;
        if(f.contains("LEV")&&!dp.LS().isEmpty())print=true;
        return print;
    }
    
    public String commentHeadMaker(Comment com,String option) throws java.lang.Exception{
    	String head="",tempHead="";
    	@SuppressWarnings("unused")
		int n=0;;
    	String headName;
    	
    	if(com.nHeads()==0)
    		return "";
    	
        String type=dpType;
        if(type.contains("alpha") || type.contains("$"))
        	type="A";
        

        HashMap<String,String> labelMap=tc.getRelabelMap();
        HashMap<String,String> translatedLabelMap=tc.getTranslatedLabelMap();
               
        String label="",translatedLabel="";
        
    	for(int k=0;k<com.nHeads();k++){
        	headName=com.headAt(k);
            
        	if(head.length()>0){
        		head+=",";
        		tempHead+=",";
        		n+=1;
        	}
        	       	
        	label=labelMap.get(headName);
        	translatedLabel=translatedLabelMap.get(headName);
           	if(label==null || label.trim().isEmpty()) {
                if(headName.equals("IP") || headName.equals("I"+type)) {
                	head+="I("+this.dpType+")";
                	tempHead+="I("+type+")";
                	n+=3+type.length();	
                }
                else if(headName.equals("E")) {
                	head+="E("+this.dpType+")";
                	tempHead+="E("+type+")";
                	n+=3+type.length();	
                }
                else if(headName.equals("EI")) {
                	
                	head+="E($^{"+this.EI.A()+"}$"+this.EI.En()+")";
                	tempHead+="E("+this.EI.A()+this.EI.En()+")"; 
                	n+=5+this.EI.En().length();        	
                }
                else if(headName.equals("T")) {
                	
                	head+="$\\Gamma$";
                	tempHead+="T"; 
                	n+=1;       	
                }
                else {
                	String translatedHead=Translator.process(headName);
                	head+=translatedHead;  
                	
                	if(translatedHead.contains("ensuremath")){
                		tempHead+=headName;
                		n+=headName.length();
                	}else{
                		tempHead+=translatedHead;
                		n+=translatedHead.length();
                	}   
                }
           	}else {
        		head+=translatedLabel.trim();
        		tempHead+=label.trim();
        		n+=label.trim().length();
           	}

    	}
    	
        if(head.length()==0)
        	return "";
        
        head+=": ";
        tempHead+=": ";
        n+=2;
        
        //System.out.println(tempHead);
        if(option.equals("temp"))
        	return tempHead;
        
        return head;
    	
    }
    
    /**creates the comments
     * @throws java.lang.Exception */
    protected String commentBuilder(Comment c) throws java.lang.Exception{
        String s="";
        if(c.nHeads()>0){
            if(c.headAt(0).equals("IP")) s="I("+dpType+")";
            else if(c.headAt(0).equals("E")) s="E";
            else if(c.headAt(0).equals("EI")) s="E($^{"+EI.A()+"}$"+EI.En()+")";
            else s=Translator.process(c.headAt(0));
            s+=": ";
        }
        if(c.nHeads()>0)s+=c.getHTranslated();
        else s+=c.getTranslated();
        return s;
    }
    

    
    protected Vector<Comment> convertDataAsComments(int panel,Record d,TableControl tc)throws Exception{
    	DParticle dp=(DParticle)d;
    	Vector<Comment> out=new Vector<Comment>();
    	  
    	String type="";
        String temp="";
        
        if(tc.getDataAsComments()!=null){
            for(int j=0;j<tc.getDataAsComments().size();j++){
                type=tc.getDataAsComments().elementAt(j);
                if(type.equals("IP")&&dp.RIS().length()>0) temp="I("+dpType+"): "+LatexWriter.printNumberAsIs(dp.RIS(),dp.DRIS());
                if(type.equals("E")&&dp.ES().length()>0) temp="E: "+LatexWriter.printNumberAsIs(dp.ES(),dp.DES());
                if(type.equals("EI")&&dp.EIS().length()>0) temp="E("+EI.A()+EI.En()+"): "+dp.EIS();
                
                if(temp.trim().length()>0){
                	Comment c=new Comment();
                	c.setBody(temp,true);
                    out.addElement(c);
                }
            }
        }
        
        return out;
    }    
    
    /*return a rough number of the width of all data columns in output table**/
    private float getEntryLength(DParticle dp,Vector<String> columns, ENSDF ens)throws Exception{
        String colName;
        String row="";

        int nColumns=columns.size();
        for(int j=0;j<nColumns;j++){
            colName=(String) columns.elementAt(j);
            if(colName.equals("E"))        row+=dp.ES()+" "+dp.DES()+" ";
            else if(colName.equals("LEV")) {
            	if(dp.getLEVI()>=0)
            		row+=dp.getLEVS()+" "+ens.levelAt(dp.getLEVI()).DES()+" ";
            	else
            		row+=dp.getLEVS()+" ";//unplaced
            }
            else if(colName.equals("EI"))  row+=dp.EIS()+" ";                                 
            else if(colName.equals("IP"))  row+=dp.RIS()+" "+dp.DRIS()+" ";
        }
        
        return row.length();
    }
    
    
    /*return a rough number of the maximum width of all data entries in output table panel**/
    @SuppressWarnings("unused")
	private float findMaxEntryWidth(Footnotes footnotes,Vector<String> columns,Vector<DParticle> Delays,DParticle firstDelay,DParticle lastDelay)throws Exception{
    	float maxEntryWidth=0;
    	HashMap<String,String> colStrings=findMaxColumnStrings(footnotes,columns,Delays,firstDelay,lastDelay);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		maxEntryWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return maxEntryWidth;
    }
    
    private HashMap<String,String> findMaxColumnStrings(Footnotes footnotes,Vector<String> columns,Vector<DParticle> Delays,DParticle firstDelay,DParticle lastDelay)throws Exception{
        
        HashMap<String,String> colStrings=new HashMap<String,String>();
        
        if(firstDelay==null)
            return colStrings;
        
        String colName;
        String temp="";
        String maxColString="";
        String flagStr="";
        String fakeFlagStr="";
        
        StringMax stringMax;
        
        float maxColWidth=0;
        float colWidth=0;
        
        ENSDF ens=etd.getENSDF();
       	 
        int start=Delays.indexOf(firstDelay);
        int end  =Delays.indexOf(lastDelay);

        
        //if(start<0) start=0;
        //if(end<0) end=Delays.size()-1;
        
        int nColumns=columns.size();
        for(int j=0;j<nColumns;j++){    	
            colName=(String) columns.elementAt(j);
            maxColString="";
            maxColWidth=0;
            
            stringMax=new StringMax();
            for(int k=start;k<=end;k++){          	
            	DParticle dp=Delays.elementAt(k); 
                temp="";
                if(dp.flag().trim().length()>0)
                	flagStr=dp.flag();
                else
                	flagStr="*";//fake flag. Note that footnotes.nMarks("",type) returns number of flags for heading of given "type"
                
                fakeFlagStr="";
                
                if(colName.equals("E")) 
                	temp=Str.getString(dp.ES(),dp.DES(),""); 
                else if(colName.equals("LEV")) {
                	temp=dp.getLEVS();//dp.getLEVI()<0 for unplaced
                    
                	if(dp.getLEVI()>=0 && tc.showDE())
                		temp=Str.getString(temp,ens.levelAt(dp.getLEVI()).DES(),"");
                	else if(temp.toUpperCase().equals("UNPLACED"))
                		temp="";
                }
                else if(colName.equals("EI"))  temp=dp.EIS();                                 
                else if(colName.equals("IP"))  temp=Str.getString(dp.RIS(),dp.DRIS(),""); 
                else continue;
                
                fakeFlagStr=Str.repeat(NDSConfig.fakeFlagLabel, footnotes.nMarks(flagStr, "EI"));
                		
                if(temp.contains("E") || temp.contains("e"))//eg, 1.2E3 translated as 1.2x10^3
                {
                	temp+="*";
                	if(!temp.toUpperCase().contains("E+") && !temp.toUpperCase().contains("E-") )
                		temp+="*";
                	else
                		temp+="*";
                }

                stringMax.compare(temp, fakeFlagStr);
                
                temp=stringMax.str;
                colWidth=stringMax.width;
                
                if(temp.trim().length()>0 && colWidth>maxColWidth){
                	maxColString=temp;    
                	maxColWidth=colWidth;
                }           

            }

            colStrings.put(colName, maxColString);

        }
        
        return colStrings;
    }
    
    private float findMaxCommentWidth(int panel,DParticle g,TableControl tc,boolean showSuppressed)throws Exception{

    	String com="";
        float width=0;
        float maxCommentWidth=0;
        float commentColumnWidth=tc.getCommentColumnWidth(panel); 
        int nlines=0;
        String dpType=this.dpType;
        if(dpType.contains("alpha"))
        	dpType="A";
        
        for(int j=0;j<g.nContRecords();j++){
        	ContRecord cr=g.contRecordAt(j);
            if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) ){
                com=cr.name()+"="+cr.s()+" "+cr.ds()+" "+cr.ref(true);
                
                width=com.length()*NDSConfig.CHAR2CM;
                if(width>maxCommentWidth) maxCommentWidth=width;
                if(width>0)
               	 nlines+=width/commentColumnWidth+1;
            }
        }
        
        for(int n=0;n<g.nComments();n++){
        	Comment c=g.commentAt(n);
            com="";
            if(c.nHeads()>0){
                if(c.headAt(0).equals("IP")) com="I("+dpType+")";
                else if(c.headAt(0).equals("E")) com="E";
                else if(c.headAt(0).equals("EI")) com="E("+EI.A()+EI.En()+")";
                else com=Translator.process(c.headAt(0));
                com+=": ";
            }
            com+=c.body();
           
            width=com.length()*NDSConfig.CHAR2CM;
            if(width>maxCommentWidth) maxCommentWidth=width;
            if(width>0)
           	 nlines+=width/commentColumnWidth+1;
        }
        
        if(tc.getDataAsComments()!=null){
            for(int j=0;j<tc.getDataAsComments().size();j++){
                String type=tc.getDataAsComments().elementAt(j);
                if(type.equals("IP")&&g.RIS().length()>0) com="I("+dpType+"): "+g.RIS()+" "+g.DRIS();
                if(type.equals("E")&&g.ES().length()>0) com="E: "+g.ES()+" "+g.DES();
                if(type.equals("EI")&&g.EIS().length()>0) com="E("+EI.A()+EI.En()+"): "+g.EIS();
                
                width=com.length()*NDSConfig.CHAR2CM;
                if(width>maxCommentWidth) maxCommentWidth=width;   
                if(width>0)
               	 nlines+=width/commentColumnWidth+1;
            }
        }
        
        this.nCommentLines=nlines;
        return maxCommentWidth;
    }
}

class DPComparator implements Comparator<Object>{
    public int compare(Object one,Object two){
        DParticle a=(DParticle)one;
        DParticle b=(DParticle)two;
        int out;
        float aE=a.ERPF();
        float bE=b.ERPF();
        
        //if(a.ES().trim().length()==0)//dp with empty energy record, using fake energy set in Level.java
        //	aE=a.ERPF();
        //if(b.ES().trim().length()==0)//dp with empty energy record, using fake energy set in Level.java
        //	bE=b.ERPF();
        
        if(aE<bE)
        	out=-1;
        else if(aE>bE)
        	out=1;
        else {
        	float aEL=EnsdfUtil.s2f(a.getLEVS());
        	float bEL=EnsdfUtil.s2f(a.getLEVS());
        	if(aEL<bEL)
        		out=-1;
        	else if(aEL>bEL)
        		out=1;
        	else
        		out=0;
        }
        return out;
    }
}