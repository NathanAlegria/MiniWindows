/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package VisorImagenes;

import miniwindows.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan
 */

public class VisorImagenes extends JPanel {

    private final User currentUser;
    private final File imagesDir;
    private final JLabel imageLabel;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton addButton;

    private final List<File> images = new ArrayList<>();
    private int currentIndex = -1;

    public VisorImagenes(User user) {
        this.currentUser = user;
        this.imagesDir = new File("Z_ROOT" + File.separator + currentUser.getUsername() + File.separator + "Imagenes");

        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        setLayout(new BorderLayout());

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(800, 500));
        add(imageLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("⏮️ Anterior");
        nextButton = new JButton("⏭️ Siguiente");
        addButton = new JButton("➕ Añadir Imagen");

        buttonsPanel.add(prevButton);
        buttonsPanel.add(addButton);
        buttonsPanel.add(nextButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        prevButton.addActionListener(this::showPreviousImage);
        nextButton.addActionListener(this::showNextImage);
        addButton.addActionListener(this::addImage);

        loadImages();
        if (!images.isEmpty()) {
            currentIndex = 0;
            updateImage();
        } else {
            imageLabel.setText("No hay imágenes");
        }
    }

    private void loadImages() {
        images.clear();
        File[] files = imagesDir.listFiles((dir, name) -> name.matches(".*\\.(png|jpg|jpeg|gif|bmp)$"));
        if (files != null) {
            for (File f : files) {
                images.add(f);
            }
        }
    }

    private void updateImage() {
        if (images.isEmpty() || currentIndex < 0 || currentIndex >= images.size()) {
            imageLabel.setIcon(null);
            imageLabel.setText("No hay imágenes");
            return;
        }

        ImageIcon icon = new ImageIcon(images.get(currentIndex).getAbsolutePath());
        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            imageLabel.setIcon(null);
            imageLabel.setText("No se puede mostrar la imagen");
            return;
        }
        Image scaled = icon.getImage().getScaledInstance(
                imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setText(null);
    }

    private void showPreviousImage(ActionEvent e) {
        if (images.isEmpty()) return;
        currentIndex--;
        if (currentIndex < 0) currentIndex = images.size() - 1;
        updateImage();
    }

    private void showNextImage(ActionEvent e) {
        if (images.isEmpty()) return;
        currentIndex++;
        if (currentIndex >= images.size()) currentIndex = 0;
        updateImage();
    }

    private void addImage(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().matches(".*\\.(png|jpg|jpeg|gif|bmp)$");
            }

            @Override
            public String getDescription() {
                return "Imágenes (*.png, *.jpg, *.jpeg, *.gif, *.bmp)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();
            for (File f : selectedFiles) {
                File dest = new File(imagesDir, f.getName());
                try {
                    Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al copiar la imagen: " + ex.getMessage());
                }
            }
            loadImages();
            if (!images.isEmpty()) {
                currentIndex = images.size() - 1;
                updateImage();
            }
        }
    }
}

