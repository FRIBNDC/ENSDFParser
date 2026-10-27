 package ensdfparser.ensdf;

import java.util.Arrays;
import java.util.Vector;

import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

public class XDX2SDS{

  private String s,ss;//ss for symmetrized s
  private String sn;
  private int lsn;
  private String ds,dss,dsu,dsl;//dsu,dsl: upper and lower uncertainty, respectively
                            //dss for symmetrized value if dxiu and dxil are given
                            //ds=+dsu-dsl if dsu and dsl is not the same, otherwise =dsu
  private String sign;

  private double x0,dxu0,dxl0;//original values
  private String s0,ds0,dsu0,dsl0;
  
  private double x,sx;//sx is symmetrized x values
                      //x=sx if symmetrized=true
  
  private double xn;
  private long ixn;
  
  private double dx,dxu,dxl;//dx is symmetrized uncertainty is dxu and dxl are given
                            //dxu=dxl=dx if hasSymmetrized=true
  
  private double dxn;
  private long idxn;
  private double a;
  private long ia;
  private boolean canGetSDS;
  private boolean sysout;
  public float xf,dxf,dxuf,dxlf;
  
  private int exp;
  private String exps,bases;
  private double basex;
  private boolean isExpo=false;
  
  private int nsig=3;
  
  public static final int defaultErrorLimit=35;
  
  private static double expFormatLimitSmallNumber=0.001;
  //public static double expFormatUpperLargeNumber=5000;
  
  private int errorLimit=defaultErrorLimit;
  
  private boolean isLimits=false;
  private String su,sl;//strings of upper and lower limit
  
  private boolean hasSymmetrized=false, canBeSymmetrized=false;
  
  //force uncertainty to comply with the precision determined based on face value
  private static boolean forcePrecision=true;
  
  //automatically add one extra digit, only for large uncertainty >=50% of value 
  //and both value (base part if it is expo format) and uncertainty have length=1, e.g., 8(5) or 8E2(5)
  //this condition is applied to operations on a SDS2XDX object
  private static boolean isAddExtraDigitForLargeUnc=true;
  
  public XDX2SDS(){
    x=-1;sx=-1;
    dx=-1;dxu=-1;dxl=-1;
    x0=-1;dxu0=-1;dxl0=-1;
    s="";ss="";
    ds="";dss="";dsu="";dsl="";
    s0="";ds0="";dsu0="";dsl0="";
    
    sign="";
    xf=-1.0f; dxf=-1.0f;
    dxuf=-1.0f;dxlf=-1.0f;
    
    isLimits=false;
    su="";sl="";
    exp=0;
    exps="";bases="";
    basex=-1;
    isExpo=false;
    
    nsig=3;
    
    errorLimit=defaultErrorLimit;
    
    hasSymmetrized=false;
    canBeSymmetrized=false;
  }

  public XDX2SDS(double xi, double dxi){
	  this();
	  setValues(xi,dxi);
  }
  
  public XDX2SDS(double xi, double dxi,int errorLimit){
	  this();
	  setErrorLimit(errorLimit);
	  setValues(xi,dxi);
  }
  
  public XDX2SDS(double xi, double dxi,int errorLimit,int nsig){
	  this();
	  setErrorLimit(errorLimit);
	  setNsig(nsig);
	  setValues(xi,dxi);
  }
  
  public XDX2SDS(double xi,double dxiu,double dxil){
	  this();
      setValues(xi,dxiu,dxil);  
  }
  
  public XDX2SDS(double xi,double dxiu,double dxil,int errorLimit){
	  this();
	  setErrorLimit(errorLimit);
      setValues(xi,dxiu,dxil);  
  }

  public XDX2SDS(double xi,double dxiu,double dxil,int errorLimit,int nsig){
	  this();
	  setErrorLimit(errorLimit);
	  setNsig(nsig);
      setValues(xi,dxiu,dxil);  
  }
  
  public XDX2SDS(float xi, float dxi){
	  this();
	  setValues(xi,dxi);
  }
  
  public XDX2SDS(float xi, float dxi,int errorLimit){
	  this();
	  setErrorLimit(errorLimit);
	  setValues(xi,dxi);
  }
  
  public XDX2SDS(float xi,float dxiu,float dxil){
	  this();
      setValues(xi,dxiu,dxil);  
  }
  

  public void setValues_old(double xi,double dxiu,double dxil){
	  	  
	  //set values with the symmetrized (x,dx) pair
	  double[] xdx=symmetrize(xi,dxiu,dxil);
	  setValues(xdx[0],xdx[1]);
	  
      //debug
      //System.out.println("   xi="+xi+" dxiu="+dxiu+" dxil="+dxil);

	  if(dxiu==dxil || dxiu<0f || dxil<0f || (dxiu+dxil)==0f)
		  return;
	  
	  double r=Math.abs((dxiu-dxil)/(dxiu+dxil));
	  if(r<0.001){
		  canBeSymmetrized=true;
		  return;
	  }
	  //set asymetric values
	  x=xi;
	  dxu=dxiu;dxl=dxil;

	  xf=(float)x;
	  dxuf=(float)dxiu;
	  dxlf=(float)dxil;
	  
	  x0=x;
	  dxu0=dxu;dxl0=dxl;
	  
	  isLimits=false;
	  
	  XDX2SDS xs1=new XDX2SDS(xi,dxiu,errorLimit,nsig);
	  XDX2SDS xs2=new XDX2SDS(xi,dxil,errorLimit,nsig);
	  su=xs1.su;
	  sl=xs2.sl;
	  

	  int len1=xs1.ds.length();
	  int len2=xs2.ds.length();
	
      //debug
      //System.out.println("   xi="+xi+" dxiu="+dxiu+" dxil="+dxil+" xs1.s="+xs1.s+" xs1.ds="+xs1.ds+" xs2.s="+xs2.s+" xs2.ds="+xs2.ds+" su="+su+" sl="+sl+" errorLimit="+errorLimit);
      //System.out.println(" len1="+len1+" len2="+len2+" xs1.base="+xs1.bases+" xs2.base="+xs2.bases+" xs1.exp="+xs1.exp+" xs2.exp="+xs2.exp);;

	  boolean reset1=false,reset2=false;
	  long aa=0;
	  String tempS="";
	  
	  if(xs1.s.equals(xs2.s)){
		  s=xs1.s;
		  dsu=xs1.ds;
		  dsl=xs2.ds;

		  exp=xs1.exp();
		  basex=x;
		  
		  exps=xs1.exps();
		  bases=xs1.bases();
  
		  checkDS();
		  
		  return;
	  }else if(xs1.exp==xs2.exp){
		  int n=(int)(xs1.ia-xs2.ia);
		  if(n>0 && (n+len2)==2){
			  reset2=true;
			  reset1=false;
		  }else if(n>0 && (n+len2)==3&&len1==2&&xs1.bases.length()>1){
			  reset1=true;
			  reset2=true;
			  aa=xs1.ia-1;
			  tempS=xs1.s;
		  }else if(n<0&&(-n+len1)==2){
			  reset1=true;
			  reset2=false;
		  }else if(n<0 && (-n+len1)==3&&len2==2&&xs2.bases.length()>1){
			  reset1=true;
			  reset2=true;
			  aa=xs2.ia-1;
			  tempS=xs2.s;
		  }
	  }else{//one is in scientific format (exp!=0) and the other one not (exp==0)
		  if(xs1.exp==0){//xs2 is in scientific format
		      int dx2i=(int)(roundUncertainty(xs2.dx*Math.pow(10.,xs1.a)));
		      if(dx2i<=99){
		    	  reset2=true;
		    	  reset1=false;
		      }else if(dx2i>99 && len1==2&&xs1.bases.length()>1){
		    	  reset1=true;
		    	  reset2=true;
		    	  aa=xs1.ia-1;
		    	  tempS=xs1.s;
		      }
		  }else{//xs1 is in scientific format
		      int dx1i=(int)(roundUncertainty(xs1.dx*Math.pow(10.,xs2.a)));
		      if(dx1i<=99){
		    	  reset1=true;
		    	  reset2=false;
		      }else if(dx1i>99 && len2==2&&xs2.bases.length()>1){
		    	  reset1=true;
		    	  reset2=true;
		    	  aa=xs2.ia-1;
		    	  tempS=xs2.s;
		      }
		  }
	  }

	     
	  //System.out.println(" reset1="+reset1+" reset2="+reset2+" tempS="+tempS+" aa="+aa);;

	     
	  if(reset1!=reset2){
		  if(reset1){
		      xs1.dxn=roundUncertainty( xs1.dx*Math.pow(10.,xs2.a) );//round ##.4 to #(#+1)
		      xs1.idxn=(long) xs1.dxn;
		      xs1.ds=Long.toString(xs1.idxn);
		      dsu=xs1.ds;
		      
		      s=xs2.s;
		      dsl=xs2.ds;
		      bases=xs2.bases;
			  exps=xs2.exps();
			  basex=xs2.basex;
			  exp=xs2.exp();
			  
		      //System.out.println(" *****bases="+bases+" exps="+exps+"  dsu="+dsu+" xs1.dx="+xs1.dx+" xs2.a="+xs2.a+" xs1.a="+xs1.a);
		      
		  }else{
		      xs2.dxn=roundUncertainty( xs2.dx*Math.pow(10.,xs1.ia) );//round ##.4 to #(#+1)
		      xs2.idxn=(long) xs2.dxn;
		      xs2.ds=Long.toString(xs2.idxn);
		      dsl=xs2.ds; 
		      
		      s=xs1.s;
		      dsu=xs1.ds;
		      bases=xs1.bases;
			  exps=xs1.exps();
			  basex=xs1.basex;
			  exp=xs1.exp();
			  
		  }
		  
		  checkDS();
		  
		  return;
	  }else if(reset1=true){//also reset2=true, reduce the number of uncertainty digits by one
	      xs1.dxn=roundUncertainty( xs1.dx*Math.pow(10.,aa) );//round ##.4 to #(#+1)
	      xs1.idxn=(long) xs1.dxn;
	      xs1.ds=Long.toString(xs1.idxn);
	      dsu=xs1.ds;
	      
	      xs2.dxn=roundUncertainty( xs2.dx*Math.pow(10.,aa) );//round ##.4 to #(#+1)
	      xs2.idxn=(long) xs2.dxn;
	      xs2.ds=Long.toString(xs2.idxn);
	      dsl=xs2.ds;
	      
	      //round last digit, tempS.lengh()>1
	      int len=tempS.length();
	      xs1.xn=round( x*Math.pow(10.,aa))/Math.pow(10.,aa);
	      tempS=Double.toString(xs1.xn);
	      tempS=tempS.substring(0, len-1);

	      //System.out.println(" tempS="+tempS+"   len="+len+" "+tempS.length()+" x="+x);
	      
	      s=tempS;
	      
	      bases=tempS;
		  exps=xs1.exps();
		  basex=x;
		  exp=xs1.exp();
		  
		  checkDS();
		  
	      return;
	  }
	  //if reaching here, it means dxiu and dxil differ significantly

	  isLimits=true;
  }

