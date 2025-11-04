package app;

import ui.swing.FileTreeFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileTreeFrame().setVisible(true));
    }
}
