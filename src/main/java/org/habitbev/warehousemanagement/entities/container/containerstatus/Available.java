package org.habitbev.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Available implements ContainerStatus {
    @Override
    public List<ContainerStatus> previousStates() {
        return ImmutableList.of(new Discontinued(), new Filled(), new PartiallyFilled());
    }

    @Override
    public String toString() {
        return "AVAILABLE";
    }
}
