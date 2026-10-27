package ensdfparser.base;

import java.io.File;


/**
 * This object contains the application setup.  Paths to specific utilities 
 * needed by the program, etc.
 * 
 * Setup is saved/loaded from users home directory.

 *Obsoleted: Setup of each app is different, and should not share common settings, since
 *           a change in one app would result in unexpected change in the Setup of others
 */

@Deprecated
public class BaseSetup {
    // the defaults should be fine for most unix systems
    
	public static String dirSeparator=File.separator;

    /// directory used last
    public static String filedir = null;
    /// directory for the output files
    public static String outdir=System.getProperty("user.dir")+dirSeparator+"out";
    
    /// default folder to store the xml configure files for all codes
    public static String confdir=System.getProperty("user.home");
    
    //public static String lastOutdir=outdir;
    
    public static void setUserDirs(String indir1,String outdir1) {
    	try {
    		filedir=indir1;
    		outdir=outdir1;
    		
    		//lastOutdir=outdir;
    	}catch(Exception e) {
    		
    	}
    }
}

