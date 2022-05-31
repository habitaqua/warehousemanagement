package org.warehousemanagement.entities;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class SKUBarcodesGenerationRequestDTO {

    String companyId;

    String warehouseId;
    SKUCategory skuCategory;
    SKUType skuType;
    int quantity;

    @Builder
    private SKUBarcodesGenerationRequestDTO(String companyId, String warehouseId, String skuCategory, String skuType, int quantity) {

        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCategory), "skucategory cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuType), "skuType cannot be null");
        Preconditions.checkArgument(quantity > 0 && quantity <= 1000, "quantity cannot be greater than 1000");


        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.skuCategory = SKUCategory.fromName(skuCategory);
        this.skuType = SKUType.fromName(skuType);
        if (!this.skuCategory.isSupported(this.skuType)) {
            throw new UnsupportedOperationException("skuCategory and skuType and not compatible");
        }
        this.quantity = quantity;
    }

    public static SKUBarcodesGenerationRequestDTO from(SKUBarcodesGenerationRequest skuBarcodesGenerationRequest) {
        return new SKUBarcodesGenerationRequestDTO(skuBarcodesGenerationRequest.getCompanyId(),
                skuBarcodesGenerationRequest.getWarehouseId(), skuBarcodesGenerationRequest.getSkuCategory(),
                skuBarcodesGenerationRequest.getSkuType(),
                skuBarcodesGenerationRequest.getQuantity());
    }
}
