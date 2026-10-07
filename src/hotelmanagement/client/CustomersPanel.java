package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import hotelmanagement.model.Customer;

/**
 * List of customers with search by name or phone.
 * The receptionist (canRegister = true) can also register walk-in customers.
 */
public class CustomersPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel model = ClientUtil.readOnlyModel(
            "Customer ID", "Full Name", "Phone", "Email", "Address", "ID Proof", "Type");
    private final JTable table = ClientUtil.createTable(model);

    public CustomersPanel(boolean canRegister) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchPanel.add(new JLabel("Name or phone:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        add(searchPanel, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        if (canRegister) {
            JButton registerButton = new JButton("Register Walk-in Customer");
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
            buttons.add(registerButton);
            add(buttons, BorderLayout.SOUTH);
            registerButton.addActionListener(e -> registerWalkIn());
        }

        searchButton.addActionListener(e -> refreshData());
        searchField.addActionListener(e -> refreshData());   // Enter key in the search box
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshData();
        });
    }

    /** Loads the customers that match the search box (all customers if it is empty). */
    @Override
    public void refreshData() {
        try {
            List<Customer> customers = RemoteServices.customer.searchCustomers(searchField.getText());
            model.setRowCount(0);
            for (Customer c : customers) {
                model.addRow(new Object[] {c.getCustomerId(), c.getFullName(), c.getPhone(), c.getEmail(),
                        c.getAddress(), c.getIdProof(), c.getUserId() > 0 ? "Registered" : "Walk-in"});
            }
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    private void registerWalkIn() {
        CustomerFormDialog dialog = new CustomerFormDialog(SwingUtilities.getWindowAncestor(this), false);
        dialog.setVisible(true);
        Customer saved = dialog.getSavedCustomer();
        if (saved != null) {
            ClientUtil.showInfo(this, "Customer registered.\nCustomer ID: " + saved.getCustomerId()
                    + "\nUse this ID in the 'Book a Room' tab.");
            searchField.setText("");
            refreshData();
        }
    }
}
