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
        logoLabel.setFont(new Font("Arial", Font.BOLD, 25));
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
        roleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(roleLabel);

        if (currentUsername != null && !currentUsername.isBlank()) {
            JLabel userLabel = new JLabel("Logged in: " + currentUsername);
            userLabel.setForeground(Color.WHITE);
            userLabel.setFont(new Font("Arial", Font.PLAIN, 20));
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
        JPanel centerPanel = createDashboardPanel(role);
        this.dashboardPanel = centerPanel;
        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
    }

    // Create role-based panels and pass currentUsername
    private JPanel createDashboardPanel(String role) {
        if (role == null) {
            JOptionPane.showMessageDialog(this, "Invalid role. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return new JPanel();
        }
        String upper = role.toUpperCase();
        // First try to load the full dashboard class reflectively (so we don't create compile-time dependency)
        String targetClass = switch (upper) {
            case "ADMIN" -> "UI.AdminDashboardPanel";
            case "DOCTOR" -> "UI.DoctorDashboardPanel";
            case "STAFF" -> "UI.StaffDashboardPanel";
            case "USER" -> "UI.PatientDashboardPanel";
            default -> null;
        };
        if (targetClass != null) {
            try {
                Class<?> cls = Class.forName(targetClass);
                Object inst = null;
                // Prefer constructors: (Object controller, String username) -> call with (null, currentUsername)
                for (java.lang.reflect.Constructor<?> c : cls.getDeclaredConstructors()) {
                    Class<?>[] pts = c.getParameterTypes();
                    try {
                        if (pts.length == 2 && pts[1] == String.class) {
                            c.setAccessible(true);
                            inst = c.newInstance((Object) null, currentUsername);
                            break;
                        }
                        if (pts.length == 1 && pts[0] == String.class) {
                            c.setAccessible(true);
                            inst = c.newInstance(currentUsername);
                            break;
                        }
                        if (pts.length == 0) {
                            c.setAccessible(true);
                            inst = c.newInstance();
                            break;
                        }
                    } catch (Exception ex) {
                        // try next constructor
                    }
                }
                if (inst instanceof JPanel p) return p;
            } catch (Throwable t) {
                // loading failed; we'll fall back to lite panel
                System.err.println("Failed to load full dashboard class: " + targetClass + ". Falling back to Lite. Reason:");
                t.printStackTrace();
            }
        }
        // Fallback lightweight panels preserve original UI layout if full panels unavailable
        switch (upper) {
            case "ADMIN": return new AdminLitePanel(currentUsername);
            case "DOCTOR": return new DoctorLitePanel(currentUsername);
            case "STAFF": return new StaffLitePanel(currentUsername);
            case "USER": return new PatientLitePanel(currentUsername);
            default:
                JPanel p = new JPanel(new BorderLayout());
                p.add(new JLabel(upper + " Dashboard (Lite)", SwingConstants.CENTER), BorderLayout.NORTH);
                p.add(new JLabel("No specialized panel available."), BorderLayout.CENTER);
                return p;
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