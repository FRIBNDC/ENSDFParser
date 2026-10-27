package ensdfparser.nds.control;

import java.io.IOException;
import java.util.Vector;

import ensdfparser.base.BaseControl;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.ensdf.EnsdfTableData;

public class NDSControl extends BaseControl{
	
    public static Vector<String> controls; ///contains all the lines of the control file
    
    public static float currentLineNo=0;//counting starts from 0, since a <1 weight is used for non-table-data entry, this variable is set to be float type
    public static float currentLinePos=0;//mark the top position of a text, top-straight-line: pos=0, bottom-straight-line pos=MAX_TEXT_HEIGHT
                                         //the first line has lineNo=0, linePos=0, before counting, after counting they point to the next line to be counter;
                                         //for the line to be counted
    
    public static int numberOfAllTitleLines=0;
    public static float heightOfAllTitleLines=0; 
    
    public static int numberOfAuthorsToShow=4;

    public static boolean includeAllDrawings=true;
    public static boolean nodrawing=false;
    public static boolean nodrawingExceptAdoptedBands=false;
    
    public static boolean includeRefTitle=false;
    
    public static boolean printDocumentRecord=false;
    
    public static boolean isModified=false;
    public static boolean isTableControlModified=false;//true if control settings have been modified and applied in TableControlPanel
    public static boolean isHeaderSettingModified=false;
    public static boolean isBandSettingModified=false;
    
    public static boolean useControlFile=false;//set from the check box in main panel
    
    public static boolean autoAdjust=true;//if no control if given
    
    public static boolean needToFindBreaks=true;
    public static boolean isCommentWidthReset=false;
    public static boolean isBreakPointReset=false;
    
    public static int numberOfPages=0;
    
    public static boolean writeHeaderOnly=false;
    public static boolean drawBandOnly=false;
    public static boolean drawFigureOnly=false;
    
    public static boolean isLoadedKeynumberFile=false;
    
    public static int breakLineNo=NDSConfig.MAX_LINE_COUNT-4;//counting starts from 0
    public static float breakLinePos=NDSConfig.MAX_TEXT_HEIGHT-4*NDSConfig.DEFAULT_LINEWIDTH;
    
    public static int nsplit=1;
    public static boolean isPortrait=true;
    public static boolean isNewPage=false;
    
    public static boolean isNewFigurePage=false;
    
    public static boolean isFormatTables=false;//for table records

    public static boolean showSuppressed=false;//for S continuation records
    public static boolean showInterBand=false;
    public static boolean showAllAuthors=false;
    public static boolean deleteFigureFiles=true;
    
    public static boolean showAtomicData=true;//for atomic radiationd ata
    
    public static boolean isBandCreated=false;
    public static boolean printBandSeq=false;
    
    public static boolean isForWebDisplay=false;
    public static boolean showHeading=true;//page heading, default="Nuclear Data Sheets"
    public static boolean showAllHistories=false;
    public static String heading="";//if empty, default will be used
    
    public static boolean removeAllHeading=false;
    public static boolean removeAllFooting=false;
    
    public static boolean hasSetNewPageForTopTitle=false;//used in setTopTitleSpacingInNewPage()
    
    public static boolean printAugerFirst=false;
    
    public static String userNSRFilename="";
    public static String NSRDataBaseLoginFileName="";
    
    /* Data related to the abstract for the data set*/
    public static boolean hasAbstract;
    public static String abstractVolume;
    public static String year;
    public static String receivedDate;
    public static String revisedDate;
    public static String page;
    public static String Z;
    
    //default settings
    //will be loaded from Setup if existing there
    public static String NDSPolicyURL="https://www.nndc.bnl.gov/nds/docs/NDSPolicies.pdf";
    public static String NDSIssueMonthForPolicy="April";
    
