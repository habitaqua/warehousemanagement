package org.habitbev.warehousemanagement.helpers;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.habitbev.warehousemanagement.entities.container.containerstatus.Available;
import org.habitbev.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.habitbev.warehousemanagement.entities.container.containerstatus.Filled;
import org.habitbev.warehousemanagement.entities.container.containerstatus.PartiallyFilled;

@Singleton
public class ContainerStatusDeterminer {

    public ContainerStatus determineStatus(Integer newCapacity, Integer maxCapacity) {
        Preconditions.checkArgument(newCapacity!=null && newCapacity >=0, "newCapacity cannot be out of bounds");
        Preconditions.checkArgument(maxCapacity!=null && maxCapacity >=0, "nmaxCapacity cannot be out of bounds");

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
