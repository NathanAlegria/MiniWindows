/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EditordeTexto;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import miniwindows.ImageUtils;

/**
 *
 * @author Nathan
 */
public class EditorTexto extends JInternalFrame {

    private JTextPane textPane;
    private StyledDocument doc;

    private File openedFile = null;

    public EditorTexto() {
        super("Editor de Texto - Sin Título", true, true, true, true);

        setLayout(new BorderLayout());
        setFrameIcon(ImageUtils.getScaledIcon("iconos/text.png", 16, 16));

        // Área de edición
        textPane = new JTextPane();
        doc = textPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        // Barra de herramientas
        JToolBar formatToolbar = new JToolBar();
        formatToolbar.setFloatable(false);

        formatToolbar.add(new JLabel(" Fuente: "));
        formatToolbar.add(new JComboBox<>(new String[]{"Arial", "Times New Roman", "Lucida Calligraphy"}));

        formatToolbar.add(new JLabel(" Tamaño: "));
        formatToolbar.add(new JComboBox<>(new String[]{"12", "18", "48"}));

        JButton colorBtn = new JButton(ImageUtils.getScaledIcon("iconos/color.png", 16, 16));
        colorBtn.setToolTipText("Cambiar color (No implementado)");
        formatToolbar.add(new JLabel(" Color: "));
        formatToolbar.add(colorBtn);

        formatToolbar.addSeparator();

        JButton btnSave = new JButton("Guardar");
        JButton btnOpen = new JButton("Abrir");

        formatToolbar.add(btnSave);
        formatToolbar.add(btnOpen);

        add(formatToolbar, BorderLayout.NORTH);

        // Acciones
        btnSave.addActionListener(e -> saveCurrentFile());
        btnOpen.addActionListener(e -> openFileFromDialog());

        setSize(650, 450);
        setVisible(true);
    }

    // Cargar archivo existente
    public void openFile(File f) {
        try {
            openedFile = f;
            setTitle("Editor de Texto - " + f.getName());
            textPane.setText("");

            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                textPane.getDocument().insertString(textPane.getDocument().getLength(), line + "\n", null);
            }
            br.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al abrir: " + e.getMessage());
        }
    }

    // Guardar archivo
    public void saveCurrentFile() {
        try {
            if (openedFile == null) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File("NuevoDocumento.txt"));
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    openedFile = chooser.getSelectedFile();
                    setTitle("Editor de Texto - " + openedFile.getName());
                } else {
                    return;
                }
            }

            PrintWriter pw = new PrintWriter(new FileWriter(openedFile));
            pw.print(textPane.getText());
            pw.close();

            JOptionPane.showMessageDialog(this, "Archivo guardado correctamente.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }

    private void openFileFromDialog() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }
}

