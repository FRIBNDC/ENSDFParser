package ensdfparser.ensdf;

import java.util.*;

public class ICCEstimator{
  private double icc_d, dicc_d, eg, deg;
  private String icc_s, dicc_s;

  public ICCEstimator(){
    icc_d=0; dicc_d=0; icc_s=""; dicc_s="";
  }
  
  public void setValues(Vector<Double> eV, Vector<Double> iccV, double ieg, double ideg){

    eg=ieg; deg=ideg;

    Spline s=new Spline(); 

    icc_d=s.getY(eV,iccV,eg); 

    dicc_d=addQuad( icc_d, s.getDY());

    XDX2SDS x2s=new XDX2SDS();

    x2s.setValues(icc_d, dicc_d);
 
    icc_s=x2s.s(); dicc_s=x2s.ds();
  }
  private double addQuad(double f, double df2dg){
    return Math.pow(   Math.pow( df2dg*deg , 2 ) + Math.pow( 0.03*f , 2 ) , 0.5 );
  } 
  public void setValues(double id, double idd, String is, String ids){
    icc_d=id; dicc_d=idd; icc_s=is; dicc_s=ids;
  }
  public double cc(){ return icc_d;}
  public double dcc(){ return dicc_d;}
  public String ccs(){ return icc_s;}
  public String dccs(){ return dicc_s;}

}