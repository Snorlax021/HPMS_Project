package UI;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private String selectedRole = null;

    private JButton btnAdmin, btnDoctor, btnStaff, btnPatient;

    // Default echo char for hiding password
    private char defaultEcho;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginUI frame = new LoginUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 500);
        setResizable(false);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JPanel panelTop = new JPanel();
        panelTop.setBackground(new Color(32, 118, 223));
        panelTop.setBounds(0, 0, 634, 57);
        contentPane.add(panelTop);
        panelTop.setLayout(null);

        JLabel lblHeader = new JLabel("LOGO HERE    HPMS: Hospital Patient Management System");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Artifakt Element Medium", Font.BOLD, 14));
        lblHeader.setBounds(10, 11, 460, 35);
        panelTop.add(lblHeader);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(173, 216, 230));
        formPanel.setBounds(68, 110, 498, 294);
        contentPane.add(formPanel);
        formPanel.setLayout(null);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblUser.setBounds(32, 40, 86, 26);
        formPanel.add(lblUser);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblPassword.setBounds(32, 88, 86, 26);
        formPanel.add(lblPassword);

        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblRole.setBounds(32, 146, 86, 26);
        formPanel.add(lblRole);

        usernameField = new JTextField();
        usernameField.setBounds(139, 43, 257, 25);
        formPanel.add(usernameField);

        passwordField = new JPasswordField();
        passwordField.setBounds(139, 91, 257, 25);
        formPanel.add(passwordField);

        defaultEcho = passwordField.getEchoChar();

        // SHOW / HIDE BUTTON
        JButton showBtn = new JButton("👁");
        showBtn.setBounds(400, 91, 50, 25);
        showBtn.addActionListener(e -> togglePassword(showBtn));
        formPanel.add(showBtn);

        // ROLE BUTTONS
        btnAdmin = createRoleButton("Admin", 87, 145);
        btnDoctor = createRoleButton("Doctor", 186, 145);
        btnStaff = createRoleButton("Staff", 285, 145);
        btnPatient = createRoleButton("Patient", 384, 145);

        formPanel.add(btnAdmin);
        formPanel.add(btnDoctor);
        formPanel.add(btnStaff);
        formPanel.add(btnPatient);

        JButton btnLogin = new JButton("Log In");
        btnLogin.setFont(new Font("Verdana", Font.BOLD, 15));
        btnLogin.setBackground(new Color(140, 238, 140));
        btnLogin.setBounds(196, 213, 108, 32);
        btnLogin.addActionListener(e -> login());
        formPanel.add(btnLogin);
    }

    // Show / Hide password
    private void togglePassword(JButton btn) {
        if (passwordField.getEchoChar() == 0) {
            passwordField.setEchoChar(defaultEcho);
            btn.setText("👁");
        } else {
            passwordField.setEchoChar((char) 0);
            btn.setText("🙈");
        }
    }

    // Create a role button
    private JButton createRoleButton(String role, int x, int y) {
        JButton btn = new JButton(role);
        btn.setBounds(x, y, 89, 32);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);

        btn.addActionListener((ActionEvent e) -> {
            resetRoleButtons();
            selectedRole = role;
            btn.setBackground(new Color(140, 238, 140));
        });

        return btn;
    }

    // LOGIN HANDLER with HASH
    private void login() {
        String username = usernameField.getText();
        String rawPassword = new String(passwordField.getPassword());
        String hashedPassword = hashPassword(rawPassword);

        if (username.isEmpty() || rawPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedRole == null) {
            JOptionPane.showMessageDialog(this, "Please select a role before logging in.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // STORED CREDENTIALS (already hashed)
        java.util.Map<String, String[]> creds = java.util.Map.of(
            "Admin",   new String[]{"admin", hashPassword("admin123")},
            "Doctor",  new String[]{"doctor", hashPassword("doctor123")},
            "Staff",   new String[]{"staff", hashPassword("staff123")},
            "Patient", new String[]{"user", hashPassword("user123")}
        );

        String[] roleData = creds.get(selectedRole);

        if (!username.equals(roleData[0])) {
            JOptionPane.showMessageDialog(this, "Incorrect username.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!hashedPassword.equals(roleData[1])) {
            JOptionPane.showMessageDialog(this, "Incorrect password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Login successful!");

        try {
            String dashboardRole = selectedRole.equals("Patient") ? "USER" : selectedRole.toUpperCase();
            DashboardUI dashboard = new DashboardUI(dashboardRole);
            dashboard.setVisible(true);
            this.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dashboard error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetRoleButtons() {
        Color defaultBg = UIManager.getColor("Button.background");
        btnAdmin.setBackground(defaultBg);
        btnDoctor.setBackground(defaultBg);
        btnStaff.setBackground(defaultBg);
        btnPatient.setBackground(defaultBg);
    }

    // HASH FUNCTION
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
