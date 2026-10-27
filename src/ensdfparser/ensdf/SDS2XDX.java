 package ensdfparser.ensdf;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ensdfparser.calc.UncertaintyParser;
import ensdfparser.calc.UncertaintyParser.UncParser;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

public class SDS2XDX {

  @SuppressWarnings("unused")
private String s,s0;
  private String ds,ds0,dsu,dsl;
  private String real_ds,real_dsu,real_dsl;
  
  private String strRepr="";
  
  private String id="",ref="";
  
  private double x,x0;
  private double dx,dxl,dxu;
  private double dx0=-1,dxu0=-1,dxl0=-1;//temp dx to still calculate if any has dx (even if others have no good dx)
  
  private double x_lv_d, x_uv_d, sx, dsx;
  private double upLimit=1e25;

  private int nexp;
  private String bases,exps;
  private double basex;
  
  private String nns="";/* non-numerical energy, if e_s="100+X" nns="X" */
  private String ns="";/* numerical energy if e_s="100+X" ns="100" */
  private String ns0="";//if e_s=1.3E3+X, ns0=bases=1.3
  
  private static int defaultErrorLimit=35;
  
  //note that errorLimit could be changed in setValue(x,dx,errorLimit), since it calls XDX2SDS::setValues(x,dx,errorLimit)
  //where XDX2SDS's errorLimit could be set differently if isAddExtraDigitForLargeUnc is set to be true, e.g, for a value 
  //like 8(5) when errorLimit=35, the final result could be 8.2(46) with errorLimit being changed to 99, if 
  //isAddExtraDigitForLargeUnc=true
  private int errorLimit=defaultErrorLimit;//used in XDX2SDS, for operations of add, multiply, etc
  
  //by default <10% considered as small uncertainty, Taylor's expansion can be used for propagating uncertainties
  private static double smallError=0.10;
  

  //value smaller than this will be printed in scientific format
  //This will actually be used by XDS2SDS
  @SuppressWarnings("unused")
  private static double expFormatLimitSmallNumber=0.001;
  private static double expFormatLimitLargeNumber=10000;
  
  //used for rounding final value without numerical uncertainty
  private static int defaultMaxNsigma=3;
  
  private int nsig=defaultMaxNsigma;
  
  private boolean isLargeUnc=false;
  private boolean isCrossZero=false;
  
  //Note that this only applies to large uncertainty (isLargeUnc=true)
  private boolean isAllowMaxMinEP=false;//by default use maximum&minimum approach propagating large uncertainties
  private boolean isUseNormEP=true;//Taylors expansion approach
  private boolean isUseMCEP=false;
  
  private static boolean isAllowMaxMinEPByDefault=false;//set once to apply all instances
  private static boolean isUseNormEPByDefault=true;//set once to apply all instances
  private static boolean isUseMCEPByDefault=false;//set once to apply all instances
  
  //automatically add one extra digit, only for large uncertainty >=50% of value 
  //and both value (base part if it is expo format) and uncertainty have length=1, e.g., 8(5) or 8E2(5)
  //this condition is applied to operations on a SDS2XDX object
  private static boolean isAddExtraDigitForLargeUnc=true;
  
  //keep the same number of digits in uncertainty in the result as that in the current number
  private boolean isKeepSameUncNDigit=false;
  
  //keep the same number of digits after decimal point in the final uncertainty with that of s2x to be operated on
  public boolean isKeepSameNDAfterDot=false;
	
  @SuppressWarnings("unused")
private float xf;
  @SuppressWarnings("unused")
private float dxf;
  @SuppressWarnings("unused")
private float x_lv_f, x_uv_f, xsf, dxsf;


  private boolean bx, bdx, bx_lv_d, bx_uv_d, bsx, bdsx, dsIsDigit, dsIsAsymm;
  private boolean isLimit=false;
  
  UncParser uncParser=null;
  
  public SDS2XDX(){
    resetAll();
  }

  public SDS2XDX(String name){
      this();  
      id=name;
  }
  
  
  //by default, input uncertainty string is ENSDF-style 
  public SDS2XDX(String s1,String s2){
      this();
      setValues(s1,s2);
  }
  
  public SDS2XDX(String s1,String s2,int errorLimit){
      this();
      setValues(s1,s2);
      setErrorLimit(errorLimit);
  }
  
   
  public void resetAll() {
        id="";
        x=-1;x0=-1;
        dx=-1;dxl=-1;dxu=-1;
        dx0=-1;dxl0=-1;dxu0=-1;
        
        x_lv_d=-1;
        x_uv_d=-1;
        sx=-1; dsx=-1;
        s="";s0="";
        ds="";ds0="";dsu="";dsl="";
        real_ds="";real_dsu="";real_dsl="";
        ns="";ns0="";
        nns="";
        
        bx=false; bdx=false; bx_lv_d=false; bx_uv_d=false; bsx=false; bdsx=false; dsIsDigit=false;isLimit=false;
        
        dsIsAsymm=false;
        
        nexp=0;
        exps="";bases="";
        basex=-1;
        
        errorLimit=defaultErrorLimit;
        
        isLargeUnc=false;
        isCrossZero=false;
        
        isAllowMaxMinEP=SDS2XDX.isAllowMaxMinEPByDefault;
        isUseMCEP=SDS2XDX.isUseMCEPByDefault;
        isUseNormEP=SDS2XDX.isUseNormEPByDefault;
        		
        this.setDefaultUncParser();
  }
  
  /*
   * reset all parameters related to value and uncertainty themselves
   * not including name and error limit 
   */
  public void resetValues() {

        x=-1;x0=-1;
        dx=-1;dxl=-1;dxu=-1;
        dx0=-1;dxl0=-1;dxu0=-1;
        
        x_lv_d=-1;
        x_uv_d=-1;
        sx=-1; dsx=-1;
        s="";s0="";
        ds="";ds0="";dsu="";dsl="";
        real_ds="";real_dsu="";real_dsl="";
        bx=false; bdx=false; bx_lv_d=false; bx_uv_d=false; bsx=false; bdsx=false; dsIsDigit=false;
        
        dsIsAsymm=false;
        
        isLargeUnc=false;
        isCrossZero=false;
        
        isAllowMaxMinEP=SDS2XDX.isAllowMaxMinEPByDefault;
        isUseMCEP=SDS2XDX.isUseMCEPByDefault;
        isUseNormEP=SDS2XDX.isUseNormEPByDefault;
        
        nexp=0;
        exps="";bases="";
        basex=-1;
        
        ns="";ns0="";
        nns="";
  }
  
  public SDS2XDX removeEndZeros() {

	  String[] out=Str.removeEndZeros(s, ds, dsu, dsl);
   	  
	  SDS2XDX s2x=null;
	  if(out[2].length()>0 && out[3].length()>0)
		  s2x=new SDS2XDX(s,"+"+dsu+"-"+dsl);
	  else
		  s2x=new SDS2XDX(s,ds);
	  
	  s2x.setErrorLimit(errorLimit);
	  
	  s2x.x=x;
	  s2x.dx=dx;
	  s2x.dxu=dxu;
	  s2x.dxl=dxl;
	  
	  s2x.s=out[0];
	  s2x.ds=out[1];
	  s2x.dsu=out[2];
	  s2x.dsl=out[3]; 
	  
	  return s2x;
  }
  
  /*
   * round up a number by keeping n digits at the end of the number string after decimal point
   * using scientific format if rounding up beyond ".",
   * with base within (1.0 to 9.9)
   * If n is so large that base<1.0, return original number string without rounding
   * offset: added to control rounding up or down
   * e.g., for value, add 0.55 to round up xxx.45, (note xxx.44 will be rounded down)
   *       for uncertainty, add, 0.7 to round up x.3
   */
  public SDS2XDX roundToNDigitsAfterDot(int n,float offset){
	  SDS2XDX out=null;
	  try {
		  out=new SDS2XDX();
		  out.setValues(this);
		  
		  String s=out.s;
		  s=Str.roundToNDigitsAfterDot(s, n, offset);

		  
		  int ne=s.indexOf("E");
		  String expS="";
		  int power=0;
		  if(ne>0) {
			expS=s.substring(ne);
			s=s.substring(0,ne).trim();
			if(expS.length()>1) {
				String tempS=expS.substring(1);
				char c=tempS.charAt(0);
				if(c=='+'||c=='-')
					tempS=tempS.substring(1);
				
				if(Str.isInteger(tempS)){
					power=Integer.parseInt(expS.substring(1));
				}
			}
		  }
		  
		  n=Str.findNDigitsAfterDot(s);
		  if(n<0)
			  n=0;
		  
		  double dxu= (out.dxu*Math.pow(10, -power+n));
		  double dxl= (out.dxl*Math.pow(10, -power+n));
			
		  long dxuLong=Str.roundUncertainty(dxu);
		  long dxlLong=Str.roundUncertainty(dxl);
		  if(dxuLong==0 || dxlLong==0)
			  return null;
		  
		  s=s+expS;
		  out.setS(s);
		  
		  String dsu=String.valueOf(dxuLong);
		  String dsl=String.valueOf(dxlLong);
		  
		  //System.out.println(dxuLong+" dsu="+dsu+" "+dxlLong+" dsl="+dsl);
		  if(dxuLong==dxlLong) {
			  out.setDS(dsu);
		  }else {
			  out.setDS("+"+dsu+"-"+dsl);
			  out.dsu=dsu;
			  out.dsl=dsl;
		  }	  	  
	  }catch(Exception e) {
		  
	  }
	  return out;
  }
  
  public SDS2XDX roundToNDigitsAfterDot(int n){
	  return roundToNDigitsAfterDot(n,0);
  }
  
  public SDS2XDX roundToOneDigitUnc(){
	  SDS2XDX sx=new SDS2XDX(s,ds);
	  sx.setErrorLimit(9);
	  sx=sx.multiply("1.0","0");
	  
	  return sx;
  }
  
  
  /*
   * ds in ENSDF-style format
   */
  public SDS2XDX roundToOneDigitUnc(String s,String ds){
	  SDS2XDX sx=new SDS2XDX(s,ds);
	  return sx.roundToOneDigitUnc();
  }
  
  /*
   * ds in ENSDF-style format
   */
  public static SDS2XDX roundToNDigitsAfterDot(String s,String ds,int n) {
	  try {
		  SDS2XDX out=new SDS2XDX(s,ds);
		  return out.roundToNDigitsAfterDot(n);
	  }catch(Exception e) {
		  
	  }
	  return null;
  }
  
  public void setValues( String s1, String ds1){
    resetValues();  
      
    //System.out.println("SDS2XDX 285: s="+s1+" ds="+ds1);
    
    s=s1;
      
    ns=s1.replace('(',' ').replace(')',' ').trim();

    String se=ns;
  
    if(!Str.isNumeric(se) && !se.isEmpty()) {
        if( se.indexOf("SP") >=0 ){ se=se.replace('S',' ').replace('P',' '); nns="SP";}
        else if( se.indexOf("SN") >=0 ){ se=se.replace('S',' ').replace('N',' '); nns="SN";}
        else if( se.indexOf("SA") >=0 ){ se=se.replace('S',' ').replace('A',' '); nns="SA";}
        else if( se.indexOf("S2N") >=0 ){ se=se.replace("S2N"," "); nns="S2N";}
        else if( se.indexOf("S2P") >=0 ){ se=se.replace("S2P"," "); nns="S2P";}
        else if(Character.isLetter(se.charAt(0))) {
            nns=se.substring(0,1);
            se=se.substring(1);
        }else if(Character.isLetter(se.charAt(se.length()-1))) {
            nns=se.substring(se.length()-1);
            se=se.substring(0,se.length()-1);
        }       
    }
    
    se=se.trim();

    if( se.endsWith("+") || se.endsWith("-")) se=se.substring(0,se.length()-1);
  
    ns=se;    

        
    if(ns.contains("E")){
        int exp_digits=0;
        while(ns.indexOf("E")+exp_digits+2<=ns.length()&&isInteger(ns.substring(ns.indexOf("E")+exp_digits+1,ns.indexOf("E")+exp_digits+2))){
            exp_digits++;
            
            //System.out.println("SDS2XDX 167: s1="+s1+" s2="+s2+" ns="+ns+" exp_digits="+exp_digits);
        }
        
        if(exp_digits!=0){
            exps=ns.substring(ns.indexOf("E")+1,ns.indexOf("E")+exp_digits+1);
            if(exps.contains("+")) exps=exps.substring(exps.indexOf("+")+1);
            nexp=Integer.valueOf(exps);
            
            bases=ns.substring(0,ns.indexOf("E")).trim();
            if(  !( bases.equals(null) || bases.equals("") ) ){
                convst c=new convst(bases); 
                basex=c.X(); 
                
              }
        }
    }
    
    ns0=ns;
    if(ns0.contains("E"))
        ns0=ns0.substring(0,ns0.indexOf("E"));
    
    if(ns0.trim().indexOf("+")>0)
        ns0=ns0.substring(0,ns0.indexOf("+"));
    
    ds=ds1;

    if(  !( ns0.equals(null) || ns0.equals("") ) ){
      convst c=new convst(ns0); 
      x=c.X(); bx=c.canConvert();
      
    }
    
    if(  !( ds.equals(null) ) && !( ds.equals("") ) && bx ){

        ds=ds.trim().toUpperCase();

        dsIsDigit=true;

        for( int k=0; k<ds.length(); k++){
          if( ! Character.isDigit( ds.charAt(k) ) ) dsIsDigit=false;
        }

        //System.out.println(" isDigit="+dsIsDigit+" ds="+ds);
        
        /* if Error.length= 2 and non-numeric */

        if( ! dsIsDigit && ds.length() <=2 ){

            if(ds.equals(">"))
                ds="GT";
            else if(ds.equals("|>"))
                ds="GE";
            else if(ds.equals("<"))
                ds="LT";
            else if(ds.equals("|<"))
                ds="LE";
            else if(ds.equals("|?"))
                ds="AP";

            isLimit=Str.isLetters(ds);
            
            //default values for symmetrized: xs=-1; dxs=0;
            if( ds.equals("CA") || ds.equals("AP") || ds.equals("SY")) { 
               //dx=0.1*x;
               dx=-1;
               x_uv_d=x+dx; x_lv_d=x-dx; 
               
               sx=x; dsx=dx;//symmetrized value: xs=x,dxs=0
               
               //bdx=true; bx_uv_d=true; bx_lv_d=true; bxs=true; bdxs=true;
               bdx=false; bx_uv_d=true; bx_lv_d=true; bsx=true; bdsx=true;
            }
            else if( ds.equals("LE") || ds.equals("LT") ) { 
               dx=-1; 
               if(x>0){
                   x_uv_d=x; 
                   x_lv_d=0;     
                   
                   bx_uv_d=true;
                   bx_lv_d=true; 
                   
               }else{
                   x_uv_d=x;
                   x_lv_d=-upLimit;    
                   
                   bx_uv_d=true;
                   bx_lv_d=false; 
               }
               sx=0.5*x_uv_d ; dsx=sx;//symmetrized value
               
               bdx=false; bsx=true; bdsx=true;
            }  
            else if( ds.equals("GE") || ds.equals("GT") ) { 
               dx=-1; 
               if(x>0){
                   x_uv_d=upLimit; 
                   x_lv_d=x;
                   
                   bx_uv_d=false;
                   bx_lv_d=true; 
               }else{
                   x_uv_d=0;
                   x_lv_d=x;
                      
                   bx_uv_d=true;
                   bx_lv_d=true; 
               }
               
               sx=x;dsx=0; //symmetrized value (added 1/12/2022)
               
               bdx=false; bsx=false; bdsx=false;
            }

         }else if ( dsIsDigit ) { /* if Error.length= 2 and numeric : */
              convdelta d=new convdelta(ns0,ds); 
              dx=d.DX(); bdx=d.canConvert();

              if( bdx ){
                 x_uv_d=x+dx; x_lv_d=x-dx; sx=x; dsx=dx;
                 bx_uv_d=true; bx_lv_d=true; bsx=true; bdsx=true;
                 dsu=ds;dsl=ds;
              }
         }else if( ds.length() >= 3 && ! dsIsDigit ){/* if Error.length > 2: */

            /* Looking for + and - signs: */

            int ip=ds.indexOf("+");
            int im=ds.indexOf("-");

            /* Conversion will take place only if both signs are present:  */

            if( ip >=0 || im >= 0 ){
              String sp=""; String sm="";
              if( ip > im ){ sp=ds.substring(ip+1) ; sm=ds.substring(im+1,ip) ;}
              else{sp=ds.substring(ip+1,im) ; sm=ds.substring(im+1); }

              convdelta dp=new convdelta(ns0,sp);
              bx_uv_d=dp.canConvert();
              if(bx_uv_d) x_uv_d=x+dp.DX();

              convdelta dm=new convdelta(ns0,sm);
              bx_lv_d=dm.canConvert();
              if(bx_lv_d) x_lv_d=x-dm.DX();

              //System.out.println("SDS2XDX 464: s="+s+" ds="+ds+"  sm="+sm+" sp="+sp+" uv="+bx_uv_d+" lv="+bx_lv_d+" x_uv_d="+x_uv_d+" x_lv_d="+x_lv_d);

              
              /* Symmetrizing */

              isLimit=true;
              if( bx_lv_d && bx_uv_d ){
            	 if(x_lv_d>0)
            		 isLimit=false;
            	 
                dsu=sp;
                dsl=sm;
                  
                if(!sp.equals(sm))
                    dsIsAsymm=true;
                else {
                    bdx=true;
                    dx=dp.DX();
                    ds=sp;
                }
                
                /*
                //old
                sx=0.5*( x_uv_d+x_lv_d);
                dsx=x_uv_d-sx;
                */

                double[] xdx=symmetrize(x,dp.dx(),dm.dx());
                sx=xdx[0];
                dsx=xdx[1];
              
                bsx=true; bdsx=true;
              }

            }


        }
    }

    //System.out.println(isLimit);
    
    /* null or zero-length ds: */

    if(  ( ds.equals(null) ||  ds.equals("") ) && bx ){

       dx=-1.0;
       
       sx=x;
       dsx=-1.0;
       
       bsx=bx;
       x_lv_d=x;
       x_uv_d=x;
       bx_lv_d=true;
       bx_uv_d=true;
       
       isLimit=false;
       bdx=false;
       bdsx=false;
       
       dsu="";
       dsl="";
       
    }

    double factor=1.0;
    
    for(int i=0;i<nexp;i++){x*=10;factor*=10;}
    for(int i=0;i>nexp;i--){x/=10.0;factor/=10.0;}

    if(dx!=-1)  dx*=factor;
    if(sx!=-1)  sx*=factor;
    if(dsx!=-1) dsx*=factor;
    if(x_lv_d!=-1) x_lv_d*=factor;
    if(x_uv_d!=-1) x_uv_d*=factor;

    
    if(x_lv_d<=x && !dsl.isEmpty()) dxl=x-x_lv_d;
    if(x_uv_d>=x && !dsu.isEmpty()) dxu=x_uv_d-x;
    
    
    //System.out.println(x_lv_d+"  "+x_uv_d+"   "+x+"  "+dxu+"  "+dxl);
    
    s=s1;
    s0=s;
    ds0=ds1;
      
    x0=x;
    dx0=dx;
    dxu0=dxu;
    dxl0=dxl;
    
    real_ds="";
    real_dsu="";
    real_dsl="";
    
	//if string is in scientific format like 1.2E-3, add (-power)
    //into the total number of digits 
	int n=Str.findNDigitsAfterDot(s);
	if(Str.isNumeric(ds)) {
		real_ds=Str.roundToNDigitsAfterDot(String.valueOf(dx), n);
		real_dsu=real_ds;
		real_dsl=real_ds;
	}else{
		 if(Str.isNumeric(dsu)) 	
			 real_dsu=Str.roundToNDigitsAfterDot(String.valueOf(dxu), n);		 
		 if(Str.isNumeric(dsl)) 
			 real_dsl=Str.roundToNDigitsAfterDot(String.valueOf(dxl), n);
		 
		 if(real_dsu.length()>0 && real_dsl.length()>0) {
			 if(real_dsu.equals(real_dsl))
				 real_ds=real_dsu;
			 else
				 real_ds="+"+real_dsu+"-"+real_dsl;
		 }
	}

    
	checkLargeUncAndCrossZero(this);
	
	//System.out.println(s1+"###"+s);
	
    return;

  }
  
  //uncertainty must be string representation of real values, symmetric or asymmetric,
  //like 12.3 +0.03-0.04
  public void setRealValues(String s,String ds) {
	  try {
		  if(!Str.isNumeric(ds) && !ds.contains("+") && !ds.contains("-") ) {
			  setValues(s,ds);
			  return;
		  }
		  boolean isGoodUnc=true;
		  int errorLimit=99;
		  
		  s=s.trim();
		  ds=ds.trim();

		  String dsu="",dsl="";
		  if(ds.contains("+") || ds.contains("-")) {
			  int np=ds.indexOf("+");
			  int nm=ds.indexOf("-");
			  if(np>=0 && nm>=0) {

				  if(np<nm) {
					  dsu=Str.deformatExpo(ds.substring(np+1,nm).trim());
					  dsl=Str.deformatExpo(ds.substring(nm+1).trim());
				  }else {
					  dsl=Str.deformatExpo(ds.substring(nm+1,np).trim());
					  dsu=Str.deformatExpo(ds.substring(np+1).trim());
				  }
			  }else if(np==0) {
				  dsu=ds.substring(1).trim();
			  }else if(nm==0) {
				  dsl=ds.substring(1).trim();
			  }else {
				  isGoodUnc=false;
			  }
		  }else {
			  dsu=ds;
			  dsl=ds;
		  }
		  double dxu=-1,dxl=-1;
		  try {
			  dxu=Double.parseDouble(dsu);
		  }catch(Exception e) {
		  }
		  try {
			  dxl=Double.parseDouble(dsl);
		  }catch(Exception e) {
		  }
		  if(dxu<0 && dxl<0)
			  isGoodUnc=false;

		  if(isGoodUnc) {
			  if(Str.isNumeric(s)) {

				  float tempError1=1000,tempError2=1000;
				  String sd1=Str.getSignificantDigits(dsu);
				  String sd2=Str.getSignificantDigits(dsl);
				  if(Str.isNumeric(sd1))
					  tempError1=Float.parseFloat(sd1);
				  if(Str.isNumeric(sd2))
					  tempError2=Float.parseFloat(sd2);	  
				  
				  tempError1=Math.min(tempError1, tempError2);
				  tempError1=Math.min(errorLimit, tempError1);
				  
				  errorLimit=(int)tempError1;
				  
				  double x=Double.parseDouble(s);
				  XDX2SDS xs=new XDX2SDS(x,dxu,dxl,errorLimit);
				  s=xs.s();
				  ds=xs.ds();
			  }
		  }
		  setValues(s,ds);
	  }catch(Exception e){
		  
	  }
  }
  
  private void checkLargeUncAndCrossZero(SDS2XDX sx) {
		double max=Math.max(sx.dxu, sx.dxl);
		double ax=Math.abs(sx.x);
		
		sx.isLargeUnc=false;
		if(max>ax || (ax>0&&max/ax>SDS2XDX.smallError*0.999999) )
			sx.isLargeUnc=true;
		
		sx.isCrossZero=false;
		if( (sx.x>=0&&(sx.x-sx.dxl)<0) || (sx.x<=0&&(sx.x+sx.dxu)<0) )
			sx.isCrossZero=true;
  }
  
  public boolean isLargeUnc() {return isLargeUnc;}
  public boolean isCrossZero() {return isCrossZero;}
  
  public boolean isAllowMaxMinEP() {return isAllowMaxMinEP;}
  public boolean isUseMCEP() {return isUseMCEP;}
  public boolean isUseNormEP() {return isUseNormEP;}
  
  public void setIsAllowMaxMinEP(boolean b) {
	  isAllowMaxMinEP=b;
	  if(b) {
		  isUseMCEP=false;
		  isUseNormEP=false;
	  }
  }
  public void setIsUseNormEP(boolean b) {
	  isUseNormEP=b;
	  if(b) {
		  isUseMCEP=false;
		  isAllowMaxMinEP=false;
	  }
  }

  public void setIsUseMCEP(boolean b) {
	  isUseMCEP=b;
	  if(b) {
		  isUseNormEP=false;
		  isAllowMaxMinEP=false;
	  }
  }
  
  public void setValues(SDS2XDX s2x) {
      try {
          String s=s2x.s();
          String ds=s2x.ds();
          setValues(s,ds);
          
          setX(s2x.x());//there is case where s and ds is from formatting of real values of x and dx which have higher precision than s
          if(s2x.dx()>0) setDX(s2x.dx());       
          if(s2x.dxu()>0) setDX(s2x.dxu(),s2x.dxl());  
          
          this.x0=s2x.x0;
          this.dx0=s2x.dx0;
          this.dxu0=s2x.dxu0;
          this.dxl0=s2x.dxl0;
 
      }catch(Exception e) {
          
      }
  }
  public void setValues(XDX2SDS x2s){
      try {    

          //System.out.println("SDS2XDX 725: x="+x+" "+dx+" dxu="+dxu+" dxl="+dxl+"  x2s.s="+x2s.s()+" ds="+x2s.ds()
          //  +"\n    x="+x2s.x()+" x2s.dxu="+x2s.dxu()+" dxl="+x2s.dxl()+" this.s="+this.s()+" "+this.ds()
          //  +" x2s.isLimits="+x2s.isLimits()+" this s2x isLimit="+this.isLimit);

          setValues(x2s.s(),x2s.ds());         
          setX(x2s.x());  

          if(x2s.dx()>0) {
        	  setDX(x2s.dx());  
        	  this.x_uv_d=x+dx;
        	  this.x_lv_d=x-dx;
        	  
        	  this.x_uv_f=xf+dxf;
        	  this.x_lv_f=xf-dxf;
          }
          if(x2s.dxu()>=0 && x2s.dxl()>=0) {
        	  setDX(x2s.dxu(),x2s.dxl());
        	  this.x_uv_d=x+dxu;
        	  this.x_lv_d=x-dxl;
        	  
        	  this.x_uv_f=(float) (xf+dxu);
        	  this.x_lv_f=(float) (xf-dxl);
          }

          this.x0=x;
          this.dx0=dx;
          this.dxu0=dxu;
          this.dxl0=dxl;
          
          //System.out.println("SDS2XDX 737: x="+x+" "+dx+" dxu="+dxu+" dxl="+dxl+"  x2s.s="+x2s.s()+" ds="+x2s.ds()
          //  +"\n    x="+x2s.x()+" x2s.dxu="+x2s.dxu()+" dxl="+x2s.dxl()+" this.s="+this.s()+" "+this.ds()
          //  +" x2s.isLimits="+x2s.isLimits()+" this s2x isLimit="+this.isLimit);

          double dx=-1;
          double x=-1;
          try {
        	  x=Double.parseDouble(x2s.s());
          }catch(Exception e) {
        	  
          }
          try {
        	  dx=Double.parseDouble(x2s.ds());
          }catch(Exception e) {
        	  
          }
          //System.out.println("@@@ x="+x+" "+dx+" dxu="+dxu+" dxl="+dxl+"  x2s.s="+x2s.s()+" "+x2s.ds()+" x="+x2s.x()
          //+"\n  x2s.dxu="+x2s.dxu()+" dxl="+x2s.dxl()+" this.s="+this.s()+" "+this.ds()+" x2s.isLimits="+x2s.isLimits());
          
          if(dx>0) {
        	  bdx=true;
          }else if(dxu>=0 && dxl>=0 && (dxu+dxl)>0) {
        	  bdsx=true;
        	  if(dsu.equals(dsl))
        		  bdx=true;
          }
          
          if(x2s.isLimits() && dx>0) {
        	  isLimit=true;

    		  if(dxu>dxl) {
    			  x=x2s.x()-dxl;
    			  if(x2s.x()>0 && x<0)
    				  x=0;
 
    			  if(x!=0) {
        			  setValues(x,-1);
        			  setDS("GT");
                      bdx=false;
    			  }

    		  }else if(dxl>dxu) {
    			  x=x2s.x()+dxu;
    			  if(x2s.x()<0 && x>0)
    				  x=0;
    			  
    			  if(x!=0) {
        			  setValues(x,-1);
        			  setDS("LT");
                      bdx=false;
    			  }

    		  }
          }
          //System.out.println("@@@ x="+x+" "+dx+" dxu="+dxu+" dxl="+dxl+"  x2s.s="+x2s.s()+" "+x2s.ds()+" x2s.dxu="+x2s.dxu()+" dxl="+x2s.dxl()+" this.s="+this.s()+" "+this.ds()+" x2s.isLimits="+x2s.isLimits());
          //System.out.println("s="+s+" ds="+ds);
      }catch(Exception e) {       
      }

  }
  
