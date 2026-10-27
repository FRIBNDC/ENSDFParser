 package ensdfparser.ensdf;

import ensdfparser.nds.ensdf.EnsdfConfig;
import ensdfparser.nds.util.Str;

//Half life of a level
public class T12{

  private String t12_u_s;  // units
  private String t12_v_s;  // value
  private String dt12_s; // uncertainty
  private String th_s,t12_uh_s;
  private String tu_s;
  private String tuf_s;
  private String te_s;

  private double t12_d, dt12_d; @SuppressWarnings("unused")
private float t12_f; private String t12_ds;
  private double t12_uv_d;
  private double t12_lv_d;

  private double x_d;private double x_uv_d;private double x_lv_d;private double mult;

  private boolean dt12IsNumber;

  public T12(){ 

    t12_d=-1; t12_f=-1;
    t12_uv_d=-1; 
    t12_lv_d=-1;
    dt12_d=-1;

    t12_ds="";

    t12_u_s="";
    t12_v_s="";
    dt12_s="";
    th_s="";
    tu_s="";
    tuf_s="";
    te_s="";
    dt12IsNumber=false;
  }

  public void setValues(String value, String units, String uncertainty){

  
    t12_u_s=""; t12_v_s=""; dt12_s="";

    if( ! value.equals(null) ){

      t12_v_s=value.toUpperCase().trim();

      if( ! units.equals(null) ) t12_u_s=units.toUpperCase().trim();

      if( ! uncertainty.equals(null) ) dt12_s=uncertainty.toUpperCase().trim();

      dt12IsNumber=true;
      for( int j=0; j<dt12_s.length(); j++){
        if(! Character.isDigit( dt12_s.charAt(j) ) ) dt12IsNumber=false;
      }

      if(dt12_s.isEmpty())
    	  dt12IsNumber=false;
      
      te_s=t12_v_s+" "+t12_u_s+" "+dt12_s;
 
      parseMultiplier();
      
      if( t12_v_s.length() > 0 && ! t12_v_s.equals("UNKNOWN") &&  ! t12_v_s.equals("?") && mult>0){
   
        /* If it says "STABLE" we assign a numeric value of 1e+24 s */

        if( t12_v_s.indexOf("STABLE") >=0 ) { 
            t12_d=1e24;
            t12_uv_d=1e26;
            t12_lv_d=1e24;
            t12_ds="infinity";
        }
        else{
           SDS2XDX t=new SDS2XDX();
           t.setValues(t12_v_s,dt12_s);
           x_d=t.x();
           x_uv_d=t.XUV();
           x_lv_d=t.XLV();
           if( t12_u_s.indexOf("EV") >= 0 ){
             t12_d=mult/x_d;
             t12_uv_d=mult/x_lv_d;
             t12_lv_d=mult/x_uv_d;
             if( dt12IsNumber ){
               dt12_d=mult*t.dx()/(x_d*x_d);
               t12_uv_d=t12_d+dt12_d;
               t12_lv_d=t12_d-dt12_d;
             }else {
            	 dt12_d=mult/( (t12_uv_d-t12_lv_d)/2 );
             }

//             System.out.println("t0="+ t12_d+" "+dt12_d+" "+x_d+" "+t.dx() );
           }else{
              t12_d=mult*x_d; 
              t12_uv_d=mult*x_uv_d; 
              t12_lv_d=mult*x_lv_d;
              if( dt12IsNumber ) 
            	  dt12_d=mult*t.dx();
              else 
            	  dt12_d=(t12_uv_d-t12_lv_d)/2 ;
           }
//           System.out.println("t12 ="+t12_d+" "+dt12_d );
           XDX2SDS x2s=new XDX2SDS();
//           float tjunk=(float) t12_d;
//           t12_d=(double) tjunk; 
           x2s.setValues( t12_d,0.0);
           t12_ds=x2s.S();
        }
//        System.out.println("t0="+ t12_ds+" "+dt12_d);

       /* Doing the html version */

       WebSpecChar wsc=new WebSpecChar();

       t12_uh_s=""; /* the html units */

       if( t12_u_s.equals("US") ) t12_uh_s="&micro;S" ; 
       else if( t12_u_s.equals("EV") ) t12_uh_s="eV";
       else if( t12_u_s.equals("KEV") ) t12_uh_s="keV";
       else if( t12_u_s.equals("MEV") ) t12_uh_s="MeV";
       else t12_uh_s=t12_u_s.toLowerCase();

       th_s=t12_v_s;
       if( t12_uh_s.length() > 0 ) th_s=th_s+" "+t12_uh_s;

       if( dt12_s.equals("LE") ) th_s=wsc.SC("LE")+" "+th_s;
       else if( dt12_s.equals("LT") ) th_s=wsc.SC("LT")+" "+th_s;
       else if( dt12_s.equals("GE") ) th_s=wsc.SC("GE")+" "+th_s;
       else if( dt12_s.equals("GT") ) th_s=wsc.SC("GT")+" "+th_s;
       else if( dt12_s.equals("AP") ) th_s=wsc.SC("AP")+" "+th_s;
       else if( dt12_s.length() > 0 ) th_s=th_s+" <i>"+dt12_s+"</i>" ;

       /* Adding an extra space to make the italics look better: */

       if(th_s.endsWith("</i>") )  th_s=th_s+"&nbsp;";

       /* Doing the unicode version without the dt12*/

       tu_s="";

       if( dt12_s.toUpperCase().equals("GT") ) tu_s="> ";
       if( dt12_s.toUpperCase().equals("GE") ) tu_s='\u2265'+" ";
       if( dt12_s.toUpperCase().equals("LT") ) tu_s="< ";
       if( dt12_s.toUpperCase().equals("LE") ) tu_s='\u2264'+" ";
       if( dt12_s.toUpperCase().equals("AP") ) tu_s='\u2248'+" ";

       tu_s=tu_s+( t12_v_s +" "+ t12_u_s ).trim();

       int ius=tu_s.indexOf("US");
       if( ius > 0 ) tu_s=tu_s.substring(0,ius)+'\u03bc'+tu_s.substring(ius+1);

       ius=tu_s.indexOf("micro-s");
       if( ius > 0 ) tu_s=tu_s.substring(0,ius)+'\u03bc'+tu_s.substring(ius+6);

       int iev=tu_s.indexOf("EV");
       if( iev > 0 ) tu_s=tu_s.substring(0,iev)+"eV"+tu_s.substring(iev+2);

       /* Doing the unicode version with the dt12*/

       tuf_s=tu_s;
       if( ! dt12_s.toUpperCase().equals("GT") || ! dt12_s.toUpperCase().equals("GE") 
        || ! dt12_s.toUpperCase().equals("LT")  || ! dt12_s.toUpperCase().equals("LE")
        || ! dt12_s.toUpperCase().equals("AP") ) tuf_s=tuf_s+" "+dt12_s;


   }/* if length>0 */
 } /* if not null */ 
 }
  private double parseMultiplier(){
	    mult=parseMultiplier(t12_u_s);
	    return mult;                
	 }
 public static double parseMultiplier(String unit){
    double mult=0;
    if(unit.equals("MEV") ) mult=4.557E-22;
    else if(unit.equals("KEV") ) mult=4.557E-19;
    else if(unit.equals("EV") ) mult=4.557E-16;
    else if(unit.equals("FS") ) mult=1e-15; 
    else if(unit.equals("PS") ) mult=1e-12; 
    else if(unit.equals("NS") ) mult=1e-9; 
    else if(unit.equals("US") ) mult=1e-6; 
    else if(unit.equals("MS") ) mult=1e-3; 
    else if(unit.equals("S") ) mult=1; 
    else if(unit.equals("M") ) mult=60; 
    else if(unit.equals("H") ) mult=60*60;
    else if(unit.equals("D") ) mult=24*60*60;  
    else if(unit.equals("Y") ) mult=365.25*24*60*60;  
    else if(unit.equals("KY") ) mult=(10E+3)*365.25*24*60*60;  
    else if(unit.equals("MY") ) mult=(10E+6)*365.25*24*60*60;  
    else if(unit.equals("GY") ) mult=(10E+9)*365.25*24*60*60;
    return mult;                
 }


/* public methods: */

