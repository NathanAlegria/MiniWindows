/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Nathan
 */

/**
 * Simulación de un navegador de archivos con JTree, incluyendo funcionalidades
 * de creación de archivos/documentos, ordenamiento, organización y búsqueda.
 */
public class FileExplorer extends JPanel {

    // --- Variables Globales ---
    private final String userRootPath; // La ruta raíz del usuario, e.g., "Z:\nathan"
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private JTextField searchField;
    private File currentSelectedDirectory;

    // --- Tipos de Ordenamiento ---
    private enum SortType {
        NAME_ASC, DATE_DESC, TYPE_ASC, SIZE_DESC
    }

    private SortType currentSort = SortType.NAME_ASC;

    // --- Constructor ---
    public FileExplorer(String username) {
        // La ruta raíz del usuario, se debe asegurar que esta carpeta exista
        this.userRootPath = "Z:" + File.separator + username; 
        
        setLayout(new BorderLayout());
        
        // Inicializar la estructura de directorios si no existe
        initializeUserDirectory(username);

        // Inicializar la interfaz gráfica
        createToolbar();
        createFileTree();

        // Inicializar la ruta seleccionada por defecto
        this.currentSelectedDirectory = new File(this.userRootPath);

        JScrollPane scrollPane = new JScrollPane(fileTree);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Crea los directorios básicos para el nuevo usuario (Mis Documentos, Música, Mis Imágenes).
     * @param username El nombre del usuario.
     */
    private void initializeUserDirectory(String username) {
        Path rootPath = Paths.get("Z:", username);
        try {
            // Crear el directorio raíz del usuario
            Files.createDirectories(rootPath);
            System.out.println("Directorio raíz creado: " + rootPath);

            // Crear carpetas básicas
            String[] defaultFolders = {"Mis Documentos", "Música", "Mis Imágenes"};
            for (String folder : defaultFolders) {
                Files.createDirectories(rootPath.resolve(folder));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al inicializar el directorio de usuario: " + e.getMessage(), "Error de Archivos", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // --- 1. Funcionalidad de Creación de Archivos/Carpetas ---

    /**
     * Maneja la creación de nuevos archivos o directorios.
     */
    private void handleNew() {
        if (currentSelectedDirectory == null || !currentSelectedDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta válida para crear el nuevo elemento.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] options = {"Carpeta", "Documento (.txt)", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(this,
                "¿Qué desea crear?", "Crear Nuevo",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) { // Carpeta
            String name = JOptionPane.showInputDialog(this, "Ingrese el nombre de la nueva carpeta:");
            if (name != null && !name.trim().isEmpty()) {
                File newDir = new File(currentSelectedDirectory, name.trim());
                if (newDir.mkdir()) {
                    reloadTree(currentSelectedDirectory);
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo crear la carpeta.", "Error de Creación", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (choice == 1) { // Documento (.txt)
            String name = JOptionPane.showInputDialog(this, "Ingrese el nombre del nuevo documento (sin extensión):");
            if (name != null && !name.trim().isEmpty()) {
                File newFile = new File(currentSelectedDirectory, name.trim() + ".txt");
                try {
                    if (newFile.createNewFile()) {
                        reloadTree(currentSelectedDirectory);
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "No se pudo crear el archivo: " + e.getMessage(), "Error de Creación", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // --- 2. y 3. Interfaz de Ordenamiento, Organización y Búsqueda ---
    private void createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // --- Botón de Crear Nuevo ---
        JButton newBtn = new JButton("Nuevo");
        newBtn.setToolTipText("Crear nueva Carpeta o Documento (.txt)");
        newBtn.addActionListener(e -> handleNew());
        toolbar.add(newBtn);
        toolbar.addSeparator();

        // --- Menú de Ordenar Por ---
        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"Ordenar por Nombre", "Ordenar por Fecha", "Ordenar por Tipo", "Ordenar por Tamaño"});
        sortCombo.addActionListener(e -> {
            switch (sortCombo.getSelectedIndex()) {
                case 0: currentSort = SortType.NAME_ASC; break;
                case 1: currentSort = SortType.DATE_DESC; break;
                case 2: currentSort = SortType.TYPE_ASC; break;
                case 3: currentSort = SortType.SIZE_DESC; break;
            }
            reloadTree(currentSelectedDirectory);
        });
        toolbar.add(sortCombo);
        
        // --- Botón de Organizar ---
        JButton organizeBtn = new JButton("Organizar");
        organizeBtn.setToolTipText("Mover archivos al tipo de carpeta correspondiente (Imágenes, Documentos, Música)");
        organizeBtn.addActionListener(e -> organizeFiles());
        toolbar.add(organizeBtn);
        toolbar.addSeparator();

        // --- Campo de Búsqueda ---
        searchField = new JTextField(15);
        searchField.putClientProperty("JComponent.roundRect", Boolean.TRUE); // Estilo visual
        searchField.setToolTipText("Buscar archivos que comiencen con...");
        
        // Listener para la búsqueda
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTree(); }
            public void removeUpdate(DocumentEvent e) { filterTree(); }
            public void changedUpdate(DocumentEvent e) { filterTree(); }
        });
        
        toolbar.add(new JLabel(" Buscar: "));
        toolbar.add(searchField);

        add(toolbar, BorderLayout.NORTH);
    }
    
    // --- Lógica de Navegador (JTree) ---

    private void createFileTree() {
        File rootFile = new File(userRootPath);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootFile);
        treeModel = new DefaultTreeModel(root);
        fileTree = new JTree(treeModel);

        // Ocultar la raíz por defecto para que solo se vea el contenido del usuario
        fileTree.setRootVisible(true); 
        fileTree.setShowsRootHandles(true);

        // Listener para actualizar la carpeta seleccionada y recargar con ordenamiento
        fileTree.getSelectionModel().addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path != null) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (selectedNode.getUserObject() instanceof File) {
                    File selectedFile = (File) selectedNode.getUserObject();
                    if (selectedFile.isDirectory()) {
                        currentSelectedDirectory = selectedFile;
                        // Recargar la vista del directorio seleccionado, aplicando orden y filtro si es necesario
                        reloadTree(currentSelectedDirectory); 
                    }
                }
            }
        });

        // Cargar el contenido inicial
        loadDirectory(root, rootFile);
    }

    /**
     * Carga el contenido de un directorio en el JTree.
     * @param parentNode El nodo padre en el JTree.
     * @param directory El objeto File del directorio.
     */
    private void loadDirectory(DefaultMutableTreeNode parentNode, File directory) {
        parentNode.removeAllChildren();
        
        // Obtener los archivos y aplicar el filtro de búsqueda
        File[] files = directory.listFiles();
        if (files == null) return;

        // Aplicar la lógica de ordenamiento
        List<File> fileList = Arrays.asList(files);
        fileList.sort(getFileComparator());
        
        // Aplicar el filtro de búsqueda
        String searchText = searchField.getText().toLowerCase().trim();
        List<File> filteredList = fileList.stream()
            .filter(file -> file.getName().toLowerCase().startsWith(searchText) || searchText.isEmpty())
            .collect(Collectors.toList());

        // Recargar los nodos visibles
        for (File file : filteredList) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
            parentNode.add(node);
            
            // Añadir un nodo dummy para el manejador de expansión (lazy loading)
            if (file.isDirectory()) {
                node.add(new DefaultMutableTreeNode(null));
            }
        }
        treeModel.reload(parentNode);
    }