  public XDX2SDS removeEndZeros() {

	  String[] out=Str.removeEndZeros(s, ds, dsu, dsl);

	  XDX2SDS x2s=new XDX2SDS(x,dxu,dxl,errorLimit,nsig);
	  x2s.s=out[0];
	  x2s.ds=out[1];
	  x2s.dsu=out[2];
	  x2s.dsl=out[3];
	  
	  return x2s;
  }
  
  
  private void checkDS(){
	  try{
		  
		  if(x<=0)
			  return;
		  
		  
		  if(dsu.equals(dsl)) {
			  ds=dsu;
			  return;
		  }
		  
		  double dx1=Double.parseDouble(dsl);
		  double dx2=Double.parseDouble(dsu);
		  
		  String temp="";
		  if(dx2>=0)
			  temp+="+"+dsu;
		  if(dx1>=0)
			  temp+="-"+dsl;
		  
		  //System.out.println(s+" ## "+temp+" dsu="+dsu+" dsl="+dsl);
		  
		  ds=temp;
		  
		  double dds=Math.abs(dx1-dx2);
		  double ddx=Math.abs(dxu-dxl);
		  
		  //System.out.println(dds+"  "+ddx);
		  
		  double r=dds/(dx1+dx2);
		  if( (dds<=1&&r<0.05) || r<=0.04)//eg, x1=24, x2=26, can be symmetrized
			  canBeSymmetrized=true;
		  
		  //System.out.println(r+" "+dds+" "+canBeSymmetrized);
		  
		  //System.out.println("s="+s+" dsu="+dsu+" dsl="+dsl+" r="+r+" dd="+dds+" 1.0/(errorLimit)="+1.0/(errorLimit)+"  x="+x+" dxu="+dx2+" dxl="+dx1+" ddx="+ddx);
		  
		  if(ddx<0.01*Math.abs(x) && (r<=1.0/errorLimit || (dds==1.0)&&r<0.05) ) {
				  canBeSymmetrized=true;
				  hasSymmetrized=true;	

				  s0=s;ds0=ds;dsu0=dsu;dsl0=dsl;
				  
				  //s=s;//keep the original s
				  s=ss;
				  dsl=dss;
				  dsu=dss;
				  ds=dss;
				  
				  //System.out.println("s="+s+" dsu="+dsu+" dsl="+dsl+" r="+r+" 1.0/(2*errorLimit-1)="+1.0/(2*errorLimit-1)+"  x="+x+" dxu="+dx2+" dxl="+dx1);
				  //System.out.println(" errorLimit="+errorLimit);
				  
				  return;			  
		  }
	  
		  //for dxl>abs(x0)
		  
		  int n=s.toUpperCase().indexOf("E");
		  String s0=s;
		  Double x0=x;
		  
		  if(n>0){
			  s0=s.substring(0, n).trim();
			  x0=Double.parseDouble(s0);
		  }
		  	
		  if(dx1<=0)
			  return;
		  
		  dds=Math.log10(dx1/x0);
		  int p=(int)(dds);
		  
		  dx2=x0*Math.pow(10,p);
		 
		  //System.out.println("1 s="+s+" dsu="+dsu+" dsl="+dsl+" r="+r+" 1.0/(2*errorLimit-1)="+1.0/(2*errorLimit-1)+"  x="+x+" dxu="+dx2+" dxl="+dx1+" dds="+dds);
		  
		  if(dxl>Math.abs(x)) {
			  if(dx2<dx1 && (dx1-dx2)<=1 && Math.abs(x-dx1)/x<(1.0/dx1)){
				  for(int i=0;i<s0.length();i++){
					  char c=s0.charAt(i);
					  if(c!='0'&&Character.isDigit(c)){
						  s0=s0.substring(i).replace(".","").trim();
						  dsl=s0;//lower bound=0
						  break;
					  }
				  }
			  }
		  }

		  //System.out.println("2 s="+s+" dsu="+dsu+" dsl="+dsl+" r="+r+" 1.0/(2*errorLimit-1)="+1.0/(2*errorLimit-1)+"  x="+x+" dxu="+dx2+" dxl="+dx1);
		  
		  temp="";
		  if(dx2>=0)
			  temp+="+"+dsu;
		  if(dx1>=0)
			  temp+="-"+dsl;
		  
		  ds=temp;
	  }catch(Exception e){
		  return;
	  }
  }
  
  public String getNumberFormat(String s) {
	  String s0=s;
	  s=s.toUpperCase();
	  
	  int p=s.indexOf("E");
	  if(!Str.isNumeric(s) || p<=0)
		  return s0;
	  
	  try {
		  int nexp=Integer.parseInt(s.substring(p+1).trim());
		  s=s.substring(0,p).trim();
		  
		  int pDecimal=s.indexOf(".");
		  
		  String intS=s;
		  int nDecimalDigits=0;
		  if(pDecimal>=0) {
			  nDecimalDigits=s.substring(pDecimal+1).length();
			  intS=s.substring(0,pDecimal);
		  }
		  
		  if(nexp>0) {//move "." to right
			  int n=nexp-nDecimalDigits;
			  s=s.replace(".", "");
			  if(n>0) {
				  for(int i=0;i<n;i++)
					  s+="0";
			  }else {
				  n=-n;
				  int len=s.length();
				  if(n>0) {
					  s=s.substring(0,len-n)+"."+s.substring(len-n);
					  while(s.charAt(0)=='0') {
						  if(s.length()>1 && s.charAt(1)!='.') {
							  s=s.substring(1);
							  continue;
						  }
						  
						  break;
					  }
				  }
			  }
		  }else {//move "." to left, adding leading zeros if moving steps>length before "."		  
			  char c=s.charAt(0);
			  String sign=c+"";
			  if(c!='+' && c!='-')
				  sign="";
			  else {
				  s=s.substring(1);
				  intS=intS.substring(1);
			  }
			  
			  s=s.replace(".", "");
			  nexp=-nexp;
			  
			  int n=nexp-intS.length();
			  if(n>=0) {
				  for(int i=0;i<n;i++)
					  s="0"+s;
				  
				  s="0."+s;
			  }else {//moving steps (nexp) <length before "."
				  n=-n;
				  s=s.substring(0,n)+"."+s.substring(n);
				  s=sign+s;
			  }			  
		  }
		  
		  return s;
	  }catch(Exception e) {
	  }
	  
	  return s0;
  }
  
