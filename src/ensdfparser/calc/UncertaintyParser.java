package ensdfparser.calc;

import java.util.Vector;

import ensdfparser.ensdf.Record;
import ensdfparser.nds.util.Str;

/*
 * parse non-numerical uncertainty string, like "LT","AP","CA","SY"
 * and assign an estimated numerical uncertainty by using the numerical 
 * value multiplied by a factor that can be user-defined. 
 */
public class UncertaintyParser {

    /*
     *    ds          dri
     *    empty       20%*ri
     *    AP,SY,CA    50%*ri
     *    LT or LE    50%*ri, ri changed to ri*50
     *    GT or GE    if ri/sum(ri)<10%, set dri=0
     *                if ri/sum(ri)>10%, set dri=ri*100
     */
    
    //factors for non-numerical uncertainty string
    private static double factorForEmpty=0.2f;
    private static double factorForAP=0.5f;
    private static double factorForCA=0.5f;
    private static double factorForSY=0.5f;
    private static double factorForLT=0.5f;
    private static double factorForGT=1E6f;
    
    private static double factorPForLT=0.0f;//unc(+)
    private static double factorMForLT=1.0f-1E-6f;//unc(-)=val-val*1E-6
    
    private static double factorPForGT=1E6f;//unc(+)=val*1E6
    private static double factorMForGT=0.0f;
    
    private static XDX originalXDX=null;
    private static XDX parsedXDX=null;
    private static XDX parsedXDXNoAdjust=null;//central value remains original for any non-numerical unertainty, without adjustment
    
    private static boolean toParse=true;
    private static boolean isGoodX=false;
       
    public interface UncParser {XDX get(String s,String ds);}
    
