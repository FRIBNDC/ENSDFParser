package ensdfparser.ensdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import ensdfparser.nds.util.Str;

public final class DatasetID {
    public String dsid="",nucid="",datasetType="";
    public String date="",pub="",year="";
    public String decayType="",decayTimeS="",decayTimeUnit="",decayParent="",keynumbers="";
    public String beam="",target="",ejectile="";
    
    public int decayTypeOrder=-1;
    public String otherLabel="";
    public String particleType="",delayType="";//for delayed or prompt particle decay, delayType="B-","B+","EC". decayType=delayType+particleType
    
    public Vector<String> beamsV=new Vector<String>();
    public Vector<String> targetsV=new Vector<String>();
    public Vector<String> ejectilesV=new Vector<String>();
    
    public DatasetID() {
    }
    
    @SuppressWarnings("unused")
	public DatasetID(String DSIDLine){
    	
        try {
        	//note that it also takes dsid string
        	//so first check if the input line is a full ENSDF DSID line or
        	//simply a DSID string
        	
        	if(DSIDLine.length()>9) {
        		String tempNUCID=DSIDLine.substring(0,5);
        		String AS=DSIDLine.substring(0,3);
            	String lineType=DSIDLine.substring(5,9);
            	
            	if(lineType.trim().isEmpty() && !tempNUCID.trim().isEmpty()) {      
            	//if(lineType.trim().isEmpty() && !tempNUCID.trim().isEmpty()&&Str.isNumeric(AS.trim())) {
            		nucid=tempNUCID;
            		dsid=DSIDLine.substring(9).trim();
            		
                	String line=Str.fixLineLength(DSIDLine, 80);                                 
                    keynumbers=line.substring(39,65).trim();
                    pub=line.substring(65,74).trim();
                    date=line.substring(74).trim();
                    if(date.length()>4) {
                        year=date.substring(0,4);
                    }
            	}else
            		dsid=DSIDLine.trim();

        	}else {
        		dsid=DSIDLine.trim();
        	}
        	
            String s=dsid.toUpperCase();
            int ilbracket=s.indexOf("(");
            int irbracket=s.indexOf(")");
            int icomma=s.indexOf(",");
            
            //for case like, 32,33CL(24MG,XG)
            int prevI=0;
            while(icomma>=0 && icomma<ilbracket) {
                String tempS=dsid.substring(prevI,icomma).trim();
                if(Str.isNumeric(tempS)) {
                    prevI=icomma+1;
                    icomma=dsid.indexOf(",",prevI);
                    continue;
                }else
                    break;
            }
            
            if(ilbracket>0 && ilbracket<icomma-1 && icomma<irbracket-1){
                datasetType="REACTION";

                target=s.substring(0, ilbracket).trim();
                beam=s.substring(ilbracket+1, icomma).trim();
                ejectile=s.substring(icomma+1, irbracket);
                
                if(beam.equals("N") && ejectile.equals("G"))
                	datasetType="NEUTRON";
                
                beamsV.add(beam);
                targetsV.add(target);
                ejectilesV.add(ejectile);
                
                String[] temp=Str.splitOutsideBrackets(dsid, ",");
                if(temp.length>1) {
                	for(int i=1;i<temp.length;i++) {
                		String rs=temp[i];
                		int nl=rs.indexOf("(");
                		int nc=rs.indexOf(",");
                		int nr=rs.indexOf(")");
                		if(nl>=0 && nc>nl && nr>nc) {
                      		String tempTarget=rs.substring(0,nl).trim();
                      		String tempBeam=rs.substring(nl+1,nc).trim();
                      		String tempEjectile=rs.substring(nc+1,nr).trim();
                      		if(tempTarget.isEmpty())
                      			tempTarget=target;
                      		
                      		beamsV.add(tempBeam);
                      		targetsV.add(tempTarget);
                      		ejectilesV.add(tempEjectile);
                		} 
                	}
                }
                
            }else if(s.indexOf("DECAY")>0){

                datasetType="DECAY";
                
                s=s.replace(':',' ').replace('(',' ').replace(')',' ');
                String[] parts=Str.trimArray(s.split("[ ]+"));
                
                //debug
                //System.out.println(" s="+s+" len="+parts.length);
                //System.out.println(" part1="+parts[1]+" part3="+parts[3]);
                        
            
                if(parts.length<3 || !parts[2].equals("DECAY"))
                    decayTypeOrder=10000;
                else{
                    decayParent=parts[0];
                    decayType=parts[1];
                    if(parts.length>4 && Str.isNumeric(parts[3])){
                        decayTimeS=parts[3];
                        decayTimeUnit=parts[4];
                    }
                    decayTypeOrder=getDecayOrder(decayType);

                }

            }else if(s.startsWith("ADOPTED"))
                datasetType="ADOPTED";
            else if(s.startsWith("COMMENTS"))
                datasetType="COMMENTS";
            else if(s.startsWith("REFERENCES"))
                datasetType="REFERENCES";
            else if(s.contains("INELASTIC"))
                datasetType="INELASTIC";
            else if(s.contains("COULOMB EXCITATION"))
                datasetType="COULEX";
            else if(s.contains("(N,G)"))
                datasetType="NEUTRON";
            else if(s.contains("(HI,X"))//DSID=(HI,XNG), or DSID=123AB(HI,XG)
                datasetType="HIXNG";
      	
        }catch(Exception e) {
        	e.printStackTrace();
        }

    }
    public DatasetID(ENSDF ens){        
        this(ens.lineAt(0));
    }
    
    
    private int getDecayOrder(String decayType){
        try {
            String s=decayType.trim().toUpperCase();
            if(decayType.isEmpty())
                return -1;
            
            ArrayList<String> types=new ArrayList<String>(Arrays.asList(
                    "B-","B+","EC","EC+B+","A","IT","B-N","ECP","B-2N","B-3N","B-A","B-P","B+P","B+2P","B+3P",
                    "B+A","EC2P","EC3P","ECA","SF","P","N","2N","2P","3N","3P"
                    ));
            ArrayList<String> dpTypes=new ArrayList<String>(Arrays.asList(
                    "B-N","B-2N","B-3N","B-A","B-P","B+P","B+2P","B+3P",
                    "B+A","ECP","EC2P","EC3P","ECA","P","N","2N","2P","3N","3P"
                    ));
            ArrayList<String> regularTypes= new ArrayList<String>(Arrays.asList("B-","EC","B+","EC+B+","P","N","A"));
            
            int n=types.indexOf(s);
            if(n>=0) {
            	if(dpTypes.contains(s)) {
            		if(s.length()==1) {
            			particleType=s;
            		}else if(s.length()==2 && Character.isDigit(s.charAt(0))) {
            			particleType=s;
            		}else {
            			if(s.startsWith("B+")) {
            				delayType="B+";
            				particleType=s.substring(2);
            			}else if(s.startsWith("B-")) {
            				delayType="B-";
            				particleType=s.substring(2);
            			}else if(s.startsWith("EC")) {
            				delayType="EC";
            				particleType=s.substring(2);
            			}
            		}
            	}
            	
                return n;
            }
            
            char c=s.charAt(0);
            if(Character.isDigit(c)) {
                int m=Integer.parseInt(c+"");
                if(m>0) {
                    String temp=s.substring(1);
                    n=regularTypes.indexOf(temp);
                    if(n>=0)
                        return types.size()+(m-1)*regularTypes.size()+n;
                    
                    //check if heavy ion decay
                    Nucleus nuc=new Nucleus(s);
                    if(nuc.A()>0 && nuc.Z()>=0)
                        return 200+nuc.A();
                    
                }
                
            }            
        }catch(Exception e) {
            
        }

        return 1000;
    }

