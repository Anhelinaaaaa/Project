package one;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulerGUI extends JFrame {
    private OptimizedOperationScheduler scheduler;

    public SchedulerGUI() {
        scheduler = new OptimizedOperationScheduler();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Operation Scheduler");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 5));
        addButtons(panel);
        add(new JScrollPane(panel));
        setVisible(true);
    }

    private void addButtons(JPanel panel) {
        panel.add(makeButton("Add Health Professional", e -> addProfessional()));
        panel.add(makeButton("Edit Health Professional", e -> editProfessional()));
        panel.add(makeButton("Delete Health Professional", e -> deleteProfessional()));
        panel.add(makeButton("View Diary", e -> viewDiary()));
        panel.add(makeButton("Add Appointment", e -> showAddAppointmentDialog()));
        panel.add(makeButton("Search & Schedule Appointment", e -> showSearchScheduleDialog()));
        panel.add(makeButton("Save to File", e -> FileManager.save(scheduler)));
        panel.add(makeButton("Load from File", e -> scheduler = FileManager.load()));
        panel.add(makeButton("Undo Last Change", e -> {
            boolean result = scheduler.undo();
            JOptionPane.showMessageDialog(this, result ? "Undo successful" : "Nothing to undo");
        }));
        panel.add(makeButton("View All Professionals", e -> viewAllProfessionals()));
        panel.add(makeButton("Exit", e -> System.exit(0)));
    }

    private JButton makeButton(String title, ActionListener action) {
        JButton btn = new JButton(title);
        btn.addActionListener(action);
        return btn;
    }

    private void addProfessional() {
        JTextField name = new JTextField(), profession = new JTextField(), location = new JTextField();
        Object[] fields = {"Name:", name, "Profession:", profession, "Location:", location};
        int ok = JOptionPane.showConfirmDialog(this, fields, "Add Professional", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            boolean added = scheduler.addHealthProfessional(new HealthProfessional(name.getText(), profession.getText(), location.getText()));
            JOptionPane.showMessageDialog(this, added ? "Added successfully" : "Already exists or invalid");
        }
    }

    private void editProfessional() {
        JTextField oldName = new JTextField(), newName = new JTextField(), newProf = new JTextField(), newLoc = new JTextField();
        Object[] fields = {"Old Name:", oldName, "New Name:", newName, "New Profession:", newProf, "New Location:", newLoc};
        int ok = JOptionPane.showConfirmDialog(this, fields, "Edit Professional", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            boolean updated = scheduler.editHealthProfessional(oldName.getText(), new HealthProfessional(newName.getText(), newProf.getText(), newLoc.getText()));
            JOptionPane.showMessageDialog(this, updated ? "Updated successfully" : "Update failed");
        }
    }

    private void deleteProfessional() {
        String name = JOptionPane.showInputDialog(this, "Enter name to delete:");
        if (name != null) {
            boolean deleted = scheduler.removeHealthProfessional(name);
            JOptionPane.showMessageDialog(this, deleted ? "Deleted" : "Not found");
        }
    }

    private void viewDiary() {
        String name = JOptionPane.showInputDialog(this, "Enter professional name:");
        if (name != null) {
            Diary diary = scheduler.getDiary(name);
            if (diary != null) {
                StringBuilder sb = new StringBuilder();
                for (Appointment app : diary.getAppointments()) sb.append(app).append("\n");
                JOptionPane.showMessageDialog(this, sb.length() > 0 ? sb.toString() : "No appointments");
            } else {
                JOptionPane.showMessageDialog(this, "Professional not found");
            }
        }
    }

    private void viewAllProfessionals() {
        List<HealthProfessional> all = scheduler.getAllProfessionals();
        StringBuilder sb = new StringBuilder();
        all.forEach(p -> sb.append(p).append("\n"));
        JOptionPane.showMessageDialog(this, sb.length() > 0 ? sb.toString() : "No professionals found");
    }

    private void showAddAppointmentDialog() {
        JDialog dialog = new JDialog(this, "Add Appointment", true);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));

        JTextField dateField = new JTextField();
        JTextField startField = new JTextField();
        JTextField endField = new JTextField();
        JTextField typeField = new JTextField();
        JTextField patientIdField = new JTextField();

        List<String> names = scheduler.getAllProfessionals().stream().map(HealthProfessional::getName).toList();
        JList<String> doctorList = new JList<>(names.toArray(String[]::new));
        doctorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane doctorScroll = new JScrollPane(doctorList);
        doctorScroll.setPreferredSize(new Dimension(200, 80));

        form.add(new JLabel("Date (YYYY-MM-DD):")); form.add(dateField);
        form.add(new JLabel("Start Time (HH:MM):")); form.add(startField);
        form.add(new JLabel("End Time (HH:MM):")); form.add(endField);
        form.add(new JLabel("Treatment Type:")); form.add(typeField);
        form.add(new JLabel("Patient ID:")); form.add(patientIdField);
        form.add(new JLabel("Select Professionals:")); form.add(doctorScroll);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setPreferredSize(new Dimension(420, 250));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton submit = new JButton("Add Appointment");
        submit.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                LocalTime start = LocalTime.parse(startField.getText().trim());
                LocalTime end = LocalTime.parse(endField.getText().trim());
                String type = typeField.getText().trim();
                String pid = patientIdField.getText().trim();
                List<String> selected = doctorList.getSelectedValuesList();

                if (selected.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Select at least one doctor.");
                    return;
                }

                TimeSlot slot = new TimeSlot(LocalDateTime.of(date, start), LocalDateTime.of(date, end));
                boolean added = scheduler.scheduleAppointment(slot, pid, type, selected);
                JOptionPane.showMessageDialog(dialog, added ? "Appointment scheduled" : "Conflict or error");
                if (added) dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        dialog.add(submit, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSearchScheduleDialog() {
        JDialog dialog = new JDialog(this, "Search & Schedule", true);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField startDate = new JTextField();
        JTextField endDate = new JTextField();
        JTextField duration = new JTextField();
        JTextField pidField = new JTextField();

        List<String> names = scheduler.getAllProfessionals().stream().map(HealthProfessional::getName).toList();
        JList<String> doctorList = new JList<>(names.toArray(String[]::new));
        doctorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane doctorScroll = new JScrollPane(doctorList);
        doctorScroll.setPreferredSize(new Dimension(200, 80));

        form.add(new JLabel("Start Date (YYYY-MM-DD):")); form.add(startDate);
        form.add(new JLabel("End Date (YYYY-MM-DD):")); form.add(endDate);
        form.add(new JLabel("Duration (minutes):")); form.add(duration);
        form.add(new JLabel("Patient ID:")); form.add(pidField);
        form.add(new JLabel("Select Professionals:")); form.add(doctorScroll);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setPreferredSize(new Dimension(420, 250));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton submit = new JButton("Search");
        submit.addActionListener(e -> {
            try {
                LocalDate start = LocalDate.parse(startDate.getText().trim());
                LocalDate end = LocalDate.parse(endDate.getText().trim());
                Duration dur = Duration.ofMinutes(Long.parseLong(duration.getText().trim()));
                String pid = pidField.getText().trim();
                List<String> selected = doctorList.getSelectedValuesList();
                if (selected.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select professionals.");
                    return;
                }

                List<TimeSlot> slots = scheduler.findAvailableTimeSlots(selected, start, end, dur);
                if (slots.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "No available slots.");
                    return;
                }

                String[] options = slots.stream().map(TimeSlot::toString).toArray(String[]::new);
                String picked = (String) JOptionPane.showInputDialog(dialog, "Select a slot:", "Available Slots",
                        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                if (picked != null) {
                    TimeSlot chosen = slots.get(List.of(options).indexOf(picked));
                    String type = JOptionPane.showInputDialog(dialog, "Enter treatment type:");
                    boolean success = scheduler.scheduleAppointment(chosen, pid, type, selected);
                    JOptionPane.showMessageDialog(dialog, success ? "Booked successfully" : "Booking failed");
                    if (success) dialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        dialog.add(submit, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SchedulerGUI::new);
    }
}

