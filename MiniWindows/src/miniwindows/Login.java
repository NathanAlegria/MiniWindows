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
        UserManager.loadUsers();

        setTitle("Mini-Windows - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);    

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        JPanel singleUserCard = createSingleUserLoginPanel();
        JPanel userSelectionCard = createUserSelectionPanel();

        cardPanel.add(singleUserCard, "SINGLE_USER");
        cardPanel.add(userSelectionCard, "USER_SELECTION");

        BackgroundPanel mainPanel = new BackgroundPanel(BACKGROUND_IMAGE);
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(cardPanel, gbc);

        add(mainPanel);

        List<User> initialUsers = UserManager.getUsers();
        if (!initialUsers.isEmpty()) {
            this.currentUser = initialUsers.get(0).getUsername();
            if (lblUsername != null) {
                lblUsername.setText(this.currentUser);
            }
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);

        setVisible(true);

        cardLayout.show(cardPanel, "SINGLE_USER");
    }

    private JPanel createSingleUserLoginPanel() {
        JPanel loginContainer = new JPanel();
        loginContainer.setLayout(new BoxLayout(loginContainer, BoxLayout.Y_AXIS));
        loginContainer.setOpaque(false);
        loginContainer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        ImageIcon userIcon = ImageUtils.getScaledIcon(USER_ICON_IMAGE, 150, 150);
        lblIcon = new JLabel(userIcon);
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblUsername = new JLabel(currentUser);
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword = new JPasswordField(15);
        txtPassword.setMaximumSize(new Dimension(300, 40));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtPassword.setForeground(Color.WHITE);
        txtPassword.setCaretColor(Color.WHITE);
        txtPassword.setEchoChar('•');
        txtPassword.setOpaque(true);
        txtPassword.setBackground(new Color(0, 0, 0, 120));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JButton btnLogin = new JButton("Ingresar");
        btnLogin.setMaximumSize(new Dimension(130, 40));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBackground(new Color(0, 120, 215));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);

        JButton btnOtherUser = new JButton("Cambiar Usuario");
        btnOtherUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOtherUser.setForeground(Color.WHITE);
        btnOtherUser.setOpaque(false);
        btnOtherUser.setContentAreaFilled(false);
        btnOtherUser.setBorderPainted(false);
        btnOtherUser.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(e -> attemptLogin());
        txtPassword.addActionListener(e -> attemptLogin());
        btnOtherUser.addActionListener(e -> {
            cardPanel.remove(1);
            cardPanel.add(createUserSelectionPanel(), "USER_SELECTION");
            cardLayout.show(cardPanel, "USER_SELECTION");
        });

        loginContainer.add(Box.createVerticalStrut(40));
        loginContainer.add(lblIcon);
        loginContainer.add(Box.createVerticalStrut(10));
        loginContainer.add(lblUsername);
        loginContainer.add(Box.createVerticalStrut(18));
        loginContainer.add(txtPassword);
        loginContainer.add(Box.createVerticalStrut(12));
        loginContainer.add(btnLogin);
        loginContainer.add(Box.createVerticalStrut(20));
        loginContainer.add(btnOtherUser);
        loginContainer.add(Box.createVerticalStrut(40));

        return loginContainer;
    }

    private JPanel createUserSelectionPanel() {
        JPanel selectionPanel = new JPanel();
        selectionPanel.setOpaque(false);
        selectionPanel.setLayout(new GridBagLayout());

        JPanel usersDisplay = new JPanel(new FlowLayout(FlowLayout.CENTER, 36, 10));
        usersDisplay.setOpaque(false);

        List<User> userList = UserManager.getUsers();

        if (userList.isEmpty()) {
            usersDisplay.add(new JLabel("No hay usuarios."));
        } else {
            for (User user : userList) {
                usersDisplay.add(createUserIconPanel(user.getUsername()));
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 0, 18, 0);
        selectionPanel.add(usersDisplay, gbc);

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
        panel.add(Box.createVerticalStrut(8));
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
        if (lblUsername != null) {
            lblUsername.setText(newUsername);
        }
        if (txtPassword != null) {
            txtPassword.setText("");
        }
    }

    private void attemptLogin() {
        String username = currentUser;
        String password = new String(txtPassword.getPassword());

        User user = UserManager.validateLogin(username, password);

        if (user != null) {
            dispose();
            Desktop desktop = new Desktop(user);
            desktop.setVisible(true);
            desktop.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Contraseña incorrecta para el usuario " + username + ".",
                    "Error de Credenciales", JOptionPane.ERROR_MESSAGE);
            if (txtPassword != null) {
                txtPassword.setText("");
            }
        }
    }

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