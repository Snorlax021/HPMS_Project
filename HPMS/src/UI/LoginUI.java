package UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Model.Role;
import Model.User;
import Service.UserService;

import java.util.Arrays;
import java.util.Optional;
import javax.swing.JFrame;
import UI.AdminDashboardPanel;
import UI.DoctorDashboardPanel;
import UI.StaffDashboardPanel;
import UI.PatientDashboardPanel;

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
        // start maximized/fullscreen and allow resizing
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        // Use a BorderLayout so we can show a larger information panel alongside the login form
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(0, 0, 20, 0));
        setContentPane(contentPane);

        JPanel panelTop = new JPanel();
        panelTop.setBackground(new Color(32, 118, 223));
        panelTop.setPreferredSize(new Dimension(0, 80));
        contentPane.add(panelTop, BorderLayout.NORTH);
        panelTop.setLayout(null);
        
        JLabel lblHeader = new JLabel("\t\t\t\t\tHopewell General Hospital System");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Tahoma", Font.BOLD, 20));
        lblHeader.setBounds(20, 20, 400, 35);
        panelTop.add(lblHeader);
        
        // form panel (left)
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(173, 216, 230));
        formPanel.setPreferredSize(new Dimension(520, 400));
        
        // keep null layout for existing coordinates so minimal changes needed
        formPanel.setLayout(null);
        contentPane.add(formPanel, BorderLayout.WEST);
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

        // informational panel (right)
        JTextPane infoPane = new JTextPane();
        infoPane.setEditable(false);
        infoPane.setContentType("text/plain");
        infoPane.setFont(new Font("Tahoma", Font.PLAIN, 18));
        infoPane.setText(buildInfoText());

        JScrollPane infoScroll = new JScrollPane(infoPane);
        infoScroll.setBorder(BorderFactory.createTitledBorder("About Hopewell General Hospital System"));
        
        // Create center panel and add infoScroll to CENTER
        JPanel centerPanel = new JPanel(new BorderLayout());
        contentPane.add(centerPanel, BorderLayout.CENTER);
        centerPanel.add(infoScroll, BorderLayout.CENTER);
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
        // Open the main DashboardUI which will show a placeholder and can attempt to load the full role panel
        DashboardUI dash = new DashboardUI((actualRole == Role.PATIENT) ? "USER" : actualRole.name(), false, username);
        dash.setVisible(true);
        dispose();
    }

    private String buildInfoText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hopewell General Hospital System\n\n");
        sb.append("Overview:\n");
        sb.append("  This is a HPMS it's a lightweight practice management application designed to help hospital manage patients, staff, appointments, and basic reporting.\n\n");

        sb.append("Doctors:\n");
        sb.append("  - Dr. Marcus Alvarado — Orthopedic Surgeon, Age 47\n");
        sb.append("      Achievement: Led the hospital’s first minimally invasive spine surgery program, cutting recovery time by 40%.\n");
        sb.append("  - Dr. Helena Cruz — Pediatrician, Age 39\n");
        sb.append("      Achievement: Founded the Child Wellness Outreach Program, giving free care to 2,000+ children.\n");
        sb.append("  - Dr. Samuel Dizon — Cardiologist, Age 52\n");
        sb.append("      Achievement: Saved the hospital’s first high-risk patient with an emergency angioplasty, improving cardiac protocols.\n");
        sb.append("  - Dr. Fiona Reyes — Dermatologist, Age 34\n");
        sb.append("      Achievement: Developed a new eczema treatment protocol now used across the department.\n");
        sb.append("  - Dr. Adrian Velasco — Neurologist, Age 45\n");
        sb.append("      Achievement: Created the Stroke Rapid Response Team, reducing treatment delays by 60%.\n");
        sb.append("  - Dr. Clarisse Montemayor — OB-GYN, Age 41\n");
        sb.append("      Achievement: Led the Safe Birth Initiative, achieving a 0% maternal mortality rate for 3 years.\n");
        sb.append("  - Dr. Jordan Estrella — General Surgeon, Age 50\n");
        sb.append("      Achievement: Completed 500+ successful laparoscopic surgeries, reducing need for invasive procedures.\n");
        sb.append("  - Dr. Bianca Soriano — Radiologist, Age 36\n");
        sb.append("      Achievement: Modernized the imaging department with digital systems, cutting patient wait times in half.\n");
        sb.append("  - Dr. Leonard Quinto — Internal Medicine Specialist, Age 48\n");
        sb.append("      Achievement: Designed a monitoring protocol that reduced hospital readmissions by 30%.\n");
        sb.append("  - Dr. Elisa Navarro — Emergency Medicine Physician, Age 33\n");
        sb.append("      Achievement: Implemented the Emergency Fast-Track System, improving ER workflow and treatment times.\n\n");
        
        sb.append("Staff:\n");
        sb.append("  - Create and manage staff accounts with role-based access control.\n");
        sb.append("  - Manage clinic resources and patient intake.\n\n");

        sb.append("Patient Registration:\n");
        sb.append("  - Register new patients with contact and medical details.\n");
        sb.append("  - Update patient demographics and view history.\n\n");

        sb.append("Appointment Scheduling:\n");
        sb.append("  - Create, reschedule, and cancel appointments.\n");
        sb.append("  - Book appointments by doctor, date, and available slots.\n\n");

        sb.append("Department Scheduling:\n");
        sb.append("  - Emergency Department (ER): Open 24/7.\n");
        sb.append("  - Orthopedics (Ortho): 8:00 AM – 10:00 PM (Mon–Sat), closed Sunday.\n");
        sb.append("  - Pediatrics: 8:00 AM – 5:00 PM (Mon–Fri), 8:00 AM – 12:00 PM (Sat), closed Sunday.\n");
        sb.append("  - Radiology / Imaging: 7:00 AM – 7:00 PM (Mon–Fri), 8:00 AM – 2:00 PM (Sat), closed Sunday.\n");
        sb.append("  - Laboratory: 6:00 AM – 6:00 PM (Mon–Fri), 6:00 AM – 12:00 PM (Sat), on-call overnight.\n");
        sb.append("  - Pharmacy: 8:00 AM – 8:00 PM (Mon–Sat), 9:00 AM – 5:00 PM (Sun).\n");
        sb.append("  - General Medicine: 8:00 AM – 5:00 PM (Mon–Fri), closed weekends.\n");
        sb.append("  - Surgery Department: Operating 7:00 AM – 3:00 PM (Mon–Fri), emergency surgery 24/7.\n");
        sb.append("  - OB-GYN: 8:00 AM – 6:00 PM (Mon–Fri), 8:00 AM – 12:00 PM (Sat); maternity available 24/7.\n");
        sb.append("  - Dental Clinic: 9:00 AM – 4:00 PM (Mon–Fri), closed weekends.\n");
        sb.append("  - Dermatology: 10:00 AM – 4:00 PM (Mon–Fri), closed weekends.\n");
        sb.append("  - Cardiology: 8:00 AM – 5:00 PM (Mon–Fri), closed weekends.\n");
        sb.append("  - Neurology: 9:00 AM – 4:00 PM (Mon–Fri).\n");
        sb.append("  - Physical Therapy / Rehab: 8:00 AM – 6:00 PM (Mon–Sat), closed Sunday.\n\n");
        
        sb.append("How to get started:\n");
        sb.append("  1) Log in with your provided credentials or use demo accounts.\n");
        sb.append("  2) Use the dashboard to access features according to your role (Patient/Staff/Admin).\n");
        sb.append("  3) For demo purposes, sample users and data are created on first run.\n");

        sb.append("\nFor support or feature requests, contact the development team.");
        sb.append("\nEmail: info@hopewelgeneralhospital.com (for general inquiries)\r\n"
        		+ "Phone: +1-800-555-HPMS (for support)\r\n"
        		+ "Address: 123 Health St, Lipa City, Philippines\r\n"
    			+ "Website: www.hopewellgeneralhospital.com");
        
        sb.append("\n© 2024 Hopewell General Hospital. All rights reserved.");
	
        		
        return sb.toString();
    }

    // Allow launching directly for testing/demo
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            LoginUI ui = new LoginUI();
            ui.setVisible(true);
        });
    }
}