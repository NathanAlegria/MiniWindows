/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CMD;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CMD_Funciones {
    private File archivo = null;

    public void setFile(String dirrecion) {
        archivo = new File(dirrecion);
    }

    public File getFile() {
        return archivo;
    }

    public boolean esDirectorio() {
        return archivo != null && archivo.isDirectory();
    }

    public boolean crearCarpeta(File directorio, String nombreCarpeta) throws IOException {
        File nuevaCarpeta = new File(directorio, nombreCarpeta);
        return nuevaCarpeta.mkdir();
    }

    public boolean crearArchivo(File directorio, String nombreArchivo) throws IOException {
        File nuevoArchivo = new File(directorio, nombreArchivo);
        return nuevoArchivo.createNewFile();
    }

    public boolean eliminar(File arch) throws IOException {
        if (arch.isDirectory()) {
            File[] contenido = arch.listFiles();
            if (contenido != null) {
                for (File hijo : contenido) {
                    eliminar(hijo);
                }
            }
        }
        return arch.delete();
    }

    public File cambiarDirectorio(File directorioActual, String ruta) {
        if (ruta == null || ruta.isEmpty()) return directorioActual;

        File nuevoDir = new File(ruta);

        if (!nuevoDir.isAbsolute()) {
            nuevoDir = new File(directorioActual, ruta);
        }

        if (nuevoDir.exists() && nuevoDir.isDirectory()) {
            return nuevoDir;
        } else {
            return directorioActual;
        }
    }

    public String listarDirectorio(File directorio) {
        if (!directorio.exists()) 
            return "Error: El directorio no existe.";

        if (!directorio.isDirectory()) 
            return "Error: La ruta especificada no es un directorio";

        StringBuilder resultado = new StringBuilder();

        File[] contenido = directorio.listFiles();
        if (contenido == null || contenido.length == 0) {
            return "El directorio está vacío.";
        }

        for (File elemento : contenido) {
            if (!elemento.isHidden()) {
                String tipo = elemento.isDirectory() ? "<DIR>" : "     ";
                resultado.append("\n").append(tipo).append(" - ").append(elemento.getName());
            }
        }
        return resultado.toString();
    }

    public File directorioAnterior(File actual) {
        File padre = actual.getParentFile();
        if (padre != null) return padre;
        return actual;
    }

    public String getDate() {
        return new SimpleDateFormat("dd/MM/yyyy")
                .format(Calendar.getInstance().getTime());
    }

    public String getTime() {
        return new SimpleDateFormat("HH:mm:ss")
                .format(new Date());
    }

    public void escribirArchivo(File mf, String texto) throws IOException {
        if (!mf.exists()) throw new IOException("El archivo no existe.");

        if (mf.isHidden() || !mf.isFile()) 
            throw new IOException("Ruta inválida.");

        BufferedWriter write = new BufferedWriter(new FileWriter(mf, true));
        write.write(texto);
        write.newLine();
        write.close();
    }

    public String leerArchivo(File mf) throws IOException {
        if (!mf.exists()) throw new IOException("El archivo no existe.");

        if (mf.isHidden() || !mf.isFile()) 
            throw new IOException("Ruta inválida.");

        BufferedReader leer = new BufferedReader(new FileReader(mf));
        StringBuilder texto = new StringBuilder();
        String linea;

        while ((linea = leer.readLine()) != null) {
            texto.append(linea).append("\n");
        }
        leer.close();
        return texto.toString();
    }
}
