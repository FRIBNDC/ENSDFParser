package ensdfparser.calc;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

public class GammaBranch{

    //input variables
	public double e,deu,del,ri,driu,dril,mr,dmru,dmrl,cc,dccu,dccl,ti,dtiu,dtil;//deu for upper de, del for lower de

    
	//deduced variables
    //percentage branching for gamma, set in calculateBranchings() in BXLCalculator: 
    //dbru_ep,dbrl_ep from normal error propagation,
    //dbru_ex & dbrl_ex from extremes (min and max) of each variable 
	public double br,dbru,dbrl,dbru_ep,dbrl_ep,dbru_ex,dbrl_ex;
	public double dbru_mc,dbrl_mc,dtbru_mc,dtbrl_mc,dptu_mc,dptl_mc;//uncertainty from Monte-Carlo method
	
	public double tbr,dtbru,dtbrl,dtbru_ep,dtbrl_ep,dtbru_ex,dtbrl_ex;//percentage branching for (gamma+ce), set in calculateBranchings()
	public double pt,dptu,dptl,dptu_ep,dptl_ep,dptu_ex,dptl_ex;//partial T1/2 for gamma decay (not g+ce total), set in calculateBranchings()
	public double ptTOT,dptuTOT,dptlTOT,dptuTOT_ep,dptlTOT_ep,dptuTOT_ex,dptlTOT_ex;//partial T1/2 for gamma+ce decay, set in calculateBranchings()
	public double dptuTOT_mc,dptlTOT_mc;
	
	public String es,des,ris,dris,mrs,dmrs,ccs,dccs,tis,dtis;
    public String mult;
	
	public double ji=-1000,jf=-1000;//ji is spin of parent level, jf for daughter level
	public String[] EMs,expandedEMs;//D will be expanded to E1,M1, Q will be expanded to E2,M2
			
	public XDX calcICC;//store calculated cc values using briccs
	
	public LinkedHashMap<String,XDX> ICCmap=new LinkedHashMap<String,XDX>();//store calculated ICC of each component in MULT if number of component>1
	
	//a map of all BXLW results (like raw results) before formatting for output
	public LinkedHashMap<String,ResultOfUncertainties> BXLWmap=new LinkedHashMap<String,ResultOfUncertainties>();
	
	
    //a map of new BXLW entries for the branch gb to be printed in output ENSDF file
    //(both asymmetric and symmetrized ones)
    //key=EL or ML for original entry and ELS or MLS for symmetrized one, L=1,2,3,4
    //value=new BXLW entry of value and uncertainty in ENSDF style
    //e.g., key="M1", value="BM1W=12 +3-2"
    //      key="M1S"
	HashMap<String,String> adoptedBXLWEntryMap=new HashMap<String,String>();
	
	public LinkedHashMap<String,XDX> largeErrorMap=new LinkedHashMap<String,XDX>();//<name,relative uncertainty>
	
	//true if any of the variables has limit value (GT,GE,LT,LE), 
	//except for some cases under specific conditions
	public boolean isAnyLimitInput=false;
	
	public boolean isAnyLimitBXLW=false;//for BXLW results, set in makeNewBXLWEntryMap() in BXLCalcWrap
	public boolean isAllLimitBXLW=false;//for BXLW results, set in makeNewBXLWEntryMap() in BXLCalcWrap
	
	
	public boolean isRILimit=false,isTILimit=false,isMRLimit=false,isEGLimit=false,isCCLimit=false,isBRLimit=false,isT12Limit=false;
	public boolean isMRCrossZero=false;//if MR overlaps with zero


	public boolean isMultMixed=false;
	public boolean isUseTIForBranching=false;
	
	public GammaBranch(){
		reset();
	}
	
	public void setJiJf(double ji,double jf){if(Math.abs(ji)<100) this.ji=Math.abs(ji);if(Math.abs(jf)<100) this.jf=Math.abs(jf);}
	public void setJi(double ji){if(Math.abs(ji)<100) this.ji=Math.abs(ji);}
	public void setJf(double jf){if(Math.abs(jf)<100) this.jf=Math.abs(jf);}
		
