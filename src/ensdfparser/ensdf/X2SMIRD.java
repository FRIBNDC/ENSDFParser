 package ensdfparser.ensdf;

public class X2SMIRD{

  /* 
     Converts a double or a float number to a String to be used in MIRD,
     for instance, if d=123123 then s=1.23123x10+5 if i=5, s=1.23x10+5 if i=2
  */

  private String s;

  private int i;
  private double d;

  public X2SMIRD(){

    s=""; 

  }

  public void setValues(double id, int ii){
    d=id;
    i=ii;
    doWork();
  }
  public void setValues(float id, int ii){
    d=id;
    i=ii;
    doWork();
  }

  public void doWork(){
    if( d != 0 ){
      s="";
      double f=Math.floor( Math.log(d)/Math.log(10.) );
      double dnew=d*Math.pow(10.0,-f);

      s=""+dnew;
      int id=s.indexOf(".");
      if( (id+i+1) <= s.length() ) s=s.substring( 0,id+i+1  );
      else{
        for( int j=0; j<= ( (id+i+1) - s.length()) ;j++ ){
          s=s+"0";
        }
      }
      if( f < 0 ) s=s+"&times;10<sup>"+((int) f)+"</sup>";
      if( f > 0 ) s=s+"&times;10<sup>+"+((int) f)+"</sup>";
    }
    else{ s="0.0";}
  }
  public String S(){return s;}
  public String s(){return s;}

  public String se(float ixf, int ii){
    d=ixf;
    i=ii;
    doWork();
    return s;
  }
  public String s(double ixd, int ii){
    d=ixd;
    i=ii;
    doWork();
    return s;
  }
}