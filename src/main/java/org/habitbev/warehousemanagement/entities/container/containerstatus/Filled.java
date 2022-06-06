package org.habitbev.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;

import java.util.List;

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
