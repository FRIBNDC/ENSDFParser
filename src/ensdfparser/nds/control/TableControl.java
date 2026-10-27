package ensdfparser.nds.control;

import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.nds.config.*;
import ensdfparser.nds.latex.*;

import java.io.*;


/** contains data and functions that are used by all the different types of tables*/
public class TableControl implements Cloneable{
	private final static int NUM=100;
	
	protected Vector<String> ensdfFieldNames;//all field names in an ENSDF record in current type of table
    protected Vector<String> defaultColumns,firstPanelColumns;//all columns that have data to display
    protected Vector<String> defaultFixedColumns;//columns that must display even it is empty
    
    protected HashMap<Integer,Vector<String>> fixedColumnsMap;//currently not used
    
    protected float commentColumnWidth;//default comment column width
    protected float[] commentColumnWidths=new float[NUM];//store actual comment column width for each panel
    
    protected String[] lastLines=new String[NUM];//store last line information of each panel
    protected String[] secondLastLines=new String[NUM];//store 2nd last line information of each panel 
    
    protected float[]  lastLineEnergies=new float[NUM];
    protected float[]  lastLineLevelEnergies=new float[NUM];

    
    protected String[] firstLines=new String[NUM];//store first line information of each panel
    protected float[]  firstLineEnergies=new float[NUM];
    protected float[]  firstLineLevelEnergies=new float[NUM];
    
    protected boolean[] isLastLevelSameAsNextFirst=new boolean[NUM];
    
    protected String[] leftoversOfLastCom=new String[NUM];//store leftover of last comment to be printed in next panel
    
    protected Vector<Comment> comments;
    protected boolean showDE;
    
    HashMap<String,String> relabelMap=new HashMap<String,String>();//(headName,relabel)
    HashMap<String,String> translatedLabelMap=new HashMap<String,String>();//(headName,translated relabel)
    
    protected Vector<String> dataAsComments;
    protected Vector<String> Fcolumns;
    protected boolean isDrawn;
    protected boolean isNewPage;//if the table is drawn on a new page (first table panel)
    protected boolean isSeparateFoots;
    protected boolean isEmpty;//if the table is empty
    
    private float headWidth;

    private boolean isUpdated;
    private boolean isSmall;
    private boolean isNewCommentWidth;
    private boolean isFormatTables;
    private boolean isPortrait;//global setting for all table panels of a table, but there could be different orientations for different panels
    private boolean showAll;
    private boolean showSuppressed;
    private boolean isSortGammas;//for gamma table only
    private boolean isSortDelays;//for delay table only
    private boolean isBodyInNewPage;//used only for cases with comments/table records that can be kept in current page, but table body must be moved to new page
                                    //since there is no enough room for at least two table lines after table heading
    
    private boolean useAutoCommentWidth;//width calculated automatically; used in manual mode but need comment width to be calculated automatically
                                        //in auto mode, comment width is always calculated automatically
    
    private boolean reorderGamma;//ordering follows PN option by default, unless user selects to reorder in Control Settings for each data set (manul mode)
    
    private int nrecords;
    
    private Vector<Break> breaks;
    
    private String tableType="";//"G","L","D","P"
    
    private int currentPanelNo=0;//used in setting TableControlPanel
    
    public TableControl(){
        comments=new Vector<Comment>();
        defaultColumns=new Vector<String>();
        defaultFixedColumns=new Vector<String>();
        
        fixedColumnsMap=new HashMap<Integer,Vector<String>>();
        
        ensdfFieldNames=new Vector<String>();
        breaks=new Vector<Break>();
        isSeparateFoots=false;        
        isNewCommentWidth=false;
        isUpdated=false;
        isFormatTables=false;
        isPortrait=true;
        showAll=false;
        isSmall=false;
        isEmpty=true;
        
        isBodyInNewPage=false;
        
        nrecords=0;
        showSuppressed=true;
        
        useAutoCommentWidth=false;
        
        reorderGamma=false;
        
        for(int i=0;i<NUM;i++) {
        	commentColumnWidths[i]=5.0f;
        	
        	fixedColumnsMap.put(i,new Vector<String>());
        	
        	leftoversOfLastCom[i]="";
        	
        	isLastLevelSameAsNextFirst[i]=false;
        }
    }
    
    
    public TableControl(String[] fieldNames,String type){
    	this();
    	for(int i=0;i<fieldNames.length;i++){
    		ensdfFieldNames.add(fieldNames[i]);
    	}
    	tableType=type;
    }
    
