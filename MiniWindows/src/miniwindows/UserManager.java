/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan
 */

public class UserManager {
    // *** NUEVAS CONSTANTES PARA EL SISTEMA DE ARCHIVOS ***
    private static final String Z_ROOT = "Z_ROOT" + File.separator; // Usamos un nombre de carpeta local simulando Z:\
    private static final String USER_FILE = "usuarios.sop";
    private static final String ADMIN_PASSWORD = "Adm!1"; 

    // Método de carga: Carga del archivo binario y crea el Admin si no existe
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(USER_FILE);

        if (!file.exists()) {
            // Inicialización: Crea el Admin y su estructura de archivos
            User admin = new User("Admin", ADMIN_PASSWORD, true);
            users.add(admin);
            saveUsers(users);
            createInitialDirectories(admin.getUsername()); 
            return users;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject();
        } catch (Exception e) {
            // Error de carga. Devolver lista vacía para evitar fallos.
        }
        return users;
    }

    // Método para obtener la lista de usuarios. Recarga la lista para asegurar que sea la más reciente.
    public static List<User> getUsers() {
        return loadUsers();
    }
    
    public static void saveUsers(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
        }
    }
    
    public static User validateLogin(String username, String password) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
    
    public static boolean createUser(String username, String password) {
        List<User> users = loadUsers();
        
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return false; // Nombre de usuario ya tomado
            }
        }
        
        User newUser = new User(username, password, false);
        users.add(newUser);
        saveUsers(users);
        createInitialDirectories(username); // Llamada al sistema de archivos
        
        return true;
    }
    
    // *** IMPLEMENTACIÓN REQUERIDA: Creación de directorios iniciales ***
    private static void createInitialDirectories(String username) {
        try {
            // 1. Crear el directorio Z_ROOT (si no existe)
            File zRoot = new File(Z_ROOT);
            if (!zRoot.exists()) {
                zRoot.mkdir();
            }

            // 2. Crear el directorio Z_ROOT/usuario
            File userRoot = new File(Z_ROOT + username);
            userRoot.mkdirs(); 
            
            // 3. Crear las 3 carpetas básicas dentro de Z_ROOT/usuario
            String[] defaultFolders = {"Mis Documentos", "Música", "Mis Imágenes"};
            
            if (userRoot.exists()) {
                for (String folderName : defaultFolders) {
                    File subFolder = new File(userRoot, folderName);
                    subFolder.mkdir(); 
                }
            }
        } catch (Exception e) {
            // Manejar errores de creación de directorios
        }
    }
}
