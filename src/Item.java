public class Item {
    public int itemId;
    public String title, description, location, date, status, imagePath;
    public int reportedBy;
    public String reporterContact, reporterAdmissionNo, reporterName;

    public Item(int itemId, String title, String description, String location, String date, String status, String imagePath, int reportedBy, String reporterContact, String reporterAdmissionNo, String reporterName) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.status = status;
        this.imagePath = imagePath;
        this.reportedBy = reportedBy;
        this.reporterContact = reporterContact;
        this.reporterAdmissionNo = reporterAdmissionNo;
        this.reporterName = reporterName;
    }

    @Override
    public String toString() {
        return "Item ID: " + itemId + " | " + title + " | " + description + " | Location: " + location + " | Date: " + date + " | Status: " + status + " | Reported by: " + (reporterName == null ? reportedBy : reporterName);
    }
}
