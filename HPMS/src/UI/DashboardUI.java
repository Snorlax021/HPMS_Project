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

    public static void main(String[] args) {
        String role = "STAFF:REGISTRATION"; // example launch
        EventQueue.invokeLater(() -> {
            try {
                DashboardUI frame = new DashboardUI(role);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DashboardUI(String role) {
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
            }
        });
        rightPanel.add(logoutButton);

        panel.add(rightPanel, BorderLayout.EAST);

        // MAIN CENTER PANEL - Role-based dashboard
        JPanel dashboardPanel = createDashboardPanel(role);
        contentPane.add(dashboardPanel, BorderLayout.CENTER);
    }

    // Extracted method for creating role-based panels
    private JPanel createDashboardPanel(String role) {
        if (role == null) {
            JOptionPane.showMessageDialog(this, "Invalid role. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return new JPanel();  // Default empty panel
        }
        String upper = role.toUpperCase();
        switch (upper) {
            case "ADMIN":
                return new AdminDashboardPanel();
            case "DOCTOR":
                return new DoctorDashboardPanel();
            case "STAFF":
                return new StaffDashboardPanel();
            case "USER":
                return new PatientDashboardPanel();
            default:
                // Support STAFF sub-roles like STAFF:REGISTRATION
                if (upper.startsWith("STAFF:")) {
                    String subRole = upper.substring("STAFF:".length());
                    return new StaffDashboardPanel(subRole);
                }
                JOptionPane.showMessageDialog(this, "Unknown role: " + role + ". Defaulting to basic view.", "Warning", JOptionPane.WARNING_MESSAGE);
                return new JPanel();  // Default panel
        }
    }
}