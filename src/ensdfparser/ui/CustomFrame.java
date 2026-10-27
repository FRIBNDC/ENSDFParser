package ensdfparser.ui;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.*;
import java.awt.event.*;

public class CustomFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private String appName = "app Name";
    private String version = "version";
    private String developer = "Jun Chen";
    private String institution = "FRIB/MSU";
      
    public CustomFrame() {
        init();
    }

    
    //the pop-up menu of subclass will flash on and off
    private void init() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check modifiers DIRECTLY from the mouse event
                boolean ctrlPressed = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
                boolean shiftPressed = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
                
                if (ctrlPressed && shiftPressed && 
                    SwingUtilities.isRightMouseButton(e)) {
                    
                    showVersionDialog();
                    
                    // Close any popup after a short delay
                    Timer timer = new Timer(100, ev -> {
                        closeAllPopups();
                        ((Timer)ev.getSource()).stop();
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        });
    }
    
    private void closeAllPopups() {
        // Close any JPopupMenu that's showing
        for (Window window : Window.getWindows()) {
            if (window instanceof JWindow) {
                JWindow jwindow = (JWindow) window;
                if (jwindow.getContentPane().getComponentCount() > 0) {
                    Component comp = jwindow.getContentPane().getComponent(0);
                    if (comp instanceof JPopupMenu) {
                        ((JPopupMenu) comp).setVisible(false);
                        window.dispose();
                    }
                }
            }
        }
    }
    
    @SuppressWarnings("unused")
	private void showVersionPopupMenu(int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item = new JMenuItem("Version Info");
        item.addActionListener(e -> showVersionDialog());
        popup.add(item);
        popup.show(this, x, y);
    }
    
    private void showVersionDialog() {
        String info = String.join("\n",
            "Application: " + appName,
            "Version: " + version,
            "Developer: " + developer,
            "Institution: " + institution
        );
        
        // PLAIN_MESSAGE = no icon
        JOptionPane.showMessageDialog(this, info,
            "Version Information", JOptionPane.PLAIN_MESSAGE);
    }
    
    // Setters
    public void setNameAndVersion(String appName,String version) { this.appName = appName; this.version=version;}
    
    public void setAppName(String appName) { this.appName = appName; }
    public void setVersion(String version) { this.version = version; }
    public void setDeveloper(String developer) { this.developer = developer; }
    public void setInstitution(String institution) { this.institution = institution; }
    
    // Getters
    public String getAppName() { return appName; }
    public String getVersion() { return version; }
    public String getDeveloper() { return developer; }
    public String getInstitution() { return institution; }
    
    // Usage example
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            //launch master control panel
            try{
           	
            	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            	//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            	//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }catch(Exception e1){
                try{
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    	
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                        else{
                        	UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                        }
                    }
                    
                }catch(Exception e2){
                	
                }        	
            }
            
            CustomFrame frame = new CustomFrame();
            frame.setTitle("ENSDF Parser Pro");
            
            // Set version info using setters
            frame.setAppName("ENSDF Parser Pro Advanced");
            frame.setVersion("3.0.0");

            
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            
            // Add test label
            JLabel label = new JLabel(
                "<html><center><b>Test Ctrl+Right Click</b><br><br>" +
                "1. Hold Ctrl + Right click ANYWHERE in this window<br>" +
                "2. Try normal right click (no Ctrl)<br>" +
                "3. Try left clicks<br><br>" +
                "Only Ctrl+Right click shows version info</center></html>",
                SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            frame.add(label, BorderLayout.CENTER);
            
            // Add a test panel with button
            JPanel panel = new JPanel();
            JButton testButton = new JButton("Test Button");
            panel.add(testButton);
            frame.add(panel, BorderLayout.SOUTH);
            
            frame.setVisible(true);
        });
    }
}