package UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Model.Role;
import Model.User;
import Service.UserService;

import java.util.Arrays;
import java.util.Optional;

public class LoginUI extends JFrame {

    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private char defaultEcho;
    private final UserService userService; // use app service for auth

    public LoginUI(String role) {
        this.userService = UserService.getInstance();
        // Seed demo users (idempotent)
        this.userService.createDefaultDemoUsers();

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

        JLabel lblHeader = new JLabel("HPMS - Login");
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

    // Convenience no-arg constructor
    public LoginUI() { this(null); }

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
        char[] password = passwordField.getPassword();
        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in both username and password.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Optional<User> auth = userService.authenticate(username, password);
        // Clear sensitive data ASAP
        Arrays.fill(password, '\0');

        if (auth.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Incorrect username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        }

        User user = auth.get();
        Role actualRole = user.getRole();

        JOptionPane.showMessageDialog(this, "Login Successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);

        // Decide dashboard based on the authenticated user's role
        String dashboardRole = (actualRole == Role.PATIENT) ? "USER" : actualRole.name();
        try {
            DashboardUI dash = new DashboardUI(dashboardRole, false, username); // pass username for header
            dash.setVisible(true);
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to open dashboard: " + ex.getMessage(),
                "Startup Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Allow launching directly for testing/demo
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            LoginUI ui = new LoginUI();
            ui.setVisible(true);
        });
    }
}