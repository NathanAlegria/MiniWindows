/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CMD;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CMD_GUI extends javax.swing.JPanel {

    private JTextArea consola;
    private CMD_Funciones gestor1;
    private String prompt;
    private File directorioActual;

    public CMD_GUI() {
        setPreferredSize(new java.awt.Dimension(800, 500));
        setLayout(new java.awt.BorderLayout()); 
        directorioActual = new File(System.getProperty("user.dir"));
        prompt = directorioActual.getAbsolutePath() + "> ";

        consola = new JTextArea();
        consola.setFont(new Font("Consolas", Font.PLAIN, 14));
        consola.setBackground(Color.BLACK);
        consola.setForeground(Color.WHITE);
        consola.setCaretColor(Color.WHITE);

        consola.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();

                    String textoCompleto = consola.getText();
                    int indicePrompt = textoCompleto.lastIndexOf(prompt);

                    if (indicePrompt != -1) {
                        String comando = textoCompleto.substring(indicePrompt + prompt.length()).trim();

                        if (!comando.isEmpty()) {
                            consola.append("\n");
                            procesarComando(comando);
                        }

                        consola.append("\n" + prompt);
                        consola.setCaretPosition(consola.getText().length());
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (cursorEnPrompt()) {
                        e.consume();
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_UP) {
                    if (cursorEnPrompt()) {
                        e.consume();
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (cursorEnPrompt()) {
                    consola.setCaretPosition(consola.getText().length());
                }
            }
        });

        consola.setText("Microsoft Windows [Version 10.0.22621.521]\n");
        consola.append("(c) Nathan y Jeremy. Todos los derechos reservados.\n\n");
        consola.append(prompt);

        add(new JScrollPane(consola), java.awt.BorderLayout.CENTER);

        gestor1 = new CMD_Funciones();
    }

    private boolean cursorEnPrompt() {
        String texto = consola.getText();
        int indicePrompt = texto.lastIndexOf(prompt);
        return consola.getCaretPosition() <= indicePrompt + prompt.length();
    }

    private String extraerArgumento(String texto) {
        int inicio = texto.indexOf('<');
        int fin = texto.indexOf('>');

        if (inicio != -1 && fin != -1 && fin > inicio) {
            return texto.substring(inicio + 1, fin).trim();
        }

        String[] partes = texto.split("\\s+", 2);
        if (partes.length > 1) {
            return partes[1].replace("<", "").replace(">", "").trim();
        }

        return "";
    }

    private String extraerComando(String texto) {
        return texto.trim().split("\\s+")[0];
    }

    private void procesarComando(String entrada) {
        try {

            String comando = extraerComando(entrada);
            String argumento = extraerArgumento(entrada);

            switch (comando) {
                case "Mkdir":
                    ejecutarMkdir(argumento);
                    break;
                case "Mfile":
                    ejecutarMfile(argumento);
                    break;
                case "Rm":
                    ejecutarRm(argumento);
                    break;
                case "Cd":
                    ejecutarCd(argumento);
                    break;
                case "...":
                    ejecutarRegresarDir();
                    break;
                case "Dir":
                    ejecutarDir();
                    break;
                case "Date":
                    ejecutarDate();
                    break;
                case "Time":
                    ejecutarTime();
                    break;
                case "Escribir":
                    ejecutarEscribir(entrada);
                    break;
                case "Leer":
                    ejecutarLeer(argumento);
                    break;
                case "Exit":
                    ejecutarExit();
                    break;
                default:
                    consola.append("Error: Comando no reconocido - " + comando);
            }

        } catch (IOException e) {
            consola.append("Error de disco: " + e.getMessage());
        } catch (Exception e) {
            consola.append("Error: " + e.getMessage());
        }
    }

    private void ejecutarMkdir(String nombreCarpeta) throws IOException {
        if (nombreCarpeta.isEmpty()) {
            consola.append("Error: Debe especificar el nombre de la carpeta");
            return;
        }
        if (gestor1.crearCarpeta(directorioActual, nombreCarpeta)) {
            consola.append("Carpeta creada exitosamente");
        } else {
            consola.append("Error: No se pudo crear la carpeta");
        }
    }

    private void ejecutarMfile(String nombreArchivo) throws IOException {
        if (nombreArchivo.isEmpty()) {
            consola.append("Error: Debe especificar el nombre del archivo");
            return;
        }
        if (gestor1.crearArchivo(directorioActual, nombreArchivo)) {
            consola.append("Archivo creado exitosamente");
        } else {
            consola.append("Error: No se pudo crear el archivo");
        }
    }

    private void ejecutarRm(String ruta) throws IOException {
        if (ruta.isEmpty()) {
            consola.append("Error: Debe especificar la ruta a eliminar");
            return;
        }

        File archivoEliminar = new File(directorioActual, ruta);

        if (!archivoEliminar.exists()) {
            consola.append("Error: El archivo o carpeta no existe");
            return;
        }

        if (gestor1.eliminar(archivoEliminar)) {
            consola.append("Elemento eliminado exitosamente");
        } else {
            consola.append("Error: No se pudo eliminar");
        }
    }

    private void ejecutarCd(String ruta) {
        File nuevoDir = gestor1.cambiarDirectorio(directorioActual, ruta);

        if (!nuevoDir.equals(directorioActual)) {
            directorioActual = nuevoDir;
            prompt = directorioActual.getAbsolutePath() + "> ";
            consola.append("Directorio cambiado");
        }
    }

    private void ejecutarRegresarDir() {
        File anterior = gestor1.directorioAnterior(directorioActual);

        if (!anterior.equals(directorioActual)) {
            directorioActual = anterior;
            prompt = directorioActual.getAbsolutePath() + "> ";
        } else {
            consola.append("Ya está en el directorio raíz");
        }
    }

    private void ejecutarDir() {
        consola.append(gestor1.listarDirectorio(directorioActual));
    }

    private void ejecutarDate() {
        consola.append("Fecha actual: " + gestor1.getDate());
    }

    private void ejecutarTime() {
        consola.append("Hora actual: " + gestor1.getTime());
    }

    private void ejecutarEscribir(String entrada) throws IOException {
        String resto = entrada.substring(9).trim();

        int separador = resto.indexOf(':');
        String nombreArchivo;
        String texto;

        if (separador != -1) {
            nombreArchivo = resto.substring(0, separador).trim();
            texto = resto.substring(separador + 1).trim();
        } else {
            String[] partes = resto.split("\\s+", 2);
            if (partes.length < 2) {
                consola.append("Error: Formato incorrecto. Uso: Escribir <archivo>: texto");
                return;
            }
            nombreArchivo = partes[0];
            texto = partes[1];
        }

        nombreArchivo = nombreArchivo.replace("<", "").replace(">", "").trim();

        File archivo = new File(directorioActual, nombreArchivo);
        gestor1.escribirArchivo(archivo, texto);

        consola.append("Texto guardado en el archivo");
    }

    private void ejecutarLeer(String archivoNombre) throws IOException {
        if (archivoNombre.isEmpty()) {
            consola.append("Error: Debe especificar el nombre del archivo");
            return;
        }

        File archivo = new File(directorioActual, archivoNombre);
        consola.append("\n------ CONTENIDO DEL ARCHIVO ------\n");
        consola.append(gestor1.leerArchivo(archivo));
        consola.append("-----------------------------------\n");
    }

    private void ejecutarExit() {
        consola.append("Cerrando la consola...");
        System.exit(0);
    }
}
