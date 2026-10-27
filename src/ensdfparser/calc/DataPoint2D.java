package ensdfparser.calc;

public class DataPoint2D {
	public double x=0;
	public double y=0;
	public String label="";
	
	public DataPoint2D(double x0,double y0,String label0){
		x=x0;
		y=y0;
		label=label0;
	}
	public DataPoint2D(double x0,double y0){
		x=x0;
		y=y0;
		label="";
	}
}
