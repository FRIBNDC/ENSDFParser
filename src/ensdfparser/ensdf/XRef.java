 package ensdfparser.ensdf;

 public class XRef{

  private String dsid, dsid0,oldTag, newTag;
  @SuppressWarnings("unused")
private boolean isNG, isNNpG, isNR, isDecay, isITDecay, isECBPDecay, isBMDecay, isPDecay,isADecay,isSFDecay,isHIR; 

  public XRef(){
    dsid="";
    dsid0="";
    isNG=false; isNNpG=false; isNR=false; 
    isDecay=false; isITDecay=false; isECBPDecay=false; isBMDecay=false; isPDecay=false;isSFDecay=false; isADecay=false;
    isHIR=false; 
  }

  public void setValues(String soldtag, String snewtag, String sdsid){

     dsid=sdsid; 
     
     dsid0=dsid;
     
     //max length=30 for dataset DSID in the first line 
     if(dsid.trim().length()>30){//means the corresponding dataset has continuation DSID lines
    	 String s=dsid.trim();
    	 int ibreakBest=-1,ibreakAlt=-1,ibreak=-1;
    	 int nbrackets=0;
    	 for(int i=0;i<s.length();i++){
    		 char c=s.charAt(i);
    		 if(c=='(') nbrackets++;
    		 if(c==')') nbrackets--;
    		 
    		 if(i>=30) {
    			 break;
    		 }else if(c==' ' || c==':' || c==',') {
    			 if(nbrackets==0)
    				 ibreakBest=i;
    			 else
    				 ibreakAlt=i;
 			 } 
    	 }
    	 
    	 ibreak=-1;
    	 if(ibreakBest>0 && s.substring(0,ibreakBest).trim().length()>0)
    		 ibreak=ibreakBest;
    	 else if(ibreakAlt>0 && s.substring(0,ibreakAlt).trim().length()>0)
    		 ibreak=ibreakAlt;
    	 else
    		 ibreak=29;
    	 
    	 //System.out.println("XRef 51: s="+s+" ibreak="+ibreak+" dsid0="+dsid0+" dsid="+dsid+" "+ibreakBest+" "+ibreakAlt);
    	 ibreak=29;
    	 
    	 dsid0=s.substring(0, ibreak+1).trim();
     }
     
     //System.out.println(dsid+"$"+dsid0+"#");
     
     oldTag=soldtag;
     newTag=snewtag;
     if( dsid.toUpperCase().indexOf( "(N,G)" ) > 0 ) isNG=true;
     if( dsid.toUpperCase().indexOf( "(N,N'G)" ) > 0 ) isNNpG=true;
     if( dsid.toUpperCase().indexOf( "(N" ) > 0 ) isNR=true;

     if( dsid.toUpperCase().indexOf( "B- DECAY" ) > 0 ) isBMDecay=true;
     if( dsid.toUpperCase().indexOf( "B-N DECAY" ) > 0 ) isBMDecay=true;
     if( dsid.toUpperCase().indexOf( "B+ DECAY" ) > 0 ) isECBPDecay=true;
     if( dsid.toUpperCase().indexOf( "EC DECAY" ) > 0 ) isECBPDecay=true;
     if( dsid.toUpperCase().indexOf( "EC+B+ DECAY" ) > 0 ) isECBPDecay=true;
     if( dsid.toUpperCase().indexOf( "ECP DECAY" ) > 0 ) isECBPDecay=true;
     if( dsid.toUpperCase().indexOf( "IT DECAY" ) > 0 ) isITDecay=true;
     if( dsid.toUpperCase().indexOf( "A DECAY" ) > 0 ) isADecay=true;
     if( dsid.toUpperCase().indexOf( "P DECAY" ) > 0 ) isPDecay=true;
     if( dsid.toUpperCase().indexOf( "SF DECAY" ) > 0 ) isSFDecay=true;

     if( dsid.toUpperCase().indexOf( "DECAY" ) > 0 ) isDecay=true;


  }
  public String DSId(){return dsid;}
  public String DSId0(){return dsid0;}//dsid that matches the dataset DSID in the first line, col 10-39, length=30
  public String newTag(){return newTag;}
  public String tag(){return newTag;}
  public String oldTag(){return oldTag;}

  public boolean isDecay(){ return isDecay;}
  public boolean isITDecay(){ return isITDecay;}
  public boolean isBMDecay(){ return isBMDecay;}
  public boolean isECBPDecay(){ return isECBPDecay;}
  public boolean isPDecay(){ return isPDecay;}
  public boolean isADecay(){ return isADecay;}
  public boolean isSFDecay(){ return isSFDecay;}

  public boolean isNR(){ return isNR;}
  public boolean isNG(){ return isNG;}
  public boolean isNNpG(){ return isNNpG;}


}