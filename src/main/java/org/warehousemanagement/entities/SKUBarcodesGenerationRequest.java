package org.warehousemanagement.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class SKUBarcodesGenerationRequest {
    @JsonProperty("skuCategory")
    String skuCategory;
    @JsonProperty("skuType")
    String skuType;
    @JsonProperty("quantity")
    int quantity;



    @Builder
    public SKUBarcodesGenerationRequest(String skuCategory, String skuType, int quantity) {
        Preconditions.checkArgument(null != skuCategory, "skuCategory is null");
        Preconditions.checkArgument(null != skuType, "skuType is null");
        Preconditions.checkArgument(quantity > 0 && quantity < 1000, "quantity should be less than 1000");

        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.quantity = quantity;
    }
}