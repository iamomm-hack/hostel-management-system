package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

/**
 * Modern modal form to create or modify room records.
 */
public class RoomFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Room existingRoom;

    private final JTextField numberField = UITheme.createTextField(14);
    private final JComboBox<RoomType> typeBox = new JComboBox<>(RoomType.values());
    private final JTextField priceField = UITheme.createTextField(14);
    private final JTextField capacityField = UITheme.createTextField(14);

    private boolean saved = false;

    public RoomFormDialog(Window owner, Room room) {
        super(owner, room == null ? "Add Room" : "Update Room " + room.getRoomNumber(),
                ModalityType.APPLICATION_MODAL);
        this.existingRoom = room;
        getContentPane().setBackground(UITheme.BG_APP);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_CARD);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel headerTitle = new JLabel(room == null ? "Add New Room" : "Edit Room " + room.getRoomNumber());
        headerTitle.setFont(UITheme.FONT_HEADER);
        headerTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel headerSub = new JLabel("Configure room specifications, pricing, and guest capacity");
        headerSub.setFont(UITheme.FONT_CAPTION);
        headerSub.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(headerTitle, BorderLayout.NORTH);
        headerPanel.add(headerSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UITheme.styleComboBox(typeBox);

        addField(formCard, gbc, 0, "Room Number *", numberField);
        addTypeField(formCard, gbc, 1, "Room Classification *", typeBox);
        addField(formCard, gbc, 2, "Rate per Night (Rs.) *", priceField);
        addField(formCard, gbc, 3, "Maximum Capacity (guests) *", capacityField);

        if (room != null) {
            numberField.setText(room.getRoomNumber());
            typeBox.setSelectedItem(room.getRoomType());
            priceField.setText(String.valueOf(room.getPrice()));
            capacityField.setText(String.valueOf(room.getCapacity()));
        }

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        centerWrapper.add(formCard, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footerPanel.setBackground(UITheme.BG_CARD);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelButton = UITheme.createSecondaryButton("Cancel");
        JButton saveButton = UITheme.createPrimaryButton("Save Room");

        footerPanel.add(cancelButton);
        footerPanel.add(saveButton);
        add(footerPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveButton);

        pack();
        setMinimumSize(new Dimension(440, getHeight()));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(field, gbc);
    }

    private void addTypeField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComboBox<RoomType> box) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(box, gbc);
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
