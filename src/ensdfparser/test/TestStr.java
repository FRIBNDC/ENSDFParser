package ensdfparser.test;

import java.util.Vector;

import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.latex.Translator;
import ensdfparser.nds.util.Str;

public class TestStr {
	  public static void main(String[] args) throws Exception {
		  test3();
	  }
	  
	  public static void test2() {
		  SDS2XDX sx=new SDS2XDX("100","0");
		  sx=sx.subtract("21","8");
		  sx=sx.subtract("45","7");
		  System.out.println(" s="+sx.s()+" ds="+sx.ds()+" dsu="+sx.dsu()+" dsl="+sx.dsl()
		  +"\n dx="+sx.dx()+" dxu="+sx.dxu()+" dxl="+sx.dxl());
	  }
	  
	  public static void test1() {
		  String s="208PB(36S,35PG),(36S,35PG)";
		  String[] temp=Str.splitOutsideBrackets(s, ",");
		  for(int i=0;i<temp.length;i++)
			  System.out.println(temp[i]);
	  }
	  
	  public static void test3() throws Exception {
		  Translator.init();
		  /*
		  String s=" 78SE    76GE(A,2NG)                   1987SC07,1982MA45         09NDS    200910                           \n"
		  		 + " 78SE C  SIGMA(EG,THETA)";
		  String[] temp=s.split("\n");
		  Vector<String> linesV=new Vector<String>();
		  for(int i=0;i<temp.length;i++)
			  linesV.add(temp[i]);
		  
          EnsdfUtil.cleanENSDFLines(linesV,true);

          for(String line:linesV)
          	System.out.println(line);
          */
		  
		  String s="SIGMA(EG,THETA)";
		  s=Translator.procGenCom(s, 5, false);
		  System.out.println(s);
	  }
	  
	  
}
