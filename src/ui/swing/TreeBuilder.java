package ui.swing;

import model.FsNode;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Clasa utilitara care construieste arborele Swing ({@link DefaultMutableTreeNode})
 * pornind din arborele logic de fisiere bazat pe {@link FsNode}.
 * <p>
 * Conversia este necesara deoarece modelul Swing foloseste noduri diferite
 * fata de modelul logic al aplicatiei, iar arborele UI trebuie sincronizat
 * cu structura FsNode incarcata din fisier sau modificata in runtime.
 */
public class TreeBuilder {

    /**
     * Construieste radacina Swing a arborelui pe baza radacinii logice FsNode.
     * <p>
     * Aceasta metoda:
     * <ul>
     *     <li>creaza un {@link DefaultMutableTreeNode} cu {@code root} ca userObject</li>
     *     <li>apeleaza recursiv {@link #buildRec(FsNode)} pentru fiecare copil</li>
     * </ul>
     *
     * @param root radacina logica ({@link FsNode})
     * @return radacina arborelui Swing corespunzatoare
     */
    public static DefaultMutableTreeNode buildSwingTree(FsNode root){
        DefaultMutableTreeNode swingRoot = new DefaultMutableTreeNode(root);
        for(FsNode ch: root.getChildren()) swingRoot.add(buildRec(ch));
        return swingRoot;
    }

    /**
     * Metoda recursiva care converteste un nod FsNode intr-un nod Swing
     * si adauga recursiv toti descendentii lui.
     *
     * @param n nodul FsNode care trebuie convertit
     * @return nodul Swing construit, impreuna cu subarborele sau
     */
    private static DefaultMutableTreeNode buildRec(FsNode n){
        DefaultMutableTreeNode dn = new DefaultMutableTreeNode(n);
        for(FsNode ch: n.getChildren()) dn.add(buildRec(ch));
        return dn;
    }
}
