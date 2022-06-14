package org.habitbev.warehousemanagement.entities.inventory.inventorystatus;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class Inbound implements InventoryStatus {

    @Override public List<InventoryStatus> previousStates() {
        return ImmutableList.of(new Production());
    }

    @Override public String getStatus() {
        return "INBOUND";
    }
}
