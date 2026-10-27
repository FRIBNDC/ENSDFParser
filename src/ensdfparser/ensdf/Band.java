 package ensdfparser.ensdf;

import java.util.*;

/*
 * for band and sequence
 */
public class Band{

  private Vector<Level> lV=new Vector<Level>();

  private Comment c;
  private int ncolumns=0;
  
  private String type="BAND";//default
 
  public Band(){

    lV.clear();

  }

  public void setType(String s) {
      s=s.toUpperCase();
      if(!s.startsWith("BAND"))
          type="SEQ";
  }
  public String type() {return type;}
  
  public void addLevel(Level l){
    lV.addElement(l);
  }

  public void setComment(Comment ic){
    c=ic;
  }

  public void setNColumns(int n){ncolumns=n;}
  
  public int nLevels(){ return lV.size(); }
  
  public Vector<Level> levels(){return lV;}

  public Level levelAt(int j){
      if(j<0)
          return null;
      
      return (Level) lV.elementAt(j); 
      }

  
  public Level firstLevel(){return (Level) lV.firstElement(); }
  public Level lastLevel(){return (Level) lV.lastElement(); }

  public String tag(){return c.flags(); }

  public Comment comment(){return c;}
  
  public int nColumns(){return ncolumns;}
  
}