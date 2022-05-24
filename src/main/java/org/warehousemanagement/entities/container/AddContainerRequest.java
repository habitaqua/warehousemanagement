package org.warehousemanagement.entities.container;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.Capacity;
import org.warehousemanagement.utils.Utilities;

import java.util.Map;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class AddContainerRequest {
    @JsonProperty("warehouseId")
    String warehouseId;

    Map<String, Integer> skuCodeWisePredefinedCapacity;


    @Builder
    public AddContainerRequest(String warehouseId, Map<String, Integer> skuCodeWisePredefinedCapacity) {

        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseid cannot be blank");
        Preconditions.checkArgument(Utilities.validateContainerPredefinedCapacities(skuCodeWisePredefinedCapacity),
                "total capacity cannot be zero or empty");

        this.warehouseId = warehouseId;
        this.skuCodeWisePredefinedCapacity = skuCodeWisePredefinedCapacity;
    }
}
