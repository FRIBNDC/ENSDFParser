/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ensdfparser.nds.latex;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import ensdfparser.ensdf.*;
import ensdfparser.ensdf.Record;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.*;
import ensdfparser.nds.util.Str;

/**Writes the tables with levels in them*/
public class LevelTableMaker extends TableMaker {

	TablePanel T;
    //private EnsdfTableData etd;
    //private MassChain data;
    //private TableControl tc;
    
    public LevelTableMaker(){
        
    }
    
    public LevelTableMaker(EnsdfTableData etd,MassChain data){
        this.etd=etd;
        this.data=data;
        this.tc=etd.getLevelTableControl();
        this.tableType="LEVEL";
        XREFLinks=new HashMap<String,String>();
        init();
    }
    
    /**Implement the abstract method in parent class*/
    public TablePanel makeTablePanel(int panel,boolean isUnplaced)throws Exception{
    	return makeTablePanel(panel);
    }
    
    /** write level table */
    public TablePanel makeTablePanel(int panel)throws Exception{
    	if(autoAdjust)
    		return makeTablePanel_auto(panel);
    	else
    		//return makeTablePanel_manual(panel);
    		return makeTablePanel_auto(panel);
    }
    
    //for automatic mode
    @SuppressWarnings({ "rawtypes", "unused" })
	public TablePanel makeTablePanel_auto(int panel)throws Exception{
        /*Levels table*/
        ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        Vector<String> Set=new Vector<String>();
        Vector<String> tempComs;
        
        String row="";
        String temp="";
        float maxRowWidth=0;
        float maxCommentWidth=0;
        float maxDataWidth=0;
        float dataHeadWidth=0;//including empty column if existing
        float width=0;
        int lineCount=0;
        float linePos=0;
        int count=0;

        Vector<String> levelEnergies=new Vector<String>();
        Vector<String> dataEnergies=new Vector<String>();
        
        this.maxCommentWidth=0;
        
        boolean showSuppressed=etd.isShowSuppressed();
        
		//debug					
		//if(panel>0) System.out.println("##In LevelTableMaker line 80 tc breakpoint size="+tc.getBreaks().size()+" panelNo="+panel+" breakPos="+tc.getBreaks().elementAt(panel-1).pos);
		//if(panel>0) System.out.println("##In LevelTableMaker line 81 panel column size="+tc.getBreaks().elementAt(panel-1).panelColumns.size());
		
        //table header
        int nColumns;
        Vector<String> columns;

        columns=tc.getPanelColumns(panel);
        
		//debug					
		//System.out.println("B*In LevelTableMaker tc size"+tc.getBreaks().size()+" panelNo="+panel);
		
        nColumns=columns.size();
        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        //if(panel==0)
        //	System.out.println(" #panel="+panel+" columns size="+nColumns);
        
        if(tc.getDefaultColumns().contains("COMM"))
        	T.addComLoc(tc.getDefaultColumns().indexOf("COMM"));

        //prepare general level comments, footnotes (the footnotes dont get written till the end of the table)
        //then write some comments at the start
        Vector<String> footnotes=new Vector<String>();

        //add non-band comments
        String Eheadflags="",Theadflags="",Jheadflags="",Lheadflags="",Sheadflags="";
        String tempHead,head;
        Comment c;
        ComFlag cf;
        Vector<ComFlag> flags=new Vector<ComFlag>();
        ComFlag.setNumFlags(0);
        for(int j=0;j<tc.getComments().size();j++){
            c=(Comment)tc.getComments().elementAt(j);
            
        	//process general comment with head, like "cL BE2W$...."
        	Comment topComment=new Comment();    	
        	if(c.nHeads()>0 && c.type().charAt(0)!='N')//head in "N" comment is not printed, type=line.substring(7,9), like "L ", "G "
        		topComment=tc.extractTopComment(c);
        	else
        		topComment=c.clone();   	         	
           	
            //comments without head, must be general comments at top of table
            //comments with head, if containing head like "BE2W", is also at top of table
            //if head is mixture of ENSDF-field name and other names, comment is printed both at top as general comment and at bottom as footnote comments
            if((c.nHeads()<1||!topComment.isEmpty()) && c.body().indexOf("LABEL=")==-1 && !c.hasBandHead()) {
            	Comment c1=c;
            	if(!topComment.isEmpty()) {
            		c1=topComment;
            		
            		//System.out.println("topComment="+c1.body()+" c1.head="+c1.head());
            	}
            	
            	tempComs=wrapENSDFCommentTranslated(c1,HEADNOTE_WIDTH,"","",c1.isBigC());   	
            	
         
            	
            	if(tempComs.size()>0){
            		tempComs.insertElementAt("\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+tempComs.elementAt(0),0);
            		tempComs.removeElementAt(1);
            	}
            	
            	Set.addAll(tempComs);
            	lineCount+=tempComs.size();
            	linePos+=NDSConfig.HEAD_COMMENT_LINEWIDTH*tempComs.size();
            	
            	//debug
            	//System.out.println("LevelTableMaker 147: linePos="+linePos+" lineCount="+lineCount+" top com size="+tempComs.size());
            	//System.out.println("     com="+c1.body());
            	
            }
            
            //debug
            //System.out.println("LevelTableMaker 153: #1 linePos="+linePos+" lineCount="+lineCount);
            
            //comment with only head like "BE2W"--non standard ENSDF-field name--is filter out from footnote comments
            //if head is mixture of ENSDF-field name and other names, comment is printed both at top as general comment and at bottom as footnote comments
            if(c.nHeads()>=1&&c.body().indexOf("LABEL=")==-1) {//comments with head, could be footnotes or comments like "cL BE2W$...."
            	boolean print=false;
            	
                	            	
            	//process footnote comment
            	for(int k=0;k<c.headV().size();k++){//filter out comments with no corresponding column data
            		if(tc.getDefaultColumns().contains(c.headAt(k))){//column is in table
            			int n=c.flagVV().elementAt(k).size();
            			if(n==0)//comment has no flag, print it
            				print=true;
            			else{//comment has flags, if flag is also in data line, print it
							Vector ff= c.flagVV().elementAt(k);
            				for(int m=0;m<n;m++){
            					String f=(String)ff.elementAt(m); 
            					if(ens.levelFlags().contains(f))
            						print=true;          					
            				}
            			}
            			
            			if(print) break;
            		}          		
            	}
            	
            	
            	//for band comments to be printed as footnote comments instead of in band summary table above level table
            	//when Control.printBandSeq=true, a column named "Seq." is added in level table and a summary table is printed
            	//above level table
            	if(c.hasBandHead() && !NDSControl.printBandSeq){             	                	
                	for(int m=0;m<ens.nBands();m++)
                		if(ens.bandAt(m).comment().flags().equals(c.flags()))
                			print=true;
            	}
            		
            	if(!print) continue;
            	
            	
            	//NOTE: the flag in band comment "c" is renamed as upper-case letter in alphabetic order and
            	//the renamed flag is different from level band flag in corresponding bands. Be careful with this when checking
            	//if a level is in band by flag! 
                cf=new ComFlag(c.headV(),c.flagVV());
                if(cf.hasFlags())//return false only when there is no any flag in the head string             	                	
                    flags.add(cf);
                    
                
                String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"LEVEL"+cf.getIndex();
                
                //get table heading flags
                //there could be head as mixture of no-flag head and has-flag head
                if(cf.hasNoFlagHead()){
                    if(cf.hasNoFlagHead("E")) Eheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("T")) Theadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("J")) Jheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("L")) Lheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("S")) Sheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                }

                
            	tempHead="h ";//arbitrary, each symbol is one character
            	head="\\makebox[1ex]{\\ensuremath{^{\\hypertarget{"+linkID+"}{"+cf.getSymbol()+"}}}} ";

                
            	if(!c.hasBandHead() ||NDSControl.printBandSeq){
             
            		tempComs=wrapENSDFCommentTranslated(c,FOOTNOTE_WIDTH,tempHead,head,c.isBigC(),2);

            	}
                else{//Note that: flag of band comments (band sequence label) is renamed using uppercase letter in alphabetic order 

                	String s="";
                	if(c.headAt(0).equals("BAND"))
                		s="Band";
                	else
                		s="Seq.";
                	
                	s+="(";
                	if(c.isBigC())
                			s+="^";//uppercase comment, not translate the band label, eg, A to alpha, G to gamma in latex
                	s+=c.flagAt(0, 0)+"): ";

                    //System.out.println("1 body="+c.body());

                    Comment c1=new Comment();
                    
                    c1.setBigC(c.isBigC());
                    
                    c1.setBody(s+c.body());
                    
                    //System.out.println("2 body="+c.body()+" c1.body="+c1.body()+" isBigC="+c1.isBigC());
                    
                	tempComs=wrapENSDFCommentTranslated(c1,FOOTNOTE_WIDTH,tempHead,head,c.isBigC(),2);

                }

                
            	//debug
            	//for(int m=0;m<tempComs.size();m++)
            	//	System.out.println("In LevelTableMaker: tempComs["+m+"]="+tempComs.elementAt(m));
            	
