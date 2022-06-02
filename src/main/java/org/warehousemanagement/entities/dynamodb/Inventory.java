package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.dynamodb.typeconvertors.InventoryStatusTypeConvertor;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

@Data
@NoArgsConstructor
@DynamoDBTable(tableName = "inventory")
public class Inventory {

    @DynamoDBHashKey(attributeName = "uniqueProductId")
    private String uniqueProductId;

    @DynamoDBAttribute(attributeName = "warehouseId")
    private String warehouseId;

    @DynamoDBAttribute(attributeName = "containerId")
    private String containerId;
    @DynamoDBAttribute(attributeName = "skuCode")
    private String skuCode;

    @DynamoDBAttribute(attributeName = "skuCategoryType")
    private String skuCategoryType;

    @DynamoDBAttribute(attributeName = "creationTime")
    private Long creationTime;

    @DynamoDBAttribute(attributeName = "modifiedTime")
    private Long modifiedTime;

    @DynamoDBAttribute(attributeName = "productionTime")
    private Long productionTime;

    @DynamoDBAttribute(attributeName = "companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "inventoryStatus")
    @DynamoDBTypeConverted(converter = InventoryStatusTypeConvertor.class)
    private InventoryStatus inventoryStatus;


    @DynamoDBAttribute(attributeName = "orderId")
    private String orderId;

    @DynamoDBAttribute(attributeName = "inboundId")
    private String inboundId;

    @DynamoDBAttribute(attributeName = "outboundId")
    private String outboundId;

    @Builder
    private Inventory(String uniqueProductId, String warehouseId, String containerId, String skuCode,  String skuCategoryType, String inboundId, String outboundId
            , InventoryStatus inventoryStatus, String orderId, Long creationTime, Long modifiedTime, Long productionTime, String companyId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(uniqueProductId), "skuId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseLocationId" +
                " cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCategoryType), "skuCategoryType cannot be blank");
        Preconditions.checkArgument(inventoryStatus != null, "inventoryStatus cannot be null");
        Preconditions.checkArgument(creationTime != null, "creationTime cannot be null");
        Preconditions.checkArgument(productionTime != null, "productionTime cannot be null");
        Preconditions.checkArgument(modifiedTime != null, "modifiedTime cannot be null");


        this.uniqueProductId = uniqueProductId;
        this.warehouseId = warehouseId;
        this.containerId = containerId;
        this.creationTime = creationTime;
        this.modifiedTime = modifiedTime;
        this.productionTime = productionTime;
        this.skuCode = skuCode;
        this.inventoryStatus = inventoryStatus;
        this.orderId = orderId;
        this.skuCategoryType = skuCategoryType;
        this.companyId = companyId;
        this.inboundId = inboundId;
        this.outboundId = outboundId;

    }
}
