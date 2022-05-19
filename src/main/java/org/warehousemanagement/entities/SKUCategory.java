package org.warehousemanagement.entities;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Getter
public enum SKUCategory {

    FINISHED_GOODS("Finished Goods", ImmutableSet.of(SKUType.FIVE_HUNDRED_ML,
            SKUType.TWO_FIFTY_ML, SKUType.ONE_LITRE));

    private String value;
    private Set<SKUType> supportedSKUs;

    SKUCategory(String value, Set<SKUType> supportedSKUs) {
        this.value = value;
        this.supportedSKUs = supportedSKUs;
    }

    public boolean isSupported(SKUType skuType) {

        return this.supportedSKUs.contains(skuType);
    }

    public static SKUCategory fromName(String skuCategory) {

        Optional<SKUCategory> matchedEnum = Arrays.stream(SKUCategory.values())
                .filter(item -> item.value.equals(skuCategory)).findFirst();
        String message = String.format("Given skuCategory %s is not supported yet", skuCategory);
        return matchedEnum.orElseThrow(() -> new UnsupportedOperationException(message));
    }

    @Override
    public String toString() { return value; }
}