    //ensdfFieldNames are used to identify the fields that can have footnote comments
    //eg, for comment like "cG BE2W$....", the head="BE2W", which can not have footnote
    //comments, and it is not a standard record field. All such comments with head not in
    //ensdfFieldNames go to the top of tables with the head also printed.
    public TableControl(String[] fieldNames){
    	this(fieldNames,"");
    }
    
    
    /**sets all the variables in this object based on a line of input from the control file
     * @throws Exception */
    @SuppressWarnings("unchecked")
	public void loadControlLine(String line) throws Exception{
        isDrawn=true;
        isSeparateFoots=false;
        String temp;

        defaultColumns=NDSControl.getLine(line,"/D:");
        if(defaultColumns!=null){
        	if(defaultColumns.contains("LEV"))defaultColumns.insertElementAt("DLEV", defaultColumns.indexOf("LEV")+1);
        	if(defaultColumns.contains("LEVF"))defaultColumns.insertElementAt("DLEVF", defaultColumns.indexOf("LEVF")+1);        
        	if(defaultColumns.contains("E"))defaultColumns.insertElementAt("DE", defaultColumns.indexOf("E")+1);
        	if(defaultColumns.contains("RI"))defaultColumns.insertElementAt("DRI", defaultColumns.indexOf("RI")+1);
           	if(defaultColumns.contains("MR"))defaultColumns.insertElementAt("DMR", defaultColumns.indexOf("MR")+1);
        }else isDrawn=false;
        
        dataAsComments=NDSControl.getLine(line,"/O:");
        if(dataAsComments!=null&&!((String)defaultColumns.elementAt(defaultColumns.size()-1)).equals("COMM")) 
            defaultColumns.add("COMM"); 
        if(line.contains("/E:")) showDE=false;
        else showDE=false;
        if(line.contains("/P:")) isNewPage=true;
        else isNewPage=false;
        if(line.contains("/A:")) showAll=true;
        else showAll=false;
        if(line.contains("/T:")) isFormatTables=true;
        if(line.contains("/H:"))isPortrait=false;
        if(line.contains("/M:"))isSmall=true;
        
        if(line.contains("/W:")){ 
        	setCommentColumnWidth(Float.parseFloat(NDSControl.getLineAsString(line,"/W:")));
        	isNewCommentWidth=true;       	
        }
        
        Fcolumns=NDSControl.getLine(line,"/F:");
        if(Fcolumns!=null){
            temp="";
            Comment c=new Comment();
            Vector<String> v=new Vector<String>();
            for(int i=0;i<Fcolumns.size();i++){
                temp+=NDSControl.transColHead((String)Fcolumns.elementAt(i));
            }
            v.add(temp);
            c.setValues(v);
            comments.add(c);
        }
        //get column breaks
        breaks=new Vector<Break>();
        Vector<String>col=(Vector<String>)defaultColumns.clone();
        int ix=0,ib,x2;
        String inBrackets;
        while(ix>=0){
            ix=line.indexOf("{",ix+1);
            if(ix<0) break;
            ib=line.indexOf("}",ix+1);
            inBrackets=line.substring(ix+1,ib);
            temp=NDSControl.getLine(inBrackets,"/B:").elementAt(0);
            if(temp.contains("F")) 
            	isSeparateFoots=true;
            else{
                Break b=new Break();
                b.pos=temp;
                b.sups=NDSControl.getLine(inBrackets,"/S:");
                if(b.sups!=null){//suppress columns
                    for(int i=0;i<b.sups.size();i++) col.removeElement(b.sups.elementAt(i));
                }            
                b.restore=NDSControl.getLine(inBrackets,"/R:");
                if(b.restore!=null){//see if any of the columns need to be restored
                    x2=0;
                    for(int i=0;i<defaultColumns.size();i++){
                        if(x2>=col.size()){
                            for(int j=0;j<b.restore.size();j++){
                                if(b.restore.elementAt(j).equals(defaultColumns.elementAt(i)))
                                    col.add(b.restore.elementAt(j));
                                }                            
                        }
                        else{
                            if(col.elementAt(x2).equals(defaultColumns.elementAt(i))) x2++;
                            else{
                                for(int j=0;j<b.restore.size();j++){
                                    if(b.restore.elementAt(j).equals(defaultColumns.elementAt(i))){
                                        col.insertElementAt(b.restore.elementAt(j), x2);
                                        x2++;
                                        break;
                                    }
                                }//for
                            }//else
                        }
                    }
                    for(int i=0;i<b.restore.size();i++){
                        if(!defaultColumns.contains(b.restore.elementAt(i))) col.add(b.restore.elementAt(i));
                    }
                }//if null
                b.panelColumns=(Vector<String>)col.clone();
                if(inBrackets.contains("/N:")) b.isNewPage=true;
                b.gammaPos=NDSControl.getLineAsFloat(inBrackets,"/G:");
                breaks.add(b);
 
//debug                
                //System.out.println("In TableConrol:newpage="+b.newPage);
                //System.out.println(line);
            }
        }
        Break [] temp2=new Break[breaks.size()];
        if(breaks.size()>0){
            breaks.copyInto(temp2);
            Arrays.sort(temp2,new BreakComparator());
        }
        for(int i=0;i<breaks.size();i++){
            breaks.setElementAt(temp2[i],i);
        }
    }
    /**writes a new control file based on the variables in this object*/
    public void writeControlLine(java.io.Writer out)throws IOException{
            out.write("/M:");
            
            Vector<String> tempColumns=new Vector<String>();
            String columnName;
            //for merged panels, there are duplicate column names in the defulatColumns
            for(int i=0;i<defaultColumns.size();i++){
            	columnName=(String)defaultColumns.elementAt(i);
                if(columnName.indexOf("D")!=0 && !tempColumns.contains(columnName))
                	tempColumns.add(columnName);
            }
            for(int i=0;i<tempColumns.size();i++){
                out.write((String)tempColumns.elementAt(i));
                if(i<tempColumns.size()-1) out.write(",");
            }           
            
            
            if(dataAsComments!=null){
                out.write("/O:");
                for(int i=0;i<dataAsComments.size();i++){
                    out.write((String)dataAsComments.elementAt(i));
                    if(i<dataAsComments.size()-1) out.write(",");
                }
            }
            if(Fcolumns!=null){
                out.write("/F:");
                for(int i=0;i<Fcolumns.size();i++){
                    out.write((String)Fcolumns.elementAt(i));
                    if(i<Fcolumns.size()-1) out.write(",");
                }
            }
            
            if(defaultColumns.contains("COMM") && isNewCommentWidth){
            	out.write("/W:"+String.format("%.1f",getMinCommentColumnWidth()));
            }
            
            if(!showDE) out.write("/E:");
            if(isNewPage) out.write("/P:");
            if(showAll) out.write("/A:");
            if(isFormatTables) out.write("/T:");
            if(isSeparateFoots) out.write("{/B:F}");
            if(isSmall)out.write("/M:");;
            if(!isPortrait)out.write("/H:");
            if(breaks!=null){
                Break b;
                for(int i=0;i<breaks.size();i++){
                    b=breaks.elementAt(i);
                    out.write("{/B:"+b.pos);
                    if(b.gammaPos!=-1) out.write("/G:"+b.gammaPos);
                    if(b.sups!=null){
                        out.write("/S:");
                        for(int j=0;j<b.sups.size();j++){
                            out.write(b.sups.elementAt(j));
                            if(j<b.sups.size()-1) out.write(",");
                        }
                    }
                    if(b.restore!=null){
                        out.write("/R:");
                        for(int j=0;j<b.restore.size();j++){
                            out.write(b.restore.elementAt(j));
                            if(j<b.restore.size()-1) out.write(",");
                        }
                    }
                    if(b.isNewPage) out.write("/N:");
                    out.write("}");
                }
            }
            out.write("\n");
        
    }
    /**if there are suppressed comment records, deselect that column in the table
     returns whether or the number of columns has changed*/
    public boolean refreshColumns(boolean suppressComment){
        boolean hasComCol=false,refresh=false;
        @SuppressWarnings("unused")
		int index=0;
        for(int i=0;i<defaultColumns.size();i++){
            if(((String)defaultColumns.elementAt(i)).equals("COMM")){
                hasComCol=true;
                index=i;
            }
        }
        if(!suppressComment&&!hasComCol){ defaultColumns.add("COMM"); refresh=true;
            for(int i=0;i<breaks.size();i++) breaks.elementAt(i).panelColumns.add("COMM");
        }
        if(suppressComment&&hasComCol) {defaultColumns.removeElement("COMM"); refresh=true;
            for(int i=0;i<breaks.size();i++) breaks.elementAt(i).panelColumns.removeElement("COMM");            
        }
        return refresh;
    }

    
    /**check orientation of a table panel based on page size **/
    public boolean isPortrait(TablePanel tp){
    	boolean portrait=true;
    	float dataWidth=tp.getDataWidth();
    	float totalWidth=tp.getTotalWidth();
    	boolean noComment=false;
    	if(dataWidth==totalWidth)
    		noComment=true;
    	
    	if(noComment){
        	if(dataWidth>=NDSConfig.MAX_TOTAL_WIDTH)
        		portrait=false;   		
    	}
    	else{
        	if(dataWidth>NDSConfig.MAX_DATA_WIDTH && totalWidth>NDSConfig.MAX_TOTAL_WIDTH)
            	portrait=false;    		
    	}
   	
    
    	
    	return portrait;
    }
    
