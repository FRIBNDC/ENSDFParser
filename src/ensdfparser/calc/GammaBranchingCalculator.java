package ensdfparser.calc;

import java.util.Vector;

import ensdfparser.ensdf.DecayMode;
import ensdfparser.ensdf.Gamma;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.SDS2XDX;
import ensdfparser.ensdf.XDX2SDS;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

/*
 * calculation of gamma branching ratios from given intensities, conversion coefficients, 
 * multipolarities, mixing ratios, etc.
 */

public class GammaBranchingCalculator {

    private int z;
    private double mass,t,dtu,dtl;
    
    private String T12LimitOP="";//LT,LE,GT,GE
    
    //probability of gamma+ce decay branch, =100 by default, except for some IT decay, 
    //where level T1/2 has to be corrected for this IT decay probability, T1/2=T1/2(level)/p(IT)
    private double probG,dprobGU,dprobGL;
    
    private double sumTI,dsumTIU_ep,dsumTIL_ep,dsumTIU_ex,dsumTIL_ex;
    private double sumRI,dsumRIU_ep,dsumRIL_ep,dsumRIU_ex,dsumRIL_ex;
    
    //if false, the external BrIcc program will be used to calculate cc in fly
    private boolean isUseInputCC=true;
    
    private boolean isAbsoluteRI=false;//true if intensity is already absolute value
    
    private boolean isAssumeOneForNoMR=false;//true to assume MR=1.0 if no MR is given
    
    private boolean isOnlyCalculateLargeCC=true;//only re-calculate large CC (>0.01) if true,
                                                //otherwise, re-calculate all,
                                                //when is UseInputCC is set to be false (use CC from Bricc)
    private float theoryDCC=0.014f;
    private String theoryDCCType="B";
    
    private float largeCCLimit=0.01f;

    private Vector<GammaBranch> branches=new Vector<GammaBranch>();
    
    private int NPSample=10000;//number of sample point for MC method
    
    public GammaBranchingCalculator(){
        reset();
    }
        
    
    public void setIsUseInputCC(boolean b){isUseInputCC=b;}
    public void setIsOnlyCalculateLargeCC(boolean b){isOnlyCalculateLargeCC=b;}
    public void setIsAssumeOneForNoMR(boolean b){isAssumeOneForNoMR=b;}
    
    public void setLargeCCLimit(float f){largeCCLimit=f;}
    public void setTheoryDCC(float v){theoryDCC=v;}
    public void setTheoryDCCType(String type){theoryDCCType=type;}

    
    public void setMass(double mass){this.mass=mass;}
    public void setZ(int z){this.z=z;}
    public void setT12(double t,double dt){this.t=t;this.dtu=dt;this.dtl=dt;}
    public void setT12(double t,double dtu,double dtl){this.t=t;this.dtu=dtu;this.dtl=dtl;}

    public void setProb(double p,double dp) {this.probG=p;this.dprobGU=dp;this.dprobGL=dp;}
    public void setProb(double p,double dpu,double dpl) {this.probG=p;this.dprobGU=dpu;this.dprobGL=dpl;}
    
    public void setT12LimitOP(String op) {T12LimitOP=op;}
    
    public int Z(){return z;}
    public double mass(){return mass;}
    public double t12(){return t;}
    public double dtu(){return dtu;}
    public double dtl(){return dtl;}
    
    public String T12LimitOP() {return T12LimitOP;}
    
    public double prob(){return probG;}
    public double dpu(){return dprobGU;}
    public double dpl(){return dprobGL;}
    
    public double sumTI() {return sumTI;}
    public double dsumTIU_ep() {return dsumTIU_ep;}
    public double dsumTIL_ep() {return dsumTIL_ep;}
    public double dsumTIU_ex() {return dsumTIU_ex;}
    public double dsumTIL_ex() {return dsumTIL_ex;}
    
    public double sumRI() {return sumRI;}
    public double dsumRIU_ep() {return dsumRIU_ep;}
    public double dsumRIL_ep() {return dsumRIL_ep;}
    public double dsumRIU_ex() {return dsumRIU_ex;}
    public double dsumRIL_ex() {return dsumRIL_ex;}
    
    public boolean isAbsoluteRI() {return isAbsoluteRI;}
    public void setIsAbsoluteRI(boolean b) {isAbsoluteRI=b;}
    
    public boolean isUseInputCC(){return isUseInputCC;}
    public boolean isOnlyCalculateLargeCC(){return isOnlyCalculateLargeCC;}
    public boolean isAssumeOneForNoMR() {return isAssumeOneForNoMR;}
    
    public float getTheoryDCC(){return theoryDCC;}
    public String getTheoryDCCType(){return theoryDCCType;}
    
    public Vector<GammaBranch> branchsV(){return branches;}
    public int nBranches(){return branches.size();}
    public GammaBranch branchAt(int i){return branches.get(i);}
    public ResultOfUncertainties getBXLW(int nb,String EM){return branches.get(nb).getBXLW(EM);}
    
    public void setNumberOfSample(int NP){NPSample=NP;}
    public int getNumberOfSample(){return NPSample;}
    
    public void addBranch(double e,double de){

        GammaBranch gb=new GammaBranch();   
        gb.setEnergy(e,de);
        branches.add(gb);
    }
    
    /*
     * for general case other than gamma branch, like beta branch
     */
    public void addBranch(double e,double de,double ri,double driu,double dril) {
    	addBranch(e,de,ri,driu,dril,"",0,0,0,0,0,0);
    }
    public void addBranch(double e,double de,double ri,double dr) {
    	addBranch(e,de,ri,dr,dr,"",0,0,0,0,0,0);
    }
    
    /*
     * for numerical inputs
     */
    public void addBranch(double e,double de,double ri,double driu,double dril,
            String mult,double mr,double dmru,double dmrl,double cc,double dccu,double dccl){
        GammaBranch gb=new GammaBranch();
        gb.setEnergy(e,de);
        gb.setIntensity(ri,driu,dril);
        gb.setMixingRatio(mr,dmru,dmrl);
        gb.setConversionCoeff(cc,dccu,dccl);
        gb.setMultipolarity(mult);
        
        //debug
        //System.out.println(" eg="+e+" gb.mr="+gb.mr+" mr="+mr+" dmru="+dmru+" dmrl="+dmrl);
        
        branches.add(gb);
    }
    
    /*
     * for general case other than gamma branch, like beta branch
     */
    public void addBranch(String es,String des,String ris,String dris) {
    	addBranch(es,des,ris,dris,"","","","","",false);
    }
    public void addBranch(String es,String des,String ris,String dris,
            String mults,String mrs,String dmrs,String ccs,String dccs){
        addBranch(es,des,ris,dris,mults,mrs,dmrs,ccs,dccs,false);
    }
    /*
     * for text inputs as given in ENSDF file, from which numerical values will be parsed,
     * e.g., for uncertainty=LT,GT, etc
     */
    public void addBranch(String es,String des,String ris,String dris,
            String mults,String mrs,String dmrs,String ccs,String dccs,boolean isT12Limit){
        
        double e=0,de=0,mr=0,dmru=0,dmrl=0,ri=0,driu=0,dril=0,cc=0,dccu=0,dccl=0;

        boolean isRILimit=false,isMRLimit=false,isTILimit=false,isEGLimit=false,isCCLimit=false;
        boolean isMRCrossZero=false;
        
        //true if any of the variables has limit value (GT,GE,LT,LE), or T12 is limit
        //except for some cases under specific conditions
        boolean isAnyLimit=isT12Limit;
        
        String dmrs0=dmrs;
        
        //asymmetric uncertainty can be given in MR field in ENSDF file
        //note that MR values will be converted to and used as positive afterwards
        if(!mrs.isEmpty()){
            
            SDS2XDX MR=new SDS2XDX(mrs,dmrs);
            mr=MR.x();
            
            if(Str.isNumeric(dmrs.replace("+", "").replace("-",""))){
                if(mr>0){
                    dmru=MR.XUV()-mr;
                    dmrl=mr-MR.XLV();   
                }else{
                    dmrl=MR.XUV()-mr;
                    dmru=mr-MR.XLV();
                    mr=-mr;
                }
                
                if(mr>0 && dmrl>=mr) {
                    isMRLimit=true;
                    isAnyLimit=true;
                    isMRCrossZero=true;
                }
                
            }else{//for uncertainty=empty,LT,GT,AP,SY, CA 
                if(dmrs.startsWith("L") || dmrs.startsWith("G"))
                    isMRLimit=true;
                
                if(mrs.startsWith("-")){
                    mrs=mrs.substring(1).trim();
                    if(dmrs.startsWith("L")){
                        dmrs="G"+dmrs.substring(1);
                    }else if(dmrs.startsWith("G")){
                        dmrs="L"+dmrs.substring(1);
                    }
                }
                
                XDX parsedMR=UncertaintyParser.findParsedXDX(mrs,dmrs,dmrs);
                if(parsedMR.dxl>=0){
                    mr=parsedMR.x();
                    dmru=parsedMR.dxu();
                    dmrl=parsedMR.dxl();
                    if(isMRLimit)
                        isAnyLimit=true;
                }else{//should not happen
                    System.out.println("*** something wrong in parsing non-numerical MR uncertainty:"+mrs+" "+dmrs0);
                    mr=Math.abs(mr);
                    dmru=mr*0.5;
                    dmrl=dmru;
                    isAnyLimit=true;
                }

            }
            //System.out.println("k="+k+" eg="+g.EF()+" mrs="+g.MRS()+" dmrs="+g.DMRS()+"  MR.XUV=="+MR.XUV()+" MR.XLV="+MR.XLV()+" line="+g.recordLine());

            
            //if(Math.abs(g.EF()-492.5)<1){
            //  System.out.println("### eg="+g.EF()+"  mr="+mr+" min="+min+" max="+max+"  dmru="+dmru+" dmrl="+dmrl);
            //}
        }
        

        
        //for case, DRI=0 & DRI_S=empty, use DRI=0, this is the case for 
        //ENSDF RI=100 with DRI=empty when PN=6
        
        double rid=0,drid=0;
        SDS2XDX RI=new SDS2XDX(ris,dris);
        rid=RI.x();
        drid=RI.dx();
        
        if(Str.isNumeric(dris) || (dris.isEmpty()&&Math.abs(100-rid)<1E-6)){
            ri=rid;
            driu=drid;
            dril=driu;
            
            if(ri>0 && dril>=ri) {
                isAnyLimit=true;
                isRILimit=true;
            }
            
        }else{//for uncertainty=LT,GT,AP,SY, CA
            if(dris.startsWith("L") || dris.startsWith("G"))
                isRILimit=true;
            
            XDX parsedXDX=UncertaintyParser.findParsedXDX(ris, dris,dris);
            ri=parsedXDX.x();
            driu=parsedXDX.dxu();
            dril=parsedXDX.dxl();
        }

        //if(g.ES().contains("1002.48"))
        //      System.out.println("In GammaBranching line 244: eg="+g.ES()+" g.IS="+g.IS()+" g.DIS="+g.DIS()+"* g.DID="+g.DID()+" ri="+ri+" driu="+driu+" dril="+dril);
        
        //ri=g.ID();
        //driu=g.DID();
        //dril=g.DID();
        
        SDS2XDX EF=new SDS2XDX(es,des);
        e=EF.x();
        de=EF.dx();
        
        if(des.isEmpty()){
            es=es.trim().toUpperCase();
            if(es.indexOf(".")>0 && es.indexOf("E")<0){
                de=0.5;
                if(de>e*0.01)
                    de=e*0.01;
            }else
                de=1.0;//1 keV
        }else if(de<=0) {//ds=AP, CA, LT, ...
            de=20.0;//
            if(des.startsWith("L") || des.startsWith("G")) {
                isAnyLimit=true;
                isEGLimit=true;                
            }

        }
        
        if(de>=e) {
            de=e*0.99999999;
            isAnyLimit=true;
            isEGLimit=true;
        }
        
        //only symmetric uncertainty (len<=2) can be given in ENSDF file
        
        SDS2XDX CC=new SDS2XDX(ccs,dccs);
        cc=CC.x();
        dccu=CC.dx();
        dccl=dccu;
        
        float r=theoryDCC;
        if(cc>0){
            if(dccs.isEmpty()){
                dccu=r*cc;
                dccl=dccu;
            }else if(dccu>=0){
                if(theoryDCCType.equals("H"))
                    dccu=Math.sqrt(dccu*dccu+r*r*cc*cc);
                else if(!theoryDCCType.equals("B"))
                    dccu=r*cc;
                
                dccl=dccu;
                if(cc>0.2 && dccl>=cc) {
                    isAnyLimit=true;
                    isCCLimit=true;
                }
                
            }else{//dccs=LT,GT,AP, ...
                
                if(dccs.startsWith("L") || dccs.startsWith("G")) 
                    isCCLimit=true;
                
                double min=CC.XLV();
                double max=CC.XUV();
                double f=(max-cc)*(mr-cc);
                if(max>min && f>=0){
                    dccu=CC.XUV()-cc;
                    dccl=cc-CC.XLV();
                    
                    //LT,LE, cc<0.2, cc'=0.1+/-0.1 contributes an uncertainty of <10% in (1+cc). So it 
                    //can be treated as regular value instead of limit value.
                    if(cc>0.2 || dccs.indexOf("L")!=0) 
                        isAnyLimit=true;      
                }else{//if uncertainty AP, SY, CA
                    XDX parsedCC=UncertaintyParser.findParsedXDX(ccs,dccs,dccs);
                    if(parsedCC.dxl>=0){
                        dccu=parsedCC.dxu();
                        dccl=parsedCC.dxl();
                    }else{//should not happen
                        System.out.println("*** something wrong in parsing non-numerical CC uncertainty:"+ccs+" "+dccs);
                        dccu=Math.abs(cc*0.5);
                        dccl=dccu;
                    }
                    if(isCCLimit)
                        isAnyLimit=true;
                }

            }
        }
            
        if(e<0) {e=0;de=0;}
        if(de<0) de=0;
        if(ri<0) ri=0;
        if(driu<0) driu=0;
        if(dril<0) dril=0;
        if(dmru<0) dmrl=0;
        if(dmrl<0) dmrl=0;
        if(cc<0) cc=0;
        if(dccu<0) dccu=0;
        if(dccl<0) dccl=0;
        
        addBranch(e, de, ri, driu,dril,mults,mr, dmru, dmrl, cc, dccu, dccl);

        GammaBranch gb=branchsV().lastElement();
        
        String[] EMs=gb.getEMs();
        int nGoodEM=0;
        for(int j=0;j<EMs.length;j++){
            if(GammaBranch.isGoodEM(EMs[j]))
                nGoodEM++;
        }
        
        gb.es=es;
        gb.des=des;
        gb.mrs=mrs;
        gb.dmrs=dmrs;
            
        gb.ris=ris;
        gb.dris=dris;
        gb.mult=mults;
        gb.ccs=ccs;
        gb.dccs=dccs;

        boolean isMultMixed=false;
        if(nGoodEM>1)
            isMultMixed=true;
        
        if(nGoodEM==2 && mrs.isEmpty()){//e.g., MULT=M1+E2, but MR is not given
            if(isAssumeOneForNoMR) {
                gb.setMixingRatio(1, 9999,0.9999);//assuming MR=1.0, MR range=[0.0001,10000]
                isAnyLimit=true;
            }else
                isMultMixed=false;
        }

        gb.setIsMultMixed(isMultMixed);
        
        /////////////////////////////////////
        gb.setIsAnyLimitInput(isAnyLimit);
        /////////////////////////////////////
        
        gb.setIsRILImit(isRILimit);
        gb.setIsTILImit(isTILimit);
        gb.setIsMRLImit(isMRLimit);
        gb.setIsMRCrossZero(isMRCrossZero);
        gb.setIsEGLImit(isEGLimit);
        gb.setIsCCLImit(isCCLimit);
        
        gb.setIsT12Limit(isT12Limit);
        //debug
        //System.out.println(" eg="+e+" gb.mr="+gb.mr+" mr="+mr+" dmru="+dmru+" dmrl="+dmrl);

    }
    
