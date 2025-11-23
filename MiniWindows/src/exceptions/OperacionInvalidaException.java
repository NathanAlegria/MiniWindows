/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exceptions;

/**
 *
 * @author Nathan
 */
public class OperacionInvalidaException extends Exception {

    public OperacionInvalidaException() {
        super();
    }

    public OperacionInvalidaException(String message) {
        super(message);
    }

    public OperacionInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}

