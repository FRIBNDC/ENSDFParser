package ensdfparser.math;

import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.math3.complex.Complex;

import ensdfparser.calc.DataPoint2D;
import ensdfparser.calc.XDX;
import ensdfparser.ensdf.XDX2SDS;
import ensdfparser.nds.util.Str;

public class Util {

	//GAMMA functions converted from those Fortran functions in nndclib.f
	
	public static double gamma(double x) {
		Complex out=gamma(new Complex(x,0));	
		return out.getReal();
	}
	
	public static Complex gamma(double re,double im) {
		return gamma(new Complex(re,im));
	}
	
	public static Complex gamma(Complex z) {
		Complex out=null,tempZ=null;
		double zR=z.getReal();
		double zI=z.getImaginary();
	
		if(zR<0) {
			int n=(int) (1.0-zR);
			double fn=n;
			tempZ=z.add(fn);

			if(zI>=0) {
				out=GAMZ(tempZ);
			}else {
				out=GAMZ(tempZ.conjugate());
				out=out.conjugate();
			}
			
			for(int i=1;i<=n;i++) {
				double fi=i-1;
				tempZ=z.add(fi);
				out=out.divide(tempZ);
			}
		}else if(zI>=0) {
			out=GAMZ(z);
		}else {
			out=GAMZ(z.conjugate());
			out=out.conjugate();
		}
		
		return out;
	}
	
	/*
	 * Gamma function for real and imaginary part>=0
	 */
	private static Complex GAMZ(Complex z) {
		Complex out=null,tempZ=null,z1=null,z2=null,zt=null;
		double zR=z.getReal();
		double zI=z.getImaginary();
		final double c0=2.50662827463;
		
		if(zI<=1.0) {
			zt=z;
			out=GAM1(zt);
		}else {
			int m=(int) zI;
			double fn=m+1;
			tempZ=z.divide(fn);
			
			zt=tempZ;
			z1=GAM1(zt);
			double fn1=1.0/fn;
			for(int i=1;i<=m;i++) {
				tempZ=tempZ.add(fn1);
				zt=tempZ;
				z2=GAM1(zt);
				z1=z1.multiply(z2);
			}
			
		    double s=Math.pow(fn,zR-0.5)/Math.pow(c0,m);
		    double t=zI*Math.log(fn);
		    Complex z3=new Complex(Math.cos(t),Math.sin(t));
		    z3=z3.multiply(s);
		    
		    out=z3.multiply(z1);
		}
		
		return out;
	}
	
	/*
	 * Gamma function for real>=0, and imaginary part: [0,1]
	 */
	private static Complex GAM1(Complex z) {
		Complex out=null;
		double zR=z.getReal();
		
		if(zR<=1.0) {
			out=GAM2(z);
		}else {
			int n=(int)zR;
			double fn=n;
			z=z.subtract(fn);
			if(z.getReal()==0) {
				n=n-1;
				z=z.add(1.0);
			}
			out=GAM2(z);
			for(int i=1;i<=n;i++) {
				double fi=i-1;
				out=z.add(fi).multiply(out);
			}
		}
		return out;
	}
	
	/*
	 * Gamma function for real>=0, and imaginary part: [0,1]
	 */
	private static Complex GAM2(Complex z) {
		Complex out=null,p=null,q=null;

		double[] a= {
				+0.0000000000E+0, +1.0000000000E+0,
				+1.2536302998E+0, +6.2294126401E-2,
				-1.9367439704E-1, +9.5294089001E-3,
				+1.0021677762E-2, -1.7669280217E-3,
				+7.9027635693E-5};            
		double[] b= {	
				+1.0000000000E+0, +6.7641463495E-1,
				+3.2773507466E-1, +1.0279994528E-1,
				+2.7018504538E-2, +5.1647208257E-3,
				+8.7521995448E-4, +9.5129148083E-5,
				+9.9862892410E-6};
		
		p=new Complex(a[8]);
		q=new Complex(b[8]);
		for(int i=7;i>=0;i--) {
			p=p.multiply(z).add(a[i]);
			q=q.multiply(z).add(b[i]);
		}
		
		out=q.divide(p);

		return out;
	}
	
