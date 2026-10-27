package ensdfparser.ensdf;

public abstract class Decay extends Record{

	private String er_s="",der_s="";
	
    abstract public void setFLI(int FLI);
    abstract public void setFLS(String FLS);
    abstract public void setJFS(String JFS);
    abstract public void setES(String es);
    abstract public void setDES(String des);
    
    abstract public void setEF(float ef);
    abstract public void setDEF(float def);
    
    abstract public int getFLI();
    abstract public String getFLS();
    abstract public String getJFS();    
    
    abstract public String ES0();
    abstract public String DES0();
    
    abstract public String ES();
    abstract public String DES();
    abstract public float EF();
    abstract public float DEF();

    
    abstract public String recoilES();
    abstract public String recoilDES();
    
    abstract public String RIS();
    abstract public String DRIS();
    abstract public double ID();
    abstract public double DID();
    
    abstract public String IES();//for ECBP
    abstract public String DIES();
    abstract public String ITS();
    abstract public String DITS();
    
    abstract public String HFS();//for ALPHA
    abstract public String DHFS();
    
    abstract public String LOGFTS();//for B- and ECBP
    abstract public String DLOGFTS();


    
    abstract public void setRecoilES(String s);
    abstract public void setRecoilDES(String s);
    
    abstract public void setAIS(String s);
    abstract public void setDAIS(String s);
    abstract public String AIS();
    abstract public String DAIS();
      
    abstract public void setEAvS(String s);
    abstract public void setDEAvS(String s);
    abstract public String EAvS();
    abstract public String DEAvS();
    
    abstract public void setEAvF(float f);
    abstract public void setDEAvF(float f);
    abstract public float EAvF();
    abstract public float DEAvF();
    
    abstract public void setAID(double d);
    abstract public void setDAID(double d);
    abstract public double AID();
    abstract public double DAID();
     
    abstract public void setDoseF(float f);
    abstract public void setDDoseF(float df);
    abstract public float DoseF();
    abstract public float DDoseF();
    
    abstract public void setDoseS(String s);
    abstract public void setDDoseS(String ds);
    abstract public String DoseS();
    abstract public String DDoseS();
    
      
    abstract public void setRecoilDoseS(String s);
    abstract public void setRecoilDDoseS(String s);
    abstract public String recoilDoseS();
    abstract public String recoilDDoseS();

    abstract public void setRecoilDoseF(float f);
    abstract public void setRecoilDDoseF(float f);
    abstract public float recoilDoseF();
    abstract public float recoilDDoseF();
    
    abstract public String flagS();
    abstract public void addFlag(String s);
    abstract public String flag();
    abstract public String coinS();
    abstract public String QS();
    abstract public String q();
    abstract public String US();
    abstract public String unique();

    abstract public String decayType();

    abstract public String dpType();
    abstract void setDPType(String type);
    
    
    public String ERS() {return er_s;}
    public String DERS() {return der_s;}
    public void setERS(String s) {er_s=s;}
    public void setDERS(String s) {der_s=s;}
}
