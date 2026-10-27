package ensdfparser.ui;

import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;


import ensdfparser.base.BaseControl;
import ensdfparser.base.BaseRun;
import ensdfparser.base.BaseSetup;

public class BrowseButton <Run extends BaseRun<? extends JTextComponent>, Setup extends BaseSetup, Control extends BaseControl> extends JButton{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Run run=null;
	Class<Setup> setupClass=null;
	Class<Control> controlClass=null;
	JTextField pathField=null;

	private String lastPath="";
	
	public BrowseButton() {
		super("Browse");
	}
	
	public BrowseButton(Run run, Class<Setup> setupClass, Class<Control> controlClass, JTextField pathField) {
		super("Browse");
		
		try {			
			this.run=run;
			this.setupClass=setupClass;
			this.controlClass=controlClass;
			this.pathField=pathField;
			
			/*
			pathField.addMouseListener(new MouseAdapter(){
	        	@Override
	        	public void mouseClicked(MouseEvent arg0) {
	        		textFieldMouseClicked(arg0);
	        	}
			});
			*/
			
			addMouseListener(new MouseAdapter() {
	        	@Override
	        	public void mouseClicked(MouseEvent arg0) {
	        		thisButtonMouseClicked(arg0);
	        	}
	        });
		}catch(Exception e) {
			e.printStackTrace();
		}

		
	}
	
	private void thisButtonMouseClicked(MouseEvent evt) {
		if(evt.getModifiers()==InputEvent.BUTTON1_MASK){//left-button, BUTTON2-middle, BUTTON3-right
			loadFileBrowser();
		}else if(evt.getModifiers()==InputEvent.BUTTON3_MASK){
			final JPopupMenu popupMenu=new JPopupMenu();
			//popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
			//popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
            popupMenu.setForeground(SystemColor.windowText);
            popupMenu.setBackground(UIManager.getColor("ArrowButton.background"));

            
			 JMenuItem useCurrentFolder=new JMenuItem("Use Current Folder");
			 useCurrentFolder.setToolTipText("set output path to be the current working folder where the code is started");
			 useCurrentFolder.addActionListener(new ActionListener(){
				 //item.addMouseListener(new MouseAdapter(){
					 //public void mouseClicked(MouseEvent evt){
					 public void actionPerformed(ActionEvent evt){
						 try {
							 lastPath=pathField.getText();
							 setOutpathAsCurrentActionPerformed(evt); 
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 }
				 });

			 popupMenu.add(useCurrentFolder);
			 
			 popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
    }
	
	
	@SuppressWarnings("unused")
	private void textFieldMouseClicked(MouseEvent evt) {
		if(evt.getModifiers()==InputEvent.BUTTON1_MASK){//left-button, BUTTON2-middle, BUTTON3-right
			//do nothing
		}else if(evt.getModifiers()==InputEvent.BUTTON3_MASK){
			final JPopupMenu popupMenu=new JPopupMenu();
			//popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
			//popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
            popupMenu.setForeground(SystemColor.windowText);
            popupMenu.setBackground(UIManager.getColor("ArrowButton.background"));

            
			 JMenuItem useCurrentFolder=new JMenuItem("Restore last path");
			 useCurrentFolder.setToolTipText("set output path to be the previous path");
			 useCurrentFolder.addActionListener(new ActionListener(){
				 //item.addMouseListener(new MouseAdapter(){
					 //public void mouseClicked(MouseEvent evt){
					 public void actionPerformed(ActionEvent evt){
						 try {
							 String currPath=pathField.getText();
							 if(lastPath.isEmpty())
								 lastPath=(String)getSetupField("outdir");
							 
							 if(!currPath.equals(lastPath)) {
								 setOutpath(lastPath);
							 }

 
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 }
				 });

			 popupMenu.add(useCurrentFolder);
			 
			 popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
    }
	private void loadFileBrowser() {
        try{
            
            //JFileChooser fc=new JFileChooser();
            JFileChooser fc;
            try {
                fc = new JFileChooser(".");            
             }catch (Exception e) {
                fc = new JFileChooser(".", new RestrictedFileSystemView());
             }

            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setApproveButtonText("Select");
            fc.setApproveButtonToolTipText("Select this directory");
            
            String outdir=(String)getSetupField("outdir");
            if(outdir!=null)
                fc.setCurrentDirectory(new File(outdir));
            
            int ret=fc.showOpenDialog(this);
            if(ret==JFileChooser.APPROVE_OPTION){
                //Setup.outdir=fc.getSelectedFile().toString();
            	
            	File file = fc.getSelectedFile();
            	
            	outdir=file.getAbsolutePath();
            	setSetupField("outdir",outdir);
            	
            	System.out.println();
            	
            	invokeSetupMethod("save");
            	//setupClass.getMethod("save").invoke(null);

            	if(outdir.equals(pathField.getText()))
            		setControlField("isNewOutpath",false);	            		
            	else
            		setControlField("isNewOutpath",true);
            
                pathField.setText(outdir);
            }
            
           
        }catch(Exception e){
            //e.printStackTrace();
           	String message="Error when selecting output path!";
           	JOptionPane.showMessageDialog(this,message);
           	run.printMessage(message);           	                         
        }
	  
	}
	   
    protected void setOutpathAsCurrentActionPerformed(ActionEvent arg0) {
    	setOutpath(System.getProperty("user.dir"));		
	}
    
    protected void setOutpath(String outdir) {
        try{

        	setSetupField("outdir",outdir);

        	//System.out.println("1###"+Setup.outdir);
        	
        	run.updateRedirectOutput();
        	
            System.out.println();
            
            invokeSetupMethod("save");
            //setupClass.getMethod("save").invoke(null);
               
            //System.out.println("2###"+Setup.outdir);
            
            pathField.setText(outdir);

        }catch(Exception e){
            e.printStackTrace();
           	String message="Error when setting output path!";
           	JOptionPane.showMessageDialog(this,message);
           	run.printMessage(message);           	                         
        }
		
	}
    
    private void invokeSetupMethod(String methodName) {
    	try {
        	setupClass.getMethod(methodName).invoke(null);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
	private void setSetupField(String fieldName,Object value) {		  
		try {		        
			java.lang.reflect.Field field = setupClass.getField(fieldName);		        
			field.setAccessible(true);		       	        
			field.set(null, value);		  
		}catch(Exception e) {			   
			e.printStackTrace();		  
		}		
	}
	   
	   private void setControlField(String fieldName,Object value) {
		   try {
		        java.lang.reflect.Field field = controlClass.getField(fieldName);
		        field.setAccessible(true);
		        field.set(null, value);
		   }catch(Exception e) {
			   e.printStackTrace();
		   }
	   }
	   
	   private Object getSetupField(String fieldName) {
		   try {
		        java.lang.reflect.Field field = setupClass.getField(fieldName);
		        field.setAccessible(true);
		        return field.get(null);
		   }catch(Exception e) {
			   e.printStackTrace();
		   }
		   return null;
	   }
	   
	   @SuppressWarnings("unused")
	private Object getControlField(String fieldName) {
		   try {
		        java.lang.reflect.Field field = controlClass.getField(fieldName);
		        field.setAccessible(true);
		        return field.get(null);
		   }catch(Exception e) {
			   e.printStackTrace();
		   }
		   return null;
	   }
}
