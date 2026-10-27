
package ensdfparser.nds.util;

/**
 * Given a long sentence, returns only the next word in that sentence
 * 
 * Where the next word starts is determined by a set of delimiters
 */
public class Word_bk {
 
	//note that '!' is reserved as a fake delim; '!' in a text is not treated as a delim; also '*' is not treated as a delim
    private char[]dels={' ',',','.',';',':','(',')','-','=','+','<','>','/','?','$','^','[',']','{','}','\0','|','\'','%'};
    private String out="";
    private char delim='!';//when line="" or pos>line.lengh()-1;

    public Word_bk(String line,int pos){
        //delimiters that indicate a new word
        char temp;
        int inBrackets=0;
        int begin=pos;
        
        if(pos>line.length()-1)
        	return;
        //we ignore delimiters if they are enclosed in brackets
        if(line.charAt(pos)=='('){
        	delim='(';
            begin++;
            inBrackets++;
        }
        
        //debug
        //System.out.println("%%%%%In Word: at beginning1: line="+line+" pos="+pos+" length="+line.length());
        
        for(int i=begin;i<line.length();i++){
            temp=line.charAt(i);
            //scan string until a delimiter is found
            for(int j=0;j<dels.length;j++){
                if(temp==dels[j]){
                    if(dels[j]=='.'&&i<line.length()-1&&line.charAt(i+1)!=' ')continue;
                    //keeps track if whether we are in brackets or not
                    if(dels[j]=='(') inBrackets++;
                    if(dels[j]==')'&&inBrackets>0) inBrackets--;
                    //if not in brackets, finish loop
                    if(inBrackets!=0&&dels[j]!='(') continue;
                    
                    delim=dels[j];
                    out=line.substring(pos,i);
                     
                    if(delim=='{'){
                    	if(out.equals("A") || out.equals("B") || out.equals("G"))
                    		continue;
                    }else if(delim=='='){
                    	if(out.equals("A") || out.equals("B") || out.equals("G")){
                    		char prevChar=' ';
                    		if(pos>0) prevChar=line.charAt(pos-1);
                    		if(prevChar!='+' && prevChar!='-'&&prevChar!='/'){ //e.g., for next word starting at "A" in %B+A=2.77, %B-A=1.23, G/A=1.2, word="A", for other cases, word="A=" 
                    			out+='=';
                    			delim='!';
                    			//continue;
                    		}
                    	}
                    }else if(out.equals("A")){//print " A " as A
                    	if(delim==' '){
                        	if(pos==0 || line.substring(pos-1,pos).equals(" ")){
                        		String s=line.substring(i+1).trim().toUpperCase();
                        		if(s.indexOf("DECAY")!=0)
                        			out="a";
                        			
                        	}
                        		
                    	}

                    }else if(out.equals("G")){//print G FACTORS
                    	String s=line.substring(i+1).toUpperCase();
                    	if((delim==' '||delim=='-')&&s.indexOf("FACTOR")==0)
                    		continue;
                    }
                    	
                    
                    //debug
                    //System.out.println("%%%%%In Word: at end: delim="+delim+" out="+out);
                    
                    return;
                }
            }
            if(i==line.length()-1){ out=line.substring(pos); delim='n';}
        }
        return;
    }
    
    public String getWord(){ return out;}
    public char getDelim(){return delim;}
    public char firstChar(){return out.charAt(0);}
    public char charAt(int i){return out.charAt(i);}
    public int length(){return out.length();}
}
