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
 * Adding of inventory at a bulk level. Happens against a containerId.
 */
@Value
public class InventoryAddRequest {

    List<String> uniqueProductIds;

    String skuCode;

    String skuCategory;

    String skuType;
    String warehouseId;

    String companyId;

    long productionTime;
    InventoryStatus inventoryStatus;



    @Builder
    private InventoryAddRequest(List<String> uniqueProductIds, String skuCode, String companyId, Long productionTime,
                               String skuCategory, String skuType, String warehouseId, InventoryStatus inventoryStatus) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(uniqueProductIds), "uniqueProductIds cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), " skuCode cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCategory), " skuCategory cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuType), " skuType cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        Preconditions.checkArgument(productionTime != null, "productionTime cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(inventoryStatus != null, "inventoryStatus cannot be null");


        this.uniqueProductIds = uniqueProductIds;
        this.skuCode = skuCode;
        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.inventoryStatus = inventoryStatus;
        this.productionTime = productionTime;
        this.skuCategory = skuCategory;
        this.skuType = skuType;

    }
}
