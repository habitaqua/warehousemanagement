package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.common.base.Preconditions;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

/**
 * @author moduludu
 * Counts table to maintainingg available vs allocated inventory. free inventory is calculated at runtime.
 */
@DynamoDBTable(tableName = "inventory-counts")
public class InventoryCounts {

    String skuCode;
    Integer availableInventory;
    Integer allocatedInventory;

    /**
     *
     * @param skuCode
     * @param availableInventory total inventory count against skucode in INBOUND state.
     * @param allocatedInventory
     */
    @Builder
    private InventoryCounts(String skuCode, Integer availableInventory, Integer allocatedInventory) {

        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be empty");
        Preconditions.checkArgument(availableInventory >= 0, "availableInventory cannot be less than zero");
        Preconditions.checkArgument(allocatedInventory >= 0, "allocatedInventory cannot be less than zero");

        this.skuCode = skuCode;
        this.availableInventory = availableInventory;
        this.allocatedInventory = allocatedInventory;
    }
}
