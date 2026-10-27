
package ensdfparser.nds.config;

import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.nds.ensdf.EnsdfTableData;

/**
 * The configuration of this run.  A static global object is being used
 * to prevent having to pass dozens of small and potentially changing
 * variables around.
 *
 * Created 2007 Roy Zywina
 * Updated May 4 2010 Scott Geraedts
 * Updated by Jun Chen in 2015
 */
public class NDSConfig {
    /* Constants */
	public static final float PAGE_WIDTH=21.5f;//
	
    public static final float DEFAULT_WIDTH = 18.5f;
    public static final float DEFAULT_HEIGHT = 20.0f;
    public static final float DEFAULT_SCALE_FACTOR=1.0f;
    public static final int DEFAULT_LABEL_SIZE=12;
    
    //default line count of the heading in an ENSDF table when no comments and record table data on top of the table
    public static final int DEFAULT_HEADLINE_NO=8;     //start from 0, underlined head count as two lines
    public static final float DEFAULT_HEADLINE_POS=2.5f; //cm, from top line to the line under the heading
    
    public static final float DEFAULT_BAND_FONT_SCALE=1.0f;       
    public static final float DEFAULT_LEVEL_FONT_SCALE=1.0f;
    public static final float DEFAULT_GAMMA_FONT_SCALE=1.0f;
    
    public static final float LEFT_MARGIN=2.0f;//set total left margin=1 inch +/hoffset+/oddsidemargin=2.0cm in latex
    public static final float RIGHT_MARGIN=1.8f;
    
    public static final float MAX_TEXT_WIDTH=PAGE_WIDTH-LEFT_MARGIN-RIGHT_MARGIN;
    public static final float MIN_COMMENT_WIDTH=4.0f;
    public static final float MAX_DATA_WIDTH=MAX_TEXT_WIDTH-MIN_COMMENT_WIDTH;//when comment column present
    
    public static final float MAX_TEXT_HEIGHT=22.5f;//cm, between top and bottom lines
    

    public static final float MAX_TOTAL_WIDTH=MAX_TEXT_WIDTH;
    public static final float MAX_TOTAL_HEIGHT=MAX_TEXT_HEIGHT;

    //total left margin (left-edge to text)=1 inch (default)+/hoffset+/oddsidemargin
    //it is found that /oddsidemargin=2.15cm (default in our case, haven't figured out why)
    //1 inch=2.52cm
    
    //public static final float MAX_TEXT_WIDTH=19f;
    //public static final float MAX_TOTAL_WIDTH=19f;
    //public static final float HOFFSET=-3.7f;
    
    public static final float HOFFSET=LEFT_MARGIN-2.52f-2.15f;
    public static final float VOFFSET=-1.8f;
    
    public static final float FOOTNOTE_WIDTH=MAX_TEXT_WIDTH;//cm
    public static final float HEADNOTE_WIDTH=MAX_TEXT_WIDTH;
    public static final int FOOTNOTE_LENGTH=(int)(FOOTNOTE_WIDTH*117/16.0f);
    public static final int HEADNOTE_LENGTH=(int)(HEADNOTE_WIDTH*117/16.0f);
    

    public static final int MAX_COMMENT_LENGTH=100;
    public static final int MAX_LINE_LENGTH=100;
    
    public static final int MAX_LINE_COUNT=62;//when no comments. With comment present, it should be less due to larger line spacing
    public static final int MAX_LINE_COUNT_LANDSCAPE=47;
    
    
    //cm, when comment line (in parbox) is present or not, the line width is different
    public static final float TABLE_BODY_LINEWIDTH0=0.340f;                         //linewidth when no comment is present. With comment, the linewidth is larger.                                                                      
    public static final float TABLE_BODY_LINEWIDTH1=TABLE_BODY_LINEWIDTH0+0.0000f;  //first linewidth when comment is only one line   
    public static final float TABLE_BODY_LINEWIDTH2=TABLE_BODY_LINEWIDTH0+0.0000f;  //first linewidth when comment has multiple lines.  
    public static final float TABLE_BODY_LINEWIDTH3=TABLE_BODY_LINEWIDTH0+0.0000f;  //comment linewidth when comment has multiple lines (excluding the first line)
    public static final float TABLE_BODY_LINEWIDTH=TABLE_BODY_LINEWIDTH0+0.0300f;   //used in fingBreaks in TableMaker.java
    
