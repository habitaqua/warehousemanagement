package org.warehousemanagement.entities.container;

import org.warehousemanagement.entities.Capacity;
import org.warehousemanagement.entities.SKUCategory;
import org.warehousemanagement.entities.SKUType;

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
