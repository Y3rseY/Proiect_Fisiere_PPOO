package service;

import model.*;

public class FileTreeService {
    private final FsNode root; // root invizibil (conține C:, D:, ...)

    public FileTreeService(FsNode invisibleRoot){ this.root = invisibleRoot; }
    public FsNode getRoot(){ return root; }

    public FsNode find(String... parts){
        FsNode cur = root;
        for (String p : parts) {
            FsNode next = cur.childByName(p);
            if(next==null) throw new IllegalArgumentException("Path not found: "+String.join("/",parts));
            cur = next;
        }
        return cur;
    }

    public FsNode createFolder(String[] parentPath, String name){
        FsNode parent = find(parentPath);
        FsNode node = new FsNode(name, NodeType.FOLDER);
        node.addChild(new FsNode("",NodeType.FILE));
        parent.addChild(node);

        return node;
    }

    public FsNode createFile(String[] parentPath, String name, long sizeBytes){
        FsNode parent = find(parentPath);
        FsNode node = new FsNode(name, NodeType.FILE, sizeBytes);
        parent.addChild(node);
        return node;
    }

    public void rename(String[] path, String newName){
        FsNode n = find(path);
        FsNode parent = n.getParent();
        if(parent!=null && parent.childByName(newName)!=null) throw new IllegalArgumentException("Duplicate name");
        n.rename(newName);
    }

    public void delete(String[] path){
        FsNode n = find(path);
        if(n.getParent()==null) throw new IllegalStateException("Cannot delete root");
        n.getParent().removeChild(n);
    }

    public void moveNode(FsNode nodeToMove, FsNode newParent) {
        if (nodeToMove == null || newParent == null) return;

        // nu mutam root-ul invizibil
        if (nodeToMove == root) {
            throw new IllegalArgumentException("Nu poti muta root-ul");
        }

        // noul parinte trebuie sa poata avea copii (DRIVE sau FOLDER)
        if (!newParent.canHaveChildren()) {
            throw new IllegalArgumentException("Poti muta doar in drive sau folder");
        }

        // nu mutam un nod intr-un descendent al lui (ca sa nu facem ciclu)
        if (isDescendant(newParent, nodeToMove)) {
            throw new IllegalArgumentException("Nu poti muta un folder in el insusi sau intr-un descendent");
        }

        FsNode oldParent = nodeToMove.getParent();
        if (oldParent != null) {
            oldParent.removeChild(nodeToMove);
        }

        newParent.addChild(nodeToMove);
    }

    private boolean isDescendant(FsNode node, FsNode potentialAncestor) {
        FsNode cur = node;
        while (cur != null) {
            if (cur == potentialAncestor) return true;
            cur = cur.getParent();
        }
        return false;
    }

public Stats stats(String[] path){
    FsNode start = (path == null || path.length == 0)
            ? root
            : find(path);

    Stats s = new Stats();
    dfsStats(start, 0, s);
    return s;
}

    private void dfsStats(FsNode node, int depth, Stats s){
        s.totalNodes++;
        if (depth > s.maxDepth) s.maxDepth = depth;

        if (node.getType() == NodeType.FILE) {
            s.files++;
            s.totalSizeBytes += node.getSizeBytes();
        } else if (node.getType() == NodeType.FOLDER) {
            s.folders++;
        }

        if (node.getChildren() != null) {
            for (FsNode child : node.getChildren()) {
                dfsStats(child, depth + 1, s);
            }
        }
    }


    private void calcStatsDfs(FsNode node, int depth, Stats s){
        s.totalNodes++;

        // incadram in interpolare de adancime
        if(depth >= 0 && depth < s.nodesPerDepth.length){
            s.nodesPerDepth[depth]++;

            int typeIndex = node.getType().ordinal();
            if(typeIndex >= 0 && typeIndex < s.countByTypePerDepth[depth].length){
                s.countByTypePerDepth[depth][typeIndex]++;
            }
        }

        // numaram foldere / fisiere
        switch (node.getType()) {
            case FOLDER -> s.folders++;
            case FILE   -> s.files++;
            default     -> { /* DRIVE sau alt tip, il ignori aici */ }
        }

        if(depth > s.maxDepth){
            s.maxDepth = depth;
        }

        // parcurgem recursiv copiii, daca exista
        if(node.getChildren() != null){
            for(FsNode child : node.getChildren()){
                calcStatsDfs(child, depth + 1, s);
            }
        }
    }
}
