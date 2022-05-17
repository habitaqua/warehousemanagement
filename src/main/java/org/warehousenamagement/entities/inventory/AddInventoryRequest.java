package org.warehousenamagement.entities.inventory;

import lombok.Builder;
import lombok.Value;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

@Value
public class AddInventoryRequest {

    String skuId;
    SKUCategory skuCategory;
    SKUType skuType;
    String locationId;
    String warehouseId;
    String companyId;

    @Builder
    private AddInventoryRequest(String skuId, SKUCategory skuCategory, SKUType skuType, String locationId,
            String warehouseId, String companyId) {
        this.skuId = skuId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.locationId = locationId;
        this.warehouseId = warehouseId;
        this.companyId = companyId;
    }
}
