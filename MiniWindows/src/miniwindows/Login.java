/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;

/**
 *
 * @author Nathan
 */
// NOTA: Se asume que la clase Desktop existe y es accesible.
public class Login extends JFrame {

    private static final String BACKGROUND_IMAGE = "Imagenes/Fondo.png";
    private static final String USER_ICON_IMAGE = "Imagenes/U.png";

    private JPasswordField txtPassword;
    private JLabel lblUsername;
    private JLabel lblIcon;

    private String currentUser = "Admin";
    private JPanel cardPanel;
    private CardLayout cardLayout;

    public Login() {
        // CORRECCIÓN: Usar loadUsers() para asegurar la inicialización de Admin y el sistema de archivos
        UserManager.loadUsers();

        setTitle("Mini-Windows - Iniciar Sesión");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        JPanel singleUserCard = createSingleUserLoginPanel();
        JPanel userSelectionCard = createUserSelectionPanel();

        cardPanel.add(singleUserCard, "SINGLE_USER");
        cardPanel.add(userSelectionCard, "USER_SELECTION");

        BackgroundPanel mainPanel = new BackgroundPanel(BACKGROUND_IMAGE);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.add(cardPanel);

        add(mainPanel);

        // Intenta establecer el usuario actual al último guardado o "Admin" por defecto
        List<User> initialUsers = UserManager.getUsers();
        if (!initialUsers.isEmpty()) {
            this.currentUser = initialUsers.get(0).getUsername();
        }

        cardLayout.show(cardPanel, "SINGLE_USER");
    }

    private JPanel createSingleUserLoginPanel() {
        JPanel loginContainer = new JPanel();
        loginContainer.setLayout(new BoxLayout(loginContainer, BoxLayout.Y_AXIS));
        loginContainer.setOpaque(false);
        loginContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImageIcon userIcon = ImageUtils.getScaledIcon(USER_ICON_IMAGE, 150, 150);
        lblIcon = new JLabel(userIcon);
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblUsername = new JLabel(currentUser);
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword = new JPasswordField(15);
        txtPassword.setMaximumSize(new Dimension(250, 35));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Configuración de visibilidad de contraseña (contraste)
        txtPassword.setForeground(Color.WHITE);
        txtPassword.setCaretColor(Color.WHITE);
        txtPassword.setEchoChar('•');
        txtPassword.setOpaque(true);
        txtPassword.setBackground(new Color(0, 0, 0, 100));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton btnLogin = new JButton("Ingresar");
        btnLogin.setMaximumSize(new Dimension(100, 35));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBackground(new Color(0, 120, 215));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);

        JButton btnOtherUser = new JButton("Otro usuario / Crear nuevo");
        btnOtherUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOtherUser.setForeground(Color.WHITE);
        btnOtherUser.setOpaque(false);
        btnOtherUser.setContentAreaFilled(false);
        btnOtherUser.setBorderPainted(false);
        btnOtherUser.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(e -> attemptLogin());
        txtPassword.addActionListener(e -> attemptLogin());
        btnOtherUser.addActionListener(e -> {
            // Recarga la lista de usuarios cada vez que se cambia a la selección
            cardPanel.remove(cardPanel.getComponent(1));
            cardPanel.add(createUserSelectionPanel(), "USER_SELECTION");
            cardLayout.show(cardPanel, "USER_SELECTION");
        });

        loginContainer.add(Box.createVerticalStrut(20));
        loginContainer.add(lblIcon);
        loginContainer.add(Box.createVerticalStrut(10));
        loginContainer.add(lblUsername);
        loginContainer.add(Box.createVerticalStrut(20));
        loginContainer.add(txtPassword);
        loginContainer.add(Box.createVerticalStrut(10));
        loginContainer.add(btnLogin);
        loginContainer.add(Box.createVerticalStrut(30));
        loginContainer.add(btnOtherUser);

