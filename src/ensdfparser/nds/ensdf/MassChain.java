/*
 * MassChain.java
 *
 */

package ensdfparser.nds.ensdf;

import java.io.*;
import java.util.*;

import ensdfparser.ensdf.*;
import ensdfparser.nds.util.Str;

/**
 * Encapsulate an entire mass chain (or mass chains).
 *contains the lines in the mass chains, and ENSDF object for each dataset, and the keynumbers in the mass chain
 */
public class MassChain {

	
    /** text of ensdf file */
    protected Vector<String> text;
    /**
     * A mass chain is split into blocks separated by blank lines.
     * Contains vectors of strings
     */

	@SuppressWarnings({ "rawtypes" })
	protected Vector<Vector> blocks;
    /**
     * the subset of the blocks that contain individual nuclei
     */

	
	protected Vector<String> blockTypes;//COMMENT,REFERENCE,ENSDF,OTHERS
	
	@SuppressWarnings({ "rawtypes" })
	protected Vector<Vector> eblocks;
	
	
	protected Vector<Vector<DocumentRecords>> documentsVV;//store all document records for each data block (data set)
    /**
     * Array of ENSDF objects that contain information for individual
     * nuclei.
     */
    protected Vector<EnsdfTableData> etdV;
    protected Vector<ENSDF> ensV;
    
    protected int[] blockIndexArrayOfETDs;//store block index of each etd 
    protected int[] etdIndexArrayOfBlocks;//store etd index of each block, =-1 if block is not an ENSDF dataset, eg, comment, reference 
    
    //array of booleans showing which ensdfs have band drawings included
    protected int [] nBands;
    /// Percent done loading for gui purposes
    protected float done;
    
    //text of abstract
    protected String abst="";
    
    protected EnsdfReferences ref;
    
    protected int A;
    protected int nLines=0;
    
    protected Vector<String> ASV;//vector of A strings, for a file of single mass chain, it should have only one element
    
    protected boolean loaded;
    
    protected boolean printDocumentRecord=false;
    protected String documentLabel="";
    
    protected boolean silent=false;
    
    HashMap<String,String> masses;//<A+name,mass+" "+dmass> e.g., <"40CA","39962590.863 0.022">
    
    /** Creates a new instance of MassChain */
    public MassChain() {
        done=0;
        loaded=false;
        
        printDocumentRecord=false;
        
        String massFileName="AME2020.dat";
        try {

			loadMassTable(massFileName);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(!silent)
				System.out.println("Warning: mass file: "+massFileName+" does not exist! will use mass number as mass.");
		}
    }
    
    public MassChain(boolean printDocumentRecord){
    	this();
    	this.printDocumentRecord=printDocumentRecord;
    }
    
    public void loadMassTable(String filename) throws IOException{
        masses=new HashMap<String,String>();

        try {
            //don't work with web app, using getResourceAsStream() instead
        	/*
            //URL url = ClassLoader.getSystemResource(filename);
            URL url=this.getClass().getClassLoader().getResource(filename);
            
            if(url==null){
            
                System.out.println("Warning: mass table file: "+filename+" not found. will use A as mass.");
                return;
            }
        
            InputStream is=url.openStream();
            */
        	
        	InputStream is=MassChain.class.getClassLoader().getResourceAsStream(filename); 
            if(is==null){
                if(!silent)
                	System.out.println("Warning: mass table file: "+filename+" not found. will use A as mass.");
                
                return;
            }
            BufferedReader br= new BufferedReader(new InputStreamReader(is));
        
            //File f=new File(filename);        
            //BufferedReader br = new BufferedReader(new FileReader(f));

            
            String s;
            @SuppressWarnings("unused")
    		int count=0;
            String key="",mass="";
            String[] items;
            s=br.readLine();//skip the first headline
            while(true){
                s = br.readLine();//line: N Z A Elt. Mass dMass
                if (s==null) break;
                s=s.trim();
                items=s.split("\\s+");
                key="";
                mass="";

                count++;
                try{
                    for(int i=0;i<items.length;i++){
                        //debug
                        //if(count<10) System.out.println("In MassChain: loadMassTable: count="+count+" i="+i+" item="+items[i]);
                        
                    	if(i==2 || i==3)//A and element name
                    		key+=items[i].trim();
                    	else if(i==4 || i==5){//mass, dmass
                    		mass+=items[i].trim();
                    		if(i==4)
                    			mass+="  ";
                    	}
                    	else
                    		continue;
                    }                
                }catch(IndexOutOfBoundsException e){
                	if(!silent)
                		System.out.println("Warning: mass table file should have at least 6 columns: N Z A Elt. Mass dMass! will use A as mass.");
                	
                	br.close();
                	return;
                }catch(NumberFormatException e){
                	if(!silent)
                		System.out.println("Error: mass table file could have wrong format! will use A as mass.");
                	
                	br.close();
                	return;
                }
                
                //debug
                //if(count<100) 
                //	System.out.println("In MassChain: loadMassTable: count="+count+" key="+key+" mass="+mass);
                
                if(key.length()>0 && mass.length()>0){
                	masses.put(key.toUpperCase(), mass);
                }
            }

            br.close();
        }catch(Exception e) {
        	
        }

    }
    
