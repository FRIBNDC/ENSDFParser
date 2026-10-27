 package ensdfparser.ensdf;

import java.util.*;

import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;
public class Comment implements Cloneable{

/*  Comment object,

    A comment is characterized by type, head, flag and body

    General Comment:  "134CE  C This is a general comment" type="  ", Head="", flag="" , body="This...", nFlags=0   

    Level Comment: "134CE  CL $From..."   type="L ", Head="", flag="" , body="from...", nFlags=0    

    Footnote:"134CE  CL E$From..."   type="L ", Head="E", flag="" , body="from...", nFlags=0
    Footnote:"134CE  CL E,T$From..."   type="L ", nHeads=2 headAt(0)=E, headAt(1)=T, flag="" , body="from...", nFlags=0

    Flagged footnote: "134CE  CL E(A)$From..."   type="L ", Head="E(A)", flag="A", body="from...", nFlags=1, flagAt(0)="A"

    Flagged footnote: "134CE  CL E(A,B)$From..."   type="L ", Head="E(A,B)", flag="A,B", body="from...", nFlags=2, flagAt(0)="A", flatAt(1)="B"

    For "132CE  CL E(A,B),RI(A,C),M$...": 
           head()="E(A,B),RI(A,C),M"
           headV()={"E","RI","M"}, headAt(0)="E",headAt(1)="RI",headAt(2)="M"
           
           flags()="ABC"
           flagV()={"(A,B)","(A,C)",""}
           flagVV()={{"A","B"},{"A","C"},{}}
 */

  private int id=-1;
  
  private Vector<String> lines=new Vector<String>();
  private String type;
  private String head;
  private String flags;
  private String body;//could mix up upper-case "C" comment line and lower-case "c" comment line,
                      //if so, convert "C" comment to "c" comment, set bigC=false, body=cbody
  private String cbody;//lower-case "c" comment body of "body"
  private String rawBody;//comment text as is, =body if not mixed "C" and "c" comment line
  private String bold;
  private String substring10;
  private String symbol;
  private String translated;
  private boolean isExplicit;
  private boolean bigC=true;
  private boolean mixC=false;
  private boolean isPeriodAdded=false;
  private boolean hasEndPeriod=false;
  
  private boolean isContinuedXREF=false;

  private boolean sysOut;

  private Vector<String> flagV=new Vector<String>();//flag of each head as given in head, eg E(A,B), flagV(0)=(A,B)
  @SuppressWarnings("rawtypes")
  private Vector<Vector> flagVV=new Vector<Vector>();//flag letters of each head, eg, E(A,B),J(C,D), flagVV(0) is a vector of A and B, flagVV(1) is a vector of C and D
  private Vector<String> headV=new Vector<String>(); //head name of each head, eg, E(A,B), headV(0)=E

  private String tag="";//used to store extra information, like T1/2 tag for T1/2 or widht when T1/2 is relabeled as T or width
  
