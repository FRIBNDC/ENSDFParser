package ensdfparser.nds.latex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import ensdfparser.ensdf.Comment;
import ensdfparser.ensdf.ContRecord;
import ensdfparser.ensdf.DecayMode;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.Record;
import ensdfparser.ensdf.TableRecord;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.Break;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.EnsdfConfig;
import ensdfparser.nds.ensdf.EnsdfTableData;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.util.Str;


/**
 * Abstract class for making tables
 * @author Jun Chen
 */
public abstract class TableMaker {

	HashMap<String,String> XREFLinks=null;
	
	protected boolean autoAdjust;//value set to be Control.autoAdjust
	protected TableControl tc;
    protected EnsdfTableData etd;
    protected MassChain data;
    protected float maxCommentWidth=0;
    protected float tableRecordsHeight=0;
    protected float tableBodyHeight=0;
    protected int nTableRecordsLines=0;
    protected int nXREFTableLines=0;
    protected int nTableBodyLines=0;
    protected int nCommentLines=0;//initialized to 0 for each level/gamma/decay and incremented in findMaxCommentWidth, 
                                  //used to determine the total number of lines of all comments for each level/gamma/decay in
                                  //the real PDF output [comments are written in Latex parbox, one long comment could occupy
                                  //multiple lines in output depending on the width of the parbox which is set to be 
                                  //tc.getCommentColumnWidth(panel)]
    
	  
    protected float HEADNOTE_WIDTH=NDSConfig.HEADNOTE_WIDTH;  
    protected float FOOTNOTE_WIDTH=NDSConfig.FOOTNOTE_WIDTH;
    protected int HEADNOTE_LENGTH=NDSConfig.HEADNOTE_LENGTH;  
    protected int FOOTNOTE_LENGTH=NDSConfig.FOOTNOTE_LENGTH;
	  
    protected String tableType="";
    	
    protected void init(){
        if(tc!=null && !tc.isPortrait()){
			  HEADNOTE_WIDTH=(NDSConfig.MAX_TEXT_HEIGHT/NDSConfig.MAX_TEXT_WIDTH)*NDSConfig.HEADNOTE_WIDTH;
			  FOOTNOTE_WIDTH=(NDSConfig.MAX_TEXT_HEIGHT/NDSConfig.MAX_TEXT_WIDTH)*NDSConfig.FOOTNOTE_WIDTH;
			  HEADNOTE_LENGTH=(int)((NDSConfig.MAX_TEXT_HEIGHT/NDSConfig.MAX_TEXT_WIDTH)*NDSConfig.HEADNOTE_LENGTH);
			  FOOTNOTE_LENGTH=(int)((NDSConfig.MAX_TEXT_HEIGHT/NDSConfig.MAX_TEXT_WIDTH)*NDSConfig.FOOTNOTE_LENGTH);  
        }  
        
        autoAdjust=NDSControl.autoAdjust;//make sure all TableMakers have the same value of autoAdjust
    }
	
    protected String tableType(){return tableType;}
    
    //*******************************************************
    //Abstract methods
	/**isUnplaced is a option for making gamma tables*/
	abstract public TablePanel makeTablePanel(int panel,boolean isUnplaced)throws Exception;
	abstract public TablePanel makeTablePanel(int panel)throws Exception;
	
	abstract protected String commentHeadMaker(Comment c,String option) throws Exception;
	abstract protected String commentBuilder(Comment c) throws Exception;

	abstract protected Vector<Comment> convertDataAsComments(int panel,Record d,TableControl tc)throws Exception;

	//*******************************************************
		
	/* make the same panel twice, the first make is with default settings and is used to 
	 * scan the whole panel and get the actual settings. Then the second make is used to
	 * make the final panel with the actual settings.
	 * This method is called only in findBreaks to create temporary panels in order to
	 * find actual comment width.
	 * 
	 * About comment width used in makeTablePanel:
	 * 1. In the first make, the program uses existing width set previously in the 
	 *    TableControl tc for this panel or the default comment width initialized with 
	 *    the table's tc in EnsdfTableData. Then at the end of the making, the program
	 *    estimates the actual data width, from which the actual comment width for
	 *    this panel is determined here and is used to replace the width setting in
	 *    the tc. So after each remakeTablePanel(), the width setting in tc will be
	 *    updated for this panel.
	 * 2. The data width used here to determine the final comment width is determined
	 *    by the non-empty columns in this panel's panelColumns in makeTablePanel(), 
	 *    while this panel's panelColumns could be updated after each scan of more  
	 *    lines to be put in the current panel with empty columns being removed after 
	 *    updatePanelCoulums(). The updated columns of this panel will then be used to 
	 *    determine the data width in the next makeTablePanel().
	 *    
	 * Here it is how they work:
	 *    step1: remakeTablePanel() calls makeTablePanel() to use current panelColumns
	 *           to determine the actual max data width in this panel, and then uses it 
	 *           to determine actual comment width of this panel
	 *    step2: updatePanelColumns() remove empty columns (if not set to be fixed) in 
	 *           this panel's columns    
	 *    step3: call remakeTablePanel() again with updated panel columns to re-determine
	 *           the actual data width and comment width.
	 *           
	 * The determining factor is how data width used here is determined.
	 * Note that in the makeTablePanel() where data width is estimated, empty data column
	 * doesn't count if it is not a fixed column. For the special cases below:
	 *   case 1. If the current panel contains only comment column, which could be
	 *           the remaining part of the long comment of last record in previous panel, 
	 *           empty data columns can be added in the output by adding those columns
	 *           into the fixed column of this panel set in the tc. 
	 *   case 2. The current panel has large blank at bottom and next panel has more columns.
	 *           Moving lines from next panel to current panel will update this panel's
	 *           panelColumns and result in a narrow comment width. As a consequence of the
	 *           change in comment width, the current panel will not be able to hold even
	 *           its existing content, since narrow comment width turns existing comments to
	 *           more lines which beyond the limit of the current panel. To fix it, empty 
	 *           columns that are removed earlier can be added back one by one by adding it
	 *           to fixed column of current panel set in tc for each remakeTablePanel() until 
	 *           no large blank is left at the bottom of the current panel.
	 */
	protected TablePanel remakeTablePanel(int panel,boolean isUnplaced)throws Exception{
		//debug
		//System.out.println("######################### In TableMaker line 89: start calling makeTablePanel()");
		
		float commentColumnWidth;
		TablePanel tp=makeTablePanel(panel,isUnplaced);
        
		boolean isBreakAtLastCom=tp.isBreakAtLastCom();
		
		if(NDSControl.autoAdjust || tc.isUseAutoComWidth()){
            float dataWidth=tp.getDataWidth();
            float dataHeadWidth=tp.getDataHeadWidth();
            if(dataWidth==0 || dataWidth<dataHeadWidth)
                dataWidth=dataHeadWidth;
            
			if(tc.isPortrait(tp) && tc.isPortrait())
				commentColumnWidth=NDSConfig.MAX_TEXT_WIDTH-dataWidth;
			else
				commentColumnWidth=NDSConfig.MAX_TEXT_HEIGHT-dataWidth;
			
			//if(commentColumnWidth>tc.getCommentColumnWidth())
			//	commentColumnWidth=(tc.getCommentColumnWidth()+commentColumnWidth)/2;



			tc.setCommentColumnWidth(panel, commentColumnWidth);
			
			tp=makeTablePanel(panel,isUnplaced);
			
		}
		
		if(isBreakAtLastCom)
			tp.setIsBreakAtLastCom(isBreakAtLastCom);
		
		//debug
		//System.out.println("######################## In TableMaker line 117: end calling makeTablePanel()");
		
		return tp;
	}
	
	protected TablePanel remakeTablePanel(int panel)throws Exception{
		float commentColumnWidth;
		
		TablePanel tp=makeTablePanel(panel);
		//debug
		//for(int k=0;k<tp.getColumns().size();k++)
		//	System.out.println(" In TableMaker line 118: column name+"+k+"="+tp.getColumns().elementAt(k));
		//debug
		//if(panel>=0) System.out.println("In TableMaker line 93: 1@@@panel="+panel+" isBreakAtLastCom="+tp.isBreakAtLastCom()+" gammaBreak="+tc.getBreaks().get(panel).pos+" "+tc.getCommentColumnWidth(panel));
		
		//debug
	
		//System.out.println("######In TableMaker: remakeTablePanel line 133: panel="+panel+" table body height="+tp.getTableBodyHeight()+"  body lines="+tp.getNTableBodyLines());
		
		boolean isBreakAtLastCom=tp.isBreakAtLastCom();
		
		if(NDSControl.autoAdjust || tc.isUseAutoComWidth()){
            float dataWidth=tp.getDataWidth();
            float dataHeadWidth=tp.getDataHeadWidth();
            if(dataWidth==0 || dataWidth<dataHeadWidth)
                dataWidth=dataHeadWidth;
            
            if(tc.isPortrait(tp) && tc.isPortrait())
                commentColumnWidth=NDSConfig.MAX_TEXT_WIDTH-dataWidth;
            else
                commentColumnWidth=NDSConfig.MAX_TEXT_HEIGHT-dataWidth;
			
			//debug
			//if(commentColumnWidth<0)
			//	System.out.println("In TableMaker: remakeTablePanel: DataWidth="+tp.getDataWidth());
			
			//if(commentColumnWidth<0)
			if(commentColumnWidth<NDSConfig.MIN_COMMENT_WIDTH)
				commentColumnWidth=NDSConfig.MIN_COMMENT_WIDTH;
						
			
			tc.setCommentColumnWidth(panel, commentColumnWidth);
			tc.setIsNewCommentWidth(true);
			
			/*
			//debug
			if(this.tableType.equals("DECAY")) {
				System.out.println("In TableMaker 210: panel="+panel+" remakeTablePanel: commentColumnWidth(panel="+panel+")="+commentColumnWidth+" dataWidth="+tp.getDataWidth());
				System.out.println("            min comColWidth="+NDSConfig.MIN_COMMENT_WIDTH+"  text width="+NDSConfig.MAX_TEXT_WIDTH);
				
				//System.out.println("               tc.isPortrait(tp)="+tc.isPortrait(tp)+" tc.isPortrait="+tc.isPortrait());
			}
            */
		
			//debug
			//for(int k=0;k<tp.getColumns().size();k++)
			//	System.out.println(" In TableMaker line 144: column name+"+k+"="+tp.getColumns().elementAt(k));

			tp=makeTablePanel(panel);
			
			tp.setCommentColumnWidth(commentColumnWidth);

		}
    
		if(isBreakAtLastCom)
			tp.setIsBreakAtLastCom(isBreakAtLastCom);
		
		//debug
		//if(panel>=0) System.out.println("In TableMaker line 158: 2###panel="+panel+" isBreakAtLastCom="+tp.isBreakAtLastCom()+" gammaBreak="+tc.getBreaks().get(panel).pos+" "+tc.getCommentColumnWidth(panel));
		
		return tp;
	}	
	
    /**Get all table panels for a table created by corresponding TableMaker*
     * Merge panels if necessary, e.g., after table splitting
     */
	protected Vector <TablePanel> makeTablePanels(boolean isUnplaced)throws Exception {
		//debug
		//System.out.println("######################### In TableMaker line 182: start calling makeTablePanel()");
		
		Vector <TablePanel> T=new Vector<TablePanel>();
		
		TablePanel tp=makeTablePanel(0,isUnplaced);      
		T.add(tp);
            
        
		for(int i=1;i<=tc.getBreakPoints().size();i++){         
			if(tc.isBreakPage(i-1)){ 
				tp=makeTablePanel(i);              	
				T.add(tp);                 
			}              
			else {                	
  				
				tp=makeTablePanel(i,isUnplaced);
				TablePanel lastT=T.lastElement();
				Vector<String> b1=new Vector<String>();
				b1.addAll(lastT.getBody());
				float height=lastT.getTableBodyHeight();
				
				lastT.Merge(tp, true); 
				Vector<String> b2=lastT.getBody();
				
                for(int m=0;m<b2.size();m++){
                	float scale=1.0f;
                	if(Str.containSpecialSupScript(b2.get(m)))
                		scale=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*1.10f;             
                	else if(Str.containSupScript(b2.get(m))){
                		if(!Str.containJPIScriptOnly(b2.get(m)))
                			scale=NDSConfig.TABLE_LINEWIDTH_SCALE0;
                		else
                			scale=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                	}
                	
            		if(m<b1.size() && !Str.containSupScript(b1.get(m)))
            			height+=(scale-1)*NDSConfig.TABLE_BODY_LINEWIDTH0;
            		else if(m>=b1.size())
            			height+=scale*NDSConfig.TABLE_BODY_LINEWIDTH0;
                }

                lastT.setTableBodyHeight(height);
                
			}            
		}            
         
		if(tc.isSeparateFoots()) T.lastElement().sepFoots();
            
		//debug
		//System.out.println("######################### In TableMaker line 230: start calling makeTablePanel()");
		
		return T;
	}
	
	/**
	 * make all table panels with final settings in current TableControl tc
	 * which should be set with actual settings (comment width) in findBreaks()
	 * by calling remakeTablePanel(panel)
	 */
	public Vector <TablePanel> makeTablePanels()throws Exception {	
		
		//debug
		//System.out.println("######################### In TableMaker line 293: makeTablePanels(): start calling makeTablePanel() "+tc.getBreakPoints().size());
		
		Vector <TablePanel> T=new Vector<TablePanel>();
        
		TablePanel tp=makeTablePanel(0);    
		
		//debug
		//if(this instanceof LevelTableMaker) {
		//System.out.println("In TableMaker line 252: "+tp.getHead());
		//System.out.println("In TableMaker line 252:     "+tp.getBody());
		//}
		
		//for(int i=1;i<=tc.getBreakPoints().size();i++){
		//	System.out.println("    break at:"+tc.getBreaks().get(i-1).pos+" gammaPos="+tc.getBreaks().get(i-1).gammaPos+" isBreakPage="+tc.isBreakPage(i-1));   
		//}
		//}

		//System.out.println(tp.getNColumns());	
			
		T.add(tp);
        tp.setIsBodyInNewPage(tc.isBodyInNewPage()); //for fist panel only, used for breaking short comments/table records that can be kept in current page from table body
                                                     //that need to be moved to new page
		
		//debug
		//System.out.println("@@@@@@@@@@@In TableMaker: makeTablePanels: final makeTablePanel: panel=0"+": "+tc.getCommentColumnWidth(0));
		for(int i=1;i<=tc.getBreakPoints().size();i++){
			//System.out.println("    break at:"+tc.getBreaks().get(i-1).pos+" gammaPos="+tc.getBreaks().get(i-1).gammaPos+" isBreakPage="+tc.isBreakPage(i-1));     

			
			if(tc.isBreakPage(i-1)){ 	
				//debug
				//System.out.println("@@@@@@@@@@@In TableMaker: makeTablePanels: final makeTablePanel: panel="+i+": "+tc.getCommentColumnWidth(i));
				tp=makeTablePanel(i); 
				//debug
				//System.out.println("In TableMaker: makeTablePanels: commentColumnWidth("+i+")="+tc.getCommentColumnWidth(i));

	            //System.out.println("TableMaker 334: ########## body="+tp.getBody());
				
				tp.setNewPage(true);            	
				T.add(tp);    
				
			}              
			else {       
				tp=makeTablePanel(i);
				TablePanel lastT=T.lastElement();
				
				//System.out.println(tp.getNColumns()+" "+lastT.getNColumns());
				
				//note that the columns of TablePanel is a reference to the corresponding columns in TableControl,
				//Any change made to the columns of TablePanel acually applies to the columns of its TableControl.
				//So here after merging, the corresponding columns in TableControl become the columns of the merged
				//TablePanel
				lastT.Merge(tp, true); 
				
		        //System.out.println("TableMaker 351: nBreaks="+tc.getBreakPoints().size()+" "+tableType+" "+lastT.getNColumns());
		        //for(String s:tc.getBreakPoints())
		        //	System.out.println("   "+s);
		        
				//if(this instanceof LevelTableMaker) {
				//System.out.println("In TableMaker line 252: "+tp.getHead());
				//System.out.println("In TableMaker line 252 body:     "+lastT.getBody());
				//}
			}            
		}            
         
		if(tc.isSeparateFoots()) T.lastElement().sepFoots();
          
		//debug
		//System.out.println("######################### In TableMaker line 313: makeTablePanels(): end calling makeTablePanel()");
		
		return T;
	}

	/**make a vector of Latex strings for the table-record data
	 * ("t" record). Other table-record data like XREF are
	 * created separately in corresponding TableMaker. 
	 */
    public Vector<String> makeTableRecords(TableRecord t,boolean format)throws Exception{

    	//debug
    	//System.out.println("In TableMaker: makeTableRecords: t.nlines="+t.nLines()+" format="+format);
    	
        if(format){    	
        	return makeTableRecordsFormatted(t);
        }else{
        	return makeTableRecordsUnformatted(t);
        }//unformatted table
    }
	
    public Vector<String> makeTableRecordsUnformatted(TableRecord t) throws Exception{
    	
        Vector<String> out=new Vector<String>();
        boolean inCol=false;
        int start=0,end=0;

        String temp;
        String line;
        String tline;
        char mark=0;//character at column 6 (line.charAt(5))
        
        boolean inBracket=false;
        int nBrackets=0;
        
        int numNonPrintable=0;
    	//non-printable characters are treated as ""
    	//TO DO (a better way for formatted table): insert "&", like LaTex table,
    	//                                          this way, column can be easily separated
        out.add("\n");
        //out.add("\\begin{minipage}{\\textwidth}\n");
        
        for(int j=0;j<t.nLines();j++){
        	start=0;
        	end=0;
        	temp="";
            line="";
            inCol=false;
            inBracket=false;
            nBrackets=0;
            tline=t.tableLineAt(j);
            
      
            //NOTE: if mark='#', the line will be printed as is for spacing, not considering the non-printable. 
            //      This is to preserve the spacing in footnotes of the table records. Otherwise, unwanted extra
            //      spaces will added at the end of a word if there are non-printable characters in this word 
            //      before translation.
            
            mark=t.fullLineAt(j).charAt(5);
            
            tline=Str.rtrim(tline);
            
            
            if(tline.trim().equals("$"))
            	tline=" ";
            
        	//debug
        	//System.out.println("In TableMaker: makeTableRecords: line=");
        	//System.out.println("   ->"+tline+"<-");
        	
            numNonPrintable=0;
            for(int k=0;k<tline.length();k++){

                @SuppressWarnings("unused")
				char nextChar=0;
                char ch=tline.charAt(k);
                if(k<tline.length()-1)
                	nextChar=tline.charAt(k+1);
                
            	if(ch=='{'){
            		nBrackets++;
            		inBracket=true;
            	}
            	else if(ch=='}'){
            		nBrackets--;
            		if(nBrackets==0)
            			inBracket=false;
            	}

            	if(ch==' ' || k==tline.length()-1){ 
                	if(!inBracket){
                       	end=k;
                    	if(k==tline.length()-1)
                    		end=k+1;
                    	
                        if(inCol){
                            inCol=false;
                            temp=tline.substring(start,end);
                            if(t.bigT(j)) 
                            	line+=(Translator.process(temp));
                            else{ 
                            	String tempLine=Translator.translate(temp);
                            	line+=tempLine;
                            	
                            	//System.out.println("temp="+temp+" tempLine="+tempLine+" line="+line);
                            	
                            	//"~" is counted as non-printable, assumed for cases like, ~A,~E,~O,..., which are
                            	//translated as some special symbols with something like "." or ".." on the top of the
                            	//letters.
                            	//But in other cases, "~" is not translated and is printed in LaTex as a whitespace and is printable.
                            	
                                //      here if mark='#', the line will be printed as is for spacing, not considering the non-printable. 
                                //      This is to preserve the spacing in footnotes of the table records. Otherwise, unwanted extra
                                //      spaces will added at the end of a word if there are non-printable characters in this word 
                                //      before translation.
                            	if(mark!='#'){
                                	numNonPrintable=Str.countNonPrintableENSDF(temp);
                                	if(temp.indexOf("~")>=0 && tempLine.indexOf("~")>=0)
                                		numNonPrintable--;
                            	}

                            }
                        }
                        else if(k==tline.length()-1){
                            temp=tline.substring(k);
                            if(t.bigT(j)) 
                            	line+=(Translator.process(temp));
                            else 
                            	line+=(Translator.translate(temp));
                     
                        }
                        
                        
                        
                        if(ch==' '){
                        	//if(numNonPrintable>0 && nextChar==' '){
                        	if(numNonPrintable>0){
                        		for(int m=0;m<numNonPrintable;m++)
                        			line+="\\ ";
                        		
                        		numNonPrintable=0;
                        	}
                        	line+="\\ ";
                        }
                	}
                }else if(!inCol){
                    start=k;
                    inCol=true;
                }
            	
                //debug
                //if(tline.contains("428")) System.out.println("     ch="+ch+" k="+k+" start="+start+" end="+end+" inCol="+inCol+" inBracket="+inBracket+" line="+line);
                                                             
            }

        	//debug
        	//System.out.println("In TableMaker: makeTableRecords: line B="+line+" tableheight="+this.tableRecordsHeight);
            
            //symbols (relation operators) in math mode have different size as text and they take more space than text (a letter, symbols)
            //shift it to left by 0.04cm to compensate the difference
            line=line.replace(" \\ensuremath", " \\hspace{-0.04cm}\\ensuremath");
            
            
            out.add("\\raggedright\\texttt{"+line+"}\\\\\n");
            this.nTableRecordsLines++;
            this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
        }
        
        //add an extra blank line
        this.nTableRecordsLines++;
        this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
        
        out.add("\\centering \\texttt{}\\\\\n");
        //out.add("\\end{minipage}\n");
        
        return out;
    }
    
