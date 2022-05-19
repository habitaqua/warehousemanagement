package org.warehousemanagement.entities;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum SKUType {

    FIVE_HUNDRED_ML("500ML", UOM.Number), TWO_FIFTY_ML("250ML", UOM.Number), ONE_LITRE("1L", UOM.Number);

    private String value;
    private UOM uom;

    SKUType(String value, UOM uom) {

        this.value = value;
        this.uom = uom;
    }

    public static SKUType fromName(String skuType) {

        Optional<SKUType> matchedEnum = Arrays.stream(SKUType.values())
                .filter(item -> item.value.equals(skuType)).findFirst();
        String message = String.format("Given skuType %s is not supported yet", skuType);
        return matchedEnum.orElseThrow(() -> new UnsupportedOperationException(message));
    }

    @Override
    public String toString() {
        return value;
    }
}
