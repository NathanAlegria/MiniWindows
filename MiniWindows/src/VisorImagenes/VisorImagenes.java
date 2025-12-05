/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package VisorImagenes;

import miniwindows.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

    private JLabel imageLabel;
    private JButton prevButton, nextButton, addButton;

    private JPanel thumbnailPanel;
    private JScrollPane thumbnailScroll;

    private final List<File> images = new ArrayList<>();
    private int currentIndex = -1;

    public VisorImagenes(User user) {
        this.currentUser = user;
        this.imagesDir = new File("Z_ROOT" + File.separator + currentUser.getUsername()
                + File.separator + "Imagenes");

        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        setLayout(new BorderLayout());

        // Imagen grande
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(800, 500));
        imageLabel.setBackground(Color.BLACK);
        imageLabel.setOpaque(true);
        add(imageLabel, BorderLayout.CENTER);

        // Panel de botones superiores
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        prevButton = new JButton("⏮️ Anterior");
        nextButton = new JButton("⏭️ Siguiente");
        addButton = new JButton("➕ Añadir Imagen");

        topPanel.add(prevButton);
        topPanel.add(addButton);
        topPanel.add(nextButton);
        add(topPanel, BorderLayout.NORTH);

        // Panel miniaturas tipo "islas"
        thumbnailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        thumbnailScroll = new JScrollPane(
                thumbnailPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        thumbnailScroll.setPreferredSize(new Dimension(100, 150));
        add(thumbnailScroll, BorderLayout.SOUTH);

        // Eventos
        prevButton.addActionListener(e -> showPrevious());
        nextButton.addActionListener(e -> showNext());
        addButton.addActionListener(e -> addImage());

        // Cuando la ventana cambia tamaño, actualiza la imagen
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateImage();
            }
        });

        loadImages();
        loadThumbnails();

        if (!images.isEmpty()) {
            currentIndex = 0;
            updateImage();
        } else {
            imageLabel.setText("No hay imágenes");
        }
    }

    // ---------------------------------------------------------------------
    // CARGA DE IMÁGENES
    // ---------------------------------------------------------------------
    private void loadImages() {
        images.clear();
        File[] files = imagesDir.listFiles((dir, name) -> name.matches(".*\\.(png|jpg|jpeg|gif|bmp)$"));
        if (files != null) {
            for (File f : files) images.add(f);
        }
    }

    // ---------------------------------------------------------------------
    // MINIATURAS
    // ---------------------------------------------------------------------
    private void loadThumbnails() {
        thumbnailPanel.removeAll();

        for (int i = 0; i < images.size(); i++) {
            int index = i;
            ImageIcon icon = new ImageIcon(images.get(i).getAbsolutePath());

            Image thumb = scaleProportionally(icon.getImage(), 120, 80);

            JLabel thumbLabel = new JLabel(new ImageIcon(thumb));
            thumbLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            thumbLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            thumbLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    currentIndex = index;
                    updateImage();
                }
            });

            thumbnailPanel.add(thumbLabel);
        }

        thumbnailPanel.revalidate();
        thumbnailPanel.repaint();
        highlightSelectedThumbnail();
    }

    private void highlightSelectedThumbnail() {
        for (int i = 0; i < thumbnailPanel.getComponentCount(); i++) {
            JLabel lbl = (JLabel) thumbnailPanel.getComponent(i);
            if (i == currentIndex) {
                lbl.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            } else {
                lbl.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            }
        }
    }

    // ---------------------------------------------------------------------
    // MOSTRAR IMAGEN PRINCIPAL
    // ---------------------------------------------------------------------
    private void updateImage() {
        if (images.isEmpty() || currentIndex < 0 || currentIndex >= images.size()) {
            imageLabel.setIcon(null);
            imageLabel.setText("No hay imágenes");
            return;
        }

        File imgFile = images.get(currentIndex);
        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());

        int w = imageLabel.getWidth();
        int h = imageLabel.getHeight();

        if (w <= 0 || h <= 0) return;

        Image scaled = scaleProportionally(icon.getImage(), w, h);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setText("");

        highlightSelectedThumbnail();
    }

    private Image scaleProportionally(Image img, int maxW, int maxH) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) return img;

        double ratio = Math.min((double) maxW / w, (double) maxH / h);

        return img.getScaledInstance(
                (int) (w * ratio),
                (int) (h * ratio),
                Image.SCALE_SMOOTH
        );
    }

    // ---------------------------------------------------------------------
    // NAVEGACIÓN
    // ---------------------------------------------------------------------
    private void showPrevious() {
        if (images.isEmpty()) return;
        currentIndex = (currentIndex - 1 + images.size()) % images.size();
        updateImage();
    }

    private void showNext() {
        if (images.isEmpty()) return;
        currentIndex = (currentIndex + 1) % images.size();
        updateImage();
    }

    // ---------------------------------------------------------------------
    // AÑADIR IMÁGENES
    // ---------------------------------------------------------------------
    private void addImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);

        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)$");
            }

            @Override
            public String getDescription() {
                return "Imágenes (*.png, *.jpg, *.jpeg, *.gif, *.bmp)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File[] selected = chooser.getSelectedFiles();
        for (File f : selected) {
            try {
                File dest = new File(imagesDir, f.getName());
                Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                images.add(dest);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al copiar imagen: " + ex.getMessage());
            }
        }

        loadThumbnails();
        currentIndex = images.size() - 1;
        updateImage();
    }
}

