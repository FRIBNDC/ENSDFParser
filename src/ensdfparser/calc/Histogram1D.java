package ensdfparser.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.freehep.math.minuit.MnUserParameters;

import ensdfparser.math.Fit;
import ensdfparser.math.UserFunction;


public class Histogram1D {
	private int totalCount;
	private int[] binCounts;
	private int numberOfBins=0, maxBinCount=0,binOfMaxCount=0;	
    private double[] lowBinLimit;
	private ArrayList<Double> xValues;
	private double xmin, xmax, binWidth;

	private String name,title,xtitle,ytitle;

	private int defaultNumberOfBins=100;
	
	private int[] CDFCounts;//counts of each bin of CDF (cumulative distribution=integral of binCounts)
	
	private boolean isXsorted=false;
	
	public Histogram1D(){}
	
	public Histogram1D(double[] dataValue){
		if(dataValue==null || dataValue.length==0)
			return;
		
		int n=dataValue.length;
		xValues=new ArrayList<Double>();
		for(int i=0;i<n;i++)
			xValues.add(dataValue[i]);
		
		setXValues(xValues);
	}
	
	public Histogram1D(ArrayList<Double> dataValues){
		setXValues(dataValues);
	}
	
	public Histogram1D(String name,String title,int nbins,double xmin,double xmax){
		this.name=name;
		this.title=name;
		this.xmin=xmin;
		this.xmax=xmax;
		numberOfBins=nbins;	
		totalCount=0;
		xValues=new ArrayList<Double>();
		
		initializeBins();
	}
	
	public Histogram1D(int nbins,double xmin,double xmax){
		this("h1d","h1d",nbins,xmin,xmax);
	}
	
	public void fill(double x){
		int ibin=getBin(x);
		if(ibin>=0 && ibin<=numberOfBins){
			xValues.add(x);
			totalCount++;
			binCounts[ibin]++;
		}
	}
	
	public void sortXValues(){
		int n=xValues.size();
		if(isXsorted || xValues.size()==0)
			return;
		
		Double[] valuesArray=new Double[n];
		xValues.toArray(valuesArray);	
		Arrays.sort(valuesArray);
	
		xValues.clear();		
		for(int i=0;i<n;i++){
			xValues.add(valuesArray[i]);	
		}
	}

	//used by setXValues(), rebins
	private void sortBins(){
		int n=xValues.size();
		if(xValues.size()==0)
			return;
		if(numberOfBins<=0)
			numberOfBins=defaultNumberOfBins;

		if (n>1){		
			for (int i =1 ; i<n; i++){
				double energy=xValues.get(i);
				if (energy < xmin) xmin = energy;
				if (energy > xmax) xmax = energy;
					
				//System.out.println("i="+i+" max="+xmax+" min="+xmin+" e="+dataValue[i]);
			}
		}		
		
        initializeBins();
		
		for (int i =0 ; i<n; i++){
			double x=xValues.get(i);
			int ibin=(int) ((x-lowBinLimit[0])/binWidth);
			binCounts[ibin]++;
			
			//System.out.println("i="+i+" max="+xmax+" min="+xmin+" x="+x+" ibin="+ibin+" count[ibin]="+binCounts[ibin]+" lowBinLimit[0]="+lowBinLimit[0]);
		}
		
		findBinOfMaxCount();
	}

	private void initializeBins(){
		binCounts= new int[numberOfBins+1];
		CDFCounts= new int[numberOfBins+1];
		lowBinLimit = new double[numberOfBins+1];
		Arrays.fill(binCounts,0);
		Arrays.fill(CDFCounts,0);
		
		binWidth = (xmax-xmin)/numberOfBins;
		lowBinLimit[0]=xmin-binWidth/2;	
		for (int i =1; i<=numberOfBins; i++){
			lowBinLimit[i]=lowBinLimit[i-1]+binWidth;
		}
		
		
	}
	

	
	public String getName(){return name;}
	public String getTitle(){return title;}
	public String getXTitle(){return xtitle;}
	public String getYTitle(){return ytitle;}
	public int getTotalCount(){return totalCount;}
	public int getNBins(){return numberOfBins;}
	public double getXMin(){return xmin;}
	public double getXMax(){return xmax;}
	public ArrayList<Double> getXValues(){return xValues;}
	public double[] getXValuesArray(){
		int n=xValues.size();
		double[] out=new double[n];
		for(int i=0;i<n;i++)
			out[i]=xValues.get(i);

		return out;	
	}
	
	
	public double getBinWidth(){
		return binWidth;
	}
	
