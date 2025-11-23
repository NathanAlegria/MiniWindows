/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exceptions;

/**
 *
 * @author Nathan
 */
public class ArchivoInvalidoException extends Exception {

    public ArchivoInvalidoException() {
        super();
    }

    public ArchivoInvalidoException(String message) {
        super(message);
    }

    public ArchivoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
