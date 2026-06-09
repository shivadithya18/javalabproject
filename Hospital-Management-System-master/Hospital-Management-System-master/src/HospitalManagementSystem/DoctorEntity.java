package HospitalManagementSystem;

public class DoctorEntity extends Person {
    private String specialization;

    public DoctorEntity(int id, String name, int age, String gender, String specialization) {
        super(id, name, age, gender);
        this.specialization = specialization;
    }

    public String getSpecialization() {
        return specialization;
    }
}