    public Vector<String> makeTableRecordsUnformatted1(TableRecord t) throws Exception{
    	
        Vector<String> out=new Vector<String>();
        boolean inCol=false;
        int start=0,end=0;

        String temp;
        String line;
        String tline;
        
        boolean inBracket=false;
        int nBrackets=0;
        
    	//non-printable characters are treated as ""
    	//TO DO (a better way for formatted table): insert "&", like LaTex table,
    	//                                          this way, column can be easily separated
        out.add("\n");
        //out.add("\\begin{minipage}{\\textwidth}\n");
        
        for(int j=0;j<t.nLines();j++){
        	start=0;
        	end=0;
            line="";
            inCol=false;
            inBracket=false;
            nBrackets=0;
            tline=t.tableLineAt(j);//return t.linesV.get(j).substring(8);
            
            
            tline=Str.rtrim(tline);
            
            if(tline.trim().equals("$"))
            	tline=" ";
            
        	//debug
        	//System.out.println("In TableMaker: makeTableRecords: line=");
        	//System.out.println("   ->"+tline+"<-");
        	
            
            for(int k=0;k<tline.length();k++){

                char ch=tline.charAt(k);
         
            	if(ch=='{'){
            		nBrackets++;
            		inBracket=true;
            	}
            	else if(ch=='}'){
            		nBrackets--;
            		if(nBrackets==0)
            			inBracket=false;
            	}

            	if(ch==' ' || k==tline.length()-1){ 
                	if(!inBracket){
                       	end=k;
                    	if(k==tline.length()-1)
                    		end=k+1;
                    	
                        if(inCol){
                            inCol=false;
                            temp=tline.substring(start,end);
                            if(t.bigT(j)) 
                            	line+=(Translator.process(temp));
                            else 
                            	line+=(Translator.translate(temp));
                        }
                        else if(k==tline.length()-1){
                            temp=tline.substring(k);
                            if(t.bigT(j)) 
                            	line+=(Translator.process(temp));
                            else 
                            	line+=(Translator.translate(temp));
                     
                        }
                        
                        if(ch==' ') line+="\\ ";
                	}
                }else if(!inCol){
                    start=k;
                    inCol=true;
                }
            	
                //debug
                //if(tline.contains("428")) System.out.println("     ch="+ch+" k="+k+" start="+start+" end="+end+" inCol="+inCol+" inBracket="+inBracket+" line="+line);
                                                             
            }

        	//debug
        	//System.out.println("In TableMaker: makeTableRecords: line B="+line);
            
            out.add("\\raggedright\\texttt{"+line+"}\\\\\n");
            this.nTableRecordsLines++;
            this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
        }
        out.add("\\centering \\texttt{}\\\\\n");
        //out.add("\\end{minipage}\n");
        
        return out;
    }
    
    
    //not used
    public Vector<String> makeTableRecordsFormatted(TableRecord t) throws Exception{
        Vector<String> headV=new Vector<String>();
        Vector<String> colV=new Vector<String>();
        Vector<Integer> newColPos=new Vector<Integer>();
        Vector<String> out=new Vector<String>();
        boolean inCol=false;
        int start=0;
        int nColumns=0;
        String temp;
        String line;

    	
    	int headIndex=0;
        //find the first line that is broken up into columns, and count the columns
        //that line becomes the header of the table
        for(int j=0;j<t.nLines();j++){
        	
        	
            line=t.tableLineAt(j).trim();
            if(line.contains("   ")){
                headIndex=j;
                for(int k=0;k<line.length();k++){
                    if(k==line.length()-1){
                            if(t.bigT(j)) temp=Translator.process(line.substring(start,k+1));
                            else temp=Translator.translate(line.substring(start,k+1));
                            headV.add(temp);                 
                            newColPos.add(line.length());
                    }
                    if(line.charAt(k)==' '&&line.charAt(k+1)==' '){
                        if(inCol){
                            inCol=false;
                                                           
                            if(t.bigT(j)) temp=Translator.process(line.substring(start,k));
                            else temp=Translator.translate(line.substring(start,k));
                            headV.add(temp);

                        }
                    }else{
                        if(!inCol){ 
                            inCol=true;
                            start=k;
                            newColPos.add(start);
                        }
                    }
                }//loop through header
                break;
            }
        }
        nColumns=headV.size();
        if(nColumns<1) nColumns=1;
        
        //write table header
        this.nTableRecordsLines++; //each table leaves one-line space before and after it
        this.tableRecordsHeight+=NDSConfig.TABLE_TOP_MARGIN;
        
        out.add("\\begin{longtable}{");
        for(int j=0;j<nColumns;j++) out.add("l");
        out.add("}\n");
        for(int j=0;j<headIndex;j++){
            if(t.tableLineAt(j).contains("----")){
                out.add("\\hline\n");
                this.nTableRecordsLines++;
                this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
                continue;
            }
            if(t.bigT(j)) temp=Translator.process(t.tableLineAt(j));
            else temp=Translator.translate(t.tableLineAt(j));
            out.add("\\multicolumn{"+nColumns+"}{l}{"+temp+"}\\\\\n");
            this.nTableRecordsLines++;
            this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
        }
        temp="";
        for(int j=0;j<headV.size();j++){
            temp+=(String)headV.elementAt(j);
            if(j!=headV.size()-1) temp+="&";
        }
        out.add(temp+"\\end\n");
        out.add(temp+"\\endhead\n");
        //loop through the rest of the lines
        for(int j=headIndex+1;j<t.nLines();j++){
            if(t.tableLineAt(j).contains("----")){//dotted line, replace with real line
                out.add("\\hline\n");
                this.nTableRecordsLines++;
                this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
                continue;
            }else if(t.tableLineAt(j).trim().contains("   ")){//line of values to go in the table
                colV.clear();
                inCol=false;
                line=t.tableLineAt(j);
                line=Str.rtrim(line);
                
                //debug
                //System.out.println("In TableMaker: makeTableRecords: line 299: line=");
                //System.out.println("  ->"+line+"<-");
                
                for(int k=0;k<line.length();k++){
                    if(line.charAt(k)==' '&&inCol&&k<line.length()-1){
                        if(line.charAt(k+1)==' '){
                            inCol=false;
                            if(t.bigT(j)) temp=Translator.process(line.substring(start,k));
                            else temp=Translator.translate(line.substring(start,k));
                            colV.add(temp);
                        }
                    }
                    if(line.charAt(k)!=' '){
                        if(!inCol){
                        inCol=true;
                        start=k;
                        }
                        if(k==line.length()-1){
                            if(t.bigT(j)) temp=Translator.process(line.substring(start,k+1));
                            else temp=Translator.translate(line.substring(start,k+1));
                            colV.add(temp);                                    
                        }
                    }
                }//loop through row
                //check if there is the right number of columns
                if(colV.size()<headV.size()){
                    //some of the columns are blank, we need to figure out which ones
                    for(int k=0;k<newColPos.size()-1;k++){
                        if(line.substring((Integer)newColPos.elementAt(k),(Integer)newColPos.elementAt(k+1)).trim().length()<1){
                            if(k<colV.size()) colV.insertElementAt(" ",k);
                            else colV.addElement(" ");
                        }
                        if(colV.size()==headV.size()) break;
                    }
                }
                if(colV.size()>headV.size()){//if too many columns, tack the extra data into the last column
                    for(int k=headV.size();k<=colV.size();k++){
                        colV.setElementAt((String)colV.elementAt(headV.size()-1)+" "+(String)colV.elementAt(headV.size()),headV.size()-1);
                        colV.removeElementAt(headV.size());
                    }
                }
                for(int k=0;k<colV.size();k++){
                    out.add((String)colV.elementAt(k));
                    if(k!=colV.size()-1) 
                    	out.add("&");
                    else {
                    	out.add("\\\\\n");
                    	this.nTableRecordsLines++;
                    	this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
                    }
                }
            }else{
                line=t.tableLineAt(j).trim();
                if(t.bigT(j)) 
                	temp=Translator.process(line);
                else 
                	temp=Translator.translate(line);
                out.add("\\multicolumn{"+nColumns+"}{l}{"+temp+"}\\\\\n");
                this.nTableRecordsLines++;
                this.tableRecordsHeight+=NDSConfig.TABLE_RECORDS_LINEWIDTH;
            }
        }
        out.add("\\end{longtable}\n");
        this.nTableRecordsLines++;
        this.tableRecordsHeight+=NDSConfig.TABLE_BOTTOM_MARGIN;
        
        return out;
    }
    
    
    
	/**find break points based on current line position and current table to be drawn and
	 * set the values in corresponding TableControl
	 * */
	public int findBreaks_old()throws Exception{
		int nbreaks=0,totalBreaks=0;
		int offset=0;
        int maxLineCount=0;
        float maxLinePos=0;
        
		int headLineNo;
		int totalDataLines=0;
		float headLinePos=0;

		
		Vector<String> dataLineLevels,dataLineEnergies;
		Vector<Break> breaks=new Vector<Break>();
		Vector<String> panelColumns;
		
		TablePanel T=remakeTablePanel(-1);//get a single table panel for the whole table
		
		headLineNo=(int) (T.getRelHeadLineNo()+NDSControl.currentLineNo);
		headLinePos=T.getRelHeadLinePos()+NDSControl.currentLinePos;
		
		totalDataLines=T.getTotalDataLines();
		dataLineLevels=T.getDataLineLevels();
		dataLineEnergies=T.getDataLineEnergies();

		//debug
		//System.out.println("In findBreaks: commentColumnWidth(0)="+tc.getCommentColumnWidth(0));
		
		maxLineCount=NDSConfig.MAX_LINE_COUNT;
		maxLinePos=NDSConfig.MAX_TEXT_HEIGHT;
		if(!tc.isPortrait(T)){
			maxLineCount=NDSConfig.MAX_LINE_COUNT_LANDSCAPE;
			maxLinePos=NDSConfig.MAX_TEXT_WIDTH;
		}
		
		NDSControl.breakLineNo=maxLineCount-3;
		NDSControl.breakLinePos=maxLinePos-3*NDSConfig.DEFAULT_LINEWIDTH;
		NDSControl.nsplit=1;
		
		//if(headLineNo>Control.breakLineNo){
		if(headLinePos>NDSControl.breakLinePos){
			NDSControl.currentLineNo=0;
		    NDSControl.currentLinePos=0;
			tc.setNewPage(true);
			headLineNo=T.getRelHeadLineNo();
			headLinePos=T.getRelHeadLinePos();
		}
	
		offset=0;
		findNSplit(T,offset);//nsplit and breakLineNo in Control class is set here (only for portrait mode and no effect for landscape mode)
				
		for(int i=0;i<totalDataLines;i++){
			String levelEnergy=dataLineLevels.elementAt(i);
			String dataEnergy=dataLineEnergies.elementAt(i);

			if(headLineNo+1+i-offset>NDSControl.breakLineNo){
				Break b=new Break();
				
				if(this instanceof GammaTableMaker && (etd.getENSDF().fullDSId().contains("DECAY") || etd.isCombineLG()))
					b.pos=levelEnergy;
				else
					b.pos=dataEnergy;
				
				panelColumns=updatePanelColumns(T,offset,i);//update columns for the panel before this break point
								
				//debug
				//System.out.println("$$$In TableMaker: findBreaks(): paneColumns.size="+panelColumns.size());
				
			    if(totalBreaks==0) 
			    	tc.setFirstPanelColumns(panelColumns);
			    else{
			    	if(panelColumns.size()==1 && panelColumns.elementAt(0).equals("COMM"))
			    		if(totalBreaks==1)
			    			panelColumns=tc.getFirstPanelColumns();
			    		else
			    			panelColumns=breaks.elementAt(totalBreaks-2).panelColumns;
			    	
			    	breaks.elementAt(totalBreaks-1).panelColumns=panelColumns;//for the panel before current break point (for previous break point)
			    }
			    
				nbreaks++;
				offset=i;
				if(nbreaks%NDSControl.nsplit==0){
					b.isNewPage=true;
					findNSplit(T,offset);
					headLineNo=T.getRelHeadLineNo();
					nbreaks=0;
				}
								
				b.panelColumns=tc.getDefaultColumns();
				breaks.add(b);
								
				totalBreaks++;
				
//debug					
				//System.out.println("*In find Breaks3: break pos="+b.pos+" ibreak="+totalBreaks+" newPage="+b.isNewPage+" nsplit="+Control.nsplit+" headLineCount="+headLineCount);
				//System.out.println("        headLineWidth="+headLineWidth+" breakLineCount="+Control.breakLineCount);
				//System.out.println("        columns:"+panelColumns.size()+tc.getDefaultColumns().size());
			}
		}
		
		//update columns for the last panel (after the last break point) or the first panel when there is no break
		panelColumns=updatePanelColumns(T,offset,totalDataLines);
	    if(totalBreaks==0) 
	    	tc.setFirstPanelColumns(panelColumns);
	    else{   	
	    	if(panelColumns.size()==1 && panelColumns.elementAt(0).equals("COMM"))
	    		if(totalBreaks==1)
	    			panelColumns=tc.getFirstPanelColumns();
	    		else
	    			panelColumns=breaks.elementAt(totalBreaks-2).panelColumns;
	    	
	    	breaks.elementAt(totalBreaks-1).panelColumns=panelColumns;//for the panel after last break point
	    }
	    
		tc.setBreaks(breaks);
		
		return nbreaks;
	}
	
