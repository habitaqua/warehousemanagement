package org.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Outbound implements InventoryStatus {

    @Override public Set<InventoryStatus> previousStates() {
        return ImmutableSet.of(new Inbound());
    }

    @Override public String getStatus() {
        return "OUTBOUND";
    }
}
