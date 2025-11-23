/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

/**
 *
 * @author Nathan
 */

public class Password {
    
    public static boolean isValid(String password) {
        if (password == null || password.length() != 5) {
            return false;
        }

        boolean hasUppercase = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
                break;
            }
        }
        if (!hasUppercase) {
            return false;
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            return false;
        }
        
        return true;
    }
    
    public static String getErrorReason(String password) {
        if (password == null || password.length() != 5) {
            return "La contraseña debe contener exactamente **5 caracteres**.";
        }
        
        boolean hasUppercase = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
                break;
            }
        }
        if (!hasUppercase) {
            return "La contraseña debe contener al menos **una letra mayúscula**.";
        }
        
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            return "La contraseña debe contener al menos **un signo especial** (puntos, comas, @, !, #, etc.).";
        }
        
        return "Válida";
    }
}