    //access functions
 
    public Vector<String> getPanelColumns(int panelNo){  	
    	
    	//System.out.println(defaultColumns.size());

    	Vector<String> defaultColumnsCopy=new Vector<String>();
    	defaultColumnsCopy.addAll(defaultColumns);
    	
    	//return defaultColumnsCopy;
    	
    	
        if(panelNo<0 || (breaks.size()==0))
        	return defaultColumnsCopy;//set in EnsdfTableData or reading control file
        
        if(panelNo==0){
        	
        	if(firstPanelColumns==null)
        		return defaultColumnsCopy;
        	else
        		return firstPanelColumns;//set in findBreaks when making tables (TableMaker class, automode)
        }
        else{
        	if(breaks.elementAt(panelNo-1).panelColumns!=null)
        		return breaks.elementAt(panelNo-1).panelColumns;
        	else
        		return defaultColumnsCopy;
        }
        
        
    }
    
    public void setPanelColumns(int panelNo,Vector<String> panelColumns){  	
		
        if(panelNo<0 || panelColumns==null)
        	return;   
        if(panelNo>breaks.size())
        	return;
        
        if(panelNo==0)
        	this.setFirstPanelColumns(panelColumns);
        else
        	breaks.elementAt(panelNo-1).setPanelColumns(panelColumns);
        
        return;
    }
    
