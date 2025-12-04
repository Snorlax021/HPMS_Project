package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.*;

public class DashboardUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JComponent dashboardPanel;
    private JComboBox<String> roleSelector; // role switcher
    private boolean allowRoleSwitch = true;
    private String currentUsername;

    public static void main(String[] args) {
        String role = (args != null && args.length > 0) ? args[0] : "USER";
        String username = (args != null && args.length > 1) ? args[1] : "";
        boolean allowSwitch = false;
        EventQueue.invokeLater(() -> {
            try {
                DashboardUI frame = new DashboardUI(role, allowSwitch, username);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DashboardUI(String role) { this(role, true, null); }
    public DashboardUI(String role, boolean allowRoleSwitch) { this(role, allowRoleSwitch, null); }
    public DashboardUI(String role, boolean allowRoleSwitch, String username) {
        this.allowRoleSwitch = allowRoleSwitch;
        this.currentUsername = username;
        setTitle("HPMS Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1099, 750);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        // HEADER PANEL (NORTH)
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(70, 130, 180)); // Steel Blue
        panel.setPreferredSize(new Dimension(0, 60));
        contentPane.add(panel, BorderLayout.NORTH);

        JLabel logoLabel = new JLabel("LOGO HERE");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(logoLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("HPMS: Hospital Patient Management System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        String roleDisplay = role != null ? role : "UNKNOWN";
        JLabel roleLabel = new JLabel(roleDisplay.toUpperCase());
        roleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        rightPanel.add(roleLabel);

        if (currentUsername != null && !currentUsername.isBlank()) {
            JLabel userLabel = new JLabel("Logged in: " + currentUsername);
            userLabel.setForeground(Color.WHITE);
            userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            rightPanel.add(userLabel);
        }

        JButton logoutButton = new JButton("Logout");
        logoutButton.setToolTipText("Click to logout and return to login");
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                EventQueue.invokeLater(() -> new LoginUI().setVisible(true));
            }
        });
        rightPanel.add(logoutButton);

        // ROLE SWITCHER -------------------------------------------------
        if (allowRoleSwitch) {
            roleSelector = new JComboBox<>(new String[]{"ADMIN","DOCTOR","USER","STAFF","STAFF:REGISTRATION","STAFF:BILLING","STAFF:LAB"});
            roleSelector.setSelectedItem(role != null ? role.toUpperCase() : "USER");
            roleSelector.setToolTipText("Switch current role dashboard");
            roleSelector.addActionListener(e -> setRole((String) roleSelector.getSelectedItem()));
            rightPanel.add(new JLabel("Role:"));
            rightPanel.add(roleSelector);
        }

        panel.add(rightPanel, BorderLayout.EAST);

        // MAIN CENTER PANEL - Role-based dashboard
        JPanel dashboardPanel = createDashboardPanel(role);
        this.dashboardPanel = dashboardPanel;
        contentPane.add(dashboardPanel, BorderLayout.CENTER);
    }

    // Create role-based panels and pass currentUsername
    private JPanel createDashboardPanel(String role) {
        if (role == null) {
            JOptionPane.showMessageDialog(this, "Invalid role. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return new JPanel();
        }
        String upper = role.toUpperCase();
        switch (upper) {
            case "ADMIN":
                return new AdminDashboardPanel(currentUsername);
            case "DOCTOR":
                return new DoctorDashboardPanel(currentUsername);
            case "STAFF":
                return new StaffDashboardPanel(currentUsername);
            case "USER":
                return new PatientDashboardPanel(currentUsername);
            default:
                if (upper.startsWith("STAFF:")) {
                    String subRole = upper.substring("STAFF:".length());
                    return new StaffDashboardPanel(subRole, currentUsername);
                }
                JOptionPane.showMessageDialog(this, "Unknown role: " + role + ". Defaulting to basic view.", "Warning", JOptionPane.WARNING_MESSAGE);
                return new JPanel();
        }
    }

    // NEW: change role at runtime --------------------------------------
    public void setRole(String role) {
        if (role == null) return;
        if (dashboardPanel != null) {
            getContentPane().remove(dashboardPanel);
        }
        JPanel newPanel = createDashboardPanel(role);
        this.dashboardPanel = newPanel;
        getContentPane().add(newPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}