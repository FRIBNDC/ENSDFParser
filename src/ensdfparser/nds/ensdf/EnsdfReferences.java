package ensdfparser.nds.ensdf;
import java.util.*;

import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;
import ensdfparser.nds.util.Word;
import ensdfparser.nsr.BaseNSR;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Contains all of the keynumbers used in this mass chain
 * Used when printing the reference pages at the back of the Data Sheets
 * 
 * last update: 04/12/2019, by Jun Chen
 */
public class EnsdfReferences {
    protected String [] keynos;//all keynumers in the input ENSDF file
 
    
    //protected Vector<Vector<String>> NSROutputs=new Vector<Vector<String>>();//Vector of NSR search output of all keynumbers
    //protected Vector<String> keynosInNSROutputs=new Vector<String>(); //keynumers from NSROutputs in the input NSR file
    protected HashMap<String,Vector<String>> keynoNSROutputMap=new HashMap<String,Vector<String>>();//NSR search output of all keynumbers
    
    protected String warnings="";
    
    public static String wrongLengthMark="#";//mark for possible keynumber string that has only partially-valid format, eg, "1984Al state that ...." 
    public static String wrongYearMark="*";
    public static String noseparatorMark="&";//the possible keynumber not separated from neighbors
    public static String wrongFormatMark="$";
    public static String tempKeynoMark="@";//temporary keynumber: 1953Nahmias: Compt Rendu 236, 2399 (1953)

    public EnsdfReferences(){
    }
       
    public HashMap<String,Vector<String>> keynoNSROutputMap() {
    	return keynoNSROutputMap;
    }
    /** load all the keynumbers in a file*/
    public void load_old(Vector<String> lines){//does not work properly, can not extract key-number in bracket
        Vector<String> out=new Vector<String>();
        int spot=7;
        String text="";
        String line;
        Word w;
        boolean duplicate;
        for(int i=0;i<lines.size();i++){
            spot=5;
            line=(String)lines.elementAt(i);
            if(line.length()<8) continue;
           if(line.charAt(6)=='c'||line.charAt(6)=='C'||line.charAt(7)=='Q'||(line.charAt(7)==' '&&line.charAt(6)==' ')){
                while(spot<line.length()-1){
                    w=new Word(line,spot);
                    
                    System.out.println("*******************line0="+line+" line(pos)="+line.substring(spot)+" key="+w.getWord());
                    
                   // System.out.println(w.getWord()+" "+w.length());
                    if(w.length()==0)spot++;
                    else spot+=w.length();
                    text=w.getWord();
                    //debug

                    if(line.toUpperCase().contains("2015BAAA")) System.out.println("*******************line1="+line+" key="+text);
                    if(line.toUpperCase().contains("2012WA38")) System.out.println("*******************line2="+line+" key="+text);
                    if(checkIfKeyno(text)){
                        System.out.println("*******************@@line="+line+" key="+text);
                        duplicate=false;
                        for(int j=0;j<out.size();j++){
                            if(((String)out.elementAt(j)).equals(text.toUpperCase())) duplicate=true;
                        }
                        if(!duplicate) out.addElement(text.toUpperCase());
                    }
                }
            }
        }
        keynos=new String[out.size()];
        for(int i=0;i<out.size();i++) keynos[i]=(String)out.elementAt(i);
        Arrays.sort(keynos,new KeyComparator());
    }
    
    public void loadENSDFLines(Vector<String> lines){
        Vector<String> out=new Vector<String>();
        String keyno="";
        String line;
        boolean duplicate;
        boolean skip=false;
        String datatypes="LGBEAPN";
        char type=0;
        
        Vector<String> temp=new Vector<String>();
        
        for(int i=0;i<lines.size();i++){
            line=(String)lines.elementAt(i);
            if(line.length()<8) continue;
            
            skip=false;
            type=line.charAt(7);
            
            if(line.toUpperCase().charAt(6)=='D')//skip document records
                skip=true;
            else if(line.charAt(6)==' ' && line.charAt(5)==' ' && datatypes.indexOf(type)>=0)//skip data record lines
                skip=true;
            
            
            //if(line.charAt(6)=='c'||line.charAt(6)=='C'||line.charAt(7)=='Q'||(line.charAt(7)==' '&&line.charAt(6)==' '))
            //  skip=false;
                    
            if(!skip){
               temp=findKeyNumbers(line);
    
               for(int j=0;j<temp.size();j++){
                   keyno=temp.elementAt(j);
                   
                   //System.out.println("*******************@@line1="+line+" key="+keyno+" "+checkIfKeyno(keyno));
                   
                   if(checkIfKeyno(keyno)){
                        
                        duplicate=false;
                        for(int k=0;k<out.size();k++){
                            if(((String)out.elementAt(k)).equals(keyno.toUpperCase())) duplicate=true;
                        }
                        if(!duplicate) out.addElement(keyno.toUpperCase());
                   }
               }
            }
        }
        keynos=new String[out.size()];
        for(int i=0;i<out.size();i++) keynos[i]=(String)out.elementAt(i);
        Arrays.sort(keynos,new KeyComparator());
    }
    
    public boolean isDigits(String s) {
        try {
            if(s.isEmpty())
                return false;
            
            for(int i=0;i<s.length();i++) {
                if(!Character.isDigit(s.charAt(i)))
                    return false;
            }
            return true;
        }catch(Exception e) {
        }
        
        return false;
    }
    
    public boolean isLetters(String s) {
        try {
            if(s.isEmpty())
                return false;
            
            for(int i=0;i<s.length();i++) {
                if(!Character.isLetter(s.charAt(i)))
                    return false;
            }
            return true;
        }catch(Exception e) {
        }
        
        return false;
    }
    
    public boolean isLettersOrDigits(String s) {
        try {
            if(s.isEmpty())
                return false;
            
            for(int i=0;i<s.length();i++) {
                if(!Character.isLetterOrDigit(s.charAt(i)))
                    return false;
            }
            return true;
        }catch(Exception e) {
        }
        
        return false;
    }
    
    public boolean isLetter(char c) {return Character.isLetter(c);}
    public boolean isDigit(char c) {return Character.isDigit(c);}
    public boolean isLetterOrDigit(char c) {return Character.isLetterOrDigit(c);}
    
