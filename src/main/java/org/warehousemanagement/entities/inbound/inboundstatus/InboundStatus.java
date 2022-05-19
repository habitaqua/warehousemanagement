package org.warehousemanagement.entities.inbound.inboundstatus;

import org.warehousemanagement.entities.dynamodb.Inbound;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

import java.util.Set;

public interface InboundStatus {


    Set<InboundStatus> previousStates();

    String getStatus();
}
