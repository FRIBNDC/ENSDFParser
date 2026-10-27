package ensdfparser.ensdf;

import java.util.LinkedHashMap;
import java.util.Vector;

import ensdfparser.nds.util.Str;

public class Nucleus{

 private String nucNameEnsdf;
 private String nucNameGen;

 private String ms="",dms="",sns="",dsns="",sps="",dsps="",sas="",dsas="";
 private String ZS="";//LaTeX String of Z that keeps right aligned with A String in nucleus symbol in drawings (a space is added if Z.length<A.length)
 private String JPS="";//g.s. spin and parity
 private float mass=0,sn=0,sp=0,sa=0;
 private  int A, Z, N, I, mat;
 @SuppressWarnings("unused")
 private String EN, En, elementName, ElementName, ELEMENTNAME;
 private String nucNameTUC, nameHTML, zsymam;

 private boolean isNumber;
 private boolean isValidFormat=true;
 private boolean isEvenEven=false;

	public final static String QBN_NAME="QBN";//B-N
	public final static String QB_NAME="QB";//B-
	public final static String S2N_NAME="S2N";
	public final static String SN_NAME="SN";
	public final static String SP_NAME="SP";
	public final static String QEC_NAME="QEC";//for EC/B+
	public final static String QA_NAME="QA";
	public final static String S2P_NAME="S2P";//
	public final static String QEP_NAME="QEP";//ECP/B+P
	
	private LinkedHashMap<String,Nucleus> qvNameDaughterMap=new LinkedHashMap<String,Nucleus>();//all daughters which the current nucleus decays to via all decay channels
	private LinkedHashMap<String,Nucleus> qvNameParentMap=new LinkedHashMap<String,Nucleus>();//all parents that decay to the current nucleus via all decay channels
	
 public Nucleus(){

   clearValues();

  }
 
  public Nucleus(String s)throws java.io.IOException{
      this();
      parseNUCID(s);
  }
  public Nucleus(int Z,int A){
      this();
      setZA(Z,A);
  }
  
  public static boolean isNucleus(String s) {
	  try {
		  s=s.trim();
		  Nucleus nuc=new Nucleus(s);
		  if(nuc.Z>=0 && nuc.A>0)
			  return true;

	  }catch(Exception e) {
		  
	  }
	  return false;
  }
  public void clearValues(){
    A=-1; Z=-1; N=-1; EN=""; En=""; nucNameEnsdf=""; nucNameGen=""; nucNameTUC=""; isNumber=false; nameHTML="";
    ms="";dms="";mass=0;
    ZS="";isEvenEven=false;
    JPS="";
  }

  @SuppressWarnings("unused")
public void parseNUCID(String s)throws java.io.IOException{  /* To setup a ENSDF nuclear name */

    clearValues();
    
    //s=Str.rtrim(s.toUpperCase());

    if(s.isEmpty())
        return;
    
    String errmsg="Format error in ENSDF file: wrong NUCID="+s+"! Please run format checking.\n";
    
    if(s.length()==5) {
        try {
            String AS=s.substring(0,3);
            String nameS=s.substring(3);
            
            boolean isGoodAS=(Str.isDigits(AS.trim())&&!AS.endsWith(" "));
            boolean isGoodNameS=(nameS.trim().isEmpty() || (!nameS.startsWith(" ")&&Str.isLetters(nameS.trim())) );
            if(!isGoodAS || !isGoodNameS)
            	isValidFormat=false;            
        }catch(Exception e) {
        	isValidFormat=false;
        } 
    }else {
        //throw new java.io.IOException(errmsg);
        isValidFormat=false;  
        if(s.trim().length()>5)
        	return;   	
    }
    
    s=s.toUpperCase().trim();
    
    if(s.length()==1) {
        if(s.equals("P"))
            s="1H";
        else if(s.equals("N"))
            s="1NN";
        else if(s.equals("D"))
            s="2H";
        else if(s.equals("T"))
            s="3H";
        else if(s.equals("A"))
            s="4HE";
    }else if(s.toUpperCase().equals("NN"))
    	s="1NN";

    try{
        nucNameEnsdf=s; 
        
        if(s.length()==5){
            A=Integer.parseInt( nucNameEnsdf.substring(0,3).trim() );
            EN=nucNameEnsdf.substring(3).trim();     

        }else{
            int i=0;
            String tempS=s;
            while(i<tempS.length() && !Character.isLetter(tempS.charAt(i)))
                i++;
            
                
            if(tempS.substring(0,i).trim().length()>0)
                A=Integer.parseInt( tempS.substring(0,i).trim() );

            EN=tempS.substring(i).trim();
             
        }

        En=getEn();
 
        nucNameTUC=nucNameEnsdf.trim().toUpperCase();

        Z=findZ();
        
        if(Z>=0 && A>=0){
            if(A<Z) {
                if(Z==7)//n incorrectly read as Nitrogen
                    Z=0;
                else if(Z==15)//p incorrectly read as Phosphorus
                    Z=1;
            }

            N=A-Z;
        }
        
        if(A<Z) {
        	A=-1;
        	isValidFormat=false;
        }
        ZS=makeZS(A,Z);
               
        if(EN.equals("N") && Z==0) {
        	EN="n";
        	nucNameEnsdf=nucNameEnsdf.toLowerCase();
        	nucNameTUC=nucNameTUC.toLowerCase();
        }
        if(EN.equals("P") && Z==1) {
        	EN="p";
        	nucNameEnsdf=nucNameEnsdf.toLowerCase();
        	nucNameTUC=nucNameTUC.toLowerCase();
        }
        
        elementName=findElementName();
        
        nucNameEnsdf=findNUCID();
        
        if(A>0 && Z>0 && A%2==0 && Z%2==0) {
            isEvenEven=true;
            JPS="0+";
        }

        if(Z<0 && EN.length()>0)
        	isValidFormat=false;

    }catch (Exception e){
        //throw new java.io.IOException(errmsg);
    	isValidFormat=false;
    }

  }

