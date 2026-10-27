package ensdfparser.calc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.ensdf.XDX2SDS;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

/*
 * calculate weighted and unweighted average of a given list of data points
 * with uncertainty>0
 */
public class Average {
    private Vector<DataPoint> dataPoints=new Vector<DataPoint>();
    
    private Vector<Integer> outlierIndexesV=new Vector<Integer>();
    
    //all=good+bad
    private Vector<Integer> goodPointIndexesV=new Vector<Integer>();
    private Vector<Integer> badPointIndexesV=new Vector<Integer>();//dx<=0
    private Vector<Integer> nonLimitPointIndexesV=new Vector<Integer>();//dx<=0 but not limit
    
    //good=below+above (for weighted average only)
    private Vector<Integer> belowLimitIndexesV=new Vector<Integer>();//for data points below weight limits
    private Vector<Integer> aboveLimitIndexesV=new Vector<Integer>();//for data points above weight limits
    
    //for data points actually used in average
    //Note when there is <=1 datapoint above weight limit, all points will be used in average
    //In other cases, usedPointIndexsV=aboveLimitIndexesV
    //good=used+unused
    private Vector<Integer> usedPointIndexesV=new Vector<Integer>(); 
    private Vector<Integer> unusedPointIndexesV=new Vector<Integer>();                                                                  
    
    private Vector<Double> weights=new Vector<Double>();
    private double value,intError,extError,chi2,intErrorP,intErrorM,extErrorP,extErrorM;//for weighted average
    private double adoptedErrorP,adoptedErrorM;//for weighted average
    
    private boolean isGoodWeighted,isGoodUnweighted,isGoodUnweightedGoodDP;
    
    private double unwValue,unwError,unwChi2;//for unweighted average of all points (excluding limit)
    private double unwValueGoodDP,unwErrorGoodDP,unwChi2GoodDP;//for unweighted average of good data points with uncertainties
    
    private double weightLowerLimit;
    
    private int minWeightIndex=-1,maxWeightIndex=-1;
    
    private int errorLimit=35;
    
    private String defaultUnit="";
    
    public Average(){
        value=-1;
        intError=-1;
        intErrorP=-1;
        intErrorM=-1;
        extError=-1;
        extErrorP=-1;
        extErrorM=-1;
        chi2=-1;
        
        adoptedErrorP=-1;
        adoptedErrorM=-1;
        
        unwValue=-1;
        unwError=-1;
        unwChi2=-1;
        
        unwValueGoodDP=-1;
        unwErrorGoodDP=-1;
        unwChi2GoodDP=-1;
        
        isGoodWeighted=false;
        isGoodUnweighted=false;
        isGoodUnweightedGoodDP=false;
        
        weightLowerLimit=0;
        errorLimit=35;
        
        defaultUnit="";
    }

    public Average(Vector<DataPoint> dpsV,String option){
        this();
        setDataPoints(dpsV);
        calculate(option);
    }
    
    public Average(Vector<DataPoint> dpsV,double weightLimit,String option){
        this();
        setDataPoints(dpsV);
        setWeightLowerLimit(weightLimit);
        calculate(option);
    }
    
    public Average(Vector<DataPoint> dpsV){
        this(dpsV,"");
    }
    
    public Average(Vector<DataPoint> dpsV,double weightLimit){
        this(dpsV,weightLimit,"");
    }
    

    
    public void addDataPoint(DataPoint dp){
        checkUnit(dp);
        
        if(dp.dx()>0 || (dp.dxm()>0&&dp.dxp()>0)) {
        	goodPointIndexesV.addElement(dataPoints.size());
        	nonLimitPointIndexesV.addElement(dataPoints.size());
        }else {
            badPointIndexesV.addElement(dataPoints.size());      
            if(!Str.isLetters(dp.ds()))
            	nonLimitPointIndexesV.addElement(dataPoints.size()); 
        }
        dataPoints.addElement(dp);
        weights.addElement(0.0);

    }
            

    public void setDataPoints(Vector<DataPoint> dpsV){
        dataPoints.clear();
        dataPoints.addAll(dpsV);
        
        checkUnit(dpsV);

        for(int i=0;i<dpsV.size();i++){
            weights.addElement(0.0);
            
            DataPoint dp=dpsV.get(i);
            
            //if(dp.ds().contains("+52"))
            //	System.out.println("Average 120: dp: "+dp.s()+" "+dp.ds()+" dxp="+dp.dxp()+"  dxm="+dp.dxm()+" dx="+dp.dx()+" "+Str.isLetters(dp.ds()));

            if(dp.dx()>0 || (dp.dxm()>0&&dp.dxp()>0)) {
            	goodPointIndexesV.addElement(i);
            	nonLimitPointIndexesV.addElement(i);
            }else {
                badPointIndexesV.addElement(i);    
                if(!Str.isLetters(dp.ds())) {
                	nonLimitPointIndexesV.addElement(i);
                }
            }
        }
    }
    