    public Vector<String> getBreakPoints(){
        Vector<String> out=new Vector<String>();
        for(int i=0;i<breaks.size();i++) 
        	out.add(breaks.elementAt(i).pos);
        return out;
    }

    public Vector<Break> getBreaks(){
        return breaks;
    }
    
    public float getGammaBreakPoint(int i){
    	if(breaks.size()==0)
    		return 100000;
    	
    	return breaks.elementAt(i).gammaPos;    	
    }
    
    public HashMap<String,String> getRelabelMap(){return relabelMap;}
    public HashMap<String,String> getTranslatedLabelMap(){return translatedLabelMap;}
    
    public Vector<String> getDataAsComments(){return dataAsComments;}
    public Vector<String> getFcolumns(){return Fcolumns;}
    public Vector<Comment> getComments(){return comments;}
    public Vector<String> getDefaultColumns(){return defaultColumns;}
    public Vector<String> getEnsdfFieldNames(){return ensdfFieldNames;}
    public String getTableType(){return tableType;}
    
    public Vector<String> getDefaultFixedColumns(){return defaultFixedColumns;}
     
    public Vector<String> getFixedColumns(int panel){
        if(panel<0 || panel>=NUM)
            return defaultFixedColumns;

        return fixedColumnsMap.get(panel);
    }
    
    public String getTableTypeName(){
    	if(tableType.equals("L"))
    		return "Level"; 	
    	if(tableType.equals("G"))
    		return "Gamma";
    	if(tableType.equals("D"))
    		return "Decay"; 	
    	if(tableType.equals("P"))
    		return "Delay";
    	
    	return "";
    	
    }
    
    
    public Vector<String> getFirstPanelColumns(){return firstPanelColumns;}
    public float getHeadWidth(){return headWidth;}
    public float getCommentColumnWidth(){return commentColumnWidth;}
    public float getCommentColumnWidth(int panel){
    	if(panel<0 || panel>=NUM)
    		return commentColumnWidth;

    	return commentColumnWidths[panel];	
    }
    
    
    public float getMinCommentColumnWidth(){
    	float min=1000;
    	int size=breaks.size()+1;
        for(int i=0;i<size;i++){
        	if(commentColumnWidths[i]<min)
        		min=commentColumnWidths[i];
        }
        
        if(min==1000)
        	min=NDSConfig.MIN_COMMENT_WIDTH;
        
        return min;
    }
    
