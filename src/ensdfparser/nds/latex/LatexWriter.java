package ensdfparser.nds.latex;

import ensdfparser.base.BaseRun;
import ensdfparser.ensdf.*;
import ensdfparser.ensdf.QValue.QV;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.*;
import ensdfparser.nds.ensdf.MassChain.DocumentRecords;
import ensdfparser.nds.util.Str;
import ensdfparser.nsr.BaseNSR;

import java.time.Year;
import java.util.*;

/**
 * Write tabular data into a latex file.
 * There is some fairly advanced latex in here, but don't remove any of it unless you really know what you're doing
 * Writes Levels and Decay tables, Gamma tables are handled by another file
 * 
 * A note about centering: a \end{center}\begin{center} statement takes up space on the page for no reason. Therefore to reduce 
 * the amount of times going back and forth between centering we keep everything after the skeleton drawing in centered mode. We leave centered 
 * mode only for comments if they exist
 */
public class LatexWriter<T extends BaseRun<?>> {

    protected boolean sysOut=false;
    float lineweight=1;
    protected T run;
    
    private Vector<String> figureFiles=new Vector<String>();
    
    //store keynumber found from NSR for a given NDS citation in a history record
    private HashMap<String,String> citationKeynoMap=new HashMap<String,String>();
    
                /// tail of tex file
    public static String[] tail = {
        "\\end{document}\n"
    };
    /// ENSDF file
    protected ENSDF ens;
    /** Creates a new instance of Tables */
    public LatexWriter() throws Exception{

    }

    /** Creates a new instance of Tables */
    public LatexWriter(T run) throws Exception{
         this();
         this.run=run;
    }
    
    /**
     * Write head of a latex file.
     * There is some fairly advanced latex in here but I think that it is all needed
     * Don't remove anything from this unless you really know what you're doing
     */
    public void writeHead(java.io.Writer out) throws Exception{

        out.write("\\documentclass{article}[10pt,letterpaper");
        if(!ensdfparser.nds.config.NDSConfig.portrait) out.write("\\,landscape");
        out.write("]\n");
        
        out.write("\\usepackage{fancyhdr}\n");
        out.write("\\usepackage{supertabular}\n");
        
        
        if(!NDSControl.removeAllFooting){
        	out.write("\\pagestyle{fancy}\n");
        }

        String tooltip=run.name()+" (version "+run.version()+")";
        //head="Nuclear Data Sheets" for NDS and ="From ENSDF" or "From XUNDL" for web-display
        if(NDSControl.showHeading && !NDSControl.heading.isEmpty()) { 
        	String s=NDSControl.heading;
        	String s1=s.substring(0,s.length()-1);
        	String s2=s.substring(s.length()-1);
        	
        	s=s1+"\\pdftooltip{"+s2+"}{"+tooltip+"}";
        	out.write("\\chead{\\small{"+s+" \\\\ }}\n");
        }else {
        	out.write("\\chead{\\small{\\pdftooltip{\\phantom{Java-NDS}}{"+tooltip+"} \\\\ }}\n");
        }
        
        if(!NDSControl.removeAllFooting){
        	out.write("\\cfoot{ \\thepage}\n");
        }
        
        out.write("\\renewcommand{\\sectionmark}[1]{\\markboth{#1}{}}\n");
        //out.write("\\renewcommand{\\subsectionmark}[1]{\\markright{-#1}}\n");
        out.write("\\lhead{\\leftmark -\\thechappage \\ \\\\}\n");
        out.write("\\rhead{\\leftmark -\\thechappage \\ \\\\}\n");
        //out.write("\\headheight 16.0pt\n");      

        
        out.write("\\setlength{\\headwidth}{"+(NDSConfig.MAX_TEXT_WIDTH+0.f)+"cm}\n");//7.5in original
        out.write("\\setlength{\\textwidth}{"+(NDSConfig.MAX_TEXT_WIDTH+1.0f)+"cm}\n");//7.5in original, 0.5f added to compensate the negative indent of the first line
        out.write("\\setlength{\\textheight}{"+(NDSConfig.MAX_TEXT_HEIGHT+0.5f)+"cm}\n");//8.9in original
        out.write("\\addtolength{\\hoffset}{"+NDSConfig.HOFFSET+"cm}\n");//-1.37in original
        out.write("\\addtolength{\\voffset}{"+NDSConfig.VOFFSET+"cm}\n");//-0.7in original
        
        out.write("\\setcounter{secnumdepth}{0}\n");
        out.write("\\usepackage{color}\n");
        out.write("\\usepackage{amsmath}\n");
        out.write("\\usepackage{pbox}\n");
        out.write("\\usepackage{amssymb}\n");
        out.write("\\usepackage{amsbsy}\n");
        out.write("\\usepackage{scalefnt}\n");
        out.write("\\usepackage{ifpdf}\n");
        //out.write("\\usepackage{soul}\n");
        out.write("\\usepackage{graphicx}\n");
        out.write("\\usepackage{multicol}\n");
        out.write("\\usepackage[colorlinks=true, linkcolor=blue, linktocpage=true,urlcolor=blue]{hyperref}\n");
        out.write("\\usepackage{longtable}\n");
        out.write("\\usepackage{needspace}\n");
        out.write("\\usepackage{eso-pic}\n");
        out.write("\\usepackage{mathptmx}\n");
        out.write("\\usepackage{multirow}\n");
        out.write("\\usepackage{lscape}\n");
        out.write("\\usepackage{textcomp}\n");
        out.write("\\usepackage{changepage}\n");
        out.write("\\usepackage{xfrac}\n");
        out.write("\\usepackage{txfonts}\n");//for lambdabar
        out.write("\\usepackage{pdfcomment}");//for pdf tooltip
        out.write("\\setlength\\LTleft{0.in}\n");//longtable margin relative to page margin
        out.write("\\setlength\\LTright{0.in}\n");
        
        out.write("\\usepackage[absolute]{textpos}\n");//textblock used to show "Continued on next ..." note; [absolute,showboxes] to show box
        out.write("\\setlength{\\TPHorizModule}{1cm}\n");//set textblock dimension unit
        out.write("\\setlength{\\TPVertModule}{1cm}\n");
        
        //out.write("\\usepackage[tracking=true,spacing=true]{microtype}");
        out.write("\\setcounter{LTchunksize}{40}\n");
        out.write("\\ifpdf\n");//makes sure the images come out ok in pdfs
        out.write("\\DeclareGraphicsRule{*}{mps}{*}{}\n");
        out.write("\\fi\n");
        out.write("\\makeatletter\n");//gets rid of dotted lines in the table of contents
        out.write("%\\renewcommand{\\@dotsep}{100}\n");
       
        //set title indent (from 2nd line)
        out.write("\\renewcommand{\\l@section}{\\@dottedtocline{2}{1em}{0em}}\n");
        //out.write("\\renewcommand{\\l@subsection}{\\@dottedtocline{3}{1em}{0em}}\n");
        out.write("\\renewcommand{\\l@subsection}{\\@dottedtocline{3}{2.5cm}{0em}}\n");
        
        out.write("\\renewcommand{\\hrulefill}{\\leavevmode \\leaders \\hrule \\@height 1pt \\hfill \\kern\\z@}\n");
        out.write("\\makeatother\n");

        out.write("\\renewcommand{\\underline}[1]{\\begin{tabular}{@{\\extracolsep{\\fill}}c@{\\extracolsep{\\fill}}}#1\\\\[-0.2cm]\\hrulefill\\end{tabular}}\n");
        out.write("\\renewcommand{\\footrulewidth}{1pt}\n");
        out.write("\\renewcommand{\\headrulewidth}{1pt}\n");
        out.write("\\addtolength{\\footskip}{-0.4cm}\n");//move bottom line up by 0.4 cm
        
        
        //out.write("\\UseMicrotypeSet{alltext}\n\\SetTracking{encoding=*}{130}\n");
        out.write("\\newcounter{chappage}\n");//creates a counter for the chapter page numbers
        out.write("\\setcounter{chappage}{1}\n");
        out.write("\\AddToShipoutPicture{\\stepcounter{chappage}}\n");
        out.write("\\fancypagestyle{plain}{\\lhead{}\\rhead{}}\n");
        out.write("\\fancypagestyle{single}{\\lhead{\\leftmark \\ \\\\}\\rhead{\\leftmark \\ \\\\}}\n");//for single page
        out.write("\\fancypagestyle{bob}{\\lhead{\\leftmark -\\thechappage \\ \\\\}\\rhead{\\leftmark -\\thechappage \\ \\\\}}\n");
        out.write("\\begin{document}\n");
        out.write("\\fontsize{9}{10}\\selectfont\n");
        out.write("\\fontdimen2\\font=1.3\\fontdimen2\\font\n");
        
        
    }
    /**
     * Write tail of latex file.
     */
    public void writeTail(java.io.Writer out) throws Exception{
        for (int x=0; x<tail.length; x++)
            out.write(tail[x]);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //print functions from Translator.java
    
    /**prints numbers with units where characters like "<" need to be replaced */
    public static String printNumber(String x,String dx,boolean equals)throws Exception{
        return Translator.printNumber(x, dx, equals);               
    } 
    
    /**prints numbers and uncertainties as-is in an easy way */
    public static String printNumber(String x,String dx,String unit)throws Exception{
    	return Translator.printNumber(x, dx, unit);
    }  

    /**prints numbers if no unit is given */
    public static String printNumber(String x,String dx)throws Exception{
        return Translator.printNumber(x, dx);
    } 
    
    /**prints numbers and uncertainties as-is in an easy way */
    public static String printNumber(String x,String dx,String unit,boolean equals)throws Exception{
    	return Translator.printNumber(x, dx, unit,equals);
    }  
    
    
    
    /**prints numbers as is where characters like "<" need to be replaced */
    public static String printNumberAsIs(String x,String dx,String unit,boolean equals)throws Exception{
        return Translator.printNumberAsIs(x, dx, unit,equals);
    }
       
    public static String printNumberAsIs(String x,String dx,boolean equals)throws Exception{
        return Translator.printNumberAsIs(x, dx, equals);
    }
       
    /**prints numbers and uncertainties in an easy way */
    public static String printNumberAsIs(String x,String dx,String unit)throws Exception{
        return Translator.printNumberAsIs(x, dx, unit);
    }      
  
    /**prints numbers as-is if no unit is given */
    public static String printNumberAsIs(String x,String dx)throws Exception{
        return Translator.printNumberAsIs(x, dx);
    }
    
    
    /**takes a unit for a half-life and returns it in little-C comment form
    *there are a lot of parts of the ensdf package that convert units to unicode, etc, but I thint little-c is the
    *best because then you can just use a dictionary to convert to latex, unicode, etc */
    public static String halfLifeUnits(String s){
        return Translator.halfLifeUnits(s);
    }
    ////////////////////////////////////////////////////////////////////////////////////
    
    
    /** include a metapost file specified by ' Drawing d ' in the latex file */
    public void writeFigure(java.io.Writer out,DrawingControl d,String filename)throws Exception{
        int nchart=d.getPages();
        ensdfparser.ensdf.Nucleus n=d.nucleus();
        String code="";
        if(n!=null)  {
            code = "\\ensuremath{^{"+String.valueOf(n.A())+"}_{";
            if(String.valueOf(n.A()).length()>String.valueOf(n.Z()).length()) code+="\\ ";
            code+=String.valueOf(n.Z())+"}}"+n.En()+
                    "\\ensuremath{_{"+String.valueOf(n.N())+"}}";}

        //debug
        //System.out.println("In WriteFigure line 200: d.isNewPage="+d.isNewPage());
        
        if(d.isNewPage()) {
        	out.write("\\clearpage\n");
        	NDSControl.currentLineNo=0;
        	NDSControl.currentLinePos=0;
        	NDSControl.numberOfPages+=1;
        }
        
        //debug
        //System.out.println("In LatexWriter line 205: nchart="+nchart+" filename="+filename);
        
        for(int i=0;i<nchart;i++){
            out.write("\\begin{figure}[h]\n");
            out.write("\\begin{center}\n");
            String figureFileName=Str.fileNamePrefix(filename)+"-"+i+".ps";
            if(d.isPortrait()) 
            	out.write("\\includegraphics{"+figureFileName+"}");
            else{ 
            	out.write("\\includegraphics[angle=90]{"+figureFileName+"}");
            	if(!d.isNewPage())
            		NDSControl.numberOfPages+=1;//forced to draw on a new page in landscape mode
            }
            
            //debug
            //System.out.println("In LatexWriter line 217: i="+i+" figurefilename="+figureFileName);
            
            out.write("\\\\\n");
            
            if(d.isPortrait()&&n!=null) {out.write(code+"\n");}

            out.write("\\end{center}\n");
            out.write("\\end{figure}\n");
            

            if(i<nchart-1) {
            	out.write("\\clearpage\n");
            	NDSControl.numberOfPages+=1;
            }
            
            figureFiles.addElement(figureFileName);
        }
        figureFiles.addElement(Str.fileNamePrefix(filename)+".mp");
        figureFiles.addElement(Str.fileNamePrefix(filename)+".mpx");
        figureFiles.addElement(Str.fileNamePrefix(filename)+".log");
    }
    
    public void writeSkeletonFigure(java.io.Writer out,DrawingControl d,String filename)throws Exception{
        int nchart=d.getPages();
        ensdfparser.ensdf.Nucleus n=d.nucleus();
        String code="";
        if(n!=null)  {
            code = "\\ensuremath{^{"+String.valueOf(n.A())+"}_{";
            if(String.valueOf(n.A()).length()>String.valueOf(n.Z()).length()) code+="\\ ";
            code+=String.valueOf(n.Z())+"}}"+n.En()+
                    "\\ensuremath{_{"+String.valueOf(n.N())+"}}";}

        //debug
        //System.out.println("In WriteFigure line 200: d.isNewPage="+d.isNewPage());
        
        if(d.isNewPage()) {
        	out.write("\\clearpage\n");
        	NDSControl.currentLineNo=0;
        	NDSControl.currentLinePos=0;
        	NDSControl.numberOfPages+=1;
        }
        
        //debug
        //System.out.println("In LatexWriter line 205: nchart="+nchart+" filename="+filename);
        
        for(int i=0;i<nchart;i++){
            out.write("\\begin{figure}[h!]\n");
            out.write("\\begin{center}\n");
            String figureFileName=Str.fileNamePrefix(filename)+"-"+i+".ps";
            if(d.isPortrait()) 
            	out.write("\\includegraphics[scale=0.97]{"+figureFileName+"}");
            else{ 
            	out.write("\\includegraphics[scale=0.97,angle=90]{"+figureFileName+"}");
            	if(!d.isNewPage())
            		NDSControl.numberOfPages+=1;//forced to draw on a new page in landscape mode
            }
            
            //debug
            //System.out.println("In LatexWriter line 217: i="+i+" figurefilename="+figureFileName);
            
            out.write("\\\\\n");
            
            if(d.isPortrait()&&n!=null) {out.write(code+"\n");}

            out.write("\\end{center}\n");
            out.write("\\end{figure}\n");
            

            if(i<nchart-1) {
            	out.write("\\clearpage\n");
            	NDSControl.numberOfPages+=1;
            }
            
            figureFiles.addElement(figureFileName);
        }
        figureFiles.addElement(Str.fileNamePrefix(filename)+".mp");
        figureFiles.addElement(Str.fileNamePrefix(filename)+".mpx");
        figureFiles.addElement(Str.fileNamePrefix(filename)+".log");
    }
    
    public void writeTestPage(java.io.Writer out)throws Exception{
        char[] escapes={'&','%','$','#','_','{','}'};
        boolean isEscapeChar=false;
        
    	out.write("\\clearpage\n");
    	out.write("\\begin{flushleft}");
    	String line="",temp="";
    	for(char ch=' ';ch<='~';ch++){
    		line="";
    		isEscapeChar=false;
    		for(char c: escapes){
    			if(ch==c){
    				isEscapeChar=true;
    				break;
    			}
    		}
    		
            temp="\\ ";
    		if(isEscapeChar)
    			temp="\\"+ch;
    		else if(ch==' ')
    			line+='s';
    		else if(ch=='<')
    			temp="\\textless";
    		else if(ch=='>')
    			temp="\\textgreater";
    		else if(ch=='~')
    			temp="\\textasciitilde";
    		else if(ch=='^')
    			temp="\\textasciicircum";
    		else if(ch=='\\')
    			temp="\\textbackslash";
    		else
    			temp=""+ch;

    		for(int i=0;i<50;i++)
    		    line+=temp;
    		
    		if(ch==' ')
    			line+='s';
    		out.write(line+"\\\\\n");

    	}
    	
		
    	out.write("\\end{flushleft}");
    	out.write("\\clearpage\n");
    }
    
    
    /** writes the tables for one ENSDF dataset*/
    //return TopTitle to be used in level/decay scheme page
    public String writeBody(java.io.Writer out,EnsdfTableData etd,MassChain data) throws Exception{
       
    	//debug
    	//System.out.println("In writeBody: beginning...");

    	//set local dictionary
    	UserDictionary.setDICT(etd.getDICT());
    	
    	ens=etd.getENSDF();
        ensdfparser.ensdf.Nucleus n = ens.nucleus();
        boolean adopted=false;
        Vector<String> tempComs;
        
        String linewidth_str="";
        
        out.write("%"+ens.fullDSId()+"\n");
        if(ens.lineAt(0).indexOf("ADOPTED LEVELS")==9) 
        	adopted=true;
        
        String code = "\\ensuremath{^{"+String.valueOf(n.A())+"}_{";
        String En=n.En();

        if(En.toUpperCase().equals("NN"))
        	En="n";
        
        if(String.valueOf(n.A()).length()>String.valueOf(n.Z()).length()) code+="\\ ";
        
        code+=String.valueOf(n.Z())+"}}"+En+
                "\\ensuremath{_{"+String.valueOf(n.N())+"}^{~}}";

        
        int index=-1;
        boolean reset=false;
        String name="";
        //if an adopted dataset, start a new nucleus 
        if(adopted){//etd.isNewPage() has been forced to be true in EnsdfTableData if Adopted dataset 
            if(etd.isNewPage()){//reset headers if first adopted dataset on this page
                out.write("\\section["+code+"]{ }\n");
                out.write("\\vspace{-30pt}\n");
                out.write("\\setcounter{chappage}{1}\n");
                NDSControl.currentLineNo=0;
                NDSControl.currentLinePos=0;
                
                NDSControl.numberOfPages+=1;
                
                reset=true;
                
            }else{//if this is not the first dataset on the page, put in a line that looks like a header
                out.write("{\\normalsize "+code+"\\hfill"+code+"\\\\\n}");
                out.write("\\indent\\hrulefill\\\\\n");
                out.write("\\vspace{"+(NDSConfig.DEFAULT_LINEWIDTH+0.1)+"cm}\n");
                NDSControl.incrementCurrentLineNoAndPos(1+1,NDSConfig.DEFAULT_LINEWIDTH+0.1);
            }
        }
        else{
        	index=data.getIndex(etd);
        	
        	if(index==0)
        		reset=true;
        	else if(index>0){
        		name=data.getENSDF(index-1).nucleus().En();
        		if(!name.equals(n.En()))
        			reset=true;
        	}
        	
        	if(reset){
                out.write("\\section["+code+"]{ }\n");
                out.write("\\vspace{-30pt}\n");
                out.write("\\setcounter{chappage}{1}\n");
                NDSControl.currentLineNo=0;
                NDSControl.currentLinePos=0;
                
                //Control.numberOfPages+=1;
        	}else{
        		//out.write("\\vspace{-30pt}\n");
        	}
        }
        

	
        String DSID0=ens.DSId0();
        String DSID=Translator.process(DSID0,false);
        
    	//debug
    	//System.out.println("In writeBody: write title OK..."+DSID);
    	
        boolean canBeTranslated=Translator.canBeTranslatedAsDSID;

        if(EnsdfUtil.findDSIDType(ens.fullDSId()).equals("decay")){
            Parent p;
            canBeTranslated=true;
            for(int i=0;i<ens.parentsV().size();i++){
                p=(Parent)ens.parentAt(i);
                if(p.DMName().length()<=0)
                    canBeTranslated=false;
                else if( !p.DMName().equals("P") && !p.DMName().equals("N") && !Translator.canBeTranslated(p.DMName()) )
                	canBeTranslated=false;
            }
        }else if(!canBeTranslated && DSID0.contains(",")) {
        	DSID=DSID0;
        	DSID=DSID.replace("(P,","(p,").replace(",P)",",p)");
        	DSID=DSID.replace("(D,","(d,").replace(",D)",",d)");
        	DSID=DSID.replace("(T,","(t,").replace(",T)",",t)");
        	DSID=DSID.replace("(N,","(n,").replace(",N)",",n)");

        	DSID=Translator.process(DSID,false);

        }
        
        if(!canBeTranslated){//canBeTranslated is checked in each call of "process"
        	if(EnsdfUtil.findDSIDType(ens.fullDSId()).equals("decay"))
        		run.printMessage("***Warning: decay mode in DSID:"+ens.fullDSId()+" MAY or MAY NOT be translated correctly!");
        	else        	
        		run.printMessage("***Warning: DSID:"+ens.fullDSId()+" MAY or MAY NOT be translated correctly!");
        	
        	run.printMessage("***Please check and define it if incorrect.");
            run.printMessage("***To define it, use \"DICT$word_in_uppercase=translation_in_lowercase\" in a new general comment record.");
            //run.printMessage("");
        }
        
        //set up labels for table of contents by creating LaTeX subsections
        
        //for DSID=187RE[+75] B- DECAY (32.9 Y), "[" and "]" are not allowed in \subsection[]
        //\hphantom{} is not allowed in subsection 
        if(DSID.contains("[") && DSID.contains("]")){
        	DSID=DSID.replace("[", "{[");
        	DSID=DSID.replace("]","]}");        	
        }
    	
        if(DSID.contains("phantom")){
    		int p1=DSID.indexOf("\\hphantom");
    		if(p1<0)
    			p1=DSID.indexOf("\\phantom");
    		
    		if(p1>0){
    			int p2=DSID.indexOf("}", p1);
    			if(p2>0){
    				DSID=DSID.substring(0,p1)+DSID.substring(p2+1);
    			}
    		}
    	}
    	
        if(adopted){
            //out.write("\\subsection["+code+"\\hspace{2cm}"+DSID+"]{ }\n");
            out.write("\\subsection[\\hspace{-0.2cm}"+DSID+"]{ }\n");
            out.write("\\vspace{-20pt}\n");
            out.write("\\vspace{0.3cm}\n");//leave one blank line, this assures the same toptitle margin as that in continued page 
        }
        else{
            //out.write("\\subsection[\\ \\hspace{3cm}"+DSID);
            out.write("\\subsection[\\hspace{-0.2cm}"+DSID);
            if(ens.lineAt(1).contains("2   "))out.write("...");
            out.write("]{ }\n");
            
            if(reset)
            	out.write("\\vspace{-20pt}\n");
            else
            	out.write("\\vspace{-27pt}\n");//for dataset that is not the first one of a nuclide (no section label), needs to move up more 
                                               //to ensure the same margin above top-title as the first dataset
                     
            out.write("\\vspace{0.3cm}\n");
        }  
        
    	//debug
    	//System.out.println("In writeBody: write subtitle OK...");

    	
        //test page
        //writeTestPage(out);
        
        NDSControl.incrementCurrentLineNoAndPos(1,0.3f);//blank line right after the top line in the first page
        
        //debug
        //if(Control.autoAdjust){
        //    System.out.println("\n===============================================================");
        //    System.out.println("In latexwriter 561: begining (before top title) "+NDSControl.currentLineNo+" "+NDSControl.currentLinePos+" name="+n.EN()+" index="+index+" reset NewPage="+reset);
        //    System.out.println("===============================================================");       	
        //}
        
        /////////////////////////
        //write title of dataset
        /////////////////////////    
        String TopTitle=makeDatasetTitleAndRef(etd,true);
        TopTitle="{\\bf \\small \\underline{"+TopTitle;
                
        //generate hyperlink target for link in XREF
        out.write("\\hypertarget{"+etd.getLinkID()+"}{"+TopTitle+"}}}\\\\\n");       
        TopTitle+=(" (continued)}}");
        
        NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TOP_TITLE_LINEWIDTH);//for top-title line
        
        //System.out.println("In latexwriter 577: after top title: lineNo="+NDSControl.currentLineNo+" pos="+NDSControl.currentLinePos);
        
        NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline plus gap above and below it
               
        //System.out.println("In latexwriter 581: after top title: lineNo="+NDSControl.currentLineNo+" pos="+NDSControl.currentLinePos);
        
        if(NDSControl.isForWebDisplay && ens.nHis()>0){
        	writeHistory(out,ens);
        }else{
            out.write("\\vspace{4pt}\n");//4pt=0.141cm
        }
        
        //begin comments section for the whole dataset
        if(!etd.getLevelTableControl().isDrawn()&&!etd.getGammaTableControl().isDrawn()&&!etd.getDecayTableControl().isDrawn()&&!etd.getDelayTableControl().isDrawn()){
        	if(!etd.getLevelTableControl().isEmpty()||!etd.getGammaTableControl().isEmpty()||!etd.getDecayTableControl().isEmpty()||!etd.getDelayTableControl().isEmpty()){
                out.write("Complete data for this dataset can be found in the online version\\\\\n");
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH);//line width<line width of the table-data line
                //out.write("\\end{center}\n\n");
        	}
        }
        //else if(adopted||EnsdfUtil.DSIDType(ens.DSId()).equals("decay")||ens.nComments()>0) out.write("\\end{center}\n\n");
        if(!NDSControl.isForWebDisplay){
            out.write("\\vspace{8pt}\n");
            NDSControl.incrementCurrentLineNoAndPos(1,0.423);//4pt+8pt
        }

