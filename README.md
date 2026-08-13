A Java library to parse all data in ENSDF datasets into ENSDF objects that can be called to retrieve any data in the datasets.

A usage example:

        File f=new File(PATH_TO_AN_ENSDF_FILE);//
        ENSDF ens=new ENSDF();
        ens.setValues(Str.readFile(f));

        for(int i=0;i<ens.nLevels();i++) {
        	Level l=ens.levelAt(i);        	
        	System.out.println(l.ES());
        }

See attached Excample.java file for more details.
