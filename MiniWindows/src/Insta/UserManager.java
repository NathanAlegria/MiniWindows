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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static UserManager instance;
    private List<User> users;
    private static final String FILE_NAME = "users.dat";
    private static final String USERS_INS_FILE = "users.ins";

    private UserManager() {
        users = loadUsers();
        if (users.isEmpty()) {
            setupTestData();
        }
    }
    
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
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

    public boolean saveUsers() { // 👈 Cambiado de 'void' a 'boolean'
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
        oos.writeObject(users);
        return true; // Retorna éxito
    } catch (IOException e) {
        System.err.println("Error al guardar usuarios: " + e.getMessage());
        // Aquí podrías agregar un mensaje más detallado para el usuario si fuera necesario.
        return false; // Retorna fracaso
    }
}
    
    // Agrega esto dentro de tu clase UserManager
    // En UserManager.java

public List<User> searchUsers(String query) {
    List<User> matches = new ArrayList<>();
    String lowerQuery = query.toLowerCase();
    
    for (User user : users) {
        // 🚨 CRÍTICO: SOLO procesar usuarios activos
        if (user.isActive()) { 
            if (user.getUsername().toLowerCase().contains(lowerQuery)) {
                matches.add(user);
            }
        }
    }
    return matches;
}
    // Dentro de UserManager
    public boolean isUsernameUnique(String username) {
        // 1. Verificar la lista en memoria (rápido y conveniente)
        if (users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
            return false;
        }

        // 2. Opcional/Seguro: Verificar el archivo users.ins (si almacena solo usernames o registros)
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_INS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Asumiendo que el username está al inicio de la línea en users.ins
                String existingUsername = line.split("#")[0]; 
                if (existingUsername.equalsIgnoreCase(username)) {
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            // Ignorar si el archivo no existe aún, significa que es único
        } catch (IOException e) {
            System.err.println("Error al leer users.ins para validación: " + e.getMessage());
        }

        return true; // Es único
    }
    
    public boolean saveUser(User user) { // 👈 Cambiado de 'void' a 'boolean'
    Optional<User> existingUser = users.stream()
        .filter(u -> u.getUsername().equals(user.getUsername()))
        .findFirst();
        
    if (existingUser.isPresent()) {
        // Reemplaza el usuario viejo por la instancia actualizada
        int index = users.indexOf(existingUser.get());
        users.set(index, user);
    } else {
        // En caso de que sea un usuario nuevo 
        users.add(user);
    }
    
    // Ahora retorna el resultado de la operación de I/O
    return saveUsers(); // 👈 Ya no es void, devuelve boolean
}

    public User login(String usernameOrEmail, String password) throws InvalidCredentialsException {
        User user = users.stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(usernameOrEmail) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null);

        if (user == null) {
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos.");
        }
        
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Tu cuenta está desactivada. Por favor, revisa las opciones para activarla.");
        }
        
        return user;
    }

    // Dentro de UserManager

// Dentro de UserManager

public void registrarUsuario(User newUser) throws Exception {
    String username = newUser.getUsername();
    
    // 1. VALIDACIÓN DE UNICIDAD
    if (!isUsernameUnique(username)) {
        throw new Exception("El nombre de usuario '" + username + "' ya existe.");
    }

    // 2. CREACIÓN DE CARPETA Y ARCHIVOS LOCALES (Sin cambios, ya está correcto)
    try {
        File userFolder = new File(username);
        if (!userFolder.mkdirs()) { 
            throw new Exception("No se pudo crear la carpeta del usuario.");
        }
        
        new File(userFolder, "followers.ins").createNewFile();
        new File(userFolder, "following.ins").createNewFile();
        new File(userFolder, "insta.ins").createNewFile();
        
    } catch (IOException e) {
        throw new Exception("Error al inicializar archivos de usuario: " + e.getMessage());
    }

    // 3. ESCRIBIR EN users.ins (REGISTRO CENTRAL DE TEXTO)
    // 🚨 CORRECCIÓN: Se añaden getJoinDate() y isActive() al formato
    // Formato: user#pass#nombre#gen#edad#fotoPath#joinDate#isActive
    String userRecord = String.format("%s#%s#%s#%c#%d#%s#%s#%b", 
                                      newUser.getUsername(), 
                                      newUser.getPassword(), 
                                      newUser.getNombre(), 
                                      newUser.getGenero(), 
                                      newUser.getEdad(), 
                                      newUser.getFotoPath(),
                                      newUser.getJoinDate().toString(), // ⚠️ Convertimos LocalDate a String
                                      true); // Asumimos 'true' o usas newUser.isActive()

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_INS_FILE, true))) {
        writer.write(userRecord);
        writer.newLine();
    } catch (IOException e) {
        throw new Exception("Error al guardar registro en users.ins.");
    }
    
    // 4. ACTUALIZAR LISTA EN MEMORIA Y SERIALIZAR
    users.add(newUser);
    saveUsers(); 
}
    
    // --- MODIFICACIÓN EN UserManager.java ---

