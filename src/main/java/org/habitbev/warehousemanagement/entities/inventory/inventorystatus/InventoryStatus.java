package org.habitbev.warehousemanagement.entities.inventory.inventorystatus;

import java.util.List;

public interface InventoryStatus {

    List<InventoryStatus> previousStates();

    String getStatus();
}
