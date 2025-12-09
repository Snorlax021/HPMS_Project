package UI;

import Service.PatientService;
import Service.PatientService.ProvisionedAccount;
import Service.UserService;
import Model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Reusable two-column patient registration dialog used by Admin and Staff flows.
 * Collects patient info, auto-provisions PT/PW credentials via PatientService.createPatient,
 * and saves extended profile fields.
 */
public class PatientRegistrationDialog extends JDialog {
    public static class Result {
        public final Patient patient;
        public final ProvisionedAccount account; // may be null if provisioning failed
        public Result(Patient p, ProvisionedAccount a){ this.patient = p; this.account = a; }
    }

    private Result result;

    public static Result showDialog(Component parent) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        PatientRegistrationDialog dlg = new PatientRegistrationDialog(owner);
        dlg.setVisible(true);
        return dlg.result;
    }

    private PatientRegistrationDialog(Window owner) {
        super(owner, "Patient Registration", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel container = new JPanel(new BorderLayout(12,12));
        container.setBorder(new EmptyBorder(12,12,12,12));

        // Left and Right columns
        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); left.setBackground(Color.WHITE);
        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS)); right.setBackground(Color.WHITE);

        // Personal panel (use larger fields and weights for better horizontal expansion)
        JPanel personal = new JPanel(new GridBagLayout()); personal.setBackground(Color.WHITE);
        personal.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), "Personal"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0; gbc.weightx=0;
        JTextField firstName = new JTextField(24); JTextField middleName = new JTextField(24); JTextField surname = new JTextField(24);
        JTextField dobField = new JTextField(); dobField.setPreferredSize(new Dimension(240, 28)); dobField.setToolTipText("Format: YYYY-MM-DD");
        JComboBox<String> gender = new JComboBox<>(new String[]{"Male","Female","Other"}); gender.setEditable(true); gender.setPreferredSize(new Dimension(200, 28)); gender.setToolTipText("Select or type gender");
        JTextField ageField = new JTextField(); ageField.setEditable(false); ageField.setToolTipText("Auto-computed from DOB");
        // Order: First, Middle, Surname, DOB, Age, Gender
        addRow(personal, gbc, "First name:", firstName);
        addRow(personal, gbc, "Middle name:", middleName);
        addRow(personal, gbc, "Surname:", surname);
        addRow(personal, gbc, "DOB (YYYY-MM-DD):", dobField);
        addRow(personal, gbc, "Age:", ageField);
        addRow(personal, gbc, "Gender:", gender);
        // Auto-update age when DOB changes
        dobField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                try {
                    java.time.LocalDate dob = java.time.LocalDate.parse(dobField.getText().trim());
                    int age = Math.max(0, java.time.Period.between(dob, java.time.LocalDate.now()).getYears());
                    ageField.setText(String.valueOf(age));
                } catch (Exception ex) {
                    ageField.setText("");
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ update(); }
        });
        left.add(personal);

        // Contact panel (also larger fields)
        JPanel contact = new JPanel(new GridBagLayout()); contact.setBackground(Color.WHITE);
        contact.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), "Contact"));
        GridBagConstraints gbc2 = new GridBagConstraints(); gbc2.insets = new Insets(8,8,8,8); gbc2.fill = GridBagConstraints.HORIZONTAL; gbc2.gridx=0; gbc2.gridy=0; gbc2.weightx=0;
        JTextField email = new JTextField(); email.setPreferredSize(new Dimension(380, 28)); email.setToolTipText("Enter a valid email");
        JTextField phone = new JTextField(); phone.setPreferredSize(new Dimension(260, 28)); phone.setToolTipText("Digits only, e.g., 09171234567");
        JTextField address = new JTextField(); address.setPreferredSize(new Dimension(500, 28));
        JTextField emergName = new JTextField(); emergName.setPreferredSize(new Dimension(360, 28));
        JTextField emergPhone = new JTextField(); emergPhone.setPreferredSize(new Dimension(260, 28)); emergPhone.setToolTipText("Emergency contact number");
        addRow(contact, gbc2, "Email:", email);
        addRow(contact, gbc2, "Phone:", phone);
        addRow(contact, gbc2, "Address:", address);
        addRow(contact, gbc2, "Emergency Name:", emergName);
        addRow(contact, gbc2, "Emergency Phone:", emergPhone);
        left.add(Box.createVerticalStrut(10)); left.add(contact);

        // Optional panel (right)
        JPanel optional = new JPanel(new GridBagLayout()); optional.setBackground(Color.WHITE);
        optional.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), "Optional"));
        GridBagConstraints gbc3 = new GridBagConstraints(); gbc3.insets = new Insets(8,8,8,8); gbc3.fill = GridBagConstraints.HORIZONTAL; gbc3.gridx=0; gbc3.gridy=0; gbc3.weightx=0;
        // Replace text fields with dropdowns
        String[] civilOptions = new String[]{"Single","Married","Widowed","Separated","Divorced","Common-Law"};
        JComboBox<String> civilStatus = new JComboBox<>(civilOptions); civilStatus.setEditable(false); civilStatus.setPreferredSize(new Dimension(240, 28));
        String[] bloodOptions = new String[]{"O+","O-","A+","A-","B+","B-","AB+","AB-"};
        JComboBox<String> bloodType = new JComboBox<>(bloodOptions); bloodType.setEditable(false); bloodType.setPreferredSize(new Dimension(240, 28));
        addRow(optional, gbc3, "Civil Status:", civilStatus);
        addRow(optional, gbc3, "Blood Type:", bloodType);
        right.add(optional);

        // Use a split pane to give equal width to both sides and allow user resizing
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(left), new JScrollPane(right));
        split.setResizeWeight(0.50); // 50/50 split by default
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        container.add(split, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel"); JButton save = new JButton("Save");
        foot.add(cancel); foot.add(save);
        container.add(foot, BorderLayout.SOUTH);

        cancel.addActionListener(e -> { result = null; dispose(); });
        save.addActionListener(e -> onSave(firstName, middleName, surname, dobField, gender, email, phone, address, emergName, emergPhone, civilStatus, bloodType));

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setContentPane(container);
        setSize(Math.max(1100, screen.width - 100), Math.max(720, screen.height - 160));
        setLocationRelativeTo(owner);
        setResizable(true);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(140, 24)); // consistent label width
        gbc.gridx=0; gbc.weightx=0; panel.add(l, gbc);
        gbc.gridx=1; gbc.weightx=1; panel.add(comp, gbc); gbc.gridy++;
    }

    private void onSave(JTextField firstName, JTextField middleName, JTextField surname,
                        JTextField dobField, JComboBox<String> gender, JTextField email, JTextField phone,
                        JTextField address, JTextField emergName, JTextField emergPhone,
                        JComboBox<String> civilStatus, JComboBox<String> bloodType) {
        try {
            String fn = firstName.getText().trim(); String ln = surname.getText().trim();
            String mn = middleName.getText().trim(); String em = email.getText().trim();
            String ph = phone.getText().trim(); String addr = address.getText().trim();
            String g = gender.getSelectedItem()==null?"":gender.getSelectedItem().toString();
            String dobStr = dobField.getText().trim();
            if (fn.isEmpty() || ln.isEmpty() || g.isEmpty() || ph.isEmpty() || addr.isEmpty() || dobStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields: First, Surname, DOB, Gender, Phone, Address", "Validation", JOptionPane.WARNING_MESSAGE); return;
            }
            LocalDate dob = LocalDate.parse(dobStr);
            // Create patient (auto-provisions PT/PW)
            PatientService ps = PatientService.getInstance();
            Patient p = ps.createPatient(fn, ln, dob, g, ph, em, addr);
            // Retrieve generated credentials
            ProvisionedAccount acc = ps.getProvisionedAccountForPatient(p.getId()).orElse(null);
            // Save extended profile fields keyed by username when available
            if (acc != null) {
                PatientService.PatientProfile prof = ps.getProfileByUsername(acc.username);
                prof.firstName = fn; prof.middleName = mn; prof.surname = ln; prof.gender = g; prof.dateOfBirth = dobStr;
                prof.email = em; prof.phone = ph; prof.address = addr;
                prof.emergencyContactName = emergName.getText().trim();
                prof.emergencyContactNumber = emergPhone.getText().trim();
                String civil = civilStatus.getSelectedItem()==null?"":civilStatus.getSelectedItem().toString();
                String blood = bloodType.getSelectedItem()==null?"":bloodType.getSelectedItem().toString();
                prof.civilStatus = civil;
                prof.bloodType = blood;
                ps.saveProfile(acc.username, prof);
            }
            // Show credentials
            if (acc != null) {
                JOptionPane.showMessageDialog(this,
                    "Patient registered.\nUsername: " + acc.username + "\nPassword: " + acc.temporaryPassword,
                    "Account Created",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Patient registered, but account generation failed.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            }
            result = new Result(p, acc);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }
}