  public void setValues(double x,double dxu,double dxl){
	  setValues(x,dxu,dxl,errorLimit); 
  }
  public void setValues(double x,double dxu,double dxl,int errorLimit){
      //System.out.println("SDS2XDX 804 x="+x+" "+dx+" dxu="+dxu+" dxl="+dxl+" this.s="+this.s()+" "+this.ds()+" dxu="+this.dxu+" dxl="+this.dxl);

	  XDX2SDS temp=new XDX2SDS();
      
	  temp.setNsig(nsig);
      
      temp.setErrorLimit(errorLimit);
      
      temp.setValues(x, dxu,dxl);
      
      //System.out.println("SDS2XDX 807: XDX2SDS setValue x="+x+" dx="+dx+" dxu="+dxu+" dxl="+dxl+"\n     XDX2SDS   temp.s="
      //   +temp.s()+" ds="+temp.ds()+"  x="+temp.x()+" temp.dxu="+temp.dxu()+" dxl="+temp.dxl()+"  isLimit: "+temp.isLimits());
      
      //System.out.println("## 1 x="+x+" errorLimint="+errorLimit+" temp errorLimint="+temp.getErrorLimit());
      
      //System.out.println(dx+"  "+dxu+"  "+dxl+" "+temp.dx());
      
      this.setErrorLimit(temp.getErrorLimit());//errorLimit of temp could change if isAddExtraDigitForLargeUnc is set to be true, see XDX2SDS setValues()
      
      setValues(temp);    
      
      //System.out.println( isLimits()+" "+this.canGetDX());
      
      //if(x==90)
      //    System.out.println(" x="+x+" "+dx+" dxu="+dxu+" dxl="+dxl+" temp.x="+temp.x()+" temp.s="+temp.s()+" "+temp.ds()+" temp.dxu="+temp.dxu()+" dxl="+temp.dxl()+" this.s="+this.s()+" "+this.ds()+" dxu="+this.dxu+" dxl="+this.dxl);
  }
  
  public void setValues(double x,double dx,int errorLimit){
	  this.dx=dx;
      setValues(x,dx,dx,errorLimit);   
      
      //System.out.println(" x="+x+" "+dx+"  temp.s="+temp.s()+" "+temp.ds()+" this.s="+this.s()+" "+this.ds());
  }

  public void setValues(double x,double dx){
      setValues(x, dx,errorLimit);  
  }
  
  private boolean isInteger(String s){
      if(s.equals("+")||s.equals("-")||s.equals("1")||s.equals("2")||s.equals("3")||
              s.equals("4")||s.equals("5")||s.equals("6")||s.equals("7")||
              s.equals("8")||s.equals("9")||s.equals("0"))
                return true;
      else return false;
  }

    //not used
    public SDS2XDX add_new(SDS2XDX s2x){
        SDS2XDX result=this.add0(s2x);
        
        if(s2x==null || result.ds().equals("NA") || !this.canGetX() || !s2x.canGetX())
            return result;
        
        if(result.canGetDX() || result.dsIsAsymm)
            return result;
        
        //if(this.canGetDX() && s2x.canGetX())
        //  return result;
        
        SDS2XDX sx1=this;
        SDS2XDX sx2=s2x;
        if(!this.canGetDX()){
            sx1=new SDS2XDX(this.s(),"1");
            if(sx1.x()!=0 && Math.abs(sx1.dx()/sx1.x())>0.1)
                sx1.setValues(this.s(), "0");
        }
        if(!s2x.canGetDX()){
            sx2=new SDS2XDX(s2x.s(),"1");
            if(sx2.x()!=0 && Math.abs(sx2.dx()/sx2.x())>0.1)
                sx2.setValues(s2x.s(), "0");
        }
        
        SDS2XDX temp=sx1.add0(sx2);
        
        result.setS(temp.s());
        
        return result;
    }
  
 
    public SDS2XDX add(SDS2XDX s2x,boolean ignoreEmptyUnc){
    	if(ignoreEmptyUnc && s2x.ds.isEmpty()) {
    		return add(s2x.s);
    	}else {
    		return add(s2x);
    	}
    }
    
    public SDS2XDX add(SDS2XDX s2x){
        SDS2XDX result=this.add0(s2x);
         
        //System.out.println(result.s+"  "+result.ds);
        
        checkLargeUncAndCrossZero(result);
        
        if(s2x==null || s2x.s().isEmpty() || this.s().isEmpty() || result.ds().equals("NA") || !this.canGetX() || !s2x.canGetX())
            return result;

        //System.out.println("In SDS2XDX line 556: result.canGetDXS="+result.canGetDXS()+"  result.canGetDX="+
        //result.canGetDX()+" result x="+result.x+" dx="+result.dx+" dxu="+result.dxu+" dxl="+result.dxl+" xu="+result.xUV()+" xl="+result.xLV()+" s="+result.s
        //+" ds="+result.ds+" dsu="+result.dsu+" dsl="+result.dsl+" isAsym="+result.dsIsAsymm+" this s="+this.s+" ds="+this.ds+" s2x s="+s2x.s+" ds="+s2x.ds);
        
        if(result.canGetDX() || result.dsIsAsymm())
            return result;
               
        SDS2XDX sx1=this;
        SDS2XDX sx2=s2x;
        
        if((!this.canGetDX()||this.dx==0) && !this.dsIsAsymm){
            sx1=new SDS2XDX(this.s(),"5");
            if(sx1.x()==0 || (sx1.x()!=0 && Math.abs(sx1.dx()/sx1.x())>0.1))
                sx1.setValues(this.s(), "0");
            
            //System.out.println("SDS2XDX 583:this s="+this.s+" ds="+this.ds+" sx1="+sx1.s+" ds="+sx1.ds);
        }
        if((!s2x.canGetDX()||s2x.dx==0) && !s2x.dsIsAsymm){
            sx2=new SDS2XDX(s2x.s(),"5");
            
            //System.out.println(sx2.s+"  "+sx2.ds);
            
            if(sx2.x()==0 || (sx2.x()!=0 && Math.abs(sx2.dx()/sx2.x())>0.1))
                sx2.setValues(s2x.s(), "0");
            
            //System.out.println(sx2.s+"  "+sx2.ds);
        }  
        
        SDS2XDX temp=sx1.add0(sx2);
        
        String ds=result.ds();
        if(ds.startsWith("G") || ds.startsWith("L")) {//result.s might be a different value from temp.s here
            double x=Double.parseDouble(result.s());
            XDX2SDS xs=new XDX2SDS(x,temp.dx);
            
            int nsig1=Str.findNSig(result.s());
            int nsig2=Str.findNSig(xs.s());
            //System.out.println(" result S="+result.s+" nsig1="+nsig1+"  xs s="+xs.s()+" ds="+xs.ds()+" nsig2="+nsig2);
            
            if(nsig2<nsig1)
            	result.setS(xs.s());
            
            //System.out.println("In SDS2XDX line 584: result.canGetDXS="+result.canGetDXS()+"  result.canGetDX="+result.canGetDX()+" result x="+result.x+" "+result.dx+" result s="+result.s+" "+result.ds);

        }else {
            int nsig1=Str.findNSig(result.s());
            int nsig2=Str.findNSig(temp.s());
            
            if(nsig2<nsig1)
                result.setS(temp.s());
        }
        //System.out.println("original: this="+this.s+" ds="+this.ds+" s2="+s2x.s+" ds="+s2x.ds+" after adjustment: sx1="+sx1.s+" ds="+sx1.ds+" sx2="+sx2.s+" ds="+sx2.ds+" result="+result.s+" "+result.ds);
       
        return result;
    }

    private SDS2XDX dividedS2XNonNumericalUnc(SDS2XDX s2x,double x1,double dx1,int maxNsig) {
        return dividedS2XNonNumericalUnc(s2x,x1,dx1,dx1,maxNsig);
    }
    
