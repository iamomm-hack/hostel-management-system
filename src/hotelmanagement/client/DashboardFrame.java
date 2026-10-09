package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import hotelmanagement.model.User;

/**
 * Modern dashboard frame featuring a clean executive navigation bar,
 * authenticated user badge, and styled tab panes.
 */
public class DashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTabbedPane tabs = new JTabbedPane();

    public DashboardFrame(String title, User user) {
        super("Hotel Management System - " + title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_APP);
        setLayout(new BorderLayout());

        // Header Panel (Top Bar)
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(UITheme.BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        header.setBackground(UITheme.BG_CARD);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Brand & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoBadge = new JLabel("HMS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setFont(UITheme.FONT_BADGE);
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setPreferredSize(new Dimension(38, 24));

        JLabel heading = new JLabel(title);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);

        brandPanel.add(logoBadge);
        brandPanel.add(heading);

        // User info & Logout button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        // User Avatar Circle (loads custom profile photo if uploaded, otherwise initial)
        String username = user.getUsername();
        String initial = username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase();
        java.awt.image.BufferedImage rawPhoto = ProfileImageUtil.loadProfileImage(username);
        java.awt.image.BufferedImage circularPhoto = (rawPhoto != null) ? ProfileImageUtil.createCircularAvatar(rawPhoto, 32) : null;

        JLabel avatar = new JLabel(circularPhoto != null ? "" : initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (circularPhoto != null) {
                    g2.drawImage(circularPhoto, 0, 0, getWidth(), getHeight(), null);
                    g2.setColor(UITheme.PRIMARY);
                    g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                } else {
                    g2.setColor(UITheme.BG_SUBTLE);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(UITheme.BORDER_INPUT);
                    g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(UITheme.FONT_BOLD);
        avatar.setForeground(UITheme.TEXT_PRIMARY);
        avatar.setPreferredSize(new Dimension(32, 32));

        JLabel userLabel = new JLabel(user.getUsername());
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel roleBadge = new JLabel("  " + user.getRole() + "  ");
        roleBadge.setFont(UITheme.FONT_BADGE);
        roleBadge.setForeground(UITheme.PRIMARY);
        roleBadge.setBackground(UITheme.INFO_BG);
        roleBadge.setOpaque(true);
        roleBadge.setBorder(new UITheme.RoundedLineBorder(new Color(0xA8, 0xD5, 0xBF), 10, 1));

        JButton logoutButton = UITheme.createSecondaryButton("Sign Out");
        logoutButton.setFont(UITheme.FONT_REGULAR);

        userPanel.add(avatar);
        userPanel.add(userLabel);
        userPanel.add(roleBadge);
        userPanel.add(Box.createHorizontalStrut(8));
        userPanel.add(logoutButton);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Tabbed Pane Setup
        tabs.setFont(UITheme.FONT_SUBHEADER);
        tabs.setBackground(UITheme.BG_APP);
        tabs.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));
        add(tabs, BorderLayout.CENTER);

        tabs.addChangeListener(e -> refreshSelectedTab());
        logoutButton.addActionListener(e -> logout());

        setSize(1100, 700);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
    }

    protected void addTab(String name, JPanel panel) {
        tabs.addTab(name, panel);
    }

    private void refreshSelectedTab() {
        Component selected = tabs.getSelectedComponent();
        if (selected instanceof Refreshable) {
            ((Refreshable) selected).refreshData();
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
