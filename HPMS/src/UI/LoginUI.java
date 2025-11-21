package UI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

public class LoginUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginUI frame = new LoginUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoginUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 500);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(32, 118, 223));
		panel.setBounds(0, 0, 634, 57);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("LOGO HERE    HPMS: Hospital Patient Management System");
		lblNewLabel_1.setForeground(new Color(255, 255, 255));
		lblNewLabel_1.setFont(new Font("Artifakt Element Medium", Font.BOLD, 14));
		lblNewLabel_1.setBounds(10, 11, 460, 35);
		panel.add(lblNewLabel_1);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(173, 216, 230));
		panel_1.setBounds(68, 110, 498, 294);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Username:\r\n\r\n");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setBounds(32, 40, 86, 26);
		panel_1.add(lblNewLabel);
		
		JLabel lblPassword = new JLabel("Password:\r\n");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblPassword.setBounds(32, 88, 86, 26);
		panel_1.add(lblPassword);
		
		JLabel lblRole = new JLabel("Role:");
		lblRole.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblRole.setBounds(32, 146, 86, 26);
		panel_1.add(lblRole);
		
		textField = new JTextField();
		textField.setBounds(139, 43, 257, 25);
		panel_1.add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(139, 91, 257, 25);
		panel_1.add(textField_1);
		
		JButton btnNewButton = new JButton("Admin");
		btnNewButton.setBounds(87, 145, 89, 32);
		panel_1.add(btnNewButton);
		
		JButton btnDoctor = new JButton("Doctor");
		btnDoctor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnDoctor.setBounds(186, 145, 89, 32);
		panel_1.add(btnDoctor);
		
		JButton btnStaff = new JButton("Staff\r\n");
		btnStaff.setBounds(285, 145, 89, 32);
		panel_1.add(btnStaff);
		
		JButton btnPatient = new JButton("Patient\r\n");
		btnPatient.setBounds(384, 145, 89, 32);
		panel_1.add(btnPatient);
		
		JButton btnNewButton_1 = new JButton("Log In");
		btnNewButton_1.setFont(new Font("Verdana", Font.BOLD, 15));
		btnNewButton_1.setBackground(new Color(140, 238, 140));
		btnNewButton_1.setBounds(196, 213, 108, 32);
		btnNewButton_1.setContentAreaFilled(true);
		panel_1.add(btnNewButton_1);
		
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = textField.getText();
				String password = textField_1.getText();

				// Example validation logic (replace with actual authentication logic)
				if (username.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				handleLogin(username, password);
			}
		});

	}
	
	private void handleLogin(String username, String password) {
        // Temporary accounts for testing
        String adminUsername = "admin";
        String adminPassword = "admin123";
        String doctorUsername = "doctor";
        String doctorPassword = "doctor123";

        if (username.equals(adminUsername) && password.equals(adminPassword)) {
            // Open Admin Dashboard
            DashboardUI dashboard = new DashboardUI("ADMIN");
            dashboard.setVisible(true);
            this.dispose();
            
        } else if (username.equals(doctorUsername) && password.equals(doctorPassword)) {
            // Open Doctor Dashboard
        	DashboardUI dashboard = new DashboardUI("DOCTOR");
            dashboard.setVisible(true);
            this.dispose();
            
        } else if (username.equals(doctorUsername) && password.equals(doctorPassword)) {
            // Open Doctor Dashboard
        	DashboardUI dashboard = new DashboardUI("STAFF");
            dashboard.setVisible(true);
            this.dispose();
            
        }else if (username.equals(doctorUsername) && password.equals(doctorPassword)) {
            // Open Doctor Dashboard
        	DashboardUI dashboard = new DashboardUI("USER");
            dashboard.setVisible(true);
            this.dispose();
        
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}