  public Comment(){

    lines.clear();
    type=""; 
    head="";
    flags="";
    body="";
    cbody="";
    tag="";
    translated="";
    flagV.clear();
    headV.clear();
    substring10="";
    symbol="";
    bigC=true;
    mixC=false;
    isExplicit=true;
    isPeriodAdded=false;
    hasEndPeriod=false;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })

  public Comment clone(){
    try{
        Comment c=(Comment) super.clone();
        
        c.lines=new Vector<String>();
        c.lines.addAll(lines);
        
        c.flagV=new Vector<String>();
        c.flagV.addAll(flagV);
        
        c.headV=new Vector<String>();
        c.headV.addAll(headV);
        
        c.flagVV=new Vector<Vector>();
        for(int i=0;i<flagVV.size();i++){
            Vector v=new Vector();
            v.addAll(flagVV.elementAt(i));
            c.flagVV.addElement(v);
        }
    
        return c;
    }       
    catch (CloneNotSupportedException e) { 
        System.out.println("Cloning not allowed."); 
        return this; 
        

    }     
  }
  
  
  
  //add a period at the end of a comment
  private void addPeriod(Vector<String> cline){  
      
    if(cline==null || cline.size()==0)
        return;
    
    String lastLine=cline.lastElement().replaceAll("\\s+$", "");//right trim
    int length=lastLine.length();
    
    if(length>9 && lastLine.charAt(length-1)!='.'){
        lastLine=lastLine+'.';
        cline.removeElementAt(cline.size()-1);
        cline.add(lastLine);
        isPeriodAdded=true;
        hasEndPeriod=true;
    }
  }
  
  public void removePeriod(){
      //System.out.println("In Comment1: line 175: isPeriodAdded="+isPeriodAdded+" body="+body+"*translated="+translated+"*cbody="+cbody+"*");
      
      body=Str.removeEndPeriod(body);
      translated=Str.removeEndPeriod(translated);
      cbody=Str.removeEndPeriod(cbody);
      
      hasEndPeriod=false;
      isPeriodAdded=false;
      
      //System.out.println("In Comment2: line 175: isPeriodAdded="+isPeriodAdded+" body="+body+"*translated="+translated+"*cbody="+cbody+"*");
      
  }
  
  //check if the total line length exceeds 80 after adding the ending period
  public String checkEndPeriod(String line) {
      int LEN=80;
      if(line.length()<=LEN || !line.endsWith("."))
          return line;
      
      String s=line.substring(0,line.length()-1);
      if(s.length()>LEN)
          return line;
      
      //here s.length=LEN
      return s;
  }
  
  public boolean isPeriodAdded(){return isPeriodAdded;}
  public boolean hasEndPeriod(){return hasEndPeriod;}
  
  public void setValues(Vector<String> v) throws Exception{
      String s="!?.,;:";
      String lastLine=v.lastElement().trim();
      char lastChar=' ';
      int n=lastLine.length();
      while(n>0){
          lastChar=lastLine.charAt(n-1);
          if(lastChar!='}' && lastChar!='{')
              break;
          else
              n--;
      }
      boolean hasEndChar=false;
      if(s.indexOf(lastChar)>=0)
          hasEndChar=true;
      
      //if(lastLine.contains("2005Bl09"))
      //  System.out.println(lastLine+" lastChar="+lastChar+" hasEndChar="+hasEndChar);
      
      if(hasEndChar)
          setValues(v,false);
      else if(v.elementAt(0).contains("LABEL=") ||v.elementAt(0).contains("DICT$")||v.elementAt(0).contains("DICT   "))
          setValues(v,false);
      else
          setValues(v,true);
  }
  
  public void setValues(Vector<String> v, boolean toAddPeriod) throws Exception{
    lines.clear();
	  
    lines.addAll(v);
    
    if(toAddPeriod)
        addPeriod(lines);

    if(lines.lastElement().trim().endsWith("."))
        hasEndPeriod=true;
    else
        hasEndPeriod=false;
    
    if(lines.size() > 0 ) 
        doWork();
    
    //System.out.println("In Comment0: line 180: isPeriodAdded="+isPeriodAdded+" body="+body+"*translated="+translated+"*cbody="+cbody+"*");
    
    if(isPeriodAdded && body.trim().equals(".")){
        removePeriod();
    }
  }
  
  public void setBlank(String itype){
    body="New Comment"; type=itype; head=""; flags=""; flagV.clear();
  }