    public static void reset() {
        factorForEmpty=0.2f;
        factorForAP=0.5f;
        factorForCA=0.5f;
        factorForSY=0.5f;
        factorForLT=0.5f;
        factorForGT=1000000;    	
        
        factorPForLT=0.0f;
        factorMForLT=1E-6f;
        
        factorPForGT=1E6f;
        factorMForGT=0.0f;
    }
    /*
     * drius: string of upper uncertainty
     * drils: string of lower uncertainty
     * NOTE: uncertainty string is for real values
     * Option: =A for set x&dx for absolute intensities given as lower limit xl.
     *          since absolute intensity has upper limit=100, x&dx can be set as
     *            x=(xl+100)/2, dx=(100-xl)/2
     *         =U for set x&dx for input intensities x0&dx0 indicated as undivided intensities
     *            then x=(x0+dx0)/2, dx=x
     */
    public static XDX parse(String s0,String dsu0,String dsl0,char option){
        
        double x0=-1,dxu0=-1,dxl0=-1;//store original value of s,ds
        double x=-1,dxu=-1,dxl=-1;
        double x1=-1,dxu1=-1,dxl1=-1;
        
        String s=s0.trim().toUpperCase();
        String dsu=dsu0.trim().toUpperCase();
        String dsl=dsl0.trim().toUpperCase();
        
        isGoodX=false;
        toParse=true;
        
        option=(option+"X").toUpperCase().trim().charAt(0);
        
        try{
            x=Double.parseDouble(s);
            
            x0=x;
            x1=x;
            
            isGoodX=true;   
            dxu=Double.parseDouble(dsu);
            dxl=Double.parseDouble(dsl);
            
            dxu0=dxu;
            dxl0=dxl;
            
            if(option=='U') {
            	x=(x0+dxu0)/2;
            	dxu=x;
            	dxl=x;
            }
            	
            x1=x;
            dxu1=dxu;
            dxl1=dxl;
            
            toParse=false;
            
        }catch(NumberFormatException e){
            if(!isGoodX || (!dsu.equals(dsl)&&!dsu.isEmpty()&&!dsl.isEmpty()))
                toParse=false;
        }
        
        if(toParse){
            String ds=dsu;
            
            boolean hasSet1=false;
            //if reaching here, ds is non-numerical, eg, AP,LT or LE, GT or GE, SY, CA, or empty
            if(ds.isEmpty()){
                if(factorForEmpty>=0)
                	dxu=factorForEmpty*x;
            }else if(ds.equals("AP") || ds.equals("|?")){
                dxu=factorForAP*x;
            }else if(ds.equals("LT") || ds.equals("LE") || ds.equals("<") ||ds.equals("|<") || ds.equals("<=")){
                if(factorForLT>=1)//if factor>1, force to set it to be 0.5
                    factorForLT=0.5f;
                
                if(x0<0 && s0.startsWith("-")) {
                	String s1=s0.substring(1);
                	String ds1="G"+ds.substring(1);
                	XDX xd=parse(s1,ds1,ds1,option);
                	
                	x=-xd.x;
                	dxu=xd.dxl;
                	dxl=xd.dxu;
                	
                	x1=-parsedXDXNoAdjust.x;
                	dxu1=parsedXDXNoAdjust.dxl;
                	dxl1=parsedXDXNoAdjust.dxu;
  
                }else {
                    dxu=factorForLT*x;
                    x=x-dxu;
                    dxl=x*0.9999999f;
                    
                    
                    //System.out.println(x+"  "+dxu+"  "+dxl);
                    
                    x1=x0;
                    dxu1=factorPForLT*x1;
                    dxl1=factorMForLT*x1;
                }
   
                hasSet1=true;
            }else if(ds.equals("GT") || ds.equals("GE") || ds.equals(">") || ds.equals("|>") || ds.equals(">=")){
                if(x0<0 && s0.startsWith("-")) {
                	String s1=s0.substring(1);
                	String ds1="L"+ds.substring(1);
                	XDX xd=parse(s1,ds1,ds1,option);
                	
                	x=-xd.x;
                	dxu=xd.dxl;
                	dxl=xd.dxu;
                	
                	x1=-parsedXDXNoAdjust.x;
                	dxu1=parsedXDXNoAdjust.dxl;
                	dxl1=parsedXDXNoAdjust.dxu;
  
                }else {
                	if(option=='A' && x0<100) {
                		//x=(x0+100)/2;
                		//dxu=(100-x0)/2;
                		dxu=100-x0;
                		dxl=0;
                	}else {
                        dxu=factorForGT*x;
                        dxl=0;
                	}

                    x1=x0;
                    dxu1=factorPForGT*x1;
                    dxl1=factorMForGT*x1;
                }
                
                hasSet1=true;
            }else if(ds.equals("SY"))
                dxu=factorForSY*x;
            else if(ds.equals("CA"))
                dxu=factorForCA*x;          
            
            if(dxl<0)
                dxl=dxu;
            else if(dxl>0 && dxl==x)
                dxl=x*0.999999f;
            
            if(!hasSet1) {
            	dxu1=dxu;
            	dxl1=dxl;
            }
            
        }
        

        originalXDX=new XDX(x0,dxu0,dxl0);
        parsedXDX=new XDX(x,dxu,dxl);
        parsedXDXNoAdjust=new XDX(x1,dxu1,dxl1);
        
        ///System.out.println(" s="+s+" dsu="+dsu+"$x="+x+"  "+dxu+"  "+dxl+"  "+parsedXDX.x);
        
        return parsedXDX;
    }
     
	/*
	 * The original non-numerical uncertainty could have been adjusted to numerical values
	 * based on parsing rules. This function reparses x and dx with the original non-numerical
	 * uncertainty to its original form 
       param: parsedX, parsedDX---adjusted value and its uncertainty
       param: originalDS       ---the original non-numerical uncertainty
	 */
    public static XDX reparseXDX(double parsedX,double parsedDX,String originalDS) {
        XDX AI=new XDX(parsedX,parsedDX);
        
        originalDS=originalDS.trim().toUpperCase();
        
        if(!Str.isNumeric(originalDS) && !originalDS.contains("+") && !originalDS.contains("-")) {
            AI.dsl=originalDS;
            AI.dsu=originalDS;
            AI.dxl=-1;
            AI.dxu=-1;
            
            
            //if(Math.abs(parsedX-parsedDX)<1E-4*Math.abs(parsedX) && originalDS.startsWith("L")) {//dai adjusted for original DS=LT or LE, ai=dai=(orginal ai)/2
            if(parsedX>=0 && parsedDX>=0 && originalDS.startsWith("L")) {  
            	AI.x=parsedX+parsedDX;              
                AI.s=String.valueOf(AI.x);
            }

            //System.out.println("DecayDataset 1197: AI="+AI.x+"  "+AI.dxu+"  s="+AI.s+"  "+AI.dsu);
        }
        
        return AI;
		
    }
    
