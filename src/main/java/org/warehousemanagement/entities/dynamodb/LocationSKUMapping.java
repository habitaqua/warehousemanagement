package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Builder;
import lombok.Value;
import org.warehousemanagement.entities.UOM;

@DynamoDBTable(tableName = "location_sku_mapping")
@Value
public class LocationSKUMapping {

    @DynamoDBHashKey(attributeName = "warehouseLocationId")
    String warehouseLocationId;
    @DynamoDBRangeKey(attributeName = "skuCategoryAndType")
    String skuCategoryAndType;
    String warehouseId;
    String companyId;
    int quantity;
    UOM uom;
    long creationTime;
    long modifiedtime;



    @Builder
    private LocationSKUMapping(String warehouseLocationId, String skuCategoryAndType, String warehouseId, String companyId,
                               int quantity, UOM uom, Long creationTime, Long modifiedTime) {
        this.warehouseLocationId = warehouseLocationId;
        this.skuCategoryAndType = skuCategoryAndType;
        this.warehouseId = warehouseId;
        this.companyId = companyId;
        this.quantity = quantity;
        this.uom = uom;
        this.creationTime = creationTime;
        this.modifiedtime = modifiedTime;
    }
}
