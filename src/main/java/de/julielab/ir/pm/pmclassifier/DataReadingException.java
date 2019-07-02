package de.julielab.ir.pm.pmclassifier;

public class DataReadingException extends ClassifierException {
    public DataReadingException() {
    }

    public DataReadingException(String message) {
        super(message);
    }

    public DataReadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataReadingException(Throwable cause) {
        super(cause);
    }

    public DataReadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
