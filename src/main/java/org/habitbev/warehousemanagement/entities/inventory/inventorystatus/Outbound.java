package org.habitbev.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Outbound implements InventoryStatus {

    @Override public List<InventoryStatus> previousStates() {
        return ImmutableList.of(new Inbound());
    }

    @Override public String getStatus() {
        return "OUTBOUND";
    }
}