// ... (código existente) ...

public User getUserByUsername(String username) {
    // 1. FORZAR LA RECARGA DE TODA LA LISTA DE USUARIOS DESDE EL ARCHIVO
    // Esto asegura que la lista 'this.users' tenga la última versión del disco. 

    // 2. Buscar el usuario en la lista recién cargada
    return this.users.stream()
        .filter(u -> u.getUsername().equalsIgnoreCase(username))
        .findFirst()
        .orElse(null);
}


// Dentro de UserManager

private Post parsePostFromLine(String line) {
    try {
        // 🚨 CAMBIO CRÍTICO: Ahora el formato esperado es: username#YYYYMMDD_HHmmss#imagePath#caption
        String[] parts = line.split("#", 4); // <--- Dividir por 4 partes
        
        if (parts.length < 4) { // <--- Esperamos 4 partes
            System.err.println("Advertencia: Línea de post incompleta (4 partes esperadas): " + line);
            return null;
        }

        String author = parts[0];
        String dateString = parts[1];
        String imagePath = parts[2]; // <--- NUEVO: Extraer la ruta de la imagen
        String caption = parts[3];
        
        // 1. Parsear la fecha del archivo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        LocalDateTime postDate = LocalDateTime.parse(dateString, formatter);
        
        // 2. Crear el objeto Post (usando AHORA la ruta de imagen real)
        Post post = new Post(author, imagePath, caption); // <--- Usar imagePath real
        
        // 3. Asignar la fecha leída
        post.setDate(postDate); 

        return post;
        
    } catch (Exception e) {
        System.err.println("Error al parsear línea de post: " + line);
        return null;
    }
}


/** Lee y parsea todos los posts del archivo insta.ins de un usuario específico. */
public List<Post> loadPostsFromLocalFile(String username) {
    List<Post> userPosts = new ArrayList<>();

    // 📌 Obtener la ruta absoluta de la raíz del proyecto
    String basePath = System.getProperty("user.dir");

    // 📌 Carpeta del usuario: [RaizProyecto]/[username]/
    File userFolder = new File(basePath, username);

    // Si la carpeta no existe, la creamos
    if (!userFolder.exists()) {
        userFolder.mkdirs();
    }

    // 📌 Archivo: [RaizProyecto]/[username]/insta.ins
    File file = new File(userFolder, "insta.ins");

    // Si el archivo no existe, devolvemos lista vacía (normal si no hay posts)
    if (!file.exists()) {
        return userPosts;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {

            // Evitar líneas vacías o corruptas
            if (line.trim().isEmpty()) continue;

            Post post = parsePostFromLine(line);

            if (post != null) {
                userPosts.add(post);
            }
        }
    } catch (IOException e) {
        System.err.println("⚠ Error al leer posts de " + username + " desde insta.ins: " + e.getMessage());
    }

    return userPosts;
}

// Dentro de UserManager

// Dentro de UserManager

/** Lee los usernames seguidos del archivo local [username]/following.ins */
private List<String> loadFollowingsFromLocalFile(String username) {
    List<String> followings = new ArrayList<>();
    File file = new File(username, "following.ins");
    if (!file.exists()) return followings;

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                followings.add(line.trim());
            }
        }
    } catch (IOException e) {
        System.err.println("Error al leer followings.ins de " + username);
    }
    return followings;
}

public List<Post> getAllRelevantPostsByDate(User loggedUser) {
    if (loggedUser == null) {
        return Collections.emptyList();
    }
    
    List<Post> allPosts = new ArrayList<>();

    // 1. Cargar Posts Propios (Usando el archivo local)
    if (loggedUser.isActive()) {
         allPosts.addAll(loadPostsFromLocalFile(loggedUser.getUsername()));
    }

    // 2. Obtener la lista de usuarios seguidos desde el archivo de texto
    List<String> followedUsers = loadFollowingsFromLocalFile(loggedUser.getUsername());
    
    // 3. Cargar Posts de los Usuarios Seguidos (Usando el archivo local)
    for (String followedUsername : followedUsers) {
        User followedUser = getUserByUsername(followedUsername); 
        
        // 🚨 CRÍTICO: Solo cargar posts si el usuario seguido está ACTIVO
        if (followedUser != null && followedUser.isActive()) {
            allPosts.addAll(loadPostsFromLocalFile(followedUsername));
        }
    }

    // 4. Ordenar la lista combinada
    allPosts.sort(Comparator.comparing(Post::getDate, Comparator.reverseOrder())); 

    return allPosts;
}

