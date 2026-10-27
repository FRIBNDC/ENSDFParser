package ensdfparser.calc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.ensdf.XDX2SDS;


/*
 * wrap for the BrIccs executable for getting the calculated ICC values
 */
public class BriccsWrap {
	
	private static String briccsHome="";//needed for running briccs
	private static String briccsExecPath="";//executable name including absolute path
	private static String briccsExecName="";//executable name
	
	private static String errorMsg="";//error message if briccs fails
	
	private static int countOfCalcs=0;
	private static float totalTime=0;
	
	public BriccsWrap(){
	}
	
	
	public static String briccsExecName(){return briccsExecName;} 
	public static String briccsExecPath(){return briccsExecPath;} 
	public static String briccsExecHome(){return briccsHome;} 	
	
	public static void setBriccsExecName(String name){briccsExecName=name;}
	public static void setBriccsExecHome(String home){briccsHome=home;}
	public static void setBriccsExecPath(String path){briccsExecPath=path;}
	
	
	public static String dirSeparator(){
        String dirSeparator="/";//"\\" for Windows, "/" for Linux
        if(System.getProperty("os.name").toLowerCase().contains("windows"))
        		dirSeparator="\\";
        return dirSeparator;
	}
	
    public static boolean is64bit() {
        boolean is64bit = false;
        if (System.getProperty("os.name").contains("Windows")) {
            is64bit = (System.getenv("ProgramFiles(x86)") != null);
        } else {
            is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
        }
        
        return is64bit;
    }
    
	/*
	 * copy to dir only if the file doesn't exist. No override
	 */
	public static boolean copy(String filename,String dir){
		return copy(filename,dir,"");
	}
	
