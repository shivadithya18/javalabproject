package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initDatabase(Connection connection) throws SQLException {
        createPatientsTable(connection);
        createDoctorsTable(connection);
        createAppointmentsTable(connection);
        createMedicalRecordsTable(connection);
        createBillsTable(connection);
        createBillItemsTable(connection);
        seedDoctors(connection);
    }

    private static void createPatientsTable(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS patients ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "name VARCHAR(100) NOT NULL,"
            + "age INT NOT NULL,"
            + "gender VARCHAR(20) NOT NULL)";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.execute();
        }
    }

    private static void createDoctorsTable(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS doctors ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "name VARCHAR(100) NOT NULL,"
            + "age INT NOT NULL,"
            + "gender VARCHAR(20) NOT NULL,"
            + "specialization VARCHAR(100) NOT NULL)";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.execute();
        }
    }

    private static void createAppointmentsTable(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS appointments ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "patient_id INT NOT NULL,"
            + "doctor_id INT NOT NULL,"
            + "appointment_date DATE NOT NULL,"
            + "FOREIGN KEY (patient_id) REFERENCES patients(id),"
            + "FOREIGN KEY (doctor_id) REFERENCES doctors(id))";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.execute();
        }
    }

    private static void createMedicalRecordsTable(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS medical_records ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "patient_id INT NOT NULL,"
            + "diagnosis VARCHAR(255) NOT NULL,"
            + "prescription VARCHAR(255) NOT NULL,"
            + "notes TEXT,"
            + "record_date DATE NOT NULL,"
            + "FOREIGN KEY (patient_id) REFERENCES patients(id))";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.execute();
        }
    }

    private static void createBillsTable(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS bills ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "patient_id INT NOT NULL,"
            + "bill_date DATE NOT NULL,"
            + "total_amount DOUBLE NOT NULL,"
            + "FOREIGN KEY (patient_id) REFERENCES patients(id))";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.execute();
        }
    }

    private static void createBillItemsTable(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS bill_items ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "bill_id INT NOT NULL,"
            + "item_name VARCHAR(255) NOT NULL,"
            + "item_cost DOUBLE NOT NULL,"
            + "FOREIGN KEY (bill_id) REFERENCES bills(id))";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.execute();
        }
    }

    private static void seedDoctors(Connection connection) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM doctors";
        try(PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet resultSet = countStatement.executeQuery()){
            if(resultSet.next() && resultSet.getInt(1) == 0) {
                String insert = "INSERT INTO doctors(name, age, gender, specialization) VALUES(?, ?, ?, ?)";
                try(PreparedStatement insertStatement = connection.prepareStatement(insert)){
                    insertStatement.setString(1, "Dr. Amit Sharma");
                    insertStatement.setInt(2, 45);
                    insertStatement.setString(3, "Male");
                    insertStatement.setString(4, "Cardiology");
                    insertStatement.addBatch();

                    insertStatement.setString(1, "Dr. Sneha Patel");
                    insertStatement.setInt(2, 38);
                    insertStatement.setString(3, "Female");
                    insertStatement.setString(4, "Gynecology");
                    insertStatement.addBatch();

                    insertStatement.setString(1, "Dr. Rahul Verma");
                    insertStatement.setInt(2, 50);
                    insertStatement.setString(3, "Male");
                    insertStatement.setString(4, "Orthopedics");
                    insertStatement.addBatch();

                    insertStatement.executeBatch();
                }
            }
        }
    }
}
