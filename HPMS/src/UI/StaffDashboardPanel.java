package UI;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.border.LineBorder;

public class StaffDashboardPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public StaffDashboardPanel() {
		setBackground(new Color(173, 216, 230));
		setBorder(new EmptyBorder(80, 20, 20, 20));
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		add(panel);
		panel.setLayout(null);

		
	}

}
