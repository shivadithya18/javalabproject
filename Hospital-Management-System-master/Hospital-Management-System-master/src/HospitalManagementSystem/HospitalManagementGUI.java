package HospitalManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HospitalManagementGUI extends JFrame {
    private Connection connection;
    private Patient patientService;
    private Doctor doctorService;
    private MedicalRecord medicalRecordService;
    private Billing billingService;

    public HospitalManagementGUI(Connection connection) {
        super("Hospital Management System");
        this.connection = connection;
        Scanner scanner = new Scanner(System.in);
        patientService = new Patient(connection, scanner);
        doctorService = new Doctor(connection);
        medicalRecordService = new MedicalRecord(connection, scanner);
        billingService = new Billing(connection, scanner);
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 460);
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 2, 10, 10));

        buttonPanel.add(createButton("Add Patient", this::onAddPatient));
        buttonPanel.add(createButton("View Patients", e -> showLargeText("Patient List", patientService.getPatientListString())));
        buttonPanel.add(createButton("View Doctors", e -> showLargeText("Doctor List", doctorService.getDoctorListString())));
        buttonPanel.add(createButton("Book Appointment", e -> onBookAppointment()));
        buttonPanel.add(createButton("Add Medical Record", e -> onAddMedicalRecord()));
        buttonPanel.add(createButton("View Medical Records", e -> onViewMedicalRecords()));
        buttonPanel.add(createButton("Generate Bill", e -> onGenerateBill()));
        buttonPanel.add(createButton("View Bills", e -> showLargeText("Bills", billingService.getBillListString())));

        add(buttonPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createButton(String label, ActionListener listener) {
        JButton button = new JButton(label);
        button.addActionListener(listener);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return button;
    }

    private void onAddPatient(ActionEvent event) {
        String name = JOptionPane.showInputDialog(this, "Enter patient name:");
        if(name == null || name.trim().isEmpty()){
            return;
        }
        String ageStr = JOptionPane.showInputDialog(this, "Enter patient age:");
        if(ageStr == null){
            return;
        }
        String gender = JOptionPane.showInputDialog(this, "Enter patient gender:");
        if(gender == null){
            return;
        }
        try{
            int age = Integer.parseInt(ageStr.trim());
            if(patientService.addPatient(name.trim(), age, gender.trim())){
                JOptionPane.showMessageDialog(this, "Patient added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Age must be a number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onBookAppointment(){
        try{
            String patientText = JOptionPane.showInputDialog(this, "Enter patient ID:");
            String doctorText = JOptionPane.showInputDialog(this, "Enter doctor ID:");
            String date = JOptionPane.showInputDialog(this, "Enter appointment date (YYYY-MM-DD):");
            if(patientText == null || doctorText == null || date == null){
                return;
            }
            int patientId = Integer.parseInt(patientText.trim());
            int doctorId = Integer.parseInt(doctorText.trim());
            if(!patientService.getPatientById(patientId) || !doctorService.getDoctorById(doctorId)){
                JOptionPane.showMessageDialog(this, "Patient or doctor not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(HospitalManagementSystem.checkDoctorAvailability(doctorId, date.trim(), connection)){
                String query = "INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)";
                try(var stmt = connection.prepareStatement(query)){
                    stmt.setInt(1, patientId);
                    stmt.setInt(2, doctorId);
                    stmt.setString(3, date.trim());
                    int rows = stmt.executeUpdate();
                    if(rows > 0){
                        JOptionPane.showMessageDialog(this, "Appointment booked successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to book appointment.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(this, "Error booking appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Doctor is not available on that date.", "Unavailable", JOptionPane.WARNING_MESSAGE);
            }
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Please enter valid numeric IDs.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onAddMedicalRecord(){
        try{
            String idText = JOptionPane.showInputDialog(this, "Enter patient ID:");
            if(idText == null){
                return;
            }
            int patientId = Integer.parseInt(idText.trim());
            String diagnosis = JOptionPane.showInputDialog(this, "Enter diagnosis:");
            String prescription = JOptionPane.showInputDialog(this, "Enter prescription:");
            String notes = JOptionPane.showInputDialog(this, "Enter notes:");
            if(diagnosis == null || prescription == null || notes == null){
                return;
            }
            if(medicalRecordService.addMedicalRecord(patientId, diagnosis.trim(), prescription.trim(), notes.trim())){
                JOptionPane.showMessageDialog(this, "Medical record saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save medical record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Please enter a valid patient ID.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onViewMedicalRecords(){
        try{
            String idText = JOptionPane.showInputDialog(this, "Enter patient ID:");
            if(idText == null){
                return;
            }
            int patientId = Integer.parseInt(idText.trim());
            showLargeText("Medical Records", medicalRecordService.getMedicalRecordsForPatient(patientId));
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Please enter a valid patient ID.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onGenerateBill(){
        try{
            String idText = JOptionPane.showInputDialog(this, "Enter patient ID:");
            if(idText == null){
                return;
            }
            int patientId = Integer.parseInt(idText.trim());
            List<BillItem> items = new ArrayList<>();
            while(true){
                String itemName = JOptionPane.showInputDialog(this, "Enter service/item name (or Cancel to finish):");
                if(itemName == null || itemName.trim().isEmpty()){
                    break;
                }
                String costText = JOptionPane.showInputDialog(this, "Enter cost for " + itemName + ":");
                if(costText == null){
                    break;
                }
                double cost = Double.parseDouble(costText.trim());
                items.add(new BillItem(itemName.trim(), cost));
            }
            if(items.isEmpty()){
                JOptionPane.showMessageDialog(this, "No bill items entered.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if(billingService.generateBill(patientId, items)){
                JOptionPane.showMessageDialog(this, "Bill generated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate bill.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showLargeText(String title, String text){
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
