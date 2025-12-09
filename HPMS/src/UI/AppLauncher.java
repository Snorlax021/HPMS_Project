package UI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class AppLauncher {
    public static void main(String[] args) {
        // Try to use system look and feel for a native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // ignore and use default
        }

        SwingUtilities.invokeLater(() -> {
            LoginUI ui = new LoginUI();
            ui.setVisible(true);
        });
    }
}
