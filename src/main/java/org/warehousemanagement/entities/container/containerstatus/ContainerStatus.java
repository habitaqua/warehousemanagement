package org.warehousemanagement.entities.container.containerstatus;

import java.util.Set;

public interface ContainerStatus {


    Set<ContainerStatus> previousStates();

    String getStatus();
}