    public static XDX parse(String s0,String dsu0,String dsl0){
    	return parse(s0,dsu0,dsl0,' ');
    }
    
    public static void parse(String s,String ds,char option){
        parse(s,ds,ds,option);
    }
    public static void parse(String s,String ds){
        parse(s,ds,ds);
    }
    

    public static XDX findOriginalXDX(String s,String dsu,String dsl){
        parse(s,dsu,dsl);
        return originalXDX;
    }  
    
    public static XDX findOriginalXDX(String s,String ds){
        return findOriginalXDX(s,ds,ds);
    }

    
    public static XDX findParsedXDX(String s,String dsu,String dsl,char option){
        return parse(s,dsu,dsl,option);      
    }
    public static XDX findParsedXDX(String s,String dsu,String dsl){
        return parse(s,dsu,dsl,' ');      
    }
    
    public static XDX findParsedXDX(String s,String ds,char option){
        return findParsedXDX(s,ds,ds,option);      
    }
    public static XDX findParsedXDX(String s,String ds){
        return findParsedXDX(s,ds,ds);      
    }
    
    public static XDX findParsedXDXNoAdjust(String s,String dsu,String dsl){
    	parse(s,dsu,dsl);
        return parsedXDXNoAdjust;      
    }
    
    public static XDX findParsedXDXNoAdjust(String s,String ds){
    	return findParsedXDXNoAdjust(s,ds,ds);      
    }
    
    public static XDX getOriginalXDX(){return originalXDX;}
    public static XDX getParsedXDX(){return parsedXDX;} 
    public static XDX getParsedXDXNoAdjust(){return parsedXDXNoAdjust;} 
    
    public static void setDRIfactorForEmpty(double x){factorForEmpty=x;}
    public static void setDRIfactorForAP(double x){factorForAP=x;}
    public static void setDRIfactorForLT(double x){factorForLT=x;}
    public static void setDRIfactorPForLT(double x){factorPForLT=x;}
    public static void setDRIfactorMForLT(double x){factorMForLT=x;}
    
    public static void setDRIfactorForGT(double x){factorForGT=x;}
    public static void setDRIfactorPForGT(double x){factorPForGT=x;}
    public static void setDRIfactorMForGT(double x){factorMForGT=x;}

    public static void setDRIfactorForCA(double x){factorForCA=x;}
    public static void setDRIfactorForSY(double x){factorForSY=x;}
    
    public static double getDRIfactorForEmpty(){return factorForEmpty;}
    public static double getDRIfactorForAP(){return factorForAP;}
    public static double getDRIfactorForLT(){return factorForLT;}
    public static double getDRIfactorForGT(){return factorForGT;}
    
    public static double getDRIfactorPForLT(){return factorPForLT;}
    public static double getDRIfactorMForLT(){return factorMForLT;}
    public static double getDRIfactorPForGT(){return factorPForGT;}
    public static double getDRIfactorMForGT(){return factorMForGT;}
    
    public static double getDRIfactorForCA(){return factorForCA;}
    public static double getDRIfactorForSY(){return factorForSY;}
    

