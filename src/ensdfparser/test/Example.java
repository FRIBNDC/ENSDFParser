package ensdfparser.test;

import java.io.File;

import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.Level;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

public class Example {

	public static void main(String[] args) throws Exception {
		Translator.init();
		
        String filePath="",fileDir="";
        String os=System.getProperty("os.name").toLowerCase();
        if(os.contains("mac"))
            fileDir="/Users/chenj/work/evaluation/ENSDF/check/";
        else
            fileDir="H:\\work\\evaluation\\ENSDF\\check\\";
        
        filePath=fileDir+"check.ens";
        
        File f=new File(filePath);
        
        ENSDF ens=new ENSDF();
        ens.setValues(Str.readFile(f));

        
        for(int i=0;i<ens.nLevels();i++) {
        	Level l=ens.levelAt(i);
        	
        	System.out.println(l.ES());
        }
        
	}
}
