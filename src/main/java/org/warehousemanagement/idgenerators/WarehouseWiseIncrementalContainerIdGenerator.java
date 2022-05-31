package org.warehousemanagement.idgenerators;

import com.google.inject.Inject;
import org.warehousemanagement.entities.container.ContainerDTO;
import org.warehousemanagement.entities.dynamodb.Container;
import org.warehousemanagement.entities.container.AddContainerRequest;
import org.warehousemanagement.dao.ContainerDynamoDAOImpl;

import java.util.Optional;

/**
 * generates location id incrementally location wise
 */
public class WarehouseWiseIncrementalContainerIdGenerator implements ContainerIdGenerator<AddContainerRequest> {
    private static final String CONTAINER = "CONTAINER_";
    private static final String FIRST_CONTAINER_ID = CONTAINER + 1;
    ContainerDynamoDAOImpl containerDynamoDAOImpl;

    @Inject
    public WarehouseWiseIncrementalContainerIdGenerator(ContainerDynamoDAOImpl containerDynamoDAOImpl) {
        this.containerDynamoDAOImpl = containerDynamoDAOImpl;
    }

    @Override
    public String generate(AddContainerRequest addContainerRequest) {
        String warehouseId = addContainerRequest.getWarehouseId();
        Optional<ContainerDTO> lastAddedContainer = containerDynamoDAOImpl.getLastAddedContainer(warehouseId);
        if(lastAddedContainer.isPresent())
        {
            String containerId = lastAddedContainer.get().getContainerId();
            Integer number = Integer.valueOf(containerId.split(CONTAINER)[1]);
            return CONTAINER + (number + 1);
        }
        return FIRST_CONTAINER_ID;
    }
}
