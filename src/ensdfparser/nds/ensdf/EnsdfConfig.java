package ensdfparser.nds.ensdf;

import java.util.*;


/*
 * All column names are defined here.
 * Adapted from EntryDataMap class in Text2ENSDF
 * @author Jun Chen
 * June 23, 2018
 */
public class EnsdfConfig
{

    //inner class for holding column name and its position and len 
    //in ENSDF file
    public static class EntryInfo
    {
        public String name;
        public int pos;
        public int len;
        public String type;//"L","G","B","E","A","D","P","Q","N","PN","X","H"
        public EntryInfo(String name,int pos,int len,String type)
        {
            this.name=name;
            this.pos=pos;//starts from index=0. NOTE that in definitions of record fields below, position index starts from 1
            this.len=len;
            this.type=type;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // All names used in Excel header sheet and data sheet as titles of data columns;
    // and corresponding entry position and length and all other related settings 
    // in ENSDF format
    
    //record width=80   
    public final static int ENSDF_WIDTH=80;
        
    public final static ArrayList<String> letters=new ArrayList<String>(Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
            "p","q","r","s","t","u","v","w","x","y","z"));
    public final static ArrayList<String> digits=new ArrayList<String>(Arrays.asList("0","1","2","3","4","5","6","7","8","9"));

    //the following must have one-to-one correspondence, except for "="
    public final static ArrayList<String> math_relation_OPs=new ArrayList<String>(Arrays.asList("<","<=",">",">=","~","="));
    public final static ArrayList<String> ENSDF_relation_OPs=new ArrayList<String>(Arrays.asList("LT","LE","GT","GE","AP","AP","CA","SY"," "));
    public final static ArrayList<String> ENSDF_relation_OPs_lower=new ArrayList<String>(Arrays.asList("<","|<",">","|>","|?","~","CA","SY"," "));
    
    
    public final static ArrayList<String> pm_OPs=new ArrayList<String>(Arrays.asList("+","-"));
    
    public final static ArrayList<String> mathOperationNames=new ArrayList<String>(Arrays.asList("ADD","SUBTRACT","MULTIPLY","DIVIDE","INVERSE"));
    public final static ArrayList<String> textOperationNames=new ArrayList<String>(Arrays.asList("APPEND","APPENDBEFORE","APPENDAFTER","PREFIX","REPLACE"));
    
    public final static ArrayList<String> continuationMarks=new ArrayList<String>(Arrays.asList("2","B","F","X","M","S","1","3","4","5","6","7","8","9"));
    
    //column 6-8 in ENSDF
    public final static ArrayList<String> recordLineType=new ArrayList<String>(Arrays.asList("L","G","B","E","A","D","Q","P","N","PN","H"));
    
    //non-nuclide beam, like p,n,e
    public static final ArrayList<String> commonBeams=new ArrayList<String>(Arrays.asList("N","P","D","T","A","G","MU","MU+","MU-","MU0",
            "PI","PI+","PI-","PI0","NU","K","K+","K-","K0","E","E+","E-","KAPPA","KAPPA+","KAPPA-"));
      
    public final static ArrayList<String> theoryCCNames=new ArrayList<String>(Arrays.asList("CC","KC","LC","MC","NC","OC","PC","QC","NC+","K/T",
    		"L/T","M/T","N/T","O/T","P/T","Q/T","NP/T"));
		 
    //all ENSDF-line prefixes in lowercase "C" or "D" comments where applicable: column 6-9
    public final static String prefix_empty       ="    ";
    public final static String prefix_general_com =" c  ";
    public final static String prefix_general_doc =" d  ";
    public final static String prefix_level       ="  L ";
    public final static String prefix_gamma       ="  G ";
    public final static String prefix_level_con   ="2 L ";
    public final static String prefix_gamma_con   ="2 G ";
    public final static String prefix_level_com   =" cL ";
    public final static String prefix_gamma_com   =" cG ";
    public final static String prefix_level_doc   =" dL ";
    public final static String prefix_gamma_doc   =" dG ";
    public final static String prefix_level_EM    ="2 L ";
    public final static String prefix_gamma_EM    ="B G ";
    public final static String prefix_level_FLAG  ="F L ";
    public final static String prefix_gamma_FLAG  ="F G ";
    public final static String prefix_gamma_FL    ="F G ";
    public final static String prefix_level_xref  ="X L ";
    public final static String prefix_level_MOM   ="2 L ";
    
    public final static String prefix_decay       ="  B ";
    public final static String prefix_decay_com   =" cB ";//for beta decay by defulat, modify the prefix for other decays
    public final static String prefix_decay_con   ="2 B ";
    public final static String prefix_decay_FLAG  ="F B ";
    public final static String prefix_decay_doc   =" dB ";
    
    public final static String prefix_delay       ="  DP";
    public final static String prefix_delay_com   =" cDP";//for delayed-proton by default
    public final static String prefix_delay_con   ="2 DP";
    public final static String prefix_delay_FLAG  ="F DP";
    public final static String prefix_delay_doc   =" dDP";

    public final static String prefix_level_SL    ="S L ";
    public final static String prefix_gamma_SG    ="S G ";
    public final static String prefix_decay_SB    ="S B ";
    public final static String prefix_decay_SD    ="S DP";
    
            
    public final static String prefix_parent      ="  P ";
    public final static String prefix_norm        ="  N ";
    public final static String prefix_pn          =" PN ";
    public final static String prefix_Q           ="  Q ";

    public final static String prefix_parent_com  =" cP ";
    public final static String prefix_parent_con  ="2 P ";
    public final static String prefix_norm_com    =" cN ";
    public final static String prefix_norm_con    ="2 N ";
    public final static String prefix_pn_com      ="2PN ";
    public final static String prefix_Q_com       =" cQ ";
    public final static String prefix_Q_con       ="2 Q ";

    public final static String prefix_parent_doc  =" dP ";
    public final static String prefix_norm_doc    =" dN ";
    public final static String prefix_pn_doc      ="dPN ";
    public final static String prefix_Q_doc       =" dQ ";

    public final static String prefix_history     ="  H ";
    public final static String prefix_xref        ="X L ";
    
    //note types col=7-8 (7-9 for delay type)
    //note includes comment and document records
    public final static ArrayList<String> levelNoteTypes=new ArrayList<String>(Arrays.asList("CL","DL")); 
    public final static ArrayList<String> gammaNoteTypes=new ArrayList<String>(Arrays.asList("CG","DG")); 
    public final static ArrayList<String> decayNoteTypes=new ArrayList<String>(Arrays.asList("CA","CE","CB","DA","DE","DB")); 
    public final static ArrayList<String> delayNoteTypes=new ArrayList<String>(Arrays.asList("CDP","CDN","CDA","CD","DDP","DDN","DDA","DD")); 
    public final static ArrayList<String> otherNoteTypes=new ArrayList<String>(Arrays.asList("CN","DN","CQ","DQ","CP","DP","CPN","DPN")); 

    
    public static ArrayList<String> allNoteTypes=new ArrayList<String>();
    static{
        allNoteTypes.addAll(levelNoteTypes);
        allNoteTypes.addAll(gammaNoteTypes);
        allNoteTypes.addAll(decayNoteTypes);
        allNoteTypes.addAll(delayNoteTypes);
        allNoteTypes.addAll(otherNoteTypes);
    }
    
    public static LinkedHashMap<String,EntryInfo> levelFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> gammaFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> decayFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> delayFieldMap=new LinkedHashMap<String,EntryInfo>();

    public static LinkedHashMap<String,EntryInfo> parentFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> QRecordFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> NRecordFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> PNRecordFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> normFieldMap=new LinkedHashMap<String,EntryInfo>();//N record+PN record
    
    public static LinkedHashMap<String,EntryInfo> levelEMMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> levelMOMMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> levelWidthMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> gammaEMMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> iccFieldMap=new LinkedHashMap<String,EntryInfo>();
    public static LinkedHashMap<String,EntryInfo> angularFieldMap=new LinkedHashMap<String,EntryInfo>();
    
    public static LinkedHashMap<String,EntryInfo> allFieldsMap=new LinkedHashMap<String,EntryInfo>();
    
    
    private static LinkedHashMap<String,EntryInfo> makeMap(ArrayList<String> entryFields,String type){
        LinkedHashMap<String,EntryInfo> out=new LinkedHashMap<String,EntryInfo>();
        for(int i=0;i<entryFields.size();i++){
            String s=entryFields.get(i);
            String[] v=s.split("[:(,)]+");
            try{
                String name=v[0];

                int pos=Integer.parseInt(v[1]);
                
                //NOTE pos in EntryInfo starts from index=0, while the index in field definition starts from 1
                if(pos>0)
                    pos=pos-1;
                
                int len=Integer.parseInt(v[2]);
                out.put(name, new EntryInfo(name,pos,len,type));
            }catch(Exception e){
                continue;
            }
        }
        return out;
    }
    
	public static LinkedHashMap<String,Integer> makeNameLenMap(Vector<String> namesV){
		LinkedHashMap<String,Integer> nameLenMap=new LinkedHashMap<String,Integer>();
		try {
			for(String name:namesV) {
				int len=entryLen(name);
				int dsLen=entryLen("D"+name);	
				if(dsLen>0)
					len+=dsLen;
				
				if(len<0)
					len=12;
				else
					len+=1;
				
				nameLenMap.put(name, len);
			}
		}catch(Exception e) {
			
		}
		return nameLenMap;
	}
	
    /*
     * general names
     */
    public final static String UDN_NAME="UDN",UDD_NAME="UDD",UDU_NAME="UDU", COM_DATA_NAME="CD";
    
            
    /*
     * DSID fields
     */
    public static LinkedHashMap<String,EntryInfo> dsidFieldMap=new LinkedHashMap<String,EntryInfo>();
    
    public final static String DSID_NAME="DSID",REF_NAME="KEYNO",PUB_NAME="PUB",DATE_NAME="DATE";
    private static ArrayList<String> dsidFields=new ArrayList<String>(Arrays.asList(
            DSID_NAME+":(10,30)",REF_NAME+":(40,26)",PUB_NAME+":(66,9)",DATE_NAME+":(75,6)" 
    )); 
    static{
        dsidFieldMap=makeMap(dsidFields," ");
    }
    
    /*
     * atomic data fields
     */
    public static LinkedHashMap<String,EntryInfo> atomicFieldMap=new LinkedHashMap<String,EntryInfo>();
    
    public final static String ATOMIC_TYPE="TYPE",IM_NAME="IM";
    private static ArrayList<String> atomicFields=new ArrayList<String>(Arrays.asList(
            IM_NAME+":(-1,-1)" 
    )); 
    static{
        atomicFieldMap=makeMap(atomicFields," ");
    }
    
