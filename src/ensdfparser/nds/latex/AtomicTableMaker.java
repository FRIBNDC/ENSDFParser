package ensdfparser.nds.latex;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import ensdfparser.ensdf.Comment;
import ensdfparser.ensdf.ContRecord;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.Record;
import ensdfparser.ensdf.TableRecord;
import ensdfparser.ensdf.Atomic.AtomicData;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.EnsdfTableData;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.util.Str;

//added 2/12/2020 for atomic data
public class AtomicTableMaker extends TableMaker{

    private int currentPanelNo;
    private TablePanel T;
    
    private String tableName="ATOMIC";
    private String label="";//for use in column title
    
    String atomicType="X";//X for x ray, A for Auger electrons
    
    Vector <AtomicData> atomicRads=new Vector<AtomicData>();
    
    //private EnsdfTableData etd;
    //@SuppressWarnings("unused")
	//private MassChain data;
    private TableControl ctc;
    @SuppressWarnings("unused")
	private boolean pastBreak=false;
    private String leftoverOfLastCom="";
    
    public AtomicTableMaker(){
        
    }
    
    /*
     * type="X" for x ray, "A" for Auger electrons
     *      
     */
    public AtomicTableMaker(EnsdfTableData etd,MassChain data,String type){
        this.etd=etd;
        this.data=data;

        
        this.atomicType=type;
        
        if(type.equals("X")) {
        	tableName="X rays";
            this.tableType="XRAY";
            this.tc=etd.getXRayTableControl();
            this.ctc=etd.getXRayTableControl();
            
            label="X ray";
        }else if(type.equals("A")) {
        	tableName="Auger electrons";
            this.tableType="AUGER";
            this.tc=etd.getAugerTableControl();
            this.ctc=etd.getAugerTableControl();
            
            label="Auger";
        }
        
        initAtomicRads(type);

        
        init();
    }
    
    /*
     * type="X" for x ray, "A" for Auger electrons
     */
    private void initAtomicRads(String type){
        ENSDF ens=etd.getENSDF();   
        
        if(type.equals("X")) {
        	atomicRads.addAll(ens.atomicRad().xRaysV());
        }else if(type.equals("A")) {
        	atomicRads.addAll(ens.atomicRad().AugersV());
        }
        
        /*no sorting: print data by the order in input file
        AtomicData[] TEMP=new AtomicData[atomicRads.size()];
        for(int i=0;i<atomicRads.size();i++)
        	TEMP[i]=atomicRads.elementAt(i);
        
        for(AtomicData ad: atomicRads)
        	System.out.println("1 "+ad.name()+" e="+ad.e()+" es="+ad.ES());
               	
        Arrays.sort(TEMP,new MComparator());
        atomicRads=new Vector<AtomicData>();
        for(int i=0;i<TEMP.length;i++){            	
        	atomicRads.addElement(TEMP[i]);
        }
        
        for(AtomicData ad: atomicRads)
        	System.out.println("2 "+ad.name()+" e="+ad.e()+" es="+ad.ES());
        */
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
    
    
    /** Writes atomic tables
     * 
     * @param ens
     * @param etd
     * @param panel
     * @return the Table Panel object for this dataset
     * @throws Exception
     */
    /**Writes the atomic table (auto mode)*/
	public TablePanel makeTablePanel_auto(int panel)throws Exception{

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
        
        Vector<String> dataEnergies=new Vector<String>(); 
        
        boolean showSuppressed=etd.isShowSuppressed();

 
        int nColumns;
        Vector<String> columns;

    	nColumns=tc.getPanelColumns(currentPanelNo).size();
    	columns=tc.getPanelColumns(currentPanelNo);

        
        //debug
        //if(tc.getPanelColumns(currentPanelNo).contains("MR"))
        //	System.out.println(tc.getPanelColumns(currentPanelNo));

        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getPanelColumns(currentPanelNo).contains("COMM"))
        	T.addComLoc(tc.getPanelColumns(currentPanelNo).indexOf("COMM"));
                     
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
        footnotes.setLinkIDs(etd.getLinkID()+"ATOMIC_"+atomicType);
        
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
        
    	T.setGenComments(Set);
    	lineCount+=count;
    	linePos+=count*NDSConfig.HEAD_COMMENT_LINEWIDTH;
    	
        //one blank line before and after general comment
        if(Set.size()>0){
        	lineCount+=2;
            linePos+=2*NDSConfig.HEAD_COMMENT_LINEWIDTH;;
        }

        count=0;
        Vector<String> v;
        Set=new Vector<String>();
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            
            if(t.type()=='M' && t.fullLineAt(i).charAt(8)==this.atomicType.charAt(0)) {
            	v=makeTableRecords(t,tc.isFormatTables());//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
            	Set.addAll(v);
            	count=this.nTableRecordsLines;
            }
        }
        
    	T.setTableRecords(Set);
    	T.setNTableRecordsLines(this.nTableRecordsLines);
    	T.setTableRecordsHeight(this.tableRecordsHeight);
    	lineCount+=count;
        
