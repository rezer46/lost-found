public class Claim {
    public int claimId;
    public int itemId;
    public int userId;
    public String claimDate;
    public String status;
    public String claimantContact, claimantAdmissionNo;

    public Claim(int claimId, int itemId, int userId, String claimDate, String status, String claimantContact, String claimantAdmissionNo) {
        this.claimId = claimId;
        this.itemId = itemId;
        this.userId = userId;
        this.claimDate = claimDate;
        this.status = status;
        this.claimantContact = claimantContact;
        this.claimantAdmissionNo = claimantAdmissionNo;
    }

    @Override
    public String toString() {
        return "Claim ID: " + claimId + " | Item ID: " + itemId + " | User ID: " + userId + " | Date: " + claimDate + " | Status: " + status;
    }
}