  public String getExpoFormat(String s,int nexp) {
	  String s0=s;
	  s=s.toUpperCase();
	  
	  int p=s.indexOf("E");
	  if(!Str.isNumeric(s) || p>0)
		  return s0;

	  s=s+"E"+(-nexp);
	  String baseS=getNumberFormat(s);
	  return baseS+"E"+nexp;
  }
  
  private static double getPrecision(double xi) {
		double f=1.0E-12;
		if(forcePrecision) {
			f=1.0E-8;
			if(Math.abs(xi)>1.0E3) {
				int n=(int) Math.log10(Math.abs(xi))+1-3;
				f=f/Math.pow(10, n);
			}
		}

		
		return f;
  }

  
  public void setValues(double xi,double dxiu,double dxil){  

	  setValues0(xi,dxiu,dxil);
 
	  //System.out.println("XDX2SDS 548 xi="+xi+"  "+dxiu+"  "+dxil+" s="+s+" ds="+ds+" errorLimit="+errorLimit+"  dx="+dx);
	  
	  
	  if(errorLimit>=99 || !isAddExtraDigitForLargeUnc)  		  
		  return;
	  
	  String tempS=bases;
	  if(tempS.startsWith("+")||tempS.startsWith("-"))
		  tempS=tempS.substring(1).trim();
	  
	  int nsig=Str.findNSig(tempS);

	  if(nsig<=2 && Str.isNumeric(tempS) && dsu.length()==1 && Str.isInteger(dsu) && dsl.length()==1 && Str.isInteger(dsl)) {
		  double r1=0,r2=0;
		  double tempX=0,tempDX1=0,tempDX2=0;

		  //System.out.println(nsig);
		  
		  int power=Str.findNDigitsAfterDot(tempS);
		  if(power<0)
			  power=0;
		 
		  //System.out.println(errorLimit+" "+power);
		  
		  tempX=Double.parseDouble(tempS)*Math.pow(10,power);

		  tempDX1=Double.parseDouble(dsu);
		  tempDX2=Double.parseDouble(dsl);

		  if(tempX==0) {
			  r1=1;
			  r2=1;
		  }else{
			  r1=Math.abs(tempDX1/tempX);
			  r2=Math.abs(tempDX2/tempX);
		  }
		  
		  //System.out.println(tempS+" r1="+r+" dsu="+dsu+" r2="+r2+" dsl="+dsl+"  errorLimit="+errorLimit);
		  
		  if( (r1>=0.5||r2>=0.5) && (tempDX1>errorLimit||tempDX2>errorLimit)) {
			  errorLimit=99;
			  setValues0(xi,dxiu,dxil);
		  }
	  }
  }
  
