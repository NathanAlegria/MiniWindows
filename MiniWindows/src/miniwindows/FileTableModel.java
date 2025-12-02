/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package miniwindows;

/**
 *
 * @author Nathan
 */
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.List;

public class FileTableModel extends AbstractTableModel {

    private List<File> files = List.of();
    private final String[] columns = {"Nombre", "Tipo", "Tamaño"};

    public void setFiles(List<File> files) {
        this.files = files;
        fireTableDataChanged();
    }

    public File getFileAt(int row) {
        return files.get(row);
    }

    @Override
    public int getRowCount() { return files.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        File f = files.get(row);
        return switch (col) {
            case 0 -> f.getName();
            case 1 -> f.isDirectory() ? "Carpeta" : "Archivo";
            case 2 -> f.isFile() ? f.length() + " bytes" : "";
            default -> "";
        };
    }
}

