package ensdfparser.calc;

import java.util.LinkedHashMap;
import java.util.Vector;

import ensdfparser.check.RUL;

public class BXLBase {
	protected double mass;
	
	
	//T(EL)*B(EL)down=L/(L+1)*[(2L+1)!!]^2*2.488354E-18*(3.89391289E8)^L*1/Eg^(2L+1)=A/Eg^(2L+1)
	//constantBELW=A/constantBESP=L/(L+1)*[(3+L)/3]^2*[(2L+1)!!]^2*3.126957E-17*(2.70410617E10)^L
	final static double[] constantBESP={0,6.446E-4,5.940E-6,5.940E-8,6.285E-10,6.929E-12,7.884E-14};//=1.0/(4*pi)*[3/(3+L)]^2*(0.0144)^L
	final static double[] constantBELW={0,6.764E-6,9.527E6,2.045E19,6.503E31,2.895E44,1.722E57};
	
	//T(ML)*B(ML)down=L/(L+1)*[(2L+1)!!]^2*2.250260E-14*(3.89391289E8)^L*1/Eg^(2L+1)=B/Eg^(2L+1)
	//constantBMLW=B/constantBMSP=L/(L+1)*[(3+L)/3]^2*[(2L+1)!!]^2*1.017994E-16*(2.70410617E10)^L
	final static double[] constantBMSP={0,1.791,1.650E-2,1.650E-4,1.746E-6,1.925E-8,2.190E-10};//=10/pi*[3/(3+L)]^2*(0.0144)^(L-1)
	final static double[] constantBMLW={0,2.202E-5,3.102E7,6.658E19,2.117E32,9.425E44,5.607E57};

	private static long doublefact(long n) {		     
	    if (n == 0 || n==1) 
	        return 1; 
	      return n*doublefact(n-2); 
	  
	}

	//type=E or M
	public static double getConstantBXSP(String type,int L) {
		String s=type.trim().toUpperCase();
		
		if(s.equals("E"))
			return getConstantBESP(L);
		else if(s.equals("M"))
			return getConstantBMSP(L);
		else
			return -1;
	}
	
	//type=EL or ML
	public static double getConstantBXSP(String typeL) {
		try {
			String s=typeL.trim().toUpperCase();
			char c1=s.charAt(0);
			char c2=s.charAt(1);
			String type=c1+"";
			int L=Integer.parseInt(c2+"");
			
			return getConstantBXSP(type,L);

		}catch(Exception e) {
			
		}
		
		return -1;
	}
	
	//type=E or M
	public static double getConstantBXLW(String type,int L) {
		String s=type.trim().toUpperCase();
		
		if(s.equals("E"))
			return getConstantBELW(L);
		else if(s.equals("M"))
			return getConstantBMLW(L);
		else
			return -1;
	}
	
	//type=EL or ML
	public static double getConstantBXLW(String typeL) {
		try {
			String s=typeL.trim().toUpperCase();
			char c1=s.charAt(0);
			char c2=s.charAt(1);
			String type=c1+"";
			int L=Integer.parseInt(c2+"");
			
			return getConstantBXLW(type,L);

		}catch(Exception e) {
			
		}
		
		return -1;
	}
	
	public static double getConstantBESP(int L) {
		try {
			if(L>0)
				return 1.0/(4*Math.PI)*Math.pow(3.0/(3+L),2)*Math.pow(0.0144,L);
		}catch(Exception e) {
			
		}
		return -1;
	}

	public static double getConstantBELW(int L) {
		try {
			if(L>0)
				return L/(L+1.0)*Math.pow((3+L)/3.0,2)*Math.pow(doublefact(2*L+1),2)*3.126957E-17*Math.pow(2.70410617E10,L);
		}catch(Exception e) {
			
		}
		return -1;
	}
	public static double getConstantBMSP(int L) {
		try {
			if(L>0)
				return 10.0/Math.PI*Math.pow(3.0/(3+L),2)*Math.pow(0.0144,L-1);
		}catch(Exception e) {
			
		}
		return -1;
	}
	public static double getConstantBMLW(int L) {
		//L/(L+1)*[(3+L)/3]^2*[(2L+1)!!]^2*1.017994E-16*(2.70410617E10)^L
		try {
			if(L>0)
				return L/(L+1.0)*Math.pow((3+L)/3.0,2)*Math.pow(doublefact(2*L+1),2)*1.017994E-16*Math.pow(2.70410617E10,L);
		}catch(Exception e) {
			
		}
		return -1;
	}
	
