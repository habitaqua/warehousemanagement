package org.warehousenamagement.entities.location;

import lombok.Builder;
import lombok.Value;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

@Value
public class GetLocationSKUCountRequest {

    String locationId;
    String warehouseId;
    SKUType skuType;
    SKUCategory skuCategory;

    @Builder
    private GetLocationSKUCountRequest(String locationId, String warehouseId, SKUType skuType,
            SKUCategory skuCategory) {
        this.locationId = locationId;
        this.warehouseId = warehouseId;
        this.skuType = skuType;
        this.skuCategory = skuCategory;
    }
}
