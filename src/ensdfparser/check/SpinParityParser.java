package ensdfparser.check;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import ensdfparser.calc.BXLBase;
import ensdfparser.calc.UncertaintyParser;
import ensdfparser.calc.XDX;
import ensdfparser.ensdf.Alpha;
import ensdfparser.ensdf.Beta;
import ensdfparser.ensdf.Decay;
import ensdfparser.ensdf.ECBP;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.Gamma;
import ensdfparser.ensdf.JPI;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.Parent;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

public class SpinParityParser {
    private ENSDF ens=null;
    private Vector<Gamma> gammasForJPI=new Vector<Gamma>();//store gammas used to make JPI assignment of current level
    private Vector<Gamma> keyGammasForJPI=new Vector<Gamma>();
    
    private Decay decayForJPI=null;
    
    private String LTransferForJPI="";
    
    private LinkedHashMap<Level,Vector<Gamma>> mapOflevelGammasForJPI=new LinkedHashMap<Level,Vector<Gamma>>();//store gammas used to make JPI assignment
    
    private LinkedHashMap<Level,Decay> mapOflevelDecayForJPI=new LinkedHashMap<Level,Decay>();//store decay used to make JPI assignment

    private LinkedHashMap<Level,String> mapOflevelLTransferForJPI=new LinkedHashMap<Level,String>();//store L-transfer used to make JPI assignment
    
	
	Vector<Gamma> allUsedGammasV=new Vector<Gamma>();//all gammas that have been used for JPI assignment 
    
    //////////////////////////////////
    //private double defaultT12=0.01E-9;//in second, default value if T1/2 not known for finding possible mult
    //////////////////////////////////
    
    private double minT12=1.0E-14;//10 fs
    private double maxT12=1.0E-9;//  1 ns
    
    private TransferInfo transfer=null;
    
    public SpinParityParser(ENSDF ens) {
        this.ens=ens;
        transfer=new TransferInfo(ens);
    }   

    //public double defaultT12() {return defaultT12;}
    //public void setDefaultT12(double t) {defaultT12=t;}
    
    public boolean isTransfer() {return transfer.isTransfer();}
    public String decayType() {return ens.decayRecordType();}
   
    public boolean parseJPIsFromAllData() {
        Vector<String> jsV=null;
        boolean hasSet=false;
        try {

        	mapOflevelDecayForJPI.clear();
        	mapOflevelLTransferForJPI.clear();
        	
        	Parent p=null;
        	String decayType=decayType();
        	if(ens.nParents()>0 && decayType.length()>0 && "EBA".contains(decayType)) {
        		p=ens.parentAt(0);
        	}

        	//first, scan all levels one time to determine JPI based on data other than gammas, which don't have circular argument issue
            for(Level lev:ens.levelsV()) {
            	
            	decayForJPI=null;
            	LTransferForJPI="";
            	
        		Vector<JPI> jpV1=findJPVFromDecays(p,lev);
        		Vector<JPI> jpV2=findJPVFromLTransfer(lev);
                
        		/*
        		if(ens.DSId0().contains("(N,AG)")) {
        		//if(jpV1==null || jpV2==null)
        			System.out.println("##########SpinParityParser 84: DSID="+ens.DSId()+" lev="+lev.ES()+"  "+(jpV1==null)+"  "+(jpV2==null)+" "+jpV1.size());
        		    System.out.println("    altJS="+lev.altJPiS());
        			for(JPI jp:jpV1) System.out.println("#   "+jp.js()+" "+jpV1.size());
        		    for(JPI jp:jpV2) System.out.println("@   "+jp.js()+" "+jpV2.size());
        		}
        		*/
                try {                	
                	if(decayForJPI!=null)
                		mapOflevelDecayForJPI.put(lev,decayForJPI);
                	
                    if(LTransferForJPI.length()>0)
                    	mapOflevelLTransferForJPI.put(lev,LTransferForJPI);
                }catch(Exception e) {}
                
        		
                String jps="";
                if(jpV1!=null && jpV2!=null) {
            		Vector<JPI> mergedJPV=JPI.mergeJPV(jpV1, jpV2, false);//false for an "AND" operation      		
                    jsV=JPV2JSV(mergedJPV);
                    jps=JPI.combineJSVtoStr(jsV);
                    if(jps==null)
                    	jps="NA";
                }else {
                	jps="NA";//no JPI can be assigned
                }

                /*
                for(String s:jpV)
                    jps+=s+",";

                if(jps.endsWith(","))
                    jps=jps.substring(0,jps.length()-1);
                */
                
                //if(lev.ES().equals("4101.7"))
                //System.out.println("SpinParityParser 105: dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+"  isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+jps);
                
                
                lev.setAltJPiS(jps.trim());
                
                if(!hasSet && jps.length()>0)
                	hasSet=true;
            }
        	
            boolean useAltJPS=true;
            boolean hasSet1=parseJPIsFromGammas(useAltJPS);
            
            //for(Level lev:ens.levelsV()) 
            //	if(lev.ES().equals("4101.7"))          
            //		System.out.println("SpinParityParser 117: dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+"  isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+lev.altJPiS());

            
            hasSet=hasSet||hasSet1;
            
        }catch(Exception e) {
        	
        }
   
        return hasSet;
    }
    
    public boolean parseJPIsFromAllData_old() {
        Vector<String> jsV=null;
        boolean hasSet=false;
        try {
        	Parent p=null;
        	if(ens.nParents()>0)
        		p=ens.parentAt(0);
        	
        	mapOflevelGammasForJPI.clear();
        	mapOflevelDecayForJPI.clear();
        	mapOflevelLTransferForJPI.clear();
        	
            for(Level lev:ens.levelsV()) {
            	
            	decayForJPI=null;
            	LTransferForJPI="";
            	gammasForJPI.clear();//gammas are key Gammas that constrain JPI range
            	//keyGammasForJPI.clear();
            	
                jsV=findJSVFromAllData(p,lev);//gammasForJPI reset in this call
                
                try {
                	Vector<Gamma> tempV=mapOflevelGammasForJPI.get(lev);
                	if(tempV==null) {
                		tempV=new Vector<Gamma>();
                		mapOflevelGammasForJPI.put(lev, tempV);
                	}
                	
                	tempV.clear();
                	tempV.addAll(gammasForJPI);
                	
                	if(decayForJPI!=null)
                		mapOflevelDecayForJPI.put(lev,decayForJPI);
                	
                    if(LTransferForJPI.length()>0)
                    	mapOflevelLTransferForJPI.put(lev,LTransferForJPI);
                }catch(Exception e) {}
                

                String jps=JPI.combineJSVtoStr(jsV);
                /*
                for(String s:jpV)
                    jps+=s+",";

                if(jps.endsWith(","))
                    jps=jps.substring(0,jps.length()-1);
                */
                
                //System.out.println("SpinParityParser 64: dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+"  isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+jps);
                
                
                lev.setAltJPiS(jps.trim());
                
                if(!hasSet && jps.length()>0)
                	hasSet=true;
            }
        }catch(Exception e) {
        	
        }
   
        return hasSet;
    }
    
    public boolean parseJPIsFromGammasAndDecays() {
        Vector<String> jsV=null;
        boolean hasSet=false;
                
        try {
        	Parent p=null;
        	if(ens.nParents()>0)
        		p=ens.parentAt(0);
        	
        	mapOflevelGammasForJPI.clear();
        	mapOflevelDecayForJPI.clear();
        	
            for(Level lev:ens.levelsV()) {
            	decayForJPI=null;
            	
                jsV=findJSVFromGammasAndDecays(p,lev);//gammasForJPI reset in this call
                
                try {
                	Vector<Gamma> tempV=mapOflevelGammasForJPI.get(lev);
                	if(tempV==null) {
                		tempV=new Vector<Gamma>();
                		mapOflevelGammasForJPI.put(lev, tempV);
                	}
                	
                	tempV.clear();
                	tempV.addAll(gammasForJPI);
                	
                	if(decayForJPI!=null)
                		mapOflevelDecayForJPI.put(lev,decayForJPI);
                }catch(Exception e) {}
                

                String jps="";
                if(jsV!=null) {
                	jps=JPI.combineJSVtoStr(jsV);
                }else {
                	jps="NA";
                }
                
                /*
                for(String s:jpV)
                    jps+=s+",";

                if(jps.endsWith(","))
                    jps=jps.substring(0,jps.length()-1);
                */
                
                //System.out.println("SpinParityParser 64: dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+"  isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+jps);
                
                
                lev.setAltJPiS(jps.trim());
                
                if(!hasSet && jps.length()>0)
                	hasSet=true;
            }
        }catch(Exception e) {
        	
        }
   
        return hasSet;
    }
    public boolean parseJPIsFromGammas() {
    	return parseJPIsFromGammas(false);
    }

