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

public class StaffDashboardPanel extends JPanel implements GlobalSearchable {
    private static final long serialVersionUID = 1L;
    // THEME (aligned with Admin/Doctor/Patient dashboards)
    private static final Color COLOR_BG = Color.WHITE;
    private static final Color COLOR_SIDEBAR_BG = new Color(245, 247, 250);
    private static final Color COLOR_PRIMARY = new Color(60, 120, 200);
    private static final Color COLOR_PRIMARY_HOVER = new Color(80, 140, 220);
    private static final Color COLOR_ACTIVE = new Color(100, 160, 240);
    private static final Color COLOR_BORDER = new Color(210, 215, 220);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

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

    private String subRole; // REGISTRATION, BILLING, LAB (optional)

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

    public StaffDashboardPanel() { this(null); }
    public StaffDashboardPanel(String subRole) {
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
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new LineBorder(COLOR_BORDER));
        header.setPreferredSize(new Dimension(0, 55));

        JLabel title = new JLabel("Staff Dashboard", SwingConstants.LEFT);
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY.darker());
        title.setBorder(new EmptyBorder(0, 16, 0, 0));
        header.add(title, BorderLayout.WEST);

        if (subRole != null) {
            JLabel roleBanner = new JLabel("Sub-Role: " + subRole, SwingConstants.RIGHT);
            roleBanner.setFont(new Font("Segoe UI", Font.BOLD, 14));
            roleBanner.setForeground(COLOR_PRIMARY);
            roleBanner.setBorder(new EmptyBorder(0, 0, 0, 16));
            header.add(roleBanner, BorderLayout.EAST);
        }
        return header;
    }

    private JComponent createSideBar() {
        sideNavPanel = new JPanel();
        sideNavPanel.setLayout(new BoxLayout(sideNavPanel, BoxLayout.Y_AXIS));
        sideNavPanel.setBackground(COLOR_SIDEBAR_BG);
        sideNavPanel.setBorder(new LineBorder(COLOR_BORDER));
        sideNavPanel.setPreferredSize(new Dimension(190, 0));

        btnSummary = createNavButton("Summary", "SUMMARY");
        btnPatientReg = createNavButton("Patient Registration", "PATIENT_REG");
        btnMedical = createNavButton("Medical Records", "MEDICAL");
        btnBilling = createNavButton("Billing History", "BILLING");
        btnLab = createNavButton("Lab Tests", "LAB");
        btnAdmission = createNavButton("Admission", "ADMISSION");

        sideNavPanel.add(Box.createVerticalStrut(6));
        sideNavPanel.add(btnSummary);
        sideNavPanel.add(btnPatientReg);
        sideNavPanel.add(btnMedical);
        sideNavPanel.add(btnBilling);
        sideNavPanel.add(btnLab);
        sideNavPanel.add(btnAdmission);
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
        cardLayout.show(mainContentPanel, card);
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

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Patient Registration & Records", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Patients:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Age", "Gender", "Status"};
        Object[][] data = {{1, "John Doe", 45, "M", "Active"}, {2, "Jane Smith", 29, "F", "Inactive"}};
        patientRegTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterPatientRegTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterPatientRegTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterPatientRegTable(searchField.getText()); }
        });

        root.add(new JScrollPane(patientRegTable), BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar(); toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "Add", this::openAddPatientDialog);
        styleToolbarButton(toolbar, "View", this::openViewPatientDialog);
        styleToolbarButton(toolbar, "Deactivate", this::openDeactivatePatientDialog);
        root.add(toolbar, BorderLayout.SOUTH);
        return root;
    }

    // MEDICAL RECORDS -------------------------------------------------
    private JPanel buildMedicalPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Medical & Treatment Records", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Records:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

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

        JToolBar toolbar = new JToolBar(); toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "Add Record", this::openAddMedicalRecordDialog);
        styleToolbarButton(toolbar, "View", this::openViewMedicalRecordDialog);
        styleToolbarButton(toolbar, "Remove", this::openDeleteMedicalRecordDialog);
        root.add(toolbar, BorderLayout.SOUTH);
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

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT)); footer.setOpaque(false);
        JButton exportBtn = new JButton("Export"); styleSecondaryButton(exportBtn); exportBtn.addActionListener(e -> openExportBillingDialog());
        JButton markPaidBtn = new JButton("Mark Paid"); styleSecondaryButton(markPaidBtn); markPaidBtn.addActionListener(e -> openMarkPaidDialog());
        footer.add(exportBtn); footer.add(markPaidBtn);
        root.add(footer, BorderLayout.SOUTH);
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

        JToolBar toolbar = new JToolBar(); toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "Add Test", this::openAddLabTestDialog);
        styleToolbarButton(toolbar, "Update", this::openUpdateLabTestDialog);
        styleToolbarButton(toolbar, "Complete", this::openCompleteLabTestDialog);
        root.add(toolbar, BorderLayout.SOUTH);
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

        JToolBar toolbar = new JToolBar(); toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "Admit", this::openAdmitPatientDialog);
        styleToolbarButton(toolbar, "Discharge", this::openDischargePatientDialog);
        styleToolbarButton(toolbar, "Transfer", this::openTransferPatientDialog);
        root.add(toolbar, BorderLayout.SOUTH);
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
        JPanel panel = new JPanel(new GridLayout(5,2,8,8)); panel.setBorder(new EmptyBorder(10,10,10,10));
        JTextField name = field(panel, "Name:"); JTextField age = field(panel, "Age:"); JTextField gender = field(panel, "Gender:"); JTextField status = field(panel, "Status:");
        field(panel, "Notes:"); // placeholder unused
        int r = showDialog(panel, "Add Patient");
        if (r == JOptionPane.OK_OPTION) {
            if (!name.getText().isEmpty()) {
                DefaultTableModel m = (DefaultTableModel) patientRegTable.getModel();
                m.addRow(new Object[]{m.getRowCount()+1, name.getText(), parseIntSafe(age.getText()), gender.getText(), status.getText()});
            } else warn("Name required");
        }
    }
    private void openViewPatientDialog() {
        int row = patientRegTable.getSelectedRow(); if (row==-1){warn("Select a patient first"); return;}
        DefaultTableModel m=(DefaultTableModel)patientRegTable.getModel();
        info(String.format("ID: %s\nName: %s\nAge: %s\nGender: %s\nStatus: %s",m.getValueAt(row,0),m.getValueAt(row,1),m.getValueAt(row,2),m.getValueAt(row,3),m.getValueAt(row,4)));
    }
    private void openDeactivatePatientDialog() {
        int row = patientRegTable.getSelectedRow(); if (row==-1){warn("Select a patient first"); return;}
        int c = confirm("Deactivate this patient?"); if (c==JOptionPane.YES_OPTION){ ((DefaultTableModel)patientRegTable.getModel()).setValueAt("Inactive", row, 4); info("Patient deactivated."); }
    }

    // Medical Records dialogs
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

    // Billing dialogs
    private void openExportBillingDialog() { info("Export billing (placeholder)"); }
    private void openMarkPaidDialog() {
        int r=billingTable.getSelectedRow(); if(r==-1){warn("Select a bill first"); return;} ((DefaultTableModel)billingTable.getModel()).setValueAt("Paid", r, 4); info("Marked as paid.");
    }

    // Lab dialogs
    private void openAddLabTestDialog() { JPanel p=new JPanel(new GridLayout(3,2,8,8)); p.setBorder(new EmptyBorder(10,10,10,10)); JTextField patient=field(p,"Patient:"); JTextField test=field(p,"Test:"); field(p,"Notes:"); if(showDialog(p,"Add Lab Test")==JOptionPane.OK_OPTION){ if(!patient.getText().isEmpty()){ DefaultTableModel m=(DefaultTableModel)labTable.getModel(); m.addRow(new Object[]{m.getRowCount()+501, patient.getText(), test.getText(), "Pending"}); info("Lab test added."); } else warn("Patient required"); } }
    private void openUpdateLabTestDialog() { int r=labTable.getSelectedRow(); if(r==-1){warn("Select test first"); return;} ((DefaultTableModel)labTable.getModel()).setValueAt("In Progress", r, 3); info("Status updated."); }
    private void openCompleteLabTestDialog() { int r=labTable.getSelectedRow(); if(r==-1){warn("Select test first"); return;} ((DefaultTableModel)labTable.getModel()).setValueAt("Completed", r, 3); info("Test completed."); }

    // Admission dialogs
    private void openAdmitPatientDialog() { JPanel p=new JPanel(new GridLayout(3,2,8,8)); p.setBorder(new EmptyBorder(10,10,10,10)); JTextField patient=field(p,"Patient:"); JTextField room=field(p,"Room:"); field(p,"Notes:"); if(showDialog(p,"Admit Patient")==JOptionPane.OK_OPTION){ if(!patient.getText().isEmpty()){ DefaultTableModel m=(DefaultTableModel)admissionTable.getModel(); m.addRow(new Object[]{m.getRowCount()+801, patient.getText(), room.getText(), "Admitted"}); info("Patient admitted."); } else warn("Patient required"); } }
    private void openDischargePatientDialog() { int r=admissionTable.getSelectedRow(); if(r==-1){warn("Select admission first"); return;} ((DefaultTableModel)admissionTable.getModel()).setValueAt("Discharged", r, 3); info("Patient discharged."); }
    private void openTransferPatientDialog() { int r=admissionTable.getSelectedRow(); if(r==-1){warn("Select admission first"); return;} ((DefaultTableModel)admissionTable.getModel()).setValueAt("Transferred", r, 3); info("Patient transferred."); }

    // SMALL UTILS -----------------------------------------------------
    private JTextField field(JPanel p, String label){ p.add(new JLabel(label)); JTextField f=new JTextField(); p.add(f); return f; }
    private int showDialog(JPanel panel, String title){ return JOptionPane.showConfirmDialog(this,panel,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE); }
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
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) patientRegTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(patientRegTable.getModel());
            patientRegTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 3, 4));
        }
    }
    private void filterMedicalTable(String query) {
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