    /**
     * Define el comparador basado en el tipo de ordenamiento actual.
     */
    private Comparator<File> getFileComparator() {
        Comparator<File> comparator;

        switch (currentSort) {
            case DATE_DESC:
                // Por fecha (el más reciente primero)
                comparator = Comparator.comparing(File::lastModified).reversed();
                break;
            case TYPE_ASC:
                // Por tipo (extensiones, luego carpetas)
                comparator = Comparator.comparing((File f) -> f.isDirectory() ? 0 : 1)
                                      .thenComparing(f -> getFileExtension(f).toLowerCase());
                break;
            case SIZE_DESC:
                // Por tamaño (el más grande primero)
                comparator = Comparator.comparing(File::length).reversed();
                break;
            case NAME_ASC:
            default:
                // Por nombre (alfabético)
                comparator = Comparator.comparing(File::getName);
                break;
        }

        // Asegura que las carpetas siempre se muestren primero, independientemente del criterio secundario
        return Comparator.comparing(File::isDirectory).reversed().thenComparing(comparator);
    }

    /**
     * Recarga el JTree desde la carpeta raíz o la carpeta actualmente seleccionada.
     */
    private void reloadTree(File directoryToReload) {
        // En un JTree simple, lo más fácil es reconstruir el nodo del directorio seleccionado
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        // Encuentra el nodo correspondiente al directorio (solo si no es la raíz)
        DefaultMutableTreeNode targetNode = findNode(root, directoryToReload);
        
        if (targetNode != null) {
            loadDirectory(targetNode, directoryToReload);
        } else {
             // Si no se encuentra, reconstruir desde la raíz
            loadDirectory(root, new File(userRootPath));
        }
        
        // Asegurarse de expandir el nodo para que los cambios sean visibles
        fileTree.expandPath(new TreePath(targetNode.getPath()));
    }
    
