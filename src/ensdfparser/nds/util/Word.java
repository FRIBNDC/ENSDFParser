package ensdfparser.nds.util;

public class Word
{
  private char[] dels = { ' ', ',', '.', ';', ':', '(', ')', '-', '=', '+', '<', '>', '/', '?', '$', '^', '[', ']', '{', '}', '\000', '|', '\'', '%' };
  private String out = "";
  private char delim = '!';
  
  public Word(String line, int pos)
  {
    int inBrackets = 0;
    int begin = pos;
    if (pos > line.length() - 1) {
      return;
    }
    if (line.charAt(pos) == '(')
    {
      this.delim = '(';
      begin++;
      inBrackets++;
    }
    for (int i = begin; i < line.length(); i++)
    {
      char temp = line.charAt(i);
      for (int j = 0; j < this.dels.length; j++) {
        if ((temp == this.dels[j]) && ((this.dels[j] != '.') || (i >= line.length() - 1) || (line.charAt(i + 1) == ' '))){
          if (this.dels[j] == '(') {
            inBrackets++;
          }
          if ((this.dels[j] == ')') && (inBrackets > 0)) {
            inBrackets--;
          }
          if ((inBrackets == 0) || (this.dels[j] == '(')){
          
            this.delim = this.dels[j];
            this.out = line.substring(pos, i);
            
            if (this.delim == '{'){
              if ((this.out.equals("A")) || (this.out.equals("B"))) {
                continue;
              }
              if (this.out.equals("G")) {
                continue;
              }
            }else if (this.delim == '='){
              if ((this.out.equals("A")) || (this.out.equals("B")) || (this.out.equals("G"))){
                char prevChar = ' ';
                if (pos > 0) {
                  prevChar = line.charAt(pos - 1);
                }
                if ((prevChar != '+') && (prevChar != '-') && (prevChar != '/'))
                {
                  this.out += '=';
                  this.delim = '!';
                }
              }
            }else if (this.out.equals("A")){
              if ((this.delim == ' ') && ( (pos == 0) || (line.substring(pos - 1, pos).equals(" ")))){             
                String s = line.substring(i + 1).trim().toUpperCase();
                if (s.indexOf("DECAY") != 0) {
                  this.out = "a";
                }
              }
            }else if (this.out.equals("G")){
              String s = line.substring(i + 1).toUpperCase();
              if (((this.delim == ' ') || (this.delim == '-')) && (s.indexOf("FACTOR") == 0)) {
                continue;
              }
            }
            return;
          }
        }   
      }//end for
      if (i == line.length() - 1){
        this.out = line.substring(pos);
        this.delim = 'n';

      }
    }
  }
  
  public String getWord()
  {
    return this.out;
  }
  
  public char getDelim()
  {
    return this.delim;
  }
  
  public char firstChar()
  {
    return this.out.charAt(0);
  }
  
  public char charAt(int i)
  {
    return this.out.charAt(i);
  }
  
  public int length()
  {
    return this.out.length();
  }
}
