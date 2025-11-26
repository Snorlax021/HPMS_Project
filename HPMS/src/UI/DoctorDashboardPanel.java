package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoctorDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // THEME CONSTANTS (match AdminDashboardPanel)
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

    public DoctorDashboardPanel() {
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new BorderLayout(8, 8));

        add(createHeader(), BorderLayout.NORTH);
        add(createSideBar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        // Default view
        setActiveButton(btnDashboard, "DASHBOARD");
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new LineBorder(COLOR_BORDER));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 55));

        JLabel title = new JLabel("Doctor Dashboard", SwingConstants.LEFT);
        title.setFont(FONT_TITLE);
        title.setBorder(new EmptyBorder(0, 16, 0, 0));
        title.setForeground(COLOR_PRIMARY.darker());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh");
        styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> JOptionPane.showMessageDialog(this, "Data refreshed (placeholder)", "Info", JOptionPane.INFORMATION_MESSAGE));
        right.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent createSideBar() {
        sideNavPanel = new JPanel();
        sideNavPanel.setLayout(new BoxLayout(sideNavPanel, BoxLayout.Y_AXIS));
        sideNavPanel.setBackground(COLOR_SIDEBAR_BG);
        sideNavPanel.setBorder(new LineBorder(COLOR_BORDER));
        sideNavPanel.setPreferredSize(new Dimension(190, 0));

        btnDashboard = createNavButton("Dashboard", "DASHBOARD");
        btnPatients = createNavButton("Patients", "PATIENTS");
        btnReports = createNavButton("Reports", "REPORTS");
        btnSummary = createNavButton("Summary", "SUMMARY");

        sideNavPanel.add(Box.createVerticalStrut(6));
        sideNavPanel.add(btnDashboard);
        sideNavPanel.add(btnPatients);
        sideNavPanel.add(btnReports);
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

    private JComponent createMainContent() {
        mainContentPanel = new JPanel();
        cardLayout = new CardLayout();
        mainContentPanel.setLayout(cardLayout);
        mainContentPanel.setBorder(new LineBorder(COLOR_BORDER));

        mainContentPanel.add(buildDashboardPanel(), "DASHBOARD");
        mainContentPanel.add(buildPatientsPanel(), "PATIENTS");
        mainContentPanel.add(buildReportsPanel(), "REPORTS");
        mainContentPanel.add(buildSummaryPanel(), "SUMMARY");
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

        JLabel header = new JLabel("Patient Management", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        styleToolbarButton(toolbar, "Add", this::openAddPatientDialog);
        styleToolbarButton(toolbar, "View", this::openViewPatientDialog);
        styleToolbarButton(toolbar, "Delete", this::openDeletePatientDialog);
        styleToolbarButton(toolbar, "Assign Appointment", this::openAssignAppointmentDialog);
        root.add(toolbar, BorderLayout.SOUTH);

        String[] cols = {"ID", "Name", "Age", "Condition"};
        Object[][] data = {{1, "Alice Johnson", 45, "Hypertension"}, {2, "Bob Lee", 32, "Diabetes"}};
        patientsTable = new JTable(new DefaultTableModel(data, cols));
        root.add(new JScrollPane(patientsTable), BorderLayout.CENTER);
        return root;
    }

    // REPORTS PANEL ----------------------------------------------------
    private JPanel buildReportsPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel header = new JLabel("Reports History", SwingConstants.LEFT);
        header.setFont(FONT_SECTION);
        header.setForeground(COLOR_PRIMARY.darker());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(header, BorderLayout.NORTH);

        String[] cols = {"Date", "Patient", "Type", "Status"};
        Object[][] data = {{"2025-01-10", "Alice Johnson", "Lab", "Completed"}, {"2025-01-11", "Bob Lee", "Imaging", "Pending"}};
        reportsTable = new JTable(new DefaultTableModel(data, cols));
        root.add(new JScrollPane(reportsTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        JButton btnExport = new JButton("Export");
        styleSecondaryButton(btnExport);
        btnExport.addActionListener(e -> openExportReportsDialog());
        footer.add(btnExport);
        root.add(footer, BorderLayout.SOUTH);
        return root;
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
        area.setText("Summary details will appear here. Charts and graphs can be added.");
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
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
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Name:")); JTextField name = new JTextField(); panel.add(name);
        panel.add(new JLabel("Age:")); JTextField age = new JTextField(); panel.add(age);
        panel.add(new JLabel("Condition:")); JTextField cond = new JTextField(); panel.add(cond);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Patient", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (!name.getText().isEmpty() && !age.getText().isEmpty()) {
                DefaultTableModel m = (DefaultTableModel) patientsTable.getModel();
                m.addRow(new Object[]{m.getRowCount() + 1, name.getText(), parseIntSafe(age.getText()), cond.getText()});
            } else {
                JOptionPane.showMessageDialog(this, "Please fill required fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void openViewPatientDialog() {
        int row = patientsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        DefaultTableModel m = (DefaultTableModel) patientsTable.getModel();
        String info = String.format("ID: %s\nName: %s\nAge: %s\nCondition: %s",
            m.getValueAt(row, 0), m.getValueAt(row, 1), m.getValueAt(row, 2), m.getValueAt(row, 3));
        JOptionPane.showMessageDialog(this, info, "Patient Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openDeletePatientDialog() {
        int row = patientsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected patient?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel m = (DefaultTableModel) patientsTable.getModel();
            m.removeRow(row);
        }
    }

    private void openAssignAppointmentDialog() {
        int row = patientsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Date (YYYY-MM-DD):")); JTextField date = new JTextField(); panel.add(date);
        panel.add(new JLabel("Type:")); JComboBox<String> type = new JComboBox<>(new String[]{"Consultation", "Follow-up", "Procedure"}); panel.add(type);
        int result = JOptionPane.showConfirmDialog(this, panel, "Assign Appointment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(this, "Appointment assigned on " + date.getText() + " (" + type.getSelectedItem() + ")");
        }
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
}