    public void addBranch(Gamma g) {
        addBranch(g,false);
    }
    
    public void addBranch(Gamma g, boolean isT12Limit) {
        double e=0,de=0,mr=0,dmru=0,dmrl=0,ri=0,driu=0,dril=0,cc=0,dccu=0,dccl=0;
        double ti=0,dtil=0,dtiu=0;

        boolean isRILimit=false,isTILimit=false,isMRLimit=false,isCCLimit=false,isEGLimit=false;
        boolean isMRCrossZero=false;
        
        //true if any of the variables has limit value (GT,GE,LT,LE), or T12 is limit
        //except for some cases under specific conditions
        boolean isAnyLimit=isT12Limit;
        
        //asymmetric uncertainty can be given in MR field in ENSDF file
        //note that MR values will be converted to and used as positive afterwards
        if(!g.MRS().isEmpty()){
            String mrs=g.MRS();
            String dmrs=g.DMRS();
            
            SDS2XDX MR=new SDS2XDX(mrs,dmrs);
            mr=MR.x();
            
            if(Str.isNumeric(dmrs.replace("+", "").replace("-",""))){
                if(mr>0){
                    dmru=MR.XUV()-mr;
                    dmrl=mr-MR.XLV();   
                }else{
                    dmrl=MR.XUV()-mr;
                    dmru=mr-MR.XLV();
                    mr=-mr;
                }
                
                if(mr>0 && dmrl>=mr) {
                    isAnyLimit=true;
                    isMRLimit=true;
                    isMRCrossZero=true;
                }
                
            }else{//for uncertainty=empty,LT,GT,AP,SY, CA 
                
                if(dmrs.startsWith("L") || dmrs.startsWith("G"))
                    isMRLimit=true;
                
                if(mrs.startsWith("-")){
                    mrs=mrs.substring(1).trim();
                    if(dmrs.startsWith("L")){
                        dmrs="G"+dmrs.substring(1);
                    }else if(dmrs.startsWith("G")){
                        dmrs="L"+dmrs.substring(1);
                    }
                }
                
                XDX parsedMR=UncertaintyParser.findParsedXDX(mrs,dmrs,dmrs);
                if(parsedMR.dxl>=0){
                    mr=parsedMR.x();
                    dmru=parsedMR.dxu();
                    dmrl=parsedMR.dxl();
                    if(!g.DMRS().isEmpty() && isMRLimit)
                        isAnyLimit=true;
                }else{//should not happen
                    System.out.println("*** something wrong in parsing non-numerical MR uncertainty:"+g.MRS()+" "+g.DMRS());
                    mr=Math.abs(mr);
                    dmru=mr*0.5;
                    dmrl=dmru;
                    isAnyLimit=true;
                }

            }
            //System.out.println("k="+k+" eg="+g.EF()+" mrs="+g.MRS()+" dmrs="+g.DMRS()+"  MR.XUV=="+MR.XUV()+" MR.XLV="+MR.XLV()+" line="+g.recordLine());

            
            //if(Math.abs(g.EF()-492.5)<1){
            //  System.out.println("### eg="+g.EF()+"  mr="+mr+" min="+min+" max="+max+"  dmru="+dmru+" dmrl="+dmrl);
            //}
        }
        

        
        //for case, DRI=0 & DRI_S=empty, use DRI=0, this is the case for 
        //ENSDF RI=100 with DRI=empty when PN=6
        
        //if(g.RIS().equals("100"))
        //	System.out.println("GammaBRCalc 493: gam="+g.ES()+" RI="+g.RIS()+" DRI="+g.DRIS()+"$ altDRIS="+g.altDRIS100()+" DRID="+g.DRID());
        
        
        if(Str.isNumeric(g.DRIS()) || (g.DRIS().isEmpty()&&(g.RID()%100==0||Math.abs(100-g.RID())<1E-6)) ){
            ri=g.RID();
            driu=g.DRID();
            dril=driu;
            if(driu<0) {
            	driu=0;
            	dril=0;
            }
            if(ri>0 && dril>=ri) {
                isAnyLimit=true;
                isRILimit=true;
            }
            
        }else{//for uncertainty=LT,GT,AP,SY, CA
            
            
            if(g.DRIS().startsWith("L") || g.DRIS().startsWith("G")) 
                isRILimit=true;
            
            XDX parsedXDX=UncertaintyParser.findParsedXDX(g.RIS(), g.DRIS(),g.DRIS());
            ri=parsedXDX.x();
            driu=parsedXDX.dxu();
            dril=parsedXDX.dxl();
        }

        //if(g.ES().contains("1002.48"))
        //      System.out.println("In GammaBranchingCalc line 479: eg="+g.ES()+" g.IS="+g.RIS()+" g.DIS="+g.DRIS()+"* g.DID="+g.DRID()+" ri="+ri+" driu="+driu+" dril="+dril);
        
        //ri=g.ID();
        //driu=g.DID();
        //dril=g.DID();
        
        e=g.EF();
        de=g.DEF();
        if(g.DES().isEmpty()){
            String es=g.ES().trim().toUpperCase();
            if(es.indexOf(".")>0 && es.indexOf("E")<0){
                de=0.5;
                if(de>e*0.01)
                    de=e*0.01;
            }else
                de=1.0;//1 keV
        }else if(de<=0) {//ds=AP, CA, LT, ...
            de=20.0;//
            if(g.DES().startsWith("L")||g.DES().startsWith("G")) {
                isAnyLimit=true;
                isEGLimit=true;
            }
        }
        
        if(de>=e) {
            de=e*0.99999999;
            isAnyLimit=true;
            isEGLimit=true;
        }
        
        //only symmetric uncertainty (len<=2) can be given in ENSDF file
        
        cc=g.CCF();
        dccu=g.DCCF();
        dccl=dccu;
        
        float r=theoryDCC;
        if(cc>0){
            if(g.DCCS().isEmpty()){
                dccu=r*cc;
                dccl=dccu;
            }else if(dccu>=0){
                if(theoryDCCType.equals("H"))
                    dccu=Math.sqrt(dccu*dccu+r*r*cc*cc);
                else if(!theoryDCCType.equals("B"))
                    dccu=r*cc;
                
                dccl=dccu;
                if(cc>0.2 && dccl>=cc) {
                    isAnyLimit=true;
                    isCCLimit=true;
                }
                
            }else{//dccs=LT,GT,AP, ...
                String dccs=g.DCCS();
                if(dccs.startsWith("L") || dccs.startsWith("G")) 
                    isCCLimit=true;
                
                SDS2XDX CC=new SDS2XDX(g.CCS(),g.DCCS());
                double min=CC.XLV();
                double max=CC.XUV();
                double f=(max-cc)*(mr-cc);
                if(max>min && f>=0){
                    dccu=CC.XUV()-cc;
                    dccl=cc-CC.XLV();
                    //LT,LE, cc<0.2, contributes an uncertainty of <10% in (1+cc). So it 
                    //can be treated as regular value instead of limit value.
                    if(cc>0.2 || g.DCCS().indexOf("L")!=0) 
                        isAnyLimit=true;      
                }else{//if uncertainty AP, SY, CA
                    XDX parsedCC=UncertaintyParser.findParsedXDX(g.CCS(),g.DCCS(),g.DCCS());
                    if(parsedCC.dxl>=0){
                        dccu=parsedCC.dxu();
                        dccl=parsedCC.dxl();
                    }else{//should not happen
                        System.out.println("*** something wrong in parsing non-numerical CC uncertainty:"+g.CCS()+" "+g.DCCS());
                        dccu=Math.abs(cc*0.5);
                        dccl=dccu;
                    }
                    
                    if(isCCLimit)
                        isAnyLimit=true;
                }

            }
        }
            
        if(e<0) {e=0;de=0;}
        if(de<0) de=0;
        if(ri<0) ri=0;
        if(driu<0) driu=0;
        if(dril<0) dril=0;
        if(dmru<0) dmrl=0;
        if(dmrl<0) dmrl=0;
        if(cc<0) cc=0;
        if(dccu<0) dccu=0;
        if(dccl<0) dccl=0;
        
        addBranch(e, de, ri, driu,dril,g.MS(),mr, dmru, dmrl, cc, dccu, dccl);

        GammaBranch gb=branchsV().lastElement();
        
        String[] EMs=gb.getEMs();
        int nGoodEM=0;
        for(int j=0;j<EMs.length;j++){
            if(GammaBranch.isGoodEM(EMs[j]))
                nGoodEM++;
        }
        
        gb.es=g.ES();
        gb.des=g.DES();
        gb.mrs=g.MRS();
        gb.dmrs=g.DMRS();
            
        gb.ris=g.RIS();
        gb.dris=g.DRIS();
        gb.mult=g.MS();
        gb.ccs=g.CCS();
        gb.dccs=g.DCCS();
        gb.tis=g.TIS();
        gb.dtis=g.DTIS();
        
        boolean isMultMixed=false;
        if(nGoodEM>1)
            isMultMixed=true;
        
        if(nGoodEM==2 && g.MRS().isEmpty()){//e.g., MULT=M1+E2, but MR is not given
            if(isAssumeOneForNoMR) {
                gb.setMixingRatio(1, 9999,0.9999);//assuming MR=1.0, MR range=[0.0001,10000]
                isAnyLimit=true;
            }else
                isMultMixed=false;
        }
        
        gb.setIsMultMixed(isMultMixed);
        
        /////////////////////////////////////
        gb.setIsAnyLimitInput(isAnyLimit);
        /////////////////////////////////////
        
        if(!g.TIS().isEmpty()){
            if(Str.isNumeric(g.DTIS()) || (g.DTIS().isEmpty() &&(g.TID()%100==0||Math.abs(100-g.TID())<1E-6)) ){
                ti=g.TID();
                dtiu=g.DTID();
                dtil=dtiu;
                if(dtiu<0) {
                	dtiu=0;
                	dtil=0;
                }
                
                if(ti>0 && dtil>=ti) {
                    isTILimit=true;
                    if(!isAnyLimit && (!isRILimit||g.RIS().isEmpty()))
                    	isAnyLimit=true;
                }
            }else{
                String dtis=g.DTIS();
                
                if(dtis.startsWith("L") || dtis.startsWith("G")) 
                    isTILimit=true;
    
                XDX parsedXDX=UncertaintyParser.findParsedXDX(g.TIS(), g.DTIS(),g.DTIS());
                ti=parsedXDX.x();
                dtiu=parsedXDX.dxu();
                dtil=parsedXDX.dxl();
            }
            
            gb.setTotalIntensity(ti, dtiu, dtil);
        }
        //System.out.println("GammaBranchingCalc 627: b="+gb.es+" ri="+gb.ri+" dri="+gb.dris+" dtil="+gb.dtil+" dtiu="+gb.dtiu+" g.DTID()"+g.DTID());
        
        gb.setIsRILImit(isRILimit);
        gb.setIsTILImit(isTILimit);
        gb.setIsMRLImit(isMRLimit);
        gb.setIsMRCrossZero(isMRCrossZero);
        gb.setIsEGLImit(isEGLimit);
        gb.setIsCCLImit(isCCLimit);
        
        gb.setIsT12Limit(isT12Limit);
    }
    
    public void addGammaBranches(Level level){
        addGammaBranches(level,true);
    }
    
