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
        JButton btnAdd = new JButton("Add Patient");
        JButton btnAddShort = new JButton("ADD");
        JButton btnEdit = new JButton("Edit");
        JButton btnDeactivate = new JButton("Deactivate");
        JButton btnRefresh = new JButton("Refresh");
        actions.add(btnDeactivate); actions.add(btnEdit); actions.add(btnAdd); actions.add(btnAddShort); actions.add(btnRefresh);
        add(actions, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> reload());
        btnDeactivate.addActionListener(e -> deactivateSelected());
        btnEdit.addActionListener(e -> editSelected());
        btnAdd.addActionListener(e -> addNewPatient());
        btnAddShort.addActionListener(e -> addNewPatient());

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
        List<Patient> list = List.copyOf(patientService.listAll());
        for (Patient p : list) {
            model.addRow(new Object[]{p.getId(), (p.getFirstName()==null?"":p.getFirstName()) + " " + (p.getLastName()==null?"":p.getLastName()), p.getDateOfBirth(), p.getGender(), p.getContactNumber()});
        }
    }

    private void showSelectedDetails() {
        int r = table.getSelectedRow(); if (r==-1) return;
        String id = (String) model.getValueAt(r,0);
        patientService.findById(id).ifPresent(p -> {
            detailsPane.removeAll();
            JPanel pinfo = new JPanel(new GridLayout(0,1,6,6)); pinfo.setBorder(new EmptyBorder(8,8,8,8));
            pinfo.add(new JLabel("Name: " + p.getFirstName() + " " + p.getLastName()));
            pinfo.add(new JLabel("DOB: " + p.getDateOfBirth()));
            pinfo.add(new JLabel("Gender: " + p.getGender()));
            pinfo.add(new JLabel("Phone: " + p.getContactNumber()));
            if (p.getUser() != null) pinfo.add(new JLabel("Linked user: " + p.getUser().getUsername()));
            detailsPane.add(pinfo, BorderLayout.CENTER);
            detailsPane.revalidate(); detailsPane.repaint();
        });
    }

    private void deactivateSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        String id = (String) model.getValueAt(r,0);
        int c = JOptionPane.showConfirmDialog(this, "Deactivate this patient account?","Confirm", JOptionPane.YES_NO_OPTION);
        if (c!=JOptionPane.YES_OPTION) return;
        boolean ok = patientService.deletePatient(id); // repo delete; implement soft-deactivate if needed
        if (ok) { JOptionPane.showMessageDialog(this, "Patient removed."); reload(); }
        else JOptionPane.showMessageDialog(this, "Failed to remove patient.");
    }

    private void editSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        String id = (String) model.getValueAt(r,0);
        patientService.findById(id).ifPresent(p -> {
            JPanel form = new JPanel(new GridLayout(0,2,8,8));
            JTextField fn = new JTextField(p.getFirstName());
            JTextField ln = new JTextField(p.getLastName());
            JTextField phone = new JTextField(p.getContactNumber()==null?"":p.getContactNumber());
            form.add(new JLabel("First name:")); form.add(fn);
            form.add(new JLabel("Last name:")); form.add(ln);
            form.add(new JLabel("Phone:")); form.add(phone);
            int res = JOptionPane.showConfirmDialog(this, form, "Edit Patient", JOptionPane.OK_CANCEL_OPTION);
            if (res==JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(this, "Direct edit of Patient fields is not supported. To change details, create a new patient record.");
            }
        });
    }

    private void addNewPatient() {
        // Build a resizable dialog with full form inputs for Patient
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Patient", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12,12,12,12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        JTextField fn = new JTextField(); JTextField ln = new JTextField(); JTextField mn = new JTextField();
        JTextField dob = new JTextField(); JTextField gender = new JTextField();
        JTextField phone = new JTextField(); JTextField email = new JTextField(); JTextField address = new JTextField();

        int row = 0;
        addRow(form, gc, row++, "First name:", fn);
        addRow(form, gc, row++, "Middle name:", mn);
        addRow(form, gc, row++, "Last name:", ln);
        addRow(form, gc, row++, "DOB (YYYY-MM-DD):", dob);
        addRow(form, gc, row++, "Gender:", gender);
        addRow(form, gc, row++, "Phone:", phone);
        addRow(form, gc, row++, "Email:", email);
        addRow(form, gc, row++, "Address:", address);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Register"); JButton cancel = new JButton("Cancel");
        buttons.add(cancel); buttons.add(ok);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(true);

        cancel.addActionListener(e -> dialog.dispose());
        ok.addActionListener(e -> {
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(dob.getText().trim());
                Patient saved = patientService.createPatient(fn.getText().trim(), ln.getText().trim(), ld, gender.getText().trim(), phone.getText().trim(), email.getText().trim(), address.getText().trim());
                // Show provisioned account
                patientService.getProvisionedAccountForPatient(saved.getId()).ifPresentOrElse(acc -> {
                    JOptionPane.showMessageDialog(dialog, "Patient registered.\nUsername: " + acc.username + "\nPassword: " + acc.temporaryPassword, "Account Created", JOptionPane.INFORMATION_MESSAGE);
                }, () -> {
                    JOptionPane.showMessageDialog(dialog, "Patient registered, but account generation failed.", "Warning", JOptionPane.WARNING_MESSAGE);
                });
                dialog.dispose();
                reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 1; form.add(new JLabel(label), gc);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.gridwidth = 1; form.add(field, gc);
    }
}