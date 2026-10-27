package ensdfparser.ui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import ensdfparser.nds.util.Str;

import java.awt.datatransfer.*;

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality 
 */
public class CustomTextPane extends JTextPane implements ActionListener{
   /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Clipboard clipboard;  
    private StringSelection stsel;
    private Style defaultStyle=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    private boolean lineWrap=false;
    
    private boolean copyEnabled=true;
    private boolean pasteEnabled=true;
    private boolean cutEnabled=true;
    private boolean deleteEnabled=true;
    
    //int mask=ActionEvent.CTRL_MASK;
    int mask=Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,mask,false);
    // Identifying the copy KeyStroke user can modify this
    // to copy on some other Key combination.
    
    KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,mask,false);
    // Identifying the Paste KeyStroke user can modify this
    //to copy on some other Key combination.

    KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X,mask,false);
    
    //KeyStroke delete= KeyStroke.getKeyStroke(' ');
    
    public CustomTextPane(){

	    //StyleConstants.setFontFamily(defaultStyle,Font.MONOSPACED);
	    //StyleConstants.setFontSize(defaultStyle, 13);
	    
       //this.registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);
       //this.registerKeyboardAction(this,"Paste",paste,JComponent.WHEN_FOCUSED);
       //this.registerKeyboardAction(this,"Cut",cut,JComponent.WHEN_FOCUSED);
       //this.registerKeyboardAction(this,"Delete",delete,JComponent.WHEN_FOCUSED);
    	//setEditorKit(JTextPane.createEditorKitForContentType("text/html"));
    	
        setCopyEnabled(true);
        setPasteEnabled(true);
        setCutEnabled(true);
        
       //new method to register a KeyStroke
       //getInputMap().put(copy,"Copy"); 
       //getActionMap().put("Copy",this);

       if(lineWrap)
    	   this.setEditorKit(new WrapEditorKit());
       
       //unregisterKeyboardAction(copy);
       
       clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
       
       
       this.addMouseListener(new MouseAdapter(){
           public void mouseClicked(MouseEvent e){
               makePopupMenu(e);
           }
       });
    }

    public CustomTextPane(boolean lineWrap){
    	this();
    	setLineWrap(lineWrap);
    }
    public void setCopyEnabled(boolean b) {
        copyEnabled=b;
    	if(b) {
    	    registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);
    	}else {
    	    unregisterKeyboardAction(copy);
    	}
    }
    
    public void setPasteEnabled(boolean b) {
        pasteEnabled=b;
        if(b) {
            //getInputMap().put(paste,"Paste"); 
            //getActionMap().put("Paste",(Action) this.getActionForKeyStroke(paste));
            registerKeyboardAction(this,"Paste",paste,JComponent.WHEN_FOCUSED);
        }else {
            unregisterKeyboardAction(paste);
        }
    }
    
    public void setCutEnabled(boolean b) {
        cutEnabled=b;
        if(b) {
            registerKeyboardAction(this,"Cut",cut,JComponent.WHEN_FOCUSED);
        }else {
            unregisterKeyboardAction(cut);
        }
    }
    public void setDeleteEnabled(boolean b) {
        deleteEnabled=b;
    }
    
    private void makePopupMenu(MouseEvent e) {
    	boolean toMake=copyEnabled||cutEnabled||pasteEnabled||deleteEnabled;
    	
        if(e.getButton()==3 && toMake){//right clicked
            JPopupMenu tablePopupMenu=new JPopupMenu();
             
            if(copyEnabled) {
                JMenuItem item1=new JMenuItem("Copy");
                item1.addActionListener(this);
                tablePopupMenu.add(item1);            	
            }

          
            if(pasteEnabled) {
                JMenuItem item2=new JMenuItem("Paste");
                item2.addActionListener(this);
                tablePopupMenu.add(item2);	
            }            
            

          
            if(cutEnabled) {
                JMenuItem item3=new JMenuItem("Cut");
                item3.addActionListener(this);
                tablePopupMenu.add(item3);	
            }            

          
            if(deleteEnabled) {
                JMenuItem item4=new JMenuItem("Delete");
                item4.addActionListener(this);
                tablePopupMenu.add(item4);
            }            

            tablePopupMenu.show(e.getComponent(),e.getX(), e.getY());

        }    
    }


   /**
    * This method is activated on the Keystrokes we are listening to
    * in this implementation. Here it listens for Copy and Paste ActionCommands.
    * Selections comprising non-adjacent cells result in invalid selection and
    * then copy action cannot be performed.
    * Paste is done by aligning the upper left corner of the selection with the
    * 1st element in the current selection of the JTable.
    */
    public void actionPerformed(ActionEvent e)
    {       
       if (e.getActionCommand().compareTo("Copy")==0 && copyEnabled){
          copyToClipboard(false);
       }
       
       if (e.getActionCommand().compareTo("Cut")==0 && cutEnabled){
          copyToClipboard(true);
       }

       if (e.getActionCommand().compareTo("Delete")==0 && deleteEnabled){
          deleteSelected();
       }      
       
       if (e.getActionCommand().compareTo("Paste")==0 && pasteEnabled){

          pasteFromClipboard();
       }
    }
    
    private void copyToClipboard(boolean isCut){
        
        String text=this.getSelectedText();
        
        if(text==null || text.isEmpty())
            return;
        
        stsel  = new StringSelection(text);
        clipboard.setContents(stsel,stsel);   
        if(isCut)
            deleteSelected();
    }
  
    private void pasteFromClipboard(){
        
        //System.out.println("Trying to Paste");
    
        try {
            String text= (String)(clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor));
            //System.out.println("String is:"+trstring);
            
            //System.out.println(getUI().getPreferredSize(this).width+"  "+getParent().getSize().width);
            String[] temp=text.split("[\n]");
            if(temp.length>0) {
            	String line="";
            	text="";
            	for(int i=0;i<temp.length;i++) {
            		line=Str.rtrim(temp[i]);

            		//if(i<temp.length-1)
            		//	line=Str.fixLineLength(line, 80);

            		text+=line+"\n";
            		/*
            		if(line.trim().length()>0)
            			text+=line+"\n";
            		else
            			text+=line;
            		*/
            	}
            }
            
            StyledDocument doc=this.getStyledDocument();
            
            deleteSelected();
            
            //System.out.println(this.getCaretPosition());
            //System.out.println(" doc.getLength()="+doc.getLength());
            
            //doc.insertString(doc.getLength(),text,defaultStyle);
            doc.insertString(getCaretPosition(),text,defaultStyle);
        }catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();              
        }  


   
    }

    /*
    private void pasteFromClipboard() { 
        
        int startRow=table.getSelectedRows()[0]; 
        int startCol=table.getSelectedColumns()[0];

        String pasteString = ""; 
        try { 
                pasteString = (String)(clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor)); 
        } catch (Exception e) { 
                JOptionPane.showMessageDialog(null, "Invalid Paste Type", "Invalid Paste Type", JOptionPane.ERROR_MESSAGE);
                return; 
        } 
        
        String[] lines = pasteString.split("\n"); 
        for (int i=0 ; i<lines.length; i++) { 
                String[] cells = lines[i].split("\t"); 
                for (int j=0 ; j<cells.length; j++) { 
                        if (table.getRowCount()>startRow+i && table.getColumnCount()>startCol+j) { 
                                table.setValueAt(cells[j], startRow+i, startCol+j); 
                        } 
                } 
        } 
    }  
    */  
  
    private void deleteSelected(){

        int start=this.getSelectionStart();
        int end=this.getSelectionEnd();
        if(start<0)
            return;
        
        if(start>end) {
           JOptionPane.showMessageDialog(null, "Invalid Selection",
                                         "Invalid Selection",
                                         JOptionPane.ERROR_MESSAGE);
           return;
        }
        
        
        StyledDocument doc=this.getStyledDocument();
        
        //System.out.println(" start="+start+" end="+end+"  doc length="+doc.getLength());
        
        try {
            if(start==end) {
                //doc.insertString(end, " ", defaultStyle);
                return;
            }
            
            //if(end==doc.getLength())
            //    end=end-1;
            
            doc.remove(start, end-start);
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

    }
    
    public void setLineWrap(boolean lineWrap) {

    	//if(this.lineWrap==lineWrap)
    	//	return;
    	
    	this.lineWrap=lineWrap;
    	//if(lineWrap)
    		setEditorKit(new WrapEditorKit());//also needed for no wrapping
    	
    	this.updateUI();
    	
    }
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
    	//System.out.println("linewrap="+lineWrap+" scrolllableTrack="+super.getScrollableTracksViewportWidth()+" $"+getUI().getPreferredSize(this).width+" $"+getParent().getSize().width);
        if(lineWrap)
        	return super.getScrollableTracksViewportWidth();//this is the key for wrapping, plus the use of WrapEditorKit
        else
        	return getUI().getPreferredSize(this).width<= getParent().getSize().width;//this is the key for no wrapping, plus the use of WrapEditorKit

    }
    
  
}

