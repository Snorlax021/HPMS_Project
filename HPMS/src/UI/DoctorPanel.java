package UI;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;

public class DoctorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private JPanel mainContentPanel;
	
	private JButton activeButton = null; // Track the currently active button
	private CardLayout cardLayout;

	/**
	 * Create the panel.
	 */
	public DoctorPanel() {
		setBackground(new Color(173, 216, 230));
		setBorder(new EmptyBorder(20, 20, 20, 20));
		setLayout(null);
		
		// Main content panel
		mainContentPanel = new JPanel();
		mainContentPanel.setBounds(186, 20, 880, 610);
		mainContentPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		mainContentPanel.setBackground(Color.WHITE);
		add(mainContentPanel);
		
		// Initialize CardLayout for main content panel
		cardLayout = new CardLayout();
		mainContentPanel.setLayout(cardLayout);

		// Add blank panels for each section
		JPanel dashboardPanel = new JPanel();
		dashboardPanel.setBackground(Color.WHITE);
		JPanel appointmentsPanel = new JPanel();
		appointmentsPanel.setBackground(Color.WHITE);
		JPanel patientsPanel = new JPanel();
		patientsPanel.setBackground(Color.WHITE);

		mainContentPanel.add(dashboardPanel, "Dashboard");
		mainContentPanel.add(appointmentsPanel, "Appointments");
		mainContentPanel.add(patientsPanel, "Patients");
		
		// Button panel
		buttonPanel = new JPanel();
		buttonPanel.setBounds(20, 20, 156, 610);
		buttonPanel.setBackground(new Color(173, 216, 230));
		add(buttonPanel);
		
		// Add buttons
		addButton(buttonPanel, "Patient Records");
		addButton(buttonPanel, "Appointments");
		addButton(buttonPanel, "Prescriptions");
		addButton(buttonPanel, "Reports");
		
		// Highlight the Dashboard button by default
		resetActiveButton();
		if (buttonPanel.getComponent(0) instanceof JButton) {
            activeButton = (JButton) buttonPanel.getComponent(0); // Assuming "Patient Records" is the first button
            activeButton.setBackground(Color.CYAN);
            cardLayout.show(mainContentPanel, "Dashboard");
        }
		
		// Add hover and click effects to buttons
		addHoverAndClickEffects((JButton)buttonPanel.getComponent(0), "Dashboard");
		addHoverAndClickEffects((JButton)buttonPanel.getComponent(1), "Appointments");
		addHoverAndClickEffects((JButton)buttonPanel.getComponent(2), "Patients");
	}

	private void resetActiveButton() {
		if (activeButton != null) {
			activeButton.setBackground(Color.WHITE);
			activeButton = null;
		}
	}

	private void addButton(JPanel panel, String text) {
		JButton button = new JButton(text);
		button.setBounds(0, 0, 156, 51);
		button.setFont(new Font("Tahoma", Font.BOLD, 13));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		button.setBackground(Color.WHITE);
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				if (button != activeButton) {
					button.setBackground(Color.CYAN);
				}
			}
			public void mouseExited(java.awt.event.MouseEvent evt) {
				if (button != activeButton) {
					button.setBackground(Color.WHITE);
				}
			}
		});
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activeButton != null) {
					activeButton.setBackground(Color.WHITE); // Reset previous active button
				}
				activeButton = button;
				button.setBackground(Color.LIGHT_GRAY); // Highlight the clicked button
				// Add functionality for the button here
			}
		});
		buttonPanel.setLayout(null);
		panel.add(button);
	}

	private void addHoverAndClickEffects(JButton button, String panelName) {
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