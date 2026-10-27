package ensdfparser.calc;

import java.util.Arrays;
import java.util.Vector;

import ensdfparser.ensdf.XDX2SDS;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

public class XDX{
	public double x=-1;
	public double dxu=-1;
	public double dxl=-1;
	
    public double dxu0=-1,dxl0=-1;//temp dx to still calculate if any has dx (even if others have no good dx)
	  
	public static double largeErrorLimit=0.1;//beyond this value, a relative uncertainty is considered large 
	                                  //and max&min for error propagation should be used
	

	//Note that this only applies to large uncertainty (isLargeUnc=true)
	public static boolean isUseMaxMinEPByDefault=false;//set once to apply all instances
	  
	public boolean isCrossZero=false;
	public boolean isLargeUnc=false;
	public boolean isLimit=false;  //0.8 +4-8 is also considered as limit
	//NOTE: string representation of x, dxu , dxl in real format (NOT ENSDF style)
	//      for positive dxu and dxl
	public String s="",dsu="",dsl="";
	
	public XDX(){};
	
	//NOTE: dxu=0 has dsu="0.0", not ""
	//      dsu="0" has dxu=0
	//      but dsu="" has dxu=-1 not 0
	
	public XDX(double x,double dxu,double dxl){
		setValues(x,dxu,dxl);
	}

	
	public XDX(double x,double dx){
		this(x,dx,dx);		
	}
	
	/*
	 * dsu and dsl: string representation of real values
	 * or non-numerical uncertainties, like "", "LT", "SY", etc
	 * For numerical dsu and dsl, they must be non-negative values,
	 * If any of them is negative, both are considered invalid 
	 * uncertainties and discarded
	 */
	public XDX(String s,String dsu,String dsl){
		this.s=s;
		this.dsl=dsl;
		this.dsu=dsu;
		
		try {
			x=Double.parseDouble(s);
			
            if( (dsu.startsWith("L")||dsu.startsWith("G")) || (dsl.startsWith("L")||dsl.startsWith("G")) ){
                isLimit=true;
            }
            
			dxu=Double.parseDouble(dsu);
			dxl=Double.parseDouble(dsl);
			if(dxu<0 || dxl<0) {
				dxu=-1;
				dxl=-1;
				dsl="";
				dsu="";
			}
			
			if(!isLimit && dxl>=0 && dxu>=0) {
				//0(0) is not a limit
	            if( (x>=0&&x<=dxl&&(x+dxl)>0) || (x<=0&&(x+dxu)>=0&&(x-dxu)<0) ) 
	                isLimit=true;
			}

			if(dxu0<0) dxu0=dxu;
			if(dxl0<0) dxl0=dxl;
		}catch(Exception e) {		

		}
	}
	
	public XDX(String s,String ds){
		this(s,ds,ds);
	}
	
	public void setValues(double x,double dx){
		setValues(x,dx,dx);
	}
	public void setValues(double x,double dxu,double dxl){
		this.x=x;
		
		//for dxu and dxl to be valid uncertainties:
		//both have to be non-negative
		//If any one is negative, then the other one 
		//is discarded even it is non-negative
		//To set limit, use XDX(s,dsu,dsl) constructor
		
		s=String.valueOf(x);
		if(dxu>=0 && dxl>=0) {
			this.dxu=dxu;
			this.dxl=dxl;
			
			double max=Math.max(dxu, dxl);
			double ax=Math.abs(x);
			
			isLargeUnc=false;
			if(max>ax || (ax>0&&max/ax>largeErrorLimit) )
				isLargeUnc=true;
			
			dsu=String.valueOf(dxu);
			dsl=String.valueOf(dxl);
			
			isCrossZero=false;
			if( (x>=0&&(x-dxl)<0) || (x<=0&&(x+dxu)>0) )
				isCrossZero=true;
			
            if( (x>=0&&x<=dxl&&(x+dxl)>0) || (x<=0&&(x+dxu)>=0&&(x-dxu)<0) ) 
                isLimit=true;
		}
		
		if(dxu0<0) dxu0=dxu;
		if(dxl0<0) dxl0=dxl;
	}
	
	public void setSDS() {
		try {
			s=String.valueOf(x);
			
			if(dxu>=0) {
				dsu=String.valueOf(dxu);
			}else if(Str.isNumeric(dsu)||dsu.contains("+")) {
				dsu="";
			}
			
			if(dxl>=0) {
				dsl=String.valueOf(dxl);
			}else if(Str.isNumeric(dsl)||dsl.contains("+")) {
				dsl="";
			}
			
			if(dxu0<0) dxu0=dxu;
			if(dxl0<0) dxl0=dxl;
		}catch(Exception e) {
			
		}
	}
	
	public boolean checkIsCrossZero() {
		isCrossZero=false;
		if( (x>=0&&(x-dxl)<0) || (x<=0&&(x+dxu)>0) )
			isCrossZero=true;        
        return isCrossZero;
	}
	public boolean checkIsLimit() {
		isLimit=false;
        if( (x>=0&&x<=dxl&&(x+dxl)>0) || (x<=0&&(x+dxu)>=0&&(x-dxu)<0) ) 
            isLimit=true;
        
        return isLimit;
	}
	
	public double x(){return x;}
	public double dxl(){return dxl;}
	public double dxu(){return dxu;}
	
	public String s() {return s;}
	public String dsu() {return dsu;}
	public String dsl() {return dsl;}
	
	public XDX copy() {
		XDX out=new XDX();
		out.x=x;
		out.dxu=dxu;
		out.dxl=dxl;
		
		out.dxu0=dxu0;
		out.dxl0=dxl0;
		
		out.s=s;
		out.dsu=dsu;
		out.dsl=dsl;
		
		out.isLimit=isLimit;
		out.isCrossZero=isCrossZero;
		out.isLargeUnc=isLargeUnc;
		
		return out;
	}
	
	public static void setLargeErrorLimitRatio(double b) {
		if(b>0.01 && b<0.50)
			largeErrorLimit=b;			
	}
	public static double largeErrorLimitRatio() {return largeErrorLimit;}
	
	public void checkSDS() {
    	try{
    		s=String.valueOf(x);
    		if(!dsu.isEmpty() & !Str.isNumeric(dsu)) {
    			dxu=-1;
    			dxl=-1;
    		}else if(dxu>=0) {
    			dsu=String.valueOf(dxu);
    			dsl=String.valueOf(dxl);
    		}
    	}catch(Exception e) {
    		
    	}
	}

	public void reset() {
		x=-1;
		dxu=-1;
		dxl=-1;
		
		dxu0=-1;
		dxl0=-1;
		
		s="";
		dsu="";
		dsl="";

	}
	
	public boolean isCrossZero() {return isCrossZero;}
	
	public boolean isLimit() {return isLimit;}
	
	public XDX clone(){return copy();}
	
