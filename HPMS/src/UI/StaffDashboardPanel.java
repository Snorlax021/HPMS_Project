package UI;

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
import Service.PatientService;
import Service.AppointmentService;
import Service.UserService;
import Model.Patient;
import Model.User;
import Model.Role;
import Model.Appointment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Period;
import Controller.StaffController;

public class StaffDashboardPanel extends JPanel implements GlobalSearchable {
    private static final long serialVersionUID = 1L;
    // THEME (aligned with Admin/Doctor/Patient dashboards)
    private static final Color COLOR_BG = Color.WHITE;
    private static final Color COLOR_SIDEBAR_BG = new Color(245, 247, 250);
    private static final Color COLOR_PRIMARY = new Color(60, 120, 200);
    private static final Color COLOR_PRIMARY_HOVER = new Color(80, 140, 220);
    private static final Color COLOR_ACTIVE = new Color(100, 160, 240);
    private static final Color COLOR_BORDER = new Color(210, 215, 220);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 16);

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sideNavPanel;
    private JButton btnSummary;
    private JButton btnPatientReg;
    private JButton btnMedical;
    private JButton btnBilling;
    private JButton btnLab;
    private JButton btnAdmission;
    private JButton activeButton;
    // headerActions removed to avoid duplicate toolbar gap; per-panel action panels are used

    private String currentUsername;
    private String subRole; // REGISTRATION, BILLING, LAB (optional)

    // controller reference
    private final StaffController staffController;

    // NEW: Keep a reference to the username label to toggle visibility
    private JLabel userTagLabel;

    // Tables
    private JTable patientRegTable;
    private JTable medicalRecordTable;
    private JTable billingTable;
    private JTable labTable;
    private JTable admissionTable;
    // Global search/filter state
    private String globalSearchQuery;
    private final Map<String, Map<String,String>> columnFilters = new HashMap<>();

    // Summary dynamic labels
    private JLabel lblTotalPatients;
    private JLabel lblPendingBills;
    private JLabel lblLabPending;

    // Constructors (controller-first to avoid ambiguous String overloads)
    public StaffDashboardPanel(StaffController controller, String username) { this(controller, null, username); }
    public StaffDashboardPanel(StaffController controller, String subRole, String username) {
        this.staffController = controller;
        this.currentUsername = username;
        this.subRole = subRole != null ? subRole.toUpperCase() : null;
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new BorderLayout(8, 8));

        add(createHeader(), BorderLayout.NORTH);
        add(createSideBar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        // Default card selection based on subRole
        if (this.subRole == null) {
            setActiveButton(btnSummary, "SUMMARY");
        } else {
            switch (this.subRole) {
                case "REGISTRATION": setActiveButton(btnPatientReg, "PATIENT_REG"); break;
                case "BILLING": setActiveButton(btnBilling, "BILLING"); break;
                case "LAB": setActiveButton(btnLab, "LAB"); break;
                default: setActiveButton(btnSummary, "SUMMARY");
            }
        }

        // Try to show queued notifications for this staff user (if NotificationService exists)
        SwingUtilities.invokeLater(this::checkNotifications);
     }

    // Reflection-based notification fetcher to avoid hard dependency on NotificationService
    private void checkNotifications() {
        if (this.currentUsername == null || this.currentUsername.isBlank()) return;
        try {
            Class<?> cls = Class.forName("Service.NotificationService");
            java.lang.reflect.Method getInst = cls.getMethod("getInstance");
            Object inst = getInst.invoke(null);
            java.lang.reflect.Method getAndClear = cls.getMethod("getAndClearNotifications", String.class);
            Object notes = getAndClear.invoke(inst, this.currentUsername);
            if (notes instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) notes;
                if (!list.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Object n : list) sb.append("- ").append(n==null?"":n.toString()).append("\n");
                    JOptionPane.showMessageDialog(this, sb.toString(), "Notifications", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Throwable ignored) {
            // Notification service not available; ignore
        }
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new LineBorder(COLOR_BORDER));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 60));

        // Left side: title (with subRole if any)
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        left.setOpaque(false);
        userTagLabel = null;

        String titleText = (subRole != null && !subRole.isBlank()) ? "Staff Dashboard - " + subRole : "Staff Dashboard";
        JLabel title = new JLabel(titleText);
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY.darker());
        left.add(title);

        // Right: persistent refresh and dynamic actions area
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12)); right.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh"); styleSecondaryButton(btnRefresh); btnRefresh.addActionListener(e -> JOptionPane.showMessageDialog(this, "Data refreshed (placeholder)", "Info", JOptionPane.INFORMATION_MESSAGE));
        right.add(btnRefresh);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent createSideBar() {
        sideNavPanel = new JPanel();
        sideNavPanel.setLayout(new BoxLayout(sideNavPanel, BoxLayout.Y_AXIS));
        sideNavPanel.setBackground(COLOR_SIDEBAR_BG);
        sideNavPanel.setBorder(new LineBorder(COLOR_BORDER));
        sideNavPanel.setPreferredSize(new Dimension(260, 0));

        btnSummary = createNavButton("Summary", "SUMMARY");
        btnPatientReg = createNavButton("Patient Registration", "PATIENT_REG");
        btnMedical = createNavButton("Medical Records", "MEDICAL");
        btnBilling = createNavButton("Billing History", "BILLING");
        btnLab = createNavButton("Lab Tests", "LAB");
        btnAdmission = createNavButton("Admission & Discharge", "ADMISSION");
        JButton btnGuide = createNavButton("User Guide", "GUIDE");

        // consistent spacing
        int gap = 12;
        sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnSummary); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnPatientReg); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnMedical); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnBilling); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnLab); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnAdmission); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnGuide); sideNavPanel.add(Box.createVerticalStrut(8));
        sideNavPanel.add(Box.createVerticalGlue());
        return sideNavPanel;
    }

    private JButton createNavButton(String text, String card) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        // taller buttons for consistent spacing with patient UI
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
        // Username is no longer shown anywhere
        cardLayout.show(mainContentPanel, card);
        // per-panel action panels are used; no global headerActions to update
    }

    private JComponent createMainContent() {
        mainContentPanel = new JPanel();
        cardLayout = new CardLayout();
        mainContentPanel.setLayout(cardLayout);
        mainContentPanel.setBorder(new LineBorder(COLOR_BORDER));

        mainContentPanel.add(buildSummaryPanel(), "SUMMARY");
        mainContentPanel.add(buildPatientRegPanel(), "PATIENT_REG");
        mainContentPanel.add(buildMedicalPanel(), "MEDICAL");
        mainContentPanel.add(buildBillingPanel(), "BILLING");
        mainContentPanel.add(buildLabPanel(), "LAB");
        mainContentPanel.add(buildAdmissionPanel(), "ADMISSION");
        mainContentPanel.add(buildGuidePanel(), "GUIDE");
        return mainContentPanel;
    }

    // SUMMARY ---------------------------------------------------------
    private JPanel buildSummaryPanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("Operational Summary"), BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 12, 12));
        statsGrid.setOpaque(false);
        lblTotalPatients = createStatValueLabel("0");
        lblPendingBills = createStatValueLabel("0");
        lblLabPending = createStatValueLabel("0");
        statsGrid.add(wrapStat("Registered Patients", lblTotalPatients));
        statsGrid.add(wrapStat("Pending Bills", lblPendingBills));
        statsGrid.add(wrapStat("Pending Lab Tests", lblLabPending));
        root.add(statsGrid, BorderLayout.CENTER);

        JTextArea info = new JTextArea("Welcome staff! Navigate using the left menu to manage patients, records, billing, labs, and admissions.");
        info.setEditable(false); info.setLineWrap(true); info.setWrapStyleWord(true); info.setFont(FONT_NORMAL);
        info.setBorder(new EmptyBorder(8, 12, 8, 12));
        root.add(new JScrollPane(info), BorderLayout.SOUTH);
        return root;
    }

    private JLabel createStatValueLabel(String value) {
        JLabel l = new JLabel(value, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(COLOR_PRIMARY);
        return l;
    }
    private JPanel wrapStat(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(COLOR_BORDER));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(COLOR_PRIMARY.darker());
        t.setBorder(new EmptyBorder(6, 6, 0, 6));
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    // PATIENT REGISTRATION --------------------------------------------
    private JPanel buildPatientRegPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top area: title (left), search (center), actions (right) - action buttons visible next to search
        JPanel topPanel = new JPanel(new BorderLayout(8, 8)); topPanel.setOpaque(false);
        JLabel header = new JLabel("Patient Registration & Records", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker()); header.setBorder(new EmptyBorder(0,0,0,0)); topPanel.add(header, BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); searchPanel.setOpaque(false); searchPanel.add(new JLabel("Search Patients:")); JTextField searchField = new JTextField(20); searchPanel.add(searchField); topPanel.add(searchPanel, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actionPanel.setOpaque(false);
        JButton btnAdd = new JButton("Add"); styleSecondaryButton(btnAdd); btnAdd.addActionListener(e -> openAddPatientDialog());
        JButton btnView = new JButton("View"); styleSecondaryButton(btnView); btnView.addActionListener(e -> openViewPatientDialog());
        JButton btnDeactivate = new JButton("Deactivate"); styleSecondaryButton(btnDeactivate); btnDeactivate.addActionListener(e -> openDeactivatePatientDialog());
        JButton btnAssign = new JButton("Assign Appointment"); styleSecondaryButton(btnAssign); btnAssign.addActionListener(e -> openAssignAppointmentDialogForStaff());
        btnAdd.setPreferredSize(new Dimension(80,34)); btnView.setPreferredSize(new Dimension(80,34)); btnDeactivate.setPreferredSize(new Dimension(110,34)); btnAssign.setPreferredSize(new Dimension(180,34));
        actionPanel.add(btnAdd); actionPanel.add(btnView); actionPanel.add(btnDeactivate); actionPanel.add(btnAssign);
        topPanel.add(actionPanel, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        // Now include hidden ID column at index 0: {ID, Name, Age, Gender, Status}
        String[] cols = {"ID", "Name", "Age", "Gender", "Status"};
        Object[][] data = {{"p1", "John Doe", 45, "M", "Active"}, {"p2", "Jane Smith", 29, "F", "Inactive"}};
        patientRegTable = new JTable(new DefaultTableModel(data, cols));
        // Hide ID column visually
        patientRegTable.getColumnModel().getColumn(0).setMinWidth(0);
        patientRegTable.getColumnModel().getColumn(0).setMaxWidth(0);
        patientRegTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterPatientRegTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterPatientRegTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterPatientRegTable(searchField.getText()); }
        });

        root.add(new JScrollPane(patientRegTable), BorderLayout.CENTER);
        return root;
    }

    // Assign appointment flow used by staff panel
    private void openAssignAppointmentDialogForStaff() {
        int row = patientRegTable.getSelectedRow();
        if (row == -1) { warn("Select a patient first"); return; }
        DefaultTableModel m = (DefaultTableModel) patientRegTable.getModel();
        String patientId = (String) m.getValueAt(row, 0);
        String patientName = (String) m.getValueAt(row, 1);

        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JLabel("Patient:")); JTextField patientField = new JTextField(patientName); patientField.setEditable(false); p.add(patientField);

        // SPECIALTY -> doctor selection UI
        p.add(new JLabel("Specialty:"));
        String[] specialties = new String[]{"Any","Orthopedics","Cardiology","Pediatrics","General"};
        JComboBox<String> specialtyCombo = new JComboBox<>(specialties);
        p.add(specialtyCombo);

        p.add(new JLabel("Doctor:"));
        JComboBox<String> doctorCombo = new JComboBox<>();
        doctorCombo.setEditable(false);
        p.add(doctorCombo);

        // Reason and datetime fields
        p.add(new JLabel("Date & Time (YYYY-MM-DDTHH:MM):")); JTextField whenField = new JTextField(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))); p.add(whenField);
        p.add(new JLabel("Reason:")); JTextField reasonField = new JTextField(); p.add(reasonField);

        // Build a small specialty mapping for demo doctor accounts available in UserService
        java.util.Map<String, java.util.List<String>> specialtyToUsers = new java.util.LinkedHashMap<>();
        for (String s : specialties) specialtyToUsers.put(s, new java.util.ArrayList<>());
        // Collect doctor users from UserService
        for (User u : UserService.getInstance().getAllUsers()) {
            if (u == null) continue;
            if (u.getRole() == Role.DOCTOR) {
                String uname = u.getUsername();
                // Demo mapping heuristics: drjohn -> Orthopedics, other "doctor" -> General
                if ("drjohn".equalsIgnoreCase(uname)) specialtyToUsers.get("Orthopedics").add(uname);
                else specialtyToUsers.get("General").add(uname);
                // Also add to Any
                specialtyToUsers.get("Any").add(uname);
            }
        }

        // Populate doctor combo based on selected specialty
        Runnable refreshDoctorCombo = () -> {
            String sel = (String) specialtyCombo.getSelectedItem();
            java.util.List<String> list = specialtyToUsers.getOrDefault(sel, new java.util.ArrayList<>());
            doctorCombo.removeAllItems();
            if (list.isEmpty()) {
                doctorCombo.addItem("(No doctors available)");
                doctorCombo.setEnabled(false);
            } else {
                for (String d : list) doctorCombo.addItem(d);
                doctorCombo.setEnabled(true);
            }
        };
        specialtyCombo.addActionListener(e -> refreshDoctorCombo.run());
        // initial fill
        refreshDoctorCombo.run();

        int res = JOptionPane.showConfirmDialog(this, p, "Assign Appointment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String doctor = (doctorCombo.getItemCount() > 0 && doctorCombo.isEnabled()) ? (String) doctorCombo.getSelectedItem() : null;
            if (doctor != null && doctor.startsWith("(No doctors")) doctor = null;
            if (doctor == null) { warn("No doctor selected or available for that specialty"); return; }
            String whenStr = whenField.getText().trim();
            String reason = reasonField.getText().trim();
            if (doctor.isEmpty() || whenStr.isEmpty()) { warn("Doctor and date/time are required"); return; }
            LocalDateTime when;
            try { when = LocalDateTime.parse(whenStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")); }
            catch (Exception ex) { warn("Invalid date/time format. Use YYYY-MM-DDTHH:MM"); return; }

            // Schedule using selected doctor's username as staffId
            try {
                Appointment appt = AppointmentService.getInstance().schedule(patientId, doctor, when, reason);
                // Register the request origin so staff can be notified if it is cancelled/accepted (optional registry)
                try {
                    Class<?> regCls = Class.forName("Service.AppointmentRequestRegistry");
                    java.lang.reflect.Method getReg = regCls.getMethod("getInstance");
                    Object regInst = getReg.invoke(null);
                    java.lang.reflect.Method register = regCls.getMethod("registerRequest", String.class, String.class);
                    register.invoke(regInst, appt.getId(), this.currentUsername==null?"staff":this.currentUsername);
                } catch (Throwable ignored) {}
                info("Appointment scheduled for " + patientName + " with " + doctor + " on " + when);
            } catch (Exception ex) {
                warn("Failed to schedule appointment: " + ex.getMessage());
            }
        }
    }

    // MEDICAL RECORDS -------------------------------------------------
    private JPanel buildMedicalPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top: header, search, actions
        JPanel topPanel = new JPanel(new BorderLayout(8,8)); topPanel.setOpaque(false);
        JLabel header = new JLabel("Medical & Treatment Records", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker()); topPanel.add(header, BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); searchPanel.setOpaque(false); searchPanel.add(new JLabel("Search Records:")); JTextField searchField = new JTextField(20); searchPanel.add(searchField); topPanel.add(searchPanel, BorderLayout.CENTER);
        JPanel actionPanelMed = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actionPanelMed.setOpaque(false); JButton medAdd = new JButton("Add Record"); styleSecondaryButton(medAdd); medAdd.addActionListener(e -> openAddMedicalRecordDialog()); JButton medView = new JButton("View"); styleSecondaryButton(medView); medView.addActionListener(e -> openViewMedicalRecordDialog()); JButton medRemove = new JButton("Remove"); styleSecondaryButton(medRemove); medRemove.addActionListener(e -> openDeleteMedicalRecordDialog()); actionPanelMed.add(medAdd); actionPanelMed.add(medView); actionPanelMed.add(medRemove); topPanel.add(actionPanelMed, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Record ID", "Patient", "Type", "Notes"};
        Object[][] data = {{101, "John Doe", "Consultation", "Blood pressure stable"}, {102, "Jane Smith", "Follow-up", "Recommend lab test"}};
        medicalRecordTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterMedicalTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterMedicalTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterMedicalTable(searchField.getText()); }
        });

        root.add(new JScrollPane(medicalRecordTable), BorderLayout.CENTER);

        // actions moved to headerActions (no bottom toolbar)
        return root;
    }

    // BILLING ---------------------------------------------------------
    private JPanel buildBillingPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Billing & Payment History", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Billing:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel actionPanelBill = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actionPanelBill.setOpaque(false); JButton billExport = new JButton("Export"); styleSecondaryButton(billExport); billExport.addActionListener(e -> openExportBillingDialog()); JButton billMark = new JButton("Mark Paid"); styleSecondaryButton(billMark); billMark.addActionListener(e -> openMarkPaidDialog()); actionPanelBill.add(billExport); actionPanelBill.add(billMark); topPanel.add(actionPanelBill, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Date", "Patient", "Amount", "Description", "Status"};
        Object[][] data = {{"2025-01-10", "John Doe", "$120", "Consultation", "Unpaid"}, {"2025-01-11", "Jane Smith", "$200", "Lab Test", "Paid"}};
        billingTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterBillingTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterBillingTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterBillingTable(searchField.getText()); }
        });

        root.add(new JScrollPane(billingTable), BorderLayout.CENTER);

        // actions moved to headerActions (no bottom footer)
        return root;
    }

    // LAB -------------------------------------------------------------
    private JPanel buildLabPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Laboratory & Test Management", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Lab Tests:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel actionPanelLab = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actionPanelLab.setOpaque(false); JButton labAdd = new JButton("Add Test"); styleSecondaryButton(labAdd); labAdd.addActionListener(e -> openAddLabTestDialog()); JButton labUpdate = new JButton("Update"); styleSecondaryButton(labUpdate); labUpdate.addActionListener(e -> openUpdateLabTestDialog()); JButton labComplete = new JButton("Complete"); styleSecondaryButton(labComplete); labComplete.addActionListener(e -> openCompleteLabTestDialog()); actionPanelLab.add(labAdd); actionPanelLab.add(labUpdate); actionPanelLab.add(labComplete); topPanel.add(actionPanelLab, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Test ID", "Patient", "Test", "Status"};
        Object[][] data = {{501, "John Doe", "CBC", "Pending"}, {502, "Jane Smith", "X-Ray", "Completed"}};
        labTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterLabTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterLabTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterLabTable(searchField.getText()); }
        });

        root.add(new JScrollPane(labTable), BorderLayout.CENTER);

        // actions moved to headerActions (no bottom toolbar)
        return root;
    }

    // ADMISSION -------------------------------------------------------
    private JPanel buildAdmissionPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Admission & Discharge Management", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Admissions:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel actionPanelAdm = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actionPanelAdm.setOpaque(false); JButton admAdmit = new JButton("Admit"); styleSecondaryButton(admAdmit); admAdmit.addActionListener(e -> openAdmitPatientDialog()); JButton admDis = new JButton("Discharge"); styleSecondaryButton(admDis); admDis.addActionListener(e -> openDischargePatientDialog()); JButton admTrans = new JButton("Transfer"); styleSecondaryButton(admTrans); admTrans.addActionListener(e -> openTransferPatientDialog()); actionPanelAdm.add(admAdmit); actionPanelAdm.add(admDis); actionPanelAdm.add(admTrans); topPanel.add(actionPanelAdm, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Admission ID", "Patient", "Room", "Status"};
        Object[][] data = {{801, "John Doe", "101A", "Admitted"}, {802, "Jane Smith", "102B", "Discharged"}};
        admissionTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterAdmissionTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterAdmissionTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterAdmissionTable(searchField.getText()); }
        });

        root.add(new JScrollPane(admissionTable), BorderLayout.CENTER);

        // actions moved to headerActions (no bottom toolbar)
        return root;
    }

    // USER GUIDE ------------------------------------------------------
    private JPanel buildGuidePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("User Guide"), BorderLayout.NORTH);

        JTextArea area = new JTextArea(
            "Welcome to the Staff User Guide.\n\n" +
            "Navigation:\n- Use the left menu to manage Summary, Patient Registration, Medical Records, Billing, Lab, Admission, and this Guide.\n\n" +
            "Patient Registration:\n- Add, view, deactivate patients; use the search box to filter.\n\n" +
            "Medical Records:\n- Add, view, remove treatment records.\n\n" +
            "Billing:\n- Search bills, mark as paid, and export history.\n\n" +
            "Lab:\n- Add tests, update status, and complete tests.\n\n" +
            "Admission:\n- Admit, discharge, and transfer patients.\n\n" +
            "Tips:\n- Use global search/filter when available to narrow results.\n- Toolbar buttons at the bottom provide common actions.");
        area.setEditable(false);
        area.setFont(FONT_NORMAL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        return root;
    }

    // HELPERS ---------------------------------------------------------
    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setFont(FONT_SECTION);
        l.setForeground(COLOR_PRIMARY.darker());
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }
    private void styleToolbarButton(JToolBar bar, String text, Runnable action) {
        JButton b = new JButton(text); b.setFont(FONT_NORMAL); b.addActionListener(e -> action.run()); bar.add(b);
    }
    private void styleSecondaryButton(JButton b) {
        b.setFont(FONT_NORMAL); b.setBackground(Color.WHITE); b.setBorder(new LineBorder(COLOR_BORDER)); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e){ b.setBackground(COLOR_PRIMARY_HOVER);} @Override public void mouseExited(MouseEvent e){ b.setBackground(Color.WHITE);} });
    }

    // DIALOG METHODS (Patient Registration)
    private void openAddPatientDialog() {
        PatientRegistrationDialog.Result res = PatientRegistrationDialog.showDialog(this);
        if (res == null || res.patient == null) return;
        Model.Patient p = res.patient;
        int age = 0; try { age = Math.max(0, java.time.Period.between(p.getDateOfBirth(), java.time.LocalDate.now()).getYears()); } catch (Exception ignored) {}
        javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) patientRegTable.getModel();
        m.addRow(new Object[]{p.getId(), (p.getFirstName()==null?"":p.getFirstName()) + " " + (p.getLastName()==null?"":p.getLastName()), age, p.getGender(), "Active"});
        if (res.account != null) {
            JOptionPane.showMessageDialog(this,
                "Patient account has been created.\n\nUsername: " + res.account.username + "\nTemporary Password: " + res.account.temporaryPassword + "\n\nShare these with the patient.",
                "Account Created",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void openViewPatientDialog() {
        int row = patientRegTable.getSelectedRow(); if (row==-1){warn("Select a patient first"); return;}
        DefaultTableModel m=(DefaultTableModel)patientRegTable.getModel();
        String patientId = (String) m.getValueAt(row, 0);
        Service.PatientService ps = Service.PatientService.getInstance();
        java.util.Optional<Model.Patient> optP = ps.findById(patientId);
        if (optP.isEmpty()) { warn("Patient record not found."); return; }
        Model.Patient p = optP.get();
        // Resolve username via linkedPatientId
        String username = null;
        for (Model.User u : Service.UserService.getInstance().getAllUsers()) {
            try { if (patientId.equals(u.getLinkedPatientId())) { username = u.getUsername(); break; } } catch (Exception ignored) {}
        }
        Service.PatientService.PatientProfile prof = (username!=null) ? ps.getProfileByUsername(username) : new Service.PatientService.PatientProfile();

        // Build a neat, resizable dialog panel
        JPanel panel = new JPanel(new BorderLayout(12,12)); panel.setBorder(new EmptyBorder(12,12,12,12)); panel.setBackground(Color.WHITE);
        JLabel title = new JLabel("Patient Details", SwingConstants.LEFT); title.setFont(FONT_SECTION); title.setForeground(COLOR_PRIMARY.darker()); panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout()); grid.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,10,6,10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0; gbc.weightx=0;
        Dimension labelSize = new Dimension(180, 24);
        java.util.function.BiConsumer<String,String> addRow = (label, value) -> {
            JLabel l = new JLabel(label + ":"); l.setPreferredSize(labelSize); l.setForeground(COLOR_PRIMARY.darker());
            gbc.gridx=0; gbc.weightx=0; grid.add(l, gbc);
            gbc.gridx=1; gbc.weightx=1; JTextField tf = new JTextField(value==null?"":value); tf.setEditable(false); grid.add(tf, gbc); gbc.gridy++;
        };
        // Coalesce helper
        java.util.function.BiFunction<String,String,String> coalesce = (a,b) -> (a!=null && !a.isBlank())?a:(b==null?"":b);
        String dobStr = prof.dateOfBirth!=null && !prof.dateOfBirth.isBlank() ? prof.dateOfBirth : (p.getDateOfBirth()==null?"":p.getDateOfBirth().toString());
        String ageStr = (p.getAge()!=null)?String.valueOf(p.getAge()):"";

        addRow.accept("ID", p.getId());
        addRow.accept("Patient Number", p.getPatientNumber());
        addRow.accept("Name", coalesce.apply(prof.firstName, p.getFirstName()) + (prof.middleName==null||prof.middleName.isBlank()?"":" " + prof.middleName) + " " + coalesce.apply(prof.surname, p.getLastName()));
        addRow.accept("Gender", coalesce.apply(prof.gender, p.getGender()));
        addRow.accept("DOB", dobStr);
        addRow.accept("Age", ageStr);
        addRow.accept("Phone", coalesce.apply(prof.phone, p.getContactNumber()));
        addRow.accept("Email", prof.email);
        addRow.accept("Address", coalesce.apply(prof.address, p.getAddress()));
        addRow.accept("Civil Status", prof.civilStatus);
        addRow.accept("Blood Type", prof.bloodType!=null?prof.bloodType:p.getBloodType());
        addRow.accept("Emergency Contact", (prof.emergencyContactName==null?"":prof.emergencyContactName) + (prof.emergencyContactNumber==null||prof.emergencyContactNumber.isBlank()?"":" ("+prof.emergencyContactNumber+")"));
        // Optional richer medical/insurance fields from profile (if present)
        addRow.accept("Allergies", prof.allergies);
        addRow.accept("Medications", prof.currentMedications);
        addRow.accept("Insurance Provider", prof.insuranceProvider);
        addRow.accept("Insurance Number", prof.insuranceNumber);
        addRow.accept("PhilHealth Number", prof.philHealthNumber);
        addRow.accept("Insurance Expiry", prof.insuranceExpiry);

        panel.add(new JScrollPane(grid), BorderLayout.CENTER);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Patient Details", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setLayout(new BorderLayout()); dlg.getContentPane().add(panel, BorderLayout.CENTER);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); foot.setOpaque(false); JButton close = new JButton("Close"); styleSecondaryButton(close); close.addActionListener(e->dlg.dispose()); foot.add(close); dlg.getContentPane().add(foot, BorderLayout.SOUTH);
        dlg.setSize(800, 600); dlg.setLocationRelativeTo(this); dlg.setResizable(true); dlg.setVisible(true);
    }
    private void openDeactivatePatientDialog() {
        int row = patientRegTable.getSelectedRow(); if (row==-1){warn("Select a patient first"); return;}
        int c = confirm("Deactivate this patient?"); if (c==JOptionPane.YES_OPTION){ ((DefaultTableModel)patientRegTable.getModel()).setValueAt("Inactive", row, 4); info("Patient deactivated."); }
    }

    // MEDICAL RECORDS DIALOGS ----------------------------------------
    private void openAddMedicalRecordDialog() {
        JPanel p=new JPanel(new GridLayout(4,2,8,8)); p.setBorder(new EmptyBorder(10,10,10,10));
        JTextField patient=field(p,"Patient:"); JTextField type=field(p,"Type:"); JTextField notes=field(p,"Notes:"); field(p,"Extra:");
        if (showDialog(p,"Add Medical Record")==JOptionPane.OK_OPTION){ if(!patient.getText().isEmpty()){ DefaultTableModel m=(DefaultTableModel)medicalRecordTable.getModel(); m.addRow(new Object[]{m.getRowCount()+101, patient.getText(), type.getText(), notes.getText()}); info("Record added."); } else warn("Patient required"); }
    }
    private void openViewMedicalRecordDialog() {
        int r=medicalRecordTable.getSelectedRow(); if(r==-1){warn("Select a record first"); return;} DefaultTableModel m=(DefaultTableModel)medicalRecordTable.getModel();
        info(String.format("Record: %s\nPatient: %s\nType: %s\nNotes: %s",m.getValueAt(r,0),m.getValueAt(r,1),m.getValueAt(r,2),m.getValueAt(r,3)));
    }
    private void openDeleteMedicalRecordDialog() {
        int r=medicalRecordTable.getSelectedRow(); if(r==-1){warn("Select a record first"); return;} if(confirm("Delete this record?")==JOptionPane.YES_OPTION){ ((DefaultTableModel)medicalRecordTable.getModel()).removeRow(r); info("Record deleted."); }
    }

    // BILLING DIALOGS ------------------------------------------------
    private void openExportBillingDialog() { info("Export billing (placeholder)"); }
    private void openMarkPaidDialog() {
        int r=billingTable.getSelectedRow(); if(r==-1){warn("Select a bill first"); return;} ((DefaultTableModel)billingTable.getModel()).setValueAt("Paid", r, 4); info("Marked as paid.");
    }

    // LAB DIALOGS -----------------------------------------------------
    private void openAddLabTestDialog() { JPanel p=new JPanel(new GridLayout(3,2,8,8)); p.setBorder(new EmptyBorder(10,10,10,10)); JTextField patient=field(p,"Patient:"); JTextField test=field(p,"Test:"); field(p,"Notes:"); if(showDialog(p,"Add Lab Test")==JOptionPane.OK_OPTION){ if(!patient.getText().isEmpty()){ DefaultTableModel m=(DefaultTableModel)labTable.getModel(); m.addRow(new Object[]{m.getRowCount()+501, patient.getText(), test.getText(), "Pending"}); info("Lab test added."); } else warn("Patient required"); } }
    private void openUpdateLabTestDialog() { int r=labTable.getSelectedRow(); if(r==-1){warn("Select test first"); return;} ((DefaultTableModel)labTable.getModel()).setValueAt("In Progress", r, 3); info("Status updated."); }
    private void openCompleteLabTestDialog() { int r=labTable.getSelectedRow(); if(r==-1){warn("Select test first"); return;} ((DefaultTableModel)labTable.getModel()).setValueAt("Completed", r, 3); info("Test completed."); }

    // ADMISSION DIALOGS ----------------------------------------------
    private void openAdmitPatientDialog() { JPanel p=new JPanel(new GridLayout(3,2,8,8)); p.setBorder(new EmptyBorder(10,10,10,10)); JTextField patient=field(p,"Patient:"); JTextField room=field(p,"Room:"); field(p,"Notes:"); if(showDialog(p,"Admit Patient")==JOptionPane.OK_OPTION){ if(!patient.getText().isEmpty()){ DefaultTableModel m=(DefaultTableModel)admissionTable.getModel(); m.addRow(new Object[]{m.getRowCount()+801, patient.getText(), room.getText(), "Admitted"}); info("Patient admitted."); } else warn("Patient required"); } }
    private void openDischargePatientDialog() { int r=admissionTable.getSelectedRow(); if(r==-1){warn("Select admission first"); return;} ((DefaultTableModel)admissionTable.getModel()).setValueAt("Discharged", r, 3); info("Patient discharged."); }
    private void openTransferPatientDialog() { int r=admissionTable.getSelectedRow(); if(r==-1){warn("Select admission first"); return;} ((DefaultTableModel)admissionTable.getModel()).setValueAt("Transferred", r, 3); info("Patient transferred."); }

    // SMALL UTILS -----------------------------------------------------
    private JTextField field(JPanel p, String label){ p.add(new JLabel(label)); JTextField f=new JTextField(); p.add(f); return f; }
    private int showDialog(JPanel panel, String title){ return JOptionPane.showConfirmDialog(this,panel,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE); }
    // Replaces small JOptionPane dialogs with a resizable modal JDialog that is larger for readability
    private int showLargeDialog(JPanel panel, String title) {
        final int[] result = {JOptionPane.CLOSED_OPTION};
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(panel);
        // Use nearly full screen size to maximize space
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.max(900, screen.width - 80);
        int h = Math.max(600, screen.height - 120);
        sp.setPreferredSize(new Dimension(w, h));
        dlg.getContentPane().add(sp, BorderLayout.CENTER);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK"); JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> { result[0] = JOptionPane.OK_OPTION; dlg.dispose(); });
        cancel.addActionListener(e -> { result[0] = JOptionPane.CANCEL_OPTION; dlg.dispose(); });
        foot.add(cancel); foot.add(ok);
        dlg.getContentPane().add(foot, BorderLayout.SOUTH);
        dlg.pack(); dlg.setLocation(20, 20); dlg.setVisible(true);
        // make the dialog fullscreen (undecorated) so admin/staff see the full form
        dlg.setUndecorated(true);
        dlg.setBounds(0, 0, screen.width, screen.height);
        dlg.setVisible(true);
        return result[0];
    }
    private void info(String msg){ JOptionPane.showMessageDialog(this,msg,"Info",JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String msg){ JOptionPane.showMessageDialog(this,msg,"Warning",JOptionPane.WARNING_MESSAGE); }
    private int confirm(String msg){ return JOptionPane.showConfirmDialog(this,msg,"Confirm",JOptionPane.YES_NO_OPTION); }
    private int parseIntSafe(String s){ try{ return Integer.parseInt(s.trim()); }catch(Exception e){ return 0; } }

    // PUBLIC UPDATE API -----------------------------------------------
    public void updateSummary(int patients, int pendingBills, int labPending){
        if(lblTotalPatients!=null) lblTotalPatients.setText(String.valueOf(patients));
        if(lblPendingBills!=null) lblPendingBills.setText(String.valueOf(pendingBills));
        if(lblLabPending!=null) lblLabPending.setText(String.valueOf(labPending));
    }
    public JTable getPatientRegTable(){ return patientRegTable; }
    public JTable getMedicalRecordTable(){ return medicalRecordTable; }
    public JTable getBillingTable(){ return billingTable; }
    public JTable getLabTable(){ return labTable; }
    public JTable getAdmissionTable(){ return admissionTable; }

    // GlobalSearchable implementation --------------------------------
    @Override
    public Map<String, JTable> getSearchableTables() {
        Map<String, JTable> map = new LinkedHashMap<>();
        if (patientRegTable != null) map.put("patients", patientRegTable);
        if (medicalRecordTable != null) map.put("medical", medicalRecordTable);
        if (billingTable != null) map.put("billing", billingTable);
        if (labTable != null) map.put("lab", labTable);
        if (admissionTable != null) map.put("admission", admissionTable);
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

    // PATIENT REGISTRATION FILTERING ----------------------------------
    private void filterPatientRegTable(String query) {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) patientRegTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(patientRegTable.getModel());
            patientRegTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Search across Name (1), Gender (3), Status (4)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 3, 4));
        }
    }

    private void filterMedicalTable(String query) {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) medicalRecordTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(medicalRecordTable.getModel());
            medicalRecordTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2, 3));
        }
    }

    private void filterBillingTable(String query) {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) billingTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(billingTable.getModel());
            billingTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 3, 4));
        }
    }

    private void filterLabTable(String query) {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) labTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(labTable.getModel());
            labTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2, 3));
        }
    }

    private void filterAdmissionTable(String query) {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) admissionTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(admissionTable.getModel());
            admissionTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 3));
        }
    }
}