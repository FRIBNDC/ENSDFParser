 package ensdfparser.ensdf;

public class WebSpecChar{
  private String s;
  private String ts;
  private String brow;


  private boolean b_ie;
  @SuppressWarnings("unused")
private boolean b_nt;
  @SuppressWarnings("unused")
private boolean b_ff;
  @SuppressWarnings("unused")
private boolean b_nt7;
  private boolean b_win;
  @SuppressWarnings("unused")
private boolean b_lin;

  public WebSpecChar(){
    s="";
    ts="";
    brow="";

    b_ie=false;
    b_nt=false;
    b_ff=false;
    b_win=false;
    b_lin=false;

  }
  public void setBrowser(String ibrowser){

    brow=ibrowser.toUpperCase().trim();
    b_ie=false;
    b_nt=false;
    b_nt7=false;
    b_win=false;
    b_lin=false;

    if( brow.indexOf("MSIE") >= 0 ){ b_ie=true; }
    else if( brow.indexOf("MOZILLA") >= 0 && brow.indexOf("NETSCAPE/7") >= 0 ){ b_nt7=true; } /* NETSCAPE 7.X */
    else if( brow.indexOf("MOZILLA") >= 0 && brow.indexOf("FIREFOX") > 0){ b_ff=true; }
    else if( brow.indexOf("MOZILLA") >= 0 && brow.indexOf("NETSCAPE") < 0){ b_nt=true; }

    if( brow.indexOf("WINDOW") >= 0 ){ b_win=true; }
    else if( brow.indexOf("LINUX") >= 0 ){ b_lin=true; }

  }
  public boolean getBIE(){ return b_ie;}
  public boolean getBWIN(){ return b_win;}

 public String SC(String is){ 
    s=is.trim();
    ts=is.trim();

    if( s.equals("LE") ||  s.equals("le") || s.equals("<=") || s.equals("=<")  ) ts="&le;";

    else if( s.equals("LT") ||  s.equals("lt") || s.equals("<")  )  ts="&lt;";

    else if( s.equals("GE") ||  s.equals("ge") || s.equals(">=") || s.equals("=>")  ) ts="&ge;";

    else if( s.equals("GT") || s.equals("gt") ||s.equals(">")  ) ts="&gt;";

    else if( s.equals("AP") || s.equals("ap") || s.equals("APP") || s.equals("app")  ) ts="&asymp;";

    else if( s.toLowerCase().equals("alpha") || s.toLowerCase().equals("|a") ) ts="&alpha;";

    else if( s.toLowerCase().equals("beta") || s.toLowerCase().equals("|b")) ts="&beta;";

    else if( s.equals("gamma") || s.equals("|g") ) ts="&gamma;";

    else if( s.equals("Gamma") || s.equals("|G") || s.equals("Width") ) ts="&Gamma;";

    else if( s.equals("delta") || s.equals("|d") ) ts="&delta;";

    else if( s.equals("epsilon") ) ts="&epsilon;";

    else if( s.equals("mu") || s.equals("|m") || s.equals("micro")) ts="&mu;";

    else if( s.equals("nu") || s.equals("|n") ) ts="&nu;";

    else if( s.equals("rho") || s.equals("|r") ) ts="&rho;";

    else if( s.equals("sigma") || s.toLowerCase().equals("cross section") ) ts="&sigma;";

    else if( s.equals("pi") ||  s.equals("Pi") || s.equals("PI")) ts="&pi;";

    else if( s.equals("DELTA") ||   s.equals("Delta") || s.toLowerCase().equals("mass excess") || s.equals("|D")) ts="&Delta;";

    else if( s.equals("theta") || s.equals("|q") ) ts="&theta;";

    else if( s.equals("US") )ts="&mu;S";

/* The decay modes: */

    else if( s.equals("EC") ) ts="&epsilon;"; 
    else if( s.equals("EP") || s.equals("ECP") )  ts="&epsilon;p"; 
    else if( s.equals("E2P") ) ts="&epsilon;2p"; 
    else if( s.equals("E3P") ) ts="&epsilon;3p"; 
    else if( s.equals("EA") ) ts="&epsilon;&alpha;"; 
    else if( s.equals("E2A") ) ts="&epsilon;2&alpha;"; 
    else if( s.equals("E3A") ) ts="&epsilon;3&alpha;"; 
    else if( s.equals("EP2A") ) ts="&epsilon;p2&alpha;"; 
    else if( s.equals("P2A") ) ts="p2&alpha;"; 
    else if( s.equals("2EC") ) ts="2&epsilon;";
    else if( s.equals("EF") ) ts="&epsilon;F";
    else if( s.equals("ESF") ) ts="&epsilon;F";
    else if( s.equals("EC+B+") ) ts="&epsilon;";
    else if( s.equals("B+") ) ts="&beta;<sup>+</sup>";
    else if( s.equals("B+P") ) ts="&beta;<sup>+</sup>p";
    else if( s.equals("2B+") ) ts="2&beta;<sup>+</sup>";

    else if( s.equals("B-") ) ts="&beta;<sup>-</sup>"; 
    else if( s.equals("BN") ) ts="&beta;<sup>-</sup>n";
    else if( s.equals("B-N") ) ts="&beta;<sup>-</sup>n";
    else if( s.equals("B2N") ) ts="&beta;<sup>-</sup>2n";
    else if( s.equals("B3N") ) ts="&beta;<sup>-</sup>3n";
    else if( s.equals("B4N") ) ts="&beta;<sup>-</sup>4n";
    else if( s.equals("BXN") ) ts="&beta;<sup>-</sup>xn";
    else if( s.equals("BNA") ) ts="&beta;<sup>-</sup>n&alpha;";
    else if( s.equals("BN2A") ) ts="&beta;<sup>-</sup>n2&alpha;";
    else if( s.equals("BA") ) ts="&beta;<sup>-</sup>&alpha;";
    else if( s.equals("B2A") ) ts="&beta;<sup>-</sup>2&alpha;";
    else if( s.equals("B3A") ) ts="&beta;<sup>-</sup>3&alpha;";
    else if( s.equals("BT") ) ts="&beta;<sup>-</sup>t";
    else if( s.equals("BP") ) ts="&beta;<sup>-</sup>p";
    else if( s.equals("2B-") ) ts="2&beta;<sup>-</sup>";
    else if( s.equals("BB") ) ts="2&beta;<sup>-</sup>"; 

    else if( s.equals("A") ) ts="&alpha;";
    else if( s.equals("2A") ) ts="2&alpha;";
    else if( s.equals("12C") ) ts="<sup>12</sup>C"; 
    else if( s.equals("14C") ) ts="<sup>14</sup>C"; 
    else if( s.equals("20O") ) ts="<sup>20</sup>O"; 
    else if( s.equals("34Si") ) ts="<sup>34</sup>Si"; 
    else if( s.equals("34SI") ) ts="<sup>34</sup>Si"; 
    else if( s.equals("28Mg") )  ts="<sup>28</sup>Mg"; 
    else if( s.equals("30Mg") )  ts="<sup>30</sup>Mg"; 

    else if( s.equals("P") ) ts="p";
    else if( s.equals("2P") ) ts="2p";
    else if( s.equals("3P") ) ts="3p";
    else if( s.equals("N") ) ts="n";
    else if( s.equals("2N") ) ts="2n";
    else if( s.equals("3N") ) ts="3n";

    return ts;
  }
}