	public int getBin(double x){
		try{
			int ibin=(int) ((x-lowBinLimit[0])/binWidth);
			if(ibin>=0)
				return ibin;
		}catch(Exception e){}
		
		return -1;
	}
	
	/*
	 * bin number starts from 0 not 1
	 * ibin=0 for first bin
	 */
	public double getBinCenter(int ibin){
		return xmin+ibin*binWidth;
	}
	
	public int getBinCount(int ibin){
		try{
			return binCounts[ibin];
		}catch(Exception e){}
		
		return -1;
	}
	
	public int[] getBinCounts(){
		return binCounts;
	}
	
	public int getBinOfMaxCount(){
		if(binOfMaxCount<=0)
			binOfMaxCount=findBinOfMaxCount();
		
		return binOfMaxCount;
	}
	
	public double getBinCenterOfMaxCount(){
		return getBinCenter(getBinOfMaxCount());
	}
	
	public int getMaxBinCount(){
		if(binOfMaxCount<=0)
			binOfMaxCount=findBinOfMaxCount();//maxBinCount is set in findBinOfMaxCount()
		
		return maxBinCount;	
	}
	
	public double getAverage(){
		double avg=0;
		for(int i=0;i<numberOfBins+1;i++){
			int binCount=binCounts[i]; 
			if(binCount>0){
				double x=getBinCenter(i);
				avg+=x*binCount/totalCount;
			}
		}
		
		return avg;
	}
	
	
	public void setName(String s){name=s;}
	public void setTitle(String s){title=s;}
	public void setXTitle(String s){xtitle=s;}
	public void setYTitle(String s){ytitle=s;}
	
