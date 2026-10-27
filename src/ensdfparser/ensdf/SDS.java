package ensdfparser.ensdf;

public class SDS {
	String s="",ds="",unit="",sds="";
	boolean hasUnit=false,isEmpty=false;
	public SDS(String s,String ds) {
		try {
			this.s=s.trim();
			this.ds=ds.trim();
			setSDS();
		}catch(Exception e) {
			
		}

	}
	
	public SDS(String s,String ds,String unit) {
		try {
			this.s=s.trim();
			this.ds=ds.trim();
			this.unit=unit.trim();
			setSDS();
		}catch(Exception e) {
			
		}
	}
	
	private void setSDS() {
		try {
			sds=s+"("+ds+")";
			sds=sds.replace("()","").trim();
			if(unit.length()>0) {
				sds+=" "+unit;
				hasUnit=true;
			}
			if(sds.isEmpty())
				isEmpty=true;
		}catch(Exception e) {
			
		}
	}
	public String s() {return s;}
	public String ds() {return ds;}
	public String sds() {return sds;}
	public String toString() {return sds;}
	
	public boolean hasUnit() {return hasUnit;}
	public boolean isEmpty() {return isEmpty;}
	
	public boolean equals(SDS sds1) {
		return sds().equals(sds1.sds());
	}
}
