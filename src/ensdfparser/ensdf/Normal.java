 package ensdfparser.ensdf;

import java.util.*;

import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

public class Normal{

  protected int id=-1;
    
  protected String nr_s;
  protected String dnr_s;
  protected String nt_s;
  protected String dnt_s;
  protected String br_s;
  protected String dbr_s;
  protected String nb_s;
  protected String dnb_s;
  protected String np_s;
  protected String dnp_s;

  protected double nr_d;
  protected double dnr_d;
  protected double nt_d;
  protected double dnt_d;
  protected double br_d;
  protected double dbr_d;
  protected double nb_d;
  protected double dnb_d;
  @SuppressWarnings("unused")

  protected double nbuv_d;
  @SuppressWarnings("unused")

  protected double nblv_d;
  protected double np_d;
  protected double dnp_d;

  protected String nrbr_s;
  protected String dnrbr_s;
  protected double nrbr_d;
  protected double dnrbr_d;

  protected String ntbr_s;
  protected String dntbr_s;
  protected double ntbr_d;
  protected double dntbr_d;

  protected String nbbr_s;
  protected String dnbbr_s;
  protected double nbbr_d;
  protected double dnbbr_d;

  protected String pncom_s;
  protected String pnopt_s;
  protected int implicit_opt;

  protected double gren_d;
  protected double dgren_d;
  protected String gren_s;
  protected String dgren_s;

  protected double tiren_d;
  protected double dtiren_d;

  protected double bmren_d;
  protected double dbmren_d;

  protected double ecbpren_d;
  protected double decbpren_d;

  protected Vector<String> lines=new Vector<String>();
  protected String recordLine="";
  protected String recordLinePN="";
  
  protected Vector<String> comNTextV=new Vector<String>();
  protected Vector<String> comPNTextV=new Vector<String>();
  
  //note that comments for norm have already been parsed in the top and footnote comments for an ENSDF object
  protected Vector<Comment> commentNV=new Vector<Comment>();
  protected Vector<Comment> commentPNV=new Vector<Comment>();
  
  protected boolean exists;

  protected boolean normExists;
  protected boolean pNormExists;

  protected String tiren_s;
  protected String dtiren_s;
  protected String bmren_s;
  protected String ecbpren_s;
  protected String decbpren_s;
  protected String dbmren_s;

  protected boolean isAssumedNB=false;

  protected String indexS="";//used to identify Norm where there are multiple Norm records 
                       //it is the number following "N" or "PN" or "P" records


  
  protected boolean isGRenUnity=false;//NR*BR
  protected boolean isTIRenUnity=false;//NT*BR
  protected boolean isBMRenUnity=false;//NB*BR
  protected boolean isECBPRenUnity=false;//NB*BR
  protected boolean isNRUnity=false;
  protected boolean isNTUnity=false;
  protected boolean isNBUnity=false;
  protected boolean isBRUnity=false;
  protected boolean isNPUnity=false;

  protected Vector<String> crLineV=new Vector<String>();
  protected Vector<ContRecord> crV=new Vector<ContRecord>();
  
  protected HashMap<String,Vector<ContRecord>> crMap=new HashMap<String,Vector<ContRecord>>();
  
