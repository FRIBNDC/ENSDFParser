package ensdfparser.calc;

import java.util.Vector;

/*
 * simple calculation of branching ratios simply from given intensities,
 * without involvements of conversion coefficients, mixing ratios, 
 * multipolarities, etc., for gammas
 */

public class SimpleBranchingCalculator {
	
	private Vector<Branch> branches=new Vector<Branch>();

	/*
	 *    ds          dri
	 *    empty       20%*ri
	 *    AP,SY,CA    50%*ri
	 *    LT or LE    50%*ri, ri changed to ri*50
	 *    GT or GE    if ri/sum(ri)<10%, set dri=0
	 *                if ri/sum(ri)>10%, set dri=ri*100

	
	//factors for non-numerical uncertainty string in uncertaintyParser
	private float factorForEmpty=0.2f;
	private float factorForAP=0.5f;
	private float factorForCA=0.5f;
	private float factorForSY=0.5f;
	private float factorForLT=0.5f;
	private float factorForGT=10000;
	 */
	
	//dx/x limit
	//if dx/x<limit, use regular error propagation for variable of Gaussian distribution
	//otherwise if dx/x of any vaiable exceeds this limit, calculate max, median, min and then
	//deduced (max-median) and (median-min) as upper and lower uncertainties, respectively.
	private float GaussianLimit=0.1f;
	
	private float sum=0;
	
    ///////////////////////////////////////////////
	//inner class Branch:
	class Branch{
		
		String s="",ds="";
		
		float ri0=-1,dri0=-1;//store original value of s,ds
		float ri=-1,driu=-1,dril=-1;
		
		float br=-1,dbru=-1,dbrl=-1;//% branching, add up to 100
		float rb=-1,drbu=-1,drbl=-1;//relative branching, strongest=100
		
		public Branch(String ris,String dris){
			initializeBranch(ris,dris);
		}
		
		public Branch(String ris,String drius,String drils){
			initializeBranch(ris,drius,drils);
		}
		
		public void clear(){
			ri0=-1;dri0=-1;
			ri=-1;driu=-1;dril=-1;
			
			s="";ds="";
			br=-1;dbru=-1;dbrl=-1;
			rb=-1;drbu=-1;drbl=-1;
		}
		
		/*
		 * drius: string of upper uncertainty
		 * drils: string of lower uncertainty
		 * NOTE: uncertainty string is for real values
		 */
		public void initializeBranch(String ris,String drius,String drils){
			clear();
			
			s=ris.trim().toUpperCase();
			ds=drius.trim().toUpperCase();
			
			UncertaintyParser.parse(ris,drius,drils);
			XDX originalXDX=UncertaintyParser.getOriginalXDX();
			XDX parsedXDX=UncertaintyParser.getParsedXDX();
			
			ri0=(float) originalXDX.x();
			dri0=(float) originalXDX.dxu();
			

			ri=(float)parsedXDX.x();
			driu=(float)parsedXDX.dxu();
			dril=(float)parsedXDX.dxl();			
		    		
		    
		    //initialized absolute and relative branchings
			br=ri;dbru=driu;dbrl=dril;
			rb=ri;drbu=driu;drbl=dril;
		}
		   
		public void initializeBranch(String ris,String dris){
			initializeBranch(ris,dris,dris);
		}
	}
	//end inner class Branch:
    ///////////////////////////////////////////////
	
	
	//functions for BranchingCalculator class
	
	public SimpleBranchingCalculator(){
		reset();
	}
	
	public SimpleBranchingCalculator(Vector<String> valueStrs){
	
	}
	
	
	public void addBranch(String s,String ds){
		branches.add(new Branch(s,ds));
	}

	public void addBranch(String s,String dus,String dls){
		branches.add(new Branch(s,dus,dls));
	}
	
	public void addBranch(float ri,float dri){
		addBranch(Float.toString(ri),Float.toString(dri));
	}

	public void addBranch(float ri,float driu,float dril){
		addBranch(Float.toString(ri),Float.toString(driu),Float.toString(dril));
	}
	
	public void addBranch(double ri,double dri){
		addBranch(Double.toString(ri),Double.toString(dri));
	}
	
	public void addBranch(double ri,double driu,double dril){
		addBranch(Double.toString(ri),Double.toString(driu),Double.toString(dril));
	}

	
	public void setDRIfactorForEmpty(float x){UncertaintyParser.setDRIfactorForEmpty(x);}
	public void setDRIfactorForAP(float x){UncertaintyParser.setDRIfactorForAP(x);}
	public void setDRIfactorForLT(float x){UncertaintyParser.setDRIfactorForLT(x);}
	public void setDRIfactorForGT(float x){UncertaintyParser.setDRIfactorForGT(x);}
	public void setDRIfactorForCA(float x){UncertaintyParser.setDRIfactorForCA(x);}
	public void setDRIfactorForSY(float x){UncertaintyParser.setDRIfactorForSY(x);}
	
	public float getRISum(){return sumRI();}
	public int nBranches(){return branches.size();}
	public Vector<Branch> branchsV(){return branches;}
	public Branch branchAt(int i){return branches.get(i);}
	public Branch lastBranch(){return branches.lastElement();}
	