    /*other data for header */
    public static boolean hasIndex;
    public static boolean blankPage; //put a blank page after the index
    public static boolean hasReference;
    public static String referenceA;
    public static String type;
    public static boolean hasSkeleton;
    public static String skelOptions;
    public static boolean isUpdate;
    public static String updCom1;//editor's comment on cover page, see LatexWriter
    public static String updCom2;//editor's comment in individual data set (level table only), see LevelTableMaker
    
    public static boolean reorderGamma=false;//global option for reordering gamma table. 
    public static boolean reorderByGamma=false;//global option for reordering gamma table by gamma. It can be overwritten by reordering option in the control settings for each dataset
    public static boolean reorderByLevel=false;

	public static boolean isForcedNSRWebQuery=true;// isForcedWebQuery=true, always connect to NNDC NSR page for web query and parse the searched results, even if
                                                //the direct connection to database can't be established. Otherwise, stop searching for 
                                                //NSR information if the direct connection failed.      
                                                //This option works only when loginFileName is not empty 
    
	public static boolean isSilentJDBC=false; //isSilentJDBC=true, the content of JDBC login file and exceptions if connection fails will be printed out.
	                                          //This option works only when loginFileName is not empty
	
	public static boolean useLetterFootnoteMarkFirst=false;//use letters first for footnote marks

	public static boolean autoCorrectionForKeynumber=true;//true for correcting keynumers with incorrect letter-case, but still valid keynumber format (except for case)
	
    //\& and @ occupy more space than other symbols, but one can use makebox[width]{text} to make a box with fixed width 
    public final static String[] footmarkSymbs={"\\dagger","\\ddagger","\\#","@","\\&","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
    		"1","2","3","4","5","6","7","8","9","!","~","|","/","<",">",
    		"\\alpha","\\beta","\\eta","\\lambda","\\varphi","\\chi","\\psi","\\omega","\\gamma","\\delta","\\epsilon","\\theta","\\rho","\\mu","\\nv","\\S","\\zeta",
    		"\\Delta","\\Theta","\\kappa","\\sigma","\\upsilon"};
    //private String[] symbs={"\\dagger","\\ddagger","\\S","\\#","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

    public final static String[] footmarkSymbsLetterFirst={"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
    		"1","2","3","4","5","6","7","8","9","!","~","|","/","<",">","\\dagger","\\ddagger","\\#","@","\\&",
    		"\\alpha","\\beta","\\eta","\\lambda","\\varphi","\\chi","\\psi","\\omega","\\gamma","\\delta","\\epsilon","\\theta","\\rho","\\mu","\\nv","\\S","\\zeta",
    		"\\Delta","\\Theta","\\kappa","\\sigma","\\upsilon"};
	
    /// constructor
    public NDSControl(){
        hasIndex=true;
        blankPage=true;
        type="REVIEW";
        referenceA="";
        hasReference=false;
        hasAbstract=true;
        abstractVolume="xx";
        year="****";
        receivedDate="****";
        page="****";
        revisedDate="";
        Z="";
        skelOptions="SKELETON";
        hasSkeleton=true;
        isUpdate=false;
        updCom1="";
        updCom2="";
        nsplit=1;
        currentLineNo=0;
        isPortrait=true;
        isNewPage=false;
        isNewFigurePage=false;
        
        isFormatTables=false;

        autoAdjust=true;
        needToFindBreaks=true;
        isCommentWidthReset=false;
        isBreakPointReset=false;
        
        numberOfPages=0;
        		
        writeHeaderOnly=false;
        drawBandOnly=false;
        drawFigureOnly=false;
        
        isLoadedKeynumberFile=false;
        		
        isModified=false;
        isTableControlModified=false;
        isHeaderSettingModified=false;
        isBandSettingModified=false;
        useControlFile=false;
        includeAllDrawings=true;
        includeRefTitle=false;
        printDocumentRecord=false;
        nodrawing=false;
        nodrawingExceptAdoptedBands=false;
        showSuppressed=false;
        showInterBand=false;
        showAllAuthors=false;
        
        deleteFigureFiles=true;
        
        isForWebDisplay=false;
        showHeading=true;
        heading="NUCLEAR DATA SHEETS";
        
        removeAllHeading=false;
        removeAllFooting=false;
        
        breakLineNo=NDSConfig.MAX_LINE_COUNT-4;
        breakLinePos=NDSConfig.MAX_TEXT_HEIGHT-4*NDSConfig.DEFAULT_LINEWIDTH;
        
        reorderGamma=false;
        reorderByGamma=false;
        reorderByLevel=false;
        
        numberOfAuthorsToShow=4;
        
        userNSRFilename="";
        NSRDataBaseLoginFileName="";
        
        isForcedNSRWebQuery=true;
        isSilentJDBC=false;
        
        useLetterFootnoteMarkFirst=false;
    }

