A Java library to parse all data in ENSDF datasets into ENSDF objects that can be called to retrieve any data in the datasets.

A usage example:

import java.io.File;
import java.io.IOException;

import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.Level;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

public class TestParser {
	   public static void main(String[] args) throws IOException, Exception {
			Translator.init();
			
	        String filePath="example.ens";	        
		    File f=new File(filePath);
		    ENSDF ens=new ENSDF();
		    ens.setValues(Str.readFile(f));

		    for(int i=0;i<ens.nLevels();i++) {
		    	Level l=ens.levelAt(i);        	
		    	System.out.println(l.ES());
		    }
		   }
}

See attached Example.java file for more details.