	public void setEnergy(double e,double de){this.e=e;this.deu=de;this.del=de;}
	public void setIntensity(double ri,double dri){this.ri=ri;this.driu=dri;this.dril=dri;}
	public void setGammaBranching(double br,double dbr){this.br=br;this.dbru=dbr;this.dbrl=dbr;}
	public void setTotalBranching(double tbr,double dtbr){this.tbr=tbr;this.dtbru=dtbr;this.dtbrl=dtbr;}//g+ce
	public void setTotalIntensity(double ti,double dti){this.ti=ti;this.dtiu=dti;this.dtil=dti;}//g+ce
	public void setMixingRatio(double mr,double dmr){this.mr=mr;this.dmru=dmr;this.dmrl=dmr;}
	public void setConversionCoeff(double cc,double dcc){this.cc=cc;this.dccu=dcc;this.dccl=dcc;}
	public void setMultipolarity(String mult){
		this.mult=mult.toUpperCase().trim();
		EMs=parseMUL(this.mult);
		
		expandedEMs=expandEMs(EMs,this.mult);
	}
	
	public void setEnergy(double e,double deu,double del){this.e=e;this.deu=deu;this.del=del;}
	public void setIntensity(double ri,double driu,double dril){this.ri=ri;this.driu=driu;this.dril=dril;}
	public void setGammaBranching(double br,double dbru,double dbrl){this.br=br;this.dbru=dbru;this.dbrl=dbrl;}
	public void setTotalBranching(double tbr,double dtbru,double dtbrl){this.tbr=tbr;this.dtbru=dtbru;this.dtbrl=dtbrl;}
	public void setTotalIntensity(double ti,double dtiu,double dtil){this.ti=ti;this.dtiu=dtiu;this.dtil=dtil;}
	public void setMixingRatio(double mr,double dmru,double dmrl){this.mr=mr;this.dmru=dmru;this.dmrl=dmrl;}
	public void setConversionCoeff(double cc,double dccu,double dccl){this.cc=cc;this.dccu=dccu;this.dccl=dccl;}
	public void setCalcICC(XDX icc){calcICC=icc;}
	
	public void setMixingRatioStr(String mrs,String dmrs){this.mrs=mrs;this.dmrs=dmrs;}
	
	public void setIsAnyLimitInput(boolean b) {isAnyLimitInput=b;}
	public void setIsAnyLimitBXLW(boolean b) {isAnyLimitBXLW=b;}
	public void setIsAllLimitBXLW(boolean b) {isAllLimitBXLW=b;}
	
	public void setIsRILImit(boolean b) {isRILimit=b;}
	public void setIsTILImit(boolean b) {isTILimit=b;}
	public void setIsMRLImit(boolean b) {isMRLimit=b;}
	public void setIsMRCrossZero(boolean b) {isMRCrossZero=b;if(b) isMRLimit=true;}
	public void setIsCCLImit(boolean b) {isCCLimit=b;}
	public void setIsEGLImit(boolean b) {isEGLimit=b;}
	public void setIsBRLImit(boolean b) {isBRLimit=b;}
	public void setIsT12Limit(boolean b) {isT12Limit=b;}
	
	public void setIsMultMixed(boolean b) {isMultMixed=b;}
	
	public void setIsUseTIForBranching(boolean b) {isUseTIForBranching=b;}
	
	//store orignial content of a gamma
	public String es() {return es;}
	public String des() {return des;}
	public String ris() {return ris;}
	public String dris() {return dris;}
	public String mrs() {return mrs;}
	public String dmrs() {return dmrs;}
	public String ccs() {return ccs;}
	public String dccs() {return dccs;}
	public String tis() {return tis;}
	public String dtis() {return dtis;}
	public String mult() {return mult;}
	
	
	public double e(){return e;}
	public double deu(){return deu;} 
	public double del(){return del;}
	public double ji(){return ji;}
	public double jf(){return jf;}		
	public double ri(){return ri;}
	public double driu(){return driu;}
	public double dril(){return dril;}
	public double ti(){return ti;}
	public double dtiu(){return dtiu;}
	public double dtil(){return dtil;}
	public double mr(){return mr;}
	public double dmru(){return dmru;}
	public double dmrl(){return dmrl;}
	public double cc(){return cc;}
	public double dccu(){return dccu;}
	public double dccl(){return dccl;} 

