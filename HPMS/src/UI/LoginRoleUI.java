package UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginRoleUI extends JFrame {

    private JPanel contentPane;
    private JButton btnAdmin, btnDoctor, btnPatient, btnStaff;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginRoleUI frame = new LoginRoleUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginRoleUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 500, 300);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JLabel lbl = new JLabel("Select Role to Continue");
        lbl.setFont(new Font("Tahoma", Font.BOLD, 18));
        lbl.setBounds(120, 20, 300, 30);
        contentPane.add(lbl);

        btnAdmin = createRoleButton("Admin");
        btnAdmin.setBounds(60, 100, 140, 40);
        contentPane.add(btnAdmin);

        btnDoctor = createRoleButton("Doctor");
        btnDoctor.setBounds(260, 100, 140, 40);
        contentPane.add(btnDoctor);

        btnPatient = createRoleButton("Patient");
        btnPatient.setBounds(60, 160, 140, 40);
        contentPane.add(btnPatient);

        btnStaff = createRoleButton("Staff");
        btnStaff.setBounds(260, 160, 140, 40);
        contentPane.add(btnStaff);

        btnAdmin.addActionListener(e -> openLogin("Admin"));
        btnDoctor.addActionListener(e -> openLogin("Doctor"));
        btnPatient.addActionListener(e -> openLogin("Patient"));
        btnStaff.addActionListener(e -> openStaffLogin());
    }

    private JButton createRoleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(230, 230, 230));
        btn.setBorder(new javax.swing.border.LineBorder(Color.GRAY, 2, true));

        addHoverEffect(btn, new Color(230,230,230), new Color(200,200,200));
        return btn;
    }

    private void addHoverEffect(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normal);
                btn.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(hover.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(hover);
            }
        });
    }

    private void openLogin(String role) {
        LoginUI login = new LoginUI(role);
        login.setVisible(true);
        dispose();
    }

    private void openStaffLogin() {
        LoginStaffUI login = new LoginStaffUI();
        login.setVisible(true);
        dispose();
    }
}
