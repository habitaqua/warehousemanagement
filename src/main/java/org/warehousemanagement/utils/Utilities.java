package org.warehousemanagement.utils;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.Optional;

public class Utilities {

    public static boolean validateContainerPredefinedCapacities(Map<String, Integer> skuWiseCapacity) {
        if (MapUtils.isEmpty(skuWiseCapacity))
            return false;
        Optional<Integer> capacityOutOfBounds = skuWiseCapacity.values().stream().filter(value -> value <= 0).findAny();
        if (capacityOutOfBounds.isPresent())
            return false;
        return true;
    }
}
