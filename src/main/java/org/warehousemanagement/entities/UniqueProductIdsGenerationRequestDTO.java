package org.warehousemanagement.entities;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class UniqueProductIdsGenerationRequestDTO {

    String companyId;

    Long productionTime;
    String warehouseId;
    SKUCategory skuCategory;
    SKUType skuType;

    String skuCode;
    int quantity;

    @Builder
    private UniqueProductIdsGenerationRequestDTO(String companyId, String warehouseId, String skuCategory, String skuType,
                                                 String skuCode, int quantity, Long productionTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCategory), "skucategory cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuType), "skuType cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be null");
        Preconditions.checkArgument(productionTime != null && productionTime > 0, "productionTime should be > 0");
        Preconditions.checkArgument(quantity > 0 && quantity <= 1000, "quantity cannot be greater than 1000");


        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.skuCategory = SKUCategory.fromName(skuCategory);
        this.skuType = SKUType.fromName(skuType);
        this.skuCode = skuCode;
        if (!this.skuCategory.isSupported(this.skuType)) {
            throw new UnsupportedOperationException("skuCategory and skuType and not compatible");
        }
        this.quantity = quantity;
        this.productionTime = productionTime;
    }
}
