 package ensdfparser.ensdf;

import ensdfparser.nds.util.Str;

public class ContRecord{

  @SuppressWarnings("unused")
  private String type, name,s,ds,ref,units, symbol,text,marker;//marker=continuation type at column#6, e.g., "2", "B","S"
  private String vs;//vNoSymbol, string after the symbol including ref, note that text is everything including name, symbol, value and ref string after the symbol
                    //this is useful for CR with text value
  private boolean sup;
  private boolean newLine=false;//=true if this record is the first one in a line
  private boolean isEmpty=true;
  
  //set to be ture if this CR is the extension of data field in record line and such CR
  //will not be printed out in the output or be used as the recommended data as the corresponding
  //record value which is extended by this CR, unless the record value is empty for which case
  //the extended value in CR will be parsed as the value in the record line
  //e.g., extended energy record with asymmetric uncertainty since energy in the reocrd line can 
  //only have symmetric uncertainty
  private boolean isExt=false;

  public ContRecord(){
    
   name="";s="";ds="";ref="";units="";symbol="";text="";marker="";

  }

  public void setValues(String iname, String isymbol, String is, String iunits, String ids, String iref, String itext,String imarker,boolean issuppressed,boolean isNewLine,boolean isExtension){
    name=iname; symbol=isymbol; s=is; units=iunits; ds=ids;  sup=issuppressed; text=itext;
    ref=Str.lowerKeynos(iref);marker=imarker;
    newLine=isNewLine;
    
    isExt=isExtension;
    
    for(int i=0;i<s.length();i++){
        if(s.charAt(i)=='&'&&(i==0||s.charAt(i-1)!='\\'))s=s.substring(0,i)+"\\"+s.substring(i);
    }
    
    int n=text.indexOf(symbol,name.length());
    if(n>=0)
    	vs=text.substring(n+symbol.length()).trim();
    
    if(name!="" && s!="" && text!="")
        isEmpty=false;
  }

  public void setValues1(String iname, String isymbol, String is, String iunits, String ids, String iref, String itext, String imarker,boolean issuppressed){
      setValues(iname,isymbol,is,iunits,ids,iref,itext,imarker,issuppressed,false,false);
  }
  public void setNewLine(boolean b){newLine=b;}
  
  public void setText(String itext){text=itext;}
  
 
  public String name(){ return name; }
  public String symbol(){ return symbol; }
  public String s(){return s;}
  public String ds(){ return ds; }
  public String ref(){ return ref; }
  public String ref(boolean addSpace){if(addSpace&&ref.length()>0)return " ("+ref()+")";else return ref();}
  public String txt(){return text;}
  public String vs() {return vs;}
  public String units(){ return units; }
  public String contType() {return marker;}
  public boolean isSuppressed(){return sup;}
  public boolean isNewLine(){return newLine;}
  public boolean isEmpty(){return isEmpty;}

  public boolean isExtension() {return isExt;}
}