    /*
     * Level fields
     * format: "field_name:(col//,length) (here col# starts from 1 not 0)
     */
    public final static String 
            EL_NAME="EL",DEL_NAME="DEL",JPI_NAME="JPI",T_NAME="T",DT_NAME="DT",TU_NAME="TU",L_NAME="L",
            S_NAME="S",DS_NAME="DS",
            MS_NAME="MS", //mark for meta-stable state
            ISPIN_NAME="ISPIN",ISPINZ_NAME="ISPINZ",
            GF_NAME="GF",DGF_NAME="DGF",GF_REF="GF_REF", //g-factor
            MODE_NAME="MODE",//decay modes
            LFLAG_NAME="LFLAG", //level flags
            BAND_NAME="BAND", SEQ_NAME="SEQ",//band and sequence flags
            CL_NAME="CL",DL_NAME="DL",SL_NAME="SL",   // general level comment and level document record
            CONF_NAME="CONF",//configuration
            LQUE_NAME="LQUE",//question marks for level, 
            LCD_NAME="L"+COM_DATA_NAME,DLCD_NAME="D"+LCD_NAME,LCD_REF=LCD_NAME+"_REF",LCD_LABEL=LCD_NAME+"_LABEL",
            LCD1_NAME=LCD_NAME+"1",DLCD1_NAME=DLCD_NAME+"1",LCD1_REF=LCD1_NAME+"_REF",LCD1_LABEL=LCD1_NAME+"_LABEL",
            LCD2_NAME=LCD_NAME+"2",DLCD2_NAME=DLCD_NAME+"2",LCD2_REF=LCD2_NAME+"_REF",LCD2_LABEL=LCD2_NAME+"_LABEL",
            LCD3_NAME=LCD_NAME+"3",DLCD3_NAME=DLCD_NAME+"3",LCD3_REF=LCD3_NAME+"_REF",LCD3_LABEL=LCD3_NAME+"_LABEL",
            LCD4_NAME=LCD_NAME+"4",DLCD4_NAME=DLCD_NAME+"4",LCD4_REF=LCD4_NAME+"_REF",LCD4_LABEL=LCD4_NAME+"_LABEL",
            LCD5_NAME=LCD_NAME+"5",DLCD5_NAME=DLCD_NAME+"5",LCD5_REF=LCD5_NAME+"_REF",LCD5_LABEL=LCD5_NAME+"_LABEL", //for data to be put in comment records starting with "$"
            LCD6_NAME=LCD_NAME+"6",DLCD6_NAME=DLCD_NAME+"1",LCD6_REF=LCD6_NAME+"_REF",LCD6_LABEL=LCD6_NAME+"_LABEL",
            LCD7_NAME=LCD_NAME+"7",DLCD7_NAME=DLCD_NAME+"2",LCD7_REF=LCD7_NAME+"_REF",LCD7_LABEL=LCD7_NAME+"_LABEL",
            LCD8_NAME=LCD_NAME+"8",DLCD8_NAME=DLCD_NAME+"3",LCD8_REF=LCD8_NAME+"_REF",LCD8_LABEL=LCD8_NAME+"_LABEL",
            LCD9_NAME=LCD_NAME+"9",DLCD9_NAME=DLCD_NAME+"4",LCD9_REF=LCD9_NAME+"_REF",LCD9_LABEL=LCD9_NAME+"_LABEL",
            LUDN_NAME="L"+UDN_NAME,LUDD_NAME="L"+UDD_NAME,DLUDD_NAME="D"+LUDD_NAME,LUDU_NAME="L"+UDU_NAME,//user-defined record name, data value, uncertainty, unit    
                      LUD_REF=LUDN_NAME+"_REF",
            LUDN1_NAME=LUDN_NAME+"1",LUDD1_NAME=LUDD_NAME+"1",DLUDD1_NAME=DLUDD_NAME+"1",LUDU1_NAME=LUDU_NAME+"1",//user-defined record name, data value, uncertainty, unit  
                      LUD1_REF=LUDN1_NAME+"_REF",
            LUDN2_NAME=LUDN_NAME+"2",LUDD2_NAME=LUDD_NAME+"2",DLUDD2_NAME=DLUDD_NAME+"2",LUDU2_NAME=LUDU_NAME+"2",
                      LUD2_REF=LUDN2_NAME+"_REF",
            LUDN3_NAME=LUDN_NAME+"3",LUDD3_NAME=LUDD_NAME+"3",DLUDD3_NAME=DLUDD_NAME+"3",LUDU3_NAME=LUDU_NAME+"3", 
                      LUD3_REF=LUDN3_NAME+"_REF",
            XREF_NAME="XREF",                   
            //deformation parameters                 
            B2_NAME="B2",DB2_NAME="DB2",B3_NAME="B3",DB3_NAME="DB3",B4_NAME="B4",DB4_NAME="DB4",B5_NAME="B5",DB5_NAME="DB5",   
            B6_NAME="B6",DB6_NAME="DB6",
            B2_REF=B2_NAME+"_REF",B3_REF=B3_NAME+"_REF",B4_REF=B4_NAME+"_REF",B5_REF=B5_NAME+"_REF",B6_REF=B6_NAME+"_REF";
    
    private static ArrayList<String> levelFields=new ArrayList<String>(Arrays.asList(
            EL_NAME+":(10,10)",DEL_NAME+":(20,2)",JPI_NAME+":(22,18)",T_NAME+":(40,10)",DT_NAME+":(50,6)",TU_NAME+":(-1,-1)",L_NAME+":(56,9)",
            S_NAME+":(65,10)",DS_NAME+":(75,2)",
            MS_NAME+":(78,2)", //mark for meta-stable state
            ISPIN_NAME+":(-1,-1)",ISPINZ_NAME+":(-1,-1)",
            GF_NAME+":(-1,-1)",DGF_NAME+":(-1,-1)",GF_REF+":(-1,-1)", //g-factor
            MODE_NAME+":(-1,-1)",//decay modes
            LFLAG_NAME+":(77,1)", //level flags
            BAND_NAME+":(77,1)", SEQ_NAME+":(77,1)",//band and sequence flags
            CL_NAME+":(-1,-1)",DL_NAME+":(-1,-1)",SL_NAME+":(-1,-1)",   // general level comment and level document record
            CONF_NAME+"(-1,-1)",//configuration
            LQUE_NAME+":(80,1)",//question marks for level, 
            LCD_NAME+":(-1,-1)",DLCD_NAME+":(-1,-1)",
            LCD1_NAME+":(-1,-1)",DLCD1_NAME+":(-1,-1)",LCD2_NAME+":(-1,-1)",DLCD2_NAME+":(-1,-1)",
            LCD3_NAME+":(-1,-1)",DLCD3_NAME+":(-1,-1)",LCD4_NAME+":(-1,-1)",DLCD4_NAME+":(-1,-1)",
            LCD5_NAME+":(-1,-1)",DLCD5_NAME+":(-1,-1)", //for data to be put in comment records starting with "$"
            LCD6_NAME+":(-1,-1)",DLCD6_NAME+":(-1,-1)",LCD7_NAME+":(-1,-1)",DLCD7_NAME+":(-1,-1)",
            LCD8_NAME+":(-1,-1)",DLCD8_NAME+":(-1,-1)",LCD9_NAME+":(-1,-1)",DLCD9_NAME+":(-1,-1)",
            LCD_LABEL+":(-1,-1)",LCD1_LABEL+":(-1,-1)",LCD2_LABEL+":(-1,-1)",LCD3_LABEL+":(-1,-1)",LCD4_LABEL+":(-1,-1)",LCD5_LABEL+":(-1,-1)",//to be put before LCD_NAME
            LCD6_LABEL+":(-1,-1)",LCD7_LABEL+":(-1,-1)",LCD8_LABEL+":(-1,-1)",LCD9_LABEL+":(-1,-1)",
            LCD_REF+":(-1,-1)",LCD1_REF+":(-1,-1)",LCD2_REF+":(-1,-1)",LCD3_REF+":(-1,-1)",LCD4_REF+":(-1,-1)",LCD5_REF+":(-1,-1)",
            LCD6_REF+":(-1,-1)",LCD7_REF+":(-1,-1)",LCD8_REF+":(-1,-1)",LCD9_REF+":(-1,-1)",
            LUDN_NAME+":(-1,-1)",LUDD_NAME+":(-1,-1)",DLUDD_NAME+":(-1,-1)",LUDU_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit      
            LUDN1_NAME+":(-1,-1)",LUDD1_NAME+":(-1,-1)",DLUDD1_NAME+":(-1,-1)",LUDU1_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit  
            LUDN2_NAME+":(-1,-1)",LUDD2_NAME+":(-1,-1)",DLUDD2_NAME+":(-1,-1)",LUDU2_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit  
            LUDN3_NAME+":(-1,-1)",LUDD3_NAME+":(-1,-1)",DLUDD3_NAME+":(-1,-1)",LUDU2_NAME+":(-1,-1)",
            LUD_REF+":(-1,-1)",LUD1_REF+":(-1,-1)",LUD2_REF+":(-1,-1)",LUD3_REF+":(-1,-1)",
            XREF_NAME+":(-1,-1)",
            //deformation parameters                 
            B2_NAME+":(-1,-1)",DB2_NAME+":(-1,-1)",B3_NAME+":(-1,-1)",DB3_NAME+":(-1,-1)",B4_NAME+":(-1,-1)",DB4_NAME+":(-1,-1)",
            B5_NAME+":(-1,-1)",DB5_NAME+":(-1,-1)",B6_NAME+":(-1,-1)",DB6_NAME+":(-1,-1)",
            B2_REF+":(-1,-1)",B3_REF+":(-1,-1)",B4_REF+":(-1,-1)",B5_REF+":(-1,-1)",B6_REF+":(-1,-1)"
    ));
    
    public final static String          
            BE1UP_NAME="BE1UP",DBE1UP_NAME="DBE1UP",BE2UP_NAME="BE2UP",DBE2UP_NAME="DBE2UP",BE3UP_NAME="BE3UP",DBE3UP_NAME="DBE3UP",
            BE4UP_NAME="BE4UP",DBE4UP_NAME="DBE4UP",BE5UP_NAME="BE5UP",DBE5UP_NAME="DBE5UP",BE6UP_NAME="BE6UP",DBE6UP_NAME="DBE6UP",
            BE7UP_NAME="BE7UP",DBE7UP_NAME="DBE7UP",BE8UP_NAME="BE8UP",DBE8UP_NAME="DBE8UP",
            BM1UP_NAME="BM1UP",DBM1UP_NAME="DBM1UP",BM2UP_NAME="BM2UP",DBM2UP_NAME="DBM2UP",BM3UP_NAME="BM3UP",DBM3UP_NAME="DBM3UP",
            BM4UP_NAME="BM4UP",DBM4UP_NAME="DBM4UP",BM5UP_NAME="BM5UP",DBM5UP_NAME="DBM5UP",BM6UP_NAME="BM6UP",DBM6UP_NAME="DBM6UP",
            BM7UP_NAME="BM7UP",DBM7UP_NAME="DBM7UP",BM8UP_NAME="BM8UP",DBM8UP_NAME="DBM8UP",
           
            BE1WUP_NAME="BE1WUP",DBE1WUP_NAME="DBE1WUP",BE2WUP_NAME="BE2WUP",DBE2WUP_NAME="DBE2WUP",BE3WUP_NAME="BE3WUP",DBE3WUP_NAME="DBE3WUP",
            BE4WUP_NAME="DBE4WUP",DBE4WUP_NAME="DBE4WUP",BE5WUP_NAME="BE5WUP",DBE5WUP_NAME="DBE5WUP",BE6WUP_NAME="BE6WUP",DBE6WUP_NAME="DBE6WUP",
            BE7WUP_NAME="BE7WUP",DBE7WUP_NAME="DBE7WUP",BE8WUP_NAME="BE8WUP",DBE8WUP_NAME="DBE8WUP",
           
            BM1WUP_NAME="BM1WUP",DBM1WUP_NAME="DBM1WUP",BM2WUP_NAME="BM2WUP",DBM2WUP_NAME="DBM2WUP",BM3WUP_NAME="BM3WUP",DBM3WUP_NAME="DBM3WUP",
            BM4WUP_NAME="DBM4WUP",DBM4WUP_NAME="DBM4WUP",BM5WUP_NAME="BM5WUP",DBM5WUP_NAME="DBM5WUP",BM6WUP_NAME="BM6WUP",DBM6WUP_NAME="DBM6WUP",
            BM7WUP_NAME="BM7WUP",DBM7WUP_NAME="DBM7WUP",BM8WUP_NAME="BM8WUP",DBM8WUP_NAME="DBM8WUP",
            