	public void calculate(){
		int nb=branches.size();
		if(nb==0)
			return;
		
		if(nb==1){
			Branch b=branches.get(0);
			b.br=100;b.dbru=0;b.dbrl=0;
			b.rb=100;b.drbu=0;b.drbl=0;
			if(b.ri>0)
				sum=b.ri;
			return;
		}
		
		sum=sumRI();	
		
		float maxRI0=0;
		boolean isRegularErrorPropagation=true;
		
		//find branch with ds="GT" or "GE", 
		//if it ri/sum<10%, set dri=0, instead of ri*factorForGT
		for(int i=0;i<nb;i++){
			Branch b=branches.get(i);
			if(b.ri0>maxRI0 && b.dri0>=0)//note that for AP, GT/GE, empty ds, dri0 has been set to be 0
				maxRI0=b.ri0;
			
			if((b.ds.equals("GT") || b.ds.equals("GE")) && b.ri>0 && sum>0){
				if(b.ri/sum<0.1){
					b.driu=0;
					b.dril=0;
				}
			}
			
			float ddiff=0;
			float frac=0;
			float ru=0;//relative uncertainty
			
			if(b.driu>0 && b.dril>=0) ddiff=(b.driu-b.dril)/b.driu;
			if(sum>0) frac=b.ri/sum;
			if(b.ri>0) ru=b.driu/b.ri;
			
			if(frac>0.1 && (ddiff>0.01||ru>=GaussianLimit))
				isRegularErrorPropagation=false;
			
		}
		
		
		//calculate relative photon branching, strongest=100
		if(maxRI0>0){
			for(int i=0;i<nb;i++){
				Branch b=branches.get(i);
				b.rb=b.ri0*100.0f/maxRI0;
				if(b.dri0>0){
					b.drbu=b.dri0*100/maxRI0;
					b.drbl=b.drbu;
				}			
			}
		}

		
		//calculate percentage photon branching, add up to 100 (here nb>=2)
		//dx/x limit
		//if dx/x<limit, use regular error propagation for variable of Gaussian distribution
		//otherwise if dx/x of any vaiable exceeds this limit, calculate max, median, min and then
		//deduced (max-median) and (median-min) as upper and lower uncertainties, respectively.
		if(isRegularErrorPropagation){
			calculateBRRegularError();
		}else{//calculate min and max
			calculateBRExtremes();
		}


		
	}
	
	
	private void calculateBRRegularError(){
		int nb=branches.size();
		double sumDRI2=sumDRI2();
		if(sum>0){
			for(int i=0;i<nb;i++){
				Branch b=branches.get(i);
				double r=b.ri;
				double dr=b.driu;
				
				if(r>0){
					b.br=(float) (100.0f*r/sum);
					
					double d=r*r*(sumDRI2-dr*dr)+(sum-r)*(sum-r)*dr*dr;
					b.dbru=(float) (100.0*Math.sqrt(d)/(sum*sum));
					b.dbrl=b.dbru;
					
					//System.out.println(b.br+"  "+b.dbru+"  "+b.dbrl+" sum="+sum+" d="+d);
				}
			}
			
		}
	}
	
	private void calculateBRExtremes(){
		int nb=branches.size();
		float minSum=0;
		float maxSum=0;
		for(int i=0;i<nb;i++){
			Branch b=branches.get(i);
			if(b.ri>0){
				minSum+=b.ri-b.dril;
				maxSum+=b.ri+b.driu;
			}
		}
		
		if(sum>0 && minSum>0 && maxSum>0){
			for(int i=0;i<nb;i++){
				Branch b=branches.get(i);
				if(b.ri<=0)
					continue;
				
				float min=b.ri-b.dril;
				float max=b.ri+b.driu;
				
				b.br=b.ri/sum*100;
				b.dbrl=b.br-min/(min+maxSum-max)*100;
				b.dbru=max/(max+minSum-min)*100-b.br;
				
				//System.out.println(b.br+"  "+b.dbru+"  "+b.dbrl+" min"+min+" max"+max+" minSum"+minSum+" maxSum"+maxSum);
			}
		}

	}
	
	private float sumRI(){
		float out=0;
		for(int i=0;i<branches.size();i++){
			Branch b=branches.get(i);
			if(b.ri<=0)
				continue;
			
			out+=b.ri;
		}
		
		return out;
	}
	
	//sum of dri^2
	private float sumDRI2(){
		float out=0;
		for(int i=0;i<branches.size();i++){
			Branch b=branches.get(i);
			if(b.ri<=0)
				continue;
			
			out+=b.driu*b.driu;
		}
		
		return out;
	}
	
	public void reset(){
		branches.clear();

		setDRIfactorForEmpty(0.2f);
		setDRIfactorForAP(0.5f);
		setDRIfactorForLT(0.5f);
		setDRIfactorForGT(0.5f);
		setDRIfactorForCA(0.5f);
		setDRIfactorForSY(100); 

		GaussianLimit=0.1f;
		
		sum=0;
	}
	
	public void printBranch(int i){
		Branch b=branches.get(i);
		
		System.out.println("");
		System.out.println("B"+i+": %Branching="+b.br+"  +"+b.dbru+"-"+b.dbrl+" ds="+b.ds);
		System.out.println("B"+i+": relative B="+b.rb+"  +"+b.drbu+"-"+b.drbl+" ds="+b.ds);
	}
	
	//static methods:
	
	public static Branch parseBranch(String s,String ds){
		SimpleBranchingCalculator calc=new SimpleBranchingCalculator();
		Branch out=calc.new Branch(s,ds);
		return out;
	}
}