    /**
     * 
     * @param useAltJPS: use altJPS of each level for the first scan (then always use altJPS in the following cycles if any)
     * @param mergeExisting: merge JPIs from gammas with existing JPI of each level
     * @return
     */
    public boolean parseJPIsFromGammas(boolean useAltJPS) {
        Vector<String> jsV=null;
        boolean hasSet=false;
        
        try {
        	
        	/*
        	//debug
        	for(int i=0;i<ens.levelsV().size();i++) {
        		Level lev=ens.levelsV().get(i);
        		//if(lev.ES().equals("4101.2"))
        		System.out.println("SpinParityParser 263: DSID="+ens.DSId()+" lev="+lev.ES()+" JPI="+lev.JPiS()+" altJP="+lev.altJPiS());
        	}
        	*/
        	
        	mapOflevelGammasForJPI.clear();       	
        	
        	
        	Vector<Level> noJPILevelsV=new Vector<Level>();//levels that can't be assigned with any JPI, e.g., conflicting data
        	boolean hasAnyChange=true;
        	
        	int nCycles=0;
        	while(hasAnyChange && nCycles<5) {
        		
        		allUsedGammasV.clear();
        		
        		hasAnyChange=false;
        		//for low to top levels, determine JPI using decaying gammas and others (but not feeding gammas)
            	for(int i=0;i<ens.levelsV().size();i++) {
            		Level lev=ens.levelsV().get(i);
            		
                    jsV=findJSVFromGammas(lev,"DECAYING",useAltJPS);//gammasForJPI reset in this call
                    
                    String jps="";
                    if(jsV!=null) {
                        jps=JPI.combineJSVtoStr(jsV);
                        
                        /*
                        //debug
                        if(lev.ES().equals("597.1")) {
                        	for(String s:jsV)
                        		System.out.println("SpinParityParser 317: combined jps="+jps+"  this part js="+s+"  size="+jsV.size());
                        }
                        */
                        
                        String altJPS=lev.altJPiS();
                        int cp=JPI.compareJPS(jps, altJPS);
                        
                        //if(lev.ES().equals("2386.9"))
                        //System.out.println("SpinParityParser 289: nCycles="+nCycles+"  cp="+cp+" jps="+jps+" lev.altJPS="+altJPS+" jsV "+jsV.size());
                        
                        //debug
                        /*
                        if(lev.ES().equals("597.1")) {
                        //if(lev.ES().equals("4493.5") || lev.ES().equals("4101.2")) {
                        	System.out.println("  SpinParityParser 323: DSID="+ens.DSId()+" lev="+lev.ES()+" lev.jps="+lev.JPiS()+" altJPS="+lev.altJPiS()+" jps="+jps
                        			+" gammasForJPI size="+gammasForJPI.size());
                        	for(Gamma g:gammasForJPI)
                        		System.out.println("         DSID="+ens.DSId()+" lev="+lev.ES()+" g="+g.ES()+" from EL="+g.ILS()+" used="+allUsedGammasV.contains(g));
                        }
                        */
                        
                        if(cp!=-1 && !jps.equals(altJPS)) {
                        	//   1 jps is better than altJPS
                        	//  -1 altJPS is better
                        	//-10000 not comparable
                        	//others, jps and altJPS overlap
                        	if(cp>=-10) {
                        		//if cp==-10000, not comparable, no need for any further scan for this level
                        		hasAnyChange=true;          
                        	}       
                        	
                        	altJPS=JPI.mergeJPS(jps,altJPS,false);
                        	lev.setAltJPiS(altJPS);
                        	
                        	//if(lev.ES().equals("597.1"))
                        	//System.out.println("SpinParityParser 341: nCycles="+nCycles+"  cp="+cp+" jps="+jps+" lev.altJPS="+altJPS+" lev.JPI="+lev.JPiS());
                            
                        	
                        	hasSet=true;
                        }
                    }else {
                    	if(!noJPILevelsV.contains(lev))
                    		noJPILevelsV.add(lev);
                    	
                    	lev.setAltJPiS("NA");
                    	hasSet=true;
                    }
                    

                     
                    /*
                    //debug
                    if(lev.ES().equals("597.1")) {
                    //if(lev.ES().equals("4493.5") || lev.ES().equals("4101.2")) {
                    	System.out.println("  SpinParityParser 315: DSID="+ens.DSId()+" lev="+lev.ES()+" gammasForJPI size="+gammasForJPI.size());
                    	for(Gamma g:gammasForJPI)
                    		System.out.println("         DSID="+ens.DSId()+" lev="+lev.ES()+" g="+g.ES()+" from EL="+g.ILS()+" used="+allUsedGammasV.contains(g));
                    }
                    */
                    
                    try {
                    	Vector<Gamma> tempV=mapOflevelGammasForJPI.get(lev);
                    	if(tempV==null) {
                    		tempV=new Vector<Gamma>();
                    		mapOflevelGammasForJPI.put(lev, tempV);
                    	}
                    	if(gammasForJPI.size()>0) {
                        	tempV.clear();
                        	tempV.addAll(gammasForJPI);//gammasForJPI are decaying gammas
                        	
                        	for(Gamma g:gammasForJPI) {
                        		if(!allUsedGammasV.contains(g))
                        			allUsedGammasV.add(g);
                        	}
                    	}

                    }catch(Exception e) {}
            		
            	}
            	
            	//System.out.println("        SpinParityParser 383: --------"+nCycles);
            	
        		//for top to low levels, further constrain JPI using feeding gammas only
            	for(int i=ens.levelsV().size()-1;i>=0;i--) {
            		Level lev=ens.levelsV().get(i);
            		if(noJPILevelsV.contains(lev))
            			continue;
            		
                    jsV=findJSVFromGammas(lev,"FEEDING",useAltJPS);//gammasForJPI reset in this call
                    
                    String jps="";
                    if(jsV!=null) {
                        jps=JPI.combineJSVtoStr(jsV);
                        
                        String altJPS=lev.altJPiS();
                        int cp=JPI.compareJPS(jps, altJPS);
                        
                        //if(lev.ES().equals("2386.9"))
                        //System.out.println("SpinParityParser 398: nCycles="+nCycles+"  cp="+cp+" jps="+jps+" lev.altJPS="+altJPS);
                        
                        if(cp!=-1 && !jps.equals(altJPS)) {
                        	//   1 jps is better than altJPS
                        	//  -1 altJPS is better
                        	//-10000 not comparable
                        	//others (10 or -10), jps and altJPS overlap
                        	if(cp>=-10) {
                        		//if cp==-10000, not comparable, no need for any further scan for this level
                        		hasAnyChange=true;          
                        	}
                        	
                        	altJPS=JPI.mergeJPS(jps,altJPS,false);
                        	lev.setAltJPiS(altJPS);
                        	
                            //if(lev.ES().equals("4101.2"))
                            //    System.out.println("SpinParityParser 348: nCycles="+nCycles+"  cp="+cp+" jps="+jps+" lev.altJPS="+altJPS);
                            
                        	hasSet=true;
                        }
                    }else {
                      	if(!noJPILevelsV.contains(lev))
                    		noJPILevelsV.add(lev);
                      	
                    	lev.setAltJPiS("NA");
                    	hasSet=true;
                    }
 
                    
                    try {
                    	Vector<Gamma> tempV=mapOflevelGammasForJPI.get(lev);
                    	if(tempV==null) {
                    		tempV=new Vector<Gamma>();
                    		mapOflevelGammasForJPI.put(lev, tempV);
                    	}
                    	
                    	if(gammasForJPI.size()>0) {
                        	//tempV.clear();
                        	tempV.addAll(gammasForJPI);//gammasForJPI are feeding gammas
                        	
                        	for(Gamma g:gammasForJPI) {
                        		if(!allUsedGammasV.contains(g))
                        			allUsedGammasV.add(g);
                        	}
                    	}

                    	/*
                    	if(lev.ES().equals("3860.4")) {
    						System.out.println("SpinParityParser 444: DSID="+ens.DSId()+"  lev="+lev.ES()+"  gammasForJPI size="+tempV.size());
    						for(Gamma g:tempV)
    							System.out.println("  g="+g.ES()+"  from "+g.ILS());
                    	}
                    	*/
                    	
                    }catch(Exception e) {}
            	}
            	//After the first scan, always use altJPS in the following cycles if any
            	useAltJPS=true;
            	
            	nCycles++;
        	}
        	
        }catch(Exception e) {
        	
        }
   
        return hasSet;
    }
    
    public boolean parseJPIsFromGammas_old(String option,boolean useAltJPS) {
        Vector<String> jsV=null;
        boolean hasSet=false;
        
        try {
        	mapOflevelGammasForJPI.clear();

        	
            for(Level lev:ens.levelsV()) {
                jsV=findJSVFromGammas(lev,option,useAltJPS);//gammasForJPI reset in this call
                
                try {
                	Vector<Gamma> tempV=mapOflevelGammasForJPI.get(lev);
                	if(tempV==null) {
                		tempV=new Vector<Gamma>();
                		mapOflevelGammasForJPI.put(lev, tempV);
                	}
                	
                	tempV.clear();
                	tempV.addAll(gammasForJPI);
                }catch(Exception e) {}
                
                String jps=JPI.combineJSVtoStr(jsV);
                /*
                for(String s:jpV)
                    jps+=s+",";

                if(jps.endsWith(","))
                    jps=jps.substring(0,jps.length()-1);
                */
                
                //System.out.println("dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+" spin="+tr.targetSpin()+" isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+jps);
                
                lev.setAltJPiS(jps.trim());
                
                if(!hasSet && jps.length()>0)
                	hasSet=true;
            }
        }catch(Exception e) {
        	
        }
   
        return hasSet;
    }
    
    public boolean parseJPIsFromDecays() {
        Vector<String> jsV=null;
        boolean hasSet=false;
        
        try {
        	mapOflevelDecayForJPI.clear();
        	
        	Parent p=ens.parentAt(0);
            for(Level lev:ens.levelsV()) {
            	decayForJPI=null;
            	
                jsV=findJSVFromDecays(p,lev); 
                
                if(decayForJPI!=null)
                	mapOflevelDecayForJPI.put(lev, decayForJPI);
                
                String jps="";
                if(jsV!=null)
                	jps=JPI.combineJSVtoStr(jsV);
                else
                	jps="NA";
                /*
                for(String s:jpV)
                    jps+=s+",";

                if(jps.endsWith(","))
                    jps=jps.substring(0,jps.length()-1);
                */
                
                //System.out.println("dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+" spin="+tr.targetSpin()+" isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+jps);
                
                lev.setAltJPiS(jps.trim());
                
                if(!hasSet && jps.length()>0)
                	hasSet=true;
            }
        }catch(Exception e) {
        	
        }
   
        return hasSet;
    }
    
