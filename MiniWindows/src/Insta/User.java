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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jerem
 */
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private char genero;
    private String username;
    private String password;
    private int edad;
    private String fotoPath;
    private LocalDate joinDate;
    private List<Post> posts;
    private List<String> followers;
    private List<String> followings;

    public User(String nombre, char genero, String username, String password, int edad, String fotoPath) {
        this.nombre = nombre;
        this.genero = genero;
        this.username = username;
        this.password = password; // En un proyecto real, hashea la contraseña
        this.edad = edad;
        this.fotoPath = fotoPath;
        this.joinDate = LocalDate.now();
        this.posts = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public char getGenero() { return genero; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getEdad() { return edad; }
    public String getFotoPath() { return fotoPath; }
    public LocalDate getJoinDate() { return joinDate; }
    public List<Post> getPosts() { return posts; }
    public List<String> getFollowers() { return followers; }
    public List<String> getFollowings() { return followings; }

    // Métodos de acción
    public void addPost(Post post) { this.posts.add(0, post); } // Añadir al inicio de la lista
    public boolean isFollowing(String targetUsername) { return followings.contains(targetUsername); }
    public void follow(String targetUsername) {
        if (!isFollowing(targetUsername)) {
            followings.add(targetUsername);
        }
    }
    public void unfollow(String targetUsername) { followings.remove(targetUsername); }
    public void addFollower(String followerUsername) {
        if (!followers.contains(followerUsername)) {
            followers.add(followerUsername);
        }
    }
    public void removeFollower(String followerUsername) { followers.remove(followerUsername); }
}