package org.habitbev.warehousemanagement.entities.sku;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;

@Value
public class SKUDTO {


    private String skuCode;
    private String companyId;
    private String skuCategory;
    private String skuType;

    @Builder
    private SKUDTO(String skuCode, String companyId, String skuCategory, String skuType) {
        this.skuCode = skuCode;
        this.companyId = companyId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
    }

    public static SKUDTO fromSKU(SKU sku) {
        Preconditions.checkArgument(sku != null, "sku cannot be null");
        return SKUDTO.builder().companyId(sku.getCompanyId()).skuCode(sku.getSkuCode())
                .skuCategory(sku.getSkuCategory()).skuType(sku.getSkuType()).build();
    }
}
