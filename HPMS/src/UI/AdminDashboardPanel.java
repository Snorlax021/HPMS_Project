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

public class AdminDashboardPanel extends JPanel implements GlobalSearchable {
    private static final long serialVersionUID = 1L;

    // THEME CONSTANTS
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

    public AdminDashboardPanel() { this(null); }
    public AdminDashboardPanel(String username) {
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
        sideNavPanel.setPreferredSize(new Dimension(190, 0));

    //Dito magaadd ng buttons para sa side bar
        btnDashboard = createNavButton("Dashboard", "DASHBOARD");
        btnUsers = createNavButton("User Management", "USERS");
        btnPayments = createNavButton("Payments", "PAYMENTS");
        btnSummary = createNavButton("Summary", "SUMMARY");
        JButton btnGuide = createNavButton("User Guide", "GUIDE");

        sideNavPanel.add(Box.createVerticalStrut(6));
        sideNavPanel.add(btnDashboard);
        sideNavPanel.add(btnUsers);
        sideNavPanel.add(btnPayments);
        sideNavPanel.add(btnSummary);
        sideNavPanel.add(btnGuide);
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
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Role:"));
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        panel.add(roleCombo);

        panel.add(new JLabel("Password:"));
        JPasswordField passField = new JPasswordField();
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            char[] pw = passField.getPassword();
            if (username.isEmpty() || pw.length == 0) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                userService.createUser(username, pw, (Role) roleCombo.getSelectedItem());
                // password cleared inside service
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
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User Role", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Role newRole = (Role) roleCombo.getSelectedItem();
            if (newRole != null && newRole != currentRole) {
                // Use username to find user id then update
                userService.findByUsername(username).ifPresentOrElse(u -> {
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

    private void openResetPasswordDialog() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to reset password!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = userTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        String username = (String) model.getValueAt(row, 0);

        JPanel panel = new JPanel(new GridLayout(3, 3, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Username (read-only)
        panel.add(new JLabel("User:"));
        JTextField userField = new JTextField(username);
        userField.setEditable(false);
        panel.add(userField);
        panel.add(new JLabel(""));

        // Old password
        panel.add(new JLabel("Old Password:"));
        JPasswordField oldPassField = new JPasswordField();
        panel.add(oldPassField);
        JButton toggleOld = new JButton("Show");
        toggleOld.addActionListener(e -> {
            oldPassField.setEchoChar(oldPassField.getEchoChar() == 0 ? (char) 8226 : (char) 0);
            toggleOld.setText(oldPassField.getEchoChar() == 0 ? "Show" : "Hide");
        });
        panel.add(toggleOld);

        // New password
        panel.add(new JLabel("New Password:"));
        JPasswordField newPassField = new JPasswordField();
        panel.add(newPassField);
        JButton toggleNew = new JButton("Show");
        toggleNew.addActionListener(e -> {
            newPassField.setEchoChar(newPassField.getEchoChar() == 0 ? (char) 8226 : (char) 0);
            toggleNew.setText(newPassField.getEchoChar() == 0 ? "Show" : "Hide");
        });
        panel.add(toggleNew);

        int result = JOptionPane.showConfirmDialog(this, panel, "Reset Password (Confirm Old First)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            char[] oldPw = oldPassField.getPassword();
            char[] newPw = newPassField.getPassword();
            if (oldPw == null || oldPw.length == 0 || newPw == null || newPw.length == 0) {
                JOptionPane.showMessageDialog(this, "Please enter both old and new passwords.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                boolean ok = userService.changePassword(username, oldPw, newPw);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Old password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete '" + username + "'?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            userService.findByUsername(username).ifPresentOrElse(u -> {
                boolean ok = userService.deleteById(u.getId());
                if (ok) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete user!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }, () -> JOptionPane.showMessageDialog(this, "User not found!", "Error", JOptionPane.ERROR_MESSAGE));
        }
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
}
