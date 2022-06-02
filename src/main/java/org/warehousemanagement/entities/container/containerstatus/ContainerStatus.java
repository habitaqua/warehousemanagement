package org.warehousemanagement.entities.container.containerstatus;

import java.util.List;
import java.util.Set;

public interface ContainerStatus {


    List<ContainerStatus> previousStates();

    String getStatus();
}