/*
  public void setValues(String itype, String ibody, String ihead, String iflag){
     body=ibody; type=itype; head=ihead; flag=iflag; 
     String sjunk=flag;
     flagV.clear();
     while( sjunk.length() > 1 ){
         if( ! sjunk.substring(0,1).equals(",") ) flagV.addElement(sjunk.substring(0,1) );
         sjunk=sjunk.substring(1).trim();
     }
     if(sjunk.length() > 0 ) flagV.addElement(sjunk );
  }
*/
  public void setImplicit(){ isExplicit=false;}
  public void setExplicit(){ isExplicit=true;}
  public boolean isExplicit(){ return isExplicit;}

  public void setIsContinuedXREF(boolean b){isContinuedXREF=b;}
  public boolean isContinuedXREF(){return isContinuedXREF;}
  
  /*
   * check if a header string is in a valid format. 
   * Note that it doesn't have to be an actual known header string
   */
  public boolean isValidHeaderStr(String s) {
	  try {
		  char c1=s.charAt(0);
		  if(!Character.isLetter(c1) && c1!='%') //%B- is a valid header name
			  return false;
		  
		  if(s.length()==1) {
			  if(c1=='%')
				  return false;
			  
			  return true;
		  }else if(s.length()==2){
		      char c2=s.charAt(1);
              if(c1=='%'){
                  if(!Character.isLetter(c2))
                      return false;
              }else if(!Character.isLetterOrDigit(c2))
                  return false;
              
              return true;
		  }else{
		      String tempS="";
		      
			  if(s.contains(",")) {
			      if(s.contains("(")) {
			          int nb=0;
			          for(int i=0;i<s.length();i++) {
			              char c=s.charAt(i);
			              if(c=='(')
			                  nb++;
			              else if(c==')')
			                  nb--;
			              else if(c==',' && nb!=0) {
			                  continue;
			              }
			              
			              tempS+=c;
			          }
			          s=tempS;
			      }
			      //System.out.println(" s="+s);
			      
				  String[] temp=s.split("[,]");
				  for(int i=0;i<temp.length;i++) {
					  String s1=temp[i];
					  if(!isValidHeaderStr(s1))
						  return false;
				  }
				  
				  return true;
			  }else {				  
				  tempS=s.substring(1);	
				  if(c1=='%') {
                      tempS=tempS.replace("+","").replace("-", "");
                      if(!Str.isLettersOrDigits(tempS)) 
                          return false;
				  }else {
				      if(tempS.endsWith(")")) {
				          int n=tempS.indexOf("(");
				          if(n<0)
				              return false;
				          
				          s=tempS.substring(n+1,tempS.length()-1);
				          
				          if(s.trim().isEmpty() || s.indexOf(" ")>=0)
				              return false;
				          
				          tempS=tempS.substring(0,n);
				          
				          //System.out.println(s+"#"+tempS);
				      }
				      
				      if(!tempS.isEmpty() && !Str.isLettersOrDigits(tempS)) 
						  return false;
				  }
				  
				  return true;
			  }
  
		  }
		  
	  }catch(Exception e) {
		  
	  }
	  return false;
  }
  @SuppressWarnings("unused")
  /*only used in setValues*/
