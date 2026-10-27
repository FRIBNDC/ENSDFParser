package ensdfparser.ensdf;

import java.util.LinkedHashMap;
import java.util.Vector;

public class Elements {
	 
	static Vector<String> elementSymbolV=new Vector<String>();
	static  Vector<String> elementNameV=new Vector<String>();
	static Vector<Integer> mat25V=new Vector<Integer>();
	
	static LinkedHashMap<String,Integer> elementDefaultMassNumberMap=new LinkedHashMap<String,Integer>();
	
	static final String[] naturalElements={
			 "NN:1",  "H:1",   "HE:4",  "LI:7",  "BE:9",  "B:11",  "C:12",  "N:14",  "O:16",  "F:19", "NE:20",	//Z=0  - 10
			 "NA:23", "MG:24", "AL:23", "SI:28", "P:31",  "S:32",  "CL:35", "AR:40", "K:39",  "CA:40",		//  11 - 20
			 "SC:45", "TI:48", "V:51",  "CR:52", "MN:55", "FE:56", "CO:59", "NI:59", "CU:64", "ZN:65",		//  21 - 30
			 "GA:70", "GE:73", "AS:75", "SE:79", "BR:80", "KR:84", "RB:85", "SR:88", "Y:89",  "ZR:91",		//  31 - 40
			 "NB:93", "MO:96", "TC:98", "RU:101","RH:103","PD:106","AG:108","CD:112","IN:115","SN:119",		//  41 - 50
			 "SB:122","TE:128","I:127", "XE:131","CS:133","BA:137","LA:139","CE:140","PR:141","ND:144",		//  51 - 60
			 "PM:145","SM:150","EU:152","GD:157","TB:159","DY:163","HO:165","ER:167","TM:169","YB:173",		//  61 - 70
			 "LU:175","HF:178","TA:181","W:184", "RE:186","OS:190","IR:192","PT:195","AU:197","HG:201",		//  71 - 80
			 "TL:204","PB:207","BI:209","PO:209","AT:210","RN:222","FR:223","RA:226","AC:227","TH:232",		//  81 - 90
			 "PA:231","U:238", "NP:237","PU:244","AM:243","CM:247","BK:247","CF:251","ES:252","FM:257",		//  91 - 100
			 "MD:258","NO:259","LR:266","RF:267","DB:268","SG:269","BH:270","HS:277","MT:278","DS:281",		// 101 - 110
			 "RG:282","CN:285","NH:286","FL:289","MC:290","LV:293","TS:294","OG:294"		        // 111 - 118
	};
	
	static {
		for(int i=0;i<naturalElements.length;i++) {
			String[] temp=naturalElements[i].split("[:]+");
			String name=temp[0];
			String AS=temp[1];
			elementDefaultMassNumberMap.put(name, Integer.valueOf(AS));
			
		}
	}
	
