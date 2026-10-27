package ensdfparser.calc;
import java.util.Random;

/*
 * 
 */
public class AnyDistribution{

	public static interface Function{public double v(double params);}
	
	Function f=null;//pdf
	private double Xmin=-1E6;
	private double Xmax=1E6;
	private double EPSILON = 1E-6; //integration precision
	private double integral=0;
	private double interval=1.0;//for quickSample
	
	//factor to bring a very small or very large number to the magnitude of 1-10
	//used in percentile() and integrate()
	private double factor=1.0;
	
	int count=0;
	
	//min,max have to be set properly
	//the brange should be set too broad, otherwise the intergrate function
	//could fail if there are long zero tails.
	public AnyDistribution(Function f,double min,double max) throws IllegalArgumentException {
		init(f,min,max);
	}
	
	private void init(Function f,double min,double max) throws IllegalArgumentException{
		//System.out.println("  min="+min+"  max="+max);
		
		if(min>=max)
			throw new IllegalArgumentException();
		
		this.f=f;
		setXmin(min);
		setXmax(max);		

		factor=1.0;
		int n=(int)Math.log10(max-min);
		factor=Math.pow(10,n);
				
		integral=integrate(Xmin,Xmax);

		//System.out.println("  ***EPSILON="+EPSILON+"  factor="+factor+" Xmin="+Xmin+"  pdf(a)="+pdf(Xmin)+" Xmax="+Xmax+"  pdf(b)="+pdf(Xmax)+"  integral="+integral);

	}
	
	public double pdf(double x) {
		try {
			double y=f.v(x);
			if(Double.isInfinite(y) || Double.isNaN(y) || y>1E12 || y<1E-12)
				return 0;
			
			return f.v(x);	
		}catch(Exception e) {
		}		
		return 0;
	}
	
	public double cdf(double x) {return cdf(Xmin,x);}
	public double cdf(double x1,double x2) {return integrate(x1,x2);}
	
	public double pdfN(double x) {return normalizedPDF(x);}
	public double cdfN(double x) {return normalizedCDF(x);}
	public double cdfN(double x1,double x2) {return normalizedCDF(x1,x2);}
	
	public double normalizedPDF(double x){
		try{
			return pdf(x)/integral;
		}catch(Exception e){
			return 0;
		}
	}
	public double normalizedCDF(double x){
		return normalizedCDF(Xmin,x);
	}
	
	public double normalizedCDF(double x1,double x2){
		try{
			return cdf(x1,x2)/integral;
		}catch(Exception e){
			return 0;
		}
	}
	
	//fail occasionally, need to be improved
	//solve equation using Newton's method
	//Newton's method is only guaranteed to converge if certain conditions are satisfied. 
	//It can fail under following conditions:
	//1. Bad starting points---Iteration point is stationary (derivative=0)
	//This can happen, for example, if the function whose root is sought approaches zero 
	//asymptotically as x goes to -infinity or +infinity In such cases a different method, such as 
	//bisection, should be used to obtain a better estimate for the zero to use as an initial point.
	public double percentile_old(double p) {
		if(p>=1)
			return Xmax;
		if(p<=0 || integral<=0)
			return Xmin;
	
		
		//System.out.println("  integral="+integral+"  p="+p+" EPSILON="+EPSILON+"  Math.abs(cdf(x0)-p)="+(Math.abs(cdf(0)-p*integral))+" factor="+factor+"  f.v(0)="+f.v((Xmin+Xmax)/2.0));

		p=p*integral;
		
		double x0=(Xmin+Xmax)/2.0;
		double range=(Xmax-Xmin);
		double diff=Math.abs(cdf(x0)-p);
		double temp=0;
		int ntries=0;

		
		try {
			while(diff/factor>EPSILON) {				
				while(pdf(x0)<=0){
					x0+=(cdf(x0)>p)?-(x0/2+range/100):(x0/2+range/100);
					
					//System.out.println(" 1 x0="+x0+"  cdf(x0)="+cdf(x0)+" p="+p+"  f.v(x0)="+f.v(x0)+"  diff="+diff+" ntries="+ntries+" integral="+integral);
					
					if(Double.isInfinite(x0)) {
						System.out.println("can't get percentile for p="+(p/integral)+" xmin="+Xmin+"  xmax="+Xmax);
						return Xmin;
					}
				}
				
				//System.out.println("### 1.1 x0="+x0+"  cdf(x0)="+cdf(x0)+" p="+p+"  f.v(x0)="+f.v(x0)+"  diff="+diff+" ntries="+ntries+" integral="+integral);
				
				temp=x0-(cdf(x0)-p)/pdf(x0);
				
				if(temp<=Xmin || temp>=Xmax)
					x0+=(cdf(x0)>p)?-(x0/2+range/100):(x0/2+range/100);
				else
					x0=temp;
				
				diff=Math.abs(cdf(x0)-p);
				ntries++;
				
				//System.out.println(" 2 x0="+x0+"  cdf(x0)="+cdf(x0)+" p="+p+"  f.v(x0)="+f.v(x0)+"  diff="+diff+" ntries="+ntries+" temp="+temp);
				
				if(ntries>10000) {
					System.out.println("still can't get percentile for p="+(p/integral)+"  diff="+(diff/factor)+" xmin="+Xmin+"  xmax="+Xmax);
					return x0;
				}
				
				//System.out.println("  x0="+x0+"  cdf(x0)="+cdf(x0)+" p="+p+"  f.v(x0)="+f.v(x0)+"  diff="+diff+" ntries="+ntries);

			}	
			
			//System.out.println(" ntries="+ntries+"  x0="+x0);
			
			return x0;
		}catch(Exception e) {
		}

		return Xmin;
	}
	
