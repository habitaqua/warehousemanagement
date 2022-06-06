package org.habitbev.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Discontinued implements ContainerStatus {
    @Override
    public List<ContainerStatus> previousStates() {
        return ImmutableList.of(new Available());
    }

    @Override
    public String toString() {
        return "DISCONTINUED";
    }
}