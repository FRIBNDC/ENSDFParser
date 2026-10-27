package ensdfparser.nds.control;
import java.util.Vector;

import ensdfparser.ensdf.*;

/** stores some information about a metapost drawing so that the
 * latex writer can insert that drawing in the right place
 */
public class DrawingControl implements Cloneable{
    protected int nPages; //number of pages in drawing
    protected boolean isPortrait; //is this drawing portrait or landscape
    Nucleus nuc; //the nucleus that this drawing is about
    protected float height;
    protected float width;
    
    protected float decayTableXOffset=0;
    
    //for skeleton
    protected float ladderSpacing;
    protected float ladderWidth;
    
    protected float heightScale;
    protected float widthScale;
    
    protected float minY;
    protected boolean isNewPage;
    protected boolean isDrawn;
    protected float moveQ;
    protected boolean supFlag;
    protected boolean supPN;
    protected boolean supGammaLabels;
    protected boolean showAllGammaLabels;
    protected boolean drawDecayFedLevels;
    
    protected boolean showTitle;
    protected boolean showLegend;
    protected boolean showGSQValue;
    protected boolean showQValueUnit;
    
    protected boolean isBandCreated;
    
    protected float levelLabelSize;//in units of pt
    protected float gammaLabelSize;
    protected float otherLabelSize;
    protected float bandLabelSize;
    
    protected Vector<String> subtitle;
    
    public DrawingControl (){
        isPortrait=true;
      height=-1;
      width=-1;
      
      decayTableXOffset=0;
      
      heightScale=-1;
      widthScale=-1;
      
      isNewPage=false;
      supFlag=false;
      supPN=false;
      minY=6;
      
      supGammaLabels=false;
      showAllGammaLabels=false;
      drawDecayFedLevels=true;
      
      isBandCreated=false;
      
      
      showTitle=true;
      showLegend=true;
      showGSQValue=true;
      showQValueUnit=false;
      
      levelLabelSize=0.7f*10*ensdfparser.nds.config.NDSConfig.levelLabelScale;;//pt
      gammaLabelSize=0.6f*10;
      
      otherLabelSize=0.8f*10;
      
      bandLabelSize=(float)0.7*10*ensdfparser.nds.config.NDSConfig.bandLabelScale;


    }
    
    
    public void resetLabelSize(){
        levelLabelSize=0.7f*10;//pt
        gammaLabelSize=0.6f*10;
        
        otherLabelSize=0.8f*10;
        
        bandLabelSize=(float)0.7*10*ensdfparser.nds.config.NDSConfig.bandLabelScale;
    }
    
    public void reset(){
        isPortrait=true;
      height=-1;
      width=-1;
      
      decayTableXOffset=0;
      
      heightScale=-1;
      widthScale=-1;
      
      isNewPage=false;
      supFlag=false;
      supPN=false;
      minY=6;
      
      supGammaLabels=false;
      showAllGammaLabels=false;
      drawDecayFedLevels=true;
      
      isBandCreated=false;
      
      showTitle=true;
      showLegend=true;
      showGSQValue=true;
      showQValueUnit=false;
      
      levelLabelSize=0.7f*10;//pt
      gammaLabelSize=0.6f*10;
      
      otherLabelSize=0.8f*10;
      
      bandLabelSize=(float)0.7*10*ensdfparser.nds.config.NDSConfig.bandLabelScale;

    }
    
    public void loadControlLine(String temp){
        isDrawn=true;
        height=NDSControl.getLineAsFloat(temp,"/Y:");
        width=NDSControl.getLineAsFloat(temp,"/X:");
        if(NDSControl.getLineAsFloat(temp,"/M:")>-1)minY=NDSControl.getLineAsFloat(temp,"/M:");
        if(temp.contains("/P:")) isNewPage=true;
        if(temp.contains("/N:")){
            nPages=Integer.parseInt(NDSControl.getLine(temp,"/N:").elementAt(0));
        }
        if(temp.contains("/H:")) isPortrait=false;
        try{
            if(temp.contains("/Q:")) moveQ=Float.parseFloat(NDSControl.getLineAsString(temp,"/Q:"));
        }catch(Exception e){
            moveQ=0;
        }
        subtitle=NDSControl.getLine(temp,"/T:");
        Vector<String>sups=NDSControl.getLine(temp,"/S:");
        if(sups!=null){
            if(sups.contains("PN")) supPN=true;
            if(sups.contains("FLAG")) supFlag=true;
        }
    }
    public void writeControlLine(java.io.Writer out)throws java.io.IOException{
        if(height!=-1) out.write("/Y:"+height); 
        if(width!=-1) out.write("/X:"+width);
        if(minY!=2) out.write("/M:"+minY);
        if(isNewPage) out.write("/P:");
        out.write("/N:"+nPages);
        if(moveQ!=0) out.write("/Q:"+moveQ);
        if(!isPortrait) out.write("/H:");
        if(subtitle!=null){
            out.write("/T:");
            for(int i=0;i<subtitle.size();i++){
                out.write(subtitle.elementAt(i));
                if(i<subtitle.size()-1) out.write(",");
            }
        }
        if(supFlag||supPN) out.write("/S:");
        if(supFlag) out.write("FLAG");
        if(supFlag&&supPN) out.write(",");
        if(supPN) out.write("PN");
        
        out.write("\n");
    }
    public boolean isDrawn(){return isDrawn;}
    public void setDrawn(boolean b){isDrawn=b;}
    