  /* -------------------------------------------------------------- */

  
  private LinkedHashMap<String, Nucleus> findQVNameDaughterMap() {
	  LinkedHashMap<String,Nucleus> out=new LinkedHashMap<String,Nucleus>();
	  try {
			String[] qvNames=new String[] {QBN_NAME,QB_NAME,S2N_NAME,SN_NAME,SP_NAME,QEC_NAME,QA_NAME,S2P_NAME,QEP_NAME};
			for(int i=0;i<qvNames.length;i++) {
				String qvName=qvNames[i];
				out.put(qvName,findDaughterNucleus(qvName));
			}
			qvNameDaughterMap.clear();
			qvNameDaughterMap.putAll(out);
	  }catch(Exception e) {
		  
	  }
	  
	  return out;
  }
  
  public LinkedHashMap<String, Nucleus> getQVNameDaughterMap() {
	  if(qvNameDaughterMap.isEmpty())
		  findQVNameDaughterMap();
	  
	  return qvNameDaughterMap;
  }
  
  private Nucleus findDaughterNucleus(String qvName) {
		try {			
			int A1=A;
			int Z1=Z;
			int[] dAdZ=dAdZ(qvName);
			int dA=dAdZ[0];
			int dZ=dAdZ[1];
			
			//System.out.println(qvName+"  "+dA+"  "+dZ);
			if(dA==0 && dZ==0)
				return null;
			
			A1=A1+dA;
			Z1=Z1+dZ;
			if(A1<=0 || Z<0)
				return null;
			
			Nucleus dNuc=new Nucleus(Z1,A1);
			if(dNuc.En.isEmpty())
				return null;
			
			return dNuc;
		}catch(Exception e) {
			
		}
		
		return null;
  }
  
  public Nucleus getDaughterNucleus(String qvName) {
		try {			
			return getQVNameDaughterMap().get(qvName);
		}catch(Exception e) {
			
		}
		
		return null;
  }
  
  private LinkedHashMap<String, Nucleus> findQVNameParentMap() {
	  LinkedHashMap<String,Nucleus> out=new LinkedHashMap<String,Nucleus>();
	  try {
			String[] qvNames=new String[] {QBN_NAME,QB_NAME,S2N_NAME,SN_NAME,SP_NAME,QEC_NAME,QA_NAME,S2P_NAME,QEP_NAME};
			for(int i=0;i<qvNames.length;i++) {
				String qvName=qvNames[i];
				out.put(qvName,findParentNucleus(qvName));
			}
			qvNameParentMap.clear();
			qvNameParentMap.putAll(out);
	  }catch(Exception e) {
		  
	  }
	  
	  return out;
  }
  
  public LinkedHashMap<String, Nucleus> getQVNameParentMap() {
	  if(qvNameParentMap.isEmpty())
		  findQVNameParentMap();
	  
	  return qvNameParentMap;
  }
  
  private Nucleus findParentNucleus(String qvName) {
		try {			
			int A1=A;
			int Z1=Z;
			int[] dAdZ=dAdZ(qvName);//this is: daughter-parent for decay channel=qvName
			                        
			int dA=dAdZ[0];
			int dZ=dAdZ[1];
			if(dA==0 && dZ==0)
				return null;
			
			//now we need to find A and Z of the parent that decays to current nucleus by channel=qvName
			//here dA=daughterA-parentA
			//     dZ=daughterZ-parentZ
			//current nucleus is daughter, A1 and Z1 are for parent
			A1=A1-dA;
			Z1=Z1-dZ;
			if(A1<=0 || Z<0)
				return null;
			
			Nucleus dNuc=new Nucleus(Z1,A1);
			if(dNuc.En.isEmpty())
				return null;
			
			return dNuc;
		}catch(Exception e) {
			
		}
		
		return null;
  }
  
  public Nucleus getParentNucleus(String qvName) {
		try {			
			return getQVNameDaughterMap().get(qvName);
		}catch(Exception e) {
			
		}
		
		return null;
  }
	
  private String makeZS(int A,int Z){
    if(Z<0 || A<=0)
        return "";
    String ZS=String.valueOf(Z);
    if(A<=0)
        return ZS;
    
    if(String.valueOf(A).length()>ZS.length())
        ZS="\\ "+ZS;
    
    return ZS;
  }
  
  public void setEN(String s){  

   /* To setup just an element name */

    //clearValues();
    s=s.trim();
	if(s.equals("n") || s.equals("p")) {
		EN=s;
	}else
		EN=s.toUpperCase();

    En=getEn();
    Z=findZ();
    elementName=findElementName();
  }

  /* -------------------------------------------------------------- */

