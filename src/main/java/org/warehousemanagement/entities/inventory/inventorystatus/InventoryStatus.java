package org.warehousemanagement.entities.inventory.inventorystatus;

import java.util.Set;

public interface InventoryStatus {

    Set<InventoryStatus> previousStates();

    String getStatus();
}
