package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
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
import hotelmanagement.model.ExtraService;
import hotelmanagement.model.PaymentMethod;
import hotelmanagement.model.Role;

/**
 * List of bookings with the actions each role is allowed to use:
 *   CUSTOMER     - own bookings: cancel, view bill
 *   RECEPTIONIST - all bookings: check-in, add service, check-out, cancel, view bill
 *   ADMIN        - all bookings: view bill
 */
public class BookingsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final Role role;
    private final int customerId;   // used only when role is CUSTOMER

    private final DefaultTableModel model = ClientUtil.readOnlyModel(
            "Booking ID", "Customer", "Room", "Check-in", "Check-out", "Guests", "Status", "Booked On");
    private final JTable table = ClientUtil.createTable(model);
    private List<Booking> bookings = new ArrayList<>();

    public BookingsPanel(Role role, int customerId) {
        this.role = role;
        this.customerId = customerId;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        buttons.add(refreshButton);

        if (role == Role.RECEPTIONIST) {
            JButton checkInButton = new JButton("Check-In");
            JButton serviceButton = new JButton("Add Service");
            JButton checkOutButton = new JButton("Check-Out & Bill");
            checkInButton.addActionListener(e -> checkIn());
            serviceButton.addActionListener(e -> addService());
            checkOutButton.addActionListener(e -> checkOut());
            buttons.add(checkInButton);
            buttons.add(serviceButton);
            buttons.add(checkOutButton);
        }
        if (role == Role.RECEPTIONIST || role == Role.CUSTOMER) {
            JButton cancelButton = new JButton("Cancel Booking");
            cancelButton.addActionListener(e -> cancelBooking());
            buttons.add(cancelButton);
        }
        JButton billButton = new JButton("View Bill");
        billButton.addActionListener(e -> viewBill());
        buttons.add(billButton);

        add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        try {
            if (role == Role.CUSTOMER) {
                bookings = RemoteServices.booking.getBookingsByCustomer(customerId);
            } else {
                bookings = RemoteServices.booking.getAllBookings();
            }
            model.setRowCount(0);
            for (Booking b : bookings) {
                model.addRow(new Object[] {b.getBookingId(), b.getCustomerName(), b.getRoomNumber(),
                        b.getCheckIn(), b.getCheckOut(), b.getNumberOfGuests(), b.getStatus(),
                        b.getBookingDate().format(ClientUtil.DATE_TIME)});
            }
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    /** Returns the selected booking, or null (after telling the user) if no row is selected. */
    private Booking selectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ClientUtil.showInfo(this, "Please select a booking in the table first.");
            return null;
        }
        return bookings.get(row);
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
            ClientUtil.showInfo(this, "Booking #" + booking.getBookingId() + " was cancelled.");
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
        try {
            RemoteServices.booking.checkIn(booking.getBookingId());
            ClientUtil.showInfo(this, booking.getCustomerName() + " is checked in to room "
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
            for (ExtraService service : RemoteServices.payment.getAllServices()) {
                serviceBox.addItem(service);
            }
            JTextField quantityField = new JTextField("1");
            JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
            form.add(new JLabel("Service:"));
            form.add(serviceBox);
            form.add(new JLabel("Quantity:"));
            form.add(quantityField);

            int answer = JOptionPane.showConfirmDialog(this, form,
                    "Add service to booking #" + booking.getBookingId(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            ExtraService service = (ExtraService) serviceBox.getSelectedItem();
            int quantity = ClientUtil.parseInt(quantityField.getText(), "Quantity");
            RemoteServices.payment.addServiceUsage(booking.getBookingId(), service.getServiceId(), quantity);
            ClientUtil.showInfo(this, service.getServiceName() + " x" + quantity + " added to the bill.");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    /** Shows the bill, asks for the payment method, then checks the guest out. */
    private void checkOut() {
        Booking booking = selectedBooking();
        if (booking == null) {
            return;
        }
        try {
            Bill bill = RemoteServices.payment.getBill(booking.getBookingId());
            if (bill.isPaid()) {
                ClientUtil.showInfo(this, "This booking is already checked out and paid.");
                return;
            }

            JComboBox<PaymentMethod> methodBox = new JComboBox<>(PaymentMethod.values());
            JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            methodPanel.add(new JLabel("Payment method:"));
            methodPanel.add(methodBox);
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.add(ClientUtil.billArea(bill), BorderLayout.CENTER);
            panel.add(methodPanel, BorderLayout.SOUTH);

            int answer = JOptionPane.showConfirmDialog(this, panel, "Check-out and payment",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }

            PaymentMethod method = (PaymentMethod) methodBox.getSelectedItem();
            RemoteServices.booking.checkOut(booking.getBookingId(), method);
            // Show the final (paid) bill as stored on the server
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