    public boolean isValidKeynumber(String s){
        int n=s.trim().length();
        if(n!=8)
            return false;
        if(!isDigits(s.substring(0,4)))
            return false;
        if(!isLetters(s.substring(4,6)))
            return false;
        
        if(isDigits(s.substring(6,8)) || isLetters(s.substring(6,8)))
            return true;
        
        return false;
    }
    
    /*
     * find all keynumbers with partially-valid format in an ENSDF line, that has six characters consistent with
     * the first six characters of a valid keynumber
     * 
     */
    public Vector<String> findPossibleKeyNumbers(String line){
        Vector<String> possibleKeynumbers=new Vector<String>();
            
        int i=0,j=0;
        String s="",mark="";
        
        char c1,prevChar,prevChar2,nextChar;
        
        while(i<=line.length()-6){
            mark="";
            c1=line.charAt(i);
                 
            prevChar=nextChar=prevChar2=0;
            if(i>1) {
                prevChar=line.charAt(i-1);
                if(i>2)
                    prevChar2=line.charAt(i-2);
            }
            //if(i<line.length()-s.length()) nextChar=line.charAt(i+s.length());
            
            if(c1=='1' || c1=='2'){
            	
            //if(Character.isDigit(c) && c!='0'){
                if(!isDigit(line.charAt(i+1))){
                    i+=2;
                    continue;
                }else if(!isDigit(line.charAt(i+2))){
                    i+=3;
                    continue;   
                }else if(prevChar=='.' && Character.isDigit(prevChar2)) {
                	i+=4;
                	continue;
                }
                	
                
                //if reaching here, the string of the 3 characters that have
                //been scanned are in format of DDD  (D for digit, L for letter)
                //then continue to scan the next t characters
                s=line.substring(i,i+3);
                j=i+3;
                while(j<line.length() && isLetterOrDigit(line.charAt(j))){
                    s+=line.charAt(j);
                    if(s.length()==8)
                        break;
                    j++;
                }
                

                //the 8 chars are all digits, skip the first 4 to continue the scan
                if(isDigits(s)) {
                    i+=4;
                    continue;
                }   
  
                if(i<line.length()-s.length()) 
                	nextChar=line.charAt(i+s.length());
                
                //min length=6 for s being considered as wrong keynumber
                //length=6, s is not considered as wrong keynumber due to missing characters
                //and is skip, plus nextChar being skipped too (since it is not digit or letter)
                //For s.length=6 or 7, nextChar is already non-letter or non-digit (see how s is obtained) and 
                //for s to be considered as partial keynumber, prevChar also must be non-letter and non-digit
                if(s.length()<6) {
                    i+=s.length()+1;//skip nextChar since it is not digit or letter
                    continue;
                }
                  
                if(s.length()<8&&isLetterOrDigit(prevChar)){
                    if(isDigits(s.substring(0,4))) {
                        int n=Integer.parseInt(s.substring(0,4));
                        if(n>1900 && n<2100 && isLetters(s.substring(4,6))) {
                            //do nothing, will be processed later
                        }else {
                            i+=s.length()+1;//skip nextChar since it is not digit or letter
                            continue;
                        }
                    }

                }
                
                if(s.length()==8) {    

                    if(isValidKeynumber(s)) {
                        i+=8;
                        int n=Integer.parseInt(s.substring(0,4));
                        if(n<1900 || n>2100)
                            mark=wrongYearMark;
                        else if(isLetterOrDigit(prevChar) && !isLetterOrDigit(nextChar))
                            mark=noseparatorMark;    
                        else if(!isLetterOrDigit(prevChar) && isLetterOrDigit(nextChar)) {
                        	if(Character.isDigit(s.charAt(s.length()-1)))
                        		mark=noseparatorMark;
                        	else
                        		mark=tempKeynoMark;
                        }
                        //System.out.println("EnsdfReference 302: keyno="+s+" mark="+mark+" prevChar="+prevChar+" nextChar="+nextChar+"$");
                    }else if(!isLetterOrDigit(prevChar) && !isLetterOrDigit(nextChar)){
                        i+=8;
                        mark=wrongFormatMark;
                    }else if(!isLetterOrDigit(prevChar)) {//isLetterOrDigit(nextChar)=true
                        i+=1;
                        continue;
                    }else {//isLetterOrDigit(prevChar)=true
                        i+=9;//also skip nextChar
                    }           
                }else{
                    //s.length=6 or 7, and here prevChar is non-digit or non-letter 
                    //(prevChar=letter or digit is processed above already)
                    //s here is considered as a partial keynumber
                    i+=s.length()+1;
                    if(s.length()==6 && isLetters(s.substring(3)))//100KEV, 100CDG (in DSID, {+100}Cd|g)
                        continue;
                 
                    mark=wrongLengthMark;

                }
            
                if(!Str.isNumeric(s))
                    possibleKeynumbers.add(s+mark);
                
                continue;
            }
            
            i++;
        }
        
        return possibleKeynumbers;
    }
    
    /*
     * find all keynumbers with valid 8-character format
     */
    public Vector<String> findKeyNumbers(String line){
        Vector<String> keynumbers=new Vector<String>();
        int i=0;
        char c;
        while(i<=line.length()-8){
            c=line.charAt(i);
            if(Character.isDigit(c) && c!='0'){
                if(!Character.isDigit(line.charAt(i+1))){i+=2;continue;}
                else if(!Character.isDigit(line.charAt(i+2))){i+=3;continue;}
                else if(!Character.isDigit(line.charAt(i+3))){i+=4;continue;}
                else if(!Character.isLetter(line.charAt(i+4))){i+=5;continue;}
                else if(!Character.isLetter(line.charAt(i+5))){i+=6;continue;}  
                else if(!Character.isDigit(line.charAt(i+6)) && !Character.isLetter(line.charAt(i+6))){i+=7;continue;}
                else if(!Character.isDigit(line.charAt(i+7)) && !Character.isLetter(line.charAt(i+7))){i+=8;continue;}
                
                int n=Integer.parseInt(line.substring(i, i+4));
                if(n<1900 || n>2100){
                    i=i+8;
                    continue;
                }
                
                if( (i<line.length()-8) && (Character.isDigit(line.charAt(i+8)) || Character.isLetter(line.charAt(i+8))) ) {
                    i=i+8;
                    continue;
                }
                
                keynumbers.add(line.substring(i,i+8));
                i=i+8;
                continue;
            }
            i++;
        }
        
        return keynumbers;
    }
    
