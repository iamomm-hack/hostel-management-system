package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Payment;

/**
 * Modern financial payments ledger and audit trail.
 */
public class PaymentsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel model = ClientUtil.readOnlyModel(
            "Payment ID", "Booking ID", "Customer", "Room", "Room Charges", "Services", "GST",
            "Total Amount", "Payment Method", "Paid On");
    private final JTable table = ClientUtil.createTable(model);
    private final JLabel countLabel = new JLabel("0 records");

    public PaymentsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel title = new JLabel("Payment Transactions");
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

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        JButton refreshButton = UITheme.createGhostButton("Refresh Ledger");
        refreshButton.addActionListener(e -> refreshData());
        rightPanel.add(refreshButton);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        JScrollPane scrollPane = UITheme.createScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        try {
            model.setRowCount(0);
            int count = 0;
            for (Payment p : RemoteServices.payment.getAllPayments()) {
                model.addRow(new Object[] {
                        p.getPaymentId(),
                        p.getBookingId(),
                        p.getCustomerName(),
                        p.getRoomNumber(),
                        ClientUtil.money(p.getRoomCharges()),
                        ClientUtil.money(p.getServiceCharges()),
                        ClientUtil.money(p.getGstAmount()),
                        ClientUtil.money(p.getTotalAmount()),
                        p.getPaymentMethod().name(),
                        p.getPaymentDate().format(ClientUtil.DATE_TIME)
                });
                count++;
            }
            countLabel.setText(count + " transactions logged");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }
}
