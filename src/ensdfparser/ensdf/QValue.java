 package ensdfparser.ensdf;

import java.util.*;


//11/19/2019: revise the code to read and store continuation Q record

public class QValue extends Record{

  protected QV qbm,qbp,qec,sn,sp,qa,s2n,s2p,qep,qbn,q2b,q4b,qda,qpa,qna;
     
  protected String qref;
  protected String recordLine;
  
  protected Vector<String> commentLinesV=new Vector<String>();
  
  protected Vector<String> extraQLinesV=new Vector<String>();

  protected Vector<QV> extraQV=new Vector<QV>();
  
  protected boolean exists=false;

  protected String NUCID="";
  
  public QValue(){
    lines.clear();
    commentLinesV.clear();
    extraQLinesV.clear();
    
    extraQV.clear();
    
    NUCID="";
    
    recordLine="";
    qref="";
    
    qbm=new QV();qec=new QV();sn=new QV();sp=new QV();qa=new QV();s2n=new QV();s2p=new QV();qep=new QV();
    qbn=new QV();q2b=new QV();q4b=new QV();qda=new QV();qpa=new QV();qna=new QV();
  }

  public void setValues(Vector<String> v){
	  
	  lines.clear();   
	  lines.addAll(v); 
	  if( lines.size() > 0 ) doWork();
  }

  public void doWork(){

	  exists=true;

	  String st=(String) lines.elementAt(0);
	  for(int j=st.length(); j<=79; j++){ st=st+" ";}

	  recordLine=st;
	  
	  String s,ds;
	  
	  NUCID=st.substring(0,5).trim();
	  
	  s=st.substring(9,19).trim(); 
	  ds=st.substring(19,21).trim();
	  qbm.setValues(s, ds);
	  
	  s=st.substring(21,29).trim(); 
	  ds=st.substring(29,31).trim(); 
	  sn.setValues(s, ds);


	  s=st.substring(31,39).trim(); 
	  ds=st.substring(39,41).trim(); 
	  sp.setValues(s, ds);
	  
	  s=st.substring(41,49).trim(); 
	  ds=st.substring(49,55).trim(); 
	  qa.setValues(s, ds);
	  
	  qref=st.substring(55,79).trim(); 
	  

	  qbm.ref=qref;
	  sn.ref=qref;
	  sp.ref=qref;
	  qa.ref=qref;
	  
	  parseContinuationRecords();
	  
	  makeContRecordsVMap(); 
	  

	/* The Comments: */

	   for( int k=1; k< lines.size(); k++){
	       if( ( ((String) lines.elementAt(k)).substring(5,9).equals(" CQ ") || 
	           ((String) lines.elementAt(k)).substring(5,9).equals(" cQ ") ) &&
	           ((String) lines.elementAt(k)).substring(9).trim().length() > 0
	          ){
	             String sjunk=rTrim( ( (String) lines.elementAt(k)).substring(9,79) );
	             int l=1;
	             while ( (k+l) < lines.size()  && 
	                     ( ((String) lines.elementAt(k+l)).substring(6,8).equals("CQ") || 
	                       ((String) lines.elementAt(k+l)).substring(6,8).equals("cQ") ) && 
	                     !((String) lines.elementAt(k+l)).substring(5,6).equals(" ") 
	                    ){
	                       sjunk=sjunk+" "+ ((String) lines.elementAt(k+l)).substring(9,79).trim();
	                       l++;
	             }
	             if( sjunk.length() > 0 ) commentLinesV.addElement(sjunk);
	       }  
	       
	       if(  ((String) lines.elementAt(k)).substring(5,9).equals("  Q ")  &&
	               ((String) lines.elementAt(k)).substring(9).trim().length() > 0){
	           extraQLinesV.add(lines.elementAt(k));
	       }
	              
	   }

/* End of dowork: */

  }
  
