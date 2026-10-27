package ensdfparser.math;

public class UserFunction{
	private double[] par;

	
	public UserFunction(double[] par){
		this.par=par.clone();
	}

	public UserFunction() {
	}

	public double valueAt(double x){
		return valueAt(x,this.par);
	}

	
	public double valueAt(double x,double[] par){

		double result=0;
		//user-define function goes here
		result= par[0]
		       +par[1]*x
		       +par[2]*x*x
		       +par[3]*x*x*x;

		/*
		double K=.5;
		result= par[0]
			       +par[1]*(x*(x+1)-K*K)
			       +par[2]*(x*(x+1)-K*K)*(x*(x+1)-K*K)
			       +par[3]*Math.pow(-1,x+0.5)*(x+0.5);
		           //+par[3]*Math.pow(-1, x+3./2.)*(x-1./2.)*(x+1./2.)*(x+3./2.);
		//System.out.println("hello");
		 
		*/
		
		return result;

	}
	
	public void setParameters(double[] par){this.par=par.clone();}


}
