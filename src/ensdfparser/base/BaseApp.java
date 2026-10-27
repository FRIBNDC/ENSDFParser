package ensdfparser.base;

import java.io.File;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import ensdfparser.calc.XDX;
import ensdfparser.ensdf.Decay;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.util.Str;

abstract public class BaseApp {
	protected MassChain data;//all of the data in the file
	
	protected String message="";//stores only warning and error messages or others that need to be captured for output
	protected String report="";//results (and message) that are written into the report file, 
	
	protected String inputFilepath="";
	protected String outputFilepath="";
	protected String reportFilepath="";
			
	protected String inputFilename="";
	protected String outputFilename="";
	protected String reportFilename="";

	
	protected int nBlocks=0;
	protected int nFiles=0;
	protected Vector<String> filePaths=new Vector<String>();

	/*
    protected static int errorLimit=35;
    */
	//probably no linger needed (1/10/2025), since a BaseControl
	//class with errorLimit variable has been created and all individual
	//control class inherit this variable (after removing errorLimit in 
	//each individual control class), change made to errorLimit in 
	//sub control class is actually made to BaseControl.errorLimit
	public void setErrorLimit(int limit) {
		if(limit>0)
			BaseControl.errorLimit=limit;
	}
	
	
	public void load(File[] files) throws Exception {
		try {	        
			Vector<String> lines=new Vector<String>();
			
			for(int i=0;i<files.length;i++){
				lines.addAll(Str.readFile(files[i]));
				lines.add(" ");
				
				filePaths.add(files[i].getAbsolutePath());
			}
			
			load(lines);
			if(files.length==1)
				setInputFilepath(filePaths.get(0));
			else
				setInputFilepath("");
			
			setNFiles(files.length);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error loading ENSDF file");
		}
	}
	
	public void load(File file) throws Exception{
		File[] files= new File[1];
		files[0]=file;
		
		load(files);
	}
	
	
    public void setInputFilepath(String s){
    	inputFilepath=s;
		try {
			File f=new File(s);
			inputFilename=f.getName();
		}catch(Exception e) {		
		}
    }
    
    public String getInputFilepath(){return inputFilepath;}
    
    public void setOutputFilepath(String s){
    	outputFilepath=s;
		try {
			File f=new File(s);
			outputFilename=f.getName();
		}catch(Exception e) {		
		}
    }
    public String getOutputFilepath(){return outputFilepath;}
    
    public void setReportFilepath(String s){
    	reportFilepath=s;
		try {
			File f=new File(s);
			reportFilename=f.getName();
		}catch(Exception e) {		
		}
    }
    public String getReportFilepath(){return reportFilepath;}
    
    public Vector<String> getFilePaths(){return filePaths;}

    public int nBlocks() {return nBlocks;}
    public int nFiles(){return nFiles;}
    
    public String getInputFilename() {return inputFilename;}
    public String getOutputFilename() {return outputFilename;}
    
    
    public void setNFiles(int n){nFiles=n;}
   
    
	public void clearMessage(){message="";}
	public String getMessage(){return message;}
	public void writeMessage(String msg){message+=msg;}
	public void writeMessageln(String msg){message+=msg+"\n";}

 
    public static interface DecayIntensity{
    	SDS2XDX v(Decay d);
    }
   
    
    public void printToReport(String txt) {
        if(!txt.trim().isEmpty())
            report+=txt+"\n";    
        else
            report+=txt;
    }
    
    public String getReport() {return report;}
	public void clearReport(){report="";}
	
	public void writeReportHeader(PrintWriter out) {

    	out.write(title()+"\n");
    	
        Date date=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("E MM/dd/yyyy 'at' hh:mm:ss a zzz");
        String dateS="Generated at: "+sdf.format(date);
        
    	out.write(dateS+"\n\n");
    	
    	out.write(" Input file: "+filePaths.get(0)+"\n");
    	if(nFiles>1) {
        	for(int i=1;i<nFiles;i++)
        		out.write("             "+filePaths.get(i)+"\n");
    	}
    	out.write("Report file: "+reportFilepath+"\n");
    	out.write("Output file: "+outputFilepath+"\n");
		
	}
	
    public void writeReport() {writeReport(reportFilename);}
    
    public void writeReport(String filePath) {
    	if(!filePath.contains(File.separator))
    		filePath=outdir()+File.separator+filePath;
    	
    	//System.out.println(filePath);
    	
    	try {
    		File f=new File(filePath);
        	PrintWriter out=new PrintWriter(f);
        	
        	reportFilepath=f.getAbsolutePath();
        	
        	//System.out.println(" size="+f.length());
        	
        	writeReportHeader(out);
        	
        	out.write(report);
        	
        	out.close();
    	}catch(Exception e) {
    		//e.printStackTrace();
    	}

    }
     
    public void writeOutput(Vector<String> lines){writeOutput(lines,outputFilename);}
    
