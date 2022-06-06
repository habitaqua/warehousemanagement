package org.habitbev.warehousemanagement.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class SKUBarcodesGenerationRequest {

    String companyId;

    String warehouseId;
    @JsonProperty("skuCode")
    String skuCode;
    @JsonProperty("quantity")
    int quantity;



    @Builder
    public SKUBarcodesGenerationRequest(String companyId, String warehouseId, String skuCode, int quantity) {
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be null");

        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuType is null");
        Preconditions.checkArgument(quantity > 0 && quantity <= 1000, "quantity should be less than 1000");

        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.skuCode = skuCode;
        this.quantity = quantity;
    }
}
