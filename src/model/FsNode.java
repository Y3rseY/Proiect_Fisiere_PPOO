package model;

import java.util.*;

/**
 * Reprezinta un nod din arborele logic al sistemului de fisiere.
 * Un nod poate fi de tip DRIVE, FOLDER sau FILE.
 * <p>
 * DRIVE si FOLDER pot avea copii, FILE nu poate avea copii.
 * Fiecare nod poate sti parintele sau si lista de copii.
 */
public class FsNode {

    /**
     * Numele afisat al nodului (ex: "C:", "folder1", "poza.jpg").
     */
    private String name;

    /**
     * Tipul nodului: DRIVE, FOLDER sau FILE.
     * Este final, nu se modifica dupa creare.
     */
    private final NodeType type;

    /**
     * Referinta catre nodul parinte in arbore.
     * Pentru radacina invizibila sau pentru drive-uri poate fi null.
     */
    private FsNode parent;

    /**
     * Dimensiunea fisierului in bytes.
     * Are sens doar pentru noduri de tip FILE.
     * Pentru DRIVE si FOLDER valoarea este, in mod normal, 0.
     */
    private long sizeBytes;

    /**
     * Lista copiilor acestui nod in arbore.
     * Lista este modificabila doar din interiorul clasei prin addChild/removeChild.
     */
    private final List<FsNode> children = new ArrayList<>();

    /**
     * Creeaza un nod cu nume si tip, cu dimensiune 0.
     *
     * @param name numele nodului
     * @param type tipul nodului
     */
    public FsNode(String name, NodeType type) {
        this(name,type,0);
    }

    /**
     * Creeaza un nod cu nume, tip si dimensiune specificata.
     *
     * @param name      numele nodului
     * @param type      tipul nodului
     * @param sizeBytes dimensiunea in bytes (folosita pentru fisiere)
     */
    public FsNode(String name, NodeType type, long sizeBytes) {
        this.name = name;
        this.type = type;
        this.sizeBytes = sizeBytes;
    }

    /**
     * Returneaza dimensiunea nodului in bytes.
     * Pentru fisiere reprezinta dimensiunea fisierului.
     *
     * @return dimensiunea in bytes
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * Seteaza dimensiunea nodului in bytes.
     * In mod normal este folosit doar pentru fisiere.
     *
     * @param sizeBytes noua dimensiune in bytes
     */
    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    /**
     * Indica daca acest nod are voie sa aiba copii.
     * Doar DRIVE si FOLDER pot avea copii.
     *
     * @return true daca poate avea copii, false altfel
     */
    public boolean canHaveChildren() {
        return type == NodeType.DRIVE || type == NodeType.FOLDER;
    }

    /**
     * Returneaza numele nodului.
     *
     * @return numele curent
     */
    public String getName() {
        return name;
    }

    /**
     * Returneaza tipul nodului.
     *
     * @return tipul (DRIVE, FOLDER sau FILE)
     */
    public NodeType getType() {
        return type;
    }

    /**
     * Returneaza parintele acestui nod in arbore.
     *
     * @return nodul parinte sau null daca nu este atasat
     */
    public FsNode getParent() {
        return parent;
    }

    /**
     * Returneaza lista copiilor acestui nod ca lista nemodificabila.
     * Modificarile se fac doar prin {@link #addChild(FsNode)} si {@link #removeChild(FsNode)}.
     *
     * @return lista copii (unmodifiable)
     */
    public List<FsNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Schimba numele nodului.
     *
     * @param newName noul nume; nu are voie sa fie null sau gol
     * @throws IllegalArgumentException daca numele este null sau gol
     */
    public void rename(String newName) {
        if(newName==null||newName.isBlank())
            throw new IllegalArgumentException();
        name = newName.trim();
    }

    /**
     * Adauga un copil in acest nod.
     * Verifica mai multe conditii:
     * <ul>
     *     <li>nodul curent trebuie sa poata avea copii (nu este FILE)</li>
     *     <li>copilul nu trebuie sa fie deja atasat unui alt parinte</li>
     *     <li>nu trebuie sa existe deja un copil cu acelasi nume (ignorand literele mari/mici)</li>
     * </ul>
     *
     * @param child nodul copil care va fi atasat
     * @throws IllegalStateException    daca nodul curent nu poate avea copii sau copilul este deja atasat
     * @throws IllegalArgumentException daca exista deja un copil cu acelasi nume
     */
    public void addChild(FsNode child) {
        if(!canHaveChildren()) throw new IllegalStateException("Files cannot have children");
        if(child.parent != null) throw new IllegalStateException("Already attached");
        if(childByName(child.name)!=null) throw new IllegalArgumentException("Duplicate name");
        child.parent = this; children.add(child);
    }

    /**
     * Elimina un copil din acest nod.
     * Daca nodul copil exista in lista, este scos si parintele lui devine null.
     *
     * @param child nodul copil care va fi eliminat
     */
    public void removeChild(FsNode child) {
        if(children.remove(child))
            child.parent = null;
    }

    /**
     * Cauta un copil dupa nume (case-insensitive).
     *
     * @param name numele copilului cautat
     * @return nodul copil daca este gasit, altfel null
     */
    public FsNode childByName(String name){
        for(var c:children)
            if(c.name.equalsIgnoreCase(name))
                return c; return null;
    }

    /**
     * Returneaza numele nodului.
     * Este folosit si de JTree pentru afisarea textului.
     */
    @Override
    public String toString(){
        return name;
    }
}
