package ui.swing;

import model.FsNode;
import service.FileTreeService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.*;

public class FileTreeTransferHandler extends TransferHandler {

    private final JTree tree;
    private final FileTreeService service;

    private DefaultMutableTreeNode draggedSwingNode;
    private FsNode draggedFsNode;

    public FileTreeTransferHandler(JTree tree, FileTreeService service) {
        this.tree = tree;
        this.service = service;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        TreePath path = tree.getSelectionPath();
        if (path == null) return null;

        DefaultMutableTreeNode dmtn =
                (DefaultMutableTreeNode) path.getLastPathComponent();
        Object uo = dmtn.getUserObject();
        if (!(uo instanceof FsNode fs)) return null;

        draggedSwingNode = dmtn;
        draggedFsNode = fs;

        // nu conteaza textul, important e sa avem ceva transferabil
        return new StringSelection(fs.getName());
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) return false;
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) return false;
        if (draggedSwingNode == null || draggedFsNode == null) return false;

        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        if (destPath == null) return false;

        DefaultMutableTreeNode destSwingNode =
                (DefaultMutableTreeNode) destPath.getLastPathComponent();
        Object uo = destSwingNode.getUserObject();
        if (!(uo instanceof FsNode destFsNode)) return false;

        // destinatia trebuie sa poata avea copii (DRIVE/FOLDER)
        if (!destFsNode.canHaveChildren()) return false;

        // nu permitem drop pe acelasi nod
        if (destFsNode == draggedFsNode) return false;

        // nu permitem mutare intr-un descendent al nodului mutat
        if (isDescendantFs(destFsNode, draggedFsNode)) return false;

        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        if (destPath == null) return false;

        DefaultMutableTreeNode destSwingNode =
                (DefaultMutableTreeNode) destPath.getLastPathComponent();
        Object uo = destSwingNode.getUserObject();
        if (!(uo instanceof FsNode destFsNode)) return false;

        try {
            // 1. actualizam modelul de business
            service.moveNode(draggedFsNode, destFsNode);

            // 2. actualizam si arborele Swing
            DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode oldParent =
                    (DefaultMutableTreeNode) draggedSwingNode.getParent();
            if (oldParent != null) {
                dtm.removeNodeFromParent(draggedSwingNode);
            }
            dtm.insertNodeInto(draggedSwingNode, destSwingNode, destSwingNode.getChildCount());

            // expandam destinatia ca sa vezi rezultatul
            TreePath newPath = new TreePath(draggedSwingNode.getPath());
            tree.scrollPathToVisible(newPath);
            tree.setSelectionPath(newPath);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(tree,
                    "Eroare la mutare: " + ex.getMessage(),
                    "Eroare",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            draggedSwingNode = null;
            draggedFsNode = null;
        }

        return true;
    }

    // helper: verifica daca node este descendent (in FsNode) al potentialAncestor
    private boolean isDescendantFs(FsNode node, FsNode potentialAncestor) {
        FsNode cur = node;
        while (cur != null) {
            if (cur == potentialAncestor) return true;
            cur = cur.getParent();
        }
        return false;
    }
}
