package org.warehousemanagement.entities.locationskumapping;

import lombok.Builder;
import lombok.Value;
import org.warehousemanagement.entities.Capacity;
import org.warehousemanagement.entities.SKUCategory;
import org.warehousemanagement.entities.SKUType;

@Value
public class UpdateLocationSKUMappingRequest {

    String locationId;
    String warehouseId;
    SKUCategory skuCategory;
    SKUType skuType;
    Capacity deltaCapacity;

    @Builder
    private UpdateLocationSKUMappingRequest(String locationId, String warehouseId, SKUCategory skuCategory,
                                            SKUType skuType, Capacity deltaCapacity) {
        this.locationId = locationId;
        this.warehouseId = warehouseId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.deltaCapacity = deltaCapacity;
    }
}
