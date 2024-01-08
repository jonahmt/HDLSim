package Exceptions;

/**
 * Exception for when an unreadable expression is found in the HDL.
 */
public class HDLParseException extends HDLException {

    public HDLParseException() {

    }

    public HDLParseException(String message) {
        super(message);
    }

}