	/*
	 * Confluent Hypergeometric function
	 */
	public static Complex hyperG(Complex A,Complex B,Complex z) {
		Complex out=null, APN=null,BPN=null,FN=null,t=null,test=null;
		APN=A;
		BPN=B;
		FN=Complex.ZERO;
		t=Complex.ONE;
		out=Complex.ONE;
		
		final double PERC=1.0E-6;
		
		for(int i=1;i<=30;i++) {
			FN=FN.add(1.0);
			t=t.multiply(APN).multiply(z).divide(FN).divide(BPN);
			APN=APN.add(1.0);
			BPN=BPN.add(1.0);
			test=t.divide(out);
			out=out.add(t);
			
			if(test.abs()<PERC)
				return out;
		}
		
		out=Complex.ZERO;
		
		return out;
	}
	   
	public static long fact(long n) {		     
		if (n <= 1)		       
			return 1;		     
		else		         
			return n * fact(n - 1);
	  
	}	
	public static long doublefact(long n) {		     
	    if (n == 0 || n==1) 
	        return 1; 
	      return n*doublefact(n-2); 
	  
	}
	
    /*
    Given vector of data points of (xi,yi), and given a value x, return   
    a value y and an error estimate dy from interpolation of the data ponints.                     
    [Numerical Recipes (The Art of Scientific Computing.  W.H. Press, 
    et al.  Cambridge University Press (NY, 1986), p.82]  	
    
    use all given points
    */
	static public double[] polynomialInterpolation(double x, Vector<DataPoint2D> pointsV) {
		double[] v=new double[2];
		Arrays.fill(v,-1);
		int iBest=-1;
		double minDiff=100000,diff; 
		int n=pointsV.size();
		
		double y=-1,dy=100000;
		
		double[] rowC=new double[n+1];
		double[] rowD=new double[n+1];
		
		double[] xi=new double[n+1];
		double[] yi=new double[n+1];

        
		//dummy point
		xi[0]=0;
		yi[0]=0;
		
		for(int i=0;i<n;i++) {
			DataPoint2D p=pointsV.get(i);		
			xi[i+1]=p.x;
			yi[i+1]=p.y;

			
			//System.out.println(" i="+(i+1)+" x="+p.x+" y="+p.y);
		}
		
		for(int i=1;i<=n;i++) {
			diff=Math.abs(xi[i]-x);
			if(diff<minDiff) {
				iBest=i;
				minDiff=diff;
			}
			rowC[i]=yi[i];
			rowD[i]=yi[i];
		}
		
		y=yi[iBest];
		iBest-=1;
		
		//At each level (column) nc, the rowCs and rowDs are the corrections that make the interpolation one
		//order higher. The final answer y is equal to the sum of any yi plus a set of rowCs
		//and/or rowD�s that form a path through the family tree to the rightmost daughter.
		
		double dx1,dx2,slope;
		for(int nc=1;nc<n;nc++) {//column index
			for(int nr=1;nr<=n-nc;nr++) {//row index
				dx1=xi[nr]-x;
				dx2=xi[nr+nc]-x;
				
				//This error can occur only if two input are (to within roundoff) identical
				if(dx1==dx2) {
					dy=1000*y;
					v[0]=y;
					v[1]=dy;
					
					//System.out.println(" dx1="+dx1+" dx2="+dx2+" y="+y+" dy="+dy);
					
					return v;
				}
								
				slope=(rowC[nr+1]-rowD[nr])/(dx1-dx2);

				//update rowC and rowD
				rowD[nr]=dx2*slope;
				rowC[nr]=dx1*slope;
			}
			
			//After each column in the tableau is completed, we decide
			//which correction, c or d, we want to add to our accumulating
			//value of y, i.e., which path to take through
			//the tableau forking up or down. We do this in such a
			//way as to take the most straight line  route through the
			//tableau to its apex, updating ns accordingly to keep track
			//of where we are. This route keeps the partial approximations
			//centered (insofar as possible) on the target x. The 
			//last dy added is thus the error indication.
			
			if(2*iBest<(n-nc))
				dy=rowC[iBest+1];
			else{
				dy=rowD[iBest];
				iBest-=1;
			}
			
			y+=dy;
		}
		
		
		v[0]=y;
		v[1]=dy;
		return v;
	}	
	
