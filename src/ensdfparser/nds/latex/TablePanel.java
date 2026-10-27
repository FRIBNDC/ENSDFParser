/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ensdfparser.nds.latex;
import java.util.*;

import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.util.Str;
/**
 *
 * @author Jeremie
 */
public class TablePanel {
    private Vector<String> genComs=new Vector<String>();//contains translated (Latex) general (header) and footnote comments for the table
    private Vector<String> Head=new Vector<String>();
    private Vector<String> FirstHead=new Vector<String>();
    private Vector<String> Tail=new Vector<String>();
    private Vector<String> LastTail=new Vector<String>();
    private Vector<String> Body=new Vector<String>();
    private Vector<String> columns=new Vector<String>();
    private Vector<Integer> Split=new Vector<Integer>();
    private Vector<String> TableRecords=new Vector<String>();//table records in an ENSDF file
    private Vector<Integer> ComLoc=new Vector<Integer>();
    private int ncolumns=0;
    private String spare="";
    private Vector<Integer> multicols=new Vector<Integer>();
    private float dataWidth;
    private float dataHeadWidth;//not including comment head
    
    private float commentColumnWidth;
    private float totalWidth;
    private float relHeadLinePos=0;//relative linePo in the table, pointing to the bottom of the headLine
    private int relHeadLineNo=0;//relative lineNo in the table, total number of headLines
    private float headLineWidth=0;//including comment head
    private int totalDataLines=0;
    private Vector<String> levelEnergies=new Vector<String>();//store level energy of each data line in the panel (a single panel for the whole table)
    private Vector<String> dataEnergies=new Vector<String>();//store data energy of each data line in the panel (a single panel for the whole table)
    private boolean isPortrait=true;
    private boolean isNewPage=false;
    private boolean isBreakAtLastCom=false;
    private boolean isBodyInNewPage=false;//used only for cases with comments/table records that can be kept in current page, but table body must be moved to new page
                                    //since there is no enough room for at least two table lines after table heading
                                    //for first panel only
    
    private int nTableRecordsLines=0;
    private int nTableBodyLines=0;
    
    private float tableRecordsHeight=0;
    private float tableBodyHeight=0;
    