	/**find break points based on current line position and current table to be drawn and
	 * set the values of break points in corresponding TableControl.
	 * The idea is that,
	 * scan the whole table, break at regular page break if table is across pages (otherwise, the end of
	 * the table), make a panel from the scanned part, check if it can be split into multiple sub-panels
	 * side-by-side, if yes, scan further down to see if more lines can be split and put in the same page,
	 * reduce the number of split sub-panels (nsplit) by 1 if no success and scan again, if success 
	 * or nsplit=1, find and set break points based on nsplit and count of scanned lines. 
	 * */
	@SuppressWarnings("unused")
	public int findBreaks()throws Exception{
		boolean debug=false;
		//if(!this.tableType.equals("GAMMA"))
		//	debug=false;
		
		int nbreaks=0,totalBreaks=0;
		int start=0,end=0;
		int offset=0;
        int maxLineCount=0;
        float maxLinePos=0;
        
		int headLineNo;
		int totalDataLines=0;
		int nsplit=1;
        int count=0;

		float headLinePos=0;
		float totalDataHeight=0;
		
		Vector<String> dataLineLevels,dataLineEnergies;
		Vector<Break> breaks=new Vector<Break>();
		Vector<String> panelColumns;
		
		boolean needGammaBreak=false;//for gamma table which is in order of level energies, excluding in decay data set, in which gamma rays are sorted in order of gamma ray energies
		TablePanel tempPanel=null;
		
		
		
		//////////////////////////////////////
		TablePanel T=remakeTablePanel(-1);    //get a single table panel for the whole table, with no break points
		/////////////////////////////////////

        
		maxLineCount=NDSConfig.MAX_LINE_COUNT;
		maxLinePos=NDSConfig.MAX_TEXT_HEIGHT;
		if(!tc.isPortrait(T)){
			tc.setPortait(false);
			tc.setNewPage(true);

			//Control.currentLineNo=Control.numberOfAllTitleLines; 
		    //Control.currentLinePos=Control.heightOfAllTitleLines;

		    NDSControl.setTopTitleSpacingInNewPage();//currentLineNo and currentLinePos are set in this call.
		    
    	    //for table title right below top-title
            NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
            NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline
            NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
            
			maxLineCount=NDSConfig.MAX_LINE_COUNT_LANDSCAPE;
			maxLinePos=NDSConfig.MAX_TEXT_WIDTH;
			
			init();//reset the page height and width
		}
		
		//maxLinePos-=0.2;
		
		NDSControl.breakLineNo=maxLineCount-NDSConfig.BOTTOM_GAP_NLINES;//-2 does not work (across pages), counting starts from 0
		NDSControl.breakLinePos=maxLinePos-NDSConfig.BOTTOM_GAP_NLINES*NDSConfig.DEFAULT_LINEWIDTH;//leave space above bottom line
		NDSControl.nsplit=1;
		
		headLineNo=(int) (T.getRelHeadLineNo()+NDSControl.currentLineNo);//currentLineNo is for the last line before making the current table panel
		headLinePos=T.getRelHeadLinePos()+NDSControl.currentLinePos;//not that headLinePos here is for the bottom of head line, currentLinePos is for the top of current line
		
		totalDataLines=T.getTotalDataLines();
		dataLineLevels=T.getDataLineLevels();
		dataLineEnergies=T.getDataLineEnergies();
		//totalDataHeight=totalDataLines*Config.DEFAULT_LINEWIDTH;
		totalDataHeight=T.getTableBodyHeight();

		if(this instanceof GammaTableMaker && (!etd.isSortGammas() || etd.isCombineLG())){// gamma ray is not sorted
			dataLineEnergies=T.getDataLineLevels();
			needGammaBreak=true;
		}

		
		//debug
		//System.out.println("A-1****** offset="+offset+" breakPos="+Control.breakLinePos+" breakLineNo="+Control.breakLineNo+" totalBreaks="+totalBreaks);
		//System.out.println("          currentLinePos="+Control.currentLinePos+" "+Control.currentLineNo+" headLinePos="+headLinePos+" "+headLineNo);
		//System.out.println("          relHeadLinePos="+T.getRelHeadLinePos()+"  "+T.getRelHeadLineNo()+" nTableRecords="+T.getNTableRecordsLines()+" height="+T.getTableRecordsHeight());
		
		float currentLinePos=NDSControl.currentLinePos;
		float currentLineNo=NDSControl.currentLineNo;
		
		float genCommentsHeight=T.getGenComments().size()*NDSConfig.GENNERAL_COMMENT_LINEWIDTH;
		if(T.getGenComments().size()>0)
			genCommentsHeight+=NDSConfig.DEFAULT_LINEWIDTH;//leave one blank line after all comments
		
		float totalHeight=NDSControl.currentLinePos+genCommentsHeight+T.getTableRecordsHeight();;
		float height=NDSControl.currentLinePos+genCommentsHeight;
		float maxHeight=NDSControl.breakLinePos-2*NDSConfig.DEFAULT_LINEWIDTH;
		float titleHeight=(NDSConfig.DEFAULT_HEADLINE_POS+2*NDSConfig.DEFAULT_LINEWIDTH);
		float r=(height-maxHeight)/(maxHeight-titleHeight);//titles will be printed for each new page after pagebreak in comments

		if(genCommentsHeight>0){
			height+=0.5f;//blank line after general comments
			totalHeight+=0.5f;
		}
			
		//general comments
		if(r>0){//general comments are across pages
			currentLinePos=(r-(int)r)*(maxHeight-titleHeight)+titleHeight;
			currentLineNo=(int)(currentLinePos/NDSConfig.GENNERAL_COMMENT_LINEWIDTH);
		}
		else{
			currentLinePos=height;
			currentLineNo=NDSControl.currentLineNo+T.getGenComments().size();
		}
			
		maxHeight=maxLinePos;
		height=currentLinePos+T.getTableRecordsHeight();
		r=height/maxHeight;//currently no title for TableRecords on a new page
		
		//debug
		//System.out.println("A-1.1****** totalHeightBeforeTableTitle="+totalHeight+" maxHeight="+maxHeight+" breakPos="
		//+NDSControl.breakLinePos+" breakLineNo="+NDSControl.breakLineNo+" currentLieNo="+currentLineNo);
		//System.out.println("    tableRecordHeight="+T.getTableRecordsHeight()+" headLineNo="+headLineNo+"   headLinePos="+headLinePos+" T.getRelHeadLinePos()="+T.getRelHeadLinePos());
		
		//table records
		if(totalHeight>=maxHeight){
			//debug
			//System.out.println("A-1.2****** totalHeightBeforeTableTitle="+totalHeight+" maxHeight="+maxHeight+" breakPos="+Control.breakLinePos+" breakLineNo="+Control.breakLineNo);
			//System.out.println("            headLineNo="+headLineNo+"   headLinePos="+headLinePos+" T.getRelHeadLinePos()="+T.getRelHeadLinePos());

			
			
			if(r>=1)//table records are across pages
				headLinePos=(r-(int)r)*maxHeight;
			else
				headLinePos=height;

			//here headLinePos is for the top of the #headLineNo line
			headLineNo=(int)(headLinePos/NDSConfig.TABLE_RECORDS_LINEWIDTH)+3;
			headLinePos+=3*NDSConfig.DEFAULT_LINEWIDTH;
			
			//new page is set and titles are written after writing table records in makeTablePanels (after findBreaks) in LatexWriter
			if(headLinePos>=NDSControl.breakLinePos-2*NDSConfig.DEFAULT_LINEWIDTH){
				headLineNo=NDSConfig.DEFAULT_HEADLINE_NO; 
				headLinePos=NDSConfig.DEFAULT_HEADLINE_POS;
			}
			
		}
		else if(headLinePos>=NDSControl.breakLinePos-2*NDSConfig.DEFAULT_LINEWIDTH){//if remaining space can only hold less than two lines, then start from a new page.
			                                                                  //here headLinePos points to the bottom of the headLine
			//debug
			//System.out.println("A-1.3****** totalHeightBeforeTableTitle="+totalHeight+" maxHeight="+maxHeight+" breakPos="+Control.breakLinePos+" breakLineNo="+Control.breakLineNo);
			//System.out.println("            headLineNo="+headLineNo+"   headLinePos="+headLinePos+" T.getRelHeadLinePos()="+T.getRelHeadLinePos());

			
			if(T.getGenComments().size()+T.getNTableRecordsLines()==0){
				tc.setNewPage(true);                                              //new page is set and titles are written before findBreaks in LatexWriter under the same condition
				headLineNo=T.getRelHeadLineNo()+4;;
				headLinePos=T.getRelHeadLinePos()+3*NDSConfig.DEFAULT_LINEWIDTH+0.1f;//relative pos+(width of top title), see Control.setTotTitleSpacing	
			}else{
				//table records and/or general comments remain in previous page if any, only table body is printed in a new page
				//Note that we cannot do tc.setNewPage(true), since it would print the whole table from top title (before table records and table general comments) in
				//the new page, which is unwanted. We want to print the top title and general comments and/or table records in the current page and the table body
				//in the new page. So this break point (before table body) is out of the scope of findBreaks() and should be implemented when writing the table (in LatexWriter)
				
				//for headline of table body in new page
				headLineNo=NDSConfig.DEFAULT_HEADLINE_NO; 
				headLinePos=NDSConfig.DEFAULT_HEADLINE_POS;	
				tc.setIsBodyInNewPage(true);
			}
			

		}
		
	    //if(headLineNo+totalDataLines<=Control.breakLineNo)
		if(headLinePos+totalDataHeight<=NDSControl.breakLinePos+NDSConfig.DEFAULT_LINEWIDTH/3.0){
	    	NDSControl.breakLineNo=headLineNo+totalDataLines;
	    	NDSControl.breakLinePos=headLinePos+totalDataHeight;
	    	//Control.breakLinePos=headLinePos+totalDataHeight+0.1f-0.2f;//0.1 for underline, -0.2 see in LatexWriter after wiring headline: out.write("[-.2cm]\n");
		}
	    
	    
		offset=0;
		int i=0,morelines=0,nextPanelNo=0;
		int nTableBodyLines=0;
		
		int breakLineNo=NDSControl.breakLineNo;
		float breakLinePos=NDSControl.breakLinePos;
		
		float linePos=0;
		String dataEnergy="",pos="";
		float gammaBreakPos=-100000;
		
		int nTriesOfScan=0,nTriesOfSplit=0;
		boolean done=false;
		
		headLinePos+=0.1f;//pos after headline, 0.1 for underline
				
		float tempPos=headLinePos;

		//debug
		//for(int j=0;j<totalDataLines;j++){
		//	System.out.println("TableMaker line 1142         *dataLevelEnergy["+j+"]="+dataLineLevels.elementAt(j));
		//	System.out.println("                             dataLineEnergy["+j+"]="+dataLineEnergies.elementAt(j));
		//}
        
        Vector<String> tempFixedColumns=new Vector<String>();
        
		while(i<totalDataLines){
			
			if(totalBreaks==0)//for first panel
				headLinePos=tempPos-0.2f;//-0.2 see in LatexWriter after writing headline in first panel: out.write("[-.2cm]\n");
			
			i=breakLineNo-headLineNo+offset;

			if(i<=0){
				breakLineNo++;
				continue;
			}
			
			if(i>=totalDataLines)
				i=totalDataLines-1;
			
			linePos=headLinePos+(i-offset)*NDSConfig.TABLE_BODY_LINEWIDTH;

			if(debug) {
				//debug
				System.out.println("First scan of next lines in an initially-made whole single panel T (initial comment width):");
				System.out.println("A0********i="+i+"  offset="+offset+" linePos="+linePos+" breakPos="+breakLinePos+" breakLineNo="+breakLineNo+" tc.isNewPage="+tc.isNewPage());
				System.out.println("          Control (start of findBreaks):currentLinePos="+NDSControl.currentLinePos+" currentLineNo="+NDSControl.currentLineNo);
				System.out.println("          headLinePos="+headLinePos+" headLineNo="+headLineNo+" tc.isPortrait="+tc.isPortrait());
				System.out.println("          T.getRelHeadLinePos="+T.getRelHeadLinePos()+"  T.getRelHeadLineNo="+T.getRelHeadLineNo());
				System.out.println("          T.dataEnergy["+i+"]="+dataLineEnergies.elementAt(i));
				System.out.println("          initial bodyheight="+(linePos-headLinePos)+" initial nTableBodyLines="+(i-offset));
				System.out.println("          T.commentColumnWidth="+T.getCommentColumnWidth()+" T.dataWidth="+T.getDataWidth()+
						" dataLength="+(int)(T.getDataWidth()/NDSConfig.CHAR2CM));
				System.out.println("          T.totalDataLine="+totalDataLines+" totalBreaks="+totalBreaks);
				System.out.println("          line="+T.getBody().get(i));
			}

			
			if(i<totalDataLines-1){
				if(linePos<breakLinePos-NDSConfig.DEFAULT_LINEWIDTH && nTriesOfScan==0 && nTriesOfSplit==0){
					breakLineNo++;
				    i=offset;
				    continue;
			    }
			}
			
			dataEnergy=dataLineEnergies.elementAt(i);             
			count=i-offset;
				
			if(count<=0) break;	
			
			Break b=new Break();
			b.pos=dataEnergy;	//entry at break point is printed in next page
			if(needGammaBreak)
				b.gammaPos=Str.parseFloatEnergy(T.getDataLineEnergies().elementAt(i));
			
			if(i==totalDataLines-1){
				b.pos=String.valueOf(Str.parseFloatEnergy(b.pos)+0.5);//set a temporary break point marking the end of the data table
			                                                      //and it will be removed from the break points at the end
				if(needGammaBreak) b.gammaPos=b.gammaPos+0.5f;
			}
			
			//System.out.println("1 nTriesScan="+nTriesOfScan+" nTriesSplit="+nTriesOfSplit+" i="+i+" count="+count+" linePos="+linePos);
			
			b.setPanelColumns(tc.getDefaultColumns());
			
			start=offset;
			end=i-1; //entry at break point is printed in next page
			
			if(i==totalDataLines-1)
				end+=1;
			
			//debug
			//System.out.println("TableMaker 1202: data energy="+dataLineEnergies.get(start)+"  to "+dataLineEnergies.get(end));
			//for(int k=0;k<tc.getDefaultColumns().size();k++)
			//	System.out.println(" In TableMaker line 1203: column name+"+k+"="+tc.getDefaultColumns().elementAt(k));	
			//for(int k=0;k<T.getColumns().size();k++)
			//	System.out.println(" In TableMaker line 1205: column name+"+k+"="+T.getColumns().elementAt(k));

			
			panelColumns=updatePanelColumns(T,start,end);//update columns for the panel before this break point	
			
			//debug
			//if(this instanceof LevelTableMaker)
			//for(int k=0;k<panelColumns.size();k++)
			//	System.out.println(" In TableMaker line 1212: "+this.tableType+" panel="+totalBreaks+" column name+"+k+"="+panelColumns.elementAt(k)+" size="+panelColumns.size());
	        //for(int k=0;k<tc.getDefaultFixedColumns().size();k++)
	        //    System.out.println(" In TableMaker line 1214: fixed column name+"+k+"="+tc.getDefaultFixedColumns().elementAt(k));
	         
			//debug
			//System.out.println("In TableMaker line 882: i="+i+" offset="+offset+" panel="+totalBreaks+" break="+b.pos+" ncolumns="+panelColumns.size()+" start="+start+" end="+end);
			
			if(!NDSControl.autoAdjust && totalBreaks>=0){//apply customized panelColumns from manual settings
				int panel=totalBreaks;
				Vector<String> customizedColumns=tc.getPanelColumns(panel);
								
				if(customizedColumns.size()<=panelColumns.size()){
					panelColumns.clear();
					panelColumns.addAll(customizedColumns);
				}
					
			}
			//for(int k=0;k<panelColumns.size();k++)
			//	System.out.println(" In TableMaker line 1232: column name+"+k+"="+panelColumns.elementAt(k)+" size="+panelColumns.size());
			
			if(totalBreaks==0){//the first break point
				tc.setFirstPanelColumns(panelColumns);//tc.firstPanelColumns will be used in remakeTablePanel(0), if not set, it will report error
			                                          //here it is just temporary and it will be finally reset after all break points are foun				
			}
			else{
		    	if(panelColumns.size()==1 && panelColumns.elementAt(0).equals("COMM")){
		    	    //note that this is the case only if there is no fixedColumns set in the TableControl object=tc
		    	    //fixedColumns are columns (non-comm column) that are set to always display and they are initially set when a TableControl
		    	    //object is initialized in EnsdfTableData
		    		if(totalBreaks==1)//the second break point, breakNo=1, current panelNo=2 (start from 0); break0 for panel1, break1 for panel2, ...
		    			panelColumns=tc.getFirstPanelColumns();
		    		else
		    			panelColumns=tc.getBreaks().elementAt(totalBreaks-2).panelColumns;
		    	}
		    	
				tc.getBreaks().elementAt(totalBreaks-1).setPanelColumns(panelColumns);//for the panel before current break point (for previous break point)
				
			}
			
			//debug
			//for(int k=0;k<panelColumns.size();k++)
			//	System.out.println(" In TableMaker line 1068: column name+"+k+"="+panelColumns.elementAt(k));		
			//for(int k=0;k<tc.getFirstPanelColumns().size();k++)
			//	System.out.println(" In TableMaker line 1072: column name+"+k+"="+tc.getFirstPanelColumns().elementAt(k));
			
			morelines=0;
						
			tc.setBreak(totalBreaks, b);//set temporary break point for making a temporary panel (the global "tc" will be used in remakeTablePanel)			
			
			//debug
			//nextPanelNo=totalBreaks+1;
			//System.out.println("#####In TableMaker: findBreaks line 1158: panel="+totalBreaks+" i="+i+" start="+start+" end="+end);
			//System.out.println("             getLastLineLevelEnergy(nextPanelNo-1)="+tc.getLastLineLevelEnergy(nextPanelNo-1));
			//System.out.println("             getFirstLineLevelEnergy(nextPanelNo)="+tc.getFirstLineLevelEnergy(nextPanelNo));
			//System.out.println("             getLastLineEnergy(nextPanelNo-1)="+tc.getLastLineEnergy(nextPanelNo-1));
			//System.out.println("             getFirstLineEnergy(nextPanelNo)="+tc.getFirstLineEnergy(nextPanelNo));
			//System.out.println("      firstLine="+tc.getFirstLine(nextPanelNo-1));
			//System.out.println("       lastLine="+tc.getLastLine(nextPanelNo-1));
			
			tempPanel=remakeTablePanel(totalBreaks);//after remake, the height of the panel is recalculated based on actual line width of each line and
			                                        //the total lines and height of this panel could shrink leaving a blank at the page bottom.
			                                        //To solve this, check the lastLineNo against breakLineNo and last linePos against breakLinePos, 
			                                        //if less, go back and start over
			
			
			
			//debug
			//for(int k=0;k<tempPanel.getColumns().size();k++)
			//	System.out.println(" In TableMaker line 1079: column name+"+k+"="+tempPanel.getColumns().elementAt(k)+" isBreakAtLastCom="+tempPanel.isBreakAtLastCom());
			//System.out.println("   in TableMaker line 1171: nextPanelNo="+nextPanelNo);
			//System.out.println("             getLastLineLevelEnergy(nextPanelNo-1)="+tc.getLastLineLevelEnergy(nextPanelNo-1));
			//System.out.println("             getFirstLineLevelEnergy(nextPanelNo)="+tc.getFirstLineLevelEnergy(nextPanelNo));
			//System.out.println("             getLastLineEnergy(nextPanelNo-1)="+tc.getLastLineEnergy(nextPanelNo-1));
			//System.out.println("             getFirstLineEnergy(nextPanelNo)="+tc.getFirstLineEnergy(nextPanelNo));
			//System.out.println("      firstLine="+tc.getFirstLine(nextPanelNo-1));
			//System.out.println("       lastLine="+tc.getLastLine(nextPanelNo-1));
			
			nTableBodyLines=tempPanel.getNTableBodyLines();
			//linePos=headLinePos+nTableBodyLines*Config.DEFAULT_LINEWIDTH;//less precise, due to different linewidths in table body
			linePos=headLinePos+tempPanel.getTableBodyHeight();
			
			//in makeTablePanel(), if the break point is the last comment line (ncom>1) of a gamma/level/decay/decay which is the first line in next panel,
			//that line has moved to current panel. So breakLineNo should be increased by one.
			
			if(debug) {
				//debug
				System.out.println("After remaking the panel with the first-scanned lines of data with initial comment width (new comment width determined and used):");
				System.out.println("         need to scan more lines if the new comment width becomes wider so that less lines are used to leave extra space");
				System.out.println("A*********i="+i+"  offset="+offset+" linePos="+linePos+" breakPos="+breakLinePos+" breakLineNo="+breakLineNo+" totalBreaks="+totalBreaks);
				System.out.println("          nColumns(val+unc=2 cols)="+tc.getPanelColumns(totalBreaks).size());
				System.out.println("          Control: currentLinePos="+NDSControl.currentLinePos+" "+NDSControl.currentLineNo+" headLinePos="+headLinePos+" "+headLineNo);
				System.out.println("          T.getRelHeadLinePos="+T.getRelHeadLinePos()+" T.getRelHeadLineNo="+T.getRelHeadLineNo());
				System.out.println("          T.dataEnergy["+i+"]="+dataLineEnergies.elementAt(i)+" b.pos="+b.pos+" b.gammaPos="+b.gammaPos+" nTableBodyLines="+nTableBodyLines);
				System.out.println("          T.dataEnergy["+end+"]="+dataLineEnergies.elementAt(end)+" prev="+dataLineEnergies.elementAt(end-1));
				System.out.println("          T.totalDataLines="+totalDataLines+"  ntries="+nTriesOfScan+" end="+end+" count="+count+" tc.isPortrait="+tc.isPortrait());
				System.out.println("          T.lastline="+T.getBody().get(end));
				System.out.println("          new bodyheight="+tempPanel.getTableBodyHeight()+" new nTableBodyLines="+nTableBodyLines+" isBreakAtLastCom="+tempPanel.isBreakAtLastCom());
				System.out.println("          new commentColumnWidth(panel="+totalBreaks+")="+tc.getCommentColumnWidth(totalBreaks)+" dataWidth="+tempPanel.getDataWidth()+
						" dataLength="+(int)(tempPanel.getDataWidth()/NDSConfig.CHAR2CM));
				
				if(totalBreaks==2 || totalBreaks==1) {
					for(int k=0;k<tempPanel.getBody().size();k++) {
						String line=tempPanel.getBody().get(k);
						System.out.println("New Panel line#"+k+": "+line);
					}
				}
			}

	         if(tempPanel.isBreakAtLastCom() && linePos<breakLinePos+1*NDSConfig.DEFAULT_LINEWIDTH){
                 breakLineNo++;
                 i++;
                 count++;
                 end=i-1;
                 if(i==totalDataLines-1)
                     end+=1;

                 if(i>=totalDataLines-1){
                     break;
                 }
            
	         }
	         
			//debug
			//for(int m=0;m<tempPanel.getBody().size();m++){
			//	System.out.println(" line E=:"+tempPanel.getDataLineEnergies().get(m)+" "+tempPanel.getBody().get(m));
			//}
	         
	         //System.out.println("2 nTriesScan="+nTriesOfScan+" nTriesSplit="+nTriesOfSplit+" i="+i+" count="+count+" linePos="+linePos);
	         
			//if(nTableBodyLines+headLineNo<Control.breakLineNo-1 && i<totalDataLines-1){
			int njump;
			//if(linePos<breakLinePos-0.5*NDSConfig.DEFAULT_LINEWIDTH && i<totalDataLines-1 && nTriesOfScan==0 && nTriesOfSplit==0){
			if(linePos<breakLinePos-0.5*NDSConfig.DEFAULT_LINEWIDTH && i<totalDataLines-1 && nTriesOfScan<=3 && nTriesOfSplit==0){
				i=offset;
				njump=(int)((breakLinePos-linePos)/NDSConfig.DEFAULT_LINEWIDTH);
				
				njump=njump-nTriesOfScan;
				
				if(njump<=0) njump=1;
				breakLineNo+=njump;
				
				nTriesOfScan++;
				
				continue;
			}
			else if(linePos>breakLinePos+0.5*NDSConfig.DEFAULT_LINEWIDTH && nTriesOfScan<10){
				i=offset;
				njump=(int)((linePos-breakLinePos)/NDSConfig.DEFAULT_LINEWIDTH);
				njump=njump/3;
				
				if(njump==0) njump=1;
				breakLineNo-=njump;
				nTriesOfScan++;//set to break from the infinite loop when linePos<breakLinePos-Config.DEFAULT_LINEWIDTH above after breakLineNo--
				
			    continue;
			}else if(linePos<breakLinePos-3*NDSConfig.DEFAULT_LINEWIDTH && panelColumns.contains("COMM") && nTriesOfScan>0) {//not splitable if there is "COMM" column
			    
	            //It could happen that in the first scan with initial columns and comment width for the current panel, the lines can fill to the bottom 
	            //till the default page break position, but after re-making the panel with the scanned lines and updated columns if there is empty 
	            //columns being removed, which results in a much wider comment width (squeeze more comment text in one line), the total lines of this panel
	            //reduce by a large number of lines, which are left blank. Then the program continue to scan more lines to fill the blank, but if 
	            //there are more columns (extra columns that are previous empty but not empty now), after re-making the panel with extra lines, the new
	            //comment width becomes narrow again making the line number increase, resulting in the new panel not being able to hold all the new lines.
	            //So then the program starts to jump back (ntries<10). In the end, the final end of the current panel could come back to the original point, 
	            //with a large number of blank lines at bottom. So, to solve this situation, a column from the empty columns in the current panel can be added
	            //to the fixed columns (always printed).
			                   
                //debug
				//System.out.println("TableMaker 1379: data energy="+dataLineEnergies.elementAt(i));
                //for(String col:tc.getDefaultFixedColumns())
                //    System.out.println(" ##  In TableMaker 1381: fixed column="+col);
                
                boolean toContinue=false;
                
                /*
                for(String col:T.getColumns()) {
                    if(!col.startsWith("D") && !panelColumns.contains(col)) {
                        tc.getDefaultFixedColumns().add(col);                       
                        tempFixedColumns.add(col);
                        
                       
                        //String s=col.substring(1);
                        //if(T.getColumns().contains(s) && !panelColumns.contains(s)) {
                        //    tc.getDefaultFixedColumns().add(s);                       
                        //    tempFixedColumns.add(s);
                        //}
                        
                        
                        toContinue=true;
                        
                        break;
                    }
                }
                */

                //for(String col:tc.getDefaultFixedColumns())
                //    System.out.println(" &&  In TableMaker 1405: fixed column="+col);
                
                if(toContinue) {
                    i=offset;               
                    continue;
                }
                
			}
			

			//System.out.println(" #### ntries="+ntries);
			//System.out.println("3 nTriesScan="+nTriesOfScan+" nTriesSplit="+nTriesOfSplit+" i="+i+" count="+count+" linePos="+linePos);
			
			nTriesOfScan=0;
			
			if(debug) {
				System.out.println("\nAfter scanning extra lines to fill the extra space due to less lines used with the wider new comment width");
				System.out.println("B*********i="+i+"  offset="+offset+" linePos="+linePos+" breakPos="+breakLinePos+" totalBreaks="+totalBreaks);
				System.out.println("          Control: currentLinePos="+NDSControl.currentLinePos+" currentLineNo="+NDSControl.currentLineNo);
				System.out.println("          headLinePos="+headLinePos+" headLineNo="+headLineNo+" tc.isPortrait="+tc.isPortrait());
				System.out.println("          T.getRelHeadLinePos="+T.getRelHeadLinePos()+"  T.getRelHeadLineNo="+T.getRelHeadLineNo());
				System.out.println("          T.dataEnergy["+i+"]="+dataLineEnergies.elementAt(i)+" b.pos="+b.pos+" b.gammaPos="+b.gammaPos+" nTableBodyLines="+nTableBodyLines);
				System.out.println("          new bodyheight="+tempPanel.getTableBodyHeight()+" new nTableBodyLines="+nTableBodyLines);
				System.out.println("          new commentColumnWidth(panel="+totalBreaks+")="+tc.getCommentColumnWidth(totalBreaks)+" dataWidth="+tempPanel.getDataWidth()+
						" dataLength="+(int)(tempPanel.getDataWidth()/NDSConfig.CHAR2CM));	
			}

            			
			nextPanelNo=totalBreaks+1;
			
			if(i<totalDataLines-1){
				if(needGammaBreak){//gamma rays are not sorted and arranged in order of level energies
					b.pos=String.valueOf(tc.getFirstLineLevelEnergy(nextPanelNo));//firstLineLevelEnergy and firstLineEnergy of next panel are set in makeTablePanel of current one
					
					/////////////////////////
					double el1=tc.getLastLineLevelEnergy(nextPanelNo-1);
					double el2=tc.getFirstLineLevelEnergy(nextPanelNo);
					if(el1<el2) {
						b.pos=String.valueOf(tc.getLastLineLevelEnergy(nextPanelNo-1));
						float eg1=tc.getLastLineEnergy(nextPanelNo-1);
						float eg2=tc.getFirstLineEnergy(nextPanelNo);
						if(Math.abs(el1-el2)<=0.1) {
							if(eg1<eg2) {
								b.gammaPos=eg1+(eg2-eg1)/2.0f;
							}else {
								b.gammaPos=eg1+0.5f;
							}
						}else {
							b.gammaPos=eg1+0.5f;
						}
					}else {
						b.gammaPos=tc.getFirstLineEnergy(nextPanelNo);
					}
					///////////////////////////////////
					/*
					//if(tc.getLastLineLevelEnergy(nextPanelNo-1)<tc.getFirstLineLevelEnergy(nextPanelNo) && !tc.isLastLevelSameAsNextFirst(nextPanelNo-1) ){//first line in next panel is different level
					if(tc.getLastLineLevelEnergy(nextPanelNo-1)<tc.getFirstLineLevelEnergy(nextPanelNo) ){//first line in next panel is different level
						b.pos=String.valueOf(tc.getLastLineLevelEnergy(nextPanelNo-1));
						b.gammaPos=tc.getLastLineEnergy(nextPanelNo-1)+0.5f;
					}
					else{
						b.gammaPos=tc.getFirstLineEnergy(nextPanelNo);//gamma ray energy
					}
					*/
				}else {	
			        b.pos=String.valueOf(tc.getFirstLineEnergy(nextPanelNo));//it should be set in above remakeTablePanel() by corresponding TableMaker
				}                                                      //getFirstLineEnergy(panel) always return the first DataLineEnergy in panelNo=panel
			                                                                 //for unsorted gamma table in order of level energies, the break point should set 
			                                                                 //with level energy, and additional gamma break is with gamma energy (DataLineEnergy)
			}
			
			
			if(count>5 && totalDataLines>10)
				nsplit=checkSplitable(tempPanel);
			else
				nsplit=1;

			
			if(debug) {
				//debug
				System.out.println("Before finding table spliting with extra scanned lines and continuing to scan more lines if splitable");
				System.out.println("In findBreaks while loop: i="+i+" offset="+offset+" dataLineEnergy="+dataLineEnergies.elementAt(i)+" b.pos="+b.pos+" b.gammaPos="+b.gammaPos+" headLineNo="+headLineNo+" breakLineNo="+breakLineNo+" nsplit="+nsplit);
		        System.out.println("                          count="+count+" totalLines="+totalDataLines);
				System.out.println("                          linePos="+linePos+"  headLinePos="+headLinePos+" breakLinePos="+breakLinePos+" totalBreaks="+totalBreaks);
				System.out.println("                          nTableBodyLines="+tempPanel.getNTableBodyLines()+" nextPanel="+nextPanelNo+": firstLine="+tc.getFirstLineEnergy(nextPanelNo));
				System.out.println("                          currPanel="+totalBreaks+":  lastLine="+tc.getLastLineEnergy(nextPanelNo-1));
				System.out.println("             getLastLineLevelEnergy(nextPanelNo-1)="+tc.getLastLineLevelEnergy(nextPanelNo-1));
				System.out.println("             getFirstLineLevelEnergy(nextPanelNo)="+tc.getFirstLineLevelEnergy(nextPanelNo));
				System.out.println("             getLastLineEnergy(nextPanelNo-1)="+tc.getLastLineEnergy(nextPanelNo-1));
				System.out.println("             getFirstLineEnergy(nextPanelNo)="+tc.getFirstLineEnergy(nextPanelNo));
				System.out.println("      firstLine (new)="+tc.getFirstLine(nextPanelNo-1));
				System.out.println("      lastLine (new)="+tc.getLastLine(nextPanelNo-1));
			}

			
			Break tempBr=b.clone();
			
			if(totalDataLines-offset>=count){//scan more lines to see if that panel is splitable and can be merged with the previous tempPanel
			    boolean toSecondTry=false;
			    int nMoveBack=0;
				while(nsplit>1){
					morelines=count*nsplit-count;
					pos="";
					
					if(toSecondTry){
						morelines=morelines-nMoveBack;
						pos=dataLineEnergies.elementAt(i+morelines);
						if(needGammaBreak)
							gammaBreakPos=Str.parseFloatEnergy(T.getDataLineEnergies().elementAt(i+morelines));
					}else{
						if(count*nsplit>=totalDataLines-offset){
							morelines=totalDataLines-offset-count-nMoveBack;
						    pos=dataLineEnergies.lastElement();
						    pos=String.valueOf(Str.parseFloatEnergy(pos)+0.5);
						    if(needGammaBreak)
						    	gammaBreakPos=Str.parseFloatEnergy(T.getDataLineEnergies().lastElement())+0.5f;
						    
						}else{
							pos=dataLineEnergies.elementAt(i+morelines);
							if(needGammaBreak)
								gammaBreakPos=Str.parseFloatEnergy(T.getDataLineEnergies().elementAt(i+morelines));
						}
					}

					
					tc.setBreakPoint(totalBreaks, pos);//set temporary break point for making a temporary panel
					if(needGammaBreak)
						tc.setGammaBreakPoint(totalBreaks, gammaBreakPos);
					
					//set the panel columns after more lines are scanned, temporarily.
					//In remakeTablePanel, panel columns are from tc.getPanelCoumns(nPanel), which
					//always return the panelCoumns that has been previously set if not empty. But it could be that
					//after scanning more lines more columns show up than the previously-set panelColumns has.
					//At the end, the panelColumns will be reset based on final breaks points
					
					//for panel#=totalBreaks before current break point ibreak=totalBreaks; firstPanel if current panel is
					//the first panel, ibreak=0
					//System.out.println("In TableMaker line 1232: i="+i+"  end="+end+" totalDataLines="+totalDataLines+" morelines="+morelines);
					
					panelColumns=updatePanelColumns(T,start,end+morelines);
					tc.setPanelColumns(totalBreaks,panelColumns);
								
					tempPanel=remakeTablePanel(totalBreaks);
					
				
					int nsplit1=checkSplitable(tempPanel);
					
					//debug
					if(debug) {
						for(int k=0;k<tempPanel.getColumns().size();k++)
							System.out.println(" In TableMaker line 1521: column name"+k+"="+tempPanel.getColumns().elementAt(k));
						
						//debug
						System.out.println("In findBreaks 1524: scan more lines: i="+i+" offset="+offset+" morelines="+morelines+" pos="+pos+" dataLineEnergy="+dataLineEnergies.elementAt(i)+" b.pos="+b.pos+" b.gammaPos="+b.gammaPos+" nsplit="+nsplit+" split1="+nsplit1);
						System.out.println("                                 headwidth="+tempPanel.getHeadWidth()+" totalBreaks="+totalBreaks+" totalDataLines="+totalDataLines);
						System.out.println("                                 datawidth="+tempPanel.getDataWidth()+" totalWidth="+tempPanel.getTotalWidth());
						//System.out.println("       firstLine="+tempPanel.getBody().firstElement());
						//System.out.println("        lastLine="+tempPanel.getBody().lastElement());		
						System.out.println(" !toSecondTry && (totalDataLines-offset)>=(count*nsplit-1)="+(!toSecondTry && (totalDataLines-offset)>=(count*nsplit-1)));
					}


					if(totalDataLines>10){
						if(nsplit1>=nsplit){//the further scanned panel can be also split the same way as previous tempPanel and so they can be merged
							break;
						}else if(!toSecondTry && (totalDataLines-offset)>=(count*nsplit-1)){
							//second try by moving back by two lines
							//(totalDataLines-offset) is the number of remaining lines
							nMoveBack=2;
							toSecondTry=true;
							continue;
						}
					}

					
					nsplit-=1;//the further scanned panel can not be split the same way as previous tempPanel, so reduce the number of subpanel to split into
					          //and scan again more lines	
					
					toSecondTry=false;
					nMoveBack=0;
				}
			}
			
			if(nsplit==1){
				morelines=0;
				b=tempBr.clone();
			}
			
			//System.out.println("4 nTriesScan="+nTriesOfScan+" nTriesSplit="+nTriesOfSplit+" i="+i+" count="+count+" linePos="+linePos);
			
			int n=(count+morelines)/nsplit; //NOTE that the break point line is included in the new panel. (first line in new panel)
			if((count+morelines)%nsplit>0)
				n=n+1;

			
			Vector<Break> breaks0=new Vector<Break>();
			int totalBreaks0=totalBreaks;
			breaks0.addAll(breaks);
			
			for(int j=0;j<nsplit;j++){
				Break br=b.clone();
				
				start=j*n+offset;
				end=n+j*n+offset-1;//index of last entry in current panel
				if(end>=totalDataLines-2)
					end=totalDataLines-1;
				else if(j==nsplit-1 && end>(count+morelines-1+offset))
					end=count+morelines-1+offset;
						        
				
				
				panelColumns=updatePanelColumns(T,start,end);//return a clone of T.columns
				
				//System.out.println("In TableMaker line 1463: start="+start+" end="+end+" panel="+totalBreaks+"  is last panel's break at lastCommentLine:"+tempPanel.isBreakAtLastCom());
				//System.out.println("                         last line in last panel="+T.getBody().elementAt(end));
				
				//debug
				//for(int k=0;k<panelColumns.size();k++)
				//	System.out.println(" In TableMaker line 1468: panel="+totalBreaks+" start="+start+" end="+end+" column name"+k+"="+panelColumns.elementAt(k));
		        
				
				if(!NDSControl.autoAdjust && totalBreaks>=0){//apply customized panelColumns from manual settings
					int panel=totalBreaks;
					Vector<String> customizedColumns=tc.getPanelColumns(panel);
					if(customizedColumns.size()<=panelColumns.size()){
						panelColumns.clear();
						panelColumns.addAll(customizedColumns);
					}
						
				}
		        
				//debug
				//for(int k=0;k<panelColumns.size();k++)
				//	System.out.println(" In TableMaker line 1294: column name"+k+"="+panelColumns.elementAt(k));
				
				if(totalBreaks==0)
					tc.setFirstPanelColumns(panelColumns);
				else{
			    	if(panelColumns.size()==1 && panelColumns.elementAt(0).equals("COMM")){
			    		if(totalBreaks==1)
			    			panelColumns=tc.getFirstPanelColumns();
			    		else
			    			panelColumns=breaks.elementAt(totalBreaks-2).panelColumns;
			    	}
			    	
					breaks.elementAt(totalBreaks-1).setPanelColumns(panelColumns);//for the panel before current break point (for previous break point)
				}
								
				if(j==nsplit-1) { 
					if((n+j*n+offset)>=totalDataLines)
						break;
					else
						br.isNewPage=true;
				}

				//if comment column does not exist, remakeTablePanel will not change line counting, otherwise line-counting could be different 
				//in individual panel from that of the single table of whole table data
				if(nsplit>1) {
					if(n+j*n+offset>=totalDataLines || (j==nsplit-1 && n*nsplit>=(morelines+count))){
						br.pos=b.pos;
					    br.gammaPos=b.gammaPos;	
					}
					else{					
						br.pos=dataLineEnergies.elementAt(n+j*n+offset);
						if(needGammaBreak)
							br.gammaPos=Str.parseFloatEnergy(T.getDataLineEnergies().elementAt(n+j*n+offset));						
					}

				}
				else{
					br.pos=b.pos;
				    br.gammaPos=b.gammaPos;
				}
				
				breaks.add(br);
				
				if(debug) {
					//debug
					System.out.println("\n@@@ Here finally-determined break point (note: pos is with respect to each panel after remaking the panel\n"
							+ "   with new comment width, not to the total single panel, which is only used for scanning lines to fill a new panel)");
					System.out.println("*****In find Breaks: totalBreaks="+totalBreaks+" break pos="+br.pos+" nsplit="+nsplit+" j="+j+" newPage="+br.isNewPage);
					System.out.println("                                                    b.pos="+b.pos+" b.gammaPos="+b.gammaPos);
					System.out.println("                     breaks.size="+breaks.size()+" gamma.pos="+br.gammaPos+" dataLine index="+(n+j*n+offset)+" dataLineEnergy="+T.getDataLineEnergies().elementAt(n+j*n+offset));
					System.out.println("                     i="+i+" # lines="+n+"  offset="+offset+" morelines="+morelines+" headLineNo="+headLineNo+" headLinePos="+headLinePos);
					System.out.println("                     Control.currentLineNo="+NDSControl.currentLineNo+" Control.currentLinePos="+NDSControl.currentLinePos+" calculated linePos="+linePos);
					System.out.println("                     Control.breakLineNo="+NDSControl.breakLineNo+" breakLinePos="+NDSControl.breakLinePos+" last dataline="+dataLineEnergies.lastElement());
					System.out.println("                     T.getRelHeadLinePos="+T.getRelHeadLinePos()+"  "+T.getRelHeadLineNo()+" nTableRecords="+T.getNTableRecordsLines()+" height="+T.getTableRecordsHeight());
					System.out.println("                     isPortrait="+tc.isPortrait()+" line size="+dataLineEnergies.size());
					System.out.println("                     T.firstDataLine="+T.getBody().elementAt(j*n+offset));
					System.out.println("                     T.lastDataLine="+T.getBody().elementAt(j*n+offset+n));
					System.out.println("                     new commentColumnWidth(panel="+totalBreaks+")="+tc.getCommentColumnWidth(totalBreaks)+" dataWidth="+tempPanel.getDataWidth()+
							" dataLength="+(int)(tempPanel.getDataWidth()/NDSConfig.CHAR2CM));
					
					if(totalBreaks==2) {
						for(int k=j*n+offset;k<j*n+offset+n;k++)
							System.out.println("    line#"+k+"="+T.getBody().elementAt(k));
					}
					for(int k=0;k<panelColumns.size();k++)
						System.out.println(" In TableMaker line 1638: panel="+totalBreaks+" start="+start+" end="+end+" column name"+k+"="+panelColumns.elementAt(k));
					
					//debug
					//if(i>500 && i<600) 
					//	for(int m=500;m<i+n;m++) System.out.println(" line="+T.getBody().get(m));
				}

				
				tc.setBreak(totalBreaks, br);//temporary
				totalBreaks++;

			}//end nsplit loop

			if(nsplit>1) {
				tempPanel=null;
				
				TablePanel tempMergedPanel=null;
				Vector<String> panelColumns0=new Vector<String>();
				panelColumns0.addAll(tc.getPanelColumns(totalBreaks0));
				
				for(int j=0;j<nsplit;j++){	
								
					tempPanel=makeTablePanel(totalBreaks0+j);
					
					if(j==0)
						tempMergedPanel=tempPanel;
					else {
						//note that in makeTablePanel(), columns of TableControl assigned to tempPanel,
						//all changed made to columns of tempPanel later will actually apply to columns of TableControl
						//So here the columns of the first panel will become the columns of the merged panel, which is
						//not what is expected (each column is expected to be associated with each sub-panel)
						tempMergedPanel.Merge(tempPanel,true);
						
						//System.out.println(tempPanel.getNColumns()+" "+tempMergedPanel.getNColumns());
					}


					//System.out.println(" tempPanel height="+tempPanel.getTableBodyHeight()+" merge panel height="+tempMergedPanel.getTableBodyHeight());
				}

				//restore the columns of the sub-panel
				tc.setPanelColumns(totalBreaks0,panelColumns0);
				
				//System.out.println("5 nTriesScan="+nTriesOfScan+" nTriesSplit="+nTriesOfSplit+" i="+i+" count="+count+" linePos="+linePos
				//		+" merged H="+tempMergedPanel.getTableBodyHeight());
				
				//the height of the merged panel could be greater than the height of any sub-panel when
				//some panel has superscript and some doesn't in the same lines, since the line that doesn't
				//have the superscipt in a table will be automatically adjusted to match the max line height
				//which is from the line with superscipt in another table
				linePos=headLinePos+tempMergedPanel.getTableBodyHeight();
				if(linePos>breakLinePos+0.5*NDSConfig.DEFAULT_LINEWIDTH && nTriesOfSplit<10){
					i=offset;
					breakLineNo-=1;
					nTriesOfSplit++;//set to break from the infinite loop when linePos<breakLinePos-Config.DEFAULT_LINEWIDTH above after breakLineNo--
					
					totalBreaks=totalBreaks0;
					
					//System.out.println(" ntries="+nTriesOfSplit+" headLinePos="+headLinePos+" linePos="+linePos+" breakLinePos="+breakLinePos);
					//System.out.println("    break0 size="+breaks0.size()+"  break size="+breaks.size()+" tc breaks size= "+tc.getBreaks().size());
					
					breaks.clear();
					breaks.addAll(breaks0);
					tc.setBreaks(breaks0);
					
				    continue;
				}	

			}
			//System.out.println("2*****In find Breaks: i="+i+" morelines="+morelines+" offset="+offset+"  totalBreaks1="+totalBreaks+" tc.getBreakPoints.size="+tc.getBreakPoints().size());
			//System.out.println("In TableMaker: findBreaks line 1377: panel="+totalBreaks+" i="+i+" start="+start+" end="+end);

			
			//remove temporarily added fixed columns when making the current panel
			for(String col:tempFixedColumns)
			    tc.getDefaultFixedColumns().remove(col);
			
			tempFixedColumns.clear();
			
			//for next panel if exist
			offset=i+morelines;
			headLineNo=NDSConfig.DEFAULT_HEADLINE_NO;
			headLinePos=NDSConfig.DEFAULT_HEADLINE_POS;
			breakLineNo=NDSControl.breakLineNo;
			breakLinePos=NDSControl.breakLinePos;
		}

		
		//debug
		if(debug) {
			System.out.println("After scaning all lines and finding all breaks:");
			System.out.println("*****In find Breaks: before remove last one: totalBreaks="+totalBreaks+" needGammaBreak="+needGammaBreak+" nsplit="+nsplit);
			//System.out.println("                     breaks.lastElement().pos="+breaks.lastElement().pos+" breaks.lastElement().gammaPos="+breaks.lastElement().gammaPos+" lastDataLine="+T.getDataLineEnergies().lastElement());
			//System.out.println("                     lastBodyLine="+T.getBody().lastElement());
			//System.out.println("                     lastLevelEnergy="+T.getDataLineLevels().lastElement());
			//if(this instanceof LevelTableMaker) System.out.println("  tc columns="+tc.getPanelColumns(0).size());
		}

		
		boolean removeLastBreak=false;
		if(totalBreaks>0){
		    
			if(!needGammaBreak){//sorted by gamma
				if(Str.parseFloatEnergy(breaks.lastElement().pos)>=Str.parseFloatEnergy(dataLineEnergies.lastElement()))//gamma rays are sorted,b.pos=b.gammaPos
					removeLastBreak=true;
			}else if(Str.parseFloatEnergy(breaks.lastElement().pos)>=Str.parseFloatEnergy(T.getDataLineLevels().lastElement())){//sorted by level			
				if(breaks.lastElement().gammaPos>=Str.parseFloatEnergy(T.getDataLineEnergies().lastElement()))//gamma rays are not sorted,b.pos=level energy
					removeLastBreak=true;
			}
		
			
			if(removeLastBreak ){
				breaks.remove(breaks.size()-1);
				if(totalBreaks==breaks.size()+1)
				    totalBreaks--;
				else
					System.out.println("There is something wrong in findBreaks for counting totalBreaks!");
			}

		}
			
		//for(int d=0;d<dataLineEnergies.size();d++)
		//	System.out.println("TableMaker 1770: index="+d+"  data line energy "+dataLineEnergies.get(d));
		
		//debug
		//System.out.println("*****In find Breaks: totalBreaks1="+totalBreaks+" tc.getBreakPoints.size="+tc.getBreakPoints().size());
		
		
		tc.setBreaks(breaks);//the final break points are set here.

		//debug
		//System.out.println("*****In find Breaks: totalBreaks2="+totalBreaks+" tc.getBreakPoints.size="+tc.getBreakPoints().size());
		//System.out.println(" In find Breaks: ending");
		
		if(totalBreaks==0 || !breaks.lastElement().isNewPage)
			tc.setIsSmall(true);//default value=false
		
		return totalBreaks;
	}
	
	
	public void setBreaks(Vector<Break> breaks){tc.setBreaks(breaks);}
	
