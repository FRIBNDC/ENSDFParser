package ensdfparser.nds.ensdf;

import java.io.IOException;
import java.util.Vector;

import ensdfparser.nds.latex.Translator;

public class DSIDMatcher {

	private Vector<String> dsidsV=new Vector<String>();//translated, e.g., {+31}S |b{+-} decay
	private Vector<String> DSIDsV=new Vector<String>();//not translated, e.g., 31S B- DECAY
	
	private Vector<String> shortDsidsV=new Vector<String>();//translated, e.g., (p,d) for {+31}S(p,d)
	
	public DSIDMatcher() {
		try {
			Translator.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clear();
	}
	public DSIDMatcher(Vector<String> dsidsV) {
		this();
		setDSIDsV(dsidsV);
	}
	
	public void setDSIDsV(Vector<String> dsidsV) {
		clear();
		
		for(String s:dsidsV) {
			addDSID(s);
		}

	}
	public void clear() {
		dsidsV.clear();
		DSIDsV.clear();
		shortDsidsV.clear();
	}
	
	
	public int size() {return dsidsV.size();}
	
	public void add(String dsid) {
		addDSID(dsid);
	}
	
	public void addAll(Vector<String> dsidsV) {
		if(dsidsV==null)
			return;
		
		for(String s:dsidsV)
			addDSID(s);
	}
	
	public void addDSID(String dsid) {
		try {
			dsid=dsid.trim();
			
			String s=Translator.procGenCom(dsid, 0, false);
			if(s.equals(dsid) || s.toUpperCase().equals(dsid.toUpperCase())) {
				if(!dsidsV.contains(s)) {
					dsidsV.add(s);
					DSIDsV.add(deGenCom(s));
				}

			}else if(dsid.toUpperCase().equals(dsid)) {
				if(!DSIDsV.contains(dsid)) {
					DSIDsV.add(dsid);
					dsidsV.add(s);
				}
			}

			String s1=shorten(s);
			
			//System.out.println("   s="+s+"   s1="+s1);
			
			if(!s1.isEmpty() && s1.length()<s.length()) {
				shortDsidsV.add(s1);
			}

		}catch(Exception e) {
			
		}
	}
	

	public boolean contains(String dsid) {
		String s=dsid.trim();
		String s1=dsid.toUpperCase();
        
		if(DSIDsV.contains(s1) || dsidsV.contains(s) || shortDsidsV.contains(s))
			return true;
		
		String s2=shorten(s);
		if(DSIDsV.contains(s2) || dsidsV.contains(s2) || shortDsidsV.contains(s2))
			return true;
		
		return false; 
	}
	
	public void print() {
		for(String s:dsidsV)
			System.out.println("lower: "+s);
		for(String s:DSIDsV)
			System.out.println("Upper: "+s);
		for(String s:shortDsidsV)
			System.out.println("Short: "+s);
	}
	
	////////////////////////////////////////////////
	//static methods
	public static boolean isTranslatedDSID(String dsid) {
		if(dsid==null || dsid.trim().isEmpty())
			return false;
		
		dsid=dsid.trim();
		
		String s=translate(dsid);		
		if(s.equals(dsid))
			return true;
		
		return false;
	}
	
	//not the same as translate in Translator, which is for translating to LaTeX
	//translate Upper-case "C" type to lower-case "c" type
	
	public static String translate(String dsid) {
		if(dsid==null || dsid.trim().isEmpty())
			return dsid;
		
		dsid=dsid.trim();

		try {
			dsid=Translator.procGenCom(dsid, 0, false);
		}catch(Exception e) {
			try {
				Translator.init();
				dsid=Translator.procGenCom(dsid, 0, false);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return dsid;
	}
	
	public static String deGenCom(String dsid) {
		try {
			String s=dsid.replace("{+","").replace("{-","").replace("}","").trim();
			s=s.replace("|b","B").replace("|a", "A").replace("|g","G").replace("|e","EC");
			
			dsid=s.trim().toUpperCase();
		}catch(Exception e) {
			
		}
		
		return dsid;
	}
	
	//For input A(B,C)D, return (B,C)
	public static String shorten(String dsid) {
		try {
			String s=dsid.trim();
			String out="",s1="";
			while(s.length()>0) {
				int n1=s.indexOf("(");
				int n2=s.indexOf(")");
				if(n1>0 && n2>n1) {
					s1=s.substring(n1,n2+1);
					s=s.substring(n2+1);
					if(s1.contains(",")) { 
						out+=s1+",";	
						continue;
					}else {
						break;
					}
				}else {
					break;
				}
			}

			if(out.length()>1 && out.endsWith(",")) {
				dsid=out.substring(0,out.length()-1).trim();
				if(s.length()>0)
					dsid+=s;
			}
		}catch(Exception e) {
			
		}
		return dsid;
	}
}
