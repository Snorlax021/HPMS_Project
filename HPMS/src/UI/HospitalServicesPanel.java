package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class HospitalServicesPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Color COLOR_BORDER = new Color(210,215,220);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

    private final List<Department> departments = new ArrayList<>();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel detailPanel = new JPanel(new BorderLayout(8,8));

    // detail components
    private final JLabel nameLabel = new JLabel();
    private final JTextArea descriptionArea = new JTextArea();
    private final JLabel contactLabel = new JLabel();
    private final JLabel phoneLabel = new JLabel();
    private final JLabel emailLabel = new JLabel();
    private final JLabel scheduleLabel = new JLabel();
    private final JTextArea notesArea = new JTextArea();

    public HospitalServicesPanel() {
        setLayout(new BorderLayout(8,8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12,12,12,12));

        JLabel header = new JLabel("Hospital Services");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(new Color(60,120,200));

        // Action buttons (moved to top-right) -- will be added to a top bar together with the header
        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topActions.setBackground(Color.WHITE);
        JButton btnEdit = new JButton("Edit");
        JButton btnManageDoctors = new JButton("Manage Doctors");
        JButton btnRefresh = new JButton("Refresh List");
        styleButton(btnEdit); styleButton(btnRefresh); styleButton(btnManageDoctors);
        topActions.add(btnManageDoctors); topActions.add(btnRefresh); topActions.add(btnEdit);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.add(header, BorderLayout.WEST);
        topBar.add(topActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // left buttons column
        buttonsPanel.setLayout(new GridLayout(9, 1, 6, 6));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setBorder(new LineBorder(COLOR_BORDER));

        // right details column
        JPanel rightWrapper = new JPanel(new BorderLayout(8,8));
        rightWrapper.setBackground(Color.WHITE);
        rightWrapper.setBorder(new LineBorder(COLOR_BORDER));

        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.gridy = 0;

        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridwidth = 2; info.add(nameLabel, gbc); gbc.gridy++; gbc.gridwidth = 1;

        descriptionArea.setEditable(false); descriptionArea.setLineWrap(true); descriptionArea.setWrapStyleWord(true); descriptionArea.setBackground(Color.WHITE);
        info.add(new JLabel("Short Description:"), gbc); gbc.gridx = 1; info.add(new JScrollPane(descriptionArea){ { setBorder(null); } }, gbc); gbc.gridx = 0; gbc.gridy++;

        info.add(new JLabel("Contact:"), gbc); gbc.gridx = 1; info.add(contactLabel, gbc); gbc.gridx = 0; gbc.gridy++;
        info.add(new JLabel("Phone:"), gbc); gbc.gridx = 1; info.add(phoneLabel, gbc); gbc.gridx = 0; gbc.gridy++;
        info.add(new JLabel("Email:"), gbc); gbc.gridx = 1; info.add(emailLabel, gbc); gbc.gridx = 0; gbc.gridy++;
        info.add(new JLabel("Schedule:"), gbc); gbc.gridx = 1; info.add(scheduleLabel, gbc); gbc.gridx = 0; gbc.gridy++;

        notesArea.setEditable(false); notesArea.setLineWrap(true); notesArea.setWrapStyleWord(true); notesArea.setBackground(Color.WHITE);
        info.add(new JLabel("Notes / Additional Info:"), gbc); gbc.gridx = 1; info.add(new JScrollPane(notesArea){ { setBorder(null); } }, gbc); gbc.gridx = 0; gbc.gridy++;

        rightWrapper.add(info, BorderLayout.CENTER);

        detailPanel.add(rightWrapper, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonsPanel, detailPanel);
        split.setResizeWeight(0.25);
        add(split, BorderLayout.CENTER);

        // seed and build UI
        seedDepartments();
        buildButtons();

        // default show first
        if (!departments.isEmpty()) showDepartment(departments.get(0));

        // wire the top bar buttons
        btnEdit.addActionListener(e -> {
            Department d = getSelectedDepartment();
            if (d == null) { JOptionPane.showMessageDialog(this, "Select a department from the left to edit."); return; }
            openDepartmentDialog(d);
        });

        btnManageDoctors.addActionListener(e -> openManageDoctorsDialog());

        btnRefresh.addActionListener(e -> buildButtons());
    }

    private void styleButton(JButton b) {
        b.setFont(FONT_NORMAL);
        b.setBackground(Color.WHITE);
        b.setBorder(new LineBorder(COLOR_BORDER));
        b.setFocusPainted(false);
    }

    private Department selectedDept = null;
    private void buildButtons() {
        buttonsPanel.removeAll();
        for (Department d : departments) {
            JButton b = new JButton(d.name);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            styleButton(b);
            b.addActionListener(ev -> { showDepartment(d); });
            buttonsPanel.add(b);
        }
        buttonsPanel.revalidate(); buttonsPanel.repaint();
    }

    private void showDepartment(Department d) {
        selectedDept = d;
        nameLabel.setText(d.name);
        descriptionArea.setText(d.description == null ? "" : d.description);
        contactLabel.setText(d.contactName == null ? "" : d.contactName);
        phoneLabel.setText(d.contactPhone == null ? "" : d.contactPhone);
        emailLabel.setText(d.contactEmail == null ? "" : d.contactEmail);
        scheduleLabel.setText(d.schedule == null ? "" : d.schedule);
        notesArea.setText(d.notes == null ? "" : d.notes);
    }

    private Department getSelectedDepartment() {
        return selectedDept;
    }

    private void openDepartmentDialog(Department existing) {
        boolean isNew = existing == null;
        JTextField nameF = new JTextField(isNew?"":existing.name);
        JTextField descF = new JTextField(isNew?"":existing.description);
        JTextField contactF = new JTextField(isNew?"":existing.contactName);
        JTextField phoneF = new JTextField(isNew?"":existing.contactPhone);
        JTextField emailF = new JTextField(isNew?"":existing.contactEmail);
        JTextField scheduleF = new JTextField(isNew?"":existing.schedule);
        JTextArea notesA = new JTextArea(isNew?"":existing.notes,4,40); notesA.setLineWrap(true); notesA.setWrapStyleWord(true);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;
        p.add(new JLabel("Department Name:"), gbc); gbc.gridx=1; p.add(nameF, gbc); gbc.gridx=0; gbc.gridy++;
        p.add(new JLabel("Short Description:"), gbc); gbc.gridx=1; p.add(descF, gbc); gbc.gridx=0; gbc.gridy++;
        p.add(new JLabel("Contact Name:"), gbc); gbc.gridx=1; p.add(contactF, gbc); gbc.gridx=0; gbc.gridy++;
        p.add(new JLabel("Contact Phone:"), gbc); gbc.gridx=1; p.add(phoneF, gbc); gbc.gridx=0; gbc.gridy++;
        p.add(new JLabel("Contact Email:"), gbc); gbc.gridx=1; p.add(emailF, gbc); gbc.gridx=0; gbc.gridy++;
        p.add(new JLabel("Schedule (Days/Hours or 24/7):"), gbc); gbc.gridx=1; p.add(scheduleF, gbc); gbc.gridx=0; gbc.gridy++;
        p.add(new JLabel("Notes / Additional Info:"), gbc); gbc.gridx=1; p.add(new JScrollPane(notesA), gbc); gbc.gridx=0; gbc.gridy++;

        int res = JOptionPane.showConfirmDialog(this, p, isNew?"Add Department":"Edit Department", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String name = nameF.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Department name is required."); return; }
        if (isNew) {
            Department d = new Department(name, descF.getText().trim(), contactF.getText().trim(), phoneF.getText().trim(), emailF.getText().trim(), scheduleF.getText().trim(), notesA.getText().trim());
            departments.add(d);
        } else {
            existing.name = name;
            existing.description = descF.getText().trim();
            existing.contactName = contactF.getText().trim();
            existing.contactPhone = phoneF.getText().trim();
            existing.contactEmail = emailF.getText().trim();
            existing.schedule = scheduleF.getText().trim();
            existing.notes = notesA.getText().trim();
        }
        buildButtons();
        if (!departments.isEmpty()) showDepartment(departments.get(0));
    }

    // Open a dialog that lists doctors and allows editing their details
    private void openManageDoctorsDialog() {
        Service.DoctorServiceImpl ds = Service.DoctorServiceImpl.getInstance();
        java.util.List<Model.Doctor> list = java.util.List.copyOf(ds.listAll());
        DefaultListModel<String> lm = new DefaultListModel<>();
        for (Model.Doctor d : list) lm.addElement(d.getDoctorId() + " - " + (d.getUser()!=null?d.getUser().getFullName():"(unknown)"));
        JList<String> doctorList = new JList<>(lm);
        doctorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(doctorList); sp.setPreferredSize(new Dimension(600,300));
        JPanel p = new JPanel(new BorderLayout()); p.add(sp, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton editBtn = new JButton("Edit Selected"); JButton closeBtn = new JButton("Close");
        JButton availBtn = new JButton("Set Availability");
        btns.add(availBtn); btns.add(editBtn); btns.add(closeBtn); p.add(btns, BorderLayout.SOUTH);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Manage Doctors", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().add(p); dlg.pack(); dlg.setLocationRelativeTo(this);

        editBtn.addActionListener(ev -> {
            int idx = doctorList.getSelectedIndex(); if (idx<0) { JOptionPane.showMessageDialog(dlg, "Select a doctor to edit."); return; }
            Model.Doctor d = list.get(idx);
            JPanel form = new JPanel(new GridLayout(0,2,8,8));
            JTextField fullname = new JTextField(d.getUser()!=null?d.getUser().getFullName():"");
            JTextField email = new JTextField(d.getUser()!=null?d.getUser().getEmail():"");
            JTextField spec = new JTextField(d.getSpecialization());
            JTextField phone = new JTextField(d.getContactNumber());
            JTextField pic = new JTextField(d.getUser()!=null?d.getUser().getProfilePictureUrl():""); pic.setEditable(false);
            JButton picBtn = new JButton("Choose 2x2"); picBtn.addActionListener(ae->{ JFileChooser fc = new JFileChooser(); if (fc.showOpenDialog(dlg)==JFileChooser.APPROVE_OPTION) pic.setText(fc.getSelectedFile().getAbsolutePath()); });
            form.add(new JLabel("Full name:")); form.add(fullname);
            form.add(new JLabel("Email:")); form.add(email);
            form.add(new JLabel("Specialization:")); form.add(spec);
            form.add(new JLabel("Contact Number:")); form.add(phone);
            form.add(new JLabel("Profile picture:")); form.add(pic); form.add(new JLabel()); form.add(picBtn);
            int res = JOptionPane.showConfirmDialog(dlg, form, "Edit Doctor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res==JOptionPane.OK_OPTION) {
                if (d.getUser()!=null) { d.getUser().setFullName(fullname.getText().trim()); d.getUser().setEmail(email.getText().trim()); d.getUser().setProfilePictureUrl(pic.getText().trim()); }
                d.setSpecialization(spec.getText().trim()); d.setContactNumber(phone.getText().trim());
                ds.update(d);
                lm.set(idx, d.getDoctorId() + " - " + (d.getUser()!=null?d.getUser().getFullName():"(unknown)"));
                JOptionPane.showMessageDialog(dlg, "Doctor updated.");
            }
        });

        // Add weekly availability slot using dropdowns for day and times
        availBtn.addActionListener(ev -> {
            int idx = doctorList.getSelectedIndex(); if (idx<0) { JOptionPane.showMessageDialog(dlg, "Select a doctor first."); return; }
            Model.Doctor d = list.get(idx);
            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;
            // Day dropdown (Mon-Sun)
            JComboBox<java.time.DayOfWeek> dayBox = new JComboBox<>(java.time.DayOfWeek.values());
            // Time dropdowns (30-min increments)
            java.util.List<java.time.LocalTime> times = new java.util.ArrayList<>();
            for (int h=6; h<=22; h++) { times.add(java.time.LocalTime.of(h,0)); times.add(java.time.LocalTime.of(h,30)); }
            JComboBox<java.time.LocalTime> startBox = new JComboBox<>(times.toArray(new java.time.LocalTime[0]));
            JComboBox<java.time.LocalTime> endBox = new JComboBox<>(times.toArray(new java.time.LocalTime[0]));
            // Layout labels and controls
            form.add(new JLabel("Day:"), gbc); gbc.gridx=1; form.add(dayBox, gbc); gbc.gridx=0; gbc.gridy++;
            form.add(new JLabel("Start Time:"), gbc); gbc.gridx=1; form.add(startBox, gbc); gbc.gridx=0; gbc.gridy++;
            form.add(new JLabel("End Time:"), gbc); gbc.gridx=1; form.add(endBox, gbc); gbc.gridx=0; gbc.gridy++;

            int res = JOptionPane.showConfirmDialog(dlg, form, "Add Availability", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                java.time.DayOfWeek day = (java.time.DayOfWeek) dayBox.getSelectedItem();
                java.time.LocalTime start = (java.time.LocalTime) startBox.getSelectedItem();
                java.time.LocalTime end = (java.time.LocalTime) endBox.getSelectedItem();
                if (day == null || start == null || end == null) { JOptionPane.showMessageDialog(dlg, "Please select day and times."); return; }
                if (!end.isAfter(start)) { JOptionPane.showMessageDialog(dlg, "End time must be after start time."); return; }
                // Persist via DoctorScheduleService
                Service.DoctorScheduleService schedSvc = Service.DoctorScheduleService.getInstance();
                try {
                    schedSvc.addSlot(d.getDoctorId(), day, start, end);
                    JOptionPane.showMessageDialog(dlg, "Availability added for " + day + " " + start + " - " + end + ".");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Failed to add availability: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        closeBtn.addActionListener(ev -> dlg.dispose());
        dlg.setVisible(true);
    }

    private void seedDepartments() {
        departments.clear();
        departments.add(new Department("Emergency","Handles urgent and life-threatening cases.","[Doctor Name]","[Phone]","","24/7",""));
        departments.add(new Department("Intensive Care Unit (ICU)","Specialized care for critically ill patients.","[Doctor Name]","[Phone]","","24/7",""));
        departments.add(new Department("Cardiology","Heart care and diagnostics.","[Doctor Name]","[Phone]","","[Days/Hours]",""));
        departments.add(new Department("Neurology","Brain and nervous system care.","[Doctor Name]","[Phone]","","[Days/Hours]",""));
        departments.add(new Department("Pediatrics","Child health services.","[Doctor Name]","[Phone]","","[Days/Hours]",""));
        departments.add(new Department("Obstetrics & Gynecology","Women’s health and childbirth.","[Doctor Name]","[Phone]","","[Days/Hours]",""));
        departments.add(new Department("Orthopedics","Bone and musculoskeletal care.","[Doctor Name]","[Phone]","","[Days/Hours]",""));
        departments.add(new Department("Pharmacy","Dispensing and managing medications.","[Pharmacist Name]","[Phone]","","[Days/Hours]",""));
        departments.add(new Department("Nephrology (Dialysis)","Kidney care and dialysis services.","[Doctor Name]","[Phone]","","[Days/Hours]",""));
    }

    // Simple DTO for this panel
    public static class Department {
        public String name;
        public String description;
        public String contactName;
        public String contactPhone;
        public String contactEmail;
        public String schedule;
        public String notes;
        public Department(String name, String description, String contactName, String contactPhone, String contactEmail, String schedule, String notes) {
            this.name = name; this.description = description; this.contactName = contactName; this.contactPhone = contactPhone; this.contactEmail = contactEmail; this.schedule = schedule; this.notes = notes;
        }
    }
}