  public double ValueInSeconds(){ return t12_d; }
  public double valueInSeconds(){ return t12_d; }
  public double uncInSeconds(){ return dt12_d; }

  public double symValueInSeconds(){ return 0.5*(t12_uv_d+t12_lv_d); }
  public double symUncInSeconds(){ return dt12_d; }

  public String ValueInSecondsS(){ return t12_ds; }
  public double UVInSeconds(){ return t12_uv_d; }
  public double LVInSeconds(){ return t12_lv_d;}
  public String t12html(){ if( th_s.length() > 0 ) return th_s; else return "&nbsp;"; }
  public String t12ensdf(){ return te_s;  }
  public String t12Unicode(){ return tu_s;  }
  public String t12UnicodeFull(){ return tuf_s;  }

  public String value(){return t12_v_s;}
  public String units(){return t12_u_s;}
  public String uncertainty(){return dt12_s;}
  
  public boolean dt12IsNumber(){return dt12IsNumber;}

  ///////////////////////////////////////////////////////////
  private static boolean isGoodUnit(String unit) {
		 double mult=parseMultiplier(unit);
		 if(mult<=0)
			 return false;
		 
		 return true;
	 }
  
  public static T12 parseTS(String s) {
	  try {
		  s=s.trim();
		  s=s.replace("{I","").replace("{","").replace("}","");//for 13 ns {I3}
		  
		  s=s.replace("(", " ").replace(")"," ");//for "13(3) ns"
		  
		  s=s.trim().toUpperCase();
		  String[] temp=s.split("[\\s]+");
		  if(temp.length<2)
			  return null;
		  
		  //FindValue fv=new FineValue();
		  //fv.findValueInEntry(s);
		  String unit="",ds="";
		  if(temp.length==2) {
			  //case#1: 13 NS
			  //case#2: >=13 NS (note > =13 NS is considered invalid)
			  s=temp[0];
			  unit=temp[1];
			  
			  //System.out.println(s+"   "+unit+" "+isGoodUnit(unit));
			  if(!isGoodUnit(unit))
				  return null;
			  
			  char c0=s.charAt(0);
			  int i=0;
			  while(c0!='.' && !Character.isDigit(c0)) {
				  i++;
				  if(i>s.length()-1)
					  break;
				  c0=s.charAt(i);
				 
			  }
			  //System.out.println("c0="+c0+" i="+i);
			  if(i<=s.length()-1) {
				  ds=s.substring(0,i);
				  s=s.substring(i);
			  }
			  //System.out.println(" s="+s+"  unit= "+unit+" ds="+ds+"  "+ isGoodUnit(unit));
			  if(!Str.isNumeric(s))
				  return null;
		  }else {
			  //case#1: 13 NS 3 (default)
			  //case#2: 13 3 NS
			  //case#3: 13 NS LT
			  //case#4: 13 LT NS
			  //case#5: >= 13 NS
			  //case#6: LT 13 NS
			  s=temp[0];
			  unit=temp[1];
			  ds=temp[2];
			  if(Str.isNumeric(s)) {
				  //case#1: 13 NS 3 (default)
				  //case#2: 13 3 NS
				  //case#3: 13 NS LT
				  //case#4: 13 LT NS
				  
				  if(isGoodUnit(unit) ){
					  //do nothing here
				  }else if(isGoodUnit(temp[2]) ){
					  unit=temp[2];
					  ds=temp[1];
				  }else {//no unit found
					  return null;
				  }
			  }else if(EnsdfConfig.isAnyRelationOP(temp[0]) && Str.isNumeric(temp[1]) && isGoodUnit(temp[2])){
				  //case#5: >= 13 NS
				  //case#6: LT 13 NS
				  s=temp[1];
				  unit=temp[2];
				  ds=temp[0];
				  if(ds.equals("=")) 
					  ds="";	
			  }else {
				  return null;
			  }
		  }
		  
		  //if reaching here, value (numeric) and unit is good, still need to check uncertainty
		  if(ds.length()>0 && !Str.isNumericUnc(ds)) {
			  if(EnsdfConfig.isEnsdfRelationOPUpperCase(ds)) {
				  //do nothing
			  }else if(EnsdfConfig.isEnsdfRelationOPLowerCase(ds)) {
				  int index=EnsdfConfig.ENSDF_relation_OPs_lower.indexOf(ds);
				  ds=EnsdfConfig.ENSDF_relation_OPs.get(index);
			  }else if(EnsdfConfig.isRegularRelationOP(ds)) {
				  if(ds.equals("=")) {
					  ds="";
				  }else if(ds.equals(">=")) {
					  ds="GE";
				  }else if(ds.equals("<=")) {
					  ds="LE";
				  }else {
					  //others are also in ENSDF_relation_OPs_lower
				  }
			  }else {
				  return null;
			  }
		  }
		  
		  T12 t=new T12();
		  t.setValues(s, unit, ds);
		  
		  return t;
	  }catch(Exception e) {
		  e.printStackTrace();
	  }
	  return null;
  }
}
    

