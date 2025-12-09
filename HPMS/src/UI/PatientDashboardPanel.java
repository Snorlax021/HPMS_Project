package UI;

import Service.PatientService;
import Service.UserService;
import Model.Patient;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;

public class PatientDashboardPanel extends JPanel implements GlobalSearchable {
    private static final long serialVersionUID = 1L;
    // THEME (aligned with AdminDashboardPanel for consistency)
    private static final Color COLOR_BG = Color.WHITE;
    private static final Color COLOR_SIDEBAR_BG = new Color(245, 247, 250);
    private static final Color COLOR_PRIMARY = new Color(60, 120, 200);
    private static final Color COLOR_PRIMARY_HOVER = new Color(80, 140, 220);
    private static final Color COLOR_ACTIVE = new Color(100, 160, 240);
    private static final Color COLOR_BORDER = new Color(210, 215, 220);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

    // Layout + navigation
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sideNavPanel;
    private JButton btnProfile;
    private JButton btnAppointments;
    private JButton btnHistory;
    private JButton btnBills;
    private JButton btnLab;
    private JButton btnGuide;
    private JButton btnServices; // New button for Hospital Services
    private JButton btnAdmission; // NEW: Admission & Discharge button
    private JButton activeButton;
    // Tables / components for future data binding
    private JTable appointmentsTable;
    private JTable billsTable;
    private JTable labTable;
    private JTable servicesTable;
    private JTable admissionTable; // NEW: Admission & Discharge table (static data only)
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // Global search/filter state
    private String globalSearchQuery;
    private final Map<String, Map<String,String>> columnFilters = new HashMap<>();

    // Dashboard dynamic labels
    private JLabel lblUpcomingAppts;
    private JLabel lblPendingBills;
    private JLabel lblLabResults;

    // Missing profileArea field re-added
    private JTextArea profileArea;

    private final String currentUsername;
    // NEW: username label reference to control visibility
    private JLabel userTagLabel;

    // Simple in-memory profile model to retain values (can be replaced by Service/Model integration)
    private static class ProfileData {
        String name = "";
        String age = "";
        String bloodType = "";
        String gender = "";
        String address = "";
        String doctor = "";
        String email = "";
        String phone = "";
        String civilStatus = ""; // NEW: track civil status even when no linked model
    }
    // Remove static cache and use service-backed profile
    // private static final ConcurrentHashMap<String, ProfileData> PROFILE_CACHE = new ConcurrentHashMap<>();
    private ProfileData profileData;
    private final PatientService patientService = PatientService.getInstance();

    // NEW: profilePanel and profileCenter fields
    private JPanel profilePanel;
    private JPanel profileCenter;

    public PatientDashboardPanel(String username) {
        this.currentUsername = username;
        // Load profile data from PatientService using username mapping when available
        if (username != null && !username.isBlank()) {
            PatientService.PatientProfile p = patientService.getProfileByUsername(username);
            profileData = fromServiceProfile(p);
        } else {
            profileData = new ProfileData();
        }
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new BorderLayout(8, 8));
        add(createHeader(), BorderLayout.NORTH);
        add(createSideBar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
        // Set default view to My Profile (Summary removed)
        setActiveButton(btnProfile, "PROFILE");
    }