  public Normal(){

    lines.clear();
    comNTextV.clear();
    comPNTextV.clear();
    commentNV.clear();
    commentPNV.clear();
    crV.clear();
    crMap.clear();
    crLineV.clear();
    
    nr_s="";
    dnr_s="";
    nt_s="";
    dnt_s="";
    br_s="";
    dbr_s="";
    nb_s="";
    dnb_s="";
    np_s="";
    dnp_s="";

    nr_d=-1.0;
    dnr_d=-1.0;

    nt_d=-1.0;
    dnt_d=-1.0;

    br_d=-1.0;
    dbr_d=-1.0;

    nb_d=-1.0;
    dnb_d=-1.0;

    np_d=-1.0;
    dnp_d=-1.0;

    nrbr_s="";
    dnrbr_s="";
    nrbr_d=-1.0;
    dnrbr_d=-1.0;

    ntbr_s="";
    dntbr_s="";
    ntbr_d=-1.0;
    dntbr_d=-1.0;

    nbbr_s="";
    dnbbr_s="";
    nbbr_d=-1.0;
    dnbbr_d=-1.0;

    pncom_s="";
    pnopt_s="";
    implicit_opt=-1;

    gren_s="";
    dgren_s="";
    gren_d=-1.;
    dgren_d=-1.0;

    tiren_s="";
    dtiren_s="";
    tiren_d=-1.;
    dtiren_d=-1.0;
    
    bmren_s="";
    dbmren_s="";
    bmren_d=-1.;
    dbmren_d=-1.0;

    ecbpren_s="";
    decbpren_s="";
    ecbpren_d=-1.;
    decbpren_d=-1.0;

    recordLine="";
    
    exists=false;
    normExists=false;
    pNormExists=false;
    
    isAssumedNB=false;
    
    isGRenUnity=false;
    isTIRenUnity=false;
    isBMRenUnity=false;
    isECBPRenUnity=false;
    isNRUnity=false;
    isNTUnity=false;
    isNBUnity=false;
    isBRUnity=false;
    isNPUnity=false;
  }

  protected boolean isUnity(String s,String ds) {
	  return EnsdfUtil.isUnity(s, ds);
  }
  
  public void setValues(Vector<String> v) throws Exception{
	  lines.clear();
   lines.addAll(v);
   if(lines.size() > 0 ) doWork();
  }

  protected void parseNR() {
      SDS2XDX s1=new SDS2XDX(); s1.setValues(nr_s,dnr_s);
      if( s1.canGetX() ) nr_d=s1.X();
      if( s1.canGetDX() ) dnr_d=s1.DX();

      isNRUnity=isUnity(nr_s,dnr_s);      
  }
  protected void parseNT() {
      SDS2XDX s2=new SDS2XDX(); s2.setValues(nt_s,dnt_s);
      if( s2.canGetX() ) nt_d=s2.X();
      if( s2.canGetDX() ) dnt_d=s2.DX();

      isNTUnity=isUnity(nt_s,dnt_s);      
  }
  protected void parseBR() {
      /* if( br_s.equals("0.9995") ){ br_s="1.0";  dbr_s="";} */
      SDS2XDX s3=new SDS2XDX(); s3.setValues(br_s,dbr_s);
      if( s3.canGetX() ) br_d=s3.X();
      if( s3.canGetDX() ) dbr_d=s3.DX();

      isBRUnity=isUnity(br_s,dbr_s);
      
      //System.out.println("N: "+ br_d+" "+dbr_d );      
  }
  protected void parseNB() {
      
     /*  nb_s="1.0"; dnb_s="";   */

      if(nb_s.length()>0){
          SDS2XDX s4=new SDS2XDX(); s4.setValues(nb_s,dnb_s);
          if( s4.canGetXS() && s4.canGetDXS() ){
            nb_d=s4.XS();
            dnb_d=s4.DXS();
            if( s4.canGetXUV() ) nbuv_d=s4.XUV();
            if( s4.canGetXLV() )nblv_d=s4.XLV();
          }
          else{     
//    System.out.println("Problems!, couldn't symmetrize nb for nb= "+nb_s+" and "+dnb_s);
            if( s4.canGetX() ) nb_d=s4.X();
            if( s4.canGetXUV() ) nbuv_d=s4.XUV();
            if( s4.canGetXLV() ) nblv_d=s4.XLV();
          }
      }else{//if empyt, =1 by default. Note that this only applies to NB record. For other records like NR, BR, values must be given even if =1.0
          nb_s="1.0"; dnb_s="";
          nb_d=1.0;dnb_d=0;
          nbuv_d=1.0;nblv_d=1.0;
          
          isAssumedNB=true;
      }

      isNBUnity=isUnity(nb_s,dnb_s);      
  }
  protected void parseNP() {
      SDS2XDX s5=new SDS2XDX(); s5.setValues(np_s,dnp_s);
      if( s5.canGetX() ) np_d=s5.X();
      if( s5.canGetDX() ) dnp_d=s5.DX();

      //System.out.println("nr="+nr_s+" nt="+nt_s+" br="+br_s+" nb="+nb_s);
      isNPUnity=isUnity(np_s,dnp_s);      
  }
  protected void parseNRBR() {
      SDS2XDX s1=new SDS2XDX(); s1.setValues(nrbr_s,dnrbr_s);
      if( s1.canGetX() ) nrbr_d=s1.X();
      if( s1.canGetDX() ) dnrbr_d=s1.DX();      
  }
  protected void parseNTBR() {    
      SDS2XDX s2=new SDS2XDX(); s2.setValues(ntbr_s,dntbr_s);
      if( s2.canGetX() ) ntbr_d=s2.X();
      if( s2.canGetDX() ) dntbr_d=s2.DX();      
  }
  protected void parseNBBR() {   
      SDS2XDX s3=new SDS2XDX(); s3.setValues(nbbr_s,dnbbr_s);
      if( s3.canGetX() ) nbbr_d=s3.XS();
      if( s3.canGetDX() ) dnbbr_d=s3.DXS();      
  }
  
