package org.warehousemanagement.entities.inventory;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

import java.util.List;

/**
 * @author moduludu
 */
@Value
public class FulfillInventoryRequest {

    List<String> uniqueProductIds;

    String containerId;
    String warehouseId;
    String orderId;

    String outboundId;

    String skuCode;

    InventoryStatus inventoryStatus;

    int containerMaxCapacity;

    @Builder
    private FulfillInventoryRequest(List<String> uniqueProductIds, String containerId, String warehouseId, String orderId,
                                    String outboundId, String skuCode, InventoryStatus inventoryStatus, Integer containerMaxCapacity) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(uniqueProductIds), "uniqueProductIds cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(orderId), "orderId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");
        Preconditions.checkArgument(inventoryStatus != null, "inventoryStatus cannot be null");
        Preconditions.checkArgument(containerMaxCapacity != null && containerMaxCapacity> 0 , "containerMaxCapacity cannot be < 0");



        this.uniqueProductIds = uniqueProductIds;
        this.containerId = containerId;
        this.warehouseId = warehouseId;
        this.orderId = orderId;
        this.outboundId = outboundId;
        this.inventoryStatus = inventoryStatus;
        this.skuCode = skuCode;
        this.containerMaxCapacity = containerMaxCapacity;
    }
}
