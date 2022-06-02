package org.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

public class Available implements ContainerStatus {
    @Override
    public List<ContainerStatus> previousStates() {
        return ImmutableList.of(new Discontinued(), new Filled(), new PartiallyFilled());
    }

    @Override
    public String getStatus() {
        return "AVAILABLE";
    }
}