    public boolean isSameDecayType(DatasetID DID){
        String s=this.dsid;
        String dsid=DID.dsid;
        
        if(s.length()==0 || dsid.length()==0)
            return false;
        
        if(s.equals(dsid))
            return true;    

        if(!DID.decayParent.isEmpty()){
            if(this.decayParent.equals(DID.decayParent) && this.decayType.equals(DID.decayType)){
                //System.out.println("dsid1="+s+" this.decayTimeUnit="+this.decayTimeUnit+" dsid2="+dsid+" DID.decayTimeUnit="+DID.decayTimeUnit+" "+this.decayTimeS+" "+DID.decayTimeS);
                return true;
            }
        }
        
        return false;
        
    }
    /*
     * check if the given dsid is the same as this dsid. It could be an exact match or 
     * it could match for most  parts (for the latter, in cases, the given dsid could be
     * the update of this dsid, updated T1/2 in DSID of decay datasets)
     */
    public boolean isSameDSID(DatasetID DID){
        String s=this.dsid;
        String dsid=DID.dsid;
        
        if(s.length()==0 || dsid.length()==0)
            return false;
        
        if(s.equals(dsid))
            return true;    

        if(!DID.decayParent.isEmpty()){
            if(this.decayParent.equals(DID.decayParent) && this.decayType.equals(DID.decayType)){
                //System.out.println("dsid1="+s+" this.decayTimeUnit="+this.decayTimeUnit+" dsid2="+dsid+" DID.decayTimeUnit="+DID.decayTimeUnit+" "+this.decayTimeS+" "+DID.decayTimeS);
                if(this.decayTimeUnit.contains(DID.decayTimeUnit) || DID.decayTimeUnit.contains(this.decayTimeUnit)){
                    try{
                        float t1=Float.parseFloat(this.decayTimeS);
                        float t2=Float.parseFloat(DID.decayTimeS);
                        float diff=Math.abs(t1-t2);
                        if(diff/t1<0.1)
                            return true;
                    }catch(Exception e){}
                }
            }
        }else if(!DID.beam.isEmpty() && !DID.target.isEmpty()){
            if(this.beam.equals(DID.beam) && this.target.equals(DID.target) && this.ejectile.equals(DID.ejectile)){
                int i1=s.indexOf("E=");
                int i2=dsid.indexOf("E=");
                if(i1<0 && i2<0)
                    return true;
                
                //System.out.println("DatasetID 125: dsid1="+s+" dsid2="+dsid+" i1="+i1+" i2="+i2);
                
                if(i1>0 && i2>0){
                    s=s.substring(i1+2).trim();
                    dsid=dsid.substring(i2+2).trim();
                    i1=s.indexOf(" ");
                    i2=dsid.indexOf(" ");
                    if(i1<0 && i2<0 && (s.contains(dsid)||dsid.contains(s)))
                        return true;
                    
                    if(i1>0 && i2>0){
                        float e1=0,e2=0,diff=0;
                        String u1="",u2="";
                        try{
                            e1=Float.parseFloat(s.substring(0,i1));
                            e2=Float.parseFloat(dsid.substring(0, i2));
                            diff=Math.abs(e1-e2);
                            u1=s.substring(i1);
                            u2=s.substring(i2);
                            if(u1.equalsIgnoreCase(u2) && (diff/e1)<0.2)
                                return true;
                            
                        }catch(Exception e){}
                    }
                }else if(s.contains(dsid) || dsid.contains(s)){
                    return true;
                }
            }
        }
        
        return false;
        
    }
    
    public boolean isSameDSID(String dsid){
        return isSameDSID(new DatasetID(dsid));
    }
}
