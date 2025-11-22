package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PatientDashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JPanel buttonPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JButton activeButton = null;

    // Patient Buttons
    private JButton profileButton;
    private JButton appointmentsButton;
    private JButton medicalHistoryButton;
    private JButton billsButton;
    private JButton labResultsButton;
    private JButton summaryButton;

    public PatientDashboardPanel() {
        setBackground(new Color(200, 230, 255));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(10, 10));

        // LEFT BUTTON PANEL
        buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(200, 230, 255));
        buttonPanel.setLayout(new GridLayout(6, 1, 5, 5));
        buttonPanel.setPreferredSize(new Dimension(200, 300));

        profileButton = new JButton("My Profile");
        appointmentsButton = new JButton("Appointments");
        medicalHistoryButton = new JButton("Medical History");
        billsButton = new JButton("Billing Info");
        labResultsButton = new JButton("Lab Results");
        summaryButton = new JButton("Dashboard Summary");

        addHoverAndClick(profileButton, "Profile");
        addHoverAndClick(appointmentsButton, "Appointments");
        addHoverAndClick(medicalHistoryButton, "MedicalHistory");
        addHoverAndClick(billsButton, "Bills");
        addHoverAndClick(labResultsButton, "LabResults");
        addHoverAndClick(summaryButton, "Summary");

        buttonPanel.add(profileButton);
        buttonPanel.add(appointmentsButton);
        buttonPanel.add(medicalHistoryButton);
        buttonPanel.add(billsButton);
        buttonPanel.add(labResultsButton);
        buttonPanel.add(summaryButton);

        add(buttonPanel, BorderLayout.WEST);

        // MAIN CONTENT PANEL
        mainContentPanel = new JPanel();
        cardLayout = new CardLayout();
        mainContentPanel.setLayout(cardLayout);
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(new LineBorder(Color.BLACK));

        // Create and add section panels
        mainContentPanel.add(createProfilePanel(), "Profile");
        mainContentPanel.add(createAppointmentsPanel(), "Appointments");
        mainContentPanel.add(createMedicalHistoryPanel(), "MedicalHistory");
        mainContentPanel.add(createBillsPanel(), "Bills");
        mainContentPanel.add(createLabResultsPanel(), "LabResults");
        mainContentPanel.add(createSummaryPanel(), "Summary");

        add(mainContentPanel, BorderLayout.CENTER);

        // Default panel
        setActiveButton(summaryButton);
        cardLayout.show(mainContentPanel, "Summary");
    }

    // Extracted methods for panel creation
    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("My Profile", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        profilePanel.add(label, BorderLayout.CENTER);
        // Add more components here, e.g., form fields for profile editing
        return profilePanel;
    }

    private JPanel createAppointmentsPanel() {
        JPanel appointmentPanel = new JPanel(new BorderLayout());
        appointmentPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Appointments", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        appointmentPanel.add(label, BorderLayout.CENTER);
        return appointmentPanel;
    }

    private JPanel createMedicalHistoryPanel() {
        JPanel medicalHistoryPanel = new JPanel(new BorderLayout());
        medicalHistoryPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Medical History", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        medicalHistoryPanel.add(label, BorderLayout.CENTER);
        return medicalHistoryPanel;
    }

    private JPanel createBillsPanel() {
        JPanel billingPanel = new JPanel(new BorderLayout());
        billingPanel.setBackground(Color.WHITE);
        
        // Improved JTable with model and sample data
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Date");
        model.addColumn("Description");
        model.addColumn("Amount");
        model.addRow(new Object[]{"2023-10-01", "Consultation", "$50"});
        model.addRow(new Object[]{"2023-10-05", "Lab Test", "$75"});
        
        JTable table = new JTable(model);
        table.setEnabled(false);  // Non-editable for display
        billingPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return billingPanel;
    }

    private JPanel createLabResultsPanel() {
        JPanel labResultPanel = new JPanel(new BorderLayout());
        labResultPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Lab Results", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        labResultPanel.add(label, BorderLayout.CENTER);
        return labResultPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Dashboard Summary", JLabel.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        summaryPanel.add(label, BorderLayout.NORTH);
        
        JTextArea textArea = new JTextArea("Overview of your health and activities.\n- Upcoming Appointments: 2\n- Pending Bills: 1");
        textArea.setEditable(false);
        summaryPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return summaryPanel;
    }

    // Button Effects (unchanged, but consider adding tooltips)
    private void addHoverAndClick(JButton button, String panelName) {
        button.setPreferredSize(new Dimension(180, 60));
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != activeButton) button.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != activeButton) button.setBackground(Color.WHITE);
            }
        });

        button.addActionListener(e -> {
            setActiveButton(button);
            cardLayout.show(mainContentPanel, panelName);
        });
    }

    private void setActiveButton(JButton newButton) {
        if (activeButton != null) {
            activeButton.setBackground(Color.WHITE);
        }
        activeButton = newButton;
        activeButton.setBackground(Color.CYAN);
    }
}
