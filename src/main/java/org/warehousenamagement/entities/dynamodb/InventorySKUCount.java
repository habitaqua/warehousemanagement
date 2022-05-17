package org.warehousenamagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import org.warehousenamagement.entities.UOM;

public class InventorySKUCount {

    @DynamoDBHashKey(attributeName = "warehouseId")
    String warehouseId;
    @DynamoDBRangeKey(attributeName = "skuCategoryAndType")
    String skuCategoryAndType;
    String companyId;
    int quantity;
    UOM uom;
}
