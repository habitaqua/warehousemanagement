package org.habitbev.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
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

    @DynamoDBHashKey
    String skuCode;

    @DynamoDBRangeKey
    String warehouseId;
    @DynamoDBAttribute
    Integer availableInventory;
    @DynamoDBAttribute
    Integer allocatedInventory;

    /**
     *
     * @param skuCode
     * @param availableInventory total inventory count against skucode in INBOUND state.
     * @param allocatedInventory
     */
    @Builder
    private InventoryCounts(String skuCode, String warehouseId, Integer availableInventory, Integer allocatedInventory) {

        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");
        Preconditions.checkArgument(availableInventory >= 0, "availableInventory cannot be less than zero");
        Preconditions.checkArgument(allocatedInventory >= 0, "allocatedInventory cannot be less than zero");

        this.skuCode = skuCode;
        this.warehouseId = warehouseId;
        this.availableInventory = availableInventory;
        this.allocatedInventory = allocatedInventory;
    }
}
