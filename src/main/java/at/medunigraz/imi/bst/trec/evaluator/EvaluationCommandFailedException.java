package at.medunigraz.imi.bst.trec.evaluator;

public class EvaluationCommandFailedException extends RuntimeException {
    public EvaluationCommandFailedException() {
    }

    public EvaluationCommandFailedException(String message) {
        super(message);
    }

    public EvaluationCommandFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluationCommandFailedException(Throwable cause) {
        super(cause);
    }

    public EvaluationCommandFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
