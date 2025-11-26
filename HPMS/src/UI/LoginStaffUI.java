package UI;

import java.awt.*;
import java.awt.event.*;
import java.security.*;
import javax.swing.*;

public class LoginStaffUI extends JFrame {

    private JPanel contentPane;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton btnReg, btnBill, btnLab;

    private String selectedRole = null;
    private char defaultEcho;

    public LoginStaffUI() {
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

        JLabel lblHeader = new JLabel("HPMS - Staff Login");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblHeader.setBounds(10, 11, 300, 35);
        panelTop.add(lblHeader);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(173, 216, 230));
        formPanel.setBounds(53, 104, 528, 315);
        contentPane.add(formPanel);
        formPanel.setLayout(null);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblUser.setBounds(113, 53, 120, 26);
        formPanel.add(lblUser);

        usernameField = new JTextField();
        usernameField.setBounds(113, 85, 257, 25);
        formPanel.add(usernameField);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblPassword.setBounds(113, 113, 120, 26);
        formPanel.add(lblPassword);

        passwordField = new JPasswordField();
        passwordField.setBounds(113, 142, 257, 25);
        formPanel.add(passwordField);

        defaultEcho = passwordField.getEchoChar();

        JButton showBtn = new JButton("👁");
        showBtn.setBounds(380, 142, 50, 25);
        formPanel.add(showBtn);
        showBtn.addActionListener(e -> togglePassword(showBtn));

        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblRole.setBounds(113, 178, 86, 26);
        formPanel.add(lblRole);

        btnReg = createRoleButton("Registration");
        btnReg.setBounds(113, 204, 115, 32);
        formPanel.add(btnReg);

        btnBill = createRoleButton("Billing");
        btnBill.setBounds(238, 204, 76, 32);
        formPanel.add(btnBill);

        btnLab = createRoleButton("Lab");
        btnLab.setBounds(324, 204, 76, 32);
        formPanel.add(btnLab);

        btnReg.addActionListener(e -> selectRole("REGISTRATION", btnReg));
        btnBill.addActionListener(e -> selectRole("BILLING", btnBill));
        btnLab.addActionListener(e -> selectRole("LAB", btnLab));

        JButton btnLogin = new JButton("Log In");
        btnLogin.setBounds(193, 260, 108, 32);
        btnLogin.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(32, 118, 223));
        btnLogin.setBorder(new javax.swing.border.LineBorder(new Color(0,90,200), 2, true));
        addHoverEffect(btnLogin, new Color(32,118,223), new Color(10,90,200));
        formPanel.add(btnLogin);

        btnLogin.addActionListener(e -> login());

        // Enter key triggers login
        KeyAdapter enterToLogin = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) login(); }
        };
        usernameField.addKeyListener(enterToLogin);
        passwordField.addKeyListener(enterToLogin);
    }

    private JButton createRoleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new javax.swing.border.LineBorder(Color.GRAY, 2, true));
        btn.setBackground(UIManager.getColor("Button.background"));

        addHoverEffect(btn, UIManager.getColor("Button.background"), new Color(200, 200, 200));
        return btn;
    }

    private void addHoverEffect(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normal);
                btn.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(hover.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(hover);
            }
        });
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

    private void selectRole(String role, JButton pressed) {
        selectedRole = role;
        resetButtons();
        pressed.setBackground(new Color(140, 238, 140));
    }

    private void resetButtons() {
        Color bg = UIManager.getColor("Button.background");
        btnReg.setBackground(bg);
        btnBill.setBackground(bg);
        btnLab.setBackground(bg);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String hashed = hashPassword(password);

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        if (selectedRole == null) {
            JOptionPane.showMessageDialog(this, "Select a role first.");
            return;
        }

        java.util.Map<String, String[]> creds = new java.util.HashMap<>();
        creds.put("REGISTRATION", new String[]{"register", hashPassword("register123")});
        creds.put("BILLING", new String[]{"bill", hashPassword("bill123")});
        creds.put("LAB", new String[]{"lab", hashPassword("lab123")});

        String key = selectedRole.toUpperCase();
        if (!creds.containsKey(key)) {
            JOptionPane.showMessageDialog(this, "Unknown staff role.");
            return;
        }

        String[] roleData = creds.get(key);
        if (!username.equalsIgnoreCase(roleData[0])) {
            JOptionPane.showMessageDialog(this, "Incorrect username.");
            return;
        }
        if (!hashed.equals(roleData[1])) {
            JOptionPane.showMessageDialog(this, "Incorrect password.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Login Successful!");
        // Pass sub-role to dashboard so StaffDashboardPanel can select proper card
        DashboardUI dash = new DashboardUI("STAFF:" + key);
        dash.setVisible(true);
        dispose();
    }

    private String hashPassword(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pass.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}