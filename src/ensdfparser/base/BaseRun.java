package ensdfparser.base;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import ensdfparser.nds.util.Str;
import ensdfparser.ui.UITool;



abstract public class BaseRun <T extends JTextComponent> extends BaseApp{
    
	protected String logFilename="log.txt";
    
    protected T messenger;
    FileOutputStream outputStream;
    static protected final String newline = "\n";
    
    protected boolean redirectToMessenger=false;
    protected boolean redirectToFile=true;
    
    protected boolean hasRedirectFileMsg=false;
    
    protected String os;
    protected String dirSeparator="\\";//"\\" for Windows, "/" for Linux
    
    public BaseRun(){   
        //DO NOT ADD ANY CODE HERE
    }
    
    public BaseRun(T m){
      //DO NOT ADD ANY CODE HERE
    }
    
    public void init() {
    	init(null);
    }
    public void init(T m) {

        //find out what operating system is being used, and set the right filename for the script
        String temp=System.getProperty("os.name");
        
        if(temp.toLowerCase().contains("linux")) 
        	os="linux";
        else if(temp.toLowerCase().contains("mac")) 
        	os="mac";
        else if(temp.toLowerCase().contains("windows")) 
        	os="windows";
        else 
        	os="other";
        
        dirSeparator="\\";
        if(!os.equals("windows"))
        	dirSeparator="/";

        if(m!=null) {
        	setMessenger(m);//redirectOutput(file) will be called when calling clear() in setMessenger 

        }else if(redirectToFile) { 
            String filename=getLogFilePath();
            if(!filename.isEmpty()) {
                //redirectOutputToFile(filename);	
            }
        }  

    }

