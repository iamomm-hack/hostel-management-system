package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import hotelmanagement.model.Customer;
import hotelmanagement.model.User;

/** First screen: login for all roles, and a link to customer registration. */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    public LoginFrame() {
        super("Hotel Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Hotel Management System", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(20, 30, 5, 30));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 40, 10, 40));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("New Customer? Register");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        buttons.add(loginButton);
        buttons.add(registerButton);
        add(buttons, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        getRootPane().setDefaultButton(loginButton);   // Enter key logs in

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            User user = RemoteServices.auth.login(username, password);
            JFrame dashboard;
            switch (user.getRole()) {
                case ADMIN:
                    dashboard = new AdminFrame(user);
                    break;
                case RECEPTIONIST:
                    dashboard = new ReceptionistFrame(user);
                    break;
                default:
                    Customer customer = RemoteServices.customer.getCustomerByUserId(user.getUserId());
                    dashboard = new CustomerFrame(user, customer);
                    break;
            }
            dashboard.setVisible(true);
            dispose();
        } catch (Exception e) {
            passwordField.setText("");
            ClientUtil.showError(this, e);
        }
    }

    private void register() {
        CustomerFormDialog dialog = new CustomerFormDialog(this, true);
        dialog.setVisible(true);
        if (dialog.getSavedCustomer() != null) {
            usernameField.setText(dialog.getSavedUsername());
            passwordField.setText("");
            ClientUtil.showInfo(this, "Registration successful. Please log in with your new account.");
        }
    }
}