        if(panel<=0){
        	T.setRelHeadLineNo(lineCount+2);//lineCount+(one line space)+(one line headline column names)
        	
        	//linePos+=2*Config.DEFAULT_LINEWIDTH;//if counting lines separately, not using this.tableRecordHeight, slightly different
        	linePos+=tableRecordsHeight+NDSConfig.TABLE_TOP_MARGIN+NDSConfig.HEAD_COMMENT_LINEWIDTH+0.2;//if using this.tableRecordsHeight, more precise
        	
        	T.setRelHeadLinePos(linePos);
        }
        else{
        	T.setRelHeadLineNo(NDSConfig.DEFAULT_HEADLINE_NO);
        	T.setRelHeadLinePos(NDSConfig.DEFAULT_HEADLINE_POS);
        }
        
        Set=new Vector<String>();
        Set.addElement(makeTableHead(footnotes, tc, labcom));//NOTE: global currentPanelNo is used in this call
        T.setFirstHead(Set);

        Set=new Vector<String>();

        row+="\\multicolumn{"+String.valueOf(nColumns)+"}{c}{\\underline{"+tableName+" ($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}}";
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
        String tempHead="",head="";
        
        for(int i=0;i<footnotes.size();i++){
            Comment c = new Comment();
            c=(Comment)footnotes.commentAt(i);
            
        	//System.out.println("AtomicTableMaker 303: footnote      ens.gammaFlags="+ens.gammaFlags()+" com="+c.body()+" c.head="+c.head());
        	//System.out.println("           hashead="+c.hasHead(defaultColumns));       	           
            
            tempHead="h ";//arbitrary, each symbol is one character     
            head=footnotes.footnoteMarkAt(i)+" ";
        	
        	tempComs=wrapENSDFCommentTranslated(c,FOOTNOTE_WIDTH,tempHead,head,c.isBigC(),2);//leave 2-whitespace indent after first line   
        	       	
        	Set.addAll(tempComs);
        }

        
        T.setLastTail(Set);

        Set=new Vector<String>();


        float min=-1000000;
        float max=1000000;
                
        if(panel<0){//for printing the whole table in a single panel with no beak and splitting
        	//panel=0;       
        	//currentPanelNo=0;
        }
        else{             
        	if(panel>0&&panel<=tc.getBreakPoints().size())
        		min=Float.parseFloat(tc.getBreakPoints().elementAt(panel-1));
        	if(panel<tc.getBreakPoints().size())
        		max=Float.valueOf(tc.getBreakPoints().elementAt(panel));
            
        }
       
                     
        //print each row of the table
        Vector<String> comsFormatted;
        
        boolean pastMax=false;
        boolean pastMin=false;
        boolean isLastRadInPrevPanel=false;
        lineCount=0;
        this.nTableBodyLines=0;
        this.tableBodyHeight=0;
        float nextEF=0;
        boolean pastBreak=false;
        
