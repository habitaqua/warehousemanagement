package org.warehousenamagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

@DynamoDBTable(tableName = "sku-inventory")
@Getter
public class Inventory {

    @DynamoDBHashKey(attributeName = "skuId")
    private String skuId;

    @DynamoDBAttribute(attributeName = "warehouseId")
    private String warehouseId;

    @DynamoDBAttribute(attributeName = "locationId")
    private String locationId;

    @DynamoDBAttribute(attributeName = "skuType")
    @DynamoDBTypeConvertedEnum
    private SKUType skuType;

    @DynamoDBAttribute(attributeName = "companyId")
    private String companyId;

    @DynamoDBAttribute(attributeName = "creationTime")
    private long creationTime;

    @DynamoDBAttribute(attributeName = "modifiedTime")
    private long modifiedTime;

    @DynamoDBAttribute(attributeName = "skuCategory")
    @DynamoDBTypeConvertedEnum
    private SKUCategory skuCategory;

    @DynamoDBAttribute(attributeName = "status")
    private String status;

    @Builder
    private Inventory(String skuId, String warehouseId, String locationId, String companyId, long creationTime,
            long modifiedTime, SKUCategory skuCategory, SKUType skuType, String inventoryStatus) {
        Preconditions.checkArgument(skuId != null, "itemId cannot be null");
        this.skuId = skuId;
        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.creationTime = creationTime;
        this.modifiedTime = modifiedTime;
        this.skuCategory = skuCategory;
        this.skuType = skuType;
        this.locationId = locationId;
        this.status = inventoryStatus;
    }
}