  public void setGen(String s){ 

    /* To setup a generic nuclear name */

    clearValues();

    nucNameGen=s.trim().toUpperCase(); 

    if( nucNameGen.length() > 0 ){

      isNumber=true;
      for(int j=0; j<nucNameGen.length(); j++){
        if( ! Character.isDigit( nucNameGen.charAt(j) ) ) isNumber=false;
      }

      if( isNumber ){
        setZA( Double.parseDouble( nucNameGen) );
      }

      else{

        if( nucNameGen.equals("1NN") || nucNameGen.equals("1N") || nucNameGen.equals("N") || nucNameGen.equals("NEUTRON")){ 
          A=1; EN="NN"; En="n"; nucNameTUC="1NN"; Z=0; N=1; 
        }
        else if(nucNameGen.indexOf("NN")==1 && Character.isDigit(nucNameGen.charAt(0))){
          A=1; EN="NN"; En="n"; nucNameTUC=nucNameGen; Z=0; N=1;            
        }
        else if( nucNameGen.equals("P") || nucNameGen.equals("PROTON")){ 
          A=1; EN="H"; En="H"; nucNameTUC="1H"; Z=1; N=0; 
        }
        else if( nucNameGen.equals("D") || nucNameGen.equals("DEUTERON")){ 
          A=2; EN="H"; En="H"; nucNameTUC="2H"; Z=1; N=1; 
        }
        else if( nucNameGen.equals("T") || nucNameGen.equals("TRITON") ){ 
          A=3; EN="H"; En="H"; nucNameTUC="3H"; Z=1; N=2; 
        }
        else if( nucNameGen.equals("A") || nucNameGen.equals("ALPHA") || nucNameGen.equals("ALFA")){ 
          A=4; EN="HE"; En="He"; nucNameTUC="4He"; Z=2; N=2; 
        }
        else if( nucNameGen.equals("G") || nucNameGen.equals("GAMMA") ){ 
          A=0; EN="G"; En="G"; nucNameTUC="G"; Z=0; N=0; 
        }
        else{

          int il=nucNameGen.length();

          int im=nucNameGen.indexOf("-");

          if( im > 0 ){
             int im2=nucNameGen.lastIndexOf("-");
             if( im2 == im ){
              
               /* the 238-U case */
             
               if( Character.isDigit( nucNameGen.charAt(0) ) ){
                  try{
                    A=Integer.parseInt( nucNameGen.substring(0,im) );
                    EN=nucNameGen.substring(im+1);
                  }
                  catch(NumberFormatException e){}
               }
              
               /* the U-238 case */
             
               if( Character.isDigit( nucNameGen.charAt(il-1) ) ){
                  try{
                    A=Integer.parseInt( nucNameGen.substring(im+1) );
                    EN=nucNameGen.substring(0,im); 
                  }         
                  catch(NumberFormatException e){}
               }
             }
             else if( im2 > im ){
              
               /* the 92-U-238 case */
             
               try{
                 A=Integer.parseInt( nucNameGen.substring(im2+1).trim() );
                 EN=nucNameGen.substring(im+1,im2).trim();
               }
               catch(NumberFormatException e){}

             }
          }

          else if( im < 0 ){
             String sa="";

             if( Character.isDigit( nucNameGen.charAt(0) )  ){
                 int j=1;
                 sa=nucNameGen.substring(0,1);

                 while ( (j+1) <= il && Character.isDigit( nucNameGen.charAt(j) ) ){
                     sa=sa+nucNameGen.substring(j,j+1);
                     j++;
                 } 
                 if( sa.length() <= 3 ){
                   A=Integer.parseInt(sa); 
                   EN=nucNameGen.substring(j);
                 }
                 if( sa.length() == 5){
                   A= Integer.parseInt(sa.substring(0,3)); 
                   EN= sa.substring(3); 
                 }
             }

             else if( Character.isDigit( nucNameGen.charAt(il-1) )  ){
                 int j=il-1;
                 sa=nucNameGen.substring(il-1);

                 while ( j >= 1 && Character.isDigit( nucNameGen.charAt(j-1) )  ){
                     sa=nucNameGen.substring(j-1,j)+sa;
                     j--;
                 } 
                 if( sa.length() <= 3 ){
                   A=Integer.parseInt(sa); 
                   EN=nucNameGen.substring(0,j);          
                 }
                 if( sa.length() == 5){
                   A= Integer.parseInt(sa.substring(0,3)); 
                   EN= sa.substring(3); 
                 }
             }

          }  /* if im */

          nucNameTUC=String.valueOf(A)+EN;
          En=getEn();
          Z=findZ();
          N=A-Z;
          elementName=findElementName();

       } /* if neutron */

      } /* if number */
 
    }  /* if length > 0 */
    
    if(ZS.length()==0)
        ZS=makeZS(A,Z);

  }

  /* -------------------------------------------------------------- */


  /* To setup a nucleus with Z and A */

  public void setZA(int iz, int ia){  
    clearValues();
    Z=iz;
    A=ia;
    
    if(A>=0 && Z>=0 && A>=Z)
        N=A-Z;
    
    if(A<Z || (A==Z&&Z>1))
    	A=-1;
    
    if( 0 <= Z && Z <= 121){
        EN=findElementSymbolAlt();
        En=getEn();
        elementName=findElementName();
        nucNameEnsdf=findNUCID();
        if(A>=0 && N>=0)
            nucNameTUC=String.valueOf(A)+EN;
    }
    
    ZS=makeZS(A,Z);
  }

  /* To setup a nucleus with the ENDF ZA */