class WrapEditorKit extends StyledEditorKit {
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ViewFactory defaultFactory = new WrapColumnFactory();

	  public ViewFactory getViewFactory() {
	    return defaultFactory;
	  }
	}

	class WrapColumnFactory implements ViewFactory {
	  public View create(Element elem) {
	    String kind = elem.getName();
	    if (kind != null) {
	      if (kind.equals(AbstractDocument.ContentElementName)) {
	        return new WrapLabelView(elem);
	      } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
	        return new ParagraphView(elem);
	      } else if (kind.equals(AbstractDocument.SectionElementName)) {
	        return new BoxView(elem, View.Y_AXIS);
	      } else if (kind.equals(StyleConstants.ComponentElementName)) {
	        return new ComponentView(elem);
	      } else if (kind.equals(StyleConstants.IconElementName)) {
	        return new IconView(elem);
	      }
	    }
	    return new LabelView(elem);
	  }
	}

	class WrapLabelView extends LabelView {
	  public WrapLabelView(Element elem) {
	    super(elem);
	  }

	  public float getMinimumSpan(int axis) {
	    switch (axis) {
	    case View.X_AXIS://Jun comment 10/9/2024: is this for no wrapping, instead of wrapping?
	      return 0;
	    case View.Y_AXIS:
	      return super.getMinimumSpan(axis);
	    default:
	      throw new IllegalArgumentException("Invalid axis: " + axis);
	    }
	  }
	}