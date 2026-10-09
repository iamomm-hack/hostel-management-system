package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Bill;
import hotelmanagement.model.Booking;
import hotelmanagement.model.BookingStatus;
import hotelmanagement.model.ExtraService;
import hotelmanagement.model.PaymentMethod;
import hotelmanagement.model.Role;

/**
 * Modern management interface for bookings and active guest stays.
 */
public class BookingsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final Role role;
    private final int customerId;

    private final DefaultTableModel model = ClientUtil.readOnlyModel(
            "Booking ID", "Customer", "Room", "Check-in", "Check-out", "Guests", "Status", "Booked On");
    private final JTable table = ClientUtil.createTable(model);
    private final JLabel countLabel = new JLabel("0 bookings");
    private final JComboBox<String> filterBox = new JComboBox<>(new String[] {
            "All Bookings", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"
    });

    private List<Booking> allBookings = new ArrayList<>();

    public BookingsPanel(Role role, int customerId) {
        this.role = role;
        this.customerId = customerId;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel title = new JLabel(role == Role.CUSTOMER ? "My Reservations" : "Bookings & Check-In");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);

        countLabel.setFont(UITheme.FONT_BADGE);
        countLabel.setForeground(UITheme.TEXT_MUTED);
        countLabel.setOpaque(true);
        countLabel.setBackground(UITheme.BG_SUBTLE);
        countLabel.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedLineBorder(UITheme.BORDER, 10, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        leftPanel.add(title);
        leftPanel.add(countLabel);

        if (role != Role.CUSTOMER) {
            UITheme.styleComboBox(filterBox);
            filterBox.addActionListener(e -> applyFilter());
            leftPanel.add(Box.createHorizontalStrut(10));
            leftPanel.add(new JLabel("Filter:"));
            leftPanel.add(filterBox);
        }

        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightToolbar.setOpaque(false);

        JButton refreshButton = UITheme.createGhostButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        rightToolbar.add(refreshButton);

        if (role == Role.RECEPTIONIST) {
            JButton checkInButton = UITheme.createSuccessButton("Check-In");
            JButton serviceButton = UITheme.createSecondaryButton("+ Add Service");
            JButton checkOutButton = UITheme.createPrimaryButton("Check-Out & Bill");

            checkInButton.addActionListener(e -> checkIn());
            serviceButton.addActionListener(e -> addService());
            checkOutButton.addActionListener(e -> checkOut());

            rightToolbar.add(checkInButton);
            rightToolbar.add(serviceButton);
            rightToolbar.add(checkOutButton);
        }

        if (role == Role.RECEPTIONIST || role == Role.CUSTOMER) {
            JButton cancelButton = UITheme.createDangerButton("Cancel Booking");
            cancelButton.addActionListener(e -> cancelBooking());
            rightToolbar.add(cancelButton);
        }

        JButton billButton = UITheme.createSecondaryButton("View Invoice");
        billButton.addActionListener(e -> viewBill());
        rightToolbar.add(billButton);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightToolbar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        JScrollPane scrollPane = UITheme.createScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        try {
            if (role == Role.CUSTOMER) {
                allBookings = RemoteServices.booking.getBookingsByCustomer(customerId);
            } else {
                allBookings = RemoteServices.booking.getAllBookings();
            }
            applyFilter();
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    private void applyFilter() {
        String filter = (String) filterBox.getSelectedItem();
        model.setRowCount(0);
        int count = 0;

        for (Booking b : allBookings) {
            if (filter == null || filter.equals("All Bookings") || b.getStatus().name().equals(filter)) {
                model.addRow(new Object[] {
                        b.getBookingId(),
                        b.getCustomerName(),
                        b.getRoomNumber(),
                        b.getCheckIn(),
                        b.getCheckOut(),
                        b.getNumberOfGuests(),
                        b.getStatus().name(),
                        b.getBookingDate().format(ClientUtil.DATE_TIME)
                });
                count++;
            }
        }
        countLabel.setText(count + " shown");
    }

    private Booking selectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ClientUtil.showInfo(this, "Please select a booking in the table first.");
            return null;
        }
        int bookingId = (int) model.getValueAt(row, 0);
        for (Booking b : allBookings) {
            if (b.getBookingId() == bookingId) {
                return b;
            }
        }
        return null;
    }

    private void cancelBooking() {
        Booking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        if (!ClientUtil.confirm(this, "Cancel booking #" + booking.getBookingId()
                + " for room " + booking.getRoomNumber() + "?")) {
            return;
        }
        try {
            RemoteServices.booking.cancelBooking(booking.getBookingId());
            ClientUtil.showInfo(this, "Booking #" + booking.getBookingId() + " was successfully cancelled.");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
        refreshData();
    }

    private void checkIn() {
        Booking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            ClientUtil.showInfo(this, "Guest is already checked in.");
            return;
        }
        try {
            RemoteServices.booking.checkIn(booking.getBookingId());
            ClientUtil.showInfo(this, booking.getCustomerName() + " has been checked in to Room "
                    + booking.getRoomNumber() + ".");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
        refreshData();
    }

    private void addService() {
        Booking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        try {
            JComboBox<ExtraService> serviceBox = new JComboBox<>();
            UITheme.styleComboBox(serviceBox);
            for (ExtraService service : RemoteServices.payment.getAllServices()) {
                serviceBox.addItem(service);
            }
            JTextField quantityField = UITheme.createTextField("1", 5);

            JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
            form.add(new JLabel("Service Name:"));
            form.add(serviceBox);
            form.add(new JLabel("Quantity:"));
            form.add(quantityField);

            int answer = JOptionPane.showConfirmDialog(this, form,
                    "Add Service to Booking #" + booking.getBookingId(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            ExtraService service = (ExtraService) serviceBox.getSelectedItem();
            int quantity = ClientUtil.parseInt(quantityField.getText(), "Quantity");
            RemoteServices.payment.addServiceUsage(booking.getBookingId(), service.getServiceId(), quantity);
            ClientUtil.showInfo(this, service.getServiceName() + " (x" + quantity + ") added to bill.");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    private void checkOut() {
        Booking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        try {
            Bill bill = RemoteServices.payment.getBill(booking.getBookingId());
            if (bill.isPaid()) {
                ClientUtil.showInfo(this, "This booking has already been settled and checked out.");
                return;
            }

            JComboBox<PaymentMethod> methodBox = new JComboBox<>(PaymentMethod.values());
            UITheme.styleComboBox(methodBox);

            JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            methodPanel.add(new JLabel("Select Settlement Method:"));
            methodPanel.add(methodBox);

            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.add(ClientUtil.billArea(bill), BorderLayout.CENTER);
            panel.add(methodPanel, BorderLayout.SOUTH);

            int answer = JOptionPane.showConfirmDialog(this, panel,
                    "Check-Out & Final Settlement - Booking #" + booking.getBookingId(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }

            PaymentMethod method = (PaymentMethod) methodBox.getSelectedItem();
            RemoteServices.booking.checkOut(booking.getBookingId(), method);
            ClientUtil.showBill(this, RemoteServices.payment.getBill(booking.getBookingId()));
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
        refreshData();
    }

    private void viewBill() {
        Booking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        try {
            ClientUtil.showBill(this, RemoteServices.payment.getBill(booking.getBookingId()));
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }
}
