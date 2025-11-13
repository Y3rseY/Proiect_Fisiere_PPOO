package io;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileTreeRepository {
    public FsNode loadFromText(File file) throws IOException {
        FsNode root = new FsNode("(root)", NodeType.FOLDER);
        Map<Integer, FsNode> levelMap = new HashMap<>();
        levelMap.put(0, root);

        // if(!file.exists()) return sample(root);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                int leading = countLeadingSpaces(line);
                int level = leading / 3;

                // continutul fara spatiile de la inceput
                String content = line.substring(leading);

                String name;
                long size = 0L;

                // cautam separatorul //
                int sep = content.lastIndexOf("//");
                if (sep != -1) {
                    name = content.substring(0, sep).trim();
                    String sizeStr = content.substring(sep + 2).trim();
                    if (!sizeStr.isEmpty()) {
                        try {
                            size = Long.parseLong(sizeStr);
                        } catch (NumberFormatException e) {
                            size = 0L; // daca e corupt, ignoram size-ul
                        }
                    }
                } else {
                    // nu exista // -> doar nume (folder/drive sau fisier fara size)
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

    public void saveToText(FsNode root, File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (FsNode d : root.getChildren()) {
                writeRec(d, 0, bw);
            }
        }
    }

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

    private int countLeadingSpaces(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return i;
    }

    private NodeType guessType(String name) {
        if (name.endsWith(":")) return NodeType.DRIVE;
        if (name.contains(".")) return NodeType.FILE;
        return NodeType.FOLDER;
    }

//    private FsNode sample(FsNode root){
//        FsNode c = new FsNode("C:", NodeType.DRIVE); root.addChild(c);
//        FsNode f1 = new FsNode("folder1", NodeType.FOLDER); c.addChild(f1);
//        f1.addChild(new FsNode("folder2", NodeType.FOLDER));
//        f1.addChild(new FsNode("folder3", NodeType.FOLDER));
//        c.addChild(new FsNode("folder4", NodeType.FOLDER));
//        root.addChild(new FsNode("D:", NodeType.DRIVE));
//        return root;
//    }
}

