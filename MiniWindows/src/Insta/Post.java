/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Insta;

/**
 *
 * @author jerem
 */
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private String imagePath;
    private String caption;
    
    // NOTA: Este 'username' representa al AUTOR del post.
    private String username; 
    
    private LocalDateTime date;
    private Set<String> likedBy; // Almacena los usernames de quienes dieron like
    private List<Comment> comments; // Almacena la lista de comentarios

    /**
     * Constructor del Post.
     * @param username El username del autor del post.
     * @param imagePath La ruta de la imagen.
     * @param caption El texto de la descripción.
     */
    public Post(String username, String imagePath, String caption) {
        this.username = username;
        this.imagePath = imagePath;
        this.caption = caption;
        this.date = LocalDateTime.now();
        this.likedBy = new HashSet<>();
        this.comments = new ArrayList<>();
    }

    // --- Getters del autor y contenido (Compatibles con tu código original) ---
    
    // Renombramos el getter para mayor claridad, aunque el campo se llame 'username'
    public String getAuthorUsername() {
        return username;
    }
    // Dejamos el getter original por si es necesario para compatibilidad directa
    public String getUsername() {
        return username; 
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getCaption() {
        return caption;
    }

    // --- Nuevos Getters y Lógica (Likes y Comentarios) ---

    public LocalDateTime getDate() {
        return date;
    }
    
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }

    public List<Comment> getComments() {
        return comments;
    }
    
    // --- Lógica de Likes ---

    public int getLikesCount() {
        return likedBy.size();
    }

    public boolean isLikedBy(String user) {
        return likedBy.contains(user);
    }

    public void like(String user) {
        likedBy.add(user);
    }

    public void unlike(String user) {
        likedBy.remove(user);
    }
    
    // --- Lógica de Comentarios ---
    
    public void addComment(Comment comment) {
        comments.add(comment);
    }
}