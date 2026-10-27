
//sg: This looks at continuation records and retrieves any data that is in them
package ensdfparser.ensdf;

import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

/*

   Assumes that a continuation record looks like:
     s+" "+units+" "+ds+" ("+ref+") "
*/

public class FindValue{
  private String line, name, entryAfterName;//v is everything after name before next "$" or end
  private String entryAfterOPSymbol, entryAfterOPBeforeRef;//vms is v excluding relationship symbol; vmsmr is vms excluding reference at the end
  private String s, ds, dsu,dsl,units, symbol, ref,text,rest;//rest the remaining text at the end after parsing all properties
  private double x,dx,dxu,dxl;
  private int indexOfSpace;
  private boolean bv,bx,bdx,found;
  private int ieq,iap,igt,ige,ile,ilt,ip1,ip2,iLT,iGT,iLE,iGE,iAP;
  private boolean sysOut;
  private boolean isInBracket=false;//flag if the value is put inside brackets, e.g., %B-=(13 3)
  
  private String separateSign;//for data value like -(0.10 {I+4-3}), which is different from -0.10 {I+4-3}
  
  public FindValue(){
    entryAfterName=""; s=""; ds="";dsu="";dsl=""; symbol=""; x=-1; dx=-1; dxu=-1;dxl=-1; name=""; units=""; ref="";text="";
    entryAfterOPSymbol="";entryAfterOPBeforeRef="";rest="";separateSign="";
    bv=false; bx=false; bdx=false;
  }

