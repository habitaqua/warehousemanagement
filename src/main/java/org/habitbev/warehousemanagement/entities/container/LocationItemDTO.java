package org.habitbev.warehousemanagement.entities.container;

import org.habitbev.warehousemanagement.entities.Capacity;
import org.habitbev.warehousemanagement.entities.SKUCategory;
import org.habitbev.warehousemanagement.entities.SKUType;

public class LocationItemDTO {

    String id;
    SKUCategory skuCategory;
    SKUType skuType;
    Capacity capacity;

    public LocationItemDTO(String id, SKUCategory skuCategory, SKUType skuType, Capacity capacity) {
        this.id = id;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.capacity = capacity;
    }
}
