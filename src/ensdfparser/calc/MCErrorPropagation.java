package ensdfparser.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.Vector;

/*
 * Monte-Carlo Error Propagation
 */
public class MCErrorPropagation {
    private final int NBINSMAX=10000,NBINSMIN=500;
	
	private ArrayList<Param> paramList;

	private int NP=10000;
	
	private double avg=0,peak=-1E6;
	private double[] avgs, peaks;
	
	private double min=1E6, max=-1E6;
	

	private int nbins=NBINSMAX;
	private double binWidth=0.1;
	
	//for a single function
	private Param result=null;
	private FunctionMD f=null;
	Histogram1D hist=null;
	
	//for multiple functions with the same set of sample parameters
	private Vector<Param> resultsV=new Vector<Param>();
    private Vector<FunctionMD> fV=new Vector<FunctionMD>();
	Vector<Histogram1D> histsV=new Vector<Histogram1D>();
    
	private double[] parsOfMin,parsOfMax;//parameters at max and min final values 
	private double[][] parsOfMins,parsOfMaxs;//parameters at max and min final values for each function
	private double[] mins, maxs;
	
	double[][] samplesOfVars=null; //for sampling from pre-sampled points of a list of variables
	
	// Reusable generator instance at the class level
	private final CustomRandom sharedRandom = new CustomRandom();
	private final SplittableRandom fastRandom = new SplittableRandom();
	
	public MCErrorPropagation(){
		NP=10000;
		paramList=new ArrayList<Param>();
		parsOfMin=new double[0];
		parsOfMax=new double[0];
		
		mins=new double[0];
		maxs=new double[0];
		
		avgs=new double[0];
		peaks=new double[0];

	}
	
	public MCErrorPropagation(int NP){
		this();
		setNumOfSamples(NP);
	}
	
	public MCErrorPropagation(ArrayList<Param> paramList,FunctionMD f,int NP){
		this();
		this.paramList.addAll(paramList);
		this.setFunction(f);
		setNumOfSamples(NP);
	}
	
	public MCErrorPropagation(ArrayList<Param> paramList,FunctionMD f){
		this();
		this.paramList.addAll(paramList);
		this.setFunction(f);
	}
	
	public static final class Param{
		//each par must have at least three items: val, dval_upper,dval_lower
		public double v=0;
		public double dvu=0;
		public double dvl=0;
		public double min=-1E6;
		public double max=1E6;
		public double median=0;
		public double mode=0;
		public double mean=0;
		public double vu=0;
		public double vl=0;
		
		public String name="";
		public boolean isLimit=false;
		
		public Param(){};
		public Param(String id,double pv,double dpvu,double dpvl,double pmin,double pmax,boolean pIsLimit){name=id;v=pv;dvu=dpvu;dvl=dpvl;min=pmin;max=pmax;isLimit=pIsLimit;}	
		public Param(String id,double pv,double dpvu,double dpvl,double pmin,double pmax){this(id,pv,dpvu,dpvl,pmin,pmax,false);}	
		
		public Param(String id,double pv,double dpvu,double dpvl){name=id;v=pv;dvu=dpvu;dvl=dpvl;}

		public Param(double pv,double dpvu,double dpvl){this("",pv,dpvu,dpvl);}
		public Param(double pv,double dpvu,double dpvl,double pmin,double pmax){this("",pv,dpvu,dpvl,pmin,pmax);}	

	}
	
	public void reset(){
		paramList.clear();
		parsOfMin=new double[0];
		parsOfMax=new double[0];
		
		parsOfMins=new double[0][0];
		parsOfMaxs=new double[0][0];
		
		mins=new double[0];
		maxs=new double[0];
		
		f=null;	
		fV.clear();
		
		avg=0;
		peak=-1E6;
		min=1E6;
		max=-1E6;
		
		hist=null;
		histsV.clear();
		
		nbins=NBINSMAX;
		binWidth=0.1;
		
		result=null;
		resultsV.clear();
		
		peaks=new double[0];
		avgs=new double[0];
	}
	
	//not used
	public void addParam(String name,double v,double dv){
		Param param=new Param(name,v,dv,dv);
		paramList.add(param);
	}
	//not used
	public void addParam(String name,double v,double dvu,double dvl){
		Param param=new Param(name,v,dvu,dvl);
		paramList.add(param);
	}
	
