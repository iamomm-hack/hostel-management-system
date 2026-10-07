package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Payment;

/** Admin view of all payments received. */
public class PaymentsPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel model = ClientUtil.readOnlyModel(
            "Payment ID", "Booking ID", "Customer", "Room", "Room Charges", "Services", "GST",
            "Total", "Method", "Paid On");
    private final JTable table = ClientUtil.createTable(model);

    public PaymentsPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        buttons.add(refreshButton);
        add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        try {
            model.setRowCount(0);
            for (Payment p : RemoteServices.payment.getAllPayments()) {
                model.addRow(new Object[] {p.getPaymentId(), p.getBookingId(), p.getCustomerName(),
                        p.getRoomNumber(), ClientUtil.money(p.getRoomCharges()),
                        ClientUtil.money(p.getServiceCharges()), ClientUtil.money(p.getGstAmount()),
                        ClientUtil.money(p.getTotalAmount()), p.getPaymentMethod(),
                        p.getPaymentDate().format(ClientUtil.DATE_TIME)});
            }
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }
}
