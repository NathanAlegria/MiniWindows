/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

/**
 *
 * @author Nathan
 */
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class ImageUtils {
    public static ImageIcon getScaledIcon(String path, int width, int height) {
        Image image = null;
        try {
            // Intento 1: Cargar como recurso ABSOLUTO (ruta que inicia en el classpath)
            URL imageUrl = ImageUtils.class.getClassLoader().getResource(path);
            
            if (imageUrl != null) {
                image = new ImageIcon(imageUrl).getImage();
            } else {
                // Intento 2: Cargar como archivo local
                image = new ImageIcon(path).getImage();
                if (image.getWidth(null) == -1) {
                    image = null;
                }
            }

            if (image != null) {
                Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            // Manejo silencioso.
        }
        return null;
    }
}
