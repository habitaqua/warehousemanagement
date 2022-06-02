package org.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

public class Production implements InventoryStatus{
    @Override
    public List<InventoryStatus> previousStates() {
        return ImmutableList.of(new Production());
    }

    @Override
    public String getStatus() {
        return "PRODUCTION";
    }
}
