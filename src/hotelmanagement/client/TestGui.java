package hotelmanagement.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Standalone runner to test the GUI client screens.
 */
public class TestGui {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UITheme.init();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