  public void setValues0(double xi,double dxiu,double dxil){  	
	  
	  
      double prec=getPrecision(xi);
	  
	  //set values with the symmetrized (x,dx) par=ur
	  double[] xdx=symmetrize(xi,dxiu,dxil);
	  setValues(xdx[0],xdx[1]);

      //debug
      //System.out.println("   xi="+xi+" dxiu="+dxiu+" dxil="+dxil+" xdx[0]="+xdx[0]+" xdx[1]="+xdx[1]+"  s="+s+" ds="+ds);
      //System.out.println(" x="+x+" dxu="+dxu+" dxl="+dxl+" s="+s+" ds="+ds+" errorLimit="+errorLimit);
	  
      x0=xi;
      dxu0=dxiu;
      dxl0=dxil;
	  
      if(Math.abs(dxil)<(Math.abs(xi)*prec)) {
			if(dxil>0) {
				int np=(int)Math.ceil(Math.log(dxil));
				if(Math.pow(10, np)<Math.abs(xi)*prec)
					dxil=0;
			}else
				dxil=0.0;
      }
      if(Math.abs(dxiu)<(Math.abs(xi)*prec)) {
			if(dxiu>0) {
				int np=(int)Math.ceil(Math.log(dxiu));
				if(Math.pow(10, np)<Math.abs(xi)*prec)
					dxiu=0;
			}else
				dxiu=0.0;
      }


      //System.out.println("   xi="+xi+" dxiu="+dxiu+" dxil="+dxil+" xdx[0]="+xdx[0]+" xdx[1]="+xdx[1]);
      
	  if(dxiu==dxil || dxiu<0f || dxil<0f || (dxiu+dxil)==0f){
		  if(dxiu!=dxil)
			  hasSymmetrized=true;	  
		  else if(dxil==xi) {
			  isLimits=true;
		  }
		  return;
	  }

	  double diff=Math.abs(xi-dxil);
	  
	  //System.out.println("diff="+diff+"  "+Math.abs(xi)*prec+"  "+Math.abs(dxiu-dxil)+"  "+Math.abs(xi)*prec+" xi="+xi+" prec="+prec);
			  
	  if( diff<=Math.abs(xi)*prec && Math.abs(dxiu-dxil)<Math.abs(xi)*prec) {
	  //if( diff<=Math.abs(xi)*prec) {
		  isLimits=true;
		  return;
	  }
	  
	  //System.out.println(diff+"  "+xi+"  "+(dxiu-dxil));
	  
	  //isLimits=false;//it could be set true in setValues(xdx[0],xdx[1]) above, which could be not expected
	  
	  //double dd=Math.abs(dxiu-dxil);
	  double r=Math.abs((dxiu-dxil)/(dxiu+dxil));
	  
	  XDX2SDS xs0=new XDX2SDS(xi,xdx[1],errorLimit);
	  XDX2SDS xs1=null,xs2=null;
	  
	  //System.out.println("XDX2SDS 642: original x and symmetrided ds: s="+xs0.s+" symmetrized ds="+xs0.ds+" errorLimit="+errorLimit);
	  //System.out.println("      symmetrized s="+this.s+" ds="+this.ds);	  
	  //System.out.println("r="+r+" dd/xi="+(Math.abs(dxiu-dxil)/xi)+" 1.0/(errorLimit)="+1.0/(errorLimit)+"  xi="+xi+" dxiu="+dxiu+" dxil="+dxil);
	  
	  
	  //check if symmetrized s and ds are very close to original values
	  //if so, return symmetrized s and ds, otherwise proceed to process 
	  //original dsu and dsl as asymmetric values
	  if(r<=1.0/errorLimit && this.s.equals(xs0.s) && Math.abs(xdx[0])>=xdx[1]){
	  //if(r<=1.0/errorLimit && dd<0.001*Math.abs(xi)){
	  //if(!dsu.equals(dsl) && r<=1.0/(2*errorLimit-1)){
		  xs1=new XDX2SDS(xi,dxiu,errorLimit);
		  xs2=new XDX2SDS(xi,dxil,errorLimit);
		  if(xs1.s.equals(xs2.s) && this.s.equals(xs1.s)) {
			  double dx1=Double.parseDouble(xs1.ds());
			  double dx2=Double.parseDouble(xs2.ds());
			  
			  double dds=Math.abs(dx1-dx2);
			  r=dds/(dx1+dx2);
			  
			  if(xs1.ds.equals(xs2.ds) || (dds<=1&&r<0.05) || r<=0.04) {//+10-11 can be symmetrized, but +9-10 not
				  canBeSymmetrized=true;
				  hasSymmetrized=true;	
				  
				  s0=this.s;
				  dsu0=xs1.ds();
				  dsl0=xs2.ds();
				  //System.out.println("XDX2SDS 540:  isLimits="+isLimits+" dsu="+dsu+" dsl="+dsl+" r="+r+" 1.0/(2*errorLimit-1)="+1.0/(2*errorLimit-1)+"  xi="+xi+" dxiu="+dxiu+" dxil="+dxil);
				  
				  return;
			  }

		  }

	  }
	  
	  //set asymmetric values
	  x=xi;
	  dxu=dxiu;dxl=dxil;
	  xf=(float)x;
	  dxuf=(float)dxiu;
	  dxlf=(float)dxil;
	  
	  isLimits=false;

	  if(dxu>0 && dxl>0 && dxu!=dxl)
		  dx=-1;
	  
	  //System.out.println("XDX2SDS 544: input: x="+x+" dxu="+dxu+" dxl="+dxl+" errorLimit="+errorLimit+" "+defaultErrorLimit+" "+(dxl==0));
	  

	  xs1=null;xs2=null;
	  
	  boolean isLowerLimit=false,isUpperLimit=false;
	  if(x==0 && (dxu==0 || dxl==0)) {
		  //do nothing here and go to next step
	  }else {
		  int[] limits={errorLimit,99,defaultErrorLimit,9};
		  		  
		  if(errorLimit==defaultErrorLimit){
			  if(errorLimit==99) {
				  limits=new int[]{99,9,4};
			  }else if(errorLimit<9) {
				  if(errorLimit<=4)
					  limits=new int[]{defaultErrorLimit,9,99};
				  else
					  limits=new int[]{defaultErrorLimit,9,99,4};
			  }else {
				  limits=new int[]{defaultErrorLimit,99,9,4};
			  }
		  }else if(errorLimit>999) {
		      Vector<Integer> temp=new Vector<Integer>();
		      int n=999;
		      while(true) {
		          temp.add(0,n);
		          n=n*10+9;
		          if(n>=errorLimit)
		              break;
		      }
		      
		      temp.add(0,errorLimit);
		      temp.add(99);
		      temp.add(defaultErrorLimit);
		      limits=new int[temp.size()];
		      for(int i=0;i<limits.length;i++)
		          limits[i]=temp.get(i).intValue();
		  }else {
			  Vector<Integer> temp=new Vector<Integer>();
			  temp.add(4);
			  temp.add(9);
			  temp.add(99);
			  if(!temp.contains(defaultErrorLimit))
				  temp.add(defaultErrorLimit);
			  if(temp.contains(errorLimit))
				  temp.removeElement(errorLimit);
			  
			  if(temp.size()>1) {
				  int[] tempLimits=new int[temp.size()];
				  for(int i=0;i<temp.size();i++)
					  tempLimits[i]=temp.get(i);
				  
				  Arrays.sort(tempLimits);
				  
			      limits=new int[temp.size()+1];
			      limits[0]=errorLimit;
			      for(int i=1;i<limits.length;i++) {
			          limits[i]=tempLimits[i-1];
			          //System.out.println("XDX2SDS 602: limit#"+i+"="+limits[i]+" errorLimit="+errorLimit);
			      }
			  }else {
				  temp.add(0,errorLimit);
				  
			      limits=new int[temp.size()];
			      for(int i=0;i<limits.length;i++) {
			          limits[i]=temp.get(i).intValue();
			          //System.out.println("XDX2SDS 602: limit#"+i+"="+limits[i]);
			      }
			  }
			  

		  }

		  
		  //store original setting
		  boolean isAddExtraDigitForLargeUnc0=isAddExtraDigitForLargeUnc;

		  
		  try {
			  isAddExtraDigitForLargeUnc=false;//this is for setValues(x,dx) call below
			  
			  for(int i=0;i<limits.length;i++){
				  xs1=new XDX2SDS(xi,dxiu,limits[i],nsig);
				  su=xs1.su;
		          
				  if(!Str.isNumeric(xs1.ds))
					  continue;
				  
				  for(int j=0;j<limits.length;j++){
					  xs2=new XDX2SDS(xi,dxil,limits[j],nsig);
					  sl=xs2.sl;
					  
					  if(!Str.isNumeric(xs2.ds))
						  continue;
					  
					  //debug
					  //System.out.println("XDX2SDS 784: limits.length="+limits.length+" x="+xi+" dxiu="+dxiu+" dxil="+dxil+
					  // " xs1="+xs1.s+" "+xs1.ds+" xs2="+xs2.s+" "+xs2.ds+"  limit1="+limits[i]+" limit2="+limits[j]+" sl="+sl+" su="+su);
					  
					  boolean isEqualS=false;
					  if(xs1.s.equals(xs2.s))
						  isEqualS=true;
					  else if(xs1.isExpo!=xs2.isExpo){//e.g, 1.9E-4 8 with isExpo=true and 0.00019 13 with isExpo=false (NOTE: when dx<0.0001, exponential format is used by default
						  String s1=xs1.s;
						  String s2=xs2.s;
						  if(xs1.isExpo)
							  s1=getNumberFormat(s1);
						  else
							  s2=getNumberFormat(s2);
						  
						  int nsig1=Str.findNSig(xs1.s);
						  int nsig2=Str.findNSig(xs2.s);
						  if(s1.equals(s2) && nsig1==nsig2)
							  isEqualS=true;
						  
						  //System.out.println("s1="+s1+" s2="+s2+" nsig1="+nsig1+" nsig2="+nsig2);
					  }
					  
					  //System.out.println("  (xs1.s.equals(xs2.s))="+(xs1.s.equals(xs2.s))+" isEqualS="+isEqualS);
					  
					  if(isEqualS){
						  XDX2SDS xs=xs1;
						  if(xs1.isExpo!=xs2.isExpo) {//use number format instead of exponential format 
							  if(xs1.isExpo)
								  xs=xs2;
							  else
								  xs=xs1;
						  }
						  //System.out.println("In XDX2SDS end 1: s="+s+" ds="+ds+" x="+xi+" dxiu="+dxiu+" dxil="+dxil+" xs1="+xs1.s+" "+xs1.ds+" xs2="+xs2.s+" "+xs2.ds+"  limit1="+limits[i]+"  limit="+limits[j]);
							  
						  s=xs.s;
						  
						  dsu=xs1.ds;
						  dsl=xs2.ds;

						  exp=xs.exp();
						  basex=x;
						  
						  exps=xs.exps();
						  bases=xs.bases();
				  
						  /*
						  if(dsu.equals(dsl) && !dsu.equals(ds)) { 
							  //e.g. x1=0.00746 +52-46, limit=50, in the end xs1=xs2=0.0075 5, s=0.0075, but symmetrized ss=0.00746 ds=49)
							  //then, it will be wrong to use s+/-ds=0.0075 49
							  canBeSymmetrized=true;
							  hasSymmetrized=true;	
							  
							  ss=s;
							  ds=dsu;
						  }
						  */
						  if(dsu.equals(dsl))
							  ds=dsu;
						  else 
							  ds="+"+dsu+"-"+dsl;
						  
						  //System.out.println("In XDX2SDS end 2: s="+s+" ds="+ds+" x="+xi+" dxiu="+dxiu+" dxil="+dxil+" xs1="+xs1.s+" "+xs1.ds+" xs2="+xs2.s+" "+xs2.ds+"  limit1="+limits[i]+"  limit="+limits[j]);
						  //System.out.println(s+"  "+dsu+"  "+dsl);
						  
						  checkDS();
		
						  //System.out.println("In XDX2SDS 850: s="+s+" ds="+ds+" x="+xi+" dxiu="+dxiu+" dxil="+dxil+" xs1="+xs1.s+" "+xs1.ds+" xs2="+xs2.s+" "+xs2.ds+"  limit1="+limits[i]+"  limit="+limits[j]);
						  
						  diff=Math.abs(x-dxl);
						  if( diff<=Math.abs(x)*prec && Math.abs(dxu-dxl)<Math.abs(x)*prec) {
							  //do nothing, go to next step
						  }else							  
							  return;
		
					  }else if(dxl==0 && dxu>0) {
						  if(dxu>1000*x) {//x is lower limit
							  //do nothing, go to next step
							  isLowerLimit=true;
						  }else {
							  s=xs1.s;
							  dsu=xs1.ds;
							  dsl="0";
							  
							  exp=xs1.exp();
							  basex=x;
							  
							  exps=xs1.exps();
							  bases=xs1.bases();
							  checkDS();
							  
							  return;
						  }

						  //ds="+"+dsu+"-"+dsl;

						  //System.out.println("In XDX2SDS end: s="+s+" ds="+ds+" x="+xi+" dxiu="+dxiu+" dxil="+dxil+" xs1="+xs1.s+" "+xs1.ds+" xs2="+xs2.s+" "+xs2.ds+"  limit1="+limits[i]+"  limit="+limits[j]);
						  //System.out.println("        ds="+ds+"  dsu="+dsu+" dsl="+dsl);
						  
						  
					  }else if(dxu==0 && dxl>0) {
						  //System.out.println("x="+x+" dxu="+dxu+" dxl="+dxl+" "+(Math.abs(x-dxl)<Math.abs(x)*prec) );
						  
						  if(Math.abs(x-dxl)<Math.abs(x)*prec) {//x is upper limit
							  //do nothing, go to next step
							  isUpperLimit=true;
						  }else {
							  s=xs2.s;
							  dsu="0";
							  dsl=xs2.ds;
							  
							  exp=xs2.exp();
							  basex=x;
							  
							  exps=xs2.exps();
							  bases=xs2.bases();
							  checkDS();
							  
							  return; 
						  }
						  //System.out.println("In XDX2SDS 773: s="+s+" ds="+ds+" x="+xi+" dxiu="+dxiu+" dxil="+dxil+" xs1="+xs1.s+" "+xs1.ds+" xs2="+xs2.s+" "+xs2.ds+"  limit1="+limits[i]+"  limit="+limits[j]);
						  //System.out.println("        ds="+ds+"  dsu="+dsu+" dsl="+dsl);
					  }
				  }
			  } 
		  }catch(Exception e) {
			  
		  }finally{
			  isAddExtraDigitForLargeUnc=isAddExtraDigitForLargeUnc0;//restore original setting
		  }
  
	  }

	  
	  //if reaching here, it means dxiu and dxil differ significantly
      //s and ds is from symmetrized values

	  
	  xs1=new XDX2SDS(xi,dxiu,defaultErrorLimit,nsig);
	  xs2=new XDX2SDS(xi,dxil,defaultErrorLimit,nsig);
	  
	  su=xs1.su;
	  sl=xs2.sl;
	  
	  su=Str.removeExtraZerosAfterDot(su,2);
	  sl=Str.removeExtraZerosAfterDot(sl,2);
	  
	  
	  //System.out.println("x="+xi+" su="+su+" sl="+sl+" dxiu="+dxiu+" dxil="+dxil);
	  
	  //System.out.println("### x="+x+" "+dx+" dxu="+dxu+" dxl="+dxl+ "  s="+s+" dsu="+dsu+" dsl="+dsl+" su="+xs1.su+" sl="+xs2.sl+" "+isLowerLimit+" "+isUpperLimit+" prec="+prec);
	  
	  if(isLowerLimit) {
		  dx=-1;
		  ds="GT";
		  s=sl;
	  }else if(isUpperLimit) {
		  dx=-1;
		  ds="LT";
		  s=su;
	  }else {
		  hasSymmetrized=true;
	  }
	  
	  isLimits=true;
  }
  
  
  public void setValues(float xi,float dxiu,float dxil){
	    setValues((double)xi,(double)dxiu,(double)dxil);
  }
  
  
  public void setValues(double xi,double dxi) {
	  setValues0(xi,dxi);
	  
	  //System.out.println(isAddExtraDigitForLargeUnc+" "+errorLimit);
	  //System.out.println(sl);
	  
	  //System.out.println(s+" "+ds);
	  
	  if(errorLimit>=99 || !isAddExtraDigitForLargeUnc)  		  
		  return;
	  
	  String tempS=bases;
	  if(tempS.startsWith("+")||tempS.startsWith("-"))
		  tempS=tempS.substring(1).trim();
	  
	  int nsig=Str.findNSig(tempS);
	  
	  //System.out.println(nsig+"   s="+s+" ds="+ds);
	  
	  if(nsig<=2 && ds.length()==1 && Str.isInteger(ds)) {
		  double r=0;
		  double tempX=0,tempDX=0;
		  
		  int power=Str.findNDigitsAfterDot(tempS);
		  if(power<0)
			  power=0;
		  		  
		  tempX=Double.parseDouble(tempS)*Math.pow(10,power);
		  
		  tempDX=Double.parseDouble(ds);

		  if(tempX==0) {
			  r=1;
		  }else{
			  r=Math.abs(tempDX/tempX);
		  }
		  
		  //System.out.println(tempS+" r="+r+" ds="+ds+" "+errorLimit);
		  
		  if( r>=0.5) {
			  errorLimit=99;
			  setValues0(xi,dxi);
		  }
	  }
  }
  
