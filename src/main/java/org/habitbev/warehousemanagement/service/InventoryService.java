package org.habitbev.warehousemanagement.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.habitbev.warehousemanagement.dao.InventoryDAO;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.habitbev.warehousemanagement.entities.inventory.InventoryInboundRequest;
import org.habitbev.warehousemanagement.entities.inventory.InventoryOutboundRequest;
import org.habitbev.warehousemanagement.entities.inventory.MoveInventoryRequest;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Production;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class InventoryService {

    public static final int ADD_SUBLIST_SIZE = 25;

    public static final int INBOUND_SUBLIST_SIZE = 24;
    public static final int FULFILL_SUBLIST_SIZE = 24;
    public static final int MOVE_SUBLIST_SIZE = 23;

    InventoryDAO inventoryDAO;
    ExecutorService executorService;
    ContainerService containerService;


    @Inject
    public InventoryService(InventoryDAO inventoryDAO, ContainerService containerService, ExecutorService executorService) {
        this.inventoryDAO = inventoryDAO;
        this.containerService = containerService;
        this.executorService = executorService;
    }


    public List<String> add(InventoryAddRequest addRequest) {

        try {
            List<List<String>> uniqueProductIdsSubList = Lists.partition(addRequest.getUniqueProductIds(), ADD_SUBLIST_SIZE);
            List<InventoryAddRequest> partitionedInventoryInventoryAddRequests = uniqueProductIdsSubList.stream().map(subIds -> InventoryAddRequest.builder().uniqueProductIds(subIds)
                    .inventoryStatus(new Production()).productionTime(addRequest.getProductionTime())
                    .skuCategory(addRequest.getSkuCategory()).skuCode(addRequest.getSkuCode())
                    .skuType(addRequest.getSkuType()).warehouseId(addRequest.getWarehouseId())
                    .companyId(addRequest.getCompanyId()).build()).collect(Collectors.toList());
            List<CompletableFuture<List<String>>> completableFutures = partitionedInventoryInventoryAddRequests.stream()
                    .map(inventoryAddRequest -> CompletableFuture.supplyAsync(() -> inventoryDAO.add(inventoryAddRequest), executorService)).collect(Collectors.toList());
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
            List<String> successfulProductIds = new ArrayList<>();
            for (CompletableFuture<List<String>> completableFuture : completableFutures) {
                successfulProductIds.addAll(completableFuture.get());
            }
            return successfulProductIds;
        } catch (InterruptedException e) {
            throw new RetriableException(e);
        } catch (ExecutionException e) {
            throw new RetriableException(e);
        }

    }

    //TODO USING SEQUENTIAL . CHECK PERF AND DECIDE MULTI THREADING
    public void inbound(InventoryInboundRequest inventoryInboundRequest) {

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
        List<List<String>> uniqueProductIdsSubList = Lists.partition(inventoryInboundRequest.getUniqueProductIds(), INBOUND_SUBLIST_SIZE);
        List<InventoryInboundRequest> partitionedInventoryInboundRequests = uniqueProductIdsSubList.stream().map(list -> InventoryInboundRequest.builder().uniqueProductIds(list)
                .inventoryStatus(inventoryInboundRequest.getInventoryStatus()).containerId(containerId)
                .warehouseId(warehouseId).containerMaxCapacity(maxCapacity).inboundId(inventoryInboundRequest.getInboundId())
                .skuCode(inventoryInboundRequest.getSkuCode()).build()).collect(Collectors.toList());
        partitionedInventoryInboundRequests.forEach(partitionedInventoryInboundRequest -> inventoryDAO.inbound(partitionedInventoryInboundRequest));
    }

    public void outbound(InventoryOutboundRequest inventoryOutboundRequest) {

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

    public void move(MoveInventoryRequest moveInventoryRequest) {
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
