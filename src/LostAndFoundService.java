import java.util.List;

public class LostAndFoundService {
    private final DBHelper db;

    public LostAndFoundService() {
        db = new DBHelper();
    }

    public void init() throws Exception {
        db.initDB();
    }

    // user/admin wrappers
    public boolean insertUser(String name, String email, String password, String contact, String admission) throws Exception {
        return db.insertUser(name, email, password, contact, admission);
    }

    public User getUserByEmailAndPassword(String email, String password) throws Exception {
        return db.getUserByEmailAndPassword(email, password);
    }

    public Admin getAdminByEmail(String email) throws Exception {
        return db.getAdminByEmail(email);
    }

    // items
    public void insertItem(String title, String description, String location, String date, String status, String imagePath, int reportedBy, String reporterContact, String reporterAdmission) throws Exception {
        db.insertItem(title, description, location, date, status, imagePath, reportedBy, reporterContact, reporterAdmission);
    }

    public Item getItemById(int id) throws Exception {
        return db.getItemById(id);
    }

    public List<Item> getAllItems() throws Exception {
        return db.getAllItems();
    }

    public List<Item> getItemsByStatus(String status) throws Exception {
        return db.getItemsByStatus(status);
    }

    public List<Item> getItemsByReporter(int reporterId) throws Exception {
        return db.getItemsByReporter(reporterId);
    }

    public void updateItemStatus(int itemId, String status) throws Exception {
        db.updateItemStatus(itemId, status);
    }

    // claims
    public void insertClaim(int itemId, int userId, String claimDate, String claimantContact, String claimantAdmissionNo) throws Exception {
        db.insertClaim(itemId, userId, claimDate, claimantContact, claimantAdmissionNo);
    }

    public List<Claim> getAllClaims() throws Exception {
        return db.getAllClaims();
    }

    public Claim getClaimById(int id) throws Exception {
        return db.getClaimById(id);
    }

    public List<Claim> getClaimsByUser(int userId) throws Exception {
        return db.getClaimsByUser(userId);
    }

    public void approveClaim(int id) throws Exception {
        db.approveClaim(id);
    }

    public void rejectClaim(int id) throws Exception {
        db.rejectClaim(id);
    }
}
