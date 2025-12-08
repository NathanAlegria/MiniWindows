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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

/**
 *
 * @author jerem
 */
public class InstagramProject extends JPanel {

    private CardLayout cardLayout;
private JPanel centerContentPanel;
    private JPanel mainPanel;
    private UserManager userManager;
    private User loggedUser;
    private JPanel profileCardContainer;
    private JPanel resultsPanel;      
    private JTextField txtSearchUser; 
    private JTextField txtSearchHashtag;
    private JPanel hashtagResultsPanel;
    private JPanel notificationsListPanel;
    

    // Colores Estilo Instagram Dark Mode
    private final Color BG_COLOR = new Color(0, 0, 0); // Fondo negro
    private final Color INPUT_BG = new Color(38, 38, 38); // Gris oscuro inputs
    private final Color TEXT_COLOR = new Color(250, 250, 250); // Blanco
    private final Color BORDER_COLOR = new Color(54, 54, 54); // Borde sutil
    private final Color BTN_BLUE = new Color(0, 149, 246); // Azul Instagram
    private final Color POST_BG = new Color(18, 18, 18); // Fondo de post 

    public InstagramProject() {
        userManager = new UserManager();
        
        // El JPanel principal (this) ahora usa BorderLayout para contener el CardLayout global
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(900, 700)); 
        
        // 1. Crear el CardLayout Global (mainPanel)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout); // Contenedor de cartas global: LOGIN, REGISTER, ROOT_VIEW

        // 2. Crear la Vista Raíz Logueada (ROOT_VIEW)
        JPanel rootView = new JPanel(new BorderLayout());
        rootView.setBackground(BG_COLOR);
        
        // 2a. Sidebar Fijo a la izquierda
        JPanel sidebar = crearSidebarDesktop(); 
        rootView.add(sidebar, BorderLayout.WEST); 
        
        // 2b. Contenedor de Cartas para el Centro (contenido variable)
        CardLayout contentLayout = new CardLayout();
        centerContentPanel = new JPanel(contentLayout); 
        
        // 3. Agregar todas las tarjetas de CONTENIDO al centerContentPanel
        centerContentPanel.add(crearFeedContentWrapper(), "MAIN"); // El Feed
        centerContentPanel.add(crearPanelProfileSearch(), "PROFILE_SEARCH"); // Buscador de perfiles
        
        // --- NUEVAS TARJETAS DE CONTENIDO ---
        centerContentPanel.add(crearPanelCrearPostContent(), "CREATE_POST"); 
        centerContentPanel.add(crearPanelHashtagSearchContent(), "HASHTAG_SEARCH");
        centerContentPanel.add(crearPanelNotificacionesContent(), "NOTIFICATIONS");
        
        rootView.add(centerContentPanel, BorderLayout.CENTER);
        
        // 4. Agregar todas las vistas principales al mainPanel (el CardLayout global)
        mainPanel.add(crearPanelLogin(), "LOGIN"); 
        mainPanel.add(crearPanelRegistro(), "REGISTER"); 
        mainPanel.add(rootView, "ROOT_VIEW"); // ¡Esta es la vista logueada!

        // 5. Agregar el CardLayout global al Frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Mostrar la vista de inicio (Login)
        cardLayout.show(mainPanel, "LOGIN");
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

                if (u.isEmpty() || p.isEmpty()) {
                    throw new EmptyFieldException("Llena todos los campos");
                }

                loggedUser = userManager.login(u, p);
                cardLayout.show(mainPanel, "ROOT_VIEW"); 
        
                CardLayout clCenter = (CardLayout) centerContentPanel.getLayout();
                clCenter.show(centerContentPanel, "MAIN"); 
                rebuildMainFeed();
                JOptionPane.showMessageDialog(this, "Bienvenido " + loggedUser.getNombre(), "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);

                txtUser.setText("");
                txtPass.setText("");

                cardLayout.show(mainPanel, "MAIN");
                rebuildMainFeed();

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
        styleRadioButton(rbM);
        styleRadioButton(rbF);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbM);
        bg.add(rbF);

        JPanel genderPanel = new JPanel();
        genderPanel.setBackground(BG_COLOR);
        genderPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), "Género", 0, 0, null, Color.GRAY));
        genderPanel.setBounds(210, 215, 140, 45);
        genderPanel.add(rbM);
        genderPanel.add(rbF);
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
                try {
                    // Crear un nombre único para la foto de perfil
                    String newFileName = "profile_" + System.currentTimeMillis() + "_" + selectedFile.getName();

                    // Ruta de la raíz del proyecto
                    String projectRoot = System.getProperty("user.dir");
                    File destFile = new File(projectRoot, newFileName);

                    // Copiar el archivo seleccionado a la raíz del proyecto
                    java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // Guardamos solo el nombre del archivo
                    photoPath[0] = newFileName;
                    btnPhoto.setText(selectedFile.getName());
                    btnPhoto.setForeground(BTN_BLUE);
                } catch (Exception ex) {
                    btnPhoto.setText("Error al guardar imagen");
                    btnPhoto.setForeground(Color.RED);
                    ex.printStackTrace();
                }
            }
        });

        registerCard.add(btnPhoto);
        registerCard.add(txtNombre);
        registerCard.add(txtUser);
        registerCard.add(txtPass);
        registerCard.add(txtEdad);

        // Dentro de la clase InstagramProject, en el método crearPanelRegistro()...

        JButton btnRegister = styledButton("Registrarte");
        btnRegister.setBounds(50, 330, 300, 40);
        btnRegister.addActionListener(e -> {
            try {
                if (txtNombre.getText().isEmpty() || txtUser.getText().isEmpty()
                        || new String(txtPass.getPassword()).isEmpty() || txtEdad.getText().isEmpty()) {
                    throw new EmptyFieldException("Todos los campos son obligatorios.");
                }

                int edad = Integer.parseInt(txtEdad.getText());
                char genero = rbM.isSelected() ? 'M' : (rbF.isSelected() ? 'F' : ' ');
                if (genero == ' ') {
                    throw new EmptyFieldException("Selecciona un género.");
                }

                String finalPath = photoPath[0];
                if (finalPath.isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(this, "No seleccionaste foto. ¿Continuar sin foto?", "Advertencia", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                    finalPath = "default_user.png";
                }

                // 1. CREACIÓN DE OBJETO USER (Incluye joinDate y isActive=true)
                User newUser = new User(
                        txtNombre.getText(),
                        genero,
                        txtUser.getText(),
                        new String(txtPass.getPassword()),
                        edad,
                        finalPath
                );

                // 2. LLAMADA A LA LÓGICA (Persiste en users.dat, users.ins y crea carpeta local)
                userManager.registrarUsuario(newUser);
                loggedUser = newUser;

                JOptionPane.showMessageDialog(this, "¡Cuenta creada exitosamente! Redirigiendo...", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);

                // 3. Limpiar formulario
                txtNombre.setText("");
                txtUser.setText("");
                txtPass.setText("");
                txtEdad.setText("");
                photoPath[0] = "";
                btnPhoto.setText("Seleccionar Foto de Perfil...");
                btnPhoto.setForeground(Color.WHITE);
                bg.clearSelection();

                // 4. 🚨 CORRECCIÓN CRÍTICA DE REDIRECCIÓN
                cardLayout.show(mainPanel, "ROOT_VIEW"); // Usar la clave correcta para la vista logueada
                rebuildMainFeed(); // Cargar inmediatamente el feed del nuevo usuario
                // ---------------------------------------------

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "La edad debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Captura el error de unicidad del username que lanza userManager.registrarUsuario
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
    // --- CÓDIGO MODIFICADO: crearFeedContentWrapper ---
   private JPanel crearFeedContentWrapper() {
    JPanel contentAreaWrapper = new JPanel(new GridBagLayout());
    contentAreaWrapper.setBackground(BG_COLOR);

    // Panel con los posts
    JPanel feedContent = new JPanel();
    feedContent.setLayout(new BoxLayout(feedContent, BoxLayout.Y_AXIS));
    feedContent.setBackground(BG_COLOR);
    feedContent.setName("FEED_POSTS_INNER_PANEL");

    int feedWidth = 550;
    feedContent.setMaximumSize(new Dimension(feedWidth, Integer.MAX_VALUE));
    feedContent.setMinimumSize(new Dimension(feedWidth, 0));

    loadFeedPosts(feedContent, feedWidth);

    // ScrollPane
    JScrollPane scrollPane = new JScrollPane(feedContent);
    scrollPane.setName("FEED_SCROLL_PANE");
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setBackground(BG_COLOR);

    // GridBagConstraints para centrar el feed
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.anchor = GridBagConstraints.CENTER;

    // MUY IMPORTANTE: ocupar espacio horizontal y vertical
    gbc.fill = GridBagConstraints.BOTH;

    // Añadir scrollPane centrado
    contentAreaWrapper.add(scrollPane, gbc);

    return contentAreaWrapper;
}

    // --- PANEL PRINCIPAL (FEED) (MODIFICADO) ---

    private JPanel crearPanelPrincipal() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(BG_COLOR);

    // AGREGAR SOLO UNA VEZ CADA PANEL
    panel.add(crearSidebarDesktop(), BorderLayout.WEST);
    panel.add(crearFeedContentWrapper(), BorderLayout.CENTER);

    return panel;
}

// --- NUEVO MÉTODO: Cargar y Mostrar el Feed ---
    private void loadFeedPosts(JPanel feedContent, int feedWidth) {
        feedContent.removeAll();

        if (loggedUser == null) {
            JLabel err = new JLabel("Inicia sesión para ver el Feed.", SwingConstants.CENTER);
            err.setForeground(TEXT_COLOR);
            err.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedContent.add(err);
            return;
        }

        // 1. Obtener todos los posts (propios y de seguidos)
        // NOTA: Asumo que tienes un método getAllRelevantPostsByDate() en UserManager o PostManager
        List<Post> allPosts = userManager.getAllRelevantPostsByDate(loggedUser);

        if (allPosts.isEmpty()) {
            // Mensaje de feed vacío
            JLabel emptyMessage = new JLabel("<html><div style='text-align: center; width: " + (feedWidth - 50) + "px;'><b>¡Bienvenido!</b><br>Sigue a tus amigos para ver publicaciones.</div></html>", SwingConstants.CENTER);
            emptyMessage.setForeground(Color.GRAY);
            emptyMessage.setFont(new Font("SansSerif", Font.BOLD, 14));
            emptyMessage.setBorder(new EmptyBorder(50, 0, 0, 0));
            emptyMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedContent.add(emptyMessage);
        } else {
            // 2. Crear y añadir el componente visual para cada post
            for (Post post : allPosts) {
                JPanel postPanel = createPostFeedView(post, feedWidth - 50); // Ajustar ancho
                postPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                feedContent.add(postPanel);
                feedContent.add(Box.createVerticalStrut(20)); // Espacio entre posts
            }
        }

        feedContent.revalidate();
        feedContent.repaint();
    }

// --- NUEVO MÉTODO: Vista Detallada de un Post para el Feed ---
    private JPanel createPostFeedView(Post post, int width) {
        JPanel postPanel = new JPanel();
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
        postPanel.setBackground(POST_BG);
        postPanel.setBorder(new LineBorder(BORDER_COLOR, 1));
        postPanel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));

        // 1. Cabecera (Username y Foto de Perfil)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        header.setBackground(POST_BG);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        User postAuthor = userManager.getUserByUsername(post.getAuthorUsername());

        JLabel lblAuthor = new JLabel(post.getAuthorUsername());
        lblAuthor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblAuthor.setForeground(TEXT_COLOR);

        JLabel lblProfilePic = new JLabel(cargarImagenCuadrada(postAuthor.getFotoPath(), 30));

        header.add(lblProfilePic);
        header.add(lblAuthor);
        postPanel.add(header);

        // 2. Imagen del Post
        int imageSize = width; // La imagen ocupa todo el ancho del post
        JLabel lblImage = new JLabel();
        ImageIcon postIcon = cargarImagenCuadrada(post.getImagePath(), imageSize);
        if (postIcon != null) {
            lblImage.setIcon(postIcon);
        } else {
            lblImage.setText("No Image");
            lblImage.setForeground(Color.RED);
        }
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        postPanel.add(lblImage);

        // 3. Botones (Like y Comentar)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        actions.setBackground(POST_BG);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Botón de Like
        JButton btnLike = createIconButton("💖", 24);
        // Necesitas un método para crear un botón con un ícono o emoji
        btnLike.setForeground(post.isLikedBy(loggedUser.getUsername()) ? Color.RED : TEXT_COLOR);
        btnLike.addActionListener(e -> handleLikeAction(post, btnLike));
        actions.add(btnLike);

        // Contador de Likes
        JLabel lblLikes = new JLabel(post.getLikesCount() + " likes");
        lblLikes.setForeground(TEXT_COLOR);
        lblLikes.setFont(new Font("SansSerif", Font.PLAIN, 12));
        actions.add(lblLikes);
        actions.add(Box.createHorizontalStrut(20));

        // Botón de Comentar (Asume un ícono de burbuja de diálogo "💬")
        JButton btnComment = createIconButton("💬", 24);
        btnComment.addActionListener(e -> showCommentDialog(post));
        actions.add(btnComment);

        postPanel.add(actions);

        // 4. Descripción (Caption)
        JTextArea txtCaption = new JTextArea(post.getCaption());
        txtCaption.setEditable(false);
        txtCaption.setBackground(POST_BG);
        txtCaption.setForeground(TEXT_COLOR);
        txtCaption.setLineWrap(true);
        txtCaption.setWrapStyleWord(true);
        txtCaption.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtCaption.setBorder(new EmptyBorder(5, 15, 10, 15));
        txtCaption.setAlignmentX(Component.LEFT_ALIGNMENT);

        postPanel.add(txtCaption);

        return postPanel;
    }

// --- NUEVO MÉTODO: Crea el panel con el grid de posts ---
    private JButton createIconButton(String text, int size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, size));
        btn.setForeground(TEXT_COLOR); // Color del texto (o del emoji)
        btn.setBackground(POST_BG); // Fondo del post (para que se mezcle)
        btn.setBorderPainted(false); // Quitar el borde
        btn.setContentAreaFilled(false); // Quitar el color de fondo del área de contenido
        btn.setFocusPainted(false); // Quitar el recuadro de foco
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano al pasar por encima
        return btn;
    }

