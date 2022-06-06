package org.habitbev.warehousemanagement.entities.inventory;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

import java.util.List;

/**
 * @author moduludu
 * Adding of inventory at a bulk level. Happens against a containerId.
 */
@Value
public class InventoryInboundRequest {

    List<String> uniqueProductIds;

    String inboundId;
    String skuCode;
    String containerId;
    String warehouseId;

    String companyId;
    InventoryStatus inventoryStatus;

    int containerMaxCapacity;

    @Builder
    private InventoryInboundRequest(List<String> uniqueProductIds, String skuCode, String containerId, String inboundId,
                                    String warehouseId, String companyId, InventoryStatus inventoryStatus, int containerMaxCapacity) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(uniqueProductIds), "uniqueProductIds cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), " sku code cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        Preconditions.checkArgument(inventoryStatus != null, "inventoryStatus cannot be null");
        Preconditions.checkArgument(containerMaxCapacity >0, "max Capacity cannot be null");


        this.uniqueProductIds = uniqueProductIds;
        this.skuCode = skuCode;
        this.inboundId = inboundId;
        this.containerId = containerId;
        this.warehouseId = warehouseId;
        this.companyId = companyId;
        this.inventoryStatus = inventoryStatus;
        this.containerMaxCapacity = containerMaxCapacity;
    }
}