    public TablePanel(){}
    /**Merges the current TablePanel with the one passed to this method*/
    public void Merge(TablePanel T, boolean sameType){
        int i=0;
        String a="";
        String line="";
        String h="";
        String s1="",s2="";
        int n=0,k=0;
        
        
        //System.out.println("TablePanel 64: nColumns="+ncolumns);
        //for(String s:columns)
        //	System.out.println("   "+s);
        
        for(int j=0;j<T.multicols.size();j++){
            multicols.addElement(T.multicols.elementAt(j)+ncolumns);
        }
        //Merge the first heads
        Vector<String> NewFirstHead=new Vector<String>();
        if(!sameType){
            for(int j=genComs.size()-1;j>=0;j--){
                FirstHead.insertElementAt("\\multicolumn{"+ncolumns+"}{l}{"+genComs.elementAt(j)+"}", 0);
            }
            Vector<String> temp=new Vector<String>();
            for(int j=T.getGenComments().size()-1;j>=0;j--){
                temp.insertElementAt("\\multicolumn{"+T.getNColumns()+"}{l}{"+T.getGenComments().elementAt(j)+"}",0);
            }
            genComs.clear();
            T.setGenComments(genComs);
            
            temp.addAll(T.getFirstHead());
            T.setFirstHead(temp);
            
            
            n=FirstHead.size()-T.getFirstHead().size();
            if(n>0){
                for(i=0;i<n;i++){
                	h=FirstHead.elementAt(i).toString();
                    if(h.contains("multicolumn")&&h.contains("}{c}{"))FirstHead.setElementAt(h.replace("}{c}{", "}{c|}{"), i);
                    if(h.contains("multicolumn")&&h.contains("}{l}{"))FirstHead.setElementAt(h.replace("}{l}{", "}{l|}{"), i);
                    for(int r=0;r<T.getNColumns();r++)a+="&";
                    NewFirstHead.addElement(FirstHead.elementAt(i).toString()+a);
                    a="";
                }
                for(i=n;i<FirstHead.size();i++){
                	h=FirstHead.elementAt(i).toString();
                    if(h.contains("multicolumn")&&h.contains("}{c}{"))FirstHead.setElementAt(h.replace("}{c}{", "}{c|}{"), i);
                    if(h.contains("multicolumn")&&h.contains("}{l}{"))FirstHead.setElementAt(h.replace("}{l}{", "}{l|}{"), i);
                    
                    h=FirstHead.elementAt(i).toString()+"&"+T.getFirstHead().elementAt(i-n).toString();
                    NewFirstHead.addElement(h);
                }
            }else if(n<0){
            	n=-n;
                for(i=0;i<n;i++){
                    for(int r=0;r<ncolumns;r++)a+="&";
                    NewFirstHead.addElement(a+T.getFirstHead().elementAt(i).toString());
                    a="";
                }
                for(i=n;i<T.getFirstHead().size();i++){
                	h=FirstHead.elementAt(i-n).toString();
                    if(h.contains("multicolumn")&&h.contains("}{c}{"))
                        FirstHead.setElementAt(h.replace("}{c}{", "}{c|}{"), i-n);
                    if(h.contains("multicolumn")&&h.contains("}{l}{"))
                        FirstHead.setElementAt(h.replace("}{l}{", "}{l|}{"), i-n);
                    NewFirstHead.addElement(FirstHead.elementAt(i-n).toString()+"&"+T.getFirstHead().elementAt(i).toString());
                }
            }else{
                genComs.addAll(T.getGenComments());
                for(i=0;i<T.getFirstHead().size();i++){
                	h=FirstHead.elementAt(i).toString();
                    if(h.contains("multicolumn")&&h.contains("}{c}{"))FirstHead.setElementAt(h.replace("}{c}{", "}{c|}{"), i);
                    if(h.contains("multicolumn")&&h.contains("}{l}{"))FirstHead.setElementAt(h.replace("}{l}{", "}{l|}{"), i);
                    NewFirstHead.addElement(FirstHead.elementAt(i).toString()+"&"+T.getFirstHead().elementAt(i).toString());
                }
            }
        }else{
            for(i=0; i<FirstHead.size();i++){
            	h=FirstHead.elementAt(i).toString();
                if(h.contains("multicolumn{"+Integer.toString(ncolumns)+"}")&&h.contains("continued")){//for head name spanning all columns
                	a=h;
                    a=a.replace("{"+Integer.toString(ncolumns)+"}", "{"+Integer.toString(ncolumns+T.getNColumns())+"}");
                    NewFirstHead.addElement(a);
                    a="";
                }else {       
                	s1="";s2="";
                	k=h.lastIndexOf("&");
                	
                	int k1=h.lastIndexOf("\\&");
                	if(k1>=0 && k==k1+1)
                		k=h.substring(0,k1).lastIndexOf("&");
                	
                	if(k>=0){
                		s1=h.substring(0,k+1);
                		s2=h.substring(k+1);                		
                	}
                	
                	if(s2.contains("multicolumn{")&&s2.contains("}{c}{")) FirstHead.setElementAt(s1+s2.replace("}{c}{", "}{c|}{"), i);
                	if(s2.contains("multicolumn{")&&s2.contains("}{l}{")) FirstHead.setElementAt(s1+s2.replace("}{l}{", "}{l|}{"), i);
                	if(s2.contains("multicolumn{")&&s2.contains("}{r}{")) FirstHead.setElementAt(s1+s2.replace("}{r}{", "}{r|}{"), i);
                	
                	NewFirstHead.addElement(FirstHead.elementAt(i).toString()+"&"+T.getFirstHead().elementAt(i).toString());               	
                }
            }
        }
        setFirstHead(NewFirstHead);

        //merge tables
        if(!sameType) TableRecords.addAll(T.getTableRecords());
        
        i=0;
        //Merge the heads
        Vector<String> NewHead=new Vector<String>();
        a="";
        if(!sameType){
        	n=Head.size()-T.getHead().size();
            if(n>0){
                for(i=0;i<n;i++){
                	h=Head.elementAt(i).toString();
                	if(h.contains("multicolumn")){
                        if(h.contains("}{c}{"))Head.setElementAt(h.replace("}{c}{", "}{c|}{"), i);
                        else if(h.contains("}{l}{"))Head.setElementAt(h.replace("}{l}{", "}{l|}{"), i);
                        else if(h.contains("{@{}c}"))Head.setElementAt(h.replace("{@{}c}", "{@{}c|}"), i);
                	}

                    for(int r=0;r<T.getNColumns();r++)a+="&";
                    NewHead.addElement(Head.elementAt(i).toString()+a);
                    a="";
                }
                for(i=n;i<Head.size();i++){
                	h=Head.elementAt(i).toString();
                	if(h.contains("multicolumn")){
                        if(h.contains("}{c}{"))Head.setElementAt(h.replace("}{c}{", "}{c|}{"), i);
                        if(h.contains("}{l}{"))Head.setElementAt(h.replace("}{l}{", "}{l|}{"), i);
                        if(h.contains("{@{}c}"))Head.setElementAt(h.replace("{@{}c}", "{@{}c|}"), i);
                	}
                	
                    NewHead.addElement(Head.elementAt(i).toString()+"&"+T.getHead().elementAt(i-(Head.size()-T.getHead().size())).toString());
                }
            }else if(n<0){
            	n=-n;
                for(i=0;i<n;i++){
                    for(int r=0;r<ncolumns;r++)a+="&";
                    NewHead.addElement(a+T.getHead().elementAt(i).toString());
                    a="";
                }
                for(i=n;i<T.getHead().size();i++){
                	h=Head.elementAt(i-n).toString();
                	if(h.contains("multicolumn")){
                        if(h.contains("}{c}{"))Head.setElementAt(h.replace("}{c}{", "}{c|}{"), i-n);
                        else if(h.contains("}{l}{"))Head.setElementAt(h.replace("}{l}{", "}{l|}{"), i-n);
                        else if(h.contains("{@{}c}"))Head.setElementAt(h.replace("{@{}c}", "{@{}c|}"), i-n);
                	}
                	
                    NewHead.addElement(Head.elementAt(i-n).toString()+"&"+T.getHead().elementAt(i).toString());
                }
            }else{
                for(i=0;i<T.getHead().size();i++){
                	h=Head.elementAt(i).toString();
                	if(h.contains("multicolumn")){
                        if(h.contains("}{c}{"))Head.setElementAt(h.replace("}{c}{", "}{c|}{"), i);
                        else if(h.contains("}{l}{"))Head.setElementAt(h.replace("}{l}{", "}{l|}{"), i);
                        else if(h.contains("{@{}c}"))Head.setElementAt(h.replace("{@{}c}", "{@{}c|}"), i);
                	}

                    NewHead.addElement(Head.elementAt(i).toString()+"&"+T.getHead().elementAt(i).toString());
                }
            }
        }else{
            for(i=0; i<Head.size();i++){
            	h=Head.elementAt(i).toString();
                if(h.contains("multicolumn{"+Integer.toString(ncolumns)+"}")&&h.contains("continued")){//for head name spanning all columns
                    a=h;
                    a=a.replace("{"+Integer.toString(ncolumns)+"}", "{"+Integer.toString(ncolumns+T.getNColumns())+"}");
                    NewHead.addElement(a);
                    a="";
                }else {
                	k=h.lastIndexOf("&");
                	s1="";s2="";
                	
                	int k1=h.lastIndexOf("\\&");
                	if(k1>=0 && k==k1+1)
                		k=h.substring(0,k1).lastIndexOf("&");
                	
                	if(k>=0){s1=h.substring(0,k+1);s2=h.substring(k+1);}
                	if(s2.contains("multicolumn{")&&!s2.contains("~")){
                		if(s2.contains("}{c}{")) Head.setElementAt(s1+s2.replace("}{c}{", "}{c|}{"), i);
                		else if(s2.contains("{@{}c}"))Head.setElementAt(s1+s2.replace("{@{}c}", "{@{}c|}"), i);
                		else if(s2.contains("}{l}{")) Head.setElementAt(s1+s2.replace("}{l}{", "}{l|}{"), i);
                		else if(s2.contains("}{r}{")) Head.setElementAt(s1+s2.replace("}{r}{", "}{r|}{"), i);
                	}
                	
                	NewHead.addElement(Head.elementAt(i).toString()+"&"+T.getHead().elementAt(i).toString());
                }
            }
        }
        setHead(NewHead);

        i=0;
        //Merge the bodies
        Vector<String> newBody=new Vector<String>();
        Vector<String> mergedBody=T.getBody();

        for(i=0;i<Body.size();i++){
        	s1="";
        	s2="";
            line=Body.elementAt(i);
            k=line.lastIndexOf("&");
        	
        	int k1=line.lastIndexOf("\\&");
        	if(k1>=0 && k==k1+1)
        		k=line.substring(0,k1).lastIndexOf("&");
        	
            if(k>=0){s1=line.substring(0,k+1);s2=line.substring(k+1);}
            
            if(s2.contains("multicolumn{")){               
                if(s2.contains("}{c}{")){
                    Body.insertElementAt(s1+s2.replace("}{c}{","}{c|}{"),i);
                    Body.removeElementAt(i+1);
                }else if(s2.contains("}{@{}c}")){
                    Body.insertElementAt(s1+s2.replace("{@{}c}","{@{}c|}"),i);
                    Body.removeElementAt(i+1);
                }else if(s2.contains("}{l}{")){
                    Body.insertElementAt(s1+s2.replace("}{l}{","}{l|}{"),i);
                    Body.removeElementAt(i+1);
                }else if(s2.contains("}{r}{")){
                    Body.insertElementAt(s1+s2.replace("}{r}{","}{r|}{"),i);
                    Body.removeElementAt(i+1);
                }else if(s2.contains("}{@{.}l}")){
                    Body.insertElementAt(s1+s2.replace("{@{.}l}","{@{.}l|}"),i);
                    Body.removeElementAt(i+1);
                }else if(s2.contains("}{@{ }l}")){
                    Body.insertElementAt(s1+s2.replace("{@{ }l}","{@{ }l|}"),i);
                    Body.removeElementAt(i+1);
                }else if(s2.contains("}{@{}l}")){
                    Body.insertElementAt(s1+s2.replace("{@{}l}","{@{}l|}"),i);
                    Body.removeElementAt(i+1);
                }	
            }
            
            if(i>mergedBody.size()-1){
            	for(int r=0;r<T.getNColumns();r++) a+="&";
            	newBody.addElement(Body.elementAt(i).toString()+a);
                a="";
            }
            else
            	newBody.addElement(Body.elementAt(i).toString()+"&"+mergedBody.elementAt(i).toString());
        }

        if(Body.size()<mergedBody.size()){
            for(;i<mergedBody.size();i++){
                for(int r=0;r<ncolumns;r++) a+="&";
                newBody.addElement(a+T.getBody().elementAt(i).toString());
                a="";
            }
        }
        
        setBody(newBody);

        i=0;
        //Merge the tails
        Vector<String> NewTail=new Vector<String>();
        /*if(!sameType){
            for(;i<Tail.size()&&i<T.getTail().size();i++){
                NewTail.addElement(Tail.elementAt(i).toString()+"&"+T.getTail().elementAt(i).toString());
            }
            if(Tail.size()>T.getTail().size()){
                for(;i<Tail.size();i++){
                    for(int r=0;r<T.getNColumns();r++)a+="&";
                    NewTail.addElement(Tail.elementAt(i).toString()+a);
                    a="";
                }
            }
            if(Tail.size()<T.getTail().size()){
                for(;i<T.getTail().size();i++){
                    for(int r=0;r<ncolumns;r++)a+="&";
                    NewTail.addElement(a+T.getTail().elementAt(i).toString());
                    a="";
                }
            }
        }else*/{
            for(; i<Tail.size();i++){
                if(Tail.elementAt(i).toString().contains("&")){
                    for(int r=0;r<ncolumns+T.getNColumns()-1;r++)a+="&";
                    NewTail.addElement(a);
                    a="";
                }else if(Tail.elementAt(i).toString().contains(Integer.toString(ncolumns))){
                    a=Tail.elementAt(i).toString();
                    a=a.replace(Integer.toString(ncolumns), Integer.toString(ncolumns+T.getNColumns()));
                    NewTail.addElement(a);
                    a="";
                }
            }
        }
        setTail(NewTail);

        //Merge the lasttails
        Vector<String> NewLastTail=new Vector<String>();
        if(!sameType){
            String temp;
            int longest=LastTail.size(); 
            if(T.getLastTail().size()>longest) longest=T.getLastTail().size();
            for(int j=0;j<longest;j++){
                temp="";
                if(j<LastTail.size()) temp+="\\multicolumn{"+ncolumns+"}{l}{\\parbox[c]{"+ncolumns*.75+"in}{"+LastTail.elementAt(j)+"}}";
                temp+="&";
                if(j<T.getLastTail().size()) temp+="\\multicolumn{"+T.getNColumns()+"}{l}{\\parbox[c]{"+T.getNColumns()*.75+"in}{"+T.getLastTail().elementAt(j)+"}}";
                NewLastTail.add(temp);
            }
            LastTail=NewLastTail;
        }
        Split.addElement(ncolumns);

        for(int j=0;j<T.ComLoc.size();j++){
            ComLoc.addElement(T.ComLoc.elementAt(j)+ncolumns);
        }
        
        columns.addAll(T.getColumns());
        ncolumns+=T.getNColumns();
        
        //System.out.println("TablePanel 369: nColumns="+ncolumns);
        //for(String s:columns)
        //	System.out.println("   "+s);
        
        //update table body height
		Vector<String> linesV=getBody();
		
		//debug
		//System.out.println("In TableMaker: Merge: tablebody height before="+height);
		
		float height=0;
        for(int m=0;m<linesV.size();m++){
        	float scale=getLineWidthScale(linesV.get(m));

        	//assuming no comment columns in table panels in merging
        	height+=scale*NDSConfig.TABLE_BODY_LINEWIDTH0;
    		
			//debug
			//System.out.println("In TableMaker: Merge: "+b2.get(m));
        }
        
        setTableBodyHeight(height);
    }
    