// También podrías necesitar un createStatPanel para las stats del perfil
// ** MODIFICACIÓN EN createPostGridPanel (ahora crearPostsGrid) **
// Debe devolver SÓLO el JPanel con las miniaturas, no el JScrollPane.
// --- REEMPLAZO COMPLETO DEL MÉTODO crearPostsGrid ---
    private JPanel crearPostsGrid(User targetUser) {
    List<Post> userPosts = userManager.loadPostsFromLocalFile(targetUser.getUsername());

        // Si no hay posts, devolvemos el wrapper centrado (tal como lo tienes)
        if (userPosts.isEmpty()) {
            JLabel noPosts = new JLabel("No hay publicaciones aún.", SwingConstants.CENTER);
            noPosts.setForeground(Color.GRAY);
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setBackground(BG_COLOR);
            wrapper.add(noPosts);
            return wrapper;
        }

        // --- 1. Panel de Posts: Usa GridBagLayout para control total ---
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(BG_COLOR);
        gridPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Pequeño padding general

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST; // Pegar las miniaturas arriba y a la izquierda
        gbc.insets = new Insets(1, 1, 1, 1); // Espacio entre miniaturas (1px)

        // Tamaño fijo de la miniatura
        int thumbnailSize = 200;
        int col = 0;
        int row = 0;

        for (Post post : userPosts) {
            // --- 2. Configurar la miniatura como JButton ---
            JButton postThumbnail = new JButton();
            postThumbnail.setPreferredSize(new Dimension(thumbnailSize, thumbnailSize));
            postThumbnail.setBorder(null);
            postThumbnail.setBackground(POST_BG);
            postThumbnail.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Cargar y escalar la imagen (Usando tu método existente)
            ImageIcon icon = cargarImagenCuadrada(post.getImagePath(), thumbnailSize);
            if (icon != null) {
                postThumbnail.setIcon(icon);
            } else {
                postThumbnail.setText("IMG");
                postThumbnail.setForeground(Color.RED);
            }

            postThumbnail.addActionListener(e -> showPostDetail(post));

            // --- 3. Aplicar GridBagConstraints ---
            gbc.gridx = col; // Columna actual
            gbc.gridy = row; // Fila actual
            gridPanel.add(postThumbnail, gbc);

            // --- 4. Incrementar posición ---
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        // --- 5. Rellenar el espacio restante si la última fila está incompleta ---
        // Esto es crucial. Rellena el espacio horizontal restante para que los posts
        // siempre se peguen a la izquierda sin estirarse.
       if (col > 0) {
        gbc.gridx = col;
        gbc.weightx = 1.0; 
        gridPanel.add(Box.createHorizontalGlue(), gbc);
    }

    // Rellena el espacio vertical restante
    gbc.gridx = 0;
    gbc.gridy = row + 1;
    gbc.weighty = 1.0; 
    gbc.gridwidth = 3; // Ocupa las 3 columnas
    gridPanel.add(Box.createVerticalGlue(), gbc);

        return gridPanel;
    }

    // Dentro de la clase InstagramProject:

/** Crea un JButton para la acción de "Me gusta" con el estado visual inicial. */
private JButton createLikeButton(Post post) {
    JButton btnLike = new JButton();
    btnLike.setBorderPainted(false);
    btnLike.setFocusPainted(false);
    btnLike.setContentAreaFilled(false); // Estilo transparente
    btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnLike.setFont(new Font("SansSerif", Font.BOLD, 16));

    // Inicializar el estado visual del botón
    if (loggedUser != null && post.isLikedBy(loggedUser.getUsername())) {
        btnLike.setText("❤️");
        btnLike.setForeground(Color.RED);
    } else {
        btnLike.setText("♡");
        btnLike.setForeground(Color.GRAY);
    }
    return btnLike;
}
// --- NUEVO MÉTODO: Maneja el like/unlike de un post ---

    private void handleLikeAction(Post post, JButton likeButton) {
        if (loggedUser == null) {
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para dar like.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = loggedUser.getUsername();
        User author = userManager.getUserByUsername(post.getAuthorUsername());
        if (author == null) {
            return;
        }

        if (post.isLikedBy(username)) {
            post.unlike(username);
            likeButton.setText("♡");
            likeButton.setForeground(Color.GRAY);
        } else {
            post.like(username);
            likeButton.setText("❤️");
            likeButton.setForeground(Color.RED);
        }

        userManager.saveUser(author);

        if (post.getAuthorUsername().equals(loggedUser.getUsername())) {
            loggedUser = userManager.getUserByUsername(username);
        }
    }

// --- NUEVO MÉTODO: Muestra el diálogo de comentarios ---
    private void showCommentDialog(Post post) {
        if (loggedUser == null) {
            JOptionPane.showMessageDialog(this, "Inicia sesión para comentar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Comentarios de " + post.getAuthorUsername(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.getContentPane().setBackground(POST_BG);

        // 1. Panel de Comentarios Existentes
        JPanel commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(POST_BG);
        commentsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Lógica para cargar y mostrar los comentarios (opción "a" de tu instrucción)
        List<Comment> postComments = post.getComments();

        if (postComments.isEmpty()) {
            JLabel empty = new JLabel("Sé el primero en comentar.");
            empty.setForeground(Color.GRAY);
            commentsPanel.add(empty);
        } else {
            // Ordenar del más reciente al más antiguo
            postComments.sort(Comparator.comparing(Comment::getDate).reversed());

            for (Comment comment : postComments) {
                JLabel lblComment = new JLabel("<html><b>" + comment.getUsername() + "</b>: " + comment.getText() + "<br><small style='color: gray;'>" + comment.getFormattedDate() + "</small></html>");
                lblComment.setForeground(TEXT_COLOR);
                lblComment.setBorder(new EmptyBorder(5, 0, 5, 0));
                commentsPanel.add(lblComment);
                commentsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
            }
        }

        JScrollPane scrollComments = new JScrollPane(commentsPanel);
        scrollComments.setBorder(null);
        dialog.add(scrollComments, BorderLayout.CENTER);

        // 2. Panel para Agregar Nuevo Comentario
        JPanel addCommentPanel = new JPanel(new BorderLayout(5, 5));
        addCommentPanel.setBackground(POST_BG);
        addCommentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtComment = styledTextField("Añadir un comentario...");
        JButton btnPostComment = styledButton("Enviar");
        btnPostComment.setPreferredSize(new Dimension(80, 35));
        btnPostComment.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnPostComment.addActionListener(e -> {
            String commentText = txtComment.getText().trim();
            if (!commentText.isEmpty()) {
                User author = userManager.getUserByUsername(post.getAuthorUsername());

                // Crear el nuevo comentario
                Comment newComment = new Comment(loggedUser.getUsername(), commentText);

                // Añadir al post y guardar
                post.addComment(newComment);
                userManager.saveUser(author);

                // Recargar la vista de comentarios o el diálogo
                dialog.dispose();
                showCommentDialog(post); // Llama recursivamente para recargar
            }
        });

        addCommentPanel.add(txtComment, BorderLayout.CENTER);
        addCommentPanel.add(btnPostComment, BorderLayout.EAST);

        dialog.add(addCommentPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // --- PANEL CREAR POST (NUEVO) ---
   private JPanel crearPanelCrearPostContent() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(BG_COLOR);
    
    // ❌ ELIMINADO: Antes tenía: panel.add(crearSidebarDesktop(), BorderLayout.WEST);
    // ¡ESTO ES LO CLAVE! Ahora el sidebar se agrega en el contenedor raíz (rootView)
    // fuera de este CardLayout.

    // Contenedor Central con GridBagLayout para centrar el formulario
    JPanel centerWrapper = new JPanel(new GridBagLayout());
    centerWrapper.setBackground(BG_COLOR);

    // ... (El resto del código del formulario de publicación no necesita cambios) ...

    JPanel formCard = new JPanel(new BorderLayout(10, 10));
    formCard.setPreferredSize(new Dimension(500, 500));
    formCard.setBackground(POST_BG);
    formCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // ... (JLabel title, JTextArea txtCaption, JScrollPane, JButton btnSelectImage, etc.) ...
    
    // 1. Título
    JLabel title = new JLabel("✨ Crear Nueva Publicación", SwingConstants.CENTER);
    title.setFont(new Font("SansSerif", Font.BOLD, 20));
    title.setForeground(TEXT_COLOR);
    formCard.add(title, BorderLayout.NORTH);

    // 2. Formulario de Post (Caption, Imagen, Status)
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
            
            // 🚨 CAMBIO CRÍTICO A PARTIR DE AQUÍ:
            
            // 1. Crear un nombre único para el archivo (usando la ruta del usuario logueado)
            if (loggedUser == null) {
                lblImageStatus.setText("Error: Usuario no logueado.");
                lblImageStatus.setForeground(Color.RED);
                return;
            }
            
            String username = loggedUser.getUsername();
            // Creamos un nombre único usando el timestamp para evitar colisiones
            String fileExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
            String uniqueFileName = "post_img_" + System.currentTimeMillis() + fileExtension;

            // 2. Definir la ruta de destino dentro de la CARPETA DEL USUARIO
            // Ejemplo: Link/post_img_123456.png
            File userFolder = new File(username);
            File destFile = new File(userFolder, uniqueFileName);
            
            try {
                // 3. ASEGURAR QUE LA CARPETA EXISTA (aunque ya se hace en registrarUsuario, es seguro repetirlo)
                if (!userFolder.exists()) {
                    userFolder.mkdirs(); 
                }

                // 4. Copiar el archivo seleccionado a la carpeta del usuario
                java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // 5. Guardar la RUTA RELATIVA (carpeta/archivo) que debe usar la GUI
                imagePath[0] = username + File.separator + uniqueFileName;
                lblImageStatus.setText("Archivo guardado: " + uniqueFileName);
                lblImageStatus.setForeground(BTN_BLUE);
                
            } catch (Exception ex) {
                lblImageStatus.setText("Error al guardar imagen: " + ex.getMessage());
                lblImageStatus.setForeground(Color.RED);
            }
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
    btnPost.setBackground(new Color(255, 105, 180)); // Pinkish color for post

    btnPost.addActionListener(e -> {
        try {
            String caption = txtCaption.getText().trim();
            String path = imagePath[0];

            if (loggedUser == null) {
                throw new Exception("Debes iniciar sesión para publicar.");
            }
            if (path.isEmpty() || path.equals("default_user.png")) {
                throw new EmptyFieldException("Debes seleccionar una imagen para la publicación.");
            }
            // Si el caption está vacío, lo dejamos vacío, es válido.

            // 1. Crear el objeto Post
            Post newPost = new Post(loggedUser.getUsername(), path, caption);
            
            // 2. 🚨 CORRECCIÓN CRÍTICA: Llamar al método de persistencia híbrida
            // ESTO REEMPLAZA las 5 líneas de userManager.getUserByUsername, addPost y saveUser.
            // Si userManager.publishPost() falla, lanza una Exception de IO.
            userManager.publishPost(newPost);
            
            // 3. Actualizar la instancia local del usuario logueado (Opcional, pero seguro)
            // Ya que publishPost guarda la instancia en la lista central, refrescamos loggedUser
            loggedUser = userManager.getUserByUsername(loggedUser.getUsername());

            JOptionPane.showMessageDialog(this, "Publicación creada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos y volver al inicio
            txtCaption.setText("");
            imagePath[0] = "";
            lblImageStatus.setText("Archivo: Ninguno seleccionado");
            lblImageStatus.setForeground(TEXT_COLOR);

            // ⚠️ CRÍTICO: Navegar de vuelta al Feed del centro (MAIN)
            CardLayout clCenter = (CardLayout) centerContentPanel.getLayout();
            clCenter.show(centerContentPanel, "MAIN"); 

            // 2. Recargar el feed para mostrar el nuevo post
            rebuildMainFeed();

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

    private void showPostDetail(Post post) {

        // Almacena datos de identificación del post original
        String originalAuthorUsername = post.getAuthorUsername();
        String originalImagePath = post.getImagePath();
        String originalPostId = post.getId(); // Asume que getId() ya maneja nulos de forma segura

        // 1. OBTENER LA VERSIÓN MÁS RECIENTE DEL AUTOR DESDE EL ARCHIVO
        User author = userManager.getUserByUsername(originalAuthorUsername);

        if (author == null) {
            return;
        }

        // 2. ENCONTRAR EL POST REFRESCADO en la lista del autor recargado.
        Post refreshedPost = author.getPosts().stream()
                // Prioridad 1: Buscar por ID de forma segura
                .filter(p -> java.util.Objects.equals(p.getId(), originalPostId))
                .findFirst()
                // Respaldo: Si el ID falla o es nulo, buscar por imagePath
                .orElseGet(() -> author.getPosts().stream()
                .filter(p -> p.getImagePath().equals(originalImagePath))
                .findFirst()
                .orElse(post));

        // 3. Manipulación del CardLayout (La lógica que faltaba)
        if (mainPanel.getLayout() instanceof CardLayout) {

            // --- A. Crear el panel de detalle usando el post REFRESCADO ---
            JPanel detailView = createDetailedPostPanel(refreshedPost);

            CardLayout cl = (CardLayout) mainPanel.getLayout();

            // --- B. Remover la tarjeta anterior (limpieza) ---
            // Eliminar el componente con el nombre "POST_DETAIL_VIEW" si ya existe
            for (Component comp : mainPanel.getComponents()) {
                if (comp.getName() != null && comp.getName().equals("POST_DETAIL_VIEW")) {
                    mainPanel.remove(comp);
                    break;
                }
            }

            // --- C. Añadir y Mostrar la nueva tarjeta ---
            detailView.setName("POST_DETAIL_VIEW"); // Le damos un nombre para la limpieza futura
            mainPanel.add(detailView, "POST_DETAIL"); // Usamos "POST_DETAIL" como clave para el CardLayout

            cl.show(mainPanel, "POST_DETAIL");
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    private JPanel createDetailedPostPanel(Post post) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JButton btnClose = new JButton("X");
        btnClose.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnClose.setForeground(TEXT_COLOR);
        btnClose.setBackground(BG_COLOR);
        btnClose.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel closePanel = new JPanel(new BorderLayout());
        closePanel.setBackground(BG_COLOR);
        closePanel.add(btnClose, BorderLayout.EAST);

        btnClose.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "PROFILE_SEARCH");
        });

        JPanel contentWrapper = new JPanel(new GridBagLayout());
        contentWrapper.setBackground(POST_BG);
        contentWrapper.setBorder(new LineBorder(BORDER_COLOR, 1));
        contentWrapper.setPreferredSize(new Dimension(800, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        int imgSize = 500;

        JLabel lblImage = new JLabel();
        ImageIcon postIcon = cargarImagenCuadrada(post.getImagePath(), imgSize);

        if (postIcon != null) {
            lblImage.setIcon(postIcon);
        } else {
            lblImage.setText("Imagen no disponible");
            lblImage.setForeground(Color.RED);
        }
        lblImage.setPreferredSize(new Dimension(imgSize, imgSize));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentWrapper.add(lblImage, gbc);
        
        JPanel commentsInnerPanel = createCommentsSidePanel(post); 
    
        // 1. Crear el JScrollPane y poner el panel de comentarios dentro
        JScrollPane commentsScrollPane = new JScrollPane(commentsInnerPanel);
        commentsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentsScrollPane.setBorder(null); // Quitar el borde por defecto
        commentsScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll más rápido

        // 2. Asignar el tamaño al JScrollPane
        commentsScrollPane.setPreferredSize(new Dimension(300, imgSize)); 

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        // 3. Agregar el JScrollPane al contentWrapper
        contentWrapper.add(commentsScrollPane, gbc);
    
        JPanel sidePanel = createCommentsSidePanel(post);
        sidePanel.setPreferredSize(new Dimension(300, imgSize));

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        contentWrapper.add(sidePanel, gbc);

        JButton likeButton = new JButton();

        if (loggedUser != null && post.isLikedBy(loggedUser.getUsername())) {
            likeButton.setText("❤️");
            likeButton.setForeground(Color.RED);
        } else {
            likeButton.setText("❤");
            likeButton.setForeground(Color.GRAY);
        }

        likeButton.addActionListener(e -> handleLikeAction(post, likeButton));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(POST_BG);
        bottomPanel.add(likeButton);

        panel.add(closePanel, BorderLayout.NORTH);
        panel.add(contentWrapper, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCommentsSidePanel(Post post) {
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setBackground(POST_BG);

        // --- A. CABECERA (AUTOR DEL POST) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(POST_BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Etiqueta del autor (Clicable)
        JButton btnAuthor = new JButton("@" + post.getAuthorUsername());
        btnAuthor.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAuthor.setForeground(TEXT_COLOR);
        btnAuthor.setBackground(POST_BG);
        btnAuthor.setBorderPainted(false);
        btnAuthor.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAuthor.addActionListener(e -> {
            User author = userManager.getUserByUsername(post.getAuthorUsername());
            if (author != null) {
                // Regresar al panel de búsqueda/perfiles
                CardLayout cl = (CardLayout) mainPanel.getLayout();
                cl.show(mainPanel, "PROFILE_SEARCH");

                // Mostrar el perfil del autor en el sub-CardLayout
                mostrarPerfil(author);
            }
        });

        header.add(btnAuthor, BorderLayout.WEST);
        sidePanel.add(header, BorderLayout.NORTH);

        // --- B. ÁREA DE COMENTARIOS Y CAPTION (CENTRO) ---
        // Usamos BoxLayout para la descripción y los comentarios
        JPanel commentsAndCaptionPanel = new JPanel();
        commentsAndCaptionPanel.setLayout(new BoxLayout(commentsAndCaptionPanel, BoxLayout.Y_AXIS));
        commentsAndCaptionPanel.setBackground(POST_BG);
        commentsAndCaptionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Descripción (Caption) del Post
        JLabel lblCaption = new JLabel("<html><b>" + post.getAuthorUsername() + "</b>: " + post.getCaption() + "</html>");
        lblCaption.setForeground(TEXT_COLOR);
        lblCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentsAndCaptionPanel.add(lblCaption);
        commentsAndCaptionPanel.add(Box.createVerticalStrut(10));

        // 2. Comentarios
        for (Comment comment : post.getComments()) {
            commentsAndCaptionPanel.add(createClickableComment(post, comment));
            commentsAndCaptionPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane commentsScrollPane = new JScrollPane(commentsAndCaptionPanel);
        commentsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commentsScrollPane.setBorder(null);
        sidePanel.add(commentsScrollPane, BorderLayout.CENTER);

        // --- C. BOTONES DE ACCIÓN Y AÑADIR COMENTARIO (SUR) ---
        JPanel actionAndInputPanel = new JPanel(new BorderLayout());
        actionAndInputPanel.setBackground(POST_BG);

        // 1. Panel de Likes (Actualización en tiempo real)
        JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        likePanel.setBackground(POST_BG);

        JButton btnLike = createLikeButton(post);
        JLabel lblLikesCount = new JLabel(post.getLikesCount() + " Me gusta");
        lblLikesCount.setForeground(TEXT_COLOR);
        lblLikesCount.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Lógica para el Like (Actualización inmediata)
        btnLike.addActionListener(e -> {
            // Usa tu método existente, pero actualiza solo las etiquetas locales
            handleLikeAction(post, btnLike); // Esto guarda la persistencia y actualiza el botón de forma visual
            lblLikesCount.setText(post.getLikesCount() + " Me gusta");
        });

        likePanel.add(btnLike);
        likePanel.add(lblLikesCount);
        actionAndInputPanel.add(likePanel, BorderLayout.NORTH);

        // 2. Input para Añadir Comentario
        actionAndInputPanel.add(createCommentInputPanel(post), BorderLayout.CENTER);

        sidePanel.add(actionAndInputPanel, BorderLayout.SOUTH);

        return sidePanel;
    }

    // --- NUEVO MÉTODO: Crea el input para agregar un comentario ---
    private JPanel createCommentInputPanel(Post post) {
        JPanel addCommentPanel = new JPanel(new BorderLayout(5, 5));
        addCommentPanel.setBackground(POST_BG);
        addCommentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Campo de texto para el comentario (usando tu método de estilo existente)
        JTextField txtComment = styledTextField("Añadir un comentario...");

        // Botón de Enviar (usando tu método de estilo existente)
        JButton btnPostComment = styledButton("Enviar");
        btnPostComment.setPreferredSize(new Dimension(80, 35));
        btnPostComment.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Lógica al hacer clic en ENVIAR
        btnPostComment.addActionListener(e -> {
        String commentText = txtComment.getText().trim();
        if (loggedUser == null) {
            JOptionPane.showMessageDialog(this, "Inicia sesión para comentar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!commentText.isEmpty()) {
            
            // 1. Crear el nuevo comentario
            Comment newComment = new Comment(loggedUser.getUsername(), commentText);
            
            // 2. LLAMADA CRÍTICA: Delegar la lógica de adición y guardado al UserManager
            // Esto garantiza que el post dentro de la lista del autor sea actualizado antes de la serialización.
            boolean success = userManager.addCommentAndSave(post, newComment);

            if (success) {
                // 3. Limpiar el campo de texto
                txtComment.setText("");
                
                // 4. Recargar la vista de detalle para mostrar el nuevo comentario
                // La vista detallada cargará el post recién guardado del archivo.
                showPostDetail(post);
            } else {
                 JOptionPane.showMessageDialog(this, "Error al guardar el comentario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

        addCommentPanel.add(txtComment, BorderLayout.CENTER);
        addCommentPanel.add(btnPostComment, BorderLayout.EAST);

        return addCommentPanel;
    }

    private JButton createClickableComment(Post post, Comment comment) {
        // Usamos JButton para que sea fácilmente clicable y se vea como link
        JButton btnComment = new JButton();
        btnComment.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnComment.setBackground(POST_BG);
        btnComment.setBorderPainted(false);
        btnComment.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Texto del comentario formateado (Username en negrita, texto normal)
        JLabel lblUsername = new JLabel("<html><b>" + comment.getUsername() + "</b>: " + comment.getText() + "</html>");
        lblUsername.setForeground(TEXT_COLOR);
        lblUsername.setFont(new Font("SansSerif", Font.PLAIN, 12));

        btnComment.add(lblUsername);

        btnComment.addActionListener(e -> {
            User commenter = userManager.getUserByUsername(comment.getUsername());
            if (commenter != null) {
                // Al hacer clic en el comentario, volvemos a la vista de perfil y mostramos al comentarista
                CardLayout cl = (CardLayout) mainPanel.getLayout();
                cl.show(mainPanel, "PROFILE_SEARCH");
                mostrarPerfil(commenter); // Muestra el perfil del comentarista
            }
        });

        return btnComment;
    }

    // --- PANEL DE BÚSQUEDA Y VISUALIZACIÓN DE PERFIL (EXISTENTE) ---
    private JPanel crearPanelProfileSearch() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(BG_COLOR);

    JPanel centerPanel = new JPanel(new GridBagLayout());
    centerPanel.setBackground(BG_COLOR);

    // Aseguramos que profileCardContainer sea una variable de instancia
    profileCardContainer = new JPanel(new CardLayout());
    profileCardContainer.setPreferredSize(new Dimension(630, 650));
    profileCardContainer.setBackground(BG_COLOR);

    // 1. Añadir la vista de entrada de búsqueda (debes crear un método auxiliar para tu código anterior)
    // ⚠️ Importante: El método auxiliar debe devolver SOLO la interfaz de búsqueda.
    profileCardContainer.add(crearSearchInputView(), "SEARCH_INPUT"); 
    
    // 2. Añadir un panel vacío para la vista de PERFIL
    // La función mostrarPerfil() usará esta clave para mostrar el perfil.
    profileCardContainer.add(new JPanel(), "PROFILE_VIEW"); 
    
    // 3. Establecer el estado inicial a la búsqueda
    CardLayout cl = (CardLayout) profileCardContainer.getLayout();
    cl.show(profileCardContainer, "SEARCH_INPUT"); 

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.CENTER;
    centerPanel.add(profileCardContainer, gbc);

    panel.add(centerPanel, BorderLayout.CENTER);
    return panel;
}
    
    /**
 * Crea y devuelve el panel con el campo de texto y el área de resultados.
 * Este panel es la tarjeta "SEARCH_INPUT" dentro de profileCardContainer.
 */
private JPanel crearSearchInputView() {
    JPanel searchInputView = new JPanel(new BorderLayout());
    searchInputView.setBackground(BG_COLOR);

    // --- 1. Panel Superior (Input y Botón) ---
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setBackground(BG_COLOR);
    topPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    JLabel title = new JLabel("Buscar Personas", SwingConstants.CENTER);
    title.setFont(new Font("SansSerif", Font.BOLD, 24));
    title.setForeground(TEXT_COLOR);
    title.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Asignación a la variable de INSTANCIA
    txtSearchUser = styledTextField("Escribe un username...");
    txtSearchUser.setMaximumSize(new Dimension(400, 40));
    txtSearchUser.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton btnSearch = styledButton("Buscar");
    btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnSearch.setMaximumSize(new Dimension(400, 40));

    topPanel.add(title);
    topPanel.add(Box.createVerticalStrut(20));
    topPanel.add(txtSearchUser);
    topPanel.add(Box.createVerticalStrut(10));
    topPanel.add(btnSearch);

    // --- 2. Panel de Resultados (Centro con Scroll) ---
    // Asignación a la variable de INSTANCIA
    resultsPanel = new JPanel();
    resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
    resultsPanel.setBackground(BG_COLOR);

    JScrollPane scrollResults = new JScrollPane(resultsPanel);
    scrollResults.setBorder(null);
    scrollResults.getVerticalScrollBar().setBackground(BG_COLOR);
    scrollResults.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    // Acción del Botón Buscar
    btnSearch.addActionListener(e -> {
        String texto = txtSearchUser.getText().trim();
        if (texto.isEmpty() || texto.equals("Escribe un username...")) {
            JOptionPane.showMessageDialog(this, "Escribe algo para buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Limpiar resultados anteriores
        resultsPanel.removeAll();

        // 2. Buscar en el UserManager
        List<User> encontrados = userManager.searchUsers(texto);

        if (encontrados.isEmpty()) {
            JLabel lblNo = new JLabel("No se encontraron usuarios con: " + texto);
            lblNo.setForeground(Color.GRAY);
            lblNo.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(lblNo);
        } else {
            // 3. Llenar la lista
            for (User u : encontrados) {
                // Determinar estado (LO SIGO / NO LO SIGUES)
                String estado = loggedUser.isFollowing(u.getUsername()) ? "LO SIGO" : "NO LO SIGUES";
                String textoResultado = u.getUsername() + " – " + estado;

                // Crear un botón o panel clicable para el resultado
                JButton itemBtn = new JButton(textoResultado);
                itemBtn.setFont(new Font("SansSerif", Font.PLAIN, 16));
                itemBtn.setForeground(TEXT_COLOR);
                itemBtn.setBackground(INPUT_BG);
                itemBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                itemBtn.setMaximumSize(new Dimension(400, 50));
                itemBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                itemBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                itemBtn.setFocusPainted(false);

                // Al hacer clic, ir al perfil
                itemBtn.addActionListener(ev -> {
                    // Navega a la vista de perfil (usando el CardLayout interno)
                    mostrarPerfil(u);
                });

                resultsPanel.add(itemBtn);
                resultsPanel.add(Box.createVerticalStrut(10)); // Espacio entre items
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    });

    searchInputView.add(topPanel, BorderLayout.NORTH);
    searchInputView.add(scrollResults, BorderLayout.CENTER);

    return searchInputView;
}

// 💡 Debes crear este nuevo método auxiliar:
// private JPanel crearSearchInputView() { /* Devuelve el contenido de tu antiguo crearProfileCardSearch */ }

    // Tarjeta inicial para la búsqueda de perfiles (EXISTENTE)
    // Reemplaza tu método crearProfileCardSearch() con este:
    private JPanel crearProfileCardSearch() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(BG_COLOR);
        

        // --- 1. Panel Superior (Input y Botón) ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Buscar Personas", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtSearchUser = styledTextField("Escribe un username...");
        txtSearchUser.setMaximumSize(new Dimension(400, 40));
        txtSearchUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnSearch = styledButton("Buscar");
        btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSearch.setMaximumSize(new Dimension(400, 40));

        topPanel.add(title);
        topPanel.add(Box.createVerticalStrut(20));
        topPanel.add(txtSearchUser);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(btnSearch);

        // --- 2. Panel de Resultados (Centro con Scroll) ---
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BG_COLOR);

        JScrollPane scrollResults = new JScrollPane(resultsPanel);
        scrollResults.setBorder(null);
        scrollResults.getVerticalScrollBar().setBackground(BG_COLOR);
        scrollResults.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Acción del Botón Buscar
        btnSearch.addActionListener(e -> {
            String texto = txtSearchUser.getText().trim();
            if (texto.isEmpty() || texto.equals("Escribe un username...")) {
                JOptionPane.showMessageDialog(this, "Escribe algo para buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Limpiar resultados anteriores
            resultsPanel.removeAll();

            // 2. Buscar en el UserManager (usando el nuevo método)
            List<User> encontrados = userManager.searchUsers(texto);

            if (encontrados.isEmpty()) {
                JLabel lblNo = new JLabel("No se encontraron usuarios con: " + texto);
                lblNo.setForeground(Color.GRAY);
                lblNo.setAlignmentX(Component.CENTER_ALIGNMENT);
                resultsPanel.add(lblNo);
            } else {
                // 3. Llenar la lista
                for (User u : encontrados) {
                    // Determinar estado (LO SIGO / NO LO SIGUES)
                    String estado = loggedUser.isFollowing(u.getUsername()) ? "LO SIGO" : "NO LO SIGUES";
                    String textoResultado = u.getUsername() + " – " + estado;

                    // Crear un botón o panel clicable para el resultado
                    JButton itemBtn = new JButton(textoResultado);
                    itemBtn.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    itemBtn.setForeground(TEXT_COLOR);
                    itemBtn.setBackground(INPUT_BG);
                    itemBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    itemBtn.setMaximumSize(new Dimension(400, 50));
                    itemBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                    itemBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    itemBtn.setFocusPainted(false);

                    // Al hacer clic, ir al perfil
                    itemBtn.addActionListener(ev -> {
                        mostrarPerfil(u);
                    });

                    resultsPanel.add(itemBtn);
                    resultsPanel.add(Box.createVerticalStrut(10)); // Espacio entre items
                }
            }

            resultsPanel.revalidate();
            resultsPanel.repaint();
        });

        searchPanel.add(topPanel, BorderLayout.NORTH);
        searchPanel.add(scrollResults, BorderLayout.CENTER);

        return searchPanel;
    }
    
    /**
 * Restaura la vista de búsqueda a su estado inicial: limpio y listo para buscar.
 */
private void mostrarPanelDeBusqueda() {
    // 1. 🚨 CRÍTICO: Navegar al estado de búsqueda dentro del CardLayout anidado
    if (profileCardContainer != null) {
        CardLayout clProfile = (CardLayout) profileCardContainer.getLayout();
        // Le decimos al contenedor interno que muestre la interfaz de búsqueda.
        clProfile.show(profileCardContainer, "SEARCH_INPUT"); 

        // 2. Limpiar el área de resultados (si está en la tarjeta SEARCH_INPUT)
        if (resultsPanel != null) {
            resultsPanel.removeAll();
            
            // Opcional: Agregar un mensaje de bienvenida si quieres que la pantalla no esté vacía.
            JLabel lblWelcome = new JLabel("Escribe un nombre de usuario para empezar la búsqueda.");
            lblWelcome.setForeground(Color.GRAY);
            lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(lblWelcome);
            
            // Limpiar el campo de texto
            if (txtSearchUser != null) {
                txtSearchUser.setText("Escribe un username..."); // Resetear el placeholder si es necesario
            }
            
            resultsPanel.revalidate();
            resultsPanel.repaint();
        }
        
        profileCardContainer.revalidate();
        profileCardContainer.repaint();
    }
}

// Dentro de la clase InstagramProject:

// Variables de instancia necesarias (declararlas al inicio de la clase):

public JPanel crearPanelBusqueda() {
    JPanel mainSearchPanel = new JPanel(new BorderLayout());
    mainSearchPanel.setBackground(BG_COLOR);

    // Inicializar el CardLayout Contenedor
    profileCardContainer = new JPanel(new CardLayout());
    profileCardContainer.setBackground(BG_COLOR);
    
    // 1. Tarjeta de Búsqueda (SEARCH_INPUT)
    JPanel searchInputPanel = createSearchInputPanel();
    profileCardContainer.add(searchInputPanel, "SEARCH_INPUT");

    // 2. Tarjeta de Vista de Perfil (PROFILE_VIEW)
    // Ya la manejamos en mostrarPerfil(), pero la agregamos como placeholder.
    profileCardContainer.add(new JPanel(), "PROFILE_VIEW"); 
    
    mainSearchPanel.add(profileCardContainer, BorderLayout.CENTER);
    return mainSearchPanel;
}

// Dentro de la clase InstagramProject:

private JPanel createSearchInputPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(BG_COLOR);
    
    // Panel Wrapper para centrar el formulario de búsqueda
    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
    wrapper.setBackground(BG_COLOR);
    wrapper.setBorder(new EmptyBorder(20, 0, 0, 0));
    
    JPanel searchBar = new JPanel(new BorderLayout(5, 0));
    searchBar.setPreferredSize(new Dimension(500, 35));
    searchBar.setMaximumSize(new Dimension(500, 35));
    searchBar.setBackground(INPUT_BG);
    searchBar.setBorder(new LineBorder(BORDER_COLOR, 1));
    
    // Campo de texto de búsqueda (Lo guardamos como variable de instancia)
    txtSearchUser = new JTextField("Escribe un username...");
    txtSearchUser.setBackground(INPUT_BG);
    txtSearchUser.setForeground(TEXT_COLOR);
    txtSearchUser.setCaretColor(TEXT_COLOR);
    txtSearchUser.setBorder(new EmptyBorder(0, 10, 0, 10));
    txtSearchUser.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusGained(java.awt.event.FocusEvent evt) {
            if (txtSearchUser.getText().equals("Escribe un username...")) {
                txtSearchUser.setText("");
            }
        }
        public void focusLost(java.awt.event.FocusEvent evt) {
            if (txtSearchUser.getText().isEmpty()) {
                txtSearchUser.setText("Escribe un username...");
            }
        }
    });
    
    // Botón de búsqueda
    JButton btnSearch = styledButton("Buscar");
    btnSearch.setPreferredSize(new Dimension(100, 35));
    btnSearch.setBackground(BTN_BLUE);
    
    // 🚨 Action: Llamar a la lógica de búsqueda
    btnSearch.addActionListener(e -> performUserSearch(txtSearchUser.getText()));
    
    searchBar.add(txtSearchUser, BorderLayout.CENTER);
    searchBar.add(btnSearch, BorderLayout.EAST);
    wrapper.add(searchBar);
    
    panel.add(wrapper, BorderLayout.NORTH);

    // Área de Resultados (Guardamos como variable de instancia)
    resultsPanel = new JPanel();
    resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
    resultsPanel.setBackground(BG_COLOR);
    resultsPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
    
    JScrollPane scrollResults = new JScrollPane(resultsPanel);
    scrollResults.setBorder(null);
    scrollResults.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    panel.add(scrollResults, BorderLayout.CENTER);
    
    return panel;
}

// Dentro de la clase InstagramProject:

private void performUserSearch(String query) {
    if (query == null || query.trim().isEmpty() || query.trim().equals("Escribe un username...")) {
        JOptionPane.showMessageDialog(this, "Ingresa un nombre de usuario válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    resultsPanel.removeAll();
    
    // 1. LLAMADA CRÍTICA: Buscar usuarios en el manager
    // Usamos el manager, que lee de la lista serializada de usuarios (users.dat)
    List<User> foundUsers = userManager.searchUsers(query.trim());
    
    if (foundUsers.isEmpty()) {
        JLabel lblNotFound = new JLabel("No se encontraron usuarios que coincidan con '" + query + "'.");
        lblNotFound.setForeground(Color.RED);
        lblNotFound.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(lblNotFound);
    } else {
        // 2. Mostrar los resultados
        for (User user : foundUsers) {
            if (!user.getUsername().equals(loggedUser.getUsername())) { // No mostrar el usuario logueado
                resultsPanel.add(createUserResultItem(user));
                resultsPanel.add(Box.createVerticalStrut(10));
            }
        }
    }
    
    resultsPanel.revalidate();
    resultsPanel.repaint();
}

// Dentro de la clase InstagramProject:

private JPanel createUserResultItem(User user) {
    JPanel item = new JPanel(new BorderLayout(10, 5));
    item.setBackground(POST_BG);
    item.setBorder(new LineBorder(BORDER_COLOR, 1, true));
    item.setMaximumSize(new Dimension(500, 60)); 
    
    // Foto
    JLabel lblPhoto = new JLabel(cargarImagenCuadrada(user.getFotoPath(), 40));
    lblPhoto.setBorder(new EmptyBorder(5, 5, 5, 5));
    item.add(lblPhoto, BorderLayout.WEST);
    
    // Info
    JLabel lblInfo = new JLabel("<html><b>@" + user.getUsername() + "</b><br>" + user.getNombre() + "</html>");
    lblInfo.setForeground(TEXT_COLOR);
    item.add(lblInfo, BorderLayout.CENTER);
    
    // Botón Ver Perfil
    JButton btnView = styledButton("Ver Perfil");
    btnView.setPreferredSize(new Dimension(100, 30));
    btnView.addActionListener(e -> {
        // Navegar a la vista del perfil del usuario encontrado
        mostrarPerfil(user); 
        // mostrarPerfil ya se encarga de cambiar el CardLayout interno a "PROFILE_VIEW"
    });
    
    JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnWrapper.setBackground(POST_BG);
    btnWrapper.add(btnView);
    item.add(btnWrapper, BorderLayout.EAST);
    
    return item;
}

    private void mostrarPerfil(User targetUser) {
        // Eliminar vistas anteriores y añadir la nueva

        // **IMPORTANTE**: Recargar el targetUser para asegurar que los contadores (followers) estén actualizados
        User refreshedTargetUser = userManager.getUserByUsername(targetUser.getUsername());
        if (refreshedTargetUser == null) {
            JOptionPane.showMessageDialog(this, "Error al cargar el perfil.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel profileView = buildProfileView(refreshedTargetUser);
        profileCardContainer.add(profileView, "PROFILE_VIEW");

        for (Component comp : profileCardContainer.getComponents()) {
            // Asumiendo que SÓLO la vista del perfil tiene un nombre específico
            if (comp.getName() != null && comp.getName().equals("PROFILE_VIEW_CONTENT")) {
                profileCardContainer.remove(comp);
                break;
            }
        }

        // 3. Añadir la nueva vista (y le damos un nombre específico)
        profileView.setName("PROFILE_VIEW_CONTENT");
        profileCardContainer.add(profileView, "PROFILE_VIEW"); // La etiqueta del CardLayout sigue siendo "PROFILE_VIEW"

        // 4. Mostrar y actualizar
        CardLayout cl = (CardLayout) (profileCardContainer.getLayout());
        cl.show(profileCardContainer, "PROFILE_VIEW");

        profileCardContainer.revalidate();
        profileCardContainer.repaint();
    }

    // --- Método de Utilidad: cargarImagenCuadrada (Añadir a InstagramProject) ---
// Es vital para mostrar fotos de perfil y posts.

private ImageIcon cargarImagenCuadrada(String path, int size) {
    // 1. Manejar la ruta (por ejemplo, si se guarda solo el nombre del archivo)
    File imgFile = new File(path);

    // 2. Si el archivo no existe o la ruta es inválida, usar placeholder (por ejemplo: default_user.png)
    if (!imgFile.exists() || path == null || path.isEmpty()) {
        imgFile = new File("default_user.png"); // Asegúrate de tener este archivo en la raíz
    }

    try {
        BufferedImage originalImage = ImageIO.read(imgFile);
        if (originalImage == null) {
            // Si ImageIO no pudo leerlo, puede ser que el archivo no sea una imagen válida
            return null;
        }

        // 3. Redimensionar para que encaje en el cuadrado
        Image scaledImage = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        
        // 4. (Opcional, pero recomendado) Crear un borde circular para fotos de perfil si size es pequeño
        if (size <= 50) {
            BufferedImage circleImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Dibujar un círculo
            g2.setClip(new Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(scaledImage, 0, 0, null);
            g2.dispose();
            return new ImageIcon(circleImage);
        }

        return new ImageIcon(scaledImage);

    } catch (IOException e) {
        System.err.println("❌ ERROR: Imagen no encontrada en la ruta: " + path);
        e.printStackTrace();
        // Retorna null o un icono de error
        return null; 
    }
}

    /**
     * Construye la vista completa de un perfil (similar al diseño de
     * Instagram). (EXISTENTE)
     */
    private JPanel buildProfileView(User targetUser) {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(BG_COLOR);
        profilePanel.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1.1. Foto de Perfil (Izquierda)
        int photoSize = 120;

// Crear JLabel para la foto
        JLabel lblPhoto = new JLabel();
        lblPhoto.setPreferredSize(new Dimension(photoSize, photoSize));
        lblPhoto.setMaximumSize(new Dimension(photoSize, photoSize));
        lblPhoto.setMinimumSize(new Dimension(photoSize, photoSize));
        lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhoto.setVerticalAlignment(SwingConstants.CENTER);

        // Borde cuadrado del mismo tamaño que el JLabel
        lblPhoto.setBorder(BorderFactory.createLineBorder(new Color(255, 105, 39), 3));

        // Cargar imagen cuadrada escalada al tamaño interior del JLabel
        ImageIcon icon = cargarImagenCuadrada(targetUser.getFotoPath(), photoSize - 6);
        // 6 = grosor del borde para que no corte la imagen
        if (icon != null) {
            lblPhoto.setIcon(icon);
            lblPhoto.setText("");
        } else {
            lblPhoto.setText("IMG");
            lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // Wrapper para centrar
        JPanel photoWrapper = new JPanel(new BorderLayout());
        photoWrapper.setPreferredSize(new Dimension(photoSize, photoSize));
        photoWrapper.setBackground(BG_COLOR);
        photoWrapper.add(lblPhoto, BorderLayout.CENTER);

        headerPanel.add(photoWrapper, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_COLOR);
        tabs.setForeground(TEXT_COLOR);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.setBorder(new LineBorder(BORDER_COLOR, 1));
        tabs.setOpaque(true);

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
        // CORREGIDO: Llama al método que carga los posts del .ins
// Asumiendo que has implementado loadPostsFromLocalFile(username) en UserManager
    int postCount = userManager.loadPostsFromLocalFile(targetUser.getUsername()).size();
    statsPanel.add(createStatPanel(String.valueOf(postCount), "publicaciones"));
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

        // Lógica para mostrar botones solo si no es mi propio perfil
        if (loggedUser != null && !targetUser.getUsername().equals(loggedUser.getUsername())) {
            boolean isFollowing = loggedUser.isFollowing(targetUser.getUsername());

            // Botón principal (SEGUIR / DEJAR DE SEGUIR)
            JButton btnAction = styledButton(isFollowing ? "DEJAR DE SEGUIR" : "SEGUIR");
            btnAction.setPreferredSize(new Dimension(200, 35));
            btnAction.setMaximumSize(new Dimension(200, 35));
            btnAction.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Estilos visuales según estado
            if (isFollowing) {
                btnAction.setBackground(new Color(54, 54, 54)); // Gris si ya lo sigo
                btnAction.setForeground(TEXT_COLOR);
            } else {
                btnAction.setBackground(BTN_BLUE); // Azul si no lo sigo
            }

            btnAction.addActionListener(e -> {
                if (isFollowing) {
                    // Opción DEJAR DE SEGUIR con Confirmación
                    int respuesta = JOptionPane.showConfirmDialog(this,
                            "¿Estás seguro que quieres dejar de seguir a " + targetUser.getUsername() + "?",
                            "Confirmar",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (respuesta == JOptionPane.YES_OPTION) {
                        userManager.toggleFollow(loggedUser.getUsername(), targetUser.getUsername());
                        // Recargar usuario local y actualizar vista
                        loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                        mostrarPerfil(targetUser);
                        rebuildMainFeed();
                    }
                } else {
                    // Opción SEGUIR (Directo)
                    userManager.toggleFollow(loggedUser.getUsername(), targetUser.getUsername());
                    // Recargar usuario local y actualizar vista
                    loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
                    mostrarPerfil(targetUser);
                }
            });

            actionPanel.add(btnAction);
            actionPanel.add(Box.createVerticalStrut(10));

        } else if (loggedUser != null && targetUser.getUsername().equals(loggedUser.getUsername())) {
            JLabel lblOwn = new JLabel("(Tu Perfil)");
            lblOwn.setForeground(Color.GRAY);
            actionPanel.add(lblOwn);
        }

        // Botón "VER SUS TWEETS" (Decorativo, ya que abajo se muestran)
        // Aunque el grid ya los muestra, agregamos el texto visual para cumplir con el requisito
        JLabel lblVerTweets = new JLabel("⬇ VER SUS TWEETS ⬇");
        lblVerTweets.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblVerTweets.setForeground(Color.GRAY);
        lblVerTweets.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblVerTweets.setBorder(new EmptyBorder(15, 0, 0, 0));

        actionPanel.add(lblVerTweets);

        // ... (El resto del código donde se agrega headerPanel al profilePanel sigue igual) ...
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
// Simular el borde inferior del Tab activo
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        tabBar.setPreferredSize(new Dimension(profilePanel.getWidth(), 50));

// Creamos un JLabel para simular el ícono de Grid (Activo)
        JLabel lblGridIcon = new JLabel("◼️ POSTS");
        lblGridIcon.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblGridIcon.setForeground(TEXT_COLOR);
        tabBar.add(lblGridIcon);

        contentPanel.add(tabBar, BorderLayout.NORTH);

// 2.2. Posts Grid con Scroll (USANDO EL MÉTODO CORREGIDO)
        JPanel postsGrid = crearPostsGrid(targetUser);

// Si el postsGrid es el wrapper de "No hay posts" (que ya incluye el centrado), lo agregamos directo.
        if (targetUser.getPosts().isEmpty()) {
            contentPanel.add(postsGrid, BorderLayout.CENTER);
        } else {
            // Si hay posts, envolvemos el grid en un JScrollPane para el desplazamiento
            JScrollPane scrollPane = new JScrollPane(postsGrid);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setBackground(BG_COLOR);

            contentPanel.add(scrollPane, BorderLayout.CENTER);
        }

        profilePanel.add(contentPanel, BorderLayout.CENTER);
        return profilePanel;
    }

    // --- En tu Clase Principal (Insta.InstagramProject) ---
// --- Método de Recarga en tu Clase Principal ---
// --- Método de Recarga en tu Clase Principal ---
    public void rebuildMainFeed() {
        
        if (loggedUser != null) {
        // Obtenemos la versión más reciente del usuario (con listas de seguidos actualizadas)
        // La implementación de userManager.getUserByUsername() fuerza la recarga desde el archivo.
        loggedUser = userManager.getUserByUsername(loggedUser.getUsername());
    } else {
        // Si loggedUser es NULL, no tiene sentido recargar el feed aún
        // y loadFeedPosts ya mostrará el mensaje de login.
    }
        
        // 1. Buscar el JScrollPane dentro del mainPanel (CardLayout)
        JScrollPane scrollPane = (JScrollPane) getComponentByName(mainPanel, "FEED_SCROLL_PANE");
        

        if (scrollPane == null) {
            System.err.println("Error: El JScrollPane del feed ('FEED_SCROLL_PANE') no fue encontrado en mainPanel.");
            // Si no lo encuentra, forzamos a mostrar la tarjeta para asegurar
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "MAIN");
            return;
        }

        // 2. Obtener la referencia al JPanel interno que contiene los posts
        // Este panel se obtiene de la Viewport del JScrollPane
        JPanel feedContentPanel = (JPanel) getComponentByName(scrollPane.getViewport(), "FEED_POSTS_INNER_PANEL");

        if (feedContentPanel == null) {
            System.err.println("Error: El JPanel interno del feed no fue encontrado.");
            return;
        }

        // 3. Ejecutar la lógica de recarga de posts con la referencia correcta
        int feedWidth = 550;
        loadFeedPosts(feedContentPanel, feedWidth);

        // 4. Mostrar la vista principal y forzar el repintado
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "MAIN");

        mainPanel.revalidate();
        mainPanel.repaint();
        
        if (scrollPane != null) {
        // Obtener la barra de desplazamiento vertical
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            verticalScrollBar.setValue(0);
        });
    }
    }

// --- Asegúrate de tener este método auxiliar ---
// Ya lo usamos en la corrección anterior, pero es clave para esta solución.
    private Component getComponentByName(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
            // Buscar recursivamente si el componente es un contenedor (ej. Viewport)
            if (comp instanceof Container) {
                Component found = getComponentByName((Container) comp, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // Crea una miniatura de post (cuadrado) para la cuadrícula del perfil
    private JPanel crearPostMiniatura(Post post) {
        final int MINI_SIZE = 190; // Tamaño de la miniatura (ajustado para un grid 3x en 600px de ancho)

        JPanel miniatura = new JPanel(new BorderLayout());
        miniatura.setPreferredSize(new Dimension(MINI_SIZE, MINI_SIZE));
        miniatura.setMaximumSize(new Dimension(MINI_SIZE, MINI_SIZE));
        miniatura.setBackground(POST_BG);
        miniatura.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);

        // Cargar imagen cuadrada escalada
        ImageIcon icon = cargarImagenCuadrada(post.getImagePath(), MINI_SIZE);

        if (icon != null) {
            lblImage.setIcon(icon);
            lblImage.setText("");
        } else {
            lblImage.setText("❌");
            lblImage.setForeground(Color.RED);
            lblImage.setFont(new Font("SansSerif", Font.BOLD, 20));
        }

        miniatura.add(lblImage, BorderLayout.CENTER);

        // Opcional: Agregar un efecto visual al pasar el mouse (Overlay)
        // Puedes agregar aquí un MouseListener para mostrar detalles del post (likes, comentarios)
        // al pasar el mouse, simulando el comportamiento de Instagram.
        // Ejemplo de funcionalidad (Click para ver el post completo, si existiera esa vista)
        miniatura.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(miniatura,
                        "Post de " + post.getUsername() + "\nDescripción: " + post.getCaption(),
                        "Ver Post",
                        JOptionPane.INFORMATION_MESSAGE);
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
        navLinks.add(createSidebarButton("🔔 Notificaciones", "NOTIFICATIONS"));
        navLinks.add(createSidebarButton("🔎 Buscar Hashtag", "HASHTAG_SEARCH")); 
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
    // Referencia al CardLayout del contenido central
    CardLayout clCenter = (CardLayout) centerContentPanel.getLayout(); 

    if (cardName.equals("LOGOUT")) {
        // --- CASO 1: LOGOUT (Usa el CardLayout GLOBAL) ---
        int opt = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Confirmar Salida", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            loggedUser = null;
            // Navega del ROOT_VIEW al LOGIN (usando el CardLayout global)
            cardLayout.show(mainPanel, "LOGIN"); 
        }
    } else {
        // --- CASO 2: Navegación de Contenido (Usa el CardLayout CENTRAL) ---
        
        // Antes de navegar, asegúrate de estar en la vista ROOT_VIEW si no lo estás.
        // Esto solo es necesario si se saliera de ROOT_VIEW sin hacer logout, pero por seguridad:
        cardLayout.show(mainPanel, "ROOT_VIEW");
        
        if (loggedUser == null) {
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para acceder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cardName.equals("MAIN")) {
            // Lógica de INICIO (FEED): Recarga y Navega al Feed.
            rebuildMainFeed();
            clCenter.show(centerContentPanel, "MAIN"); 
            
        } else if (cardName.equals("MY_PROFILE")) {
            if (loggedUser != null) {
                // La navegación DEBE usar el CardLayout CENTRAL (clCenter)

                // 1. Navegar al contenedor de perfil (asumiendo que es PROFILE_SEARCH)
                clCenter.show(centerContentPanel, "PROFILE_SEARCH");

                // 2. Cargar y mostrar tu perfil. 
                // Esta función DEBE actualizar el contenido del panel PROFILE_SEARCH.
                mostrarPerfil(loggedUser);

                centerContentPanel.revalidate();
                centerContentPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Debes iniciar sesión primero.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        // ... (dentro del ActionListener del botón de la Sidebar, en la sección 'else' para navegación)

    } else if (cardName.equals("PROFILE_SEARCH")) {
        if (loggedUser != null) {
            
            // 1. 🚨 ¡CRÍTICO! LLAMAR AL RESET AQUÍ
            mostrarPanelDeBusqueda(); // <--- Limpia resultados y campo (Asumimos que este método existe)
            
            // 2. Navegar a la tarjeta contenedora
            clCenter.show(centerContentPanel, "PROFILE_SEARCH"); 
            
            centerContentPanel.revalidate();
            centerContentPanel.repaint();
        }
    } else if (cardName.equals("NOTIFICATIONS")) { // 🚨 NUEVO CASO: NOTIFICACIONES
    if (loggedUser != null) {
        
        // 1. Navegar a la tarjeta
        clCenter.show(centerContentPanel, "NOTIFICATIONS"); 
        
        // 2. Cargar las menciones
        // 🚨 CORRECCIÓN: Llamamos al método loadNotifications() sin argumentos. 
        // Este método accede a notificationsListPanel (variable de instancia) 
        // y actualiza su contenido internamente.
        loadNotifications(); 
        
        // La revalidación del contenedor principal es suficiente
        centerContentPanel.revalidate();
        centerContentPanel.repaint();
    } 
    // Opcional: Si no hay usuario logueado, mostrar el mensaje de inicio de sesión.
    // else {
    //     // Lógica para mostrar la vista de Login o un mensaje de error
    // }
    } else if (cardName.equals("HASHTAG_SEARCH")) { // 🚨 NUEVO CASO: BÚSQUEDA DE HASHTAG
        // Asumimos que la lógica de reinicio está en mostrarPanelHashtagSearch()
        crearPanelHashtagSearchContent();
        clCenter.show(centerContentPanel, "HASHTAG_SEARCH");
        
    } else if (cardName.equals("CREATE_POST")) {
        // Navegación normal para "CREATE_POST" (no requiere recarga de datos)
        clCenter.show(centerContentPanel, "CREATE_POST"); 
        
    } else {
        // CASO POR DEFECTO: Si es una tarjeta simple (ej. HASHTAG_SEARCH o CREATE_POST)
        // Y no requiere recarga especial, esta línea lo cubre (pero es más seguro ser explícito).
        clCenter.show(centerContentPanel, cardName); 
    }
        
        // Se requiere forzar la actualización para layouts complejos o redibujo de scrollpanes
        centerContentPanel.revalidate();
        centerContentPanel.repaint();
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
    
    private JPanel crearPanelHashtagSearchContent() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(BG_COLOR);

    // ❌ CRÍTICO: Eliminado cualquier llamada a crearSidebarDesktop() aquí.
    // Este panel ya no incluye la sidebar.

    // 1. Panel de Búsqueda (NORTH)
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    searchPanel.setBackground(POST_BG);
    searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JTextField txtHashtag = new JTextField(25);
    txtHashtag.setFont(new Font("SansSerif", Font.PLAIN, 16));
    txtHashtag.setText("#Buscar"); // Placeholder

    JButton btnSearch = styledButton("Buscar");
    btnSearch.setPreferredSize(new Dimension(100, 30));

    searchPanel.add(txtHashtag);
    searchPanel.add(btnSearch);

    panel.add(searchPanel, BorderLayout.NORTH);

    // 2. Área de Resultados (CENTER) - Usaremos un CardLayout interno
    JPanel resultsContainer = new JPanel(new CardLayout());
    resultsContainer.setBackground(BG_COLOR);
    resultsContainer.setName("HASHTAG_RESULTS_CONTAINER");

    // Mensaje de inicio
    JPanel defaultMessage = createMessagePanel("Ingresa un hashtag (ej: #viajes) para ver publicaciones.");
    defaultMessage.setName("DEFAULT_MESSAGE");
    resultsContainer.add(defaultMessage, "DEFAULT");
    
    // Panel para resultados (inicialmente vacío)
    // ⚠️ Importante: resultsPanel debe tener un BorderLayout para que displayHashtagResults()
    // pueda añadir el JScrollPane correctamente en BorderLayout.CENTER.
    JPanel resultsPanel = new JPanel(new BorderLayout()); 
    resultsPanel.setName("RESULTS_PANEL");

    resultsContainer.add(resultsPanel, "RESULTS");

    panel.add(resultsContainer, BorderLayout.CENTER);

    // 3. Lógica del Botón de Búsqueda
    btnSearch.addActionListener(e -> {
        String input = txtHashtag.getText().trim();
        if (input.isEmpty() || input.equals("#Buscar")) { // Evitar buscar el placeholder
            JOptionPane.showMessageDialog(this, "Por favor, ingresa un hashtag válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Aseguramos que el hashtag empiece con '#' (si el usuario no lo puso)
        String hashtag = input.startsWith("#") ? input : "#" + input; 
        
        // Ejecutar la búsqueda y mostrar los resultados
        displayHashtagResults(hashtag, resultsContainer, resultsPanel);
    });
    
    // Agregar listener para limpiar el placeholder al hacer click
    txtHashtag.addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent evt) {
            if (txtHashtag.getText().equals("#Buscar")) {
                txtHashtag.setText("");
            }
        }
        @Override
        public void focusLost(java.awt.event.FocusEvent evt) {
            if (txtHashtag.getText().isEmpty()) {
                txtHashtag.setText("#Buscar");
            }
        }
    });

    return panel;
}
    
    /**
 * Busca y muestra todos los posts que contienen el hashtag especificado.
 */
private void displayHashtagResults(String hashtagInput, JPanel container, JPanel resultsPanel) {
    
    // El hashtagInput puede venir con o sin el '#'.
    // Aseguramos que el hashtag empiece con '#' (si el usuario no lo puso)
    String hashtag = hashtagInput.startsWith("#") ? hashtagInput : "#" + hashtagInput; 

    // 1. OBTENER POSTS: DELEGAR la búsqueda al UserManager (Lógica de negocio)
    // Usamos el método searchPostsByHashtag que ya maneja la iteración y los duplicados.
    List<Post> foundPosts = userManager.searchPostsByHashtag(hashtag);

    // 2. Limpieza y Configuración de la Vista
    resultsPanel.removeAll();
    CardLayout cl = (CardLayout) container.getLayout();
    
    // ** CRÍTICO **: Asegurar que resultsPanel tenga un LayoutManager para el JScrollPane.
    // Usamos BorderLayout para que el JScrollPane ocupe todo el espacio.
    // Esto debería haberse definido en crearPanelHashtagSearch(), pero lo forzamos aquí si es necesario:
    if (!(resultsPanel.getLayout() instanceof BorderLayout)) {
        resultsPanel.setLayout(new BorderLayout());
    }
    
    if (foundPosts.isEmpty()) {
        // Si no hay resultados, mostramos el mensaje de "NO_RESULTS"
        JPanel noResults = createMessagePanel("No se encontraron publicaciones con el hashtag: " + hashtag);
        
        // Antes de añadir la nueva tarjeta, removemos cualquier tarjeta antigua con el mismo nombre para evitar conflictos
        for (Component comp : container.getComponents()) {
            if ("NO_RESULTS".equals(comp.getName())) {
                container.remove(comp);
            }
        }
        container.add(noResults, "NO_RESULTS");
        cl.show(container, "NO_RESULTS");
        
    } else {
        // Crear el panel de contenido para los posts
        JPanel postsContainer = new JPanel();
        postsContainer.setLayout(new BoxLayout(postsContainer, BoxLayout.Y_AXIS));
        postsContainer.setBackground(BG_COLOR);
        
        int feedWidth = 550; // Mismo ancho que el feed principal
        
        // Iterar y agregar cada post al contenedor
        for (Post post : foundPosts) {
            JPanel postView = createPostFeedView(post, feedWidth); 
            postsContainer.add(postView);
            postsContainer.add(Box.createVerticalStrut(15)); // Espacio entre posts
        }
        
        // Envolver el contenedor de posts en un JScrollPane
        JScrollPane scrollPane = new JScrollPane(postsContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        // ** CRÍTICO **: Usar invokeLater para restablecer el scroll al inicio (como en rebuildMainFeed)
        javax.swing.SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
        });

        // Agregar el JScrollPane al resultsPanel (que tiene BorderLayout)
        resultsPanel.add(scrollPane, BorderLayout.CENTER); 
        
        cl.show(container, "RESULTS");
    }
    
    // Forzar el repintado y recálculo de layouts
    container.revalidate();
    container.repaint();
}
// Dentro de la clase InstagramProject

private void attemptPublish(String caption, String imagePath) {
    if (loggedUser == null) {
        JOptionPane.showMessageDialog(this, "Debe iniciar sesión para publicar.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    if (imagePath.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Debe seleccionar una imagen.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Crear el objeto Post
    Post newPost = new Post(
        loggedUser.getUsername(),
        imagePath,
        caption
    );
    // Nota: El constructor de Post establece automáticamente la fecha actual (LocalDateTime.now()).
    
    try {
        // Llama al método de persistencia híbrida
        userManager.publishPost(newPost);
        
        JOptionPane.showMessageDialog(this, "Publicación exitosa!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, 
            "Error de persistencia al guardar el post:\n" + e.getMessage(), 
            "Error de I/O", JOptionPane.ERROR_MESSAGE);
    } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

/**
 * Método auxiliar para crear un panel con mensaje centrado.
 */
private JPanel createMessagePanel(String message) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(BG_COLOR);
    JLabel lbl = new JLabel(message);
    lbl.setForeground(Color.GRAY);
    lbl.setFont(new Font("SansSerif", Font.ITALIC, 16));
    panel.add(lbl);
    return panel;
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
    
    // Dentro de la clase InstagramProject:

private JPanel crearPanelNotificacionesContent() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(BG_COLOR);
    
    JLabel title = new JLabel("🔔 Notificaciones (Menciones)", SwingConstants.CENTER);
    title.setFont(new Font("SansSerif", Font.BOLD, 24));
    title.setForeground(TEXT_COLOR);
    title.setBorder(new EmptyBorder(15, 0, 15, 0));
    panel.add(title, BorderLayout.NORTH);
    
    // Panel central que contendrá la lista de notificaciones (scrollable)
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    listPanel.setBackground(BG_COLOR);
    listPanel.setAlignmentX(Component.CENTER_ALIGNMENT); 
    listPanel.setBorder(new EmptyBorder(0, 50, 0, 50));
    
    JScrollPane scrollPane = new JScrollPane(listPanel);
    scrollPane.setBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    panel.add(scrollPane, BorderLayout.CENTER);
    
    return panel;
}

// Dentro de la clase InstagramProject:

/** Carga y muestra los posts donde el usuario logueado fue mencionado. */
public void loadNotifications() { 
    
    // 🚨 Usamos la variable de instancia.
    if (notificationsListPanel == null || loggedUser == null) return;
    
    notificationsListPanel.removeAll();
    notificationsListPanel.revalidate();
    notificationsListPanel.repaint();
    
    String myUsername = loggedUser.getUsername();
    
    // 1. Llama a la lógica del backend
    List<Post> mentions = userManager.findMentions(myUsername);
    
    if (mentions.isEmpty()) {
        JLabel emptyMsg = new JLabel("No tienes menciones recientes (@" + myUsername + ").", SwingConstants.CENTER);
        emptyMsg.setForeground(Color.GRAY);
        emptyMsg.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emptyMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 🚨 Usamos la variable de instancia:
        notificationsListPanel.add(Box.createVerticalStrut(50));
        notificationsListPanel.add(emptyMsg);
    } else {
        // 2. Muestra cada post de mención
        for (Post post : mentions) {
            JPanel mentionItem = createMentionItem(post);
            mentionItem.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // 🚨 Usamos la variable de instancia:
            notificationsListPanel.add(mentionItem);
            notificationsListPanel.add(Box.createVerticalStrut(10));
        }
    }
    
    // 🚨 Usamos la variable de instancia:
    notificationsListPanel.revalidate();
    notificationsListPanel.repaint();
}

/** Crea el componente visual para una notificación de mención. */
private JPanel createMentionItem(Post post) {
    JPanel item = new JPanel(new BorderLayout(10, 5));
    item.setBackground(POST_BG);
    item.setBorder(new LineBorder(BORDER_COLOR, 1, true));
    item.setMaximumSize(new Dimension(500, 80)); 
    
    String author = post.getAuthorUsername();
    
    // Texto de la notificación
    JLabel lblText = new JLabel("<html><b>@" + author + "</b> te mencionó en una publicación: <i>" 
                               + post.getCaption().substring(0, Math.min(post.getCaption().length(), 50)) 
                               + (post.getCaption().length() > 50 ? "..." : "") + "</i></html>");
    lblText.setForeground(TEXT_COLOR);
    lblText.setBorder(new EmptyBorder(10, 10, 10, 10));
    item.add(lblText, BorderLayout.CENTER);
    
    // Imagen miniatura del post (ícono)
    JLabel lblImage = new JLabel(cargarImagenCuadrada(post.getImagePath(), 60));
    lblImage.setBorder(new EmptyBorder(5, 5, 5, 10));
    item.add(lblImage, BorderLayout.EAST);
    
    return item;
}


}
