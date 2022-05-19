package org.warehousemanagement.entities;

import java.util.Arrays;
import java.util.Optional;

public enum UOM {

    Kilogram("kg"), Metre("mt"), Number("nm");

    private String value;

    UOM(String value) {
        this.value = value;
    }

    public static UOM fromName(String uom) {

        Optional<UOM> matchedEnum = Arrays.stream(UOM.values())
                .filter(item -> item.value.equals(uom)).findFirst();
        String message = String.format("Given uoum %s is not supported yet", uom);
        return matchedEnum.orElseThrow(() -> new UnsupportedOperationException(message));
    }

    @Override
    public String toString() {
        return value;
    }

}
