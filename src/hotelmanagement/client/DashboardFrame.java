package hotelmanagement.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import hotelmanagement.model.User;

/**
 * Common main window for all three roles: a header with the user name and a
 * Logout button, and a set of tabs. CustomerFrame, ReceptionistFrame and
 * AdminFrame only decide which tabs are shown.
 */
public class DashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTabbedPane tabs = new JTabbedPane();

    public DashboardFrame(String title, User user) {
        super("Hotel Management System - " + title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel heading = new JLabel(title);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

        JButton logoutButton = new JButton("Logout");
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.add(new JLabel("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")"));
        userPanel.add(logoutButton);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        header.add(heading, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // Reload a tab's data from the server whenever the tab is opened
        tabs.addChangeListener(e -> refreshSelectedTab());
        logoutButton.addActionListener(e -> logout());

        setSize(1000, 620);
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
