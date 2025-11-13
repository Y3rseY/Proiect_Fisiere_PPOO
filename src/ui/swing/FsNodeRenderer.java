package ui.swing;

import model.FsNode;
import model.NodeType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class FsNodeRenderer extends DefaultTreeCellRenderer {

    private final Icon folderIcon;
    private final Icon fileIcon;
    private final Icon driveIcon;

    public FsNodeRenderer() {
        folderIcon = new ImageIcon(getClass().getResource("/resources/icons/folder.png"));
        fileIcon   = new ImageIcon(getClass().getResource("/resources/icons/file.png"));
        driveIcon  = new ImageIcon(getClass().getResource("/resources/icons/drive.png"));
    }

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
