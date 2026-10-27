package ensdfparser.ensdf;

import java.util.LinkedHashMap;
import java.util.Vector;

import ensdfparser.nds.ensdf.EnsdfUtil;

/*
 * store data for atomic radiation
 */
public class Atomic{
	
	protected Vector<String> linesV=new Vector<String>();
	
	protected Vector<String> crLinesV=new Vector<String>();
	
	protected LinkedHashMap<String,AtomicData> xRayMap=new LinkedHashMap<String,AtomicData>();
	protected LinkedHashMap<String,AtomicData> AugerMap=new LinkedHashMap<String,AtomicData>();
		
	protected Vector<Integer> poscV=new Vector<Integer>();
	protected Vector<Comment> commentV=new Vector<Comment>();
	  
    public boolean isEmpty() {
    	if(crLinesV.size()==0)
    		return true;
    	else
    		return false;
    }
    
	public void setValues(Vector<String> lines) throws Exception {
		   
		if(lines==null || lines.size()<=0)
			return;
        
		linesV.clear();
		linesV.addAll(lines);
		
		parseComments("M");
		
		Vector<String> entries;
		String line="";
		char type=' ';
		
		LinkedHashMap<String,AtomicData> tempMap=null;
		for(int i=0;i<lines.size();i++) {
			line=lines.get(i);
			
			if(line.length()<10 || line.charAt(6)!=' ')
				continue;
			
			type=line.charAt(8);
			
			if(type=='X')
				tempMap=xRayMap;
			else if(type=='A')
				tempMap=AugerMap;
			else
				continue;
			
    	    crLinesV.addElement(line);
    	
            entries=findContRecordEntries(line);//find entries of all continuation records in current line 
                   
            FindValue fv=new FindValue(); 
            for(int m=0;m<entries.size();m++){
            	String entry=entries.get(m);
            	
            	fv.clear();
            	fv.findValueInEntry(entry); 
            	String name=fv.name();
            	
                //debug
                //System.out.println("In Level.java line 704: fv.canGetV="+fv.canGetV()+" entry="+entry+" name="+name+" v="+fv.v());
                
                if(!fv.canGetV())
                	continue;
                
                int p1=name.indexOf("(");
                int p2=name.indexOf(")");
                String entryName=name;
                if(p1>=0 && p2>p1)
                	entryName=name.substring(p1+1,p2).trim().toUpperCase();
                
                AtomicData ad=null;
                if(tempMap.containsKey(entryName))
                	ad=(AtomicData) tempMap.get(entryName);
                else {
                	ad=new AtomicData();               
                    ad.name=entryName;
                    
                    ad.parsePos(ad.name);
                    
                    //System.out.println("  In Atomic line 87: type="+type+"  name="+name+" pos="+ad.pos);
                    
                    tempMap.put(entryName, ad);
                }
                
                if(name.startsWith("E(")) {
                    ad.ES=fv.s();
                    ad.DES=fv.ds();
                    ad.e=fv.x();
                    ad.de=fv.dx();
                }else if(name.startsWith("I(")) {
                    ad.IS=fv.s();
                    ad.DIS=fv.ds();
                    ad.ia=fv.x();
                    ad.dia=fv.dx();
                }
                
                ad.flag_s=line.substring(76,77).trim();
                ad.q_s=line.substring(79).trim();
                   
            }
		}
		
		
	}
	
    
    //////////////////////////////////////////////////////////////////////
    public int nComments(){ return commentV.size(); }
    public Comment commentAt(int k){ return (Comment) commentV.elementAt(k); }
    public Vector<Comment> commentV(){ return commentV; }
    
    public int nXRays(){return xRayMap.size();}
    public int nAugers(){return AugerMap.size();}
    
    public AtomicData xRaydAt(int k){
    	return (AtomicData)xRayMap.values().toArray()[k];  
    	//return contRecsV().get(k);
    }
    public AtomicData AugerAt(int k){
    	return (AtomicData)AugerMap.values().toArray()[k];  
    	//return contRecsV().get(k);
    }
    
    public Vector<AtomicData> xRaysV(){
    	Vector<AtomicData> out=new Vector<AtomicData>();
    	out.addAll(xRayMap.values());
    	return out;  	
    }
    public Vector<AtomicData> AugersV(){
    	Vector<AtomicData> out=new Vector<AtomicData>();
    	out.addAll(AugerMap.values());
    	return out;  	
    }
    
	public LinkedHashMap<String,AtomicData> xRayMap(){return xRayMap;}
	public LinkedHashMap<String,AtomicData> AugerMap(){return AugerMap;}
	
	
    public int nContRecordLines(){return crLinesV.size();}
    public String contRecordLineAt(int k){return (String) crLinesV.elementAt(k);}
    public Vector<String> contRecsLineV(){return crLinesV;}
        

	
	
    /* find names of all continuation records in an ENSDF line**/
    protected Vector<String> findContRecordNames(String line){
    	return EnsdfUtil.findContRecordNames(line);
    }

   
    /* find name of a continuation record entry**/
    protected String findContRecordName(String entry){
    	return EnsdfUtil.findContRecordName(entry);
    }
    
    /* find entries of all continuation records in an ENSDF line**/
    protected Vector<String> findContRecordEntries(String line){
    	return EnsdfUtil.findContRecordEntries(line);
    }
    