    public SDS2XDX findMass(int A,String name){
    	SDS2XDX result=new SDS2XDX();
    	XDX2SDS temp=new XDX2SDS();
    	boolean found=true;
    	String key;
    	String[] values;
    	String val_str="",unc_str="";
    	float val,unc;

    	try{
        	key=A+name.trim();
        	
        	//System.out.println(" "+(masses==null)+" key="+key+" mass size="+masses.size());
        	
        	String s=masses.get(key.toUpperCase()).trim();
        	
            values=s.split("\\s+");
            
            //debug
            //System.out.println("In MassChain: findMass: A="+A+" name="+name+" mass="+masses.get(key)+" v="+values[0]+" u="+values[1]);
            
            val_str=values[0].trim();
            unc_str=values[1].trim();
    	}catch(Exception e){
    		//e.printStackTrace();
            val_str="";
            unc_str="";
            found=false;
    	}
    	
    	if(val_str.length()>0){
    		try{
        		val=Float.parseFloat(val_str.replace("#","").trim());
        		if(unc_str.length()==0)
        			unc=0f;
        		else
        			unc=Float.parseFloat(unc_str.replace("#","").trim());
       		                
                val=val/1000000;
                unc=unc/1000000;
                
                //debug
                //System.out.println("In MassChain: findMass: A="+A+" name="+name+" val="+val+" unc="+unc+" val_str="+val_str+" unc_str="+unc_str);

                
        		temp.setValues(val, unc);
        		
                //debug
                //System.out.println("In MassChain: findMass: A="+A+" name="+name+" temp.s="+temp.s()+" temp.ds="+temp.ds());
                
        		result.setValues(temp);
        		
        		if(Math.abs(1-Float.parseFloat(result.s())/A)>0.1){
        			System.out.println("Error in findMass(): something is wrong, probably overflow.");
        			found=false;
        		}
        		
        		if(val_str.contains("#")) result.setS(temp.s()+"#");
        		if(unc_str.length()>0 && unc_str.contains("#")) result.setDS(temp.ds()+"#");
        		
                //debug
                //System.out.println("In MassChain: findMass: A="+A+" name="+name+" result.s="+result.s()+" result.ds="+result.ds());
    
        		
    		}catch(Exception e){
    			found=false;
    		}
    	}  
    	
    	if(!found){
    		if(!silent)
    			System.out.println("Warning: can't find mass for "+A+name+". will use A as mass.");
    		temp.setValues(A+0f,0f);
    		result.setValues(temp.S(), temp.DS());
    	}
    	
    	return result;
    }
    /** 
     * Load an entire mass chain from file.  Split into individual
     * nucleii which can be handled by ENSDF objects and comments
     * and reference sections.
     */
    public void load(File f) throws Exception{
    	load(f,"");
    }
    
    /** 
     * Load an entire mass chain from file.  Split into individual
     * nucleii which can be handled by ENSDF objects and comments
     * and reference sections.
     */
    public void load(File f,String option) throws Exception{
    	BufferedReader br;
    	try{
            br = new BufferedReader(new FileReader(f));
    	}catch(Exception e){
    		throw new IOException("Error: file does not exist!\n");
    	}

        
        
        String s;
        
        text = new Vector<String>();
        while(true){
            s = br.readLine();
            if (s==null) break;
            
            text.add(s);
        }

        nLines=text.size();
        
        //System.out.println("#######"+nLines+" "+f.getAbsolutePath());

        parse_sections(option);
                
        br.close();
        loaded=true;
    }
    
    
    /** 
     * Load dataset(s) from text.  Split into individual
     * nuclei which can be handled by ENSDF objects and comments
     * and reference sections. It is used when processing selected
     * dataset(s) from a mass chain
     */
    public void load(Vector<String> lines,String option) throws Exception{
    	if(text!=null) 
    		text.clear();
    	else
    		text = new Vector<String>();
    	
    	text.addAll(lines);
    	
        nLines=text.size();
        
        parse_sections(option);
        loaded=true;
    }
    
    public void load(Vector<String> lines) throws Exception{
    	load(lines,"");
    }
    
    public void load(ArrayList<String> lines,String option) throws Exception{
    	if(text!=null) 
    		text.clear();
    	else
    		text = new Vector<String>();
    	
    	text.addAll(lines);
    	    	
        nLines=text.size();
        
        parse_sections(option);
        loaded=true;
    }
    
