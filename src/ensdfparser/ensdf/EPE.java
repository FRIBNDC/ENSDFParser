 package ensdfparser.ensdf;

public class EPE{

/* calculates end point energies */

  private float q, dq, pe, dpe, le, dle, epe_f, depe_f;
  private String epe_s, depe_s;

  public EPE(){
    epe_s=""; depe_s=""; epe_f=(float) -1.; depe_f=(float) -1.0;
  }

  public void setValues(float iq, float idq, float ipe, float idpe, float ile, float idle){
     q=iq; dq=idq; pe=ipe; dpe=idpe; le=ile; dle=idle;

     double x=q+pe-le;
     double dx=0;

     if( dq > 0 ) dx=dx + Math.pow(dq,2);
     if( dpe > 0 ) dx=dx + Math.pow(dpe,2);
     if( dle > 0 ) dx=dx + Math.pow( dle ,2);

     if( dx > 0 ) dx=Math.pow( dx , 0.5);

     XDX2SDS s=new XDX2SDS();
     s.setValues(x,dx);
     epe_s=s.S();
     depe_s=s.DS();
   
     epe_f=(float) s.X();
     depe_f=(float) s.DX();
  }


  public String EPES(){ return epe_s;}
  public String DEPES(){ return depe_s;}
  public float EPEF(){ return epe_f;}
  public float DEPEF(){ return depe_f;}

}