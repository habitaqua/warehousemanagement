package org.warehousemanagement.helpers;

import com.google.inject.Singleton;
import org.warehousemanagement.entities.container.containerstatus.Available;
import org.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.warehousemanagement.entities.container.containerstatus.Filled;
import org.warehousemanagement.entities.container.containerstatus.PartiallyFilled;

@Singleton
public class ContainerStatusDeterminer {

    public ContainerStatus determineStatus(int newCapacity, int maxCapacity) {

        ContainerStatus newContainerStatus = new Available();
        if (newCapacity == 0) {
            newContainerStatus = new Available();
        } else if (newCapacity == maxCapacity) {
            newContainerStatus = new Filled();
        } else if (newCapacity < maxCapacity) {
            newContainerStatus = new PartiallyFilled();
        }
        return newContainerStatus;
    }
}
