package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DashboardUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DashboardUI frame = new DashboardUI();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public DashboardUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1099, 750);
        setResizable(false);

        contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        // HEADER PANEL (NORTH)
        JPanel panel = new JPanel();
        panel.setBackground(SystemColor.textHighlight);
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setLayout(null);
        contentPane.add(panel, BorderLayout.NORTH);

        JLabel lblNewLabel_1 = new JLabel("LOGO HERE    HPMS: Hospital Patient Management System");
        lblNewLabel_1.setForeground(Color.WHITE);
        lblNewLabel_1.setFont(new Font("Artifakt Element Medium", Font.BOLD, 14));
        lblNewLabel_1.setBounds(10, 0, 600, 60);
        panel.add(lblNewLabel_1);

        JButton btnNewButton = new JButton("Logout");
        btnNewButton.setBounds(980, 18, 90, 25);
        panel.add(btnNewButton);

        // MAIN CENTER PANEL
        JPanel dashboardPanel = new StaffDashboardPanel();
        contentPane.add(dashboardPanel, BorderLayout.CENTER);
    }
}
