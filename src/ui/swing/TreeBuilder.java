package ui.swing;

import model.FsNode;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeBuilder {
    public static DefaultMutableTreeNode buildSwingTree(FsNode root){
        DefaultMutableTreeNode swingRoot = new DefaultMutableTreeNode(root);
        for(FsNode ch: root.getChildren()) swingRoot.add(buildRec(ch));
        return swingRoot;
    }
    private static DefaultMutableTreeNode buildRec(FsNode n){
        DefaultMutableTreeNode dn = new DefaultMutableTreeNode(n);
        for(FsNode ch: n.getChildren()) dn.add(buildRec(ch));
        return dn;
    }
}