	public void setMass(double mass){this.mass=mass;}
	
	/////////////////////////////
	// calculating functions
	////////////////////////////
	public double BELDOWN_SP(int L){
		return BELDOWN_SP(L,mass);
	}
	
	public static double BELDOWN_SP(int L,double mass){
		if(mass<=0 || L<=0)
			return -1;
		
		double c=-1;
		try {
			c=constantBESP[L];
		}catch(Exception e) {
			c=getConstantBESP(L);
		}
		
		try{
			if(c>0)
				return c*Math.pow(mass,2.0*L/3.0);
		}catch(Exception e){}
			
		return -1;
	}
    
	
    public double BE1DOWN_SP(){return BELDOWN_SP(1);}
	public double BE2DOWN_SP(){return BELDOWN_SP(2);}
	public double BE3DOWN_SP(){return BELDOWN_SP(3);}
	public double BE4DOWN_SP(){return BELDOWN_SP(4);}
	public double BE5DOWN_SP(){return BELDOWN_SP(5);}

    public static double BE1DOWN_SP(double mass){return BELDOWN_SP(1,mass);}
	public static double BE2DOWN_SP(double mass){return BELDOWN_SP(2,mass);}
	public static double BE3DOWN_SP(double mass){return BELDOWN_SP(3,mass);}
	public static double BE4DOWN_SP(double mass){return BELDOWN_SP(4,mass);}
	public static double BE5DOWN_SP(double mass){return BELDOWN_SP(5,mass);}
	
	public double BMLDOWN_SP(int L){
		return BMLDOWN_SP(L,mass);
	}
	
	public static double BMLDOWN_SP(int L,double mass){
		if(mass<=0 || L<=0)
			return -1;
		
		double c=-1;
		try {
			c=constantBMSP[L];
		}catch(Exception e) {
			c=getConstantBMSP(L);
		}
		
		try{
			if(c>0)
				return c*Math.pow(mass,2.0*(L-1)/3.0);
		}catch(Exception e){}
			
		return -1;
	}
	
	public double BXLDOWN_SP(int L,String option){
		return BXLDOWN_SP(L,option,mass);
	}
	
	public static double BXLDOWN_SP(int L,String option,double mass){
		if(option.toUpperCase().trim().indexOf("M")==0)
			return BMLDOWN_SP(L,mass);
		else
			return BELDOWN_SP(L,mass);
	}
	
	public double BM1DOWN_SP(){return BMLDOWN_SP(1);}
	public double BM2DOWN_SP(){return BMLDOWN_SP(2);}
	public double BM3DOWN_SP(){return BMLDOWN_SP(3);}
	public double BM4DOWN_SP(){return BMLDOWN_SP(4);}
	public double BM5DOWN_SP(){return BMLDOWN_SP(5);}
	
    public static double BM1DOWN_SP(double mass){return BMLDOWN_SP(1,mass);}
	public static double BM2DOWN_SP(double mass){return BMLDOWN_SP(2,mass);}
	public static double BM3DOWN_SP(double mass){return BMLDOWN_SP(3,mass);}
	public static double BM4DOWN_SP(double mass){return BMLDOWN_SP(4,mass);}
	public static double BM5DOWN_SP(double mass){return BMLDOWN_SP(5,mass);}

	public double T12EL_SP(double eg,int L){
		return T12EL_SP(eg,L,mass);
	}
	public static double T12EL_SP(double eg,int L,double mass){
		if(mass<=0 || eg<=0 || L<=0)
			return -1;
		
		double c=-1;
		try {
			c=constantBELW[L];
		}catch(Exception e) {
			c=getConstantBELW(L);
		}
		
		try{		
			if(c>0)
				return c/Math.pow(mass,2.0*L/3.0)/Math.pow(eg,2*L+1);
		}catch(Exception e){}
			
		return -1;
	}