  public void setValues0( double xi, double dxi){
      
	double prec=getPrecision(xi);
	
	//System.out.println(prec+" "+Math.abs(dxi)+" "+Math.abs(xi)*prec);
	
	if(Math.abs(dxi)<Math.abs(xi)*prec) {
		if(dxi>0) {
			int np=(int)Math.ceil(Math.log(dxi));
			if(Math.pow(10, np)<Math.abs(xi)*prec)
				dxi=0;
		}else
			dxi=0.0;
	}

      
    x=xi;sx=xi;
    dx=dxi;
    dxu=dxl=dx;
    
	x0=x;
    dxu0=dxu;dxl0=dxl;
	  
    xf=(float)xi;
    dxf=(float)dxi;
    dxuf=dxlf=dxf;
    
    basex=xi;
 
    //System.out.println("&&&&nsig="+nsig+" xi="+xi+" dxi="+dxi);
    //System.out.println("0**** nsig="+nsig+" su="+su+" sl="+sl+" dsl="+dsl+"  dsu="+dsu+" s="+s+" ds="+ds+" x="+x);
    
    calculate();
    
    //System.out.println("1**** nsig="+nsig+" su="+su+" sl="+sl+" dsl="+dsl+"  dsu="+dsu+" s="+s+" ds="+ds+" x="+x);
    
    ss=s;
    dss=ds;
    dsu=dsl=ds;
    
    double xu=xi+dxi;
    double xl=xi-dxi;
	if(xi>0 && Math.abs(xl)<xi*prec)
		xl=0.0;
		
	if(xi<0 &&Math.abs(xu)<Math.abs(xi)*prec)
		xu=0.0;
	
	su=Str.findBestStringOfNumber(xu, nsig);
	sl=Str.findBestStringOfNumber(xl, nsig);
    
    double diff=Math.abs(xi-dxi);
    if( diff<=Math.abs(xi)*prec) 
		  isLimits=true;
   
    //System.out.println("2**** nsig="+nsig+" su="+su+" sl="+sl+" dsl="+dsl+" dsu="+dsu+" s="+s+" x="+x+" xl="+xl+" xu="+xu);
 }

  public void setValues( float xi, float dxi){
    setValues((double)xi,(double)dxi);
  }

  
  public void setNsig(int nsig){this.nsig=nsig;}
  public void setErrorLimit(int limit){
	try{
		if(limit<=0)
			limit=defaultErrorLimit;
		
		//if(limit<defaultErrorLimit)
		//	errorLimit=defaultErrorLimit;
		//else
			errorLimit=limit;
	}catch(Exception e){}
  } 
  
  
  public int getErrorLimit(){return errorLimit;}
  
  public static void setExpFormatLimitSmallNumber(double d) {
	  if(d<=0)
		  return;
	  expFormatLimitSmallNumber=d;

  }
/* ************************************************************************************************ */
 
  
  public double[] symmetrize(double x,double dxu,double dxl){
	  double[] out=new double[2];
	  out[0]=x;out[1]=dxu;
	  if(dxu>=0 && dxl>=0 && dxu!=dxl){
		  double f=(1-2.0/Math.PI)*(dxu-dxl)*(dxu-dxl)+dxu*dxl;
		  out[0]=x+0.64*(dxu-dxl);
		  out[1]=Math.sqrt(f);
	  }
	  
	  return out;
  }
  
  public void calculate(){
	  calculate(nsig);
  }
  
  
  public void calculate(int nsig){
	  calculate(nsig,errorLimit);
  }
  