            BE1UP_REF="BE1UP_REF",BE2UP_REF="BE2UP_REF",BE3UP_REF="BE3UP_REF",BE4UP_REF="BE4UP_REF",BE5UP_REF="BE5UP_REF",
            		BE6UP_REF="BE6UP_REF",BE7UP_REF="BE7UP_REF",BE8UP_REF="BE8UP_REF",
            BM1UP_REF="BM1UP_REF",BM2UP_REF="BM2UP_REF",BM3UP_REF="BM3UP_REF",BM4UP_REF="BM4UP_REF",BM5UP_REF="BM5UP_REF",
                    BM6UP_REF="BM6UP_REF",BM7UP_REF="BM7UP_REF",BM8UP_REF="BM8UP_REF";
    private static ArrayList<String> levelEMs=new ArrayList<String>(Arrays.asList(
            BE1UP_NAME+":(-1,-1)",DBE1UP_NAME+":(-1,-1)",BE2UP_NAME+":(-1,-1)",DBE2UP_NAME+":(-1,-1)",BE3UP_NAME+":(-1,-1)",DBE3UP_NAME+":(-1,-1)",
            BE4UP_NAME+":(-1,-1)",DBE4UP_NAME+":(-1,-1)",BE5UP_NAME+":(-1,-1)",DBE5UP_NAME+":(-1,-1)",BE6UP_NAME+":(-1,-1)",DBE6UP_NAME+":(-1,-1)",
            BE7UP_NAME+":(-1,-1)",DBE7UP_NAME+":(-1,-1)",BE8UP_NAME+":(-1,-1)",DBE8UP_NAME+":(-1,-1)",
            BM1UP_NAME+":(-1,-1)",DBM1UP_NAME+":(-1,-1)",BM2UP_NAME+":(-1,-1)",DBM2UP_NAME+":(-1,-1)",BM3UP_NAME+":(-1,-1)",DBM3UP_NAME+":(-1,-1)",
            BM4UP_NAME+":(-1,-1)",DBM4UP_NAME+":(-1,-1)",BM5UP_NAME+":(-1,-1)",DBM5UP_NAME+":(-1,-1)",BM6UP_NAME+":(-1,-1)",DBM6UP_NAME+":(-1,-1)",
            BM7UP_NAME+":(-1,-1)",DBM7UP_NAME+":(-1,-1)",BM8UP_NAME+":(-1,-1)",DBM8UP_NAME+":(-1,-1)",
            
            BE1WUP_NAME+":(-1,-1)",DBE1WUP_NAME+":(-1,-1)",BE2WUP_NAME+":(-1,-1)",DBE2WUP_NAME+":(-1,-1)",BE3WUP_NAME+":(-1,-1)",DBE3WUP_NAME+":(-1,-1)",
            DBE4WUP_NAME+":(-1,-1)",DBE4WUP_NAME+":(-1,-1)",BE5WUP_NAME+":(-1,-1)",DBE5WUP_NAME+":(-1,-1)",BE6WUP_NAME+":(-1,-1)",DBE6WUP_NAME+":(-1,-1)",
            BE7WUP_NAME+":(-1,-1)",DBE7WUP_NAME+":(-1,-1)",BE8WUP_NAME+":(-1,-1)",DBE8WUP_NAME+":(-1,-1)",
            
            BM1WUP_NAME+":(-1,-1)",DBM1WUP_NAME+":(-1,-1)",BM2WUP_NAME+":(-1,-1)",DBM2WUP_NAME+":(-1,-1)",BM3WUP_NAME+":(-1,-1)",DBM3WUP_NAME+":(-1,-1)",
            DBM4WUP_NAME+":(-1,-1)",DBM4WUP_NAME+":(-1,-1)",BM5WUP_NAME+":(-1,-1)",DBM5WUP_NAME+":(-1,-1)",BM6WUP_NAME+":(-1,-1)",DBM6WUP_NAME+":(-1,-1)",
            BM7WUP_NAME+":(-1,-1)",DBM7WUP_NAME+":(-1,-1)",BM8WUP_NAME+":(-1,-1)",DBM8WUP_NAME+":(-1,-1)",
            BE1UP_REF+":(-1,-1)",BE2UP_REF+":(-1,-1)",BE3UP_REF+":(-1,-1)",BE4UP_REF+":(-1,-1)",BE5UP_REF+":(-1,-1)",BE6UP_REF+":(-1,-1)",BE7UP_REF+":(-1,-1)",BE8UP_REF+":(-1,-1)",
            BM1UP_REF+":(-1,-1)",BM2UP_REF+":(-1,-1)",BM3UP_REF+":(-1,-1)",BM4UP_REF+":(-1,-1)",BM5UP_REF+":(-1,-1)",BM6UP_REF+":(-1,-1)",BM7UP_REF+":(-1,-1)",BM8UP_REF+":(-1,-1)"

    ));
    
    public final static String      
            MOMM1_NAME="MOMM1",DMOMM1_NAME="DMOMM1",MOMM2_NAME="MOMM2",DMOMM2_NAME="DMOMM2",MOMM3_NAME="MOMM3",DMOMM3_NAME="DMOMM3",
            MOME1_NAME="MOME1",DMOME1_NAME="DMOME1",MOME2_NAME="MOME2",DMOME2_NAME="DMOME2",MOME3_NAME="MOME3",DMOME3_NAME="DMOME3",
            MOMM1_REF="MOMM1_REF",MOMM2_REF="MOMM2_REF",MOMM3_REF="MOMM3_REF",
            MOME1_REF="MOME1_REF",MOME2_REF="MOME2_REF",MOME3_REF="MOME3_REF";


    private static ArrayList<String> levelMOMs=new ArrayList<String>(Arrays.asList(
            MOMM1_NAME+":(-1,-1)",DMOMM1_NAME+":(-1,-1)",MOMM2_NAME+":(-1,-1)",DMOMM2_NAME+":(-1,-1)",MOMM3_NAME+":(-1,-1)",DMOMM3_NAME+":(-1,-1)",
            MOME1_NAME+":(-1,-1)",DMOME1_NAME+":(-1,-1)",MOME2_NAME+":(-1,-1)",DMOME2_NAME+":(-1,-1)",MOME3_NAME+":(-1,-1)",DMOME3_NAME+":(-1,-1)",
            MOMM1_REF+":(-1,-1)",MOMM2_REF+":(-1,-1)",MOMM3_REF+":(-1,-1)",
            MOME1_REF+":(-1,-1)",MOME2_REF+":(-1,-1)",MOME3_REF+":(-1,-1)"
    ));
    
    public final static String  
            WIDTH_NAME="WIDTH",DWIDTH_NAME="DWIDTH",WIDTH_REF="WIDTH_REF",WIDTH_UNIT="WIDTH_UNIT",//total width
            WIDTHG_NAME="WIDTHG",DWIDTHG_NAME="DWIDTHG",WIDTHG_REF="WIDTHG_REF",WIDTHG_UNIT="WIDTHG_UNIT",
            WIDTHG0_NAME="WIDTHG0",DWIDTHG0_NAME="DWIDTHG0",WIDTHG0_REF="WIDTHG0_REF",WIDTHG0_UNIT="WIDTHG0_UNIT",
            WIDTHG1_NAME="WIDTHG1",DWIDTHG1_NAME="DWIDTHG1",WIDTHG1_REF="WIDTHG1_REF",WIDTHG1_UNIT="WIDTHG1_UNIT",
            WIDTHG2_NAME="WIDTHG2",DWIDTHG2_NAME="DWIDTHG2",WIDTHG2_REF="WIDTHG2_REF",WIDTHG2_UNIT="WIDTHG2_UNIT",//gamma width
            WIDTHG3_NAME="WIDTHG2",DWIDTHG3_NAME="DWIDTHG2",WIDTHG3_REF="WIDTHG2_REF",WIDTHG3_UNIT="WIDTHG2_UNIT",//gamma width
            WIDTHN_NAME="WIDTHN",DWIDTHN_NAME="DWIDTHN",WIDTHN_REF="WIDTHN_REF",WIDTHN_UNIT="WIDTHN_UNIT",//neutron width
            WIDTHN0_NAME="WIDTHN0",DWIDTHN0_NAME="DWIDTHN0",WIDTHN0_REF="WIDTHN0_REF",WIDTHN0_UNIT="WIDTHN0_UNIT",//neutron width
            WIDTHN1_NAME="WIDTHN1",DWIDTHN1_NAME="DWIDTHN1",WIDTHN1_REF="WIDTHN1_REF",WIDTHN1_UNIT="WIDTHN1_UNIT",//neutron width
            WIDTHN2_NAME="WIDTHN2",DWIDTHN2_NAME="DWIDTHN2",WIDTHN2_REF="WIDTHN2_REF",WIDTHN2_UNIT="WIDTHN2_UNIT",//neutron width
            WIDTHN3_NAME="WIDTHN3",DWIDTHN3_NAME="DWIDTHN3",WIDTHN3_REF="WIDTHN3_REF",WIDTHN3_UNIT="WIDTHN3_UNIT",//neutron width                  
            WIDTHP_NAME="WIDTHP",DWIDTHP_NAME="DWIDTHP",WIDTHP_REF="WIDTHP_REF",WIDTHP_UNIT="WIDTHP_UNIT",//proton width
            WIDTHP0_NAME="WIDTHP0",DWIDTHP0_NAME="DWIDTHP0",WIDTHP0_REF="WIDTHP0_REF",WIDTHP0_UNIT="WIDTHP0_UNIT",//proton width
            WIDTHP1_NAME="WIDTHP1",DWIDTHP1_NAME="DWIDTHP1",WIDTHP1_REF="WIDTHP1_REF",WIDTHP1_UNIT="WIDTHP1_UNIT",//proton width
            WIDTHP2_NAME="WIDTHP2",DWIDTHP2_NAME="DWIDTHP2",WIDTHP2_REF="WIDTHP2_REF",WIDTHP2_UNIT="WIDTHP2_UNIT",//proton width
            WIDTHP3_NAME="WIDTHP3",DWIDTHP3_NAME="DWIDTHP3",WIDTHP3_REF="WIDTHP3_REF",WIDTHP3_UNIT="WIDTHP3_UNIT",//proton width
            WIDTHA_NAME="WIDTHA",DWIDTHA_NAME="DWIDTHA",WIDTHA_REF="WIDTHA_REF",WIDTHA_UNIT="WIDTHA_UNIT",//alpha width
            WIDTHA0_NAME="WIDTHA0",DWIDTHA0_NAME="DWIDTHA0",WIDTHA0_REF="WIDTHA0_REF",WIDTHA0_UNIT="WIDTHA0_UNIT",//alpha width
            WIDTHA1_NAME="WIDTHA1",DWIDTHA1_NAME="DWIDTHA1",WIDTHA1_REF="WIDTHA1_REF",WIDTHA1_UNIT="WIDTHA1_UNIT",//alpha width
            WIDTHA2_NAME="WIDTHA2",DWIDTHA2_NAME="DWIDTHA2",WIDTHA2_REF="WIDTHA2_REF",WIDTHA2_UNIT="WIDTHA2_UNIT",//alpha width
            WIDTHA3_NAME="WIDTHA3",DWIDTHA3_NAME="DWIDTHA3",WIDTHA3_REF="WIDTHA3_REF",WIDTHA3_UNIT="WIDTHA3_UNIT";//alpha width
    
    private static ArrayList<String> levelWidths=new ArrayList<String>(Arrays.asList(
            WIDTH_NAME+":(-1,-1)",DWIDTH_NAME+":(-1,-1)",WIDTH_REF+":(-1,-1)",WIDTH_UNIT+":(-1,-1)",//total width
            WIDTHG_NAME+":(-1,-1)",DWIDTHG_NAME+":(-1,-1)",WIDTHG_REF+":(-1,-1)",WIDTHG_UNIT+":(-1,-1)",
            WIDTHG0_NAME+":(-1,-1)",DWIDTHG0_NAME+":(-1,-1)",WIDTHG0_REF+":(-1,-1)",WIDTHG0_UNIT+":(-1,-1)",
            WIDTHG1_NAME+":(-1,-1)",DWIDTHG1_NAME+":(-1,-1)",WIDTHG1_REF+":(-1,-1)",WIDTHG1_UNIT+":(-1,-1)",
            WIDTHG2_NAME+":(-1,-1)",DWIDTHG2_NAME+":(-1,-1)",WIDTHG2_REF+":(-1,-1)",WIDTHG2_UNIT+":(-1,-1)",//gamma width
            WIDTHG3_NAME+":(-1,-1)",DWIDTHG3_NAME+":(-1,-1)",WIDTHG3_REF+":(-1,-1)",WIDTHG3_UNIT+":(-1,-1)",//gamma width
            WIDTHN_NAME+":(-1,-1)",DWIDTHN_NAME+":(-1,-1)",WIDTHN_REF+":(-1,-1)",WIDTHN_UNIT+":(-1,-1)",//neutron width
            WIDTHN0_NAME+":(-1,-1)",DWIDTHN0_NAME+":(-1,-1)",WIDTHN0_REF+":(-1,-1)",WIDTHN0_UNIT+":(-1,-1)",
            WIDTHN1_NAME+":(-1,-1)",DWIDTHN1_NAME+":(-1,-1)",WIDTHN1_REF+":(-1,-1)",WIDTHN1_UNIT+":(-1,-1)",
            WIDTHN2_NAME+":(-1,-1)",DWIDTHN2_NAME+":(-1,-1)",WIDTHN2_REF+":(-1,-1)",WIDTHN2_UNIT+":(-1,-1)",
            WIDTHN3_NAME+":(-1,-1)",DWIDTHN3_NAME+":(-1,-1)",WIDTHN3_REF+":(-1,-1)",WIDTHN3_UNIT+":(-1,-1)",           
            WIDTHP_NAME+":(-1,-1)",DWIDTHP_NAME+":(-1,-1)",WIDTHP_REF+":(-1,-1)",WIDTHP_UNIT+":(-1,-1)",//proton width
            WIDTHP0_NAME+":(-1,-1)",DWIDTHP0_NAME+":(-1,-1)",WIDTHP0_REF+":(-1,-1)",WIDTHP0_UNIT+":(-1,-1)",
            WIDTHP1_NAME+":(-1,-1)",DWIDTHP1_NAME+":(-1,-1)",WIDTHP1_REF+":(-1,-1)",WIDTHP1_UNIT+":(-1,-1)",
            WIDTHP2_NAME+":(-1,-1)",DWIDTHP2_NAME+":(-1,-1)",WIDTHP2_REF+":(-1,-1)",WIDTHP2_UNIT+":(-1,-1)",
            WIDTHP3_NAME+":(-1,-1)",DWIDTHP3_NAME+":(-1,-1)",WIDTHP3_REF+":(-1,-1)",WIDTHP3_UNIT+":(-1,-1)",           
            WIDTHA_NAME+":(-1,-1)",DWIDTHA_NAME+":(-1,-1)",WIDTHA_REF+":(-1,-1)",WIDTHA_UNIT+":(-1,-1)",//alpha width
            WIDTHA0_NAME+":(-1,-1)",DWIDTHA0_NAME+":(-1,-1)",WIDTHA0_REF+":(-1,-1)",WIDTHA0_UNIT+":(-1,-1)",
            WIDTHA1_NAME+":(-1,-1)",DWIDTHA1_NAME+":(-1,-1)",WIDTHA1_REF+":(-1,-1)",WIDTHA1_UNIT+":(-1,-1)",
            WIDTHA2_NAME+":(-1,-1)",DWIDTHA2_NAME+":(-1,-1)",WIDTHA2_REF+":(-1,-1)",WIDTHA2_UNIT+":(-1,-1)",
            WIDTHA3_NAME+":(-1,-1)",DWIDTHA3_NAME+":(-1,-1)",WIDTHA3_REF+":(-1,-1)",WIDTHA3_UNIT+":(-1,-1)"           

    ));

    
    static{
        levelFields.addAll(levelEMs);
        levelFields.addAll(levelMOMs);
        levelFields.addAll(levelWidths);
        
        levelEMMap=makeMap(levelEMs,"L");
        levelMOMMap=makeMap(levelMOMs,"L");
        levelWidthMap=makeMap(levelWidths,"L");
        levelFieldMap=makeMap(levelFields,"L");
    }
    