	public double T12ML_SP(double eg,int L){
		return T12ML_SP(eg,L,mass);
	}
	public static double T12ML_SP(double eg,int L,double mass){
		if(mass<=0 || eg<=0 || L<=0)
			return -1;
		
		double c=-1;
		try {
			c=constantBMLW[L];
		}catch(Exception e) {
			c=getConstantBMLW(L);
		}
		
		try{	
			if(c>0)
				return c/Math.pow(mass,2.0*(L-1)/3.0)/Math.pow(eg,2*L+1);
		}catch(Exception e){}
			
		return -1;
	}

	
	public double T12XL_SP(double eg,int L,String option){
		return T12XL_SP(eg,L,option,mass);
	}
	/*
	 * option="E" or "M"
	 * 
	 * option="E" by default, if empty or incorrect
	 */
	public static double T12XL_SP(double eg,int L,String option,double mass){
		if(option.toUpperCase().trim().indexOf("M")==0)
			return T12ML_SP(eg,L,mass);
		else
			return T12EL_SP(eg,L,mass);
	}
	
	
	public double DT12XL_SP(double eg,double deg, int L,String option){	
		return DT12XL_SP(eg,deg,L,option,mass);
	}
	/*
	 * T11XL_SP unertainty
	 */
	public static double DT12XL_SP(double eg,double deg, int L,String option,double mass){		
		return T12XL_SP(eg,L,option,mass)*(2*L+1)*(deg/eg);
	}
	
	/*
	 *  Loption="L" or "L+1" for BX(L) and BX(L+1) component, respectively,
	 *   if mixed multipolarity
	 *  Loption="L" by default, if empty or incorrect
	 */

	public double BXLW2BXLDOWN(double BXLW,int L,String type){return BXLW*BXLDOWN_SP(L,type);}
	public static double BXLW2BXLDOWN(double BXLW,int L,String type,double mass){return BXLW*BXLDOWN_SP(L,type,mass);}
	
	
	public double BXLW2BXLUP(double BXLW,int L,String type,float ji,float jf){return BXLW2BXLUP(BXLW,L,type,ji,jf,mass);}
	public static double BXLW2BXLUP(double BXLW,int L,String type,float ji,float jf,double mass){
		if(ji<0 || jf<0 || (ji+jf)==0)
			return -1;
		
		return BXLW2BXLDOWN(BXLW,L,type,mass)*(2*ji+1)/(2*jf+1);	
	}
	
	public double BXLDOWN2BXLW(double BXLDOWN,int L,String type){return BXLDOWN/BXLDOWN_SP(L,type,mass);}
	public static double BXLDOWN2BXLW(double BXLDOWN,int L,String type,double mass){return BXLDOWN/BXLDOWN_SP(L,type,mass);}
	
	public double BXLUP2BXLW(double BXLUP,int L,String type,float ji,float jf){return BXLUP2BXLW(BXLUP,L,type,ji,jf,mass);}
	public static double BXLUP2BXLW(double BXLUP,int L,String type,float ji,float jf,double mass){
		if(ji<0 || jf<0 || (ji+jf)==0)
			return -1;
		
		return BXLUP/BXLDOWN_SP(L,type,mass)*(2*jf+1)/(2*ji+1);		
	}

	
	public static double BXLUP2BXLDOWN(double BXLUP,float ji,float jf) {//ji for the level gamma decay from, jf to the level gamma decay to 
		return BXLUP*(2*jf+1)/(2*ji+1);
	}
	public static double BXLDOWN2BXLUP(double BXLDOWN,float ji,float jf) {//ji for the level gamma decay from, jf to the level gamma decay to 
		return BXLDOWN*(2*ji+1)/(2*jf+1);
	}

	public static float[] RULs(int A,String type){
		return RUL.getRULs(A, type);	
	}
	
	public static float RUL(int A,String type){
		return RUL.getRUL(A, type);
	}	
	
	//public static LinkedHashMap<String,String> sortEMs(String[] EMs){
	//	return sortEMs(EMs,true);
	//}
	