    public void addGammaBranches(Level level,boolean forceSingleRI){
        
        setMass(level.nucleus().m());
        setZ(level.nucleus().Z());
        
        double t=level.T12D();//note that this is the value in second, might not be the face value in the dataset
                              //it also applies to width value which is converted to halflife in seconds and stored in T12D()
        
        double dtu=0;
        double dtl=0;
        boolean isT12Limit=false;
        
        //parse T1/2
        if(Str.isNumeric(level.T12S())){
            String ds=level.DT12S();
            if(Str.isNumeric(ds) || (ds.contains("+")&&ds.contains("-"))){
                dtu=level.T12UpperD()-t;
                dtl=t-level.T12LowerD();
                
                if(dtl>=t)
                    isT12Limit=true;
            }else if(ds.length()>0){//AP,CA,GT,LT,...
                
                if(ds.startsWith("L") || ds.startsWith("G")) 
                    isT12Limit=true;
                
                T12LimitOP=ds;
                
                XDX parsedXDX=UncertaintyParser.findParsedXDX(level.T12S(), level.DT12S(),level.DT12S());
                
                double T12Multiplier=EnsdfUtil.T12UnitMultiplier(level.T12Unit());
                t=parsedXDX.x()*T12Multiplier;
                dtu=parsedXDX.dxu()*T12Multiplier;
                dtl=parsedXDX.dxl()*T12Multiplier;
                
                //for GT, GE, an upper uncertainty of 10000 times of value is already assumed in findParsedXDX()
                //But for cases of T12, we need more for that limit
                if(level.DT12S().charAt(0)=='G'){
                    dtu=dtu*1E6f;
                }
                
                //System.out.println("   t="+t+"  dtl="+dtl+"  dtu="+dtu+"   T12Multiplier="+T12Multiplier);
            }
        }
        
        //System.out.println("1 e="+level.ES()+" t="+t+" dtu="+dtu+" dtl="+dtl);
        
        if(dtu<0) dtu=0;
        if(dtl<0) dtl=0;
        if(dtl>=t) dtl=t*0.99999999;
        
            
        //System.out.println("2 e="+level.ES()+" t="+t+" dtu="+dtu+" dtl="+dtl);
        
        setT12(t,dtu,dtl);
        
        //parse IT decay branching and other decay modes, e.g., %P if available
        //note that in datasets like (p,g) E=res, there are resonances which decay by both
        //proton and gamma (usually proton decay dominant), but usually decay modes (%P) 
        //is not given here, so simply using level T1/2 to calculate B(XL) is incorrect, 
        //since T1/2 is mostly contributed by proton decay not gamma decay. In the 
        //B(XL) formula, a branching of %(gamma decay) must be multiplied in addition to 
        //%branching of a gamma within the gamma decay channel
        
        double p=100,dpu=0,dpl=0;
        DecayMode dm=level.decayMode("IT");
        if(dm==null)
        	dm=level.decayMode("G");//for gamma; no such mode in current ENSDF format yet
        
        if(dm!=null && Str.isNumeric(dm.value())){
        	XDX xdx=parseDecayModeBranching(dm);
        	p=xdx.x;
        	dpu=xdx.dxu;
        	dpl=xdx.dxl;
        	
            isT12Limit=xdx.isLimit();

        }else if(level.decayModesV().size()>0) {
        	//check other decay modes
        	XDX sum=new XDX(0,0);
        	for(int i=0;i<level.decayModesV().size();i++) {

                dm=level.decayModeAt(i);
                
            	XDX xdx=parseDecayModeBranching(dm);
            	
            	sum=sum.add(xdx);
        	}
        	
        	if(sum.x>=100) {
        		p=dpu=dpl=0;
        	}else if(sum.x>0) {
        		p=100-sum.x;
        		dpu=sum.dxu;
        		dpl=sum.dxl;    			
        	}
        	       
        }
        
        if(p>100)
        	p=100;
        
        if(dpu<0) dpu=0;
        if(dtl<0) dpl=0;
        if(dpl>=p) dpl=p*0.99999999;
        if((p+dpu)>100) dpu=100-p;
        
        setProb(p,dpu,dpl);//value in ENSDF is given as %IT
        

        //debug
        //if(level.ES().contains("1362"))
        //System.out.println(" T="+t+" "+level.T12VS()+"  DTU="+dtu+" DTL="+dtl+" DTS="+level.DT12S());         
        
        
        //add gamma branches
        int ng=level.nGammas();
        float ji=EnsdfUtil.spinValue(level.JPiS());
        float jf=-1;
       
        for(int k=0;k<ng;k++){

            
            Gamma g=level.gammaAt(k);       
            
            addBranch(g,isT12Limit);

            GammaBranch gb=branchsV().lastElement();
            jf=EnsdfUtil.spinValue(g.JFS());
            
            //System.out.println("1 g="+gb.es+" gb.ji="+gb.ji+" gb.jf="+gb.jf+"  g.JFS= "+g.JFS());
            
            gb.setJiJf(ji, jf);
            
            //System.out.println("2 g="+gb.es+" gb.ji="+gb.ji+" gb.jf="+gb.jf+"  g.JFS= "+g.JFS());
            
            if(!Str.isNumeric(g.DRIS()) && !g.DRIS().isEmpty() && ng>1)
                gb.setIsAnyLimitInput(true);
            
            
            //debug
            //System.out.println(" gamma="+g.ES()+" IS="+g.IS()+" ri="+ri+" mult="+g.MS()+" MR="+g.MRS()+" "+g.DMRS());
            //System.out.println("    mr="+mr+" dmru="+dmru+" dmrl="+dmrl+" cc="+g.CCF()+" dcc="+g.DCCF()+" ti="+gb.ti+" g.TIS="+g.TIS()+" g.ID="+g.ID());
        }
        
        if(ng==1 && forceSingleRI){
            branchAt(0).setIntensity(100, 0);//NOTE: force to set ri=100 if there is only one branch
            
            
        }
        
        //debug
        //for(GammaBranch gb:BXLCalc.branchsV())
        //  System.out.println(" eg="+gb.e()+" ri="+gb.ri());

        //calculate absolute and relative gamma branchings
        //No need for this branching calculation since only
        //input gamma intensities are needed for calculating 
        //BXL. Here BranchingCalculator is just used for 
        //re-setting the uncertainties for those intensities 
        //with non-numerical uncertainties, e.g., "AP", "LT"
        //bc.calculate();
    }
    
    private XDX parseDecayModeBranching(DecayMode dm) {
    	return EnsdfUtil.parseDecayModeBranching(dm);
    }
    
    public void reset(){
        setT12(0,0);
        setProb(100,0);
        
        largeCCLimit=0.01f;
        
        isUseInputCC=true;
        isOnlyCalculateLargeCC=true;
        isAbsoluteRI=false;
        
        sumRI=sumTI=0;
        dsumTIU_ep=dsumTIL_ep=dsumTIU_ex=dsumTIL_ex=0;
        dsumRIU_ep=dsumRIL_ep=dsumRIU_ex=dsumRIL_ex=0;
        
        branches.clear();

    }
    
    
    public float findRISum(){
        float out=0;
        for(int i=0;i<branches.size();i++){
            GammaBranch b=branches.get(i);
            if(b.ri<=0)
                continue;
            
            out+=b.ri;
        }
        
        return out;
    }
    
    public float findTISum(){
        float out=0;
        for(int i=0;i<branches.size();i++){
            GammaBranch b=branches.get(i);
            if(b.ri<=0)
                continue;
        
            double cc=b.cc;
            if(cc<0)
                cc=0;
            
            out+=b.ri*(1.0f+cc);
        }
        
        return out;
    }
    
    public void calculateBranchingsFromRI(){
        calculateBranchingsFromRI(true);
    }
    