  public void setZA(double d){ 
    clearValues();
    if( d == 1 ){
      Z=0;
      A=1;
      N=1;
      EN=findElementSymbolAlt();
      elementName=findElementName();
      En=getEn();
      nucNameTUC=String.valueOf(A)+EN;
      nucNameEnsdf=findNUCID();
    }
    else if( d >= 1000 ){
      int iZ=(int) Math.floor( d/1000.);
      int iA=(int) Math.floor(d-iZ*1000); 
      int iN=iA-iZ;
      
      if(iA<iZ || (iA==iZ&&iZ>1)) 
      	A=-1;
      
      
      if( 0 <= iZ && iZ <= 121 && 0 <= iA && 0 <=iN ){      
        Z=iZ;
        A=iA;
        N=iN;
        EN=findElementSymbolAlt();
        En=getEn();
        nucNameTUC=String.valueOf(A)+EN;
        nucNameEnsdf=findNUCID();
        elementName=findElementName();
      }
      else{
        Z=-1; A=-1; N=-1; EN=""; En=""; nucNameTUC=""; nucNameEnsdf="";
      }
    }
    
    ZS=makeZS(A,Z);
  }
  
  public boolean isEvenEven(){return isEvenEven;}
  public boolean isValidFormat() {return isValidFormat;}
  
  public void setM(float m){mass=m;}
  public void setMS(String s){ms=s;}
  public void setMS(String s,String ds){ms=s;dms=ds;}
  public void setDMS(String ds){dms=ds;}
  
  public void setSN(float m){sn=m;}
  public void setSNS(String s){sns=s;}
  public void setSNS(String s,String ds){sns=s;dsns=ds;}
  public void setDSNS(String ds){dsns=ds;}
  
  public void setSP(float m){sp=m;}
  public void setSPS(String s){sps=s;}
  public void setSPS(String s,String ds){sps=s;dsps=ds;}
  public void setDSPS(String ds){dsps=ds;}
  
  public void setSA(float m){sa=m;}
  public void setSAS(String s){sas=s;}
  public void setSAS(String s,String ds){sas=s;dsas=ds;}
  public void setDSAS(String ds){dsas=ds;}
  
  public void setJPS(String s) {JPS=s;}
  
  public int Z(){ return Z;}
  public int A(){ return A;}
  public int N(){ return N;}
  public int z(){ return Z;}
  public int a(){ return A;}
  public int n(){ return N;}
  
  public String ZS(){return ZS;}
  public String zs(){return ZS;}
  
  public String JPS() {return JPS;}
 
  
  public float m(){if(mass<=0) return A;else return mass;}
  public float M(){if(mass<=0) return A;else return mass;}
  public String ms(){return ms;}
  public String dms(){return dms;}
  
  public float sn(){return sn;}
  public float SN(){return sn;}
  public String sns(){return sns;}
  public String dsns(){return dsns;}
  
  public float sp(){return sp;}
  public float SP(){return sp;}
  public String sps(){return sps;}
  public String dsps(){return dsps;}
  
  public float sa(){return sa;}
  public float SA(){return sa;}
  public String sas(){return sas;}
  public String dsas(){return dsas;}
  
  public String EN(){ return EN;}
  public String En(){ return En;}
  public String NucNameTrimUC(){  return nucNameTUC; }
  public String nameTUC(){  return nucNameTUC; }
  
  
  public String nameENSDF(){  return nucNameEnsdf;}
  
  private String findNUCID() {
         nucNameEnsdf="";
         if( A>= 0 &&A <= 9 ) 
             nucNameEnsdf=nucNameEnsdf+"  "+A;
         else if( A >= 10 && A <= 99 ) 
             nucNameEnsdf=nucNameEnsdf+" "+A;
         else if( A >= 100 ) 
             nucNameEnsdf=nucNameEnsdf+A;
         
         if( EN.length() == 1){
             if(EN.equalsIgnoreCase("N") && Z==0)
                 nucNameEnsdf=nucNameEnsdf+"n ";
             else
                 nucNameEnsdf=nucNameEnsdf+EN+" ";
         }
         else if( EN.length() == 2){
             if(EN.equalsIgnoreCase("NN"))
                 nucNameEnsdf=nucNameEnsdf+"n ";
             else
                 nucNameEnsdf=nucNameEnsdf+EN;
         }
         return nucNameEnsdf;     
  }

  /* -------------------------------------------------------------- */
 public String getElementName(){return elementName;}

 
 public static String getElementNameByZ(int Z){
     if(Z>=0 && Z<Elements.elementNameV.size())
         return (String) Elements.elementNameV.elementAt(Z);
     else if(Z>0)
         return Integer.toString(Z);
     else 
         return "";
 }
 

 public static String getElementSymbolByZ(int Z){
     if(Z>=0 && Z<Elements.elementSymbolV.size()){
         String symbol=Elements.elementSymbolV.elementAt(Z);
         if(symbol.equals("NN"))
             return "n";
         else
             return symbol;
     }
     else if(Z>0)
         return Integer.toString(Z);
     else 
         return "";
 }

 
 public String nameWC(){

   if( Z==0 && A==1){ return "Neutron";}
   else{ return (A+En); }

 }

 /*
  * normal name (print name), like 56Fe, A+En
  */
 public String nameNorm(){

	   if( Z==0 && A==1){
		   return "n";
	   }else if(Z==1) {
		   return A+"H";
	   }else if(Z==2) {
		   return A+"He";
	   }else{ 
		   return (A+En); 	   
	   }

 }
 
 public String evenZ(){
   String ez="";
   float fjunk=((float) Z)/2-Math.round( ((float) Z)/2);
   if( fjunk == 0 ) ez="e";
   else ez="o";
   return ez;
 }

 public String evenN(){
   String en="";
   float fjunk=((float) N)/2-Math.round( ((float) N)/2);
   if( fjunk == 0 ) en="e";
   else en="o";
   return en;
 }
 public String evenA(){
   String ea="";
   float fjunk=((float) A)/2-Math.round( ((float) A)/2);
   if( fjunk == 0 ) ea="e";
   else ea="o";
   return ea;
 }

