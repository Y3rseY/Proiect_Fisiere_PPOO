package ui.swing;

import io.FileTreeRepository;
import model.FsNode;
import service.FileTreeService;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class FileTreeFrame extends JFrame {
    private final JTree tree;
    private final DefaultTreeModel model;
    private DefaultMutableTreeNode clickedNode;
    private final PopupController controller;

    private FsNode rootModel;
    private FileTreeService service;

    public FileTreeFrame() {
        super("File Structure");

        try { rootModel = new FileTreeRepository().loadFromText(new File("structura.txt")); }
        catch (Exception e){ throw new RuntimeException(e); }

        DefaultMutableTreeNode swingRoot = TreeBuilder.buildSwingTree(rootModel);
        model = new DefaultTreeModel(swingRoot);
        tree  = new JTree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        expandFirstLevel();

        // Wire controller (service)
        service = new FileTreeService(rootModel);
        controller = new PopupController(tree, model, service);

        // Drag & Drop
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON);
        tree.setTransferHandler(new FileTreeTransferHandler(tree, service));
        tree.setExpandsSelectedPaths(true);

        // Popup (ramane cum era)
        JPopupMenu popup = buildPopupMenu();
        tree.addMouseListener(new MouseAdapter() {
            private void maybe(MouseEvent e){
                if(!e.isPopupTrigger()) return;
                int row = tree.getRowForLocation(e.getX(), e.getY());
                if(row==-1) return;
                tree.setSelectionRow(row);
                clickedNode = (DefaultMutableTreeNode) tree.getPathForLocation(e.getX(), e.getY()).getLastPathComponent();
                popup.show(tree, e.getX(), e.getY());
            }
            @Override public void mousePressed (MouseEvent e){ maybe(e); }
            @Override public void mouseReleased(MouseEvent e){ maybe(e); }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);
        setSize(760, 560);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                onBeforeExit();
            }
        });
    }


    private void expandFirstLevel(){ for(int i=0;i<tree.getRowCount();i++) tree.expandRow(i); }

    private JPopupMenu buildPopupMenu(){
        JPopupMenu p = new JPopupMenu();
        JMenuItem miNewFolder = new JMenuItem("Create folder");
        JMenuItem miNewFile   = new JMenuItem("Create file");
        JMenuItem miRename    = new JMenuItem("Rename");
        JMenuItem miDelete    = new JMenuItem("Delete");
        JMenuItem miStats     = new JMenuItem("Stats");

        miNewFolder.addActionListener(e -> controller.createFolder(clickedNode));
        miNewFile.addActionListener  (e -> controller.createFile(clickedNode));
        miRename.addActionListener   (e -> controller.rename(clickedNode));
        miDelete.addActionListener   (e -> controller.delete(clickedNode));
        miStats.addActionListener    (e -> controller.stats(clickedNode));

        p.add(miNewFolder); p.add(miNewFile); p.addSeparator();
        p.add(miRename); p.add(miDelete); p.add(miStats);
        return p;
    }

    private void onBeforeExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Vrei să salvez modificările înainte de ieșire?",
                "Confirmare ieșire",
                JOptionPane.YES_NO_CANCEL_OPTION
        );
        if (choice == JOptionPane.CANCEL_OPTION) return;

        if (choice == JOptionPane.YES_OPTION) {
            try {
                // dacă ai deja o metodă în service, folosește-o; altfel salvează din repository:
                new FileTreeRepository().saveToText(rootModel, new File("structura.txt"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare la salvare: " + ex.getMessage(),
                        "Eroare", JOptionPane.ERROR_MESSAGE);
                return; // nu închide dacă a eșuat salvarea
            }
        }
        dispose();
        System.exit(0);
    }
}
