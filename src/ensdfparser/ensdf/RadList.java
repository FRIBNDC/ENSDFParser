 package ensdfparser.ensdf;

public class RadList{

  private String rt_s;

  private String rl_s;

  private String e_s, de_s; private float e_f, de_f;

  private String i_s, di_s; private double i_d, di_d;

  private String dose_s, ddose_s; private float dose_f, ddose_f;

  private String epe_s,  depe_s;

  private String q_s;



  public RadList(){

    rt_s=""; rl_s="";

    e_s=""; de_s=""; e_f=(float)-1.0; de_f=(float)-1.0;

    i_s=""; di_s=""; i_d=-1.0; di_d=-1.0;

    q_s="";

    dose_s=""; ddose_s=""; dose_f=(float) -1.0;  ddose_f=(float) -1.0;

    epe_s=""; depe_s="";
  }

  public String radType(){ return rt_s;}
  public String radLabel(){ return rl_s;}

  public void setRadType(String s){ rt_s=s;}
  public void setRadLabel(String s){ rl_s=s;}

  public void setES(String s){ e_s=s;}
  public void setDES(String s){ de_s=s;}
  public String ES(){ return e_s;}
  public String DES(){ return de_s;}

  public void setEF(float f){ e_f=f;}
  public void setDEF(float f){ de_f=f;}
  public float EF(){ return e_f;}
  public float DEF(){ return de_f;}

  public void setIS(String s){ i_s=s;}
  public void setDIS(String s){ di_s=s;}
  public String IS(){ return i_s;}
  public String DIS(){ return di_s;}

  public void setID(double d){ i_d=d;}
  public void setDID(double d){ di_d=d;}
  public double ID(){ return i_d;}
  public double DID(){ return di_d;}

  public void setDS(String s){ dose_s=s;}
  public void setDDS(String s){ ddose_s=s;}
  public String DS(){ return dose_s;}
  public String DDS(){ return ddose_s;}

  public void setDF(float f){ dose_f=f;}
  public void setDDF(float f){ ddose_f=f;}
  public float DF(){ return dose_f;}
  public float DDF(){ return ddose_f;}

  public void setEPES(String s){ epe_s=s;}
  public void setDEPES(String s){ depe_s=s;}
  public String EPES(){ return epe_s;}
  public String DEPES(){ return depe_s;}

  public void setQS(String s){ q_s=s;}
  public String QS(){ return q_s;}


}