	/*
	 * solve equation using bi-section method
	 * VERY SLOW!!!
	 */
	public double percentile(double p0) {
		if(p0>=1)
			return Xmax;
		if(p0<=0 || integral<=0)
			return Xmin;
	
		
		//System.out.println("  integral="+integral+"  p="+p0+" EPSILON="+EPSILON+"  Math.abs(cdf(x0)-p)="+(Math.abs(cdf(0)-p0*integral))+" factor="+factor+"  f.v(0)="+f.v((Xmin+Xmax)/2.0));
	
		int ntries=0;
		double left=Xmin,right=Xmax;
		double m=(Xmin+Xmax)/2.0;
		
		double p=p0*integral;
		double diff=Math.abs(cdf(m)-p);
		
	    //String mark="";
		try {
			while(diff/factor>EPSILON && (right-left)/factor>EPSILON) {	
				
				//if(ntries<2) System.out.println("####  m="+m+"   left="+left+" right="+right+"  (right-left)/factor="+(right-left)/factor+" xmin="+Xmin+" cdf(m)="+cdf(m)+"  cdf(0.005)="+cdf(0.0051));
				
				if(cdf(m)<p){
					left=m;
					//mark="right half";
				}else{
					right=m;
					//mark="left half";
				}
				
				m=(left+right)/2.0;
				
				diff=Math.abs(cdf(m)-p);	
				
				//if(ntries<200) System.out.println("   @"+mark+"   left="+left+" right="+right+"  (right-left)/factor="+(right-left)/factor+"  cdf(left)="+cdf(left)+"  cdf(right)="+cdf(right));
				//if(ntries<20) System.out.println("   @   left="+left+" right="+right+"  (right-left)/factor="+(right-left)/factor+"  cdf(left)="+cdf(left)+"  cdf(right)="+cdf(right));
				//if(ntries<20) System.out.println("   m="+m+"  cdf(x0)="+cdf(m)+" p="+p+"  f.v(x0)="+f.v(m)+"  diff="+(diff/factor)+" ntries="+ntries+" integral="+integral+" p0="+p0);
				
				ntries++;
			
				
				if(ntries>500) {
					System.out.println("still can't get percentile for p="+(p/integral)+"  diff="+(diff/factor)+" xmin="+Xmin+"  xmax="+Xmax+"  ntries="+ntries);
					return m;
				}
				
				//System.out.println("  x0="+x0+"  cdf(x0)="+cdf(x0)+" p="+p+"  f.v(x0)="+f.v(x0)+"  diff="+diff+" ntries="+ntries);

			}	
			
			//System.out.println(" ntries="+ntries+"  x0="+x0);
			
			return m;
		}catch(Exception e) {
		}

		return Xmin;
	}
	
	/*
	 * The sampling using percentile(r.nextDouble()) is very slow when sample size is large
	 */
	public double[] slowSample(int size) {
		double[] out=new double[size];
		for(int i=0;i<size;i++)
			out[i]=next();
		
		return out;
	}

