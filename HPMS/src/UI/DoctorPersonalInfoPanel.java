package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Arrays;

import Model.Doctor;
import Service.DoctorServiceImpl;
import Service.AppointmentService;
import Service.PatientService;

/**
 * Personal information panel for a doctor user. This is a trimmed/duplicated
 * version of DoctorManagementPanel that shows only the currently-logged-in
 * doctor's details (profile, bio, upcoming appointments, availability).
 */
public class DoctorPersonalInfoPanel extends JPanel {
    private final DoctorServiceImpl doctorService = DoctorServiceImpl.getInstance();
    private final AppointmentService appointmentService = AppointmentService.getInstance();
    private final PatientService patientService = PatientService.getInstance();

    private final String username; // doctor's username
    private Doctor currentDoctor;

    private JPanel detailsPane;

    public DoctorPersonalInfoPanel(String username) {
        this.username = username;
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(12,12,12,12));

        JLabel header = new JLabel("Personal Information", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        detailsPane = new JPanel(new BorderLayout());
        detailsPane.setBorder(new LineBorder(Color.LIGHT_GRAY));
        detailsPane.setPreferredSize(new Dimension(820, 420));

        // Provide a refresh, edit and reset-password action bar (right-bottom aligned)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEdit = new JButton("Edit");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnResetPassword = new JButton("Reset Password");
        actions.add(btnEdit);
        actions.add(btnRefresh);
        actions.add(btnResetPassword);

        btnRefresh.addActionListener(e -> reload());
        btnEdit.addActionListener(e -> editProfile());
        btnResetPassword.addActionListener(e -> openResetPasswordDialog());

        add(detailsPane, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        reload();
    }

    private void reload() {
        detailsPane.removeAll();
        // find doctor by username
        currentDoctor = doctorService.listAll().stream().filter(d -> d.getUser()!=null && d.getUser().getUsername()!=null && d.getUser().getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
        if (currentDoctor == null) {
            detailsPane.add(new JLabel("No doctor record found for user: " + username), BorderLayout.CENTER);
            detailsPane.revalidate(); detailsPane.repaint();
            return;
        }

        JPanel top = new JPanel(new BorderLayout(8,8));
        JPanel left = new JPanel(new BorderLayout());
        JLabel pic = new JLabel(); pic.setPreferredSize(new Dimension(180,180));
        String picUrl = currentDoctor.getUser()!=null?currentDoctor.getUser().getProfilePictureUrl():null;
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
        String full = currentDoctor.getUser()!=null?currentDoctor.getUser().getFullName():"(unknown)";
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
        meta.add(new JLabel("License: " + (currentDoctor.getLicenseNumber()==null?"(n/a)":currentDoctor.getLicenseNumber())));
        meta.add(new JLabel("Specialty: " + (currentDoctor.getSpecialization()==null?"(n/a)":currentDoctor.getSpecialization())));
        meta.add(new JLabel("Contact: " + (currentDoctor.getContactNumber()==null?"(n/a)":currentDoctor.getContactNumber())));
        meta.add(new JLabel("Email: " + (currentDoctor.getUser()!=null?currentDoctor.getUser().getEmail():"(n/a)")));
        Service.PatientService.PatientProfile dp = Service.PatientService.getInstance().getProfileByUsername(currentDoctor.getUser()!=null?currentDoctor.getUser().getUsername():"");
        meta.add(new JLabel("Emergency Contact: " + (dp==null?"":(dp.emergencyContactName + " " + dp.emergencyContactNumber))));
        meta.add(new JLabel("Status: " + (currentDoctor.getStatus()==null?"(n/a)":currentDoctor.getStatus().name())));

        long patients = appointmentService.listAll().stream().filter(a -> a.getDoctorId()!=null && a.getDoctorId().equals(currentDoctor.getDoctorId()) && a.getAppointmentStatus()!=null && a.getAppointmentStatus().name().equals("PENDING")).count();
        meta.add(new JLabel("Current patients: " + patients));

        left.add(meta, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        JTextArea bio = new JTextArea(); bio.setEditable(false); bio.setLineWrap(true); bio.setWrapStyleWord(true);
        bio.setText(currentDoctor.getBiography()==null?"(no biography)":currentDoctor.getBiography());
        right.add(new JScrollPane(bio), BorderLayout.CENTER);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.CENTER);

        // Schedule summary (upcoming appts)
        JPanel sched = new JPanel(new BorderLayout());
        sched.setBorder(BorderFactory.createTitledBorder("Upcoming Appointments"));
        DefaultListModel<String> lm = new DefaultListModel<>();
        appointmentService.listAll().stream().filter(a->a.getDoctorId()!=null && a.getDoctorId().equals(currentDoctor.getDoctorId()) && a.getAppointmentStatus()!=null && a.getAppointmentStatus().name().equals("PENDING")).sorted((a,b)->a.getScheduledAt().compareTo(b.getScheduledAt())).limit(10).forEach(a-> {
            patientService.getPatientSummaryById(a.getPatientId()).ifPresent(p-> lm.addElement(p.getFullName() + " — " + a.getScheduledAt().toString()));
        });
        JList<String> list = new JList<>(lm);
        sched.add(new JScrollPane(list), BorderLayout.CENTER);

        // Availability
        JPanel avail = new JPanel(new BorderLayout());
        avail.setBorder(BorderFactory.createTitledBorder("Availability"));
        DefaultListModel<String> availModel = new DefaultListModel<>();
        Service.DoctorScheduleService.getInstance().listByDoctorId(currentDoctor.getDoctorId()).stream().sorted((s1,s2)-> {
            int c = s1.getDayOfWeek().compareTo(s2.getDayOfWeek()); if (c!=0) return c; return s1.getTimeStart().compareTo(s2.getTimeStart());
        }).forEach(s -> availModel.addElement(s.getDayOfWeek().name() + " " + s.getTimeStart().toString() + " - " + s.getTimeEnd().toString()));
        JList<String> availList = new JList<>(availModel);
        avail.add(new JScrollPane(availList), BorderLayout.CENTER);

        detailsPane.add(top, BorderLayout.CENTER);
        JPanel south = new JPanel(new GridLayout(2,1)); south.add(avail); south.add(sched);
        detailsPane.add(south, BorderLayout.SOUTH);

        detailsPane.revalidate(); detailsPane.repaint();
    }

    private void editProfile() {
        if (currentDoctor == null) { JOptionPane.showMessageDialog(this, "No doctor record loaded."); return; }
        Doctor d = currentDoctor;

        // Load existing profile stored in PatientService (used by admin registration UI)
        Service.PatientService.PatientProfile profile = Service.PatientService.getInstance().getProfileByUsername(d.getUser()!=null?d.getUser().getUsername():"");

        // --- Fields mirrored from Admin add-user form ---
        // Personal
        JTextField surnameField = new JTextField(profile.surname == null ? "" : profile.surname);
        JTextField givenNameField = new JTextField(profile.firstName == null ? "" : profile.firstName);
        JTextField middleField = new JTextField(profile.middleName == null ? "" : profile.middleName);
        JTextField dobField = new JTextField(profile.dateOfBirth == null ? "" : profile.dateOfBirth);
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male","Female","Other"});
        if (profile.gender != null && !profile.gender.isBlank()) genderBox.setSelectedItem(profile.gender);
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel( parseIntSafe(profile.age==null?"0":profile.age), 0, 150, 1));
        JTextField nationalityField = new JTextField(profile.nationality == null ? "" : profile.nationality);

        // Contact
        JTextField emailField = new JTextField(d.getUser()!=null?d.getUser().getEmail(): (profile.email==null?"":profile.email));
        JTextField contactNumField = new JTextField(d.getContactNumber()==null?profile.phone: d.getContactNumber());
        JTextField addressField = new JTextField(profile.address == null ? "" : profile.address);
        JTextField emergencyNameField = new JTextField(profile.emergencyContactName == null ? "" : profile.emergencyContactName);
        JTextField emergencyContactField = new JTextField(profile.emergencyContactNumber == null ? "" : profile.emergencyContactNumber);

        // Professional
        JComboBox<String> titleCombo = new JComboBox<>(new String[]{"Doctor","Physician","Specialist","Surgeon"});
        JTextField specialityField = new JTextField(d.getSpecialization()==null?profile.doctor: d.getSpecialization());
        JSpinner yearsField = new JSpinner(new SpinnerNumberModel(1, 0, 80, 1));
        JTextField licenseField = new JTextField(d.getLicenseNumber()==null?profile.idNumber:d.getLicenseNumber());
        JTextField prcExpiryField = new JTextField(profile.insuranceExpiry==null?"":profile.insuranceExpiry);
        JTextField hospitalAffilField = new JTextField(profile.employerName==null?"":profile.employerName);

        // Identification / documents
        JTextField idNumberField = new JTextField(profile.idNumber==null?"":profile.idNumber);
        JCheckBox minorCheck = new JCheckBox("Minor (under 18)");
        JTextField studentIdField = new JTextField(profile.idNumber==null?"":profile.idNumber);
        studentIdField.setEnabled(minorCheck.isSelected());
        minorCheck.addActionListener(ae -> studentIdField.setEnabled(minorCheck.isSelected()));

        // System / picture
        JTextField usernameField = new JTextField(d.getUser()!=null?d.getUser().getUsername():""); usernameField.setEditable(false);
        JPasswordField passwordField = new JPasswordField(); passwordField.setEditable(false); passwordField.setToolTipText("Password shown only on admin creation");
        JTextField picField = new JTextField(d.getUser()!=null?d.getUser().getProfilePictureUrl(): (profile.twoByTwoPath==null?"":profile.twoByTwoPath)); picField.setEditable(false);
        JButton picBtn = new JButton("Choose 2x2"); picBtn.addActionListener(e->{ JFileChooser fc=new JFileChooser(); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) picField.setText(fc.getSelectedFile().getAbsolutePath()); });

        // Schedule editor (preload existing slots)
        JComboBox<java.time.DayOfWeek> dayBox = new JComboBox<>(java.time.DayOfWeek.values());
        java.util.List<java.time.LocalTime> timeOptions = new java.util.ArrayList<>();
        for (int hh = 6; hh <= 22; hh++) { timeOptions.add(java.time.LocalTime.of(hh, 0)); timeOptions.add(java.time.LocalTime.of(hh, 30)); }
        JComboBox<java.time.LocalTime> startBox = new JComboBox<>(timeOptions.toArray(new java.time.LocalTime[0]));
        JComboBox<java.time.LocalTime> endBox = new JComboBox<>(timeOptions.toArray(new java.time.LocalTime[0]));
        DefaultListModel<String> scheduleListModel = new DefaultListModel<>();
        java.util.List<Object[]> pendingSlots = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter TIME_FMT = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
        Service.DoctorScheduleService.getInstance().listByDoctorId(d.getDoctorId()).forEach(s -> { pendingSlots.add(new Object[]{s.getDayOfWeek(), s.getTimeStart(), s.getTimeEnd()}); scheduleListModel.addElement(s.getDayOfWeek().name() + " " + s.getTimeStart().format(TIME_FMT) + " - " + s.getTimeEnd().format(TIME_FMT)); });
        JList<String> scheduleList = new JList<>(scheduleListModel);
        JButton addSlotBtn = new JButton("Add Slot"); addSlotBtn.addActionListener(ae -> {
            java.time.DayOfWeek day = (java.time.DayOfWeek) dayBox.getSelectedItem(); java.time.LocalTime st = (java.time.LocalTime) startBox.getSelectedItem(); java.time.LocalTime en = (java.time.LocalTime) endBox.getSelectedItem();
            if (day==null||st==null||en==null) { JOptionPane.showMessageDialog(this, "Select day/start/end"); return; }
            if (!en.isAfter(st)) { JOptionPane.showMessageDialog(this, "End must be after start"); return; }
            scheduleListModel.addElement(day.name() + " " + st.format(TIME_FMT) + " - " + en.format(TIME_FMT)); pendingSlots.add(new Object[]{day, st, en});
        });
        JButton removeSlotBtn = new JButton("Remove Selected"); removeSlotBtn.addActionListener(ae -> { int i = scheduleList.getSelectedIndex(); if (i<0) return; scheduleListModel.remove(i); pendingSlots.remove(i); });

        // Build panels similar to admin form, but compact
        JPanel container = new JPanel(new BorderLayout(12,12)); container.setBorder(new EmptyBorder(8,8,8,8));
        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JPanel personal = new JPanel(new GridLayout(0,2,6,6)); personal.setBorder(BorderFactory.createTitledBorder("Personal"));
        personal.add(new JLabel("Surname:")); personal.add(surnameField); personal.add(new JLabel("Given name:")); personal.add(givenNameField); personal.add(new JLabel("Middle name:")); personal.add(middleField);
        personal.add(new JLabel("DOB (YYYY-MM-DD):")); personal.add(dobField); personal.add(new JLabel("Gender:")); personal.add(genderBox); personal.add(new JLabel("Age:")); personal.add(ageSpinner);

        JPanel contact = new JPanel(new GridLayout(0,2,6,6)); contact.setBorder(BorderFactory.createTitledBorder("Contact"));
        contact.add(new JLabel("Email:")); contact.add(emailField); contact.add(new JLabel("Contact Number:")); contact.add(contactNumField); contact.add(new JLabel("Address:")); contact.add(addressField);
        contact.add(new JLabel("Emergency Name:")); contact.add(emergencyNameField); contact.add(new JLabel("Emergency Number:")); contact.add(emergencyContactField);

        left.add(personal); left.add(Box.createVerticalStrut(8)); left.add(contact);

        JPanel prof = new JPanel(new GridLayout(0,2,6,6)); prof.setBorder(BorderFactory.createTitledBorder("Professional"));
        prof.add(new JLabel("Title:")); prof.add(titleCombo); prof.add(new JLabel("Specialty:")); prof.add(specialityField); prof.add(new JLabel("Years:")); prof.add(yearsField);
        prof.add(new JLabel("License:")); prof.add(licenseField); prof.add(new JLabel("PRC Expiry:")); prof.add(prcExpiryField); prof.add(new JLabel("Hospital Affiliation:")); prof.add(hospitalAffilField);

        JPanel idBlock = new JPanel(new GridLayout(0,2,6,6)); idBlock.setBorder(BorderFactory.createTitledBorder("Identification")); idBlock.add(new JLabel("ID Number:")); idBlock.add(idNumberField); idBlock.add(minorCheck); idBlock.add(studentIdField);

        JPanel sys = new JPanel(new GridLayout(0,2,6,6)); sys.setBorder(BorderFactory.createTitledBorder("Account / Photo")); sys.add(new JLabel("Username:")); sys.add(usernameField); sys.add(new JLabel("Profile picture:")); sys.add(picField); sys.add(new JLabel()); sys.add(picBtn);

        right.add(prof); right.add(Box.createVerticalStrut(8)); right.add(idBlock); right.add(Box.createVerticalStrut(8)); right.add(sys);

        JPanel schedPanel = new JPanel(new BorderLayout()); schedPanel.setBorder(BorderFactory.createTitledBorder("Weekly Schedule")); JPanel schedCtrl = new JPanel(new FlowLayout(FlowLayout.LEFT)); schedCtrl.add(new JLabel("Day:")); schedCtrl.add(dayBox); schedCtrl.add(new JLabel("Start:")); schedCtrl.add(startBox); schedCtrl.add(new JLabel("End:")); schedCtrl.add(endBox); schedCtrl.add(addSlotBtn); schedCtrl.add(removeSlotBtn); schedPanel.add(schedCtrl, BorderLayout.NORTH); schedPanel.add(new JScrollPane(scheduleList), BorderLayout.CENTER);

        container.add(left, BorderLayout.WEST); container.add(right, BorderLayout.CENTER); container.add(schedPanel, BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(this, container, "Edit Personal Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        // On save: persist to Doctor and PatientService.PatientProfile and schedule
        try {
            // Update profile object
            Service.PatientService.PatientProfile np = new Service.PatientService.PatientProfile();
            np.surname = surnameField.getText().trim();
            np.firstName = givenNameField.getText().trim();
            np.middleName = middleField.getText().trim();
            np.dateOfBirth = dobField.getText().trim();
            np.gender = (String) genderBox.getSelectedItem();
            np.age = String.valueOf(((Number) ageSpinner.getValue()).intValue());
            np.nationality = nationalityField.getText().trim();
            np.email = emailField.getText().trim();
            np.phone = contactNumField.getText().trim();
            np.address = addressField.getText().trim();
            np.emergencyContactName = emergencyNameField.getText().trim();
            np.emergencyContactNumber = emergencyContactField.getText().trim();
            np.idNumber = idNumberField.getText().trim();
            np.twoByTwoPath = picField.getText().trim();
            np.doctor = specialityField.getText().trim();
            np.employerName = hospitalAffilField.getText().trim();
            np.insuranceExpiry = prcExpiryField.getText().trim();

            // Save profile to PatientService keyed by username
            String uname = d.getUser()!=null?d.getUser().getUsername():"";
            if (uname != null && !uname.isBlank()) {
                Service.PatientService.getInstance().saveProfile(uname, np);
            }

            // Update Doctor and User fields
            if (d.getUser() != null) {
                String fullname = (np.surname + " " + np.firstName + " " + np.middleName).trim();
                if (!fullname.isBlank()) d.getUser().setFullName(fullname);
                d.getUser().setEmail(np.email);
                d.getUser().setProfilePictureUrl(np.twoByTwoPath);
            }
            d.setSpecialization(np.doctor);
            d.setContactNumber(np.phone);
            d.setLicenseNumber(licenseField.getText().trim());
            doctorService.update(d);

            // Replace schedule
            Service.DoctorScheduleService ds = Service.DoctorScheduleService.getInstance();
            java.util.List<Model.DoctorSchedule> existing = ds.listByDoctorId(d.getDoctorId());
            for (Model.DoctorSchedule s : existing) { try { ds.delete(s.getScheduleId()); } catch (Exception ignored) {} }
            for (Object[] o : pendingSlots) {
                java.time.DayOfWeek day = (java.time.DayOfWeek) o[0]; java.time.LocalTime st = (java.time.LocalTime) o[1]; java.time.LocalTime en = (java.time.LocalTime) o[2];
                try { ds.addSlot(d.getDoctorId(), day, st, en); } catch (Exception ignored) {}
            }

            JOptionPane.showMessageDialog(this, "Personal details updated.");
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Opens a small dialog to let the logged-in doctor change their password.
    private void openResetPasswordDialog() {
        if (currentDoctor == null || currentDoctor.getUser() == null || currentDoctor.getUser().getUsername() == null || currentDoctor.getUser().getUsername().isBlank()) {
            JOptionPane.showMessageDialog(this, "No account available to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JPasswordField currentField = new JPasswordField();
        JPasswordField newField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JPanel pnl = new JPanel(new GridLayout(0,2,8,8));
        pnl.add(new JLabel("Current password:")); pnl.add(currentField);
        pnl.add(new JLabel("New password:")); pnl.add(newField);
        pnl.add(new JLabel("Confirm new password:")); pnl.add(confirmField);

        int res = JOptionPane.showConfirmDialog(this, pnl, "Reset Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            // clear sensitive fields
            Arrays.fill(currentField.getPassword(), '\0');
            Arrays.fill(newField.getPassword(), '\0');
            Arrays.fill(confirmField.getPassword(), '\0');
            return;
        }

        char[] currentPwd = currentField.getPassword();
        char[] newPwd = newField.getPassword();
        char[] confirmPwd = confirmField.getPassword();

        try {
            if (!java.util.Arrays.equals(newPwd, confirmPwd)) {
                JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String uname = currentDoctor.getUser().getUsername();
            boolean ok = Service.UserService.getInstance().changePassword(uname, currentPwd, newPwd);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password changed successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Password not accepted: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to change password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Clear sensitive data
            if (currentPwd != null) java.util.Arrays.fill(currentPwd, '\0');
            if (newPwd != null) java.util.Arrays.fill(newPwd, '\0');
            if (confirmPwd != null) java.util.Arrays.fill(confirmPwd, '\0');
        }
    }

    // small helper used by the edit dialog to safely parse integers
    private int parseIntSafe(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}