	/*
	 * copy file packed in JAR file to local dir
	 */
	public static boolean copy(String filename,String dir,String option){


		try {
	        //don't work with web app, using getResourceAsStream() instead
			/*
			URL url = ClassLoader.getSystemResource(filename);//get the default file in the classpath if it exists
	        //System.out.println(" 0 filename="+filename+" url="+url);
	        
	        if(url==null)//briccs does not exist in the classpath
	        	return false;
	        	
	        InputStream is = url.openStream();
	        */
			
	    	InputStream is=BriccsWrap.class.getClassLoader().getResourceAsStream(filename); 
	        if(is==null)
	        	return false;
	        
	        String op=option.toUpperCase().trim();
	        
	        String filePath=dir+dirSeparator()+filename;
	        File file=new File(filePath);
	        if(file!=null && file.exists() && (op.isEmpty() || op.charAt(0)!='F'))//force to copy only if option="FORCE" or "F" or starts with "F"
	        	return true;
	        
	        //System.out.println(" 1 filename="+filename);

	        OutputStream os=new FileOutputStream(filePath);//sets the output stream to a system folder
	        
	        byte[] b = new byte[2048];
	        int length;
	        
	        while ((length = is.read(b)) != -1) {
	            os.write(b, 0, length);
	        }

	        is.close();
	        os.close();
	        

	        file.setExecutable(true);
	        //if (file.exists()) {
	        //    System.out.println("read="+file.canRead());
	        //    System.out.println("write="+file.canWrite());
	        //    System.out.println("Execute="+file.canExecute());
	        //}
	        //System.out.println(" 2 filename="+filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	
	public static boolean checkInternalBrIccs(String  copyToDir){
		return checkInternalBrIccs(copyToDir,"");
	}
	
	
	/*
	 * check if the BrIccs program exists in the package containing this class and if it works
	 * copyToPath: the local path which the bricss exec file is coped to
	 * option: copy option. force to copy only if option="FORCE" or "F" or starts with "F"
	 */
	public static boolean checkInternalBrIccs(String  copyToDir,String option){
	    
	    errorMsg="";
	    countOfCalcs=0;
	    totalTime=0;
	    
        //find out what operating system is being used, and set the right filename for the script
		String temp=System.getProperty("os.name").toLowerCase();
        
		//System.out.println(" temp="+temp);
        
        if(temp.toLowerCase().contains("windows"))
        	briccsExecName="BrIccS.exe";
        else if(temp.contains("linux"))
        	briccsExecName="briccs";
        else if(temp.contains("mac")) {
            if(is64bit())
                briccsExecName="briccsMac";
            else
                briccsExecName="briccsMac";
        }else {
            errorMsg="No briccs executable for system="+temp;
        	return false;
        }
		
		//System.out.println(" briccsExecName="+briccsExecName);

        File f=new File(copyToDir);
        if(!f.exists())
        	f.mkdir();
        
		briccsExecPath=copyToDir+dirSeparator()+briccsExecName;
		
        f=new File(briccsExecPath);       
        if(!copy(briccsExecName,copyToDir,option)) {
            errorMsg="executable="+briccsExecName+" cannot be copied to "+copyToDir;
        	return false;
        }
        
        if(!copy("BrIccFOV22.icc",copyToDir,option)) {
            errorMsg="BrIccFOV22.icc cannot be copied to "+copyToDir;
        	return false;
        }
        
        if(!copy("BrIccFOV22.idx",copyToDir,option)) {
            errorMsg="BrIccFOV22.idx cannot be copied to "+copyToDir;
        	return false;
        }
		
		//System.out.println(" briccsExec="+briccsExecPath);
        
        //File f=new File(briccsExecPath);
        if(f!=null)
        	briccsHome=f.getParent();
        
       return checkBrIccsPath(briccsExecPath);
	}

	/*
	 * check if the BrIccs program at the specified path exists and works
	 */
	public static boolean checkBrIccsPath(String briccsExecPath){

		//System.out.println(" briccsExecPath="+briccsExecPath);
		
		if(briccsExecPath.trim().length()==0) {
		    errorMsg="No path for briccs has been specified";
        	return false;
		}
        
        File f=new File(briccsExecPath);
        if(f==null || !f.exists()) {
            errorMsg=briccsExecPath+" does not exist.";
        	return false;
        }
       
        //new version (after 04/16/2025) removes -w option
        String[] test=getICCSByCommand(briccsExecPath+" -Z 82 -g 1063 -e 1 -L M1");
        //String[] test=getICCSByCommand(briccsExecPath+" -Z 82 -g 1063 -e 1 -L M1 -w BrIccFO");
        //String[] test=getICCSByCommand(briccsExec+" -Z 44 -g 652.4400024414062 -e 0 -L E2 -d 0.0 -u 0 -w BrIccFO");
        
        //System.out.println(" briccsExec="+briccsExecName+" bricssHome="+briccsHome+" Float.parseFloat(test[0])="+Float.parseFloat(test[0]));

        
        if(Float.parseFloat(test[0])>0)
        	return true;
        
        
        errorMsg ="BrIccs executable="+briccsExecPath+" exists,\n";
        errorMsg+=" but it does not work. It could be that the BrIccs executable:\n";
        
        String msg1="is not permitted to run. Try changing permission manually\n";
        String msg2="malfuntions. Check it with an example in command line.\n";
        
        if(test[1].equals("0")) 
            errorMsg+="     ** "+msg1;
        else if(test[1].contains("ERROR"))
            errorMsg+="     ** "+msg2;
        else {
            errorMsg+="     1) "+msg1;
            errorMsg+="     2) "+msg2;
        }
        
		return false;	
	}
	
	public static String errorMessage() {return errorMsg;}
	
	/*
	 * all uncertainty parameters in ENSDF style
	 * all parameters have to be checked to be valid before calling 
	 */
	public static String[] getICCS(int Z,String es,String des,String mult,String mrs,String dmrs){
		String[] out={"-1","0"};
		
		String command=briccsExecPath;
		
		//debug
		//System.out.println(" command="+command+" Z="+Z+" es="+es+" des="+des+" mult="+mult+" mrs="+mrs+" dmrs="+dmrs);
		
		float ef=-1,mr=-1000;
		try{
			ef=Float.parseFloat(es);
		}catch(NumberFormatException e){	
			return out;
		}
		try{
			mr=Float.parseFloat(mrs);
		}catch(NumberFormatException e){	
			if(!mrs.trim().isEmpty())
				return out;
		}
		
		if(Z<=0 || ef<=0 || mult.trim().isEmpty())
			return out;
		
		String argZ="",argEG="",argMR="",argMult="";
		
		argZ=" -Z "+Z;
		
		if(des.trim().isEmpty() || des.trim().equals("0"))
			argEG=" -g "+String.format("%.3f", ef)+" -e +0-0";
		else
			argEG=" -g "+es+" -e "+des;

		argMult=" -L "+mult;
		
		int p1=mult.indexOf("M")+1;
		int p2=mult.indexOf("E")+1;
		if((p1+p2)==0 || (p1*p2==0&&!mrs.trim().isEmpty()&&mr!=0))
			return out;
					

		if(p1*p2!=0 && !mrs.trim().isEmpty()){
			if(dmrs.trim().isEmpty() || dmrs.trim().equals("0")){
				if(Math.abs(mr)>=1)
					argMR=" -d "+String.format("%8.1f", mr).trim()+" -u +0-0";
				else if(Math.abs(mr)>=0.001)
					argMR=" -d "+String.format("%8.4f", mr)+" -u +0-0";
				else
					argMR=" -d "+String.format("%8.4E", mr)+" -u +0-0";
			}else
				argMR=" -d "+mrs+" -u "+dmrs;
		}

		//new version (after 04/16/2025) removes -w option	
		command+=argZ+argEG+argMult+argMR;
		//command+=argZ+argEG+argMult+argMR+" -w BrIccFO";		
		out=getICCSByCommand(command);
		
		//System.out.println(" command="+command+" out[0]="+out[0]+" dmrs="+dmrs+" "+dmrs.trim().equals("0"));
		
		return out;
	}
	
	public static float[] getICC(int Z,String es,String des,String mult,String mr,String dmr){
		String[] iccs=getICCS(Z,es,des,mult,mr,dmr);
		
		float[] out=new float[2];
		
		String s=iccs[0];
		String ds=iccs[1];
		
		//debug
		//System.out.println(" "+es+" "+des+" "+mult+" "+mr+" "+dmr+" s="+s+" ds="+ds);
		
		SDS2XDX s2x=new SDS2XDX(s,ds);
		
		out[0]=s2x.xf();
		out[1]=s2x.DXf();
		
		//System.out.println(" out s="+out[0]+" ds="+out[1]);
		
		return out;
	}
	
	public static float[] getICC(int Z,double eg,double deg,String mult,double mr,double dmru,double dmrl){
		
		String es="",des="",mrs="",dmrus="",dmrls="",dmrs="";
		
		XDX2SDS xs=new XDX2SDS(eg,deg);
		es=xs.s();
		des=xs.ds();
		
		xs=new XDX2SDS(mr,dmru,dmrl);
		mrs=xs.S();
		dmrus=xs.dsu();
		dmrls=xs.dsl();
		
		dmrs=dmrus;
		if(!dmrus.equals(dmrls))
			dmrs="+"+dmrus+"-"+dmrls;
			
		return getICC(Z,es,des,mult,mrs,dmrs);

	}
	
	public static float[] getICCByCommand(String command){
		String[] iccs=getICCSByCommand(command);
		
		float[] out=new float[2];
		
		String s=iccs[0];
		String ds=iccs[1];
				
		SDS2XDX s2x=new SDS2XDX(s,ds);
		
		out[0]=s2x.xf();
		out[1]=s2x.DXf();
		
		return out;
	}
	
	/*
	 * run briccs program and find cc values from the XML output
	 * XML output example:
	 *   <MixedCC
           Shell="Tot"
           CCmult1="1.521E-02"
           CCmult2="5.915E-03"
           DCC="5">
           0.011     
         </MixedCC>
	 */
	public static String[] getICCSByCommand(String command){
	    
        long startTime,endTime;
        float timeElapsed;//in second
        startTime=System.currentTimeMillis();
        
		String[] out=new String[2];
		out[0]="-1";
		out[1]="0";
		
		String[] envp={"BrIccHome="+briccsHome};
		
		//System.out.println(" command="+command);
		
		Vector<String> msgs=new Vector<String>();
		try{
			msgs=runCommand(command,envp,briccsHome);
		}catch(Exception e){
			return out;
		}
		
		String line="";
		String ccs="",dccs="",shell="";
		String name="",value="";
		
		int nShell=0;
		
		boolean hasReadDCC=false;
		boolean inShell=false;
		
		Vector<String> slines=new Vector<String>();//lines in one shell
		for(int i=0;i<msgs.size();i++){
			
			//System.out.println("  line="+line+" size="+msgs.size()+" command="+command);
			
			if(nShell>2)//"Tot" shell is the first one in the XML output
				break;
			
			line=msgs.get(i);
			if(!inShell){
				if(line.indexOf("<MixedCC")>=0 || line.indexOf("<PureCC")>=0){
					slines.clear();
					shell="";
					ccs="";
					dccs="";
					inShell=true;
					hasReadDCC=false;
				}
				continue;
			}else if(line.indexOf("</MixedCC")>=0 || line.indexOf("</PureCC")>=0){
				inShell=false;
			}else{
				slines.add(line);
				continue;
			}
			

			for(int j=0;j<slines.size();j++){
				line=slines.get(j);
				
				name="";
				value="";
				
				int p=line.indexOf("=");
				if(p>0){
					name=line.substring(0, p).trim();
					value=line.substring(p+1).replace("\"","").replace(">","");
				}				
				
				//System.out.println("name="+name+" value="+value+" line="+line);
				
				if(name.equals("Shell")){
					shell=value;
				}else if(name.equals("DCC")){
					dccs=value;
					hasReadDCC=true;
				}else if(hasReadDCC){//cc line immediately follows dcc line
					ccs=line.trim();
		            hasReadDCC=false;
				}				
			}
			
			nShell++;
			
			//System.out.println("*shell=*"+shell+"* ccs="+ccs+" "+shell.equals("Tot"));
			
			if(shell.equals("Tot") && ccs.length()>0){
				out[0]=ccs;
				out[1]=dccs;	
				
				countOfCalcs++;
				endTime=System.currentTimeMillis();
		        timeElapsed=(float)(endTime-startTime)/1000;
		        totalTime+=timeElapsed;
		        
				break;
			}
		}
		
		//the executable can run but reports error
		if(out[0].equals("-1") && msgs.size()>0) {
		    for(int i=0;i<msgs.size();i++){
		        line=msgs.get(i);
		        if(line.contains("ERROR"))
		            out[1]="PROGRAM ERROR";
		    }
		}
		
		return out;
	}

    public static float getTotalTimeOfCalculations() {return totalTime;}
	public static int getCountOfCalculations() {return countOfCalcs;}
	public static void resetCountOfCalculations() {countOfCalcs=0;totalTime=0;}
	
    public static Vector<String> runCommand(String command,String[] envp,String dir) throws Exception{

    	Vector<String> msgs=new Vector<String>();
    	
    	if(command.trim().isEmpty())
    		return msgs;
    	
    	String os=System.getProperty("os.name").toLowerCase();  	
    	
		Process proc=null;
    	File wd=null;
    	if(dir!=null && !dir.isEmpty())
    		wd=new File(dir);

    	if(os.contains("linux")||os.contains("mac")){     	
    		proc = Runtime.getRuntime().exec(command, envp, wd);    		  
    	}
    	else if(os.contains("windows")){
        	Runtime rt = Runtime.getRuntime();
        	
        	//run cmd command in Java   
        	//
        	//method1: works only when run in the same drive, not work if script in a different drive
        	//
    		//command="cmd /c cd "+nds.Setup.outdir+"&& start "+script+"&exit";
    		//rt.exec(command);


    		/*
        	//
        	//method2: 
        	//
        	command="cmd /c "+script;             //If you do not want to start your process in it's separate console (that's what start does), 
        	                                      //you must wait with p.waitFor() or read it's input stream - otherwise it may silently fail.
    		Process proc=rt.exec(command,         //path to executable
    	            null,                                  // env vars, null means pass parent env
    	            new File(nds.Setup.outdir));

    	    InputStream is=proc.getInputStream();
    	    BufferedReader br= new BufferedReader(new InputStreamReader(is));
 		    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
 		    out.println("exit");
 		   
    	    String line=new String();
    	    while ((line=br.readLine())!=null) {
    	    	//System.out.println (line);
    	    }
    	    br.close();
    	    proc.destroy();
    	    */
    	    
        	//
        	//method2:easiest way
        	//

        	//command="cmd /c start "+command;       //start your process in it's separate console (that's what start does)
        	//command="cmd /c "+command;
    		proc=rt.exec(command,                  //path to executable
    	            envp,                          // env vars, null means pass parent env
    	            wd);                           // working directory 
    		  	
    	}

 
    	if(proc!=null){
    		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));		  
    		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
       		  
    		String line="";		    		  
    		while ((line=in.readLine()) != null) { 	
    			   msgs.add(line);		       
    			   //System.out.println(line);		    	
    			   //if(line.trim().indexOf('?')==0){//try to catch latex error		    	
    			   //	   System.out.println("test");		    		
    			   //	   break;		    	 
    			   //}		       		  
    		}
    		   
    	    proc.waitFor();
    	    in.close();
    	    out.close();		   
    	    proc.destroy();
    	}

		    
    	return msgs;
    }	
    
