package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import hotelmanagement.model.HotelStatistics;

/**
 * Modern executive dashboard panel displaying key performance indicators (KPIs)
 * including revenue, occupancy rate, inventory distribution, and active bookings.
 */
public class StatisticsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    // Stat metric value labels
    private final JLabel revenueValue = createMetricLabel("Rs. 0.00");
    private final JLabel bookingsValue = createMetricLabel("0");
    private final JLabel occupancyValue = createMetricLabel("0.0%");
    private final JLabel totalRoomsValue = createMetricLabel("0");

    // Inventory breakdown labels
    private final JLabel availableCount = createCountLabel("0");
    private final JLabel bookedCount = createCountLabel("0");
    private final JLabel occupiedCount = createCountLabel("0");
    private final JLabel maintenanceCount = createCountLabel("0");

    public StatisticsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Performance & Occupancy Overview");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JButton refreshButton = UITheme.createGhostButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshData());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Content Area (Cards Grid)
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 4 KPI Cards in a row
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 14));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        kpiGrid.add(createKpiCard("TOTAL REVENUE", revenueValue, "Net billing collected", UITheme.PRIMARY));
        kpiGrid.add(createKpiCard("TOTAL BOOKINGS", bookingsValue, "All reservations to date", new Color(0x63, 0x66, 0xF1)));
        kpiGrid.add(createKpiCard("OCCUPANCY RATE", occupancyValue, "Active & booked rooms", UITheme.SUCCESS));
        kpiGrid.add(createKpiCard("TOTAL ROOM CAPACITY", totalRoomsValue, "Managed room units", UITheme.TEXT_SECONDARY));

        contentPanel.add(kpiGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // Inventory Status Breakdown Card
        JPanel breakdownCard = UITheme.createCard();
        breakdownCard.setLayout(new BorderLayout(10, 14));
        breakdownCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel breakdownTitle = new JLabel("Room Inventory Status Breakdown");
        breakdownTitle.setFont(UITheme.FONT_SUBHEADER);
        breakdownTitle.setForeground(UITheme.TEXT_PRIMARY);
        breakdownCard.add(breakdownTitle, BorderLayout.NORTH);

        JPanel statusGrid = new JPanel(new GridLayout(1, 4, 12, 12));
        statusGrid.setOpaque(false);

        statusGrid.add(createStatusBox("Available", availableCount, UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT, UITheme.SUCCESS));
        statusGrid.add(createStatusBox("Booked (Reserved)", bookedCount, UITheme.INFO_BG, UITheme.INFO_TEXT, UITheme.PRIMARY));
        statusGrid.add(createStatusBox("Currently Occupied", occupiedCount, UITheme.WARNING_BG, UITheme.WARNING_TEXT, UITheme.WARNING));
        statusGrid.add(createStatusBox("Under Maintenance", maintenanceCount, UITheme.DANGER_BG, UITheme.DANGER_TEXT, UITheme.DANGER));

        breakdownCard.add(statusGrid, BorderLayout.CENTER);
        contentPanel.add(breakdownCard);
        contentPanel.add(Box.createVerticalGlue());

        add(contentPanel, BorderLayout.CENTER);
    }

    private static JLabel createMetricLabel(String initial) {
        JLabel l = new JLabel(initial);
        l.setFont(UITheme.FONT_STAT_NUM);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private static JLabel createCountLabel(String initial) {
        JLabel l = new JLabel(initial, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        return l;
    }

    private JPanel createKpiCard(String label, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                // Top accent bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel(label);
        title.setFont(UITheme.FONT_BADGE);
        title.setForeground(UITheme.TEXT_MUTED);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(UITheme.FONT_CAPTION);
        sub.setForeground(UITheme.TEXT_MUTED);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        return card;
    }

    private JPanel createStatusBox(String name, JLabel countLabel, Color bg, Color fg, Color borderCol) {
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(borderCol.getRed(), borderCol.getGreen(), borderCol.getBlue(), 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(UITheme.FONT_BOLD);
        nameLabel.setForeground(fg);
        nameLabel.setAlignmentX(0.5f);

        countLabel.setForeground(fg);
        countLabel.setAlignmentX(0.5f);

        box.add(nameLabel);
        box.add(Box.createVerticalStrut(6));
        box.add(countLabel);
        return box;
    }

    @Override
    public void refreshData() {
        try {
            HotelStatistics stats = RemoteServices.payment.getStatistics();
            revenueValue.setText(ClientUtil.money(stats.getTotalRevenue()));
            bookingsValue.setText(String.valueOf(stats.getTotalBookings()));
            totalRoomsValue.setText(String.valueOf(stats.getTotalRooms()));

            int total = stats.getTotalRooms();
            int occupied = stats.getOccupiedRooms();
            int booked = stats.getBookedRooms();
            double rate = total > 0 ? ((double) (occupied + booked) / total) * 100.0 : 0.0;
            occupancyValue.setText(String.format("%.1f%%", rate));

            availableCount.setText(String.valueOf(stats.getAvailableRooms()));
            bookedCount.setText(String.valueOf(stats.getBookedRooms()));
            occupiedCount.setText(String.valueOf(stats.getOccupiedRooms()));
            maintenanceCount.setText(String.valueOf(stats.getMaintenanceRooms()));
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }
}
