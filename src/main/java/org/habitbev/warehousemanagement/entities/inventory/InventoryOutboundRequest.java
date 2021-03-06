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
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor
public class InventoryOutboundRequest {

    @JsonProperty("uniqueProductIds")
    List<String> uniqueProductIds;
    @JsonProperty("containerId")
    String containerId;

    @JsonProperty("warehouseId")
    String warehouseId;

    @JsonProperty("orderId")
    String orderId;

    @JsonProperty("companyId")
    String companyId;

    @JsonProperty("outboundId")
    String outboundId;

    @JsonProperty("skuCode")
    String skuCode;


    @Builder
    private InventoryOutboundRequest(List<String> uniqueProductIds, String containerId, String warehouseId, String orderId, String companyId,
                                     String outboundId, String skuCode) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(uniqueProductIds), "uniqueProductIds cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(orderId), "orderId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");

        this.uniqueProductIds = uniqueProductIds;
        this.containerId = containerId;
        this.warehouseId = warehouseId;
        this.orderId = orderId;
        this.outboundId = outboundId;
        this.skuCode = skuCode;
        this.companyId = companyId;
    }
}
