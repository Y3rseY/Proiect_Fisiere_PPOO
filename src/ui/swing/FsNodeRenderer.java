package ui.swing;

import model.FsNode;
import model.NodeType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Renderer personalizat pentru nodurile JTree.
 * <p>
 * Inlocuieste iconitele default ale lui Swing cu iconite custom
 * pentru fiecare tip de nod din arborele de fisiere:
 * <ul>
 *     <li>FOLDER → iconita folder</li>
 *     <li>FILE → iconita fisier</li>
 *     <li>DRIVE → iconita drive / harddisk</li>
 * </ul>
 * <p>
 * Acest renderer se asigura ca un folder este afisat ca folder
 * chiar daca este gol (nu depindem de flagul "leaf").
 */
public class FsNodeRenderer extends DefaultTreeCellRenderer {

    /**
     * Iconita pentru foldere.
     */
    private final Icon folderIcon;

    /**
     * Iconita pentru fisiere.
     */
    private final Icon fileIcon;

    /**
     * Iconita pentru drive-uri.
     */
    private final Icon driveIcon;

    /**
     * Constructor care incarca iconitele din resurse.
     * Path-urile trebuie sa existe in directorul resources/icons.
     */
    public FsNodeRenderer() {
        folderIcon = new ImageIcon(getClass().getResource("/resources/icons/folder.png"));
        fileIcon   = new ImageIcon(getClass().getResource("/resources/icons/file.png"));
        driveIcon  = new ImageIcon(getClass().getResource("/resources/icons/drive.png"));
    }

    /**
     * Returneaza componenta Swing folosita pentru randarea unui nod din JTree.
     * Seteaza iconita corecta in functie de tipul nodului FsNode:
     * <ul>
     *     <li>NodeType.FOLDER → folderIcon</li>
     *     <li>NodeType.FILE → fileIcon</li>
     *     <li>NodeType.DRIVE → driveIcon</li>
     * </ul>
     *
     * @param tree      arborele in care se randeaza nodul
     * @param value     nodul curent (DefaultMutableTreeNode)
     * @param sel       daca nodul este selectat
     * @param expanded  daca nodul este expandat
     * @param leaf      ignorat (nu folosim flagul pentru iconite)
     * @param row       indexul in arbore
     * @param hasFocus  daca nodul are focus
     * @return componenta configurata pentru afisare
     */
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        Object obj = ((DefaultMutableTreeNode) value).getUserObject();
        if (obj instanceof FsNode node) {
            switch (node.getType()) {
                case FOLDER -> setIcon(folderIcon);
                case FILE   -> setIcon(fileIcon);
                case DRIVE  -> setIcon(driveIcon);
            }
        }

        return this;
    }
}
