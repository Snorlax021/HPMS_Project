package UI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PatientPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;

    /**
     * Create the panel.
     */
    public PatientPanel() {
        // Use BorderLayout for better responsiveness instead of null layout
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Top panel for title, search, and buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(245, 247, 250));

        JLabel lblTitle = new JLabel("Patient Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topPanel.add(lblTitle);

        txtSearch = new JTextField(20);  // Declare as instance variable
        topPanel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        topPanel.add(btnSearch);

        JButton btnAdd = new JButton("New Patient");
        topPanel.add(btnAdd);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Name", "Age", "Gender", "Blood Group", "Phone", "Email"};
        Object[][] data = {
            {"Elizabeth Polson", "32", "Female", "B+ve", "+91 1234567890", "elizabethpolson@hotmail.com"},
            {"John David", "28", "Male", "B+ve", "+91 1234567890", "davidjohn22@gmail.com"},
            {"Krishtav Rajan", "24", "Male", "AB-ve", "+91 1234567890", "krishnarajan23@gmail.com"},
            {"Sumanth Tinson", "26", "Male", "O+ve", "+91 1234567890", "tintintin@gmail.com"},
        };

        tableModel = new DefaultTableModel(data, columns);
        table = new JTable(tableModel);  // Use the model directly
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listeners for functionality
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = txtSearch.getText().trim();
                if (!searchText.isEmpty()) {
                    // Simple search example: filter by name (case-insensitive)
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String name = (String) tableModel.getValueAt(i, 0);
                        if (name != null && name.toLowerCase().contains(searchText.toLowerCase())) {
                            table.setRowSelectionInterval(i, i);
                            table.scrollRectToVisible(table.getCellRect(i, 0, true));
                            return;
                        }
                    }
                    JOptionPane.showMessageDialog(null, "Patient not found.");
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a search term.");
                }
            }
        });

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Example: Add a new row (in a real app, open a dialog for input)
                tableModel.addRow(new Object[]{"New Patient", "", "", "", "", ""});
                JOptionPane.showMessageDialog(null, "New patient row added. Edit the table to fill details.");
            }
        });
    }
}
