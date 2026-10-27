 package ensdfparser.ensdf;

/* ABE: Atomic Binding energy */

public class ABE{

private double bk, bl, bm, bn, bo, bp, bq;
private double dbk, dbl, dbm, dbn, dbo, dbp, dbq;

public ABE(int z){

 bk=0; bl=0; bm=0; bn=0; bo=0; bp=0; bq=0;
 dbk=0; dbl=0; dbm=0; dbn=0; dbo=0; dbp=0; dbq=0;

 if( z == 1){ bk=0.0136; } 
 if( z == 2){ bk=0.0246; } 
 if( z == 3){ bk=0.0548; bl=0.0053; } 
 if( z == 4){ bk=0.1121; bl=0.0080; } 
 if( z == 5){ bk=0.188; bl=0.007333333; } 
 if( z == 6){ bk=0.2838; bl=0.010266666; } 
 if( z == 7){ bk=0.4016; bl=0.014266666; } 
 if( z == 8){ bk=0.532; dbk=4.0E-4; bl=0.014233333; } 
 if( z ==   9){ bk=0.6854; dbk=4.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  10){ bk=0.8669; dbk=3.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  11){ bk=1.0721; dbk=4.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  12){ bk=1.305; dbk=4.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  13){ bk=1.5596; dbk=4.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  14){ bk=1.8389; dbk=4.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  15){ bk=2.1455; dbk=4.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  16){ bk=2.472; dbk=4.0E-4; bl=0.0; dbl=0.0; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  17){ bk=2.8224; dbk=3.0E-4; bl=0.2702; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  18){ bk=3.2029; dbk=3.0E-4; bl=0.32; dbl=0.0010; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  19){ bk=3.6074; dbk=4.0E-4; bl=0.3771; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  20){ bk=4.0381; dbk=4.0E-4; bl=0.4378; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  21){ bk=4.4928; dbk=4.0E-4; bl=0.5004; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  22){ bk=4.9664; dbk=4.0E-4; bl=0.5637; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  23){ bk=5.4651; dbk=3.0E-4; bl=0.6282; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  24){ bk=5.9892; dbk=3.0E-4; bl=0.6946; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  25){ bk=6.539; dbk=4.0E-4; bl=0.769; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  26){ bk=7.112; dbk=9.0E-4; bl=0.8461; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  27){ bk=7.7089; dbk=3.0E-4; bl=0.9256; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  28){ bk=8.3328; dbk=4.0E-4; bl=1.0081; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  29){ bk=8.9789; dbk=4.0E-4; bl=1.0966; dbl=4.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  30){ bk=9.6586; dbk=6.0E-4; bl=1.1936; dbl=9.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  31){ bk=10.3671; dbk=5.0E-4; bl=1.2977; dbl=0.0011; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  32){ bk=11.103; dbk=0.0070; bl=1.4143; dbl=7.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  33){ bk=11.8667; dbk=7.0E-4; bl=1.5265; dbl=8.0E-4; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  34){ bk=12.6578; dbk=7.0E-4; bl=1.6539; dbl=0.0035; bm=0.0; dbm=0.0; bn=0.0; dbn=0.0;}
 if( z ==  35){ bk=13.4737; dbk=4.0E-4; bl=1.782; dbl=4.0E-4; bm=0.2565; dbm=4.0E-4; bn=0.0273; dbn=5.0E-4;}
 if( z ==  36){ bk=14.3256; dbk=8.0E-4; bl=1.921; dbl=6.0E-4; bm=0.288; dbm=0.0010; bn=0.024; dbn=8.0E-4;}
 if( z ==  37){ bk=15.1997; dbk=3.0E-4; bl=2.0651; dbl=3.0E-4; bm=0.3221; dbm=3.0E-4; bn=0.0293; dbn=3.0E-4;}
 if( z ==  38){ bk=16.1046; dbk=3.0E-4; bl=2.2163; dbl=3.0E-4; bm=0.3575; dbm=3.0E-4; bn=0.0377; dbn=3.0E-4;}
 if( z ==  39){ bk=17.0384; dbk=3.0E-4; bl=2.3725; dbl=3.0E-4; bm=0.3936; dbm=3.0E-4; bn=0.0454; dbn=3.0E-4;}
 if( z ==  40){ bk=17.9976; dbk=4.0E-4; bl=2.5316; dbl=3.0E-4; bm=0.4303; dbm=3.0E-4; bn=0.0513; dbn=3.0E-4;}
 if( z ==  41){ bk=18.9856; dbk=4.0E-4; bl=2.6977; dbl=3.0E-4; bm=0.4684; dbm=3.0E-4; bn=0.0581; dbn=3.0E-4;}
 if( z ==  42){ bk=19.9995; dbk=3.0E-4; bl=2.8655; dbl=3.0E-4; bm=0.5046; dbm=3.0E-4; bn=0.0618; dbn=3.0E-4;}
 if( z ==  43){ bk=21.044; dbk=7.0E-4; bl=3.0425; dbl=4.0E-4; bm=0.544; dbm=0.0010; bn=0.068; dbn=0.0020;}
 if( z ==  44){ bk=22.1172; dbk=3.0E-4; bl=3.224; dbl=3.0E-4; bm=0.585; dbm=3.0E-4; bn=0.0749; dbn=3.0E-4;}
 if( z ==  45){ bk=23.2199; dbk=3.0E-4; bl=3.4119; dbl=3.0E-4; bm=0.6271; dbm=3.0E-4; bn=0.081; dbn=3.0E-4;}
 if( z ==  46){ bk=24.3503; dbk=3.0E-4; bl=3.6043; dbl=3.0E-4; bm=0.6699; dbm=3.0E-4; bn=0.0864; dbn=3.0E-4;}
 if( z ==  47){ bk=25.514; dbk=3.0E-4; bl=3.8058; dbl=3.0E-4; bm=0.7175; dbm=3.0E-4; bn=0.0952; dbn=3.0E-4;}
 if( z ==  48){ bk=26.7112; dbk=3.0E-4; bl=4.018; dbl=3.0E-4; bm=0.7702; dbm=3.0E-4; bn=0.1076; dbn=3.0E-4; bo=0.0022; }
 if( z ==  49){ bk=27.9399; dbk=3.0E-4; bl=4.2375; dbl=3.0E-4; bm=0.8256; dbm=3.0E-4; bn=0.1219; dbn=3.0E-4; bo=0.0006; }
 if( z ==  50){ bk=29.2001; dbk=4.0E-4; bl=4.4647; dbl=3.0E-4; bm=0.8838; dbm=3.0E-4; bn=0.1365; dbn=4.0E-4; bo=0.0010; }
 if( z ==  51){ bk=30.4912; dbk=3.0E-4; bl=4.6983; dbl=3.0E-4; bm=0.9437; dbm=3.0E-4; bn=0.152; dbn=3.0E-4; bo=0.0036; }
 if( z ==  52){ bk=31.8138; dbk=3.0E-4; bl=4.9392; dbl=3.0E-4; bm=1.006; dbm=3.0E-4; bn=0.1683; dbn=3.0E-4; bo=0.0054; }
 if( z ==  53){ bk=33.1694; dbk=4.0E-4; bl=5.1881; dbl=3.0E-4; bm=1.0721; dbm=3.0E-4; bn=0.1864; dbn=3.0E-4; bo=0.0067; }
 if( z ==  54){ bk=34.5614; dbk=0.0011; bl=5.4528; dbl=4.0E-4; bm=1.142; dbm=0.0010; bn=0.208; dbn=0.0010; bo=0.0163;}
 if( z ==  55){ bk=35.9846; dbk=4.0E-4; bl=5.7143; dbl=4.0E-4; bm=1.2171; dbm=4.0E-4; bn=0.2308; dbn=4.0E-4; bo=0.0157; }
 if( z ==  56){ bk=37.4406; dbk=4.0E-4; bl=5.9888; dbl=4.0E-4; bm=1.2928; dbm=4.0E-4; bn=0.253; dbn=5.0E-4; bo=0.0201; }
 if( z ==  57){ bk=38.9246; dbk=4.0E-4; bl=6.2663; dbl=5.0E-4; bm=1.3613; dbm=3.0E-4; bn=0.2704; dbn=8.0E-4; bo=0.0204; }
 if( z ==  58){ bk=40.443; dbk=4.0E-4; bl=6.5488; dbl=5.0E-4; bm=1.4346; dbm=6.0E-4; bn=0.2896; dbn=7.0E-4; bo=0.0258; }
 if( z ==  59){ bk=41.9906; dbk=5.0E-4; bl=6.8348; dbl=5.0E-4; bm=1.511; dbm=8.0E-4; bn=0.3045; dbn=9.0E-4; bo=0.0273; }
 if( z ==  60){ bk=43.5689; dbk=4.0E-4; bl=7.126; dbl=4.0E-4; bm=1.5753; dbm=7.0E-4; bn=0.3152; dbn=8.0E-4; bo=0.0266; }
 if( z ==  61){ bk=45.184; dbk=7.0E-4; bl=7.4279; dbl=8.0E-4; bm=1.649; dbm=0.0050; bn=0.33; dbn=0.0050; bo=0.0273; }
 if( z ==  62){ bk=46.8342; dbk=5.0E-4; bl=7.7368; dbl=5.0E-4; bm=1.7228; dbm=8.0E-4; bn=0.3457; dbn=9.0E-4; bo=0.0267; }

 if( z ==  63){ bk=48.519; dbk=4.0E-4; bl=8.052; dbl=4.0E-4; bm=1.8; dbm=5.0E-4; bn=0.3602; dbn=7.0E-4; bo=0.0253; }
 if( z ==  64){ bk=50.2391; dbk=5.0E-4; bl=8.3756; dbl=5.0E-4; bm=1.8808; dbm=5.0E-4; bn=0.3758; dbn=7.0E-4; bo=0.0255;}
 if( z ==  65){ bk=51.9957; dbk=5.0E-4; bl=8.708; dbl=5.0E-4; bm=1.9675; dbm=6.0E-4; bn=0.3979; dbn=8.0E-4; bo=0.0299;}
 if( z ==  66){ bk=53.7885; dbk=5.0E-4; bl=9.0458; dbl=5.0E-4; bm=2.0468; dbm=4.0E-4; bn=0.4163; dbn=5.0E-4; bo=0.0385;}
 if( z ==  67){ bk=55.6177; dbk=5.0E-4; bl=9.3942; dbl=4.0E-4; bm=2.1283; dbm=6.0E-4; bn=0.4357; dbn=8.0E-4; bo=0.0306;}

 if( z ==  68){ bk=57.4855; dbk=5.0E-4; bl=9.7513; dbl=4.0E-4; bm=2.2065; dbm=6.0E-4; bn=0.4491; dbn=0.0010; bo=0.0395;}
 if( z ==  69){ bk=59.3896; dbk=5.0E-4; bl=10.1157; dbl=4.0E-4; bm=2.3068; dbm=7.0E-4; bn=0.4717; dbn=9.0E-4; bo=0.0393;}
 if( z ==  70){ bk=61.3323; dbk=5.0E-4; bl=10.4864; dbl=4.0E-4; bm=2.3981; dbm=4.0E-4; bn=0.4872; dbn=6.0E-4; bo=0.0336;}
 if( z ==  71){ bk=63.3138; dbk=5.0E-4; bl=10.8704; dbl=4.0E-4; bm=2.4912; dbm=5.0E-4; bn=0.5062; dbn=6.0E-4; bo=0.0244;}
 if( z ==  72){ bk=65.3508; dbk=6.0E-4; bl=11.2707; dbl=4.0E-4; bm=2.6009; dbm=4.0E-4; bn=0.5381; dbn=4.0E-4; bo=0.0294;}

 if( z ==  73){ bk=67.4164; dbk=6.0E-4; bl=11.6815; dbl=3.0E-4; bm=2.708; dbm=4.0E-4; bn=0.5655; dbn=5.0E-4; bo=0.0328;}
 if( z ==  74){ bk=69.525; dbk=3.0E-4; bl=12.0998; dbl=3.0E-4; bm=2.8196; dbm=4.0E-4; bn=0.595; dbn=4.0E-4; bo=0.0343;}
 if( z ==  75){ bk=71.6764; dbk=4.0E-4; bl=12.5267; dbl=4.0E-4; bm=2.9317; dbm=4.0E-4; bn=0.625; dbn=4.0E-4; bo=0.0340;}
 if( z ==  76){ bk=73.8708; dbk=5.0E-4; bl=12.968; dbl=4.0E-4; bm=3.0485; dbm=4.0E-4; bn=0.6543; dbn=5.0E-4; bo=0.0624;}
 if( z ==  77){ bk=76.111; dbk=5.0E-4; bl=13.4185; dbl=3.0E-4; bm=3.1737; dbm=7.0E-4; bn=0.6901; dbn=4.0E-4; bo=0.0433;}

 if( z ==  78){ bk=78.3948; dbk=7.0E-4; bl=13.8799; dbl=4.0E-4; bm=3.296; dbm=9.0E-4; bn=0.722; dbn=6.0E-4; bo=0.0444;}
 if( z ==  79){ bk=80.7249; dbk=5.0E-4; bl=14.3528; dbl=4.0E-4; bm=3.4249; dbm=3.0E-4; bn=0.7588; dbn=4.0E-4; bo=0.0486;}
 if( z ==  80){ bk=83.1023; dbk=8.0E-4; bl=14.8393; dbl=0.0010; bm=3.5616; dbm=0.0011; bn=0.8003; dbn=0.0010; bo=0.0574;}
 if( z ==  81){ bk=85.5304; dbk=6.0E-4; bl=15.3467; dbl=4.0E-4; bm=3.7041; dbm=4.0E-4; bn=0.8455; dbn=5.0E-4; bo=0.0675;}
 if( z ==  82){ bk=88.0045; dbk=7.0E-4; bl=15.8608; dbl=5.0E-4; bm=3.8507; dbm=5.0E-4; bn=0.8936; dbn=7.0E-4; bo=0.0752; bp=0.0015;}

 if( z ==  83){ bk=90.5259; dbk=7.0E-4; bl=16.3875; dbl=4.0E-4; bm=3.9991; dbm=3.0E-4; bn=0.9382; dbn=3.0E-4; bo=0.0840; bp=0.0044;}
 if( z ==  84){ bk=93.105; dbk=0.0038; bl=16.939; dbl=0.01; bm=4.1494; dbm=0.0039; bn=0.9953; dbn=0.0029; bo=0.0948; bp=0.0047;}
 if( z ==  85){ bk=95.73; dbk=0.0080; bl=17.493; dbl=0.029; bm=4.317; dbm=0.0050; bn=1.042; dbn=0.0050; bo=0.1054; bp=0.0067;}
 if( z ==  86){ bk=98.404; dbk=0.012; bl=18.049; dbl=0.038; bm=4.482; dbm=0.0050; bn=1.097; dbn=0.0050; bo=0.1160; bp=0.0087;}
 if( z ==  87){ bk=101.137; dbk=0.013; bl=18.639; dbl=0.04; bm=4.652; dbm=0.0050; bn=1.153; dbn=0.0050; bo=0.1320; bp=0.0122;}

 if( z ==  88){ bk=103.922; dbk=0.0080; bl=19.2367; dbl=0.0015; bm=4.822; dbm=0.0015; bn=1.2084; dbn=0.0016; bo=0.1478; bp=0.0169;}
 if( z ==  89){ bk=106.755; dbk=0.0060; bl=19.84; dbl=0.018; bm=5.002; dbm=0.0050; bn=1.269; dbn=0.0050; bo=0.1634; bp=0.0172;}
 if( z ==  90){ bk=109.6509; dbk=9.0E-4; bl=20.4721; dbl=5.0E-4; bm=5.1823; dbm=3.0E-4; bn=1.3295; dbn=4.0E-4; bo=0.1774; bp=0.0187;}
 if( z ==  91){ bk=112.601425; dbk=-1.0; bl=21.1046; dbl=0.0018; bm=5.3669; dbm=0.0016; bn=1.3871; dbn=0.0018; bo=0.1554; bp=0.0194;}
 if( z ==  92){ bk=115.60612; dbk=-1.0; bl=21.7574; dbl=3.0E-4; bm=5.548; dbm=4.0E-4; bn=1.4408; dbn=4.0E-4; bo=0.1638; bp=0.0197;}

 if( z ==  93){ bk=118.678; dbk=0.033; bl=22.4268; dbl=9.0E-4; bm=5.7232; dbm=0.0036; bn=1.5007; dbn=8.0E-4; bo=0.1730; bp=0.0209;}
 if( z ==  94){ bk=121.818; dbk=0.044; bl=23.0972; dbl=0.0016; bm=5.9329; dbm=0.0014; bn=1.5586; dbn=8.0E-4; bo=0.1780; bp=0.0241;}
 if( z ==  95){ bk=124.998; dbk=0.025; bl=23.806; dbl=0.017; bm=6.125; dbm=0.012; bn=1.616; dbn=0.02; bo=0.1598; bp=0.0246;}
 if( z ==  96){ bk=128.261; dbk=0.044; bl=24.532; dbl=0.032; bm=6.336; dbm=0.018; bn=1.685; dbn=0.02; bo=0.1690; bp=0.0213;}
 if( z ==  97){ bk=131.583; dbk=0.044; bl=25.27; dbl=0.032; bm=6.545; dbm=0.018; bn=1.75; dbn=0.02; bo=0.1765; bp=0.0221;}

 if( z ==  98){ bk=134.971; dbk=0.044; bl=26.019; dbl=0.032; bm=6.755; dbm=0.018; bn=1.81; dbn=0.02; bo=0.1829; bp=0.0274;}
 if( z ==  99){ bk=138.429; dbk=0.044; bl=26.793; dbl=0.032; bm=6.975; dbm=0.018; bn=1.878; dbn=0.02; bo=0.1908; bp=0.0287;}
 if( z == 100){ bk=141.968; dbk=0.034; bl=27.59; dbl=0.025; bm=7.206; dbm=0.012; bn=1.954; dbn=0.02; bo=0.2001; bp=0.0245;}
 if( z == 101){ bk=145.571; dbk=0.062; bl=28.4; dbl=0.044; bm=7.433; dbm=0.02; bn=2.019; dbn=0.02; bo=0.2066; bp=0.0302;}
 if( z == 102){ bk=149.256; dbk=0.062; bl=29.234; dbl=0.044; bm=7.67; dbm=0.02; bn=2.092; dbn=0.02; bo=0.2148; bp=0.0313;}
 if( z == 103){ bk=153.023; dbk=0.062; bl=30.098; dbl=0.044; bm=7.921; dbm=0.02; bn=2.174; dbn=0.02; bo=0.2294; bp=0.0294;}

}
  public double Bkd(){ return bk; }
  public double Bld(){ return bl; }
  public double Bmd(){ return bm; }
  public double Bnd(){ return bn; }
  public double Bod(){ return bo; }
  public double Bpd(){ return bp; }
  public double Bqd(){ return bq; }

  public float Bkf(){ return (float) bk; }
  public float Blf(){ return (float) bl; }
  public float Bmf(){ return (float) bm; }
  public float Bnf(){ return (float) bn; }
  public float Bof(){ return (float) bo; }
  public float Bpf(){ return (float) bp; }
  public float Bqf(){ return (float) bq; }

  public double DBkd(){ return dbk; }
  public double DBld(){ return dbl; }
  public double DBmd(){ return dbm; }
  public double DBnd(){ return dbn; }
  public double DBod(){ return dbo; }
  public double DBpd(){ return dbp; }
  public double DBqd(){ return dbq; }

  public float DBkf(){ return (float) dbk; }
  public float DBlf(){ return (float) dbl; }
  public float DBmf(){ return (float) dbm; }
  public float DBnf(){ return (float) dbn; }
  public float DBof(){ return (float) dbo; }
  public float DBpf(){ return (float) dbp; }
  public float DBqf(){ return (float) dbq; }

}
