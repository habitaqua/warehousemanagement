package org.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

public class Outbound implements InventoryStatus {

    @Override public List<InventoryStatus> previousStates() {
        return ImmutableList.of(new Inbound());
    }

    @Override public String getStatus() {
        return "OUTBOUND";
    }
}