    /**
     * Busca un nodo en el árbol que contenga el objeto File dado.
     */
    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode root, File file) {
        if (root.getUserObject().equals(file)) {
            return root;
        }
        Enumeration<TreeNode> children = root.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (child.getUserObject() instanceof File && child.getUserObject().equals(file)) {
                return child;
            }
            // Si el directorio tiene una estructura profunda, se podría buscar recursivamente.
        }
        return root.getUserObject().equals(new File(userRootPath)) ? root : null;
    }

    // --- Lógica de Búsqueda (Filtro) ---

    private void filterTree() {
        // La lógica de filtrado se maneja en loadDirectory,
        // así que simplemente recargamos el directorio raíz para aplicar el filtro.
        reloadTree(new File(userRootPath)); 
    }
    
    // --- Lógica de Organización ---
    
    /**
     * Mueve archivos del directorio actual a las carpetas predefinidas (Documentos, Imágenes, Música).
     */
    private void organizeFiles() {
        if (currentSelectedDirectory == null || !currentSelectedDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una carpeta para organizar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] files = currentSelectedDirectory.listFiles();
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "La carpeta no contiene archivos para organizar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int movedCount = 0;
        File docDir = new File(userRootPath + File.separator + "Mis Documentos");
        File imgDir = new File(userRootPath + File.separator + "Mis Imágenes");
        File musicDir = new File(userRootPath + File.separator + "Música");
        
        // Crear directorios de destino si no existen (deberían existir por initializeUserDirectory)
        docDir.mkdirs();
        imgDir.mkdirs();
        musicDir.mkdirs();

        for (File file : files) {
            if (file.isFile()) {
                String extension = getFileExtension(file);
                File destination = null;

                if (extension.matches("(?i)txt|pdf|docx?|xlsx?|pptx?")) {
                    destination = docDir;
                } else if (extension.matches("(?i)jpg|jpeg|png|gif|bmp")) {
                    destination = imgDir;
                } else if (extension.matches("(?i)mp3|wav|ogg|flac")) {
                    destination = musicDir;
                }

                if (destination != null && !file.getParentFile().equals(destination)) {
                    try {
                        Path source = file.toPath();
                        Path target = destination.toPath().resolve(file.getName());
                        Files.move(source, target);
                        movedCount++;
                    } catch (IOException e) {
                        System.err.println("Error al mover el archivo " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
        
        if (movedCount > 0) {
            JOptionPane.showMessageDialog(this, movedCount + " archivos fueron movidos a sus carpetas correspondientes.", "Organización Exitosa", JOptionPane.INFORMATION_MESSAGE);
            reloadTree(currentSelectedDirectory); // Recarga para ver los cambios
        } else {
            JOptionPane.showMessageDialog(this, "No se encontraron archivos para organizar en las carpetas predefinidas.", "Organización", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Obtiene la extensión de un archivo (sin el punto).
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // No tiene extensión
        }
        return name.substring(lastIndexOf + 1);
    }

    // --- Método Main de Prueba ---
    public static void main(String[] args) {
        // Configuración de la unidad Z:\ para la simulación
        File rootSim = new File("Z:");
        if (!rootSim.exists()) {
             // Simulación de la unidad Z:\ en el directorio de ejecución actual para prueba
            rootSim = new File("TestRoot"); 
            System.out.println("Usando directorio local para simular Z:\\: " + rootSim.getAbsolutePath());
        }
        
        // Simular el directorio del usuario 'Nathan'
        String testUsername = "Nathan";

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("File Explorer - Usuario: " + testUsername);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new FileExplorer(testUsername));
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
