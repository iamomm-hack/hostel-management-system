package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Role;
import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;

/**
 * List of all rooms with their current status.
 * The receptionist can change the status; the admin can also add, update and delete rooms.
 */
public class RoomsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel model =
            ClientUtil.readOnlyModel("Room ID", "Room No", "Type", "Price / Night", "Capacity", "Status");
    private final JTable table = ClientUtil.createTable(model);
    private List<Room> rooms = new ArrayList<>();

    public RoomsPanel(Role role) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        buttons.add(refreshButton);

        if (role == Role.ADMIN) {
            JButton addButton = new JButton("Add Room");
            JButton updateButton = new JButton("Update Room");
            JButton deleteButton = new JButton("Delete Room");
            addButton.addActionListener(e -> addRoom());
            updateButton.addActionListener(e -> updateRoom());
            deleteButton.addActionListener(e -> deleteRoom());
            buttons.add(addButton);
            buttons.add(updateButton);
            buttons.add(deleteButton);
        }

        JButton maintenanceButton = new JButton("Set Maintenance");
        JButton availableButton = new JButton("Set Available");
        maintenanceButton.addActionListener(e -> changeStatus(RoomStatus.MAINTENANCE));
        availableButton.addActionListener(e -> changeStatus(RoomStatus.AVAILABLE));
        buttons.add(maintenanceButton);
        buttons.add(availableButton);

        add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        try {
            rooms = RemoteServices.room.getAllRooms();
            model.setRowCount(0);
            for (Room room : rooms) {
                model.addRow(new Object[] {room.getRoomId(), room.getRoomNumber(), room.getRoomType(),
                        ClientUtil.money(room.getPrice()), room.getCapacity(), room.getStatus()});
            }
        } catch (Exception e) {
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

    private void addRoom() {
        RoomFormDialog dialog = new RoomFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void updateRoom() {
        Room room = selectedRoom();
        if (room == null) {
            return;
        }
        RoomFormDialog dialog = new RoomFormDialog(SwingUtilities.getWindowAncestor(this), room);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void deleteRoom() {
        Room room = selectedRoom();
        if (room == null) {
            return;
        }
        if (!ClientUtil.confirm(this, "Delete room " + room.getRoomNumber() + "?")) {
            return;
        }
        try {
            RemoteServices.room.deleteRoom(room.getRoomId());
            ClientUtil.showInfo(this, "Room " + room.getRoomNumber() + " was deleted.");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
        refreshData();
    }

    private void changeStatus(RoomStatus status) {
        Room room = selectedRoom();
        if (room == null) {
            return;
        }
        try {
            RemoteServices.room.updateRoomStatus(room.getRoomId(), status);
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
        refreshData();
    }
}