    /*
    Given vector of data points of (xi,yi), and given a value x, first locate 
    position of x in data points which are in ascending order, then choose n=nStepMax 
    data points around that position to be used for interpolation of value y and an error estimate dy from interpolation of the data points.                     
    [Numerical Recipes (The Art of Scientific Computing.  W.H. Press, 
    et al.  Cambridge University Press (NY, 1986), p.82]    
    
    use all given points
    */
    public static double[] polynomialInterpolation(double x,Vector<DataPoint2D> dataPointsV,int nStepMax) {
        double[] out=new double[2];
        out[0]=-1;
        out[1]=-1;

        
        //int nStepMax=6;
        int nStep=0;
        try {
            
            int nPoints=dataPointsV.size();
            
            int index=findIndexToInsert(x,dataPointsV);//index>=0
            if(index>0 && Math.abs(x-dataPointsV.get(index-1).x)<1E-6) {
                out[0]=dataPointsV.get(index-1).y;
                return out;
            }
            
            if(index<nPoints) {
                 if(Math.abs(x-dataPointsV.get(index).x)<1E-6) {
                        out[0]=dataPointsV.get(index).y;
                        return out;
                 }
                        
                 if(index>0) {
                     //W is within the energy range of existing calculatedFermiV, then extrapolate Fermi function at W using existing calculated ones 
                     nStep=Math.min(nPoints, nStepMax);
                     if(index>nPoints-nStep)
                         index=nPoints-nStep;              
                     else
                         index=index-nStep/2;
                     
                     if(index>=1) {
                         nStep=Math.min(nPoints-1-index,nStepMax);
                         Vector<DataPoint2D> tempV=new Vector<DataPoint2D>();
                         for(int i=index;i<index+nStep;i++) {
                             tempV.add(dataPointsV.get(i));
                             //System.out.println(" i="+i+" x="+dataPointsV.get(i).x+"  y="+dataPointsV.get(i).y);
                         }


                         return polynomialInterpolation(x, tempV);

                       }
                 }
            }

        }catch(Exception e) {
            
        }

        
        return out;
    }

    /*
     * find the index of the first data point that has energy greater than e, so
     * that the point with this energy can be inserted at the found index.
     * Data points must be in increasing order of energy.
     * If e is greater than or equal to the last point, return the size of data points;
     * if e is less than the first data point, return the index of the first data point.
     * In other case, index-1 is the last data point that has energy less than e
     */
    public static int findIndexToInsert(double e,Vector<DataPoint2D> pointsV) {
        int index=pointsV.size();
        if(index==0)
            return 0;
        
        try {
            
            double em=0,ei=0,ef=0;
            int start=0;
            int end=pointsV.size()-1;
            int middle=0;
            
            while(true){
                
                boolean to_break=false;
                
                middle=(start+end)/2;           

                em=pointsV.get(middle).x;
                ei=pointsV.get(start).x;
                ef=pointsV.get(end).x;

                
                if(middle==start)//in this case, start+1=end; eg, start=1,end=2, then middle=
                   to_break=true;

                if(e>=ef){
                   start=end+1;
                   to_break=true;
                }else if(e<ei){
                   end=start;
                   to_break=true;
                }else if(e>em) {
                   start=middle+1;
                }else if(e<em) {
                   end=middle;
                }else{//e=em
                   start=middle+1;
                   to_break=true;
                }
                
                if(to_break)
                   break;
                else
                   continue;             
            }

            index=start;
    
        }catch(Exception e0) {
            
        }
        
        return index;
    }    
    
