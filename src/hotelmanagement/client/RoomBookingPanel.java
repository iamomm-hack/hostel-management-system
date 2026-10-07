package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Booking;
import hotelmanagement.model.Customer;
import hotelmanagement.model.Room;
import hotelmanagement.model.RoomType;

/**
 * Search for rooms that are free on the chosen dates and book one.
 * A customer books for themself; the receptionist types the ID of the customer.
 */
public class RoomBookingPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private static final String ANY_TYPE = "ANY";

    private final int fixedCustomerId;   // 0 when the receptionist chooses the customer

    private final JTextField checkInField = new JTextField(LocalDate.now().toString(), 9);
    private final JTextField checkOutField = new JTextField(LocalDate.now().plusDays(1).toString(), 9);
    private final JTextField guestsField = new JTextField("1", 3);
    private final JComboBox<String> typeBox = new JComboBox<>();
    private final JTextField customerIdField = new JTextField(6);

    private final DefaultTableModel model =
            ClientUtil.readOnlyModel("Room No", "Type", "Price / Night", "Capacity", "Total for Stay");
    private final JTable table = ClientUtil.createTable(model);

    // The rooms shown in the table and the search they belong to
    private List<Room> rooms = new ArrayList<>();
    private LocalDate searchedCheckIn;
    private LocalDate searchedCheckOut;
    private int searchedGuests;

    public RoomBookingPanel(int fixedCustomerId) {
        this.fixedCustomerId = fixedCustomerId;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        typeBox.addItem(ANY_TYPE);
        for (RoomType type : RoomType.values()) {
            typeBox.addItem(type.name());
        }

        JButton searchButton = new JButton("Search Available Rooms");
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search (dates as YYYY-MM-DD)"));
        searchPanel.add(new JLabel("Check-in:"));
        searchPanel.add(checkInField);
        searchPanel.add(new JLabel("Check-out:"));
        searchPanel.add(checkOutField);
        searchPanel.add(new JLabel("Guests:"));
        searchPanel.add(guestsField);
        searchPanel.add(new JLabel("Room type:"));
        searchPanel.add(typeBox);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton detailsButton = new JButton("View Room Details");
        JButton bookButton = new JButton("Book Selected Room");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        if (fixedCustomerId == 0) {
            bottom.add(new JLabel("Customer ID (see Customers tab):"));
            bottom.add(customerIdField);
        }
        bottom.add(detailsButton);
        bottom.add(bookButton);
        add(bottom, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> search());
        detailsButton.addActionListener(e -> showDetails());
        bookButton.addActionListener(e -> bookSelectedRoom());
    }

    @Override
    public void refreshData() {
        search();
    }

    private void search() {
        try {
            LocalDate checkIn = ClientUtil.parseDate(checkInField.getText(), "Check-in");
            LocalDate checkOut = ClientUtil.parseDate(checkOutField.getText(), "Check-out");
            int guests = ClientUtil.parseInt(guestsField.getText(), "Guests");
            String typeName = (String) typeBox.getSelectedItem();
            RoomType type = ANY_TYPE.equals(typeName) ? null : RoomType.valueOf(typeName);

            rooms = RemoteServices.room.searchAvailableRooms(checkIn, checkOut, type, guests);
            searchedCheckIn = checkIn;
            searchedCheckOut = checkOut;
            searchedGuests = guests;

            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            model.setRowCount(0);
            for (Room room : rooms) {
                model.addRow(new Object[] {room.getRoomNumber(), room.getRoomType(),
                        ClientUtil.money(room.getPrice()), room.getCapacity(),
                        ClientUtil.money(nights * room.getPrice())});
            }
        } catch (Exception e) {
            rooms = new ArrayList<>();
            model.setRowCount(0);
            ClientUtil.showError(this, e);
        }
    }

    /** Returns the selected room, or null (after telling the user) if no row is selected. */
    private Room selectedRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ClientUtil.showInfo(this, "Please select a room in the table first.");
            return null;
        }
        return rooms.get(row);
    }

    private void showDetails() {
        Room room = selectedRoom();
        if (room == null) {
            return;
        }
        long nights = ChronoUnit.DAYS.between(searchedCheckIn, searchedCheckOut);
        ClientUtil.showInfo(this,
                "Room number : " + room.getRoomNumber() + "\n"
                + "Room type : " + room.getRoomType() + "\n"
                + "Capacity : " + room.getCapacity() + " guest(s)\n"
                + "Price per night : " + ClientUtil.money(room.getPrice()) + "\n\n"
                + "Stay : " + searchedCheckIn + " to " + searchedCheckOut + " (" + nights + " night(s))\n"
                + "Room charges : " + ClientUtil.money(nights * room.getPrice()) + "\n"
                + "(GST and any additional services are added in the final bill)");
    }

    private void bookSelectedRoom() {
        Room room = selectedRoom();
        if (room == null) {
            return;
        }
        try {
            int customerId = fixedCustomerId;
            String customerName = "yourself";
            if (customerId == 0) {
                customerId = ClientUtil.parseInt(customerIdField.getText(), "Customer ID");
                Customer customer = RemoteServices.customer.getCustomerById(customerId);
                customerName = customer.getFullName() + " (ID " + customerId + ")";
            }

            boolean yes = ClientUtil.confirm(this, "Book room " + room.getRoomNumber() + " for " + customerName
                    + "\nfrom " + searchedCheckIn + " to " + searchedCheckOut
                    + " for " + searchedGuests + " guest(s)?");
            if (!yes) {
                return;
            }

            // The server checks availability again, so a room taken meanwhile is rejected
            Booking booking = RemoteServices.booking.createBooking(customerId, room.getRoomId(),
                    searchedCheckIn, searchedCheckOut, searchedGuests);
            ClientUtil.showInfo(this, "Booking confirmed.\nBooking ID: " + booking.getBookingId()
                    + "\nRoom: " + booking.getRoomNumber());
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
        search();   // the booked room disappears from the list
    }
}
