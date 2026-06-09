package HospitalManagementSystem;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class HospitalManagementSystem {
    private static final String url = "jdbc:mysql://localhost:3306/hospital?useSSL=false&serverTimezone=UTC";
    private static final String username = "root";
    private static final String password = "Admin@123";

    public static void main(String[] args) {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            return;
        }

        try(Connection connection = DriverManager.getConnection(url, username, password)){
            DatabaseInitializer.initDatabase(connection);
            if(args.length > 0 && args[0].equalsIgnoreCase("gui")){
                SwingUtilities.invokeLater(() -> new HospitalManagementGUI(connection));
                return;
            }

            Scanner scanner = new Scanner(System.in);
            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);
            MedicalRecord medicalRecord = new MedicalRecord(connection, scanner);
            Billing billing = new Billing(connection, scanner);

            while(true){
                System.out.println("HOSPITAL MANAGEMENT SYSTEM");
                System.out.println("1. Add Patient");
                System.out.println("2. View Patients");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. Add Medical Record");
                System.out.println("6. View Medical Records");
                System.out.println("7. Generate Bill");
                System.out.println("8. View Bills");
                System.out.println("9. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch(choice){
                    case 1:
                        patient.addPatient();
                        break;
                    case 2:
                        patient.viewPatients();
                        break;
                    case 3:
                        doctor.viewDoctors();
                        break;
                    case 4:
                        bookAppointment(patient, doctor, connection, scanner);
                        break;
                    case 5:
                        medicalRecord.addMedicalRecord();
                        break;
                    case 6:
                        medicalRecord.viewMedicalRecordsByPatient();
                        break;
                    case 7:
                        billing.generateBill();
                        break;
                    case 8:
                        billing.viewBills();
                        break;
                    case 9:
                        System.out.println("THANK YOU! FOR USING HOSPITAL MANAGEMENT SYSTEM!!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Enter valid choice!!!");
                }
                System.out.println();
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner){
        System.out.print("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        System.out.print("Enter Doctor Id: ");
        int doctorId = scanner.nextInt();
        System.out.print("Enter appointment date (YYYY-MM-DD): ");
        String appointmentDate = scanner.next();
        if(patient.getPatientById(patientId) && doctor.getDoctorById(doctorId)){
            if(checkDoctorAvailability(doctorId, appointmentDate, connection)){
                String appointmentQuery = "INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)";
                try(var preparedStatement = connection.prepareStatement(appointmentQuery)){
                    preparedStatement.setInt(1, patientId);
                    preparedStatement.setInt(2, doctorId);
                    preparedStatement.setString(3, appointmentDate);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if(rowsAffected>0){
                        System.out.println("Appointment Booked!");
                    }else{
                        System.out.println("Failed to Book Appointment!");
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }else{
                System.out.println("Doctor not available on this date!!");
            }
        }else{
            System.out.println("Either doctor or patient doesn't exist!!!");
        }
    }

    public static boolean checkDoctorAvailability(int doctorId, String appointmentDate, Connection connection){
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try(var preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate);
            try(var resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return resultSet.getInt(1) == 0;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
