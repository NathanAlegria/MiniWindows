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
import java.util.*;



public class Post implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String imageName; // solo el nombre del archivo
    private String caption;
    private String username;
    private LocalDateTime date;
    private Set<String> likedBy;
    private List<Comment> comments;

    public Post(String username, String imageName, String caption) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.imageName = imageName; // nombre del archivo guardado en la raíz
        this.caption = caption;
        this.date = LocalDateTime.now();
        this.likedBy = new HashSet<>();
        this.comments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getAuthorUsername() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public String getImageName() {
        return imageName;
    }

    public String getCaption() {
        return caption;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public List<Comment> getComments() {
        return comments;
    }

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

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public String getImagePath() {
        return FileManager.getImagePath(imageName);
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
}
