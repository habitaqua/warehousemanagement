package org.habitbev.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableList;

import java.util.List;

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
