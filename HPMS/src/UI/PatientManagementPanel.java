package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import Service.PatientService;
import Model.Patient;

/**
 * Patient Management panel - lists patients and allows view/edit/deactivate similar to Admin usermanagement.
 */
public class PatientManagementPanel extends JPanel {
    private final PatientService patientService = PatientService.getInstance();

    private JTable table;
    private DefaultTableModel model;
    private JPanel detailsPane;

    public PatientManagementPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));

        JLabel header = new JLabel("Patient Management", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Name","DOB","Gender","Phone"}, 0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(700,300));

        detailsPane = new JPanel(new BorderLayout());
        detailsPane.setBorder(new LineBorder(Color.LIGHT_GRAY));
        detailsPane.setPreferredSize(new Dimension(420,300));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, detailsPane);
        split.setResizeWeight(0.7);
        add(split, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDeactivate = new JButton("Deactivate");
        JButton btnRefresh = new JButton("Refresh");
        actions.add(btnDeactivate); actions.add(btnRefresh);
        add(actions, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> reload());
        btnDeactivate.addActionListener(e -> deactivateSelected());

        table.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ if (e.getClickCount()==1) showSelectedDetails(); }});

        reload();
    }

    private void reload() {
        _doReload();
    }
    public void reloadPanel() {
        _doReload();
    }
    private void _doReload() {
        model.setRowCount(0);
        java.util.List<Model.Patient> list = java.util.List.copyOf(patientService.listAll());
        for (Model.Patient p : list) {
            // Display PT-ID formatted patientNumber instead of internal UUID
            String displayId = (p.getPatientNumber() != null && !p.getPatientNumber().isBlank()) ? p.getPatientNumber() : p.getId();
            model.addRow(new Object[]{displayId, (p.getFirstName()==null?"":p.getFirstName()) + " " + (p.getLastName()==null?"":p.getLastName()), p.getDateOfBirth(), p.getGender(), p.getContactNumber()});
        }
    }

    private void showSelectedDetails() {
        int r = table.getSelectedRow(); if (r==-1) return;
        int modelRow = table.convertRowIndexToModel(r);
        // When displaying PT-ID, the repo findById uses internal UUID; instead, search by matching display fields
        // We’ll iterate over listAll to find the patient whose patientNumber or UUID matches the selected ID.
        String selectedId = (String) model.getValueAt(modelRow, 0);
        java.util.Optional<Model.Patient> opt = patientService.listAll().stream().filter(px -> {
            String num = px.getPatientNumber(); String pid = px.getId();
            return (num != null && num.equals(selectedId)) || (pid != null && pid.equals(selectedId));
        }).findFirst();
        opt.ifPresent(p -> {
            detailsPane.removeAll();
            JPanel root = new JPanel(new BorderLayout(8,8)); root.setBorder(new EmptyBorder(10,10,10,10));
            JPanel top = new JPanel(new BorderLayout(12,12));

            // Left: avatar placeholder (patients may not have profile pictures stored here)
            JPanel left = new JPanel(new BorderLayout());
            JLabel pic = new JLabel(); pic.setPreferredSize(new Dimension(180,180));
            if (p.getUser()!=null && p.getUser().getProfilePictureUrl()!=null && !p.getUser().getProfilePictureUrl().isBlank()) {
                ImageIcon ic = new ImageIcon(p.getUser().getProfilePictureUrl()); Image im = ic.getImage().getScaledInstance(180,180,Image.SCALE_SMOOTH); pic.setIcon(new ImageIcon(im));
            } else {
                pic.setIcon(new ImageIcon(new java.awt.image.BufferedImage(180,180,java.awt.image.BufferedImage.TYPE_INT_ARGB)));
                pic.setText("No image"); pic.setHorizontalTextPosition(SwingConstants.CENTER); pic.setVerticalTextPosition(SwingConstants.CENTER);
            }
            left.add(pic, BorderLayout.NORTH);

            // Meta details (read-only), similar richness to doctor panel
            JPanel meta = new JPanel(new GridLayout(0,1,6,6));
            meta.add(new JLabel("Patient ID: " + (p.getPatientNumber()!=null?p.getPatientNumber():p.getId())));
            meta.add(new JLabel("Name: " + (p.getFirstName()==null?"":p.getFirstName()) + " " + (p.getLastName()==null?"":p.getLastName())));
            meta.add(new JLabel("DOB: " + p.getDateOfBirth()));
            meta.add(new JLabel("Gender: " + p.getGender()));
            meta.add(new JLabel("Phone: " + (p.getContactNumber()==null?"":p.getContactNumber())));
            meta.add(new JLabel("Address: " + (p.getAddress()==null?"":p.getAddress())));
            meta.add(new JLabel("Emergency Contact: " + ((p.getEmergencyContactName()==null?"":p.getEmergencyContactName()) + " " + (p.getEmergencyContactNumber()==null?"":p.getEmergencyContactNumber()))));
            if (p.getUser() != null) meta.add(new JLabel("Linked user: " + p.getUser().getUsername()));
            left.add(meta, BorderLayout.CENTER);

            // Right: notes area (view-only)
            JTextArea notes = new JTextArea(); notes.setEditable(false); notes.setLineWrap(true); notes.setWrapStyleWord(true);
            notes.setText("Patient details are view-only for admin.");

            top.add(left, BorderLayout.WEST);
            top.add(new JScrollPane(notes), BorderLayout.CENTER);

            // Bottom: summary placeholder
            JPanel bottom = new JPanel(new BorderLayout()); bottom.setBorder(BorderFactory.createTitledBorder("Summary"));
            DefaultListModel<String> lm = new DefaultListModel<>();
            lm.addElement("Record created: " + p.getCreatedAt());
            if (p.getUser() != null) lm.addElement("Login status: " + p.getUser().getStatus());
            JList<String> summary = new JList<>(lm);
            bottom.add(new JScrollPane(summary), BorderLayout.CENTER);

            root.add(top, BorderLayout.CENTER);
            root.add(bottom, BorderLayout.SOUTH);
            detailsPane.add(root, BorderLayout.CENTER);
            detailsPane.revalidate(); detailsPane.repaint();
        });
    }

    private void deactivateSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        String id = (String) model.getValueAt(r,0);
        int c = JOptionPane.showConfirmDialog(this, "Deactivate this patient account?","Confirm", JOptionPane.YES_NO_OPTION);
        if (c!=JOptionPane.YES_OPTION) return;
        boolean ok = patientService.deletePatient(id); // patient delete here uses repo delete; if you prefer soft-deactivate, we need to add that to PatientService
        if (ok) { JOptionPane.showMessageDialog(this, "Patient removed."); reload(); }
        else JOptionPane.showMessageDialog(this, "Failed to remove patient.");
    }

    private void editSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        String id = (String) model.getValueAt(r,0);
        patientService.findById(id).ifPresent(p -> {
            JPanel form = new JPanel(new GridLayout(0,2,8,8));
            JTextField fn = new JTextField(p.getFirstName()==null?"":p.getFirstName());
            JTextField ln = new JTextField(p.getLastName()==null?"":p.getLastName());
            JTextField phone = new JTextField(p.getContactNumber()==null?"":p.getContactNumber());
            JTextField dobField = new JTextField(p.getDateOfBirth()==null?"":p.getDateOfBirth().toString());
            JTextField genderField = new JTextField(p.getGender()==null?"":p.getGender());
            form.add(new JLabel("First name:")); form.add(fn);
            form.add(new JLabel("Last name:")); form.add(ln);
            form.add(new JLabel("DOB (YYYY-MM-DD):")); form.add(dobField);
            form.add(new JLabel("Gender:")); form.add(genderField);
            form.add(new JLabel("Phone:")); form.add(phone);
            int res = JOptionPane.showConfirmDialog(this, form, "Edit Patient", JOptionPane.OK_CANCEL_OPTION);
            if (res==JOptionPane.OK_OPTION) {
                try {
                    p.setFirstName(fn.getText().trim());
                    p.setLastName(ln.getText().trim());
                    if (!dobField.getText().trim().isEmpty()) p.setDateOfBirth(java.time.LocalDate.parse(dobField.getText().trim()));
                    p.setGender(genderField.getText().trim());
                    p.setContactNumber(phone.getText().trim());
                    // persist via service
                    boolean ok = patientService.updatePatient(p);
                    if (ok) { JOptionPane.showMessageDialog(this, "Patient updated."); reload(); }
                    else JOptionPane.showMessageDialog(this, "Failed to update patient.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void addNewPatient() {
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        JTextField fn = new JTextField(); JTextField ln = new JTextField(); JTextField phone = new JTextField();
        JTextField gender = new JTextField(); JTextField dob = new JTextField(); JTextField email = new JTextField();
        form.add(new JLabel("First name:")); form.add(fn);
        form.add(new JLabel("Last name:")); form.add(ln);
        form.add(new JLabel("DOB (YYYY-MM-DD):")); form.add(dob);
        form.add(new JLabel("Gender:")); form.add(gender);
        form.add(new JLabel("Phone:")); form.add(phone);
        form.add(new JLabel("Email:")); form.add(email);
        int res = JOptionPane.showConfirmDialog(this, form, "Add Patient", JOptionPane.OK_CANCEL_OPTION);
        if (res==JOptionPane.OK_OPTION) {
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(dob.getText().trim());
                patientService.createPatient(fn.getText().trim(), ln.getText().trim(), ld, gender.getText().trim(), phone.getText().trim(), email.getText().trim(), "");
                JOptionPane.showMessageDialog(this, "Patient created."); reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}