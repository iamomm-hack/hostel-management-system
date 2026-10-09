package hotelmanagement.client;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

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

/**
 * Modern, clean login window with role-based routing and customer registration.
 * Centered layout with #5eae84 brand theme.
 */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField usernameField = UITheme.createTextField(18);
    private final JPasswordField passwordField = UITheme.createPasswordField(18);

    public LoginFrame() {
        super("Hotel Management System - Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_APP);
        setLayout(new GridBagLayout());

        // Card container
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));
        card.setPreferredSize(new Dimension(420, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Logo Badge
        JLabel logoBadge = new JLabel("HMS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.INFO_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(UITheme.PRIMARY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoBadge.setForeground(UITheme.PRIMARY);
        logoBadge.setPreferredSize(new Dimension(58, 28));

        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(logoBadge, gbc);

        // Title
        JLabel titleLabel = new JLabel("Hostel & Hotel System", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Sign in to access your dashboard", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_CAPTION);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(subtitleLabel, gbc);

        // Username Label
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setForeground(UITheme.TEXT_SECONDARY);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(userLabel, gbc);

        // Username Field
        usernameField.setPreferredSize(new Dimension(0, 38));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(usernameField, gbc);

        // Password Label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_BOLD);
        passLabel.setForeground(UITheme.TEXT_SECONDARY);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(passLabel, gbc);

        // Password Field
        passwordField.setPreferredSize(new Dimension(0, 38));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 22, 0);
        card.add(passwordField, gbc);

        // Sign In Button
        JButton loginButton = UITheme.createPrimaryButton("Sign In");
        loginButton.setFont(UITheme.FONT_BOLD);
        loginButton.setPreferredSize(new Dimension(0, 42));

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(loginButton, gbc);

        // Register Button
        JButton registerButton = UITheme.createSecondaryButton("Register New Customer");
        registerButton.setFont(UITheme.FONT_REGULAR);
        registerButton.setPreferredSize(new Dimension(0, 38));

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 18, 0);
        card.add(registerButton, gbc);

        // Hint Label
        JLabel hintLabel = new JLabel("Roles: admin / receptionist / registered customers", SwingConstants.CENTER);
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLabel.setForeground(UITheme.TEXT_MUTED);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(hintLabel, gbc);

        // Center card inside frame
        GridBagConstraints frameGbc = new GridBagConstraints();
        frameGbc.gridx = 0;
        frameGbc.gridy = 0;
        frameGbc.weightx = 1.0;
        frameGbc.weighty = 1.0;
        frameGbc.anchor = GridBagConstraints.CENTER;
        add(card, frameGbc);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        getRootPane().setDefaultButton(loginButton);

        setSize(540, 580);
        setMinimumSize(new Dimension(480, 540));
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
