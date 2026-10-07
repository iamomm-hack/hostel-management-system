package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import hotelmanagement.model.HotelStatistics;

/** Admin view of the basic hotel statistics. */
public class StatisticsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final JLabel totalRooms = valueLabel();
    private final JLabel availableRooms = valueLabel();
    private final JLabel bookedRooms = valueLabel();
    private final JLabel occupiedRooms = valueLabel();
    private final JLabel maintenanceRooms = valueLabel();
    private final JLabel totalBookings = valueLabel();
    private final JLabel totalRevenue = valueLabel();

    public StatisticsPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel grid = new JPanel(new GridLayout(7, 2, 10, 12));
        grid.setBorder(BorderFactory.createTitledBorder("Hotel Statistics"));
        addRow(grid, "Total rooms:", totalRooms);
        addRow(grid, "Available rooms:", availableRooms);
        addRow(grid, "Booked rooms:", bookedRooms);
        addRow(grid, "Occupied rooms:", occupiedRooms);
        addRow(grid, "Maintenance rooms:", maintenanceRooms);
        addRow(grid, "Number of bookings:", totalBookings);
        addRow(grid, "Total revenue:", totalRevenue);

        // Keep the grid at its natural size in the top-left corner
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT));
        holder.add(grid);
        add(holder, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        buttons.add(refreshButton);
        add(buttons, BorderLayout.SOUTH);
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        return label;
    }

    private void addRow(JPanel grid, String caption, JLabel value) {
        JLabel label = new JLabel(caption);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 40));
        grid.add(label);
        grid.add(value);
    }

    @Override
    public void refreshData() {
        try {
            HotelStatistics stats = RemoteServices.payment.getStatistics();
            totalRooms.setText(String.valueOf(stats.getTotalRooms()));
            availableRooms.setText(String.valueOf(stats.getAvailableRooms()));
            bookedRooms.setText(String.valueOf(stats.getBookedRooms()));
            occupiedRooms.setText(String.valueOf(stats.getOccupiedRooms()));
            maintenanceRooms.setText(String.valueOf(stats.getMaintenanceRooms()));
            totalBookings.setText(String.valueOf(stats.getTotalBookings()));
            totalRevenue.setText(ClientUtil.money(stats.getTotalRevenue()));
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }
}