    public float getLastLineEnergy(int panel){
    	if(panel<0 || panel>=NUM)
    		return -1;
    	
    	return lastLineEnergies[panel];
    }

    public float getLastLineLevelEnergy(int panel){
    	if(panel<0 || panel>=NUM)
    		return -1;
    	
    	return lastLineLevelEnergies[panel];
    }
    
    public String getLastLine(int panel){
    	if(panel<0 || panel>=NUM)
    		return "";
    	
    	return lastLines[panel];
    }
    public String getSecondLastLine(int panel){
    	if(panel<0 || panel>=NUM)
    		return "";
    	
    	return secondLastLines[panel];
    }
    
    public String[] getLastLines(int panel){
    	if(panel<0 || panel>=NUM)
    		return new String[0];
    	
    	Vector<String> tempV=new Vector<String>();
    	tempV.add(secondLastLines[panel]);
    	tempV.add(lastLines[panel]);//the last line is the last one in the array
    	String[] ar=new String[tempV.size()];
    	tempV.toArray(ar);
    	return ar;
    	   	
    }
    
    public float getFirstLineEnergy(int panel){
    	if(panel<0 || panel>=NUM)
    		return -1;
 
    	
    	return firstLineEnergies[panel];
    }

    public float getFirstLineLevelEnergy(int panel){
    	if(panel<0 || panel>=NUM)
    		return -1;
 
    	
    	return firstLineLevelEnergies[panel];
    }
    
    public String getFirstLine(int panel){
    	if(panel<0 || panel>=NUM)
    		return "";
    	
    	return firstLines[panel];
    }
    
    public String getLeftOverOfLastCom(int panel) {
        if(panel<0 || panel>=NUM)
            return "";
        
        return leftoversOfLastCom[panel];
    }
    
    public boolean isLastLevelSameAsNextFirst(int panel) {
    	return isLastLevelSameAsNextFirst[panel];
    }
    
    public void SetIsLastLevelSameAsNextFirst(int panel,boolean b) {
    	isLastLevelSameAsNextFirst[panel]=b;
    }
    
    public void setDefaultFixedColumns(String[] columnNames) {
        defaultFixedColumns.clear();
        for(int i=0;i<columnNames.length;i++){
            defaultFixedColumns.add(columnNames[i]);
        }
    }
    
    public void setFixedColumns(String[] columnNames){
        setDefaultFixedColumns(columnNames);
        for(int i=0;i<NUM;i++)
            setFixedColumns(i,columnNames); 
        
    }
    
    public void setFixedColumns(int panel,String[] columnNames){
        if(panel<0||panel>=NUM)
            return;
        
        Vector<String> fixedColumns=fixedColumnsMap.get(panel);
        fixedColumns.clear();
        for(int i=0;i<columnNames.length;i++){
            fixedColumns.add(columnNames[i]);
        }
    }
    
    public void setCommentColumnWidth(int panel,float w){
    	if(panel<0||panel>=NUM)
    		setCommentColumnWidth(w);
    	else
    		commentColumnWidths[panel]=w;
    	
    }
    
    public void setCommentColumnWidth(float w){
    	commentColumnWidth=w;
    	for(int i=0;i<NUM;i++)
    		commentColumnWidths[i]=w; 
    	
    }
    
    public void setCommentColumnWidths(float w){
    	commentColumnWidth=w;
    	for(int i=0;i<NUM;i++)
    		setCommentColumnWidth(i,w);
    }
    
    public void setLastLineEnergy(int panel,float ef){
    	if(panel<0 || panel>=NUM)
    		for(int i=0;i<NUM;i++)
    			lastLineEnergies[i]=-1;
    	else
    		lastLineEnergies[panel]=ef;
    }
    
    public void setLastLineLevelEnergy(int panel,float ef){
    	if(panel<0 || panel>=NUM)
    		for(int i=0;i<NUM;i++)
    			lastLineLevelEnergies[i]=-1;
    	else
    		lastLineLevelEnergies[panel]=ef;
    }
    