	public void addParam(String name,double v,double dvu,double dvl,double min,double max){
		addParam(name,v,dvu,dvl,min,max,false);
	}
	public void addParam(String name,double v,double dvu,double dvl,double min,double max,boolean isLimit){
		if(isLimit) {
			min=v-dvl;
			max=v+dvu;
		}
		
		Param param=new Param(name,v,dvu,dvl,min,max,isLimit);
		paramList.add(param);
	}

	public void addParam(Param par) {
		paramList.add(par);
	}

	public void setParamList(ArrayList<Param>list){
		paramList.clear();
		paramList.addAll(list);
	}
	
	public void setFunction(FunctionMD func){
		f=func;
		fV.clear();
		fV.add(func);
	}
	
	public void setFunctions(Vector<FunctionMD> funcV){

		try {
			fV.clear();
			fV.addAll(funcV);
			f=fV.get(0);
		}catch(Exception e) {}
	}
	
	public void setNumOfSamples(int N){
		if(N>0)
			NP=N;
	}

	public int getNumOfSamples(){
		return NP;
	}
	
	public int getNBins(){return nbins;}
	public double getBinWidth(){return binWidth;}
	

	public Histogram1D getHistogram(){return hist;}
	public Histogram1D getHistogramAt(int i) {
	    try{return histsV.get(i);}catch(Exception e) {}
	    return null;	    
	}
	public FunctionMD getFunction(){return f;}
	public double getAverage(){return avg;}
	public double getPeakValue(){return peak;}
	public double getCentralValue(){return result.v;}
	public Param getResult(){return result;}
	public double[] parsOfMin(){return parsOfMin;}
	public double[] parsOfMax(){return parsOfMax;}
	
	public double[] mins(){return mins;}
	public double[] maxs(){return maxs;}
	public double min() {return min;}
	public double max() {return max;}
	
	public Vector<Histogram1D> getHistogramsV(){return histsV;}
	public Vector<FunctionMD> getFunctionsV(){return fV;}
	public double getAverage(int i){return avgs[i];}
	public double getPeakValue(int i){return peaks[i];}
	public double getCentralValue(int i){return resultsV.get(i).v;}
	public Vector<Param> getResultsV(){return resultsV;}
    public double[][] parsOfMins(){return parsOfMins;}
    public double[][] parsOfMaxs(){return parsOfMaxs;}
    
	public void setReturnValuesAt(int index) {
	    if(index<0)
	        return;
	    
	    hist=histsV.get(index);
	    f=fV.get(index);
	    avg=avgs[index];
	    peak=peaks[index];
	    result=resultsV.get(index);
	    
	    parsOfMin=parsOfMins[index];
	    parsOfMax=parsOfMaxs[index];
	    
	    min=mins[index];
	    max=maxs[index];
	}
	
	public ArrayList<Param> getParamList(){return paramList;}
	

	public double[][] samplesOfVars(){return samplesOfVars;}
	
	/*
	 * get equivalent sigma of variable-Gaussian (see Roger Barlow, statistical error)
	 * e0 is a sample value of a normal distribution
	 */
	private static double sigmaVG1(double e0,double deu,double del){
		try{
			double a=2*deu*del/(deu+del);
			double b=(deu-del)/(deu+del);
			return a/(1-b*e0);
		}catch(Exception e){	
		}
		
		return 0;
	}
	
	private static double sigmaVG2(double e0,double deu,double del){
		double a=deu*del;
		double b=deu-del;
		double be2=Math.pow(b*e0,2);
		return (b*e0+Math.sqrt(be2+4*a))/2;
	}
	
	public double sample(Param par,String sampleOption){
	
		sampleOption=sampleOption.toUpperCase().trim();
		if(par.dvl==par.dvu && par.dvl>0)
			return sample_Gauss(par);
		else if(sampleOption.isEmpty() || sampleOption.equals("VG1")){
			return sample_VG1(par);
		}else if(sampleOption.equals("VG2")){
			return sample_VG2(par);
		}else{
			return customSample(par);
		}
	}
	
	public double sample_Gauss(Param par){
	
		double v0=0,v=0;
		v0=sharedRandom.nextGaussian();
		v=v0*par.dvu+par.v;		
		return v;
	}