    /*
     * check unit of each data point and make all units the same as the first one
     * NOTE that, that means some data point could be changed (value, uncertainty, unit),
     */
    public void checkUnit(Vector<DataPoint> dpsV) {
    	try {
        	String refUnit=dpsV.get(0).unit();
        	String tempUnit="";
            for(int i=0;i<dpsV.size();i++) {
            	DataPoint dp=dpsV.get(i);
            	String s=dp.unit();
            	if(!s.isEmpty() && tempUnit.isEmpty())
            		tempUnit=s;
            	
            	if(!EnsdfUtil.isHalflifeUnit(s))
            		continue;     
            	
            	refUnit=s;
            	if(dp.x()<0.1) {//e.g., 0.053 ps, then use fs as unit
            		String[] temp=EnsdfUtil.nextHalflifeUnit(s,"SMALLER");
            		refUnit=temp[0];
            	}
            	break;
            }
            	
            if(refUnit.isEmpty())
            	refUnit=tempUnit;
            
            defaultUnit=refUnit;
            
            for(int i=0;i<dpsV.size();i++)
                checkUnit(dpsV.get(i),refUnit);
    	}catch(Exception e) {
    		
    	}
       
    }
    public void checkUnit(DataPoint dp) {
    	checkUnit(dp,defaultUnit);
    }
    public void checkUnit(DataPoint dp,String refUnit) {
        if(dataPoints.size()==0)
            return;

        refUnit=refUnit.trim();
        
        String s=dp.s();
        String ds=dp.ds();
        String unit=dp.unit();
        
        if(unit.equalsIgnoreCase(refUnit) || refUnit.isEmpty() || unit.isEmpty())
            return;
        
        SDS2XDX s2x=convertUnit(s,ds,unit,refUnit);
        s=s2x.s();
        ds=s2x.ds();
        
        
        if(refUnit.length()>0 && s2x.ID().length()>0) {
            String name=s2x.ID();//set in convertUnit(), T{-1/2} or |G for original. Set only if unit type is changed
            try {
				String label2=EnsdfUtil.printValueAsComment(name, dp.s(),dp.ds(),dp.unit());
				dp.set2ndLabel(label2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
        
        dp.setS(s,ds);
        dp.setX(s2x.x(),s2x.dx());
        dp.setX(s2x.x(),s2x.dxu(),s2x.dxl());
        dp.setUnit(refUnit);
	
    }
    
    /*
     * convert (s,ds) pair in unit=oldUnit to newUnit
     */
    public SDS2XDX convertUnit(String s,String ds,String oldUnit,String refUnit) {

        SDS2XDX s2x=new SDS2XDX(s,ds);
        if(oldUnit.equalsIgnoreCase(refUnit))
        	return s2x;
        
        double factor1=EnsdfUtil.T12UnitMultiplier(oldUnit);//convert to second
        double factor2=EnsdfUtil.T12UnitMultiplier(refUnit);    
        
        //System.out.println("1  s0="+s+"  "+ds+" new s="+s2x.s()+"  "+s2x.ds());
		
        if(factor1==factor2 || factor1<=0 || factor2<=0)
            return s2x;

        boolean isTUnit1=EnsdfUtil.isHalflifeUnit(oldUnit);//not width unit
        boolean isTUnit2=EnsdfUtil.isHalflifeUnit(refUnit);//not width unit
        
        double R=1; 
        boolean isSameType=true;
    	
        
        if(isTUnit2) {
        	//if refUnit is half-life unit
        	R=factor1/factor2;
        }else {
        	//refUnit is width unit
        	R=factor2/factor1;
        }

        if(isTUnit1!=isTUnit2) {
        	isSameType=false;
        }
        
        SDS2XDX sxR=new SDS2XDX();
        sxR.setValues(R, 0);
        
        double logR=Math.abs(Math.log10(R));
        int n=(int)(logR+1.0E-3);
        
        int uncLimit=errorLimit;
        
    	String dsu=s2x.dsu();
    	String dsl=s2x.dsl();
    	
    	/*
		if(s.equals("21.5")) {
			System.out.println("#### "+s+" "+ds+" old= "+oldUnit+" "+refUnit
					+" factor1="+factor1+" factor2="+factor2+" R="+R+" n="+n+" logR="+logR);
		}
		*/
    	
        int unc1=errorLimit;
        int unc2=errorLimit;
        if(Str.isNumeric(dsu))
        	unc1=Integer.parseInt(dsu);
        if(Str.isNumeric(dsl))
        	unc2=Integer.parseInt(dsl);
        
        uncLimit=Math.max(unc1, unc2)+1;
        sxR.setErrorLimit(uncLimit);
        
        //String s1="",ds1="";
        if(Math.abs(n-logR)<=1.0E-3) {
            //units differ by orders of 10, e.g., fs to ps, 
            //check if redundant ending zero is added in the new (s,ds)
            //or existing ending zero is removed from old (s,ds)

        	s2x.setErrorLimit(uncLimit);
        	
            if(!Str.isNumeric(ds)) {
                
            	if(isSameType) {
            		s2x=s2x.multiply((float)R); 
            	}else {
            		s2x=sxR.divided(s2x);
            		
            	}
                //System.out.println("0  s0="+s+"  "+ds+" new s="+s2x.s()+"  "+s2x.ds()+" x="+s2x.x()+" dx="+s2x.dx()+" errorLimit="+errorLimit);

            }else{
                //int errorLimit=Integer.parseInt(ds)+1;
                //s2x.setErrorLimit(errorLimit);
            	if(isSameType) {
            		s2x=s2x.multiply((float)R); 
            	}else {
            		s2x=sxR.divided(s2x);
            	}
                //System.out.println("2  s0="+s+"  "+ds+" new s="+s2x.s()+"  "+s2x.ds()+" x="+s2x.x()+" dx="+s2x.dx()+" errorLimit="+errorLimit);                
            }
            //System.out.println("3  s0="+s+"  "+ds+" new s="+s2x.s()+"  "+s2x.ds()+" x="+s2x.x()+" dx="+s2x.dx()+" errorLimit="+errorLimit);
        }else {
            uncLimit=Math.max(uncLimit, errorLimit);          
        	s2x.setErrorLimit(uncLimit);
        	if(isSameType) {
        		s2x=s2x.multiply((float)R); 
        	}else {
        		sxR.setErrorLimit(uncLimit);
        		s2x=sxR.divided(s2x);
        	}
        }
        
        if(!isSameType) {
        	//set ID from the name of original type, not the converted type, to be used in checkUnit()
        	if(isTUnit1) {
        		s2x.setID("T{-1/2}");
        	}else {
        		s2x.setID("|G");
        	}
        }
        //System.out.println("2  s0="+s+"  "+ds+" new s="+s2x.s()+"  "+s2x.ds()+" x="+s2x.x()+" dx="+s2x.dx()+" "+s2x.dxu()+" errorLimit="+errorLimit);
        
        return s2x;
    }
    
    
    public void setWeightLowerLimit(double limit){weightLowerLimit=limit;}
    
    public void calculate(){
        calculate("");
    }
    
    public void calculate(String option){
        
        weightedAverage(option);
        unweightedAverage();
        
    }
    
    /*
     * For symmetric uncertainty dx, weight=1/(dx*dx)
     * For asymetric uncertainty +dxp-dxm, using mode 1
     * in "Asymmetric Errors" by R. Barlow, PHYSTAT2003
     * given by 1/V, where V is variance of the datapoint
     *   V=(dxp+dxm)^2/4+(1-2/pi)*(dxp-dxm)^2/4
     *   
     * In Filip's code for option=="1"
     *   V=(dxp*dxp+dxm*dxm)/2
     *
     * In AveLib program for option=="2"
     * where
     *   V=dxp*dxm+(1-2/pi)*(dxp-dxm)^2
     */
    public double findWeight(DataPoint dp,String option){
        return findWeight(dp.x(),dp.dxp(),dp.dxm(),option);
    }
    
    public double findWeight(double x,double dxp,double dxm,String option){
        if(dxp<0) dxp=0;
        if(dxm<0) dxm=0;
        
        double V=0;
        if(option.equals("1"))
            V=(Math.pow(dxp,2)+Math.pow(dxm,2))/2.0;
        else if(option.equals("2"))
            V=dxp*dxm+(1-2.0/Math.PI)*Math.pow(dxp-dxm,2);
        else
            V=Math.pow(dxp+dxm,2)/4+(1-2.0/Math.PI)*Math.pow(dxp-dxm,2)/4;
        
        if(V==0) 
            return 0;
        
        return 1.0/V;
    }
    
    //for weighted average
    public int findMaxNDigitsAfterDot() {
        int maxNDigitsAfterDot=0;
        String s="";
        boolean hasS=false;
        for(int i=0;i<usedPointIndexesV().size();i++){
            int index=usedPointIndexesV().get(i);
            DataPoint dp=dataPointsV().get(index);
            
            int n=0;
            s=dp.s().trim();
            if(!s.isEmpty()) {
            	hasS=true;
                n=Str.findNDigitsAfterDot(s);
                
                if(n>maxNDigitsAfterDot)
                    maxNDigitsAfterDot=n;
            }

        }
        
        if(!hasS) {//use average result based on errorLimit
            for(int i=0;i<usedPointIndexesV().size();i++){
                int index=usedPointIndexesV().get(i);
                DataPoint dp=dataPointsV().get(index);
                
                XDX2SDS xs=new XDX2SDS(dp.x(),dp.dxp(),dp.dxm(),errorLimit);
                int n=0;
                s=xs.s();
                n=Str.findNDigitsAfterDot(s);
                
                if(n>maxNDigitsAfterDot)
                    maxNDigitsAfterDot=n;

            }
        }
        
        return maxNDigitsAfterDot;
    }
    
    private void clearWeights() {
        try {
            int n=weights.size();
            weights.clear();
            for(int i=0;i<n;i++)
                weights.add(0.0);
        }catch(Exception e) {}
        
    }
    
    private void setWeight(int index, double wi) {
        try {
            weights.insertElementAt(wi, index);
            weights.remove(index+1);
        }catch(Exception e) {}
    }
    
    /*
     * use Model 1 in R. Barlow, "Asymmetric Errors"
     *
     * For symmetric uncertainty dx, weight=1/(dx*dx)
     * For asymetric uncertainty +dxp-dxm, using mode 1
     * in "Asymmetric Errors" by R. Barlow, PHYSTAT2003
     * given by 1/V, where V is variance of the datapoint
     *   V=(dxp+dxm)^2/4+(1-2/pi)*(dxp-dxm)^2/4
     *   
     * In Filip's code for option=="1"
     *   V=(dxp*dxp+dxm*dxm)/2
     *
     * In AveLib program for option=="2"
     * where
     *   V=dxp*dxm+(1-2/pi)*(dxp-dxm)^2
     *
     */ 
    private void weightedAverage(String option){
        double valSum=0f;
        int n=goodPointIndexesV.size();
        
        if(n<1){
            return;
        }
        
        double[] val=new double[n];
        double[] offset=new double[n];//offset=0 for symmetric uncertainty. 
        double[] wi=new double[n];
        double[] wip=new double[n];//weight of each point
        double[] wim=new double[n];
        double wt=0;//total weight
        double wtp=0,wtm=0,wtp1=0,wtm1=0;// for sum of upper and lower, respectively; used to calculate intErrorP and intErrorM
        double tp=0,tm=0,tp1=0,tm1=0;

        
        if(option.equals("1") || option.equals("2")){
            Arrays.fill(offset,0.0);
        }else{
            for(int i=0;i<n;i++){
                int index=goodPointIndexesV.get(i);
                DataPoint dp=dataPoints.get(index);
                
                offset[i]=(dp.dxp()-dp.dxm())/Math.sqrt(2*Math.PI);
            }
        }

        for(int i=0;i<n;i++){
            int index=goodPointIndexesV.get(i);
            DataPoint dp=dataPoints.get(index);
            
            val[i]=dp.x();
                    
            wi[i]=findWeight(dp,option);
            
            if(dp.dxp()>0)
            	wip[i]=1.0/Math.pow(dp.dxp(),2);
            else
            	wip[i]=0;
            
            if(dp.dxm()>0)
            	wim[i]=1.0/Math.pow(dp.dxm(),2);
            else
            	wim[i]=0;
            
            //valSum+=(val[i]-offset[i])*wi[i];
            valSum+=val[i]*wi[i];//changed in 9/2/2020
            
            wt+=wi[i];
            
            tp+=Math.pow(dp.dxp(),2);
            tm+=Math.pow(dp.dxm(),2);
            
            try{
                wtp+=1.0/Math.pow(dp.dxp(),2);
            }catch(Exception e){}

            try{
                wtm+=1.0/Math.pow(dp.dxm(),2);
            }catch(Exception e){}       
        }
    
        //find outliers
        int count=0;
        double wt1=0;
        double valSum1=0;
        double minWt=1000,maxWt=0;
        
        minWeightIndex=-1;
        maxWeightIndex=-1;
        
        //note that the size and indexing of "weights" match "dataPoints"
        //So DO NOT call clear() on "weights"
        clearWeights();
        
        belowLimitIndexesV.clear();
        aboveLimitIndexesV.clear();
        for(int i=0;i<n;i++){
            int index=goodPointIndexesV.get(i);
            double weight=wi[i]/wt;
            
            if(weight>0 && weight>maxWt) {
                maxWt=weight;
                maxWeightIndex=index;
            }
            if(weight>0 && weight<minWt) {
                minWt=weight;
                minWeightIndex=index;
            }
            
            setWeight(index,weight);
            
            if(weight<weightLowerLimit){
                belowLimitIndexesV.add(index);
                continue;
            }
                
            aboveLimitIndexesV.add(index);
            wt1+=wi[i];
            //valSum1+=(val[i]+offset[i])*wi[i];
            valSum1+=val[i]*wi[i];//changed in 9/2/2020
            
            
            DataPoint dp=dataPoints.get(index);
            tp1+=Math.pow(dp.dxp(),2);
            tm1+=Math.pow(dp.dxm(),2);
            
            try{
                wtp1+=1.0/Math.pow(dp.dxp(),2);
            }catch(Exception e){}

            try{
                wtm1+=1.0/Math.pow(dp.dxm(),2);
            }catch(Exception e){}
            
            count++;
        }
        
        usedPointIndexesV.clear();
        unusedPointIndexesV.clear();    
        usedPointIndexesV.addAll(aboveLimitIndexesV);
        unusedPointIndexesV.addAll(belowLimitIndexesV);
        
        
        //If all values or (all-1) have weight<weight_limit and then average all
        if(count<=1){
            usedPointIndexesV.clear();
            unusedPointIndexesV.clear();
            usedPointIndexesV.addAll(goodPointIndexesV);            
            count=n;
        }else if(count<n){
            valSum=valSum1;
            wt=wt1;
            wtp=wtp1;
            wtm=wtm1;
            tp=tp1;
            tm=tm1;
        }
        //System.out.println(valSum1+"  "+(valSum1/wt1)+" "+count+" "+n);
        
        if(wt==0)
            return;
        
        value=valSum/wt;
        intError=(double) Math.sqrt(1.0/wt);
        
        if(option.equals("1")){
            intErrorP=intError*Math.sqrt(2*tp/(tp+tm));
            intErrorM=intError*Math.sqrt(2*tm/(tp+tm));
        }else if(option.equals("2")){
            intErrorP=(double) Math.sqrt(1.0/wtp);
            intErrorM=(double) Math.sqrt(1.0/wtm);
        }else{
            intErrorP=(double) Math.sqrt(1.0/wtp);
            intErrorM=(double) Math.sqrt(1.0/wtm);
        }
    
                
        extError=0;
        extErrorP=0;
        extErrorM=0;

        
        //System.out.println(valSum1+"  "+(valSum1/wt1));
        //System.out.println(valSum+"  "+(valSum/wt));
        //System.out.println(value);
        
        for(int i=0;i<n;i++){
            if(unusedPointIndexesV.contains(i))
                continue;
            
 
            double error=(val[i]-value);
            extError+=wi[i]/wt*error*error;
            
            extErrorP+=wip[i]/wtp*error*error;
            extErrorM+=wip[i]/wtm*error*error;
        }
        
        if(count>1) {
            chi2=extError*wt/(count-1);
            extError=(double) Math.sqrt(extError/(count-1));    
            extErrorP=(double) Math.sqrt(extErrorP/(count-1)); 
            extErrorM=(double) Math.sqrt(extErrorM/(count-1)); 
        }

                
        isGoodWeighted=true;
        
        //adopted uncertainty       
        double minErrM=1E6,minErrP=1E6;
        try {
            for(int i=0;i<usedPointIndexesV().size();i++){
                int index=usedPointIndexesV().get(i);
                DataPoint dp=dataPointsV().get(index);
                
                double dxp=1E6,dxm=1E6;
                if(dp.dxp()>0 && dp.dxm()>0) {
                    dxp=dp.dxp();
                    dxm=dp.dxm();
                }else if(dp.dx()>0) {
                    dxp=dp.dx();
                    dxm=dp.dx();
                }
                
                if(dxp<minErrP)
                    minErrP=dxp;
                
                if(dxm<minErrM)
                    minErrM=dxm;
            }
        }catch(Exception e) {}
            
        adoptedErrorM=Math.max(extError, intErrorM);
        adoptedErrorP=Math.max(extError, intErrorP);
        
        adoptedErrorM=Math.max(adoptedErrorM, minErrM);
        adoptedErrorP=Math.max(adoptedErrorP, minErrP);
        
        //debug();

    }
    
    /*
     * note that chi-square and uncertainty (variance) of unweighted average
     * has nothing to do with the uncertainties of data points, which are  
     * used as weights only for weighted average.
     * e.g. two sets of data points with the same values but different uncertainies
     * (no matter symmetric or asymmetric) have the same unweighted average value
     * and chi-square
     */
    private void unweightedAverage(){
    	unweightedAverageAllPoints();
    	unweightedAverageGoodPoints();//points with uncertainties
    }
    private void unweightedAverageAllPoints(){
        double valSum=0f;
        int n=dataPoints.size();
        
        if(n<1)
            return;
        
        Vector<DataPoint> tempV=new Vector<DataPoint>();
        
        for(DataPoint dp:dataPoints){        	
        	if(dp.x()!=0 && !Str.isLetters(dp.ds())) {
                valSum+=dp.x();
                tempV.add(dp);
        	}
        }
        
        n=tempV.size();
        if(n<1)
            return;
        
        unwValue=valSum/n;
        unwError=0f;
        unwChi2=0f;
 
        for(DataPoint dp:tempV){
            double error=(dp.x()-unwValue);
            error=error*error;
            
            unwError+=error/n;
            unwChi2+=error;
        }
        
        if(n>1) {
            unwError=(double) Math.sqrt(unwError/(n-1));
            unwChi2=unwChi2/(n-1);              
        }
            
        isGoodUnweighted=true;
    }
    
    private void unweightedAverageGoodPoints(){
        double valSum=0f;
        int n=goodPointIndexesV.size();
        
        if(n<1)
            return;
        
        for(int i:goodPointIndexesV){
            DataPoint dp=dataPoints.get(i);
            valSum+=dp.x();
        }
            
        unwValueGoodDP=valSum/n;
        unwErrorGoodDP=0f;
        unwChi2GoodDP=0f;
 
        for(int i:goodPointIndexesV){
            DataPoint dp=dataPoints.get(i);
            double error=(dp.x()-unwValueGoodDP);
            error=error*error;
            
            unwErrorGoodDP+=error/n;
            unwChi2GoodDP+=error;
        }
        if(n>1) {
            unwErrorGoodDP=(double) Math.sqrt(unwErrorGoodDP/(n-1));
            unwChi2GoodDP=unwChi2GoodDP/(n-1);              
        }
    
        
        isGoodUnweightedGoodDP=true;
    }
    
    //if the weighted average is equal or very close to an input data point
    //must be called after weightedAverage()
    @SuppressWarnings("unused")
	public boolean isAvgEqualToAnInput(int errorLimit) {
        
        //check if the final value and uncertainty are equal or very close to the input data point with smallest uncertainty (max weight) 
        int index=this.maxWeightIndex;
        double dpVal=dataPointsV().get(index).x();
        double dpUncP=dataPointsV().get(index).dxp();
        double dpUncM=dataPointsV().get(index).dxm();
        
        double maxIntError=Math.max(intErrorM,intErrorP);
        double maxExtError=Math.max(extErrorM,extErrorP);
        double avgError=-1;
        
        avgError=Math.max(maxIntError, extError);
        //avgError=Math.max(maxIntError, maxExtError);
        
        XDX2SDS x2s=new XDX2SDS(value,adoptedErrorP,adoptedErrorM,errorLimit);
        int maxNDigitsAfterDot=findMaxNDigitsAfterDot();
        
        SDS2XDX s2x=SDS2XDX.checkUncertainty(x2s.s(), x2s.ds(), maxNDigitsAfterDot,errorLimit);
        double valDiff=Math.abs(dpVal-s2x.x());
        double avgErrP=s2x.dxu();
        double avgErrM=s2x.dxl();

        double dpUnc=Math.min(dpUncP, dpUncM);

        //System.out.println("AvrageReport line 595: avg: dpVal="+dpVal+" s2x.x="+s2x.x()+"  value="+value+" unc(ext)="+extError+" unc(int)="+intError
        //		+"\n x2s: "+x2s.s()+" "+x2s.ds()+" valDiff="+valDiff+" avgErrP="+avgErrP+" avgErrM="+avgErrM
        //		+"\n dpUnc="+dpUnc+" dpUncP="+dpUncP+" dpUncM="+dpUncM+" maxNDigitsAfterDot="+maxNDigitsAfterDot+" errorLimit="+errorLimit);

        if(valDiff<(dpUnc/10) && avgErrP<=dpUncP && avgErrM<=dpUncM) {                  
            return true;
        } 
        
        return false;
    }
    
    //format weighted average result to ENSDF format
    //must be called after weighedaverage()
    public SDS2XDX formatWeighted(int errorLimit) {
        XDX2SDS x2s=new XDX2SDS(value,adoptedErrorP,adoptedErrorM,errorLimit);
        int maxNDigitsAfterDot=findMaxNDigitsAfterDot();        

        return SDS2XDX.checkUncertainty(x2s.s(), x2s.ds(), maxNDigitsAfterDot,errorLimit);

    }
    
    //format unweighted average result to ENSDF format
    //must be called after unweighedaverage()
    public SDS2XDX formatUnweighted(int errorLimit) {
        XDX2SDS x2s=new XDX2SDS(this.unwValue,this.unwError,errorLimit);
        int maxNDigitsAfterDot=findMaxNDigitsAfterDot();        

        return SDS2XDX.checkUncertainty(x2s.s(), x2s.ds(), maxNDigitsAfterDot,errorLimit);
    }
    public SDS2XDX formatUnweightedGoodDP(int errorLimit) {
        XDX2SDS x2s=new XDX2SDS(this.unwValueGoodDP,this.unwErrorGoodDP,errorLimit);
        int maxNDigitsAfterDot=findMaxNDigitsAfterDot();        

        return SDS2XDX.checkUncertainty(x2s.s(), x2s.ds(), maxNDigitsAfterDot,errorLimit);
    }
    
    public double comparedWeightedUnc(Average b,int errorLimit) {
        if(this==b)
            return 0;
        
        //System.out.println("  1: "+a.value+"  extE="+a.extError+" intErrM="+a.intErrorM+" intErrP="+a.intErrorP);
        //System.out.println("  2: "+value+"  extE="+extError+" intErrM="+intErrorM+" intErrP="+intErrorP);

        SDS2XDX s2x=this.formatWeighted(errorLimit);
        double errM1=s2x.dxl();
        double errP1=s2x.dxu();

        s2x=b.formatWeighted(errorLimit);
        double errM2=s2x.dxl();
        double errP2=s2x.dxu();
        
        //System.out.println("         errM1: "+errM1+"  errP1="+errP1);
        //System.out.println("         errM2: "+errM2+"  errP2="+errP2);
        double err1=(errM1+errP1)/2;
        double err2=(errM2+errP2)/2;
        
        int p1=power(errM1,errP1,errorLimit);
        int p2=power(errM2,errP2,errorLimit);
        
        int p=Math.min(p1, p2);
        
        //factor to bring value and error to integer for resulting integer error less than and close to limit
        double factor=Math.pow(10, p);
        
        //the default minimum difference between the two resulting integer values or errors for them to be considered equal when uncertainty itself is large 
        //For example, if uncertainty limit (errorLimit)=100, then minGap=2, so integer value and uncertainty in 1222(22) and 1220(20) considered
        double defaultMinGap=Math.log10(errorLimit);
    
        //if difference between two uncertainties is greater 10% of the smaller one, they are considered different
        double minGapErrM=Math.min(err1,errM2)*factor/10;
        double minGapErrP=Math.min(errP1,errP2)*factor/10;

        minGapErrM=Math.min(defaultMinGap, minGapErrM);
        minGapErrP=Math.min(defaultMinGap, minGapErrP);
        
        double minGapErr=Math.min(minGapErrM, minGapErrP);
        
        if(Math.abs(err1-err2)<=minGapErr) {
            if(Math.abs(errM1-errM2)<=minGapErrM)
                return 0;
            
            return errM1-errM2;
        }
                
        return err1-err2;
    }
    
    //find the power which can bring errP and errM witin in the integer limit by 
    //multiplying 10^power
    private int power(double errP,double errM,int limit) {
        try {
            int p1=(int) Math.floor(Math.log10(limit/errP));
            int p2=(int) Math.floor(Math.log10(limit/errM));
            return Math.min(p1, p2);
        }catch(Exception e) {}
        
        return 0;
    }
    
    public boolean isEqualWeighted(Average a,int errorLimit) {
        if(this==a)
            return true;
        
        //System.out.println("  1: "+a.value+"  extE="+a.extError+" intErrM="+a.intErrorM+" intErrP="+a.intErrorP);
        //System.out.println("  2: "+value+"  extE="+extError+" intErrM="+intErrorM+" intErrP="+intErrorP);

        SDS2XDX s2x=this.formatWeighted(errorLimit);
        double errM1=s2x.dxl();
        double errP1=s2x.dxu();

        s2x=a.formatWeighted(errorLimit);
        double errM2=s2x.dxl();
        double errP2=s2x.dxu();

        
        if(errM1<=0 || errM2<=0 || errP1<=0 || errP2<=0)
            return false;
        
        //System.out.println("         errM1: "+errM1+"  errP1="+errP1);
        //System.out.println("         errM2: "+errM2+"  errP2="+errP2);
        
        int p1=power(errM1,errP1,errorLimit);
        int p2=power(errM2,errP2,errorLimit);
        
        int p=Math.min(p1, p2);
        
        //factor to bring value and error to integer for resulting integer error less than and close to limit
        double factor=Math.pow(10, p);
        
        //the default minimum difference between the two resulting integer values or errors for them to be considered equal when uncertainty itself is large 
        //For example, if uncertainty limit (errorLimit)=100, then minGap=2, so integer value and uncertainty in 1222(22) and 1220(20) considered
        double defaultMinGap=Math.log10(errorLimit);
    
        //if difference between two uncertainties is greater 10% of the smaller one, they are considered different
        double minGapErrM=Math.min(errM1,errM2)*factor/10;
        double minGapErrP=Math.min(errP1,errP2)*factor/10;

        minGapErrM=Math.min(defaultMinGap, minGapErrM);
        minGapErrP=Math.min(defaultMinGap, minGapErrP);
        
        double minGapVal=Math.min(minGapErrM, minGapErrP);
        
        double val1=Math.round(a.value()*factor);
        double val2=Math.round(value*factor);
        
        if(Math.abs(val1-val2)>minGapVal)
            return false;
        
        //for uncertainty
        errM1=Math.round(errM1*factor);
        errM2=Math.round(errM2*factor);
        if(Math.abs(errM1-errM2)>minGapErrM)
            return false;
        
        errP1=Math.round(errP1*factor);
        errP2=Math.round(errP2*factor);
        if(Math.abs(errP1-errP2)>minGapErrP)
            return false;
        
        //System.out.println("  ***EQUAL***");
        
        return true;
    }
    
    public boolean isEqualUnWeighted(Average a,int errorLimit) {
        if(this==a)
            return true;
        
        SDS2XDX s2x=this.formatUnweighted(errorLimit);
        double err1=s2x.dx();

        s2x=a.formatUnweighted(errorLimit);
        double err2=s2x.dx();
    
        if(err1<=0 || err2<=0)
            return false;

        
        int p1=power(err1,err1,errorLimit);
        int p2=power(err2,err2,errorLimit);
        
        
        int p=Math.min(p1, p2);
        double factor=Math.pow(10, p);
        
        double defaultMinGap=Math.log10(errorLimit);
        
        //if difference between two uncertainties is greater 10% of the smaller one, they are considered different
        double minGapErr=Math.min(err1,err2)*factor/10;

        minGapErr=Math.min(defaultMinGap, minGapErr);
        
        double val1=Math.round(a.unwValue*factor);
        double val2=Math.round(unwValue*factor);
        
        if(Math.abs(val1-val2)>minGapErr)
            return false;
                
        err1=Math.round(err1*factor);
        err2=Math.round(err2*factor);
        
        if(Math.abs(err1-err2)>minGapErr)
            return false;
        
        return true;
    }
    
    public boolean isEqualUnWeightedGoodDP(Average a,int errorLimit) {
        if(this==a)
            return true;
        
        SDS2XDX s2x=this.formatUnweightedGoodDP(errorLimit);
        double err1=s2x.dx();

        s2x=a.formatUnweightedGoodDP(errorLimit);
        double err2=s2x.dx();
    
        if(err1<=0 || err2<=0)
            return false;

        
        int p1=power(err1,err1,errorLimit);
        int p2=power(err2,err2,errorLimit);
        
        
        int p=Math.min(p1, p2);
        double factor=Math.pow(10, p);
        
        double defaultMinGap=Math.log10(errorLimit);
        
        //if difference between two uncertainties is greater 10% of the smaller one, they are considered different
        double minGapErr=Math.min(err1,err2)*factor/10;

        minGapErr=Math.min(defaultMinGap, minGapErr);
        
        double val1=Math.round(a.unwValueGoodDP*factor);
        double val2=Math.round(unwValueGoodDP*factor);
        
        if(Math.abs(val1-val2)>minGapErr)
            return false;
                
        err1=Math.round(err1*factor);
        err2=Math.round(err2*factor);
        
        if(Math.abs(err1-err2)>minGapErr)
            return false;
        
        return true;
    }
    
    public boolean isGoodWeighted(){return isGoodWeighted;}
    public boolean isGoodUnweighted(){return isGoodUnweighted;}
    public boolean isGoodUnweightedGoodDP(){return isGoodUnweightedGoodDP;}
    //public boolean isAvgEqualToAnInput() {return isAvgEqualToAnInput;}
    
    public int nDataPoints(){return dataPoints.size();}
    
    public Vector<DataPoint> dataPointsV(){return dataPoints;}
    
    public Vector<Integer> outlierIndexesV(){return outlierIndexesV;}
    public Vector<Integer> goodPointIndexesV(){return goodPointIndexesV;}
    public Vector<Integer> badPointIndexesV(){return badPointIndexesV;}
    public Vector<Integer> nonLimitPointIndexesV(){return nonLimitPointIndexesV;}
    
    public Vector<Integer> belowLimitIndexesV(){return belowLimitIndexesV;}
    public Vector<Integer> aboveLimitIndexesV(){return aboveLimitIndexesV;}
    public Vector<Integer> usedPointIndexesV(){return usedPointIndexesV;}
    public Vector<Integer> unusedPointIndexesV(){return unusedPointIndexesV;}
    
    public int minWeightIndex() {return minWeightIndex;}
    public int maxWeightIndex() {return maxWeightIndex;}
    
    public double minWeight() {return getWeight(minWeightIndex);}
    public double maxWeight() {return getWeight(maxWeightIndex);}
    
    public DataPoint minWeightDataPoint() {try {return dataPoints.get(minWeightIndex);}catch(Exception e) {return null;}}
    public DataPoint maxWeightDataPoint() {try {return dataPoints.get(maxWeightIndex);}catch(Exception e) {return null;}}
    
    public Vector<Double> weightsV(){return weights;}

    public double getX(int i){return dataPoints.get(i).x();}
    public double getDX(int i){return dataPoints.get(i).dx();}
    public String getS(int i){return dataPoints.get(i).s();}
    public String getDS(int i){return dataPoints.get(i).ds();}
    public String getLabel(int i){return dataPoints.get(i).label();}
    
    //for weighted average
    public double value(){return value;}
    public double intError(){return intError;}
    public double intErrorP(){return intErrorP;}//unc+
    public double intErrorM(){return intErrorM;}//unc-
    public double extError(){return extError;}
    public double extErrorP(){return extErrorP;}//unc+
    public double extErrorM(){return extErrorM;}//unc-
    public double chi2(){return chi2;}
    public double getWeight(int i){try{return weights.get(i).doubleValue();}catch(Exception e) {return 0;}}
    
    public double adoptedErrorM() {return adoptedErrorM;}
    public double adoptedErrorP() {return adoptedErrorP;}
    
    //for unweighted average
    public double unweightedValue(){return unwValue;}
    public double unweightedError(){return unwError;}
    public double unweightedChi2(){return unwChi2;}

    public double unweightedValueGoodDP(){return unwValueGoodDP;}
    public double unweightedErrorGoodDP(){return unwErrorGoodDP;}
    public double unweightedChi2GoodDP(){return unwChi2GoodDP;}
            
    public void setErrorLimit(int limit) {errorLimit=limit;}
    public int errorLimit() {return errorLimit;}
    
    public String defaultUnit() {return defaultUnit;}
    
    public void debug(){
        System.out.println("   weighted ="+value());
        System.out.println("   intError ="+this.intError);
        System.out.println("   extError ="+this.extError);
        System.out.println("   intErrorP="+this.intErrorP);
        System.out.println("   intErrorM="+this.intErrorM);
        System.out.println("   extErrorP="+this.extErrorP);
        System.out.println("   extErrorM="+this.extErrorM);
        System.out.println("   chi2/ndf ="+this.chi2);
        System.out.println("\n");
        System.out.println("  unweighted="+this.unwValue);
        System.out.println("      error ="+this.unwError);  
        System.out.println("   chi2/ndf ="+this.unwChi2);
        System.out.println("\n");
        System.out.println("  unweightedGoodDP="+this.unwValueGoodDP);
        System.out.println("            error ="+this.unwErrorGoodDP);  
        System.out.println("         chi2/ndf ="+this.unwChi2GoodDP);
    }
}
