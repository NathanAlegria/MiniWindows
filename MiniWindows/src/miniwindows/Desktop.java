/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import CMD.CMD_GUI;
import EditordeTexto.EditorTexto;
import Insta.InstagramProject;
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
import java.util.List;
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
        {"Visor de Imágenes", "🖼️"},
        {"Instagram", "📸"}
    };

    private final User currentUser;
    private JDesktopPane desktopPane;
    private JPopupMenu startMenu;
    private JPanel desktopIconPanel;
    private JLabel timeLabel;
    private javax.swing.Timer clockTimer;

    private JPanel taskbarAppPanel;
    private Map<JInternalFrame, JButton> frameButtonMap = new HashMap<>();

    private float iconAlpha = 1f; 
    private Timer fadeTimer;

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

        JPanel contentPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (desktopIconPanel != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconAlpha));
                    desktopIconPanel.paint(g2d);
                    g2d.dispose();
                }
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBounds(0, 0, screen.width, screen.height);
        desktopPane.add(contentPanel, JLayeredPane.DRAG_LAYER);

        taskbarAppPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        taskbarAppPanel.setOpaque(false);
        contentPanel.add(createModernTaskbar(), BorderLayout.SOUTH);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconAlpha));
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.add(createDesktopIcon("🗂️", "Archivos", e -> launchFileExplorer()));
        iconPanel.add(createDesktopIcon("🎵", "Reproductor Musical", e -> launchMusicPlayer()));
        iconPanel.add(createDesktopIcon("📝", "Texto", e -> launchTextEditor()));
        iconPanel.add(createDesktopIcon("🚀", "Consola", e -> launchConsole()));
        iconPanel.add(createDesktopIcon("🖼️", "Visor de Imágenes", e -> launchImageViewer()));
        iconPanel.add(createDesktopIcon("📸", "Instagram", e -> launchInstagram()));
        contentPanel.add(iconPanel, BorderLayout.NORTH);
        this.desktopIconPanel = iconPanel;

        add(desktopPane);
        startClockTimer();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                checkDesktopIconVisibility();
            }
        });
    }

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

        for (String[] app : APPS) {
            leftPanel.add(createTaskbarIcon(app[1], app[0], e -> openAppByName(app[0])));
        }

        leftPanel.add(taskbarAppPanel);
        taskbar.add(leftPanel, BorderLayout.WEST);

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
            case "Instagram" ->
                launchInstagram();
            default ->
                JOptionPane.showMessageDialog(this, "Aplicación no encontrada: " + appName);
        }
    }

    private void launchInstagram() {
        InstagramProject insta = new InstagramProject();
        JInternalFrame frame = new JInternalFrame("Instagram", true, true, true, true);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout());
        frame.add(insta, BorderLayout.CENTER);
        addInternalFrame(frame, "Instagram");
    }

    private void launchFileExplorer() {
        try {
            FileExplorerWindow fileExplorer = new FileExplorerWindow(currentUser);
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
            CMD_GUI cmd = new CMD_GUI(currentUser);
            JInternalFrame cmdFrame = new JInternalFrame("Consola", true, true, true, true);
            cmdFrame.setSize(800, 500);
            cmdFrame.setLayout(new BorderLayout());
            cmdFrame.add(cmd, BorderLayout.CENTER);
            addInternalFrame(cmdFrame, "Consola");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la consola: " + e.toString());
        }
    }

    private void launchImageViewer() {
        try {
            String rutaImagenes = Z_ROOT_PATH + currentUser.getUsername() + File.separator + "Mis Imágenes";
            File f = new File(rutaImagenes);
            if (!f.exists()) {
                f.mkdirs();
            }
            VisorImagenes visor = new VisorImagenes(currentUser);
            JInternalFrame imgFrame = new JInternalFrame("Visor de Imágenes", true, true, true, true);
            imgFrame.setSize(900, 600);
            imgFrame.setLayout(new BorderLayout());
            imgFrame.add(visor, BorderLayout.CENTER);
            addInternalFrame(imgFrame, "Visor de Imágenes");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al abrir Visor de Imágenes: " + e.getMessage());
        }
    }

    private void addInternalFrame(JInternalFrame frame, String title) {
        frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        int x = (desktopPane.getWidth() - frame.getWidth()) / 2;
        int y = (desktopPane.getHeight() - frame.getHeight()) / 2;
        frame.setLocation(x, y);

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
                checkDesktopIconVisibility();
            }
        });

        frame.addPropertyChangeListener(evt -> {
            if ("maximum".equals(evt.getPropertyName()) || "icon".equals(evt.getPropertyName())) {
                checkDesktopIconVisibility();
            }
        });

        frame.toFront();
        try {
            frame.setSelected(true);
        } catch (Exception ignored) {
        }
    }

    private void checkDesktopIconVisibility() {
        boolean hideIcons = false;

        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame.isVisible() && (frame.isMaximum() || frame.getLayer() > JLayeredPane.DEFAULT_LAYER)) {
                hideIcons = true;
                break;
            }
        }

        animateIconVisibility(hideIcons);
    }

    private void animateIconVisibility(boolean hide) {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        float target = hide ? 0f : 1f;
        fadeTimer = new Timer(20, null);
        fadeTimer.addActionListener(e -> {
            if (hide && iconAlpha > 0f) {
                iconAlpha -= 0.05f;
                if (iconAlpha < 0f) {
                    iconAlpha = 0f;
                }
            } else if (!hide && iconAlpha < 1f) {
                iconAlpha += 0.05f;
                if (iconAlpha > 1f) {
                    iconAlpha = 1f;
                }
            } else {
                fadeTimer.stop();
            }
            desktopIconPanel.repaint();
        });
        fadeTimer.start();
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

    private void showStartMenu(JButton source) {
        if (startMenu != null && startMenu.isShowing()) {
            startMenu.setVisible(false);
            return;
        }

        startMenu = new JPopupMenu();
        startMenu.setPreferredSize(new Dimension(450, 520)); 
        startMenu.setLayout(new BorderLayout());
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

        JPanel bottom = new JPanel(new GridLayout(1, 0, 5, 5));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottom.setBackground(new Color(45, 45, 45));

        JButton switchAccount = new JButton("↺ Cerrar Sesión");
        switchAccount.setBackground(new Color(80, 80, 80));
        switchAccount.setForeground(Color.WHITE);
        switchAccount.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new Login().setVisible(true); 
                Desktop.this.dispose();
            });
        });
        bottom.add(switchAccount);
        
        if (currentUser.isAdmin()) { 
            JButton adminAccounts = new JButton("⚙️ Cuentas");
            adminAccounts.setBackground(new Color(0, 120, 215)); 
            adminAccounts.setForeground(Color.WHITE);
            adminAccounts.addActionListener(e -> {
                startMenu.setVisible(false);
                showAdminMenu(); 
            });
            bottom.add(adminAccounts);
        }

        JButton logout = new JButton("⏻ Cerrar Aplicación");
        logout.setBackground(new Color(150, 0, 0));
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> System.exit(0));
        
        bottom.add(logout);

        startMenu.add(bottom, BorderLayout.SOUTH);

        startMenu.show(source, 0, source.getHeight());
    }
    
    private void showAdminMenu() {
        Object[] options = {"➕ Crear Cuenta", "➖ Eliminar Cuenta"};
        int n = JOptionPane.showOptionDialog(
                this,
                "Seleccione una acción de administración de cuentas:",
                "Administración de Cuentas",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, 
                options,
                options[0]);

        if (n == 0) {
            showCreateUserDialog(); 
        } else if (n == 1) {
            showDeleteUserDialog(); 
        }
    }
    
    private void showCreateUserDialog() {
        JTextField userField = new JTextField(12);
        JPasswordField passField = new JPasswordField(12);
        passField.setEchoChar('•');

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Nombre de Usuario:"));
        panel.add(userField);
        panel.add(new JLabel("Contraseña (5 chars EXACTOS, 1 Mayús., 1 Signo Esp.):"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Crear Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newUsername = userField.getText().trim();
            String newPassword = new String(passField.getPassword());

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Usuario y contraseña no pueden estar vacíos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newUsername.equalsIgnoreCase("Admin")) {
                JOptionPane.showMessageDialog(this, "El nombre de usuario 'Admin' está reservado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!Password.isValid(newPassword)) {
                JOptionPane.showMessageDialog(this,
                        "La contraseña NO cumple los requisitos:\n" + Password.getErrorReason(newPassword),
                        "Error de Validación de Contraseña", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (UserManager.createUser(newUsername, newPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Usuario '" + newUsername + "' creado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "El nombre de usuario '" + newUsername + "' ya existe.",
                        "Error de Creación", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDeleteUserDialog() {
        List<User> users = UserManager.getUsers();
        
        List<String> usernames = users.stream()
            .filter(user -> !user.isAdmin())
            .map(User::getUsername)
            .toList();
        
        if (usernames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay usuarios no administradores para eliminar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedUser = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione el usuario que desea eliminar:",
                "Eliminar Usuario",
                JOptionPane.QUESTION_MESSAGE,
                null,
                usernames.toArray(),
                usernames.get(0));

        if (selectedUser != null && !selectedUser.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "¿Está seguro que desea eliminar a '" + selectedUser + "'? Esta acción es irreversible.", 
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (UserManager.deleteUser(selectedUser)) {
                    JOptionPane.showMessageDialog(this, 
                            "Usuario '" + selectedUser + "' eliminado correctamente.", 
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                            "Error al eliminar al usuario. Asegúrese de que el usuario existe.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    private JPanel createAppMenuItem(String emoji, String name, ActionListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 60, 60));
        panel.setPreferredSize(new Dimension(100, 100)); 

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
}