    public static void setControl(Vector<String> v){
        String temp;
        controls=new Vector<String>();
        for(int i=0;i<v.size();i++){
            temp=(String)v.elementAt(i);
            if(temp.length()<=3) continue;
            if(temp.charAt(0)!='!'&&temp.charAt(0)!='\\'&&!temp.substring(0,3).equals("REM") ) controls.add((temp));
        }
    }
    

    /** Interprets the first lines of a control file */
    public static void readControlHead(){
        String temp,sub;
        try{
            type=(String)controls.elementAt(0);
        }catch(Exception e){
            System.out.println("Control file empty");
            e.printStackTrace();
            return;
        }
        
        int n=1;
        
        blankPage=false;        
        hasReference=false;
        referenceA="";
        while(n<1000){
            temp=(String)controls.elementAt(n);
            if((temp.charAt(0)>='0'&&temp.charAt(0)<='9')||(temp.charAt(1)>='0'&&temp.charAt(1)<='9')||(temp.charAt(2)>='0'&&temp.charAt(2)<='9')) break;
            sub=temp.substring(0,3);
            if(sub.equals("ABS")){
                hasAbstract=true;
                abstractVolume=getLineAsString(temp,"/V:");
                year=getLineAsString(temp,"/Y:");
                receivedDate=getLineAsString(temp,"/R:");
                revisedDate=getLineAsString(temp,"/C:");
                Z=getLineAsString(temp,"/Z:");                
            }
            if(sub.equals("IND")){
                hasIndex=true;
                if(temp.contains("/B:")) blankPage=true;
            }
            if(sub.equals("REF")){
                hasReference=true;
                referenceA=getLineAsString(temp,"/A:");
            }
            if(sub.equals("SKE")){
                hasSkeleton=true;
                skelOptions=temp;
            }
            if(sub.equals("UPD")){
                isUpdate=true;
                updCom1=getLineAsString(temp,"/C:");
                updCom2=getLineAsString(temp,"/D:");
            }
            n++;
        }
        
    }
    /**writes the first lines of a control file */
    public static void writeControlHead(java.io.Writer out)throws IOException{
        out.write(type+"\n");
        if(hasAbstract){
            out.write("ABSTRACT/V:"+abstractVolume+"/Y:"+year+"/R:"+receivedDate);
            if(revisedDate.length()>0) out.write("/C:"+revisedDate);
            if(Z.length()>0) out.write("/Z:"+Z);
            out.write("\n");
        }
        
        if(hasReference){ out.write("REFERENCE");
            if(referenceA.length()>0) out.write("/A:"+referenceA);
            out.write("\n");
        }
        
        if(hasSkeleton) out.write(skelOptions+"\n");
        
        if(hasIndex){ out.write("INDEX");
            if(blankPage) out.write("/B:");
            out.write("\n");
        }
        if(isUpdate)out.write("UPDATE/C:"+updCom1+"/D:"+updCom2+"\n");
    }
    
