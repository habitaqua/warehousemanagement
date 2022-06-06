package org.habitbev.warehousemanagement.entities.exceptions;

public class NonRetriableException extends RuntimeException{


    public NonRetriableException() {
        super();
    }

    public NonRetriableException(String message) {
        super(message);
    }

    public NonRetriableException(String message, Throwable cause) {
        super(message, cause);
    }


    public NonRetriableException(Throwable cause) {
        super(cause);
    }


    protected NonRetriableException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