	@SuppressWarnings("unused")
	private XDX dividedS2XNonNumericalUnc(XDX s2x,double x1,double dxu1,double dxl1,int maxNsig) {
        String ds2=s2x.dsu;
        double x2=s2x.x;
        
        if(s2x.dxu>=0 && s2x.dxl>=0)
        //if(Str.isNumeric(ds2)|| (ds2.contains("+")) || x1==0)
            return null;
        
        double x=x2/x1;
        
        double minX=0,maxX=0,limitX=0;
        
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
        
        boolean isX1CrossZero=false;
        
        if(dxu1>=0 && dxl1>=0) {
        	isX1CrossZero=((x1+dxu1)*(x1-dxl1)<=0);
        }else {
            if(dxu1<0)
                dxu1=0.25*Math.abs(x1);
            if(dxl1<0)
                dxl1=0.25*Math.abs(x1);
        }

          
        XDX result=new XDX();

        double dxu2=0,dxl2=0;
        String ds=ds2;
        if(ds2.isEmpty()) {
        	dxu2=0.2*Math.abs(x2);//assumed 20% unc for empty unc
        	dxl2=dxu2;

    		if(s2x.dxu0>=0) dxu2=s2x.dxu0;
    		if(s2x.dxl0>=0) dxl2=s2x.dxl0;
        }else if(ds2.startsWith("L")) {
        	if(x2>0) {
            	dxu2=0;
            	dxl2=x2; 
            	
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1+dxu1);
            		maxX=x2/(x1-dxl1);
            	}
        	}else {
        		dxu2=0;
        		dxl2=-10*x2;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1-dxl1);
            		maxX=x2/(x1+dxu1);
            	}
        	}	
        	
        	if(x1<0) {
        		ds="G"+ds2.substring(1);
        		limitX=minX;
        	}else {
        		limitX=maxX;
        	}
        }else if(ds2.startsWith("G")) {
        	if(x2>0) {
                dxu2=10*x2;
                dxl2=0;
                
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1+dxu1);
            		maxX=x2/(x1-dxl1);
            	}
        	}else {
        		dxu2=-x2;
        		dxl2=0;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1-dxl1);
            		maxX=x2/(x1+dxu1);
            	}
        	}

        	if(x1<0) {
        		ds="L"+ds2.substring(1);
        		limitX=maxX;
        	}else {
        		limitX=minX;
        	}
        }else {
        	dxu2=0.5*Math.abs(x2);
        	dxl2=dxu2;
        }
        
        
        // result is x2/x1
    	if(x1>=0 && x2>=0) {
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x1<0 && x2<0) {
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x1>0) {
    		//x2<0
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x2>0) {
    		//x1<0
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
    	}else {
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}

        XDX temp =new XDX(x,dxu1,dxl1);
        
        if(limitX!=0)
        	x=limitX;
        
        result=new XDX(x,-1);
        
        result.dsu=ds;
        result.dsl=ds;
        
        result.dxu0=temp.dxu;
        result.dxl0=temp.dxl;
        
        return result;
    }
	
	@SuppressWarnings("unused")
	private XDX dividedS2XNonNumericalUnc(double x1,double dxu1,double dxl1,XDX s2x,int maxNsig) {
        String ds2=s2x.dsu;
        double x2=s2x.x;
        
        if(s2x.dxu>=0 && s2x.dxl>=0)
        //if(Str.isNumeric(ds2)|| (ds2.contains("+")) || x2==0)
            return null;
        
        double x=x1/x2;
        
        double minX=0,maxX=0,limitX=0;
        
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
        
        boolean isX1CrossZero=false;
        
        if(dxu1>=0 && dxl1>=0) {
        	isX1CrossZero=((x1+dxu1)*(x1-dxl1)<=0);
        }else {
            if(dxu1<0)
                dxu1=0.25*Math.abs(x1);
            if(dxl1<0)
                dxl1=0.25*Math.abs(x1);
        }

          
        XDX result=new XDX();

        
        double dxu2=0,dxl2=0;
        String ds=ds2;
        if(ds2.isEmpty()) {
        	dxu2=0.2*Math.abs(x2);//assumed 20% unc for empty unc
        	dxl2=dxu2;

    		if(s2x.dxu0>=0) dxu2=s2x.dxu0;
    		if(s2x.dxl0>=0) dxl2=s2x.dxl0;
        }else if(ds2.startsWith("L")) {
        	if(x2>0) {
            	dxu2=0;
            	dxl2=x2; 
            	
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		maxX=(x1+dxu1)/x2;
            		minX=(x1-dxl1)/x2;
            	}
        	}else {
        		dxu2=0;
        		dxl2=-10*x2;
        		
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=(x1+dxu1)/x2;
            		maxX=(x1-dxl1)/x2;
            	}
        	}	
        	
        	if(x1>0) {
        		ds="G"+ds2.substring(1);
        		limitX=minX;
        	}else {
        		limitX=maxX;
        	}

        }else if(ds2.startsWith("G")) {
        	if(x2>0) {
                dxu2=10*x2;
                dxl2=0;
                
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		maxX=(x1+dxu1)/x2;
            		minX=(x1-dxl1)/x2;
            	}
        	}else {
        		dxu2=-x2;
        		dxl2=0;
        		
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=(x1+dxu1)/x2;
            		maxX=(x1-dxl1)/x2;
            	}
        	}
        	if(x1>0) {
        		ds="L"+ds2.substring(1);
        		limitX=maxX;
        	}else {
        		limitX=minX;
        	}
        }else {
        	dxu2=0.5*Math.abs(x2);
        	dxl2=dxu2;
        }
        
        //result is x1/x2
    	if(x1>=0 && x2>=0) {
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x1<0 && x2<0) {
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x1>0) {
    		//x2<0
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x2>0) {
    		//x1<0
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
    	}else {
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}

        XDX temp =new XDX(x,dxu1,dxl1);
        
        if(limitX!=0)
        	x=limitX;
        
        result=new XDX(x,-1);
        
        result.dsu=ds;
        result.dsl=ds;
        
        result.dxu0=temp.dxu;
        result.dxl0=temp.dxl;
        
        return result;
    }
	
	private XDX multiplyS2XNonNumericalUnc(double x1,double dxu1,double dxl1,XDX s2x) {
        String ds2=s2x.dsu;
        double x2=s2x.x;
        
        //System.out.println("#################XDX 349: "+ds2);
        if(s2x.dxu>=0 && s2x.dxl>=0)
            return null;
        
        if(Str.isNumeric(s2x.dsu))
        	ds2="";
        
        double x=x1*x2;
        
        double minX=0,maxX=0,limitX=0;
        
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
        
        if(dxu1<0)
            dxu1=0.20*Math.abs(x1);
        if(dxl1<0)
            dxl1=0.20*Math.abs(x1);
		        
        //if(Math.abs(s2x.x-6.21)<0.2) 
        //System.out.println("XDX 356: x="+x+" x1="+x1+" dxu1="+dxu1+" dxl1="+dxl1+" x2="+s2x.x+" dxu2="+s2x.dxu+" dxu0="+s2x.dxu0+" ds2="+ds2);
        
        XDX result=new XDX();
        
        double dxu2=0,dxl2=0;
        String ds=ds2;
        if(ds2.isEmpty()) {
        	dxu2=0.2*Math.abs(x2);//assumed 20% unc for empty unc
        	dxl2=dxu2;
        	
    		if(s2x.dxu0>=0) dxu2=s2x.dxu0;
    		if(s2x.dxl0>=0) dxl2=s2x.dxl0;
   
        }else if(ds2.startsWith("L")) {
        	if(x2>0) {
            	dxu2=0;
            	dxl2=x2; 
            	
            	if(dxu1Orig>=0 && dxl1Orig>=0) {
            		minX=(x1-dxl1)*x2;
            		maxX=(x1+dxu1)*x2;
            	}
        	}else {
        		dxu2=0;
        		dxl2=-10*x2;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0) {
            		maxX=(x1-dxl1)*x2;
            		minX=(x1+dxu1)*x2;
            	}
        	}	
        	
        	if(x1<0) {
        		ds="G"+ds2.substring(1);
        		limitX=minX;
        	}else {
        		limitX=maxX;
        	}

        }else if(ds2.startsWith("G")) {
        	if(x2>0) {
                dxu2=10*x2;
                dxl2=0;
                
            	if(dxu1Orig>=0 && dxl1Orig>=0) {
            		minX=(x1-dxl1)*x2;
            		maxX=(x1+dxu1)*x2;
            	}
        	}else {
        		dxu2=-x2;
        		dxl2=0;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0) {
            		maxX=(x1-dxl1)*x2;
            		minX=(x1+dxu1)*x2;
            	}
        	}
        	if(x1<0) {
        		ds="L"+ds2.substring(1);
        		limitX=maxX;
        	}else {
        		limitX=minX;
        	}

        }else {
        	dxu2=0.5*Math.abs(x2);
        	dxl2=dxu2;
        }
        
    	if(x1>=0 && x2>=0) {
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);

    	}else if(x1<0 && x2<0) {
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
    	}else if(x1>0) {
    		//x2<0
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x2>0) {
    		//x1<0
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else {
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
    	}

        XDX temp =new XDX(x,dxu1,dxl1);
        
        if(limitX!=0) //LT or GT values
        	x=limitX;
        
        result=new XDX(x,-1);
        result.dsu=ds;
        result.dsl=ds;
        
        result.dxu0=temp.dxu;
        result.dxl0=temp.dxl;

        return result;
    }
	
    private XDX addS2XNonNumericalUnc(double x1,double dxu1,double dxl1,XDX s2x) {
        String ds2=s2x.dsu;
        double x2=s2x.x;
        double dx2=s2x.dxu;
        
        if(dx2>=0)
            return null;
        
        if(Str.isNumeric(ds2))
        	ds2="";
        
        double x=x1+x2;
        
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
        
        double limitX=0;
        
        boolean isGoodDX1=true;
        if(dxu1<0) {
            dxu1=0.2*Math.abs(x1);
            isGoodDX1=false;
        }
        if(dxl1<0) {
            dxl1=0.2*Math.abs(x1);
            isGoodDX1=false;
        }
        
        double dx1=Math.min(dxu1, dxl1); 

        
        x1=Math.abs(x1);
        x2=Math.abs(x2);
        
        XDX result=new XDX();
        

        if(dx1>Math.abs(x2) || x1>=10*Math.abs(x2) || (!ds2.startsWith("G")&&x1>=2*Math.abs(x2)) ){   
            if(ds2.isEmpty()) {                
            	double dxu2=0.2*x2;//assumed 20% unc for empty unc
            	double dxl2=dxu2;
        		if(s2x.dxu0>=0) dxu2=s2x.dxu0;
        		if(s2x.dxl0>=0) dxl2=s2x.dxl0;
        		
                dxu1=Math.sqrt(dxu1*dxu1+dxu2*dxu2);
                dxl1=Math.sqrt(dxl1*dxl1+dxl2*dxl2);
                
            }else if(ds2.startsWith("L")) {
                //dxu1=dxu1;
                dxl1=Math.sqrt(dxl1*dxl1+x2*x2);
            }else if(ds2.startsWith("G")) {
                dxu1=Math.sqrt(dxu1*dxu1+100*x2*x2);
                //dxl1=dxl1;
            }else {
                dxu1=Math.sqrt(dxu1*dxu1+0.25*x2*x2);//assumed 50% unc for other non-numerical unc
                dxl1=Math.sqrt(dxl1*dxl1+0.25*x2*x2);
            }
            
            result=new XDX(x,dxu1,dxl1);
            if(!isGoodDX1) {
            	result.dxl=-1;
            	result.dxu=-1;
            	result.dsu="";
            	result.dsl="";
            }
        }else {
            if(ds2.startsWith("L")) {
                x=x+dxu1;//upper limit with assumed uncertainty if existing
                
            	if(dxu1Orig>=0) {
            		limitX=x+dxu1Orig;
            	} 
            }else if(ds2.startsWith("G")) {
                x=x-dxl1;//lower limit
                
                if(dxl1Orig>=0) {
                	limitX=x-dxl1Orig;
                }
            }
            if(s2x.s().contains(".")) {
            	dx1=Math.max(dx1, 0.1);
            }else {
            	dx1=Math.max(dx1, 1.0);
            }
            
            XDX temp=new XDX(x,dxl);
            
            if(limitX!=0) {//LT or GT values
            	x=limitX;          	
            }
            
            result=new XDX(x,-1);
            result.dsu=ds2;
            result.dsl=ds2;
            
            //store numerical uncertainties from assumed numerical uncertainties for non-numerical input uncertainties
            result.dxu0=temp.dxu;
            result.dxl0=temp.dxl;
        }
        //System.out.println("## x1="+x1+" dx1="+dx1+"  s2="+s2x.s+" ds2="+ds2+" result.x="+result.x+" dx="+result.dxu+"  "+result.dxl+" result.s="+result.s+" ds="+result.ds+"  hello");
        
        return result;
    }
    
    
    /*
     * empty unc: ds=empty, dxu and dxl=-1
     * Note that: dxu=dxl=-1 for ds=LT,GT,AP, etc, non-numerical uncertainties
     */
	public XDX add(XDX xdx,boolean ignoreEmptyUnc) {
		try {			
			String dsl=xdx.dsl;
			String dsu=xdx.dsu;
			double dxl=xdx.dxl;
			double dxu=xdx.dxu;
			
	    	if(ignoreEmptyUnc && dsl.isEmpty() && dsu.isEmpty() && dxl<0 && dxu<0) {
	    		return add(xdx.x);
	    	}else {
	    		return add(xdx);
	    	}
		}catch(Exception e) {
			
		}
				
		return this.copy();
	}
	
	public XDX add(XDX xdx) {
		XDX result=this.copy();
		result.x+=xdx.x;
		result.s=String.valueOf(result.x);
		
		double dxu1=this.dxu;
		double dxu2=xdx.dxu;
		double dxl1=this.dxl;
		double dxl2=xdx.dxl;
		
		boolean useMinMax=false;
		
		//For large uncertainty (>10%), Tayler expansion method for error propagation 
		//is not valid (e.g., a symmetric uncertainty will become very asymmeric). For such cases, 
		//use max-min approach
		
		if( (this.isLargeUnc||xdx.isLargeUnc) && isUseMaxMinEPByDefault)
			useMinMax=true;
		else if(this.isLimit || this.dsu.startsWith("L") || this.dsu.startsWith("G"))
			useMinMax=true;
		else if(xdx.isLimit || xdx.dsu.startsWith("L") || xdx.dsu.startsWith("G"))
			useMinMax=true;
		
		//if(x>95761 && x<95763)
		//System.out.println("XDX 218: x1="+this.x+"  "+dxu1+" "+dxl1+" x2="+xdx.x+"  "+dxu2+" "+dxl2);
		
		if(dxu1>=0 && dxl1>=0 && dxu2>=0 && dxl2>=0) {
			if(!useMinMax) {
				result.dxu=Math.sqrt(dxu1*dxu1+dxu2*dxu2);
				result.dxl=Math.sqrt(dxl1*dxl1+dxl2*dxl2);

			}else {
				result.dxu=dxu1+dxu2;
				result.dxl=dxl1+dxl2;
			}

			if(result.dxu>=0)
				result.dsu=String.valueOf(result.dxu);
			else
				result.dsu="";
			
			if(result.dxl>=0)
				result.dsl=String.valueOf(result.dxl);
			else
				result.dsl="";
			
		}else {
			result.dxu=-1;
			result.dxl=-1;
			result.dsu="";
			result.dsl="";
			
			//dsu and dsl are the same for non-numerical uncertainty
			String ds1=this.dsu;
			String ds2=xdx.dsu;

			if(dxu1>=0) {
			    XDX temp=addS2XNonNumericalUnc(this.x,dxu1,dxl1,xdx);
			           
			    //System.out.println("XDX 247: this.x="+this.x+" "+dxu1+" "+dxl1+"  xdx="+xdx.x+"  "+xdx.dxu+"  "+xdx.dxl+" dsu="+xdx.dsu);
				result=temp.copy();
			}else if(dxu2>=0) {
                XDX temp=addS2XNonNumericalUnc(xdx.x,xdx.dxu,xdx.dxl,this);
                
                //System.out.println("XDX 254: this.x="+this.x+" "+dxu1+" "+dxl1+"  xdx="+xdx.x+"  "+xdx.dxu+"  "+xdx.dxl+" dsu="+xdx.dsu+" temp dsu="+temp.dsu+" dxu="+temp.dxu);
                result=temp.copy();
			}else if(!ds1.isEmpty() && !ds2.isEmpty()) {
				char c1=ds1.charAt(0);
				char c2=ds1.charAt(0);
				
				String ds=ds1;
		       	if(c1==c2) {
            		if( (c1=='L' || c1=='G') && !ds1.equals(ds2))
            			ds=c1+"T";
            	}else if("LG".contains(c1+"") && "LG".contains(c2+"")){//e.g., LT for T and GT for f, no way to determine the non-numerical uncertainty
            		ds="";
            	}else if("LG".contains(c1+"")){
            		ds=c1+"T";
            	}else if("LG".contains(c2+"")){
            		ds=c2+"T";
            	}else if(c1=='C' || c2=='C') {
            		ds="CA";
            	}else if(c1=='S' || c2=='S') {
            		ds="SY";
            	}
		       	
		       	result.dsu=ds;
		       	result.dsl=ds;
			}else if(!ds1.isEmpty()) {//xdx has empty unc and this has non-numerical unc (not empty)
                XDX temp=addS2XNonNumericalUnc(xdx.x,-1,-1,this);
                result=temp.copy();
			}else if(!ds2.isEmpty()) {//this has empty unc and xdx has non-numerical unc (not empty)
                XDX temp=addS2XNonNumericalUnc(this.x,-1,-1,xdx);
                result=temp.copy();
			}			
		}
		
		return result;
	}
	public XDX add(double x,double dx) {
		return add(new XDX(x,dx));
	}
	public XDX add(double x) {
		return add(x,0);
	}
	
	public XDX subtract(XDX xdx, boolean ignoreEmptyUnc) {
		try {			
			String dsl=xdx.dsl;
			String dsu=xdx.dsu;
			double dxl=xdx.dxl;
			double dxu=xdx.dxu;
			
	    	if(ignoreEmptyUnc && dsl.isEmpty() && dsu.isEmpty() && dxl<0 && dxu<0) {
	    		return subtract(xdx.x);
	    	}else {
	    		return subtract(xdx);
	    	}
		}catch(Exception e) {
			
		}
				
		return this.copy();
	}
	
	public XDX subtract(XDX xdx) {
		
		XDX xdx1=xdx.copy();
		xdx1.x=-xdx.x;
		xdx1.dxu=xdx.dxl;
		xdx1.dxl=xdx.dxu;
		
        if(xdx.dsl.startsWith("L")) { 
            xdx1.dsl="G"+xdx.dsl.substring(1);
            xdx1.dsu=xdx1.dsl;
        }else if(xdx.dsl.startsWith("G")) { 
            xdx1.dsl="L"+xdx.dsl.substring(1);
            xdx1.dsu=xdx1.dsl;
        }else { 
            xdx1.dsl=xdx.dsl;
            xdx1.dsu=xdx1.dsu;
        }

        //System.out.println("XDX 316: xdx1="+xdx.x+"  "+xdx.dxu+"  "+xdx.dxl+" dsu="+xdx.dsu);
        
		return add(xdx1);
	}
	public XDX subtract(double x,double dx) {
		return subtract(new XDX(x,dx));
	}
	public XDX subtract(double x) {
		return subtract(x,0);
	}
	
	
	public String printAsComment(int errorLimit) {
		try {				
			XDX2SDS xs=new XDX2SDS(x,dxl,errorLimit);				
			String tempS=EnsdfUtil.printValueAsComment("",xs.s(),xs.ds());
			return tempS;
		}catch(Exception e) {
			
		}
		
		return "";
	}
	public double[] findProductMinMax(XDX xdx1,XDX xdx2,boolean canCrossZero) {
		double x1=xdx1.x;
		double x2=xdx2.x;
		double dxu1=xdx1.dxu;
		double dxu2=xdx2.dxu;
		double dxl1=xdx1.dxl;
		double dxl2=xdx2.dxl;
		
		double[] out=new double[2];
		Arrays.fill(out, 0);
		
		double min=1E20,max=-1E20;
		if(dxu1>=0 && dxl1>=0 && dxu2>=0 && dxl2>=0) {
			double[] val=new double[4];
			val[0]=x1+dxu1;
			val[1]=x1-dxl1;
			
			val[2]=x2+dxu2;
			val[3]=x2-dxl2;
			
			if(val[0]*val[1]<0 &&!canCrossZero) {
				if(x1>=0) {
					val[1]=0;
				}else {
					val[0]=0;
				}
			}
			
			if(val[2]*val[3]<0 &&!canCrossZero) {
				if(x2>=0) {
					val[3]=0;
				}else {
					val[2]=0;
				}
			}
			//System.out.println("XDX 344: val="+val[0]+"  "+val[1]+"  "+val[2]+" "+val[3]);
			
			for(int i=0;i<2;i++) {
				double va=val[i];
				for(int j=2;j<4;j++) {
					double vb=val[j];
					double v=va*vb;
					if(v>max)
						max=v;
					if(v<min)
						min=v;
					
					//System.out.println("XDX 356: va="+va+" vb="+vb+" v="+v+" max="+max+" min="+min);

				}
			}
			
			out[0]=min;
			out[1]=max;
		}

		
		return out;
	}
	
    public static boolean isCrossZero(XDX xdx) {
		double x=xdx.x;
		double dxu=xdx.dxu;
		double dxl=xdx.dxl;
		
		if(dxu>=0 && dxl>=0) {
			if( (x>=0&&(x-dxl)<0) || (x<=0&&(x+dxu)<0) )
				return true;
		}

		return false;
    }

    
    public XDX multiply(XDX xdx,boolean ignoreEmptyUnc) {
    	return multiply(xdx,ignoreEmptyUnc,false);
    }
    public XDX multiply(XDX xdx) {
    	return multiply(xdx,false,false);
    }
	public XDX multiply(XDX xdx,boolean ignoreEmptyUnc,boolean canCrossZero) {
		XDX result=this.copy();

		double x1=0,x2=0;
		
		x1=this.x;
		x2=xdx.x;
		result.x=x1*x2;
		
		result.s=String.valueOf(result.x);
		
		double dxu1=this.dxu;
		double dxu2=xdx.dxu;
		double dxl1=this.dxl;
		double dxl2=xdx.dxl;
	
		String dsl=xdx.dsl;
		String dsu=xdx.dsu;
		double dxl=xdx.dxl;
		double dxu=xdx.dxu;
		
    	if(ignoreEmptyUnc && dsl.isEmpty() && dsu.isEmpty() && dxl<0 && dxu<0) 
    		return multiply(xdx.x);

		double[] out=findProductMinMax(this,xdx,canCrossZero);
		double lv=out[0];
		double uv=out[1];
		
		//System.out.println("XDX 1013:  x1="+x1+" dxu1="+dxu1+" dxl1="+dxl1+" x2="+x2+" dxu2="+dxu2+" dxl2="+dxl2+" uv="+uv+" lv="+lv);
		
		//dsu and dsl are the same for non-numerical uncertainty
		String ds1=this.dsu;
		String ds2=xdx.dsu;

		boolean useMinMax=false;
		if( (this.isLargeUnc||xdx.isLargeUnc) && isUseMaxMinEPByDefault)
			useMinMax=true;
		else if(this.isLimit || this.dsu.startsWith("L") || this.dsu.startsWith("G"))
			useMinMax=true;
		else if(xdx.isLimit || xdx.dsu.startsWith("L") || xdx.dsu.startsWith("G"))
			useMinMax=true;
		
		
		if(x1<0 && x2<0) {//swap dsu and dsl of x1 and x2
			dxu1=this.dxl;
			dxl1=this.dxu;
			
			dxu2=xdx.dxl;
			dxl2=xdx.dxu;
			
			if(ds1.startsWith("L")) {
				ds1="G"+ds1.substring(1);
			}else if(ds1.startsWith("G")) {
				ds1="L"+ds1.substring(1);
			}
			
			if(ds2.startsWith("L")) {
				ds2="G"+ds2.substring(1);
			}else if(ds1.startsWith("G")) {
				ds2="L"+ds2.substring(1);
			}
		}else if(x1<0) {//x2>=0, swap dsu and dsl of x2
			dxu2=xdx.dxl;
			dxl2=xdx.dxu;
			
			if(ds2.startsWith("L")) {
				ds2="G"+ds2.substring(1);
			}else if(ds1.startsWith("G")) {
				ds2="L"+ds2.substring(1);
			}
		}else if(x2<0){//x1>=0, swap dsu and dsl of x1
			dxu1=this.dxl;
			dxl1=this.dxu;
			
			if(ds1.startsWith("L")) {
				ds1="G"+ds1.substring(1);
			}else if(ds1.startsWith("G")) {
				ds1="L"+ds1.substring(1);
			}
		}
		//else {//x1>=0 and x2>=0
		//	//do nothing
		//}

        if(dxu1>=0 && dxl1>=0 && dxu2>=0 && dxl2>=0) {
            
        	if(!useMinMax) {
    			result.dxu=Math.sqrt(x2*x2*dxu1*dxu1+x1*x1*dxu2*dxu2);
    			result.dxl=Math.sqrt(x2*x2*dxl1*dxl1+x1*x1*dxl2*dxl2);
        	}else {
        		result.dxu=uv-result.x;
        		result.dxl=result.x-lv;
        	}
			
        	//if(Math.abs(result.x-2.5)<0.1)
			//System.out.println("XDX 854: useMinMax="+useMinMax+" x1="+x1+" dxu1="+dxu1+" dxl1="+dxl1+" x2="+x2+" dxu2="+dxu2+" dxl2="+dxl2+" result="+result.x+" dxu="+result.dxu+" dxl="+result.dxl);
		
			if(XDX.isCrossZero(result) && !this.isCrossZero && !xdx.isCrossZero) {
				if(result.x<=0) {
					result.dxu=Math.min(uv,0)-result.x;
				}else {
					result.dxl=result.x-Math.max(lv,0);
				}
			}
						
			result.dsu=String.valueOf(result.dxu);
			result.dsl=String.valueOf(result.dxl);
        }else {//non-numerical uncertainty
			result.dxu=-1;
			result.dxl=-1;
			result.dsu="";
			result.dsl="";

			if(dxu1>=0) {
				XDX temp=multiplyS2XNonNumericalUnc(this.x,dxu1,dxl1,xdx);
				result=temp.copy();
				
				//System.out.println("XDX 874: useMinMax="+useMinMax+" x1="+x1+" dxu1="+dxu1+" dxl1="+dxl1+" x2="+x2+" dxu2="+dxu2+" dxl2="+dxl2+" result="+result.x+" dxu="+result.dxu+" dxl="+result.dxl);
				
				//result.dsu=ds2;
				//result.dsl=ds2;
			}else if(dxu2>=0) {
                XDX temp=multiplyS2XNonNumericalUnc(xdx.x,xdx.dxu,xdx.dxl,this);
                result=temp.copy();

                /*
                if(Math.abs(x-6.21)<0.1) {
				System.out.println("XDX 897: useMinMax="+useMinMax+" this x1="+this.x+" dxu1="+this.dxu+" dxl1="+this.dxl+" dxu0="+this.dxu0+" dxl0="+this.dxl0+"  ds="+this.dsu);
				System.out.println("        xdx ="+xdx.x+" dxu2="+xdx.dxu+" dxl2="+xdx.dxl+" dxu0="+xdx.dxu0+" dxl0="+xdx.dxl0+" dsu="+xdx.dsu);
				System.out.println("    result="+result.x+"  "+result.dxu+" "+result.dxl+" dxu0="+result.dxu0+"  "+result.dxl0);
                }
				*/
                //result.dsu=ds1;
				//result.dsl=ds1;
			}else if(!ds1.isEmpty() && !ds2.isEmpty()) {
				char c1=ds1.charAt(0);
				char c2=ds1.charAt(0);
				
				String ds=ds1;
		       	if(c1==c2) {
            		if( (c1=='L' || c1=='G') && !ds1.equals(ds2))
            			ds=c1+"T";
            	}else if("LG".contains(c1+"") && "LG".contains(c2+"")){//e.g., LT for T and GT for f, no way to determine the non-numerical uncertainty
            		ds="";
            	}else if("LG".contains(c1+"")){
            		ds=c1+"T";
            	}else if("LG".contains(c2+"")){
            		ds=c2+"T";
            	}else if(c1=='C' || c2=='C') {
            		ds="CA";
            	}else if(c1=='S' || c2=='S') {
            		ds="SY";
            	}
		       	
		       	result.dsu=ds;
		       	result.dsl=ds;
			}else if(!ds1.isEmpty()) {		
                XDX temp=multiplyS2XNonNumericalUnc(xdx.x,-1,-1,this);
                result=temp.copy();
				//result.dsu=ds1;
				//result.dsl=ds1;
			}else{		
                XDX temp=multiplyS2XNonNumericalUnc(this.x,-1,-1,xdx);
                result=temp.copy();
				//result.dsu=ds2;
				//result.dsl=ds2;
			}	
        }
		if(result.dxu0<0) result.dxu0=result.dxu;
		if(result.dxl0<0) result.dxl0=result.dxl;

		
		/*
		if(Math.abs(x-6.21)<0.1) {
		   System.out.println("XDX 936:  x1="+x1+" dxu1="+dxu1+" dxl1="+dxl1+" x2="+x2+" dxu2="+dxu2+" dxl2="+dxl2+" dxu0="+dxu0+" dxl0="+dxl0);
	       System.out.println("  this="+this.x+" "+this.dxu+" "+this.dxl+"   this dxu0="+this.dxu0+"  "+this.dxl0);
	       System.out.println("  xdx="+xdx.x+" "+xdx.dxu+" "+xdx.dxl+" dxu0="+xdx.dxu0+"  "+xdx.dxl0);
	       System.out.println("  result="+result.x+"  "+result.dxu+" "+result.dxl+" dxu0="+result.dxu0+"  "+result.dxl0);
		}
		*/
		return result;
	}
	
	public XDX multiply(double x,double dxu,double dxl) {
		return multiply(new XDX(x,dxu,dxl));
	}
	
	public XDX multiply(double x,double dxu,double dxl,boolean canCrossZero) {
		return multiply(new XDX(x,dxu,dxl),false,canCrossZero);//not ignore emptyUnc
	}
	
	public XDX multiply(double x,double dx) {
		return multiply(new XDX(x,dx));
	}
	
	public XDX multiply(double x,double dx,boolean canCrossZero) {
		return multiply(new XDX(x,dx),false,canCrossZero);//not ignore meptyUnc
	}
	public XDX multiply(double x) {
		return multiply(x,0);
	}
	
	public XDX divided(XDX xdx,boolean ignoreEmptyUnc) {
		try {			
			String dsl=xdx.dsl;
			String dsu=xdx.dsu;
			double dxl=xdx.dxl;
			double dxu=xdx.dxu;
			
	    	if(ignoreEmptyUnc && dsl.isEmpty() && dsu.isEmpty() && dxl<0 && dxu<0) {
	    		return divided(xdx.x);
	    	}else {
	    		return divided(xdx);
	    	}
		}catch(Exception e) {
			
		}
				
		return this.copy();
	}
	
	public XDX divided(XDX xdx) {
		XDX result=this.copy();
		XDX xdx2=xdx.copy();

		double x1=0,x2=0;
		
		x1=this.x;
		x2=xdx.x;
		
		if(x2==0)
			return null;
		if(x1==0)
			return result;
		
		//dsu and dsl are the same for non-numerical uncertainty
		String ds2=xdx.dsu;	
		double dxu2=xdx.dxu;
		double dxl2=xdx.dxl;

		xdx2.x=1.0/x2;
		xdx2.s=String.valueOf(xdx2.x);
		
		
		//System.out.println(xdx.dxu);
		
		if(xdx.dxu>=0) {
			double ax=Math.abs(x2);

			//if( (dxu2/ax>0.1||dxl2/ax>0.1) && isUseMaxMinEPByDefault) {
			if(dxu2/ax>0.1||dxl2/ax>0.1) {
				//when the denominator has large uncertainty (>10%), Taylor's expansion method for error propagation 
				//is not valid (e.g., a symmetric uncertainty will become very asymmetric). For such cases, 
				//use max-min approach
				
				
				double max=-1,min=-1;
				if(x2>0 && x2<=dxl2) {//min x2 value overlaps with zero, max 1/x2 is positive infinity
					xdx2.dxu=-1;
					xdx2.dxl=-1;
					xdx2.dsl="GE";
					xdx2.dsu="GE";
					
					min=1.0/(x2+dxu2);
					xdx2.x=min;
				}else if(x2<0 && (x2+dxu2)>=0) {//max x2 value overlaps with zero, min 1/x2 is negative infinity
					xdx2.dxu=-1;
					xdx2.dxl=-1;
					xdx2.dsl="LE";
					xdx2.dsu="LE";
					
					max=1.0/(x2-dxl2);
					xdx2.x=max;
				}else {
					max=1.0/(x2-dxl2);
					min=1.0/(x2+dxu2);
					
					dxu2=max-xdx2.x;
					dxl2=xdx2.x-min;

					xdx2=new XDX(xdx2.x,dxu2,dxl2);
					
					//System.out.println("1  x2="+x2+" dxu2="+dxu2+" dxl2="+dxl2);
				}

			}else {
				//System.out.println("1  x2="+x2+" dxu2="+dxu2+" dxl2="+dxl2);
				
				double tempDX=dxu2;
				dxu2=dxl2/(x2*x2);
				dxl2=tempDX/(x2*x2);
	            x2=xdx2.x;
	            xdx2=new XDX(x2,dxu2,dxl2);
	            
	            //System.out.println("2  x2="+x2+" dxu2="+dxu2+" dxl2="+dxl2);
			}	
		}else if(ds2.startsWith("L")) { 
            xdx2.dsl="G"+ds2.substring(1);
            xdx2.dsu=xdx2.dsl;  

        }else if(ds2.startsWith("G")) { 
            xdx2.dsl="L"+ds2.substring(1);
            xdx2.dsu=xdx2.dsl;           
        }
        
        return multiply(xdx2);	
	}
	
	public XDX divided(double x,double dxu,double dxl) {
		return divided(new XDX(x,dxu,dxl));
	}
	
	public XDX divided(double x,double dx) {
		return divided(new XDX(x,dx));
	}
	public XDX divided(double x) {
		return divided(x,0);
	}
	
	//note that this function changes its own values of a XDX instance
	public void scale(double x) {
		XDX temp=this.multiply(x);
		this.x=temp.x;
		this.dxu=temp.dxu;
		this.dxl=temp.dxl;
		
		this.s=temp.s;
		this.dsu=temp.dsu;
		this.dsl=temp.dsl;
	}
	
	
	///////////////////
	//  static methods
	///////////////////
	public static XDX ZERO() {return new XDX(0.0,0.0);}
	public static XDX ZERO(double dx) {return new XDX(0.0,dx);}
	public static XDX ONE() {return new XDX(1.0,0.0);}
	public static XDX INIT(double x,double dxu,double dxl) {return new XDX(x,dxu,dxl);}
	public static XDX INIT(double x,double dx) {return INIT(x,dx,dx);}
	public static XDX INIT0() {return ZERO();}
	public static XDX INIT1() {return ONE();}
	
	
	//ascending order
	public static XDX[] sort(XDX[] b) {
		XDX[] a=b.clone();
		XDX temp;
		for(int i=0;i<a.length-1;i++) {
			for(int j=i+1;j<a.length;j++) {
				if(a[j].x<a[i].x) {
					temp=a[i];
					a[i]=a[j];
					a[j]=temp;
				}
			}
		}

		return a;
	}
	
	//descending order
	public static XDX[] reverseOrder(XDX[] b) {
		XDX[] a=b.clone();
		XDX temp;
		for(int i=0;i<a.length-1;i++) {
			for(int j=i+1;j<a.length;j++) {
				if(a[j].x>a[i].x) {
					temp=a[i];
					a[i]=a[j];
					a[j]=temp;
				}
			}
		}

		return a;
	}
	
	public static Vector<XDX> sort(Vector<XDX> b){
		XDX[] a=sort((XDX[]) b.toArray());
		return new Vector<XDX>(Arrays.asList(a));
	}
	public static Vector<XDX> reverseOrder(Vector<XDX> b){
		XDX[] a=reverseOrder((XDX[]) b.toArray());
		return new Vector<XDX>(Arrays.asList(a));
	}
	
	public static XDX log(XDX xdx) {
		XDX out=null;
		if(xdx.x<=0)
			return null;
				
		double logx=Math.log(xdx.x);
		
		double xl=xdx.x;
		double xu=xdx.x;
		
		if(xdx.dxl>0)
			xl-=xdx.dxl;
		if(xdx.dxu>0)
			xu+=xdx.dxu;
		
		String ds=xdx.dsu;

		if(!Str.isNumeric(ds)) {
			out=new XDX(logx,0);
			out.dsu=ds;
			out.dsl=ds;
			out.dxu=-1;
			out.dxl=-1;
			return out;
		}
		
		
		if(xl<=0) {
			logx=Math.log(xu);
			ds="LE";
			out=new XDX(logx,0);
			out.dsu=ds;
			out.dsl=ds;
			out.dxu=-1;
			out.dxl=-1;
			return out;
		}
				
		double logXL=Math.log(xl);
		double logXU=Math.log(xu);

		return new XDX(logx,logXU-logx,logx-logXL);
		
	}
	
	public static XDX log10(XDX xdx) {
		XDX out=null;
		if(xdx.x<=0)
			return null;
				
		double logx=Math.log10(xdx.x);
		
		double xl=xdx.x;
		double xu=xdx.x;
		
		if(xdx.dxl>0)
			xl-=xdx.dxl;
		if(xdx.dxu>0)
			xu+=xdx.dxu;
		
		String ds=xdx.dsu;
		
		if(!Str.isNumeric(ds)) {
			out=new XDX(logx,0);
			out.dsu=ds;
			out.dsl=ds;
			out.dxu=-1;
			out.dxl=-1;
			return out;
		}
		
		
		if(xl<=0) {
			logx=Math.log10(xu);
			ds="LE";
			out=new XDX(logx,0);
			out.dsu=ds;
			out.dsl=ds;
			out.dxu=-1;
			out.dxl=-1;
			return out;
		}
				
		double logXL=Math.log10(xl);
		double logXU=Math.log10(xu);

		return new XDX(logx,logXU-logx,logx-logXL);
		
	}
	
	public static XDX exp(XDX xdx) {
		XDX out=null;
				
		double ex=Math.exp(xdx.x);
		
		double xl=xdx.x;
		double xu=xdx.x;
		
		if(xdx.dxl>0)
			xl-=xdx.dxl;
		if(xdx.dxu>0)
			xu+=xdx.dxu;
		
		String ds=xdx.dsu;
		
		if(!ds.isEmpty() && !Str.isNumeric(ds)) {
			out=new XDX(ex,0);
			out.dsu=ds;
			out.dsl=ds;
			return out;
		}
	
				
		double exL=Math.exp(xl);
		double exU=Math.exp(xu);

		return new XDX(ex,exU-ex,ex-exL);
		
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//string functions
    //NOTE that s, dsu and dsl are simply string representations of x, dxu and dxl of xdx, 
    //except for non-numerical uncertainties
    //they are not in ENSDF style as those in XDX2SDS and SDS2XDX
    public static String[] makeSDS(XDX xdx) {
    	return makeSDS(xdx,-1);
    }
    
    public static String[] makeSDS(XDX xdx,int errorLimit) {	
    	return makeSDS(xdx,errorLimit,3);
    }
    
    public static String[] makeSDS(XDX xdx,int errorLimit, int nsig) {		
		
		double x=xdx.x;
		double dxu=xdx.dxu;
		double dxl=xdx.dxl;
		
		xdx.checkSDS();
		
	    //NOTE that s, dsu and dsl are simply string representations of x, dxu and dxl of xdx, 
	    //except for non-numerical uncertainties
	    //they are not in ENSDF style as those in XDX2SDS and SDS2XDX
		//dsu=dsl for non-numerical uncertainty
		//String dsu=xdx.dsu;
		//String dsl=xdx.dsl;
		
		String s=xdx.s,ds=xdx.dsu;
		
		if(Str.isNumeric(ds)) {			
			XDX2SDS xs=null;
			if(errorLimit>0)
				xs=new XDX2SDS(x,dxu,dxl,errorLimit,nsig);
			else
				xs=new XDX2SDS(x,dxu,dxl,XDX2SDS.defaultErrorLimit,nsig);
			
			//convert dsu and dsl to ENSDF style
			s=xs.s();
			ds=xs.ds();
	    	String tempDS=ds.replace("+","").replace("-", "").replace("0", "").trim();
	    	if(tempDS.isEmpty())
	    		ds="";
			
			//System.out.println("x="+xdx.x+" "+xdx.dxu+" "+xdx.dxl+" s="+xdx.s+" dsl="+xdx.dsl+" dsu="+xdx.dsu+" out s="+s+" ds="+ds);

		}else {
			//System.out.println("x="+xdx.x+" "+xdx.dxu+" "+xdx.dxl+" s="+xdx.s+" dsl="+xdx.dsl+" dsu="+xdx.dsu+" out s="+s+" ds="+ds);
			
			XDX2SDS xs=new XDX2SDS(x,0,errorLimit,nsig);
			s=xs.s();
			
			//System.out.println(" s="+s+" ds="+ds+" nsig="+nsig+" errorLimit="+errorLimit);
			//if(Str.isNumeric(ds))//for errorLimit=0
			//	ds="";
			/*
			double dx=Math.min(Math.abs(x*0.1), errorLimit);
			XDX2SDS xs=new XDX2SDS(x,dx,errorLimit);
			int n=(int) (Math.log10(errorLimit)+1);//length of errorLimit integer
			s=xs.s();
			
			System.out.println("1 x="+xdx.x+" "+xdx.dxu+" "+xdx.dxl+" s="+xdx.s+" dsl="+xdx.dsl+" dsu="+xdx.dsu+" out s="+s+" ds="+ds);
			int nsig=Str.findNSig(s);		
			if(nsig<n+1) {
				s=Str.roundByNsig(String.valueOf(x), n+1);
			}			
			System.out.println("2 x="+xdx.x+" "+xdx.dxu+" "+xdx.dxl+" s="+xdx.s+" dsl="+xdx.dsl+" dsu="+xdx.dsu+" out s="+s+" ds="+ds);
		    */
		}

		String[] out=new String[2];
		out[0]=s.trim();
		out[1]=ds.trim();
		
		return out;
    }
    
    public static String printXDX(XDX xdx) {return printXDX(xdx,12);}   
    public static String printXDX(XDX xdx,String alignment) {return printXDX(xdx,12,alignment);}   
    public static String printXDX(XDX xdx,int len) {return printXDX(xdx,len,"RIGHT");}    
    public static String printXDX(XDX xdx,int len,String alignment) {
    	if(xdx==null) 
    		return "";
    	
    	String[] out=makeSDS(xdx);
    	String s=out[0];
    	String ds=out[1];
    	
    	return printSDS(s,ds,len,alignment);
    	//return printXDX(xdx.x,xdx.dxu,xdx.dxl,len,alignment);   	
    } 
    
    public static String printXDX(XDX xdx,int sLen,int dsLen) {return printXDX(xdx,sLen,dsLen,"RIGHT");}
    public static String printXDX(XDX xdx,int sLen,int dsLen,String alignment) {
    	if(xdx==null) 
    		return "";
    	
    	//System.out.println("1 xdx x="+xdx.x+" "+xdx.dxu+"  "+xdx.dxl+" "+xdx.dsu+" "+xdx.dsl);
    	
    	String[] out=makeSDS(xdx);
    	String s=out[0];
    	String ds=out[1];
    	
    	//System.out.println("2 xdx x="+xdx.x+" "+xdx.dxu+"  "+xdx.dxl+" "+xdx.dsu+" "+xdx.dsl+" out s="+s+"  "+ds);
    	
    	return printSDS(s,ds,sLen,dsLen,alignment);   	
    	//return printXDX(xdx.x,xdx.dxu,xdx.dxl,sLen,dsLen,alignment);   	
    }
  
    
    public static String printXDX(double x,double dx,String alignment) {return printXDX(x,dx,12,alignment);}
    public static String printXDX(double x,double dx,int len) {return printXDX(x,dx,dx,len);}
    public static String printXDX(double x,double dx,int len,String alignment) {return printXDX(x,dx,dx,len,alignment);}
    public static String printXDX(double x,double dxu,double dxl,int len) {return printXDX(x,dxu,dxl,len,"RIGHT");}
    public static String printXDX(double x,double dxu,double dxl,int len,String alignment) {return printXDX(x,dxu,dxl,len,alignment,-1);}
    public static String printXDX(double x,double dxu,double dxl,int len,String alignment,int errorLimit) {
    	XDX2SDS x2s=null;
    	if(errorLimit>0)
    		x2s=new XDX2SDS(x,dxu,dxl,errorLimit);
    	else
    		x2s=new XDX2SDS(x,dxu,dxl);
    	
    	return printSDS(x2s.s(),x2s.ds(),len,alignment);
    }
    
    public static String printXDX(double x,double dx,int sLen,int dsLen) {return printXDX(x,dx,sLen,dsLen,"RIGHT");}
    public static String printXDX(double x,double dx,int sLen,int dsLen,String alignment) {return printXDX(x,dx,dx,sLen,dsLen,alignment);}
    public static String printXDX(double x,double dxu,double dxl,int sLen,int dsLen,String alignment) {return printXDX(x,dxu,dxl,sLen,dsLen,alignment,-1);}
    public static String printXDX(double x,double dxu,double dxl,int sLen,int dsLen,String alignment,int errorLimit) {
    	XDX2SDS x2s=null;
    	if(errorLimit>0)
    		x2s=new XDX2SDS(x,dxu,dxl,errorLimit);
    	else
    		x2s=new XDX2SDS(x,dxu,dxl);
    	
    	int n=x2s.ds().length();
    	if(n>0 && n<=dsLen && (dxu>0||dxl>0) )
    		return printSDS(x2s.s(),x2s.ds(),sLen,dsLen,alignment);
    	
    	if(dxu>0 && dxu>=dxl)
    		return printSDS(x2s.s(),x2s.dsu(),sLen,dsLen,alignment);
    	else if(dxl>0)
    		return printSDS(x2s.s(),x2s.dsl(),sLen,dsLen,alignment);
    	else
    		return printSDS(x2s.s(),"",sLen,dsLen,alignment);
    }
    
    public static String printSDS(String s,int len) {return printSDS(s,"",len,0,"RIGHT");}
    public static String printSDS(String s,int len,String alignment) {return printSDS(s,"",len,0,alignment);}
    public static String printSDS(String s,String ds) {return printSDS(s,ds,12);}
    public static String printSDS(String s,String ds,int len) {return printSDS(s,ds,len,"RIGHT");}
    public static String printSDS(String s,String ds,int len,String alignment) {
    	  
        if(len<s.length()+2)
            len=s.length()+2;
        
        int sLen=len-2;
        int dsLen=2;
        
    	return printSDS(s,ds,sLen,dsLen,alignment);
    }
    
    public static String printSDS(String s,String ds,int sLen,int dsLen) {return printSDS(s,ds,sLen,dsLen,"RIGHT");}
    public static String printSDS(String s,String ds,int sLen,int dsLen,String alignment) {
  	  
    	String out="";
    	String formatStr="";
    	if(sLen<0)
    		sLen=0;
    	if(dsLen<0)
    		dsLen=0;
    	
    	if(Str.isNumeric(s)&&Float.parseFloat(s)==0&&s.startsWith("-"))
    		s=s.substring(1);
    	
    	if(Str.isNumeric(ds)&&Float.parseFloat(ds)==0)
    		ds="";
    	else {
        	String tempDS=ds.replace("+","").replace("-", "").replace("0", "").trim();
        	if(tempDS.isEmpty())
        		ds="";
    	}
    	
    	alignment=alignment.toUpperCase().trim();
    	if(alignment.startsWith("L")) {
    		if(s.trim().length()>=sLen)
    			formatStr="%-"+sLen+"s %-"+dsLen+"s";
    		else
    			formatStr="%-"+sLen+"s%-"+dsLen+"s";
    	}else {
    		formatStr="%"+sLen+"s %"+dsLen+"s";
    	}
    	
    	formatStr=formatStr.replace("%-0","%").replace("%0", "%");
    	out=String.format(formatStr,s,ds);  
    	
    	return out;
    }	
    
    //the following use default sLen=10, dsLen=2, right-aligned
    public static String printXDXDefault(XDX xdx) {return printXDXDefault(xdx,13);} 
    public static String printXDXDefault(XDX xdx,int len) {
    	if(xdx==null) 
    		return "";
    	
    	String[] out=makeSDS(xdx);
    	String s=out[0];
    	String ds=out[1];
    	
    	return printSDSDefault(s,ds,len);  	
    	//return printXDXDefault(xdx.x,xdx.dxu,xdx.dxl,len);
    }  
    
    public static String printXDXDefault(double x,double dx) {return printXDXDefault(x,dx,13);}
    public static String printXDXDefault(double x,double dx,int len) {return printXDXDefault(x,dx,dx,len);}
    public static String printXDXDefault(double x,double dxu,double dxl,int len) {return printXDXDefault(x, dxu, dxl, len, -1);}
    public static String printXDXDefault(double x,double dxu,double dxl,int len, int errorLimit) {
    	XDX2SDS x2s=null;
    	if(errorLimit>0)
    		x2s=new XDX2SDS(x,dxu,dxl,errorLimit);
    	else
    		x2s=new XDX2SDS(x,dxu,dxl);
    	return printSDSDefault(x2s.s(),x2s.ds(),len);
    }
    
    public static String printSDSDefault(String s,String ds) {	  
    	return printSDSDefault(s,ds,13);
    }
    
    public static String printSDSDefault(String s,String ds,int len) { 	  
    	return printSDSDefault(s,ds,len,false);
    }
    
    public static String printSDSDefault(String s,String ds,int len,boolean useBracket) {
    	  
    	String out="";
    	if(Str.isNumeric(s)&&Float.parseFloat(s)==0&&s.startsWith("-"))
    		s=s.substring(1);
    	if(Str.isNumeric(ds)&&Float.parseFloat(ds)==0) {
    		//ds="";
    	}else {
        	//String tempDS=ds.replace("+","").replace("-", "").replace("0", "").trim();
        	String tempDS=ds.replace("+","").replace("-", "").trim();
        	if(tempDS.isEmpty())
        		ds="";
    	}
    	
    	if(useBracket && ds.length()>0 && s.length()>0) {
    		out=s+"("+ds+")";
    	}else {
        	out=String.format("%10s %2s",s,ds); 
    	}
    	
    	String temp=Str.ltrim(out);
    	if(temp.length()<=len)
    		out=String.format("%"+len+"s",temp);
    	
    	return out;
    }
}
