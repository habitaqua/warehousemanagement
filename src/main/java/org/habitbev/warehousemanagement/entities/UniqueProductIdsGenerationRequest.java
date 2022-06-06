package org.habitbev.warehousemanagement.entities;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class UniqueProductIdsGenerationRequest {

    String companyId;

    Long productionTime;
    String warehouseId;
    String skuCategory;
    String skuType;

    String skuCode;
    int quantity;

    @Builder
    private UniqueProductIdsGenerationRequest(String companyId, String warehouseId, String skuCategory, String skuType,
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
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.skuCode = skuCode;
        this.quantity = quantity;
        this.productionTime = productionTime;
    }
}
