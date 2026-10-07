package hotelmanagement.client;

import java.awt.Component;
import java.awt.Font;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Bill;
import hotelmanagement.model.ServiceUsage;
import hotelmanagement.remote.HotelException;

/** Helper methods shared by all client screens. */
public class ClientUtil {

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ClientUtil() {
        // only static methods
    }

    /** Shows a user-friendly message for any exception. Stack traces are never shown to the user. */
    public static void showError(Component parent, Exception e) {
        String message;
        if (e instanceof HotelException || e instanceof IllegalArgumentException) {
            // business error from the server, or invalid input found on the client
            message = e.getMessage();
        } else if (e instanceof RemoteException) {
            System.err.println("RMI error: " + e.getMessage());
            message = "Cannot reach the hotel server.\nPlease check that the server is running and try again.";
        } else {
            e.printStackTrace();
            message = "Something went wrong. Please try again.";
        }
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int answer = JOptionPane.showConfirmDialog(parent, message, "Please confirm", JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    /** Parses a date typed as yyyy-MM-dd. */
    public static LocalDate parseDate(String text, String fieldName) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " must be a date in the form YYYY-MM-DD.");
        }
    }

    public static int parseInt(String text, String fieldName) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    public static double parseDouble(String text, String fieldName) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }

    public static String money(double amount) {
        return String.format("Rs. %,.2f", amount);
    }

    /** A table model whose cells cannot be edited by the user. */
    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    /** The bill as plain text lines: Room Charges + Service Charges + GST = Final Amount. */
    public static String formatBill(Bill bill) {
        String line = "------------------------------------------------\n";
        StringBuilder text = new StringBuilder();
        text.append("                   HOTEL BILL\n");
        text.append(line);
        text.append("Booking ID : ").append(bill.getBookingId()).append("\n");
        text.append("Customer   : ").append(bill.getCustomerName()).append("\n");
        text.append("Room       : ").append(bill.getRoomNumber()).append(" (").append(bill.getRoomType()).append(")\n");
        text.append("Stay       : ").append(bill.getCheckIn()).append(" to ").append(bill.getCheckOut())
                .append(" (").append(bill.getNights()).append(" night(s))\n");
        text.append(line);
        text.append(billRow("Room: " + bill.getNights() + " x " + money(bill.getPricePerNight()), bill.getRoomCharges()));
        for (ServiceUsage usage : bill.getServices()) {
            text.append(billRow("  " + usage.getServiceName() + " x" + usage.getQuantity(), usage.getAmount()));
        }
        text.append(billRow("Service Charges", bill.getServiceCharges()));
        text.append(billRow("GST (" + (int) bill.getGstPercent() + "%)", bill.getGstAmount()));
        text.append(line);
        text.append(billRow("FINAL AMOUNT", bill.getTotalAmount()));
        text.append(line);
        if (bill.isPaid()) {
            text.append("Status     : PAID by ").append(bill.getPaymentMethod()).append("\n");
        } else {
            text.append("Status     : NOT PAID YET (bill so far)\n");
        }
        return text.toString();
    }

    private static String billRow(String label, double amount) {
        return String.format("%-30s %17s%n", label, money(amount));
    }

    /** A read-only text area with a fixed-width font, used to display a bill. */
    public static JScrollPane billArea(Bill bill) {
        JTextArea area = new JTextArea(formatBill(bill));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        return new JScrollPane(area);
    }

    public static void showBill(Component parent, Bill bill) {
        JOptionPane.showMessageDialog(parent, billArea(bill), "Bill for booking #" + bill.getBookingId(),
                JOptionPane.PLAIN_MESSAGE);
    }
}