	private float getLineWidthScale(String line) {
		float scale=1.0f;
    	
        if(Str.containSpecialSupScript(line)){
        	scale=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*1.10f;  
        }else if(Str.containSupScript(line, "0,1,2,3,4,5,6,7,8,9")){
            scale=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*0.2f;  
        }else if(Str.containSupScript(line)){
            if(Str.containJPIScriptOnly(line)){
                scale=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
            }else if(Str.containGivenSupScriptOnly(line,"x")){
                scale=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
            }else{
                scale=NDSConfig.TABLE_LINEWIDTH_SCALE0;
            }

        }else if(Str.containSubScript(line)) {
            scale=NDSConfig.TABLE_LINEWIDTH_SCALE4;
        }
        
		return scale;
	}
	
    @SuppressWarnings("unchecked")
	public void sepFoots(){
        Vector<String> newFirstHead=(Vector<String>)FirstHead.clone();
        Vector<String> newHead=(Vector<String>)FirstHead.clone();
        Vector<String> newBody=(Vector<String>)Body.clone();
        Tail.clear();
        newFirstHead.setElementAt(FirstHead.lastElement()+"& ",newFirstHead.size()-1);
        newHead.setElementAt(Head.lastElement()+"& ",newHead.size()-1);
        
        String foots=""; //the string of all the footnotes in the table
        String s;
        for(int i=0;i< LastTail.size();i++){
            s=LastTail.elementAt(i);
            if(s.trim().length()==0)//LaTeX does not allow end-line symbol "\\" to end an empty line
            	s="\\ ";
            
            foots+=s+"\\\\\n";
        }
        newBody.setElementAt(Body.elementAt(0)+"& \\multirow{"+Body.size()+"}{*}{\\parbox[c]{10cm}{"+foots+"}}",0);
        LastTail.clear();
        Body=newBody;
        Head=newHead;
        FirstHead=newFirstHead;
        Split.addElement(ncolumns);
        ncolumns++;
    }
   
    
    public int getNTableBodyLines(){return nTableBodyLines;}
    public int getNTableRecordsLines(){return nTableRecordsLines;}
    public float getTableRecordsHeight(){return tableRecordsHeight;}
    public float getTableBodyHeight(){return tableBodyHeight;}
    public float getHeadWidth(){return headLineWidth;}
    public float getTotalWidth(){return totalWidth;}//Jun add this 4/8/2015
    public float getDataWidth(){return dataWidth;}//Jun add this 4/8/2015    
    public float getCommentColumnWidth(){return commentColumnWidth;}    
    public float getDataHeadWidth(){return dataHeadWidth;}
    