    public void setLastLine(int panel,String s){
    	if(panel<0 || panel>=NUM)
    		for(int i=0;i<NUM;i++)
    			lastLines[i]=s;
    	else
    		lastLines[panel]=s;
    }

    public void setSecondLastLine(int panel,String s){
    	if(panel<0 || panel>=NUM)
    		for(int i=0;i<NUM;i++)
    			secondLastLines[i]=s;
    	else
    		secondLastLines[panel]=s;
    }
    
    public void setFirstLineEnergy(int panel,float ef){
    	if(panel<0 || panel>=NUM)
    		for(int i=0;i<NUM;i++)
    			firstLineEnergies[i]=-1;
    	else
    		firstLineEnergies[panel]=ef;
    }
    
    public void setFirstLineLevelEnergy(int panel,float ef){
    	if(panel<0 || panel>=NUM)
    		for(int i=0;i<NUM;i++)
    			firstLineLevelEnergies[i]=-1;
    	else
    		firstLineLevelEnergies[panel]=ef;
    }
    
    
    public void setFirstLine(int panel,String s){
    	if(panel<0 || panel>=NUM)
    		for(int i=0;i<NUM;i++)
    			firstLines[i]=s;
    	else
    		firstLines[panel]=s;
    }
    

    public void setLeftoverOfLastCom(int panel, String leftoverOfLastCom) {
        if(panel<0 || panel>=NUM)
            for(int i=0;i<NUM;i++)
                leftoversOfLastCom[i]=leftoverOfLastCom;
        else
            leftoversOfLastCom[panel]=leftoverOfLastCom;
    }

    public void setShowDE(boolean s){showDE=s;}
    public void setShowAll(boolean s){showAll=s;}
    public void setNRecords(int n){nrecords=n;}
    public void setSuppressStatus(boolean sup ){showSuppressed=!sup;}
    public void setPortait(boolean p){isPortrait=p;}
    public void setDrawn(boolean drawn){isDrawn=drawn;}
    public void setIsEmpty(boolean empty){isEmpty=empty;}
    public void setBreaks(Vector<Break> brs){breaks=brs;}
    public void setBreak(int n,Break br){
    	if(breaks.size()>n) 
    		breaks.setElementAt(br, n);
    	else if(breaks.size()==n)
    		breaks.addElement(br);
    	else
    		System.out.println("Something wrong when setting a temporary break point!"+breaks.size()+" "+n);    	
    }
    
    public void setBreakPoint(int n,String pos){
    	if(breaks.size()>n) 
    		breaks.elementAt(n).pos=pos;
    	else if(breaks.size()==n){
    		Break b=new Break();
    		b.pos=pos;
    		breaks.addElement(b);
    	}		
    	else
    		System.out.println("Something wrong when setting a temporary break point pos!");    	
    }
    
    public void setDataAsComments(Vector<String> dac){
    	dataAsComments.clear();
    	dataAsComments.addAll(dac);	
    }
    public void setFcolumns(Vector<String> fc){Fcolumns=copy(fc);}
    public void setEnsdfFieldNames(Vector<String> names){ensdfFieldNames=copy(names);}	
    public void setTableType(String type){tableType=type;}
    
    public void setGammaBreakPoint(int n,float eg){breaks.elementAt(n).gammaPos=eg;}
    
    public void setHeadWidth(float width){headWidth=width;}
    public void setNewPage(boolean n){isNewPage=n;}
    
    public void setIsBodyInNewPage(boolean b){isBodyInNewPage=b;}
 
    public void setFirstPanelColumns(Vector<String> v){firstPanelColumns=copy(v);}
    
    public void setUpdated(boolean u){isUpdated=u;}
    
    public void setIsNewCommentWidth(boolean b){isNewCommentWidth=b;}
    public void setIsSeparateFoots(boolean s){isSeparateFoots=s;}
    public void setSortGammas(boolean b){isSortGammas=b;}
    public void setSortDelays(boolean b){isSortDelays=b;}    
    
    public void setUseAutoComWidth(boolean b){useAutoCommentWidth=b;}
    
  //small table that is not across pages (no break or lastbreak.isNewPage=false in case split panels are merged)
    public void setIsSmall(boolean b){isSmall=b;}
    
    public void setCurrentPanelNo(int n){currentPanelNo=n;}//used in TableControlPanel
    
