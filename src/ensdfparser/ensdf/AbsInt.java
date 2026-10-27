 package ensdfparser.ensdf;

import ensdfparser.nds.ensdf.EnsdfUtil;

public class AbsInt{
  private double ai_d=-1, dai_d=-1, daill_d=-1;
  private String ai_s, dai_s;
  private double n=-1; double dn=-1; double i=-1; double di=-1;

  public AbsInt(){
    ai_d=-1;
    dai_d=-1;
    daill_d=-1;
    
    n=-1;dn=-1;i=-1;di=-1;
    
    ai_s="";
    dai_s="";
  }

  public void setValues(double xn, double xdn, double xi, double xdi){ 
     n=xn; dn=xdn; i=xi; di=xdi; 
     calculate();
  }
  public void setValues(double xn, double xdn, float xi, float xdi){ 
    n=xn; dn=xdn; i=(double) xi; di= (double) xdi; calculate();  
  }
  public void setValues(float xn, float xdn, float xi, float xdi){ 
    n=(double) xn; dn=(double) xdn; i=(double) xi; di= (double) xdi; calculate();  
  }

  public void setValues(String NS,String DNS,String IS,String DIS){
	   SDS2XDX NORM=new SDS2XDX();NORM.setValues(NS, DNS);
       SDS2XDX RI=new SDS2XDX();RI.setValues(IS, DIS);
       SDS2XDX ARI=null;
       
       if(EnsdfUtil.isUnity(NS,DNS))
    	   ARI=RI;
       else if(EnsdfUtil.isUnity(IS, DIS))
    	   ARI=NORM;
       else
    	   ARI=RI.multiply(NORM);
           
       ai_d=ARI.x();
       dai_d=ARI.dx();
       daill_d=dai_d;
       
       ai_s=ARI.s();
       dai_s=ARI.ds();
  }
  
  private void calculate(){ 

    ai_d=-1;
    dai_d=-1;
    ai_s="";
    dai_s="";

    XDX2SDS norm=new XDX2SDS(n,dn);
    XDX2SDS ri=new XDX2SDS(i,di);
    
    setValues(norm.s(),norm.ds(),ri.s(),ri.ds());
    
    /*
    if( n >= 0 && i >= 0 ){
        ai_d=n*i;

        if( dn >= 0 && di>=0) {
      	  dai_d=dai_d+Math.pow( dn*i,2);
      	  dai_d=dai_d+Math.pow( n*di,2);
        }

        if( dai_d >= 0 ) dai_d=Math.pow( dai_d, 0.5);

        daill_d=dai_d;

        if( ai_d == 0 ){;
           ai_s="0";
           dai_s="0";
        }else{
            XDX2SDS s = new XDX2SDS();
            s.setValues(ai_d, dai_d);
            ai_s=s.S();
            dai_s=s.DS();
         }

        // very important, if the unnormalized intensity has a negative uncertainty, put the norm. unc=0 
        //if( di < 0 ){ dai_d=0.0; dai_s=""; }
   
    }
    */
  }

public double AId(){ return ai_d; }
public double DAId(){ return dai_d; }
public double DAILLd(){ return daill_d; }

public float AIf(){ return (float) ai_d; }
public float DAIf(){ return (float) dai_d; }
public float DAILLf(){ return (float) daill_d; }

public String AIs(){ return ai_s; }
public String DAIs(){ return dai_s; }

}