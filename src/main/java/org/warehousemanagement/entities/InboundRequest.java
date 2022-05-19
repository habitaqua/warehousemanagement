package org.warehousemanagement.entities;

import lombok.Builder;
import lombok.Value;
import org.warehousemanagement.entities.SKUCategory;
import org.warehousemanagement.entities.SKUType;

@Value
public class InboundRequest {

    String skuId;
    SKUCategory skuCategory;
    SKUType skuType;
    String locationId;
    String warehouseId;
    String companyId;

    @Builder
    private InboundRequest(String skuId, SKUCategory skuCategory, SKUType skuType, String locationId,
                           String warehouseId, String companyId) {
        this.skuId = skuId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.locationId = locationId;
        this.warehouseId = warehouseId;
        this.companyId = companyId;
    }
}
