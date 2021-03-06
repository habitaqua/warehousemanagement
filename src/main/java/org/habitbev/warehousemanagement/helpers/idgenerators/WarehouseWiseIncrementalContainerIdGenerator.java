package org.habitbev.warehousemanagement.helpers.idgenerators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.ContainerDAO;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.container.AddContainerRequest;

import java.util.Optional;

/**
 * generates location id incrementally location wise
 */
public class WarehouseWiseIncrementalContainerIdGenerator implements ContainerIdGenerator<AddContainerRequest> {
    private static final String CONTAINER = "CONTAINER-";
    private static final String FIRST_CONTAINER_ID = CONTAINER + 1;
    ContainerDAO containerDAO;

    @Inject
    public WarehouseWiseIncrementalContainerIdGenerator(@Named("dynamoDbImpl") ContainerDAO containerDAO) {
        this.containerDAO = containerDAO;
    }

    @Override
    public String generate(AddContainerRequest addContainerRequest) {

        Preconditions.checkArgument(addContainerRequest != null,
                "warehouseWiseIncrementalContainerIdGenerator.input cannot be null");
        String warehouseId = addContainerRequest.getWarehouseId();
        Optional<ContainerDTO> lastAddedContainer = containerDAO.getLastAddedContainer(warehouseId);
        if (lastAddedContainer.isPresent()) {
            String containerId = lastAddedContainer.get().getContainerId();
            Integer number = Integer.valueOf(containerId.split(CONTAINER)[1]);
            return CONTAINER + (number + 1);
        }
        return FIRST_CONTAINER_ID;
    }
}