    public void writeOutput(Vector<String> lines,String filename) {
    	try{
    		if(lines.size()==0) {
    			writeMessage("Nothing to write into output!\n");
    			return;
    		}
    		
     		String filepath=outdir()+File.separator+filename;    
     		
     		EnsdfUtil.saveENSDF(lines, filepath);

    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
           
    public static String printXDX(XDX xdx) {return printXDX(xdx,12);}  
    public static String printXDX(XDX xdx,String alignment) {return printXDX(xdx,12,alignment);}   
    public static String printXDX(XDX xdx,int len) {return printXDX(xdx,len,"RIGHT");}   
    public static String printXDX(XDX xdx,int len,String alignment) {
    	return XDX.printXDX(xdx, len, alignment);	
    } 
    
    public static String printXDX(XDX xdx,int sLen,int dsLen) {return printXDX(xdx,sLen,dsLen,"RIGHT");}
    public static String printXDX(XDX xdx,int sLen,int dsLen,String alignment) {
    	return XDX.printXDX(xdx, sLen, dsLen, alignment);
    }

    public static String printXDX(double x,double dx) {return printXDX(x,dx,12,"RIGHT");}//same as printXDXDefault
    public static String printXDX(double x,double dx,String alignment) {return printXDX(x,dx,12,alignment);}
    public static String printXDX(double x,double dx,int len) {return printXDX(x,dx,dx,len);}
    public static String printXDX(double x,double dx,int len,String alignment) {return printXDX(x,dx,dx,len,alignment);}
    public static String printXDX(double x,double dxu,double dxl,int len) {return printXDX(x,dxu,dxl,len,"RIGHT");}
    public static String printXDX(double x,double dxu,double dxl,int len,String alignment) {return printXDX(x,dxu,dxl,len,alignment,BaseControl.errorLimit);}
    public static String printXDX(double x,double dxu,double dxl,int len,String alignment,int errorLimit) {
    	//System.out.println("###errorlimit="+errorLimit);
    	return XDX.printXDX(x, dxu, dxl, len, len, alignment, errorLimit);
    }
    
    public static String printXDX(double x,double dx,int sLen,int dsLen) {return printXDX(x,dx,sLen,dsLen,"RIGHT");}
    public static String printXDX(double x,double dx,int sLen,int dsLen,String alignment) {return printXDX(x,dx,dx,sLen,dsLen,alignment);}
    public static String printXDX(double x,double dxu,double dxl,int sLen,int dsLen,String alignment) {return printXDX(x,dxu,dxl,sLen,dsLen,alignment,BaseControl.errorLimit);}  
    public static String printXDX(double x,double dxu,double dxl,int sLen,int dsLen,String alignment,int errorLimit) {
    	return XDX.printXDX(x,dxu,dxl,sLen,dsLen,alignment,errorLimit);
    }
    
    public static String printSDS(String s,int len) {return printSDS(s,"",len,0,"RIGHT");}
    public static String printSDS(String s,int len,String alignment) {return printSDS(s,"",len,0,alignment);}
    public static String printSDS(String s,String ds) {return printSDS(s,ds,12);}
    public static String printSDS(String s,String ds,int len) {return printSDS(s,ds,len,"RIGHT");}
    public static String printSDS(String s,String ds,int len,String alignment) {
    	return XDX.printSDS(s,ds,len,alignment);
    }
    
    public static String printSDS(String s,String ds,int sLen,int dsLen) {return printSDS(s,ds,sLen,dsLen,"RIGHT");}
    public static String printSDS(String s,String ds,int sLen,int dsLen,String alignment) {
    	return XDX.printSDS(s, ds, sLen, dsLen, alignment);
    }
    
    //The following use default sLen=10, dsLen=2   
    public static String printXDXDefault(XDX xdx) {return printXDXDefault(xdx,13);} 
    public static String printXDXDefault(XDX xdx,int len) {
    	return XDX.printXDXDefault(xdx,len);
    }  
    
    public static String printXDXDefault(double x,double dx) {return printXDXDefault(x,dx,13);}
    public static String printXDXDefault(double x,double dx,int len) {return printXDXDefault(x,dx,dx,len);}
    public static String printXDXDefault(double x,double dxu,double dxl,int len) {return printXDXDefault(x, dxu, dxl, len, BaseControl.errorLimit);}
    public static String printXDXDefault(double x,double dxu,double dxl,int len, int errorLimit) {
    	return XDX.printXDXDefault(x, dxu, dxl, len, errorLimit);
    }
    
    public static String printSDSDefault(String s,String ds) {	return printSDSDefault(s,ds,13);}   
    public static String printSDSDefault(String s,String ds,int len) { return printSDSDefault(s,ds,len,false);}
    public static String printSDSDefault(String s,String ds,int len,boolean useBracket) {
    	return XDX.printSDSDefault(s, ds, len, useBracket);
    }
    	      
    /*
     * recordType: letter in "LGABE"
     */
    public boolean insertVersionLine(Vector<String> linesV,String versionText,String recordType,String recordName) {
    	try {
    		
    		String versionDate=version().trim();
    		String appName=name().trim();

    		versionText=versionText.trim();   		
    		recordType=recordType.trim().toUpperCase();
    		recordName=recordName.trim().toUpperCase();//can be empty
    		
    		if(linesV.isEmpty() || versionText.isEmpty() ||versionDate.isEmpty()||recordType.isEmpty()||appName.isEmpty())
    			return false;
    		if(recordType.length()>1 || !"LGABE".contains(recordType))
    			return false;
    		
    		versionDate=formattedVersionDate();
    		
        	//insert document record for version of the code 
        	int p=0;
        	String line="";
        	for(int i=0;i<linesV.size();i++) {
        		line=linesV.get(i);
        		String s=line.substring(6,8);//"  " for DSID line, " H" for history line
        		if(s.equals("  ") || s.equals(" H"))
        			continue;
        		
        		p=i;
        		break;
        	}
        	
        	//if(line.length()<10)
        	if(p==linesV.size()-1 || p==0)
        		return false;
        	
        	//find and remove existing version line
        	String prevType="",type="";
        	boolean isVersionLine=false;
        	Vector<String> linesToRemove=new Vector<String>();
        	
        	boolean hasPassedDoc=false;
        	for(int i=p;i<linesV.size();i++) {
        		line=linesV.get(i);
        		String s=line.toUpperCase();//DO NOT TRIM
        		type=s.substring(6,8);//"  " for DSID line, " H" for history line
        		
        		if(s.contains(appName.toUpperCase()) && s.contains("VERSION")) {
        			isVersionLine=true;
        			linesToRemove.add(line);
        		}else if(type.equals(prevType) && s.charAt(5)!=' ' && isVersionLine) {//continuous line of previous line
        			linesToRemove.add(line);
        		}else if(s.substring(5,7).trim().isEmpty() && "LG".contains(s.charAt(7)+"")) {//reaching "  L" or "  G", then done removing
        			break;
        		}else if(!type.equals(prevType) && isVersionLine) {
        			isVersionLine=false;
        		}

        		//put new doc lines right below H lines
        		if(type.charAt(0)!='D'||type.charAt(1)==' ')
        			hasPassedDoc=true;
        		
        		if(!hasPassedDoc) {
            		String types="ABEGL";
            		for(int j=0;j<types.length();j++) {
            			String tempRecType=types.charAt(j)+"";
            			if(tempRecType.equals(recordType))
            				break;
            			
            			if(type.equals("D"+tempRecType)) {
            				p=i+1;
            				break;
            			}
            		}
        		}

        		prevType=type;
        	}
        	        	
        	String NUCID=line.substring(0,5);

    		try {    	
    	    	String text=recordName+"$"+versionText+" by "+appName+" "+versionDate;
    	    	Vector<String> tempV=Str.makeENSDFLines(NUCID," d"+recordType,text);
    	    	linesV.addAll(p,tempV);
    		} catch (Exception e) {
    			line=NUCID+" d"+recordType+" "+recordName+"$"+versionText+" by "+appName+" "+versionDate;
    			linesV.add(p,line);
    		} 
    		
	    	for(String s:linesToRemove)
	    		linesV.remove(s);
	    	
    		return true;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return false;
    }
    
    public String insertVersionLine(String datasetBody,String versionText,String recordType,String recordName) {
    	String s=datasetBody;
    	try {
    		String[] lines=s.split("\n");
    		Vector<String> linesV=new Vector<String>(Arrays.asList(lines));
    		boolean success=insertVersionLine(linesV,versionText,recordType,recordName);
    		if(success) {
    			s="";
    			for(String line:linesV)
    				s+=line+"\n";
    		}
    	}catch(Exception e) {}
    	
    	return s;
    }
    
    public String formattedVersionDate() {
        SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy");
        Date date;
        String versionDateS=version();
        String dateStr="";
		try {
			date = sdf.parse(versionDateS);
	        sdf=new SimpleDateFormat("dd-MMM-yyyy");
	        //System.out.println(sdf.format(date));
	    	dateStr=sdf.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			dateStr=versionDateS;
		}

		//System.out.println(versionDateS+"  "+dateStr);
		return "(version "+dateStr+")";
    }
	     
    public Vector<String> filePaths() {return filePaths;}
   
    public void setReportFilename(String s) {
    	reportFilename=s;
		reportFilepath=outdir()+File.separator+reportFilename;
    }
    public void setOutputFilename(String s) {
    	outputFilename=s;
    	outputFilepath=outdir()+File.separator+outputFilename;
    }
    public void setInputFilename(String s) {
    	inputFilename=s;
    	inputFilepath=filedir()+File.separator+outputFilename;
    }
    
	//abstract functions
    public abstract void load(Vector<String> lines) throws Exception;  
	public abstract String printDeclaration(String indent);
	
    public abstract String version();
    public abstract String title();
    public abstract String name();  
    public abstract String outdir();
    public abstract String filedir();

}
