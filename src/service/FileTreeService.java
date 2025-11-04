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

    public FsNode createFile(String[] parentPath, String name){
        FsNode parent = find(parentPath);
        FsNode node = new FsNode(name, NodeType.FILE);
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

    public Stats stats(String[] path){
        FsNode n = find(path);
        Stats s = new Stats();
        dfs(n,0,s); return s;
    }
    private void dfs(FsNode n,int d,Stats s){
        s.totalNodes++; if(n.getType()==NodeType.FILE) s.files++; else s.folders++; s.maxDepth=Math.max(s.maxDepth,d);
        for(FsNode ch: n.getChildren()) dfs(ch,d+1,s);
    }
}
