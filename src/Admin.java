public class Admin {
    public int id;
    public String name;
    public String email;
    public String contactNumber;

    public Admin(int id, String name, String email, String contactNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    @Override
    public String toString() {
        return "Admin[id=" + id + ", name=" + name + ", email=" + email + "]";
    }
}
