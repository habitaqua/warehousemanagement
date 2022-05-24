package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.SKUCategory;
import org.warehousemanagement.entities.SKUType;
import org.warehousemanagement.entities.dynamodb.typeconvertors.InventoryStatusTypeConvertor;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

@DynamoDBTable(tableName = "inventory")
@Getter
public class Inventory {

    @DynamoDBHashKey(attributeName = "uniqueItemId")
    private String uniqueItemId;

    @DynamoDBAttribute(attributeName = "warehouseLocationId")
    private String warehouseLocationId;

    @DynamoDBAttribute(attributeName = "skuCode")
    private String skuCode;

    @DynamoDBAttribute(attributeName = "creationTime")
    private long creationTime;

    @DynamoDBAttribute(attributeName = "modifiedTime")
    private long modifiedTime;

    @DynamoDBAttribute(attributeName = "status")
    @DynamoDBTypeConverted(converter = InventoryStatusTypeConvertor.class)
    private InventoryStatus status;

    private String orderId;

    @Builder
    private Inventory(String skuId, String warehouseLocationId, String skuCode, InventoryStatus inventoryStatus,
                      String orderId, Long creationTime, Long modifiedTime) {
        Preconditions.checkArgument(StringUtils.isNotBlank(skuId), "skuId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseLocationId), "warehouseLocationId" +
                " cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");
        Preconditions.checkArgument(inventoryStatus != null, "inventoryStatus cannot be null");
        Preconditions.checkArgument(creationTime != null, "creationTime cannot be null");
        Preconditions.checkArgument(modifiedTime != null, "modifiedTime cannot be null");


        this.uniqueItemId = skuId;
        this.warehouseLocationId = warehouseLocationId;
        this.creationTime = creationTime;
        this.modifiedTime = modifiedTime;
        this.skuCode = skuCode;
        this.status = inventoryStatus;
        this.orderId = orderId;
    }
}