                footnotes.addAll(tempComs);
                
            } 
            
        }
        T.setGenComments(Set);
        
        //one blank line before and after general comment
        if(Set.size()>0){
        	lineCount+=2;
        	linePos+=2*NDSConfig.HEAD_COMMENT_LINEWIDTH;;
        }
        
        //System.out.println("In LevelTableMaker"+Set.size());
        //System.out.println("hello"+Set.elementAt(0));
        
        Vector<String> v;
        Set=new Vector<String>();
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        
        //debug
        //System.out.println("LevelTableMaker 271: #2 linePos="+linePos+" lineCount="+lineCount);
        
        //System.out.println(ens.DSId()+"  "+ens.nXRefs()+"  "+etd.getNXRefs()+" "+ens.hasLevelXref());
        
        //add XREF table
        v=new Vector<String>();
        if(ens.fullDSId().contains("ADOPTED")&&ens.nXRefs()>0&&etd.getNXRefs()>0 && ens.hasLevelXref()){ 
        	v=makeXRefs(etd,data);//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
        	Set.addAll(v);
        	lineCount+=this.nTableRecordsLines;
        	//linePos+=this.tableRecordsHeight;
        }
        
        //debug
        //if(panel>=0) System.out.println("In LevelTableMaker line 262:XREF nTableRecordsLines="+this.nTableRecordsLines+" "+this.tableRecordsHeight);

        
        //add band summary table at top of level table
        v=new Vector<String>();
        int ntemp=this.nTableRecordsLines;
        if(ens.nBands()>0 && NDSControl.printBandSeq){
        	v=makeBandTable(ens);//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
        	Set.addAll(v);
        	count=this.nTableRecordsLines-ntemp;
        	lineCount+=count;
        }
        

        //debug
        //if(panel>=0) System.out.println("In LevelTableMaker line 277:+Sequence nTableRecordsLines="+this.nTableRecordsLines+" "+this.tableRecordsHeight);
        //if(panel>=0) System.out.println("In LevelTableMaker line 278:ens.nTableRecords="+ens.nTableRecords());
        
        v=new Vector<String>();
        ntemp=this.nTableRecordsLines;
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            
            if(t.type()=='L') {
            	v=makeTableRecords(t,tc.isFormatTables());//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
            	Set.addAll(v);
            	count=this.nTableRecordsLines-ntemp;
            	lineCount+=count;
            	//linePos+=count*Config.TABLE_RECORDS_LINEWIDTH;
            	
            }
        }
        
        //debug
        //System.out.println("In LevelTableMaker:Total nTableRecordsLines="+this.nTableRecordsLines);
        
        T.setTableRecords(Set);//set the table records in ENSDF file
        T.setNTableRecordsLines(this.nTableRecordsLines);
        T.setTableRecordsHeight(this.tableRecordsHeight);
        
        //T.setHeadLinePos(lineCount+1);//could be over-estimated, resulting less data lines to be drawn and leaving blank gap above bottom line
                                        //so precise HeadLinePos is required for proper output
        if(panel<=0){
            
            //each table leaves one-line space before (=0.7cm) and after it (=0.4cm). See LatexWriter
        	T.setRelHeadLineNo(lineCount+2);//lineNo of the heading underline, relative to the beginning of current table panel, =lineCount+(one line space before table)+(one line headline column names) 
        	
        	//linePos+=2*Config.DEFAULT_LINEWIDTH;//if counting lines separately, not using this.tableRecordHeight, slightly different     	
        	linePos+=tableRecordsHeight+NDSConfig.TABLE_TOP_MARGIN+NDSConfig.HEAD_COMMENT_LINEWIDTH+0.2;//if using this.tableRecordsHeight, more precise. 0.2 for underline
 
        	//debug
            //System.out.println("LevelTableMaker 329: #3 linePos="+linePos+" lineCount="+lineCount+" tableRecordsHeight="+tableRecordsHeight+" nTableRecordsLines="+nTableRecordsLines);
            
        	T.setRelHeadLinePos(linePos);
        }
        else{
        	T.setRelHeadLineNo(NDSConfig.DEFAULT_HEADLINE_NO);
        	T.setRelHeadLinePos(NDSConfig.DEFAULT_HEADLINE_POS);
        }
        
        Set=new Vector<String>();
        
        //table column labels
        temp="";

        temp=makeTableHead(tc,Eheadflags,Jheadflags,Theadflags,Lheadflags,Sheadflags,panel);

        Set.addElement(temp);
        T.setFirstHead(Set);
        
        //debug
        //     System.out.println("In LevelTableMaker: head="+temp);
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{\\underline{\\ensuremath{^{"+ens.nucleus().A()+"}}"+ens.nucleus().En()+" Levels (continued)}}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement(temp);
        T.setHead(Set);
        //System.out.println("In LevelTableMaker"+Set.elementAt(0));
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{Continued on next page (footnotes at end of table)}");
        T.setTail(Set);
        //System.out.println("In LevelTableMaker"+Set.elementAt(0));
        
        Set=new Vector<String>();
        //write general level comments
        for(int j=0;j<footnotes.size();j++){
            Set.addElement(footnotes.elementAt(j));
        }
        T.setLastTail(Set);     

        Set=new Vector<String>();

        float min=-100000;
        float max=1000000;

        
        //NOTE: the break point is not necessarily the level energy, since the comments could occupy 
        //multiple lines and the break could occur in the middle of comment lines, for which case,
        //the comments should be split into two parts and break point is set to be a value between 
        //current level energy and next level energy, and 
        //fromPrevPanel=true, which means the first line(s) in a new panel are the second part of 
        //comments from the last level in the previous panel.
        //fromLastLev=true means the current line(s) to write are the first part of comment lines
        //of the last level in current panel, the second part going to the next panel.
        if(panel<0){//for printing the whole table in a single panel with no beak and splitting     	
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
        boolean isLastLevelInPrevPanel=false;
        lineCount=0;
        this.nTableBodyLines=0;
        this.tableBodyHeight=0;
        float nextEF=0;
        boolean pastBreak=false;
        
        Level firstLevel=null,lastLevel=null;
        
        String Eflags="",Tflags="",Jflags="",Lflags="",Sflags="";
        
        String leftoverOfLastCom="";//for remaining part of the last comment to be printed in next panel
        
        for(int i=0;i<ens.nLevels();i++){
            row="";
            Level l=ens.levelAt(i);
            Level nl;//next level
            if(i<ens.nLevels()-1){
            	nl=ens.levelAt(i+1);
            	nextEF=nl.EF();
            }
            else{
            	nl=null;
            	nextEF=l.EF()+100;
            }
            
            comsFormatted=procComs(panel,l,tc,showSuppressed);
                    
            //debug

            /*
            if(l.EF()<10) {
                System.out.println("##########");
                for(String s:comsFormatted) System.out.println("   LevelTableMaker 408: #1 l="+l.ES()+"   comment="+s);
            }
            */
            
            //determine whether this level should be drawn in this panel
            if(l.EF()>=min) {
            	pastMin=true;
            	if(firstLevel==null)
            		firstLevel=l;
            }
            
            if(l.EF()>=max && panel>=0) {
            	if(!pastBreak) tc.setFirstLineEnergy(panel+1, l.EF()); //pastBreak check if this level has already passed the break point 
            	                                                       //for the case where breaking is in the middle of comment lines 
            	                                                       //(pastBreak set to be true there)
            	
                //System.out.println("In LevelTableMaker line 418: past break="+pastBreak+" i="+i+" l.EF()="+l.EF()+" pastMin="+pastMin+" pastMax="+pastMax+" min="+minlev+" max="+maxlev);

            	pastBreak=true;
            	pastMax=true;
            	
            	if(lastLevel==null && i>0)
            		lastLevel=ens.levelAt(i-1);
            }else if(l.EF()<max && (nextEF>=max||i==ens.nLevels()-1)) {
                if(lastLevel==null) {
                    lastLevel=l;
                }
            }

            
            //debug
            //if(panel==0) 
            //System.out.println("In LevelTableMaker line 429: i="+i+" l.EF()="+l.EF()+" pastMin="+pastMin+" pastMax="+pastMax+" min="+minlev+" max="+maxlev);
            
            isLastLevelInPrevPanel=false;
            if(l.EF()<min && nextEF>min){//break in the middle of comments of last level in previous panel, so print the remaining comments in the new panel, only when autoAdjust=true
            	pastMin=true;
            	isLastLevelInPrevPanel=true;
            	
            	//debug
            	//if(panel==1){
            	//System.out.println("In LevelTableMaker: **comsFormatted.size: "+comsFormatted.size());
            	//System.out.println("                      comsFormatted[0]="+comsFormatted.elementAt(0));
            	//System.out.println("                      comsFormatted[last]="+comsFormatted.lastElement());
            	//}
            	
            	comsFormatted=procComsFormattedPartial(panel,l,tc,showSuppressed,tc.getLastLine(panel-1),tc.getSecondLastLine(panel-1));//get level comments continued in new panel, so skip level comments before lastComLineInPrevPanel        
                
            	
            	/*
            	//debug
            	//if(l.EF()>7086 && l.EF()<7225) {
            	if(panel==5){
            	System.out.println("LevelTableMaker 490 **comsFormattedPartial.size: "+comsFormatted.size()+" lastLine="+tc.getLastLine(panel-1));
            	System.out.println("                      lastComLineInPrevPanel="+tc.getLastLine(panel-1));
            	System.out.println("                      firstlineCom="+tc.getFirstLine(panel));
            	System.out.println("                      panel="+panel+" l.EF()="+l.EF()+" firstComE="+tc.getFirstLineEnergy(panel)+" pastMin="+pastMin+" pastMax="+pastMax+" minlev="+min+" maxlev="+max);
            	for(String s:comsFormatted) System.out.println("                    comsFormattedPartial: comsFormatted="+s);
            	}
            	*/
            	
            }
                        
            if(pastMax||!pastMin) continue;
            
            /*
            //debug
            //if(panel>=0) System.out.println("In LevelTableMaker line 425: i="+i+" l.EF()="+l.EF()+" pastMin="+pastMin+" pastMax="+pastMax+" min="+minlev+" max="+maxlev);
        	//debug
            if(l.EF()>1593 && l.EF()<1648) {
            //if(panel==0){
        	System.out.println(" LevelTableMaker 506**comsFormattedPartial.size: "+comsFormatted.size());
        	System.out.println("                      lastComLineInPrevPanel="+tc.getLastLine(panel-1)+" isLastLevelInPrevPanel="+isLastLevelInPrevPanel);
        	System.out.println("                      firstlineCom="+tc.getFirstLine(panel));
        	System.out.println("                      panel="+panel+" l.EF()="+l.EF()+" firstComE="+tc.getFirstLineEnergy(panel)+" pastMin="+pastMin+" pastMax="+pastMax+" minlev="+min+" maxlev="+max);
        	for(String s:comsFormatted) 
        		System.out.println("                    comsFormattedPartial: comsFormatted="+s);
        	}
            */
            
            //if the control file has F records, we need to only print levels if they have values for this records
            temp="";
            if(tc.getFcolumns()!=null){
                boolean print=false;
                for(int j=0;j<tc.getFcolumns().size();j++){
                    temp=(String)tc.getFcolumns().elementAt(j);
                    if(temp.equals("E")&&l.ES().length()>0) print=true;
                    if(temp.equals("J")&&l.JPiS().length()>0) print=true;
                    if(temp.equals("XREF")&&l.newXREFS().length()>0) print=true;
                    if(temp.equals("T")&&l.halflife().length()>0) print=true;
                    if(temp.equals("L")&&l.lS().length()>0) print=true;
                    if(temp.equals("S")&&l.sS().length()>0) print=true;
                }
                if(!print){
                    
                    continue;
                }
            }
            //get level flags
            Eflags="";Tflags=""; Jflags="";Sflags="";Lflags="";           
            
            boolean inBand=l.belongs2Band();
            
            for(int j=0;j<flags.size();j++){
                cf=(ComFlag)flags.elementAt(j);
                
                
                String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"LEVEL"+cf.getIndex();
                
                if(cf.hasFlagAny(l.flag())){
                    if(cf.hasFlagAny("T",l.flag())) Tflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}"; 
                    if(cf.hasFlagAny("J",l.flag())) Jflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("E",l.flag())) Eflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("L",l.flag())) Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("S",l.flag())) Sflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                }
                
               
                //band flag is printed for level when a separate band sequence column is not printed
                //NOTE: flag in band comment is renamed as upper-case letter in alphabetic order,
                //For band, ComFlag cf is created from flags in band comments, while l.flag() has
                //the original band flags in ENSDF file. 
                if(inBand && cf.hasBandHead() &&!NDSControl.printBandSeq){
                	String headS="BAND";
                    if(cf.hasHead("SEQ"))
                    	headS="SEQ";
                    
                	if(cf.hasFlagAny(headS,ens.bandAt(l.bandNum()).comment().flagAt(0,0)))
                		Eflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                }
               
            }

            	
            //debug
            //for(String s:comsFormatted) System.out.println("LevelTableMaker 530: #2 l="+l.ES()+"   comment="+s);
            
			row=makeLevelEntry(l,columns,ens,comsFormatted,Eflags,Jflags,Tflags,Lflags,Sflags);

            //debug
            //if(panel==5) System.out.println(" "+columns.size()+"  In LevelTableMaker 582: isLastLevelInPrevPanel="+isLastLevelInPrevPanel+" row="+row);
                        
            if(!isLastLevelInPrevPanel){
            	Set.addElement(row);
                lineCount++;          
                this.nTableBodyLines++;
                
                float scale0=1.0f,scale1=1.0f,scale3=1.0f,extra=0.0f;
                if(Str.containSpecialSupScript(row)){     
                	scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*1.10f;
                	scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;      
                	scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;  
                	//if(panel==7) 
                	//System.out.println("########### In LevelTableMaker: special script row="+row);
                	
                }else if(Str.containSupScript(row, "0,1,2,3,4,5,6,7,8,9")){
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*0.2f;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;      
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;    
                }else if(Str.containSupScript(row)){
                    if(Str.containJPIScriptOnly(row)){
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        
                        //System.out.println("########### In LevelTableMaker: special script row="+row);
                    }else if(Str.containGivenSupScriptOnly(row,"x")){
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                    	
                    	//System.out.println("########### In LevelTableMaker: special script row="+row);
                	}else{
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                	}
                	//if(panel==7) System.out.println("aaaaaaaaaaa In LevelTableMaker: special script row="+row);
                	
                }else if(Str.containSubScript(row)) {
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                }

                //debug
                //float EL=l.EF();
                //if(EL>1025 && EL<1107) 
                //System.out.println("########### In LevelTableMaker 650: scale0="+scale0+" scale1="+scale1+" scale3="+scale3
                //		+" row="+row+"\n "
                //		+ "Str.containSupScript="+Str.containSupScript(row)+" contain special="+Str.containSpecialSupScript(row)
                //		+" contain JPI only="+Str.containJPIScriptOnly(row)+" Str.containSubScript(r)="+Str.containSubScript(row));
                
                //System.out.println("########### In LevelTableMaker: special script row="+Str.containSpecialScript(row)+"  "+Str.containScript(row)+row);
                
                if(comsFormatted.size()==0)
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH0*scale0;
                else if(comsFormatted.size()==1)
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH1*scale1;
                else 
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale3;

            	if(l.EF()>=0){
                    levelEnergies.add(String.valueOf(l.EF()));
                    dataEnergies.add(String.valueOf(l.EF()));
            	}
            	else{
            		
                    levelEnergies.add(String.valueOf(nextEF));
                    dataEnergies.add(String.valueOf(nextEF));
            	}
                
            }
      
            String tempS="";
            if(tc.getPanelColumns(panel).contains("COMM")){
            	float e1=0,e2,de,ef;
            	int start=0;
            	String es="";
            	
            	if(isLastLevelInPrevPanel)
            		e1=min;
            	
            	if(l.EF()>e1)
            		e1=l.EF();
            	
            	if(nl!=null)
            		e2=nl.EF();
            	else
            		e2=e1+100;
            	
            	de=e2-e1;
            	if(comsFormatted.size()>0)
            	    de=(e2-e1)/comsFormatted.size();
            	
            	if(isLastLevelInPrevPanel)
            		start=0;
            	else
            		start=1;
          	
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
                    
                    /*
                    //debug
                    if(panel==5){
                       System.out.println("BBBBB In LevelTableMaker 776: firstComLineE="+tc.getFirstLineEnergy(panel)+" l.EF()="+l.EF()+" maxlev="+max+" minlev"+min+" ef="+ef+" row="+row);
                       System.out.println("                          panel="+panel+" k="+k+" e1="+e1+" isLastLevelInPrevPanel="+isLastLevelInPrevPanel+" comsFormatted.size()="+comsFormatted.size());
                       System.out.println("                          lineCount="+lineCount);
                    }
                    */
                    
                    Set.addElement(row);
                    
        
                    
                    float scale=1.0f;
                    
                    if(Str.containSpecialSupScript(row))
                    	scale=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.9f;        
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
                    
                    //these are fake level and data energies assigned for comment lines that are not in the same line with level and data record
                    levelEnergies.add(es);
                    dataEnergies.add(es);
                    
                }

                
            }

                    
            width=getEntryLength(l,tc.getPanelColumns(panel));
                        
            if(width>maxDataWidth) 
            	maxDataWidth=width;
                       
            this.nCommentLines=0;//incremented in findMaxCommentWidth()
            width=findMaxCommentWidth(panel,l,etd,showSuppressed);
            if(width>this.maxCommentWidth)
            	this.maxCommentWidth=width;   
            
            //if(this.nCommentLines>0)
            //	this.nTableBodyLines+=this.nCommentLines-1;//the first comment line is in the data line which is already counted 
            
            //debug
            //System.out.println("In LevelTableMaker: i="+i+" nTableBodyLines="+this.nTableBodyLines+" after l.EF()="+l.EF());
            
        }//levels
                
        T.setBody(Set);

        //if(panel>4)
        //System.out.println("LevelTableMaker 846: ########## panel no="+panel+"  body="+T.getBody());
        
        //if(panel==5)
        //System.out.println("########## body="+T.getBody());
        
        //if(panel<0) panel=0;
      
        if(Set.size()>0 && panel>=0){
            tc.setFirstLine(panel,Set.firstElement());
            tc.setFirstLineEnergy(panel, Str.parseFloatEnergy(dataEnergies.firstElement())); 
            
            tc.setLastLine(panel,Set.lastElement());
            tc.setLastLineEnergy(panel, Str.parseFloatEnergy(dataEnergies.lastElement()));    
            
            if(Set.size()>1)
            	tc.setSecondLastLine(panel, Set.elementAt(Set.size()-2));
            
            //debug
            //System.out.println("In LevelTableMaker: line 756: this panel ");
            //System.out.println("           first line="+Set.firstElement());
            //System.out.println("           enery="+dataEnergies.firstElement());
            //System.out.println("           last  line="+Set.firstElement());
            //System.out.println("                enery="+dataEnergies.lastElement());
            
            tc.setLeftoverOfLastCom(panel,leftoverOfLastCom);
        }
   
        try{
        	//check if the first line of next panel is the same as the last line of current panel just set here
        	float nextLineE=tc.getFirstLineEnergy(panel+1);
        	if(nextLineE==tc.getLastLineEnergy(panel)){
        		tc.setFirstLine(panel+1, null);
        		tc.setFirstLineEnergy(panel+1, nextLineE+Math.min(0.1f,0.001f*nextLineE));
        	}
        	
        	//bueg
            //System.out.println("In LevelTableMaker: line 772: next panel ");
            //System.out.println("           first line energy="+tc.getFirstLineEnergy(panel+1));
            
            
        }catch(Exception e){}
        
        T.setNTableBodyLines(this.nTableBodyLines);
        T.setTableBodyHeight(this.tableBodyHeight);
        T.setTotalDataLines(lineCount);
        
        T.setDataLineLevels(levelEnergies);
        T.setDataLineEnergies(dataEnergies);
        
        Set=new Vector<String>();

        //maxDataWidth=maxDataWidth*Config.CHAR2CM;//this is inaccurate since there could be different empty columns in different lines

        //here if still firstLevel=null, it means no level is in the current panel and all are comments from last level in the previous panel
        //see condition at line 421 for how firstLevel is assigned.
        
        HashMap<String,String> maxColStrings=findMaxColumnStrings(tc.getPanelColumns(panel),firstLevel,lastLevel,flags);
        HashMap<String,String> headStrings=colHeadStrings(tc,Eheadflags,Jheadflags,Theadflags,Lheadflags,Sheadflags,panel);
        
        
        maxDataWidth=findMaxDataWidth(maxColStrings,headStrings);
        
        maxCommentWidth=this.maxCommentWidth;
        
        dataHeadWidth=findDataHeadWidth(maxColStrings,headStrings);
        
        /*
        //debug
        if(panel==8) {
            System.out.println("In LevelTableMaker line 761: panel="+panel+" datawidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" headwidth="+width+
        		"  tc.commentColumnWdith="+tc.getCommentColumnWidth(panel));
  
            for(String key:maxColStrings.keySet())
                System.out.println("    col="+key+"  name="+maxColStrings.get(key));
            
            for(String key:headStrings.keySet())
                System.out.println("    head="+key+"  name="+headStrings.get(key));
            
            for(String s:tc.getDefaultFixedColumns())
                System.out.println("   fixedColumn="+s);
            
            System.out.println("   firstLevel="+firstLevel.ES());
            System.out.println("   lastLevel="+lastLevel.ES()); 
        }
        */
        
        
        /*
        maxDataWidth=findMaxEntryWidth(tc.getPanelColumns(panel),firstLevel,lastLevel);
            
        width=colHeadWidth(tc,Eflags,Jflags,Tflags,Lflags,Sflags,panel);
        if(maxCommentWidth>0)
        	width=width-"Comments".length()*Config.CHAR2CM;
       
        //debug
        //System.out.println("In LevelTableMaker: datawidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" headwidth="+width);
        
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
    //I created a separate function for creating table in manual mode because I don't
    //want to mess up the code for automatic mode by including code for manual settings
    //in it and also because it will save time since the program doesn't need to
    //check if it is automatic or manual mode all the time (many many thousands of
    //times) and it needs to be checked only once when makeTablePanel is called.
    @SuppressWarnings("rawtypes")
	public TablePanel makeTablePanel_manual(int panel)throws Exception{
        /*Levels table*/
        ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        Vector<String> Set=new Vector<String>();
        String row="";
        String temp="";
        this.maxCommentWidth=0;
        
        boolean showSuppressed=etd.isShowSuppressed();
        
		//debug					
		//System.out.println("A*In LevelTableMaker tc size"+tc.getBreaks().size()+" panelNo="+panel);
		//if(panel>0)
		//System.out.println("A*In LevelTableMaker tc size"+tc.getBreaks().elementAt(panel-1).panelColumns);
		
        //table header
        int nColumns;
        Vector<String> columns;
        if(panel<0){
        	System.out.println("In manual mode for LevelTableMaker, panelNo<0 is not allowed! Please check the code!");
        	System.exit(0);
        }
               
        columns=tc.getPanelColumns(panel);

        
		//debug	
		//System.out.println("B*In LevelTableMaker tc size"+tc.getBreaks().size()+" panelNo="+panel+columns);
		
        nColumns=columns.size();
        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getDefaultColumns().contains("COMM"))T.addComLoc(tc.getDefaultColumns().indexOf("COMM"));

        //prepare general level comments, footnotes (the footnotes dont get written till the end of the table)
        //then write some comments at the start
        Vector<String> footnotes=new Vector<String>();

        //add non-band comments
        String Eflags="",Tflags="",Jflags="",Lflags="",Sflags="";
        Comment c;
        ComFlag cf;
        Vector<ComFlag> flags=new Vector<ComFlag>();
        ComFlag.setNumFlags(0);
        for(int j=0;j<tc.getComments().size();j++){
            c=(Comment)tc.getComments().elementAt(j);
            
        	//process general comment with head, like "cL BE2W$...."
        	Comment topComment=new Comment();
        	topComment=tc.extractTopComment(c);     
        	
            //comments without head, must be general comments at top of table
            //comments with head, if containing head like "BE2W", is also at top of table
            //if head is mixture of ENSDF-field name and other names, comment is printed both at top as general comment and at bottom as footnote comments
            if((c.nHeads()<1||!topComment.isEmpty()) && c.body().indexOf("LABEL=")==-1) {
            	Comment c1=c;
            	if(!topComment.isEmpty())
            		c1=topComment;

            	Set.addElement("\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+c1.getRegTranslated()); 
            	
            }
            

            //comment with only head like "BE2W"--non standard ENSDF-field name--is filter out from footnote comments
            //if head is mixture of ENSDF-field name and other names, comment is printed both at top as general comment and at bottom as footnote comments
            if(c.nHeads()>=1&&c.body().indexOf("LABEL=")==-1) {//comments with head, could be footnotes or comments like "cL BE2W$...."
            	boolean print=false;
            	for(int k=0;k<c.headV().size();k++){//filter out comments with no corresponding column data
            		if(tc.getDefaultColumns().contains(c.headAt(k))){//column is in table
            			int n=c.flagVV().elementAt(k).size();
            			if(n==0)//comment has no flag, print it
            				print=true;
            			else{//comment has flags, if flag is also in data line, print it
							Vector ff= c.flagVV().elementAt(k);
            				for(int m=0;m<n;m++){
            					String f=(String)ff.elementAt(m); 
            					if(ens.levelFlags().contains(f))
            						print=true;          					
            				}
            			}
            			
            			if(print) break;
            		}          		
            	}
            	
            	//for band comments to be printed as footnote comments instead of in band summary table above level table
            	//when Control.printBandSeq=true, a column named "Seq." is added in level table and a summary table is printed
            	//above level table
            	if(c.hasBandHead() && !NDSControl.printBandSeq){             	                	
                	for(int m=0;m<ens.nBands();m++)
                		if(ens.bandAt(m).comment().flags().equals(c.flags()))
                			print=true;
            	}
            	
            	if(!print) continue;
            	            	
                cf=new ComFlag(c.headV(),c.flagVV());
                if(cf.hasFlags())//return ture only when there is no any flag in the head string             	                	
                    flags.add(cf);
                    
                String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"LEVEL"+cf.getIndex();
                
                //get table heading flags
                //there could be head as mixture of no-flag head and has-flag head
                if(cf.hasNoFlagHead()){
                    if(cf.hasNoFlagHead("E")) Eflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("T")) Tflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("J")) Jflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("L")) Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("S")) Sflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                }
                
            	if(!c.hasBandHead() || NDSControl.printBandSeq)
            		footnotes.add("\\makebox[1ex]{\\ensuremath{^{\\hypertarget{"+linkID+"}{"+cf.getSymbol()+"}}}} "+c.getRegTranslated());      
                else{//Note that: flag of band comments (band sequence label) is renamed using uppercase letter in alphabetic order 
                	String s;
                	
                	if(c.headAt(0).equals("BAND"))
                		s="Band";
                	else 
                		s="Seq.";

                	s+="("+c.flagAt(0, 0)+"): ";
                	footnotes.add("\\makebox[1ex]{\\ensuremath{^{\\hypertarget{"+linkID+"}{"+cf.getSymbol()+"}}}} "+s+c.getRegTranslated());      
                }               
            }            
        }
        T.setGenComments(Set);

        
        Vector<String> v;
        Set=new Vector<String>();
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        //add XREF table
        if(ens.fullDSId().contains("ADOPTED")&&ens.nXRefs()>0&&etd.getNXRefs()>0){ 
        	v=makeXRefs(etd,data);//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
        	Set.addAll(v);
        }
        
        //debug
        //System.out.println("In LevelTableMaker:XREF nTableRecordsLines="+this.nTableRecordsLines+" "+this.tableRecordsHeight);


        //add band summary table at top of level table
        v=new Vector<String>();

        if(ens.nBands()>0 && NDSControl.printBandSeq){
        	v=makeBandTable(ens);//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
        	Set.addAll(v);
        }
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);          
            if(t.type()=='L') {
            	v=makeTableRecords(t,tc.isFormatTables());//this.nTableRecordsLines and this.tableRecordHeight are incremented in this call
            	Set.addAll(v);            	
            }
        }
         
        T.setTableRecords(Set);//set the table records in ENSDF file
        
        Set=new Vector<String>();
        
        //table column labels
        temp="";


        temp=makeTableHead(tc,Eflags,Jflags,Tflags,Lflags,Sflags,panel);
        
        Set.addElement(temp);
        T.setFirstHead(Set);
        
        //debug
        //     System.out.println("In LevelTableMaker: head="+temp);
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{\\underline{\\ensuremath{^{"+ens.nucleus().A()+"}}"+ens.nucleus().En()+" Levels (continued)}}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement(temp);
        T.setHead(Set);
        //System.out.println("In LevelTableMaker"+Set.elementAt(0));
        
        Set=new Vector<String>();
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{Continued on next page (footnotes at end of table)}");
        T.setTail(Set);
        //System.out.println("In LevelTableMaker"+Set.elementAt(0));
        
        Set=new Vector<String>();
        //write general level comments
        for(int j=0;j<footnotes.size();j++){
            Set.addElement(footnotes.elementAt(j));
        }
        T.setLastTail(Set);     

        Set=new Vector<String>();

        float minlev=-100000;
        float maxlev=1000000;
       
    	if(panel>0&&panel<=tc.getBreakPoints().size())
    		minlev=Float.parseFloat(tc.getBreakPoints().elementAt(panel-1));
    	if(panel>=0 && panel<tc.getBreakPoints().size())
    		maxlev=Float.valueOf(tc.getBreakPoints().elementAt(panel));
              
        //print each row of the table
        Vector<String> comsFormatted;
        
        boolean pastMax=false;
        boolean pastMin=false;
        
        for(int i=0;i<ens.nLevels();i++){
            row="";
            Level l=ens.levelAt(i);
            comsFormatted=procComs(panel,l,tc,showSuppressed);
                    
            //determine whether this level should be drawn in this panel
            if(l.EF()>=minlev) {
            	pastMin=true;
            }
            if(l.EF()>=maxlev) {
            	pastMax=true;
            }
                        
            if(pastMax||!pastMin) continue;
                     
            //if the control file has F records, we need to only print levels if they have values for this records
            temp="";
            if(tc.getFcolumns()!=null){
                boolean print=false;
                for(int j=0;j<tc.getFcolumns().size();j++){
                    temp=(String)tc.getFcolumns().elementAt(j);
                    if(temp.equals("E")&&l.ES().length()>0) print=true;
                    if(temp.equals("J")&&l.JPiS().length()>0) print=true;
                    if(temp.equals("XREF")&&l.newXREFS().length()>0) print=true;
                    if(temp.equals("T")&&l.halflife().length()>0) print=true;
                    if(temp.equals("L")&&l.lS().length()>0) print=true;
                    if(temp.equals("S")&&l.sS().length()>0) print=true;
                }
                if(!print){
                    
                    continue;
                }
            }
            
            //get level flags
            Eflags="";Tflags=""; Jflags="";Sflags="";Lflags="";
            boolean inBand=l.belongs2Band();
            
            for(int j=0;j<flags.size();j++){
                cf=(ComFlag)flags.elementAt(j);
                String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"LEVEL"+cf.getIndex();
                
                if(cf.hasFlagAny(l.flag())){
                    if(cf.hasFlagAny("T",l.flag())) Tflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("J",l.flag())) Jflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("E",l.flag())) Eflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("L",l.flag())) Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("S",l.flag())) Sflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                }
                
                //band flag is printed for level when a separate band sequence column is not printed
                //NOTE: flag in band comment is renamed as upper-case letter in alphabetic order,
                //For band, ComFlag cf is created from flags in band comments, while l.flag() has
                //the original band flags in ENSDF file. 
                if(inBand && cf.hasHead("BAND")&&!NDSControl.printBandSeq)
                	if(cf.hasFlagAny("BAND",ens.bandAt(l.bandNum()).comment().flagAt(0,0)))
                		Eflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
            }
           
			row=makeLevelEntry(l,columns,ens,comsFormatted,Eflags,Jflags,Tflags,Lflags,Sflags);
                 
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
        }//levels
        
        T.setBody(Set);
        
        Set=new Vector<String>();
        
        return T;
    }
    
    /** make a line in level table from level field data*/
    private String makeLevelEntry(Level l, Vector<String> columns, ENSDF ens, Vector<String> comsFormatted, String Eflags, String Jflags, String Tflags, String Lflags, String Sflags) throws Exception{
    	String row="";
    	String colName,temp="";
    	String flagStr="";
    	String flagsLatex="";
    	int nColumns=columns.size();
        //String fontsize_str="\\scriptsize";
        String fontsize_str="";
        
    	boolean print=false;
    	
        for(int j=0;j<nColumns;j++){
            colName=(String) columns.elementAt(j);
        	String S="";      	
        	print=false;
        	flagsLatex="";
        	
            //debug
            //System.out.println("LevelTableMaker 1172: l="+l.ES()+"   colName="+colName);
            
        	//NOTE that: "BND" col is added in EnsdfTableData only when Control.printBandSeq=true
            if(colName.equals("BND")){
                try{
                    if(l.belongs2Band()) row+=(ens.bandAt(l.bandNum()).comment().flagAt(0,0));
                }catch(Exception e){
                    System.out.println(ens.fullDSId()+" "+l.ES());
                    System.out.println("ERROR: One of the levels is not band labeled: maybe because a band has multiple flags associated with it");
                }
            }

            if((colName.equals("E")||colName.equals("DE")) && l.ES().length()>0){
            	temp=l.ES();
            	
            	S=LatexWriter.printNumber(temp,l.DES());
            	
            	if(l.q().equals("?"))//print question mark following level energy
            		S=addQuestionMark(S);
            	
                S=addFlagsLatex(S,Eflags);  
                flagsLatex=addFlagsLatex("",Eflags);

                if(l.q().equals("S"))//"S" marked the level energy is to be put in bracket
                	S="("+S+")";

            	
                print=true;
            }
            else if(colName.equals("T")||colName.equals("DT")){
            	S=LatexWriter.printNumber(l.T12S(),l.DT12S(),Translator.halfLifeUnits(l.T12Unit()));
                S=addFlagsLatex(S,Tflags);     
                flagsLatex=addFlagsLatex("",Tflags);
                if(l.halflife().length()>0 || Tflags.length()>0) 
                	print=true;
            }
            else if(colName.equals("S")||colName.equals("DS")){
            	S=LatexWriter.printNumber(l.sS(),l.dsS());
            	S=addFlagsLatex(S,Sflags);         
            	flagsLatex=addFlagsLatex("",Sflags);
                if(l.sS().length()>0 || Sflags.length()>0) 
                	print=true;
            }
            
            
            //print string
            temp="";
            int n=-1;
            if(columns.contains("D"+colName)){
                temp="\\multicolumn{1}{r@{}}{";
                if(S.contains(".") && (!S.contains("times10^")||S.contains("-")||colName.equals("T"))){//exclude case like 1.53X10^3 12 exception T1/2, 1.43X10^-3
                    temp+=S.substring(0,S.indexOf("."));
                }else if(!S.contains(".") && S.contains("times10^") && S.contains("-")){//e.g, 1072X10^-6 16, aligned by X
                    temp+=S.substring(0,S.indexOf("\\ensuremath{\\times10^"));
                }else if(!S.contains(".") && (n=S.indexOf("+"))>0 && n<S.length()-1 && Character.isLetter(S.charAt(n+1))){//e.g, 1234+X
                    temp+=S.substring(0,S.indexOf("+"));
                }else if(flagsLatex.length()>0){
                    temp+=S.substring(0,S.indexOf(flagsLatex));
                }else if(S.contains(" ")){
                    temp+=S.substring(0,S.indexOf(" "));
                }else if(S.toUpperCase().contains("STABLE")){//make a little alignment for T1/2="stable"
                	temp+=S.substring(0,S.toUpperCase().indexOf("BLE"));
                }else{
                    temp+=S;
                }
        	
                
                temp+="}";
            }else if(colName.charAt(0)=='D' && columns.contains(colName.substring(1))){
                temp="\\multicolumn{1}{@{";
                if(S.contains(".") && (!S.contains("times10^")||S.contains("-")||colName.equals("DT"))){
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
                }else if(S.toUpperCase().contains("STABLE")){
                	temp+="}l}{"+S.substring(S.toUpperCase().indexOf("BLE"));
                }else{
                    temp+="}l}{";
                }
                
                
                temp+="}";
            }else if(colName.equals("J")) {
            	flagStr="";
            	if(Jflags.length()>0) 
            		flagStr="\\ensuremath{^{"+Jflags+"}}";
                if(l.JPiS().length()>0 || Jflags.length()>0){
                	temp=("\\multicolumn{1}{l}{"+Translator.spin(l.JPiS())+flagStr+"}");
                	print=true;
                }
            }else if(colName.equals("L")){
            	flagStr="";
            	if(Lflags.length()>0) 
            		flagStr="$^{"+Lflags+"}$";
                if(l.lS().length()>0 || Lflags.length()>0) {
                	temp=("\\multicolumn{1}{l}{"+Translator.plainSpin(l.lS())+flagStr+"}");
                	print=true;
                }
            }else if(colName.equals("XREF") && l.newXREFSFormatted().length()>0){
                String tempS="";
                temp="\\multicolumn{1}{l}{";
                int nXrefs=l.newXREFSFormatted().length();
                if(nXrefs>etd.getNXRefs()) nXrefs=etd.getNXRefs();
                for(int k=0;k<nXrefs;k++){//print the XREFS, but cut them off at a given letter (15=O,25=Y)
                    if(l.newXREFSFormatted().charAt(k)=='.') 
                    	tempS+="\\ ";
                    else{ 
                    	String tag=l.newXREFSFormatted().charAt(k)+"";
                    	String linkID="";
                    	if(XREFLinks.containsKey(tag.toUpperCase()))
                    		linkID=XREFLinks.get(tag.toUpperCase());

                    	
                        if(linkID.length()>0){
                        	tag="\\hyperlink{"+linkID+"}{"+tag+"}";
                        }else{//linked dataset is not present then link to the pdf file of that dataset in the same folder
                        	XRef xR=EnsdfUtil.findXRef(ens.XRefV(),tag.toUpperCase());
                    		String href="";
                    		if(xR!=null){ 
                    			href=EnsdfUtil.DSID2Filename(xR.DSId0())+".pdf";
                    			href=EnsdfUtil.makePDFLink(href);
                    			
                    			//debug
                    			//System.out.println("In LevelTablemaker: xref DSID="+xR.DSId()+" href="+href);
                    		}
                    		if(href.length()>0)
                    			tag="\\href{"+href+"}{"+tag+"}";                   			
                        }
                        	
                    	tempS+=tag;
                    }
                }
                //temp+=("\\texttt{"+fontsize_str+"{\\emph{"+temp+"}}} ");
                temp+=("\\texttt{"+fontsize_str+tempS+"} ");
                temp+="}";
                print=true;
            }else if(colName.equals("COMM")){
            	if(comsFormatted.size()>0){ 
            		print=true;
                	temp=comsFormatted.elementAt(0);
                	
                    //debug
                    //for(String s:comsFormatted) System.out.println("LevelTableMaker 408: #1 l="+l.ES()+"   comment="+s);
            	}
                //else if(j==nColumns-1)
                //	row+=("\\parbox[c]["+Config.TABLE_BODY_LINEWIDTH3+"cm]{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright \\vspace{0.1cm}}");//to keep the same line-space for data-entry lines as that between comment lines 
            }
                
            if(print)
            	row+=temp;
            
            if(j<nColumns-1) row+="&";
        }//columns
        
        //debug
        //for(String s:comsFormatted) System.out.println("LevelTableMaker 1337: #1 l="+l.ES()+"   comment="+s);
        
        return row;
    }
    
    /** Returns the correct heading for this table
     * A complicated enough process to deserve its own function 
     * @throws java.lang.Exception */     
    private String makeTableHead(TableControl tc,String Eflags,String Jflags,String Tflags,String Lflags,String Sflags,int panel) throws java.lang.Exception{
        String out="";
        String temp;
        String colName;
        int nColumns=tc.getPanelColumns(panel).size();
        
        String Jhead="J$^{\\pi}$";
        //String Jhead="J$\\pi$";
        String Thead="T$_{1/2}$";
        String Lhead="L";
        String Shead="S";
        
        //see if any of the columns need to be relabelled
        int ix;
        Comment c;
        for(int i=0;i<tc.getComments().size();i++){
            c=tc.getComments().elementAt(i);
            temp=c.body();
            ix=temp.indexOf("LABEL=");
            if(ix>=0) 
            	temp=temp.substring(ix+6);
            else 
            	continue;
            
            if(c.isBigC())
            	temp=Translator.process(temp);
            else
            	temp=Translator.translate(temp);
            
            if(c.head().equals("J")) Jhead=temp.trim();
            else if(c.head().equals("T")) Thead=temp.trim();
            else if(c.head().equals("L")) Lhead=temp.trim();
            else if(c.head().equals("S")) Shead=temp.trim();
            
            //debug
            //System.out.println("In LevelTableMaker: Shead="+Shead);
        }
        
        for(int i=0;i<nColumns;i++){
            colName=(String)tc.getPanelColumns(panel).elementAt(i);
            
            if(colName.equals("BND")) out+="Seq.";
            else if(colName.equals("E")){
                out+="\\multicolumn{2}{c}{E(level)$^{"+Eflags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("J")) out+=Jhead+"$^{"+Jflags+"}$";
            else if(colName.equals("T")) {            	
                out+="\\multicolumn{2}{c}{"+Thead+"$^{"+Tflags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("L")) out+=Lhead+"$^{"+Lflags+"}$";
            else if(colName.equals("S")) {            	
                out+="\\multicolumn{2}{c}{"+Shead+"$^{"+Sflags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("XREF")) out+="XREF";
            else if(colName.equals("COMM")) out+="Comments";
            
            if(i<nColumns-1) out+="&";
        }    
        return out;
    }

    @SuppressWarnings("unused")
	private float colHeadWidth(TableControl tc,String Eflags,String Jflags,String Tflags,String Lflags,String Sflags,int panel){
    	float headWidth=0;
    	HashMap<String,String> colStrings=colHeadStrings( tc,Eflags,Jflags,Tflags,Lflags,Sflags,panel);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		headWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return headWidth;
    	
    }
    
    private HashMap<String,String> colHeadStrings(TableControl tc,String Eflags,String Jflags,String Tflags,String Lflags,String Sflags,int panel){
        String out="";
        String temp;
        String colName;
        int nColumns=tc.getPanelColumns(panel).size();
        
        HashMap<String,String> colStrings=new HashMap<String,String>();
        
        String Jhead="Jp";
        String Thead="T1/2";
        String Lhead="L";
        String Shead="S";
               
        if(Eflags.length()>0) Eflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(Eflags));
        if(Jflags.length()>0) Jflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(Jflags));
        if(Tflags.length()>0) Tflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(Tflags));
        if(Lflags.length()>0) Lflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(Lflags));
        if(Sflags.length()>0) Sflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(Sflags));
        

        //see if any of the columns need to be relabelled
        int ix;
        Comment c;
        for(int i=0;i<tc.getComments().size();i++){
            c=tc.getComments().elementAt(i);
            temp=c.body();
            ix=temp.indexOf("LABEL=");
            if(ix>=0) temp=temp.substring(ix+6);
            else continue;

            if(c.head().equals("J")) Jhead=temp.trim();
            else if(c.head().equals("T")) Thead=temp.trim();
            else if(c.head().equals("L")) Lhead=temp.trim();
            else if(c.head().equals("S")) Shead=temp.trim();
        }
        for(int i=0;i<nColumns;i++){
        	out="";
            colName=(String)tc.getPanelColumns(panel).elementAt(i);
            if(colName.equals("BND")) out="Seq.";
            if(colName.equals("E")) out="E(level)"+Eflags;
            if(colName.equals("J")) out=Jhead+Jflags;
            if(colName.equals("T")) out=Thead+Tflags;
            if(colName.equals("L")) out=Lhead+Lflags;
            if(colName.equals("S")) out=Shead+Sflags;
            if(colName.equals("XREF")) out="XREF";
            if(colName.equals("COMM")) out="Comments";

            colStrings.put(colName, out);
            
            //debug
            //System.out.println("In LevelTableMaker: colHeadString(): colName="+colName+" out="+out+" length="+out.length());
        }    
        

        
        return colStrings;
    }

    
	public String commentHeadMaker(Comment com,String option) throws java.lang.Exception{
    	String head="",tempHead="";
    
		int n=0;;
    	
        String headName;
        
        String Jhead="J\\ensuremath{^{\\pi}}";
        //String Jhead="J\\ensuremath{\\pi}";
        String Thead="T\\ensuremath{_{1/2}}";
        String Lhead="L";
        String Shead="S";
        
        String tempJhead="Jpi",tempThead="T1/2",tempLhead="L",tempShead="S";
        
    	if(com.head().trim().length()==0 && com.nHeads()==0)
    		return "";
    	
        HashMap<String,String> labelMap=tc.getRelabelMap();
        HashMap<String,String> translatedLabelMap=tc.getTranslatedLabelMap();
    	
    	String label="",translatedLabel="";
        for(int k=0;k<com.headV().size();k++){
        	headName=com.headAt(k);
          
        	//debug
        	//System.out.println("k="+k+" Headname="+headName);
        	
        	if(head.length()>0){
        		head+=",";
        		tempHead+=",";
        		n+=1;
        	}
        	
        	label=labelMap.get(headName);
        	translatedLabel=translatedLabelMap.get(headName);
        	
        	//if(label!=null && label.length()>0)
        	//	System.out.println("LevelTableMaker 1638: "+headName+"  label="+label+" tag="+com.tag());
        	
        	if(label==null || label.trim().isEmpty()) {
                if(headName.equals("J")) {
                	head+=Jhead;
                	tempHead+=tempJhead;
                	n+=2;	
                }else if(headName.equals("T"))  {
                	head+=Thead;
                	tempHead+=tempThead;
                	n+=4;	
                }else if(headName.equals("L"))  {
                	head+=Lhead;
                	tempHead+=tempLhead;
                	n+=1;	
                }
                else if(headName.equals("S"))  {
                	head+=Shead;
                	tempHead+=tempShead;
                	n+=1;	
                }else if(headName.equals("MOMM1")) {head+="\\ensuremath{\\mu}";tempHead+="u";n+=1;}
                else if(headName.equals("MOME2")) {head+="Q";tempHead+="Q";n+=1;}
                else if(headName.equals("E")) {head+="E(level)";tempHead+="E(level)";n+=8;}
                else if(headName.equals("XREF")) {head+="XREF";tempHead+="XREF";n+=4;}
                else if(headName.equals("ISPIN")) {head+="T";tempHead+="T";n+=1;}
                else if(headName.equals("G")) {head+="g";tempHead+="g";n+=1;}
                else if((headName.indexOf("BE")==0 || headName.indexOf("BM")==0) && headName.length()>=3){
                	if(headName.length()==3 && Character.isDigit(headName.charAt(2)))
                		headName+="UP";
                	
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
        		
        		String tag=com.tag();
        		String label1=label.toUpperCase();
        		
        		if(headName.equals("T") && !tag.isEmpty()) {//tag is used to indicate whether it is actually a T1/2 or width when T is relabeled as "T or width"
        			if(tag.startsWith("T")) {
                    	head+=Thead;
                    	tempHead+=tempThead;
                    	n+=4;
        			//}else if(tag.startsWith("W") && (label1.equals("T OR WIDTH")||label1.equals("T OR |G")||label1.equals("T{-1/2} OR |G"))){
        			}else if(tag.startsWith("W") && label1.length()>0){
                    	head+="\\ensuremath{\\Gamma}";
                    	tempHead+="W";
                    	n+=2;
        			}else {
                		head+=translatedLabel.trim();
                		tempHead+=label.trim();
                		n+=label.trim().length();
        			}
        		}else {
            		head+=translatedLabel.trim();
            		tempHead+=label.trim();
            		n+=label.trim().length();
        		}

        	}        	            	
        }
    	   	
        if(head.length()==0)
        	return "";
        
        head+=": ";
        tempHead+=": ";
        n+=2; 
        
        if(option.equals("temp")){
        	//System.out.println("In LevelTableMaker: head="+head+" "+head.length()+" "+n+" com.body="+com.body());
    
        	if(n<head.length())       		
        	   head=head.substring(head.length()-n);
        	return tempHead;
        	
        }
        return head;
    }
    
    protected Vector<Comment> convertDataAsComments(int panel,Record d,TableControl tc)throws Exception{
    	Level l=(Level)d;
    	Vector<Comment> out=new Vector<Comment>();
    	  
    	String type="";
        String temp="";

        ENSDF ens=this.etd.getENSDF();

        boolean hasXREF=(ens.nXRefs()>0);  
        boolean isXREFAsComment=(tc.getDataAsComments()!=null && tc.getDataAsComments().contains("XREF"));
        boolean hasContinuedXREF=false;
        String lastXREF="";
        if(l.nNewXRefsV()>0)
        	lastXREF=l.newXRefsV().lastElement();
        
        if(hasXREF && l.newXRefsVFormatted().indexOf(lastXREF)>etd.getNXRefs()-1)
        	hasContinuedXREF=true;
        
        //comment for continued XREF tag (nXrefs>etd.getNXRefs())
        
        if(hasContinuedXREF && !isXREFAsComment){
        	for(int i=l.nNewXRefsVFormatted()-1;i>=0;i--){
        		String x=l.newXRefsVFormatted().elementAt(i);
        		
        		//System.out.println(" In LevelTableMaker 1630: i="+i+" XREF="+x+" etd.getNXREfs="+etd.getNXRefs());
        		
        		if(i<etd.getNXRefs())
        			break;
        		if(x.length()==0) 
        			continue;
        		
        		if(temp.length()>0)
        			temp=", "+temp;
        		temp=x+temp;
        	}
        	
        	if(temp.length()>0){
        		temp="Others: "+temp;
            	Comment c=new Comment();
           		c.headV().add("XREF");
            	c.setBody(temp,false);
            	c.setBigC(false);
                //out.addElement(c);
            	out.insertElementAt(c, 0);
            	
                //debug
                //System.out.println("In LevelTableMaker: dataAsComment: c.body="+c.body()+" nhead="+c.nHeads());
            	
            	c.setIsContinuedXREF(true);
            	l.setHasContinuedXREF(true);
        	}
        }
        
        if(tc.getDataAsComments()!=null){
            for(int k=0;k<tc.getDataAsComments().size();k++){
                temp="";
                type=(String)tc.getDataAsComments().elementAt(k);
                if(type.equals("J")&&l.JPiS().length()>0) temp=l.JPiS();
                else if(type.equals("T")&&l.halflife().length()>0) temp=LatexWriter.printNumberAsIs(l.T12S(),l.DT12S(),Translator.halfLifeUnits(l.T12Unit()));
                else if(type.equals("XREF")&&l.newXREFS().length()>0) temp=l.newXREFS();
                else if(type.equals("BND")&&l.belongs2Band()){
                    try{
                        temp="Band: "+ens.bandAt(l.bandNum()).comment().flagAt(0,0);
                        type="";
                    }catch(Exception e){
                        System.out.println(ens.fullDSId()+" "+l.ES());
                        System.out.println("ERROR: One of the levels is not band labelled: maybe because a band has multiple flags associated with it");
                    }
                }
                else if(type.equals("L")&&l.lS().length()>0) temp=l.lS();
                
                if(temp.trim().length()>0){
                	Comment c=new Comment();
                	if(type.trim().length()>0)
                		c.headV().add(type);
                	
                	boolean isBigC=true;
                	if(type.equals("XREF"))
                		isBigC=false;
                	c.setBody(temp,isBigC);
                	c.setBigC(isBigC);
                    out.addElement(c);
                }
            }
        }
        
        return out;
    }
    
    
  /**Writes the table of Xrefs at the start of adopted datasets*/
     public Vector<String> makeXRefs(EnsdfTableData etd,MassChain data)throws Exception{
    //XREF TablePanel
         ENSDF ens=etd.getENSDF();
         Vector<String>out=new Vector<String>();
         
         this.XREFLinks.clear();
         
        //decide how many columns the table should have
        int nColumns=1;
        int count=0;
        //String fontsize_str="\\scriptsize";
        String fontsize_str="";
        
        if(ens.nXRefs()/2>4) nColumns=3;
        else if(ens.nXRefs()>4) nColumns=2;
        
        this.nTableRecordsLines++;//each table leaves one-line space before and after it
        this.tableRecordsHeight+=NDSConfig.TABLE_TOP_MARGIN;
        
        //out.add("\\begin{center}\n");
        
        
        //out.add("\\begin{tabular}{");       
        out.add("\\begin{longtable}[c]{");
        
        for(int j=0;j<nColumns;j++) out.add("ll");
        out.add("}\n");

        out.add("\\multicolumn{"+2*nColumns+"}{c}{\\underline{Cross Reference (XREF) Flags}}\\\\\n");
        out.add(" \\\\\n");
        this.nTableRecordsLines+=3;
        this.tableRecordsHeight+=NDSConfig.XREF_TABLE_LINEWIDTH+0.4+0.2;//title+underline+blankline
        
        Vector<XRef> XRefs=ens.XRefV();

        if(ens.nXRefs()>26){
        	XRefs=new Vector<XRef>();
            XRefs.addAll(ens.XRefV());
        	XRef other=new XRef();
        	other.setValues("Others", "Others", "");
        	XRefs.insertElementAt(other, 26);
        	
        }
        
        //debug
        //for(XRef r:ens.XRefV()){
        //	System.out.println("In LevelTableMaker line 1658: size="+XRefs.size()+" xref="+r.tag()+" "+r.DSId());
        //}
        	
        
        boolean needFootnote=false;
        int size=XRefs.size();
        int nRows=size/nColumns;
        if(size%nColumns!=0)
        	nRows+=1;
        
        for(int i=0;i<nRows;i++){
            for(int j=0;j<nColumns;j++){
            	int n=i+j*nRows;
                if(n<size){
                    XRef xR=XRefs.elementAt(n);
                    //out.add("\\texttt{"+fontsize_str+"{\\emph{"+xR.tag()+" }}}& "+Translator.process(xR.DSId(),true));
                    
                    String linkID="";
                    
                    boolean included=false;
                    String dsid0=xR.DSId0();//dsid0 used to make filename
                    String dsid=xR.DSId();//dsid used to show in XREF
                    
                    //System.out.println(dsid0+"$"+dsid);
                    
                    //this is only for a mass-chain ENSDF file containing multiple ENSDF datasets
                    for(int k=0;k<data.nENSDF();k++){
                    	String ensDSID0=data.getENSDF(k).DSId0();//DSID in first line
                    	String ensDSID=data.getENSDF(k).fullDSId();//DSID including continuation lines
                    	
                    	//System.out.println("                    dsid0="+dsid0+" ensDSID0="+ensDSID0);
                    	
                        //if((dsid.equals(ensDSID)||dsid.equals(ensDSID0)) && ens.nucleus().Z()==data.getENSDF(k).nucleus().Z()){
                    	if(dsid0.equals(ensDSID0) && ens.nucleus().Z()==data.getENSDF(k).nucleus().Z()){
                        	linkID=data.getETD(k).getLinkID();
                        	
                            dsid=ensDSID;//this corrects the xR DSID if it is only taken from the first line in its corresponding dataset  
                            
                                                        
                            if(data.getETD(k).isDrawn()) 
                            	included=true;
                        }
                    }
                    
                    //debug
                    //System.out.println("In LevelTableMaker: line 1675: i="+i+" j="+j+" n="+(i+j*(ens.nXRefs()/nColumns+1))+" XREF tag="+xR.tag()+"  "+Translator.process(xR.DSId(),true));
                    //System.out.println("                    ens.nXRefs()"+ens.nXRefs()+" ncolumn="+nColumns+" *"+ens.nXRefs()/nColumns);
                    //System.out.println("                    XR dsid0="+dsid0+" dsid="+dsid);
                    
                    
                    if(xR.tag().contains("Others"))
                    	out.add("\\multicolumn{2}{l}{"+fontsize_str+" Others:}");
                    else{
                      	String title=LatexWriter.makeDatasetTitle(dsid);
                      	
                    	if(linkID.length()>0){
                    		out.add("\\hyperlink{"+linkID+"}{\\texttt{"+fontsize_str+xR.tag()+" }}& "+title);
                    	    XREFLinks.put(xR.tag(), linkID);
                    	}
                    	else{//linked dataset is not present then link to the pdf file of that dataset in the same folder
                    		String href=EnsdfUtil.DSID2Filename(dsid0)+".pdf";
                    		href=EnsdfUtil.makePDFLink(href);
                    		out.add("\\href{"+href+"}{\\texttt{"+fontsize_str+xR.tag()+" }}& "+title);
                    	}
                    }
                    
                    if(etd.isUpdate()&&!included){ out.add("$^{\\dagger}$"); needFootnote=true;}
                    if(j<nColumns-1) out.add(" & ");
                }
                else{
                    break;
                }
            }
            out.add("\\\\\n");
            this.nTableRecordsLines++;
            this.tableRecordsHeight+=NDSConfig.XREF_TABLE_LINEWIDTH;
        }
        //out.add("\\end{tabular}\\\\\n");
        out.add("\\end{longtable}\n");
        
        if(needFootnote){
            out.add("$^{\\dagger}$");
            if(etd.updCom().length()>0){ 
            	out.add(etd.updCom());
            	count=etd.updCom().length()/NDSConfig.MAX_LINE_LENGTH+1;
            	this.nTableRecordsLines+=count;
            	this.tableRecordsHeight+=count*NDSConfig.XREF_TABLE_LINEWIDTH;
            }
            else{ 
            	String s="Although information derived from this experiement has been used in the making of the adopted " +
                        "levels, gammas, no tables or drawings for this dataset are given here. See the Elsevier or NNDC web sites for this " +
                        "experiment has part of a full report";
            	out.add(s);
            	count=s.length()/NDSConfig.MAX_LINE_LENGTH+1;
            	this.nTableRecordsLines+=count;
            	this.tableRecordsHeight+=count*NDSConfig.XREF_TABLE_LINEWIDTH;
            }
            out.add("\n");
        }
        
        //out.add("\\end{center}\n");
        
        this.nTableRecordsLines++;
        this.tableRecordsHeight+=NDSConfig.TABLE_BOTTOM_MARGIN;
        this.tableRecordsHeight-=0.4;//-0.5cm for vspace{-0.5cm} after \end{center} in LatexWriter
        
        return out;
    }  
     
     /*return a rough number of the width of all data columns in output table**/
     private float getEntryLength(Level l,Vector<String> columns)throws Exception{
         String colName;
         String row="";

         int nColumns=columns.size();
         for(int j=0;j<nColumns;j++){
             colName=(String) columns.elementAt(j);
             if(colName.equals("E"))   row+=l.ES()+" ";
             else if(colName.equals("DE"))  row+=l.DES()+" ";
             else if(colName.equals("J"))   row+=l.JPiS()+" ";
             else if(colName.equals("T"))   row+=l.T12S()+" "+l.DT12S()+" "+l.T12Unit()+" ";
             else if(colName.equals("L"))   row+=l.lS()+" ";
             else if(colName.equals("S"))   row+=l.sS()+" "+l.dsS()+" ";
             else if(colName.equals("XREF"))row+=l.newXREFSFormatted();
         }
         
         return row.length();
     }
     

    /*return a rough number of the maximum width of all data entries in output table panel**/
    @SuppressWarnings("unused")
 	private float findMaxEntryWidth(Vector<String> columns,Level firstLevel,Level lastLevel,Vector<ComFlag> flags)throws Exception{
     	float maxEntryWidth=0;
     	HashMap<String,String> colStrings=findMaxColumnStrings(columns,firstLevel,lastLevel,flags);
     	for(Map.Entry<String, String> entry : colStrings.entrySet()){
     		maxEntryWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
     	}
     	
     	return maxEntryWidth;
     }
     
	private HashMap<String,String> findMaxColumnStrings(Vector<String> columns,Level firstLevel,Level lastLevel,Vector<ComFlag> flags)throws Exception{
         HashMap<String,String> colStrings=new HashMap<String,String>();
        
	     if(firstLevel==null)
	         return colStrings;
	    
         String colName;
         String temp="";

         String decimalLeft="";
         String decimalRight="";
         String fakeFlagStr="";
               
         String maxDecimalLeftStr="";
         String maxDecimalRightStr="";
         
         float decimalLeftWidth=0;
         float maxDecimalLeftWidth=0;
         
         float decimalRightWidth=0;
         float maxDecimalRightWidth=0;
         
         String maxColString="";
         float maxColWidth=0;
         
         float colWidth=0;
         ENSDF ens=etd.getENSDF();
        	 
         int start=ens.levelsV().indexOf(firstLevel);
         int end  =ens.levelsV().indexOf(lastLevel);
         

         
         //if(start<0) start=0;
         if(end<0) end=start;
         
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
            	 temp="";
            	 Level l=ens.levelAt(k);  
            	             	 
            	 //debug
                 //System.out.println("In LevelTableMaker: line 1747:  l.flag()="+l.flag()+" temp="+temp+" fakeFlagStr="+fakeFlagStr);
                 fakeFlagStr="";
                 
                 if(colName.equals("E")){
                	 //NOTE that: band flags in band comment have been renamed and are different from the corresponding band flags in l.flag()
                	 //To access the renamed flags, use bandAt(l.banNum()).comment().flagAt(0,0)
                	 String bandFlags="";
                	 
                	 //NOTE: "flags" here are Vector of ComFlag type comments, which are made from top comments, of which the band comments have
                	 //the renamed band flags that are different from those in l.flag(). See in ENSDF.java->Comment() for the reason of renaming
                	 if(l.belongs2Band()){
                		 String bandComFlag=ens.bandAt(l.bandNum()).comment().flagAt(0, 0);
                    	 bandFlags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsByType(flags,bandComFlag,"BAND"));
                	 }

                	 
                	 fakeFlagStr+=bandFlags;
                	 temp=Str.getString(l.ES(),l.DES(),"");	 
                 }
                 else if(colName.equals("J"))   temp=l.JPiS().replace(":", "*to*");//use "*" as estimate of the width for " "
                 else if(colName.equals("T"))   temp=Str.getString(l.T12S(),l.DT12S(),l.T12Unit());
                 else if(colName.equals("L"))   temp=l.lS();
                 else if(colName.equals("S"))   temp=Str.getString(l.sS(),l.dsS(),""); 
                 else if(colName.equals("XREF")){
                	 temp=l.newXREFSFormatted();
                	 if(temp.length()>etd.getNXRefs())//the rest is written in comment
                		 temp=temp.substring(0, etd.getNXRefs());
                 }
                 else continue;
                 
            	 //debug
                 //System.out.println("In LevelTableMaker: line 1747: temp="+temp+" fakeFlagStr="+fakeFlagStr);
                 
                 fakeFlagStr+=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsByType(flags,l.flag(),colName));
                 
                 if(temp.trim().length()==0 && fakeFlagStr.length()==0)
                	 continue;
                 
                 if(colName.equals("E") || colName.equals("T") || colName.equals("S")){
                     
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
                     
                     if(temp.toUpperCase().contains("STABLE")){//alignment for T1/2=stable is slightly different, see makeLevelEntry()
                    	 decimalLeft="sta";
                    	 decimalRight="ble";
                     }
                     decimalLeftWidth=NDSConfig.findWidth(decimalLeft);
                     decimalRightWidth=NDSConfig.findWidth(decimalRight);
                    
                     //debug
                     //System.out.println("In LevelTableMaker: line 1775: temp="+temp+" decimalLeft="+decimalLeft+" decimalRight="+decimalRight+"####"+decimalLeftWidth+" "+decimalRightWidth);
                     
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
                    
                 }else if(colName.equals("XREF"))//plain text               	               	
                 	colWidth=Str.findWidth(temp,0.175f);
                 else{
                	temp+=fakeFlagStr;
                 	colWidth=NDSConfig.findWidth(temp);
                 }
                 
                 if(temp.trim().length()>0 && colWidth>maxColWidth){
                 	maxColString=temp;    
                 	maxColWidth=colWidth;
                 }

             }

             colStrings.put(colName, maxColString);
         }
         
         
         return colStrings;
     }
     
	@SuppressWarnings("unused")
	private HashMap<String,Float> findMaxColumnWidths(Vector<String> columns,Level firstLevel,Level lastLevel,Vector<ComFlag> flags)throws Exception{
        String colName="";
        String temp="";

        String decimalLeft="";
        String decimalRight="";
        String fakeFlagStr="";
        
        float maxColWidth=0;
              
        float decimalLeftWidth=0;
        float maxDecimalLeftWidth=0;
        
        float decimalRightWidth=0;
        float maxDecimalRightWidth=0;
        
        float colWidth=0;
        ENSDF ens=etd.getENSDF();
       	 
        int start=ens.levelsV().indexOf(firstLevel);
        int end  =ens.levelsV().indexOf(lastLevel);
        
        HashMap<String,Float> colWidths=new HashMap<String,Float>();
        
        if(start<0) start=0;
        if(end<0) end=ens.levelsV().size()-1;
        
        int nColumns=columns.size();
        for(int j=0;j<nColumns;j++){

       	 maxColWidth=0;
       	 
       	 maxDecimalLeftWidth=0;       	 
       	 maxDecimalRightWidth=0;

       	 
       	 for(int k=start;k<=end;k++){
           	 temp="";
           	 Level l=ens.levelAt(k);  
           	 fakeFlagStr=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsByType(flags,l.flag(),colName));
           	 
                if(colName.equals("E")){fakeFlagStr+=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsByType(flags,l.flag(),"BAND"));temp=Str.getString(l.ES(),l.DES(),"",fakeFlagStr);}
                else if(colName.equals("J"))   temp=l.JPiS().replace(":", "*to*")+fakeFlagStr;//use "*" as estimate of the width for " "
                else if(colName.equals("T"))   temp=Str.getString(l.T12S(),l.DT12S(),l.T12Unit(),fakeFlagStr);
                else if(colName.equals("L"))   temp=l.lS()+fakeFlagStr;
                else if(colName.equals("S"))   temp=Str.getString(l.sS(),l.dsS(),"",fakeFlagStr); 
                else if(colName.equals("XREF")){
               	 temp=l.newXREFSFormatted();
               	 if(temp.length()>etd.getNXRefs())//the rest is written in comment
               		 temp=temp.substring(0, etd.getNXRefs());
                }
                else continue;
                
                if(colName.equals("E") || colName.equals("T") || colName.equals("S")){
                 
                	if(temp.contains("E") || temp.contains("e"))//eg, 1.2E3 translated as 1.2x10^3               	 
                  		 temp+="***";
                   
                    decimalLeft=Str.delimLeftString(temp, '.');//aligned by '.'
                    decimalRight=Str.delimRightString(temp, '.');
                    if(decimalLeft.length()<=0){//otherwise, aligned by ' '
                       decimalLeft=Str.delimLeftString(temp, ' ');
                       decimalRight=Str.delimRightString(temp, ' ');
                       
                       if(decimalLeft.length()<=0)
                    	   decimalLeft=temp;
                    }else
                       decimalRight+='.';//add '.' to the right part of the string for width estimation
                   
                    decimalLeftWidth=NDSConfig.findWidth(decimalLeft);
                    decimalRightWidth=NDSConfig.findWidth(decimalRight);
                   
                    if(decimalLeftWidth>maxDecimalLeftWidth){                 
                	   maxDecimalLeftWidth=decimalLeftWidth;
                    }
                    if(decimalRightWidth>maxDecimalRightWidth){                  	
                	   maxDecimalRightWidth=decimalRightWidth;
                    }
                                                       
                    colWidth=maxDecimalLeftWidth+maxDecimalRightWidth;
                   
                }else if(colName.equals("XREF"))//plain text               	               	
                	colWidth=Str.findWidth(temp,0.175f);
                else
                	colWidth=NDSConfig.findWidth(temp);
                
                if(temp.trim().length()>0 && colWidth>maxColWidth){
                	maxColWidth=colWidth;
                }

            }

            colWidths.put(colName, maxColWidth);
        }
        
        
        return colWidths;
    }
	
     private float findMaxCommentWidth(int panel,Level l,EnsdfTableData etd,boolean showSuppressed)throws Exception{
         TableControl tc=etd.getLevelTableControl();
         ENSDF ens=etd.getENSDF();

         float width;
         float maxCommentWidth=0;
         float commentColumnWidth=tc.getCommentColumnWidth(panel);  
         int nlines=0;
         
         String head="";
         String com="";

         
         DecayMode dm;
         for(int k=0;k<l.nDecayModes();k++){
             com="";
             dm=l.decayModeAt(k);
             if(dm.name().equals("A"))com+="%A="+dm.value()+" "+dm.unc()+" "+dm.ref();
             else if(dm.name().equals("B-"))com+="%B-="+dm.value()+" "+dm.unc()+" "+dm.ref();
             else com+="%"+dm.name()+"="+dm.value()+" "+dm.unc()+" "+dm.ref();
             
             width=com.length()*NDSConfig.CHAR2CM;
             if(width>maxCommentWidth) maxCommentWidth=width;
             if(width>0)
            	 nlines+=width/commentColumnWidth+1;
         }
         
         ContRecord cr;
         for(int k=0;k<l.nContRecords();k++){
        	cr=l.contRecordAt(k);
            if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) ){
                 com=cr.name()+"="+cr.s()+" "+cr.ds()+" "+cr.ref(true);

                 width=com.length()*NDSConfig.CHAR2CM;
                 if(width>maxCommentWidth) maxCommentWidth=width;
                 if(width>0)
                	 nlines+=width/commentColumnWidth+1;
        	 }
         }
         
         Comment c;
         for(int k=0;k<l.nComments();k++){
             com=""; 
             c=l.commentAt(k);
             if(c.nHeads()>0){
                 if(c.headAt(0).equals("J")) head="Jpi";
                 else if(c.headAt(0).equals("T")) head="T1/2";
                 else if(c.headAt(0).equals("MOMM1")) head="m";
                 else if(c.headAt(0).equals("MOME2")) head="Q";
                 else if(c.headAt(0).equals("E")) head="E(level)";
                 else if(c.headAt(0).equals("XREF")) head="XREF";
                 else head=Translator.process(c.headAt(0));
                 head+=": ";
             }else head="";
             com+=head+c.body();

             width=com.length()*NDSConfig.CHAR2CM;
//debug
             //System.out.println("in LevelTableMaker: width="+width+" %"+com+"%"+com.length());
             if(width>maxCommentWidth) maxCommentWidth=width;
             if(width>0)
            	 nlines+=width/commentColumnWidth+1;
         }
         
         String type;
         if(tc.getDataAsComments()!=null){
             for(int k=0;k<tc.getDataAsComments().size();k++){
                 com="";
                 type=(String)tc.getDataAsComments().elementAt(k);
                 if(type.equals("J")&&l.JPiS().length()>0) com+="Jpi: "+l.JPiS();
                 else if(type.equals("T")&&l.halflife().length()>0) com+="T1/2: "+l.T12S()+" "+l.T12Unit()+" "+l.DT12S();
                 else if(type.equals("XREF")&&l.newXREFS().length()>0) com+="XREF: "+l.newXREFS();
                 else if(type.equals("BND")&&l.belongs2Band()){
                     try{
                         com+="Band: "+ens.bandAt(l.bandNum()).comment().flagAt(0,0);
                     }catch(Exception e){
                         System.out.println(ens.fullDSId()+" "+l.ES());
                         System.out.println("ERROR: One of the levels is not band labelled: maybe because a band has multiple flags associated with it");
                     }
                 }
                 else if(type.equals("L")&&l.lS().length()>0) com+="l: "+l.lS();
                 if(com.length()>0){
                     width=com.length()*NDSConfig.CHAR2CM;
                     if(width>maxCommentWidth) maxCommentWidth=width;
                     nlines+=width/commentColumnWidth+1;
                 }
             }
         }
         //System.out.println("In LevelTableMaker: maxCommentWidth="+maxCommentWidth);
         this.nCommentLines=nlines;
         return maxCommentWidth;
     }

     
     private Vector<String> makeBandTable(ENSDF ens) throws java.lang.Exception{
         //add band table
         String s,head;
         Vector<String> Set=new Vector<String>();
         
         
         if(ens.nBands()>1)
        	 s="s";
         else 
        	 s="";

         if(ens.nBands()>0){
        	 
        	 //if(Control.autoAdjust){
        	 if(true){	 
             	 this.nTableRecordsLines+=1;//each table leaves one-line space before and after it
                 this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
                
                 Set.add("\\begin{longtable}{ll}\n");
                 Set.add("&\\hspace{-0.6cm}Nuclear Level Sequence"+s+"\\\\[0.1cm]\n");
                 
                 this.nTableRecordsLines+=1;
                 this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
                 
                 this.tableRecordsHeight+=0.1f;

                 
                 Vector<String> tempComs=new Vector<String>();
                 int count=0;
                 head="";
                 
                 for(int i=0;i<ens.nBands();i++){
                 	Comment tempC=ens.bandAt(i).comment();
                 	tempComs=wrapENSDFCommentTranslated(tempC,NDSConfig.HEADNOTE_WIDTH,"","",tempC.isBigC());
                 	count=0;
                 	head=ens.bandAt(i).comment().flagAt(0,0);

                 	
                 	for(int m=0;m<tempComs.size();m++){
                 		if(tempComs.elementAt(m).length()==0)
                 			continue;

                 		
                 		if(m==0)
                 			Set.add(head+" &\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]["+NDSConfig.TABLE_RECORDS_LINEWIDTH+"cm]{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\raggedright "+tempComs.elementAt(0)+"}\\\\\n");
                 		else
                 			Set.add(" &\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]["+NDSConfig.TABLE_RECORDS_LINEWIDTH+"cm]{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\raggedright "+tempComs.elementAt(m)+"}\\\\\n");
                 		
                 		count++;
                 	}
                 	
                     
                     this.nTableRecordsLines+=count;
                     this.tableRecordsHeight+=count*NDSConfig.TABLE_RECORDS_LINEWIDTH;//

                 }
                 Set.add("\\end{longtable}\n");
                 
                 this.nTableRecordsLines+=1;
                 this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
        	 }
        	 /*
        	 else
        	 {
                 Set.add("\\begin{longtable}{ll}\n");
                 Set.add("&\\hspace{-0.3cm}Nuclear Level Sequence"+s+"\\\\[0.1cm]\n");
                 for(int i=0;i<ens.nBands();i++){
                 	Comment tempC=ens.bandAt(i).comment();
                 	head=ens.bandAt(i).comment().flagAt(0,0);
                 	
                 	Set.add(head+" &\\parbox[c]{15cm}{\\raggedright "+tempC.getRegTranslated()+"}\\\\\n");
                 }
                 Set.add("\\end{longtable}\n");
        	 }
        	 */

         }
         
         return Set;
     }
     
     protected String commentBuilder(Comment c) throws java.lang.Exception{
         String temp="";
         String head;
         if(c.nHeads()>0){
             //if(c.headAt(0).equals("J")) head="J\\ensuremath{^{\\pi}}";
             if(c.headAt(0).equals("J")) head="J\\ensuremath{\\pi}";
             else if(c.headAt(0).equals("T")) head="T\\ensuremath{_{1/2}}";
             else if(c.headAt(0).equals("MOMM1")) head="\\ensuremath{\\mu}";
             else if(c.headAt(0).equals("MOME2")) head="Q";
             else if(c.headAt(0).equals("E")) head="E(level)";
             else if(c.headAt(0).equals("XREF")) head="XREF";
             else head=Translator.process(c.headAt(0));
             head+=": ";
         }else head="";
         temp+=head;
         if(head.length()>0)temp+=c.getHTranslated();
         else temp+=c.getTranslated();
         return temp;
     }
     
}