private void doWork() throws Exception{
//    sysOut=true;
    
      boolean nospace=false;
      
      String line0=(String) lines.elementAt(0);
      String ctype=line0.substring(6,7);
      if(ctype.equals("c") || ctype.equals("d") || ctype.equals("t")) bigC=false;

     
    if(((String) lines.elementAt(0)).length()<8) return;
    
    type=( (String) lines.elementAt(0) ).substring(7,9);  /* Do not trim the type, remember the delayed particles */

    
    if( type.trim().length()==0 ) type="  ";

    
/* Getting the flagged comments: */
  
    body=( (String) lines.elementAt(0) ).substring(9);
    
    //debug
    //if(body.contains("Adopted Gammas"))
    //  System.out.println("In Comment: doWOrk: line="+line+" body="+body);
    String tempBody=body.trim();
    int lastIndex=tempBody.lastIndexOf("\\");
    if(lastIndex==tempBody.length()-1){//|\ is translated as '|'
        nospace=true;
        body=body.replace("\\ ", " ");//replace concatenation symbol '\'
        if(body.lastIndexOf("\\")==body.length()-1)
            body=body.substring(0, body.length()-1);
    }
    
    //for removing '\' symbol in printing degree circle
    while(body.indexOf(" \\|'")>=0)       
    	body=body.replace(" \\|'", "\\|'");
    
    
    body=body.replace("\\|'", "|'");    

    
//    if( type.trim().length() > 0 ){

       int id=body.indexOf("$");
       
       //debug
       //if(body.contains("Adopted Gammas"))
       //if(line0.contains("COMPA"))
       //   System.out.println("In Comment: doWOrk: id="+id+" text="+body+" translated="+translated+" body="+body+" type="+type+"#");
        
       if( id < 0 ){
        
         //if the flag and the comment are separated by a space instead of a $ sign.
         //comment body must start at ncol>=19, head is in ncol=9-17, column 18 must be blank
    	   if(body.length()<10 || body.charAt(9)!=' ' || (type.trim().isEmpty()&&(body.indexOf("  ")<0||body.indexOf("  ")>9)) ) {
    		   //there is case where record type is missing but header is present
               head="";
               body=body.trim();
    	   }else {   	         
    		   int ih=body.indexOf(" ");   	         
    		   if (ih>=10 || ih<0 || body.substring(ih).trim().length()<1) { //ih=10 is ncol=19 (start from 0)      	           
    			   ih=0;      	         
    		   }

    		    
    		   String phead=body.substring(0,ih).trim();
    	       String pbody=body.substring(ih).trim();

    	       int ib=body.indexOf(pbody,ih);
    	       if(ib<=9) {
    	    	   ih=0;
        		   phead=body.substring(0,ih).trim();
        	       pbody=body.substring(ih).trim();
    	       }
    	       if(pbody.length() == 0 || phead.length()==0 || !isValidHeaderStr(phead)){
    	    	   head="";    	           
    	    	   body=body.trim();   	         
    	       }else{   	            
    	    	   body=pbody;   	           
    	    	   head=phead;         	         
    	       }       	         
    	   }
       }
       else if( id == 0 ){
         head="";
         body=body.substring(1).trim();
       }
       else if( id > 0 ){
         head=body.substring(0,id).trim();
         body=body.substring(id+1).trim();
       }
  //   }

       if(bigC){ 
           //translated=Translator.process(body);
           cbody=Translator.procGenCom(body, 0, true);
       }
       else {
           //translated=Translator.translate(body);
           cbody=body;
       }

       
       //debug
       //if(body.contains("using the calculated"))
       //   System.out.println("In Comment: doWork: text="+body+" translated="+translated+" head="+head+" cbody="+cbody);
        
       int length=0;
       String temp="",slashes=" ",wspace=" ";
       boolean tempBigC;
       
       tempBody=cbody;
       translated="";
       
       mixC=false;
       for( int j=1; j< lines.size() ; j++){
         slashes="";
         temp="";
         wspace=" ";
         
         
         length=lines.elementAt(j).length();
         if(length<7){
             System.out.println("In Comment: comment line="+lines+" is not correct! Please check.");
             continue;
         }
           
         String tempType=((String)lines.elementAt(j)).substring(6,7);
         tempBigC=(tempType.equals("C")||tempType.equals("D")||tempType.equals("T"));
         //tempBigC=(tempType.equals("C"));
         if(!mixC && tempBigC!=bigC){//if mix "C" and "c" comment line, convert all "C" lines to "c" lines
             mixC=true;
             bigC=false;
         }
         
                 
         if(length>9)
             temp=((String)lines.elementAt(j)).substring(9).trim();
                     
         
         if(((String)lines.elementAt(j)).charAt(5)=='@'){//add to last comment if same type, but start a new paragraph 
             /*
             //skip empty line
             if(temp.length()>0) 
                 slashes="\n\n";
             else 
                 continue;
             */
             
             slashes="\n\n";
             
         }
         else if(((String)lines.elementAt(j)).charAt(5)=='#'){//add to last comment if same type, but start a new line with indent
             /*
             //skip empty line
             if(temp.length()>0) 
                 slashes="\\ \\\\\n ";//"\\ " a space is added to avoid ending a empty line
                 //slashes="\\\\\n";
             else 
                 continue;
             */
             
             slashes="\\\\\n ";
             if(tempBody.trim().length()==0)
                 slashes="\\ "+slashes;//"\\ " a space is added to avoid ending a empty line
             
         }
         else if(((String)lines.elementAt(j)).charAt(5)=='!'){//start a new page
             slashes="\n\\newpage\n";
         }
         
         //NOTE that maker=@,#,! work only for writing comments in abstract which uses "translated" of comments,
         //but not work for regular comments which are translated elsewhere differently.
         if(slashes.length()>0 && tempBody.length()>0){//if a continuation comment line is marked by @,#,!, translate lines before this line first.
             translated+=Translator.translate(tempBody)+slashes;
             tempBody="";
             
             //System.out.println("  111 translated="+translated);
         }
        
         
         int n=temp.trim().indexOf("\\");
         if(n==0){
             boolean ns=true;
             if(temp.trim().length()>1&&temp.trim().charAt(1)=='s')// \s translated as whitespace {\\ }
                 if(temp.trim().length()<=2 || temp.trim().charAt(2)==' ')
                     ns=false;
             
             if(ns){
                 n=temp.indexOf("\\");
                 if(temp.length()>1)
                     temp=temp.substring(n+1);
                 else
                     temp="";
             }
                 
             if(!nospace) nospace=ns;
                         
         }
         
         if(nospace) wspace="";//nospace determined from previous line and current line
         
         n=temp.lastIndexOf("\\");
         if(n>=0&&n==temp.length()-1){
            nospace=true;//to be used for next line
            temp=temp.replace("\\ ", " ");//replace concatenation symbol '\'
            if(temp.lastIndexOf("\\")==temp.length()-1)
                temp=temp.substring(0, temp.length()-1);
                        
         }else
            nospace=false;//to be used for next line
         
         
         //for removing '\' symbol in printing degree circle
         while(temp.indexOf(" \\|'")>=0)
            temp=temp.replace(" \\|'", "\\|'");
         
         temp=temp.replace("\\|'", "|'");
         
         body+=wspace+temp;
             
         if(tempBigC){
             cbody+=wspace+Translator.procGenCom(temp, 0, false);
             tempBody+=wspace+Translator.procGenCom(temp, 0, false);

             //System.out.println("####"+temp+"  $$"+tempBody);
             //if(slashes.length()>0)//start a new line or new page, the following lines are precessed separately
             //  translated+=wspace+slashes+Translator.process(temp,false);
         }
         else{ 
             cbody+=wspace+temp;
             tempBody+=wspace+temp;
             
             //if(slashes.length()>0)
             //  translated+=wspace+slashes+Translator.translate(temp);
         }
         
                     
       }//end for
       
       //System.out.println(" ### tempBody="+tempBody+"  ###translated="+Translator.translate(tempBody));
       
       if(tempBody.length()>0)
           translated+=Translator.translate(tempBody);       

       
       //debug
       //System.out.println("***body="+tempBody);
       //System.out.println("Comment 574: translated="+translated);
       
       if(type.contains("G"))translated=translated.replace("TI", "I($\\gamma$+ce)");
    
       if(type.contains("E"))translated=translated.replace("TI", "I($\\epsilon$+$\\beta^{+}$)");
     
    
       body=body.trim();     
       cbody=cbody.trim();    
       rawBody=body;
       
       if(mixC){   
           body=cbody;   
           bigC=false;   
       }
     
     
     //debug
     //if(body.contains("Adopted Gammas"))
     // System.out.println("In Comment: doWOrk 2: text="+body+"\n translated="+translated);
     
     String sjunk=head;

     /* first we get rid of the "()" to count the heads, ie E(A,B),T(D) --> E,T */

     int ipi=sjunk.indexOf("(");
     int ipf=sjunk.indexOf(")");
     while( ipi > 0 && ipf > ipi ){
        sjunk=sjunk.substring(0,ipi)+sjunk.substring(ipf+1);
        ipi=sjunk.indexOf("(");
        ipf=sjunk.indexOf(")");
     }

     /* Now we get the heads into the headV */

     String ssjunk=sjunk;
     int ic=ssjunk.indexOf(",");
     while( ic > 0 ){
           headV.addElement( ssjunk.substring(0,ic) );
           ssjunk=ssjunk.substring(ic+1).trim();
           ic=ssjunk.indexOf(",");
     }
     if( ssjunk.length() > 0 ) headV.addElement(ssjunk);

     /* Now we fill the flagV and the flagCV with trivial values */

     flagV.clear();
     flags="";
     for(int j=0; j<headV.size(); j++){ flagV.addElement(""); Vector<String> iflagV=new Vector<String>(); flagVV.addElement( iflagV );}
  
     /* Now we fill the flagV and the flagCV with the right values: */

     sjunk=head;
     ipi=sjunk.indexOf("(");
     ipf=sjunk.indexOf(")");
     while( ipi > 0 && ipf > ipi ){

        /* For the example: (String) flagV.elementAt(0)="(A,B)" and (String) flagV.elementAt(1)="(D)" */

        int nc=0;
        for( int j=0; j< ipi; j++){
          if( sjunk.substring(j,j+1).equals(",") ) nc++;
        }
        flagV.setElementAt(sjunk.substring(ipi,ipf+1),nc);

        /* the flagCV contains the individual flags */

        Vector<String> iflagV=new Vector<String>();
        ssjunk=sjunk.substring(ipi,ipf+1).replace('(',' ').replace(')',' ').trim();
        while( ssjunk.length() > 1 ){
           if( ! ssjunk.substring(0,1).equals(",") ) {
               iflagV.addElement( ssjunk.substring(0,1) );
               if(!flags.contains(ssjunk.substring(0,1)))
                       flags+=ssjunk.substring(0,1);
           }
           ssjunk=ssjunk.substring(1).trim();
        }
        if( ssjunk.length() > 0 ) {
            iflagV.addElement(ssjunk);
           if(!flags.contains(ssjunk.substring(0,1)))
               flags+=ssjunk.substring(0,1);
        }

        flagVV.setElementAt(iflagV,nc);
       
        sjunk=sjunk.substring(0,ipi)+sjunk.substring(ipf+1);
        ipi=sjunk.indexOf("(");
        ipf=sjunk.indexOf(")");
     }

    if( sysOut ) System.out.println("Comment Type="+ type +"*");

    if( sysOut ) System.out.println("Comment N of heads="+ nHeads());
    if( sysOut ){
      for( int j=0; j<nHeads();j++){
        System.out.println("  Comment head at="+j+" "+headAt(j) );
        for( int k=0; k<nFlagsAt(j); k++){  
          System.out.println("   Comment Flag at="+j+","+k+" "+flagAt(j,k) );
        }
      }
    }
    if( sysOut ) System.out.println("Comment body="+ body );
    if( sysOut ) System.out.println("-----------------------------------" );
   
    
    //if(body.contains("DCO(")) System.out.println(" 1 body="+body+"  rawbody="+rawBody);
    
    //System.out.println("translated="+translated);
    
  }
  
  public boolean isEmpty(){
      if(body.trim().length()>0)
          return false;
      
      return true;
  }
 
  public Vector<String> lines(){return lines;} 
  public String lineAt(int i){return lines.get(i);} 
  
  public String type(){ return type;}
  public void setType(String t){type=t;}
  
  public String head(){ return head;}//the whole head string like E(A,B),RI(A,C),M
  public String flags(){ return flags;}//a string containing all flag letters

  public String tag() {return tag;}
  public void setTag(String s) {tag=s;}
  
  public Vector<String> headV(){return headV;}
  @SuppressWarnings("rawtypes")
  public Vector<Vector> flagVV(){return flagVV;}
  public Vector<String> flagV(){return flagV;}
  
  public int nHeads(){ return headV.size();}
  
  public void setFlagAt(int j,String k){ 
    Vector<String> temp=new Vector<String>();
    temp.addElement(k);
    flagVV.setElementAt(temp,j);
  }
  public String headAt(int j){return (String) headV.elementAt(j);}
  @SuppressWarnings("rawtypes")
  public int nFlagsAt(int j){return ( (Vector) flagVV.elementAt(j) ).size();}
  @SuppressWarnings("rawtypes")
  public String flagAt(int j, int k){return (String) ( (Vector) flagVV.elementAt(j) ).elementAt(k); }

  public String body(){ return body;}
  public String cbody(){return cbody;}
  public String rawBody() {return rawBody;}
  
 
  public void setBody(String s) throws Exception{
      setBody(s,bigC);//bigC=true by default
  }
  
  public void setBody(String s, boolean bigC) throws Exception{
      body=s;
      rawBody=s;
      if(bigC){
          translated=Translator.process(body);
          cbody=Translator.procGenCom(s, 0, true);
      }
      else{
          translated=Translator.translate(body);  
          cbody=s;
      }
      
      //if(s.contains("DCO"))
      //     System.out.println("  body="+body+"  rawbody="+rawBody);
      
      //debug
      //if(s.contains("1973BE44")) System.out.println("In Comment lin 602: getTranslated 1: text="+body+" translated="+translated);
  }
  
  
  
  public boolean isBigC(){return bigC;}
  public void setBigC(boolean bc){bigC=bc;}
  
  public boolean isMixC(){return mixC;}
  
  public void setSymbol(String s){ symbol=s;}
  public String symbol(){ return symbol;}

  public String substring10(){

    substring10="";
    for( int j=0; j< nHeads() ; j++){
      substring10=substring10+headAt(j);
      for( int k=0; k<nFlagsAt(j); k++){
        if( k == 0 ) substring10=substring10+"(";
        substring10=substring10+flagAt(j,k);  
        if( k < ( nFlagsAt(j) -1 ) ) substring10=substring10+",";
        if( k == ( nFlagsAt(j) -1 ) ) substring10=substring10+")";
      }
      if( j < ( nHeads() -1 ) ) substring10=substring10+",";
    }
    if(  ! type.equals("  ")  )  substring10=substring10+"$";
    substring10=substring10.trim()+body.trim();
    return substring10;
  }
  public boolean hasHead(String x){
      for(int i=0;i<nHeads();i++){
          if (headAt(i).equals(x)) return true;
      }
      return false;
  }
  
  public boolean hasHead(Vector<String> heads){
      for(int i=0;i<heads.size();i++){
          String head=heads.elementAt(i);
          if(hasHead(head))
              return true;

      }
      return false;
  }
  
  /*check if the comment heads contain band or seq head name*/
  public boolean hasBandHead(){
      int i=-1;
      String s=head.toUpperCase();

      i=s.indexOf("BAND");
      if(i<0)
          i=s.indexOf("SEQ");
      
      if(i>=0)
          return true;

      return false;
  }
  
  public void setBold(String x){
      bold=x;
  }
  public String bold(){
      return bold;
  }
  public String getRegTranslated(){return translated;}

  //uncapitalize the first letter of translated, exception: translated="weisskopf"
  public String getHTranslated(){
    char[]dels={' ',',','.',';',':','(',')','-','=','+','<','>','/','?','$','^','[',']','{','}','\0','|','\\','1','2','3','4','5','6','7','8','9','0'};
    String first="";
    translated=translated.trim();
    //if(translated.length()>1&&translated.charAt(translated.length()-1)!='.'&&translated.charAt(translated.length()-1)!='?'&&translated.charAt(translated.length()-1)!=':')translated+=".";
    if(translated.length()>1){
        for(int i=0;i<translated.length();i++){
            for(int j=0;j<dels.length;j++){
                if(translated.charAt(i)==dels[j]){first=translated.substring(0,i);break;}
            }
            if(!first.equals(""))break;
        }
        if(!first.equals("")){
            if(!alwaysUpperCase(first.toLowerCase()))translated=translated.substring(0,1).toLowerCase()+translated.substring(1);
        }
    }
    return translated;
  }
  
  //capitalize the first letter of translated, exception: translated="ce"
  public String getTranslated(){
    char[]dels={' ',',','.',';',':','(',')','-','=','+','<','>','/','?','$','^','[',']','{','}','\0','|','\\','1','2','3','4','5','6','7','8','9','0'};
    String first="";
    
    translated=translated.trim();

    //debug
    //if(body.contains("A{-2}"))
    //  System.out.println("In Comment: getTranslated 1: text="+body+" translated="+translated);
    
    //if(translated.length()>1&&translated.charAt(translated.length()-1)!='.'&&translated.charAt(translated.length()-1)!='?'&&translated.charAt(translated.length()-1)!=':')translated+=".";
    if(translated.length()>1){
        for(int i=0;i<translated.length();i++){
            for(int j=0;j<dels.length;j++){
                if(translated.charAt(i)==dels[j]){first=translated.substring(0,i);break;}
            }
            if(!first.equals(""))break;
        }
        if(!first.equals("")){
            if(!alwaysLowerCase(first.toLowerCase()))translated=translated.substring(0,1).toUpperCase()+translated.substring(1);
        }
    }
    //debug
    //if(body.contains("A{-2}"))
    //  System.out.println("In Comment: getTranslated 2: text="+body+" translated="+translated);
    
    return translated;
  }
  private boolean alwaysUpperCase(String s){
      String[]highs={
          "weisskopf"
      };
      if(s.length()==1)return true;
      for(int i=0;i<highs.length;i++)if(highs[i].equals(s))return true;
      return false;
  }
  private boolean alwaysLowerCase(String s){
      String[]lows={
          "ce"
      };
      if(s.length()==1)return true;
      for(int i=0;i<lows.length;i++)if(lows[i].equals(s))return true;
      return false;
  }
  
  public int id() {return id;}
  public void setID(int i) {id=i;}
}