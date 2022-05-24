package org.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Available implements ContainerStatus {
    @Override
    public Set<ContainerStatus> previousStates() {
        return ImmutableSet.of(new Discontinued(), new Filled(), new PartiallyFilled());
    }

    @Override
    public String getStatus() {
        return "AVAILABLE";
    }
}
