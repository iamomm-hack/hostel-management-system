package hotelmanagement.client;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Starts the Swing client.
 * Run from the project folder:  java -cp "out;lib/*" hotelmanagement.client.HotelClient
 * To use a server on another computer:  ... hotelmanagement.client.HotelClient <server-ip>
 */
public class HotelClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // the default Swing look is fine too
        }
        UITheme.init();

        try {
            RemoteServices.connect(host);
        } catch (Exception e) {
            System.err.println("Could not connect to the RMI server: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the hotel server on '" + host + "'.\n"
                    + "Please start the server first and then open the client again.",
                    "Server not available", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
