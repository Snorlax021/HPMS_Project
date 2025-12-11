package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PatientLitePanel extends JPanel {
    public PatientLitePanel(String username) {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));
        JLabel h = new JLabel("Patient Dashboard (Lite)", SwingConstants.LEFT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(h, BorderLayout.NORTH);

        JTextArea body = new JTextArea();
        body.setEditable(false);
        body.setText("Welcome " + (username==null?"(patient)":username) + "\n\nThis is the lightweight Patient dashboard.\n\n" +
                "Use the full Patient Dashboard for complete features.");
        add(new JScrollPane(body), BorderLayout.CENTER);
    }
}
