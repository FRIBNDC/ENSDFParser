package ensdfparser.ensdf;


public class ICC {

    //shell name or ICC name:
    //  TOT
    //  K
    //  L1,L2,L3, L-TOT, K/L
    //  M1,M2,M3,M4,M5,M-TOT,L/M
    //  N1,N2,N3,N4,N5,N6,N7,N-TOT,L/N
    //  O1,O2,O3,O4,O5,O-TOT,L/O
    //  P1,P-TOT,L/P
    
    public String name="";//shell name
    public String mult="";//multpolarity
    
    public double x=-1,dx=-1;
    public double dxMRL=-1,dxMRU=-1;//uncertainty corresponding to lower and upper uncertainties of MR (no sign)
    public String s="",ds="";
    public boolean isMixed=false,isExp=false;
    
    public ICC() {
        
    }
    public ICC(String name,String mult,double icc,double dicc,int errorLimit) {setValues(name,mult,icc,dicc,errorLimit);}
    public ICC(String name,String mult,double icc,double dicc) {setValues(name,mult,icc,dicc);}
    public ICC(String name,String mult,double icc) {setValues(name,mult,icc);}
    
    public ICC(String name,String mult,String iccs,String diccs,int errorLimit) {setValues(name,mult,iccs,diccs,errorLimit);}
    public ICC(String name,String mult,String iccs,String diccs) {setValues(name,mult,iccs,diccs);}
    public ICC(String name,String mult,String iccs) {setValues(name,mult,iccs);}
    
    public void setValues(String name,String mult,double icc,double dicc,int errorLimit) {
        this.name=name;
        this.mult=mult;
        x=icc;
        dx=dicc;
        
        XDX2SDS x2s=new XDX2SDS(x,dx);
        x2s.setErrorLimit(errorLimit);
        
        s=x2s.s();
        ds=x2s.ds();
        
        mult=mult.toUpperCase();
        if(mult.contains("+") || mult.equals("MIXED"))
        	isMixed=true;
    }
    
    public void setValues(String name,String mult,double icc,double dicc) {
        setValues(name,mult,icc,dicc,35);
    }
    public void setValues(String name,String mult,double icc) {
        setValues(name,mult,icc,-1,35);
    }    
    
    public void setValues(String name,String mult,String iccs,String diccs,int errorLimit) {
        this.name=name;
        this.mult=mult;
        s=iccs;
        ds=diccs;
        
        SDS2XDX s2x=new SDS2XDX(s,ds);
        s2x.setErrorLimit(errorLimit);
        
        x=s2x.x();
        dx=s2x.dx();
        
        mult=mult.toUpperCase();
        if(mult.contains("+") || mult.equals("MIXED"))
        	isMixed=true;
    }
    public void setValues(String name,String mult,String iccs,String diccs) {
        setValues(name,mult,iccs,diccs,35);
    }
    public void setValues(String name,String mult,String iccs) {
        setValues(name,mult,iccs,"",35);
    } 
    
    public String name() {return name;}
    public String mult() {return mult;}
    
    public double x() {return x;}
    public double dx() {return dx;}
    public String s() {return s;}
    public String ds() {return ds;};
    
    public boolean isMixed() {return isMixed;}
    public boolean isExp() {return isExp;}
    
    
}
