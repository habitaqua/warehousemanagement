package org.warehousenamagement.entities.location;

import org.warehousenamagement.entities.Capacity;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

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
