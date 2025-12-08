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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
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
    
    // Agrega esto dentro de tu clase UserManager
    public List<User> buscarUsuarios(String busqueda) {
        List<User> resultados = new ArrayList<>();
        // Convertimos a minúsculas para que 'Patito' y 'patito' sean iguales
        String termino = busqueda.toLowerCase(); 

        // CORRECCIÓN AQUÍ: Iteramos directamente sobre la lista 'users'
        for (User u : users) {
            if (u.getUsername().toLowerCase().contains(termino)) {
                resultados.add(u);
            }
        }
        return resultados;
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
    
    // --- MODIFICACIÓN EN UserManager.java ---

// ... (código existente) ...

public User getUserByUsername(String username) {
    // 1. FORZAR LA RECARGA DE TODA LA LISTA DE USUARIOS DESDE EL ARCHIVO
    // Esto asegura que la lista 'this.users' tenga la última versión del disco.
    this.users = loadUsers(); 

    // 2. Buscar el usuario en la lista recién cargada
    return this.users.stream()
        .filter(u -> u.getUsername().equalsIgnoreCase(username))
        .findFirst()
        .orElse(null);
}

// ... (código existente) ...
    /**
     * Alterna el estado de seguimiento entre dos usuarios.
     * @param followerUsername El usuario que sigue/deja de seguir (el logueado).
     * @param targetUsername El usuario objetivo.
     */
    
    // Dentro de la clase UserManager
// --- MODIFICACIÓN EN UserManager.java -> getAllRelevantPostsByDate ---

public List<Post> getAllRelevantPostsByDate(User loggedUser) {
    if (loggedUser == null) {
        return Collections.emptyList();
    }
    
    // ** PASO CLAVE 1: Obtener la versión más reciente del usuario logueado **
    // Usamos getUserByUsername, que ya modificamos para recargar la lista global, 
    // asegurando que tenemos la lista de seguidos actualizada.
    User currentUser = getUserByUsername(loggedUser.getUsername()); 
    if (currentUser == null) return Collections.emptyList();

    List<Post> allPosts = new ArrayList<>();

    // 1. Agregar los posts propios
    allPosts.addAll(currentUser.getPosts()); // Usar currentUser

    // 2. Agregar los posts de los usuarios seguidos
    for (String followedUsername : currentUser.getFollowings()) { // Usar currentUser
        try {
            // ** PASO CLAVE 2: Obtener el usuario seguido desde la lista cargada **
            // getUserByUsername recarga la lista global, lo cual es útil.
            User followedUser = getUserByUsername(followedUsername); 
            
            if (followedUser != null) {
                allPosts.addAll(followedUser.getPosts());
            }
        } catch (Exception e) {
            System.err.println("Error al cargar posts de " + followedUsername + ": " + e.getMessage());
        }
    }

    // 3. Ordenar la lista combinada (Del más reciente al más antiguo)
    // Asumiendo que Post tiene el método getDate()
    allPosts.sort(Comparator.comparing(Post::getDate, Comparator.reverseOrder())); 

    return allPosts;
}
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
    
    public List<Post> searchPostsByHashtag(String hashtag) {
    // Usamos un LinkedHashSet para garantizar la unicidad de los posts
    // y mantener el orden de inserción (aunque el requisito dice que el orden no importa).
    Set<Post> uniquePosts = new LinkedHashSet<>();
    
    // Convertimos la búsqueda a minúsculas y aseguramos que tenga el '#'
    String normalizedHashtag = hashtag.toLowerCase();
    if (!normalizedHashtag.startsWith("#")) {
        normalizedHashtag = "#" + normalizedHashtag;
    }

    // 1. Recorrer todos los usuarios (debemos recargar la lista si no se hizo recientemente)
    // Aunque loadUsers se llama en getUserByUsername, es más seguro y explícito aquí.
    this.users = loadUsers(); 

    // 2. Iterar sobre todos los posts de todos los usuarios
    for (User user : users) {
        for (Post post : user.getPosts()) {
            
            String caption = post.getCaption();
            
            // Verificación: Solo si el post tiene descripción
            if (caption != null && !caption.trim().isEmpty()) {
                
                // Normalizar la descripción para la búsqueda sin importar mayúsculas
                String normalizedCaption = caption.toLowerCase();
                
                // CRÍTICO: Comprobar si la descripción contiene el hashtag.
                // Usamos contains() para manejar hashtags en cualquier parte del texto.
                if (normalizedCaption.contains(normalizedHashtag)) {
                    uniquePosts.add(post);
                }
            }
        }
    }

    // 3. Devolver la lista de posts únicos
    return new ArrayList<>(uniquePosts);
}
}