  protected void parseContinuationRecords() {
      extraQV.clear();
      
      /* continuation Q record */

      boolean isNewCRLine=false;
      boolean isSuppressed=false;      
      boolean isExtension=false;
   
      Vector<String> entries;   
      ArrayList<String> recordNames=new ArrayList<String>(Arrays.asList("Q-","QB","QBM","QB-","SN","SP","QA"));
      String line="";
      for( int k=0; k< lines.size(); k++){

          line=(String) lines.elementAt(k);       
           
          //System.out.println(line);
          
          if( line.substring(6,9).equals(" Q ") && ! line.substring(5,6).equals(" ")) { 
              crLineV.addElement(line);
                
              entries=findContRecordEntries(line);//find entries of all continuation records in current line including decay modes
  
              FindValue fv=new FindValue(); 

              isNewCRLine=true;       
        
              boolean skip=false;

              String contType=line.substring(5,6);
              
              isSuppressed=contType.equals("S");
              
                
              for(int m=0;m<entries.size();m++){
                   
                  String entry=entries.get(m);

                  fv.clear();               
                  fv.findValueInEntry(entry);         
                  String name=fv.name();
                    
                  //look for "SY" for systematic value 
                  String sds=fv.vNoSymbolNoRef();
                  int index=sds.indexOf(" SY");
                  boolean isSyst=false,isSystAMid=true;
                 
                  if(index>0){
                    isSyst=true;
                    if(sds.substring(index+3).trim().length()==0)
                       isSystAMid=false; 
                        
                    entry=entry.replace(" SY", "");
                    fv.clear();
                    fv.findValueInEntry(entry);                     
                  }
                 
                  boolean isCalc=false;
                  index=sds.indexOf(" CA"); 
                  if(index>0) {
                      isCalc=true;
                      if(sds.substring(index+3).trim().length()==0)
                         isSystAMid=false; 
                          
                      entry=entry.replace(" CA", "");
                      fv.clear();
                      fv.findValueInEntry(entry); 
                  }
                  //debug                    
                  //System.out.println("In Level.java line 704: fv.canGetV="+fv.canGetV()+" entry="+entry+" name="+name+" v="+fv.v());

                  if(!fv.canGetV())         
                      continue;    
                     
                  ContRecord cr=new ContRecord();
                  String crName=name;
                  String crText=fv.txt();
                     
                  skip=false;
                     
                  isExtension=false;
                  
                  boolean isSupTemp=isSuppressed;
                  if(recordNames.contains(crName)) {
                      isSupTemp=false;
                      isExtension=true;
                      skip=true;
                  }               
                     
                  //cr.setValues(crName,fv.symbol(),fv.s(),"",fv.ds(),"",crText,isSuppressed,isNewline);       
                  cr.setValues(crName,fv.symbol(),fv.s(),fv.units(),fv.ds(),fv.ref(),crText,contType,isSupTemp,isNewCRLine,isExtension);    
                  crV.add(cr);
                  
                     
                  //debug          
                  //System.out.println("In Level.java line 673: cr.name="+cr.name()+" name="+name+" cr.s="+cr.s()+" cr.txt="+cr.txt()+" fv.txt()="+fv.txt());
                     
                     
                     
                  /* T1/2 from level continuation records, only cases like T=120 NS 40 are accepted at the 
                        moment, one should not use it for cases like T AP 120 NS */

                    
                  //QBM,SN,SP,QA will be still printed in the first line (not included in extraQV)
                     
                  if( (name.equals("QBM")||name.equals("QB-")||name.equals("QB")||name.equals("Q-")) && qbm.s.isEmpty()){
                      qbm.setValues(name,fv.s(), fv.ds());
                      qbm.symbol=fv.symbol();qbm.ref=fv.ref();                   
                      qbm.isSyst=isSyst;qbm.isSystAMid=isSystAMid; 
                      qbm.isCalc=isCalc;
                  }else if(name.equals("QEC")){
                      qec.setValues(name,fv.s(), fv.ds());
                      qec.symbol=fv.symbol();qec.ref=fv.ref();                   
                      qec.isSyst=isSyst;qec.isSystAMid=isSystAMid;qec.isCalc=isCalc;
                      extraQV.add(qec); 
                  }else if(name.equals("QBP") || name.equals("QB+") || name.equals("Q+")){
                      qbp.setValues(name,fv.s(), fv.ds());
                      qbp.symbol=fv.symbol();qbp.ref=fv.ref();                   
                      qbp.isSyst=isSyst;qbp.isSystAMid=isSystAMid;qbp.isCalc=isCalc;
                      extraQV.add(qbp);
                  }else if(name.equals("SN") && sn.s.isEmpty()) {
                      sn.setValues(name,fv.s(), fv.ds());
                      sn.symbol=fv.symbol();sn.ref=fv.ref(); 
                      sn.isSyst=isSyst;sn.isSystAMid=isSystAMid;sn.isCalc=isCalc;
                  }else if(name.equals("SP") && sp.s.isEmpty()) {
                      sp.setValues(name,fv.s(), fv.ds());
                      sp.symbol=fv.symbol();sp.ref=fv.ref(); 
                      sp.isSyst=isSyst;sp.isSystAMid=isSystAMid;sp.isCalc=isCalc;  
                  }else if(name.equals("QA") && qa.s.isEmpty()) {
                      qa.setValues(name,fv.s(), fv.ds());
                      qa.symbol=fv.symbol();qa.ref=fv.ref(); 
                      qa.isSyst=isSyst;qa.isSystAMid=isSystAMid;qa.isCalc=isCalc;
                  }else if(name.equals("S2N")) {
                      s2n.setValues(name,fv.s(), fv.ds());
                      s2n.symbol=fv.symbol();s2n.ref=fv.ref(); 
                      s2n.isSyst=isSyst;s2n.isSystAMid=isSystAMid;s2n.isCalc=isCalc;
                      extraQV.add(s2n);
                  }else if(name.equals("S2P")) {
                      s2p.setValues(name,fv.s(), fv.ds());
                      s2p.symbol=fv.symbol();s2p.ref=fv.ref(); 
                      s2p.isSyst=isSyst;s2p.isSystAMid=isSystAMid;s2p.isCalc=isCalc;
                      extraQV.add(s2p); 
                  }else if(name.equals("QECP") || name.equals("QEP")) {
                      qep.setValues(name,fv.s(), fv.ds());
                      qep.symbol=fv.symbol();qep.ref=fv.ref(); 
                      qep.isSyst=isSyst;qep.isSystAMid=isSystAMid;qep.isCalc=isCalc;
                      extraQV.add(qep);
                  }else if(name.equals("QBMN") || name.equals("QB-N") || name.equals("QBN")) {
                      qbn.setValues(name,fv.s(), fv.ds());
                      qbn.symbol=fv.symbol();qbn.ref=fv.ref(); 
                      qbn.isSyst=isSyst;qbn.isSystAMid=isSystAMid;qbn.isCalc=isCalc;
                      extraQV.add(qbn);
                  }else if(name.equals("Q2B")) {
                      q2b.setValues(name,fv.s(), fv.ds());
                      q2b.symbol=fv.symbol();q2b.ref=fv.ref(); 
                      q2b.isSyst=isSyst;q2b.isSystAMid=isSystAMid;q2b.isCalc=isCalc;
                      extraQV.add(q2b);
                  }else if(name.equals("Q4B")) {
                      q4b.setValues(name,fv.s(), fv.ds());
                      q4b.symbol=fv.symbol();q4b.ref=fv.ref(); 
                      q4b.isSyst=isSyst;q4b.isSystAMid=isSystAMid;q4b.isCalc=isCalc;
                      extraQV.add(q4b);
                  }else if(name.equals("QDA")) {
                      qda.setValues(name,fv.s(), fv.ds());
                      qda.symbol=fv.symbol();qda.ref=fv.ref(); 
                      qda.isSyst=isSyst;qda.isSystAMid=isSystAMid;qda.isCalc=isCalc;
                      extraQV.add(qda);
                  }else if(name.equals("QPA")) {
                      qpa.setValues(name,fv.s(), fv.ds());
                      qpa.symbol=fv.symbol();qpa.ref=fv.ref(); 
                      qpa.isSyst=isSyst;qpa.isSystAMid=isSystAMid;qpa.isCalc=isCalc;
                      extraQV.add(qpa);                 
                  }else if(name.equals("QNA")) {
                      qna.setValues(name,fv.s(), fv.ds());
                      qna.symbol=fv.symbol();qna.ref=fv.ref(); 
                      qna.isSyst=isSyst;qna.isSystAMid=isSystAMid;qna.isCalc=isCalc;
                      extraQV.add(qna);                   
                  }else {
                      QV qv=new QV();
                      qv.setValues(name,fv.s(), fv.ds());
                      qv.symbol=fv.symbol();qv.ref=fv.ref(); 
                      qv.isSyst=isSyst;qv.isSystAMid=isSystAMid;qv.isCalc=isCalc;
                      extraQV.add(qv);                 
                  }


                  if(!skip)//the first entry in current line has been processed (not skipped), and the rest are in the same line instead of a new line       
                      isNewCRLine=false;     
                 
              }             

          }


      }      
  }
  
