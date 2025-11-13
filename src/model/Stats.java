package model;

public class Stats {
    public int totalNodes, folders, files, maxDepth;
    public long totalSizeBytes;

    // 1) vector unidimensional: cate noduri sunt pe fiecare nivel de adancime
    public int[] nodesPerDepth;

    // 2) matrice bidimensionala: [depth][nodeType]
    // nodeType este indexul lui NodeType.ordinal()
    public int[][] countByTypePerDepth;

    public Stats(int maxDepth, int nodeTypeCount) {
        this.nodesPerDepth = new int[maxDepth + 1];
        this.countByTypePerDepth = new int[maxDepth + 1][nodeTypeCount];
    }

    public Stats() {

    }
}