    /*
     * Gamma fields
     */
    
    public final static String          
            EG_NAME="EG",DEG_NAME="DEG",RI_NAME="RI",DRI_NAME="DRI",MUL_NAME="MUL",MR_NAME="MR",DMR_NAME="DMR",
            A2_NAME="A2",DA2_NAME="DA2",A2_REF="A2_REF",A4_NAME="A4",DA4_NAME="DA4",A4_REF="A4_REF",
                    A6_NAME="A6",DA6_NAME="DA6",A6_REF="A6_REF",
            CG_NAME="CG", DG_NAME="DG",SG_NAME="SG",//general gamma comment and gamma document record
            TI_NAME="TI",DTI_NAME="DTI",
            CC_NAME="CC",DCC_NAME="DCC",CC_REF="CC_REF",
            DCO_NAME="DCO",DDCO_NAME="DDCO",DCO_REF="DCO_REF",POL_NAME="POL",DPOL_NAME="DPOL",POL_REF="POL_REF",
            GFLAG_NAME="GFLAG",GCOIN_NAME="GCOIN",GQUE_NAME="GQUE",FL_NAME="FL",    
            GCD_NAME="G"+COM_DATA_NAME,DGCD_NAME="D"+GCD_NAME,GCD_REF=GCD_NAME+"_REF",GCD_LABEL=GCD_NAME+"_LABEL",
            GCD1_NAME=GCD_NAME+"1",DGCD1_NAME=DGCD_NAME+"1",GCD1_REF=GCD1_NAME+"_REF",GCD1_LABEL=GCD1_NAME+"_LABEL",
            GCD2_NAME=GCD_NAME+"2",DGCD2_NAME=DGCD_NAME+"2",GCD2_REF=GCD2_NAME+"_REF",GCD2_LABEL=GCD2_NAME+"_LABEL",
            GCD3_NAME=GCD_NAME+"3",DGCD3_NAME=DGCD_NAME+"3",GCD3_REF=GCD3_NAME+"_REF",GCD3_LABEL=GCD3_NAME+"_LABEL",
            GCD4_NAME=GCD_NAME+"4",DGCD4_NAME=DGCD_NAME+"4",GCD4_REF=GCD4_NAME+"_REF",GCD4_LABEL=GCD4_NAME+"_LABEL",
            GCD5_NAME=GCD_NAME+"5",DGCD5_NAME=DGCD_NAME+"5",GCD5_REF=GCD5_NAME+"_REF",GCD5_LABEL=GCD5_NAME+"_LABEL",
            GCD6_NAME=GCD_NAME+"6",DGCD6_NAME=DGCD_NAME+"6",GCD6_REF=GCD6_NAME+"_REF",GCD6_LABEL=GCD6_NAME+"_LABEL",
            GCD7_NAME=GCD_NAME+"7",DGCD7_NAME=DGCD_NAME+"7",GCD7_REF=GCD7_NAME+"_REF",GCD7_LABEL=GCD7_NAME+"_LABEL",
            GCD8_NAME=GCD_NAME+"8",DGCD8_NAME=DGCD_NAME+"8",GCD8_REF=GCD8_NAME+"_REF",GCD8_LABEL=GCD8_NAME+"_LABEL",            
            GCD9_NAME=GCD_NAME+"9",DGCD9_NAME=DGCD_NAME+"9",GCD9_REF=GCD9_NAME+"_REF",GCD9_LABEL=GCD9_NAME+"_LABEL", //for data to be put in comment records starting with "$"
            GUDN_NAME="G"+UDN_NAME,GUDD_NAME="G"+UDD_NAME,DGUDD_NAME="D"+GUDD_NAME,GUDU_NAME="G"+UDU_NAME,//user-defined record name, data value, uncertainty, unit      
                    GUD_REF=GUDN_NAME+"_REF",
            GUDN1_NAME=GUDN_NAME+"1",GUDD1_NAME=GUDD_NAME+"1",DGUDD1_NAME=DGUDD_NAME+"1",GUDU1_NAME=GUDU_NAME+"1",//user-defined record name, data value, uncertainty, unit  
                    GUD1_REF=GUDN1_NAME+"_REF",
            GUDN2_NAME=GUDN_NAME+"2",GUDD2_NAME=GUDD_NAME+"2",DGUDD2_NAME=DGUDD_NAME+"2",GUDU2_NAME=GUDU_NAME+"2",
                    GUD2_REF=GUDN2_NAME+"_REF",
            GUDN3_NAME=GUDN_NAME+"3",GUDD3_NAME=GUDD_NAME+"3",DGUDD3_NAME=DGUDD_NAME+"3",GUDU3_NAME=GUDU_NAME+"3", 
                    GUD3_REF=GUDN3_NAME+"_REF",
            ELF_NAME="ELF",DELF_NAME="DELF",JF_NAME="JF",ELI_NAME="ELI",DELI_NAME="DELI",JI_NAME="JI"; //for final and initial levels of gamma
    private static ArrayList<String> gammaFields=new ArrayList<String>(Arrays.asList(
            EG_NAME+":(10,10)",DEG_NAME+":(20,2)",RI_NAME+":(22,8)",DRI_NAME+":(30,2)",MUL_NAME+":(32,10)",MR_NAME+":(42,8)",DMR_NAME+":(50,6)",
            CG_NAME+":(-1,-1)", DG_NAME+":(-1,-1)",SG_NAME+":(-1,-1)",//general gamma comment and gamma document record
            TI_NAME+":(65,10)",DTI_NAME+":(75,2)",
            CC_NAME+":(56,7)",DCC_NAME+":(63,2)",CC_REF+":(-1,-1)",
            DCO_NAME+":(-1,-1)",DDCO_NAME+":(-1,-1)",DCO_REF+":(-1,-1)",POL_NAME+":(-1,-1)",DPOL_NAME+":(-1,-1)",POL_REF+":(-1,-1)",
            GFLAG_NAME+":(77,1)",GCOIN_NAME+":(78,1)",GQUE_NAME+":(80,1)",FL_NAME+":(-1,-1)",
            GCD_NAME+":(-1,-1)",DGCD_NAME+":(-1,-1)",GCD1_NAME+":(-1,-1)",DGCD1_NAME+":(-1,-1)",GCD2_NAME+":(-1,-1)",DGCD2_NAME+":(-1,-1)",
            GCD3_NAME+":(-1,-1)",DGCD3_NAME+":(-1,-1)",GCD4_NAME+":(-1,-1)",DGCD4_NAME+":(-1,-1)",GCD5_NAME+":(-1,-1)",DGCD5_NAME+":(-1,-1)",
            GCD6_NAME+":(-1,-1)",DGCD6_NAME+":(-1,-1)",GCD7_NAME+":(-1,-1)",DGCD7_NAME+":(-1,-1)",GCD8_NAME+":(-1,-1)",DGCD8_NAME+":(-1,-1)",
            GCD9_NAME+":(-1,-1)",DGCD9_NAME+":(-1,-1)",//for data to be put in comment records starting with "$"
            GCD_LABEL+":(-1,-1)",GCD1_LABEL+":(-1,-1)",GCD2_LABEL+":(-1,-1)",GCD3_LABEL+":(-1,-1)",GCD4_LABEL+":(-1,-1)",GCD5_LABEL+":(-1,-1)",//to be put before GCD_NAME
            GCD6_LABEL+":(-1,-1)",GCD7_LABEL+":(-1,-1)",GCD8_LABEL+":(-1,-1)",GCD9_LABEL+":(-1,-1)",//to be put before GCD_NAME
            GCD_REF+":(-1,-1)",GCD1_REF+":(-1,-1)",GCD2_REF+":(-1,-1)",GCD3_REF+":(-1,-1)",GCD4_REF+":(-1,-1)",GCD5_REF+":(-1,-1)",//to be put after GCD data
            GCD6_REF+":(-1,-1)",GCD7_REF+":(-1,-1)",GCD8_REF+":(-1,-1)",GCD9_REF+":(-1,-1)",
            GUDN_NAME+":(-1,-1)",GUDD_NAME+":(-1,-1)",DGUDD_NAME+":(-1,-1)",GUDU_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit     
            GUDN1_NAME+":(-1,-1)",GUDD1_NAME+":(-1,-1)",DGUDD1_NAME+":(-1,-1)",GUDU1_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            GUDN2_NAME+":(-1,-1)",GUDD2_NAME+":(-1,-1)",DGUDD2_NAME+":(-1,-1)",GUDU2_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            GUDN3_NAME+":(-1,-1)",GUDD3_NAME+":(-1,-1)",DGUDD3_NAME+":(-1,-1)",GUDU3_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            GUD_REF+":(-1,-1)",GUD1_REF+":(-1,-1)",GUD2_REF+":(-1,-1)",GUD3_REF+":(-1,-1)",
            ELF_NAME+":(-1,-1)",DELF_NAME+":(-1,-1)",JF_NAME+":(-1,-1)",ELI_NAME+":(-1,-1)",DELI_NAME+":(-1,-1)",JI_NAME+":(-1,-1)" //for final and initial levels of gamma
    ));
    
    private static ArrayList<String> angularFields=new ArrayList<String>(Arrays.asList(
            A2_NAME+":(-1,-1)",DA2_NAME+":(-1,-1)",A4_NAME+":(-1,-1)",DA4_NAME+":(-1,-1)",A6_NAME+":(-1,-1)",DA6_NAME+":(-1,-1)",
            A2_REF+":(-1,-1)",A4_REF+":(-1,-1)",A6_REF+":(-1,-1)"
    ));
    
    
    public final static String          
            BE1_NAME="BE1",DBE1_NAME="DBE1",BE2_NAME="BE2",DBE2_NAME="DBE2",BE3_NAME="BE3",DBE3_NAME="DBE3",
            BE4_NAME="BE4",DBE4_NAME="DBE4",BE5_NAME="BE5",DBE5_NAME="DBE5",BE6_NAME="BE6",DBE6_NAME="DBE6",
            BE7_NAME="BE7",DBE7_NAME="DBE7",BE8_NAME="BE8",DBE8_NAME="DBE8",                       
            BM1_NAME="BM1",DBM1_NAME="DBM1",BM2_NAME="BM2",DBM2_NAME="DBM2",BM3_NAME="BM3",DBM3_NAME="DBM3",
            BM4_NAME="BM4",DBM4_NAME="DBM4",BM5_NAME="BM5",DBM5_NAME="DBM5",BM6_NAME="BM6",DBM6_NAME="DBM6",
            BM7_NAME="BM7",DBM7_NAME="DBM7",BM8_NAME="BM8",DBM8_NAME="DBE8",  
           