  protected String rTrim(String s){
     String st=s.trim();
     int i=s.indexOf(st);
     String srt=s.substring(0,i)+st; 
     return srt;
  }

/*  getter methods */

  public boolean exists(){ return exists;}

  public QV QBM() {return qbm;}
  public QV QEC() {return qec;}
  public QV SN() {return sn;}
  public QV SP() {return sp;}
  public QV QA(){return qa;}
  public QV S2P(){return s2p;}
  public QV S2N(){return s2n;}
  public QV QEP(){return qep;}
  public QV QBN(){return qbn;}
  public QV Q2B(){return q2b;}
  public QV Q4B(){return q4b;}
  public QV QDA(){return qda;}
  public QV QPA(){return qpa;}
  public QV QNA(){return qna;}
   
  
  public String QBMS(){return qbm.s;}
  public String DQBMS(){return qbm.ds;}
  public float QBMF(){return qbm.x;}
  public float DQBMF(){return qbm.dx;}
  public float QQBMUVF(){return qbm.uv;}
  public float QQBMLVF(){return qbm.lv;}
  
  public String QECS(){return qec.s;}
  public String DQECS(){return qec.ds;}
  public float QECF(){return qec.x;}
  public float DQECF(){return qec.dx;}
  public float QECUVF(){return qec.uv;}
  public float QECLVF(){return qec.lv;}
  
