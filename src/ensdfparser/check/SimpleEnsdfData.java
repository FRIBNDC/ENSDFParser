package ensdfparser.check;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import ensdfparser.ensdf.DParticle;
import ensdfparser.ensdf.DatasetID;
import ensdfparser.ensdf.Decay;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.Gamma;
import ensdfparser.ensdf.History;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.Normal;
import ensdfparser.ensdf.Nucleus;
import ensdfparser.ensdf.Parent;
import ensdfparser.ensdf.QValue;
import ensdfparser.ensdf.T12;

/*
 * a class to hold some basic sorted ENSDF data, like levels, gammas, flags 
 */
public class SimpleEnsdfData {
	ENSDF ens;

	Vector<History> histV;
	
	//exclusive variables of this class
	String allRecordTypes="";//store all record types allowed in this dataset
	String NUCID="",parentNUCID="";
	String dsid0="";//dsid in the first DSID line
	String dsid="";//dsid combining all DSID lines
	
	int A=-1,Z=-1;
	
	Vector<T12> T12VInDSID=new Vector<T12>();
	DatasetID dsidInfo;
	
    HashMap<String,Vector<String>> levelFlagRecNamesMap=new HashMap<String,Vector<String>>();
    HashMap<String,Vector<String>> gammaFlagRecNamesMap=new HashMap<String,Vector<String>>();
    HashMap<String,Vector<String>> decayFlagRecNamesMap=new HashMap<String,Vector<String>>();
    HashMap<String,Vector<String>> delayFlagRecNamesMap=new HashMap<String,Vector<String>>();
    
    Vector<String> usedLevelFlagsV=new Vector<String>();
    Vector<String> usedGammaFlagsV=new Vector<String>();
    Vector<String> usedDecayFlagsV=new Vector<String>();
    Vector<String> usedDelayFlagsV=new Vector<String>();
    
    HashMap<String,String> relabelMap=new HashMap<String,String>();
    
	public SimpleEnsdfData(){
		ens=new ENSDF();
		histV=ens.histV();		
	}
	
	//getters for references of all vectors in ens   
	public Vector<History> histV(){return ens.histV();}
	   
	public Vector<Level> levelsV(){return ens.levelsV();}
	public Vector<Gamma> unpGammasV(){return ens.unpGammas();}
	@SuppressWarnings("unchecked")
	public Vector<Decay> unpDecaysV(){return (Vector<Decay>) ens.unpDecaysV();}
	public Vector<DParticle> unpDPsV(){return ens.unpDParticles();}
	public Vector<Parent> parentsV(){return ens.parentsV();}
	public Vector<Normal> normsV(){return ens.normsV();}
	public Normal norm(){
		try {
			return normsV().get(0);
		}catch(Exception e) {
			
		}
		
		return null;
	}
	
	//getters for exclusive variable of this class
	public HashMap<String,Vector<String>> levelFlagRecNamesMap(){return levelFlagRecNamesMap;}
	public HashMap<String,Vector<String>> gammaFlagRecNamesMap(){return gammaFlagRecNamesMap;}
	public HashMap<String,Vector<String>> decayFlagRecNamesMap(){return decayFlagRecNamesMap;}
	public HashMap<String,Vector<String>> delayFlagRecNamesMap(){return delayFlagRecNamesMap;}
    
	public Vector<String> usedLevelFlagsV(){return usedLevelFlagsV;}
	public Vector<String> usedGammaFlagsV(){return usedGammaFlagsV;}
	public Vector<String> usedDecayFlagsV(){return usedDecayFlagsV;}
	public Vector<String> usedDelayFlagsV(){return usedDelayFlagsV;}

	public Vector<T12> T12VInDSID(){return T12VInDSID;}
    public HashMap<String,String> relabelMap(){return relabelMap;}
    
	/////////////////////////////////////////////////////////////
	public void setDatasetInfo(DatasetID dsInfo) {dsidInfo=dsInfo;}
    public void setAllRecordTypes(String s) {allRecordTypes=s;}
    
    public void setNUCID(String s) {//column 1-5, including heading and ending blank spaces
    	NUCID=s;
    	try {		
    		ens.nucleus().parseNUCID(s);	
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();	
    	}  	
    }
    
    public void setQValue(Vector<String> linesV) {
        try {
            ens.qv().setValues(linesV);
        }catch(Exception e) {
            
        }
    }
    public void setParentNUCID(String s) {parentNUCID=s;}
    
    public void setDSID0(String s) {dsid0=s;}
    public void setDSID(String s) {dsid=s;}
    
	public void addLevel(Level lev) {levelsV().add(lev);}
	public void addParent(Parent p) {parentsV().add(p);}
	public void addNorm(Normal norm) {normsV().add(norm);}
	
	public void addUnpGamma(Gamma gam) {unpGammasV().add(gam);}
	public void addUnpDecays(Decay d) {unpDecaysV().add(d);}
	public void addUnpDParticle(DParticle dp) {unpDPsV().add(dp);}
	
	public void addHistory(History hist) {histV.add(hist);}
	
	public ENSDF getENSDF() {return ens;}
	public Nucleus nucleus() {return ens.nucleus();}
    public QValue qv() {return ens.qv();}
    
	public String NUCID() {return NUCID;}
	public String parentNUCID() {return parentNUCID;}
	public String DSID0() {return dsid0;}
	public String fullDSID() {return dsid;}
	

	public String dsType() {return dsidInfo.datasetType;}
}
