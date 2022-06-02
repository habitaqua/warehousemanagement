package org.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class PartiallyFilled implements ContainerStatus {
    @Override
    public List<ContainerStatus> previousStates() {
        return ImmutableList.of(new Available(), new Filled(), new PartiallyFilled());
    }

    @Override
    public String getStatus() {
        return "PARTIALLY_FILLED";
    }
}