    public void calculateBranchingsFromRI(boolean includeMC){
        int nb=branches.size();
        double minSumTI=0;
        double maxSumTI=0;
        
        double minSumRI=0;
        double maxSumRI=0;
        
        sumTI=0;
        sumRI=0;
        
        dsumTIU_ep=dsumTIL_ep=dsumTIU_ex=dsumTIL_ex=0;
        dsumRIU_ep=dsumRIL_ep=dsumRIU_ex=dsumRIL_ex=0;
        
        //used for relative uncertainties for normal error propagation
        double dtilSqSum=0,dtiuSqSum=0;
        double[] dtilSQs=new double[nb];
        double[] dtiuSQs=new double[nb];
        
        double drilSqSum=0,driuSqSum=0;
        
        //relative uncertainties from normal error propagation
        double rel_driu=0,rel_dril=0,rel_dbru=0,rel_dbrl=0;
        double rel_dtiu=0,rel_dtil=0,rel_dtbru=0,rel_dtbrl=0;
        double rel_drku=0,rel_drkl=0,rel_dccku=0,rel_dcckl=0;
        double rel_dtu=0,rel_dtl=0,rel_dpu=0,rel_dpl=0;
        
        double[] minTIs=new double[nb];
        double[] maxTIs=new double[nb];
        double[] TIs=new double[nb];
        
        double[] minRIs=new double[nb];
        double[] maxRIs=new double[nb];
        double[] RIs=new double[nb];
        
        double[] minCCs=new double[nb];
        double[] maxCCs=new double[nb];
        
        double[] ccs=new double[nb];
        double[] dccus=new double[nb];
        double[] dccls=new double[nb];
        
        double [] dTIdRI=new double[nb];//dTI/dRI, =0 if TI and RI are not correlated, that is, RI and TI are given with no CC
        
        boolean[] useRIs=new boolean[nb];
        
        if(t>0){
            rel_dtu=dtu/t;
            rel_dtl=dtl/t;
        }
        
        rel_dpu=dprobGU/probG;
        rel_dpl=dprobGL/probG;
        
        Vector<Integer> indexesOfLimitTI=new Vector<Integer>();
        Vector<Integer> indexesOfLimitRI=new Vector<Integer>();
        
        for(int i=0;i<nb;i++){
            GammaBranch b=branches.get(i);
            double cc=b.cc;
            double dccu=b.dccu;
            double dccl=b.dccl;
            
            if(!isUseInputCC){
                if(!isOnlyCalculateLargeCC || cc<0 || cc>largeCCLimit){
                    
                    b.calcICC=BriccsWrap.icc(z,b.es,b.des, b.mult,b.mrs,b.dmrs);
                    cc=b.calcICC.x;
                    dccu=b.calcICC.dxu;
                    dccl=b.calcICC.dxl;
                    
                    //debug
                    //XDX x1=BriccsWrap.icc(45,"207.20","3","M1(+E2)","0.4","LT");
                    //System.out.println("In GammaBranchingCalculator line 158: e="+b.e+" b.mult="+b.mult+" b.mr="+b.mrs+" b.dmr="+b.dmrs+" cc="+cc+" x1="+x1.x+" "+x1.dxl);
                }
            }
            
            if(cc<0) cc=0;
            if(dccu<0) dccu=0;
            if(dccl<0) dccl=0;
            
            ccs[i]=cc;
            dccus[i]=dccu;
            dccls[i]=dccl;
            
            TIs[i]=0;
            minTIs[i]=0;
            maxTIs[i]=0;
            
            
            useRIs[i]=b.checkIsUseRI(cc);
            /*
            useRIs[i]=false;
            if(b.ri>0) {
                useRIs[i]=true;
                if(!Str.isNumeric(b.dris) && Str.isNumeric(b.dtis))
                    useRIs[i]=false;
            }
            */
            //System.out.println(" E="+b.es+"  RI="+b.ri+" useRI="+useRIs[i]+"  "+b.dril+" "+b.driu+" TI="+b.ti+" "+b.dtil+"  "+b.dtiu+" dris="+b.dris+" dtis="+b.dtis);
            
            if(useRIs[i]){
            	RIs[i]=b.ri;
                minRIs[i]=(b.ri-b.dril);
                maxRIs[i]=(b.ri+b.driu);
                
                TIs[i]=b.ri*(1.0+cc);
                minTIs[i]=(b.ri-b.dril)*(1.0+cc-dccl);
                maxTIs[i]=(b.ri+b.driu)*(1.0+cc+dccu);
                
                sumTI+=TIs[i];
                minSumTI+=minTIs[i];
                maxSumTI+=maxTIs[i];    

                
                rel_drku=b.driu/b.ri;
                rel_drkl=b.dril/b.ri;
                if(cc>0){
                    rel_dccku=dccu/cc;
                    rel_dcckl=dccl/cc;
                }else{
                    rel_dccku=0;
                    rel_dcckl=0;
                }
                
                //for normal error propagation
                dtilSQs[i]=Math.pow(b.ri*(1+cc)*rel_drkl,2)+Math.pow(b.ri*cc*rel_dcckl,2);
                dtilSqSum+=dtilSQs[i];
    
                dtiuSQs[i]=Math.pow(b.ri*(1+cc)*rel_drku,2)+Math.pow(b.ri*cc*rel_dccku,2);
                dtiuSqSum+=dtiuSQs[i];
                
                dTIdRI[i]=1+cc;//TI and RI are correlated
                
                if(b.isRILimit) {
                    indexesOfLimitRI.add(i);
                    indexesOfLimitTI.add(i);
                }
            }else if(b.ti>0){
            	if(cc>0) {
                	RIs[i]=b.ti/(1+cc);
                    minRIs[i]=(b.ti-b.dtil)/(1+cc+dccu);
                    maxRIs[i]=(b.ti+b.dtiu)/(1+cc-dccl);
                    
                    dTIdRI[i]=1+cc;//TI and RI are correlated
            	}else {
                	RIs[i]=b.ri;
                    minRIs[i]=(b.ri-b.dril);
                    maxRIs[i]=(b.ri+b.driu);
                    
                    if((b.ti/b.ri-1)<0.05) {
                    	//if ti and ri are very close, meaning cc close to 1.0
                    	//consider ti=cc, correlated
                    	dTIdRI[i]=b.ti/b.ri;
                    }else if (b.dtiu>0&&b.dtil>0&&b.driu>0&&b.dril>0) {
                    	boolean isCorrelated=Math.abs(b.dtiu/b.ti-b.driu/b.ri)<0.1*(b.driu/b.ri)
                    			&&Math.abs(b.dtil/b.ti-b.dril/b.ri)<0.1*(b.dril/b.ri);
                    	isCorrelated=isCorrelated
                    			||((b.driu/b.ri)<=0.05&&(b.dril/b.ri)<=0.05&&(b.dtiu/b.ti)<=0.05&&(b.dtil/b.ti)<=0.05);
                    	if(isCorrelated)
                    		dTIdRI[i]=b.ti/b.ri;
                    	else
                    		dTIdRI[i]=0;
                    }else {
                    	dTIdRI[i]=0;//TI and RI are not correlated
                    }
            	}
            	
            	
                TIs[i]=b.ti;
                minTIs[i]=b.ti-b.dtil;
                maxTIs[i]=b.ti+b.dtiu;
                
                sumTI+=TIs[i];
                minSumTI+=minTIs[i];
                maxSumTI+=maxTIs[i];
                
             	//System.out.println(b.es+"  ri="+RIs[i]+" ti="+TIs[i]);
             	
                //for normal error propagation
                dtilSQs[i]=b.dtil*b.dtil;
                dtilSqSum+=dtilSQs[i];      
                
                dtiuSQs[i]=b.dtiu*b.dtiu;
                dtiuSqSum+=dtiuSQs[i];
                
                if(b.isTILimit) {
                    indexesOfLimitTI.add(i);
                    if(dTIdRI[i]>0)
                    	indexesOfLimitRI.add(i);
                }
            }
            
            if(b.ri>0) {
                sumRI+=b.ri;
                minSumRI+=(b.ri-b.dril);
                maxSumRI+=(b.ri+b.driu);
                
                drilSqSum+=b.dril*b.dril;
                driuSqSum+=b.driu*b.driu;
                
                if(b.isRILimit && !indexesOfLimitRI.contains(i))
                    indexesOfLimitRI.add(i);

            }
            
            //System.out.println("   TI="+TIs[i]+" dtiuSQs[i]="+dtiuSQs[i]+" sum="+dtiuSqSum+"  dtilSQs[i]="+dtilSQs[i]+" sum="+dtilSqSum);
            
            minCCs[i]=cc-dccl;
            maxCCs[i]=cc+dccu;
            
            //if(Math.abs(b.e-415)<1)
            //System.out.println("i="+i+" eg="+b.e+" ri="+b.ri+"  "+b.driu+"  "+b.dril+" cc="+cc+" dccl="+dccl+" dccu="+dccu+" minSum"+minSum+" maxSum"+maxSum);
        }
        
        float deltaForAbsoluteRI=0;//0 or 1
        if(isAbsoluteRI) {       	
        	deltaForAbsoluteRI=1.0f;
        	
        	sumTI=100;
        	minSumTI=100;
        	maxSumTI=100;
        }
        
        float delta=deltaForAbsoluteRI;
        /*
         * bri=ri/sum[rk*(1+cck)],k=1-n,          it increases as ri increases and as rk (k!=i) and cck (k=1-n) decreases
         * tbri=ri*(1+cci)/sum[rk*(1+cck], k=1-n, it increases as ri and cci increases and as rk (k!=i) and cck (k!=i) decreases 
         * pti=t/bri
         */
        if(sumTI>0 && minSumTI>=0 && maxSumTI>0){
            dsumTIU_ex=maxSumTI-sumTI;
            dsumTIL_ex=sumTI-minSumTI;
            
            dsumRIU_ex=maxSumRI-sumRI;
            dsumRIL_ex=sumRI-minSumRI;


            double dtiuSqSum1=dtiuSqSum;
            double dtilSqSum1=dtilSqSum;
            double extraDtiu=0,extraDtil=0;
            for(int i=0;i<indexesOfLimitTI.size();i++) {
                int index=indexesOfLimitTI.get(i).intValue();
                dtiuSqSum1-=dtiuSQs[index];
                extraDtiu+=Math.sqrt(dtiuSQs[index]);

                dtilSqSum1-=dtilSQs[index];
                extraDtil+=Math.sqrt(dtilSQs[index]);
                
                //System.out.println("For TI: index="+index+"  ri="+branches.get(index).ri);
            }
            
            //for limit values, do not add their uncertainties in quadrature
            //instead, add them arithmetically
            if(dtiuSqSum1<0)
                dtiuSqSum1=0;
            if(dtilSqSum1<0)
                dtilSqSum1=0;
            
            dsumTIU_ep=(1-delta)*(Math.sqrt(dtiuSqSum1)+extraDtiu);//=0 if RI
            dsumTIL_ep=(1-delta)*(Math.sqrt(dtilSqSum1)+extraDtil);
    
            //debug
            //System.out.println("sumTI="+sumTI+" dsumTIU_ep="+dsumTIU_ep+"   old="+Math.sqrt(dtiuSqSum));
            //System.out.println("           dsumTIL_ep="+dsumTIL_ep+"   old="+Math.sqrt(dtiuSqSum));
            
            double driuSqSum1=driuSqSum;
            double drilSqSum1=drilSqSum;
            double extraDriu=0,extraDril=0;
            for(int i=0;i<indexesOfLimitRI.size();i++) {
                int index=indexesOfLimitRI.get(i).intValue();
                GammaBranch b=branches.get(index);
                if(b.ri>0) {
                    driuSqSum1-=b.driu*b.driu;
                    extraDriu+=b.driu;

                    drilSqSum1-=b.dril*b.dril;
                    extraDril+=b.dril;
                    
                    //System.out.println("For RI: "+b.es+" index="+index+"  ri="+branches.get(index).ri+" driuSqSum="+driuSqSum+" driuSqSum1="+driuSqSum1
                    //        +" driu*driu="+(b.driu*b.driu)+" driu="+b.driu+" extraDriu="+extraDriu+" dril="+b.dril+" extraDril="+extraDril+" "+indexesOfLimitRI.size()+"  "+nb);
                }    
            }
            
            if(driuSqSum1<0)
                driuSqSum1=0;
            if(drilSqSum1<0)
                drilSqSum1=0;
            
            dsumRIU_ep=Math.sqrt(driuSqSum1)+extraDriu;
            dsumRIL_ep=Math.sqrt(drilSqSum1)+extraDril;
                     
            //debug
            //System.out.println("sumRI="+sumRI+" dsumRIU_ep="+dsumRIU_ep+"   old="+Math.sqrt(driuSqSum)+" driuSqSum1="+driuSqSum1+" extraDriu="+extraDriu);
            //System.out.println("           dsumRIL_ep="+dsumRIL_ep+"   old="+Math.sqrt(driuSqSum)+" drilSqSum1="+drilSqSum1+" extraDri1="+extraDril);
            
            for(int i=0;i<nb;i++){
                GammaBranch b=branches.get(i);
                if(b.ri<=0 && b.ti<=0)
                    continue;
                
                double minTI=minTIs[i];
                double maxTI=maxTIs[i];

                double minRI=minRIs[i];
                double maxRI=maxRIs[i];
                
                
                //System.out.println("   TI="+TIs[i]+" max="+max+"  min="+min);
                b.tbr=TIs[i]/sumTI*100;
                b.dtbrl_ex=(1-delta)*(b.tbr-minTI/(minTI+maxSumTI-maxTI)*100)+delta*(b.tbr-minTI);
                b.dtbru_ex=(1-delta)*(maxTI/(maxTI+minSumTI-minTI)*100-b.tbr)+delta*(maxTI-b.tbr); 
                
                if(b.dtbrl_ex<0) b.dtbrl_ex=0;
                if(b.dtbru_ex<0) b.dtbru_ex=0;
                if(b.dtbrl_ex>b.tbr) b.dtbrl_ex=b.tbr;
                
                //double min1=(b.ri-b.dril)*(1.0+maxCCs[i]);
                //double max1=(b.ri+b.driu)*(1.0+minCCs[i]);
                
                b.br=RIs[i]/sumTI*100;    
                
                if(dTIdRI[i]>0) {
                    b.dbrl_ex=(1-delta)*(b.br-minRI/(minTI+maxSumTI-maxTI)*100)+delta*(b.br-minRI);
                    b.dbru_ex=(1-delta)*(maxRI/(maxTI+minSumTI-minTI)*100-b.br)+delta*(maxRI-b.br); 
                }else {//TI and RI not correlated
                    b.dbrl_ex=(1-delta)*(b.br-minRI/maxSumTI*100)+delta*(b.br-minRI);
                    b.dbru_ex=(1-delta)*(maxRI/minSumTI*100-b.br)+delta*(maxRI-b.br); 
                }

                //System.out.println(dTIdRI[i]);
                //System.out.println(b.es+"  br="+b.br+" dbr ex="+b.dbru_ex+"  "+b.dbrl_ex+"  minRI="+minRI+" minTI="+minTI+" maxRI="+maxRI+" maxTI="+maxTI);
                //System.out.println(b.es+"  tbr="+b.tbr+" dtbr ex="+b.dtbru_ex+"  "+b.dtbrl_ex+"  minRI="+minRI+" minTI="+minTI+" maxRI="+maxRI+" maxTI="+maxTI);

                if(b.dbrl_ex<0) b.dbrl_ex=0;
                if(b.dbru_ex<0) b.dbru_ex=0;
                if(b.dbrl_ex>b.br) b.dbrl_ex=b.br;
                
                
                //System.out.println(b.es+"  ri="+RIs[i]+" ti="+TIs[i]+" br="+b.br+"  tbr="+b.tbr);
                
                if(b.br>0){
                    b.pt=t*100/b.br*100/probG;
                    b.dptl_ex=b.pt-(t-dtl)*100/(b.br+b.dbru_ex)*100/(probG+dprobGU);
                    if(b.dptl_ex>b.pt) b.dptl_ex=b.pt;

                    
                    if(b.br>b.dbrl_ex && probG>dprobGL)
                        b.dptu_ex=(t+dtu)*100/(b.br-b.dbrl_ex)*100/(probG-dprobGL)-b.pt;
                    else
                        b.dptu_ex=1000000;
                    
                    //System.out.println(b.pt+"  "+b.dptl_ex+" t="+t+" dtl="+dtl+" br="+b.br+" dbru_ex="+b.dbru_ex+" probG="+probG+" dprobGU="+dprobGU);
                    
                    if(b.dptl_ex<0) b.dptl_ex=0;
                    if(b.dptu_ex<0) b.dptu_ex=0;
                    
                  //System.out.println(b.pt+"  "+b.dptl_ex+" t="+t+" dtl="+dtl+" br="+b.br+" dbru_ex="+b.dbru_ex+" probG="+probG+" dprobGU="+dprobGU);
                }
                
                if(b.tbr>0){
                    b.ptTOT=t*100/b.tbr*100/probG;
                    b.dptlTOT_ex=b.ptTOT-(t-dtl)*100/(b.tbr+b.dtbru_ex)*100/(probG+dprobGU);
                    if(b.dptlTOT_ex>b.ptTOT) b.dptlTOT_ex=b.ptTOT;
                    
                    if(b.tbr>b.dtbrl_ex && probG>dprobGL)
                        b.dptuTOT_ex=(t+dtu)*100/(b.tbr-b.dtbrl_ex)*100/(probG-dprobGL)-b.ptTOT;
                    else
                        b.dptuTOT_ex=1000000;
                    
                    if(b.dptlTOT_ex<0) b.dptlTOT_ex=0;
                    if(b.dptuTOT_ex<0) b.dptuTOT_ex=0;
                }
                //if(Math.abs(b.e-492.97)<0.1) {
                //	System.out.println("GammaBranchingCalculator 1183:  eg="+b.e+" ri="+b.ri+" "+b.dril+" "+b.driu
                //			+"\n   sumTI="+sumTI+" br.ex="+b.br+"  "+b.dbru_ex+" "+b.dbrl_ex
                //			+"\n   t="+t+" dtu="+dtu+" dtl="+dtl+"  b.pt="+b.pt+"  dptu="+b.dptu_ex+" dptl="+b.dptl_ex
                //			+"\n   p="+probG+" dpu="+dprobGU+" dpl="+dprobGL);
                //}
                
                
                //get uncertainty from normal error propagation
                double cc=ccs[i];
                double dccu=dccus[i];
                double dccl=dccls[i];
                
                if(cc>0){
                    rel_dccku=dccu/cc;
                    rel_dcckl=dccl/cc;
                }else{
                    rel_dccku=0;
                    rel_dcckl=0;
                }
                
                if(useRIs[i]){       
                	//in this case, TI and RI are correlated
                    rel_driu=b.driu/b.ri;
                    rel_dril=b.dril/b.ri;
                    if(rel_driu<0) rel_driu=0;
                    if(rel_dril<0) rel_dril=0;
                    
                    rel_dbru=dtilSqSum-dtilSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_driu*rel_driu+Math.pow(b.ri*cc*rel_dcckl,2);
                    rel_dbru=(1-delta)*Math.sqrt(rel_dbru)/sumTI+delta*rel_driu;
                    b.dbru_ep=rel_dbru*b.br;
                    
                    rel_dbrl=dtiuSqSum-dtiuSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_dril*rel_dril+Math.pow(b.ri*cc*rel_dccku,2);
                    rel_dbrl=(1-delta)*Math.sqrt(rel_dbrl)/sumTI+delta*rel_dril;
                    b.dbrl_ep=rel_dbrl*b.br;
                    
                    
                    if(b.dbrl_ep>b.br) b.dbrl_ep=b.br;
                    
                    rel_dtbru=dtilSqSum-dtilSQs[i]+Math.pow(sumTI-TIs[i],2)*(rel_driu*rel_driu+Math.pow(dccu/(1+cc),2));
                    rel_dtbru=Math.sqrt(rel_dtbru)/sumTI;
                    b.dtbru_ep=rel_dtbru*b.tbr;
                    
                    rel_dtbrl=dtiuSqSum-dtiuSQs[i]+Math.pow(sumTI-TIs[i],2)*(rel_dril*rel_dril+Math.pow(dccl/(1+cc),2));
                    rel_dtbrl=Math.sqrt(rel_dtbrl)/sumTI;
                    b.dtbrl_ep=rel_dtbrl*b.tbr;
                    
                    if(b.dtbrl_ep>b.tbr) b.dtbrl_ep=b.tbr;
                    
                    if(b.pt>0) {
                        b.dptu_ep=b.pt*Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl+rel_dpl*rel_dpl);
                        b.dptl_ep=b.pt*Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru+rel_dpu*rel_dpu);
                        
                        if(b.dptl_ep>b.pt)
                            b.dptl_ep=b.pt;
                    }
                   
                    if(b.ptTOT>0) {
                        b.dptuTOT_ep=b.ptTOT*Math.sqrt(rel_dtu*rel_dtu+rel_dtbrl*rel_dtbrl+rel_dpl*rel_dpl);
                        b.dptlTOT_ep=b.ptTOT*Math.sqrt(rel_dtl*rel_dtl+rel_dtbru*rel_dtbru+rel_dpu*rel_dpu);
                        
                        if(b.dptlTOT_ep>b.ptTOT)
                            b.dptlTOT_ep=b.ptTOT;
                    }
                
                    b.setIsBRLImit(b.isRILimit);
                    
                    //System.out.println("****E="+b.es+" ri="+b.ri+"  "+b.driu+"  "+b.dril+"  br="+b.br()+" "+b.dbru()+" "+b.dbrl()+" sumTI="+sumTI+" dsumTI="+dsumTIU_ep);
                    
                    //System.out.println("**     br.ep="+b.br+"  "+b.dbru_ep+" "+b.dbrl_ep);
                    //System.out.println("       br.ex="+b.br+"  "+b.dbru_ex+" "+b.dbrl_ex);
                    //System.out.println("       ri="+b.ri+"  "+b.driu+"  "+b.dril+" rel_dbru="+rel_dbru+" rel_driu="+rel_driu);
                }else if(b.ti>0){

                    rel_dtiu=b.dtiu/b.ti;
                    rel_dtil=b.dtil/b.ti;
                    if(rel_dtiu<0) rel_dtiu=0;
                    if(rel_dtil<0) rel_dtil=0;
                    
                    rel_dtbru=dtilSqSum-dtilSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_dtiu*rel_dtiu;
                    rel_dtbru=(1-delta)*Math.sqrt(rel_dtbru)/sumTI+delta*rel_dtiu;
                    b.dtbru_ep=rel_dtbru*b.tbr;
                    
                    rel_dtbrl=dtiuSqSum-dtiuSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_dtil*rel_dtil;
                    rel_dtbrl=(1-delta)*Math.sqrt(rel_dtbrl)/sumTI+delta*rel_dtil;
                    b.dtbrl_ep=rel_dtbrl*b.tbr;
                                       
                    if(b.dtbrl_ep>b.tbr) b.dtbrl_ep=b.tbr;
                    
                	//TI and RI could be corrected, or not (e.g., only RI and TI are given with no cc)
                	if(dTIdRI[i]<=0) { //if >0, TI and RI are correlated, TI=RI*(1+cc), otherwise not correlated, f=0
                        rel_driu=b.driu/b.ri;
                        rel_dril=b.dril/b.ri;
                        if(rel_driu<0) rel_driu=0;
                        if(rel_dril<0) rel_dril=0;
                        
                        rel_dbru=dtilSqSum+Math.pow(sumTI,2)*rel_driu*rel_driu;
                        rel_dbru=(1-delta)*Math.sqrt(rel_dbru)/sumTI+delta*rel_driu; 
                        
                        rel_dbrl=dtiuSqSum+Math.pow(sumTI,2)*rel_dril*rel_dril;
                        rel_dbrl=(1-delta)*Math.sqrt(rel_dbrl)/sumTI+delta*rel_dtil;
                	}else {
                        rel_dbru=rel_dtbru*rel_dtbru+Math.pow(dccl/(1+cc),2);
                        rel_dbru=Math.sqrt(rel_dbru);
                        
                        rel_dbrl=rel_dtbrl*rel_dtbrl+Math.pow(dccu/(1+cc),2);
                        rel_dbrl=Math.sqrt(rel_dbrl);
                	}
                 

                    b.dbru_ep=rel_dbru*b.br;
                    b.dbrl_ep=rel_dbrl*b.br;

                    if(b.dbrl_ep>b.br) b.dbrl_ep=b.br;
                    
                    if(b.pt>0) {
                        b.dptu_ep=b.pt*Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl+rel_dpl*rel_dpl);
                        b.dptl_ep=b.pt*Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru+rel_dpu*rel_dpu);
                        
                        if(b.dptl_ep>b.pt)
                            b.dptl_ep=b.pt;
                    }
                    
