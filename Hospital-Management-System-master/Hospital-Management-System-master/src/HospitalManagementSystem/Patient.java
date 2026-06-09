package HospitalManagementSystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Patient {
    private Connection connection;
    private Scanner scanner;

    public Patient(Connection connection, Scanner scanner){
        this.connection = connection;
        this.scanner = scanner;
    }

    public void addPatient(){
        System.out.print("Enter Patient Name: ");
        String name = scanner.next();
        System.out.print("Enter Patient Age: ");
        int age = scanner.nextInt();
        System.out.print("Enter Patient Gender: ");
        String gender = scanner.next();
        addPatient(name, age, gender);
    }

    public boolean addPatient(String name, int age, String gender){
        String query = "INSERT INTO patients(name, age, gender) VALUES(?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            preparedStatement.setString(3, gender);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public void viewPatients(){
        System.out.println(getPatientListString());
    }

    public String getPatientListString(){
        String query = "SELECT * FROM patients";
        StringBuilder output = new StringBuilder();
        try(PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()){
            output.append("Patients:\n");
            output.append("+------------+--------------------+----------+------------+\n");
            output.append("| Patient Id | Name               | Age      | Gender     |\n");
            output.append("+------------+--------------------+----------+------------+\n");
            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String gender = resultSet.getString("gender");
                output.append(String.format("| %-10s | %-18s | %-8s | %-10s |\n", id, name, age, gender));
                output.append("+------------+--------------------+----------+------------+\n");
            }
        }catch (SQLException e){
            e.printStackTrace();
            return "Failed to load patients.";
        }
        return output.length() == 0 ? "No patients found." : output.toString();
    }

    public List<PatientEntity> getAllPatients(){
        List<PatientEntity> list = new ArrayList<>();
        String query = "SELECT * FROM patients";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()){
            while(resultSet.next()){
                list.add(new PatientEntity(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getInt("age"),
                    resultSet.getString("gender")
                ));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public boolean getPatientById(int id){
        String query = "SELECT * FROM patients WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                return resultSet.next();
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
