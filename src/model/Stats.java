package model;

/**
 * Clasa care retine statistici pentru un nod selectat:
 * numar de fisiere, foldere, noduri totale, adancime maxima
 * si dimensiunea totala a fisierelor din subarbore.
 */
public class Stats {

    /**
     * Numarul total de noduri (inclusiv cel de start).
     */
    public int totalNodes;

    /**
     * Numarul total de foldere din subarbore.
     */
    public int folders;

    /**
     * Numarul total de fisiere din subarbore.
     */
    public int files;

    /**
     * Nivelul maxim de adancime al subarborelui.
     */
    public int maxDepth;

    /**
     * Dimensiunea totala in bytes a tuturor fisierelor din subarbore.
     */
    public long totalSizeBytes;

    /**
     * Vector unidimensional care retine cate noduri exista pe fiecare
     * nivel de adancime.
     * <p>
     * Indexul reprezinta adancimea (0, 1, 2, ...).
     */
    public int[] nodesPerDepth;

    /**
     * Matrice bidimensionala pentru numarul de noduri pe fiecare nivel si tip.
     * <p>
     * Indexarea este:
     * <pre>
     * countByTypePerDepth[depth][typeIndex]
     * </pre>
     * unde depth este nivelul de adancime, iar typeIndex este
     * {@code NodeType.ordinal()}.
     */
    public int[][] countByTypePerDepth;

    /**
     * Constructor gol, folosit atunci cand ne intereseaza doar
     * valorile agregate simple (totalNodes, folders, files, maxDepth, totalSizeBytes).
     * <p>
     * Campurile nodesPerDepth si countByTypePerDepth nu sunt initializate aici.
     */
    public Stats() {

    }

    /**
     * Constructor care initializeaza si cele doua masive:
     * <ul>
     *     <li>nodesPerDepth – vector de dimensiune maxDepth + 1</li>
     *     <li>countByTypePerDepth – matrice [maxDepth + 1][nodeTypeCount]</li>
     * </ul>
     *
     * @param maxDepth      adancimea maxima estimata a subarborelui
     * @param nodeTypeCount numarul de tipuri de noduri (ex: NodeType.values().length)
     */
    public Stats(int maxDepth, int nodeTypeCount) {
        this.nodesPerDepth = new int[maxDepth + 1];
        this.countByTypePerDepth = new int[maxDepth + 1][nodeTypeCount];
    }


}
