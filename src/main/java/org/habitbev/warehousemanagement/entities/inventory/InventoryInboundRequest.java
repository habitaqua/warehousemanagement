package org.habitbev.warehousemanagement.entities.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

import java.util.List;

/**
 * @author moduludu
 * Adding of inventory at a bulk level. Happens against a containerId.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor
public class InventoryInboundRequest {

    @JsonProperty("uniqueProductIds")
    List<String> uniqueProductIds;

    @JsonProperty("inboundId")
    String inboundId;

    @JsonProperty("skuCode")
    String skuCode;

    @JsonProperty("containerId")
    String containerId;

    @JsonProperty("warehouseId")
    String warehouseId;

    @JsonProperty("companyId")
    String companyId;



    @Builder
    private InventoryInboundRequest(List<String> uniqueProductIds, String skuCode, String containerId, String inboundId,
                                    String warehouseId, String companyId) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(uniqueProductIds), "uniqueProductIds cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), " sku code cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");

        this.uniqueProductIds = uniqueProductIds;
        this.skuCode = skuCode;
        this.inboundId = inboundId;
        this.containerId = containerId;
        this.warehouseId = warehouseId;
        this.companyId = companyId;
    }
}