  protected void parseRenorm() {
      /* The renormalizations: */
      //NOTE: if PN record exists, it should be used to override N record
      
      /*old
      if( nr_d > 0 && br_d > 0 ){
         AbsInt rn=new AbsInt(); 
         rn.setValues(nr_d , dnr_d , br_d , dbr_d );  
         gren_d=rn.AId(); dgren_d=rn.DAId();
      }
      else if( nrbr_d > 0 &&  ( br_d <= 0 || nr_d <= 0 ) ){
         gren_d=nrbr_d; 
         if( dnrbr_d >= 0 ) dgren_d=dnrbr_d;
      }    
      */
      
	  if(isUnity(nr_s,dnr_s)){
		  dnr_s="";
		  dnr_d=0;
		  isNRUnity=true;
	  }
	  if(isUnity(br_s,dbr_s)){
		  dbr_s="";
		  dbr_d=0;
		  isBRUnity=true;
	  }
	  if(isUnity(nb_s,dnb_s)){
		  dnb_s="";
		  dnb_d=0;
		  isNBUnity=true;
	  }
	  if(isUnity(nt_s,dnt_s)){
		  dnt_s="";
		  dnt_d=0;
		  isNTUnity=true;
	  }
	  if(isUnity(np_s,dnp_s)){
		  dnp_s="";
		  dnp_d=0;
		  isNPUnity=true;
	  }
	  if(isUnity(nrbr_s,dnrbr_s)){
		  dnrbr_s="";
		  dnrbr_d=0;
	  }
	  if(isUnity(ntbr_s,dntbr_s)){
		  dntbr_s="";
		  dntbr_d=0;
	  }
	  if(isUnity(nbbr_s,dnbbr_s)){
		  dnbbr_s="";
		  dnbbr_d=0;
	  }
	  
      /*new*/
      if(nrbr_d > 0){
          gren_d=nrbr_d;
          if( dnrbr_d >= 0 ) 
              dgren_d=dnrbr_d;
          gren_s=nrbr_s;
          dgren_s=dnrbr_s;
      }else if(nr_d > 0 && br_d > 0){//Ig normalization factor NR>0, branching ratio BR>0, in this case NT=NR   
          AbsInt rn=new AbsInt(); 
          rn.setValues(nr_d , dnr_d , br_d , dbr_d );  
          gren_d=rn.AId(); dgren_d=rn.DAId();   
          
          //System.out.println(nr_d+" "+dnr_d+" "+br_d+" "+dbr_d);
          
          SDS2XDX NR=new SDS2XDX();NR.setValues(nr_s, dnr_s);
          SDS2XDX BR=new SDS2XDX();BR.setValues(br_s, dbr_s);
          SDS2XDX GREN;
          if(isBRUnity) 
              GREN=NR;
          else if(isNRUnity) 
              GREN=BR;
          else { 
              GREN=NR.multiply(BR);
              float x=Float.parseFloat(GREN.s());
              if(Math.abs(x-1.0)<=1E-4)
                  GREN.setDS("");
          }
          gren_s=GREN.s();dgren_s=GREN.ds();  
          
          //debug
          //System.out.println("In Normal: line316: gren_d="+gren_d+" nr_d="+nr_d+" br_d="+br_d+" gren_s="+gren_s+" nr_s="+nr_s+" br_s="+br_s);
      }else if(nt_d>0 && br_d>0){//Ig+ce normalization factor NT>0, branching ratio BR>0, in case NR is not given but NT is given
          AbsInt rn=new AbsInt(); 
          rn.setValues(nt_d , dnt_d , br_d , dbr_d );  
          gren_d=rn.AId(); dgren_d=rn.DAId();   

          SDS2XDX NT=new SDS2XDX();NT.setValues(nt_s, dnt_s);
          SDS2XDX BR=new SDS2XDX();BR.setValues(br_s, dbr_s);
          SDS2XDX GREN;
          if(isBRUnity) 
              GREN=NT;
          else if(isNTUnity) 
              GREN=BR;
          else { 
              GREN=NT.multiply(BR);
              float x=Float.parseFloat(GREN.s());
              if(Math.abs(x-1.0)<=1E-4)
                  GREN.setDS("");
          }
          
          gren_s=GREN.s();dgren_s=GREN.ds();  
      }
         
      isGRenUnity=isUnity(gren_s,dgren_s);
      if(isGRenUnity) {
    	  gren_s="1.0";dgren_s="";
    	  gren_d=1.0;dgren_d=0;
      }
      
      /*old
      if( nt_d > 0 && br_d > 0 ){
         AbsInt rn=new AbsInt(); 
         rn.setValues(nt_d , dnt_d , br_d , dbr_d );  
         tiren_d=rn.AId(); dtiren_d=rn.DAId();
      }
      else if( ntbr_d > 0 &&  ( br_d <= 0 || nt_d <= 0 )  ){
         tiren_d=ntbr_d; 
         if( dntbr_d >= 0 ) dtiren_d=dntbr_d;
      }      
      */
      
      /*new*/
      if(ntbr_d > 0){
          tiren_d=ntbr_d;
          if( dntbr_d >= 0 ) 
              dtiren_d=dntbr_d;
          
          tiren_s=ntbr_s;
          dtiren_s=dntbr_s;
      }else if(nt_d > 0 && br_d > 0){   
          AbsInt rn=new AbsInt(); 
          rn.setValues(nt_d , dnt_d , br_d , dbr_d );  
          tiren_d=rn.AId(); dtiren_d=rn.DAId();   
     
          
          SDS2XDX NT=new SDS2XDX();NT.setValues(nt_s, dnt_s);
          SDS2XDX BR=new SDS2XDX();BR.setValues(br_s, dbr_s);   
          SDS2XDX TIREN;
          if(isBRUnity) 
              TIREN=NT;
          else if(isNTUnity) 
              TIREN=BR;
          else { 
              TIREN=NT.multiply(BR);
              float x=Float.parseFloat(TIREN.s());
              if(Math.abs(x-1.0)<=1E-4)
                  TIREN.setDS("");
          }
          
          tiren_s=TIREN.s();dtiren_s=TIREN.ds();  
           
      }else if(nt_s.isEmpty() && !gren_s.isEmpty()) {
          tiren_s=gren_s;
          dtiren_s=dgren_s;
      }

      isTIRenUnity=isUnity(tiren_s,dtiren_s);
      if(isTIRenUnity) {
    	  tiren_s="1.0";dtiren_s="";
    	  tiren_d=1.0;dtiren_d=0;
      }
      
      /*old
      if( nb_d > 0 && br_d > 0 ){
          AbsInt rn=new AbsInt(); 
          rn.setValues(nb_d , dnb_d , br_d , dbr_d );  
          bmren_d=rn.AId(); dbmren_d=rn.DAId();
      }
      else if( nbbr_d > 0 &&  nb_d <= 0  ){
         bmren_d=nbbr_d; 
         if( dnbbr_d >= 0 ) dbmren_d=dnbbr_d;
      }      
      */
      
      /*new*/
      if(nbbr_d > 0){
          bmren_d=nbbr_d;
          ecbpren_d=nbbr_d;
          if( dnbbr_d >= 0 ) {dbmren_d=dnbbr_d;decbpren_d=dnbbr_d;}
          bmren_s=nbbr_s;dbmren_s=dnbbr_s;
          ecbpren_s=nbbr_s;decbpren_s=dnbbr_s;
      }else if(nb_d > 0 && br_d > 0){   
          AbsInt rn=new AbsInt(); 
          rn.setValues(nb_d , dnb_d , br_d , dbr_d );  
          bmren_d=rn.AId(); dbmren_d=rn.DAId();  
          ecbpren_d=rn.AId();decbpren_d=rn.DAId();
          
          SDS2XDX NB=new SDS2XDX();NB.setValues(nb_s, dnb_s);
          SDS2XDX BR=new SDS2XDX();BR.setValues(br_s, dbr_s);
          SDS2XDX BMREN;     
          if(isBRUnity) 
              BMREN=NB;   
          else if(isNBUnity) 
              BMREN=BR;  
          else {
              BMREN=NB.multiply(BR);
              float x=Float.parseFloat(BMREN.s());
              if(Math.abs(x-1.0)<=1E-4)
                  BMREN.setDS("");
          }
          
          bmren_s=BMREN.s();dbmren_s=BMREN.ds();
          ecbpren_s=BMREN.s();decbpren_s=BMREN.ds();
          //System.out.println(nb_s+" "+dnb_s+" "+br_s+" "+dbr_s+" "+bmren_s+" "+dbmren_s);
      }
      
      isBMRenUnity=isUnity(bmren_s,dbmren_s);
      isECBPRenUnity=isUnity(ecbpren_s,decbpren_s);      
      if(isBMRenUnity) {
    	  bmren_s="1.0";dbmren_s="";
    	  bmren_d=1.0;dbmren_d=0;
    	  
    	  ecbpren_s="1.0";decbpren_s="";
    	  ecbpren_d=1.0;decbpren_d=0;
      }
  };
  
