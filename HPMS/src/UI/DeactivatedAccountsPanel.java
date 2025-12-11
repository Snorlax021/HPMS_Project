package UI;

import Service.UserService;
import Model.User;
import Model.Role;
import Model.UserStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

public class DeactivatedAccountsPanel extends JPanel {
    private final UserService userService = UserService.getInstance();
    private JTable doctorsTable;
    private JTable staffTable;
    private JTable patientsTable;
    private JTabbedPane tabs;
    private final Runnable onReactivate; // callback to notify parent UI to refresh

    // Provide a default constructor for existing callers
    public DeactivatedAccountsPanel() { this(null); }

    // New constructor accepts an optional callback that will be invoked after a successful reactivation
    public DeactivatedAccountsPanel(Runnable onReactivate) {
        this.onReactivate = onReactivate;
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JComponent createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel h = new JLabel("Deactivated Accounts", SwingConstants.LEFT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(h, BorderLayout.WEST);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> reloadAll());
        JButton btnReactivate = new JButton("Reactivate");
        btnReactivate.setBackground(new Color(60,120,200)); btnReactivate.setForeground(Color.WHITE);
        btnReactivate.addActionListener(e -> { if (tabs!=null) reactivateSelected(tabs.getSelectedIndex()); else JOptionPane.showMessageDialog(this, "View not ready."); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,6)); right.setOpaque(false); right.add(btnRefresh); right.add(btnReactivate);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent createBody() {
        this.tabs = new JTabbedPane();
        doctorsTable = buildTable(new String[]{"Username","ID","Created At"});
        staffTable = buildTable(new String[]{"Username","ID","Created At"});
        patientsTable = buildTable(new String[]{"Username","ID","Created At"});

        tabs.addTab("Doctors", new JScrollPane(doctorsTable));
        tabs.addTab("Staff", new JScrollPane(staffTable));
        tabs.addTab("Patients", new JScrollPane(patientsTable));

        JPanel container = new JPanel(new BorderLayout());
        container.add(tabs, BorderLayout.CENTER);
        reloadAll();
        return container;
    }

    private JTable buildTable(String[] cols) {
        DefaultTableModel m = new DefaultTableModel(cols,0) { @Override public boolean isCellEditable(int r,int c){ return false; } };
        JTable t = new JTable(m);
        t.setRowHeight(28);
        return t;
    }

    private void reloadAll() {
        List<User> all = userService.getAllUsers();
        java.util.List<User> deact = new java.util.ArrayList<>();
        for (User u : all) if (u.getStatus() == UserStatus.INACTIVE) deact.add(u);
        loadIntoTable(doctorsTable, u -> u.getRole()==Role.DOCTOR, deact);
        loadIntoTable(staffTable, u -> u.getRole()==Role.STAFF, deact);
        loadIntoTable(patientsTable, u -> u.getRole()==Role.PATIENT, deact);
    }

    private void loadIntoTable(JTable table, Predicate<User> filter, List<User> data) {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        for (User u : data) {
            if (!filter.test(u)) continue;
            m.addRow(new Object[]{u.getUsername(), u.getId(), u.getCreatedAt().toString()});
        }
    }

    private void reactivateSelected(int tabIndex) {
        JTable t;
        switch(tabIndex) { case 0: t = doctorsTable; break; case 1: t = staffTable; break; default: t = patientsTable; }
        int view = t.getSelectedRow();
        if (view == -1) { JOptionPane.showMessageDialog(this, "Select a row first.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int row = t.convertRowIndexToModel(view);
        DefaultTableModel m = (DefaultTableModel) t.getModel();
        String id = (String) m.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Reactivate selected account?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        // Use the service API to perform activation so any centralized logic is applied.
        boolean ok = userService.activateById(id);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "User not found or failed to reactivate.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Notify parent UI (if provided) so lists can refresh and the user reappears in the Users view.
        if (onReactivate != null) {
            try { onReactivate.run(); } catch (Exception ignored) {}
        }
        // Refresh this panel's view (user should disappear from deactivated list)
        reloadAll();
        JOptionPane.showMessageDialog(this, "Account reactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}