    public static void reset(){
            hasIndex=true;
            blankPage=true;
            type="REVIEW";
            referenceA="";
            hasReference=false;
            hasAbstract=true;
            abstractVolume="xx";
            year="****";
            receivedDate="****";
            revisedDate="";
            page="****";
            Z="";
            skelOptions="SKELETON";
            hasSkeleton=true;
            isUpdate=false;
            updCom1="";
            updCom2="";
            controls=null;
            nsplit=1;
            currentLineNo=0;
            currentLinePos=0;
            
            autoAdjust=true;
            needToFindBreaks=true;
            isCommentWidthReset=false;
            isBreakPointReset=false;
            
            writeHeaderOnly=false;
            drawBandOnly=false;
            drawFigureOnly=false;
            
            
            isLoadedKeynumberFile=false;
            
            isModified=false;
            isTableControlModified=false;
            isHeaderSettingModified=false;
            isBandSettingModified=false;
            
            useControlFile=false;
            includeAllDrawings=true;
            includeRefTitle=false;
            printDocumentRecord=false;
            
            nodrawing=false;
            nodrawingExceptAdoptedBands=false;
            showSuppressed=false;
            showInterBand=false;
            showAllAuthors=false;
            
            deleteFigureFiles=true;
            
            isForWebDisplay=false;
            showHeading=true;
            heading="NUCLEAR DATA SHEETS";
            removeAllHeading=false;
            removeAllFooting=false;
            
            isPortrait=true;
            isNewPage=false;
            isFormatTables=false;
            
            isNewFigurePage=false;
            
            numberOfPages=0;
            
            breakLineNo=NDSConfig.MAX_LINE_COUNT-4;
            breakLinePos=NDSConfig.MAX_TEXT_HEIGHT-4*NDSConfig.DEFAULT_LINEWIDTH;
            
            reorderGamma=false;
            reorderByGamma=false;
            reorderByLevel=false;
            
            numberOfAuthorsToShow=4;
            
            userNSRFilename="";
            NSRDataBaseLoginFileName="";
            
            isForcedNSRWebQuery=true;
            isSilentJDBC=false;
            
            useLetterFootnoteMarkFirst=false;
    }

    public static void setControlDefaults(){
        hasIndex=true;
        blankPage=true;
        type="REVIEW";
        referenceA="";
        hasReference=false;
        hasAbstract=true;
        abstractVolume="xx";
        year="****";
        receivedDate="****";
        revisedDate="";
        page="****";
        Z="";
        skelOptions="SKELETON";
        hasSkeleton=true;
        isUpdate=false;
        updCom1="";
        updCom2="";
        controls=null;
        nsplit=1;
        currentLineNo=0;
        currentLinePos=0;
        
        autoAdjust=true;
        needToFindBreaks=true;
        isCommentWidthReset=false;
        isBreakPointReset=false;
        
        writeHeaderOnly=false;
        drawBandOnly=false;
        drawFigureOnly=false;
        
        isLoadedKeynumberFile=false;
        		
        isModified=false;
        isTableControlModified=false;
        isHeaderSettingModified=false;
        isBandSettingModified=false;
        
        useControlFile=false;
        includeAllDrawings=true;
        includeRefTitle=false;
        printDocumentRecord=false;
        
        nodrawing=false;
        showSuppressed=false;
        showInterBand=false;
        showAllAuthors=false;
        
        deleteFigureFiles=true;
        
        isForWebDisplay=false;
        showHeading=true;
        heading="NUCLEAR DATA SHEETS";
        removeAllHeading=false;
        removeAllFooting=false;
        
        isPortrait=true;
        isNewPage=false;
        isFormatTables=false;
        
        isNewFigurePage=false;
        
        numberOfPages=0;
        
        breakLineNo=NDSConfig.MAX_LINE_COUNT-4;
        breakLinePos=NDSConfig.MAX_TEXT_HEIGHT-4*NDSConfig.DEFAULT_LINEWIDTH;
        
        reorderGamma=false;
        reorderByGamma=false;
        reorderByLevel=false;
        NSRDataBaseLoginFileName="";
        
        isForcedNSRWebQuery=true;
        isSilentJDBC=false;
        		
        numberOfAuthorsToShow=4;
        
        userNSRFilename="";
        
        useLetterFootnoteMarkFirst=false;
}    
    
