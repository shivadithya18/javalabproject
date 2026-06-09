package HospitalManagementSystem;

public abstract class Person {
    private int id;
    private String name;
    private int age;
    private String gender;

    public Person(String name, int age, String gender) {
        this(0, name, age, gender);
    }

    public Person(int id, String name, int age, String gender) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }
}