  @SuppressWarnings("unused")
  //lineStr: an ENSDF line of continuation records including NUCID as prefix
public void findValueInLine(String lineStr, String nameStr){ 

    sysOut=true;

    sysOut=false;

    entryAfterName=""; s=""; ds=""; dsu=""; dsl=""; symbol=""; x=-1; dx=-1; dxu=-1; dxl=-1; name=""; units=""; ref=""; text="";
    entryAfterOPSymbol="";entryAfterOPBeforeRef="";

    
    bv=false; bx=false; bdx=false;

    line=lineStr.trim();
    name=nameStr.trim();
    
    //System.out.println("###name="+name+" line="+lineStr);
    
    if( line.length() > 0 && name.length() > 0){

      /* cases like line=" 44TI  %EC=80 " and name="%EC" */

      String s1=" "+name;
      int i1=line.indexOf(s1);

      /* or cases like line=" 44TI  %A=20$%EC=80 " and name="%EC" */

      String s2="$"+name;
      int i2=line.indexOf(s2);

      int i3=-1;

      if(line.indexOf(name)==0)
    	  i3=0;
      else if( i1 >= 0 && i2 < 0 ) 
    	  i3=i1+1;
      else if( i1 < 0 && i2 >= 0 ) 
    	  i3=i2+1;      
      else if(i1>=0 && i2>=0) {
    	  if(i1<i2)
    		  i3=i1+1;
    	  else
    		  i3=i2+1;
      }else if(i1<0 && i2<0) {
    	  i3=line.indexOf(name);
    	  if(i3>0&&Character.isLetter(line.charAt(i3-1))) 
    		  i3=-1;
    	  
      }
      
      found=false;

      
      if( name.endsWith("=") ) found=true;

      else{
         if( line.length() >= (i3+name.length()+2) ){
           String nextChar=line.substring(i3+name.length(),i3+name.length()+1);
           String next2Char=line.substring(i3+name.length(),i3+name.length()+2);
           //debug
           //System.out.println("In FindValue.java line 68: line="+line+" name="+name+" i1="+i1+" i2="+i2+" i3="+i3+" found="+found+" nextChar="+nextChar);
           
           if( nextChar.equals(" ") || nextChar.equals("=") || nextChar.equals("<") || nextChar.equals(">")
        		   || nextChar.equals("|") || nextChar.equals("?") || nextChar.equals("~") 
           ) {
        	   found=true;
           }
         }
      }

      //debug
      //System.out.println("In FindValue.java line 75: line="+line+" name="+name+" i1="+i1+" i2="+i2+" i3="+i3+" found="+found+" line.len="+line.length()+" name.len="+name.length());
      
      if(sysOut) System.out.println("i3="+i3+", found="+found);

      if( i3 >= 0 ){

        if( line.indexOf(name) >= 0 && found){
        	
          /* looking for v: value */

          int beg=i3+name.length(); 
          entryAfterName=line.substring(beg);

          while(entryAfterName.contains("  ")) {
        	  entryAfterName=entryAfterName.replace("  "," ");
          }
          
          int nb=0;
          String tempS="";
          for(int i=0;i<entryAfterName.length();i++) {
        	  char c=entryAfterName.charAt(i);
        	  if(c=='('||c=='[')
        		  nb++;
        	  else if(c==')'||c==']')
        		  nb--;
        	  else if(c==','&&nb==0)
        		  c=';';
        	  
        	  tempS+=c;
          }
          
 
          //System.out.println("temps="+tempS+" v="+entryAfterName);
          
          int end=entryAfterName.length();
          //String[] temp=entry.split("[$;]+");
          String[] temp;
          
          if(entryAfterName.contains("$")) {
        	  temp=entryAfterName.split("[$]+");
          }else {
        	  temp=EnsdfUtil.splitEntryLine(entryAfterName);
        	 
        	  //if(temp.length>0) 
        	  //System.out.println("FindValue 150: entryAfterName="+entryAfterName+" temp[0]= "+temp[0]+"  temp size="+temp.length);
        	  
        	  int n=entryAfterName.indexOf(",");
        	  if(n>0 && temp.length>1 && entryAfterName.startsWith("=")) {
        		  String part1=entryAfterName.substring(0,n).trim();
        		  
        		  //System.out.println("FindValue 150: entryAfterName="+entryAfterName+" part1="+part1+" temp[0]= "+temp[0]+"  temp size="+temp.length);
        		  //System.out.println(part1.equals(temp[0]));
        		  
        		  if(temp[0].trim().equals(part1)) {//first part split at ",", check if part after "," is till part of the entry, like CIT=NDS 74, 63 (1995)
        			  part1=part1.substring(1).trim();
        			  String part2=temp[1].trim();
        			  
        			  //System.out.println("FindValue 156: entryAfterName="+entryAfterName+" part1="+part1+" temp[0]= "+temp[0]+"  temp size="+temp.length);
        			  
        			  boolean isGoodSplit=true;
        			  n=part1.indexOf(" ");
        			  if(n<0) {
        				  if(!Str.isNumeric(part1) && Str.isLettersOrDigits(part1))
        					  isGoodSplit=false;
        			  }else {
        				  part1=part1.substring(0,n).trim();
        				  if(!Str.isNumeric(part1) && Str.isLettersOrDigits(part1))
        					  isGoodSplit=false;
        			  }
        			  
        			  if(!isGoodSplit) {
        				  temp=entryAfterName.split("[$;]+");
        			  }
        		  }
        	  }
          }
          
          if(temp.length>0) {	  
        	  tempS=temp[0].trim();
        	  if(tempS.length()>0) {
        		  end=entryAfterName.indexOf(tempS)+tempS.length();
        		  if(end>entryAfterName.length())
        			  end=entryAfterName.length();
        	  }
          }
          
          //System.out.println("In FindValue.java line 138: v="+entryAfterName+"  tempS="+tempS+"  end="+end);
          
          entryAfterName=entryAfterName.substring(0,end).trim();
          if( entryAfterName.length() > 0) 
        	  bv=true;

          //System.out.println("name="+name+" v="+entryAfterName);
          
          if(!name.startsWith("CIT"))
        	  entryAfterName=entryAfterName.replace(", ", ",");
          
          if(!name.endsWith("=") && entryAfterName.length()>0 && "=<>|?~".indexOf(entryAfterName.charAt(0))<0)
        	  text=name+" "+entryAfterName;
          else
        	  text=name+entryAfterName;
          
          text=text.trim();
          
          //debug
          //if(lineStr.toUpperCase().contains("2016ST"))
          //System.out.println("In FindValue.java line 103: line="+line+" name="+name+"\n     entry="+entryAfterName+" text="+text+" begin="+beg+" end="+end+" bv="+bv);
          
          if(sysOut) System.out.println("v="+entryAfterName); 
             
          ieq=entryAfterName.indexOf("=");
          
          int nap=2;
          iap=entryAfterName.indexOf("|?");
          if(iap<0 && ieq<0) {
        	  iap=entryAfterName.indexOf("?");
        	  nap=-1;
        	  if(iap>=0 && iap<entryAfterName.length()-1){
        		  String s= entryAfterName.substring(iap+1).trim();
        		  if(Character.isDigit(s.charAt(0))){
                	  nap=1;
                	  text=text.replace("?", "|?");   
        		  }
        	  }
              if(nap<0) iap=-1;
          }

          
          if(iap<0) {
        	  iap=entryAfterName.indexOf("~");
        	  nap=-1;
        	  if(iap>=0 && iap<entryAfterName.length()-1){
        		  String s= entryAfterName.substring(iap+1).trim();
        		  if(Character.isDigit(s.charAt(0))){
                	  nap=1;
                	  text=text.replace("~", "|?");	   
        		  }       		  
        	  }else {
        		  iap=entryAfterName.indexOf("|J");
        		  if(iap>=0) {
        			  try {
                  		  String s= entryAfterName.substring(iap+2).trim();
                		  if(Character.isDigit(s.charAt(0))){
                        	  nap=2;
                        	  text=text.replace("|J", "|?");	   
                		  }   
        			  }catch(Exception e) {}
    
        		  }
        	  }
        	  if(nap<0) iap=-1;
          }
        
          
          igt=entryAfterName.indexOf(">");
          ilt=entryAfterName.indexOf("<");
          
          ige=entryAfterName.indexOf("|>");
          if(ige<0){ige=entryAfterName.indexOf(">=");text=text.replace(">=", "|>");}
          
          ile=entryAfterName.indexOf("|<");
          if(ile<0){ile=entryAfterName.indexOf("<=");text=text.replace("<=", "|<");}
          
          iAP=entryAfterName.indexOf("AP");
          iGT=entryAfterName.indexOf("GT");
          iGE=entryAfterName.indexOf("GE");
          iLE=entryAfterName.indexOf("LE");
          iLT=entryAfterName.indexOf("LT");



          //if(lineStr.toUpperCase().contains("2016ST")) 
          //System.out.println("In FindValue.java line 182: line="+line+"\n    name="+name+" entryAfterName="+entryAfterName+" text="+text+" symbol="+symbol+"  iap="+iap+" nap="+nap+" "+ilt);
          
          if( ieq >= 0 ){ entryAfterOPSymbol=entryAfterName.substring(ieq+1).trim(); symbol="="; }
          else if( iAP >= 0 ){ entryAfterOPSymbol=entryAfterName.substring(iAP+2).trim(); symbol="AP"; }
          else if( iGE >= 0 && (iLE<0||iGE<iLE) && (iLT<0||iGE<iLT) ){ entryAfterOPSymbol=entryAfterName.substring(iGE+2).trim(); symbol="GE"; }//e.g., BM1W GT 12 LT 24
          else if( iGT >= 0 && (iLE<0||iGT<iLE) && (iLT<0||iGT<iLT) ){ entryAfterOPSymbol=entryAfterName.substring(iGT+2).trim(); symbol="GT"; }
          else if( iLE >= 0 && (iGE<0||iLE<iGE) && (iGT<0||iLE<iGT) ){ entryAfterOPSymbol=entryAfterName.substring(iLE+2).trim(); symbol="LE"; }
          else if( iLT >= 0 && (iGE<0||iLT<iGE) && (iGT<0||iLT<iGT) ){ entryAfterOPSymbol=entryAfterName.substring(iLT+2).trim(); symbol="LT"; }
          else if( iap >= 0 ){ entryAfterOPSymbol=entryAfterName.substring(iap+nap).trim(); symbol="|?"; }          
          else if( ige >= 0 ){ entryAfterOPSymbol=entryAfterName.substring(ige+2).trim(); symbol="|>"; }
          else if( ile >= 0 ){ entryAfterOPSymbol=entryAfterName.substring(ile+2).trim(); symbol="|<"; }
          else if( igt >= 0 ){ entryAfterOPSymbol=entryAfterName.substring(igt+1).trim(); symbol=">"; }
          else if( ilt >= 0 ){ entryAfterOPSymbol=entryAfterName.substring(ilt+1).trim(); symbol="<"; }
          else{ 
        	  entryAfterOPSymbol=entryAfterName;
          }

          //if(lineStr.toUpperCase().contains("2016ST")) 
          //System.out.println("In FindValue.java line 244: line="+line+"\n   name="+name+" entryAfterOP="+entryAfterOPSymbol+" text="+text+" symbol="+symbol+"  iap="+iap+" nap="+nap);
          
          if(entryAfterOPSymbol.startsWith("(")) {
        	  isInBracket=true;
        	  int n=entryAfterOPSymbol.indexOf(")");
        	  int n1=entryAfterOPSymbol.indexOf("(",1);
        	  
        	  if(n==entryAfterOPSymbol.length()-1)
        		  entryAfterOPSymbol=entryAfterOPSymbol.substring(1,entryAfterOPSymbol.length()-1);
        	  else if(n>0 && n<entryAfterOPSymbol.length()-1) {
        		  if(n1>n || n1<0)
        			  entryAfterOPSymbol=entryAfterOPSymbol.substring(1,n)+entryAfterOPSymbol.substring(n+1); 
        	  }else if(n<0) {//unmatched "(", should be checked using FormatCheck
        		  entryAfterOPSymbol=entryAfterOPSymbol.substring(1);
        	  }
          }

          //System.out.println("In FindValue.java line 309: line="+line+" name="+name+" entryAfterOPSymbol="+entryAfterOPSymbol+" text="+text+" symbol="+symbol+"  iap="+iap+" nap="+nap);
          
          if(entryAfterOPSymbol.endsWith("."))
              entryAfterOPSymbol=entryAfterOPSymbol.substring(0,entryAfterOPSymbol.length()-1).trim();
          
          int n=-1;
          boolean isRatio=false;
    	  if(entryAfterOPSymbol.contains("/")) {
    		  String[] temp0=entryAfterOPSymbol.split("[/]");
    		  if(temp0.length==2 && Str.isNumeric(temp0[0]) && Str.isNumeric(temp0[1])) {
    			  if(temp0[1].equals("2") && Str.isInteger(temp0[0]) && Integer.parseInt(temp0[0])%2==1) {
    				  //spin
    			  }else
    				  isRatio=true;
    		  }
    	  }else if(entryAfterOPSymbol.contains(":") && symbol.equals("=")){
    		  String[] temp0=entryAfterOPSymbol.split("[:]");
    		  isRatio=true;
    		  for(int i=0;i<temp0.length;i++) {
    			  if(!Str.isNumeric(temp0[i])) {
    				  isRatio=false;
    				  break;
    			  }
    		  }
    	  }
          
          //System.out.println("In FindValue.java line 335: line="+line+" name="+name+" entryAfterOPSymbol="+entryAfterOPSymbol+" text="+text+" symbol="+symbol+"  iap="+iap+" nap="+nap);

          
          if(!isRatio) {
              nb=0;
              tempS="";
              for(int i=0;i<entryAfterOPSymbol.length();i++) {
                  char c=entryAfterOPSymbol.charAt(i);
                  if(c=='('||c=='[')
                      nb++;
                  else if(c==')'||c==']')
                      nb--;
                  else if(c==' '&&nb==0)
                      c=';';
                  
                  tempS+=c;
              }
              
              //temp=vms.split("[\\s]+");
              temp=Str.specialSplitAllDelims(entryAfterOPSymbol, "[,\\s]+");//not split at space inside brackcets () or [] or {}
              if(temp.length>1 && Str.isNumeric(temp[0])) {
            	  n=-1;
            	  int ioffset=0;
            	  
            	  //if(lineStr.toUpperCase().contains("2016ST")) 
            	  //System.out.println("FindValue 360: entryAfterOPSymbol="+entryAfterOPSymbol+" temp[0]="+temp[0]+" temp[1]="+temp[1]+"  vms.indexOf(temp[1])="+entryAfterOPSymbol.indexOf(" "+temp[1]));
            	  
            	  tempS=temp[1].replace("+", "").replace("-", "").replace("{I","").replace("}","").trim();
            	  
            	  if(Str.isNumeric(temp[1]) || Str.isNumeric(tempS)) {

            		  if(temp.length>2 && temp[2].startsWith("(") && temp[2].endsWith(")")) { 
            			  ioffset=(temp[0]+" "+temp[1]).length();
            			  n=entryAfterOPSymbol.indexOf(" "+temp[2],ioffset)+temp[2].length()+1;
            		  }else {
            			  ioffset=temp[0].length();
            			  n=entryAfterOPSymbol.indexOf(" "+temp[1],ioffset)+temp[1].length()+1;
            		  }
            		  
            	  }else if(!"IS,DEDUCED,FOR,FROM,BY,OF,IN,AT,TO,WHICH,WEIGHTED,AVERAGE,UNWEIGHTED".contains(temp[1].toUpperCase())) {
            		  //temp[1] is units or (ref);
            		  if(temp.length>2) {
            			  tempS=temp[2];//temp[2] is unc or (ref)
            			  ioffset=(temp[0]+" "+temp[1]).length();
            			  
            			  String tempS1=tempS.replace("+", "").replace("-", "").replace("{I","").replace("}","").trim();
            			  if((Str.isNumeric(tempS) || Str.isNumeric(tempS1) ||EnsdfUtil.ENSDF_relation_OPs.contains(tempS) )&& temp.length>3) {
            				  if((temp[3].startsWith("(") && temp[3].endsWith(")")) || temp[3].length()>=8) {
                				  tempS=temp[3];
                				  ioffset=(temp[0]+" "+temp[1]+" "+temp[2]).length();
            				  }
            			  }
            			  //System.out.println("FindValue 317:  tempS="+tempS+" temp[0]="+temp[0]+" temp[1]="+temp[1]+"  entryAfterOPSymbol.indexOf(temp[1])="+entryAfterOPSymbol.indexOf(" "+temp[1]));
            			  
            			  n=entryAfterOPSymbol.indexOf(" "+tempS,ioffset)+tempS.length()+1;     			  
            		  }
            		  
            	  }else {
            		  n=entryAfterOPSymbol.indexOf(" "+temp[1],temp[0].length());
            	  }
            	  
            	  if(n>0) {
                      rest=entryAfterOPSymbol.substring(n).trim();
            		  entryAfterOPSymbol=entryAfterOPSymbol.substring(0,n).trim();
            	  }
            	  
                  //debug
                  //if(n<0) 
            	  
            	  //System.out.println("In FindValue.java line 401: entryAfterOPSymbol="+entryAfterOPSymbol+" name="+name+" n="+n+" text="+text
            	  //		  +" symbol="+symbol+" rest="+rest+" offset="+ioffset+" temp[0]="+temp[0]+" temp[1]="+temp[1]);
              }        	  
          }


          
          /* looking for the reference: */

          ip1=entryAfterOPSymbol.lastIndexOf("(");
          ip2=entryAfterOPSymbol.lastIndexOf(")");

          //if(lineStr.toUpperCase().contains("2016ST")) 
          //System.out.println("In FindValue.java line 364: entryAfterOP="+entryAfterOPSymbol+" "
          //		+ "\n name="+name+" entryAfterName="+entryAfterName+" text="+text+" symbol="+symbol+" entryAfterOPBeforeRef="+entryAfterOPBeforeRef);
          
          /* vmsmr: value minus symbol minus reference */

          if( ip1 > 0 && ip2 > (ip1+8) ){
            ref=entryAfterOPSymbol.substring(ip1+1,ip2).trim(); 
            entryAfterOPBeforeRef=entryAfterOPSymbol.substring(0,ip1).trim();
          }
          else{ entryAfterOPBeforeRef=entryAfterOPSymbol.trim();}

          //System.out.println("In FindValue.java line 428: entryAfterOP="+entryAfterOPSymbol+" "
          //		+ "\n name="+name+" entryAfterName="+entryAfterName+" text="+text+" symbol="+symbol+" entryAfterOPBeforeRef="+entryAfterOPBeforeRef);
          
          
          String entryAfterOPBeforeRef1="",tempRefs="";
          String refS1=entryAfterOPBeforeRef;
          int lb=-1;
          while((lb=refS1.indexOf("{I"))>0) {
        	  int rb=refS1.indexOf("}",lb);
        	  if(rb<0) {
        		  tempRefs+=refS1.substring(0,lb)+refS1.substring(lb+2).replace("{I","");
        		  break;
        	  }else {
            	  String refS2=refS1.substring(lb+2,rb).replace("+","").replace("-","").trim();
            	  if(Str.isInteger(refS2)) {
            		  tempRefs+=refS1.substring(0,lb)+refS1.substring(lb+2,rb);
            	  }else {
            		  tempRefs+=refS1.substring(0,rb+1);
            	  }
        		  refS1=refS1.substring(rb+1);
        	  }

          }
          //this will replace good } like in 4.6|*10{+-3} 
          //String entryAfterOPBeforeRef1=entryAfterOPBeforeRef.replace("{I", "").replace("}", "").trim();
          if(tempRefs.length()>0)
        	  entryAfterOPBeforeRef1=tempRefs;
          else
        	  entryAfterOPBeforeRef1=entryAfterOPBeforeRef;
          
          /* The values and units: */
          units="";
          indexOfSpace=entryAfterOPBeforeRef1.indexOf(" ");
            
          //System.out.println("In FindValue.java line 462: entryAfterOP="+entryAfterOPSymbol+" "
          //  		+ "\n name="+name+" entryAfterName="+entryAfterName+" text="+text+" symbol="+symbol+" entryAfterOPBeforeRef="+entryAfterOPBeforeRef1);
          String tempS1=entryAfterOPBeforeRef1;
          if(tempS1.length()>3) {
        	  //for entry like, entry="(666,2+ TO 1960,2+)=-(0.10 {I+4-3}) (1995KA29)";
        	  //for which entryAfterOPBeforeRef="-(0.10 {I+4-3})"
        	  char c0=tempS1.charAt(0);
        	  char c1=tempS1.charAt(1);
        	  char c2=tempS1.charAt(tempS1.length()-1);  
        	  int n1="([".indexOf(c1);
        	  int n2=")]".indexOf(c2);
        	  if("+-".contains(c0+"")&&n1>=0&&n1==n2) {
        		  separateSign=c0+"";
        		  tempS1=tempS1.substring(2,tempS1.length()-1).trim();
        	  }
          }

          if( indexOfSpace > 0 && !isRatio){ 
        	 if(separateSign.length()>0) {
        		 int indexOfSpace1=tempS1.indexOf(" ");
                 s=tempS1.substring(0,indexOfSpace1).trim();
                 ds=tempS1.substring(indexOfSpace1).trim().toUpperCase();
        	 }else {
                 s=entryAfterOPBeforeRef1.substring(0,indexOfSpace).trim();
                 ds=entryAfterOPBeforeRef1.substring(indexOfSpace).trim().toUpperCase();
        	 }

                     
        	 //System.out.println("  s="+s+" unit="+units+" ds="+ds);
        	 
             indexOfSpace=ds.indexOf(" ");
             if(indexOfSpace>0){
            	 units=ds.substring(0, indexOfSpace).trim();
            	 ds=ds.substring(indexOfSpace).trim();
            	 
            	 //System.out.println("vms="+vms+"  s="+s+" unit="+units+" ds="+ds);
            	 
           		 n=ds.indexOf(" ");
        		 String tempDS=ds;
        		 if(n>0) {
        			 tempDS=ds.substring(0,n).trim();
        			 if(Str.isNumeric(tempDS) || EnsdfUtil.ENSDF_relation_OPs.contains(tempDS) || tempDS.contains("+") || tempDS.contains("-")) {      				 
        				 tempDS=ds.substring(n+1).trim();
        				 ds=ds.substring(0,n).trim();
        			 }      			 
        		 }
        		 
            	 if(tempDS.length()>2 && !Str.isNumeric(tempDS) && !tempDS.contains("+")&&!tempDS.contains("-") ) {
            		 if(ref.isEmpty())
            			 ref=tempDS;
            		 else {
            			 //format error, should be caught by FormatCheck code
            		 }
            	 }

             }else if(!Str.isNumeric(ds)){
            	 boolean isNumeric=false;
            	 if(ds.contains("+") && ds.contains("-")) {
            		 tempS=ds.replace("+", "").replace("-", "").trim();
            		 if(Str.isNumeric(tempS))
            			 isNumeric=true;
            	 }
            	 
            	 if(!isNumeric) {
            		 if(ds.length()==2 && "LT,LE,GT,GE,AP,SY,CA".contains(ds)) {
            			 
            		 }else if(Character.isDigit(ds.charAt(0))){
            			 int k=1;
            			 while(k<ds.length()) {
            				 char c=ds.charAt(k);
            				 if(!Character.isDigit(c))
            					 break;
            				 k++;
            			 }
            			 String ds1=ds.substring(0,k);
            			 String ds2=ds.substring(k);
            			 if(Str.isNumeric(ds1)&&ds2.equals(")")){
            				 units="";
            				 ds=ds1;
            			 }
            		 }else {
                    	 units=ds;
                    	 ds="";
                         if(symbol.trim().length()>0 && !symbol.equals("="))
                        	 ds=symbol; 
            		 }

            	 }

             }          
          }
          else{
             s=entryAfterOPBeforeRef1;
             ds="";
             if(symbol.trim().length()>0 && !symbol.equals("="))
            	 ds=symbol;

          }
          //System.out.println("In FindValue.java line 555: s="+s);
          
          if(ds.endsWith("."))
        	  ds=ds.substring(0,ds.length()-1).trim();
          
          /* converting Strings to numbers */

          if( symbol.equals("=") ){
              SDS2XDX c=new SDS2XDX();
              c.setValues(s,ds);
              x=c.X();
              dx=c.DX();
              
              dxu=c.dxu();
              dxl=c.dxl();
              dsu=c.dsu();
              dsl=c.dsl();
              
              bx=c.canGetX();
              bdx=c.canGetDX();
          }

          else{

              String fakeds="";

              if( symbol.equals(">") ) fakeds="GT";
              else if( symbol.equals("<") ) fakeds="LT";
              else if( symbol.equals("|>") ) fakeds="GE";
              else if( symbol.equals("|<") ) fakeds="LE";
              else if( symbol.equals("|?") ) fakeds="AP";
              else if( symbol.equals("AP") || symbol.equals("GT") ||
                       symbol.equals("GE") || symbol.equals("LT") ||
                       symbol.equals("LE") ) fakeds=symbol;

              SDS2XDX c=new SDS2XDX();
              c.setValues(s,fakeds);
              x=c.X();
              dx=c.DX();
              
              dxu=c.dxu();
              dxl=c.dxl();
              dsu=c.dsu();
              dsl=c.dsl();
              
              bx=c.canGetX();
              bdx=c.canGetDX();

          }
          
          if(dx==0 && dxu!=dxl) {
        	  if(dxu>dxl && dxu>0)
        		  dx=dxu;
        	  else if(dxl>dxu && dxl>0)
        		  dx=dxl;
          }

          //System.out.println("In FindValue.java line 514: entryAfterOP="+entryAfterOPSymbol+" "
          //  		+ "\n name="+name+" entryAfterName="+entryAfterName+" text="+text+" symbol="+symbol+" entryAfterOPBeforeRef="+entryAfterOPBeforeRef+" bv="+bv);
        }//end has name & found
      
      }//end i3>=0
    
    }//end line.length>0

  }