	public double sample_uniform(Param par) {
	    double min = par.v - par.dvl;
	    double max = par.v + par.dvu;
		double diff=max-min;
		
		double offset=0;
		if(min<=0) {
			if(min==0)
				offset=diff/1.0E4;
			else
				offset=-min+diff/1.0E4;
			
			min=min+offset;//set min at diff/1.0E4
            //max=max+offset;
            max=max+offset;
		}
		/*
		//for half-life limit
		int n=(int)Math.log10(max/min)+1;		
		if(n>=4) {		
			double d=fastRandom.nextDouble();
			int n1=(int)(n*d);
            double min1=min*Math.pow(10, n1-1); //very expensive code         
			double max1=min*Math.pow(10,n1);
			if(min1>min && min1<max)
				min=min1;
			if(max1<max && max1>min)
				max=max1;
			
			//System.out.println(" n="+n+" n1="+n1+" v="+par.v+"  "+min1+" "+max1+" d="+d+" isLimit="+par.isLimit+" sample v="+v);
		}
		*/
	    return fastRandom.nextDouble(min, max);
	}
	
	public double sample_uniform_old(Param par) {
	
		//CustomRandom r=new CustomRandom();	
		
		double min=par.v-par.dvl;
		double max=par.v+par.dvu;//note that par.max could be set far beyond its upper bound for sampling value with regular Gaussian or Gaussian-like distribution		
		double diff=max-min;

		double d=sharedRandom.nextDouble();
		double v=d*diff;
			
		double offset=0;
		if(min<=0) {
			if(min==0)
				offset=diff/1.0E4;
			else
				offset=-min+diff/1.0E4;
			
			min=min+offset;//set min at diff/1.0E4
            max=max+offset;
		}
		
		int n=(int)Math.log10(max/min)+1;
		
		if(n>=4) {
			int n1=(int)(n*d);
            double min1=min*Math.pow(10, n1-1);          
			double max1=min*Math.pow(10,n1);
			if(min1<min)
				min1=min;
			if(max1>max)
				max1=max;
			
			d=sharedRandom.nextDouble();
			//ThreadLocalRandom.current().nextDouble();
			v=d*(max1-min1);
			if(v<=0) {
				v=min1;
			}
			
			v=v-offset;
			
			//System.out.println(" n="+n+" n1="+n1+" v="+par.v+"  "+min1+" "+max1+" d="+d+" isLimit="+par.isLimit+" sample v="+v);
		}
        
		return v;	
	}
	
	//incorrect sampling using Variable-Gaussian #1 in R.Barlow's paper, see written notes
	//This sampling assumes (x/(s1*s2+(s2-s1)*x)) follows a normal distribution, but it is not.
	//Because if it is so, then x would not follow the distribution f(x) of Variable-Gaussian #2,
	//but instead, a distribution of f(x)*g(x), with g(x) shifting the peak
	public double sample_VG1(Param par){		
		if(par.isLimit) 
			return sample_uniform(par);
	
		//CustomRandom r=new CustomRandom();			
		double v0=0,v=0;
			
		v0=sharedRandom.nextGaussian();
		v=v0*sigmaVG1(v0,par.dvu,par.dvl)+par.v;	
		
		return v;
	}
	
	//incorrect sampling using Variable-Gaussian #2 in R.Barlow's paper, see written notes
	public double sample_VG2(Param par){
		if(par.isLimit) 
			return sample_uniform(par);
		
		//CustomRandom r=new CustomRandom();
		
		double v0=0,v=0;
		v0=sharedRandom.nextGaussian();
		v=v0*sigmaVG2(v0,par.dvu,par.dvl)+par.v;	
		
		return v;
	}
	
