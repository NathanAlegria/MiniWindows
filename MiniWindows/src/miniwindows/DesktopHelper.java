/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.util.List;
/**
 *
 * @author Nathan
 */

public class DesktopHelper {
    public static JPanel createDesktopIcon(String emoji, String name, java.awt.event.ActionListener doubleClickListener) {
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(100, 80));

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(Color.WHITE);

        iconPanel.add(iconLabel, BorderLayout.CENTER);
        iconPanel.add(nameLabel, BorderLayout.SOUTH);

        if (doubleClickListener != null) {
            iconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        doubleClickListener.actionPerformed(new java.awt.event.ActionEvent(iconPanel, java.awt.event.ActionEvent.ACTION_PERFORMED, null));
                    }
                }
            });
        }

        return iconPanel;
    }

    public static JPanel createFileIcon(String name) {
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(100, 80));

        String emoji = "📄";
        if (name.toLowerCase().endsWith(".mp3")) emoji = "🎵";
        else if (name.toLowerCase().endsWith(".txt")) emoji = "📝";
        else if (name.toLowerCase().matches(".*\\.(jpg|png|gif|jpeg)$")) emoji = "🖼️";
        else if (name.toLowerCase().endsWith(".pdf")) emoji = "📑";

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        iconPanel.add(iconLabel, BorderLayout.CENTER);
        iconPanel.add(nameLabel, BorderLayout.SOUTH);

        return iconPanel;
    }

    public static JTree createSimulatedFileTree(User user) {
        DefaultMutableTreeNode top;

        if (user.isAdmin()) {
            top = new DefaultMutableTreeNode("Z:\\ (Admin)");
            
            top.add(createDirectoryNode(user.getUsername())); 
            
            List<User> users = UserManager.getUsers();
            for (User u : users) {
                if (!u.getUsername().equals(user.getUsername())) {
                    top.add(createDirectoryNode(u.getUsername()));
                }
            }
        } else {
            String userPath = user.getUsername();
            top = new DefaultMutableTreeNode("Z:\\" + userPath);
            File userRoot = new File(Desktop.Z_ROOT_PATH + userPath);
            if (!userRoot.exists()) userRoot.mkdirs();

            File[] files = userRoot.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        top.add(new DefaultMutableTreeNode(file.getName()));
                    }
                }
            }
        }
        return new JTree(top);
    }

    private static DefaultMutableTreeNode createDirectoryNode(String username) {
        DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(username);
        File userDir = new File(Desktop.Z_ROOT_PATH + username);

        if (!userDir.exists()) userDir.mkdirs();

        File[] files = userDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    userNode.add(new DefaultMutableTreeNode(file.getName()));
                }
            }
        }
        return userNode;
    }
    
    public static void cleanUserDirectory(String username) {
        File userDir = new File(Desktop.Z_ROOT_PATH + username);
        if (!userDir.exists()) {
            return;
        }

        String officialDocs = "Mis Documentos";
        String officialMusic = "Música"; 
        String officialImages = "Mis Imágenes";
        
        File[] files = userDir.listFiles();
        if (files == null) return;
        
        for (File f : files) {
            if (f.isDirectory()) {
                String name = f.getName();
                
                // Eliminar duplicados no oficiales. Mantenemos "Mis Documentos", "Música" y "Mis Imágenes"
                if (name.equalsIgnoreCase("Documentos") && !name.equals(officialDocs)) {
                    deleteDirectory(f);
                } else if (name.equalsIgnoreCase("Imágenes") && !name.equals(officialImages)) {
                    deleteDirectory(f);
                } else if (name.equalsIgnoreCase("Mis Documentos") && !name.equals(officialDocs)) {
                     deleteDirectory(f);
                } else if (name.equalsIgnoreCase("Musica") && !name.equals(officialMusic)) {
                    deleteDirectory(f);
                }
            }
        }
    }
    
    // Método auxiliar de borrado para la limpieza
    private static void deleteDirectory(File dir) {
        if (dir == null) return;
        File[] children = dir.listFiles();
        if (children != null) {
            for (File f : children) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}