    public static UncParser defaultAbsDRIParser() {return (s,ds)->UncertaintyParser.findParsedXDX(s,ds,'A');}
    public static UncParser defaultUncParser() {return (s,ds)->UncertaintyParser.findParsedXDX(s,ds);}
    /*
     * Among a group of records like all gammas from a level if RI is branching ratio, all gammas in a dataset if RI is relative
     * find the uncertainty of RI=100 if DRI is empty: 0 or treated as a regular value without unc
     * 
     */
    public static <T extends Record> T findAndSetDRIOfNormRecord(Vector<T> recordsV,boolean ignoreQueRec) {
    	T normRec=null;
    	try {
    		String s="",ds="";
    		T maxRec=null;
    		double ri=-1,dri=-1,maxRI=-1;
    		
    		@SuppressWarnings("unused")
			int nRecordsWithUnc=0,nRecordsWithNumUnc=0,nGoodRecords=0;
    		
    		Vector<T> recordsVNoUnc=new Vector<T>();
    		Vector<T> goodRecordsV=new Vector<T>();
    		Vector<T> tempNormRecordsV=new Vector<T>();//there could be RI=100, RI=1000, ...
    		
    		for(T r:recordsV) {
    			if(!r.q().isEmpty() && ignoreQueRec)
    				continue;
    			
    			s=r.RIS();
    			ds=r.DRIS();
    			
    			ri=-1;dri=-1;
    			try {
    				ri=Double.parseDouble(s);
    				dri=Double.parseDouble(ds);
    			}catch(Exception e) {}
    			
    			if(ri<=0)
    				continue;
    			
    			nGoodRecords++;
    			goodRecordsV.add(r);
    			
    			if(dri>=0)
    				nRecordsWithNumUnc++;
    			
    			if(ds.length()>0) {
    				nRecordsWithUnc++;
    			}else {
    				recordsVNoUnc.add(r);
    			}
    			
    			if(ri%100==0) {
    				tempNormRecordsV.add(r);
    			}
    			
    			if(ri>maxRI) {
    				maxRI=ri;
    				maxRec=r;
    			}
    			
    			
    		}
    		
    		if(tempNormRecordsV.size()==0 || nGoodRecords==0)//no reference RI=100 or 1000 or ...
    			return null;
    		
    		if(nGoodRecords==1) {//here tempNormRecordsV.size() should be also equal to 1
    			return tempNormRecordsV.get(0);//DRI is not set and return as is
    		}

    		if(tempNormRecordsV.size()>1) {
    			//case 1: all or >1 have no unc, then DRI=0 can't be set for any of them
    			//case 2: all have unc, then no need to reset DRI
    			//case 3: only one has no unc, set its DRI=0 if its RI=maxRI, otherwise its DRI can't be set 
    			int tempCountNoUnc=0;
    			double tempMaxRI=-1;
    			T tempMaxRec=null,tempRec=null;
    			
    			for(T r:tempNormRecordsV) {
    				if(recordsVNoUnc.contains(r)) {
    					tempCountNoUnc++;
    					tempRec=r;
    				}
    				
    				ri=Double.parseDouble(r.RIS());
    				if(ri>tempMaxRI) {
    					tempMaxRI=ri;
    					tempMaxRec=r;
    				}
    			}
    			if(tempCountNoUnc!=1) 
    				return tempMaxRec;
    			
    			else {
    				tempRec.setAltDRIS100("0");
    				return tempRec;
    			}
    			
    		}else {
    			normRec=tempNormRecordsV.get(0);
    			
    		}

    		if(!recordsVNoUnc.contains(normRec))//norm record has DRI, no need to reset DRI of norm REC
    			return normRec;
    		
    		recordsVNoUnc.remove(normRec);

    		//max record is not the norm record but a higher energy, and it has no DRI, 
    		//then DRI=0 can't be set for norm record
    		if(maxRec!=normRec && !Str.isNumeric(maxRec.DRIS()))
    			return normRec;
    		
    		double normRI=Double.parseDouble(normRec.RIS());
    		int nBad=0;
    		for(T r:goodRecordsV) {
    			if(r==normRec)
    				continue;
    			
    			ri=Double.parseDouble(r.RIS());
    			if(!Str.isNumeric(r.DRIS())) {
            		if(ri>normRI)
            			return normRec;
            		
            		if(ri>normRI*0.1) {
            			nBad++;
            		}
    			}
    		}
    		
    		//for recordsVNoUnc.size()==goodRecordsV.size(), if all those RI<10%*normRI, still set DRI=0 for normRI
    		
    		//here, all records with no or non-numerical unc should have RI<normRI
    		int n=nGoodRecords-nRecordsWithNumUnc;
    		//int n=recordsVNoUnc.size();
    		
    		//System.out.println("UncertaintParser 408: "+normRec.RIS()+" n="+n+" nGoodRecords="+nGoodRecords+" nBad="+nBad);
    		
    		if(nBad>0 && (n>Math.min(3, nGoodRecords/2)||(n>0&&nBad>=n/2)) ){
    			return normRec;
    		}
    		
    		//if reaching here, DRI=0 can be set for normRec
    		normRec.setAltDRIS100("0");
    		return normRec;
    	}catch(Exception e) {
    	}
    	
    	return normRec;
    }
}

