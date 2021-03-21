package tech.itpark.di.exception;

public class MultipleInterfaceImplementationException extends DIException {
    public MultipleInterfaceImplementationException() {
        super();
    }

    public MultipleInterfaceImplementationException(String message) {
        super(message);
    }

    public MultipleInterfaceImplementationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleInterfaceImplementationException(Throwable cause) {
        super(cause);
    }

    protected MultipleInterfaceImplementationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
