package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DoctorLitePanel extends JPanel {
    public DoctorLitePanel(String username) {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));
        JLabel h = new JLabel("Doctor Dashboard (Lite)", SwingConstants.LEFT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(h, BorderLayout.NORTH);

        JTextArea body = new JTextArea();
        body.setEditable(false);
        body.setText("Welcome " + (username==null?"(doctor)":username) + "\n\nThis is the lightweight Doctor dashboard.\n\n" +
                "Use the full Doctor Dashboard for complete features.");
        add(new JScrollPane(body), BorderLayout.CENTER);
    }
}