	public void setTableControl(TableControl tc){this.tc=tc;}
	public TableControl getTableControl(){return tc;}
	
	
	public void setETD(EnsdfTableData etd){this.etd=etd;}
	public EnsdfTableData getETD(){return etd;}

	/**split narrow long table (only in portrait mode)
	 * NO LONGER USED IN findBreaks() 
	 */
    private int findNSplit(TablePanel T,int offset){
    	
		int headLineNo;
		int totalDataLines=0;
		int count=0;
		float headLinePos;
		float headLineWidth;

	
		if(!tc.isPortrait(T)) return 1;
		
		if(offset==0){
			headLineNo=(int) (T.getRelHeadLineNo()+NDSControl.currentLineNo);
			headLinePos=T.getRelHeadLinePos()+NDSControl.currentLinePos;
		}
		else{
			headLineNo=T.getRelHeadLineNo();
			headLinePos=T.getRelHeadLinePos();
		}
		
		headLineWidth=T.getHeadWidth();
		totalDataLines=T.getTotalDataLines()-offset;
		
/* old
		if(Config.MAX_LINE_COUNT-headLineNo-3>5 && totalDataLines>10){
			if(3*headLineWidth+8*Config.CHAR2CM<Config.MAX_TOTAL_WIDTH){
				Control.breakLineNo=Math.min(totalDataLines/3+1, Config.MAX_LINE_COUNT-headLineNo-3)+headLineNo;//-3 for footnote on top of bottom line
				Control.nsplit=3;
			}
			else if(2*headLineWidth+4*Config.CHAR2CM<Config.MAX_TOTAL_WIDTH){
				Control.breakLineNo=Math.min(totalDataLines/2+1, Config.MAX_LINE_COUNT-headLineNo-3)+headLineNo;
				Control.nsplit=2;
			}
			
		}
		
*/
        count=(int)((NDSConfig.MAX_TEXT_HEIGHT-headLinePos)/NDSConfig.DEFAULT_LINEWIDTH)-3;//-3 for footnote on top of bottom line
		if(count>5 && totalDataLines>10){
			if(3*headLineWidth+8*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_WIDTH){
				NDSControl.breakLineNo=Math.min(totalDataLines/3+1,count)+headLineNo;
				NDSControl.breakLinePos=Math.min(totalDataLines/3+1,count)*NDSConfig.DEFAULT_LINEWIDTH+headLinePos;
				NDSControl.nsplit=3;
			}
			else if(2*headLineWidth+4*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_WIDTH){
				NDSControl.breakLineNo=Math.min(totalDataLines/2+1,count)+headLineNo;
				NDSControl.breakLinePos=Math.min(totalDataLines/2+1,count)*NDSConfig.DEFAULT_LINEWIDTH+headLinePos;
				NDSControl.nsplit=2;
			}
			
		}
//debug
		//System.out.println("In findNSplit: offset="+offset+" headLineWidth="+headLineWidth);
		//System.out.println("                  maxDataWidth="+T.getDataWidth());
		//System.out.println("                 maxTotalWidth="+T.getTotalWidth());
		//System.out.println("             comentColumnWidht="+tc.getCommentColumnWidth());

		return NDSControl.nsplit;
    }
     
    
	/**check if a table panel is splitable and return nsplit (only in portrait mode)*/
    public int checkSplitable(TablePanel T){
    	
		float headLineWidth;
		int nsplit=1;
	    boolean isPortrait=true;
	    
		if(!tc.isPortrait(T) || !tc.isPortrait()){
			isPortrait=false;
			//return 1;
		}
	    
		//debug
		//for(int i=0;i<T.getColumns().size();i++)
		//	System.out.println(" In checkSPlitable: column name+"+i+"="+T.getColumns().elementAt(i));
		
		if(T.getColumns().contains("COMM")){//no split when comment column is present.

			return 1;
		}
		
		headLineWidth=T.getHeadWidth();		
			

		
		//System.out.println(" In checkSPlitable TableMaker lin 1296: headLineWidth="+headLineWidth+"  ###3*headLineWidth+8*Config.CHAR2CM="
		//+(3*headLineWidth+8*NDSConfig.CHAR2CM)+" Config.MAX_TOTAL_WIDTH="+NDSConfig.MAX_TOTAL_WIDTH);
		
		if(headLineWidth==0)
			return 1;
		
		if(isPortrait){
			if(4*headLineWidth+10*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_WIDTH)	
				nsplit=4;
			else if(3*headLineWidth+7*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_WIDTH)				
				nsplit=3;					
			else if(2*headLineWidth+5*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_WIDTH)							
				nsplit=2;				
		}else{
			if(5*headLineWidth+14*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_HEIGHT)	
				nsplit=5;
			else if(4*headLineWidth+10*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_HEIGHT)	
				nsplit=4;
			else if(3*headLineWidth+7*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_HEIGHT)				
				nsplit=3;					
			else if(2*headLineWidth+5*NDSConfig.CHAR2CM<NDSConfig.MAX_TOTAL_HEIGHT)							
				nsplit=2;		
		}
		
				
		//debug
		//System.out.println("In TableMaker: checkSplitable: line 1321: headLineWidth="+headLineWidth+" maxWidth="+Config.MAX_TOTAL_WIDTH+" nsplit="+nsplit);
		
		return nsplit;
    }    
    