  //find all values in a single continuation record entry
  public void findValueInEntry(String entryStr, String nameStr){ 
	  String str=entryStr.trim();
	  if(str.length()<=0 || nameStr.trim().length()<=0)
		  return;
	  
	  str=" 44TI    $"+entryStr.trim();//make a fake ENSDF line in order to use findValueInLine()

	  findValueInLine(str,nameStr.trim());	  	  
  }
  
  //find all values in a single continuation record entry
  public void findValueInEntry(String entryStr){ 
	  String name="";
  	  bv=false;
  	  bx=false;
  	  bdx=false;
  	  
  	  if(entryStr.length()<=0)
  		  return ;
  	
	  name=EnsdfUtil.findContRecordName(entryStr);	 
	 
	  //System.out.println(entryStr+"#"+name);
	  
	  if(name.trim().isEmpty())
		  return;
	  
	  findValueInEntry(entryStr,name);	
	  
  }
  public String rest() {return rest;}
  public String v(){ return entryAfterName; }
  public boolean canGetV(){ return bv;}
  public String vNoSymbol(){ return entryAfterOPSymbol; }
  public String vNoSymbolNoRef(){ return entryAfterOPBeforeRef; }
  public String s(){ return s; }
  public String ds(){ return ds; }
  public String dsu(){ return dsu; }
  public String dsl(){ return dsl; }
  public String txt(){return text;}
  public double x(){ return x; }
  public double dx(){ return dx; }
  public double dxu(){ return dxu; }
  public double dxl(){ return dxl; }
  public float xf(){ return (float) x; }
  public float dxf(){ return (float) dx; }
  public float dxuf(){ return (float) dxu; }
  public float dxlf(){ return (float) dxl; }
  public boolean canGetX(){ return bx;}
  public boolean canGetDX(){ return bdx;}
  public String ref(){ return ref; }
  public String symbol(){ return symbol; }
  public String units(){ return units; }
  public String name() {return name;}
  
  public boolean isInBracket() {return isInBracket;}
  public String separateSign() {return separateSign;}
  
  public void clear(){
	    entryAfterName=""; s=""; ds=""; dsu=""; dsl=""; symbol=""; x=-1; dx=-1; dxu=-1; dxl=-1; name=""; units=""; ref="";text="";
	    entryAfterOPSymbol="";entryAfterOPBeforeRef="";separateSign="";
	    bv=false; bx=false; bdx=false;
	    isInBracket=false;
  }

}