  //nsig=number of significant digits to keep in value if no uncertainty 
  //errorLimit=largest allowed error in ENSDF format, default=35
  public void calculate(int nsig,int errorLimit){
	  
    sysout=false;
    //if(errorLimit<defaultErrorLimit)
    //	errorLimit=defaultErrorLimit;
    
    if(nsig<1)
    	nsig=3;
    	
    //System.out.println("IN calculate: nsig="+nsig+" x="+x+" dx="+dx+" errorLimit="+errorLimit);
    
    if( dx>0){
    	/* According to ENSDF rules, 35 is the largest possible error: */
        //a=Math.floor( Math.log(errorLimit/dx)/Math.log(10.) +1.0E-6);//note that for errorLimit=2, dx=0.002, a is supposed to be 3, but here it gives a=2 from floor(2.99999999999999999999999)
    	
    	if(Str.roundUncertainty(dx)==errorLimit)
    		a=0;
    	else
    		a=Math.floor( Math.log10(errorLimit/dx)+1.0E-10);
    	
        ia=(long) a;   /* ia is the power of 10 required to bring dx less than errorLimit */
        
        dxn=roundUncertainty( dx*Math.pow(10.,a));//round ##.4 to #(#+1) for uncertainty
        xn=round( x*Math.pow(10.,a),0.05);//in case, #.5 from input could become #.49999, round xxx.45 up, standard rounding is down
       
        if(dxn>xn && round( dx*Math.pow(10.,a))==xn)
        	dxn=xn;
        
        idxn=(long) dxn;
        ds=Long.toString(idxn);

        
        ixn=(long) xn;
        sn=Long.toString(ixn);
        if( sn.startsWith("-") ){ sign="-"; sn=sn.substring(1);}
        lsn=sn.length();
        
        
        //System.out.println("In XDX2SDS 962: xn="+xn+" dxn="+dxn+" ixn="+ixn+" idxn="+idxn+" sn="+sn+" ia="+ia+" lsn="+lsn);
        //System.out.println(x*Math.pow(10.,a));
        
        //System.out.println("In XDX2SDS: x="+x+" dx="+dx+" a="+a+" ia="+ia+" dxn="+dxn+" xn="+xn+" lsn="+lsn+" sn=*"+sn+"*"+" dx*Math.pow(10.,a)="+dx*Math.pow(10.,a)+"  x*Math.pow(10.,a)="+ x*Math.pow(10.,a));
        //System.out.println("       errorLimit="+errorLimit+"  Math.log(errorLimit/dx)/Math.log(10.)="+(Math.log(errorLimit/dx)/Math.log(10.))+"  errorLimit/dx="+(errorLimit/dx)
        //		+"  Math.log(errorLimit/dx)="+(Math.log(errorLimit/dx))+" Math.log(10.)="+Math.log(10.));
        
        /*
        if(sn.replace("0","").trim().length()==0 && Math.abs(dx)<=0.001){
            s="0";
            if(Double.toString(x).contains("E-")){
                s=Double.toString(x).substring(0,1)+".";
                if(Double.toString(x).indexOf("E")<4)
                	s+=Double.toString(x).substring(2,Double.toString(x).indexOf("E"))+Double.toString(x).substring(Double.toString(x).indexOf("E"));
                else 
                	s+=(int)(Double.parseDouble(Double.toString(x).substring(2,4))/10+.5)+Double.toString(x).substring(Double.toString(x).indexOf("E"));
                
                //System.out.println(s);
            }
            x=0;
            ds="AP";
            xf=0.0f;
            dxf=0.0f;
        }else*/
        {
            if( a >= 0 ){

                if( lsn < ia ){ /* ex x=0.0040 dx=0.0001 */

                    if( Math.abs(x) >= XDX2SDS.expFormatLimitSmallNumber){ /* x=0.044 dx=0.007 will go as 0.044 7 */

                        s=sign+"0.";
                        for( int k=1; k<= (ia-lsn); k++){ s=s+"0";}
                        s=s+sn;
                    }
                    else{  /* x=0.00044 dx=0.00007 will go as 4.4E-4 7*/

                  	  if(lsn>1)
                  		  s=sign+sn.substring(0,1)+"."+sn.substring(1)+"E"+(sn.length()-1-ia);
                  	  else {
                  		  int n=(int)Math.abs(sn.length()-1-ia);
                  		  if(x==0 && n<5) {
                  			  s=sign+"0.";
                  			for( int k=1; k<=n; k++){
                  				s=s+"0";
                  			}
                  			
                  		  }else
                  			  s=sign+sn.substring(0,1)+"E"+(sn.length()-1-ia);
                  	  }
                    }
                } 
                else if( lsn == ia ){ 
                    s=sign+"0."+sn;
                }
                else{
                    if( ia == 0 ) 
                    	s=sign+sn; 
                    else 
                    	s=sign+sn.substring(0,(int)(lsn-ia))+"."+sn.substring((int)(lsn-ia));
                    
                    //System.out.println("In XDX2SDS 907: dx="+dx+" s="+s+" ds="+ds+" sn="+sn+" ia="+ia+" lsn="+lsn+" errorLimit="+errorLimit);
                }
              }
              else if( a < 0 ){ /* ex x=2500 dx=200 */
            	if(sn.length()>1)
            		s=sign+sn.substring(0,1)+"."+sn.substring(1)+"E"+(sn.length()-1-ia);
            	else
            		s=sign+sn+"E"+(sn.length()-1-ia);
            	
            	ds=Long.toString(idxn);  
              }
            
        	//System.out.println("In XDX2SDS:    x="+x+" dx="+dx+" a="+a+" ia="+ia+" lsn="+lsn+" sn="+sn+" s="+s+" ds="+ds);

        }
    	
    }else{
      	/* If double error is negative: */
      	/* will round to nsig non-zero numbers after the last zero */
    	
    	s=Str.findBestStringOfNumber(x, nsig);
    	
    	if(dx==0)
    		ds="0";
    	else
    		ds="";
        
        //System.out.println("XDX2SDS 1017: x="+x+" "+dx+"  s="+s+" nsig="+nsig);
    }
    
    dsu=ds;
    dsl=ds;
    
    
    //System.out.println("In XDX2SDS: s="+s+" ds="+ds+" sn="+sn+" ia="+ia+" lsn="+lsn);
    
    int ie=s.indexOf("E");
    basex=x;
    if(ie>0){
    	try{
    		exps=s.substring(ie+1).trim();
    		exp=Integer.parseInt(exps);
    		bases=s.substring(0, ie).trim();
    		basex=Float.parseFloat(bases);
    		
    		isExpo=true;
    	}catch(Exception e){
    		exp=0;
    		exps="";
    		basex=x;
    		bases=s;
    	}
    	
    	
    	
    }else{
    	exp=0;
    	bases=s;
    	exps="";
    }
    
    //System.out.println("In XDX2SDS: s="+s+" ds="+ds+" sn="+sn+" ia="+ia+" lsn="+lsn);
  }
  

  
  //nsig=number of significant digits to keep in value if no uncertainty 
  public void calculate_old(int nsig){
    sysout=false;

    if( dx > 0 && Math.abs(x) < 0.8*dx ){
        s="0";
        if(Double.toString(x).contains("E-")){
            s=Double.toString(x).substring(0,1)+".";
            if(Double.toString(x).indexOf("E")<4)
            	s+=Double.toString(x).substring(2,Double.toString(x).indexOf("E"));
            else 
            	s+=(int)(Double.parseDouble(Double.toString(x).substring(2,4))/10+.5)+Double.toString(x).substring(Double.toString(x).indexOf("E"));
        }
        x=0;
        ds="AP";
        xf=0.0f;
        dxf=0.0f;
    }

    else if( dx > 0 && Math.abs(x) >= 0.8*dx){


/* According to ENSDF rules, 35 is the largest possible error: */

      a=Math.floor( Math.log(((double)defaultErrorLimit)/dx)/Math.log(10.) ); 
      ia=(long) a;   /* ia is the power of 10 required to brin dx less than 35 */
      
      dxn=roundUncertainty( dx*Math.pow(10.,a) );//round ##.4 to #(#+1)
      
      idxn=(long) dxn;
      ds=Long.toString(idxn);

      xn=round( x*Math.pow(10.,a) );
      ixn=(long) xn;
      sn=Long.toString(ixn);
      if( sn.startsWith("-") ){ sign="-"; sn=sn.substring(1);}
      lsn=sn.length();

//        System.out.println("a= "+a+", idxn= "+idxn);


      //debug
      //System.out.println("In XDX2SDS:  a="+a+" ia="+ia+" dxn="+dxn+" idxn="+idxn+" ds="+ds+" xn="+xn+" ixn="+ixn+" sn="+sn+" lsn="+lsn);

        if( a >= 0 ){

          if( lsn < ia ){ /* ex x=0.0040 dx=0.0001 */

              if( Math.abs(x) > 0.001 ){ /* x=0.044 dx=0.007 will go as 0.044 7 */

                  s=sign+"0.";
                  for( int k=1; k<= (ia-lsn); k++){ s=s+"0";}
                  s=s+sn;
//                  if( !canGetSDS() && sysout ) System.out.println("Case A");
              }
              else{  /* x=0.00044 dx=0.00007 will go as 4.4E-4 7 */

            	  if(lsn>1)
            		  s=sign+sn.substring(0,1)+"."+sn.substring(1)+"E"+(sn.length()-1-ia);
            	  else
            		  s=sign+sn.substring(0,1)+"E"+(sn.length()-1-ia);
            	  
 //                 if( !canGetSDS() && sysout ) System.out.println("Case B");
              }
          } 
          else if( lsn == ia ){ 
              s=sign+"0."+sn;
              //if( !canGetSDS() && sysout ) System.out.println("Case C");
          }
          else{
              if( ia == 0 ) s=sign+sn; 
              else s=sign+sn.substring(0,(int)(lsn-ia))+"."+sn.substring((int)(lsn-ia));
//              if( !canGetSDS() && sysout ) System.out.println("Case D");
          }
        }
        else if( a < 0 ){ /* ex x=2500 dx=200 */
          s=sign+sn.substring(0,1)+"."+sn.substring(1)+"E"+(sn.length()-1-ia);
          ds=Long.toString(idxn);
//              if( !canGetSDS() && sysout ) System.out.println("Case E");        
        }
        //if( dx > Math.abs(x) ){  /* ex x=0.1 dx=0.4 */
//          if( !canGetSDS() && sysout ) System.out.println("Case F");
        //}
    }

/* If double error is negative: */

    else{ 
        s=Double.toString(x);
        ds="";

/* will round to nsig non-zero numbers after the last zero */

//        System.out.println("begin S: "+s);

        String sign="";String basePartS="";String expoPartS="";String integerPartS=""; String floatPartS="";

        int isign=s.indexOf("-");
        if( isign == 0 ) { sign="-"; s=s.substring(1); }

//        System.out.println(" S: "+s+" sb: "+sb);

        int iE=s.indexOf("E");
        if( iE > 0 ){ expoPartS=s.substring(iE); s=s.substring(0,iE); }

//        System.out.println(" S: "+s+" se: "+se);

        int idot=s.indexOf(".");
        if( idot < 0 ) basePartS=s;

//        System.out.println(" S: "+s+" sm: "+sm);

        if( idot >= 1) { 
             integerPartS=s.substring(0,idot); 
             floatPartS=s.substring(idot);

//             System.out.println("smi: "+smi+" smf= "+smf);

             double floatPartX=Double.parseDouble(floatPartS);
             double integerPartX=Double.parseDouble(integerPartS);
             
             int nsigAfterDot=nsig;
             
             if( floatPartX > 0 ){
               double xlogf=-1.*Math.log(floatPartX)/Math.log(10.);
               int nzerosAfterDot=(int) xlogf;

//               System.out.println(xmul+"  "+imul);

               //if the part after decimal is too small, eg, <(integer part/1000), then
               //ignore it
               double r=integerPartX/floatPartX;
               int ndigitsAfterDot=nzerosAfterDot+nsigAfterDot;
               
               int ilen=integerPartS.trim().length();
               if(r>0){
            	   ndigitsAfterDot=nsig-ilen;
            	   if(ndigitsAfterDot<=0){
            		   ndigitsAfterDot=0;
            		   floatPartS="";
            	   }
               }
               
               if(floatPartS.length()>(ndigitsAfterDot+1) ) {
                  floatPartX=floatPartX*Math.pow(10.,ndigitsAfterDot);
                  long imf=round(floatPartX); 
                  
                  if(imf>0){
                      int p1=(int)(Math.log(imf)/Math.log(10.));//still p1=2 when imf=1000, 2.99999999
                      int p2=(int)(Math.log(floatPartX)/Math.log(10.));
                 
                      String p1s=Long.toString(imf);
                      String p2s=Integer.toString((int)floatPartX);
                      p1=p1s.length();
                      p2=p2s.length();
                      
                      //System.out.println("imf="+imf+" p1="+p1+" xmf="+xmf+" p2="+p2);
                      
                      if(p1==p2)
                    	  floatPartS=floatPartS.substring(0,nzerosAfterDot+1)+Long.toString(imf);
                      else if(p1>p2){//if rounding results in one-digit increment, p1>p2
                    	  if(floatPartS.charAt(1)=='0'){//equivalent to nzerosAfterDot>0, note that charAt(0)=='.'
                    		  floatPartS=floatPartS.substring(0,nzerosAfterDot)+Long.toString(imf);
                    	  }
                    	  else{//equivalent to nzerosAfterDot=0, increment 1 in integerPart number, e.g., floatPart=.999
                    		  int i1=Integer.parseInt(integerPartS);
                    		  int i2=(Math.abs(i1)+1);
                    		  if(i1<0)
                    			  i2=-i2;
                    		  
                    		  integerPartS=Integer.toString(i2);      
                    		  floatPartS="."+Long.toString(imf).substring(1);
                    	  }
                      }  
                  }else{//1.00 floatPartS=".00"
                	  floatPartS=floatPartS.substring(0,ndigitsAfterDot+1);
                  }                	                   
//               System.out.println("imf: "+imf+" smf= "+smf);
               }              
               basePartS=integerPartS+floatPartS;       
               while( basePartS.endsWith("0") ) { 
            	   basePartS=basePartS.substring(0,basePartS.length()-1);             	   
               }
               
               if(basePartS.endsWith(".")){
            	   if(integerPartX<1000)
            		   basePartS+="0";
            	   else
            		   basePartS=basePartS.substring(0,basePartS.length()-1);
               }
             }else if(integerPartX<1000){
            	 basePartS=integerPartS+".0";
             }else{
            	 basePartS=integerPartS;
             }

//             System.out.println("smi: "+smi+" smf= "+smf);

        }
        s=sign+basePartS+expoPartS;
        

    }


  }
/* ************************************************************************************************ */

