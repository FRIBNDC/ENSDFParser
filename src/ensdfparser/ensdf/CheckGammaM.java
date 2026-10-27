 package ensdfparser.ensdf;

public class CheckGammaM{

  private Level li; 
  
  private Level lf;
  
  private Gamma g;

  @SuppressWarnings("unused")
  private float  gj; 
  @SuppressWarnings("unused")
  private int gp;

  private boolean canDo;

  private String warning;

  @SuppressWarnings("unused")
  private String ms;

  public CheckGammaM(){

    gj=0;
    gp=0;
    canDo=false;
    warning="";
  }

  public void setValues( Level ili , Gamma ig , Level ilf){
    li=ili;
    lf=ilf;
    g=ig;
    doWork();
  }

  private void doWork(){

    canDo=true;

    if( g.MS().trim().length() > 0 && li.nJs() > 0  && lf.nJs() > 0){

    canDo=false;
 
    for( int j=0 ; j < li.nJs() ; j++){
    for( int k=0 ; k < lf.nJs() ; k++){

        float jmax=lf.jValAt(k)  + g.L() ;
        float jmin=Math.abs(lf.jValAt(k)  - g.L()) ;

        if( li.jValAt(j) >= jmin && li.jValAt(j) <= jmax ){

           if( li.piIValAt(j) != 0 && g.piI() != 0 && lf.piIValAt(k) != 0 ){

               if( li.piIValAt(j) == ( lf.piIValAt(k)*g.piI() ) ) canDo=true;

           }
           else{

             canDo=true;

           }    
        }

    }
    }

    }

    if( ! canDo ) warning="Gamma multipolarity not compatible with Initial and Final levels Spins";

/* End of dowork: */

  }

/*  public methods */

  public String warning(){ return warning;}
  public boolean canDo(){ return canDo;}

}