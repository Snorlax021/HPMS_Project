package UI;

import Service.PatientService;
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
    private JButton btnSummary;
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
    }
    // Remove static cache and use service-backed profile
    // private static final ConcurrentHashMap<String, ProfileData> PROFILE_CACHE = new ConcurrentHashMap<>();
    private ProfileData profileData;
    private final PatientService patientService = PatientService.getInstance();

    public PatientDashboardPanel(String username) {
        this.currentUsername = username;
        // Restore original behavior: no constructor catch-all
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
        setActiveButton(btnSummary, "SUMMARY");
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
        sideNavPanel.setPreferredSize(new Dimension(190, 0));

        btnSummary = createNavButton("Summary", "SUMMARY");
        btnProfile = createNavButton("My Profile", "PROFILE");
        btnAppointments = createNavButton("Appointments", "APPOINTMENTS");
        btnHistory = createNavButton("Medical History", "HISTORY");
        btnBills = createNavButton("Billing Info", "BILLS");
        btnLab = createNavButton("Lab Results", "LAB");
        btnGuide = createNavButton("User Guide", "GUIDE");
        btnServices = createNavButton("Hospital Services", "SERVICES");
        btnAdmission = createNavButton("Admission & Discharge", "ADMISSION"); // Re-add Admission & Discharge button

        sideNavPanel.add(Box.createVerticalStrut(6));
        sideNavPanel.add(btnSummary);
        sideNavPanel.add(btnProfile);
        sideNavPanel.add(btnAppointments);
        sideNavPanel.add(btnHistory);
        sideNavPanel.add(btnBills);
        sideNavPanel.add(btnLab);
        sideNavPanel.add(btnServices);
        sideNavPanel.add(btnAdmission); // Insert Admission button before Guide
        sideNavPanel.add(btnGuide); // Add new button to sidebar
        sideNavPanel.add(Box.createVerticalGlue());
        return sideNavPanel;
    }

    private JButton createNavButton(String text, String card) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
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

        mainContentPanel.add(buildSummaryPanel(), "SUMMARY");
        mainContentPanel.add(buildProfilePanel(), "PROFILE");
        mainContentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        mainContentPanel.add(buildHistoryPanel(), "HISTORY");
        mainContentPanel.add(buildBillsPanel(), "BILLS");
        mainContentPanel.add(buildLabPanel(), "LAB");
        mainContentPanel.add(buildGuidePanel(), "GUIDE");
        mainContentPanel.add(buildServicesPanel(), "SERVICES");
        mainContentPanel.add(buildAdmissionPanel(), "ADMISSION"); // Re-add Admission & Discharge panel/card
        return mainContentPanel;
    }

    // SUMMARY PANEL ---------------------------------------------------
    private JPanel buildSummaryPanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel header = sectionHeader("Dashboard Summary");
        root.add(header, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 12, 12));
        statsGrid.setOpaque(false);
        lblUpcomingAppts = createStatLabel("Upcoming Appts", "2");
        lblPendingBills = createStatLabel("Pending Bills", "1");
        lblLabResults = createStatLabel("Lab Results Ready", "0");
        statsGrid.add(wrapStat("Upcoming Appointments", lblUpcomingAppts));
        statsGrid.add(wrapStat("Pending Bills", lblPendingBills));
        statsGrid.add(wrapStat("Lab Results Ready", lblLabResults));
        root.add(statsGrid, BorderLayout.CENTER);

        JTextArea info = new JTextArea("Overview of your health and activities. Use the navigation to explore more.");
        info.setFont(FONT_NORMAL);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBorder(new EmptyBorder(8, 12, 8, 12));
        root.add(new JScrollPane(info), BorderLayout.SOUTH);
        return root;
    }

    private JLabel createStatLabel(String name, String value) {
        JLabel l = new JLabel(value, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(COLOR_PRIMARY);
        return l;
    }
    private JPanel wrapStat(String titleText, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(COLOR_BORDER));
        JLabel t = new JLabel(titleText, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(COLOR_PRIMARY.darker());
        t.setBorder(new EmptyBorder(6, 6, 0, 6));
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    // PROFILE PANEL ---------------------------------------------------
    private JPanel buildProfilePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("My Profile"), BorderLayout.NORTH);
        profileArea = new JTextArea();
        profileArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        profileArea.setEditable(false);
        refreshProfileAreaFromModel();
        root.add(new JScrollPane(profileArea), BorderLayout.CENTER);
        JButton btnEdit = new JButton("Edit Profile");
        styleSecondaryButton(btnEdit);
        btnEdit.addActionListener(e -> openEditProfileDialog());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnEdit);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    // APPOINTMENTS PANEL ----------------------------------------------
    private JPanel buildAppointmentsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Appointments", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Appointments:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Date", "Time", "Doctor", "Type"};
        Object[][] data = {{"2025-01-15", "09:00", "Dr. Smith", "Follow-up"}, {"2025-01-20", "14:30", "Dr. Adams", "Consultation"}};
        appointmentsTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterAppointmentsTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterAppointmentsTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterAppointmentsTable(searchField.getText()); }
        });

        root.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        // Remove mutating toolbar actions, keep only View
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "View", this::openViewAppointment);
        root.add(toolbar, BorderLayout.SOUTH);
        return root;
    }

    // MEDICAL HISTORY -------------------------------------------------
    private JPanel buildHistoryPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("Medical History"), BorderLayout.NORTH);
        JTextArea area = new JTextArea("History placeholder: previous diagnoses, medications, surgeries.");
        area.setEditable(false);
        area.setFont(FONT_NORMAL);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        return root;
    }

    // BILLS ------------------------------------------------------------
    private JPanel buildBillsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Billing Info", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Bills:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        // Updated columns: add Status next to Amount
        String[] cols = {"Date", "Description", "Amount", "Status"};
        Object[][] data = {
            {"2025-01-05", "Consultation", "$50", "Paid"},
            {"2025-01-07", "Lab Test", "$75", "Unpaid"}
        };
        billsTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });

        // Add search listener (include Status column index 3)
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterBillsTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterBillsTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterBillsTable(searchField.getText()); }
        });

        root.add(new JScrollPane(billsTable), BorderLayout.CENTER);
        // No footer actions in view-only mode
        return root;
    }

    // LAB --------------------------------------------------------------
    private JPanel buildLabPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Lab Results", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Lab Results:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Date", "Test", "Status"};
        Object[][] data = {{"2025-01-02", "CBC", "Completed"}, {"2025-01-08", "X-Ray", "Pending"}};
        labTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterLabTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterLabTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterLabTable(searchField.getText()); }
        });

        root.add(new JScrollPane(labTable), BorderLayout.CENTER);
        return root;
    }

    // SERVICES PANEL ---------------------------------------------------
    private JPanel buildServicesPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Hospital Services", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(0, 2, 10, 10));
        nav.setOpaque(false);

        JButton btnSurgery = createNavButton("Surgery", "SERVICES");
        JButton btnRadiology = createNavButton("Radiology", "SERVICES");
        JButton btnPharmacy = createNavButton("Pharmacy", "SERVICES");
        JButton btnPediatrics = createNavButton("Pediatrics", "SERVICES");
        JButton btnCardiology = createNavButton("Cardiology", "SERVICES");
        JButton btnOrthopedics = createNavButton("Orthopedics", "SERVICES");

        java.util.List<JButton> categoryButtons = java.util.Arrays.asList(
            btnSurgery, btnRadiology, btnPharmacy, btnPediatrics, btnCardiology, btnOrthopedics
        );
        for (JButton b : categoryButtons) nav.add(b);

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(FONT_NORMAL);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        JScrollPane infoScroll = new JScrollPane(infoArea);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);
        center.add(nav);
        center.add(infoScroll);
        root.add(center, BorderLayout.CENTER);

        // Helper to set active highlight for service buttons
        Runnable resetAll = () -> {
            for (JButton b : categoryButtons) { b.setBackground(Color.WHITE); b.setForeground(Color.BLACK); }
        };
        java.util.function.Consumer<JButton> setActive = (btn) -> {
            resetAll.run();
            btn.setBackground(COLOR_ACTIVE);
            btn.setForeground(Color.WHITE);
        };

        btnSurgery.addActionListener(e -> { setActive.accept(btnSurgery); infoArea.setText(
            "Surgery Department\n\n" +
            "Lead Surgeon: Dr. Anthony Rivera\n" +
            "Specialties: General surgery, minimally invasive procedures.\n" +
            "Availability: Mon-Fri, 7:00 AM - 6:00 PM.\n" +
            "Contact: surgery@hospital.example"); });

        btnRadiology.addActionListener(e -> { setActive.accept(btnRadiology); infoArea.setText(
            "Radiology Department\n\n" +
            "Chief Radiologist: Dr. Sophia Nguyen\n" +
            "Services: X-Ray, MRI, CT, Ultrasound.\n" +
            "Availability: Mon-Sat, 8:00 AM - 8:00 PM.\n" +
            "Contact: radiology@hospital.example"); });

        btnPharmacy.addActionListener(e -> { setActive.accept(btnPharmacy); infoArea.setText(
            "Pharmacy\n\n" +
            "Head Pharmacist: Mr. Daniel Perez, RPh\n" +
            "Services: Prescriptions, medication counseling, refills.\n" +
            "Availability: Mon-Sun, 9:00 AM - 9:00 PM.\n" +
            "Contact: pharmacy@hospital.example"); });

        btnPediatrics.addActionListener(e -> { setActive.accept(btnPediatrics); infoArea.setText(
            "Pediatrics\n\n" +
            "Attending Pediatrician: Dr. Emily Carter\n" +
            "Services: Well-child visits, immunizations, acute care.\n" +
            "Availability: Mon-Fri, 9:00 AM - 5:00 PM.\n" +
            "Contact: pediatrics@hospital.example"); });

        btnCardiology.addActionListener(e -> { setActive.accept(btnCardiology); infoArea.setText(
            "Cardiology\n\n" +
            "Consultant Cardiologist: Dr. Raj Patel\n" +
            "Services: ECG, echocardiogram, stress tests, heart health.\n" +
            "Availability: Mon-Fri, 8:00 AM - 4:00 PM.\n" +
            "Contact: cardiology@hospital.example"); });

        btnOrthopedics.addActionListener(e -> { setActive.accept(btnOrthopedics); infoArea.setText(
            "Orthopedics\n\n" +
            "Orthopedic Surgeon: Dr. Laura Kim\n" +
            "Services: Bone/joint care, sports injuries, rehabilitation.\n" +
            "Availability: Mon-Fri, 10:00 AM - 6:00 PM.\n" +
            "Contact: ortho@hospital.example"); });

        // Default selection
        setActive.accept(btnSurgery);
        infoArea.setText(
            "Surgery Department\n\n" +
            "Lead Surgeon: Dr. Anthony Rivera\n" +
            "Specialties: General surgery, minimally invasive procedures.\n" +
            "Availability: Mon-Fri, 7:00 AM - 6:00 PM.\n" +
            "Contact: surgery@hospital.example");

        return root;
    }

    // ADMISSION & DISCHARGE PANEL -------------------------------------
    private JPanel buildAdmissionPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Admission & Discharge", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Records:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Type", "Date", "Department", "Status"};
        Object[][] data = {
            {"Admission", "2025-01-03", "General Medicine", "Completed"},
            {"Discharge", "2025-01-07", "General Medicine", "Completed"},
            {"Admission", "2025-02-10", "Orthopedics", "Scheduled"},
            {"Admission", "2025-03-12", "Cardiology", "In Progress"},
            {"Discharge", "2025-03-18", "Cardiology", "Completed"}
        };
        admissionTable = new JTable(new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        });

        // Add search listener (local only)
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void apply(String q) {
                TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) admissionTable.getRowSorter();
                if (sorter == null) {
                    sorter = new TableRowSorter<>(admissionTable.getModel());
                    admissionTable.setRowSorter(sorter);
                }
                if (q == null || q.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + q.trim(), 0, 2, 3));
                }
            }
            public void insertUpdate(DocumentEvent e) { apply(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { apply(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { apply(searchField.getText()); }
        });

        root.add(new JScrollPane(admissionTable), BorderLayout.CENTER);

        JTextArea info = new JTextArea(
            "This module shows sample admission and discharge records."
        );
        info.setFont(FONT_NORMAL);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBorder(new EmptyBorder(8, 12, 8, 12));
        root.add(new JScrollPane(info), BorderLayout.SOUTH);
        return root;
    }

    // GUIDE PANEL -----------------------------------------------------
    private JPanel buildGuidePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("User Guide"), BorderLayout.NORTH);
        JTextArea area = new JTextArea(
            "Welcome to the Patient Dashboard.\n\n" +
            "• Use the sidebar to navigate between Summary, Profile, Appointments, Bills, Lab Results, Services, and Admission & Discharge.\n" +
            "• Use the search boxes at the top of tables to quickly filter information.\n" +
            "• Edit Profile lets you update your personal and contact details.\n\n" +
            "For support, click Help in the header."
        );
        area.setEditable(false);
        area.setFont(FONT_NORMAL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
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
        int res = JOptionPane.showConfirmDialog(this, panel, "Edit Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            // Update local model
            profileData.name = name.getText().trim();
            profileData.age = age.getText().trim();
            profileData.bloodType = blood.getText().trim();
            profileData.gender = gender.getText().trim();
            profileData.address = address.getText().trim();
            profileData.doctor = doctor.getText().trim();
            profileData.email = email.getText().trim();
            profileData.phone = phone.getText().trim();
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
        return d;
    }
    private PatientService.PatientProfile toServiceProfile(ProfileData d) {
        PatientService.PatientProfile p = new PatientService.PatientProfile();
        p.name = d.name; p.age = d.age; p.bloodType = d.bloodType; p.gender = d.gender;
        p.address = d.address; p.doctor = d.doctor; p.email = d.email; p.phone = d.phone;
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
