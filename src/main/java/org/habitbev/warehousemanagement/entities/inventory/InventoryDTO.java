package org.habitbev.warehousemanagement.entities.inventory;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

import java.util.List;

@Value
public class InventoryDTO {

    List<String> uniqueItemIds;

    String containerId;
    String warehouseId;

    String skuCode;
    InventoryStatus inventoryStatus;
    String orderId;
    String inboundId;
    String outboundId;

    @Builder
    private InventoryDTO(List<String> uniqueItemIds, String warehouseId, String skuCode, String containerId,
                         InventoryStatus inventoryStatus, String orderId, String inboundId) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(uniqueItemIds), "uniqueItemIds cannot be empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "locationId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "locationId cannot be blank");

        this.uniqueItemIds = uniqueItemIds;
        this.warehouseId = warehouseId;
        this.skuCode = skuCode;
        this.containerId = containerId;
        this.inventoryStatus = inventoryStatus;
        this.inboundId = inboundId;
        this.outboundId = inboundId;
        this.orderId = orderId;
    }
}