    public boolean isUpdated(){return isUpdated;}
    public boolean isUseAutoComWidth(){return useAutoCommentWidth;}
    
    public boolean isBreakPage(int i){return breaks.elementAt(i).isNewPage;}
    public boolean isDrawn(){return isDrawn;}
    public boolean isEmpty(){return isEmpty;}
    public boolean showDE(){return showDE;}
    public boolean isSmall(){return isSmall;}
    public boolean isNewPage(){return isNewPage;}
    public boolean isSeparateFoots(){return isSeparateFoots;}
    public boolean isFormatTables(){return isFormatTables;}
    public boolean isPortrait(){return isPortrait;}
    public boolean showAll(){return showAll;}
    public boolean isShowSuppressed(){return showSuppressed;}
    public boolean isNewCommentWidth(){return isNewCommentWidth;}
    public boolean isSortGammas(){return isSortGammas;}
    public boolean isSortDelays(){return isSortDelays;}
    
    public boolean isBodyInNewPage(){return isBodyInNewPage;}
    
    public void setRecorderGamma(boolean b){reorderGamma=b;} 
    public boolean reorderGamma(){return reorderGamma;}
    
    public int getNRecords(){return nrecords;}

    public int getCurrentPanelNo(){return currentPanelNo;}

    private Vector<String> copy(Vector<String> source){
    	
    	if(source==null)
    		return null;
    	
    	Vector<String> target=new Vector<String>();
    	
    	target.addAll(source);
    	
    	return target;        
    }
    
    
    //NOTE: clone() in Cloneable only copy non-object variables.
    //For object varibales, it copies the links, which means they
    //are just different references to the same object and any change
    //to the object variables in the cloned object will be reflected 
    //in the object variables in the original object. It is all
    //because Java pass by value. 
    public TableControl clone() { 
    	try { 
    		// call clone in Object.
    		TableControl tc=(TableControl)super.clone();
   		
    		tc.breaks=new Vector<Break>();
    		for(int i=0;i<breaks.size();i++){
    			Break br=breaks.elementAt(i).clone();
    			tc.breaks.addElement(br);
    		}
    		
    		tc.comments=new Vector<Comment>();
    		for(int i=0;i<comments.size();i++){
    			Comment c=comments.elementAt(i).clone();
    			tc.comments.addElement(c);
    		}
    		
    		tc.defaultColumns=new Vector<String>();
    		tc.defaultColumns.addAll(defaultColumns);
    		
            tc.defaultFixedColumns=new Vector<String>();
            tc.defaultFixedColumns.addAll(defaultFixedColumns);
            
            tc.fixedColumnsMap=new HashMap<Integer,Vector<String>>();
            for(Integer key:fixedColumnsMap.keySet()){
                Vector<String> fixedColumns=new Vector<String>();
                fixedColumns.addAll(fixedColumnsMap.get(key));
                tc.fixedColumnsMap.put(key,fixedColumns);
            }
            
    		if(firstPanelColumns!=null){
        		tc.firstPanelColumns=new Vector<String>();
        		tc.firstPanelColumns.addAll(firstPanelColumns);    			
    		}

    		if(dataAsComments!=null){
        		tc.dataAsComments=new Vector<String>();
        		tc.dataAsComments.addAll(dataAsComments);    			
    		}

    		if(Fcolumns!=null){
        		tc.Fcolumns=new Vector<String>();
        		tc.Fcolumns.addAll(Fcolumns);    			
    		}

   		
    		return tc;    		
    	} 
    	catch (CloneNotSupportedException e) { 
    			System.out.println("Cloning not allowed."); 
    			return this; 
    			
    	}    	
    }  
    
    
    //check if a comment should be placed at top of the table as a general comment at top
    //top comments: "cG $....", "cG        .....", or "cG BE2W$......" and so on.
    //For a comment like, "cG RI,BE2W$....", it should be treated as both top comment and
    //footnote comment.
    //This function returns true when the comment is only top comment.
    //NOTE: Comment c and TableControl tc must be for the same type of table
    public boolean isTopCommentOnly(Comment c){

        if(c.flagVV().isEmpty()||c.flagVV().firstElement().toString().equals("[]"))
        	if(c.nHeads()==0||c.headAt(0).isEmpty() || (c.type().equals("N ")&&c.headAt(0).contains("N")))
        		return true;

    		
    	for(int k=0;k<c.headV().size();k++)//filter out comments with no corresponding column data
    		if(ensdfFieldNames.contains(c.headAt(k)))//contains ENSDF-record field name
    			return false;
    	
    	return true;
    }
    
   
    