    public void load(ArrayList<String> lines) throws Exception{
    	load(lines,"");
    }
    
    /** 
     * Parse text into sections and instantiate objects with only 
     * the basic information (no parsing records (levels/gammas/decays)
     */
    protected void parse_sections_header()throws Exception{
    	parse_sections("HEADER");
    }
    
    
	protected void parse_sections()throws Exception{
		parse_sections("");
	}
    /** 
     * Parse text into sections and instantiate objects that deal with 
     * these sections.
     * option: "HEADER": parse header information (1st line of a dataset) only
     *         "LITTLE": parse header and some basic information, see in ENSDF.java: setValues()
     *         ""     :  parse all information (default)
     * @throws Exception 
     */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void parse_sections(String option)throws Exception{
        String line;
        Vector<String> chunk = new Vector<String>();
        
        blocks = new Vector<Vector>();
        eblocks= new Vector<Vector>();
        documentsVV=new Vector<Vector<DocumentRecords>>();
        
        blockTypes=new Vector<String>();
        ASV=new Vector<String>();
        
        //remove blank lines at beginning
        line=(String)text.get(0);
        while(line.trim().length()==0){
        	text.remove(0);
        	line=(String)text.get(0);
        }
        
        //remove blank lines at the end
        line=(String)text.lastElement();
        while(line.trim().length()==0){
        	text.remove(text.size()-1);//DO NOT DO remove(line), because it will also remove blank lines in the middle
        	line=(String)text.lastElement();
        }
       
        
        ref=new EnsdfReferences();
        ref.loadENSDFLines(text);

        //now the first line is definitely not blank line
        for (int i=0; i<text.size(); i++){
            line = (String)text.get(i);
            
            //if(line.contains("7.81E-")) System.out.println(line+" i="+i);
            
            if(line.trim().length()>0)
            	chunk.add(line);
            else{            // look for blank lines
                if(chunk.size()>0){     
                	if(!blocks.contains(chunk))
                		blocks.add(chunk);
                	
                    chunk = new Vector<String>();
                }
            }
        }
        
        text.removeAllElements();
        
        //if(chunk.lastElement().contains("7.81E-")) System.out.println(chunk.lastElement()+" "+blocks.indexOf(chunk));
        
        if (chunk.size()>0 && !blocks.contains(chunk))
            blocks.add(chunk);
        
        
        // run through blocks and classify by type
        etdV = new Vector<EnsdfTableData>();
        ensV = new Vector<ENSDF>();
        
        ENSDF en;
        EnsdfTableData etd;
        setDone(0);
        int numEns=0;
        
        chunk = new Vector<String>();
        
        blockIndexArrayOfETDs=new int[blocks.size()];
        etdIndexArrayOfBlocks=new int[blocks.size()];
        //initialize blockTypes
        for (int i=0; i<blocks.size(); i++){
        	blockTypes.add("OTHER");
        	blockIndexArrayOfETDs[i]=-1;
        	etdIndexArrayOfBlocks[i]=-1;
        }
        
        
      //convert document record in text to regular comment for printing; for web-display only, 11/9/2017
        if(printDocumentRecord){
        	//convertDocumentRecord();//print all document records as regular comments
        	parseAndconvertDocumentRecords();//parse and convert document records in blocks
        }
        
        
        String pre_id="";
        for (int i=0; i<blocks.size(); i++){
            setDone((float)i/blocks.size()*100.0f);
            
            // get info from first line
            String line1 = (String)((Vector<?>)blocks.get(i)).get(0);  
            
            
            //System.out.println(" i="+i+" "+line1);
            
            // if two letter element code is there it's a data block
            String AS=line1.substring(0,3).trim();
            String id = line1.substring(3,5).trim();
            float mass=0;
            int AA;

            if(!ASV.contains(AS)){
            	ASV.add(AS);
            }
            	
            
            String name=id.toUpperCase();
            
            //debug
            //System.out.println("In MassChina: line1="+line1);
            //System.out.println("              id="+id+" preid="+pre_id+id.equals(pre_id));
            if(id.length()==0){
            	if(line1.contains("COMMENTS"))
            		blockTypes.set(i,"COMMENT");
            	else if(line1.contains("REFERENCE"))
            		blockTypes.set(i,"REFERENCE");
            	else
            		blockTypes.set(i,"OTHER");
            	
            	continue;
            }
            else if (id.length()>0){
            	if(line1.contains("COMMENTS")){
            		blockTypes.set(i,"COMMENT");
            		continue;
            	}
            	else if(line1.contains("REFERENCE")){
            		blockTypes.set(i,"REFERENCE");
            		continue;
            	}

            	Nucleus nucleus=new Nucleus(AS+id);
            	if(nucleus.A()<0 || nucleus.z()<0){
            		blockTypes.set(i,"UNKNOWN");
            		continue;
            	}
            	
            	//if(line1.contains("COMMENTS")) //skip "COMMENTS" block for individual nucleus
            	//	continue;                  //NDS-PUB prints it out with cover-page style
            	                               //JAVA-NDS prints it out as plain text in a separate dataset
            	
            	if(pre_id=="")//at beginning, add an empty chunk, fill later
            		eblocks.add(chunk);           	
            	else if(!id.equals(pre_id)){
            		chunk = new Vector<String>();
            		eblocks.add(chunk); 
            	}
            		
            	
            	//System.out.println(id+"   "+pre_id+" i="+i+" "+line1);
            	
            	if(!chunk.isEmpty()) 
            		chunk.add("\n");
            	
            	chunk.addAll(blocks.get(i));
            	           	                               
                en = new ENSDF();
                en.setSilent(silent);
                
                en.setValues((Vector<String>)blocks.get(i),option);
                AA=en.nucleus().A();

                blockTypes.set(i,"ENSDF");
                
                
                SDS2XDX msx=findMass(AA,name);
                mass=msx.xf();
                
                if(mass==0)
                	mass=AA;
                en.nucleus().setM(mass);
                en.nucleus().setMS(msx.s(), msx.ds());
                
                //debug
                //System.out.println("In MassChain: AA="+AA+" name="+ name+" mass="+mass+" "+findMass(AA,name).dxf());
                
                for(int j=0;j<en.parentsV().size();j++){
                	Nucleus n=en.parentAt(j).nucleus();
                	AA=n.a();
                	name=n.EN();
                	mass=findMass(AA,name).xf();
                	
                    //debug
                    //System.out.println("In MassChain: parent "+name+" mass="+mass+" "+findMass(AA,name).dxf());
                    
                	n.setM(mass);
                }
//                if(line1.indexOf("ADOPTED")==9){ en.setValues((Vector)block.get(i)); }
//                else en.setLittleValues((Vector)block.get(i));
                etd=new EnsdfTableData(en);
                etd.setNID(numEns);
                etd.setLinkID(name+numEns);
                etdV.add(etd);
                ensV.add(en);
                
                blockIndexArrayOfETDs[numEns]=i;
                etdIndexArrayOfBlocks[i]=numEns;
                
                pre_id=id;
                numEns++;
                //if (numEns%10==0){
                //    System.gc();
                //}
            }
        }
        
        
        if(blocks.size()>0){
        	line=(String)((Vector<?>)blocks.get(0)).elementAt(0);
        	A=Integer.parseInt(line.substring(0,3).trim());
        }
        
        //debug
        //System.out.println("In MassChain: eblock.size "+eblock.size());
        
        nBands=new int[numEns];
        setDone(100);
    }
	
	
    /// set percent loaded
    protected synchronized void setDone(float d){ done = d; }
    /// get percent loaded
    public synchronized float getDone(){ return done; }
    
