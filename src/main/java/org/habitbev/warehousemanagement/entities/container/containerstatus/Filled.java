package org.habitbev.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;

import java.util.List;
@EqualsAndHashCode
public class Filled  implements ContainerStatus {
    @Override
    public List<ContainerStatus> previousStates() {
        return ImmutableList.of(new PartiallyFilled(), new Available());
    }

    @Override
    public String toString() {
        return "FILLED";
    }
}