        //System.out.println("In latexwriter 598: after top title: lineNo="+NDSControl.currentLineNo+" pos="+NDSControl.currentLinePos);

        
        //out.write("\\parbox{16cm}{\\addtolength{\\parindent}{-0.2in}");
        //write Q value record and its comments in Adopted dataset
        if(adopted){
            //debug
            //System.out.println("#in latexwriter: at Q record line: LineNo="+Control.currentLineNo+" LinePos="+Control.currentLinePos);
            linewidth_str="";           
            //if(Control.autoAdjust)
            	linewidth_str="["+NDSConfig.PARBOX_HEIGHT1+"cm]";
            	
 
            out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}");
 
            String temp="";	
            
            String qs="",dqs="",ending="";
            if(ens.qv().QBMS().length()>0){
            	qs=ens.qv().QBMS();
            	dqs=ens.qv().DQBMS();
            	if(ens.qv().QBM().isSyst) {
            		if(!ens.qv().QBM().isSystAMid)
            			ending=" (syst)";
            		else {
            			ending="";
            			qs+=" (syst)";
            		}
            	}
            	temp+="Q(\\ensuremath{\\beta^-})"+printNumber(qs,dqs,true)+ending+"; ";
            }
            
            if(ens.qv().SNS().length()>0){
            	qs=ens.qv().SNS();
            	dqs=ens.qv().DSNS();
            	if(ens.qv().SN().isSyst) {
            		if(!ens.qv().SN().isSystAMid)
            			ending=" (syst)";
            		else {
            			ending="";
            			qs+=" (syst)";
            		}
            	}
            	temp+="S(n)"+printNumber(qs,dqs,true)+ending+"; ";
            }
            if(ens.qv().SPS().length()>0){
            	qs=ens.qv().SPS();
            	dqs=ens.qv().DSPS();
            	if(ens.qv().SP().isSyst) {
            		if(!ens.qv().SP().isSystAMid)
            			ending=" (syst)";
            		else {
            			ending="";
            			qs+=" (syst)";
            		}
            	}
            	temp+="S(p)"+printNumber(qs,dqs,true)+ending+"; ";
            }
            if(ens.qv().QAS().length()>0){
            	qs=ens.qv().QAS();
            	dqs=ens.qv().DQAS();
            	if(ens.qv().QA().isSyst) {
            		if(!ens.qv().QA().isSystAMid)
            			ending=" (syst)";
            		else {
            			ending="";
            			qs+=" (syst)";
            		}
            	}
            	temp+="Q(\\ensuremath{\\alpha})"+printNumber(qs,dqs,true)+ending+"; ";
            }

            temp=temp.trim();
            if(temp.endsWith(";"))
            	temp=temp.substring(0,temp.length()-1);
            
