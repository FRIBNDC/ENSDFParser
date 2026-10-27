package ensdfparser.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * recommended upper limit for transition strength
 * refer to ENSDF manual
 */
public class RUL_OLD {
	private static final HashMap<String,ArrayList<Float>> RULMap=new HashMap<String,ArrayList<Float>>();
	//                          for A: 6-44 45-150 >150
	//private static final Float[] E1={0.3f,0.01f,0.01f};
	//private static final Float[] E2={100f, 300f,1000f};
	//private static final Float[] E3={100f, 100f, 100f};
	//private static final Float[] E4={100f, 100f,  -1f};
	//private static final Float[] M1={ 10f,   3f,   2f};
	//private static final Float[] M2={  3f,   1f,   1f};
	//private static final Float[] M3={ 10f,  10f,  10f};
	//private static final Float[] M4={ -1f,  30f,  10f};
	static{	
		RULMap.put("E1",new ArrayList<Float>(Arrays.asList(0.3f,0.01f,0.01f)));
		RULMap.put("E2",new ArrayList<Float>(Arrays.asList(100f, 300f,1000f)));
		RULMap.put("E3",new ArrayList<Float>(Arrays.asList(100f, 100f, 100f)));
		RULMap.put("E4",new ArrayList<Float>(Arrays.asList(100f, 100f,  -1f)));
		RULMap.put("M1",new ArrayList<Float>(Arrays.asList( 10f,   3f,   2f)));
		RULMap.put("M2",new ArrayList<Float>(Arrays.asList(  3f,   1f,   1f)));
		RULMap.put("M3",new ArrayList<Float>(Arrays.asList( 10f,  10f,  10f)));
		RULMap.put("M4",new ArrayList<Float>(Arrays.asList( -1f,  30f,  10f)));
	}

	public static float getLimit(String MUL,int A){
		try{
			int index=-1;
			if(A>150)
				index=2;
			else if(A>=45)
				index=1;
			else if(A>=6)
				index=0;
			else
				return -1;
			
			return RULMap.get(MUL).get(index);
		}catch(Exception e){
			return -1;
		}
	}
	
	public static float E1(int A){
		return getLimit("E1",A);
	}
	public static float E2(int A){
		return getLimit("E2",A);
	}
	public static float E3(int A){
		return getLimit("E3",A);
	}
	public static float E4(int A){
		return getLimit("E4",A);
	}
	
	public static float M1(int A){
		return getLimit("M1",A);
	}
	public static float M2(int A){
		return getLimit("M2",A);
	}
	public static float M3(int A){
		return getLimit("M3",A);
	}
	public static float M4(int A){
		return getLimit("M4",A);
	}
	
	public static boolean isOutLimit(float BXL,String MUL,int A){
		float limit=getLimit(MUL,A);
		if(limit<0)
			return false;
		
		if(BXL<0 || BXL>=limit)
			return true;
		
		return false;
	}
}