            BE1W_NAME="BE1W",DBE1W_NAME="DBE1W",BE2W_NAME="BE2W",DBE2W_NAME="DBE2W",BE3W_NAME="BE3W",DBE3W_NAME="DBE3W",
            BE4W_NAME="BE4W",DBE4W_NAME="DBE4W",BE5W_NAME="BE5W",DBE5W_NAME="DBE5W",BE6W_NAME="BE6W",DBE6W_NAME="DBE6W",
            BE7W_NAME="BE7W",DBE7W_NAME="DBE7W",BE8W_NAME="BE8W",DBE8W_NAME="DBE8W",               
            
            BM1W_NAME="BM1W",DBM1W_NAME="DBM1W",BM2W_NAME="BM2W",DBM2W_NAME="DBM2W",BM3W_NAME="BM3W",DBM3W_NAME="DBM3W",
            BM4W_NAME="BM4W",DBM4W_NAME="DBM4W",BM5W_NAME="BM5W",DBM5W_NAME="DBM5W",BM6W_NAME="BM6W",DBM6W_NAME="DBM6W",
            BM7W_NAME="BM7W",DBM7W_NAME="DBM7W",BM8W_NAME="BM8W",DBM8W_NAME="DBM8W",  
            
            BE1W_REF="BE1W_REF",BE2W_REF="BE2W_REF",BE3W_REF="BE3W_REF",BE4W_REF="BE4W_REF",BE5W_REF="BE5W_REF",
            BE6W_REF="BE6W_REF",BE7W_REF="BE7W_REF",BE8W_REF="BE8W_REF",
            BM1W_REF="BM1W_REF",BM2W_REF="BM2W_REF",BM3W_REF="BM3W_REF",BM4W_REF="BM4W_REF",BM5W_REF="BM5W_REF",
            BM6W_REF="BM6W_REF",BM7W_REF="BM7W_REF",BM8W_REF="BM8W_REF";
    private static ArrayList<String> gammaEMs=new ArrayList<String>(Arrays.asList(
            BE1_NAME+":(-1,-1)",DBE1_NAME+":(-1,-1)",BE2_NAME+":(-1,-1)",DBE2_NAME+":(-1,-1)",BE3_NAME+":(-1,-1)",DBE3_NAME+":(-1,-1)",
            BE4_NAME+":(-1,-1)",DBE4_NAME+":(-1,-1)",BE5_NAME+":(-1,-1)",DBE5_NAME+":(-1,-1)",BE6_NAME+":(-1,-1)",DBE6_NAME+":(-1,-1)",
            BE7_NAME+":(-1,-1)",DBE7_NAME+":(-1,-1)",BE8_NAME+":(-1,-1)",DBE8_NAME+":(-1,-1)",
            BM1_NAME+":(-1,-1)",DBM1_NAME+":(-1,-1)",BM2_NAME+":(-1,-1)",DBM2_NAME+":(-1,-1)",BM3_NAME+":(-1,-1)",DBM3_NAME+":(-1,-1)",
            BM4_NAME+":(-1,-1)",DBM4_NAME+":(-1,-1)",BM5_NAME+":(-1,-1)",DBM5_NAME+":(-1,-1)",BM6_NAME+":(-1,-1)",DBM6_NAME+":(-1,-1)",
            BM7_NAME+":(-1,-1)",DBM7_NAME+":(-1,-1)",BM8_NAME+":(-1,-1)",DBM8_NAME+":(-1,-1)",
            BE1W_NAME+":(-1,-1)",DBE1W_NAME+":(-1,-1)",BE2W_NAME+":(-1,-1)",DBE2W_NAME+":(-1,-1)",BE3W_NAME+":(-1,-1)",DBE3W_NAME+":(-1,-1)",
            BE4W_NAME+":(-1,-1)",DBE4W_NAME+":(-1,-1)",BE5W_NAME+":(-1,-1)",DBE5W_NAME+":(-1,-1)",BE6W_NAME+":(-1,-1)",DBE6W_NAME+":(-1,-1)",
            BE7W_NAME+":(-1,-1)",DBE7W_NAME+":(-1,-1)",BE8W_NAME+":(-1,-1)",DBE8W_NAME+":(-1,-1)",
            BM1W_NAME+":(-1,-1)",DBM1W_NAME+":(-1,-1)",BM2W_NAME+":(-1,-1)",DBM2W_NAME+":(-1,-1)",BM3W_NAME+":(-1,-1)",DBM3W_NAME+":(-1,-1)",
            BM4W_NAME+":(-1,-1)",DBM4W_NAME+":(-1,-1)",BM5W_NAME+":(-1,-1)",DBM5W_NAME+":(-1,-1)",BM6W_NAME+":(-1,-1)",DBM6W_NAME+":(-1,-1)",
            BM7W_NAME+":(-1,-1)",DBM7W_NAME+":(-1,-1)",BM8W_NAME+":(-1,-1)",DBM8W_NAME+":(-1,-1)",
            BE1W_REF+":(-1,-1)",BE2W_REF+":(-1,-1)",BE3W_REF+":(-1,-1)",BE4W_REF+":(-1,-1)",BE5W_REF+":(-1,-1)",BE6W_REF+":(-1,-1)",
            BE7W_REF+":(-1,-1)",BE8W_REF+":(-1,-1)",
            BM1W_REF+":(-1,-1)",BM2W_REF+":(-1,-1)",BM3W_REF+":(-1,-1)",BM4W_REF+":(-1,-1)",BM5W_REF+":(-1,-1)",BM6W_REF+":(-1,-1)",
            BM7W_REF+":(-1,-1)",BM8W_REF+":(-1,-1)"
    ));
    
    public final static String          
            ECC_NAME="ECC",DECC_NAME="DECC",ECC_REF="ECC_REF",EKC_NAME="EKC",DEKC_NAME="DEKC",EKC_REF="EKC_REF",
            ELC_NAME="ELC",DELC_NAME="DELC",ELC_REF="ELC_REF",
            EMC_NAME="EMC",DEMC_NAME="DEMC",EMC_REF="EMC_REF",
            ENC_NAME="ENC",DENC_NAME="DENC",ENC_REF="ENC_REF",
            EOC_NAME="EOC",DEOC_NAME="DEOC",EOC_REF="EOC_REF",
            EPC_NAME="EPC",DEPC_NAME="DEPC",EPC_REF="EPC_REF",
            EQC_NAME="EQC",DEQC_NAME="DEQC",EQC_REF="EQC_REF",
                    
            EL1C_NAME="EL1C",DEL1C_NAME="DEL1C",EL2C_NAME="EL2C",DEL2C_NAME="DEL2C",EL3C_NAME="EL3C",DEL3C_NAME="DEL3C",
            EL12C_NAME="EL12C",DEL12C_NAME="DEL12C",EL23C_NAME="EL23C",DEL23C_NAME="DEL23C",ELCP_NAME="ELC+",DELCP_NAME="DELC+",
            EM1C_NAME="EM1C",DEM1C_NAME="DEM1C",EM2C_NAME="EM2C",DEM2C_NAME="DEM2C",EM3C_NAME="EM3C",DEM3C_NAME="DEM3C",
            EM4C_NAME="EM4C",DM4C_NAME="DM4C",EM5C_NAME="EM5C",DEM5C_NAME="DEM5C",EMCP_NAME="EMC+",DEMCP_NAME="DEMC+",
            EN1C_NAME="EN1C",DEN1C_NAME="DEN1C",EN2C_NAME="EN2C",DEN2C_NAME="DEN2C",EN3C_NAME="EN3C",DEN3C_NAME="DEN3C",
            EN4C_NAME="EN4C",DN4C_NAME="DN4C",EN23C_NAME="EN23C",DEN23C_NAME="DEN23C",ENCP_NAME="ENC+",DENCP_NAME="DENC+";
    private static ArrayList<String> iccFields=new ArrayList<String>(Arrays.asList(
            ECC_NAME+":(-1,-1)",DECC_NAME+":(-1,-1)",EKC_NAME+":(-1,-1)",DEKC_NAME+":(-1,-1)",ELC_NAME+":(-1,-1)",DELC_NAME+":(-1,-1)",
            EMC_NAME+":(-1,-1)",DEMC_NAME+":(-1,-1)",ENC_NAME+":(-1,-1)",DENC_NAME+":(-1,-1)",
            EOC_NAME+":(-1,-1)",DEOC_NAME+":(-1,-1)",EPC_NAME+":(-1,-1)",DEPC_NAME+":(-1,-1)",EQC_NAME+":(-1,-1)",DEQC_NAME+":(-1,-1)",
            ECC_REF+":(-1,-1)",EKC_REF+":(-1,-1)",ELC_REF+":(-1,-1)",EMC_REF+":(-1,-1)",ENC_REF+":(-1,-1)",
            EOC_REF+":(-1,-1)",EPC_REF+":(-1,-1)",EQC_REF+":(-1,-1)",
            EL1C_NAME+":(-1,-1)",DEL1C_NAME+":(-1,-1)",EL2C_NAME+":(-1,-1)",DEL2C_NAME+":(-1,-1)",EL3C_NAME+":(-1,-1)",DEL3C_NAME+":(-1,-1)",
            EL12C_NAME+":(-1,-1)",DEL12C_NAME+":(-1,-1)",EL23C_NAME+":(-1,-1)",DEL23C_NAME+":(-1,-1)",ELCP_NAME+":(-1,-1)",DELCP_NAME+":(-1,-1)",
            EM1C_NAME+":(-1,-1)",DEM1C_NAME+":(-1,-1)",EM2C_NAME+":(-1,-1)",DEM2C_NAME+":(-1,-1)",EM3C_NAME+":(-1,-1)",DEM3C_NAME+":(-1,-1)",
            EM4C_NAME+":(-1,-1)",DM4C_NAME+":(-1,-1)",EM5C_NAME+":(-1,-1)",DEM5C_NAME+":(-1,-1)",EMCP_NAME+":(-1,-1)",DEMCP_NAME+":(-1,-1)",
            EN1C_NAME+":(-1,-1)",DEN1C_NAME+":(-1,-1)",EN2C_NAME+":(-1,-1)",DEN2C_NAME+":(-1,-1)",EN3C_NAME+":(-1,-1)",DEN3C_NAME+":(-1,-1)",
            EN4C_NAME+":(-1,-1)",DN4C_NAME+":(-1,-1)",EN23C_NAME+":(-1,-1)",DEN23C_NAME+":(-1,-1)",ENCP_NAME+":(-1,-1)",DENCP_NAME+":(-1,-1)"
    ));
    
    
    private static ArrayList<String> gammaWidths=new ArrayList<String>(Arrays.asList(
            WIDTHG_NAME+":(-1,-1)",DWIDTHG_NAME+":(-1,-1)",WIDTHG_REF+":(-1,-1)",WIDTHG_UNIT+":(-1,-1)"    

    ));
    
