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
        JButton btnAdd = new JButton("Add Staff");
        JButton btnEdit = new JButton("Edit");
        JButton btnDeactivate = new JButton("Deactivate");
        JButton btnRefresh = new JButton("Refresh");
        actions.add(btnDeactivate); actions.add(btnEdit); actions.add(btnAdd); actions.add(btnRefresh);
        add(actions, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> reload());
        btnDeactivate.addActionListener(e -> deactivateSelected());
        btnEdit.addActionListener(e -> editSelected());
        btnAdd.addActionListener(e -> addNewStaff());

        table.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ if (e.getClickCount()==1) showSelectedDetails(); }});

        reload();
    }

    private void reload() {
        // expose public reload if needed
    }
    public void reloadPanel() {
         model.setRowCount(0);
         List<User> all = userService.getAllUsers();
         for (User u : all) {
             if (u.getRole() == Role.STAFF) {
                 model.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getStatus()});
             }
         }
     }

     private void showSelectedDetails() {
         int r = table.getSelectedRow(); if (r==-1) return;
         String id = (String) model.getValueAt(r,0);
         userService.findById(id).ifPresent(u -> {
             detailsPane.removeAll();
             JPanel p = new JPanel(new BorderLayout(8,8)); p.setBorder(new EmptyBorder(8,8,8,8));
             JPanel left = new JPanel(new BorderLayout());
             JLabel pic = new JLabel(); pic.setPreferredSize(new Dimension(160,160));
             if (u.getProfilePictureUrl()!=null && !u.getProfilePictureUrl().isBlank()) { ImageIcon ic = new ImageIcon(u.getProfilePictureUrl()); Image im = ic.getImage().getScaledInstance(160,160,Image.SCALE_SMOOTH); pic.setIcon(new ImageIcon(im)); }
             else { pic.setIcon(new ImageIcon(new java.awt.image.BufferedImage(160,160,java.awt.image.BufferedImage.TYPE_INT_ARGB))); pic.setText("No image"); pic.setHorizontalTextPosition(SwingConstants.CENTER); pic.setVerticalTextPosition(SwingConstants.CENTER); }
             left.add(pic, BorderLayout.NORTH);
             JPanel meta = new JPanel(new GridLayout(0,1,6,6));
             meta.add(new JLabel("Username: " + u.getUsername()));
             meta.add(new JLabel("Full name: " + u.getFullName()));
             meta.add(new JLabel("Email: " + u.getEmail()));
             meta.add(new JLabel("Role: " + u.getRole()));
             meta.add(new JLabel("Status: " + u.getStatus()));
             Service.PatientService.PatientProfile prof = Service.PatientService.getInstance().getProfileByUsername(u.getUsername());
             meta.add(new JLabel("Contact: " + (prof==null?"":prof.phone)));
             meta.add(new JLabel("Emergency: " + (prof==null?"":(prof.emergencyContactName + " " + prof.emergencyContactNumber))));
             left.add(meta, BorderLayout.CENTER);
             p.add(left, BorderLayout.CENTER);
             detailsPane.add(p, BorderLayout.CENTER);
             detailsPane.revalidate(); detailsPane.repaint();
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