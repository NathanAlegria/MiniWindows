/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package miniwindows;

import javax.swing.SwingUtilities;

/**
 *
 * @author Nathan
 */
public class MiniWindows {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true); 
        });
    } 
     
}
