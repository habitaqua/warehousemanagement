package org.warehousemanagement.service;

import com.google.inject.Inject;
import org.warehousemanagement.dao.ContainerDAO;
import org.warehousemanagement.entities.PaginatedResponse;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.container.AddContainerRequest;
import org.warehousemanagement.entities.container.GetContainerRequest;
import org.warehousemanagement.entities.container.GetContainersRequest;
import org.warehousemanagement.entities.container.ContainerDTO;
import org.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.warehousemanagement.helpers.idgenerators.ContainerIdGenerator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY = 5;
    ContainerDAO containerDAO;
    ContainerCapacityService containerCapacityService;
    ContainerIdGenerator<AddContainerRequest> containerIdGenerator;

    @Inject
    public ContainerService(ContainerDAO containerDAO, ContainerCapacityService containerCapacityService,
                            ContainerIdGenerator<AddContainerRequest> containerIdGenerator) {
        this.containerDAO = containerDAO;
        this.containerCapacityService = containerCapacityService;
        this.containerIdGenerator = containerIdGenerator;
    }

    public String add(AddContainerRequest addContainerRequest) {

        String warehouseId = addContainerRequest.getWarehouseId();
        synchronized (warehouseId) {
            String newContainerId = containerIdGenerator.generate(addContainerRequest);
            ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(newContainerId).warehouseId(warehouseId)
                    .predefinedCapacity(addContainerRequest.getSkuCodeWisePredefinedCapacity()).build();
            containerDAO.add(containerDTO);
            return newContainerId;
        }
    }

    public PaginatedResponse<ContainerDTO> getContainers(GetContainersRequest getContainersRequest) {
        PaginatedResponse<ContainerDTO> containers = containerDAO.getContainers(getContainersRequest);
        String warehouseId = getContainersRequest.getWarehouseId();
        List<ContainerDTO> enrichedContainers = containers.getItems().stream().map(containerDTO -> {
            Optional<ContainerCapacity> containerCapacity = containerCapacityService.get(warehouseId, containerDTO.getContainerId());
            return new ContainerDTO.Builder()
                    .currentCapacityDetails(containerCapacity.get()).containerId(containerDTO.getContainerId())
                    .warehouseId(warehouseId).predefinedCapacity(containerDTO.getSkuCodeWisePredefinedCapacity()).build();
        }).collect(Collectors.toList());
        PaginatedResponse<ContainerDTO> enrichedData = PaginatedResponse.<ContainerDTO>builder().items(enrichedContainers).nextPageToken(containers.getNextPageToken()).build();
        return enrichedData;
    }

    public ContainerDTO getContainer(GetContainerRequest getContainerRequest) {
        Optional<ContainerDTO> containerOp = containerDAO.getContainer(getContainerRequest);
        if (!containerOp.isPresent()) {
            throw new ResourceNotAvailableException("given container id not found at the warehouse");
        }
        ContainerDTO containerDTO = containerOp.get();
        String warehouseId = containerDTO.getWarehouseId();
        String containerId = containerDTO.getContainerId();
        Optional<ContainerCapacity> containerCapacityOp = containerCapacityService.get(warehouseId, containerId);
        if (containerCapacityOp.isPresent()) {
            return new ContainerDTO.Builder()
                    .currentCapacityDetails(containerCapacityOp.get()).containerId(containerId)
                    .warehouseId(warehouseId).predefinedCapacity(containerDTO.getSkuCodeWisePredefinedCapacity()).build();
        }
        return containerDTO;
    }
}
