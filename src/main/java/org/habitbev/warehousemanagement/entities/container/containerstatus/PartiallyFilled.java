package org.habitbev.warehousemanagement.entities.container.containerstatus;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;

import java.util.List;
@EqualsAndHashCode
public class PartiallyFilled implements ContainerStatus {
    @Override
    public List<ContainerStatus> previousStates() {
        return ImmutableList.of(new Available(), new Filled(), new PartiallyFilled());
    }

    @Override
    public String toString() {
        return "PARTIALLY_FILLED";
    }
}