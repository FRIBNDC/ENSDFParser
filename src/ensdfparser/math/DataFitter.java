package ensdfparser.math;

import org.freehep.math.minuit.FCNBase;


public class DataFitter implements FCNBase{

	private double[] x,y,error;//experimental data
	private UserFunction ufunc;
	
	public DataFitter(double[] x,double[] y,double[] error){
		this.x=x;
		this.y=y;
		this.error=error;	
		ufunc=new UserFunction();
	}
		
	@Override
	/*
	 * return the value of function to be minimized.
	 * For fitting, return the chi-square value
	 * @param par parameters of fit function
	 */
	public double valueOf(double[] par) {
		return getChi2(par);
	}

	public double getChi2(double[] par){
		double chi2=0.;
		double error0=0.;

		ufunc.setParameters(par);
		
		for(int i=0;i<x.length;i++){
			error0=error[i];
			
			//if error not given, take 10%
			if(error0<=0.0E-18) {
				if(y[i]!=0)
					error0=y[i]*0.001;
				else
					continue;
			}
			
			chi2+=(ufunc.valueAt(x[i])-y[i])*(ufunc.valueAt(x[i])-y[i])/(error0*error0);
		}
		
		//System.out.println(chi2);
		
		return chi2;
	}

	public void setUserFunction(UserFunction ufunc){this.ufunc=ufunc;}
	public UserFunction getUserFunction(){return ufunc;}
}