	public double[] sampleAllPars_VG1(){
        double[] parVals=new double[paramList.size()];
		int npar=paramList.size();
		Param par;

		double v=0;
		
    	//Sampling each parameter
    	int ntries=0;
    	for(int i=0;i<npar;i++){
    		ntries=0;
     		par=paramList.get(i);
     		if(par.dvu==0 && par.dvl==0)
     			v=par.v;
     		else{
        		do{
        			
        			///////////////
        			// sampling             			
        			v=sample_VG1(par);
        			///////////////       			
            		
            		ntries++;
        		}while((v>par.max||v<par.min)&&ntries<1000);	
     		    //}while(v>par.max||v<par.min);	
     		}

     		//debug
        	//System.out.println(" @@@@   y="+f.v(parVals)+" v="+v+" par.v="+par.v);	
        	
    		parVals[i]=v;
    	}
    	
    	return parVals;
	}
	
	
	public double[] sampleAllPars_VG2(){
        double[] parVals=new double[paramList.size()];
		int npar=paramList.size();
		Param par;

		double v=0;
		
    	//Sampling each parameter
    	int ntries=0;
    	for(int i=0;i<npar;i++){
    		ntries=0;
     		par=paramList.get(i);
     		if(par.dvu==0 && par.dvl==0)
     			v=par.v;
     		else{
        		do{
        			
        			///////////////
        			// sampling             			
        			v=sample_VG2(par);
        			///////////////       			
            		
            		ntries++;
        		}while((v>par.max||v<par.min)&&ntries<1000);	
     		    //}while(v>par.max||v<par.min);	
     		}

     		//debug
        	//System.out.println(" @@@@   y="+f.v(parVals)+" v="+v+" par.v="+par.v);	
        	
    		parVals[i]=v;
    	}
    	
    	return parVals;
	}
	
	
	//In Barlow's paper: asymmetric statistical uncertainty, the Variable-Gaussian #1 is the
	//best estimate for the distribution of a Gaussian measurement with asymmetric uncertainties
	public double customSample(Param par){
		if(par.isLimit) 
			return sample_uniform(par);
		
		double s1=par.dvl;//lower uncertainty
		double s2=par.dvu;//upper uncertainty
		double x0=par.v;
				
        double min=par.min;
        double max=par.max;
        if(s1>s2)
        	max=Math.min(s1*s2/(s1-s2)+x0,max);
        else if(s1<s2)
        	min=Math.max(s1*s2/(s1-s2)+x0, min);

		//System.out.println("  v="+par.v+" s1="+s1+" s2="+s2+" min="+par.min+"  max="+par.max);
        
		AnyDistribution.Function f=(x)->Math.exp(-Math.pow((x-x0)/(2*s1*s2/(s1+s2)+(s2-s1)/(s1+s2)*(x-x0)),2)/2);  	
    	AnyDistribution ad=new AnyDistribution(f,min,max);   	
    	
		double v=ad.next();
		
		//System.out.println("  v="+par.v+" s1="+s1+" s2="+s2+" min="+par.min+"  max="+par.max+" sample="+v
		//		+" f="+f.v(1E-8)+"  percen(0.16)="+ad.percentile(0.16)+"  percen(0.84)="+ad.percentile(0.84)+"  integral="+ad.integral());
		
		return v;
	}
	
	private double[] customSamples(Param par,int NP){
		double s1=par.dvl;//lower uncertainty
		double s2=par.dvu;//upper uncertainty
		double x0=par.v;
		
		double[] samples=new double[NP];
		if(s1==0 && s2==0){
			Arrays.fill(samples,x0);
		}else{
			try{
				//System.out.println(" v="+par.v+" par.min="+par.min+"   par.max="+par.max+" isLimit="+par.isLimit);
				
				AnyDistribution ad=null;
				if(par.isLimit) {
					//CustomRandom r=new CustomRandom();
					samples=sharedRandom.doubles(NP,par.v-par.dvl, par.v+par.dvu).toArray();
				}else {
					AnyDistribution.Function f=(x)->Math.exp(-Math.pow((x-x0)/(2*s1*s2/(s1+s2)+(s2-s1)/(s1+s2)*(x-x0)),2)/2);  	
			    	ad=new AnyDistribution(f,par.min,par.max);  	
			    	
			    	//if(par.min<0) System.out.println(" v="+par.v+" par.min="+par.min+"   par.max="+par.max);
			    	
					samples=ad.quickSample(NP);
				}
 
				//for(double d:samples) if(d<0) System.out.println("  d="+d+"  min="+par.min+"  max="+par.max);
			}catch(IllegalArgumentException e){
				Arrays.fill(samples,x0);
			}

		}
		
		return samples;
	}
	
