package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
 * Modern customer directory with fast search and walk-in registration.
 */
public class CustomersPanel extends JPanel implements Refreshable {
    private static final long serialVersionUID = 1L;

    private final JTextField searchField = UITheme.createTextField(20);
    private final DefaultTableModel model = ClientUtil.readOnlyModel(
            "Customer ID", "Full Name", "Phone", "Email", "Address", "ID Proof", "Account Type");
    private final JTable table = ClientUtil.createTable(model);
    private final JLabel countLabel = new JLabel("0 records");

    public CustomersPanel(boolean canRegister) {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top Search and Action Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel title = new JLabel("Guest Directory");
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
        leftPanel.add(Box.createHorizontalStrut(14));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(UITheme.FONT_BOLD);
        searchLabel.setForeground(UITheme.TEXT_SECONDARY);

        JButton searchButton = UITheme.createSecondaryButton("Search");
        JButton showAllButton = UITheme.createGhostButton("Clear");

        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(searchButton);
        leftPanel.add(showAllButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        if (canRegister) {
            JButton registerButton = UITheme.createPrimaryButton("+ Register Walk-in Customer");
            registerButton.addActionListener(e -> registerWalkIn());
            rightPanel.add(registerButton);
        }

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        JScrollPane scrollPane = UITheme.createScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> refreshData());
        searchField.addActionListener(e -> refreshData());
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshData();
        });
    }

    @Override
    public void refreshData() {
        try {
            List<Customer> customers = RemoteServices.customer.searchCustomers(searchField.getText());
            model.setRowCount(0);
            for (Customer c : customers) {
                model.addRow(new Object[] {
                        c.getCustomerId(),
                        c.getFullName(),
                        c.getPhone(),
                        c.getEmail(),
                        c.getAddress(),
                        c.getIdProof(),
                        c.getUserId() > 0 ? "Registered" : "Walk-in"
                });
            }
            countLabel.setText(customers.size() + " guests found");
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    private void registerWalkIn() {
        CustomerFormDialog dialog = new CustomerFormDialog(SwingUtilities.getWindowAncestor(this), false);
        dialog.setVisible(true);
        Customer saved = dialog.getSavedCustomer();
        if (saved != null) {
            ClientUtil.showInfo(this, "Guest registered successfully.\n"
                    + "Generated Customer ID: #" + saved.getCustomerId()
                    + "\nYou can now use this ID in the 'Book a Room' tab.");
            searchField.setText("");
            refreshData();
        }
    }
}
