import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    // DB config - change if needed
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 3306;
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root123";
    private static final String DB_NAME = "lost_and_found_db";
    private static final String JDBC_URL_WITHOUT_DB = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DBHelper() { }

    public void initDB() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL_WITHOUT_DB, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DB_NAME + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "name VARCHAR(255) NOT NULL," +
                            "email VARCHAR(255) NOT NULL UNIQUE," +
                            "password VARCHAR(255) NOT NULL," +
                            "contact VARCHAR(50)," +
                            "admission_no VARCHAR(50)" +
                            ") ENGINE=InnoDB"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS admins (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "name VARCHAR(255) NOT NULL," +
                            "email VARCHAR(255) NOT NULL UNIQUE," +
                            "contact VARCHAR(50)" +
                            ") ENGINE=InnoDB"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS items (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "title VARCHAR(255)," +
                            "description TEXT," +
                            "location VARCHAR(255)," +
                            "date DATE," +
                            "status VARCHAR(50)," +
                            "image_path VARCHAR(1024)," +
                            "reported_by INT," +
                            "reporter_contact VARCHAR(50)," +
                            "reporter_admission VARCHAR(50)," +
                            "FOREIGN KEY (reported_by) REFERENCES users(id) ON DELETE SET NULL" +
                            ") ENGINE=InnoDB"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS claims (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "item_id INT," +
                            "user_id INT," +
                            "claim_date DATE," +
                            "status VARCHAR(50)," +
                            "claimant_contact VARCHAR(50)," +
                            "claimant_admission VARCHAR(50)," +
                            "FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE," +
                            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                            ") ENGINE=InnoDB"
            );

            // ensure an admin exists
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM admins");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement ins = conn.prepareStatement("INSERT INTO admins (name,email,contact) VALUES (?,?,?)")) {
                        ins.setString(1, "System Admin");
                        ins.setString(2, "admin@lostandfound.com");
                        ins.setString(3, "12341234");
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    // --- Users ---
    public boolean insertUser(String name, String email, String password, String contact, String admission) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
            try (PreparedStatement ch = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                ch.setString(1, email);
                try (ResultSet rs = ch.executeQuery()) {
                    if (rs.next()) return false;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users (name,email,password,contact,admission_no) VALUES (?,?,?,?,?)")) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, contact);
                ps.setString(5, admission);
                ps.executeUpdate();
            }
        }
        return true;
    }

    public User getUserByEmailAndPassword(String email, String password) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT id,name,email,contact,admission_no FROM users WHERE email = ? AND password = ?")) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("contact"), rs.getString("admission_no"));
                }
            }
        }
        return null;
    }

    public Admin getAdminByEmail(String email) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT id,name,email,contact FROM admins WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Admin(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("contact"));
            }
        }
        return null;
    }

    // --- Items ---
    public void insertItem(String title, String description, String location, String date, String status, String imagePath, int reportedBy, String reporterContact, String reporterAdmission) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO items (title,description,location,date,status,image_path,reported_by,reporter_contact,reporter_admission) VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, location);
            ps.setDate(4, Date.valueOf(LocalDate.parse(date, DATE_FMT)));
            ps.setString(5, status);
            ps.setString(6, imagePath);
            ps.setInt(7, reportedBy);
            ps.setString(8, reporterContact);
            ps.setString(9, reporterAdmission);
            ps.executeUpdate();
        }
    }

    private Item loadItemFromResultSet(ResultSet rs) throws SQLException {
        int itemId = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String location = rs.getString("location");
        Date d = rs.getDate("date");
        String date = d == null ? "" : d.toLocalDate().format(DATE_FMT);
        String status = rs.getString("status");
        String imagePath = rs.getString("image_path");
        int reportedBy = rs.getInt("reported_by");
        String repContact = rs.getString("reporter_contact");
        String repAdmission = rs.getString("reporter_admission");
        String reporterName = null;
        try { reporterName = rs.getString("reporter_name"); } catch (Exception ignored) {}
        return new Item(itemId, title, description, location, date, status, imagePath, reportedBy, repContact, repAdmission, reporterName);
    }

    public List<Item> getItemsByReporter(int reporterId) throws Exception {
        List<Item> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT i.*, u.name AS reporter_name FROM items i LEFT JOIN users u ON i.reported_by = u.id WHERE i.reported_by = ?")) {
            ps.setInt(1, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(loadItemFromResultSet(rs));
            }
        }
        return list;
    }

    public List<Item> getItemsByStatus(String status) throws Exception {
        List<Item> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT i.*, u.name AS reporter_name FROM items i LEFT JOIN users u ON i.reported_by = u.id WHERE i.status = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(loadItemFromResultSet(rs));
            }
        }
        return list;
    }

    public List<Item> getAllItems() throws Exception {
        List<Item> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT i.*, u.name AS reporter_name FROM items i LEFT JOIN users u ON i.reported_by = u.id")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(loadItemFromResultSet(rs));
            }
        }
        return list;
    }

    public Item getItemById(int id) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT i.*, u.name AS reporter_name FROM items i LEFT JOIN users u ON i.reported_by = u.id WHERE i.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return loadItemFromResultSet(rs);
            }
        }
        return null;
    }

    public void updateItemStatus(int itemId, String status) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("UPDATE items SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    // --- Claims ---
    public void insertClaim(int itemId, int userId, String claimDate, String claimantContact, String claimantAdmissionNo) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("INSERT INTO claims (item_id,user_id,claim_date,status,claimant_contact,claimant_admission) VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            ps.setDate(3, Date.valueOf(LocalDate.parse(claimDate, DATE_FMT)));
            ps.setString(4, "Pending");
            ps.setString(5, claimantContact);
            ps.setString(6, claimantAdmissionNo);
            ps.executeUpdate();
        }
    }

    private Claim loadClaimFromResultSet(ResultSet rs) throws SQLException {
        int claimId = rs.getInt("id");
        int itemId = rs.getInt("item_id");
        int userId = rs.getInt("user_id");
        Date d = rs.getDate("claim_date");
        String claimDate = d == null ? "" : d.toLocalDate().format(DATE_FMT);
        String status = rs.getString("status");
        String contact = rs.getString("claimant_contact");
        String admission = rs.getString("claimant_admission");
        return new Claim(claimId, itemId, userId, claimDate, status, contact, admission);
    }

    public List<Claim> getAllClaims() throws Exception {
        List<Claim> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM claims")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(loadClaimFromResultSet(rs));
            }
        }
        return list;
    }

    public Claim getClaimById(int id) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM claims WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return loadClaimFromResultSet(rs);
            }
        }
        return null;
    }

    public List<Claim> getClaimsByUser(int userId) throws Exception {
        List<Claim> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM claims WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(loadClaimFromResultSet(rs));
            }
        }
        return list;
    }

    public void approveClaim(int claimId) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("UPDATE claims SET status = 'Approved' WHERE id = ?")) {
            ps.setInt(1, claimId);
            ps.executeUpdate();
        }
    }

    public void rejectClaim(int claimId) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("UPDATE claims SET status = 'Rejected' WHERE id = ?")) {
            ps.setInt(1, claimId);
            ps.executeUpdate();
        }
    }
}