    public float getRelHeadLinePos(){return relHeadLinePos;}
    public int getRelHeadLineNo(){return relHeadLineNo;}
    public int getTotalDataLines(){return totalDataLines;}
    public Vector<String> getDataLineLevels(){return levelEnergies;}
    public Vector<String> getDataLineEnergies(){return dataEnergies;}
    public Vector<String> getGenComments(){return genComs;}
    public Vector<String> getHead(){return Head;}
    public Vector<String> getFirstHead(){return FirstHead;}
    public Vector<String> getTableRecords(){return TableRecords;}
    public Vector<String> getTail(){return Tail;}
    public Vector<String> getLastTail(){return LastTail;}
    public Vector<String> getBody(){return Body;}
    public Vector<Integer> getMulticols(){return multicols;}
    public int getNColumns(){return ncolumns;}
    public Vector<String> getColumns(){return columns;}
   
    public Vector<Integer> getSplit(){return Split;}
    public Vector<Integer> getComLoc(){return ComLoc;}
    public String getSpare(){return spare;}
    
    public void setNTableBodyLines(int n){nTableBodyLines=n;}
    public void setNTableRecordsLines(int n){nTableRecordsLines=n;}
    public void setTableRecordsHeight(float h){tableRecordsHeight=h;}
    public void setTableBodyHeight(float h){tableBodyHeight=h;}
    public void setHeadWidth(float width){headLineWidth=width;}
    public void setCommentColumnWidth(float width){commentColumnWidth=width;}
    public void setTotalWidth(float w){totalWidth=w;}
    public void setDataWidth(float w){dataWidth=w;}   
    public void setDataHeadWidth(float w) {dataHeadWidth=w;}
    