    //check if a comment should be placed at top of the table as a general comment at top
    //top comments: "cG $....", "cG        .....", or "cG BE2W$......" and so on.
    //For a comment like, "cG RI,BE2W$....", it should be treated as both top comment and
    //footnote comment.
    //This function returns true when the comment can be treated as both top comment and
    //footnote comment
    //NOTE: Comment c and TableControl tc must be for the same type of table
    public boolean isAlsoTopComment(Comment c){
    	if(isTopCommentOnly(c))
    		return false;
    		
    	for(int k=0;k<c.headV().size();k++)
    		if(!ensdfFieldNames.contains(c.headAt(k)))//contains non-standard ENSDF-record field name, like "BE2W"
    			return true;
    	
    	return false;
    }
    
    
    //extract top comment for a mixture head like "cG BE2W,RI$....", return "cG $B(E2)(W.u.) ..."

    public Comment extractTopComment(Comment c) throws Exception{
    	Comment topComment=new Comment();
    	String body=c.body();
    	String temp="";
    	for(int i=0;i<c.headV().size();i++){
    		if(!ensdfFieldNames.contains(c.headAt(i))){
    			//add "^" to avoid the secondary translation, for cases like "ISPIN" translated as "T" here
    			//but it could be incorrectly translated further to "T1/2" if isBigC=true in the subsequent
    			//translation of the whole comment, without "^" being added.
    			if(c.isBigC())
    				temp+=", ^"+Translator.procGenCom(c.headAt(i),0,false);
    			else
    				temp+=", "+Translator.procGenCom(c.headAt(i),0,false);
    		}
    	}
    	
    	
    	
    	if(temp.length()<=0)
    		return topComment;
    	
    	if(temp.charAt(0)==',')
    		temp=temp.substring(1).trim();
    	 
    	if(temp.length()>0 && body.length()>0)
    		temp=temp+": "+body;

    	
    	topComment.setBody(temp, c.isBigC());
    	topComment.setBigC(c.isBigC());
    	topComment.setType(c.type());
    	    	
    	return topComment;
    	
    	  	    	
    }
    
    /*
     * for gamma table only called when isSortGamma is set or changed
     */
    public void updateDefaultColumns(){
    	int indexOfEG=defaultColumns.indexOf("E");
    	int indexOfDEG=defaultColumns.indexOf("DE");
    	int indexOfRI=defaultColumns.indexOf("RI");
    	int indexOfDRI=defaultColumns.indexOf("DRI");

    	int indexOfLEV=defaultColumns.indexOf("LEV");
    	int indexOfDLEV=defaultColumns.indexOf("DLEV");
    	int indexOfJI=defaultColumns.indexOf("JI");
    	
    	if(indexOfEG<0)
    		return;
    	
    	if(isSortGammas && indexOfEG>0){
    		defaultColumns.remove("E");
    		defaultColumns.remove("DE");
    		defaultColumns.remove("RI");
    		defaultColumns.remove("DRI");
    		
    		if(indexOfDRI>0) defaultColumns.insertElementAt("DRI",0);
    		if(indexOfRI>0)  defaultColumns.insertElementAt("RI",0);
    		if(indexOfDEG>0) defaultColumns.insertElementAt("DE",0);
    		if(indexOfEG>0)  defaultColumns.insertElementAt("E",0);

    	}else if(!isSortGammas && indexOfLEV>0){
    		defaultColumns.remove("LEV");
    		defaultColumns.remove("DLEV");
    		defaultColumns.remove("JI");
    		
    		if(indexOfJI>0)  defaultColumns.insertElementAt("JI",0);
    		if(indexOfDLEV>0)defaultColumns.insertElementAt("DLEV",0);
    		if(indexOfLEV>0) defaultColumns.insertElementAt("LEV",0);
    	}
    	
    	return;
    }


    
    
 }




class BreakComparator implements Comparator<Object>{
    public int compare(Object one,Object two){
        Break a=(Break)one;
        Break b=(Break)two;
        if(Float.parseFloat(a.pos)<Float.parseFloat(b.pos)) return -1;
        if(Float.parseFloat(a.pos)>Float.parseFloat(b.pos)) return 1;
        else return 0;
    }
}
