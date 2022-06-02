package org.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;
import org.warehousemanagement.entities.inventory.inventorystatus.Inbound;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;
import org.warehousemanagement.entities.inventory.inventorystatus.Outbound;
import org.warehousemanagement.entities.inventory.inventorystatus.Production;

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
