package org.habitbev.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Inbound;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Outbound;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Production;

public class InventoryStatusTypeConvertor implements DynamoDBTypeConverter<String, InventoryStatus> {

    @Override
    public String convert(InventoryStatus inventoryStatus) {
        return inventoryStatus.getStatus();
    }
    @Override
    public InventoryStatus unconvert(String inventoryStatusString) {
        switch (inventoryStatusString) {
            case "INBOUND":
                return new Inbound();
            case "OUTBOUND":
                return new Outbound();
            case "PRODUCTION":
                return new Production();

            default:
                throw new UnsupportedOperationException("No inventory status configured for " + inventoryStatusString);
        }
    }


}
