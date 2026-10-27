/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ensdfparser.nds.latex;

import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.ensdf.Record;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.*;
import ensdfparser.nds.util.Str;
/**
 *
 * @author Jeremie Choquette
 * updated by Jun Chen in 2015
 */
public class GammaTableMaker extends TableMaker{

    private int currentPanelNo;
    private TablePanel T;
    
    Vector <Gamma>Gammas=new Vector<Gamma>();
    
    //private EnsdfTableData etd;
    //@SuppressWarnings("unused")
	//private MassChain data;
    private TableControl ctc;
    private boolean pastBreak=false;
    private String leftoverOfLastCom="";
    
    public GammaTableMaker(){
        
    }
    
    public GammaTableMaker(EnsdfTableData etd,MassChain data){
        this.etd=etd;
        this.data=data;
        this.tableType="GAMMA";
        this.tc=etd.getGammaTableControl();
        
        initGammas();

        
        init();
    }
    
    private void initGammas(){
        ENSDF ens=etd.getENSDF();       
    	for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nGammas();j++)
              Gammas.addElement(ens.levelAt(i).gammaAt(j));                
      	}
      	
        if(etd.isSortGammas()){
          	for(int i=0;i<ens.nUnpGammas();i++)          
          		Gammas.addElement(ens.unpGammaAt(i));
          	
            Gamma[] TEMP=new Gamma[Gammas.size()];
            for(int i=0;i<Gammas.size();i++)TEMP[i]=Gammas.elementAt(i);
            Arrays.sort(TEMP,new GComparator());
            Gammas=new Vector<Gamma>();
            for(int i=0;i<TEMP.length;i++){            	
            	Gammas.addElement(TEMP[i]);
            }
            
            int n=Gammas.size();
            for(int i=1;i<n;i++){
            	float ef=-1,prevEF=-1;
            	int count=0;
            	int j=i;
            	do{
            		ef=Gammas.get(j).EF();
            		prevEF=Gammas.get(j-1).EF();
            		j++;
            	}while(ef==prevEF&&j<n);
            	
            	count=j-i-1;
            	if(count>0){
            		float e0=Gammas.get(i-1).EF();
            		float gap=(ef-e0);
            		if(gap>1.0)
            			gap=1.0f;
            		gap=gap/(count+1);
            		for(int k=i;k<i+count;k++){
            			Gamma g=Gammas.get(k);
            			g.setERF(e0+gap*(k-i+1), g.DEF());
            		}           			
            	}
            }
            
        }else if(etd.getUnplcTableControl().isDrawn()){
          	for(int i=0;i<ens.nUnpGammas();i++)
          		Gammas.insertElementAt(ens.unpGammaAt(i),i);
          }
    }
    
    /**Implement the abstract method in parent class*/
    public TablePanel makeTablePanel(int panel)throws Exception{
    	return makeTablePanel(panel,false);
    }

    /** write level table */
    public TablePanel makeTablePanel(int panel,boolean U)throws Exception{
    	if(autoAdjust)
    		return makeTablePanel_auto(panel,U);
    	else
    		//return makeTablePanel_manual(panel,U);
    	    return makeTablePanel_auto(panel,U);
    }
    
    
    /** Writes gamma tables
     * 
     * @param ens
     * @param etd
     * @param panel
     * @param doUnplaced is this the unplaced gamma table?
     * @param allGammas should we print all gammas, or just the placed ones?
     * @return the Table Panel object for this dataset
     * @throws Exception
     */
    /**Writes the gamma table (auto mode)*/
	public TablePanel makeTablePanel_auto(int panel,boolean U)throws Exception{

        ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        Vector<String> Set=new Vector<String>();
        String row="";
        currentPanelNo=panel;
      
        
        float maxRowWidth=0;
        float maxCommentWidth=0;
        float maxDataWidth=0;
        float dataHeadWidth=0;//including empty column if existing
        float width=0;
        int lineCount=0;
        int count=0;
        float linePos=0;
               
        this.maxCommentWidth=0;
        
        if(U){
            tc=etd.getUnplcTableControl();
            ctc=etd.getUnplcTableControl();
            tc.setEnsdfFieldNames(etd.getGammaTableControl().getEnsdfFieldNames());
            ctc.setEnsdfFieldNames(etd.getGammaTableControl().getEnsdfFieldNames());    
            
            //reset Gammas
            Gammas.clear();
        	for(int i=0;i<ens.nUnpGammas();i++)          
        		Gammas.addElement(ens.unpGammaAt(i));
        	
        }else{
            tc=etd.getGammaTableControl();
            ctc=etd.getGammaTableControl();
        }
        boolean showSuppressed=etd.isShowSuppressed();

 
        int nColumns;
        Vector<String> columns;

    	nColumns=tc.getPanelColumns(currentPanelNo).size();
    	columns=tc.getPanelColumns(currentPanelNo);

        
    	//System.out.println("GammaTableMaker 173: nColumns="+nColumns);
    	//for(String s:columns)
    	//	System.out.println("   "+s);
    	
        //debug
        //if(tc.getPanelColumns(currentPanelNo).contains("MR"))
        //	System.out.println(tc.getPanelColumns(currentPanelNo));

        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getPanelColumns(currentPanelNo).contains("COMM"))T.addComLoc(tc.getPanelColumns(currentPanelNo).indexOf("COMM"));
                     
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
        footnotes.findFootnotes(ctc);//wrap of footnote comments and flag information
        
        //set link ID for each footnote: etd.getLinkID()+"GAMMA"+index
        footnotes.setLinkIDs(etd.getLinkID()+"GAMMA");
        
        for(int i=0;i<ctc.getComments().size();i++){
            com=(Comment)ctc.getComments().elementAt(i);
            
        	//debug
        	//System.out.println("In GammaTableMaker: com="+com.head()+" com.body="+com.body()+"com.type()="+com.type()+" "+ctc.isTopCommentOnly(com));
            //System.out.println("In GammaTableMaker 160: com="+com.head()+" extract.body="+ctc.extractTopComment(com).body()+" com.type()="+com.type()+" "+ctc.isTopCommentOnly(com));
            
            if(com.body().contains("LABEL=")){labcom.addElement(com);continue;}
            
            if(ctc.isTopCommentOnly(com)){
            	if(com.nHeads()>0 && com.type().charAt(0)!='N')//head in "N" comment is not printed, type=line.substring(7,9), like "L ", "G "
            		topcom.addElement(ctc.extractTopComment(com));
            	else
            		topcom.addElement(com);
            }
            else if(ctc.isAlsoTopComment(com))
            	topcom.addElement(ctc.extractTopComment(com));
           

        }
        
        if(U&&(topcom.size()>0||!footnotes.isEmpty())){
        	Set.addElement("Comments and Footnotes shared for placed and unplaced gamma tables.");
        	lineCount++;
        	linePos+=NDSConfig.HEAD_COMMENT_LINEWIDTH;
        }
        
        count=0;
        for(int i=0; i<topcom.size();i++){
            Comment c = new Comment();
            c=(Comment)topcom.elementAt(i);
        	tempComs=wrapENSDFCommentTranslated(c,HEADNOTE_WIDTH,"","",c.isBigC()); 
        	
        	//debug
        	//System.out.println("In GammaTableMaker: com="+c.head()+" com.body="+c.body()+"com.type()="+c.type()+" tempComs(0)="+tempComs.elementAt(0));
        	
        	
        	//add indent after first line of a comment
        	if(tempComs.size()>0){
        		tempComs.insertElementAt("\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+tempComs.elementAt(0),0);
        		tempComs.removeElementAt(1);
        	}
        	
        	Set.addAll(tempComs);
        	count+=tempComs.size();
        }
        
        if(U||!etd.getUnplcTableControl().isDrawn()){
        	T.setGenComments(Set);
        	lineCount+=count;
        	linePos+=count*NDSConfig.HEAD_COMMENT_LINEWIDTH;
        	
            //one blank line before and after general comment
            if(Set.size()>0){
                lineCount+=2;
                linePos+=2*NDSConfig.HEAD_COMMENT_LINEWIDTH;;
            }
        }

        count=0;
        Vector<String> v;
        Set=new Vector<String>();
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            if(t.type()=='G') {
            	v=makeTableRecords(t,tc.isFormatTables());//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
            	Set.addAll(v);
            	count=this.nTableRecordsLines;
            }
        }
        
        if(U||!etd.getUnplcTableControl().isDrawn()){
        	T.setTableRecords(Set);
        	T.setNTableRecordsLines(this.nTableRecordsLines);
        	T.setTableRecordsHeight(this.tableRecordsHeight);
        	lineCount+=count;
        }

        
        if(panel<=0){
            
        	T.setRelHeadLineNo(lineCount+2);//lineCount+(one line space)+(one line headline column names)
        	
        	//linePos+=2*Config.DEFAULT_LINEWIDTH;//if counting lines separately, not using this.tableRecordHeight, slightly different
        	
        	//each table leaves one-line space before (=0.7cm) and after it (=0.4cm). See LatexWriter
        	linePos+=tableRecordsHeight+NDSConfig.TABLE_TOP_MARGIN+NDSConfig.HEAD_COMMENT_LINEWIDTH+0.2;//if using this.tableRecordsHeight, more precise.
        	
        	T.setRelHeadLinePos(linePos);
        }
        else{
        	T.setRelHeadLineNo(NDSConfig.DEFAULT_HEADLINE_NO);
        	T.setRelHeadLinePos(NDSConfig.DEFAULT_HEADLINE_POS);
        }
        
        Set=new Vector<String>();
        String tableHead=makeTableHead(footnotes, tc, labcom);
        
        //System.out.println("GammaTableMaker 305: table head="+tableHead);
        
        Set.addElement(tableHead);//NOTE: global currentPanelNo is used in this call
        T.setFirstHead(Set);

        //System.out.println("GammaTableMaker 306: panel no="+panel+" table head="+tableHead);
        Set=new Vector<String>();

        row+="\\multicolumn{"+String.valueOf(nColumns)+"}{c}{\\underline{$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}}";
        Set.addElement(row); row="";
        row="\\multicolumn{"+nColumns+"}{c}{~~~}";
        Set.addElement(row); row="";
        Set.addElement(tableHead);
        T.setHead(Set);
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~~~}");
        Set.addElement("\\multicolumn{"+String.valueOf(nColumns)+"}{c}{Continued on next page (footnotes at end of table)}");
        T.setTail(Set);
        
        
        Set=new Vector<String>();

        //print out the footnoted comments
        row="";
        String tempHead="",head="";
        
        for(int i=0;i<footnotes.size();i++){
            Comment c = new Comment();
            c=(Comment)footnotes.commentAt(i);
            
        	//System.out.println("     c.flag="+c.flag()+"   ens.gammaFlags="+ens.gammaFlags()+" com="+c.body()+" c.head="+c.head());
        	//System.out.println("           hashead="+c.hasHead(defaultColumns));       	           
            
            tempHead="h ";//arbitrary, each symbol is one character     
            head=footnotes.footnoteMarkAt(i)+" ";
        	
        	tempComs=wrapENSDFCommentTranslated(c,FOOTNOTE_WIDTH,tempHead,head,c.isBigC(),2);//leave 2-whitespace indent after first line   
        	       	
        	Set.addAll(tempComs);
        }

        if(!U)
        	T.setLastTail(Set);

        Set=new Vector<String>();


        float min=-1000000;
        float max=1000000;
        
        float mingam=-100000;//needed when gamma is not sorted
        float maxgam=100000;
        
        if(panel<0){//for printing the whole table in a single panel with no beak and splitting
        	//panel=0;       
        	//currentPanelNo=0;
        }
        else{ 
        	if(currentPanelNo>0&&currentPanelNo<=tc.getBreakPoints().size()){
        		min=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo-1).toString());
        		mingam=tc.getGammaBreakPoint(currentPanelNo-1);
        	}
            if(currentPanelNo<tc.getBreakPoints().size()){
        	    max=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo).toString());
        	    maxgam=tc.getGammaBreakPoint(currentPanelNo);
            }
            
        }
        
        if(etd.isSortGammas()){
        	mingam=min;
        	maxgam=max;
        }
        
             
        
        //debug
        //System.out.println("In GammaTableMaker line 372: paneNo="+currentPanelNo+" min="+min+" max="+max+" mingam="+mingam+" maxgam="+maxgam);
        //if(tc.getBreakPoints().size()>0)
        //System.out.println("                    break size="+tc.getBreakPoints().size()+" breakPoint(0)="+tc.getBreakPoints().elementAt(0).toString()+" breakGamma(0)="+tc.getGammaBreakPoint(0));
        
        
        lineCount=0;
        this.nTableBodyLines=0;
        this.tableBodyHeight=0;
        boolean isLastGammaInPrevPanel=false;
        boolean isLastGammaInCurrPanel=false;
        float nextEF=0;
        
        boolean doIL=true;
        boolean hasprinted=false;
        boolean pastMax=false,pastMin=false;
        
        this.pastBreak=false;
        
        Gamma firstGamma=null,lastGamma=null;

        
        for(int i=0; i<Gammas.size();i++){
            Gamma g=new Gamma();
            g=(Gamma)Gammas.elementAt(i);
            Gamma ng;//next gamma

            if(i<Gammas.size()-1){
            	ng=Gammas.elementAt(i+1);
            	if(etd.isSortGammas() || ng.ILI()==g.ILI())
            		nextEF=ng.ERF();
            	else
            		nextEF=g.ERF()+100;          	
            }
            else{
            	ng=null;
            	nextEF=g.ERF()+100;
            }
         
            //System.out.println("In GammaTableMaker: line 411: g.EF="+g.EF()+" g.ERF="+g.ERF());

            //check if we should draw this gamma in this panel
            if(etd.isSortGammas()){//in order of gamma energy, break points set for gamma energy
            	
                //determine whether this gamma should be drawn in this panel
                if(g.ERF()>=min) {
                	pastMin=true;
                	if(firstGamma==null)
                		firstGamma=g;
                }
                
                isLastGammaInCurrPanel=false;
                if(g.ERF()>=max && panel>=0) {
                	if(!this.pastBreak) {
                		tc.setFirstLineEnergy(panel+1, g.ERF()); 
                        if(g.ILI()>=0) 
                        	tc.setFirstLineLevelEnergy(panel+1, ens.levelAt(g.ILI()).EF());
                        else
                        	tc.setFirstLineLevelEnergy(panel+1, -1);
                	}
                	this.pastBreak=true;
                	pastMax=true;
                	
                	if(lastGamma==null && i>0)
                		lastGamma=Gammas.elementAt(i-1);
                }
                else if(nextEF>=max){
                	isLastGammaInCurrPanel=true;
                	
                	if(lastGamma==null)
                		lastGamma=g;
                }
                else if(i==Gammas.size()-1)
                	if(lastGamma==null)
                		lastGamma=Gammas.lastElement();
                
                isLastGammaInPrevPanel=false;
                             
                if(g.ERF()<min && nextEF>min){//break in the middle of comments of last level in previous panel, so print the remaining comments in the new panel, only when autoAdjust=true
                	pastMin=true;
                	isLastGammaInPrevPanel=true;
                }               	
                
                if(pastMax || !pastMin) 
                	continue;
            }
            else{                 //in order of level energy, break points set for level energy
                float levE;
                
                if(g.ILI()>=0) 
                	levE=ens.levelAt(g.ILI()).EF();
                else 
                	levE=-1;
                
                if(levE>min) {
                	pastMin=true;
            		if(firstGamma==null)//for panelNo=0
            			firstGamma=g;
                }
                
                if(levE>max) 
                	pastMax=true;
                else if(levE<max && i==Gammas.size()-1)
                   	if(lastGamma==null)
                		lastGamma=Gammas.lastElement();
                              
                //determine whether this gamma should be drawn in this panel
                //g.EF() and nextEF(for next g) are from the same level
                isLastGammaInPrevPanel=false;
                if(levE==min&&currentPanelNo>0){
                	if(g.ERF()>=mingam) {
                		pastMin=true;
                		if(firstGamma==null)
                			firstGamma=g;
                	}
                	else if(nextEF>mingam){//break in the middle of comments of last gamma in previous panel, so print the remaining comments in the new panel, only when autoAdjust=true
                		pastMin=true;
                		isLastGammaInPrevPanel=true; 		
                	}
                }
                

            	isLastGammaInCurrPanel=false;
                if(levE==max && currentPanelNo>=0 && tc.getGammaBreakPoint(currentPanelNo)!=-1){
                	if(g.ERF()>=maxgam) {
                    	if(!this.pastBreak) {
                           	tc.setFirstLineEnergy(currentPanelNo+1, g.ERF()); 
                            tc.setFirstLineLevelEnergy(currentPanelNo+1, levE);            
                            
                    	}
                    	this.pastBreak=true;
                    	pastMax=true;
                    	
                    	if(lastGamma==null && i>0)
                    		lastGamma=Gammas.elementAt(i-1);
                	}
                	else if(nextEF>=maxgam){
                		isLastGammaInCurrPanel=true;
                    	if(lastGamma==null)
                    		lastGamma=g;
                	}
                	
                	/*
                	//debug
                	if(currentPanelNo==9){ 
                		System.out.println("In GammaTableMaker line 517: i="+i+" next panel first eL="+levE+" eg="+g.ERF()+" maxLevE="+max);
                		System.out.println("   maxgam="+maxgam+" tc.getGammaBreakPoint(currentPanelNo)="+tc.getGammaBreakPoint(currentPanelNo)+" nextEF="+nextEF);
                	    System.out.println("   pastMax="+pastMax+"  pastMin="+pastMin+" pastBreak="+pastBreak+" isLastGammaInPrevPanel="+isLastGammaInPrevPanel);
                	    System.out.println("  firstLine="+tc.getFirstLine(currentPanelNo));
                	    System.out.println("   lastLine="+tc.getLastLine(currentPanelNo));
                   	    System.out.println("  lastLineEnergy="+tc.getFirstLineEnergy(currentPanelNo));
                	    System.out.println("   lastLineLevelEnergy="+tc.getFirstLineLevelEnergy(currentPanelNo));
                	    System.out.println(" next firstLine="+tc.getFirstLine(currentPanelNo+1));
                	    System.out.println("  next firstLineEnergy="+tc.getFirstLineEnergy(currentPanelNo+1));
                	    System.out.println("   next firstLineLevelEnergy="+tc.getFirstLineLevelEnergy(currentPanelNo+1));
               
                	} 
                	*/           	
                }           	
                
                if(pastMax||!pastMin) 
                	continue;
                
            }
            

            if(!(g.ILI()>=0))
            	g.setILS("Unplaced");
            
            //if(g.ILI()<0)
            //System.out.println("GammaTableMaker 1352: EG="+g.ES()+" level size="+ens.levelsV().size()+" g.ILI="+g.ILI()+" "+g.isPlaced());
            
            if((i>0&&!tc.showAll()&&hasprinted&&!etd.isSortGammas()&&g.ILI()>0 && g.ILI()==Gammas.elementAt(i-1).ILI())||!g.isPlaced())
            	doIL=false;
            else 
            	doIL=true;
            
            Vector<String> out=new Vector<String>();
            this.nCommentLines=0;//incremented in findMaxCommentWidth() in makeSingleGamma


            //debug
            //System.out.println("In GammaTableMaker line 501:  index="+g.ILI()+"  g="+g.ES());
            
            //page breaking in the middle of comment lines is handled in this call
            //this.nTableBodyLines and this.TableBody height are incremented in this call
            if(tc.getFcolumns()==null||checkFColumns(tc,g)){
                //debug
                //System.out.println("In GammaTableMaker line 507: l="+ens.levelAt(g.ILI()).ES()+" index="+g.ILI()+" JPI="+ens.levelAt(g.ILI()).JPiS()+" g="+g.ES());
            	
            	out=makeSingleGamma_auto(panel,g,etd,T,footnotes, doIL, showSuppressed,isLastGammaInPrevPanel,isLastGammaInCurrPanel,nextEF,mingam,maxgam);
            }


            //debug
           	//System.out.println("In GammaTableMaker: line 532: mingam="+mingam+" maxgam="+maxgam+" g.e="+g.ES()+" nextEF="+nextEF);
            //System.out.println("       isLastGammaInCurrPanel="+isLastGammaInCurrPanel+"  isLastGammaInPrevPanel="+isLastGammaInPrevPanel);
        	//System.out.println("  panel="+panel+"     IsBreakAtLastCom="+T.isBreakAtLastCom());
        	
            for(int j=0;j<out.size();j++){
            	Set.addElement(out.elementAt(j));
            	
            	//debug
            	//System.out.println("####In GammaTableMaker: "+i+" "+lineCount+" gamma="+g.EF()+"  com="+out.elementAt(j));
            	
            	lineCount+=1;
            }
            
            if(!isLastGammaInPrevPanel) hasprinted=true;
                         	
            width=getEntryLength(g,tc.getPanelColumns(panel),ens);

            
            if(width>maxDataWidth) 
            	maxDataWidth=width; 
            
        }

        //debug
        //System.out.println("******* GammaTableMaker line 587 panel="+panel+"     IsBreakAtLastCom="+T.isBreakAtLastCom());
        
        T.setBody(Set);
       
       
        
        if(Set.size()>0 && panel>=0){
            tc.setFirstLine(panel,Set.elementAt(0));
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
        		
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        
        T.setNTableBodyLines(this.nTableBodyLines);
        T.setTableBodyHeight(this.tableBodyHeight);
        T.setTotalDataLines(lineCount);
        
        //debug
        //System.out.println("In GammaTableMaker 596: panel="+panel+" lastLine="+Set.lastElement());
        
        //Set=new Vector<String>();

        //maxDataWidth=maxDataWidth*Config.CHAR2CM;//this is inaccurate since there could be different empty columns in different lines

        HashMap<String,String> maxColStrings=findMaxColumnStrings(footnotes,tc.getPanelColumns(panel),Gammas,firstGamma,lastGamma);
        HashMap<String,String> headStrings=colHeadStrings(footnotes, tc, labcom);
        
        maxDataWidth=findMaxDataWidth(maxColStrings,headStrings);

        maxCommentWidth=this.maxCommentWidth;//this.maxCommentWidth is determined in makeSingleGamma()
        
        dataHeadWidth=findDataHeadWidth(maxColStrings,headStrings);
        
        /*
        if(panel==0) {
            System.out.println("In GammaTableMaker line 644: panel="+panel+" datawidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" headwidth="+width+
                "  tc.commentColumnWdith="+tc.getCommentColumnWidth(panel));
  
            for(String key:maxColStrings.keySet())
                System.out.println("    col="+key+"  name="+maxColStrings.get(key));
            
            for(String key:headStrings.keySet())
                System.out.println("    head="+key+"  name="+headStrings.get(key));
            
            for(String s:tc.getDefaultFixedColumns())
                System.out.println("   fixedColumn="+s);
            
            System.out.println("   firstGamma="+firstGamma.ES());
            System.out.println("   lastGamma="+lastGamma.ES()); 
        }
        */
        
        /*
        maxDataWidth=findMaxEntryWidth(tc.getPanelColumns(panel),Gammas,firstGamma,lastGamma);
        maxCommentWidth=this.maxCommentWidth;//this.maxCommentWidth is determined in makeSingleGamma()
        width=colHeadWidth(gencom, tc, labcom);
        
        //debug
        System.out.println("In GammaTableMaker: maxDataWidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" head width="+width);
        
        if(maxCommentWidth>0)
        	width=width-"Comments".length()*Config.CHAR2CM;

        if(width>maxDataWidth)
        	maxDataWidth=width;
                   
        */

        if(dataHeadWidth>maxDataWidth)
            maxRowWidth=dataHeadWidth+maxCommentWidth;
        else
            maxRowWidth=maxDataWidth+maxCommentWidth;

        
        //System.out.println("##In GammaTableMaker line 628: panelNo="+panel+" maxDataWidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" head width="+maxRowWidth);
        //.out.println("  firstLine="+T.getBody().firstElement());
        //System.out.println("   lastLine="+T.getBody().lastElement());
        //System.out.println(" tc.firstLine="+tc.getFirstLine(currentPanelNo));
        //System.out.println(" tc.lastLine="+tc.getLastLine(currentPanelNo));
        //System.out.println(" tc.firstLineEnergy="+tc.getFirstLineEnergy(currentPanelNo));
        //System.out.println(" tc.firstLineLevelEnergy="+tc.getFirstLineLevelEnergy(currentPanelNo));
        //System.out.println(" tc.lastLineEnergy="+tc.getLastLineEnergy(currentPanelNo));
        //System.out.println(" tc.lastLineLevelEnergy="+tc.getLastLineLevelEnergy(currentPanelNo));
        //System.out.println(" tc.next firstLineEnergy="+tc.getFirstLineEnergy(currentPanelNo+1));
        //System.out.println(" tc.next firstLineLevelEnergy="+tc.getFirstLineLevelEnergy(currentPanelNo+1));
        
        //for(String s:tc.getPanelColumns(panel)) System.out.println("   panel columns="+s);

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
	public TablePanel makeTablePanel_manual(int panel,boolean U)throws Exception{

        ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        Vector<String> Set=new Vector<String>();
        String row="";
        currentPanelNo=panel;
                    
        this.maxCommentWidth=0;
        
        if(U){
            tc=etd.getUnplcTableControl();
            ctc=etd.getUnplcTableControl();
            tc.setEnsdfFieldNames(etd.getGammaTableControl().getEnsdfFieldNames());
            ctc.setEnsdfFieldNames(etd.getGammaTableControl().getEnsdfFieldNames());    
        }else{
            tc=etd.getGammaTableControl();
            ctc=etd.getGammaTableControl();
        }
        boolean showSuppressed=etd.isShowSuppressed();

 
        int nColumns;
        Vector<String> columns;
        
        if(panel<0){
        	System.out.println("In manual mode for GammaTableMaker, panelNo<0 is not allowed! Please check the code!");
        	System.exit(0);
        }

    	nColumns=tc.getPanelColumns(currentPanelNo).size();
    	columns=tc.getPanelColumns(currentPanelNo);
        
        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getPanelColumns(currentPanelNo).contains("COMM"))T.addComLoc(tc.getPanelColumns(currentPanelNo).indexOf("COMM"));
    

        Vector <Gamma>Gammas=new Vector<Gamma>();
        if(!U)for(int i=0;i<ens.nLevels();i++){
            for(int j=0;j<ens.levelAt(i).nGammas();j++){
                Gammas.addElement(ens.levelAt(i).gammaAt(j));
            }
        }
        if((etd.isSortGammas()&&!U)||U)
        	for(int i=0;i<ens.nUnpGammas();i++){            
        		Gammas.addElement(ens.unpGammaAt(i));
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
        
        Footnotes footnotes=new Footnotes();
        footnotes.findFootnotes(ctc);//wrap of footnote comments and flag information
        
        //set link ID for each footnote: etd.getLinkID()+"GAMMA"+index
        footnotes.setLinkIDs(etd.getLinkID()+"GAMMA");
        
        for(int i=0;i<ctc.getComments().size();i++){
            com=(Comment)ctc.getComments().elementAt(i);
            if(ctc.isTopCommentOnly(com)){
            	if(com.nHeads()>0 && !com.type().equals("N "))//head in "N" comment is not printed
            		topcom.addElement(ctc.extractTopComment(com));
            	else
            		topcom.addElement(com);
            }
            else if(ctc.isAlsoTopComment(com))
            	topcom.addElement(ctc.extractTopComment(com));
        }
        
        if(U&&(topcom.size()>0||!footnotes.isEmpty())){
        	Set.addElement("Comments and Footnotes shared for placed and unplaced gamma tables.");
        }
        
        for(int i=0; i<topcom.size();i++){
            Comment c = new Comment();
            c=(Comment)topcom.elementAt(i);
            row+="\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+c.getRegTranslated();
            Set.addElement(row); 
            row="";
        }
        
        if(U||!etd.getUnplcTableControl().isDrawn()){
        	T.setGenComments(Set);
        }
        
        Vector<String> v;
        Set=new Vector<String>();
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            if(t.type()=='G') {
            	v=makeTableRecords(t,tc.isFormatTables());//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
            	Set.addAll(v);
            }
        }
        
        if(U||!etd.getUnplcTableControl().isDrawn()){
        	T.setTableRecords(Set);
        }
        
        Set=new Vector<String>();
        Set.addElement(makeTableHead(footnotes, tc, labcom));//NOTE: global currentPanelNo is used in this call
        T.setFirstHead(Set);
        
        
        Set=new Vector<String>();

        row+="\\multicolumn{"+String.valueOf(nColumns)+"}{c}{\\underline{$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}}";
        Set.addElement(row); row="";
        row="\\multicolumn{"+nColumns+"}{c}{~~~}";
        Set.addElement(row); row="";
        Set.addElement(makeTableHead(footnotes, tc, labcom));
        T.setHead(Set);
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~~~}");
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
        
        if(!U)
        	T.setLastTail(Set);

        Set=new Vector<String>();
        if(!U){
            if(etd.isSortGammas()){
                Gamma[] TEMP=new Gamma[Gammas.size()];
                for(int i=0;i<Gammas.size();i++)TEMP[i]=Gammas.elementAt(i);
                Arrays.sort(TEMP,new GComparator());
                Gammas=new Vector<Gamma>();
                for(int i=0;i<TEMP.length;i++)Gammas.addElement(TEMP[i]);
            }
            if(!etd.isSortGammas()&&!etd.getUnplcTableControl().isDrawn())
            	for(int i=0;i<ens.nUnpGammas();i++){
            		Gammas.insertElementAt(ens.unpGammaAt(i),i);
            }
        }

        float min=-1000000;
        float max=1000000;
        
        float mingam=-100000;//needed when gamma is not sorted
        float maxgam=100000;
        
    	if(currentPanelNo>0&&currentPanelNo<=tc.getBreakPoints().size()){
    		min=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo-1).toString());
    		mingam=tc.getGammaBreakPoint(currentPanelNo-1);
    	}
        if(currentPanelNo<tc.getBreakPoints().size()){
    	    max=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo).toString());
    	    maxgam=tc.getGammaBreakPoint(currentPanelNo);
        }
        
        if(etd.isSortGammas()){
        	mingam=min;
        	maxgam=max;
        }
       
        boolean doIL=true;
        boolean hasprinted=false;
        boolean pastMax=false,pastMin=false;
                
        
        for(int i=0; i<Gammas.size();i++){
            Gamma g=new Gamma();
            g=(Gamma)Gammas.elementAt(i);
                       
            //check if we should draw this gamma in this panel
            if(etd.isSortGammas()){//in order of gamma energy, break points set for gamma energy           	
                //determine whether this gamma should be drawn in this panel
                if(g.ERF()>=min) {
                	pastMin=true;
                }
                if(g.ERF()>=max) {
                	pastMax=true;
                }             	
                
                if(pastMax || !pastMin) 
                	continue;
            }
            else{                 //in order of level energy, break points set for level energy
                float levE;
                
                if(g.ILI()>=0) 
                	levE=ens.levelAt(g.ILI()).EF();
                else 
                	levE=-1;
                
                if(levE>min) {
                	pastMin=true;
                }                
                if(levE>max) 
                	pastMax=true;
                              
                if(levE==min&&currentPanelNo>0){
                	if(g.ERF()>=mingam) {
                		pastMin=true;
                	}
                }
                if(levE==max&&tc.getGammaBreakPoint(currentPanelNo)!=-1){
                	if(g.ERF()>=maxgam) {
                    	pastMax=true;
                	}
                }
                	
                if(pastMax||!pastMin) 
                	continue;
                
            }
            
            if(!(g.ILI()>=0))
            	g.setILS("Unplaced");
            if((i>0&&!tc.showAll()&&hasprinted&&!etd.isSortGammas()&&g.ILS().equals(Gammas.elementAt(i-1).ILS()))||!g.isPlaced())
            	doIL=false;
            else 
            	doIL=true;
            
            Vector<String> out=new Vector<String>();

            if(tc.getFcolumns()==null||checkFColumns(tc,g))
            	out=makeSingleGamma_manual(panel,g,etd,T,footnotes, doIL, showSuppressed);
            
            for(int j=0;j<out.size();j++){
            	Set.addElement(out.elementAt(j));
            }
            
            hasprinted=true;
                              
        }
           
        T.setBody(Set);
                    
        Set=new Vector<String>();
   
        //determine the orientation of just created table based on all columns widths
        //T.setPortrait(tc.isPortrait(T));
        
        return T;

    }
	
    /**returns the data for a single gamma ray (auto mode)*/
    @SuppressWarnings("unused")
	private Vector<String> makeSingleGamma_auto(int panel,Gamma g, EnsdfTableData etd, TablePanel T, Footnotes footnotes, boolean doIL, boolean showSuppressed,boolean isLastGammaInPrevPanel,
    		boolean isLastGammaInCurrPanel,float nextEF,float mingam,float maxgam)throws Exception{
    	
    //Creating gamma and final level objects
    	float width=0;
    	
    	Vector<String> out=new Vector<String>();    	
        String r="";
        String levelEnergy,dataEnergy;
        float levE=-1;

        ENSDF ens=etd.getENSDF();          
        TableControl tc=etd.getGammaTableControl();
              
        Vector<String> coms=new Vector<String>();
        

        
        if(isLastGammaInPrevPanel)
        	coms=procComsFormattedPartial(panel,g,tc,showSuppressed,tc.getLastLine(panel-1),tc.getSecondLastLine(panel-1));
        else
        	coms=procComs(panel,g,tc,showSuppressed);


        //debug
        //System.out.println("In GammaTableMaker line 943: panel="+panel+" isLastGammaInPrevPanel="+isLastGammaInPrevPanel+" coms.size="+coms.size());
        //System.out.println("In GammaTableMaker line 943: panel="+panel+" lastLine of lastPanel="+tc.getLastLine(panel-1));
        
        levelEnergy=Float.toString(-1);
        dataEnergy=Float.toString(-1);
        
        if(g.ILI()>=0 && g.ILS()!=null){
            levE=ens.levelAt(g.ILI()).EF();//float values, see ENSDF.java. For level like 1234.2+x, value=(pre+next)/2
            //levelEnergy=g.ILS();
            levelEnergy=String.valueOf(levE);
        }
        
               
        if(g.ES()!=null){
        	dataEnergy=Float.toString(g.ERF());
        			
        	//dataEnergy=g.ES();
        	//try{
        	//	Float.parseFloat(dataEnergy);
        	//}
        	//catch(NumberFormatException e){
        	//	dataEnergy=Float.toString(g.ERF());//for EG="X", g.EF()=-1, for this case set dataEnergy=g.ERF(), see line 500 in Level.java
        	//}
        }

        Vector<String> columns=tc.getPanelColumns(currentPanelNo);
        
        //debug
        //System.out.println("In GammaTableMaker line 918: l="+ens.levelAt(g.ILI()).ES()+" index="+g.ILI()+" JPI="+ens.levelAt(g.ILI()).JPiS()+" g="+g.ES());
        //System.out.println("    panel="+panel+" dataEnergy="+dataEnergy);
        
        r=makeGammaEntry(g,columns,ens,coms,doIL,footnotes);
        
        //debug
        //System.out.println("In GammaTableMaker line 923: panel="+panel+" l="+ens.levelAt(g.ILI()).ES()+" index="+g.ILI()+" JPI="+ens.levelAt(g.ILI()).JPiS()+" g="+g.ES()+" line="+r);
        
        if(!isLastGammaInPrevPanel){
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
            //debug
            //float EL=EnsdfUtil.s2f(g.ILS());
            //float EL=g.EF();
            //if(EL>1025 && EL<1107) 
            //System.out.println("########### In GammaTableMaker 1080: scale0="+scale0+" scale1="+scale1+" scale3="+scale3
            //		+" row="+r+"\n "
            //		+ "Str.containSupScript="+Str.containSupScript(r)+" contain special="+Str.containSpecialSupScript(r)
            //		+" contain JPI only="+Str.containJPIScriptOnly(r)+" Str.containSubScript(r)="+Str.containSubScript(r)+" Str.containGivenSupScriptOnly(r,\"x\")="+Str.containGivenSupScriptOnly(r,"x"));
            
            if(coms.size()==0)
            	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH0*scale0;
            else if(coms.size()==1)
            	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH1*scale1;
            else 
            	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale3;
            
            //debug
            //System.out.println("In GammaTableMaker line 948: level="+levelEnergy+"  gamma="+dataEnergy);
            
            T.getDataLineEnergies().add(dataEnergy);
            T.getDataLineLevels().add(levelEnergy);
            
        }       
             
        //debug
       	//System.out.println("In GammaTableMaker: line 1015: dataE="+dataEnergy+" levelE="+levelEnergy+" coms.size="+coms.size()+" levE="+levE+" maxgam="+maxgam);
        //System.out.println("       isLastGammaInCurrPanel="+isLastGammaInCurrPanel+"  isLastGammaInPrevPanel="+isLastGammaInPrevPanel+" mingam="+mingam);
        //System.out.println("       com.size="+coms.size()+" g.es="+g.ES());
        //for(nds.control.Break b:tc.getBreaks()) System.out.println(" break: "+b.pos+" gammpos="+b.gammaPos);
        
        if(tc.getPanelColumns(currentPanelNo).contains("COMM")){
        	String row,tempS;
        	float e1=0,e2,de,ef;
        	int start=0;
        	String es="";
        	
        	if(isLastGammaInPrevPanel)
        		e1=mingam;
        	
        	if(g.EF()<=0 && g.ES().length()>0)//for EG="X", g.EF()=-1, for this case set e1=ERF(), see Level.java line 500 for what is g.ERF()
        		e1=g.ERF();
        	else if(g.ERF()>e1)
        		e1=g.ERF();
        	
        	e2=nextEF;
        	
        	de=e2-e1;
        	if(coms.size()>0)
        	    de=(e2-e1)/coms.size();
        	
        	if(isLastGammaInPrevPanel)
        		start=0;
        	else
        		start=1;

        	int index=-1,size=-1;
        	float firstLineEF=-1;
        	
        	if(isLastGammaInCurrPanel && coms.size()>0){
                int[] a=findIndexAndSize(maxgam,e1,e2,coms.size(),10);
                index=a[0];
                size=a[1];
                
                int newindex=0;
                if(de>0 && maxgam>e1 && maxgam<e2)
                	newindex=(int)((maxgam-e1)/de); 
                
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
            	
                //break in the middle of comments of the last gamma in current panel, only print part before the break for the panel before this break
            	//NOTE:in findBreaks in TableMaker.java, after each calling of remakeTablePanel, the comment width is adjusted and thus the number of lines
            	//of each comment could change (mostly decrease since the adjustment tends to increase the comment line width), which results in different
            	//fake data energy assigned to each comment line from what is used previously in determining break points (maxgam from current breakpoints,
            	//but mingam is ok since it is from previous breakpoints and it is already updated with newly assigned energy).
            	//One solution: based on maxgam, find its index since it is among [k*(e2-e1)/coms.size+e1],k=0,1,2,...,coms.size-1. The number of lines 
            	//allowed to be printed in current panel is index+1. Compare it with the index of current comment line.
                //If the last gamma has only one line (break point) left in next panel, print it in current panel

                //debug
               	//System.out.println("In GammaTableMaker: line 1032: start="+start+" index="+index+" coms.size="+coms.size()+" levE="+levE+" ef e1 e2="+ef+" "+e1+" "+e2+" maxgam="+maxgam);
                //System.out.println("       isLastGammaInCurrPanel="+isLastGammaInCurrPanel+" row="+row+" isLastGammaInPrevPanel="+isLastGammaInPrevPanel+" ef,mingam="+ef+" "+mingam);
                //System.out.println("       com.size="+coms.size()+" k="+k+" g.es="+g.ES());
  
            	if(isLastGammaInCurrPanel && index>=0){
                    //debug
                	//System.out.println("@@In GammaTableMaker: line 979: start="+start+" index="+index+" coms.size="+coms.size()+" levE="+levE+" ef e1 e2="+ef+" "+e1+" "+e2+" maxgam="+maxgam+"\n");

                	if(k>=index || k==coms.size()-1){//if previous remakeTablePanel split the comment to more lines, it is possible index (previous)>coms.size-1 (current).
                		                             //For this case, k will reach coms.size-1
                		if(k==coms.size()-1 && k>0 && maxgam<e2){//if break at last comment line, print that line in current panel and move the break point to next gamma
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
                
            	//System.out.println(" line 1070 panel="+panel+"     IsBreakAtLastCom="+T.isBreakAtLastCom()+" leftoverOfLastCom="+leftoverOfLastCom);
                
                if(isLastGammaInPrevPanel && ef<mingam) //break in the middle of comments of the last gamma in previous panel,
                	continue;                           //only print part after the break for the panel after this break
                                                        //gammas could be sorted (in order of gamma energy) or unsorted (in order of level energy).
                                        
                
                //debug
               	//System.out.println("In GammaTableMaker: line 1078: start="+start+" index="+index+" coms.size="+coms.size()+" levE="+levE+" ef e1 e2="+ef+" "+e1+" "+e2+" maxgam="+maxgam);
                //System.out.println("       isLastGammaInCurrPanel="+isLastGammaInCurrPanel+" row="+row+" isLastGammaInPrevPanel="+isLastGammaInPrevPanel+" ef,mingam="+ef+" "+mingam);
               	
                out.addElement(row);
                
                float scale=1.0f;
                if(Str.containSpecialSupScript(row))
                	scale=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET;         
                else if(Str.containSupScript(row, "0,1,2,3,4,5,6,7,8,9")){   
                    scale=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;    
                    //System.out.println("1  row="+row);
                }else if(Str.containSupScript(row)){
                    //System.out.println("2  row="+row);
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
                //System.out.println("@@In GammaTableMaker: nTableBodyLines="+this.nTableBodyLines+" at g.EF()="+g.EF()+" "+row);
                //System.out.println("                    tableBodyHeight="+this.tableBodyHeight);
                
                es=String.valueOf(ef);
                
                //debug
                //System.out.println("In GammaTableMaker line 1080 for comment: level="+levelEnergy+"  gamma="+es);
                
                //fake data energy (gamma) assigned for comment lines that are not in the same line with level and data record              
                T.getDataLineEnergies().add(es);
                T.getDataLineLevels().add(levelEnergy);
            }
            
            
        }       
        

        width=findMaxCommentWidth(panel,g,tc,showSuppressed);
        if(width>this.maxCommentWidth)
        	this.maxCommentWidth=width;
        
        return out;
    }

    /**returns the data for a single gamma ray (manual mode)*/
    private Vector<String> makeSingleGamma_manual(int panel,Gamma g, EnsdfTableData etd, TablePanel T, Footnotes footnotes, boolean doIL, boolean showSuppressed)throws Exception{
    	
    //Creating gamma and final level objects
    	
    	Vector<String> out=new Vector<String>();    	   	
        String r="";
        ENSDF ens=etd.getENSDF();          
        TableControl tc=etd.getGammaTableControl();            
        Vector<String> coms=new Vector<String>();
        
        coms=procComs(panel,g,tc,showSuppressed);

        Vector<String> columns=tc.getPanelColumns(currentPanelNo);
        
        r=makeGammaEntry(g,columns,ens,coms,doIL,footnotes);
        
        out.addElement(r); 
                   
        
        if(tc.getPanelColumns(currentPanelNo).contains("COMM")){
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
    
    /** make a line in level table from gamma field data
     * @throws Exception */
    private String makeGammaEntry(Gamma g,Vector<String> columns,ENSDF ens, Vector<String> coms,boolean doIL,Footnotes footnotes) throws Exception{
    	String r="",temp;
    	String colName;
    	String flag=g.flag();
    	boolean hasFlag=false;
    	boolean print=false;
    	
    	boolean alignExpo=false;
    	String flagsLatex="";
    	
    	int nColumns=columns.size();
    	
        Level fl=new Level();
        if(g.FLI()>=0){
            fl = ens.levelAt(g.FLI());
        }
        
        //if(g.ILI()<0)
        //	System.out.println("GammaTableMaker 1352: EG="+g.ES()+" level size="+ens.levelsV().size()+" g.ILI="+g.ILI()+" doIL="+doIL+" "+g.ILS());
        
        for(int c=0;c<nColumns;c++){
	     	
        	colName=(String) columns.elementAt(c);
        	
        	String S="";
        	flagsLatex="";
        	
        	print=false;
        	alignExpo=false;
        	
        	//get value+uncertainty string
            if((colName.equals("LEV")||colName.equals("DLEV")) && doIL && g.ILS().length()>0){
            	temp=g.ILS();
            	
            	//System.out.println("GammaTableMaker 1352: EG="+g.ES()+" level size="+ens.levelsV().size()+" g.ILI="+g.ILI());
            	
                //debug
                //System.out.println("In GammaTableMaker line 1174: l="+ens.levelAt(g.ILI()).ES()+" index="+g.ILI()+" JPI="+ens.levelAt(g.ILI()).JPiS()+" g="+g.ES());
            	
            	String DES=ens.levelAt(g.ILI()).DES();
                S=LatexWriter.printNumber(temp,DES);
            	if(S.contains("times10^") && (DES.isEmpty()||!tc.showDE()) )
            		alignExpo=true;
            	
            	//System.out.println("GammaTableMaker 1457: EL="+g.ILS()+" EG="+g.ES()+"  "+alignExpo+" s="+S+" DES="+DES+" empty="+DES.isEmpty());
            	
                if(ens.levelAt(g.ILI()).q().equals("?"))
            		S=addQuestionMark(S);
            	
                if(!tc.showDE()&&S.contains("{\\it"))
                	S=S.substring(0,S.indexOf("{\\it")).trim();
                
                if(ens.levelAt(g.ILI()).q().equals("S"))//"S" marked the level energy is to be put in bracket
                	S="("+S+")";
                print=true;
            }
            else if((colName.equals("LEVF")||colName.equals("DLEVF"))){
            	if(g.FLI()>=0 && ens.levelAt(g.FLI()).ES().length()>0){
                	temp=ens.levelAt(g.FLI()).ES();
                	
                	String DES=ens.levelAt(g.FLI()).DES();                	
                    S=LatexWriter.printNumber(temp,DES);
                	if(S.contains("times10^") && (DES.isEmpty()||!tc.showDE()) )
                		alignExpo=true;
                	
                	if(ens.levelAt(g.FLI()).q().length()>0)
                		S=addQuestionMark(S);
                	
                    if(!tc.showDE()&&S.contains("{\\it"))
                    	S=S.substring(0,S.indexOf("{\\it"));
                    
                    print=true;
            	}else if(g.FLRecord().indexOf("?")>=0){
            		S="?";
            		print=true;
            	}

            }
            else if((colName.equals("E")||colName.equals("DE")) && g.ES().length()>0){
                S=LatexWriter.printNumber(g.ES(),g.DES());

                int n=flag.indexOf("^");//"^" for unplaced gamma
                String tempFlag=flag;
                if(n>=0){
                	tempFlag=flag.replace("^", "");
                	S="$^{x}$"+S;//"x" flag is at left
                }

                S=addFlagsLatex(S, tempFlag, footnotes,"E");
                flagsLatex=addFlagsLatex("",tempFlag, footnotes,"E");
                
                if(g.q().equals("S"))//"S" marked the energy is to be put in bracket
                	S="("+S+")";
                
                print=true;
            }
            else if(colName.equals("RI")||colName.equals("DRI")){
            	S=LatexWriter.printNumber(g.RIS(),g.DRIS());
                S=addFlagsLatex(S,flag,footnotes,"RI");
                flagsLatex=addFlagsLatex("",flag, footnotes,"RI");
                print=true;
                hasFlag=hasPrintFlag(flag,footnotes, "RI");
                if(g.RIS().length()>0 || hasFlag)
                	print=true;
            }
            else if(colName.equals("MR")||colName.equals("DMR")){
                S=LatexWriter.printNumber(g.MRS(),g.DMRS());                
                S=addFlagsLatex(S,flag,footnotes,"MR");
                flagsLatex=addFlagsLatex("",flag, footnotes,"MR");
                hasFlag=hasPrintFlag(flag,footnotes, "MR");
                if(g.MRS().length()>0 || hasFlag)
                	print=true;
            }
            else if(colName.equals("CC")||colName.equals("DCC")){
            	S=(LatexWriter.printNumber(g.CCS(),g.DCCS()));
                S=addFlagsLatex(S,flag,footnotes,"CC");
                flagsLatex=addFlagsLatex("",flag, footnotes,"CC");
                hasFlag=hasPrintFlag(flag,footnotes, "CC"); 
                if(g.CCS().length()>0 || hasFlag)
                	print=true;
            }
            else if(colName.equals("TI")||colName.equals("DTI")){
            	S=LatexWriter.printNumber(g.TIS(),g.DTIS());
                S=addFlagsLatex(S,flag,footnotes,"TI");
                flagsLatex=addFlagsLatex("",flag, footnotes,"TI");
                hasFlag=hasPrintFlag(flag,footnotes, "TI");  
                if(g.TIS().length()>0 || hasFlag)
                	print=true;
            }
            
            //if(colName.contains("LEV"))
            //	System.out.println("GammaTableMaker 1457: EL="+g.ILS()+" EG="+g.ES()+"  "+alignExpo);
            
            //print string
            temp="";
            int n=-1;
            if(columns.contains("D"+colName)){
                temp="\\multicolumn{1}{r@{}}{";
                if(S.contains(".") && (!S.contains("times10^")||S.contains("-"))){//exclude case like 1.53X10^3 12, exception 1.53X10^-3
                    temp+=S.substring(0,S.indexOf("."));
                }else if(alignExpo || (!S.contains(".") && S.contains("times10^") && S.contains("-")) ){//e.g, 1072X10^-6 16, aligned by X
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
            }else if(colName.equals("JI")){

                if(doIL&&g.ILI()>=0 && ens.levelAt(g.ILI()).JPiS().length()>0){
                    temp=("\\multicolumn{1}{l}{"+Translator.spin(ens.levelAt(g.ILI()).JPiS())+"}");
                    print=true;
                }
            }else if(colName.equals("JF")){
                if(g.FLI()>=0 && fl.JPiS().length()>0){
                    temp=("\\multicolumn{1}{@{}l}{"+Translator.spin(fl.JPiS())+"}");
                    print=true;
                }
            }else if(colName.equals("M")){            	
            	hasFlag=hasPrintFlag(flag,footnotes,"M");
                temp="\\multicolumn{1}{l}{"+g.MS()+addFlagsLatex("",flag,footnotes,"M")+"}";                               
                if(g.MS().length()>0 || hasFlag) 
                	print=true;
            }else if(colName.equals("COMM") && coms.size()>0){
            	print=true;
            	temp=coms.elementAt(0);
            //else
            //	r+=("\\parbox[c]{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright \\vspace{0.1cm}}");//to keep the same line-space for data-entry lines as that between comment lines 
            }

            if(print)
            	r+=temp;                               
        
            if(c<tc.getPanelColumns(currentPanelNo).size()-1)
            	r+=("&");
        }	
        
        return r;
    }
    
    
    /**returns a string corresponding to the table header*/
    private String makeTableHead(Footnotes footnotes, TableControl tc, Vector<Comment> labcom){
        String h="",colName="";
        boolean wroteLabel=false;
        Vector<String> columns=tc.getPanelColumns(currentPanelNo);
        for(int c=0;c<columns.size();c++){
        	colName=(String) columns.elementAt(c);
        	
            if(colName.equals("LEV")){
                h+="\\multicolumn{2}{c}{E\\ensuremath{_{i}}(level)}";
                T.addMulticol(c);
            }
            else if(colName.equals("JI")){
                h+="J\\ensuremath{^{\\pi}_{i}}";
                //h+="J\\ensuremath{_{i}{\\pi}}";
            }
            else if(colName.equals("LEVF")){
                h+="\\multicolumn{2}{c}{E\\ensuremath{_{f}}}";
                T.addMulticol(c);
            }
            else if(colName.equals("JF")){
                h+="J\\ensuremath{^{\\pi}_{f}}";
            	//h+="J\\ensuremath{_{f}{\\pi}}";
            }
            else if(colName.equals("E")){
                h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("E")){
                    	
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+="E\\ensuremath{_{\\gamma}}";
                wroteLabel=false;
                
                h+=footnotes.getFlagSymbolsLatex("", "E");//(flag,type)
                
                h+="}";
                T.addMulticol(c);
            }
            else if(colName.equals("RI")){
                h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("RI")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);

                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("I\\ensuremath{_{\\gamma}}");
                wroteLabel=false;
                
                h+=footnotes.getFlagSymbolsLatex("", "RI");//(flag,type)
                
               h+="}";
               T.addMulticol(c);
            }
            else if(colName.equals("M")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("M")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("Mult.");
                wroteLabel=false;

                h+=footnotes.getFlagSymbolsLatex("", "M");//(flag,type)
            }
            else if(colName.equals("MR")){
                h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("MR")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("\\ensuremath{\\delta}");
                wroteLabel=false;

                h+=footnotes.getFlagSymbolsLatex("", "MR");//(flag,type)
                
                h+="}";
               T.addMulticol(c);
            }
            else if(colName.equals("CC")){
            	h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("CC")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("\\ensuremath{\\alpha}");
                wroteLabel=false;
                h+=footnotes.getFlagSymbolsLatex("", "CC");//(flag,type)
                
                h+="}";
               T.addMulticol(c);
            }
            else if(colName.equals("TI")){
               	h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("TI")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("I\\ensuremath{_{(\\gamma+ce)}}");
                wroteLabel=false;
                h+=footnotes.getFlagSymbolsLatex("", "TI");//(flag,type)
                
                h+="}";
               T.addMulticol(c);
            }
            else if(colName.equals("COMM"))h+=("Comments");
            
            if(((c<columns.size()-1)&&(colName.indexOf("D")!=0))&&!((c==columns.size()-2)&&(columns.elementAt(c+1).toString().indexOf("D")==0)))
            	h+=("&");
            
            //System.out.println("name="+colName+" h="+h);
        }
        return h;
    }

    
    @SuppressWarnings("unused")
	private float colHeadWidth(Footnotes footnotes, TableControl tc, Vector<Comment> labcom){
    	float headWidth=0;
    	HashMap<String,String> colStrings=colHeadStrings( footnotes,tc,labcom);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		headWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return headWidth;
    	
    }
    
    private HashMap<String,String> colHeadStrings(Footnotes footnotes, TableControl tc, Vector<Comment> labcom){
        String h="";
        boolean wroteLabel=false;
        String colName="";
        
        HashMap<String,String> colStrings=new HashMap<String,String>();
        
        for(int c=0;c<tc.getPanelColumns(currentPanelNo).size();c++){
        	h="";
        	colName=(String)tc.getPanelColumns(currentPanelNo).elementAt(c);
            if(colName.equals("LEV")) h+="Ei(level)";
            else if(colName.equals("JI")) h+="Jpi";
            else if(colName.equals("LEVF")) h+="Ef";
            else if(colName.equals("JF")) h+="Jpf";
            else if(colName.equals("E")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("E")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+="Eg";
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "E");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }
            else if(colName.equals("RI")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("RI")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("Ig");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "RI");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }
            else if(colName.equals("M")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("M")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("Mult.");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "M");i++)
                    h+=NDSConfig.fakeFlagLabel;     
            }            
            else if(colName.equals("MR")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("MR")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("d");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "MR");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }            
            else if(colName.equals("CC")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("CC")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("a");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "CC");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }           
            else if(colName.equals("TI")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("TI")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("I(g+ce)");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "TI");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }
            else if(colName.equals("COMM"))h+=("Comments");

            colStrings.put(colName, h);
            
            //debug
            //System.out.println("In GammaTableMaker: colHeadString(): colName="+colName+" out="+h+" length="+h.length());
        }
        return colStrings;
    }
    

    /**checks the Fcolumns*/
    private boolean checkFColumns(TableControl tc, Gamma g){
        boolean print=false;
        Vector<String> f=tc.getFcolumns();
        if(f.contains("E")&&!g.ES().isEmpty())print=true;
        if(f.contains("RI")&&!g.RIS().isEmpty())print=true;
        if(f.contains("M")&&!g.MS().isEmpty())print=true;
        if(f.contains("MR")&&!g.MRS().isEmpty())print=true;
        if(f.contains("CC")&&!g.CCS().isEmpty())print=true;
        if(f.contains("TI")&&!g.TIS().isEmpty())print=true;
        if(f.contains("Lev")&&!g.ILS().isEmpty())print=true;
        if(f.contains("LevF")&&!g.FLRecord().isEmpty())print=true;
        if(f.contains("COMM")&&g.nComments()>=0&&g.nContRecords()>=0)print=true;
        return print;
    }

    
    public String commentHeadMaker(Comment com,String option) throws java.lang.Exception{
    	String head="",tempHead="";
    	@SuppressWarnings("unused")
		int n=0;
    	String headName;
    	
    	
    	if(com.nHeads()==0)
    		return "";
        
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
                if(headName.equals("J")) {head+="J\\ensuremath{^{\\pi}}";tempHead+="Jpi";n+=2;}
                else if(headName.equals("RI")) {head+="I\\ensuremath{_{\\gamma}}";tempHead+="Ig";n+=2;}
                else if(headName.equals("MR")) {head+="\\ensuremath{\\delta}";tempHead+="d";n+=1;}
                else if(headName.equals("CC")) {head+="\\ensuremath{\\alpha}";tempHead+="a";n+=1;}
                else if(headName.equals("E")) {head+="E\\ensuremath{_{\\gamma}}";tempHead+="Eg";n+=2;}
                else if(headName.equals("TI")) {head+="I\\ensuremath{_{(\\gamma+ce)}}";tempHead+="I(g+ce)";n+=7;}
                else if(headName.equals("M")) {head+="Mult.";tempHead+="Mult.";n+=5;}
                else if((headName.indexOf("BE")==0 || headName.indexOf("BM")==0) && headName.length()>=3){
                	if(headName.length()==3 && Character.isDigit(headName.charAt(2)))
                		headName+="DWN";
                	
                	head+=Translator.process(headName);
                	tempHead+=headName;
                	n+=headName.length();            	
                }else {           	
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
        
        if(option.equals("temp"))
        	return tempHead;
        
        return head;
    }
    
    
    /**builds the comments
     * @throws java.lang.Exception */
    protected String commentBuilder(Comment c) throws java.lang.Exception{
        String s="";
        if(c.nHeads()>0){
            if(c.headAt(0).equals("J")) s="J\\ensuremath{\\pi}";
            else if(c.headAt(0).equals("RI")) s="I\\ensuremath{_{\\gamma}}";
            else if(c.headAt(0).equals("MR")) s="\\ensuremath{\\delta}";
            else if(c.headAt(0).equals("CC")) s="\\ensuremath{\\alpha}";
            else if(c.headAt(0).equals("E")) s="E\\ensuremath{_{\\gamma}}";
            else if(c.headAt(0).equals("TI")) s="I\\ensuremath{_{(\\gamma+ce)}}";
            else if(c.headAt(0).equals("M")) s="Mult.";
            else s=Translator.process(c.headAt(0));
            s+=": ";
        }
        if(c.nHeads()>0)s+=c.getHTranslated();
        else s+=c.getTranslated();
        return s;
    }
    
    protected String getContRecordRawString(ContRecord cr){
    	String out="";
    	out+=cr.name();
    	if(Str.isNumeric(cr.ds()) || cr.ds().trim().length()==0)
    		out+="="+cr.s()+" "+cr.ds().trim();
    	else
    		out+=cr.ds()+cr.s()+" ";
    	
    	if(cr.ref().trim().length()>0)
    		out+=cr.ref().trim();
    	
    	return out;
    	
    }

    /**convert the continuation records as comments*/
    protected Vector<Comment> convertContRecords_old(int panel,Record d,TableControl tc,boolean showSuppressed)throws Exception{
    	Gamma g=(Gamma)d;
        Vector<Comment> out=new Vector<Comment>();
        
        String temp="";
        ContRecord cr;
        
        boolean isBigC=true;
        for(int k=0;k<g.nContRecords();k++){
        	cr=g.contRecordAt(k);
            if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) ){
                temp=cr.txt();
                Comment c=new Comment();
                c.setBody(temp, isBigC);
                if(temp.trim().length()>0) out.addElement(c);
                
                //debug
                //System.out.println("In GammaTableMaker: procContRecords: "+cr.name()+" out="+temp);
            }
        }
        return out;
    }

    
    protected Vector<Comment> convertDataAsComments(int panel,Record d,TableControl tc)throws Exception{
    	Gamma g=(Gamma)d;
    	Vector<Comment> out=new Vector<Comment>();
    	  
    	String type="";
        String temp;
        
    	if(tc.getDataAsComments()!=null){
            for(int j=0;j<tc.getDataAsComments().size();j++){
                temp="";
                type=tc.getDataAsComments().elementAt(j);
                if(type.equals("RI")&&g.RIS().length()>0) temp=LatexWriter.printNumberAsIs(g.RIS(),g.DRIS());
                else if(type.equals("M")&&g.MS().length()>0) temp=g.MS();
                else if(type.equals("MR")&&g.MRS().length()>0) temp=LatexWriter.printNumberAsIs(g.MRS(),g.DMRS());
                else if(type.equals("CC")&&g.CCS().length()>0) temp=LatexWriter.printNumberAsIs(g.CCS(),g.DCCS());
                else if(type.equals("TI")&&g.TIS().length()>0) temp=LatexWriter.printNumberAsIs(g.TIS(),g.DTIS());
                
                if(temp.trim().length()>0){
                	Comment c=new Comment();
                	if(type.trim().length()>0)
                		c.headV().add(type);
                	c.setBody(temp,true);
                    out.addElement(c);
                }
            }
        }
        return out;
    }
        
    /*return a rough number of the width of all data columns in output table**/
    private float getEntryLength(Gamma g,Vector<String> columns,ENSDF ens)throws Exception{
        String colName;
        String row="";

        int nColumns=columns.size();
        for(int j=0;j<nColumns;j++){
            colName=(String) columns.elementAt(j);
            if(colName.equals("E"))         row+=g.ES()+" ";
            else if(colName.equals("DE"))   row+=g.DES()+" ";
            else if(colName.equals("LEV")&& g.ILI()>0)   row+=ens.levelAt(g.ILI()).ES()+" ";
            else if(colName.equals("DLEV")&& g.ILI()>0)  row+=ens.levelAt(g.ILI()).DES()+" ";
            else if(colName.equals("JI")&& g.ILI()>0)    row+=ens.levelAt(g.ILI()).JPiS()+" ";
            else if(colName.equals("LEVF") && g.FLI()>0) row+=ens.levelAt(g.FLI()).ES()+" ";
            else if(colName.equals("DLEVF")&& g.FLI()>0) row+=ens.levelAt(g.FLI()).DES()+" ";
            else if(colName.equals("JF") && g.FLI()>0)   row+=ens.levelAt(g.FLI()).JPiS()+" ";
            else if(colName.equals("RI"))   row+=g.RIS()+" ";
            else if(colName.equals("DRI"))  row+=g.DRIS()+" ";
            else if(colName.equals("M"))    row+=g.MS()+" ";
            else if(colName.equals("MR"))   row+=g.MRS()+" ";
            else if(colName.equals("DMR"))  row+=g.DMRS()+" ";
            else if(colName.equals("CC"))   row+=g.CCS()+" ";
            else if(colName.equals("DCC"))  row+=g.DCCS()+" ";  
            else if(colName.equals("TI"))   row+=g.TIS()+" ";
            else if(colName.equals("DTI"))  row+=g.DTIS()+" ";
        }
        //System.out.println("**"+row+"**\n");
        return row.length();
    }
    
    /*return flags of a gamma record data**/
    @SuppressWarnings("unused")
	private Vector<String> findDataFlags(Gamma g,String dataType,String flags){
    	String temp="";
    	String flag=g.flag();
    	Vector<String> out=new Vector<String>();
    	char type=dataType.toLowerCase().trim().charAt(0);
        for(int i=0; i<flag.length();i++){
        	String key=flag.substring(i,i+1)+type;
            if(flags.contains(key)){
            	int index=flags.indexOf(key);
                temp=("\\ensuremath{^{"+flags.charAt(index+2)+"}}");
                out.add(temp);
            }
        }
        
        return out;
    }
    
    /*return a rough number of the maximum width of all data entries in output table panel**/
    @SuppressWarnings("unused")
	private float findMaxEntryWidth(Footnotes footnotes,Vector<String> columns,Vector<Gamma> Gammas,Gamma firstGamma,Gamma lastGamma)throws Exception{
    	float maxEntryWidth=0;
    	HashMap<String,String> colStrings=findMaxColumnStrings(footnotes,columns,Gammas,firstGamma,lastGamma);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		maxEntryWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return maxEntryWidth;
    }
    
    /*return a rough number of the maximum width of all data entries in output table panel**/
	private HashMap<String,String> findMaxColumnStrings(Footnotes footnotes,Vector<String> columns,Vector<Gamma> Gammas,Gamma firstGamma,Gamma lastGamma)throws Exception{
        HashMap<String,String> colStrings=new HashMap<String,String>();
        
        if(firstGamma==null)
            return colStrings;
        
        String colName;
        String temp="";
        String maxColString="";
        String flagStr="";
        
        String decimalLeft="";
        String decimalRight="";
        String fakeFlagStr="";
              
        String maxDecimalLeftStr="";
        String maxDecimalRightStr="";
        
        float decimalLeftWidth=0;
        float maxDecimalLeftWidth=0;
        
        float decimalRightWidth=0;
        float maxDecimalRightWidth=0;
        
        float maxColWidth=0;
        float colWidth=0;
        
        ENSDF ens=etd.getENSDF();
       	 
        int start=Gammas.indexOf(firstGamma);
        int end  =Gammas.indexOf(lastGamma);
        
        if(start<0) start=0;
        if(end<0) end=Gammas.size()-1;
        
        int nColumns=columns.size();
        for(int j=0;j<nColumns;j++){    	
            colName=(String) columns.elementAt(j);
            
            maxColString="";
            maxColWidth=0;
            
       	    maxDecimalLeftWidth=0;
       	    maxDecimalLeftStr="";
       	    maxDecimalRightWidth=0;
       	    maxDecimalRightStr="";
       	    fakeFlagStr="";
       	 
            for(int k=start;k<=end;k++){          	
            	Gamma g=Gammas.elementAt(k); 
                temp="";
                if(g.flag().trim().length()>0)
                	flagStr=g.flag();
                else
                	flagStr="*";//fake flag. Note that footnotes.nMarks("",type) returns number of flags for heading of given "type"                	
                
                fakeFlagStr="";
                
                if(colName.equals("E"))         temp=Str.getString(g.ES(), g.DES(), "");
                else if(colName.equals("LEV")&& g.ILI()>0)  {temp=ens.levelAt(g.ILI()).ES();if(tc.showDE()) temp=Str.getString(temp,ens.levelAt(g.ILI()).DES(),"");}
                else if(colName.equals("JI")&& g.ILI()>0)    temp=ens.levelAt(g.ILI()).JPiS().replace(":","*to*");
                else if(colName.equals("LEVF") && g.FLI()>=0) {temp=ens.levelAt(g.FLI()).ES();if(tc.showDE()) temp=Str.getString(temp,ens.levelAt(g.FLI()).DES(),"");}
                else if(colName.equals("JF") && g.FLI()>=0)   temp=ens.levelAt(g.FLI()).JPiS().replace(":","*to*");
                else if(colName.equals("RI"))   temp=Str.getString(g.RIS(),g.DRIS(),"");
                else if(colName.equals("M"))    temp=g.MS();
                else if(colName.equals("MR"))   temp=Str.getString(g.MRS(),g.DMRS(),"");
                else if(colName.equals("CC"))   temp=Str.getString(g.CCS(),g.DCCS(),"");
                else if(colName.equals("TI"))   temp=Str.getString(g.TIS(),g.DTIS(),"");
                else continue;
                
                fakeFlagStr+=Str.repeat(NDSConfig.fakeFlagLabel, footnotes.nMarks(flagStr, colName));
                
                if(temp.trim().length()==0 && fakeFlagStr.length()==0)
               	 continue;
                
                
                if(colName.equals("M") || colName.equals("JI") || colName.equals("JF")){
                	temp+=fakeFlagStr;
                	colWidth=NDSConfig.findWidth(temp);               	
                }else{
                	
                	boolean isExpo=false;
                	if(temp.contains("E") || temp.contains("e"))//eg, 1.2E3 translated as 1.2x10^3
                    {
                		isExpo=true;
                    	temp+="*";
                    	if(!temp.toUpperCase().contains("E+") && !temp.toUpperCase().contains("E-") )
                    		temp+="*";
                    	else
                    		temp+="*";
                    }
                	
                    decimalLeft=Str.delimLeftString(temp, '.');//aligned by '.'
                    decimalRight=Str.delimRightString(temp, '.');
                    
                    //if(!temp.contains(".")){
                    if(!temp.contains(".") || isExpo){//otherwise, aligned by ' '
                       decimalLeft=Str.delimLeftString(temp, ' ');
                       decimalRight=Str.delimRightString(temp, ' ');
                       
                       if(decimalLeft.length()<=0){
                    	   decimalLeft=temp;
                    	   decimalRight=fakeFlagStr;
                       }else if(decimalRight.length()>0){
                          decimalRight=fakeFlagStr+' '+decimalRight;
                       }
                    }else{
                       decimalRight='.'+decimalRight;//add '.' to the right part of the string for width estimation
                       int p=decimalRight.indexOf(' ');
                       if(p>=0)
                       	decimalRight=decimalRight.substring(0, p)+fakeFlagStr+decimalRight.substring(p);
                       else
                       	decimalRight+=fakeFlagStr;
                    }

                    decimalRight=decimalRight.replace(" ", "*");
                    
                    decimalLeftWidth=NDSConfig.findWidth(decimalLeft);
                    decimalRightWidth=NDSConfig.findWidth(decimalRight);
                                       
                    if(decimalLeftWidth>maxDecimalLeftWidth){                 
                	   maxDecimalLeftWidth=decimalLeftWidth;
                	   maxDecimalLeftStr=decimalLeft;
                    }
                    if(decimalRightWidth>maxDecimalRightWidth){                  	
                	   maxDecimalRightWidth=decimalRightWidth;
                	   maxDecimalRightStr=decimalRight;
                    }
                     
                    temp=maxDecimalLeftStr+maxDecimalRightStr;
                    colWidth=maxDecimalLeftWidth+maxDecimalRightWidth;
                }

                if(temp.trim().length()>0 && colWidth>maxColWidth){
                	maxColString=temp;    
                	maxColWidth=colWidth;
                }     
                
                //debug
                //System.out.println("In GammaTableMaker: findMaxColumnStrings: j="+j+" k="+k+" g="+g.ES()+" colName="+colName+" colWidth="+colWidth+" start="+start+" end="+end+" ncolumns="+nColumns+" maxColString="+maxColString+" temp="+temp+" g.FLI="+g.FLI());

            }
            
            //debug
            //System.out.println("In GammaTableMaker: findMaxColumnStrings: j="+j+"  colName="+colName+" start="+start+" end="+end+" ncolumns="+nColumns+" maxColString="+maxColString+" temp="+temp);

            colStrings.put(colName, maxColString);

        }
        
        return colStrings;
    }
    
    private float findMaxCommentWidth(int panel,Gamma g,TableControl tc,boolean showSuppressed)throws Exception{

        String com="";
        float width=0;
        float maxCommentWidth=0;     
        float commentColumnWidth=tc.getCommentColumnWidth(panel); 
        int nlines=0;
        
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
                if(c.headAt(0).equals("J")) com="Jpi";
                else if(c.headAt(0).equals("RI")) com="Ig";
                else if(c.headAt(0).equals("MR")) com="d";
                else if(c.headAt(0).equals("CC")) com="a";
                else if(c.headAt(0).equals("E")) com="Eg";
                else if(c.headAt(0).equals("TI")) com="I(g+ce)";
                else if(c.headAt(0).equals("M")) com="Mult";
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
                com="";
                String type=tc.getDataAsComments().elementAt(j);
                if(type.equals("RI")&&g.RIS().length()>0) com=NDSControl.transColHeadENSDF(type)+": "+g.RIS()+" "+g.DRIS();
                if(type.equals("M")&&g.MS().length()>0) com=NDSControl.transColHeadENSDF(type)+": "+g.MS();
                if(type.equals("MR")&&g.MRS().length()>0) com=NDSControl.transColHeadENSDF(type)+": "+g.MRS()+" "+g.DMRS();
                if(type.equals("CC")&&g.CCS().length()>0) com=NDSControl.transColHeadENSDF(type)+": "+g.CCS()+" "+g.DCCS();
                if(type.equals("TI")&&g.TIS().length()>0) com=NDSControl.transColHeadENSDF(type)+": "+g.TIS()+" "+g.DTIS();

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


class GComparator implements Comparator<Object>{
    public int compare(Object one,Object two){
        Gamma a=(Gamma)one;
        Gamma b=(Gamma)two;
        int out;
        if(a.EF()<b.EF())out=-1;
        else if(a.EF()>b.EF())out=1;
        else{
        	float el1=-1,el2=-1;
        	if(Str.isNumeric(a.ILS())) el1=Float.parseFloat(a.ILS());
        	if(Str.isNumeric(b.ILS())) el2=Float.parseFloat(b.ILS());
        	if(el1<el2)
        		out=-1;
        	else if(el1>el2)
        		out=1;
        	else
        		out=0;
        }
        
        return out;
    }
}