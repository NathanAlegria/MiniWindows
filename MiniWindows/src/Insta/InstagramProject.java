/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Insta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
/**
 *
 * @author jerem
 */
    public class InstagramProject extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private UserManager userManager;
    private User loggedUser; 
    private JPanel profileCardContainer; 

    // Colores Estilo Instagram Dark Mode
    private final Color BG_COLOR = new Color(0, 0, 0); // Fondo negro
    private final Color INPUT_BG = new Color(38, 38, 38); // Gris oscuro inputs
    private final Color TEXT_COLOR = new Color(250, 250, 250); // Blanco
    private final Color BORDER_COLOR = new Color(54, 54, 54); // Borde sutil
    private final Color BTN_BLUE = new Color(0, 149, 246); // Azul Instagram
    private final Color POST_BG = new Color(18, 18, 18); // Fondo de post

    public InstagramProject() {
        super("Instagram");
        userManager = new UserManager();
        
        setSize(900, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Añadir las vistas
        mainPanel.add(crearPanelLogin(), "LOGIN");
        mainPanel.add(crearPanelRegistro(), "REGISTER");
        mainPanel.add(crearPanelPrincipal(), "MAIN"); 
        mainPanel.add(crearPanelProfileSearch(), "PROFILE_SEARCH"); 
        mainPanel.add(crearPanelCrearPost(), "CREATE_POST"); // Nuevo panel de creación

        add(mainPanel);
    }
    
    // Método main para ejecutar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InstagramProject().setVisible(true);
        });
    }

    // --- PANEL DE LOGIN (EXISTENTE) ---
    private JPanel crearPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);

        JPanel loginCard = new JPanel(null); 
        loginCard.setPreferredSize(new Dimension(400, 600)); 
        loginCard.setBackground(BG_COLOR);
        loginCard.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        JLabel title = new JLabel("Instagram", SwingConstants.CENTER);
        title.setFont(new Font("Segoe Script", Font.BOLD, 40));
        title.setForeground(TEXT_COLOR);
        title.setBounds(50, 60, 300, 60);
        loginCard.add(title);

        JTextField txtUser = styledTextField("Usuario");
        txtUser.setBounds(50, 150, 300, 40);
        
        JPasswordField txtPass = styledPasswordField("Contraseña");
        txtPass.setBounds(50, 200, 300, 40);

        loginCard.add(txtUser);
        loginCard.add(txtPass);

        JButton btnLogin = styledButton("Entrar");
        btnLogin.setBounds(50, 260, 300, 40);
        btnLogin.addActionListener(e -> {
            try {
                String u = txtUser.getText();
                String p = new String(txtPass.getPassword());
                
                if(u.isEmpty() || p.isEmpty()) throw new EmptyFieldException("Llena todos los campos");

                loggedUser = userManager.login(u, p); 
                JOptionPane.showMessageDialog(this, "Bienvenido " + loggedUser.getNombre(), "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);
                
                txtUser.setText("");
                txtPass.setText("");
                
                cardLayout.show(mainPanel, "MAIN");

            } catch (InvalidCredentialsException | EmptyFieldException ex) {
                int opt = JOptionPane.showConfirmDialog(this, 
                    ex.getMessage() + "\n¿Deseas intentar de nuevo (Yes) o Crear cuenta (No)?", 
                    "Error de Login", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                
                if (opt == JOptionPane.NO_OPTION) {
                    cardLayout.show(mainPanel, "REGISTER");
                }
            }
        });
        loginCard.add(btnLogin);

        JLabel lblOr = new JLabel("- O -", SwingConstants.CENTER);
        lblOr.setForeground(Color.GRAY);
        lblOr.setBounds(50, 320, 300, 20);
        loginCard.add(lblOr);

        JButton btnGoRegister = createLinkButton("¿No tienes una cuenta? Regístrate");
        btnGoRegister.setBounds(50, 550, 300, 30);
        btnGoRegister.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        loginCard.add(btnGoRegister);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginCard, gbc);

        return panel;
    }

    // --- PANEL DE REGISTRO (EXISTENTE) ---
    private JPanel crearPanelRegistro() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        
        JPanel registerCard = new JPanel(null); 
        registerCard.setPreferredSize(new Dimension(400, 600)); 
        registerCard.setBackground(BG_COLOR);
        registerCard.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        final String[] photoPath = {""}; 

        JLabel title = new JLabel("Crear Cuenta", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        title.setBounds(50, 30, 300, 30);
        registerCard.add(title);

        JTextField txtNombre = styledTextField("Nombre Completo");
        txtNombre.setBounds(50, 80, 300, 35);
        
        JTextField txtUser = styledTextField("Username (Único)");
        txtUser.setBounds(50, 125, 300, 35);

        JPasswordField txtPass = styledPasswordField("Contraseña");
        txtPass.setBounds(50, 170, 300, 35);

        JTextField txtEdad = styledTextField("Edad");
        txtEdad.setBounds(50, 215, 140, 35);

        JRadioButton rbM = new JRadioButton("M");
        JRadioButton rbF = new JRadioButton("F");
        styleRadioButton(rbM); styleRadioButton(rbF);
        ButtonGroup bg = new ButtonGroup(); bg.add(rbM); bg.add(rbF);
        
        JPanel genderPanel = new JPanel();
        genderPanel.setBackground(BG_COLOR);
        genderPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), "Género", 0, 0, null, Color.GRAY));
        genderPanel.setBounds(210, 215, 140, 45);
        genderPanel.add(rbM); genderPanel.add(rbF);
        registerCard.add(genderPanel);

        JButton btnPhoto = new JButton("Seleccionar Foto de Perfil...");
        btnPhoto.setBackground(INPUT_BG);
        btnPhoto.setForeground(Color.WHITE);
        btnPhoto.setBounds(50, 280, 300, 30);
        
        btnPhoto.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes JPG & PNG", "jpg", "png", "jpeg");
            fileChooser.setFileFilter(filter);
            
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                photoPath[0] = selectedFile.getAbsolutePath();
                btnPhoto.setText(selectedFile.getName()); 
                btnPhoto.setForeground(BTN_BLUE); 
            }
        });
        
        registerCard.add(btnPhoto);
        registerCard.add(txtNombre); 
        registerCard.add(txtUser); 
        registerCard.add(txtPass); 
        registerCard.add(txtEdad);

        JButton btnRegister = styledButton("Registrarte");
        btnRegister.setBounds(50, 330, 300, 40);
        btnRegister.addActionListener(e -> {
            try {
                if (txtNombre.getText().isEmpty() || txtUser.getText().isEmpty() || 
                    new String(txtPass.getPassword()).isEmpty() || txtEdad.getText().isEmpty()) {
                    throw new EmptyFieldException("Todos los campos son obligatorios.");
                }

                int edad = Integer.parseInt(txtEdad.getText());
                char genero = rbM.isSelected() ? 'M' : (rbF.isSelected() ? 'F' : ' ');
                if (genero == ' ') throw new EmptyFieldException("Selecciona un género.");
                
                String finalPath = photoPath[0];
                if (finalPath.isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(this, "No seleccionaste foto. ¿Continuar sin foto?", "Advertencia", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    finalPath = "default_user.png";
                }

                User newUser = new User(
                    txtNombre.getText(), 
                    genero, 
                    txtUser.getText(), 
                    new String(txtPass.getPassword()), 
                    edad, 
                    finalPath
                );

                userManager.registrarUsuario(newUser);
                loggedUser = newUser; 
                
                JOptionPane.showMessageDialog(this, "¡Cuenta creada exitosamente! Redirigiendo...", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
                
                txtNombre.setText(""); txtUser.setText(""); txtPass.setText(""); txtEdad.setText(""); 
                photoPath[0] = ""; btnPhoto.setText("Seleccionar Foto de Perfil..."); btnPhoto.setForeground(Color.WHITE);
                bg.clearSelection();
                
                cardLayout.show(mainPanel, "MAIN");

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "La edad debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        registerCard.add(btnRegister);

        JButton btnBack = createLinkButton("¿Ya tienes cuenta? Entrar");
        btnBack.setBounds(50, 550, 300, 30);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        registerCard.add(btnBack);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerCard, gbc);

        return panel;
    }

    // --- PANEL PRINCIPAL (FEED) (EXISTENTE) ---
    private JPanel crearPanelPrincipal() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        panel.add(crearSidebarDesktop(), BorderLayout.WEST);

        JPanel contentAreaWrapper = new JPanel(new GridBagLayout());
        contentAreaWrapper.setBackground(BG_COLOR);
        
        JPanel feedContent = new JPanel();
        feedContent.setLayout(new BoxLayout(feedContent, BoxLayout.Y_AXIS));
        feedContent.setBackground(BG_COLOR);
        
        int feedWidth = 550; 
        feedContent.setPreferredSize(new Dimension(feedWidth, 600)); 
        feedContent.setMaximumSize(new Dimension(feedWidth, Integer.MAX_VALUE));
        
        // Mensaje de feed vacío 
        JLabel emptyMessage = new JLabel("<html><div style='text-align: center; width: " + (feedWidth-50) + "px;'><b>¡Bienvenido!</b><br>Sigue a tus amigos para ver publicaciones.</div></html>", SwingConstants.CENTER);
        emptyMessage.setForeground(Color.GRAY);
        emptyMessage.setFont(new Font("SansSerif", Font.BOLD, 14));
        emptyMessage.setBorder(new EmptyBorder(50, 0, 0, 0));
        emptyMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        feedContent.add(emptyMessage);
        

        JScrollPane scrollPane = new JScrollPane(feedContent);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setBackground(BG_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0; 
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.VERTICAL;
        
        contentAreaWrapper.add(scrollPane, gbc);

        panel.add(contentAreaWrapper, BorderLayout.CENTER);

        return panel;
    }
    
    // --- PANEL CREAR POST (NUEVO) ---
    private JPanel crearPanelCrearPost() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.add(crearSidebarDesktop(), BorderLayout.WEST);

        // Contenedor Central con GridBagLayout para centrar el formulario
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(BG_COLOR);

        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(500, 500));
        formCard.setBackground(POST_BG);
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("✨ Crear Nueva Publicación", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        formCard.add(title, BorderLayout.NORTH);

        // Formulario de Post
        JPanel postForm = new JPanel();
        postForm.setLayout(new BoxLayout(postForm, BoxLayout.Y_AXIS));
        postForm.setBackground(POST_BG);
        postForm.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Campo de descripción (Caption)
        JTextArea txtCaption = new JTextArea(5, 20);
        txtCaption.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtCaption.setForeground(TEXT_COLOR);
        txtCaption.setBackground(INPUT_BG);
        txtCaption.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        txtCaption.setCaretColor(TEXT_COLOR);
        txtCaption.setLineWrap(true);
        txtCaption.setWrapStyleWord(true);
        JScrollPane captionScroll = new JScrollPane(txtCaption);
        captionScroll.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(BORDER_COLOR), "Escribe la descripción...", 0, 0, null, Color.GRAY));
        captionScroll.setMaximumSize(new Dimension(460, 150));
        captionScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        postForm.add(captionScroll);
        postForm.add(Box.createVerticalStrut(20));

        // Botón para seleccionar Imagen
        JButton btnSelectImage = styledButton("Seleccionar Imagen");
        btnSelectImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSelectImage.setMaximumSize(new Dimension(460, 40));

        // Placeholder para la ruta de la imagen
        final String[] imagePath = {""};
        JLabel lblImageStatus = createDetailLabel("Archivo: Ninguno seleccionado");
        lblImageStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSelectImage.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes JPG & PNG", "jpg", "png", "jpeg");
            fileChooser.setFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                imagePath[0] = selectedFile.getAbsolutePath();
                lblImageStatus.setText("Archivo: " + selectedFile.getName());
                lblImageStatus.setForeground(BTN_BLUE);
            }
        });

        postForm.add(btnSelectImage);
        postForm.add(Box.createVerticalStrut(5));
        postForm.add(lblImageStatus);
        postForm.add(Box.createVerticalGlue()); 

        // Botón Publicar
        JButton btnPost = styledButton("Publicar");
        btnPost.setMaximumSize(new Dimension(460, 40));
        btnPost.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPost.setBackground(new Color(255, 105, 180)); 

        btnPost.addActionListener(e -> {
            try {
                String caption = txtCaption.getText().trim();
                String path = imagePath[0];

                if (loggedUser == null) {
                    throw new Exception("Debes iniciar sesión para publicar.");
                }
                if (path.isEmpty() || path.equals("default_user.png")) { // Asegurar que no sea el path por defecto si no se eligió uno
                    throw new EmptyFieldException("Debes seleccionar una imagen para la publicación.");
                }
                if (caption.isEmpty()) {
                    caption = ""; 
                }
                
                // Recargar el usuario logueado para tener la instancia correcta del manager
                User userToUpdate = userManager.getUserByUsername(loggedUser.getUsername());
                if (userToUpdate == null) {
                    throw new Exception("Error al cargar el usuario para publicar.");
                }

                Post newPost = new Post(loggedUser.getUsername(), path, caption);
                userToUpdate.addPost(newPost); // Añadir a la lista del usuario
                userManager.saveUser(userToUpdate); // Guardar los cambios del usuario

                // Actualizar la instancia local del usuario logueado
                loggedUser = userToUpdate;

                JOptionPane.showMessageDialog(this, "Publicación creada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                // Limpiar campos y volver al inicio
                txtCaption.setText("");
                imagePath[0] = "";
                lblImageStatus.setText("Archivo: Ninguno seleccionado");
                lblImageStatus.setForeground(TEXT_COLOR);

                cardLayout.show(mainPanel, "MAIN");

            } catch (EmptyFieldException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al publicar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(postForm, BorderLayout.CENTER);
        formCard.add(btnPost, BorderLayout.SOUTH);

        centerWrapper.add(formCard);
        panel.add(centerWrapper, BorderLayout.CENTER);

        return panel;
    }


    // --- PANEL DE BÚSQUEDA Y VISUALIZACIÓN DE PERFIL (EXISTENTE) ---
    private JPanel crearPanelProfileSearch() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.add(crearSidebarDesktop(), BorderLayout.WEST); 

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BG_COLOR);
        
        profileCardContainer = new JPanel(new CardLayout());
        profileCardContainer.setPreferredSize(new Dimension(600, 650)); 
        profileCardContainer.setBackground(BG_COLOR);

        profileCardContainer.add(crearProfileCardSearch(), "SEARCH_INPUT");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(profileCardContainer, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }
    
    // Tarjeta inicial para la búsqueda de perfiles (EXISTENTE)
    private JPanel crearProfileCardSearch() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(BG_COLOR);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        
        JLabel title = new JLabel("Buscar Perfil", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JTextField txtSearchUser = styledTextField("Username a buscar");
        txtSearchUser.setMaximumSize(new Dimension(300, 40));
        txtSearchUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton btnSearch = styledButton("Buscar");
        btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSearch.setMaximumSize(new Dimension(300, 40));
        btnSearch.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnSearch.addActionListener(e -> {
            String targetUsername = txtSearchUser.getText().trim();
            if (targetUsername.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa un nombre de usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            User targetUser = userManager.getUserByUsername(targetUsername);
            
            if (targetUser != null) {
                mostrarPerfil(targetUser);
                txtSearchUser.setText(""); // Limpiar el campo
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado: " + targetUsername, "Búsqueda Fallida", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        inputPanel.add(title);
        inputPanel.add(txtSearchUser);
        inputPanel.add(Box.createVerticalStrut(15));
        inputPanel.add(btnSearch);
        
        searchPanel.add(inputPanel);
        return searchPanel;
    }
    
    /**
     * Muestra el perfil del usuario objetivo dinámicamente. (EXISTENTE)
     */
    private void mostrarPerfil(User targetUser) {
        // Eliminar vistas anteriores y añadir la nueva
        profileCardContainer.removeAll();
        
        // **IMPORTANTE**: Recargar el targetUser para asegurar que los contadores (followers) estén actualizados
        User refreshedTargetUser = userManager.getUserByUsername(targetUser.getUsername());
        if (refreshedTargetUser == null) {
            JOptionPane.showMessageDialog(this, "Error al cargar el perfil.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel profileView = buildProfileView(refreshedTargetUser);
        profileCardContainer.add(profileView, "PROFILE_VIEW");
        
        CardLayout cl = (CardLayout) (profileCardContainer.getLayout());
        cl.show(profileCardContainer, "PROFILE_VIEW");
        
        profileCardContainer.revalidate();
        profileCardContainer.repaint();
    }
    
    /**
     * Construye la vista completa de un perfil (similar al diseño de Instagram). (EXISTENTE)
     */
    private JPanel buildProfileView(User targetUser) {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(BG_COLOR);
        profilePanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel headerPanel = new JPanel(new BorderLayout(20, 0)); 
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1.1. Foto de Perfil (Izquierda)
        JLabel lblPhoto = new JLabel();
        try {
            File imageFile = new File(targetUser.getFotoPath());
            if (imageFile.exists()) {
                // Simplificado: usar texto si la ruta no existe realmente
                lblPhoto.setText("IMG");
                lblPhoto.setFont(new Font("SansSerif", Font.BOLD, 18));
                lblPhoto.setForeground(TEXT_COLOR);
                lblPhoto.setPreferredSize(new Dimension(100, 100));
                lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
                lblPhoto.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 180), 3, true));
            } else {
                lblPhoto.setText("Foto");
                lblPhoto.setForeground(TEXT_COLOR);
                lblPhoto.setPreferredSize(new Dimension(100, 100));
                lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
                lblPhoto.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 180), 3, true));
            }
        } catch (Exception e) {
            lblPhoto.setText("Foto Error");
            lblPhoto.setForeground(TEXT_COLOR);
            lblPhoto.setPreferredSize(new Dimension(100, 100));
            lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // 1.2. Info General (Centro)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(BG_COLOR);
        infoPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        infoPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); 

        JLabel lblUsername = new JLabel(targetUser.getUsername() + " ✅");
        lblUsername.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblUsername.setForeground(TEXT_COLOR);
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblUsername);
        infoPanel.add(Box.createVerticalStrut(5));

        // Stats: Posts | Followers | Followings (Mejorado)
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.X_AXIS));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setMaximumSize(new Dimension(500, 40));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Posts
        statsPanel.add(createStatPanel(String.valueOf(targetUser.getPosts().size()), "publicaciones"));
        statsPanel.add(Box.createHorizontalStrut(30)); 
        // Followers
        statsPanel.add(createStatPanel(String.valueOf(targetUser.getFollowers().size()), "seguidores"));
        statsPanel.add(Box.createHorizontalStrut(30)); 
        // Followings
        statsPanel.add(createStatPanel(String.valueOf(targetUser.getFollowings().size()), "seguidos"));
        
        infoPanel.add(statsPanel);
        infoPanel.add(Box.createVerticalStrut(10)); 
        
        // Datos Personales
        infoPanel.add(createDetailLabel("Nombre: " + targetUser.getNombre()));
        infoPanel.add(createDetailLabel("Género: " + targetUser.getGenero()));
        infoPanel.add(createDetailLabel("Edad: " + targetUser.getEdad()));
        infoPanel.add(createDetailLabel("Miembro desde: " + targetUser.getJoinDate()));

        // 1.3. Botones de Acción (Derecha)
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(BG_COLOR);
        actionPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        // Lógica de SEGUIR/DEJAR DE SEGUIR
        if (loggedUser != null && !targetUser.getUsername().equals(loggedUser.getUsername())) {
            boolean isFollowing = loggedUser.isFollowing(targetUser.getUsername());
            
            JButton btnFollowToggle = styledButton(isFollowing ? "Siguiendo" : "Seguir");
            btnFollowToggle.setPreferredSize(new Dimension(180, 30));
            btnFollowToggle.setMinimumSize(new Dimension(180, 30));
            btnFollowToggle.setMaximumSize(new Dimension(180, 30));
            btnFollowToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            if (isFollowing) {
                btnFollowToggle.setBackground(new Color(54, 54, 54)); 
                btnFollowToggle.setText("Siguiendo"); 
            } else {
                btnFollowToggle.setBackground(BTN_BLUE); 
                btnFollowToggle.setText("Seguir"); 
            }

            btnFollowToggle.addActionListener(e -> {
                userManager.toggleFollow(loggedUser.getUsername(), targetUser.getUsername());
                
                // CORRECCIÓN: Volver a cargar el usuario logueado para que su lista 'followings' esté actualizada
                User updatedLoggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                if (updatedLoggedUser != null) {
                    loggedUser = updatedLoggedUser; 
                }
                
                // Recargar el perfil para actualizar los contadores y el estado del botón
                mostrarPerfil(targetUser); 
            });
            actionPanel.add(btnFollowToggle);
            actionPanel.add(Box.createVerticalStrut(10)); 
        } else if (loggedUser != null && targetUser.getUsername().equals(loggedUser.getUsername())) {
            // Es el perfil propio
            JLabel lblOwnProfile = new JLabel("Este es tu perfil");
            lblOwnProfile.setForeground(Color.GRAY);
            lblOwnProfile.setFont(new Font("SansSerif", Font.PLAIN, 12));
            actionPanel.add(lblOwnProfile);
            actionPanel.add(Box.createVerticalStrut(10));
        }
        

        // Ensamblar Header
        headerPanel.add(lblPhoto, BorderLayout.WEST);
        headerPanel.add(infoPanel, BorderLayout.CENTER);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        
        profilePanel.add(headerPanel, BorderLayout.NORTH);

        // 2. TABS Y GRID DE POSTS (CENTER)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);

        // 2.1. Tab Bar Simulation 
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 80, 10)); 
        tabBar.setBackground(BG_COLOR);
        tabBar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR)); 
        tabBar.setPreferredSize(new Dimension(profilePanel.getWidth(), 50));

        tabBar.add(createTabIcon("◼️ Posts", "Posts")); 

        contentPanel.add(tabBar, BorderLayout.NORTH);

        // 2.2. Posts Grid 
        JPanel gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.setBackground(BG_COLOR);
        
        JPanel postsGrid = new JPanel(new GridLayout(0, 3, 5, 5)); 
        postsGrid.setBackground(BG_COLOR);
        postsGrid.setBorder(new EmptyBorder(10, 5, 10, 5));
        
        if (targetUser.getPosts().isEmpty()) {
            JLabel noPosts = new JLabel("Este usuario aún no tiene publicaciones.", SwingConstants.CENTER);
            noPosts.setForeground(Color.GRAY);
            noPosts.setFont(new Font("SansSerif", Font.ITALIC, 14));
            gridWrapper.add(noPosts);
        } else {
            for (Post post : targetUser.getPosts()) {
                postsGrid.add(crearPostMiniatura(post));
            }
            JScrollPane scrollPane = new JScrollPane(postsGrid);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setBackground(BG_COLOR);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gridWrapper.add(scrollPane, gbc);
        }
        
        contentPanel.add(gridWrapper, BorderLayout.CENTER);
        profilePanel.add(contentPanel, BorderLayout.CENTER);
        
        return profilePanel;
    }
    
    // Crea una miniatura de post (cuadrado) para la cuadrícula del perfil (EXISTENTE)
    private JPanel crearPostMiniatura(Post post) {
        JPanel miniatura = new JPanel(new BorderLayout());
        miniatura.setPreferredSize(new Dimension(150, 150));
        miniatura.setBackground(INPUT_BG);
        miniatura.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        JLabel lblImage = new JLabel("Post: " + post.getCaption().substring(0, Math.min(post.getCaption().length(), 15)) + "...", SwingConstants.CENTER);
        lblImage.setForeground(Color.GRAY);
        
        miniatura.add(lblImage, BorderLayout.CENTER);
        
        miniatura.setCursor(new Cursor(Cursor.HAND_CURSOR));
        miniatura.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(null, "Ver Post Completo: " + post.getCaption(), "Post", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        return miniatura;
    }
    
    // --- SIDEBAR DESKTOP (NAVIGATION) (COMPLETADO) ---
    private JPanel crearSidebarDesktop() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_COLOR);
        sidebar.setPreferredSize(new Dimension(220, getHeight())); 
        sidebar.setBorder(new LineBorder(BORDER_COLOR, 1)); 

        // 1. Logo (NORTH)
        JLabel title = new JLabel("Instagram", SwingConstants.LEFT);
        title.setFont(new Font("Segoe Script", Font.BOLD, 22));
        title.setForeground(TEXT_COLOR);
        title.setBorder(new EmptyBorder(20, 15, 20, 15));
        sidebar.add(title, BorderLayout.NORTH);

        // 2. Navigation Links (CENTER)
        JPanel navLinks = new JPanel();
        navLinks.setLayout(new BoxLayout(navLinks, BoxLayout.Y_AXIS)); 
        navLinks.setBackground(BG_COLOR);
        navLinks.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Botones de navegación (COMPLETADOS)
        navLinks.add(createSidebarButton("🏠 Inicio", "MAIN"));
        navLinks.add(createSidebarButton("🔍 Búsqueda", "PROFILE_SEARCH"));
        navLinks.add(createSidebarButton("✨ Crear", "CREATE_POST")); 
        navLinks.add(createSidebarButton("👤 Perfil", "MY_PROFILE")); 
        
        navLinks.add(Box.createVerticalGlue()); // Empuja el resto hacia abajo
        
        navLinks.add(createSidebarButton("🚪 Salir", "LOGOUT")); // Botón de Logout

        sidebar.add(navLinks, BorderLayout.CENTER);
        return sidebar;
    }
    
    // --- MÉTODOS HELPER ---
    
    // Helper para botones de la Sidebar (COMPLETADO con lógica de navegación)
    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(BG_COLOR);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMargin(new Insets(10, 0, 10, 0)); // Padding vertical

        btn.addActionListener(e -> {
            if (cardName.equals("LOGOUT")) {
                int opt = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Confirmar Salida", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    loggedUser = null; 
                    cardLayout.show(mainPanel, "LOGIN");
                }
            } else if (cardName.equals("MY_PROFILE")) {
                if (loggedUser != null) {
                    // Navega a la vista de búsqueda de perfil, luego fuerza a mostrar el propio perfil
                    cardLayout.show(mainPanel, "PROFILE_SEARCH");
                    mostrarPerfil(loggedUser);
                } else {
                    JOptionPane.showMessageDialog(this, "Debes iniciar sesión primero.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (cardName.equals("PROFILE_SEARCH")) {
                // Navega a la vista de búsqueda y resetea al panel de input
                cardLayout.show(mainPanel, "PROFILE_SEARCH");
                CardLayout cl = (CardLayout) (profileCardContainer.getLayout());
                cl.show(profileCardContainer, "SEARCH_INPUT");
            } else {
                // Navegación normal (MAIN, CREATE_POST)
                cardLayout.show(mainPanel, cardName);
            }
        });

        // Efecto hover sutil
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(18, 18, 18));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(BG_COLOR);
            }
        });

        return btn;
    }

    // Helper para crear etiquetas de detalle de perfil (EXISTENTE)
    private JLabel createDetailLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_COLOR);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
        return lbl;
    }
    
    // Helper para crear etiquetas de estadísticas de perfil (EXISTENTE)
    private JPanel createStatPanel(String count, String label) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        
        JLabel lblCount = new JLabel(count, SwingConstants.CENTER);
        lblCount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCount.setForeground(TEXT_COLOR);
        lblCount.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label, SwingConstants.CENTER);
        lblLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblLabel.setForeground(Color.GRAY);
        lblLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        p.add(lblCount);
        p.add(lblLabel);
        return p;
    }
    
    // Helper para crear iconos de pestaña (EXISTENTE)
    private JLabel createTabIcon(String icon, String tooltip) {
        JLabel lbl = new JLabel(icon, SwingConstants.CENTER);
        lbl.setForeground(TEXT_COLOR); // Seleccionado por defecto en perfil
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lbl.setToolTipText(tooltip);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, TEXT_COLOR)); 
        return lbl;
    }
    
    // --- ESTILOS COMPARTIDOS (INCLUIDOS PARA COMPLETAR EL CÓDIGO) ---
    
    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setBackground(INPUT_BG);
        field.setCaretColor(TEXT_COLOR);
        field.setText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1), 
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setBackground(INPUT_BG);
        field.setCaretColor(TEXT_COLOR);
        field.setText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1), 
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        field.setEchoChar((char) 0); // Mostrar placeholder
        
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('*');
                    field.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).isEmpty()) {
                    field.setEchoChar((char) 0);
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BTN_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 10, 10, 10)); // Más padding
        return btn;
    }
    
    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(BTN_BLUE);
        btn.setBackground(BG_COLOR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
    
    private void styleRadioButton(JRadioButton rb) {
        rb.setBackground(BG_COLOR);
        rb.setForeground(TEXT_COLOR);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rb.setFocusPainted(false);
    }
}