	/*
	 * isMixed=false, then calculate each component independently as pure
	 */
	public static LinkedHashMap<String,String> sortEMs(String[] EMs,boolean isMixed){
		LinkedHashMap<String,String> out=new LinkedHashMap<String,String>();

		String[] types=new String[EMs.length];
		String[] Loptions=new String[EMs.length];
		Integer[] Ls=new Integer[EMs.length];
		
		int nz=0;//none zero L
		for(int j=0;j<EMs.length;j++){
			String type=EMs[j];
			
			//debug
			//System.out.println(" lev="+lev.ES()+" gam="+gam.ES()+" M="+gam.MS()+" eg="+gb.e+" *"+type+"*"+" j="+j+" size="+gb.EMs.length+" gb.mr="+gb.mr+" isGoodEM(type)="+gb.isGoodEM(type)+" pt="+gb.pt);
			try{
				int L=Integer.parseInt(type.substring(type.length()-1));
				if(L>0) {					
					Loptions[nz]="L";				
	                Ls[nz]=L;
	                types[nz]=type;
	                nz++;
				}else
					out.put(type, "L");//for L=0
			}catch(Exception e){}
		}
		
		if(nz==2 && Ls[0]>0 && Ls[1]>0){
			if(Ls[0]>Ls[1]){
				if(isMixed)
					Loptions[0]="L+1";
				
				int tempL=Ls[0];
				String tempType=types[0];
				Ls[0]=Ls[1];
				types[0]=types[1];
				
				Ls[1]=tempL;
				types[1]=tempType;
			}else if(Ls[0]<Ls[1]){
				if(isMixed)
					Loptions[1]="L+1";
			}
		}
		
		
		for(int i=0;i<types.length;i++){
			out.put(types[i],Loptions[i]);
		}
		
		
		return out;
	}

	public static double AFactor(int A,String MUL) {
		try {
			MUL=MUL.trim().toUpperCase();
			char c1=MUL.charAt(0);
			char c2=MUL.charAt(1);
			int L=Integer.parseInt(c2+"");
			if(!"EM".contains(c1+"") || L<=0 || A<0)
				return -1;
			
			double AFactor=0;
			if(c1=='E')
				AFactor=Math.pow(A,2.0*L/3.0);
			else
				AFactor=Math.pow(A,2.0*(L-1)/3.0);
			
			return AFactor;
		}catch(Exception e) {
			
		}
		
		return -1;
	}
	//for ICC=0, branching for (g+ce) BR=100%
	//For other ICC and branching, multiply BR/(1+ICC)
	//t12 in units of second
	public static float findMinEGForRUL(int A,String MUL,double t12) {
		try {
			MUL=MUL.trim().toUpperCase();
			char c1=MUL.charAt(0);
			char c2=MUL.charAt(1);
			int L=Integer.parseInt(c2+"");
			if(!"EM".contains(c1+"") || L<=0 || L>4)
				return -1;
			if(A<=0||t12<=0)
				return -1;
			
			float bx=RUL(A,MUL);//B(X)W.u., =T1/2(s.p.)/t12
			if(bx<=0)
				return -1;
			
			double c=getConstantBXLW(MUL);
			double AFactor=AFactor(A,MUL);
				
			double eg=c/AFactor/t12/bx;//=EG^(2L+1)
			
			return (float)Math.pow(eg, 1.0/(2*L+1));
			
			//T1/2(s.p.)
		}catch(Exception e) {
			
		}
		
		return -1;
	}
	
