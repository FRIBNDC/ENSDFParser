package ensdfparser.nsr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.exception.ExceptionUtils;

import ensdfparser.nds.util.Str;

public class BaseNSR {
    protected static boolean useOldNSRLinkManager=true;
	
    public static boolean verbose=false;
	
	protected static Connection con;
	
	protected static Vector<String> nomatchKeynos=new Vector<String>();
	
	//------------------------------------------------------------------------
	//For NSR retrieval page: old NSR webpage (before June 2024)
	public static final String FROM_YEAR_NAME="yrlo";//for publication year
	public static final String TO_YEAR_NAME="yrhi";
	
	public static final String PRIMARY_NAME="prim";
	public static final String VIEWALL_NAME="nsrev";
	public static final String MEASUREDONLY_NAME="expr";
	
	//format option by old NSR before June 2024
	public static final String HTML_FORMAT="html";//changed from "Normal" to "html" 02/27/2025 (note that case-sensitive)
	public static final String BIBTEX_FORMAT="bibTex";
	public static final String TEXT_FORMAT="text";
	public static final String KEYNUMBER_FORMAT="keynum";
	public static final String EXCHANGE_FORMAT="exchange";//changed from "Exchange" to "exchange" 02/27/2025
	
	
	//available format by 2024 NSR: keynum,json,csv,text,bibtex,exchange 
	public static final String CSV_FORMAT="csv";
	public static final String JSON_FORMAT="json";

	
	protected static Vector<String> outputFormatsV=new Vector<String>(Arrays.asList(
			KEYNUMBER_FORMAT,
			EXCHANGE_FORMAT,
			JSON_FORMAT,
			CSV_FORMAT,
			HTML_FORMAT,
			BIBTEX_FORMAT,
			TEXT_FORMAT
	));
	
	public static final String YEARORDER_ASCENDING="Ascending";
	public static final String YEARORDER_DESCENDING="Descending";
	
	public static final String PARAM1_SELECTOR_NAME="qwhat1";	
	public static final String PARAM1_NAME="param1";	
	public static final String PARAM2_SELECTOR_NAME="qwhat2";
	public static final String PARAM2_NAME="param2";	
	public static final String PARAM3_SELECTOR_NAME="qwhat3";
	public static final String PARAM3_NAME="param3";	
	//------------------------------------------------------------------------
	   
	///////////////////////////////////////////////////////////////
	//for NSR after June 2024
	
	public static final String AUTHOR_LABEL="author";
	public static final String FIRST_AUTHOR_LABEL="first-author";
	public static final String NUCLIDE_LABEL="nuclide";
	public static final String NUCLIDE_TARGET_LABEL="nuclide-target";
	public static final String NUCLIDE_PARENT_LABEL="nuclide-parent";
	public static final String NUCLIDE_DAUGHTER_LABEL="nuclide-daughter";
	public static final String SUBJECT_LABEL="subject";
	public static final String SUBJECT_TYPE_LABEL="subject-type";//measured; deduced;calculated
	public static final String REACTION_LABEL="reaction";
	public static final String INCIDENT_BEAM_LABEL="incident-beam";
	public static final String OUTGOING_BEAM_LABEL="outgoing-beam";
	public static final String JOURNAL_LABEL="journal";
	public static final String TOPIC_LABEL="topic";
	public static final String Z_VALUE_LABEL="z-value";
	public static final String Z_RANGE_LABEL="z-range";
	public static final String A_VALUE_LABEL="a-value";
	public static final String A_RANGE_LABEL="a-range";//30-50
	public static final String YEAR_MIN_LABEL="year-min";
	public static final String YEAR_MAX_LABEL="year-max";
	public static final String ENTRY_DATE_LABEL="entry-date";//2024-06-02
	public static final String PRIMARY_ONLY_LABEL="primary-only";
	
	public static final String OUTPUT_FORMAT_LABEL="output-format";
	
	public static final String INDEXED_SEARCH_URL="https://www.nndc.bnl.gov/nsr/IndexedSearchServlet?";
	
	/*
	//available servlet by new NSR 2024 (provided by Ben Shu, 6/14/2024): 
	 -	KeyNumberSearchServlet
     -	DOISearchServlet
     -	IndexedSearchServlet
     -	QuickSearchServlet
     -	RecentReferenceServlet
     -	TextSearchServlet
     
     example usage:
     https://www.nndc.bnl.gov/nsr/IndexedSearchServlet?first-author=j.chen&year-min=2000&output-format=keynum
     
     without "output-format" option, it will return json
	 */
	
	public static ArrayList<String> ParamNameList=new ArrayList<String>(Arrays.asList(
			AUTHOR_LABEL,          
			FIRST_AUTHOR_LABEL,    
			NUCLIDE_LABEL,         
			NUCLIDE_TARGET_LABEL,  
			NUCLIDE_PARENT_LABEL,  
			NUCLIDE_DAUGHTER_LABEL,
			SUBJECT_LABEL,         
			SUBJECT_TYPE_LABEL,    
			REACTION_LABEL,        
			INCIDENT_BEAM_LABEL,   
			OUTGOING_BEAM_LABEL,   
			JOURNAL_LABEL,         
			TOPIC_LABEL,           
			Z_VALUE_LABEL,         
			Z_RANGE_LABEL,         
			A_VALUE_LABEL,         
			A_RANGE_LABEL,         
			YEAR_MIN_LABEL,        
			YEAR_MAX_LABEL,        
			ENTRY_DATE_LABEL,      
			PRIMARY_ONLY_LABEL    

			));

	
	private static String exceptionMsg="";
	public static String getExcpetionMsg() {return exceptionMsg;}
	public static void clearExceptionMsg() {exceptionMsg="";}
	
	public static class SearchParameter{
		public int yearRangeL=1857;//for publication year
	    public int yearRangeR=LocalDate.now().getYear();
	    
	    public boolean primaryOnly=false;
	    public boolean viewAll=true;
	    public boolean measuredOnly=false;
	    
	    public String outputFormat=EXCHANGE_FORMAT;
	    public String yearOrder=YEARORDER_DESCENDING;
	    
	    //////////////////////////////////////////
	    // for NSR search form before June 2024
	    public String param1Option="Nuclide";//default option, see NSR search page for a full list
	    public String param1Value="";//e.g., A=100, 100Ru, ...
	    
	    public String param2Option="";
	    public String param2Value="";
	    public String param3Option="";
	    public String param3Value="";
	    //////////////////////////////////////////
	    
	    //////
	    //New option for NSR search after June 2024
	    
	    public HashMap<String,String> paramOptionValueMap=new HashMap<String,String>();
	    
	    public void addSearchCondition(String option,String value) {
	    	option=option.trim().toLowerCase();
	    	value=value.trim().toLowerCase();
	    	boolean toAdd=false;
	    	
	    	if(isGoodParamName(option)) {
	    		toAdd=true;
	    	}else if(option.contains("first") && option.contains("author")) {
	    		toAdd=true;
	    		option=FIRST_AUTHOR_LABEL;
	    	}
	    		
	    	
	    	if(toAdd) {
	    		if(option.equals("nuclide") && value.contains("=")) {
	    			//nuclide=A=30 with old NSR is not acceptable by new NSR
	    			int n=value.indexOf("=");
	    			String s1=value.substring(0,n).trim();
	    			String s2=value.substring(n+1).trim();
		
	    			//System.out.println(s1+"  "+s2);
	    			
    				if(s1.length()==1 && "az".contains(s1)) {
    	    			String label="";
    	    			n=s2.indexOf("-");	
    	    			
    	    			if(Str.isInteger(s2)) {
    	    				label="value";
    	    			}else if(n>0) {
    	    				label="range";
    	    			}
    	    			
    	    			if(label.length()>0) {
        					option=s1+"-"+label;
        					value=s2;
    	    			}
    				}
	    		}
	    		paramOptionValueMap.put(option, value);
	    	}
	    }
	    public void removeSearchCondition(String option) {
	    	try {
	    		option=option.trim().toLowerCase();
		    	paramOptionValueMap.remove(option);	
	    	}catch(Exception e) {
	    		
	    	}

	    }
	    /**
	     * Generate a short summary string for this search parameter
	     * Example: "37Cl [RAD] (measured)"
	     */
	    public String getShortSummary() {
	        StringBuilder sb = new StringBuilder();
	        
	        // 1. Check parameter map (new style)
	        if (paramOptionValueMap != null && !paramOptionValueMap.isEmpty()) {
	            String nuclide = paramOptionValueMap.get(NUCLIDE_LABEL);
	            String topic = paramOptionValueMap.get(TOPIC_LABEL);
	            String subjectType = paramOptionValueMap.get(SUBJECT_TYPE_LABEL);
	            String author = paramOptionValueMap.get(AUTHOR_LABEL);
	            String yearMin = paramOptionValueMap.get(YEAR_MIN_LABEL);
	            String yearMax = paramOptionValueMap.get(YEAR_MAX_LABEL);
	            String reaction = paramOptionValueMap.get(REACTION_LABEL);
	            String journal = paramOptionValueMap.get(JOURNAL_LABEL);
	            String firstAuthor = paramOptionValueMap.get(FIRST_AUTHOR_LABEL);
	            
	            // Nuclide (keep original case: 37Cl, 37Ar, 37K, etc.)
	            if (nuclide != null && !nuclide.isEmpty()) {
	                sb.append(nuclide.toUpperCase());  // Preserves original case
	            }
	            
	            // Topic (keep original case: RAD, ATOMIC_MASSES, NUCLEAR_REACTIONS, etc.)
	            if (topic != null && !topic.isEmpty()) {
	                if (sb.length() > 0) sb.append(" ");
	                sb.append("[").append(topic.toUpperCase()).append("]");  // Preserves original case
	            }
	            
	            // Subject type (keep original case: measured, deduced, calculated, etc.)
	            if (subjectType != null && !subjectType.isEmpty()) {
	                if (sb.length() > 0) sb.append(" ");
	                sb.append("(").append(subjectType).append(")");  // Preserves original case
	            }
	            
	            // Author (keep original case)
	            if (author != null && !author.isEmpty()) {
	                if (sb.length() > 0) sb.append(" ");
	                sb.append("by ").append(author);  // Preserves original case
	            }
	            
	            // First Author (keep original case)
	            if (firstAuthor != null && !firstAuthor.isEmpty()) {
	                if (sb.length() > 0) sb.append(" ");
	                sb.append("first: ").append(firstAuthor);  // Preserves original case
	            }
	            
	            // Journal (keep original case)
	            if (journal != null && !journal.isEmpty()) {
	                if (sb.length() > 0) sb.append(" ");
	                sb.append("in ").append(journal);  // Preserves original case
	            }
	            
	            // ---- Year range: ONLY show if NOT default ----
	            if (!isDefaultYear(yearMin, yearMax) && yearMin != null && !yearMin.isEmpty()) {
	                if (sb.length() > 0) sb.append(" ");
	                if (yearMax != null && !yearMax.isEmpty() && !yearMax.equals(yearMin)) {
	                    sb.append(yearMin).append("-").append(yearMax);
	                } else {
	                    sb.append(yearMin);
	                }
	            }
	            
	            // Reaction (keep original case)
	            if (reaction != null && !reaction.isEmpty()) {
	                if (sb.length() > 0) sb.append(" ");
	                sb.append("(").append(reaction).append(")");  // Preserves original case
	            }
	        }
	        
	        // 2. Fallback: use param1, param2, param3 (old style)
	        if (sb.length() == 0) {
	            if (param1Value != null && !param1Value.isEmpty()) {
	                sb.append(param1Value);  // Preserves original case
	            }
	            if (param2Value != null && !param2Value.isEmpty()) {
	                if (sb.length() > 0) sb.append(" + ");
	                sb.append(param2Value);  // Preserves original case
	            }
	            if (param3Value != null && !param3Value.isEmpty()) {
	                if (sb.length() > 0) sb.append(" + ");
	                sb.append(param3Value);  // Preserves original case
	            }
	        }
	        
	        // 3. If still empty, return a default
	        if (sb.length() == 0) {
	            return "NSR search";
	        }
	        
	        return sb.toString();
	    }
	    