  protected void doWork() throws Exception{

    exists=true;

    recordLine=(String)lines.elementAt(0);
    
    String line="";
    for( int k=0; k<lines.size(); k++){
    	
  	  line=(String)lines.elementAt(k);
  	  line=Str.fixLineLength(line, 80);
  	  
      String temp=line.substring(5,9);
      
      if( temp.substring(0, 3).equals("  N") && (temp.charAt(3)==' ' || Character.isDigit(temp.charAt(3))) ){

          normExists=true;

          nr_s=line.substring(9,19).trim();  
          /* if( nr_s.length() == 0 ) nr_s="0.91996";   */   
          dnr_s=line.substring(19,21).trim(); 
          parseNR();
                   
          nt_s=line.substring(21,29).trim();  
          dnt_s=line.substring(29,31).trim();
          parseNT();
          
          br_s=line.substring(31,39).trim();
          dbr_s=line.substring(39,41).trim();
          parseBR();

          nb_s=line.substring(41,49).trim();  
          dnb_s=line.substring(49,55).trim(); 
          parseNB();
          
          np_s=line.substring(55,62).trim();
          dnp_s=line.substring(62,64).trim();
          parseNP();

      }else if( temp.equals(" PN ") ){
          recordLinePN=line;
    	  
          pNormExists=true;

          nrbr_s=line.substring(9,19).trim(); 
          dnrbr_s=line.substring(19,21).trim();
          parseNRBR();

          ntbr_s=line.substring(21,29).trim();  
          dntbr_s=line.substring(29,31).trim();
          parseNTBR();
     
          nbbr_s=line.substring(41,49).trim();
          dnbbr_s=line.substring(49,55).trim();
          parseNBBR();


          pncom_s=line.substring(76,77).trim();
          pnopt_s=line.substring(77,78).trim();

      }

    }


    /* The continuation records: */
    
    boolean isExtension=true;
    
    Vector<String> entries;
    ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("NR","NT","BR","NB"));
    line="";
    