  public String SNS(){return sn.s;}
  public String DSNS(){return sn.ds;}
  public float SNF(){return sn.x;}
  public float DSNF(){return sn.dx;}
  public float SNUVF(){return sn.uv;}
  public float SNLVF(){return sn.lv;}
  
  public String SPS(){return sp.s;}
  public String DSPS(){return sp.ds;}
  public float SPF(){return sp.x;}
  public float DSPF(){return sp.dx;}
  public float SPUVF(){return sp.uv;}
  public float SPLVF(){return sp.lv;}
  
  public String QAS(){return qa.s;}
  public String DQAS(){return qa.ds;}
  public float QAF(){return qa.x;}
  public float DQAF(){return qa.dx;}
  public float QAUVF(){return qa.uv;}
  public float QALVF(){return qa.lv;}

  public String QRef(){return qref;}
  public void setQRef(String s) {qref=s;}
  
  public String S2PS(){return s2p.s;}
  public String DS2PS(){return s2p.ds;}
  public float S2PF(){return s2p.x;}
  public float DS2PF(){return s2p.dx;}
  public float S2PUVF(){return s2p.uv;}
  public float S2PLVF(){return s2p.lv;}
  
  public String S2NS(){return s2n.s;}
  public String DS2NS(){return s2n.ds;}
  public float S2NF(){return s2n.x;}
  public float DS2NF(){return s2n.dx;}
  public float S2NUVF(){return s2n.uv;}
  public float S2NLVF(){return s2n.lv;}
  
