package org.jetbrains.test.calltree;

/**
 * Created by mekhrubon on 13.05.2017.
 */
public class FileParseException extends Exception {
    public FileParseException(String message) {
        super(message);
    }

    public FileParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileParseException() {
        super();
    }

    public FileParseException(Throwable cause) {
        super(cause);
    }
}
