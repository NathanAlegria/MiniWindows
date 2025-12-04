/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import CMD.CMD_GUI;
import EditordeTexto.EditorTexto;
import VisorImagenes.VisorImagenes;
import reproductor.ReproductorGUI;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
        {"Consola", "🚀"},
        {"Visor de Imágenes", "🖼️"}
    };

    private final User currentUser;
    private JDesktopPane desktopPane;
    private JPopupMenu startMenu;
    private JPanel desktopIconPanel;
    private JLabel timeLabel;
    private javax.swing.Timer clockTimer;

    private int cascadeX = 30;
    private int cascadeY = 30;
    private final int cascadeStep = 30;

    private JPanel taskbarAppPanel;
    private Map<JInternalFrame, JButton> frameButtonMap = new HashMap<>();

    public Desktop(User user) {
        this.currentUser = user;
        setTitle("Mini-Windows Desktop - Sesión de: " + user.getUsername());

        setUndecorated(true);
        setResizable(false);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        desktopPane = new JDesktopPane();

        BackgroundPanel background = new BackgroundPanel(BACKGROUND_IMAGE);
        background.setBounds(0, 0, screen.width, screen.height);
        desktopPane.add(background, JLayeredPane.DEFAULT_LAYER);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBounds(0, 0, screen.width, screen.height);
        desktopPane.add(contentPanel, JLayeredPane.DRAG_LAYER);

        taskbarAppPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        taskbarAppPanel.setOpaque(false);

        contentPanel.add(createModernTaskbar(), BorderLayout.SOUTH);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        iconPanel.setOpaque(false);

        iconPanel.add(createDesktopIcon("🗂️", "Archivos", e -> launchFileExplorer()));
        iconPanel.add(createDesktopIcon("🎵", "Reproductor", e -> launchMusicPlayer()));
        iconPanel.add(createDesktopIcon("📝", "Texto", e -> launchTextEditor()));
        iconPanel.add(createDesktopIcon("🚀", "Consola", e -> launchConsole()));
        iconPanel.add(createDesktopIcon("🖼️", "Visor de Imágenes", e -> launchImageViewer()));

        contentPanel.add(iconPanel, BorderLayout.NORTH);
        this.desktopIconPanel = iconPanel;

        add(desktopPane);
        startClockTimer();
    }

    //Barra de tareas
    private JPanel createModernTaskbar() {
        JPanel taskbar = new JPanel(new BorderLayout());
        taskbar.setBackground(new Color(38, 38, 38));
        taskbar.setPreferredSize(new Dimension(getWidth(), 40));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setOpaque(false);

        JButton startButton = new JButton("🪟");
        startButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        startButton.setBackground(new Color(0, 120, 215));
        startButton.setForeground(Color.WHITE);
        startButton.setPreferredSize(new Dimension(30, 30));
        startButton.addActionListener(e -> showStartMenu(startButton));
        leftPanel.add(startButton);

        // Barra de búsqueda
        JTextField searchBar = new JTextField("Buscar", 20);
        searchBar.setBackground(new Color(60, 60, 60));
        searchBar.setForeground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchBar.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if ("Buscar".equals(searchBar.getText())) {
                    searchBar.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (searchBar.getText().trim().isEmpty()) {
                    searchBar.setText("Buscar");
                }
            }
        });
        searchBar.addActionListener(e -> {
            String q = searchBar.getText().trim().toLowerCase();
            if (q.isEmpty() || "buscar".equalsIgnoreCase(q)) {
                return;
            }

            for (String[] app : APPS) {
                if (app[0].toLowerCase().startsWith(q)) {
                    openAppByName(app[0]);
                    return;
                }
            }
        });
        leftPanel.add(searchBar);

        // Botones de apps rápidas
        leftPanel.add(createTaskbarIcon("🗂️", "Archivos", e -> launchFileExplorer()));
        leftPanel.add(createTaskbarIcon("🎵", "Reproductor", e -> launchMusicPlayer()));
        leftPanel.add(createTaskbarIcon("📝", "Texto", e -> launchTextEditor()));
        leftPanel.add(createTaskbarIcon("🚀", "Consola", e -> launchConsole()));
        leftPanel.add(createTaskbarIcon("🖼️", "Visor", e -> launchImageViewer()));
    
        leftPanel.add(taskbarAppPanel);
        taskbar.add(leftPanel, BorderLayout.WEST);
        // Panel derecho: reloj
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setOpaque(false);
        timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        rightPanel.add(timeLabel);

        taskbar.add(rightPanel, BorderLayout.EAST);
        return taskbar;
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

    private JPanel createDesktopIcon(String emoji, String name, ActionListener doubleClickListener) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(100, 80));

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setForeground(Color.WHITE);

        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.WHITE);

        panel.add(icon, BorderLayout.CENTER);
        panel.add(label, BorderLayout.SOUTH);

        if (doubleClickListener != null) {
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        doubleClickListener.actionPerformed(
                                new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, name));
                    }
                }
            });
        }

        return panel;
    }

    private JPanel createAppMenuItem(String emoji, String name, ActionListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 60, 60));
        panel.setPreferredSize(new Dimension(90, 90));

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        iconLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(Color.WHITE);

        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                listener.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, ""));
            }
        });

        return panel;
    }

    private void showStartMenu(JButton source) {
        if (startMenu != null && startMenu.isShowing()) {
            startMenu.setVisible(false);
            return;
        }

        startMenu = new JPopupMenu();
        startMenu.setLayout(new BorderLayout());
        startMenu.setPreferredSize(new Dimension(380, 520));
        startMenu.setBackground(new Color(45, 45, 45));

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(35, 35, 35));
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel iconLabel = new JLabel("👤", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        iconLabel.setForeground(Color.WHITE);

        JLabel name = new JLabel(currentUser.getUsername(), SwingConstants.LEFT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(Color.WHITE);

        userPanel.add(iconLabel, BorderLayout.WEST);
        userPanel.add(name, BorderLayout.CENTER);
        startMenu.add(userPanel, BorderLayout.NORTH);

        JPanel appGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        appGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        appGrid.setBackground(new Color(45, 45, 45));

        for (String[] app : APPS) {
            appGrid.add(createAppMenuItem(app[1], app[0], e -> {
                openAppByName(app[0]);
                startMenu.setVisible(false);
            }));
        }
        startMenu.add(appGrid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 5, 5));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottom.setBackground(new Color(45, 45, 45));

        JButton logout = new JButton("⏻ Cerrar Aplicación");
        logout.setBackground(new Color(150, 0, 0));
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> System.exit(0));

        JButton switchAccount = new JButton("↺ Salir de Cuenta");
        switchAccount.setBackground(new Color(80, 80, 80));
        switchAccount.setForeground(Color.WHITE);
        switchAccount.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new Login().setVisible(true);
                Desktop.this.dispose();
            });
        });

        bottom.add(switchAccount);
        bottom.add(logout);
        startMenu.add(bottom, BorderLayout.SOUTH);

        startMenu.show(source, 0, source.getHeight());
    }

    private void startClockTimer() {
        java.time.format.DateTimeFormatter timeFormatter
                = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        java.time.format.DateTimeFormatter dateFormatter
                = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        clockTimer = new javax.swing.Timer(1000, e -> {
            String time = java.time.LocalTime.now().format(timeFormatter);
            String date = java.time.LocalDate.now().format(dateFormatter);
            timeLabel.setText(time + " | " + date);
        });
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }

    private void openAppByName(String appName) {
        switch (appName) {
            case "Archivos" ->
                launchFileExplorer();
            case "Reproductor Musical" ->
                launchMusicPlayer();
            case "Texto" ->
                launchTextEditor();
            case "Consola" ->
                launchConsole();
            case "Visor de Imágenes" ->  
                launchImageViewer();
            default ->
                JOptionPane.showMessageDialog(this, "Aplicación no encontrada: " + appName);
        }
    }

    private void launchFileExplorer() {
        try {
            File userRoot = new File(Z_ROOT_PATH + currentUser.getUsername());
            if (!userRoot.exists()) {
                userRoot.mkdirs();
            }

            FileExplorerWindow fileExplorer = new FileExplorerWindow(currentUser, userRoot);
            addInternalFrame(fileExplorer, "Explorador");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el explorador: " + ex.toString());
        }
    }

    private void launchTextEditor() {
        EditorTexto editor = new EditorTexto(currentUser);
        addInternalFrame(editor, "Editor de Texto");
    }

    private void launchMusicPlayer() {
        ReproductorGUI player = new ReproductorGUI(currentUser);
        addInternalFrame(player, "Reproductor");
    }

    private void launchConsole() {
        try {
            CMD_GUI cmd = new CMD_GUI();
            JInternalFrame cmdFrame = new JInternalFrame("Consola", true, true, true, true);
            cmdFrame.setSize(800, 500);
            cmdFrame.setLayout(new BorderLayout());
            cmdFrame.add(cmd, BorderLayout.CENTER);
            cmdFrame.setVisible(true);

            addInternalFrame(cmdFrame, "Consola");

            cmdFrame.toFront();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la consola: " + e.toString());
        }
    }

    private void launchImageViewer() {
        try {
            String rutaImagenes = Z_ROOT_PATH + currentUser.getUsername() + File.separator + "Imagenes";
            File f = new File(rutaImagenes);
            if (!f.exists()) {
                f.mkdirs();
            }

            VisorImagenes visor = new VisorImagenes(currentUser);

            JInternalFrame imgFrame = new JInternalFrame(
                    "Visor de Imágenes", true, true, true, true
            );
            imgFrame.setSize(900, 600);
            imgFrame.setLayout(new BorderLayout());
            imgFrame.add(visor, BorderLayout.CENTER);
            imgFrame.setVisible(true);

            addInternalFrame(imgFrame, "Visor de Imágenes");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al abrir Visor de Imágenes: " + e.getMessage());
        }
    }

    private void addInternalFrame(JInternalFrame frame, String title) {

        frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        frame.setLocation(cascadeX, cascadeY);
        cascadeX += cascadeStep;
        cascadeY += cascadeStep;

        if (cascadeX + frame.getWidth() > desktopPane.getWidth()) {
            cascadeX = 30;
        }
        if (cascadeY + frame.getHeight() > desktopPane.getHeight()) {
            cascadeY = 30;
        }

        desktopPane.add(frame, JLayeredPane.PALETTE_LAYER);
        frame.setVisible(true);

        JButton taskButton = new JButton(title);
        taskButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        taskButton.setBackground(new Color(60, 60, 60));
        taskButton.setForeground(Color.WHITE);

        taskButton.addActionListener(e -> {
            try {
                if (frame.isIcon()) {
                    frame.setIcon(false);
                }
                frame.setSelected(true);
            } catch (Exception ignored) {
            }
        });

        taskbarAppPanel.add(taskButton);
        taskbarAppPanel.revalidate();
        taskbarAppPanel.repaint();

        frameButtonMap.put(frame, taskButton);

        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                taskbarAppPanel.remove(taskButton);
                taskbarAppPanel.revalidate();
                taskbarAppPanel.repaint();
                frameButtonMap.remove(frame);
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent e) {
                taskButton.setBackground(new Color(80, 80, 80));
            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
                taskButton.setBackground(new Color(60, 60, 60));
            }
        });

        try {
            frame.setSelected(true);
        } catch (Exception ignored) {
        }
    }

    private class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public BackgroundPanel(String path) {
            try {
                URL url = getClass().getClassLoader().getResource(path);
                if (url != null) {
                    backgroundImage = new ImageIcon(url).getImage();
                } else {
                    backgroundImage = new ImageIcon(path).getImage();
                }
            } catch (Exception e) {
                backgroundImage = null;
            }
        }

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
}