	/*
	 * calculate total conversion coefficient using BrIcc program
	 * NOTE that: even without giving any input uncertainties,
	 *            BrIcc will still generate uncertainty for 
	 *            calculated icc value
	 */
	public static XDX icc(int Z,double eg,String mult,double mr){
		XDX cc=new XDX();
		
		String es=Double.toString(eg);
		String des="0";
		String mrs=Double.toString(mr);
		String dmrs="0";
				
		float[] out=getICC(Z, es, des,mult, mrs, dmrs);
		
		cc.x=out[0];
		cc.dxl=out[1];
		cc.dxu=out[1];
			
		return cc;
	}
	
	public static XDX icc(int Z,double eg,String mult){
		return icc(Z,eg,mult,0);
	}
	
	public static XDX icc(int Z,double eg,double deg,String mult,double mr,double dmru,double dmrl){
		XDX cc=new XDX();
		
		float[] out=getICC(Z, eg,deg,mult,mr,dmru,dmrl);
		
		cc.x=out[0];
		cc.dxl=out[1];
		cc.dxu=out[1];
			
		return cc;
	}
	
	public static XDX icc(int Z,double eg,double deg,String mult,double mr,double dmru){
		return icc(Z,eg,deg,mult,mr,dmru,dmru);
	}
	