	public static String findPossibleMULForEG(String es,int A,double t12) {
		try {
			String[] mults= {"E1","M1","E2","M2","E3","M3","E4","M4"};
			
        	if(t12<=0)
        		t12=0.01E-9;//in units of second
        	
        	float eg=-1;
        	try {
        		eg=Float.parseFloat(es);
        	}catch(Exception e) {
        		return "";
        	}
        	
        	
        	String s="";
        	Vector<Float> egV=new Vector<Float>();
        	Vector<String> multV=new Vector<String>();
        	
        	for(int i=0;i<mults.length;i++) {
        		String MUL=mults[i];
        		float minEG=-1;
        		float ratio=0.8f;
        		
        		if(A>0) {
        			minEG=findMinEGForRUL(A,MUL,t12);
        		}else {
        			
        			float tempEG=0;
        			int count=0;
        			
        			//ratio=0.70f;
        			
        			int[] As= {10,20,30,40,50,80,110,140,160,200,240,280};
        			for(int j=0;j<As.length;j++) {
        				float eg1=findMinEGForRUL(As[j],MUL,t12);
           			//for(int a=10;a<300;a=a+10) {
        			//	float eg1=findMinEGForRUL(a,MUL,t12);
        				if(eg1>0) {
        					count++;
        					tempEG+=eg1;
        					//System.out.println(count+" A="+As[j]+" MUL="+MUL+" minEG="+eg1);
        				}
        			}
        			
        			if(count>0) {
        				minEG=tempEG/count;
        			}
        			
        			//System.out.println(count+"  "+MUL+"  "+minEG);
        		}

        		
        		if(minEG<=0)
        			break;
        		
        		if(eg>minEG*ratio) {
        			egV.add(eg);
        			multV.add(MUL);
        		}
        		//if(Math.abs(eg-4101.4)<0.1)
        		//	System.out.println("BXLBase 448: eg="+eg+" MUL="+MUL+" minEG="+minEG+" minEG*ratio="+(minEG*ratio));
        	}
        	if(egV.size()>0) {
        		for(int i=0;i<egV.size()-1;i++) {
        			float ei=egV.get(i);
        			String mi=multV.get(i);
        			
        			for(int j=i;j<egV.size();j++) {
        				float ej=egV.get(j);
        				String mj=multV.get(j);
        				if(ei>ej) {
        					egV.remove(i);
        					egV.add(i,ej);
        					multV.remove(i);
        					multV.add(i,mi);
        					
        					egV.remove(j);
        					egV.add(j,ei);
        					multV.remove(j);
        					multV.add(i,mj);
        				}
        			}
        		}
        		for(String mult:multV)
        			s+=mult+",";
        			
    			if(s.endsWith(","))
    				s=s.substring(0,s.length()-1).trim();
        	}

    		
    		
    		//if(Math.abs(eg-4101.4)<0.1)
    		//	System.out.println("BXLBase 488: eg="+eg+" s="+s);
    		
			//if(es.equals("2386.4")||es.equals("2386"))
			//	System.out.println(es+"######"+s);
			
			return s;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/*
	 * t12 in units of second
	 */
	public static String printMinEGForAllRULs(double t12) {
		try {
			String s="";
			boolean printHeader=true;
			for(int A=10;A<300;A=A+10) {
				if(A>10)
					printHeader=false;
				s+=printMinEGForAllRULsOfA(t12,A,printHeader);
			}
			
			return s;
		}catch(Exception e) {
			
		}
		return "";
	}
	
	public static String printMinEGForAllRULsOfA(double t12,int A) {
		return printMinEGForAllRULsOfA(t12,A,true);
	}
	public static String printMinEGForAllRULsOfA(double t12,int A,boolean printHeader) {
		try {
			String[] mults= {"M1","E1","E2","M2","E3","M3","E4","M4"};
			if(t12<=0)
				return "";
			
			String line="",s="";
			
			//System.out.println(RUL.getRUL(150,"E4"));
			
			if(printHeader) {
				line=String.format("%-10s", "Mass");
				
				for(int i=0;i<mults.length;i++) {
					String mult=mults[i];
					line+=String.format("%-10s", mult);
					
				}
				//System.out.println(line);
				s+=line+"\n";
			}

			line=String.format("%-10s", A+"");		
			for(int i=0;i<mults.length;i++) {
				String mult=mults[i];
				float eg=BXLBase.findMinEGForRUL(A, mult,t12);
				line+=String.format("%-10s", ((int)Math.round(eg))+"");
			}
			
			//System.out.println(line);
			s+=line+"\n";
			
			return s;
		}catch(Exception e) {
			
		}
		return "";
	}
}
