 package ensdfparser.ensdf;

public class SingleJPi{

/* 
   Assumes that a single value of JPi can be written as:
   J
   (J)
   JPi
   (Jpi)
   J(Pi)
   (J)Pi
   
   Pi
   (Pi)

*/
  private float j; @SuppressWarnings("unused")
private boolean dj; private String pi; @SuppressWarnings("unused")
private boolean dpi; private boolean canGetJ; private boolean canGetPi; private boolean canGetF;
  public SingleJPi(){
  }

// --------------------------------------------------------

  public void setValues(String st){ 

  j=-1;
  canGetJ=false;
  canGetF=false;
  pi="";
  canGetPi=false;

  String s=st;

  if( !s.equals(null) ){

      s=s.trim();

      if( s.equals("+") ){ j=-1; canGetJ=true; pi="+"; canGetPi=true;}
      else if( s.equals("(+)") ){ j=-1; canGetJ=true; pi="+"; canGetPi=true;}
      else if( s.equals("-") ){ j=-1; canGetJ=true; pi="-"; canGetPi=true;}
      else if( s.equals("(-)") ){ j=-1; canGetJ=true; pi="-"; canGetPi=true;}

      else if( s.indexOf(",") < 0 && s.indexOf("O") < 0 && s.indexOf("N") < 0 && s.indexOf("L") <0 &&
               s.indexOf("G") < 0 && s.indexOf(":") < 0 && s.indexOf("&") < 0 ){

          if( s.indexOf("+")  > 0 &&  s.indexOf("-") < 0 ){ pi="+"; canGetPi=true; }
          else if( s.indexOf("+")  < 0 &&  s.indexOf("-") > 0 ){ pi="-"; canGetPi=true; }
          else if( s.indexOf("+")  < 0 &&  s.indexOf("-") < 0 ){ pi=""; canGetPi=true; }

          String sj=s.replace('(',' ').replace(')',' ').replace('+',' ').replace('-',' ').replace('[',' ').replace(']',' ').trim();
          j=convj(sj); 
          canGetJ=canGetF;
      }
   }        
  
 }
// --------------------------------------------------------


  public float j(){ return j;}
  public boolean canGetJ(){ return canGetJ;}
  public String pi(){ return pi;}
  public int piI(){ if( pi.equals("+")) return 1; if( pi.equals("+")) return -1; else return 1;}
  public boolean canGetPi(){ return canGetPi;}

// --------------------------------------------------------


  private float convj(String st){
    String s=st.trim();

    float f=-1;
    canGetF=false;
    if( s.indexOf("/") > 0 ){
      int is=s.indexOf("/");
        try{
          float f1=Float.parseFloat( s.substring(0,is) );
          float f2=Float.parseFloat( s.substring(is+1) );
          f=f1/f2;
          canGetF=true;
        }
	catch(NumberFormatException e){
          f=-1;
          canGetF=false;
        }
    }
    else{
        try{
          f=Float.parseFloat(s);
          canGetF=true;
        }
	catch(NumberFormatException e){
          f=-1;
          canGetF=false;
        }

    }
    return f;
  }

// --------------------------------------------------------


}