	/*
	 * The sampling using percentile(r.nextDouble()) is very slow
	 */
	public double next() {
		Random r=new Random();
		return percentile(r.nextDouble());
	}
	
	
	/* note that the length of the returned array could not 
	 * be exactly equal to size due to rounding of counts 
	 * of each bin
	 */
	public double[] quickSample(int size) {
    	Random r=new Random();
    	
        int NBINSMAX=1000;
        
        /*
        double xm=this.percentile(0.16);
        double xp=this.percentile(0.84);
        double sigma=(xp-xm)/2;       
        int nsigma=49;
        interval=sigma/100;
        
        //System.out.println("1  start="+Xmin+"  end="+Xmax+" sigma="+sigma+" xm="+xm+" xp="+xp+"  integral="+integral+"  gap="+interval+"  cdf="+cdfN(Xmin)+"  "+cdfN(0.00427)+"  "+cdfN(Xmax));   
        double xstart=Xmin,xend=Xmax;
        if(xstart<xm-nsigma*sigma)
        	xstart=xm-nsigma*sigma;
        if(xend>xp+nsigma*sigma)
        	xend=xp+nsigma*sigma;
        	
        	        
        int nbins=(int)((xend-xstart)/interval);
        
        if(nbins>NBINSMAX){
        	nbins=NBINSMAX;
        	interval=(xend-xstart)/nbins;
        }
        */
        
        double xstart=Xmin;
        double xend=Xmax;

        int nbins=NBINSMAX;
        interval=(xend-xstart)/NBINSMAX;

               
        //System.out.println("2  start="+xstart+"  end="+xend+"  nbins="+nbins+" interval="+interval);
        
        double ysum=0;
    	
        double[] x= new double[nbins+1];
        double[] y= new double[nbins+1];
        
        for (int k=0;k<=nbins;k++){
            x[k]=xstart+k*interval;
            y[k]=pdf(x[k]);
            ysum+=y[k];
        }
        
        
        int count=0;
        int NP=size;
        double[] xs=new double[NP+1];
        for(int i=0;i<=nbins;i++){
        	int nxi=(int) (y[i]/ysum*NP);
        	for(int j=0;j<nxi;j++){
        		//xs[count]=r.nextGaussian()*interval/4+x[i];
        		xs[count]=r.nextDouble()*interval+x[i];
        		count++;
        	}       		
        }
        
        double[] newXS;        
        if(count<=NP){
        	newXS=new double[count];
        	for(int i=0;i<count;i++)
        		newXS[i]=xs[i];
        	
        	xs=newXS;
        }
        
    	int n=xs.length;
    	
    	newXS=new double[n];
    	for(int i=0;i<n;i++) {
    		int index=(int) (n*r.nextDouble());
    		newXS[i]=xs[index];
    	}
    	
        return newXS;
	}
	
    public double integrate0(double a, double b) {
    	
        double h = b - a;
        double c = (a + b) / 2.0;
        double d = (a + c) / 2.0;
        double e = (b + c) / 2.0;

        //System.out.println(" a="+a+" f(a)="+pdf(a)+" c="+c+" f(c)="+pdf(c)+"  b="+b+" f(b)="+pdf(b));

        double Q1 = h/6  * (pdf(a) + 4*pdf(c) + pdf(b));
        double Q2 = h/12 * (pdf(a) + 4*pdf(d) + 2*pdf(c) + 4*pdf(e) + pdf(b));
     
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
        double sum = 1.0 / 3.0 * (pdf(a) + pdf(b));

        // 4/3 terms
        for (int i = 1; i < N - 1; i += 2) {
           double x = a + h * i;
           sum += 4.0 / 3.0 * pdf(x);
        }

        // 2/3 terms
        for (int i = 2; i < N - 1; i += 2) {
           double x = a + h * i;
           sum += 2.0 / 3.0 * pdf(x);
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

        double Q1 = h/6  * (pdf(a) + 4*pdf(c) + pdf(b));
        double Q2 = h/12 * (pdf(a) + 4*pdf(d) + 2*pdf(c) + 4*pdf(e) + pdf(b));
        

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

    public void setXmin(double x) {Xmin=x;}
    public void setXmax(double x) {Xmax=x;}
    public void setEPSILON(double e) {EPSILON=e;}
    
    public double Xmin() {return Xmin;}
    public double Xmax() {return Xmax;}
    public double EPSILON() {return EPSILON;}
    
    public double interval(){return interval;}
    public double integral(){return integral;}
    
    public Function getFunction(){return f;}
    
}
