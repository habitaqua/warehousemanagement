package org.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Discontinued implements ContainerStatus {
    @Override
    public Set<ContainerStatus> previousStates() {
        return ImmutableSet.of(new Available());
    }

    @Override
    public String getStatus() {
        return "DISCONTINUED";
    }
}