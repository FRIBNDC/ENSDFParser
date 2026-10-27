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



/*
 * wrap for the bricc executable for getting the calculated ICC values
 */
public class BriccExeWrap {
	
	private static String briccHome="";//needed for running bricc
	private static String briccExecPath="";//executable name including absolute path
	private static String briccExecName="";//executable name
	
	private static String errorMsg="";//error message if bricc fails
	
	private static int countOfCalcs=0;
	private static float totalTime=0;
	
	public BriccExeWrap(){
	}
	
	
	public static String briccExecName(){return briccExecName;} 
	public static String briccExecPath(){return briccExecPath;} 
	public static String briccExecHome(){return briccHome;} 	
	
	public static void setBriccExecName(String name){briccExecName=name;}
	public static void setBriccExecHome(String home){briccHome=home;}
	public static void setBriccExecPath(String path){briccExecPath=path;}
	
	
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
	        
	        if(url==null)//bricc does not exist in the classpath
	        	return false;
	        	
	        InputStream is = url.openStream();
	        */
			
	    	InputStream is=BriccExeWrap.class.getClassLoader().getResourceAsStream(filename); 
	    	//System.out.println((is==null)+"   "+filename);
	    	
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
	
	public static boolean checkInternalBricc(String  copyToDir){
		return checkInternalBricc(copyToDir,"");
	}
	
	
	/*
	 * check if the bricc program exists in the package containing this class and if it works
	 * copyToPath: the local path which the brics exec file is coped to
	 * option: copy option. force to copy only if option="FORCE" or "F" or starts with "F"
	 */
	public static boolean checkInternalBricc(String  copyToDir,String option){
	    
	    errorMsg="";
	    countOfCalcs=0;
	    totalTime=0;
	    
        //find out what operating system is being used, and set the right filename for the script
		String temp=System.getProperty("os.name").toLowerCase();
        
		//System.out.println(" temp="+temp);
        
        if(temp.toLowerCase().contains("windows"))
        	briccExecName="BrIcc.exe";
        else if(temp.contains("linux"))
        	briccExecName="bricc";
        else if(temp.contains("mac")) {
            if(is64bit())
                briccExecName="briccMac";
            else
                briccExecName="briccMac";
        }else {
            errorMsg="No bricc executable for system="+temp;
        	return false;
        }
		
		//errorMsg+=" debug: briccExecName="+briccExecName+" "+copyToDir;

        File f=new File(copyToDir);
        
        //System.out.println(f.exists());
        
        if(!f.exists())
        	f.mkdir();
        
		briccExecPath=copyToDir+dirSeparator()+briccExecName;
		
        f=new File(briccExecPath);       
        
        //System.out.println(briccExecPath+"  "+f.exists());
        //errorMsg+=" debug: "+briccExecPath+"  "+f.exists();
        
        if(!copy(briccExecName,copyToDir,option)) {
            errorMsg="executable="+briccExecName+" cannot be copied to "+copyToDir;
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
		
		//System.out.println(" briccExec="+briccExecPath);
        
        //File f=new File(briccExecPath);
        if(f!=null)
        	briccHome=f.getParent();
        
        boolean isGood=checkBriccPath(briccExecPath);
        
        //errorMsg+=" BriccExeWrap 205: isGood="+isGood+" path="+briccExecPath;
        
       return isGood;
	}

	private static void makeDummyFile(String filePath) {
		try {
			PrintWriter out=new PrintWriter(new File(filePath));
			out.write("test");
			out.close();
		}catch(Exception e) {
			
		}

	}
	/*
	 * check if the bricc program at the specified path exists and works
	 */
	public static boolean checkBriccPath(String briccExecPath){

		//System.out.println(" briccExecPath="+briccExecPath);
		
		if(briccExecPath.trim().length()==0) {
		    errorMsg="No path for bricc has been specified";
        	return false;
		}
        
        File f=new File(briccExecPath);
        if(f==null || !f.exists()) {
            errorMsg=briccExecPath+" does not exist.";
        	return false;
        }

 
		Vector<String> msgs=null;
		try{

		       
			if(briccExecPath.contains(" ")) {
				briccExecPath="\""+briccExecPath+"\"";
			}
			
			String dummyInput=briccHome+File.separator+"dummy.txt";
			makeDummyFile(dummyInput);
			String[] envp={"BrIccHome="+briccHome};
			String[] command= {briccExecPath,dummyInput,"TEST"};//when BRICC read "TEST", it is "command line argument unknown" error, then exit which is what I want

			//errorMsg+="BriccExeWrap252: briccHome="+briccHome;
			
			msgs=runCommand(command,envp,briccHome);

		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		if(msgs.size()==0)
			return false;
		
		for(String s:msgs) {
			s=s.toUpperCase();
			
			//errorMsg+=" debug: s="+s+" path="+briccHome+"\n";
			if(s.contains("BRICC")&&s.contains("CONVERSION"))
				return true;
		}
        /*
        //System.out.println(" briccExec="+briccExecName+" bricssHome="+briccHome+" Float.parseFloat(test[0])="+Float.parseFloat(test[0]));

        
        if(Float.parseFloat(test[0])>0)
        	return true;
        
        
        errorMsg ="bricc executable="+briccExecPath+" exists,\n";
        errorMsg+=" but it does not work. It could be that the bricc executable:\n";
        
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
        */
		return false;	
	}
	
	public static String errorMessage() {return errorMsg;}
		
    public static float getTotalTimeOfCalculations() {return totalTime;}
	public static int getCountOfCalculations() {return countOfCalcs;}
	public static void resetCountOfCalculations() {countOfCalcs=0;totalTime=0;}
	
    public static Vector<String> runCommand(String[] command,String[] envp,String dir) throws Exception{

    	Vector<String> msgs=new Vector<String>();
    	
    	if(command.length==0)
    		return msgs;
    	
    	String os=System.getProperty("os.name").toLowerCase();  	
    	
		Process proc=null;
    	File wd=null;
    	if(dir!=null && !dir.isEmpty())
    		wd=new File(dir);

    	//System.out.println("command="+command);
    	
    	if(os.contains("linux")||os.contains("mac")){     	
            //String shell="/bin/bash";
            
            proc = Runtime.getRuntime().exec(command, envp, wd);   //nor working for file redirection in command
            
    		//proc = Runtime.getRuntime().exec(shell, envp, wd);
    		
    		
    		
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
        	
        	/*
        	String tempS=command.length+"";
        	
        	if(command!=null) {
            	for(int i=0;i<command.length;i++)
            		tempS+=command[i]+"\n";
        	}else
        		tempS+=" command=null\n";

        	if(envp!=null) {
              	for(int i=0;i<envp.length;i++)
            		tempS+=" envp: "+envp[i];
        	}else
        		tempS+=" envp=null\n";
        	
  
        	
        	errorMsg+="BriccExeWrap 376: "+tempS;
        	*/
        	
    		proc=rt.exec(command,                  //path to executable and arguments
    	            envp,                          // env vars, null means pass parent env
    	            wd);                           // working directory 

    		  	
    	}

 
    	if(proc!=null){

    		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));		  
    		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
       		  
    		try {
        		String line="";		    		  
        		while ((line=in.readLine()) != null) { 	
        			   msgs.add(line);	
        	        //errorMsg+="BriccExeWrap 407: "+line+"\n";
        			   //System.out.println(line);		    	
        			   //if(line.trim().indexOf('?')==0){//try to catch latex error		    	
        			   //	   System.out.println("test");		    		
        			   //	   break;		    	 
        			   //}		       		  
        		}
    		}catch(Exception e) {
    			e.printStackTrace();
    		}

    		   

    	    proc.waitFor();

    	    in.close();
    	    out.close();		   

    	    proc.destroy();

    	}

		    
    	return msgs;
    }	

}