    public int getA(){return A;}
    
    public int nENSDF(){
        //return eblock.size();
    	if(etdV==null)
    		return 0;
    	
        return etdV.size();
    }
    
    public int nNucleus(){
    	if(eblocks!=null)
    		return eblocks.size();
    	
    	String prevNUCID="";
    	int n=0;
    	for(int i=0;i<ensV.size();i++) {
    		String NUCID=ensV.get(i).nucleus().nameENSDF();
    		if(!Str.isNumeric(NUCID)) {
    		    if(NUCID.equals(prevNUCID)) 
    		    	n++;
    		
    		    prevNUCID=NUCID;
    		}
    	}
    	
    	return n;
    }
    
    public ENSDF getENSDF(int i){
        /*ENSDF en = new ENSDF();
        en.setValues((Vector)eblock.get(i));
        return en;*/
    	if(ensV.size()==0 || i<0 || i>=ensV.size())
    		return null;
    	
        return ensV.get(i);
    }
    
    public Vector<ENSDF> ensV(){return ensV;}
    
    public EnsdfTableData getETD(int i){
    	if(etdV.size()==0)
    		return null;
    	
        return(EnsdfTableData)etdV.get(i);
    }
    
    public EnsdfTableData getETDByDSID(String DSID){
    	if(etdV.size()==0)
    		return null;
    	
    	for(int i=0;i<etdV.size();i++){
    		EnsdfTableData etd=etdV.get(i);
    		if(etd.getENSDF().fullDSId().equals(DSID))
    			return etd;
    	}
    	
        return null;
    }

    public EnsdfTableData getETDByBlockAt(int b){
    	if(etdV.size()==0 || b<0 || b>=blocks.size())
    		return null;
    	
    	int etdIndex=etdIndexArrayOfBlocks[b];
    	if(etdIndex<0 || etdIndex>=etdV.size())
    		return null;
    	
    	return etdV.get(etdIndex);
    }
    
