package tech.itpark.di.exception;

public class AnnotationNotFoundException extends DIException {
    public AnnotationNotFoundException() {
    }

    public AnnotationNotFoundException(String message) {
        super(message);
    }

    public AnnotationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationNotFoundException(Throwable cause) {
        super(cause);
    }

    protected AnnotationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