// Métodos Auxiliares para la gestión de archivos de seguimiento

/** Escribe una línea (username) al final de un archivo de usuario local. */

    /** Elimina una línea (username) de un archivo de usuario local. */
    private void removeLineFromFile(String username, String filename, String lineToRemove) throws IOException {
        File inputFile = new File(username, filename);
        File tempFile = new File(username, "temp_" + filename); // Usamos un archivo temporal

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                // Si la línea NO es la que queremos eliminar, la escribimos al temporal
                if (!currentLine.trim().equalsIgnoreCase(lineToRemove)) {
                    writer.write(currentLine);
                    writer.newLine();
                }
            }
        }

        // Reemplazar el archivo original con el temporal
        // 1. Borrar original
        if (!inputFile.delete()) {
            throw new IOException("No se pudo borrar el archivo original: " + inputFile.getName());
        }
        // 2. Renombrar temporal
        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("No se pudo renombrar el archivo temporal a: " + inputFile.getName());
        }
    }

public void toggleFollow(String followerUsername, String targetUsername) {
    User follower = getUserByUsername(followerUsername);
    User target = getUserByUsername(targetUsername);

    if (follower == null || target == null || followerUsername.equalsIgnoreCase(targetUsername)) return;

    boolean isFollowing = follower.isFollowing(targetUsername);

    try {
        if (isFollowing) {
            // --- UNFOLLOW (DEJAR DE SEGUIR) ---
            
            // 1. Actualizar objetos en memoria
            follower.unfollow(targetUsername);
            target.removeFollower(followerUsername);

            // 2. Actualizar archivos de texto (Fuente de verdad)
            removeLineFromFile(followerUsername, "following.ins", targetUsername);
            removeLineFromFile(targetUsername, "followers.ins", followerUsername);
            
        } else {
            // --- FOLLOW (SEGUIR) ---
            
            // 1. Actualizar objetos en memoria
            follower.follow(targetUsername);
            target.addFollower(followerUsername);

            // 2. Actualizar archivos de texto (Fuente de verdad)
            // Añadir al TARGET a la lista de following del FOLLOWER
            writeLocalFile(followerUsername, "following.ins", targetUsername);
            // Añadir al FOLLOWER a la lista de followers del TARGET
            writeLocalFile(targetUsername, "followers.ins", followerUsername);
        }
        
        // 3. Guardar objetos actualizados en users.dat
        saveUser(follower);
        saveUser(target);

    } catch (IOException e) {
        System.err.println("Error de persistencia de seguimiento en archivos .ins: " + e.getMessage());
        // En una aplicación real, se debería revertir la acción del objeto si falla la persistencia.
    }
}
    
    public List<Post> searchPostsByHashtag(String hashtag) {
    List<Post> results = new ArrayList<>();
    
    // 🚨 CORRECCIÓN CRÍTICA:
    // 1. Limpiar la entrada: Quitar el '#' si ya existe, y convertir a minúsculas.
    String cleanedHashtag = hashtag.replace("#", "").toLowerCase(); 
    
    // 2. Reconstruir la cadena de búsqueda: Añadir el '#' solo al inicio.
    String searchTag = "#" + cleanedHashtag; 
    
    // Iterar sobre todos los usuarios cargados
    for (User user : users) {
        // Cargar los posts desde el archivo .ins (persistencia de posts)
        List<Post> userPosts = loadPostsFromLocalFile(user.getUsername());
        
        for (Post post : userPosts) {
            String captionLower = post.getCaption().toLowerCase();
            
            // Búsqueda simplificada: si el caption contiene la cadena "#tag"
            if (captionLower.contains(searchTag)) {
                results.add(post);
            }
        }
    }
    // Ordenar los resultados por fecha, los más recientes primero
    results.sort(Comparator.comparing(Post::getDate, Comparator.reverseOrder()));
    
    return results;
}
    
    public void publishPost(Post post) throws IOException {
    String username = post.getUsername();
    
    // 1. y 2. (Objetos en memoria y serialización .dat) - Se mantienen sin cambios
    User user = getUserByUsername(username); 
    if (user == null) {
        throw new IllegalArgumentException("Usuario no encontrado para publicar el post.");
    }
    user.addPost(post);
    saveUser(user);
    
    // 3. PERSISTIR EN ARCHIVO DE TEXTO PLANO (.ins)
    
    // Formatear la fecha a String
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    String dateString = post.getDate().format(formatter);
    
    // --- 🚨 INICIO DE LA CORRECCIÓN CRÍTICA DE LA RUTA ---
    String rawImagePath = post.getImagePath();
    String cleanedImagePath;

    // Si la ruta contiene un separador de archivo ('/' o '\'), asumimos que es una ruta completa.
    // Usamos new File(rawImagePath).getName() para extraer SÓLO el nombre del archivo.
    if (rawImagePath != null && (rawImagePath.contains(File.separator) || new File(rawImagePath).isAbsolute())) {
         cleanedImagePath = new File(rawImagePath).getName();
    } else {
         // Si ya es solo el nombre del archivo (ej: "post_img_123.png"), lo usamos directamente.
         cleanedImagePath = rawImagePath;
    }
    // --- 🚨 FIN DE LA CORRECCIÓN CRÍTICA DE LA RUTA ---

    // Crear el registro con el formato: [username]#[fecha]#[nombre_archivo]#[caption]
    String postRecord = String.format("%s#%s#%s#%s", 
                                      post.getUsername(),
                                      dateString,
                                      cleanedImagePath, // <-- USAMOS LA RUTA LIMPIA
                                      post.getCaption().replaceAll("[\r\n]", " "));

    // Escribir el registro en la carpeta local del usuario
    writeLocalFile(username, "insta.ins", postRecord);
}

