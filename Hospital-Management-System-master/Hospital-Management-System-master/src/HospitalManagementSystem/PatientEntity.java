package HospitalManagementSystem;

public class PatientEntity extends Person {
    public PatientEntity(String name, int age, String gender) {
        super(name, age, gender);
    }

    public PatientEntity(int id, String name, int age, String gender) {
        super(id, name, age, gender);
    }
}
