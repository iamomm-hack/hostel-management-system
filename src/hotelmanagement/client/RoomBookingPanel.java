package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
 * Modern room search and reservation booking interface.
 */
public class RoomBookingPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private static final String ANY_TYPE = "ALL TYPES";

    private final int fixedCustomerId;

    private final JTextField checkInField = UITheme.createTextField(LocalDate.now().toString(), 9);
    private final JTextField checkOutField = UITheme.createTextField(LocalDate.now().plusDays(1).toString(), 9);
    private final JTextField guestsField = UITheme.createTextField("1", 4);
    private final JComboBox<String> typeBox = new JComboBox<>();
    private final JTextField customerIdField = UITheme.createTextField(6);

    private final DefaultTableModel model =
            ClientUtil.readOnlyModel("Room No", "Type", "Price / Night", "Capacity", "Total Estimated");
    private final JTable table = ClientUtil.createTable(model);

    private List<Room> rooms = new ArrayList<>();
    private LocalDate searchedCheckIn;
    private LocalDate searchedCheckOut;
    private int searchedGuests;

    public RoomBookingPanel(int fixedCustomerId) {
        this.fixedCustomerId = fixedCustomerId;
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Search Filter Card
        JPanel searchCard = UITheme.createCard();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        typeBox.addItem(ANY_TYPE);
        for (RoomType type : RoomType.values()) {
            typeBox.addItem(type.name());
        }
        UITheme.styleComboBox(typeBox);

        JButton searchButton = UITheme.createPrimaryButton("Search Rooms");

        JLabel cinLabel = new JLabel("Check-in:");
        cinLabel.setFont(UITheme.FONT_BOLD);
        cinLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel coutLabel = new JLabel("Check-out:");
        coutLabel.setFont(UITheme.FONT_BOLD);
        coutLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel guestLabel = new JLabel("Guests:");
        guestLabel.setFont(UITheme.FONT_BOLD);
        guestLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(UITheme.FONT_BOLD);
        typeLabel.setForeground(UITheme.TEXT_SECONDARY);

        searchCard.add(cinLabel);
        searchCard.add(checkInField);
        searchCard.add(Box.createHorizontalStrut(4));
        searchCard.add(coutLabel);
        searchCard.add(checkOutField);
        searchCard.add(Box.createHorizontalStrut(4));
        searchCard.add(guestLabel);
        searchCard.add(guestsField);
        searchCard.add(Box.createHorizontalStrut(4));
        searchCard.add(typeLabel);
        searchCard.add(typeBox);
        searchCard.add(Box.createHorizontalStrut(8));
        searchCard.add(searchButton);

        add(searchCard, BorderLayout.NORTH);

        // Center Table
        JScrollPane scrollPane = UITheme.createScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBottom.setOpaque(false);
        if (fixedCustomerId == 0) {
            JLabel cidLabel = new JLabel("Customer ID (from Customers tab):");
            cidLabel.setFont(UITheme.FONT_BOLD);
            cidLabel.setForeground(UITheme.TEXT_SECONDARY);
            leftBottom.add(cidLabel);
            leftBottom.add(customerIdField);
        }

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBottom.setOpaque(false);

        JButton detailsButton = UITheme.createSecondaryButton("Room Specs & Rates");
        JButton bookButton = UITheme.createPrimaryButton("Confirm Reservation");

        rightBottom.add(detailsButton);
        rightBottom.add(bookButton);

        bottomBar.add(leftBottom, BorderLayout.WEST);
        bottomBar.add(rightBottom, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

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
                model.addRow(new Object[] {
                        room.getRoomNumber(),
                        room.getRoomType(),
                        ClientUtil.money(room.getPrice()),
                        room.getCapacity(),
                        ClientUtil.money(nights * room.getPrice())
                });
            }
        } catch (Exception e) {
            rooms = new ArrayList<>();
            model.setRowCount(0);
            ClientUtil.showError(this, e);
        }
    }

    private Room selectedRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ClientUtil.showInfo(this, "Please select an available room from the list first.");
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
                + "Room type   : " + room.getRoomType() + "\n"
                + "Capacity    : " + room.getCapacity() + " guest(s)\n"
                + "Nightly Rate: " + ClientUtil.money(room.getPrice()) + "\n\n"
                + "Dates       : " + searchedCheckIn + " to " + searchedCheckOut + " (" + nights + " night(s))\n"
                + "Base Charges: " + ClientUtil.money(nights * room.getPrice()) + "\n"
                + "(GST and extra amenities are calculated upon checkout)");
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
                customerName = customer.getFullName() + " (ID #" + customerId + ")";
            }

            boolean yes = ClientUtil.confirm(this,
                    "Confirm booking for Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")?\n"
                    + "Guest: " + customerName + "\n"
                    + "Dates: " + searchedCheckIn + " to " + searchedCheckOut + " (" + searchedGuests + " guests)");
            if (!yes) {
                return;
            }

            Booking booking = RemoteServices.booking.createBooking(customerId, room.getRoomId(),
                    searchedCheckIn, searchedCheckOut, searchedGuests);
            ClientUtil.showInfo(this, "Reservation successfully confirmed!\n"
                    + "Booking ID: #" + booking.getBookingId() + "\nRoom: " + booking.getRoomNumber());
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
        search();
    }
}
