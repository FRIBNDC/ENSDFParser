package ensdfparser.ensdf;

import java.util.*;

public class Spline{

  private double a,b,c,d,x0,alpha,beta,gamma,delta, x[],y[],func,dfunc;
  private Vector<Double> ix=new Vector<Double>(); private Vector<Double> iy=new Vector<Double>();
  public Spline(){

  }
  public double getY(Vector<Double> iix, Vector<Double> iiy, double z){
    func=0; dfunc=0;
    ix.clear(); iy.clear();

    for( int j=0; j<iix.size();j++){
      ix.addElement(  (Double) iix.elementAt(j)  );
      iy.addElement( new Double( Math.log( ( (Double) iiy.elementAt(j) ).doubleValue() ) ));
    }

    int ji=0;  
    if( z < ix(2) ){ 
       ji=0;
    }
    else if( z > ix( ix.size()-3 ) ){
       ji=ix.size()-4;
    }
    else{
      for( int j=0; j<ix.size();j++){
         if( z >= ix(j) && z <= ix(j+1) ){ ji=j-1;}
      }
    }

    double lx[]=new double[4]; double ly[]=new double[4];

    for( int j=ji; j<ji+4;j++){
      lx[j-ji]=ix(j); ly[j-ji]=iy(j);
    }

    getABCD(lx,ly);

    func=Math.exp( a+b*(z-x0)+c*Math.pow(z-x0,2)+d*Math.pow(z-x0,3) );

    dfunc=func*( b+2*c*(z-x0)+3*d*Math.pow(z-x0,2) );

    return func;
  }
  public double getDY(){return dfunc;}

  private double ix(int j){return ( (Double) ix.elementAt(j) ).doubleValue(); }
  private double iy(int j){return ( (Double) iy.elementAt(j) ).doubleValue(); }

  public void getABCD( double ix[], double iy[]){
    x=new double[4]; y=new double[4];
    for( int j=1; j<=3; j++){
      y[j]=iy[j]-iy[0];
      x[j]=ix[j]-ix[0];
    }
    x0=ix[0];
    alpha= ( y[2]-y[3]*x[2]/x[3] ) / ( x[2]*x[2] - x[2]*x[3] );
    beta= ( x[3]*x[3]*x[2] - x[2]*x[2]*x[2] )/ ( x[2]*x[2] - x[2]*x[3] );
    gamma= y[3]/x[3] - alpha*x[3];
    delta=-beta*x[3] - x[3]*x[3];
    d= ( y[1] - gamma*x[1] - alpha*x[1]*x[1] ) / ( delta*x[1] + beta*x[1]*x[1] + x[1]*x[1]*x[1] );
    b=gamma+delta*d;
    c=alpha+beta*d;
    a=iy[0];
//    System.out.println( a+" "+b+" "+c+" "+d );
 }
}