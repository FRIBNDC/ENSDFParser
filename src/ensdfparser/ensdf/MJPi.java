 package ensdfparser.ensdf;

public class MJPi{

  int njmax=100;
  int njval=0;
  float jval[]=new float[njmax];
  String pival[]=new String[njmax];
  int piival[]=new int[njmax];

  public MJPi(){
  }

  @SuppressWarnings("unused")
public void setValues(String st){ 

   String s=st.trim();

   if( s.indexOf("OR") >= 0 ){
     int io=s.indexOf("OR");
     s=s.substring(0,io).trim()+","+s.substring(io+2).trim();
   }
   if( s.indexOf("AND") > 0 ){
     int ia=s.indexOf("AND");
     s=s.substring(0,ia).trim()+","+s.substring(ia+3).trim();
   }
   if( s.indexOf("&") >= 0 ){
     int ia=s.indexOf("&");
     s=s.substring(0,ia).trim()+","+s.substring(ia+1).trim();
   }

   if( s.indexOf("AP") >= 0){
      try{
      String j=s.substring( s.indexOf("AP") +2 ).trim();
      njval=0;
      SingleJPi jp=new SingleJPi();
      jp.setValues(j);
      if( jp.canGetJ() ){
          njval++;
          jval[njval]=jp.j();
          pival[njval]=jp.pi();
          piival[njval]=jp.piI();
      }
      }catch(Exception e){ njval=0;}
   }

   else if( s.indexOf("NOT")  >= 0){
   }
   else if( s.indexOf("GE")  >= 0){
      try{
        String j=s.substring( s.indexOf("GE")+2).trim();
        j=j.replace('(',' ').replace(')',' ').trim();
        SingleJPi jp=new SingleJPi();
        jp.setValues(j);
        if( jp.canGetJ() ){
          njval=0;
          for( int k=0; k<=10; k++){
            njval++;
            jval[njval]=jp.j()+k;
            pival[njval]=jp.pi();
            piival[njval]=jp.piI();
          }
        }        
      }catch(Exception e){njval=0;}
   }
   else if( s.indexOf("LE")  >= 0){
      try{
        String j=s.substring( s.indexOf("LE")+2).trim();
        j=j.replace('(',' ').replace(')',' ').trim();
        SingleJPi jp=new SingleJPi();
        jp.setValues(j);
        if( jp.canGetJ() ){
          njval=0;
          for( int k=0; k<=10; k++){
            if( jp.j()-k >= 0 ){
              njval++;
              jval[njval]=jp.j()-k;
              pival[njval]=jp.pi();
              piival[njval]=jp.piI();
            }
          }
        }        
      }catch(Exception e){njval=0;}
   }
   else if( s.indexOf("NATURAL") >= 0 ){
   }
/*---------------------------------------------------------------------------------
    The TO or : cases:                                                           */

   else if( s.indexOf("TO") >= 0 || s.indexOf(":") >= 0 ){
      try{
      String ji="";String jf="";
      if( s.indexOf("TO") > 0 ){
          ji=s.substring(0,s.indexOf("TO")).trim();
          jf=s.substring(s.indexOf("TO")+2).trim();
      }
      else if( s.indexOf(":") > 0 ){
          ji=s.substring(0,s.indexOf(":")).trim();
          jf=s.substring(s.indexOf(":")+1).trim();
      }

      int ipari=ji.indexOf("(");
      int ipli=ji.indexOf("+");
      int imii=ji.indexOf("-");

      int iparf=jf.indexOf("(");
      int iplf=jf.indexOf("+");
      int imif=jf.indexOf("-");

      String par="";
      String pari="";
      String parf="";
      if( ipli <0 && imii <0 && iplf <0 && imif <0 ) par="";
      if( ipli <0 && imii <0 && ( iplf > 0 || imif > 0 ) ){ if( iplf > 0) par="+"; if( imif > 0) par="-"; pari=par;parf=par;}
      if( (ipli >0 || imii >0) && iplf <0 && imif <0 ){ 
            if( ipli > 0 ) pari="+"; if( ipli < 0 ) pari="-"; 
      }
      if( (ipli >0 || imii >0) && ( iplf > 0 || imif > 0 ) ){ 
            if( ipli > 0 ) pari="+"; if( ipli < 0 ) pari="-"; 
            if( iplf > 0 ) parf="+"; if( iplf < 0 ) parf="-"; 
      }
      if( jf.endsWith(")(+)") ){ pari="+"; parf="+"; par="+";}
      if( jf.endsWith(")(-)") ){ pari="-"; parf="-"; par="-";}

      ji=ji.replace('(',' ').replace(')',' ').replace('+',' ').replace('-',' ').trim();
      jf=jf.replace(')',' ').replace('(',' ').replace('+',' ').replace('-',' ').trim();

      SingleJPi jpi=new SingleJPi();
      jpi.setValues(ji);

      SingleJPi jpf=new SingleJPi();
      jpf.setValues(jf);

      if( jpi.canGetJ() && jpf.canGetJ() ){

          njval=0;
          int nj=(int) (jpf.j()-jpi.j())+1;
          for( int k=1; k<= nj; k++){
            njval++;
            jval[njval]=jpi.j()+(k-1);
            if( k == 1 ) pival[njval]=pari;
            else if( k == nj ) pival[njval]=parf;
            else pival[njval]=par;
          }

      }
      }catch(Exception e){njval=0;}
          
   }

/* If there are no weird characters: */

   else{

/* Counting how many times there is a comma: */

      int posc[]=new int[100];
      int nc=0;
      int ilc=0;
      for( int k=0; k< s.length(); k++ ){
         if( s.charAt(k) == ','){ 
             nc++; 
             posc[nc]=k; 
         }
      }

/*---------------------------------------------------------------------------------
    If there are no commas, there is only one jpi value:                          */

      if( nc == 0 ){
         try{
         SingleJPi jp=new SingleJPi();
         jp.setValues(s);
         if( jp.canGetJ() || jp.canGetPi() ){
             njval=1;
             jval[1]=jp.j();
             pival[1]=jp.pi();
         }
         }catch(Exception e){ njval=0;}

      }

/* If there is 1 or more commas: */

      else if (nc > 0 ){ 

/* Looking into the first value */

         String ji=s.substring(0,posc[1]); 

/*---------------------------------------------------------------------------------
   If there is no parenthesis, or if it is balanced, ex: 2+,3- or (2),(3)+        */

         if( ( ji.indexOf("(") < 0 ) || ( ji.indexOf("(") >= 0 && ji.indexOf(")") > 0) ){
            njval=0;
            try{
            for( int k=1; k<= nc+1; k++){

              String ss="";
              if( k == 1) ss=s.substring(0,posc[1]);
              if( k > 1 && k < (nc+1) ) ss=s.substring(posc[k-1]+1,posc[k]);
              if( k == (nc+1)) ss=s.substring(posc[nc]+1);
    
              SingleJPi jp=new SingleJPi();
              jp.setValues(ss);
              if( jp.canGetJ() || jp.canGetPi() ){
                njval++;
                jval[k]=jp.j();
                pival[k]=jp.pi();
              }
            }
            }catch(Exception e){njval=0;}
         }

/*---------------------------------------------------------------------------------
   If first parenthesis is not balanced, ex: (2+,3-) or (2,3)+                     */

         else if(  ji.indexOf("(") >= 0 && ji.indexOf(")") < 0 ){

            njval=0;
            try{
            for( int k=1; k<= nc+1; k++){

              String ss="";
              String par="";
              if( s.endsWith("+") ) par="+";
              if( s.endsWith("-") ) par="-";

              if( k == 1) ss=s.substring(0,posc[1]).replace('(',' ').trim();
              if( k > 1 && k < (nc+1) ) ss=s.substring(posc[k-1]+1,posc[k]);
              if( k == (nc+1)){
                 
                 if( s.endsWith("+") ) ss=s.substring(posc[nc]+1,(s.length()-1)); 
                 else if( s.endsWith("-") ) ss=s.substring(posc[nc]+1,(s.length()-1));
                 else{ ss=s.substring(posc[nc]+1); }
                 ss=ss.replace(')',' ').trim();

              }

             SingleJPi jp=new SingleJPi();
              jp.setValues(ss);
              if( jp.canGetJ() || jp.canGetPi() ){
                njval++;
                jval[k]=jp.j();
                if( par.length() == 0 )  pival[k]=jp.pi();
                if( par.length() > 0 )  pival[k]=par;
              }

            }
            }catch(Exception e){}
         }

      } /* if more than 1 value */


   } /* If not special cases */

   for( int k=1; k<= njval; k++){
      if( pival[k].equals("+") ) piival[k]=1;
      else if( pival[k].equals("-") ) piival[k]=-1;
      else if( pival[k].trim().length() == 0 )piival[k]=0;
   }

}

  public int nJ(){ return njval;}
  public float j(int k){ return jval[k]; }
  public String pi(int k){ return pival[k];}
  public int piI(int k){ return piival[k];}

}