package ensdfparser.check;

import java.util.Vector;

import ensdfparser.calc.UncertaintyParser;
import ensdfparser.calc.XDX;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.nds.util.Str;

public class LOGFT {
	//see 2023TU02,1998SI17,1973RA10
	
	//for decays above second unique, no limit can be obtained due to scarce of available data.
	//Note that all values below are for logft calculated as allowed
	public static final double limitOfAllowed=5.9;    //allowed, below that, DJ=0 or 1, DP=no
	public static final double limitOfFirstUnique=7.4;//for logft calculated as allowed (corresponding logft(1U)<logft+1.079~8.5, calculated as 1U)
	                              //below that, DJ=0,1, DP=yes or no (allowed or 1st non-unique)
	public static final double limitOfSecondNonUnique=11.0;//below that, 1) DJ=0 or 1, DP=yes or no (allowed or 1st non-unique); 
	                                                       //            2) DJ=2, DP=yes (1st unique)
	public static final double limitOfSecondUnique=13.8;//below that, DJ=0,1,2, DP=yes or no 
	                                                    //Note it doesn't mean above 13.8, decay is 2nd unique; but means all 2nd unique>13.8
	                                                    //same applies to other limits
	
	public static final double lowerLimitOfSuperAllowed=3.17;//0+ to 0+, deltaIsospin=0
	public static final double upperLimitOfSuperAllowed=3.53;
	
	public static final double limitOfIsospinForbidden=6.4;//0+ to 0+, deltaIsospin=1, all>6.4, so below 6.4, it can't be 0+ to 0+ with deltaISPIN=1
	
	//so if logft is within 3.6 and 6.4, it can't be 0+ to 0+ (either super-allowed or isospin-forbidden)
	