	static {
		   elementSymbolV.clear();
		   elementSymbolV.add("NN") ;
		   elementSymbolV.add("H");
		   elementSymbolV.add("HE");
		   elementSymbolV.add("LI");
		   elementSymbolV.add("BE");
		   elementSymbolV.add("B");
		   elementSymbolV.add("C");
		   elementSymbolV.add("N");
		   elementSymbolV.add("O");
		   elementSymbolV.add("F");
		   elementSymbolV.add("NE");
		   elementSymbolV.add("NA");
		   elementSymbolV.add("MG");
		   elementSymbolV.add("AL");
		   elementSymbolV.add("SI");
		   elementSymbolV.add("P");
		   elementSymbolV.add("S");
		   elementSymbolV.add("CL");
		   elementSymbolV.add("AR");
		   elementSymbolV.add("K");
		   elementSymbolV.add("CA");
		   elementSymbolV.add("SC");
		   elementSymbolV.add("TI");
		   elementSymbolV.add("V");
		   elementSymbolV.add("CR");
		   elementSymbolV.add("MN");
		   elementSymbolV.add("FE");
		   elementSymbolV.add("CO");
		   elementSymbolV.add("NI");
		   elementSymbolV.add("CU");
		   elementSymbolV.add("ZN");
		   elementSymbolV.add("GA");
		   elementSymbolV.add("GE");
		   elementSymbolV.add("AS");
		   elementSymbolV.add("SE");
		   elementSymbolV.add("BR");
		   elementSymbolV.add("KR");
		   elementSymbolV.add("RB");
		   elementSymbolV.add("SR");
		   elementSymbolV.add("Y");
		   elementSymbolV.add("ZR");
		   elementSymbolV.add("NB") ;
		   elementSymbolV.add("MO");
		   elementSymbolV.add("TC");
		   elementSymbolV.add("RU");
		   elementSymbolV.add("RH");
		   elementSymbolV.add("PD");
		   elementSymbolV.add("AG") ;
		   elementSymbolV.add("CD");
		   elementSymbolV.add("IN") ;
		   elementSymbolV.add("SN") ;
		   elementSymbolV.add("SB") ;
		   elementSymbolV.add("TE") ;
		   elementSymbolV.add("I") ;
		   elementSymbolV.add("XE") ;
		   elementSymbolV.add("CS") ;
		   elementSymbolV.add("BA") ;
		   elementSymbolV.add("LA") ;
		   elementSymbolV.add("CE") ;
		   elementSymbolV.add("PR") ;
		   elementSymbolV.add("ND") ;
		   elementSymbolV.add("PM") ;
		   elementSymbolV.add("SM") ;
		   elementSymbolV.add("EU") ;
		   elementSymbolV.add("GD") ;
		   elementSymbolV.add("TB") ;
		   elementSymbolV.add("DY") ;
		   elementSymbolV.add("HO") ;
		   elementSymbolV.add("ER") ;
		   elementSymbolV.add("TM") ;
		   elementSymbolV.add("YB") ;
		   elementSymbolV.add("LU") ;
		   elementSymbolV.add("HF") ;
		   elementSymbolV.add("TA") ;
		   elementSymbolV.add("W") ;
		   elementSymbolV.add("RE") ;
		   elementSymbolV.add("OS") ;
		   elementSymbolV.add("IR") ;
		   elementSymbolV.add("PT") ;
		   elementSymbolV.add("AU");
		   elementSymbolV.add("HG") ;
		   elementSymbolV.add("TL") ;
		   elementSymbolV.add("PB") ;
		   elementSymbolV.add("BI") ;
		   elementSymbolV.add("PO") ;
		   elementSymbolV.add("AT") ;
		   elementSymbolV.add("RN") ;
		   elementSymbolV.add("FR") ;
		   elementSymbolV.add("RA") ;
		   elementSymbolV.add("AC") ;
		   elementSymbolV.add("TH") ;
		   elementSymbolV.add("PA") ;
		   elementSymbolV.add("U") ;
		   elementSymbolV.add("NP") ;
		   elementSymbolV.add("PU") ;
		   elementSymbolV.add("AM") ;
		   elementSymbolV.add("CM") ;
		   elementSymbolV.add("BK") ;
		   elementSymbolV.add("CF") ;
		   elementSymbolV.add("ES") ;
		   elementSymbolV.add("FM") ;
		   elementSymbolV.add("MD") ;
		   elementSymbolV.add("NO") ;
		   elementSymbolV.add("LR") ;
		   elementSymbolV.add("RF") ;
		   elementSymbolV.add("DB") ;
		   elementSymbolV.add("SG") ;
		   elementSymbolV.add("BH") ;
		   elementSymbolV.add("HS") ;
		   elementSymbolV.add("MT") ;
		   elementSymbolV.add("DS") ;
		   elementSymbolV.add("RG") ;
		   elementSymbolV.add("CN") ;
		   elementSymbolV.add("NH") ;
		   elementSymbolV.add("FL") ;
		   elementSymbolV.add("MC") ;
		   elementSymbolV.add("LV") ;
		   elementSymbolV.add("TS") ;
		   elementSymbolV.add("OG") ;

		   elementNameV.clear();
		   elementNameV.add("Neutron") ;
		   elementNameV.add("Hydrogen");
		   elementNameV.add("Helium");
		   elementNameV.add("Lithium");
		   elementNameV.add("Beryllium");
		   elementNameV.add("Boron");
		   elementNameV.add("Carbon");
		   elementNameV.add("Nitrogen");
		   elementNameV.add("Oxygen");
		   elementNameV.add("Fluorine");
		   elementNameV.add("Neon");
		   elementNameV.add("Sodium");
		   elementNameV.add("Magnesium");
		   elementNameV.add("Aluminum");
		   elementNameV.add("Silicon");
		   elementNameV.add("Phosphorus");
		   elementNameV.add("Sulfur");
		   elementNameV.add("Chlorine");
		   elementNameV.add("Argon");
		   elementNameV.add("Potassium");
		   elementNameV.add("Calcium");
		   elementNameV.add("Scandium");
		   elementNameV.add("Titanium");
		   elementNameV.add("Vanadium");
		   elementNameV.add("Chromium");
		   elementNameV.add("Manganese");
		   elementNameV.add("Iron");
		   elementNameV.add("Cobalt");
		   elementNameV.add("Nickel");
		   elementNameV.add("Copper");
		   elementNameV.add("Zinc");
		   elementNameV.add("Gallium");
		   elementNameV.add("Germanium");
		   elementNameV.add("Arsenic");
		   elementNameV.add("Selenium");
		   elementNameV.add("Bromine");
		   elementNameV.add("Krypton");
		   elementNameV.add("Rubidium");
		   elementNameV.add("Strontium");
		   elementNameV.add("Yttrium");
		   elementNameV.add("Zirconium");
		   elementNameV.add("Niobium");
		   elementNameV.add("Molybdenum");
		   elementNameV.add("Technetium");
		   elementNameV.add("Ruthenium");
		   elementNameV.add("Rhodium");
		   elementNameV.add("Palladium");
		   elementNameV.add("Silver") ;
		   elementNameV.add("Cadmium");
		   elementNameV.add("Indium") ;
		   elementNameV.add("Tin") ;
		   elementNameV.add("Antimony") ;
		   elementNameV.add("Tellurium") ;
		   elementNameV.add("Iodine") ;
		   elementNameV.add("Xenon") ;
		   elementNameV.add("Cesium") ;
		   elementNameV.add("Barium") ;
		   elementNameV.add("Lanthanum") ;
		   elementNameV.add("Cerium") ;
		   elementNameV.add("Praseodymium") ;
		   elementNameV.add("Neodymium") ;
		   elementNameV.add("Promethium") ;
		   elementNameV.add("Samarium") ;
		   elementNameV.add("Europium") ;
		   elementNameV.add("Gadolinium") ;
		   elementNameV.add("Terbium") ;
		   elementNameV.add("Dysprosium") ;
		   elementNameV.add("Holmium") ;
		   elementNameV.add("Erbium") ;
		   elementNameV.add("Thulium") ;
		   elementNameV.add("Ytterbium") ;
		   elementNameV.add("Lutetium") ;
		   elementNameV.add("Hafnium") ;
		   elementNameV.add("Tantalum") ;
		   elementNameV.add("Tungsten") ;
		   elementNameV.add("Rhenium") ;
		   elementNameV.add("Osmium") ;
		   elementNameV.add("Iridium") ;
		   elementNameV.add("Platinum") ;
		   elementNameV.add("Gold");
		   elementNameV.add("Mercury") ;
		   elementNameV.add("Thallium") ;
		   elementNameV.add("Lead") ;
		   elementNameV.add("Bismuth") ;
		   elementNameV.add("Polonium") ;
		   elementNameV.add("Astatine") ;
		   elementNameV.add("Radon") ;
		   elementNameV.add("Francium") ;
		   elementNameV.add("Radium") ;
		   elementNameV.add("Actinium") ;
		   elementNameV.add("Thorium") ;
		   elementNameV.add("Protactinium") ;
		   elementNameV.add("Uranium") ;
		   elementNameV.add("Neptunium") ;
		   elementNameV.add("Plutonium") ;
		   elementNameV.add("Americium") ;
		   elementNameV.add("Curium") ;
		   elementNameV.add("Berkelium") ;
		   elementNameV.add("Californium") ;
		   elementNameV.add("Einsteinium") ;
		   elementNameV.add("Fermium") ;
		   elementNameV.add("Mendelevium") ;
		   elementNameV.add("Nobelium") ;
		   elementNameV.add("Lawrencium") ;
		   elementNameV.add("Rutherfordium") ;
		   elementNameV.add("Dubnium") ;
		   elementNameV.add("Seaborgium") ;
		   elementNameV.add("Bohrium") ;
		   elementNameV.add("Hassium") ;
		   elementNameV.add("Meitnerium") ;
		   elementNameV.add("Darmstadtium") ;
		   elementNameV.add("Roentgenium") ;
		   elementNameV.add("Copernicium") ;//Z=112
		   elementNameV.add("Nihonium") ;//Z=113
		   elementNameV.add("Flerovium") ;
		   elementNameV.add("Moscovium") ;
		   elementNameV.add("Livermorium") ;
		   elementNameV.add("Tennessine") ;
		   elementNameV.add("Oganesson") ;


		   mat25V.clear();
		   mat25V.add(new Integer(1));    /* z=0 */ 
		   mat25V.add(new Integer(1));    /* z=1 */ 
		   mat25V.add(new Integer(3));    /* z=2 */ 
		   mat25V.add(new Integer(6));    /* z=3 */ 
		   mat25V.add(new Integer(9));    /* z=4 */ 
		   mat25V.add(new Integer(10));    /* z=5 */ 
		   mat25V.add(new Integer(12));    /* z=6 */ 
		   mat25V.add(new Integer(14));    /* z=7 */ 
		   mat25V.add(new Integer(16));    /* z=8 */ 
		   mat25V.add(new Integer(19));    /* z=9 */ 
		   mat25V.add(new Integer(20));    /* z=10 */ 
		   mat25V.add(new Integer(23));    /* z=11 */ 
		   mat25V.add(new Integer(24));    /* z=12 */ 
		   mat25V.add(new Integer(27));    /* z=13 */ 
		   mat25V.add(new Integer(28));    /* z=14 */ 
		   mat25V.add(new Integer(31));    /* z=15 */ 
		   mat25V.add(new Integer(32));    /* z=16 */ 
		   mat25V.add(new Integer(35));    /* z=17 */ 
		   mat25V.add(new Integer(36));    /* z=18 */ 
		   mat25V.add(new Integer(39));    /* z=19 */ 
		   mat25V.add(new Integer(40));    /* z=20 */ 
		   mat25V.add(new Integer(45));    /* z=21 */ 
		   mat25V.add(new Integer(46));    /* z=22 */ 
		   mat25V.add(new Integer(50));    /* z=23 */ 
		   mat25V.add(new Integer(50));    /* z=24 */ 
		   mat25V.add(new Integer(55));    /* z=25 */ 
		   mat25V.add(new Integer(54));    /* z=26 */ 
		   mat25V.add(new Integer(59));    /* z=27 */ 
		   mat25V.add(new Integer(58));    /* z=28 */ 
		   mat25V.add(new Integer(63));    /* z=29 */ 
		   mat25V.add(new Integer(64));    /* z=30 */ 
		   mat25V.add(new Integer(69));    /* z=31 */ 
		   mat25V.add(new Integer(70));    /* z=32 */ 
		   mat25V.add(new Integer(75));    /* z=33 */ 
		   mat25V.add(new Integer(74));    /* z=34 */ 
		   mat25V.add(new Integer(79));    /* z=35 */ 
		   mat25V.add(new Integer(78));    /* z=36 */ 
		   mat25V.add(new Integer(85));    /* z=37 */ 
		   mat25V.add(new Integer(84));    /* z=38 */ 
		   mat25V.add(new Integer(89));    /* z=39 */ 
		   mat25V.add(new Integer(90));    /* z=40 */ 
		   mat25V.add(new Integer(93));    /* z=41 */ 
		   mat25V.add(new Integer(92));    /* z=42 */ 
		   mat25V.add(new Integer(99));    /* z=43 */ 
		   mat25V.add(new Integer(96));    /* z=44 */ 
		   mat25V.add(new Integer(103));    /* z=45 */ 
		   mat25V.add(new Integer(102));    /* z=46 */ 
		   mat25V.add(new Integer(107));    /* z=47 */ 
		   mat25V.add(new Integer(106));    /* z=48 */ 
		   mat25V.add(new Integer(113));    /* z=49 */ 
		   mat25V.add(new Integer(112));    /* z=50 */ 
		   mat25V.add(new Integer(121));    /* z=51 */ 
		   mat25V.add(new Integer(120));    /* z=52 */ 
		   mat25V.add(new Integer(127));    /* z=53 */ 
		   mat25V.add(new Integer(124));    /* z=54 */ 
		   mat25V.add(new Integer(133));    /* z=55 */ 
		   mat25V.add(new Integer(130));    /* z=56 */ 
		   mat25V.add(new Integer(138));    /* z=57 */ 
		   mat25V.add(new Integer(136));    /* z=58 */ 
		   mat25V.add(new Integer(141));    /* z=59 */ 
		   mat25V.add(new Integer(142));    /* z=60 */ 
		   mat25V.add(new Integer(139));    /* z=61 */ 
		   mat25V.add(new Integer(144));    /* z=62 */ 
		   mat25V.add(new Integer(151));    /* z=63 */ 
		   mat25V.add(new Integer(152));    /* z=64 */ 
		   mat25V.add(new Integer(159));    /* z=65 */ 
		   mat25V.add(new Integer(156));    /* z=66 */ 
		   mat25V.add(new Integer(165));    /* z=67 */ 
		   mat25V.add(new Integer(162));    /* z=68 */ 
		   mat25V.add(new Integer(169));    /* z=69 */ 
		   mat25V.add(new Integer(168));    /* z=70 */ 
		   mat25V.add(new Integer(175));    /* z=71 */ 
		   mat25V.add(new Integer(174));    /* z=72 */ 
		   mat25V.add(new Integer(180));    /* z=73 */ 
		   mat25V.add(new Integer(180));    /* z=74 */ 
		   mat25V.add(new Integer(185));    /* z=75 */ 
		   mat25V.add(new Integer(184));    /* z=76 */ 
		   mat25V.add(new Integer(191));    /* z=77 */ 
		   mat25V.add(new Integer(190));    /* z=78 */ 
		   mat25V.add(new Integer(197));    /* z=79 */ 
		   mat25V.add(new Integer(196));    /* z=80 */ 
		   mat25V.add(new Integer(203));    /* z=81 */ 
		   mat25V.add(new Integer(204));    /* z=82 */ 
		   mat25V.add(new Integer(209));    /* z=83 */ 
		   mat25V.add(new Integer(206));    /* z=84 */ 
		   mat25V.add(new Integer(203));    /* z=85 */ 
		   mat25V.add(new Integer(211));    /* z=86 */ 
		   mat25V.add(new Integer(212));    /* z=87 */ 
		   mat25V.add(new Integer(223));    /* z=88 */ 
		   mat25V.add(new Integer(225));    /* z=89 */ 
		   mat25V.add(new Integer(227));    /* z=90 */ 
		   mat25V.add(new Integer(229));    /* z=91 */ 
		   mat25V.add(new Integer(234));    /* z=92 */ 
		   mat25V.add(new Integer(230));    /* z=93 */ 
		   mat25V.add(new Integer(235));    /* z=94 */ 
		   mat25V.add(new Integer(235));    /* z=95 */ 
		   mat25V.add(new Integer(240));    /* z=96 */ 
		   mat25V.add(new Integer(240));    /* z=97 */ 
		   mat25V.add(new Integer(240));    /* z=98 */ 
	}
	
