 package ensdfparser.ensdf;

import java.util.*;

import ensdfparser.nds.util.Str;

public class History{
    protected int id=-1;
    
  protected String typ_s;
  protected String aut_s;
  protected String cit_s;
  protected String cut_s;
  protected String dat_s;
  protected String com_s;
  
  protected Vector<String> lines=new Vector<String>();

  protected Vector<Comment> commentV=new Vector<Comment>();

  protected Vector<ContRecord> crV=new Vector<ContRecord>();
  
  protected Vector<Author> authorsV=new Vector<Author>();
  public History(){
    lines.clear();
    typ_s=""; cit_s=""; cut_s=""; aut_s="";  dat_s=""; com_s="";
  }

  public void setValues(Vector<String> v){
   lines=v;
   if( lines.size() > 0 ) doWork();
  }
  public void isBlank(){
    aut_s="New History Record";
  }
  public void doWork(){

	   String sjunk="";
	    for( int j=0; j<lines.size(); j++){
	    	String line=(String) lines.elementAt(j);
	    	
	    	line=Str.rtrim(line);
	    	
	    	String space=" ";
	    	if(line.endsWith("\\")) {
	    		line=line.substring(0,line.length()-1);
	    		space="";
	    	}

	       if( j == 0 ) sjunk=sjunk+line+space;
	       if( j > 0 ) sjunk=sjunk+line.substring(9).trim()+space;
	    }
    //System.out.println("sjunk="+sjunk);
    FindValue fv=new FindValue(); 

    aut_s="";
    fv.findValueInLine(sjunk,"AUT="); 
    if( fv.canGetV() ) {
    	aut_s=fv.v();

    	ContRecord cr=new ContRecord();
        cr.setValues("AUT",fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),fv.txt(),"H",false,false,false);
        crV.add(cr);
    }

    parseAuthors();
    
    cit_s="";
    fv.findValueInLine(sjunk,"CIT="); 
    if( fv.canGetV() ) {
    	cit_s=fv.v();
    	
    	//System.out.println("########"+cit_s+"  "+sjunk);
    	
    	ContRecord cr=new ContRecord();
        cr.setValues("CIT",fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),fv.txt(),"H",false,false,false);
        crV.add(cr);
    }

    cut_s=""; 
    fv.findValueInLine(sjunk,"CUT="); 
    if( fv.canGetV() ) {
    	cut_s=fv.v();
    	
    	ContRecord cr=new ContRecord();
        cr.setValues("CUT",fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),fv.txt(),"H",false,false,false);
        crV.add(cr);
    }

    typ_s=""; 
    fv.findValueInLine(sjunk,"TYP="); 
    if( fv.canGetV() ) {
    	typ_s=fv.v();
    	
    	ContRecord cr=new ContRecord();
        cr.setValues("TYP",fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),fv.txt(),"H",false,false,false);
        crV.add(cr);
    }

    dat_s=""; 
    fv.findValueInLine(sjunk,"DAT="); 
    if( fv.canGetV() ) {
    	dat_s=fv.v();
    	
    	ContRecord cr=new ContRecord();
        cr.setValues("DAT",fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),fv.txt(),"H",false,false,false);
        crV.add(cr);
    }
    
    com_s=""; 
    fv.findValueInLine(sjunk,"COM="); 
    if( fv.canGetV() ) {
    	//com_s=fv.v(); 
    	int n=sjunk.indexOf("COM=");
    	int n1=sjunk.indexOf("$",n);
    	if(n1>0) {
    		com_s=sjunk.substring(n+4,n1).trim();
    	}else {
    		com_s=sjunk.substring(n+4).trim();
    	}
    	//System.out.println(sjunk+"####"+com_s);
    	
    	ContRecord cr=new ContRecord();
        cr.setValues("COM",fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),com_s,"H",false,false,false);
        crV.add(cr);
    }

//    System.out.println("********************* "+aut_s);

