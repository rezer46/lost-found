import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                LostAndFoundService service = new LostAndFoundService();
                service.init();
                LostAndFoundGUI gui = new LostAndFoundGUI(service);
                gui.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
