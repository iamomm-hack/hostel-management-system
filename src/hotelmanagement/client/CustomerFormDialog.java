package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import hotelmanagement.model.Customer;

/**
 * Modern modal form for registering a new customer or walk-in guest.
 * Includes an optional profile picture selection section.
 */
public class CustomerFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final boolean withLogin;

    private final JTextField usernameField = UITheme.createTextField(18);
    private final JPasswordField passwordField = UITheme.createPasswordField(18);
    private final JPasswordField confirmField = UITheme.createPasswordField(18);
    private final JTextField nameField = UITheme.createTextField(18);
    private final JTextField phoneField = UITheme.createTextField(18);
    private final JTextField emailField = UITheme.createTextField(18);
    private final JTextField addressField = UITheme.createTextField(18);
    private final JTextField idProofField = UITheme.createTextField(18);

    // Optional profile photo fields
    private File selectedPhotoFile = null;
    private BufferedImage selectedPhotoPreview = null;
    private final JLabel photoPreviewLabel = new JLabel("", SwingConstants.CENTER) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int diameter = Math.min(getWidth(), getHeight());
            if (selectedPhotoPreview != null) {
                BufferedImage circular = ProfileImageUtil.createCircularAvatar(selectedPhotoPreview, diameter);
                g2.drawImage(circular, 0, 0, diameter, diameter, null);
                g2.setColor(UITheme.PRIMARY);
                g2.drawOval(0, 0, diameter - 1, diameter - 1);
            } else {
                g2.setColor(UITheme.BG_SUBTLE);
                g2.fillOval(0, 0, diameter, diameter);
                g2.setColor(UITheme.BORDER_INPUT);
                g2.drawOval(0, 0, diameter - 1, diameter - 1);
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_CAPTION);
                g2.drawString("No Photo", 6, diameter / 2 + 5);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    };

    private Customer savedCustomer;
    private String savedUsername;

    public CustomerFormDialog(Window owner, boolean withLogin) {
        super(owner, withLogin ? "Customer Registration" : "Register Walk-in Guest",
                ModalityType.APPLICATION_MODAL);
        this.withLogin = withLogin;
        getContentPane().setBackground(UITheme.BG_APP);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_CARD);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel headerTitle = new JLabel(withLogin ? "Create Customer Account" : "Register Walk-in Guest");
        headerTitle.setFont(UITheme.FONT_HEADER);
        headerTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel headerSub = new JLabel(withLogin
                ? "Enter contact information, credentials, and optional profile image"
                : "Fill in guest details for immediate reservation");
        headerSub.setFont(UITheme.FONT_CAPTION);
        headerSub.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(headerTitle, BorderLayout.NORTH);
        headerPanel.add(headerSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Body
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Profile Photo Section (Optional, for self-registration with login)
        if (withLogin) {
            JPanel photoPanel = createPhotoPickerPanel();
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(4, 8, 12, 8);
            formCard.add(photoPanel, gbc);
            gbc.gridwidth = 1;
            gbc.insets = new Insets(6, 8, 6, 8);
        }

        if (withLogin) {
            addField(formCard, gbc, row++, "Username *", usernameField);
            addField(formCard, gbc, row++, "Password * (min 6 chars)", passwordField);
            addField(formCard, gbc, row++, "Confirm Password *", confirmField);
        }

        addField(formCard, gbc, row++, "Full Name *", nameField);
        addField(formCard, gbc, row++, "Phone Number * (10 digits)", phoneField);
        addField(formCard, gbc, row++, "Email Address", emailField);
        addField(formCard, gbc, row++, "Residential Address", addressField);
        addField(formCard, gbc, row++, "Government ID Proof", idProofField);

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
        JButton saveButton = UITheme.createPrimaryButton(withLogin ? "Register Account" : "Save Guest");

        footerPanel.add(cancelButton);
        footerPanel.add(saveButton);
        add(footerPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveButton);

        pack();
        setMinimumSize(new Dimension(500, getHeight()));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JPanel createPhotoPickerPanel() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedLineBorder(UITheme.BORDER, 8, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        // Preview circle
        photoPreviewLabel.setPreferredSize(new Dimension(56, 56));

        // Details and actions
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Profile Photo (Optional)");
        title.setFont(UITheme.FONT_BOLD);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("PNG or JPG image for your avatar display");
        subtitle.setFont(UITheme.FONT_CAPTION);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnRow.setOpaque(false);

        JButton chooseBtn = UITheme.createSecondaryButton("Upload Photo");
        chooseBtn.setFont(UITheme.FONT_CAPTION);

        JButton removeBtn = UITheme.createGhostButton("Remove");
        removeBtn.setFont(UITheme.FONT_CAPTION);

        chooseBtn.addActionListener(e -> choosePhoto());
        removeBtn.addActionListener(e -> removePhoto());

        btnRow.add(chooseBtn);
        btnRow.add(removeBtn);

        details.add(title);
        details.add(Box.createVerticalStrut(2));
        details.add(subtitle);
        details.add(btnRow);

        container.add(photoPreviewLabel);
        container.add(details);
        return container;
    }

    private void choosePhoto() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            File file = ProfileImageUtil.pickImageFile(this);
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                if (file != null) {
                    try {
                        BufferedImage img = ImageIO.read(file);
                        if (img != null) {
                            selectedPhotoFile = file;
                            selectedPhotoPreview = img;
                            photoPreviewLabel.repaint();
                        } else {
                            ClientUtil.showError(this, new IllegalArgumentException("The selected file is not a valid image."));
                        }
                    } catch (Exception ex) {
                        ClientUtil.showError(this, ex);
                    }
                }
            });
        }).start();
    }

    private void removePhoto() {
        selectedPhotoFile = null;
        selectedPhotoPreview = null;
        photoPreviewLabel.repaint();
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void save() {
        try {
            Customer customer = new Customer(
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    addressField.getText().trim(),
                    idProofField.getText().trim()
            );

            if (withLogin) {
                char[] password = passwordField.getPassword();
                if (!Arrays.equals(password, confirmField.getPassword())) {
                    throw new IllegalArgumentException("The two passwords do not match.");
                }
                String username = usernameField.getText().trim();
                savedCustomer = RemoteServices.auth.registerCustomer(username, new String(password), customer);
                savedUsername = username;

                // Save selected profile image if provided
                if (selectedPhotoFile != null) {
                    ProfileImageUtil.saveProfileImage(username, selectedPhotoFile);
                }
            } else {
                savedCustomer = RemoteServices.customer.registerWalkInCustomer(customer);
            }
            dispose();
        } catch (Exception e) {
            ClientUtil.showError(this, e);
        }
    }

    public Customer getSavedCustomer() {
        return savedCustomer;
    }

    public String getSavedUsername() {
        return savedUsername;
    }
}
