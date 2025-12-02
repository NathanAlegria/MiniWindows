/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import exceptions.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.TableRowSorter;
import reproductor.ReproductorGUI;

/**
 *
 * @author Nathan
 */
public class FileExplorerWindow extends JInternalFrame {

    private JTree folderTree;
    private JTable fileTable;
    private FileTableModel tableModel;
    private File currentDir;
    private User currentUser;
    private File rootFolder;

    // UI extras
    private JComboBox<String> sortCombo;
    private JTextField searchField;
    private final java.util.List<File> clipboard = new ArrayList<>();
    private boolean clipboardCut = false; // indica si es cortar o copiar

    public FileExplorerWindow(User user, File rootFolder) {
        super("Explorador - " + user.getUsername(), true, true, true, true);
        this.currentUser = user;
        this.rootFolder = rootFolder;
        setSize(900, 600);
        setLocation(50, 50);
        setLayout(new BorderLayout());

        // ---------------- Barra de herramientas ----------------
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton btnNewFolder = new JButton("Nueva Carpeta");
        JButton btnRename = new JButton("Renombrar");
        JButton btnDelete = new JButton("Eliminar");
        JButton btnOrganize = new JButton("Organizar");
        JButton btnCopy = new JButton("Copiar");
        JButton btnPaste = new JButton("Pegar");

        toolbar.add(btnNewFolder);
        toolbar.add(btnRename);
        toolbar.add(btnDelete);
        toolbar.add(btnOrganize);
        toolbar.addSeparator(new Dimension(10, 0));

        toolbar.add(new JLabel("Buscar: "));
        searchField = new JTextField(15);
        toolbar.add(searchField);
        toolbar.addSeparator(new Dimension(10, 0));

        toolbar.add(new JLabel("Ordenar por: "));
        sortCombo = new JComboBox<>(new String[]{"Nombre", "Fecha (reciente)", "Tamaño (desc)"});
        toolbar.add(sortCombo);
        toolbar.addSeparator();

        toolbar.add(btnCopy);
        toolbar.add(btnPaste);

        add(toolbar, BorderLayout.NORTH);

        // Listeners de barra
        btnNewFolder.addActionListener(e -> createNewFolder());
        btnRename.addActionListener(e -> renameSelectedFile());
        btnDelete.addActionListener(e -> deleteSelectedFile());
        btnOrganize.addActionListener(e -> organizeFiles());
        btnCopy.addActionListener(e -> copySelectedFile());
        btnPaste.addActionListener(e -> pasteClipboard());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateFileTable(currentDir);
            }

            public void removeUpdate(DocumentEvent e) {
                updateFileTable(currentDir);
            }

            public void changedUpdate(DocumentEvent e) {
                updateFileTable(currentDir);
            }
        });

        sortCombo.addActionListener(e -> updateFileTable(currentDir));

        // ---------------- Panel principal (SplitPane) ----------------
        folderTree = DesktopHelper.createSimulatedFileTree(currentUser);
        folderTree.setPreferredSize(new Dimension(240, 0));

        folderTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) folderTree.getLastSelectedPathComponent();
            if (node != null) {
                currentDir = new File(buildPathFromNode(node));
                updateFileTable(currentDir);
            }
        });

        tableModel = new FileTableModel();
        fileTable = new JTable(tableModel);
        fileTable.setRowHeight(24);
        fileTable.setShowGrid(false);
        fileTable.setIntercellSpacing(new Dimension(0, 0));

        // doble clic sobre fila del JTable
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = fileTable.getSelectedRow();
                    if (row < 0) {
                        return;
                    }

                    File f = tableModel.getFileAt(row);

                    if (f.isDirectory()) {
                        currentDir = f;
                        updateFileTable(f);
                        return;
                    }

                    String name = f.getName().toLowerCase();

                    // ============================
                    // ABRIR EDITOR DE TEXTO
                    // ============================
                    if (name.endsWith(".txt")) {
                        Container parent = SwingUtilities.getAncestorOfClass(JDesktopPane.class, FileExplorerWindow.this);

                        if (parent instanceof JDesktopPane desktop) {
                            EditordeTexto.EditorTexto editor = new EditordeTexto.EditorTexto();
                            editor.openFile(f);

                            desktop.add(editor, JLayeredPane.PALETTE_LAYER);
                            editor.setVisible(true);
                            try {
                                editor.setSelected(true);
                            } catch (Exception ignored) {
                            }
                        }
                        return;
                    }

                    // ============================
