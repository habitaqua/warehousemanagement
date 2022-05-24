package org.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Filled  implements ContainerStatus {
    @Override
    public Set<ContainerStatus> previousStates() {
        return ImmutableSet.of(new PartiallyFilled());
    }

    @Override
    public String getStatus() {
        return "FILLED";
    }
}
