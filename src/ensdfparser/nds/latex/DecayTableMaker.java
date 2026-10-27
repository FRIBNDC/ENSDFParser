package ensdfparser.nds.latex;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import ensdfparser.ensdf.*;
import ensdfparser.ensdf.Record;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.*;
import ensdfparser.nds.util.Str;

/**
 * Writes the decay tables for nuclear data sheets
 * @author scott
 * updated by Jun Chen in 2015
 */
public class DecayTableMaker extends TableMaker{
    String type;
    private boolean pastBreak=false;
    private String leftoverOfLastCom="";
    
    private Decay firstDecay=null,lastDecay=null;
    Vector<Decay> Decays=new Vector<Decay>();
    
    TablePanel T;
    
    //private EnsdfTableData etd;
    //@SuppressWarnings("unused")
	//private MassChain data;
    //private TableControl tc;
    
    public DecayTableMaker(){
        
    }
    
    public DecayTableMaker(EnsdfTableData etd,MassChain data){
        this.etd=etd;
        this.data=data;
        this.tableType="DECAY";
        this.tc=etd.getDecayTableControl();
        this.maxCommentWidth=0;
        
        ENSDF ens=etd.getENSDF();
        if(ens!=null){
            if(ens.decayRecordType().equals("B")) type="BETA";
            else if(ens.fullDSId().contains(" B+ "))  type="ECBP";
            else if(ens.decayRecordType().equals("A")) type="ALPH";
            else if(ens.decayRecordType().equals("E")){
            	if(ens.parentAt(0).QF()>1022) type="ECBP";
            	else type="ECBP";
            }else if(ens.fullDSId().contains(" SF ") || ens.fullDSId().contains("SPONTANEOUS FISSION")){
            	type="SF";			
            }else {
            	type="";     	
            }
        }

        
        init();
    }
    
    /**Implement the abstract method in parent class*/
    public TablePanel makeTablePanel(int panel,boolean option)throws Exception{
    	return makeTablePanel(panel);
    }
    
    /** write tables for beta+, beta- and alpha decays */
    public TablePanel makeTablePanel(int panel)throws Exception{
    	if(autoAdjust)
    		return makeTablePanel_auto(panel);
    	else
    		//return makeTablePanel_manual(panel);
    		return makeTablePanel_auto(panel);
    }

    //for auto mode
    //see comments in LevelTableMaker
    @SuppressWarnings("rawtypes")
	public TablePanel makeTablePanel_auto(int panel)throws Exception{
        ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        String temp,comTit;
        Vector<String> Set=new Vector<String>();
        
        float maxRowWidth=0;
        float maxCommentWidth=0;
        float maxDataWidth=0;
        float dataHeadWidth=0;//including empty column if existing
        float width=0;
        int lineCount=0;
        float linePos=0;
          
        this.maxCommentWidth=0;
        
        boolean showSuppressed=etd.isShowSuppressed();
        
        //table header
        int nColumns;
        Vector<String> columns;
       	columns=tc.getPanelColumns(panel);
 
        nColumns=columns.size();
        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getDefaultColumns().contains("COMM"))T.addComLoc(tc.getDefaultColumns().indexOf("COMM"));
        
        /*TablePanel Header*/
        String title="";
        if(ens.decayRecordType().equals("B")) {comTit="\\beta^{- ";title="\\ensuremath{\\beta^-} radiations";type="BETA";}
        else if(ens.fullDSId().contains(" B+ ")) {comTit="\\beta^{+ ";title="\\ensuremath{\\epsilon,\\beta^+} radiations";type="ECBP";}
        else if(ens.decayRecordType().equals("A")){ comTit="\\alpha^{";title="\\ensuremath{\\alpha} radiations";type="ALPH";}
        else if(ens.decayRecordType().equals("E")){
            comTit="\\epsilon^{ "; 
            type="ECBP"; 
        	if(ens.parentAt(0).QF()>1022) 
        	    title="\\ensuremath{\\epsilon,\\beta^+} radiations";      	    
        	else
        	    title="\\ensuremath{\\epsilon} radiations";
        }else if(ens.fullDSId().contains(" SF ") || ens.fullDSId().contains("SPONTANEOUS FISSION")){
        	comTit="(fission)^{";
        	title="Spontaneous fission";
        	type="SF";			
        }
        else {       	
        	comTit="(decay)^{";
        	title="";
        	type="";     	
        }
                
        //debug
        //System.out.println("In DecayTableMaker: parent mass="+ens.parentAt(0).nucleus().m());
        
        
        String EBheadflags="",Iheadflags="",I2headflags="",I3headflags="",TIheadflags="",Lheadflags="";
        Comment c;
        ComFlag cf;
        Vector<String> tempComs;
        String tempHead="", head="";
        
        Vector<String> footnotes=new Vector<String>();
        Vector<ComFlag> flags=new Vector<ComFlag>();
        ComFlag.setNumFlags(0);
        