	    @Override
	    public SearchParameter clone() {
	        SearchParameter clone = new SearchParameter();
	        
	        // Primitive fields
	        clone.yearRangeL = this.yearRangeL;
	        clone.yearRangeR = this.yearRangeR;
	        clone.primaryOnly = this.primaryOnly;
	        clone.viewAll = this.viewAll;
	        clone.measuredOnly = this.measuredOnly;
	        
	        // String fields
	        clone.outputFormat = this.outputFormat;
	        clone.yearOrder = this.yearOrder;
	        clone.param1Option = this.param1Option;
	        clone.param1Value = this.param1Value;
	        clone.param2Option = this.param2Option;
	        clone.param2Value = this.param2Value;
	        clone.param3Option = this.param3Option;
	        clone.param3Value = this.param3Value;
	        
	        // Deep copy of HashMap
	        if (this.paramOptionValueMap != null) {
	            clone.paramOptionValueMap = new HashMap<>(this.paramOptionValueMap);
	        } else {
	            clone.paramOptionValueMap = new HashMap<>();
	        }
	        
	        return clone;
	    }

	    /**
	     * Check if year range is at default values
	     */
	    private boolean isDefaultYear(String yearMin, String yearMax) {
	        int defaultMin = 1857;
	        int defaultMax = LocalDate.now().getYear();
	        
	        // If both are null or empty, it's default
	        if ((yearMin == null || yearMin.isEmpty()) && (yearMax == null || yearMax.isEmpty())) {
	            return true;
	        }
	        
	        // Check if values match defaults
	        boolean minIsDefault = true;
	        boolean maxIsDefault = true;
	        
	        if (yearMin != null && !yearMin.isEmpty()) {
	            try {
	                if (Integer.parseInt(yearMin) != defaultMin) {
	                    minIsDefault = false;
	                }
	            } catch (NumberFormatException e) {
	                minIsDefault = false;
	            }
	        }
	        
	        if (yearMax != null && !yearMax.isEmpty()) {
	            try {
	                if (Integer.parseInt(yearMax) != defaultMax) {
	                    maxIsDefault = false;
	                }
	            } catch (NumberFormatException e) {
	                maxIsDefault = false;
	            }
	        }
	        
	        // If both are default (or null), it's default
	        return minIsDefault && maxIsDefault;
	    }
	}

	
    /*
    private static Statement initDatabase_old(){
            String url="jdbc:mysql://ndclx3.nndc.bnl.gov:9100/nsr";
            String usr="webusr";
            String pwd="xyzq197d";
            String driver="com.mysql.cj.jdbc.Driver";
            Statement stmt=null;
            try{
                Class.forName(driver).newInstance();
                con=DriverManager.getConnection(url,usr,pwd);
                stmt=con.createStatement();            
            }catch(Exception e){
                System.out.println("Error connecting to NSR database");
                return null;
            }
            return stmt;
    }
    */
    