        return loginContainer;
    }

    private JPanel createUserSelectionPanel() {
        JPanel selectionPanel = new JPanel();
        selectionPanel.setOpaque(false);
        selectionPanel.setLayout(new GridBagLayout());

        JPanel usersDisplay = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        usersDisplay.setOpaque(false);

        List<User> userList = UserManager.getUsers(); // Obtiene la lista más reciente

        for (User user : userList) {
            usersDisplay.add(createUserIconPanel(user.getUsername()));
        }

        JButton btnCreate = new JButton("Crear Nuevo Usuario");
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setOpaque(false);
        btnCreate.setContentAreaFilled(false);
        btnCreate.setBorderPainted(false);
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreate.addActionListener(e -> showCreateUserDialog());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(50, 0, 50, 0);

        selectionPanel.add(usersDisplay, gbc);

        gbc.gridy = 1;
        selectionPanel.add(btnCreate, gbc);

        JButton btnBack = new JButton("← Volver a Login");
        btnBack.setOpaque(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setForeground(Color.WHITE);
        btnBack.setBorderPainted(false);
        btnBack.addActionListener(e -> cardLayout.show(cardPanel, "SINGLE_USER"));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(selectionPanel, BorderLayout.CENTER);
        wrapper.add(btnBack, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createUserIconPanel(String userName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon userIcon = ImageUtils.getScaledIcon(USER_ICON_IMAGE, 80, 80);
        JLabel icon = new JLabel(userIcon);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(userName);
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(5));
        panel.add(name);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateCurrentUser(userName);
                cardLayout.show(cardPanel, "SINGLE_USER");
            }
        });

        return panel;
    }

    private void updateCurrentUser(String newUsername) {
        this.currentUser = newUsername;
        lblUsername.setText(newUsername);
        txtPassword.setText("");
    }

    private void attemptLogin() {
        String username = currentUser;
        String password = new String(txtPassword.getPassword());

        User user = UserManager.validateLogin(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this,
                    "¡Bienvenido, " + user.getUsername() + "!",
                    "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);

            dispose();
            new Desktop(user).setVisible(true);

        } else {
            JOptionPane.showMessageDialog(this,
                    "Contraseña incorrecta para el usuario " + username + ".",
                    "Error de Credenciales", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }

    private void showCreateUserDialog() {
        JTextField userField = new JTextField(10);
        JPasswordField passField = new JPasswordField(10);

        passField.setEchoChar('•');

        JPanel panel = new JPanel(new GridLayout(0, 1));
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
                JOptionPane.showMessageDialog(this, "El usuario y la contraseña no pueden estar vacíos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Validar la contraseña usando la clase Password
            if (!Password.isValid(newPassword)) {
                String errorMsg = Password.getErrorReason(newPassword);
                JOptionPane.showMessageDialog(this,
                        "La contraseña NO cumple los requisitos:\n" + errorMsg,
                        "Error de Validación de Contraseña",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Intentar crear el usuario y sus directorios
            if (UserManager.createUser(newUsername, newPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Usuario '" + newUsername + "' creado exitosamente. Ahora inicie sesión.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);

                // Forzar la actualización de la lista de usuarios en la vista
                cardPanel.remove(cardPanel.getComponent(1));
                cardPanel.add(createUserSelectionPanel(), "USER_SELECTION");
                cardLayout.show(cardPanel, "USER_SELECTION");

            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al crear usuario: El nombre de usuario '" + newUsername + "' ya existe.",
                        "Error de Creación", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Clase BackgroundPanel incluida en Login.java
    private class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public BackgroundPanel(String path) {
            try {
                URL imageUrl = Login.class.getClassLoader().getResource(path);

                if (imageUrl != null) {
                    backgroundImage = new ImageIcon(imageUrl).getImage();
                } else {
                    backgroundImage = new ImageIcon(path).getImage();
                    if (backgroundImage.getWidth(null) == -1) {
                        backgroundImage = null;
                    }
                }
            } catch (Exception e) {
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
}