   for( int k=0; k< lines.size(); k++){
       line=lines.elementAt(k);
       
       String temp=line.substring(5,9);
       if( temp.charAt(0)!=' ' && temp.substring(1, 3).equals(" N") && (temp.charAt(3)==' ' || Character.isDigit(temp.charAt(3))) ){

            FindValue fv=new FindValue();

            crLineV.addElement(line);
            
            entries=EnsdfUtil.findContRecordEntries(line);//find entries of all continuation records in current line including decay modes
            
            //find out if this beta is suppressed or not
            boolean isSuppressed=true;
            boolean isNewCRLine=false;
            
            String contType=line.substring(5,6);
            isSuppressed=contType.equals("S");
            
            for(int m=0;m<entries.size();m++){
                String entry=entries.get(m);

                
                fv.clear();
                fv.findValueInEntry(entry); 
                String name=fv.name();
          
                if(!fv.canGetV())
                    continue;
                
                ContRecord cr=new ContRecord();
                String crName=name;
                String crText=fv.txt();
                
                //System.out.println(" line="+line+" entry="+entry+" fv.s()="+fv.s()+"  "+fv.ds()+" dsu="+fv.dsu()+" dxu="+fv.dxu());
                
                isExtension=false;
                
                boolean isSupTemp=isSuppressed;
                if(recordNames.contains(crName)) {
                    isSupTemp=false;
                    isExtension=true;
                }
                //cr.setValues(crName,fv.symbol(),fv.s(),"",fv.ds(),"",crText,isSuppressed,isNewline);  
                cr.setValues(crName,fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),crText,contType,isSupTemp,isNewCRLine,isExtension);
                crV.add(cr);

                if(name.equals("NR") && dnr_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
                   nr_s=cr.s();
                   dnr_s=cr.ds();            
                   parseNR();
                }else if(name.equals("BR") && dbr_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
                    br_s=cr.s();
                    dbr_s=cr.ds();            
                    parseBR();
                 }else if(name.equals("NT") && dnt_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
                     nt_s=cr.s();
                     dnt_s=cr.ds();            
                     parseNT();
                 }else if(name.equals("NB") && dnb_s.isEmpty() && !cr.s().isEmpty() && !cr.ds().isEmpty()) {                
                     nb_s=cr.s();
                     dnb_s=cr.ds();            
                     parseNT();                
                 }     
            }  
         }
   }
   
   makeContRecordsVMap();
   