    /**copy the content of TablePanel T between firstIndex and lastIndex to
     * 
     * @param T
     * @param firstIndex
     * @param lastIndex
     * @return newPanel
     */
    public TablePanel copyTablePanel(TablePanel T,int firstIndex,int lastIndex){
    	TablePanel newPanel=new TablePanel();
    	
    	return newPanel;
    }
    
	/**update columns of the panel before this break point by removing empty columns in that panel
	 * 
	 * @param T
	 * @param firstIndex index of first element (included in the scan)
	 * @param lastIndex  index of last element (included in the scan)
	 * @return updated panelColumns
	 */
    @SuppressWarnings({ "unchecked", "unused" })
	private Vector<String> updatePanelColumns(TablePanel T,int start,int end){
        boolean empty=true;	
		Vector<String> body=T.getBody();
		String temp="";

		int firstIndex=start;
		int lastIndex=end;
		
		if(start<0)
			firstIndex=0;
		if(end>body.size()-1)
			lastIndex=body.size()-1;
		
		//Vector<String> panelColumns=(Vector<String>) tc.getDefaultColumns().clone();
		//int nColumns=tc.getDefaultColumns().size();

		Vector<String> panelColumns=(Vector<String>) T.getColumns().clone();
		int nColumns=panelColumns.size();

		//for(int k=0;k<panelColumns.size();k++)
		//	System.out.println(" In TableMaker line 2024: "+this.tableType+" start="+start+" column name+"+k+"="+panelColumns.elementAt(k)+" size="+panelColumns.size());

		
        //ArrayList<String> excludedColumns=new ArrayList<String>(Arrays.asList("LEV","DLEV","JI","LEVF","DLEVF","JF"));//keep these columns even if they are empty.
        //ArrayList<String> excludedColumns=new ArrayList<String>(Arrays.asList(" "));
		// ArrayList<String> excludedColumns=new ArrayList<String>(Arrays.asList("LEV","DLEV"));//keep these columns even if they are empty.
		
		Vector<String> excludedColumns=tc.getDefaultFixedColumns();
		
		 //debug
		 //for(String name:panelColumns) System.out.println("In TableMaker 1787: #1 name="+name+" first="+firstIndex+" last="+lastIndex);
		 //for(String name:excludedColumns) System.out.println("In TableMaker 2026: excluded columns name="+name+" first="+firstIndex+" last="+lastIndex);
		
		 String firstJFS="";
		 Vector<String> values=new Vector<String>();
		 //for gamma table: new gamma from the level in the previous page (sort by level), print JI record
		 if(firstIndex>0 && panelColumns.contains("JI") && panelColumns.contains("JF")){
			 String line=body.elementAt(firstIndex);		
			 if(line.contains("\\&")){			
				 line=line.replace("\\&", "\\#");				
			 }			
			 values=Str.splitString(line,"&");             
             
			 int j=panelColumns.indexOf("JF");
			 if(j>=0){	        
				 temp=values.elementAt(j).trim();	             
				 if(temp.contains("multicolumn")){	             
					 int p1=temp.lastIndexOf("{}");	                
					 int p2=temp.lastIndexOf("{");	                
					 if(p1==p2)	                
						 temp="";	                	                
				 }
				 firstJFS=temp;
			 }
 
		 }

		 
			
		 //System.out.println("##In TableMaker 2044: start="+start+" "+body.elementAt(start)+" nColumns="+nColumns+"  values="+values.size());
		 //System.out.println("                        end="+end+" "+body.elementAt(end));
			
		 for(int j=0;j<nColumns;j++){
	    	empty=true;    	
			
	    	String colName=T.getColumns().elementAt(j);
	    	
	    	//System.out.println("In TableMaker 2062: excludedColumns="+excludedColumns+" colName="+colName+"  first="+firstIndex+" last="+lastIndex);		
	    	if(excludedColumns.contains(colName)){
			//if(excludedColumns.contains(colName) || (colName.equals("JI") && firstJFS.length()>0) ){
				//debug
				//System.out.println("In TableMaker 2054: updateCOlumn"+excludedColumns+" "+colName+"  "+firstJFS);
				continue;
			}
			
			for(int k=firstIndex;k<=lastIndex;k++){
				String line=body.elementAt(k);

				
				if(line.contains("\\&")){
					line=line.replace("\\&", "\\#");
				}
				values=Str.splitString(line,"&");

				
				//System.out.println("###"+nColumns+" j="+j+" k="+k+" line="+ line);
				
				if(j>=values.size())
					break;
				
                temp=values.elementAt(j).trim();
                if(temp.contains("multicolumn")){
                	int p1=temp.lastIndexOf("{}");
                	int p2=temp.lastIndexOf("{");
                	if(p1==p2)
                		temp="";
                }
                
                //if(colName.equals("COMM")){
                //	//debug
        		// System.out.println("In TableMaker 2090: "+this.tableType+" k="+k+" start="+start+" end="+end+" colName="+colName+" empty="+empty+" temp="+temp+" line="+line);
                //}
                
				if(temp.length()>0){ 
					empty=false;
					break;
				}
			}
			
			
			
			if(empty){
                boolean remove=true;
                  			
				//when current colName is for uncertainty, check if corresponding value columns has been removed
				if(colName.length()>1 && colName.startsWith("D")){
					String tempName;
					tempName=colName.substring(1);
					
					//value columns is not removed, so keep uncertainty column even if it is empty. Because it will be 
					//used for decimal-point alignment in making data table line
					if(panelColumns.contains(tempName))
						remove=false;
						
				}

				if(remove) {
					//System.out.println("In TableMaker 2117: excludedColumns="+excludedColumns+" j="+j+" removed colName="+colName+"  firstJFS="+firstJFS);		
					panelColumns.removeElement(colName);
				}
				

			}else if(colName.length()>1 && colName.startsWith("D")) {
				String tempName;
				tempName=colName.substring(1);
				if(EnsdfConfig.isValueColumn(tempName) && !panelColumns.contains(tempName)) {
					int n=panelColumns.indexOf(colName);
					panelColumns.add(n, tempName);
				}
			}
		}

		 //final check
		 //note that there are cases where a RI is empty but its DRI is not, for example
		 //when a RI footnote flag is added at a gamma which has no RI and DRI values, but
		 //here in the column values of the panel, the footnote mark is still put in the DRI column
		 //while it will not be printed out (note that a footnote mark is placed in the same column as the uncertainty)
		 
	    //debug
        //for(String name:panelColumns) System.out.println("            #2 name="+name);
         
	    return panelColumns;
    }
    
    @SuppressWarnings("unused")
	private Vector<String> updatePanelColumns(TablePanel T){
    	return updatePanelColumns(T,0,T.getNTableBodyLines()-1);
    }
    
    protected Vector<String> splitFormattedComments(String str,int nComsSeqAtBreak,int panel){
    	Vector<String> out=new Vector<String>();
    	int index=-1;
    	String s=str;
    	String temp="";
    	
    	if(!s.contains("\\parbox[c]{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright .\\vspace{0.1cm}}")){
    		temp="\\parbox[c]{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright ";
    		index=s.indexOf(temp);
    		if(index>=0){
    			s=s.substring(index+temp.length());
    			index=s.indexOf("\\vspace{0.1cm}}");
    			if(index>0){
    				s=s.substring(0,index);
                    Vector<String> tempV=Str.wrapString(s, (int) (tc.getCommentColumnWidth(panel)*1.1/NDSConfig.CHAR2CM));
                    if(nComsSeqAtBreak<tempV.size()){
                    	temp="";
                    	for(int i=0;i<nComsSeqAtBreak;i++)
                    		temp+=tempV.elementAt(i)+" ";
                    	if(!temp.isEmpty())
                    		out.add(temp);
                    	
                    	temp="";
                    	for(int i=nComsSeqAtBreak;i<tempV.size();i++)
                    		temp+=tempV.elementAt(i)+" ";
                    	if(!temp.isEmpty())
                    		out.add(temp);
                    }
    			}
    		}
    		

    	}
    	
    	return out;
    }
 
