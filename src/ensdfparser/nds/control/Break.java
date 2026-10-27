package ensdfparser.nds.control;

import java.util.Vector;

/** Note: a break point is the first item in a new table panel.
 *        panelColumns is the columns of the new panel
 */
public class Break implements Cloneable{
    public Vector<String>sups;
    public Vector<String>restore;
    public Vector<String>panelColumns;
    public String pos;
    public boolean isNewPage=false;
    public float gammaPos;//the use of gammaPos is not need in auto mode. Now an virtual level energy is assigned to each gamma and each of its comment lines
                          //when the gammas are sorted in order of level energies.
                          //It is not needed when gammas are in order of gamma energies.

    public void setPanelColumns(Vector<String> v){
    	if(panelColumns!=null)
    		panelColumns.clear();
    	else
    		panelColumns=new Vector<String>();
    	
    	panelColumns.addAll(v);
    }
 
    public Break clone() {
    	try{
        	Break newBreak=(Break) super.clone();
        	
        	if(sups!=null){
        		newBreak.sups=new Vector<String>();
        		newBreak.sups.addAll(sups);
        	}
        	
        	if(restore!=null){
        		newBreak.restore=new Vector<String>();
        		newBreak.restore.addAll(restore);
        	}
        	
        	if(panelColumns!=null){
        		newBreak.panelColumns=new Vector<String>();
        		newBreak.panelColumns.addAll(panelColumns);
        	}
    	
        	return newBreak;
    	}    	
    	catch (CloneNotSupportedException e) { 
			System.out.println("Cloning not allowed."); 
			return this; 
			
	
    	} 

    }

}
