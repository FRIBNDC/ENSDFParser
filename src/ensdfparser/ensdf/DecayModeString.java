 package ensdfparser.ensdf;

import java.util.*;

import ensdfparser.nds.ensdf.EnsdfUtil;
public class DecayModeString{

//-- Parameters:

  private String line;

  private String dm_name_s;
  private String dm_symbol_s;
  private String dm_value_s;
  private String dm_unc_s;
  private String dm_ref_s;
  private String dm_text;

  private Vector<String> nameV=new Vector<String>();
  private Vector<String> valueV=new Vector<String>();
  private Vector<String> symbolV=new Vector<String>();
  private Vector<String> uncV=new Vector<String>();
  private Vector<String> refV=new Vector<String>();
  private Vector<String> textV=new Vector<String>();

  
  /* The default decay Mode names: */
  private static String dmn[]={
		   "",//dummy
		   "IT", 
		   "G", 
		   "B",
		   "B-",
		   "B-N",
		   "B-2N",
		   "B-3N",
		   "B-4N",
		   "B-A",
		   "B-NA",
		   "B-P",
		   "B-PA",
		   "BEC",
		   "2B-",
		   "2EC",
		   "2B+",
		   "B+",
		   "B+A",
          "B+N",
          "B+NA",
		   "B+P",
		   "B+PA",
		   "B+2P",
		   "B+3P",
		   "B+4P",
		   "EC+%B+",
		   "EC",
		   "ECP", 
		   "EC3P",
		   "EC4P",
		   "EC2P", 
		   "ECA", 
		   "EC2A",
		   "ECSF",
		   "ECF",
		   "A", 
		   "N", 
		   "P", 
		   "2N",
		   "2P",
		   "3N",
		   "3P",
		   "2A",  
		   "D", 
		   "3H", 
		   "3HE", 
		   "SF",
		   "8BE",
		   "12C",
		   "14C",
		   "20O",
		   "{+20}O",
		   "{+20}NE",
		   "{+24}NE",
		   "20NE",
		   "24NE",
		   "NE",
		   "MG",
		   "34SI",

  };

  private static ArrayList<String> defaultDecayModes=new ArrayList<String>(Arrays.asList(dmn));
  
  public DecayModeString(){ 
    nameV.clear();
    symbolV.clear();
    valueV.clear();
    uncV.clear();
    refV.clear();
    textV.clear();
    dm_name_s="";
    dm_symbol_s="";
    dm_value_s="";
    dm_unc_s="";
    dm_ref_s="";
    dm_text="";
  }

  public void setValues(String s1){ 
    line=s1; getDMs();
  }
  