	/**Given a line from the control file and an option name, returns all the arguments of that option as a vector*/
	public static Vector<String> getLine(String s,String type){
	    Vector<String> out=new Vector<String>();
	    int ix=s.indexOf(type);
	    int oldx;
	    if (ix<0||ix>=s.length()) return null;
	    s=s.substring(ix+3);
	    ix=s.indexOf("/");
	    int temp=s.indexOf("{"); 
	    if(temp>=0&&temp<ix) ix=temp;
	    if(ix>=0&&ix<s.length()) s=s.substring(0,ix);
	    oldx=0;
	    while(true){
	        ix=s.indexOf(",",oldx);
	        if(ix>=0&&ix<s.length()) out.add(s.substring(oldx,ix));
	        else{out.add(s.substring(oldx)); break;}
	        oldx=ix+1;
	    }       
	    return out;
	}

	public static float getLineAsFloat(String s,String type){
	    String temp=getLineAsString(s,type);
	    if(temp.length()<=0) return -1;
	    else 
	        try{ return Float.parseFloat(temp);}
	        catch(Exception e){System.out.println("Control file expects a number, but you gave it "+temp);
	        return -1;
	        }
	}

	public static String getLineAsString(String s,String type){
	    int ix=s.indexOf(type);
	    if(ix<0||ix>=s.length()) return "";
	    s=s.substring(ix+3);
	    ix=s.indexOf("/");
	    int temp=s.indexOf("{"); 
	    if(temp>=0&&temp<ix) ix=temp;
	    if(ix>=0&&ix<s.length()) return s.substring(0,ix);
	    else return s;
	}

	/**Translates control file column heading codes into little-c format*/
	public static String transColHead(String s){
	    String out="";
	    if(s.equals("E")) out="Energy";
	    if(s.equals("LEV")) out="E(level)";
	    if(s.equals("J")) out="J$^{\\pi}$";
	    if(s.equals("JI")) out="J$^{\\pi}_{i}$";
	    if(s.equals("JF")) out="J$^{\\pi}_{f}$";
	    if(s.equals("RI")) out="I$_{\\gamma}$";
	    if(s.equals("IB")) out="I$\\beta$";
	    if(s.equals("IA")) out="I$\\alpha$";
	    if(s.equals("IP")) out="I$_{(particle)}$";
	    if(s.equals("XREF")) out="XREF";
	    if(s.equals("M")) out="Mult.";
	    if(s.equals("IE")) out="I$\\varepsilon$";
	    if(s.equals("HF")) out="HF";
	    if(s.equals("T")) out="T$_{1/2}$";
	    if(s.equals("MR")) out="$\\delta$";
	    if(s.equals("LOGFT")) out="log$ft$";
	    if(s.equals("L")) out="l";
	    if(s.equals("CC")) out="$\\alpha$";
	    if(s.equals("S")) out="S";
	    if(s.equals("TI")) out="I$_{(\\gamma+ce)}$";
	    if(s.equals("EI")) out="E$_{(intermediate)}";
	    if(s.equals("COMM")) out="comments";
	    return out;
	}

	/**for the purpose of determining the length of column name in final output*/
	public static String transColHeadENSDF(String s){
	    String out="";
	    if(s.equals("E")) out="Energy";
	    if(s.equals("LEV")) out="E(level)";
	    if(s.equals("J")) out="JP";
	    if(s.equals("JI")) out="JPi";
	    if(s.equals("JF")) out="JPf";
	    if(s.equals("RI")) out="IG";
	    if(s.equals("IB")) out="IB";
	    if(s.equals("IA")) out="IA";
	    if(s.equals("IP")) out="Iparticle";
	    if(s.equals("XREF")) out="XREF";
	    if(s.equals("M")) out="Mult.";
	    if(s.equals("IE")) out="IE";
	    if(s.equals("HF")) out="HF";
	    if(s.equals("T")) out="T1/2$";
	    if(s.equals("MR")) out="D";
	    if(s.equals("LOGFT")) out="LOGFT";
	    if(s.equals("L")) out="L";
	    if(s.equals("CC")) out="C";
	    if(s.equals("S")) out="S";
	    if(s.equals("TI")) out="I(g+ce)";
	    if(s.equals("EI")) out="E(intermediate)";
	    if(s.equals("COMM")) out="comments";
	    return out;
	}
	
