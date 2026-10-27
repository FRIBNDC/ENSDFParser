package ensdfparser.check;

public class RUL {
	//Recommended Upper Limit of BXLW, from ENSDF manual
	//                           A=6-44    45-150   >150 
	/*
	//Isoscalar
	private final double[] RULE1IS={0.003,    0,    0};
	private final double[] RULE2IS={  100,  300, 1000};
	private final double[] RULE3IS={  100,  100,  100};
	private final double[] RULE4IS={  100,  100,    0};
	private final double[] RULM1IS={ 0.03,    0,    0};
	private final double[] RULM2IS={  0.1,    0,    0};
	private final double[] RULM3IS={    0,    0,    0};
	private final double[] RULM4IS={    0,   30,   10};
	
	//Isovector
	private final double[] RULE1IV={  0.3, 0.01, 0.01};
	private final double[] RULE2IV={   10,    0,    0};
	private final double[] RULE3IV={    0,    0,    0};
	private final double[] RULE4IV={    0,    0,    0};
	private final double[] RULM1IV={   10,    3,    2};
	private final double[] RULM2IV={    3,    1,    1};
	private final double[] RULM3IV={   10,   10,   10};
	private final double[] RULM4IV={    0,    0,    0};
	*/
	
	
	private static final double[][] RULIS={
			{0.003,    0,    0},	 //RUL E1 IS
			{  100,  300, 1000},	 //RUL E2 IS
			{  100,  100,  100},	 //RUL E3 IS
			{  100,  100,    0},	 //RUL E4 IS
			{ 0.03,    0,    0},	 //RUL M1 IS
			{  0.1,    0,    0},	 //RUL M2 IS
			{    0,    0,    0},	 //RUL M3 IS
			{    0,   30,   10} 	 //RUL M4 IS			
	};
	private static final double[][] RULIV={
			{  0.3, 0.01, 0.01},	 //RUL E1 IV
			{   10,    0,    0},	 //RUL E2 IV
			{    0,    0,    0},	 //RUL E3 IV
			{    0,    0,    0},	 //RUL E4 IV
			{   10,    3,    2},	 //RUL M1 IV
			{    3,    1,    1},	 //RUL M2 IV
			{   10,   10,   10},	 //RUL M3 IV
			{    0,    0,    0} 	 //RUL M4 IV		
	};
	
	/*
	//Recommended Upper Limit of BXLW, from ENSDF manual
	//                           A=6-44    45-150   >150 
	//Isoscalar
	private final double[] RULE1IS={0.003,    0,    0};
	private final double[] RULE2IS={  100,  300, 1000};
	private final double[] RULE3IS={  100,  100,  100};
	private final double[] RULE4IS={  100,  100,    0};
	private final double[] RULM1IS={ 0.03,    0,    0};
	private final double[] RULM2IS={  0.1,    0,    0};
	private final double[] RULM3IS={    0,    0,    0};
	private final double[] RULM4IS={    0,   30,    10};
	
	//Isovector
	private final double[] RULE1IV={  0.3, 0.01, 0.01};
	private final double[] RULE2IV={   10,    0,    0};
	private final double[] RULE3IV={    0,    0,    0};
	private final double[] RULE4IV={    0,    0,    0};
	private final double[] RULM1IV={   10,    3,    2};
	private final double[] RULM2IV={    3,    1,    1};
	private final double[] RULM3IV={   10,   10,   10};
	private final double[] RULM4IV={    0,    0,    0};
	 */
	public static float[] getRULs(int A,String type){
		float[] out=new float[2];
		out[0]=0;
		out[1]=0;
		
		String s=type.trim().toUpperCase();
		if(A<6 || s.length()!=2 || (s.charAt(0)!='E'&&s.charAt(0)!='M') || !Character.isDigit(s.charAt(1)))
			return out;
		
		char c=s.charAt(0);
		int L=Integer.parseInt(s.charAt(1)+"");
		if(L>4 || L==0)
			return out;		
		
		//RULIS[i][j] and RULIV[i][j]:
		//i=0-3: for E1,E2,E3,E4
		//i=4-7: for M1,M2,M3,M4
		//j=0,1,2: for A=6-44, 45-150, >150	
		int i=L-1,j=0;
	    if(c=='M')
	    	i=i+4;
	    
	    float otherIS=0,otherIV=0;
		if(A<=44){
			j=0;
			if(A>=21 && i==0)//E1(IV)
				otherIV=0.1f;
		}else if(A<=150){
			j=1;
			if(A>=90 && i==3)//E4(IS)
				otherIS=30f;
		}else
			j=2;
		
		out[0]=(float)RULIS[i][j];
		out[1]=(float)RULIV[i][j];
		
		if(otherIS>0)
			out[0]=otherIS;
		if(otherIV>0)
			out[1]=otherIV;
				
		return out;		
	}
	
	public static float getRUL(int A,String type){
		float[] out=getRULs(A,type);
		float is=out[0];
		float iv=out[1];
		
		//System.out.println(A+"  "+type+"   "+is+"  "+iv);
		
		if(is>iv)
			return is;
		else
			return iv;
	}	
	
	public static float getLimit(int A,String type) {
		return getRUL(A,type);
	}
}