	//In Java, a 2D array is an arrays of 1D-array. Each element could have different length, 
	//unlike other languages in which, each element must have the same length
	public double[][] makeSamplesOfVars(){
		double[][] samplesOfVars=new double[paramList.size()][NP];
		int npar=paramList.size();
    	for(int i=0;i<npar;i++){
    		samplesOfVars[i]=customSamples(paramList.get(i),NP);
    		
    		/*
    		//debug
    		if(samplesOfVars[i].length<1){
    			System.out.println(" npar="+npar+" i="+i+" id="+paramList.get(i).name+" par="+paramList.get(i).v+"  "+paramList.get(i).dvl+"  "
    		+paramList.get(i).dvu+" min="+paramList.get(i).min+" max="+paramList.get(i).max);
    			System.out.println("     par0="+paramList.get(0).v);
    		}
    		*/
    	}
		return samplesOfVars;
	}
	
	/*
	 * sample from pre-sampled points of a list of variables
	 */
	private double[] sampleAllPars(double[][] samplesOfVars){
		int npar=paramList.size();
        double[] parVals=new double[npar];
		//CustomRandom r=new CustomRandom();
		
		int randomIndex=0;
    	//Sampling each parameter
    	for(int i=0;i<npar;i++){
    		randomIndex=(int)(sharedRandom.nextDouble()*(samplesOfVars[i].length-1));
    		
    		//if(samplesOfVars[i].length<1) System.out.println(" npar="+npar+" i="+i+" par="+paramList.get(i).v+"  "+paramList.get(i).dvl+"  "+paramList.get(i).dvu+" min="+paramList.get(i).min+" max="+paramList.get(i).max);
    		//if(samplesOfVars[i].length<1)  System.out.println("   randomIndex="+randomIndex+"  NP="+NP+"  samplesOfVars[i].lengh="+samplesOfVars[i].length);
    		
    		parVals[i]=samplesOfVars[i][randomIndex];
    	}

     		//debug
        	//if(i==3&&v>0.3) System.out.println(" @@@@  n="+n+" y="+f.v(parVals)+" v0="+v0+" v="+v+" par.v="+par.v+"  *"+sigmaVG(v0,par.dvu,par.dvl));	
    	
    	return parVals;
	}
	
	public boolean doWork(){
		return doWork("");
	}
	
