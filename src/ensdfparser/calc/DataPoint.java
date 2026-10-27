package ensdfparser.calc;


public class DataPoint{
	private double x=-1,dx=-1,dxp,dxm;
	private String label="",label2="",ref="";
	private String s="",ds="",unit="";

	public DataPoint(){}
	public DataPoint(double x,double dx){
		setX(x,dx);
		setS(String.valueOf(x),String.valueOf(dx));
	}
	public DataPoint(double x,double dxp,double dxm){
		setX(x,dxp,dxm);
		String ds="";
		if(dxp!=dxm)
			ds="+"+String.valueOf(dxp)+"-"+String.valueOf(dxm);
		else
			ds=String.valueOf(dxp);
			setS(String.valueOf(x),ds);
	}
	
	public DataPoint(double x,double dx,String label){
		this(x,dx);
		this.label=label;
	}
	
	public DataPoint(double x,double dxp,double dxm,String label){
		this(x,dxp,dxm);
		this.label=label;
	}
	
	public DataPoint(double x,double dxp,double dxm,String label,String label2){
		this(x,dxp,dxm);
		this.label=label;
		this.label2=label2;
	}
	
	public double x(){return x;}
	public double dx(){return dx;}
	public double dxp(){return dxp;}
	public double dxm(){return dxm;}
	
	public String s(){return s;}
	public String ds(){return ds;}
	public String unit(){return unit;}
	public String ref() {return ref;}
	public String label(){return label;}
	public String label2() {return label2;}
	
	public void setS(String es,String des){
		this.s=es;
		this.ds=des;
	}
	public void setS(String es,String des,String unit){
		this.s=es;
		this.ds=des;
		this.unit=unit;
	}
	
	public void setUnit(String unit){this.unit=unit;}
	
	public void setX(double x,double dx){
		this.x=x;
		this.dx=dx;
		this.dxp=dx;
		this.dxm=dx;
	}
	
	public void setX(double x,double dxp,double dxm){
		this.x=x;
		this.dx=dxp;
		this.dxm=dxm;
		this.dxp=dxp;
	}
	
	public void setLabel(String s){label=s;}
	public void setRef(String s){ref=s;}
	
	public void set2ndLabel(String s) {label2=s;}
	
	public DataPoint copy() {
		DataPoint dp=new DataPoint(x,dxp,dxm,label,label2);
		dp.dx=dx;
		dp.setS(s, ds,unit);
		dp.ref=ref;
		
		return dp;
	}
}
