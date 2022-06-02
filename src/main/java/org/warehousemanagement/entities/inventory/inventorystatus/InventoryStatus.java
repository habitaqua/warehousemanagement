package org.warehousemanagement.entities.inventory.inventorystatus;

import java.util.List;
import java.util.Set;

public interface InventoryStatus {

    List<InventoryStatus> previousStates();

    String getStatus();
}
