/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
/**
 *
 * @author Nathan
 */

public class ImageViewer extends JInternalFrame {

    private JLabel imageLabel;
    private JButton prevButton, nextButton, addButton;
    private List<File> images;
    private int currentIndex = 0;
    private File imagesFolder;

    public ImageViewer(List<File> images) {
        super("Visor de Imágenes", true, true, true, true);
        this.images = images;

        // Carpeta donde se almacenan las imágenes del usuario
        if (!images.isEmpty()) {
            imagesFolder = images.get(0).getParentFile();
        } else {
            imagesFolder = new File(Desktop.Z_ROOT_PATH + "Imagenes");
            if (!imagesFolder.exists()) imagesFolder.mkdirs();
        }

        setSize(600, 500);
        setLayout(new BorderLayout());

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setBackground(Color.BLACK);
        imageLabel.setOpaque(true);
        add(imageLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("Anterior");
        nextButton = new JButton("Siguiente");
        addButton = new JButton("Añadir Imagen");

        buttonsPanel.add(prevButton);
        buttonsPanel.add(nextButton);
        buttonsPanel.add(addButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        prevButton.addActionListener(e -> showPrevious());
        nextButton.addActionListener(e -> showNext());
        addButton.addActionListener(e -> addImage());

        updateImage();
    }

    private void showPrevious() {
        if (images.isEmpty()) return; // Evitar errores
        currentIndex = (currentIndex - 1 + images.size()) % images.size();
        updateImage();
    }

    private void showNext() {
        if (images.isEmpty()) return; // Evitar errores
        currentIndex = (currentIndex + 1) % images.size();
        updateImage();
    }

    private void updateImage() {
        if (images.isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("No hay imágenes");
        } else {
            File imgFile = images.get(currentIndex);
            ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
            // Escalar imagen al tamaño del label
            Image img = icon.getImage().getScaledInstance(
                    imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setText(null);
        }
    }

    private void addImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                        || name.endsWith(".gif") || name.endsWith(".bmp");
            }

            public String getDescription() {
                return "Archivos de imagen (*.png, *.jpg, *.jpeg, *.gif, *.bmp)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();
            for (File f : selectedFiles) {
                try {
                    File dest = new File(imagesFolder, f.getName());
                    Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    images.add(dest); // Añadir a la lista actual
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al añadir imagen: " + ex.getMessage());
                }
            }
            if (!images.isEmpty()) {
                currentIndex = images.size() - selectedFiles.length; // Mostrar primera nueva añadida
                updateImage();
            }
        }
    }
}

