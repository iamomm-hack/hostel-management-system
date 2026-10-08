package hotelmanagement.client;

import java.awt.Component;
import java.awt.Font;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
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
        UITheme.styleTable(table);
        return table;
    }

    /** The bill formatted with clean visual separators and alignment. */
    public static String formatBill(Bill bill) {
        String div = "====================================================\n";
        String line = "----------------------------------------------------\n";
        StringBuilder text = new StringBuilder();
        text.append("\n");
        text.append("                   INVOICE & BILL DETAILS           \n");
        text.append("                 Hotel Management System            \n");
        text.append(div);
        text.append(String.format("  Booking ID : %-16s Status: %s%n",
                bill.getBookingId(), bill.isPaid() ? "[PAID]" : "[PENDING]"));
        text.append(String.format("  Customer   : %s%n", bill.getCustomerName()));
        text.append(String.format("  Room       : %s (%s)%n", bill.getRoomNumber(), bill.getRoomType()));
        text.append(String.format("  Stay Period: %s to %s (%d nights)%n",
                bill.getCheckIn(), bill.getCheckOut(), bill.getNights()));
        text.append(line);
        text.append(String.format("  %-36s %12s%n", "DESCRIPTION", "AMOUNT"));
        text.append(line);
        text.append(billRow("Room (" + bill.getNights() + " nights @ " + money(bill.getPricePerNight()) + ")", bill.getRoomCharges()));
        for (ServiceUsage usage : bill.getServices()) {
            text.append(billRow(usage.getServiceName() + " (x" + usage.getQuantity() + ")", usage.getAmount()));
        }
        text.append(line);
        text.append(billRow("Subtotal (Services)", bill.getServiceCharges()));
        text.append(billRow("GST (" + (int) bill.getGstPercent() + "%)", bill.getGstAmount()));
        text.append(div);
        text.append(billRow("TOTAL PAYABLE", bill.getTotalAmount()));
        text.append(div);
        if (bill.isPaid()) {
            text.append(String.format("  Payment Method : %-15s Payment Status: COMPLETED%n", bill.getPaymentMethod()));
        } else {
            text.append("  Payment Status : UNPAID (Payment pending at check-out)\n");
        }
        text.append("\n");
        return text.toString();
    }

    private static String billRow(String label, double amount) {
        return String.format("  %-36s %12s%n", label, money(amount));
    }

    /** A read-only text area with a clean monospace font and border. */
    public static JScrollPane billArea(Bill bill) {
        JTextArea area = new JTextArea(formatBill(bill));
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBackground(UITheme.BG_APP);
        area.setForeground(UITheme.TEXT_PRIMARY);
        area.setCaretPosition(0);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(new UITheme.RoundedLineBorder(UITheme.BORDER, 8, 1));
        return sp;
    }

    public static void showBill(Component parent, Bill bill) {
        JOptionPane.showMessageDialog(parent, billArea(bill), "Invoice - Booking #" + bill.getBookingId(),
                JOptionPane.PLAIN_MESSAGE);
    }
}
