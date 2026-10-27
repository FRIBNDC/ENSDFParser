package ensdfparser.ensdf;

import java.util.Vector;

public class Author{
    public String name="";
    public String firstName="";
    public String lastName="";
    public String middleName="";
    public String orcid="";
    public String email="";
    public String gender="";
    
    public Vector<String> affiliations=new Vector<String>();
    
    public Author() {};
    public Author(String authorString) {
    	parseAuthorString(authorString);
    }
    public String toString(boolean lastNameFirst) {
    	return getFullName(lastNameFirst);
    }

    public String toString() {
    	return toString(false);
    }
    
    private String getFullName(boolean lastNameFirst,boolean removeHeadingHyphen) {
    	try {
    		String s="";
    		
    		String firstName=this.firstName;
    		String middleName=this.middleName;
    		String lastName=this.lastName;
    		
    		if(removeHeadingHyphen && middleName.startsWith("-"))
    			middleName=middleName.substring(1).trim();
    		
    		if(lastNameFirst) {
    			if(firstName.isEmpty())
    				s=lastName+", "+middleName;
    			else
    				s=lastName+", "+firstName+" "+middleName;

    			s=s.trim();
    			if(s.startsWith(","))
    				s=s.substring(1).trim();
    			if(s.endsWith(","))
    				s=s.substring(0,s.length()-1).trim();
    			
    		}else {
    			if(middleName.isEmpty())
    				s=firstName+" "+lastName;
    			else
    				s=firstName+" "+middleName+" "+lastName;
    			
    			s=s.trim();
    		}
    		
    		return s;
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    public String getFullName(boolean lastNameFirst) {
    	return getFullName(lastNameFirst,true);
    }
    
    /*
     * KEEP "-" in the middle name
     */
    public String getFullNameAsIs(boolean lastNameFirst) {
    	return getFullName(lastNameFirst,false);
    }

    public String getFullName() {
    	return getFullName(false);
    }
    
    public String getFullNameAsIs() {
    	return getFullNameAsIs(false);
    }
    
    private void parseAuthorString(String authorString) {
    	try {
    		String s=authorString.trim();
            if(s.isEmpty())
                return;
            
            String s1="",s2="";
            
            
            this.name=s;//e.gl,  H.S.Jung
            
            //E.Norbeck, Jr.
            //E.Norbeck Jr.
            //E.Norbeck, Jr
            //E.Norbeck Jr
            //or Jr. in E.Norbeck, Jr. 
            
            s1=s.toUpperCase();
            
            String postfix="";
            if(s1.endsWith("JR.")) {
            	postfix=s.substring(s.length()-3);
            	s=s.substring(0,s.length()-3).trim();
            }else if(s1.endsWith("JR")) {
            	postfix=s.substring(s.length()-2);
            	s=s.substring(0,s.length()-2).trim();
            }
            	
            if(s.endsWith(","))
            	s=s.substring(0,s.length()-1).trim();
            
            //E.Norbeck, Jr.
            //or Jr in S.K.Allison, P.G.Murphy, E.Norbeck, Jr
            if(s.isEmpty() && !postfix.isEmpty()) {
            	lastName=postfix;
            	return;
            }
            
            String delim="";
            if(s.contains(".")) {
                delim=".";  
            }else if(s.contains(" ")){//not initial
                delim=" ";
            }

            int n1=s.lastIndexOf(delim);  
            if(delim.isEmpty()) {
            	s1="";
            	s2=s;
            }else {
                s1=s.substring(0,n1+1).trim();
                s2=s.substring(n1+1).trim();
            }
            
            s2=s2+" "+postfix;
            
            lastName=s2.trim();
            
            //if(s2.toUpperCase().startsWith("FRIED")) System.out.println(s2+"  s="+s+" authors="+Authors());
            
            n1=s1.indexOf(delim);
            if(s1.isEmpty() || n1==s1.length()-1) {
                firstName=s1;
            }else {
                firstName=s1.substring(0,n1+1).trim();
                middleName=s1.substring(n1+1).trim();
                
                if(lastName.toUpperCase().startsWith("COLLABOR")) {
                	if(firstName.toUpperCase().equals("FOR"))
                		lastName=s.substring(3).trim();
                	else
                		lastName=s;
                	
                	firstName="";
                	middleName="";
                }
            }	    		
    	}catch(Exception e) {
    		
    	}
    }
    
}