	public static void writeControlCommands(java.io.Writer out,EnsdfTableData etd, String type)throws IOException{
        etd.writeControl(out, type);
	}
	
	//since non-table-data entry has smaller font size and line-space, such lines should not be counted as 
	//full-size lines and a weight<1 should be used. Otherwise, the maximum number of lines will be different
	//in different pages
	//NOT USED
	public static void incrementCurrentLineNo(int count,double weight){	
	int maxLineCount=NDSConfig.MAX_LINE_COUNT;
	if(!isPortrait)
		maxLineCount=NDSConfig.MAX_LINE_COUNT_LANDSCAPE;
	
	currentLineNo+=count*weight;
	
	if(currentLineNo>maxLineCount)
		currentLineNo-=maxLineCount;
	}
	
	public static void incrementCurrentLineNo(int count){	
		incrementCurrentLineNo(count,1);
	}
	
    //increment global lineNo and position
	@SuppressWarnings("unused")
	public static void incrementCurrentLineNoAndPos(int count,double height){	
		int maxLineCount=NDSConfig.MAX_LINE_COUNT;
		float maxLinePos=NDSConfig.MAX_TEXT_HEIGHT;
		if(!isPortrait){
			maxLineCount=NDSConfig.MAX_LINE_COUNT_LANDSCAPE;
			maxLinePos=NDSConfig.MAX_TEXT_WIDTH;
		}
		
		currentLineNo+=count;
		currentLinePos+=height;
		
		//if(currentLineNo>maxLineCount)
		//	currentLineNo-=maxLineCount;
		
		//if(currentLinePos>=maxLinePos){
		//	currentLinePos=0;
		//	currentLineNo=0;
		//}
	}
	
	public static void incrementCurrentLineNoAndPos(float n){
		int count=(int)n;
		if(n>count)
			count=count+1;
		
        incrementCurrentLineNoAndPos(count,n*NDSConfig.DEFAULT_LINEWIDTH);
	}
	
	
    //Increment global lineNo and position. (specifically used for comment lines, NOT for table body)
	//If a page break is reached, write a new page into the "out" Latex file.
	//For this purpose, the line No should be incremented by one.
	public static void incrementCurrentLineNoAndPos(int count,double height,java.io.Writer out)throws IOException{
		double pos=NDSControl.currentLinePos+height;
		
		if(pos>=NDSConfig.MAX_TEXT_HEIGHT-2*NDSConfig.GENNERAL_COMMENT_LINEWIDTH){
			NDSControl.currentLineNo=0;
			NDSControl.currentLinePos=0;
			
			NDSControl.numberOfPages+=1;
			//if(autoAdjust) out.write("\\clearpage\n");
		}
		else
			incrementCurrentLineNoAndPos(count,height);			
	}
	
	public static void incrementCurrentLineNoAndPos(int count,double height,String topTitle,java.io.Writer out)throws IOException{
		double pos=NDSControl.currentLinePos+height;
		
		if(pos>=NDSConfig.MAX_TEXT_HEIGHT-2*NDSConfig.GENNERAL_COMMENT_LINEWIDTH){					
			setTopTitleSpacingInNewPage(topTitle,out);
			NDSControl.numberOfPages+=1;
		}
		else
			incrementCurrentLineNoAndPos(count,height);			
	}
	
