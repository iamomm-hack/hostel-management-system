package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Role;
import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;

/**
 * Modern management interface for rooms with clean status badges and organized action controls.
 */
public class RoomsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel model =
            ClientUtil.readOnlyModel("Room ID", "Room No", "Type", "Price / Night", "Capacity", "Status");
    private final JTable table = ClientUtil.createTable(model);
    private final JLabel countLabel = new JLabel("0 rooms");
    private List<Room> rooms = new ArrayList<>();

    public RoomsPanel(Role role) {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top bar with header and count
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Room Inventory");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);

        countLabel.setFont(UITheme.FONT_BADGE);
        countLabel.setForeground(UITheme.TEXT_MUTED);
        countLabel.setOpaque(true);
        countLabel.setBackground(UITheme.BG_SUBTLE);
        countLabel.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedLineBorder(UITheme.BORDER, 10, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        titlePanel.add(title);
        titlePanel.add(countLabel);

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton refreshButton = UITheme.createGhostButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        toolbar.add(refreshButton);

        if (role == Role.ADMIN) {
            JButton addButton = UITheme.createPrimaryButton("+ Add Room");
            JButton updateButton = UITheme.createSecondaryButton("Edit Room");
            JButton deleteButton = UITheme.createDangerButton("Delete Room");

            addButton.addActionListener(e -> addRoom());
            updateButton.addActionListener(e -> updateRoom());
            deleteButton.addActionListener(e -> deleteRoom());

            toolbar.add(addButton);
            toolbar.add(updateButton);
            toolbar.add(deleteButton);
        }

        JButton maintenanceButton = UITheme.createSecondaryButton("Set Maintenance");
        JButton availableButton = UITheme.createSuccessButton("Set Available");
        maintenanceButton.addActionListener(e -> changeStatus(RoomStatus.MAINTENANCE));
        availableButton.addActionListener(e -> changeStatus(RoomStatus.AVAILABLE));

        toolbar.add(maintenanceButton);
        toolbar.add(availableButton);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(toolbar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table in styled card scrollpane
        JScrollPane scrollPane = UITheme.createScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        try {
            rooms = RemoteServices.room.getAllRooms();
            model.setRowCount(0);
            for (Room room : rooms) {
                model.addRow(new Object[] {room.getRoomId(), room.getRoomNumber(), room.getRoomType(),
                        ClientUtil.money(room.getPrice()), room.getCapacity(), room.getStatus().name()});
            }
            countLabel.setText(rooms.size() + " rooms total");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    private Room selectedRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            ClientUtil.showInfo(this, "Please select a room from the table first.");
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
        if (!ClientUtil.confirm(this, "Are you sure you want to delete Room " + room.getRoomNumber() + "?")) {
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
