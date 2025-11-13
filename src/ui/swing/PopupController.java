package ui.swing;

import model.*;
import service.FileTreeService;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Controller pentru meniul contextual (popup) al arborelui JTree.
 * <p>
 * Clasa leaga actiunile din meniu (create folder, create file, rename, delete, stats)
 * de modelul logic ({@link FsNode}) prin intermediul {@link FileTreeService}
 * si actualizeaza in acelasi timp si arborele Swing ({@link DefaultTreeModel}).
 */
public class PopupController {

    /**
     * Arborele Swing pe care se afiseaza structura de fisiere.
     */
    private final JTree tree;

    /**
     * Modelul de date asociat arborelui Swing.
     */
    private final DefaultTreeModel model;

    /**
     * Serviciul care opereaza pe arborele logic de fisiere.
     */
    private final FileTreeService service;

    /**
     * Creeaza un controller pentru meniul popup asociat unui JTree.
     *
     * @param tree    arborele care afiseaza structura
     * @param model   modelul de date al arborelui Swing
     * @param service serviciul care lucreaza cu FsNode
     */
    public PopupController(JTree tree, DefaultTreeModel model, FileTreeService service){
        this.tree=tree; this.model=model; this.service=service;
    }

    /**
     * Helper care construieste calea logica (sir de nume) pornind de la un nod Swing.
     * <p>
     * Primul element din path (root-ul invizibil) este ignorat. Exemplu:
     * <pre>
     * [rootInvizibil, "C:", "folder1", "fisier.txt"]
     * -> ["C:", "folder1", "fisier.txt"]
     * </pre>
     *
     * @param node nodul Swing de la care se construieste calea
     * @return vector de stringuri reprezentand calea pentru FileTreeService
     */
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

    /**
     * Gestioneaza actiunea de creare folder din meniul contextual.
     * <ul>
     *     <li>Nu permite creare de folder sub un fisier.</li>
     *     <li>Cere utilizatorului numele folderului.</li>
     *     <li>Apeleaza {@link FileTreeService#createFolder} pentru model.</li>
     *     <li>Insereaza nodul nou si in arborele Swing.</li>
     * </ul>
     *
     * @param clicked nodul Swing de la care s-a deschis meniul (parintele noului folder)
     */
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

    /**
     * Gestioneaza actiunea de creare fisier din meniul contextual.
     * <ul>
     *     <li>Nu permite creare sub un nod de tip FILE.</li>
     *     <li>Cere numele fisierului si dimensiunea in bytes.</li>
     *     <li>Apeleaza {@link FileTreeService#createFile} pentru model.</li>
     *     <li>Insereaza nodul nou in arborele Swing.</li>
     * </ul>
     *
     * @param clicked nodul Swing de la care s-a deschis meniul (parintele noului fisier)
     */
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

    /**
     * Gestioneaza actiunea de redenumire din meniul contextual.
     * <ul>
     *     <li>Cere utilizatorului un nume nou.</li>
     *     <li>Apeleaza {@link FileTreeService#rename} pentru a actualiza modelul.</li>
     *     <li>Actualizeaza si nodul Swing si notifica modelul de schimbare.</li>
     * </ul>
     *
     * @param clicked nodul selectat care va fi redenumit
     */
    public void rename(DefaultMutableTreeNode clicked){
        FsNode d = (FsNode) clicked.getUserObject();
        String nn = JOptionPane.showInputDialog("New name:", d.getName());
        if(nn==null || nn.isBlank()) return;
        service.rename(pathOf(clicked), nn.trim());
        d.rename(nn.trim()); model.nodeChanged(clicked);
    }

    /**
     * Gestioneaza actiunea de stergere din meniul contextual.
     * <ul>
     *     <li>Nu permite stergerea unui drive sau a unui nod fara parinte.</li>
     *     <li>Cere confirmare utilizatorului.</li>
     *     <li>Apeleaza {@link FileTreeService#delete} pentru model.</li>
     *     <li>Elimina nodul si din arborele Swing si selecteaza parintele.</li>
     * </ul>
     *
     * @param clicked nodul care urmeaza sa fie sters
     */
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

    /**
     * Gestioneaza actiunea de afisare statistici din meniul contextual.
     * <ul>
     *     <li>Daca nodul este FILE: afiseaza nume, extensie si dimensiune.</li>
     *     <li>Daca nodul este FOLDER sau DRIVE: apeleaza {@link FileTreeService#stats(String[])}
     *         si afiseaza dimensiunea totala, numarul de foldere, fisiere, noduri si adancimea maxima.</li>
     * </ul>
     *
     * @param clicked nodul pentru care se afiseaza statisticile
     */
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