/* The Comments: */
   Vector<String> comLinesV=new Vector<String>();
   
   for( int k=0; k< lines.size(); k++){
       
       comLinesV.clear();
       
       if( ( ((String) lines.elementAt(k)).substring(5,8).equals(" CN") ||   //(5,9)=" CN " or "CN1"
           ((String) lines.elementAt(k)).substring(5,8).equals(" cN") ) &&
           ((String) lines.elementAt(k)).substring(9).trim().length() > 0
          ){
             String sjunk="";
             if(!comLinesV.contains(lines.elementAt(k))){
                 sjunk=rTrim( ( (String) lines.elementAt(k)).substring(9,79) );
                 comLinesV.addElement(lines.elementAt(k));
             }

             
             int l=1;
             while ( (k+l) < lines.size()  && 
                     ( ((String) lines.elementAt(k+l)).substring(6,8).equals("CN") || 
                       ((String) lines.elementAt(k+l)).substring(6,8).equals("cN") ) && 
                     !((String) lines.elementAt(k+l)).substring(5,6).equals(" ") 
                    ){
                       String sline=(String) lines.elementAt(k+l);
                       if(!comLinesV.contains(sline)){ 
                           sjunk=sjunk+" "+ sline.substring(9,79).trim();
                           comLinesV.add(sline);
                       }
                       
                       l++;
             
             }
             if( sjunk.length() > 0 ) comNTextV.addElement(sjunk);
             
             if(comLinesV.size()>0) {
                Comment c=new Comment();
                c.setValues(comLinesV);
                commentNV.add(c); 
             }
       } 
       else if( ((String) lines.elementAt(k)).substring(6,8).equals("PN") && 
               !((String) lines.elementAt(k)).substring(5,6).equals(" ")  &&
               ((String) lines.elementAt(k)).substring(9).trim().length() > 0
              ){
                   
                 String sjunk="";
                 if(!comLinesV.contains(lines.elementAt(k))){
                     sjunk=rTrim( ( (String) lines.elementAt(k)).substring(9,79) );
                     comLinesV.addElement(lines.elementAt(k));
                 }
                 
                 int l=1;
                 while ( (k+l) < lines.size()  && 
                          ((String) lines.elementAt(k+l)).substring(6,8).equals("PN")  && 
                         !((String) lines.elementAt(k+l)).substring(5,6).equals(" ") 
                        ){
                     
                           String sline=(String) lines.elementAt(k+l);
                           if(!comLinesV.contains(sline)){ 
                               sjunk=sjunk+" "+ sline.substring(9,79).trim();
                               comLinesV.add(sline);
                           }
                 
                           l++;
               
                 }
                 
                 if( sjunk.length() > 0 ){
                     if(sjunk.charAt(0)=='$')
                         sjunk=sjunk.substring(1);
                     
                     comPNTextV.addElement(sjunk);
                 }
                 
                 if(comLinesV.size()>0) {
                     Comment c=new Comment();
                     c.setValues(comLinesV);
                     commentPNV.add(c); 
                  }
           } 
   }

   parseRenorm();
 
   //lines.clear();
   