    public static final float TABLE_RECORDS_LINEWIDTH=0.345f;      //for table records data, like comment table data
    public static final float XREF_TABLE_LINEWIDTH=0.365f;      //for table records data, like XREF
    
    
    public static final float HEAD_COMMENT_LINEWIDTH=0.385f;       //for general comments of each table
    public static final float FOOT_COMMENT_LINEWIDTH=0.380f;       //for footnote comments of each table
    public static final float GENNERAL_COMMENT_LINEWIDTH=0.385f;   //for general comment before all tables
    public static final float DEFAULT_LINEWIDTH=0.340f;            //for plain text lines not in any environments, actual =0.36133-0.028=0.333, see below PARBOX_HEIGHT
    
    public static final float TABLE_TOP_MARGIN=0.385f;//LaTeX put a blank line before a long table
    public static final float TABLE_BOTTOM_MARGIN=0.385f;//LaTeX put a blank line after a long table
    
    public static final float HEADNOTE_INDENT=-0.254f;//-0.1in
    public static final float COMMENT_INDENT=0.254f;
    
    public static final float LINEWIDTH_SCALE=1.00f;//multiplier for the width of lines 
    
    public static final float TABLE_LINEWIDTH_SCALE0=1.115f;//multiplier for the width of lines that contains super- or sub-script (like 10^16, add 0.1 for ^@,#)
    public static final float TABLE_LINEWIDTH_SCALE1=1.105f;//multiplier for the width of lines that contains super- or sub-script
    public static final float TABLE_LINEWIDTH_SCALE2=1.105f;//multiplier for the width of lines that contains super- or sub-script
    public static final float TABLE_LINEWIDTH_SCALE3=1.105f;//multiplier for the width of lines that contains super- or sub-script
    public static final float TABLE_LINEWIDTH_SCALE4=1.050f;//multiplier for the width of lines that contains super- or sub-script
    
    public static final float TABLE_LINEWIDTH_SCALE_JPI=1.025f;
    public static final float TABLE_LINEWIDTH_SCALE_SHORT=1.050f;
    
    
    public static final float FOOTNOTE_LINEWIDTH_SCALE=1.030f;//multiplier for the width of lines that contains super- or sub-sript
    public static final float HEADNOTE_LINEWIDTH_SCALE=1.030f;//multiplier for the width of lines that contains super- or sub-sript
    
    public static final float LINEWIDTH_SCALE_OFFSET0=0.200f;//offset for getting linewidth with special superscript;
    public static final float LINEWIDTH_SCALE_OFFSET=0.150f;//offset for getting linewidth with special superscript;
    
    public static final float HEADNOTE_LINEWIDTH_SCALE_OFFSET=0.050f;
    		
    public static final char PARBOX_ALIGNMENT='b';//vertical alignment
    
    //parbox height=measured from text baseline. So it could be smaller than total text height (height+depth, 0.36133cm measured from PDF file for fontsize=10pt)
    //It is found when height is set to be <0.282cm, there is always 0.028 cm overlap between two text lines, the same as between plain text lines (not in parbox).
    //When height>0.282cm, a gap shows up between lines with gap=0.923*height-0.2542cm for lines in general comments. Somehow, the gap is smaller in parbox in
    //footnote commments for the same height.
    //So the height here is not the actual height each line occupies (line width). 
    //Actual line width=textheight+gap=0.36133+(0.923*height-0.2542) for lines in general comments.
    //For height=0.300cm, linewidth=0.385cm (general comments), 0.381cm (footnote comments).
    public static final float PARBOX_HEIGHT1=0.300f;//height of parbox for general comments
    public static final float PARBOX_HEIGHT2=0.300f;//height of parbox for footnote comments
    public static final float PARBOX_HEIGHT3=0.300f;//height of parbox for comments in tables
        
    
    public static final float COLUMN_GAP=0.400f;
    
