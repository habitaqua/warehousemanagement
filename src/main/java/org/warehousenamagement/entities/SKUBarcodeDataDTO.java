package org.warehousenamagement.entities;

import lombok.Builder;
import lombok.Value;

@Value
public class SKUBarcodeDataDTO implements BarcodeDataDTO{


    String skuId;
    SKUCategory skuCategory;
    SKUType skuType;

    @Builder
    public SKUBarcodeDataDTO(String skuId, SKUCategory skuCategory, SKUType skuType) {
        this.skuId = skuId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
    }
}
