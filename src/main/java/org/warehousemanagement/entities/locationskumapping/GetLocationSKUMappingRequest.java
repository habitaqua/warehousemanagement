package org.warehousemanagement.entities.locationskumapping;

import lombok.Builder;
import lombok.Value;
import org.warehousemanagement.entities.SKUCategory;
import org.warehousemanagement.entities.SKUType;

@Value
public class GetLocationSKUMappingRequest {

    String locationId;
    String warehouseId;
    SKUType skuType;
    SKUCategory skuCategory;

    @Builder
    private GetLocationSKUMappingRequest(String locationId, String warehouseId, SKUType skuType,
                                         SKUCategory skuCategory) {
        this.locationId = locationId;
        this.warehouseId = warehouseId;
        this.skuType = skuType;
        this.skuCategory = skuCategory;
    }
}
