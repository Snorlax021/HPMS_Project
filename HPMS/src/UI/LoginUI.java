package UI;

import java.awt.*;
import java.awt.event.*;
import java.security.*;
import javax.swing.*;

public class LoginUI extends JFrame {

    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private char defaultEcho;
    private String selectedRole;

    public LoginUI(String role) {
        this.selectedRole = role;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 500);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JPanel panelTop = new JPanel();
        panelTop.setBackground(new Color(32, 118, 223));
        panelTop.setBounds(0, 0, 634, 57);
        contentPane.add(panelTop);
        panelTop.setLayout(null);

        JLabel lblHeader = new JLabel("HPMS - Login (" + selectedRole + ")");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblHeader.setBounds(10, 11, 300, 35);
        panelTop.add(lblHeader);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(173, 216, 230));
        formPanel.setBounds(68, 110, 498, 294);
        contentPane.add(formPanel);
        formPanel.setLayout(null);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblUser.setBounds(96, 49, 120, 26);
        formPanel.add(lblUser);

        usernameField = new JTextField();
        usernameField.setBounds(96, 86, 257, 25);
        formPanel.add(usernameField);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblPassword.setBounds(96, 122, 120, 26);
        formPanel.add(lblPassword);

        passwordField = new JPasswordField();
        passwordField.setBounds(96, 159, 257, 25);
        formPanel.add(passwordField);

        defaultEcho = passwordField.getEchoChar();

        JButton showBtn = new JButton("👁");
        showBtn.setBounds(363, 159, 50, 25);
        formPanel.add(showBtn);
        showBtn.addActionListener(e -> togglePassword(showBtn));

        JButton btnLogin = new JButton("Log In");
        btnLogin.setBounds(188, 211, 108, 32);
        formPanel.add(btnLogin);
        btnLogin.addActionListener(e -> loginUser());

        // Enter key triggers login
        KeyAdapter enterToLogin = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) loginUser(); }
        };
        usernameField.addKeyListener(enterToLogin);
        passwordField.addKeyListener(enterToLogin);
    }

    private void togglePassword(JButton btn) {
        if (passwordField.getEchoChar() == 0) {
            passwordField.setEchoChar(defaultEcho);
            btn.setText("👁");
        } else {
            passwordField.setEchoChar((char) 0);
            btn.setText("🙈");
        }
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both username and password.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String roleNormalized = selectedRole == null ? "" : selectedRole.trim().toUpperCase();

        // Unified credential map (username + hashed password) - demo only
        java.util.Map<String, String[]> creds = new java.util.HashMap<>();
        creds.put("ADMIN", new String[]{"admin", hashPassword("admin123")});
        creds.put("DOCTOR", new String[]{"doctor", hashPassword("doctor123")});
        creds.put("STAFF", new String[]{"staff", hashPassword("staff123")});
        creds.put("PATIENT", new String[]{"patient", hashPassword("patient123")});

        if (!creds.containsKey(roleNormalized)) {
            JOptionPane.showMessageDialog(this, "Unknown role: " + selectedRole + "\nValid roles: Admin, Doctor, Staff, Patient", "Role Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] expected = creds.get(roleNormalized);
        String hashedInput = hashPassword(password);
        if (!username.equalsIgnoreCase(expected[0])) {
            JOptionPane.showMessageDialog(this, "Incorrect username for role " + selectedRole + ".", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!hashedInput.equals(expected[1])) {
            JOptionPane.showMessageDialog(this, "Incorrect password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Login Successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);

        // Map PATIENT -> USER for dashboard usage
        String dashboardRole = roleNormalized.equals("PATIENT") ? "USER" : roleNormalized;
        DashboardUI dash = new DashboardUI(dashboardRole);
        dash.setVisible(true);
        dispose();
    }

    private String hashPassword(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pass.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}