package ensdfparser.math;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MinosError;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnMinos;
import org.freehep.math.minuit.MnUserParameters;


public class Fit {
	private DataFitter fcn;
	private FunctionMinimum min;
	private MnUserParameters upar;
	private MnMinos minos;
	private int numberOfData=0,npar=0;
	
	public Fit(double[] x,double[] y,double[] Dy){
		fcn = new DataFitter(x,y,Dy);	
		/*
        upar = new MnUserParameters();
        for(int i=0;i<10;i++){
        	upar.add("a"+i, 1,0.1);
        }
        */
        numberOfData=x.length;
                
	}

	private void suppressErrMsg(boolean sup) {
		if(sup) {
	        //suppress error message from minimize()
	        System.setErr(new PrintStream(new OutputStream() {
	            public void write(int b) {
	            }
	        }));
		}else {
	        //restore error message from minimize()    		
	        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
		}
	}
	public void doFit(UserFunction f,double[] pars0) throws Exception{
        
		upar=new MnUserParameters();
        for(int i=0;i<pars0.length;i++) {
        	upar.add("a"+i, pars0[i],Math.abs(pars0[i]*0.01));
        	//upar.add("a"+i, 1,0.1);
        }
        
        npar=pars0.length;
        
        doFit(f);
	}
	public void doFit(UserFunction f) throws Exception{
        suppressErrMsg(true);
        
		fcn.setUserFunction(f);
       
        MnMigrad migrad = new MnMigrad(fcn, upar);

        migrad.setUseAnalyticalDerivatives(false);

        min = migrad.minimize();

        upar = min.userParameters();

        minos = new MnMinos(fcn, min);

        suppressErrMsg(false);
 	}

	public double[] solve(double[] x,double[] y){
		double[][] M; //matrix for solving the parameters 
		double[][] UM;//unit matrix
		double[] par=null;
		Matrix mat=null;
		UserFunction ufunc;
		ufunc=fcn.getUserFunction();
		//double par[];
		
		M=new double[x.length][x.length];
		UM=new double[x.length][x.length];
		
		for(int i=0;i<x.length;i++)
			for(int j=0;j<x.length;j++){
				if(i==j)
					UM[i][j]=1;
				else
					UM[i][j]=0;
			}
		
		for(int i=0;i<x.length;i++)
			for(int j=0;j<x.length;j++){
				M[i][j]=ufunc.valueAt(x[i],UM[j]);
		}
		
        mat=new Matrix(M,y);
        try{
        	//mat.print();
        	par=mat.solve();
        	//mat.print();
        }
        catch(Exception e){
        	JOptionPane.showMessageDialog(null, "Error with solving matrix! "+e.getMessage());
        }
		return par;
		
	}	
	
	
	public MnUserParameters getUserParameters() {return upar;}
	public double getUserParameter(int index) {return upar.value(index);}	
	public FunctionMinimum getMinimum() {return min;}
	public double getMinimumValue() {return min.fval();}
	public int getNumberOfData() {return numberOfData;}
	public double getParErrorLower(int index) {return ((MinosError)minos.minos(index)).lower();}
	public double getParErrorUpper(int index){return ((MinosError)minos.minos(index)).upper();}
	public UserFunction getUserFunction() {return fcn.getUserFunction();}
 
	
	public double[] getParametersArray(){
		double[] par=new double[npar];
		for(int i=0;i<npar;i++){
			par[i]=upar.value(i);
		}
		
		return par;
    }
	

	public double[] getParErrorArray(){
		double[] par=new double[npar];
		for(int i=0;i<npar;i++){
			//par[i]=(Math.abs(getParErrorLower(i))+Math.abs(getParErrorUpper(i)))/2.0;
			par[i]=min.userParameters().error(i);
		}
		
		return par;
    }
	
	
	public double getChi2(){
		return fcn.getChi2(this.getParametersArray());
	}
    public void setUserParameters(String[] name,double[] par){
        upar=new MnUserParameters();
    	for(int i=0;i<par.length;i++)
    		upar.add(name[i], par[i]);
    }
    
    public int getNpar() {return npar;}
    
    public void setUserParameters(MnUserParameters upar,int npar){this.upar=upar;this.npar=npar;}
    public void setUserFunction(UserFunction ufunc) {
    	fcn.setUserFunction(ufunc);	
    }
}
