package org.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Outbounded implements InventoryStatus {

    @Override public Set<InventoryStatus> previousStates() {
        return ImmutableSet.of(new Inbounded());
    }

    @Override public String getStatus() {
        return "fulfilled";
    }
}