 public String nameHTML(){
   if( Z == 0 && N == 0 ) nameHTML="&gamma;";
   else if( Z == 0 && N == 1 ) nameHTML="n";
   else{
      nameHTML="<sup>"+A+"</sup>"+En;
   }
   return nameHTML;
 }

 public int mat(){

   /* the mat used in endf */

   if( Z < 99 ){
      mat=Z*100;
      I=25+3*( A - ( (Integer) Elements.mat25V.elementAt(Z) ).intValue() );
      if( I > 0 && I < 100 ) mat=mat+I;      
   }
   else if( Z == 99 ){
      if( A == 253 ) mat=9913;
      else if( A == 254 ) mat=9914;
      else mat=9900;
   }
   else if( Z == 100 ){
      if( A == 254 ) mat=9935;
      else if( A == 255 ) mat=9936;
      else if( A == 256 ) mat=9937;
      else mat=9900;
   }

   else if( Z > 100 ) mat=9900;

   return mat;
 }

 public String zsymam(){

    zsymam=""+Z+"-";
    if( En.length() == 1 ) zsymam=zsymam+En+" -";
    else zsymam=zsymam+En+"-";
    if( A < 10 ) zsymam=zsymam+"  "+A;
    else if( A >= 10 && A < 100) zsymam=zsymam+" "+A;
    else if( A >= 100) zsymam=zsymam+A;
    return zsymam;

 }

/* some private methods: */

 private String getEn(){ 
  String s="";
  if( EN.length() == 1 ){
      if(EN.equals("N") && A<7)
          s="n";
      else
          s=EN;
  }
  else if( EN.length() == 2 ) {
      if(EN.equals("NN"))
          s="n";
      else if(Character.isDigit(EN.charAt(0))){
          try{
              int Z=findZ();
              if(Z>0)
                  s=Integer.toString(Z);
              else
                  s=EN;
          }catch(NumberFormatException e){
              s=EN;
          }
      }
      else
          s=EN.substring(0,1)+EN.substring(1).toLowerCase();
  }
  return s;
  }

 private int findZ(){
   int zVal=-1;

   if((EN.equals("N") || EN.equals("NN")) && A==1)
       return 0;
   
   for( int j=0; j< Elements.elementSymbolV.size(); j++){
     if( ( (String) Elements.elementSymbolV.elementAt(j) ).equals( EN ) ){ zVal=j; break; }
   }
   if( zVal < 0 ){
     try{
         int tempZ=Integer.parseInt(EN);
         if(tempZ>=10 && tempZ<30)
             zVal=100+tempZ;
     }catch(NumberFormatException e){
         //do nothing
         zVal=-1;
     }
     /*
     if( EN.equals("10") ) zVal=110;
     if( EN.equals("11") ) zVal=111;
     if( EN.equals("12") ) zVal=112;
     if( EN.equals("13") ) zVal=113;
     if( EN.equals("14") ) zVal=114;
     if( EN.equals("15") ) zVal=115;
     if( EN.equals("16") ) zVal=116;
     if( EN.equals("17") ) zVal=117;
     if( EN.equals("18") ) zVal=118;
     if( EN.equals("19") ) zVal=119;
     if( EN.equals("20") ) zVal=120;
     if( EN.equals("21") ) zVal=121;
     */
   }
   return zVal;
 }

 /* return upper-case element symbol. Exception: "n" for neutron but
  * "N" for nitrogen 
  */
 public String findElementSymbol(){//"n" for neutron and "N" for nitrogen
     if(Z>=0 && Z<Elements.elementSymbolV.size()){
         String symbol=Elements.elementSymbolV.elementAt(Z);
         if(symbol.equals("NN"))
             return "n";
         else
             return symbol;
     }
     else if(Z>0)
         return Integer.toString(Z);
     else 
         return "";
 }
 
 /* return upper-case element symbol with "NN" for neutron but
  * "N" for nitrogen 
  */
 private String findElementSymbolAlt(){//"NN" for neutron, "N" for nitrogen
     if(Z>=0 && Z<Elements.elementSymbolV.size())
         return (String) Elements.elementSymbolV.elementAt(Z);
     else if(Z>0)
         return Integer.toString(Z);
     else 
         return "";
 } 
 
 
 private String findElementName(){

     if(Z>=0 && Z<Elements.elementNameV.size())
         return (String) Elements.elementNameV.elementAt(Z);
     else if(Z>0)
         return Integer.toString(Z);
     else 
         return "";
 } 

 //daughter nucleus-parent nucleus
	public static int[] dAdZ(String qvName) {
		int[] out=new int[2];
		out[0]=0;
		out[1]=0;
		
		int dA=0,dZ=0;
		switch(qvName) {
		  case QBN_NAME:
			  dA=-1;dZ=1;
			  break;
		  case QB_NAME:
			  dA=0;dZ=1;
			  break;
		  case S2N_NAME:
			  dA=-2;dZ=0;
			  break;
		  case SN_NAME:
			  dA=-1;dZ=0;
			  break;
		  case SP_NAME:
			  dA=-1;dZ=-1;
			  break;
		  case QEC_NAME:
			  dA=0;dZ=-1;
			  break;
		  case QA_NAME:
			  dA=-4;dZ=-2;
			  break;
		  case S2P_NAME:
			  dA=-2;dZ=-2;
			  break;
		  case QEP_NAME:
			  dA=-1;dZ=-2;
			  break;
		  default:
			  break;
		}
		
		out[0]=dA;
		out[1]=dZ;
		return out;
	}
	