    @SuppressWarnings("unchecked")
	public Vector<String> getBlockByETDAt(int e){
    	if(blocks.size()==0 || e<0 || e>=etdV.size())
    		return null;
    	
    	int blockIndex=blockIndexArrayOfETDs[e];
    	if(blockIndex<0 || blockIndex>=blocks.size())
    		return null;
    	
    	return blocks.get(blockIndex);
    }
    
    public int getIndex(EnsdfTableData etd){
    	if(etd==null || etdV.size()==0)
    		return -1;
    	
    	return etdV.indexOf(etd);
    }

    
    public Vector<String> getNucleusNames(){
    	Vector<String> names=new Vector<String>();
    	String line1="";
    	String id="";
    	for(int i=0;i<eblocks.size();i++)
    		line1 = (String)((Vector<?>)eblocks.get(i)).get(0);
    	    id = line1.substring(3,5).trim();
    		names.add(id);   	
    	return names;
    }
    public void removeEnsdfAt(int i){
        if(etdV.size()>i) etdV.removeElementAt(i);
    }
    @SuppressWarnings({ "unchecked" })
	public Vector<String> getComBlock(){
    	try {
            //returns the comments at the top of a mass chain
        	Vector<String> comBlock=(Vector<String>)blocks.elementAt(0);
        	String line=comBlock.elementAt(0);
        	if(line.contains("COMMENTS"))
        		return comBlock;
        	else
        		return new Vector<String>();
    	}catch(Exception e) {
    		
    	}

    	return null;
    }
    
    @SuppressWarnings("unchecked")
	public Vector<String> getBlockAt(int i){
    	if(blocks!=null)
    		return (Vector<String>)blocks.get(i);
    	
    	return new Vector<String>();
    }
    
    public int nBlocks(){
    	if(blocks!=null)
    		return blocks.size();
    	
    	return 0;
    }
    public Vector<String> linesV(){return text;}
    
    public int nLines(){
    	return nLines;
    }
    
    public String getBlockTypeAt(int i){
        if(blockTypes!=null && i<blockTypes.size())
        	return blockTypes.get(i);
        else
        	return "";
    }
    
    public int nChains(){
    	return ASV.size();
    }
    
    
    public Vector<String> getAbstract(){
        return getComBlock();	
    }
    
    public void clear(){
        if(text!=null) text.clear();
        if(etdV!=null)etdV.clear();
        if(ensV!=null) ensV.clear();
        if(blocks!=null) blocks.clear();
        if(eblocks!=null) eblocks.clear();
        loaded=false;
    }
    public EnsdfReferences ref(){return ref;}
    
    public boolean isEmpty(){
    	if(blocks!=null && blocks.size()>0)
    		return false;  		
    	
    	//if(ensdf!=null && ensdf.size()>0)
    	//	return false;
    	
    	return true;
    }
    
    public boolean isLoaded(){return loaded;}
    
    
    public Vector<DocumentRecords> getDocumentRecordsV(int i){
    	if(i>=0 && i<documentsVV.size())
    		return documentsVV.get(i);
    	
    	return null;
    }
    
    public int nDocumentRecordsV(){
    	return documentsVV.size();
    }
    public void setPrintDocumentRecord(boolean b){
    	printDocumentRecord=b;
    }
    
    public boolean isPrintDocumentRecord(){
    	return printDocumentRecord;
    }
    
    public String getDocumentLabel(){return documentLabel;}
    