/* End of dowork: */

  }
  
  private void parseAuthors() {  
      authorsV.clear();
      authorsV.addAll(parseAuthors(aut_s));
  }
  public static Vector<Author> parseAuthors(String s) {
      Vector<Author> out=new Vector<Author>();
	  try {
          if(s.isEmpty())
              return out;
          
          s=s.toUpperCase().trim();
          s=s.replace("ET Al.", "").replace("ET AL", "").trim();
          
          String[] temp=s.split("[,;]+|AND");
          
          for(int i=0;i<temp.length;i++) {
              s=temp[i].trim();
          
              if(s.length()>0 && !s.equals(".")) {
                  Author author=new Author(s);
                  out.add(author);               
              }
          }
      }catch(Exception e) {
          
      }
	  
	  return out;
  }  
  
  public String typ(){return typ_s;}
  public String cit(){return cit_s;}
  public String cut(){return cut_s;}
  public String aut(){return aut_s;}
  public String dat(){return dat_s;}
  public String com() {return com_s;}
  
  //use for comparator
  public String effectiveDate() {
	    String date = cut();
	    if(date.length()<=0)
	    	date=dat();

	    if (date.isEmpty()) return "";
	    return date;

  }
  
  public String dateForComparator() {
	    String date = cut();
	    if(date.length()<=0)
	    	date=dat();

	    if (date.isEmpty()) return "";
	    return toYYYYMMDD(date);   // "15-JAN-2024" → "20240115"
	
  }

  private static final Map<String, String> MONTHS = new HashMap<>();
  static {
      MONTHS.put("JAN", "01");
      MONTHS.put("FEB", "02");
      MONTHS.put("MAR", "03");
      MONTHS.put("APR", "04");
      MONTHS.put("MAY", "05");
      MONTHS.put("JUN", "06");
      MONTHS.put("JUL", "07");
      MONTHS.put("AUG", "08");
      MONTHS.put("SEP", "09");
      MONTHS.put("OCT", "10");
      MONTHS.put("NOV", "11");
      MONTHS.put("DEC", "12");
  }

  private static String toYYYYMMDD(String ddmmyyyy) {
      // "15-JAN-2024" → "20240115"
      String[] parts = ddmmyyyy.split("-");
      if (parts.length != 3) return ddmmyyyy;   // unexpected format — return as-is
      String day   = parts[0];                  // "15"
      String month = MONTHS.get(parts[1].toUpperCase());   // "JAN" → "01"
      String year  = parts[2];                  // "2024"
      if (month == null) return ddmmyyyy;       // unknown month
      return year + month + day;                // "20240115"
  }
  
  public Vector<String> lines(){return lines;}
  
  /*return author names in lower case with first letter capitalized*/
  public String Aut(){
      String lower=aut_s.trim().toLowerCase();
      String upper=aut_s.trim().toUpperCase();
      String Aut_s="";
      if(aut_s.length()==0)
          return aut_s;
      
      Aut_s+=upper.charAt(0);
      int len=lower.length();
      for(int i=1;i<len;i++){
          char c=lower.charAt(i);
          Aut_s+=c;
          if(c==' ' || c=='.' || c==','){
              char next=0;
              if(i<len-1){
                  next=upper.charAt(i+1);
                  int j=i+1;
                  while(next==' ' && j<len-1){
                      next=upper.charAt(j+1);
                      j++;
                  }     
                  if(next!=0){
                      if(c!=' ')
                          Aut_s+=' ';
                      
                      if(upper.indexOf("AND")==j && next=='A')
                          next='a';
                      
                      Aut_s+=next;
                  }
                  
                  i=j;
              }           
          }
      }
      
      Aut_s=Aut_s.replaceAll("Et Al", "et al");
      return Aut_s;
  }

  //for converting date like 30-SEP-2014 to 30-Sep-2014 
  public String formatDate(String dat){

      String[] temp=Str.trimArray(dat.toLowerCase().split("-"));

      if(temp.length!=3)
          return dat;
      
      if(temp[1].length()>1)
          temp[1]=temp[1].toUpperCase().charAt(0)+temp[1].substring(1);
      return temp[0]+'-'+temp[1]+'-'+temp[2];
  }

  public String Cut(){return formatDate(cut_s);}
  public String Dat(){return formatDate(dat_s);}
 
  public int id() {return id;}
  public void setID(int i) {id=i;}
  
  public Vector<ContRecord> contRecsV(){ return crV;}
  public Vector<Author> authorsV(){return authorsV;}
  
}

