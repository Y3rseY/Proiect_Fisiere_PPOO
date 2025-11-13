# FileTree Manager (Proiect POOO)

## Descriere (Romana - fara diacritice)

Acest proiect reprezinta o aplicatie Java Swing pentru gestionarea unei structuri de fisiere simulate, folosind concepte avansate de Programare Orientata pe Obiecte (POO). Aplicatia permite vizualizarea, modificarea si salvarea structurii de tip arbore, folosind clase precum `FsNode`, `FileTreeService` si `FileTreeRepository`.

### Functionalitati principale
- Afisarea structurii de fisiere in JTree cu iconite personalizate
- Creare / redenumire / stergere fisiere si foldere
- Mutare fisiere prin drag and drop intre foldere
- Calcularea statisticilor pentru fisiere, foldere si drive-uri
- Salvarea si incarcarea structurii din fisier text (`structura.txt`)
- Calcularea dimensiunilor fisierelor si a dimensiunii totale a folderelor
- Generare documentatie automata cu JavaDoc

### Tehnologii folosite
- Java 17 / Java 25
- Swing (JTree, PopupMenu, TransferHandler)
- Programare orientata pe obiecte (compozitie, polimorfism, incapsulare)
- JavaDoc pentru documentatie
- Manipulare fisiere text (I/O)

### Structura proiectului
```
src/
 ├─ model/
 │   ├─ FsNode.java
 │   ├─ NodeType.java
 │   └─ Stats.java
 ├─ service/
 │   └─ FileTreeService.java
 ├─ io/
 │   └─ FileTreeRepository.java
 ├─ ui/swing/
 │   ├─ FileTreeFrame.java
 │   ├─ FsNodeRenderer.java
 │   ├─ PopupController.java
 │   ├─ TreeBuilder.java
 │   └─ FileTreeTransferHandler.java
 └─ app/
     └─ Main.java
```

### JavaDoc
Documentatia JavaDoc este generata automat si include comentarii pentru toate clasele si metodele importante din proiect.

Pentru a regenera JavaDoc manual:
```
javadoc -protected -splitindex -d JavaDoc-Proiect_Fisiere @javadoc_args
```

### Salvare structura fisier
Structura este salvata in format text cu indentare, iar fisierele includ dimensiunea:

```
C:
   folder1
      imagine.png//2048
      video.mp4//500000
D:
   muzica
      track.mp3//3000000
```

---

## Description (English)

This project is a Java Swing application that simulates a file system using advanced Object-Oriented Programming principles. The GUI allows users to visualize, modify, and persist a tree-based file structure.

### Main features
- Display hierarchical file structure using JTree with custom icons
- Create / rename / delete folders and files
- Drag and drop file movement between directories
- Compute statistics for folders, files, and drives
- Load and save structure from a text file (`structura.txt`)
- Store file sizes and calculate aggregated folder sizes
- Fully generated JavaDoc documentation

### Technologies used
- Java 17 / Java 25
- Swing UI
- Object-Oriented Programming patterns
- JavaDoc generator
- File I/O (text format)

### Project structure
```
src/
 ├─ model/
 ├─ service/
 ├─ io/
 ├─ ui/swing/
 └─ app/
```

### JavaDoc regeneration
```
javadoc -protected -splitindex -d JavaDoc-Proiect_Fisiere @javadoc_args
```

### File structure format
Files include their size after a double slash:

```
video.mp4//500000
folder/
   image.jpg//4000
```

---

## Running the application
Compile and run:

```
javac app/Main.java
java app.Main
```

---

## Author
**Vlad Anghel (Y3rseY)**  
Proiect pentru POOO — Master ASE CSIE  
2025

