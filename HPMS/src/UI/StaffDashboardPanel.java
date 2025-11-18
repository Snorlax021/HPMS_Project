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

public class StaffDashboardPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public StaffDashboardPanel() {
		setBackground(new Color(173, 216, 230));
		setBorder(new EmptyBorder(100, 20, 20, 20));
		setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(20, 102, 1043, 523);
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		add(panel);
		panel.setLayout(null);
		
		JButton btnNewButton = new JButton("Patient Registration\r\n");
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnNewButton.setBounds(20, 63, 175, 38);
		btnNewButton.setBorderPainted(false);   // removes border
		btnNewButton.setFocusPainted(false);    // removes focus border
		btnNewButton.setContentAreaFilled(false); // removes background
		btnNewButton.setOpaque(true);           // allows background color to show
		btnNewButton.setBorder(new EtchedBorder(15));
		btnNewButton.setBackground(Color.WHITE);
		btnNewButton.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		    	btnNewButton.setBackground(Color.CYAN); // change color on hover
		    }
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    	btnNewButton.setBackground(Color.WHITE); // revert color
		    }
		});
		add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Appoinments");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnNewButton_1.setBounds(194, 63, 175, 38);
		btnNewButton_1.setBorderPainted(false);   // removes border
		btnNewButton_1.setFocusPainted(false);    // removes focus border
		btnNewButton_1.setContentAreaFilled(false); // removes background
		btnNewButton_1.setOpaque(true);           // allows background color to show
		btnNewButton_1.setBorder(new EtchedBorder(15));
		btnNewButton_1.setBackground(Color.WHITE);
		btnNewButton_1.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		    	btnNewButton_1.setBackground(Color.CYAN); // change color on hover
		    }
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    	btnNewButton_1.setBackground(Color.WHITE); // revert color
		    }
		});
		add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Patient Registration\r\n");
		btnNewButton_2.setBounds(368, 63, 175, 38);
		add(btnNewButton_2);
		btnNewButton_2.setOpaque(true);
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnNewButton_2.setFocusPainted(false);
		btnNewButton_2.setBorderPainted(false);
		btnNewButton_2.setContentAreaFilled(false);
		btnNewButton_2.setOpaque(true);
		btnNewButton_2.setBorder(new EtchedBorder(15));
		btnNewButton_2.setBackground(Color.WHITE);
		btnNewButton_2.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		    	btnNewButton_2.setBackground(Color.CYAN); // change color on hover
		    }
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    	btnNewButton_2.setBackground(Color.WHITE); // revert color
		    }
		});

		
	}

}