    public boolean parseJPIsFromLTransfers() {
    	if(!isTransfer())
    		return false;
    	
        Vector<String> jsV=null;
        boolean hasSet=false;
        
        try {
        	mapOflevelLTransferForJPI.clear();
        	
            for(Level lev:ens.levelsV()) {
            	LTransferForJPI="";
            	
            	//System.out.println("SpinParityParser 464: DSID="+ens.DSId()+"  Lev="+lev.ES()+" LS="+lev.lS());
            	
                jsV=findJSVFromLTransfer(lev);
                
                //System.out.println("SpinParityParser 468: DSID="+ens.DSId()+"  Lev="+lev.ES()+" LS="+lev.lS());
                
                if(LTransferForJPI.length()>0)
                	mapOflevelLTransferForJPI.put(lev,LTransferForJPI);

                
                String jps="";
                if(jsV!=null) {
                    jps=JPI.combineJSVtoStr(jsV);
                }else {
                	jps="NA";
                }
       
                /*
                for(String s:jpV)
                    jps+=s+",";

                if(jps.endsWith(","))
                    jps=jps.substring(0,jps.length()-1);
                */
                
                //System.out.println("dsid="+ens.DSId0()+" targetJPS="+ens.target().JPS()+" spin="+tr.targetSpin()+" isEvenEven="+ens.target().isEvenEven()+" level="+lev.ES()+"  altJPS="+jps);
                
                lev.setAltJPiS(jps.trim());
                
                if(!hasSet && jps.length()>0)
                	hasSet=true;
            }
        }catch(Exception e) {
        	
        }
   
        return hasSet;
    }
    