    //add a comment with content="additional information" where there are document records
    @SuppressWarnings("unchecked")
	public void parseAndconvertDocumentRecords(){
    	documentsVV.clear();
    	Vector<DocumentRecords> documentsV;
    	Vector<String> chunk;
    	
    	documentLabel="Additional information";
    	
    	for(int m=0;m<blocks.size();m++){
    		Vector<String> lines=(Vector<String>)blocks.get(m);   		
        	String currLine="",nextLine="";
        	documentsV=new Vector<DocumentRecords>();
        	
        	chunk=new Vector<String>();
         	
        	boolean beforeFirstEntry=true,forNewEntry=false,isSortGammas=false;
        	String recordTypes="LBEAGD";//D for delayed particle
        	float eg=-1;
        	int PN=-1;
        	
        	for(int i=0;i<lines.size();i++){
        		currLine=lines.get(i);
        		nextLine="";
        		if(currLine.length()<10 || currLine.toUpperCase().charAt(6)!='D'){
        			String type="";
        			if(currLine.length()>=9){
        				type=currLine.substring(5,9).trim();
        				if(type.length()==2 && currLine.indexOf(type)==7 && type.charAt(0)=='D')//for delayed-particle record
        					type="D";
        				
        			}
        			
        			int p=recordTypes.indexOf(type);
        			if(beforeFirstEntry){
        				if(type.length()==1 && p>=0)
        					beforeFirstEntry=false;
        				
        				
        				//get PN option at col 78 for gamma energy ordering
        				if(PN<0 && currLine.length()>78 && currLine.substring(6,8).equals("PN")){
        					try{
        						PN=Integer.parseInt(currLine.substring(77,78));
        					}catch (NumberFormatException e){
        						PN=-1;
        					}
        					
        			        if(PN==6 || PN==7)//intensity values are branching or percentage from each level, not sort gamma (sort by level)
        			        	isSortGammas=false;
        			        else if(PN<0 && lines.get(0).contains("ADOPTED LEVELS"))//if PN is not given, force sorting gammas by levels in Adopted dataset
        			        	isSortGammas=false;
        			        else
        			        	isSortGammas=true;
        				}
        			}
        			
        			if(!beforeFirstEntry && p>=0){
        				forNewEntry=true;
        				
        				//get gamma energy only when gamma sorting is needed       				
        				if(type.equals("G") && isSortGammas){
        					try{
        						eg=Float.parseFloat(currLine.substring(9,19));
        					}catch (NumberFormatException e){
        						eg=-1.0f;
        					}
        				}else
        					eg=-1.0f;
        			}
        			
        			
        			continue;
        		}
        		else if(currLine.substring(10).replaceAll("$", "").trim().length()==0){//empty document line
        			continue;
        		}
        		
        		
        		if(i<lines.size()-1){
        			nextLine=lines.get(i+1);

        		}
        		
        		//System.out.println("In MassChain line 797: docu#="+documentsV.size()+"  currline="+currLine+"  oldline="+lines.get(i)+" "+lines.get(i-1)+" nextLine="+nextLine); 
        		//System.out.println(" forNewEntry="+forNewEntry);
        		
        		//empty document lines and will be removed later
        		lines.setElementAt("",i);
        		
        		//System.out.println("In MassChain line 802: currline="+currLine+"  newline="+lines.get(i)+" "+lines.get(i-1));
        		
        		chunk.add(currLine);       				
                if(nextLine.length()<10 || nextLine.toUpperCase().charAt(6)!='D' || nextLine.charAt(7)!=currLine.charAt(7) || nextLine.charAt(5)==' '){
                	
                	DocumentRecords dr=new DocumentRecords(chunk,i,beforeFirstEntry);
                	
                	dr.e=eg;
                	
                	if(documentsV.size()==0 || forNewEntry){
                		documentsV.addElement(dr);
                		//System.out.println("   ############ add document#"+documentsV.size());
                	}else if(!beforeFirstEntry){//not new entry
                		documentsV.lastElement().docV.add(chunk);
                	}else{
                		boolean found=false;
                    	for(int j=documentsV.size()-1;j>=0;j--){
                    		DocumentRecords tempDR=documentsV.get(j);
                
                    		//System.out.println("In MassChain line 818: docu#="+j+" tempDR="+tempDR.docuType+"  line="+tempDR.docV.lastElement().lastElement());
                    		//System.out.println("                                       dr="+dr.docuType+" line="+dr.docV.lastElement().lastElement()); 
                    		
                        	//for(int n=0;n<documentsV.get(j).docV.size();n++)
                        	//	for(int k=0;k<documentsV.get(j).docV.get(n).size();k++){
                        	//		System.out.println("         %%%%%%%%%%%:   j="+j+" "+documentsV.get(j).docV.get(n).get(k)+" type="+documentsV.get(j).docuType);
                        	//	}
                    		
                    		if(tempDR.docuType.equals(dr.docuType)){
                    			if(dr.docuType.charAt(0)=='A' || dr.docuType.charAt(2)!='H')//footnotes ([0]='B' and [2]='H') are different if headers are different, 
                    				found=true;                                             //'H' means it has line head, 'B' means before fist entry
                    		}else if(dr.docuType.substring(0,2).equals(tempDR.docuType.substring(0,2))){
                    			if(dr.docuType.charAt(0)=='A' || dr.docuType.charAt(1)=='P' || dr.docuType.charAt(1)=='Q')// [0]=A for document records after the first data entry
                    				found=true;                    		    	                  		    
                    		}                   		
                    		
                    		if(found){
                    			tempDR.docV.add(chunk);
                    			found=true;
                    			break;
                    		}
                    	}
                    	if(!found)
                    		documentsV.add(dr);
                	}
                	
                	forNewEntry=false;
                	
        			chunk=new Vector<String>();

        		}
        	}//end processing lines of current block
        	
        	
        	//debug
        	//for(int i=0;i<documentsV.size();i++){
        	//	for(int j=0;j<documentsV.get(i).docV.size();j++)
        	//		for(int k=0;k<documentsV.get(i).docV.get(j).size();k++)
        	//		System.out.println("In MassChain line 845:   i="+i+" "+documentsV.get(i).docV.get(j).get(k));
        	//}
        	
        	
        	//sort documentsV
        	sortDocumentRecords(documentsV);
        	
        	//debug
        	//for(int i=0;i<documentsV.size();i++){
        	//	for(int j=0;j<documentsV.get(i).docV.size();j++)
        	//		for(int k=0;k<documentsV.get(i).docV.get(j).size();k++)
        	//		System.out.println("In MassChain line 856:   i="+i+" "+documentsV.get(i).docV.get(j).get(k));
        	//}
        	
        	//insert a comment line with text="additional information #" and remove empty lines inserted previously
       	
    		String href="";
    		if(documentsV.size()>0){
        		href=EnsdfUtil.makeDocumentFilename(lines)+".pdf";
        		href=EnsdfUtil.makePDFLink(href);
    		}

    		
        	int count=1;
        	for(int i=0;i<documentsV.size();i++){
        		DocumentRecords dr=documentsV.get(i);
        		String firstLine="";
        		String newLine="";
        		if(dr.docV.size()>0 && dr.docV.get(0).size()>0)
        			firstLine=dr.docV.get(0).get(0);
        		
        		if(firstLine.trim().length()==0)
        			continue;
        			
        		String head=EnsdfUtil.getCommentLineHead(firstLine);
        		if(head.length()>0 && dr.docuType.charAt(0)=='B')
        			head+="$";
        		else
        			head="";
        		

        		///////////////////////////////////////////////////////////////////////////////////////////////////////
        		String label="",marks="";
        		label=this.documentLabel;
        		        		
        		//used to mark this special comment and will be replaced later
                //use repeated pattern for easy identification and replacement later
                //must be the same length, same spacing as label
        		marks="@B@"+m+"@0@@@@";//mark for "Additional", No=0 for 1st word
        		marks+=" ";
        		marks+="@B@"+m+"@1@@@@@";//mark for "information", No=1 for 2nd word
        		
        		//////////////////////////////////////////////////////////////////////////////////////////////////////

        		marks=marks+count;
        		
        		dr.label=label;
                dr.marks=marks;
        		dr.href=href;
        		
        		
        		/*
                href=href.replace("_", "\\_");//see latex_dict.dat
        		if(href.length()>0)
        			label="\\href[pdfnewwindow]{"+href+"}{"+label+"}";
        		*/
        		
        		
        		newLine=firstLine.substring(0,5)+" c"+firstLine.substring(7,9)+head+marks;
        		
        		lines.setElementAt(newLine,dr.lastLineNo);
        		
        		//System.out.println("In MassChain line 713: count="+count+" newline="+newLine+"  firstline="+firstLine);
        		
        		count++;
        	}
        	
        	int i=0;
        	while(i<lines.size()){
        		if(lines.get(i).trim().length()==0){
        			lines.remove(i);
        			continue;
        		}
        		i++;
        	}
        	
        	documentsVV.add(documentsV);
    		
    	}//end all blocks
    
    }
    
    
    
    
    @SuppressWarnings("unused")
	private void convertDocumentRecord(){
    	Vector<String> out=convertDocumentRecord(text);
    	text.clear();
    	text.addAll(out);
    	out.clear();
    }
    
