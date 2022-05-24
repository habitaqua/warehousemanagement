package org.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Inbound implements InventoryStatus {

    @Override public Set<InventoryStatus> previousStates() {
        return ImmutableSet.of();
    }

    @Override public String getStatus() {
        return "INBOUND";
    }
}