    //make a ContRecord object from the string of a continuation record entry 
    protected ContRecord makeContRecord(String entry,String contType,boolean isSuppressed, boolean isNewLine){
    	return EnsdfUtil.makeContRecord(entry, contType,isSuppressed, isNewLine);
    }
    
    
    /* find all continuation records in an ENSDF line**/
    protected Vector<ContRecord> findContRecords(String line){
    	return EnsdfUtil.findContRecords(line);
    }
    
    protected void parseComments(String typeS) throws Exception{
    	
    	if(typeS.trim().length()==0)
    		return;
    	
    	char type=typeS.trim().charAt(0);
    	
        poscV.clear();

        String lineType="";
        
        if(type=='D' && linesV.size()>0){
        	String particle=linesV.elementAt(0).substring(8,9).trim();
        	String delayedS=linesV.elementAt(0).substring(7,8).trim();
        	if(delayedS.isEmpty() && particle.length()>0)//for particle decay
        		type=' ';
        }

        for( int k=0; k < linesV.size(); k++){
           
           lineType=linesV.elementAt(k).substring(5,8);
           
           //System.out.println("In AbstractRecrod: line 76: lineType="+lineType);
           
           if( ( lineType.toUpperCase().equals(" C"+type) || lineType.toUpperCase().equals(" T"+type) )
           //   && ( (String) line.elementAt(k) ).substring(9).trim().length() > 0  //removed to allow to print a blank line, 11/8/2017
           )
           {          
        	   poscV.add( new Integer(k) );    
           }
        }

        for(int k=0; k< poscV.size(); k++){
          int ki= ((Integer) poscV.elementAt(k)).intValue(); 
          int kf=linesV.size()-1; 
          if( k < ( poscV.size()-1 ) ) kf=((Integer) poscV.elementAt(k+1)).intValue() -1;
          Vector<String> cline=new Vector<String>();
          for( int j=ki; j <= kf; j++){ 
        	lineType=linesV.elementAt(j).substring(6,8);
            if(lineType.toUpperCase().equals("C"+type) ){
               cline.addElement( (String) linesV.elementAt(j)); 
            }else if(lineType.toUpperCase().equals("T"+type) ) {
               String temp=linesV.elementAt(j);
               if(lineType.charAt(0)=='T')
            	   temp=temp.substring(0,6)+"C"+temp.substring(7);
               else
               	   temp=temp.substring(0,6)+"c"+temp.substring(7);
 
               cline.addElement(temp);
            }
          }
          Comment c=new Comment();
          c.setValues(cline);
          commentV.addElement(c);
          
          //System.out.println(" k="+k+" body="+c.body());
        }
    }
    
	public Vector<String> linesV(){return linesV;}	

	
	public class AtomicData extends Record{
		String name="";
		
		String ES="", DES="";
		String IS="",DIS="";
		
		float pos=-1;
		
		double e=0,de=0;
		double ia=0,dia=0;//intensity of atomic radiation, x ray or auger electron
		
	    String flag_s="";
	    String q_s="";
	    		
		public String name() {return name;}
		
		public String ES() {return ES;}
		public String DES() {return DES;}
		public String RIS() {return IS;}
		public String DRIS() {return DIS;}
		public double e() {return e;}
		public double de() {return de;}


		//for ordering X rays and Auger electrons
		//(shell-K)+1: eg, 1 for K, 2 for L, ...
		//atomic name:
		//for X rays:
		//        TOT : total             POS=0
		//        KTOT: K shell, total    POS=1*400+0=400
		//        KM  : K shell, M        POS=1*400+3*20=460
		//        KM2 : K shell, M2       POS=1*400+3*20+2=462
		//for Auger electrons:
		//        TOT : total             POS=0
		//        KTOT: K shell, total    POS=1*400+0=400
		//        KLL : K shell, L to L   POS=1*400+2*20+1=441
		//        KLX:  K shell, L to X   POS=1*400+2*20+13=453
		
		public void parsePos(String name) {
			if(name==null || name.trim().isEmpty()) {
				pos=-1;
				return;
			}
			
			float[] factor= {400,20,1};
			
			name=name.trim().toUpperCase();
			if(name.startsWith("TOT"))
				pos=0;
			else {
				pos=0;
				
		        if (name.contains("CK")) {
		            int n=name.indexOf("CK-");
		            if (n<0)
		              n=name.indexOf("CK "); 
		            
		            if (n>=0)
		              name=name.substring(n+3).trim(); 	        
		        }
		        
				for(int i=0;i<name.length();i++) {
					char c=name.charAt(i);
					if(name.substring(i).startsWith("TOT") || i>=3)
						return;
					
					if(Character.isDigit(c)) {
						pos+=factor[i]*Integer.parseInt(c+"");
					}else if(Character.isLetter(c)){
						pos+=factor[i]*((int)(c-'K')+1);
					}else if(c=='-' || c==' ') {
						continue;
					}else {
						pos=10000;
						return;
					}
				}
			}
			
			return;
		}
		
		public float EF() {return pos;}

	    public String flag(){ return flag_s;}
	    public String q() {return q_s;}

		@Override
		public float DEF() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String coinS() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addFlag(String s) {
			// TODO Auto-generated method stub
			
		}
		
	}
}