            out.write(temp+"\\hspace{0.2in}"+Translator.process(ens.qv().QRef())+"}\\\\\n");
            NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH);//line width<line width of the table-data line
            
            
            //print additional Q record in continuation records
            if(ens.qv().extraQV().size()>0) {
            	temp="";
            	
            	String ref="";
            	for(int i=0;i<ens.qv().extraQV().size();i++) {
            		QV qentry=ens.qv().extraQV().get(i);
            		String qname=Translator.translateQName(qentry.name);
            		qs=qentry.s;
            		dqs=qentry.ds;
                    ref=qentry.ref;
            		//debug
            		//System.out.println(" i="+i+" extra Q name="+qname+" s="+qs+" ds="+dqs);
            		
            		ending="";
                    if(qs.length()>0){
                    	if(qentry.isSyst) {
                    		if(!qentry.isSystAMid)
                    			ending=" (syst)";
                    		else {
                    			ending="";
                    			qs+=" (syst)";
                    		}
                    	}
                    	
                    	temp+=qname+printNumber(qs,dqs,true)+ending+"; ";
                    }

            	}
            	
                temp=temp.trim();
                if(temp.endsWith(";"))
                	temp=temp.substring(0,temp.length()-1);
                
                if(temp.length()>0) {
                    out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}");
                    out.write(temp+"\\hspace{0.2in}"+Translator.process(ref)+"}\\\\\n");
                    
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH);//line width<line width of the table-data line
                }

            }
            
            //debug
            //System.out.println("In latexwriter line 720: after Q record line LineNo="+NDSControl.currentLineNo+" LinePos="+NDSControl.currentLinePos);
            
            //******************
            // auto or manual
            //******************
            //if(Control.autoAdjust){//automatic mode
            if(true){
                for(int i=0;i<ens.nComments();i++){
                	Comment c=ens.commentAt(i);
                    if(c.type().equals("Q ")){    
                    	//out.write("\\parbox{16cm}{\\addtolength{\\parindent}{-0.2in}"+ens.commentAt(i).getTranslated()+"}\\\\\n");
                    	//out.write("\\parbox{"+Config.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}"+ens.commentAt(i).getTranslated()+"}\\\\\n");
                    	

                    	//tempComs=TableMaker.wrapENSDFCommentTranslated(ens.commentAt(i).body(),(int)(Config.HEADNOTE_WIDTH/Config.CHAR2CM),"","",ens.commentAt(i).isBigC());
           
                        String tempHead="",head="";
                        if(c.head().length()>0){
                        	head=Translator.process(c.head())+": ";
                        	tempHead=c.head()+": ";                       	
                        }
                        
                    	tempComs=TableMaker.wrapENSDFCommentTranslated(c,NDSConfig.HEADNOTE_WIDTH,tempHead,head,c.isBigC());
                    	for(int m=0;m<tempComs.size();m++){
                    		String row=tempComs.elementAt(m);
                    		
                    		//if(row.length()==0) continue;

                    		if(m==0)
                    			out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]["+NDSConfig.PARBOX_HEIGHT1+"cm]{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}"+row+"}\\\\\n");
                    		else
                    			out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]["+NDSConfig.PARBOX_HEIGHT1+"cm]{"+NDSConfig.HEADNOTE_WIDTH+"cm}{"+row+"}\\\\\n");
                    		
                            float scale=1.0f;
                            if(Str.containSpecialSupScript(row))
                            	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE+NDSConfig.HEADNOTE_LINEWIDTH_SCALE_OFFSET;              
                            else if(Str.containSupScript(row))
                            	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                            else if(Str.containSpecialSubScript(row))
                            	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                            
                            
                    		//check if a new page is needed, if so, reset the page and write the top title
                    		NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH*scale,TopTitle,out);//line width<line width of the table-data line 
                    	}
                    }
                }
                
        	}
            /*
            else{//for manual mode using a control file
                for(int i=0;i<ens.nComments();i++){
                	Comment c=ens.commentAt(i);
                    if(c.type().equals("Q ")){          
                        String head="";
                        if(c.head().length()>0)
                        	head=Translator.process(c.head())+": ";                     	
                        
                		out.write("\\parbox["+Config.PARBOX_ALIGNMENT+"]{"+Config.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}"+head+c.getRegTranslated()+"}\\\\\n");                     	
                    }
                }
            }
            */

            //print additional Q records
            //Note that additional Q records will cause format error 
            //since only one Q record line is allowed in one Adopted dataset
            
            for(int i=0;i<ens.qv().extraQLinesV().size();i++){
            	QValue qv=new QValue();
            	Vector<String> qlines=new Vector<String>();
            	qlines.add(ens.qv().extraQLinesV().get(i));
            	qv.setValues(qlines);
            	
                out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}");
                if(qv.QBMS().length()>0) out.write("Q(\\ensuremath{\\beta^-})"+printNumber(qv.QBMS(),qv.DQBMS(),true)+"; ");
                if(qv.SNS().length()>0) out.write("S(n)"+printNumber(qv.SNS(),qv.DSNS(),true)+"; ");
                if(qv.SPS().length()>0) out.write("S(p)"+printNumber(qv.SPS(),qv.DSPS(),true)+"; ");
                if(qv.QAS().length()>0) out.write("Q(\\ensuremath{\\alpha})"+printNumber(qv.QAS(),qv.DQAS(),true));
                out.write("\\hspace{0.2in}"+Translator.process(qv.QRef())+"}\\\\\n");
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH);//line width<line width of the table-data line
            }
            
            
            out.write("\n");
        }
        
        
        linewidth_str="";    
        //if(Control.autoAdjust)
        	linewidth_str="["+NDSConfig.PARBOX_HEIGHT1+"cm]";

        
        ////////////////////////////
        //write parent record and its comments in decay data set
        ////////////////////////////
        if(EnsdfUtil.findDSIDType(ens.fullDSId()).equals("decay")){//parent record, if this is a decay dataset
            Parent p;
            String tempHead="";
            
            //write each parent record
            for(int i=0;i<ens.parentsV().size();i++){
                p=(Parent)ens.parentAt(i);
                
       
                
                //out.write("\\parbox{16cm}{\\addtolength{\\parindent}{-0.2in}Parent: $^{"+p.nucleus().A()+"}$"+p.nucleus().En()+": ");
                out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}Parent: $^{"+p.nucleus().A()+"}$"+p.nucleus().En()+": ");
                
                if(p.level().ES().length()>0) {
                	String es=printNumber(p.level().ES(),p.level().DES());
                	if(p.level().DES().trim().length()==0 || Str.isNumericUnc(p.level().DES()) || p.level().DES().equalsIgnoreCase("SY"))
                		es="="+es;
                	
                	out.write("E"+es+"; ");
                }
                if(p.level().JPiS().length()>0) {
                	String jp=p.level().JPiS().trim();
                	String js=Translator.spin(p.level().JPiS());
                	
                	if(jp.indexOf("G")==0 || jp.indexOf("L")==0 || jp.indexOf("<")==0 || jp.indexOf(">")==0){
                	}
                	else
                		js="="+js;
                	
                	out.write("J$^{\\pi}$"+js+"; ");
                }
                
                if(p.level().halflife().length()>0){
                	String ts=printNumber(p.level().T12S(),p.level().DT12S(),Translator.halfLifeUnits(p.level().T12Unit()));
                	if(p.level().DT12S().trim().length()==0 || Str.isNumericUnc(p.level().DT12S()) || p.level().DT12S().equalsIgnoreCase("SY"))
                		ts="="+ts;
                	
                	out.write("T$_{1/2}$"+ts+"; ");
                }

                String dmn=p.DMName().trim();
                String translatedDecayType=Translator.process(dmn);
                String translatedDMName=Translator.process(dmn);
                
                
                //writing QP record
                if(p.QS().length()>0 && !ens.fullDSId().contains("IT")) {
                    out.write("Q");
                                      
                    if(dmn.length()>0){                    	
                    	if(dmn.equals("P") || dmn.equals("N"))
                    		translatedDecayType=dmn.toLowerCase();
                    	
                    	out.write("("+translatedDecayType+")");
                    }else if(p.level().EF()==0)
                    	out.write("(g.s.)");
                    
                    String qs=printNumber(p.QS(),p.DQS());

                    if(p.DQS().trim().length()==0 || Str.isNumericUnc(p.DQS()) || p.DQS().equalsIgnoreCase("SY"))
                    	qs="="+qs;
                    
                    
                    out.write(qs+"; ");
                }
                
                translatedDMName=translatedDecayType;
                
                //writing N record: decay-mode
                if(dmn.length()>0){
                	/*
                    if(dmn.equals("A")){
                        out.write("\\%\\ensuremath{\\alpha}");
                    }else if(dmn.equals("B-")){
                        out.write("\\%\\ensuremath{\\beta^{-}}");
                    }
                    else if(dmn.equals("EC")){
                    	out.write("\\%\\ensuremath{\\varepsilon}+\\%\\ensuremath{\\beta^{+}}");
                    }
                    else if(dmn.equals("P")||dmn.equals("N")){
                        out.write("\\%"+dmn.toLowerCase());
                    }else{
                        out.write("\\%"+Translator.process(dmn));
                    }
                    */
                	
                	if(dmn.equals("EC")){
                		if(p.QF()>1022) {
                			translatedDecayType="\\ensuremath{\\varepsilon}+\\ensuremath{\\beta^{+}}";
                			translatedDMName="\\ensuremath{\\varepsilon}+\\%\\ensuremath{\\beta^{+}}";
                		}else {
                			translatedDecayType="\\ensuremath{\\varepsilon}";
                			translatedDMName="\\ensuremath{\\varepsilon}";
                		}
                	}
                    out.write("\\%"+translatedDMName);
                	
                    out.write(" decay");
                    
                    if(p.DM().unc().length()==0||p.DM().unc().charAt(0)<=57 || p.DM().unc().equalsIgnoreCase("SY"))
                    	out.write("=");
                    
                    //System.out.println(" p.DM()="+p.DM().value()+" "+p.DM().unc()+"  "+p.DM().txt()+"  "+ens.norm().BRS()+" br= "+ens.norm().DBRS());
                    
                    if(p.DM().value().length()>0)
                    	out.write(printNumber(p.DM().value(),p.DM().unc())+"\n\n");    
                    else
                    	out.write("?");
                }

                out.write("}\\\\\n");
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH);//line width<line width of the table-data line
            }
            
            //write comments for each parent
            for(int i=0;i<ens.parentsV().size();i++){
                p=(Parent)ens.parentAt(i);
                
                String dmn=p.DMName().trim();
                String translatedDecayType=Translator.process(dmn);
                String translatedDMName=Translator.process(dmn);

                //System.out.println("In LatexWriter line 767: i="+i+" dmn="+dmn+" ncomments="+p.nComments());
                String ID="";
                if(ens.nParents()>1)
                	ID='('+p.level().ES()+')';
                
            	if(dmn.equals("P") || dmn.equals("N")){
            		translatedDecayType=dmn.toLowerCase();
            		translatedDMName=translatedDecayType;
            	}else if(dmn.equals("EC")){
            		if(p.QF()>1022) {
            			translatedDecayType="\\ensuremath{\\varepsilon}+\\ensuremath{\\beta^{+}}";
            			translatedDMName="\\ensuremath{\\varepsilon}+\\%\\ensuremath{\\beta^{+}}";
            		}else {
            			translatedDecayType="\\ensuremath{\\varepsilon}";
            			translatedDMName="\\ensuremath{\\varepsilon}";
            		}
            	}
                
                for(int j=0;j<p.nComments();j++){
                    //out.write("\\parbox{16cm}{\\addtolength{\\parindent}{-0.2in}\\ensuremath{^{"+p.nucleus().A()+"}}"+p.nucleus().En()+"-");
                	if(!p.commentAt(j).isEmpty() || p.commentAt(j).head().length()>0)
                		out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}\\ensuremath{^{"+p.nucleus().A()+"}}"+p.nucleus().En()+ID+"-");
                	else{
                		out.write("\\vspace{"+NDSConfig.GENNERAL_COMMENT_LINEWIDTH+"cm}\n");//empty parent comment line
                		NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH);
                		continue;
                	}
                		
                    tempHead="";
                    String translatedHead="";
                    String comma="";
                    if(p.commentAt(j).head().length()>0){
                    	tempHead=p.nucleus().A()+p.nucleus().En()+"-";
                    	comma="";
                    	for(int k=0;k<p.commentAt(j).nHeads();k++){
                    		String head=p.commentAt(j).headAt(k);  
                    		if(head.isEmpty()) continue;
                    	     
                            if(head.equals("T")){
                            	translatedHead+=comma+"T$_{1/2}$";
                            	tempHead+=comma+"T1/2";
                            }else if(head.equals("J")){
                            	translatedHead+=comma+"J$^{\\pi}$";
                            	tempHead+=comma+"Jpi";
                            }else if(head.equals("QP") && p.DMName().trim().length()>0){//QP is translated as Q(g.s.) in the dictionary, but here we want Q(decay mode)
                            	if(dmn.equals("EC")) {
                                	translatedHead+=comma+"Q(\\ensuremath{\\varepsilon})";
                            	}else {
                                	translatedHead+=comma+"Q("+translatedDecayType+")";
                            	}

                            	tempHead+=comma+"Q("+p.DMName().trim()+")";
                            }else if(head.equals("BR") && p.DMName().trim().length()>0){//BR is translated as Branching in the dictionary, but here we want "%mode decay"
                            	if(p.DMName().equals("EC")){
                            		if(p.QF()>1022){
                            			translatedHead+=comma+"\\%\\ensuremath{\\varepsilon}+\\%\\ensuremath{\\beta^{+}} decay";
                            			tempHead+=comma+"%EC+%B decay";
                            		}else{
                            			translatedHead+=comma+"\\%\\ensuremath{\\varepsilon} decay";
                            			tempHead+=comma+"%E decay";
                            		}     
                            	}else{
                                	translatedHead+=comma+"\\%"+translatedDMName+" decay";
                                	tempHead+=comma+"%"+p.DMName().trim()+" decay";
                            	}

                            }                       
                            else {
                            	translatedHead+=comma+Translator.process(head);
                            	tempHead+=comma+head;
                            }
                            
                            comma=",";
                    		
                    	}
                    	
                    	if(translatedHead.length()>0){                    		
                    		translatedHead+=": ";
                    		out.write(translatedHead);
                    	}

                    }

                    //******************
                    // auto or manual
                    //******************
                    //if(Control.autoAdjust){
                    if(true){
                        tempComs=TableMaker.wrapENSDFCommentTranslated(p.commentAt(j),NDSConfig.HEADNOTE_WIDTH,tempHead,tempHead,p.commentAt(j).isBigC());
                        for(int m=0;m<tempComs.size();m++){
                        	
                        	String row=tempComs.elementAt(m);
                        	                                          	
                        	if(tempComs.elementAt(m).length()==0) continue;
                        	
                        	if(m==0)
                        		out.write(tempComs.elementAt(0).substring(tempHead.length())+"}\\\\\n");
                        	else
                        		out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]["+NDSConfig.PARBOX_HEIGHT1+"cm]{"+NDSConfig.HEADNOTE_WIDTH+"cm}{"+tempComs.elementAt(m)+"}\\\\\n");
                        	
                            float scale=1.0f;
                            if(Str.containSpecialSupScript(row))
                            	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE+NDSConfig.HEADNOTE_LINEWIDTH_SCALE_OFFSET;            
                            else if(Str.containSupScript(row))
                            	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                            else if(Str.containSpecialSubScript(row))
                            	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                            
                            NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH*scale,TopTitle,out);//line width<line width of the table-data line
                        }                    	
                    }
                    //else{
                    //	out.write(p.commentAt(j).getTranslated()+"}\n\n");
                    //}                   
                }
            }
        }

        ////////////////////////
        //write general comments of the current ENSDF data set
        ////////////////////////
        
    	//debug
    	//System.out.println("In writeBody: start writing general comments...");

        
        //******************
        // auto or manual
        //******************
        //if(Control.autoAdjust){
        if(true){
            for(int i=0;i<ens.nComments();i++){
            	Comment c=ens.commentAt(i);
                if(c.type().equals("  ")&&!c.head().contains("DICT")){            

                	//Even thought it is not recommended and not standard, some evaluators still use comment head in general comments. So
                    String tempHead="",head="";
                    if(c.head().length()>0){
                    	head=Translator.process(c.head())+": ";
                    	tempHead=c.head()+": ";                       	
                    }
                    
                    //debug
                    //System.out.println("In LatexWriter: writing general comments: beginning...");
                    //System.out.println(" comment#"+i+" of "+ens.nComments()+" comment body="+ens.commentAt(i).body());
                    //System.out.println("     translated="+ens.commentAt(i).getTranslated());
                    
                    //empty general comment, leave a blank line
                    if(ens.commentAt(i).body().trim().length()==0){
                    	out.write("\\vspace{"+NDSConfig.GENNERAL_COMMENT_LINEWIDTH+"cm}\n");
                    	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH);
                    	continue;
                    }
                    
                	//debug
                	//System.out.println("   start wrapping comments..."+ens.commentAt(i).body());
                    
                	tempComs=TableMaker.wrapENSDFCommentTranslated(ens.commentAt(i),NDSConfig.HEADNOTE_WIDTH,tempHead,head,ens.commentAt(i).isBigC());                   
                	
                	//debug
                	//System.out.println("   wrap comments OK...");
                	
                    for(int m=0;m<tempComs.size();m++){
                        
                    	String row=tempComs.elementAt(m);
                    	
                    	if(row.trim().length()==0) continue;
                    
                    	
                		//check if a new page should be started, if so, reset the new page and write the top title
                		NDSControl.incrementCurrentLineNoAndPos(0,0,TopTitle,out);
                		
                    	if(m==0)
                    		out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]["+NDSConfig.PARBOX_HEIGHT1+"cm]{"+NDSConfig.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}"+row+"}\\\\\n");
                    	else
                    		out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]["+NDSConfig.PARBOX_HEIGHT1+"cm]{"+NDSConfig.HEADNOTE_WIDTH+"cm}{"+row+"}\\\\\n");
                    	
                        //debug
                        //System.out.println("In LatexWriter 1097: writing general comments: currentLineNo="+NDSControl.currentLineNo+" pos="+NDSControl.currentLinePos);
                        //System.out.println("    Str.containSpecialSupScript(row)="+Str.containSpecialSupScript(row)+" Str.containSupScript(row)="+Str.containSupScript(row));
                        //System.out.println("      line#"+m+" of "+tempComs.size()+"    com="+tempComs.elementAt(m));
                        
                        float scale=1.0f;
                        if(Str.containSpecialSupScript(row))
                        	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE+NDSConfig.HEADNOTE_LINEWIDTH_SCALE_OFFSET;            
                        else if(Str.containSupScript(row))
                        	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                        else if(Str.containSpecialSubScript(row))
                        	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                        
            
                        NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.GENNERAL_COMMENT_LINEWIDTH*scale);//line width<line width of the table-data line
                    }

                }
            }        	
        }
        /*
        else{
            for(int i=0;i<ens.nComments();i++){
            	Comment c=ens.commentAt(i);
                if(c.type().equals("  ")&&!c.head().contains("DICT")){
                    String head="";
                    
                    //empty comment, leave a blank line
                    if(ens.commentAt(i).body().trim().length()==0){
                    	out.write("\\vspace{"+Config.GENNERAL_COMMENT_LINEWIDTH+"cm}\n");
                    	continue;
                    }
                    
                    if(c.head().length()>0)
                    	head=Translator.process(c.head())+": ";
                    
                	out.write("\\parbox["+Config.PARBOX_ALIGNMENT+"]{"+Config.HEADNOTE_WIDTH+"cm}{\\addtolength{\\parindent}{-0.2in}"+head+ens.commentAt(i).getTranslated()+"}\n\n");	
                }
            }
        }
        */
        
    	//debug
    	//System.out.println("In writeBody: write general comments OK...");
        
        out.write("\\vspace{12pt}\n");//12pt=0.423cm
        NDSControl.incrementCurrentLineNoAndPos(1,0.423);
        
        //if(adopted||EnsdfUtil.DSIDType(ens.DSId()).equals("decay")||ens.nComments()>0) out.write("\\begin{center}\n");
        //end start of dataset comments section
        


    	
        //////////////////////
        //write table records of no type (type=" "); for type="L","G",..., they re written in corresponding TableMaker 
        /////////////////////
        Vector<String> v;
        Vector<String> tableRecords=new Vector<String>();
        		
        //we only want to use the TableMaker.makeTableRecords() methd
        LevelTableMaker tm=new LevelTableMaker(etd,data);
        
        //*****************
        //get table records
        //*****************
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            
            if(t.type()!=' ') continue;
            
            v=tm.makeTableRecords(t,NDSControl.isFormatTables);//tm.nTableRecordsLines and tm.tableRecordHeight are incremented in this call
            tableRecords.addAll(v);          	           
        }
       

        
        //*******************
        //write table records
        //*******************
        int count=0;
        float height=0;
        float MAX_TEXT_HEIGHT=NDSConfig.MAX_TEXT_HEIGHT;
        
        if(!NDSControl.isPortrait)
        	MAX_TEXT_HEIGHT=NDSConfig.MAX_TEXT_WIDTH;
        
        float breakLinePos=MAX_TEXT_HEIGHT-2*NDSConfig.DEFAULT_LINEWIDTH;//leave two-line space above bottom line
        
        if(tableRecords.size()>0){
            for(int i=0;i<tableRecords.size();i++) {
  	           	
            	//NOTE: new line character "\n" (and "\\\\" in Latex) is already added at the end of each line in TableRecords 	
            	out.write(tableRecords.elementAt(i));            	
            }
           
            //if(Control.autoAdjust){
            if(true){
                
            	//NOTE: contents in TableReocrds are already formatted in Latex longtable environment, 
                //so its page break can not be handled the same way as the general comments above. 
                //Here we let Latex to handle the long table-records that are across pages automatically 
                //and make a rough estimation of the line position of the last line.
               
            	count=tm.nTableRecordsLines;
                height=tm.tableRecordsHeight;//in tableRecords, linewidth's are not all the same, so the total height is already determined separately when creating it.
          	  
                NDSControl.incrementCurrentLineNoAndPos(count,height);//line width<line width of the table-data line          	  
          	  
                float maxHeight=NDSConfig.MAX_TEXT_HEIGHT-0.2f;          	  
                if(!NDSControl.isPortrait) maxHeight=NDSConfig.MAX_TEXT_WIDTH-0.2f;
          	           	  
                float r=(NDSControl.currentLinePos/maxHeight);
         	  
                int ir=(int)r;          	  
                if(r>1)          		
                	r=r-ir;
          		       		  
                //debug      	      
                //System.out.println("In latexwriter: after TableRecords1 currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);      	      
                //System.out.println("                maxHeight="+maxHeight+" r="+r);      	    		  
      		  
                NDSControl.currentLinePos=r*maxHeight;      		  
                NDSControl.currentLineNo=NDSControl.currentLinePos/NDSConfig.TABLE_RECORDS_LINEWIDTH;      		 

                //debug      	      
                //System.out.println("In latexwriter: after TableRecords1 currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);      	      

                
                if(NDSControl.currentLinePos>=breakLinePos-3*NDSConfig.DEFAULT_LINEWIDTH){      			  
                	//if(Control.autoAdjust){          			 
                		NDSControl.setTopTitleSpacingInNewPage(TopTitle,out);	        			  
                	//}      			 
                	//else      				  
                	//	Control.setTopTitleSpacingInNewPage();
      			      	          
                	NDSControl.incrementCurrentLineNoAndPos(1);      	                 		 
                }            
            }      	      	             
            //out.write("\\vspace{-0.5cm}\n");
            //Control.incrementCurrentLineNoAndPos(0,-0.5);   
            
        }else{//when there is no table record                
        	//if(Control.autoAdjust){     
        	if(true){
        		//debug
        		//System.out.println("In LatexWriter1: currentLinepos="+Control.currentLineNo+" breakLinePos="+breakLinePos);
        		
    			if(NDSControl.currentLinePos>=breakLinePos-3*NDSConfig.DEFAULT_LINEWIDTH){      			            		
    				NDSControl.setTopTitleSpacingInNewPage(TopTitle,out);    			     				        		           	
    			}   		         	            
        	}
        }
        ///////////
        
        
        ////////////////
        //write tables
        ////////////////

    	//debug
    	//System.out.println("In writeBody: start writing data tables...");

        
        //write atomic data table
        if(!ens.atomicRad().isEmpty()) {
        	if(NDSControl.printAugerFirst) {
            	if(!ens.atomicRad().AugerMap().isEmpty())
            		writeTable(etd,"MA",out,data,adopted,TopTitle);
            	if(!ens.atomicRad().xRayMap().isEmpty())
            		writeTable(etd,"MX",out,data,adopted,TopTitle);
        	}else {
            	if(!ens.atomicRad().xRayMap().isEmpty())
            		writeTable(etd,"MX",out,data,adopted,TopTitle);
            	if(!ens.atomicRad().AugerMap().isEmpty())
            		writeTable(etd,"MA",out,data,adopted,TopTitle);
        	}

        }
        
        
        //write level table
        if(ens.nLevels()>0&&etd.getLevelTableControl().isDrawn()){//at this stage, the settings of TableControl are either by default or from control file if autoAdjust=false.
                                                                  //in auto mode, the actual settings are determined later in "findBreaks()" when making tables          
            if(sysOut) System.out.println("Writing levels");

            
            if(!etd.isCombineLG()) 
            	writeTable(etd,"L",out,data,adopted,TopTitle);
            else
            	writeTable(etd,"LG",out,data,adopted,TopTitle);   
            
        	//debug
        	//System.out.println("In writeBody: write level table OK...");
        }



        //write unplaced gamma table
        if(ens.nTotGam()>0&&etd.getUnplcTableControl().isDrawn()&&!etd.isCombineLG()){
        
            if(sysOut) System.out.println("Writing unplaced gammas");
            writeTable(etd,"U",out,data,adopted,TopTitle);        
            if(sysOut) System.out.println("Done Writing unplaced gammas");
            
        }

        //System.out.println(ens.nTotBet()+"  "+ens.nTotAlp()+" "+ens.nTotECBP()+" "+ens.nTotDP());
        
        //write decay table
        if((ens.nTotBet()>0||ens.nTotAlp()>0||ens.nTotECBP()>0||ens.nTotDP()>0)&&etd.getDecayTableControl().isDrawn()){

            if(sysOut) System.out.println("Writing decays");
            writeTable(etd,"D",out,data,adopted,TopTitle);
        }
 
        
        //write gamma table
        //System.out.println(ens.nTotGam()+"  "+etd.getGammaTableControl().isDrawn()+"  "+etd.isCombineLG());
        
        if(ens.nTotGam()>0&&etd.getGammaTableControl().isDrawn()&&!etd.isCombineLG()){
            
            if(sysOut) System.out.println("Writing gammas");
            writeTable(etd,"G",out,data,adopted,TopTitle);
            if(sysOut) System.out.println("Done Writing gammas");
            
        	//debug
        	//System.out.println("In writeBody: write gamma table OK...");

        }
        


        //write delayed-particle table
        if(ens.nTotDP()>0&&etd.getDelayTableControl().isDrawn()){

            if(sysOut) System.out.println("Writing Delayed");
            writeTable(etd,"DP",out,data,adopted,TopTitle);
            if(sysOut) System.out.println("Done Writing Delayed");
        }      
        

    	//debug
    	//System.out.println("In writeBody: write all tables OK...");
    	
        //Control.incrementCurrentLineNo(1);//Latex leaves one whitespace line after \end{center}
        
        return TopTitle;
    }
    

    
    /**Get all table panels for a table created by corresponding TableMaker*
     * Merging panels if necessary, e.g., after table splitting
     */
//    private Vector<TablePanel>() getTablePanels(){
    	