    public void setGammaLabelSize(float size){gammaLabelSize=size;}
    public void setLevelLabelSize(float size){levelLabelSize=size;}
    public void setBandLabelSize(float size){bandLabelSize=size;}
    public void setOtherLabelSize(float size){otherLabelSize=size;}
    
    public void scaleGammaLabelSize(float scale){gammaLabelSize*=scale;}
    public void scaleLevelLabelSize(float scale){levelLabelSize*=scale;}
    public void scaleBandLabelSize(float scale){bandLabelSize*=scale;}
    public void scaleOtherLabelSize(float scale){otherLabelSize*=scale;}
    
    public float gammaLabelSize(float scale){return scale*gammaLabelSize;}
    public float levelLabelSize(float scale){return scale*levelLabelSize;}
    public float bandLabelSize(float scale){return scale*bandLabelSize;}
    public float otherLabelSize(float scale){return scale*otherLabelSize;}
    
    public float gammaLabelSize(){return gammaLabelSize;}
    public float levelLabelSize(){return levelLabelSize;}
    public float bandLabelSize(){return bandLabelSize;}
    public float otherLabelSize(){return otherLabelSize;}
    
    public boolean isNewPage(){return isNewPage;}
    public float rescaleH(){
        if(heightScale==-1) return -1;
        else return heightScale;
    }
    public float rescaleW(){
        if(widthScale==-1) return -1;
        else return widthScale;
    }
    public float getMinY(){
        return minY;
    }
    
    public float getWidth(){return width;}
    public float getHeight(){return height;}
    public float getSpacing(){return minY;}
    public float getDecayTableXOffset(){return decayTableXOffset;}
    
    public float getLadderSpacing() {return ladderSpacing;}
    public float getLadderWidth(){return ladderWidth;}
    
    /** The number of pages in the drawing */
    public int getPages(){return nPages;}
    public void setPages(int i){nPages=i;}
    
    /**whether the drawing has portrait or landscape orientation */
    public boolean isPortrait(){return isPortrait;}
    public void  setPortrait(boolean b){isPortrait=b;}
    
    public boolean isBandCreated(){return isBandCreated;}
    
    
    
    public boolean isShowTitle(){return showTitle;}
    public boolean isShowLegend(){return showLegend;}
    public boolean isShowGSQValue(){return showGSQValue;}
    public boolean isShowQValueUnit(){return showQValueUnit;}
    
    public void setNewPage(boolean b){isNewPage=b;}
    public void setWidth(float w){width=w;}
    public void setHeight(float h){height=h;}
    public void setSpacing(float sp){minY=sp;}
    
    public void setDecayTableXOffset(float offset){decayTableXOffset=offset;}
    
    //for skeleton
    public void setLadderSpacing(float ls){ladderSpacing=ls;}
    public void setLadderWidth(float lw){ladderWidth=lw;}
    
    public void setSupGammaLabels(boolean b){supGammaLabels=b;}
    public void setShowAllGammaLables(boolean b){showAllGammaLabels=b;}
    public void setDrawDecayFedLevels(boolean b){drawDecayFedLevels=b;}
    public void setIsBandCreated(boolean b){isBandCreated=b;}
    
    
    public void setShowTitle(boolean b){showTitle=b;}
    public void setShowLegend(boolean b){showLegend=b;}
    public void setShowGSQValue(boolean b){showGSQValue=b;}
    public void setShowQValueUnit(boolean b){showQValueUnit=b;}
    
    /** The nucleus this drawing relates to*/
    public void setNuc(Nucleus n){nuc=n;}
    public Nucleus nucleus(){return nuc;}    
    
    public float moveQ(){return moveQ;}
    public Vector<String> subtitle(){ 
        Vector<String> out=new Vector<String>();
        if(subtitle==null) return out;
        for(int i=0;i<subtitle.size();i++){
            out.add(subtitle.elementAt(i));
        }
        return out;
    }
    public boolean supFlag(){return supFlag;}
    public boolean supPN(){return supPN;}
    
  
    public boolean supGammaLabels(){return supGammaLabels;}
    public boolean showAllGammaLabels(){return showAllGammaLabels;}
    public boolean toDrawDecayFedLevels(){return drawDecayFedLevels;}
    
    public DrawingControl clone() { 
    	try { 
    		// call clone in Object.     		
    		return (DrawingControl) super.clone();    		
    	} 
    	catch (CloneNotSupportedException e) { 
    			System.out.println("Cloning not allowed."); 
    			return this; 
    			
    	}    	
    } 
}
