 package ensdfparser.ensdf;

 public class MirdElement{

  private double y,e,ye;
  private String ys,es,yes;
  private X2SMIRD x2sm;

  private String label;
  public MirdElement(){

    y=0;
    e=0;
    ye=0;
    label="";

  }

  public void setValues(String ilabel, double iy, double ie, double iye){
    label=ilabel; y=iy; e=ie; ye=iye;
    x2sm=new X2SMIRD();

    ys=x2sm.s( y , 2 );
    es=x2sm.s( e , 3 );
    yes=x2sm.s( ye , 2 );

  }
  public double y(){return y; }
  public double e(){return e; }
  public double ye(){return ye; }
  public String label(){ return label;}
  public String ys(){ return ys;}
  public String es(){ return es;}
  public String yes(){ return yes;}

}