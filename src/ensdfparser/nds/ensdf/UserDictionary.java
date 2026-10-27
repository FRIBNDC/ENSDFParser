package ensdfparser.nds.ensdf;

import java.util.HashMap;
import java.util.Map;

/* user-defined dictionary for converting words in upper-case "C" comment to 
 * translations in lower-case "c" comment 
 */
public class UserDictionary {

	private static HashMap<String,String> dict=new HashMap<String,String>();
	
	public UserDictionary(){}
	
	public static void addEntry(String entry){
		String s=entry.trim();
		int index=s.indexOf("=");
		if(index<=0||index==s.length()-1)
			return;
		
		String word,translation;
		word=s.substring(0, index).trim();
		translation=s.substring(index+1).trim();
		dict.put(word, translation);
	}
	
	public static String getTranslation(String word){
		String translation=dict.get(word);
		if(translation==null)
			translation=" ";
		
		return translation;
	}
	
	public static HashMap<String,String> getDICT(){return dict;}
	
    public static void setDICT(HashMap<String,String> d){
    	dict.clear();
    	for(Map.Entry<String,String> e:d.entrySet())
    		dict.put(e.getKey(),e.getValue());

    }
    
	public static void clear(){dict.clear();}
	
	public static boolean isEmpty(){
		if(dict.size()<=0)
			return true;
		
		return false;
	}
}
