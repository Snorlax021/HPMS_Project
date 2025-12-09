package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import DTO.PatientSummaryDTO;
import Model.Appointment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import Controller.DoctorController;

public class DoctorDashboardPanel extends JPanel implements GlobalSearchable {
    private static final long serialVersionUID = 1L;

    // controller reference
    private final DoctorController doctorController;

    // THEME CONSTANTS (match AdminDashboardPanel)
    private static final Color COLOR_BG = Color.WHITE;
    private static final Color COLOR_SIDEBAR_BG = new Color(245, 247, 250);
    private static final Color COLOR_PRIMARY = new Color(60, 120, 200);
    private static final Color COLOR_PRIMARY_HOVER = new Color(80, 140, 220);
    private static final Color COLOR_ACTIVE = new Color(100, 160, 240);
    private static final Color COLOR_BORDER = new Color(210, 215, 220);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 16);

    // Layout + navigation
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sideNavPanel;
    private JButton btnDashboard;
    private JButton btnPatients;
    private JButton btnReports;
    private JButton btnSummary;
    private JButton activeButton;

    // Dashboard labels
    private JLabel lblAppointments;
    private JLabel lblPatients;
    private JLabel lblCompletedReports;

    // Tables
    private JTable patientsTable;
    private JTable reportsTable;
    private JTable appointmentsTable;
    private JTable requestsTable;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // Global search/filter state
    private String globalSearchQuery;
    private final Map<String, Map<String,String>> columnFilters = new HashMap<>();
    private final String currentUsername;
    // NEW: username label reference to control visibility
    private JLabel userTagLabel;

    public DoctorDashboardPanel() { this(null, null); }
    public DoctorDashboardPanel(String username) { this(null, username); }
    public DoctorDashboardPanel(DoctorController controller, String username) {
        this.doctorController = controller;
        this.currentUsername = username;
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new BorderLayout(8, 8));

        add(createHeader(), BorderLayout.NORTH);
        add(createSideBar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        // Default view
        setActiveButton(btnDashboard, "DASHBOARD");

        // Show any queued notifications for this user (doctor/staff/patient)
        SwingUtilities.invokeLater(this::checkNotifications);
    }

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
                    for (Object n : list) sb.append("- ").append(n == null ? "" : n.toString()).append("\n");
                    JOptionPane.showMessageDialog(this, sb.toString(), "Notifications", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Throwable ignored) {
            // ignore if notification service not available
        }
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new LineBorder(COLOR_BORDER));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 55));

        // Left: title only — username removed
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        left.setOpaque(false);
        // Do not add username label
        userTagLabel = null;

        JLabel title = new JLabel("Doctor Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY.darker());
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh");
        styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> JOptionPane.showMessageDialog(this, "Data refreshed (placeholder)", "Info", JOptionPane.INFORMATION_MESSAGE));
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

        btnDashboard = createNavButton("Summary", "DASHBOARD");
        btnPatients = createNavButton("Patients", "PATIENTS");
        btnReports = createNavButton("Reports", "REPORTS");
        JButton btnRequests = createNavButton("Appointment Requests", "REQUESTS");
        JButton btnAppointments = createNavButton("Appointments", "APPOINTMENTS");
        // Initialize Summary button properly
        btnSummary = createNavButton("Patient Detail", "SUMMARY");
        JButton btnGuide = createNavButton("User Guide", "GUIDE");
        JButton btnDoctors = createNavButton("Doctor Management", "DOCTORS");

        int gap = 12;
        sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnDashboard); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnPatients); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnReports); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnRequests); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnAppointments); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnSummary); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnDoctors); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnGuide); sideNavPanel.add(Box.createVerticalStrut(8));
        sideNavPanel.add(Box.createVerticalGlue());
        return sideNavPanel;
    }

    private JButton createNavButton(String text, String card) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
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

    private JComponent createMainContent() {
        mainContentPanel = new JPanel();
        cardLayout = new CardLayout();
        mainContentPanel.setLayout(cardLayout);
        mainContentPanel.setBorder(new EmptyBorder(16,16,16,16));

        mainContentPanel.add(buildDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(buildPatientsPanel(), "PATIENTS");
        mainContentPanel.add(buildReportsPanel(), "REPORTS");
        mainContentPanel.add(buildAppointmentRequestsPanel(), "REQUESTS");
        mainContentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        mainContentPanel.add(buildSummaryPanel(), "SUMMARY");
        mainContentPanel.add(buildGuidePanel(), "GUIDE");
        mainContentPanel.add(new UI.DoctorManagementPanel(), "DOCTORS");
        return mainContentPanel;
    }

    // DASHBOARD PANEL --------------------------------------------------
    private JPanel buildDashboardPanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Overview", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 12, 12));
        statsGrid.setOpaque(false);

        lblAppointments = new JLabel("Appointments Today: 0", SwingConstants.CENTER);
        lblAppointments.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAppointments.setForeground(COLOR_PRIMARY);
        lblAppointments.setBorder(new LineBorder(COLOR_BORDER));
        lblAppointments.setOpaque(true);
        lblAppointments.setBackground(Color.WHITE);

        lblPatients = new JLabel("Active Patients: 0", SwingConstants.CENTER);
        lblPatients.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPatients.setForeground(COLOR_PRIMARY);
        lblPatients.setBorder(new LineBorder(COLOR_BORDER));
        lblPatients.setOpaque(true);
        lblPatients.setBackground(Color.WHITE);

        lblCompletedReports = new JLabel("Completed Reports: 0", SwingConstants.CENTER);
        lblCompletedReports.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCompletedReports.setForeground(COLOR_PRIMARY);
        lblCompletedReports.setBorder(new LineBorder(COLOR_BORDER));
        lblCompletedReports.setOpaque(true);
        lblCompletedReports.setBackground(Color.WHITE);

        statsGrid.add(lblAppointments);
        statsGrid.add(lblPatients);
        statsGrid.add(lblCompletedReports);
        root.add(statsGrid, BorderLayout.CENTER);

        JTextArea info = new JTextArea("Use the sidebar to manage patients, review reports, and view a summary.");
        info.setFont(FONT_NORMAL);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBorder(new EmptyBorder(8, 12, 8, 12));
        root.add(new JScrollPane(info), BorderLayout.SOUTH);
        return root;
    }

    // PATIENTS PANEL ---------------------------------------------------
    private JPanel buildPatientsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // top area: header (left), search (center), actions (right)
        JPanel topPanel = new JPanel(new BorderLayout(8,8)); topPanel.setOpaque(false);
        JLabel header = new JLabel("Patient Management", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker()); topPanel.add(header, BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); searchPanel.setOpaque(false); searchPanel.add(new JLabel("Search Patients:")); JTextField searchField = new JTextField(20); searchPanel.add(searchField); topPanel.add(searchPanel, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actionPanel.setOpaque(false);
        JButton btnAdd = new JButton("Add"); styleSecondaryButton(btnAdd); btnAdd.addActionListener(e -> openAddPatientDialog());
        JButton btnView = new JButton("View"); styleSecondaryButton(btnView); btnView.addActionListener(e -> openViewPatientDialog());
        JButton btnDelete = new JButton("Delete"); styleSecondaryButton(btnDelete); btnDelete.addActionListener(e -> openDeletePatientDialog());
        JButton btnAssign = new JButton("Assign Appointment"); styleSecondaryButton(btnAssign); btnAssign.addActionListener(e -> openAssignAppointmentDialog());
        actionPanel.add(btnAdd); actionPanel.add(btnView); actionPanel.add(btnDelete); actionPanel.add(btnAssign);
        topPanel.add(actionPanel, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        // Table setup - fully backed by PatientService and matching Admin's columns
        String[] cols = {"ID", "Name", "DOB", "Gender", "Phone"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){ return false; } };
        patientsTable = new JTable(model);

        // Populate from PatientService
        reloadPatientsTable();

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterPatientsTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterPatientsTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterPatientsTable(searchField.getText()); }
        });

        root.add(new JScrollPane(patientsTable), BorderLayout.CENTER);
        return root;
    }

    private void reloadPatientsTable() {
        DefaultTableModel m = (DefaultTableModel) patientsTable.getModel();
        m.setRowCount(0);
        for (Model.Patient p : PatientService.getInstance().listAll()) {
            m.addRow(new Object[]{p.getId(), (p.getFirstName()==null?"":p.getFirstName()) + " " + (p.getLastName()==null?"":p.getLastName()), p.getDateOfBirth(), p.getGender(), p.getContactNumber()});
        }
    }

    // APPOINTMENTS PANEL -----------------------------------------------
    private JPanel buildAppointmentsPanel() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12,12,12,12));
        // top area: header (left), search (center), actions (right)
        JPanel topAppPanel = new JPanel(new BorderLayout(8,8)); topAppPanel.setOpaque(false);
        JLabel appHeader = new JLabel("Appointments", SwingConstants.LEFT); appHeader.setFont(FONT_SECTION); appHeader.setForeground(COLOR_PRIMARY.darker()); topAppPanel.add(appHeader, BorderLayout.WEST);
        JPanel appActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); appActionPanel.setOpaque(false);
        JButton btnRefreshAppts = new JButton("Refresh"); styleSecondaryButton(btnRefreshAppts); btnRefreshAppts.addActionListener(e -> refreshAppointments()); appActionPanel.add(btnRefreshAppts);
        JButton btnAcceptAppt = new JButton("Accept Appointment"); styleSecondaryButton(btnAcceptAppt); btnAcceptAppt.addActionListener(e -> acceptSelectedAppointment()); appActionPanel.add(btnAcceptAppt);
        JButton btnCancelAppt = new JButton("Cancel Appointment"); styleSecondaryButton(btnCancelAppt); btnCancelAppt.addActionListener(e -> cancelSelectedAppointment()); appActionPanel.add(btnCancelAppt);
        topAppPanel.add(appActionPanel, BorderLayout.EAST);
        root.add(topAppPanel, BorderLayout.NORTH);

        // Include appointment id (hidden/visible) to allow cancellation by id
        String[] cols = {"Appt ID", "Patient ID","Patient","Doctor","When","Reason","Status"};
        Object[][] data = {};
        appointmentsTable = new JTable(new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r,int c){ return false; } });
        // Optionally hide the Appt ID column width
        root.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        refreshAppointments();
        return root;
    }

    private void refreshAppointments() {
        DefaultTableModel m = (DefaultTableModel) appointmentsTable.getModel();
        m.setRowCount(0);
        for (Appointment a : AppointmentService.getInstance().listAll()) {
            String doc = a.getStaffId();
            if (this.currentUsername != null && !this.currentUsername.isBlank()) {
                // show only appointments for this doctor
                if (!this.currentUsername.equalsIgnoreCase(doc) && !doc.equalsIgnoreCase(this.currentUsername)) continue;
            }
            // Resolve patient display name when possible
            String patientDisplay = a.getPatientId();
            java.util.Optional<Model.Patient> pat = PatientService.getInstance().findById(a.getPatientId());
            if (pat.isPresent()) {
                Model.Patient pp = pat.get();
                patientDisplay = (pp.getFirstName()==null?"":pp.getFirstName()) + " " + (pp.getLastName()==null?"":pp.getLastName());
            }
            // First column is appointment id to allow cancel by id
            m.addRow(new Object[]{a.getId(), a.getPatientId(), patientDisplay, a.getStaffId(), dtf.format(a.getScheduledAt()), a.getReason(), a.getStatus().name()});
        }
        // Attempt to hide the Appt ID column visually (best-effort)
        try {
            appointmentsTable.getColumnModel().getColumn(0).setMinWidth(0);
            appointmentsTable.getColumnModel().getColumn(0).setMaxWidth(0);
            appointmentsTable.getColumnModel().getColumn(0).setWidth(0);
        } catch (Exception ignored) {}
    }

    private void cancelSelectedAppointment() {
        int row = appointmentsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
        DefaultTableModel m = (DefaultTableModel) appointmentsTable.getModel();
        String apptId = (String) m.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel selected appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            AppointmentService.getInstance().cancel(apptId);
            JOptionPane.showMessageDialog(this, "Appointment cancelled.");
            refreshAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to cancel appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // SUMMARY PANEL ----------------------------------------------------
    private JPanel buildSummaryPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel header = new JLabel("Summary", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setFont(FONT_NORMAL);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);

        // Try to show a patient summary DTO for demo (uses first available patient)
        String text;
        PatientService ps = PatientService.getInstance();
        // Seed a demo patient if none exist yet (singleton store)
        if (ps.listAll().isEmpty()) {
            ps.createPatient("Jane", "Doe", LocalDate.of(1990, 1, 1), "F", "1234567890", "jane@example.com", "123 St");
        }
        java.util.Optional<Model.Patient> first = ps.listAll().stream().findFirst();
        if (first.isPresent()) {
            PatientSummaryDTO dto = ps.getPatientSummaryById(first.get().getId()).orElse(null);
            if (dto != null) {
                text = String.format(
                    "Patient Summary Demo:\nID: %s\nName: %s\nAge: %s\nGender: %s\nStatus: %s\nRoom: %s\nBed: %s\nAdmitted At: %s\n\n(Fields not tracked remain blank)",
                    dto.getId(), dto.getFullName(), dto.getAge(), dto.getGender(),
                    dto.getStatus(), dto.getRoomId(), dto.getBedId(), dto.getAdmittedAt()
                );
            } else {
                text = "No summary DTO available.";
            }
        } else {
            text = "No patients found. Add patients to see a summary here.";
        }
        area.setText(text);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        return root;
    }

    // USER GUIDE PANEL ------------------------------------------------
    private JPanel buildGuidePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel header = new JLabel("User Guide", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setFont(FONT_NORMAL);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(
            "Welcome to the Doctor User Guide.\n\n" +
            "Navigation:\n- Use the left sidebar to access Dashboard, Patients, Reports, Summary, and this Guide.\n\n" +
            "Patients:\n- Add, view, delete patients; search with the top search bar.\n- Assign appointments using the toolbar.\n\n" +
            "Reports:\n- Review report history and export as CSV/Excel/PDF.\n\n" +
            "Summary:\n- See overview metrics.\n\n" +
            "Tips:\n- Use search to filter tables.\n- Refresh button reloads data (placeholder).\n- Validate dates when assigning appointments.");
        root.add(new JScrollPane(area), BorderLayout.CENTER);

        return root;
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

    // Dialogs for Patients actions
    private void openAddPatientDialog() {
        // Mirror Admin's patient create form and use PatientService
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        JTextField fn = new JTextField(); JTextField ln = new JTextField(); JTextField phone = new JTextField();
        JTextField gender = new JTextField(); JTextField dob = new JTextField(); JTextField email = new JTextField(); JTextField addr = new JTextField();
        form.add(new JLabel("First name:")); form.add(fn);
        form.add(new JLabel("Last name:")); form.add(ln);
        form.add(new JLabel("DOB (YYYY-MM-DD):")); form.add(dob);
        form.add(new JLabel("Gender:")); form.add(gender);
        form.add(new JLabel("Phone:")); form.add(phone);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel("Address:")); form.add(addr);
        int res = JOptionPane.showConfirmDialog(this, form, "Add Patient", JOptionPane.OK_CANCEL_OPTION);
        if (res==JOptionPane.OK_OPTION) {
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(dob.getText().trim());
                PatientService.getInstance().createPatient(fn.getText().trim(), ln.getText().trim(), ld, gender.getText().trim(), phone.getText().trim(), email.getText().trim(), addr.getText().trim());
                JOptionPane.showMessageDialog(this, "Patient created.");
                // reload patients table
                reloadPatientsTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openViewPatientDialog() {
        int row = patientsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        DefaultTableModel m = (DefaultTableModel) patientsTable.getModel();
        String info = String.format("ID: %s\nName: %s\nDOB: %s\nGender: %s\nPhone: %s",
            m.getValueAt(row, 0), m.getValueAt(row, 1), m.getValueAt(row, 2), m.getValueAt(row, 3), m.getValueAt(row, 4));
        JOptionPane.showMessageDialog(this, info, "Patient Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openDeletePatientDialog() {
        int row = patientsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected patient?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel m = (DefaultTableModel) patientsTable.getModel();
            String id = (String) m.getValueAt(row, 0);
            boolean ok = PatientService.getInstance().deletePatient(id);
            if (ok) { JOptionPane.showMessageDialog(this, "Patient removed."); reloadPatientsTable(); }
            else JOptionPane.showMessageDialog(this, "Failed to remove patient.");
        }
    }

    private void openAssignAppointmentDialog() {
        // Seed drjohn availability (Mon-Wed 07:00-14:00) if a doctor record exists for that username
        try {
            Service.UserService.getInstance().findByUsername("drjohn").ifPresent(u -> {
                Service.DoctorServiceImpl.getInstance().listAll().stream().filter(d -> d.getUser()!=null && "drjohn".equalsIgnoreCase(d.getUser().getUsername())).findFirst().ifPresent(d -> {
                    boolean hasMon = false;
                    try {
                        java.util.List<Model.DoctorSchedule> scheds = listDoctorSchedules(d.getDoctorId());
                        for (Model.DoctorSchedule s : scheds) if (s.getDayOfWeek()==java.time.DayOfWeek.MONDAY) { hasMon = true; break; }
                    } catch (Throwable ignored) {}
                    if (!hasMon) {
                        saveDoctorSchedule(new Model.DoctorSchedule(d, java.time.DayOfWeek.MONDAY, java.time.LocalTime.of(7,0), java.time.LocalTime.of(14,0), true));
                        saveDoctorSchedule(new Model.DoctorSchedule(d, java.time.DayOfWeek.TUESDAY, java.time.LocalTime.of(7,0), java.time.LocalTime.of(14,0), true));
                        saveDoctorSchedule(new Model.DoctorSchedule(d, java.time.DayOfWeek.WEDNESDAY, java.time.LocalTime.of(7,0), java.time.LocalTime.of(14,0), true));
                    }
                });
            });
        } catch (Exception ignored) {}

        int row = patientsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        DefaultTableModel m = (DefaultTableModel) patientsTable.getModel();
        String patientId = (String) m.getValueAt(row, 0);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Doctor:"));
        // build doctor dropdown from DoctorServiceImpl
        java.util.List<Model.Doctor> docs = new java.util.ArrayList<>(Service.DoctorServiceImpl.getInstance().listAll());
        java.util.Vector<String> docLabels = new java.util.Vector<>();
        java.util.Map<String, Model.Doctor> docByLabel = new java.util.HashMap<>();
        for (Model.Doctor d : docs) {
            String uname = d.getUser()!=null?d.getUser().getUsername():d.getDoctorId();
            String label = uname + " (" + d.getDoctorId() + ")";
            docLabels.add(label);
            docByLabel.put(label, d);
        }
        if (docLabels.isEmpty()) docLabels.add("(no doctors available)");
        JComboBox<String> doctorBox = new JComboBox<>(docLabels);
        panel.add(doctorBox);
        panel.add(new JLabel("Available slot:")); JComboBox<String> slotBox = new JComboBox<>(new String[]{"-- select doctor first --"}); panel.add(slotBox);
        panel.add(new JLabel("Date (YYYY-MM-DD):")); JTextField date = new JTextField(); panel.add(date);
        panel.add(new JLabel("Time (HH:MM optional):")); JTextField time = new JTextField(); panel.add(time);

        // update slotBox when doctor selection changes
        doctorBox.addActionListener(e -> {
            String sel = (String) doctorBox.getSelectedItem();
            slotBox.removeAllItems();
            if (sel == null) return;
            Model.Doctor dd = docByLabel.get(sel);
            if (dd == null) return;
            java.util.List<Model.DoctorSchedule> slots = listDoctorSchedules(dd.getDoctorId());
            for (Model.DoctorSchedule s : slots) {
                 if (!s.isAvailable()) continue;
                 slotBox.addItem(s.getDayOfWeek().name() + " " + s.getTimeStart().toString() + "-" + s.getTimeEnd().toString());
             }
             if (slotBox.getItemCount()==0) slotBox.addItem("(no available slots)");
         });

        int result;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Assign Appointment", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setLayout(new BorderLayout()); JScrollPane sp = new JScrollPane(panel); dlg.getContentPane().add(sp, BorderLayout.CENTER);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton ok = new JButton("Assign"); JButton cancel = new JButton("Cancel"); foot.add(cancel); foot.add(ok); dlg.getContentPane().add(foot, BorderLayout.SOUTH);
        final int[] picked = {JOptionPane.CANCEL_OPTION}; ok.addActionListener(e -> { picked[0] = JOptionPane.OK_OPTION; dlg.dispose(); }); cancel.addActionListener(e -> { picked[0] = JOptionPane.CANCEL_OPTION; dlg.dispose(); });
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); dlg.setUndecorated(true); dlg.setBounds(0,0, screen.width, screen.height); dlg.setVisible(true); result = picked[0];
        if (result == JOptionPane.OK_OPTION) {
            String dateStr = date.getText().trim();
            if (dateStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Date required.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            java.time.LocalDate parsed;
            try {
                parsed = java.time.LocalDate.parse(dateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
            java.time.LocalDate today = java.time.LocalDate.now();
            if (parsed.isBefore(today)) {
                JOptionPane.showMessageDialog(this, "Cannot assign an appointment in the past.", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
            if (parsed.isAfter(today.plusYears(2))) { // arbitrary future cap
                JOptionPane.showMessageDialog(this, "Date too far in the future.", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
            // parse time (optional)
            java.time.LocalTime lt = java.time.LocalTime.of(9,0);
            String timeStr = time.getText().trim();
            if (!timeStr.isEmpty()) {
                try { lt = java.time.LocalTime.parse(timeStr);
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:MM.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            }
            String sel = (String) doctorBox.getSelectedItem();
            if (sel == null || sel.startsWith("(no doctors")) { JOptionPane.showMessageDialog(this, "Doctor required.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            Model.Doctor chosenDoc = docByLabel.get(sel);
            if (chosenDoc == null) { JOptionPane.showMessageDialog(this, "Doctor selection invalid.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            try {
                java.time.LocalDateTime when = java.time.LocalDateTime.of(parsed, lt);
                Appointment a = AppointmentService.getInstance().schedule(patientId, chosenDoc.getDoctorId(), when, "" + ((slotBox.getSelectedItem()!=null)?slotBox.getSelectedItem().toString():"") + " " + timeStr);
                JOptionPane.showMessageDialog(this, "Appointment assigned on " + when + " to " + sel);
                refreshAppointments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to schedule appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void acceptSelectedAppointment() {
        int row = appointmentsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
        DefaultTableModel m = (DefaultTableModel) appointmentsTable.getModel();
        String apptId = (String) m.getValueAt(row, 0);
        String status = null;
        try { status = (String) m.getValueAt(row, 6); } catch (Exception ignored) {}
        if (status != null && !status.equalsIgnoreCase("PENDING")) {
            JOptionPane.showMessageDialog(this, "Only PENDING appointments can be accepted. Current status: " + status, "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Accept selected appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            AppointmentService.getInstance().approve(apptId);
            JOptionPane.showMessageDialog(this, "Appointment accepted.");
            refreshAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to accept appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // REPORTS PANEL ----------------------------------------------------
    private JPanel buildReportsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top area: header (left), search (center), actions (right)
        JPanel reportsTop = new JPanel(new BorderLayout(8,8)); reportsTop.setOpaque(false);
        JLabel repHeader = new JLabel("Reports History", SwingConstants.LEFT); repHeader.setFont(FONT_SECTION); repHeader.setForeground(COLOR_PRIMARY.darker()); reportsTop.add(repHeader, BorderLayout.WEST);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); searchPanel.setOpaque(false); searchPanel.add(new JLabel("Search Reports:")); JTextField searchField = new JTextField(20); searchPanel.add(searchField); reportsTop.add(searchPanel, BorderLayout.CENTER);
        JPanel reportsAction = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); reportsAction.setOpaque(false); JButton btnExport = new JButton("Export"); styleSecondaryButton(btnExport); btnExport.addActionListener(e -> openExportReportsDialog()); reportsAction.add(btnExport); reportsTop.add(reportsAction, BorderLayout.EAST);
        root.add(reportsTop, BorderLayout.NORTH);

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterReportsTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterReportsTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterReportsTable(searchField.getText()); }
        });

        String[] cols = {"Date", "Patient", "Type", "Status"};
        Object[][] data = {{"2025-01-10", "Alice Johnson", "Lab", "Completed"}, {"2025-01-11", "Bob Lee", "Imaging", "Pending"}};
        reportsTable = new JTable(new DefaultTableModel(data, cols));

        root.add(new JScrollPane(reportsTable), BorderLayout.CENTER);
        return root;
    }

    private void openExportReportsDialog() {
        String[] options = {"CSV", "Excel", "PDF"};
        int choice = JOptionPane.showOptionDialog(this, "Choose export format:", "Export Reports", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice >= 0) {
            JOptionPane.showMessageDialog(this, "Reports exported as " + options[choice] + ".");
        }
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    @Override
    public Map<String, JTable> getSearchableTables() {
        Map<String, JTable> map = new LinkedHashMap<>();
        if (patientsTable != null) map.put("patients", patientsTable);
        if (reportsTable != null) map.put("reports", reportsTable);
        return map;
    }

    @Override
    public void applyGlobalSearch(String query) {
        globalSearchQuery = (query == null || query.isBlank()) ? null : query.trim();
        refreshAllFilters();
    }

    @Override
    public void clearGlobalSearch() { globalSearchQuery = null; refreshAllFilters(); }

    @Override
    public void applyGlobalFilter(String tableName, String columnName, String value) {
        if (tableName == null || columnName == null) return;
        Map<String,String> map = columnFilters.computeIfAbsent(tableName, k -> new HashMap<>());
        if (value == null || value.isBlank()) { map.remove(columnName); if (map.isEmpty()) columnFilters.remove(tableName); }
        else map.put(columnName, value.trim());
        JTable t = getSearchableTables().get(tableName);
        if (t != null) applyFiltersToTable(tableName, t);
    }

    @Override
    public void clearGlobalFilter() { columnFilters.clear(); refreshAllFilters(); }

    private void refreshAllFilters() { getSearchableTables().forEach(this::applyFiltersToTable); }

    @SuppressWarnings("unchecked")
    private void applyFiltersToTable(String logicalName, JTable table) {
        if (table.getRowSorter() == null) table.setRowSorter(new TableRowSorter<>(table.getModel()));
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        List<RowFilter<TableModel,Object>> filters = new ArrayList<>();
        if (globalSearchQuery != null) {
            final String q = globalSearchQuery.toLowerCase();
            filters.add(new RowFilter<TableModel,Object>() {
                @Override public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
                    for (int i=0;i<entry.getValueCount();i++){ Object v=entry.getValue(i); if (v!=null && v.toString().toLowerCase().contains(q)) return true; }
                    return false;
                }
            });
        }
        Map<String,String> colMap = columnFilters.get(logicalName);
        if (colMap != null) {
            for (Map.Entry<String,String> e : colMap.entrySet()) {
                String colName = e.getKey(); String val = e.getValue(); if (val==null||val.isBlank()) continue;
                int colIndex; try { colIndex = table.getColumnModel().getColumnIndex(colName); } catch (IllegalArgumentException ex){ continue; }
                final String qv = val.toLowerCase();
                filters.add(new RowFilter<TableModel,Object>() {
                    @Override public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
                        Object v = entry.getValue(colIndex); return v!=null && v.toString().toLowerCase().contains(qv);
                    }
                });
            }
        }
        if (filters.isEmpty()) sorter.setRowFilter(null);
        else if (filters.size()==1) sorter.setRowFilter(filters.get(0));
        else sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private void filterPatientsTable(String query) {
        if (patientsTable.getRowSorter() == null) {
            patientsTable.setRowSorter(new TableRowSorter<>(patientsTable.getModel()));
        }
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) patientsTable.getRowSorter();
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query.trim()));
        }
    }

    private void filterReportsTable(String query) {
        if (reportsTable.getRowSorter() == null) {
            reportsTable.setRowSorter(new TableRowSorter<>(reportsTable.getModel()));
        }
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) reportsTable.getRowSorter();
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query.trim()));
        }
    }

    // APPOINTMENT REQUESTS PANEL --------------------------------------
    private JPanel buildAppointmentRequestsPanel() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12,12,12,12));

        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel header = new JLabel("Appointment Requests", SwingConstants.LEFT); header.setFont(FONT_SECTION); header.setForeground(COLOR_PRIMARY.darker()); top.add(header, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh"); styleSecondaryButton(btnRefresh); btnRefresh.addActionListener(e -> refreshRequestList());
        JButton btnAccept = new JButton("Accept"); styleSecondaryButton(btnAccept); btnAccept.addActionListener(e -> acceptSelectedRequest());
        JButton btnReject = new JButton("Reject"); styleSecondaryButton(btnReject); btnReject.addActionListener(e -> rejectSelectedRequest());
        actions.add(btnRefresh); actions.add(btnAccept); actions.add(btnReject);
        top.add(actions, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Req ID", "Patient", "Requested By", "When", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){ return false; } };
        requestsTable = new JTable(model);
        root.add(new JScrollPane(requestsTable), BorderLayout.CENTER);

        refreshRequestList();
        return root;
    }

    private void refreshRequestList() {
        DefaultTableModel m = (DefaultTableModel) requestsTable.getModel();
        m.setRowCount(0);
        for (Appointment a : AppointmentService.getInstance().listAll()) {
            // treat PENDING as incoming requests
            if (a.getStatus() != null && a.getStatus().name().equalsIgnoreCase("PENDING")) {
                // If currentUsername set, show only requests addressed to this doctor
                String doc = a.getStaffId();
                if (this.currentUsername != null && !this.currentUsername.isBlank()) {
                    if (!this.currentUsername.equalsIgnoreCase(doc) && !doc.equalsIgnoreCase(this.currentUsername)) continue;
                }
                String patientName = a.getPatientId();
                java.util.Optional<Model.Patient> pat = PatientService.getInstance().findById(a.getPatientId());
                if (pat.isPresent()) {
                    Model.Patient pp = pat.get();
                    patientName = (pp.getFirstName()==null?"":pp.getFirstName()) + " " + (pp.getLastName()==null?"":pp.getLastName());
                }
                m.addRow(new Object[]{a.getId(), patientName, a.getStaffId(), dtf.format(a.getScheduledAt()), a.getReason(), a.getStatus().name()});
            }
        }
    }

    private void acceptSelectedRequest() {
        int row = requestsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a request first."); return; }
        DefaultTableModel m = (DefaultTableModel) requestsTable.getModel();
        String reqId = (String) m.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Accept this appointment request?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            AppointmentService.getInstance().approve(reqId);
            // refresh both lists
            refreshRequestList();
            refreshAppointments();
            JOptionPane.showMessageDialog(this, "Request accepted — moved to Appointments.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to accept request: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelectedRequest() {
        int row = requestsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a request first."); return; }
        DefaultTableModel m = (DefaultTableModel) requestsTable.getModel();
        String reqId = (String) m.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Reject (cancel) this appointment request?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            AppointmentService.getInstance().cancel(reqId);
            refreshRequestList();
            // optionally refresh patient requests/appointments
            refreshAppointments();
            JOptionPane.showMessageDialog(this, "Request rejected — moved to cancelled.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to reject request: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper: attempt to list doctor schedules via reflection if Service.DoctorScheduleService exists
    @SuppressWarnings("unchecked")
    private java.util.List<Model.DoctorSchedule> listDoctorSchedules(String doctorId) {
        try {
            Class<?> cls = Class.forName("Service.DoctorScheduleService");
            Object inst = cls.getMethod("getInstance").invoke(null);
            java.lang.reflect.Method m = null;
            for (java.lang.reflect.Method mm : cls.getMethods()) {
                if ("listByDoctorId".equals(mm.getName()) && mm.getParameterCount() == 1 && mm.getParameterTypes()[0] == String.class) { m = mm; break; }
            }
            if (m != null) {
                Object res = m.invoke(inst, doctorId);
                if (res instanceof java.util.List) return (java.util.List<Model.DoctorSchedule>) res;
            }
        } catch (Throwable ignored) {}
        return new ArrayList<>();
    }

    // Helper: attempt to save a doctor schedule via reflection if Service.DoctorScheduleService exists
    private void saveDoctorSchedule(Model.DoctorSchedule ds) {
        try {
            Class<?> cls = Class.forName("Service.DoctorScheduleService");
            Object inst = cls.getMethod("getInstance").invoke(null);
            java.lang.reflect.Method saveMethod = null;
            for (java.lang.reflect.Method mm : cls.getMethods()) {
                if ("save".equals(mm.getName()) && mm.getParameterCount() == 1) { saveMethod = mm; break; }
            }
            if (saveMethod != null) saveMethod.invoke(inst, ds);
        } catch (Throwable ignored) {}
    }
}
