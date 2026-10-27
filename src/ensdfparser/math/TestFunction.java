package ensdfparser.math;

public abstract class TestFunction{
	private double Xmin=-1E6;
	private double Xmax=1E6;
	private double EPSILON = 1E-6; //integration precision
	
	//factor to bring a very small or very large number to the magnitude of 1-10
	//used in percentile() and integrate()
	private double factor=1.0;
	
	private double[] par;

	
	public TestFunction(double[] par){
		this.par=par.clone();
	}
	public TestFunction() {
	}

	public TestFunction(double xmin,double xmax) {

        setRange(xmin,xmax);
		int n=(int)Math.log10(Xmax-Xmin);
		factor=Math.pow(10,n);
	}
	
	public void setRange(double xmin,double xmax) {
		Xmax=xmax;
		Xmin=xmin;
	}
	
	public double valueAt(double x){
		return valueAt(x,this.par);
	}

	private double v(double x) {
		double y=valueAt(x);
		if(Double.isInfinite(y) || Double.isNaN(y) || y>1E12 || y<1E-12)
			return 0;
		
		return y;
	}
	
	/* must be overwritten in sub-class */
	public abstract double valueAt(double x,double[] par);
	
	public void setParameters(double[] par){this.par=par.clone();}


    public double integrate0(double a, double b) {
    	
        double h = b - a;
        double c = (a + b) / 2.0;
        double d = (a + c) / 2.0;
        double e = (b + c) / 2.0;

        //System.out.println(" a="+a+" f(a)="+pdf(a)+" c="+c+" f(c)="+pdf(c)+"  b="+b+" f(b)="+pdf(b));

        double Q1 = h/6  * (v(a) + 4*v(c) + v(b));
        double Q2 = h/12 * (v(a) + 4*v(d) + 2*v(c) + 4*v(e) + v(b));
     
        return (Q1+Q2)/2;
    }

    /**********************************************************************
     * Integrate f from a to b using Simpson's rule.
     * Increase N for more precision.
     **********************************************************************/
     public double Simpson(double a, double b) {
        int N = 10000;                    // precision parameter
        double h = (b - a) / (N - 1);     // step size
   
        // 1/3 terms
        double sum = 1.0 / 3.0 * (v(a) + v(b));

        // 4/3 terms
        for (int i = 1; i < N - 1; i += 2) {
           double x = a + h * i;
           sum += 4.0 / 3.0 * v(x);
        }

        // 2/3 terms
        for (int i = 2; i < N - 1; i += 2) {
           double x = a + h * i;
           sum += 2.0 / 3.0 * v(x);
        }

        return sum * h;
     }
     
	/**********************************************************************
	 * Integrate f from a to b using adaptive quadrature
	 * Increase N for more precision.
	 * NOTE that: the integrate could fail if the range (a,b) is too broad,
	 * that is a long zero tail (f(x)~0) at either side
	 **********************************************************************/	
    public double integrate(double a, double b) {

    	
    	//if(Double.isNaN(a) || Double.isNaN(b))
    	//	return 0;
    		
    	if(a>=Xmax || b<=Xmin || a>=b)
    		return 0;
    	if(a<Xmin)
    		a=Xmin;
    	if(b>Xmax)
    		b=Xmax;
    	
    	//System.out.println(" a="+a+"  b="+b);
    	
        double h = b - a;
        double c = (a + b) / 2.0;
        double d = (a + c) / 2.0;
        double e = (b + c) / 2.0;

        //System.out.println(" a="+a+" f(a)="+pdf(a)+" c="+c+" f(c)="+pdf(c)+"  b="+b+" f(b)="+pdf(b)+" Simpson(a,b)="+Simpson(a,b));

        double Q1 = h/6  * (v(a) + 4*v(c) + v(b));
        double Q2 = h/12 * (v(a) + 4*v(d) + 2*v(c) + 4*v(e) + v(b));
        

        //if(count==0 || Math.abs(count-673)<3) 
        //System.out.println(" count="+count+"  a="+a+" f(a)="+pdf(a)+" c="+c+" f(c)="+pdf(c)+"  b="+b+" f(b)="+pdf(b)+" h="+h+"  Q1="+Q1+"  Q2="+Q2+" "+(Math.abs(Q2 - Q1) <= EPSILON));
        //count++;
        
         
        if (Math.abs(Q2 - Q1)/factor <= EPSILON){
        	//the adaptive quadrature seems not to work for a large range with every long zero tails,
        	//for which it could return zero. So double check it with the integration of Simpson rule
        	if(Q1<=EPSILON*factor || Q2<=EPSILON*factor)
        		return Simpson(a,b);
        	
            return Q2 + (Q2 - Q1) / 15;
        }else
            return integrate(a, c) + integrate(c, b);
    }
    
    public double EPSILON() {return EPSILON;}
}

