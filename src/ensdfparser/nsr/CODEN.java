package ensdfparser.nsr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class CODEN {

    static final Map<String, String> codenMap;
    static ArrayList<String> journalList=new ArrayList<String>();
    static ArrayList<String> codenList=new ArrayList<String>();
    
    static {
        HashMap<String,String> tempMap=new HashMap<String,String>();
        tempMap.put("ACBCA","Acta Crystallogr. B");
        tempMap.put("ARFAA","Acad.Repub.Pop.Rom.Stud., Cercet.Fiz.");
        tempMap.put("ACSAA","Acta Chem.Scand.");
        tempMap.put("ACACB","Acta Crystallogr. A");
        tempMap.put("ASBSD","Acta Crystallographica B");
        tempMap.put("APAHA","Acta Phys.Acad.Sci.Hung.");
        tempMap.put("APASA","Acta Phys.Austr.");
        tempMap.put("APHPF","Acta Phys.Hung.N.S.");
        tempMap.put("APPOA","Acta Phys.Pol.");
        tempMap.put("ATPLB","Acta Phys.Pol. A");
        tempMap.put("APOBB","Acta Phys.Pol. B");
        tempMap.put("CHJPB","Acta Phys.Sinica(Peking)");
        tempMap.put("APSVC","Acta Phys.Slovaca");
        tempMap.put("WLHPA","Acta Physica Sinica");
        tempMap.put("ASPUA","Acta Polytech.Scand., Phys.Nucl.Ser.");
        tempMap.put("ACRAA","Acta Radiol.");
        tempMap.put("ACTNA","Actinides Rev.");
        tempMap.put("AHEPH","Adv.High Energy Phys.");
        tempMap.put("ANUPB","Advan.Nucl.Phys.");
        tempMap.put("ADPHA","Advances in Phys.");
        tempMap.put("AGPYA","Agrochemophysica");
        tempMap.put("ACSRA","Am.Chem.Soc.Absts.");
        tempMap.put("AJPIA","Am.J.Phys.");
        tempMap.put("AJSCA","Am.J.Sci.");
        tempMap.put("ANFIA","An.Fis.");
        tempMap.put("ARSFA","An.R.Soc.Esp.Fis.Quin., Ser.A");
        tempMap.put("ABCNB","Anal.Bioanal.Chem.");
        tempMap.put("ACACA","Anal.Chim.Acta");
        tempMap.put("ANCHA","Anal.Chem.");
        tempMap.put("ANALA","Analyst");
        tempMap.put("ANCEA","Angew.Chem.");
        tempMap.put("ACIEA","Angew.Chem., Int.Ed.Eng.");
        tempMap.put("AABCA","Ann.Acad.Brasil.Cienc.");
        tempMap.put("AAFPA","Ann.Acad.Sci.Fennicae");//*** journal revised 11/01/23
        tempMap.put("AIPSD","Ann.Isr.Phys.Soc.");
        tempMap.put("ANYAA","Ann.N.Y.Acad.Sci.");
        tempMap.put("ANEND","Ann.Nucl.Energy");
        tempMap.put("ANSEC","Ann.Nucl.Sci.Eng.");
        tempMap.put("APNYA","Ann.Phys.(New York)");
        tempMap.put("ANPHA","Ann.Phys.(Paris)");
        tempMap.put("ANPYA","Ann.Phys.");//*** Michael proposed 10/10/23
        tempMap.put("KURAA","Ann.Rep.Res.Reactor Inst., Kyoto Univ.");
        tempMap.put("ARROA","Ann.Rept.Radiat.Center Osaka Prefect");
        tempMap.put("ARAAA","Ann.Rev.Astron.Astrophys.");
        tempMap.put("ARNUA","Ann.Rev.Nucl.Part.Sci.");
        tempMap.put("ANNSA","Ann.Sci.");
        tempMap.put("ASSBA","Ann.Soc.Sci.Brux.");
        tempMap.put("AAWWA","Anz.Akad.Wiss.Wien");//*** journal revised 11/01/23
        tempMap.put("APPLA","App.Phys.Lett.");
        tempMap.put("APPGE","Appl.Geochem.");
        tempMap.put("APOPA","Appl.Opt.");
        tempMap.put("APCOD","Appl.Phys.Commun.");
        tempMap.put("APSFD","Appl.Phys. A");
        tempMap.put("ARISE","Appl.Radiat.Isot.");
        tempMap.put("AJNAD","Arab J.Nucl.Sc.Appl.");
        tempMap.put("ARBMA","Arbok Univ.i Bergen, Mat.-Nat.Ser.");
        tempMap.put("AMNVA","Arch.Mat.Naturvidensk.");
        tempMap.put("ASGVA","Arch.Sci.(Geneva)");
        tempMap.put("ARCHA","Archaeometry");
        tempMap.put("AFYSA","Ark.Fys.");
        tempMap.put("AMAFA","Arkiv Mat.Astron.Fysik");
        tempMap.put("AAEJA","Astron.Astrophys.");
        tempMap.put("ASZHA","Astron.Zh.");
        tempMap.put("APHYE","Astropart.Phys.");
        tempMap.put("ASJOA","Astrophys.J.");
        tempMap.put("APJSA","Astrophys.J.Suppl.Ser.");
        tempMap.put("ASTLA","Astrophys.Lett.");
        tempMap.put("APSSB","Astrophys.Space Sci.");
        tempMap.put("ADNDA","At.Data Nucl.Data Tables");
        tempMap.put("AEAUA","At.Energ.(Australia)");
        tempMap.put("YKJIE","At.Energ.Sci.Tech.(Beijing)");
        tempMap.put("AERVA","At.Energy Rev.");
        tempMap.put("ATKEA","Atomkernenergie");
        tempMap.put("ATKOA","ATOMKI Kozlem.");
        tempMap.put("AKZMA","ATOMKI Kozlemenyek Suppl.");
        tempMap.put("ATPXA","Atompraxis");
        tempMap.put("AETPA","Atoomenerg.Haar Toepass.");
        tempMap.put("AANLA","Atti.Accad.Naz.Lincei, Cl.Sci.Fis., Mat.Nat.Rend.");//*** revised
        tempMap.put("AATFA","Atti.Accad.Sci.Torino");
        tempMap.put("AJCHA","Aus.J.Chem.");
        tempMap.put("AUJPA","Aust.J.Phys.");
        tempMap.put("AJSIA","Aust.J.Sci.");
        tempMap.put("AJSRA","Aust.J.Sci.Res.,Ser.A");
        tempMap.put("BDCBA","Ber.Dtsch.Chem.Ges.B");
        tempMap.put("BDCGA","Ber.Dtsch.Chem.Ges.");//*** Michael proposed 10/10/23
        tempMap.put("CONF-BIRMINGHAM","British Assoc.Adv.Sci.");//*** Michael proposed 10/10/23
        tempMap.put("BJAPA","Br.J.Appl.Phys.");
        tempMap.put("BJPHE","Braz.J.Phys.");
        tempMap.put("BJRAA","British J.Radiology");
        tempMap.put("BJPHD","Bulg.J.Phys.");
        tempMap.put("BAPMA","Bull.Acad.Pol.Sci., Ser.Sci.Math.Astron.Phys.");
        tempMap.put("BAPCA","Bull.Acad.Polon.Sci.Classe (III)");
        tempMap.put("IANFA","Izv.Akad.Nauk SSSR, Ser.Fiz.");//*** Michael change it back 3/20/2026
        //tempMap.put("IANFA","Bull.Acad.Sci.USSR");//*** Michael proposed 10/18/23 using English translation
        tempMap.put("BAPSA","Bull.Am.Phys.Soc.");
        tempMap.put("BKBPA","Bull.Boris Kidrich Inst.Nucl.Sci., Phys.");
        tempMap.put("BCSJA","Bull.Chem.Soc.Jap.");
        tempMap.put("BCSAA","Bull.Cl.Sci., Acad.R.Belg.");
        tempMap.put("BFEYA","Bull.Fac.Eng., Yokohama Nat.Univ.");
        tempMap.put("BUIAA","Bull.Inform.Sci.Tech.(Paris)");
        tempMap.put("BICRA","Bull.Inst.Chem.Res., Kyoto Univ.");
        tempMap.put("BKNSA","Bull.Inst.Nucl.Sci.Boris Kidrich(Belgrade)");
        tempMap.put("BKTMA","Bull.Kyushu Inst.Tech., Natur.Sci.");
        tempMap.put("BRSPE","Bull.Rus.Acad.Sci.Phys.");
        tempMap.put("BSBQA","Bull.Soc.Chim.Belg.");
        tempMap.put("BSRSA","Bull.Soc.Roy.Sci.Liege");
        tempMap.put("BTITA","Bull.Tokyo Inst.Tech.");
        tempMap.put("CJCHA","Can.J.Chem.");
        tempMap.put("CJPHA","Can.J.Phys.");
        tempMap.put("CJREA","Can.J.Res.");
        tempMap.put("CNRBA","Can.J.Res., Sect. B");
        tempMap.put("CKCFA","Cesk.Casopsis Fys. A");
        tempMap.put("CHGEA","Chem.Geol.");
        tempMap.put("CHREA","Chem.Rev.");
        tempMap.put("CMLTA","Chem.Lett.");
        tempMap.put("CMPHC","Chem.Phys.");
        tempMap.put("CHPLB","Chem.Phys.Lett.");
        tempMap.put("CMKZA","Chem.Ztg.");
        tempMap.put("CHIMA","Chimia(Aarau)");
        tempMap.put("YTHLD","Chin.J.Nucl.Phys.");
        tempMap.put("CJOPA","Chin.J.Phys.(Taiwan)");
        tempMap.put("CPBHA","Chin.Phys.B");
        tempMap.put("CPCHC","Chin.Phys.C");
        tempMap.put("CPLEE","Chin.Phys.Lett.");
        tempMap.put("CHPHD","Chinese Physics");
        tempMap.put("CCCCA","Collect.Czech.Chem.Commun.");
        tempMap.put("CNPPA","Comments Nucl.Part.Phys.");
        tempMap.put("CCOHC","Commun.Chem.");
        tempMap.put("CPOHD","Commun.Phys.");
        tempMap.put("CRABA","Compt.Rend.Acad.Bulg.Sci.");
        //tempMap.put("DBANA","Compt.Rend.Acad.Bulgaresci");
        tempMap.put("DBANA","Compt.Rend.Acad.Bulg.Sci.");//*** Michael revised 10/10/13
        tempMap.put("CHDAA","Compt.Rend.Acad.Sci., Ser.A");
        //tempMap.put("COREA","Compt.Rend.Acad.Sci.");//added by Jun and Michel, 10/31/2023, not in the list of CODENs at NSR website
        tempMap.put("COREA","Compt.Rend.");//proposed by Michel, 10/10/2023, not in the list of CODENs at NSR website
        //tempMap.put("CHDBA","Compt.Rend.Acad.Sci., Ser.B");
        tempMap.put("CHDBA","Compt.Rend.");//*** Michael proposed 10/10/23
        //tempMap.put("CRPOB","C.R.Physique");//continuation of Compt.Rend.Acad.Sci after 1965
        tempMap.put("CRPOB","Compt.Rend.");//*** Michael proposed 10/10/23
        tempMap.put("CHDCA","Compt.Rend.Acad.Sci., Ser.C");
        tempMap.put("CRSUD","Compt.Rend.Acad.Sci., Ser.II");
        tempMap.put("CMMSE","Comput.Mater.Sci.");
        tempMap.put("JTMEF","Comput.Math.Meth.Med.");
        tempMap.put("CPHCB","Comput.Phys.Commun.");
        tempMap.put("CTPHA","Contemp.Phys.");
        tempMap.put("CUSCA","Curr.Sci.(India)");
        tempMap.put("CZYPA","Czech.J.Phys.");
        tempMap.put("DABBB","Diss.Abst.Int.");
        tempMap.put("DANTA","Dok.Akad.Nauk Tadzh.SSR");
        tempMap.put("DANUA","Dok.Akad.Nauk Uzb.SSR");
        tempMap.put("DKPCA","Doklady Physical Chemistry");
        tempMap.put("DDIPD","Drug Dev.Ind.Pharm.");
        tempMap.put("EPSLA","Earth Planet.Sci.Lett.");
        tempMap.put("EEJPA","East.Eur.J.Phys");
        tempMap.put("ECGLA","Econ.Geol.");
        tempMap.put("JUARA","Egypt.Chem.");
        tempMap.put("ENNLA","Energ.Nucl.(Milan)");
        tempMap.put("EPJDR","EPJdirect");
        tempMap.put("EARRD","Eur.Appl.Res.Rep., Nucl.Sci.Tech.");
        tempMap.put("EJPHD","Eur.J.Phys.");
        tempMap.put("ZAANE","Eur.Phys.J. A");
        tempMap.put("ZBBNE","Eur.Phys.J. B");
        tempMap.put("ZCCNE","Eur.Phys.J. C");
        tempMap.put("ZDDNE","Eur.Phys.J. D");
        tempMap.put("ZSTNE","Eur.Phys.J. Special Topics");
        tempMap.put("EPHJP","Eur.Phys.J.Plus");
        tempMap.put("ZHHNE","Eur.Phys.Jour.H");
        tempMap.put("EULEE","Europhys.Lett.");
        tempMap.put("EXPPA","Exp.Tech.Phys.");
        tempMap.put("EXTPA","Exp.Tech.Physik");
        tempMap.put("EXPEA","Experientia");
        tempMap.put("FBSYE","Few-Body Systems");
        tempMap.put("FECAA","Fiz.Elem.Chastits At.Yadra");
        tempMap.put("FISZA","Fiz.Szemle");
        tempMap.put("FTVTA","Fiz.Tverd.Tela");
        tempMap.put("FZKAA","Fizika(Zagreb)");
        tempMap.put("FIZAE","Fizika(Zagreb) A");
        tempMap.put("FIZBE","Fizika(Zagreb) B");
        tempMap.put("FZKSA","Fizika(Zagreb) Suppl.");
        tempMap.put("FLNWA","Forschungsber.Landes Nordrhein-Westfalen");
        tempMap.put("FPYKA","Fortsch.Phys.");
        tempMap.put("FPHSA","Front.Phys.(Beijing)");
        tempMap.put("FPHYA","Front.Phys.(Lausanne)");
        tempMap.put("FEDEE","Fusion Eng.Des.");
        tempMap.put("FSTUC","Fusion Sci.Tech.");
        tempMap.put("FYCAB","Fyz.Cas.");
        tempMap.put("GKKGA","Ganseki Kobutsu Kosho Gakkaishi");
        tempMap.put("GCITA","Gazz.Chim.Ital.");
        tempMap.put("GEKEA","Genshikaku Kenkyu (Japan)");
        tempMap.put("GCACA","Geochim.Cosmochim.Act.");
        tempMap.put("GEOKA","Geokhimiya");
        tempMap.put("GMFAA","Glas.Mat.-Fiz.Astron.");
        tempMap.put("GSUFA","God.Sofii.Univ., Fiz.Fak.");
        tempMap.put("HLTPA","Health Phys.");
        tempMap.put("HCACA","Helv.Chim.Acta");
        tempMap.put("HPACA","Helv.Phys.Acta");
        tempMap.put("HEPNP","High Energy Phys. and Nucl.Phys.(China)");
        tempMap.put("HKDKA","Himeji Kogyo Daigaku Kenkyu Hokoku");
        tempMap.put("HTKHA","Ho Tsu K'O Haueh");
        tempMap.put("HYIND","Hyperfine Interactions");
        tempMap.put("IAFAE","Iader.Fiz.Enerh.");
        tempMap.put("IEIMA","IEEE Trans.Instrum.Meas.");
        tempMap.put("IETNA","IEEE trans.nucl.sci.");
        tempMap.put("IJPYA","Indian J.Phys.");
        tempMap.put("IJOPA","Indian J.Pure Appl.Phys.");
        tempMap.put("IACAD","Inf.-Argent., Com.Nac.Energ.At.");
        tempMap.put("INGRA","Ingenieur(The Hague)");
        tempMap.put("INOCA","Inorg.Chem.");
        tempMap.put("INUCA","Inorg.Nucl.Chem.Lett.");
        tempMap.put("ICHAA","Inorganica chimica acta");
        tempMap.put("INETA","Instrum.Exp.Tech.");
        tempMap.put("IMSPF","Int.J.Mass Spectrom.");
        tempMap.put("IJARA","Int.J.Appl.Radiat.Isotop.");
        tempMap.put("IJMIB","Int.J.Mass Spect.Ion Phys.");
        tempMap.put("IJMPD","Int.J.Mass Spect.Ion Proc.");
        tempMap.put("IMPAE","Int.J.Mod.Phys. A");
        tempMap.put("IMPEE","Int.J.Mod.Phys. E");
        tempMap.put("IJNMC","Int.J.Nucl.Med.Biol.");
        tempMap.put("IRAID","Int.J.Radiat.Appl.Instr.D (Nucl.Tracks Radiat.Meas.)");
        tempMap.put("IJRBE","Int.J.Radiat.Biol.");
        tempMap.put("IJTPB","Int.J.Theor.Phys.");
        tempMap.put("ISJRA","Iowa State Coll.J.Sci.");
        tempMap.put("ISISA","Isis");
        tempMap.put("ISRTA","Isot.Radiat.Tech.");
        tempMap.put("IPRXA","Isotopenpraxis");
        tempMap.put("IFMAA","Istanbul Univ. Fen Fak.Meem.Ser.A");
        tempMap.put("IZTEA","Izmeritelnaya Tekh.");
        tempMap.put("IAAFA","Izv.Akad.Nauk Arm.SSR, Ser.Fiz.");
        tempMap.put("IAMNA","Izv.Akad.Nauk Arm.SSR, Ser.Fiz.-Mat.Nauk");
        tempMap.put("ETFMB","Izv.Akad.Nauk Est.SSR, Fiz.Mat.");
        tempMap.put("IAKFB","Izv.Akad.Nauk Kaz.SSR, Ser.Fiz.-Mat.");
        tempMap.put("IUZFA","Izv.Akad.Nauk Uzb.SSR, Ser.Fiz.Mat.");
        tempMap.put("IFABA","Izv.Fiz.Inst.Aneb.Bulg.Akad.Nauk");
        tempMap.put("IVUFA","Izv.Vyssh.Ucheb.Zaved., Fiz.");
        tempMap.put("ITPKA","Izvest.Tomsk.Politekhn.Inst.");
        tempMap.put("JSTNA","J.Am.Stat.Assoc.");
        tempMap.put("JAIOA","J.ASTM Int.");
        tempMap.put("JCSDA","J.Chem.Soc., Dalton Trans.");
        tempMap.put("JCAPB","J.Cosmol.Astropart.Phys.");
        tempMap.put("JESRA","J.Electron Spectrosc.Relat.Phenom.");
        tempMap.put("JJGBD","J.Geophys.Res. B");
        tempMap.put("JMLID","J.Mol.Liq.");
        tempMap.put("JMOSB","J.Mol.Struct.");
        tempMap.put("JNREF","J.Neutron Res.");
        tempMap.put("JPCAF","J.Phys.Chem. A");
        tempMap.put("JQSRA","J.Quant.Spectrosc.Radiat.Transfer");
        tempMap.put("JALCE","J.Alloys and Compounds");
        tempMap.put("JACSA","J.Am.Chem.Soc.");
        tempMap.put("JASPE","J.anal.at.spectrom.");
        tempMap.put("JACGA","J.Appl.Crystallogr");
        tempMap.put("JAPIA","J.Appl.Phys.");
        tempMap.put("NGEGA","J.At.Energy Soc.Jap.");
        tempMap.put("JCEDA","J.Chem.Educ.");
        tempMap.put("JCPSA","J.Chem.Phys.");
        tempMap.put("JCPQA","J.Chim.Phys.");
        tempMap.put("JCTPA","J.Comput.Phys.");
        tempMap.put("JTPHE","J.Exper.Theo.Phys.");
        tempMap.put("JFINA","J.Franklin Inst.");
        tempMap.put("JGREA","J.Geophys.Res.");
        tempMap.put("JOGLA","J.Glaciol.");
        tempMap.put("JICSA","J.Indian Chem.Soc.");
        tempMap.put("JINCA","J.Inorg.Nucl.Chem.");
        tempMap.put("JIONA","J.Instrum.");
        tempMap.put("JINST","J.Instrum.");
        tempMap.put("KNSJA","J.Korean Nucl.Soc.");
        tempMap.put("KPSJA","J.Korean Phys.Soc.");
        tempMap.put("JLCRD","J.Labelled Compd.Radiopharm.");
        tempMap.put("JCOMA","J.Less-Common Met.");
        tempMap.put("JLTPA","J.Low Temp.Physics");
        tempMap.put("JOMRA","J.Magn.Resonance");
        tempMap.put("JMMMD","J.Magnetism and Magnetic Materials");
        tempMap.put("JMOPE","J.Mod.Opt.");
        tempMap.put("JNSMA","J.Natur.Sci.Math.");
        //tempMap.put("JNCEA","J.Nucl.Energy");
        tempMap.put("JNCEA","J.Nuclear Energy");//*** Michael revised 10/10/23
        tempMap.put("JNUCA","J.Nuclear Energy");
        tempMap.put("JNABA","J.Nuclear Energy A/B");
        tempMap.put("JNUMA","J.Nucl.Mater.");
        tempMap.put("JNMME","J.Nucl.Mater.Manage.");
        tempMap.put("JNMEA","J.Nucl.Med.");
        tempMap.put("JNMSB","J.Nucl.Med., Suppl.");
        tempMap.put("HHHHD","J.Nucl.Radiochem.");
        tempMap.put("JNRSA","J.Nucl.Radiochem.Sci.");
        tempMap.put("WOYNA","J.Nucl.Sci.(Seoul)");
        tempMap.put("JNSTA","J.Nucl.Sci.Technol.(Tokyo)");
        tempMap.put("JDXLA","J. of Jilin Uni.,Sci.Ed.");
        tempMap.put("JOSAA","J.Opt.Soc.Amer.");
        tempMap.put("JOBPD","J.Opt.Soc.Amer.B");
        tempMap.put("JPCCA","J.Phys. and Colloid Chem.");
        tempMap.put("JPAGB","J.Phys.(London) A");
        tempMap.put("JPAMA","J.Phys.(London) B");
        tempMap.put("JPBAB","J.Phys.(London) B");
        tempMap.put("JPSOA","J.Phys.(London) C");
        tempMap.put("JPCPA","J.Phys.(London) C");
        tempMap.put("JPAPB","J.Phys.(London) D");
        tempMap.put("JSINA","J.Phys.(London) E");
        tempMap.put("JPFMA","J.Phys.(London) F");
        //tempMap.put("JPGPE","J.Phys.(London) G");
        tempMap.put("JPGPE","J.Phys.G");//*** Michael revised 10/10/23
        //tempMap.put("JPHGB","J.Phys.(London) G");
        tempMap.put("JPHGB","J.Phys.G");//*** Michael revised 10/10/23
        tempMap.put("JOPYA","J.Phys.(Moscow) (U.S.S.R.)");
        tempMap.put("JOPQA","J.Phys.(Paris)");
        tempMap.put("JPQSA","J.Phys.(Paris)");
        tempMap.put("JPQCA","J.Phys.(Paris), Colloq.C");
        tempMap.put("JPSLB","J.Phys.(Paris), Lett.");
        //tempMap.put("JPSLB","J.Phys.(Paris)");//*** Michael revised 10/10/23, but changed back 10/11/23
        //tempMap.put("JPQSA","J.Phys.(Paris), Suppl.");//*** Michael: no such CODEN anymore 10/11/23
        //tempMap.put("JPQSA","J.Phys.(Paris)");//*** Michael revised 10/10/23, but changed 10/11/23
        tempMap.put("JPCSD","J.Phys.:Conf.Ser.");
        tempMap.put("JPCHA","J.Phys.Chem.");
        tempMap.put("JPCRB","J.Phys.Chem.Ref.Data");
        tempMap.put("JPCSA","J.Phys.Chem.Solids");
        tempMap.put("JCOME","J.Phys.Condens.Matter");
        tempMap.put("JCOSE","J.Phys.Conf.Ser.");
        tempMap.put("JPRAA","J.Phys.Radium");
        //tempMap.put("JUPSA","J.Phys.Soc.Jpn.");
        tempMap.put("JUPSA","J.Phys.Soc.Jap.");//*** Michael revised 10/10/23
        tempMap.put("JPJSA","J.Phys.Soc.Jap., Suppl.");
        tempMap.put("JRACB","J.Radioanal.Chem.");
        tempMap.put("JRNCD","J.Radioanal.Nucl.Chem.");
        tempMap.put("JRSRA","J.Rech.CNRS");
        tempMap.put("JNBAA","J.Res.Natl.Bur.Stand., Sect.A");
        tempMap.put("JRNBA","J.Res.Natl.Inst.Stand.Technol.");
        tempMap.put("JSACA","J.S.Afr.Chem.Inst.");
        tempMap.put("JSIRA","J.Sci.Ind.Res.(India)");
        tempMap.put("JADEA","Jad.Energ.");
        tempMap.put("JJAPA","Jap.J.App.Phys.");
        tempMap.put("JTPLA","JETP Lett.");
        tempMap.put("PZETA","Pisma Zh.Eksp.Teor.Fiz.");//*** Michael change it back 3/20/2026
        //tempMap.put("PZETA","JETP Lett.");//*** Michael proposed 10/18/2023 using English translation for counting
        tempMap.put("ZFPRA","ZhETF Pisma v Redaktsiyu");//*** Michael change it back 3/20/2026
        //tempMap.put("ZFPRA","JETP Lett.");//*** Michael proposed 10/18/2023 using English translation for counting
        tempMap.put("KNWLD","Kao Neng Wu Li Yu Heh Wu Li");
        tempMap.put("KERNA","Kernenergie");
        tempMap.put("KERNE","Kerntechnik");
        tempMap.put("KFKKA","KFKI Kozlem.");
        tempMap.put("KDVSA","Kgl.Dan.Vidensk.Selsk.Mat.-Fys.Medd.");
        tempMap.put("KVMFA","Kgl.Danske Videnskab.Selskab, Mat.fys.Skrifter");
        tempMap.put("KHTPA","Ko Hsueh Tung Pao");
        tempMap.put("KRSFA","Kratk.Soobshch.Fiz.");
        tempMap.put("KDKSB","Kyushu Daigaku Kogaku Shuho");
        tempMap.put("LAPLA","Laser Phys.Lett.");
        tempMap.put("LZFTA","Lat.PSR Zinat.Akad.Vestis, Fiz.Teh.Ser.");
        tempMap.put("LJPSE","Latv.J.Phys.Tech.Sci.");
        tempMap.put("NCLTA","Lett.Nuovo Cim.");
        tempMap.put("LFRMA","Liet.Fiz.Rinkinys");
        tempMap.put("MRCHE","Magn.Reson.Chem.");
        tempMap.put("MGFFA","Magy.Fiz.Foly.");
        tempMap.put("MGTOA","Magy.Tud.Akad.Kem.Tud.Oszt.Kozlem.");
        tempMap.put("MSTCA","Meas.Tech.(USSR)");
        tempMap.put("MPHYA","Med.Phys.");
        tempMap.put("MWBWA","Meded.Kon.Vlaam.Acad.Wetensch.Belg., Kl.Wetensch.");
        tempMap.put("MEKSA","Mem.Fac.Eng., Kyushu Univ.");
        tempMap.put("MFKPA","Mem.Fac.Sci., Kyoto Univ., Ser.Phys.Astrophys., Geophys.Chem.");
        tempMap.put("MENCE","Mendeleev Commun.");
        tempMap.put("MPSCF","Meteorit.Planet.Sci.");
        tempMap.put("MTRGA","Metrologia");
        tempMap.put("MPLAE","Mod.Phys.Lett. A");
        tempMap.put("MOPHA","Molecular Physics");
        tempMap.put("MOLEF","Molecules");
        tempMap.put("MNRAA","Mon.not.R.Astron.Soc.");
        tempMap.put("NWGPA","Nachr.Ges.Wiss.Gottingen, Math.-Phys.Kl.Fachgruppe 2");
        tempMap.put("NCAOB","Nat.Commun.");
        tempMap.put("NPAHA","Nat.Phys.");
        tempMap.put("NRPAC","Nat.Rev.Phys.");
        tempMap.put("NGGMA","Natl.Geogr.Mag");
        tempMap.put("NATUA","Nature");//*** Michael 10/10/2023, not in CODEN list but in NSR abstracts
        tempMap.put("NARSA","Naturw.Rundschau");
        tempMap.put("NATWA","Naturwissenschaften");
        tempMap.put("NAUZA","Nauka Zhizn");
        tempMap.put("NTNAA","Ned.Tijdschr.Natuurkd.");
        tempMap.put("NTNEE","Neutron News");
        tempMap.put("NJOPF","New Journal of Physics");
        tempMap.put("NPSUA","New Phys.");
        tempMap.put("NKAKB","Nippon Kagaku Kaishi");
        tempMap.put("NFBFA","Notas Fis.Cent.Brasil.Pesqui.");
        tempMap.put("NOREA","Notes and Records: of the Royal Society");
        tempMap.put("NOGAE","Nouvelles de GANIL");
        tempMap.put("NUTRD","Nucl.Tracks");
        tempMap.put("NSABA","Nucl.Sci.Abstr.");
        tempMap.put("NSETE","Nucl.Sci.Tech.");
        tempMap.put("NMIMA","Nucl.Med.(Stuttgart)");
        tempMap.put("NUAPA","Nucl.Appl.");
        tempMap.put("NDSBA","Nucl.Data Sheets");
        tempMap.put("NDSAA","Nucl.Data Tables");
        tempMap.put("NDTSB","Nucl.Data, Sect.B");
        tempMap.put("NEBSD","Nucl.Energy (Br.Nucl.Energy Soc.)");
        tempMap.put("NEINB","Nucl.Eng.Int.");
        tempMap.put("NUFUA","Nucl.Fusion");
        tempMap.put("NUIMA","Nucl.Instrum.Methods");
        tempMap.put("NIMAE","Nucl.Instrum.Methods Phys.Res. A");
        tempMap.put("NIMBE","Nucl.Instrum.Methods Phys.Res. B");
        tempMap.put("NMBIE","Nucl.Med.Biol.");
        tempMap.put("NUPHA","Nucl.Phys.");
        tempMap.put("NUPAB","Nucl.Phys. A");
        tempMap.put("NUPBB","Nucl.Phys. B");
        tempMap.put("NPBSE","Nucl.Phys. B(Proc.Supp.) S");
        tempMap.put("NUPHN","Nucl.Phys.News");
        tempMap.put("NSAPA","Nucl.Sci.Appl., Ser.");
        tempMap.put("NSAAB","Nucl.Sci.Appl., Ser.A");
        tempMap.put("NUSBA","Nucl.Sci.Appl., Ser.B");
        tempMap.put("NSENA","Nucl.Sci.Eng.");
        tempMap.put("NSIJA","Nucl.Sci.J.(Taiwan)");
        tempMap.put("NUTYB","Nucl.Technology");
        tempMap.put("NUCLA","Nucleonics");
        tempMap.put("NCLEA","Nucleus(Lahore)");
        tempMap.put("NUKKA","Nukleonik");
        tempMap.put("NUKLA","Nukleonika");
        //tempMap.put("NCIAA","Nuovo Cim.");
        tempMap.put("NCIAA","Nuovo Cimento");//revised by Jun 11/2/2023
        tempMap.put("NIFBA","Nuovo Cim. B");
        tempMap.put("NCIBA","Nuovo Cim. B");
        tempMap.put("NIFCA","Nuovo Cim. C");
        tempMap.put("NCSDD","Nuovo Cim. D");
        tempMap.put("NUCUA","Nuovo Cim., Suppl.");
        tempMap.put("NUCIA","Nuovo Cimento");
        tempMap.put("OAWPA","Oesterr.Akad.Wiss., Math-Naturw.Kl., Sitzber., Abt.II");
        tempMap.put("OPSUA","Opt.Spectrosc.");
        tempMap.put("OPSPA","Opt.Spektrosk.");
        tempMap.put("OPLED","Optics Letters");
        tempMap.put("OSAWA","Osterr.Akad.Wiss., Math.-Naturw.Kl.");
        tempMap.put("PSIRA","Pakistan J.Sci.Ind.Research");
        tempMap.put("FECLA","Part. and Nucl.Lett.");
        tempMap.put("PLACB","Part.Accel.");
        tempMap.put("PANUA","Part.Nucl.");
        tempMap.put("PHMAA","Phil.Mag.");//*** Michael 10/10/2023 not in CODEN list but in NSR abstracts
        tempMap.put("PHSCA","Philippine J.Sci.");
        //tempMap.put("PTRMA","Philos.Trans.R.Soc. A");
        tempMap.put("PTRMA","Philos.Trans.R.Soc.");//*** Michael revised 10/10/2023
        tempMap.put("PHSBB","Phys.Bull.");
        tempMap.put("PPHRC","Phys.Procedia");
        tempMap.put("PRRHA","Phys.Rev.Res.");
        tempMap.put("PHTOA","Phys.Today");
        tempMap.put("PHWOE","Phys.World");
        tempMap.put("PHUSE","Phys.Usp.");
        tempMap.put("PANUE","Phys.Atomic Nuclei");
        tempMap.put("PHCAA","Phys.Can.");
        tempMap.put("PPCPF","Phys.Chem.Chem.Phys.");
        tempMap.put("PCEAA","Phys.Chem.Earth");
        tempMap.put("PHFEA","Phys.Fenn.");
        tempMap.put("PHLTA","Phys.Lett.");
        tempMap.put("PYLAA","Phys.Lett. A");
        tempMap.put("PYLBB","Phys.Lett. B");
        tempMap.put("PHMBA","Phys.Med.Biol.");
        tempMap.put("PHNOA","Phys.Norv.");
        tempMap.put("PPNLA","Phys.Part. and Nucl.Lett.");
        tempMap.put("PHPEF","Phys.Perspect.");
        tempMap.put("PRPLC","Phys.Rep.");
        tempMap.put("PHRVA","Phys.Rev.");
        tempMap.put("PLRAA","Phys.Rev. A");
        tempMap.put("PLRBA","Phys.Rev. B");
        tempMap.put("PRBMD","Phys.Rev. B");
        tempMap.put("PRVCA","Phys.Rev. C");
        tempMap.put("PRVDA","Phys.Rev. D");
        tempMap.put("PLEEE","Phys.Rev. E");
        tempMap.put("PRSTA","Phys.Rev. Special Topics - Accelerators and Beams");
        tempMap.put("PRLTA","Phys.Rev.Lett.");
        tempMap.put("PRXHA","Phys.Rev.X");
        tempMap.put("PHSTB","Phys.Scr.");
        tempMap.put("PSSAB","Phys.Stat.Sol. (a)");
        tempMap.put("PSSBB","Phys.Stat.Sol. (b)");
        tempMap.put("PHSSA","Phys.Status Solidi");
        tempMap.put("PHVEA","Phys.Verh.");
        tempMap.put("PHZTA","Phys.Z.");
        tempMap.put("PHYSA","Physica");
        tempMap.put("PHYAD","Physica A");
        tempMap.put("PHYBE","Physica B");
        tempMap.put("PHBCD","Physica C");
        tempMap.put("PHYCE","Physica C");
        tempMap.put("EFOMP","Physica Medica");
        tempMap.put("PPNUE","Physics of Part.and Nuclei");
        tempMap.put("PHZSA","Physik Z.Sowjetunion");
        tempMap.put("PZTFD","Pisma Zh.Tekh.Fiz.");
        tempMap.put("PLSSA","Planet.Space Sci.");
        tempMap.put("PTMRA","Platinum Met.Rev.");
        tempMap.put("POPYA","Port.Phys.");
        tempMap.put("PSTFA","Postepy Fiz.");
        tempMap.put("PRAMC","Pramana");
        tempMap.put("PRTEA","Prib.Tekh.Eksp.");
        tempMap.put("VANYD","Probl.Atom.Sci.Tech.:Ser.Nucl.Rect.Constants");
        tempMap.put("PIFLD","Probl.Yad.Fiz.Kosm.Luchei");
        tempMap.put("PAPCA","Proc.Am.Philos.Soc.");
        tempMap.put("PKNAA","Proc.K.Ned.Akad.Wet.");//*** Michael change it back 3/20/2026
        //tempMap.put("PKNAA","Proc.Acad.Soc.Amsterdam");//*** Michael revised 10/10/2023 using English for counting
        tempMap.put("PMPEA","Proc.Math.Phys.Soc.Egypt");
        tempMap.put("PPMJA","Proc.Phys.Math.Soc.");
        tempMap.put("PCPSA","Proc.Cambridge Phil.Soc.");
        tempMap.put("PISAA","Proc.Indian Acad.Sci.Sect.A");
        tempMap.put("PIPSB","Proc.Indian Natl.Sci.Acad.");
        tempMap.put("PIAIA","Proc.Iowa Acad.Sci.");
        tempMap.put("PIIAA","Proc.Nat.Inst.Sci.India A");
        tempMap.put("PNASA","Proc.Natl.Acad.Sci USA");
        tempMap.put("PIPUA","Proc.Natl.Inst.Sci.India.Pt.");
        tempMap.put("PPSSA","Proc.Nucl.Phys.Solid State Phys.Symp.");
        tempMap.put("PPSOA","Proc.Phys.Soc.(London)");
        tempMap.put("PRYCA","Proc.Roy.Soc.(Canada)");
        tempMap.put("PRLAA","Proc.Roy.Soc.(London)");
        tempMap.put("PREAA","Proc.Roy.Soc.Edinburgh, Sect.");
        tempMap.put("PNEND","Prog.Nucl.Energy");
        tempMap.put("PNMRA","Prog.Nucl.Magn.Reson.Spectrosc.");
        tempMap.put("PPNPD","Prog.Part.Nucl.Phys.");
        tempMap.put("PTEPC","Prog.Theor.Exp.Phys.");
        tempMap.put("PTPKA","Prog.Theor.Phys.(Kyoto)");
        tempMap.put("PTPSA","Prog.Theor.Phys.(Kyoto), Suppl.");
        tempMap.put("PTBMA","PTB-Mitt.");
        tempMap.put("ARAQA","Publ.Com.Nac.Ener.Atom.Ser.Quim.");
        tempMap.put("PACHA","Pure Appl.Chem.");
        tempMap.put("QSRED","Quat.Sci.Rev.");
        tempMap.put("RAEFB","Radiat.Eff.");
        tempMap.put("RMEAE","Radiat.Meas.");
        tempMap.put("RPCHD","Radiat.Phys.Chem.");
        tempMap.put("RAREA","Radiat.Res.");
        tempMap.put("RARAE","Radioact.Radiochem.");
        tempMap.put("RACAA","Radiocarbon");
        tempMap.put("RRALA","Radiochem.Radioanal.Lett.");
        tempMap.put("RDIOE","Radiochemistry");
        tempMap.put("RAACA","Radiochim.Acta");
        tempMap.put("RAISA","Radioisotopes(Tokyo)");
        tempMap.put("RAISB","Radioisotopy");
        tempMap.put("RADMA","Radium (Paris)");
        tempMap.put("RESSE","Reliab.eng.syst.saf.");
        tempMap.put("RPPHA","Rep.Prog.Phys.");
        tempMap.put("KKYHB","Res.Rep.Lab.Nucl.Sci., Tohoku Univ.");
        tempMap.put("RMFSA","Rev.Brasil.Fis.");
        tempMap.put("RBFSA","Rev.Brasil.Fis.");
        tempMap.put("RMXFA","Rev.Mex.Fis.");
        tempMap.put("RMPHA","Rev.Mod.Phys.");
        tempMap.put("RPRRA","Rev.Phys., Acad.Rep.Populaire Roumaine");
        tempMap.put("RPHAA","Rev.Phys.Appl.");
        tempMap.put("RRPQA","Rev.Roum.Phys.");
        tempMap.put("RSINA","Rev.Sci.Instrum.");
        tempMap.put("SCPGB","Rev.Soc.Cient.Parag.");
        tempMap.put("RMAFA","Rev.Union Mat.Arg.Asoc.Fis.Arg.");
        tempMap.put("RISCA","Ric.Sci.");
        tempMap.put("RINUA","Riv.Ing.Nucl.");
        tempMap.put("RNCUA","Riv.Nuovo Cimento Soc.Ital.Fis.");
        tempMap.put("ROCHA","Roczniki chemii");
        tempMap.put("RJPHE","Rom.J.Phys.");
        tempMap.put("SAJAA","S.Afr.J.Agric.Sci.");
        tempMap.put("SAPHD","S.Afr.J.Phys.");
        tempMap.put("SRCEC","Sci.Rep.");
        tempMap.put("SCAMA","Sci.Amer.");
        tempMap.put("SCPMC","Sci.China: Phys.Mech.Astron.");
        tempMap.put("SPIPA","Sci.Pap.Inst.Phys.Chem.Res.");
        tempMap.put("SPDAA","Sci.Proc.Roy.Dublin Soc.");
        tempMap.put("KPSNB","Sci.Rep.Kyoto Prefect.Univ.");
        tempMap.put("STNIC","Sci.Technol.Nucl.Install.");
        tempMap.put("STEND","Sci.Total Environ.");
        tempMap.put("SCIEA","Science");
        tempMap.put("SSINA","Scientica Sinica");
        tempMap.put("SHIBA","Shitsuryo Bunseki");
        tempMap.put("SBGMA","Sitzber.Ges.Befoerder.Ges.Naturw.Marburg");
        tempMap.put("CPHMA","Soc.Sci.Fenn., Comment.Phys.-Math.");
        tempMap.put("SSCOA","Solid State Commun.");
        tempMap.put("SAKNA","Soobshch.Akad.Nauk Aruz.SSR");
        tempMap.put("AENGA","At.Energ.");//*** Michael change it back 3/20/2026
        //tempMap.put("AENGA","Soviet J.At.Energy");//*** Michael proposed 10/18/2023 using English translation for counting
        tempMap.put("RADKA","Radiokhimiya");//*** Michael change it back 3/20/2026
        //tempMap.put("RADKA","Sov.Radiochemistry");//*** Michael proposed 10/18/2023 using English translation for counting
        tempMap.put("YAFIA","Yad.Fiz.");//*** Michael change it back 3/20/2026
        //tempMap.put("YAFIA","Sov.J.Nucl.Phys.");//*** Michael proposed 10/18/2023 using English translation for counting
        tempMap.put("DANKA","Dok.Akad.Nauk");//*** Michael change it back 3/20/2026
        //tempMap.put("DANKA","Soviet Phys.Doklady");//*** Michael proposed 10/18/2023 using English translation for counting
        tempMap.put("ZETFA","Zh.Eksp.Teor.Fiz.");//*** Michael change it back 3/20/2026
        //tempMap.put("ZETFA","Soviet Phys.JETP");//*** Michael proposed 10/18/2023 using English translation for counting
        tempMap.put("SPSRA","Space Sci.Rev.");
        tempMap.put("SHPMP","Stud.Hist.Phil.Mod.Phys B");
        tempMap.put("SCEFA","Stud.Cercet.Fiz.");
        tempMap.put("SSRED","Surf.Sci.Rep.");
        tempMap.put("SYMMA","Symmetry");
        tempMap.put("SYPHE","Synth.Philos.");
        tempMap.put("DHWHA","Taehn Hwahakoe Chi");
        tempMap.put("TLNTA","Talanta");
        tempMap.put("TCACF","Theor.Chem.Acc.");
        tempMap.put("TVGGB","Tr., Vses.Nauch.-Issled.Inst.Yadern.Geofiz.Geokhim.");
        tempMap.put("TYFKA","Tr.Inst.Yadern.Fiz.Akad.Nauk Kaz.SSR");
        tempMap.put("TSNIB","Tr.Soyuznogo Nauch.-Issled.Inst.Priborostr.");
        tempMap.put("TBICA","Trans.Bose Res.Inst.(Calcutta)");
        tempMap.put("TKASA","Trans.Ky.Acad.Sci");
        tempMap.put("TNYAA","Trans.N.Y.Acad.Sci.");
        tempMap.put("TANSA","Trans.Amer.Nucl.Soc.");
        tempMap.put("TBGNS","Trans.Bulg.Nucl.Soc.");
        tempMap.put("TCITA","Trans.Chalmers Univ.Technol., Gothamburg");
        tempMap.put("TFSOA","Trans.Faraday Soc.");
        tempMap.put("TRSAA","Trans.R.Soc.S.Afr.");
        tempMap.put("TJPHE","Turk.J.Phys.");
        tempMap.put("UFIZA","Ukr.Fiz.Zh.");
        tempMap.put("UFZHA","Ukr.Fiz.Zh.");
        tempMap.put("UKPJA","Ukr.J.Phys.");
        tempMap.put("UNIVB","Universe");
        tempMap.put("UFNAA","Usp.Fiz.Nauk");
        tempMap.put("VDPEA","Verh.Deut.Phys.Ges.");
        tempMap.put("VANKA","Vestn.Akad.Nauk Kaz.SSR");
        tempMap.put("VLUFB","Vestn.Leningr.Univ., Ser.Fiz.Khim.");
        tempMap.put("VMUFA","Vestn.Mosk.Univ., Fiz., Astron.");
        tempMap.put("VBSFA","Vestsi Akad.Navuk Belarusk.Ssr, Ser.Fiz.-Mat.Navuk");
        tempMap.put("VAFEA","Vetsi Acad.Nauk BSSR Ser.Fiz.Energ.");
        tempMap.put("VKSGA","Vis.Khar.Nat.Univ.");
        tempMap.put("VANTR","Voprosy At.Nauki i Tekh., Ser.Fiz.Yad.Reak.(Moscow)");
        tempMap.put("VANKD","Voprosy At.Nauki i Tekh., Ser.Yad.Konstanty(Moscow)");
        tempMap.put("XRSPA","X-Ray Spectrom.");
        tempMap.put("YWPIF","Yuan.Wul.Ping.");
        tempMap.put("ZKKKA","Z.Kristallogr., Kristallgeom., Kristallphys., Kristallchem.");
        tempMap.put("ZANCA","Z.Anal.Chem.");
        tempMap.put("ZAMPA","Z.Angew.Math.Phys.");
        tempMap.put("ZAPHA","Z.Angew.Phys.");
        tempMap.put("ZEELA","Z.Elektrochem.");
        tempMap.put("ZEINA","Z.Instrumentenk.");
        tempMap.put("ZNASE","Z.Naturforsch.");
        tempMap.put("ZENAA","Z.Naturforsch.");
        tempMap.put("ZEPYA","Z.Phys.");
        tempMap.put("ZPAAD","Z.Phys. A");
        tempMap.put("ZPBBD","Z.Phys. B");
        tempMap.put("ZPCFD","Z.Phys. C");
        tempMap.put("ZDACE","Z.Phys. D");
        tempMap.put("ZPCLA","Z.Phys.Chem.(Leipzig)");
        tempMap.put("ZPCAA","Z.Phys.Chem., Abt. A");
        tempMap.put("ZPCBA","Z.Phys.Chem., Abt. B");
        tempMap.put("ZRPHE","Zb.Rad.Prir.Mat. Fak.");
        tempMap.put("ZTEFA","Zh.Tekh.Fiz.");
        tempMap.put("ZAKHA","Zhur.Analit.Khim.");
        tempMap.put("BIRMINGHAM(83RD MEETING)","British Assoc. Adv. Sci");//for 1913ASZZ
        tempMap.put("NNES","Nat.Nucl.Ener.Ser.");
        tempMap.put("RADIOCHEM.STUDIES:FISSION","Nat.Nucl.Ener.Ser.");
        tempMap.put("TRANSUR.ELEMENTS.BOOK","Nat.Nucl.Ener.Ser.");
        
        codenMap= Collections.unmodifiableMap(tempMap);
        
        for(Entry<String,String>entry:codenMap.entrySet()) {
            codenList.add(entry.getKey());
            journalList.add(entry.getValue().toUpperCase().replace(". ",".").trim());
        }
        
    }
    
    public static String getJournalByCoden(String key) {
    	try {
            String s=codenMap.get(key);
            if(s!=null)
            	return s;
            
            return "";
    	}catch(Exception e) {
    		
    	}

    	return "";
    }
    
    public static Map<String,String> getMap(){
        return codenMap;      
    }
    
    public static String getCodenOfJournal(String journal) {
        try {
            String s=journal.toUpperCase().replace(". ",".").trim();
            int index=journalList.indexOf(s);
            if(index>=0)
                return codenList.get(index);
                
        }catch(Exception e) {
            
        }
        
        return "";
    }
}