	public static String htmlQvName(String qvName) {
		String out=qvName;

		switch(qvName) {
		  case QBN_NAME://B-N
			  out="Q(&beta;<sup>-</sup>n)";
			  break;
		  case QB_NAME://B-
			  out="Q(&beta;<sup>-</sup>)";
			  break;
		  case S2N_NAME:
			  out="S(2n)";
			  break;
		  case SN_NAME:
			  out="S(n)";
			  break;
		  case SP_NAME:
			  out="S(p)";
			  break;
		  case QEC_NAME:
			  out="Q(&epsilon;)";
			  break;
		  case QA_NAME:
			  out="Q(&alpha;)";
			  break;
		  case S2P_NAME:
			  out="S(2p)";
			  break;
		  case QEP_NAME:
			  out="Q(&epsilon;p)";
			  break;
		  default:
			  break;
		}

		return out;
	}
	
	public static String nameNorm(String NUCID) {
		String s="";
		try {
			s=NUCID.toUpperCase().trim();
			if(s.length()>1) {
				if(Str.isLetter(s.charAt(s.length()-2))) {
					s=s.substring(0,s.length()-1)+s.toLowerCase().charAt(s.length()-1);
				}
			}
		}catch(Exception e) {
			
		}
		
		return s;
	}
	
	public static String[] A_En(String NUCID) {
		String[] out=new String[2];
		out[0]="";
		out[1]="";

		try {
			NUCID=NUCID.trim();
			if(NUCID.length()>5 || NUCID.isEmpty())
				return out;
			
			//29920: A=299, Z=120, no element name has been assigned yet
			if(Str.isInteger(NUCID)) {
				if(NUCID.length()==5) {
					out[0]=NUCID.substring(0,3);
					out[1]=NUCID.substring(3);
					return out;
				}else if(NUCID.length()<=3) {
					out[0]=NUCID;
				}
				
				return out;
			}else if(Str.isLetters(NUCID)&&NUCID.length()<=2) {
				out[1]=NUCID;
				return out;
			}

			
			int i=0;
			while(Character.isDigit(NUCID.charAt(i))) {
				out[0]+=NUCID.charAt(i);
				i++;
			}
			out[1]=NUCID.substring(i);
			String s=out[1];
			if(s.length()>0) {
				if(Str.isLetters(s)) {
					if(s.length()>1) {
						out[1]=s.toUpperCase().substring(0,s.length()-1)+s.toLowerCase().charAt(s.length()-1);
					}
				}else {
					out[0]="";
					out[1]="";
				}
			}


				
		}catch(Exception e) {
			
		}
		
		return out;
	}
	
	//find a list of all possible nuclides with mass number A
	//Note it is not necessary every nuclide in the list exists
	/*
	 * output map: <NUCID,Z>
	 */
	public static LinkedHashMap<String,Integer> findAllNuclidesZMapByA(int A){
		LinkedHashMap<String,Integer> out=new LinkedHashMap<String,Integer>();
		try {
			if(A<=0)
				return out;
            int middleA=A/2;
            
            //from a survey of all istopes in AME
            //max distance of Z down from A/2 is 32, that is, max(A/2-Z)=32, for Z<A/2
            //max distance of Z up from A/2 is 4, that is, max(Z-A/2)=4, for Z>A/2
            //ratio of Z to half A: min=0.286
            
            int minZ=-1,maxZ=-1;
            minZ=Math.max(middleA-32, 0);
            maxZ=middleA+4;
            
            if(A<=10) {minZ=(int)(0.286*A/2); maxZ=(int)(2.000*A/2)+1;}
            else if(A<=20) {minZ=(int)(0.462*A/2); maxZ=(int)(1.455*A/2)+1;}
            else if(A<=30) {minZ=(int)(0.476*A/2); maxZ=(int)(1.273*A/2)+1;}
            else if(A<=40) {minZ=(int)(0.564*A/2); maxZ=(int)(1.226*A/2)+1;}
            else if(A<=50) {minZ=(int)(0.585*A/2); maxZ=(int)(1.171*A/2)+1;}
            else if(A<=60) {minZ=(int)(0.644*A/2); maxZ=(int)(1.115*A/2)+1;}
            else if(A<=70) {minZ=(int)(0.656*A/2); maxZ=(int)(1.082*A/2)+1;}
            else if(A<=80) {minZ=(int)(0.684*A/2); maxZ=(int)(1.042*A/2)+1;}
            else if(A<=90) {minZ=(int)(0.683*A/2); maxZ=(int)(1.037*A/2)+1;}
            else if(A<=100) {minZ=(int)(0.714*A/2); maxZ=(int)(1.022*A/2)+1;}
            else if(A<=110) {minZ=(int)(0.710*A/2); maxZ=(int)(1.000*A/2)+1;}
            else if(A<=120) {minZ=(int)(0.706*A/2); maxZ=(int)(0.991*A/2)+1;}
            else if(A<=130) {minZ=(int)(0.703*A/2); maxZ=(int)(0.975*A/2)+1;}
            else if(A<=140) {minZ=(int)(0.702*A/2); maxZ=(int)(0.963*A/2)+1;}
            else if(A<=150) {minZ=(int)(0.717*A/2); maxZ=(int)(0.958*A/2)+1;}
            else if(A<=160) {minZ=(int)(0.724*A/2); maxZ=(int)(0.943*A/2)+1;}
            else if(A<=170) {minZ=(int)(0.733*A/2); maxZ=(int)(0.945*A/2)+1;}
            else if(A<=180) {minZ=(int)(0.744*A/2); maxZ=(int)(0.936*A/2)+1;}
            else if(A<=190) {minZ=(int)(0.755*A/2); maxZ=(int)(0.906*A/2)+1;}
            else if(A<=200) {minZ=(int)(0.751*A/2); maxZ=(int)(0.891*A/2)+1;}
            else if(A<=210) {minZ=(int)(0.749*A/2); maxZ=(int)(0.876*A/2)+1;}
            else if(A<=220) {minZ=(int)(0.741*A/2); maxZ=(int)(0.863*A/2)+1;}
            else if(A<=230) {minZ=(int)(0.740*A/2); maxZ=(int)(0.852*A/2)+1;}
            else if(A<=240) {minZ=(int)(0.745*A/2); maxZ=(int)(0.833*A/2)+1;}
            else if(A<=250) {minZ=(int)(0.755*A/2); maxZ=(int)(0.830*A/2)+1;}
            else if(A<=260) {minZ=(int)(0.762*A/2); maxZ=(int)(0.824*A/2)+1;}
            else if(A<=270) {minZ=(int)(0.771*A/2); maxZ=(int)(0.824*A/2)+1;}
            else if(A<=280) {minZ=(int)(0.770*A/2); maxZ=(int)(0.816*A/2)+1;}
            else {minZ=(int)(0.773*A/2); maxZ=(int)(0.804*A/2)+1;}
  
            minZ-=1;//offset for possible future new discoveries
            maxZ+=1;
            if(minZ<0)
            	minZ=0;
            
            
			for(int Z=minZ;Z<=maxZ;Z++) {
				//int defaultA=Elements.getDefaultAForZ(Z);
				//if(defaultA<=0)
				//	continue;
				
				int N=A-Z;
				if(N<0)
					continue;
				
				String EN=Nucleus.getElementSymbolByZ(Z);//return n not NN for neutron
				if(EN==null || EN.isEmpty() || Str.isInteger(EN))
					continue;
				
				if(EN.equals("n")&&Z==0) {
					out.put(A+"NN",Z);
				}else
					out.put(A+EN,Z);
			}
		}catch(Exception e) {
			
		}
		
		return out;
	}
	
