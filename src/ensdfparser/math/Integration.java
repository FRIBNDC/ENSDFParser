package ensdfparser.math;

public class Integration {

    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Numerically computes the integral of the given function using midpoint rules
     *
     * @param f the function to integrate
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @param N the number of points in the integral to take, must be &ge; 2.
     * @return an approximation of the integral 
     */
    static public double midpoint(Function1D f,double a,double b, int N) {
        /*
         *    b               /    N                        \
         *   /                |  =====                      |
         *  |           b - a |  \      /           (b - a)\|
         *  | f(x) dx = ----- |   >    f|a +(k-1/2) -------||
         *  |             N   |  /      \              N   /|
         * /                  |  =====                      |
         *  a                 \  k = 1                      /
         */
        if(a == b)
            return 0;
        else if(a > b)
            throw new RuntimeException("Integral upper limit (" + b+") must be larger than the lower-limit (" + a + ")");
        else if(N < 1)
            throw new RuntimeException("At least two integration parts must be used, not " + N);
        
        double sum =0;
        for(int k = 1; k < N; k++)
            sum += f.v(a+k*(b-a)/N);

        sum+= (f.v(a)+f.v(b))/2;

        return (b-a)/N*sum;
    }
    
    /**
     * Numerically computes the integral of the given function using trapezoidal rules
     *
     * @param f the function to integrate
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @param N the number of points in the integral to take, must be &ge; 2.
     * @return an approximation of the integral 
     */
    static public double trapz(Function1D f, double a, double b, int N)
    {
        if(a == b)
            return 0;
        else if(a > b)
            throw new RuntimeException("Integral upper limit (" + b+") must be larger than the lower-limit (" + a + ")");
        else if(N < 1)
            throw new RuntimeException("At least two integration parts must be used, not " + N);
        /*
         *    b               /              N - 1                 \
         *   /                |              =====                 |
         *  |           b - a |f(a) + f(b)   \      /    k (b - a)\|
         *  | f(x) dx = ----- |----------- +  >    f|a + ---------||
         *  |             N   |     2        /      \        N    /|
         * /                  |              =====                 |
         *  a                 \              k = 1                 /
         */
        double sum =0;
        for(int k = 1; k < N; k++)
            sum += f.v(a+k*(b-a)/N);

        sum+= (f.v(a)+f.v(b))/2;

        return (b-a)/N*sum;
    } 
    
    //////////////////////////////////////////////////////////////////////////////////////    
    // Adaptive Simpson method for numerically computing an integral
    
    /**
     * Numerically computes the integral of the given function
     *
     * @param f the function to integrate
     * @param tol the precision for the desired result
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @return an approximation of the integral 
     */
    static public double Simpson(Function1D f, double tol, double a, double b)
    {
        return Simpson(f, tol, a, b, 100);
    }
    
