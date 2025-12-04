/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Insta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Date;
/**
 *
 * @author jerem
 */
public class Post implements Serializable {
    private String imagePath;
    private String caption;
    private String username;
    private int likes;

    public Post(String imagePath, String caption, String username) {
        this.imagePath = imagePath;
        this.caption = caption;
        this.username = username;
        this.likes = 0; // Inicialmente 0 likes
    }

    public String getImagePath() { return imagePath; }
    public String getCaption() { return caption; }
}