	public static Vector<String> findAllNuclidesByA(int A){
		Vector<String> out=new Vector<String>();
		out.addAll(findAllNuclidesZMapByA(A).keySet());
		return out;
	}
	/*
	 * output map: <NUCID,Z>
	 */
	public static LinkedHashMap<String,Integer> findAllNuclidesZMapByZ(int Z){
		LinkedHashMap<String,Integer> out=new LinkedHashMap<String,Integer>();
		
		try {
            int minA=-1,maxA=-1;
            
			if(Z==0) {minA=1; maxA=1;}
			else if(Z==1) {minA=1; maxA=7;}
			else if(Z<=5) {minA=(int)(0.200*Z)+Z; maxA=(int)(4.000*Z)+Z+1;}
			else if(Z<=10) {minA=(int)(0.333*Z)+Z; maxA=(int)(2.833*Z)+Z+1;}
			else if(Z<=15) {minA=(int)(0.545*Z)+Z; maxA=(int)(2.545*Z)+Z+1;}
			else if(Z<=20) {minA=(int)(0.611*Z)+Z; maxA=(int)(2.105*Z)+Z+1;}
			else if(Z<=25) {minA=(int)(0.667*Z)+Z; maxA=(int)(2.000*Z)+Z+1;}
			else if(Z<=30) {minA=(int)(0.714*Z)+Z; maxA=(int)(1.929*Z)+Z+1;}
			else if(Z<=35) {minA=(int)(0.806*Z)+Z; maxA=(int)(1.839*Z)+Z+1;}
			else if(Z<=40) {minA=(int)(0.861*Z)+Z; maxA=(int)(1.825*Z)+Z+1;}
			else if(Z<=45) {minA=(int)(0.927*Z)+Z; maxA=(int)(1.844*Z)+Z+1;}
			else if(Z<=50) {minA=(int)(0.957*Z)+Z; maxA=(int)(1.848*Z)+Z+1;}
			else if(Z<=55) {minA=(int)(1.000*Z)+Z; maxA=(int)(1.788*Z)+Z+1;}
			else if(Z<=60) {minA=(int)(1.018*Z)+Z; maxA=(int)(1.754*Z)+Z+1;}
			else if(Z<=65) {minA=(int)(1.063*Z)+Z; maxA=(int)(1.710*Z)+Z+1;}
			else if(Z<=70) {minA=(int)(1.087*Z)+Z; maxA=(int)(1.667*Z)+Z+1;}
			else if(Z<=75) {minA=(int)(1.113*Z)+Z; maxA=(int)(1.662*Z)+Z+1;}
			else if(Z<=80) {minA=(int)(1.115*Z)+Z; maxA=(int)(1.700*Z)+Z+1;}
			else if(Z<=85) {minA=(int)(1.171*Z)+Z; maxA=(int)(1.702*Z)+Z+1;}
			else if(Z<=90) {minA=(int)(1.244*Z)+Z; maxA=(int)(1.686*Z)+Z+1;}
			else if(Z<=95) {minA=(int)(1.319*Z)+Z; maxA=(int)(1.648*Z)+Z+1;}
			else if(Z<=100) {minA=(int)(1.402*Z)+Z; maxA=(int)(1.625*Z)+Z+1;}
			else if(Z<=105) {minA=(int)(1.416*Z)+Z; maxA=(int)(1.594*Z)+Z+1;}
			else if(Z<=110) {minA=(int)(1.427*Z)+Z; maxA=(int)(1.598*Z)+Z+1;}
			else if(Z<=115) {minA=(int)(1.450*Z)+Z; maxA=(int)(1.577*Z)+Z+1;}
			else {minA=(int)(1.483*Z+Z); maxA=(int)(1.526*Z)+Z+1;}
	
			
            minA-=1;//offset for possible future new discoveries
            maxA+=1;
            if(minA<0)
            	minA=0;
            
            
			for(int A=minA;A<=maxA;A++) {
				//int defaultA=Elements.getDefaultAForZ(Z);
				//if(defaultA<=0)
				//	continue;
				
				int N=A-Z;
				if(N<0)
					continue;
				
				String EN=Nucleus.getElementSymbolByZ(Z);//return n not NN for neutron
				if(EN==null || EN.isEmpty() || Str.isInteger(EN))
					continue;
				
				if(EN.equals("n")&&Z==0) {
					out.put(A+"NN",Z);
				}else
					out.put(A+EN,Z);
			}
		}catch(Exception e) {
			
		}
		return out;
	}
	