	public static XDX icc(int Z,String es,String des,String mult,String mrs,String dmrs){
		XDX cc=new XDX();
		
		float[] out=getICC(Z,es,des,mult,mrs,dmrs);
		
		cc.x=out[0];
		cc.dxl=out[1];
		cc.dxu=out[1];
			
		return cc;
	}
	


	public static XDX iccM1(int Z,String es,String des) {
		return icc(Z,es,des,"M1","0","0");
	}
	public static XDX iccE1(int Z,String es,String des) {
		return icc(Z,es,des,"E1","0","0");
	}
	public static XDX iccE2(int Z,String es,String des) {
		return icc(Z,es,des,"E2","0","0");
	}
	public static XDX iccM2(int Z,String es,String des) {
		return icc(Z,es,des,"M2","0","0");
	}
	
	/*
	 * get an estimate of ICC for Mult=D, taken as the mean of ICCs for M1 and E1
	 */
	public static XDX[] iccD(int Z,String es,String des) {
		XDX[] ccA=new XDX[3];
		try {
			ccA[1]=iccE1(Z,es,des);
			ccA[2]=iccM1(Z,es,des);
			
			XDX ccE1=ccA[1];
			XDX ccM1=ccA[2];
			if(ccE1.x>=0 && ccM1.x>=0) {
				XDX cc=new XDX();
				cc.x=(ccE1.x+ccM1.x)/2.0;
				cc.dxl=Math.abs(ccE1.x-ccM1.x)/2.0;
				cc.dxu=cc.dxl;
				
				ccA[0]=cc;
			}		
		}catch(Exception e) {
			
		}
		
		return ccA;
	}
	
