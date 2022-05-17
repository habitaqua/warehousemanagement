package org.warehousenamagement.entities.inventorystatus;

import java.util.Set;

public interface InventoryStatus {

    Set<InventoryStatus> previousStates();

    String getStatus();
}
