package Exceptions;

/**
 * General custom exception for this project.
 * All other custom exceptions inherit from this class.
 */
public class HDLException extends RuntimeException {

    public HDLException() {

    }

    public HDLException(String message) {
        super(message);
    }

}