	/*
	 * get an estimate of ICC for Mult=Q, taken as the mean of ICCs for M2 and E2
	 */
	public static XDX[] iccQ(int Z,String es,String des) {
		XDX[] ccA=new XDX[3];
		try {
			ccA[1]=iccE2(Z,es,des);
			ccA[2]=iccM2(Z,es,des);
			
			XDX ccE2=ccA[1];
			XDX ccM2=ccA[2];
			if(ccE2.x>=0 && ccM2.x>=0) {			
				XDX cc=new XDX();
				cc.x=(ccE2.x+ccM2.x)/2.0;
				cc.dxl=Math.abs(ccE2.x-ccM2.x)/2.0;
				cc.dxu=cc.dxl;
				
				ccA[0]=cc;
			}		
		}catch(Exception e) {
			
		}
		
		return ccA;
	}
	
	/*
	 * get an estimate of ICC for Mult=D,E2, taken as the mean of min and max among ICCs for E1, M1 and E2
	 */
	public static XDX[] iccDE2(int Z,String es,String des) {
		XDX[] ccA=new XDX[4];
		try {
			ccA[1]=iccE1(Z,es,des);
			ccA[2]=iccM1(Z,es,des);
			ccA[3]=iccE2(Z,es,des);
			double x1=ccA[1].x;
			double x2=ccA[2].x;
			double x3=ccA[3].x;
			if(x1>=0 && x2>=0 && x3>=0) {
				double max=Math.max(x1,x2);
				max=Math.max(max, x3);
				
				double min=Math.min(x1,x2);
				min=Math.min(min, x3);
				
				XDX cc=new XDX();
				cc.x=(max+min)/2.0;
				cc.dxl=(max-min)/2.0;
				cc.dxu=cc.dxl;
				
				ccA[0]=cc;
			}		
		}catch(Exception e) {
			
		}
		
		return ccA;
	}
	
	/*
	 * get an estimate of ICC for Mult=D,Q, taken as the mean of min and max among ICCs for E1, M1, E2, M2
	 */
	public static XDX[] iccDQ(int Z,String es,String des) {
		XDX[] ccA=new XDX[5];
		try {
			ccA[1]=iccE1(Z,es,des);
			ccA[2]=iccM1(Z,es,des);
			ccA[3]=iccE2(Z,es,des);
			ccA[4]=iccM2(Z,es,des);
			double x1=ccA[1].x;
			double x2=ccA[2].x;
			double x3=ccA[3].x;
			double x4=ccA[4].x;
			if(x1>=0 && x2>=0 && x3>=0 && x4>=0) {
				double max=Math.max(x1,x2);
				max=Math.max(max, x3);
				max=Math.max(max, x4);
				
				double min=Math.min(x1,x2);
				min=Math.min(min, x3);
				min=Math.min(min, x4);
				
				XDX cc=new XDX();
				cc.x=(max+min)/2.0;
				cc.dxl=(max-min)/2.0;
				cc.dxu=cc.dxl;
				
				ccA[0]=cc;
			}		
		}catch(Exception e) {
			
		}
		
		return ccA;
	}
}
