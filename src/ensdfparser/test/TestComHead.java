package ensdfparser.test;

import java.io.File;
import java.util.Vector;

import ensdfparser.ensdf.Comment;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.Gamma;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

public class TestComHead {
	  public static void main(String[] args) throws Exception {
		  Translator.init();
		  
		  //test1();
		  test2();
	  }
	  
	  static void test1() throws Exception {
		  String s1=" 78KR cG E$From 1981Ba40";
		  String s2=" 78KR cG E(A,B),RI(A,C),M$From 1979He18";
		  
		  Vector<String> linesV=new Vector<String>();
		  
		  linesV.add(s1);
		  Comment c1=new Comment();
		  c1.setValues(linesV);
		  
		  linesV.clear();
		  linesV.add(s2);
		  Comment c2=new Comment();
		  c2.setValues(linesV);
		  
		  System.out.println("head(): c1="+c1.head()+" type="+c1.type()+"$ c2="+c2.head()+" type="+c2.type()+"$");
		  
		  for(int i=0;i<c2.headV().size();i++) {
			  System.out.println("c2.headV #"+i+": "+c2.headV().get(i)+" headAt="+c2.headAt(i));
		  }
		  
		  System.out.println("\n\nc2.flags()="+c2.flags()+" flagV.size="+c2.flagV().size());
		  for(int i=0;i<c2.flagVV().size();i++) {
			  
			  System.out.println("###### c2.flagVV("+i+") size="+c2.flagVV().get(i).size()+"  flagV("+i+")="+c2.flagV().get(i));
			  for(int j=0;j<c2.flagVV().get(i).size();j++) {
				  System.out.println("   c2.flagVV("+i+")("+j+")="+c2.flagVV().get(i).get(j)+" flagAt(i,j)="+c2.flagAt(i, j));
				  
			  }
		  }
			  
	  }
	  
	  static void test2() throws Exception {
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
	        
	        for(Gamma g:ens.gammasV())
	        	System.out.println("EG="+g.ES()+" flag="+g.flag()+"  EGRef="+g.EGRef());
	  }
}
