/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import exceptions.OperacionInvalidaException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.List;


// Excepciones personalizadas
import exceptions.CarpetanoEncontradaException;
import exceptions.PermisoDenegadoException;
import exceptions.ArchivoInvalidoException;
import exceptions.OperacionInvalidaException;

import reproductor.ReproductorGUI;


/**
 *
 * @author Nathan
 */
public class Desktop extends JFrame {

    private static final String BACKGROUND_IMAGE = "Imagenes/Fondo.png";
    public static final String Z_ROOT_PATH = "Z_ROOT" + File.separator;

    private final String[][] APPS = {
            {"Archivos", "🗂️"},
            {"Reproductor Musical", "🎵"},
            {"Texto", "📝"},
            {"Imágenes", "🖼️"},
            {"Consola", "🚀"},
            {"Admin", "👤"}
    };

    private final User currentUser;
    private JDesktopPane desktopPane;
    private JPopupMenu startMenu;

    private JPanel desktopIconPanel;
    private boolean iconsHidden = false;

    public Desktop(User user) {
        this.currentUser = user;
        setTitle("Mini-Windows Desktop - Sesión de: " + user.getUsername());

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screen.width;
        int screenHeight = screen.height;

        desktopPane = new JDesktopPane();

        // Fondo
        BackgroundPanel background = new BackgroundPanel(BACKGROUND_IMAGE);
        background.setBounds(0, 0, screenWidth, screenHeight);
        desktopPane.add(background, JLayeredPane.DEFAULT_LAYER);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBounds(0, 0, screenWidth, screenHeight);

        desktopPane.add(contentPanel, JLayeredPane.DRAG_LAYER);

        // Barra de tareas
        contentPanel.add(createModernTaskbar(), BorderLayout.SOUTH);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        iconPanel.setOpaque(false);

        iconPanel.add(createDesktopIcon("🗂️", "Archivos Personales", e -> launchFileExplorer()));
        iconPanel.add(createDesktopIcon("🎵", "Reproductor Musical", e -> launchMusicPlayer()));

        contentPanel.add(iconPanel, BorderLayout.NORTH);

        this.desktopIconPanel = iconPanel;

        add(desktopPane);

        revalidate();
        repaint();
    }

