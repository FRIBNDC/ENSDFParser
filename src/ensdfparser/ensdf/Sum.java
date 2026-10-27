 package ensdfparser.ensdf;

public class Sum{

  private double sum,dsum, prod, dprod;
  private int n;

  public Sum(){

    sum=0;
    dsum=0;
    n=0;

  }

  public void addElement(double x, double dx){
    if( x > 0 ){
      sum=sum+x;
      if( dx > 0 ) dsum=dsum+Math.pow( dx,2);
      n++;
    }
  }
  public void addElement(float x, float dx){
    if( x > 0 ){
      sum=sum+x;
      if( dx > 0 ) dsum=dsum+Math.pow( dx,2);
      n++;
    }
  }
  public void addAnyElement(float x, float dx){
      sum=sum+x;
      if( dx > 0 ) dsum=dsum+Math.pow( dx,2);
      n++;
  }
  public void addAnyElement(double x, double dx){
      sum=sum+x;
      if( dx > 0 ) dsum=dsum+Math.pow( dx,2);
      n++;
  }

  public void addPosElement(double x, double dx){
    if( x > 0 ){
      sum=sum+x;
      if( dx > 0 ) dsum=dsum+Math.pow( dx,2);
      n++;
    }
  }
  public void addPosElement(float x, float dx){
    if( x > 0 ){
      sum=sum+x;
      if( dx > 0 ) dsum=dsum+Math.pow( dx,2);
      n++;
    }
  }
  public void addPosProduct(float x, float dx, float a, float da){
    prod=x*a;
    dprod=0;
    if( dx > 0 ) dprod=Math.pow( dx*a , 2);
    if( da > 0 ) dprod=dprod+Math.pow( x*da , 2);
    dprod=Math.pow( dprod , 0.5 );
    if( prod > 0 ){
      sum=sum+prod;
      if( dprod > 0 ) dsum=dsum+Math.pow( dprod,2);
      n++;
    }
  }
  public void addPosProduct(double x, double dx, double a, double da){
    prod=x*a;
    dprod=0;
    if( dx > 0 ) dprod=Math.pow( dx*a , 2);
    if( da > 0 ) dprod=dprod+Math.pow( x*da , 2);
    dprod=Math.pow( dprod , 0.5 );
    if( prod > 0 ){
      sum=sum+prod;
      if( dprod > 0 ) dsum=dsum+Math.pow( dprod,2);
      n++;
    }
  }


  public double d(){return sum; }
  public double dd(){return Math.pow(dsum,0.5); }
  public float f(){return (float) sum; }
  public float df(){return (float) Math.pow(dsum,0.5); }
  public int n(){return n;}

  public String s(){
    XDX2SDS x2s=new XDX2SDS();
    x2s.setValues(d(),dd());
    return x2s.S();
  }

  public String ds(){
    XDX2SDS x2s=new XDX2SDS();
    x2s.setValues(d(),dd());
    return x2s.DS();
  }

}