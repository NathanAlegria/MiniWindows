/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reproductor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.sound.sampled.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Nathan
 */
public class ReproductorGUI extends JFrame {

    private Clip clip;
    private File currentFile;
    private long pausedFrame = 0;

    private boolean isLoaded = false;
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

        try {
            ImageIcon playIcon = new ImageIcon(getClass().getResource("/Imagenes/Bplay.jpg"));
            Image scaled = playIcon.getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH);
            playButton = new JButton(new ImageIcon(scaled));
            setupImageButton(playButton, buttonWidth, buttonHeight);
        } catch (Exception e) {
            playButton = new JButton("▶ Play");
        }

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Imagenes/Bpause.jpg"));
            Image scaled = icon.getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH);
            pauseButton = new JButton(new ImageIcon(scaled));
            setupImageButton(pauseButton, buttonWidth, buttonHeight);
        } catch (Exception e) {
            pauseButton = new JButton("⏸ Pause");
        }

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Imagenes/Bstop.jpg"));
            Image scaled = icon.getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH);
            stopButton = new JButton(new ImageIcon(scaled));
            setupImageButton(stopButton, buttonWidth, buttonHeight);
        } catch (Exception e) {
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
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Audio Files (wav, aiff, au)", "wav", "aiff", "au"));

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            pausedFrame = 0;
            loadAudioFile();
        }
    }

    private void loadAudioFile() {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }

            if (currentFile == null) return;

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(currentFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            nowPlayingLabel.setText("Now Playing: " + currentFile.getName());
            isLoaded = true;
            updateButtonState(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar audio: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            isLoaded = false;
            currentFile = null;
            updateButtonState(false);
        }
    }

    private void playMusic() {
        if (!isLoaded || clip == null) return;

        clip.setMicrosecondPosition(pausedFrame);
        clip.start();

        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
    }

    private void pauseMusic() {
        if (clip != null && clip.isRunning()) {
            pausedFrame = clip.getMicrosecondPosition();
            clip.stop();

            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    private void stopMusic() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
            pausedFrame = 0;

            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
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