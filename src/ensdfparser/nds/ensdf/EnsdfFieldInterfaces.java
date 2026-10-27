package ensdfparser.nds.ensdf;

import java.util.HashMap;

import ensdfparser.ensdf.*;
import ensdfparser.ensdf.Record;

/*
 * contains interfaces for all get functions of ENSDF records
 */
public class EnsdfFieldInterfaces <T extends ensdfparser.ensdf.Record>{
	public static interface EnsdfGetterS{public String get(Object rec); }//getter for string values
	public static interface EnsdfGetterD{public double get(Object rec);}//getter for float values
    
	public static HashMap<String,EnsdfGetterS> mapOfGetterS=new HashMap<String,EnsdfGetterS>();
	public static HashMap<String,EnsdfGetterD> mapOfGetterD=new HashMap<String,EnsdfGetterD>();
	
	public static void initializeGetterMap(){
		//common fields
		mapOfGetterS.put(    "ES", (r)->((Record)r).ES());        mapOfGetterD.put(    "EF", (r)->((Record)r).EF());
		mapOfGetterS.put(   "DES", (r)->((Record)r).DES());       mapOfGetterD.put(   "DEF", (r)->((Record)r).DEF());
		mapOfGetterS.put(     "F", (r)->((Record)r).flag());      
		mapOfGetterS.put(   "COIN", (r)->((Record)r).coinS());      
		mapOfGetterS.put(     "Q", (r)->((Record)r).q());      

		//level fields
		mapOfGetterS.put(      "J", (r)->((Level)r).JPiS());
		mapOfGetterS.put(      "T", (r)->((Level)r).T12S());     mapOfGetterD.put(     "T", (r)->((Level)r).T12D());
		mapOfGetterS.put(     "DT", (r)->((Level)r).DT12S());     mapOfGetterD.put(    "DT", (r)->((Level)r).DT12D());
		mapOfGetterS.put(     "TU", (r)->((Level)r).T12Unit());      
		mapOfGetterS.put(      "L", (r)->((Level)r).lS());      
		mapOfGetterS.put(      "S", (r)->((Level)r).sS());        
		mapOfGetterS.put(     "DS", (r)->((Level)r).dsS());
		mapOfGetterS.put(     "MS", (r)->((Level)r).msS());      
		
		//gamma fields
		mapOfGetterS.put(     "RI", (r)->((Gamma)r).RIS());        mapOfGetterD.put(    "RI", (r)->((Gamma)r).RID());
		mapOfGetterS.put(    "DRI", (r)->((Gamma)r).DRIS());       mapOfGetterD.put(   "DRI", (r)->((Gamma)r).DRID());
		mapOfGetterS.put(      "M", (r)->((Gamma)r).MS());         
		mapOfGetterS.put(     "MR", (r)->((Gamma)r).MRS());        
		mapOfGetterS.put(    "DMR", (r)->((Gamma)r).DMRS());      
		mapOfGetterS.put(     "CC", (r)->((Gamma)r).CCS());       mapOfGetterD.put(    "CC", (r)->((Gamma)r).CCF());
		mapOfGetterS.put(    "DCC", (r)->((Gamma)r).DCCS());      mapOfGetterD.put(   "DCC", (r)->((Gamma)r).DCCF());
		mapOfGetterS.put(     "TI", (r)->((Gamma)r).TIS());       mapOfGetterD.put(    "TI", (r)->((Gamma)r).syTID());
		mapOfGetterS.put(    "DTI", (r)->((Gamma)r).DTIS());      mapOfGetterD.put(   "DTI", (r)->((Gamma)r).DsyTID());

		//common decay fields		
		mapOfGetterS.put(     "IB", (r)->((Decay)r).RIS());        mapOfGetterD.put(    "IB", (r)->((Decay)r).ID());
		mapOfGetterS.put(    "DIB", (r)->((Decay)r).DRIS());       mapOfGetterD.put(   "DIB", (r)->((Decay)r).DID());
		mapOfGetterS.put(  "LOGFT", (r)->((Decay)r).LOGFTS());    
		mapOfGetterS.put( "DLOGFT", (r)->((Decay)r).DLOGFTS());  
		mapOfGetterS.put(     "UN", (r)->((Decay)r).unique());  
		
		//ECBP
		mapOfGetterS.put(     "IE", (r)->((ECBP)r).IES());        mapOfGetterD.put(    "IE", (r)->((ECBP)r).IBD());
		mapOfGetterS.put(    "DIE", (r)->((ECBP)r).DIES());       mapOfGetterD.put(   "DIE", (r)->((ECBP)r).DIED());
		mapOfGetterS.put(     "TI", (r)->((ECBP)r).ITS());        mapOfGetterD.put(    "TI", (r)->((ECBP)r).ITD());
		mapOfGetterS.put(    "DTI", (r)->((ECBP)r).DITS());       mapOfGetterD.put(   "DTI", (r)->((ECBP)r).DITD());

        //Alpha
		mapOfGetterS.put(     "IA", (r)->((Alpha)r).RIS());        mapOfGetterD.put(    "IA", (r)->((Alpha)r).ID());
		mapOfGetterS.put(    "DIA", (r)->((Alpha)r).DRIS());       mapOfGetterD.put(   "DIA", (r)->((Alpha)r).DID());
		mapOfGetterS.put(     "HF", (r)->((Alpha)r).HFS());      
		mapOfGetterS.put(    "DHF", (r)->((Alpha)r).DHFS());    

        
        //beta/EC-delayed particle
        //also for prompt particle decay is no delay
		mapOfGetterS.put(     "IP", (r)->((DParticle)r).RIS());    mapOfGetterD.put(    "IP", (r)->((DParticle)r).ID());
		mapOfGetterS.put(    "DIP", (r)->((DParticle)r).DRIS());   mapOfGetterD.put(    "IP", (r)->((DParticle)r).DID());
		mapOfGetterS.put(     "EI", (r)->((DParticle)r).EIS());   mapOfGetterD.put(    "EI", (r)->((DParticle)r).EIF());//for intermediate nuclide
		mapOfGetterS.put(     "TD", (r)->((DParticle)r).TS());    
		mapOfGetterS.put(    "TDU", (r)->((DParticle)r).TUNITS());  
		mapOfGetterS.put(    "DTD", (r)->((DParticle)r).DTS());   
		mapOfGetterS.put(     "LD", (r)->((DParticle)r).LS()); 

        
		//Parent
		mapOfGetterS.put(     "EP", (r)->((Parent)r).level().ES());     mapOfGetterD.put(     "EP", (r)->((Parent)r).level().EF());
		mapOfGetterS.put(    "DEP", (r)->((Parent)r).level().DES());    mapOfGetterD.put(    "DEP", (r)->((Parent)r).level().DEF());
		mapOfGetterS.put(     "JP", (r)->((Parent)r).level().JPiS());
		mapOfGetterS.put(     "TP", (r)->((Parent)r).level().T12S());  mapOfGetterD.put(     "TP", (r)->((Parent)r).level().T12D());
		mapOfGetterS.put(    "TPU", (r)->((Parent)r).level().T12Unit());  
		mapOfGetterS.put(    "DTP", (r)->((Parent)r).level().DT12S()); mapOfGetterD.put(    "DTP", (r)->((Parent)r).level().DT12D());
		mapOfGetterS.put(     "QP", (r)->((Parent)r).QS());             mapOfGetterD.put(     "QP", (r)->((Parent)r).QF());
		mapOfGetterS.put(    "DQP", (r)->((Parent)r).DQS());            mapOfGetterD.put(    "DQP", (r)->((Parent)r).DQF());
		mapOfGetterS.put(    "ION", (r)->((Parent)r).ionS()); 

		
        //Q-Value
		mapOfGetterS.put(     "Q-", (r)->((QValue)r).QBMS());     mapOfGetterD.put(     "Q-", (r)->((QValue)r).QBMF()); 
		mapOfGetterS.put(    "DQ-", (r)->((QValue)r).DQBMS());    mapOfGetterD.put(    "DQ-", (r)->((QValue)r).DQBMF());
		mapOfGetterS.put(     "SN", (r)->((QValue)r).SNS());	  mapOfGetterD.put(     "SN", (r)->((QValue)r).SNF());
		mapOfGetterS.put(    "DSN", (r)->((QValue)r).DSNS());     mapOfGetterD.put(    "DSN", (r)->((QValue)r).DSNF()); 
		mapOfGetterS.put(     "SP", (r)->((QValue)r).SPS());  	  mapOfGetterD.put(     "SP", (r)->((QValue)r).SPF());  
		mapOfGetterS.put(    "DSP", (r)->((QValue)r).DSPS());     mapOfGetterD.put(    "DSP", (r)->((QValue)r).DSPF()); 
		mapOfGetterS.put(     "QA", (r)->((QValue)r).QAS());      mapOfGetterD.put(     "QA", (r)->((QValue)r).QAF());  
		mapOfGetterS.put(    "DQA", (r)->((QValue)r).DQAS());     mapOfGetterD.put(    "DQA", (r)->((QValue)r).DQAF()); 
		mapOfGetterS.put(   "QREF", (r)->((QValue)r).QRef()); 	  


        
		//Norm 
		mapOfGetterS.put(     "NR", (r)->((Normal)r).NRS());     mapOfGetterD.put(     "NR", (r)->((Normal)r).NRD());  
		mapOfGetterS.put(    "DNR", (r)->((Normal)r).DNRS());    mapOfGetterD.put(    "DNR", (r)->((Normal)r).DNRD()); 
		mapOfGetterS.put(     "NT", (r)->((Normal)r).NTS());	 mapOfGetterD.put(     "NT", (r)->((Normal)r).NTD());	
		mapOfGetterS.put(    "DNT", (r)->((Normal)r).DNTS());    mapOfGetterD.put(    "DNT", (r)->((Normal)r).DNTD()); 
		mapOfGetterS.put(     "BR", (r)->((Normal)r).BRS());  	 mapOfGetterD.put(     "BR", (r)->((Normal)r).BRD());  
		mapOfGetterS.put(    "DBR", (r)->((Normal)r).DBRS());    mapOfGetterD.put(    "DBR", (r)->((Normal)r).DBRD()); 
		mapOfGetterS.put(     "NB", (r)->((Normal)r).NBS());     mapOfGetterD.put(     "NB", (r)->((Normal)r).NBD());  
		mapOfGetterS.put(    "DNB", (r)->((Normal)r).DNBS());    mapOfGetterD.put(    "DNB", (r)->((Normal)r).DNBD()); 
		mapOfGetterS.put(     "NP", (r)->((Normal)r).NPS());     mapOfGetterD.put(     "NP", (r)->((Normal)r).NPD());  
		mapOfGetterS.put(    "DNP", (r)->((Normal)r).DNPS());    mapOfGetterD.put(    "DNP", (r)->((Normal)r).DNPD()); 

		//PNorm 
		mapOfGetterS.put(   "NRBR", (r)->((Normal)r).NRBRS());     mapOfGetterD.put(   "NRBR", (r)->((Normal)r).NRBRD()); 
		mapOfGetterS.put(  "DNRBR", (r)->((Normal)r).DNRBRS());    mapOfGetterD.put(  "DNRBR", (r)->((Normal)r).DNRBRD());
		mapOfGetterS.put(   "NTBR", (r)->((Normal)r).NTBRS());	   mapOfGetterD.put(   "NTBR", (r)->((Normal)r).NTBRD());
		mapOfGetterS.put(  "DNTBR", (r)->((Normal)r).DNTBRS());    mapOfGetterD.put(  "DNTBR", (r)->((Normal)r).DNTBRD());
		mapOfGetterS.put(   "NBBR", (r)->((Normal)r).NBBRS());     mapOfGetterD.put(   "NBBR", (r)->((Normal)r).NBBRD()); 
		mapOfGetterS.put(  "DNBBR", (r)->((Normal)r).DNBBRS());    mapOfGetterD.put(  "DNBBR", (r)->((Normal)r).DNBBRD());

	}
	
