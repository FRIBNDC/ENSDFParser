 package ensdfparser.ensdf;

public class Dose{
  private double dose_d,  ddose_d;
  private String dose_s, ddose_s;

  private double dosem_d,  ddosem_d;
  private String dosem_s, ddosem_s;

 
  public Dose(){
    dose_d=-1;
    ddose_d=-1;
    dose_s="";
    ddose_s="";

    dosem_d=-1;
    ddosem_d=-1;
    dosem_s="";
    ddosem_s="";
  }
  public void setValues(float xe, float xde, double xi, double xdi){ 

    double e=(double) xe; double de=(double) xde; double i=xi; double di=xdi;

    /* Note that intensities inputs are percentual ones */

    if( e >= 0 && i >= 0 ){
      double x=e*i;
      double dx=0;
      
      if( di > 0 ) dx= dx+ Math.pow( e*di , 2 );
      if( de > 0 ) dx= dx + Math.pow( de*i , 2 );
      if( dx > 0 ) dx=Math.pow( dx , 0.5) ;

      /* The Mird format uses radiations in MeV, and the intensities have to be divided by 100 */

      dosem_d=x*0.00001; ddosem_d=dx*0.00001;  

      XDX2SDS s = new XDX2SDS();
      s.setValues(dosem_d, ddosem_d);
      dosem_s=s.S();
      ddosem_s=s.DS();

      dose_d=0.0000213*x;//convert keV/(100 disintegrations) to units of (gm*rad)/(muCi*h)
      ddose_d=0.0000213*dx;
      XDX2SDS t = new XDX2SDS();
      t.setValues(dose_d, ddose_d);
      dose_s=t.S();
      ddose_s=t.DS();
      
      /* if the energy or intensity uncertainties are not know, then set them equal to 0 and "" */

      if( ! ( di >= 0 && de >= 0 ) ){
        ddose_d=0;ddose_s=""; ddosem_d=0; ddosem_s="";
      }


    }
  }

public double DoseD(){ return dose_d; }
public double DDoseD(){ return ddose_d; }
public float DoseF(){ return (float) dose_d; }
public float DDoseF(){ return (float) ddose_d; }
public String DoseS(){ return dose_s; }
public String DDoseS(){ return ddose_s; }

public float MirdDoseF(){ return (float) dosem_d; }
public float MirdDDoseF(){ return (float) ddosem_d; }
public String MirdDoseS(){ return dosem_s; }
public String MirdDDoseS(){ return ddosem_s; }

}