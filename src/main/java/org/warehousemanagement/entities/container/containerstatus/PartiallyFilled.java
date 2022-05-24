package org.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class PartiallyFilled implements ContainerStatus {
    @Override
    public Set<ContainerStatus> previousStates() {
        return ImmutableSet.of(new Available(), new Filled());
    }

    @Override
    public String getStatus() {
        return "PARTIALLY_FILLED";
    }
}