	/*
	 * used by searching NSR with JDBC
	 * Not for the new NSR after June 2024
	 */
    protected static Statement initDatabase(String loginFileName,boolean isSilent){
        Statement stmt=null;
        String url="";
        String usr="";
        String pwd="";
        String driver="";
        
        try {
            File f=new File(loginFileName);
            
            if(!isSilent){
                System.out.println("Reading login information for connecting to NSR database...");
                System.out.println("   file: "+f.getAbsolutePath());    
            }

            
            Vector<String> lines=Str.readFile(f);
            String[] params=new String[3];
            String parName="",parValue="";
            for(int i=0;i<lines.size();i++){
                String line=lines.get(i);
                params=Str.trimArray(line.split("=",2));
                if(params.length>=2){
                    parName=params[0].toUpperCase().trim();
                    parValue=params[1].trim();
                    if(parName.contains("DRIVER") && driver.length()==0) 
                        driver=parValue;
                    else if(parName.contains("URL") && url.length()==0)
                        url=parValue;
                    else if((parName.contains("LOGIN")||parName.contains("USER")) && usr.length()==0)
                        usr=parValue;
                    else if(parName.contains("PASS") || pwd.length()==0)
                        pwd=parValue;   
                    
                    if(!isSilent)
                        System.out.println(String.format("%20s%3s%s", parName," = ",parValue));
                }
            }
    
        } catch (Exception e) { 
            if(!isSilent){
                System.out.println("Error reading the login file of NSR database properties");
                e.printStackTrace();
            }

            return null;
        }
        
        try{
            if(!isSilent) System.out.println("Initializing connection to NSR database...");
            
            Class.forName(driver).newInstance();
            con=DriverManager.getConnection(url,usr,pwd);
            stmt=con.createStatement();            
            if(!isSilent) System.out.println("Connection successful!");
        }catch(SQLException se){
            if(!isSilent){
                //Handle errors for JDBC
                System.out.println("Error initializing connection to NSR database");
                se.printStackTrace();
            }

         }catch(Exception e){
             if(!isSilent){
                 System.out.println("Error initializing connection to nsr database");
                 e.printStackTrace();    
             }
            return null;
        }
        
        return stmt;

    }

 
    public static String getFinalURL(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getFinalURL(redirectUrl);
        }
        return url;
    }
    
    /*
     * using URLConnection with "nsrlink.jsp"
     * Download NSR information from NSR database using NSRLink manager via URLConnection
     * The same function is also in ENSDFParser->nds.ensdf.Reference class, except for verbose option
     * 
     * return the NSR output in exchange-format by default
     * works for old NSR and modified to work for new NSR after June 2024
     */
    public static Vector<String> downloadNSRByKeynumber_NSRLink(String keynumber)throws IOException{
    	return downloadNSRByKeynumber_NSRLink(keynumber,"X");
    }
    
    /*
     * return a vector of lines in the NSR output for the input keynumber in a specified format based on mode
     * work for old NSR an modified to work for new NSR after June 2024
     */
    public static Vector<String> downloadNSRByKeynumber_NSRLink(String keynumber,String mode)throws IOException{
    	long startTime,endTime;
    	float timeElapsed;//in second
    	startTime=System.currentTimeMillis();
    	
    	String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
    	   	
   	
    	//NSR Link Manager, return exchange-format ("X" mode), HTML format ("H" mode), redirect to paper page ("B" mode)
    	//process one keynumber only
     	if(mode==null || mode.trim().isEmpty())
     		mode="X";
     	else {
         	mode=mode.trim().toUpperCase();
         	if(mode.length()>1) {
             	if(mode.startsWith("EX"))
             		mode="X";
             	else if(mode.startsWith("H"))
             		mode="H";
             	else if(mode.startsWith("B") || mode.startsWith("REDIRECT"))
             		mode="B";
             	else
             		mode="X";
         	}else if(!"XHB".contains(mode)) {
         		mode="X";
         	}
     	}

     	mode=","+mode;
     	
    	String url = "https://www.nndc.bnl.gov/nsr/?";    	
    	URLConnection connection;
  		
    	//https://www.nndc.bnl.gov/nsr/ExchangeServlet?key=2012WA38
    	//2024 change
    	
    	InputStream response;
    	Vector<String> nsrOutput=new Vector<String>();//store search result for a NSR keynumber (exchange-format)
    	    	
        boolean reachNSRKey=false;

        try{
        	
        	url=url+keynumber+mode;
        	url=convertToNewURL2024(url);
        	
            connection = (HttpURLConnection) new URL(url).openConnection();
        	connection.setRequestProperty("Accept-Charset", charset);

            connection.setConnectTimeout(5000);//5 second
            connection.setReadTimeout(5000);//5 second
            
        	response = connection.getInputStream();
            
        	BufferedReader br=new BufferedReader(new InputStreamReader(response));
        	nsrOutput=new Vector<String>();
        	String str=br.readLine();
        	      	
        	//System.out.println("**** key="+keynumber+" url="+url+"   result="+str);
        	
        	reachNSRKey=false;
        	while(str!=null){       		
         	   if(!reachNSRKey){ 
            	   if((str.contains("&lt;")||str.contains("<")) && str.contains("KEYNO"))
            		   reachNSRKey=true;
         	   }
        	   else if(str.contains("</pre>") || str.trim().isEmpty())
        		   break;
          	   
         	   if(reachNSRKey && str.trim().length()>0) 
         		   nsrOutput.addElement(str);
         	   
         	   str=br.readLine();
         	  
        	}
        	br.close();

        	
        	if(nsrOutput.size()<=0){
        		System.out.println("Warning: Nothing is dowloaded from NSR database for keynumber="+keynumber);
        		return nsrOutput;
        	}
        		
        	nsrOutput.add(0, "<pre>");
        	nsrOutput.add("</pre>");
        			
            
        }catch(ConnectException e){
        	e.printStackTrace();
        	System.out.println("*** Error: can't connect to NNDC for NSR web query"); 
        }catch(Exception e){ 
        	e.printStackTrace();
        	String s=ExceptionUtils.getStackTrace(e);
        	if(exceptionMsg.isEmpty())
        		exceptionMsg=s;
        	else
        		exceptionMsg+="\n\n"+s;
        	
        	System.out.println("*** Error: something wrong with keynumber="+keynumber); 
        }

        if(verbose) {
            endTime=System.currentTimeMillis();
            timeElapsed=(float)(endTime-startTime)/1000;
            System.out.println("Time elapsed for downloading NSR by keynumber: "+String.format("%.3f", timeElapsed)+" seconds");
        }

        return nsrOutput;               
    }
    
   
    /*
     * Using NSR link manager to download NSR outputs for a list of keynumbers
     *  
     * return a vector of NSR outputs each of which is a vector of lines in the NSR output for a keynumber
	 * works for old NSR and new NSR after June 2024
	 * 
	 * NOT RECOMMENDED: a connection has to be made for each keynumber. Connection might be rejected by the server
	 *                  when there are many keynumbers 
     */
    public static Vector<Vector<String>> downloadNSRsByKeynumbersV_NSRLink(Vector<String> keynumbers)throws IOException{
    	long startTime,endTime;
    	float timeElapsed;//in second
    	startTime=System.currentTimeMillis();
    	
    	Vector<String> nsr;//store search result for a NSR keynumber (HTML source page)
    	
    	Vector<Vector<String>> NSRs=new Vector<Vector<String>>();

        for(int i=0;i<keynumbers.size();i++){

            nsr=downloadNSRByKeynumber_NSRLink(keynumbers.get(i));
            
            if(nsr.size()<=0)
            	continue;
            
            NSRs.add(nsr);
        }
        
        if(verbose) {
            endTime=System.currentTimeMillis();
            timeElapsed=(float)(endTime-startTime)/1000;
            System.out.println("Time elapsed for downloading NSRs by keynumbers: "+String.format("%.3f", timeElapsed)+" seconds");
        }
       
        return NSRs;               
    }
    
    /*
     * download NSR output for input keynumbers using JDBC
     * 
     * return a vector of NSR outputs each of which is a vector of lines in the NSR output for a keynumber
	 * works for old NSR only
     */
    public static Vector<Vector<String>> downloadNSRsByKeynumbersV_JDBC(Vector<String> keynumbers,String loginFileName,boolean isSilent)throws Exception{
        Vector<Vector<String>> nsrV=new Vector<Vector<String>>();   
        
        Statement stmt=null;
        String sql="select line_text from exch_tbl where keyno='";

        try{
            stmt=initDatabase(loginFileName, isSilent);}
        catch(Exception e ){
            System.out.println("Error initializing access to NSR database");
            if(!isSilent){               
                e.printStackTrace();
            }
        }
        
        Vector<String> nsrOutput=new Vector<String>();//store search result for a NSR keynumber (HTML source page)
        ResultSet rs=null;
        String s="";
        
        try{  
            for(int i=0;i<keynumbers.size();i++){                           
                rs=stmt.executeQuery(sql+keynumbers.get(i)+"'");                
                nsrOutput=new Vector<String>();
                
                while(rs.next()){
                    s=rs.getString("line_text").trim();
                    if(s.trim().indexOf("<")==0 && s.trim().indexOf(">")>0){
                        int p=s.indexOf(">");
                        String s1=s.substring(0, p+1).replace("<", "&lt;").replaceAll(">", "&gt;");
                        String s2=s.substring(p+1);
                        s=s1+s2;
                        
                        nsrOutput.add(s);
                    }                 
                }
                
                
                if(nsrOutput.size()>0){
                    //nsrOutput.add(0,"&lt;KEYNO   &gt;"+keynos[i]);;
                    nsrOutput.add(0, "<pre>");
                    nsrOutput.add("</pre>"); 
                    
                    nsrV.add(nsrOutput);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
       
        return nsrV;
    }
    
    /* 
     * using URLConnection with "knum_act.jsp"
     * Download reference information from NSR for a given keynumber
     * 
     * return a vector of lines in the NSR output for the input keynumber
     * works for old NSR and new NSR after June 2024
     */
    public static Vector<String> downloadNSRByKeynumber(String keynumber)throws IOException{
    	
    	Vector<String> inputKeynos=new Vector<String>();
    	inputKeynos.add(keynumber);
    	
    	Vector<Vector<String>> temp=downloadNSRsByKeynumbersV(inputKeynos);
    	
    	if(temp.size()>0)
    	    return temp.get(0);
    	
    	return new Vector<String>();
    }
 
    /*
	 * Same as downloadNSRsByKeynumbers_URLC function in ENSDFParser->nds.ensdf.Reference class
	 * 
	 * return a vector of NSR outputs each of which is a vector of lines in the NSR output for 
	 *        a keynumber in the input keynumber array
	 * works for old NSR and new NSR after June 2024
     */
    public static Vector<Vector<String>> downloadNSRsByKeynumbers(String[] keynumbers)throws IOException{
    	Vector<String> keynosV=new Vector<String>();
    	for(int i=0;i<keynumbers.length;i++)
    		keynosV.add(keynumbers[i]);
    	
    	return downloadNSRsByKeynumbersV(keynosV);
    }    

    
    /*
     * convert the query URL for old NSR to the corresponding new query URL 
     *     working for the new NSR after June 2024
     */
    public static String convertToNewURL2024(String oldURL) {
    	String newURL=oldURL;
    	String baseURL="https://www.nndc.bnl.gov/nsr/";
    	if(!oldURL.startsWith(baseURL))
    		return oldURL;
    	
    	String s=oldURL.substring(baseURL.length()).trim();
    	if(s.isEmpty())
    		return oldURL;
    	
    	newURL=baseURL;
    	
    	//System.out.println(s);
    	if(s.startsWith("nsrlink.jsp?") || s.startsWith("?") || s.startsWith("knum_act")) {
    		int n=s.indexOf("?");
    		s=s.substring(n+1).trim();
    		String[] temp=s.split("[,]");//2021WA16,X
    		    		
    		if(temp.length==2 && temp[0].length()==8 && temp[1].length()==1) {
    			String keyno=temp[0];
    			String mode=temp[1].toUpperCase();
    			if(mode.equals("X")) {
    				//get exchange format of NSR entries with </pre>, &lt; (HTML format)
    				//In new link in 2024, no </pre>, &lt; (pure text format)
    				newURL=baseURL+"KeyNumberSearchServlet?key="+keyno+"&output-format=exchange";
    			}else if(mode.equals("B")) {
    				//newURL=baseURL+"?search-type=keynumber&key="+keyno+"&redirect=true";
    				newURL=baseURL+"DOIRedirectServlet?key="+keyno;
    				
    			}else if(mode.equals("H")) {
    				newURL=baseURL+"?search-type=keynumber&key="+keyno;
    			}
    		}else if(s.contains("keylst") && s.contains("ofrml") && s.contains("getkl=Search")) {
        		//search NSR by keynumbers
        		//new: https://www.nndc.bnl.gov/nsr/?search-type=keynumber&key=2021Wa16&key=2012Wa38
        		//old: https://www.nndc.bnl.gov/nsr/knum_act.jsp?ofrml=Exchange&keylst=2021Wa16%0D%0A2012Wa38%0D%0A&getkl=Search
    			String s0=s;
    			
    			int n1=s.indexOf("keylst");
    			int n2=s.indexOf("&getkl");
    			s=s.substring(n1+7,n2).trim();
    			
    			s=s.replace("%0D%0A",",");
    			
    			String[] temp1=s.split("[,]+");
    			newURL=baseURL+"KeyNumberSearchServlet?";
    			for(int i=0;i<temp1.length;i++) {
    				if(temp1[i].length()==8)
    					newURL+="key="+temp1[i]+"&";
    			}
    			//if(newURL.endsWith(","))
    			//	newURL=newURL.substring(0,newURL.length()-1);
    			n1=s0.indexOf("ofrml=");
    			n2=s0.indexOf("&",n1);
    			String format="exchange";
    			
    			//System.out.println(n1+"  "+n2+"  "+s0);
    			if(n1>=0) {
    				if(n2<0)
    					n2=s0.length();
    				s=s0.substring(n1+6,n2).trim().toLowerCase();
    				if(!outputFormatsV.contains(s))
    					format="exchange";
    				else
    					format=s;
    			}
    			//System.out.println(format);
    			newURL+="output-format="+format;
    		}
    		
    	}
    	
    	//System.out.println(newURL);
    	
    	return newURL;
    }
    
    public static Vector<String> parseNSRExchangeOutput(String nsrExchangeOutput){
    	Vector<String> nsrOutput=new Vector<String>();
    	try {
    		String out=nsrExchangeOutput;
    		
	    	String[] temp=out.split("\n");
	    	boolean reachNSRKey=false;
	    	for(int i=0;i<temp.length;i++){
	    		String line=temp[i];
	    		if(line==null)
	    			continue;
	    		
	    		//System.out.println(line);
	    		
	    		if(!reachNSRKey){ 		            	   
	    			if((line.contains("&lt;")||line.contains("<")) && line.contains("KEYNO"))	            		  
	    				reachNSRKey=true;
	    		}else if(line.contains("</pre>") || line.trim().isEmpty())		        		   
	    			break;		         	   		     
	         	
	    		if(reachNSRKey && line.trim().length()>0) 		         		  
	    			nsrOutput.addElement(line);

	    	}
    	}catch(Exception e) {
    		
    	}
    	
    	return nsrOutput;
    }
    
    //StringBuffer sb must contain contents obtained using NSR Link Manager with "X" mode
    //added by Jun 8/26/2015
    public static HashMap<String,String> parseNSR(Vector<String> nsrOutput) throws Exception{
        try {
            for(String line:nsrOutput) {
                if(line.contains("KEYNO")) {
                    if(line.contains("&lt;"))
                        return parseNSRasXML(nsrOutput);
                    else if(line.contains("<KEYNO"))
                        return parseNSRasText(nsrOutput);
                }
            }
        }catch(Exception e) {
            
        }
        
        return new HashMap<String,String>();
    }

    //nsrOutput must be extracted from the NSR output as text containing no "&lt;" and "&gt" 
    //StringBuffer sb must contain contents obtained using NSR Link Manager with "X" mode
    //added by Jun 12/9/2022
    public static HashMap<String,String> parseNSRasText(Vector<String> nsrOutput) throws Exception{
        HashMap<String,String> hm=new HashMap<String,String>();
        
        int size=nsrOutput.size();
        if(size<=0)
     	   return hm;
        
        Vector<String> tempV=new Vector<String>();
        tempV.addAll(nsrOutput);
        tempV.add("</pre>");//add as end in case none
        
        String temp="";
        String value="";
        String key="";
        String separator="$$$";
        
        int n1,n2;
        boolean reachNSRKey=false;
        boolean done=false;
        
        key="";
        for(int i=0;i<size;i++){
     	   temp=nsrOutput.elementAt(i);
     	      	    	   
     	   if(!reachNSRKey) 
         	   if(temp.contains("<KEYNO"))
         		   reachNSRKey=true;
         	   else
         		   continue;
     	   else if(temp.contains("BibTex"))
     		   break;
        
     	   n1=temp.indexOf("<");
     	   n2=temp.indexOf(">");
     	   
     	   //System.out.println(temp+"  n1="+n1+"  n2="+n2+" key="+key+" value="+value+" done="+done);
     	   
     	   //read a new entry (reading previous entry is done)
     	   if(n1>=0 && n2>n1){
     		   done=true;
         	   if(key!=""){
         		   if(hm.containsKey(key)) {
         			   value=hm.get(key)+separator+value;
         			   separator="";
         		   }
         		   hm.put(key, value);
         	   }
         	   
     	       value=temp.substring(n2+1).replace("\n", "").replace("\r", "");
         	   if(temp.contains("KEYNO"))
         		   key="KEYNUMBER";
         	   else if(temp.contains("HISTORY"))
         		   key="HISTORY";
         	   else if(temp.contains("CODEN"))
         		   key="CODEN";
         	   else if(temp.contains("REFRENCE"))
         		   key="REFERENCE";
         	   else if(temp.contains("AUTHORS"))
         		   key="AUTHORS";
         	   else if(temp.contains("TITLE"))
         		   key="TITLE";
         	   else if(temp.contains("KEYWORDS"))
         		   key="KEYWORDS";
         	   else if(temp.contains("SELECTRS"))
         		   key="SELECTORS";
               else if(temp.contains("DOI"))
            	   key="DOI";
         	   else if(temp.contains("</pre")){//finish reading the last entry
             	   if(key!="" && value.trim().length()>0){
             		   if(hm.containsKey(key)) {//mutiple KEYWORDS or SELECTORS could exist
             			   value=hm.get(key)+separator+value;
             			   separator="";
             		   }
             		   hm.put(key, value);
             	   }
             	   break;
         	   }else {
         		   key="";
         	   }
         	   
         	   if(key.length()>0 && hm.containsKey(key))
         		   separator="$$$";
     	   }else
     		   done=false;
     	       	   
     	   if(!done)
     		   value+=temp;
     	     		      	   
        }

        //System.out.println(hm.get("KEYNUMBER"));
        
        return hm;
     }  
    
    //nsrOutput must be extracted from the NSR output as XML containing "&lt;" for "<", "&gt" for ">" 
    //StringBuffer sb must contain contents obtained using NSR Link Manager with "X" mode
    //added by Jun 12/9/2022
    public static HashMap<String,String> parseNSRasXML(Vector<String> nsrOutput) throws Exception{
       HashMap<String,String> hm=new HashMap<String,String>();
       
       int size=nsrOutput.size();
       if(size<=0)
           return hm;
       
       String temp="";
       String value="";
       String key="";
       String separator="$$$";
       
       int index;
       boolean reachNSRKey=false;
       boolean done=false;
       
       key="";
       for(int i=0;i<size;i++){
           temp=nsrOutput.elementAt(i);
                           
           if(!reachNSRKey) 
               if((temp.contains("&lt;")||temp.contains("<")) && temp.contains("KEYNO"))
                   reachNSRKey=true;
               else
                   continue;
           else if(temp.contains("BibTex"))
               break;
       
           
           index=temp.indexOf("&gt;");
           int offset=4;
           if(index<0) {
        	   index=temp.indexOf(">");
        	   offset=1;
           }
           
           //read a new entry (reading previous entry is done)
           if(index>0){
               done=true;
               if(key!="" && value.trim().length()>0){
                   if(hm.containsKey(key)) {
                       value=hm.get(key)+separator+value;
                       separator="";
                   }                       
                   hm.put(key, value);
               }
               
               value=temp.substring(index+offset).replace("\n", "").replace("\r", "");
               if(temp.contains("KEYNO"))
                   key="KEYNUMBER";
               else if(temp.contains("HISTORY"))
                   key="HISTORY";
               else if(temp.contains("CODEN"))
                   key="CODEN";
               else if(temp.contains("REFRENCE"))
                   key="REFERENCE";
               else if(temp.contains("AUTHORS"))
                   key="AUTHORS";
               else if(temp.contains("TITLE"))
                   key="TITLE";
               else if(temp.contains("KEYWORDS"))
                   key="KEYWORDS";
               else if(temp.contains("SELECTRS"))
                   key="SELECTORS";
               else if(temp.contains("DOI"))
            	   key="DOI";
               else
                   key="";
               
               if(key.length()>0 && hm.containsKey(key))
                   separator="$$$";
           }
           else if(temp.contains("</pre") || temp.trim().isEmpty()){//finish reading the last entry
               if(key!=""){
                   if(hm.containsKey(key) && value.trim().length()>0) {//mutiple KEYWORDS or SELECTORS could exist
                       value=hm.get(key)+separator+value;
                       separator="";
                   }
                   hm.put(key, value);
               }
               
               break;
           }
           else
               done=false;
                   
           if(!done)
               value+=temp;
                               
       }

       return hm;
    }
    

    /* 
     * using URLConnection with all keynumbers searched at one time
     * Download reference information from NSR for a given list of keynumbers 
	 * wrap of downloadNSRsByKeynumbersV100() for processing >100 keynumbers, since that function can only process up to
	 * 100 keynumbers (even the option "view all" is selected; don't know the reason so far)
	 * Same as downloadNSRsByKeynumbersV_URLC function in ENSDFParser->nds.ensdf.Reference class, except for verbose option
	 * 
	 * return a vector of NSR outputs each of which is a vector of lines in the NSR output for a keynumber
	 * works for old NSR and new NSR after June 2024
	 */  
    public static Vector<Vector<String>> downloadNSRsByKeynumbersV(Vector<String> keynumbersV)throws IOException{
    	Vector<Vector<String>> nsrV=new Vector<Vector<String>>();
    	Vector<Vector<String>> tempV=new Vector<Vector<String>>();
 
    	
    	long startTime,endTime;
    	float timeElapsed;//in second
    	startTime=System.currentTimeMillis();
    	
    	
        int LIMIT=100;//limit of numbers for NSR search in a single page (even if I set nsrev=yes for "view all")

        int size=keynumbersV.size();       
        int nRuns=size/LIMIT;

    	if(size<=0){
    		System.out.println("Warning: No keynumbers to search for from NSR database");
    		return nsrV;
    	}

        if(size%LIMIT!=0)
        	nRuns+=1;
        
        nomatchKeynos.clear();

        Vector<String> tempKeynos=new Vector<String>();;
        int start=0,end=0;
    	for(int i=0;i<nRuns;i++){
    		start=i*LIMIT;
    		end=(i+1)*LIMIT;
    		
    		if(end>=size)
    			end=size;
    		
    		tempKeynos.clear();
    		tempKeynos.addAll(keynumbersV.subList(start,end));
    		
    		tempV=downloadNSRsByKeynumbersV100_URLC(tempKeynos);
    		
    		nsrV.addAll(tempV);
    	}
    	
    	//System.out.println(keynumbersV.size()+"###"+nsrV.size());
    	
		//for(int i=0;i<nsrV.get(0).size();i++)
		//	System.out.println(nsrV.get(0).get(i));
		
        
    	if(nomatchKeynos.size()>0){
    		System.out.println("Warning: Following keynumbers are not found in NSR database:");
    		for(int i=0;i<nomatchKeynos.size();i++)
    			System.out.println(nomatchKeynos.get(i));
    	}
    	
    	if(verbose) {
            endTime=System.currentTimeMillis();
            timeElapsed=(float)(endTime-startTime)/1000;
            System.out.println("Time elapsed for downloading NSRs by keynumbers: "+String.format("%.3f", timeElapsed)+" seconds");
    	}

        
    	return nsrV;
    }
    
    /*
     * return a map: key=keynumber
     *               value=vector of all lines in the NSR output of the keynumber in exchange-format
     * divide the search to multiple single search each having a size=100               
     * works for old NSR, modified to work for new NSR after June, 2024
     */
    public static HashMap<String,Vector<String>> downloadKeynoNSRsMapByKeynumbersV(Vector<String> keynumbersV)throws IOException{
    	HashMap<String,Vector<String>> nsrMap=new HashMap<String,Vector<String>>();
    	HashMap<String,Vector<String>> tempMap=new HashMap<String,Vector<String>>();
 
    	
    	long startTime,endTime;
    	float timeElapsed;//in second
    	startTime=System.currentTimeMillis();
    	
    	
        int LIMIT=100;//limit of numbers for NSR search in a single page (even if I set nsrev=yes for "view all")

        int size=keynumbersV.size();       
        int nRuns=size/LIMIT;

    	if(size<=0){
    		System.out.println("Warning: No keynumbers to search for from NSR database");
    		return nsrMap;
    	}

        if(size%LIMIT!=0)
        	nRuns+=1;
        
        nomatchKeynos.clear();

        Vector<String> tempKeynos=new Vector<String>();;
        int start=0,end=0;
    	for(int i=0;i<nRuns;i++){
    		start=i*LIMIT;
    		end=(i+1)*LIMIT;
    		
    		if(end>=size)
    			end=size;
    		
    		tempKeynos.clear();
    		tempKeynos.addAll(keynumbersV.subList(start,end));
    		
    		tempMap=downloadKeynoNSRsMapByKeynumbersV100_URLC(tempKeynos);
    		
    		nsrMap.putAll(tempMap);
    	}
    	
		//for(int i=0;i<nsrV.get(0).size();i++)
		//	System.out.println(nsrV.get(0).get(i));
		
        
    	if(nomatchKeynos.size()>0){
    		System.out.println("Warning: Following keynumbers are not found in NSR database:");
    		for(int i=0;i<nomatchKeynos.size();i++)
    			System.out.println(nomatchKeynos.get(i));
    	}
    	
    	if(verbose) {
            endTime=System.currentTimeMillis();
            timeElapsed=(float)(endTime-startTime)/1000;
            System.out.println("Time elapsed for downloading NSRs by keynumbers: "+String.format("%.3f", timeElapsed)+" seconds");
    	}

        
    	return nsrMap;
    }
    
    /* 
     * using URLConnection with parameters in address line and "knum_act.jsp"
     * Download reference information from NSR for a given list of keynumbers
     * size<=100 for keynumbers, otherwise only the first 100 keynumbers are searched
     * 
     * The same function is also in ENSDFParser->nds.ensdf.Reference class, except for verbose option
     * works for old NSR and new NSR after June 2024
     */
	
    public static Vector<Vector<String>> downloadNSRsByKeynumbersV100_URLC(Vector<String> keynumbers)throws IOException{        
    	Vector<Vector<String>> nsrV=new Vector<Vector<String>>();
    	
        try{
        	HashMap<String,Vector<String>> NSROutputsMap=downloadKeynoNSRsMapByKeynumbersV100_URLC(keynumbers);
        	if(NSROutputsMap.size()>0)
        		nsrV=new Vector<Vector<String>>(NSROutputsMap.values()); 

        }catch(Exception e){ 
        }

        return nsrV;               
    }
    
    
    /*
     * return NSR search output string for input keynumbers in specified output format
     * outputFormat: keynum, json, csv, text, bibtex, exchange
     */
    public static String downloadNSROutputStringByKeynumbersV(Vector<String> keynumbers,String outputFormat)throws IOException{
    	String out="";
    	
        try{
        	String s=downloadNSROutputStringByKeynumbersV100_URLC(keynumbers,outputFormat);
        	s=s.trim();
        	if(s.length()>0) {
        		if(!out.isEmpty())
        			out+="    "+"\n";
		
        		out+=s+"\n";   		
        	}

        }catch(Exception e){ 
        }

        return out;
    }
    
    /*
     * return NSR search output string for input keynumbers in specified output format
     * outputFormat: keynum, json, csv, text, bibtex, exchange
     * For json format, the returned string should be parsed as a JSONArray in which 
     *    each element is an NSRJSONObject. See NSR json output for details of the 
     *    JSON object of a keynumber 
     *     
     */
    public static String downloadNSROutputStringByKeynumbersV100_URLC(Vector<String> keynumbers,String outputFormat)throws IOException{
    	String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
	   	
    	//String charset=java.nio.charset.StandardCharsets.UTF_8.name();
    	
    	//NSR Link Manager, return exchange-format ("X" mode), HTML format ("H" mode), redirect to paper page ("B" mode)
    	//process one keynumber only
    	
    	if(outputFormat==null || outputFormat.trim().isEmpty()) {
    		outputFormat=EXCHANGE_FORMAT;
    	}else {
    		outputFormat=outputFormat.trim().toLowerCase();
    		if(!outputFormatsV.contains(outputFormat))
    			outputFormat=EXCHANGE_FORMAT;
    	}
    		
    	//System.out.println(outputFormat);
    	
     	String format="ofrml="+outputFormat;
     	
     	
    	String url = "https://www.nndc.bnl.gov/nsr/knum_act.jsp?";    	
    	URLConnection connection;
    	InputStream response;   	  
    	
    	//long startTime,endTime;
    	//float timeElapsed;//in second
    	//startTime=System.currentTimeMillis();
        
        //PrintWriter testOut=new PrintWriter(new File("/home/junchen/work/evaluation/mytools/CheckKeynumber/test/testOut.txt"));
 
        String keynoStr="";
        
        String out="";
        try{
        	
        	for(int i=0;i<keynumbers.size();i++)
        		keynoStr+=keynumbers.get(i).trim()+"%0D%0A";
        	
        	url=url+format+"&keylst="+keynoStr+"&getkl=Search";
        	
        	url=convertToNewURL2024(url);
        	
        	//System.out.println(url);
        	
            //connection = new URL(url+keynumber+mode).openConnection();
            connection=new URL(url).openConnection();         
            connection.setRequestProperty("Accept-Charset", charset);
            
            connection.setConnectTimeout(5000);//5 second
            connection.setReadTimeout(5000);//5 second
            
        	response = connection.getInputStream();
        	
    		try{
            	BufferedReader br=new BufferedReader(new InputStreamReader(response));
            	String str=br.readLine();
            	while(str!=null){ 
            		//str=str.trim();
            		if(str.isEmpty())
            			str="    ";//very important to separate entries of different NSR keynumbers
            		
            		out+=str+"\n";
            		str=br.readLine();
            	}
            	
            	//System.out.println(out);
            	
            	br.close();
    		}catch(Exception e) {}
    		
        }catch(ConnectException e){
        	e.printStackTrace();
        	System.out.println("*** Error: can't connect to NNDC for NSR web query"); 
        }catch(Exception e){ 
        	e.printStackTrace();
        	System.out.println("*** Error: something wrong when downloading NSR"); 
        }
    	
    	return out;
    }
    
    /*
     * return a map: key=keynumber
     *               value=vector of all lines in the NSR output of the keynumber in exchange-format
     *               size=100 due to the limit number of connections (?)
     * works for old NSR, modified to work for new NSR after June, 2024
     */
    public static HashMap<String,Vector<String>> downloadKeynoNSRsMapByKeynumbersV100_URLC(Vector<String> keynumbers)throws IOException{

    	HashMap<String,Vector<String>> NSROutputsMap=new HashMap<String,Vector<String>>();

        try{
        	 
        	String keyno="";
        	String outputS=downloadNSROutputStringByKeynumbersV100_URLC(keynumbers,EXCHANGE_FORMAT);
        	
        	NSROutputsMap=makeNSROutputsMap(outputS);
        	if(NSROutputsMap.size()==0) {
        		System.out.println("Warning: Nothing is dowloaded from NSR database for "+keynumbers.size()+" input keynumbers");
        		return NSROutputsMap;
        	}    
        	     	
        	for(int i=0;i<keynumbers.size();i++){
        		keyno=keynumbers.get(i).toUpperCase().trim();
        		if(!NSROutputsMap.containsKey(keyno) && !nomatchKeynos.contains(keyno))
        			nomatchKeynos.add(keyno);
        	}
            /*
        	if(nomatchKeynos.size()>0){
        		System.out.println("Warning: Following keynumbers are not found in NSR database:");
        		for(int i=0;i<nomatchKeynos.size();i++)
        			System.out.println(nomatchKeynos.get(i));
        	}
        	*/
        	
        }catch(Exception e){ 
        	e.printStackTrace();
        	System.out.println("*** Error: something wrong when downloading NSR"); 
        }

        //endTime=System.currentTimeMillis();
        //timeElapsed=(float)(endTime-startTime)/1000;
        //System.out.println("Time elapsed for downloading NSR: "+String.format("%.3f", timeElapsed)+" seconds");

        
        return NSROutputsMap;               
    }
    
    /*
     * using HttpURLConnection with "knum_form.jsp"
     * slower than downloadNSRsbyKeynumbersV100()
     * using and sending address-line parameters in separate calls 
     * after connection established with knum_act.jsp
     * 
     * for old NSR only
     */
	protected static Vector<Vector<String>>  downloadNSRsByKeynumbersV100_test(Vector<String> keynumbers) {
    	
    	Vector<Vector<String>> nsrV=new Vector<Vector<String>>();
    	
    	InputStream response;
    	
    	Vector<String> nsrOutput=new Vector<String>();//store search result for a NSR keynumber (HTML source page)
  	  
    	Vector<String> tempSearchedKeynos=new Vector<String>();
    	
    	//long startTime,endTime;
    	//float timeElapsed;//in second
    	//startTime=System.currentTimeMillis();
    	
        boolean reachNSRKey=false;
        
        String keyno="";
    	String keynoStr="";
        try {

        	for(int i=0;i<keynumbers.size();i++)
        		keynoStr+=keynumbers.get(i).trim()+"%0D%0A";
        	
            String strSend = "nsrev=yes&ofrml=Exchange&keylst="+keynoStr+"&getkl=Search";
            String pageBase = "https://www.nndc.bnl.gov/nsr/";


            
            // First, do a GET on the proper page

            URL theUrl = new URL(pageBase+"knum_form.jsp");
            //URL theUrl = new URL(pageBase+"indx_form.jsp");
            //strSend="prim=yes&ofrm=Keynum&nsrev=yes&rsin=Search&qwhat1=Nuclide&param1=40ca";
            
            HttpURLConnection h = (HttpURLConnection)theUrl.openConnection();
            h.setRequestMethod("GET");
            h.setDoOutput(true);
            // h.setFollowRedirects(false);
            h.connect();

            InputStream is = h.getInputStream();
            StringBuffer sb = new StringBuffer();
            int ch = 0;

            while ((ch = is.read()) != -1) {
                 sb.append((char)ch);
            }
            is.close();

            String s = new String(sb);
            //System.out.println("sb length="+s.length());

            //System.out.println("s="+s);

            // Warning: there can be multiple cookies...
            // String cookie = h.getHeaderField ("set-cookie");

            // Now search for the full "action" of the form
            // This is sloppy because
            // I do not check for uppercase or spaces


            String targ = "action";

            int i = s.indexOf(targ);
            if(i < 0 ){
                 System.out.println("Did not find start of action");
                 return nsrV;
            }


            i = i + targ.length();
            
            i = s.indexOf("\"", i);
            		
            System.out.println("i="+i);
            String s2=s.substring(i+1);
            int j = s2.indexOf("\"");
            if(j < 0 ){
                 System.out.println("Did not find end of action");
                 return nsrV;
            }
            String s3=s2.substring(0,j);
            System.out.println("suffix="+s3);


            theUrl = new URL(pageBase+s3);

            h = (HttpURLConnection)theUrl.openConnection();
            h.setRequestMethod("GET");
            h.setDoOutput(true);
            h.connect();

            OutputStreamWriter osw = new OutputStreamWriter(h.getOutputStream());
            osw.write(strSend);
            osw.close();

            response = h.getInputStream();
        	BufferedReader br=new BufferedReader(new InputStreamReader(response));
        	nsrOutput=new Vector<String>();
        	String str=br.readLine();
        	
        	
        	//System.out.println("****  url="+url+format+"&keylst="+keynoStr+"&getkl=Search");
        	
        	reachNSRKey=false;
        	while(str!=null){       		
         	   if(!reachNSRKey){ 
            	   if((str.contains("&lt;")||str.contains("<")) && str.contains("KEYNO")){
            		   reachNSRKey=true;
            		   int p=str.indexOf("&gt;");
            		   if(p>0){
            			   keyno=str.substring(p+4).trim();
            			   if(keyno.length()>0)
            				   tempSearchedKeynos.add(keyno.toUpperCase());           			             			   
            		   }else if((p=str.indexOf(">"))>0) {
               			   keyno=str.substring(p+1).trim();
            			   if(keyno.length()>0)
            				   tempSearchedKeynos.add(keyno.toUpperCase());     
            		   }
            	   }
         	   }else if(str.contains("</pre>") || str.trim().isEmpty()){
        		   
                   if(nsrOutput.size()>0){
            		   nsrOutput.add(0, "<pre>");
                	   nsrOutput.add("</pre>");
                	   nsrV.add(nsrOutput);
                	   
                	   nsrOutput=new Vector<String>();
                   }else{
                	   System.out.println("*** Error: something wrong with keynumber="+keyno); 
                   }
                   
        		   reachNSRKey=false;
        		   keyno="";
        	   }
         	   
               //if(keyno.equalsIgnoreCase("2018AD01")) System.out.println(str);
               
         	   if(reachNSRKey && str.trim().length()>0) 
         		   nsrOutput.addElement(str);
         	   
         	   str=br.readLine();
         	   
         	   //debug
         	   //if(str!=null && str.contains("KEYNO")) System.out.println(str);
         	   
         	   //if(str!=null) testOut.write(str+"\n");
        	}
        	br.close();

        	//testOut.close();
        	
        	if(nsrV.size()<=0){
        		System.out.println("Warning: Nothing is dowloaded from NSR database for input keynumbers");
        		return nsrV;
        	} 

       } catch ( Exception ex) {
             ex.printStackTrace();
       }

        //endTime=System.currentTimeMillis();
        //timeElapsed=(float)(endTime-startTime)/1000;
        //System.out.println("Time elapsed for downloading NSRs: "+String.format("%.3f", timeElapsed)+" seconds");

        
       return nsrV;
    }
    
	public static Vector<String> downloadKeynumbersByNuclide(String nuclide) throws Exception {
		return downloadKeynumbersByNuclide(nuclide,new SearchParameter());
	}
	
	public static Vector<String> downloadKeynumbersByNuclide(String nuclide, SearchParameter param) throws Exception {
		//note that search condition like 
		//    param option="nuclide", param value="A=30" with old NSR 
		//is no longer acceptable by new NSR 2024.
		//for the same search with NSR 2024, the condition should be : 
		//    param option="a", param value="30"
		//and the param option="nuclide" must have a value of nuclide name like 31Na
		
		param.param1Value=nuclide;
		param.addSearchCondition("nuclide", nuclide);
		
		return downloadKeynumbersByParam(param);
	}
	
	public static Vector<String> downloadKeynumbersByParam(SearchParameter param) throws Exception {
	    Vector<String> keynumbersV=new Vector<String>();	    
		try {
			param.outputFormat=KEYNUMBER_FORMAT;
			
			/*
	    	long startTime,endTime;
	    	float timeElapsed;//in second
	    	startTime=System.currentTimeMillis();
	    	*/
			
	    	/*
			//debug
            if(param.param1Value.length()>0) System.out.println("   Search Param1: "+param.param1Option+"  "+param.param1Value);
            if(param.param2Value.length()>0) System.out.println("   Search Param2: "+param.param2Option+"  "+param.param2Value);
            if(param.param3Value.length()>0) System.out.println("   Search Param3: "+param.param3Option+"  "+param.param3Value);
            */
			
			String resultS=downloadNSRSearchResultsByParam(param);

			//System.out.println("NSR 241: result="+resultS);
			
	        String[] temp=resultS.split("\n");
	        for(int i=0;i<temp.length;i++){
	            String keyno=temp[i].trim();
	            //System.out.println(keyno);
	            if(keyno.length()==8)
	                keynumbersV.add(keyno.toUpperCase());
	        }	
	        
	        if(keynumbersV.size()==0) {
	            if(resultS.length()==0) {
	                System.out.println("Warning: No keynumber is dowloaded from NSR database for input search parameter");
	                System.out.println("  Query servlet="+INDEXED_SEARCH_URL);
	            }else {
	                System.out.println("Error: Something is downloaded from NSR database for input search parameter but not keynumbers");
	                System.out.println("   something="+resultS);
	            }

	            //if(param.param1Value.length()>0) System.out.println("   Search Param1: "+param.param1Option+"  "+param.param1Value);
	            //if(param.param2Value.length()>0) System.out.println("   Search Param2: "+param.param2Option+"  "+param.param2Value);
	            //if(param.param3Value.length()>0) System.out.println("   Search Param3: "+param.param3Option+"  "+param.param3Value);
	            if(param.paramOptionValueMap.size()>0) {
	            	for(String option:param.paramOptionValueMap.keySet()) {
	            		System.out.println("   Search Param: "+option+"  "+param.paramOptionValueMap.get(option));
	            	}
	                System.out.println(""); 
	            }
	            
	  
	        }
            
	        /*
	        endTime=System.currentTimeMillis();
	        timeElapsed=(float)(endTime-startTime)/1000;
	        System.out.println("Time elapsed for downloading NSR keynumbers: "+String.format("%.3f", timeElapsed)+" seconds");
	        */
		}catch(Exception e) {
			e.printStackTrace();
		}

		return keynumbersV;
	}
	
	public static String downloadNSRSearchResultsByNuclide(String nuclide) throws Exception {
		return downloadNSRSearchResultsByNuclide(nuclide,new SearchParameter());
	}
	public static String downloadNSRSearchResultsByNuclide(String nuclide, SearchParameter param) throws Exception {
		param.param1Value=nuclide;
		param.addSearchCondition(NUCLIDE_LABEL, nuclide);
		
		return downloadNSRSearchResultsByParam(param);
	}
	
    /*
     * default NSR search output is EXCHANGE_FORMAT in this class, unless otherwise specified
     * 
     * return a vector of NSR outputs each of which is a vector of lines in the NSR output for a keynumber
     *     in exchange format (note that other formats like json and csv don't fit the output style here)
	 * works for old NSR and new NSR after June 2024
     */
    public static Vector<Vector<String>> downloadNSRsByParam(SearchParameter param)throws IOException{
    	Vector<Vector<String>> nsrV=new Vector<Vector<String>>();
    	
    	try {
    	    //System.out.println("1-----------------------------------------------------");
    	    
        	param.outputFormat=EXCHANGE_FORMAT;       	
        	String resultS=downloadNSRSearchResultsByParam(param);
        	
        	//System.out.println("2-----------------------------------------------------");
        	
        	HashMap<String,Vector<String>> NSROutputsMap=makeNSROutputsMap(resultS);
        	
        	//System.out.println("3-----------------------------------------------------");
        	
        	//System.out.println(NSROutputsMap.size());
        	
        	if(NSROutputsMap.size()>0)
        		nsrV=new Vector<Vector<String>>(NSROutputsMap.values());
        	else {
        		if(resultS.length()==0) {
            		System.out.println("Warning: No NSR result is downloaded from NSR database for input search parameter");
        		}else {
        			System.out.println("Error: Something is downloaded from NSR database for input search parameter but not NSR result");
        			System.out.println("   something="+resultS);
        		}

    			//if(param.param1Value.length()>0) System.out.println("   Search Param1: "+param.param1Option+"  "+param.param1Value);
    	        //if(param.param2Value.length()>0) System.out.println("   Search Param2: "+param.param2Option+"  "+param.param2Value);
    	        //if(param.param3Value.length()>0) System.out.println("   Search Param3: "+param.param3Option+"  "+param.param3Value);
	            if(param.paramOptionValueMap.size()>0) {
	            	for(String option:param.paramOptionValueMap.keySet()) {
	            		System.out.println("   Search Param: "+option+"  "+param.paramOptionValueMap.get(option));
	            	}
	                System.out.println(""); 
	            }
	            
        		return nsrV;
        	}         	
        	
    	}catch(Exception e) {
    		
    	}

    	return nsrV;
    }
    
	//for new NSR indexed search after June 2024
	//return a string of all outputs for the input searchParameter
	//the output format can be set using SearchParameter.output-format option
	public static String downloadNSRSearchResultsByParam(SearchParameter searchParameter) throws Exception {
		return downloadNSRSearchResultsByParam(searchParameter,"");
	}
	
	/*
	 * for new NSR indexed search after June 2024
	 * 
	 * return a string of all outputs for the input searchParameter
	 * the output format can be set using SearchParameter.output-format option
	 * 
	 * input param outputFormat if not empty and valid will override output-format in SearchParameter 
	 */

	public static String downloadNSRSearchResultsByParam(SearchParameter searchParameter,String outputFormat) throws Exception {
    	long startTime,endTime;
    	float timeElapsed;//in second
    	startTime=System.currentTimeMillis();
    	
    	String out="";
    	String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
    	
		try{

	    	URLConnection connection;
	    	InputStream response;  
	    	
	    	if(outputFormat==null || outputFormat.trim().isEmpty()) {
	    		outputFormat=searchParameter.outputFormat;
	    	}else {
	    		outputFormat=outputFormat.trim().toLowerCase();
	    		if(!outputFormatsV.contains(outputFormat))
	    			outputFormat=searchParameter.outputFormat;
	    	}
	    	
	     	String format=outputFormat;	     	
	     	if(format.isEmpty() || format.equals("normal"))
	     		format=EXCHANGE_FORMAT;
	     	
	    	String url = INDEXED_SEARCH_URL;  

	    	HashMap<String,String> parMap=new HashMap<String,String>();		    	
	    	parMap.putAll(searchParameter.paramOptionValueMap);
	    	
	    	//adapt to settings with previous NSR index search
	    	if(searchParameter.param1Option.length()>0 && searchParameter.param1Value.length()>0)
	    		searchParameter.addSearchCondition(searchParameter.param1Option,searchParameter.param1Value);
	    	if(searchParameter.param2Option.length()>0 && searchParameter.param2Value.length()>0)
	    		searchParameter.addSearchCondition(searchParameter.param2Option,searchParameter.param2Value);
	    	if(searchParameter.param3Option.length()>0 && searchParameter.param3Value.length()>0)
	    		searchParameter.addSearchCondition(searchParameter.param3Option,searchParameter.param3Value);
	    	
	    	if(searchParameter.yearRangeL>0 && (!parMap.containsKey(YEAR_MIN_LABEL)||parMap.get(YEAR_MIN_LABEL).isEmpty()) )
	    			searchParameter.addSearchCondition(YEAR_MIN_LABEL, searchParameter.yearRangeL+"");
	    	if(searchParameter.yearRangeR>0 && (!parMap.containsKey(YEAR_MAX_LABEL)||parMap.get(YEAR_MAX_LABEL).isEmpty()) )
    			searchParameter.addSearchCondition(YEAR_MAX_LABEL, searchParameter.yearRangeR+"");
            if(searchParameter.measuredOnly)
            	searchParameter.addSearchCondition(SUBJECT_TYPE_LABEL,"measured");
		    
		    parMap.clear();
		    parMap.putAll(searchParameter.paramOptionValueMap);
		    
	    	String queryS="";
	    	for(String option:parMap.keySet()) {
	    		String value=parMap.get(option);
	    		if(option.trim().length()>0) {
	    			queryS+="&"+option+"="+value;
	    		}
	    	}
            

	    	if(searchParameter.primaryOnly)
	    		queryS+="&"+PRIMARY_ONLY_LABEL+"="+searchParameter.primaryOnly;   
	    	
	    	queryS+="&"+OUTPUT_FORMAT_LABEL+"="+format;
		    
	    	if(queryS.startsWith("&"))
		    	queryS=queryS.substring(1);
		    
		    url+=queryS;
		    
	    	//System.out.println("BaseNSR 1640 url="+url);
	    	
            connection=new URL(url).openConnection();         
            connection.setRequestProperty("Accept-Charset", charset);
            
            connection.setRequestProperty("User-Agent", "NSRSearchTool/1.0 (Academic Research Tool)");
            
            connection.setConnectTimeout(5000);//5 second
            connection.setReadTimeout(5000);//5 second
            
        	response = connection.getInputStream();
        	
    		try{
            	BufferedReader br=new BufferedReader(new InputStreamReader(response));
            	String str=br.readLine();
            	while(str!=null){ 
            		str=str.trim();
            		if(str.isEmpty())
            			str="    ";//very important to separate entries of different NSR keynumbers
            		
            		out+=str+"\n";
            		str=br.readLine();
            	}
            	
            	//System.out.println(out);
            	
            	br.close();
    		}catch(Exception e) {}
    		
		    if(out.contains("No matches found")) {
		    	out="";
		    }else if(out.contains("Server Error")){
		    	System.out.println("Connection issue: "+out+"\n");
		    	out="";
		    }else if(out.trim().isEmpty()){
		    	System.out.println("No matches found");
		    	
		    }

		}catch(Exception e){
			//e.printStackTrace();
			System.out.println("Connection issue: NSR Index Search page is not accessible!");
			//if(searchParameter.param1Value.length()>0) System.out.println("Search Param1: "+searchParameter.param1Option+"  "+searchParameter.param1Value);
	        //if(searchParameter.param2Value.length()>0) System.out.println("Search Param2: "+searchParameter.param2Option+"  "+searchParameter.param2Value);
	        //if(searchParameter.param3Value.length()>0) System.out.println("Search Param3: "+searchParameter.param3Option+"  "+searchParameter.param3Value);
	          
			//if(webClient!=null)
			//    webClient.close();
		}

		if(verbose) {
	        endTime=System.currentTimeMillis();
	        timeElapsed=(float)(endTime-startTime)/1000;
	        System.out.println("Time elapsed for NSR retrieval of "+searchParameter.param1Value+": "+String.format("%.3f", timeElapsed)+" seconds");
		}

        
	    return out;
	    
	}
    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    
	/*get keynumber from the NSR output in HTML or exchange format*/ 
	public static String findKeynumberFromNSROutput(Vector<String> nsrSearchOutput){
		String keynumber="";
		String temp="";
		int index=0;
		for(int j=0;j<nsrSearchOutput.size();j++){
			temp=nsrSearchOutput.elementAt(j);
			index=temp.toUpperCase().indexOf("KEYNO");
			if(index<0)
				continue;
			
			int offset=4;
			index=temp.indexOf("&gt;");
			if(index<0) {
				index=temp.indexOf(">");
				offset=1;
			}
			if(index>0)
				keynumber=temp.substring(index+offset).toUpperCase().trim();
				
			break;
		}
		
		return keynumber;
	}
	
	public static Vector<String> getNSROutputByKeynumber(String keynumber,Vector<Vector<String>> NSROutputs){
		Vector<String> nsr=new Vector<String>();
		String temp="";
		Vector<String> tempNSR;
		int index=0;
		for(int i=0;i<NSROutputs.size();i++){
			tempNSR=NSROutputs.elementAt(i);
			for(int j=0;j<tempNSR.size();j++){
				temp=tempNSR.elementAt(j).toUpperCase();
				index=temp.indexOf("KEYNO");
				if(index<0)
					continue;
				
				index=temp.indexOf(keynumber.toUpperCase());

				if(index>0)
					return tempNSR;
				else
					break;
			}

		}
		
		return nsr;
	}
	
    /*
     * check if the NSR database is accessible by using the current NSR Line manager
     * before the program can process to download the NSR 
     */
    public static boolean isNSRAccessible(){
        Vector<String> out=new Vector<String>();
        try {
            out = downloadNSRByKeynumber_NSRLink("2012WA38");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        
        //System.out.println(out.size());
        
        if(out.size()==0)
            return false;
        
        return true;
    }
	
	/*
	 * for search results in exchange format as XML, with <pre> </pre> enclosure and "&lt;" for "<" and "&gt;" for ">"
	 */
	public static HashMap<String,Vector<String>> makeNSROutputsMap(InputStreamReader reader){
		HashMap<String,Vector<String>> NSROutputsMap=new HashMap<String,Vector<String>>();
    	
    	Vector<String> nsrOutput=new Vector<String>();//store search result for a NSR keynumber (HTML source page) 	  
	
        boolean reachNSRKey=false;
        
        String keyno="";
		
		try{
        	BufferedReader br=new BufferedReader(reader);
        	nsrOutput=new Vector<String>();
        	String str=br.readLine();

        	//System.out.println("****  url="+url+format+"&keylst="+keynoStr+"&getkl=Search");
        	
        	reachNSRKey=false;
        	while(str!=null){   

         	   if(!reachNSRKey){ 
            	   if((str.contains("&lt;")||str.contains("<")) && str.contains("KEYNO")){
                   	   //System.out.println(str);
                   	
            		   reachNSRKey=true;
            		   int p=str.indexOf("&gt;");
            		   if(p>0){
            			   keyno=str.substring(p+4).trim().toUpperCase();       			             			   
            		   }else if((p=str.indexOf(">"))>0) {
            			   keyno=str.substring(p+1).trim().toUpperCase();  
            		   }
            	   }
         	   }else if(str.contains("</pre>") || str.trim().isEmpty()){
        		   
                   if(nsrOutput.size()>0){
            		   nsrOutput.add(0, "<pre>");
                	   nsrOutput.add("</pre>");
                	   NSROutputsMap.put(keyno,nsrOutput);
                	   
                	   nsrOutput=new Vector<String>();
                   }else{
                	   System.out.println("*** Error: something wrong with keynumber="+keyno); 
                   }
                   
        		   reachNSRKey=false;
        		   keyno="";
        	   }
         	   
               //if(keyno.equalsIgnoreCase("2018AD01")) System.out.println(str);
               
         	   if(reachNSRKey && str.trim().length()>0) 
         		   nsrOutput.addElement(str);
         	   
         	   str=br.readLine();
         	   
         	   //debug
         	   //if(str!=null && str.contains("KEYNO")) System.out.println(str);
         	   
         	   //if(str!=null) testOut.write(str+"\n");
        	}
        	br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return NSROutputsMap;
	}

	/*
	 * for search results in exchange format as text (no <pre></pre> enclosure)
	 */
	public static HashMap<String,Vector<String>> makeNSROutputsMap(String nsrText){
		HashMap<String,Vector<String>> NSROutputsMap=new HashMap<String,Vector<String>>();
    	
    	Vector<String> nsrOutput=new Vector<String>();//store search result for a NSR keynumber (HTML source page) 	  
	
        boolean reachNSRKey=false;
        
        String keyno="";
		
		try{
            //System.out.println(nsrText);
            
	    	String[] temp=nsrText.split("[\r?\n?]+");
	    	//String[] temp=nsrText.split("[\r\n]+|[\n]+|[\r]+");
	    	
	    	Vector<String> linesV=new Vector<String>();
	    	for(int i=0;i<temp.length;i++){
	    		String line=temp[i];
	    		if(line.isEmpty())//DO NOT TRIM
	    			continue;
	    		
	    		//System.out.println("#"+line+"#");
	    		
	    		linesV.add(line);//DO NOT trim since the space at the end or beginning counts;
	    		                 //also empty lines are added as separation between NSR blocks for different keynumbers
	    	}

        	linesV.add("    "); //add an empty line to indicate the end in case there is none
        	
	    	int n=0;
        	String str="";
        	
        	reachNSRKey=false;
        	while(n<linesV.size()){   
        		str=linesV.get(n);
        		if(!reachNSRKey){ 
             	   if(str.contains("<KEYNO")){
            		   reachNSRKey=true;
            		   int p=str.indexOf(">");
            		   if(p>0){
            			   keyno=str.substring(p+1).trim().toUpperCase();       			             			   
            		   }
            	   }
        		}else if(str.trim().isEmpty()){       		   
                    if(nsrOutput.size()>0){
             		   nsrOutput.add(0, "<pre>");
                 	   nsrOutput.add("</pre>");
                 	   NSROutputsMap.put(keyno,nsrOutput);
                 	   
                 	   nsrOutput=new Vector<String>();
                    }else{
                 	   System.out.println("*** Error: something wrong with keynumber="+keyno); 
                    }
                    
         		   reachNSRKey=false;
         		   keyno="";
         		  
         		   //System.out.println("@@@@@@@@@@@@@@@@@@@@"+str+"  map size="+ NSROutputsMap.size());
        		}
                //if(keyno.equalsIgnoreCase("2018AD01")) System.out.println(str);
                 	   
        		if(reachNSRKey && str.trim().length()>0)          		   
        			nsrOutput.addElement(str);
          	   
        		//System.out.println("#####"+str);
        		
        		n++;
          	   
          	   //debug
          	   //if(str!=null && str.contains("KEYNO")) System.out.println(str);
          	   
          	   //if(str!=null) testOut.write(str+"\n");  
        	}
       
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return NSROutputsMap;
	}
	
    public static String NSRLinkManager(String keynumber,char option){
        String url="https://www.nndc.bnl.gov/nsr/nsrlink.jsp?"+keynumber+",";
        if(option!='X' && option!='B' && option!='H')
            url=url+"B";
        else
            url=url+option;
        
        if(!useOldNSRLinkManager)
        	url=convertToNewURL2024(url);
        
        return url;
    }
    
	/////////////////////////////////////////////////
	//For NSR after June 2024
	public static boolean isGoodParamName(String name) {
		if(ParamNameList.contains(name.toLowerCase()))
			return true;
		
		return false;
	}
	
	public static Vector<String> downloadKeynumbersOfMassByNuclide(String nuclide,int fromYear,int toYear) {

		Vector<String> keynosV=new Vector<String>();
		try {
			SearchParameter sp=new SearchParameter();
			sp.addSearchCondition("nuclide",nuclide);
			sp.addSearchCondition("topic","A-M");
			
			if(fromYear>0)
				sp.addSearchCondition("year-min",fromYear+"");
			else
				fromYear=1800;
			
		    if(toYear>fromYear)
		    	sp.addSearchCondition("year-max",toYear+"");

			sp.measuredOnly=true;
			
			keynosV=downloadKeynumbersByParam(sp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return keynosV;
	}
	
	public static Vector<String> downloadKeynumbersOfMassByNuclide(String nuclide,int fromYear) {
		return downloadKeynumbersOfMassByNuclide(nuclide,fromYear,-1);
	}
	
	public static Vector<String> downloadKeynumbersOfMassByNuclide(String nuclide) {
		return downloadKeynumbersOfMassByNuclide(nuclide,-1,-1);
	}
	
    public static Vector<NSRAbstract> downloadAndParseNSRsByKeynumbersV(Vector<String> keynumbersV){
    	Vector<NSRAbstract> out=new Vector<NSRAbstract>();
    	Vector<Vector<String>> NSROutputsV=new Vector<Vector<String>>();
    	String keyno="";
    	NSRAbstract nsrAbstract=null;
    	if(keynumbersV==null || keynumbersV.isEmpty())
    		return out;
    	
    	try {
			NSROutputsV=downloadNSRsByKeynumbersV(keynumbersV);		
			//System.out.println(" keyno="+keynumbersV.get(0)+" "+NSROutputsV.size());
			
			for(int i=0;i<NSROutputsV.size();i++){
				Vector<String> NSROutput=NSROutputsV.get(i);
                if(NSROutput.size()==0)
                	continue;
                
				keyno=findKeynumberFromNSROutput(NSROutput).toUpperCase();
				
                if(keyno.isEmpty())
                	continue;
                
        		//for(int m=0;m<NSROutput.size();m++)
    			//System.out.println(NSROutput.get(m));
    			
				nsrAbstract=new NSRAbstract();
                nsrAbstract.keyno=keyno;
                
                //System.out.println("  1+"+keyno+"  "+NSROutput.get(0));
                
        		nsrAbstract.NSRInfoMap=parseNSR(NSROutput);
        		
        		//System.out.println("  2+"+keyno+" "+nsrAbstract.NSRInfoMap.size());
        		//for(String key:nsrAbstract.NSRInfoMap.keySet())
        		//	System.out.println("key="+key+" value="+nsrAbstract.NSRInfoMap.get(key));
        		
        		nsrAbstract.parseAll();
        		
        		out.add(nsrAbstract);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return out;
    }
    
    public static NSRAbstract downloadAndParseNSRByKeynumber(String keynumber){
    	Vector<String> inputKeynos=new Vector<String>();
    	inputKeynos.add(keynumber);
    	
    	Vector<NSRAbstract> temp=downloadAndParseNSRsByKeynumbersV(inputKeynos);
    	//System.out.println(keynumber+" "+temp.size());
    	
    	if(temp.size()>0)
    		return temp.get(0);
    	
    	return null;
    }
    
    public static Vector<NSRAbstract> downloadAndParseNSRsbyParam(SearchParameter param)throws IOException{
    	Vector<NSRAbstract> out=new Vector<NSRAbstract>();
    	Vector<Vector<String>> NSROutputsV=new Vector<Vector<String>>();
    	
    	try {
    	    //System.out.println("1-----------------------------------------------------");
    	    
			NSROutputsV=downloadNSRsByParam(param);		
			out=parseNSRsV(NSROutputsV);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	//System.out.println("3-----------------------------------------------------");
    	
    	return out;    	
    }
    
    public static Vector<NSRAbstract> parseNSRsV(Vector<Vector<String>> NSROutputsV)throws IOException{
    	Vector<NSRAbstract> out=new Vector<NSRAbstract>();
    	String keyno="";
    	NSRAbstract nsrAbstract=null;
    	
    	try {
    	    //System.out.println("1-----------------------------------------------------");	
			
			//System.out.println("2----------------------------------------------------- NSROutputsV.size()="+NSROutputsV.size());
			
			for(int i=0;i<NSROutputsV.size();i++){
				Vector<String> NSROutput=NSROutputsV.get(i);
                if(NSROutput.size()==0)
                	continue;
                
            	//System.out.println(NSROutputsV.size()+"  "+NSROutput.size());
                
                //System.out.println("  2.1-----------------------------------------------------");
				
                keyno=findKeynumberFromNSROutput(NSROutput).toUpperCase();
				
				//System.out.println("  2.2-----------------------------------------------------");
				//System.out.println(keyno);
				
                if(keyno.isEmpty())
                	continue;

				nsrAbstract=new NSRAbstract();
                nsrAbstract.keyno=keyno;
                
        		nsrAbstract.NSRInfoMap=parseNSR(NSROutput);
                //nsrAbstract.NSRInfoMap=parseNSRasText(NSROutput);
        		//for(String s:NSROutput) System.out.println(s);
        		//for(String key:nsrAbstract.NSRInfoMap.keySet()) System.out.println(key+"  "+nsrAbstract.NSRInfoMap.get(key));
        		
        		//System.out.println("  2.3-----------------------------------------------------");
        		
        		nsrAbstract.parseAll();
        		
        		//System.out.println("  2.4-----------------------------------------------------");
        		out.add(nsrAbstract);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	//System.out.println("3-----------------------------------------------------");
    	
    	return out;    	
    }

	/*
	 * make a NSRAbstract instance from a string block of NSR output in exchange format from NSR database
	 */
	public static NSRAbstract makeNSRAbstractFromNSROutput(Vector<String> NSROutput) {
    	String keyno="";
    	NSRAbstract nsrAbstract=null;
    	
    	try {

			keyno=findKeynumberFromNSROutput(NSROutput).toUpperCase();

            if(keyno.isEmpty())
            	return null;
            
			nsrAbstract=new NSRAbstract();
            nsrAbstract.keyno=keyno;
            
    		nsrAbstract.NSRInfoMap=parseNSR(NSROutput);
    		
    		nsrAbstract.parseAll();
			
    		return nsrAbstract;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return null;
	}
	

}