  public int exp(){return exp;}
  public String exps(){return exps;}
  public String bases(){return bases;}
  public String baseS(){return bases;}
  public double baseX(){return basex;}
  public double basex(){return basex;
  
  
  }
  public double X(){ return x;}
  public double x(){ return x;}

  public double DX(){ return dx; }
  public double dx(){ return dx; }

  public String S(){ return s; }
  public String s(){ return s; }

  public String DS(){ return ds; }
  public String ds(){ return ds; }
  public String dss(){ return dss; }//symmetrized
  
  public void setS(String es){s=es;}
  public void setDS(String des){ds=des;}
  public void setDSS(String des){dss=des;}
  
  public boolean canGetSDS(){


      xf=0.0f; dxf=0.0f;
      canGetSDS=false;
      SDS2XDX s2x=new SDS2XDX();
      s2x.setValues(s,ds);  
      xf=(float) s2x.x();
      double diffx=Math.abs( s2x.x()- x );
      if( x != 0 ) diffx=Math.abs( diffx/x );

      dxf=(float) s2x.dx();
      double diffdx=0;
      if( dx > 0 ) diffdx=Math.abs( s2x.dx()- dx )/dx;
      if( diffx < 0.15 && diffdx < 0.25 ) canGetSDS=true;
       if( ! canGetSDS && sysout ) System.out.println("problems for x="+(float)x+", dx="+(float)dx+", s= "+s+" "+ds +
      " "+(float) diffx+" "+(float)diffdx);

      return canGetSDS;

  }
  
  
  // note that Math.round() rounds 1.5 (1.4999999) to 1 not 2
  public long round(double v,double roundingOffset) {
		return Str.round(v, roundingOffset);
  } 
  public long round(double v) {
	return Str.round(v);
  }
  
  //uncertainty in ENSDF format: digit-after-decimal>=4 should be round up, or 0.35, 0.36 (0.34 rounded down)
  //e.g., 2.4 to 3, 2.3 to 2, 2.35 to 3, 2.345 to 3, 2.344 to 2
  public long roundUncertainty(double unc) {
	  return Str.roundUncertainty(unc);
  }
  
  public String printAsComment() {
		try {							
			String tempS=EnsdfUtil.printValueAsComment("",s,ds);
			return tempS;
		}catch(Exception e) {
			
		}		
		return "";
  }
  
  /*
   * print value in format of continuation record value
   */
  public String printEntry() {
	  return printEntry(this,"","");
  }
  public String printEntry(String name) {
	  return printEntry(this,name,"");
  }
  public String printEntry(String name,String unit) {
	  return printEntry(this,name,unit);
  }
  
