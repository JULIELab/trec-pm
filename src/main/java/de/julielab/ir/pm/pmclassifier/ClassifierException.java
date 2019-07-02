package de.julielab.ir.pm.pmclassifier;

public class ClassifierException extends Exception {
    public ClassifierException() {
    }

    public ClassifierException(String message) {
        super(message);
    }

    public ClassifierException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassifierException(Throwable cause) {
        super(cause);
    }

    public ClassifierException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
