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
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author jerem
 */
public class UserManager {
    private List<User> users;
    private static final String FILE_NAME = "users.dat";

    public UserManager() {
        users = loadUsers();
        // Cargar datos de prueba si la lista está vacía
        if (users.isEmpty()) {
            setupTestData();
        }
    }

    private void setupTestData() {
        User user1 = new User("Ana García", 'F', "anita123", "1234", 25, "default_user.png");
        User user2 = new User("Carlos Ruiz", 'M', "carlitos_r", "pass", 30, "default_user.png");
        
        // Agregar posts de prueba a user1
        user1.addPost(new Post("anita123", "default_post_1.png", "¡Primer post en la app! 🎉"));
        user1.addPost(new Post("anita123", "default_post_2.png", "Un atardecer increíble."));

        // Asegurar que user2 sigue a user1
        user2.follow(user1.getUsername());
        user1.addFollower(user2.getUsername());

        users.add(user1);
        users.add(user2);
        saveUsers(); // Guardar los datos de prueba
    }

    @SuppressWarnings("unchecked")
    private List<User> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<User>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de usuarios no encontrado. Creando nueva lista.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }
    
    public void saveUser(User user) {
        Optional<User> existingUser = users.stream()
            .filter(u -> u.getUsername().equals(user.getUsername()))
            .findFirst();
        
        if (existingUser.isPresent()) {
            // Reemplaza el usuario viejo por la instancia actualizada
            int index = users.indexOf(existingUser.get());
            users.set(index, user);
        } else {
            // En caso de que sea un usuario nuevo (no debería pasar si se usa saveUser correctamente)
            users.add(user);
        }
        saveUsers();
    }

    public User login(String usernameOrEmail, String password) throws InvalidCredentialsException {
        User user = users.stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(usernameOrEmail) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null);

        if (user == null) {
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos.");
        }
        return user;
    }

    public void registrarUsuario(User newUser) throws Exception {
        if (users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(newUser.getUsername()))) {
            throw new Exception("El nombre de usuario ya existe.");
        }
        users.add(newUser);
        saveUsers();
    }
    
    public User getUserByUsername(String username) {
        return users.stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(username))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Alterna el estado de seguimiento entre dos usuarios.
     * @param followerUsername El usuario que sigue/deja de seguir (el logueado).
     * @param targetUsername El usuario objetivo.
     */
    public void toggleFollow(String followerUsername, String targetUsername) {
        User follower = getUserByUsername(followerUsername);
        User target = getUserByUsername(targetUsername);

        if (follower == null || target == null) return;

        boolean isFollowing = follower.isFollowing(targetUsername);

        if (isFollowing) {
            // Unfollow
            follower.unfollow(targetUsername);
            target.removeFollower(followerUsername);
        } else {
            // Follow
            follower.follow(targetUsername);
            target.addFollower(followerUsername);
        }
        
        // Guardar ambos usuarios con sus listas actualizadas
        saveUser(follower);
        saveUser(target);
    }
}