	static double bufferOffset=0.1;//for values too close to the limits
	
	 
	public static double limitOfAllowed(int Z) {
		if(Z<80)
			return limitOfAllowed;
		
		return 5.2;
	}
	public static Vector<String> findDeltaJPSForLogft(SDS2XDX logft,int Z,boolean isWeakBranch,boolean isQueBranch){
		return findDeltaJPSForLogft(logft,Z,isWeakBranch,isQueBranch,bufferOffset);
	}
	public static Vector<String> findDeltaJPSForLogft(SDS2XDX logft,int Z,boolean isWeakBranch,boolean isQueBranch,double bufferOffset){
		return findDeltaJPSForLogft(logft,Z,isWeakBranch,isQueBranch,bufferOffset,"");
	}
	public static Vector<String> findDeltaJPSForLogft(SDS2XDX logft,int Z,boolean isWeakBranch,boolean isQueBranch,double bufferOffset,String parentJS){
		Vector<String> LV=new Vector<String>();
		 try {
			double x=logft.x();
			double dx=logft.dx();
			String s=logft.s();
			String ds=logft.ds();
			if(x<=0)
				return LV;
			
			double tempLimitOfAllowed=limitOfAllowed(Z);
			
			boolean isGoodUnc=false;
			if(logft.dxu()>0) {
				dx=logft.dxu();
				isGoodUnc=true;
			}else if(dx<0) {
				XDX xdx=UncertaintyParser.defaultUncParser().get(s, ds);
				
				x=xdx.x();//note that x could be changed for ds=LT, LE, GT, GE
				dx=xdx.dxu();
				if(ds.isEmpty() || ds.startsWith("L")) {
					if(logft.x()<tempLimitOfAllowed-bufferOffset && x<tempLimitOfAllowed-bufferOffset && (x+dx)>=tempLimitOfAllowed) {
						x=logft.x();
						dx=0.01;
						isWeakBranch=true;
					}
					
					if(ds.startsWith("L"))
						isGoodUnc=true;
				}

			}
			 
			if(dx>0) {
				isQueBranch=(isQueBranch||!isGoodUnc);
				LV=findDeltaJPSForLogft(x+dx,Z,isWeakBranch,isQueBranch,bufferOffset,parentJS);
			}else {
				LV.add("(0)");
				LV.add("(1)");
				LV.add("(2)");
				LV.add("(3+)");//2nd forbidden-unique
			}
		 }catch(Exception e) {
			 
		 }
		
		 return LV;	
	}
	public static Vector<String> findDeltaJPSForLogft(double logft,int Z,boolean isWeakBranch,boolean isQueBranch){
		return findDeltaJPSForLogft(logft,Z,isWeakBranch,isQueBranch);
	}
	public static Vector<String> findDeltaJPSForLogft(double logft,int Z,boolean isWeakBranch,boolean isQueBranch,double bufferOffset){
		return findDeltaJPSForLogft(logft,Z,isWeakBranch,isQueBranch,bufferOffset,"");
	}
    public static Vector<String> findDeltaJPSForLogft(double logft,int Z,boolean isWeakBranch,boolean isQueBranch,double bufferOffset,String parentJS){
    	//note that decays above 2nd forbidden unique are very rare
    	
    	Vector<String> LV=new Vector<String>();
    	try {
    		if(logft<=0)
    			return LV;
    		
    		double x=logft;
    		
			double tempLimitOfAllowed=limitOfAllowed(Z);
			
			//System.out.println("LOGFT 107: logft="+logft+" tempLimit="+tempLimitOfAllowed+" Que="+isQueBranch+" weak="+isWeakBranch+" offset="+bufferOffset);
			
			if(x<=tempLimitOfAllowed-bufferOffset) {

				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
				}else if(isWeakBranch) {
					LV.add("0(+)");
					LV.add("1(+)");
				}else {
					LV.add("0+");
					LV.add("1+");
				}
			}else if(x<=tempLimitOfAllowed) {
				//for logft values too close to the limit
				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
				}else if(isWeakBranch) {
					LV.add("(0+)");
					LV.add("(1+)");
				}else {
					LV.add("0(+)");
					LV.add("1(+)");
				}

			}else if(x<=limitOfFirstUnique-bufferOffset) {
				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
				}else if(isWeakBranch) {
					LV.add("(0)");
					LV.add("(1)");
				}else {
					LV.add("0");
					LV.add("1");
				}

			}else if(x<=limitOfFirstUnique) {
			    if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
					LV.add("(3)");
			    }else if(isWeakBranch) {
					LV.add("0");
					LV.add("1");
					LV.add("2-");
				}else {
					LV.add("(0)");
					LV.add("(1)");	
				}
			}else if(x<limitOfSecondNonUnique-bufferOffset){
				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
					LV.add("(3)");
				}else {
					LV.add("0");
					LV.add("1");
					LV.add("2-");
				}

			}else if(x<limitOfSecondNonUnique){
				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
					LV.add("(3)");
				}else {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2-)");
				}
			}else if(x<limitOfSecondUnique-bufferOffset) {
				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
					LV.add("(3)");
				}else {
					LV.add("0");
					LV.add("1");
					LV.add("2");	
				}

			}else if(x<limitOfSecondUnique) {
				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
					LV.add("(3)");
				}else {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
				}

			}else {
				if(isQueBranch) {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
					LV.add("(3)");
					LV.add("(4)");
				}else {
					LV.add("(0)");
					LV.add("(1)");
					LV.add("(2)");
					LV.add("(3+)");//2nd forbidden-unique
				}
			}

			//System.out.println("LOGFT 190: logft="+logft+" tempLimit="+tempLimitOfAllowed+" Que="+isQueBranch+" weak="+isWeakBranch+" offset="+bufferOffset);
			//for(String s:LV) System.out.println(s);
			
			String jps=parentJS.trim();
			jps=Str.removeAllBrackets(jps);
			if(jps.equals("0+") && x<limitOfIsospinForbidden && x>3.6) {
				for(String s:LV) {
					if(s.contains("0+")) {
						LV.remove(s);
						break;
					}
				}
			}
    	}catch(Exception e) {
    		
    	}
    	
		//System.out.println("LOGFT 190: logft="+logft+"  Que="+isQueBranch+" weak="+isWeakBranch+" offset="+bufferOffset);
		//for(String s:LV) System.out.println(s);
		
    	return LV;
    }
}
