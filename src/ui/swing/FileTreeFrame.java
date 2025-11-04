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

    public FileTreeFrame() {
        super("File Structure");

        // Load model
        FsNode rootModel;
        try { rootModel = new FileTreeRepository().loadFromText(new File("structura.txt")); }
        catch (Exception e){ throw new RuntimeException(e); }

        // Build fx tree
        DefaultMutableTreeNode swingRoot = TreeBuilder.buildSwingTree(rootModel);
        model = new DefaultTreeModel(swingRoot);
        tree  = new JTree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        expandFirstLevel();

        // Wire controller (service)
        FileTreeService service = new FileTreeService(rootModel);
        controller = new PopupController(tree, model, service);

        // Popup
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
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
}