    //convert and print all document records as regular comments
    public Vector<String> convertDocumentRecord(Vector<String> lines){
    	Vector<String> out=new Vector<String>();
    	String currLine="",nextLine="";
    	for(int i=0;i<lines.size();i++){
    		currLine=lines.get(i);
    		nextLine="";
    		if(currLine.length()<10 || currLine.toUpperCase().charAt(6)!='D'){
    			out.add(currLine);
    			continue;
    		}
    		else if(currLine.substring(10).replaceAll("$", "").trim().length()==0){//empty document line
    			out.add(currLine);
    			continue;
    		}
    		
    		if(i<lines.size()-1)
    			nextLine=lines.get(i+1);
    		
            char c=currLine.charAt(6);
    		if(c=='d')
    			currLine=currLine.substring(0,6)+'c'+currLine.substring(7);
    		else
    			currLine=currLine.substring(0,6)+'C'+currLine.substring(7);
            
    		
            if(nextLine.length()<10 || nextLine.toUpperCase().charAt(6)!='D' || nextLine.charAt(5)==' '){
    			String tempLine=currLine.trim();
    			if(tempLine.endsWith(".")){
    				int p=currLine.indexOf(tempLine);
    				currLine=currLine.substring(0,p)+tempLine.substring(0, tempLine.length()-1);
    			}
    			out.add(currLine);
    			  			
    			currLine=currLine.substring(0,5)+"xc"+currLine.substring(7,9)+"(document comment available online only)";
    			out.add(currLine);
    			
    		}
    		else{
    			out.add(currLine);
    		}
    	}
    	
    	return out;
    }
    
