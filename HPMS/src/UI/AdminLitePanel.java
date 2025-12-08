package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminLitePanel extends JPanel {
    public AdminLitePanel(String username) {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));
        JLabel h = new JLabel("Admin Dashboard (Lite)", SwingConstants.LEFT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(h, BorderLayout.NORTH);

        JTextArea body = new JTextArea();
        body.setEditable(false);
        body.setText("Welcome " + (username==null?"(admin)":username) + "\n\nThis is the lightweight Admin dashboard.\n\n" +
                "Use the full Admin Dashboard for complete features.");
        add(new JScrollPane(body), BorderLayout.CENTER);
    }
}
