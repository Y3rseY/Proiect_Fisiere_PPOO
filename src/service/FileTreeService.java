package service;

import model.*;

/**
 * Clasa de serviciu care incapsuleaza operatiile asupra arborelui logic de fisiere.
 * Lucreaza cu un root invizibil care contine ca si copii principalele drive-uri
 * (ex: "C:", "D:", etc.).
 * <p>
 * UI-ul (Swing) foloseste aceasta clasa pentru a crea, muta, sterge, redenumi
 * noduri si pentru a calcula statistici.
 */
public class FileTreeService {

    /**
     * Radacina invizibila a arborelui (parinte pentru toate drive-urile).
     */
    private final FsNode root; // root invizibil (conține C:, D:, ...)

    /**
     * Constructor care initializeaza serviciul cu un root invizibil deja construit.
     *
     * @param invisibleRoot nodul radacina invizibil
     */
    public FileTreeService(FsNode invisibleRoot){
        this.root = invisibleRoot;
    }

    /**
     * Returneaza radacina invizibila folosita de serviciu.
     */
    public FsNode getRoot(){
        return root;
    }

    /**
     * Cauta un nod pe baza unei cai logice, data ca sir de nume.
     * Exemplu: find("C:", "folder1", "fisier.txt").
     *
     * @param parts secventa de nume, de la root invizibil pana la nodul dorit
     * @return nodul gasit
     * @throws IllegalArgumentException daca nu exista un copil pentru una dintre componente
     */
    public FsNode find(String... parts){
        FsNode cur = root;
        for (String p : parts) {
            FsNode next = cur.childByName(p);
            if(next==null) throw new IllegalArgumentException("Path not found: "+String.join("/",parts));
            cur = next;
        }
        return cur;
    }

    /**
     * Creeaza un folder nou ca si copil al unui nod dat prin cale.
     * <p>
     * Atentie: in versiunea curenta adauga si un copil "dummy" de tip FILE
     * cu nume gol, conform implementarii existente.
     *
     * @param parentPath calea pana la parinte (ex: ["C:", "folder1"])
     * @param name       numele noului folder
     * @return nodul folder creat
     * @throws IllegalArgumentException daca parintele nu este gasit
     */
    public FsNode createFolder(String[] parentPath, String name){
        FsNode parent = find(parentPath);
        FsNode node = new FsNode(name, NodeType.FOLDER);
        node.addChild(new FsNode("",NodeType.FILE));
        parent.addChild(node);

        return node;
    }

    /**
     * Creeaza un fisier nou in cadrul unui folder dat prin cale.
     *
     * @param parentPath calea pana la folderul parinte
     * @param name       numele fisierului (ex: "poza.jpg")
     * @param sizeBytes  dimensiunea fisierului in bytes
     * @return nodul fisier creat
     * @throws IllegalArgumentException daca parintele nu este gasit
     */
    public FsNode createFile(String[] parentPath, String name, long sizeBytes){
        FsNode parent = find(parentPath);
        FsNode node = new FsNode(name, NodeType.FILE, sizeBytes);
        parent.addChild(node);
        return node;
    }

    /**
     * Redenumeste un nod identificat prin calea sa logica.
     * Verifica daca in parinte nu exista deja un copil cu acelasi nume
     * (pentru a evita duplicatele).
     *
     * @param path    calea pana la nodul care va fi redenumit
     * @param newName numele nou
     * @throws IllegalArgumentException daca exista deja un copil cu acelasi nume
     */
    public void rename(String[] path, String newName){
        FsNode n = find(path);
        FsNode parent = n.getParent();
        if(parent!=null && parent.childByName(newName)!=null)
            throw new IllegalArgumentException("Duplicate name");
        n.rename(newName);
    }

    /**
     * Sterge un nod din arbore, pe baza caii sale logice.
     * Nu permite stergerea root-ului invizibil.
     *
     * @param path calea pana la nodul care va fi sters
     * @throws IllegalStateException daca se incearca stergerea radacinii
     */
    public void delete(String[] path){
        FsNode n = find(path);
        if(n.getParent()==null)
            throw new IllegalStateException("Cannot delete root");
        n.getParent().removeChild(n);
    }

    /**
     * Muta un nod (fisier sau folder) intr-un nou parinte.
     * <ul>
     *     <li>Nu permite mutarea root-ului invizibil.</li>
     *     <li>Noul parinte trebuie sa poata avea copii (DRIVE sau FOLDER).</li>
     *     <li>Nu permite mutarea unui nod intr-un descendent al sau (evita cicluri).</li>
     * </ul>
     *
     * @param nodeToMove nodul care va fi mutat
     * @param newParent  noul parinte
     * @throws IllegalArgumentException daca regulile de mai sus sunt incalcate
     */
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

    /**
     * Verifica daca un nod este descendent (in jos in arbore) al unui anumit stramos.
     *
     * @param node             nodul de verificat
     * @param potentialAncestor nodul care ar putea fi stramos
     * @return true daca {@code potentialAncestor} se afla pe lantul de parinti al lui {@code node}
     */
    private boolean isDescendant(FsNode node, FsNode potentialAncestor) {
        FsNode cur = node;
        while (cur != null) {
            if (cur == potentialAncestor) return true;
            cur = cur.getParent();
        }
        return false;
    }

    /**
     * Calculeaza statisticile simple pentru subarborele pornind de la un anumit nod.
     * <ul>
     *     <li>Daca path este null sau gol, se foloseste root-ul invizibil.</li>
     *     <li>Completeaza campurile simple din {@link Stats}: totalNodes, folders, files, maxDepth, totalSizeBytes.</li>
     * </ul>
     *
     * @param path calea pana la nodul pentru care se calculeaza statisticile
     * @return obiect Stats populat
     */
    public Stats stats(String[] path){
        FsNode start = (path == null || path.length == 0)
                ? root
                : find(path);

        Stats s = new Stats();
        dfsStats(start, 0, s);
        return s;
    }

    /**
     * Functie ajutatoare recursiva pentru calculul statisticilor simple.
     * Numarara noduri, fisiere, foldere, adancime maxima si suma dimensiunilor fisierelor.
     *
     * @param node  nodul curent
     * @param depth nivelul de adancime
     * @param s     obiectul Stats care se actualizeaza
     */
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

    /**
     * Functie recursiva alternativa pentru calculul statisticilor detaliate,
     * folosind doua masive in clasa {@link Stats}:
     * <ul>
     *     <li>{@code nodesPerDepth} - numar de noduri pe fiecare nivel</li>
     *     <li>{@code countByTypePerDepth} - numar de noduri pe fiecare nivel si tip</li>
     * </ul>
     * <p>
     * Aceasta metoda poate fi apelata dintr-o varianta mai avansata a lui {@link #stats(String[])}
     * daca se doreste folosirea masivelor.
     *
     * @param node  nodul curent
     * @param depth adancimea curenta
     * @param s     obiectul Stats care se actualizeaza
     */
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
