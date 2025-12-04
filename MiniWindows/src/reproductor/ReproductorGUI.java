/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reproductor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javazoom.jl.player.advanced.*;
import miniwindows.User;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

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

    public ReproductorGUI(User user) {
        super("Reproductor Musical - " + user.getUsername(), true, true, true, true);
        this.currentUser = user;

        playlist = new ArrayList<>();
        playlistModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistModel);

        loadPlaylistFromUserFolder(); 
        setupUI();
        setSize(600, 450);
        setVisible(true);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        setCoverImage("/Imagenes/Play");
        nowPlayingLabel = new JLabel("Now Playing: Ninguno", SwingConstants.CENTER);
        nowPlayingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(coverLabel, BorderLayout.CENTER);
        topPanel.add(nowPlayingLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(playlistList);
        scroll.setPreferredSize(new Dimension(200, 0));
        add(scroll, BorderLayout.EAST);

        JPanel controlPanel = new JPanel();
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
        add(controlPanel, BorderLayout.SOUTH);

        JPanel progressPanel = new JPanel(new BorderLayout());
        elapsedLabel = new JLabel("00:00");
        durationLabel = new JLabel("00:00");
        progressSlider = new JSlider(0, 100, 0);
        progressPanel.add(elapsedLabel, BorderLayout.WEST);
        progressPanel.add(progressSlider, BorderLayout.CENTER);
        progressPanel.add(durationLabel, BorderLayout.EAST);
        add(progressPanel, BorderLayout.CENTER);

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

        progressSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentFile != null && totalMillis > 0) {
                    int value = progressSlider.getValue();
                    playedMillis = (long) (value / 100.0 * totalMillis);
                    pausedFrame = (int) (playedMillis / 26.0); 
                    boolean wasPlaying = isPlaying;
                    stopMusic();
                    loadFileAtCurrentIndex();
                    if (wasPlaying) playMusic(); 
                    else updateProgress(); 
                }
            }
        });

        sliderTimer = new javax.swing.Timer(500, e -> updateProgress());
    }

    private void setCoverImage(String imageName) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(imageName + ".jpg"));
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            coverLabel.setIcon(new ImageIcon(img));
        } catch (Exception ignored) {}
    }

    private void loadPlaylistFromUserFolder() {
        try {
            File musicDir = new File("Z_ROOT" + File.separator + currentUser.getUsername() + File.separator + "Musica");
            if (!musicDir.exists()) musicDir.mkdirs();
            File[] files = musicDir.listFiles((d,n)-> n.toLowerCase().endsWith(".mp3"));
            if (files != null) {
                for(File f : files){
                    playlist.add(f);
                    playlistModel.addElement(f);
                }
            }
        } catch (Exception ignored) {}
    }

    public void loadFromFile(File f){
        try {
            File musicDir = new File("Z_ROOT"+File.separator+currentUser.getUsername()+File.separator+"Musica");
            if(!musicDir.exists()) musicDir.mkdirs();
            File copy = new File(musicDir, f.getName());
            if(!copy.exists()) java.nio.file.Files.copy(f.toPath(), copy.toPath());
            f = copy;

            if(!playlist.contains(f)){
                playlist.add(f);
                playlistModel.addElement(f);
            }
            currentIndex = playlist.indexOf(f);
            loadFileAtCurrentIndex();
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Error cargando archivo: "+ex.getMessage());
        }
    }

    private void selectAndLoadFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("MP3 Files (*.mp3)","mp3"));
        int result = fc.showOpenDialog(this);
        if(result==JFileChooser.APPROVE_OPTION) loadFromFile(fc.getSelectedFile());
    }

    private void loadFileAtCurrentIndex() {
        if(currentIndex<0 || currentIndex>=playlist.size()) {
            currentFile = null;
            nowPlayingLabel.setText("Now Playing: Ninguno");
            setCoverImage("/Imagenes/Play");
            return;
        }
        currentFile = playlist.get(currentIndex);
        nowPlayingLabel.setText("Now Playing: "+currentFile.getName());
        setCoverImage("/Imagenes/Play");
        int totalFrames = getTotalFrames(currentFile);
        totalMillis = (long) totalFrames * 26L; // aproximación ms
        playedMillis = (long) pausedFrame * 26L;
        updateProgress();
    }

    private void togglePlayPause(){
        if(isPlaying) {
            pauseMusic();
        } else {
            playMusic();
        }
        playPauseButton.setText(isPlaying ? "⏸" : "▶");
    }

    //Reproduccion
    private void playMusic(){
        if(currentFile==null) return;
        if(playerThread!=null && playerThread.isAlive()) return;

        FileInputStream fis;
        try{
            fis = new FileInputStream(currentFile);
            player = new AdvancedPlayer(fis);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Error reproduciendo archivo: "+ex.getMessage());
            return;
        }

        isPlaying = true;
        playPauseButton.setText("⏸");
        sliderTimer.start();

        playerThread = new Thread(()->{
            try{
                player.setPlayBackListener(new PlaybackListener(){
                    @Override
                    public void playbackFinished(PlaybackEvent evt){
                        pausedFrame += evt.getFrame();
                        playedMillis = (long) pausedFrame * 26L;
                    }
                });

                player.play(pausedFrame, Integer.MAX_VALUE);

            }catch(Exception ignored){
            } finally {
                SwingUtilities.invokeLater(() -> {
                    if (isPlaying) { 
                        playNext();
                    } else {
                        playPauseButton.setText("▶");
                        sliderTimer.stop();
                    }
                });
            }
        });
        playerThread.start();
    }

    private void pauseMusic(){
        if(player!=null){
            player.close();
        }
        pausedFrame = (int)(playedMillis / 26L);
        isPlaying=false;
        sliderTimer.stop();
        playPauseButton.setText("▶");
    }

    private void stopMusic(){
        if(player!=null) player.close();
        playerThread=null;
        pausedFrame=0;
        playedMillis = 0;
        isPlaying=false;
        sliderTimer.stop();
        progressSlider.setValue(0);
        playPauseButton.setText("▶");
        updateProgress();
    }

    private void playNext(){
        if(playlist.isEmpty()) return; 
        stopMusic();
        currentIndex = (currentIndex+1)%playlist.size();
        loadFileAtCurrentIndex();
        playMusic();
    }

    private void playPrevious(){
        if(playlist.isEmpty()) return;
        stopMusic();
        currentIndex = (currentIndex-1+playlist.size())%playlist.size();
        loadFileAtCurrentIndex();
        playMusic();
    }

    private void updateProgress(){
        if(currentFile!=null && totalMillis>0){
            if(isPlaying){
                playedMillis += 500;
                if(playedMillis > totalMillis) playedMillis = totalMillis;
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

    private int getTotalFrames(File f){
        return 10000; 
    }

    private String formatTime(long millis){
        long totalSecs = millis/1000;
        long minutes = totalSecs/60;
        long seconds = totalSecs%60;
        return String.format("%02d:%02d",minutes,seconds);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