	public static void incrementCurrentLineNoAndPos(int count,double height,String topTitle,String tableTitle,java.io.Writer out)throws IOException{
		double pos=NDSControl.currentLinePos+height;
		//if(pos>=Config.MAX_TEXT_HEIGHT-2*Config.DEFAULT_LINEWIDTH){
		if(pos>=NDSConfig.MAX_TEXT_HEIGHT-2*NDSConfig.GENNERAL_COMMENT_LINEWIDTH){				
			//if(autoAdjust) {
				setTopTitleSpacingInNewPage(topTitle,out);
				NDSControl.numberOfPages+=1;
				
				//table title
				if(tableTitle.trim().length()>0)//LaTeX does not allow end-line symbol "\\" to end an empty line
					out.write(tableTitle+"\\\\\n");
				else
					out.write("Unknown title\\\\\n");
				
		        NDSControl.incrementCurrentLineNoAndPos(1);
		        if(tableTitle.contains("underline"))
		        	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
			//}
			//else
			//	setTopTitleSpacingInNewPage();			
		}
		else
			incrementCurrentLineNoAndPos(count,height);	
	}
       
               
	//check if new page should be started based on currentLinePos
	public static boolean checkNewPage(){
		
		if(NDSControl.currentLinePos>NDSConfig.MAX_TEXT_HEIGHT-2-4*NDSConfig.DEFAULT_LINEWIDTH){//top of table title to bottom of head line is about 2cm, with nothing in between them (general comments, table records table)
			NDSControl.currentLineNo=0;
			NDSControl.currentLinePos=0;
			
			NDSControl.numberOfPages+=1;
			
			return true;
		}
		
		return false;
	}
	
	public static void resetNewPage(java.io.Writer out)throws IOException{
		resetNewPage();
		
	    //if(autoAdjust)
	    	out.write("\\clearpage\n");
	}
	
	public static void resetNewPage(){
		NDSControl.currentLineNo=0;
		NDSControl.currentLinePos=0;
	}
	
	public static void setTopTitleSpacingInNewPage(String topTitle,java.io.Writer out)throws java.io.IOException{
		setTopTitleSpacingInNewPage(out);
		
	    //if(autoAdjust)
	    	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");
	}
	
    public static void setTopTitleSpacingInNewPage(java.io.Writer out)throws java.io.IOException{
    	setTopTitleSpacingInNewPage();
    	hasSetNewPageForTopTitle=true;
    	
	    //if(autoAdjust){
	    	out.write("\\clearpage\n");
	        out.write("\\vspace{0.3cm}\n");
	    //}
	        
	    setDefaultHeading(out);//for web-display only; write nothing for others
	    //debug
	    //System.out.println("In Control line 636: "+Control.heading);
    }
    
    public static void setTopTitleSpacingInNewPage(){
    	NDSControl.resetNewPage();
		NDSControl.incrementCurrentLineNoAndPos(1,0.30);    //blank line between toptitle and page topline
	    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TOP_TITLE_LINEWIDTH);//dataset top title line=0.430 cm
	    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline=0.20 cm
	    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.DEFAULT_LINEWIDTH);    //one blank line after dataset top title=0.34 cm  
	    
	    NDSControl.numberOfAllTitleLines=4;
	    NDSControl.heightOfAllTitleLines=0.30f+NDSConfig.TOP_TITLE_LINEWIDTH+NDSConfig.UNDERLINE_WIDTH+NDSConfig.DEFAULT_LINEWIDTH;
    }
    
    public static void addToAllTitleLines(int count,double height){
    	NDSControl.numberOfAllTitleLines+=count;
    	NDSControl.heightOfAllTitleLines+=height;
    }
    
	public static void setDefaultHeading(java.io.Writer out)throws java.io.IOException{
		if(NDSControl.isForWebDisplay)
    		out.write("\\chead{\\small{"+NDSControl.heading+" \\\\ }}\n");//Control.heading has been reset at beginning of each dataset
	}
}


