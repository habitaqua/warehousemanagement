package org.warehousenamagement.entities.location;

import lombok.Builder;
import lombok.Value;
import org.warehousenamagement.entities.Capacity;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

@Value
public class UpdateLocationSKUCountRequest {

    String locationId;
    String warehouseId;
    SKUCategory skuCategory;
    SKUType skuType;
    Capacity deltaCapacity;

    @Builder
    private UpdateLocationSKUCountRequest(String locationId, String warehouseId, SKUCategory skuCategory,
            SKUType skuType, Capacity deltaCapacity) {
        this.locationId = locationId;
        this.warehouseId = warehouseId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.deltaCapacity = deltaCapacity;
    }
}