	public static int getZ(String EN) {
		try {
			EN=EN.toUpperCase().trim();
			int Z=elementSymbolV.indexOf(EN);
			if(Z>=0)
				return Z;
			
			EN=EN.charAt(0)+EN.substring(1).toLowerCase();
			return elementNameV.indexOf(EN);
		}catch(Exception e) {
			
		}
		return -1;
	}
	
	public static int getDefaultA(String EN) {
		try {
			int[] out=getDefaultAZ(EN);
			return out[0];
		}catch(Exception e) {
			
		}
		
		return -1;
	}
	
	public static int[] getDefaultAZ(String EN) {
		int[] out=new int[2];
		out[0]=-1;
		out[1]=-1;
		
		try {
			int defaultA=-1;
			int Z=-1;
			
			String EN0=EN.trim();
			EN=EN0.toUpperCase();
			
			if(EN0.equals("p") || EN.equals("PP") || EN.equals("PROTON"))
				EN="H";
					
			else if(EN0.equals("n") || EN.equals("NN")||EN.equals("NEUTRON"))
				EN="NN";
			
			if(elementDefaultMassNumberMap.containsKey(EN)) {
				Z=elementSymbolV.indexOf(EN);
				defaultA=elementDefaultMassNumberMap.get(EN).intValue();
			}else if(EN.length()>2) {//check if element name
				EN=EN.charAt(0)+EN.substring(1).toLowerCase();
				Z=elementNameV.indexOf(EN);
				if(Z>=0) {
					EN=elementSymbolV.get(Z);
					defaultA=elementDefaultMassNumberMap.get(EN).intValue();
				}
	
			}

			out[0]=defaultA;
			out[1]=Z;
		}catch(Exception e) {
			
		}
		return out;
	}
	
	public static int getDefaultAForZ(int Z) {
		try {
			String EN=elementSymbolV.get(Z);
			return getDefaultA(EN);
		}catch(Exception e) {
			
		}
		return -1;
	}
}