    public static Vector<DataPoint2D> makeDataPoints(double[] xs,double ys[]){
        Vector<DataPoint2D> pointsV=new Vector<DataPoint2D>();
        for(int i=0;i<xs.length;i++) {
            double x=xs[i];
            double y=0;
            if(i<ys.length)
                y=ys[i];
            
            pointsV.add(new DataPoint2D(x,y));
        }
        return pointsV;
    }
	public static double radius(int A) {
		double v=Math.pow(A, 1.0/3);
		return 0.002908*v-0.002437/v;
	}	

    public static float getValue(float[] vals,int i) {
        try {
            return vals[i];
        }catch(Exception e) {
            
        }
        return -1; 
    }
    public static double getValue(double[] vals,int i) {
        try {
            return vals[i];
        }catch(Exception e) {
            
        }
        return -1; 
    }
    
    public static XDX getXDX(XDX[] vals,int i) {
        try {
            return vals[i];
        }catch(Exception e) {
            
        }
        return null; 
    }	
    
    public static String printXDX(XDX xdx) {
    	return printXDX(xdx,13);
    }
    public static String printXDX(XDX xdx,int len) {
    	return printXDX(xdx,len,99);
    }
    
    public static String printXDX(XDX xdx,int len,int errorLimit) {
        if(!Str.isNumeric(xdx.dsl)) {
            XDX2SDS x2s=new XDX2SDS(xdx.x,0,errorLimit);
            return printSDS(x2s.s(),xdx.dsl);
        }
                    
    	return printXDX(xdx.x,xdx.dxu,xdx.dxl,len,errorLimit);
    }    
    
    public static String printXDX(double x,double dx) {
    	return printXDX(x,dx,13);
    }
    public static String printXDX(double x,double dx,int len) {
    	return printXDX(x,dx,len,99);
    }
    public static String printXDX(double x,double dx,int len,int errorLimit) {
    	return printXDX(x,dx,dx,len,errorLimit);
    }
    public static String printXDX(double x,double dxu,double dxl,int len,int errorLimit) {
    	  
    	String out="";
    	XDX2SDS s2x=new XDX2SDS(x,dxu,dxl,errorLimit);
    	
    	String ds=s2x.ds();
    	if(!s2x.dsl().equals(s2x.dsu()))
    	    ds="+"+s2x.dsu()+"-"+s2x.dsl();
    	
    	out=String.format("%10s %-2s",s2x.s(),ds);  	
    	
    	String temp=Str.ltrim(out);
    	if(temp.length()<=len)
    		out=String.format("%"+len+"s",temp);
    	
    	return out;
    }
    
    public static String printXMaxDX(XDX xdx) {
        if(xdx.dxu>xdx.dxl && xdx.dxu>0)
            return printXDX(xdx.x,xdx.dxu);
        else if(xdx.dxl>0)
            return printXDX(xdx.x,xdx.dxl);
        else
            return printXDX(xdx);
    }
    
    public static String printXMinDX(XDX xdx) {
        if(xdx.dxu>0 && xdx.dxu<xdx.dxl)
            return printXDX(xdx.x,xdx.dxu);
        else if(xdx.dxl>0)
            return printXDX(xdx.x,xdx.dxl);
        else
            return printXDX(xdx);
    }
    
    public static String printSDS(String s,String ds) {	  
    	return printSDS(s,ds,13);
    }
    
    public static String printSDS(String s,String ds,int len) {
  	  
    	String out="";
    	out=String.format("%10s %-2s",s,ds);  
    	
    	String temp=Str.ltrim(out);
    	if(temp.length()<=len)
    		out=String.format("%"+len+"s",temp);
    	
    	return out;
    }
}
