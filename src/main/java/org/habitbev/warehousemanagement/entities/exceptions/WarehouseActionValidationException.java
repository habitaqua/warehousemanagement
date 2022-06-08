package org.habitbev.warehousemanagement.entities.exceptions;

public class WarehouseActionValidationException extends NonRetriableException{


    public WarehouseActionValidationException() {
        super();
    }

    public WarehouseActionValidationException(String message) {
        super(message);
    }


    public WarehouseActionValidationException(String message, Throwable cause) {
        super(message, cause);
    }


    public WarehouseActionValidationException(Throwable cause) {
        super(cause);
    }


    protected WarehouseActionValidationException(String message, Throwable cause,
                                                 boolean enableSuppression,
                                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
