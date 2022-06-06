package org.habitbev.warehousemanagement.entities.container.containerstatus;

import java.util.List;

public interface ContainerStatus {


    List<ContainerStatus> previousStates();

    String toString();
}
