package ensdfparser.calc;

public class ResultOfUncertainties {

	public XDX normalError;//result with uncertainty from normal error propagation
	public XDX extremeError;//result with uncertainty from min and max of each variable
	public XDX MCError;//result with uncertainty from Monte-Carlo method (central)
	public XDX MCError_median;//median value
	
	public boolean isValid=true;//false, if no solution
	
	public boolean isLimit=false;
	public boolean isNonNumericUnc0=false;//for original unc
	
	public boolean canUseNormalError=false, useNormalError=false,useMCError=true;
	
	public ResultOfUncertainties(){
		normalError=new XDX(0,0);
		extremeError=new XDX(0,0);
		MCError=new XDX(0,0);
		MCError_median=new XDX(0,0);
		canUseNormalError=false;	
		useMCError=true;
		useNormalError=false;
		isLimit=false;
		isValid=true;
		
		isNonNumericUnc0=false;
	}
}
