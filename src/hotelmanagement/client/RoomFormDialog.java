package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import hotelmanagement.model.Room;
import hotelmanagement.model.RoomStatus;
import hotelmanagement.model.RoomType;

/** Admin form to add a new room (room == null) or update an existing one. */
public class RoomFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Room existingRoom;   // null when adding

    private final JTextField numberField = new JTextField(12);
    private final JComboBox<RoomType> typeBox = new JComboBox<>(RoomType.values());
    private final JTextField priceField = new JTextField(12);
    private final JTextField capacityField = new JTextField(12);

    private boolean saved = false;

    public RoomFormDialog(Window owner, Room room) {
        super(owner, room == null ? "Add Room" : "Update Room " + room.getRoomNumber(),
                ModalityType.APPLICATION_MODAL);
        this.existingRoom = room;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        form.add(new JLabel("Room Number:"));
        form.add(numberField);
        form.add(new JLabel("Room Type:"));
        form.add(typeBox);
        form.add(new JLabel("Price per Night (Rs.):"));
        form.add(priceField);
        form.add(new JLabel("Capacity (guests):"));
        form.add(capacityField);
        add(form, BorderLayout.CENTER);

        if (room != null) {
            numberField.setText(room.getRoomNumber());
            typeBox.setSelectedItem(room.getRoomType());
            priceField.setText(String.valueOf(room.getPrice()));
            capacityField.setText(String.valueOf(room.getCapacity()));
        }

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(saveButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> dispose());

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void save() {
        try {
            Room room = new Room();
            room.setRoomNumber(numberField.getText().trim());
            room.setRoomType((RoomType) typeBox.getSelectedItem());
            room.setPrice(ClientUtil.parseDouble(priceField.getText(), "Price"));
            room.setCapacity(ClientUtil.parseInt(capacityField.getText(), "Capacity"));

            if (existingRoom == null) {
                room.setStatus(RoomStatus.AVAILABLE);
                RemoteServices.room.addRoom(room);
            } else {
                room.setRoomId(existingRoom.getRoomId());
                room.setStatus(existingRoom.getStatus());
                RemoteServices.room.updateRoom(room);
            }
            saved = true;
            dispose();
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
