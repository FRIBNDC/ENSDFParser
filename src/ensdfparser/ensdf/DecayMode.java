package ensdfparser.ensdf;

import ensdfparser.nds.util.Str;

public class DecayMode{


  @SuppressWarnings("unused")
  private String line;

  private String dm_name_s;
  private String dm_symbol_s;
  private String dm_value_s;
  private double dm_value_d;
  private String dm_unc_s;
  private double dm_unc_d;
  private String dm_ref_s;
  private String dm_text;

  private boolean newLine=false;//=true if this record is the first one in a line
  
  public DecayMode(){ 
    dm_name_s="";
    dm_symbol_s="";
    dm_value_s="";
    dm_unc_s="";
    dm_ref_s="";
    dm_value_d=-1;
    dm_unc_d=-1;
    dm_text="";
    newLine=false;
  }

  /* The public methods */
 
  public void setName(String s){ dm_name_s=s.trim().toUpperCase();}
  public void setSymbol(String s){ dm_symbol_s=s.trim().toUpperCase();}
  public void setValue(String s){ dm_value_s=s.trim().toUpperCase();}
  public void setUnc(String s){ dm_unc_s=s.trim().toUpperCase();}
  public void setRef(String s){ dm_ref_s=s.trim().toUpperCase();}
 
  public boolean isNewLine(){return newLine;}
  public void setNewLine(boolean b){newLine=b;}
  
  public void setValues( String dmn, String dms, String dmv, String dmu, String dmr,String dmt,boolean isNewLine){
    dm_name_s=dmn.trim().toUpperCase();
    dm_symbol_s=dms.trim().toUpperCase();
    dm_value_s=dmv.trim().toUpperCase();
    dm_unc_s=dmu.trim().toUpperCase();
    dm_ref_s=dmr.trim();
    dm_ref_s=Str.lowerKeynos(dm_ref_s);
    SDS2XDX s2x=new SDS2XDX(); 
    s2x.setValues( dm_value_s, dm_unc_s );
    dm_value_d=s2x.x(); dm_unc_d=s2x.dx();
    
    dm_text=dmt;
    newLine=isNewLine;
  }

  public void setValues( String dmn, String dms, String dmv, String dmu, String dmr,String dmt){
	  setValues(dmn,dms,dmv,dmu,dmr,dmt,false);  
  }
  
  public String name(){ return  dm_name_s; }
  public String symbol(){ return dm_symbol_s; }
  public String value(){ return  dm_value_s; }
  public String unc(){ return dm_unc_s; }
  public String ref(){ return dm_ref_s; }
  public String txt(){return dm_text;}

  public double doubleValue(){ return  dm_value_d; }
  public double doubleUncertainty(){ return dm_unc_d; }

  public String DMStUnicode(){

    String dmnu=dm_name_s;

    if( dm_name_s.equals("N") ) dmnu="n";
    if( dm_name_s.equals("P") ) dmnu="p";
    if( dm_name_s.equals("A") ) dmnu='\u03b1'+"";

    if( dm_name_s.equals("EC") ) dmnu=""+'\u03b5';
    if( dm_name_s.equals("2EC") ) dmnu="2"+'\u03b5';
    if( dm_name_s.equals("EC+B+") ) dmnu=""+'\u03b5';
    if( dm_name_s.equals("ECP") ) dmnu='\u03b5'+"p";
    if( dm_name_s.equals("EC2P") ) dmnu='\u03b5'+"2p";
    if( dm_name_s.equals("ECA") ) dmnu=""+'\u03b5'+'\u03b1';

    if( dm_name_s.equals("B+P") ) dmnu='\u03b5'+"p";
    if( dm_name_s.equals("B+2P") ) dmnu='\u03b5'+"2p";
    if( dm_name_s.equals("B+3P") ) dmnu='\u03b5'+"3p";
    if( dm_name_s.equals("B+A") ) dmnu=""+'\u03b5'+'\u03b1';


    if( dm_name_s.equals("B-") ) dmnu='\u03b2'+"-";
    if( dm_name_s.equals("2B-") ) dmnu="2"+'\u03b2'+"-";
    if( dm_name_s.equals("B-N") ) dmnu='\u03b2'+"-n";
    if( dm_name_s.equals("B-2N") ) dmnu='\u03b2'+"-2n";
    if( dm_name_s.equals("B-A") ) dmnu=""+'\u03b2'+'\u03b1';


    String dmsu="";

    String dmu=dmnu;

    if( dm_symbol_s.length() > 0 || dm_value_s.length() > 0 || dm_unc_s.length() > 0 ){

      dmsu=":";
      if( dm_symbol_s.equals("GT") ) dmsu=">";
      if( dm_symbol_s.equals("GE") ) dmsu='\u2265'+"";
      if( dm_symbol_s.equals("LT") ) dmsu="<";
      if( dm_symbol_s.equals("LE") ) dmsu='\u2264'+"";
      if( dm_symbol_s.equals("AP") ) dmsu='\u2248'+"";


      dmu=dmu+" "+dmsu;
      if( dm_value_s.length() > 0 ) dmu=dmu+" "+dm_value_s+" % ";
      if( dm_unc_s.length() > 0 ) dmu=dmu+dm_unc_s;
    }

    return dmu;

  }

}