	public void setXValues(ArrayList<Double> dataValues){
		try{
			xValues=dataValues;
			totalCount=xValues.size();
			sortBins();	
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	public void setNBins(int nbins){
		if(nbins<0 || nbins==numberOfBins)
			return;
		
		numberOfBins=nbins;
		sortBins();
	}
	
	
	/*
	 * rebin the histogram by setting 
	 * nbins=nbins/n
	 */
	public void rebin(int n) {
		if(n<=0 || n>numberOfBins/4)
			return;
		
		setNBins(numberOfBins/n);	
	}
	
	/*
	 * return integral from x1 to x2
	 */
	public int integral(double x1,double x2){
		int out=0;
		int nbin1=0,nbin2=0;
		nbin1=getBin(x1);
		nbin2=getBin(x2);
		
		out=integralByBin(nbin1,nbin2);
		
		return out;
	}
	
	public int integralByBin(int nbin1,int nbin2){
		int sum=0;
		if(nbin1<0)
			nbin1=0;
		if(nbin2>numberOfBins)
			nbin2=numberOfBins;
		
		int count=0;
		for(int i=nbin1;i<=nbin2;i++){
			count=this.getBinCount(i);
			if(count>0){
				sum+=count;
			}
		}
		
		return sum;
	}
	
	
	/*
	 * get the CDF counts by integral at each bin
	 */
	public void makeCDF(){
		for(int i=0;i<=numberOfBins;i++){
			CDFCounts[i]=integralByBin(0,i);
		}
		
		sortXValues();
	}
	
	/*
	 * find the bin corresponding to the given CDF value (integral from bin=0 to this bin)
	 */
	public int findBinOfCDF(int cdf){
		int i1=0,i2=numberOfBins;
		int im=0;
		while(i1<i2-1){
			im=(i1+i2)/2;
			
			if(cdf<CDFCounts[im])
				i2=im;
			else
				i1=im;
		}
		
		//reaching here, i1+1=i2 or i1=i2=0
		if(i2==(i1+1)){
			int mean=(CDFCounts[i1]+CDFCounts[i2])/2;
			if(cdf<mean)
				return i1;
			else
				return i2;
		}
		
		return -1;
	}
	
	/*
	 * alternative to fidnBinCenterOfCDFByPercentage(cdf)
	 * but not dependent on bin size and width.
	 * It uses sorted x-values and uses index=totalCount*cdf
	 * to get the x corresponding to cdf
	 * NOTE: must call sortXValues() before this call
	 */
	public double percentile(double r){
		int n=xValues.size();	
		if(r>=1)
			return xmax;
		if(r<=0 || n<=1)
			return xmin;
		
		int index=(int)(n*r);	
		double d=0,x=xmin,x1=0;
		try{
			d=n*r-index;
			index=index-1;

			x=xValues.get(index);
			x1=xValues.get(index+1);
			return x+d*(x1-x);
		}catch(IndexOutOfBoundsException e){
			return x; 
		}
	}
	
	public double findBinCenterOfCDF(int cdf){
		return getBinCenter(findBinOfCDF(cdf));
	}
	
	public int findBinOfCDFByPercentage(double r){
		//System.out.println("  r="+r+"  totalCount="+totalCount+"  r*totalCount="+r*totalCount);
		
		return findBinOfCDF((int)(r*totalCount));
	}
	
	public double findBinCenterOfCDFByPercentage(double r){
		return findBinCenterOfCDFByPercentageRatio(r/100);
	}
	
	public double findBinCenterOfCDFByPercentageRatio(double r){
		return getBinCenter(findBinOfCDFByPercentage(r));
	}
	
	/*
	 * using Golden-section search
	 */
	public int findBinOfMaxCount(){	
		try{					
			Vector<Integer> indexV=new Vector<Integer>();
			Vector<Integer> tempCountsV=new Vector<Integer>();
			for(int i=0;i<binCounts.length;i++){
				int binCount=binCounts[i];
				if(binCount>0){ 
					tempCountsV.add(binCount);
					indexV.add(i);
				}
			}
			
			int[] counts=new int[indexV.size()];
			for(int i=0;i<tempCountsV.size();i++)
				counts[i]=tempCountsV.get(i);
			
			//using Golden-section search
			int a=0,b=indexV.size()-1;
			int c=0,d=0, prevC=0,prevD=0;
			double gr=(Math.sqrt(5f)+1)/2.0;
			
			c=(int)(b-(b-a)/gr);
			d=(int)(a+(b-a)/gr);
			
			//int n=0;
			while(c<d){
				
				//debug
				//System.out.println(" A# n="+n+"  a="+a+"  b="+b+"  c="+c+"   d="+d);
				//System.out.println("      count(c)="+counts[c]+"  count[d]="+counts[d]+"   totalBin="+numberOfBins+" count="+totalCount);
				
				if(counts[c]>counts[d])
					b=d;
				else
					a=c;
				
				c=(int)(b-(b-a)/gr);
				d=(int)(a+(b-a)/gr);
				
				if(c==prevC && d==prevD)//avoid infinite loop
					break;
				
				prevC=c;
				prevD=d;
				
				//debug
				//System.out.println(" B#  n="+n+"  a="+a+"  b="+b+"  c="+c+"   d="+d);
				//System.out.println("      count(c)="+counts[c]+"  count[d]="+counts[d]);
				//n++;
			}


			int indexOfMax=indexV.get(a);
			int maxCount=counts[a];
			for(int i=a+1;i<=b;i++){
				if(counts[i]>maxCount){
					maxCount=counts[i];
					indexOfMax=indexV.get(i);
				}
			}
			
			//for(int i=indexOfMax-2;i<indexOfMax+2;i++)	System.out.println("  ** i="+i+"  "+binCounts[i]+" index="+indexOfMax);
			binOfMaxCount=indexOfMax;
			maxBinCount=maxCount;
			
			return indexOfMax;
		}catch(Exception e){
			return 0;
		}	
	}
	public double[] fit0(ParametricUnivariateFunction f,double[] pars0, double xmin,double xmax) {
		return fit0(f,pars0,xmin,xmax,1.0);
	}
	public double[] fit0(ParametricUnivariateFunction f,double[] pars0, double xmin,double xmax,double multiplier) {
		
		int nbins=this.getNBins();
		double[] x=new double[nbins+1];
		double[] y=new double[nbins+1];
		double[] Dy=new double[nbins+1];
		double[] ws=new double[nbins+1];
		double sumW=0,minW=1E9;;
		
		for(int i=0;i<=nbins;i++) {
			x[i]=getBinCenter(i);
			y[i]=getBinCount(i);
			if(y[i]>0) {
				Dy[i]=Math.sqrt(y[i]);
				ws[i]=1.0/Math.pow(Dy[i],3);
			}else {
				Dy[i]=0;
				ws[i]=minW;
			}
			
			 if(ws[i]<minW)
				 minW=ws[i];
		}
		
		for(int i=0;i<=nbins;i++) {			
			if(y[i]<=0)				
				ws[i]=minW;
			
			sumW+=ws[i];		
		}
		for(int i=0;i<=nbins;i++) 
			ws[i]=ws[i]/sumW;

		 
		
		SimpleCurveFitter fitter=SimpleCurveFitter.create(f,pars0);		
	
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i=0;i<x.length;i++) { 
        	if(x[i]>xmax || x[i]<xmin)
        		continue;
        	
            obs.add(ws[i], x[i]*multiplier, y[i]);
            //obs.add(x[i], y[i]);
        }
        
        double[] pars = fitter.fit(obs.toList());
        
		return pars;
	}

	public double[] fitVariableGauss(double normIn,double centralIn,double sigmaPIn,double sigmaMIn,double xmin,double xmax) {
		
        UserFunction f=new UserFunction() {
        	public double valueAt(double x,double[] pars){
		        double norm=pars[0];
		        double central=pars[1];
		        double sigmaP=pars[2];
		        double sigmaM=pars[3];
		        double s1=2*sigmaP*sigmaM/(sigmaP+sigmaM);
		        double s2=(sigmaP-sigmaM)/(sigmaP+sigmaM);
		        
				double v=norm*Math.exp(-1.0/2.0*Math.pow((x-central)/(s1+s2*(x-central)),2));
				
				return v;

        	}        	
        };
        
        double norm0=normIn;
        double central0=centralIn;        
       
        
        double sigmaP0=sigmaPIn;
        double sigmaM0=sigmaMIn;
        double[] pars=new double[4];
        
        double middle=xmin+(xmax-xmin)/2;
        double range=(xmax-xmin);
        
        //System.out.println("1 sigmaP="+sigmaP0+" sigmaM="+sigmaM0+" middle="+middle+" range="+range+" central="+central0);
        
        if(sigmaP0<=0) {
            if(central0>middle+range/10)
                sigmaP0=(xmax-central0)/2.0;
            else
                sigmaP0=(xmax-central0)/3.0;
        }
        if(sigmaM0<=0) {
            if(central0>middle+range/10)
                sigmaM0=(central0-xmin)/3.0;
            else
                sigmaM0=(central0-xmin)/2.0; 
        }
        
        pars[0]=norm0;
        pars[1]=central0;
        pars[2]=sigmaP0;
        pars[3]=sigmaM0;
        
        double xminFit=central0-5*sigmaM0;
        double xmaxFit=central0+5*sigmaP0;
        if(xminFit<xmin)
        	xminFit=xmin;
        if(xmaxFit>xmax)
        	xmaxFit=xmax;
     
        pars=fit(f, pars, xminFit,xmaxFit);    
        
        return pars;
	}

	public double[] fitGauss() {
		
		int nbins=this.getNBins();
		double[] x=new double[nbins];
		double[] y=new double[nbins];
		double[] Dy=new double[nbins];
		double[] ws=new double[nbins];
		double sumW=0,minW=1000;;
		
		for(int i=0;i<nbins;i++) {
			x[i]=getBinCenter(i);
			y[i]=getBinCount(i);
			if(y[i]>0) {
				Dy[i]=Math.sqrt(y[i]);
				ws[i]=1.0/y[i];
			}else {
				Dy[i]=0;
				ws[i]=minW;
			}
			
			 if(ws[i]<minW)
				 minW=ws[i];
		}
		
		for(int i=0;i<nbins;i++) {			
			if(y[i]<=0)				
				ws[i]=minW;
			
			sumW+=ws[i];		
		}
		for(int i=0;i<nbins;i++) 
			ws[i]=ws[i]/sumW;

		 
		GaussianCurveFitter fitter=GaussianCurveFitter.create();		
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i=0;i<x.length;i++) 
            obs.add(ws[i],x[i], y[i]);
        
        double[] pars = fitter.fit(obs.toList());
        
		return pars;
	}	