    public static final float POINT2CM=0.035278f;
    
    public static final float CHAR2CM=POINT2CM*5;

    public static final float DIGIT2CM=0.150f;
    public static final float UPLETTER2CM=0.218f;
    public static final float LOLETTER2CM=0.150f;

	public static final int BOTTOM_GAP_NLINES = 2;//number of blnk lines in the gap between text bottom and bottom line
    public static final float UNDERLINE_WIDTH=0.150f;//including space left under the line and before next line
    public static final float TOP_TITLE_LINEWIDTH=0.400f;//420f;
    public static final float TABLE_TITLE_LINEWIDTH=0.400f;

    public static final float TABLE_TITLE_TOTAL_LINEWIDTH=TABLE_TITLE_LINEWIDTH+UNDERLINE_WIDTH;
    /// Configuration file.
    //public static IniFile config=null;
    public static String ensdfFile;
    /// ENSDF file data.
    public static ENSDF ensdf;
    //wrap of ENSDF file
    public static EnsdfTableData etd;
    /// Bands to show, or null for all, empty array for none
    public static Band[] bands;
    /// Keep the text of the ensdf file.
    public static Vector<String> lines;
    /// LaTeX file name.
    public static String latex;
    
    //local directory or url of the directory where pdf linked files are stored
    //used only for web-display for XREF links
    public static String linkDir;
    
    /// Metapost file name.
    public static String mpost;
    /// Number of gammas per level.
    public static int gammasPerLevel=3;
    /// The number of levels below which no gammas are drawn on a band.
    public static int emptyLevelLimit=3;
    /// default chart width (cm)
    private static float chartWidth = DEFAULT_WIDTH-1f;
    /// default chart height (cm)
    private static float chartHeight = DEFAULT_HEIGHT;
    /// charts are in portrait mode
    public static boolean portrait = true;
    /// Angle of level fanning.
    /// 90 is vertical.
    public static float fanAngle = 75f;
    
    public static String tableFormat="none";
    
    public static int labelSize=DEFAULT_LABEL_SIZE;
    public static float bandGap=0.5f;
    
    public static float bandPad=0.5f;
    public static float bandWidths[];
    public static float scaleFactor=DEFAULT_SCALE_FACTOR;//makes bands wider or thinner to get the right amount per page 
    public static float bandLabelScale=DEFAULT_BAND_FONT_SCALE;//changes the size of the font on the bands
    public static float levelLabelScale=DEFAULT_LEVEL_FONT_SCALE;    
    public static float gamamLabelScale=DEFAULT_GAMMA_FONT_SCALE;
    
    /// reset variables to defaults to prevent problems between runs
    public static void reset(){
        latex = mpost = ensdfFile = null;
        bands = null;
        lines = null;
        ensdf = null;
        chartWidth = DEFAULT_WIDTH;
        chartHeight = DEFAULT_HEIGHT;
        labelSize=DEFAULT_LABEL_SIZE;
        bandLabelScale=DEFAULT_BAND_FONT_SCALE;
        levelLabelScale=DEFAULT_LEVEL_FONT_SCALE;
        gamamLabelScale=DEFAULT_GAMMA_FONT_SCALE;
        
        scaleFactor=DEFAULT_SCALE_FACTOR;
        tableFormat="none";
        portrait=true;
        bandGap=0.5f;       
    }
    public static float getChartWidth() {return chartWidth;}
    public static float getChartHeight() {return chartHeight;}
    public static void setChartWidth(float x){chartWidth=x;}
    public static void setChartHeight(float x){chartHeight=x;}
       
    
    
