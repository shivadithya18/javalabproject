package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class MedicalRecord {
    private Connection connection;
    private Scanner scanner;

    public MedicalRecord(Connection connection, Scanner scanner){
        this.connection = connection;
        this.scanner = scanner;
    }

    public void addMedicalRecord(){
        System.out.print("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Diagnosis: ");
        String diagnosis = scanner.nextLine();
        System.out.print("Enter Prescription: ");
        String prescription = scanner.nextLine();
        System.out.print("Enter Notes: ");
        String notes = scanner.nextLine();
        if(addMedicalRecord(patientId, diagnosis, prescription, notes)){
            System.out.println("Medical record saved successfully.");
        } else {
            System.out.println("Failed to save medical record.");
        }
    }

    public boolean addMedicalRecord(int patientId, String diagnosis, String prescription, String notes){
        if(!new Patient(connection, scanner).getPatientById(patientId)){
            return false;
        }
        String query = "INSERT INTO medical_records(patient_id, diagnosis, prescription, notes, record_date) VALUES(?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1, patientId);
            preparedStatement.setString(2, diagnosis);
            preparedStatement.setString(3, prescription);
            preparedStatement.setString(4, notes);
            preparedStatement.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
            return preparedStatement.executeUpdate() > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public void viewMedicalRecordsByPatient(){
        System.out.print("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        System.out.println(getMedicalRecordsForPatient(patientId));
    }

    public String getMedicalRecordsForPatient(int patientId){
        StringBuilder output = new StringBuilder();
        String query = "SELECT id, diagnosis, prescription, notes, record_date FROM medical_records WHERE patient_id = ? ORDER BY record_date DESC";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1, patientId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                output.append("Medical Records for Patient ID: ").append(patientId).append("\n");
                output.append("------------------------------------------------------------\n");
                while(resultSet.next()){
                    output.append("Record ID: ").append(resultSet.getInt("id")).append("\n");
                    output.append("Date: ").append(resultSet.getDate("record_date")).append("\n");
                    output.append("Diagnosis: ").append(resultSet.getString("diagnosis")).append("\n");
                    output.append("Prescription: ").append(resultSet.getString("prescription")).append("\n");
                    output.append("Notes: ").append(resultSet.getString("notes")).append("\n");
                    output.append("------------------------------------------------------------\n");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "Failed to load medical records.";
        }
        return output.length() == 0 ? "No medical records found for this patient." : output.toString();
    }
}
