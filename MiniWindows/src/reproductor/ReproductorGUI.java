/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reproductor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javazoom.jl.player.advanced.*;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import miniwindows.User;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Nathan
 */
public class ReproductorGUI extends JInternalFrame {

    private User currentUser;
    private AdvancedPlayer player;
    private Thread playerThread;
    private File currentFile;
    private int pausedFrame = 0;
    private boolean isPlaying = false;

    private JButton playPauseButton, stopButton, nextButton, prevButton, addButton;
    private JLabel nowPlayingLabel, elapsedLabel, durationLabel;
    private JLabel coverLabel;

    private JSlider progressSlider;
    private javax.swing.Timer sliderTimer;

    private DefaultListModel<File> playlistModel;
    private JList<File> playlistList;
    private List<File> playlist;
    private int currentIndex = 0;

    private long playedMillis = 0;
    private long totalMillis = 0;
    private int totalFrames = 0;

    public ReproductorGUI(User user) {
        super("Reproductor Musical - " + user.getUsername(), true, true, true, true);
        this.currentUser = user;

        playlist = new ArrayList<>();
        playlistModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistModel);
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        loadPlaylistFromUserFolder(); // carga canciones
        setupUI(); // crea interfaz

        setSize(700, 450);
        setVisible(true);

        // ----------------- Actualizar carátula y lista al iniciar -----------------
        SwingUtilities.invokeLater(() -> {
            setCoverImagePlay();
            playlistList.setModel(playlistModel);
            if (!playlistModel.isEmpty()) {
                playlistList.setSelectedIndex(0);
            }
            playlistList.repaint();
        });
    }

    private void setupUI() {
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Panel izquierdo
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        leftPanel.setBackground(new Color(240, 240, 240));
        leftPanel.setMinimumSize(new Dimension(300, 400));
        leftPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        JPanel imagePanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 300);
            }
        };
        imagePanel.setBackground(new Color(220, 220, 220));
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setPreferredSize(new Dimension(300, 300));
        imagePanel.add(coverLabel, BorderLayout.CENTER);

        nowPlayingLabel = new JLabel("Now Playing: Ninguno", SwingConstants.CENTER);
        nowPlayingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nowPlayingLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        imagePanel.add(nowPlayingLabel, BorderLayout.SOUTH);

        leftPanel.add(imagePanel);

        // Barra de progreso
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(new Color(240, 240, 240));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        elapsedLabel = new JLabel("00:00");
        durationLabel = new JLabel("00:00");
        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setValue(0);
        progressPanel.add(elapsedLabel, BorderLayout.WEST);
        progressPanel.add(progressSlider, BorderLayout.CENTER);
        progressPanel.add(durationLabel, BorderLayout.EAST);

        leftPanel.add(progressPanel);

        // Botones de control
        JPanel controlWrapper = new JPanel(new BorderLayout());
        controlWrapper.setBackground(new Color(200, 200, 200));
        controlWrapper.setMinimumSize(new Dimension(100, 60));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controlPanel.setBackground(new Color(200, 200, 200));

        prevButton = new JButton("⏮");
        playPauseButton = new JButton("▶");
        stopButton = new JButton("■");
        nextButton = new JButton("⏭");
        addButton = new JButton("📂");

        controlPanel.add(prevButton);
        controlPanel.add(playPauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(nextButton);
        controlPanel.add(addButton);

        controlWrapper.add(controlPanel, BorderLayout.CENTER);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(controlWrapper);

        // Panel derecho
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        rightPanel.setBackground(new Color(230, 230, 250));
        rightPanel.setMinimumSize(new Dimension(200, 300));

        playlistList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                if (value instanceof File) {
                    value = ((File) value).getName();
                }
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                return label;
            }
        });

        JScrollPane scroll = new JScrollPane(playlistList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        rightPanel.add(scroll, BorderLayout.CENTER);

        // JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.3);
        splitPane.setDividerSize(5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(splitPane);
        add(centerPanel, BorderLayout.CENTER);

        // Listeners
        playPauseButton.addActionListener(e -> togglePlayPause());
        stopButton.addActionListener(e -> stopMusic());
        nextButton.addActionListener(e -> playNext());
        prevButton.addActionListener(e -> playPrevious());
        addButton.addActionListener(e -> selectAndLoadFile());

        playlistList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = playlistList.getSelectedIndex();
                    if (idx >= 0 && idx < playlist.size()) {
                        currentIndex = idx;
                        loadFileAtCurrentIndex();
                        playMusic();
                    }
                }
            }
        });

        progressSlider.addChangeListener(e -> {
            if (progressSlider.getValueIsAdjusting() && currentFile != null && totalFrames > 0) {
                int value = progressSlider.getValue();
                pausedFrame = (int) (totalFrames * (value / 100.0));
                playedMillis = (long) (totalMillis * (value / 100.0));
            }
        });

        progressSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentFile != null && totalFrames > 0) {
                    boolean wasPlaying = isPlaying;

                    // Detener la reproducción actual sin reiniciar loadFileAtCurrentIndex
                    if (player != null) {
                        player.close();
                    }
                    sliderTimer.stop();
                    isPlaying = false;

                    int value = progressSlider.getValue();
                    pausedFrame = (int) (totalFrames * (value / 100.0));
                    playedMillis = (long) (totalMillis * (value / 100.0));

                    if (wasPlaying) {
                        playMusic();
                    } else {
                        updateProgress();
                    }
                }
            }
        });

        sliderTimer = new javax.swing.Timer(500, e -> updateProgress());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int windowWidth = getWidth();
                int windowHeight = getHeight() - 25;

                int newHeight = windowHeight - 50;
                leftPanel.setPreferredSize(new Dimension(leftPanel.getPreferredSize().width, newHeight));
                rightPanel.setPreferredSize(new Dimension(rightPanel.getPreferredSize().width, newHeight));

                if (windowWidth > 800) {
                    splitPane.setPreferredSize(new Dimension(windowWidth - 500, newHeight));
                } else {
                    splitPane.setPreferredSize(null);
                }

                splitPane.revalidate();
                setCoverImagePlay();
            }
        });
    }

    private void setCoverImagePlay() {
        SwingUtilities.invokeLater(() -> {
            try {
                java.net.URL imgURL = getClass().getResource("/Imagenes/Play.jpg");
                if (imgURL != null) {
                    ImageIcon icon = new ImageIcon(imgURL);
                    int width = coverLabel.getWidth() > 0 ? coverLabel.getWidth() : 300;
                    int height = coverLabel.getHeight() > 0 ? coverLabel.getHeight() : 300;
                    Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    coverLabel.setIcon(new ImageIcon(scaled));
                } else {
                    System.err.println("Imagen Play.jpg no encontrada en /Imagenes/");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadPlaylistFromUserFolder() {
        try {
            File musicDir = new File("Z_ROOT" + File.separator + currentUser.getUsername() + File.separator + "Musica");
            if (!musicDir.exists()) {
                musicDir.mkdirs();
            }
            File[] files = musicDir.listFiles((d, n) -> n.toLowerCase().endsWith(".mp3"));
            if (files != null) {
                for (File f : files) {
                    playlist.add(f);
                    playlistModel.addElement(f);
                }
            }
            playlistList.setModel(playlistModel);
            if (!playlistModel.isEmpty()) {
                playlistList.setSelectedIndex(0);
            }
            playlistList.repaint();
        } catch (Exception ignored) {
        }
    }

    public void loadFromFile(File f) {
        try {
            File musicDir = new File("Z_ROOT" + File.separator + currentUser.getUsername() + File.separator + "Musica");
            if (!musicDir.exists()) {
                musicDir.mkdirs();
            }
            File copy = new File(musicDir, f.getName());
            if (!copy.exists()) {
                java.nio.file.Files.copy(f.toPath(), copy.toPath());
            }
            f = copy;
            if (!playlist.contains(f)) {
                playlist.add(f);
                playlistModel.addElement(f);
            }
            currentIndex = playlist.indexOf(f);
            loadFileAtCurrentIndex();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando archivo: " + ex.getMessage());
        }
    }

    private void selectAndLoadFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("MP3 Files (*.mp3)", "mp3"));
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            loadFromFile(fc.getSelectedFile());
        }
    }

    private void loadFileAtCurrentIndex() {
        if (currentIndex < 0 || currentIndex >= playlist.size()) {
            currentFile = null;
            nowPlayingLabel.setText("Now Playing: Ninguno");
            progressSlider.setValue(0);
            elapsedLabel.setText("00:00");
            durationLabel.setText("00:00");
            totalFrames = 0;
            totalMillis = 0;
            pausedFrame = 0; // reiniciar barra
            playedMillis = 0; // reiniciar barra
            return;
        }

        currentFile = playlist.get(currentIndex);
        nowPlayingLabel.setText("Now Playing: " + currentFile.getName());
        setCoverImagePlay();

        // Calcular duración y frames
        try (FileInputStream fis = new FileInputStream(currentFile)) {
            Bitstream bitstream = new Bitstream(fis);
            totalFrames = 0;
            long totalMillisTemp = 0;
            Header header;
            while ((header = bitstream.readFrame()) != null) {
                totalMillisTemp += header.ms_per_frame();
                totalFrames++;
                bitstream.closeFrame();
            }
            bitstream.close();
            totalMillis = totalMillisTemp;
        } catch (Exception e) {
            totalMillis = 0;
            totalFrames = 0;
            e.printStackTrace();
        }

        // Reiniciar barra al cambiar de canción
        pausedFrame = 0;
        playedMillis = 0;
        updateProgress();
    }

    private void togglePlayPause() {
        if (isPlaying) {
            pauseMusic();
        } else {
            playMusic();
        }
        playPauseButton.setText(isPlaying ? "⏸" : "▶");
    }

    private void playMusic() {
        if (currentFile == null) {
            return;
        }
        if (playerThread != null && playerThread.isAlive()) {
            return;
        }

        try {
            player = new AdvancedPlayer(new FileInputStream(currentFile));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reproduciendo archivo: " + ex.getMessage());
            return;
        }

        isPlaying = true;
        playPauseButton.setText("⏸");
        sliderTimer.start();

        playerThread = new Thread(() -> {
            try {
                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        pausedFrame += evt.getFrame();
                        playedMillis = (long) pausedFrame * 26L;

                        // --- CORRECCIÓN 3: Solo avanzar si isPlaying sigue siendo true ---
                        SwingUtilities.invokeLater(() -> {
                            if (isPlaying) {
                                playNext();
                            }
                        });
                    }
                });

                player.play(pausedFrame, Integer.MAX_VALUE);
            } catch (Exception ignored) {
            } finally {
                SwingUtilities.invokeLater(() -> {
                    if (!isPlaying) {
                        playPauseButton.setText("▶");
                        sliderTimer.stop();
                    }
                });
            }
        });

        playerThread.start();
    }

    private void pauseMusic() {
        if (player != null) {
            player.close();
        }
        pausedFrame = (int) (playedMillis / 26L);
        isPlaying = false;
        sliderTimer.stop();
        playPauseButton.setText("▶");
    }

    private void stopMusic() {
        if (player != null) {
            player.close();
        }
        if (playerThread != null && playerThread.isAlive()) {
            try {
                playerThread.join();
            } catch (InterruptedException ignored) {
            }
        }
        playerThread = null;
        pausedFrame = 0;
        playedMillis = 0;
        isPlaying = false;
        sliderTimer.stop();
        progressSlider.setValue(0);
        playPauseButton.setText("▶");
        updateProgress();
    }

    private void playNext() {
        if (playlist.isEmpty()) {
            return;
        }

        // Detener canción actual
        stopMusic();

        // Cambiar de canción
        currentIndex = (currentIndex + 1) % playlist.size();
        loadFileAtCurrentIndex();

        // Reproducir la nueva canción
        playMusic();
    }

    private void playPrevious() {
        if (playlist.isEmpty()) {
            return;
        }

        stopMusic();
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        loadFileAtCurrentIndex();
        playMusic();
    }

    private void updateProgress() {
        if (currentFile != null && totalMillis > 0) {
            if (isPlaying) {
                playedMillis += 500;
                if (playedMillis > totalMillis) {
                    playedMillis = totalMillis;
                }
                pausedFrame = (int) (totalFrames * ((double) playedMillis / totalMillis));
            }
            int percent = (int) ((playedMillis * 100.0) / totalMillis);
            progressSlider.setValue(Math.min(Math.max(percent, 0), 100));
            elapsedLabel.setText(formatTime(playedMillis));
            durationLabel.setText(formatTime(totalMillis));
        } else {
            progressSlider.setValue(0);
            elapsedLabel.setText("00:00");
            durationLabel.setText("00:00");
        }
    }

    private String formatTime(long millis) {
        long totalSecs = millis / 1000;
        long minutes = totalSecs / 60;
        long seconds = totalSecs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
