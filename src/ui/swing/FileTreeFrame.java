package ui.swing;

import io.FileTreeRepository;
import model.FsNode;
import service.FileTreeService;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Fereastra principala Swing care afiseaza structura logica de fisiere
 * intr-un {@link JTree} si permite operatii asupra ei printr-un meniu contextual:
 * creare folder/fisier, redenumire, stergere si afisare statistici.
 * <p>
 * La pornire, structura este incarcata din fisierul text {@code structura.txt},
 * apoi este construita o reprezentare echivalenta in arborele Swing.
 * La inchidere, utilizatorul este intrebat daca vrea sa salveze modificarile.
 */
public class FileTreeFrame extends JFrame {

    /**
     * Arborele Swing care afiseaza nodurile din model.
     */
    private final JTree tree;

    /**
     * Modelul de date pentru arborele Swing.
     */
    private final DefaultTreeModel model;

    /**
     * Nodul pe care s-a dat click dreapta ultima data (folosit de meniul contextual).
     */
    private DefaultMutableTreeNode clickedNode;

    /**
     * Controller-ul care gestioneaza actiunile din meniul contextual
     * si apeleaza metodele din {@link FileTreeService}.
     */
    private final PopupController controller;

    /**
     * Radacina invizibila a modelului logic ({@link FsNode}).
     */
    private FsNode rootModel;

    /**
     * Serviciul care incapsuleaza logica asupra arborelui de fisiere.
     */
    private FileTreeService service;

    /**
     * Constructorul ferestrei principale.
     * <ul>
     *     <li>Incarca structura din fisierul {@code structura.txt}.</li>
     *     <li>Construieste arborele Swing pe baza modelului.</li>
     *     <li>Configureaza renderer-ul de iconite, drag & drop si meniul contextual.</li>
     *     <li>Seteaza comportamentul la inchidere (intrebare de salvare).</li>
     * </ul>
     */
    public FileTreeFrame() {
        super("Proiect Anghel Vlad-Andrei -- Structura fisiere");

        try { rootModel = new FileTreeRepository().loadFromText(new File("structura.txt")); }
        catch (Exception e){ throw new RuntimeException(e); }

        DefaultMutableTreeNode swingRoot = TreeBuilder.buildSwingTree(rootModel);
        model = new DefaultTreeModel(swingRoot);
        tree  = new JTree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new FsNodeRenderer());
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

    /**
     * Extinde primul nivel de noduri din arbore, astfel incat drive-urile
     * sa fie vizibile imediat dupa pornirea aplicatiei.
     */
    private void expandFirstLevel(){
        for(int i=0;i<tree.getRowCount();i++)
            tree.expandRow(i);
    }

    /**
     * Construieste meniul contextual (popup) asociat arborelui.
     * Meniul contine actiunile:
     * <ul>
     *     <li>Create folder</li>
     *     <li>Create file</li>
     *     <li>Rename</li>
     *     <li>Delete</li>
     *     <li>Stats</li>
     * </ul>
     *
     * @return un {@link JPopupMenu} configurat
     */
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

    /**
     * Metoda apelata cand utilizatorul inchide fereastra.
     * Afiseaza un dialog de confirmare:
     * <ul>
     *     <li>Daca se apasa Cancel, aplicatia ramane deschisa.</li>
     *     <li>Daca se apasa Yes, se incearca salvarea structurii in {@code structura.txt}.</li>
     *     <li>Daca salvarea reuseste sau se apasa No, aplicatia se inchide.</li>
     * </ul>
     */
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
