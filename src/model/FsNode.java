package model;

import java.util.*;

public class FsNode {
    private String name;
    private final NodeType type;
    private FsNode parent;
    private long sizeBytes;
    private final List<FsNode> children = new ArrayList<>();

    public FsNode(String name, NodeType type) {
        this(name,type,0);
    }
    public FsNode(String name, NodeType type, long sizeBytes) {
        this.name = name;
        this.type = type;
        this.sizeBytes = sizeBytes;
    }

    // getter / setter pentru dimensiune (are sens doar la FILE)
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public boolean canHaveChildren() {
        return type == NodeType.DRIVE || type == NodeType.FOLDER;
    }

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }

    public FsNode getParent() {
        return parent;
    }

    public List<FsNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void rename(String newName) {
        if(newName==null||newName.isBlank()) throw new IllegalArgumentException(); name = newName.trim();
    }

    public void addChild(FsNode child) {
        if(!canHaveChildren()) throw new IllegalStateException("Files cannot have children");
        if(child.parent != null) throw new IllegalStateException("Already attached");
        if(childByName(child.name)!=null) throw new IllegalArgumentException("Duplicate name");
        child.parent = this; children.add(child);
    }

    public void removeChild(FsNode child) {
        if(children.remove(child)) child.parent = null;
    }

    public FsNode childByName(String name){
        for(var c:children) if(c.name.equalsIgnoreCase(name)) return c; return null;
    }

    @Override
    public String toString(){
        return name;
    }
}