    public String findFirstKeyNumber(String line){
        String keynumber="";
        int i=0;
        char c;
        while(i<=line.length()-8){
            c=line.charAt(i);
            if(Character.isDigit(c) && c!='0'){
                if(!Character.isDigit(line.charAt(i+1))){i+=2;continue;}
                else if(!Character.isDigit(line.charAt(i+2))){i+=3;continue;}
                else if(!Character.isDigit(line.charAt(i+3))){i+=4;continue;}
                else if(!Character.isLetter(line.charAt(i+4))){i+=5;continue;}
                else if(!Character.isLetter(line.charAt(i+5))){i+=6;continue;}  
                else if(!Character.isDigit(line.charAt(i+6)) && !Character.isLetter(line.charAt(i+6))){i+=7;continue;}
                else if(!Character.isDigit(line.charAt(i+7)) && !Character.isLetter(line.charAt(i+7))){i+=8;continue;}
                
                int n=Integer.parseInt(line.substring(i, i+4));
                if(n<1800 || n>2100){
                    i=i+8;
                    continue;
                }
                
                if( (i<line.length()-8) && (Character.isDigit(line.charAt(i+8)) || Character.isLetter(line.charAt(i+8))) ) {
                    i=i+8;
                    continue;
                }
                
                return line.substring(i,i+8);
            }
            i++;
        }
        
        return keynumber;
    }
    
    
    /*
     * load NSR outputs from the HTML source file of the page of search results in Exchange format
     * This text format of the file is that of the outputs from downloadNSRbyKey() which directly accesses the NSR database
     */
    public void loadNSRsFromSourceFile(String filename) throws Exception{
        //NSROutputs.clear();
        //keynosInNSROutputs.clear();
    	
        keynoNSROutputMap.clear();
        
        BufferedReader br= new BufferedReader(new FileReader(new File(filename)));      
        String s="";
        Vector<String> nsrOutput=new Vector<String>();
        while(true){
            s = br.readLine();
            if (s==null) break;

            nsrOutput.add(s);
            if(s.contains("</pre>")){
                //NSROutputs.add(nsrOutput);
                String key=getKeynoFromNSROutput(nsrOutput);
                if(key.length()>0) {
                    //keynosInNSROutputs.add(key);
                    keynoNSROutputMap.put(key, nsrOutput);
                }
                
                nsrOutput=new Vector<String>();
            }
        }
        br.close();
    }
    