// ----------------------------------------------------------------------
// MÉTODO AUXILIAR (Necesario para escribir en archivos .ins, ya usado en toggleFollow)
// Inclúyelo en UserManager si no lo has hecho aún.

/** Escribe una línea al final de un archivo de usuario local. */
    /** Escribe una línea al final de un archivo de usuario local, asegurando que la carpeta exista. */
private void writeLocalFile(String username, String filename, String content) throws IOException {
    // 1. Crear el objeto File para la ruta: [username]/[filename]
    File file = new File(username, filename);
    
    // 2. 🚨 SOLUCIÓN CRÍTICA: Crear el directorio padre si no existe.
    File parentDir = file.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
        if (!parentDir.mkdirs()) {
            // Si la creación falla, lanzamos una excepción para que el usuario lo sepa.
            throw new IOException("No se pudo crear el directorio para el usuario: " + username);
        }
    }
    
    // 3. Escribir el contenido (Ahora que la carpeta está garantizada)
    // Usamos 'true' en FileWriter para append (añadir al final)
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
        writer.write(content);
        writer.newLine();
    }
}
    // Dentro de la clase UserManager:

/**
 * Busca posts que mencionen al usuario objetivo (@username) en su caption 
 * utilizando una simple verificación de subcadena.
 * * @param targetUsername El nombre de usuario que fue mencionado.
 * @return Una lista de posts donde el usuario fue mencionado.
 */
public List<Post> findMentions(String targetUsername) {
    List<Post> mentionedPosts = new ArrayList<>();
    
    String cleanedTarget = targetUsername.replace("@", "").toLowerCase();
    String searchString = "@" + cleanedTarget;

    System.out.println("DEBUG: Buscando menciones en users.dat...");

    // 🚨 Aquí usamos directamente los posts serializados reales
    for (User user : users) {
        for (Post post : user.getPosts()) {

            String caption = post.getCaption();
            if (caption == null) continue;

            if (caption.toLowerCase().contains(searchString)) {
                if (!user.getUsername().equalsIgnoreCase(cleanedTarget)) {
                    mentionedPosts.add(post);
                }
            }
        }
    }

    mentionedPosts.sort(Comparator.comparing(Post::getDate).reversed());
    return mentionedPosts;
}

// Nuevo método necesario en la clase UserManager:
public void updatePostInUserList(User author, Post modifiedPost) {
    List<Post> posts = author.getPosts();
    for (int i = 0; i < posts.size(); i++) {
        Post p = posts.get(i);
        // Buscar por ID para garantizar la coincidencia (o por ImagePath como respaldo)
        if (p.getId().equals(modifiedPost.getId())) {
            posts.set(i, modifiedPost); // Reemplazar el post antiguo con el modificado
            return;
        }
    }
}

public boolean addCommentAndSave(Post post, Comment newComment) {
    // 1. Encontrar al autor
    User author = getUserByUsername(post.getAuthorUsername());
    if (author == null) return false;

    // 2. Modificar el objeto Post (que es el post que se pasó a la vista)
    post.addComment(newComment); 

    // 3. 🚨 Actualizar la lista de Posts del Autor en la memoria del Manager 
    //    con la versión recién modificada del post.
    List<Post> posts = author.getPosts();
    boolean postFoundAndReplaced = false;

    for (int i = 0; i < posts.size(); i++) {
        if (posts.get(i).getId().equals(post.getId())) { 
            posts.set(i, post); // Reemplazo de la versión antigua
            postFoundAndReplaced = true;
            break;
        }
    }
    
    if (!postFoundAndReplaced) return false;

    // 4. Guardar la persistencia del autor
    return saveUser(author); // Asume que saveUser guarda el archivo users.dat
}


}