	public boolean doWork(String sampleOption){
		
		int npar=0;
        double[] parVals;
        double[] centrals;
        boolean zeroError=true;
		Param par;
		
		resultsV.clear();
		histsV.clear();
		
		boolean success=true;
		
		try{
			npar=paramList.size();
			parVals=new double[npar];
			
			avgs=new double[fV.size()];
			peaks=new double[fV.size()];
			centrals=new double[fV.size()];
			
			parsOfMins=new double[fV.size()][npar];
			parsOfMaxs=new double[fV.size()][npar];			
			
			mins=new double[fV.size()];
			maxs=new double[fV.size()];		
			
			Arrays.fill(mins,1E6);
			Arrays.fill(maxs,-1E6);
			
			
			for(int i=0;i<npar;i++){
				par=paramList.get(i);
				parVals[i]=par.v;
		        
				//if(npar==2) 
				//if(Math.abs(parVals[0]-202)<1) 
				//System.out.println(" i=  "+i+" name="+par.name+" par="+par.v+"  "+par.dvl+"  "+par.dvu);
		        
				if(par.dvl!=0 || par.dvu!=0)
					zeroError=false;
			}
			
			for(int i=0;i<fV.size();i++) {

	            //System.out.println("  ******* ");
	        	
				centrals[i]=fV.get(i).v(parVals);
				
		        //if(Math.abs(parVals[1]-133.9)<1) 
		        //    System.out.println("  e="+parVals[1]+"  central="+centrals[i]);
		        
				if(zeroError){
			        result=new Param();
			        result.v=centrals[i];
			        result.dvu=0;
			        result.dvl=0;
			        result.min=centrals[i];
			        result.max=centrals[i];
			        
			        resultsV.add(result);
				}				
			}
			
			if(zeroError)
				return false;
				
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		int n=0;

		/////////////////////////////////////
		//initialize samples of all variables
		//double[][] samplesOfVars=makeSamplesOfVars();
		
		SampleMaker sampleMaker=null;
		sampleOption=sampleOption.toUpperCase().trim();

		//if(sampleOption.equals("VG1")){
		if(sampleOption.isEmpty() || sampleOption.equals("VG1")){
			sampleMaker=()->sampleAllPars_VG1();
		}else if(sampleOption.equals("VG2")){
			sampleMaker=()->sampleAllPars_VG2();
		}else{
			samplesOfVars=makeSamplesOfVars();
			sampleMaker=()->sampleAllPars(samplesOfVars);
		}
			
		double[][] ys=new double[fV.size()][NP];
		
		avg=0;
		Arrays.fill(avgs,0);	
		
		//int count=0,count1=0;
		
		int ntries=0;
		
        while(n<NP){
        	    
        	///////////////
        	//// sampling
        	///////////////

        	parVals=sampleMaker.get();
        	
        	double[] tempY=new double[fV.size()];
        	try {
        	    boolean isBad=false;

            	for(int i=0;i<fV.size();i++) {
            		
                	tempY[i]=fV.get(i).v(parVals);//throws exception if return value is invalid, as defined user's function
                	
                	//if(ntries>=0 && ntries<2) for(int j=0;j<parVals.length;j++) System.out.println(" #f="+i+" n="+n+"  j="+j+"   val="+parVals[j]+" tempY[i]="+tempY[i]
                	//		+" sampleOption="+sampleOption+" fv.size="+fV.size());

                	
                	if(Double.isNaN(tempY[i]) || Double.isInfinite(tempY[i])) {
                	    isBad=true;
                	    break;
                	}
                	                   	
                	//double a=x1*0.001+0.013;
                	//double n=x2*0.04+1.86;
                	//double h=x3*1-100;
                	//xt[i]=0.09+(0.44-0.09)/Math.pow((1+Math.pow(-a*h,n)),(1-1.0/n));
                	//if(parVals.length<=2){
                	//	System.out.println("   n="+n+" y="+ys[n]);	
                	//	for(double a:parVals) System.out.println("  **"+a);
                	//}

        		
            	} 
            	if(isBad) {
            		ntries++;
            		if(ntries>NP)
            			break;
            		
            	    continue;
            	}
        	}catch(Exception e) {
        		ntries++;     
        		
        		
        		if(ntries>NP)
        			break;
        		
        		continue;
        	}
        	
        	for(int i=0;i<fV.size();i++) {
            	
            	ys[i][n]=tempY[i];
        	
            	avgs[i]+=ys[i][n]/NP;
        	
            	if(ys[i][n]>maxs[i]){
            		maxs[i]=ys[i][n];
            		parsOfMaxs[i]=parVals.clone();
            	}
            	if(ys[i][n]<mins[i]){
            		mins[i]=ys[i][n];
            		parsOfMins[i]=parVals.clone();
            	}
        	}


        	//debug
        	/*
        	if(fV.size()>1) {
        		//for(int i=0;i<npar;i++)
        		//    System.out.println(" par#"+i+" name="+paramList.get(i).name+"  value="+parVals[i]+" bxl0="+ys[0][n]+" bxl1="+ys[1][n]);
        		if(ys[1][n]<=1.0) count++;
        		if(parVals[3]<0.1) count1++;
        	}
        	*/
        	
        	ntries=0;
        	
        	n++;

        }
        
        if(n==0) {
        	success=false;
        }
        
        for(int i=0;i<fV.size();i++) {
        	result=findParamOfCDF(ys[i],centrals[i],mins[i],maxs[i]);
        	histsV.add(hist);
        	
        	//System.out.println(" i="+i+" central="+centrals[i]+" min="+mins[i]+" max="+maxs[i]);
        	
        	resultsV.add(result);
        }
        
        result=resultsV.get(0);
        hist=histsV.get(0);
        
        
        //NOTE that result and hist will be reset to be the result and hist of the current record by MCEP.setReturnValuesAt(index) in
        //     BXLCalculator.BXLW_MC(int L,String Loption,String type,int NP); 
        
        //System.out.println(" NP="+NP+" n="+n+"  central="+central);
        
        return success;

	}
	
	public Param findParamOfCDF(double[] ys,double central,double min,double max) {
        Param result=null;
        
        double vl=0,vu=0,dvu=0,dvl=0,tempMax=max,median=0,mode=0,mean=0;
        double r1=0.1586553f,r2=0.8413447f,rm=0.5000;
        
        boolean done=false;
        int ntries=0;
        while(!done){     	
            hist=makeHistogram(ys,central,min,tempMax);  
            tempMax=hist.findBinCenterOfCDFByPercentageRatio(0.9995f);
            int nbin=hist.getBin(tempMax);

            /*
            //debug
            int t=0;
            for(int i=0;i<nbins;i++) {
            	double d=hist.getBinCenter(i);
            	if(d>0.006 && d<0.018) t+=hist.getBinCount(i);
            }
            System.out.println(" **** nbin="+nbin+" nbins="+nbins+" v="+central+" min="+min+" max="+tempMax+" total counts="+t);
            */

            if(nbin<0.8*nbins && ntries<=3){
            	ntries++;
            	continue;
            }
            
            vl=hist.findBinCenterOfCDFByPercentageRatio(r1);
            vu=hist.findBinCenterOfCDFByPercentageRatio(r2);
            median=hist.findBinCenterOfCDFByPercentageRatio(rm);
            
            /*
            //check two approaches of integration
            double vl1=hist.percentile(r1);
            double vu1=hist.percentile(r2);
            double d1=Math.abs(vl-vl1);
            double d2=Math.abs(vu-vu1);
            if(vl1/vu1<1E-6 && vl/vu<1E-6)
            	d1=0;       	
            if(d1/vl>0.001||d2/vu>0.001)
            	System.out.println(String.format("###  v=%-12.2e ntries=%-2d  %-40s %-60s",central, ntries," vl: "+((vl-vl1)>0?"+":"-")
            			+String.format("%12.2f",100*d1/vl)+"%  vu: "+((vu-vu1)>0?"+":"-")+String.format("%12.2f",100*d2/vu)+"%",
            			String.format("vl=%12.2e vu=%12.2e   vl1=%12.2e vu1=%12.2e",vl,vu,vl1,vu1)));
            */
            
            //System.out.println("MCErrorProp 805: v="+central+"  vl="+vl+" vu="+vu+"  hist.percentile(r1)="+hist.percentile(r1)+"  hist.percentile(r2)="+hist.percentile(0.84));
            //System.out.println(" **** nbin="+nbin+" nbins="+nbins+" v="+central+" min="+min+" max="+tempMax+" median="+median);
            
            dvu=vu-central;
            dvl=central-vl;
            
            if(dvu<0) dvu=0;
            if(dvl<0) dvl=0;
            
            //if(dvu<binWidth || dvl<binWidth){           	
            //}
            
            done=true;
        }

        double sum=0;
        int count=0;
        for(int i=0;i<ys.length;i++) {
        	if(ys[i]<=0)
        		continue;
        	
        	sum+=ys[i];
        	count++;
        }
        
        //mean=hist.getAverage();
        mean=sum/count;
        
        mode=hist.getBinCenterOfMaxCount();
        
        double norm=hist.getMaxBinCount();
        double sigmaP0=dvu*0.9;//sigma+ in Variable Gaussian (1) in Barlow's paper: Asymetric Statistical Errors
        double sigmaM0=dvl*0.9;//sigma-
        double[] pars=hist.fitVariableGauss(norm, central, sigmaP0, sigmaM0, min, max);
        //double norm=pars[0];
        //double central=pars[1];//this is actually the mode value of the fit to the hist
        //double sigmaP=pars[2];
        //double sigmaM=pars[3];
        
        if(pars!=null && pars[1]>0 && pars[0]>0) {
        	if(Math.abs(pars[0]-norm)<0.05*norm && Math.abs(pars[1]-central)<0.05*central)
        		mode=pars[1];
        }
        
        result=new Param();
        result.v=central;
        result.dvu=dvu;
        result.dvl=dvl;

        result.min=min;
        result.max=max;	
        
        result.vl=vl;
        result.vu=vu;
        result.median=median;
        result.mean=mean;
        result.mode=mode;
        
        return result;
	}
	
	
	public Function1D getVariableGaussFit(double normIn,double centralIn,double sigmaPIn,double sigmaMIn,double xmin,double xmax) {
        double[] pars=hist.fitVariableGauss(normIn, centralIn, sigmaPIn, sigmaMIn, xmin, xmax);
        double norm=pars[0];
        double central=pars[1];
        double sigmaP=pars[2];
        double sigmaM=pars[3];
        double s1=2*sigmaP*sigmaM/(sigmaP+sigmaM);
        double s2=(sigmaP-sigmaM)/(sigmaP+sigmaM);
        
        Function1D func=(x)->norm*Math.exp(-1.0/2.0*Math.pow((x-central)/(s1+s2*(x-central)),2));
        
        return func;
	}
	
	public Histogram1D makeHistogram(double[] ys,double central,double min,double max){
		
        double xstart=min,xend=max;
        binWidth=Math.abs(central)/1000.0;
 
        if(binWidth>0){
        	nbins=(int)((max-min)/binWidth);
            if(nbins>NBINSMAX)
            	nbins=NBINSMAX;
            else if(nbins<NBINSMIN)
            	nbins=NBINSMIN;
        }
       
        binWidth=(max-min)/nbins;
        
        //System.out.println(" nbins="+nbins+" min="+min+" max="+max+"  binWidth="+binWidth+" central="+central);
        
        Histogram1D h1=new Histogram1D(nbins,xstart,xend);
        for(int i=0;i<ys.length;i++)
        	h1.fill(ys[i]);
            
        
        int index=0;
        double[] xtemp=new double[nbins];
        double[] ytemp=new double[nbins];
        
        double xOfMaxCount=-1;
        int maxCount=0;
        
        for(int i=0;i<nbins;i++){
        	int xcount=h1.getBinCount(i);
        	if(xcount<=0)
        		continue;
        	
        	if(xcount>maxCount){
        		maxCount=xcount;
        		xOfMaxCount=h1.getBinCenter(i);
        	}
        	
        	xtemp[index]=h1.getBinCenter(i);
        	ytemp[index]=xcount;
        	index++;
        }
        
        peak=xOfMaxCount;
        
        /*
        //for plotting
        double[] x=new double[index];
        double[] y=new double[index];
        for(int i=0;i<index;i++){
        	x[i]=xtemp[i];
        	y[i]=ytemp[i];
        }
         
         */
        
        h1.makeCDF();   
        
        return h1;
	}
	
	//inner classes and interfaces
	public class CustomRandom extends Random{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private int NP=1000000;//total number of points to be sampled
		private double[] xs;
		
		public CustomRandom(){
			super();		
		}
		
		public CustomRandom(int NP,double min,double max, Function1D pdf){
			this();
			init(NP,min,max,pdf);
		}
		
		public void init(double min,double max,Function1D pdf){
			init(NP,min,max,pdf);
		}
		
		public void init(int NP,double min,double max,Function1D pdf){
	        int NX=1000;//equivalent to bin size of a histogram
	        double[] x= new double[NX+1];
	        double[] y= new double[NX+1];
	        
	        double interval=(max-min)/NX;//equivalent to bin width
	        
	        double ysum=0;
	        
	        this.NP=NP;
	        xs=new double[NP+1];
	        
	        for (int k=0;k<=NX;k++){
	            x[k]=min+k*interval;
	            y[k]=pdf.v(x[k]);
	            ysum+=y[k];
	        }
	        
	        
	        int count=0;
	        for(int i=0;i<=NX;i++){
	        	int nxi=(int) (y[i]/ysum*NP);
	        	for(int j=0;j<nxi;j++){
	        		xs[count]=x[i];
	        		count++;
	        	}       		
	        }
	        
	        if(count<=NP){
	        	double[] newXS=new double[count];
	        	for(int i=0;i<count;i++)
	        		newXS[i]=xs[i];
	        	
	        	xs=newXS;
	        }
		}
		
		public double nextXValue(){
	    	int n=xs.length;
	    	int index=-1;
	    	while(index<0 || index>=n){
	    		index=(int) (n*nextDouble());
	    	}
	    	
	    	return xs[index];
		}		
	}
	
	public static interface Function1D{
		public double v(double x) ;
	}
	
	public static interface Function2D{
		public double v(double x,double y);
	}
	
	public static interface FunctionMD{
		public double v(double[] params) throws InvalidFunctionResultException;
	}
	
	private static interface SampleMaker{
		public double[] get();
	}
}