// ABRIR ARCHIVOS DE AUDIO
// ============================
                    if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".aiff") || name.endsWith(".au")) {
                        Container parent = SwingUtilities.getAncestorOfClass(JDesktopPane.class, FileExplorerWindow.this);

                        if (parent instanceof JDesktopPane desktop) {

                            // Buscar si ya hay un ReproductorGUI abierto para este usuario
                            ReproductorGUI rep = null;
                            for (JInternalFrame frame : desktop.getAllFrames()) {
                                if (frame instanceof ReproductorGUI r && r.getCurrentUser().getUsername().equals(currentUser.getUsername())) {
                                    rep = r;
                                    break;
                                }
                            }

                            // Si no existe, crear uno nuevo
                            if (rep == null) {
                                rep = new ReproductorGUI(currentUser);
                                desktop.add(rep);
                                cascadeFrames(desktop); // opcional: organiza ventanas en cascada
                                rep.setVisible(true);
                            }

                            rep.loadFromFile(f); // carga la canción seleccionada en la playlist
                            try {
                                rep.setSelected(true);
                            } catch (Exception ignored) {
                            }
                        }
                        return;
                    }

                    // ============================
                    // ABRIR IMÁGENES
                    // ============================
                    if (name.matches(".*\\.(png|jpg|jpeg|gif|bmp)")) {
                        JOptionPane.showMessageDialog(FileExplorerWindow.this,
                                "Aquí se abriría el visor de imágenes (lo pediste deshabilitado).");
                        return;
                    }

                    // ============================
                    // ABRIR PDF / OTROS
                    // ============================
                    if (name.endsWith(".pdf")) {
                        JOptionPane.showMessageDialog(FileExplorerWindow.this,
                                "No se admite visor PDF.");
                        return;
                    }

                    JOptionPane.showMessageDialog(FileExplorerWindow.this,
                            "Tipo de archivo no reconocido: " + f.getName());
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(folderTree);
        JScrollPane tableScroll = new JScrollPane(fileTable);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, tableScroll);
        splitPane.setDividerLocation(240);
        add(splitPane, BorderLayout.CENTER);

        currentDir = rootFolder;
        updateFileTable(currentDir);
    }

    private void cascadeFrames(JDesktopPane desktop) {
        int x = 20, y = 20;
        int offset = 30;
        for (JInternalFrame frame : desktop.getAllFrames()) {
            frame.setLocation(x, y);
            x += offset;
            y += offset;
            if (x + frame.getWidth() > desktop.getWidth()) {
                x = 20;
            }
            if (y + frame.getHeight() > desktop.getHeight()) {
                y = 20;
            }
        }
    }

    private void updateFileTable(File dir) {
        if (dir == null || !dir.exists()) {
            tableModel.setFiles(List.of());
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            tableModel.setFiles(List.of());
            return;
        }
        // Filtrar por búsqueda
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<File> list = Arrays.stream(files)
                .filter(f -> q.isEmpty() || f.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());

        // Ordenar según combo
        String ord = (String) sortCombo.getSelectedItem();
        if ("Fecha (reciente)".equals(ord)) {
            list.sort(Comparator.comparingLong(File::lastModified).reversed());
        } else if ("Tamaño (desc)".equals(ord)) {
            list.sort(Comparator.comparingLong(File::length).reversed());
        } else {
            list.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        }

        tableModel.setFiles(list);
    }

    private String buildPathFromNode(DefaultMutableTreeNode node) {
        Object[] nodes = node.getPath();
        StringBuilder path = new StringBuilder(rootFolder.getAbsolutePath());
        for (int i = 1; i < nodes.length; i++) {
            path.append(File.separator).append(nodes[i].toString());
        }
        return path.toString();
    }

    // ---------------- Ejemplos de acciones ----------------
    private void createNewFolder() {
        String name = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:");
        if (name != null && !name.trim().isEmpty()) {
            new File(currentDir, name).mkdirs();
            updateFileTable(currentDir);
        }
    }

    private void renameSelectedFile() {
        int row = fileTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        File file = tableModel.getFileAt(row);
        String newName = JOptionPane.showInputDialog(this, "Nuevo nombre:", file.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            file.renameTo(new File(file.getParentFile(), newName));
            updateFileTable(currentDir);
        }
    }

    private void deleteSelectedFile() {
        int row = fileTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        File file = tableModel.getFileAt(row);
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
        updateFileTable(currentDir);
    }

    private void deleteDirectory(File dir) {
        if (dir == null) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (File f : children) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }

    private void organizeFiles() {
        // Puedes reutilizar la lógica de tu FileExplorer.organizeFiles aquí si es necesario.
        JOptionPane.showMessageDialog(this, "Organizar: implementado en el File Explorer principal.");
    }

    // ---------------- Copiar / Pegar ----------------
    private void copySelectedFile() {
        int[] rows = fileTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }

        clipboard.clear();
        clipboardCut = false;

        for (int r : rows) {
            clipboard.add(tableModel.getFileAt(r));
        }

        JOptionPane.showMessageDialog(this, "Copiado(s): " + clipboard.size() + " elemento(s).");
    }

    private void cutSelectedFile() {
        int[] rows = fileTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }

        clipboard.clear();
        clipboardCut = true;

        for (int r : rows) {
            clipboard.add(tableModel.getFileAt(r));
        }

        JOptionPane.showMessageDialog(this, "Cortado(s): " + clipboard.size() + " elemento(s).");
    }

    private void pasteClipboard() {
        if (clipboard.isEmpty()) {
            return;
        }

        try {
            for (File f : clipboard) {

                File newLoc = new File(currentDir, f.getName());

                if (f.isDirectory()) {
                    copyDirectory(f, newLoc);
                } else {
                    Files.copy(f.toPath(), newLoc.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                if (clipboardCut) {
                    deleteDirectory(f);
                }
            }

            clipboard.clear();
            clipboardCut = false;
            updateFileTable(currentDir);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al pegar: " + ex.getMessage());
        }
    }

    //recursiva
    private void copyDirectory(File src, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }

        for (File f : src.listFiles()) {
            File newPath = new File(dest, f.getName());
            if (f.isDirectory()) {
                copyDirectory(f, newPath);
            } else {
                Files.copy(f.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    // ---------------- Editor de texto (simple) ----------------
    private void launchTextEditorWithFile(File f) {
        try {
            // Intento de obtener JDesktopPane
            Container parent = SwingUtilities.getAncestorOfClass(JDesktopPane.class, FileExplorerWindow.this);
            if (parent instanceof JDesktopPane) {
                JDesktopPane desktop = (JDesktopPane) parent;
                // Aquí deberías tener tu editor de texto como JInternalFrame.
                // Por simplicidad, usaremos un JInternalFrame con JTextArea.
                JInternalFrame editor = new JInternalFrame("Editor - " + f.getName(), true, true, true, true);
                JTextArea ta = new JTextArea();
                ta.setEditable(true);
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        ta.append(line + "\n");
                    }
                }
                editor.add(new JScrollPane(ta), BorderLayout.CENTER);
                JButton save = new JButton("Guardar");
                save.addActionListener(ev -> {
                    try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                        pw.print(ta.getText());
                        JOptionPane.showMessageDialog(editor, "Archivo guardado.");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(editor, "Error guardando: " + ex.getMessage());
                    }
                });
                editor.add(save, BorderLayout.SOUTH);
                editor.setSize(600, 400);
                desktop.add(editor, JLayeredPane.PALETTE_LAYER);
                editor.setVisible(true);
            } else {
                // fallback: abrir en notepad (o con Desktop)
                java.awt.Desktop.getDesktop().open(f);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir editor: " + ex.getMessage());
        }
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i == -1) {
            return "";
        }
        return name.substring(i + 1);
    }
}
