package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import Service.UserService;
import Model.User;
import Model.Role;

/**
 * Simple Staff Management panel: lists staff users and shows details with edit/deactivate.
 * The add/edit form uses the same minimal fields used in User Management.
 */
public class StaffManagementPanel extends JPanel {
    private final UserService userService = UserService.getInstance();

    private JTable table;
    private DefaultTableModel model;
    private JPanel detailsPane;

    public StaffManagementPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));

        JLabel header = new JLabel("Staff Management", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID","Username","Full Name","Email","Status"}, 0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(600,300));

        detailsPane = new JPanel(new BorderLayout());
        detailsPane.setBorder(new LineBorder(Color.LIGHT_GRAY));
        detailsPane.setPreferredSize(new Dimension(420,300));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, detailsPane);
        split.setResizeWeight(0.65);
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

    // Implement reload() so external callers that call doctorPanel.reload() also work for staff
    public void reload() {
        reloadPanel();
    }

    public void reloadPanel() {
         model.setRowCount(0);
         List<User> all = userService.getAllUsers();
         for (User u : all) {
             if (u.getRole() == Role.STAFF) {
                 // Show ST-ID formatted staff number if present; otherwise fall back to internal ID
                 String displayId = (u.getStaffNumber() != null && !u.getStaffNumber().isBlank()) ? u.getStaffNumber() : u.getId();
                 model.addRow(new Object[]{displayId, u.getUsername(), u.getFullName(), u.getEmail(), u.getStatus()});
             }
         }
     }

     private void showSelectedDetails() {
         int r = table.getSelectedRow();
         if (r == -1) return;
         int modelRow = table.convertRowIndexToModel(r);
         String username = (String) model.getValueAt(modelRow, 1);
         userService.findByUsername(username).ifPresent(u -> {
             detailsPane.removeAll();
             JPanel root = new JPanel(new BorderLayout(8,8));
             root.setBorder(new EmptyBorder(10,10,10,10));

             JPanel top = new JPanel(new BorderLayout(12,12));
             JPanel left = new JPanel(new BorderLayout());
             JLabel pic = new JLabel();
             pic.setPreferredSize(new Dimension(180,180));
             if (u.getProfilePictureUrl()!=null && !u.getProfilePictureUrl().isBlank()) {
                 ImageIcon ic = new ImageIcon(u.getProfilePictureUrl());
                 Image im = ic.getImage().getScaledInstance(180,180,Image.SCALE_SMOOTH);
                 pic.setIcon(new ImageIcon(im));
             } else {
                 pic.setIcon(new ImageIcon(new java.awt.image.BufferedImage(180,180,java.awt.image.BufferedImage.TYPE_INT_ARGB)));
                 pic.setText("No image");
                 pic.setHorizontalTextPosition(SwingConstants.CENTER);
                 pic.setVerticalTextPosition(SwingConstants.CENTER);
             }
             left.add(pic, BorderLayout.NORTH);

             JPanel meta = new JPanel(new GridLayout(0,1,6,6));
             meta.add(new JLabel("Staff ID: " + (u.getStaffNumber()!=null?u.getStaffNumber():"(not assigned)")));
             meta.add(new JLabel("Username: " + u.getUsername()));
             meta.add(new JLabel("Full name: " + (u.getFullName()==null?"":u.getFullName())));
             meta.add(new JLabel("Email: " + (u.getEmail()==null?"":u.getEmail())));
             meta.add(new JLabel("Role: " + u.getRole()));
             meta.add(new JLabel("Status: " + u.getStatus()));
             Service.PatientService.PatientProfile prof = Service.PatientService.getInstance().getProfileByUsername(u.getUsername());
             if (prof != null) {
                 meta.add(new JLabel("Contact: " + (prof.phone==null?"":prof.phone)));
                 meta.add(new JLabel("Address: " + (prof.address==null?"":prof.address)));
                 meta.add(new JLabel("Emergency Contact: " + ((prof.emergencyContactName==null?"":prof.emergencyContactName) + " " + (prof.emergencyContactNumber==null?"":prof.emergencyContactNumber))));
             }
             left.add(meta, BorderLayout.CENTER);

             JTextArea notes = new JTextArea();
             notes.setEditable(false);
             notes.setLineWrap(true);
             notes.setWrapStyleWord(true);
             notes.setText("Staff details are view-only.\n\nUse Hospital Services > Manage Doctors for doctor updates; staff editing is disabled for admin.");
             top.add(left, BorderLayout.WEST);
             top.add(new JScrollPane(notes), BorderLayout.CENTER);

             JPanel bottom = new JPanel(new BorderLayout());
             bottom.setBorder(BorderFactory.createTitledBorder("Summary"));
             DefaultListModel<String> lm = new DefaultListModel<>();
             lm.addElement("Last updated: " + java.time.Instant.ofEpochMilli(java.lang.System.currentTimeMillis()).toString());
             lm.addElement("Role: " + u.getRole() + ", Status: " + u.getStatus());
             JList<String> summary = new JList<>(lm);
             bottom.add(new JScrollPane(summary), BorderLayout.CENTER);

             root.add(top, BorderLayout.CENTER);
             root.add(bottom, BorderLayout.SOUTH);
             detailsPane.add(root, BorderLayout.CENTER);
             detailsPane.revalidate();
             detailsPane.repaint();
         });
     }

    private void deactivateSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a staff first."); return; }
        String id = (String) model.getValueAt(r,0);
        int c = JOptionPane.showConfirmDialog(this, "Deactivate this staff account?","Confirm", JOptionPane.YES_NO_OPTION);
        if (c!=JOptionPane.YES_OPTION) return;
        boolean ok = userService.deactivateById(id);
        if (ok) { JOptionPane.showMessageDialog(this, "Staff deactivated."); reload(); }
        else JOptionPane.showMessageDialog(this, "Failed to deactivate staff.");
    }

    private void editSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a staff first."); return; }
        String id = (String) model.getValueAt(r,0);
        userService.findById(id).ifPresent(u -> {
            JPanel p = new JPanel(new GridLayout(0,2,8,8));
            JTextField full = new JTextField(u.getFullName());
            JTextField email = new JTextField(u.getEmail());
            JTextField contact = new JTextField(); JTextField emergencyName = new JTextField(); JTextField emergencyPhone = new JTextField();
            Service.PatientService.PatientProfile prof = Service.PatientService.getInstance().getProfileByUsername(u.getUsername());
            if (prof!=null) { contact.setText(prof.phone); emergencyName.setText(prof.emergencyContactName); emergencyPhone.setText(prof.emergencyContactNumber); }
            p.add(new JLabel("Full name:")); p.add(full);
            p.add(new JLabel("Email:")); p.add(email);
            p.add(new JLabel("Contact:")); p.add(contact);
            p.add(new JLabel("Emergency Name:")); p.add(emergencyName);
            p.add(new JLabel("Emergency Phone:")); p.add(emergencyPhone);
            int res = JOptionPane.showConfirmDialog(this, p, "Edit Staff", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res==JOptionPane.OK_OPTION) {
                u.setFullName(full.getText().trim()); u.setEmail(email.getText().trim());
                Service.PatientService.PatientProfile np = Service.PatientService.getInstance().getProfileByUsername(u.getUsername());
                np.phone = contact.getText().trim(); np.emergencyContactName = emergencyName.getText().trim(); np.emergencyContactNumber = emergencyPhone.getText().trim();
                Service.PatientService.getInstance().saveProfile(u.getUsername(), np);
                JOptionPane.showMessageDialog(this, "Staff updated."); reload();
            }
        });
    }

    private void addNewStaff() {
        // Copy same inputs as user management: Username + Password + Role selection (Staff), Full name, Email
        JPanel p = new JPanel(new GridLayout(0,2,8,8));
        JTextField username = new JTextField(); JPasswordField pw = new JPasswordField();
        JTextField fullname = new JTextField(); JTextField email = new JTextField();
        p.add(new JLabel("Username:")); p.add(username);
        p.add(new JLabel("Password:")); p.add(pw);
        p.add(new JLabel("Full name:")); p.add(fullname);
        p.add(new JLabel("Email:")); p.add(email);
        int res = JOptionPane.showConfirmDialog(this, p, "Add New Staff", JOptionPane.OK_CANCEL_OPTION);
        if (res==JOptionPane.OK_OPTION) {
            try {
                userService.createUser(username.getText().trim(), pw.getPassword(), Role.STAFF);
                userService.findByUsername(username.getText().trim()).ifPresent(u -> {
                    u.setFullName(fullname.getText().trim()); u.setEmail(email.getText().trim());
                    Service.PatientService.PatientProfile prof = Service.PatientService.getInstance().getProfileByUsername(u.getUsername());
                    prof.phone = ""; prof.emergencyContactName = ""; prof.emergencyContactNumber = ""; Service.PatientService.getInstance().saveProfile(u.getUsername(), prof);
                });
                JOptionPane.showMessageDialog(this, "Staff created."); reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to create staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
