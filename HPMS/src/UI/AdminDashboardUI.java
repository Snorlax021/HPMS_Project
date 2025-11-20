package UI;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.BoxLayout;

public class AdminDashboardUI extends JPanel {

	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;

	/**
	 * Create the panel.
	 */
	public AdminDashboardUI() {
		setBackground(new Color(173, 216, 230));
		setBorder(new EmptyBorder(20, 20, 20, 20));
		setLayout(null);
		
		// Main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setBounds(186, 20, 880, 610);
		contentPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		contentPanel.setBackground(Color.WHITE);
		add(contentPanel);
		contentPanel.setLayout(null);
		
		// Button panel
		buttonPanel = new JPanel();
		buttonPanel.setBounds(20, 20, 156, 610);
		buttonPanel.setBackground(new Color(173, 216, 230));
		add(buttonPanel);
		
		// Add buttons
		addButton(buttonPanel, "User Management");
		addButton(buttonPanel, "System Logs");
		addButton(buttonPanel, "Settings");
		addButton(buttonPanel, "Reports");
	}

	private void addButton(JPanel panel, String text) {
		JButton button = new JButton(text);
		button.setBounds(0, 0, 156, 38);
		button.setFont(new Font("Tahoma", Font.BOLD, 13));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		button.setBackground(Color.WHITE);
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				button.setBackground(Color.CYAN);
			}
			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(Color.WHITE);
			}
		});
		buttonPanel.setLayout(null);
		panel.add(button);
	}
}