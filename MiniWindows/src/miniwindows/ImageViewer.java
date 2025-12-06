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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan
 */
public class ImageViewer extends JInternalFrame {

    private JLabel imageLabel;
    private JButton prevButton, nextButton, addButton;
    private JPanel thumbnailPanel;
    private JScrollPane thumbnailScroll;

    private List<File> images = new ArrayList<>();
    private int currentIndex = 0;
    private File imagesFolder;

    public ImageViewer(List<File> images) {
        super("Visor de Imágenes", true, true, true, true);

        this.images = images;

        if (!images.isEmpty()) {
            imagesFolder = images.get(0).getParentFile();
        } else {
            // CORRECCIÓN: Apunta a "Mis Imágenes"
            imagesFolder = new File(Desktop.Z_ROOT_PATH + File.separator + "Mis Imágenes"); 
            if (!imagesFolder.exists()) imagesFolder.mkdirs();
        }

        setSize(900, 700);
        setLayout(new BorderLayout());

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setBackground(Color.BLACK);
        imageLabel.setOpaque(true);
        add(imageLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("⏮️ Anterior");
        nextButton = new JButton("⏭️ Siguiente");
        addButton = new JButton("➕ Añadir Imagen");

        buttonsPanel.add(prevButton);
        buttonsPanel.add(addButton);
        buttonsPanel.add(nextButton);
        add(buttonsPanel, BorderLayout.NORTH);

        thumbnailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        thumbnailScroll = new JScrollPane(
                thumbnailPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        thumbnailScroll.setPreferredSize(new Dimension(100, 150));
        add(thumbnailScroll, BorderLayout.SOUTH);

        prevButton.addActionListener(e -> showPrevious());
        nextButton.addActionListener(e -> showNext());
        addButton.addActionListener(e -> addImage());

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateImage();
            }
        });

        loadThumbnails();

        updateImage();
    }

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

    private void updateImage() {
        if (images.isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("No hay imágenes");
            return;
        }

        File imgFile = images.get(currentIndex);
        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());

        int labelW = imageLabel.getWidth();
        int labelH = imageLabel.getHeight();

        if (labelW <= 0 || labelH <= 0) return;

        Image scaled = getScaledProportional(icon.getImage(), labelW, labelH);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.setText("");

        highlightSelectedThumbnail();
    }

    private Image getScaledProportional(Image img, int maxW, int maxH) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        double ratio = Math.min((double) maxW / w, (double) maxH / h);

        return img.getScaledInstance(
                (int) (w * ratio),
                (int) (h * ratio),
                Image.SCALE_SMOOTH
        );
    }

    private void loadThumbnails() {
        thumbnailPanel.removeAll();

        for (int i = 0; i < images.size(); i++) {
            int index = i;

            ImageIcon icon = new ImageIcon(images.get(i).getAbsolutePath());
            Image thumb = getScaledProportional(icon.getImage(), 120, 80);

            JLabel thumbLabel = new JLabel(new ImageIcon(thumb));
            thumbLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            thumbLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            thumbLabel.addMouseListener(new MouseAdapter() {
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
            JLabel label = (JLabel) thumbnailPanel.getComponent(i);
            if (i == currentIndex) {
                label.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 4));
            } else {
                label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            }
        }
    }

    private void addImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                String n = f.getName().toLowerCase();
                return f.isDirectory() || n.endsWith(".png") || n.endsWith(".jpg") ||
                        n.endsWith(".jpeg") || n.endsWith(".gif") || n.endsWith(".bmp");
            }

            public String getDescription() {
                return "Imágenes (*.png, *.jpg, *.jpeg, *.gif, *.bmp)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File[] selected = chooser.getSelectedFiles();
        for (File f : selected) {
            try {
                File dest = new File(imagesFolder, f.getName());
                Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                images.add(dest);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        loadThumbnails();
        currentIndex = images.size() - 1;
        updateImage();
    }
}