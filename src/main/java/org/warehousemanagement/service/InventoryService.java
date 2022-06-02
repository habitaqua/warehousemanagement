package org.warehousemanagement.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.warehousemanagement.dao.InventoryDAO;
import org.warehousemanagement.entities.container.ContainerDTO;
import org.warehousemanagement.entities.container.GetContainerRequest;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.inventory.InventoryInboundRequest;
import org.warehousemanagement.entities.inventory.InventoryOutboundRequest;
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
    public void add(InventoryInboundRequest inventoryInboundRequest) {

        String containerId = inventoryInboundRequest.getContainerId();
        String warehouseId = inventoryInboundRequest.getWarehouseId();
        ContainerDTO containerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(containerId)
                .warehouseId(warehouseId).build());
        int existingCapacity = containerDTO.getCurrentCapacity();
        int deltaCapacity = inventoryInboundRequest.getUniqueProductIds().size();
        int maxCapacity = containerDTO.getSkuCodeWisePredefinedCapacity().get(inventoryInboundRequest.getSkuCode());
        if (existingCapacity + deltaCapacity > maxCapacity) {
            throw new NonRetriableException("Container max capacity reached");
        }
        List<List<String>> uniqueProductIdsSubList = Lists.partition(inventoryInboundRequest.getUniqueProductIds(), ADD_SUBLIST_SIZE);
        List<InventoryInboundRequest> partitionedInventoryInboundRequests = uniqueProductIdsSubList.stream().map(list -> InventoryInboundRequest.builder().uniqueProductIds(list)
                .inventoryStatus(inventoryInboundRequest.getInventoryStatus()).containerId(containerId)
                .warehouseId(warehouseId).containerMaxCapacity(maxCapacity).inboundId(inventoryInboundRequest.getInboundId())
                .skuCode(inventoryInboundRequest.getSkuCode()).build()).collect(Collectors.toList());
        partitionedInventoryInboundRequests.forEach(partitionedInventoryInboundRequest -> inventoryDAO.inbound(partitionedInventoryInboundRequest));
    }

    public void fulfill(InventoryOutboundRequest inventoryOutboundRequest) {

        String containerId = inventoryOutboundRequest.getContainerId();
        String warehouseId = inventoryOutboundRequest.getWarehouseId();
        ContainerDTO containerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(containerId)
                .warehouseId(warehouseId).build());
        int existingCapacity = containerDTO.getCurrentCapacity();
        int deltaCapacity = inventoryOutboundRequest.getUniqueProductIds().size();
        int maxCapacity = containerDTO.getSkuCodeWisePredefinedCapacity().get(inventoryOutboundRequest.getSkuCode());
        if (existingCapacity - deltaCapacity < 0) {
            throw new NonRetriableException("fulfill exceeded container capacity");
        }
        List<List<String>> uniqueProductIdsSubList = Lists.partition(inventoryOutboundRequest.getUniqueProductIds(), FULFILL_SUBLIST_SIZE);
        List<InventoryOutboundRequest> partitionedInventoryOutboundRequests = uniqueProductIdsSubList.stream().map(list -> InventoryOutboundRequest.builder()
                .uniqueProductIds(list)
                .inventoryStatus(inventoryOutboundRequest.getInventoryStatus()).containerId(containerId)
                .warehouseId(warehouseId).containerMaxCapacity(maxCapacity).outboundId(inventoryOutboundRequest.getOutboundId())
                .orderId(inventoryOutboundRequest.getOrderId()).skuCode(inventoryOutboundRequest.getSkuCode()).build()).collect(Collectors.toList());
        partitionedInventoryOutboundRequests.forEach(partitionedInventoryOutboundRequest -> inventoryDAO.outbound(partitionedInventoryOutboundRequest));
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
