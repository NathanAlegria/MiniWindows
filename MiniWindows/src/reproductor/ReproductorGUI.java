/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reproductor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import javazoom.jl.player.Player;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Nathan
 */

public class ReproductorGUI extends JFrame {

    // --- CAMPOS DE LA CLASE ---
    private Player player;
    private Thread playerThread;
    private File currentFile;
    private long pausedFrame = 0; 
    private boolean isLoaded = false;
    
    // --- COMPONENTES DE LA INTERFAZ ---
    private JButton playButton, stopButton, pauseButton, addButton;
    private JLabel nowPlayingLabel;
    private JLabel imageLabel;

    public ReproductorGUI() {
        super("Music Player");
        setupUI();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Imagenes/Play.jpg"));
            Image image = icon.getImage();
            Image scaledImage = image.getScaledInstance(380, 350, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(imageLabel, BorderLayout.NORTH);
        } catch (Exception e) {
            imageLabel = new JLabel("Error: No se encontró la imagen en /Imagenes/Play.jpg", SwingConstants.CENTER);
            System.err.println("Error al cargar la imagen principal: " + e.getMessage());
            add(imageLabel, BorderLayout.NORTH);
        }

        nowPlayingLabel = new JLabel("Now Playing: Ninguno", SwingConstants.CENTER);
        nowPlayingLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(nowPlayingLabel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        
        int buttonWidth = 80;
        int buttonHeight = 50;

        // Botón Play
        try {
            ImageIcon playIcon = new ImageIcon(getClass().getResource("/Imagenes/Bplay.jpg"));
            Image playImage = playIcon.getImage();
            Image scaledPlayImage = playImage.getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH);
            playButton = new JButton(new ImageIcon(scaledPlayImage));
            setupImageButton(playButton, buttonWidth, buttonHeight);
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen del botón Play: " + e.getMessage());
            playButton = new JButton("▶ Play");
        }

        // Botón Pause
        try {
            ImageIcon pauseIcon = new ImageIcon(getClass().getResource("/Imagenes/Bpause.jpg"));
            Image pauseImage = pauseIcon.getImage();
            Image scaledPauseImage = pauseImage.getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH);
            pauseButton = new JButton(new ImageIcon(scaledPauseImage));
            setupImageButton(pauseButton, buttonWidth, buttonHeight);
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen del botón Pause: " + e.getMessage());
            pauseButton = new JButton("⏸ Pause");
        }

        // Botón Stop
        try {
            ImageIcon stopIcon = new ImageIcon(getClass().getResource("/Imagenes/Bstop.jpg"));
            Image stopImage = stopIcon.getImage();
            Image scaledStopImage = stopImage.getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH);
            stopButton = new JButton(new ImageIcon(scaledStopImage));
            setupImageButton(stopButton, buttonWidth, buttonHeight);
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen del botón Stop: " + e.getMessage());
            stopButton = new JButton("■ Stop");
        }

        addButton = new JButton("📂 Add...");

        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(addButton);
        
        addButton.addActionListener(e -> selectAndLoadFile());
        playButton.addActionListener(e -> playMusic());
        pauseButton.addActionListener(e -> pauseMusic());
        stopButton.addActionListener(e -> stopMusic());
        
        updateButtonState(false);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void setupImageButton(JButton button, int width, int height) {
        button.setPreferredSize(new Dimension(width + 10, height + 5));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
    }

    private void selectAndLoadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 Files (*.mp3)", "mp3"));
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            pausedFrame = 0;
            loadAudioFile();
        }
    }
    
    private void loadAudioFile() {
        if (player != null) { player.close(); }
        if (currentFile == null) return;
        
        long streamPosition = pausedFrame; 
        
        try {
            FileInputStream fis = new FileInputStream(currentFile);
            
            if (streamPosition > 0) {
                long skippedBytes = fis.skip(streamPosition);
                if (skippedBytes != streamPosition) {
                    System.err.println("Advertencia: No se saltaron los bytes esperados. Reanudación imprecisa.");
                }
            }
            
            BufferedInputStream bis = new BufferedInputStream(fis);
            player = new Player(bis); 
            
            nowPlayingLabel.setText("Now Playing: " + currentFile.getName());
            isLoaded = true;
            updateButtonState(true);
        } catch (Exception ex) {
            nowPlayingLabel.setText("Now Playing: Error de carga");
            JOptionPane.showMessageDialog(this,
                "Error al cargar el archivo con JLayer.\n" + ex.getMessage(),
                "Error de Audio", JOptionPane.ERROR_MESSAGE);
            isLoaded = false;
            currentFile = null;
            updateButtonState(false);
        }
    }

    private void playMusic() {
        if (!isLoaded || currentFile == null) return;
        
        if (playerThread != null && playerThread.isAlive()) {
            return;
        }

        loadAudioFile();

        if (player != null) {
            playerThread = new Thread(() -> {
                try {
                    player.play();    
                    if (pausedFrame > 0) {
                        pausedFrame = 0; 
                    }
                } catch (Exception e) {
                } finally {
                    if (player != null && player.isComplete()) {
                        SwingUtilities.invokeLater(() -> stopMusic());
                    }
                }
            });
            playerThread.start();
            
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
        }
    }

    private void pauseMusic() {
        if (player != null && playerThread != null && playerThread.isAlive()) {
            pausedFrame = player.getPosition();
            player.close();
            
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    private void stopMusic() {
        if (player != null) {
            player.close();
            playerThread = null;
            pausedFrame = 0;
            isLoaded = true;
            updateButtonState(true); 
        }
    }
    
    private void updateButtonState(boolean loaded) {
        if (loaded) {
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
        } else {
            playButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            nowPlayingLabel.setText("Now Playing: Ninguno");
        }
    }
}