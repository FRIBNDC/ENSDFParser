 package ensdfparser.ensdf;

import java.util.*;

public class TableRecord{

  private Vector<String> lineV=new Vector<String>();

  public TableRecord(){

    lineV.clear();

  }
  public void setValues(Vector<String> v){ lineV=v;}

  public int nLines(){ return lineV.size(); }

  public String tableLineAt(int j){return ( (String) lineV.elementAt(j) ).substring(8); }
  public String fullLineAt(int j){return  (String) lineV.elementAt(j); }
  public boolean bigT(int j){
      if(((String)lineV.elementAt(j)).charAt(6)=='t') return false;
      else return true;
  }
  public char type(){return ((String)lineV.elementAt(0)).charAt(7);}

}