	public static EnsdfGetterS findGetterS(String fieldName) {
		EnsdfGetterS getter=null;
		try {
			getter=mapOfGetterS.get(fieldName);
			getter.getClass();
		}catch(Exception e) {
			if(mapOfGetterS.isEmpty())
				initializeGetterMap();
			
			getter=mapOfGetterS.get(fieldName);
		}
		
		return getter;
	}

	public static EnsdfGetterD findGetterF(String fieldName) {
		EnsdfGetterD getter=null;
		try {
			getter=mapOfGetterD.get(fieldName);
			getter.getClass();
		}catch(Exception e) {
			if(mapOfGetterD.isEmpty())
				initializeGetterMap();
			
			getter=mapOfGetterD.get(fieldName);
		}
		
		return getter;
	}
	
	public static String getS(Object rec,String fieldName) {
		String S="";
		try {
			S=mapOfGetterS.get(fieldName).get(rec);
		}catch(Exception e) {
			if(mapOfGetterS.isEmpty()) {
				try {
					initializeGetterMap();
					S=mapOfGetterS.get(fieldName).get(rec);
				}catch(Exception e1) {}

			}
		}
		
		return S;
	}

	public static double getD(Object rec,String fieldName) {
		double v=-1;
		try {
			v=mapOfGetterD.get(fieldName).get(rec);
		}catch(Exception e) {
			if(mapOfGetterD.isEmpty()) {
				try {
					initializeGetterMap();
					v=mapOfGetterD.get(fieldName).get(rec);
				}catch(Exception e1) {}

			}

		}
		
		return v;
	}
	
	
}
