package org.warehousemanagement.entities.sku;

import lombok.Builder;
import lombok.Value;

@Value
public class SKU {

    private String skuCode;
    private String companyId;
    private String skuCategory;
    private String skuType;

    @Builder
    private SKU(String skuCode, String companyId, String skuCategory, String skuType) {
        this.skuCode = skuCode;
        this.companyId = companyId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
    }
}
