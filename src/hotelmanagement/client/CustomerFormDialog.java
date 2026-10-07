package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import hotelmanagement.model.Customer;

/**
 * Form for a new customer.
 * withLogin = true  : self-registration from the login screen (also creates a username and password).
 * withLogin = false : the receptionist registers a walk-in customer (no login account).
 */
public class CustomerFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final boolean withLogin;

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);
    private final JTextField nameField = new JTextField(18);
    private final JTextField phoneField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField addressField = new JTextField(18);
    private final JTextField idProofField = new JTextField(18);

    private Customer savedCustomer;   // stays null if the dialog is cancelled

    public CustomerFormDialog(Window owner, boolean withLogin) {
        super(owner, withLogin ? "Customer Registration" : "Register Walk-in Customer",
                ModalityType.APPLICATION_MODAL);
        this.withLogin = withLogin;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        if (withLogin) {
            form.add(new JLabel("Username: *"));
            form.add(usernameField);
            form.add(new JLabel("Password: * (min 6 characters)"));
            form.add(passwordField);
            form.add(new JLabel("Confirm Password: *"));
            form.add(confirmField);
        }
        form.add(new JLabel("Full Name: *"));
        form.add(nameField);
        form.add(new JLabel("Phone: * (10 digits)"));
        form.add(phoneField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("ID Proof:"));
        form.add(idProofField);
        add(form, BorderLayout.CENTER);

        JButton saveButton = new JButton(withLogin ? "Register" : "Save Customer");
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
            Customer customer = new Customer(nameField.getText().trim(), phoneField.getText().trim(),
                    emailField.getText().trim(), addressField.getText().trim(), idProofField.getText().trim());

            if (withLogin) {
                char[] password = passwordField.getPassword();
                if (!Arrays.equals(password, confirmField.getPassword())) {
                    throw new IllegalArgumentException("The two passwords do not match.");
                }
                savedCustomer = RemoteServices.auth.registerCustomer(
                        usernameField.getText().trim(), new String(password), customer);
            } else {
                savedCustomer = RemoteServices.customer.registerWalkInCustomer(customer);
            }
            dispose();
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    /** The customer saved on the server, or null if the dialog was cancelled. */
    public Customer getSavedCustomer() {
        return savedCustomer;
    }

    public String getSavedUsername() {
        return usernameField.getText().trim();
    }
}
