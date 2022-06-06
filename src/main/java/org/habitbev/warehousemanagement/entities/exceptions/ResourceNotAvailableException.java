package org.habitbev.warehousemanagement.entities.exceptions;

public class ResourceNotAvailableException extends NonRetriableException{


    public ResourceNotAvailableException() {
        super();
    }

    public ResourceNotAvailableException(String message) {
        super(message);
    }

    public ResourceNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }


    public ResourceNotAvailableException(Throwable cause) {
        super(cause);
    }


    protected ResourceNotAvailableException(String message, Throwable cause,
                                            boolean enableSuppression,
                                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