    static{
        gammaFields.addAll(angularFields);
        gammaFields.addAll(gammaEMs);
        gammaFields.addAll(iccFields);
        gammaFields.addAll(gammaWidths);
        
        angularFieldMap=makeMap(angularFields,"G");
        gammaEMMap=makeMap(gammaEMs,"G");
        iccFieldMap=makeMap(iccFields,"G");
        gammaFieldMap=makeMap(gammaFields,"G");
    }
    
    
    /*
     * decay fields
     */
    public final static String          
            EB_NAME="EB",DEB_NAME="DEB",IB_NAME="IB",DIB_NAME="DIB",
            IBE_NAME="IBE",DIBE_NAME="DIBE", //B+ decay branch
            LOGFT_NAME="LOGFT",DLOGFT_NAME="DLOGFT",
            UN_NAME="UN",//forbiddenness, e.g., "1U","2U"
            IE_NAME="IE",DIE_NAME="DIE", // EC decay branch
            TIE_NAME="TIE",DTIE_NAME="DTIE",// EC+B+ total decay intensity
            EA_NAME="EA",DEA_NAME="DEA",IA_NAME="IA",DIA_NAME="DIA",HF_NAME="HF",DHF_NAME="DHF",
            DFLAG_NAME="DFLAG", DCOIN_NAME="DCOIN",//decay flags B+, B- and EC, ALPHA
            EAV_NAME="EAV",DEAV_NAME="DEAV",//average decay energy for B+ and B-
            ECK_NAME="ECK",DECK_NAME="DECK",//measured fraction of decay by EC decay from K shell
            ECL_NAME="ECL",DECL_NAME="DECL",//L shell
            ECM_NAME="ECM",DECM_NAME="DECM",//M shell
            ECN_NAME="ECN",DECN_NAME="DECN",//   
            ECO_NAME="ECO",DECO_NAME="DECO",//            
            ECP_NAME="ECP",DECP_NAME="DECP",// 
            ECLM_NAME="ECLM",DECLM_NAME="DECLM",//L+M+... shells
            SHAPE_C1_NAME="C1",SHAPE_DC1_NAME="DC1",//shape-factor C1 coefficient                   
            SHAPE_C2_NAME="C2",SHAPE_DC2_NAME="DC2",//shape-factor C2 coefficient
            SHAPE_C3_NAME="C3",SHAPE_DC3_NAME="DC3",//shape-factor C3 coefficient                   
            SHAPE_C4_NAME="C4",SHAPE_DC4_NAME="DC4",//shape-factor C4 coefficient
            //AFLAG_NAME="AFLAG", //flags for alpha decy
            CB_NAME="CB",CE_NAME="CE",CA_NAME="CA", //coments for B+/-, EC and alpha decays
            DB_NAME="DB",DE_NAME="DE",DA_NAME="DA", //documents for B+/-, EC and alpha decays
            SB_NAME="SB",SE_NAME="SE",SA_NAME="SA", //calculated lines
            DQUE_NAME="DQUE", //decay records
            DCD_NAME="D"+COM_DATA_NAME,DDCD_NAME="D"+DCD_NAME,DCD_REF=DCD_NAME+"_REF",DCD_LABEL=DCD_NAME+"+LABEL",
            DCD1_NAME=DCD_NAME+"1",DDCD1_NAME=DDCD_NAME+"1",DCD1_REF=DCD1_NAME+"_REF",DCD1_LABEL=DCD1_NAME+"+LABEL",
            DCD2_NAME=DCD_NAME+"2",DDCD2_NAME=DDCD_NAME+"2",DCD2_REF=DCD2_NAME+"_REF",DCD2_LABEL=DCD2_NAME+"+LABEL",
            DCD3_NAME=DCD_NAME+"3",DDCD3_NAME=DDCD_NAME+"3",DCD3_REF=DCD3_NAME+"_REF",DCD3_LABEL=DCD3_NAME+"+LABEL",
            DCD4_NAME=DCD_NAME+"4",DDCD4_NAME=DDCD_NAME+"4",DCD4_REF=DCD4_NAME+"_REF",DCD4_LABEL=DCD4_NAME+"+LABEL",
            DCD5_NAME=DCD_NAME+"5",DDCD5_NAME=DDCD_NAME+"5",DCD5_REF=DCD5_NAME+"_REF",DCD5_LABEL=DCD5_NAME+"+LABEL", //for data to be put in comment records starting with "$"
            DUDN_NAME="D"+UDN_NAME,DUDD_NAME="D"+UDD_NAME,DDUDD_NAME="D"+DUDD_NAME,DUDU_NAME="D"+UDU_NAME,//user-defined record name, data value, uncertainty, unit      
                   DUD_REF=DUDN_NAME+"_REF",
            DUDN1_NAME=DUDN_NAME+"1",DUDD1_NAME=DUDD_NAME+"1",DDUDD1_NAME=DDUDD_NAME+"1",DUDU1_NAME=DUDU_NAME+"1",//user-defined record name, data value, uncertainty, unit  
                   DUD1_REF=DUDN1_NAME+"_REF",
            DUDN2_NAME=DUDN_NAME+"2",DUDD2_NAME=DUDD_NAME+"2",DDUDD2_NAME=DDUDD_NAME+"2",DUDU2_NAME=DUDU_NAME+"2",
                   DUD2_REF=DUDN2_NAME+"_REF",
            DUDN3_NAME=DUDN_NAME+"3",DUDD3_NAME=DUDD_NAME+"3",DDUDD3_NAME=DDUDD_NAME+"3",DUDU3_NAME=DUDU_NAME+"3",
                   DUD3_REF=DUDN3_NAME+"_REF",
            ELD_NAME="ELD",DELD_NAME="DELD",JD_NAME="JF",ELP_NAME="ELP",DELP_NAME="DELP",JP_NAME="JP"; //for daughter and parent levels of decay

    private static ArrayList<String> decayFields=new ArrayList<String>(Arrays.asList(
            EB_NAME+":(10,10)",DEB_NAME+":(20,2)",IB_NAME+":(22,8)",DIB_NAME+":(30,2)",
            IBE_NAME+":(22,8)",DIBE_NAME+":(30,2)", //B+ decay branch
            LOGFT_NAME+":(42,8)",DLOGFT_NAME+":(50,6)",
            UN_NAME+":(78,2)",//forbiddenness, e.g., "1U","2U"
            IE_NAME+":(32,8)",DIE_NAME+":(40,2)", // EC decay branch
            TIE_NAME+":(65,10)",DTIE_NAME+":(75,2)",// EC+B+ total decay intensity
            EA_NAME+":(10,10)",DEA_NAME+":(20,2)",IA_NAME+":(22,8)",DIA_NAME+":(30,2)",HF_NAME+":(32,8)",DHF_NAME+":(40,2)",
            DFLAG_NAME+":(77,1)", DCOIN_NAME+":(78,1)",//decay flags and coincidence mark B+, B- and EC, ALPHA
            EAV_NAME+":(-1,-1)",DEAV_NAME+":(-1,-1)",//average decay energy for B+ and B-
            ECK_NAME+":(-1,-1)",DECK_NAME+":(-1,-1)",//EC fraction from K shell
            ECL_NAME+":(-1,-1)",DECL_NAME+":(-1,-1)",
            ECM_NAME+":(-1,-1)",DECM_NAME+":(-1,-1)",
            ECN_NAME+":(-1,-1)",DECN_NAME+":(-1,-1)",
            ECO_NAME+":(-1,-1)",DECO_NAME+":(-1,-1)",
            ECP_NAME+":(-1,-1)",DECP_NAME+":(-1,-1)",
            ECLM_NAME+":(-1,-1)",DECLM_NAME+":(-1,-1)",
            //AFLAG_NAME+":(77,-1)", //flags for alpha decy
            SHAPE_C1_NAME+":(-1,-1)",SHAPE_DC1_NAME+":(-1,-1)",//shape-factor C1 coefficient
            SHAPE_C2_NAME+":(-1,-1)",SHAPE_DC2_NAME+":(-1,-1)",//shape-factor C2 coefficient
            SHAPE_C3_NAME+":(-1,-1)",SHAPE_DC3_NAME+":(-1,-1)",//shape-factor C3 coefficient
            SHAPE_C4_NAME+":(-1,-1)",SHAPE_DC4_NAME+":(-1,-1)",//shape-factor C4 coefficient
            CB_NAME+":(-1,-1)",CE_NAME+":(-1,-1)",CA_NAME+":(-1,-1)", //coments for B+/-, EC and alpha decays
            DB_NAME+":(-1,-1)",DE_NAME+":(-1,-1)",DA_NAME+":(-1,-1)", //documents for B+/-, EC and alpha decays
            SB_NAME+":(-1,-1)",SE_NAME+":(-1,-1)",SA_NAME+":(-1,-1)", //calculated lines
            DQUE_NAME+":(80,1)", //decay records
            DCD_NAME+":(-1,-1)",DDCD_NAME+":(-1,-1)",
            DCD1_NAME+":(-1,-1)",DDCD1_NAME+":(-1,-1)",DCD2_NAME+":(-1,-1)",DDCD2_NAME+":(-1,-1)",
            DCD3_NAME+":(-1,-1)",DDCD3_NAME+":(-1,-1)",DCD4_NAME+":(-1,-1)",DDCD4_NAME+":(-1,-1)",
            DCD5_NAME+":(-1,-1)",DDCD5_NAME+":(-1,-1)", //for data to be put in decay comment records starting with "$" 
            DCD_LABEL+":(-1,-1)",DCD1_LABEL+":(-1,-1)",DCD2_LABEL+":(-1,-1)",DCD3_LABEL+":(-1,-1)",DCD4_LABEL+":(-1,-1)",DCD5_LABEL+":(-1,-1)",//to be put before DCD_NAME
            DCD_REF+":(-1,-1)",DCD1_REF+":(-1,-1)",DCD2_REF+":(-1,-1)",DCD3_REF+":(-1,-1)",DCD4_REF+":(-1,-1)",DCD5_REF+":(-1,-1)",
            DUDN_NAME+":(-1,-1)",DUDD_NAME+":(-1,-1)",DDUDD_NAME+":(-1,-1)",DUDU_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit     
            DUDN1_NAME+":(-1,-1)",DUDD1_NAME+":(-1,-1)",DDUDD1_NAME+":(-1,-1)",DUDU1_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            DUDN2_NAME+":(-1,-1)",DUDD2_NAME+":(-1,-1)",DDUDD2_NAME+":(-1,-1)",DUDU2_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            DUDN3_NAME+":(-1,-1)",DUDD3_NAME+":(-1,-1)",DDUDD3_NAME+":(-1,-1)",DUDU3_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            DUD_REF+":(-1,-1)",DUD1_REF+":(-1,-1)",DUD2_REF+":(-1,-1)",DUD3_REF+":(-1,-1)",
            ELD_NAME+":(-1,-1)",DELD_NAME+":(-1,-1)",JD_NAME+":(-1,-1)",ELP_NAME+":(-1,-1)",DELP_NAME+":(-1,-1)",JP_NAME+":(-1,-1)" //for daughter and parent levels of decay

    ));
    static{
        decayFieldMap=makeMap(decayFields,"B");
    }
    
    /*
     * delay fields
     */
    
    public final static String          
            EP_NAME="EP",DEP_NAME="DEP",IP_NAME="IP",DIP_NAME="DIP",EI_NAME="EI", //delayed-particle
            WIDTH_DP_NAME="WIDTH_DP",DWIDTH_DP_NAME="DWIDTH_DP",WIDTH_DP_REF="WIDTH_DP_REF",LP_NAME="LP",
            EDP_NAME="EDP",DEDP_NAME="DEDP",EDN_NAME="EDN",DEDN_NAME="DEDN",EDA_NAME="EDA",DEDA_NAME="DEDA",//used by xls2ens for delayed
            PFLAG_NAME="PFLAG", PCOIN_NAME="PCOIN",//particle flags and coin
            CD_NAME="CD",DD_NAME="DD",              //comments and documents for delayed-particle
            CDN_NAME="CDN",DDN_NAME="DDN",
            CDP_NAME="CDP",DDP_NAME="DDP",
            CDA_NAME="CDA",DDA_NAME="DDA",
            PQUE_NAME="PQUE", //delayed-particle records 
            PCD_NAME="P"+COM_DATA_NAME,DPCD_NAME="D"+PCD_NAME,PCD_REF=PCD_NAME+"_REF",PCD_LABEL=PCD_NAME+"_LABEL",
            PCD1_NAME=PCD_NAME+"1",DPCD1_NAME=DPCD_NAME+"1",PCD1_REF=PCD1_NAME+"_REF",PCD1_LABEL=PCD1_NAME+"_LABEL",
            PCD2_NAME=PCD_NAME+"2",DPCD2_NAME=DPCD_NAME+"2",PCD2_REF=PCD2_NAME+"_REF",PCD2_LABEL=PCD2_NAME+"_LABEL",
            PCD3_NAME=PCD_NAME+"3",DPCD3_NAME=DPCD_NAME+"3",PCD3_REF=PCD3_NAME+"_REF",PCD3_LABEL=PCD3_NAME+"_LABEL",
            PCD4_NAME=PCD_NAME+"4",DPCD4_NAME=DPCD_NAME+"4",PCD4_REF=PCD4_NAME+"_REF",PCD4_LABEL=PCD4_NAME+"_LABEL",
            PCD5_NAME=PCD_NAME+"5",DPCD5_NAME=DPCD_NAME+"5",PCD5_REF=PCD5_NAME+"_REF",PCD5_LABEL=PCD5_NAME+"_LABEL", //for data to be put in comment records starting with "$"
            PUDN_NAME="P"+UDN_NAME,PUDD_NAME="P"+UDD_NAME,DPUDD_NAME="D"+PUDD_NAME,PUDU_NAME="P"+UDU_NAME,//user-defined record name, data value, uncertainty, unit      
                    PUD_REF=PUDN_NAME+"_REF",
            PUDN1_NAME=PUDN_NAME+"1",PUDD1_NAME=PUDD_NAME+"1",DPUDD1_NAME=DPUDD_NAME+"1",PUDU1_NAME=PUDU_NAME+"1",//user-defined record name, data value, uncertainty, unit  
                    PUD1_REF=PUDN1_NAME+"_REF",
            PUDN2_NAME=PUDN_NAME+"2",PUDD2_NAME=PUDD_NAME+"2",DPUDD2_NAME=DPUDD_NAME+"2",PUDU2_NAME=PUDU_NAME+"2",
                    PUD2_REF=PUDN2_NAME+"_REF",
            PUDN3_NAME=PUDN_NAME+"3",PUDD3_NAME=PUDD_NAME+"3",DPUDD3_NAME=DPUDD_NAME+"3",PUDU3_NAME=PUDU_NAME+"3",
                    PUD3_REF=PUDN3_NAME+"_REF";