    private JPanel createModernTaskbar() {
        JPanel taskbar = new JPanel();
        taskbar.setBackground(new Color(38, 38, 38));
        taskbar.setPreferredSize(new Dimension(0, 40));
        taskbar.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        centerPanel.setOpaque(false);

        JButton startButton = new JButton("🪟");
        startButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        startButton.setBackground(new Color(0, 120, 215));
        startButton.setForeground(Color.WHITE);
        startButton.setPreferredSize(new Dimension(30, 30));
        startButton.setToolTipText("Menú de Inicio");
        startButton.addActionListener(e -> showStartMenu(startButton));
        centerPanel.add(startButton);

        JTextField searchBar = new JTextField("Buscar", 20);
        searchBar.setBackground(new Color(60, 60, 60));
        searchBar.setForeground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchBar.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("Buscar".equals(searchBar.getText())) searchBar.setText("");
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchBar.getText().trim().isEmpty()) searchBar.setText("Buscar");
            }
        });

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                showSearchSuggestions(searchBar);
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        searchBar.addActionListener(e -> {
            String q = searchBar.getText().trim().toLowerCase();
            if (q.isEmpty() || "buscar".equalsIgnoreCase(q)) return;
            for (String[] app : APPS) {
                if (app[0].toLowerCase().startsWith(q)) {
                    openAppByName(app[0]);
                    return;
                }
            }
            showSearchSuggestions(searchBar);
        });

        centerPanel.add(searchBar);

        centerPanel.add(createTaskbarIcon("🗂️", "Archivos", e -> launchFileExplorer()));
        centerPanel.add(createTaskbarIcon("🎵", "Música", e -> launchMusicPlayer()));

        taskbar.add(centerPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setOpaque(false);
        JLabel timeLabel = new JLabel(java.time.LocalTime.now().withNano(0).toString() + " | " + java.time.LocalDate.now().toString());
        timeLabel.setForeground(Color.WHITE);
        rightPanel.add(timeLabel);

        taskbar.add(rightPanel, BorderLayout.EAST);

        return taskbar;
    }

    private void showStartMenu(JButton source) {
        if (startMenu != null && startMenu.isShowing()) {
            startMenu.setVisible(false);
            return;
        }

        startMenu = new JPopupMenu();
        startMenu.setLayout(new BorderLayout());
        startMenu.setPreferredSize(new Dimension(350, 500));
        startMenu.setBackground(new Color(45, 45, 45));

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(35, 35, 35));
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel iconLabel;
        URL avatarUrl = getClass().getClassLoader().getResource("Imagenes/Avatar.png");
        if (avatarUrl != null) {
            ImageIcon avatar = new ImageIcon(new ImageIcon(avatarUrl).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            iconLabel = new JLabel(avatar);
        } else {
            iconLabel = new JLabel("👤", SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            iconLabel.setForeground(Color.WHITE);
        }

        JLabel name = new JLabel(currentUser.getUsername(), SwingConstants.LEFT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(Color.WHITE);

        userPanel.add(iconLabel, BorderLayout.WEST);
        userPanel.add(name, BorderLayout.CENTER);

        startMenu.add(userPanel, BorderLayout.NORTH);

        JPanel appGrid = new JPanel(new GridLayout(3, 3, 10, 10));
        appGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        appGrid.setBackground(new Color(45, 45, 45));

        appGrid.add(createAppMenuItem("🗂️", "Archivos", e -> { launchFileExplorer(); startMenu.setVisible(false); }));
        appGrid.add(createAppMenuItem("🎵", "Reproductor Musical", e -> { launchMusicPlayer(); startMenu.setVisible(false); }));
        appGrid.add(createAppMenuItem("📝", "Texto", e -> { launchTextEditor(); startMenu.setVisible(false); }));
        appGrid.add(createAppMenuItem("🖼️", "Imágenes", e -> { launchImageViewer(); startMenu.setVisible(false); }));
        appGrid.add(createAppMenuItem("🚀", "Consola", e -> { launchConsole(); startMenu.setVisible(false); }));
        appGrid.add(createAppMenuItem("👤", "Admin", e -> { JOptionPane.showMessageDialog(this, "Panel de Admin"); startMenu.setVisible(false); }));

        startMenu.add(appGrid, BorderLayout.CENTER);

        JButton logout = new JButton("⏻  Cerrar Sesión");
        logout.setBackground(new Color(150, 0, 0));
        logout.setForeground(Color.WHITE);
        logout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logout.addActionListener(e -> {
            confirmLogout();
            startMenu.setVisible(false);
        });

        startMenu.add(logout, BorderLayout.SOUTH);

        startMenu.show(source, source.getX(), -startMenu.getPreferredSize().height);
    }

    private void showSearchSuggestions(JTextField searchBar) {
        String query = searchBar.getText().trim().toLowerCase();
        if (query.isEmpty() || "buscar".equalsIgnoreCase(query)) return;

        JPopupMenu results = new JPopupMenu();
        results.setBackground(new Color(45, 45, 45));
        boolean found = false;

        for (String[] app : APPS) {
            String appName = app[0];
            if (appName.toLowerCase().contains(query)) {
                found = true;
                JMenuItem item = new JMenuItem(app[1] + " " + appName);
                item.setBackground(new Color(60, 60, 60));
                item.setForeground(Color.WHITE);
                item.addActionListener(e -> {
                    openAppByName(appName);
                    results.setVisible(false);
                });
                results.add(item);
            }
        }

        if (!found) {
            JMenuItem noMatch = new JMenuItem("No se encontraron resultados");
            noMatch.setBackground(Color.DARK_GRAY);
            noMatch.setForeground(Color.WHITE);
            results.add(noMatch);
        }

        results.show(searchBar, 0, searchBar.getHeight());
    }

    private void openAppByName(String appName) {
        switch (appName) {
            case "Archivos" -> launchFileExplorer();
            case "Reproductor Musical" -> launchMusicPlayer();
            case "Texto" -> launchTextEditor();
            case "Imágenes" -> launchImageViewer();
            case "Consola" -> launchConsole();
            case "Admin" -> JOptionPane.showMessageDialog(this, "Panel de Admin");
            default -> JOptionPane.showMessageDialog(this, "Aplicación no encontrada: " + appName);
        }
    }
    
private void launchFileExplorer() {
    JInternalFrame fileFrame = new JInternalFrame("Carpeta personal - Z:\\" + currentUser.getUsername(),
            true, true, true, true);

    int initialWidth = (int) (getWidth() * 0.55);
    int initialHeight = (int) (getHeight() * 0.55);
    fileFrame.setSize(initialWidth, initialHeight);

    fileFrame.setLocation(getWidth() / 2 - initialWidth / 2, getHeight() / 2 - initialHeight / 2);
    fileFrame.setLayout(new BorderLayout());

    JPanel contentIconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
    contentIconPanel.setBackground(Color.WHITE);
    JScrollPane contentScrollPane = new JScrollPane(contentIconPanel);

    JTree fileTree = createSimulatedFileTree();
    JScrollPane treeScrollPane = new JScrollPane(fileTree);
    treeScrollPane.setPreferredSize(new Dimension(200, 400));
    fileFrame.add(treeScrollPane, BorderLayout.WEST);

    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    toolbar.add(new JButton("📁 Nueva Carpeta"));
    toolbar.add(new JButton("📄 Nuevo Archivo"));
    toolbar.add(new JButton("Organizar"));
    toolbar.add(new JButton("Ordenar por..."));
    fileFrame.add(toolbar, BorderLayout.NORTH);

    fileFrame.add(contentScrollPane, BorderLayout.CENTER);

    String initialPath = Z_ROOT_PATH + currentUser.getUsername();
    try {
        updateContentPanel(contentIconPanel, initialPath);
    } catch (CarpetanoEncontradaException | PermisoDenegadoException | OperacionInvalidaException ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        contentIconPanel.add(new JLabel("No se pudo cargar la carpeta: " + initialPath));
    }

    fileTree.addTreeSelectionListener(new TreeSelectionListener() {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
            if (node == null) return;

            String folderName = node.getUserObject().toString();
            TreePath path = e.getPath();
            String currentDir = Z_ROOT_PATH;

            if (path.getPathCount() > 1) {
                for (int i = 1; i < path.getPathCount(); i++) {
                    currentDir += path.getPathComponent(i).toString() + File.separator;
                }
            } else {
                currentDir = initialPath;
            }

            try {
                updateContentPanel(contentIconPanel, currentDir);
            } catch (CarpetanoEncontradaException | PermisoDenegadoException | OperacionInvalidaException ex) {
                JOptionPane.showMessageDialog(Desktop.this, "Error: " + ex.getMessage());
            }

            fileFrame.setTitle("Archivos - " + folderName);
        }
    });

    fileFrame.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            JInternalFrame frame = (JInternalFrame) e.getComponent();
            if (frame.isMaximum()) {
                hideDesktopIcons();
                frame.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight() - 40);
            } else {
                showDesktopIcons();
            }
        }

        @Override
        public void componentShown(ComponentEvent e) {
            if (fileFrame.isMaximum()) hideDesktopIcons();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            showDesktopIcons();
        }
    });

    fileFrame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
        @Override
        public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) {
            if (fileFrame.isMaximum()) hideDesktopIcons();
        }

        @Override
        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent e) {
            boolean anyMax = false;
            for (JInternalFrame f : desktopPane.getAllFrames()) {
                if (f.isMaximum() && f != fileFrame) { 
                    anyMax = true; 
                    break; 
                }
            }
            if (!anyMax) showDesktopIcons();
        }
        
        @Override
        public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
            boolean anyMax = false;
            for (JInternalFrame f : desktopPane.getAllFrames()) {
                if (f.isMaximum()) { 
                    anyMax = true; 
                    break; 
                }
            }
            
            if (!anyMax) {
                showDesktopIcons();
            }
        }
    });

    desktopPane.add(fileFrame, JLayeredPane.PALETTE_LAYER);
    fileFrame.setVisible(true);
    try {
        fileFrame.setSelected(true);
    } catch (java.beans.PropertyVetoException ignored) {
    }
}

    private void launchTextEditor() {
        JOptionPane.showMessageDialog(this, "Lanzando Editor de Texto (Req. 4)...");
    }

    private void launchImageViewer() {
        JOptionPane.showMessageDialog(this, "Lanzando Visor de Imágenes (Req. 5)...");
    }

    private void launchConsole() {
        JOptionPane.showMessageDialog(this, "Lanzando Consola de Comandos (Req. 6)...");
    }

    private void launchMusicPlayer() {
        try {
            new ReproductorGUI();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo iniciar el reproductor: " + ex.getMessage());
        }
    }

    
    private void updateContentPanel(JPanel panel, String path)
            throws CarpetanoEncontradaException, PermisoDenegadoException, OperacionInvalidaException {

        panel.removeAll();

        File currentDir = new File(path);

        if (!currentDir.exists() || !currentDir.isDirectory()) {
            if (path.startsWith(Z_ROOT_PATH)) {
                try {
                    boolean created = currentDir.mkdirs();
                    if (!created) {
                        throw new PermisoDenegadoException("No se pudo crear la carpeta (permiso denegado): " + path);
                    }
                } catch (SecurityException se) {
                    throw new PermisoDenegadoException("Permiso denegado al intentar crear la carpeta: " + path);
                } catch (Exception ex) {
                    throw new OperacionInvalidaException("Error al intentar acceder/crear la carpeta: " + ex.getMessage());
                }
            } else {
                throw new CarpetanoEncontradaException("Carpeta no encontrada: " + path);
            }
        }

        File[] files = currentDir.listFiles();
        if (files == null) {
            throw new PermisoDenegadoException("No se pueden listar los archivos en: " + path);
        }

        List<File> fileList = Arrays.asList(files);
        fileList.sort((f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });

        for (File file : fileList) {
            if (file.isDirectory()) {
                panel.add(createDesktopIcon("📁", file.getName(), e -> {
                    try {
                        updateContentPanel(panel, file.getAbsolutePath());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Desktop.this, "Error: " + ex.getMessage());
                    }
                }));
            } else {
                if (file.getName().contains("..")) {
                    panel.add(createFileIcon(file.getName() + " (nombre inválido)"));
                } else {
                    panel.add(createFileIcon(file.getName()));
                }
            }
        }

        panel.revalidate();
        panel.repaint();
    }

    private JTree createSimulatedFileTree() {
        DefaultMutableTreeNode top;

        if (currentUser.isAdmin()) {
            top = new DefaultMutableTreeNode("Z:\\ (Admin)");
            List<User> users = UserManager.getUsers();
            for (User user : users) {
                top.add(createDirectoryNode(user.getUsername()));
            }
        } else {
            String userPath = currentUser.getUsername();
            top = new DefaultMutableTreeNode("Z:\\" + userPath);

            File userRoot = new File(Z_ROOT_PATH + userPath);
            if (!userRoot.exists()) userRoot.mkdirs();

            File[] files = userRoot.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        top.add(new DefaultMutableTreeNode(file.getName()));
                    }
                }
            }
        }
        return new JTree(top);
    }

    private DefaultMutableTreeNode createDirectoryNode(String username) {
        DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(username);
        File userDir = new File(Z_ROOT_PATH + username);

        if (!userDir.exists()) userDir.mkdirs();

        if (userDir.exists() && userDir.isDirectory()) {
            File[] files = userDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        userNode.add(new DefaultMutableTreeNode(file.getName()));
                    }
                }
            }
        }
        return userNode;
    }


    private JPanel createDesktopIcon(String emoji, String name, java.awt.event.ActionListener doubleClickListener) {
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(100, 80));

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(Color.WHITE);

        iconPanel.add(iconLabel, BorderLayout.CENTER);
        iconPanel.add(nameLabel, BorderLayout.SOUTH);

        if (doubleClickListener != null) {
            iconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2 && !iconsHidden) {
                        doubleClickListener.actionPerformed(new java.awt.event.ActionEvent(iconPanel, java.awt.event.ActionEvent.ACTION_PERFORMED, null));
                    }
                }
            });
        }

        return iconPanel;
    }

    private JButton createTaskbarIcon(String emoji, String tooltip, ActionListener listener) {
        JButton button = new JButton(emoji);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        button.setToolTipText(tooltip);
        button.setBackground(new Color(38, 38, 38));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setPreferredSize(new Dimension(30, 30));
        button.addActionListener(listener);
        return button;
    }

    private JPanel createAppMenuItem(String emoji, String name, ActionListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 60, 60));
        panel.setPreferredSize(new Dimension(80, 80));

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        iconLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(Color.WHITE);

        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                listener.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, ""));
            }
        });

        return panel;
    }

    private JPanel createFileIcon(String name) {
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(100, 80));

        String emoji = "📄";
        if (name.toLowerCase().endsWith(".mp3")) {
            emoji = "🎵";
        } else if (name.toLowerCase().endsWith(".txt")) {
            emoji = "📝";
        } else if (name.toLowerCase().matches(".*\\.(jpg|png|gif|jpeg)$")) {
            emoji = "🖼️";
        } else if (name.toLowerCase().endsWith(".pdf")) {
            emoji = "📑";
        }

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        iconPanel.add(iconLabel, BorderLayout.CENTER);
        iconPanel.add(nameLabel, BorderLayout.SOUTH);

        return iconPanel;
    }

    private void confirmLogout() {
        int result = JOptionPane.showConfirmDialog(
                Desktop.this,
                "¿Desea cerrar sesión de " + currentUser.getUsername() + "?",
                "Confirmar Cierre",
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            // new Login().setVisible(true);
        }
    }
    

    /** Fondo */
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String path) {
            try {
                URL imageUrl = getClass().getClassLoader().getResource(path);

                if (imageUrl != null) {
                    backgroundImage = new ImageIcon(imageUrl).getImage();
                } else {
                    backgroundImage = new ImageIcon(path).getImage();
                }
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen de fondo: " + e.getMessage());
                backgroundImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(25, 25, 112));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void hideDesktopIcons() {
        if (!iconsHidden && desktopIconPanel != null) {
            desktopIconPanel.setVisible(false);
            iconsHidden = true;
        }
    }

    private void showDesktopIcons() {
        if (iconsHidden && desktopIconPanel != null) {
            desktopIconPanel.setVisible(true);
            iconsHidden = false;
        }
    }
}
