package org.habitbev.warehousemanagement.entities.exceptions;

public class RetriableException extends RuntimeException{


    public RetriableException() {
        super();
    }

    public RetriableException(String message) {
        super(message);
    }

    public RetriableException(String message, Throwable cause) {
        super(message, cause);
    }


    public RetriableException(Throwable cause) {
        super(cause);
    }


    protected RetriableException(String message, Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