    private static ArrayList<String> delayFields=new ArrayList<String>(Arrays.asList(
            EDP_NAME+":(10,10)",DEDP_NAME+":(20,2)",EDN_NAME+":(10,10)",DEDN_NAME+":(20,2)",EDA_NAME+":(10,10)",DEDA_NAME+":(20,2)",//used by xls2ens for delayed
            EP_NAME+":(10,10)",DEP_NAME+":(20,2)",IP_NAME+":(22,8)",DIP_NAME+":(30,2)",EI_NAME+":(32,8)", //delayed-particle
            WIDTH_DP_NAME+":(40,10)",DWIDTH_DP_NAME+":(50,6)",LP_NAME+":(56,9)",
            PFLAG_NAME+":(77,1)", PCOIN_NAME+":(78,1)",//particle flags and coin
            CD_NAME+":(-1,-1)",DD_NAME+":(-1,-1)",              //comments and documents for delayed-particle
            CDN_NAME+":(-1,-1)",DDN_NAME+":(-1,-1)",
            CDP_NAME+":(-1,-1)",DDP_NAME+":(-1,-1)",
            CDA_NAME+":(-1,-1)",DDA_NAME+":(-1,-1)",
            PQUE_NAME+":(80,1)", //delayed-particle records 
            PCD_NAME+":(-1,-1)",DPCD_NAME+":(-1,-1)",
            PCD1_NAME+":(-1,-1)",DPCD1_NAME+":(-1,-1)",PCD2_NAME+":(-1,-1)",DPCD2_NAME+":(-1,-1)",
            PCD3_NAME+":(-1,-1)",DPCD3_NAME+":(-1,-1)",PCD4_NAME+":(-1,-1)",DPCD4_NAME+":(-1,-1)",
            PCD5_NAME+":(-1,-1)",DPCD5_NAME+":(-1,-1)",//for data to be put in delay comment records starting with "$" 
            PCD_LABEL+":(-1,-1)",PCD1_LABEL+":(-1,-1)",PCD2_LABEL+":(-1,-1)",PCD3_LABEL+":(-1,-1)",PCD4_LABEL+":(-1,-1)",PCD5_LABEL+":(-1,-1)",//to be put before PCD_NAME
            PCD_REF+":(-1,-1)",PCD1_REF+":(-1,-1)",PCD2_REF+":(-1,-1)",PCD3_REF+":(-1,-1)",PCD4_REF+":(-1,-1)",PCD5_REF+":(-1,-1)",
            PUDN_NAME+":(-1,-1)",PUDD_NAME+":(-1,-1)",DPUDD_NAME+":(-1,-1)",PUDU_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit     
            PUDN1_NAME+":(-1,-1)",PUDD1_NAME+":(-1,-1)",DPUDD1_NAME+":(-1,-1)",PUDU1_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            PUDN2_NAME+":(-1,-1)",PUDD2_NAME+":(-1,-1)",DPUDD2_NAME+":(-1,-1)",PUDU2_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            PUDN3_NAME+":(-1,-1)",PUDD3_NAME+":(-1,-1)",DPUDD3_NAME+":(-1,-1)",PUDU3_NAME+":(-1,-1)",//user-defined record name, data value, uncertainty, unit 
            PUD_REF+":(-1,-1)",PUD1_REF+":(-1,-1)",PUD2_REF+":(-1,-1)",PUD3_REF+":(-1,-1)"
    ));
    
    static{
        delayFieldMap=makeMap(delayFields,"D");
    }
    
    
    //parent record fields for decay dataset
    public final static String          
            PID_NAME="PID",EPA_NAME="EPA",DEPA_NAME="DEPA",JPA_NAME="JPA",TP_NAME="TP",DTP_NAME="DTP",TPU_NAME="TPU",     
            QP_NAME="QP",DQP_NAME="DQP",
            CP_NAME="CP",   // general parent comment    
            PDOC_NAME="PDOC";   //document comment          
    private static ArrayList<String> parentFields=new ArrayList<String>(Arrays.asList(
            PID_NAME+":(1,5)",EPA_NAME+":(10,10)",DEPA_NAME+":(20,2)",JPA_NAME+":(22,18)",TP_NAME+":(40,10)",DTP_NAME+":(50,6)",TPU_NAME+":(-1,-1)",     
            QP_NAME+":(65,10)",DQP_NAME+":(75,2)",
            CP_NAME+":(-1,-1)",   // general parent comment    
            PDOC_NAME+":(-1,-1)"   //document comment      
    )); 
    
    public final static String          
            NR_NAME="NR",DNR_NAME="DNR",NT_NAME="NT",DNT_NAME="DNT",BR_NAME="BR",DBR_NAME="DBR",
            NB_NAME="NB",DNB_NAME="DNB",NP_NAME="NP",DNP_NAME="DNP",
            CN_NAME="CN",NDOC_NAME="NDOC";
    private static ArrayList<String> NRecordFields=new ArrayList<String>(Arrays.asList(
            NR_NAME+":(10,10)",DNR_NAME+":(20,2)",NT_NAME+":(22,8)",DNT_NAME+":(30,2)",BR_NAME+":(32,8)",DBR_NAME+":(40,2)",
            NB_NAME+":(42,8)",DNB_NAME+":(50,6)",NP_NAME+":(56,7)",DNP_NAME+":(63,2)",
            CN_NAME+":(-1,-1)",NDOC_NAME+":(-1,-1)"
    )); 
    
    public final static String          
            NRBR_NAME="NRBR",DNRBR_NAME="DNRBR",NTBR_NAME="NTBR",DNTBR_NAME="DNTBR",
            NBBR_NAME="NBBR",DNBBR_NAME="DNBBR",PNCOM_NAME="PNCOM",PNOPT_NAME="PNOPT",
            CPN_NAME="CPN",PNDOC_NAME="PNDOC",PNP_NAME="PNP",DPNP_NAME="DPNP";
    private static ArrayList<String> PNRecordFields=new ArrayList<String>(Arrays.asList(
            NRBR_NAME+":(10,10)",DNRBR_NAME+":(20,2)",NTBR_NAME+":(22,8)",DNTBR_NAME+":(30,2)",
            NBBR_NAME+":(42,8)",DNBBR_NAME+":(50,6)",PNP_NAME+":(56,7)",DPNP_NAME+":(63,2)",PNCOM_NAME+":(77,1)",PNOPT_NAME+":(78,1)",
            CPN_NAME+":(-1,-1)",PNDOC_NAME+":(-1,-1)"
    )); 
    
    
    public final static String          
            Q_NAME="Q",DQ_NAME="DQ",SN_NAME="SN",DSN_NAME="DSN",SP_NAME="SP",DSP_NAME="DSP",
            QA_NAME="QA",DQA_NAME="DQA",QREF_NAME="QREF",
            CQ_NAME="CQ",QDOC_NAME="QDOC";      
    private static ArrayList<String> QRecordFields=new ArrayList<String>(Arrays.asList(
            Q_NAME+":(10,10)",DQ_NAME+":(20,2)",SN_NAME+":(22,8)",DSN_NAME+":(30,2)",SP_NAME+":(32,8)",DSP_NAME+":(40,2)",
            QA_NAME+":(42,8)",DQA_NAME+":(50,6)",QREF_NAME+":(56,25)",
            CQ_NAME+":(-1,-1)",QDOC_NAME+":(-1,-1)"
    )); 
    
    static{
        parentFieldMap=makeMap(parentFields,"P");
        NRecordFieldMap=makeMap(NRecordFields,"N");
        PNRecordFieldMap=makeMap(PNRecordFields,"PN");
        QRecordFieldMap=makeMap(QRecordFields,"Q");
        
        normFieldMap.putAll(NRecordFieldMap);
        normFieldMap.putAll(PNRecordFieldMap);
    }
    
    static{
        allFieldsMap.putAll(levelFieldMap);
        allFieldsMap.putAll(gammaFieldMap);
        allFieldsMap.putAll(decayFieldMap);
        allFieldsMap.putAll(delayFieldMap);

        allFieldsMap.putAll(parentFieldMap);
        allFieldsMap.putAll(QRecordFieldMap);
        allFieldsMap.putAll(NRecordFieldMap);
        allFieldsMap.putAll(PNRecordFieldMap);
        
        allFieldsMap.putAll(levelEMMap);
        allFieldsMap.putAll(levelMOMMap);
        allFieldsMap.putAll(gammaEMMap);
        allFieldsMap.putAll(iccFieldMap);
    
        
    }


    
    //default names of records in the record line (not continuation lines or comment lines) used as titles in Excel columns
    //Note for record and field defined here:
    //   a record could contains value field and uncertainty field, like level energy, also unit field, like half-life
    
    public final static ArrayList<String> levelLineFieldNames=new ArrayList<String>(Arrays.asList(
            EL_NAME,DEL_NAME,JPI_NAME,T_NAME,DT_NAME,TU_NAME,L_NAME,S_NAME,DS_NAME,LFLAG_NAME,MS_NAME,LQUE_NAME));
    
    public final static ArrayList<String> gammaLineFieldNames=new ArrayList<String>(Arrays.asList(
            EG_NAME,DEG_NAME,RI_NAME,DRI_NAME,MUL_NAME,MR_NAME,DMR_NAME,CC_NAME,DCC_NAME,TI_NAME,DTI_NAME,GFLAG_NAME,GCOIN_NAME,GQUE_NAME));
    
    public final static ArrayList<String> betaLineFieldNames=new ArrayList<String>(Arrays.asList(
            EB_NAME,DEB_NAME,IB_NAME,DIB_NAME,LOGFT_NAME,DLOGFT_NAME,UN_NAME,DFLAG_NAME,DQUE_NAME));   
    
    public final static ArrayList<String> ecbpLineFieldNames=new ArrayList<String>(Arrays.asList(//EC+B+
            EB_NAME,DEB_NAME,IB_NAME,DIB_NAME,LOGFT_NAME,DLOGFT_NAME,TIE_NAME,DTIE_NAME,UN_NAME,DFLAG_NAME,DQUE_NAME));  
    
    public final static ArrayList<String> alphaLineFieldNames=new ArrayList<String>(Arrays.asList(
            EA_NAME,DEA_NAME,IA_NAME,DIA_NAME,HF_NAME,DHF_NAME,DFLAG_NAME,DQUE_NAME));  
    
    public final static ArrayList<String> delayLineFieldNames=new ArrayList<String>(Arrays.asList(
            EP_NAME,DEP_NAME,IP_NAME,DIP_NAME,EI_NAME,WIDTH_DP_NAME,DWIDTH_DP_NAME,LP_NAME,PFLAG_NAME,PCOIN_NAME,PQUE_NAME));  
 
    public final static ArrayList<String> parentLineFieldNames=new ArrayList<String>(Arrays.asList(
            PID_NAME,EPA_NAME,DEPA_NAME,JPA_NAME,TP_NAME,DTP_NAME,TPU_NAME,QP_NAME,DQP_NAME));   
    
    public final static ArrayList<String> NRecordFieldNames=new ArrayList<String>(Arrays.asList(//EC+B+
            NR_NAME,DNR_NAME,NT_NAME,DNT_NAME,BR_NAME,DBR_NAME,NB_NAME,DNB_NAME,NP_NAME,DNP_NAME));  
    
    public final static ArrayList<String> PNRecordFieldNames=new ArrayList<String>(Arrays.asList(
            NRBR_NAME,DNRBR_NAME,NTBR_NAME,DNTBR_NAME,NBBR_NAME,DNBBR_NAME,PNP_NAME,DPNP_NAME,PNCOM_NAME,PNOPT_NAME));  
    
    public final static ArrayList<String> QRecordFieldNames=new ArrayList<String>(Arrays.asList(
            Q_NAME,DQ_NAME,SN_NAME,DSN_NAME,SP_NAME,DSP_NAME,QA_NAME,DQA_NAME,QREF_NAME)); 
    
