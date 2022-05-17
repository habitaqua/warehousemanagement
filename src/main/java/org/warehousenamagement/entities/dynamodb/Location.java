package org.warehousenamagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Builder;
import org.warehousenamagement.entities.Capacity;
import org.warehousenamagement.entities.SKUType;

import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "locations")
public class Location {

    String id;
    String warehouseId;
    Set<SKUType> supportedSKUTypes;
    boolean allSKUSupported;
    Map<SKUType, Capacity> currentSKUs;
    boolean isActive;

    @Builder
    private Location(String id,String warehouseId, String code,
            Set<SKUType> supportedSKUTypes, Boolean allSKUSupported,
            Map<SKUType, Capacity> currentSKUs, Boolean isActive) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.supportedSKUTypes = supportedSKUTypes;
        this.allSKUSupported = allSKUSupported;
        this.currentSKUs = currentSKUs;
        this.isActive = isActive;

    }
}