    public HashMap<String,Vector<String>> loadNSRsFromFile(String filename) throws Exception{
    	return loadNSRsFromFile(filename,false);
    }
    /*
     * load NSR outputs from the text file of the page of search results in Exchange format
     * This text format of the file is that of the outputs in the web page of the NSR search results from manual search
     * The difference between the two text formats(see above method) is that "<" and ">" around the title word of each entry
     * are replaced by "&lt;" and "&gt;", respectively, in the file used in the method above.
     * Note that all methods that process the entries from NSR outputs treat the text in the format in the source file above.
     * So in this loading method here, "<" and ">" are converted to "&lt;" and "&gt;", respectively in each NSR output 
     */
    public HashMap<String,Vector<String>> loadNSRsFromFile(String filepath,boolean append) throws Exception{
    	HashMap<String,Vector<String>> out=new HashMap<String,Vector<String>>();
    	
    	if(!append) {
            //NSROutputs.clear();
            //keynosInNSROutputs.clear();
            keynoNSROutputMap.clear();
    	}
    	
    	//System.out.println("NSR filepath "+filepath+" append="+append);
       
        try{
            BufferedReader br= new BufferedReader(new FileReader(new File(filepath)));      
            String s="";
            Vector<String> nsrOutput=new Vector<String>();
            
            while(true){
                s = br.readLine();
                if (s==null ) break;
                if(s.contains("KEYNO")) 
                    break;
            }
            
            while(true){
                
                if(s==null || s.contains("KEYNO")){
                    if(nsrOutput.size()>0){
                        String key=getKeynoFromNSROutput(nsrOutput);
                        if(key.length()>0){
                            nsrOutput.add(0, "<pre>");
                            nsrOutput.add("</pre>"); 
                            
                            //NSROutputs.add(nsrOutput);
                            //keynosInNSROutputs.add(key);
                            out.put(key, nsrOutput);
                        }
                    }
                
                    if(s==null)
                        break;
                    
                    nsrOutput=new Vector<String>();
                }
                 
                if(s.trim().indexOf("<")==0 && s.trim().indexOf(">")>0){
                    int p=s.indexOf(">");
                    String s1=s.substring(0, p+1).replace("<", "&lt;").replaceAll(">", "&gt;");
                    String s2=s.substring(p+1);
                    s=s1+s2;
                }
                
                if(!s.trim().isEmpty())
                    nsrOutput.add(s);          
                
                s = br.readLine();
            }
                  
            br.close();
        }catch(FileNotFoundException e){
            //e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        keynoNSROutputMap.putAll(out);
        
        return out;
    }
    
    
    public boolean loadNSRs(String filename) throws Exception{
        return loadNSRs(filename,"");
    }
    
    public boolean loadNSRs(String filename,String loginFileName) throws Exception{
        return loadNSRs(filename,loginFileName,true,false);
    }
    
    /*
     * return true if the NSR file already exists and valid
     * otherwise, return false, for which case, NSR are downloaded
     * from NSR database
     * loginFileName=name of the file containing information for logging in
     *               the NSR database directly
     *               If not given, NSR will be obtained by simulating NSR 
     *               webpage search 
     * if isForcedWebQuery=true, always connect to NNDC NSR page for web query and parse the searched results, even if
     *                           the direct connection to database can't be established. Otherwise, stop searching for 
     *                           NSR information if the direct connection failed.      
     *                           This option works only when loginFileName is not empty    
     * if isSilent=true, the content of the login file and exceptions will not be printed.  
     *                           This option works only when loginFileName is not empty 
     */
    public boolean loadNSRs(String filename,String loginFileName,boolean isForcedWebQuery,boolean isSilent) throws Exception{
        //String filename="BaseNSR.dat";
       
        boolean isLoadedFromFile=false;
               
        try{
        
            loadNSRsFromFile(filename);
            
            if(keynoNSROutputMap.size()>0){
                isLoadedFromFile=true;
                
                //check if the NSR file has information for all keynumbers. 
                //example, new keynumbers are added in the ENSDF file while the old NSR file is used.
                //So download NSR for those missing keynumbers and append them into the NSR file        
                Vector<String> tempKeys=new Vector<String>();
                Vector<Vector<String>> tempNSROutputs=new Vector<Vector<String>>();
                
                for(int i=0;i<keynos.length;i++){
                    //debug
                    //System.out.println(" keynumber"+i+"="+keynos[i]+"  "+keynoNSROutputMap.containsKey(keynos[i].toUpperCase()));
                    
                    if(!keynoNSROutputMap.containsKey(keynos[i].toUpperCase())){
                        tempKeys.add(keynos[i]);
                    }                                     
                }
                
                //debug
                //System.out.println("  tempKeys.size="+tempKeys.size());
                
                if(tempKeys.size()>0 && (loginFileName.length()>0||isNSRAccessible())){
                    
                    System.out.println("Some keynumbers are not in the NSR file and their NSR information will be ");
                    System.out.println("     downloaded from NSR database and appended into the existing NSR file.");
                    
                    tempNSROutputs=(Vector<Vector<String>>)downloadNSRsByKeynumbersV0(tempKeys,"",loginFileName,isForcedWebQuery,isSilent);//NSR results not written into the NSR file in this step
                                
                    //System.out.println(tempNSROutputs.size());
                    
                    PrintWriter out=new PrintWriter(new FileWriter(filename,true));    
                    Vector<String> nsrOutput=new Vector<String>();
                    String key="";
                    for(int i=0;i<tempNSROutputs.size();i++){
                        nsrOutput=tempNSROutputs.get(i);
                        
                        key=getKeynoFromNSROutput(nsrOutput);
                        
                        if(nsrOutput.size()>0 && key.length()>0){  
                        	//keynosInNSROutputs.add(key);
                            //NSROutputs.add(nsrOutput);
                            keynoNSROutputMap.put(key, nsrOutput);
                            
                            writeNSR(out,nsrOutput);
                            System.out.println("Append NSR information for keynumber="+keynos[i]+" into the existing file");
                        }
                    }
                    
                    /* old
                    for(int i=0;i<keynos.length;i++){
                        //debug
                        //System.out.println(" keynumber"+i+"="+keynos[i]+"  "+keynosInNSROutputs.contains(keynos[i].toUpperCase()));
                        
                        if(!keynosInNSROutputs.contains(keynos[i].toUpperCase())){
                            nsrOutput=downloadNSRbyKey(keynos[i]);   
                            if(nsrOutput.size()>0){
                                NSROutputs.add(nsrOutput);
                                writeNSR(out,nsrOutput);
                                System.out.println("Append NSR information for keynumber="+keynos[i]+" into the existing file: "+filename);
                            }
                        }                                     
                    }
                    */
                    out.close();
                }
                
            }else if(keynoNSROutputMap.size()==0){//NSR file doesn't exist or is empty
                
                //isNSRAccessible checks connection using NSRLink manager
                //loginFileName is used to check direct access to NSR database
                
                if(loginFileName.length()>0 || isNSRAccessible()){
                	Vector<Vector<String>> NSROutputs=(Vector<Vector<String>>)downloadAndWriteNSRs(filename,loginFileName,isForcedWebQuery,isSilent);
                    //keynosInNSROutputs.clear();
                    keynoNSROutputMap.clear();
                     
                    Vector<String> nsrOutput=new Vector<String>();
                    String key="";
                    for(int i=0;i<NSROutputs.size();i++){
                        nsrOutput=NSROutputs.get(i);
                        key=getKeynoFromNSROutput(nsrOutput);
                        if(nsrOutput.size()>0 && key.length()>0){  
                        	//keynosInNSROutputs.add(key);
                            keynoNSROutputMap.put(key, nsrOutput);
                        }
                    }
                    
                }else{
                    String msg="can't access the NSR database and NSR information will not be downloaded.";
                    //System.out.println("*** check NSR link manager ***");
                    throw new Exception(msg);
                }
                
                return false;
            }
           
        }catch (Exception e){
            e.printStackTrace();
            try {
            	Vector<Vector<String>> NSROutputs=(Vector<Vector<String>>)downloadAndWriteNSRs(filename);
                //keynosInNSROutputs.clear();
                keynoNSROutputMap.clear();
                 
                Vector<String> nsrOutput=new Vector<String>();
                String key="";
                for(int i=0;i<NSROutputs.size();i++){
                    nsrOutput=NSROutputs.get(i);
                    key=getKeynoFromNSROutput(nsrOutput);
                    if(nsrOutput.size()>0 && key.length()>0){  
                    	//keynosInNSROutputs.add(key);
                        keynoNSROutputMap.put(key, nsrOutput);
                    }
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        return isLoadedFromFile;
    }
    
    /*get keynumber from the NSR HTML information*/ 
    public String getKeynoFromNSROutput(Vector<String> nsrSearchOutput){
        return BaseNSR.findKeynumberFromNSROutput(nsrSearchOutput);
    }
    
    /*
    public Vector<String> getNSROutputbyKey(String keynumber){
        Vector<String> nsr=new Vector<String>();
        String temp="";
        Vector<String> tempNSR;
        int index=0;
        for(int i=0;i<NSROutputs.size();i++){
            tempNSR=NSROutputs.elementAt(i);
            for(int j=0;j<tempBaseNSR.size();j++){
                temp=tempBaseNSR.elementAt(j).toUpperCase();
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
    */
    public Vector<String> getNSROutputbyKey(String keynumber){
    	Vector<String> nsr=new Vector<String>();
    	try {
    		return this.keynoNSROutputMap.get(keynumber.toUpperCase().trim());
    	}catch(Exception e) {
    		
    	}
    	
    	return nsr;
    }
    /** write HTML NSR information to the NSR file */
    public void writeNSR(java.io.Writer out,Vector<String> nsr)throws IOException{
        for(int j=0;j<nsr.size();j++)
            out.write(nsr.elementAt(j)+"\n");
    }
    
    /** Writes the reference list in LaTeX format */
    public void write(java.io.Writer out,String A,boolean suppress)throws IOException{
        out.write("\\newpage\n");
        out.write("\\pagestyle{plain}\n");
        out.write("\\section[References]{ }\n");
        out.write("\\vspace{-30pt}\n");
        out.write("\\begin{longtable}{l@{\\hskip 0.9cm}l}\n");
        String temp=("\\multicolumn{2}{c}{REFERENCES FOR");
        if(A.contains(",")) 
            temp+=(" A's="+A);
        else if(!A.isEmpty()) 
            temp+=(" A="+A);
        else
            temp="";
        
        if(!temp.isEmpty()){
            out.write(temp+"}\\\\\n&\\endfirsthead\n");
            out.write(temp+"(CONTINUED)}\\\\\n&\\endhead\n");
        }

    
        Vector<String> nsrOutput;//store search result for a NSR keynumber (HTML source page)
        HashMap<String,String> hm=new HashMap<String,String>();
        
        String tit="",aut="",ref="",coden="";
        //debug
        //System.out.println("In Reference.write(): keynos.length"+keynos.length);
        
        String dataOut="";
        for(int i=0;i<keynos.length;i++){
            try{
                tit="";aut="";ref="";coden="";
                
                nsrOutput=getNSROutputbyKey(keynos[i]);
                
                
                if(nsrOutput.size()<=0){
                    System.out.println("Warning: Nothing is read from NSR database for keynumber="+keynos[i]);   
                }
                
                
                //debug
                //System.out.println(" keynumber"+i+"="+keynos[i]+" "+nsrOutput.size());
                
                /////////////////
                hm=parseNSR(nsrOutput);
                /////////////////
                
                
                Iterator<String> rs=hm.keySet().iterator();
                String key="";
                while(rs.hasNext()){
                    
                    key=rs.next();
                    
                    if(key=="AUTHORS") aut=hm.get(key);
                    else if(key=="REFERENCE") ref=hm.get(key);
                    else if(key=="TITLE") tit=hm.get(key);
                    else if(key=="CODEN") coden=hm.get(key);
                    else continue;
                }
                 
                if(ref.length()<=0 && coden.length()>0)
                    ref=coden;
                
                if(ref.length()>0 && ref.charAt(ref.length()-1)!='.')
                    ref+='.';
                
                if(ref.length()>0)
                    ref=Translator.translate(ref.trim());
                if(aut.length()>0)
                    aut=Translator.translate(aut.trim());
                
                String[] authors;
                if(aut.length()>0 && !NDSControl.showAllAuthors){
                    String s=aut;
                    s=s.replace(" and ", "&");
                    authors=s.split("[,\\&]");
                    
                    int nAuthorsToShow=NDSControl.numberOfAuthorsToShow;
                    
                    Vector<String> tempAuthors=new Vector<String>();
                    
                    for(int j=0;j<authors.length;j++) 
                        if(authors[j].trim().length()>0){
                            tempAuthors.add(authors[j].trim());
                            //System.out.println("In Reference Line 268: authors="+authors[j]);
                        }
                    
                    authors=new String[tempAuthors.size()];
                    tempAuthors.copyInto(authors);
                    int nAuthors=tempAuthors.size();
                    if(nAuthors>nAuthorsToShow+1){
                        
                        aut="";
                        s=", ";
                        for(int k=0;k<nAuthorsToShow;k++){
                            if(k==nAuthorsToShow-1)
                                s=" et al.";
                            aut+=authors[k]+s;
                        }
                    }
                }
                
                //debug
                //System.out.println("In Reference Line 285: authors="+aut+" ref="+ref);
                
                if(aut.length()>0 && ref.length()>0)
                    dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm}"+aut+" - "+ref+"}\\\\\n";
                else if(ref.length()>0){
                    dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm}"+ref+"}\\\\\n";
                    warnings+="\n*** Warning: no author information in NSR reference for keynumber="+keynos[i]+" ***";                  
                }
                else{
                    dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm} \\bf *** Error: no reference found for this keynumber ***}\\\\\n";
                    warnings+="\n*** Error: no reference found in NSR for keynumber="+keynos[i]+" ***";  
                }
               
            }catch(Exception e){ 
                //e.printStackTrace();
                dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm} \\bf *** Error: no reference found for this keynumber ***}\\\\\n";            
            }
                       
            
            out.write(printKeynumber(keynos[i])+"&"+dataOut);

                        
            if(!suppress && ref.length()>0 && tit.trim().length()>0){
                
                //debug
                //System.out.println("In Reference A: "+hm.get("KEYNUMBER")+" title="+tit);
                
                tit=tit.trim();
                
                //symbol "<",">",... in title is in HTML format and need to be converted to ENSDF format  
                //there could be more to be converted.
                tit=tit.replaceAll("&lt;", "<");
                tit=tit.replaceAll("&gt;", ">");
                tit=tit.replaceAll("&le;", "|<");
                tit=tit.replaceAll("&ge;", "|>");
                tit=tit.replaceAll("&asymp;", "|?");
                
                tit=Translator.translate(tit.trim());
                
                
                if(tit.charAt(tit.length()-1)!='.')
                    tit+='.';
                
                dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm} \\textit{"+tit+"}}\\\\\n";
                out.write("&"+dataOut);
                
                //debug
                //System.out.println("In Reference B: "+hm.get("KEYNUMBER")+" title="+tit+" "+dataOut);
            }
        }
        out.write("\\end{longtable}\n");
    }
    
    /*
    public void write_old(java.io.Writer out,String A,boolean suppress)throws IOException{
        out.write("\\newpage\n");
        out.write("\\pagestyle{plain}\n");
        out.write("\\section[ ]{ }\n");
        out.write("\\vspace{-30pt}\n");
        out.write("\\begin{longtable}{ll}\n");
        String temp=("\\multicolumn{2}{c}{REFERENCES FOR");
        if(A.contains(",")) temp+=(" A's="+A);
        else temp+=(" A="+A);
        out.write(temp+"}\\\\\n&\\endfirsthead\n");
        out.write(temp+"(CONTINUED)}\\\\\n&\\endhead\n");
        
        Statement stmt=null;
        String tit="",aut="",ref="";
        String sql="select line_text from exch_tbl where keyno='";
        boolean inAuthor,inTitle;
        try{stmt=initDatabase();}
        catch(Exception e ){
            System.out.println("error accessing nsr database");
        }
        ResultSet rs=null;
        String dataOut="";
        for(int i=0;i<keynos.length;i++){
            try{
                tit="";aut="";ref="";
                rs=stmt.executeQuery(sql+keynos[i]+"'");
                inAuthor=false;inTitle=false;
                while(rs.next()){
                    temp=rs.getString("line_text");
                    if(temp.contains("<AUTHORS >")) inAuthor=true;
                    if(temp.contains("<REFRENCE>")) ref=temp.substring(temp.indexOf(">")+1);
                    if(temp.contains("<TITLE   >")){ inTitle=true; inAuthor=false;}
                    if(temp.contains("<KEYWORDS>")) inTitle=false;

                    if(inAuthor) aut+=temp.substring(temp.indexOf(">")+1);
                    if(inTitle) tit+=temp.substring(temp.indexOf(">")+1);
                    dataOut="\\parbox[t]{6in}{";
                    if(!suppress)  dataOut+="'"+tit+"'";
                    dataOut+=(aut+" - "+ref+"}\\\\\n");
                }
            }catch(Exception e){ dataOut="";}
            out.write(keynos[i]+"&"+dataOut+"\\\\\n");
        }
        out.write("\\end{longtable}\n");
    }
    */
    
    /** Writes the reference list in LaTeX format */
    public void write_old2(java.io.Writer out,String A,boolean suppress)throws IOException{
        out.write("\\newpage\n");
        out.write("\\pagestyle{plain}\n");
        out.write("\\section[ ]{ }\n");
        out.write("\\vspace{-30pt}\n");
        out.write("\\begin{longtable}{l@{\\hskip 0.9cm}l}\n");
        String temp=("\\multicolumn{2}{c}{REFERENCES FOR");
        if(A.contains(",")) temp+=(" A's="+A);
        else temp+=(" A="+A);
        out.write(temp+"}\\\\\n&\\endfirsthead\n");
        out.write(temp+"(CONTINUED)}\\\\\n&\\endhead\n");
        
        
        String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
            
        //NSR Link Manager, return exchange-format ("X" mode), HTML format ("H" mode), redirect to paper page ("B" mode)
        //process one keynumber only
        String mode=",X";
        String url = "https://www.nndc.bnl.gov/nsr/nsrlink.jsp?";       
        URLConnection connection;
        InputStream response;
        Vector<String> nsr;//store search result for a NSR keynumber (HTML source page)
        HashMap<String,String> hm=new HashMap<String,String>();
        
        String tit="",aut="",ref="",coden="";
        //debug
        //System.out.println("In Reference.write(): keynos.length"+keynos.length);
        
        String dataOut="";
        for(int i=0;i<keynos.length;i++){
            try{
                tit="";aut="";ref="";coden="";
                
                url=url+keynos[i]+mode;
                url=BaseNSR.convertToNewURL2024(url);
                
                connection = new URL(url).openConnection();
                connection.setRequestProperty("Accept-Charset", charset);
                response = connection.getInputStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(response));
                nsr=new Vector<String>();
                String str=br.readLine();
                while(str!=null){
                    nsr.addElement(str);
                    str=br.readLine();
                }
                br.close();
                
                if(nsr.size()<=0){
                    System.out.println("Warning: Nothing is read from NSR database for keynumber="+keynos[i]);
                    continue;
                }
                    
                /////////////////
                hm=parseNSR(nsr);
                /////////////////
                
                
                Iterator<String> rs=hm.keySet().iterator();
                String key="";
                while(rs.hasNext()){
                    
                    key=rs.next();
                    
                    if(key=="AUTHORS") aut=hm.get(key);
                    else if(key=="REFERENCE") ref=hm.get(key);
                    else if(key=="TITLE") tit=hm.get(key);
                    else if(key=="CODEN") coden=hm.get(key);
                    else continue;
                }
       
                if(ref.length()<=0 && coden.length()>0)
                    ref=coden;
                    
                if(ref.length()>0 && ref.charAt(ref.length()-1)!='.')
                    ref+='.';
                
                if(ref.length()>0)
                    ref=Translator.translate(ref.trim());
                if(aut.length()>0)
                    aut=Translator.translate(aut.trim());
                
                if(aut.length()>0 && ref.length()>0)
                    dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm}"+aut+" - "+ref+"}\\\\\n";
                else if(ref.length()>0)
                    dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm}"+ref+"}\\\\\n";
                else
                    dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm} *** Error: no reference found for this keynumber ***}\\\\\n";
               
            }catch(Exception e){ 
                dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm} *** Error: something wrong with this keynumber ***}\\\\\n";               
            }
                       
            
            out.write(keynos[i]+"&"+dataOut);

                        
            if(!suppress && ref.length()>0 && tit.trim().length()>0){
                
                //debug
                //System.out.println("In Reference A: "+hm.get("KEYNUMBER")+" title="+tit);
                
                tit=tit.trim();
                
                //symbol "<",">",... in title is in HTML format and need to be converted to ENSDF format  
                //there could be more to be converted.
                tit=tit.replaceAll("&lt;", "<");
                tit=tit.replaceAll("&gt;", ">");
                tit=tit.replaceAll("&le;", "|<");
                tit=tit.replaceAll("&ge;", "|>");
                tit=tit.replaceAll("&asymp;", "|?");
                
                tit=Translator.translate(tit.trim());
                
                
                if(tit.charAt(tit.length()-1)!='.')
                    tit+='.';
                
                dataOut="\\parbox[t]{6in}{\\addtolength{\\parindent}{-0.25cm} \\textit{"+tit+"}}\\\\\n";
                out.write("&"+dataOut);
                
                //debug
                //System.out.println("In Reference B: "+hm.get("KEYNUMBER")+" title="+tit+" "+dataOut);
            }
        }
        out.write("\\end{longtable}\n");
    }
    
    
    
    

    /*
     * check if the NSR database is accessible by using the current NSR Line manager
     * before the program can process to download the NSR 
     */
    public boolean isNSRAccessible(){
        return BaseNSR.isNSRAccessible();
    }
    
    /*
     * using URLConnection with "nsrlink.jsp" 
     * Download reference information from NSR for a given keynumber
     */
    public Vector<String> downloadNSRByKeynumber_NSRLink(String keynumber)throws Exception{
    	return BaseNSR.downloadNSRByKeynumber_NSRLink(keynumber);
    }
    

    
    /*
     * Download reference information from NSR using NSRLink manager for keynumbers one by one
     * very slow for large number of keynumbers
     */
    public Vector<Vector<String>> downloadAndWriteNSRs_old(String filename)throws Exception{
               
        Vector<String> nsr;//store search result for a NSR keynumber (HTML source page)

        PrintWriter out=new PrintWriter(new File(filename));
        
        Vector<Vector<String>> NSRs=new Vector<Vector<String>>();

        for(int i=0;i<keynos.length;i++){

            nsr=downloadNSRByKeynumber_NSRLink(keynos[i]);
            
            if(nsr.size()<=0)
                continue;
            
            NSRs.add(nsr);
            writeNSR(out,nsr);
        }
        
        out.close();
        return NSRs;               
    }
    
    /*
     * Download reference information from NSR for all keynumbers in the input file
     * using URLConnection with all keynumbers searched at one time.
     * much faster than the old method using NSRLink manager
     */
    public Vector<Vector<String>> downloadAndWriteNSRs(String nsrFilename)throws Exception{
        
        return downloadAndWriteNSRs(nsrFilename,"");              
    }
    
    public Vector<Vector<String>> downloadAndWriteNSRs(String nsrFilename,String loginFileName)throws Exception{
        
        return downloadAndWriteNSRs(nsrFilename,loginFileName,true,false);              
    }
    
    /*
     * download NSR information for keynumbers that have been read from the input ENSDF file
     */
    public Vector<Vector<String>> downloadAndWriteNSRs(String nsrFilename,String loginFileName,boolean isForcedWebQuery,boolean isSilent)throws Exception{
        return downloadNSRsByKeynumbers(keynos,nsrFilename,loginFileName,isForcedWebQuery,isSilent);
    }
    
    public Vector<Vector<String>> downloadNSRsByKeynumbers(String[] keynos,String nsrFilename,String loginFileName,boolean isForcedWebQuery,boolean isSilent)throws Exception{
        Vector<String> keynosV=new Vector<String>();
        for(int i=0;i<keynos.length;i++)
            keynosV.add(keynos[i]);
        
        return  downloadNSRsByKeynumbersV0(keynosV,nsrFilename,loginFileName,isForcedWebQuery,isSilent);
    }
    
    /*
     * Download reference information from NSR for all keynumbers in the input file
     * if isUseJDBC=true, access NSR database using JDBC 
     * if isForcedWebQuery=true, always connect to NNDC NSR page for web query and parse the searched results, even if
     *                           the direct connection to database can't be established. Otherwise, stop searching for 
     *                           NSR information if the direct connection failed.
     *                           This option works only when loginFileName is not empty      
     */
    public Vector<Vector<String>> downloadNSRsByKeynumbersV0(Vector<String> keynosV,String nsrFilename,String loginFileName,boolean isForcedWebQuery,boolean isSilent)throws Exception{
        
        Vector<String> nsr;//store search result for a NSR keynumber (HTML source page)

        PrintWriter out=null;
        if(nsrFilename.trim().length()>0)
            out=new PrintWriter(new File(nsrFilename));
        
        boolean isUseJDBC=false;
        
        Vector<Vector<String>> NSRs=new Vector<Vector<String>>();
        
        if(loginFileName.trim().length()>0){
            NSRs=downloadNSRsByKeynumbersV_JDBC(keynosV,loginFileName,isSilent);//use JDBC to access NSR database directly
            if(NSRs==null){
                isUseJDBC=false;            
                if(!isForcedWebQuery){
                    //for use of JDBC database only, without further NSR web query
                    System.out.println("Error: unable to connect to NSR database by JDBC.");
                    System.out.println("       Please check the login file.");
                    return new Vector<Vector<String>>();
                }else{
                    System.out.println("Warning: unable to connect to NSR database by JDBC.");
                    System.out.println("         NSR information will be obtained by simulating webpage search.");
                }
            }else if(NSRs.size()==0){
                isUseJDBC=false;            
                if(!isForcedWebQuery){
                    //for use of JDBC database only, without further NSR web query
                    System.out.println("Warning: no matching NSR information in NSR database (JDBC).");
                    return new Vector<Vector<String>>();
                }else{
                    System.out.println("Warning: no matching NSR information in NSR database (JDBC).");
                    System.out.println("         NSR information will be obtained by simulating webpage search.");
                }
            }else
                isUseJDBC=true;
        }
        
        if(!isUseJDBC){

            NSRs=downloadNSRsByKeynumbersV_URLC(keynosV);//indirectly access using web connection
        }           

        if(out!=null){
            for(int i=0;i<NSRs.size();i++){

                nsr=NSRs.get(i);
                
                if(nsr.size()<=0)
                    continue;

                writeNSR(out,nsr);
            }
            
            out.close();
        }
        
        return NSRs;               
    }
    
    public static Vector<Vector<String>> downloadNSRsByKeynumbers_URLC(String[] keynumbers)throws Exception{
        Vector<String> keynosV=new Vector<String>();
        for(int i=0;i<keynumbers.length;i++)
            keynosV.add(keynumbers[i]);
        
        return downloadNSRsByKeynumbersV_URLC(keynosV);
    }
    
    /* 
     * using URLConnection with all keynumbers searched at one time
     * Download reference information from NSR for a given list of keynumbers 
     * wrap of downloadNSRsByKeynumbersV100() for processing >100 keynumbers, since that function can only process up to
     * 100 keynumbers (even the option "view all" is selected; don't know the reason so far)
     */    
    public static Vector<Vector<String>> downloadNSRsByKeynumbersV_URLC(Vector<String> keynumbers)throws Exception{
        Vector<Vector<String>> nsrV=new Vector<Vector<String>>();
        Vector<Vector<String>> tempV=new Vector<Vector<String>>();
 
        
        //long startTime,endTime;
        //float timeElapsed;//in second
        //startTime=System.currentTimeMillis();
        
        
        int LIMIT=100;//limit of numbers for NSR search in a single page (even if I set nsrev=yes for "view all")

        int size=keynumbers.size();       
        int nRuns=size/LIMIT;

        if(size<=0){
            System.out.println("Warning: No keynumbers to search for from NSR database");
            return nsrV;
        }

        if(size%LIMIT!=0)
            nRuns+=1;

        Vector<String> tempKeynos=new Vector<String>();;
        int start=0,end=0;
        for(int i=0;i<nRuns;i++){
            start=i*LIMIT;
            end=(i+1)*LIMIT;
            
            if(end>=size)
                end=size;
            
            tempKeynos.clear();
            tempKeynos.addAll(keynumbers.subList(start,end));
            
            tempV=downloadNSRsByKeynumbersV100_URLC(tempKeynos);
            
            nsrV.addAll(tempV);
        }
        
        //endTime=System.currentTimeMillis();
        //timeElapsed=(float)(endTime-startTime)/1000;
        //System.out.println("Time elapsed for downloading NSR: "+String.format("%.3f", timeElapsed)+" seconds");
        
        return nsrV;
    }
    
    public Vector<Vector<String>> downloadNSRsByKeynumbers_JDBC(String[] keynumbers,String loginFileName,boolean isSilent)throws Exception{
        Vector<String> keynosV=new Vector<String>();
        for(int i=0;i<keynumbers.length;i++)
            keynosV.add(keynumbers[i]);
        
        return downloadNSRsByKeynumbersV_JDBC(keynosV,loginFileName,isSilent);
    }
    
    public static Vector<Vector<String>> downloadNSRsByKeynumbersV_JDBC(Vector<String> keynumbers,String loginFileName,boolean isSilent)throws Exception{
    	return BaseNSR.downloadNSRsByKeynumbersV_JDBC(keynumbers, loginFileName, isSilent);
    }
    
    /* 
     * using URLConnection with parameters in address line and "knum_act.jsp"
     * Download reference information from NSR for a given list of keynumbers
     * size<=100 for keynumbers, otherwise only the first 100 keynumbers are searched
     */
    protected static Vector<Vector<String>> downloadNSRsByKeynumbersV100_URLC(Vector<String> keynumbers)throws IOException{                 
        return BaseNSR.downloadNSRsByKeynumbersV100_URLC(keynumbers);             
    }
    
    public static HashMap<String,Vector<String>> makeNSROutputsMap(InputStreamReader reader){
    	return BaseNSR.makeNSROutputsMap(reader);
    }
    
    /** returns true if 'text' is a valid keynumber with correct or incorrect letter case */
    public static boolean checkIfKeyno(String text){
        boolean isKeyNo=false;
        if(text.length()==8||text.length()==6){
            isKeyNo=true;
            for(int j=0;j<text.length()-4;j++)
                if(text.charAt(j)<'0'||text.charAt(j)>'9') isKeyNo=false;
            
            if(!Character.isLetter(text.charAt(text.length()-4))) isKeyNo=false;
            if(!Character.isLetter(text.charAt(text.length()-3))) isKeyNo=false;
            
            for(int j=text.length()-2;j<text.length()-1;j++)
                if(!Character.isLetterOrDigit(text.charAt(j))) 
                    isKeyNo=false;

        }
        return isKeyNo;
    }    
    
    /** returns true if 'text' is a valid keynumber with correct letter case */
    public static boolean checkIfKeynoInCorrectCase(String text){
        boolean isKeyNo=false;
        if(text.length()==8||text.length()==6){
            isKeyNo=true;
            for(int j=0;j<text.length()-4;j++)
                if(text.charAt(j)<'0'||text.charAt(j)>'9') isKeyNo=false;
            if(text.charAt(text.length()-4)<'A'||text.charAt(text.length()-4)>'Z') isKeyNo=false;
            if((text.charAt(text.length()-3)<'A'||text.charAt(5)>'Z')&&(text.charAt(text.length()-3)<'a'||text.charAt(5)>'z')) isKeyNo=false;
            for(int j=text.length()-2;j<text.length()-1;j++)
                if((text.charAt(j)<'0'||text.charAt(j)>'9')&&(text.charAt(j)<'A'||text.charAt(j)>'Z')) isKeyNo=false;

        }
        return isKeyNo;
    } 

    //StringBuffer sb must contain contents obtained using NSR Link Manager with "X" mode
    //added by Jun 8/26/2015
    public static HashMap<String,String> parseNSR(Vector<String> nsrOutput) throws Exception{
        return BaseNSR.parseNSR(nsrOutput);
    }


    public String[] getKeyNumbers(){
        return keynos;
    }
    
    public String printKeynumber(String keyNo){
        return EnsdfUtil.printKeynumber(keyNo);
        
    }
    public void resetWarnings(){
        warnings="";
    }
    
    public void setWarnings(String w){warnings=w;}
    
    public String getWarnings(){return warnings;}
    
    /*
    //get webpage link for a keynumbers using NSR link manager
    //For example,
    //     https://www.nndc.bnl.gov/nsr/nsrlink.jsp?2004HE05,B
    //It directs to the journal webpage if available.
    //If it cannot be directed to the journal but it is valid keynumber
    //it will direct to the NSR webpage of that keynumber.
    //If it is not valid keynumber, it will still direct to NSR webpage
    //but show "No entry found for..."
    
    //Users may embed links to NSR reference information in html pages by using the NSR "link manager". 
    //The URL for the link should be in the form https://www.nndc.bnl.gov/nsr/nsrlink.jsp?{keynum}[,{mode}] 
    //where {keynum} is a valid eight-character NSR keynumber, and {mode} is an optional retrieval mode 
    //specifier consisting of the single character H, X, or B. 'H' specifies html-formatted output, and is 
    //the default if no specifier is given. 'X' specifies exchange-format output, and 'B' indicates that 
    //the browser should be redirected to the publisher's page for the reference, if a link is available in the database. 
     * 
     */
    public String NSRLinkManager(String keynumber,char option){
        return BaseNSR.NSRLinkManager(keynumber, option);
    }
    
    public class KeyComparator implements Comparator<Object>{
        public int compare(Object one,Object two){
            String a=(String)one;
            String b=(String)two;
            
            int out=a.compareToIgnoreCase(b);
            return out;
        }
    }    
}