	public double[] fit(UserFunction f,double[] pars0, double xmin,double xmax) {
		try {
			int nbins=this.getNBins();
			double[] x=new double[nbins+1];
			double[] y=new double[nbins+1];
			double[] Dy=new double[nbins+1];
			double[] ws=new double[nbins+1];
			double sumW=0,minW=1E9;;
	        
			double factor=1.0;
			double central=pars0[1];
			
	        if(central>0 && Math.abs(Math.log10(central))>3) {
	            factor=Math.floor(Math.log10(central));
	            factor=Math.pow(10,-factor);
	        }
	        
	        xmin=xmin*factor;
	        xmax=xmax*factor;
	 
			for(int i=0;i<=nbins;i++) {
				x[i]=getBinCenter(i)*factor;
				y[i]=getBinCount(i);
				if(y[i]>0) {
					Dy[i]=Math.sqrt(y[i]);
					ws[i]=1.0/Math.pow(Dy[i],3);
				}else {
					Dy[i]=0;
					ws[i]=minW;
				}
				
				 if(ws[i]<minW)
					 minW=ws[i];
			}
			
			for(int i=0;i<=nbins;i++) {			
				if(y[i]<=0)				
					ws[i]=minW;
				
				sumW+=ws[i];		
			}
			
			for(int i=0;i<=nbins;i++) 
				ws[i]=ws[i]/sumW;

			int[] indexes=new int[x.length];
			int count=0;
	        for (int i=0;i<x.length;i++) { 
	        	if(x[i]>xmax || x[i]<xmin || y[i]<=0)
	        		continue;
	        	
	            indexes[count]=i;
	            count++;
	            		
	            //obs.add(x[i], y[i]);
	        }
	        double[] x1=new double[count];
	        double[] y1=new double[count];
	        double[] Dy1=new double[count];
	        

	        for (int i=0;i<x1.length;i++) { 
	        	int index=indexes[i];
	        	x1[i]=x[index];
	        	y1[i]=y[index];
	        	Dy1[i]=Dy[index];
	        }
	        
	        Fit userFit=new Fit(x1,y1,Dy1);

	        //central,sigmaP,sigmaM
	        for(int i=1;i<=3;i++)
	        	pars0[i]=pars0[i]*factor;
	        
	        
	        //double[] ps=userFit.solve(x,y);
	        MnUserParameters upar=new MnUserParameters();
	        for(int i=0;i<pars0.length;i++) {
	        	upar.add("a"+i, pars0[i],Math.abs(pars0[i]*0.01));
	        }
	        userFit.setUserParameters(upar,pars0.length);
	        
	        //norm
	        
	        userFit.getUserParameters().setLowerLimit(0,pars0[0]*0.5);	
	        userFit.getUserParameters().setUpperLimit(0,pars0[0]*1.5);	

	        //central
	        userFit.getUserParameters().setLowerLimit(1,pars0[1]*0.8);	
	        userFit.getUserParameters().setUpperLimit(1,pars0[1]*1.2);	
	        
	        //sigmaP
	        userFit.getUserParameters().setLowerLimit(2,pars0[2]*0.5);	
	        userFit.getUserParameters().setUpperLimit(2,pars0[2]*1.5);	
	        
	        //sigmaM
	        userFit.getUserParameters().setLowerLimit(3,pars0[3]*0.5);	
	        userFit.getUserParameters().setUpperLimit(3,pars0[3]*1.5);	
	        
	        
		   	//for(int i=0;i<pars0.length;i++)
		   	//	System.out.println(" i="+i+" par0="+pars0[i]);
	        
		   	userFit.doFit(f);

		   	double[] pars=userFit.getParametersArray();
		   	
		   	//central, sigmaP, sigmaM
		   	for(int i=1;i<=3;i++)
		   		pars[i]=pars[i]/factor;
		   	
		   	//for(int i=0;i<pars.length;i++)
		   	//	System.out.println(" i="+i+" par="+pars[i]);

			return pars;			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}	
}