	private SDS2XDX dividedS2XNonNumericalUnc(SDS2XDX s2x,double x1,double dxu1,double dxl1,int maxNsig) {
        String ds2=s2x.ds;
        double x2=s2x.x;
        
        if(Str.isNumeric(ds2)|| (ds2.contains("+")) || x1==0)
            return null;
        
        double x=x2/x1;
        
        double minX=0,maxX=0,limitX=0;
               
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
        
        boolean isX1CrossZero=false;
        
        if(dxu1>=0 && dxl1>=0) {
        	isX1CrossZero=((x1+dxu1)*(x1-dxl1)<=0);
        }else {
            if(dxu1<0)
                dxu1=0.2*Math.abs(x1);
            if(dxl1<0)
                dxl1=0.2*Math.abs(x1);
        }

          
        SDS2XDX result=new SDS2XDX();
        XDX2SDS temp=new XDX2SDS(); 
        temp.setNsig(maxNsig);
        
        double dxu2=0,dxl2=0;
        String ds=ds2;
        if(ds2.isEmpty()) {
        	dxu2=0.2*Math.abs(x2);//assumed 20% unc for empty unc
        	dxl2=dxu2;
        	if(s2x.dx0>=0) {
        		dxu2=dxl2=dx0;
        	}else {
        		if(s2x.dxu0>=0) dxu2=s2x.dxu0;
        		if(s2x.dxl0>=0) dxl2=s2x.dxl0;
        	}

        }else if(ds2.startsWith("L")) {
        	if(x2>0) {
            	dxu2=0.2*x2;
            	dxl2=0.2*x2; 
            	
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1+dxu1);
            		maxX=x2/(x1-dxl1);
            	}
        	}else {
        		dxu2=-0.2*x2;
        		dxl2=-0.2*x2;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1-dxl1);
            		maxX=x2/(x1+dxu1);
            	}
        	}	
        	
        	if(x1<0) {
        		ds="G"+ds2.substring(1);
        		limitX=minX;
        	}else {
        		limitX=maxX;
        	}
        }else if(ds2.startsWith("G")) {
        	if(x2>0) {
                dxu2=0.2*x2;
                dxl2=0.2*x2;
                
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1+dxu1);
            		maxX=x2/(x1-dxl1);
            	}
        	}else {
        		dxu2=-0.2*x2;
        		dxl2=-0.2*x2;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=x2/(x1-dxl1);
            		maxX=x2/(x1+dxu1);
            	}
        	}
        	if(x1<0) {
        		ds="L"+ds2.substring(1);
        		limitX=maxX;
        	}else {
        		limitX=minX;
        	}
        }else {
        	dxu2=0.5*Math.abs(x2);
        	dxl2=dxu2;
        }
        
        double absX=Math.abs(x);
        
        // result is x2/x1
    	if(x1>=0 && x2>=0) {
            dxl1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
            dxu1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
    	}else if(x1<0 && x2<0) {
            dxu1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
            dxl1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
    	}else if(x1>0) {
    		//x2<0
            dxl1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
            dxu1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
    	}else if(x2>0) {
    		//x1<0
            dxl1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
            dxu1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
    	}else {
            dxl1=-1;
            dxu1=-1;
    	}

    	int errorLimit=99;
    	if(Math.abs(x)<1000 && (dxu1>99||dxl1>99)) {
    		errorLimit=999;
    	}
    	temp.setErrorLimit(errorLimit);
    	
        temp.setValues(x,dxu1,dxl1);
        if(temp.s().contains("E") && Math.abs(x)<SDS2XDX.expFormatLimitLargeNumber && Math.abs(x)>1) {
        	temp.setValues(x,0,0);
        }
        
        String tempS="";
        if(limitX!=0) {//LT or GT values
        	x=limitX;
        	tempS=Str.roundByNsig(String.valueOf(x), maxNsig); 
        	
        }else {
            tempS=Str.roundByNsig(temp.s(), maxNsig); 	
        }
        
        result.setValues(tempS,ds);
        result.setX(x);

        
        //store numerical uncertainties from assumed numerical uncertainties for non-numerical input uncertainties
        result.dx0=temp.dx();
        result.dxu0=temp.dxu();
        result.dxl0=temp.dxl();
        
        //System.out.println("SDS2XDX 661: errorLimit="+errorLimit+" x1="+x1+" dx1="+dx1+"  s2="+s2x.s+" ds2="+ds2+" result.x="+result.x+" dx="+result.dxu+"  "+result.dxl
        //		+" result.s="+result.s+" ds="+result.ds+" temp.S="+temp.s());
        
        return result;
    }
	
    private SDS2XDX dividedS2XNonNumericalUnc(double x1,double dx1,SDS2XDX s2x,int maxNsig) {
        return dividedS2XNonNumericalUnc(x1,dx1,dx1,s2x,maxNsig);
    }
    
	private SDS2XDX dividedS2XNonNumericalUnc(double x1,double dxu1,double dxl1,SDS2XDX s2x,int maxNsig) {
        String ds2=s2x.ds;
        double x2=s2x.x;
        
        if(Str.isNumeric(ds2)|| (ds2.contains("+")) || x2==0)
            return null;
        
        double x=x1/x2;
        
        double minX=0,maxX=0,limitX=0;
        
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
        
        boolean isX1CrossZero=false;
        
        if(dxu1>=0 && dxl1>=0) {
        	isX1CrossZero=((x1+dxu1)*(x1-dxl1)<=0);
        }else {
            if(dxu1<0)
                dxu1=0.01*Math.abs(x1);
            if(dxl1<0)
                dxl1=0.01*Math.abs(x1);
        }

          
        SDS2XDX result=new SDS2XDX();
        XDX2SDS temp=new XDX2SDS(); 
        temp.setNsig(maxNsig);
        
        double dxu2=0,dxl2=0;
        String ds=ds2;
        if(ds2.isEmpty()) {
        	dxu2=0.01*Math.abs(x2); 
        	dxl2=dxu2;
        	if(s2x.dx0>=0) {
        		dxu2=dxl2=dx0;
        	}else {
        		if(s2x.dxu0>=0) dxu2=s2x.dxu0;
        		if(s2x.dxl0>=0) dxl2=s2x.dxl0;
        	}
        	
        	//System.out.println(x2+"  "+dxu2);
        	
        }else if(ds2.startsWith("L")) {
        	if(x2>0) {
            	dxu2=0.2*x2;
            	dxl2=0.2*x2; 
            	
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		maxX=(x1+dxu1)/x2;
            		minX=(x1-dxl1)/x2;
            	}
        	}else {
        		dxu2=-0.2*x2;
        		dxl2=-0.2*x2;
        		
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=(x1+dxu1)/x2;
            		maxX=(x1-dxl1)/x2;
            	}
        	}	
        	
        	if(x1>0) {
        		ds="G"+ds2.substring(1);
        		limitX=minX;
        	}else {
        		limitX=maxX;
        	}
        	
        }else if(ds2.startsWith("G")) {
        	if(x2>0) {
                dxu2=0.2*x2;
                dxl2=0.2*x2;
                
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		maxX=(x1+dxu1)/x2;
            		minX=(x1-dxl1)/x2;
            	}
        	}else {
        		dxu2=-0.2*x2;
        		dxl2=-0.2*x2;
        		
            	if(dxu1Orig>=0 && dxl1Orig>=0 && !isX1CrossZero) {
            		minX=(x1+dxu1)/x2;
            		maxX=(x1-dxl1)/x2;
            	}
        	}
        	if(x1>0) {
        		ds="L"+ds2.substring(1);
        		limitX=maxX;
        	}else {
        		limitX=minX;
        	}
        }else {
        	dxu2=0.05*Math.abs(x2);
        	dxl2=dxu2;
        }
        
        double absX=Math.abs(x);
        //result is x1/x2
    	if(x1>0 && x2>0) {
            dxu1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
            dxl1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
    	}else if(x1<0 && x2<0) {
            dxl1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
            dxu1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
    	}else if(x1>0) {
    		//x2<0
            dxu1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
            dxl1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
    	}else if(x2>0) {
    		//x1<0
            dxu1=Math.sqrt(dxu1*dxu1/(x1*x1)+dxu2*dxu2/(x2*x2))*absX;
            dxl1=Math.sqrt(dxl1*dxl1/(x1*x1)+dxl2*dxl2/(x2*x2))*absX;
    	}else {
            dxu1=-1;
            dxl1=-1;
    	}


    	int tempLimit=this.errorLimit;
    	/*
    	if(Math.abs(x)<1000 && (dxu1>tempLimit||dxl1>tempLimit)) {
    		tempLimit=999;
    	}
    	*/
    	temp.setErrorLimit(tempLimit);
    	
        temp.setValues(x,dxu1,dxl1);
        if(temp.s().contains("E") && Math.abs(x)<SDS2XDX.expFormatLimitLargeNumber && Math.abs(x)>1) {
        	temp.setValues(x,0,0);
        }
        
        String tempS="";
        if(limitX!=0) {//LT or GT values
        	x=limitX;
        	tempS=Str.roundByNsig(String.valueOf(x), maxNsig); 
            //System.out.println(x+"  "+maxNsig+"  "+tempS);
        }else {
            tempS=Str.roundByNsig(temp.s(), maxNsig); 	
        }
        
        result.setValues(tempS,ds);
        result.setX(x);
        
        //store numerical uncertainties from assumed numerical uncertainties for non-numerical input uncertainties
        result.dx0=temp.dx();
        result.dxu0=temp.dxu();
        result.dxl0=temp.dxl();
        
        //System.out.println("SDS2XDX 661: errorLimit="+errorLimit+" x="+x+" dxu1="+dxu1+" dxl1="+dxl1+" x1="+x1+" dx1="+dxu1+"  s2="+s2x.s+" ds2="+ds2+" result.x="+result.x+" dx="+result.dxu+"  "+result.dxl
        //		+" result.s="+result.s+" ds="+result.ds+" temp.S="+temp.s()+" maxNsig="+maxNsig);
        
        return result;
    }
    
    
    private SDS2XDX multiplyS2XNonNumericalUnc(double x1,double dx1,SDS2XDX s2x,int maxNsig) {
        return multiplyS2XNonNumericalUnc(x1,dx1,dx1,s2x,maxNsig);
    }
    
	private SDS2XDX multiplyS2XNonNumericalUnc(double x1,double dxu1,double dxl1,SDS2XDX s2x,int maxNsig) {
		String s2=s2x.s;
        String ds2=s2x.ds;
        double x2=s2x.x;
        
        //System.out.println(x1+"  "+dxu1+"  "+dxl1+"   s2x="+s2x.s+" ds="+s2x.ds+" msgNsig="+maxNsig);
        
        if(Str.isNumeric(ds2)|| (ds2.contains("+")))
            return null;
        
        double x=x1*x2;
        
        boolean isGoodDX1=true;
        
        double minX=0,maxX=0,limitX=0;
        
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
                
        String s1=String.valueOf(x1);
        int nd=0;
        if(s1.contains(".")) {
        	nd=Str.findNDigitsAfterDot(s1);
        	if(nd>=3)
        		nd=3;
        }
        if(dxu1<0) {
            dxu1=0.01*Math.abs(x1);
            dxu1=Math.min(dxu1,Math.pow(10, -nd)*5.0);
            isGoodDX1=false;
        }
        if(dxl1<0) {
            dxl1=0.01*Math.abs(x1);
            dxl1=Math.min(dxl1,Math.pow(10, -nd)*5.0);
            isGoodDX1=false;
        }
        
        SDS2XDX result=new SDS2XDX();
        XDX2SDS temp=new XDX2SDS(); 
        temp.setNsig(maxNsig);
        
        double dxu2=0,dxl2=0;
        String ds=ds2;
        boolean resetDS=false;
        
        
        //System.out.println("SDS2XDX 1329: nd="+nd+" s2="+s2+"  ds2="+ds2);
        
        if(ds2.isEmpty()) {

        	dxu2=0.01*Math.abs(x2); 
        	
        	nd=Str.findNDigitsAfterDot(s2);
        	double tempDX2=-1;
        	if(nd>0) {
        		tempDX2=Math.pow(10, -nd)*5.0;
        		dxu2=Math.min(dxu2, tempDX2);
        		double tempX2=Math.abs(x2);
        		if(tempX2>0 && tempDX2/tempX2<=0.01 && isGoodDX1) {
        			//resetDS=true;
        		}
        	}
        	
        	//System.out.println("  1  "+x1+"  "+dxu1+"  "+dxl1+"   s2x="+s2x.s+" ds="+s2x.ds+" msgNsig="+maxNsig+"  dxu2="+dxu2+" dxl2="+dxl2+" nd="+nd+" s2="+s2+" dxu2="+dxu2);

        	
        	dxl2=dxu2;
        	if(s2x.dx0>=0) {
        		dxu2=dxl2=s2x.dx0;
        	}else {
        		if(s2x.dxu0>=0) dxu2=s2x.dxu0;
        		if(s2x.dxl0>=0) dxl2=s2x.dxl0;
        	}
        	
        	//System.out.println("  2 "+x1+"  "+dxu1+"  "+dxl1+"   s2x="+s2x.s+" ds="+s2x.ds+" msgNsig="+maxNsig+"  dxu2="+dxu2+" dxl2="+dxl2+" nd="+nd+" s2="+s2+" dxu2="+dxu2);
        }else if(ds2.startsWith("L")) {
        	if(x2>0) {
            	dxu2=0.2*x2;
            	dxl2=0.2*x2; 
            	
            	if(dxu1Orig>=0 && dxl1Orig>=0) {
            		minX=(x1-dxl1)*x2;
            		maxX=(x1+dxu1)*x2;
            	}
        	}else {
        		dxu2=-0.2*x2;
        		dxl2=-0.2*x2;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0) {
            		maxX=(x1-dxl1)*x2;
            		minX=(x1+dxu1)*x2;
            	}
        	}	
        	
        	if(x1<0) {
        		ds="G"+ds2.substring(1);
        		limitX=minX;
        	}else {
        		limitX=maxX;
        	}
        }else if(ds2.startsWith("G")) {
        	if(x2>0) {
                dxu2=0.2*x2;
                dxl2=0.2*x2;
                
            	if(dxu1Orig>=0 && dxl1Orig>=0) {
            		minX=(x1-dxl1)*x2;
            		maxX=(x1+dxu1)*x2;
            	}
        	}else {
        		dxu2=-0.2*x2;
        		dxl2=-0.2*x2;
        		
            	if(x2<0 && dxu1Orig>=0 && dxl1Orig>=0) {
            		maxX=(x1-dxl1)*x2;
            		minX=(x1+dxu1)*x2;
            	}
        	}
        	if(x1<0) {
        		ds="L"+ds2.substring(1);
        		limitX=maxX;
        	}else {
        		limitX=minX;
        	}
        }else {
        	dxu2=0.05*Math.abs(x2);
        	dxl2=dxu2;
        }
        
        //System.out.println(x1+"  "+dxu1+"  "+dxl1+" x2="+x2+" "+dxu2+"  "+dxl2);
        
    	if(x1>=0 && x2>=0) {
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
    	}else if(x1<0 && x2<0) {
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
    	}else if(x1>0) {
    		//x2<0
            dxl1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxu1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else if(x2>0) {
    		//x1<0
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxl2*dxl2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxu2*dxu2);
    	}else {
            dxu1=Math.sqrt(dxu1*dxu1*x2*x2+x1*x1*dxu2*dxu2);
            dxl1=Math.sqrt(dxl1*dxl1*x2*x2+x1*x1*dxl2*dxl2);
    	}

    	int tempLimit=this.errorLimit;
    	/*
    	if(Math.abs(x)<1000 && (dxu1>tempLimit||dxl1>tempLimit)) {
    		tempLimit=999;
    	}
    	*/
    	temp.setErrorLimit(tempLimit);
        temp.setValues(x,dxu1,dxl1);

        //System.out.println("###"+x+"  "+dxu1+"  "+dxl1+"  "+tempLimit+"  "+temp.s()+" "+SDS2XDX.expFormatLimitLargeNumber+"   "+resetDS);
        
        if(temp.s().contains("E") && Math.abs(x)<SDS2XDX.expFormatLimitLargeNumber && Math.abs(x)>1) {
        	temp.setValues(x,0,0);
        }
        //System.out.println(temp.s());
        
        if(resetDS) {
        	//System.out.println(temp.s()+"  "+temp.ds()+" "+tempLimit);
        	result.setValues(temp.s(),temp.ds());
        	result.setX(x);
        	result.setDX(dxu1,dxl1);
        }else {            
            String tempS="";
            if(limitX!=0) {//LT or GT values
            	x=limitX;
            	tempS=Str.roundByNsig(String.valueOf(x), maxNsig);            	
            }else {
            	//System.out.println("####"+temp.s());
                tempS=Str.roundByNsig(temp.s(), maxNsig); 	
            }
            
            result.setValues(tempS,ds);
            result.setX(x);
            
            //System.out.println(" x="+x+" dxu1="+dxu1+" dxl1="+dxl1+" temp.s="+temp.s()+" "+tempS +" ds="+ds+" maxNsig="+maxNsig+" "+limitX);
            //System.out.println(" String.valueOf(x)="+String.valueOf(x)+"  result.s="+result.s);
        }
        
        //store numerical uncertainties from assumed numerical uncertainties for non-numerical input uncertainties
        result.dx0=temp.dx();
        result.dxu0=temp.dxu();
        result.dxl0=temp.dxl();
        
        //System.out.println("SDS2XDX 1123: errorLimit="+errorLimit+" x="+x+" x1="+x1+" dxu1="+dxu1+" dxl1="+dxl1+"  s2="+s2x.s+" ds2="+ds2+" result.x="+result.x+" dx="+result.dxu+"  "+result.dxl
        //		+"\n result.s="+result.s+" ds="+result.ds+" temp.S="+temp.s()+"  "+temp.ds()+" maxNsig="+maxNsig);
        //System.out.println(Str.roundByNsig("1000",3));
        //System.out.println(Str.toSciFormat("-0.000245331",3));
        
        return result;
    }
    
    
    /*
     * s2x has non-numerical uncertainty: LT,GT,AP,SY,CA,empty
     */
    private SDS2XDX addS2XNonNumericalUnc(double x1,double dx1,SDS2XDX s2x) {
        return addS2XNonNumericalUnc(x1,dx1,dx1,s2x);
    }
    
    private SDS2XDX addS2XNonNumericalUnc(double x1,double dxu1,double dxl1,SDS2XDX s2x) {
    	String s2=s2x.s;
        String ds2=s2x.ds;
        double x2=s2x.x;
        
        if(Str.isNumeric(ds2)|| (ds2.contains("+")))
            return null;
        
        double x=x1+x2;
        
        String s1=String.valueOf(x1);
        int nd=0;
        if(s1.contains(".")) {
        	nd=Str.findNDigitsAfterDot(s1);
        	if(nd>=3)
        		nd=3;
        }
        
        double dxu1Orig=dxu1;
        double dxl1Orig=dxl1;
        
        double limitX=0;
        
        boolean isGoodDX1=true;
        if(dxu1<0) {
            dxu1=0.01*Math.abs(x1);
            dxu1=Math.min(dxu1,Math.pow(10, -nd)*5.0);
            isGoodDX1=false;
        }
        if(dxl1<0) {
            dxl1=0.01*Math.abs(x1);
            dxl1=Math.min(dxl1,Math.pow(10, -nd)*5.0);
            isGoodDX1=false;
        }
        
        double dx1=Math.min(dxu1, dxl1); 
        
        x1=Math.abs(x1);
        x2=Math.abs(x2);
        
        SDS2XDX result=new SDS2XDX();
        SDS2XDX temp=new SDS2XDX();
        
        //System.out.println("SDS2XDX 1179: x="+x+" x1="+x1+" dxu1="+dxu1+" dxl1="+dxl1+" dx1="+dx1+" x2="+x2+" ds2="+ds2);
        int tempLimit=this.errorLimit; 
        boolean isGoodDX2=false;
        if( (dxu1+dxl1)>0 &&  (dx1>x2 || x1>=10*x2 || (!ds2.startsWith("G")&&x1>=5*x2)) ){       
        	//System.out.println("SDS2XDX 650: x="+x+" dxu1="+dxu1+" dxl1="+dxl1+" sx2.dx0="+s2x.dx0);
        	
        	isGoodDX2=true;
            if(ds2.isEmpty()) {
            	double dxu2=0.01*Math.abs(x2); 
            	double dxl2=dxu2;
            	
            	nd=Str.findNDigitsAfterDot(s2);
            	double tempDX2=-1;
            	
           	           	
            	if(s2x.dx0>=0) {
            		dxu2=dxl2=s2x.dx0;            		
            	}else if(s2x.dxu0>=0 && s2x.dxl0>=0) {
            		dxu2=s2x.dxu0;
            		dxl2=s2x.dxl0;
            	}else if(nd>=0) {
            		tempDX2=Math.pow(10, 1-nd)*5.0;
            		dxu2=Math.min(dxu2, tempDX2);
            		dxl2=dxu2;

            		if( x2>0 && (dxu2/x2>=0.01 || dxu2>0.5*dxu1 || dxu2>0.5*dxl1) ){
            			isGoodDX2=false;
            			dxu2=Math.pow(10, -nd)*0.51;
                		dxl2=dxu2;
                		tempLimit=5;
            		}
            		//System.out.println(nd+"   tempDX2="+tempDX2+" dxu2="+dxu2);
            	}
            	
            	
                dxu1=Math.sqrt(dxu1*dxu1+dxu2*dxu2);
                dxl1=Math.sqrt(dxl1*dxl1+dxl2*dxl2);
                
                //System.out.println(" x="+x+" dxu1="+dxu1+" dxl1="+dxl1);
            }else if(ds2.startsWith("L")) {
                //dxu1=dxu1;
                dxl1=Math.sqrt(dxl1*dxl1+x2*x2);
            }else if(ds2.startsWith("G")) {
                dxu1=Math.sqrt(dxu1*dxu1+100*x2*x2);
                //dxl1=dxl1;
            }else {
                dxu1=Math.sqrt(dxu1*dxu1+0.5*x2*x2);
                dxl1=Math.sqrt(dxl1*dxl1+0.5*x2*x2);
            }
            
            result.setErrorLimit(tempLimit);
            result.setValues(x,dxu1,dxl1);
            
            //System.out.println("SDS2XDX 1319: x="+x+" dxu1="+dxu1+" dxl1="+dxl1+" "+result.s+"  "+tempLimit);
            
            if(!isGoodDX1 || !isGoodDX2) {
            	result.setDX(-1, -1);
            	result.setDS("");
            }
            
        }else {
        	//System.out.println("SDS2XDX 667: x="+x+" dxu1="+dxu1+" dxl1="+dxl1);
        	
            if(ds2.startsWith("L")) {
                x=x+dxu1;//upper limit with assumed uncertainty if existing
                
            	if(dxu1Orig>=0) {
            		limitX=x+dxu1Orig;
            	} 
               
            }else if(ds2.startsWith("G")) {
                x=x-dxl1;//lower limit 
                
                if(dxl1Orig>=0) {
                	limitX=x-dxl1Orig;
                }
            }
            
            double d=this.errorLimit/99.0;

            if(s2x.s().contains(".")) {
                d=Math.max(0.1, d);
            	dx1=Math.max(dx1, d);
            }else {
                d=Math.max(1.0, d);
            	dx1=Math.max(dx1, 1.0);
            }
            
            
        	temp.setErrorLimit(tempLimit);
        	
            temp.setValues(x,dxu1,dxl1);
            
            int nd1=Str.findNDigitsAfterDot(temp.s);
            int nd2=Str.findNDigitsAfterDot(s2);
            nd=Math.min(nd1, nd2);
            if(nd>=0) {
            	dx1=Math.pow(10, -nd)*0.51;
            	tempLimit=5;
            }
                	
            
        	/*
        	if(Math.abs(x)<1000 && (dxu1>tempLimit||dxl1>tempLimit)) {
        		tempLimit=999;
        	}
        	*/

            temp.setErrorLimit(tempLimit);
            temp.setValues(x,dx1);
            
            String tempS="";
            if(limitX!=0) {//LT or GT values
            	x=limitX;
            	
            	SDS2XDX temp1=temp.clone();
                temp1.setValues(x,dx1);
            	tempS=temp1.s();
            	
            }else {
                tempS=temp.s();
            }
            
            result.setValues(tempS,ds2);
            result.setX(x);
            
            //store numerical uncertainties from assumed numerical uncertainties for non-numerical input uncertainties
            result.dx0=temp.dx;
            result.dxu0=temp.dxu;
            result.dxl0=temp.dxl;
        }
        //System.out.println("SDS2XDX 661: errorLimit="+errorLimit+" x1="+x1+" dx1="+dx1+"  s2="+s2x.s+" ds2="+ds2+" result.x="+result.x+" dx="+result.dxu+"  "+result.dxl
        //		+" result.s="+result.s+" ds="+result.ds+" temp.S="+temp.s());
        
        return result;
    }
    
    //Jun Chen 10/19/2015
    public SDS2XDX add0(SDS2XDX s2x){
        
        SDS2XDX result=new SDS2XDX();
        XDX2SDS temp=new XDX2SDS();
        
        result.setValues(this);
        result.setErrorLimit(errorLimit);

        if(s2x==null || s2x.s().isEmpty() || this.s().isEmpty())
            return result;
        
    	if(s2x.toString().startsWith("-")) {
    		result.strRepr="["+this.toString()+s2x.toString()+"]";
    	}else {
            result.strRepr="["+this.toString()+"+"+s2x.toString()+"]";    		
    	}

		//System.out.println("SDS2XDX 830@@@@##"+this.toString()+"$$  s2x="+s2x+"  result="+strRepr);
    	
        double x=-1,dx=-1,dxu=-1,dxl=-1;
        
        boolean isPostNNS=true;//1234.5+X, non-numerical string (NNS) at the end
        
        //debug
        //System.out.println(" In SDS2XDX: line 373: this="+s+"*"+ds+" s2x="+s2x.s+"*"+s2x.ds+" x="+this.x+" s2x.x="+s2x.x+" can getDX s1="+this.canGetDX()+" s2="+s2x.canGetDX()+" add result="+result.canGetDX());
        //System.out.println("   canGetX: this "+this.canGetX()+"  s2 "+s2x.canGetX());
        
        String nns0="";
        if(this.canGetX()&&s2x.canGetX()){
   
            if(this.nns.length()>0 && s2x.nns.length()>0) {
                System.out.println("Can't add:"+id+" "+this.S()+" "+this.DS()+" with "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
                result.setValues(this.s(),"NA");
                return result;              
            }
            
            nns0=this.nns+s2x.nns;
            if(nns0.length()>0) {
            	String s1=this.s;
            	String s2=s2x.s;
            	
            	if(s1.contains("-"+nns0) || s2.contains("-"+nns0)) {
            		nns0="-"+nns0;
            	}else if(s1.contains("+"+nns0) || s2.contains("+"+nns0)) {
            		nns0="+"+nns0;
            	}else if(s1.contains(nns0+"-") || s2.contains(nns0+"-")) {
            		nns0=nns0+"-";
            		isPostNNS=false;
            	}else {
            		nns0=nns0+"+";
            		isPostNNS=false;
            	}
            }
            
            x=this.x+s2x.x();

            //System.out.println(x);
            
            if(this.canGetDX()&&s2x.canGetDX()){
                dx=Math.sqrt(this.dx*this.dx+s2x.dx()*s2x.dx());
                result.setValues(x, dx);

                //System.out.println(" In SDS2XDX 717: this="+s+"*"+ds+" s2x="+s2x.s+"*"+s2x.ds+" x="+this.x+" s2x.x="+s2x.x+" can getDX s1="+this.canGetDX()+" s2="+s2x.canGetDX()+" add result="+result.canGetDX());
                //System.out.println("   result x="+result.x+" dx="+result.dx+" s="+result.s+" ds="+result.ds);
                
                //temp.setValues(x, dx);
                //result.setValues(temp.s(), temp.ds());
            }else if(this.canGetDX()){
                
                if(s2x.dsIsAsymm) {
                    dxu=Math.sqrt(this.dx*this.dx+s2x.dxu()*s2x.dxu());
                    dxl=Math.sqrt(this.dx*this.dx+s2x.dxl()*s2x.dxl());                    
                  
                    //will be process further below
                }else {                 
                    //s2x.ds: LT,GT,SY,CA,AP,empty,or negative value
                    SDS2XDX tempResult=addS2XNonNumericalUnc(this.x, this.dx,s2x);
                    result.setValues(tempResult);
                } 
                //System.out.println(" In SDS2XDX 728: this="+s+"*"+ds+" s2x="+s2x.s+"*"+s2x.ds+" x="+this.x+" s2x.x="+s2x.x+" can getDX s1="+this.canGetDX()+" s2="+s2x.canGetDX()+" add result="+result.canGetDX());
                //System.out.println("   result x="+result.x+" dx="+result.dx+" s="+result.s+" ds="+result.ds);
            }else if(s2x.canGetDX()){             

                if(this.dsIsAsymm) {
                    dxu=Math.sqrt(this.dxu*this.dxu+s2x.dx()*s2x.dx());
                    dxl=Math.sqrt(this.dxl*this.dxl+s2x.dx()*s2x.dx());
                  
                    //will be process further below
                }else {               
                    //this.ds: LT,GT,SY,CA,AP,empty, or negative value
                    SDS2XDX tempResult=addS2XNonNumericalUnc(s2x.x,s2x.dx,this);
                    result.setValues(tempResult);
                    
                    //System.out.println(" In SDS2XDX 749: this="+s+"*"+ds+" s2x="+s2x.s+"*"+s2x.ds+" x="+this.x+" s2x.x="+s2x.x+" can getDX s1="+this.canGetDX()+" s2="+s2x.canGetDX()+" add result="+result.canGetDX());
                    //System.out.println("   result x="+result.x+" dx="+result.dx+" s="+result.s+" ds="+result.ds);
                    //System.out.println("   tempResult: s="+tempResult.s+" ds="+tempResult.ds);
                } 
                
                //temp.setValues(x,0);
                //result.setValues(temp.s(), this.ds()); 
            }else if(s2x.dsIsAsymm && this.dsIsAsymm){
                dxu=Math.sqrt(this.dxu*this.dxu+s2x.dxu()*s2x.dxu());
                dxl=Math.sqrt(this.dxl*this.dxl+s2x.dxl()*s2x.dxl());
                
                //will be process further below
            }else if(this.dsIsAsymm()) {//s2x has non-numerical unc             
                SDS2XDX tempResult=addS2XNonNumericalUnc(this.x,this.dxu,this.dxl,s2x);
                result.setValues(tempResult);   
            }else if(s2x.dsIsAsymm()) {//this has non-numerical unc                
                SDS2XDX tempResult=addS2XNonNumericalUnc(s2x.x,s2x.dxu,s2x.dxl,this);
                result.setValues(tempResult);  
            }else{
            	
                //temp.setValues(x, 0);
            	//String tempS=temp.s();
            	
                SDS2XDX tempResult=this.addS2XNonNumericalUnc(this.x, -1, -1,s2x);
                String tempS=tempResult.s;
                
                //System.out.println("SDS2XDX 1361: x="+x+" "+ temp.s()+" "+temp.ds()+" "+tempResult.s+" "+tempResult.ds);
                
                //LT,LE,GT,GE,AP,SY,empty
                
                String ds1=this.ds().trim();
                String ds2=s2x.ds().trim();
                String s0="";
                if(ds1.length()>0 && ds2.length()>0) {
                    s0=ds1.charAt(0)+ds2.charAt(0)+"";
                    if(s0.equals("LG") || s0.equals("GL")){//assuming two numbers are positive
                        result.setValues(tempS,"NA");
                        
                        if(isPostNNS)
                        	result.setS(result.s+nns0);
                        else
                        	result.setS(nns0+result.s);
                        
                        result.setX(x);
                        
                        return result;
                    }
                }else if(ds1.length()==0) {
                	//use tempResult
                	result.setValues(tempResult);
                	return result;
                }else if(ds2.length()==0) {
                	tempResult=this.addS2XNonNumericalUnc(s2x.x, -1, -1,this);
                	result.setValues(tempResult);
                	return result;
                }
                
                s0=ds1+" "+ds2;
                if(ds1.equals(ds2))
                    result.setValues(tempS,ds1);
                else if(s0.contains("L"))
                    result.setValues(tempS,"LT");
                else if(s0.contains("G"))
                    result.setValues(tempS,"GT");
                else if(s0.contains("CA"))
                    result.setValues(tempS,"CA");
                else if(s0.contains("SY"))
                    result.setValues(tempS,"SY");  
                else if(s0.contains("AP"))
                    result.setValues(tempS,"AP");  
                else if(ds1.isEmpty())
                	result.setValues(tempS,ds2);
                else if(ds2.isEmpty())
                	result.setValues(tempS,ds1);
                else{
                    result.setValues(tempS,"NA");//not available or not applicable
                }
                
                if(isPostNNS)
                	result.setS(result.s+nns0);
                else
                	result.setS(nns0+result.s);
             
                result.setX(x);

                
                //TO DO: what about this has large x but empty ds, s2x has small x but non-numerial ds
                //should it be processed the same way as in addS2XNonNumerialUnc()
                //It is now implemented in addS2XNonNumericalUnc (4/23/2025)
                
                //System.out.println("#    result.s="+result.s()+" result.ds="+result.ds());
                return result;
 
            }
            //debug
            //System.out.println("In SDS2XDX 814: this:"+this.s+" "+this.ds+" s2x="+s2x.s+"  "+s2x.ds+" x="+x+"  temp="+temp.x()+" "+temp.dx()+" s="+tempS+" ds="+temp.ds());
            //System.out.println("                                result.s="+result.s()+" result.ds="+result.ds());
                
        }else if(this.s.isEmpty()&&this.ds.isEmpty()&&s2x.canGetX()){
            result.setValues(s2x);
            return result;
        }else if(s2x.s().isEmpty()&&s2x.ds().isEmpty()&&this.canGetX()){
            return result;
        }else{
            System.out.println("Can't add:"+id+" "+this.S()+" "+this.DS()+" with "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            result.setValues(this.s(),"NA");
            result.setX(x);
            return result;
        }
        
        boolean useMinMax=false;
        if( (this.isLargeUnc||s2x.isLargeUnc) && isAllowMaxMinEP) {	
        	useMinMax=true;
        }else if(this.isLimit || this.ds.startsWith("L") || this.ds.startsWith("G"))
			useMinMax=true;
		else if(s2x.isLimit || s2x.ds.startsWith("L") || s2x.ds.startsWith("G"))
			useMinMax=true;
        
		if( useMinMax ) {			
	        if(this.dxu>=0 && this.dxl>=0 && s2x.dxu>=0 && s2x.dxl>=0) {	    		
        		dxu=this.dxu+s2x.dxu;
        		dxl=this.dxl+s2x.dxl;
	        }

		}
		
        //System.out.println(" In SDS2XDX: line 873: x="+x+"  dx="+dx+" dxu="+dxu+" dxl="+dxl+"  s="+result.s()+"  "+result.ds()+" result.x="+result.x+" result.dx="+result.dx+" result.can getDX="+result.canGetDX());
        
        if(dxu>=0 && dxl>=0 && (dxu+dxl)>0) {
            temp.setErrorLimit(errorLimit);
            temp.setValues(x, dxu, dxl);
       
            if(temp.dsu().length()>0 && temp.dsl().length()>0) {
                if(temp.dsu().equals(temp.dsl()))
                    result.setValues(temp.s(),temp.dsu());
                else
                    result.setValues(temp.s(),"+"+temp.dsu()+"-"+temp.dsl());
            }
            
            //System.out.println("In SDS2XDX 842: temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" "+temp.ds()+" dsu="+temp.dsu()+" dsl="+temp.dsl());
            //System.out.println("                                result.s="+result.s()+" result.ds="+result.ds());
        }
        
        if(this.ds.isEmpty() && s2x.ds().isEmpty())
            result.setDS("");

        //System.out.println(" In SDS2XDX: line 894: x="+x+"  dx="+dx+"   s="+result.s()+" ds="+result.ds()+" result.x="+result.x+" result.dx="+result.dx+" result.can getDX="+result.canGetDX());
        //System.out.println("  "+this.s+"  "+this.ds+"  $"+s2x.s+"  "+s2x.ds);
        
        checkUncertainty(result,this,s2x,"+");

        //System.out.println(" In SDS2XDX: line 898: x="+x+"  dx="+dx+"   s="+result.s()+"  ds="+result.ds()+" result.x="+result.x+" result.dx="+result.dx+" result.can getDX="+result.canGetDX());

        if(nns0.length()>0) {
            if(isPostNNS)
            	result.setS(result.s+nns0);
            else
            	result.setS(nns0+result.s);
        }
        
        if(!result.ds.startsWith("L")&&!result.ds.startsWith("G")) {
            result.setX(x);
            
            //only for both this and s2x has symmetric numerical uncertainty 
            if(dx>=0) result.setDX(dx);
            
            //only for cases with asymmetric uncertainties
            if(dxu>=0 && dxl>=0 && (dxu+dxl)>0) result.setDX(dxu,dxl);
            
            if(result.dx0<0) dx0=result.dx;
            if(result.dxu0<0) dxu0=result.dxu;
            if(result.dxl0<0) dxl0=result.dxu;
        }


        //System.out.println(" In SDS2XDX: line 915: x="+x+"  dx="+dx+"   s="+result.s()+"  "+result.ds());
        
        return result;
    }

    //ds must be in ENSDF format
    public SDS2XDX add(String s,String ds){
        return add(s,ds,false);
    }
    //ds must be in ENSDF format
    //if ignoreEmptyUnc=true, treat empty unc as "0"
    public SDS2XDX add(String s,String ds,boolean ignoreEmptyUnc){
        SDS2XDX temp=new SDS2XDX();
        if(ds.trim().isEmpty() && ignoreEmptyUnc)
        	ds="0";
        
        temp.setValues(s, ds);
        return add(temp);
    }
    public SDS2XDX add(String s){return add(s,"0");}//treat s as a constant with no uncertainty (unc=0)
 
    public SDS2XDX add(double x,double dxu,double dxl){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(x,dxu,dxl,errorLimit);
        
        //if(x>4 && x<5)
        //System.out.println(" x="+x+" dxu="+dxu+" dxl="+dxl+" temp="+temp.x()+" "+temp.dx+" temp dxu="+temp.dxu+" temp s="+temp.s+" ds="+ temp.ds+" "+temp.canGetDX());
        
        return add(temp);
    }
    public SDS2XDX add(float x,float dxu,float dxl){return add((double)x,(double)dxu,(double)dxl);}
    public SDS2XDX add(double x,double dx){return add(x,dx,dx);}
    public SDS2XDX add(float x,float dx){return add(x,dx,dx);}   
    public SDS2XDX add(double v){return add(String.valueOf(v));}
    public SDS2XDX add(float v){return add((double)v);}


    public SDS2XDX subtract(SDS2XDX s2x,boolean ignoreEmptyUnc){
    	if(ignoreEmptyUnc && s2x.ds.isEmpty()) {
    		return subtract(s2x.s);
    	}else {
    		return subtract(s2x);
    	}
    }
    public SDS2XDX subtract(SDS2XDX s2x){
        SDS2XDX result=new SDS2XDX();
        result.setValues(this);
        result.setErrorLimit(errorLimit);
        if(s2x==null || s2x.s().isEmpty() || this.s().isEmpty())
            return result;
        
        //System.out.println(s2x.ns+"   $"+this.ns+" "+(s2x.nns.equals(this.nns)));
        String strRepr0="";
    	if(s2x.toString().startsWith("-")) {
    		strRepr0="["+this.toString()+"-("+s2x.toString()+")]";
    	}else {
            strRepr0="["+this.toString()+"-"+s2x.toString()+"]";    		
    	}
    	
        if(s2x.nns.equals(this.nns)) {
        	s2x=new SDS2XDX(s2x.ns,s2x.ds);
        	
        	//System.out.println(s2x.ns);
        	
            SDS2XDX temp=s2x.multiply(-1.0);
            
            //System.out.println("###"+temp.s);
            
            result=this.add(temp);
        }
        
        result.strRepr=strRepr0;
    	
        return result;

     }
    
    public SDS2XDX subtract(String s,String ds,boolean ignoreEmptyUnc){
    	if(ds.isEmpty() && ignoreEmptyUnc)
    		ds="0";

		SDS2XDX temp=new SDS2XDX(s,ds);        
		return subtract(temp);
    }
    
    public SDS2XDX subtract(String s,String ds){
        SDS2XDX temp=new SDS2XDX(s,ds);
        return subtract(temp);
    }
    
    public SDS2XDX subtract(String s){//treat s as a constant with no unc
        return subtract(s,"0");
    }
    public SDS2XDX subtract(double x,double dxu,double dxl){
        x=-x;
        double temp=dxl;
        dxl=dxu;
        dxu=temp;
        
        return add(x,dxu,dxl);
    }
    public SDS2XDX subtract(float x,float dxu,float dxl){return subtract((double)x,(double)dxu,(double)dxl);}
    public SDS2XDX subtract(double x,double dx){return subtract(x,dx,dx);}
    public SDS2XDX subtract(float x,float dx){return subtract(x,dx,dx);}
    
    public SDS2XDX subtract(double x){
        x=-x;
        return add(String.valueOf(x));
    } 
    public SDS2XDX subtract(float x){return subtract((double)x); }
    
    public SDS2XDX multiply(SDS2XDX s2x,boolean ignoreEmptyUnc){
    	if(ignoreEmptyUnc && s2x.ds.isEmpty()) {
    		return multiply(s2x.s);
    	}else {
    		return multiply(s2x);
    	}
    }
    public SDS2XDX multiply(SDS2XDX s2x){
        SDS2XDX result=this.multiply0(s2x);
        //System.out.println("SDS2XDX 1635 after multiply0: "+result.x+"  "+result.s+"  "+result.ds+" this.s="+this.s+" ds="+this.ds+" s2x="+s2x.s()+"  "+s2x.ds());
        
        checkLargeUncAndCrossZero(result);
        
        //System.out.println("SDS2XDX 885: s1x.canGetDX="+this.canGetDX()+"  s1="+this.s()+" "+this.ds+"  s2x.canGetDX="+s2x.canGetDX()
        //+" s2="+s2x.s()+" "+s2x.ds+" result.s="+result.s+" result.ds="+result.ds+" result.dx="+result.dx+" result.canGetDX="+result.canGetDX()+" errorLimit="+errorLimit);
        
        if(s2x==null || s2x.s().isEmpty() || this.s().isEmpty() || result.ds().equals("NA") || !this.canGetX() || !s2x.canGetX())
            return result;


        //canGetDX()=true for symmetric uncertainty
        //canGetDXS()=true for asymmetric uncertainty
        if( result.canGetDX() || result.dsIsAsymm())//
            return result;

        //if(this.canGetDX() && s2x.canGetDX())
        //  return result;      

        
    
        //The purpose here is to set the precision of the final value when one of (s,ds) pair has 
        //non-numerical ds like LT,AP,..., while the other one has numerical ds
        //Right now, even with one of the (s,ds) pair having empty, the final result will still have uncertainty.
        
        SDS2XDX sx1=this;
        SDS2XDX sx2=s2x;

        if(!this.canGetDX() || this.dx==0){
            sx1=new SDS2XDX(this.s(),"1");
            double r=0;
            if(sx1.x!=0)
            	r=Math.abs(sx1.dx()/sx1.x());
            
            double tempDX=Math.abs(result.x*r);
            
            if(r>0.1 || tempDX>errorLimit || this.ds.isEmpty() || this.dx==0)
                sx1.setValues(this.s(), "0");
            
            //System.out.println(" this.s="+this.s+" x="+sx1.x+" dx="+sx1.dx);
        }
        if(!s2x.canGetDX() || s2x.dx==0){
            sx2=new SDS2XDX(s2x.s(),"1");
            double r=0;
            if(sx2.x!=0)
            	r=Math.abs(sx2.dx()/sx2.x());
            
            double tempDX=Math.abs(result.x*r);
            
            if(r>0.1 || tempDX>errorLimit || s2x.ds.isEmpty() || s2x.dx==0)
                sx2.setValues(s2x.s(), "0");

            //System.out.println(" x="+sx2.x+" dx="+sx2.dx+" r="+r+" tempDX="+tempDX+" s2x.ds="+s2x.ds+" s2x.dx="+s2x.dx);
        }
        
        SDS2XDX temp=sx1.multiply0(sx2);

        //System.out.println("SDS2XDX 965: sx1.canGetDX="+sx1.canGetDX()+"  s1="+sx1.s()+" "+sx1.ds()+" sx2.canGetDX="+sx2.canGetDX()+" sx2="+sx2.s()+" "+sx2.ds()+" temp.s="+temp.s+" temp.ds="+temp.ds+"  "+temp.canGetDX()+" temp.dx="+temp.dx);
        
        int nsig1=Str.findNSig(result.s());
        int nsig2=Str.findNSig(temp.s());
        
        //if(nsig2<nsig1 && (result.s().contains("E")==temp.s().contains("E")) )       	
        if(nsig2<nsig1)
            result.setS(temp.s());
        
        //System.out.println("SDS2XDX 974: nsig1="+nsig1+" nsisg2="+nsig2+" result s="+result.s);
        
        return result;
    }
  
    public SDS2XDX multiply1(SDS2XDX s2x){
        return multiply0(s2x);
    }
    
    @SuppressWarnings("unused")
	public SDS2XDX multiply0(SDS2XDX s2x){
        SDS2XDX result=new SDS2XDX();
        XDX2SDS temp=new XDX2SDS();
        result.setValues(this.s, this.ds);
        result.setX(x);
        result.setDX(dx);
        result.setDX(dxu,dxl);
          
        result.setErrorLimit(errorLimit);
        
        if(s2x==null || s2x.s().isEmpty() || this.s().isEmpty())
            return result;
        
        if(s2x.s().isEmpty())
            return s2x;
        
    	if(s2x.toString().startsWith("-")) {
    		result.strRepr="{"+this.toString()+"*{"+s2x.toString()+"}}";
    	}else {
            result.strRepr="{"+this.toString()+"*"+s2x.toString()+"}";    		
    	}
		//System.out.println("@@@@##"+this.toString()+"$$  s2x="+s2x+"  result="+strRepr);
		
        double x=-1,dx=-1,dxu=-1,dxl=-1;
        if(this.x==0)
            return result;
        if(s2x.x==0){
            result.setValues("0", "");
            return result;
        }
        
        if(this.nns.length()>0 || s2x.nns.length()>0) {
            System.out.println("Can't multipy:"+id+" "+this.S()+" "+this.DS()+" with "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            result.setValues(this.s(),"NA");
            return result;             
        }

        int nsig1=-1,nsig2=-1,maxNsig=-1,minNsig=-1;
        if(this.canGetX()&&s2x.canGetX()){
            x=this.x*s2x.x();
            
            nsig1=Str.findNSig(this.s());
            //nsig2=Str.findNDigitsAfterDot(s2x.s);
            nsig2=Str.findNSig(s2x.s);
            

            //maxNsig=Math.min(defaultMaxNsigma,nsig1+nsig2);
            //maxNsig=Str.findBestNSigOfProduct(this.s, s2x.s, errorLimit);
            maxNsig=Str.findNSigOfExactProduct(this.s, s2x.s);
            maxNsig=Math.min(maxNsig,nsig1+nsig2);
            
            //System.out.println("SDS2XDX 2173: this.s="+this.s+"   s2x.s="+s2x.s+"  nsig1="+nsig1+"  nsig2="+nsig2+"  maxNsig="+maxNsig);
            
            /*
            maxNsig=Math.max(nsig1,nsig2);
            minNsig=Math.min(nsig1,nsig2);
            
            String tempS=String.valueOf(x);
            tempS=Str.removeEndZeros(tempS);
            int tempNsig=Str.findNSig(tempS);
            if(maxNsig==1 && tempNsig>1) {
                //e.g., 2*4=8         tempNsig=1 maxNsig=1
            	//      0.2*0.9=0.18  tempNsig=2 maxNsig=2
            	maxNsig=2;
            }
            if(minNsig>defaultMaxNsigma)
            	maxNsig=minNsig;
            else
            	maxNsig=Math.min(defaultMaxNsigma, maxNsig);//should not exceed global nsigma, default=3 for all ENSDF values if not set specifically
            
            */
            
            //debug
            //System.out.println("In SDS2XDX 2195: multiply: this="+this.x+" "+this.dx+" s2x="+s2x.x()+" "+s2x.dx()+"  maxNsig="+maxNsig+"  "+Str.findNSig("0.080"));
            //System.out.println("              canGetDX: this="+this.canGetDX()+" s2x="+s2x.canGetDX()+" this.ds="+this.ds());
            
            if(this.canGetDX()&&s2x.canGetDX()){
                dx=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                result.setValues(x,dx);
                
                //System.out.println("In SDS2XDX 2199 "+this.x+"  "+this.dxu+" "+this.dxl+" "+ s2x.x+" "+s2x.dxu+"  "+s2x.dxl+" "+result.s+" x="+x+" dx="+dx);
               
                if(dx==0) { 
                	int nsig=maxNsig;
                	
                	result.setS(Str.roundByNsig(String.valueOf(x), nsig));  
                }

                
                //temp.setValues(x, dx);
                //result.setValues(temp.s(), temp.ds());
                
                //debug
                //System.out.println("In SDS2XDX 2209: multiply: this="+x+" "+dx+" result="+result.x()+" "+result.dx()+" resultS="+result.s()+" "+result.ds()+" errorLimit="+errorLimit);
            }else if(this.canGetDX()){
                /*
            	if(s2x.ds().trim().length()==0){
                    dx=Math.abs(x*this.dx/this.x);
                    result.setValues(x,dx);
                    
                    if(dx==0) 
                    	result.setS(Str.roundByNsig(String.valueOf(x), maxNsig));   
                    
                    //temp.setValues(x, dx);
                    //result.setValues(temp.s(), temp.ds());
                    
                    //debug
                    //System.out.println("In SDS2XDX: multiply: temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" "+temp.ds()+" errorLimit="+errorLimit);
                    //System.out.println("            x="+x+" dx="+dx+"          result.s="+result.s()+" result.ds="+result.ds());

                }else */
                	

            	if(s2x.dsIsAsymm()) {
                    if(this.x>0) {
                        dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                        dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                    }else {
                        dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                        dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                    }
    
                }else{
                	SDS2XDX tempResult=multiplyS2XNonNumericalUnc(this.x,this.dx,s2x,maxNsig);
                	result.setValues(tempResult);
                	
                	/*
                	temp.setNsig(maxNsig);
                    temp.setValues(x,0);
           
                    //LT,LE,GT,GE,AP
                    if(!s2x.ds().trim().isEmpty() && Character.isLetter(s2x.ds().trim().charAt(0))){
                        result.setValues(temp.s(), s2x.ds());
                        result.setX(x);
                    }
                    else{
                        result.setValues(temp.s(),"");
                    }
                    */
                	
                    //debug
                    //System.out.println("In SDS2XDX 1113: multiply: temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" "+temp.ds());
                    //System.out.println("        result.s="+result.s()+" result.ds="+result.ds()+" x="+result.x+" dx="+result.dx+" maxNsig="+maxNsig);
                }
            
            }else if(s2x.canGetDX()){
            	//System.out.println("hello"+this.x+"  "+this.dxu+" "+this.dxl+" "+ s2x.x+" "+s2x.dxu+"  "+s2x.dxl+" "+result.s);
            	
                /*
            	if(this.ds().trim().length()==0){
                    dx=Math.abs(x*s2x.dx()/s2x.x());
                    result.setValues(x,dx);
                    
                    if(dx==0) 
                    	result.setS(Str.roundByNsig(String.valueOf(x), maxNsig));   
                    
                    //temp.setValues(x, dx);
                    //result.setValues(temp.s(), temp.ds());
                    
                    //debug
                    //System.out.println("In SDS2XDX 1168: multiply: temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" "+temp.ds()+" errorLimit="+errorLimit);
                    //System.out.println("            x="+x+" dx="+dx+"          result.s="+result.s()+" result.ds="+result.ds());
                    

                }else */
            	
            	if(this.dsIsAsymm()) {

                    if(s2x.x()>0) {
                        dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                        dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                    }else {
                        dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                        dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                    }
  
                }else{
                	SDS2XDX tempResult=multiplyS2XNonNumericalUnc(s2x.x,s2x.dx,this,maxNsig);
                	result.setValues(tempResult);
                	
                	//System.out.println("In SDS2XDX 1796: multiply:  result.s="+result.s()+" result.ds="+result.ds()+" maxNsig="+maxNsig+" temp.s="+tempResult.s);
                    
                	/*
                	temp.setNsig(maxNsig);
                    temp.setValues(x,0);
                    
                    //System.out.println("In SDS2XDX 1224: multiply: temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" "+temp.ds()+" errorLimit="+errorLimit+" maxNsig="+maxNsig);
                    
                    //LT,LE,GT,GE,AP
                    if(!this.ds().trim().isEmpty() && Character.isLetter(this.ds().trim().charAt(0))){
                        result.setValues(temp.s(), this.ds());
                        result.setX(x);
                    }
                    else{
                    
                        result.setValues(temp.s(),"");
                    }
                    */
                }
            }else if(this.dsIsAsymm()&&s2x.dsIsAsymm()){
                if(this.x()>0 && s2x.x()>0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));  
                }else if(this.x()<0 && s2x.x()<0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));  
                }else if(this.x()>0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));  
                }else if(s2x.x()>0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));  
                }

            
            }else if(this.dsIsAsymm()){    
            	SDS2XDX tempResult=multiplyS2XNonNumericalUnc(this.x,this.dxu,this.dxl,s2x,maxNsig);
            	result.setValues(tempResult);
            	
                /*
            	if(s2x.ds().isEmpty()) {
                    
                    if(s2x.x()>0) {
                        dxu=Math.abs(x*this.dxu()/this.x());
                        dxl=Math.abs(x*this.dxl()/this.x()); 
                    }else {
                        dxu=Math.abs(x*this.dxl()/this.x());
                        dxl=Math.abs(x*this.dxu()/this.x()); 
                    }
                    
                    //System.out.println(" dxu="+dxu+" dxl="+dxl);
                 }else{
                	 temp.setNsig(maxNsig);
                     temp.setValues(x,0);
                     
                     //LT,LE,GT,GE,AP
                     if(!s2x.ds().trim().isEmpty() && Character.isLetter(s2x.ds().trim().charAt(0))){
                         result.setValues(temp.s(), s2x.ds());
                         result.setDX(Math.abs(x));
                     }else{
                         result.setValues(temp.s(),"");
                     }
                 }  
                 */              

            }else if(s2x.dsIsAsymm()){
            	SDS2XDX tempResult=multiplyS2XNonNumericalUnc(s2x.x,s2x.dxu,s2x.dxl,this,maxNsig);
            	result.setValues(tempResult);
            	
                /*
            	if(this.ds.isEmpty()) {
                    if(this.x()>0) {
                        dxu=Math.abs(x*s2x.dxu()/s2x.x());
                        dxl=Math.abs(x*s2x.dxl()/s2x.x()); 
                    }else {
                        dxu=Math.abs(x*s2x.dxl()/s2x.x());
                        dxl=Math.abs(x*s2x.dxu()/s2x.x()); 
                    }
                }else {
                	temp.setNsig(maxNsig);
                    temp.setValues(x,0);
                    
                    //LT,LE,GT,GE,AP
                    if(!this.ds().trim().isEmpty() && Character.isLetter(this.ds().trim().charAt(0))){
                        result.setValues(temp.s(), this.ds());
                        result.setX(x);
                    }else{
                        result.setValues(temp.s(),"");
                    } 
                }
                */
            }else if(this.ds.isEmpty() && s2x.ds().isEmpty()) {
            	SDS2XDX tempResult=multiplyS2XNonNumericalUnc(this.x,this.dxu,this.dxl,s2x,maxNsig);
            	result.setValues(tempResult);
            	
            	//System.out.println("In SDS2XDX 1953: multiply:          result.s="+result.s()+" result.ds="+result.ds()+" maxNsig="+maxNsig);
            }else{
     
            	temp.setNsig(maxNsig);
                temp.setValues(x, 0);

                //System.out.println("SDS2XDX 1957: maxNsig="+maxNsig+" temp.x="+temp.x()+" temp.s="+temp.s()+" sx1="+this.s+" ds="+this.ds+" s2x="+s2x.s+" ds="+s2x.ds);
                
                //LT,LE,GT,GE,AP
                
                String ds1=this.ds().trim();
                String ds2=s2x.ds().trim();
                String s0="";
                
                if(ds1.length()>0 || ds2.length()>0) {
                	if(ds1.length()>0)
                		s0+=ds1.charAt(0);
                	if(ds2.length()>0)
                		s0+=ds2.charAt(0);
                	
                    if(this.x()*s2x.x()>=0) {
                         if(s0.equals("LG") || s0.equals("GL")){//assuming two numbers are positive
                             result.setValues(temp.s(),"NA");
                             result.setX(x);
                             return result;                         
                         }
                         
                         if(s0.equals("LL") || s0.equals("GG")) {
                             if(this.x()<=0 || s2x.x()<=0) {
                                 char c=ds1.charAt(0);
                                 if(c=='L')
                                     c='G';
                                 else
                                     c='L';
                                 
                                 ds1=c+ds1.substring(1);
                                 ds2=c+ds2.substring(1);
                             }
                         }else if(ds1.startsWith("L") && this.x()<=0)
                             ds1="G"+ds1.substring(1);
                         else if(ds1.startsWith("G") && this.x()<=0)
                             ds1="L"+ds1.substring(1);
                         else if(ds2.startsWith("L") && s2x.x()<=0)
                             ds2="G"+ds2.substring(1);
                         else if(ds2.startsWith("G") && s2x.x()<=0)
                             ds2="L"+ds2.substring(1);
                    }else { 
                        if(s0.equals("LL") || s0.equals("GG")) {
                            result.setValues(temp.s(),"NA");
                            result.setX(x);
                            return result; 
                        }
                        
                        if(this.x()>0){//but s2x.x()<0
                            if(ds1.startsWith("L"))
                                ds1="G"+ds1.substring(1);
                            else if(ds1.startsWith("G"))
                                ds1="L"+ds1.substring(1);   
                            else if(ds2.startsWith("L"))
                                ds2="G"+ds2.substring(1);
                            else if(ds2.startsWith("G"))
                                ds2="L"+ds2.substring(1);
                        }else{//s2x.x()>0 but this.x()<0
                            if(ds2.startsWith("L"))
                                ds2="G"+ds2.substring(1);
                            else if(ds2.startsWith("G"))
                                ds2="L"+ds2.substring(1);   
                            else if(ds1.startsWith("L"))
                                ds1="G"+ds1.substring(1);
                            else if(ds1.startsWith("G"))
                                ds1="L"+ds1.substring(1); 
                        }

                    }

                }
                
                //System.out.println(temp.S()+" ds1="+ds1+" ds2="+ds2);
                
                s0=ds1+" "+ds2;
                if(ds1.equals(ds2)) {
                	result.setValues(temp.s(),ds1);
                }else if(s0.contains("L"))
                    result.setValues(temp.s(),"LT");
                else if(s0.contains("G"))
                    result.setValues(temp.s(),"GT");
                else if(s0.contains("CA"))
                    result.setValues(temp.s(),"CA");
                else if(s0.contains("SY"))
                    result.setValues(temp.s(),"SY");  
                else if(s0.contains("AP"))
                    result.setValues(temp.s(),"AP");  
                else {
                    result.setValues(temp.s(),"NA");//not available or not applicable
                }
                
                //debug
                //System.out.println("In SDS2XDX: multiply: x="+x+"  temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" ds="+temp.ds());
                //System.out.println("ds1="+ds1+" ds2="+ds2+"  this="+this.s()+"  "+this.ds()+" s2x="+s2x.s()+" "+s2x.ds()+" result.s="+result.s()+" result.ds="+result.ds());
                
                
                result.setX(x);
                
                return result;

                 
            }
            
            //debug
            //System.out.println("In SDS2XDX 1222: multiply: x="+x+"  dxu="+dxu+" dxl="+dxl+" temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" ds="+temp.ds());
            //System.out.println("  this="+this.s()+"  "+this.ds()+" s2x="+s2x.s()+" "+s2x.ds()+" result.s="+result.s()+" result.ds="+result.ds());                
        }
        else{
            System.out.println("Can't multipy:"+id+" "+this.S()+" "+this.DS()+" with "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            result.setValues(this.s(),"NA");
            result.setX(this.x);
            return result;
        }

        boolean useMinMax=false;
        if( (this.isLargeUnc||s2x.isLargeUnc) && isAllowMaxMinEP) {	
        	useMinMax=true;
        }else if(this.isLimit || this.ds.startsWith("L") || this.ds.startsWith("G"))
			useMinMax=true;
		else if(s2x.isLimit || s2x.ds.startsWith("L") || s2x.ds.startsWith("G"))
			useMinMax=true;
        
		if( useMinMax ) {	

			if(dxu<0 && result.dxu>0)
				dxu=result.dxu;
			if(dxl<0 && result.dxl>0)
				dxl=result.dxl;
           
	        if(x!=0 && dxu>0 && dxl>0) {

	        	boolean canCrossZero=false;
	        	
	    		double[] out=findProductMinMax(this,s2x,canCrossZero);
	    		double lv=out[0];
	    		double uv=out[1];
	    		
        		dxu=uv-x;
        		dxl=x-lv;
	        }

		}

        if(dxu>=0 && dxl>=0 && (dxu+dxl)>0) {
            temp.setErrorLimit(errorLimit);
            temp.setValues(x, dxu, dxl);

            
            if(temp.dsu().length()>0 && temp.dsl().length()>0) {
                if(temp.dsu().equals(temp.dsl()))
                    result.setValues(temp.s(),temp.dsu());
                else
                    result.setValues(temp.s(),"+"+temp.dsu()+"-"+temp.dsl());
            }
            
            //System.out.println("In SDS2XDX 772: multiply: temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" "+temp.ds()+" dsu="+temp.dsu()+" dsl="+temp.dsl());
            //System.out.println("                                result.s="+result.s()+" result.ds="+result.ds());
        }
        
        if(this.ds.isEmpty() && s2x.ds().isEmpty())
            result.setDS("");

        
        //debug
        //System.out.println("In SDS2XDX 1254: multiply: this="+this.s()+" "+this.ds()+" s2="+s2x.s()+" "+s2x.ds());
        //System.out.println("  1                              result.s="+result.s()+" result.ds="+result.ds());
        //System.out.println(" errorLimit="+errorLimit+" temp.s="+temp.s()+" temp.dsu="+temp.dsu()+" temp.dsl="+temp.dsl());
        
        checkUncertainty(result,this,s2x,"*");

        //debug
        //System.out.println("In SDS2XDX 1261: multiply: this="+this.s()+" "+this.ds()+" s2="+s2x.s()+" "+s2x.ds());
        //System.out.println("  2                              result.s="+result.s()+" result.ds="+result.ds());
        
        //System.out.println(x);
        
        if(!result.ds.startsWith("L")&&!result.ds.startsWith("G")) {
            result.setX(x);
            if(dx>=0) result.setDX(dx);
            if(dxu>=0 && dxl>=0 && (dxu+dxl)>0) result.setDX(dxu,dxl);
            
            if(result.dx0<0) dx0=result.dx;
            if(result.dxu0<0) dxu0=result.dxu;
            if(result.dxl0<0) dxl0=result.dxu;
        }

        
        //debug
        //System.out.println("In SDS2XDX 1222: multiply: x="+x+"  dxu="+dxu+" dxl="+dxl+" temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" ds="+temp.ds());
        //System.out.println("  this="+this.s()+"  "+this.ds()+" s2x="+s2x.s()+" "+s2x.ds()+" result.s="+result.s()+" result.ds="+result.ds()); 
        
        return result;
    }
    
	public double[] findProductMinMax(SDS2XDX sx1,SDS2XDX sx2,boolean canCrossZero) {
		double x1=sx1.x;
		double x2=sx2.x;
		double dxu1=sx1.dxu;
		double dxu2=sx2.dxu;
		double dxl1=sx1.dxl;
		double dxl2=sx2.dxl;
		
		
		double[] out=new double[2];
		Arrays.fill(out, 0);
        //System.out.println("@@@@"+x1+"  "+dxu1+"  "+dxl1+"   $"+x2+"  "+dxu2+"  "+dxl2);
        
		double min=1E20,max=-1E20;
		if(dxu1>=0 && dxl1>=0 && dxu2>=0 && dxl2>=0) {
			double[] val=new double[4];
			val[0]=x1+dxu1;
			val[1]=x1-dxl1;
			
			val[2]=x2+dxu2;
			val[3]=x2-dxl2;
			
			if(val[0]*val[1]<0 &&!canCrossZero) {
				if(x1>=0) {
					val[1]=0;
				}else {
					val[0]=0;
				}
			}
			
			if(val[2]*val[3]<0 &&!canCrossZero) {
				if(x2>=0) {
					val[3]=0;
				}else {
					val[2]=0;
				}
			}
			//System.out.println("XDX 344: val="+val[0]+"  "+val[1]+"  "+val[2]+" "+val[3]);
			
			for(int i=0;i<2;i++) {
				double va=val[i];
				for(int j=2;j<4;j++) {
					double vb=val[j];
					double v=va*vb;
					if(v>max)
						max=v;
					if(v<min)
						min=v;
					
					//System.out.println("XDX 356: va="+va+" vb="+vb+" v="+v+" max="+max+" min="+min);

				}
			}
			
			out[0]=min;
			out[1]=max;
		}

		
		return out;
	}
	
    public SDS2XDX multiply(String s,String ds,boolean ignoreEmptyUnc){
        SDS2XDX temp=new SDS2XDX();
        if(ds.trim().isEmpty() && ignoreEmptyUnc)
        	ds="0";
        
        temp.setValues(s, ds);
        return multiply(temp);
    }
    
    public SDS2XDX multiply(String s,String ds){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(s, ds);
        
        //System.out.println(s+"  "+ds);
        
        return multiply(temp);
    }
    
    public SDS2XDX multiply(String s){
        return multiply(s,"0");
    }
    
    public SDS2XDX multiply(float v,float dv){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(v, dv,errorLimit);
        return multiply(temp);
    }
    public SDS2XDX multiply(double v,double dv){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(v, dv,errorLimit);
        return multiply(temp);
    }
    
    public SDS2XDX multiply(float x,float dxu,float dxl){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(x, dxu,dxl,errorLimit);
        return multiply(temp);
    }
    
    public SDS2XDX multiply(double x,double dxu,double dxl){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(x, dxu,dxl,errorLimit);
        return multiply(temp);
    }
    
    public SDS2XDX multiply(float v){
            return multiply(String.valueOf(v));
    }
    public SDS2XDX multiply(double v){
        return multiply(String.valueOf(v));
    }

    //not used
    public SDS2XDX divided_new(SDS2XDX s2x){
        SDS2XDX result=this.divided0(s2x);
        
        if(s2x==null || s2x.s().isEmpty() || this.ds().isEmpty() || result.ds().equals("NA") || !this.canGetX() || !s2x.canGetX())
            return result;
        
        if(result.canGetDX() || result.dsIsAsymm)
            return result;
        //if(this.canGetDX() && s2x.canGetX())
        //  return result;
        
        SDS2XDX sx1=this;
        SDS2XDX sx2=s2x;
        if(!this.canGetDX()){
            sx1=new SDS2XDX(this.s(),"1");
            if(sx1.x()!=0 && Math.abs(sx1.dx()/sx1.x())>0.1)
                sx1.setValues(this.s(), "0");
        }
        if(!s2x.canGetDX()){
            sx2=new SDS2XDX(s2x.s(),"1");
            if(sx2.x()!=0 && Math.abs(sx2.dx()/sx2.x())>0.1)
                sx2.setValues(s2x.s(), "0");
        }
        
        SDS2XDX temp=sx1.divided0(sx2);
        
        result.setS(temp.s());
        
        if(this.ds.isEmpty() && s2x.ds().isEmpty())
            result.setDS("");
        
        return result;
    }
  
    public SDS2XDX divided(SDS2XDX s2x,boolean ignoreEmptyUnc){
    	if(ignoreEmptyUnc && s2x.ds.isEmpty()) {
    		return divided(s2x.s);
    	}else {
    		return divided(s2x);
    	}
    }
    
    public SDS2XDX divided(SDS2XDX s2x){
    	SDS2XDX result=divided0(s2x);
        
    	//System.out.println(result.s+"  "+result.ds);
        
    	checkLargeUncAndCrossZero(result);
    	
        return result;
    }
    
    @SuppressWarnings("unused")
	public SDS2XDX divided0(SDS2XDX s2x){
    	
    	//System.out.println("SDS2XDX 2537: s1="+this.s+" ds1="+this.ds+"@s2="+s2x.s+" ds2="+s2x.ds);

    	
        SDS2XDX result=new SDS2XDX();
        XDX2SDS temp=new XDX2SDS();
        result.setValues(this);
        result.setErrorLimit(errorLimit);
        
        double x,dx=-1,dxu=-1,dxl=-1;

		String tempDS="",ds1="",ds2="";
        
        if(s2x==null || s2x.s().isEmpty() || this.s().isEmpty() || this.x==0)
            return result;
        
        //System.out.println("SDS2XDX 1423: "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
        
        if(s2x.x==0){
            System.out.println("Can't divide:"+id+" "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            
            result.setValues(this.s(),"NA");
            return result;
        }
        
        
        if(this.nns.length()>0 || s2x.nns.length()>0) {
            System.out.println("Can't multiply:"+id+" "+this.S()+" "+this.DS()+" with "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            return this;                
        }
        
    	if(s2x.toString().startsWith("-")) {
    		result.strRepr="{"+this.toString()+"/{"+s2x.toString()+"}}";
    	}else {
            result.strRepr="{"+this.toString()+"/"+s2x.toString()+"}";    		
    	}
    	
        int nsig1=-1,nsig2=-1,maxNsig=-1;
        
        //System.out.println("SDS2XDX 2576: s1="+this.s+" ds1="+this.ds+"@s2="+s2x.s+" ds2="+s2x.ds+" "+this.canGetDX()+" "+s2x.canGetDX());
        
        if(this.canGetX()&&s2x.canGetX()){
            
            x=this.x/s2x.x();
            
            nsig1=Str.findNSig(this.s());
            nsig2=Str.findNSig(s2x.s);
            //maxNsig=Math.min(defaultMaxNsigma,nsig1+nsig2);
            
            maxNsig=Str.findNSigOfExactProduct(this.s, s2x.s);
            maxNsig=Math.min(maxNsig,nsig1+nsig2);
            
            /*
            if(maxNsig==1 && Str.isInteger(this.s) && Str.isInteger(s2x.s)) {
            	//e.g, s1=3,s2=1, result=3, maxNsig=1
            	// but s1=3,s2=2, result=1.5, maxNsig=1, then 
            	//      final result would be rounded to 1 or 2, but we want 1.5
            	int i1=Integer.parseInt(this.s);
            	int i2=Integer.parseInt(s2x.s);
            	if(i2!=0 && i1%i2!=0)
            		maxNsig=2;
            }
            */
            
            String tempS=String.valueOf(x);
            tempS=Str.removeEndZeros(tempS);
            
      
            
            /*
            int tempNsig=Str.findNSig(tempS);
            if(maxNsig==1 && tempNsig>1) {
                //e.g., 3/1=3         tempNsig=1 maxNsig=1
            	//      3/2=0.15      tempNsig=2 maxNsig=2
            	maxNsig=2;
            }
            
            maxNsig=Math.min(defaultMaxNsigma, maxNsig);//should not exceed global nsigma, default=3 for all ENSDF values if not set specially
            */
            
            //System.out.println(" tempS="+tempS+" maxNsig="+maxNsig+" tempNsig="+tempNsig);
            
            if(this.canGetDX()&&s2x.canGetDX()){   
            	
                dx=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                result.setValues(x, dx);

                if(dx==0) 
                	result.setS(Str.roundByNsig(String.valueOf(x), maxNsig));  
                
                //temp.setValues(x, dx);
                //result.setValues(temp.s(), temp.ds());
                
                //System.out.println("SDS2XDX 2901: result="+result.s+" ds="+result.ds);
                
            }else if(this.canGetDX() || this.ds().trim().length()==0){
                ds2=s2x.ds().toUpperCase().trim();      
                
                //System.out.println(" SDS2XDX 707: ds2="+ds2);
                /*
                if(this.canGetDX()) {               	
                    if(ds2.length()==0){// this.canGetDX=true
                        dx=Math.abs(x*this.dx/this.x);
                        result.setValues(x, dx);
                        
                        if(dx==0) 
                        	result.setS(Str.roundByNsig(String.valueOf(x), maxNsig));  
                        
                        //System.out.println("In SDS2XDX 708: divided: this="+this.s()+" "+this.ds()+" s2="+s2x.s()+" "+s2x.ds());
                        //System.out.println("                                result.s="+result.s()+" result.ds="+result.ds());
                        
                        //temp.setValues(x, dx);
                        //result.setValues(temp.s(), temp.ds());
                    }else 
                */               
                    if(this.canGetDX() && s2x.dsIsAsymm()) {  
                	//if(s2x.dsIsAsymm()) {
                        if(this.x>0) {
                            dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                            dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                        }else {
                            dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                            dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dx/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                        }
        
                    }else{//s2x.ds: LT,LE,GT,GE,AP,empty, and this.canGetDX or this.ds empty; or s2x.canGetDX=true and this.ds empty
                    	if(s2x.canGetDX()) {//this.canGetDX=false here
                    		
                           	SDS2XDX tempResult=dividedS2XNonNumericalUnc(this,s2x.x,s2x.dx,maxNsig);
                        	result.setValues(tempResult);
                        	
                            //System.out.println("In SDS2XDX 2337: divided: this="+this.s()+" "+this.ds()+" s2="+s2x.s()+" "+s2x.ds());
                            //System.out.println("      result.s="+result.s()+" result.ds="+result.ds());
                    	}else {//this.canGetDX=true, or this.DS empty
                           	SDS2XDX tempResult=dividedS2XNonNumericalUnc(this.x,this.dx,s2x,maxNsig);
                        	result.setValues(tempResult);
                        	
                        	//System.out.println("      result.s="+result.s()+" result.ds="+result.ds()+" "+result.x+" "+result.dx);
                    	}
                    	
                    	/*
                    	temp.setNsig(maxNsig);
                        temp.setValues(x,0);
                        
                        if(s2x.canGetDX())
                        	ds=this.ds;
                        else if(ds2.equals("LT")) ds="GT";
                        else if(ds2.equals("GT")) ds="LT";
                        else if(ds2.equals("LE")) ds="GE";
                        else if(ds2.equals("GE")) ds="LE";
                        else ds=ds2;
       
                        result.setValues(temp.s(), ds);
                        result.setX(x);
                        */
                    }
                /*
                }
                else {//this.ds.len==0, s2x.canGetDX=true
                    dx=Math.abs(x*s2x.dx()/s2x.x());
                    result.setValues(x,dx); 
                    if(dx==0) 
                    	result.setS(Str.roundByNsig(String.valueOf(x), maxNsig));   
                }
                */
            }else if(s2x.canGetDX() || s2x.ds().trim().length()==0){
            	//this.canGetDX=false and this.ds is NOT empty
            	
                ds1=this.ds().toUpperCase().trim();//not empty here
                
                //if(this.dsIsAsymm()) {
                if(this.dsIsAsymm() && s2x.canGetDX()) {
                    if(s2x.x()>0) {
                        dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                        dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                    }else {
                        dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                        dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dx()/s2x.x(),2));
                    }
        
                    //System.out.println(s+" "+ds+"  x="+x+" dxu="+dxu+" dxl="+dxl);
                }else if(!this.canGetDX()){//this.ds: LT,LE,GT,GE,AP and, s2x.canGetDX=true or s2x.ds empty     
                 	SDS2XDX tempResult=dividedS2XNonNumericalUnc(this,s2x.x,s2x.dx,maxNsig);
                	result.setValues(tempResult);
                	
                	/*
                	temp.setNsig(maxNsig);
                    temp.setValues(x,0);
                    
                    result.setValues(temp.s(), ds1);  
                    result.setX(x);
                    */
                }
            }else if(this.dsIsAsymm() && s2x.dsIsAsymm()) {
                if(this.x()>0 && s2x.x()>0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));  
                }else if(this.x()<0 && s2x.x()<0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));  
                }else if(this.x()>0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));  
                }else if(s2x.x()>0) {
                    dxu=Math.abs(x)*Math.sqrt(Math.pow(this.dxu/this.x,2)+Math.pow(s2x.dxu()/s2x.x(),2));
                    dxl=Math.abs(x)*Math.sqrt(Math.pow(this.dxl/this.x,2)+Math.pow(s2x.dxl()/s2x.x(),2));  
                }
            }else if(this.dsIsAsymm() && s2x.ds().isEmpty()) {
            	/*
                if(s2x.x()>0) {
                    dxu=Math.abs(x*this.dxu()/this.x());
                    dxl=Math.abs(x*this.dxl()/this.x()); 
                }else if(s2x.x()<0){
                    dxu=Math.abs(x*this.dxl()/this.x());
                    dxl=Math.abs(x*this.dxu()/this.x()); 
                }
                */
               	SDS2XDX tempResult=dividedS2XNonNumericalUnc(this.x,this.dxu,this.dxl,s2x,maxNsig);
            	result.setValues(tempResult);
            	/*
            	temp.setNsig(maxNsig);
                temp.setValues(x,0);
                
                result.setValues(temp.s(),"");  
                result.setX(x);
                */
                //System.out.println(" dxu="+dxu+" dxl="+dxl);
                
            }else if(s2x.dsIsAsymm()&&this.ds.isEmpty()) {
            	/*
                if(this.x()>0) {
                    dxu=Math.abs(x*s2x.dxl()/s2x.x());
                    dxl=Math.abs(x*s2x.dxu()/s2x.x()); 
                }else {
                    dxu=Math.abs(x*s2x.dxu()/s2x.x());
                    dxl=Math.abs(x*s2x.dxl()/s2x.x()); 
                } 
                */ 
             	SDS2XDX tempResult=dividedS2XNonNumericalUnc(this,s2x.x,s2x.dxu,s2x.dxl,maxNsig);
            	result.setValues(tempResult);
            	
            	/*
            	temp.setNsig(maxNsig);
                temp.setValues(x,0);
                
                result.setValues(temp.s(), this.ds());  
                result.setX(x);
                */
            }else{
            	temp.setNsig(maxNsig);
                temp.setValues(x, 0);
                
                //LT,LE,GT,GE,AP
                
                ds1=this.ds().trim();//not empty here
                ds2=s2x.ds().trim(); //not empty here
                
               
                String s0="";
                
                if(ds1.length()>0 && ds2.length()>0) {
                    s0=ds1.charAt(0)+ds2.charAt(0)+"";
                    if(this.x()*s2x.x()>=0) {
                         if(s0.equals("LL") || s0.equals("GG")){
                             result.setValues(temp.s(),"NA");
                             return result;                         
                         }
                         
                         if(ds1.startsWith("L") && this.x()<=0)
                             ds1="G"+ds1.substring(1);
                         else if(ds1.startsWith("G") && this.x()<=0)
                             ds1="L"+ds1.substring(1);
                         else if(ds2.startsWith("L") && s2x.x()>0)
                             ds2="G"+ds2.substring(1);
                         else if(ds2.startsWith("G") && s2x.x()>0)
                             ds2="L"+ds2.substring(1);
                    }else { 
                        if(s0.equals("LG") || s0.equals("GL")) {
                            result.setValues(temp.s(),"NA");
                            result.setX(x);
                            return result; 
                        }
                        
                        if(s0.equals("LL") || s0.equals("GG")) {
                            if(this.x()>=0) {
                                char c=ds1.charAt(0);
                                if(c=='L')
                                    c='G';
                                else
                                    c='L';
                                
                                ds1=c+ds1.substring(1);
                                ds2=c+ds2.substring(1);
                            }
                        }else if(this.x()>0){//but s2x.x()<0
                            if(ds1.startsWith("L"))
                                ds1="G"+ds1.substring(1);
                            else if(ds1.startsWith("G"))
                                ds1="L"+ds1.substring(1);   
                            else if(ds2.startsWith("L"))
                                ds2="G"+ds2.substring(1);
                            else if(ds2.startsWith("G"))
                                ds2="L"+ds2.substring(1);   
                        }else{//s2x.x()>0 but this.x()<0
                            //nothing to change                        
                        }

                    }

                }
                
                s0=ds1+" "+ds2;
                if(ds1.equals(ds2))
                    result.setValues(temp.s(),ds1);
                else if(s0.contains("L"))
                    result.setValues(temp.s(),"LT");
                else if(s0.contains("G"))
                    result.setValues(temp.s(),"GT");
                else if(s0.contains("CA"))
                    result.setValues(temp.s(),"CA");
                else if(s0.contains("SY"))
                    result.setValues(temp.s(),"SY");  
                else if(s0.contains("AP"))
                    result.setValues(temp.s(),"AP");  
                else {
                    result.setValues(temp.s(),"NA");//not available or not applicable
                }
                
                //debug
                //System.out.println("In SDS2XDX: multiply: x="+x+"  temp="+temp.x()+" "+temp.dx()+" s="+temp.s()+" ds="+temp.ds());
                //System.out.println("  this="+this.s()+"  "+this.ds()+" s2x="+s2x.s()+" "+s2x.ds()+" result.s="+result.s()+" result.ds="+result.ds());
                result.setX(x);
                
                return result;

                 
            }

            //System.out.println("SDS2XDX 2864: "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS()+" result="+result.dxu+" "+result.dxl+" dxu="+dxu+" dxl="+dxl);
            
            // result=A/B, if B has large uncertainty, reset result uncertainty by using max and min
            
            //System.out.println(s2x.isLargeUnc+" "+this.isLargeUnc);
            
            boolean useMinMax=false;
            if(s2x.isLargeUnc || (this.isLargeUnc && isAllowMaxMinEP) ){
            	useMinMax=true;
            }else if(this.isLimit || this.ds.startsWith("L") || this.ds.startsWith("G"))
    			useMinMax=true;
    		else if(s2x.isLimit || s2x.ds.startsWith("L") || s2x.ds.startsWith("G"))
    			useMinMax=true;
            
    		if( useMinMax ) {	
    		//if( (this.isLargeUnc||s2x.isLargeUnc) && isUseMaxMinEP) {
    			//System.out.println("SDS2XDX 3162: dxu="+dxu+" dxl="+dxl+" result.dx="+result.dxu+"  "+result.dxl);
    			
    			if(dxu<0 && result.dxu>0)
    				dxu=result.dxu;
    			if(dxl<0 && result.dxl>0)
    				dxl=result.dxl;
    			
    	        if(x!=0 && dxu>0 && dxl>0) {
                	double xu=0,xl=0;
                	double xu1=this.x+this.dxu;
                	double xl1=this.x-this.dxl;
                	double xu2=s2x.x+s2x.dxu;
                	double xl2=s2x.x-s2x.dxl;
                	
                	if(xl2<=0) {
                    	xu=Math.max(Math.max(xu1/xu2, 1E6*x),Math.max(xl1/xu2, 1E6*x));
                    	xl=Math.min(Math.min(xu1/xu2, 1E6*x),Math.min(xl1/xu2, 1E6*x));
                    	result.isLimit=true;
                	}else {
                    	xu=Math.max(Math.max(xu1/xu2, xu1/xl2),Math.max(xl1/xu2, xl1/xl2));
                    	xl=Math.min(Math.min(xu1/xu2, xu1/xl2),Math.min(xl1/xu2, xl1/xl2));
                	}

                	               	
                	if(xl*xu>=0) {
                		dxu=xu-x;
                		dxl=x-xl;
                	}
                	
                	//System.out.println("SDS2XDX 3185: "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS()+" x="+x+" xu="+xu+" xl="+xl+" "+Double.isInfinite(xu1/xl2));
                    //System.out.println("      dxu="+dxu+" dxl="+dxl);
    	        }
    		}
    		
    		/*
            if(x!=0 && dxu>0 && dxl>0) {
                double r1=Math.abs(s2x.dxu/s2x.x);
                double r2=Math.abs(s2x.dxl/s2x.x);
                
                //>=10% considered as large uncertainty
                //System.out.println(" r1="+r1+" r2="+r2);
                
                if(x>0 && s2x.dxu>0 && s2x.dxl>0 && Math.max(r1, r2)>=smallError*0.999999) {
                	double xu=0,xl=0;
                	double xu1=this.x+this.dxu;
                	double xl1=this.x-this.dxl;
                	double xu2=s2x.x+s2x.dxu;
                	double xl2=s2x.x-s2x.dxl;
                	
                	if(xl2<=0) {
                    	xu=Math.max(Math.max(xu1/xu2, 1E6*x),Math.max(xl1/xu2, 1E6*x));
                    	xl=Math.min(Math.min(xu1/xu2, 1E6*x),Math.min(xl1/xu2, 1E6*x));
                	}else {
                    	xu=Math.max(Math.max(xu1/xu2, xu1/xl2),Math.max(xl1/xu2, xl1/xl2));
                    	xl=Math.min(Math.min(xu1/xu2, xu1/xl2),Math.min(xl1/xu2, xl1/xl2));
                	}

                	
                	//System.out.println("SDS2XDX 1423: "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS()+" x="+x+" xu="+xu+" xl="+xl+" "+Double.isInfinite(xu1/xl2));
                	
                	if(xl*xu>=0) {
                		dxu=xu-x;
                		dxl=x-xl;
                	}
                }
            }
             */
            
        }else{
            
            System.out.println("Can't divide:"+id+" "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            
            result.setValues(this.s(),"NA");
            result.setX(this.x());
            return result;
        }
        
        //System.out.println("SDS2XDX: x="+x+" dxu="+dxu+" dxl="+dxl+" s="+s+" dsu="+dsu+" dsl="+dsl+" tempDS="+tempDS+" result s="+result.s+" "+result.ds);
        
        if(dxu>=0 && dxl>=0 && (dxu+dxl)>0) {
            temp.setErrorLimit(errorLimit);
            temp.setValues(x, dxu, dxl);
            
            //System.out.println("SDS2XDX 1703: "+x+" "+dxu+" "+dxl+" "+temp.isLimits());
            
            result.isLimit=temp.isLimits();
            if(!temp.isLimits()) {
                if(temp.dsu().length()>0 && temp.dsl().length()>0) {
                    if(temp.dsu().equals(temp.dsl()))
                        result.setValues(temp.s(),temp.dsu());
                    else
                        result.setValues(temp.s(),"+"+temp.dsu()+"-"+temp.dsl());
                    

                }
            }else if(x>0){
            	if(dxu>x*1E3) {
            		//lower limit
            		result.setValues(x-dxl, dxl);
            		
            		//System.out.println("   SDS2XDX 3262:         result.s="+result.s()+" result.ds="+result.ds()+"result x="+result.x+" dx="+result.dx+" dxu="+result.dxu+" dxl="+result.dxl);
            		//System.out.println("      x="+x+" dxu="+dxu+" dxl="+dxl);
            		
            		result.setDX(-1);
            		result.setDS("GT");
            		result.isLimit=true;

            	}else if(Math.abs(x-dxl)<1E-3*x && dxu<=1E-3*x){
            		result.setDX(-1);
            		result.setDS("LT");
            		result.isLimit=true;
            	}
            }

        }
        
        if(this.ds.isEmpty() && s2x.ds().isEmpty())
            result.setDS("");
            
        //System.out.println("In SDS2XDX 1: divided: this="+this.s()+" "+this.ds()+" s2="+s2x.s()+" "+s2x.ds());
        //System.out.println("   SDS2XDX 3267:         result.s="+result.s()+" result.ds="+result.ds()+" x="+result.x+" dx="+result.dx+" dxu="+result.dxu);
        
        checkUncertainty(result,this,s2x,"/");
        
        //System.out.println(result.s+"  "+result.ds);
        
        if(!result.ds.startsWith("L")&&!result.ds.startsWith("G")) {
            result.setX(x);
            if(dx>=0) result.setDX(dx);
            if(dxu>=0 && dxl>0 && (dxu+dxl)>0) result.setDX(dxu,dxl);
            
            if(result.dx0<0) dx0=result.dx;
            if(result.dxu0<0) dxu0=result.dxu;
            if(result.dxl0<0) dxl0=result.dxu;
        }

        
        //System.out.println("In SDS2XDX 1: divided: this="+this.s()+" "+this.ds()+" s2="+s2x.s()+" "+s2x.ds());
        //System.out.println("   SDS2XDX 3185         result.s="+result.s()+" result.ds="+result.ds()+" "+maxNsig+" x="+result.x);
        
        return result;
    }
    
    public SDS2XDX divided(String s,String ds,boolean ignoreEmptyUnc){
        SDS2XDX temp=new SDS2XDX();
        if(ds.trim().isEmpty() && ignoreEmptyUnc)
        	ds="0";
        
        temp.setValues(s, ds);
        return divided(temp);
    }
    
    public SDS2XDX divided(String s,String ds){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(s, ds);
        return divided(temp);
    }
    
    public SDS2XDX divided(String s){
        return divided(s,"0");
    }
    
    public SDS2XDX divided(float v,float dv){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(v, dv,errorLimit);
        
        //System.out.println(" "+temp.s()+"  "+temp.ds()+" x="+temp.x+"  dx="+temp.dx);
        
        return divided(temp);

    }
    public SDS2XDX divided(double v,double dv){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(v, dv,errorLimit);
        
        return divided(temp);

    }
    
    public SDS2XDX divided(double x,double dxu,double dxl){
        SDS2XDX temp=new SDS2XDX();
        temp.setValues(x, dxu,dxl,errorLimit);
        
        return divided(temp);

    }
    
    public SDS2XDX divided(float v){
            return divided(String.valueOf(v));
    }
    
    public SDS2XDX divided(double v){
        return divided(String.valueOf(v));
    }
    
	@SuppressWarnings("unused")
	public SDS2XDX divided0_new(SDS2XDX s2x) {
        SDS2XDX result=new SDS2XDX();      
        result.setValues(this);
        result.setErrorLimit(errorLimit);
        
        if(s2x==null || s2x.s().isEmpty() || this.s().isEmpty() || this.x==0)
            return result;
        
        //System.out.println("SDS2XDX 1923: "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
        
        if(s2x.x==0){
            System.out.println("Can't divide:"+id+" "+this.S()+" "+this.DS()+" by "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            
            result.setValues(this.s(),"NA");
            return result;
        }
        
        
        if(this.nns.length()>0 || s2x.nns.length()>0) {
            System.out.println("Can't multiply:"+id+" "+this.S()+" "+this.DS()+" with "+s2x.ID()+" "+s2x.S()+" "+s2x.DS());
            return this;                
        }
		
        SDS2XDX temp=new SDS2XDX();
        temp.setErrorLimit(99);
        
        double x=-1,dx=-1,dxu=-1,dxl=-1;
        String ds="",dsl="",dsu="";
        
        x=s2x.x;
        dx=s2x.dx;
		dxu=s2x.dxu;
		dxl=s2x.dxl;
		
		ds=s2x.ds;
        dsl=s2x.dsl;
        dsu=s2x.dsu;
		
        //System.out.println("SDS2XDX 1953: x="+x+" dx="+dx+" dxu="+dxu+" dxl="+dxl+" ds="+ds+" dsl="+dsl+" dsu="+dsu);
        
        boolean hasResetDxl=false,hasResetDxu=false;
        if(dxu<0) {
        	dxu=0;
        	hasResetDxu=true;
        }
        if(dxl<0) {
        	dxl=0;
        	hasResetDxl=true;
        }
		if(!hasResetDxl || !hasResetDxu) {
			double ax=Math.abs(x);
			String s1="",ds1="";
			boolean done=false;
			
			if(dxu/ax>0.1 || dxl/ax>0.1) {
				//when the denominator has large uncertainty (>10%), Taylor's expansion method for error propagation 
				//is not valid (e.g., a symmetric uncertainty will become very asymmetric). For such cases, 
				//use max-min approach
				double max=-1,min=-1;
				if(x>0 && x<=dxl) {//min x value overlaps with zero, max 1/x is positive infinity
					min=1.0/(x+dxu);
					temp.setValues(min,0);	
					
					s1=temp.s;
					ds1="GE";
				}else if(x<0 && (x+dxu)>=0) {//max x value overlaps with zero, min 1/x is negative infinity
					max=1.0/(x-dxl);
					temp.setValues(max,0);			
					
					s1=temp.s;
					ds1="LE";
				}else {
					max=1.0/(x-dxl);
					min=1.0/(x+dxu);
					
					x=1.0/x;
					dxu=max-x;
					dxl=x-min;		
					
					temp.setValues(x,dxu,dxl);
					
					if(hasResetDxl) {//max==x, original dxl<0
						temp.dxu=-1;
						temp.dsu="";
						temp.dx=-1;
						temp.ds="";
					}else if(hasResetDxu) {//min==x, original dxu<0
						temp.dxl=-1;
						temp.dsl="";
						temp.dx=-1;
						temp.ds="";
					}
					
					done=true;
				}

			}else {
				//System.out.println("1  x="+x+" dxu="+dxu+" dxl="+dxl);
				
				double tempDX=dxu;
				dxu=dxl/(x*x);
				dxl=tempDX/(x*x);    
				x=1.0/x;
	            //System.out.println("2  x="+x+" dxu="+dxu+" dxl="+dxl);
			}

			if(!done) {
				if(ds1.length()>0)
					temp.setValues(s1,ds1);
				else
					temp.setValues(x,dxu,dxl);
			}
	
		}else if(ds.startsWith("L")) { 
            ds="G"+ds.substring(1);        
			temp.setValues(1.0/x,0);
			temp.setValues(temp.s,ds);
        }else if(ds.startsWith("G")) { 
            ds="L"+ds.substring(1);
			temp.setValues(1.0/x,0);
			temp.setValues(temp.s,ds);   
			
        }else {
        	if(!Str.isLetters(ds))
        		ds="";
        	
			temp.setValues(1.0/x,0);
			temp.setValues(temp.s,ds);  
        }

        return multiply(temp);	
	}
    
	public SDS2XDX pow(double p) {
		try {
		
			if(!this.canGetX() || !Str.isNumeric(this.s()))
				return null;
			if(p==0)
				return new SDS2XDX("1.0","0");
			
			double tempX=-1, tempDX=-1;
			tempX=Math.pow(x, p);
			
		
			int errorLimit=this.errorLimit;
			
			//System.out.println("SDS2XDX 3436: s="+s+" "+ds);
			
			if(!Str.isNumericUnc(ds)) {
				
				tempDX=-1;
				
				boolean isGood=false;
				if(x>=0) {
					isGood=true;
				}else {
					if(ds.startsWith("L")||ds.startsWith("G")) {
						int intP=(int)p;
						if(p==intP && intP%2==1) {
							isGood=true;
						}else
							isGood=false;
					}else {
						isGood=true;
					}
				}
			
				if(isGood) {
					SDS2XDX result=new SDS2XDX();
					
					result.setErrorLimit(9);
					result.setValues(s, "1");
					
					tempDX=Math.min(Math.abs(result.dx/result.x*p*tempX), tempX*0.01);
					result.setValues(tempX,tempDX);
					
					result.setDS(ds);
					result.setDX(-1);
					
					return result;
				}else {
					return null;
				}

			}
	
			//when reaching here, both value and uncertainty are numerical
			int tempErrorLimit=errorLimit;
			
			//System.out.println("SDS2XDX 3479: s="+s+" "+ds+" dx="+dxu+" "+dxl);
			
			if(dxu>=0 && dxl>=0) {
				tempDX=Math.max(dxu, dxl);
				
				int d1=Integer.parseInt(dsu);
				int d2=Integer.parseInt(dsl);
				
				tempErrorLimit=Math.max(d1,d2);
			}else { 
				tempDX=dx;
	            if(dxu<=0)
	            	dxu=dx;
	            if(dxl<=0)
	            	dxl=dx;
	            
	            tempErrorLimit=Integer.parseInt(ds);
			}
			if(Math.abs(x)>0)
				tempDX=Math.abs(p)*(tempDX/Math.abs(x))*Math.abs(tempX);
			else
				tempDX=Math.abs(p)*tempDX;
					
			//tempErrorLimit=Math.min(errorLimit, 10*tempErrorLimit);
		 
			tempErrorLimit=errorLimit;
		
			SDS2XDX result=new SDS2XDX();
			result.setErrorLimit(tempErrorLimit);
			
			//System.out.println(tempErrorLimit+" "+isLargeUnc);
			
			//System.out.println("SDS2XDX 3508: tempX="+tempX+" tempDX="+tempDX+" errorLimit="+tempErrorLimit);
			
			if(!isLargeUnc && tempDX<Math.abs(tempX)) {
				result.setValues(tempX,tempDX);						
			}else {
				double x1=Math.pow(x+dxu,p);
				double x2=Math.pow(x-dxl,p);
				double f=(tempX-x1)*(tempX-x2);
				
				//System.out.println(tempX+" "+x1+" "+x2);
				
				if(f<=0) {
					//x is in between x1 and x2
					if(tempX<x1) {
						result.setValues(tempX,x1-tempX,tempX-x2);
					}else {
						result.setValues(tempX,x2-tempX,tempX-x1);
					}
					
					//System.out.println(tempX+" "+x1+" "+x2);
					//System.out.println(tempErrorLimit+" "+result.errorLimit);
				}else {
						
					if((x+dxu)*(x-dxl)<0) {
						//x range crosses zero, then pow(x,n) also crosses zero
						if(tempX>0 && x1>tempX) {
							result.setValues(tempX,Math.max(x1, x2)-tempX,tempX);
						}else if(tempX<0 && x1<tempX) {
							result.setValues(tempX,-tempX,tempX-Math.min(x1, x2));
						}else if(x1<tempX){
							result.setValues(tempX,0,tempX-Math.min(x1, x2));
						}else if(x1>tempX) {
							result.setValues(tempX,Math.max(x1, x2)-tempX,0);
						}else {
							return null;
						}
					}
				}
			}
			if(result.s.contains("E") && result.dsu.length()==1 && result.dsl.length()==1) {
				result.setValues(result.x,result.dxu,result.dxl,99);
			}
			
			result=result.removeEndZeros();
			
			return result;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * natural logarithm to the base e
	 * @param p
	 * @return
	 */
	public SDS2XDX log() {
		try {
		
			if(!this.canGetX() || !Str.isNumeric(this.s()) || x<0)
				return null;
			
			boolean isCrossZero=false;
			
			if(dxl>=0 && x<=dxl) {
				isCrossZero=true;
			}
			
			double tempX=-1, tempDX=-1;
			tempX=Math.log(x);

			int errorLimit=this.errorLimit;
			
			//System.out.println("SDS2XDX 3436: s="+s+" "+ds);
			
			if(!Str.isNumericUnc(ds)) {
	
				SDS2XDX result=new SDS2XDX();
				
				result.setErrorLimit(9);
				result.setValues(s, "1");
				
				tempDX=Math.min(Math.abs(result.dx/x), tempX*0.01);
				result.setValues(tempX,tempDX);
				
				result.setDS(ds);
				result.setDX(-1);
							
				return result;

			}else if(isCrossZero) {
				SDS2XDX result=new SDS2XDX();
				
				result.setErrorLimit(9);
				result.setValues(s, "1");
				
				tempX=Math.log(x+dxu);
				
				tempDX=Math.min(Math.abs(result.dx/x), tempX*0.01);
				result.setValues(tempX,tempDX);
				
				result.setDS("LT");
				result.setDX(-1);
				
				return result;
			}
	
			//when reaching here, both value and uncertainty are numerical
			int tempErrorLimit=errorLimit;
			
			//System.out.println("SDS2XDX 3479: s="+s+" "+ds+" dx="+dxu+" "+dxl);
			
			if(dxu>=0 && dxl>=0) {
				tempDX=Math.max(dxu, dxl);
				
				int d1=Integer.parseInt(dsu);
				int d2=Integer.parseInt(dsl);
				
				tempErrorLimit=Math.max(d1,d2);
			}else { 
				tempDX=dx;
	            if(dxu<=0)
	            	dxu=dx;
	            if(dxl<=0)
	            	dxl=dx;
	            
	            tempErrorLimit=Integer.parseInt(ds);
			}
			
			tempDX=tempDX/x;
					
			//tempErrorLimit=Math.min(errorLimit, 10*tempErrorLimit);
		 
			tempErrorLimit=errorLimit;
		
			SDS2XDX result=new SDS2XDX();
			result.setErrorLimit(tempErrorLimit);
			
			//System.out.println(tempErrorLimit+" "+isLargeUnc);
			
			//System.out.println("SDS2XDX 3508: tempX="+tempX+" tempDX="+tempDX+" errorLimit="+tempErrorLimit);
			
			if(!isLargeUnc && tempDX<Math.abs(tempX)) {
				result.setValues(tempX,tempDX);						
			}else {
				double x1=Math.log(x+dxu);
				double x2=Math.log(x-dxl);
					
				result.setValues(tempX,x1-tempX,tempX-x2);
				
				//System.out.println(tempX+" "+x1+" "+x2);				
			}
			
			if(result.s.contains("E") && result.dsu.length()==1 && result.dsl.length()==1) {
				result.setValues(result.x,result.dxu,result.dxl,99);
			}
			
			result=result.removeEndZeros();
			
			return result;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public SDS2XDX log10() {
		//log10(x)=log(x)/log(10)
		SDS2XDX result=log();

		if(result!=null) {
			result=result.multiply(1.0/Math.log(10),0);
		}
		
		return result;
	}
	
	/*
	 * return logarithm to sx with base=a
	 * loga(x)=log(x)/log(a)
	 */
	public static SDS2XDX log(SDS2XDX sx,double a) {
		if(a<=0)
			return null;
		
		SDS2XDX result=sx.log();
		if(result!=null) {
			result=result.multiply(1.0/Math.log(a),0);
		}
		
		return result;
	}
	
	public static SDS2XDX log10(SDS2XDX sx) {
		return SDS2XDX.log(sx, 10);
	}
	

	public SDS2XDX exp() {
		try {
			
			if(!this.canGetX() || !Str.isNumeric(this.s()))
				return null;
			
				
			double tempX=-1, tempDX=-1;
			tempX=Math.exp(x);

			int errorLimit=this.errorLimit;
			
			//System.out.println("SDS2XDX 3436: s="+s+" "+ds);
			
			if(!Str.isNumericUnc(ds)) {
	
				SDS2XDX result=new SDS2XDX();
				
				result.setErrorLimit(9);
				result.setValues(s, "1");
				
				tempDX=Math.min(Math.exp(x)*result.dx, tempX*0.01);
				result.setValues(tempX,tempDX);
				
				result.setDS(ds);
				result.setDX(-1);
				
				return result;

			}
	
			//when reaching here, both value and uncertainty are numerical
			int tempErrorLimit=errorLimit;
			
			//System.out.println("SDS2XDX 3479: s="+s+" "+ds+" dx="+dxu+" "+dxl);
			
			if(dxu>=0 && dxl>=0) {
				tempDX=Math.max(dxu, dxl);
				
				int d1=Integer.parseInt(dsu);
				int d2=Integer.parseInt(dsl);
				
				tempErrorLimit=Math.max(d1,d2);
			}else { 
				tempDX=dx;
	            if(dxu<=0)
	            	dxu=dx;
	            if(dxl<=0)
	            	dxl=dx;
	            
	            tempErrorLimit=Integer.parseInt(ds);
			}
			
			tempDX=tempDX/x;
					
			//tempErrorLimit=Math.min(errorLimit, 10*tempErrorLimit);
		 
			tempErrorLimit=errorLimit;
		
			SDS2XDX result=new SDS2XDX();
			result.setErrorLimit(tempErrorLimit);
			
			//System.out.println(tempErrorLimit+" "+isLargeUnc);
			
			//System.out.println("SDS2XDX 3508: tempX="+tempX+" tempDX="+tempDX+" errorLimit="+tempErrorLimit);
			
			//always use max and min approach since a 5% uncertainty in x still results in a large uncertianty in exp(x)
			double x1=Math.exp(x+dxu);
			double x2=Math.exp(x-dxl);
				
			result.setValues(tempX,x1-tempX,tempX-x2);
			
			if(result.s.contains("E") && result.dsu.length()==1 && result.dsl.length()==1) {
				result.setValues(result.x,result.dxu,result.dxl,99);
			}
			
			result=result.removeEndZeros();
			
			return result;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static SDS2XDX exp(SDS2XDX sx) {
		return sx.exp();
	}
	
	public SDS2XDX expByBase(double base) {
		try {
			
			if(!this.canGetX() || !Str.isNumeric(this.s()) || (base==0&& (x<0||x-dxl<0)) )
				return null;
					
			double tempX=-1, tempDX=-1;
			tempX=Math.pow(base,x);

			int errorLimit=this.errorLimit;
			
			//System.out.println("SDS2XDX 3436: s="+s+" "+ds);
			
			if(!Str.isNumericUnc(ds)) {
	
				SDS2XDX result=new SDS2XDX();
				
				result.setErrorLimit(9);
				result.setValues(s, "1");
				
				tempDX=Math.min(Math.pow(base,x)*result.dx, tempX*0.01);
				result.setValues(tempX,tempDX);
				
				result.setDS(ds);
				result.setDX(-1);
				
				return result;

			}
	
			//when reaching here, both value and uncertainty are numerical
			int tempErrorLimit=errorLimit;
			
			//System.out.println("SDS2XDX 3479: s="+s+" "+ds+" dx="+dxu+" "+dxl);
			
			if(dxu>=0 && dxl>=0) {
				tempDX=Math.max(dxu, dxl);
				
				int d1=Integer.parseInt(dsu);
				int d2=Integer.parseInt(dsl);
				
				tempErrorLimit=Math.max(d1,d2);
			}else { 
				tempDX=dx;
	            if(dxu<=0)
	            	dxu=dx;
	            if(dxl<=0)
	            	dxl=dx;
	            
	            tempErrorLimit=Integer.parseInt(ds);
			}
			
			tempDX=tempDX/x;
					
			//tempErrorLimit=Math.min(errorLimit, 10*tempErrorLimit);
		 
			tempErrorLimit=errorLimit;
		
			SDS2XDX result=new SDS2XDX();
			result.setErrorLimit(tempErrorLimit);
			
			//System.out.println(tempErrorLimit+" "+isLargeUnc);
			
			//System.out.println("SDS2XDX 3508: tempX="+tempX+" tempDX="+tempDX+" errorLimit="+tempErrorLimit);
			
			//always use max and min approach since a 5% uncertainty in x still results in a large uncertianty in exp(x)
			double x1=Math.pow(base,x+dxu);
			double x2=Math.pow(base,x-dxl);
				
			result.setValues(tempX,x1-tempX,tempX-x2);
			
			if(result.s.contains("E") && result.dsu.length()==1 && result.dsl.length()==1) {
				result.setValues(result.x,result.dxu,result.dxl,99);
			}
			
			result=result.removeEndZeros();
			
			return result;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
    /* check if the significant digits of result is consistent with
     * that of sx1 and sx2 which makes result by add or multiply.
     * For example,
     * ("0.822","2")X("1.0","") results ("0.8220","20"),
     * but it should be ("0.822","2")
     */
    public void checkUncertainty(SDS2XDX result,SDS2XDX sx1,SDS2XDX sx2,String operator){
        if(result==null || sx1==null || sx2==null)
            return;
        
        double x0=result.x();
        double dx0=result.dx();
        double dxu0=result.dxu();
        double dxl0=result.dxl();
        
       String ds=result.ds().trim();
        
        //If uncertainty in result has only one digit, it is good.
        if(ds.length()==1 && Str.isNumeric(ds))
            return;
               
        String s=result.s().trim();
        //if(!s.contains("."))
        //  return;
        
        /*
        //not needed, since result like 1234.5 +20-21 should not happen, since it has been
        //taken care of in setValues(x,dxu,dxl) of a XDX2SDS object which is used to
        //set the result (SDS2XDX)
        if(Str.isNumeric(s) && ds.contains("+") && ds.contains("-")) {
            XDX2SDS xs=new XDX2SDS(result.x,result.dxu,result.dxl,errorLimit);
            if(xs.canBeSymmetrized() && xs.hasSymmetrized()) {
                result.setS(xs.s());
                result.setDS(xs.ds());
            }
        }
        */
        
        String s1=sx1.ns().trim();
        String ds1=sx1.ds().trim();
        String s2=sx2.ns().trim();
        String ds2=sx2.ds().trim();
          
        if( (sx1.x==0||s1.replace(".","").replace("0","").isEmpty()) && (sx1.dx==0||ds1.equals("0")) ) {
            s1="";
            ds1="";
        }
        
        if( (sx2.x==0||s2.replace(".","").replace("0","").isEmpty()) && (sx2.dx==0||ds2.equals("0")) ) {
            s2="";
            ds2="";
        }
        
        String sign="",exps="";
        if(s.charAt(0)=='+'||s.charAt(0)=='-'){
            sign+=s.charAt(0);
            s=s.substring(1);
        }
    
        if(s.indexOf("E")>=0){
            exps=s.substring(s.indexOf("E")).trim();
            s=s.substring(0, s.indexOf("E")).trim();
        }
        

        //System.out.println("In SDS2XDX: line 1844: result.s="+result.s()+"  "+result.ds()+"  s="+s+" ds="+result.ds()+"* exps="+exps);
        
        //If uncertainty is "LT","GT","AP", "LE","GE" or empty, 
        //use max(2,min number of significant digits among sx1 and sx2) for add and subtract
        //use min(3,nsig1+nsig2)

        if(ds.length()==0 || (!Str.isNumeric(ds)&&!ds.contains("+"))){
            
            if(s.length()==0)
                return;
            
            int nsig1=Str.findNSig(s1);
            int nsig2=Str.findNSig(s2);
            int nsig=Str.findNSig(s);
            
            /*
            int max=nsig1;
            if(nsig2>max)
                max=nsig2;
            */
            
            
            int max=Math.max(2, Math.max(nsig1, nsig2));
            if(operator.equals("*")||operator.equals("/"))
            	max=Math.min(3, nsig1+nsig2);
            	
            //System.out.println("  s1="+s1+" nsig1="+nsig1+" s2="+s2+"  nsig2="+nsig2+"  s="+s+" nsig="+nsig+"  max="+max+" Math.min(nsig1, nsig2)="+Math.min(nsig1, nsig2));
            
            if(nsig<=max)
                return;
            
            
            
            if(Str.isNumeric(s)){               
                int power=0;
                if(s.indexOf(".")>=0)
                	power=s.length()-s.indexOf('.')-1;
                
                power-=(nsig-max);
                if(power<0)
                    power=0;
                
                if(power>0) {
                    int v=(int) (Double.valueOf(s)*Math.pow(10.0, power)+0.55f);
                    s=String.valueOf(v);
                    int idot=s.length()-power;
                    
                    //System.out.println(" power="+power+" idot="+idot+" v="+v+" s="+s+" nsig="+nsig+" max="+max);
                    
                    if(idot==0)
                        s="0."+s;
                    else if(idot>0 && idot<s.length())
                        s=s.substring(0,idot)+"."+s.substring(idot);
                    else if(idot<0){//should never happen
                        for(int i=0;i<-idot;i++)
                            s='0'+s;
                        
                        s="0."+s;
                    }
                }
            }
            
            s=sign+s+exps;
            
            //System.out.println("In SDS2XDX: line 3496: result.s="+result.s()+"  "+result.ds()+"  sign="+sign+" s="+s+" exps="+exps);
            //System.out.println("  s1="+s1+" nsig1="+nsig1+" s2="+s2+"  nsig2="+nsig2+"  nsig="+nsig);
            
            result.setS(s);
            return;
        }
        
        if(s.indexOf("E")>0 && !s1.contains("E") && !s2.contains("E"))
            return;
        
        //System.out.println("SDS2XDX 1909: #1 s="+s+" ds="+ds+" s1="+s1+" ds1="+ds1+" s2="+s2+" ds2="+ds2+" exps="+exps);
        
        int n1=ds1.length();
        int n2=ds2.length();
        if((n1+n2)==0)
            return;
        
        boolean isNum1=Str.isNumeric(ds1);
        boolean isNum2=Str.isNumeric(ds2);
        
        //System.out.println("SDS2XDX 1793: result: "+result.s+" "+result.ds+" s="+s+" ds="+ds+"  s1="+s1+" "+ds1+" n1="+n1+" s2="+s2+" "+ds2+" n2="+n2+" errorLimit="+errorLimit);
        

        if((Str.isNumeric(ds)||(Str.isNumeric(result.dsu)&&Str.isNumeric(result.dsl)) )) {
        	
        	if(ds.length()>1 && n1>0 && n2>0 && (sx1.isKeepSameUncNDigit||sx2.isKeepSameUncNDigit)) {
                int nd=0,nd1=0,nd2=0;
                if(sx1.isKeepSameUncNDigit && Str.isNumericUnc(ds1)) {
                	if(Str.isNumeric(ds1)) {
                		nd1=ds1.length();
                	}else {
                		nd1=Math.max(sx1.dsu.length(),sx1.dsl.length());
                	}
                }
                
                if(sx2.isKeepSameUncNDigit && Str.isNumericUnc(ds2)) {
                	if(Str.isNumeric(ds1)) {
                		nd2=ds1.length();
                	}else {
                		nd2=Math.max(sx1.dsu.length(),sx1.dsl.length());
                	}
                }
                
                //System.out.println("SDS2XDX 4147: nd1="+nd1+" nd2="+nd2+"  ds1="+ds1+"  ds2="+ds2+"  "+sx1.isKeepSameUncNDigit); 
                
                if(nd1>0 && nd2>0) {
                	nd=Math.min(nd1, nd2);
                }else {
                	nd=Math.max(nd1, nd2);
                }
                
                if(nd==1) {
                	SDS2XDX sx=result.roundToOneDigitUnc();
                	result.setS(sx.s);
                	result.setDS(sx.ds);
                	return;
                }
        	}else if(sx1.isKeepSameNDAfterDot||sx2.isKeepSameNDAfterDot){
        		int nd1=-1;
        		int nd2=-1;
        		
        		if(sx1.isKeepSameNDAfterDot)
        			nd1=sx1.getNDigitsAfterDot();
        		if(sx2.isKeepSameNDAfterDot)
        			nd2=sx2.getNDigitsAfterDot();
        		
        		int nd=-1;
        		if(nd1>=0 && nd2>=0) {
        			nd=Math.min(nd1, nd2);
        		}else if(nd1>=0) {
        			nd=nd1;
        		}else if(nd2>=0) {
        			nd=nd2;
        		}
        		
        		if(nd>=0 && result.getNDigitsAfterDot()>nd) {
        			SDS2XDX sx=result.roundToNDigitsAfterDot(nd);
                	result.setS(sx.s);
                	result.setDS(sx.ds);
                	return;
        		}
        	}


        }

        
        if( (n1>0 &&!isNum1) || (n2>0&&!isNum2) ) {
            if(ds.endsWith("0") && ds.length()>1 && s.endsWith("0") && s.contains(".") ) {

                String ds11=ds1.replace("+", "").replace("-", "").trim();
                String ds22=ds2.replace("+", "").replace("-", "").trim();
                
                boolean done=false;
                if(!isNum1 && Str.isNumeric(ds11)) {
                    String[] ds1V=ds1.split("[+-]");
                    if(ds1V.length!=3)
                        return;
                    
                    String tempDS="",tempDS1="";
                    if(ds1.indexOf("+")<ds1.indexOf("-")) {
                        tempDS="+"+ds1V[1]+"0-"+ds1V[2]+"0";
                        tempDS1="+"+ds1V[1]+"-"+ds1V[2];
                    }else {
                        tempDS="+"+ds1V[2]+"0-"+ds1V[1]+"0";
                        tempDS1="+"+ds1V[2]+"-"+ds1V[1];
                    }
                    
                    if(tempDS.equals(ds)) {
                        s=s.substring(0,s.length()-1);
                        if(s.endsWith("."))
                        	s=s.substring(0,s.length()-1);
                        
                        s=sign+s+exps;
                        
                        ds=tempDS1;
                        result.setValues(s,ds);
                        done=true;
                    }
     
                }
                //System.out.println("SDS2XDX 1829: result: "+result.s+" "+result.ds+" s="+s+" ds="+ds+"  s1="+s1+" "+ds1+" n1="+n1+" s2="+s2+" "+ds2+" n2="+n2);
                
                if(!done && !isNum2 && Str.isNumeric(ds22)) {
                    String[] ds2V=ds2.split("[+-]");
                    if(ds2V.length!=3)
                        return;
                    
                    String tempDS="",tempDS1="";
                    if(ds2.indexOf("+")<ds2.indexOf("-")) {
                        tempDS="+"+ds2V[1]+"0-"+ds2V[2]+"0";
                        tempDS1="+"+ds2V[1]+"-"+ds2V[2];
                    }else {
                        tempDS="+"+ds2V[2]+"0-"+ds2V[1]+"0";
                        tempDS1="+"+ds2V[2]+"-"+ds2V[1];
                    }
                    
                    if(tempDS.equals(ds)) {
                        s=s.substring(0,s.length()-1);
                        if(s.endsWith("."))
                        	s=s.substring(0,s.length()-1);
                        
                        s=sign+s+exps;
                        
                        ds=tempDS1;
                        result.setValues(s,ds);
                    }

                }
                
                
            }

            
            /*
            if(!Str.isNumeric(ds) && Str.isNumeric(dss)) {
                String[] dsV=ds.split("[+-]+");
                if(dsV.length!=3)
                    return;
                
                SDS2XDX resultA=new SDS2XDX(s,dsV[1]);
                SDS2XDX resultB=new SDS2XDX(s,dsV[2]);

                checkUncertainty(resultA, sx1, sx2, operator);
                checkUncertainty(resultB, sx1, sx2, operator);
                
                //System.out.println("ds="+ds+" "+dsV.length+" resultA="+resultA.ds+"  resultB="+resultB.ds);
                
                if(resultA.s().equals(resultB.s())) {
                    if(ds.indexOf("+")>ds.indexOf("-"))
                        result.setValues(resultA.s,"+"+resultA.ds+"-"+resultB.ds);
                    else
                        result.setValues(resultA.s,"+"+resultB.ds+"-"+resultA.ds);
                }
                
            }else if(!isNum1 && Str.isNumeric(ds11)) {
                String[] ds1V=ds1.split("[+-]");
                if(ds1V.length!=3)
                    return;
                
                SDS2XDX sx1A=new SDS2XDX(s1,ds1V[1]);
                SDS2XDX sx1B=new SDS2XDX(s1,ds1V[2]);
                
                SDS2XDX resultA=result.clone();         
                SDS2XDX resultB=result.clone();  
                
                //System.out.println("#1 s="+s+" ds="+ds+" s1="+s1+" ds1="+ds1+" s2="+s2+" ds2="+ds2+" result="+result.s+" "+result.ds+" resultA="+resultA.s+"  "+resultA.ds+
                //      " resultB="+resultB.s+" "+resultB.ds+" sxA="+sx1A.ds+" limitA="+sx1A.getErrorLimit()+"  sxB="+sx1B.ds+"  limitB="+sx1B.getErrorLimit());
                
                checkUncertainty(resultA, sx1A, sx2, operator);
                checkUncertainty(resultB, sx1B, sx2, operator);
                
                if(resultA.equals(resultB))
                    result.setValues(resultA.s,resultA.ds);
                
                //System.out.println("#2 s="+s+" ds="+ds+" s1="+s1+" ds1="+ds1+" s2="+s2+" ds2="+ds2+" result="+result.s+" "+result.ds+" resultA="+resultA.s+"  "+resultA.ds+
                //      " resultB="+resultB.s+" "+resultB.ds+" sxA="+sx1A.ds+" limitA="+sx1A.getErrorLimit()+"  sxB="+sx1B.ds+"  limitB="+sx1B.getErrorLimit());
                
            }else if(!isNum2 && Str.isNumeric(ds22)) {
                String[] ds2V=ds2.split("[+-]");
                if(ds2V.length!=3)
                    return;
                
                SDS2XDX sx2A=new SDS2XDX(s2,ds2V[1]);
                SDS2XDX sx2B=new SDS2XDX(s2,ds2V[2]);
                
                SDS2XDX resultA=result.clone();         
                SDS2XDX resultB=result.clone();  
                checkUncertainty(resultA, sx1,sx2A, operator);
                checkUncertainty(resultB, sx1,sx2B, operator);
                
                if(resultA.equals(resultB))
                    result.setValues(resultA.s,resultA.ds);
                
            }
            */
            //System.out.println("SDS2XDX 1914: result: "+result.s+" "+result.ds+" s1="+s1+" "+ds1+" n1="+n1+" s2="+s2+" "+ds2+" n2="+n2);
            
            result.setX(x0);
            if(dx0>0) result.setDX(dx0);
            if(dxu0>0) result.setDX(dxu0,dxl0);
            
            return;
        }
        
        //debug
        //System.out.println("SDS2XDX 2060:  ###2 sign="+sign+" s="+s+" ds="+ds+"   sx1 ds="+sx1.ds+" sx2 ds="+sx2.ds+"  n1="+n1+" n2="+n2+" result="+result.s+" "+result.ds+" operator="+operator);
        
        //for case like 1.58(2)*0.981=1.550(20), but should be 1.55(2)
        //          but 1.58(2)*0.981(1)=1.550(20) is considered OK (for now)
        if(ds.length()>1 && ds.endsWith("0") && s.indexOf(".")>0) {
            String tds1=ds1.replace("0", "");
            String tds2=ds2.replace("0", "");
            
            double dx1=sx1.dx();
            double dx2=sx2.dx();
            double dx=result.dx();
            
            //System.out.println("SDS2XDX 3347:  ###1 sign="+sign+" s="+s+" ds="+ds+"  tds1="+tds1+" tds2="+tds2+"  ds1="+ds1+" ds2="+ds2);
            //System.out.println("   dx="+dx+" dx1="+dx1+"  dx2="+dx2);
            
            if((tds1.isEmpty()&&ds.equals(ds2+"0")&&Math.abs(dx-dx1)<0.01*dx) 
            		|| (tds2.isEmpty()&&ds.equals(ds1+"0")&&Math.abs(dx-dx2)<0.001*dx)){
                ds=ds.substring(0,ds.length()-1);
                
                //System.out.println("SDS2XDX 3352:  ###2 sign="+sign+" s="+s+" ds="+ds+"  tds1="+tds1+" tds2="+tds2+"  ds1="+ds1+" ds2="+ds2);
                
                //round up by one digit (rounding offset=0.55 to round up xx.45 but still round down xx.44)
                s=Str.roundUpByNDigits(s,1,0.55f);
                
                result.setValues(sign+s+exps, ds);              
                
                result.setX(x0);
                if(dx0>0) result.setDX(dx0);
                if(dxu0>0) result.setDX(dxu0,dxl0);
                
                return;
            }
        }
        
        if(!s.contains("."))
        	return;
        
        //System.out.println(x0+"  "+result.s+" "+ds);

        if(ds.charAt(ds.length()-1)!='0'||s.charAt(s.length()-1)!='0') {
            if(ds.length()==2 && (operator.equals("+")||operator.equals("-")) ) {
                int nd1=Str.findNDigitsAfterDot(s1);
                int nd2=Str.findNDigitsAfterDot(s2);
                if((nd1+nd2)>=0) {
                    int nd=nd1;
                    if(nd1>nd2) {
                    	if(s2.isEmpty()) {
                    		nd=nd1;
                    	}else {
                    		nd=nd2;
                    	}
                    }else if(nd1<nd2) {
                    	if(s1.isEmpty()) {
                    		nd=nd2;
                    	}else {
                    		nd=nd1;
                    	}
                    		
                    }
                    /*
                    if(nd1>nd2 && (!s2.isEmpty()||!ds2.isEmpty()) ){//123.43(5) - 0.2(1)
                        //nd=nd2;
                    }else if(nd1<nd2 && s1.isEmpty() && ds1.isEmpty()){//0 - 123.4(5)
                    	nd=nd2;
                    }
                    */
                    //System.out.println(result.s+"  "+result.ds+" nd="+nd);
                    
                    SDS2XDX temp=checkUncertainty(sign+s+exps, ds, nd,errorLimit);
                    result.setValues(temp);
                    
                    //System.out.println("SDS2XDX 2115: s1="+s1+"  s2="+s2+" nd1="+nd1+" nd2="+nd2+" nd="+nd+"  errorLimit="+errorLimit);
                    //System.out.println("  ###2.5 n1="+n1+" n2="+n2+" s="+s+" ds="+ds+"   sx1="+sx1.ds+" sx2="+sx2.ds+"  nd1="+nd1+" nd2="+nd2+" result="+result.s+" "+result.ds+" operator="+operator);

                }else if(nd1==0 && nd2==0) {
                	
                }
                
            }

            return;
        }
        
        //System.out.println("  ###3 n1="+n1+" n2="+n2+" s="+s+" ds="+ds+"   sx1 ds="+sx1.ds+" sx2 ds="+sx2.ds+"  n1="+n1+" n2="+n2+" result="+result.s+" "+result.ds+" operator="+operator);
        
        boolean removeZero=false;
        
        if(sx1.x()==0 || sx2.x()==0){
            if(operator.equals("*")||operator.equals("/"))
                return;
            else if(operator.equals("+")||operator.equals("-")){
                if(sx1.x()!=0){
                    result.setValues(sx1);;
                    return;
                }
                if(sx2.x()!=0){
                    result.setValues(sx2);
                    return;
                }
            }
        }

        if(n1>1||n2>1){
            if(operator.equals("*")||operator.equals("/"))
                return;
            else if(operator.equals("+")||operator.equals("-")){
                if((n1>1 && sx1.dx()>sx2.dx()) || (n2>1 &&sx2.dx()>sx1.dx()))
                    return;
                else
                    removeZero=true;
            }
        }
        //System.out.println("  ###4 s="+s+" ds="+ds+"   sx1="+sx1.ds+" sx2="+sx2.ds+"  n1="+n1+" n2="+n2+" result="+result.s+" "+result.ds+" operator="+operator);
        
        //here, ds has two digits, ds1 and ds2 have one or zero digit       
              
        //if multiplying or dividing operation increase either uncertainty by one-digit increment,
        //e.g., 2.3(3)*5.1(2), dx1=3 multiplying x2=5.1 increments the uncertainty by one-digit
        //so for this case, keep ending zero if there is any
        //but for case like 2.3(3)*3.1(2), keeps the same number of significant digits as those of input, 
        //here it is one, so remove ending zeros if there is any to keep only one digit in uncertainty
        
        if(n1+n2==1){//one of ds1 and ds2 has uncertainty
            if(operator.equals("+")||operator.equals("-")) {
                removeZero=true;
            }else if(operator.equals("*")||operator.equals("/")){
                Float x,dx;
                if(n1==1){
                    dx=Float.parseFloat(ds1);
                    x=Float.parseFloat(s2);                             
                }else{
                    dx=Float.parseFloat(ds2);
                    x=Float.parseFloat(s1);  
                }
                
                //debug
                //System.out.println("In SDS2XDS: s1,ds1,s2,ds2="+s1+" "+ds1+" "+s2+" "+ds2+" n1,n2 "+n1+" "+n2+" x,dx "+x+" "+dx);
                
                if(operator.equals("*")) {//zero at the end of s and ds is kept
                    double b=findExpBase(x);
                    if(Math.abs(b*dx)>=10)
                        return;
                }else if(operator.equals("/")){
                    if(n1==1&& (Math.abs(dx/x))>=10)
                        return;
                    else if(n2==1 && Math.abs(x/sx2.x()*dx/sx2.x())>=10)
                        return;
                    else 
                        removeZero=true;                                        
                }
                else
                    removeZero=true;
            }
        }else{//n1=1,n2=1
            if(operator.equals("+")||operator.equals("-"))
                removeZero=true;
            else if(operator.equals("*")){
                
                double b1=sx1.x();
                double b2=sx2.x();
                
     
                //b1=findExpBase(sx1.x());                          
                //b2=findExpBase(sx2.x());
                //System.out.println("  ###3 s="+s+" ds="+ds+"  s1="+sx1.s+" dsx1="+sx1.ds+" s2="+sx2.s+" dsx2="+sx2.ds+"  n1="+n1+" n2="+n2+" result="+result.s+" "+result.ds+" b1="+b1+" b2="+b2);

                if(Math.abs(Float.parseFloat(ds1)*b2)>=10 || Math.abs(Float.parseFloat(ds2)*b1)>=10)
                    return;
                else {
                    int ns1=Str.findNSig(sx1.s);
                    int ns2=Str.findNSig(sx2.s);
                    int ns=Str.findNSig(s);
                    if(ns>ns1+1 && ns>ns2+1)
                    	removeZero=true;
                }
            }else if(operator.equals("/")){
                if(Math.abs(sx1.x()/sx2.x()*Float.parseFloat(ds2)/sx2.x())>=10 || Math.abs(sx2.x()/sx1.x()*Float.parseFloat(ds1)/sx1.x())>=10)
                    return;
                else
                    removeZero=true;
            }
        }

        
        if(removeZero){
            s=s.substring(0,s.length()-1);
            ds=ds.substring(0,ds.length()-1);
            
            //System.out.println("  ###5 s="+s+" ds="+ds+"   sx1="+sx1.ds+" sx2="+sx2.ds+"  n1="+n1+" n2="+n2+" result="+result.s+" "+result.ds+" operator="+operator+" "+removeZero);
            
            if(s.endsWith(".")){
                s=s.substring(0,s.length()-1);
                //ds=ds.substring(0,ds.length()-1);
            }
            
            result.setValues(sign+s+exps, ds);
            
            result.setX(x0);
            if(dx0>0) result.setDX(dx0);
            if(dxu0>0) result.setDX(dxu0,dxl0);
        }
        
        
    }

    private double findExpBase(double x) {
        return Str.findExpBase(String.valueOf(x));
    }
    
    public int getNDigitsAfterDot() {
    	int n=Str.findNDigitsAfterDot(s);
    	return n;
    }
    
    //convert ENSDF-style DS to real-format string
    public String getRealDS() {
    	try {
    		return getRealDS(s,ds);
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    public String getRealDSU() {
    	try {
    		if(!Str.isNumeric(dsu))
    			return "";
    		
    		return getRealDS(s,dsu);
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    public String getRealDSL() {
    	try {
    		if(!Str.isNumeric(dsl))
    			return "";
    		
    		return getRealDS(s,dsl);
    	}catch(Exception e) {
    		
    	}
    	return "";
    }
    
    //ds must be in ENSDF-style format
    public static String getRealDS(String s, String ds) {
    	String out="";
    	try {
    		if(!Str.isNumericUnc(ds))
    			return "";
    		
    		String baseS=Str.getBaseOfExpo(s);
    		int power=Str.getNPowerOfExpo(s);
    		if(baseS.isEmpty())
    			baseS=s;
    		
    		int n=Str.findNDigitsAfterDot(baseS);//already takes into account the power of expo

    		
    		//System.out.println(s+"  "+n+" n="+n+" power="+power+" baseS="+baseS);
    		n=power-n;
    		
    		out=ds;
    		if(ds.contains("+")) {
    			SDS2XDX sx=new SDS2XDX(s,ds);
    			String realDSU=sx.dsu;
    			String realDSL=sx.dsl;
    			if(n>0) {
    				realDSU=Str.shiftDecimalPoint(realDSU,n,"RIGHT");
    				realDSL=Str.shiftDecimalPoint(realDSL,n,"RIGHT");
    			}else if(n<0) {
    				realDSU=Str.shiftDecimalPoint(realDSU,-n,"LEFT");
    				realDSL=Str.shiftDecimalPoint(realDSL,-n,"LEFT");
    			}else {
    				return out;
    			}
    			
    			out="+"+realDSU+"-"+realDSL;
    		}else {
    			if(n>0)
    				out=Str.shiftDecimalPoint(ds,n,"RIGHT");
    			else if(n<0)
    				out=Str.shiftDecimalPoint(ds,-n,"LEFT");
    			else
    				return out;
    		}
    		
    		System.out.println(s+"  "+n+" n="+n+" power="+power+" baseS="+baseS+" out="+out);
    	}catch(Exception e) {
    		
    	}
    	return out;
    }
    
    public static SDS2XDX checkUncertainty(String s,String ds,int errorLimit) {
        SDS2XDX s2x=null;
        try{
            s2x=new SDS2XDX();
            s2x.setErrorLimit(errorLimit);
            s2x.setValues(s, ds);
            
            if(!ds.isEmpty()) {
                if(s2x.dxu()>0 && s2x.dxl()>0) {
                    int du=Integer.parseInt(s2x.dsu());
                    int dl=Integer.parseInt(s2x.dsl());
                    if(du>errorLimit || dl>errorLimit) {                          
                        XDX2SDS xs=new XDX2SDS(s2x.x(),s2x.dxu(),s2x.dxl(),errorLimit);                              
                        s2x.setValues(xs);
                    }

                }
            } 
        }catch(Exception e) {
            
        }

        return s2x;
    }
    
    public static SDS2XDX checkUncertainty(String s,String ds,int maxNDigitsAfterDot,int errorLimit){
        SDS2XDX s2x=null;
        try{
            s2x=checkUncertainty(s,ds,errorLimit);
            
            //System.out.println(" SDS2XDX 2261: original s="+s+" ds="+s+" after check 1: s="+s2x.s+" ds="+s2x.ds+" maxNDigitsAfterDot="+maxNDigitsAfterDot);
            
            int idot=s.indexOf(".");
            if(idot<0 || idot==(s.length()-1) || maxNDigitsAfterDot<0)
                return s2x;
            
            int n=Str.findNDigitsAfterDot(s);
            
            n=n-maxNDigitsAfterDot;
            if(n<=0)
                return s2x;
     
            //offset=0.55 is set so that, rounding following the rule:
            //a value<1.0-0.55=0.45 is rounded down 
            //a value>=0.45 is rounded up 
            //e.g., 0.44 is rounded down to 0.4 by 1 digit off, to 0 by 2 digits off
            //      0.45 is rounded up to 0.5 by 1 digit off, to 1 by 2 digits off
            //if offset=0, rounding is by default (build-in rounding), which is 
            //equivalent to set offset=0.5, so that 0.499 will be rounded down and 0.500
            //will be rounded up
            s=Str.roundUpByNDigits(s,n, 0.55f);
  
            if(Str.isNumeric(ds)) {
                double dx=Integer.parseInt(ds);
                //int intUnc=(int)(dx/Math.pow(10,n)+0.655);//add 0.655 to round up uncertainty like x.345
               
                int intUnc=(int)Str.roundUncertainty(dx/Math.pow(10,n));
                if(intUnc<1)//uncertainty cannot be rounded up, return original value
                    return s2x;
                
                ds=String.valueOf(intUnc);
            }else if(ds.contains("+")&& ds.contains("-")) {
                String dsu0=s2x.dsu();
                String dsl0=s2x.dsl();
                double dx1=Integer.parseInt(dsu0);
                
                //int intUnc1=(int)(dx1/Math.pow(10,n)+0.655);//add 0.655 to round up uncertainty like x.345
                int intUnc1=(int)Str.roundUncertainty(dx1/Math.pow(10,n));
                
                if(intUnc1<1)//uncertainty cannot be rounded up, return original value
                    return s2x;
                dsu0=String.valueOf(intUnc1);
                
                double dx2=Integer.parseInt(dsl0);
                //int intUnc2=(int)(dx2/Math.pow(10,n)+0.655);//add 0.655 to round up uncertainty like x.345
                int intUnc2=(int)Str.roundUncertainty(dx2/Math.pow(10,n));
                
                if(intUnc2<1)//uncertainty cannot be rounded up, return original value
                    return s2x;
                
                dsl0=String.valueOf(intUnc2);
                
                ds="+"+dsu0+"-"+dsl0;
            }else if(!ds.isEmpty())
                return s2x;

            s2x.setValues(s,ds);
            
            //System.out.println(" SDS2XDX 2312: original s="+s+" ds="+s+" after check 2: s="+s2x.s+" ds="+s2x.ds+" maxNDigitsAfterDot="+maxNDigitsAfterDot);
            
        }catch(Exception e){
            e.printStackTrace();
        }

        return s2x;
    }
   
    
    public int maxUncNDigit() {
    	if(ds.isEmpty())
    		return 0;
    	if(Str.isLetters(ds))
    		return -1;
    	
    	if(Str.isNumeric(ds)) {
    		return ds.length();
    	}else if(Str.isNumeric(dsu)||Str.isNumeric(dsl)) {
    		int n1=-1,n2=-1;
    		if(Str.isNumeric(dsu))
    			n1=dsu.length();
    		else if(dsu.isEmpty())
    			n1=0;
    		
    		if(Str.isNumeric(dsl))
    			n2=dsl.length();
    		else if(dsl.isEmpty())
    			n2=0;
    		
    		
    		return Math.max(n1, n2);
    	}
    	
    	return -1;
    }
    
    public int minUncNDigit() {
    	if(ds.isEmpty())
    		return 0;
    	if(Str.isLetters(ds))
    		return -1;
    	
    	if(Str.isNumeric(ds)) {
    		return ds.length();
    	}else if(Str.isNumeric(dsu)||Str.isNumeric(dsl)) {
    		int n1=-1,n2=-1;
    		if(Str.isNumeric(dsu))
    			n1=dsu.length();
    		else if(dsu.isEmpty())
    			n1=0;
    		
    		if(Str.isNumeric(dsl))
    			n2=dsl.length();
    		else if(dsl.isEmpty())
    			n2=0;
    		
    		
    		return Math.min(n1, n2);
    	}
    	
    	return -1;
    }
    //same as the function of XDX2SDS
    public double[] symmetrize(double x,double dxu,double dxl){
          double[] out=new double[2];
          out[0]=x;out[1]=dxu;
          if(dxu>0 && dxl>0 && dxu!=dxl){
              double f=(1-2.0/Math.PI)*(dxu-dxl)*(dxu-dxl)+dxu*dxl;
              out[0]=x+0.64*(dxu-dxl);
              out[1]=Math.sqrt(f);
          }
          
          return out;     
    }
    public String printAsComment() {
    	return printAsComment("");
    }
    
    public String printAsComment(String name) {
    	return printAsComment(name,"");
    }
    public String printAsComment(String name,String unit) {
		try {							
			String tempS=EnsdfUtil.printValueAsComment(name,s,ds,unit);
			return tempS;
		}catch(Exception e) {
			
		}		
		return "";
    }
    
    public String printAsContinuation() {
    	return printAsContinuation("","");
    }
    public String printAsContinuation(String name) {
    	return printAsContinuation(name,"");
    }
    public String printAsContinuation(String name,String unit) {
		try {							
			String tempS=EnsdfUtil.printValueAsContinuation(name,s,ds,unit);
			return tempS;
		}catch(Exception e) {
			
		}		
		return "";
    }
    
  public int nexp(){return nexp;}
  public String exps(){return exps;}
  public double baseX(){return basex;}
  public double basex(){return basex;}
  public String bases(){return bases;}
  public String baseS(){return bases;}
  
  
  public String s(){return s;}  
  public String ds(){return ds;}
  public String ds0(){return ds0;}
  
  public String ns0(){return ns0;} //note that for 1.3E3+X ns0=1.3 not 1.3E3
  public String ns(){return ns;}  
  public String nns(){return nns;}
  
  public double X(){ return x;}
  public double DX(){ return dx; }
  public double XUV(){ return x_uv_d;}
  public double XLV(){ return x_lv_d;}
  public double XS(){ return sx;}   /* symmetric value */
  public double DXS(){ return dsx; } /* symmetric error */
  public float Xf(){ return (float) x;}
  public float DXf(){ return (float) dx; }
  public float XUVf(){ return (float) x_uv_d;}
  public float XLVf(){ return (float) x_lv_d;}
  public float XSf(){ return (float) sx;}
  public float DXSf(){ return (float) dsx; }

  public boolean canGetX(){ return bx;}
  public boolean canGetDX(){ return bdx;}
  public boolean canGetXUV(){ return bx_uv_d;}
  public boolean canGetXLV(){ return bx_lv_d;}
  public boolean canGetXS(){ return bsx;}
  public boolean canGetDXS(){ return bdsx;}

  public boolean dsIsDigit() {return dsIsDigit;}
  public boolean dsIsAsymm() {return dsIsAsymm;}
  
  public boolean isLimits() {return isLimit;}
  
  public void setDefaultUncParser() {uncParser=(s,ds)->UncertaintyParser.findParsedXDX(s,ds);}
  public void setUncParser(UncParser up) {uncParser=up;}
  public UncParser uncParser() {return uncParser;}
  
  public String S(){return s;}  
  public String DS(){return ds;}
  public String dsu() {return dsu;}
  public String dsl() {return dsl;}
  
  public String realDS(){return real_ds;}
  public String realDSU() {return real_dsu;}
  public String realDSL() {return real_dsl;}
  
  public double x(){ return x;}

  public double dx(){ return dx; }
  public double dxu(){ return dxu; }
  public double dxl(){ return dxl; }
  public double xUV(){ return x_uv_d;}
  public double xLV(){ return x_lv_d;}
  public double xSym(){ return sx;}   /* symmetric value */
  public double dxSym(){ return dsx; } /* symmetric error */
  public float xf(){ return (float) x;}
  public float dxf(){ return (float) dx; }
  public float xUVf(){ return (float) x_uv_d;}
  public float xLVf(){ return (float) x_lv_d;}
  public float xSymf(){ return (float) sx;}
  public float dxSymf(){ return (float) dsx; }

  public double x0() {return x0;}
  public double dx0(){ return dx0; }
  public double dxu0(){ return dxu0; }
  public double dxl0(){ return dxl0; }
  
  public void setS(String es){
	  s=es;
	  if(Str.isNumeric(es))
		  bx=true;	
  }
  public void setDS(String des){
	  ds=des;
	  if(Str.isInteger(des)&& Integer.parseInt(des)>=0)
		  bdx=true;			  
  }
  
  public void setDS0(String des){
	  ds0=des;
  }
  
  public void setX0(double xd0){
	  x0=xd0;
  }
  
  public void setX(double xd){
	  x=xd;
	  bx=true;
  }
  
  public void setDX(double dxd){
	  dx=dxd;dxu=dxl=dx;
	  if(dx>=0)
		  bdx=true;
	  else
		  bdx=false;
  }
  
  public void setDX(double dxu0,double dxl0){
	  dxu=dxu0;dxl=dxl0;	 
	  if(dxu>=0 && dxl>=0) {
		  if(Math.abs(dxu-dxl)<0.01*Math.max(dxu0, dxl))
			  bdx=true;
	  }else
		  bdx=false;
  }
  
  public void setXF(float xf){setX(xf);}
  public void setDXF(float dxf){setDX(dxf);}
  public void setDXF(float dxu0,float dxl0){setDX(dxu0,dxl0);}
  
  public int getNsig() {return nsig;}
  public void setNsig(int n) {
	  if(n<=0)
		  nsig=defaultMaxNsigma;
	  else
		  nsig=n;
  }
  
  public String ID(){return id;}
  public void setID(String name){id=name;}

  public String ref(){return ref;}
  public void setRef(String name){ref=name;}
  
  public void setErrorLimit(int limit){
    try{
       
    	if(limit<1)       
        	errorLimit=defaultErrorLimit;
        else       
        	errorLimit=limit;
    }catch(Exception e){}
  } 
  
  public int getErrorLimit(){return errorLimit;}   

	 
	
  public String toString() {	 
	if(strRepr.isEmpty()) {		
		return !ds.isEmpty()?(s+"("+ds+")"):s;
	}else {
	    return strRepr;
	}
  }
	
  public String sds() {	 
	  return !ds.isEmpty()?(s+"("+ds+")"):s;
  }
  
  /*
   * show the string representation of all operations to get the final number
   * show sds itself if there is no operatioin
   */
  public String strRepr() {	 
	  return toString();
  }
  
  public static void setExpFormatLimitSmallNumber(double d) {
	  if(d<=0)
		  return;
	  
	  expFormatLimitSmallNumber=d;
	  XDX2SDS.setExpFormatLimitSmallNumber(d);
  }
  public static void setSmallError(double d){
	    try{
	        if(d<=0)
	          smallError=0.10;
	        else
	          smallError=d;
	    }catch(Exception e){}
	  } 
	
  //only need to be set once when the program is still running
  public static void setDefaultMaxNsigma(int nsig) {
	    try{
	        if(nsig<=0)
	          defaultMaxNsigma=3;
	        else
	          defaultMaxNsigma=nsig;
	    }catch(Exception e){}
  }
  public static void setNoMaxNsigmaLimit() {defaultMaxNsigma=1000;}
  
  public static void resetSmallError() {smallError=0.1;}
  public static void resetDefaultMaxNsigma() {defaultMaxNsigma=3;}
  
  public static double getSmallError(){return smallError;}  
  public static int getDefaultMaxNsigma(){return defaultMaxNsigma;}  
  
  
  //if isENSDFStyleUnc=false, input uncertainty is in real-value format
  //otherwise, it is in ENSDF-style format
  public static SDS2XDX parse(String s,String ds,boolean isENSDFStyleUnc) {
      SDS2XDX sx=null;
      if(s.isEmpty())
          return null;
                  
      ds=ds.toUpperCase();
      if(isENSDFStyleUnc) {
          sx=new SDS2XDX(s,ds);
          sx.setErrorLimit(99);
          return sx;
      }
      
      //real-value style uncertainty
      try {
          double x=Double.parseDouble(s);
          if(!Str.isNumeric(ds)) {
              if(ds.contains("+") && ds.contains("-") && (ds.startsWith("+")||ds.startsWith("-"))) {
                  String ds1=ds.substring(1).trim();
                  String[] temp=ds1.split("[+-]");
                  if(temp.length==2 && Str.isNumeric(temp[0]) && Str.isNumeric(temp[1])) {
                      double dxu=Double.parseDouble(temp[0]);
                      double dxl=Double.parseDouble(temp[0]);
                      if(ds.startsWith("-")) {
                          double tempDX=dxu;
                          dxu=dxl;
                          dxl=tempDX;
                      }
                      sx=new SDS2XDX();
                      sx.setValues(x, dxu, dxl, 99);
                  }              
              }else if(ds.isEmpty() || (ds.length()==2 && "LT,LE,GT,GE,AP,SY".contains(ds) && !ds.contains(",")) ){
                  sx=new SDS2XDX(s,ds);
              }
          }else {
              double dx=Double.parseDouble(ds);
              sx=new SDS2XDX();
              sx.setValues(x, dx, 99);
          }
      }catch(Exception e) {
          
      }
      return sx;
  }
  
  //parse string like "1.2(3)", "1.2 3", "1.2 {I3}"
  public static SDS2XDX parse(String str) {
	  try {

		  String s=str.replace("(", " ").replace(")","").replace("{I"," ").replace("}","");
		  
		  s=s.replace("["," ").replace("]","").replace("{"," ");
		  String[] temp=s.split("[\\s]+");
		  
		  if(temp.length>2 || temp.length==0)
			  return null;
		  
		  s=temp[0];
		  
		  String ds="";
		  if(temp.length==2) 
			  ds=temp[1];
		  
		  if(Str.isNumeric(s) && (ds.isEmpty()) || Str.isNumericUnc(ds)){
			  SDS2XDX sx=new SDS2XDX(s,ds);
			  return sx;
		  }

	  }catch(Exception e) {
		  
	  }
	  
	  return null;
  }
  
  public SDS2XDX clone() {
        SDS2XDX resultA=new SDS2XDX();
        resultA.setErrorLimit(errorLimit);

        resultA.setValues(this);
        resultA.id=id;
        resultA.ref=ref;
        
        return resultA;
  }
  
  /*
   * exact the same
   */
  public boolean equals(SDS2XDX sx) {
      if(sx!=null && s.equals(sx.s) && ds.equals(sx.ds) && x==sx.x() && dx==sx.dx() && dxu==sx.dxu() && dxl==sx.dxl() && errorLimit==sx.getErrorLimit())
          return true;
      
      return false;
  }

  /*
   * same number, others like format may be different, like 5270(3) vs 5.270E3(3)
   */
  public boolean isSameNumberAs(SDS2XDX sx) {
  	try {
  		
		String s1=this.s().toUpperCase();
		String ds1=this.ds().toUpperCase();
		String s2=sx.s().toUpperCase();
		String ds2=this.ds().toUpperCase();
		
		if(s1.isEmpty() || s2.isEmpty())
			return false;
		
		if(Str.isSameNumber(s1, s2) && ds1.equals(ds2))
			return true;
				
	}catch(Exception e) {
		
	}
	return false;
  }
  
  public boolean isKeepSameUncNDigit() {return isKeepSameUncNDigit;}
  public void setIsKeepSameUncNDigit(boolean b) {
	  isKeepSameUncNDigit=b;
	  if(b) {
		  isKeepSameNDAfterDot=false; 
	  }
  }
  
  public boolean isKeepSameNDAfterDot() {return isKeepSameNDAfterDot;}
  public void setIsKeepSameNDAfterDot(boolean b) {
	  isKeepSameNDAfterDot=b;
	  if(b)
		  isKeepSameUncNDigit=false; 
  }
  
  public static boolean isAllowMaxMinEPByDefault() {
		return isAllowMaxMinEPByDefault;
	}

	public static void setIsAllowMaxMinEPByDefault(boolean b) {
		SDS2XDX.isAllowMaxMinEPByDefault = b;	  
		if(b) {		 
			isUseMCEPByDefault=false;		  
			isUseNormEPByDefault=false;	  
		}
	}

	public static boolean isUseMCEPByDefault() {
		return isUseMCEPByDefault;
	}

	public static void setIsUseMCEPByDefault(boolean b) {
		SDS2XDX.isUseMCEPByDefault = b;
		if(b) {		 
			isAllowMaxMinEPByDefault=false;		  
			isUseNormEPByDefault=false;	  
		}
		
	}

	public static boolean isUseNormEPByDefault() {
		return isUseNormEPByDefault;
	}

	public static void setIsUseNormEPByDefault(boolean b) {
		SDS2XDX.isUseNormEPByDefault = b;
		if(b) {		 
			isUseMCEPByDefault=false;		  
			isAllowMaxMinEPByDefault=false;	  
		}
	}
	
	public static boolean isAddExtraDigitForLargeUnc() {
		return isAddExtraDigitForLargeUnc;
	}
	
	public static void setIsAddExtraDigitForLargeUnc(boolean b) {
		SDS2XDX.isAddExtraDigitForLargeUnc=b;
		XDX2SDS.setIsAddExtraDigitForLargeUnc(b);
	}
	
	public static SDS2XDX sum(ArrayList<SDS2XDX> list) {
		SDS2XDX sum=null;
		try {
			if(list==null || list.isEmpty())
				return null;

            sum=list.get(0);
            
			for(int i=1;i<list.size();i++) {
				sum=sum.add(list.get(i));
			}
		}catch(Exception e) {
			
		}
		
		return sum;
		
	}
	
	public static ArrayList<SDS2XDX> listAdd(ArrayList<SDS2XDX> list1,ArrayList<SDS2XDX> list2) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list1==null && list2==null)
				return outList;
			
			if(list1==null) {
				outList.addAll(list2);
				return outList;
			}else if(list2==null) {
				outList.addAll(list1);
				return outList;
			}
			
			ArrayList<SDS2XDX> tempList=new ArrayList<SDS2XDX>();
			if(list1.size()<list2.size()) {
				tempList.addAll(list1);
				list1=list2;
				list2=tempList;
			}
			
			for(int i=0;i<list1.size();i++) {
				SDS2XDX sx1=list1.get(i);
				try {
					SDS2XDX sx2=list2.get(i);
					outList.add(sx1.add(sx2));
				}catch(Exception e) {
					outList.add(sx1);
				}
			}
		}catch(Exception e) {
			
		}
		
		return outList;
		
	}
	public static ArrayList<SDS2XDX> listSubtract(ArrayList<SDS2XDX> list1,ArrayList<SDS2XDX> list2) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list1==null && list2==null)
				return outList;
			
			if(list1==null) {
				outList.addAll(list2);
				return outList;
			}else if(list2==null) {
				outList.addAll(list1);
				return outList;
			}
			
			ArrayList<SDS2XDX> tempList=new ArrayList<SDS2XDX>();
			if(list1.size()<list2.size()) {
				tempList.addAll(list1);
				list1=list2;
				list2=tempList;
			}
			
			for(int i=0;i<list1.size();i++) {
				SDS2XDX sx1=list1.get(i);
				try {
					SDS2XDX sx2=list2.get(i);
					outList.add(sx1.subtract(sx2));
				}catch(Exception e) {
					outList.add(sx1);
				}
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	public static ArrayList<SDS2XDX> listMultiply(ArrayList<SDS2XDX> list1,ArrayList<SDS2XDX> list2) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list1==null && list2==null)
				return outList;
			
			if(list1==null) {
				outList.addAll(list2);
				return outList;
			}else if(list2==null) {
				outList.addAll(list1);
				return outList;
			}
			
			ArrayList<SDS2XDX> tempList=new ArrayList<SDS2XDX>();
			if(list1.size()<list2.size()) {
				tempList.addAll(list1);
				list1=list2;
				list2=tempList;
			}
			
			for(int i=0;i<list1.size();i++) {
				SDS2XDX sx1=list1.get(i);
				try {
					SDS2XDX sx2=list2.get(i);
					outList.add(sx1.multiply(sx2));
				}catch(Exception e) {
					outList.add(sx1);
				}
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	public static ArrayList<SDS2XDX> listDivide(ArrayList<SDS2XDX> list1,ArrayList<SDS2XDX> list2) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list1==null && list2==null)
				return outList;
			
			if(list1==null) {
				outList.addAll(list2);
				return outList;
			}else if(list2==null) {
				outList.addAll(list1);
				return outList;
			}
			
			ArrayList<SDS2XDX> tempList=new ArrayList<SDS2XDX>();
			if(list1.size()<list2.size()) {
				tempList.addAll(list1);
				list1=list2;
				list2=tempList;
			}
			
			for(int i=0;i<list1.size();i++) {
				SDS2XDX sx1=list1.get(i);
				try {
					SDS2XDX sx2=list2.get(i);
					outList.add(sx1.divided(sx2));
				}catch(Exception e) {
					outList.add(sx1);
				}
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	
	public static ArrayList<SDS2XDX> listLog(ArrayList<SDS2XDX> list) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list==null || list.isEmpty())
				return outList;
					
			for(int i=0;i<list.size();i++) {
				SDS2XDX sx=list.get(i);
				if(sx==null)
					outList.add(null);
				else
					outList.add(sx.log());
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	public static ArrayList<SDS2XDX> listLog10(ArrayList<SDS2XDX> list) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list==null || list.isEmpty())
				return outList;
					
			for(int i=0;i<list.size();i++) {
				
				SDS2XDX sx=list.get(i);
				
				//if(sx!=null)
				//System.out.println("i="+i+" # "+sx.sds());
				
				if(sx==null)
					outList.add(null);
				else
					outList.add(sx.log10());
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	
	public static ArrayList<SDS2XDX> listExp(ArrayList<SDS2XDX> list) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list==null || list.isEmpty())
				return outList;
					
			for(int i=0;i<list.size();i++) {
				SDS2XDX sx=list.get(i);
				if(sx==null)
					outList.add(null);
				else
					outList.add(sx.exp());
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	
	public static ArrayList<SDS2XDX> listExpByBase(ArrayList<SDS2XDX> list, double base) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list==null || list.isEmpty())
				return outList;
					
			for(int i=0;i<list.size();i++) {
				SDS2XDX sx=list.get(i);
				if(sx==null)
					outList.add(null);
				else
					outList.add(sx.expByBase(base));
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	
	public static ArrayList<SDS2XDX> listPow(ArrayList<SDS2XDX> list,double p) {
		ArrayList<SDS2XDX> outList=new ArrayList<SDS2XDX>();
		try {
			if(list==null || list.isEmpty())
				return outList;
					
			for(int i=0;i<list.size();i++) {
				SDS2XDX sx=list.get(i);
				if(sx==null)
					outList.add(null);
				else
					outList.add(sx.pow(p));
			}
		}catch(Exception e) {
			
		}
		
		return outList;
	}
	/* ------------------------------------------------------------------------------------ */

	
	/*
	//see also findNSig(s) in Str.java
	public static int findBestNSigOfProduct(String s1,String s2) {
	   	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println(n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		//[0]-base string, [1]-npower string
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		SDS2XDX sx1=null,sx2=null;
    		
    		double v1=Double.parseDouble(out1[0]);//[1,9.999]
    		double v2=Double.parseDouble(out2[0]);//[1,9.999]
 
    		if(out1[0].contains(".")) {
    			sx1=new SDS2XDX(out1[0],"1");
    		}else {
    			sx1=new SDS2XDX();
    			sx1.setValues(v1, v1*0.1);
    		}

    		if(out2[0].contains(".")) {
    			sx2=new SDS2XDX(out2[0],"1");
    		}else {
    			sx2=new SDS2XDX();
    			sx2.setValues(v2, v2*0.1);
    		}

    		
    		sx1=sx1.multiply(sx2);
    		
    		return Str.findNSig(sx1.s());
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
	}
	public static String findBestStringOfProduct(String s1,String s2) {
		try {
    		double v1=Double.parseDouble(s1);
    		double v2=Double.parseDouble(s2);
    		double v=v1*v2;
    		
			int nsig=findBestNSigOfProduct(s1,s2);
			if(nsig<0)
				return "";
			
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	
			s=Str.roundByNsig(s, nsig);
			return s;
			
		}catch(Exception e) {
			
		}
		
		return "";

	}
	
	public static int findBestNSigOfDivision(String s1,String s2) {
	   	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println(n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		//[0]-base string, [1]-npower string
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		SDS2XDX sx1=null,sx2=null;
    		
    		double v1=Double.parseDouble(out1[0]);//[1,9.999]
    		double v2=Double.parseDouble(out2[0]);//[1,9.999]
            if(v2==0)
            	return -1;
            
    		if(out1[0].contains(".")) {
    			sx1=new SDS2XDX(out1[0],"1");
    		}else {
    			sx1=new SDS2XDX();
    			sx1.setValues(v1, v1*0.1);
    		}

    		if(out2[0].contains(".")) {
    			sx2=new SDS2XDX(out2[0],"1");
    		}else {
    			sx2=new SDS2XDX();
    			sx2.setValues(v2, v2*0.1);
    		}

    		sx1=sx1.divided(sx2);
    		
    		return Str.findNSig(sx1.s());
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
	}
	public static String findBestStringOfDivision(String s1,String s2) {
		try {
    		double v1=Double.parseDouble(s1);
    		double v2=Double.parseDouble(s2);
    		if(v2==0)
    			return "";
    		
    		double v=v1/v2;
    		
			int nsig=findBestNSigOfProduct(s1,s2);
			if(nsig<0)
				return "";
			
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	
			s=Str.roundByNsig(s, nsig);
			return s;
			
		}catch(Exception e) {
			
		}
		
		return "";

	}
	
	
	public static int findBestNSigOfAddition(String s1,String s2) {
	   	try {
    		int n1=Str.findNSig(s1);
    		int n2=Str.findNSig(s2);
    		//System.out.println("@@"+n1+"  "+n2);
    		
    		if(n1<=0 || n2<=0)
    			return -1;
    		
    		//[0]-base string, [1]-npower string
    		//out[0]-[1,9.9999]
    		String[] out1=Str.findSciBaseNPower(s1);
    		String[] out2=Str.findSciBaseNPower(s2);
    		
    		SDS2XDX sx1=null,sx2=null;
    		
    		double v1=Double.parseDouble(s1); 
    		double v2=Double.parseDouble(s2); 
 
    		//System.out.println("@@ "+out1[0]+"  "+out2[0]+"  s1="+s1+" s2="+s2);
    		
    		if(out1[0].contains(".")) {
    			sx1=new SDS2XDX(s1,"1");
    		}else {
    			sx1=new SDS2XDX();
    			sx1.setValues(v1, v1*0.1);
    		}

    		if(out2[0].contains(".")) {
    			sx2=new SDS2XDX(s2,"1");
    		}else {
    			sx2=new SDS2XDX();
    			sx2.setValues(v2, v2*0.1);
    		}

    		sx1=sx1.add(sx2);
    		
    		int nsig=Str.findNSig(sx1.s());
    		//System.out.println(sx1.s()+"  "+sx1.ds()+" nsig="+nsig);
    		
    		return nsig;
    	}catch(Exception e) {
    		
    	}
    	
    	return -1;
	}
	public static String findBestStringOfAddition(String s1,String s2) {
		try {
    		double v1=Double.parseDouble(s1);
    		double v2=Double.parseDouble(s2);
    		double v=v1+v2;
    		
			int nsig=findBestNSigOfAddition(s1,s2);
			if(nsig<0)
				return "";
			
        	String s=String.valueOf(v);//v=0.0010, s=0.001 
        	if(s1.endsWith("0") || s2.endsWith("0")) {
        		if(!s.endsWith("0"))
        			if(s.contains("."))
        				s+="0";
        			else
        				s+=".0";
        	}
        	
			s=Str.roundByNsig(s, nsig);

        	//System.out.println(" s1="+s1+" s2="+s2+"  s="+s+" nsig="+nsig);
        	
			return s;
			
		}catch(Exception e) {
			
		}
		
		return "";

	}
	
	public static int findBestNSigOfSubtraction(String s1,String s2) {
		if(s2.startsWith("-"))
			s2=s2.substring(1);
		else
			s2="-"+s2;
		
		return findBestNSigOfAddition(s1,s2);
	}
	
	public static String findBestStringOfSubtraction(String s1,String s2) {
		if(s2.startsWith("-"))
			s2=s2.substring(1);
		else
			s2="-"+s2;
		
		return findBestStringOfAddition(s1,s2);
	}
	*/
	
	public static SDS2XDX eval(String expression,int errorLimit) {
		return eval(expression,null,errorLimit);
	}
	public static SDS2XDX eval(String expression, HashMap<String,SDS2XDX> substitutionMap, int errorLimit) {
		
		try {
			//System.out.println(expression+"  $"+substitutionMap.size());
			
			if(substitutionMap!=null && substitutionMap.size()>0) {
				LinkedHashMap<String,SDS2XDX> sortedMap=new LinkedHashMap<String,SDS2XDX>(substitutionMap) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
		            public void putAll(Map<? extends String, ? extends SDS2XDX> m) {
		                // Extract the entries from the map
		                List<Entry<? extends String, ? extends SDS2XDX>> entries = new ArrayList<>(m.entrySet());

		                // Sort the entries based on the length of the keys, if length is same, then sort by natural order
		                entries.sort(Comparator.comparingInt((Entry<? extends String, ? extends SDS2XDX> entry) -> entry.getKey().length())
		                		.thenComparing(Entry::getKey));

		                // Add the sorted entries to the map
		                for (Entry<? extends String, ? extends SDS2XDX> entry : entries) {
		                    super.put(entry.getKey(), entry.getValue());
		                }
		            }
					
					/*
					//note it doesn't change what keySet() returns
				    @Override
				    public Set<Entry<String, SDS2XDX>> entrySet() {
				        // Get the entries and sort them by the length of the keys
				    	List<Entry<String, SDS2XDX>> entries = new ArrayList<>(super.entrySet());
	                    entries.sort(Comparator.comparingInt((Entry<String, SDS2XDX> entry) -> entry.getKey().length())
	                        .thenComparing(Entry::getKey));

				        // Create a LinkedHashSet to maintain the order
				        Set<Entry<String, SDS2XDX>> sortedEntries = new LinkedHashSet<>(entries);
				        return sortedEntries;
				    }
				 
		            @Override
		            public Set<String> keySet() {
		                // Get the keys and sort them by the length of the keys
		                List<String> keys = new ArrayList<>(super.keySet());
		                keys.sort(Comparator.comparingInt(String::length).thenComparing(String::compareTo));

		                // Create a LinkedHashSet to maintain the order
		                Set<String> sortedKeys = new LinkedHashSet<>(keys);
		                return sortedKeys;
		            }

		            @Override
		            public Collection<SDS2XDX> values() {
		                // Get the entries and sort them by the length of the keys
		                List<Entry<String, SDS2XDX>> entries = new ArrayList<>(super.entrySet());
	               
	                    entries.sort(Comparator.comparingInt((Entry<String, SDS2XDX> entry) -> entry.getKey().length())
	                        .thenComparing(Entry::getKey));

		                // Extract the values from the sorted entries
		                List<SDS2XDX> sortedValues = new ArrayList<>();
		                for (Entry<String, SDS2XDX> entry : entries) {
		                    sortedValues.add(entry.getValue());
		                }

		                // Return the sorted values as a LinkedHashSet to maintain the order
		                return new LinkedHashSet<>(sortedValues);
		            }
		            */
				};

				sortedMap.putAll(substitutionMap);
				
				expression=temporaryReplaceFuncNames(expression);
				
				for(String key:sortedMap.keySet()) {
				    // Replace key name with the actual value and uncertainty of the SDS2XDX object
					SDS2XDX sx=substitutionMap.get(key);
					String s=sx.s();
					String ds=sx.ds();
					if(s.isEmpty())
						s="0.0(0)";
					else
						s=(!ds.isEmpty()?(s+"("+ds+")"):s);
					
					//System.out.println(key+"  s="+s+" "+sortedMap.size()+expression);
					
				    expression = expression.replace(key, s);
				}

				expression=restoreReplaceFuncNames(expression);
			}

	    
			expression=expression.replace(" ","").trim().toUpperCase();
			
		    // Use a stack to evaluate the expression
		    Stack<SDS2XDX> values = new Stack<>();
		    Stack<Character> operators = new Stack<>();

		    char c0=expression.charAt(0);
		    String sign="";
		    if(c0=='*'||c0=='/')
		    	values.push(new SDS2XDX("1.0","0",errorLimit));
		    else if(c0=='+' || c0=='-') {
		    	//values.push(new SDS2XDX("0.0","0",errorLimit));
		    	//set sign instead
		    	sign=c0+"";
		    }
		    
		     // 1. [\\+\\-]?: Matches an optional + or - sign. The \\ is used to escape the + and - characters in the regular expression.
		     // 2. ((0.[0-9]+|[1-9]+[0-9.][0-9]*)): Matches a numeric value, which can be:
		     //    0.[0-9]+: A decimal number starting with 0. followed by one or more digits.
		     //    [1-9]+[0-9.][0-9]*: A number starting with a non-zero digit, followed by zero or more digits or a decimal point, and optionally followed by zero or more digits.
		     // 3. \\(: Matches the opening parenthesis (. The \\ is used to escape the parenthesis in the regular expression.
		     // 4. ([1-9][0-9]*): Matches one or more digits (1-9 followed by zero or more digits 0-9). This captures the uncertainty part of the SDS2XDX object.
		     // 5. \\): Matches the closing parenthesis ). The \\ is used to escape the parenthesis in the regular expression.
		     // 6. |: Acts as an OR operator, allowing the pattern to match either the previous part or the following part.
		     // 7. [+\\-*/()]: Matches any single character that is a +, -, *, /, (, or ).
		    
		    
		    //Note: number of group depends on how many pairs of parentheses (non-escaped ones)
		    //group() or group(0) always the whole match
		    //String valPatternStr="("+"[\\+\\-]?"+"(0.[0-9]+|[1-9]+[0-9.]?[0-9]*)"+")";
		    String valPatternStr="("+"(0.[0-9]+|[1-9]+[0-9]*[.]?[0-9]*)([Ee][+\\-]?[1-9])?"+")";
		    //String valPatternStr="("+"(0.[0-9]+|[1-9]+[0-9]?[0-9.]?[0-9]*)([Ee][+\\-]?[1-9])?"+")";\\doesn't recognize 902.0(12)
		    String uncPatternStr="("+"\\((0|[1-9][0-9]*)\\)"+"|"
		                           +"\\(\\+(0|[1-9][0-9]*)\\-(0|[1-9][0-9]*)\\)"+"|"//asymmetric uncertainty
		    		               +"\\((L[TE]|G[TE]|AP|SY)\\)"+")";
		    
		    String operatorPatternStr="[+\\-*/()]|LOG10|LOG|EXP|POW|,";//, only for separating arguments in POW
		    Pattern pattern = Pattern.compile( (valPatternStr+uncPatternStr)+"|"+valPatternStr+"|"+operatorPatternStr);
		    Matcher matcher = pattern.matcher(expression);
	    
		    //System.out.println(expression);
		    int prevOpPos=-1;
		    
		    String remaining=expression;
		    while (matcher.find()) {
		        String token = matcher.group();

		        //System.out.println("### Token="+token);

		        
		        if (token.matches(valPatternStr+uncPatternStr)) {
		        	//System.out.println("1 token="+token+"  "+matcher.groupCount());
		        	//for(int i=0;i<matcher.groupCount();i++)
		        	//	System.out.println("i="+i+"  "+matcher.group(i));
		        	
		        	//value+uncertainty
		            String value =sign+matcher.group(1);
		            String uncertainty = matcher.group(5);
		            if(uncertainty==null && token.indexOf("(+")>0) {
		            	String s=matcher.group(4);
		            	if(s.startsWith("(")&&s.endsWith(")"))
		            		uncertainty=s.substring(1,s.length()-1).trim();
		            }
		            
		            //System.out.println("  value="+value+" uncertainty="+uncertainty);
		            
		            
		            values.push(new SDS2XDX(value, uncertainty,errorLimit));
		            sign="";
		        } else if (token.matches(valPatternStr)) {
		        	//value without uncertainty
		        	
		        	//System.out.println("2 token="+token+"  "+matcher.groupCount());
		        	//for(int i=0;i<matcher.groupCount();i++)
		        	//	System.out.println("i="+i+"  "+matcher.group(i));
		        	
		            String value = sign+matcher.group(0);
		            String uncertainty = "0";
		            
		            //System.out.println("  value="+value+" uncertainty assumed="+uncertainty);
		            
		            values.push(new SDS2XDX(value, uncertainty,errorLimit));
		            sign="";
		        } else if (token.equals("(")) {
		        	//System.out.println("3 token="+token+"  "+matcher.groupCount());
		        	//for(int i=0;i<matcher.groupCount();i++)
		        	//	System.out.println("i="+i+"  "+matcher.group(i));
		        	
		        	String prevOP=null;
		        	if(operators.size()>0)
		        		prevOP=operators.peek()+"";
		            
		        	operators.push('(');
		        
		        	int n=matcher.start();
		        	if(n<expression.length()-1) {
		        		char nextChar=expression.charAt(n+1);
		        		
		        		if(values.size()>0 && (prevOP==null|| !"+-*/LEPU".contains(prevOP)) ) {//L for log, U for LOG10, E for exp, P for pow,
		        			//e.g., 12.3(+12), wrong expression
		        			return null;
		        		}
		        		if("+-".contains(nextChar+"")) {
		        			//values.push(new SDS2XDX("0.0","0",errorLimit));
		        			sign=nextChar+"";
		        		}else if("*/".contains(nextChar+"")) {
		        			values.push(new SDS2XDX("1.0","0",errorLimit));
		        		}
		        	}
		        	
		        	prevOpPos=matcher.start();
		        } else if (token.equals(")")) {
		        	//System.out.println("4 token="+token+"  "+matcher.groupCount()+" ops size= "+operators.size());
		        	//for(int i=0;i<matcher.groupCount();i++)
		        	//	System.out.println("i="+i+"  "+matcher.group(i));
		        	
		            while (!operators.isEmpty() && operators.peek() != '(') {
		            	calculate(values,operators,errorLimit);
		            }
		            if(!operators.isEmpty())
		            	operators.pop();
		            
		            prevOpPos=matcher.start();
		        } else if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
		        	
		        	//System.out.println("5 token="+token+"  "+matcher.groupCount()+" ops size= "+operators.size());
		        	//for(int i=0;i<matcher.groupCount();i++)
		        	//	System.out.println("i="+i+"  "+matcher.group(i));
		        	
		            while (!operators.isEmpty() && hasPrecedence(operators.peek(),token.charAt(0))) {
		            	//System.out.println(operators.peek()+"  "+token.charAt(0));
		            	calculate(values,operators,errorLimit);
		            }
		            
		            if(sign.equals(token)) {//+/- is sign not operator
		            	//sign=token;
		            }else {
			            operators.push(token.charAt(0));
			            prevOpPos=matcher.start();
		            }
		        }else if(token.equals("LOG")||token.equals("EXP")||token.equals("POW")) {
		        	operators.push(token.charAt(0));
		        	prevOpPos=matcher.start();
		        }else if(token.equals("LOG10")) {
		        	operators.push('U');
		        	prevOpPos=matcher.start();
		        }else if(token.equals(",") && operators.peek()=='(') {//POW(12.3(2),2)
		        	int n=matcher.start();
		        	boolean isGoodPow=false;
		        	if(n<expression.length()-1) {
		        		int n1=expression.indexOf(")",n);
		        		if(n1>0) {
		        			String powS=expression.substring(n+1,n1);
		        			String baseS="";
		        			if(prevOpPos>0 && expression.charAt(prevOpPos)=='(')
		        				baseS=expression.substring(prevOpPos+1,n);
		        			
		        			//System.out.println(expression+" "+prevOpPos+"  "+expression.lastIndexOf("POW", prevOpPos));
		        			
		        			n1=expression.lastIndexOf("POW", prevOpPos);
		        			if(n1==prevOpPos-3) {
			        			if( (baseS.matches(valPatternStr)||baseS.matches(valPatternStr+uncPatternStr)) && Str.isNumeric(powS)) {
			        				isGoodPow=true;
			        			}
		        			}

		        			//System.out.println(baseS+"  "+powS+" "+isGoodPow);
		        		}
		        	}
		        	
		        	//prevOpPos=matcher.start();
		        	if(!isGoodPow)
		        		return null;
		        }else {
		        	return null;
		        }
		        //System.out.println("##remianing="+remaining+" TOKEN="+token);
		        int n=remaining.indexOf(token);
		        if(n>=0) {
			        String s1=remaining.substring(0,n+token.length());
			        String s2=remaining.substring(n+token.length());
			        remaining=s1.replace(token, "")+s2;
		        }

		    }

		    //System.out.println("##remianing="+remaining);
		    if(remaining.trim().length()>0)
		    	return null;
		    
		    //for(SDS2XDX sx:values)
		    //	System.out.println("sx="+sx.s+" "+expression);
		    
		    while (!operators.isEmpty()) {
		    	calculate(values,operators,errorLimit);
		    }

		    SDS2XDX result=values.pop();
		    
		    //System.out.println(" ##### expression="+expression+"  result="+result.s());
		    return result;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}

	private static String temporaryReplaceFuncNames(String expression) {
		String out=expression;
		out=expression.replace("LOG","@@").replace("EXP","##").replace("POW","$$");
		return out;
	}
	
	private static String restoreReplaceFuncNames(String expression) {
		String out=expression;
		out=expression.replace("@@","LOG").replace("##","EXP").replace("$$","POW");
		return out;
	}
	private static void calculate(Stack<SDS2XDX> values,Stack<Character> operators,int errorLimit) {
		try {
	    	//for a+b or a/b, b is popped out first
	    	
	    	if(values.size()>=1) {
	    		SDS2XDX b=values.pop();
	    		SDS2XDX a=null;
	    
	    		Character op=operators.pop();
	    		SDS2XDX result=null;
	    		
	    		if(op=='L') {
	    			//log
	    			/*
    				if(b.x<=0 || b.x_lv_d<=0) {
    					result=null;
    				}
    				
    				double x=Math.log(b.x);
    				double dxu=Math.log(b.x_uv_d)-x;
    				double dxl=x-Math.log(b.x_lv_d);
    				XDX2SDS xs=new XDX2SDS(x,dxu,dxl,b.errorLimit);
    				result=new SDS2XDX();
    				result.setValues(xs);
    				*/
	    			
	    			result=new SDS2XDX();
	    			
	    			result.setErrorLimit(errorLimit);
	    			result.setValues(b);
	    			result=result.log();
	    			
	    		}else if(op=='U') {//log10(b)
	    			result=new SDS2XDX();
	    	
	    			result.setErrorLimit(errorLimit);
	    			result.setValues(b);
	    			result=result.log10();
	    		}else if(op=='E') {//exp(b)
	    			result=new SDS2XDX();
	    		
	    			result.setErrorLimit(errorLimit);
	    			result.setValues(b);
	    			result=result.exp();
	    		}else if(op=='P') {//power(a,b)
		    		if(values.size()>=1) {
		    			a=values.pop();//base
		    			
		    			result=new SDS2XDX();
		    	
		    			result.setErrorLimit(errorLimit);
		    			result.setValues(a);
		    			
		    			//b is power and should be a numerical value
		    			if(!b.ds.equals("0"))
		    				return;
		    			
		    			double p=Double.parseDouble(b.s);
		    			result=result.pow(p);
		    		}else
		    			return;
	    		}else {
		    		if(values.size()>=1) {
		    			a=values.pop();
		    		
		    		}else {
		    			if(op=='*' || op=='/')
		    				a=new SDS2XDX("1.0","0",errorLimit);
		    			else if(op=='+' || op=='-')
		    				a=new SDS2XDX("0","0",errorLimit);
		    			else 
		    				return;
		    		}
		    		
		        	result=applyOperator(a,b,op);
	    		}

	        	
	        	//System.out.println(" #### b="+b+" a="+a+" op="+op+"  Result: " + result);

	    		values.push(result);
	    	}else {
	    		operators.pop();
	    	}
		}catch(Exception e) {
			
		}
	}
	/*
	 * return true if op1 has higher priority than op2, otherwise return false
	 */
	private static boolean hasPrecedence(char op1, char op2) {
		//"(" and ")" should not be compared with other operators, so return false
	    if (op1 == '(' || op1 == ')') {
	        return false;
	    }
	    if ((op2 == '*' || op2 == '/') && (op1 == '+' || op1 == '-')) {
	        return false;
	    }
	    return true;
	}

	private static SDS2XDX applyOperator(SDS2XDX a, SDS2XDX b,char operator) {
	    switch (operator) {
	        case '+':
	            return a.add(b);
	        case '-':
	            return a.subtract(b);
	        case '*':
	            return a.multiply(b);
	        case '/':
	            return a.divided(b);
	    }
	    return null;
	}
}



/* ------------------------------------------------------------------------------------ */

  class convst{

  boolean bx;
  double x;

  public convst(String st){
    String sst=st.toLowerCase().trim();
        try{
          x=Double.parseDouble(sst);
          bx=true;
        }
    catch(NumberFormatException e){
          x=-1;
          bx=false;
        }
  }
  public double X(){ return x; }
  public double x(){ return x; }
  public boolean canConvert(){ return bx; }

  }
/* ------------------------------------------------------------------------------------ */

class convdelta{

/*--- converts the ENSDF 2-digit error into something useful  */

  double ddelta=-1;
  boolean bddelta=false;
  
  public convdelta(String st, String dst) { 

      if( st.trim().length() > 0 && dst.trim().length() > 0 ) {
  
      String sst=st.toLowerCase().trim();
      double x=Double.parseDouble(sst);
      try{ 
        double dx=Double.parseDouble(dst);

        int id=sst.indexOf(".");
        int ie=sst.indexOf("e");
        int il=sst.length();
        if(ie>0) il=ie;
        double multiplier=1;


/*  Taking care of numers including the "e", such as 1.2e+2 */

        if( ie > 0 && x !=-1) {
          double xx=Double.parseDouble(st.substring(0,ie));
          multiplier=x/xx;
        }

/*  If there is no dot, it is an easy conversion  */

        if( id < 0 ){
          ddelta=multiplier*dx;
        }

/*  Otherwise, we need to work a bit extra:  */

        else {
          ddelta=multiplier*dx*Math.pow(10,-(il-id-1));
        }
        bddelta=true;

      } /* end of try */
      catch(NumberFormatException e){
          ddelta=-1;
          bddelta=false;
      }

    } /* if length */

  }



  public double DX(){ return ddelta;}
  public double dx(){ return ddelta;}
  public boolean canConvert(){ return bddelta;}


}



