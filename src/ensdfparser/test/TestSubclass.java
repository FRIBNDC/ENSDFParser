package ensdfparser.test;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import ensdfparser.ui.CustomFrame;

public class TestSubclass extends CustomFrame {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TestSubclass() {
        // Add subclass mouse listener
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Subclass: Mouse clicked - Button: " + e.getButton());
                
                // Ctrl+Right click won't reach here because parent consumed it
                // Only other clicks will reach here
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TestSubclass frame = new TestSubclass();
            frame.setTitle("Subclass Test");
            frame.setSize(500, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            frame.add(new JLabel("Test Ctrl+Right click vs normal clicks"));
            frame.setVisible(true);
        });
    }
}