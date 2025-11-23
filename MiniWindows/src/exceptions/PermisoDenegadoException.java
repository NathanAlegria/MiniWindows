/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exceptions;

/**
 *
 * @author Nathan
 */

/**
 * Lanzada cuando la operación no tiene permisos para completarse.
 */
public class PermisoDenegadoException extends Exception {
    public PermisoDenegadoException() { super(); }
    public PermisoDenegadoException(String message) { super(message); }
    public PermisoDenegadoException(String message, Throwable cause) { super(message, cause); }
}