    // HEADER -----------------------------------------------------------
    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new LineBorder(COLOR_BORDER));
        header.setPreferredSize(new Dimension(0, 55));

        // Left: title only — username removed
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        left.setOpaque(false);
        // Do not add username label
        userTagLabel = null;

        JLabel title = new JLabel("Patient Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY.darker());
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);
        JButton btnHelp = new JButton("Help");
        styleSecondaryButton(btnHelp);
        btnHelp.addActionListener(e -> JOptionPane.showMessageDialog(this, "Support placeholder.", "Help", JOptionPane.INFORMATION_MESSAGE));
        right.add(btnHelp);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // SIDEBAR ---------------------------------------------------------
    private JComponent createSideBar() {
        sideNavPanel = new JPanel();
        sideNavPanel.setLayout(new BoxLayout(sideNavPanel, BoxLayout.Y_AXIS));
        sideNavPanel.setBackground(COLOR_SIDEBAR_BG);
        sideNavPanel.setBorder(new LineBorder(COLOR_BORDER));
        sideNavPanel.setPreferredSize(new Dimension(260, 0));

        // Summary removed per requirement
        // btnSummary = createNavButton("Summary", "SUMMARY");
        btnProfile = createNavButton("My Profile", "PROFILE");
        btnAppointments = createNavButton("Appointments", "APPOINTMENTS");
        btnHistory = createNavButton("Medical History", "HISTORY");
        btnBills = createNavButton("Billing Info", "BILLS");
        btnLab = createNavButton("Lab Results", "LAB");
        btnGuide = createNavButton("User Guide", "GUIDE");
        btnServices = createNavButton("Hospital Services", "SERVICES");
        btnAdmission = createNavButton("Admission & Discharge", "ADMISSION");

        // Consistent spacing
        int gap = 12;
        sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnProfile); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnAppointments); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnHistory); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnBills); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnLab); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnServices); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnAdmission); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnGuide); sideNavPanel.add(Box.createVerticalStrut(8));
        sideNavPanel.add(Box.createVerticalGlue());
        return sideNavPanel;
    }

    private JButton createNavButton(String text, String card) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Match other dashboards: taller buttons
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        b.setPreferredSize(new Dimension(Integer.MAX_VALUE, 64));
        b.setFont(FONT_NORMAL);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(COLOR_BORDER));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (b != activeButton) b.setBackground(COLOR_PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e) { if (b != activeButton) b.setBackground(Color.WHITE); }
        });
        b.addActionListener(e -> setActiveButton(b, card));
        return b;
    }

    private void setActiveButton(JButton button, String card) {
        if (activeButton != null) {
            activeButton.setBackground(Color.WHITE);
            activeButton.setForeground(Color.BLACK);
        }
        activeButton = button;
        activeButton.setBackground(COLOR_ACTIVE);
        activeButton.setForeground(Color.WHITE);
        // Username not displayed anymore
        cardLayout.show(mainContentPanel, card);
    }

    // MAIN CONTENT ----------------------------------------------------
    private JComponent createMainContent() {
        mainContentPanel = new JPanel();
        cardLayout = new CardLayout();
        mainContentPanel.setLayout(cardLayout);
        mainContentPanel.setBorder(new LineBorder(COLOR_BORDER));

        // Summary removed
        // mainContentPanel.add(buildSummaryPanel(), "SUMMARY");
        profilePanel = buildProfilePanel();
        mainContentPanel.add(profilePanel, "PROFILE");
        mainContentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        mainContentPanel.add(buildHistoryPanel(), "HISTORY");
        mainContentPanel.add(buildBillsPanel(), "BILLS");
        mainContentPanel.add(buildLabPanel(), "LAB");
        mainContentPanel.add(buildGuidePanel(), "GUIDE");
        mainContentPanel.add(buildServicesPanel(), "SERVICES");
        mainContentPanel.add(buildAdmissionPanel(), "ADMISSION");
        return mainContentPanel;
    }

    // PROFILE PANEL ---------------------------------------------------
    private JPanel buildProfilePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        // Top header with Edit at upper-right
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel hdr = sectionHeader("My Profile");
        JButton btnEdit = new JButton("Edit Profile"); styleSecondaryButton(btnEdit);
        btnEdit.addActionListener(e -> openEditProfileDialog75());
        top.add(hdr, BorderLayout.WEST);
        top.add(btnEdit, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        // Center content container which we can rebuild
        profileCenter = new JPanel(new BorderLayout());
        root.add(profileCenter, BorderLayout.CENTER);
        // Initial build
        rebuildProfileCenter(profileCenter);
        return root;
    }

    // Rebuilds the profile center grid with current data
    private void rebuildProfileCenter(JPanel center) {
        center.removeAll();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Helper: create collapsible section
        java.util.function.BiFunction<String, JComponent, JPanel> collapsible = (title, content) -> {
            JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
            JButton toggle = new JButton("\u25BC " + title); // down triangle
            styleSecondaryButton(toggle);
            toggle.setHorizontalAlignment(SwingConstants.LEFT);
            toggle.setBackground(Color.WHITE);
            toggle.setFont(FONT_SECTION);
            JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
            header.add(toggle, BorderLayout.CENTER);
            wrapper.add(header, BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout()); body.setBorder(new EmptyBorder(8, 12, 8, 12)); body.setBackground(Color.WHITE);
            body.add(content, BorderLayout.CENTER);
            wrapper.add(body, BorderLayout.CENTER);

            toggle.addActionListener(e -> {
                boolean visible = body.isVisible();
                body.setVisible(!visible);
                toggle.setText((visible ? "\u25B6" : "\u25BC") + " " + title); // right or down triangle
                wrapper.revalidate(); wrapper.repaint();
            });
            return wrapper;
        };

        // Resolve patient via current user
        String patientId = null; Patient modelPatient = null;
        if (currentUsername != null && !currentUsername.isBlank()) {
            for (Model.User u : UserService.getInstance().getAllUsers()) {
                if (currentUsername.equals(u.getUsername())) { patientId = u.getLinkedPatientId(); break; }
            }
            if (patientId != null) { modelPatient = patientService.findById(patientId).orElse(null); }
        }

        // Personal Info panel
        JPanel personalGrid = new JPanel(new GridBagLayout()); personalGrid.setBackground(Color.WHITE);
        GridBagConstraints pgbc = new GridBagConstraints(); pgbc.insets = new Insets(6,10,6,10); pgbc.anchor = GridBagConstraints.WEST; pgbc.fill = GridBagConstraints.HORIZONTAL; pgbc.gridx=0; pgbc.gridy=0; pgbc.weightx=0;
        Dimension labelW = new Dimension(160, 24);
        java.util.function.BiConsumer<String,String> addPersonal = (label, value) -> {
            JLabel l = new JLabel(label + ":"); l.setPreferredSize(labelW);
            JTextField v = new JTextField((value==null||value.isBlank())?"—":value); v.setEditable(false);
            pgbc.gridx = 0; pgbc.weightx = 0; personalGrid.add(l, pgbc);
            pgbc.gridx = 1; pgbc.weightx = 1; personalGrid.add(v, pgbc);
            pgbc.gridy++;
        };

        // Medical Info panel
        JPanel medicalGrid = new JPanel(new GridBagLayout()); medicalGrid.setBackground(Color.WHITE);
        GridBagConstraints mgbc = new GridBagConstraints(); mgbc.insets = new Insets(6,10,6,10); mgbc.anchor = GridBagConstraints.WEST; mgbc.fill = GridBagConstraints.HORIZONTAL; mgbc.gridx=0; mgbc.gridy=0; mgbc.weightx=0;
        java.util.function.BiConsumer<String,String> addMedical = (label, value) -> {
            JLabel l = new JLabel(label + ":"); l.setPreferredSize(labelW);
            JTextField v = new JTextField((value==null||value.isBlank())?"—":value); v.setEditable(false);
            mgbc.gridx = 0; mgbc.weightx = 0; medicalGrid.add(l, mgbc);
            mgbc.gridx = 1; mgbc.weightx = 1; medicalGrid.add(v, mgbc);
            mgbc.gridy++;
        };

        // Emergency Contact panel
        JPanel emergencyGrid = new JPanel(new GridBagLayout()); emergencyGrid.setBackground(Color.WHITE);
        GridBagConstraints egbc = new GridBagConstraints(); egbc.insets = new Insets(6,10,6,10); egbc.anchor = GridBagConstraints.WEST; egbc.fill = GridBagConstraints.HORIZONTAL; egbc.gridx=0; egbc.gridy=0; egbc.weightx=0;
        java.util.function.BiConsumer<String,String> addEmergency = (label, value) -> {
            JLabel l = new JLabel(label + ":"); l.setPreferredSize(labelW);
            JTextField v = new JTextField((value==null||value.isBlank())?"—":value); v.setEditable(false);
            egbc.gridx = 0; egbc.weightx = 0; emergencyGrid.add(l, egbc);
            egbc.gridx = 1; egbc.weightx = 1; emergencyGrid.add(v, egbc);
            egbc.gridy++;
        };

        // Populate data
        if (modelPatient != null) {
            String fullName = ((modelPatient.getFirstName()==null?"":modelPatient.getFirstName()) + " " + (modelPatient.getLastName()==null?"":modelPatient.getLastName())).trim();
            // Personal Info
            addPersonal.accept("Name", fullName.isEmpty()?profileData.name:fullName);
            addPersonal.accept("Age", modelPatient.getAge()==null?profileData.age:String.valueOf(modelPatient.getAge()));
            addPersonal.accept("Gender", modelPatient.getGender());
            addPersonal.accept("Civil Status", modelPatient.getCivilStatus());
            addPersonal.accept("Address", modelPatient.getAddress());
            addPersonal.accept("Contact", modelPatient.getContactNumber());
            addPersonal.accept("Email", (profileData.email==null||profileData.email.isBlank())?"—":profileData.email);
            // Medical Info
            addMedical.accept("Doctor", (profileData.doctor==null||profileData.doctor.isBlank())?"—":profileData.doctor);
            addMedical.accept("Symptoms", (patientService.getProfileByUsername(currentUsername).symptoms==null?"—":patientService.getProfileByUsername(currentUsername).symptoms));
            addMedical.accept("Blood Type", modelPatient.getBloodType());
            // Height, Weight, BMI
            PatientService.PatientProfile prof = patientService.getProfileByUsername(currentUsername);
            String heightStr = prof.heightCm==null?"—":String.format("%.1f cm", prof.heightCm);
            String weightStr = prof.weightKg==null?"—":String.format("%.1f kg", prof.weightKg);
            String bmiStr = computeBmiDisplay(prof.heightCm, prof.weightKg);
            addMedical.accept("Height", heightStr);
            addMedical.accept("Weight", weightStr);
            addMedical.accept("BMI", bmiStr);
            // Emergency
            addEmergency.accept("Name", modelPatient.getEmergencyContactName());
            addEmergency.accept("Phone Number", modelPatient.getEmergencyContactNumber());
        } else {
            // Fallback to service-backed profileData
            addPersonal.accept("Name", profileData.name);
            addPersonal.accept("Age", profileData.age);
            addPersonal.accept("Gender", profileData.gender);
            addPersonal.accept("Civil Status", profileData.civilStatus);
            addPersonal.accept("Address", profileData.address);
            addPersonal.accept("Contact", profileData.phone);
            addPersonal.accept("Email", profileData.email);
            // Medical
            PatientService.PatientProfile prof = patientService.getProfileByUsername(currentUsername);
            addMedical.accept("Doctor", profileData.doctor);
            addMedical.accept("Symptoms", prof.symptoms);
            addMedical.accept("Blood Type", profileData.bloodType);
            addMedical.accept("Height", prof.heightCm==null?"—":String.format("%.1f cm", prof.heightCm));
            addMedical.accept("Weight", prof.weightKg==null?"—":String.format("%.1f kg", prof.weightKg));
            addMedical.accept("BMI", computeBmiDisplay(prof.heightCm, prof.weightKg));
            // Emergency
            addEmergency.accept("Name", prof.emergencyContactName);
            addEmergency.accept("Phone Number", prof.emergencyContactNumber);
        }

        // Add collapsible sections to center
        center.add(collapsible.apply("Personal Info", personalGrid));
        center.add(Box.createVerticalStrut(8));
        center.add(collapsible.apply("Medical Info", medicalGrid));
        center.add(Box.createVerticalStrut(8));
        center.add(collapsible.apply("Emergency Contact", emergencyGrid));

        center.revalidate();
        center.repaint();
    }

    private String computeBmiDisplay(Double heightCm, Double weightKg) {
        if (heightCm == null || weightKg == null || heightCm <= 0 || weightKg <= 0) return "—";
        double m = heightCm / 100.0;
        double bmi = weightKg / (m * m);
        return String.format("%.1f", bmi);
    }

    private void openEditProfileDialog75() {
        // 75% screen modal for editing basic profile fields; map to PatientService.PatientProfile for now
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBorder(new EmptyBorder(12,12,12,12));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(8,8,8,8); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;
        Dimension labelW = new Dimension(140,24);
        java.util.function.BiConsumer<String,JComponent> add = (label, comp) -> { JLabel l=new JLabel(label+":"); l.setPreferredSize(labelW); gbc.gridx=0; panel.add(l,gbc); gbc.gridx=1; gbc.weightx=1; panel.add(comp,gbc); gbc.gridy++; };

        JTextField name = new JTextField(profileData.name);
        JTextField age = new JTextField(profileData.age);

        String[] genderOpts = {"Male","Female","Other","Prefer not to say"};
        JComboBox<String> gender = new JComboBox<>(genderOpts);
        if (profileData.gender != null && !profileData.gender.isBlank()) gender.setSelectedItem(profileData.gender);

        String[] bloodOpts = {"A+","A-","B+","B-","AB+","AB-","O+","O-","Unknown"};
        JComboBox<String> blood = new JComboBox<>(bloodOpts);
        if (profileData.bloodType != null && !profileData.bloodType.isBlank()) blood.setSelectedItem(profileData.bloodType);

        String[] civilOpts = {"Single","Married","Widowed"};
        JComboBox<String> civil = new JComboBox<>(civilOpts);

        JTextField phone = new JTextField(profileData.phone);
        JTextField email = new JTextField(profileData.email);
        JTextField address = new JTextField(profileData.address);
        // NEW fields
        JTextField symptoms = new JTextField(patientService.getProfileByUsername(currentUsername).symptoms==null?"":patientService.getProfileByUsername(currentUsername).symptoms);
        JSpinner height = new JSpinner(new SpinnerNumberModel(patientService.getProfileByUsername(currentUsername).heightCm==null?0.0:patientService.getProfileByUsername(currentUsername).heightCm, 0.0, 300.0, 0.5));
        JSpinner weight = new JSpinner(new SpinnerNumberModel(patientService.getProfileByUsername(currentUsername).weightKg==null?0.0:patientService.getProfileByUsername(currentUsername).weightKg, 0.0, 500.0, 0.5));
        JTextField emergName = new JTextField(patientService.getProfileByUsername(currentUsername).emergencyContactName==null?"":patientService.getProfileByUsername(currentUsername).emergencyContactName);
        JTextField emergPhone = new JTextField(patientService.getProfileByUsername(currentUsername).emergencyContactNumber==null?"":patientService.getProfileByUsername(currentUsername).emergencyContactNumber);

        add.accept("Name", name);
        add.accept("Age", age);
        add.accept("Gender", gender);
        add.accept("Blood Type", blood);
        add.accept("Civil Status", civil);
        add.accept("Phone", phone);
        add.accept("Email", email);
        add.accept("Address", address);
        add.accept("Symptoms", symptoms);
        add.accept("Height (cm)", height);
        add.accept("Weight (kg)", weight);
        add.accept("Emergency Contact", emergName);
        add.accept("Emergency Number", emergPhone);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Profile", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setLayout(new BorderLayout()); dlg.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton cancel=new JButton("Cancel"), save=new JButton("Save"); foot.add(cancel); foot.add(save); dlg.getContentPane().add(foot, BorderLayout.SOUTH);
        cancel.addActionListener(e->dlg.dispose());
        save.addActionListener(e->{
            PatientService.PatientProfile prof = patientService.getProfileByUsername(currentUsername);
            prof.name = name.getText().trim();
            prof.gender = (String) gender.getSelectedItem();
            prof.bloodType = (String) blood.getSelectedItem();
            prof.civilStatus = (String) civil.getSelectedItem();
            prof.age = age.getText().trim();
            prof.phone = phone.getText().trim();
            prof.email = email.getText().trim();
            prof.address = address.getText().trim();
            prof.symptoms = symptoms.getText().trim();
            Double h = ((Number)height.getValue()).doubleValue(); prof.heightCm = (h<=0.0)?null:h;
            Double w = ((Number)weight.getValue()).doubleValue(); prof.weightKg = (w<=0.0)?null:w;
            prof.emergencyContactName = emergName.getText().trim();
            prof.emergencyContactNumber = emergPhone.getText().trim();
            patientService.saveProfile(currentUsername, prof);
            profileData = fromServiceProfile(prof);
            if (profileCenter != null) { rebuildProfileCenter(profileCenter); }
            setActiveButton(btnProfile, "PROFILE");
            dlg.dispose();
        });
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); int w=(int)(screen.width*0.75); int h=(int)(screen.height*0.75);
        dlg.setSize(w,h); dlg.setLocationRelativeTo(this); dlg.setResizable(true); dlg.setVisible(true);
    }

    // MAIN CONTENT PANELS (APPOINTMENTS, HISTORY, BILLS, LAB, SERVICES, ADMISSION) --------------------------------
    // These methods remain unchanged, except for removal of the SUMMARY panel and any references to it
    // APPOINTMENTS PANEL ----------------------------------------------
    private JPanel buildAppointmentsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel header = new JLabel("Appointments", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker());
        root.add(header, BorderLayout.NORTH);
        String[] cols = {"Date", "Time", "Doctor", "Type"};
        Object[][] data = {{"2025-01-15", "09:00", "Dr. Smith", "Follow-up"}, {"2025-01-20", "14:30", "Dr. Adams", "Consultation"}};
        appointmentsTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });
        root.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);
        return root;
    }

    // MEDICAL HISTORY -------------------------------------------------
    private JPanel buildHistoryPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("Medical History"), BorderLayout.NORTH);
        JTextArea area = new JTextArea("History placeholder: previous diagnoses, medications, surgeries.");
        area.setEditable(false); area.setFont(FONT_NORMAL);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        return root;
    }

    // BILLS ------------------------------------------------------------
    private JPanel buildBillsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel header = new JLabel("Billing Info", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker());
        root.add(header, BorderLayout.NORTH);
        String[] cols = {"Date", "Description", "Amount", "Status"};
        Object[][] data = {{"2025-01-05", "Consultation", "$50", "Paid"},{"2025-01-07", "Lab Test", "$75", "Unpaid"}};
        billsTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });
        root.add(new JScrollPane(billsTable), BorderLayout.CENTER);
        return root;
    }

    // LAB --------------------------------------------------------------
    private JPanel buildLabPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel header = new JLabel("Lab Results", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker());
        root.add(header, BorderLayout.NORTH);
        String[] cols = {"Date", "Test", "Status"};
        Object[][] data = {{"2025-01-02", "CBC", "Completed"},{"2025-01-08", "X-Ray", "Pending"}};
        labTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });
        root.add(new JScrollPane(labTable), BorderLayout.CENTER);
        return root;
    }

    // SERVICES PANEL ---------------------------------------------------
    private JPanel buildServicesPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel header = new JLabel("Hospital Services", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker()); root.add(header, BorderLayout.NORTH);
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)); topBar.setOpaque(false);
        JButton btnSurgery = new JButton("Surgery"); styleSecondaryButton(btnSurgery);
        JButton btnRadiology = new JButton("Radiology"); styleSecondaryButton(btnRadiology);
        JButton btnPharmacy = new JButton("Pharmacy"); styleSecondaryButton(btnPharmacy);
        JButton btnPediatrics = new JButton("Pediatrics"); styleSecondaryButton(btnPediatrics);
        JButton btnCardiology = new JButton("Cardiology"); styleSecondaryButton(btnCardiology);
        JButton btnOrthopedics = new JButton("Orthopedics"); styleSecondaryButton(btnOrthopedics);
        Dimension btnSize = new Dimension(140, 34);
        for (JButton b : new JButton[]{btnSurgery,btnRadiology,btnPharmacy,btnPediatrics,btnCardiology,btnOrthopedics}) { b.setPreferredSize(btnSize); topBar.add(b);} root.add(topBar, BorderLayout.NORTH);
        JTextArea infoArea = new JTextArea(); infoArea.setEditable(false); infoArea.setFont(FONT_NORMAL); infoArea.setLineWrap(true); infoArea.setWrapStyleWord(true);
        JScrollPane infoScroll = new JScrollPane(infoArea); root.add(infoScroll, BorderLayout.CENTER);
        java.util.List<JButton> categoryButtons = java.util.Arrays.asList(btnSurgery,btnRadiology,btnPharmacy,btnPediatrics,btnCardiology,btnOrthopedics);
        Runnable resetAll = () -> { for (JButton b : categoryButtons) { b.setBackground(Color.WHITE); b.setForeground(Color.BLACK);} };
        java.util.function.Consumer<JButton> setActive = (btn) -> { resetAll.run(); btn.setBackground(COLOR_ACTIVE); btn.setForeground(Color.WHITE);} ;

        // Add hover and pressed effects while respecting active state
        for (JButton b : categoryButtons) {
            b.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!COLOR_ACTIVE.equals(b.getBackground())) {
                        b.setBackground(COLOR_PRIMARY_HOVER);
                    }
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!COLOR_ACTIVE.equals(b.getBackground())) {
                        b.setBackground(Color.WHITE);
                    }
                }
                @Override public void mousePressed(MouseEvent e) {
                    // Darken on press for feedback; keep active buttons as is
                    if (!COLOR_ACTIVE.equals(b.getBackground())) {
                        b.setBackground(COLOR_PRIMARY.darker());
                        b.setForeground(Color.WHITE);
                    }
                }
                @Override public void mouseReleased(MouseEvent e) {
                    if (!COLOR_ACTIVE.equals(b.getBackground())) {
                        // If mouse is still over the button, show hover; otherwise, reset
                        Point p = e.getPoint();
                        if (p.x >= 0 && p.y >= 0 && p.x < b.getWidth() && p.y < b.getHeight()) {
                            b.setBackground(COLOR_PRIMARY_HOVER);
                            b.setForeground(Color.BLACK);
                        } else {
                            b.setBackground(Color.WHITE);
                            b.setForeground(Color.BLACK);
                        }
                    }
                }
            });
        }

        btnSurgery.addActionListener(e -> { setActive.accept(btnSurgery); infoArea.setText("Surgery Department\n\nLead Surgeon: Dr. Anthony Rivera\nSpecialties: General surgery, minimally invasive procedures.\nAvailability: Mon-Fri, 7:00 AM - 6:00 PM.\nContact: surgery@hospital.example");});
        btnRadiology.addActionListener(e -> { setActive.accept(btnRadiology); infoArea.setText("Radiology Department\n\nChief Radiologist: Dr. Sophia Nguyen\nServices: X-Ray, MRI, CT, Ultrasound.\nAvailability: Mon-Sat, 8:00 AM - 8:00 PM.\nContact: radiology@hospital.example");});
        btnPharmacy.addActionListener(e -> { setActive.accept(btnPharmacy); infoArea.setText("Pharmacy\n\nHead Pharmacist: Mr. Daniel Perez, RPh\nServices: Prescriptions, medication counseling, refills.\nAvailability: Mon-Sun, 9:00 AM - 9:00 PM.\nContact: pharmacy@hospital.example");});
        btnPediatrics.addActionListener(e -> { setActive.accept(btnPediatrics); infoArea.setText("Pediatrics\n\nAttending Pediatrician: Dr. Emily Carter\nServices: Well-child visits, immunizations, acute care.\nAvailability: Mon-Fri, 9:00 AM - 5:00 PM.\nContact: pediatrics@hospital.example");});
        btnCardiology.addActionListener(e -> { setActive.accept(btnCardiology); infoArea.setText("Cardiology\n\nConsultant Cardiologist: Dr. Raj Patel\nServices: ECG, echocardiogram, stress tests, heart health.\nAvailability: Mon-Fri, 8:00 AM - 4:00 PM.\nContact: cardiology@hospital.example");});
        btnOrthopedics.addActionListener(e -> { setActive.accept(btnOrthopedics); infoArea.setText("Orthopedics\n\nOrthopedic Surgeon: Dr. Laura Kim\nServices: Bone/joint care, sports injuries, rehabilitation.\nAvailability: Mon-Fri, 10:00 AM - 6:00 PM.\nContact: ortho@hospital.example");});
        setActive.accept(btnSurgery);
        return root;
    }

    // ADMISSION & DISCHARGE PANEL -------------------------------------
    private JPanel buildAdmissionPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel header = new JLabel("Admission & Discharge", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker()); root.add(header, BorderLayout.NORTH);
        String[] cols = {"Type", "Date", "Department", "Status"};
        Object[][] data = {{"Admission", "2025-01-03", "General Medicine", "Completed"},{"Discharge", "2025-01-07", "General Medicine", "Completed"}};
        admissionTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });
        root.add(new JScrollPane(admissionTable), BorderLayout.CENTER);
        return root;
    }

    // GUIDE PANEL -----------------------------------------------------
    private JPanel buildGuidePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("User Guide"), BorderLayout.NORTH);
        JTextArea area = new JTextArea("Welcome to the Patient Dashboard. Use the sidebar to navigate and the top toolbar in Services to view department info.");
        area.setEditable(false); area.setFont(FONT_NORMAL); area.setLineWrap(true); area.setWrapStyleWord(true);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        return root;
    }

    // SHARED HELPERS --------------------------------------------------
    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setFont(FONT_SECTION);
        l.setForeground(COLOR_PRIMARY.darker());
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    private void styleToolbarButton(JToolBar bar, String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(FONT_NORMAL);
        b.addActionListener(e -> action.run());
        bar.add(b);
    }
    private void styleSecondaryButton(JButton b) {
        b.setFont(FONT_NORMAL);
        b.setBackground(Color.WHITE);
        b.setBorder(new LineBorder(COLOR_BORDER));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(COLOR_PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
    }

    // DIALOG ACTIONS --------------------------------------------------
    private void openViewAppointment() {
        int row = appointmentsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
        DefaultTableModel m = (DefaultTableModel) appointmentsTable.getModel();
        String info = String.format("Date: %s\nTime: %s\nDoctor: %s\nType: %s", m.getValueAt(row,0), m.getValueAt(row,1), m.getValueAt(row,2), m.getValueAt(row,3));
        JOptionPane.showMessageDialog(this, info, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openEditProfileDialog() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField name = new JTextField(profileData.name);
        JTextField age = new JTextField(profileData.age);
        JTextField blood = new JTextField(profileData.bloodType);
        JTextField gender = new JTextField(profileData.gender);
        JTextField address = new JTextField(profileData.address);
        JTextField doctor = new JTextField(profileData.doctor);
        JTextField email = new JTextField(profileData.email);
        JTextField phone = new JTextField(profileData.phone);
        panel.add(new JLabel("Name:")); panel.add(name);
        panel.add(new JLabel("Age:")); panel.add(age);
        panel.add(new JLabel("Blood Type:")); panel.add(blood);
        panel.add(new JLabel("Gender:")); panel.add(gender);
        panel.add(new JLabel("Address:")); panel.add(address);
        panel.add(new JLabel("Doctor:")); panel.add(doctor);
        panel.add(new JLabel("Email:")); panel.add(email);
        panel.add(new JLabel("Phone:")); panel.add(phone);
        int res;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Profile", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setLayout(new BorderLayout()); JScrollPane sp = new JScrollPane(panel); dlg.getContentPane().add(sp, BorderLayout.CENTER);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton ok = new JButton("Save"); JButton cancel = new JButton("Cancel"); foot.add(cancel); foot.add(ok); dlg.getContentPane().add(foot, BorderLayout.SOUTH);
        final int[] picked = {JOptionPane.CANCEL_OPTION}; ok.addActionListener(e -> { picked[0] = JOptionPane.OK_OPTION; dlg.dispose(); }); cancel.addActionListener(e -> { picked[0] = JOptionPane.CANCEL_OPTION; dlg.dispose(); });
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); dlg.setUndecorated(true); dlg.setBounds(0,0, screen.width, screen.height); dlg.setVisible(true); res = picked[0];
        if (res == JOptionPane.OK_OPTION) {
            // Update local model
            profileData.name = name.getText().trim();
            profileData.age = age.getText().trim();
            profileData.bloodType = blood.getText().trim();
            profileData.gender = gender.getText().trim();
            profileData.address = address.getText().trim();
            profileData.doctor = doctor.getText().trim();
            profileData.email = email.getText().trim();
            profileData.phone = new JTextField(profileData.phone).getText().trim();
            // Save via service
            if (currentUsername != null && !currentUsername.isBlank()) {
                patientService.saveProfile(currentUsername, toServiceProfile(profileData));
            }
            refreshProfileAreaFromModel();
        }
    }

    private ProfileData fromServiceProfile(PatientService.PatientProfile p) {
        ProfileData d = new ProfileData();
        d.name = p.name; d.age = p.age; d.bloodType = p.bloodType; d.gender = p.gender;
        d.address = p.address; d.doctor = p.doctor; d.email = p.email; d.phone = p.phone;
        d.civilStatus = p.civilStatus; // NEW
        return d;
    }
    private PatientService.PatientProfile toServiceProfile(ProfileData d) {
        PatientService.PatientProfile p = new PatientService.PatientProfile();
        p.name = d.name; p.age = d.age; p.bloodType = d.bloodType; p.gender = d.gender;
        p.address = d.address; p.doctor = d.doctor; p.email = d.email; p.phone = d.phone;
        p.civilStatus = d.civilStatus; // NEW
        return p;
    }

    // PUBLIC UPDATE API -----------------------------------------------
    public void updateSummary(int upcoming, int pendingBills, int labReady) {
        if (lblUpcomingAppts != null) lblUpcomingAppts.setText(String.valueOf(upcoming));
        if (lblPendingBills != null) lblPendingBills.setText(String.valueOf(pendingBills));
        if (lblLabResults != null) lblLabResults.setText(String.valueOf(labReady));
    }
    public JTextArea getProfileArea() { return profileArea; }
    public JTable getAppointmentsTable() { return appointmentsTable; }
    public JTable getBillsTable() { return billsTable; }
    public JTable getLabTable() { return labTable; }

    @Override
    public Map<String, JTable> getSearchableTables() {
        Map<String, JTable> map = new LinkedHashMap<>();
        if (appointmentsTable != null) map.put("appointments", appointmentsTable);
        if (billsTable != null) map.put("bills", billsTable);
        if (labTable != null) map.put("lab", labTable);
        if (servicesTable != null) map.put("services", servicesTable);
        // NEW: expose admission table for global search
        // if (admissionTable != null) map.put("admission", admissionTable);
        return map;
    }
    @Override
    public void applyGlobalSearch(String query) { globalSearchQuery = (query==null||query.isBlank())?null:query.trim(); refreshAllFilters(); }
    @Override
    public void clearGlobalSearch() { globalSearchQuery = null; refreshAllFilters(); }
    @Override
    public void applyGlobalFilter(String tableName, String columnName, String value) {
        if (tableName==null||columnName==null) return;
        Map<String,String> map = columnFilters.computeIfAbsent(tableName,k->new HashMap<>());
        if (value==null||value.isBlank()) { map.remove(columnName); if(map.isEmpty()) columnFilters.remove(tableName);} else map.put(columnName,value.trim());
        JTable t = getSearchableTables().get(tableName); if (t!=null) applyFiltersToTable(tableName,t);
    }
    @Override
    public void clearGlobalFilter() { columnFilters.clear(); refreshAllFilters(); }
    private void refreshAllFilters(){ getSearchableTables().forEach(this::applyFiltersToTable); }
    @SuppressWarnings("unchecked")
    private void applyFiltersToTable(String logicalName, JTable table){
        if (table.getRowSorter()==null) table.setRowSorter(new TableRowSorter<>(table.getModel()));
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        List<RowFilter<TableModel,Object>> filters = new ArrayList<>();
        if (globalSearchQuery!=null){ final String q=globalSearchQuery.toLowerCase(); filters.add(new RowFilter<TableModel,Object>(){ @Override public boolean include(Entry<? extends TableModel, ? extends Object> entry){ for(int i=0;i<entry.getValueCount();i++){ Object v=entry.getValue(i); if(v!=null && v.toString().toLowerCase().contains(q)) return true;} return false; }}); }
        Map<String,String> colMap = columnFilters.get(logicalName);
        if (colMap!=null){ for(Map.Entry<String,String> e: colMap.entrySet()){ String colName=e.getKey(); String val=e.getValue(); if(val==null||val.isBlank()) continue; int colIndex; try{ colIndex=table.getColumnModel().getColumnIndex(colName);} catch(IllegalArgumentException ex){ continue;} final String qv=val.toLowerCase(); filters.add(new RowFilter<TableModel,Object>(){ @Override public boolean include(Entry<? extends TableModel, ? extends Object> entry){ Object v=entry.getValue(colIndex); return v!=null && v.toString().toLowerCase().contains(qv);} }); }}
        if (filters.isEmpty()) sorter.setRowFilter(null); else if(filters.size()==1) sorter.setRowFilter(filters.get(0)); else sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    // FILTERING LOGIC FOR APPOINTMENTS TABLE ---------------------------
    private void filterAppointmentsTable(String query) {
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) appointmentsTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(appointmentsTable.getModel());
            appointmentsTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 2, 3));
        }
    }
    // FILTERING LOGIC FOR BILLS TABLE ---------------------------------
    private void filterBillsTable(String query) {
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) billsTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(billsTable.getModel());
            billsTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Include Description (1) and Status (3) in filtering
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 3));
        }
    }
    // FILTERING LOGIC FOR LAB TABLE ---------------------------------
    private void filterLabTable(String query) {
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) labTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(labTable.getModel());
            labTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2));
        }
    }
    // FILTERING LOGIC FOR SERVICES TABLE ---------------------------------
    private void filterServicesTable(String query) {
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) servicesTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(servicesTable.getModel());
            servicesTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 0, 1));
        }
    }

    // Renders profileArea text from profileData
    private void refreshProfileAreaFromModel() {
        String text = "PERSONAL INFO\n" +
                "Name: " + profileData.name + "\n" +
                "Age: " + profileData.age + "\n" +
                "Blood Type: " + profileData.bloodType + "\n" +
                "Gender: " + profileData.gender + "\n" +
                "Address: " + profileData.address + "\n" +
                "Doctor: " + profileData.doctor + "\n\n" +
                "CONTACT INFO\n" +
                "Email: " + profileData.email + "\n" +
                "Phone: " + profileData.phone + "\n";
        if (profileArea != null) profileArea.setText(text);
    }
}
