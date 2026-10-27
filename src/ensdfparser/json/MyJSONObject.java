package ensdfparser.json;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MyJSONObject extends JSONObject{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
	 * wrap of the JSONObject for using an ordered map since key-value pairs are unordered. 
	 * JSONObject supports java.util.Map interface.
	 */
	//private static final long serialVersionUID = 1L;

    protected LinkedHashMap<String,Object> sortedMap = new LinkedHashMap<String,Object>();
    
    protected Vector<String> orderedKeys=new Vector<String>();
    
	public MyJSONObject() {
		super();
		
		/*
		//This workround is for org.jon.JSONObject but not org.json.simple.JSONObject, 
		//which are two different libraries. They have the same name by coincidence
		try {	
		    JSONObject obj=new JSONObject();
		    
		    Field[] fields=JSONObject.class.getDeclaredFields();
		    for(int i=0;i<fields.length;i++)
		        System.out.println(" field: "+fields[i].getName());
		    
			//Field is a class from the Reflection API. Here it helps us gain access to 
			//the “map” field declared in the JSONObject class. We create a “reflected object.”
			Field changeMap=this.getClass().getDeclaredField("map");
			
			//The "nameValuePairs" map variable in JSONObject is private. We need to make it accessible	
			changeMap.setAccessible(true);
			
			//Now that the variable is accessible, we can change the HashMap<>() to a LinkedHashMap<>()
			changeMap.set(this, new LinkedHashMap<>());

			changeMap.setAccessible(false);
		      
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/
	}

	@SuppressWarnings("unchecked")
    public MyJSONObject(JSONObject obj) {
	    this();
	    this.putAll(obj);
	    this.sortByOrderedKeys();
	}
	
	public boolean containsKey(String key) {
		return sortedMap.containsKey(key);
	}
	
	public Object get(String key) {
		try {
			return sortedMap.get(key);
		}catch(Exception e) {
			
		}
		return null;
	}
	@SuppressWarnings("unchecked")
    public void put(String key,Object value) {
	    try {           
	        super.put(key, value);
	        sortedMap.put(key, value);

	    }catch(Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@SuppressWarnings("unchecked")
    public void putAll(HashMap<String,Object> dataMap) {
	    try {
	        super.putAll(dataMap);
	        
	        sortedMap.putAll(dataMap);
	        
	        //sortLinkedMap();
	    }catch(Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void clear() {
		super.clear();
		sortedMap.clear();
	}
	public String toJSONString() {
	    try {
	        String jsonText = JSONValue.toJSONString(sortedMap);

	        return jsonText;
	    }catch(Exception e) {
	        
	    }
	    
	    return "";
	}

	/*
	 * set object property using the information from dataMap with the key
	 */
	public void setProperty(String key,HashMap<String,Object> dataMap) {
		try {
			if(dataMap.containsKey(key)) {
				this.putIfNotEmpty(key, dataMap.get(key));
			}
		}catch(Exception e) {
			
		}
	}
	
	public String printProperty(String propertyName) {
		try {
			Object obj=get(propertyName);
			return obj.toString();			
		}catch(Exception e) {
			
		}
		return "";
	}
	
	@SuppressWarnings("rawtypes")
	public void putIfNotEmpty(String key,Object obj) {
		try {
			if(obj==null)
				return;
			
			if(obj instanceof String) { 
				if( ((String)obj).isEmpty())
					return;				
			}else if(obj instanceof JSONObject){
				if( ((JSONObject)obj).isEmpty() )
					return;
			}else if(obj instanceof Map) {
				if( ((Map)obj).isEmpty() )
					return;
			}else if(obj instanceof JSONArray) {
				if( ((JSONArray)obj).isEmpty() )
					return;
			}
			
			put(key,obj);
		}catch(Exception e) {
			
		}

	}
	/*
	 * return type: "S" for String, "B" for boolean, "O" for object, "I" for integer, "F" for float, "D" for double
	 */

	public Object get(HashMap<String,Object> dataMap,String key,String returnType) {
		try {
			String t=returnType.toUpperCase().trim();
			
			if(dataMap.containsKey(key)) {
				Object val=dataMap.get(key);
				if(t.startsWith("S")) {//get string representation of the object if it is not string and it has string representation, like number
					if(val instanceof Integer || val instanceof Double || val instanceof Float || val instanceof Long) {
						return String.valueOf(val);
					}else 
						return val;
				}else {
					if(val instanceof Integer || val instanceof Double || val instanceof Float || val instanceof Long) {
						String s=String.valueOf(val);						
						
						if(t.startsWith("D"))
							return Double.parseDouble(s);
						if(t.startsWith("F"))
							return Float.parseFloat(s);
						if(t.startsWith("I")) {
							return ((int)Double.parseDouble(s));
						}
					}else if(val instanceof String) {
					    //System.out.println(" val="+val);
					    
                        String s=(String)val;
                        if(t.startsWith("D"))
                            return Double.parseDouble(s);
                        if(t.startsWith("F"))
                            return Float.parseFloat(s);
                        if(t.startsWith("I")) 
                            return ((int)Double.parseDouble(s));                          
                        if(t.startsWith("B"))
                            return Boolean.parseBoolean(s);
					}
						
					return dataMap.get(key);
				}
			}
					
			if(t.isEmpty() || t.startsWith("O"))
				return dataMap.get(key);
			if(t.startsWith("S"))
				return "";
			if(t.startsWith("B"))
				return false;
			if(t.startsWith("I"))
				return new Integer(-1);
			if(t.startsWith("F"))
				return new Float(-1);
			if(t.startsWith("D"))
				return new Double(-1);
		}catch(Exception e) {
			//e.printStackTrace();
		}
		
		return null;
	}	

    public Object get(HashMap<String,Object> dataMap,String key) {
        return get(dataMap,key,"");
    }
    
    @SuppressWarnings("unchecked")
    public Object get(String key,String returnType) {
        HashMap<String,Object> dataMap=new HashMap<String,Object>();
        dataMap.putAll(this);
        return get(dataMap,key,returnType);
    }
    
    public Object remove(Object key) {
    	super.remove(key);
    	return sortedMap.remove(key);
    }
    
    public boolean remove(Object key,Object value) {
    	super.remove(key, value);
    	return sortedMap.remove(key,value);
    }
    public void sortByOrderedKeys() {
    	sortByOrderedKeys(orderedKeys);
    }
    public void sortByOrderedKeys(Vector<String> orderedKeys) {
    	if(orderedKeys.size()>0) {
        	LinkedHashMap<String,Object> tempMap=new LinkedHashMap<String,Object>();
        	tempMap.putAll(sortedMap);
        	
        	//System.out.println("  1"+this);
        	
        	this.clear();
        	for(String key:orderedKeys) {
        		if(tempMap.containsKey(key)) {
        			//sortedMap.put(key, tempMap.get(key));
        			this.put(key, tempMap.get(key));
        			tempMap.remove(key);

        		}
        	}
        	
        	//sortedMap.putAll(tempMap);
        	this.putAll(tempMap);

        	//System.out.println("  2"+this);
    	} 	
    }

    /*
     * sort this map and sub-map
     */
    public void sortAll() {
        try {
            sortByOrderedKeys();
        }catch(Exception e) {
            
        }
    }
 
    public void naturalSort() {
        try {
            Comparator<Entry<String,Object>> comparator= new Comparator<Entry<String,Object>>() {
                @Override
                public int compare(Entry<String,Object> entry1, Entry<String,Object> entry2) {
                    String key1=(String)entry1.getKey();
                    String key2=(String)entry2.getKey();
                    return key1.compareTo(key2);
                }
            };  
            
            sortByComparator(comparator);
            
        }catch(Exception e) {
            
        }
    }
    
    
    /*
     * sort map by customized comparator
     */
	@SuppressWarnings({ "unchecked" })
	public void sortByComparator(Comparator<Entry<String, Object>> comparator) {
        try {

        	Set<Entry<String, Object>> entries = this.entrySet();
            
            // Sort method needs a List, so let's first convert Set to List in Java
            List<Entry<String, Object>> listOfEntries=new ArrayList<Entry<String, Object>>(entries);
            
            // sorting HashMap by values using comparator
            Collections.sort(listOfEntries, comparator);
            
            //sortedMap.clear();
            this.clear();
            for(Entry<String, Object> entry : listOfEntries){
                //sortedMap.put(entry.getKey(), entry.getValue());
                this.put(entry.getKey(), entry.getValue());
            }
            
        }catch(Exception e) {
            e.printStackTrace();
        }
	}    
	
    public LinkedHashMap<String,Object> getSortedMap(Comparator<Entry<String, Object>> comparator) {
        sortByComparator(comparator);
        return sortedMap;
    }
	
    public LinkedHashMap<String,Object> getSortedMap() {
        return sortedMap;
    }
    
	/*
	 * sort by keys (natural order)
	 */
	public LinkedHashMap<String,Object> getNaturalSortedMap() {

	    naturalSort();
        return sortedMap;
	}
	

    public static MyJSONObject defaultSortAll(JSONObject obj) {
        try {
            MyJSONObject jsonObj=new MyJSONObject(obj);
            if(jsonObj.isEmpty())
                return null;
            
            jsonObj.naturalSort();
            
            return jsonObj;
        }catch(Exception e) {
            
        }
        return null;
    }
	   

    
	public void writeJSONFile(String outfilePath) {
        FileWriter file=null;
        try {
            // Constructs a FileWriter given a file name, using the platform's default charset
            file = new FileWriter(outfilePath);           
            file.write(this.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }       
    }    
}
