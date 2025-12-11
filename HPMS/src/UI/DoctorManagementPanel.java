package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import Model.Doctor;
import Service.DoctorServiceImpl;
import Service.AppointmentService;
import Service.PatientService;

/**
 * Reusable panel showing doctor list and selected doctor details (picture, license, counts, schedule).
 */
public class DoctorManagementPanel extends JPanel {
    private final DoctorServiceImpl doctorService = DoctorServiceImpl.getInstance();
    private final AppointmentService appointmentService = AppointmentService.getInstance();
    private final PatientService patientService = PatientService.getInstance();

    private JTable table;
    private DefaultTableModel model;
    private JPanel detailsPane;

    public DoctorManagementPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));

        JLabel header = new JLabel("Doctor Management", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Doctor ID","Name","Specialty","Status","# Patients"},0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(600, 300));

        detailsPane = new JPanel(new BorderLayout());
        detailsPane.setBorder(new LineBorder(Color.LIGHT_GRAY));
        detailsPane.setPreferredSize(new Dimension(420, 300));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, detailsPane);
        split.setResizeWeight(0.65);
        add(split, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnDeactivate = new JButton("Deactivate");
        actions.add(btnDeactivate); actions.add(btnRefresh);
        add(actions, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> reload());
        btnDeactivate.addActionListener(e -> deactivateSelected());

        table.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ if (e.getClickCount()==1) showSelectedDetails(); }});

        reload();
    }

    public void reload() {
        model.setRowCount(0);
        List<Doctor> list = List.copyOf(doctorService.listAll());
        for (Doctor d : list) {
            String name = d.getUser()!=null?d.getUser().getFullName():"(unknown)";
            long patients = appointmentService.listAll().stream().filter(a -> a.getDoctorId()!=null && a.getDoctorId().equals(d.getDoctorId()) && a.getAppointmentStatus()!=null && a.getAppointmentStatus().name().equals("PENDING")).count();
            model.addRow(new Object[]{d.getDoctorId(), name, d.getSpecialization(), d.getStatus(), patients});
        }
    }

    private void showSelectedDetails() {
        int r = table.getSelectedRow();
        if (r==-1) return;
        String id = (String) model.getValueAt(r,0);
        doctorService.findByDoctorId(id).ifPresent(d -> {
            detailsPane.removeAll();
            JPanel top = new JPanel(new BorderLayout(8,8));
            JPanel left = new JPanel(new BorderLayout());
            JLabel pic = new JLabel(); pic.setPreferredSize(new Dimension(180,180));
            String picUrl = d.getUser()!=null?d.getUser().getProfilePictureUrl():null;
            if (picUrl!=null && !picUrl.isBlank()) {
                ImageIcon ic = new ImageIcon(picUrl);
                Image im = ic.getImage().getScaledInstance(180,180,Image.SCALE_SMOOTH);
                pic.setIcon(new ImageIcon(im));
            } else {
                pic.setIcon(new ImageIcon(new BufferedImage(180,180,BufferedImage.TYPE_INT_ARGB)));
                pic.setText("No image"); pic.setHorizontalTextPosition(SwingConstants.CENTER); pic.setVerticalTextPosition(SwingConstants.CENTER);
            }
            left.add(pic, BorderLayout.NORTH);

            JPanel meta = new JPanel(new GridLayout(0,1,4,4));
            // Expanded profile: show full name broken into parts, contact, email, emergency contact (if present in PatientProfile)
            String full = d.getUser()!=null?d.getUser().getFullName():"(unknown)";
            meta.add(new JLabel("Full name: " + full));
            String sur = "", given = "", mid = "";
            if (full != null && full.contains(" ")) {
                String[] parts = full.split(" ");
                sur = parts.length>0?parts[0]:"";
                given = parts.length>1?parts[1]:"";
                if (parts.length>2) mid = parts[2];
            }
            meta.add(new JLabel("Surname: " + sur));
            meta.add(new JLabel("Given name: " + given));
            meta.add(new JLabel("Middle name: " + mid));
            meta.add(new JLabel("License: " + (d.getLicenseNumber()==null?"(n/a)":d.getLicenseNumber())));
            meta.add(new JLabel("Specialty: " + (d.getSpecialization()==null?"(n/a)":d.getSpecialization())));
            meta.add(new JLabel("Contact: " + (d.getContactNumber()==null?"(n/a)":d.getContactNumber())));
            meta.add(new JLabel("Email: " + (d.getUser()!=null?d.getUser().getEmail():"(n/a)")));
            Service.PatientService.PatientProfile dp = Service.PatientService.getInstance().getProfileByUsername(d.getUser()!=null?d.getUser().getUsername():"");
            meta.add(new JLabel("Emergency Contact: " + (dp==null?"":(dp.emergencyContactName + " " + dp.emergencyContactNumber))));
            meta.add(new JLabel("Status: " + (d.getStatus()==null?"(n/a)":d.getStatus().name())));

            long patients = appointmentService.listAll().stream().filter(a -> a.getDoctorId()!=null && a.getDoctorId().equals(d.getDoctorId()) && a.getAppointmentStatus()!=null && a.getAppointmentStatus().name().equals("PENDING")).count();
            meta.add(new JLabel("Current patients: " + patients));

            left.add(meta, BorderLayout.CENTER);

            JPanel right = new JPanel(new BorderLayout());
            JTextArea bio = new JTextArea(); bio.setEditable(false); bio.setLineWrap(true); bio.setWrapStyleWord(true);
            bio.setText(d.getBiography()==null?"(no biography)":d.getBiography());
            right.add(new JScrollPane(bio), BorderLayout.CENTER);

            top.add(left, BorderLayout.WEST);
            top.add(right, BorderLayout.CENTER);

            // Schedule summary (simple list of upcoming appts)
            JPanel sched = new JPanel(new BorderLayout());
            sched.setBorder(BorderFactory.createTitledBorder("Upcoming Appointments"));
            DefaultListModel<String> lm = new DefaultListModel<>();
            appointmentService.listAll().stream().filter(a->a.getDoctorId()!=null && a.getDoctorId().equals(d.getDoctorId()) && a.getAppointmentStatus()!=null && a.getAppointmentStatus().name().equals("PENDING")).sorted((a,b)->a.getScheduledAt().compareTo(b.getScheduledAt())).limit(10).forEach(a-> {
                patientService.getPatientSummaryById(a.getPatientId()).ifPresent(p-> lm.addElement(p.getFullName() + " — " + a.getScheduledAt().toString()));
            });
            JList<String> list = new JList<>(lm);
            sched.add(new JScrollPane(list), BorderLayout.CENTER);

            // Availability summary (from DoctorScheduleService)
            JPanel avail = new JPanel(new BorderLayout());
            avail.setBorder(BorderFactory.createTitledBorder("Availability"));
            DefaultListModel<String> availModel = new DefaultListModel<>();
            Service.DoctorScheduleService.getInstance().listByDoctorId(d.getDoctorId()).stream().sorted((s1,s2)-> {
                int c = s1.getDayOfWeek().compareTo(s2.getDayOfWeek()); if (c!=0) return c;
                return s1.getTimeStart().compareTo(s2.getTimeStart());
            }).forEach(s -> availModel.addElement(s.getDayOfWeek().name() + " " + s.getTimeStart().toString() + " - " + s.getTimeEnd().toString()));
            JList<String> availList = new JList<>(availModel);
            avail.add(new JScrollPane(availList), BorderLayout.CENTER);

            detailsPane.add(top, BorderLayout.CENTER);
            // show availability above appointments in the south area
            JPanel south = new JPanel(new GridLayout(2,1)); south.add(avail); south.add(sched);
            detailsPane.add(south, BorderLayout.SOUTH);
            detailsPane.revalidate(); detailsPane.repaint();
        });
    }

    private void deactivateSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a doctor first."); return; }
        String id = (String) model.getValueAt(r,0);
        int c = JOptionPane.showConfirmDialog(this, "Deactivate this doctor? They will be moved to Deactivated Accounts.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c!=JOptionPane.YES_OPTION) return;
        boolean ok = doctorService.deactivateByDoctorId(id);
        if (ok) { JOptionPane.showMessageDialog(this, "Doctor deactivated."); reload(); }
        else JOptionPane.showMessageDialog(this, "Failed to deactivate doctor.");
    }

    private void editSelected() {
        int r = table.getSelectedRow(); if (r==-1) { JOptionPane.showMessageDialog(this, "Select a doctor first."); return; }
        String id = (String) model.getValueAt(r,0);
        doctorService.findByDoctorId(id).ifPresent(d -> {
            // Full edit dialog: allow editing user (full name, email, picture) and doctor fields
            JPanel p = new JPanel(new GridLayout(0,2,8,8));
            JTextField fullname = new JTextField(d.getUser()!=null?d.getUser().getFullName():"");
            JTextField email = new JTextField(d.getUser()!=null?d.getUser().getEmail():"");
            JTextField spec = new JTextField(d.getSpecialization());
            JTextField phone = new JTextField(d.getContactNumber());
            JTextField pic = new JTextField(d.getUser()!=null?d.getUser().getProfilePictureUrl():""); pic.setEditable(false);
            JButton picBtn = new JButton("Choose 2x2"); picBtn.addActionListener(e->{ JFileChooser fc=new JFileChooser(); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) pic.setText(fc.getSelectedFile().getAbsolutePath()); });
            p.add(new JLabel("Full name:")); p.add(fullname);
            p.add(new JLabel("Email:")); p.add(email);
            p.add(new JLabel("Specialization:")); p.add(spec);
            p.add(new JLabel("Contact Number:")); p.add(phone);
            p.add(new JLabel("Profile picture:")); p.add(pic); p.add(new JLabel()); p.add(picBtn);
            int res = JOptionPane.showConfirmDialog(this, p, "Edit Doctor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res==JOptionPane.OK_OPTION) {
                if (d.getUser()!=null) { d.getUser().setFullName(fullname.getText().trim()); d.getUser().setEmail(email.getText().trim()); d.getUser().setProfilePictureUrl(pic.getText().trim()); }
                d.setSpecialization(spec.getText().trim()); d.setContactNumber(phone.getText().trim());
                doctorService.update(d);
                JOptionPane.showMessageDialog(this, "Doctor updated."); reload();
            }
         });
     }
 }