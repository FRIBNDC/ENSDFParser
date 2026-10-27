/* Calculates Conversion Electrons Energy and Intensity */

package ensdfparser.ensdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;


public class CE{

  private int Z=-1;
  
  private float eg, deg, be, dbe, cc, dcc;
  private float fot,dfot;//fraction over the total intensity
  private double ig, dig;
  private double e, de, i, di, ai, dai;
  private String es, des, is, dis, ais, dais,egs,degs;
  private String dose_s, ddose_s; private float dose_f, ddose_f;
  private String type;
  private String element="";
  
  private int gIndex=-1;//index of gamma
  
  //for storing original value and uncertainty of gamma intensity RI (either from RI field or deduced from TI field if RI is empty)
  //for the cases where symmetrized RI and DRI are used to set CE intensity, so that they can be used if CE intensity needs to be
  //reset with original RI and DRI or values symmetrized differently
  private String originalIGS="",originalDIGS="";
  
  public CE(){
  element="";
  Z=-1;
  
  es=""; des=""; is=""; dis=""; ais=""; dais="";
  e=-1; de=-1; i=-1; di=-1; ai=-1; dai=-1; gIndex=-1;
  cc=0;dcc=0;
  originalIGS="";originalDIGS="";
  egs="";degs="";
  
  fot=-1;dfot=-1;
  
  dose_s=""; ddose_s=""; dose_f=(float) -1.0;  ddose_f=(float) -1.0;
  type="";
  }

  public void setValues( float xeg, float xdeg, float xbe, float xdbe, double xig, double xdig, float xcc, float xdcc, String xtype){
    eg=xeg;
    deg=xdeg;
    be=xbe;
    dbe=xdbe;
    ig=xig;
    dig=xdig;
    cc=xcc;
    dcc=xdcc;
    type=xtype.trim();
    
    if( eg > be ){

        e=eg-be;
        de=0;
        if( deg > 0 ) de=de+Math.pow( deg,2);
        if( dbe > 0 ) de=de+Math.pow( dbe,2);
        if(de > 0 ) de=Math.pow(de,0.5);

        i=ig*cc;
        di=0;

        if( dig > 0 ) di=di+Math.pow( (dig*cc), 2);
        if( dcc > 0 ) di=di+Math.pow( (ig*dcc), 2);
        if( di > 0 ) di=Math.pow(di, 0.5); 

        XDX2SDS xe=new XDX2SDS();
        xe.setValues(e,de);
        es=xe.S();
        des=xe.DS();

        XDX2SDS xi=new XDX2SDS();
        xi.setValues(i,di);
        is=xi.S();
        dis=xi.DS();

    }

  }

  public void setIceFromTI(double ti,double dti,float fot,float dfot) {
      i=ti*fot;
 
      if(dti<0) dti=0;
      if(dfot<0) dfot=0;
      
      di=Math.sqrt(dti*dti*fot*fot+ti*ti*dfot*dfot);

      XDX2SDS xi=new XDX2SDS();
      xi.setValues(i,di);
      is=xi.S();
      dis=xi.DS();	  
  }
  public void setFracOverTotal(float f,float df) {
	  fot=f;
	  dfot=df;
  }
  
  public void setZ(int Z) {this.Z=Z;}
  public void setElement(String ele) {element=ele.toUpperCase().trim();}
  
  int Z() {return Z;}
  String element() {return element;}
  
  public float FOT() {return fot;}
  public float DFOT() {return dfot;}
  
  public void setOriginalIGS(String IGS,String DIGS) {
	  originalIGS=IGS;
	  originalDIGS=DIGS;
  }
  
  public String originalIGS() {return originalIGS;}
  public String originalDIGS() {return originalDIGS;}
  
  public String ES()
  { 	  
	  return es.toLowerCase().replace("e", "E").replace("s(", "S(");
  }
  public String DES(){ return des;}

  public float EF(){ return (float) e;}
  public float DEF(){ return (float) de;}

  public String IS(){ return is;}
  public String DIS(){ return dis;}

  public double ID(){ return i;}
  public double DID(){ return di;}

  public float EG() {return eg;}
  public float DEG() {return deg;}

  public String EGS() {return egs;}
  public String DEGS() {return degs;}
  public void setEGS(String s,String ds) {egs=s;degs=ds;}
  
  public float IG() {return (float) ig;}
  public float DIG() {return (float) dig;}
  
  public float CC(){return cc;}
  public float DCC() {return dcc;}
  
  public float BE() {return be;}
  public float DBE() {return dbe;}
  
  public void setAIS(String s){ ais=s;}
  public void setDAIS(String s){ dais=s;}
  public String AIS(){ return ais;}
  public String DAIS(){ return dais;}

  public void setAID(double d){ ai=d;}
  public void setDAID(double d){ dai=d;}
  public double AID(){ return ai;}
  public double DAID(){ return dai;}

  public void setDoseS(String s){ dose_s=s;}
  public void setDDoseS(String s){ ddose_s=s;}
  public String DoseS(){ return dose_s;}
  public String DDoseS(){ return ddose_s;}

  public void setDoseF(float f){ dose_f=f;}
  public void setDDoseF(float f){ ddose_f=f;}
  public float DoseF(){ return dose_f;}
  public float DDoseF(){ return ddose_f;}


  public String type(){ return type;}
  
  public void setGamIndex(int i) {gIndex=i;}
  public int gamIndex() {return gIndex;}
  
  public static Vector<CE> sort(Vector<CE> cesV){
	  ArrayList<CE> list=new ArrayList<CE>();
	  list.addAll(cesV);
	  
	  Collections.sort(list, new CEComparator());
	  
	  Vector<CE> out=new Vector<CE>();
	  out.addAll(list);
	  
      return out;	  
  }

}
class CEComparator implements Comparator<Object>{
    public int compare(Object one,Object two){
        CE a=(CE)one;
        CE b=(CE)two;
        int out;
        if(a.EF()<b.EF())out=-1;
        else if(a.EF()>b.EF())out=1;
        else{        	
    		float eg1=a.EG();
    		float eg2=b.EG();
        	if(eg1<eg2)
        		out=-1;
        	else if(eg1>eg2)
        		out=1;
        	else
        		out=0;

        }
        
        return out;
    }
}