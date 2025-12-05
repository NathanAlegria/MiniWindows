/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package EditordeTexto;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import javax.swing.text.rtf.RTFEditorKit;
import miniwindows.ImageUtils;
import miniwindows.User;
import miniwindows.Desktop;

/**
 *
 * @author Nathan
 */
public class EditorTexto extends JInternalFrame {

    private JTextPane textPane;
    private StyledDocument doc;
    private JComboBox<String> fontCombo;
    private JComboBox<Integer> sizeCombo;
    private JButton colorBtn;

    private File openedFile = null;
    private final User user;

    public EditorTexto(User currentUser) {
        super("Editor de Texto", true, true, true, true);
        this.user = currentUser;

        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setFrameIcon(ImageUtils.getScaledIcon("iconos/text.png", 16, 16));

        // Área de texto
        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        add(scrollPane, BorderLayout.CENTER);

        // Barra de herramientas
        JToolBar formatToolbar = new JToolBar();
        formatToolbar.setFloatable(false);

        fontCombo = new JComboBox<>(new String[]{
            "Arial", "Times New Roman", "Lucida Calligraphy", "Courier New", "Verdana"
        });
        fontCombo.addActionListener(e -> updateTextStyle());

        sizeCombo = new JComboBox<>(new Integer[]{
            12, 14, 16, 18, 20, 24, 28, 32, 48
        });
        sizeCombo.addActionListener(e -> updateTextStyle());

        colorBtn = new JButton("■");
        colorBtn.setForeground(Color.BLACK);
        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Selecciona un color", colorBtn.getForeground());
            if (chosen != null) {
                colorBtn.setForeground(chosen);
                updateTextStyle();
            }
        });

        JButton btnSave = new JButton("Guardar como...");
        btnSave.addActionListener(e -> saveAsRTFtxt());

        JButton btnOpen = new JButton("Abrir");
        btnOpen.addActionListener(e -> openFileFromDialog());

        formatToolbar.add(new JLabel("Fuente: "));
        formatToolbar.add(fontCombo);
        formatToolbar.add(new JLabel(" Tamaño: "));
        formatToolbar.add(sizeCombo);
        formatToolbar.add(new JLabel(" Color: "));
        formatToolbar.add(colorBtn);
        formatToolbar.add(btnSave);
        formatToolbar.add(btnOpen);

        add(formatToolbar, BorderLayout.NORTH);

        setSize(650, 450);
        setVisible(true);
        updateTextStyle();
    }

    /**
     * Aplica la fuente, tamaño y color actuales al texto seleccionado o cursor.
     */
    private void updateTextStyle() {
        String fontName = (String) fontCombo.getSelectedItem();
        int fontSize = (Integer) sizeCombo.getSelectedItem();
        Color color = colorBtn.getForeground();

        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attr, fontName);
        StyleConstants.setFontSize(attr, fontSize);
        StyleConstants.setForeground(attr, color);

        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();

        if (start != end) {
            doc.setCharacterAttributes(start, end - start, attr, false);
        } else {
            textPane.setCharacterAttributes(attr, true);
        }
    }

    /**
     * Guarda RTF internamente, pero usando extensión .txt
     */
    private void saveAsRTFtxt() {
        try {
            // Carpeta del usuario
            File userDir = new File(Desktop.Z_ROOT_PATH + user.getUsername());
            if (!userDir.exists()) userDir.mkdirs();

            JFileChooser chooser = new JFileChooser(userDir);
            chooser.setDialogTitle("Guardar archivo");

            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Texto con Formato (*.txt)", "txt"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                File selectedFile = chooser.getSelectedFile();

                // Forzar extensión .txt
                if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                }

                // Aquí se guarda como RTF pero con extensión TXT
                RTFEditorKit rtf = new RTFEditorKit();
                FileOutputStream fos = new FileOutputStream(selectedFile);
                rtf.write(fos, textPane.getDocument(), 0, textPane.getDocument().getLength());
                fos.close();

                openedFile = selectedFile;
                setTitle("Editor de Texto - " + selectedFile.getName());

                JOptionPane.showMessageDialog(this,
                        "Archivo guardado correctamente (RTF con extensión .txt)\n" + selectedFile.getAbsolutePath());
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }

    /**
     * Abre archivo .txt o .rtf desde la carpeta del usuario.
     */
    private void openFileFromDialog() {
        File userDir = new File(Desktop.Z_ROOT_PATH + user.getUsername());
        userDir.mkdirs();

        JFileChooser chooser = new JFileChooser(userDir);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Texto / RTF", "txt", "rtf"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }

    /**
     * Carga un archivo existente.
     */
    public void openFile(File f) {
        try {
            openedFile = f;
            setTitle("Editor de Texto - " + f.getName());

            if (f.getName().toLowerCase().endsWith(".txt")) {
                // Cargar como RTF aunque sea .txt
                RTFEditorKit rtf = new RTFEditorKit();
                StyledDocument newDoc = (StyledDocument) rtf.createDefaultDocument();
                FileInputStream fis = new FileInputStream(f);
                rtf.read(fis, newDoc, 0);
                textPane.setDocument(newDoc);
                this.doc = newDoc;
                fis.close();

            } else {
                // Leer RTF normal
                RTFEditorKit rtfKit = new RTFEditorKit();
                StyledDocument newDoc = (StyledDocument) rtfKit.createDefaultDocument();
                FileInputStream fis = new FileInputStream(f);
                rtfKit.read(fis, newDoc, 0);
                textPane.setDocument(newDoc);
                this.doc = newDoc;
                fis.close();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al abrir: " + e.getMessage());
        }
    }
}


