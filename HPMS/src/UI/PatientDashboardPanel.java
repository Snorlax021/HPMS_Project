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
import java.util.regex.Pattern;

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
    private JButton activeButton;

    // Tables / components for future data binding
    private JTable appointmentsTable;
    private JTable billsTable;
    private JTable labTable;
    private JTable servicesTable;
    // Global search/filter state
    private String globalSearchQuery;
    private final Map<String, Map<String,String>> columnFilters = new HashMap<>();

    // Dashboard dynamic labels
    private JLabel lblUpcomingAppts;
    private JLabel lblPendingBills;
    private JLabel lblLabResults;

    // Missing profileArea field re-added
    private JTextArea profileArea;

    public PatientDashboardPanel() {
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
        JLabel title = new JLabel("Patient Dashboard", SwingConstants.LEFT);
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY.darker());
        title.setBorder(new EmptyBorder(0, 16, 0, 0));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);
        JButton btnHelp = new JButton("Help");
        styleSecondaryButton(btnHelp);
        btnHelp.addActionListener(e -> JOptionPane.showMessageDialog(this, "Support placeholder.", "Help", JOptionPane.INFORMATION_MESSAGE));
        right.add(btnHelp);

        header.add(title, BorderLayout.WEST);
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
        btnServices = createNavButton("Hospital Services", "SERVICES"); // Initialize new button

        sideNavPanel.add(Box.createVerticalStrut(6));
        sideNavPanel.add(btnSummary);
        sideNavPanel.add(btnProfile);
        sideNavPanel.add(btnAppointments);
        sideNavPanel.add(btnHistory);
        sideNavPanel.add(btnBills);
        sideNavPanel.add(btnLab);
        sideNavPanel.add(btnServices); 
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
        profileArea = new JTextArea("PERSONAL INFO\nName: \nAge: \nBlood Type: \nGender: \nAddress: \nDoctor: \n\nCONTACT INFO\nEmail: \nPhone: \n");
        profileArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        root.add(new JScrollPane(profileArea), BorderLayout.CENTER);
        JButton btnEdit = new JButton("Edit Profile");
        styleSecondaryButton(btnEdit);
        btnEdit.addActionListener(e -> JOptionPane.showMessageDialog(this, "Profile edit dialog placeholder."));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false); footer.add(btnEdit);
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
        appointmentsTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterAppointmentsTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterAppointmentsTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterAppointmentsTable(searchField.getText()); }
        });

        root.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "Schedule", this::openScheduleDialog);
        styleToolbarButton(toolbar, "View", this::openViewAppointment);
        styleToolbarButton(toolbar, "Cancel", this::openCancelAppointment);
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

        String[] cols = {"Date", "Description", "Amount"};
        Object[][] data = {{"2025-01-05", "Consultation", "$50"}, {"2025-01-07", "Lab Test", "$75"}};
        billsTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterBillsTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterBillsTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterBillsTable(searchField.getText()); }
        });

        root.add(new JScrollPane(billsTable), BorderLayout.CENTER);
        JButton payBtn = new JButton("Pay Selected");
        styleSecondaryButton(payBtn);
        payBtn.addActionListener(e -> openPayBillDialog());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false); footer.add(payBtn);
        root.add(footer, BorderLayout.SOUTH);
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
        labTable = new JTable(new DefaultTableModel(data, cols));

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

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Hospital Services", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Services:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Service", "Doctor", "Price"};
        Object[][] data = {{"Consultation", "Dr. Smith", "$100"}, {"Surgery", "Dr. Adams", "$500"}, {"Lab Test", "Dr. Johnson", "$50"}, {"X-Ray", "Dr. Lee", "$75"}};
        servicesTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterServicesTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterServicesTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterServicesTable(searchField.getText()); }
        });

        root.add(new JScrollPane(servicesTable), BorderLayout.CENTER);
        return root;
    }

    // GUIDE ------------------------------------------------------------
    private JPanel buildGuidePanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(sectionHeader("User Guide"), BorderLayout.NORTH);
        JTextArea area = new JTextArea("FAQs and instructions:\n\n1. Profile: view or edit your personal info.\n2. Appointments: manage schedule.\n3. History: view medical records.\n4. Billing: see outstanding payments.\n5. Lab: view test results.\n6. Hospital Services: view available services, doctors, and prices.\n7. Summary: overview of activity.");
        area.setEditable(false);
        area.setFont(FONT_NORMAL);
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
    private void openScheduleDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Date (YYYY-MM-DD):")); JTextField date = new JTextField(); panel.add(date);
        panel.add(new JLabel("Time (HH:MM):")); JTextField time = new JTextField(); panel.add(time);
        panel.add(new JLabel("Type:")); JComboBox<String> type = new JComboBox<>(new String[]{"Consultation", "Follow-up", "Procedure"}); panel.add(type);
        int res = JOptionPane.showConfirmDialog(this, panel, "Schedule Appointment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String dateStr = date.getText().trim();
            if (dateStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Date required.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            java.time.LocalDate parsed;
            try { parsed = java.time.LocalDate.parse(dateStr); } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
            java.time.LocalDate today = java.time.LocalDate.now();
            if (parsed.isBefore(today)) { JOptionPane.showMessageDialog(this, "You cannot schedule an appointment in the past.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (parsed.isAfter(today.plusYears(2))) { JOptionPane.showMessageDialog(this, "Date too far in the future.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            DefaultTableModel m = (DefaultTableModel) appointmentsTable.getModel();
            m.addRow(new Object[]{parsed.toString(), time.getText().trim(), "Dr. Smith", type.getSelectedItem().toString()});
            JOptionPane.showMessageDialog(this, "Appointment scheduled.");
        }
    }
    private void openViewAppointment() {
        int row = appointmentsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
        DefaultTableModel m = (DefaultTableModel) appointmentsTable.getModel();
        String info = String.format("Date: %s\nTime: %s\nDoctor: %s\nType: %s", m.getValueAt(row,0), m.getValueAt(row,1), m.getValueAt(row,2), m.getValueAt(row,3));
        JOptionPane.showMessageDialog(this, info, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }
    private void openCancelAppointment() {
        int row = appointmentsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ((DefaultTableModel) appointmentsTable.getModel()).removeRow(row);
            JOptionPane.showMessageDialog(this, "Appointment canceled.");
        }
    }
    private void openPayBillDialog() {
        int row = billsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a bill first."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Mark this bill as paid?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) JOptionPane.showMessageDialog(this, "Bill paid (placeholder).");
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
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1));
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
}