//    }
    
    /**Initializes the writing of a Table (wrap of writeTablePanel())*/
     protected void writeTable(EnsdfTableData etd, String type, java.io.Writer out,MassChain data,boolean adopted,String topTitle)throws Exception {
              
        Vector <TablePanel> tablePanels=new Vector<TablePanel>();
                
        boolean hasSet=false;
        
        boolean isPortrait=true;
        String title="";
        
        boolean autoNewPage=false,defaultNewPage=false;
        
        //boolean debugL=true;
        //boolean debugG=true;
        
        //if(Control.autoAdjust){
        if(true){
        	autoNewPage=(NDSControl.checkNewPage()); 
        	
        	//Control.isNewPage is set to be true only after end of previous landscape table, so after each use, it has to be set to default value=false
        	//Control.isNewPage=false;
        }
        
        NDSControl.numberOfAllTitleLines=0;
        NDSControl.heightOfAllTitleLines=0;

        
        if(type.equals("L")){

        	TableControl tc=etd.getLevelTableControl();           	
        	LevelTableMaker lt=new LevelTableMaker(etd,data);
        	        	    
            defaultNewPage=etd.getLevelTableControl().isNewPage();
        	//for top title (data set title) written below
            
            if(NDSControl.needToFindBreaks){
                /*
            	//debug
                if(debugL) {
                    System.out.println("\n==========================================");
                    System.out.println("In LatexWriter 1307: write Level tables beginning: currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos+" isNewPage="+tc.isNewPage());
                    System.out.println("                 Control.isNewPage="+NDSControl.isNewPage);                    
                }
                */
            

            	hasSet=false;
            	if(defaultNewPage || autoNewPage || !tc.isPortrait() || NDSControl.isNewPage) {//by default, TableControl.isNewPage=false
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		tc.setNewPage(true);
            		hasSet=true;                  		
            		
            		if(NDSControl.isNewPage)
            			NDSControl.isNewPage=false;
            		
            		//debug
            		//out.write("\n%%%%test1"+defaultNewPage+autoNewPage+tc.isPortrait()+Control.isNewPage+Control.checkNewPage()+"\n");
            		//System.out.println("In LatexWriter: "+defaultNewPage+" "+autoNewPage+" "+tc.isPortrait()+" "+Control.isNewPage+" "+Control.checkNewPage()+"\n");
            	}
        	    
        	    //for table title written below
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline
                NDSControl.addToAllTitleLines(2,NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                
            	//debug
            	//if(debugL) System.out.println("********LEVEL: starting findBreaks(): currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos+" tc.isNewPage="+tc.isNewPage());      
            	
            	lt.findBreaks();//will use currentLineNo and currentLinePos,portrait in table control is determined and set in this call            	
                
            	if(tc.isNewPage() && !hasSet){
            		NDSControl.setTopTitleSpacingInNewPage(out);
            	    //for table title written below
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline
                    NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                    
            	}
            }
        	
        	//debug
        	//if(debugL)
            //System.out.println("In LatexWriter 1430: starting makeTablePanels() (before level table title): currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos+" tc.isNewPage="+tc.isNewPage());

        	tablePanels=lt.makeTablePanels();
        	
        	isPortrait=tc.isPortrait();       	
            if(!isPortrait) out.write("\\begin{landscape}\n");           
            
            boolean isNewPage=false;
            
            //top title
            if(defaultNewPage || autoNewPage || !tc.isPortrait() || tc.isNewPage()) {//by default, TableControl.isNewPage=false
            	out.write("\\vspace*{-0.5cm}\n");
            	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");  
            	isNewPage=true;
            }
            
            //table title
            title="\\underline{$^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+" Levels}";
            
            if(topTitle.contains("Adopted"))
            	title=EnsdfUtil.makeAdoptedLevelHyperTarget(title, ens.nucleus());
            	
            out.write(title+"\\\\\n");
            
            title="\\underline{$^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+" Levels (continued)}";     
            

        	//debug
        	//if(debugL) 
            //System.out.println("In LatexWriter 1458: starting writing level tables: currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
        	
            writeTablePanels(tablePanels,out,false,isNewPage,isPortrait,topTitle,title,tc.isSmall());//topTitle is the TopTitle set in WriteBody(), which is the title of dataset (reaction name+keynumber)

            //if(debugL) 
            //System.out.println("In LatexWriter 1463: write Level Tables end: currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
        }
        else if(type.equals("LG")){
 
            TableControl tc=etd.getLevelTableControl();
            LevelTableMaker lt=new LevelTableMaker(etd,data);
        	GammaTableMaker gt=new GammaTableMaker(etd,data);  
        	
            defaultNewPage=etd.getLevelTableControl().isNewPage();
            
            if(NDSControl.needToFindBreaks){
                hasSet=false;
            	//for top title (data set title) written below
            	if(defaultNewPage || autoNewPage || !tc.isPortrait() || NDSControl.isNewPage) {//by default, TableControl.isNewPage=false
            		NDSControl.setTopTitleSpacingInNewPage(out);                               //Control.isNewPage is set true after landscape table
            		tc.setNewPage(true);
            		hasSet=true;
            		
            		if(NDSControl.isNewPage)
            			NDSControl.isNewPage=false;
            	}

                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline
                NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                
            	lt.findBreaks();//portrait in table control is determined and set in this call
            	
            	if(tc.isNewPage() && !hasSet){
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
                    NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
            	}
            }
       	
        	isPortrait=tc.isPortrait();       	
            TablePanel tp=lt.makeTablePanel(0);
            if(!tc.isPortrait(tp)) 
            	isPortrait=false;           
            tablePanels.add(tp);
            
            tp=gt.makeTablePanel(0,false);
            tablePanels.elementAt(0).Merge(tp, false);
            if(!tc.isPortrait(tablePanels.elementAt(0))) 
            	isPortrait=false;
            
            if(!isPortrait) out.write("\\begin{landscape}\n");
            
            boolean isNewPage=false;
            
            //top title
        	if(defaultNewPage || autoNewPage || !tc.isPortrait() || tc.isNewPage()) {//by default, TableControl.isNewPage=false
        		out.write("\\vspace*{-0.5cm}\n");
            	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");   
            	isNewPage=true;
            }
            
            out.write("\\underline{$^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+" Levels and "
                    +"$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+")}\\\\\n");
           
            title="\\underline{$^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+" Levels and "
                    +"$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}";
            
            writeTablePanels(tablePanels,out,true,isNewPage,isPortrait,topTitle,title,tc.isSmall());
        }
        else if(type.equals("G")){
        	
            TableControl tc=etd.getGammaTableControl();
        	GammaTableMaker gt=new GammaTableMaker(etd,data);
        	        	
            defaultNewPage=etd.getGammaTableControl().isNewPage();
             
            
            if(NDSControl.needToFindBreaks){
            
            	//debug
                //if(debugG) {
                //    System.out.println("\n===========================================================");
                //    System.out.println("In LatexWriter 1459: write Gamma Tables beginning: currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos+" isNewPage="+tc.isNewPage());                    
                //}


            	hasSet=false;
            	//for top title (data set title) written below
            	if(defaultNewPage || autoNewPage || !tc.isPortrait() || NDSControl.isNewPage){//by default, TableControl.isNewPage=false, isPortrait=true
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		tc.setNewPage(true);
            		hasSet=true;
            		
            		if(NDSControl.isNewPage)
            			NDSControl.isNewPage=false;
            	}            
                
                if(!etd.getUnplcTableControl().isDrawn()){
                    NDSControl.incrementCurrentLineNoAndPos(1,0.40);
                    NDSControl.incrementCurrentLineNoAndPos(1,0.15);//for underline   
                    NDSControl.addToAllTitleLines(2, 0.55);
                }
                else {
                    NDSControl.incrementCurrentLineNoAndPos(1);
                    NDSControl.addToAllTitleLines(1, NDSConfig.DEFAULT_LINEWIDTH);
                }           

            	//debug
            	//if(debugG) System.out.println("********GAMMA: starting findBreaks(): currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos+" Unp.isDrawn="+etd.getUnplcTableControl().isDrawn());  
            	
                
            	gt.findBreaks();//portrait in table control is determined and set in this call
            	                                       //isPortrait=false, Control.setTopTitleSpacingInNewPage() will be called in findBreaks() if tc.isNewPage=false
            	        

                
            	//debug
                //if(debugG) System.out.println("In LatexWriter: gamma table after find breaks: tc.getBreakPoints().size()="+tc.getBreakPoints().size());
                
            	if(tc.isNewPage() && !hasSet){
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		
                    if(!etd.getUnplcTableControl().isDrawn()){
                        NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                        NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline   
                        NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                    }
                    else {
                        NDSControl.incrementCurrentLineNoAndPos(1);
                        NDSControl.addToAllTitleLines(1, NDSConfig.DEFAULT_LINEWIDTH);
                    }
            	}
            }

        		
            //debug
            //System.out.println("In LatexWriter line 1405: at write gamma begining: tc.getBreakPoints().size()="+tc.getBreakPoints().size());
            //for(Break b:tc.getBreaks()) System.out.println("     Break point "+b.pos+"  gammaPos="+b.gammaPos);
            	
            
        	tablePanels=gt.makeTablePanels();
        	   	
            isPortrait=tc.isPortrait();
            if(!isPortrait) out.write("\\begin{landscape}\n");           
        	
            //top title
        	if(defaultNewPage || autoNewPage || !tc.isPortrait() || tc.isNewPage()) {//by default, TableControl.isNewPage=false
        		out.write("\\vspace*{-0.5cm}\n");
            	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");   	    
            }
            
            if(!etd.getUnplcTableControl().isDrawn()){
            	title="\\underline{$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+")}";
                if(topTitle.contains("Adopted"))
                	title=EnsdfUtil.makeAdoptedGammaHyperTarget(title, ens.nucleus());
 
            	out.write(title+"\\\\\n");    
            	title="\\underline{$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}";
            }
            else{ 
            	out.write("(Placed)\\\\\n");
            	title="(Placed) (continued)";
            }
                                     
        	//debug
        	//if(debugG) System.out.println("********GAMMA: starting makeTablePanels(): currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
            //for(int t=0;t<tablePanels.size();t++) {
        	//System.out.println("In LatexWriter line 1534: "+tablePanels.get(t).getHead());
        	//System.out.println("In LatexWriter line 1534:     "+tablePanels.get(t).getBody());
            //}
            
            writeTablePanels(tablePanels,out,false,true,isPortrait,topTitle,title,tc.isSmall());
            
            //if(debugG) System.out.println("In LatexWriter 1550: write Gamma Tables end: currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
        }
        else if(type.equals("U")){
        	
            TableControl tc=etd.getGammaTableControl();
        	GammaTableMaker gt=new GammaTableMaker(etd,data);
        	boolean isUnplaced=true;      	
        	
            defaultNewPage=etd.getUnplcTableControl().isNewPage();
            
            if(NDSControl.needToFindBreaks){
            	
            	//debug
            	//System.out.println("\n===========================================================");
            	//System.out.println("In LatexWriter: write unplaced Gamma Tables beginning: currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos+" isNewPage="+tc.isNewPage());

                hasSet=false;
            	//for top title (data set title) written below
            	if(defaultNewPage || autoNewPage || !tc.isPortrait() || tc.isNewPage() || NDSControl.isNewPage) {//by default, TableControl.isNewPage=false
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		tc.setNewPage(true);
            		hasSet=true;
            		
            		if(NDSControl.isNewPage)
            			NDSControl.isNewPage=false;
                }
                
            	//for titles written below
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);   //for underline   
                NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                
            	gt.findBreaks();//portrait in table control is determined and set in this call
            	
            	if(tc.isNewPage() && !hasSet){
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		
                	//for titles written below
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);   //for underline   
                    NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
            	}
            }
        	
        	tablePanels=gt.makeTablePanels(isUnplaced);
        	
            isPortrait=tc.isPortrait();            
            if(!isPortrait) 
            	out.write("\\begin{landscape}\n");
                     
            boolean isNewPage=false;
            
        	if(defaultNewPage || autoNewPage || !tc.isPortrait()) {//by default, TableControl.isNewPage=false
        		out.write("\\vspace*{-0.5cm}\n");
            	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");  
            	isNewPage=true;
            }
            
            out.write("\\underline{$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (Unplaced)}\\\\\n");  
                       	          
            title="\\underline{$\\gamma$($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (Unplaced) (continued)}";
            writeTablePanels(tablePanels,out,false,isNewPage,isPortrait,topTitle,title,tc.isSmall());
            
        }
        else if(type.equals("DP")){//including prompt particle decay
        	
            TableControl tc=etd.getDelayTableControl();
            DelayedTableMaker dp=new DelayedTableMaker(etd,data);
                   	
        	
            defaultNewPage=etd.getDelayTableControl().isNewPage();
            
            if(NDSControl.needToFindBreaks){
            	
            	//debug
            	//System.out.println("\n===========================================================");
            	//System.out.println("In LatexWriter: write Delay Tables beginning: currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos+" isNewPage="+tc.isNewPage());

                hasSet=false;
            	//for top title (data set title) written below
            	if(defaultNewPage || autoNewPage || !tc.isPortrait() || NDSControl.isNewPage) {//by default, TableControl.isNewPage=false
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		tc.setNewPage(true);
            		hasSet=true;
            		
            		if(NDSControl.isNewPage)
            			NDSControl.isNewPage=false;
            	}
            	
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
                NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                
                dp.findBreaks();//portrait in table control is determined and set in this call 
                
            	if(tc.isNewPage() && !hasSet){
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline   
                    NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
            	}
            }
            
            tablePanels=dp.makeTablePanels();   
            
            isPortrait=tc.isPortrait();           
            if(!isPortrait) out.write("\\begin{landscape}\n");
                 
            boolean isNewPage=false;
            //top title
        	if(defaultNewPage || autoNewPage || !tc.isPortrait() || tc.isNewPage()) {//by default, TableControl.isNewPage=false
        		out.write("\\vspace*{-0.5cm}\n");
            	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");  
            	isNewPage=true;
            }
            
        	String particles="Particles";
        	String delayedS="Delayed ";
        	if(etd.getENSDF().promptOrDelayMarker().equals(" "))//prompt particle decay
        		delayedS="";
        	
            if(etd.getENSDF().particleType().equalsIgnoreCase("P")) particles="Protons";
            if(etd.getENSDF().particleType().equalsIgnoreCase("N")) particles="Neutrons";
            if(etd.getENSDF().particleType().equalsIgnoreCase("A")) particles="Alphas";
            if(etd.getENSDF().particleType().equalsIgnoreCase("T")) particles="Tritons";
            
            out.write("\\underline{"+delayedS+particles+" ($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+")}\\\\\n");         
                                      
            title="\\underline{"+delayedS+particles+" ($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}";
            writeTablePanels(tablePanels,out,false,isNewPage,isPortrait,topTitle,title,tc.isSmall());
        }
        else if(type.equals("D")){
            String decayTitle="";
            TableControl tc=etd.getDecayTableControl();          
            
            
            DecayTableMaker dt=new DecayTableMaker(etd,data);
                    	
            defaultNewPage=etd.getDecayTableControl().isNewPage();
            
            if(NDSControl.needToFindBreaks){
            	//debug
            	//System.out.println("\n===========================================================");
            	//System.out.println("In LatexWriter: write Decay Tables beginning (before table title): currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos+" isNewPage="+tc.isNewPage());

            	hasSet=false;
            	//for top title (data set title) written below
            	if(defaultNewPage || autoNewPage || !tc.isPortrait() || NDSControl.isNewPage) {//by default, TableControl.isNewPage=false
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		tc.setNewPage(true);
            		hasSet=true;
            		
            		if(NDSControl.isNewPage)
            			NDSControl.isNewPage=false;
            	}
            	
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
                NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                
                
                dt.findBreaks();//portrait in table control is determined and set in this call 
                
            	if(tc.isNewPage() && !hasSet){
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
                    NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
            	}
            }


            tablePanels=dt.makeTablePanels();
 
            isPortrait=tc.isPortrait();
            if(!isPortrait) out.write("\\begin{landscape}\n");
                    
            /*
            if(ens.DSId().contains("B-")) 
            	decayTitle="\\ensuremath{\\beta^-} radiations";
            else if(ens.DSId().contains("B+")) 
            	decayTitle="\\ensuremath{\\epsilon,\\beta^+} radiations";
            else if(ens.DSId().contains(" A ")) 
            	decayTitle="\\ensuremath{\\alpha} radiations";
            else if(ens.DSId().contains("EC")&&ens.parentAt(0).QF()>1022) 
            	decayTitle="\\ensuremath{\\epsilon,\\beta^+} radiations";
            else 
            	decayTitle="\\ensuremath{\\epsilon} radiations";
            */
            
            if(ens.decayRecordType().equals("B")) 
            	decayTitle="\\ensuremath{\\beta^-} radiations";
            else if(ens.fullDSId().contains(" B+ ") && ens.nTotECBP()>0) 
            	decayTitle="\\ensuremath{\\varepsilon,\\beta^+} radiations";
            else if(ens.decayRecordType().equals("A")) 
            	decayTitle="\\ensuremath{\\alpha} radiations";
            else if(ens.decayRecordType().equals("E") && ens.parentAt(0).QF()>1022) 
            	decayTitle="\\ensuremath{\\varepsilon,\\beta^+} radiations";
            else 
            	decayTitle="\\ensuremath{\\varepsilon} radiations";
            
            boolean isNewPage=false;
            
            //top title
        	if(defaultNewPage || autoNewPage || !tc.isPortrait() || tc.isNewPage()) {//by default, TableControl.isNewPage=false
        		out.write("\\vspace*{-0.5cm}\n");
            	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");   
            	isNewPage=true;
            }
            
            out.write("\n\\underline{"+decayTitle+"}\\\\\n");           
                        
            title="\n\\underline{"+decayTitle+" (continued)}";
            
        	//debug
        	//if(Control.autoAdjust) System.out.println("********DECAY: starting makeTablePanels(): currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);

            writeTablePanels(tablePanels,out,false,isNewPage,isPortrait,topTitle,title,tc.isSmall());

            //if(Control.autoAdjust) System.out.println("In LatexWriter: write Decay Tables end: currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
        }else if(type.equals("MX") || type.equals("MA")) {//for atomic x ray and Auger electrons
        	
            TableControl tc;
         	AtomicTableMaker at=null;
         	if(type.equals("MX")) {
         		at=new AtomicTableMaker(etd,data,"X");
         		tc=etd.getXRayTableControl();
         	}else {
         		at=new AtomicTableMaker(etd,data,"A");
         		tc=etd.getAugerTableControl();
         	}
         	        	
             defaultNewPage=etd.getXRayTableControl().isNewPage();
                    
             if(NDSControl.needToFindBreaks){
             
             	//debug
             	//System.out.println("\n===========================================================");
             	//System.out.println("In LatexWriter: write Atomic Tables beginning: currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos+" isNewPage="+tc.isNewPage());

             	hasSet=false;
             	//for top title (data set title) written below
             	if(defaultNewPage || autoNewPage || !tc.isPortrait() || NDSControl.isNewPage){//by default, TableControl.isNewPage=false, isPortrait=true
             		NDSControl.setTopTitleSpacingInNewPage(out);
             		tc.setNewPage(true);
             		hasSet=true;
             		
             		if(NDSControl.isNewPage)
             			NDSControl.isNewPage=false;
             	}            
                 
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
                NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
                
                at.findBreaks();//portrait in table control is determined and set in this call 
                
            	if(tc.isNewPage() && !hasSet){
            		NDSControl.setTopTitleSpacingInNewPage(out);
            		
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline   
                    NDSControl.addToAllTitleLines(2, NDSConfig.TABLE_TITLE_TOTAL_LINEWIDTH);
            	}               

             }

        	tablePanels=at.makeTablePanels();
        	
            isPortrait=tc.isPortrait();            
            if(!isPortrait) 
            	out.write("\\begin{landscape}\n");
                     
            boolean isNewPage=false;
            
        	if(defaultNewPage || autoNewPage || !tc.isPortrait()) {//by default, TableControl.isNewPage=false
        		out.write("\\vspace*{-0.5cm}\n");
            	out.write(topTitle+"\\\\" +"\n\\vspace{0.3cm}\n");  
            	isNewPage=true;
            }
            
        	if(type.equals("MX"))
        		out.write("\\underline{X rays ($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+")}\\\\\n");  
        	else
        		out.write("\\underline{Auger electrons ($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+")}\\\\\n");  
        	
        	if(type.equals("MX"))
        		title="\\underline{X rays($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}";
        	else
        		title="\\underline{Auger electrons ($^{"+ens.nucleus().A()+"}$"+ens.nucleus().En()+") (continued)}";
        	                                            
         	//debug
         	//if(Control.autoAdjust) System.out.println("********ATOMIC: starting makeTablePanels(): currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
         	
        	writeTablePanels(tablePanels,out,false,isNewPage,isPortrait,topTitle,title,tc.isSmall());
             
             //if(Control.autoAdjust) System.out.println("In LatexWriter: write Atomic Tables end: currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
        }
    }
     
     
     /**Prints a table that has been divided into n panels (sub-tables) by break points.
      * General and footnote comments are stored in the first panel.
      * NOTE: since the table has been divided based on break points including peak breaks,
      * each panel stays in the same page in auto mode.
      * */
  @SuppressWarnings("unused")