  public void getDMs_old(){

   int ndms=11;
   int ndmn=50;
   int ndmx=2;

   String s;
   String t;

   //String dmn[]=new String[ndmn+1];
   String dms[]=new String[ndms+1];
   String dmx[]=new String[ndmx+1];

   ndmn=dmn.length-1;
   
   /*
   dmn[1]="IT"; 
   dmn[2]="G"; 

   dmn[3]="B-";

   dmn[4]="B-N";
   dmn[5]="B-2N";
   dmn[6]="B-3N";
   dmn[7]="B-4N";

   dmn[8]="B-A";

   dmn[9]="2B-";
   dmn[10]="2EC";
   dmn[11]="2B+";

   dmn[12]="B+";
   dmn[13]="B+P";
   dmn[14]="B+2P";
   dmn[15]="B+A";

   dmn[16]="EC+%B+";

   dmn[17]="EC";
   dmn[18]="ECP"; 
   dmn[19]="EC2P"; 
   dmn[20]="ECA"; 
   dmn[21]="ECSF";
   dmn[22]="ECF";
 
   dmn[23]="N"; 
   dmn[24]="P"; 
   dmn[25]="D"; 
   dmn[26]="3H"; 
   dmn[27]="3HE"; 
   dmn[28]="A"; 
   dmn[29]="SF";

   dmn[30]="8BE";

   dmn[31]="12C";
   dmn[32]="14C";
   dmn[33]="20O";
   dmn[34]="{+20}O";
   dmn[35]="{+20}NE";
   dmn[36]="{+24}NE";
   dmn[37]="20NE";
   dmn[38]="24NE";
   dmn[39]="NE";
   dmn[40]="MG";
   dmn[41]="34SI";

   dmn[42]="2N";
   dmn[43]="2P";
   dmn[44]="2A";
   
   dmn[45]="B";
   dmn[46]="B+3P";
   dmn[47]="EC3P";
   dmn[48]="B+4P";
   dmn[49]="EC4P";
*/
   
   /*  The possible symbols in front of a Decay mode value  */

   dms[1]="="; dms[2]=" AP"; dms[3]=" GT";  dms[4]=" >"; dms[5]=">"; dms[6]=" GE"; 
   dms[7]=" LT";  dms[8]=" <"; dms[9]="<"; dms[10]=" LE"; dms[11]="|?";dms[12]=" |?";
   dms[13]="|>"; dms[14]=" |>"; dms[15]="|<"; dms[16]=" |<";

   dmx[1]=" "; dmx[2]="$";

   String dmnTemp="";
   
   for( int i=1; i<=ndmx; i++){
    for( int j=1; j<=ndms; j++){
      for( int k=1; k<=ndmn; k++ ){

    	 dmnTemp=dmn[k];
    	 
         s=dmx[i]+"%"+dmn[k]+dms[j];
         int ileft=line.indexOf(s);
         
         String s1=dmx[i]+"%"+dmn[k]+"+%";
         int ileft1=line.indexOf(s1);
         if(ileft<0 && ileft1>=0){
        	 for(int m=1;m<ndmn;m++){
        		 s=s1+dmn[m]+dms[j];
        		 ileft=line.indexOf(s);
        		 dmnTemp=dmn[k]+"+%"+dmn[m];
        		 if(ileft>=0 && !dmnTemp.equals("EC+%B+"))//EC+%B+ is handled elsewhere as a reserved single decay mode
        			 break;
        	 }
         }
         
         if( ileft >= 0 ){

             if( dmnTemp.equals("EC+%B+") ) { dm_name_s="EC+%B+"; }
             else if( dmnTemp.equals("{+20}O") ) { dm_name_s="20O"; }
             else if( dmnTemp.equals("{+20}NE") ) { dm_name_s="20NE"; }
             else if( dmnTemp.equals("{+24}NE") ) { dm_name_s="24NE"; }
             else if( dmnTemp.equals("G") ) { dm_name_s="IT"; }
             else { dm_name_s=dmnTemp; }
             
             int ilength=s.length();
             t=line.substring(ileft+ilength);
             int idollar=t.indexOf("$");

             if( idollar > 0 )  {
            	 dm_value_s=t.substring(0, idollar).trim();
                 dm_text=line.substring(ileft+1, ileft+ilength+idollar).trim();
             }
             else {
            	 dm_value_s=t.trim();
            	 dm_text=line.substring(ileft+1).trim();
             }

             dm_symbol_s=dms[j].trim();

             dm_unc_s="";
             dm_ref_s="";

             int is=dm_value_s.indexOf(" "); 
             int ip1=dm_value_s.indexOf("(");
             int ip2=dm_value_s.indexOf(")");

//-- One posibility: The decay mode value has uncertainty and reference

             if( is > 0 && ip1 > (is+1) && ip2 > ip1){
               dm_unc_s=( dm_value_s.substring(is,ip1) ).trim();
               dm_ref_s=( dm_value_s.substring(ip1+1,ip2) ).trim();
               dm_value_s=( dm_value_s.substring(0,is) ).trim();
             }

//-- Another posibility: The decay mode value has only reference

             if( is > 0 && ip1 == (is+1) && ip2 > ip1){
               dm_ref_s=( dm_value_s.substring(ip1+1,ip2) ).trim();
               dm_value_s=( dm_value_s.substring(0,is) ).trim();
             }

//-- Another posibility: The decay mode value has only uncertainty

             if( is > 0 && ip1 < 0 && ip2 < 0){
               dm_unc_s=( dm_value_s.substring(is) ).trim();
               dm_value_s=( dm_value_s.substring(0,is) ).trim();
             }
             nameV.addElement(dm_name_s);
             symbolV.addElement(dm_symbol_s);
             valueV.addElement(dm_value_s);
             uncV.addElement(dm_unc_s);
             refV.addElement(dm_ref_s);
             
             //dm_text=("%"+dm_name_s+dm_symbol_s+dm_value_s+" "+dm_unc_s).trim();
             //if(dm_ref_s.length()>0 && dm_text.length()>0)
             //	 dm_text+=" ("+dm_ref_s+")";
             textV.addElement(dm_text);

          }
       }  
    }
   }

 }

  public void getDMs(){


	   FindValue fv=new FindValue();
	   
	   Vector<String> entries=EnsdfUtil.findContRecordEntries(line);//find entries of all continuation records in current line including decay modes
	   for(int i=0;i<entries.size();i++){
		   String entry=entries.get(i).trim();
		   if(entry.charAt(0)!='%')
			   continue;
		   
		   fv.clear();
		   fv.findValueInEntry(entry);
		   String name=fv.name();
		   
		   //debug
		   //System.out.println("In DecayModeString line 308: entry="+entry+" name="+name+" fv.canGetX="+fv.canGetX());
		   
		   if(name.length()==0 || name.charAt(0)!='%')
			   continue;
		   
		   name=name.substring(1);
		   
		   //debug
		   //System.out.println("In DecayModeString line 317: entry="+entry+" name="+name+" fv.canGetX="+fv.canGetX());
		   
		   //if(!fv.canGetX() || !defaultDecayModes.contains(name)){
		   if(!fv.canGetV() || !defaultDecayModes.contains(name)){	   
			   continue;
		   }
		   
		   
		   dm_name_s=name;
		   dm_symbol_s=fv.symbol();
		   dm_value_s=fv.s();
		   dm_unc_s=fv.ds();
		   dm_ref_s=fv.ref();
		   dm_text=fv.txt();   
		   
           nameV.addElement(dm_name_s);
           symbolV.addElement(dm_symbol_s);
           valueV.addElement(dm_value_s);
           uncV.addElement(dm_unc_s);
           refV.addElement(dm_ref_s);
           
           //dm_text=("%"+dm_name_s+dm_symbol_s+dm_value_s+" "+dm_unc_s).trim();
           //if(dm_ref_s.length()>0 && dm_text.length()>0)
           //	 dm_text+=" ("+dm_ref_s+")";
           textV.addElement(dm_text);
	   }
	 }

 public Vector<String> nameV(){ return nameV;}
 public Vector<String> symbolV(){ return symbolV;}
 public Vector<String> valueV(){ return valueV;}
 public Vector<String> uncV(){ return uncV;}
 public Vector<String> refV(){ return refV;}
 public Vector<String> textV(){return textV;}


 public static ArrayList<String> defaultDecayModesV(){
	  return defaultDecayModes;
 }
}
