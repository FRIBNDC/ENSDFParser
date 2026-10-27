 package ensdfparser.ensdf;

import java.util.*;

public class GammaPath{


  private Vector<Short> levelV=new Vector<Short>();
  private Vector<Short> gammaV=new Vector<Short>();
  private Vector<Double> tV=new Vector<Double>();
  private double sum;

  private boolean hasGamma;
  private boolean has2Gammas;
  private boolean has3Gammas;
  private boolean has4Gammas;

  @SuppressWarnings("unused")
private int hasGammaj;
  private int hasGammaj1;
  private int hasGammaj2;
  private int hasGammaj3;
  private int hasGammaj4;

  public GammaPath(){
    levelV.clear();
    gammaV.clear();
    tV.clear();
    hasGamma=false;
    has2Gammas=false;
    hasGammaj=-1;
    sum=0.0;
  }
  public void clear(){
    levelV.clear();
    gammaV.clear();
    tV.clear();
    hasGamma=false;
    has2Gammas=false;
    hasGammaj=-1;
    sum=0.0;
  }

/* ------------------------------------------------------------------ */

  public void addElement(int l, int g, double d){
    short sl=(short) l;
    short sg=(short) g;
    gammaV.addElement(new Short(sg) );
    levelV.addElement(new Short(sl) );

    if( d >= 0 ) tV.addElement(new Double(d) );
    else  tV.addElement(new Double(0.0) );
  }

/* ------------------------------------------------------------------ */

  public void addVector(Vector<Short> lv, Vector<Short> gv, Vector<Double> tv){

    for( int j=0; j<lv.size(); j++){
      gammaV.addElement( (Short) gv.elementAt(j));
      levelV.addElement( (Short) lv.elementAt(j));
      tV.addElement( (Double) tv.elementAt(j));
    }

  }

/* ------------------------------------------------------------------ */

  public Vector<Short> levelVector(){
    return levelV;
  }

/* ------------------------------------------------------------------ */

  public Vector<Short> gammaVector(){
    return gammaV;
  }

/* ------------------------------------------------------------------ */

  public Vector<Double> tVector(){
    return tV;
  }

/* ------------------------------------------------------------------ */

  public int levelAt(int k){
    return ( (Short) levelV.elementAt(k) ).intValue();
  }

/* ------------------------------------------------------------------ */

  public int gammaAt(int k){
    return ( (Short) gammaV.elementAt(k) ).intValue();
  }

/* ------------------------------------------------------------------ */

  public double tAt(int k){
    return ( (Double) tV.elementAt(k) ).doubleValue();
  }

/* ------------------------------------------------------------------ */

  public int firstLevel(){
    return ( (Short) levelV.firstElement() ).intValue();
  }
/* ------------------------------------------------------------------ */

  public int lastLevel(){
    return ( (Short) levelV.lastElement() ).intValue();
  }

/* ------------------------------------------------------------------ */

  public int lastGamma(){
    return ( (Short) gammaV.lastElement() ).intValue();
  }

/* ------------------------------------------------------------------ */

  public double tSum(){
    sum=0.0;
    for( int j=0;j<tV.size();j++){
      if( tAt(j) > 0 ) sum=sum+tAt(j);
    }
    return sum;
  }
  public double tSum(int i1, int i2){
    sum=0.0;
    for( int j=i1+1;j<=i2;j++){
      if( tAt(j) > 0 ) sum=sum+tAt(j);
    }
    return sum;
  }

  public int size(){ return levelV.size();}

/* ------------------------------------------------------------------ */

  public boolean hasGamma(int il, int ig){

    hasGamma=false;
    int j=0;
 
    while( j < gammaV.size() && ! hasGamma ){

      if( gammaAt(j) == ig && levelAt(j) == il){
         hasGamma=true;
         hasGammaj=j;
      }

      j++;
    }

    return hasGamma;
  }

/* ------------------------------------------------------------------ */

