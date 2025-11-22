package UI;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DoctorDashboardPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private JPanel mainContentPanel;

	private JButton activeButton = null; // Track the currently active button
	private JButton dashboardButton;
	private JButton patientManagementButton;
	private JButton reportHistoryButton;
	private JButton summaryButton;

	private CardLayout cardLayout;

	/**
	 * Create the panel.
	 */
	public DoctorDashboardPanel() {
		setBackground(new Color(173, 216, 230));
		setBorder(new EmptyBorder(20, 20, 20, 20));
		setLayout(new BorderLayout(10, 10));

		// Button panel
		buttonPanel = new JPanel();
		buttonPanel.setBackground(new Color(173, 216, 230));
		buttonPanel.setLayout(new GridLayout(4, 1, 5, 5));

		// Define buttons
		dashboardButton = new JButton("Dashboard");
		patientManagementButton = new JButton("Patients Management");
		reportHistoryButton = new JButton("Reports History");
		summaryButton = new JButton("Summary");

		// Add hover and click effects
		addHoverAndClickEffects(dashboardButton, "Dashboard");
		addHoverAndClickEffects(patientManagementButton, "Patients Management");
		addHoverAndClickEffects(reportHistoryButton, "Reports History");
		addHoverAndClickEffects(summaryButton, "Summary");

		// Add buttons to the panel
		buttonPanel.add(dashboardButton);
		buttonPanel.add(patientManagementButton);
		buttonPanel.add(reportHistoryButton);
		buttonPanel.add(summaryButton);

		add(buttonPanel, BorderLayout.WEST);

		// Main content panel with CardLayout
		mainContentPanel = new JPanel();
		cardLayout = new CardLayout();
		mainContentPanel.setLayout(cardLayout);
		mainContentPanel.setBackground(Color.WHITE);
		mainContentPanel.setBorder(new LineBorder(new Color(0, 0, 0)));

		// Add blank panels for each section
		JPanel dashboardPanel = new JPanel();
		dashboardPanel.setBackground(Color.WHITE);
		JPanel userManagementPanel = new JPanel();
		userManagementPanel.setBackground(Color.WHITE);
		JPanel paymentHistoryPanel = new JPanel();
		paymentHistoryPanel.setBackground(Color.WHITE);
		JPanel summaryPanel = new JPanel();
		summaryPanel.setBackground(Color.WHITE);

		mainContentPanel.add(dashboardPanel, "Dashboard");
		dashboardPanel.setLayout(null);
		mainContentPanel.add(userManagementPanel, "User Management");
		mainContentPanel.add(paymentHistoryPanel, "Payment History");
		mainContentPanel.add(summaryPanel, "Summary");

		add(mainContentPanel, BorderLayout.CENTER);
		// Ensure the CardLayout remains active for the mainContentPanel
		mainContentPanel.setLayout(cardLayout);

		// Add content to the Dashboard panel
		JLabel dashboardLabel = new JLabel("Dashboard Overview");
		dashboardLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		dashboardLabel.setBounds(20, 20, 200, 30);
		dashboardPanel.add(dashboardLabel);

		JLabel totalPatientsLabel = new JLabel("Total Patients: 0");
		totalPatientsLabel.setBounds(20, 60, 200, 30);
		dashboardPanel.add(totalPatientsLabel);

		JLabel totalDoctorsLabel = new JLabel("Total Doctors: 0");
		totalDoctorsLabel.setBounds(20, 100, 200, 30);
		dashboardPanel.add(totalDoctorsLabel);

		// Add content to the User Management panel
		JLabel userManagementLabel = new JLabel("User Management");
		userManagementLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		userManagementLabel.setBounds(20, 20, 200, 30);
		userManagementPanel.add(userManagementLabel);

		JButton addUserButton = new JButton("Add User");
		addUserButton.setBounds(20, 60, 120, 30);
		userManagementPanel.add(addUserButton);

		JButton viewUsersButton = new JButton("View Users");
		
		viewUsersButton.setBounds(20, 100, 120, 30);
		userManagementPanel.add(viewUsersButton);
		
		JButton btnNewButton = new JButton("Delete User");
		userManagementPanel.add(btnNewButton);
		paymentHistoryPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JTable paymentTable = new JTable(new Object[][] { {"Date","Name", "Amount", "Description"} }, new Object[] {"Date","Name", "Amount", "Description"});
		JScrollPane paymentScrollPane = new JScrollPane(paymentTable);
		paymentHistoryPanel.add(paymentScrollPane);

		// Add content to the Summary panel
		JLabel summaryLabel = new JLabel("Summary");
		summaryLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		summaryLabel.setBounds(20, 20, 200, 30);
		summaryPanel.add(summaryLabel);

		JTextArea summaryTextArea = new JTextArea("Summary details will appear here.");
		summaryTextArea.setBounds(20, 60, 400, 200);
		summaryPanel.add(summaryTextArea);

		// Highlight the Dashboard button by default
		resetActiveButton();
		activeButton = dashboardButton;
		dashboardButton.setBackground(Color.CYAN);
		cardLayout.show(mainContentPanel, "Dashboard");
	}

	private void resetActiveButton() {
		if (activeButton != null) {
			activeButton.setBackground(Color.WHITE);
			activeButton = null;
		}
	}

	private void addHoverAndClickEffects(JButton button, String panelName) {
		button.setPreferredSize(new Dimension(156, 51));
		button.setFont(new Font("Tahoma", Font.BOLD, 13));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(true);
		button.setOpaque(true);
		button.setBackground(Color.WHITE);
		button.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				if (button != activeButton)
					button.setBackground(Color.LIGHT_GRAY);
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				if (button != activeButton)
					button.setBackground(Color.WHITE);
			}
		});

		button.addActionListener(e -> {
			resetActiveButton();
			activeButton = button;
			button.setBackground(Color.CYAN);
			cardLayout.show(mainContentPanel, panelName); // Switch to the corresponding panel
		});
	}
}