        AtomicData firstRad=null,lastRad=null;
        
        
        for(int i=0;i<atomicRads.size();i++){
            row="";
            AtomicData ad=atomicRads.get(i);
            AtomicData nd;//next radiation
            if(i<atomicRads.size()-1){
            	nd=atomicRads.get(i+1);
            	nextEF=nd.EF();
            }
            else{
            	nd=null;
            	nextEF=ad.EF()+100;
            }
            //System.out.println("AtomicTableMaker 372: ES="+ad.ES()+"  EF="+ad.EF()+"  nextES="+nd.ES()+"  nextEF="+nextEF);
            
            comsFormatted=procComs(panel,ad,tc,showSuppressed);
                  
            //System.out.println("AtomicTableMaker 376: ES="+ad.ES()+"  EF="+ad.EF()+"  nextES="+nd.ES()+"  nextEF="+nextEF);
            //debug
            //for(String s:comsFormatted) System.out.println("LevelTableMaker 408: #1 l="+l.ES()+"   comment="+s);
            
            //determine whether this level should be drawn in this panel
            if(ad.EF()>=min) {
            	pastMin=true;
            	if(firstRad==null)
            		firstRad=ad;
            }
            if(ad.EF()>=max && panel>=0) {
            	if(!pastBreak) tc.setFirstLineEnergy(panel+1, ad.EF()); //pastBreak check if this level has already passed the break point 
            	                                                       //for the case where breaking is in the middle of comment lines 
            	                                                       //(pastBreak set to be true there)
            	
                //System.out.println("In AtomicTableMaker line 418: past break="+pastBreak+" i="+i+" ad.EF()="+ad.EF()+" pastMin="+pastMin+" pastMax="+pastMax+" min="+min+" max="+max);

            	pastBreak=true;
            	pastMax=true;
            	
            	if(lastRad==null && i>0)
            		lastRad=atomicRads.get(i-1);
            }
            else if(nextEF>=max || i==atomicRads.size()-1)
            	if(lastRad==null)
            		lastRad=atomicRads.lastElement();
            
            //debug
            //if(panel==0) 
            //System.out.println("In LevelTableMaker line 429: i="+i+" ad.EF()="+ad.EF()+" pastMin="+pastMin+" pastMax="+pastMax+" min="+min+" max="+max);
            
            isLastRadInPrevPanel=false;
            if(ad.EF()<min && nextEF>min){//break in the middle of comments of last rad in previous panel, so print the remaining comments in the new panel, only when autoAdjust=true
            	pastMin=true;
            	isLastRadInPrevPanel=true;
            	
            	//debug
            	//if(panel==1){
            	//System.out.println("In LevelTableMaker: **comsFormatted.size: "+comsFormatted.size());
            	//System.out.println("                      comsFormatted[0]="+comsFormatted.elementAt(0));
            	//System.out.println("                      comsFormatted[last]="+comsFormatted.lastElement());
            	//}
            	
            	comsFormatted=procComsFormattedPartial(panel,ad,tc,showSuppressed,tc.getLastLine(panel-1),tc.getSecondLastLine(panel-1));//get rad comments continued in new panel, so skip rad comments before lastComLineInPrevPanel        

            	//debug
            	//if(panel==0){
            	//System.out.println("                    **comsFormattedPartial.size: "+comsFormatted.size());
            	//System.out.println("                      lastComLineInPrevPanel="+tc.getLastLine(panel-1));
            	//System.out.println("                      firstlineCom="+tc.getFirstLine(panel));
            	//System.out.println("                      panel="+panel+" l.EF()="+l.EF()+" firstComE="+tc.getFirstLineEnergy(panel)+" minlev="+minlev+" maxlev="+maxlev);
            	//if(comsFormatted.size()>0) System.out.println("                    comsFormattedPartial: comsFormatted[0]="+comsFormatted.elementAt(0));
            	//}
            }
                        
            if(pastMax||!pastMin) continue;
            
            //debug
            //if(panel>=0) System.out.println("In LevelTableMaker line 425: i="+i+" l.EF()="+l.EF()+" pastMin="+pastMin+" pastMax="+pastMax+" min="+minlev+" max="+maxlev);
        	//debug
        	//if(panel==0){
        	//System.out.println("                    **comsFormattedPartial.size: "+comsFormatted.size());
        	//System.out.println("                      lastComLineInPrevPanel="+tc.getLastLine(panel-1));
        	//System.out.println("                      firstlineCom="+tc.getFirstLine(panel));
        	//System.out.println("                      panel="+panel+" l.EF()="+l.EF()+" firstComE="+tc.getFirstLineEnergy(panel)+" minlev="+minlev+" maxlev="+maxlev);
        	//if(comsFormatted.size()>0) System.out.println("                    comsFormattedPartial: comsFormatted[0]="+comsFormatted.elementAt(0));
        	//}
            
            	
            //debug
            //for(String s:comsFormatted) System.out.println("LevelTableMaker 530: #2 l="+l.ES()+"   comment="+s);
            
			row=makeAtomicEntry(ad,columns,ens,comsFormatted,footnotes);

            //debug
            //System.out.println("In AtomicTableMaker: row="+row);
                        
            if(!isLastRadInPrevPanel){
            	Set.addElement(row);
                lineCount++;          
                this.nTableBodyLines++;
                
                float scale0=1.0f,scale1=1.0f,scale3=1.0f;
                if(Str.containSpecialSupScript(row)){
                	scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*1.10f;
                	scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;      
                	scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;       
                	
                	//if(panel==7) System.out.println("########### In LevelTableMaker: special script row="+row);
                	
                }else if(Str.containSupScript(row, "0,1,2,3,4,5,6,7,8,9")){
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*0.2f;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;      
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;    
                }else if(Str.containSupScript(row)){
                    if(Str.containJPIScriptOnly(row)){
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                    }else if(Str.containGivenSupScriptOnly(row,"x")){
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                    }else{
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                    }
                	
                }else if(Str.containSubScript(row)) {
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                }
                

                //System.out.println("########### In LevelTableMaker: special script row="+Str.containSpecialScript(row)+"  "+Str.containScript(row)+row);
                
                if(comsFormatted.size()==0)
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH0*scale0;
                else if(comsFormatted.size()==1)
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH1*scale1;
                else 
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale3;

                //System.out.println("AtomicTableMaker 499: name="+ad.name()+"  ES="+ad.ES()+"  EF="+ad.EF()+"  nextEF="+nextEF);
                
            	if(ad.EF()>=0){
                    dataEnergies.add(String.valueOf(ad.EF()));
            	}
            	else{
                    dataEnergies.add(String.valueOf(nextEF));
            	}
                
            }
      
            String tempS="";
            if(tc.getPanelColumns(panel).contains("COMM")){
            	float e1=0,e2,de,ef;
            	int start=0;
            	String es="";
            	
            	if(isLastRadInPrevPanel)
            		e1=min;
            	
            	if(ad.EF()>e1)
            		e1=ad.EF();
            	
            	if(nd!=null)
            		e2=nd.EF();
            	else
            		e2=e1+100;
            	
            	de=e2-e1;
            	if(comsFormatted.size()>0)
            	    de=(e2-e1)/comsFormatted.size();
            	
            	if(isLastRadInPrevPanel)
            		start=0;
            	else
            		start=1;
          	
            	@SuppressWarnings("unused")
				int index=-1,size=-1;     
            	float firstLineEF=-1;
            	if(comsFormatted.size()>0){
                    int[] a=findIndexAndSize(max,e1,e2,comsFormatted.size(),0,10);
                    index=a[0];//index in comsFormatted of the comment line with maxlev
                    size=a[1];
                    
                    int newindex=0;
                    if(de>0 && max>e1 && max<e2)
                    	newindex=(int)((max-e1)/de);  
                    
                    //debug
                    //System.out.println("AAAAA In LevelTableMaker: firstComLineE="+tc.getFirstLineEnergy(panel)+" l.EF()="+l.EF()+" maxlev="+maxlev+" minlev"+minlev+" row="+row);
                    //System.out.println("                          e1="+e1+" e2="+e2+" index="+index+" newindex="+newindex+" size="+size+" comsFormatted.size()="+comsFormatted.size());

                 
                    if(index<newindex)
                    	index=newindex;

            	}
            	
                for(int k=start;k<comsFormatted.size();k++){
                    row="";
                    
                   	tempS=comsFormatted.elementAt(k);                  
                    for(int j=0;j<tc.getPanelColumns(panel).indexOf("COMM");j++) 
                    	row+="&";
                    
                    row+=tempS;
                    
                    for(int j=tc.getPanelColumns(panel).indexOf("COMM");j<tc.getPanelColumns(panel).size()-1;j++) 
                    	row+="&";
                    
                    ef=e1+de*k;
                    
                    firstLineEF=ef;

                    
                    if(ef>=max) {
                    	if(panel>=0) 
                    		tc.setFirstLineEnergy(panel+1, firstLineEF);  
                    	
                    	//debug
                    	//System.out.println("   In LevelTableMaker line 648: firstComLineE="+tc.getFirstLineEnergy(panel)+" l.EF()="+l.EF()+" maxlev="+maxlev+" minlev"+minlev+" next panel First ef="+ef+" row="+row);
                        leftoverOfLastCom="";
                        for(int m=k;m<comsFormatted.size();m++) {
                            String line=comsFormatted.get(m);
                            if(!isContinuedCommentLine(line))   
                                break;                               
                                
                            leftoverOfLastCom+=" "+cleanFormattedCommentLine(line);

                        }
                        
                    	pastBreak=true;
                    	
                    	break;
                    }
                    
                    //debug
                    //if(ef>900 && ef<1500){
                    //   System.out.println("AAAAA In LevelTableMaker: firstComLineE="+tc.getFirstLineEnergy(panel)+" l.EF()="+l.EF()+" maxlev="+maxlev+" minlev"+minlev+" ef="+ef+" row="+row);
                    //   System.out.println("                          k="+k+" e1="+e1+" e2="+e2+" index="+index);
                    //}
                              
                    if(index>0){                      
                    	if(k>=index || k==comsFormatted.size()-1){//if previous remakeTablePanel split the comment to more lines, it is possible index (previous)>coms.size-1 (current).
                            //For this case, k will reach coms.size-1

                    		if(k==comsFormatted.size()-1 && k>0 && max<e2){//if break at last comment line, print that line in current panel and move the break point to next gamma
                    			firstLineEF=e2;
                    			T.setIsBreakAtLastCom(true);
                    		}
                        	
                        	if(panel>=0) 
                        		tc.setFirstLineEnergy(panel+1, firstLineEF);  

                        	//debug
                        	//System.out.println("   In LevelTableMaker line 677: firstComLineE="+tc.getFirstLineEnergy(panel)+" l.EF()="+l.EF()+" maxlev="+maxlev+" minlev"+minlev+" next panel First ef="+firstLineEF+" row="+row);

                        	pastBreak=true;
                        			
                        	if(k<comsFormatted.size()-1)
                        		break;
                        }	
                    }

                    
 
                    if(ef<min) continue; //break in the middle of comments of a level, only print part after the break for the panel after this break
                    
                    //debug
                    //if(ef>900 && ef<1500){
                    //   System.out.println("BBBBB In LevelTableMaker: firstComLineE="+tc.getFirstLineEnergy(panel)+" l.EF()="+l.EF()+" maxlev="+maxlev+" minlev"+minlev+" ef="+ef+" row="+row);
                    //   System.out.println("                          panel="+panel+" k="+k+" e1="+e1+" isLastLevelInPrevPanel="+isLastLevelInPrevPanel+" comsFormatted.size()="+comsFormatted.size());
                    //   System.out.println("                          lineCount="+lineCount);
                    //}
                    
                    Set.addElement(row);
                    
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

                    
                    //debug
                    //for( int m=0;m<Set.size();m++){
                    	//System.out.println("       %%%%%%%%% panel="+panel+"m="+m+"  "+Set.elementAt(m));
                    	//System.out.println("                 l.EF()="+l.EF()+" maxlev="+maxlev+" minlev"+minlev+" ef="+ef+" row="+row);
                    //}
                    
                    lineCount++;
                    this.nTableBodyLines++;
                    this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale;//the line spacing between comment lines are slight larger due to the use of parbox than when no comment present
                    
                    //debug
                    //if(panel==0) System.out.println("In LevelTableMaker Line 686: nTableBodyLines="+this.nTableBodyLines+" at l.EF()="+l.EF()+" l.ES()="+l.ES()+"  "+row);
                    //if(panel==0) System.out.println("                              tableBodyHeight="+this.tableBodyHeight+" ef="+ef);
                    
                    es=String.valueOf(ef);
                    
                    dataEnergies.add(es);
                    
                }

                
            }

                    
            width=getEntryLength(ad,tc.getPanelColumns(panel));
                        
            if(width>maxDataWidth) 
            	maxDataWidth=width;
                       
            this.nCommentLines=0;//incremented in findMaxCommentWidth()
            width=findMaxCommentWidth(panel,ad,tc,showSuppressed);
            if(width>this.maxCommentWidth)
            	this.maxCommentWidth=width;   
            
            //if(this.nCommentLines>0)
            //	this.nTableBodyLines+=this.nCommentLines-1;//the first comment line is in the data line which is already counted 
            
            //debug
            //System.out.println("In LevelTableMaker: i="+i+" nTableBodyLines="+this.nTableBodyLines+" after l.EF()="+l.EF());
            
        }//atomic data