  @SuppressWarnings("unused")
public boolean has2Gammas(int il1, int ig1,int il2, int ig2){

    has2Gammas=false;

    boolean hasGamma1=false;
    boolean hasGamma2=false;
    boolean hasGamma3=false;
    boolean hasGamma4=false;

    hasGammaj1=-1;
    hasGammaj2=-1;
    hasGammaj3=-1;
    hasGammaj4=-1;

    if( il1 <= levelAt(0) && il2 <= levelAt(0) && il1 >= lastLevel() && il2 >= lastLevel() ){

        int j=0;
        while( j < gammaV.size() && ! has2Gammas ){
            if( levelAt(j) == il1){ if( gammaAt(j) == ig1){ hasGammaj1=j; hasGamma1=true; } } 
            if( levelAt(j) == il2){ if( gammaAt(j) == ig2){ hasGammaj2=j; hasGamma2=true; } } 
            if( hasGamma1 && hasGamma2 ) has2Gammas=true;
            j++;
        }
    }
    return has2Gammas;
  }

/* ------------------------------------------------------------------ */

  @SuppressWarnings("unused")
public boolean has3Gammas(int il1, int ig1,int il2, int ig2,int il3, int ig3){

    has3Gammas=false;

    boolean hasGamma1=false;
    boolean hasGamma2=false;
    boolean hasGamma3=false;
    boolean hasGamma4=false;

    hasGammaj1=-1;
    hasGammaj2=-1;
    hasGammaj3=-1;
    hasGammaj4=-1;

    int j=0;
    while( j < gammaV.size() && ! has3Gammas ){

      if( gammaAt(j) == ig1 && levelAt(j) == il1){ hasGamma1=true; hasGammaj1=j;}

      if( gammaAt(j) == ig2 && levelAt(j) == il2){ hasGamma2=true; hasGammaj2=j;}

      if( gammaAt(j) == ig3 && levelAt(j) == il3){ hasGamma3=true; hasGammaj3=j;}

      if( hasGamma1 && hasGamma2 && hasGamma3 ) has3Gammas=true;

      j++;
    }

    return has3Gammas;
  }


/* ------------------------------------------------------------------ */

  public boolean has4Gammas(int il1, int ig1,int il2, int ig2,int il3, int ig3,int il4, int ig4){

    has4Gammas=false;

    boolean hasGamma1=false;
    boolean hasGamma2=false;
    boolean hasGamma3=false;
    boolean hasGamma4=false;

    hasGammaj1=-1;
    hasGammaj2=-1;
    hasGammaj3=-1;
    hasGammaj4=-1;

    int j=0;
    while( j < gammaV.size() && ! has3Gammas ){

      if( gammaAt(j) == ig1 && levelAt(j) == il1){ hasGamma1=true; hasGammaj1=j;}

      if( gammaAt(j) == ig2 && levelAt(j) == il2){ hasGamma2=true; hasGammaj2=j;}

      if( gammaAt(j) == ig3 && levelAt(j) == il3){ hasGamma3=true; hasGammaj3=j;}

      if( gammaAt(j) == ig4 && levelAt(j) == il4){ hasGamma4=true; hasGammaj4=j;}

      if( hasGamma1 && hasGamma2 && hasGamma3 && hasGamma4 ) has4Gammas=true;

      j++;
    }

    return has4Gammas;
  }

/* ------------------------------------------------------------------ */

  public Vector<Integer> reducedLevelPath(){
    Vector<Integer> rp=new Vector<Integer>();
    if( has2Gammas ){
      int iMin=10000;
      if( hasGammaj1 >= 0 ) iMin=Math.min( iMin, hasGammaj1 );
      if( hasGammaj2 >= 0 ) iMin=Math.min( iMin, hasGammaj2 );
      if( hasGammaj3 >= 0 ) iMin=Math.min( iMin, hasGammaj3 );
      if( hasGammaj4 >= 0 ) iMin=Math.min( iMin, hasGammaj4 );

      int iMax=0;
      if( hasGammaj1 >= 0 ) iMax=Math.max( iMax, hasGammaj1 );
      if( hasGammaj2 >= 0 ) iMax=Math.max( iMax, hasGammaj2 );
      if( hasGammaj3 >= 0 ) iMax=Math.max( iMax, hasGammaj3 );
      if( hasGammaj4 >= 0 ) iMax=Math.max( iMax, hasGammaj4 );

      for( int j=iMin; j<=iMax; j++){ rp.addElement(new Integer((  levelV.elementAt(j) ).intValue() ) ); }
    }
    return rp;
  }


}