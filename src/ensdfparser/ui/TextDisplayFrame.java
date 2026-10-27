package ensdfparser.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class TextDisplayFrame extends JFrame{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JPanel panel;
    private JScrollPane scrollPane;
    private JTextPane textPane;
    String result="";
    
    private int height=560,width=740;
    
    public TextDisplayFrame(String text,int width,int height) {

    	this.result=text;
    	this.width=width;
    	this.height=height;
    	
        createResultPanel(width,height);
    }
    public TextDisplayFrame(String text) {
    	this(text, 740, 560);
    }
    protected void createResultPanel(int width,int height) {
        final Container cp=getContentPane();
        
        cp.setLayout(new BorderLayout());
        setPreferredSize(new Dimension(width,height));
        
        
        panel=new JPanel();
        panel.setPreferredSize(new Dimension(width,height));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new BorderLayout(0, 0));
        
        textPane=new JTextPane();
        textPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                          Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException | URISyntaxException e1) {
                          e1.printStackTrace();
                        }                     
                    }
                }
            }
        });

        textPane.setEditable(false);
        textPane.setEditorKit(JTextPane.createEditorKitForContentType("text/html"));
        textPane.setMargin(new Insets(1, 1, 1, 1));
        //textPane.setBorder(BorderFactory.createEmptyBorder());
        //textPane.setSize(new Dimension(300, 200));

        //DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        //caret.setUpdatePolicy(DefaultCaret.OUT_TOP);
        //caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        //caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        
        //PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        //System.setOut(printStream);
        scrollPane = new JScrollPane();
        //scrollPane.setViewportBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "message", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        //scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED, null, null));
        scrollPane.setPreferredSize(new Dimension(width,height));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.add(textPane);
        scrollPane.setViewportView(textPane);
        scrollPane.setVisible(true);
      
        StyledDocument doc=textPane.getStyledDocument();
        
        SimpleAttributeSet style=new SimpleAttributeSet(); 
        StyleConstants.setFontFamily(style,Font.MONOSPACED);
        //StyleConstants.setForeground(style, new Color(65,105,225));
        //StyleConstants.setBold(style, true);
        StyleConstants.setFontSize(style, 13);
        //int fontSize=StyleConstants.getFontSize(style);

        
        try {
            doc.insertString(doc.getLength(),result,style);
        }catch(BadLocationException ex) {
            ex.printStackTrace();
        }catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
        //THIS DOES NOT AUTOMATICALLY SCROLL TEXT 
        //DefaultCaret caret = (DefaultCaret)textPane.getCaret();
        //caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
        //caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);    
        //caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        textPane.setCaretPosition(0);
  
        panel.add(scrollPane);     
        cp.add(panel,BorderLayout.NORTH);
        
        //frame.setUndecorated(true);//remove title bar
        
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setUndecorated(true);
        
        pack();
        setResizable(false);
        setVisible(true);

    
    }
    
    public void clearResults() {
        try {
            if(textPane!=null){
                textPane.setText("");
                textPane.setCaretPosition(textPane.getDocument().getLength());
                textPane.update(textPane.getGraphics());
            }
        }catch(Exception e) {
            
        }
    }
    public void updateResult(String result) {
        try {
            clearResults();
            
            StyledDocument doc=textPane.getStyledDocument();
            
            SimpleAttributeSet style=new SimpleAttributeSet(); 
            StyleConstants.setFontFamily(style,Font.MONOSPACED);
            //StyleConstants.setForeground(style, new Color(65,105,225));
            //StyleConstants.setBold(style, true);
            StyleConstants.setFontSize(style, 13);
            //int fontSize=StyleConstants.getFontSize(style);

            
            try {
                
                doc.insertString(doc.getLength(),result,style);
            }catch(BadLocationException ex) {
                ex.printStackTrace();
            }catch (Exception ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
            
            //THIS DOES NOT AUTOMATICALLY SCROLL TEXT 
            //DefaultCaret caret = (DefaultCaret)textPane.getCaret();
            //caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
            //caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);    
            //caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            textPane.setCaretPosition(0);  
        }catch(Exception e) {
            
        }
    }
    

    public int getWidth() {return width;}
    public int getHeight() {return height;}
    
    public void setWidth(int w) {width=w;}
    public void setHeight(int h) {height=h;}
    
	public static <T extends Component> TextDisplayFrame showTextFrame(T com, String title,String text) {
		return showTextFrame(com,title,text,600,795);
	}
	public static <T extends Component> TextDisplayFrame showTextFrame(T com,String title,String text,int width,int height) {
        try {

        	TextDisplayFrame textFrame=null;
        	
            java.awt.Frame[] frames=java.awt.Frame.getFrames();
            for(int i=0;i<frames.length;i++) {
                if(frames[i].getTitle().equals(title))
                    textFrame=(TextDisplayFrame)frames[i];
                
                //System.out.println(frames[i].getName()+"  "+frames[i].getTitle());
            }
            
            if(textFrame==null) {
         
                textFrame=new TextDisplayFrame(text,width,height);
                textFrame.setTitle(title);
                textFrame.setLocation(com.getLocationOnScreen().x+com.getWidth()+2,com.getLocationOnScreen().y);
                textFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

                textFrame.pack(); 
                textFrame.setVisible(true); 
                textFrame.setResizable(false);
  
            }else {
                if ((textFrame.getExtendedState() & Frame.ICONIFIED) != 0) {
                	textFrame.setExtendedState(Frame.NORMAL);
                }
                textFrame.updateResult(text);
                if(!textFrame.isVisible()) {
                    textFrame.setLocation(com.getLocationOnScreen().x+com.getWidth()+2,com.getLocationOnScreen().y);
                    textFrame.setVisible(true); 
                }
            }
            
            textFrame.requestFocus();
            
            return textFrame;
            
        }catch(Exception e) {
        	e.printStackTrace();
	    	JOptionPane.showMessageDialog(null, "Error when openning window of\n <"+title+">.");    
        }

        return null;
    }
}