        //debug
        //System.out.println("******* TableMaker line 587 panel="+panel+"     IsBreakAtLastCom="+T.isBreakAtLastCom());
        
        T.setBody(Set);
       
       
        
        if(Set.size()>0 && panel>=0){
            tc.setFirstLine(panel,Set.elementAt(0));
            tc.setFirstLineEnergy(panel, Str.parseFloatEnergy(dataEnergies.firstElement())); 
            
            tc.setLastLine(panel,Set.lastElement());
            tc.setLastLineEnergy(panel, Str.parseFloatEnergy(dataEnergies.lastElement()));  
            
            if(Set.size()>1)
            	tc.setSecondLastLine(panel, Set.elementAt(Set.size()-2));
            
            tc.setLeftoverOfLastCom(panel,leftoverOfLastCom);
        }
 

        try{
        	//check if the first line is the same as the last line just set here
        	float nextLineE=tc.getFirstLineEnergy(panel+1);
        	if(nextLineE==tc.getLastLineEnergy(panel)){
        		tc.setFirstLine(panel+1, null);
        		tc.setFirstLineEnergy(panel+1, nextLineE+Math.min(0.1f,0.001f*nextLineE));
        	}
        		
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        
        T.setNTableBodyLines(this.nTableBodyLines);
        T.setTableBodyHeight(this.tableBodyHeight);
        T.setTotalDataLines(lineCount);
        
        T.setDataLineEnergies(dataEnergies);
        
        //debug
        //System.out.println("In GammaTableMaker 596: panel="+panel+" lastLine="+Set.lastElement());
        
        //Set=new Vector<String>();

        //maxDataWidth=maxDataWidth*Config.CHAR2CM;//this is inaccurate since there could be different empty columns in different lines

        HashMap<String,String> maxColStrings=findMaxColumnStrings(footnotes,tc.getPanelColumns(panel),atomicRads,firstRad,lastRad);
        HashMap<String,String> headStrings=colHeadStrings(footnotes, tc, labcom);
        
        maxDataWidth=findMaxDataWidth(maxColStrings,headStrings);

        maxCommentWidth=this.maxCommentWidth;//this.maxCommentWidth is determined in makeSingleGamma()
        
        dataHeadWidth=findDataHeadWidth(maxColStrings,headStrings);
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
	//see comments in AtomicTableMaker
	public TablePanel makeTablePanel_manual(int panel,boolean U)throws Exception{

        ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        Vector<String> Set=new Vector<String>();
        String row="";
        currentPanelNo=panel;
                    
        this.maxCommentWidth=0;
        
        
        boolean showSuppressed=etd.isShowSuppressed();

 
        int nColumns;
        Vector<String> columns;
        
        if(panel<0){
        	System.out.println("In manual mode for AtomicTableMaker, panelNo<0 is not allowed! Please check the code!");
        	System.exit(0);
        }

    	nColumns=tc.getPanelColumns(currentPanelNo).size();
    	columns=tc.getPanelColumns(currentPanelNo);
        
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
        
        Footnotes footnotes=new Footnotes();
        footnotes.findFootnotes(ctc);//wrap of footnote comments and flag information
        
        //set link ID for each footnote: etd.getLinkID()+"GAMMA"+index
        footnotes.setLinkIDs(etd.getLinkID()+"ATOMIC_"+atomicType);
        
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
        
        
        for(int i=0; i<topcom.size();i++){
            Comment c = new Comment();
            c=(Comment)topcom.elementAt(i);
            row+="\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+c.getRegTranslated();
            Set.addElement(row); 
            row="";
        }
        
    	T.setGenComments(Set);
        
        Vector<String> v;
        Set=new Vector<String>();
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            if(t.type()=='M'&& t.fullLineAt(i).charAt(8)==this.atomicType.charAt(0)) {
            	v=makeTableRecords(t,tc.isFormatTables());//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
            	Set.addAll(v);
            }
        }
        
    	T.setTableRecords(Set);
        
        Set=new Vector<String>();
        Set.addElement(makeTableHead(footnotes, tc, labcom));//NOTE: global currentPanelNo is used in this call
        T.setFirstHead(Set);
        
        
        Set=new Vector<String>();

        row+="\\multicolumn{"+String.valueOf(nColumns)+"}{c}{\\underline{"+tableName+" ($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}}";
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

        float min=-1000000;
        float max=1000000;

        
    	if(currentPanelNo>0&&currentPanelNo<=tc.getBreakPoints().size()){
    		min=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo-1).toString());
    	}
        if(currentPanelNo<tc.getBreakPoints().size()){
    	    max=Float.valueOf(tc.getBreakPoints().elementAt(currentPanelNo).toString());
        }
        
       
        boolean pastMax=false,pastMin=false;
        
        Vector<String> comsFormatted;     
        
        for(int i=0; i<atomicRads.size();i++){
            AtomicData ad=(AtomicData)atomicRads.elementAt(i);
                
            comsFormatted=procComs(panel,ad,tc,showSuppressed);
            
            if(ad.EF()>=min) {
            	pastMin=true;
            }
            if(ad.EF()>=max) {
            	pastMax=true;
            }             	
            
            if(pastMax || !pastMin) 
            	continue;

			row=makeAtomicEntry(ad,columns,ens,comsFormatted,footnotes);
			
	    	Set.addElement(row);
                
            String tempS="";
            
            if(tc.getPanelColumns(panel).contains("COMM")){         	
                for(int k=1;k<comsFormatted.size();k++){
                    row="";
                    
                   	tempS=comsFormatted.elementAt(k);                  
                    for(int j=0;j<tc.getPanelColumns(panel).indexOf("COMM");j++) 
                    	row+="&";
                    row+=tempS;
                    for(int j=tc.getPanelColumns(panel).indexOf("COMM");j<tc.getPanelColumns(panel).size()-1;j++) 
                    	row+="&";
                    
                    Set.addElement(row);                 
                }                
            } 
        }
           
        T.setBody(Set);
                    
        Set=new Vector<String>();
   
        //determine the orientation of just created table based on all columns widths
        //T.setPortrait(tc.isPortrait(T));
        
        return T;

    }
	
    
    /** make a line in atomic table from gamma field data
     * @throws Exception */
    private String makeAtomicEntry(AtomicData ad,Vector<String> columns,ENSDF ens, Vector<String> coms,Footnotes footnotes) throws Exception{
    	String r="",temp;
    	String colName;
    	String flag=ad.flag();
    	boolean hasFlag=false;
    	boolean print=false;
    	
    	String flagsLatex="";
    	
    	int nColumns=columns.size();
        
        for(int c=0;c<nColumns;c++){
	     	
        	colName=(String) columns.elementAt(c);
        	
        	String S="";
        	flagsLatex="";
        	
        	print=false;
        	
        	//get value+uncertainty string
     
        	
        	if((colName.equals("E")||colName.equals("DE")) && ad.ES().length()>0){
        		
                S=LatexWriter.printNumber(ad.ES(),ad.DES());
  
                S=addFlagsLatex(S, flag, footnotes,"E");
                flagsLatex=addFlagsLatex("",flag, footnotes,"E");
                
                if(ad.q().equals("S"))//"S" marked the energy is to be put in bracket
                	S="("+S+")";
                
                print=true;
            }
            else if(colName.equals("IM")||colName.equals("DIM")){
            	S=LatexWriter.printNumber(ad.RIS(),ad.DRIS());
                S=addFlagsLatex(S,flag,footnotes,"IM");
                flagsLatex=addFlagsLatex("",flag, footnotes,"IM");
                print=true;
                hasFlag=hasPrintFlag(flag,footnotes, "IM");
                if(ad.RIS().length()>0 || hasFlag)
                	print=true;
            }
            
            //print string
            temp="";
            int n=-1;
            if(columns.contains("D"+colName)){
                temp="\\multicolumn{1}{r@{}}{";
                if(S.contains(".") && (!S.contains("times10^")||S.contains("-"))){//exclude case like 1.53X10^3 12, exception 1.53X10^-3
                    temp+=S.substring(0,S.indexOf("."));
                }else if(!S.contains(".") && S.contains("times10^") && S.contains("-")){//e.g, 1072X10^-6 16, aliged by X
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
                    
                }else if(!S.contains(".") && S.contains("times10^") && S.contains("-")){
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
            }else if(colName.equals("NAME")) {
                if(ad.name().length()>0){
                	String name=ad.name().toUpperCase();
                	//if(!name.startsWith("TOT") && !name.trim().contains(" ") && !name.contains("-"))
                	//	name=name.charAt(0)+"-"+name.substring(1);
                	
                    temp=("\\multicolumn{1}{l}{"+name+"}");
                    print=true;
                }
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
        	
   
        	if(colName.equals("NAME")) {
        		h+="Transitions";
        	}else if(colName.equals("E")){
                h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("E")){
                    	
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel) {
                	//h+="E("+this.label+")";
                	h+="E(keV)";
                }
                wroteLabel=false;
                
                h+=footnotes.getFlagSymbolsLatex("", "E");//(flag,type)
                
                h+="}";
                T.addMulticol(c);
            }
            else if(colName.equals("IM")){
                h+="\\multicolumn{2}{c}{";
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("IM")){
                        h+=labcom.elementAt(i).getTranslated().substring(labcom.elementAt(i).getTranslated().toUpperCase().indexOf("LABEL=")+6);

                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel) {
                	//h+=("I("+this.label+")");
                	h+="I(\\%)";
                }
                wroteLabel=false;
                
                h+=footnotes.getFlagSymbolsLatex("", "IM");//(flag,type)
                
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

        	if(colName.equals("NAME")) {
        		h+="Name";
        	}else if(colName.equals("E")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("E")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+="E("+this.label+")";
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "E");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }
            else if(colName.equals("IM")){
                for(int i=0;i<labcom.size();i++)
                    if(labcom.elementAt(i).headAt(0).equals("IM")){
                        h+=labcom.elementAt(i).body().substring(labcom.elementAt(i).body().toUpperCase().indexOf("LABEL=")+6);
                        wroteLabel=true;
                        break;
                    }
                if(!wroteLabel)h+=("I("+this.label+")");
                wroteLabel=false;
                for(int i=0;i<footnotes.nMarks("", "IM");i++)
                    h+=NDSConfig.fakeFlagLabel;
            }else if(colName.equals("COMM"))h+=("Comments");

            colStrings.put(colName, h);
            
            //debug
            //System.out.println("In GammaTableMaker: colHeadString(): colName="+colName+" out="+h+" length="+h.length());
        }
        return colStrings;
    }
    
   
    public String commentHeadMaker(Comment com,String option) throws java.lang.Exception{
    	String head="",tempHead="";
    	@SuppressWarnings("unused")
		int n=0;;
    	String headName;
    	
    	
    	if(com.nHeads()==0)
    		return "";
    	

    	for(int k=0;k<com.nHeads();k++){
        	headName=com.headAt(k);
        	
        	if(head.length()>0){
        		head+=",";
        		tempHead+=",";
        		n+=1;
        	}
        	
        	if(headName.equals("NAME")) {head+="Transition(s)";tempHead+="Transition(s)";n+=4;}
        	else if(headName.equals("E")) {head+="E("+this.label+")";tempHead+="E("+this.label+")";n+=3+label.length();}
            else if(headName.equals("IM")) {head+="I("+this.label+")";tempHead+="I("+this.label+")";n+=3+label.length();}
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
            if(c.headAt(0).equals("E")) s="E("+this.label+")";
            else if(c.headAt(0).equals("IM")) s="I("+this.label+")";
            else if(c.headAt(0).equals("NAME")) s="Name";
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
    
    protected Vector<Comment> convertDataAsComments(int panel,Record d,TableControl tc)throws Exception{
    	AtomicData ad=(AtomicData)d;
    	Vector<Comment> out=new Vector<Comment>();
    	  
    	String type="";
        String temp;
        
    	if(tc.getDataAsComments()!=null){
            for(int j=0;j<tc.getDataAsComments().size();j++){
                temp="";
                type=tc.getDataAsComments().elementAt(j);
                if(type.equals("IM")&&ad.RIS().length()>0) temp=LatexWriter.printNumberAsIs(ad.RIS(),ad.DRIS());
 
                
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
    private float getEntryLength(AtomicData ad,Vector<String> columns)throws Exception{
        String colName;
        String row="";

        int nColumns=columns.size();
        for(int j=0;j<nColumns;j++){
            colName=(String) columns.elementAt(j);
            if(colName.equals("NAME")) row+=ad.name()+"  ";
            else if(colName.equals("E"))         row+=ad.ES()+" ";
            else if(colName.equals("DE"))   row+=ad.DES()+" ";
            else if(colName.equals("IM"))   row+=ad.RIS()+" ";
            else if(colName.equals("DIM"))  row+=ad.DRIS()+" ";
        }
        //System.out.println("**"+row+"**\n");
        return row.length();
    }
    
    /*return flags of a gamma record data**/
    @SuppressWarnings("unused")
	private Vector<String> findDataFlags(AtomicData ad,String dataType,String flags){
    	String temp="";
    	String flag=ad.flag();
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
	private float findMaxEntryWidth(Footnotes footnotes,Vector<String> columns,Vector<AtomicData> atomicRads,AtomicData firstRad,AtomicData lastRad)throws Exception{
    	float maxEntryWidth=0;
    	HashMap<String,String> colStrings=findMaxColumnStrings(footnotes,columns,atomicRads,firstRad,lastRad);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		maxEntryWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return maxEntryWidth;
    }
    
    /*return a rough number of the maximum width of all data entries in output table panel**/
	private HashMap<String,String> findMaxColumnStrings(Footnotes footnotes,Vector<String> columns,Vector<AtomicData> atomicRads,AtomicData firstRad,AtomicData lastRad)throws Exception{
        HashMap<String,String> colStrings=new HashMap<String,String>();
        
        if(firstRad==null)
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
       	 
        int start=atomicRads.indexOf(firstRad);
        int end  =atomicRads.indexOf(lastRad);
        
        if(start<0) start=0;
        if(end<0) end=atomicRads.size()-1;
        
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
            	AtomicData ad=atomicRads.elementAt(k); 
                temp="";
                if(ad.flag().trim().length()>0)
                	flagStr=ad.flag();
                else
                	flagStr="*";//fake flag. Note that footnotes.nMarks("",type) returns number of flags for heading of given "type"                	
                
                fakeFlagStr="";
                
                if(colName.equals("NAME")) temp=ad.name();
                else if(colName.equals("E"))         temp=Str.getString(ad.ES(), ad.DES(), "");
                else if(colName.equals("IM"))   temp=Str.getString(ad.RIS(),ad.DRIS(),"");
                else continue;
                
                fakeFlagStr+=Str.repeat(NDSConfig.fakeFlagLabel, footnotes.nMarks(flagStr, colName));
                
                if(temp.trim().length()==0 && fakeFlagStr.length()==0)
               	 continue;
                
                
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
    
    private float findMaxCommentWidth(int panel,AtomicData ad,TableControl tc,boolean showSuppressed)throws Exception{

        String com="";
        float width=0;
        float maxCommentWidth=0;     
        float commentColumnWidth=tc.getCommentColumnWidth(panel); 
        int nlines=0;
        
        for(int j=0;j<ad.nContRecords();j++){
        	ContRecord cr=ad.contRecordAt(j);
            if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) ){
                com=ad.contRecordAt(j).name()+"="+ad.contRecordAt(j).s()+" "+ad.contRecordAt(j).ds()+" "+ad.contRecordAt(j).ref(true);
                
                width=com.length()*NDSConfig.CHAR2CM;
                if(width>maxCommentWidth) maxCommentWidth=width;
                if(width>0)
               	 nlines+=width/commentColumnWidth+1;
                
            }
        }
        
        for(int n=0;n<ad.nComments();n++){
        	Comment c=ad.commentAt(n);
            com="";
            if(c.nHeads()>0){  
            	if(c.headAt(0).equals("E")) com="E";
                else if(c.headAt(0).equals("IM")) com="Im";
                else com=Translator.process(c.headAt(0));
                com+=": ";
            }     
            com+=c.body();
            
            width=com.length()*NDSConfig.CHAR2CM;
            if(width>maxCommentWidth) maxCommentWidth=width;
            if(width>0)
           	 nlines+=width/commentColumnWidth+1;
        }
        
        
        this.nCommentLines=nlines;
        return maxCommentWidth;
    }
    
    
}


class MComparator implements Comparator<Object>{
    public int compare(Object one,Object two){
        AtomicData a=(AtomicData)one;
        AtomicData b=(AtomicData)two;
        int out;
        
        /*
        String name1=a.name();
        String name2=b.name();
        
        if(name1.isEmpty() || name1.toUpperCase().startsWith("TOT"))
        	return -1;
        
        if(name2.isEmpty() || name2.toUpperCase().startsWith("TOT"))
        	return 1;
        
            
        char shell1=name1.charAt(0);
        char shell2=name2.charAt(0);
        
        if(shell1<shell2)
        	return -1;
        else if(shell1>shell2)
        	return 1;
        else {
        	name1=name1.substring(1);
        	name2=name2.substring(1);
        	
            if(name1.isEmpty() || name1.toUpperCase().startsWith("TOT"))
            	return -1;
            
            if(name2.isEmpty() || name2.toUpperCase().startsWith("TOT"))
            	return 1;
            
            int r=name1.compareToIgnoreCase(name2);
            if(r<0)
            	out=-1;
            else if(r>0)
            	out=1;
            else {//same level, then compare subshell
            	
                if(a.e()<b.e()) 
                	out=-1;
                else if(a.e()>b.e())
                	out=1;
                else
                	out=0; 
            }
        }

        */
        if(a.EF()<b.EF()) 
        	out=-1;
        else if(a.EF()>b.EF())
        	out=1;
        else
        	out=0; 
        
        return out;
    }
}
