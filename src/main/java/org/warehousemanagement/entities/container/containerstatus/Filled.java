package org.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

public class Filled  implements ContainerStatus {
    @Override
    public List<ContainerStatus> previousStates() {
        return ImmutableList.of(new PartiallyFilled(), new Available());
    }

    @Override
    public String getStatus() {
        return "FILLED";
    }
}