    /**split a ENSDF-formatted comment based on a given width
     * NOTE: skip the non-printable character
     * @param str
     * @param width
     * @return
     */
    protected static Vector<String> splitENSDFComment(String str,float width){
    	return splitENSDFComment(str,width,0);
    }
    
    
    /**split a ENSDF-formatted comment based on a given width
     * NOTE: skip the non-printable character
     * @param str
     * @param width
     * @param indent_spaces (number of spaces to be put at beginning as indent)
     * @return
     */
    @SuppressWarnings("unused")
	protected static Vector<String> splitENSDFComment(String str,float width,int indent_spaces){
    	Vector<String> out=new Vector<String>();
    	String temp="",s="";
      
    	ArrayList<String> nosplitBracketsV=new ArrayList<String>(Arrays.asList("^{","|\"{","|%{","|6{","{+","{-"));
    	//ArrayList<String> goodsplitBracketsV=new ArrayList<String>(Arrays.asList("{I","{B","{U"));//italics {I, bold {B, underline {U
    		
    	Vector<String> unmatchedLeftBracketsV=new Vector<String>();
    	
        int nbreaks=0,ntries=0;
        String tempBody=str.trim();            
        float commentColumnWidth=width-0.1f;
        float strWidth=NDSConfig.findWidth(tempBody); 
        String indent_str=Str.repeat("\\s", indent_spaces);
        if(indent_spaces==0)
        	indent_str="";
        
        //debug
        //System.out.println("#### In TableMaker: splitENSDFComment: str width="+strWidth+" str="+tempBody+"*"+" commentColumnWidth="+commentColumnWidth);
        
        if(tempBody.length()==0){
        	out.add(tempBody);
        	return out;
        }
        else if(strWidth<=width){
        	out.add(tempBody);
        	return out;
        }
       
        nbreaks=0;
        ntries=0;

        while(strWidth>commentColumnWidth){
        	int nBracket=0;
        	int nRoundBracket=0;
        	int ibreak=0;
        	int start=tempBody.length()-1;
        	char ch;
        	boolean isBreak=false, inNonsplitableBracket=false;
        	nbreaks=0;
        	
        	//determine the start point for the main search loop
        	float tempWidth=0;
        	float r=1.2f;

        	while(true){
        		start=(int) (r*commentColumnWidth/NDSConfig.CHAR2CM);
        		//if(tempBody.contains("m(2686)u(2673)"))
        		//	System.out.println("TableMaker 2169: "+tempBody+" start="+start+" tempBody.length()="+tempBody.length());
        		
        		if(start>tempBody.length()-1){
        			start=tempBody.length()-1;
        			break;      			
        		}
        		       		
        		String s1=tempBody.substring(0,start+1);
        		tempWidth=NDSConfig.findWidth(s1);
        		
        		//String tempS="target was irradiated with 1.4*10{+17} {+48}Ca ions in 14 h. No events were observed for {+33}F, whereas estimated counts for {+33}F";
        		//if(tempBody.contains("m(2686)u(2673)"))
        		//	System.out.println("        TableMaker 2179: s="+s1+"# tempWidth="+tempWidth+" commentWidth="+commentColumnWidth+"  "+NDSConfig.findWidth(tempS));
        		
        		if(tempWidth<commentColumnWidth){
        			r+=0.05;
        			continue;
        		}
        		
        		break;        			
        	}
        	
        	//System.out.println(tempBody.substring(0,start+1));
        	
        	unmatchedLeftBracketsV.clear();
        	
        	int count=0;
        	Vector<Integer> unmatchedLBCountIndexV=new Vector<Integer>();
        	Vector<Integer> unmatchedLBPosV=new Vector<Integer>();
        	for(int m=0;m<=start;m++){    		
        		ch=tempBody.charAt(m);
        		
        		int n=unmatchedLeftBracketsV.size();
        		if(ch=='{') {
          			count++;
        			
        			nBracket+=1;
        			int i0=tempBody.lastIndexOf(" ",m);
        			int i1=(m>=3)?(m-3):0;
        			int i2=(m<start-1)?(m+1):start;
        			
        			i1=Math.max(i0, i1);
        			
        			unmatchedLeftBracketsV.add(tempBody.substring(i1,i2+1).trim());
        			unmatchedLBCountIndexV.add(count-1);//index of this { in all { in tempBody        			
        			unmatchedLBPosV.add(m);
        		}else if(ch=='}') {
        			nBracket-=1;
        			if(n>0) {
        				unmatchedLeftBracketsV.remove(n-1);
        				unmatchedLBCountIndexV.removeElementAt(n-1);
        				unmatchedLBPosV.removeElementAt(n-1);
        			}
        		}
        	}
        	
        	nBracket=0;

        	int index=0;
        	int firstNonsplitIndex=-1;
        	for(int i=0;i<unmatchedLeftBracketsV.size();i++) {
        		String s1=unmatchedLeftBracketsV.get(i);
        		index=unmatchedLBCountIndexV.get(i).intValue();
        		if(Str.containAnyPattern(s1, nosplitBracketsV)) {
        			inNonsplitableBracket=true;
        			firstNonsplitIndex=index;
        			break;
        		}

        	}
        	
        	/*
        	if(tempBody.contains("m(2686)u(2673)")) {      			
        		System.out.println("TableMaker 2233: "+tempBody+" start="+start+" tempBody.length()="+tempBody.length());
        		System.out.println("        tempWidth="+tempWidth+" commentWidth="+commentColumnWidth);
            	String s2=(unmatchedLeftBracketsV.size()>0)?unmatchedLeftBracketsV.get(0):"###";
            	System.out.println(s2+"$count="+count+" "+inNonsplitableBracket+"  "+firstNonsplitIndex+" "+index+"  "+tempBody.substring(0,start));

        	}
        	*/
        	//String s2=(unmatchedLeftBracketsV.size()>0)?unmatchedLeftBracketsV.get(0):"###";
        	//System.out.println(s2+"$count="+count+" "+inNonsplitableBracket+"  "+firstNonsplitIndex+" "+index+"  "+tempBody.substring(0,start));
        	
            index=count-1;
        	for(int m=start;m>1;m--){
        		        		
        		ch=tempBody.charAt(m);
        		
        		//debug
        		//if(tempBody.substring(0,start).contains("target was"))
        		//System.out.println("In TableMaker 2235: "+inNonsplitableBracket+" splitENSDF1: commentColumnWidth="+commentColumnWidth+" length="+tempBody.length()+" r="+r+" m="+m+" ch="+ch+" start="+start+" nBracket="+nBracket+" tempBody="+tempBody.substring(0,start));
        		
        		if(ch=='}') {
        			nBracket+=1;//do not split text inside "{}", except for something like {Bxxx}, {Uxxx}
        		}else if(ch=='{') {
        			//for(Integer in:unmatchedLBPosV)
        			//	System.out.println("###"+in.intValue()+" @@@"+m);
        			
        			if(!unmatchedLBPosV.contains(m)) {//indexes of unmatchedLeftBracketsV
            			nBracket-=1;
            			index--;
        			}

        			
        			if(inNonsplitableBracket && index<=firstNonsplitIndex) {
        				nBracket=0;
        				inNonsplitableBracket=false;
        			}
        		}else if(ch==')') {
        			nRoundBracket+=1;
        		}else if(ch=='(') {
        			nRoundBracket-=1;
        		}
        		
        		if(inNonsplitableBracket){//start is inside a bracket, has passed "{" and now it is outside of  
        			//nBracket=0;
        			continue;
        		}
        		
        		tempWidth=NDSConfig.findWidth(tempBody.substring(0,m));
        		
        		
        		//debug
            	//if(tempBody.substring(0,start).contains("EXOGAM")) {      			
            	//	System.out.println("In TableMaker 2353: splitENSDF2: ibreak="+ibreak+" ntries="+ntries+" length="+tempBody.length()+" m="+m+" ch="+ch+" nBracket="+nBracket);
            	//	System.out.println("    tempWidth="+tempWidth+" commentColumnWidth="+commentColumnWidth+" tempBody.substring(0,m)="+tempBody.substring(0,m));

            	//}
            	
        		
        		if(tempWidth>commentColumnWidth) 
        			continue;
        		if(tempWidth<commentColumnWidth/3)
        			break;

        		if(nBracket!=0 && ntries==0) 
        			continue;
        		
        		int leftClosestSpaceIndex=tempBody.substring(0,m).lastIndexOf(" ",m-1);
        		boolean hasSpaceSplit=(leftClosestSpaceIndex>tempBody.substring(0,start).length()/2);
        		//debug
                //System.out.println("In TableMaker: splitENSDF3:  ntries="+ntries+" m="+m+"**ch="+ch+" ibreak="+ibreak+" commentColumnWidth="+commentColumnWidth+" length="+tempBody.length()+" tempBody="+tempBody);
        		
        		
        		//debug
            	//if(tempBody.substring(0,m).contains("EXOGAM")) {      			
            	//	System.out.println("In TableMaker 2376: splitENSDF2: ibreak="+ibreak+" ntries="+ntries+" length="+tempBody.length()+" m="+m+" ch="+ch+" nBracket="+nBracket);
            	//	System.out.println(hasSpaceSplit+"    tempWidth="+tempWidth+" commentColumnWidth="+commentColumnWidth+" tempBody.substring(0,m)="+tempBody.substring(0,m));

            	//}
            	
        		if(ntries==0 && ch==' ' && tempBody.charAt(m-1)!='|'){//first try to find break at " "
        			isBreak=true;
        		}else if(ntries==1 || (ntries==0&&!hasSpaceSplit)){//if break at " " is not found, try the second time to find break at other delimiters
                    if(ch=='=')      isBreak=true;
                    else if(ch=='>') isBreak=true;
                    else if(ch=='<') isBreak=true;
                    else if(ch==':') isBreak=true;
                    else if(ch==';') isBreak=true;
                    else if(ch==','&&nRoundBracket==0) isBreak=true;
                    else if(ch=='/') isBreak=true;
                    else if(ch==')') isBreak=true;
                    else if(tempBody.substring(m-1,m).equals("|?")) 
                    	isBreak=true;    
                    else if(ch=='.') {
                    	boolean isInGSWord=false;
                    	String tempS=tempBody.toUpperCase();
                    	if(m>0) {
                         	if(tempS.charAt(m-1)=='G'&& (m+3)<=tempS.length() && tempS.substring(m-1,m+3).equals("G.S."))
                        		isInGSWord=true;
                         	else if(tempS.charAt(m-1)=='S' && m>3 && tempS.substring(m-3,m+1).equals("G.S."))
                         		isInGSWord=true;
                    	}
   
                    	
                    	if(m<tempBody.length()-1 && !Str.isDigit(tempBody.charAt(m+1)) && !isInGSWord)
                    		isBreak=true;
                    }
                    
                    /*
            		//debug
                	if(tempBody.contains("m(2686)u(2673)")) {      			
                		System.out.println("In TableMaker 2326: splitENSDF2: ibreak="+ibreak+" ntries="+ntries+" is break="+isBreak+" length="+tempBody.length()+" m="+m+" ch="+ch+" nBracket="+nBracket);
                		System.out.println(hasSpaceSplit+"    tempWidth="+tempWidth+" commentColumnWidth="+commentColumnWidth+" tempBody.substring(0,m)="+tempBody.substring(0,m));

                	}
                	*/
        		}else if(ntries==2){//already tried twice but still no break is found.
        			ibreak=m;
        			nBracket=0;
        	        
        			for(int p=0;p<=m;p++){//avoid breaking within brackets
        				ch=tempBody.charAt(p);
        				if(ch=='}') nBracket+=1;       				
        				if(ch=='{') nBracket-=1;
        				if(nBracket==0) ibreak=p;
        			}
        			isBreak=true;
        			m=ibreak;
        			
        		}
        		
        		if(isBreak){
    	        	
        			String tempBody0=tempBody;
      	        	String temp1=tempBody0.substring(m+1).trim();
      	        	char c=0;
      	        	
        			temp=tempBody0.substring(0, m+1).trim();
        			tempBody=indent_str+temp1.trim();      	        
        	        tempWidth=NDSConfig.findWidth(tempBody);
        	        
	        		if(temp1.length()>0)
	        			c=temp1.charAt(0);
	        		
	        		if(c!=0 && "]).,:;".contains(c+"")) {
	        			if(tempBody0.length()>(m+2)) {
	        				temp=tempBody0.substring(0, m+2).trim();
	        				tempBody=indent_str+tempBody0.substring(m+2).trim();
	        			}else {
	        				temp=tempBody0.trim();
	        				tempBody="";
	        			}
        	        	tempWidth=NDSConfig.findWidth(tempBody);
	        		}
	        		
        	        //in case break within indent_str resulting in no change in tempBody, that causes infinite loop of the outer "while".
        	        //force to break the line if that happens.
        	        //This is supposed not to happen, unless there is format errors or translation errors, like unmatched "{","}".
        	        //e.g.,tempBody=\s\s\s}. For 505.8|g+506.1|g+506.6|g A{-2}=-0.05 {I1}}, A{-4}=+0.01 {I1}}.
        	        //     ibreak=5

        	        while(tempWidth>=strWidth){
        	        	m+=10;      
        	        	boolean done=false;
        	        	if(m>tempBody0.length()-1){
        	        		m=tempBody0.length()-1;
        	        		done=true;
        	        	}

        	        	temp1=tempBody0.substring(m+1).trim();

        	        	temp=tempBody0.substring(0, m+1).trim();
        	        	tempBody=indent_str+tempBody0.substring(m+1).trim();
        	        	tempWidth=NDSConfig.findWidth(tempBody);
        	        	
        	        	if(done) {
        	        		c=0;
        	        		if(temp1.length()>0)
        	        			c=temp1.charAt(0);
        	        		
        	        		if(c!=0 && "]).,:;".contains(c+"")) {
        	        			if(tempBody0.length()>(m+2)) {
        	        				temp=tempBody0.substring(0, m+2).trim();
        	        				tempBody=indent_str+tempBody0.substring(m+2).trim();
        	        			}else {
        	        				temp=tempBody0.trim();
        	        				tempBody="";
        	        			}

                	        	tempWidth=NDSConfig.findWidth(tempBody);
        	        		}
        	        		break;
        	        	}
        	        }
        	        
        	        /*
            		//debug
                	if(str.contains("m(2686)u(2673)")) {      			
                		System.out.println("In TableMaker 2367: splitENSDF2: ibreak="+ibreak+" ntries="+ntries+" length="+tempBody.length()+" m="+m+" ch="+ch+" nBracket="+nBracket);
                		System.out.println(hasSpaceSplit+"    tempWidth="+tempWidth+" commentColumnWidth="+commentColumnWidth+" temp="+temp+" tempBody="+tempBody);

                	}
                	*/
        			out.addElement(temp);        			
        			strWidth=tempWidth;
        			
        			nbreaks++;
        			ntries=0;
        			break;
        		} 
        		
        	}
    		//debug
            //System.out.println("In TableMaker: splitENSDF4:  nBreaks="+nbreaks+" ibreak="+ibreak+" size="+tempBody.length()+" tempBody="+tempBody);
            
        	if(nbreaks==0){
        		ntries++;//no break at " " are found, try again with other break delimiters 
        		
        	}
        	
    		if(ntries>50)//to avoid infinite loop
    			break;
        	
        }
        
		//debug
		//System.out.println("In TableMaker: splitENSDF5: tempBody="+tempBody);
		
        if(tempBody.length()>0){
        	temp=tempBody;
        	out.addElement(temp);
        }
           	
    	return out;
    }
    
    /*
     * split ENSDF comment and reformat the split ones by adding 
     * brackets ({ or }} to unmatched ones at beginning or end of each item
     * e.g., for cases, like comment text inside {Bxxx}, {Uxxx}
     * return a vector containing:
     * 1. first part is the original split items
     * 2. send part with the same size is the reformatted split item

     */
    protected static Vector<Vector<String>> splitENSDFCommentAndReformat(String str,float width,int indent_spaces){
    	Vector<Vector<String>> out=new Vector<Vector<String>>();
    	Vector<String> out0=splitENSDFComment(str,width,indent_spaces);
    	Vector<String> out1=new Vector<String>();

    	
    	//ArrayList<String> marksV=new ArrayList<String>(Arrays.asList("{I","{B","{U"));//italics {I, bold {B, underline {U
    	
    	if(out0.size()==1 || (!str.contains("{"))&&!str.contains("}")){//no split or no brackets
    		out1.addAll(out0);
    	}else {
    		Vector<String> leftBracketsV=new Vector<String>();
    		for(String line:out0) {
    			String newLine=line+"";
    			
    			int nBracket=0; //in this line only
    			int nLeftBr=0;
    			
    			int prevNomatches=leftBracketsV.size();
    			if(line.contains("{") || line.contains("}")){
    				char ch=0;  				
    	        	for(int m=0;m<line.length();m++){    		
    	        		ch=line.charAt(m);
    	        		if(ch=='{') {
    	        			nBracket+=1;
    	        			nLeftBr+=1;
    	        			if(m<line.length()-1 && "IBU".contains(line.charAt(m+1)+"")) {
    	        				ch=line.charAt(m+1);
        	        			leftBracketsV.add("{"+ch);
    	        			}else {
    	        				leftBracketsV.add("{");
    	        			}
    	        				

    	        		}else if(ch=='}') {
    	        			nBracket-=1;
    	        			int n=leftBracketsV.size();
    	        			if(n>0 && nLeftBr>0) {
    	        				leftBracketsV.remove(n-1);
    	        				nLeftBr-=1;
    	        			}
    	        		}
    	        	}

    			}

    			//1. nBracket<0 means this line has missing left brackets at beginning
    			//2. nBracket==0 means: no {} or all {} are matched, for this case if prevNomatches>0, 
    			//   we still need to add prevNomatches left brackets for this line because this whole line 
    			//   is in the non-splitable bracket
    			if(nBracket>0) {
    				for(int i=0;i<leftBracketsV.size();i++)
    					newLine+="}";
    			}else if(prevNomatches>0) {
    				for(String s:leftBracketsV) {
    					newLine=s+newLine;
    				}
    				int n=-nBracket;
    				for(int i=0;i<n;i++)
    					leftBracketsV.remove(leftBracketsV.size()-1);
    			}
    			//System.out.println("TableMaker 2573: line="+line+" ###newline="+newLine);
    			//if(leftBracketsV.size()>0) System.out.println("    "+leftBracketsV.size()+"   "+leftBracketsV.get(0));
    			
    			out1.add(newLine);
    		}

    	}
    	
    	
    	out.add(out0);
    	out.add(out1);
    	return out;
    }
    
 
    /**split and wrap a ENSDF-formatted comment based on a given length and head
     * and return translated Latex-formatted comments
     * NOTE: skip the non-printable character
     * @param str
     * @param length
     * @param tempHead: an arbitrary string with the actual length of final printed head
     * @param head: translated Latex-formatted head
     * @return 
     * @throws Exception 
     */
    protected static Vector<String> wrapENSDFCommentTranslated(Comment c,float width,String tempHead,String head, boolean isBigC,int indent) throws Exception{
    	Vector<String> tempComs;
    	Vector<String> out=new Vector<String>();
    	
    	String str=c.body();
    	
    	String tempBody="";
    	Comment c1;
    	
    	c1=new Comment();
    	
    	//debug
    	//if(str.contains("BAND"))
    	//System.out.println("In TableMaker A: isBigC="+isBigC+" wrapEnsdfComments: "+str+" tempHead="+tempHead+" head="+head);
    	

    	if(isBigC){
    		tempBody=tempHead+Translator.procGenCom(str, 0, true);
    	}else
        	tempBody=tempHead+str;

    	
    	str=tempBody;
    	
    	//debug
    	//if(str.contains("BAND"))
    	//System.out.println("In TableMaker B: width="+width+" wrapEnsdfComments: tempBody="+tempBody);
    	
    	//tempComs=splitENSDFComment(tempBody,width,indent); 	
    	Vector<Vector<String>> tempVV=splitENSDFCommentAndReformat(tempBody,width,indent);tempComs=tempVV.get(1);
    	
    	
    	//debug
    	//System.out.println("In TableMaker C: wrapEnsdfComments: tempBody="+tempBody);
    	
        for(int m=0;m<tempComs.size();m++){
        	c1=new Comment();
    		c1.setBigC(false);
    		
    		//System.out.println("TableMaker 2483: m="+m+"  element="+tempComs.elementAt(m));
    		
        	if(m==0){
            	//debug
            	//System.out.println("In TableMaker D: isBigC="+isBigC+" wrapEnsdfComments: "+tempComs.elementAt(0)+" tempHead="+tempHead+" head=");
            	
            	if(tempComs.elementAt(0).length()<=tempHead.length()) {
            		tempBody=head;
            	}else {
            		tempBody=tempComs.elementAt(0).substring(tempHead.length());
            		int start=str.indexOf(tempBody);
            		if(start>=0) {
            			tempBody=head+Translator.translate(str,start,start+tempBody.length()-1);
            		}else {                   	
                		c1.setBody(tempBody);
                		tempBody=head+c1.getRegTranslated();
            		}
            		
            		//System.out.println("   #### start="+start+"tempBody="+tempBody+"    @@@@str="+str);
            	}
          
            	//System.out.println("first line 1 ##tempBody="+tempBody);
        	}
        	else{
        		tempBody=tempComs.elementAt(m);

        		String indent_str=Str.repeat("\\s", indent);
        		tempBody=tempBody.replace(indent_str,"").trim();
        		indent_str=Translator.translate(indent_str);
        		
             	//System.out.println("other lines 1 tempBody="+tempComs.elementAt(m));

        		int start=str.indexOf(tempBody);
        		if(start>=0) {
        			tempBody=Translator.translate(str,start,start+tempBody.length()-1);
        		}else {                   	
            		c1.setBody(tempBody);
            		tempBody=c1.getRegTranslated();
        		}
        	    
        		tempBody=indent_str+tempBody;
        		
             	//System.out.println("start="+start+"  other lines 2 tempBody="+tempBody);
        	}

			//if(tempBody.length()>0)
			  out.addElement(tempBody);
        }
        
        return out;
    	
    }
    
    public static Vector<String> wrapENSDFCommentTranslated(Comment c,float width,String tempHead,String head, boolean isBigC) throws Exception{
    	return wrapENSDFCommentTranslated(c,width,tempHead,head,isBigC,0);
    }
    
    protected static Vector<String> wrapENSDFCommentTranslated(String str,float width,String tempHead,String head, boolean isBigC) throws Exception{
    	Comment c=new Comment();
    	c.setBody(str, isBigC);
    	return wrapENSDFCommentTranslated(c,width,tempHead,head,isBigC,0);
    }
    
    protected static Vector<String> wrapENSDFCommentTranslated(String str,float width,String tempHead,String head) throws Exception{
    	return wrapENSDFCommentTranslated(str,width,tempHead,head,true);
    }
    
        

    
    /**process in-record comments, for J, T1/2, E, MR, and so on */
     protected  Vector<String> processInRecordComments(int panel, Record d,String lastComLineInPrevPanel, String secondLastComLineInPrevPanel)throws Exception{  	 
    	 Vector<Comment> coms=d.commentV();
    	 return processInRecordComments(panel,coms,lastComLineInPrevPanel,secondLastComLineInPrevPanel);
    	 
     }
     
     protected boolean isContinuedCommentLine(String comLine) {
         try {
             int pos1=comLine.indexOf("raggedright")+11;
             int pos2=comLine.indexOf("vspace{0.1")-1;
             String tempLine="";
             if(pos1>0 && pos2>pos1){
                 tempLine=comLine.substring(pos1, pos2).trim();
                 
                //continued comment line has three  "{\ }{\ }{\}" at the beginning for indent, see SplitENSDFComment()
                //remove them first
                 int count=0;
                 while(tempLine.indexOf("{\\ }")==0) {
                     tempLine=tempLine.substring(4).trim();
                     count++;
                 }
                 if(count>=3)
                     return true;
             }

         }catch(Exception e) {
             
         }
         
         return false;
     }
     
     //remove LaTeX heading and tailing of a LaTeX-formatted comment line to a regular translated ENSDF line
     protected String cleanFormattedCommentLine(String comLine) {
         String out="";
         try {
             int pos1=comLine.indexOf("raggedright")+11;
             int pos2=comLine.indexOf("vspace{0.1")-1;
             String tempLine="";
             if(pos1>0 && pos2>pos1){
                 tempLine=comLine.substring(pos1, pos2).trim();
                 
                //continued comment line has three  "{\ }{\ }{\}" at the beginning for indent, see SplitENSDFComment()
                //remove them first
                 while(tempLine.indexOf("{\\ }")==0)
                     tempLine=tempLine.substring(4).trim();
                 
             }
             
             out=tempLine;
         }catch(Exception e) {
             
         }
         
         return out;
     }
     