	public static Vector<String> findAllNuclidesByZ(int Z){
		Vector<String> out=new Vector<String>();
		out.addAll(findAllNuclidesZMapByZ(Z).keySet());
		return out;
	}
	
	/*
	 * output map: <NUCID,Z>
	 */
	public static LinkedHashMap<String,Integer> findAllNuclidesZMapByN(int N){
		LinkedHashMap<String,Integer> out=new LinkedHashMap<String,Integer>();
		
		try {
            int minA=-1,maxA=-1;
            
			if(N==0) {minA=1; maxA=3;}
			else if(N==1) {minA=1; maxA=6;}
			else if(N==2) {minA=3; maxA=8;}
			else if(N<=4) {minA=(int)(1.250*N); maxA=(int)(3.667*N)+1;}
			else if(N<=6) {minA=(int)(1.167*N); maxA=(int)(3.000*N)+1;}
			else if(N<=8) {minA=(int)(1.250*N); maxA=(int)(2.750*N)+1;}
			else if(N<=10) {minA=(int)(1.300*N); maxA=(int)(2.667*N)+1;}
			else if(N<=16) {minA=(int)(1.313*N); maxA=(int)(2.636*N)+1;}
			else if(N<=24) {minA=(int)(1.353*N); maxA=(int)(2.412*N)+1;}
			else if(N<=32) {minA=(int)(1.393*N); maxA=(int)(2.240*N)+1;}
			else if(N<=40) {minA=(int)(1.475*N); maxA=(int)(2.091*N)+1;}
			else if(N<=48) {minA=(int)(1.488*N); maxA=(int)(2.073*N)+1;}
			else if(N<=56) {minA=(int)(1.519*N); maxA=(int)(2.020*N)+1;}
			else if(N<=64) {minA=(int)(1.544*N); maxA=(int)(1.982*N)+1;}
			else if(N<=72) {minA=(int)(1.551*N); maxA=(int)(1.940*N)+1;}
			else if(N<=80) {minA=(int)(1.544*N); maxA=(int)(1.920*N)+1;}
			else if(N<=88) {minA=(int)(1.541*N); maxA=(int)(1.897*N)+1;}
			else if(N<=96) {minA=(int)(1.556*N); maxA=(int)(1.889*N)+1;}
			else if(N<=104) {minA=(int)(1.567*N); maxA=(int)(1.845*N)+1;}
			else if(N<=112) {minA=(int)(1.585*N); maxA=(int)(1.804*N)+1;}
			else if(N<=120) {minA=(int)(1.607*N); maxA=(int)(1.779*N)+1;}
			else if(N<=128) {minA=(int)(1.598*N); maxA=(int)(1.752*N)+1;}
			else if(N<=136) {minA=(int)(1.588*N); maxA=(int)(1.736*N)+1;}
			else if(N<=144) {minA=(int)(1.587*N); maxA=(int)(1.709*N)+1;}
			else if(N<=152) {minA=(int)(1.593*N); maxA=(int)(1.700*N)+1;}
			else if(N<=160) {minA=(int)(1.614*N); maxA=(int)(1.701*N)+1;}
			else if(N<=168) {minA=(int)(1.627*N); maxA=(int)(1.689*N)+1;}
			else if(N<=176) {minA=(int)(1.626*N); maxA=(int)(1.674*N)+1;}
			else {minA=(int)(1.638*N); maxA=(int)(1.667*N)+1;}
	
			
            minA-=1;//offset for possible future new discoveries
            maxA+=1;
            if(minA<0)
            	minA=0;
            
            
			for(int A=minA;A<=maxA;A++) {
				//int defaultA=Elements.getDefaultAForZ(Z);
				//if(defaultA<=0)
				//	continue;
				
				int Z=A-N;
				if(Z<0)
					continue;
				
				String EN=Nucleus.getElementSymbolByZ(Z);//return n not NN for neutron
				if(EN==null || EN.isEmpty() || Str.isInteger(EN))
					continue;
				
				//if(N==2)
				//	System.out.println(A+"  #"+EN);
				if(EN.equals("n")&&Z==0) {
					out.put(A+"NN",Z);
				}else
					out.put(A+EN,Z);
			}
		}catch(Exception e) {
			
		}
		return out;
	}
	
	public static Vector<String> findAllNuclidesByN(int N){
		Vector<String> out=new Vector<String>();
		out.addAll(findAllNuclidesZMapByN(N).keySet());
		return out;
	}
}