    /**
     * Numerically computes the integral of the given function
     *
     * @param f the function to integrate
     * @param tol the precision for the desired result
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @param maxDepth the maximum recursion depth
     * @return an approximation of the integral
     */
    static public double Simpson(Function1D f, double tol, double a, double b, int maxDepth)
    {
        if(a == b)
            return 0;
        else if(a > b)
            throw new RuntimeException("Integral upper limit (" + b+") must be larger than the lower-limit (" + a + ")");

        double h = b-a;
        double c = (a+b)/2;
        
        double f_a = f.v(a);
        double f_b = f.v(b);
        double f_c = f.v(c);

        double one_simpson = h * (f_a + 4 * f_c + f_b) / 6;
        double d = (a + c) / 2;
        double e = (c + b) / 2;

        double two_simpson = h * (f_a + 4 * f.v(d) + 2 * f_c + 4 * f.v(e) + f_b) / 12;
        
        if(maxDepth <= 0)
            return two_simpson;
        
        if(Math.abs(one_simpson-two_simpson) < 15*tol)
            return two_simpson + (two_simpson - one_simpson)/15;
        else
        {
            double left_simpson  = Simpson(f, tol/2, a, c, maxDepth-1);
            double right_simpson = Simpson(f, tol/2, c, b, maxDepth-1);
            return left_simpson + right_simpson;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////// 
    /**
     * Numerically computes the integral of the given function
     *
     * @param f the function to integrate
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @return an approximation of the integral
     */
    public static double romb(Function1D f, double a, double b,double precision)
    {
        return romb(f, a, b, precision,20);
    }

    /**
     * Numerically computes the integral of the given function
     *
     * @param f the function to integrate
     * @param a the lower limit of the integral
     * @param b the upper limit of the integral
     * @param max the maximum number of extrapolation steps to perform. 
     * @param precision
     */
    public static double romb(Function1D f, double a, double b, double precision,int max)
    {
        // see http://en.wikipedia.org/wiki/Romberg's_method
        if(a>=b)
            return 0;
        
        max+=1;
        double[] currentRow = new double[max];//hold values of the current row; index is for column; first index will not be used
        double var = 0;//var is used to hold the value R(n-1,m-1) at column=m-1, from the previous row (n-1) so that 2 arrays are not needed
        double lastVal = Double.NEGATIVE_INFINITY;
        
        TrapezoidalIntegration trapz=new TrapezoidalIntegration(f,a,b);
        
        for(int i = 1; i < max; i++)
        {
            for(int j = 1; j <= i; j++)
            {
                if(j == 1)
                {
                    var = currentRow[j];
                    //currentRow[j] = trapz(f, a, b, (int)Math.pow(2, i-1));
                    currentRow[j]=trapz.next();
                }
                else
                {
                    double cj=Math.pow(4, j-1);
                    currentRow[i]= (cj*currentRow[j-1]-var )/(cj - 1);
                    var = currentRow[j];
                    currentRow[j]= currentRow[i];//for R(i,j)
                }
            }


            //System.out.println(" i="+i+"  lastVal="+lastVal+"  currentRow="+currentRow[i]+" precision="+precision);
            
            if( lastVal>0 && Math.abs(lastVal - currentRow[i]) < precision*lastVal )//there is only approximately 15.955 accurate decimal digits in a double, this is as close as we will get
                return currentRow[i];
            else 
                lastVal = currentRow[i];
        }

        
        System.out.println("Too many steps in integration");
        
        return currentRow[max-1];
    }  
    
    public static double rombOpen(Function1D f, double a, double b,double precision)
    {
        return rombOpen(f, a, b, precision,20);
    }
    
    /*
     * for open function (that is, function value can't be evaluated at boundary a and b)
     */
    public static double rombOpen(Function1D f, double a, double b, double precision,int max) {
        if(a>=b)
            return 0;
        
        max+=1;
        double[] currentRow = new double[max];//hold values of the current row; index is for column; first index will not be used
        double var = 0;//var is used to hold the value R(n-1,m-1) at column=m-1, from the previous row (n-1) so that 2 arrays are not needed
        double lastVal = Double.NEGATIVE_INFINITY;
        
        TrapezoidalIntegration midpoint=new TrapezoidalIntegration(f,a,b);
        
        for(int i = 1; i < max; i++)
        {
            for(int j = 1; j <= i; j++)
            {
                if(j == 1)
                {
                    var = currentRow[j];
                    //currentRow[j] = midpoint(f, a, b, (int)Math.pow(2, i-1));
                    currentRow[j] =midpoint.next();
                }
                else
                {
                    double cj=Math.pow(4, j-1);
                    currentRow[i]= (cj*currentRow[j-1]-var )/(cj - 1);
                    var = currentRow[j];
                    currentRow[j]= currentRow[i];//for R(i,j)
                }
            }


            if( Math.abs(lastVal - currentRow[i]) < precision*lastVal )//there is only approximately 15.955 accurate decimal digits in a double, this is as close as we will get
                return currentRow[i];
            else 
                lastVal = currentRow[i];
        }

        System.out.println("Too many steps in integration rombOpen");
        
        return currentRow[max-1];        
    }
    
    /*
     * return a Function1D of x for f(x,y) at y=yc
     */
    public static Function1D fx(Function2D f,double yc) {return (x)->f.v(x,yc);}
    public static Function1D fy(Function2D f,double xc) {return (y)->f.v(xc,y);}
    
    /*
     * return a Function1D of x for integration of f(x,y) over y=(ya,yb) at x
     */
    public static Function1D Fx(Function2D f,double ya,double yb,int maxSteps) {return (x)->romb(fy(f,x),ya,yb,maxSteps);}
    public static Function1D Fy(Function2D f,double xa,double xb,int maxSteps) {return (y)->romb(fx(f,y),xa,xb,maxSteps);}    
    
    public static Function1D Fx(Function2D f,double ya,double yb) {return Fx(f,ya,yb,20);}
    public static Function1D Fy(Function2D f,double xa,double xb) {return Fy(f,xa,xb,20);}  
    
    public static double romb2D(Function2D f, double xa, double xb, double ya,double yb, int maxSteps) {
    	return romb(Fx(f,ya,yb,maxSteps),xa,xb,maxSteps);
    }
    
    public static double romb2D(Function2D f, double xa, double xb, double ya,double yb) {
    	return romb2D(f,xa,xb,ya,yb,20);
    }
}

class TrapezoidalIntegration extends AbstractIntegration{
    
    TrapezoidalIntegration(Function1D func,double a,double b){
        super(func,a,b);
    }
    double next() {
        if(!isValid)
            return 0;
        
        nSteps++;
        if (nSteps ==1) {
            result=0.5*(xb-xa)*(f.v(xa)+f.v(xb));
            nIntervals=1;
            return result;
             
        } 

        double d=(xb-xa)/nIntervals;
        double sum=0.0;
        double x=xa+0.5*d;
        
        for (int i=0;i<nIntervals;i++) {
            sum+=f.v(x);
            x+=d;
        }
  
        result+=(xb-xa)*sum/nIntervals;
        result=result/2.0;
        
        nIntervals=nIntervals*2;
        
        return result;
    }
    int nSteps() {return nSteps;}
}

class MidpointIntegraiton extends AbstractIntegration{

    MidpointIntegraiton(Function1D func, double a, double b) {
        super(func, a, b);
    }
    
    //Returns the nth stage of refinement of the extended midpoint rule. On the first call (nSteps=1),
    //the routine returns the crudest estimate of the integration between a and b. Subsequent
    //calls set nSteps=2,3,... and improve the accuracy by adding (2/3)*3^(nSteps-1) additional interior points.
    double next() {
        if(!isValid)
            return 0;
        
        nSteps++;
        if (nSteps ==1) {
            result=(xb-xa)*f.v(0.5*(xa+xb));
            nIntervals=1;
            return result;
             
        } 
     
        //The added points alternate in spacing between d and 2*d
        double d=(xb-xa)/(3.0*nIntervals);
        double sum=0.0;
        double x=xa+0.5*d;
        
        for (int i=0;i<nIntervals;i++) {
            sum+=f.v(x);
            x+=2*d;
            sum+=f.v(x);
            x+=d;
        }
  
        result+=(xb-xa)*sum/nIntervals;
        result=result/3.0;
        
        nIntervals=nIntervals*3;
        
        return result;
    }
    
}
abstract class AbstractIntegration{
    double result=0;
    int nSteps=0;
    int nIntervals=1;
    double xa=-1,xb=-1;
    Function1D f=null; 
    
    boolean isValid=true;
    AbstractIntegration(Function1D func,double a,double b){
        f=func;
        xa=a;
        xb=b;
        nSteps=0;
        nIntervals=1;
        
        if(a == b)
            isValid=false;
        else if(a > b) {
            isValid=false;
            throw new RuntimeException("Integral upper limit (" + b+") must be larger than the lower-limit (" + a + ")");
        }

    }
    
    int nSteps() {return nSteps;}
    
    abstract double next();
}