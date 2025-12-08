package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
import javax.swing.event.*;
import Model.Role;
import Model.User;
import Service.UserService;
import Service.PatientService;
import hospital.controller.AdminController;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class AdminDashboardPanel extends JPanel implements GlobalSearchable {
    private static final long serialVersionUID = 1L;

    // controller reference (optional)
    private final AdminController adminController;

    // THEME CONSTANTS
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
    private JButton btnUsers;
    private JButton btnPayments;
    private JButton btnSummary;
    private JButton activeButton;

    // Dashboard dynamic labels
    private JLabel lblPatientsValue;
    private JLabel lblDoctorsValue;
    private JLabel lblStaffValue;
    private JLabel lblRevenueValue;

    // Tables (exposed for future data binding)
    private JTable userTable;
    private JTable paymentTable;
    // Global search/filter state
    private String globalSearchQuery;
    private final Map<String, Map<String, String>> columnFilters = new HashMap<>();
    private final UserService userService = UserService.getInstance();
    private final String currentUsername;
    // NEW: username label reference to control visibility
    private JLabel userTagLabel;

    public AdminDashboardPanel() { this(null, null); }
    public AdminDashboardPanel(String username) { this(null, username); }
    public AdminDashboardPanel(AdminController controller, String username) {
        this.adminController = controller;
        this.currentUsername = username;
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new BorderLayout(8, 8));

        add(createHeader(), BorderLayout.NORTH);
        add(createSideBar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        // Default view
        setActiveButton(btnDashboard, "DASHBOARD");
    }

    // HEADER BAR -------------------------------------------------------
    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new LineBorder(COLOR_BORDER));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 55));

        // Left container: title only — username removed
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        left.setOpaque(false);
        // Do not add username label anymore
        userTagLabel = null;

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_PRIMARY.darker());
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh Data");
        styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> JOptionPane.showMessageDialog(this, "Data refreshed (placeholder)", "Info", JOptionPane.INFORMATION_MESSAGE));
        right.add(btnRefresh);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // SIDEBAR ----------------------------------------------------------
    private JComponent createSideBar() {
        sideNavPanel = new JPanel();
        sideNavPanel.setLayout(new BoxLayout(sideNavPanel, BoxLayout.Y_AXIS));
        sideNavPanel.setBackground(COLOR_SIDEBAR_BG);
        sideNavPanel.setBorder(new LineBorder(COLOR_BORDER));
        sideNavPanel.setPreferredSize(new Dimension(260, 0));

    //Dito magaadd ng buttons para sa side bar
        btnDashboard = createNavButton("Dashboard", "DASHBOARD");
        btnUsers = createNavButton("User Management", "USERS");
        btnPayments = createNavButton("Payments", "PAYMENTS");
        btnSummary = createNavButton("Summary", "SUMMARY");
        JButton btnGuide = createNavButton("User Guide", "GUIDE");

        int gap = 12;
        sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnDashboard); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnUsers); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnPayments); sideNavPanel.add(Box.createVerticalStrut(gap));
        sideNavPanel.add(btnSummary); sideNavPanel.add(Box.createVerticalStrut(gap));
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
        // Username no longer shown
        cardLayout.show(mainContentPanel, card);
    }

    // MAIN CONTENT -----------------------------------------------------
    private JComponent createMainContent() {
        mainContentPanel = new JPanel();
        cardLayout = new CardLayout();
        mainContentPanel.setLayout(cardLayout);
        mainContentPanel.setBorder(new LineBorder(COLOR_BORDER));

        mainContentPanel.add(buildDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(buildUserPanel(), "USERS");
        mainContentPanel.add(buildPaymentPanel(), "PAYMENTS");
        mainContentPanel.add(buildSummaryPanel(), "SUMMARY");
        mainContentPanel.add(buildGuidePanel(), "GUIDE");
        return mainContentPanel;
    }

    // DASHBOARD PANEL --------------------------------------------------
    private JPanel buildDashboardPanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("System Overview", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 12, 12));
        statsGrid.setOpaque(false);

        lblPatientsValue = new JLabel("0", SwingConstants.CENTER);
        lblDoctorsValue = new JLabel("0", SwingConstants.CENTER);
        lblStaffValue = new JLabel("0", SwingConstants.CENTER);
        lblRevenueValue = new JLabel("$0.00", SwingConstants.CENTER);

        statsGrid.add(createStatCard("Patients", lblPatientsValue));
        statsGrid.add(createStatCard("Doctors", lblDoctorsValue));
        statsGrid.add(createStatCard("Staff", lblStaffValue));
        statsGrid.add(createStatCard("Revenue", lblRevenueValue));

        root.add(statsGrid, BorderLayout.CENTER);

        JTextArea info = new JTextArea("Welcome to the Admin Dashboard.\nUse sidebar navigation to manage users, review payments, and view summaries.\nThis layout is responsive and ready for data binding.");
        info.setFont(FONT_NORMAL);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBorder(new EmptyBorder(8, 12, 8, 12));
        root.add(new JScrollPane(info), BorderLayout.SOUTH);
        return root;
    }

    private JPanel createStatCard(String label, JLabel valueLabel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new LineBorder(COLOR_BORDER));
        wrapper.setPreferredSize(new Dimension(160, 120));

        JLabel title = new JLabel(label, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        title.setForeground(COLOR_PRIMARY.darker());
        title.setBorder(new EmptyBorder(6, 6, 0, 6));
        wrapper.add(title, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(COLOR_PRIMARY);
        wrapper.add(valueLabel, BorderLayout.CENTER);

        return wrapper;
    }

    // USER MANAGEMENT PANEL --------------------------------------------
    private JPanel buildUserPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("User Management", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Users:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "Add", this::openAddUserDialog);
        styleToolbarButton(toolbar, "Edit", this::openEditUserDialog);
        styleToolbarButton(toolbar, "Reset Password", this::openResetPasswordDialog);
        styleToolbarButton(toolbar, "Delete", this::openDeleteUserDialog);
        styleToolbarButton(toolbar, "Export", this::openExportDialog);
        root.add(toolbar, BorderLayout.SOUTH);

        String[] cols = {"Username", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        userTable = new JTable(model);
        // load users from service
        reloadUsersTable();

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterUserTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterUserTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterUserTable(searchField.getText()); }
        });

        root.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return root;
    }

    private void reloadUsersTable() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);
        for (User u : userService.getAllUsers()) {
            model.addRow(new Object[]{u.getUsername(), u.getRole().name()});
        }
    }

    // PAYMENT PANEL ----------------------------------------------------
    private JPanel buildPaymentPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("Payment History", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search Payments:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Date", "Name", "Amount", "Description"};
        Object[][] data = {{"2025-01-10", "Patient A", "$120.00", "Consultation"}, {"2025-01-11", "Patient B", "$450.00", "Procedure"}};
        paymentTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterPaymentTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterPaymentTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterPaymentTable(searchField.getText()); }
        });

        root.add(new JScrollPane(paymentTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        JButton btnExport = new JButton("Export CSV");
        styleSecondaryButton(btnExport);
        btnExport.addActionListener(e -> openExportPaymentDialog());
        footer.add(btnExport);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    // SUMMARY PANEL ----------------------------------------------------
    private JPanel buildSummaryPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel header = new JLabel("Summary Report", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setFont(FONT_NORMAL);
        area.setText("Daily Summary Placeholder:\n\n- Patients Admitted: 12\n- Procedures Completed: 5\n- Discharges: 7\n\nAdd analytics, charts, and export features here.");
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        root.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton btnGenerate = new JButton("Generate Detailed Report");
        styleSecondaryButton(btnGenerate);
        btnGenerate.addActionListener(e -> openGenerateSummaryDialog());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnGenerate);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    // USER GUIDE PANEL ----------------------------------------------------
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
            "Welcome to the Admin User Guide.\n\n" +
            "Navigation:\n- Use the left sidebar to access Dashboard, Users, Payments, Summary, and this Guide.\n\n" +
            "User Management:\n- Add, edit roles, reset passwords, delete, and export users from the Users tab.\n\n" +
            "Payments:\n- Review payment history and export reports.\n\n" +
            "Summary:\n- Generate daily/weekly/monthly/yearly summary reports.\n\n" +
            "Tips:\n- Use search boxes to quickly filter tables.\n- Right-hand buttons provide actions like Refresh and Export.\n- Changes reflect immediately in tables.");
        root.add(new JScrollPane(area), BorderLayout.CENTER);

        return root;
    }

    // PUBLIC API -------------------------------------------------------
    public void updateStats(int patients, int doctors, int staff, double revenue) {
        if (lblPatientsValue != null) lblPatientsValue.setText(String.valueOf(patients));
        if (lblDoctorsValue != null) lblDoctorsValue.setText(String.valueOf(doctors));
        if (lblStaffValue != null) lblStaffValue.setText(String.valueOf(staff));
        if (lblRevenueValue != null) lblRevenueValue.setText("$" + String.format("%,.2f", revenue));
    }

    public JTable getUserTable() { return userTable; }
    public JTable getPaymentTable() { return paymentTable; }

    // GLOBAL SEARCH IMPLEMENTATION ------------------------------------
    @Override
    public Map<String, JTable> getSearchableTables() {
        Map<String, JTable> map = new LinkedHashMap<>();
        if (userTable != null) map.put("users", userTable);
        if (paymentTable != null) map.put("payments", paymentTable);
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
        Map<String, String> map = columnFilters.computeIfAbsent(tableName, k -> new HashMap<>());
        if (value == null || value.isBlank()) {
            map.remove(columnName);
            if (map.isEmpty()) columnFilters.remove(tableName);
        } else {
            map.put(columnName, value.trim());
        }
        JTable table = getSearchableTables().get(tableName);
        if (table != null) applyFiltersToTable(tableName, table);
    }
    @Override
    public void clearGlobalFilter() {
        columnFilters.clear();
        refreshAllFilters();
    }
    private void refreshAllFilters() {
        getSearchableTables().forEach(this::applyFiltersToTable);
    }
    @SuppressWarnings("unchecked")
    private void applyFiltersToTable(String logicalName, JTable table) {
        if (table.getRowSorter() == null) {
            table.setRowSorter(new TableRowSorter<>(table.getModel()));
        }
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        List<RowFilter<TableModel, Object>> filters = new ArrayList<>();
        if (globalSearchQuery != null) {
            final String q = globalSearchQuery.toLowerCase();
            filters.add(new RowFilter<TableModel, Object>() {
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
                    int cols = entry.getValueCount();
                    for (int i = 0; i < cols; i++) {
                        Object v = entry.getValue(i);
                        if (v != null && v.toString().toLowerCase().contains(q)) return true;
                    }
                    return false;
                }
            });
        }
        Map<String, String> colMap = columnFilters.get(logicalName);
        if (colMap != null) {
            for (Map.Entry<String, String> e : colMap.entrySet()) {
                String colName = e.getKey();
                String val = e.getValue();
                if (val == null || val.isBlank()) continue;
                int colIndex;
                try { colIndex = table.getColumnModel().getColumnIndex(colName); } catch (IllegalArgumentException ex) { continue; }
                final String qv = val.toLowerCase();
                filters.add(new RowFilter<TableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
                        Object v = entry.getValue(colIndex);
                        return v != null && v.toString().toLowerCase().contains(qv);
                    }
                });
            }
        }
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            sorter.setRowFilter(filters.get(0));
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // DIALOG METHODS ---------------------------------------------------
    private void openAddUserDialog() {
        // Step 1: pick role
        JPanel pick = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pick.add(new JLabel("Select Role:"));
        JComboBox<Role> roleSelector = new JComboBox<>(Role.values());
        roleSelector.setSelectedItem(Role.STAFF);
        pick.add(roleSelector);

        int pickRes = JOptionPane.showConfirmDialog(this, pick, "Add New User - Choose Role", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (pickRes != JOptionPane.OK_OPTION) return;
        Role chosen = (Role) roleSelector.getSelectedItem();
        if (chosen == null) return;

        if (chosen == Role.DOCTOR || chosen == Role.STAFF) {
            // Build a two-column form for professionals (left/right) but show it in a resizable modal JDialog
            JPanel container = new JPanel(new BorderLayout(12,12));
            container.setBorder(new EmptyBorder(8,8,8,8));

            JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setBackground(Color.WHITE);
            JPanel right = new JPanel(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS)); right.setBackground(Color.WHITE);

            // Personal block (left)
            JPanel personal = new JPanel(new GridBagLayout());
            personal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "Personal"));
            personal.setBackground(Color.WHITE);
            GridBagConstraints pgbc = new GridBagConstraints(); pgbc.insets = new Insets(6,6,6,6); pgbc.fill = GridBagConstraints.HORIZONTAL; pgbc.gridx=0; pgbc.gridy=0;
            JTextField surenameField = new JTextField(); JTextField nameField = new JTextField(); JTextField middleField = new JTextField();
            JTextField dobField = new JTextField(); JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male","Female","Other"});
            JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 150, 1)); JTextField nationalityField = new JTextField();
            BiConsumer<String, Component> addP = (lbl, comp) -> { pgbc.gridx=0; pgbc.weightx=0; personal.add(new JLabel(lbl), pgbc); pgbc.gridx=1; pgbc.weightx=1; personal.add(comp, pgbc); pgbc.gridy++; };
            addP.accept("Surname:", surenameField); addP.accept("Given Name:", nameField); addP.accept("Middle Name:", middleField); addP.accept("DOB (YYYY-MM-DD):", dobField);
            addP.accept("Sex/Gender:", genderBox); addP.accept("Age:", ageSpinner); addP.accept("Nationality:", nationalityField);

            // Contact block (left) - larger fields
            Dimension contactPref = new Dimension(420, 28);
            JPanel contact = new JPanel(new GridBagLayout());
            contact.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "Contact"));
            contact.setBackground(Color.WHITE);
            GridBagConstraints cgbc = new GridBagConstraints(); cgbc.insets = new Insets(6,6,6,6); cgbc.fill = GridBagConstraints.HORIZONTAL; cgbc.gridx=0; cgbc.gridy=0;
            JTextField emailField = new JTextField(); emailField.setPreferredSize(contactPref);
            JTextField contactField = new JTextField(); contactField.setPreferredSize(contactPref);
            JTextField addressField = new JTextField(); addressField.setPreferredSize(contactPref);
            JTextField emergencyNameField = new JTextField(); emergencyNameField.setPreferredSize(contactPref);
            JTextField emergencyContactField = new JTextField(); emergencyContactField.setPreferredSize(contactPref);
            BiConsumer<String, Component> addC = (lbl, comp) -> { cgbc.gridx=0; cgbc.weightx=0; contact.add(new JLabel(lbl), cgbc); cgbc.gridx=1; cgbc.weightx=1; contact.add(comp, cgbc); cgbc.gridy++; };
            addC.accept("Email:", emailField); addC.accept("Contact Number:", contactField); addC.accept("Address:", addressField);
            addC.accept("Emergency Contact Name:", emergencyNameField); addC.accept("Emergency Contact Number:", emergencyContactField);

            left.add(personal); left.add(Box.createVerticalStrut(8)); left.add(contact);

            // Professional block (right)
            JPanel prof = new JPanel(new GridBagLayout());
            prof.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "Professional"));
            prof.setBackground(Color.WHITE);
            GridBagConstraints rgbc = new GridBagConstraints(); rgbc.insets = new Insets(6,6,6,6); rgbc.fill = GridBagConstraints.HORIZONTAL; rgbc.gridx=0; rgbc.gridy=0;
            JTextField titleField = new JTextField(); JTextField specialityField = new JTextField(); JSpinner yearsField = new JSpinner(new SpinnerNumberModel(1, 0, 80, 1));
            JTextField licenseField = new JTextField(); JTextField prcExpiryField = new JTextField(); JTextField hospitalAffilField = new JTextField();
            BiConsumer<String, Component> addR = (lbl, comp) -> { rgbc.gridx=0; rgbc.weightx=0; prof.add(new JLabel(lbl), rgbc); rgbc.gridx=1; rgbc.weightx=1; prof.add(comp, rgbc); rgbc.gridy++; };
            addR.accept("Title/Position:", titleField); addR.accept("Speciality/Department:", specialityField); addR.accept("Years in Field:", yearsField);
            addR.accept("License / PRC ID:", licenseField); addR.accept("PRC Expiry (YYYY-MM-DD):", prcExpiryField); addR.accept("Hospital Affiliations:", hospitalAffilField);

            // Identification area with Minor checkbox and alternate Student ID field
            JPanel idBlock = new JPanel(new GridBagLayout()); idBlock.setBackground(Color.WHITE);
            idBlock.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "Identification (optional for minors)"));
            GridBagConstraints idgbc = new GridBagConstraints(); idgbc.insets = new Insets(6,6,6,6); idgbc.fill = GridBagConstraints.HORIZONTAL; idgbc.gridx=0; idgbc.gridy=0;
            JComboBox<String> idType = new JComboBox<>(new String[]{"National ID","PhilHealth","Driver's License","Passport","Other"});
            JTextField idNumber = new JTextField(); idNumber.setPreferredSize(contactPref);
            JCheckBox minorCheck = new JCheckBox("Minor (under 18) — allow Student ID instead (optional)");
            JTextField studentIdField = new JTextField(); studentIdField.setPreferredSize(contactPref); studentIdField.setEnabled(false);
            idgbc.gridx=0; idBlock.add(new JLabel("ID Type:"), idgbc); idgbc.gridx=1; idBlock.add(idType, idgbc); idgbc.gridy++;
            idgbc.gridx=0; idBlock.add(new JLabel("ID Number:"), idgbc); idgbc.gridx=1; idBlock.add(idNumber, idgbc); idgbc.gridy++;
            idgbc.gridx=0; idBlock.add(minorCheck, idgbc); idgbc.gridy++; idgbc.gridwidth=1;
            idgbc.gridx=0; idBlock.add(new JLabel("Student ID (if minor):"), idgbc); idgbc.gridx=1; idBlock.add(studentIdField, idgbc); idgbc.gridy++;
            minorCheck.addActionListener(e -> {
                boolean isMinor = minorCheck.isSelected();
                idType.setEnabled(!isMinor);
                idNumber.setEnabled(!isMinor);
                studentIdField.setEnabled(isMinor);
            });

            // System & docs (right)
            JPanel sysdocs = new JPanel(new GridBagLayout());
            sysdocs.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "System / Documents"));
            sysdocs.setBackground(Color.WHITE);
            GridBagConstraints sgbc2 = new GridBagConstraints(); sgbc2.insets = new Insets(6,6,6,6); sgbc2.fill = GridBagConstraints.HORIZONTAL; sgbc2.gridx=0; sgbc2.gridy=0;
            JTextField usernameField = new JTextField(); JPasswordField passwordField = new JPasswordField(); JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active","Inactive","Pending Approval","Suspended"});
            JTextField picField = new JTextField(); picField.setEditable(false); JButton picBtn = new JButton("Choose 2x2"); picBtn.addActionListener(e->{ JFileChooser fc=new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("Image files","jpg","jpeg","png")); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) picField.setText(fc.getSelectedFile().getAbsolutePath()); });
            JTextField idFrontField = new JTextField(); idFrontField.setEditable(false); JButton idFrontBtn = new JButton("ID Front"); idFrontBtn.addActionListener(e->{ JFileChooser fc=new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("Image/PDF","jpg","jpeg","png","pdf")); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) idFrontField.setText(fc.getSelectedFile().getAbsolutePath()); });
            JTextField idBackField = new JTextField(); idBackField.setEditable(false); JButton idBackBtn = new JButton("ID Back"); idBackBtn.addActionListener(e->{ JFileChooser fc=new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("Image/PDF","jpg","jpeg","png","pdf")); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) idBackField.setText(fc.getSelectedFile().getAbsolutePath()); });
            sgbc2.gridx=0; sgbc2.weightx=0; sysdocs.add(new JLabel("Username (opt):"), sgbc2); sgbc2.gridx=1; sgbc2.weightx=1; sysdocs.add(usernameField, sgbc2); sgbc2.gridy++;
            sgbc2.gridx=0; sgbc2.weightx=0; sysdocs.add(new JLabel("Password:"), sgbc2); sgbc2.gridx=1; sgbc2.weightx=1; sysdocs.add(passwordField, sgbc2); sgbc2.gridy++;
            sgbc2.gridx=0; sysdocs.add(new JLabel("Status:"), sgbc2); sgbc2.gridx=1; sysdocs.add(statusCombo, sgbc2); sgbc2.gridy++;
            sgbc2.gridx=0; sysdocs.add(new JLabel("2x2 Picture:"), sgbc2); sgbc2.gridx=1; sysdocs.add(picField, sgbc2); sgbc2.gridx=2; sysdocs.add(picBtn, sgbc2); sgbc2.gridy++;
            sgbc2.gridx=0; sysdocs.add(new JLabel("ID Front:"), sgbc2); sgbc2.gridx=1; sysdocs.add(idFrontField, sgbc2); sgbc2.gridx=2; sysdocs.add(idFrontBtn, sgbc2); sgbc2.gridy++;
            sgbc2.gridx=0; sysdocs.add(new JLabel("ID Back:"), sgbc2); sgbc2.gridx=1; sysdocs.add(idBackField, sgbc2); sgbc2.gridx=2; sysdocs.add(idBackBtn, sgbc2); sgbc2.gridy++;

            // assemble right side
            right.add(prof); right.add(Box.createVerticalStrut(8)); right.add(idBlock); right.add(Box.createVerticalStrut(8)); right.add(sysdocs);

            container.add(left, BorderLayout.WEST); container.add(right, BorderLayout.CENTER);

            // Show in a resizable dialog so the form has space
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New " + (chosen==Role.DOCTOR?"Doctor":"Staff"), Dialog.ModalityType.APPLICATION_MODAL);
            dlg.getContentPane().setLayout(new BorderLayout());
            dlg.getContentPane().add(new JScrollPane(container), BorderLayout.CENTER);
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton okB = new JButton("Save"); JButton cancelB = new JButton("Cancel");
            foot.add(cancelB); foot.add(okB);
            dlg.getContentPane().add(foot, BorderLayout.SOUTH);
            dlg.setSize(1000,700); dlg.setLocationRelativeTo(this);

            // Perform validation and creation inside the OK button listener so fields are in-scope
            okB.addActionListener(e -> {
                // Basic validation and user creation (same logic as before)
                String surename = surenameField.getText().trim();
                String given = nameField.getText().trim();
                String uname = usernameField.getText().trim();
                char[] pw = passwordField.getPassword();
                if (given.isEmpty() || surename.isEmpty() || (uname.isEmpty() && pw.length==0)) {
                    JOptionPane.showMessageDialog(dlg, "Please provide at minimum a surname, name and a password (or specify username).", "Validation", JOptionPane.WARNING_MESSAGE);
                    return; // do not close dialog
                }
                if (uname.isEmpty()) {
                    String base = (surename + given).toLowerCase().replaceAll("[^a-z0-9]+", "");
                    String candidate = base; int attempt = 0; while (userService.findByUsername(candidate).isPresent()) { attempt++; candidate = base + attempt; if (attempt>1000) break; }
                    uname = candidate;
                }
                try {
                    userService.createUser(uname, pw, chosen);
                    Arrays.fill(pw, '\0');
                    reloadUsersTable();
                    JOptionPane.showMessageDialog(dlg, "User for " + given + " " + surename + " created (username: " + uname + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            cancelB.addActionListener(e -> dlg.dispose());
            dlg.setVisible(true);
            return;
        }

        if (chosen == Role.PATIENT) {
            // Two-column patient form: left = personal & contact, right = ID, medical, insurance, system
            JPanel container = new JPanel(new BorderLayout(12,12));
            container.setBorder(new EmptyBorder(12,12,12,12));
            container.setBackground(Color.WHITE);

            JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setBackground(Color.WHITE);
            JPanel right = new JPanel(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS)); right.setBackground(Color.WHITE);

            // Personal section (left)
            JPanel personal = new JPanel(new GridBagLayout());
            personal.setBackground(Color.WHITE);
            personal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "1. Personal Information"));
            GridBagConstraints pgbc2 = new GridBagConstraints(); pgbc2.insets = new Insets(6,6,6,6); pgbc2.fill = GridBagConstraints.HORIZONTAL; pgbc2.gridx=0; pgbc2.gridy=0;
            JTextField pSurname = new JTextField(); JTextField pFirst = new JTextField(); JTextField pMiddle = new JTextField();
            JSpinner pAge = new JSpinner(new SpinnerNumberModel(30, 0, 150, 1)); JTextField pDob = new JTextField(); JComboBox<String> pGender = new JComboBox<>(new String[]{"Male","Female","Other"});
            JTextField pNationality = new JTextField(); JComboBox<String> pCivil = new JComboBox<>(new String[]{"Single","Married","Separated","Widowed"});
            BiConsumer<String, Component> addP2 = (lbl, comp) -> { pgbc2.gridx=0; pgbc2.weightx=0; personal.add(new JLabel(lbl), pgbc2); pgbc2.gridx=1; pgbc2.weightx=1; personal.add(comp, pgbc2); pgbc2.gridy++; };
            addP2.accept("Surname:", pSurname); addP2.accept("First Name:", pFirst); addP2.accept("Middle Name (optional):", pMiddle); addP2.accept("Age:", pAge);
            addP2.accept("Date of Birth (YYYY-MM-DD):", pDob); addP2.accept("Sex / Gender:", pGender); addP2.accept("Nationality:", pNationality); addP2.accept("Civil Status:", pCivil);

            // Contact section (left) - larger fields
            Dimension contactPref = new Dimension(420, 28);
            JPanel contact = new JPanel(new GridBagLayout()); contact.setBackground(Color.WHITE);
            contact.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "2. Contact Information"));
            GridBagConstraints cgbc2 = new GridBagConstraints(); cgbc2.insets = new Insets(6,6,6,6); cgbc2.fill = GridBagConstraints.HORIZONTAL; cgbc2.gridx=0; cgbc2.gridy=0;
            JTextField cPhone = new JTextField(); cPhone.setPreferredSize(contactPref); JTextField cEmail = new JTextField(); cEmail.setPreferredSize(contactPref); JTextField cAddress = new JTextField(); cAddress.setPreferredSize(contactPref);
            JTextField cEmergencyName = new JTextField(); cEmergencyName.setPreferredSize(contactPref); JTextField cEmergencyNumber = new JTextField(); cEmergencyNumber.setPreferredSize(contactPref); JTextField cEmergencyRelation = new JTextField(); cEmergencyRelation.setPreferredSize(contactPref);
            BiConsumer<String, Component> addC2 = (lbl, comp) -> { cgbc2.gridx=0; cgbc2.weightx=0; contact.add(new JLabel(lbl), cgbc2); cgbc2.gridx=1; cgbc2.weightx=1; contact.add(comp, cgbc2); cgbc2.gridy++; };
            addC2.accept("Phone Number:", cPhone); addC2.accept("Email Address:", cEmail); addC2.accept("Home Address:", cAddress);
            addC2.accept("Emergency Contact Person:", cEmergencyName); addC2.accept("Emergency Contact Number:", cEmergencyNumber); addC2.accept("Relationship to Patient:", cEmergencyRelation);

            left.add(personal); left.add(Box.createVerticalStrut(8)); left.add(contact);

            // Identification (right) with Minor checkbox and Student ID option
            JPanel idpanel = new JPanel(new GridBagLayout()); idpanel.setBackground(Color.WHITE);
            idpanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "3. Identification"));
            GridBagConstraints igbc = new GridBagConstraints(); igbc.insets = new Insets(6,6,6,6); igbc.fill = GridBagConstraints.HORIZONTAL; igbc.gridx=0; igbc.gridy=0;
            JComboBox<String> idType = new JComboBox<>(new String[]{"National ID","PhilHealth","Driver's License","Passport","Student ID","Other"});
            JTextField idNumber = new JTextField(); idNumber.setPreferredSize(contactPref);
            JCheckBox minorCheckP = new JCheckBox("Minor (under 18) — identification optional; Student ID may be used");
            JTextField studentIdFieldP = new JTextField(); studentIdFieldP.setPreferredSize(contactPref); studentIdFieldP.setEnabled(false);
            igbc.gridx=0; idpanel.add(new JLabel("Government ID Type:"), igbc); igbc.gridx=1; idpanel.add(idType, igbc); igbc.gridy++;
            igbc.gridx=0; idpanel.add(new JLabel("ID Number:"), igbc); igbc.gridx=1; idpanel.add(idNumber, igbc); igbc.gridy++;

            // File pickers for patient identification
            JTextField idFront = new JTextField(); idFront.setEditable(false);
            JButton chooseIdFront = new JButton("Choose ID Front");
            chooseIdFront.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Image / PDF", "jpg","jpeg","png","pdf"));
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) idFront.setText(fc.getSelectedFile().getAbsolutePath());
            });

            JTextField idBack = new JTextField(); idBack.setEditable(false);
            JButton chooseIdBack = new JButton("Choose ID Back");
            chooseIdBack.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Image / PDF", "jpg","jpeg","png","pdf"));
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) idBack.setText(fc.getSelectedFile().getAbsolutePath());
            });

            JTextField twoByTwo = new JTextField(); twoByTwo.setEditable(false);
            JButton choose2x2 = new JButton("Choose 2x2 (optional)");
            choose2x2.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Image files", "jpg","jpeg","png"));
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) twoByTwo.setText(fc.getSelectedFile().getAbsolutePath());
            });

            igbc.gridx=0; idpanel.add(new JLabel("Picture of ID (front):"), igbc); igbc.gridx=1; idpanel.add(idFront, igbc); igbc.gridx=2; idpanel.add(chooseIdFront, igbc); igbc.gridx=1; igbc.gridy++;
            igbc.gridx=0; idpanel.add(new JLabel("Picture of ID (back):"), igbc); igbc.gridx=1; idpanel.add(idBack, igbc); igbc.gridx=2; idpanel.add(chooseIdBack, igbc); igbc.gridx=1; igbc.gridy++;
            igbc.gridx=0; idpanel.add(new JLabel("2×2 Picture (optional):"), igbc); igbc.gridx=1; idpanel.add(twoByTwo, igbc); igbc.gridx=2; idpanel.add(choose2x2, igbc); igbc.gridx=1; igbc.gridy++;

            igbc.gridx=0; idpanel.add(minorCheckP, igbc); igbc.gridy++;
            igbc.gridx=0; idpanel.add(new JLabel("Student ID (if applicable):"), igbc); igbc.gridx=1; idpanel.add(studentIdFieldP, igbc); igbc.gridy++;
            minorCheckP.addActionListener(e -> {
                boolean isMinor = minorCheckP.isSelected();
                // when minor, allow student ID and keep national ID optional
                idType.setEnabled(!isMinor); idNumber.setEnabled(!isMinor); studentIdFieldP.setEnabled(isMinor);
            });

            // Medical (right)
            JPanel medical = new JPanel(new GridBagLayout()); medical.setBackground(Color.WHITE);
            medical.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "4. Medical Information"));
            GridBagConstraints mgbc = new GridBagConstraints(); mgbc.insets = new Insets(6,6,6,6); mgbc.fill = GridBagConstraints.HORIZONTAL; mgbc.gridx=0; mgbc.gridy=0;
            JComboBox<String> bloodType = new JComboBox<>(new String[]{"A+","A-","B+","B-","AB+","AB-","O+","O-","Unknown"}); JTextArea allergies = new JTextArea(3,30); JTextArea currentMeds = new JTextArea(3,30);
            JTextArea existingConds = new JTextArea(3,30); JTextArea pastSurg = new JTextArea(3,30); JTextArea familyHx = new JTextArea(3,30); JTextArea immunization = new JTextArea(3,30); JTextField primaryCare = new JTextField();
            BiConsumer<String, Component> addMed2 = (lbl, comp) -> { mgbc.gridx=0; mgbc.weightx=0; medical.add(new JLabel(lbl), mgbc); mgbc.gridx=1; mgbc.weightx=1; medical.add(comp, mgbc); mgbc.gridy++; };
            addMed2.accept("Blood Type:", bloodType); addMed2.accept("Allergies:", new JScrollPane(allergies)); addMed2.accept("Current Medications:", new JScrollPane(currentMeds));
            addMed2.accept("Existing Medical Conditions:", new JScrollPane(existingConds)); addMed2.accept("Past Surgeries / Hospitalizations:", new JScrollPane(pastSurg)); addMed2.accept("Family Medical History:", new JScrollPane(familyHx));
            addMed2.accept("Immunization History (optional):", new JScrollPane(immunization)); addMed2.accept("Primary Care Physician (if any):", primaryCare);

            // Insurance & system (right)
            JPanel insurance = new JPanel(new GridBagLayout()); insurance.setBackground(Color.WHITE);
            insurance.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "5. Insurance & System"));
            GridBagConstraints igb2 = new GridBagConstraints(); igb2.insets = new Insets(6,6,6,6); igb2.fill = GridBagConstraints.HORIZONTAL; igb2.gridx=0; igb2.gridy=0;
            JTextField insProvider = new JTextField(); JTextField insNumber = new JTextField(); JTextField insExpiry = new JTextField(); JTextField philHealth = new JTextField();
            JTextField assignedDoctor = new JTextField(); JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active","Inactive","Deceased"}); JTextField dateRegistered = new JTextField(java.time.LocalDate.now().toString()); dateRegistered.setEditable(false);
            JTextField occupation = new JTextField(); JTextField employer = new JTextField(); JTextField workAddr = new JTextField(); JTextField religion = new JTextField(); JTextField prefLang = new JTextField(); JComboBox<String> prefContact = new JComboBox<>(new String[]{"Text","Call","Email"});
            BiConsumer<String, Component> addIns2 = (lbl, comp) -> { igb2.gridx=0; igb2.weightx=0; insurance.add(new JLabel(lbl), igb2); igb2.gridx=1; igb2.weightx=1; insurance.add(comp, igb2); igb2.gridy++; };
            addIns2.accept("Insurance Provider:", insProvider); addIns2.accept("Insurance Number / Policy Number:", insNumber); addIns2.accept("Insurance Expiration Date:", insExpiry); addIns2.accept("PhilHealth Number:", philHealth);
            addIns2.accept("Patient ID / Record Number:", dateRegistered); addIns2.accept("Status:", statusCombo); addIns2.accept("Assigned Doctor:", assignedDoctor);
            addIns2.accept("Occupation:", occupation); addIns2.accept("Employer Name:", employer); addIns2.accept("Work Address:", workAddr); addIns2.accept("Religion:", religion); addIns2.accept("Preferred Language:", prefLang); addIns2.accept("Preferred Communication Method:", prefContact);

            right.add(idpanel); right.add(Box.createVerticalStrut(8)); right.add(medical); right.add(Box.createVerticalStrut(8)); right.add(insurance);

            container.add(left, BorderLayout.WEST); container.add(right, BorderLayout.CENTER);

            // Show in a resizable dialog so the form has space
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New Patient", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.getContentPane().setLayout(new BorderLayout());
            dlg.getContentPane().add(new JScrollPane(container), BorderLayout.CENTER);
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton okB = new JButton("Save"); JButton cancelB = new JButton("Cancel");
            foot.add(cancelB); foot.add(okB);
            dlg.getContentPane().add(foot, BorderLayout.SOUTH);
            dlg.setSize(1100,760); dlg.setLocationRelativeTo(this);

            // OK button handles collecting fields, validation, creating patient and saving profile
            okB.addActionListener(e -> {
                String surnameVal = pSurname.getText().trim();
                String firstVal = pFirst.getText().trim();
                String middleVal = pMiddle.getText().trim();
                java.time.LocalDate dobVal = null; try { if (!pDob.getText().trim().isEmpty()) dobVal = java.time.LocalDate.parse(pDob.getText().trim()); } catch (Exception ex) {}
                String genderVal = (String) pGender.getSelectedItem();
                String nationalityVal = pNationality.getText().trim();
                String civilVal = (String) pCivil.getSelectedItem();
                String phoneVal = cPhone.getText().trim();
                String emailVal = cEmail.getText().trim();
                String addressVal = cAddress.getText().trim();
                String emergencyNameVal = cEmergencyName.getText().trim();
                String emergencyNumberVal = cEmergencyNumber.getText().trim();
                String emergencyRelationVal = cEmergencyRelation.getText().trim();

                if (surnameVal.isEmpty() || firstVal.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Please enter at least the patient's surname and first name.", "Validation", JOptionPane.WARNING_MESSAGE); return; }

                PatientService ps = PatientService.getInstance();
                try {
                    Model.Patient np = ps.createPatient(firstVal, surnameVal, dobVal, genderVal, phoneVal, emailVal, addressVal);
                    PatientService.PatientProfile prof = new PatientService.PatientProfile();
                    prof.surname = surnameVal; prof.firstName = firstVal; prof.middleName = middleVal;
                    prof.dateOfBirth = (dobVal==null?"":dobVal.toString()); prof.gender = genderVal; prof.nationality = nationalityVal; prof.civilStatus = civilVal;
                    prof.age = String.valueOf(((Number)pAge.getValue()).intValue());

                    prof.phone = phoneVal; prof.email = emailVal; prof.address = addressVal;
                    prof.emergencyContactName = emergencyNameVal; prof.emergencyContactNumber = emergencyNumberVal; prof.emergencyContactRelationship = emergencyRelationVal;

                    prof.idType = (String) idType.getSelectedItem(); prof.idNumber = idNumber.getText().trim(); prof.idFrontPath = idFront.getText().trim(); prof.idBackPath = idBack.getText().trim(); prof.twoByTwoPath = twoByTwo.getText().trim();

                    prof.bloodType = (String) bloodType.getSelectedItem(); prof.allergies = allergies.getText().trim(); prof.currentMedications = currentMeds.getText().trim(); prof.existingConditions = existingConds.getText().trim();
                    prof.pastSurgeries = pastSurg.getText().trim(); prof.familyMedicalHistory = familyHx.getText().trim(); prof.immunizationHistory = immunization.getText().trim(); prof.primaryCarePhysician = primaryCare.getText().trim();

                    prof.insuranceProvider = insProvider.getText().trim(); prof.insuranceNumber = insNumber.getText().trim(); prof.insuranceExpiry = insExpiry.getText().trim(); prof.philHealthNumber = philHealth.getText().trim();

                    prof.dateRegistered = java.time.LocalDate.now().toString(); prof.patientRecordId = np.getId(); prof.status = (String) statusCombo.getSelectedItem(); prof.assignedDoctor = assignedDoctor.getText().trim();

                    prof.occupation = occupation.getText().trim(); prof.employerName = employer.getText().trim(); prof.workAddress = workAddr.getText().trim(); prof.religion = religion.getText().trim();
                    prof.preferredLanguage = prefLang.getText().trim(); prof.preferredContactMethod = (String) prefContact.getSelectedItem();

                    ps.getProvisionedAccountForPatient(np.getId()).ifPresentOrElse(pa -> { ps.saveProfile(pa.username, prof); reloadUsersTable(); JOptionPane.showMessageDialog(dlg, "Patient created with ID: " + np.getId() + " (username: " + pa.username + ")", "Success", JOptionPane.INFORMATION_MESSAGE); dlg.dispose(); }, () -> { ps.saveProfile(np.getId(), prof); reloadUsersTable(); JOptionPane.showMessageDialog(dlg, "Patient created with ID: " + np.getId() + " (no user account provisioned).", "Created", JOptionPane.INFORMATION_MESSAGE); dlg.dispose(); });
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Failed to create patient: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            cancelB.addActionListener(e -> dlg.dispose());
            dlg.setVisible(true);
            return;
        }

        // Fallback: simple user form for ADMIN/USER/other
        JPanel panel = new JPanel(new GridLayout(3,2,8,8));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        panel.add(new JLabel("Username:")); JTextField usernameField = new JTextField(); panel.add(usernameField);
        panel.add(new JLabel("Role:")); JComboBox<Role> roleCombo = new JComboBox<>(Role.values()); roleCombo.setSelectedItem(chosen); panel.add(roleCombo);
        panel.add(new JLabel("Password:")); JPasswordField passField = new JPasswordField(); panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            char[] pw = passField.getPassword();
            Role rsel = (Role) roleCombo.getSelectedItem();
            if (username.isEmpty() || pw.length == 0) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                userService.createUser(username, pw, rsel);
                reloadUsersTable();
                JOptionPane.showMessageDialog(this, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEditUserDialog() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = userTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        String username = (String) model.getValueAt(row, 0);
        Role currentRole = Role.valueOf(((String) model.getValueAt(row, 1)).toUpperCase());

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(username);
        usernameField.setEditable(false);
        panel.add(usernameField);

        panel.add(new JLabel("Role:"));
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        roleCombo.setSelectedItem(currentRole);
        // Prevent changing ADMIN role
        if (currentRole == Role.ADMIN) {
            roleCombo.setEnabled(false);
            roleCombo.setToolTipText("Admin role cannot be changed");
        }
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User Role", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Role newRole = (Role) roleCombo.getSelectedItem();
            if (newRole != null && newRole != currentRole) {
                userService.findByUsername(username).ifPresentOrElse(u -> {
                    // Prevent downgrading/removing admin
                    if (u.getRole() == Role.ADMIN) {
                        JOptionPane.showMessageDialog(this, "Cannot change the role of an ADMIN user.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    boolean ok = userService.updateRoleById(u.getId(), newRole);
                    if (ok) {
                        model.setValueAt(newRole.name(), row, 1);
                        JOptionPane.showMessageDialog(this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update user!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }, () -> JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE));
            }
        }
    }

    private void openDeleteUserDialog() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = userTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        String username = (String) model.getValueAt(row, 0);

        // Prevent deleting ADMIN
        userService.findByUsername(username).ifPresent(u -> {
            if (u.getRole() == Role.ADMIN) {
                JOptionPane.showMessageDialog(this, "The ADMIN user cannot be deleted.", "Action Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + username + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = userService.deleteById(u.getId());
                if (ok) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete user!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void openExportDialog() {
        String[] options = {"CSV", "Excel", "PDF"};
        int choice = JOptionPane.showOptionDialog(this,
            "Choose export format:",
            "Export Users",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice >= 0) {
            String format = options[choice];
            JOptionPane.showMessageDialog(this, 
                "Users exported successfully as " + format + "!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openExportPaymentDialog() {
        String[] options = {"CSV", "Excel", "PDF"};
        int choice = JOptionPane.showOptionDialog(this,
            "Choose export format for payment history:",
            "Export Payments",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice >= 0) {
            String format = options[choice];
            JOptionPane.showMessageDialog(this, 
                "Payment history exported successfully as " + format + "!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openGenerateSummaryDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Report Type:"));
        String[] reportTypes = {"Daily", "Weekly", "Monthly", "Yearly"};
        JComboBox<String> typeCombo = new JComboBox<>(reportTypes);
        panel.add(typeCombo);

        panel.add(new JLabel("Start Date:"));
        JTextField startDate = new JTextField("2025-01-01");
        panel.add(startDate);

        panel.add(new JLabel("End Date:"));
        JTextField endDate = new JTextField("2025-01-31");
        panel.add(endDate);

        int result = JOptionPane.showConfirmDialog(this, panel, "Generate Summary Report", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String reportType = (String) typeCombo.getSelectedItem();
            JOptionPane.showMessageDialog(this, 
                "Generating " + reportType + " report...\nReport will be ready shortly!", 
                "Report Generation", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void filterUserTable(String query) {
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) userTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(userTable.getModel());
            userTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 0, 1));
        }
    }

    private void filterPaymentTable(String query) {
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) paymentTable.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(paymentTable.getModel());
            paymentTable.setRowSorter(sorter);
        }
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2, 3));
        }
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

    private void openResetPasswordDialog() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to reset password!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = userTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        String username = (String) model.getValueAt(row, 0);

        userService.findByUsername(username).ifPresentOrElse(u -> {
            if (u.getRole() == Role.ADMIN) {
                // Protect ADMIN account from accidental resets here (business rule)
                JOptionPane.showMessageDialog(this, "Password for ADMIN cannot be reset via this UI.", "Action Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JPanel p = new JPanel(new GridLayout(2,2,8,8));
            p.add(new JLabel("New Password:")); JPasswordField pw1 = new JPasswordField(); p.add(pw1);
            p.add(new JLabel("Confirm Password:")); JPasswordField pw2 = new JPasswordField(); p.add(pw2);
            int res = JOptionPane.showConfirmDialog(this, p, "Reset Password for " + username, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            char[] a = pw1.getPassword(); char[] b = pw2.getPassword();
            if (a.length == 0) { JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            if (!java.util.Arrays.equals(a, b)) { JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            try {
                boolean ok = userService.resetPasswordById(u.getId(), a);
                // resetPasswordById clears the password char[] itself, but clear again for safety
                Arrays.fill(a, '\0'); Arrays.fill(b, '\0');
                if (ok) JOptionPane.showMessageDialog(this, "Password reset successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                else JOptionPane.showMessageDialog(this, "Failed to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            }
        }, () -> JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE));
    }
}