    public Vector<String> findJSVFromAllData(Parent p,Level lev){
    	try {
            Vector<JPI> jpV=findJPVFromAllData(p,lev);
            Vector<String> out=new Vector<String>();
            
            for(int i=0;i<jpV.size();i++) {
                out.add(jpV.get(i).js());
            }
            return out;
    	}catch(Exception e) {
    		
    	}
    	return null;

    }
    
    
    public Vector<JPI> findJPVFromAllData(Parent parent,Level lev){

    	try {
    		
        	Vector<JPI> out=new Vector<JPI>();
        	
    		Vector<JPI> jpV1=findJPVFromGammas(lev,"both",false);
    		Vector<JPI> jpV2=findJPVFromDecays(parent,lev);
    		Vector<JPI> jpV3=findJPVFromLTransfer(lev);
    		
    		//if(lev.ES().equals("2386.5")) 
    		//	System.out.println("##############");
    		
    		out=JPI.mergeJPV(jpV1, jpV2, false);//false for an "AND" operation
    		out=JPI.mergeJPV(out, jpV3,false);

    		/*
    		if(lev.ES().equals("4474")) {
    			for(JPI jp:jpV1) {
    				System.out.println("##from gamma: jp="+jp.js());
    			}
    			for(JPI jp:jpV2) {
    				System.out.println("@@from decay: jp="+jp.js());
    			}
    			for(JPI jp:out) {
    				System.out.println("$$merged: jp="+jp.js());
    			}
    		}
    		*/
    		return out;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public Vector<String> findJSVFromGammasAndDecays(Parent p,Level lev){
    	try {
            Vector<JPI> jpV=findJPVFromGammasAndDecays(p,lev);
            Vector<String> out=new Vector<String>();
            
            for(int i=0;i<jpV.size();i++) {
                out.add(jpV.get(i).js());
            }
            return out;
    	}catch(Exception e) {
    		
    	}
    	return null;

    }
    
    public Vector<JPI> findJPVFromGammasAndDecays(Parent parent,Level lev){
    	
    	try {
    		Vector<JPI> out=new Vector<JPI>();
    		
    		Vector<JPI> jpV1=findJPVFromGammas(lev,"both",false);
    		Vector<JPI> jpV2=findJPVFromDecays(parent,lev);
    			
    		//if(lev.ES().equals("2386.5")) 
    		//	System.out.println("##############");
    		
    		out=JPI.mergeJPV(jpV1, jpV2, false);//false for an "AND" operation
    		
    		/*
    		if(lev.ES().equals("5560.7")) {
    			for(JPI jp:jpV1) {
    				System.out.println("##from gamma: jp="+jp.js());
    			}
    			for(JPI jp:jpV2) {
    				System.out.println("@@from decay: jp="+jp.js());
    			}
    			for(JPI jp:out) {
    				System.out.println("$$merged: jp="+jp.js());
    			}
    		}
    		*/
    		return out;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    public Vector<String> findJSVFromLTransfer(Level lev){
    	try {
        	LTransferForJPI="";
        	
            Vector<String> out=new Vector<String>();
            if(!isTransfer())
            	return out;
            
            try {
                out=transfer.findLevelJPSFromLtransfer(lev);
                if(lev.lS().length()>0 && out!=null && out.size()>0)
                	LTransferForJPI=lev.lS();

                return out;
            }catch(Exception e) {
            	
            }
            
    	}catch(Exception e) {
    		
    	}
	
        return null;
    }
    
    public Vector<JPI> findJPVFromLTransfer(Level lev){
    	try {
            Vector<JPI> out=new Vector<JPI>();
            if(!isTransfer())
            	return out;
            
            Vector<String> jsV=findJSVFromLTransfer(lev);

            if(jsV.size()==0)
            	return out;
            
            for(String js:jsV) {
            	out.add(new JPI(js));
            }
    		
    		return out;
    	}catch(Exception e) {
    		
    	}
    	return null;

    }
    
    public Vector<String> findJSVFromDecays(Parent p,Level lev){
    	try {
            Vector<JPI> jpV=findJPVFromDecays(p,lev);
            Vector<String> out=new Vector<String>();
            
            for(int i=0;i<jpV.size();i++) {
                out.add(jpV.get(i).js());
            }
            return out;
    	}catch(Exception e) {
    		
    	}
    	return null;

    }
    
    public Vector<JPI> findJPVFromDecays(Parent parent,Level lev){
    	try {
    		Vector<JPI> out=findJPVFromDecays(parent,lev,true);
    		
    		if(out.isEmpty() && lev.altJPiS().isEmpty())
    			out=findJPVFromDecays(parent,lev,false);
    		
    		return out;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return null;

    }
    public Vector<JPI> findJPVFromDecays(Parent parent,Level lev,boolean ignoreQueDecay){
    	decayForJPI=null;
    	

    	try {
            Vector<JPI> out=new Vector<JPI>();
            
    		if(parent==null || parent.level().JPiS().isEmpty() || lev.DecaysV().size()==0)
    			return out;
    		
    		Decay d=lev.DecaysV().get(0);
            String qs=d.q();
            boolean isQue=!qs.isEmpty();
            
            if(qs.equals("S") || (qs.equals("?")&&ignoreQueDecay))
            	return out;
    		
    		SDS2XDX logft=null,HF=null,ai=null;
    	    int Z=lev.nucleus().z();
    	    String parentJS=parent.level().JPiS();
    	    
    		if(d instanceof Beta){
    			Beta b=(Beta)d;
    			logft=new SDS2XDX(b.LOGFTS(),b.DLOGFTS());
    			ai=new SDS2XDX(b.AIS(),b.DAIS());
    		}else if(d instanceof ECBP) {
    			ECBP ec=(ECBP)d;
    			logft=new SDS2XDX(ec.LOGFTS(),ec.DLOGFTS());
    			ai=new SDS2XDX(ec.AITS(),ec.DAITS());
    		}else if(d instanceof Alpha) {
    			Alpha a=(Alpha)d;
    			HF=new SDS2XDX(a.HFS(),a.DHFS());
    			ai=new SDS2XDX(a.AIS(),a.DAIS());
    		}else {
    			return out;
    		}
    		
    		Vector<String> LV=new Vector<String>();
    		boolean isWeak=false;
    		if(ai.x()>0) {
    			if(ai.dxl()<=0) {
    				if(ai.x()<2.0 || ai.ds().startsWith("L"))
    					isWeak=true;
    			}else if((ai.x()-ai.dxl())<2.0 || ai.ds().startsWith("L")) {
    				isWeak=true;
    			}
    		}else {
    			isWeak=true;
    		}
    		
    		if(logft!=null && logft.x()>0) {
    			//beta or EC/B+ decay
    			double bufferOffset=0.1;//for values too close to the limits  			
    			LV=LOGFT.findDeltaJPSForLogft(logft, Z, isWeak, isQue, bufferOffset, parentJS);
    		}else if(HF!=null && HF.x()>0) {
    			double x=HF.x();
    			double dx=HF.dx();
    			String s=HF.s();
    			String ds=HF.ds();
    			if(HF.dxu()>0) {
    				dx=HF.dxu();
    			}else if(HF.dx()<0) {
    				XDX xdx=UncertaintyParser.defaultUncParser().get(s, ds);
    				dx=xdx.dxu();
    			}
    			
    			if(dx>0) {
    				x=x+dx;
    				if(x<4.0) {
    					LV.add("0+");
    				}else if(HF.x()<4.0 && !ds.startsWith("G")){
    					LV.add("(0+)");
    				}
    			}
    		}
    		
    		if(LV.size()==0)
    			return out;
    		
    		/*
            if(lev.ES().equals("4493.5")) {
            	System.out.println(" LV size="+LV.size()+"  "+isQue);
            	for(String ls:LV)
            		System.out.println("    "+ls);
            }
            */
    		
            String jps=parent.level().JPiS();
            JPI jp=new JPI(jps);
            boolean isSingleJP=jp.isUniqueSpin();
            
            Vector<JPI> temp=JPI.JPICoupling(jps,LV.get(0));//L coupling to J, only take J-L and J+L, since L here is for deltaJ=|JP-JF|
            
            if(temp.size()>1 && isSingleJP) {
            	if(jp.spinValue()>=parseL(LV.get(0)))//J>=L
            		out.add(temp.get(0));
                out.add(temp.lastElement());
            }else {
                out.addAll(temp);
            }
            
            /*
            if(lev.ES().equals("4493.5")) {
            	System.out.println(" LV size="+LV.size()+"  "+isQue+" parent jps="+jps);
            	for(JPI jpi:out)
            		System.out.println("  L="+LV.get(0)+"  "+jpi.js());
            }
            */
            
            Vector<JPI> temp1=new Vector<JPI>();
            for(int i=1;i<LV.size();i++) {
                temp=JPI.JPICoupling(jps,LV.get(i));
                
                temp1=new Vector<JPI>();
                if(temp.size()>1 && isSingleJP) {
                	if(jp.spinValue()>=parseL(LV.get(i)))//J>=L
                		temp1.add(temp.get(0));
                    temp1.add(temp.lastElement());
                }else {
                    temp1.addAll(temp);     
                }
                
                out=JPI.mergeJPV(out,temp1,true);
            } 
            decayForJPI=d;
            
            return out;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}

        return null;
    }
    
    public Vector<String> findJSVFromGammas(Level lev,String option,boolean useAltJPS){
    	try {
    		
            Vector<JPI> jpV=findJPVFromGammas(lev,option,useAltJPS);
            Vector<String> out=new Vector<String>();

            for(int i=0;i<jpV.size();i++) {
                out.add(jpV.get(i).js());
            }
            return out;
    	}catch(Exception e) {
    		
    	}
    	
    	return null;

    }
	public Vector<JPI> findJPVFromGammas(Level lev,String option,boolean useAltJPS){
		try {
			Vector<JPI> out=findJPVFromGammas(lev,true,option,useAltJPS);

			if(out.isEmpty() && lev.altJPiS().isEmpty())
				out=findJPVFromGammas(lev,false,option,useAltJPS);
			
			return out;
		}catch(Exception e) {
			
		}
		return null;

	}
	
	/**
	 * 
	 * @param lev
	 * @param ignoreQueGam
	 * @param option: ="DECAYING" or "DEEXCITING" for using decaying gammas only
	 *                ="Feeding" for using feeding gammas only
	 *                =empty or others for using both
	 * @param useAltJPS: =true to use altJPS of "lev"
	 * @return
	 */
    @SuppressWarnings("unused")
	public Vector<JPI> findJPVFromGammas(Level lev,boolean ignoreQueGam,String option,boolean useAltJPS){
      
        gammasForJPI.clear();
        keyGammasForJPI.clear();
        
        boolean useDecayingGammas=true;
        boolean useFeedingGammas=true;
        option=option.toUpperCase().trim();
        if(!option.isEmpty()) {
            if(option.startsWith("DE"))
            	useFeedingGammas=false;
            else if(option.startsWith("FEED"))
            	useDecayingGammas=false;
        }


        try {
        	
            Vector<JPI> jpV=new Vector<JPI>();
            Vector<JPI> prevJPV=new Vector<JPI>();
            
            int count=0;

            int A=lev.nucleus().A();
            
            JPI prevLower=null,prevUpper=null;
            JPI currLower=null,currUpper=null;
            
            HashMap<Gamma,JPI> gammaLowerJPIMap=new HashMap<Gamma,JPI>();
            HashMap<Gamma,JPI> gammaUpperJPIMap=new HashMap<Gamma,JPI>();
            
            /*
            if(lev.ES().equals("197.272")) {
                System.out.println("SpinParityParser 1014: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
            }
            */
            
            if(useAltJPS) {
            	//set based on other data, like decay and transfer beforehand
            	//DO NOT USE lev.JPiS(), which could be taken from other places, like Adopted dataset
            	String tempJPS=lev.altJPiS();
            	prevJPV=JPI.parseJPS2JPV(tempJPS);
            	if(prevJPV.size()>0) {
            		prevLower=prevJPV.firstElement();
            		prevUpper=prevJPV.lastElement();
            	}

            	//if(lev.ES().equals("2386.9"))
                //for(JPI s:prevJPV) System.out.println("DSID="+ens.DSId()+" "+ lev.ES()+"$$ "+s.js()+"  "+tempJPS);
            }
            
            
            //debug
            /*
            if(lev.ES().equals("597.1"))
            //if(lev.ES().equals("4493.5") || lev.ES().equals("4101.2"))
        	for(Level lev1:ens.levelsV()) {
                if(lev1.ES().equals("597.1")) {
                	for(int i=0;i<lev1.nGammas();i++) {
                        Gamma g=lev1.gammaAt(i);
                        System.out.print("SpinParityParser 1034: DSID="+ens.DSId()+" lev="+lev.ES()+" lev1="+lev1.ES()+"  jpi="+lev1.JPiS()+" altJP="+lev1.altJPiS());
                        System.out.println(" ignoreQueGam="+ignoreQueGam+"  option="+option+"  decaying g="+g.ES()+"  used="+allUsedGammasV.contains(g));
                	}
                }
        	}
            */
            
            /*
            if(lev.ES().equals("197.272")) {
                System.out.println("SpinParityParser 1043: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
            }
            */
            
            
            if(useDecayingGammas) {
                for(int i=0;i<lev.nGammas();i++) {
                    Gamma g=lev.gammaAt(i);
                    String qs=g.q();
                    if(qs.equals("S") || (qs.equals("?")&&ignoreQueGam) || allUsedGammasV.contains(g))
                    	continue;

                    String JFS=g.JFS();
                    if(useAltJPS) {                      
                        int FLI=g.FLI();
                        if(FLI>=0) {
                            Level fLev=ens.levelsV().get(FLI);
                            
                        	JFS=fLev.altJPiS();
                        }
    
                    }
                    
                    /*
                    if(lev.ES().equals("343.8")) {
                        System.out.println("SpinParityParser 903: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
                        System.out.println("     decaying g="+g.ES()+"  JFS="+JFS+" used="+allUsedGammasV.contains(g));
                    }
                    */
                    
                    Vector<JPI> temp=findJPVFromGammaOfLevelTo(JFS,g,lev);          
                    
                    /*
                    if(lev.ES().equals("343.8")) {
                        System.out.println("SpinParityParser 910: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
                        System.out.println("     decaying g="+g.ES()+"  used="+allUsedGammasV.contains(g)+" temp="+JPI.combineJPVtoStr(temp)+"  "+(temp==null)+temp.isEmpty());
                        for(JPI jp:temp) System.out.println("  ####"+jp.js());
                    }
                    */
                    
                    if(temp.size()>0) {
                    	
                    	/*
                    	if(lev.ES().equals("5089")) {
                		//if(lev.ES().equals("4493.5") && g.ES().equals("392.3")) {
                        	System.out.println("#####"+count+" "+jpV.size()+" lev.altJP="+lev.altJPiS()+" g="+g.ES()+" altJFS="+JFS);
                        	for(JPI s:jpV) System.out.println("##"+s.js());
                        	for(JPI s:temp) System.out.println("@@"+s.js()+" "+g.MS());
                        	System.out.println("SpinParityParser 899: JPI from decaying gamma="+g.ES()+" "+g.MS()+" to JFS="+JFS+":  "+ JPI.combineJPVtoStr(temp));
                		}
                        */
                    	
                        /*
                        if(lev.ES().equals("197.272")) {
                            System.out.println("SpinParityParser 1098: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS()+" count="+count);
                            System.out.println("     decaying g="+g.ES()+" JFS="+JFS+" used="+allUsedGammasV.contains(g)+" temp="+JPI.combineJPVtoStr(temp)+"  "+temp.size());
                            for(JPI jp:prevJPV) System.out.println(" prev @@@"+jp.js());
                            if(jpV!=null) for(JPI jp:jpV) System.out.println(" jpv @@@@"+jp.js());
                        }
                        */
                    	
                        if(count==0) {
                        	if(prevJPV.size()>0)
                        		jpV=JPI.mergeJPV(prevJPV, temp,false);
                        	else
                        		jpV.addAll(temp);
                            //System.out.println("lev="+lev.ES()+"  "+(jpV==null)+"  "+(temp==null)+" "+prevJPV.size());
                            //for(JPI s:temp) System.out.println("## "+s.js());
                            //for(JPI s:prevJPV) System.out.println("$$ "+s.js());
                        }else {
                            jpV=JPI.mergeJPV(jpV,temp,false);//false for performing an "AND" operation
   
                        }
                        
                        /*
                        if(lev.ES().equals("197.272")) {
                            System.out.println("SpinParityParser 1119: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS()+"  "+(jpV==null));
                            System.out.println("     decaying g="+g.ES()+"  used="+allUsedGammasV.contains(g)+" temp="+JPI.combineJPVtoStr(temp)+"  "+temp.size());
                            if(jpV!=null) for(JPI jp:jpV) System.out.println("  ####"+jp.js());
                        }
                        */
                        
                        
                        /*
                        if(lev.ES().equals("4493.5") && g.ES().equals("392.3")) {
                          System.out.println("1#####"+count+" "+jpV.size()+" "+temp.size());
                          for(JPI s:jpV) System.out.println("1##"+s.js());
                        }
                        */
                        
                        //System.out.println("SpinParityParser 1066: "+ens.DSId()+" lev="+lev.ES()+"  "+(jpV==null));
                        if(jpV==null)
                        	return null;
                        
                        if(jpV.size()==0)
                        	continue;
                        
                     	JPI thisLower=temp.firstElement();
                    	JPI thisUpper=temp.lastElement();
                    	
                        currLower=jpV.firstElement();
                        currUpper=jpV.lastElement();
                        
                        //here we need check which one of thisLower and prevLower is UPPER (HIGHER) to narrow down the range
                        JPI lowerBound=JPI.betterUpperJPI(prevLower, thisLower);
                        JPI upperBound=JPI.betterLowerJPI(prevUpper, thisUpper);
                        
                        /*
                        if(lev.ES().equals("4493.5") && g.ES().equals("392.3")) {
                            System.out.println("1#####"+count+" "+jpV.size()+" "+temp.size()+" "+gammasForJPI.contains(g)+" prevLow="+prevLower.js()+" upper="+prevUpper.js());
                            System.out.println("   lowerBound="+lowerBound.js()+"  upperBound="+upperBound.js()+"   curr low="+currLower.js()+" upper="+currUpper.js());
                            for(JPI s:jpV) System.out.println("1##"+s.js());
                        }
                        */
                                               
                        if(!currLower.equals(prevLower) || !currUpper.equals(prevUpper)) {//current gamma narrows down the JPI range
                        	gammasForJPI.add(g);
                        	//keyGammasForJPI.add(g);
                        	gammaLowerJPIMap.put(g, temp.firstElement());
                        	gammaUpperJPIMap.put(g, temp.lastElement());
                        }
                        //else if(thisLower.equalsIgnoreBrackets(prevLower) || thisUpper.equalsIgnoreBrackets(prevUpper)) {
                        //	gammasForJPI.add(g);
                        //	gammaLowerJPIMap.put(g, temp.firstElement());
                        //	gammaUpperJPIMap.put(g, temp.lastElement());
                        //}
                        else if(jpV.size()>2 && prevJPV.size()>2){
                        	//NOTE that here, the size of jpV is equal to the same of prevJPI, since the lower and upper bound hasn't changed
                        	
                        	//lowerBound==prevLower, meaning current gammas gives a broader range or currLower is the same as prevLower.
                        	//or upperBound==prevUpper
                        	//For the latter, it could be the current gammas has the same FLS as one of
                        	//the gamma store in gammasForJPI. All gammas will be checked in the end if one needs to be removed
                        	
                        	//check if other JPI in the middle have been updated based on this gamma
                        	for(int j=1;j<jpV.size()-1;j++) {
                        		JPI jp1=prevJPV.get(j);
                        		JPI jp2=jpV.get(j);
                        		if(JPI.betterLowerJPI(jp1, jp2)==jp2) {
                        			//NOTE that lowerJPI and upperJPI return the same (the one with higher firmness) if jp1 and jp2 have the same spin
                        			gammasForJPI.add(g);
                                	gammaLowerJPIMap.put(g, temp.firstElement());
                                	gammaUpperJPIMap.put(g, temp.lastElement());
                        			break;
                        		}                 		
                        	}
                        }

                        
                    	//System.out.println("2#####"+count+" "+jpV.size());
                    	
                        prevLower=currLower;
                        prevUpper=currUpper;
                        
                        prevJPV.clear();
                        prevJPV.addAll(jpV);
                        
                        count++; 

                    }
                    
                    /*
                    if(lev.ES().equals("343.8")) {
                        System.out.println("SpinParityParser 1198: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
                        System.out.println("     decaying g="+g.ES()+"  JFS="+JFS+" used="+allUsedGammasV.contains(g));
                    }
                    */
                }
            }


            /*
            //debug
            if(lev.ES().equals("4493.5") || lev.ES().equals("4101.2"))
        	for(Level lev1:ens.levelsV()) {
                if(lev1.ES().equals("4493.5")) {
                	for(int i=0;i<lev1.nGammas();i++) {
                        Gamma g=lev1.gammaAt(i);
                        System.out.print("SpinParityParser 954: DSID="+ens.DSId()+" lev="+lev.ES()+" lev1="+lev1.ES()+"  jpi="+lev1.JPiS()+" altJP="+lev1.altJPiS());
                        System.out.println(" ignoreQueGam="+ignoreQueGam+"  option="+option+"  decaying g="+g.ES()+"  used="+allUsedGammasV.contains(g));
                	}
                }
        	}
        	*/
            
        	//if(lev.ES().equals("4493.5"))
            //System.out.println("1@@@@  "+count+" "+jpV.size());
            //for(JPI s:jpV) System.out.println("##"+s.js());
            //System.out.println("SpinParityParser 459: JPI from decaying gammas:  "+ JPI.combineJPVtoStr(jpV));
            
            /*
            if(lev.ES().equals("343.8")) {
                System.out.println("SpinParityParser 1227: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
                for(JPI s:jpV) System.out.println("##"+s.js());
            }
            */
            
            //gammas feeding this level
            if(useFeedingGammas) {
                int IL=ens.levelsV().indexOf(lev);
                
                A=ens.nucleus().A();
                
                for(int i=IL+1;i<ens.nLevels();i++) {
                    Level tempLev=ens.levelAt(i);
                    for(int j=0;j<tempLev.nGammas();j++) {
                        Gamma g=tempLev.gammaAt(j);
                        String qs=g.q();
                        
                        //if(lev.ES().equals("4101.2") && tempLev.ES().equals("4493.5")) {
                        //    System.out.println("SpinParityParser 948: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
                        //    System.out.println("     feeding g="+g.ES()+" from lev="+tempLev.ES()+" jp="+tempLev.JPiS()+" altJP="+tempLev.altJPiS()+" used="+allUsedGammasV.contains(g));
                        //}
                        
                        if(qs.equals("S") || (qs.equals("?")&&ignoreQueGam) || allUsedGammasV.contains(g))
                        	continue;                  
                        
                        if(g.FLI()==IL) {
                            String JIS=tempLev.JPiS();
                            
                            if(useAltJPS) {                      
                                JIS=tempLev.altJPiS();
                            }
                            //System.out.println("   g: "+g.ES()+" EI="+tempLev.ES()+" JI="+tempLev.JPiS());
                           
                            Vector<JPI> temp=findJPVFromGammaOfLevelTo(JIS,g,tempLev);
                            
                            /*
                            if(lev.ES().equals("2386.9") && g.ES().equals("241.3")) {
                                System.out.println("SpinParityParser 1219: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS()+" count="+count+" JIS="+JIS);
                                System.out.println("     feeding g="+g.ES()+" MUL="+g.MS()+" from "+tempLev.ES()+" altJPI="+tempLev.altJPiS()+" temp="+JPI.combineJPVtoStr(temp)+"  "+temp.size());
                            }
                            */
                            
                            //System.out.println("SpinParityParser 174: "+tempLev.ES()+" JIS="+JIS+" g="+ g.ES()+" "+g.MS()+" count="+count);
                            //for(JPI s:temp) System.out.println("     @@"+s.js());
                            //System.out.println("SpinParityParser 188: JPI from feeding gamma="+g.ES()+" "+g.MS()+" from JIS="+JIS+":  "+ JPI.combineJPV(temp));
                            
                            if(temp.size()>0) {
                            	
                            	/*
                                if(lev.ES().equals("2386.9")) {
                                    System.out.println("SpinParityParser 1225: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS()+" count="+count);
                                    System.out.println("     feeding g="+g.ES()+" from "+tempLev.ES()+" altJPI="+tempLev.altJPiS()+"  used="+allUsedGammasV.contains(g)+" temp="+JPI.combineJPVtoStr(temp)+"  "+temp.size());
                                    for(JPI jp:prevJPV) System.out.println(" prev @@@"+jp.js());
                                    if(jpV!=null) for(JPI jp:jpV) System.out.println(" jpv @@@@"+jp.js());
                                }
                                */
                            	
                                if(count==0) {
                                	if(prevJPV.size()>0)
                                		jpV=JPI.mergeJPV(prevJPV, temp,false);
                                	else
                                		jpV.addAll(temp);
                                	
                                    //System.out.println("lev="+lev.ES()+"  "+(jpV==null)+"  "+(temp==null)+" "+prevJPV.size());
                                    //for(JPI s:temp) System.out.println("## "+s.js());
                                    //for(JPI s:prevJPV) System.out.println("$$ "+s.js());
                                }else {
                                    jpV=JPI.mergeJPV(jpV,temp,false);//false for performing an "AND" operation
                                }
           
                                /*
                                if(lev.ES().equals("2386.9")) {
                                    System.out.println("SpinParityParser 1246: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS()+"  "+(jpV==null));
                                    System.out.println("     feeding g="+g.ES()+"  MUL="+g.MS()+" used="+allUsedGammasV.contains(g)+" temp="+JPI.combineJPVtoStr(temp)+"  "+temp.size());
                                    if(jpV!=null) for(JPI jp:jpV) System.out.println("  ####"+jp.js());
                                }
                                */
                                
                                //System.out.println("2@@@@  "+count+" "+jpV.size());
                                //for(JPI s:jpV) System.out.println("2##"+s.js());
                                //System.out.println("SpinParityParser 195: JPI after adding feeding gamma="+g.ES()+" "+g.MS()+" from JIS="+JIS+":  "+ JPI.combineJPV(jpV));
                                if(jpV==null)
                                	return null;
                                
                                //System.out.println("SpinParityParser 1213: "+ens.DSId()+" lev="+lev.ES()+"  "+(jpV==null));
                                
                                if(jpV.size()==0)
                                	continue;
                                
                            	JPI thisLower=temp.firstElement();
                            	JPI thisUpper=temp.lastElement();
                            	
                                currLower=jpV.firstElement();
                                currUpper=jpV.lastElement();
                                
                                //here we need check which one of currLower and prevLower is UPPER (HIGHER) to narrow down the range
                                JPI lowerBound=JPI.betterUpperJPI(currLower, prevLower);
                                JPI upperBound=JPI.betterLowerJPI(currUpper, prevUpper);

                                /*
                        		System.out.println("@ SpinParityParser 207:  g="+g.ES()+" JIS="+JIS+" lower="+lowerBound.js()+" upper="+upperBound.js()
                        		  +" currLow="+currLower.js()+" currUpper="+currUpper.js()+" prevLower="+prevLower.js()+" prevUpper="+prevUpper.js()+
                        		  " thisLower="+thisLower.js()+" thisUpper="+thisUpper.js());
                        		System.out.println("   currLower spin= "+currLower.spin()+"  "+currLower.parity()+" gammaForJPI size="+gammasForJPI.size());
                        		System.out.println("  thislower equal currlower: "+(lowerBound==thisLower)+"  "+currLower.isFirmParity()+"  "+currLower.isFirmSpin());
                        		System.out.println("  thisUpper equal currupper: "+(upperBound==thisUpper)+"  "+currUpper.isFirmParity()+"  "+currUpper.isFirmSpin());
                                */
                        		
                                if(!currLower.equals(prevLower) || !currUpper.equals(prevUpper)) {//current gamma narrows down the JPI range
                                	gammasForJPI.add(g);
                                	//keyGammasForJPI.add(g);
                                	gammaLowerJPIMap.put(g, temp.firstElement());
                                	gammaUpperJPIMap.put(g, temp.lastElement());
                                	
                        			//System.out.println("SpinParityParser 235: g="+g.ES()+" thisLower="+thisLower.js()+" thisUpper="+thisUpper.js());
                        			//System.out.println("                                   currLower="+currLower.js()+" currUpper="+currUpper.js());
                        			//System.out.println("                                   prevLower="+prevLower.js()+" prevUpper="+prevUpper.js());
                        			
                                }
                                /*
                                else if(thisLower.equalsIgnoreBrackets(prevLower) || thisUpper.equalsIgnoreBrackets(prevUpper)) {
                                	boolean toAdd=false;
                                	if(thisLower.equalsIgnoreBrackets(prevLower) && thisUpper.equalsIgnoreBrackets(prevUpper)) {
                                		//toAdd=true;
                                	}else {
                                		for(Gamma g1:gammasForJPI) {
                                			JPI lower1=gammaLowerJPIMap.get(g1);
                                			JPI upper1=gammaUpperJPIMap.get(g1);
                                			
                                			//System.out.println("SpinParityParser 242: g="+g.ES()+" thisLower="+thisLower.js()+" thisUpper="+thisUpper.js());
                                			//System.out.println("                     g1="+g1.ES()+" g1  Lower="+lower1.js()+" g1  Upper="+upper1.js());
                                			
                                			if(thisLower.equalsIgnoreBrackets(lower1) && thisUpper.equalsIgnoreBrackets(upper1)) {
                                				toAdd=true;
                                				break;
                                			}
                                		}
                                	}
                                	if(toAdd) {
                                    	gammasForJPI.add(g);
                                    	gammaLowerJPIMap.put(g, temp.firstElement());
                                    	gammaUpperJPIMap.put(g, temp.lastElement());
                                	}

                                }*/
                                else if(jpV.size()>2 && prevJPV.size()>2){
                                	//NOTE that here, the size of jpV is equal to the same of prevJPI, since the lower and upper bound hasn't changed
                                	
                                	//lowerBound==prevLower, meaning current gammas gives a broader range or currLower is the same as prevLower.
                                	//or upperBound==prevUpper
                                	//For the latter, it could be the current gammas has the same FLS as one of
                                	//the gamma store in gammasForJPI. All gammas will be checked in the end if one needs to be removed
                                	
                                	//check if other JPI in the middle have been updated based on this gamma
                                	for(int k=1;k<jpV.size()-1;k++) {
                                		JPI jp1=prevJPV.get(k);
                                		JPI jp2=jpV.get(k);
                                		if(JPI.betterLowerJPI(jp1, jp2)==jp2) {
                                			//NOTE that lowerJPI and upperJPI return the same (the one with higher firmness) if jp1 and jp2 have the same spin
                                			gammasForJPI.add(g);
                                        	gammaLowerJPIMap.put(g, temp.firstElement());
                                        	gammaUpperJPIMap.put(g, temp.lastElement());
                                			break;
                                		}                 		
                                	}
                                }
                                
                                /*
                        		System.out.println("& SpinParityParser 250:  g="+g.ES()+" JIS="+JIS+" lower="+lowerBound.js()+" upper="+upperBound.js()
                      		               +" currLow="+currLower.js()+" currUpper="+currUpper.js()+" prevLower="+prevLower.js()+" prevUpper="+prevUpper.js()+
                      		                " thisLower="+thisLower.js()+" thisUpper="+thisUpper.js());                 		
                         		System.out.println("   currLower spin= "+currLower.spin()+"  "+currLower.parity()+" gammaForJPI size="+gammasForJPI.size());               		
                        		System.out.println("  thislower equal currlower: "+(lowerBound==thisLower)+"  "+currLower.isFirmParity()+"  "+currLower.isFirmSpin());                 		
                        		System.out.println("  thisUpper equal currupper: "+(upperBound==thisUpper)+"  "+currUpper.isFirmParity()+"  "+currUpper.isFirmSpin());
                                */
                                
                                prevLower=currLower;
                                prevUpper=currUpper;
                                
                                prevJPV.clear();
                                prevJPV.addAll(jpV);
                                
                                count++;       
                            } 
                            
                            break;
                        }
                    }

                }
            }

            /*
            if(lev.ES().equals("343.8")) {
                System.out.println("SpinParityParser 1421: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
            }
            */
            
            /*
            //debug
        	for(Level lev1:ens.levelsV()) {
                if(lev1.ES().equals("4493.5")) {
                	for(int i=0;i<lev1.nGammas();i++) {
                        Gamma g=lev1.gammaAt(i);
                        System.out.println("SpinParityParser 1121: DSID="+ens.DSId()+" lev="+lev.ES()+" lev1="+lev1.ES()+"  jpi="+lev1.JPiS()+" altJP="+lev1.altJPiS());
                        System.out.println(" ignoreQueGam="+ignoreQueGam+"  option="+option+"  decaying g="+g.ES()+"  used="+allUsedGammasV.contains(g));
                	}
                }
        	}
        	*/
            
            //System.out.println("SpinParityParer 257:  "+count+" "+jpV.size());
            //for(JPI s:jpV) System.out.println("##"+s.js());
            //System.out.println("SpinParityParser 188: JPI from all gammas:  "+ JPI.combineJPV(jpV));
            
            //now check gammasForJPI for redundant ones
            
            Vector<Gamma> tempGammasV=new Vector<Gamma>();
            tempGammasV.addAll(gammasForJPI);

            
            /*
            for(int i=0;i<gammasForJPI.size();i++) {
            	Gamma g=gammasForJPI.get(i);
            	System.out.println("##SpinParityParser 293:     Lev="+lev.ES()+"  g for JPI: "+g.ES());
            }
            */
            //for(int i=0;i<keyGammasForJPI.size();i++) 
            //	System.out.println("@@SpinParityParser 296:     Lev="+lev.ES()+" key g for JPI: "+keyGammasForJPI.get(i).ES());
            
            //keyGammasForJPI.clear();
            
            //TO DO:
            //check possible circular argument when using feeding gammas
            //   check the JPI comment of the levels which the feeding gammas are from to see if
            //   those gammas are used. But the question is since JPI is assigned for lower to
            //   higher levels (lower levels assigned first), it could be the JPI of higher levels
            //   can't be determined yet when the feeding gamma is used to determine JPI of the
            //   current level. So it is better to avoid using feeding gammas for JPI, unless
            //   the JPI of the higher levels can be also determined at this point. But it could 
            //   be very complicated.
            // Or treat decaying and feeding gammas separately for JPI assignments and use
            //   those from feeding gammas only as reference   
            for(int i=gammasForJPI.size()-1;i>=0;i--) {
            	Gamma g=gammasForJPI.get(i);
            	if(!tempGammasV.contains(g))
            		continue;
            	
            	JPI thisLower=gammaLowerJPIMap.get(g);
            	JPI thisUpper=gammaUpperJPIMap.get(g);
            	
            	JPI lowerBound=JPI.betterUpperJPI(thisLower,currLower);//note that if two are the same, the first input will return
            	JPI upperBound=JPI.betterLowerJPI(thisUpper,currUpper);

            	/*
        		System.out.println("##"+i+"  SpinParityParser 351: EG="+g.ES()+"  lower="+lowerBound.js()+" upper="+upperBound.js()
      		  +" currLow="+currLower.js()+" currUpper="+currUpper.js()+" this g lower="+thisLower.js()+" upper="+thisUpper.js());
      		System.out.println("   thisLower spin="+thisLower.spin()+" parity="+thisLower.parity()+" currLower: "+currLower.spin()+"  "+currLower.parity());
      		System.out.println("  thislower equal currlower: "+thisLower.equals(currLower)+"  "+currLower.isFirmParity()+"  "+currLower.isFirmSpin());
               */
      		
            	if(lowerBound==currLower && upperBound==currUpper){
          		
            		if((thisLower.spinValue()<currLower.spinValue()&&thisUpper.spinValue()>currUpper.spinValue()) && !keyGammasForJPI.contains(g)) {
            			boolean hasCurrLower=false,hasCurrUpper=false;
            			boolean hasSameLower=false,hasSameUpper=false;
            			for(Gamma g1:gammasForJPI) {
            				if(!tempGammasV.contains(g1) || g1==g)
            					continue;
            				
                        	JPI lower1=gammaLowerJPIMap.get(g1);
                        	JPI upper1=gammaUpperJPIMap.get(g1);
                        	if(lower1.equalsIgnoreBrackets(currLower))
                        		hasCurrLower=true;
                        	if(upper1.equalsIgnoreBrackets(currUpper))
                        		hasCurrUpper=true;
                        	
                        	if(lower1.equalsIgnoreBrackets(thisLower))
                        		hasSameLower=true;
                        	if(upper1.equalsIgnoreBrackets(thisUpper))
                        		hasSameUpper=true;
            			}
            			
            			if(hasCurrLower && hasCurrUpper)
            				tempGammasV.remove(g);
            			else if(hasSameLower) {
                    		for(int j=i-1;j>=0;j--) {
                    			Gamma g1=gammasForJPI.get(j);
                            	if(!tempGammasV.contains(g1) || g1==g)
                            		continue;
                            	
                            	checkGammaForJPILower(g,g1,gammaLowerJPIMap,gammaUpperJPIMap,tempGammasV);
                    		}
            			}else if(hasSameUpper) {
                    		for(int j=i-1;j>=0;j--) {
                    			Gamma g1=gammasForJPI.get(j);
                            	if(!tempGammasV.contains(g1) || g1==g)
                            		continue;

                            	checkGammaForJPIUpper(g,g1,gammaLowerJPIMap,gammaUpperJPIMap,tempGammasV);
                    		}
            			}
            			
                        //for(int k=0;k<tempGammasV.size();k++) 
                        //	System.out.println("@@"+i+"  SpinParityParser 330 after removing g="+g.ES()+" JIS="+g.JIS()+g.MS()+" for Lev="+lev.ES()+"  g for JPI: "+tempGammasV.get(k).ES());

            		}else if(thisLower.spinValue()==currLower.spinValue()) {
                		for(int j=i-1;j>=0;j--) {
                			Gamma g1=gammasForJPI.get(j);
                        	if(!tempGammasV.contains(g1) || g1==g)
                        		continue;
                        	
                        	checkGammaForJPILower(g,g1,gammaLowerJPIMap,gammaUpperJPIMap,tempGammasV);
                		}
            		}else if(thisUpper.spinValue()==currUpper.spinValue()) {
                		for(int j=i-1;j>=0;j--) {
                			Gamma g1=gammasForJPI.get(j);
                        	if(!tempGammasV.contains(g1) || g1==g)
                        		continue;

                        	checkGammaForJPIUpper(g,g1,gammaLowerJPIMap,gammaUpperJPIMap,tempGammasV);
                		}
            		}
            	}else if(lowerBound==thisLower && upperBound==currUpper) {
            		//mean thisLower is better so this gamma kept, or it is the same as currLower and then check if there is 
            		//any other gamma giving the same lowerBound JPI and keep the better one based on energy or other conditions(?)
    
            		for(int j=i-1;j>=0;j--) {
            			Gamma g1=gammasForJPI.get(j);
                    	if(!tempGammasV.contains(g1) || g1==g)
                    		continue;
                    	
                    	checkGammaForJPILower(g,g1,gammaLowerJPIMap,gammaUpperJPIMap,tempGammasV);
            		}
            	}else if(lowerBound==currLower && upperBound==thisUpper) {
            		for(int j=i-1;j>=0;j--) {
            			Gamma g1=gammasForJPI.get(j);
                    	if(!tempGammasV.contains(g1) || g1==g)
                    		continue;
                    	
                    	checkGammaForJPIUpper(g,g1,gammaLowerJPIMap,gammaUpperJPIMap,tempGammasV);
            		}
            	}else {
            		//both thisLower and thisUpper are the same as current lower and upper bounds, do the same check as above
             		for(int j=i-1;j>=0;j--) {
            			Gamma g1=gammasForJPI.get(j);
                    	if(!tempGammasV.contains(g1) || g1==g)
                    		continue;
                    	
                    	checkGammaForJPILower(g,g1,gammaLowerJPIMap,gammaUpperJPIMap,tempGammasV);
            		}
            	}
            	
                //for(int k=0;k<tempGammasV.size();k++) 
                //	System.out.println("#"+i+"  SpinParityParser 318:     Lev="+lev.ES()+"  g for JPI: "+tempGammasV.get(k).ES());
            }
            
            /*
            //debug
            if(lev.ES().equals("4493.5") || lev.ES().equals("4101.2"))
        	for(Level lev1:ens.levelsV()) {
                if(lev1.ES().equals("4493.5")) {
                	for(int i=0;i<lev1.nGammas();i++) {
                        Gamma g=lev1.gammaAt(i);
                        System.out.print("SpinParityParser 1291: DSID="+ens.DSId()+" lev="+lev.ES()+" lev1="+lev1.ES()+"  jpi="+lev1.JPiS()+" altJP="+lev1.altJPiS());
                        System.out.println(" ignoreQueGam="+ignoreQueGam+"  option="+option+"  decaying g="+g.ES()+"  used="+allUsedGammasV.contains(g));
                	}
                }
        	}
        	*/
            
            /*
            if(lev.ES().equals("2386.9")) {
                System.out.println("SpinParityParser 1547: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS()+"  "+(jpV==null));
                System.out.println("   altJPS="+JPI.combineJPVtoStr(jpV));
                if(jpV!=null) for(JPI jp:jpV) System.out.println("  ####"+jp.js());
            }
            */
            
            /*
            if(lev.ES().equals("343.8")) {
                System.out.println("SpinParityParser 1605: DSID="+ens.DSId()+" lev="+lev.ES()+"  jpi="+lev.JPiS()+" altJP="+lev.altJPiS());
            }
            */
            
            gammasForJPI.clear();
            gammasForJPI.addAll(tempGammasV);
            
            return jpV;
            //for(int k=0;k<tempGammasV.size();k++) 
            //	System.out.println("  SpinParityParser 355:     Lev="+lev.ES()+"  g for JPI: "+tempGammasV.get(k).ES());
        }catch(Exception e) {
            e.printStackTrace();
        }

                
        return null;
    }
    
    private Vector<JPI> findJPVFromGammaOfLevelTo(String JPS, Gamma g,Level lev){
        
    	try {
    		
    		Vector<JPI> temp=new Vector<JPI>();
    		
    		int A=lev.nucleus().A();
    		
            String ms=g.MS();
            if(ms.startsWith("["))
            	ms="";
            if(ms.isEmpty() && g.isPrimary())
            	ms="(D,E2)";
           
            double t12=lev.T12D();
            
            double tempMinT12=t12;
            double tempMaxT12=t12;
            
            boolean hasDone=false;
            if(!ms.isEmpty()) {
            	temp=findJPVFromGammaTo(JPS,ms,g.ES(),A,t12);//t12 is not involved in calculation
            	hasDone=true;
            }else if(t12<=0) {
            	tempMinT12=minT12;
            	tempMaxT12=maxT12;
            }else{
            	double tl=lev.T12LowerD();
            	double tu=lev.T12UpperD();
            	if(tl==0)
            		tl=Math.min(1.0E-15,t12/1000);
            	if(tl>0 && tu>0) {
            		tempMinT12=tl;
            		tempMaxT12=tu;
            	}else if(lev.DT12S().isEmpty()||Str.isLetters(lev.DT12S())){
            		XDX xdx=UncertaintyParser.findParsedXDX(lev.T12S(),lev.DT12S());
            		double x=xdx.x;
            		double dxu=xdx.dxu;
            		double dxl=xdx.dxl;
            		
            		double f=t12/Double.parseDouble(lev.T12S());
            		if(dxu>=0 && dxl>=0) {
            			tempMinT12=(x-dxu)*f;
            			tempMaxT12=(x+dxu)*f;
            			x=x*f;
            			if(tempMinT12<=0)
            				tempMinT12=Math.min(1.0E-15,x/1000);
            		}else {
                     	tempMinT12=t12/10000;
                    	tempMaxT12=t12*10000;
            		}	
            	}else {
                 	tempMinT12=t12/10000;
                	tempMaxT12=t12*10000;
            	}

            }

            if(!hasDone) {
             	String ms1=BXLBase.findPossibleMULForEG(g.ES(), A, tempMinT12);
            	String ms2=BXLBase.findPossibleMULForEG(g.ES(), A, tempMaxT12);
          	
            	String[] temp1=ms1.split("[,]");
            	String[] temp2=ms1.split("[,]");
            	int n1=temp1.length;
            	int n2=temp2.length;
            	
            	ms="(E1,M1,E2)";
            	String tempMS="E1,M1,E2";
            	//compare with [D,E2]=[E1,M1,E2]
            	if(n2<3 || (n2==3&&ms2.equals(tempMS)))
            		ms=ms2;
            	else if(n1>3 || (n1==3&&ms1.equals(tempMS)) )
            		ms=ms1;

            	if(!ms.contains("(")){
            		String s=lev.T12S();
            		String ds=lev.DT12S();
            		SDS2XDX sx=new SDS2XDX(s,ds);
            		boolean isNumUnc=false;
            		if(sx.dxu()>0 && sx.dxl()>0)
            			isNumUnc=true;
            		
            		if(s.isEmpty() || ds.isEmpty() || (!isNumUnc&&!ds.startsWith("L"))) {
            			ms="("+ms+")";
            		}           			
            	}
            	
            	temp=findJPVFromGammaTo(JPS,ms,g.ES(),A,tempMaxT12);//here A and maxT12 are not involved in calculation
            	
            	
            	//if(g.ES().equals("241.3") || g.ES().equals("1714.8") || g.ES().equals("4101.4"))
            	//	System.out.println("SpinParityParser 1464: DSID="+ens.DSId()+" lev="+lev.ES()+" g="+g.ES()+" JFS="+JPS+
            	//			" ms="+ms+" tempMinT12="+tempMinT12+" maxT12="+tempMaxT12+" temp JS="+JPI.combineJPVtoStr(temp)+"  ms1="+ms1+"   ms2="+ms2);
            }
            
            return temp;
    	}catch(Exception e) {
    		
    	}
    	return null;
    }
    
    private void checkGammaForJPILower(Gamma g1,Gamma g2,HashMap<Gamma,JPI> gammaLowerJPIMap,HashMap<Gamma,JPI> gammaUpperJPIMap,Vector<Gamma> gammasV) {
    	JPI thisLower=gammaLowerJPIMap.get(g1);
    	JPI thisUpper=gammaUpperJPIMap.get(g1);
    	
      	JPI tempLower=gammaLowerJPIMap.get(g2);
    	JPI tempUpper=gammaUpperJPIMap.get(g2);
    	
    	if(thisLower.equals(tempLower)) {
    		if(JPI.betterLowerJPI(thisUpper,tempUpper)==tempUpper) {
        		if(thisUpper.spinValue()>tempUpper.spinValue() && !keyGammasForJPI.contains(g1))
        			gammasV.remove(g1);
    		}else if(thisUpper.equals(tempUpper)) {//same lower, and same upper, then check conditions like energy, intensity, ? mark
    			checkAndRemoveGamma(g1,g2,gammasV);    			
    		}else if(!keyGammasForJPI.contains(g2)){//tempUpper>thisUpper>currUpper
    			gammasV.remove(g2);
    		}
    	}else if(thisLower.equalsIgnoreBrackets(tempLower)) {
    		if(JPI.betterLowerJPI(thisLower, tempLower)==tempLower) {
        		if(JPI.betterUpperJPI(thisUpper,tempUpper)==tempUpper) {
        			gammasV.remove(g1);           			
        		}
    		}else {
        		if(JPI.betterUpperJPI(thisUpper,tempUpper)==thisUpper) {
        			gammasV.remove(g2);           			
        		}
    		}

    	}
    }
    
    private void checkGammaForJPIUpper(Gamma g1,Gamma g2,HashMap<Gamma,JPI> gammaLowerJPIMap,HashMap<Gamma,JPI> gammaUpperJPIMap,Vector<Gamma> gammasV) {
    	JPI thisLower=gammaLowerJPIMap.get(g1);
    	JPI thisUpper=gammaUpperJPIMap.get(g1);
    	
      	JPI tempLower=gammaLowerJPIMap.get(g2);
    	JPI tempUpper=gammaUpperJPIMap.get(g2);
    	
    	if(thisUpper.equals(tempUpper)) {
    		//System.out.println("SpinParityParser 456: g1 lower="+thisLower.js()+" upper="+thisUpper.js()+" g2 lower="+tempLower.js()+" upper="+tempUpper.js());

    		if(JPI.betterUpperJPI(thisLower,tempLower)==tempLower) {
        		if(thisLower.spinValue()<tempLower.spinValue()&& !keyGammasForJPI.contains(g1))
        			gammasV.remove(g1);
        		
        		//System.out.println("SpinParityParser 460: g1 lower="+thisLower.js()+" upper="+thisUpper.js()+" g2 lower="+tempLower.js()+" upper="+tempUpper.js());
    		}else if(thisLower.equals(tempLower)) {//same lower, and same upper, then check conditions like energy, intensity, ? mark
    			checkAndRemoveGamma(g1,g2,gammasV); 	
        		//System.out.println("SpinParityParser 460: g1 lower="+thisLower.js()+" upper="+thisUpper.js()+" g2 lower="+tempLower.js()+" upper="+tempUpper.js());

    		}else if(!keyGammasForJPI.contains(g2)){//tempUpper>thisUpper>currUpper
    			gammasV.remove(g2);
    		}
    	}else if(thisUpper.equalsIgnoreBrackets(tempUpper)) {
    		//System.out.println("SpinParityParser 482: g1 lower="+thisLower.js()+" upper="+thisUpper.js()+" g2 lower="+tempLower.js()+" upper="+tempUpper.js());
    		//System.out.println(" JPI.lowerJPI(thisUpper, tempUpper)="+JPI.betterLowerJPI(thisUpper, tempUpper).js()
    		//		+" JPI.lowerJPI(thisLower,tempLower)="+JPI.betterLowerJPI(thisLower,tempLower).js());

    		if(JPI.betterUpperJPI(thisUpper, tempUpper)==tempUpper) {
        		if(JPI.betterLowerJPI(thisLower,tempLower)==tempLower) {
        			gammasV.remove(g1);           			
        		}
    		}else {
        		if(JPI.betterLowerJPI(thisLower,tempLower)==thisLower) {
        			gammasV.remove(g2);           			
        		}
    		}

    	}   	
    }
    
    private void checkAndRemoveGamma(Gamma g1,Gamma g2,Vector<Gamma> gammasV) {
    	Gamma gamToRemove=null;
		if(!g1.q().isEmpty() && g2.q().isEmpty())
			gamToRemove=g1;
		else if(g1.q().isEmpty() && !g2.q().isEmpty())
			gamToRemove=g2;
		else if(g1.EF()<2000 && g2.EF()>4000)
			gamToRemove=g2;
		else if(g1.EF()>4000 && g2.EF()<2000)
			gamToRemove=g1;
		else if(g1.RID()>g2.RID())
			gamToRemove=g2;
		else if(g1.RID()<g2.RID())
			gamToRemove=g1;
		else if(g1.EF()<=g2.EF())
			gamToRemove=g2;
		else 
			gamToRemove=g1;
		
		if(gamToRemove!=null && !keyGammasForJPI.contains(gamToRemove))
			gammasV.remove(gamToRemove);
    }
    
    
    public Vector<Gamma> getGammasForJPI(Level lev){
    	Vector<Gamma> out=new Vector<Gamma>();
    	try {
    		return mapOflevelGammasForJPI.get(lev);
    	}catch(Exception e) {
    		
    	}
    	
    	return out;
    }
    
    @SuppressWarnings("unchecked")
	public <T extends Decay> T getDecayForJPI(Level lev){
    	try {
    		return (T)mapOflevelDecayForJPI.get(lev);
    	}catch(Exception e) {
    		
    	}
    	
    	return null;
    }
    
    public Vector<Gamma> currentGammasForJPI(){return gammasForJPI;}//after findJPSFromGammas(lev) call
    
    @SuppressWarnings("unchecked")
	public <T extends Decay> T  currentDecayForJPI(){return (T) decayForJPI;}
    
    /////////////////////////////////////////
    //static methods
    

    public static Vector<JPI> findJPVFromGammaTo(String js,String MUL) {
    	return findJPVFromGammaTo(js,MUL,"",-1,-1);
    }

    public static Vector<JPI> findJPVFromGammaNoMULTo(String js,String es,int A,double t12) {
    	return findJPVFromGammaTo(js,"",es,A,t12);
    }
    public static Vector<JPI> findJPVFromGammaTo(String js,String MUL,String es,int A,double t12) {
        Vector<String> LV=EnsdfUtil.findGammaPoleExp(MUL);

        /*
        if(es.equals("241.4")) {
        	for(String s:LV) System.out.println(js+"  M="+MUL+" "+s);
        	System.out.println(" js="+js+" M="+MUL+"  "+LV.size()+" t12="+t12);
        }
        */
        
        JPI jp=new JPI(js);
        boolean isSingleJP=jp.isUniqueSpin();
        
        if(LV.size()==0) {
        	//no specific MUL
        	MUL=BXLBase.findPossibleMULForEG(es, A, t12);
        	
        	/*
            if(es.equals("241.4")) {
            	System.out.println("#####"+MUL);
            }
            */
        	
        	if(MUL.length()>0)
        		MUL="("+MUL+")";
        	
        	LV=EnsdfUtil.findGammaPoleExp(MUL);
        	if(LV.size()==0) {
                LV.add("(0)");
                LV.add("(1)");
                LV.add("(2)");
                LV.add("(3)");
                LV.add("(4)");
        	}
        	/*
            float ef=-1;
            boolean hasAssumedMUL=true;
            if(Str.isNumeric(es)) {
            	ef=Float.parseFloat(es);
            }
            
            if(ef<=0) {
            	hasAssumedMUL=false;
            }else {
            	if(t12<=0)
            		t12=1E-10;//in units of second
            	
            	float minEGFromRUL=BXLBase.findMinEGForRUL(A, MUL,t12);
            }
            
            
            if(!hasAssumedMUL) {
                LV.add("(0)");
                LV.add("(1)");
                LV.add("(2)");
                LV.add("(3)");
                LV.add("(4)");
            }
            if(ef<=0||ef>2000) {

            }else {
                LV.add("(0)");
                LV.add("(1)");
                LV.add("(2+)");
            } 
            */ 
        }

        //for(String LS:LV) System.out.println(LS);
        
        Vector<JPI> out=new Vector<JPI>();
        Vector<JPI> temp=JPI.JPICoupling(js,LV.get(0));//L coupling to J, only taken J-L and J+L
        
        //for(JPI j:temp) System.out.println(js+"  "+j.js());
        
        if(temp.size()>1 && isSingleJP) {
        	if(jp.spinValue()>=parseL(LV.get(0)))//J>=L
        		out.add(temp.get(0));
        	
            out.add(temp.lastElement());
        }else {
            out.addAll(temp);
        }
        
        Vector<JPI> temp1=null;
        for(int i=1;i<LV.size();i++) {
            temp=JPI.JPICoupling(js,LV.get(i));
            temp1=new Vector<JPI>();
            if(temp.size()>1 && isSingleJP) {
            	if(jp.spinValue()>=parseL(LV.get(i)))//J>=L
            		temp1.add(temp.get(0));
                temp1.add(temp.lastElement());
            }else {
                temp1.addAll(temp);     
            }
            
            out=JPI.mergeJPV(out,temp1,true);
        }       
        
        return out;
    }

    private static int parseL(String LS) {
    	try {
    		String s=Str.removeAllBrackets(LS);
    		s=s.replace("+","").replace("-","");
    		return Integer.parseInt(s);
    	}catch(Exception e) {
    		
    	}
    	return -1;
    }
    public static Vector<String> findJSVFromGammaTo(String js,String MUL) {
        Vector<String> out=new Vector<String>();
        Vector<JPI> jpV=findJPVFromGammaTo(js,MUL);
        for(int i=0;i<jpV.size();i++) {
            out.add(jpV.get(i).js());
        }
        return out;
    }

    public static Vector<String> JPV2JSV(Vector<JPI> jpV){       
        Vector<String> out=new Vector<String>();
        try {
            for(int i=0;i<jpV.size();i++) {
                out.add(jpV.get(i).js());
            }
        }catch(Exception e) {
        	
        }

        return out;
    }
    
    public static Vector<JPI> JSV2JPV(Vector<String> jsV){       
        Vector<JPI> out=new Vector<JPI>();
        try {
            for(int i=0;i<jsV.size();i++) {
                out.add(new JPI(jsV.get(i)));
            }
        }catch(Exception e) {
        	
        }

        return out;
    }
}
