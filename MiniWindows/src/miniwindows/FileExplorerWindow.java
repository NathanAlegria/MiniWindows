/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import EditordeTexto.EditorTexto;
import reproductor.ReproductorGUI;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Nathan
 */
public class FileExplorerWindow extends JInternalFrame {

    private JTree folderTree;
    private JTable fileTable;
    private FileTableModel tableModel;
    private File currentDir;
    private final User currentUser;
    private final File rootFolder;

    private JComboBox<String> sortCombo;
    private JTextField searchField;
    private final List<File> clipboard = new ArrayList<>();
    private boolean clipboardCut = false;

    private JButton btnUploadImage;

    public FileExplorerWindow(User user) {
        super("Explorador - " + user.getUsername(), true, true, true, true);
        this.currentUser = user;
        this.rootFolder = new File(Desktop.Z_ROOT_PATH + user.getUsername());
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }

        DesktopHelper.cleanUserDirectory(user.getUsername()); // Llama a la limpieza antes de inicializar
        initializeUserDirectory(); 

        setSize(900, 600);
        setLocation(50, 50);
        setLayout(new BorderLayout());

        initToolbar();
        initSplitPane();

        currentDir = rootFolder;
        updateFileTable(currentDir);
    }

    private void initializeUserDirectory() {
        try {
            if (!rootFolder.exists()) {
                rootFolder.mkdirs();
            }

            File docs = new File(rootFolder, "Mis Documentos");
            File music = new File(rootFolder, "Música");
            File images = new File(rootFolder, "Mis Imágenes");

            docs.mkdirs();
            music.mkdirs();
            images.mkdirs();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inicializando directorio de usuario: " + e.getMessage());
        }
    }

    private void initToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton btnNew = new JButton("Nuevo");
        JButton btnRename = new JButton("Renombrar");
        JButton btnDelete = new JButton("Eliminar");
        JButton btnOrganize = new JButton("Organizar");
        JButton btnCopy = new JButton("Copiar");
        JButton btnCut = new JButton("Cortar");
        JButton btnPaste = new JButton("Pegar");

        toolbar.add(btnNew);
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
        toolbar.add(btnCut);
        toolbar.add(btnPaste);

        btnUploadImage = new JButton("Subir Imagen");
        btnUploadImage.setVisible(false);
        btnUploadImage.addActionListener(e -> uploadImage());
        toolbar.addSeparator(new Dimension(15, 0));
        toolbar.add(btnUploadImage);

        add(toolbar, BorderLayout.NORTH);

        btnNew.addActionListener(e -> showCreateDialog());
        btnRename.addActionListener(e -> renameSelectedFile());
        btnDelete.addActionListener(e -> deleteSelectedFile());
        btnOrganize.addActionListener(e -> organizeFilesInCurrentDir());
        btnCopy.addActionListener(e -> copySelectedFile());
        btnCut.addActionListener(e -> cutSelectedFile());
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
    }

    private void initSplitPane() {
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
        fileTable.setDefaultRenderer(Object.class, new FileTableCellRenderer());

        fileTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = fileTable.getSelectedRow();
                    if (row < 0) {
                        return;
                    }
                    openFile(tableModel.getFileAt(row));
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(folderTree);
        JScrollPane tableScroll = new JScrollPane(fileTable);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, tableScroll);
        splitPane.setDividerLocation(240);
        add(splitPane, BorderLayout.CENTER);
    }

    private void showCreateDialog() {
        if (currentDir == null || !currentDir.exists()) {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] options = {"Carpeta", "Archivo", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(this,
                "¿Qué desea crear?", "Crear Nuevo",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) { // Carpeta
            String name = JOptionPane.showInputDialog(this, "Ingrese el nombre de la nueva carpeta:");
            if (name != null && !name.trim().isEmpty()) {
                File newDir = new File(currentDir, name.trim());
                if (newDir.mkdirs()) {
                    updateFileTable(currentDir);
                    refreshTreeModel(); 
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo crear la carpeta.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (choice == 1) { // Archivo de cualquier tipo
            String name = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo (ej. notas.txt o cualquier extensión):");
            if (name != null && !name.trim().isEmpty()) {
                File newFile = new File(currentDir, name.trim());
                try {
                    if (newFile.createNewFile()) {
                        updateFileTable(currentDir);
                    } else {
                        JOptionPane.showMessageDialog(this, "El archivo ya existe o no pudo crearse.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error creando archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void updateFileTable(File dir) {
        if (dir == null || !dir.exists()) {
            tableModel.setFiles(List.of());
            checkIfImagesFolder();
            return;
        }

        checkIfImagesFolder();

        File[] files = dir.listFiles();
        if (files == null) {
            tableModel.setFiles(List.of());
            return;
        }

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<File> list = Arrays.stream(files)
                .filter(f -> q.isEmpty() || f.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());

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

    private void checkIfImagesFolder() {
        if (currentDir == null) {
            btnUploadImage.setVisible(false);
            return;
        }
        String nombre = currentDir.getName().toLowerCase();
        btnUploadImage.setVisible(nombre.equals("mis imágenes") || nombre.equals("imagenes") || nombre.equals("imágenes"));
    }

    private void uploadImage() {
        if (currentDir == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(
                new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg", "gif", "bmp")
        );

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        for (File file : chooser.getSelectedFiles()) {
            try {
                File destino = new File(currentDir, file.getName());
                Files.copy(file.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error copiando imagen: " + ex.getMessage());
            }
        }

        updateFileTable(currentDir);
        JOptionPane.showMessageDialog(this, "Imágenes subidas correctamente.");
    }

    private void openFile(File f) {
        String name = f.getName().toLowerCase();
        Container parent = SwingUtilities.getAncestorOfClass(JDesktopPane.class, this);
        if (parent instanceof JDesktopPane desktop) {
            try {
                if (f.isDirectory()) {
                    currentDir = f;
                    updateFileTable(f);
                } else if (name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".java") || name.endsWith(".py") || name.endsWith(".log")) {
                    EditorTexto editor = new EditorTexto(currentUser);
                    editor.openFile(f);
                    desktop.add(editor, JLayeredPane.PALETTE_LAYER);
                    editor.setVisible(true);
                    editor.setSelected(true);
                } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".flac")) {
                    ReproductorGUI rep = null;
                    for (JInternalFrame frame : desktop.getAllFrames()) {
                        if (frame instanceof ReproductorGUI r
                                && r.getCurrentUser().getUsername().equals(currentUser.getUsername())) {
                            rep = r;
                            break;
                        }
                    }
                    if (rep == null) {
                        rep = new ReproductorGUI(currentUser);
                        desktop.add(rep);
                        cascadeFrames(desktop);
                        rep.setVisible(true);
                    }
                    rep.loadFromFile(f);
                    rep.setSelected(true);
                } else if (name.matches(".*\\.(png|jpg|jpeg|gif|bmp)")) {
                    File[] files = currentDir.listFiles(file -> file.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)"));
                    List<File> imageList = files == null ? List.of() : List.of(files);

                    ImageViewer viewer = new ImageViewer(imageList);
                    desktop.add(viewer);
                    viewer.setVisible(true);
                    viewer.setSelected(true);
                } else {
                    int openText = JOptionPane.showConfirmDialog(this, "Tipo no reconocido. ¿Abrir con editor de texto?", "Abrir archivo", JOptionPane.YES_NO_OPTION);
                    if (openText == JOptionPane.YES_OPTION) {
                        EditorTexto editor = new EditorTexto(currentUser);
                        editor.openFile(f);
                        desktop.add(editor, JLayeredPane.PALETTE_LAYER);
                        editor.setVisible(true);
                        editor.setSelected(true);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir archivo: " + ex.getMessage());
            }
        }
    }

    private void cascadeFrames(JDesktopPane desktop) {
        int x = 20, y = 20, offset = 30;
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

    private String buildPathFromNode(DefaultMutableTreeNode node) {
        Object[] nodes = node.getPath();
        StringBuilder path = new StringBuilder();
        
        if (currentUser.isAdmin()) {
            path.append(Desktop.Z_ROOT_PATH);
            
            if (nodes.length == 1) {
                 return path.toString();
            }
            
            for (int i = 1; i < nodes.length; i++) {
                path.append(File.separator).append(nodes[i].toString());
            }
            
        } else {
            path.append(rootFolder.getAbsolutePath());
            for (int i = 1; i < nodes.length; i++) {
                path.append(File.separator).append(nodes[i].toString());
            }
        }
        
        return path.toString();
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
        File f = tableModel.getFileAt(row);
        if (f.isDirectory()) {
            deleteDirectory(f);
        } else {
            f.delete();
        }
        updateFileTable(currentDir);
        refreshTreeModel();
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

    private void organizeFilesInCurrentDir() {
        File docs = new File(currentDir, "Mis Documentos"); 
        File music = new File(currentDir, "Música");
        File images = new File(currentDir, "Mis Imágenes");
        
        docs.mkdirs();
        music.mkdirs();
        images.mkdirs();

        File[] files = currentDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    String ext = f.getName().toLowerCase();
                    boolean organized = false;
                    try {
                        if (ext.endsWith(".txt") || ext.endsWith(".pdf") || ext.endsWith(".docx") || ext.endsWith(".doc") || ext.endsWith(".md") || ext.endsWith(".java") || ext.endsWith(".py") || ext.endsWith(".log")) {
                            Files.move(f.toPath(), new File(docs, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            organized = true;
                        } else if (ext.endsWith(".mp3") || ext.endsWith(".wav") || ext.endsWith(".flac") || ext.endsWith(".ogg")) {
                            Files.move(f.toPath(), new File(music, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            organized = true;
                        } else if (ext.matches(".*\\.(png|jpg|jpeg|gif|bmp)")) {
                            Files.move(f.toPath(), new File(images, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            organized = true;
                        }

                        if (!organized) { 
                            Files.move(f.toPath(), new File(docs, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                    } catch (IOException ignored) {
                    }
                }
            }
        }
        updateFileTable(currentDir);
        JOptionPane.showMessageDialog(this, "Archivos organizados correctamente.");
        refreshTreeModel();
    }

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
        if (clipboard.isEmpty() || currentDir == null) {
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
            refreshTreeModel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al pegar: " + ex.getMessage());
        }
    }

    private void copyDirectory(File src, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        for (File f : Objects.requireNonNull(src.listFiles())) {
            File newPath = new File(dest, f.getName());
            if (f.isDirectory()) {
                copyDirectory(f, newPath);
            } else {
                Files.copy(f.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void refreshTreeModel() {
        folderTree = DesktopHelper.createSimulatedFileTree(currentUser);
        Container parent = getContentPane();
        if (parent != null) {
            Component[] comps = parent.getComponents();
            for (Component c : comps) {
                if (c instanceof JSplitPane sp) {
                    JScrollPane left = new JScrollPane(folderTree);
                    sp.setLeftComponent(left);
                    folderTree.setPreferredSize(new Dimension(240, 0));
                    folderTree.addTreeSelectionListener(e -> {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) folderTree.getLastSelectedPathComponent();
                        if (node != null) {
                            currentDir = new File(buildPathFromNode(node));
                            updateFileTable(currentDir);
                        }
                    });
                    break;
                }
            }
        }
    }

    private static class FileTableCellRenderer extends DefaultTableCellRenderer {

        private final FileSystemView fsv = FileSystemView.getFileSystemView();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            FileTableModel model = (FileTableModel) table.getModel();
            File file = model.getFileAt(row);
            if (column == 0) {
                label.setIcon(fsv.getSystemIcon(file));
            }
            return label;
        }
    }
}