     //find the index of the comment text of a formatted comment line in a comment translated body
     protected int findIndexOfFormattedCommentLineText(String formattedLine,Comment c) {
 
         try {
             String head="",tempHead="";
             
             String tempLine=cleanFormattedCommentLine(formattedLine);
             
             if(tableType.equals("LEVEL") && c.isContinuedXREF()){
                 //remove hyperlinks inserted before
                 tempLine=EnsdfUtil.removeContinuedXREFLinks(tempLine);
             }
             
             if(c.nHeads()>0){
                head=commentHeadMaker(c);
                tempHead=commentHeadMaker(c,"temp");
             }
                       
             String tempBody="",tempTranslated="";
             if(c.isBigC()){
                 if(!head.toUpperCase().contains("REF"))//avoid XREF="A" being translated as "alpha" in "C" mode
                     tempBody=Translator.procGenCom(c.body(), 0, false);
                 else
                     tempBody=c.body();
                 
                 c.setBigC(false);
                 c.setBody(tempBody, false);
             }            
             
             tempBody=tempHead+c.body();     
                        
             int pos1=-1;
             Comment tempC=new Comment();
             tempC.setBigC(c.isBigC());
             tempC.setBody(c.body());
             
             if(head.length()>0)
                 tempTranslated=head+tempC.getRegTranslated();
             else
                 tempTranslated=tempC.getRegTranslated();

             pos1=tempTranslated.indexOf(tempLine);

             if(pos1<0)
                 return -1;

             
             int i=0,pos2=-1;
             String s=c.body();
             String ss="";
             while(i<s.length()){
                 pos1=s.indexOf(" ", ss.length());
                 if(pos1<0)
                     pos1=s.length()-1;
                 
                 ss=s.substring(0,pos1+1);
                 tempC.setBody(ss);
                 if(head.length()>0)
                     tempTranslated=head+tempC.getRegTranslated();
                 else
                     tempTranslated=tempC.getRegTranslated();
                 
                 pos2=-1;
                 pos2=tempTranslated.indexOf(tempLine);
                             
                 i=pos1+1;
                 if(pos2<0)
                     continue;
                 else
                     break;
             }
             
             if(pos2>=0){//match found, that is, last comment line in previous panel is contained in this comment "c"
                 if(i==s.length()){//this comment "c" is completely in previous panel, so nothing to be written in current panel
                     tempBody="";
                 }
                 else{             //this comment "c" is partially in previous panel, and the rest are to be written in current panel
                     tempBody=s.substring(i).trim();
                     if(tempBody.trim().length()>0){
                         tempBody="\\s\\s\\s"+tempBody;//leave 3 white-space indentation
                     }
                 }
                 
                 return pos1;
             }              
         }catch(Exception e) {
             
         }
         
         return -1;
     }

     /**process in-record comments, for J, T1/2, E, MR, continued records, data as comments, and so on */
     protected  Vector<String> processInRecordComments(int panel,Vector<Comment> coms,String lastComLineInPrevPanel,String secondLastComLineInPrevPanel)throws Exception{ 
    	 
         Vector<String> out=new Vector<String>();
         
         String head="",tempHead="";
         String temp="",row;

         String linewidth_str="";
        
         boolean isContinuedCom=false;
         boolean isLastComAcrossPages=false;
         
         if(coms==null || coms.size()==0)
        	 return out;
       
                        
         if(lastComLineInPrevPanel!=null && lastComLineInPrevPanel.trim().length()>0)
             isContinuedCom=true;
         
         Comment c,c1;
         int pos1=-1,pos2=-1;
         float commentColumnWidth=tc.getCommentColumnWidth(panel)-0.1f;//leave some redundant space
         
         //if(this.tableType.equals("DECAY"))
         //System.out.println("In TableMaker 2953: panel="+panel+"   portrait="+tc.isPortrait()+"  com widith="+commentColumnWidth);

         String tempBody="",tempLineLast="",tempLine2ndLast="",tempTranslated="",leftoverOfLastComInPrevPanel="";
         Vector<String> tempComs;
         
         if(isContinuedCom){
             leftoverOfLastComInPrevPanel=tc.getLeftOverOfLastCom(panel-1);
             
        	 //System.out.println("In TableMaker 2404: lastComLineInPrevPanel="+lastComLineInPrevPanel+"\n    ##### "+secondLastComLineInPrevPanel);
        	 
             pos1=lastComLineInPrevPanel.indexOf("raggedright")+11;
             pos2=lastComLineInPrevPanel.indexOf("vspace{0.1")-1;
             if(pos1>0 && pos2>pos1){
            	 tempLineLast=lastComLineInPrevPanel.substring(pos1, pos2).trim();
            	 
            	//continued comment line has three  "{\ }{\ }{\}" at the beginning for indent, see SplitENSDFComment()
            	//remove them first
             	 while(tempLineLast.indexOf("{\\ }")==0)
             		 tempLineLast=tempLineLast.substring(4).trim();
             	 
                 pos1=secondLastComLineInPrevPanel.indexOf("raggedright")+11;
                 pos2=secondLastComLineInPrevPanel.indexOf("vspace{0.1")-1;
                 if(pos1>0 && pos2>pos1){
                	 tempLine2ndLast=secondLastComLineInPrevPanel.substring(pos1, pos2).trim();
                 	 while(tempLine2ndLast.indexOf("{\\ }")==0)
                 		 tempLine2ndLast=tempLine2ndLast.substring(4).trim();
                 	 
                 }
             }
             else
            	 isContinuedCom=false;
         }
         
         //System.out.println("In TableMaker 2974: lastComLineInPrevPanel="+lastComLineInPrevPanel+" # "+tempLineLast+" $ "+tempLine2ndLast);
         
         boolean found=false;

         int size=coms.size();
         
         /*
     	 //debug
         if(size>0 && coms.get(0).type().contains("L") && lastComLineInPrevPanel.contains("doublet") ) {
             System.out.println("In TableMaker 2761:processInRecordComments: tempLine="+tempLine);
         	 System.out.println("           lastComLineInPrevPanel="+lastComLineInPrevPanel);
         	 for(Comment c2:coms)
         		 System.out.println("   comment line: "+c2.body()); 
         }
         */
     	
         isLastComAcrossPages=false;
         
         boolean hasFound2ndLast=false;
         
         int indexOfFirstMatchLast=-1;
         
         for(int k=0;k<size;k++){
             temp="";
             head="";tempHead="";
             c=coms.elementAt(k);
             
             boolean toAddHyperlink=false;
             if(tableType.equals("LEVEL") && c.isContinuedXREF()){
             	 //remove hyperlinks inserted before
             	 tempLineLast=EnsdfUtil.removeContinuedXREFLinks(tempLineLast);
             	 tempLine2ndLast=EnsdfUtil.removeContinuedXREFLinks(tempLine2ndLast);
             	
             	 toAddHyperlink=true;
             }
             
          	//debug
          	//System.out.println("In TableMaker 2392: head="+head+" tempHead="+tempHead+" line="+c.body()+" "+c.head()+" c.isBigC="+c.isBigC());
             
             if(c.nHeads()>0){
             	head=commentHeadMaker(c);
             	tempHead=commentHeadMaker(c,"temp");
             	
             	//debug
             	//System.out.println("In TableMaker: head="+head+" tempHead="+tempHead+" line="+c.body()+" "+c.head()+" c.isBigC="+c.isBigC());
             }
                       
            
             if(c.isBigC()){
            	 if(!head.toUpperCase().contains("REF"))//avoid XREF="A" being translated as "alpha" in "C" mode
            		 tempBody=Translator.procGenCom(c.body(), 0, false);
            	 else
            		 tempBody=c.body();
            	 
            	 c.setBigC(false);
            	 c.setBody(tempBody, false);
            	 
              	//debug
              	//System.out.println("In TableMaker1: k="+k+" head="+head+" tempHead="+tempHead+" line="+c.body()+" "+c.head()+" c.isBigC="+c.isBigC());
             }            
             
             tempBody=tempHead+c.body();     
              
             /*
         	//debug
          	if(c.type().contains("G") && lastComLineInPrevPanel!=null && lastComLineInPrevPanel.contains("0.08")) {
          		//if(k==1)
          		//	System.out.println("    "+coms.get(0).cbody()+" ###"+coms.get(0).type()+"####");
             	System.out.println("In TableMaker 3035:processInRecordComments: tempLine="+tempLineLast);
             	System.out.println("         isContinued: "+isContinuedCom+" found="+found);
             	System.out.println("       k="+k+"      head="+head+" tempHead="+tempHead+" body="+c.body());
             	System.out.println("                   tempTranslated="+tempTranslated);
             	System.out.println("              lastComLineInPrevPanel="+lastComLineInPrevPanel);
             	System.out.println("                 leftoverOfLastComInPrevPanel="+leftoverOfLastComInPrevPanel);
             	System.out.println("              match pos="+pos1);
          	}
          	*/
             if(isContinuedCom && !found){
             	pos1=-1;
             	pos2=-1;
             	
             	Comment tempC=new Comment();
             	tempC.setBigC(c.isBigC());
             	tempC.setBody(c.body());
             	
             	if(head.length()>0)
             	    tempTranslated=head+tempC.getRegTranslated();
             	else
             		tempTranslated=tempC.getRegTranslated();

             	pos1=tempTranslated.indexOf(tempLineLast);
             	  
             	if(tempLine2ndLast.length()>0) {
                 	pos2=tempTranslated.indexOf(tempLine2ndLast);
                 	if(pos2>=0)
                 		hasFound2ndLast=true;
             	}

             	/*
             	if(pos1<0 && tempLine.contains("textnormal")) {
             		//somehow for translations of the same reaction ID, the A number could have textnormal or could not
             		//Even though it could have been fixed in Translator.java after this issue was found, here we
             		//still double check to make sure that issue no longer exists
             		//TO DO:
             	}
             	*/
             	
             	/*
            	//debug
             	if(c.type().contains("G") && lastComLineInPrevPanel!=null && lastComLineInPrevPanel.contains("0.08")) {
                	System.out.println("In TableMaker 3067:processInRecordComments: tempLine="+tempLineLast);
                	System.out.println("       k="+k+"      head="+head+" tempHead="+tempHead+" body="+c.body());
                	System.out.println("                                       tempTranslated="+tempTranslated);
                	System.out.println("              lastComLineInPrevPanel="+lastComLineInPrevPanel);
                	System.out.println("              tempLine2ndLast="+tempLine2ndLast);
                	System.out.println("                 leftoverOfLastComInPrevPanel="+leftoverOfLastComInPrevPanel);
                	System.out.println("              match pos="+pos1+"  hasFound2ndLast="+hasFound2ndLast);
             	}
             	*/
             	
                if(indexOfFirstMatchLast<0 && pos1>=0)
                	indexOfFirstMatchLast=k;
                
             	if(pos1<0 || (tempLine2ndLast.length()>0&&!hasFound2ndLast)){
             		//the 2nd condition is for skipping matching line in the middle that is identical to last line
                  
                     if(k==size-1){
                    	 
                    	if(indexOfFirstMatchLast>=0) {
                     		//have scanned all comments with no match found for the 2nd last but match found for the last line 
                    		//so start over from the match to write comments from the match                     		
                    		k=indexOfFirstMatchLast-1;
                    		
                    		//not really found the 2nd last, just mark to skip the 2nd condition for start-over to avoid infinite loop
                    		hasFound2ndLast=true;
                    	}else {
                     		//have scanned all comments but still no match found for the last and 2nd last line 
                    		//so all comments should be written into current panel
                     		//start over from beginning to write all comments
                    		
                         	k=-1;
                         	isContinuedCom=false;
                    	}

                     }
                     	
             		continue;
             	}else { 
             	    int tempPos=tempTranslated.length()-leftoverOfLastComInPrevPanel.length()-tempLineLast.length();

             	    /*
                	//debug
                 	if(c.type().contains("G") && lastComLineInPrevPanel!=null && lastComLineInPrevPanel.contains("0.08")) {
                    	System.out.println("In TableMaker 3103:processInRecordComments: tempLine="+tempLineLast);
                    	System.out.println("       k="+k+"      head="+head+" tempHead="+tempHead+" body="+c.body());
                    	System.out.println("                              tempTranslated="+tempTranslated);
                    	System.out.println("              lastComLineInPrevPanel="+lastComLineInPrevPanel);
                    	System.out.println("                 leftoverOfLastComInPrevPanel="+leftoverOfLastComInPrevPanel);
                    	System.out.println("          match pos="+pos1+" tempPos="+tempPos);
                 	}
                 	*/
             	    if(tempPos>0) {
             	        int pos0=pos1;
                        do {
                            if(Math.abs(tempPos-pos0)<=5) {
                                pos1=pos0;
                                break;
                            }else {
                                pos0=tempTranslated.indexOf(tempLineLast, pos0+2);
                                //System.out.println("    $$$$$$$$$$$ pos0="+pos0);
                            }
                                
                        }while(pos0>0);  
             	    }  
             	    
             	    
             	    //System.out.println("In TableMaker 3081: tempTranslated="+tempTranslated);
             	    //System.out.println("                 leftoverOfLastComInPrevPanel="+leftoverOfLastComInPrevPanel);
             	    //System.out.println("    ******** tempPos="+tempPos+"  pos1="+pos1);
             	}
             	
             	//System.out.println("    final********   pos1="+pos1);
             	
             	found=true;
             	int posFound=pos1;    
             	
             	int i=0,offsetPos=0;
             	String s=c.body();
             	String ss="";

             	while(i<s.length()){
             		pos1=s.indexOf(" ", ss.length());
             		if(pos1<0)
             			pos1=s.length()-1;
             		
             		ss=s.substring(0,pos1+1);
             		tempC.setBody(ss);
             		if(head.length()>0)
             			tempTranslated=head+tempC.getRegTranslated();
             		else
             			tempTranslated=tempC.getRegTranslated();
             		
             		pos2=tempTranslated.indexOf(tempLineLast,offsetPos);
             		
             		
             		//debug
             		//System.out.println("TableMaker 2468: i="+i);
             		//System.out.println("      tempLine="+tempLine);
             		//System.out.println("      ss      ="+ss);
             		//System.out.println("translated    ="+tempTranslated);
             		//System.out.println("      c.body  ="+s);
             		
             		
             		//System.out.println("In TableMaker line 2829: pos1="+pos1+" pos2="+pos2+" prevPos="+offsetPos+" posFound="+posFound+" tempLine.length="+tempLine.length()+" s.length="+s.length());
             		
             		i=pos1+1;
             		if(pos2<0 || Math.abs(pos2-posFound)>2) {
             		    if(pos2>0)
             		        offsetPos=pos2+1;
             		    
             			continue;
             		}else
             	        break;
             	}
             	
             	if(pos2>=0){//match found, that is, last comment line in previous panel is contained in this comment "c"
             		if(i==s.length()){//this comment "c" is completely in previous panel, so nothing to be written in current panel
             			tempBody="";
             			isLastComAcrossPages=false;
             			continue;
             		}
             	    else{             //this comment "c" is partially in previous panel, and the rest are to be written in current panel
             		    tempBody=s.substring(i).trim();
             		    if(tempBody.trim().length()>0){
             		    	tempBody="\\s\\s\\s"+tempBody;//leave 3 white-space indentation
             		    	isLastComAcrossPages=true;
             		    }
             	    }
             	}
             	

             }
             
             //if(tempBody.trim().length()==0)
             //	continue;

             
             //debug
             //System.out.println("In TableMaker 2500: k="+k+" procInRecrodCom A: isContinuedCom="+isContinuedCom+" body="+tempBody+" tempHead="+tempHead);
             //System.out.println("               commentColumnWidth="+commentColumnWidth+" body width="+Str.findWidth(tempBody,0.160f));
             
             int nindent=3;            
             tempComs=splitENSDFComment(tempBody,commentColumnWidth,nindent);//leave 3 white-spaces indentation after the first line
             
             /*
             //debug
             if(tempBody.contains("Nilsson")) {
                 System.out.println("In TableMaker 3184: procInRecrodCom B: isContinuedCom="+isContinuedCom+" body="+tempBody);
                 for(int m=0;m<tempComs.size();m++)
                 System.out.println("          nCommens="+coms.size()+" m="+m+" com.size="+tempComs.size()+" temComs"+m+"="+tempComs.elementAt(m));          	 
             }
             */
             
             //if(autoAdjust){
             if(true){ 	 
            	 if(tempComs.size()==1)
            		 linewidth_str="["+NDSConfig.PARBOX_HEIGHT1+"cm]";
            	 else
            		 linewidth_str="["+NDSConfig.PARBOX_HEIGHT3+"cm]";
             }
              	
             String str=tempBody;
             for(int m=0;m<tempComs.size();m++){
             	c1=new Comment();
             	c1.setBigC(c.isBigC());

             	if(m==0 && (!isLastComAcrossPages)){
             		
               		//debug
             		//System.out.println("In TableMaker: procInRecrodCom: head="+head+" tempHead="+tempHead+"***"+tempComs.elementAt(0));
             		
             		if(tempComs.elementAt(0).length()<=tempHead.length())
             			tempBody="";
             		else
             			tempBody=tempComs.elementAt(0).substring(tempHead.length());
             		
            		int start=str.indexOf(tempBody);
            		if(start>=0) {
            			tempBody=Translator.translate(str,start,start+tempBody.length()-1);
            		}else {                   	
                		c1.setBody(tempBody);
                		tempBody=c1.getRegTranslated();
            		}
            		
             		temp=head+tempBody;

             		//debug
             		//System.out.println("In TableMaker 2924: procInRecrodCom: head="+head+" tempHead="+tempHead+"***"+tempComs.elementAt(0));
             		//System.out.println("               c.body="+c1.body()+" translated="+c1.getRegTranslated());
             	}
             	else{
                    String indent_str=Str.repeat("\\s", nindent);
             		tempBody=tempComs.elementAt(m).replace(indent_str,"");
                    indent_str=Translator.translate(indent_str);
              		
            		
            		if(m>0 && tempComs.get(m-1).toUpperCase().trim().endsWith("ADOPTED")) {
            			String s1=tempBody.trim();
            			String s2=s1.toUpperCase();
            			if(s2.startsWith("LEVELS") || s2.startsWith("GAMMAS")) {
            				int n=tempBody.indexOf(s1);
            				tempBody=tempBody.substring(0,n)+s2.charAt(0)+tempBody.substring(n+1);
            			}
            		}
            		
               		int start=str.indexOf(tempBody);
            		if(start>=0) {
            			temp=Translator.translate(str,start,start+tempBody.length()-1);
            		}else {                   	
                		c1.setBody(tempBody);
                		temp=c1.getRegTranslated();
            		}

            		temp=indent_str+temp;
            		
            		/*
                 	//debug
                 	if(tempBody.contains("level")) {
                 		System.out.println("In TableMaker 3168: start="+start+" m="+m+" Head="+head+" isBigC="+c.isBigC()+" translated="+temp);
                 	    System.out.println("###Str="+str);
                 	    System.out.println("@@@tempBody="+tempBody);
                 	}
                 	*/
             	}
             	
            	
                if(toAddHyperlink)
                	temp=EnsdfUtil.insertContinuedXREFLinks(temp,XREFLinks,etd.getENSDF());
             	
             	//debug
             	//System.out.println("In TableMaker 2294: m="+m+" Head="+head+" isBigC="+c.isBigC()+" tempBody="+tempComs.elementAt(m)+" translated="+temp);
                
     			//row=("\\parbox["+Config.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright "+temp+"\\vspace{0.1cm}}");
     			row=("\\parbox[t]"+linewidth_str+"{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright "+temp+"\\vspace{0.1cm}}");
     			
     			//if(temp.length()>0)
     			  out.addElement(row);
     			
             }
             
             if(found)
            	 isLastComAcrossPages=false;
         }
            
         //if( lastComLineInPrevPanel.contains("0.08"))
         //	 System.out.println("TableMaker 3283: "+ lastComLineInPrevPanel+"  "+out.size()+"   "+out.get(0));
         
    	 return out;
     }
     
      
     