	void sortDocumentRecords(Vector<DocumentRecords> v){
		Vector<DocumentRecords> v1=new Vector<DocumentRecords>();
		Vector<DocumentRecords> v2=new Vector<DocumentRecords>();
		
		v2.addAll(v);
		
		String decayTypes="BEAD";
        for(int i=0;i<v.size();i++){
        	DocumentRecords dr=v.get(i);
        	char t=dr.docuType.charAt(1);
        	if(decayTypes.indexOf(t)>=0 && dr.docuType.charAt(0)=='A'){//decay entries are displayed in opposite order of level entries
        		v1.add(dr);
        		v2.remove(dr);
        	}

        }
        
        v.clear(); 
        v.addAll(v2);
        for(int i=v1.size()-1;i>=0;i--)
        	v.add(v1.get(i));
        
    	
    	
        DocumentRecords[] TEMP=new DocumentRecords[v.size()];
        for(int i=0;i<v.size();i++){
        	TEMP[i]=v.elementAt(i);
        }
        
        v.clear();
        Arrays.sort(TEMP,new DRComparator());         
        for(int i=0;i<TEMP.length;i++)
        	v.addElement(TEMP[i]);

	}
	
    public class DocumentRecords {
    	public Vector<Vector<String>> docV;
    	public String docuType;
    	public String label;
    	public String marks;
    	public String href;
    	public int lastLineNo=-1;
    	public float e=-1.0f;
    	

    	DocumentRecords(){
    		docV=new Vector<Vector<String>>();
    		docuType="   ";//type[0]=B or A (before or after the first data entry)
    		lastLineNo=-1;//line number in the dataset for the last document line
    		label="";//label used for the link name to be displayed in dataset print out, like "addition information#"
    		href="";//link to the separate pdf file displaying document records
    		marks="";
    		e=-1.0f;
    	}
    	
    	DocumentRecords(Vector<String> lines,int lastLineNo,boolean beforeFirstEntry){
    		this();
    		setValues(lines,lastLineNo,beforeFirstEntry);
    	}
    	
    	void setValues(Vector<String> lines,int lastLineNo,boolean beforeFirstEntry){
    		String firstLine="";
    		if(lines.size()<1)
    			return;
    		
    		while(lines.get(0).trim().length()==0)
    			lines.remove(0);
    		
    		firstLine=lines.get(0);
    		
    		if(firstLine.length()<8 || firstLine.substring(7).trim().length()==0)
    			return;
    		
    		String h=EnsdfUtil.getCommentLineHead(firstLine).trim();  			
    		
    		docuType="";
    		
    		if(beforeFirstEntry)
    			docuType+="B";
    		else
    			docuType+='A';
    		
    		if(firstLine.charAt(7)=='N'){
    			if(h.equals("NR") || h.equals("NT"))
    				docuType+='G';
    			else if(h.equals("NB"))
    				docuType+='B';
    			else if(h.equals("NP"))//particle
    				docuType+='D';
    			else
    				docuType+='Q';
    		}
    		else		
    			docuType+=firstLine.charAt(7);//record type  L/G/B/E/A/P/Q or " "
    		
    		if(h.length()>0)
    			docuType+='H';
    		else
    			docuType+=' ';
    		
    		this.lastLineNo=lastLineNo;
    		
    		docV.add(lines);
    			
    	}
    	    	
    }
    
    class DRComparator implements Comparator<Object>{
        public int compare(Object one,Object two){
            DocumentRecords dr1=(DocumentRecords)one;
            DocumentRecords dr2=(DocumentRecords)two;
            char[] t1=dr1.docuType.toCharArray();
            char[] t2=dr2.docuType.toCharArray();
            
            String types="QP LBEADG"; //Q or P, and B/E/A/D, only one in the same dataset
            
            int i1=types.indexOf(t1[1]);
            int i2=types.indexOf(t2[1]);
            
            if(i1<i2)
            	return -1;
            else if(i1>i2)
            	return 1;
            
            //process i1==i2
            //System.out.println("In MassChain line 931: "+dr1.type+"  "+dr2.type+" "+dr1.docV.get(0).get(0)+" "+dr2.docV.get(0).get(0)+" "+getOrder1(t1)+"  "+getOrder1(t2));

            int out=getOrder1(t1)-getOrder1(t2);
            if(out==0 && t1[0]=='A' && t1[1]=='G'){//dr.e is -1 by default, and it is set as EG only when sorting gamma is needed
            	if(dr1.e<dr2.e)
            		return -1;
            	else if(dr1.e>dr2.e)
            		return 1;
            	else
            		return 0;
            }
            	
            return out; 

        }
        
        private int getOrder1(char[] type){
        	if(type[0]=='B' && type[2]==' ')
        		return 0;
        	if(type[0]=='A')
        		return 1;
        	if(type[0]=='B' && type[2]=='H')
        		return 2;
        	
        	return 0;
        }
    }
    
    public void setSilent(boolean b) {silent=b;}
}

