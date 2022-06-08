package org.habitbev.warehousemanagement.entities.exceptions;

public class InconsistentStateException extends NonRetriableException{


    public InconsistentStateException() {
        super();
    }

    public InconsistentStateException(String message) {
        super(message);
    }


    public InconsistentStateException(String message, Throwable cause) {
        super(message, cause);
    }


    public InconsistentStateException(Throwable cause) {
        super(cause);
    }


    protected InconsistentStateException(String message, Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
