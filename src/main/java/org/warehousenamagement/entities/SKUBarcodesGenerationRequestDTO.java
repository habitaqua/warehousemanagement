package org.warehousenamagement.entities;

import lombok.Builder;
import lombok.Value;

@Value
public class SKUBarcodesGenerationRequestDTO {

    SKUCategory skuCategory;
    SKUType skuType;
    int quantity;

    @Builder
    private SKUBarcodesGenerationRequestDTO(String skuCategory, String skuType, int quantity) {
        this.skuCategory = SKUCategory.fromName(skuCategory);
        this.skuType = SKUType.fromName(skuType);
        if (!this.skuCategory.isSupported(this.skuType)) {
            throw new UnsupportedOperationException("skuCategory and skuType and not compatible");
        }
        this.quantity = quantity;
    }

    public static SKUBarcodesGenerationRequestDTO from(SKUBarcodesGenerationRequest skuBarcodesGenerationRequest) {
        return new SKUBarcodesGenerationRequestDTO(skuBarcodesGenerationRequest.getSkuCategory(),
                skuBarcodesGenerationRequest.getSkuType(),
                skuBarcodesGenerationRequest.getQuantity());
    }
}
