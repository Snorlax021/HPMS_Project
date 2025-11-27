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

    public AdminDashboardPanel() {
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

        JLabel title = new JLabel("Admin Dashboard", SwingConstants.LEFT);
        title.setFont(FONT_TITLE);
        title.setBorder(new EmptyBorder(0, 16, 0, 0));
        title.setForeground(COLOR_PRIMARY.darker());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh Data");
        styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> JOptionPane.showMessageDialog(this, "Data refreshed (placeholder)", "Info", JOptionPane.INFORMATION_MESSAGE));
        right.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
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

        sideNavPanel.add(Box.createVerticalStrut(6));
        sideNavPanel.add(btnDashboard);
        sideNavPanel.add(btnUsers);
        sideNavPanel.add(btnPayments);
        sideNavPanel.add(btnSummary);
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
        styleToolbarButton(toolbar, "Delete", this::openDeleteUserDialog);
        styleToolbarButton(toolbar, "Export", this::openExportDialog);
        root.add(toolbar, BorderLayout.SOUTH);

        String[] cols = {"ID", "Name", "Role", "Status"};
        Object[][] data = {{1, "John Doe", "Doctor", "Active"}, {2, "Jane Smith", "Staff", "Active"}, {3, "Robert Admin", "Admin", "Disabled"}};
        userTable = new JTable(new DefaultTableModel(data, cols));

        // Add search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterUserTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { filterUserTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterUserTable(searchField.getText()); }
        });

        root.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return root;
    }

    private void styleToolbarButton(JToolBar bar, String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(FONT_NORMAL);
        b.addActionListener(e -> action.run());
        bar.add(b);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Action", JOptionPane.INFORMATION_MESSAGE);
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
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Role:"));
        String[] roles = {"Doctor", "Staff", "Admin", "Patient"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        panel.add(roleCombo);

        panel.add(new JLabel("Status:"));
        String[] statuses = {"Active", "Disabled"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        panel.add(statusCombo);

        panel.add(new JLabel("Password:"));
        JPasswordField passField = new JPasswordField();
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String email = emailField.getText();
            String role = (String) roleCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();
            
            if (!name.isEmpty() && !email.isEmpty()) {
                DefaultTableModel model = (DefaultTableModel) userTable.getModel();
                int newId = model.getRowCount() + 1;
                model.addRow(new Object[]{newId, name, role, status});
                JOptionPane.showMessageDialog(this, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void openEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(model.getValueAt(selectedRow, 1).toString());
        panel.add(nameField);

        panel.add(new JLabel("Role:"));
        String[] roles = {"Doctor", "Staff", "Admin", "Patient"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setSelectedItem(model.getValueAt(selectedRow, 2));
        panel.add(roleCombo);

        panel.add(new JLabel("Status:"));
        String[] statuses = {"Active", "Disabled"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(model.getValueAt(selectedRow, 3));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            model.setValueAt(nameField.getText(), selectedRow, 1);
            model.setValueAt(roleCombo.getSelectedItem(), selectedRow, 2);
            model.setValueAt(statusCombo.getSelectedItem(), selectedRow, 3);
            JOptionPane.showMessageDialog(this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openDeleteUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this user?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) userTable.getModel();
            model.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2, 3));
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
}
