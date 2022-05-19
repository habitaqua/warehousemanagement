package org.warehousemanagement.entities.inventory;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.warehousemanagement.entities.SKUCategory;
import org.warehousemanagement.entities.SKUType;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

@Value
public class InventoryDTO {

    String skuId;
    String companyId;
    String locationId;
    String warehouseId;
    SKUCategory skuCategory;
    SKUType skuType;
    InventoryStatus inventoryStatus;

    @Builder
    private InventoryDTO(String skuId, String companyId, String warehouseId, SKUCategory skuCategory,
            SKUType skuType, String locationId, InventoryStatus inventoryStatus) {
        Preconditions.checkArgument(skuId!=null, "skuId cannot be null");
        this.skuId = skuId;
        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.locationId = locationId;
        this.inventoryStatus = inventoryStatus;
    }
}