    /**find the width of an ENSDF comment string (in lower-case c)*/
    public static float findWidth(String ensdfStr){
    	float width=0;
    	if(ensdfStr==null)
    		return 0;
    	
    	
    	String str=ensdfStr.replace("\\s\\s\\s", "\\s\\s");// \s=whitespace in LaTex, used in indentation of comment lines   	
    	str=str.replace("\\s"," ");
    	
    	str=str.replace("|)", "->").replace("|(", "<-");//arrows
    	str=str.replace("|.", "|?");//proportional 
    	
    	for(int i=0;i<str.length();i++){
    		char ch=str.charAt(i);
    		if(ch=='}' || ch=='{'){
    			if(ch=='{'&& i<str.length()-1 && str.charAt(i+1)!='E')
    					i+=1;

    			continue;
    		}
    		
    		Float f=NDSConfig.widthMap.get(new Character(ch));
    		if(f==null)
    			width+=NDSConfig.CHAR2CM;
    		else
    		    width+=f.floatValue();
    		
    	}
    	
    	return width;
    }
    
    private static float norm=1.07f;
    public static HashMap<Character,Float> widthMap=new HashMap<Character,Float>();
    
    
    public static String fakeFlagLabel="A";//used for estimating the width of a flag label
    
    //In the pdf output, different characters occupy different widths of spaces
    
    private static char[][] groups1=
       {{'A','D','G','H','K','N','O','Q','U','V','X','Y'},
        {'B','C','R'},
        {'E','L','T','Z'},
        {'F','P','S'},
        {'I'},
        {'J'},
        {'M'},
        {'W'}
       };
    //widths of characters in each group in groups1
    private static float[] widths1=
       {0.216f*norm,
    	0.200f*norm,
    	0.184f*norm,
    	0.166f*norm,
    	0.100f*norm,
    	0.116f*norm,
    	0.264f*norm,
    	0.280f*norm
       };
    
    private static char[][] groups2=
       {{'a','c','e','z'},
    	{'b','d','g','h','k','n','o','p','q','u','v','x','y'},
        {'f'},
        {'i','j','l','t'},
        {'r'},
        {'s'},
        {'m'},
        {'w'}
       };
    //widths of characters in each group in groups2
    private static float[] widths2=
       {0.132f*norm,
    	0.148f*norm,
    	0.092f*norm,
    	0.084f*norm,
    	0.100f*norm,
    	0.116f*norm,
    	0.230f*norm,
    	0.216f*norm
       };
    
    private static char[][] groups3=
        {{'0','1','2','3','4','5','6','7','8','9','#','$','*'},
     	 {' ','!','(',')','-','[',']','~'},
     	 {'+','='},
     	 {'/',':',';'},
     	 {',','.'},
     	 {'\''},
         {'%'},
         {'&',},
         {'@'},
         {'|'}, //following letter is translated into Greet letter
         {'\\'}// escape character
        };
     //widths of characters in each group in groups2
     private static float[] widths3=
        {0.148f*norm,
     	 0.110f*norm,
     	 0.170f*norm,
     	 0.086f*norm,
     	 0.076f*norm,
     	 0.066f*norm,
     	 0.250f*norm,
     	 0.232f*norm,
     	 0.276f*norm,
     	 0.060f*norm,
     	 0.010f*norm
        };
     
    static{
    	for(int i=0;i<groups1.length;i++)
    		for(int j=0;j<groups1[i].length;j++)
    			widthMap.put(groups1[i][j],widths1[i]); 
    	
    	for(int i=0;i<groups2.length;i++)
    		for(int j=0;j<groups2[i].length;j++)
    			widthMap.put(groups2[i][j],widths2[i]); 
    	
       	for(int i=0;i<groups3.length;i++)
    		for(int j=0;j<groups3[i].length;j++)
    			widthMap.put(groups3[i][j],widths3[i]);
       
    }
  

    
    
    
}

