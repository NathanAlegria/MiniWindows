/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EditordeTexto;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit; // ¡IMPORTACIÓN NECESARIA!
import java.awt.*;
import java.io.*;
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
    private User user;

    public EditorTexto(User currentUser) {
        super("Editor de Texto - Sin Título", true, true, true, true);
        this.user = currentUser;
        
        // CORRECCIÓN: Asegura que solo se cierre la ventana interna
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        
        setLayout(new BorderLayout());
        setFrameIcon(ImageUtils.getScaledIcon("iconos/text.png", 16, 16));

        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        JToolBar formatToolbar = new JToolBar();
        formatToolbar.setFloatable(false);

        fontCombo = new JComboBox<>(new String[]{"Arial", "Times New Roman", "Lucida Calligraphy", "Courier New", "Verdana"});
        fontCombo.addActionListener(e -> updateTextStyle());
        formatToolbar.add(new JLabel("Fuente: "));
        formatToolbar.add(fontCombo);

        sizeCombo = new JComboBox<>(new Integer[]{12,14,16,18,20,24,28,32,48});
        sizeCombo.addActionListener(e -> updateTextStyle());
        formatToolbar.add(new JLabel(" Tamaño: "));
        formatToolbar.add(sizeCombo);

        colorBtn = new JButton("■");
        colorBtn.setForeground(Color.BLACK);
        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Selecciona un color", colorBtn.getForeground());
            if (chosen != null) {
                colorBtn.setForeground(chosen);
                updateTextStyle();
            }
        });
        formatToolbar.add(new JLabel(" Color: "));
        formatToolbar.add(colorBtn);

        JButton btnSave = new JButton("Guardar");
        JButton btnOpen = new JButton("Abrir");
        btnSave.addActionListener(e -> saveCurrentFile());
        btnOpen.addActionListener(e -> openFileFromDialog());
        formatToolbar.add(btnSave);
        formatToolbar.add(btnOpen);

        add(formatToolbar, BorderLayout.NORTH);
        setSize(650, 450);
        setVisible(true);
        updateTextStyle();
    }

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
        if (start != end) doc.setCharacterAttributes(start, end-start, attr, false);
        else textPane.setCharacterAttributes(attr, true);
    }

    public void saveCurrentFile() {
        try {
            File userDir = new File(Desktop.Z_ROOT_PATH + user.getUsername());
            if (!userDir.exists()) userDir.mkdirs();

            if (openedFile == null) {
                // Cambiamos la extensión a .rtf (Rich Text Format) para indicar que guarda formato.
                openedFile = new File(userDir, "NuevoDocumento.rtf");
            }

            // === CORRECCIÓN para guardar el formato (estilos) ===
            FileOutputStream fos = new FileOutputStream(openedFile);
            RTFEditorKit rtfKit = new RTFEditorKit();
            
            // Escribe el documento completo (texto y estilos) en el FileOutputStream
            rtfKit.write(fos, doc, 0, doc.getLength());
            
            fos.close();
            // ====================================================

            setTitle("Editor de Texto - " + openedFile.getName());
            JOptionPane.showMessageDialog(this, "Archivo guardado correctamente en " + openedFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace(); // Para depuración
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }

    private void openFileFromDialog() {
        File userDir = new File(Desktop.Z_ROOT_PATH + user.getUsername());
        if (!userDir.exists()) userDir.mkdirs();

        JFileChooser chooser = new JFileChooser(userDir);
        
        // Opcional: Filtro para mostrar solo archivos .rtf o .txt
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Documentos de Texto (rtf, txt)", "rtf", "txt"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }

    public void openFile(File f) {
        try {
            openedFile = f;
            setTitle("Editor de Texto - " + f.getName());
            
            // === CORRECCIÓN para abrir el formato (estilos) ===
            FileInputStream fis = new FileInputStream(f);
            RTFEditorKit rtfKit = new RTFEditorKit();
            
            // Crea un nuevo documento para evitar problemas de mezcla
            StyledDocument newDoc = (StyledDocument) rtfKit.createDefaultDocument();
            
            // Lee el archivo directamente en el nuevo documento
            rtfKit.read(fis, newDoc, 0);
            
            // Asigna el nuevo documento con estilos al textPane
            textPane.setDocument(newDoc);
            this.doc = newDoc; // Actualiza la referencia del documento local
            
            fis.close();
            // ====================================================
            
        } catch (Exception e) {
            e.printStackTrace(); // Para depuración
            JOptionPane.showMessageDialog(this, "Error al abrir: " + e.getMessage());
        }
    }
}