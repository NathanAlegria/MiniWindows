/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Insta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
/**
 *
 * @author Nathan
 */

public class FileManager {

    private static final String ROOT_FOLDER = System.getProperty("user.dir");

    public static String saveImage(File sourceFile, String newFileName) throws IOException {
        File destFile = new File(ROOT_FOLDER, newFileName);
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return destFile.getName();
    }

    public static String getImagePath(String fileName) {
        return ROOT_FOLDER + File.separator + fileName;
    }

    public static boolean exists(String fileName) {
        File file = new File(ROOT_FOLDER, fileName);
        return file.exists();
    }
}