  public static String printEntry(XDX2SDS xs,String name,String unit){
	  return printEntry(xs,name,unit,false);
  }
	/*
	 * isLowerCaseOP=true, use <, >, |>, |<, |?
	 * otherwise, use LT, GT, LE, GE, AP
	 * XDX2SDS symmetrize uncertainties if applicable and for that case the result
	 * will be the symmetrized result. Set useOriginal=true to use original asymmetrized
	 * uncertainties
	 */
	public static String printEntry(XDX2SDS xs,String name,String unit,boolean useOriginal){
        
		String ds=xs.ds().toUpperCase().trim();
	
		double x=xs.x();
		double dxu=xs.dxu();
		double dxl=xs.dxl();
		double xu=xs.x()+dxu;
		double xl=xs.x()-dxl;

		String s=xs.s;
		String dsu=xs.dsu;
		String dsl=xs.dsl;
		
		//System.out.println("XDS2SDS 1381: s="+s+" dsu="+dsu+" dsl="+dsl+" s0="+xs.s0+" dsu0="+xs.dsu0+" dsl0="+xs.dsl0+" hasSymmetried="+xs.hasSymmetrized);
		if(xs.hasSymmetrized &&useOriginal && !xs.s0.isEmpty()) {
			s=xs.s0;
			dsu=xs.dsu0;
			dsl=xs.dsl0;
		}
		
		double pres=XDX2SDS.getPrecision(x);
		if(x>0 && Math.abs(xl)<x*pres) {
			if(xl>0) {
				int np=(int)Math.ceil(Math.log(xl));
				if(Math.pow(10, np)<x*pres)
					xl=0;
			}else
				xl=0.0;
		}
		
		if(x<0 &&Math.abs(xu)<Math.abs(x)*pres) {
			if(xu>0) {
				int np=(int)Math.ceil(Math.log(xu));
				if(Math.pow(10, np)<Math.abs(x)*pres)
					xu=0;
			}else
				xu=0.0;
		}

		
		double f=xu*xl;
		
		String name0=name.trim();
		String name1=name.trim();
		if(!name1.isEmpty())
			name1=name1+"=";
		
		//System.out.println("XDX2SDS 1348: x="+xs.x()+" sl="+xs.sl()+" su="+xs.su()+" ds="+ds+" xu="+xu+" xl="+xl+" dxu="+dxu+" dxl="+dxl+" isLimit="+xs.isLimits()+" f="+f);
		
		
		String unitS="";
		if(unit.trim().length()>0)
			unitS=" "+unit;
		
		if((dxu<0&&dxl<0) || (dxu==0&&dxl==0)){//including xu==xl==0 for f==0		
			if(!Str.isNumeric(ds) && !ds.isEmpty()){
				String temp="LGA";//first letters of LT,LE,GT,GA,AP
				if(temp.indexOf(ds.charAt(0))>=0)
					return name0+" "+ds+" "+xs.s()+unitS;
				else
					return name1+xs.s()+unitS+" "+ds;//CA, SY
			}else
				return name1+xs.s()+unitS;			
		}
		
		//debug
		//if(Math.abs(xs.x()-2.76)<0.1) 
		//System.out.println("XDX2SDS 1356: x="+xs.x()+" sl="+xs.sl()+" su="+xs.su()+" xu="+xu+" xl="+xl+" dxu="+dxu+" dxl="+dxl+" isLimit="+xs.isLimits()+" f="+f);
		//System.out.println("     s="+xs.s+" dsu="+xs.dsu+"  dsl="+xs.dsl);
		
		if(!xs.isLimits()){
			if(xs.ds().isEmpty())
				return name1+xs.s()+unitS;
			if(dsu.equals(dsl))
	    		return name1+s+unitS+" "+dsu;
	    	else
	    		return name1+s+unitS+" +"+dsu+"-"+dsl;    	
		}else if(f<0){//not including xu==xl==0 for f==0	
			if(!name0.isEmpty())
				return xs.sl()+"<"+name+"<"+xs.su()+unitS;	
			else
				return xs.sl()+"<value<"+xs.su()+unitS;	
		}else if(xl==0){//f==0, xu>0
			if(ds.equals("LE"))
				return name0+" LE "+xs.su()+unitS;
			else
				return name0+" LT "+xs.su()+unitS;
		}else if(xu==0){//f==0, xl<0
			if(ds.equals("GE"))
				return name0+" GE "+xs.sl()+unitS;
			else
				return name0+" GT "+xs.sl()+unitS;
		}
		
		//here, xs.isLimit()=true, f>0 (xl!=0 and xu!=0), (dxu==0&&dxl!=0) or (dxu!=0&&dxl==0)
		// dxl < x < dxu
      //Now find out if one side of the limits can be removed.
		
		
		//System.out.println("XDX2SDS 1386 s="+xs.s()+"  dxu="+dxu+" dxl="+dxl+" dsu="+xs.dsu()+" dsl="+xs.dsl()+"  *ds="+xs.ds()+"*"+" xs.isLimits()="+xs.isLimits());
		
		double r=0;
		if(xu>0)//xl>0 also
			r=xu/xl;
		else//xu<0,xl<0
			r=Math.abs(xl/xu);
		
		if(r>1E5){//condition for one side of limit being able to be removed
			//then check which side the x value is closer to
			//note that xs.isLimit()=ture, means dxu and dxl differ significantly
			if(dxu>dxl){//x closer to xl
				if(ds.equals("GE"))
					return name0+" GE "+xs.sl()+unitS;
				else
					return name0+" GT "+xs.sl()+unitS;
			}else{//x closer to xu
				if(ds.equals("LE"))
					return name0+" LE "+xs.su()+unitS;
				else
					return name0+" LT "+xs.su()+unitS;
			}
				
		}else{
			//System.out.println(" printEntry="+xs.s()+" sl="+xs.sl()+" su="+xs.su()+" xu="+xu+" xl="+xl+"dxu="+dxu+" dxl="+dxl+" ds="+xs.ds()+"*"+" xs.isLimits()="+xs.isLimits());

			
			if(xs.sl().equals(xs.su()))
				return name1+xs.sl()+unitS;
			else if(!name0.isEmpty())
				return xs.sl()+"<"+name+"<"+xs.su()+unitS;	
			else
				return xs.sl()+"<value<"+xs.su()+unitS;				
			
		}		
	}
	public static String printEntry(XDX2SDS xs,String name){
		return printEntry(xs,name,"");	
	}
	public static String printEntry(XDX2SDS xs){
		return printEntry(xs,"","");	
	}	
  
	public static String printEntryAsIs(XDX2SDS xs,String name,String unit){
		
	  double dxu=xs.dxu();		
	  double dxl=xs.dxl();
		
	  String s="";
	  String name0=name.trim();
	  String name1=name.trim();
	  if(!name1.isEmpty())		
		  name1=name1+"=";		
		
		
	  String unitS="";	
	  if(unit.trim().length()>0)		
		  unitS=" "+unit;
	  
	  if((dxu<0&&dxl<0) || (dxu==0&&dxl==0))//including xu==xl==0 for f==0				
		  return name1+xs.s()+unitS;

	  if(xs.isLimits()){                      				
		  if(!name0.isEmpty())			
			  s=xs.sl()+"<"+name+"<"+xs.su()+unitS;				
		  else			
			  s=xs.sl()+"<value<"+xs.su()+unitS;		
	  }else if(xs.dsu().equals(xs.dsl())){		
		  s=name1+xs.s()+unitS+" "+xs.dsu(); 	
	  }else{	
		  s=name1+xs.s()+unitS+" +"+xs.dsu()+"-"+xs.dsl();	
	  }
			
	  //if(Math.abs(x-3.7)<1) System.out.println(" printEntry="+x+"  dxu="+dxu+" dxl="+dxl+" ds="+xs.dsu()+" "+xs.dsl()+"  *"+xs.ds()+"*"+" xs.isLimits()="+xs.isLimits()+" s="+s);
				
	  s=s.trim();	
	  if(dxu>=0)		
		  return s;	
	  else		
		  return xs.s().trim()+unitS;
	
  }	
	public static String printEntryAsIs(XDX2SDS xs,String name){
		return printEntryAsIs(xs,name,"");
	}
	public static String printEntryAsIs(XDX2SDS xs){
		return printEntryAsIs(xs,"","");
	}
	public static String printEntryAsIs(double x,double dxu,double dxl,String name,String unit,int uncertaintyLimit){		  
		XDX2SDS xs=new XDX2SDS(x,dxu,dxl,uncertaintyLimit);  
		return printEntryAsIs(xs,name,unit);
	} 
	public static String printEntryAsIs(double x,double dxu,double dxl,String name,int uncertaintyLimit){		  
		return printEntryAsIs(x,dxu,dxl,name,"",uncertaintyLimit);
	}
	public static String printEntryAsIs(double x,double dxu,double dxl,int uncertaintyLimit) {
		return printEntryAsIs(x,dxu,dxl,"","",uncertaintyLimit);
	}
	
  public float xf(){return xf;}
  public float dxf(){return dxf;}
  public float dxuf(){return dxuf;}
  public float dxlf(){return dxlf;}

  public double dxl(){return dxl;}
  public double dxu(){return dxu;}
  public double sx(){return sx;}
  
  public double x0(){return x0;}
  public double dxl0(){return dxl0;}
  public double dxu0(){return dxu0;}
  
  public String s0() {return s0;}
  public String ds0() {return ds0;}
  public String dsu0(){return dsu0;}
  public String dsl0(){return dsl0;}
  
  public String dsu(){return dsu;}
  public String dsl(){return dsl;}
  public String ss(){return ss;}
  public String su(){return su;}
  public String sl(){return sl;}
  
  public boolean isExpo() {return isExpo;}
  
  public boolean isLimits(){return isLimits;}
  public boolean hasSymmetrized(){return hasSymmetrized;}
  public boolean canBeSymmetrized(){return canBeSymmetrized;}
  
  public static void setIsForcePrecision(boolean b) {forcePrecision=b;}
  public static boolean isForcePrecision() {return forcePrecision;}
  
	
	
  public static boolean isAddExtraDigitForLargeUnc() {	
	  return isAddExtraDigitForLargeUnc;
  }
	
  public static void setIsAddExtraDigitForLargeUnc(boolean b) {
		XDX2SDS.isAddExtraDigitForLargeUnc=b;
  }
	
  public long ia(){return ia;}
 }         
