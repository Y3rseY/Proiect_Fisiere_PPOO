package io;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Clasa responsabila cu incarcare si salvarea structurii logice
 * a sistemului de fisiere din/spre un fisier text (structura.txt).
 * Formatul fiecarui nod:
 *  - nume folder/drive
 *  - pentru fisiere: nume//dimensiune
 */
public class FileTreeRepository {

    /**
     * Incarca arborele din fisierul dat.
     * Fiecare nivel este indentat cu 3 spatii.
     * Pentru fisiere formatul este nume//dimensiune.
     *
     * @param file fisierul text de intrare
     * @return radacina invizibila ce contine toti driverii
     */
    public FsNode loadFromText(File file) throws IOException {
        FsNode root = new FsNode("(root)", NodeType.FOLDER);
        Map<Integer, FsNode> levelMap = new HashMap<>();
        levelMap.put(0, root);



        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                int leading = countLeadingSpaces(line);
                int level = leading / 3;


                String content = line.substring(leading);

                String name;
                long size = 0L;


                int sep = content.lastIndexOf("//");
                if (sep != -1) {
                    name = content.substring(0, sep).trim();
                    String sizeStr = content.substring(sep + 2).trim();
                    if (!sizeStr.isEmpty()) {
                        try {
                            size = Long.parseLong(sizeStr);
                        } catch (NumberFormatException e) {
                            size = 0L;
                        }
                    }
                } else {

                    name = content.trim();
                }

                NodeType type = guessType(name);

                FsNode node;
                if (type == NodeType.FILE) {
                    node = new FsNode(name, type, size);
                } else {
                    node = new FsNode(name, type);
                }

                FsNode parent = levelMap.getOrDefault(level, root);
                parent.addChild(node);
                levelMap.put(level + 1, node);
            }
        }
        return root;
    }

    /**
     * Salveaza structura arborelui in fisierul dat.
     * Foloseste indentare cu 3 spatii pentru niveluri.
     * Pentru fisiere scrie formatul nume//dimensiune.
     *
     * @param root radacina logica
     * @param file fisierul de iesire
     */
    public void saveToText(FsNode root, File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (FsNode d : root.getChildren()) {
                writeRec(d, 0, bw);
            }
        }
    }

    /**
     * Scrie recursiv nodurile cu indentarea corespunzatoare.
     *
     * @param n   nodul curent
     * @param lvl nivelul de indentare
     * @param bw  writer
     */
    private void writeRec(FsNode n, int lvl, BufferedWriter bw) throws IOException {
        // indent
        bw.write("   ".repeat(lvl));
        // numele
        bw.write(n.getName());

        // daca este fisier, scriem // + dimensiune
        if (n.getType() == NodeType.FILE) {
            bw.write("//");
            bw.write(Long.toString(n.getSizeBytes()));
        }

        bw.newLine();

        for (FsNode ch : n.getChildren()) {
            writeRec(ch, lvl + 1, bw);
        }
    }

    /**
     * Numarul de spatii de la inceputul liniei.
     *
     * @param s linia curenta
     * @return numarul de spatii
     */
    private int countLeadingSpaces(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return i;
    }

    /**
     * Deduce tipul nodului dupa nume.
     *
     * @param name numele nodului
     * @return tipul nodului
     */
    private NodeType guessType(String name) {
        if (name.endsWith(":")) return NodeType.DRIVE;
        if (name.contains(".")) return NodeType.FILE;
        return NodeType.FOLDER;
    }

}

