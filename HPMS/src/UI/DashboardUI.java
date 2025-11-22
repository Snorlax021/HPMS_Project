package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        // Example: Pass role as an argument or set it here
        String role = "STAFF";  // Replace with actual role logic (e.g., from login)
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1099, 750);
        setResizable(false);
        
        contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        // HEADER PANEL (NORTH)
        JPanel panel = new JPanel();
        panel.setBackground(SystemColor.textHighlight);
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setLayout(null);
        contentPane.add(panel, BorderLayout.NORTH);

        JLabel lblNewLabel_1 = new JLabel("LOGO HERE    HPMS: Hospital Patient Management System");
        lblNewLabel_1.setForeground(Color.WHITE);
        lblNewLabel_1.setFont(new Font("Artifakt Element Medium", Font.BOLD, 14));
        lblNewLabel_1.setBounds(10, 0, 600, 60);
        panel.add(lblNewLabel_1);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(980, 18, 90, 25);
        logoutButton.setToolTipText("Click to logout and return to login");  // Added tooltip for UX
        panel.add(logoutButton);

        // Add logout functionality with confirmation
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                // Perform logout: Dispose the frame (closes dashboard)
                // You can customize this to redirect to a login screen or exit the app
                dispose();  // Closes the current window
                // Optional: System.exit(0); if you want to exit the entire app
            }
            // If NO, do nothing (dialog closes)
        });

        JLabel roleLabel = new JLabel();
        roleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setFont(new Font("Artifakt Element Medium", Font.BOLD, 14));
        roleLabel.setBounds(861, 0, 109, 60);
        panel.add(roleLabel);

        // MAIN CENTER PANEL - Role-based dashboard
        JPanel dashboardPanel = createDashboardPanel(role);
        roleLabel.setText(role.toUpperCase());  // Display role in uppercase
        contentPane.add(dashboardPanel, BorderLayout.CENTER);
    }

    // Extracted method for creating role-based panels
    private JPanel createDashboardPanel(String role) {
        if (role == null) {
            JOptionPane.showMessageDialog(this, "Invalid role. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return new JPanel();  // Default empty panel
        }
        
        switch (role.toUpperCase()) {
            case "ADMIN":
                return new AdminDashboardPanel();
            case "DOCTOR":
                return new DoctorDashboardPanel();
            case "STAFF":
                return new StaffDashboardPanel();
            case "USER":
                return new PatientDashboardPanel();
            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + role + ". Defaulting to basic view.", "Warning", JOptionPane.WARNING_MESSAGE);
                return new JPanel();  // Default panel
        }
    }
}