    public final static ArrayList<String> commentDataNames=new ArrayList<String>(Arrays.asList(
            LCD_NAME,LCD1_NAME,LCD2_NAME,LCD3_NAME,LCD4_NAME,LCD5_NAME,LCD6_NAME,LCD7_NAME,LCD8_NAME,LCD9_NAME,
            GCD_NAME,GCD1_NAME,GCD2_NAME,GCD3_NAME,GCD4_NAME,GCD5_NAME,GCD6_NAME,GCD7_NAME,GCD8_NAME,GCD9_NAME,
            DCD_NAME,DCD1_NAME,DCD2_NAME,DCD3_NAME,DCD4_NAME,DCD5_NAME,
            PCD_NAME,PCD1_NAME,PCD2_NAME,PCD3_NAME,PCD4_NAME,PCD5_NAME)); 
    
    public static ArrayList<String> allRecordLineFieldNames=new ArrayList<String>();//only for level/gamma/decay/delay record line fields
    static{
        allRecordLineFieldNames.addAll(levelLineFieldNames);
        allRecordLineFieldNames.addAll(gammaLineFieldNames);
        allRecordLineFieldNames.addAll(betaLineFieldNames);
        allRecordLineFieldNames.addAll(ecbpLineFieldNames);
        allRecordLineFieldNames.addAll(alphaLineFieldNames);
        allRecordLineFieldNames.addAll(delayLineFieldNames);
    }
    
    
    public static ArrayList<String> allCommentFieldNames=new ArrayList<String>();
    static{
        
        for(String s:allFieldsMap.keySet()){
            char c0=s.charAt(0);
            if(c0!='C' && c0!='S')
                continue;
            
            String dname="D"+s;
            if(allFieldsMap.containsKey(dname))
                continue;
            
            allCommentFieldNames.add(s);
            
        }

    }
    
    public static ArrayList<String> allDocumentFieldNames=new ArrayList<String>();
    static{
        allDocumentFieldNames.add("QDOC");
        allDocumentFieldNames.add("PDOC");
        allDocumentFieldNames.add("PNDOC");
        allDocumentFieldNames.add("NDOC");
        
        for(String s:allFieldsMap.keySet()){
            char c0=s.charAt(0);
            if(c0!='D')
                continue;
            
            String dname="D"+s;
            if(allFieldsMap.containsKey(dname))
                continue;
            
            if(c0=='D' && !s.equals("DL") && allFieldsMap.containsKey(s.substring(1)))
                continue;
            
            allDocumentFieldNames.add(s);
            
        }

    }
    //end settings
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////
    // functions
    ////////////////
    
    /*
     * check if a name is in the default level/gamma/decay/delay name list
     */
    public static boolean isValidDataColumn(String name)
    {
        if(allFieldsMap.containsKey(name))
            return true;
        else
            return false;
    }

    public static boolean isValidDataColumn(String name,LinkedHashMap<String,EntryInfo> recordFieldMap)
    {
        if(recordFieldMap.containsKey(name))
            return true;
        else
            return false;
    }
    
    /*
     * check if a name is in the default level name list
     */
    public static boolean isLevelColumn(String name)
    {
        if(levelFieldMap.containsKey(name))
            return true;
        else
            return false;
    }

    /*
     * check if a name is in the default gamma name list
     */
    public static boolean isGammaColumn(String name)
    {
        if(gammaFieldMap.containsKey(name))
            return true;
        else
            return false;
    }
    
    public static boolean isDecayColumn(String name)
    {
        if(decayFieldMap.containsKey(name))
            return true;
        else
            return false;
    }
    
    public static boolean isDelayColumn(String name)
    {
        if(delayFieldMap.containsKey(name))
            return true;
        else
            return false;
    }
    
    public static int entryPos(String name)
    {
        try{
            return allFieldsMap.get(name).pos;
        }catch(Exception e){}

        return -1;
    }

    public static int entryLen(String name)
    {
        try{
            return allFieldsMap.get(name).len;
        }catch(Exception e){}

        return -1;

    }
    
    public static EntryInfo getFieldInfo(String fieldName,String recordLineType) {
        try{
        	
        	HashMap<String,EntryInfo> recFieldsMap=EnsdfConfig.getAllRecordFieldMap(recordLineType);
        	if(fieldName.equals("E")) {
        		if(recordLineType.equals("E"))
        			fieldName="EB";
        		else if(recordLineType.equals("P"))
        			fieldName="EPA";
        		else if(recordLineType.equals("D"))
        			fieldName="EP";
        		else 
        			fieldName+=recordLineType;
        	}
        	EntryInfo entry=recFieldsMap.get(fieldName);
            return entry;
        }catch(Exception e){
        }
        return null;
    }

    public static EntryInfo getFieldInfo(String fieldName) {
        try{
        	return allFieldsMap.get(fieldName);
        }catch(Exception e){
        }
        return null;
    }
    
    /*
     * type="L","G","B","E","A","D" (delayed particle decay),"P" (parent),"N" (norm),"PN","PP" (prompt particle decay)
     * return names of all record fields in the record line of type
     */
    public static ArrayList<String> getRecordLineFieldNames(String recordType){
        recordType=recordType.toUpperCase().trim();
        if(recordType.equals("L"))
            return levelLineFieldNames;
        if(recordType.equals("G"))
            return gammaLineFieldNames;
        if(recordType.equals("B"))
            return betaLineFieldNames;
        if(recordType.equals("E"))
            return ecbpLineFieldNames;
        if(recordType.equals("A"))
            return alphaLineFieldNames;
        if(recordType.equals("D") || recordType.equals("PP"))//PP for prompt particle
            return delayLineFieldNames;
        if(recordType.equals("Q"))
            return QRecordFieldNames;
        if(recordType.equals("P"))
            return parentLineFieldNames;
        if(recordType.equals("N"))
            return NRecordFieldNames;
        if(recordType.equals("PN"))
            return PNRecordFieldNames;
        
        return null;
    }
    
    /*
     * type="L","G","B","E","A" for decay, "D" for delay
     * return names of all record fields in the record line of type
     */
    public static HashMap<String,EntryInfo> getAllRecordFieldMap(String recordType){
    	//DO NOT TRIM recordLineType
    	//" P" is for prompt proton, if trimmed, it becomes "P" for parent record
        String type=recordType.toUpperCase().trim();
        if(recordType.length()==2 && type.length()==1 && recordType.charAt(0)==' ') {
        	//DO NOT TRIM recordLineType
        	//" P" is for prompt proton, if trimmed, it becomes "P" for parent record
        	
        	type=recordType.toUpperCase();
        }
        recordType=type;
        
        if(recordType.equals("L"))
            return levelFieldMap;
        if(recordType.equals("G"))
            return gammaFieldMap;
        if(recordType.equals("B") || recordType.equals("E") || recordType.equals("A"))
            return decayFieldMap;
        if(recordType.equals("D") || recordType.equals("PP"))//PP for prompt particle
            return delayFieldMap;
        if(recordType.length()==2&&"PNDT".contains(recordType.charAt(1)+"")&&"D ".contains(recordType.charAt(0)+""))
        	return delayFieldMap;
        if(recordType.equals("Q"))
            return QRecordFieldMap;
        if(recordType.equals("P"))
            return parentFieldMap;
        if(recordType.equals("N"))
            return NRecordFieldMap;
        if(recordType.equals("PN"))
            return PNRecordFieldMap;
        if(recordType.equals("M"))
        	return atomicFieldMap;

        
        return null;
    }
    
    public static boolean isValueColumn(String name,HashMap<String,EntryInfo> recordFieldsMap){
        name=name.toUpperCase().trim();
        if(name.length()<1)
            return false;
        
        String dname="D"+name;
        if(recordFieldsMap.containsKey(name) && recordFieldsMap.containsKey(dname))
            return true;
        
        return false;
    }
    public static boolean isValueColumn(String name){
        return isValueColumn(name,allFieldsMap);
    }
    
    public static boolean isUncertaintyColumn(String name,HashMap<String,EntryInfo> recordFieldsMap){
        name=name.toUpperCase().trim();
        if(name.length()<2 || name.charAt(0)!='D' || name.equals("DL"))
            return false;
        
        String dname=name;
        name=name.substring(1);
        if(recordFieldsMap.containsKey(name) && recordFieldsMap.containsKey(dname))
            return true;
        
        return false;
    }
    
    public static boolean isUncertaintyColumn(String name){
        return isUncertaintyColumn(name,allFieldsMap);
    }
    
    public static boolean isCommentColumn(String name){
        name=name.toUpperCase().trim();
        return EnsdfConfig.allCommentFieldNames.contains(name);
    }
    
    public static boolean isDocumentColumn(String name){
        name=name.toUpperCase().trim();
        return EnsdfConfig.allDocumentFieldNames.contains(name);
    }
    
    public static boolean isOperationName(String s){
        s=s.toUpperCase().trim();
        if(EnsdfConfig.mathOperationNames.contains(s) || EnsdfConfig.textOperationNames.contains(s))
            return true;
        
        return false;
    }
    
    //op_command=field_name+'_'+op_name
    //op_name: ADD,MULTIPLY,DIVDE,APPEND
    public static boolean isOperationCommand(String s){
        s=s.trim();
        int p=s.indexOf("_");
        if(p<=0)
            return false;
        
        return isOperationName(s.substring(p+1).trim());
    }
    
    // relation_OPs=new ArrayList<String>(Arrays.asList("<","<=",">",">=","~","=","|?"));
    //ENSDF_relation_OPs=new ArrayList<String>(Arrays.asList("LT","LE","GT","GE","AP","CA","SY"," "));
    //ENSDF_relation_OPs_lower=new ArrayList<String>(Arrays.asList("<","|<",">","|>","~","|?","CA","SY"," "));
    
    public static boolean isEnsdfRelationOPUpperCase(String s) {//in upper-case "C" comment
    	s=s.trim().toUpperCase();
    	if(s.isEmpty())
    		return false;
    	
    	return ENSDF_relation_OPs.contains(s);
    }
    public static boolean isEnsdfRelationOPLowerCase(String s) {//in lower-case "c" comment
    	s=s.trim().toUpperCase();
    	if(s.isEmpty())
    		return false;
    	
    	return ENSDF_relation_OPs_lower.contains(s);
    }
    
    public static boolean isRegularRelationOP(String s) {
    	s=s.trim().toUpperCase();
    	if(s.isEmpty())
    		return false;
    	
    	return math_relation_OPs.contains(s);
    }
    
    public static boolean isAnyRelationOP(String s) {
    	boolean b=isRegularRelationOP(s)||isEnsdfRelationOPUpperCase(s)||isEnsdfRelationOPLowerCase(s);   	
    	return b;
    }
    public static boolean isEnsdfRelationOP(String s) {
    	boolean b=isEnsdfRelationOPUpperCase(s)||isEnsdfRelationOPLowerCase(s);  	
    	return b;
    }
    
    public static String convertEnsdfOP2MathOP(String ensdfOP) {
    	try {
    		String s=ensdfOP.toUpperCase().trim();
    		int n=-1;
    		if(ENSDF_relation_OPs.contains(s)) {
    			n=ENSDF_relation_OPs.indexOf(s);
    		}else if(ENSDF_relation_OPs_lower.contains(s)) {
    			n=ENSDF_relation_OPs_lower.indexOf(s);
    		}else {
    			n=math_relation_OPs.indexOf(s);
    		}
    		if(n>=0 && n<math_relation_OPs.size())
    			return math_relation_OPs.get(n);
    		
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    public static String convertOP2EnsdfOPLetters(String ops) {
    	try {
    		String s=ops.toUpperCase().trim();
    		int n=-1;
    		if(ENSDF_relation_OPs.contains(s)) {
    			return s;
    		}else if(ENSDF_relation_OPs_lower.contains(s)) {
    			n=ENSDF_relation_OPs_lower.indexOf(s);
    		}else {
    			n=math_relation_OPs.indexOf(s);
    		}
    		if(n>=0)
    			return ENSDF_relation_OPs.get(n);
    		
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    public static String convertOP2EnsdfOPSymbol(String ops) {
    	try {
    		String s=ops.toUpperCase().trim();
    		int n=-1;
    		if(ENSDF_relation_OPs_lower.contains(s)) {
    			return s;
    		}else if(ENSDF_relation_OPs.contains(s)) {
    			n=ENSDF_relation_OPs.indexOf(s);
    		}else {
    			n=math_relation_OPs.indexOf(s);
    		}
    		if(n>=0)
    			return ENSDF_relation_OPs_lower.get(n);
    		
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
}//end class 
