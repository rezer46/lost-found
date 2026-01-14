import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LostAndFoundGUI {
    private final LostAndFoundService service;
    private final DateTimeFormatter DATE_FMT = DBHelper.DATE_FMT;

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cards;

    // Login/Register fields
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JTextField regNameField, regEmailField, regContactField, regAdmissionField;
    private JPasswordField regPasswordField;

    private User currentUser = null;
    private Admin currentAdmin = null;

    private DefaultTableModel itemsTableModel;
    private DefaultTableModel claimsTableModel;

    public LostAndFoundGUI(LostAndFoundService service) {
        this.service = service;
    }

    public void start() {
        frame = new JFrame("Lost & Found System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        cards.add(mainMenuPanel(), "MAIN");
        cards.add(registerPanel(), "REGISTER");
        cards.add(userDashboardPanel(), "USER_DASH");
        cards.add(adminDashboardPanel(), "ADMIN_DASH");
        cards.add(browsePanel(), "BROWSE");

        frame.add(cards);
        frame.setVisible(true);

        showMain();
    }

    private void showMain() {
        cardLayout.show(cards, "MAIN");
    }

    private JPanel mainMenuPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Lost & Found System");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10,10,10,10);
        center.add(title, gbc);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(5,5,5,5);
        f.gridx = 0; f.gridy = 0;
        form.add(new JLabel("Email:"), f);
        f.gridx = 1;
        loginEmailField = new JTextField(20);
        form.add(loginEmailField, f);

        f.gridx = 0; f.gridy = 1;
        form.add(new JLabel("Password:"), f);
        f.gridx = 1;
        loginPasswordField = new JPasswordField(20);
        form.add(loginPasswordField, f);

        f.gridx = 0; f.gridy = 2; f.gridwidth = 2;
        JButton loginBtn = new JButton("User Login");
        loginBtn.addActionListener(e -> attemptUserLogin());
        form.add(loginBtn, f);

        f.gridy = 3;
        JButton adminLoginBtn = new JButton("Admin Login (email only)");
        adminLoginBtn.addActionListener(e -> attemptAdminLogin());
        form.add(adminLoginBtn, f);

        f.gridy = 4;
        JButton gotoRegister = new JButton("User Registration");
        gotoRegister.addActionListener(ev -> cardLayout.show(cards, "REGISTER"));
        form.add(gotoRegister, f);

        f.gridy = 5;
        JButton browseBtn = new JButton("Browse Items (guest)");
        browseBtn.addActionListener(ev -> {
            refreshItemsTable();
            cardLayout.show(cards, "BROWSE");
        });
        form.add(browseBtn, f);

        gbc.gridy = 1;
        center.add(form, gbc);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel registerPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(6,6,6,6);
        f.gridx = 0; f.gridy = 0;
        p.add(new JLabel("Name:"), f);
        f.gridx = 1; regNameField = new JTextField(20); p.add(regNameField, f);

        f.gridx = 0; f.gridy = 1; p.add(new JLabel("Email:"), f);
        f.gridx = 1; regEmailField = new JTextField(20); p.add(regEmailField, f);

        f.gridx = 0; f.gridy = 2; p.add(new JLabel("Password:"), f);
        f.gridx = 1; regPasswordField = new JPasswordField(20); p.add(regPasswordField, f);

        f.gridx = 0; f.gridy = 3; p.add(new JLabel("Contact Number:"), f);
        f.gridx = 1; regContactField = new JTextField(15); p.add(regContactField, f);

        f.gridx = 0; f.gridy = 4; p.add(new JLabel("Admission No.:"), f);
        f.gridx = 1; regAdmissionField = new JTextField(15); p.add(regAdmissionField, f);

        f.gridx = 0; f.gridy = 5; f.gridwidth = 2;
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> {
            String name = regNameField.getText().trim();
            String email = regEmailField.getText().trim();
            String pass = new String(regPasswordField.getPassword()).trim();
            String contact = regContactField.getText().trim();
            String admission = regAdmissionField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter name, email and password.");
                return;
            }
            try {
                if (service.insertUser(name, email, pass, contact, admission)) {
                    JOptionPane.showMessageDialog(frame, "Registration successful. You can login now.");
                    clearRegisterForm();
                    cardLayout.show(cards, "MAIN");
                } else {
                    JOptionPane.showMessageDialog(frame, "Registration failed: Email may already exist.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });
        p.add(registerBtn, f);

        f.gridy = 6;
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(ev -> cardLayout.show(cards, "MAIN"));
        p.add(backBtn, f);

        return p;
    }

    private JPanel userDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel();
        lbl.setName("userGreeting");
        top.add(lbl);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        top.add(logoutBtn);
        p.add(top, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JButton reportLostBtn = new JButton("Report Lost Item");
        JButton reportFoundBtn = new JButton("Report Found Item");
        JButton myItemsBtn = new JButton("View My Items");
        JButton claimFoundBtn = new JButton("Claim Found Item");
        JButton myClaimsBtn = new JButton("View My Claims");
        JButton browseBtn = new JButton("Browse All Items");

        reportLostBtn.addActionListener(e -> showReportDialog("Lost"));
        reportFoundBtn.addActionListener(e -> showReportDialog("Found"));
        myItemsBtn.addActionListener(e -> showMyItemsDialog());
        claimFoundBtn.addActionListener(e -> showClaimDialog());
        myClaimsBtn.addActionListener(e -> showMyClaimsDialog());
        browseBtn.addActionListener(e -> {
            refreshItemsTable();
            cardLayout.show(cards, "BROWSE");
        });

        left.add(reportLostBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(reportFoundBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(myItemsBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(claimFoundBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(myClaimsBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(browseBtn);

        p.add(left, BorderLayout.WEST);

        JTextArea help = new JTextArea("Use the left-side buttons to operate. Browse shows full items list.");
        help.setEditable(false);
        p.add(help, BorderLayout.CENTER);

        return p;
    }

    private JPanel adminDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel();
        lbl.setName("adminGreeting");
        top.add(lbl);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        top.add(logoutBtn);
        p.add(top, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JButton viewItemsBtn = new JButton("View All Items");
        JButton viewClaimsBtn = new JButton("View All Claims");
        JButton processClaimsBtn = new JButton("Process Pending Claims");
        JButton updateStatusBtn = new JButton("Update Item Status");

        viewItemsBtn.addActionListener(e -> {
            refreshItemsTable();
            cardLayout.show(cards, "BROWSE");
        });
        viewClaimsBtn.addActionListener(e -> {
            refreshClaimsTable();
            showClaimsDialog(false);
        });
        processClaimsBtn.addActionListener(e -> {
            refreshClaimsTable();
            showClaimsDialog(true);
        });
        updateStatusBtn.addActionListener(e -> showUpdateStatusDialog());

        left.add(viewItemsBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(viewClaimsBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(processClaimsBtn);
        left.add(Box.createRigidArea(new Dimension(0,6)));
        left.add(updateStatusBtn);

        p.add(left, BorderLayout.WEST);

        JTextArea help = new JTextArea("Admin actions: view/process claims and update item statuses.");
        help.setEditable(false);
        p.add(help, BorderLayout.CENTER);

        return p;
    }

    private JPanel browsePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(ev -> {
            if (currentAdmin != null) cardLayout.show(cards, "ADMIN_DASH");
            else if (currentUser != null) cardLayout.show(cards, "USER_DASH");
            else cardLayout.show(cards, "MAIN");
        });
        top.add(backBtn);
        p.add(top, BorderLayout.NORTH);

        itemsTableModel = new DefaultTableModel();
        JTable itemsTable = new JTable(itemsTableModel);
        JScrollPane scp = new JScrollPane(itemsTable);
        p.add(scp, BorderLayout.CENTER);

        itemsTableModel.setColumnIdentifiers(new Object[]{"Item ID","Title","Description","Location","Date","Status","Reported By","Reporter Contact","Reporter Admission","Image Path"});

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshItemsTable());
        bottom.add(refreshBtn);

        JButton claimBtn = new JButton("Claim Selected (user only)");
        claimBtn.addActionListener(e -> {
            int sel = itemsTable.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(frame,"Select a row first."); return; }
            int itemId = Integer.parseInt(itemsTableModel.getValueAt(sel,0).toString());
            if (currentUser == null) { JOptionPane.showMessageDialog(frame, "You must be logged in as a user to claim."); return; }
            try {
                Item item = service.getItemById(itemId);
                if (!"Found".equalsIgnoreCase(item.status)) { JOptionPane.showMessageDialog(frame, "Only items with status 'Found' can be claimed."); return; }
                service.insertClaim(itemId, currentUser.id, LocalDate.now().format(DATE_FMT), currentUser.contactNumber, currentUser.admissionNumber);
                service.updateItemStatus(itemId, "Lost");
                JOptionPane.showMessageDialog(frame, "Claim submitted successfully.");
                refreshItemsTable();
                refreshClaimsTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });
        bottom.add(claimBtn);

        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // ---------- Actions & dialogs ----------
    private void attemptUserLogin() {
        String email = loginEmailField.getText().trim();
        String pass = new String(loginPasswordField.getPassword()).trim();
        if (email.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(frame,"Enter email and password."); return; }
        try {
            User u = service.getUserByEmailAndPassword(email, pass);
            if (u == null) { JOptionPane.showMessageDialog(frame,"Invalid email or password."); return; }
            currentUser = u;
            currentAdmin = null;
            updateUserGreeting();
            cardLayout.show(cards, "USER_DASH");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,"Error: " + ex.getMessage());
        }
    }

    private void attemptAdminLogin() {
        String email = loginEmailField.getText().trim();
        if (email.isEmpty()) { JOptionPane.showMessageDialog(frame,"Enter admin email (no password required)."); return; }
        try {
            Admin a = service.getAdminByEmail(email);
            if (a == null) { JOptionPane.showMessageDialog(frame,"Invalid admin email."); return; }
            currentAdmin = a;
            currentUser = null;
            updateAdminGreeting();
            cardLayout.show(cards, "ADMIN_DASH");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,"Error: " + ex.getMessage());
        }
    }

    private void clearRegisterForm() {
        regNameField.setText("");
        regEmailField.setText("");
        regPasswordField.setText("");
        regContactField.setText("");
        regAdmissionField.setText("");
    }

    private void logout() {
        currentUser = null;
        currentAdmin = null;
        loginEmailField.setText("");
        loginPasswordField.setText("");
        cardLayout.show(cards, "MAIN");
    }

    private void updateUserGreeting() {
        for (Component c : cards.getComponents()) {
            if (c instanceof JPanel) {
                for (Component d : ((JPanel)c).getComponents()) {
                    if (d instanceof JPanel) {
                        for (Component e : ((JPanel)d).getComponents()) {
                            if (e instanceof JLabel && "userGreeting".equals(e.getName())) {
                                ((JLabel)e).setText("User: " + currentUser.name + "  (Email: " + currentUser.email + ")");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateAdminGreeting() {
        for (Component c : cards.getComponents()) {
            if (c instanceof JPanel) {
                for (Component d : ((JPanel)c).getComponents()) {
                    if (d instanceof JPanel) {
                        for (Component e : ((JPanel)d).getComponents()) {
                            if (e instanceof JLabel && "adminGreeting".equals(e.getName())) {
                                ((JLabel)e).setText("Admin: " + currentAdmin.name + "  (Email: " + currentAdmin.email + ")");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void showReportDialog(String type) {
        JTextField titleField = new JTextField(20);
        JTextField descField = new JTextField(30);
        JTextField locationField = new JTextField(20);
        JTextField dateField = new JTextField(10);
        JTextField imagePathField = new JTextField(30);

        dateField.setText(LocalDate.now().format(DATE_FMT));

        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel("Title:")); panel.add(titleField);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Location:")); panel.add(locationField);
        panel.add(new JLabel("Date (yyyy-MM-dd):")); panel.add(dateField);
        panel.add(new JLabel("Image path (optional):")); panel.add(imagePathField);

        int res = JOptionPane.showConfirmDialog(frame, panel, "Report " + type + " Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                String desc = descField.getText().trim();
                String location = locationField.getText().trim();
                String date = dateField.getText().trim();
                if (date.isEmpty()) date = LocalDate.now().format(DATE_FMT);
                String imagePath = imagePathField.getText().trim();

                service.insertItem(title, desc, location, date, type, imagePath, currentUser.id, currentUser.contactNumber, currentUser.admissionNumber);
                JOptionPane.showMessageDialog(frame, type + " item reported successfully.");
                refreshItemsTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        }
    }

    private void showMyItemsDialog() {
        try {
            List<Item> myItems = service.getItemsByReporter(currentUser.id);
            JTextArea ta = new JTextArea();
            ta.setEditable(false);
            StringBuilder sb = new StringBuilder();
            for (Item it : myItems) sb.append(it).append("\n");
            if (sb.length()==0) sb.append("No items reported by you.");
            ta.setText(sb.toString());
            JOptionPane.showMessageDialog(frame, new JScrollPane(ta), "My Reported Items", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    private void showClaimDialog() {
        try {
            List<Item> found = service.getItemsByStatus("Found");
            if (found.isEmpty()) { JOptionPane.showMessageDialog(frame,"No 'Found' items available."); return; }
            StringBuilder sb = new StringBuilder();
            for (Item it : found) sb.append(it).append("\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            int res = JOptionPane.showConfirmDialog(frame, new JScrollPane(ta), "Found Items - select an item ID to claim (enter below)", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            String input = JOptionPane.showInputDialog(frame, "Enter Item ID to claim:");
            if (input == null) return;
            int itemId = Integer.parseInt(input.trim());

            Item itemToClaim = service.getItemById(itemId);
            if (itemToClaim == null || !"Found".equalsIgnoreCase(itemToClaim.status)) {
                JOptionPane.showMessageDialog(frame,"Invalid selection or status changed.");
                return;
            }
            service.insertClaim(itemId, currentUser.id, LocalDate.now().format(DATE_FMT), currentUser.contactNumber, currentUser.admissionNumber);
            service.updateItemStatus(itemId, "Lost");
            JOptionPane.showMessageDialog(frame,"Claim submitted.");
            refreshItemsTable();
            refreshClaimsTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,"Error: " + ex.getMessage());
        }
    }

    private void showMyClaimsDialog() {
        try {
            java.util.List<Claim> claims = service.getClaimsByUser(currentUser.id);
            if (claims.isEmpty()) { JOptionPane.showMessageDialog(frame,"No claims found."); return; }
            StringBuilder sb = new StringBuilder();
            for (Claim c : claims) sb.append(c).append("\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            JOptionPane.showMessageDialog(frame, new JScrollPane(ta), "My Claims", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,"Error: " + ex.getMessage());
        }
    }

    private void showClaimsDialog(boolean allowProcess) {
        try {
            java.util.List<Claim> claims = service.getAllClaims();
            if (claims.isEmpty()) { JOptionPane.showMessageDialog(frame,"No claims found."); return; }
            StringBuilder sb = new StringBuilder();
            for (Claim c : claims) sb.append(c).append("\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            int res = JOptionPane.showConfirmDialog(frame, new JScrollPane(ta), "All Claims", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            if (!allowProcess) return;
            String s = JOptionPane.showInputDialog(frame, "Enter Claim ID to process (or cancel):");
            if (s == null) return;
            int cid = Integer.parseInt(s.trim());
            Claim chosen = service.getClaimById(cid);
            if (chosen == null) { JOptionPane.showMessageDialog(frame,"Claim not found."); return; }
            String[] opts = {"Approve","Reject","Cancel"};
            int pick = JOptionPane.showOptionDialog(frame, "Approve or Reject?", "Process Claim", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
            if (pick == 0) { service.approveClaim(cid); service.updateItemStatus(chosen.itemId, "Claimed"); JOptionPane.showMessageDialog(frame,"Claim approved."); }
            else if (pick == 1) { service.rejectClaim(cid); service.updateItemStatus(chosen.itemId, "Found"); JOptionPane.showMessageDialog(frame,"Claim rejected."); }
            refreshClaimsTable();
            refreshItemsTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,"Error: " + ex.getMessage());
        }
    }

    private void showUpdateStatusDialog() {
        try {
            java.util.List<Item> all = service.getAllItems();
            StringBuilder sb = new StringBuilder();
            for (Item it : all) sb.append(it).append("\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            int res = JOptionPane.showConfirmDialog(frame, new JScrollPane(ta), "All Items - choose one to update", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            String sid = JOptionPane.showInputDialog(frame, "Enter Item ID to update:");
            if (sid == null) return;
            int itemId = Integer.parseInt(sid.trim());
            String[] statuses = {"Lost","Found","Claimed"};
            String chosen = (String) JOptionPane.showInputDialog(frame, "Choose status:", "Update Status", JOptionPane.PLAIN_MESSAGE, null, statuses, statuses[0]);
            if (chosen == null) return;
            service.updateItemStatus(itemId, chosen);
            JOptionPane.showMessageDialog(frame,"Status updated.");
            refreshItemsTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,"Error: " + ex.getMessage());
        }
    }

    private void refreshItemsTable() {
        try {
            java.util.List<Item> items = service.getAllItems();
            itemsTableModel.setRowCount(0);
            for (Item it : items) {
                itemsTableModel.addRow(new Object[]{
                        it.itemId, it.title, it.description, it.location, it.date, it.status,
                        it.reporterName == null ? it.reportedBy : it.reporterName, it.reporterContact, it.reporterAdmissionNo, it.imagePath
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshClaimsTable() {
        try {
            java.util.List<Claim> claims = service.getAllClaims();
            if (claimsTableModel == null) {
                claimsTableModel = new DefaultTableModel(new Object[]{"Claim ID","Item ID","User ID","Date","Status","Claimant Contact","Claimant Admission"}, 0);
            } else {
                claimsTableModel.setRowCount(0);
            }
            for (Claim c : claims) {
                claimsTableModel.addRow(new Object[]{c.claimId, c.itemId, c.userId, c.claimDate, c.status, c.claimantContact, c.claimantAdmissionNo});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
