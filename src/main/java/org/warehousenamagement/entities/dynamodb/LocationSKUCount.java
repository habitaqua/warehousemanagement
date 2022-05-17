package org.warehousenamagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Builder;
import lombok.Value;
import org.warehousenamagement.entities.UOM;

@DynamoDBTable(tableName = "location_sku_count")
@Value
public class LocationSKUCount {

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
    private LocationSKUCount(String warehouseLocationId, String skuCategoryAndType, String warehouseId, String companyId,
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