                    if(b.ptTOT>0) {
                        b.dptuTOT_ep=b.ptTOT*Math.sqrt(rel_dtu*rel_dtu+rel_dtbrl*rel_dtbrl+rel_dpl*rel_dpl);
                        b.dptlTOT_ep=b.ptTOT*Math.sqrt(rel_dtl*rel_dtl+rel_dtbru*rel_dtbru+rel_dpu*rel_dpu);
                        
                        if(b.dptlTOT_ep>b.ptTOT)
                            b.dptlTOT_ep=b.ptTOT;
                    }

                    b.setIsBRLImit(b.isTILimit);
                }   
                
                //System.out.println(b.es+"  br="+b.br+" dbr ep="+b.dbru_ep+"  "+b.dbrl_ep+"  minRI="+minRI+" minTI="+minTI+" maxRI="+maxRI+" maxTI="+maxTI);
                //System.out.println(b.es+"  tbr="+b.tbr+" dtbr ep="+b.dtbru_ep+"  "+b.dtbrl_ep+"  minRI="+minRI+" minTI="+minTI+" maxRI="+maxRI+" maxTI="+maxTI);

                //System.out.println(b.es+"  ri="+RIs[i]+" ti="+TIs[i]+" br="+b.br+" "+b.dbru_ep+" "+b.dbrl_ep+" tbr="+b.tbr+" "+b.dtbru_ep+" "+b.dtbrl_ep);
                //if(Math.abs(b.e-822.6)<1){
                //  System.out.println("***t="+t+" pt="+b.pt+"  br="+b.br+"  "+b.dbru_ep+"  "+b.dbrl_ep+" tbr="+b.tbr+"  "+b.dtbru_ep+"  "+b.dtbrl_ep+" min"+min+" max"+max+" minSum"+minSumTI+" maxSum"+maxSumTI);
                //  System.out.println("sum="+sumTI+" b.ri*(1+cc)="+b.ri*(1+cc)+" pt="+b.pt+"  ptu="+b.dptu_ep+"  "+b.dptl_ep);
                //  System.out.println("  ri="+b.ri+" "+b.driu+" "+b.dril+" ti="+TIs[i]+" "+max1+" "+min1+" rel_dccku="+rel_dccku);
                //}
            }
            
            if(includeMC)
                calculateBranchingsFromRI_MC(ccs,dccus,dccls);
        }
    }
        
    public void calculateBranchingsFromRI_MC(double[] ccs,double[] dccus,double[] dccls) {
        calculateBranchingsFromRI_MC(ccs,dccus,dccls,NPSample);//NP<=0 use default NP in MCEP
    }
    
    public void calculateBranchingsFromRI_MC(double[] ccs,double[] dccus,double[] dccls,int NP) {
        int nb=branches.size();
        if(nb<1)
            return;
        
        if(nb==1) {
        	GammaBranch b=branches.get(0);
        	
            b.dptu_mc=b.dptu_ex;
            b.dptl_mc=b.dptl_ex; 
            
            b.dptuTOT_mc=b.dptuTOT_ex;
            b.dptlTOT_mc=b.dptlTOT_ex;          
        }
        
        double cc,dccu,dccl;
        double br=0,tbr=0,ri=0,driu=0,dril=0,ti=0,dtiu=0,dtil=0;
        MCErrorPropagation MCEP=new MCErrorPropagation(NP);

        //The following works only when there are RI and CC, but there are cases
        //where there are RI and TI, or cases where there are TI and CC
        
        /*
        MCErrorPropagation.FunctionMD fBR=(pars)->{//par[2*k]=RI,par[2*k+1]=CC
            double sum=0;
            for(int i=0;i<pars.length/2;i++) sum+=pars[2*i]*(1+pars[2*i+1]);
            return sum==0?0:100*(pars[0]/sum);
        };
        
        MCErrorPropagation.FunctionMD fTBR=(pars)->{
            double sum=0;
            for(int i=0;i<pars.length/2;i++) sum+=pars[2*i]*(1+pars[2*i+1]);
            return sum==0?0:100*(pars[0]*(1+pars[1])/sum);
        };
        
        MCErrorPropagation.FunctionMD fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
            if(pars[1]>0)
                return 100*pars[0]/pars[1];         
            return 0;
        };
        */
        
        String[] parNames=new String[2*nb];
        
        boolean[] useRIs=new boolean[nb];
        for(int i=0;i<nb;i++){
            GammaBranch b=branches.get(i);  
            
            //System.out.println("**     br.ep="+b.br+"  "+b.dbru_ep+" "+b.dbrl_ep+" b.ri="+b.ri+" b.ti="+b.ti);
            useRIs[i]=b.checkIsUseRI();
            /*
            useRIs[i]=false;
            if(b.ri>0) {
                useRIs[i]=true;
                if(!Str.isNumeric(b.dris) && Str.isNumeric(b.dtis))
                    useRIs[i]=false;
            }
            */
        }
        
        for(int i=0;i<nb;i++){
            GammaBranch b=branches.get(i);
            
            MCEP.reset();
            
            cc=ccs[i];
            dccu=dccus[i];
            dccl=dccls[i];
            
            ri=0;driu=0;dril=0;
            
            boolean canGetBR=false,canGetTBR=false;
            boolean added=false;
            int count=0;
            ri=b.ri;
            driu=b.driu;
            dril=b.dril;
            if(ri<0) ri=0;
            if(driu<0) driu=0;
            if(dril<0) dril=0;
            
            ti=b.ti;
            dtiu=b.dtiu;
            dtil=b.dtil;
            if(ti<0) ti=0;
            if(dtiu<0) dtiu=0;
            if(dtil<0) dtil=0;
            
            if(useRIs[i]){
               
                if(cc>=0) {
                	canGetTBR=true;
                	canGetBR=true;
                }                
            	
                MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                parNames[2*count]="ri";parNames[2*count+1]="cc";
                added=true;		
            }else if(b.ti>0){
            	
                //set ri to be b.ti for TI sum purpose
                //ri=b.ti;
                //driu=b.dtiu;
                //dril=b.dtil;
                
                canGetTBR=true;
                if(cc>=0)
                	canGetBR=true;
                
                
                //cc=0;
                //dccu=0;
                //dccl=0;
                
                //two scenarios:
                //1. ti with cc, ri empty
                //2. ti and ri, cc empty
                

                if(ri>0 && cc<=0) {
                    MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                    MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                    parNames[2*count]="ri";parNames[2*count+1]="ti";
                    added=true;	
                }else if(ri<=0 && cc>=0) {
                    MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                    MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                    parNames[2*count]="ti";parNames[2*count+1]="cc";
                    added=true;	
                }
            }
            
            if(!added) {
                MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                parNames[2*count]="ri";parNames[2*count+1]="cc";
            }
            count++;
            
            for(int j=0;j<nb;j++){
                if(j==i)
                    continue;
                
                added=false;
                
                GammaBranch bj=branches.get(j);
                
                cc=ccs[j];
                dccu=dccus[j];
                dccl=dccls[j];
                
                ri=0;driu=0;dril=0;
                ri=bj.ri;
                driu=bj.driu;
                dril=bj.dril;
                if(ri<0) ri=0;
                if(driu<0) driu=0;
                if(dril<0) dril=0;
                
                ti=bj.ti;
                dtiu=bj.dtiu;
                dtil=bj.dtil;
                if(ti<0) ti=0;
                if(dtiu<0) dtiu=0;
                if(dtil<0) dtil=0;
                
                if(useRIs[j]){                   
                    MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                    MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                    parNames[2*count]="ri";parNames[2*count+1]="cc";
                    added=true;
                }else if(bj.ti>0){
                	/*
                    //set ri to be b.ti for TI sum purpose
                    ri=bj.ti;
                    driu=bj.dtiu;
                    dril=bj.dtil;
                    
                    cc=0;
                    dccu=0;
                    dccl=0;
                    */
                	
                    if(ri>0 && cc<=0) {
                        MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                        MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                        parNames[2*count]="ri";parNames[2*count+1]="ti";
                        added=true;	
                    }else if(ri<=0 && cc>=0) {
                        MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                        MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                        parNames[2*count]="ti";parNames[2*count+1]="cc";
                        added=true;	
                    }
                }
                if(!added) {
                    MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                    MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                    parNames[2*count]="ri";parNames[2*count+1]="cc";
                }

                count++;
            }
            
            
            
            MCErrorPropagation.FunctionMD fBR=(pars)->{
                double sum=0,r0=0,tempR0=0;
                for(int k=0;k<pars.length/2;k++) {
                	String name1=parNames[2*k];
                	String name2=parNames[2*k+1];
                	tempR0=0;
                	if(name1.equals("ri")) {
                		if(name2.equals("cc")) {
                        	sum+=pars[2*k]*(1+pars[2*k+1]);
                		}else if(name2.equals("ti")) {
                        	sum+=pars[2*k+1];
                		}
                		tempR0=pars[2*k];
                	}else if(name1.equals("ti")&&name2.equals("cc")) {
                    	sum+=pars[2*k];
                    	if(pars[2*k+1]>0)
                    		tempR0=pars[2*k]/(1+pars[2*k+1]);
                    	else
                    		tempR0=pars[2*k];
                	}

                	if(k==0)
                		r0=tempR0;
                }
                return sum==0?0:100*(r0/sum);
            };
            
            MCErrorPropagation.FunctionMD fTBR=(pars)->{
                double sum=0,t0=0,tempT0=0;
                for(int k=0;k<pars.length/2;k++) {
                	String name1=parNames[2*k];
                	String name2=parNames[2*k+1];
                	tempT0=0;
                	if(name1.equals("ri")) {
                		if(name2.equals("cc")) {
                        	sum+=pars[2*k]*(1+pars[2*k+1]);
                        	tempT0=pars[2*k]*(1+pars[2*k+1]);
                		}else if(name2.equals("ti")) {
                        	sum+=pars[2*k+1];
                        	tempT0=pars[2*k+1];
                		}
                	}else if(name1.equals("ti")&&name2.equals("cc")) {
                    	sum+=pars[2*k];
                    	tempT0=pars[2*k];
                	}

                	if(k==0)
                		t0=tempT0;
                }
                
                return sum==0?0:100*(t0/sum);
            };
            
            MCErrorPropagation.FunctionMD fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
                if(pars[1]>0)
                    return 100*pars[0]/pars[1];         
                return 0;
            };
            
            if(canGetBR && (nb>1||(nb==1&&cc>0)) ) {
            	if(isAbsoluteRI) {//intensity is already absolute, no need to re-calculate it using MC
            		//must call calculateBranchingsFromRI() before this call    
                    b.dbrl_mc=b.dbrl_ex;
                    b.dbru_mc=b.dbru_ex;
            	}else {
                    MCEP.setFunction(fBR);
                    
                    //System.out.println("GammaBranchingCalc 1745:#########################"+b.ris+" "+b.isRILimit);
                    
                    MCEP.doWork();
                    
                    //System.out.println("GammaBranchingCalc 1747:#########################"+b.ris);
                    
                    br=MCEP.getResult().v;
                    b.dbru_mc=MCEP.getResult().dvu;
                    b.dbrl_mc=MCEP.getResult().dvl;     
                    if(b.dbrl_mc>b.br)
                        b.dbrl_mc=b.br;
                    
                    //System.out.println("GammaBranchingCalc 1516:  br.ep="+b.br+"  "+b.dbru_ep+" "+b.dbrl_ep+" b.ri="+b.ri+" b.ti="+b.ti);
                    //System.out.println("  e="+b.e+"   br.mc="+br+"  "+b.dbru_mc+" "+b.dbrl_mc);
                    
                    if(b.br>0 && br>0 && Math.abs(b.br-br)>0.005*br) {
                        System.out.println("Something is wrong in calculating gamma branching from RI using Monte-Carlo approach: ");
                        System.out.println("     branch at: EG="+b.es+" RI="+b.ris()+" MUL="+b.mult);
                    }
            	}

            }
            
            if(canGetTBR && nb>1){
            	if(isAbsoluteRI) {//intensity is already absolute, no need to re-calculate it using MC
            		//must call calculateBranchingsFromRI() before this call    
                    b.dtbrl_mc=b.dtbrl_ex;
                    b.dtbru_mc=b.dtbru_ex;
            	}else {
                    MCEP.setFunction(fTBR);
                    MCEP.doWork();          
                    tbr=MCEP.getResult().v;
                    b.dtbru_mc=MCEP.getResult().dvu;
                    b.dtbrl_mc=MCEP.getResult().dvl;
                    if(b.dtbrl_mc>b.tbr)
                        b.dtbrl_mc=b.tbr;
                    
                    //System.out.println("GammaBranchingCalc 1542:  tbr.ep="+b.tbr+"  "+b.dtbru_ep+" "+b.dtbrl_ep+" b.ri="+b.ri+" b.ti="+b.ti);
                    //System.out.println("  e="+b.e+"   br.mc="+tbr+"  "+b.dtbru_mc+" "+b.dtbrl_mc);
                    
                    if(tbr>0 && b.tbr>0 && Math.abs(b.tbr-tbr)>0.005*tbr) {
                        System.out.println("Something is wrong in calculating total branching from RI using Monte-Carlo approach.");
                        System.out.println("     branch at: EG="+b.es+" RI="+b.ris()+" MUL="+b.mult);
                    }
            	}

                //System.out.println("**     br.ep="+b.br+"  "+b.dbru_ep+" "+b.dbrl_ep+" b.ri="+b.ri);
                //System.out.println("  e="+b.e+"   br.mc="+br+"  "+b.dbru_mc+" "+b.dbrl_mc);
                //System.out.println("              tbr.mc="+tbr+"  "+b.dtbru_mc+" "+b.dtbrl_mc);
            }

            if(br>0){
                MCEP.reset();   
                MCEP.addParam("t",t,dtu,dtl,Math.max(0,t-100*dtl),t+100*dtu,b.isT12Limit);
                MCEP.addParam("br",br,b.dbru_mc,b.dbrl_mc,Math.max(0,br-100*b.dbrl_mc),br+100*b.dbru_mc,b.isBRLimit);
                
                if(probG!=100 || dprobGU!=0 || dprobGL!=0) {
                    fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
                        if(pars[1]>0)
                            return 100*pars[0]/pars[1]*100/pars[2];         
                        return 0;
                    };
                    
                    boolean isProbLimit=false;
                    if(dprobGL>=0.99*probG || dprobGU>=10*probG)
                    	isProbLimit=true;
                    
                    MCEP.addParam("prob",probG,dprobGU,dprobGL,Math.max(0,probG-50*dprobGL),Math.min(100, probG+50*dprobGU),isProbLimit);   
                }

                MCEP.setFunction(fPT12);
                MCEP.doWork();
                b.dptu_mc=MCEP.getResult().dvu;
                b.dptl_mc=MCEP.getResult().dvl;
                if(b.dptl_mc>b.pt)
                    b.dptl_mc=b.pt;
                
                //System.out.println("  GammaBranchingCalc 1524: t="+t+" dtu="+dtu+" dtl="+dtl+" isLimit="+b.isT12Limit+" pt="+b.pt
                //		+"  mc results partial T: pt="+b.pt+" dtu="+b.dptu_mc+" dtl="+b.dptl_mc);

            }
            
            if(tbr>0){
                MCEP.reset();   
                MCEP.addParam("t",t,dtu,dtl,Math.max(0,t-100*dtl),t+100*dtu,b.isT12Limit);
                MCEP.addParam("tbr",br,b.dtbru_mc,b.dtbrl_mc,Math.max(0,tbr-100*b.dtbrl_mc),br+100*b.dtbru_mc,b.isBRLimit);
                
                if(probG!=100 || dprobGU!=0 || dprobGL!=0) {
                    fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
                        if(pars[1]>0)
                            return 100*pars[0]/pars[1]*100/pars[2];         
                        return 0;
                    };
                    
                    boolean isProbLimit=false;
                    if(dprobGL>=0.99*probG || dprobGU>=10*probG)
                    	isProbLimit=true;
                    
                    MCEP.addParam("prob",probG,dprobGU,dprobGL,Math.max(0,probG-50*dprobGL),Math.min(100, probG+50*dprobGU),isProbLimit);   
                }

                MCEP.setFunction(fPT12);
                MCEP.doWork();
                b.dptuTOT_mc=MCEP.getResult().dvu;
                b.dptlTOT_mc=MCEP.getResult().dvl;
                if(b.dptlTOT_mc>b.ptTOT)
                    b.dptlTOT_mc=b.ptTOT;
                
                //System.out.println("  GammaBranchingCalc 1385: t="+t+" dtu="+dtu+" dtl="+dtl+" isLimit="+b.isT12Limit+" pt="+b.pt
                //		+"  mc results partial T: pt="+b.pt+" dtu="+b.dptu_mc+" dtl="+b.dptl_mc);

            }
        }
    }

    public void calculateBranchingsFromTI(){
        calculateBranchingsFromTI(true);
    }
    
    public void calculateBranchingsFromTI(boolean includeMC){
        int nb=branches.size();
        double minSumTI=0;
        double maxSumTI=0;
        
        double minSumRI=0;
        double maxSumRI=0;
        
        sumTI=0;
        sumRI=0;
        
        dsumTIU_ep=dsumTIL_ep=dsumTIU_ex=dsumTIL_ex=0;
        dsumRIU_ep=dsumRIL_ep=dsumRIU_ex=dsumRIL_ex=0;
        
        //used for relative uncertainties for normal error propagation
        double dtiuSqSum=0,dtilSqSum=0;
        double[] dtilSQs=new double[nb];
        double[] dtiuSQs=new double[nb];
        
        double drilSqSum=0,driuSqSum=0;
        
        //relative uncertainties from normal error propagation
        double rel_driu=0,rel_dril=0,rel_dbru=0,rel_dbrl=0;
        double rel_dtiu=0,rel_dtil=0,rel_dtbru=0,rel_dtbrl=0;
        double rel_drku=0,rel_drkl=0,rel_dccku=0,rel_dcckl=0;
        double rel_dtu=0,rel_dtl=0,rel_dpu=0,rel_dpl=0;
        
        double[] minTIs=new double[nb];
        double[] maxTIs=new double[nb];
        double[] TIs=new double[nb];
        
        double[] minRIs=new double[nb];
        double[] maxRIs=new double[nb];
        double[] RIs=new double[nb];
        
        double[] minCCs=new double[nb];
        double[] maxCCs=new double[nb];
        
        double[] ccs=new double[nb];
        double[] dccus=new double[nb];
        double[] dccls=new double[nb];
        
        double [] dTIdRI=new double[nb];//dTI/dRI, =0 if TI and RI are not correlated, that is, RI and TI are given with no CC
        
        boolean[] useTIs=new boolean[nb];
        
        if(t>0){
            rel_dtu=dtu/t;
            rel_dtl=dtl/t;
        }
        
        rel_dpu=dprobGU/probG;
        rel_dpl=dprobGL/probG;
        
        Vector<Integer> indexesOfLimitTI=new Vector<Integer>();
        Vector<Integer> indexesOfLimitRI=new Vector<Integer>();
        
        for(int i=0;i<nb;i++){
            GammaBranch b=branches.get(i);
            double cc=b.cc;
            double dccu=b.dccu;
            double dccl=b.dccl;
            
            if(!isUseInputCC){
                if(!isOnlyCalculateLargeCC || cc<0 || cc>largeCCLimit){
                    b.calcICC=BriccsWrap.icc(z,b.es,b.des, b.mult,b.mrs,b.dmrs);
                    cc=b.calcICC.x;
                    dccu=b.calcICC.dxu;
                    dccl=b.calcICC.dxl;
                    
                    //debug
                    //XDX x1=BriccsWrap.icc(45,"207.20","3","M1(+E2)","0.4","LT");
                    //System.out.println("In GammaBranchingCalculator line 158: e="+b.e+" b.mult="+b.mult+" b.mr="+b.mrs+" b.dmr="+b.dmrs+" cc="+cc+" x1="+x1.x+" "+x1.dxl);
                }
            }
            
            if(cc<0) cc=0;
            if(dccu<0) dccu=0;
            if(dccl<0) dccl=0;
            
            ccs[i]=cc;
            dccus[i]=dccu;
            dccls[i]=dccl;
            
            TIs[i]=0;
            minTIs[i]=0;
            maxTIs[i]=0;

            /*
            useTIs[i]=false;
            if(b.ti>0) {
                useTIs[i]=true;
                if(!Str.isNumeric(b.dtis) && Str.isNumeric(b.dris))
                    useTIs[i]=false;
            }
            */
            useTIs[i]=b.checkIsUseTI(cc);
            
            if(useTIs[i]){    
            	if(cc>0) {
                	RIs[i]=b.ti/(1+cc);
                    minRIs[i]=(b.ti-b.dtil)/(1+cc+dccu);
                    maxRIs[i]=(b.ti+b.dtiu)/(1+cc-dccl);
                    
                    dTIdRI[i]=1+cc;//TI and RI are correlated
            	}else {
                	RIs[i]=b.ri;
                    minRIs[i]=(b.ri-b.dril);
                    maxRIs[i]=(b.ri+b.driu);
                    
                    if((b.ti/b.ri-1)<0.05) {
                    	//if ti and ri are very close, meaning cc close to 1.0
                    	//consider ti=cc, correlated
                    	dTIdRI[i]=b.ti/b.ri;
                    }else if (b.dtiu>0&&b.dtil>0&&b.driu>0&&b.dril>0) {
                    	boolean isCorrelated=Math.abs(b.dtiu/b.ti-b.driu/b.ri)<0.1*(b.driu/b.ri)
                    			&&Math.abs(b.dtil/b.ti-b.dril/b.ri)<0.1*(b.dril/b.ri);
                    	isCorrelated=isCorrelated
                    			||((b.driu/b.ri)<=0.05&&(b.dril/b.ri)<=0.05&&(b.dtiu/b.ti)<=0.05&&(b.dtil/b.ti)<=0.05);
                    	if(isCorrelated)
                    		dTIdRI[i]=b.ti/b.ri;
                    	else
                    		dTIdRI[i]=0;
                    }else {
                    	dTIdRI[i]=0;//TI and RI are not correlated
                    }
            	}
            	
                TIs[i]=b.ti;
                minTIs[i]=b.ti-b.dtil;
                maxTIs[i]=b.ti+b.dtiu;
                
                sumTI+=TIs[i];
                minSumTI+=minTIs[i];
                maxSumTI+=maxTIs[i];
                
                //for normal error propagation
                dtilSQs[i]=b.dtil*b.dtil;
                dtilSqSum+=dtilSQs[i];      
                
                dtiuSQs[i]=b.dtiu*b.dtiu;
                dtiuSqSum+=dtiuSQs[i];
                
                if(b.isTILimit) {
                    indexesOfLimitTI.add(i);
                    if(dTIdRI[i]>0)
                    	indexesOfLimitRI.add(i);
                }
            }else if(b.ri>0){
            	RIs[i]=b.ri;
                minRIs[i]=(b.ri-b.dril);
                maxRIs[i]=(b.ri+b.driu);
                
            	TIs[i]=b.ri*(1.0+cc);
                minTIs[i]=(b.ri-b.dril)*(1.0+cc-dccl);
                maxTIs[i]=(b.ri+b.driu)*(1.0+cc+dccu);
                
                sumTI+=TIs[i];
                minSumTI+=minTIs[i];
                maxSumTI+=maxTIs[i];    
                
                rel_drku=b.driu/b.ri;
                rel_drkl=b.dril/b.ri;
                if(cc>0){
                    rel_dccku=dccu/cc;
                    rel_dcckl=dccl/cc;
                }else{
                    rel_dccku=0;
                    rel_dcckl=0;
                }
                
                //for normal error propagation
                dtilSQs[i]=Math.pow(b.ri*(1+cc)*rel_drkl,2)+Math.pow(b.ri*cc*rel_dcckl,2);
                dtilSqSum+=dtilSQs[i];
    
                dtiuSQs[i]=Math.pow(b.ri*(1+cc)*rel_drku,2)+Math.pow(b.ri*cc*rel_dccku,2);
                dtiuSqSum+=dtiuSQs[i];
                
                dTIdRI[i]=1+cc;//TI and RI are correlated
                
                if(b.isTILimit) {
                    indexesOfLimitTI.add(i);
                    indexesOfLimitRI.add(i);
                }
            }
            
            if(b.ri>0) {
                sumRI+=b.ri;
                minSumRI+=(b.ri-b.dril);
                maxSumRI+=(b.ri+b.driu);
                
                drilSqSum+=b.dril*b.dril;
                driuSqSum+=b.driu*b.driu;
                
                if(b.isRILimit && !indexesOfLimitRI.contains(i))
                    indexesOfLimitRI.add(i);
            }
            
            minCCs[i]=cc-dccl;
            maxCCs[i]=cc+dccu;
            
            //if(Math.abs(b.e-415)<1)
            //System.out.println("i="+i+" eg="+b.e+" ri="+b.ri+"  "+b.driu+"  "+b.dril+" cc="+cc+" dccl="+dccl+" dccu="+dccu+" minSum"+minSum+" maxSum"+maxSum);
        }
        
        /*
         * bri=ri/sum[rk*(1+cck)],k=1-n,          it increases as ri increases and as rk (k!=i) and cck (k=1-n) decreases
         * tbri=ri*(1+cci)/sum[rk*(1+cck], k=1-n, it increases as ri and cci increases and as rk (k!=i) and cck (k!=i) decreases 
         * pti=t/bri
         */
        
        float deltaForAbsoluteRI=0;//0 or 1
        if(isAbsoluteRI) {       	
        	deltaForAbsoluteRI=1.0f;
        	
        	sumTI=100;
        	minSumTI=100;
        	maxSumTI=100;
        }
        
        float delta=deltaForAbsoluteRI;
        
        if(sumTI>0 && minSumTI>=0 && maxSumTI>0){           
            
            dsumTIU_ex=maxSumTI-sumTI;
            dsumTIL_ex=sumTI-minSumTI;
            
            dsumRIU_ex=maxSumRI-sumRI;
            dsumRIL_ex=sumRI-minSumRI;


            double dtiuSqSum1=dtiuSqSum;
            double dtilSqSum1=dtilSqSum;
            double extraDtiu=0,extraDtil=0;
            for(int i=0;i<indexesOfLimitTI.size();i++) {
                int index=indexesOfLimitTI.get(i).intValue();
                dtiuSqSum1-=dtiuSQs[index];
                extraDtiu+=Math.sqrt(dtiuSQs[index]);

                dtilSqSum1-=dtilSQs[index];
                extraDtil+=Math.sqrt(dtilSQs[index]);
                
                //System.out.println("For TI: index="+index+"  ri="+branches.get(index).ri);
            }
            
            //for limit values, do not add their uncertainties in quadrature
            //instead, add them arithmetically
            if(dtiuSqSum1<0)
                dtiuSqSum1=0;
            if(dtilSqSum1<0)
                dtilSqSum1=0;
            
            dsumTIU_ep=(1.0-delta)*(Math.sqrt(dtiuSqSum1)+extraDtiu);
            dsumTIL_ep=(1.0-delta)*(Math.sqrt(dtilSqSum1)+extraDtil);
    
            //debug
            //System.out.println("GammaBranchingCalc 1793: sumTI="+sumTI+" dsumTIU_ep="+dsumTIU_ep+"   old="+Math.sqrt(dtiuSqSum));
            //System.out.println("           dsumTIL_ep="+dsumTIL_ep+"   old="+Math.sqrt(dtiuSqSum));
            
            double driuSqSum1=driuSqSum;
            double drilSqSum1=drilSqSum;
            double extraDriu=0,extraDril=0;
            for(int i=0;i<indexesOfLimitRI.size();i++) {
                int index=indexesOfLimitRI.get(i).intValue();
                GammaBranch b=branches.get(index);
                if(b.ri>0) {
                    driuSqSum1-=b.driu*b.driu;
                    extraDriu+=b.driu;

                    drilSqSum1-=b.dril*b.dril;
                    extraDril+=b.dril;
                    
                    //System.out.println("For RI: index="+index+"  ri="+branches.get(index).ri+" driuSqSum="+driuSqSum+" driuSqSum1="+driuSqSum1
                    //        +" driu*driu="+(b.driu*b.driu)+" driu="+b.driu+" extraDriu="+extraDriu+" dril="+b.dril+" extraDril="+extraDril);
                }    
            }
            
            if(driuSqSum1<0)
                driuSqSum1=0;
            if(drilSqSum1<0)
                drilSqSum1=0;
            
            dsumRIU_ep=Math.sqrt(driuSqSum1)+extraDriu;
            dsumRIL_ep=Math.sqrt(drilSqSum1)+extraDril;
   
            ///////
            for(int i=0;i<nb;i++){
                GammaBranch b=branches.get(i);
                if(b.ri<=0 && b.ti<=0)
                    continue;
                
                double minTI=minTIs[i];
                double maxTI=maxTIs[i];
                
                double minRI=minRIs[i];
                double maxRI=maxRIs[i];
                
                b.tbr=TIs[i]/sumTI*100;
                b.dtbrl_ex=(1.0-delta)*(b.tbr-minTI/(minTI+maxSumTI-maxTI)*100)+delta*(b.tbr-minTI);
                b.dtbru_ex=(1.0-delta)*(maxTI/(maxTI+minSumTI-minTI)*100-b.tbr)+delta*(maxTI-b.tbr);  
                
                if(b.dtbrl_ex<0) b.dtbrl_ex=0;
                if(b.dtbru_ex<0) b.dtbru_ex=0;
                if(b.dtbrl_ex>b.tbr) b.dtbrl_ex=b.tbr;
                
                //get uncertainty from normal error propagation
                double cc=ccs[i];
                double dccu=dccus[i];
                double dccl=dccls[i];
                
                
                /*
                //The follow doesn't work for case where there are only ri and ti without cc
                ri=TIs[i]/(1.0+cc);
                driu=maxTIs[i]/(1.0+dccu)-ri;
                dril=ri-minTIs[i]/(1.0+dccl);;
  
                
                if(driu<0) driu=0;
                if(dril<0) dril=0;
                if(dril>ri) dril=ri;
                
                
                double min1=(ri-dril)*(1.0+maxCCs[i]);
                double max1=(ri+driu)*(1.0+minCCs[i]);
                
                b.br=ri/sumTI*100;              
                b.dbrl_ex=(1.0-delta)*(b.br-(ri-dril)/(min1+maxSumTI-maxTI)*100)+delta*dril;
                b.dbru_ex=(1.0-delta)*((ri+driu)/(max1+minSumTI-minTI)*100-b.br)+delta*driu;  
                */
                
                b.br=RIs[i]/sumTI*100;        
                
                if(dTIdRI[i]>0) {
                    b.dbrl_ex=(1-delta)*(b.br-minRI/(minTI+maxSumTI-maxTI)*100)+delta*(b.br-minRI);
                    b.dbru_ex=(1-delta)*(maxRI/(maxTI+minSumTI-minTI)*100-b.br)+delta*(maxRI-b.br); 
                }else {//TI and RI not correlated
                    b.dbrl_ex=(1-delta)*(b.br-minRI/maxSumTI*100)+delta*(b.br-minRI);
                    b.dbru_ex=(1-delta)*(maxRI/minSumTI*100-b.br)+delta*(maxRI-b.br); 
                }
               
                
                if(b.dbrl_ex<0) b.dbrl_ex=0;
                if(b.dbru_ex<0) b.dbru_ex=0;
                if(b.dbrl_ex>b.br) b.dbrl_ex=b.br;
                
                if(b.br>0){
                    b.pt=t/b.br*100*100/probG;
                    b.dptl_ex=b.pt-(t-dtl)*100/(b.br+b.dbru_ex)*100/(probG+dprobGU);
                    if(b.dptl_ex>b.pt) b.dptl_ex=b.pt;
                    
                    if(b.br>b.dbrl_ex && probG>dprobGL)
                        b.dptu_ex=(t+dtu)*100/(b.br-b.dbrl_ex)*100/(probG-dprobGL)-b.pt;
                    else
                        b.dptu_ex=1000000;  
                    
                    if(b.dptl_ex<0) b.dptl_ex=0;
                    if(b.dptu_ex<0) b.dptu_ex=0;
                }
                
                if(b.tbr>0){
                    b.ptTOT=t*100/b.tbr*100/probG;
                    b.dptlTOT_ex=b.ptTOT-(t-dtl)*100/(b.tbr+b.dtbru_ex)*100/(probG+dprobGU);
                    if(b.dptlTOT_ex>b.ptTOT) b.dptlTOT_ex=b.ptTOT;
                    
                    if(b.tbr>b.dtbrl_ex && probG>dprobGL)
                        b.dptuTOT_ex=(t+dtu)*100/(b.tbr-b.dtbrl_ex)*100/(probG-dprobGL)-b.ptTOT;
                    else
                        b.dptuTOT_ex=1000000;
                    
                    if(b.dptlTOT_ex<0) b.dptlTOT_ex=0;
                    if(b.dptuTOT_ex<0) b.dptuTOT_ex=0;
                }
                
                //if(Math.abs(b.e-822.6)<1) 
                //System.out.println("**   eg="+b.e+"  br.ex="+b.br+"  "+b.dbru_ex+" "+b.dbrl_ex+" t="+t+" dtu="+dtu+" dtl="+dtl+"  b.pt"+b.pt+"  dptu="+b.dptu_ex+" dptl="+b.dptl_ex);

                    
                if(cc>0){
                    rel_dccku=dccu/cc;
                    rel_dcckl=dccl/cc;
                }else{
                    rel_dccku=0;
                    rel_dcckl=0;
                }
                
                if(useTIs[i]){
                    rel_dtiu=b.dtiu/b.ti;
                    rel_dtil=b.dtil/b.ti;
                    if(rel_dtiu<0) rel_dtiu=0;
                    if(rel_dtil<0) rel_dtil=0;
                    
                    rel_dtbru=dtilSqSum-dtilSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_dtiu*rel_dtiu;
                    rel_dtbru=(1.0-delta)*Math.sqrt(rel_dtbru)/sumTI+delta*rel_dtiu;;
                    b.dtbru_ep=rel_dtbru*b.tbr;
                    
                    rel_dtbrl=dtiuSqSum-dtiuSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_dtil*rel_dtil;
                    rel_dtbrl=(1.0-delta)*Math.sqrt(rel_dtbrl)/sumTI+delta*rel_dtil;
                    b.dtbrl_ep=rel_dtbrl*b.tbr;
                    
                    if(b.dtbrl_ep>b.tbr) b.dtbrl_ep=b.tbr;
                    
                    /*
                    rel_dbru=rel_dtbru*rel_dtbru+Math.pow(dccl/(1+cc),2);
                    rel_dbru=Math.sqrt(rel_dbru);
                    b.dbru_ep=rel_dbru*b.br;
                    
                    rel_dbrl=rel_dtbrl*rel_dtbrl+Math.pow(dccu/(1+cc),2);
                    rel_dbrl=Math.sqrt(rel_dbrl);
                    b.dbrl_ep=rel_dbrl*b.br;
                    */
                	//TI and RI could be corrected, or not (e.g., only RI and TI are given with no cc)
                	if(dTIdRI[i]<=0) { //if >0, TI and RI are correlated, TI=RI*(1+cc), otherwise not correlated, f=0
                        rel_driu=b.driu/b.ri;
                        rel_dril=b.dril/b.ri;
                        if(rel_driu<0) rel_driu=0;
                        if(rel_dril<0) rel_dril=0;
                        
                        rel_dbru=dtilSqSum+Math.pow(sumTI,2)*rel_driu*rel_driu;
                        rel_dbru=(1-delta)*Math.sqrt(rel_dbru)/sumTI+delta*rel_driu; 
                        
                        rel_dbrl=dtiuSqSum+Math.pow(sumTI,2)*rel_dril*rel_dril;
                        rel_dbrl=(1-delta)*Math.sqrt(rel_dbrl)/sumTI+delta*rel_dtil;
                	}else {
                        rel_dbru=rel_dtbru*rel_dtbru+Math.pow(dccl/(1+cc),2);
                        rel_dbru=Math.sqrt(rel_dbru);
                        
                        rel_dbrl=rel_dtbrl*rel_dtbrl+Math.pow(dccu/(1+cc),2);
                        rel_dbrl=Math.sqrt(rel_dbrl);
                	}
                 

                    b.dbru_ep=rel_dbru*b.br;
                    b.dbrl_ep=rel_dbrl*b.br;
                    
                    if(b.dbrl_ep>b.br) b.dbrl_ep=b.br;
                    
                    if(b.pt>0) {
                        b.dptu_ep=b.pt*Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl+rel_dpl*rel_dpl);
                        b.dptl_ep=b.pt*Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru+rel_dpu*rel_dpu);
                        
                        if(b.dptl_ep>b.pt)
                            b.dptl_ep=b.pt;
                    }
                    
                    if(b.ptTOT>0) {
                        b.dptuTOT_ep=b.ptTOT*Math.sqrt(rel_dtu*rel_dtu+rel_dtbrl*rel_dtbrl+rel_dpl*rel_dpl);
                        b.dptlTOT_ep=b.ptTOT*Math.sqrt(rel_dtl*rel_dtl+rel_dtbru*rel_dtbru+rel_dpu*rel_dpu);
                        
                        if(b.dptlTOT_ep>b.ptTOT)
                            b.dptlTOT_ep=b.ptTOT;
                    }
                    
                    b.setIsBRLImit(b.isTILimit);
                }else if(b.ri>0){                   
                    rel_driu=b.driu/b.ri;
                    rel_dril=b.dril/b.ri;
                    if(rel_driu<0) rel_driu=0;
                    if(rel_dril<0) rel_dril=0;
                    
                    
                    rel_dbru=dtilSqSum-dtilSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_driu*rel_driu+Math.pow(b.ri*cc*rel_dcckl,2);
                    rel_dbru=(1.0-delta)*Math.sqrt(rel_dbru)/sumTI+delta*rel_driu;
                    b.dbru_ep=rel_dbru*b.br;
                    
                    rel_dbrl=dtiuSqSum-dtiuSQs[i]+Math.pow(sumTI-TIs[i],2)*rel_dril*rel_dril+Math.pow(b.ri*cc*rel_dccku,2);
                    rel_dbrl=(1.0-delta)*Math.sqrt(rel_dbrl)/sumTI+delta*rel_dril;
                    b.dbrl_ep=rel_dbrl*b.br;
                    
                    if(b.dbrl_ep>b.br) b.dbrl_ep=b.br;
                    
                    rel_dtbru=dtilSqSum-dtilSQs[i]+Math.pow(sumTI-TIs[i],2)*(rel_driu*rel_driu+Math.pow(dccu/(1+cc),2));
                    rel_dtbru=Math.sqrt(rel_dtbru)/sumTI;
                    b.dtbru_ep=rel_dtbru*b.tbr;
                    
                    rel_dtbrl=dtiuSqSum-dtiuSQs[i]+Math.pow(sumTI-TIs[i],2)*(rel_dril*rel_dril+Math.pow(dccl/(1+cc),2));
                    rel_dtbrl=Math.sqrt(rel_dtbrl)/sumTI;
                    b.dtbrl_ep=rel_dtbrl*b.tbr;
                    
                    
                    if(b.dtbrl_ep>b.tbr) b.dtbrl_ep=b.tbr;
                    
                    if(b.pt>0) {
                        b.dptu_ep=b.pt*Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl+rel_dpl*rel_dpl);
                        b.dptl_ep=b.pt*Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru+rel_dpu*rel_dpu);
                        
                        if(b.dptl_ep>b.pt)
                            b.dptl_ep=b.pt;
                    }

                    if(b.ptTOT>0) {
                        b.dptuTOT_ep=b.ptTOT*Math.sqrt(rel_dtu*rel_dtu+rel_dtbrl*rel_dtbrl+rel_dpl*rel_dpl);
                        b.dptlTOT_ep=b.ptTOT*Math.sqrt(rel_dtl*rel_dtl+rel_dtbru*rel_dtbru+rel_dpu*rel_dpu);
                        
                        if(b.dptlTOT_ep>b.ptTOT)
                            b.dptlTOT_ep=b.ptTOT;
                    }
                    
                    b.setIsBRLImit(b.isRILimit);
                    //System.out.println("**     br.ep="+b.br+"  "+b.dbru_ep+" "+b.dbrl_ep);
                    //System.out.println("       br.ex="+b.br+"  "+b.dbru_ex+" "+b.dbrl_ex);
                    //System.out.println("       ri="+b.ri+"  "+b.driu+"  "+b.dril+" rel_dbru="+rel_dbru+" rel_driu="+rel_driu);
                }
                
                //System.out.println(b.es+"  br="+b.br+" dbr ep="+b.dbru_ep+"  "+b.dbrl_ep+"  minRI="+minRI+" minTI="+minTI+" maxRI="+maxRI+" maxTI="+maxTI);
                //System.out.println(b.es+"  tbr="+b.tbr+" dtbr ep="+b.dtbru_ep+"  "+b.dtbrl_ep+"  minRI="+minRI+" minTI="+minTI+" maxRI="+maxRI+" maxTI="+maxTI);

                
                //if(Math.abs(b.e-822.6)<1){
                //  System.out.println("***t="+t+" pt="+b.pt+"  br="+b.br+"  "+b.dbru_ep+"  "+b.dbrl_ep+" tbr="+b.tbr+"  "+b.dtbru_ep+"  "+b.dtbrl_ep+" min"+min+" max"+max+" minSum"+minSumTI+" maxSum"+maxSumTI);
                //  System.out.println("sum="+sumTI+" b.ri*(1+cc)="+b.ri*(1+cc)+" pt="+b.pt+"  ptu="+b.dptu_ep+"  "+b.dptl_ep);
                //  System.out.println("  ri="+b.ri+" "+b.driu+" "+b.dril+" ti="+TIs[i]+" "+max1+" "+min1+" rel_dccku="+rel_dccku);
                //}
            }
            
            
            if(includeMC)
                calculateBranchingsFromTI_MC(ccs,dccus,dccls);
        }   
    }
    public void calculateBranchingsFromTI_MC(double[] ccs,double[] dccus,double[] dccls) {
        calculateBranchingsFromTI_MC(ccs,dccus,dccls,NPSample);//NP=-1 use default NP in MCEP
    }

    public void calculateBranchingsFromTI_MC(double[] ccs,double[] dccus,double[] dccls,int NP) {
        int nb=branches.size();
        if(nb<1)
        	return;
        
        if(nb==1) {
        	GammaBranch b=branches.get(0);
        	
            b.dptu_mc=b.dptu_ex;
            b.dptl_mc=b.dptl_ex; 
            
            b.dptuTOT_mc=b.dptuTOT_ex;
            b.dptlTOT_mc=b.dptlTOT_ex;          
        }
        
        double cc,dccu,dccl;
        double br=0,tbr=0,ri=0,driu=0,dril=0,ti=0,dtiu=0,dtil=0;
        MCErrorPropagation MCEP=new MCErrorPropagation(NP);

        //The following works only when there are RI and CC, but there are cases
        //where there are RI and TI, or cases where there are TI and CC
        /*
        MCErrorPropagation.FunctionMD fTBR=(pars)->{//par[2*k]=TI,par[2*k+1]=CC
            double sum=0;
            for(int i=0;i<pars.length/2;i++) sum+=pars[2*i];
            return sum==0?0:100*(pars[0]/sum);
        };
        
        MCErrorPropagation.FunctionMD fBR=(pars)->{
            double sum=0;
            for(int i=0;i<pars.length/2;i++) sum+=pars[2*i];
            return sum==0?0:100*(pars[0]/(1+pars[1])/sum);
        };
        
        MCErrorPropagation.FunctionMD fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
            if(pars[1]>0)
                return pars[0]/pars[1];         
            return 0;
        };
        */
        String[] parNames=new String[2*nb];
        
        boolean[] useTIs=new boolean[nb];
        for(int i=0;i<nb;i++){
            GammaBranch b=branches.get(i);  
            useTIs[i]=b.checkIsUseTI();
            /*
            useTIs[i]=false;
            if(b.ti>0) {
                useTIs[i]=true;
                if(!Str.isNumeric(b.dtis) && Str.isNumeric(b.dris))
                    useTIs[i]=false;
            }
            */
        }

        
        for(int i=0;i<nb;i++){
            GammaBranch b=branches.get(i);
            
            //System.out.println("GammaBranchCalc 2251: eg="+b.es+" br="+b.br+" tbr="+b.tbr);
            
            MCEP.reset();
            
            cc=ccs[i];
            dccu=dccus[i];
            dccl=dccls[i];
            
            ti=0;dtiu=0;dtil=0;
            
            boolean canGetBR=false,canGetTBR=false;
            boolean added=false;
            int count=0;
            ri=b.ri;
            driu=b.driu;
            dril=b.dril;
            if(ri<0) ri=0;
            if(driu<0) driu=0;
            if(dril<0) dril=0;
            
            ti=b.ti;
            dtiu=b.dtiu;
            dtil=b.dtil;
            if(ti<0) ti=0;
            if(dtiu<0) dtiu=0;
            if(dtil<0) dtil=0;
            
            
            if(useTIs[i]){                
                canGetTBR=true;
                if(cc>=0)
                	canGetBR=true;
                
                //two scenarios:
                //1. ti with cc, ri empty
                //2. ti and ri, cc empty
                if(ri>0 && cc<=0) {
                    MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                    MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                    parNames[2*count]="ri";parNames[2*count+1]="ti";
                    added=true;	
                }else if(ri<=0 && cc>=0) {
                    MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                    MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                    parNames[2*count]="ti";parNames[2*count+1]="cc";
                    added=true;	
                }
            }else if(b.ri>0){
                //ti=b.ri*(1+cc);
                //dtiu=(b.ri+b.driu)*(1+cc+dccu)-ti;
                //dtil=ti-(b.ri-b.dril)*(1+cc-dccl);
                
                if(cc>=0) {
                	canGetBR=true;
                	canGetTBR=true;
                }
                
                MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                parNames[2*count]="ri";parNames[2*count+1]="cc";
                added=true;	
            }
            //if(dtil>ti)
            //    dtil=ti;
   
            if(!added) {
                MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                parNames[2*count]="ri";parNames[2*count+1]="cc";
            }
            count++;
            
            for(int j=0;j<nb;j++){
                if(j==i)
                    continue;
                
                added=false;
                
                GammaBranch bj=branches.get(j);
                
                cc=ccs[j];
                dccu=dccus[j];
                dccl=dccls[j];
                
                ti=0;dtiu=0;dtil=0;
                ri=bj.ri;
                driu=bj.driu;
                dril=bj.dril;
                if(ri<0) ri=0;
                if(driu<0) driu=0;
                if(dril<0) dril=0;
                
                ti=bj.ti;
                dtiu=bj.dtiu;
                dtil=bj.dtil;
                if(ti<0) ti=0;
                if(dtiu<0) dtiu=0;
                if(dtil<0) dtil=0;
                
                if(useTIs[j]){
                    if(ri>0 && cc<=0) {
                        MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                        MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                        parNames[2*count]="ri";parNames[2*count+1]="ti";
                        added=true;	
                    }else if(ri<=0 && cc>=0) {
                        MCEP.addParam("ti",ti,dtiu,dtil,Math.max(0,ti-100*dtil),ti+100*dtiu,b.isTILimit);
                        MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                        parNames[2*count]="ti";parNames[2*count+1]="cc";
                        added=true;	
                    }
                }else if(bj.ri>0){
                    MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                    MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                    parNames[2*count]="ri";parNames[2*count+1]="cc";
                    added=true;
                }
                if(!added) {
                    MCEP.addParam("ri",ri,driu,dril,Math.max(0,ri-100*dril),ri+100*driu,b.isRILimit);
                    MCEP.addParam("cc",cc,dccu,dccl,Math.max(0,cc-100*dccl),cc+100*dccu,b.isCCLimit);
                    parNames[2*count]="ri";parNames[2*count+1]="cc";
                }
                count++;
            }

            MCErrorPropagation.FunctionMD fBR=(pars)->{
                double sum=0,r0=0,tempR0=0;
                for(int k=0;k<pars.length/2;k++) {
                	String name1=parNames[2*k];
                	String name2=parNames[2*k+1];
                	tempR0=0;
                	if(name1.equals("ri")) {
                		if(name2.equals("cc")) {
                        	sum+=pars[2*k]*(1+pars[2*k+1]);
                		}else if(name2.equals("ti")) {
                        	sum+=pars[2*k+1];
                		}
                		tempR0=pars[2*k];
                	}else if(name1.equals("ti")&&name2.equals("cc")) {
                    	sum+=pars[2*k];
                    	if(pars[2*k+1]>0)
                    		tempR0=pars[2*k]/(1+pars[2*k+1]);
                    	else
                    		tempR0=pars[2*k];
                	}

                	if(k==0)
                		r0=tempR0;
                }
                return sum==0?0:100*(r0/sum);
            };
            MCErrorPropagation.FunctionMD fTBR=(pars)->{
                double sum=0,t0=0,tempT0=0;
                for(int k=0;k<pars.length/2;k++) {
                	String name1=parNames[2*k];
                	String name2=parNames[2*k+1];
                	tempT0=0;
                	if(name1.equals("ri")) {
                		if(name2.equals("cc")) {
                        	sum+=pars[2*k]*(1+pars[2*k+1]);
                        	tempT0=pars[2*k]*(1+pars[2*k+1]);
                		}else if(name2.equals("ti")) {
                        	sum+=pars[2*k+1];
                        	tempT0=pars[2*k+1];
                		}
                	}else if(name1.equals("ti")&&name2.equals("cc")) {
                    	sum+=pars[2*k];
                    	tempT0=pars[2*k];
                	}

                	if(k==0)
                		t0=tempT0;
                }
                
                return sum==0?0:100*(t0/sum);
            };
            
            MCErrorPropagation.FunctionMD fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
                if(pars[1]>0)
                    return pars[0]/pars[1];         
                return 0;
            };
            
            if(canGetBR && (nb>1||(nb==1&&cc>0)) ) {
            	
            	if(isAbsoluteRI) {//intensity is already absolute, no need to re-calculate it using MC
            		//must call calculateBranchingsFromTI() before this call        
                    b.dbrl_mc=b.dbrl_ex;
                    b.dbru_mc=b.dbru_ex;
            	}else {
                    MCEP.setFunction(fBR);
                    MCEP.doWork();
                    
                    br=MCEP.getResult().v;
                    b.dbru_mc=MCEP.getResult().dvu;
                    b.dbrl_mc=MCEP.getResult().dvl;
                    if(b.dbrl_mc>b.br)
                        b.dbrl_mc=b.br;
                    
                    if(br>0 && b.br>0 && Math.abs(b.br-br)>0.005*br) {
                        System.out.println("Something is wrong in calculating gamma branching from TI using Monte-Carlo approach.");
                        System.out.println("     branch at: EG="+b.es+" TI="+b.tis()+" MUL="+b.mult);
                    }
            	}

            }

            
            if(canGetTBR && nb>1){
            	if(isAbsoluteRI) {
            		//must call calculateBranchingsFromTI() before this call        
                    b.dtbrl_mc=b.dtbrl_ex;
                    b.dtbru_mc=b.dtbru_ex;
            	}else {
                    MCEP.setFunction(fTBR);
                    MCEP.doWork();      
                    tbr=MCEP.getResult().v;
                    b.dtbru_mc=MCEP.getResult().dvu;
                    b.dtbrl_mc=MCEP.getResult().dvl;
                    if(b.dtbrl_mc>b.tbr)
                        b.dtbrl_mc=b.tbr;
                    
                    if(tbr>0 && b.tbr>0 && Math.abs(b.tbr-tbr)>0.005*tbr) {
                        System.out.println("Something is wrong in calculating total branching from TI using Monte-Carlo approach.");
                        System.out.println("     branch at: EG="+b.es+" TI="+b.tis()+" MUL="+b.mult);
                    }
            	}
            }

            
            
            if(br>0){
                MCEP.reset();
                MCEP.addParam("t",t,dtu,dtl,Math.max(0,t-100*dtl),t+100*dtu,b.isT12Limit);
                MCEP.addParam("br",br,b.dbru_mc,b.dbrl_mc,Math.max(0,br-100*b.dbrl_mc),br+100*b.dbru_mc,b.isBRLimit);
                
                if(probG!=100 || dprobGU!=0 || dprobGL!=0) {
                    fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
                        if(pars[1]>0)
                            return 100*pars[0]/pars[1]*100/pars[2];         
                        return 0;
                    };
                    
                    boolean isProbLimit=false;
                    if(dprobGL>=0.99*probG || dprobGU>=10*probG)
                    	isProbLimit=true;
                    
                    MCEP.addParam("prob",probG,dprobGU,dprobGL,Math.max(0,probG-50*dprobGL),Math.min(100, probG+50*dprobGU),isProbLimit);   
                }
                
                MCEP.setFunction(fPT12);
                MCEP.doWork();
                b.dptu_mc=MCEP.getResult().dvu;
                b.dptl_mc=MCEP.getResult().dvl;
                if(b.dptl_mc>b.pt)
                    b.dptl_mc=b.pt;
            }
            
            if(tbr>0){
                MCEP.reset();   
                MCEP.addParam("t",t,dtu,dtl,Math.max(0,t-100*dtl),t+100*dtu,b.isT12Limit);
                MCEP.addParam("tbr",br,b.dtbru_mc,b.dtbrl_mc,Math.max(0,tbr-100*b.dtbrl_mc),br+100*b.dtbru_mc,b.isBRLimit);
                
                if(probG!=100 || dprobGU!=0 || dprobGL!=0) {
                    fPT12=(pars)->{//par[0]=t,par[1]=br (percentage)
                        if(pars[1]>0)
                            return 100*pars[0]/pars[1]*100/pars[2];         
                        return 0;
                    };
                    
                    boolean isProbLimit=false;
                    if(dprobGL>=0.99*probG || dprobGU>=10*probG)
                    	isProbLimit=true;
                    
                    MCEP.addParam("prob",probG,dprobGU,dprobGL,Math.max(0,probG-50*dprobGL),Math.min(100, probG+50*dprobGU),isProbLimit);   
                }

                MCEP.setFunction(fPT12);
                MCEP.doWork();
                b.dptuTOT_mc=MCEP.getResult().dvu;
                b.dptlTOT_mc=MCEP.getResult().dvl;
                if(b.dptlTOT_mc>b.ptTOT)
                    b.dptlTOT_mc=b.ptTOT;
                
                //System.out.println("  GammaBranchingCalc 1385: t="+t+" dtu="+dtu+" dtl="+dtl+" isLimit="+b.isT12Limit+" pt="+b.pt
                //		+"  mc results partial T: pt="+b.pt+" dtu="+b.dptu_mc+" dtl="+b.dptl_mc);

            }
        }
        
    }
    
    
    public static SDS2XDX[] calculateBranchingRatios(SDS2XDX[] inputRIs) {
    	return calculateBranchingRatios(inputRIs,null);
    }
    
    public static SDS2XDX[] calculateBranchingRatios(SDS2XDX[] inputRIs,SDS2XDX[] inputCCs) {
    	
    	SDS2XDX[] out=new SDS2XDX[inputRIs.length];
    	
    	GammaBranchingCalculator calc=new GammaBranchingCalculator();
    	calc.setTheoryDCC(0);
    	if(inputRIs.length==0)
    		return out;
    	
    	int errorLimit=99;
    	double sum=0;
    	for(int i=0;i<inputRIs.length;i++) {
    		SDS2XDX sx=inputRIs[i];
    		String ris=sx.s(),dris=sx.ds();
    		String ccs="",dccs="";
    		try {
    			SDS2XDX sx1=inputCCs[i];
    			ccs=sx1.s();
    			dccs=sx1.ds();
    		}catch(Exception e) {}
    		//             es,des,ris,dris,mults,mrs,dmrs,ccs,dccs);
    		calc.addBranch("","", ris,dris,"",   "", "",  ccs, dccs);
    		int limit=sx.getErrorLimit();
    		if(limit<errorLimit)
    			errorLimit=limit;
    		
    		if(sx.x()>0)
    			sum+=sx.x();
    	}
    	
    	if(Math.abs(sum-100)<1E-6)   		
    		return inputRIs;   
    
    	calc.calculateBranchingsFromRI(false);
    	for(int i=0;i<calc.nBranches();i++) {
    		GammaBranch gb=calc.branchAt(i);
    		double br=gb.br;
    		double dbru=gb.dbru_ep;
    		double dbrl=gb.dbrl_ep;
    		XDX2SDS xs=new XDX2SDS(br,dbru,dbrl,errorLimit);
    		SDS2XDX sx=new SDS2XDX();
    		sx.setErrorLimit(errorLimit);
    		sx.setValues(xs);
    		
    		out[i]=sx;
    	}
    	
    	return out;
    }
    
    public static boolean useTIRecord(Gamma g,boolean inputSumIntensityForTI) {
    	boolean isUseTI=false;
    	
    	try {
            
            if(Str.isNumeric(g.TIS())) {
                if(inputSumIntensityForTI) {
                	if(Str.isNumeric(g.DTIS())) {
                		isUseTI=true;
                	}else if(g.DTIS().isEmpty()) {
                		if(!Str.isNumeric(g.RIS()) || !Str.isNumeric(g.DRIS()))
                			isUseTI=true;
                	}else if(!Str.isNumeric(g.RIS()) || (!Str.isNumeric(g.DRIS())&& !g.DRIS().isEmpty()) ){
                		isUseTI=true;
                	}
                }else if(!Str.isNumeric(g.RIS())){
                	isUseTI=true;
                }else if(!Str.isNumeric(g.DRIS())){
                	if(Str.isNumeric(g.DTIS())) {
                		isUseTI=true;
                	}else if(!g.DRIS().isEmpty() && g.DTIS().isEmpty()) {
                		isUseTI=true;
                	}
                		
                }
            }
    	}catch(Exception e) {
    		
    	}
    	
    	return isUseTI;
    }
}