    public void setRelHeadLinePos(float pos){relHeadLinePos=pos;}
    public void setRelHeadLineNo(int n){relHeadLineNo=n;}
    public void setTotalDataLines(int n){totalDataLines=n;}
    protected void setDataLineLevels(Vector<String> v){levelEnergies=v;}
    protected void setDataLineEnergies(Vector<String> v){dataEnergies=v;}
    public void setGenComments(Vector<String> v){genComs=v;}
    public void setHead(Vector<String> v){Head=v;}
    public void setFirstHead(Vector<String> v){FirstHead=v;}
    public void setTableRecords(Vector<String> v){TableRecords=v;}
    public void setTail(Vector<String> v){Tail=v;}
    public void setLastTail(Vector<String> v){LastTail=v;}
    public void setBody(Vector<String> v){Body=v;}
    public void setNColumns(int n){ncolumns=n;}
    public void setColumns(Vector<String> c){columns=c;}
    
    public void setPortrait(boolean p){isPortrait=p;}
    public void setNewPage(boolean n){isNewPage=n;}
    public void setIsBreakAtLastCom(boolean b){isBreakAtLastCom=b;}
    public void setIsBodyInNewPage(boolean b){isBodyInNewPage=b;}
    
    public boolean isNewPage(){return isNewPage;}
    public boolean isPortrait(){return isPortrait;}
    public void addComLoc(int i){ComLoc.addElement(i);}
    public void setSpare(String s){spare=s;}
    public void addMulticol(int i){multicols.addElement(i);}   
    public boolean lastIsCom(){if(ComLoc.contains(ncolumns-1))return true;else return false;}
    public boolean isBreakAtLastCom(){return isBreakAtLastCom;}
    public boolean isBodyInNewPage(){return isBodyInNewPage;}

    
}