/* End of dowork: */

  }

  

  public void makeContRecordsVMap() {
      crMap.clear();
      for(ContRecord cr:crV) {
          String name=cr.name();
          Vector<ContRecord> temp=crMap.get(name);
          if(temp==null) {
              temp=new Vector<ContRecord>();
              crMap.put(name,temp);
          }
          
          temp.add(cr);
      }
  }
  
  protected String rTrim(String s){
     String st=s.trim();
     int i=s.indexOf(st);
     String srt=s.substring(0,i)+st; 
     return srt;
  }

/*  getter methods */

  public String NRS(){ return nr_s;}
  public String DNRS(){ return dnr_s;}
  public double NRD(){ return nr_d;}
  public double DNRD(){ return dnr_d;}

  public String NTS(){ return nt_s;}
  public String DNTS(){ return dnt_s;}
  public double NTD(){ return nt_d;}
  public double DNTD(){ return dnt_d;}

  public String BRS(){ return br_s;}
  public String DBRS(){ return dbr_s;}
  public double BRD(){ return br_d;}
  public double DBRD(){ return dbr_d;}
  public void setBRD(double d,double dd){br_d=d;dbr_d=dd;}

  public String NBS(){ return nb_s;}
  public String DNBS(){ return dnb_s;}
  public double NBD(){ return nb_d;}
  public double DNBD(){ return dnb_d;}

  public String NPS(){ return np_s;}
  public String DNPS(){ return dnp_s;}
  public double NPD(){ return np_d;}
  public double DNPD(){ return dnp_d;}
  public void setNPD(double d, double dd){np_d=d; dnp_d=dd;}

  public String NRBRS(){ return nrbr_s;}
  public String DNRBRS(){ return dnrbr_s;}
  public double NRBRD(){ return nrbr_d;}
  public double DNRBRD(){ return dnrbr_d;}

  public String NTBRS(){ return ntbr_s;}
  public String DNTBRS(){ return dntbr_s;}
  public double NTBRD(){ return ntbr_d;}
  public double DNTBRD(){ return dntbr_d;}

  public String NBBRS(){ return nbbr_s;}
  public String DNBBRS(){ return dnbbr_s;}
  public double NBBRD(){ return nbbr_d;}
  public double DNBBRD(){ return dnbbr_d;}

  public String CS(){ return pncom_s;}
  public String OS(){ return pnopt_s;}
  public int implicitPN() {return implicit_opt;}
  public void setImplicitPN(int opt){implicit_opt=opt;}

  public double GRenD(){ return gren_d;}
  public double DGRenD(){ return dgren_d;}
  public String GRenS(){ return gren_s;}
  public String DGRenS(){ return dgren_s;}
  public void setGRenD(double d, double dd){gren_d=d; dgren_d=dd;}
  public void setGRenS(String s, String ds){gren_s=s; dgren_s=ds;}
  
  public double TIRenD(){ return tiren_d;}
  public double DTIRenD(){ return dtiren_d;}
  public String TIRenS(){ return tiren_s;}
  public String DTIRenS(){ return dtiren_s;}
  public void setTIRenD(double d, double dd){tiren_d=d; dtiren_d=dd;}
  public void setTIRenS(String s, String ds) {tiren_s=s; dtiren_s=ds;}
  
  public double BMRenD(){ return bmren_d;}
  public double DBMRenD(){ return dbmren_d;}
  public String BMRenS(){ return bmren_s;}
  public String DBMRenS(){ return dbmren_s;}
  public void setBMRenD(double d, double dd){bmren_d=d; dbmren_d=dd;}

  public double ECBPRenD(){ return ecbpren_d;}
  public double DECBPRenD(){ return decbpren_d;}
  public String ECBPRenS(){ return ecbpren_s;}
  public String DECBPRenS(){ return decbpren_s;}
  public void setECBPRenD(double d, double dd){ecbpren_d=d; decbpren_d=dd;}

  public boolean isGRenUnity() {return isGRenUnity;}
  public boolean isTIRenUnity() {return isTIRenUnity;}
  public boolean isBMRenUnity() {return isBMRenUnity;}
  public boolean isECBPRenUnity() {return isECBPRenUnity;}
  public boolean isNRUnity() {return isNRUnity;}
  public boolean isNTUnity() {return isNTUnity;}
  public boolean isNBUnity() {return isNBUnity;}
  public boolean isBRUnity() {return isBRUnity;}
  public boolean isNPUnity() {return isNPUnity;}

  
  public int nCommentsN(){ return commentNV.size(); }
  public Comment commentNAt(int k){ return (Comment) commentNV.elementAt(k); }
  public Vector<Comment> commentNV(){ return commentNV; }
  
  public int nCommentsPN(){ return commentPNV.size(); }
  public Comment commentPNAt(int k){ return (Comment) commentPNV.elementAt(k); }
  public Vector<Comment> commentPNV(){ return commentPNV; }
  
  public String commentNTextAt(int k){ return (String) comNTextV.elementAt(k); }
  public Vector<String> commentNTextV(){ return comNTextV; }
  

  public String commentPNTextAt(int k){ return (String) comPNTextV.elementAt(k); }
  public Vector<String> commentPNTextV(){ return comPNTextV; }
  
  public boolean exists(){return exists;}
  public boolean normExists(){return normExists;}
  public boolean pNormExists(){return pNormExists;}
  public boolean isAssumedNB() {return isAssumedNB;}
  
  public String getIndexS(){return indexS;}
  public void setIndexS(String s){indexS=s;}
  
  public String recordLine(){return recordLine;}
  public String recordLinePN(){return recordLinePN;}
  
  public HashMap<String,Vector<ContRecord>> contRecordsVMap(){return crMap;}
  public int nContRecords(){return crV.size();}
  
  public ContRecord contRecordAt(int k){
      return (ContRecord)crV.get(k);  
      //return contRecsV().get(k);
  }

  public Vector<ContRecord> contRecsV(){     
      return crV;     
  }
  public int nContRecordLines(){return crLineV.size();}
  public String contRecordLineAt(int k){return (String) crLineV.elementAt(k);}
  public Vector<String> contRecsLineV(){return crLineV;}
  
  public Vector<String> lines(){return lines;}
  
  public int id() {return id;}
  public void setID(int i) {id=i;}
  

}