	public double br(){return br;}
	public double dbru(){if(dbru==0) return dbru_ep; else return dbru;}
	public double dbrl(){if(dbrl==0) return dbrl_ep; else return dbrl;}	
	public double dbru_ex(){return dbru_ex;}
	public double dbrl_ex(){return dbrl_ex;}
	public double dbru_ep(){return dbru_ep;}
	public double dbrl_ep(){return dbrl_ep;}
	public double dbru_mc(){return dbru_mc;}
	public double dbrl_mc(){return dbrl_mc;}
	public double tbr(){return tbr;}
	public double dtbru(){if(dtbru==0) return dtbru_ep; else return dtbru;}
	public double dtbrl(){if(dtbrl==0) return dtbrl_ep; else return dtbrl;}
	public double dtbru_ep(){return dtbru_ep;}
	public double dtbrl_ep(){return dtbrl_ep;}
	public double dtbru_ex(){return dtbru_ex;}
	public double dtbrl_ex(){return dtbrl_ex;}
	public double dtbru_mc(){return dtbru_mc;}
	public double dtbrl_mc(){return dtbrl_mc;}
	
	public double pt(){return pt;}//partial T12 for gamma decay
	public double dptu(){if(dptu==0) return dptu_ep; else return dptu;}
	public double dptl(){if(dptl==0) return dptl_ep; else return dptl;}	
	public double dptu_ex(){return dptu_ex;}
	public double dptl_ex(){return dptl_ex;}
	public double dptu_ep(){return dptu_ep;}
	public double dptl_ep(){return dptl_ep;}
	public double dptu_mc(){return dptu_mc;}
	public double dptl_mc(){return dptl_mc;}
	
	public double ptTOT(){return ptTOT;}//partial T12 for g+ce decay
	public double dptuTOT(){if(dptuTOT==0) return dptuTOT_ep; else return dptuTOT;}
	public double dptlTOT(){if(dptlTOT==0) return dptlTOT_ep; else return dptlTOT;}	
	public double dptuTOT_ex(){return dptuTOT_ex;}
	public double dptlTOT_ex(){return dptlTOT_ex;}
	public double dptuTOT_ep(){return dptuTOT_ep;}
	public double dptlTOT_ep(){return dptlTOT_ep;}
	public double dptuTOT_mc(){return dptuTOT_mc;}
	public double dptlTOT_mc(){return dptlTOT_mc;}
	
	public LinkedHashMap<String,XDX> getICCmap(){return ICCmap;}
	public LinkedHashMap<String,ResultOfUncertainties> getBXLWmap(){return BXLWmap;}
	public ResultOfUncertainties getBXLW(String EM){return BXLWmap.get(EM.toUpperCase().trim());}
	
	public HashMap<String,String> adoptedBXLWEntryMap(){return adoptedBXLWEntryMap;}
	public String adoptedBXLWEntry(String EM){return adoptedBXLWEntryMap.get(EM.toUpperCase().trim());}
	
	public String[] getEMs(){return EMs;}
	public String[] getExpandedEMs() {return expandedEMs;}
	
	public boolean isAnyLimitInput() {return isAnyLimitInput;}
	public boolean isAnyLimitBXLW() {return isAnyLimitBXLW;}
	public boolean isAllLimitBXLW() {return isAllLimitBXLW;}
	
	public boolean isRILimit() {return isRILimit;}
	public boolean isTILimit() {return isTILimit;}
	public boolean isMRLimit() {return isMRLimit;}
	public boolean isEGLimit() {return isEGLimit;}
	public boolean isCCLimit() {return isCCLimit;}
	public boolean isBRLimit() {return isBRLimit;}
	public boolean isT12Limit() {return isT12Limit;}
	
	public boolean isMultMixed() {return isMultMixed;}
	
	public boolean isUseTIForBranching() {return isUseTIForBranching;}
	
	public XDX getCalcICC(){
		return calcICC;
	}
	
	public void reset(){	
		calcICC=null;
		br=0;dbru=0;dbrl=0;dbru_ep=0;dbru_ex=0;dbrl_ex=0;dbru_mc=0;dbrl_mc=0;
		tbr=0;dtbru=0;dtbrl=0;dtbru_ep=0;dtbru_ex=0;dtbrl_ex=0;dtbru_mc=0;dtbrl_mc=0;
		pt=0;dptu=0;dptl=0;dptu_ep=0;dptu_ex=0;dptl_ex=0;dptu_mc=0;dptl_mc=0;
		ptTOT=0;dptuTOT=0;dptlTOT=0;dptuTOT_ep=0;dptuTOT_ex=0;dptlTOT_ex=0;dptuTOT_mc=0;dptlTOT_mc=0;
		
		isAnyLimitInput=false;
		isAnyLimitBXLW=false;
		isAllLimitBXLW=false;
		
		isUseTIForBranching=false;
		
		setEnergy(0,0);
		setIntensity(0,0);
		setTotalIntensity(0,0);
		setMixingRatio(0,0);
		setConversionCoeff(0,0);
		setJiJf(-1000,-1000);
		setMultipolarity("");
		
		BXLWmap.clear();
		adoptedBXLWEntryMap.clear();
		
		largeErrorMap.clear();

	}
	
	
	public static String[] parseMUL(String ms){
		return EnsdfUtil.parseMUL(ms);
	}

