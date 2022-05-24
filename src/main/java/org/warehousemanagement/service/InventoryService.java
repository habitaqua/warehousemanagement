package org.warehousemanagement.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.jcabi.aspects.RetryOnFailure;
import org.warehousemanagement.dao.InventoryDAO;
import org.warehousemanagement.entities.container.ContainerDTO;
import org.warehousemanagement.entities.container.GetContainerRequest;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.RetriableException;
import org.warehousemanagement.entities.inventory.AddInventoryRequest;
import org.warehousemanagement.entities.inventory.FulfillInventoryRequest;
import org.warehousemanagement.entities.inventory.MoveInventoryRequest;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY = 5;
    public static final int ADD_SUBLIST_SIZE = 24;
    public static final int FULFILL_SUBLIST_SIZE = 24;
    public static final int MOVE_SUBLIST_SIZE = 23;

    InventoryDAO inventoryDAO;
    ContainerService containerService;


    @Inject
    public InventoryService(InventoryDAO inventoryDAO, ContainerService containerService) {
        this.inventoryDAO = inventoryDAO;
        this.containerService = containerService;
    }


    //TODO USING SEQUENTIAL . CHECK PERF AND DECIDE MULTI THREADING
    @RetryOnFailure(attempts = RETRY_ATTEMPTS, delay = RETRY_DELAY, types = RetriableException.class)
    public void add(AddInventoryRequest addInventoryRequest) {

        String containerId = addInventoryRequest.getContainerId();
        String warehouseId = addInventoryRequest.getWarehouseId();
        ContainerDTO containerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(containerId)
                .warehouseId(warehouseId).build());
        int existingCapacity = containerDTO.getCurrentCapacity();
        int deltaCapacity = addInventoryRequest.getUniqueProductIds().size();
        int maxCapacity = containerDTO.getSkuCodeWisePredefinedCapacity().get(addInventoryRequest.getSkuCode());
        if (existingCapacity + deltaCapacity > maxCapacity) {
            throw new NonRetriableException("Container max capacity reached");
        }
        List<List<String>> uniqueProductIdsSubList = Lists.partition(addInventoryRequest.getUniqueProductIds(), ADD_SUBLIST_SIZE);
        List<AddInventoryRequest> partitionedAddInventoryRequests = uniqueProductIdsSubList.stream().map(list -> AddInventoryRequest.builder().uniqueProductIds(list)
                .inventoryStatus(addInventoryRequest.getInventoryStatus()).containerId(containerId)
                .warehouseId(warehouseId).containerMaxCapacity(maxCapacity).inboundId(addInventoryRequest.getInboundId())
                .skuCode(addInventoryRequest.getSkuCode()).build()).collect(Collectors.toList());
        partitionedAddInventoryRequests.forEach(partitionedAddInventoryRequest -> inventoryDAO.add(partitionedAddInventoryRequest));
    }

    @RetryOnFailure(attempts = RETRY_ATTEMPTS, delay = RETRY_DELAY, types = RetriableException.class)
    public void fulfill(FulfillInventoryRequest fulfillInventoryRequest) {

        String containerId = fulfillInventoryRequest.getContainerId();
        String warehouseId = fulfillInventoryRequest.getWarehouseId();
        ContainerDTO containerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(containerId)
                .warehouseId(warehouseId).build());
        int existingCapacity = containerDTO.getCurrentCapacity();
        int deltaCapacity = fulfillInventoryRequest.getUniqueProductIds().size();
        int maxCapacity = containerDTO.getSkuCodeWisePredefinedCapacity().get(fulfillInventoryRequest.getSkuCode());
        if (existingCapacity - deltaCapacity < 0) {
            throw new NonRetriableException("fulfill exceeded container capacity");
        }
        List<List<String>> uniqueProductIdsSubList = Lists.partition(fulfillInventoryRequest.getUniqueProductIds(), FULFILL_SUBLIST_SIZE);
        List<FulfillInventoryRequest> partitionedFulfillInventoryRequests = uniqueProductIdsSubList.stream().map(list -> FulfillInventoryRequest.builder()
                .uniqueProductIds(list)
                .inventoryStatus(fulfillInventoryRequest.getInventoryStatus()).containerId(containerId)
                .warehouseId(warehouseId).containerMaxCapacity(maxCapacity).outboundId(fulfillInventoryRequest.getOutboundId())
                .orderId(fulfillInventoryRequest.getOrderId()).skuCode(fulfillInventoryRequest.getSkuCode()).build()).collect(Collectors.toList());
        partitionedFulfillInventoryRequests.forEach(partitionedFulfillInventoryRequest -> inventoryDAO.fulfill(partitionedFulfillInventoryRequest));
    }
    public void moveInventory(MoveInventoryRequest moveInventoryRequest) {
        String sourceContainerId = moveInventoryRequest.getSourceContainerId();
        String destinationContainerId = moveInventoryRequest.getDestinationContainerId();
        String warehouseId = moveInventoryRequest.getWarehouseId();
        ContainerDTO sourceContainerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(sourceContainerId)
                .warehouseId(warehouseId).build());
        ContainerDTO destinationContainerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(destinationContainerId)
                .warehouseId(warehouseId).build());

        int sourceExistingCapacity = sourceContainerDTO.getCurrentCapacity();
        Integer sourceMaxCapacity = sourceContainerDTO.getSkuCodeWisePredefinedCapacity().get(moveInventoryRequest.getSkuCode());
        int deltaCapacity = moveInventoryRequest.getUniqueProductIds().size();
        int destinationExistingCapacity = destinationContainerDTO.getCurrentCapacity();
        Integer destinationMaxCapacity = destinationContainerDTO.getSkuCodeWisePredefinedCapacity().get(moveInventoryRequest.getSkuCode());

        if (sourceExistingCapacity - deltaCapacity < 0) {
            throw new NonRetriableException("tryinng to move more than source container capacity");
        }

        if (destinationExistingCapacity + deltaCapacity > destinationMaxCapacity) {
            throw new NonRetriableException("trying to move more than destination container capacity");
        }
        List<List<String>> uniqueProductIdsSubList = Lists.partition(moveInventoryRequest.getUniqueProductIds(), MOVE_SUBLIST_SIZE);
        List<MoveInventoryRequest> partitionedMoveInventoryRequests = uniqueProductIdsSubList.stream().map(list -> MoveInventoryRequest.builder()
                .uniqueProductIds(list).sourceContainerMaxCapacity(sourceMaxCapacity).skuCode(moveInventoryRequest.getSkuCode())
                .sourceContainerId(sourceContainerId).destinationContainerId(destinationContainerId).destinationContainerMaxCapacity(destinationMaxCapacity)
                .warehouseId(warehouseId).build()).collect(Collectors.toList());
        partitionedMoveInventoryRequests.forEach(partitionedMoveInventoryRequest -> inventoryDAO.move(partitionedMoveInventoryRequest));

    }
}
