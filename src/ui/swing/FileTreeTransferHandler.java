package ui.swing;

import model.FsNode;
import service.FileTreeService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.*;

/**
 * TransferHandler pentru JTree-ul de fisiere.
 * Gestioneaza operatia de drag & drop intre nodurile arborelui:
 * muta atat nodurile din modelul logic ({@link FsNode}), cat si nodurile
 * corespondente din arborele Swing ({@link DefaultMutableTreeNode}).
 */
public class FileTreeTransferHandler extends TransferHandler {

    /**
     * Arborele Swing asupra caruia se aplica drag & drop.
     */
    private final JTree tree;

    /**
     * Serviciul care stie sa mute nodurile in modelul de business (FsNode).
     */
    private final FileTreeService service;

    /**
     * Nodul Swing care este in curs de mutare (sursa drag-ului).
     */
    private DefaultMutableTreeNode draggedSwingNode;

    /**
     * Nodul logic (FsNode) care este in curs de mutare.
     */
    private FsNode draggedFsNode;

    /**
     * Creeaza un handler de drag & drop pentru un anumit arbore si serviciu.
     *
     * @param tree    JTree-ul care afiseaza structura
     * @param service serviciul care opereaza pe modelul FsNode
     */
    public FileTreeTransferHandler(JTree tree, FileTreeService service) {
        this.tree = tree;
        this.service = service;
    }

    /**
     * Specifica faptul ca suportam doar operatia de MOVE (mutare) ca sursa.
     *
     * @param c componenta sursa
     * @return MOVE
     */
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    /**
     * Creeaza un obiect transferabil cand utilizatorul incepe drag-ul.
     * In acest caz, ne folosim de StringSelection doar ca "suport" minimal,
     * informatia reala fiind retinuta in campurile draggedSwingNode si draggedFsNode.
     *
     * @param c componenta sursa
     * @return un Transferable sau null daca selectia nu este valida
     */
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

    /**
     * Verifica daca datele pot fi importate (drop) in locatia curenta.
     * Se asigura ca:
     * <ul>
     *     <li>operatia este un drop</li>
     *     <li>exista un nod sursa valid (draggedFsNode)</li>
     *     <li>destinatia este un FsNode care poate avea copii (DRIVE/FOLDER)</li>
     *     <li>nu se muta un nod in el insusi sau intr-un descendent</li>
     * </ul>
     *
     * @param support contextul de transfer
     * @return true daca importul este permis, false altfel
     */
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

    /**
     * Executa efectiv operatia de mutare atunci cand drop-ul este acceptat.
     * <ol>
     *     <li>Muta nodul in modelul FsNode prin {@link FileTreeService#moveNode}.</li>
     *     <li>Actualizeaza arborele Swing (scoate nodul din vechiul parinte si
     *     il insereaza sub noul parinte).</li>
     *     <li>Selecteaza si deruleaza la noua locatie a nodului mutat.</li>
     * </ol>
     *
     * @param support contextul de transfer/drop
     * @return true daca mutarea a reusit, false in caz de eroare
     */
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

    /**
     * Helper care verifica daca un nod FsNode este descendent (in jos in arbore)
     * al unui alt nod dat ca potential stramos.
     *
     * @param node              nodul verificat
     * @param potentialAncestor posibilul stramos
     * @return true daca potentialAncestor se afla pe lantul de parinti al lui node
     */
    private boolean isDescendantFs(FsNode node, FsNode potentialAncestor) {
        FsNode cur = node;
        while (cur != null) {
            if (cur == potentialAncestor) return true;
            cur = cur.getParent();
        }
        return false;
    }
}
