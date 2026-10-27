package ensdfparser.ui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class UITool {

	public static void insertHTML(JTextPane textPane, String htmlText,SimpleAttributeSet textStyle) {
        StyledDocument doc=textPane.getStyledDocument();
        
        if(textStyle==null) {
            textStyle=new SimpleAttributeSet(); 
            StyleConstants.setFontFamily(textStyle,Font.MONOSPACED);
            //StyleConstants.setForeground(style, new Color(65,105,225));
            //StyleConstants.setBold(style, true);
            StyleConstants.setFontSize(textStyle, 13);
            //int fontSize=StyleConstants.getFontSize(style);
            
            /*
            SimpleAttributeSet refStyle=new SimpleAttributeSet(); 
            StyleConstants.setFontFamily(refStyle,Font.MONOSPACED);
            //StyleConstants.setForeground(refStyle, new Color(65,105,225));
            //StyleConstants.setBold(refStyle, true);
            StyleConstants.setFontSize(refStyle, 12);
            */
        }

        
        try {
            //doc.insertString(doc.getLength(),text,style);//regular text
            //textPane.setEditorKit(JTextPane.createEditorKitForContentType("text/html"));
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
            
            HTMLEditorKit editorKit = (HTMLEditorKit)textPane.getEditorKit();
            HTMLDocument htmldoc = (HTMLDocument)textPane.getDocument();

            //htmlText="<span style=\"font-size:11pt\">"+htmlText+"</span>\n\n\n";
            htmlText="<span style=\"font-family: monospace; white-space: nowrap;\">"+htmlText+"</span>\n\n\n";//textStyle not used: TO DO
            editorKit.insertHTML(htmldoc, htmldoc.getLength(), htmlText, 0, 0, null);
            //System.out.println(htmlText);
            
            doc.insertString(doc.getLength(),"\n",textStyle);
            
            //textPane.setText(text+refText);//the whole text have to be in HTML format
        }catch(BadLocationException ex) {
            ex.printStackTrace();
        }catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
	}
	
	   /**
     * Shows an existing frame (restoring if minimized), or creates a new one.
     * 
     * @param title   The frame title (used to find existing frames)
     * @param type    The frame class
     * @param factory A supplier that creates a new instance (e.g., () -> new MyFrame())
     * @param parent  The parent frame (used for positioning)
     * @return The frame (existing or newly created)
     */
    public static <T extends JFrame> T showFrame(
            String title,
            Class<T> type,
            Supplier<T> factory,
            JFrame parent,
            boolean resizable) {
        
        // Lookup existing frame by title and type
        T frame = null;
        for (Frame f : Frame.getFrames()) {
            if (f!=null && title.equals(f.getTitle()) && type.isInstance(f)) {
                frame = type.cast(f);
                break;
            }
        }
        
        // Create or restore
        if (frame == null) {
            frame = factory.get();
            frame.setTitle(title);
         
            // Handle null parent safely
            if (parent != null) {
                frame.setLocation(parent.getX() + parent.getWidth() + 5, parent.getY() + 5);
            } else {
                frame.setLocationRelativeTo(null);
            }
            
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setResizable(resizable);
        } else {
            if ((frame.getExtendedState() & Frame.ICONIFIED) != 0) {
                frame.setExtendedState(Frame.NORMAL);
            }
        }
        
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
        return frame;
    }
    
    public static <T extends JFrame> T showFrame(
            String title,
            Class<T> type,
            Supplier<T> factory,
            JFrame parent) {
    	return showFrame(title,type,factory,parent,false);   	
    }
}