protected void writeTablePanels(Vector <TablePanel> tablePanels, java.io.Writer out, boolean combine,boolean isNewPage,boolean isPortrait,String topTitle,
		  String tableTitle,boolean smallTable)throws Exception{
      
	  int temp_count=0;
	  int count=0;
	  float height=0;
	  float MAX_TEXT_HEIGHT=NDSConfig.MAX_TEXT_HEIGHT;
	  float MAX_TEXT_WIDTH =NDSConfig.MAX_TEXT_WIDTH;
	  float HEADNOTE_WIDTH=NDSConfig.HEADNOTE_WIDTH;
	  float FOOTNOTE_WIDTH=NDSConfig.FOOTNOTE_WIDTH;

	  if(isNewPage || !isPortrait)
		  NDSControl.numberOfPages+=1;

	  if(!isPortrait){
		  MAX_TEXT_HEIGHT=NDSConfig.MAX_TEXT_WIDTH;
		  MAX_TEXT_WIDTH =NDSConfig.MAX_TEXT_HEIGHT;
		  HEADNOTE_WIDTH=(NDSConfig.MAX_TEXT_HEIGHT/NDSConfig.MAX_TEXT_WIDTH)*NDSConfig.HEADNOTE_WIDTH;
		  FOOTNOTE_WIDTH=(NDSConfig.MAX_TEXT_HEIGHT/NDSConfig.MAX_TEXT_WIDTH)*NDSConfig.FOOTNOTE_WIDTH;
	  }
	  
	  float breakLinePos=MAX_TEXT_HEIGHT-0.2f-NDSConfig.BOTTOM_GAP_NLINES*NDSConfig.DEFAULT_LINEWIDTH;//keep consistent with setting in findBreaks() in TableMaker.java
		
	  
      //debug
	  //if(NDSControl.autoAdjust){
	  //    System.out.println("\n*************writing table panels**************");
	  //    System.out.println("In LatexWriter 1978 writeTablePanels: at beginning (after table title): currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos+" isPortrait="+isPortrait);	
	  //    System.out.println("       maxTextHeight="+MAX_TEXT_HEIGHT+"  breakLinePos="+breakLinePos+" Control.breakLinePos="+NDSControl.breakLinePos);
	  //}
      
      
	  //write general comments to be printed out on top of table body
	  String linewidth_str="";
	  //if(Control.autoAdjust)
		  linewidth_str="["+NDSConfig.PARBOX_HEIGHT1+"cm]";
	  
      //if(tablePanels.elementAt(0).getGenComments().size()+tablePanels.elementAt(0).getTableRecords().size()>0){ 
      //	  Control.incrementCurrentLineNoAndPos(1);//LaTex leaves one blank line before and after center environment
      //	  out.write("\\vspace{"+Config.DEFAULT_LINEWIDTH+"cm}\n");
      //}
      
	
      NDSControl.hasSetNewPageForTopTitle=false;
      if(tablePanels.elementAt(0).getGenComments().size()>0){
            NDSControl.incrementCurrentLineNoAndPos(1);//LaTex leaves one blank line after table tile and before comments
            out.write("\\vspace{"+NDSConfig.DEFAULT_LINEWIDTH+"cm}\n");
    	  
            for(int i=0;i<tablePanels.elementAt(0).getGenComments().size();i++) {
            	String row=tablePanels.elementAt(0).getGenComments().elementAt(i);
            	
            	//check if a new page should be started, if so, reset the new page and write the top title (data set title) and table title
            	//Note that setTopTitleSpacingInNewPage() is called in this function, meaning that Control.hasSetNewPageForTopTitle could be 
            	//set to be true if comments are long
            	NDSControl.incrementCurrentLineNoAndPos(0,0,topTitle,tableTitle,out);
            	
            	out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+HEADNOTE_WIDTH+"cm}{"+row+"}\\\\\n");
            	//count=tablePanels.elementAt(0).getGenComments().elementAt(i).length()/Config.MAX_LINE_LENGTH+1;
            	
            	count=1;//each item here occupies only one line, since the long comments have been split in corresponding TableMaker.
            	
                float scale=1.0f;
                if(Str.containSpecialSupScript(row))
                	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE+NDSConfig.HEADNOTE_LINEWIDTH_SCALE_OFFSET;            
                else if(Str.containSupScript(row))
                	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                else if(Str.containSpecialSubScript(row))
                	scale=NDSConfig.HEADNOTE_LINEWIDTH_SCALE;
                
            	NDSControl.incrementCurrentLineNoAndPos(count,count*NDSConfig.GENNERAL_COMMENT_LINEWIDTH*scale);
            	
                //debug
            	//System.out.println("In latexwriter: after table general comments currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
                //System.out.println("A#in latexwriter:"+Control.currentLineNo+" "+tablePanels.elementAt(0).getGenComments().elementAt(i));
        		//  out.write("i="+i+" LatexWriter line 1940 *********************test\\\\\n");
            }
            
            out.write("\\vspace{"+NDSConfig.DEFAULT_LINEWIDTH+"cm}\n");
            NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.DEFAULT_LINEWIDTH);//leave one blank line after all comments
            

  		  
            //out.write("\\vspace{"+0.5f+"cm}\n");
            //Control.incrementCurrentLineNoAndPos(1,0.5f);//leave one blank line after all comments (keep consistent with settings in findBreaks() in TableMaker and each individual table maker)
            
            //debug
        	//System.out.println("In latexwriter 1927: after table general comments currentLinePos="+NDSControl.currentLinePos+" breakLinePos="+breakLinePos);
        	
            //out.write("\\vspace{-0.5cm}\n");
      }
      
      
      boolean hasSetNewPage=false;
      
      //write table records
      if(tablePanels.elementAt(0).getTableRecords().size()>0){
          for(int i=0;i<tablePanels.elementAt(0).getTableRecords().size();i++) {
        	           	
          	//NOTE: new line character "\n" (and "\\\\" in Latex) is already added at the end of each line in TableRecords 	
          	out.write(tablePanels.elementAt(0).getTableRecords().elementAt(i));
          	
          }

          //if(Control.autoAdjust){
          if(true){	  
              //NOTE: contents in TableReocrds are already formatted in Latex longtable environment, 
              //so its page break can not be handled the same way as the general comments above. 
              //Here we let Latex to handle the long table-records that are across pages automatically 
              //and make a rough estimation of the line position of the last line.
              count=tablePanels.elementAt(0).getNTableRecordsLines();
              height=tablePanels.elementAt(0).getTableRecordsHeight();//in tableRecords, linewidth's are not all the same, so the total height is already determined separately when creating it.
        	  NDSControl.incrementCurrentLineNoAndPos(count,height);//line width<line width of the table-data line
        	  
        	  float maxHeight=NDSConfig.MAX_TEXT_HEIGHT-0.2f;
        	  if(!isPortrait) maxHeight=NDSConfig.MAX_TEXT_WIDTH-0.2f;
        	  
        	  float r=(NDSControl.currentLinePos/maxHeight);
        	  int n=(int)r;
        	  if(r>1)
        		  r=r-n;
        		  
    		  //debug
    	      //System.out.println("In latexwriter: after TableRecords1 currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
    	      //System.out.println("                maxHeight="+maxHeight+" r="+r+" height="+height+" line count="+count);
    	    		  
        	  //keep consistent with settings in findBreaks() in TableMaker.java
    		  NDSControl.currentLinePos=r*maxHeight+NDSConfig.BOTTOM_GAP_NLINES*NDSConfig.DEFAULT_LINEWIDTH;//note that currentLinePos is for the top of the currentLine
    		  NDSControl.currentLineNo=NDSControl.currentLinePos/NDSConfig.TABLE_RECORDS_LINEWIDTH+NDSConfig.BOTTOM_GAP_NLINES;
    		  
    		  //float headLinePos=tablePanels.elementAt(0).getRelHeadLinePos()+Control.currentLinePos;	  
    		  
    	      //System.out.println("In latexwriter: after TableRecords2 currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos);
    	      //System.out.println("                breakLinePos="+breakLinePos);
    	      
    		  //here currentLinePos points to the top of the headLine
    		  if(NDSControl.currentLinePos>=breakLinePos-2*NDSConfig.DEFAULT_LINEWIDTH ){
    		  //if(headLinePos>=Control.breakLinePos-2*Config.DEFAULT_LINEWIDTH){
    			  //if(Control.autoAdjust){
    			  if(true){
    		        	if(isPortrait){
    		                out.write("\\begin{textblock}{29}(0,27.3)\n");//cm        
    		            	out.write("Continued on next page (footnotes at end of table)\n");
    		                out.write("\\end{textblock}\n");            		
    		        	}
        			  NDSControl.setTopTitleSpacingInNewPage(topTitle,out);
        			  hasSetNewPage=true;
        			  
      				  //table title 				
        			  out.write(tableTitle+"\\\\\n");
      				  out.write("\\vspace{0.1in}\n");		  
    			  }
    			  //else
    			  //	  Control.setTopTitleSpacingInNewPage();
    			
    	          NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
    	          if(tableTitle.contains("underline"))
    	        	  NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
    		  }
          }

    	      	  
          out.write("\\vspace{-0.5cm}\n");
          //Control.incrementCurrentLineNoAndPos(0,-0.5);
          
      }
            
      //if(Control.autoAdjust){
      if(true){
    	  
		  //debug
	      //System.out.println("In latexwriter 2021: after TableRecords currentLineNo="+Control.currentLineNo+" "+Control.currentLinePos+" breakLinePos="+breakLinePos+" Control.breakPos="+Control.breakLinePos);

    	  
          //when there are no table records
          if(tablePanels.elementAt(0).getTableRecords().size()==0){
        	  
              //check if there is still enough space to hold table heading and at least two table lines plus title line
    		  if(NDSControl.currentLinePos>=breakLinePos-3*NDSConfig.DEFAULT_LINEWIDTH){//keep consistent with setting in findBreaks() in TableMaker.java 
    			                                                                  //where headLinePos>=Control.breakLinePos-2*Config.DEFAULT_LINEWIDTH and headLinePos there points to
    			                                                                  //the bottom of the headLine
    			                                                                  //here currentLinePos is the position after writing general comments and/or table record
    			                                                                  //and points to the top of the headLine

    	        	if(isPortrait){
    	                out.write("\\begin{textblock}{29}(0,27.3)\n");//cm        
    	            	out.write("Continued on next page (footnotes at end of table)\n");
    	                out.write("\\end{textblock}\n");            		
    	        	}
    	        	
    			  NDSControl.setTopTitleSpacingInNewPage(topTitle,out);
    			  hasSetNewPage=true;
    			  
  				  //table title 				
    			  out.write(tableTitle+"\\\\\n");
    			
    	          NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
    	          if(tableTitle.contains("underline"))
    	        	  NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
    		  }
          }
      }
      
      //ensure the table body is moved to new page (in cases it has to) if somehow it has not been done above.
      int size=tablePanels.elementAt(0).getGenComments().size()+tablePanels.elementAt(0).getTableRecords().size();
      
      if(size>0 && !hasSetNewPage && tablePanels.elementAt(0).isBodyInNewPage()){ 
    	  
        	if(isPortrait){
                out.write("\\begin{textblock}{29}(0,27.3)\n");//cm        
            	out.write("Continued on next page (footnotes at end of table)\n");
                out.write("\\end{textblock}\n");            		
        	}
        	
        	
		  NDSControl.setTopTitleSpacingInNewPage(topTitle,out);
		  hasSetNewPage=true;
		  
		  //out.write("line LatexWriter 2077*********************test\\\\\n");
			  //table title 				
		  out.write(tableTitle+"\\\\\n");
		
          NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
          if(tableTitle.contains("underline"))
        	  NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline  
          

    	  //Control.incrementCurrentLineNoAndPos(1);//LaTex leaves one blank line before and after center environment
    	  //out.write("\\begin{enter}\n");
      }
      
      //if(tablePanels.elementAt(0).getGenComments().size()+tablePanels.elementAt(0).getTableRecords().size()==0)
      //	  out.write("\\vspace{-0.3cm}\n");
  
      temp_count=0;
      for(int j=0;j<tablePanels.size();j++){
    	  
    	    //each table leaves one-line space before it
    	    //For the first panel, it it the gap between the top of the headline and whatever line before the headline.
    	    //For panels after, since top title and table title are included as heads of the table panel, the one-line
    	    //space before the table panel is the gap between page top line and the top title and should be the same
    	    //as the setting in setTopTitleSpacingInNewPage
    	  
    	    if(j>0)
    	    	NDSControl.incrementCurrentLineNoAndPos(1,0.3);
    	    else
    	    	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TOP_MARGIN);
    	    
            //debug
            //if(NDSControl.autoAdjust) System.out.println("\nIn Latexwriter 2197: writeTablePanels for loops (before head): panelNo="+j+" currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
    	                
            //out.write("\\begin{longtable}[l]{"); //left align the table, using page margin
            out.write("\\begin{longtable}{"); //when LTleft and LTright are set, left align the table, LTleft and LTright as margin relative to page margin
            
            Vector<String> columns=tablePanels.elementAt(j).getColumns();
            int nColumns=tablePanels.elementAt(j).getNColumns();
            int n=nColumns+1;
            for(int i=0;i<nColumns;i++){
                if(tablePanels.elementAt(j).getSplit().contains(i))
                	out.write("|");
                
                String colName=columns.elementAt(i);
                
                //System.out.println("LatexWritter 2243: table="+tableTitle+" panel No="+j+" colName="+colName+" nColumns="+nColumns);
                
                if(colName.equals("JF"))
                	out.write("@{}c");//squeeze gap between EF and JF
                else
                	out.write("c");
            }
            
            out.write("@{\\extracolsep{\\fill}}c");//an invisible column added to fill up the rest of the width
            
            out.write("}\n");
            
            if(j>0)
            	out.write("\\\\[-.4cm]\n");//before TopTile. For the first panel, this is set in writeBody()
          
            //text before \endfirsthead=head of the long table in the first page (firsthead=headings only here)
            if(j==0){
                for(int i=0;i<tablePanels.elementAt(j).getFirstHead().size();i++){
                	n=nColumns+1;
                	String line=tablePanels.elementAt(j).getFirstHead().elementAt(i).toString();
                	String s1="\\multicolumn{"+nColumns+"}";
                	String s2="\\multicolumn{"+n+"}";
                	String newLine=line.replace(s1, s2);//add an invisible columns to fill up the rest width
                	if(newLine.equals(line) || nColumns<=2)//nColumns=2, eg, E(level)+dE, actually only one columns
                		newLine=line+"&";
                    out.write(newLine+"\\\\");
                    
                    NDSControl.incrementCurrentLineNoAndPos(1);//one heading line in firstHead
                    if(i<tablePanels.elementAt(j).getFirstHead().size()-1)
                    	out.write("\n");
                }
                    
                out.write("[-.2cm]\n");
                NDSControl.incrementCurrentLineNoAndPos(1,0.1);//gap between column title line and first table line
                
                //debug
                //if(NDSControl.autoAdjust) System.out.println("in latexwriter 2244 after firsthead:"+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
                
                for(int i=0;i<tablePanels.elementAt(j).getNColumns();i++){
                    if(tablePanels.elementAt(j).getMulticols().contains(i)){
                        if(tablePanels.elementAt(j).getSplit().contains(i+2))
                        	out.write("\\multicolumn{2}{c|}{\\hrulefill}");
                        else
                        	out.write("\\multicolumn{2}{c}{\\hrulefill}");
                        i++;
                    }else 
                    	out.write("\\hrulefill");
                    if(i<tablePanels.elementAt(j).getNColumns()-1)out.write("&");
                }
                //out.write("\\hrulefill");
                
                out.write("&");//add an invisible columns to fill up the rest width
                
                out.write("\n\\endfirsthead\n");
            }
            
            //if(j>0 || (!Control.autoAdjust && !smallTable)){ 
            if(j>0){ 
        		//Title here is the TopTitle set in WriteBody(), which is the title of dataset (reaction name+keynumber)
            	//Control.incrementCurrentLineNoAndPos(1);//the blank line above toptitle
            
            	n=nColumns+1;//add an invisible columns to fill up the rest width
            	
        		out.write("\\multicolumn{"+n+"}{c}{"+topTitle+"}\\\\\n");            
                out.write("\\multicolumn{"+n+"}{c}{~}\\\\\n");
                
                 
        	    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TOP_TITLE_LINEWIDTH);//dataset top title line=0.430 cm
        	    
        	    //if(NDSControl.autoAdjust) System.out.println("in latexwriter 2277 after head toptitle:"+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
        	    
        	    //top-title underline plus one blank line after dataset top title
        	    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline=0.20 cm
        	    
        	    //if(NDSControl.autoAdjust) System.out.println("in latexwriter 2282 after head underline:"+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
        	    
        	    NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.DEFAULT_LINEWIDTH);    //one blank line after dataset top title=0.34 cm 
                //Control.incrementCurrentLineNoAndPos(1.2f);//top-title underline plus one blank line after dataset top title 
            
        	    //if(NDSControl.autoAdjust) System.out.println("in latexwriter 2287 after head blank line:"+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
        	    
                //when the panel occupies one page and only head is set but firsthead is not set, head will be used; if firsthead is also
                //set, firsthead will be used.
                //so in auto mode (one panel one page), for panelNo>0, head is used so only endhead should be set (DO NOT SET FIRSTHEAD!)
                //in manual mode, if breaks are set in control file, it is also one-panel-per-page, same as above;
                //when breaks are not given in control file, panel could be across pages, so both head and firsthead should be set.
                //footnotes are written separately not by \endlastfoot
                
                //text before \endhead=head of the long table in the continued page (title(continued)+headings)
                //since each panel stays in the same page (not cross the pagebreak) in auto mode, no need to set both firsthead 
                //and head (auto mode). The reason not to do that is that, when the body width of a split narrow table is smaller than the
                //the width of the head which is actually not used, the column widths and alignments of the output Latex table
                //will be determined based on the head instead of the body.
                //NOTE: head here (for panelNo>0) also includes the title of the panel (not included in the first panel)

            	for(int i=0;i<tablePanels.elementAt(j).getHead().size();i++){
            		//heads include table title+column title line+blank line in-between for panel>0
       
                	String line=tablePanels.elementAt(j).getHead().elementAt(i).toString();
                	String s1="\\multicolumn{"+nColumns+"}";
                	String s2="\\multicolumn{"+n+"}";
                	String newLine=line;
                	
                	if(line.contains("underline") || line.contains("~"))
                		newLine=line.replace(s1, s2);//add an invisible columns to fill up the rest width
                	
                	if(newLine.equals(line))
                		newLine=line+"&";
                    out.write(newLine+"\\\\");
     
                    if(line.contains("{~}") || line.contains("~~"))
                    	NDSControl.incrementCurrentLineNoAndPos(1);//blank line
                    else
                    	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);//table title and column title
                    
                    if(line.contains("underline"))
                    	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);
                    
                    if(i<tablePanels.elementAt(j).getHead().size()-1)
                    	out.write("\n");
                    
                    //if(NDSControl.autoAdjust) System.out.println("in latexwriter 2324 after top title: head line: "+line+"  "+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
            	}
            	

        	    //NDSControl.incrementCurrentLineNo(1, Config.TABLE_TITLE_LINEWIDTH);     	    
                //NDSControl.incrementCurrentLineNo(1, Config.UNDERLINE_WIDTH);              
                //NDSControl.incrementCurrentLineNo(1, Config.DEFAULT_LINEWIDTH);//blank line after table title
            	
                //if(NDSControl.autoAdjust) System.out.println("in latexwriter 2336 after column line:"+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
                
                //NDSControl.incrementCurrentLineNoAndPos(1);//column title line, already included in the for loop above
             
                NDSControl.incrementCurrentLineNoAndPos(1,0.1);//under line and gap between column title line and first table line
            
                out.write("[-.2cm]\n");
                for(int i=0;i<tablePanels.elementAt(j).getNColumns();i++){
                   if(tablePanels.elementAt(j).getMulticols().contains(i)){
                       if(tablePanels.elementAt(j).getSplit().contains(i+2))
                    	   out.write("\\multicolumn{2}{c|}{\\hrulefill}");
                       else
                    	   out.write("\\multicolumn{2}{c}{\\hrulefill}");
                    
                       i++;
                   }else 
                	   out.write("\\hrulefill");
                   if(i<tablePanels.elementAt(j).getNColumns()-1)out.write("&");
                }
                //out.write("\\hrulefill");
                
                out.write("&");//add an invisible columns to fill up the rest width
                
                out.write("\n\\endhead\n");
            }
            
            //debug
            //if(NDSControl.autoAdjust) System.out.println("In LatexWriter 2347: after endhead: lineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
            
            
            //for manual mode only; handled differently later for auto mode 
            //set for following cases:
            // 1, panelNo<tablePanels.size-1 when tablePanels.size>1 
            // 2, panelNo=0 when tablePanels.size=1 and panel is across pages
            //                    
            //small table = a table that is not across pages
        	//when panelNo>0 (auto and manual modes), which means table is split and it is a long table, tc.isSmall=false (default value)
            //tc.isSmall=true is set in findBreaks in auto mode for a table not across page (breaks.size=0)
            
            //There is only one condition under which that auto page-break by LaTex long-table environment will happen, that is,
            //a table panel is across pages but no break points have been set in the program (only in manual mode; in auto mode, all 
            //table panels occupy one page). For this case, page-break will be taken care of by LaTex and endfoot should be set.
            if(!NDSControl.autoAdjust && !smallTable && tablePanels.size()==1) {
            //if(!smallTable && j<tablePanels.size()-1 && !Control.autoAdjust){
            	n=nColumns+1;
                out.write("\\multicolumn{"+n+"}{c}{~}\\\\\n"); //add an invisible columns to fill up the rest width
                out.write("\\multicolumn{"+n+"}{c}{Continued on next page (footnotes at end of table)} \\endfoot\n");
            }
            
            //for last panel, use lastfoot="" to override foot="Continued ..." above 
            if(!NDSControl.autoAdjust && j==tablePanels.size()-1) out.write(" \\endlastfoot\n");
            
            if(combine&&j==0){
                for(int i=0;i<tablePanels.elementAt(0).getLastTail().size();i++) {
                	out.write(tablePanels.elementAt(j).getLastTail().elementAt(i)+"\\\\\n");
                	//debug
                	//System.out.println("In LatexWriter: footnote="+tablePanels.elementAt(j).getLastTail().elementAt(i));
                	
                	temp_count+=tablePanels.elementAt(j).getLastTail().elementAt(i).length()/NDSConfig.MAX_LINE_LENGTH+1;
                }
                out.write(" \n\\endlastfoot\n");
            }
              
            //if(j==5)
            //    System.out.println("LatexWriter 2441: ########## body="+tablePanels.elementAt(j).getBody());
            
            //write table body
            for(int i=0;i<tablePanels.elementAt(j).getBody().size();i++){
                out.write(tablePanels.elementAt(j).getBody().elementAt(i).toString()+"&\\\\\n"); //add "&" for an invisible columns to fill up the rest width
                
                
        		//debug
                //System.out.println("***Debug: writing "+tableTitle+" of panel="+j+" : "+tablePanels.elementAt(j).getBody().elementAt(i).toString());
                
                //System.out.println("In LatexWriter 2448: write Gamma line: currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos+" line="+tablePanels.elementAt(j).getBody().elementAt(i));
                
                //Control.incrementLineCount(1);//assuming one body-line occupy one line in PDF output. But line comments will occupy multiple lines
            }
            
            //if(Control.autoAdjust){
            if(true){
                count=tablePanels.elementAt(j).getNTableBodyLines();
                height=tablePanels.elementAt(j).getTableBodyHeight();//in tableBody, linewidth's are not all the same, so the total height is already determined separately when creating it.
                
                //Control.incrementCurrentLineNoAndPos(tablePanels.elementAt(j).getNTableBodyLines());//not including heading and title
                NDSControl.incrementCurrentLineNoAndPos(count, height);
                
                //debug
                //System.out.println("In LatexWriter 2422: after write body: currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
                //System.out.println("                nTableBodyLines(exclude heading and title)="+tablePanels.elementAt(j).getNTableBodyLines());
                //System.out.println("                TableBodyHeight(exclude heading and title)="+tablePanels.elementAt(j).getTableBodyHeight());            	
            }

            
            out.write("\\end{longtable}\n");
            NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_BOTTOM_MARGIN);//each table leaves one-line space after it

            if(j<tablePanels.size()-1) {
            //if(isNewPage&&j<tablePanels.size()-1) {//only for size>1, added at the end of each panel excluding the last one         
            	if(isPortrait){
                    out.write("\\begin{textblock}{29}(0,27.3)\n");//cm        
                	out.write("Continued on next page (footnotes at end of table)\n");
                    out.write("\\end{textblock}\n");            		
            	}
         
            	out.write("\\clearpage\n");
            	
            	NDSControl.setDefaultHeading(out);//for web-display only; wirte nothing for others
            	
            	NDSControl.currentLineNo=0;
            	NDSControl.currentLinePos=0;
            	
            	NDSControl.numberOfPages+=1;
            }
      }
        
      if(!combine){
          
          linewidth_str="";
          //if(Control.autoAdjust)
        	  linewidth_str="["+NDSConfig.PARBOX_HEIGHT2+"cm]";
          
          int fsize=tablePanels.lastElement().getLastTail().size();
          
          if(fsize>0){
        	  	  
              //debug
              //if(NDSControl.autoAdjust) System.out.println("In LatexWriter 2461: write footnotes: begining: currentLineNo="+NDSControl.currentLineNo+" "+NDSControl.currentLinePos);
              
        	  for(int i=0;i<fsize;i++){                
        		  //if(Control.autoAdjust){   
        		  if(true){	  
        			//if only one line left in next panel, print that line in current panel  
        			  if(NDSControl.currentLinePos>=MAX_TEXT_HEIGHT-3*NDSConfig.DEFAULT_LINEWIDTH-0.2 && i<fsize-1){
        			  //if(NDSControl.currentLinePos>breakLinePos && i<fsize-1){		  
        				  if(isPortrait){
        					  out.write("\\begin{textblock}{29}(0,27.3)\n");//cm        
                          	  out.write("Continued on next page (footnotes at end of table)\n");
                              out.write("\\end{textblock}\n");        					  
        				  }

                                                 	   
                          out.write("\\clearpage\n");                         	  
                          out.write("\\vspace*{-0.5cm}\n");
                      	                                  
                          NDSControl.currentLineNo=0;                             
                          NDSControl.currentLinePos=0;
                          
                          NDSControl.numberOfPages+=1;
                          	                          		
                  		  NDSControl.incrementCurrentLineNoAndPos(1,0.30);    //blank line between toptitle and page topline
                	      NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TOP_TITLE_LINEWIDTH);//dataset top title line=0.430 cm
                	      NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline=0.20 cm
                	      NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.DEFAULT_LINEWIDTH);    //one blank line after dataset top title=0.34 cm  
                    		
                          //Title here is the TopTitle set in WriteBody(), which is the title of dataset (reaction name+keynumber)                        		
                          out.write(topTitle+"\\\\"+"\n\\vspace{0.3cm}\n");
                    		                        		
                          //table title                        		
                          out.write(tableTitle+"\\\\\n");                       		
                          out.write("\\vspace{0.3cm}\n");
                      	                      		 
                          NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.TABLE_TITLE_LINEWIDTH);
                          if(tableTitle.contains("underline"))
                          	  NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underline              
        			  }                   
        		  }
        		  
                  out.write("\\parbox["+NDSConfig.PARBOX_ALIGNMENT+"]"+linewidth_str+"{"+FOOTNOTE_WIDTH+"cm}{"+tablePanels.lastElement().getLastTail().elementAt(i).toString()+"}\\\\\n");
                  
                  count=1;//each item here occupies only one line, since the long comments have been split in corresponding TableMaker.     
                  
                  float scale=1.0f;
                  String row=tablePanels.lastElement().getLastTail().elementAt(i);
                  if(Str.containSpecialSupScript(row))
                	  scale=NDSConfig.FOOTNOTE_LINEWIDTH_SCALE+NDSConfig.HEADNOTE_LINEWIDTH_SCALE_OFFSET;            
                  else if(Str.containSupScript(row))
                	  scale=NDSConfig.FOOTNOTE_LINEWIDTH_SCALE;
                  else if(Str.containSubScript(row))
                	  scale=NDSConfig.FOOTNOTE_LINEWIDTH_SCALE;
                  
                  NDSControl.incrementCurrentLineNoAndPos(count,count*NDSConfig.FOOT_COMMENT_LINEWIDTH*scale);//line width<line width of the table-data line
                  
                  //debug
                  //System.out.println("In LatexWriter 2517: writing footnotes: currentLineNo="+NDSControl.currentLineNo+" pos="+NDSControl.currentLinePos);
                  //System.out.println("               count="+count+"  footnote="+tablePanels.lastElement().getLastTail().elementAt(i));                    	                                      

                    
                
        	  }
                
        	  out.write("\\vspace{0.5cm}\n");
                
        	  NDSControl.incrementCurrentLineNoAndPos(1,0.5); 
          }
        
      }
      else
    	  NDSControl.incrementCurrentLineNoAndPos(temp_count,temp_count*NDSConfig.FOOT_COMMENT_LINEWIDTH);
      
    
      //if(tablePanels.elementAt(0).getGenComments().size()+tablePanels.elementAt(0).getTableRecords().size()>0) {
      //	  out.write("\\end{enter}\n");   
      //	  Control.incrementCurrentLineNoAndPos(1);//leave one blank line after center mode   
      //}
    
    
      //System.out.println("WriteTable ends");
      
      if(!isPortrait){      	
    	  out.write("\\end{landscape}"); 	
    	  //if(Control.autoAdjust){         
    		  //start a new page after completing drawing the landscape table  		
    		  NDSControl.isNewPage=true;//must be set to false after use and at the beginning of each run  		
    		  NDSControl.resetNewPage(out);  	
    	  //}     
      }
    
    //Control.incrementCurrentLineNoAndPos(1);
    
  }

  public static String makeDatasetTitle(String dsid)throws Exception{
      
	  String tempID=dsid,TopTitle="";
      
      tempID=tempID.replace(" A ", " |a ").replace(" P ", " p ").replace(" N "," n ");
    	
      if(tempID.contains(":T")&&!tempID.contains(":T1/2")){    		
    	  int p=tempID.indexOf(":T");    		
    	  if(tempID.length()<=p+2 || !Character.isLetter(tempID.charAt(p+2)))  			
    		  tempID=tempID.replace(":T", ":T1/2");   
      }      
      	
      boolean canBeTranslated=true;
      
      String DSID=Translator.process(tempID,false);
      canBeTranslated=Translator.canBeTranslatedAsDSID;
      if(!canBeTranslated && tempID.contains(",")) {
      	DSID=tempID;
      	DSID=DSID.replace("(P,","(p,").replace(",P)",",p)");
      	DSID=DSID.replace("(D,","(d,").replace(",D)",",d)");
      	DSID=DSID.replace("(T,","(t,").replace(",T)",",t)");
      	DSID=DSID.replace("(N,","(n,").replace(",N)",",n)");  
      	
      	DSID=Translator.process(DSID,false);
      }

      TopTitle+=DSID;
      
      return TopTitle;
  }
  
  public static String makeDatasetTitleAndRef(EnsdfTableData etd,boolean addLink)throws Exception{
      String tempID="",TopTitle="";
      
      ENSDF ens=etd.getENSDF();
      
      if(etd.getAltID().length()>0){ 
      	tempID=etd.getAltID();
      }else {
      	tempID=ens.fullDSId();
      }
    	
      if(tempID.contains(" EC DECAY") && ens.nParents()>0) {
    	Parent p=ens.parentAt(0);
		if(p.QF()>1022)
			tempID=tempID.replace("EC DECAY", "EC+B+ DECAY");
      }

      TopTitle+=makeDatasetTitle(tempID);

      if(ens.DSRefS().length()>5) 
      	TopTitle+=("\\hspace{0.2in}"+Translator.process(ens.DSRefS()));
      
      
      //insert hyperlinks for Adopted Levels,Gammas linking to level and gamma tables
      if(TopTitle.contains("Adopted") && addLink){
      	 int a=TopTitle.indexOf("Adopted");
      	 int b=TopTitle.indexOf("Levels");
      	 int c=TopTitle.indexOf("Gammas");
      	 
      	 if(b>a){
      		 String link=EnsdfUtil.makeAdoptedLevelHyperLink("Levels", ens.nucleus());
      		 TopTitle=TopTitle.replace("Levels", link);
      	 }
      	 
      	 if(c>a){
      		 String link=EnsdfUtil.makeAdoptedGammaHyperLink("Gammas", ens.nucleus());
      		 TopTitle=TopTitle.replace("Gammas", link);
      	 }
      }
      
      return TopTitle;
  }
  
  
  public String makeDatasetTitle(Vector<String> block)throws Exception{
      String tempID="",TopTitle="";

      if(block.size()==0)
    	  return "";
    
      tempID=EnsdfUtil.getDSIDFromLine(block.get(0));
      if(tempID.length()==0)
    	  return "";
    			  
   
      if(tempID.contains(":T")&&!tempID.contains(":T1/2")){
    	  int p=tempID.indexOf(":T");
    	  if(tempID.length()<=p+2 || !Character.isLetter(tempID.charAt(p+2)))
    		  tempID=tempID.replace(":T", ":T1/2");
      }    	

      //debug
      //System.out.println("In LatexWriter line 493: toptitle="+TopTitle+" tempID="+tempID);
          
      TopTitle+=Translator.process(tempID,true);
        
      //DSID continuation lines have been already parsed in ENSDF.java---obsolete for now
      //Now always use the first line for DSID, but print all continuation lines in table title
      for(int i=1;i<=2;i++){
      	String line=block.get(i);
      	if(line.charAt(5)!=' ' && line.substring(6,9).equals("   "))
      		TopTitle+=Translator.process(ens.lineAt(i).substring(9).trim(),true);
      }        

      String ref=block.get(0).substring(39,65).trim();
      if(ref.length()>5) 
      	TopTitle+=("\\hspace{0.2in}"+Translator.process(ens.DSRefS()));
      
      return TopTitle;
  }
  
  
	/** writes the abstract of a Nuclear Data Sheets publication */
    //NOTE: Comment objects of the abstract generated in C() in ENSDF.java are not used here.
    //Instead, the abstract lines are re-parsed here to create the Comment objects.
	public void writeAbstract(java.io.Writer out,Vector<String> lines)throws Exception{
	    String mass,EN;
	    Vector<Comment> comments=new Vector<Comment>();
	    Vector<String> temp=new Vector<String>();
	    Comment c;
	    Vector<Comment> TITL=new Vector<Comment>();
	    Vector<Comment> CIT=new Vector<Comment>();
	    Vector<Comment> ABST=new Vector<Comment>();
	    Vector<Comment> CUT=new Vector<Comment>();
	    Vector<Comment> COMM=new Vector<Comment>();
	    Vector<Comment> ACKN=new Vector<Comment>();
	    Vector<Comment> FUN=new Vector<Comment>();
	    Vector<Comment> PERM=new Vector<Comment>();
	    Vector<Vector<Comment>> authors=new Vector<Vector<Comment>>();
	    //Vector<Vector<Comment>> insts=new Vector<Vector<Comment>>();
	    
	    String nuc;
	           
	    //String Z=Control.Z;
	    @SuppressWarnings("unused")
		int A=-1;
	    
	    if(lines.isEmpty())
	    	return;
	    
	    out.write("%Abstract\n");
	    out.write("\\thispagestyle{empty}\n");
	    
	    String pre_type="",type="";
	    for(int i=1;i<lines.size();i++){
	    	
	       String line=lines.elementAt(i);
	       if(line.trim().length()==0){
	    	    pre_type="";
	    		continue;	    		
	       }

	       type=""+line.charAt(6);//line.charAt(6)=c or C, t, single-space (H record)
	       type=type.toUpperCase();
	       
	       if(!type.equals("C"))
	    	   continue;
	       
	       //NOTE: # indicates the marked line should start a new line with indent
	       //      @ indicates the marked line should start a new line in a new paragraph (no indent)
	       //      ! indicates the marked line should start a new page
	       //Refer to setValues() in Comment.java
	       
	       if(line.charAt(5)!=' ' && type.equals(pre_type))
	       //if(line.charAt(5)!=' ' && line.charAt(5)!='#' && line.charAt(5)!='@')
	    	   temp.add((String)lines.elementAt(i));
	       else{
	    	   if(temp.size()>0){

		            c=new Comment();
		            c.setValues(temp);
		            comments.add(c);
		            temp.clear();	 
		            
			        //debug
			        //System.out.println("In LatexWriter 1983: comment="+c.body()+"\n   head="+c.head());
			        //System.out.println("    translated="+c.getTranslated());
	    	   }

	           temp.add((String)lines.elementAt(i));
	            
	        }
	       
	       pre_type=type;
	       
	       //debug
	       //System.out.println("In LatexWriter: line="+line+" number of lines="+temp.size());
	    }
	    
	    //for the last comment
	    if(temp.size()>0){
          c=new Comment();
          c.setValues(temp);
          comments.add(c);    	
	    }
	    
	    //String pre_head="";
	    String head="";
	    for(int i=0;i<comments.size();i++){
	        c=(Comment)comments.elementAt(i);
	        head=c.head().trim().toUpperCase();
	        //if(head.length()==0)
	        //	head=pre_head;
	        
	        //debug
	        //System.out.println("In LatexWriter 2009: comment="+c.body()+"**\n   head="+head+" c.isPeriodAdded()="+c.isPeriodAdded());//+" pre_head="+pre_head);
	        //System.out.println("        translated="+c.getTranslated());
	        
	        if(head.indexOf("AUT")==0) {//AUTH, AUTX,AUT*, all will work
	        	if(c.head().indexOf("AUTH")==0){
	        		Vector<Comment> author=new Vector<Comment>();
	        		authors.add(author);
	        	}	   

	        	if(c.isPeriodAdded())
	        		c.removePeriod();

	        	authors.lastElement().add(c);	        		        	
	        }
	        else if(head.indexOf("INS")==0) {
	        	if(c.head().indexOf("INS")==0){
	        		Vector<Comment> inst=new Vector<Comment>();
	        		authors.add(inst);
	        	}	        
	        	c.removePeriod();
		        //debug
		        //System.out.println("In LatexWriter A: comment="+c.body());
		        //System.out.println("    translated="+c.getTranslated());
	        	
	        	authors.lastElement().add(c);
	        }
	        else if(head.indexOf("TIT")==0) {c.removePeriod();TITL.add(c);}
	        else if(head.indexOf("ABS")==0) ABST.add(c);
	        else if(head.indexOf("CUT")==0) CUT.add(c);
	        else if(head.indexOf("COM")==0) COMM.add(c);
	        else if(head.indexOf("ACK")==0) ACKN.add(c);
	        else if(head.indexOf("FUN")==0) {c.removePeriod(); FUN.add(c);}
	        else if(head.indexOf("CIT")==0) {c.removePeriod(); CIT.add(c);}
	        else if(head.indexOf("PER")==0) {c.removePeriod(); PERM.add(c);}
	        else{ 
	        	COMM.add(c);
	        }
	        
	        //pre_head=head;
	    }
	    
	    
	    String page=NDSControl.page;
	    int index=NDSControl.page.indexOf("-");
	    if(index>0)
	    	page=NDSControl.page.substring(0,index).trim();
	    
	    if(!Str.isNumeric(page))
	    	page="1";
	    
	    out.write("\\setcounter{page}{"+page+"}\n");
	    
	    
	    out.write("\\begin{center}\n");
	    
	    if(!NDSControl.isForWebDisplay){
	    		    	
		    if(Str.isNumeric(NDSControl.abstractVolume) && Str.isNumeric(NDSControl.year))
		    	out.write("Nuclear Data Sheets "+NDSControl.abstractVolume+", \\thepage \\ ("+NDSControl.year+")\\\\\n");
		    else if(CIT.size()>0){
		    	Comment com=CIT.elementAt(0);
		    	if(com.body().trim().length()>0)
		    		out.write(com.body().trim()+"\\\\\n");
		    	
		    }else{
		    	out.write("\n\n");
		    }
	    }else{
	    	out.write("\n\n");
	    }
	    	

	    
	    out.write("\\vspace{0.5cm}\n");
	    
	    mass=( (String)lines.elementAt(0)).substring(0,3).trim();
	    EN=( (String)lines.elementAt(0)).substring(3,5).trim();
	    try{
	    	A=Integer.parseInt(mass);
	    }catch(NumberFormatException e){
	    	A=-1;
	    }
	    
	    if(EN.length()==0) 
	    	nuc="A="+mass;
	    else{
	        if(EN.length()==2) 
	        	EN=EN.charAt(0)+EN.substring(1).toLowerCase();
	        nuc="$^{"+mass+"}$"+EN;
	    }
	    
	    
	    String titleFontSize="";
	    //titleFontSize=" \\Huge ";
	    titleFontSize=" \\huge ";
	    //titleFontSize=" \\Large ";
	    //titleFontSize=" \\large ";
	    //titleFontSize=" \\normalsize ";
	    
	    if(TITL.size()==0){
		    out.write("{"+titleFontSize+"Nuclear Data Sheets for "+nuc);
	    }else{
	    	String title=TITL.firstElement().body();
	    	
	    	//System.out.println(" 0 "+title);
	    	
	    			
	    	title=title.replace(" A=", " ^A=");
	    	
	    	title=Translator.procGenCom(title, 0, true);
	    	
	    	//System.out.println(" 1 "+title);
	    	
	    	title=Str.capFirstLetters(title);
	    	
	    	//System.out.println(" 2 "+title);
	    	
	    	title=Translator.translate(title);

	    	//System.out.println(" 3 "+title);
	    	
	    	out.write("{"+titleFontSize+title);

	    }

	        
	    if(FUN.size()>0 &&FUN.firstElement().body().trim().length()>0) 
	    	out.write("*");
	    
	    out.write("}\\\\\n");
	    out.write("\\vspace{1.0cm}\n");
	    

	    if(TITL.size()>1){
		    out.write("\\end{center}\n\n");
		    out.write("\\addtolength{\\leftskip}{2cm}\n");
		    out.write("\\addtolength{\\rightskip}{1cm}\n");
		    out.write("\\noindent");
	    }

	    
	    for(int i=1;i<TITL.size();i++){
	    	Comment com=TITL.elementAt(i);
	    	if(com.body().trim().length()>0){
	    		String line=com.getTranslated();//translated is trimmed in this call, which removes tailling "\n"
	    		if(line.endsWith("\\\\"))
	    			out.write(line+"\n");
	    		else
	    			out.write(line+"\\\\\n");
	    		
	    	}
	    	else
	    		out.write("\n\n");
	    }
	    
	    if(TITL.size()>1){
	    	out.write("\n");
		    out.write("\\addtolength{\\leftskip}{-2cm}\n");
		    out.write("\\addtolength{\\rightskip}{-1cm}\n");
	    	out.write("\\begin{center}\n\n");
	    }
	    
	    
	    String authorFontSize="";
	    //authorFontSize=" \\Large ";
	    //authorFontSize=" \\large ";
	    authorFontSize=" \\normalsize ";
	    
	    for(int i=0;i<authors.size();i++){
	    	head=authors.elementAt(i).elementAt(0).head().trim();
	    	Vector<Comment> author=new Vector<Comment>();
	    	Vector<Comment> inst  =new Vector<Comment>();
	    	if(head.equals("AUTH"))
	    		author=authors.elementAt(i);
	    	else
	    		inst=authors.elementAt(i);
	    	
	    	
	    	for(int j=0;j<author.size();j++){
		    	Comment com=author.elementAt(j);
		    	if(com.body().trim().length()>0){
		    		String line=com.getRegTranslated();//translated is trimmed in this call, which removes tailing "\n"
                    
		    		if(com.body().indexOf(" A.")>0)
		    			line=line.replace(" a.", " A.");//translator has translated " A." to " a." to avoid the wrong translation of "A" as "\alpha".
		    		

		    		//put footnotemark in brackets in superscript
		    		line=processAuthorsLine(line);

		    		
		    		if(line.endsWith("\\\\"))
		    			out.write("{"+authorFontSize+line+"}\n");
		    		else{
		    			out.write("{"+authorFontSize+line+"}\\\\\n");
		    			
			    		//out.write("{ \\Large "+author.elementAt(j).body().toUpperCase()+"}\\\\\n");		    		
			    		//out.write("{ \\Large "+author.elementAt(j).getTranslated()+"}\\\\\n");
		    		}
		    		
		    	}
		    	else
		    		out.write("\n\n");
	    	}
	    		
		    String instFontSize="";
		    //instFontSize=" \\Large ";
		    //instFontSize=" \\large ";
		    //instFontSize=" \\normalsize ";
		    instFontSize=" \\small ";
		    
	    	for(int j=0;j<inst.size();j++){
		    	Comment com=inst.elementAt(j);
		    	if(com.body().trim().length()>0){
		    		String line=com.getTranslated();//translated is trimmed in this call, which removes tailling "\n"
		    		if(line.endsWith("\\\\"))
		    			out.write("{"+instFontSize+"\\it "+line+"}\n");
		    		else				    		
		    			out.write("{"+instFontSize+"\\it "+line+"}\\\\\n");
		    	}
		    	else
		    		out.write("\n\n");
		    	
		        //debug
		        //System.out.println("In LatexWriter B: comment="+com.body()+" translated="+com.getTranslated()+" isBigC="+com.isBigC());
	    	}
	    		
	        
	        out.write("\\vspace{0.2in}\n");
	    }
	    
	    if(NDSControl.receivedDate.length()>0 && !NDSControl.receivedDate.contains("*")){
	    	out.write("{\\it (Received "+NDSControl.receivedDate);
	    	
		    if(NDSControl.revisedDate.length()>0) 
		    	out.write("; Revised "+NDSControl.revisedDate);
		    
		    out.write(")}\\\\\n");
	    }

	    
	    out.write("\\end{center}\n\n");

	   
	    //must set it back to default values later
	    
	    
	    out.write("\\setlength{\\parindent}{-0.5cm}\n");
	    out.write("\\addtolength{\\leftskip}{2cm}\n");
	    out.write("\\addtolength{\\rightskip}{2cm}\n");
	    
	    if(ABST.size()>0)
	    	out.write("{\\bf Abstract: }\n");
	    for(int i=0;i<ABST.size();i++){
	    	Comment com=ABST.elementAt(i);
	    	if(com.body().trim().length()>0){
	    		String line=com.getTranslated();
	    		line=addBreakAtComma(line);
	    		if(line.endsWith("\\\\"))
	    			out.write(line+"\n");
	    		else if(i==ABST.size()-1)
	    			out.write(line+"\\\\\n\n");//leave a blank line and then start a new line
	    		else 
	    			out.write(line+"\n\n");
	    	}
	    	else
	    		out.write("\n\n");
	    }
	    	    
	    if(CUT.size()>0)
	    	out.write("{\\bf Cutoff Date: }\n");
	    
	    for(int i=0;i<CUT.size();i++){
	    	Comment com=CUT.elementAt(i);
	    	if(com.body().trim().length()>0){
	    		String line=com.getTranslated();//translated is trimmed in this call, which removes tailling "\n"
	    		if(line.endsWith("\\\\"))
	    			out.write(line+"\n");
	    		else if(i==CUT.size()-1)
	    			out.write(line+"\\\\\n\n");//leave a blank line and then start a new line
	    		else 
	    			out.write(line+"\n\n");   		
	    	}
	    	else
	    		out.write("\n\n");
	    }
	    
	    out.write("{\\bf General Policies and Organization of Material: }\n");
	    
	    //out.write("See the January issue of the {\\it Nuclear Data Sheets} or https:// www.nndc.bnl.gov/nds/docs/NDSPolicies.pdf. \\\\\n\n");

	    
	    String month=NDSControl.NDSIssueMonthForPolicy;
	    String url=NDSControl.NDSPolicyURL;
	    int year=Year.now().getValue();
	    
	    out.write("See the "+month+" "+year+" issue of the {\\it Nuclear Data Sheets} or \\\\"+url+". \\\\\n\n");
	    
	    if(COMM.size()>0&&COMM.firstElement().body().length()>0){
	        out.write("{\\bf General Comments: }\n");
		    for(int i=0;i<COMM.size();i++){
		    	Comment com=COMM.elementAt(i);
		    	if(com.body().trim().length()>0){
		    		String line=com.getTranslated();//translated is trimmed in this call, which removes tailling "\n"
		    		if(line.endsWith("\\\\"))
		    			out.write(line+"\n");
		    		else if(i==COMM.size()-1)
		    			out.write(line+"\\\\\n\n");//leave a blank line and then start a new line
		    		else 
		    			out.write(line+"\n\n"); 
		    	}
		    	else
		    		out.write("\n\n");
		    }
	    }
	    
	    if(ACKN.size()>0&&ACKN.firstElement().body().length()>0){
	        out.write("{\\bf Acknowledgements: }\n");
	        for(int i=0;i<ACKN.size();i++){
		    	Comment com=ACKN.elementAt(i);
		    	if(com.body().trim().length()>0){
		    		String line=com.getTranslated();//translated is trimmed in this call, which removes tailling "\n"
		    		if(line.endsWith("\\\\"))
		    			out.write(line+"\n");
		    		else if(i==ACKN.size()-1)
		    			out.write(line+"\\\\\n\n");//leave a blank line and then start a new line
		    		else 
		    			out.write(line+"\n\n");
		    	}
		    	else
		    		out.write("\n\n");
	        }
	    }
	
	    
	    if(NDSControl.isForWebDisplay){
		    if(CIT.size()>0&&CIT.firstElement().body().length()>0){
		        out.write("{\\bf Citations: }\n");
		        for(int i=0;i<CIT.size();i++){
			    	Comment com=CIT.elementAt(i);
			    	if(com.body().trim().length()>0){
			    		String line=com.getTranslated();//translated is trimmed in this call, which removes tailling "\n"
			    		if(line.endsWith("\\\\"))
			    			out.write(line+"\n");
			    		else if(i==CIT.size()-1)
			    			out.write(line+"\\\\\n\n");//leave a blank line and then start a new line
			    		else 
			    			out.write(line+"\n\n"); 
			    	}
			    	else
			    		out.write("\n\n");
		        }
		    }
	    }

	    
	    
	    if(NDSControl.isUpdate){
	        out.write("{\\bf EDITOR'S NOTE}\\\\\n");
	        if(NDSControl.updCom1.length()>0) out.write(NDSControl.updCom1+"\n");
	        else
	        out.write("The present evaluation for "+nuc+" is printed in abbreviated form. Only the adopted, and possible a few " +
	                "other datasets, are presented. See Elsevier and NNDC websites for full evaluation. The abbreviated form is used " +
	                "to reduce printed pages and also because it appears to be the users' preferred mode.\n");
	    }
	    
	    boolean hasPERM=false;
	    
	    if(PERM.size()>0&&PERM.firstElement().body().trim().length()>0){
	        out.write("\\vfill\n\n");
	        for(int i=0;i<PERM.size();i++){
		    	Comment com=PERM.elementAt(i);
		    	if(com.body().trim().length()>0){
		    		String line="$^{"+Translator.translate(com.flags())+"}$~"+com.getTranslated();//translated is trimmed in this call, which removes tailling "\n"
		    		if(line.endsWith("\\\\"))
		    			out.write(line+"\n");
		    		else if(i==PERM.size()-1)
		    			out.write(line+"\\\\\n\n");//leave a blank line and then start a new line
		    		else 
		    			out.write(line+"\n\n");
		    	}
		    	else
		    		out.write("\n\n");
	        }
	        hasPERM=true;
	    }
	    
	    
	    if(FUN.size()>0&&FUN.firstElement().body().trim().length()>0){
	        if(!hasPERM)
	        	out.write("\\vfill\n\n");
	        
	        out.write("* ");
	        for(int i=0;i<FUN.size();i++){
		    	Comment com=FUN.elementAt(i);
		    	if(com.body().trim().length()>0){
		    		String line=com.getTranslated();//translated is trimmed in this call, which removes tailing "\n"
		    		if(line.endsWith("\\\\"))
		    			out.write(line+"\n");
		    		else if(i==FUN.size()-1)
		    			out.write(line+"\\\\\n\n");//leave a blank line and then start a new line
		    		else 
		    			out.write(line+"\n\n");
		    	}
		    	else
		    		out.write("\n\n");
	        }
	    }
	    
	    out.write("\\setlength{\\parindent}{+0.5cm}\n");
	    out.write("\\addtolength{\\leftskip}{-2cm}\n");
	    out.write("\\addtolength{\\rightskip}{-2cm}\n");
	    
	    NDSControl.numberOfPages+=1;//abstract page
	    
	    //TableRecords
	    ENSDF ens=new ENSDF();
	    ens.setLittleLittleValues(lines);
	    LevelTableMaker tm=new LevelTableMaker();
	    if(ens.nTableRecords()>0){
	    	out.write("\\newpage\n");
	    	out.write("\\pagestyle{plain}\n");
	    	out.write("\\begin{minipage}{\\textwidth}\n");
	        for(int i=0;i<ens.nTableRecords();i++){
	            TableRecord t=ens.tableAt(i);
	            Vector<String> tr=tm.makeTableRecords(t,false);
	            
	            for(int j=0;j<tr.size();j++) {
		           	
	              	//NOTE: new line character "\n" (and "\\\\" in Latex) is already added at the end of each line in TableRecords 	
	              	out.write(tr.elementAt(j));
	              	
	              }
	        }
		    
	        out.write("\\end{minipage}\n");
		    NDSControl.numberOfPages+=1;//abstract page
	    }
	    
	}

	public void writeIndexPage(java.io.Writer out,int A)throws Exception{
	    out.write("\\newpage\n");
	    out.write("\\pagestyle{plain}\n");
	    if(NDSControl.hasIndex){
	        out.write("\\setlength{\\columnseprule}{1pt}\n");
	        out.write("\\setlength{\\columnsep}{1cm}\n");
	        out.write("\\begin{center}\n");
	        out.write("\\underline{\\normalsize Index for A="+A+"}\n");
	        out.write("\\end{center}\n");
	        out.write("\\hspace{.3cm}\\raggedright\\underline{Nuclide}\\hspace{1cm}\\underline{Data Type\\mbox{\\hspace{2.3cm}}}\\hspace{2cm}\\underline{Page}\\hspace{1cm}\n");
	        out.write("\\raggedright\\underline{Nuclide}\\hspace{1cm}\\underline{Data Type\\mbox{\\hspace{2.3cm}}}\\hspace{2cm}\\underline{Page}\n");
	
	        //out.write("\\begin{minipage}{0.95\\textwidth}\n");//minipage doesn't spread to multipages when the table of contents is large
	        out.write("\\begin{adjustwidth}{}{0.05\\textwidth}\n");//both of which are adjustments inward from the existing margins
	        out.write("\\begin{multicols}{2}\n");
	        out.write("\\setcounter{tocdepth}{3}\n");
	        out.write("\\renewcommand{\\contentsname}{\\protect\\vspace{-0.8cm}}\n");
	        out.write("\\tableofcontents\n");
	        out.write("\\end{multicols}\n");
	        out.write("\\end{adjustwidth}\n");
	        //out.write("\\end{minipage}\n");//minipage doesn't spread to multipages when the table of contents is large
	        
	        NDSControl.numberOfPages+=1;//index page, assuming one page
	    }
	    if(NDSControl.blankPage){
	        out.write("\\clearpage\n");
	        out.write("\\thispagestyle{empty}\n");
	        out.write("\\mbox{}\n");
	        out.write("\\clearpage\n");
	        
	        NDSControl.numberOfPages+=1;
	    }
	    
	}
	
	public void writeReferenceBlock(java.io.Writer out,Vector<String> lines)throws Exception{
	    out.write("\\newpage\n");
	    out.write("\\pagestyle{plain}\n");
		out.write("\\begin{flushleft}");
		int totalLines=0;
		for(int i=0;i<lines.size();i++){
			String line=lines.get(i);
			line=String.format("%-80s", line);
			if(line.toUpperCase().charAt(7)!='R')
				continue;
			
			line=line.trim();
			String keynumber=line.substring(9, 17);		
			line=String.format("%-12s %-70s", keynumber,line.substring(17).trim());
			out.write(line+"\\\\\n");
			
			totalLines++;

		}
		
		NDSControl.numberOfPages+=(totalLines/66)+1;//65 lines per page
		
		out.write("\\end{flushleft}");	
	}
	/** writes the history record in the output for web-display */
	public void writeHistory(java.io.Writer out,ENSDF ens)throws Exception{

		ArrayList<String> titles=new ArrayList<String>(Arrays.asList("Type","Author","Citation","Literature Cutoff Date"));

		ArrayList<String> otherTitles=new ArrayList<String>(Arrays.asList("Type","Author","Date","Comments"));
		
		String titleLine="",otherTitleLine="";
		String ruleLine="",otherRuleLine="";
		String historyLine="";
		String tempLine="";
		
		HashMap<String,String> typeMap=new HashMap<String,String>();
		typeMap.put("FUL", "Full Evaluation");
		typeMap.put("FMT", "Format Change");
		typeMap.put("ERR", "Errata");
		typeMap.put("MOD", "Modified");
		typeMap.put("UPD", "Update");
		typeMap.put("EXP", "Experimental");
		
		NDSControl.incrementCurrentLineNoAndPos(1); //each table leaves one-line space before and after it
		
        out.write("\\begin{center}\n");
        out.write("\\begin{tabular}{cccc}\n");
        
        out.write("\\multicolumn{"+titles.size()+"}{c}{History}\\\\\n");
        NDSControl.incrementCurrentLineNoAndPos(1); 
        
        for(int i=0;i<titles.size();i++){
        	titleLine+=titles.get(i);
        	ruleLine+="\\hrulefill";

        	if(i<titles.size()-1){
        		titleLine+="&";
        		ruleLine+="&";
        	}

        }     
        
        for(int i=0;i<otherTitles.size();i++){
        	otherTitleLine+=otherTitles.get(i);
        	otherRuleLine+="\\hrulefill";

        	if(i<otherTitles.size()-1){
        		otherTitleLine+="&";
        		otherRuleLine+="&";
        	}

        }    

        
        Vector<String> evalHistLinesV=new Vector<String>();//FUL and UPD with cutoff date
        Vector<String> comHistLinesV=new Vector<String>();//other histories with comments
        History hist;
        
        boolean isComHistAtFirst=false;
        for(int i=0;i<ens.nHis();i++){
        	historyLine="";
        	hist=ens.historyAt(i);
        	
        	boolean isComHistTemp=false;
        	if(hist.typ().equals("FUL") || hist.typ().equals("UPD")) {
        		String cit=hist.cit();
        		if(cit.startsWith("NDS") || cit.startsWith("NUCL")) {
        			String nsrLink="",keyno="";
        			if(!citationKeynoMap.containsKey(cit)) {
            			keyno=EnsdfUtil.findKeynumberByHistoryLine(hist);   
            			citationKeynoMap.put(cit, keyno);
        			}else {
        				keyno=citationKeynoMap.get(cit);
        			}
        			
        			if(keyno.isEmpty()) {
        				cit=Translator.translate(cit);
        			}else {
            			nsrLink=BaseNSR.NSRLinkManager(keyno,'B');
            	        nsrLink=nsrLink.replace("&","\\&");           			
            	        cit="\\href{"+nsrLink+"}{"+Translator.translate(cit)+"}";
        			}
        		}
        		
        		        		
        		historyLine+=typeMap.get(hist.typ().toUpperCase())+"&";
            	historyLine+=Translator.translate(hist.Aut())+"&";
            	historyLine+=cit+"&";
            	historyLine+=Translator.translate(hist.Cut());
            	
            	evalHistLinesV.add(historyLine);
            	
            	isComHistTemp=false;
        	}else {
            	historyLine+=typeMap.get(hist.typ().toUpperCase())+"&";
            	historyLine+=Translator.translate(hist.Aut())+"&";
            	historyLine+=Translator.translate(hist.Dat())+"&";
            	historyLine+=Translator.translate(hist.com());
            	comHistLinesV.add(historyLine);
            	
            	isComHistTemp=true;
        	}


        	if(tempLine.isEmpty()){//store the first history record
        		tempLine=historyLine;
        		isComHistAtFirst=isComHistTemp;
        	}
        	//System.out.println(" aut="+hist.aut()+" historyLine="+historyLine);
        	
        	if(!hist.Cut().isEmpty() && !hist.typ().toUpperCase().equals("ERR") 
        			&& !NDSControl.showAllHistories){//write the first history record of not Errata type and with cut date
        		tempLine=historyLine;
        		isComHistAtFirst=isComHistTemp;
        		
        		break;
        	}
        	else
        		continue;
        		
        			
        		
        	/*		
        	if(hist.Cut().isEmpty()){
        		historyLine+=Translator.translate(hist.Cut());
        	}
        	else
        		historyLine+=Translator.translate(hist.Dat());
        	       	
        	out.write(historyLine+"\\\\\n");
        	
            //debug
        	//System.out.println("In LatexWriter line 2319: historyLine="+historyLine);
        	//System.out.println(" currentPos="+Control.currentLinePos);

        	Control.incrementCurrentLineNoAndPos(1);
        	*/
        }

        if(!tempLine.isEmpty()){
        	if(!NDSControl.showAllHistories) {
        		
        		if(!isComHistAtFirst) {
        			out.write(titleLine+"\\\\\n");
        		}else {
        			out.write(otherTitleLine+"\\\\\n");
        			ruleLine=otherRuleLine;
        		}
        		out.write("[-.2cm]\n");
                
                NDSControl.incrementCurrentLineNoAndPos(1);       
            	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underlines below
                       
                out.write(ruleLine+"\\\\\n");                
            	out.write(tempLine+"\\\\\n");
            	NDSControl.incrementCurrentLineNoAndPos(1);
        	}else {//print all history records
        		boolean hasEvalHist=false;
        		if(evalHistLinesV.size()>0) {
                    out.write(titleLine+"\\\\\n");
                    out.write("[-.2cm]\n");          
                    NDSControl.incrementCurrentLineNoAndPos(1);       
                	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underlines below                  
                    out.write(ruleLine+"\\\\\n");
                    
            		for(String line:evalHistLinesV) {
                    	out.write(line+"\\\\\n");
                    	NDSControl.incrementCurrentLineNoAndPos(1);
            		}
                    
            		hasEvalHist=true;
        		}
        		
        		if(comHistLinesV.size()>0) {
        			if(hasEvalHist) {
        				out.write("\\\\\n");
        				NDSControl.incrementCurrentLineNoAndPos(1);
        			}
                    out.write(otherTitleLine+"\\\\\n");
                    out.write("[-.2cm]\n");          
                    NDSControl.incrementCurrentLineNoAndPos(1);       
                	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underlines below                  
                    out.write(otherRuleLine+"\\\\\n");
                    
            		for(String line:comHistLinesV) {
                    	out.write(line+"\\\\\n");
                    	NDSControl.incrementCurrentLineNoAndPos(1);
            		}
                    
        		}
        	}

        }else {
            out.write(titleLine+"\\\\\n");
            out.write("[-.2cm]\n");          
            NDSControl.incrementCurrentLineNoAndPos(1);       
        	NDSControl.incrementCurrentLineNoAndPos(1,NDSConfig.UNDERLINE_WIDTH);//for underlines below                  
            out.write(ruleLine+"\\\\\n");
        }
        
        out.write("\\end{tabular}\n");
        out.write("\\end{center}\n");
        
        NDSControl.incrementCurrentLineNoAndPos(1); //each table leaves one-line space before and after it
	}
	
	/* add \allowbreak after char like ',' to allow line break when there is no whitespce following
	 * the comma for linebreaking. LaTex doesn't break the line at ',' by default for
	 * situations like this.
	 */
	public String addBreak(String latexStr,char ch){
		String S="";
		String temp=latexStr;
		int p=0;
		while((p=temp.indexOf(','))>0){
			S+=temp.substring(0,p+1);
			if(p<temp.length()-1 && temp.charAt(p+1)!=' '){
				S+="{\\allowbreak}";
			}
			if(p>=temp.length()-1)
				break;
			
			temp=temp.substring(p+1);
		}
		
		S+=temp;
		
		return S;
	}
	
	public String addBreakAtComma(String latexStr){
		return addBreak(latexStr,',');
	}
	
	public Vector<String> getFigureFiles(){
		return figureFiles;
	}
	
	/* put flags in round brackets in authors line (in LaTeX) as footnote marks in superscript */
	public String processAuthorsLine(String line){
		String out="";
		String temp=line.trim();
		if(temp.length()==0)
			return line;
		
		int lb=temp.indexOf("(");
		int rb=temp.indexOf(")");
		
		if(lb<0 || rb<=0 || lb>=rb)
			return line;
	
		
		String mark=temp.substring(lb+1, rb);	
		
		out="";
        int pre_rb=-1;
		while(mark.length()>0){
			char c=mark.charAt(0);
			out+=temp.substring(pre_rb+1, lb);
			if( (mark.length()==1 && Character.isLetterOrDigit(c)) || mark.charAt(0)=='\\'){//is a mark: letter or digit or special character
				out+="$^{"+mark+"}$";
			}else{
				out+="("+mark+")";
			}
			
			
			pre_rb=rb;
			lb=temp.indexOf("(",pre_rb+1);
			rb=temp.indexOf(")",pre_rb+1);
			if(rb>lb && lb>0)
				mark=temp.substring(lb+1, rb);
			else
				mark="";
		}
		
		if(pre_rb+1<temp.length())
			out+=temp.substring(pre_rb+1);
		
		return out;
	}
	
	public void setHeading(java.io.Writer out,ENSDF ens)throws Exception{
		if(NDSControl.isForWebDisplay)
			setWebHeading(out,ens);
	}
	
	public void setDefaultHeading(java.io.Writer out)throws Exception{
		NDSControl.setDefaultHeading(out);
	}
	

    public void setWebHeading(java.io.Writer out,ENSDF ens)throws Exception{
    	String[] monthNames={"January","February","March","April","May","June","July","August","September","October","November","December"};
    	
    	String ds="";

    	String date="";
    	String year="",month="";
    	int imonth=-1;
    	
    	if(!NDSControl.heading.contains("ENSDF")){//for XUNDL dataset, date from 1st line starting from col#75 
    		
    		ds=ens.DSDateS().trim();//date=dataset 1st line from col#75 
    		
        	if(ds.length()==6){//eg, 200910=September 2009
        		year=ds.substring(0, 4).trim();
        		month=ds.substring(4).trim();
        		if(year.length()==4 && month.length()==2){
        			if(month.charAt(0)=='0')
        				month=month.substring(1);
        			
        			if(Str.isInteger(year) && Str.isInteger(month)){
        				imonth=Integer.parseInt(month);
                		if(imonth>0 && imonth<=12){
                			month=monthNames[imonth-1];
                			date=month+" "+year;
                		}
        			}
        		}     		
        	}
    	}
    	else if(ens.nHis()>0){//data from cutoff date in history record
    		int nhis=ens.nHis();
    		for(int i=0;i<nhis;i++){
    			History h=ens.historyAt(i);
    			String type=h.typ().toUpperCase();
    			
    			String extraWord="";
    			if(!type.equals("FUL") && !type.equals("UPD"))
    				continue;
    			else if(type.equals("FUL"))
    				extraWord="Evaluated";
    			else
    				extraWord="Updated";
    			
    			ds=h.cut().trim();
    			String[] temp=Str.trimArray(ds.toLowerCase().split("-"));
    			 			
    			if(temp.length==3){
  			    	for(int j=0;j<monthNames.length;j++){
  			    		if(monthNames[j].toUpperCase().indexOf(temp[1].toUpperCase())==0){
  			    			month=monthNames[j];
  			    			year=temp[2];
  			    			date=month+" "+year;
  			    			if(extraWord.length()>0)
  			    				date=extraWord+" "+date;
  			    			
  			    			break;
  			    		}
  			    	}
    			}  
    			
    			break;
    		}
    	}
    	
    	String heading=NDSControl.heading;
    	
    	if(date.trim().length()>0){
    		heading=NDSControl.heading+" - "+date;
    	}
    	
    	String tooltip=run.name()+" (version "+run.version()+")";
        if(!heading.isEmpty()) { 
        	String s=heading;
        	String s1=s.substring(0,s.length()-1);
        	String s2=s.substring(s.length()-1);
        	
        	s=s1+"\\pdftooltip{"+s2+"}{"+tooltip+"}";
        	out.write("\\chead{\\small{"+s+" \\\\ }}\n");
        }else {
        	out.write("\\chead{\\small{\\pdftooltip{\\phantom{Java-NDS}}{"+tooltip+"} \\\\ }}\n");
        }
		
	    //debug
	    //System.out.println("In LatexWriter line 2868: "+Control.heading);
		
		return;
    		
    }
    
    //write all document records together as additional information in separate pages different from the main body of the dataset
    //for web-display only
    //11/13/2017
    public void writeDocumentRecords(java.io.Writer out,MassChain data)throws Exception{
    	for(int i=0;i<data.nBlocks();i++){
    		Vector<DocumentRecords> drV=data.getDocumentRecordsV(i);
            writeDocumentRecords(out,drV,data.getBlockAt(i),data.getETDByBlockAt(i));
    	}
    }
    
    public void writeDocumentRecords(java.io.Writer out,Vector<DocumentRecords> drV,Vector<String> block,EnsdfTableData etd)throws Exception{

    	if(block.size()==0)
    		return;
    	
    	out.write("\\begin{center}\n");
    	
    	Nucleus n=new Nucleus();
    	n.parseNUCID( ( (String) block.elementAt(0) ).substring(0,5) );

        String code = "\\ensuremath{^{"+String.valueOf(n.A())+"}_{";
        String En=n.En();

        if(En.toUpperCase().equals("NN"))
        	En="n";
        
        if(String.valueOf(n.A()).length()>String.valueOf(n.Z()).length()) code+="\\ ";
        
        code+=String.valueOf(n.Z())+"}}"+En+
                "\\ensuremath{_{"+String.valueOf(n.N())+"}^{~}}";

        out.write("\\section["+code+"]{ }\n");
        out.write("\\vspace{-30pt}\n");
        out.write("\\setcounter{chappage}{1}\n");
        out.write("\\vspace{0.3cm}\n");//leave one blank line, this assures the same toptitle margin as that in continued page 
                
		String TopTitle=this.makeDatasetTitle(block);
        TopTitle="{\\bf \\small \\underline{"+TopTitle;
        
        //generate hyperlink target for link in XREF
        //out.write("\\hypertarget{"+etd.getLinkID()+"}{"+TopTitle+"}}}\\\\\n");     
        
        out.write(TopTitle+"}}\\\\\n");   
        out.write("\\vspace{4pt}\n");//4pt=0.141cm
             
		out.write("\\begin{longtable}{ll}\n"); 
		//out.write("@{\\extracolsep{\\fill}}c");//an invisible column added to fill up the rest of the width
		for(int j=0;j<drV.size();j++){
			DocumentRecords dr=drV.get(j);
			Vector<Vector<String>> linesV=dr.docV;
			for(int k=0;k<linesV.size();k++){
				Vector<String> lines=new Vector<String>();
				lines.addAll(linesV.get(k));
				
				String DQLine="",DQLineTranslated="";
				for(int m=0;m<lines.size();m++){
					String line=lines.get(m);
		            char c=line.charAt(6);
		            
		            //System.out.println(" m="+m+"  Line="+line);
		            
		            //for document Q record line, read and translated as a regular Q record line
		            if(line.charAt(7)=='Q' && !line.contains("$")) {
		                Vector<String> qline=new Vector<String>();
		                qline.add(line);		                
		                
		                String temp="";
		                try {
			                QValue qv=new QValue();
			                qv.setValues(qline);
			                
			                String qs="",dqs="",ending="";
			                if(qv.QBMS().length()>0 && Str.isNumeric(qv.QBMS())){
			                	qs=qv.QBMS();
			                	dqs=qv.DQBMS();
			                	if(qv.QBM().isSyst) {
			                		if(!qv.QBM().isSystAMid)
			                			ending=" (syst)";
			                		else {
			                			ending="";
			                			qs+=" (syst)";
			                		}
			                	}
			                	temp+="Q(\\ensuremath{\\beta^-})"+printNumber(qs,dqs,true)+ending+"; ";
			                }
			                
			                if(qv.SNS().length()>0 && Str.isNumeric(qv.SNS())){
			                	qs=qv.SNS();
			                	dqs=qv.DSNS();
			                	if(qv.SN().isSyst) {
			                		if(!qv.SN().isSystAMid)
			                			ending=" (syst)";
			                		else {
			                			ending="";
			                			qs+=" (syst)";
			                		}
			                	}
			                	temp+="S(n)"+printNumber(qs,dqs,true)+ending+"; ";
			                }
			                if(qv.SPS().length()>0 && Str.isNumeric(qv.SPS())){
			                	qs=qv.SPS();
			                	dqs=qv.DSPS();
			                	if(qv.SP().isSyst) {
			                		if(!qv.SP().isSystAMid)
			                			ending=" (syst)";
			                		else {
			                			ending="";
			                			qs+=" (syst)";
			                		}
			                	}
			                	temp+="S(p)"+printNumber(qs,dqs,true)+ending+"; ";
			                }
			                if(qv.QAS().length()>0 && Str.isNumeric(qv.QAS())){
			                	qs=qv.QAS();
			                	dqs=qv.DQAS();
			                	if(qv.QA().isSyst) {
			                		if(!qv.QA().isSystAMid)
			                			ending=" (syst)";
			                		else {
			                			ending="";
			                			qs+=" (syst)";
			                		}
			                	}
			                	temp+="Q(\\ensuremath{\\alpha})"+printNumber(qs,dqs,true)+ending+"; ";
			                }

			                temp=temp.trim();
			                if(temp.endsWith(";"))
			                	temp=temp.substring(0,temp.length()-1);
			                
			                if(temp.length()>0) {
			    				DQLineTranslated=temp+"\\hspace{0.2in}"+Translator.process(qv.QRef());
		    				    DQLine=line;	
			    				
			    				//System.out.println("DQLine="+DQLine);
			    				
				                continue;
			                }
			                
		                }catch(Exception e) {
		                	e.printStackTrace();
		                }

		            }
		            
		            //System.out.println("$$ m="+m+"  Line="+line);
		            
		    		if(c=='d')
		    			line=line.substring(0,6)+'c'+line.substring(7);
		    		else
		    			line=line.substring(0,6)+'C'+line.substring(7);
		    		
		    		lines.setElementAt(line,m);
				}
				
				lines.remove(DQLine);

				Comment c=new Comment();				
				if(lines.size()>0)
					c.setValues(lines);
				
				if(k==0){
					String label=dr.label;
					int count=j+1;//index of each document
	        		if(count<10)
	        			label+="\\phantom{11}"+count;
	        		else if(count<100)
	        			label+="\\phantom{1}"+count;
	        		else
	        			label+=count;
	        		
					out.write(label+": &");
				}
				else
					out.write("&");
				
				String head="";
				String s=c.getTranslated()+DQLineTranslated;
	        	
				//NOTE:
				//type='A' (after first data entry)  are in-table comments
				//type='B' (before first data entry) are general comments (dataset top or table top) 
				//             if head is empty or footnote comments when head is not empty 
				if(dr.docuType.charAt(0)=='A' && c.head().length()>0)
					head=this.makeCommentHead(c, etd);

				//System.out.println("In LatexWriter line 3053: head="+head+"   line="+s+"  c.head="+c.head()+" line0="+lines.get(0));
			
				
				s="\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+head+s;
				out.write("\\parbox[t]{0.78\\textwidth}{\\raggedright "+s+" \\vspace{0.1cm}}\\\\\n");

			}
		}
		
		out.write("\\end{longtable}\n");
		out.write("\\end{center}\n");
    }
    
    
    //make and translate comment head for comments in table 
    //(not footnote comments since there is no need for printing head information for those comments.)
    //Note that only footnote comments and in-table comments have head information.
    public String makeCommentHead(Comment com,EnsdfTableData etd) throws Exception{
    	String head="";
    	String types="LGBEAD";
    	
    	char type=com.type().charAt(0);//L,G,B,A,E,D
    	if(etd==null)
    		return "";
    	
    	if(com.head().trim().length()==0)
    		return "";
    	if(com.flags().length()>0)//only footnote comments have flag
    		return "";
    	if(types.indexOf(type)<0)
    		return "";
    	
    	
    	TableMaker tm;
    	TableControl tc;
    	if(type=='L'){
    		tm=new LevelTableMaker(etd,null);
    		tc=etd.getLevelTableControl();//used for relabelled head
    	}
    	else if(type=='G'){
    		tm=new GammaTableMaker(etd,null);
    		tc=etd.getGammaTableControl();//used for relabelled head
    	}
    	else if(type=='D'){
    		tm=new DelayedTableMaker(etd,null);
    		tc=etd.getDelayTableControl();//used for relabelled head
    	}  	
    	else{
    		tm=new DecayTableMaker(etd,null);
    		tc=etd.getDecayTableControl();//used for relabelled head
    	} 
    	
    	
    	//try{
    		tm.setTableControl(tc);
    		tm.setETD(etd);
    		head=tm.commentHeadMaker(com);
    	//}catch (Exception e){
    	//	head="";
    	//	e.printStackTrace();
    	//}

			
    	return head;
    	
    }

	
}