    public void setMessenger(T m){
        messenger=m;    

        try {

			clear(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if(redirectToMessenger)
        	redirectOutputToMessenger(true);

    }

    public void setMessenger(T m,boolean printTitle){
        messenger=m;    
               
        try {
			clear(printTitle);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
        
        if(redirectToMessenger)
        	redirectOutputToMessenger(true);
    }
    
    /*
     * redirect System.out.print to Messenger
     * if false, restore System.out.print to system output
     */
    public void redirectOutputToMessenger(boolean redirect){
    	try {
        	if(!redirect){
        		restoreSystemOut();
        		return;
        	}
        	
    		if(messenger instanceof JTextArea)
    			redirectOutputTextArea((JTextArea)messenger,redirect);
    		else if(messenger instanceof JTextPane)
    			redirectOutputTextPane((JTextPane)messenger,redirect);
    	}catch(Exception e) {
    		
    	}
    }
    
    private void redirectOutputTextArea(JTextArea messenger, boolean redirect){
    	if(!redirect){
    		restoreSystemOut();
    		return;
    	}
    	
    	hasRedirectFileMsg=false;
    	
        PrintStream ps=new PrintStream(new OutputStream(){
			@Override
			public void write(int b) throws IOException {
				messenger.append(String.valueOf((char)b));
				
			}
        });
        
        System.setOut(ps);
    }
    
    private void redirectOutputTextPane(JTextPane messenger, boolean redirect){
    	if(!redirect){
    		restoreSystemOut();
    		return;
    	}
    	
    	hasRedirectFileMsg=false;
    	
        PrintStream ps=new PrintStream(new OutputStream(){
			@Override
			public void write(int b) throws IOException {
				StyledDocument doc=messenger.getStyledDocument();
				
				try {
					doc.insertString(doc.getLength(),String.valueOf((char)b),doc.getStyle(StyleContext.DEFAULT_STYLE));
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
        });
        
        System.setOut(ps);
    }
    
    /*
     * redirect output to the default log file
     */
    public void redirectOutputToFile(boolean b) {
    	String defaultFilePath=getLogFilePath();
    	if(b && defaultFilePath.length()>0)
    		redirectOutputToFile(defaultFilePath);
    	else {
    		restoreSystemOut();
    	}
    }
    
    /*
     * redirect System.out.print to Messenger
     * if filename is empty, use default file
     */
    public void redirectOutputToFile(String filename) {
	
    	try {
        	String name=filename;
        	if(name.trim().length()==0)
        		name=getLogFilePath();

        	if(!hasRedirectFileMsg) {
        		printMessage(title());   	
        	}
        	
        	if(name.isEmpty()) {
        		restoreSystemOut();
        		return;
        	}
        	
        	if(!hasRedirectFileMsg) {
        		printMessage("See "+name+" for more messages!");
        		hasRedirectFileMsg=true;
        	}
        	
        	File f=new File(name);
        	if(!f.exists())
        		f.getParentFile().mkdirs();
        	
        	outputStream=new FileOutputStream(name);
    	    		
            PrintStream ps=new PrintStream(outputStream);       
            System.setOut(ps);
            
            System.out.println(title());
                    
            Date date=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("E MM/dd/yyyy 'at' hh:mm:ss a zzz");
            System.out.println("Generated at: "+sdf.format(date));
            System.out.println();
     
            redirectToFile=true;
            
    	}catch(FileNotFoundException e) {
			//e.printStackTrace();
			printMessage("Error: cannot create log file: "+getLogFilePath());
			printMessage("Please check the output path.");
			restoreSystemOut();
    	}

    }
    
    public void updateRedirectOutput() throws IOException {
    	if(redirectToMessenger)
    		updateRedirectOutputToMessenger();
    	else if(redirectToFile)
    		updateRedirectOutputToFile();
    	else {
    		restoreSystemOut();
    	}
    }
    
    private void updateRedirectOutputToFile() throws IOException {
        if(redirectToFile)
        	redirectOutputToFile(getLogFilePath());
        else {
        	restoreSystemOut();
        }
    }
    
    private void updateRedirectOutputToMessenger() throws IOException {
        if(redirectToMessenger)
        	redirectOutputToMessenger(true);
        else {
        	restoreSystemOut();
        }
    }
    
    public void redirectErrMsgToMessenger(JTextArea messenger, boolean redirect){
        if(!redirect){
        	restoreSystemOut();
        	return;
        }
        
        PrintStream ps=new PrintStream(new OutputStream(){
            @Override
            public void write(int b) throws IOException {
                messenger.append(String.valueOf((char)b));
                
            }
        });
        
        System.setErr(ps);
    }
    
    public void restoreSystemOut() {
		PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out));
		System.setOut(out);
		
		redirectToFile=false;
		hasRedirectFileMsg=false;
    }
    
    public void setRedirectOutputToFile(boolean b){
    	redirectToFile=b;
    }
    
    public void printMessage(String message){
    	try {

    		if(messenger instanceof JTextArea) {

    			printMessageTextArea((JTextArea) messenger,message);
    		}else if(messenger instanceof JTextPane) {
    			//System.out.print("");
    			printMessageTextPane((JTextPane) messenger,message);
    			//System.out.print("");
    		}else if(messenger==null)
    		    System.out.println(message);
    		
    	}catch(Exception e) {
    		
    	}
    }
    
    public void printMessage(String message,AttributeSet style){
    	try {
    		if(messenger instanceof JTextArea)
    			printMessageTextArea((JTextArea) messenger,message);
    		else if(messenger instanceof JTextPane)
    			printMessageTextPane((JTextPane) messenger,message,style);
    		else if(messenger==null)
                System.out.println(message);
    	}catch(Exception e) {
    		
    	}
	
    }
    
    public void printMessageAsIs(String message){
    	try {
    		if(messenger instanceof JTextArea)
    			printMessageAsIsTextArea((JTextArea) messenger,message);
    		else if(messenger instanceof JTextPane)
    			printMessageAsIsTextPane((JTextPane) messenger,message);
    		else if(messenger==null)
                System.out.println(message);
    	}catch(Exception e) {
    		
    	}
    }
    
    public void printMessageAsIs(String message, AttributeSet style){

    	try {
    		if(messenger instanceof JTextArea)
    			printMessageAsIsTextArea((JTextArea) messenger,message);
    		else if(messenger instanceof JTextPane)
    			printMessageAsIsTextPane((JTextPane) messenger,message,style);
    		else if(messenger==null)
                System.out.println(message);
    	}catch(Exception e) {
    		
    	}
    }
    
    //text area
    private void printMessageTextArea(JTextArea messenger, String message){

    	if(message==null)
    		return;
    	
    	int len=newline.length();
    	if(!message.contains(newline) || message.lastIndexOf(newline)<message.length()-len)
    		message+=newline;
    			
		
    	printMessageAsIsTextArea(messenger, message);	
    }
    
    private void printMessageAsIsTextArea(JTextArea messenger, String message){

    	if(message==null)
    		return;
    	  	
    	System.out.print(message);
    	   			
    	if(messenger!=null){	
    		   		
    		messenger.append(message);
    		messenger.setCaretPosition(messenger.getDocument().getLength());
    		
    		/*
    		try {
				wait(0);				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
    		
    		
    		try {
    			messenger.update(messenger.getGraphics());
				messenger.scrollRectToVisible(messenger.modelToView(messenger.getDocument().getLength()));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				System.out.println("Error");
				e.printStackTrace();
			}
    		

    		//THIS DOES NOT AUTOMATICALLY SCROLL TEXT 
    		//DefaultCaret caret = (DefaultCaret)messenger.getCaret();
    		//caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
    		//caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);    		
    		//messenger.setCaretPosition(messenger.getDocument().getLength());

    	}
    	
    	//System.out.flush();
    }
    
    //text pane
    public void printMessageTextPane(JTextPane messenger,String message){
    	Style style=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    	
        // Test if HTMLEditorKit is currently set
        boolean isUsingHtmlKit = messenger.getEditorKit() 
                                 instanceof javax.swing.text.html.HTMLEditorKit;

        // Increase font size only when HTMLEditorKit is active since it decreases the default size for non-HTML text
        if (isUsingHtmlKit) {
            StyleConstants.setFontSize(style, 14); 
        }
            	
    	printMessageTextPane(messenger, message,style);	

    }
    
    public void printMessageTextPane(JTextPane messenger,String message,AttributeSet style){

    	if(message==null)
    		return;
    	
    	if(!message.contains(newline) || message.indexOf(newline)<message.length()-1)
    		message+=newline;
		
    	printMessageAsIsTextPane(messenger,message,style);	
    }
       
    public void printMessageAsIsTextPane(JTextPane messenger, String message){
    	Style style=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    	
    	
        // Test if HTMLEditorKit is currently set
        boolean isUsingHtmlKit = messenger.getEditorKit() 
                                 instanceof javax.swing.text.html.HTMLEditorKit;
        
        //System.out.println("####"+message);
        // Increase font size only when HTMLEditorKit is active since it decreases the default size for non-HTML text
        if (isUsingHtmlKit) {
            StyleConstants.setFontSize(style, 14); 
        }
        
    	printMessageAsIsTextPane(messenger, message,style);
    }
    
    
    public void printMessageAsIsTextPane(JTextPane messenger,String message,AttributeSet style){

    	if(message==null)
    		return;
    	   
    	System.out.print(message);
    	
    	if(messenger!=null){	

			StyledDocument doc=messenger.getStyledDocument();
			try {
		        // Test if HTMLEditorKit is currently set
		        boolean isUsingHtmlKit = messenger.getEditorKit() 
		                                 instanceof javax.swing.text.html.HTMLEditorKit;
		        
		          // Test if message is HTML
	            boolean isHtml = Str.isHtmlContent(message);
	            
	            // Insert content appropriately
	            if (isHtml && isUsingHtmlKit) {
	                // Insert as HTML
	            	UITool.insertHTML(messenger, message, (SimpleAttributeSet) style);
	            } else {
	                // Insert as plain text with styling
	                doc.insertString(doc.getLength(), message, style);
	            }
	  
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

    		//messenger.setCaretPosition(messenger.getDocument().getLength());
			try {
				//otherwise, there will be an Exception:
				//"AWT-EventQueue-0" java.lang.NullPointerException
				wait(0);
				
				messenger.update(messenger.getGraphics().create());
			}catch(Exception e) {
				
			}
	        
    		
    		//try {
			//	messenger.scrollRectToVisible(messenger.modelToView(messenger.getDocument().getLength()));
			//} catch (BadLocationException e) {
			//	System.out.println("Error");
			//	e.printStackTrace();
			//}
    		

    		//THIS DOES NOT AUTOMATICALLY SCROLL TEXT 
    		DefaultCaret caret = (DefaultCaret)messenger.getCaret();
    		//caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
    		//caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);    
    		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    		//messenger.setCaretPosition(messenger.getDocument().getLength());

    	}

    	System.out.flush();

    }
    
    
    public void clear() throws Exception{
    	clear(true);
    }
    
    public void clear(boolean printTitle) throws Exception{

        if(messenger!=null){
        	try {
                messenger.setText("");
        		messenger.setCaretPosition(messenger.getDocument().getLength());
        		
        		/*
        		try {
            		wait(0);
        		}catch(Exception e) {}
                */
        		
        		messenger.update(messenger.getGraphics());
        	}catch(Exception e) {
        		
        	}

        }

        if(outputStream!=null)
        	outputStream.close();

        String filename=getLogFilePath();
        if(redirectToFile && !filename.isEmpty()) {
        	redirectOutputToFile(filename);
        }else {
        	redirectOutputToFile(false);
        	
        	if(printTitle){
            	printMessage(title());
        	}
        }

    }
    
    
    public void clear0() throws Exception{
    	clear0(true);
    }
    
    public void clear0(boolean printTitle) throws Exception{

        if(messenger!=null){
        	try {
                messenger.setText("");
        		messenger.setCaretPosition(messenger.getDocument().getLength());
        		
        		/*
        		try {
            		wait(0);
        		}catch(Exception e) {}
                */
        		
        		messenger.update(messenger.getGraphics());
        	}catch(Exception e) {
        		
        	}

        }

        if(outputStream!=null)
        	outputStream.close();

        String filename=getLogFilePath();
        if(redirectToFile && !filename.isEmpty()) {
        	//redirectOutputToFile(filename);
        }else {
        	redirectOutputToFile(false);
        	
        	if(printTitle){
            	printMessage(title());
        	}
        }

    }
    
    public boolean isRedirectToFile() {return redirectToFile;}
    
    public String OS(){return os;}
    
    public String dirSeparator(){return dirSeparator;}
    
    public boolean isWindows(){
        if(os.equals("windows"))
        	return true;
        
        return false;
    }
    
    public boolean isLinux(){
    	String name=System.getProperty("os.name").toLowerCase();
        if(name.contains("linux"))
        	return true;
        
        return false;
    }
    
    public boolean isMacOS(){
    	String name=System.getProperty("os.name").toLowerCase();
        if(name.contains("mac"))
        	return true;
        
        return false;
    }
    
    public String date(){
        Date date=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("E MM/dd/yyyy 'at' hh:mm:ss a zzz");
        return sdf.format(date);
    }
     
    //abstract methods   
    public abstract String getLogFilePath();

}