     /**convert the continuation records as comments*/
     protected Vector<Comment> convertContRecords(int panel,Record d,TableControl tc,boolean showSuppressed)throws Exception{

         Vector<Comment> out=new Vector<Comment>();
         
         String line="",type="";
         ContRecord cr;
         
         //if(d!=null)
         //	 System.out.println("TableMaker 3344: "+d.ES());
         
         if(d.recordLine().length()>7)
        	 type=d.recordLine().charAt(7)+"";
         
         //decay mode
         if(d instanceof Level){
             DecayMode dm;
             int n=0;
             
         	Level l=(Level)d;
         	n=l.nDecayModes();
         	line="";
             for(int k=0;k<n;k++){
                 dm=l.decayModeAt(k);
                 
                 if(line.length()>0){               	
                 	if(dm.isNewLine()){
                         Comment c=new Comment();
                         c.setBody(line,true);
                         c.setType(type);
                         out.addElement(c);  
                         line="";
                 	}
                 	else{
                     		line+="; ";     
                 	}
                 }
                     
                 line+=EnsdfUtil.printContRecord(dm.txt());
                 
                 //debug
                 //System.out.println("In TableMaker: line 2347: 3H, 3HE"+Translator.process("%3H")+"  "+Translator.process("3HE"));
                 //System.out.println("               line="+line+" translated="+Translator.process(line)+" procGen="+Translator.procGenCom(line,0,false));
             }
         }
         
         if(line.length()>0){
             Comment c=new Comment();
             c.setBody(line,true);
             c.setType(type);
             out.addElement(c);  
             line="";
         }
         
         
         int size=d.nContRecords();
  
         //System.out.println(size+" "+d.ES());
         
         for(int k=0;k<size;k++){
        	 
             cr=d.contRecordAt(k);

        	 //debug
        	 //System.out.println("In TableMaker: convertContRecords: cr=***"+cr.txt()+"***");
 
            if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) ){
            
                 if(line.length()>0){               	
                 	if(cr.isNewLine()){
                         Comment c=new Comment();
                         c.setBody(line,true);
                         c.setType(type);
                         out.addElement(c);  
                         line="";
                 	}
                 	else{                    		
                 		line+="; ";     
                 	}
                 }
                 
                 line+=EnsdfUtil.printContRecord(cr.txt());
 
                 //if(cr.txt().contains("LM"))
                 //	 System.out.println(cr.txt()+"    "+EnsdfUtil.printContRecord(cr.txt())+" line="+line);
             }
         }
         
         if(line.length()>0){
             Comment c=new Comment();
             c.setBody(line,true);
             c.setType(type);                          
             out.addElement(c);  
             line="";
         }
         
                  
    	 //debug
    	 //System.out.println("In TableMaker: convertContRecords: line="+line);
         
         return out;
     }
     
     protected Vector<Comment> convertContRecords(int panel,Record d,TableControl tc)throws Exception{return convertContRecords(panel,d,tc,false);}
     
 	 protected String commentHeadMaker(Comment c) throws Exception{return commentHeadMaker(c,"");}
 	
     /**process all comments of current record line-by-line to get the Latex-formatted strings*/
     protected Vector<String> procComsFormatted(int panel,Record d,TableControl tc)throws Exception{
     	return procComsFormattedPartial(panel,d,tc,"","");
     }

     protected Vector<String> procComsFormatted(int panel,Record d,TableControl tc,boolean showSuppressed)throws Exception{
    	 return procComsFormattedPartial(panel,d,tc,showSuppressed,"",""); 	 
     }

     protected Vector<String> procComsFormattedPartial(int panel,Record d,TableControl tc,String lastComLineInPrevPanel, String secondLastComLineInPrevPanel)throws Exception{
    	 return procComsFormattedPartial(panel,d,tc,false,lastComLineInPrevPanel,secondLastComLineInPrevPanel);
     }
     
     /**process partial comments of current record line-by-line to get the Latex-formatted strings if the comments are across pages*/
     //problem: what if the lastComeLineInPrevPanel is from ContRecords or DataAsComments
     //         also ContRecords could be printed twice if there is contRecord and the InRecordCom is across pages
     protected Vector<String> procComsFormattedPartial0(int panel,Record d,TableControl tc,boolean showSuppressed,String lastComLineInPrevPanel,String secondLastComLineInPrevPanel)throws Exception{
         
         Vector<Comment> coms=new Vector<Comment>();
         
        	 
         //add continuation records as comments
         coms.addAll(convertContRecords(panel,d,tc,showSuppressed));

         //add in-record comments line by line
         coms.addAll(d.commentV());
         
         int n=coms.size();
         
         //add data as comments 
         coms.addAll(convertDataAsComments(panel,d,tc));
         
         for(int i=n;i<coms.size();i++){
        	 Comment c=coms.get(i);
        	 if(c.isContinuedXREF()){
        		 coms.remove(c);
        		 coms.insertElementAt(c, 0);
        		 break;
        	 }     		 
         }
        	 
         return processInRecordComments(panel,coms,lastComLineInPrevPanel,secondLastComLineInPrevPanel);
     }
     
     /**process in-record comments, for J, T1/2, E, MR, and so on.
      * plus comment data like continued records, data-as-comment 
      */
      protected  Vector<String> procComsFormattedPartial(int panel, Record d,TableControl tc, boolean showSuppressed, String lastComLineInPrevPanel,String secondLastComLineInPrevPanel)throws Exception{  	 

    	  Vector<String> out=new Vector<String>();
    	  
          Vector<Comment> coms=new Vector<Comment>();
          
          
          //add continuation records as comments
          coms.addAll(convertContRecords(panel,d,tc,showSuppressed));
          
          /*
          if(d.ES().contains("1547.05")) {
        	  System.out.println("In TableMaker 3280: ");
        	  for(Comment c:coms) {
        		  System.out.println("     "+c.body());
        	  }
          }
          */
          
          //add in-record comments line by line
          coms.addAll(d.commentV());                    
          
          int n=coms.size();

          
          //add data as comments 
          coms.addAll(convertDataAsComments(panel,d,tc));

          
          for(int i=n;i<coms.size();i++){
         	 Comment c=coms.get(i);
    
         	 if(c.isContinuedXREF()){
         		 coms.remove(c);
         		 coms.insertElementAt(c, 0);
         		 break;
         	 }     		 
          }
          
          boolean isContinuedCom=false;
          
          if(lastComLineInPrevPanel!=null && lastComLineInPrevPanel.trim().length()>0)
              isContinuedCom=true;
          
          if(secondLastComLineInPrevPanel==null)
        	  secondLastComLineInPrevPanel="";
          
          //System.out.println("###In TableMaker line 2479: e="+d.EF()+" isContinuedCom="+isContinuedCom+" out.size="+out.size()+" lastComLineInPrevPanel="+lastComLineInPrevPanel);
          
          String tempLineLast="",tempLine2ndLast="";
          if(isContinuedCom){
              int pos=lastComLineInPrevPanel.indexOf("\\parbox");
              if(pos>=0) {
             	 tempLineLast=lastComLineInPrevPanel.substring(pos).trim();   
             	
             	 pos=secondLastComLineInPrevPanel.indexOf("\\parbox");
             	 if(pos>=0)
             		tempLine2ndLast=secondLastComLineInPrevPanel.substring(pos).trim();  
              }else
             	 isContinuedCom=false;
          }
          
          boolean found=false;
          
          
          /*
      	 //debug
          if(coms.size()>0 && coms.get(0).type().contains("L") && isContinuedCom) {                   	
        	  System.out.println("In TableMaker line 3219: e="+d.EF()+" isContinuedCom="+isContinuedCom+" out.size="+out.size()+" lastComLineInPrevPanel="+lastComLineInPrevPanel);
              System.out.println("              found="+found+" tempLine="+tempLine+" coms(0)="+coms.get(0).body()+" "+coms.get(0).head());          	
              for(Comment c2:coms)
          		 System.out.println("   comment line: "+c2.body()); 
          }
          */

          
          //add continuation record, in-record comments, data as comment line by line
          if(!isContinuedCom || found){
        	  out.addAll(processInRecordComments(panel,coms,"",""));
          }
          else{      	  
        	  out.addAll(processInRecordComments(panel,coms,tempLineLast,tempLine2ndLast));    
          }
          
          //debug
          //if(out.size()>0)
          //System.out.println("***In TableMaker line 2502: e="+d.EF()+" isContinuedCom="+isContinuedCom+" out.size="+out.size()+" out[0]="+out.get(0));

          /*
          if(d.ES().contains("1547.05")) {
        	  System.out.println("In TableMaker 3351: ");
        	  for(String line:out) {
        		  System.out.println("     "+line);
        	  }
          }
          */
          
     	 return out;
      }
      
     protected Vector<String> procComs_old(int panel,Record d,TableControl tc)throws Exception{
    	 return procComs_old(panel,d,tc,false);
     }
     
     /**process the comments comment-by-comment*/
     protected Vector<String> procComs_old(int panel,Record d,TableControl tc,boolean showSuppressed)throws Exception{
         Vector<String> out=new Vector<String>();
         String row="";
                 
         Vector<Comment> coms=new Vector<Comment>();
         
         //add continuation records as comments
         coms.addAll(convertContRecords(panel,d,tc,showSuppressed));

         //add in-record comments line by line
         coms.addAll(d.commentV());
         
         //add data as comments 
         coms.addAll(convertDataAsComments(panel,d,tc));

         int size=coms.size();

         for(int n=0;n<size;n++){
             row=("\\parbox[c]{"+tc.getCommentColumnWidth()+"cm}{\\raggedright "+commentBuilder(coms.elementAt(n))+"\\vspace{0.1cm}}");
             out.addElement(row);
         }
                  
         return out;
     }
     
     protected Vector<String> procComs(int panel,Record d,TableControl tc)throws Exception{
    	 return procComs(panel,d,tc,false);
     }
     
     protected Vector<String> procComs(int panel,Record d,TableControl tc,boolean showSuppressed)throws Exception{
    	 if(NDSControl.autoAdjust)
    		 return procComsFormatted(panel,d,tc,showSuppressed);
    	 else
    		 //return procComs_old(panel,d,tc,showSuppressed);
    		 return procComsFormatted(panel,d,tc,showSuppressed);
     }
     
     protected float findMaxDataWidth(HashMap<String,String> maxColStrings,HashMap<String,String> headStrings){
         
         String maxColString="",headString="";
         float maxDataWidth=0f;
         float colWidth,headWidth;
         
         //debug
         //System.out.println("\nIn TableMaker: line 2239: findMaxDataWidth***********");
         
         for(String key:maxColStrings.keySet()){
         	if(key.equals("COMM"))
         		continue;
         	
         	maxColString=maxColStrings.get(key);
         	if(maxColString.trim().length()==0 && !tc.getDefaultFixedColumns().contains(key))
         		continue;
         	
         	//colWidth=maxColString.length()*Config.DIGIT2CM;
         	colWidth=NDSConfig.findWidth(maxColString);
         	if(key.equals("XREF"))//plain text, all characters have equal widths
         		colWidth=Str.findWidth(maxColString,0.160f);
         	
         	headString="";  
         	if(headStrings.containsKey(key))
         		headString=headStrings.get(key);
         	
            //headWidth=headString.length()*Config.CHAR2CM;
         	headWidth=NDSConfig.findWidth(headString);
             
            //debug
            //System.out.println("In TableMaker 3508: findMaxDataWidth: table="+this.tableType()+" name="+key+" string="+maxColString+" width="+colWidth+" head="+headString+" width="+headWidth);
             
         	if(headWidth>colWidth)
         		colWidth=headWidth;
         	

         	maxDataWidth+=colWidth+NDSConfig.COLUMN_GAP;
         	
         	if(key.equals("JF"))
         		maxDataWidth-=0.1f;//JF columns is squeezed toward "EF" column, in LatexWriter.java line 1602
         	
            //debug
         	//if(tableType().equals("DECAY"))
            //System.out.println("In TableMaker 3692: findMaxDataWidth2: name="+key+" maxColString="+maxColString+" headString="
         	//   +headString+" colWidth="+colWidth+" headWidth="+headWidth+" total width="+maxDataWidth+" COLUMN_GAP="+NDSConfig.COLUMN_GAP);
         }
         
         return maxDataWidth;
     }
     
     protected float findDataHeadWidth(HashMap<String,String> maxColStrings,HashMap<String,String> headStrings){
         
         String maxColString="",headString="";
         float dataHeadWidth=0f;
         float colWidth,headWidth;
         
         //debug
         //System.out.println("\nIn TableMaker: line 2239: findMaxDataWidth***********");
         
         for(String key:headStrings.keySet()){
            if(key.equals("COMM"))
                continue;
            
            headString=headStrings.get(key);
            if(headString.trim().length()==0)
                continue;
            
          
            //headWidth=headString.length()*Config.CHAR2CM;
            headWidth=NDSConfig.findWidth(headString);
                     
            maxColString="";  
            if(maxColStrings.containsKey(key))
                maxColString=maxColStrings.get(key);
            
            //colWidth=maxColString.length()*Config.DIGIT2CM;
            colWidth=NDSConfig.findWidth(maxColString);
            if(key.equals("XREF"))//plain text, all characters have equal widths
                colWidth=Str.findWidth(maxColString,0.160f);
             
            if(colWidth>headWidth)
                headWidth=colWidth;

            //System.out.println("In TableMaker 3536 findMaxDataWidth2: name="+key+" maxColString="+maxColString+" headString="+headString+" colWidth="+colWidth+" headWidth="+headWidth+" total width="+dataHeadWidth);

            /*
            if(maxColString.isEmpty()) {
                //System.out.println("In TableMaker 3536 findMaxDataWidth2: name="+key+" maxColString="+maxColString+" headString="+headString+" colWidth="+colWidth+" headWidth="+headWidth+" total width="+dataHeadWidth);

                if(key.equals("MR"))
                	headWidth=headWidth*2;
                else if(key.equals("M"))
                	headWidth+=0.1;
                else if(key.equals("JI"))
                	headWidth+=0.1;
            }
            */
            	
            dataHeadWidth+=headWidth+NDSConfig.COLUMN_GAP;
            
            if(key.equals("JF"))
                dataHeadWidth-=0.1f;//JF columns is squeezed toward "EF" column, in LatexWriter.java line 1602            
            
            //debug
            
            //if("M,MR".contains(key)) {
            //	System.out.println("In TableMaker 3540 findMaxDataWidth2: name="+key+" maxColString="+maxColString+" headString="+headString+" colWidth="+colWidth+" headWidth="+headWidth+" total width="+dataHeadWidth);
            //}
         }
         
         return dataHeadWidth;
     }
     
     /*
      * convert flags (recordFlags) given in ENSDF to corresponding ones in 
      * output flags (footnotes) and insert them into the output value+unc string
      * footnotes: a wrap of all footnote comments and flag information
      */
     protected String addFlagsLatex(String s,String recordFlags,Footnotes footnotes,String type){

         String tempFlags="";
         String temp=s;
         
         if(recordFlags.trim().length()<=0)
        	 return temp;
         
         tempFlags=footnotes.getFlagSymbols(recordFlags, type);
         
         if(tempFlags.length()>0)
         	tempFlags="\\ensuremath{^{"+tempFlags+"}}";
         else
         	return temp;
         
         temp=s.trim();
         int n1=s.indexOf(temp);
         int n2=temp.indexOf(" ");
         if(n1>=0 && n2>0){
         	n2=n1+n2;
         	temp=s.substring(0, n2)+tempFlags+s.substring(n2);
         }
         else
         	temp=s+tempFlags;
         
         
         //debug
         //if(s.indexOf("37")==0)System.out.println(" S="+s+" n1, n2="+n1+" "+n2+" temp="+temp+" flag="+tempFlags);
         
         return temp;
     }
     
     /*
      * insert latex flags into a value+unc string
      * NOTE:this function is used when a latex falgs is already ready
      */
     protected String addFlagsLatex(String s,String latexFlags){

         String tempFlags=latexFlags.trim();
         String temp=s;
         
         if(tempFlags.length()<=0)
        	 return temp;
                 
         	
         tempFlags="\\ensuremath{^{"+tempFlags+"}}";

         
         temp=s.trim();
         int n1=s.indexOf(temp);
         int n2=temp.indexOf(" ");
         if(n2>0){
         	n2=n1+n2;
         	temp=s.substring(0, n2)+tempFlags+s.substring(n2);
         }
         else
         	temp=s+tempFlags;
         
         return temp;
     }
     
     protected String addQuestionMark(String s){

         String temp=s;
         
         
         temp=s.trim();
         int n1=s.indexOf(temp);
         int n2=temp.indexOf(" ");
         if(n2>0){
         	n2=n1+n2;
         	temp=s.substring(0,n2)+"?"+s.substring(n2);
         }
         else
         	temp=s+"?";
         
         return temp;
     }
     
     
     /*
      * check if a record has flags to be printed. If the recordFlags has a flag
      * in footnotes, then it has flag to be printed.
      */
     protected boolean hasPrintFlag(String recordFlags,Footnotes footnotes,String type){

         return footnotes.hasFootmark(recordFlags, type);
     	
     }
     
     /* for a given value v in range of [e1,e2] with a roughly estimated ndvisions=size,
      * find the actual size (between size-offset and size+offset) and the index of v, 
      * if availalbe.
      */
     protected int[] findIndexAndSize(float v,float e1,float e2,int size,int loffset,int roffset){
    	 int[] a=new int[2];
    	 a[0]=-1;
    	 a[1]=-1;
    	 
    	 int n,index;
    	 float de,f;

    	 
    	 if(v<e1 || v>e2 || e1>=e2 || size<=0 || loffset<0 || roffset<0)
    		 return a;
    	 
    	 for(n=size-loffset;n<=size+roffset;n++){
        	 if(n<=0) continue;
        	 
        	 de=(e2-e1)/n;  	 
        	 f=(v-e1)/de;
        	 index=(int)(f+0.0001f);
        	 if(Math.abs(f-index)<1E-6){
        		 a[0]=index;
        		 a[1]=n;
        		 break;
        	 }
    	 }

    	 
    	 return a;
     }
     
     protected int[] findIndexAndSize(float v,float e1,float e2,int size,int offset){
    	 int[] a=new int[2];
    	 a[0]=-1;
    	 a[1]=-1;
    	 
    	 int n,index;
    	 float de,f;
    	 int ntries=2*offset+1;
    	 
    	 if(v<e1 || v>e2 || e1>=e2 || size<=0 || offset<0)
    		 return a;
    	 
    	 for(int i=0;i<ntries;i++){
        	 n=size+(1-2*(i%2))*((i+1)/2);
        	 if(n<=0) continue;
        	 
        	 de=(e2-e1)/n;  	 
        	 f=(v-e1)/de;
        	 index=(int)(f+0.0001f);
        	 if(Math.abs(f-index)<1E-6){
        		 a[0]=index;
        		 a[1]=n;
        		 break;
        	 }
    	 }

    	 
    	 return a; 
     }
     
     //for LevelTableMaker and DecayTableMaker only.
     //input flagStr is translated LaTex string.
     protected int findNflagsLatex(String flagStr){
    	 int nflags=0;
    	 
    	 if(flagStr.trim().length()>0)//assure at least one flag
    		 nflags=1;
    	 
    	 int count=Str.countPattern(flagStr, "hyperlink");
    	 if(count>0)
    		 nflags+=count-1;
    	 
    	 return nflags;
     }
     
     //for LevelTableMaker and DecayTableMaker only
     //find the number of flagmarks in "flags" for all flag label in 
     //flagStr with the given "type"
     //input flagStr contains original flag labels in records
     protected int findNflagsByType(Vector<ComFlag> flags,String flagStr,String type){
    	 int nflags=0;
    	 ComFlag cf;

         for(int j=0;j<flags.size();j++){
             cf=(ComFlag)flags.elementAt(j);
             
             if(cf.hasFlagAny(type,flagStr)){
            	 //debug
            	 //System.out.println("in TableMaker line 2509: type="+type+" flagStr="+flagStr);
            	 
            	 nflags++;
             }

         }
         
    	 return nflags;
     }
     
     public int nTableRecordsLines() {return nTableRecordsLines;}
     public float tableRecordsHeight() {return tableRecordsHeight;}
     
     //used to store information for finding the column string with the maximum width 
     public class StringMax{
    	 public String flags="";
    	 public String leftStr="";
    	 public String rightStr="";
    	 public String delim="";
    	 public String str="";
    	 
    	 public float width=0; 
    	 public float leftWidth=0; 
    	 public float rightWidth=0; 
    	 
    	 public StringMax(){
        	 flags="";
        	 leftStr="";
        	 rightStr="";
        	 delim="";
        	 str="";
        	 
        	 width=0; 
        	 leftWidth=0; 
        	 rightWidth=0; 
    	 }
    	 
    	 public StringMax(String flagStr){
    		 this();
    		 flags=flagStr;
    	 }
    	 
    	 public void compare(String temp,String flags){
    		 
             String decimalLeft="";
             String decimalRight="";
             String fakeFlagStr=flags;
             
             float decimalLeftWidth=0;            
             float decimalRightWidth=0;
             
             
    	       decimalLeft=Str.delimLeftString(temp, '.');//aligned by '.'
               decimalRight=Str.delimRightString(temp, '.');
               
               //otherwise, aligned by ' ', also for case like 1.53E3 12.
               //NOTE that: the condition should be the same as that in makeEntry() in each TableMaker
               if(!temp.contains(".") || (Str.containExpFormat(temp) && !temp.contains("-"))){
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
              
               
               if(decimalLeftWidth>leftWidth){                 
           	   leftWidth=decimalLeftWidth;
           	   leftStr=decimalLeft;
               }
               if(decimalRightWidth>rightWidth){                  	
           	   rightWidth=decimalRightWidth;
           	   rightStr=decimalRight;
               }
                
               str=leftStr+rightStr;
               width=leftWidth+rightWidth;
    	 }
    	 
     }

}