	public static String[] expandEMs(String[] EMs,String MUL) {
		return EnsdfUtil.expandEMs(EMs, MUL);
	}
	
	public static boolean isGoodEM(String singlePol){
		return EnsdfUtil.isGoodEMComponent(singlePol);
	}
	
	/*
	 * check if any of variables has limit value
	 * return true if there is
	 */
	public boolean checkIsAnyLimit() {
		
		
		return false;
	}
	public boolean checkIsUseRI() {
		return checkIsUseRI(this.cc);
	}
	
	//for calculating branching from RI
	public boolean checkIsUseRI(double cc1) {
		try {
			boolean useRI=false;
   
            if(ri>0) {
            	double tempCC=cc1;
            	if(tempCC<=0)
            		tempCC=cc;
            	
            	if(tempCC<0)
            		tempCC=0;
            	
                useRI=true;
                if(ti>0) {
                    if(Str.isNumeric(dris) && !Str.isNumeric(dtis)) {
                        useRI=true;
                    }else if(!Str.isNumeric(dris) && Str.isNumeric(dtis)) {
                        useRI=false;
                    }else if(Str.isNumeric(dris) && Str.isNumeric(dtis)) {
                    	if(ti>ri*(1+tempCC))
                    		useRI=false;
                    }else if(dtis.length()>0){//dtis limit
                    	if(dris.length()>0 && ti>ri*(1+tempCC))//dris limit
                    		useRI=false;                   		
                    }else{//dtis empty
                    	if(dris.length()>0 || ti>ri*(1+tempCC))
                    		useRI=false;
                    }
  
                }

            }
            
            return useRI;
		}catch(Exception e) {
			
		}
		
		return true;
	}
	
	public boolean checkIsUseTI() {
		return checkIsUseTI(this.cc);
	}
	//for calculating branching using TI
	public boolean checkIsUseTI(double cc1) {
		try {
			boolean useTI=false;
   
            if(ti>0) {
            	double tempCC=cc1;
            	if(tempCC<=0)
            		tempCC=cc;
            	
            	if(tempCC<0)
            		tempCC=0;
            	
                useTI=true;
                if(ri>0) {
                    if(Str.isNumeric(dtis) && !Str.isNumeric(dris)) {
                        useTI=true;
                    }else if(!Str.isNumeric(dtis) && Str.isNumeric(dris)) {
                        useTI=false;
                    }else if(Str.isNumeric(dtis) && Str.isNumeric(dris)) {
                    	if(ti<ri*(1+tempCC))
                    		useTI=false;
                    }else if(dris.length()>0){//dris limit
                    	if(dtis.length()>0 && ti<ri*(1+tempCC))//dtis limit
                    		useTI=false;                   		
                    }else{//dris empty
                    	if(dtis.length()>0 || ti<ri*(1+tempCC))
                    		useTI=false;
                    }
  
                }

            }
            
            return useTI;
		}catch(Exception e) {
			
		}
		
		return true;
	}
	public XDX getBXLWByErrorOption(String EM,String errorOption) {
		boolean useNormalError=errorOption.toUpperCase().contains("NORMAL");		
		boolean useMCError=errorOption.toUpperCase().contains("MCEP");
		
		/*
		//use Minimum and Maximum approach if any of input variables has limit value.
		if(this.isAnyLimitInput()) {
			useNormalError=false;
			useMCError=false;
		}
		*/
		
       	ResultOfUncertainties bxResult=BXLWmap.get(EM);
    	if(bxResult==null)
    		return null;
	    
    	XDX bx;
    	if(useMCError)
    		bx=bxResult.MCError;
    	else if(useNormalError)
	    	bx=bxResult.normalError;
	    else
	    	bx=bxResult.extremeError;	
 
		return bx;
	}

}