  public String QEPS(){return qep.s;}
  public String DQEPS(){return qep.ds;}
  public float QEPF(){return qep.x;}
  public float DQEPF(){return qep.dx;}
  public float QEPUVF(){return qep.uv;}
  public float QEPLVF(){return qep.lv;}
  
  public String QBNS(){return qbn.s;}
  public String DQBNS(){return qbn.ds;}
  public float QBNF(){return qbn.x;}
  public float DQBNF(){return qbn.dx;}
  public float QBNUVF(){return qbn.uv;}
  public float QBNLVF(){return qbn.lv;}
  
  public String Q2BS(){return q2b.s;}
  public String DQ2BS(){return q2b.ds;}
  public float Q2BF(){return q2b.x;}
  public float DQ2BF(){return q2b.dx;}
  public float Q2BUVF(){return q2b.uv;}
  public float Q2BLVF(){return q2b.lv;}
  
  public String Q4BS(){return q4b.s;}
  public String DQ4BS(){return q4b.ds;}
  public float Q4BF(){return q4b.x;}
  public float DQ4BF(){return q4b.dx;}
  public float Q4BUVF(){return q4b.uv;}
  public float Q4BLVF(){return q4b.lv;}

  public String QDAS(){return qda.s;}
  public String DQDA2BS(){return qda.ds;}
  public float QDAF(){return qda.x;}
  public float DQDAF(){return qda.dx;}
  public float QDAUVF(){return qda.uv;}
  public float QDALVF(){return qda.lv;}

  public String QPAS(){return qpa.s;}
  public String DQPAS(){return qpa.ds;}
  public float QPAF(){return qpa.x;}
  public float DQPAF(){return qpa.dx;}
  public float QPAUVF(){return qpa.uv;}
  public float QPALVF(){return qpa.lv;}

  public String QNAS(){return qna.s;}
  public String DQNAS(){return qna.ds;}
  public float QNAF(){return qna.x;}
  public float DQNAF(){return qna.dx;}
  public float QNAUVF(){return qna.uv;}
  public float QNALVF(){return qna.lv;}

  public String q() {return "";}
  public String coinS() {return "";}
    
  public String recordLine(){return recordLine;}
  
  public Vector<QV> extraQV(){return extraQV;}
  public Vector<String> extraQLinesV(){return extraQLinesV;}
  public Vector<String> commentLinesV(){return commentLinesV;}
  

  @Override
  public String ES() {
      // TODO Auto-generated method stub
      return "";
  }

  @Override
  public String DES() {
      // TODO Auto-generated method stub
      return "";
  }

  @Override
  public float EF() {
      // TODO Auto-generated method stub
      return 0;
  }

  @Override
  public float DEF() {
      // TODO Auto-generated method stub
      return 0;
  }

  @Override
  public String RIS() {
      // TODO Auto-generated method stub
      return "";
  }

  @Override
  public String DRIS() {
      // TODO Auto-generated method stub
      return "";
  }

  @Override
  public String flag() {
      // TODO Auto-generated method stub
      return "";
  }


public class QV{
    
    public float x=-1.0f,dx=-1.0f,uv=-1.0f,lv=-1.0f;
    public String name="",symbol="",ref="",s="",ds="";
    public boolean isSyst=false,isSystAMid=true,isCalc=false;    
    
    public SDS2XDX s2x=null;
    
    public void setValues(String nameS,String valS,String uncS) {   
        name=nameS;
        setValues(valS,uncS);
    }
    
    public void setValues(String valS,String uncS) {    
        s=valS;
        ds=uncS;
        
        s2x=new SDS2XDX();
        s2x.setValues(s,ds);
        if( s2x.canGetX() ) x=(float) s2x.X();
        if( s2x.canGetDX() ) dx=(float) s2x.DX();
        if( s2x.canGetXUV() ) uv=(float) s2x.XUV();
        if( s2x.canGetXLV() ) lv=(float) s2x.XLV();
    }
}


@Override
public void addFlag(String s) {
	// TODO Auto-generated method stub
	
}


}