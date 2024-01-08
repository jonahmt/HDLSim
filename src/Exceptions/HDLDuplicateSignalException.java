package Exceptions;

/**
 * Exception for when a signal is found to have multiple drivers
 * or declarations in the HDL file.
 */
public class HDLDuplicateSignalException extends HDLException {

    public HDLDuplicateSignalException() {

    }

    public HDLDuplicateSignalException(String message) {
        super(message);
    }

}
