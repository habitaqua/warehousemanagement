package org.warehousenamagement.entities.inventorystatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Fulfilled implements InventoryStatus {

    @Override public Set<InventoryStatus> previousStates() {
        return ImmutableSet.of(new Available());
    }

    @Override public String getStatus() {
        return "fulfilled";
    }
}