        for(int j=0;j<tc.getComments().size();j++){
            c=(Comment)tc.getComments().elementAt(j);
            if(c.body().contains("LABEL=")) continue;
            
        	//process general comment with head, like "cL BE2W$...."
        	Comment topComment=new Comment();
        	
        	if(c.nHeads()>0 && c.type().charAt(0)!='N')//head in "N" comment is not printed, type=line.substring(7,9), like "L ", "G "
        		topComment=tc.extractTopComment(c);
        	else
        		topComment=c.clone();
        	
        	//System.out.println("In DecayTableMaker 149: c="+c.body()+" chead="+c.head()+" nhead="+c.nHeads()+" topCom isempy="+topComment.isEmpty()+" top com="+topComment.body());
        	
            //comments without head, must be general comments at top of table
            //comments with head, if containing head like "BE2W", is also at top of table
            //if head is mixture of ENSDF-field name and other names, comment is printed both at top as general comment and at bottom as footnote comments
        	
        	boolean doFootnote=false;
        	
            if(c.nHeads()<1||!topComment.isEmpty()) { 
            	Comment c1=c;
            	if(!topComment.isEmpty()) {
            		c1=topComment;
            		if(c.nHeads()>1) {
            			for(int k=0;k<c.headV().size();k++) {
            				if(tc.getDefaultColumns().contains(c.headAt(k))){//column is in table
            					doFootnote=true;
            					break;
            				}
            			}
            		}
            	}
            	
            	tempComs=wrapENSDFCommentTranslated(c1,HEADNOTE_WIDTH,"","",c.isBigC());   	
            	
            	if(tempComs.size()>0){
            		tempComs.insertElementAt("\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+tempComs.elementAt(0),0);
            		tempComs.removeElementAt(1);
            	}
            	
            	Set.addAll(tempComs);
            	lineCount+=tempComs.size();
            	linePos+=NDSConfig.HEAD_COMMENT_LINEWIDTH*tempComs.size();
            }else {
            	doFootnote=true;
            }
            
            if(doFootnote) {
            	boolean print=false;
            	for(int k=0;k<c.headV().size();k++){//filter out comments with no corresponding column data
            		if(tc.getDefaultColumns().contains(c.headAt(k))){//column is in table
            			int n=c.flagVV().elementAt(k).size();
            			if(n==0)//comment has no flag, print it
            				print=true;
            			else{//comment has flags, if flag is also in data line, print it
							Vector ff= c.flagVV().elementAt(k);
            				for(int m=0;m<n;m++){
            					String f=(String)ff.elementAt(m); 
            					if(ens.decayFlags().contains(f))
            						print=true;          					
            				}
            			}
            			
            			if(print) break;
            		}          		
            	}
            	
            	
            	if(!print) continue;
            	
                cf=new ComFlag(c.headV(),c.flagVV());

                if(cf.hasFlags())//return true only when there is no any flag in the head string             	                	
                    flags.add(cf);
                    
                String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"DECAY"+cf.getIndex();
                
                //get table heading flags
                //there could be head as mixture of no-flag head and has-flag head
                if(cf.hasNoFlagHead()){
                    if(cf.hasNoFlagHead("E")) EBheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("IB")) Iheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("IE")) I2headflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("IA")) I3headflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("TI")) TIheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("LOGFT")) Lheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("HF")) Lheadflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                }
                
            	tempHead="h ";//arbitrary, each symbol is one character
            	head="\\makebox[1ex]{\\ensuremath{^{\\hypertarget{"+linkID+"}{"+cf.getSymbol()+"}}}} ";
            	tempComs=wrapENSDFCommentTranslated(c,FOOTNOTE_WIDTH,tempHead,head,c.isBigC(),2);
                footnotes.addAll(tempComs);    
            }            
        }
        T.setGenComments(Set);
        
        //one blank line before and after general comment
        if(Set.size()>0){
        	lineCount+=2;
            linePos+=2*NDSConfig.HEAD_COMMENT_LINEWIDTH;;
        }
        
        Set=new Vector<String>();
        Vector<String> v;
        this.nTableRecordsLines=0;
        this.tableRecordsHeight=0;
        
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            if(t.type()=='E'||t.type()=='A'||t.type()=='B') {
            	v=makeTableRecords(t,tc.isFormatTables());
            	Set.addAll(v);
            	lineCount+=v.size();
            }
        }            
        T.setTableRecords(Set);
        T.setNTableRecordsLines(this.nTableRecordsLines);
        T.setTableRecordsHeight(this.tableRecordsHeight);
        
        
        //debug
        //System.out.println("IN DecayTableMaker: table records nline="+this.nTableRecordsLines+" tlines="+ens.tableAt(0).nLines()+" height="+this.tableRecordsHeight);
        
        if(panel<=0){
        	T.setRelHeadLineNo(lineCount+2);//lineCount+(line line space)+(one line headline column names)
        	
        	//linePos+=2*Config.DEFAULT_LINEWIDTH;//if counting lines separately, not using this.tableRecordHeight, slightly different
        	linePos+=tableRecordsHeight+NDSConfig.TABLE_TOP_MARGIN+NDSConfig.HEAD_COMMENT_LINEWIDTH+0.2;//if using this.tableRecordsHeight, more precise
        	
        	T.setRelHeadLinePos(linePos);
        }
        else{
        	T.setRelHeadLineNo(NDSConfig.DEFAULT_HEADLINE_NO);
        	T.setRelHeadLinePos(NDSConfig.DEFAULT_HEADLINE_POS);
        }
        
        Set=new Vector<String>();
        
       	temp=makeTableHead(tc,comTit,EBheadflags,Iheadflags,I2headflags,I3headflags,TIheadflags,Lheadflags,type,panel);
        
        Set.addElement(temp);
        T.setFirstHead(Set);Set=new Vector<String>();
        
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{\\underline{"+title+" (continued)}}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement(temp);temp="";
        T.setHead(Set);Set=new Vector<String>();

        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{Continued on next page (footnotes at end of table)}");
        T.setTail(Set);Set=new Vector<String>();
        //write general level comments
        for(int j=0;j<footnotes.size();j++){
            Set.addElement(footnotes.elementAt(j));
        }
        
        T.setLastTail(Set);
        
        Set=new Vector<String>();
        
        lineCount=0;
        this.nTableBodyLines=0;
        this.tableBodyHeight=0;
        float nextEF=0;
        this.pastBreak=false;
        
        Vector<?extends Decay> unpDecays=new Vector<Decay>();
        
        Vector<String> unplacedES=new Vector<String>();   
        int size=0;


        if(type.equals("BETA")) {
        	size=ens.nUnpBetas();
        	unpDecays=ens.unpBetas();
        	for(int i=0;i<size;i++)
        		unplacedES.addElement(ens.unpBetaAt(i).ES());
        }
        else if(type.equals("ECBP")) {
        	size=ens.nUnpECBPs();
        	unpDecays=ens.unpECBPs();
        	for(int i=0;i<size;i++)
        		unplacedES.addElement(ens.unpECBPAt(i).ES());
        }
        else if(type.equals("ALPH")) {
        	size=ens.nUnpAlphas();
        	unpDecays=ens.unpAlphas();
        	for(int i=0;i<size;i++)
        		unplacedES.addElement(ens.unpAlphaAt(i).ES());
        }
        else size=0;
        
        this.firstDecay=null;
        this.lastDecay=null;
        
        Vector<String>out;
        
        Level firstLevelWithDecay=null;
        Decay firstLevelDecay=null;//first placed decay (from the highest level among levels that have decays) 
        float firstLevelDecayEnergy=-1;

        for(int i=ens.nLevels()-1;i>=0;i--){
        	firstLevelWithDecay=ens.levelAt(i);
        	firstLevelDecay=firstLevelWithDecay.decayAt(0, type);
        	if(firstLevelDecay!=null)
        		break;
        }

        
        if(firstLevelDecay!=null) {
        	//firstLevelDecayEnergy=firstLevelDecay.EF();
			XDX2SDS e;
			if(type.equals("ALPH"))
				e=calculateDecayParticleEnergy(firstLevelWithDecay);
			else
				e=calculateDecayEnergy(firstLevelWithDecay);
			
        	firstLevelDecayEnergy=(float) e.x();
        }else if(size>0)//there is no level that have decay and there are unplaced decays
        	firstLevelDecayEnergy=100;
        
        
        
        //for unplaced decays
        for(int j=0;j<size;j++){
        	
        	this.nCommentLines=0;
        	
        	/*
        	try{
        		if(j<size-1)
        			nextEF=Float.valueOf(unplacedES.elementAt(j+1));
                else
                	nextEF=Float.valueOf(unplacedES.elementAt(j))+100;
        	}
            catch(NumberFormatException e){
            	nextEF=-1;
            }
            */
            float d=firstLevelDecayEnergy/(size+1);
        	nextEF=d*(j+2);
        	unpDecays.elementAt(j).setERS(String.valueOf(nextEF-d));
        	
        	
            out=makeSingleDecay_auto(j,null,etd,T,panel,flags,nextEF,showSuppressed); //page breaking in the middle of comment lines is handled in this call
                                                                  //this.nTableBodyLines and this.TableBody height are incremented in this call            
            if(out==null) 
            	continue;
            else{ 
            	Set.addAll(out);
            	lineCount+=out.size();
            	//this.nTableBodyLines+=out.size();
            	
                //if(this.nCommentLines>0)
                //	this.nTableBodyLines+=this.nCommentLines-1;                	
            }
        }
        
        /*Data for each level*/
        Level l;
        Vector<String> calcDecayESV,inputDecayESV;
        float nextLevelDecayEF=-1;
        
        //we go through the levels backwards to print the decay energies in ascending order
        size=0;

		Vector<?extends Decay> decays;
		
		String es="";
        for(int i=ens.nLevels()-1;i>=0;i--){
            l=ens.levelAt(i);
            calcDecayESV=new Vector<String>();
            inputDecayESV=new Vector<String>();
            
            size=0;
            decays=l.DecaysV(type);
            if(decays!=null)
            	size=decays.size();
            
        	for(int m=0;m<size;m++){
        		es=decays.elementAt(m).ES();
        		if(es.length()>0 && Str.isNumeric(es)) {
        			inputDecayESV.addElement(es);
        		}else {
        			inputDecayESV.addElement("");
        		}
        		
    			XDX2SDS e;
    			if(type.equals("ALPH"))
    				e=calculateDecayParticleEnergy(l);
    			else
    				e=calculateDecayEnergy(l);
    			
    			calcDecayESV.addElement(String.valueOf(e.x()));
        	}
        	
        	//find next decay level
        	Decay nextDecay=null;
        	Level nextDecayLevel=null;
        	for(int k=i-1;k>=0;k--){
        		if(ens.levelAt(k).nDecays(type)>0){
        			nextDecayLevel=ens.levelAt(k);
        			nextDecay=nextDecayLevel.decayAt(0, type);
        			break;
        		}
        	}
        	
        	if(nextDecay!=null){
        		//nextLevelDecayEF=nextDecay.EF();
            	
                //debug
                //System.out.println("In DecayTableMaker: level#="+i+" decay#="+0+" l.ES="+l.ES()+" ES="+nextDecay.ES()+" nextEF="+nextDecay.EF());
                
        		//if(nextDecay.ES().length()<=0 || !Str.isNumeric(nextDecay.ES())){       			
        			XDX2SDS e;
        			if(type.equals("ALPH"))
        				e=calculateDecayParticleEnergy(nextDecayLevel);
        			else
        				e=calculateDecayEnergy(nextDecayLevel);
        			
                	nextLevelDecayEF=(float) e.x();
        		//}
        		
        	}else if(!calcDecayESV.isEmpty() && Str.isNumeric(calcDecayESV.lastElement())){//already the last decay level
        	    nextLevelDecayEF=Float.parseFloat(calcDecayESV.lastElement())+100;
        	}
            
            /*
            if(type.equals("BETA")) {
            	size=l.nBetas();
            	for(int m=0;m<size;m++){
            		if(l.betaAt(m).ES().length()>0)
            		    decayES.addElement(l.betaAt(m).ES());
            		else{
            			XDX2SDS e=calculateDecayEnergy(l);
            			decayES.addElement(String.valueOf(e.x()));
            		}
            	}
            	
            	if(i>0 && ens.levelAt(i-1).nBetas()>0){
            		nextLevelDecayEF=ens.levelAt(i-1).betaAt(0).EF();
            		if(ens.levelAt(i-1).betaAt(0).ES().length()<=0){
            			XDX2SDS e=calculateDecayEnergy(ens.levelAt(i-1));
                    	nextLevelDecayEF=(float) e.x();
            		}
            	}
            }
            else if(type.equals("ALPH")) {
            	size=l.nAlphas();
            	for(int m=0;m<size;m++){
            		if(l.alphaAt(m).ES().length()>0)
            			decayES.addElement(l.alphaAt(m).ES());
            		else{
            			XDX2SDS e=calculateDecayEnergy(l);
            			decayES.addElement(String.valueOf(e.x()));
            		}
            	}
            	
            	if(i>0 && ens.levelAt(i-1).nAlphas()>0){
            		nextLevelDecayEF=ens.levelAt(i-1).alphaAt(0).EF();
            		if(ens.levelAt(i-1).alphaAt(0).ES().length()<=0){
            			XDX2SDS e=calculateDecayEnergy(ens.levelAt(i-1));
                    	nextLevelDecayEF=(float) e.x();
            		}
            	}
            }
            else if(type.equals("ECBP")) {
            	size=l.nECBPs();
            	for(int m=0;m<size;m++){
            		
                                       
            		if(l.ECBPAt(m).ES().length()>0)
            			decayES.addElement(l.ECBPAt(m).ES());
            		else{
            			XDX2SDS e=calculateDecayEnergy(l);
            			decayES.addElement(String.valueOf(e.x()));
                        //debug
                        //System.out.println("In DecayTableMaker: i="+i+" m="+m+" l.ES="+l.ES()+" decayES="+l.ECBPAt(m).ES()+" calculated="+String.valueOf(e.x()));
            		}
            		
            	}
            	
            	if(i>0 && ens.levelAt(i-1).nECBPs()>0){
            		nextLevelDecayEF=ens.levelAt(i-1).ECBPAt(0).EF();
            		if(ens.levelAt(i-1).ECBPAt(0).ES().length()<=0){
            			XDX2SDS e=calculateDecayEnergy(ens.levelAt(i-1));
                    	nextLevelDecayEF=(float) e.x();
            		}
            	}
            }
            else size=0;
            */
            
            for(int j=0;j<size;j++){
            	
            	this.nCommentLines=0;//incremented in findMaxCommentWidth() in makeSingleDecay()
            	
            	if(j<size-1){
            		nextEF=Float.valueOf(calcDecayESV.elementAt(j+1));
            	}
            	else{
            		if(i>0)
            			nextEF=nextLevelDecayEF;
            		else
            			nextEF=Float.valueOf(calcDecayESV.elementAt(j))+100;
            	}

            	//System.out.println("IN DecayTableMaker line 520: i="+i+" j="+j+" level="+l.ES()+"  panel="+panel+" nextLevelDecayEF="+nextLevelDecayEF+" decayES="+calcDecayESV.get(j));
            	
                out=makeSingleDecay_auto(j,l,etd,T,panel,flags,nextEF,showSuppressed);//page breaking in the middle of comment lines is handled in this call
                                                                  //this.nTableBodyLines and this.TableBody height are incremented in this call

                //debug
                //System.out.println("In DecayTableMaker: level#="+i+" decay#="+j+" l.ES="+l.ES()+" decayES[j]="+calcDecayESV.elementAt(j)+" panel="+panel+" nextEF="+nextEF+" out="+out);
                
                if(out==null) 
                	continue;
                else{
                	Set.addAll(out);
                	lineCount+=out.size();
                	//this.nTableBodyLines+=out.size();
                	
                    //if(this.nCommentLines>0)
                    //	this.nTableBodyLines+=this.nCommentLines-1;
                }
                       
                if(panel<0)
                	width=getEntryLength(j,l,tc.getPanelColumns(0),ens);
                else
                	width=getEntryLength(j,l,tc.getPanelColumns(panel),ens);
                
                if(width>maxDataWidth) 
                	maxDataWidth=width;
                
     
            }//beta loop       
                        
        }//level loop
 
        T.setBody(Set);
 
        if(panel<0) panel=0;
        
        if(Set.size()>0){
            tc.setFirstLine(panel,Set.firstElement());
            tc.setFirstLineEnergy(panel, Str.parseFloatEnergy(T.getDataLineEnergies().firstElement())); 
            tc.setFirstLineLevelEnergy(panel, Str.parseFloatEnergy(T.getDataLineLevels().firstElement()));         
            
            tc.setLastLine(panel,Set.lastElement());
            tc.setLastLineEnergy(panel, Str.parseFloatEnergy(T.getDataLineEnergies().lastElement()));  
            tc.setLastLineLevelEnergy(panel, Str.parseFloatEnergy(T.getDataLineLevels().lastElement()));   
            
            if(Set.size()>1)
            	tc.setSecondLastLine(panel, Set.elementAt(Set.size()-2));
            
            tc.setLeftoverOfLastCom(panel,leftoverOfLastCom);
        }

        try{
        	//check if the first line is the same as the last line just set here
        	float nextLineE=tc.getFirstLineEnergy(panel+1);
        	float nextLevelE=tc.getFirstLineLevelEnergy(panel+1);        	
        	if(nextLevelE==tc.getLastLineLevelEnergy(panel)){
        		tc.SetIsLastLevelSameAsNextFirst(panel, true);
        		if(nextLineE==tc.getLastLineEnergy(panel)) {
            		tc.setFirstLine(panel+1, null);
            		tc.setFirstLineEnergy(panel+1, nextLineE+Math.min(0.1f,0.001f*nextLineE));
            		tc.setFirstLineLevelEnergy(panel+1, nextLevelE+Math.min(0.1f,0.001f*nextLevelE));
        		}
        	}
        	
        }catch(Exception e){}
        
        
        T.setNTableBodyLines(this.nTableBodyLines);
        T.setTableBodyHeight(this.tableBodyHeight);
        T.setTotalDataLines(lineCount);
        
      
        //maxDataWidth=maxDataWidth*Config.CHAR2CM;//this is inaccurate since there could be different empty columns in different lines
        
        //debug
        //try {
        //    System.out.println(" panel="+panel+" firstDecay="+firstDecay.ES());
        //    System.out.println("                lastDecay="+lastDecay.ES());
        //}catch(Exception e) {}
        
        HashMap<String,String> maxColStrings=findMaxColumnStrings(tc.getPanelColumns(panel),Decays,firstDecay,lastDecay,flags);
        HashMap<String,String> headStrings=colHeadStrings(tc,comTit,EBheadflags,Iheadflags,I2headflags,I3headflags,TIheadflags,Lheadflags,type,panel);
        
        maxDataWidth=findMaxDataWidth(maxColStrings,headStrings);

        maxCommentWidth=this.maxCommentWidth;//this.maxCommentWidth is determined in makeSingleDecay()

        dataHeadWidth=findDataHeadWidth(maxColStrings,headStrings);
        
        /*
        //debug
        if(panel==0) {
            System.out.println("In DecayTableMaker line 598: panel="+panel+" maxDatawidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" dataheadwidth="+dataHeadWidth+
                "  tc.commentColumnWidth="+tc.getCommentColumnWidth(panel));
  
            for(String key:maxColStrings.keySet())
                System.out.println("    col="+key+"  name="+maxColStrings.get(key));
            
            for(String key:headStrings.keySet())
                System.out.println("    head="+key+"  name="+headStrings.get(key));
            
            //for(String s:tc.getFixedColumns())
            //    System.out.println("   fixedColumn="+s);
            
        }
        */
        
        //width=colHeadWidth(tc,comTit,EBflags,Iflags,I2flags,I3flags,TIflags,Lflags,type,panel);

        //debug
        //System.out.println("In DecayTableMaker: maxDataWidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" head width="+width);  

        
        /*
        System.out.println("                   nDecays="+Decays.size());
        if(firstDecay!=null)
            System.out.println("                   firstDecay="+firstDecay.ES());
        if(lastDecay!=null)
            System.out.println("                   lastDecay="+lastDecay.ES());        
        for (Map.Entry<String, String> entry : maxColStrings.entrySet()) 
            System.out.println("    colName="+entry.getKey()+" maxString="+entry.getValue());
        
        for (Map.Entry<String, String> entry : headStrings.entrySet()) 
            System.out.println("    head colName="+entry.getKey()+" maxString="+entry.getValue());
*/        

        
        //if(maxCommentWidth>0)
        //	width=width-"Comments".length()*Config.CHAR2CM;

        //debug
        //System.out.println("In DecayTableMaker: maxDataWidth="+maxDataWidth+" maxCommentWidth="+maxCommentWidth+" head width (exclude comment)="+width);
        

        
        if(dataHeadWidth>maxDataWidth)
            maxRowWidth=dataHeadWidth+maxCommentWidth;
        else
            maxRowWidth=maxDataWidth+maxCommentWidth;
        
        T.setTotalWidth(maxRowWidth);
        T.setDataWidth(maxDataWidth);
        T.setHeadWidth(maxRowWidth);

        T.setDataHeadWidth(dataHeadWidth);
        
        //determine the orientation of just created table based on all columns widths
        //T.setPortrait(tc.isPortrait(T));
        
        return T;
    }
    
    //for manual mode
    //see comments in LevelTableMaker
    @SuppressWarnings({ "rawtypes" })
	public TablePanel makeTablePanel_manual(int panel)throws Exception{
        ENSDF ens=etd.getENSDF();
        T=new TablePanel();
        String temp,comTit;
        Vector<String> Set=new Vector<String>();
          
        boolean showSuppressed=etd.isShowSuppressed();
        
        //table header
        int nColumns;
        Vector<String> columns;
        if(panel<0){
        	System.out.println("In manual mode for DecayTableMaker, panelNo<0 is not allowed! Please check the code!");
        	System.exit(0);
        }
               
        columns=tc.getPanelColumns(panel);

        nColumns=columns.size();
        T.setNColumns(nColumns);
        T.setColumns(columns);
        
        if(tc.getDefaultColumns().contains("COMM"))T.addComLoc(tc.getDefaultColumns().indexOf("COMM"));
        
        /*TablePanel Header*/
        String title="";
        if(ens.fullDSId().contains("B-")) {comTit="\\beta^{- ";title="\\ensuremath{\\beta^-} radiations";type="BETA";}
        else if(ens.fullDSId().contains("B+")) {comTit="\\beta^{+ ";title="\\ensuremath{\\varepsilon,\\beta^+} radiations";type="ECBP";}
        else if(ens.fullDSId().contains(" A ")){ comTit="\\alpha^{";title="\\ensuremath{\\alpha} radiations";type="ALPH";}
        else if(ens.fullDSId().contains(" EC ")&&ens.parentAt(0).QF()>1022) {comTit="\\varepsilon^{ "; title="\\ensuremath{\\varepsilon,\\beta^+} radiations";type="ECBP";}
        else if(ens.fullDSId().contains(" EC ")&&ens.parentAt(0).QF()<=1022) {comTit="\\varepsilon^{ "; title="\\ensuremath{\\varepsilon} radiations";type="ECBP";}
        else if(ens.fullDSId().contains(" SF ") || ens.fullDSId().contains("SPONTANEOUS FISSION")){
        	comTit="(fission)^{";
        	title="Spontaneous fission";
        	type="SF";			
        }
        else {
        	
        	comTit="^{";
        	title="";
        	type="";     	
        }
           
        //footnotes
        String EBflags="",Iflags="",I2flags="",I3flags="",TIflags="",Lflags="";
        Comment c;
        ComFlag cf;
        
        Vector<String> footnotes=new Vector<String>();
        Vector<ComFlag> flags=new Vector<ComFlag>();
        ComFlag.setNumFlags(0);
        
        for(int j=0;j<tc.getComments().size();j++){
            c=(Comment)tc.getComments().elementAt(j);
            if(c.body().contains("LABEL=")) continue;
            
        	//process general comment with head, like "cL BE2W$...."
        	Comment topComment=new Comment();
        	topComment=tc.extractTopComment(c);     
        	
            //comments without head, must be general comments at top of table
            //comments with head, if containing head like "BE2W", is also at top of table
            //if head is mixture of ENSDF-field name and other names, comment is printed both at top as general comment and at bottom as footnote comments
            if(c.nHeads()<1||!topComment.isEmpty()) {
            	Comment c1=c;
            	if(!topComment.isEmpty())
            		c1=topComment;

            	Set.addElement("\\addtolength{\\parindent}{"+NDSConfig.HEADNOTE_INDENT+"cm}"+c1.getRegTranslated()); 
            	
            }

            //comment with only head like "BE2W"--non standard ENSDF-field name--is filter out from footnote comments
            //if head is mixture of ENSDF-field name and other names, comment is printed both at top as general comment and at bottom as footnote comments
            if(c.nHeads()>=1) {//comments with head, could be footnotes or comments like "cL BE2W$...."
            	boolean print=false;
            	for(int k=0;k<c.headV().size();k++){//filter out comments with no corresponding column data
            		if(tc.getDefaultColumns().contains(c.headAt(k))){//column is in table
            			int n=c.flagVV().elementAt(k).size();
            			if(n==0)//comment has no flag, print it
            				print=true;
            			else{//comment has flags, if flag is also in data line, print it
							Vector ff= c.flagVV().elementAt(k);
            				for(int m=0;m<n;m++){
            					String f=(String)ff.elementAt(m); 
            					if(ens.decayFlags().contains(f))
            						print=true;          					
            				}
            			}
            			
            			if(print) break;
            		}          		
            	}
            	
            	
            	if(!print) continue;
            	                 
                cf=new ComFlag(c.headV(),c.flagVV());
                if(cf.hasFlags())//return ture only when there is no any flag in the head string             	                	
                    flags.add(cf);
                    
                String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"DECAY"+cf.getIndex();

                //get table heading flags
                //there could be head as mixture of no-flag head and has-flag head
                if(cf.hasNoFlagHead()){
                    if(cf.hasNoFlagHead("E")) EBflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("IB")) Iflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("IE")) I2flags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("IA")) I3flags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("TI")) TIflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("LOGFT")) Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasNoFlagHead("HF")) Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                }
                
                footnotes.add("\\makebox[1ex]{\\ensuremath{^{\\hypertarget{"+linkID+"}{"+cf.getSymbol()+"}}}} "+c.getRegTranslated());                 
            }            
        }
        T.setGenComments(Set);
               
        
        Set=new Vector<String>();
        Vector<String> v;
        for(int i=0;i<ens.nTableRecords();i++){
            TableRecord t=ens.tableAt(i);
            if(t.type()=='E'||t.type()=='A'||t.type()=='B') {
            	v=makeTableRecords(t,tc.isFormatTables());
            	Set.addAll(v);
            }
        }            
        
        T.setTableRecords(Set);
        
        Set=new Vector<String>();
        
        
        if(panel>=0)
        	temp=makeTableHead(tc,comTit,EBflags,Iflags,I2flags,I3flags,TIflags,Lflags,type,panel);
        else
        	temp=makeTableHead(tc,comTit,EBflags,Iflags,I2flags,I3flags,TIflags,Lflags,type,0);      
        
        Set.addElement(temp);
        T.setFirstHead(Set);Set=new Vector<String>();
        
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{\\underline{"+title+" (continued)}}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement(temp);temp="";
        T.setHead(Set);Set=new Vector<String>();

        Set.addElement("\\multicolumn{"+nColumns+"}{c}{~}");
        Set.addElement("\\multicolumn{"+nColumns+"}{c}{Continued on next page (footnotes at end of table)}");
        T.setTail(Set);Set=new Vector<String>();
        //write general level comments
        for(int j=0;j<footnotes.size();j++){
            Set.addElement(footnotes.elementAt(j));
        }
        
        T.setLastTail(Set);
        
        Set=new Vector<String>();
        
        Decay firstLevelDecay=ens.levelAt(ens.nLevels()-1).decayAt(0, type);
        float firstLevelDecayEnergy=-1;
        if(firstLevelDecay!=null) {
        	//firstLevelDecayEnergy=firstLevelDecay.EF();
			XDX2SDS e;
			if(type.equals("ALPH"))
				e=calculateDecayParticleEnergy(ens.levelAt(ens.nLevels()-1));
			else
				e=calculateDecayEnergy(ens.levelAt(ens.nLevels()-1));
			
        	firstLevelDecayEnergy=(float) e.x();
        }
        
        Vector<String> unplacedES=new Vector<String>();   
        int size=0;
        Vector<?extends Decay> unpDecays=new Vector<Decay>();

        if(type.equals("BETA")) {
        	size=ens.nUnpBetas();
        	unpDecays=ens.unpBetas();
        	for(int i=0;i<size;i++)
        		unplacedES.addElement(ens.unpBetaAt(i).ES());
        }
        else if(type.equals("ECBP")) {
        	size=ens.nUnpECBPs();
        	unpDecays=ens.unpECBPs();
        	for(int i=0;i<size;i++)
        		unplacedES.addElement(ens.unpECBPAt(i).ES());
        }
        else if(type.equals("ALPH")) {
        	size=ens.nUnpAlphas();
        	unpDecays=ens.unpAlphas();
        	for(int i=0;i<size;i++)
        		unplacedES.addElement(ens.unpAlphaAt(i).ES());
        }
        else size=0;
        
        Vector<String>out;
        
        for(int j=0;j<size;j++){
        	
        	this.nCommentLines=0;
            
            float d=firstLevelDecayEnergy/(size+1);
        	float nextEF=d*(j+2);
        	unpDecays.elementAt(j).setERS(String.valueOf(nextEF-d));
        	
            out=makeSingleDecay_manual(j,null,etd,T,panel,flags,showSuppressed);
            
            if(out==null) 
            	continue;
            else{ 
            	Set.addAll(out);           	
            }
        }
        
        /*Data for each level*/
        Level l;
        
        //we go through the levels backwards to print the decay energies in ascending order
        size=0;
        for(int i=ens.nLevels()-1;i>=0;i--){
            l=ens.levelAt(i);
            
            if(type.equals("BETA")) {
            	size=l.nBetas();
            }
            else if(type.equals("ALPH")) {
            	size=l.nAlphas();
            }
            else if(type.equals("ECBP")) {
            	size=l.nECBPs();
            }
            else size=0;
            
            for(int j=0;j<size;j++){
            	
                out=makeSingleDecay_manual(j,l,etd,T,panel,flags,showSuppressed);
                
                if(out==null) 
                	continue;
                else{
                	Set.addAll(out);
                }                    
            }//beta loop                               
        }//level loop
 
        T.setBody(Set);

        //determine the orientation of just created table based on all columns widths
        //T.setPortrait(tc.isPortrait(T));
        
        return T;
    }
    
    //NOTE: order decay by calculated decay energy, but print out original decay energy EB in the dataset if present
    //USNDP recommendation: leave EB field empty but place measured EB in comments to make calculated EB (usually more precise) 
    //being printed in the output
    @SuppressWarnings("unused")
	private Vector<String> makeSingleDecay_auto(int j,Level l,EnsdfTableData etd,TablePanel T,int panel,Vector<ComFlag> flags,float nextEF,boolean showSuppressed)throws Exception{
            String ES,DES,ERS,IS,DIS,IS2="",DIS2="",TIS="",DTIS="",LOGFT,DLOGFT,flag;
            String ES0,DES0;//ES could be reset in the code, here ES0 is the original value in the input dataset
            String levelEnergy,dataEnergy;
            String temp="";
            Vector <String> out=new Vector<String>();
            
            String outputES="";
            
            float E;
            Vector<Comment> commentsV;
            Vector<ContRecord> contRecsV;
            float min=-1000000;
            float max=1000000000;
            float width=0;
            
            boolean pastMin=false,pastMax=false;
            boolean isLastDecayInPrevPanel=false;
            
            ENSDF ens=etd.getENSDF();          
            TableControl tc=etd.getDecayTableControl();
            
            if(l!=null) {
            	//levelEnergy=l.ES();
                levelEnergy=String.valueOf(l.EF());
            }
            else
            	levelEnergy=Float.toString(-1);
 
            if(panel<0){//for printing the whole table in a single panel with no beak and splitting        	
            }
            else{ 
            	if(panel>0&&panel<=tc.getBreakPoints().size())
            		min=Float.valueOf(tc.getBreakPoints().elementAt(panel-1).toString());
            	if(panel<tc.getBreakPoints().size())
            		max=Float.valueOf(tc.getBreakPoints().elementAt(panel).toString());
            	
            	//System.out.println(" DecayTableMaker line 917: panel="+panel+" min="+min+" max="+max+"  size of break points="+tc.getBreakPoints().size());
            	//if(panel<tc.getBreakPoints().size()) System.out.println("   break point="+tc.getBreakPoints().elementAt(panel).toString());
            }
            
            
            //get data, depending on what kind of decay we are working with
            Decay d;
            if(type.equals("BETA")){
                if(l!=null) d=l.betaAt(j); 
                else d=ens.unpBetaAt(j);
                
                Beta b=(Beta)d;
                
                E=b.EF();
                ES=b.ES();
                DES=b.DES();
                ES0=b.ES0();
                DES0=b.DES0();
                ERS=b.ERS();
                IS=b.RIS();
                DIS=b.DRIS();
                LOGFT=b.LOGFTS();
                DLOGFT=b.DLOGFTS();
                commentsV=b.commentV();
                contRecsV=b.contRecsV();
                flag=b.flag();
            }else if(type.equals("ECBP")){
                ECBP b;
                if(l!=null) d=l.ECBPAt(j);
                else d=ens.unpECBPAt(j);
                
                b=(ECBP)d;
                
                ES0=b.ES0();
                DES0=b.DES0();
                
                ES=b.ES();
                E=b.EF();
                DES=b.DES();
                ERS=b.ERS();
                IS=b.IBS();
                DIS=b.DIBS();
                IS2=b.IES();
                DIS2=b.DIES();
                TIS=b.ITS();
                DTIS=b.DITS();
                LOGFT=b.LOGFTS();
                DLOGFT=b.DLOGFTS();
                commentsV=b.commentV();
                contRecsV=b.contRecsV();
                flag=b.flag();
            	
                //System.out.println("DecayTableMaker 1060: decay ES="+ES+" lev="+l.ES()+" decay  ERS="+d.ERS());
            }else{ 
                Alpha b;
                if(l!=null) d=l.alphaAt(j);
                else d=ens.unpAlphaAt(j);
                
                b=(Alpha)d;
                
                ES0=b.ES0();
                DES0=b.DES0();
                
                ES=b.ES();
                E=b.EF();
                DES=b.DES();
                ERS=b.ERS();
                IS=b.RIS();
                DIS=b.DRIS();
                LOGFT=b.HFS();
                DLOGFT=b.DHFS();
                commentsV=b.commentV();
                contRecsV=b.contRecsV();       
                flag=b.flag();
            }
            
            if(d!=null && !this.Decays.contains(d))
            	this.Decays.addElement(d);
            
            //debug
            //System.out.println("In DecayTableMaker: ES="+ES+" d.ES="+d.ES()+" l.ES()="+l.ES()+" l.ef()="+l.EF()+" "+l.ERF());
            
            //note that calculated decay energy is used for ordering no matter whether or not there is an original non-empty ES
            dataEnergy=Float.toString(-1);//for setting break points, used for ordering
            
            if(ES0.length()>0){//original ES0. Note that d.ES() could be reset in the code and might not be the original 
            	if(l!=null)
            		dataEnergy=ES;//from reset, NOT ES0
            	else
            		dataEnergy=ERS;//from reset for unplaced decay, printed first in the decay table,so the dataEnergy has been modified to be below the first level decay energy                
            }
            
            //System.out.println("DecayTableMaker 1100: decay ES="+ES+" lev="+l.ES()+" decay  ERS="+d.ERS());
            
            //calculate the energy of the decay. Use this to determine whether to draw this decay in this panel of the table
            //if((ES.length()<=0 || ES.contains("(")) &&l!=null){   
            boolean doneOutputES=false;
            if(l!=null){    
            	
            	//System.out.println("DecayTableMaker 1051: decay ES="+ES+" lev="+l.ES()+" decay ERS="+d.ERS());
            	
            	XDX2SDS e;
            	
            	if(type.equals("ALPH"))
            		e=calculateDecayParticleEnergy(l);//for alpha decay, calculate alpha-particle energy
            	else
            		e=calculateDecayEnergy(l);//for other decays, calculate total decay energy
            	
            	
            	//System.out.println("DecayTableMaker 1051: decay ES="+ES+" lev="+l.ES()+" decay ES="+e.s()+" ds="+e.ds()+" x="+e.x()+" dx="+e.dx()+" ERS="+d.ERS());
            	if((ES0.length()<=0 || ES0.contains("("))) {
            		outputES=e.s();//calculated decay energy string placed in bracket, in latex format of (ES {\it DES}), to be printed in radiation table
            	    doneOutputES=true;
            	}

            	XDX2SDS e1=new XDX2SDS();
                e1.setValues(e.x(), e.dx());
                d.setEF(e.xf());
                d.setDEF(e.dxf());
                
                //System.out.println("e1 s="+e1.s()+" ds="+e1.ds()+" e s="+e.s()+" ds="+e.ds()+" "+doneOutputES+" e.s="+e.s()+" e1.dx="+e1.dx()+"  "+(e1.dx()==0));
                
            	//d.setES("("+e1.s()+" "+e1.ds()+")");//this could cause decay-energy column runs over the next IB column in the decay scheme, if e1.s is long     
                                                      //solution1: do not show e1.ds()
                if(e1.s().length()>0){
                	if(e1.s().indexOf("+")>0 || e1.dx()==0 || e1.s().toUpperCase().indexOf("E")>0)
                    	d.setES("("+Double.valueOf(e1.s()).intValue()+")");
                    else if(e1.x()>0)
                    	d.setES("("+e1.s()+")");
                    else
                    	d.setES(outputES);
                	
                	if(e1.ds().trim().length()>0) {
                		String ds=e1.ds();
                		if(e.ds().equals("SY"))
                			ds="syst";//"syst" cause a little over-estimate of the col width
                		else if(e.ds().equals("CA"))
                			ds="cal";
                		else if(ds.equals("0") && e.ds().length()>0)
                			ds=e.ds().toLowerCase();
                		
                		d.setERS("("+e1.s()+" "+ds+")");//used for determining column width
                	}else
                		d.setERS("("+e1.s()+")");
                	
                	//System.out.println("DecayTableMaker 1143: lev="+d.getFLS()+" ES="+d.ERS()+" $"+e1.s()+"$"+e1.ds()+" x="+e.x()+" dx="+e.dx());
                }
                
            	E=(float) e.x();
            	dataEnergy=String.valueOf(E);//use calculated decay E for dataEnergy for all placed decays
            	
            	
                //System.out.println("DecayTableMaker 1150: decay ES="+ES+" lev="+l.ES()+" decay  ERS="+d.ERS());
                
                //debug
                //System.out.println("In DecayTableMaker: ES="+ES+" e="+e.x()+" d.ES="+d.ES()+" l.ES()="+l.ES()+" l.ef()="+l.EF()+" "+l.ERF());
                //System.out.println("                    e1.s=="+e1.s()+" e1.ds="+e1.ds()+" e.xf=="+e1.xf()+" e.dxf="+e.dxf()+" dataEnergy="+dataEnergy);
                
            }else if(Str.isNumeric(ERS)){//unplaced decay, ERS has been reset for printing before calling this function
            	dataEnergy=ERS;
            	E=Float.parseFloat(ERS);
            }
            
            /*
            if((ES0.length()<=0 || ES0.contains("(")) &&l!=null){ 
               //do nothing here, outputES already set above	
            }else{

            	if(DES0.trim().length()>0)
            		d.setERS(ES0+" "+DES0);//used for determining column width
            	else
            		d.setERS(ES0);
            	
                //if(tc.showDE())             	
            	outputES=LatexWriter.printNumber(ES0,DES0);//to be printed in radiation table
            }
            */
            if(!doneOutputES) {
            	if(DES0.trim().length()>0)
            		d.setERS(ES0+" "+DES0);//used for determining column width
            	else
            		d.setERS(ES0);
            	
                //if(tc.showDE())             	
            	outputES=LatexWriter.printNumber(ES0,DES0);//to be printed in radiation table
            }
            
            //if(l==null && Str.isNumeric(ERS))//unplaced decay
            //	E=Float.parseFloat(ERS);
            	
            	
            	
            //debug
            //System.out.println("#####In DecayTableMaker1 Line 1058: "+j+" ES="+ES+" ERS="+ERS+" E="+E+" min="+min+" max="+max+" "+pastMin+" "+pastMax+" "+pastBreak);

            	
            if(E>=min) {
            	pastMin=true;
            	if(this.firstDecay==null){
            		this.firstDecay=(Decay)d;

            	}
            }
            
            //debug
            //System.out.println(" In DecayTableMaker2 Line 1070: E="+E+" min="+min+" max="+max+" "+pastMin+" "+pastMax+" "+pastBreak);
            
            if(E>=max && panel>=0) {
            	if(!pastBreak) tc.setFirstLineEnergy(panel+1, E);
            	
            	pastBreak=true;
            	pastMax=true;
            	
            	if(this.lastDecay==null){
            		int index=this.Decays.indexOf(d);
            		if(index>0)
            			lastDecay=this.Decays.elementAt(index-1);
            		else
            			lastDecay=d;
            	}
            }
            else if(max>100000 && ens.levelsV().indexOf(l)==0)
            	lastDecay=d;//there could be more than one decays from the first level, the last one will be finally assigned as lastDecay
            else if(E<max && (nextEF>=max||Decays.indexOf(d)==Decays.size()-1))
                lastDecay=d;
            
            //debug
            //System.out.println(" In DecayTableMaker3 Line 1090: E="+E+" nextEF="+nextEF+" min="+min+" max="+max+" "+pastMin+" "+pastMax+" "+pastBreak);
            
            isLastDecayInPrevPanel=false;
            if(E<min && nextEF>min){//break in the middle of comments of last decay in previous panel, so print the remaining comments in the new panel, only when autoAdjust=true
            	pastMin=true;
            	isLastDecayInPrevPanel=true;
            }
            
            
            if(pastMax||!pastMin) return null;
                        
            Vector<String> coms;
            
            if(isLastDecayInPrevPanel)
            	coms=procComsFormattedPartial(panel,d,tc,showSuppressed,tc.getLastLine(panel-1),tc.getSecondLastLine(panel-1));
            else
            	coms=procComs(panel,d,tc,showSuppressed);
            
            //debug
            //for(String s:coms)
            //	System.out.println(" In DecayTableMaker line 1058: e="+d.ES()+" lastDecayInPrevPanel="+isLastDecayInPrevPanel+"  comments="+s);
            
            			
            //get decay flags
            String EBflags="",Iflags="", Lflags="",I2flags="",I3flags="",TIflags="";
            ComFlag cf;
            for(int k=0;k<flags.size();k++){
                cf=flags.elementAt(k);
                
                String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"DECAY"+cf.getIndex();
                
                if(cf.hasFlagAny(flag)){
                    if(cf.hasFlagAny("E",flag)) EBflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("IB",flag)) Iflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("IE",flag)) I2flags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("IA",flag)) I3flags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("TI",flag)) TIflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("LOGFT",flag)) Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    if(cf.hasFlagAny("HF",flag))	Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                    
                }
                
                
                //debug                  
                //System.out.println();
                //for(String s: cf.flagV())
                //  System.out.println("In decayTableMaker: flag="+flag+" cf.hasflag="+cf.hasFlagAny(flag)+" EBflags="+EBflags+" head="+s+" ES="+ES);

                
            }

            Vector<String> columns=tc.getPanelColumns(panel);
            temp="";
            
            
            //debug
            //System.out.println("In decayTableMaker line 1080: ES="+ES+" outputES="+outputES+" level="+l.ES());
            
            temp=makeDecayEntry(j,d,l,columns,coms,outputES,EBflags,Iflags,I2flags,I3flags,TIflags,Lflags);
            
            //System.out.println("   temp="+temp);
            
            if(!isLastDecayInPrevPanel){
            	out.addElement(temp);       
                this.nTableBodyLines++;
                
                float scale0=1.0f,scale1=1.0f,scale3=1.0f;
                int n=temp.indexOf("10^{");
                if(n>=0){
                	temp=temp.replace("10^{","{");
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                }
                
                if(Str.containSpecialSupScript(temp)){
                	scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*1.10f;
                	scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;      
                	scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*1.10f;                                
                }else if(Str.containSupScript(temp, "0,1,2,3,4,5,6,7,8,9")){
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0+NDSConfig.LINEWIDTH_SCALE_OFFSET0*0.2f;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;      
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;    
                }else if(Str.containSupScript(temp)){
                    if(Str.containJPIScriptOnly(temp)){
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                    }else if(Str.containGivenSupScriptOnly(temp,"x")){
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                    }else{
                        scale0=NDSConfig.TABLE_LINEWIDTH_SCALE0;
                        scale1=NDSConfig.TABLE_LINEWIDTH_SCALE1;
                        scale3=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                    }
                }else if(Str.containSubScript(temp)) {
                    scale0=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                    scale1=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                    scale3=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                }
                
                //debug
                //System.out.println("In DecayTableMaker line1099: row="+temp+" scale0="+scale0+" "+scale1+" "+scale3+" containScript="+Str.containScript(temp));
                
                //debug
                //float EL=l.EF();
                //if(EL>1085 && EL<2757) 
                //System.out.println("########### In DecayTableMaker 1234: scale0="+scale0+" scale1="+scale1+" scale3="+scale3+" coms.size()="+coms.size()
                //		+" row="+temp+"\n "
                //		+ "Str.containSupScript="+Str.containSupScript(temp)+" contain special="+Str.containSpecialSupScript(temp)
                //		+" contain JPI only="+Str.containJPIScriptOnly(temp)+" Str.containSubScript(r)="+Str.containSubScript(temp));
                
                if(coms.size()==0)
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH0*scale0;
                else if(coms.size()==1)
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH1*scale1;
                else 
                	this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale3;
                
                T.getDataLineEnergies().add(dataEnergy);
                T.getDataLineLevels().add(levelEnergy);
                
                //debug
                //System.out.println("In DecayTableMaker: A dataEnergy="+dataEnergy+" index="+T.getDataLineEnergies().size());
                
                
            }  
            
            
            temp="";
            String row,tempS;
            float e1=0,e2,de,ef;
            int start=1;
            String es="";
            
            if(tc.getPanelColumns(panel).contains("COMM")){
            	start=1;
            	if(isLastDecayInPrevPanel){
            		e1=min;
            		start=0;
            	}
            	
            	if(E>e1)
            		e1=E;
            	
            	e2=nextEF;
            	
            	de=e2-e1;
            	if(coms.size()>0)
            		de=de/coms.size();
            		
            	int index=-1,size=-1;     
            	float firstLineEF=-1;
            	if(coms.size()>0){
                    int[] a=findIndexAndSize(max,e1,e2,coms.size(),0,10);
                    index=a[0];
                    size=a[1];
                    
                    int newindex=0;
                    if(de>0 && max>e1 && max<e2)
                    	newindex=(int)((max-e1)/de); 
                    
                    if(index<newindex)
                    	index=newindex;
            	}
            	
                for(int k=start;k<coms.size();k++){
                    row="";
                    tempS=coms.elementAt(k);
                    
                    
                    for(int m=0;m<tc.getPanelColumns(panel).indexOf("COMM");m++) 
                    	row+="&";
                    row+=tempS;
                    for(int m=tc.getPanelColumns(panel).indexOf("COMM");m<tc.getPanelColumns(panel).size()-1;m++) 
                    	row+="&";
                    
                    ef=e1+de*k;

                    //debug
                    //System.out.println("In DecayTableMaker line 1176: max="+max+" ef="+ef+" e1="+e1+" e2="+e2+" de="+de+" k="+k+" index="+index+" ncom="+coms.size()+" com="+tempS+"  ***"+d.ES());

                    
                    firstLineEF=ef;
                    
                    if(ef>=max) {
                    	if(panel>=0) 
                    		tc.setFirstLineEnergy(panel+1, firstLineEF);  
                    	
                    	pastBreak=true;
                    	
                        leftoverOfLastCom="";
                        for(int m=k;m<coms.size();m++) {
                            String line=coms.get(m);
                            if(!isContinuedCommentLine(line))   
                                break;                               
                                
                            leftoverOfLastCom+=" "+cleanFormattedCommentLine(line);

                        }
                        
                    	break;
                    }
                    
                    if(index>0){                      
                    	if(k>=index || k==coms.size()-1){//if previous remakeTablePanel split the comment to more lines, it is possible index (previous)>coms.size-1 (current).
                                                         //For this case, k will reach coms.size-1

                    		if(k==coms.size()-1 && k>0 && max<e2){//if break at last comment line, print that line in current panel and move the break point to next gamma
                    			firstLineEF=e2;
                    			T.setIsBreakAtLastCom(true);
                    		}
                        	
                    		if(panel>=0){
                            	tc.setFirstLineEnergy(panel+1, firstLineEF);  
                            	if(l!=null)
                            		tc.setFirstLineLevelEnergy(panel+1, l.EF());
                            	
                            	this.pastBreak=true;
                    		}
    
                        	
                        	if(k<coms.size()-1)
                        		break;
                        }	
                    }
                    
                    
                    if(ef<min) continue;
                    
                    out.addElement(row);
                    
                    //debug
                    //System.out.println("In DecayTableMaker line 1176: max="+max+" ef="+ef+" e1="+e1+" e2="+e2+" de="+de+" k="+k+" index="+index+" ncom="+coms.size()+" row="+row);

                    
                    float scale=1.0f;
                    if(Str.containSpecialSupScript(row))
                    	scale=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET;                
                    else if(Str.containSupScript(row, "0,1,2,3,4,5,6,7,8,9")){     
                        scale=NDSConfig.TABLE_LINEWIDTH_SCALE3+NDSConfig.LINEWIDTH_SCALE_OFFSET*0.2f;    
                    }else if(Str.containSupScript(row)){
                        if(Str.containJPIScriptOnly(row))
                            scale=NDSConfig.TABLE_LINEWIDTH_SCALE_JPI;
                        else if(Str.containGivenSupScriptOnly(row,"x"))
                            scale=NDSConfig.TABLE_LINEWIDTH_SCALE_SHORT;
                        else
                            scale=NDSConfig.TABLE_LINEWIDTH_SCALE3;
                    }else if(Str.containSubScript(row)) {
                        scale=NDSConfig.TABLE_LINEWIDTH_SCALE4;
                    }

                    
                    this.nTableBodyLines++;
                    this.tableBodyHeight+=NDSConfig.TABLE_BODY_LINEWIDTH3*scale;
                    
                    es=String.valueOf(ef);
                    
                    T.getDataLineEnergies().add(es);
                    T.getDataLineLevels().add(levelEnergy);
                    
                    //debug
                    //System.out.println("In DecayTableMaker: B dataEnergy="+es+" index="+T.getDataLineEnergies().size()+" panel="+panel+" EL="+l.EF()+" tempS="+tempS);
                    //System.out.println("                       e2="+e2+" e1="+e1+" ef="+ef+" E="+E+" nextEF="+nextEF);
                    
                }
            }
            
            width=findMaxCommentWidth(panel,commentsV,contRecsV,etd,outputES,DES,IS,DIS,IS2,DIS2,TIS,DTIS,LOGFT,DLOGFT,l,ens.parentAt(0).QF(),showSuppressed);
            if(width>this.maxCommentWidth)
            	this.maxCommentWidth=width;
            
            
            //debug
            //System.out.println("In DecayTableMaker: panel="+panel+" ES="+outputES+" findMaxCommentWidth="+width+" this.maxCommentWidth="+this.maxCommentWidth);
            
            //for(int i=0;i<T.getDataLineEnergies().size();i++)
            //	 System.out.println("In DecayTableMaker: panel="+panel+"EL="+l.EF()+" dataEnergy["+i+"]="+T.getDataLineEnergies().elementAt(i));

            return out;
    }
    
    private Vector<String> makeSingleDecay_manual(int j,Level l,EnsdfTableData etd,TablePanel T,int panel,Vector<ComFlag> flags,boolean showSuppressed)throws Exception{
        String ES,DES,ERS,IS,DIS,IS2="",DIS2="",TIS="",DTIS="",LOGFT,DLOGFT,flag;
        String temp="";
        Vector <String> out=new Vector<String>();
        
        float E;

        float min=-1000000;
        float max=1000000000;
        
        boolean pastMin=false,pastMax=false;
         
        ENSDF ens=etd.getENSDF();          
        TableControl tc=etd.getDecayTableControl();

    	if(panel>0&&panel<=tc.getBreakPoints().size())
    		min=Float.valueOf(tc.getBreakPoints().elementAt(panel-1).toString());
    	if(panel>=0 && panel<tc.getBreakPoints().size())
    		max=Float.valueOf(tc.getBreakPoints().elementAt(panel).toString());
        
        
        //get data, depending on what kind of decay we are working with
        Decay d;
        if(type.equals("BETA")){
            if(l!=null) d=l.betaAt(j); 
            else d=ens.unpBetaAt(j);
            
            Beta b=(Beta)d;
            
            E=b.EF();
            ES=b.ES();
            DES=b.DES();
            ERS=b.ERS();
            IS=b.RIS();
            DIS=b.DRIS();
            LOGFT=b.LOGFTS();
            DLOGFT=b.DLOGFTS();
            flag=b.flag();
        }else if(type.equals("ECBP")){
            ECBP b;
            if(l!=null) d=l.ECBPAt(j);
            else d=ens.unpECBPAt(j);
            
            b=(ECBP)d;
            
            ES=b.ES();
            E=b.EF();
            DES=b.DES();
            ERS=b.ERS();
            IS=b.IBS();
            DIS=b.DIBS();
            IS2=b.IES();
            DIS2=b.DIES();
            TIS=b.ITS();
            DTIS=b.DITS();
            LOGFT=b.LOGFTS();
            DLOGFT=b.DLOGFTS();
            flag=b.flag();
        }else{ 
            Alpha b;
            if(l!=null) d=l.alphaAt(j);
            else d=ens.unpAlphaAt(j);
            
            b=(Alpha)d;
            
            ES=b.ES();
            E=b.EF();
            DES=b.DES();
            ERS=b.ERS();
            IS=b.RIS();
            DIS=b.DRIS();
            LOGFT=b.HFS();
            DLOGFT=b.DHFS();       
            flag=b.flag();
        }
         
        //System.out.println("DecayTableMaker 1500: ES="+ES);
        
        //calculate the energy of the decay. Use this to determine whether to draw this decay in this panel of the table
        if((ES.length()<=0 || ES.contains("(")) &&l!=null){    
        	XDX2SDS e;
        	
        	if(type.equals("ALPH"))
        		e=calculateDecayParticleEnergy(l);//for alpha decay, calculate alpha-particle energy
        	else
        		e=calculateDecayEnergy(l);//for other decays, calculate total decay energy
        	
        	ES=e.s();//calculated decay energy placed in bracket, in format of (ES {\it DES}), to be printed in radiation table
        	    
            XDX2SDS e1=new XDX2SDS();
            e1.setValues(e.x(), e.dx());
            d.setEF(e.xf());
            d.setDEF(e.dxf());
            
        	//d.setES("("+e1.s()+" "+e1.ds()+")");//this could cause decay-energy column runs over the next IB column in the decay scheme, if e1.s is long     
                                                  //solution1: do not show e1.ds()        	   
            if(e1.s().length()>0){
            	if(e1.s().indexOf("+")>0 || e1.dx()==0 || e1.s().toUpperCase().indexOf("E")>0)
                	d.setES("("+Double.valueOf(e1.s()).intValue()+")");
                else if(e1.x()>0)
                	d.setES("("+e1.s()+")");
                else
                	d.setES(ES);    
            	
            	d.setERS("("+e1.s()+" "+e1.ds()+")");//used for determining column width
            }
            
        	E=(float) e.x();
        	
            //debug
            //System.out.println("In DecayTableMaker 1533: ES="+ES+" e="+e.x()+" d.ES="+d.ES()+" l.ES()="+l.ES()+" l.ef()="+l.EF()+" "+l.ERF());
            //System.out.println("                    e1.s=="+e1.s()+" e1.ds="+e1.ds()+" e.xf=="+e1.xf()+" e.dxf="+e.dxf());
            
        }else{
            //if(tc.showDE()) 
        	ES=LatexWriter.printNumber(ES,DES);//to be printed in radiation table
        	d.setERS("("+ES+" "+DES+")");//used for determining column width
        }

              
        if(l==null && Str.isNumeric(ERS))//unplaced decay
        	E=Float.parseFloat(ERS);
    	
        if(E>=min) {
        	pastMin=true;
        }
        if(E>=max) {
        	pastMax=true;
        }
        
        if(pastMax||!pastMin) return null;
                    
        Vector<String> coms;
        
        coms=procComs(panel,d,tc,showSuppressed);
        
        //get level flags
        String EBflags="",Iflags="", Lflags="",I2flags="",I3flags="",TIflags="";
        ComFlag cf;
        for(int k=0;k<flags.size();k++){
            cf=flags.elementAt(k);

            String linkID=etd.getENSDF().nucleus().EN()+etd.getNID()+"DECAY"+cf.getIndex();
            
            if(cf.hasFlagAny(flag)){
                if(cf.hasFlagAny("E",flag)) EBflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                if(cf.hasFlagAny("IB",flag)) Iflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                if(cf.hasFlagAny("IE",flag)) I2flags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                if(cf.hasFlagAny("IA",flag)) I3flags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                if(cf.hasFlagAny("TI",flag)) TIflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                if(cf.hasFlagAny("LOGFT",flag)) Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                if(cf.hasFlagAny("HF",flag))	Lflags+="{\\hyperlink{"+linkID+"}{"+cf.getSymbol()+"}}";
                
            }

        }
        
        int nColumns=tc.getPanelColumns(panel).size();
        temp="";

        for(int m=0;m<nColumns;m++){
            String colName=(String)tc.getPanelColumns(panel).elementAt(m);

            if(colName.equals("E")){
                //energy
                temp+=ES;
                
                temp+="$^{"+EBflags+"}$";
            }
            if(colName.equals("LEV")){
                //level energy
                if(j==0&&l!=null) {
                	temp+=l.ES();
                	if(tc.showDE()) temp+=" \\emph{"+l.DES()+"}";
                }
            }

            //intensity
            if(colName.equals("IB") && (IS.length()>0 || Iflags.length()>0))   temp+=LatexWriter.printNumber(IS,DIS)+"$^{"+Iflags+"}$";
            if(colName.equals("IE") && (IS2.length()>0 || I2flags.length()>0)) temp+=LatexWriter.printNumber(IS2,DIS2)+"$^{"+I2flags+"}$";
            if(colName.equals("IA") && (IS.length()>0 || I3flags.length()>0))  temp+=LatexWriter.printNumber(IS,DIS)+"$^{"+I3flags+"}$";
            if(colName.equals("TI") && (TIS.length()>0 || TIflags.length()>0)) temp+=LatexWriter.printNumber(TIS,DTIS)+"$^{"+TIflags+"}$";

            //logft
            if(colName.equals("LOGFT")||colName.equals("HF")) {
            	if(LOGFT.length()>0 || Lflags.length()>0) temp+=LatexWriter.printNumber(LOGFT,DLOGFT)+"$^{"+Lflags+"}$";
            }

            if(colName.equals("COMM") && coms.size()>0)
                temp+=coms.elementAt(0);
            //else
            //	temp+=("\\parbox[c]{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright \\vspace{0.1cm}}");
            
            if(m<nColumns-1) 
            	temp+="&";
        }//columns
        
        out.addElement(temp);     
             
        temp="";
        String row,tempS;
      
        if(tc.getPanelColumns(panel).contains("COMM")){      	      	
            for(int k=1;k<coms.size();k++){
                row="";
                tempS=coms.elementAt(k);
                for(int m=0;m<tc.getPanelColumns(panel).indexOf("COMM");m++) 
                	row+="&";
                row+=tempS;
                for(int m=tc.getPanelColumns(panel).indexOf("COMM");m<tc.getPanelColumns(panel).size()-1;m++) 
                	row+="&";
                
                out.addElement(row);               
            }
        }
       
        return out;
}
    
    private String makeDecayEntry(int decayNo,Decay d, Level l, Vector<String> columns, Vector<String> comsFormatted, String EBS,
    		String EBflags, String Iflags, String I2flags, String I3flags, String TIflags,String Lflags) throws Exception{
    	String row="";
    	String colName,temp="";
    	int nColumns=columns.size();
        String flagsLatex="";
        
    	boolean print=false;
    	
    	//debug
    	//System.out.println("In DecayTableMaker line 1483: d.ES="+EBS+" l.ES="+l.ES()+" columns size="+columns.size());
    	//if(comsFormatted.size()>0) System.out.println("      com="+comsFormatted.get(0));
    	
        for(int m=0;m<nColumns;m++){
            colName=(String)columns.elementAt(m);
        	String S="";      	
        	print=false;
        	flagsLatex="";
        	
        	
        	//System.out.println("In DecayTableMaker line 1570 colname="+colName);
        	
            if((colName.equals("E")||colName.equals("DE")) && EBS.length()>0){
            	S=EBS.trim();
            	int p1=S.indexOf("(");
            	int p2=S.lastIndexOf(")");
            	if(p1==0 && p2>p1)
            		S=S.substring(p1+1,p2);
            		
            	//System.out.println("  es="+S+" d.es="+d.ES()+" d.ef="+d.EF());
            	
            	//second condition added for case like: E(level)=x, E(decay) 
            	//can't be calculated and thus set as 0 (d.EF()), but input E(decay)>0
            	if(d.EF()>0 || (Str.isNumeric(S)&&Double.parseDouble(S)>0) ) {
            		
                    S=addFlagsLatex(S,EBflags);
                    flagsLatex=addFlagsLatex("",EBflags);
                    if((p1==0 && p2>p1) || d.q().equals("S"))
                    	S="("+S+")";

                    
                    print=true;
            	}

            }
            else if(colName.equals("LEV")||colName.equals("DLEV")){
                //level energy
                if(decayNo==0&&l!=null){ 
                	S=Translator.plainValue(l.ES(),l.DES());
                	if(l.q().equals("?"))
                		S+="?";

                    if(l.q().equals("S"))//"S" marked the level energy is to be put in bracket
                    	S="("+S+")";
                    
                	print=true;
                }
            }
            else if(colName.equals("IB")||colName.equals("DIB")){   
            	S=LatexWriter.printNumber(d.RIS(),d.DRIS());
            	S=addFlagsLatex(S,Iflags);
            	flagsLatex=addFlagsLatex("",Iflags);
            	if( d.RIS().length()>0 || Iflags.length()>0)
            		print=true;
            }
            else if(colName.equals("IE")||colName.equals("DIE")){   
            	S=LatexWriter.printNumber(d.IES(),d.DIES());
            	S=addFlagsLatex(S,I2flags);
            	flagsLatex=addFlagsLatex("",I2flags);
            	if( d.IES().length()>0 || I2flags.length()>0)
            		print=true;
            }           
            else if(colName.equals("IA")||colName.equals("DIA")){   
            	S=LatexWriter.printNumber(d.RIS(),d.DRIS());
            	S=addFlagsLatex(S,I3flags);
            	flagsLatex=addFlagsLatex("",I3flags);
            	if( d.RIS().length()>0 || I3flags.length()>0)
            		print=true;
            }            
            else if(colName.equals("TI")||colName.equals("DTI")){   
            	S=LatexWriter.printNumber(d.ITS(),d.DITS());
            	S=addFlagsLatex(S,TIflags);
            	flagsLatex=addFlagsLatex("",TIflags);
            	if( d.ITS().length()>0 || TIflags.length()>0)
            		print=true;
            }           
            else if(colName.equals("LOGFT")||colName.equals("DLOGFT")){   
            	S=LatexWriter.printNumber(d.LOGFTS(),d.DLOGFTS());
            	
            	//add "1u", "2u" forbiddenness
             	String tempFlags=d.unique().trim().toLowerCase()+Lflags;
             	
            	S=addFlagsLatex(S,tempFlags);
            	flagsLatex=addFlagsLatex("",tempFlags);
            	if( d.LOGFTS().length()>0 || tempFlags.length()>0)
            		print=true;
            }
            else if(colName.equals("HF")||colName.equals("DHF")){   
            	S=LatexWriter.printNumber(d.HFS(),d.DHFS());
            	S=addFlagsLatex(S,Lflags);
            	flagsLatex=addFlagsLatex("",Lflags);
            	if( d.HFS().length()>0 || Lflags.length()>0)
            		print=true;
            }
            
            
            //print string
            temp="";
            int n=-1;
            if(columns.contains("D"+colName)){
                temp="\\multicolumn{1}{r@{}}{";
                if(S.contains(".") && (!S.contains("times10^")||S.contains("-"))){//exclude case like 1.53X10^3 12 
                    temp+=S.substring(0,S.indexOf("."));
                }else if(!S.contains(".") && S.contains("times10^") && S.contains("-")){//e.g, 1072X10^-6 16, aliged by X
                    temp+=S.substring(0,S.indexOf("\\ensuremath{\\times10^"));
                }else if(!S.contains(".") && (n=S.indexOf("+"))>0 && n<S.length()-1 && Character.isLetter(S.charAt(n+1))){//e.g, 1234+X
                    temp+=S.substring(0,S.indexOf("+"));
                }else if(flagsLatex.length()>0){
                    temp+=S.substring(0,S.indexOf(flagsLatex));
                }else if(S.contains(" ")){
                    temp+=S.substring(0,S.indexOf(" "));
                }else{
                    temp+=S;
                }
                temp+="}";
            }else if(colName.charAt(0)=='D' && columns.contains(colName.substring(1))){
                temp="\\multicolumn{1}{@{";
                if(S.contains(".") && (!S.contains("times10^")||S.contains("-"))){
                	String tempS=S.substring(S.indexOf(".")+1);
                	if(tempS.length()>0 && tempS.charAt(0)==' ')//for val+unc pair like 3076. 15,val=3076. unc=15
                		tempS="\\ "+tempS.substring(1);
                	
                    temp+=".}l}{"+tempS;
                    
                }else if(!S.contains(".") && S.contains("times10^") && S.contains("-")){
                    temp+="}l}{"+S.substring(S.indexOf("\\ensuremath{\\times10^"));
                }else if(!S.contains(".") && (n=S.indexOf("+"))>0 && n<S.length()-1 && Character.isLetter(S.charAt(n+1))){//e.g, 1234+X
                    temp+="}l}{"+S.substring(S.indexOf("+"));
                }else if(flagsLatex.length()>0){
                    temp+="}l}{"+S.substring(S.indexOf(flagsLatex));
                }else if(S.contains(" ")){
                    temp+=" }l}{"+S.substring(S.indexOf(" ")+1);
                }else{
                    temp+="}l}{";
                }
                temp+="}";
            }else if(colName.equals("COMM")){
            	if(comsFormatted.size()>0){               
            		temp+=comsFormatted.elementAt(0);
            		print=true;
            	}
                //else
                //	temp+=("\\parbox[c]{"+tc.getCommentColumnWidth(panel)+"cm}{\\raggedright \\vspace{0.1cm}}");
            }
            
            if(print)
            	row+=temp;
            	
            if(m<nColumns-1) 
            	row+="&";
        }//columns
        
        return row;
    }
    
    private String makeTableHead(TableControl tc,String comTit,String EBflags,String Iflags,String I2flags,String I3flags,String TIflags,String Lflags,String type,int panel) throws Exception{
        //column names
        //table column labels
        String temp="";
        int nColumns=tc.getPanelColumns(panel).size();
        String colName;
       
        String Ihead="I$";
        if(comTit.contains("epsilon")) 
        	Ihead+="\\beta^{+ \\ ";
        else 
        	Ihead+=comTit;
        
        String I2head="I$\\varepsilon^{";
        String I3head="I$\\alpha^{";
        String TIhead="I$(\\varepsilon+\\beta^{+})^{";
        String Hhead;
        if(type.equals("ALPH")) Hhead="HF$^{";
        else Hhead="Log {\\it ft}$^{";
        
        //see if any of the columns need to be relabelled
        int ix;
        Comment c;
        for(int i=0;i<tc.getComments().size();i++){
            c=tc.getComments().elementAt(i);
            temp=c.body();
            ix=temp.indexOf("LABEL=");
            if(ix>=0) 
            	temp=temp.substring(ix+6);
            else 
            	continue;
            
            if(c.isBigC())
            	temp=Translator.process(temp)+"$^{";
            else
            	temp=Translator.translate(temp)+"$^{";
            
            if(c.head().equals("IB")) Ihead=temp;
            else if(c.head().equals("IE")) I2head=temp;
            else if(c.head().equals("IA")) I3head=temp;
            else if(c.head().equals("LOGFT")||c.head().equals("HF")) Hhead=temp;
        }
        temp="";
        for(int i=0;i<nColumns;i++){
            colName=(String)tc.getPanelColumns(panel).elementAt(i);
            
            //if(colName.equals("E")){
            //	temp+="\\multicolumn{2}{c}{E$"+comTit+"\\ "+EBflags+"}$}";
            //    T.addMulticol(i);
            //    i++;
            //}
            if(colName.equals("E")){
            	if(type.equals("ALPH"))
            		temp+="\\multicolumn{2}{c}{E$"+comTit+"{"+EBflags+"}}$}";
            	else
            		temp+="\\multicolumn{2}{c}{E(decay)$"+EBflags+"$}";
            	
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("LEV")){           	
            	temp+="\\multicolumn{2}{c}{E(level)}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("IB")){           	
            	temp+="\\multicolumn{2}{c}{"+Ihead+Iflags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("IE")){             	
            	temp+="\\multicolumn{2}{c}{"+I2head+I2flags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("IA")){
            	temp+="\\multicolumn{2}{c}{"+I3head+I3flags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("TI")){ 
            	temp+="\\multicolumn{2}{c}{"+TIhead+TIflags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("LOGFT")||colName.equals("HF")){
            	temp+="\\multicolumn{2}{c}{"+Hhead+Lflags+"}$}";
                T.addMulticol(i);
                i++;
            }
            else if(colName.equals("COMM")) temp+=("Comments");
            
            if(i<nColumns-1) temp+="&";
        }

        
        return temp;
    }
  
    @SuppressWarnings("unused")
	private float colHeadWidth(TableControl tc,String comTit,String EBflags,String Iflags,String I2flags,String I3flags,String TIflags,String Lflags,String type,int panel){
    	float headWidth=0;
    	HashMap<String,String> colStrings=colHeadStrings( tc,comTit,EBflags,Iflags,I2flags,I3flags,TIflags,Lflags,type,panel);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		headWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return headWidth;
    	
    }
    
    private HashMap<String,String> colHeadStrings(TableControl tc,String comTit,String EBflags,String Iflags,String I2flags,String I3flags,String TIflags,String Lflags,String type,int panel){
        //column names
        //table column labels
        String temp="";
        int nColumns;
        String colName;
        String Ihead="I";
        if(comTit.contains("epsilon")) Ihead+="B+";
        else Ihead+="B-";
        String I2head="IE";
        String I3head="IA";
        String TIhead="I(E+B+)";
        String Hhead;
        if(type.equals("ALPH")) Hhead="HF";
        else Hhead="Log ft";
        
        if(EBflags.length()>0) EBflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(EBflags));
        if(Iflags.length()>0)  Iflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(Iflags));
        if(I2flags.length()>0) I2flags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(I2flags));
        if(I3flags.length()>0) I3flags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(I3flags));
        if(TIflags.length()>0) TIflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(TIflags));
        if(Lflags.length()>0) Lflags=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsLatex(Lflags));
        

        nColumns=tc.getPanelColumns(panel).size();
        
        HashMap<String,String> colStrings=new HashMap<String,String>();
        
        //see if any of the columns need to be relabelled
        int ix;
        Comment c;
        for(int i=0;i<tc.getComments().size();i++){
            c=tc.getComments().elementAt(i);
            temp=c.body();
            ix=temp.indexOf("LABEL=");
            if(ix>=0) temp=temp.substring(ix+6);
            else continue;
            
            if(c.head().equals("IB")) Ihead=temp;
            else if(c.head().equals("IE")) I2head=temp;
            else if(c.head().equals("IA")) I3head=temp;
            else if(c.head().equals("LOGFT")||c.head().equals("HF")) Hhead=temp;
        }
        temp="";
        for(int i=0;i<nColumns;i++){
            colName=(String)tc.getPanelColumns(panel).elementAt(i);
            temp="";
            
            //if(colName.equals("E")) temp="Eb+"+EBflags; 
            if(colName.equals("E")) {
            	if(type.equals("ALPH"))
            		temp="EA"+EBflags;
            	else
            		temp="E(decay)"+EBflags; 
            }
            else if(colName.equals("LEV")) temp="E(level)";
            else if(colName.equals("IB")) temp=Ihead+Iflags;
            else if(colName.equals("IE")) temp=I2head+I2flags;
            else if(colName.equals("IA")) temp=I3head+I3flags;
            else if(colName.equals("TI")) temp=TIhead+TIflags;
            else if(colName.equals("LOGFT")||colName.equals("HF")){
                temp=Hhead+Lflags;
            }
            else if(colName.equals("COMM")) temp=("Comments");
            
            //temp+=" ";
            
            colStrings.put(colName, temp);
        }
        
        return colStrings;
    }
    
    public String commentHeadMaker(Comment com,String option) throws Exception{
    	String head="",tempHead="";
    	float Q;
    	String headName;
    	    	
    	Q=etd.getENSDF().parentAt(0).QF();
    	
    	if(com.nHeads()==0)
    		return "";
    	
        HashMap<String,String> labelMap=tc.getRelabelMap();
        HashMap<String,String> translatedLabelMap=tc.getTranslatedLabelMap();
               

        String label="",translatedLabel="";
        
    	for(int k=0;k<com.nHeads();k++){
        	headName=com.headAt(k);
        	
        	if(head.length()>0){
        		head+=",";
        		tempHead+=",";
        	}
        	
        	label=labelMap.get(headName);
        	translatedLabel=translatedLabelMap.get(headName);
        	if(label==null || label.trim().isEmpty()) {
                if(headName.equals("E")) {

                	
                	//head+=Control.transColHead(headName);
                	/*
                	if(type.equals("BETA")){
                		head+="E$\\beta^-$";
                		tempHead="Eb+";
                	}
                	else if(type.equals("ECBP")){
                		head+="E$\\epsilon$";       				
                		tempHead+="Eb+";}
                	else if(type.equals("ALPH")){
                		head+="E$\\alpha$";
                		tempHead="Ea";
                	}
                	else{
                		head+=Control.transColHead(headName);
                		tempHead="Energy";
                	}
                	*/
                	if(type.equals("ALPH")){
                		head+="E$\\alpha$";
                		tempHead="Ea";
                	}else{
                		head+="E(decay)";
                		tempHead="E(decay)";
                	}
                }
                else if(headName.equals("IB")){
                	if(type.equals("BETA")){
                		head+="I$\\beta^-$";
                		tempHead="Ib-";
                	}
                	else if(type.equals("ECBP")){
                		head+="I$\\beta^+$";       				
                		tempHead+="Ib+";            		
                	}
                	else{
                		head+=NDSControl.transColHead(headName);
                		tempHead="Ib+";
                	}
                }
                else if(headName.equals("IA")) {head+="I$\\alpha$";tempHead+="Ia ";}
                else if(headName.equals("LEV")) {head+=NDSControl.transColHead(headName);tempHead+="E(level)";}
                else if(headName.equals("IE")&&Q>1022) {head+=NDSControl.transColHead(headName);tempHead+="Ie";}
                else if(headName.equals("IE")&&Q<=1022) {head+="I$\\varepsilon$";tempHead+="Ie";}
                else if(headName.equals("TI")) {head+="I$(\\varepsilon+\\beta^{+})$";tempHead+="I(e+b+) ";}
                else if(headName.equals("LOGFT")) {head+="Log {\\it ft}";tempHead+="Log ft ";}
                else if(headName.equals("HF")) {head+="HF";tempHead+="HF";}
                else {
                	String translatedHead=Translator.process(headName);
                	head+=translatedHead;  
                	
                	if(translatedHead.contains("ensuremath"))
                		tempHead+=headName;
                	else
                		tempHead+=translatedHead;
                	
                }
        	}else {
        		head+=translatedLabel.trim();
        		tempHead+=label.trim();
        	}
        	

    	}
        	
        if(head.length()==0)
        	return "";
        
        head+=": ";
        tempHead+=": ";
        
        if(option.equals("temp"))
        	return tempHead;
        
        return head;	
    	
    }
    
    protected String commentBuilder(Comment c) throws Exception{
        String head="";
        String com="";
        float Q=etd.getENSDF().parentAt(0).QF();
        
        if(c.nHeads()>0){
            head=c.headAt(0);
            if(head.equals("E")) com=NDSControl.transColHead(head)+": ";
            else if(head.equals("LEV")) com=NDSControl.transColHead(head)+": ";
            else if(head.equals("IB")||head.equals("IA")) com=NDSControl.transColHead(head)+": ";
            else if(head.equals("IE")&&Q>1022) com=NDSControl.transColHead(head)+": ";
            else if(head.equals("IE")&&Q<=1022) com="I$(\\varepsilon)$: ";
            else if(head.equals("TI")) com="$I(\\varepsilon+\\beta^{+})$: ";
            else if(head.equals("LOGFT")||head.equals("HF")) com=NDSControl.transColHead(head)+": ";
            else com+=Translator.process(c.headAt(0))+": ";
        }
        if(c.nHeads()>0)com+=c.getHTranslated();
        else com+=c.getTranslated();
        return com;

    }
    
    
    protected Vector<Comment> convertDataAsComments(int panel,Record decay,TableControl tc)throws Exception{
 
    	Decay d=(Decay)decay;
    	
    	Vector<Comment> out=new Vector<Comment>();
    	  
    	String type="";
        String temp;

        
        String ES,DES,IS,DIS,IS2="",DIS2="",TIS="",DTIS="",LOGFTS="",DLOGFTS="",HFS="",DHFS="";
        
        ES=d.ES();
        DES=d.DES();
        
        IS=d.RIS();
        DIS=d.DRIS();
        
        Level l=new Level();
        
        if(d.getFLI()>=0 && etd.getENSDF().nLevels()>0)
        	l=etd.getENSDF().levelAt(d.getFLI());
        
        if(tc.getDataAsComments()!=null){
            for(int k=0;k<tc.getDataAsComments().size();k++){
                temp="";
                type=tc.getDataAsComments().elementAt(k);
                if(type.equals("E")&&ES.length()>0) 
                	temp=LatexWriter.printNumberAsIs(ES,DES);
                else if(type.equals("LEV")){
                	if(l.ES().length()>0)
                		temp=LatexWriter.printNumberAsIs(l.ES(),l.DES());
                }
                else if(type.equals("IB")||type.equals("IA")){               	
                	if(IS.length()>0) 
                		temp=LatexWriter.printNumberAsIs(IS,DIS);
                }
                else if(type.equals("IE")){
                	IS2=((ECBP)d).IES();
                	DIS2=((ECBP)d).DIES();
                	if(IS2.length()>0) 
                		temp=LatexWriter.printNumberAsIs(IS2,DIS2);
                }
                else if(type.equals("TI")){//for ECBP
                	TIS=((ECBP)d).ITS();
                	DTIS=((ECBP)d).DITS();
                	if(TIS.length()>0) temp=LatexWriter.printNumberAsIs(TIS,DTIS);
                }
                else if(type.equals("LOGFT")){
                    if(this.type.equals("BETA")){
                    	LOGFTS=((Beta)d).LOGFTS();
                    	DLOGFTS=((Beta)d).DLOGFTS();
                    }
                    else if(this.type.equals("ECBP")){
                    	LOGFTS=((ECBP)d).LOGFTS();
                    	DLOGFTS=((ECBP)d).DLOGFTS();
                    }
                    if(LOGFTS.length()>0) 
                    	temp=LatexWriter.printNumberAsIs(LOGFTS,DLOGFTS);
                }
                else if(type.equals("HF")){
                	HFS=((Alpha)d).HFS();
                	DHFS=((Alpha)d).DHFS();
                	if(HFS.length()>0) 
                		temp=LatexWriter.printNumberAsIs(HFS,DHFS);
                }
                
                if(temp.trim().length()>0){
                	Comment c=new Comment();
                	if(type.trim().length()>0)
                		c.headV().add(type);
                	c.setBody(temp,true);
                    out.addElement(c);
                }
                
            }
        }
        return out;
    }
    
    
    /*return a rough number of the length of all data columns in output table**/
    private float getEntryLength(int j,Level l,Vector<String> columns,ENSDF ens)throws Exception{
        String colName;
        String row="";
        String ES,IS,DIS,IS2="",DIS2="",TIS="",DTIS="",LOGFT,DLOGFT;
        int nColumns=columns.size();
        
        if(type.equals("BETA")){
            Beta b;
            if(l!=null) b=l.betaAt(j);
            else b=ens.unpBetaAt(j);
            ES=b.ES();
            IS=b.RIS();
            DIS=b.DRIS();
            LOGFT=b.LOGFTS();
            DLOGFT=b.DLOGFTS();
        }else if(type.equals("ECBP")){
            ECBP b;
            if(l!=null) b=l.ECBPAt(j);
            else b=ens.unpECBPAt(j);
            ES=b.ES();
            IS=b.IBS();
            DIS=b.DIBS();
            IS2=b.IES();
            DIS2=b.DIES();
            TIS=b.ITS();
            DTIS=b.DITS();
            LOGFT=b.LOGFTS();
            DLOGFT=b.DLOGFTS();
        }else{ 
            Alpha b;
            if(l!=null) b=l.alphaAt(j);
            else b=ens.unpAlphaAt(j);
            ES=b.ES();
            IS=b.RIS();
            DIS=b.DRIS();
            LOGFT=b.HFS();
            DLOGFT=b.DHFS();
        }
        
        for(int k=0;k<nColumns;k++){
            colName=columns.elementAt(k);
            if(colName.equals("E"))         row+=ES+" ";
            else if(colName.equals("LEV"))  row+=l.ES()+" ";
            else if(colName.equals("IB"))   row+=IS+" "+DIS+" ";
            else if(colName.equals("IE"))   row+=IS2+" "+DIS2+" ";
            else if(colName.equals("IA"))   row+=IS+" "+DIS+" ";
            else if(colName.equals("TI"))   row+=TIS+" "+DTIS+" ";
            else if(colName.equals("LOGFT")||colName.equals("HF")) row+=LOGFT+" "+DLOGFT+" ";
            //System.out.println("In DecayTableMaker:colName="+colName+" row="+row);
        }
           
        return row.length();
    }

    
    /*return a rough number of the maximum width of all data entries in output table panel**/
    @SuppressWarnings("unused")
	private float findMaxEntryWidth(Vector<String> columns,Vector<Decay> Decays,Decay firstDecay,Decay lastDecay,Vector<ComFlag> flags)throws Exception{
    	float maxEntryWidth=0;
    	HashMap<String,String> colStrings=findMaxColumnStrings(columns,Decays,firstDecay,lastDecay,flags);
    	for(Map.Entry<String, String> entry : colStrings.entrySet()){
    		maxEntryWidth+=entry.getValue().length()*NDSConfig.CHAR2CM;
    	}
    	
    	return maxEntryWidth;
    }
    
    private HashMap<String,String> findMaxColumnStrings(Vector<String> columns,Vector<Decay> Decays,Decay firstDecay,Decay lastDecay,Vector<ComFlag> flags)throws Exception{
        HashMap<String,String> colStrings=new HashMap<String,String>();
        
        if(firstDecay==null)
            return colStrings;
        
        String colName;
        String temp="";
        String ES,IS,DIS,IS2="",DIS2="",TIS="",DTIS="",LOGFT,DLOGFT;
        
        StringMax stringMax;
        
        float maxColWidth=0;
        float colWidth=0;
        
        String fakeFlagStr="",unFlag="";
        String maxColString="";
        ENSDF ens=etd.getENSDF();
       	 
        int start=Decays.indexOf(firstDecay);
        int end  =Decays.indexOf(lastDecay);
        
        int nColumns=columns.size();

        
        //if(start<0) start=0;
        //if(end<0) end=Decays.size()-1;
           
        Level l=new Level();
        
        if(type.equals("BETA")){
            for(int j=0;j<nColumns;j++){    
                colName=columns.elementAt(j);
               	maxColString="";
               	maxColWidth=0;
               	
           	    stringMax=new StringMax();
           	 
                for(int k=start;k<=end;k++){          	
                	Beta b=(Beta)Decays.elementAt(k); 
                	if(b.getFLI()>=0 && b.getFLI()<ens.nLevels()) l=ens.levelAt(b.getFLI());
                	
                    ES=b.ERS();
                    IS=b.RIS();
                    DIS=b.DRIS();
                    LOGFT=b.LOGFTS();
                    DLOGFT=b.DLOGFTS();
            
                    temp="";
                    fakeFlagStr="";
                    
                    unFlag="";
                    if(b.unique().length()>0)
                    	unFlag="u";
                    
                    if(colName.equals("E")) temp=ES;
                    else if(colName.equals("LEV"))  temp=Str.removeExpFormat(l.ES());
                    else if(colName.equals("IB"))   temp=Str.getString(IS,DIS,"");
                    else if(colName.equals("IE"))   temp=Str.getString(IS2,DIS2,"");
                    else if(colName.equals("IA"))   temp=Str.getString(IS,DIS,"");
                    else if(colName.equals("TI"))   temp=Str.getString(TIS,DTIS,"");
                    else if(colName.equals("LOGFT")||colName.equals("HF")) temp=Str.getString(LOGFT,DLOGFT,unFlag); 
                    else continue;
                    
                    fakeFlagStr=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsByType(flags,b.flag(),colName));
                    
                    if(temp.contains("E") || temp.contains("e"))//eg, 1.2E3 translated as 1.2x10^3
                    {
                    	temp+="*";
                    	if(!temp.toUpperCase().contains("E+") && !temp.toUpperCase().contains("E-") )
                    		temp+="*";
                    	else
                    		temp+="*";
                    }
                    
                    stringMax.compare(temp, fakeFlagStr);
                        
                    temp=stringMax.str;
                    colWidth=stringMax.width;
                    
                    if(temp.trim().length()>0 && colWidth>maxColWidth){
                    	maxColString=temp;    
                    	maxColWidth=colWidth;
                    }    

                }

                colStrings.put(colName, maxColString);
            }           
        }             
        else if(type.equals("ECBP")){
            for(int j=0;j<nColumns;j++){    
                colName=columns.elementAt(j);
               	maxColString="";
               	maxColWidth=0;
               	
               	stringMax=new StringMax();
                for(int k=start;k<=end;k++){          	
                	ECBP b=(ECBP)Decays.elementAt(k); 
                	if(b.getFLI()>=0 && b.getFLI()<ens.nLevels()) l=ens.levelAt(b.getFLI());
                	
                    unFlag="";
                    if(b.unique().length()>0)
                    	unFlag="u";
                    
                    ES=b.ERS();
                    IS=b.IBS();
                    DIS=b.DIBS();
                    IS2=b.IES();
                    DIS2=b.DIES();
                    TIS=b.ITS();
                    DTIS=b.DITS();
                    LOGFT=b.LOGFTS();
                    DLOGFT=b.DLOGFTS();
            
                   //System.out.println("DecayTableMaker 2370: lev="+b.getFLS()+" ES="+b.ERS()+" ES0="+b.ES0()+" "+b.DES0()+" ES="+b.ES()+" "+b.DES());
                    
                    temp="";
                    if(colName.equals("E"))         temp=ES;
                    else if(colName.equals("LEV"))  temp=Str.removeExpFormat(l.ES());
                    else if(colName.equals("IB"))   temp=Str.getString(IS,DIS,""); 
                    else if(colName.equals("IE"))   temp=Str.getString(IS2,DIS2,"");
                    else if(colName.equals("IA"))   temp=Str.getString(IS,DIS,"");
                    else if(colName.equals("TI"))   temp=Str.getString(TIS,DTIS,"");
                    else if(colName.equals("LOGFT")||colName.equals("HF")) temp=Str.getString(LOGFT,DLOGFT,unFlag); 
                    else continue;
                     
                    fakeFlagStr=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsByType(flags,b.flag(),colName));
                    
                    if(temp.contains("E") || temp.contains("e"))//eg, 1.2E3 translated as 1.2x10^3
                    {
                    	temp+="*";
                    	if(!temp.toUpperCase().contains("E+") && !temp.toUpperCase().contains("E-") )
                    		temp+="*";
                    	else
                    		temp+="*";
                    }
                    
                    stringMax.compare(temp, fakeFlagStr);
                    
                    temp=stringMax.str;
                    colWidth=stringMax.width;
                    
                    if(temp.trim().length()>0 && colWidth>maxColWidth){
                    	maxColString=temp;    
                    	maxColWidth=colWidth;
                    }        

                }
                colStrings.put(colName, maxColString);
            } 
        }
        else{ 
            for(int j=0;j<nColumns;j++){    
                colName=columns.elementAt(j);
               	maxColString="";
               	maxColWidth=0;
               	
               	stringMax=new StringMax();
               	
                for(int k=start;k<=end;k++){          	
                	Alpha b=(Alpha)Decays.elementAt(k); 
                	if(b.getFLI()>=0 && b.getFLI()<ens.nLevels()) l=ens.levelAt(b.getFLI());
                	
                    ES=b.ERS();
                    IS=b.RIS();
                    DIS=b.DRIS();
                    LOGFT=b.HFS();
                    DLOGFT=b.DHFS();
            
                    temp="";
                    if(colName.equals("E"))         temp=ES;
                    else if(colName.equals("LEV"))  temp=Str.removeExpFormat(l.ES());
                    else if(colName.equals("IB"))   temp=Str.getString(IS,DIS,""); 
                    else if(colName.equals("IE"))   temp=Str.getString(IS2,DIS2,"");  
                    else if(colName.equals("IA"))   temp=Str.getString(IS,DIS,""); 
                    else if(colName.equals("TI"))   temp=Str.getString(TIS,DTIS,""); 
                    else if(colName.equals("LOGFT")||colName.equals("HF")) temp=Str.getString(LOGFT,DLOGFT,"");  
                    
                    temp+=Str.repeat(NDSConfig.fakeFlagLabel,findNflagsByType(flags,b.flag(),colName));
                    
                    if(temp.contains("E") || temp.contains("e"))//eg, 1.2E3 translated as 1.2x10^3
                    {
                    	temp+="*";
                    	if(!temp.toUpperCase().contains("E+") && !temp.toUpperCase().contains("E-") )
                    		temp+="*";
                    }
                    
                    stringMax.compare(temp, fakeFlagStr);
                    
                    temp=stringMax.str;
                    colWidth=stringMax.width;
                    
                    if(temp.trim().length()>0 && colWidth>maxColWidth){
                    	maxColString=temp;    
                    	maxColWidth=colWidth;
                    }       

                }
                colStrings.put(colName, maxColString);
            }   
        }
        
        return colStrings;
    }
    
    private float findMaxCommentWidth(int panel,Vector<Comment>commentsV,Vector<ContRecord> contRecsV,EnsdfTableData etd,String ES,String DES,
            String IS,String DIS,String I2S,String DI2S,String TIS,String DTIS,String LOGFTS,String DLOGFTS,Level l, float Q,boolean showSuppressed)throws Exception{
        String com="";
        TableControl tc=etd.getDecayTableControl();
        Comment c;
        float width;
        float maxCommentWidth=0;
        float commentColumnWidth=tc.getCommentColumnWidth(panel); 
        int nlines=0;
        
        for(int k=0;k<contRecsV.size();k++){
            ContRecord cr=contRecsV.elementAt(k);
            if(!cr.isExtension() && (!cr.isSuppressed()||showSuppressed) ){
                com=cr.name()+"="+cr.s()+" "+cr.ds()+" "+cr.ref(true);
                
                width=com.length()*NDSConfig.CHAR2CM;
                if(width>maxCommentWidth) maxCommentWidth=width;
                if(width>0)
               	 nlines+=width/commentColumnWidth+1;
            }
        }
        
        for(int k=0;k<commentsV.size();k++){
            String head="";
            com="";
            c=commentsV.elementAt(k);
            if(c.nHeads()>0){
                head=c.headAt(0);
                if(head.equals("E")) com=NDSControl.transColHeadENSDF(head)+": ";
                else if(head.equals("LEV")) com=NDSControl.transColHeadENSDF(head)+": ";
                else if(head.equals("IB")||head.equals("IA")) com=NDSControl.transColHeadENSDF(head)+": ";
                else if(head.equals("IE")&&Q>1022) com=NDSControl.transColHeadENSDF(head)+": ";
                else if(head.equals("IE")&&Q<=1022) com="Ie: ";
                else if(head.equals("TI")) com="I(e+b+)$: ";
                else if(head.equals("LOGFT")||head.equals("HF")) com=NDSControl.transColHeadENSDF(head)+": ";
                else com+=Translator.process(c.headAt(0))+": ";
            }
            com+=c.body();
            
            width=com.length()*NDSConfig.CHAR2CM;
            if(width>maxCommentWidth) maxCommentWidth=width;
            if(width>0)
           	 nlines+=width/commentColumnWidth+1;
        }
        if(tc.getDataAsComments()!=null){
            for(int k=0;k<tc.getDataAsComments().size();k++){
                String type=tc.getDataAsComments().elementAt(k);
                if(type.equals("E")&&ES.length()>0) com=NDSControl.transColHeadENSDF(type)+": "+ES+" "+DES;
                if(type.equals("LEV")&&l.ES().length()>0) com=NDSControl.transColHeadENSDF(type)+": "+l.ES()+" "+l.DES();
                if((type.equals("IB")||type.equals("IA")) &&IS.length()>0) com=NDSControl.transColHeadENSDF(type)+": "+IS+" "+DIS;
                if(type.equals("IE")&&I2S.length()>0) com=NDSControl.transColHeadENSDF(type)+": "+I2S+" "+DI2S;
                if(type.equals("TI")&&TIS.length()>0) com="I(e+b+): "+TIS+" "+DTIS;
                if((type.equals("LOGFT")||type.equals("HF"))&&LOGFTS.length()>0) com=NDSControl.transColHeadENSDF(type)+": "+LOGFTS+" "+DLOGFTS;
                
                width=com.length()*NDSConfig.CHAR2CM;
                if(width>maxCommentWidth) maxCommentWidth=width;   
                if(width>0)
               	 nlines+=width/commentColumnWidth+1;
            }
        }
        
        this.nCommentLines=nlines;
        return maxCommentWidth;
    }    
    
    private XDX2SDS calculateDecayEnergy(Level l) throws Exception{
    	return calculateEnergy(l,"TOTAL");
    }
    
    private XDX2SDS calculateDecayParticleEnergy(Level l) throws Exception{
    	return calculateEnergy(l,"PARTICLE");
    }
    
    //option="TOTAL" for total decay energy
    //      ="PARTICLE" for decay particle energy
	@SuppressWarnings("unused")
	private XDX2SDS calculateEnergy(Level l,String option) throws Exception{
    	
    	ENSDF ens=etd.getENSDF();
	    float x=-1,dx=-1;
	    float factor=1.0f;
	    String ES="",temp="";
        XDX2SDS e=new XDX2SDS();
        
        SDS2XDX offset=null,sx=null;

        boolean isSyst=false,isLimit=false;;
        boolean isRange=false;//true for level like SN+X, used in estimation for a range of levels
        
        Parent p=ens.parentAt(0);
        
        float pmass=p.nucleus().m();//in amu, multiply 931494 to convert to keV
        float mass=ens.nucleus().m();


	    if(!Str.isNumeric(l.ES())){
	    	e.setValues(0,0);
	    	e.setS("");
	    	e.setDS("");
	    	
    		//debug
    		//System.out.println("In DecayTableMaker: calcDecayEn1: l.ES="+l.ES()+" l.NNES="+l.NNES());
    		
	    	if(l.ES().contains("+")){//SN+X,SP+X,SA+X, 6913.0+X
	    		int pos=l.ES().indexOf("+");
	    		String SN=l.ES().substring(0,pos);
	    		String X=l.ES().substring(pos+1);
	    		
	    		
	    		if(X.trim().length()==1 && Character.isLetter(X.trim().charAt(0))){
	    			//do nothing
	    		}else if(SN.trim().length()==1 && Character.isLetter(SN.trim().charAt(0))){
	    			String tempS=X;
	    			X=SN;
	    			SN=tempS;
	    		}else
	    			return e;
	    			
    			if(ens.offsetMap().containsKey(X.toUpperCase())) 
    				offset=ens.offsetMap().get(X.toUpperCase());
    				
	    		//NOTE: a generic footnote comment is generated for decay energy of levels like this when parsing the input ENSDF file 
	    		if(Str.isNumeric(SN)){//6345.6+X
	    			if(offset==null) {
	    				x=(p.level().EF()+p.QF()-Float.parseFloat(SN))/2; //set X=(Q-SN)/2, DX=X, estimation of decay energy for decay-heat calculation  
	    			}else {
	    				sx=new SDS2XDX(p.level().ES(),p.level().DES());
	    				sx=sx.add(p.QS(),p.DQS());
	    				
	    				
	    				sx=sx.subtract(SN,l.DES());
	    				    				
	    				sx=sx.subtract(offset);
	    				
	    				x=sx.xf();
	    				dx=sx.dxf();
	    			}
 			
	    			//System.out.println("DecayTableMaker 2593: lev="+l.ES()+" decay E x="+x+" dx="+dx+" offset="+offset.s()+" "+offset.ds()+"$");
	    			
	    		}else{//SN+X, for this case, calculate SN using masses
	    			Nucleus nucd;//daughter nucleus for SN or SP or SA
	    			Nucleus nuce;//ejectile in decay, proton or neutron or alpha
	    			Nucleus nuc=ens.nucleus();
                    
	    			int np=1;
	    			if(SN.equals("SN") || SN.equals("S(n)")){
	    				nucd=new Nucleus(nuc.z(),nuc.A()-1);
	    				nuce=new Nucleus(0,1);
	    			}else if(SN.equals("SP") || SN.equals("S(p)")){
	    				nucd=new Nucleus(nuc.z()-1,nuc.A()-1);
	    				nuce=new Nucleus(1,1);
	    				
	    			}else if(SN.equals("S2N") || SN.equals("S(2n)")){
	    				nucd=new Nucleus(nuc.z(),nuc.A()-2);
	    				nuce=new Nucleus(0,1);
	    				np=2;
	    				
	    			}else if(SN.equals("S2P") || SN.equals("S(2p)")){
	    				nucd=new Nucleus(nuc.z()-2,nuc.A()-2);
	    				nuce=new Nucleus(1,1);
	    				np=2;
	    			}else if(SN.equals("SA") || SN.contains("alpha")){
	    				nucd=new Nucleus(nuc.z()-2,nuc.A()-4);
	    				nuce=new Nucleus(2,4);
	    			}else{
		    			float ef=p.level().EF()+p.QF();
		    			ES=String.format("%.1f", ef);
		    			ES=ES+"-"+SN.trim()+"-"+X.trim().toLowerCase();
			    		ES="("+ES+")";
			    		e.setS(ES);
			    		return e;
	    			}
	    			
	    			float massd=data.findMass(nucd.A(), nucd.findElementSymbol()).xf();
	    			float masse=data.findMass(nuce.A(), nuce.findElementSymbol()).xf();
	    			float sn=(massd+np*masse-mass)*931494;//mass in unit of amu, energy in keV
	    			
	    			if(offset==null) {
	    				x=(p.level().EF()+p.QF()-sn)/2;
	    			}else {
	    				sx=new SDS2XDX(p.level().ES(),p.level().DES());
	    				sx=sx.add(p.QS(),p.DQS());
	    				sx=sx.subtract(sn);
	    				sx=sx.subtract(offset);
	    				x=sx.xf();
	    				dx=sx.dxf();
	    			}
	    			    			
	    			//debug
	    			//System.out.println("In DecayTableMaker line 2378: massd="+massd+" masse="+masse+" mass="+mass+" sn="+sn+" x="+x);
	    		}
	    		
	    		isRange=true;
	    		    			
	    	}else{
	    		return e;
	    	}

	    }else{//for regular level energy
	    	x=p.level().EF()+p.QF()-l.EF();//in keV
	    }
	    			                
        
        //debug
        //System.out.println("*****In DecayTableMaker A: p.Q="+p.QF()+" El="+l.EF()+"  x="+x);
        
        if(option.toUpperCase().equals("PARTICLE"))
        	factor=(mass/pmass+(x+2*l.EF())/(2*pmass*931494));//relativistic for decay particle energy
        else
        	factor=1.0f;//for total energy released
        
        x=x*factor;

        //String pms =p.nucleus().ms();
        //String pdms=p.nucleus().dms();
        //String ms  =ens.nucleus().ms();
        //String dms =ens.nucleus().dms();
        //String tempms=pms+pdms+ms+dms;
        
        
        //if(tempms.contains("#")||p.DQS().toUpperCase().contains("SY")){
        if(p.DQS().toUpperCase().contains("SY")){
        	dx=0;
        	isSyst=true;
        }
        //debug
        //System.out.println("In DecayTableMaker:tempms="+tempms+" p.DQS="+p.DQS()+" isSyst="+isSyst+" *"+pms+" *"+pdms+" *"+ms+" *"+dms);

        
        if(!isSyst){
        	float defP=p.level().DEF();
        	float defQ=p.DQF();
        	float defL=l.DEF();
        	if(defP<0 && p.level().DES().isEmpty())
        		defP=0;
        	if(defQ<0 && p.DQS().isEmpty())
        		defQ=0;
        	if(defL<0 && l.DES().isEmpty())
        		defL=0;
        	
        	if(isRange) {
        		if(dx<0)
        			dx=x;
        	}else if(defP>=0 && defQ>=0 && defL>=0)
        		dx=factor*(float)Math.sqrt(Math.pow(p.level().DEF(),2)+Math.pow(p.DQF(),2)+Math.pow(l.DEF(),2));
        	else
        		isLimit=true;
        }
        
        e.setValues(x,dx);
        
        if(isSyst)
        	e.setDS("SY");
        else if(isLimit) {
        	if(p.level().DEF()<0){
        		e.setDS(p.level().DES());
        	}else if (p.DQF()<0)
                e.setDS(p.DQS());
            else if(l.DEF()<0) {
            	String ds=l.DES();
            	if(ds.startsWith("L"))
            		ds="G"+ds.substring(1);
            	else if(ds.startsWith("G"))
            		ds="L"+ds.substring(1);
            	
            	e.setDS(ds);
            }
        }else if(dx==0) {
        	e.setS(""+Float.valueOf(x).intValue());
        	e.setDS("");
        }

        
        ES=e.s();
        
        
        //debug
        //System.out.println("*****In DecayTableMaker B: p.Q="+p.QF()+"p.level.EF="+p.level().EF()+" ES="+p.level().ES()+" El="+l.EF()+"  x="+x+" e.x()="+e.x()+" isSyst="+isSyst);
        //System.out.println("                        B: mass"+mass+" pmass="+pmass+" factor="+factor+" ES="+ES+" e.s()="+e.s()+" e.ds()="+e.ds());
        
        
        //NOTE:tc.showDE is for level energy
        temp=LatexWriter.printNumber(ES,e.ds());
        //if(tc.showDE()) temp+="("+LatexWriter.printNumber(ES,e.ds())+")";
        //else temp+="("+ES+")";
                
        ES=temp;
        
        int ix=-1,ix1=-1,ix2=-1,ix3=-1;
        ix=temp.indexOf(" ");
        if(ix>0){
        	ES=temp.substring(0, ix);
        	temp=temp.substring(ix);	
        }
        else
        	temp="";
        
        
        /*
        if(!Str.isNumeric(p.level().ES()))//for level like 123.3+X
        	ix1=p.level().ES().indexOf("+");
        if(!Str.isNumeric(l.ES()))
        	ix2=l.ES().indexOf("+");
        if(!Str.isNumeric(p.QS()))
        	ix3=p.QS().indexOf("+");
        

        if(ix1>0&&ix1<p.level().ES().length()) 
        	ES+=p.level().ES().substring(ix1);
        else if(ix2>0&&ix2<l.ES().length())
        	ES+=l.ES().substring(ix2);
        else if(ix3>=0&&ix3<p.QS().length())  
        	ES+=p.QS().substring(ix3);  
        */
        
        ES+=temp;
        
        ES="("+ES+")";
        
        e.setS(ES);
        return e;
    }
	
    @SuppressWarnings("unused")
	private XDX2SDS calculateDecayEnergy_old(Level l) throws Exception{
    	
    	ENSDF ens=etd.getENSDF();
	    float x,dx=0;
	    float factor=1.0f;
	    String ES="",DES="",temp="";
        XDX2SDS e=new XDX2SDS();
        

        boolean isSyst=false;
        
        Parent p=ens.parentAt(0);
        
        float pmass=p.nucleus().m();//in amu, multiply 931494 to convert to keV
        float mass=ens.nucleus().m();

       
	    if(!Str.isNumeric(l.ES())){
	    	e.setValues(0,0);
	    	e.setS("");
	    	e.setDS("");
	    	
    		//debug
    		//System.out.println("In DecayTableMaker: calcDecayEn1: l.ES="+l.ES()+" l.NNES="+l.NNES());
    		
	    	if(l.ES().contains("+")){//SN+X,SP+X,SA+X, 6913.0+X
	    		int pos=l.ES().indexOf("+");
	    		String SN=l.ES().substring(0,pos);
	    		String X=l.ES().substring(pos+1);
	    		
	    		
	    		if(X.trim().length()==1 && Character.isLetter(X.trim().charAt(0))){
	    			//do nothing
	    		}else if(SN.trim().length()==1 && Character.isLetter(SN.trim().charAt(0))){
	    			String tempS=X;
	    			X=SN;
	    			SN=tempS;
	    		}else
	    			return e;
	    			
	    		if(Str.isNumeric(SN)){//6345.6+X
    				float ef=(p.level().EF()+p.QF()-Float.parseFloat(SN))/2; //set X=(Q-SN)/2, DX=X, estimation of decay energy for decay-heat calculation
    				e.setValues(ef,ef);   
    				
    				//ES=String.format("%.1f", ef);
    				//ES=ES+"-"+X.trim().toLowerCase();    			
	    		}else{//SN+X
	    			float ef=p.level().EF()+p.QF();
	    			ES=String.format("%.1f", ef);
	    			ES=ES+"-"+SN.trim()+"-"+X.trim().toLowerCase();
	    		}
	    		
	    		ES="("+ES+")";
	    		e.setS(ES);
	    		
	    		//debug
	    		//System.out.println("In DecayTableMaker: calcDecayEn2: ES="+ES);
	    			
	    	}
	    	
	    	
	    	return e;
	    }
	    			
        x=p.level().EF()+p.QF()-l.EF();//in keV
        
        
        //debug
        //System.out.println("*****In DecayTableMaker A: p.Q="+p.QF()+" El="+l.EF()+"  x="+x);
        
        //factor=(mass/pmass+(x+2*l.EF())/(2*pmass*931494));//relativistic for decay particle energy
        
        factor=1.0f;//for total energy released
        
        x=x*factor;

        //String pms =p.nucleus().ms();
        //String pdms=p.nucleus().dms();
        //String ms  =ens.nucleus().ms();
        //String dms =ens.nucleus().dms();
        //String tempms=pms+pdms+ms+dms;
        
        
        //if(tempms.contains("#")||p.DQS().toUpperCase().contains("SY")){
        if(p.DQS().toUpperCase().contains("SY")){
        	dx=0;
        	isSyst=true;
        }
        //debug
        //System.out.println("In DecayTableMaker:tempms="+tempms+" p.DQS="+p.DQS()+" isSyst="+isSyst+" *"+pms+" *"+pdms+" *"+ms+" *"+dms);

        
        if(!isSyst)
        	dx=factor*(float)Math.sqrt(Math.pow(p.level().DEF(),2)+Math.pow(p.DQF(),2)+Math.pow(l.DEF(),2));

        e.setValues(x,dx);
        
        if(dx==0){
        	e.setS(""+Float.valueOf(x).intValue());
        	e.setDS("");
        }
        
        if(isSyst)
        	e.setDS("SY");
                
        ES=e.s();
        
        
        //debug
        //System.out.println("*****In DecayTableMaker B: p.Q="+p.QF()+"p.level.EF="+p.level().EF()+" ES="+p.level().ES()+" El="+l.EF()+"  x="+x+" e.x()="+e.x()+" isSyst="+isSyst);
        //System.out.println("                        B: mass"+mass+" pmass="+pmass+" factor="+factor+" ES="+ES+" e.s()="+e.s()+" e.ds()="+e.ds());
        
        
        //NOTE:tc.showDE is for level energy
        temp=LatexWriter.printNumber(ES,e.ds());
        //if(tc.showDE()) temp+="("+LatexWriter.printNumber(ES,e.ds())+")";
        //else temp+="("+ES+")";
                
        ES=temp;
        
        int ix=-1,ix1=-1,ix2=-1,ix3=-1;
        ix=temp.indexOf(" ");
        if(ix>0){
        	ES=temp.substring(0, ix);
        	temp=temp.substring(ix);	
        }
        else
        	temp="";
        
        
        if(!Str.isNumeric(p.level().ES()))//for level like 123.3+X
        	ix1=p.level().ES().indexOf("+");
        if(!Str.isNumeric(l.ES()))
        	ix2=l.ES().indexOf("+");
        if(!Str.isNumeric(p.QS()))
        	ix3=p.QS().indexOf("+");
        

        if(ix1>0&&ix1<p.level().ES().length()) 
        	ES+=p.level().ES().substring(ix1);
        else if(ix2>0&&ix2<l.ES().length())
        	ES+=l.ES().substring(ix2);
        else if(ix3>=0&&ix3<p.QS().length())  
        	ES+=p.QS().substring(ix3);  
        
        ES+=temp;
        
        ES="("+ES+")";
        
        e.setS(ES);
        return e;
    }
}


