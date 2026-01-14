public class User {
    public int id;
    public String name;
    public String email;
    public String contactNumber;
    public String admissionNumber;

    public User(int id, String name, String email, String contactNumber, String admissionNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
        this.admissionNumber = admissionNumber;
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", name=" + name + ", email=" + email + "]";
    }
}
