package ui.swing;

import model.*;
import service.FileTreeService;

import javax.swing.*;
import javax.swing.tree.*;

public class PopupController {
    private final JTree tree;
    private final DefaultTreeModel model;
    private final FileTreeService service;

    public PopupController(JTree tree, DefaultTreeModel model, FileTreeService service){
        this.tree=tree; this.model=model; this.service=service;
    }

    // Helpers: extrage calea logică din nodul selectat
    private String[] pathOf(DefaultMutableTreeNode node){
        var path = node.getPath();
        // [invisibleRoot, C:, folder1, ...] -> ignoră primul
        String[] parts = new String[path.length-1];
        for(int i=1;i<path.length;i++){
            FsNode data = (FsNode)((DefaultMutableTreeNode)path[i]).getUserObject();
            parts[i-1] = data.getName();
        }
        return parts;
    }

    public void createFolder(DefaultMutableTreeNode clicked){
        FsNode d = (FsNode) clicked.getUserObject();
        if(d.getType()==NodeType.FILE) return;
        String name = JOptionPane.showInputDialog("Folder name:");
        if(name==null || name.isBlank()) return;
        service.createFolder(pathOf(clicked), name.trim());
        // UI: inserează copilul nou
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FsNode(name.trim(), NodeType.FOLDER));
        model.insertNodeInto(child, clicked, clicked.getChildCount());
        tree.expandPath(new TreePath(clicked.getPath()));
    }


    public void createFile(DefaultMutableTreeNode clicked){
        FsNode d = (FsNode) clicked.getUserObject();
        if(d.getType() == NodeType.FILE) return;

        String name = JOptionPane.showInputDialog("File name (ex: video.mp4):");
        if(name == null || name.isBlank()) return;
        name = name.trim();

        // intrebam dimensiunea
        long size = 0;
        while (true) {
            String sizeStr = JOptionPane.showInputDialog("File size in bytes:");
            if (sizeStr == null) {
                // user a apasat Cancel -> iesim, nu cream fisierul
                return;
            }
            sizeStr = sizeStr.trim();
            if (sizeStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Introdu o dimensiune (numar intreg).");
                continue;
            }
            try {
                size = Long.parseLong(sizeStr);
                if (size < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Dimensiune invalida. Scrie un numar intreg pozitiv.");
            }
        }

        // model + service
        FsNode created = service.createFile(pathOf(clicked), name, size);
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(created);
        model.insertNodeInto(child, clicked, clicked.getChildCount());
        tree.expandPath(new TreePath(clicked.getPath()));
    }

    public void rename(DefaultMutableTreeNode clicked){
        FsNode d = (FsNode) clicked.getUserObject();
        String nn = JOptionPane.showInputDialog("New name:", d.getName());
        if(nn==null || nn.isBlank()) return;
        service.rename(pathOf(clicked), nn.trim());
        d.rename(nn.trim()); model.nodeChanged(clicked);
    }

    public void delete(DefaultMutableTreeNode clicked){
        FsNode d = (FsNode) clicked.getUserObject();
        if(d.getType()==NodeType.DRIVE || clicked.getParent()==null){
            JOptionPane.showMessageDialog(null,"Cannot delete this node."); return;
        }
        int ok = JOptionPane.showConfirmDialog(null,"Delete \""+d.getName()+"\"?","Confirm",JOptionPane.YES_NO_OPTION);
        if(ok!=JOptionPane.YES_OPTION) return;
        service.delete(pathOf(clicked));
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) clicked.getParent();
        model.removeNodeFromParent(clicked);
        tree.setSelectionPath(new TreePath(parent.getPath()));
    }

public void stats(DefaultMutableTreeNode clicked){
    FsNode d = (FsNode) clicked.getUserObject();

    if (d.getType() == NodeType.FILE) {
        String name = d.getName();
        String ext;

        int dot = name.lastIndexOf('.');
        if (dot != -1 && dot < name.length() - 1) {
            ext = name.substring(dot + 1);
        } else {
            ext = "(no extension)";
        }

        JOptionPane.showMessageDialog(
                null,
                "File name: " + name +
                        "\nExtension: " + ext +
                        "\nSize: " + d.getSizeBytes() + " bytes"
        );
        return;
    }

    // pentru FOLDER / DRIVE: agregam prin service.stats
    Stats s = service.stats(pathOf(clicked));

    JOptionPane.showMessageDialog(
            null,
            "Name: " + d.getName() +
                    "\nType: " + d.getType() +
                    "\nTotal size: " + s.totalSizeBytes + " bytes" +
                    "\nFolders: " + s.folders +
                    "\nFiles: " + s.files +
                    "\nTotal nodes: " + s.totalNodes +
                    "\nMax depth: " + s.maxDepth
    );
}
}
