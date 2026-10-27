/*
 * Deals with strings that start with numbers
 */

package ensdfparser.nds.util;

import ensdfparser.ensdf.Nucleus;
import ensdfparser.nds.latex.Translator;

/**
 * Translates all strings that start with numbers
 *
 */
public class numanl {
    private String out;
    private int length=-1;//the number of characters translated
    private int nextraspaces=0;//number of extra spaces between value and uncertainty 
    //we need to provide length so the calling program will know how many characters to skip
    private boolean isNilsson=false;
    private char delim;
    private String flag="";
    
    public numanl(String line,int start){
        char temp;
       
        line=line.trim();
        int firstNonInt=line.length();
        //Translator.init();
        
        boolean hasNonDigit=false;
        boolean hasExpo=false;
        String expoStr="";
        Word expoWord=new Word(" ",0);
        int expoPos=0;
       
        String intStr;
        out=line;
         Word intmst=new Word(" ",0);
        //find first non-number character
        for(int i=start;i<line.length();i++){
            temp=line.charAt(i);
            if(temp>='0'&&temp<='9')
            	continue;
            if(temp=='.'&&i<line.length()-1&&i>0){
            	if(Character.isDigit(line.charAt(i+1)) && Character.isDigit(line.charAt(i-1)))
            		continue;
            }
            
            firstNonInt=i;
            if(temp!='.') //not the ending "."
            	hasNonDigit=true;
            
            if(i<line.length()-1 && (temp=='E'||temp=='e')){
            	

            	char tempCh=line.charAt(i+1);
            	int pos=i+1;
            	if(tempCh=='+'||tempCh=='-')
            		pos=i+2;

            	
            	expoWord=new Word(line,pos);
            	
            	//debug
            	//System.out.println("In numal line 47: line="+line+" >>> expoWord="+expoWord.getWord());
            	
            	if(Str.isNumeric(expoWord.getWord())){
            		hasExpo=true;
            		expoStr=expoWord.getWord();
            		expoPos=i;
            		if(tempCh=='+'||tempCh=='-')
            			expoStr=tempCh+expoStr;
            	}
            }
            
            delim=temp;
            break;          
        }
         
        //debug
        //if(line.contains("BAND"))
        //	System.out.println("In numman.java line 75: line="+line+" line.len="+line.length()+" firstNonInt="+firstNonInt+" delim="+delim+"$");
        
        
        //see if its a nilsson configuratoin
        char c;
        isNilsson=true;
        if(delim=='/'){
        	
        	//System.out.println("In numman.java line 90: line="+line+" line.len="+line.length()+" firstNonInt="+firstNonInt);
        	
            if(line.length()<firstNonInt+7) isNilsson=false;//11/2[505] len=9 firstNonInt=2, 1/2[501] len=8 firstNonInt=1; 7/2[523|^],len=10
            if(isNilsson){
                if(firstNonInt>2) isNilsson=false;
                else if(line.charAt(firstNonInt+1)!='2') isNilsson=false;
                else if(line.charAt(firstNonInt+2)!='[') isNilsson=false;
                else{
                    for(int i=3;i<=5;i++){
                        c=line.charAt(firstNonInt+i);
                        if(c<'0'||c>'9'){ isNilsson=false; break;}
                     }
                }

            }
            
            if(isNilsson){
            	
            	int n=line.substring(firstNonInt).indexOf(']');
            	if(n==6)//1/2[501],11/[505]
            		n=7;
//            	else //7/2[523|^]
            		n=6;
            	
                if(firstNonInt==1)
                	length=n+1;//1/2[501]
                else
                	length=n+2;//11/2[505]
                
                delim=' ';
                out=line.substring(start,firstNonInt+n);
                return;

            }
        }
        isNilsson=false;
 
        //intStr is the number, intmst is the next word
        intStr=line.substring(start,firstNonInt);
        
        
        if(hasNonDigit){
            intmst=new Word(line,firstNonInt);
        }
        
        String text=intStr+intmst.getWord();
        
        String s="";
        
        //debug
        //if(line.contains("31K")) {
        //	System.out.println("In numanl 137: intStr="+intStr+" intmst.getDelim="+intmst.getDelim()+"*** line="+line+" #"+firstNonInt);
        //	System.out.println("           intmst.getword="+intmst.getWord()+"*** delim="+delim+" text="+text);
        //}
        
        
        //check if it is single-particle orbital configuration like 1G9/2, intmst.getWord()=G9, intmst.delim='/'
        if(intmst.getWord().length()>1 && intmst.getDelim()=='/'){
        	try{
        		int n=line.indexOf(text,start)+text.length()+1;
        		if(line.charAt(n)=='2' && Character.isDigit(line.charAt(n-2))){
        			if(Character.isLetter(line.charAt(n-3)) || (Character.isDigit(line.charAt(n-3))&&Character.isLetter(line.charAt(n-4))))
        			s=Translator.getTransWord(line.substring(start+intStr.length(),n+1));
        			if(!s.equals(" ")){
            			out=intStr+s;
            			length=n-start+1;
            			return;
        			}
        		}
        	}catch(Exception e){
        		s="";
        	}    	
        }
        
        if(intmst.getWord().length()<1){
            boolean list=false;
            if(intmst.getDelim()==',' && Str.isInteger(intStr)){
            	list=true;
            }
            int pos=firstNonInt;
            String num,outTemp="{+"+intStr+",}";
            while(list){//check if it could be a list of nuclides, like 113,115Ag
                pos+=intmst.length();
                if(intmst.length()<1 && (intmst.getDelim()==','||intmst.getDelim()==' ')){
                    pos++;
                    if(pos>=line.length()) {list=false; break;}
                    intmst=new Word(line,pos);                
                    continue;
                }
                if(intmst.getDelim()!=',') list=false;
                if(list) outTemp+="{+"+intmst.getWord()+",}";
                num=intmst.getWord();
                for(int i=0;i<num.length();i++){
                    if(num.charAt(i)<'0' || num.charAt(i)>'9')list=false;                        
                }
                if(list==false){      	
                    num=num.toUpperCase();
                    s=Translator.getTransNuc(num);
                    if(!s.equals(" ")) {
                        list=true;
                        outTemp+=s;
                        pos+=num.length();
                    }
                    break;
                }
                if(pos>=line.length()) break;
                intmst=new Word(line,pos);
            }
            
            if(list){
                out=outTemp;
                length+=pos;
                return;
            }
            //check if it could be a 5-integer nuclide, like {+267}113->26713
            if(intStr.length()==5){
                int name=0,number=0;
                
                s=Translator.getTransNuc(intStr);
                if(!s.equals(" ")){
                    out=s;
                    length=intStr.length()+intmst.length();
                    return;
                }
                
                try{
                    number=Integer.parseInt(intStr.substring(0,3));
                    name=Integer.parseInt(intStr.substring(3));
                    if(name>=12&&name<=19&&number>=name+265&&number<=name+280){
                        out="{+"+number+"}1"+name;
                        length=5;
                        return;
                    }
                }catch(Exception e){
                }
            }

            //System.out.println("out="+out+"$ line="+line+" start="+start+" firstNonInt="+firstNonInt+" delim="+delim+"# inStr="+intStr+"  "+(delim==' '));

            //if a regular number, look for units/uncertainty
            length=intStr.length();
            if(hasNonDigit&&delim==' ') 
            	out=numanl80(line,firstNonInt,start);
            else 
            	out=intStr;
            
            //System.out.println("out="+out+"$ line="+line+" start="+start+" firstNonInt="+firstNonInt+" delim="+delim+"# inStr="+intStr+"  "+(delim==' '));
            return;
        }
        if(intmst.firstChar()=='%'){
            //if percent sign and followed by word with not delimiter, done

            //if(intmst.length()<1) numanl150(line);
            //if end of word, look for units
            length=intStr.length()+1;
            out=numanl80(line,firstNonInt+1,start);
            return;
        }
        //if doesnt start with a letter, done
        else if((intmst.firstChar()<'A'||intmst.firstChar()>'Z')&&(intmst.firstChar()<'a'||intmst.firstChar()>'z')){
            out=intStr;
            length=intStr.length();
            return;
        }

        //see if its a nuclide code, translate if so
        if(text.length()>=2&&text.length()<=5){
        	//set nucid to uppercase for translation, note that text=intStr+intmst.getWord()
            if(intmst.getWord().length()>1)
            	s=text.substring(0,text.length()-1)+text.substring(text.length()-1,text.length()).toUpperCase();
            else
            	s=text;
            
            s=Translator.getTransNuc(s);
            
            //debug
            //if(text.contains("31K")) System.out.println("In numal line 255: text="+text+" translated="+s);
            
            //check if 'G' is for |g or part of nuclide name, like SG
            if(s.equals(" ") && text.charAt(text.length()-1)=='G'){
            	s=text.substring(0,text.length()-1);
            	s=Translator.getTransNuc(s);
            	            	
            	if(!s.equals(" ")){
            		s=s+"|g";
            	}
            }
            
            if(!s.equals(" ")){
                out=s;
                length=intStr.length()+intmst.length();
                return;
            }else if(Str.isInteger(intStr)){//check further if it is a nuclide code even if it is not in the dictionary. Some new nuclide names could not be included in the dictionary.
            	int mass=Integer.parseInt(intStr);
            	String gam="";
            	String element=intmst.getWord();
            	s=Translator.getTransNuc(element);
            	
            	if(element.equals("N")){//for nitrogen or neutron
            		s="N";
            	}
            	else if(element.length()>1 && element.charAt(element.length()-1)=='G' && s.equals(" ")){//e.g., ALG
            		element=element.substring(0, element.length()-1);
            		s=Translator.getTransNuc(element);
            		gam="|g";
            		
            		//System.out.println("In numal line 234: text="+text+" translated="+s+" element="+element);
            	}
            		
            	if(!s.equals(" ")){
            		//check if nuclides with mass=A+offset or A-offset exist
            		Nucleus nuc=new Nucleus();
            		int offset=10;
            		boolean isNuclide=false;
            		try{
            			//System.out.println("In numal line 241: text="+text+" translated="+s+" element="+element+" mass="+mass);
            			
            			nuc.parseNUCID(mass+element);  
            			
            			//System.out.println("In numal line 243: text="+text+" translated="+s+" element="+element+" mass="+mass+" nuc.z="+nuc.z());
            			
            			if(mass>=nuc.z()){
            				//check neighbouring nuclides
            				for(int i=1;i<=offset;i++){
            					for(int j=0;j<2;j++){
            						int m=mass+(j*2-1)*i;   
            						if(m<=0)
            							continue;
            						
            						s=Translator.getTransNuc(m+element);

            						//System.out.println("In numal line 234: text="+text+" translated="+s+" element="+element+" mass="+m+" s="+s);
            						
            						if(!s.equals(" ")){
            							isNuclide=true;
            							break;
            						}
            					}
            					
            					if(isNuclide)
            						break;
            				}
            			}
            			
            		}catch (java.io.IOException e){   
            			isNuclide=false;
            			e.printStackTrace();
            		} 
            		         		
            		if(isNuclide){
                        out="{+"+intStr+"}"+nuc.En()+gam;
                        length=intStr.length()+intmst.length();
                        return;
            		}
            	}
            }
        }
        
        //debug
        //if(text.contains("31K")) System.out.println("numanl line 339: text="+text);
        
        //see if its a keynumber
        if(text.length()==8){
            boolean isKeyNo=true;
            for(int i=0;i<4;i++)
                if(text.charAt(i)<'0'||text.charAt(i)>'9') isKeyNo=false;
            if(text.charAt(4)<'A'||text.charAt(4)>'Z') isKeyNo=false;
            if((text.charAt(5)<'A'||text.charAt(5)>'Z')&&(text.charAt(5)<'a'||text.charAt(5)>'z')) isKeyNo=false;
            for(int i=6;i<7;i++)
                if((text.charAt(i)<'0'||text.charAt(i)>'9')&&(text.charAt(i)<'A'||text.charAt(i)>'Z')) isKeyNo=false;

            if(isKeyNo){
                out=text.substring(0,5)+text.substring(5,6).toLowerCase()+text.substring(6);
                length=8;
                
                //removes keynumbers in comments, useful when printing band labels
                flag="keyno";
                return;
            }
        }
        
        //check for number followed by A,B,G etc
        if((intmst.getWord().equals("A")||intmst.getWord().equals("B")||intmst.getWord().equals("G")||intmst.getWord().equals("EC"))){
            out=intStr+"|"+intmst.getWord().substring(0,1).toLowerCase();
            length=intStr.length()+intmst.getWord().length();
            
        	if(intmst.getWord().equals("B") && (intmst.getDelim()=='-'||intmst.getDelim()=='-')){
        		out+="{+"+intmst.getDelim()+"}";
        		length+=1;
        	}
            return;
        }

        
        String tempS=Translator.getTransWord(intmst.getWord());

        //debug
        //System.out.println("In numal line 241: hasExpo="+hasExpo+" intstr="+intStr+" translated="+tempS+" line="+line);
        
        if(!tempS.equals(" ") && !hasExpo){
        	length=intStr.length()+intmst.length();
        	out=intStr+tempS;
        	return;
        }
        
        //we have now eliminated all cases where >1 character follows the number, so 
        //we can just return the string if there is >1 character
        //or see if its ejectile code, like "2PNG"
        
        if(intmst.getWord().length()>=1 &&!hasExpo){
            out=intStr+intmst.getWord();
                    
            length=intStr.length()+intmst.length();
            
            //check if the whole word is in dictionary
            out=Translator.getTransWord(out);
            
            if(!out.equals(" "))
            	return;
            
            tempS=text;
            int n=tempS.length();
            boolean hasSymbol=false;
            for(int i=0;i<n;i++){
            	char ch=text.charAt(i);
            	if(!(ch>='0'&&ch<='9')&&!(ch>='A'&&ch<='Z')&&!(ch>='a'&&ch<='Z')){
            		hasSymbol=true;
            		break;
            	}
            }
            
            
            //if the sting contains special characters, return "" and leave it to procGenCom() in translator.java
            if(hasSymbol){
            	out="";
            	length=0;		
            	return;
            }

            
            out="";
            while(n>0){
                
            	if(tempS.charAt(tempS.length()-1)=='G' || tempS.charAt(tempS.length()-1)=='A')                  
                	out="|"+tempS.toLowerCase().charAt(tempS.length()-1)+out;
                else if(!out.contains("X") && tempS.charAt(n-1)=='X')
                	out='X'+out;
                else
                	out=tempS.toLowerCase().charAt(tempS.length()-1)+out;
                            
                s=tempS.substring(0,tempS.length()-1).toUpperCase();//set nucid to uppercase for translation
                s=Translator.getTransNuc(s);
                
                if(!s.equals(" ")){
                    out=s+out;
                    return;
                }
                
                tempS=tempS.substring(0, tempS.length()-1);
                n=tempS.length();
            }
           
            
            return;
        }
        
            	
        if(hasExpo){

            //debug
            //System.out.println("In numal line 313: hasExpo="+hasExpo+" intstr="+intStr+" expoStr="+expoStr);
            
            String exp=expoStr;//expoStr includes sign, not include "E"
            char sign=expoStr.charAt(0);
            
            if(sign=='+'||sign=='-')
            	exp=expoStr.substring(1);
          
            if(sign=='-') 
            	out=intStr+"|*10{+-"+exp+"}";
            else
            	out=intStr+"|*10{+"+exp+"}";  
            
        	
            length=intStr.length()+1+expoStr.length();
            
            if(expoWord.getDelim()=='%'){
            	out+="%";
            	length+=1;
            }

 

            
            //for uncertainty
            int pos=expoPos+expoStr.length()+1;
            String tempStr=line.substring(pos); //starting right after the expoStr

        	//debug
        	//if(line.contains("1.45"))
        	//		System.out.println("******getWord="+intmst.getWord()+" expoPos="+expoPos+" expoStr="+expoStr+" text="+text+" out="+out+" pos="+pos);

        	int n=tempStr.indexOf(" ");
            if((n==0 || (n==1&&tempStr.charAt(0)=='%'))){
                pos=pos+n;
                length=pos;
            	out+=numanl80(line,pos,pos);
            	
/*            	
            	int i=pos+1;
            	String unc="";
            	while(i<tempStr.length()){
            		if(tempStr.charAt(i)<'0' || tempStr.charAt(i)>'9')
            			break;
            		
            		unc+=tempStr.charAt(i);
            		i++;
            	}
            	
            	if(unc.length()>0){
            		out+="~{I"+unc+"}";
            		length+=unc.length();
            	}
*/
            }


            return;
        }
        length=line.length();
    }
    /**
     * Deals with normal numbers that might have units and uncertainties, like 1.2 fs 10
     */
    private String numanl80(String line,int firstNonInt,int pos){
        String done="";
        String temp;
        String [] oldUnits={"S", "M", "H", "D", "Y", "MS", "US", "MEV", "KEV",
         "EV", "NS", "PS", "FS", "MIN", "MB", "KOE", "UG", "MG", "FM",
          "MILLI", "CM", "CM**2", "CM2", "FM**2", "SR", "B", "AS","DEG"};
        String [] newUnits={"s", "min", "h", "d", "y", "ms", "|ms", "MeV", "keV", 
          "eV", "ns", "ps", "fs", "min", "mb", "kOe", "|mg", "mg",     
          "fm", "m", "cm", "cm{+2}", "cm{+2}", "fm{+2}", "sr", "b",    
          "as","|'"};
        //check if the number is followed by a space, if not, no units/uncertainty
        out=line.substring(pos,firstNonInt);
        boolean keepGoing=true;
        if(out.length()==line.length()) return out; // if the number is the last thing in the sentence, just end
        if(firstNonInt>=line.length()-1) return out;
        int tempcounter=0;
        while(keepGoing){
            if(tempcounter>100) 	
            {
            	//System.out.println("numanl80 loop: line="+line);
            	break;            	
            }
            //now we need to get units and uncertainties
          //  System.out.println(line);
            if(line.length()==firstNonInt+1) temp=line;
            Word w=new Word(line,firstNonInt+1);
            temp=w.getWord();
            delim=line.charAt(firstNonInt);
       
            //System.out.println("In mumanl80 line 553: temp="+temp+" delim="+delim+" w.getDelim="+w.getDelim()+" nextraspaces="+nextraspaces+" line="+line+" done="+done);
            
            if(delim!=' ' && delim!='+' && delim!='-') {
            	//if(delim=='/' && temp.equals("A") && (done.equals("MeV")||done.equals(" MeV"))) {
            		
            	//}else
            		break;
            }
            //debug
            //if(line.contains("1.6 +7")) 
            //   System.out.println("In mumanl80 line 560: temp="+temp+" delim="+delim+" w.getDelim="+w.getDelim()+" nextraspaces="+nextraspaces+" line="+line);
            
            int endPos=temp.length()+firstNonInt;
            boolean validUnc=false;
            if(temp.length()==0){
                firstNonInt++;
                if(delim==' '&&w.getDelim()==' ') 
                	nextraspaces++;
                
                if(firstNonInt>=line.length()-1) 
                	keepGoing=false;
                
                continue;
            }
            //if(w.getDelim()=='-') break;
            
            //debug
            //if(line.contains("1.45")) 
             //  System.out.println("In mumanl80-2: temp="+temp+" delim="+delim+" w.getDelim="+w.getDelim()+" nextraspaces="+nextraspaces+" line="+line);

            
            if((temp.charAt(0)>='0'&&temp.charAt(0)<='9')||temp.charAt(0)=='+'){
                //check to see if every character is a number. If so, its an uncertainty
                validUnc=true;
                for(int i=1;i<temp.length();i++){
                    if(!((temp.charAt(i)>='0'&&temp.charAt(i)<='9')||(temp.charAt(i)=='-'&&temp.charAt(0)=='+'))){
                        validUnc=false;
                    }
                }
                
                //int case like: 40 {+3}He, {+3} is not uncertainty
                char c=' ';
                int p=firstNonInt+temp.length()+1+1;//pos after delim of w
                if(p<line.length()) c=line.charAt(p);
                if(w.getDelim()=='}' && c!=' ')
                	validUnc=false;
            }
            
            if((delim=='+'||delim=='-')&&validUnc){
                //could be asymmetric uncertainty
                int pm=line.substring(firstNonInt+1).indexOf("-")+firstNonInt+1;
                char sign1='+';
                char sign2='-';
                
                if(delim=='-'){
                	pm=line.substring(firstNonInt+1).indexOf("+")+firstNonInt+1;
                	sign1='-';
                	sign2='+';
                }
                
                if(pm>firstNonInt&&pm<firstNonInt+5){
                    String un1=line.substring(firstNonInt+1,pm);
                    Word w1=new Word(line.substring(pm+1).trim(),0);
                    
                    String un2=w1.getWord();
                    length+=un1.length()+un2.length()+3;
                    done+=" {I"+sign1+un1+sign2+un2+"}";
                    
                    //debug
                    //if(line.contains("1.6 +7")){ 
                    //   System.out.println("In mumanl80-1: temp="+temp+" delim="+delim+" w.getDelim="+w.getDelim()+" nextraspaces="+nextraspaces+" line="+line);
                    //   System.out.println("                         un1="+un1+" un2="+un2);
                    //}
                    
                }
                keepGoing=false;
            }else if(delim==' '){
                //if starts with a number, it could be a symmetric uncertainty
                if(validUnc){
                    done+=" {I"+temp+"}";
                    length+=temp.length()+1;
                    keepGoing=false;                
                }else{
                    //see if it could be a unit
                    if(temp.length()>=1&&temp.length()<=5){
                        for(int i=0;i<oldUnits.length;i++){
                            if(temp.toUpperCase().equals(oldUnits[i])){
                                if(oldUnits[i].equals("DEG")) 
                                	done+=newUnits[i];
                                else if (newUnits[i].charAt(0)=='a'||newUnits[i].charAt(0)=='e'||newUnits[i].charAt(0)=='i'
                                		||newUnits[i].charAt(0)=='o'||newUnits[i].charAt(0)=='u' || newUnits[i].charAt(0)=='h')//~h translated as hbar
                                	done+=" "+newUnits[i];
                                else{ 
                                	if(temp.equals("meV"))
                                		done+=" meV";
                                	else
                                		done+=" "+newUnits[i];
                                }
                                length+=temp.length()+1;
                                firstNonInt=endPos+1;
                                //check if theres anything left to get
                                if(firstNonInt>=line.length()-1) 
                                	keepGoing=false;
                                
                                break;
                            }
                            if(i==oldUnits.length-1) 
                            	keepGoing=false;
                        }
                    }
                }            
            }
            //else if(delim=='/'){
            //	done+="/A";
            //}
            else {
                //not a unit or uncertainty
                keepGoing=false;
            }
            tempcounter++;
        }
        //System.out.println("### numanl80 line 667: return="+out+done);
        
        return out+done;
    }
    public String getLine(){return out;}
    public int getLength(){return length;}
    public int getNExtraSpaces(){return nextraspaces;}
    public boolean isNilsson(){return isNilsson;}
    public char getDelim(){return delim;}
    public String getFlag(){return flag;}
}
