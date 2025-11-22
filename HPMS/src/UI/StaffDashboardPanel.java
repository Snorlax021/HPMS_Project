package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StaffDashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JPanel buttonPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JButton activeButton = null;

    // Staff Buttons
    private JButton patientRegButton;
    private JButton medicalRecordsButton;
    private JButton billingHistoryButton;
    private JButton labTestButton;
    private JButton admissionButton;
    private JButton summaryButton;

    public StaffDashboardPanel() {
        setBackground(new Color(173, 216, 230));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(10, 10));  // Use BorderLayout for resizability

        // LEFT BUTTON PANEL
        buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(173, 216, 230));
        buttonPanel.setLayout(new GridLayout(6, 1, 5, 5));
        buttonPanel.setPreferredSize(new Dimension(200, 300));  // Preferred size instead of bounds

        // Create buttons
        patientRegButton = new JButton("Patient Registration & Records");
        medicalRecordsButton = new JButton("Medical & Treatment Records");
        billingHistoryButton = new JButton("Billing & Payment History");
        labTestButton = new JButton("Laboratory & Test Management");
        admissionButton = new JButton("Admission & Discharge Management");
        summaryButton = new JButton("Dashboard & Summary");

        // Add hover + click effects
        addHoverAndClick(patientRegButton, "PatientReg");
        addHoverAndClick(medicalRecordsButton, "MedicalRecords");
        addHoverAndClick(billingHistoryButton, "BillingHistory");
        addHoverAndClick(labTestButton, "LabTest");
        addHoverAndClick(admissionButton, "Admission");
        addHoverAndClick(summaryButton, "Summary");

        // Add buttons to left panel
        buttonPanel.add(summaryButton);
        buttonPanel.add(patientRegButton);
        buttonPanel.add(medicalRecordsButton);
        buttonPanel.add(billingHistoryButton);
        buttonPanel.add(labTestButton);
        buttonPanel.add(admissionButton);
        

        add(buttonPanel, BorderLayout.WEST);

        // MAIN CONTENT PANEL (CardLayout)
        mainContentPanel = new JPanel();
        cardLayout = new CardLayout();
        mainContentPanel.setLayout(cardLayout);
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(new LineBorder(Color.BLACK));

        // Create and add section panels
        mainContentPanel.add(createPatientRegPanel(), "PatientReg");
        mainContentPanel.add(createMedicalRecordsPanel(), "MedicalRecords");
        mainContentPanel.add(createBillingHistoryPanel(), "BillingHistory");
        mainContentPanel.add(createLabTestPanel(), "LabTest");
        mainContentPanel.add(createAdmissionPanel(), "Admission");
        mainContentPanel.add(createSummaryPanel(), "Summary");

        add(mainContentPanel, BorderLayout.CENTER);

        // DEFAULT ACTIVE PANEL
        setActiveButton(summaryButton);
        cardLayout.show(mainContentPanel, "Summary");
    }

    // Extracted method for creating Patient Registration panel
    private JPanel createPatientRegPanel() {
        JPanel patientRegistrationpanel = new JPanel(new BorderLayout());
        patientRegistrationpanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Patient Registration & Records", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        patientRegistrationpanel.add(label, BorderLayout.CENTER);
        return patientRegistrationpanel;
    }

    // Similar for other panels...
    private JPanel createMedicalRecordsPanel() {
        JPanel medicalRecordPanel = new JPanel(new BorderLayout());
        medicalRecordPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Medical & Treatment Records", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        medicalRecordPanel.add(label, BorderLayout.CENTER);
        return medicalRecordPanel;
    }

    private JPanel createBillingHistoryPanel() {
        JPanel billHistoryPanel = new JPanel(new BorderLayout());
        billHistoryPanel.setBackground(Color.WHITE);
        
        // Improved JTable with a model and sample data
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Date");
        model.addColumn("Patient");
        model.addColumn("Amount");
        model.addColumn("Description");
        model.addRow(new Object[]{"2023-10-01", "John Doe", "$100", "Consultation"});
        model.addRow(new Object[]{"2023-10-02", "Jane Smith", "$200", "Lab Test"});
        
        JTable table = new JTable(model);
        table.setEnabled(false);  // Make non-editable for display
        billHistoryPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return billHistoryPanel;
    }

    private JPanel createLabTestPanel() {
        JPanel laboratoryManagementPanel = new JPanel(new BorderLayout());
        laboratoryManagementPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Laboratory & Test Management", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        laboratoryManagementPanel.add(label, BorderLayout.CENTER);
        return laboratoryManagementPanel;
    }

    private JPanel createAdmissionPanel() {
        JPanel admissionDischargePanel = new JPanel(new BorderLayout());
        admissionDischargePanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Admission & Discharge Management", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        admissionDischargePanel.add(label, BorderLayout.CENTER);
        return admissionDischargePanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Dashboard & Summary", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        summaryPanel.add(label, BorderLayout.NORTH);
        
        JTextArea textArea = new JTextArea("Summary content here.\n- Total Patients: 150\n- Pending Bills: 5");
        textArea.setEditable(false);
        summaryPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return summaryPanel;
    }

    // BUTTON EFFECTS METHOD (unchanged, but could add tooltips)
    private void addHoverAndClick(JButton button, String panelName) {
        button.setPreferredSize(new Dimension(180, 60));
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != activeButton) button.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != activeButton) button.setBackground(Color.WHITE);
            }
        });

        // Click => change panel
        button.addActionListener(e -> {
            setActiveButton(button);
            cardLayout.show(mainContentPanel, panelName);
        });
    }

    // SWITCH ACTIVE BUTTON COLOR
    private void setActiveButton(JButton newButton) {
        if (activeButton != null) {
            activeButton.setBackground(Color.WHITE);
        }
        activeButton = newButton;
        activeButton.setBackground(Color.CYAN);
    }
}
