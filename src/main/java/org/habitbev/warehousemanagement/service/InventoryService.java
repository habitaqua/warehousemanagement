package org.habitbev.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.InventoryDAO;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.habitbev.warehousemanagement.entities.inventory.*;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Production;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseActionValidatorChain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.habitbev.warehousemanagement.helpers.validators.WarehouseAction.INVENTORY_INBOUND;
import static org.habitbev.warehousemanagement.helpers.validators.WarehouseAction.INVENTORY_OUTBOUND;

public class InventoryService {

    public static final int ADD_SUBLIST_SIZE = 25;

    public static final int INBOUND_SUBLIST_SIZE = 24;
    public static final int FULFILL_SUBLIST_SIZE = 24;
    public static final int MOVE_SUBLIST_SIZE = 23;

    InventoryDAO inventoryDAO;
    ExecutorService addInventoryExecutorService;
    ContainerService containerService;
    InboundService inboundService;
    OutboundService outboundService;

    WarehouseActionValidatorChain warehouseActionValidatorChain;


    @Inject
    public InventoryService(@Named("dynamoDbImpl") InventoryDAO inventoryDAO, ContainerService containerService,
                            InboundService inboundService, OutboundService outboundService,
                            WarehouseActionValidatorChain warehouseActionValidatorChain,
                            @Named("addInventoryExecutorService") ExecutorService addInventoryExecutorService) {
        this.inventoryDAO = inventoryDAO;
        this.containerService = containerService;
        this.addInventoryExecutorService = addInventoryExecutorService;
        this.inboundService = inboundService;
        this.outboundService = outboundService;
        this.warehouseActionValidatorChain = warehouseActionValidatorChain;
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
                    .map(inventoryAddRequest -> CompletableFuture.supplyAsync(() -> inventoryDAO.add(inventoryAddRequest), addInventoryExecutorService)).collect(Collectors.toList());
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
    public void inbound(InventoryInboundRequest inboundReq) {

        Preconditions.checkArgument(inboundReq != null, "inboundReq cannot be null");

        List<String> uniqueProductIdsToInbound = inboundReq.getUniqueProductIds();
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(INVENTORY_INBOUND)
                .inboundId(inboundReq.getInboundId()).capacityToInbound(uniqueProductIdsToInbound.size())
                .companyId(inboundReq.getCompanyId()).containerId(inboundReq.getContainerId()).skuCode(inboundReq.getSkuCode()).build();
        WarehouseValidatedEntities warehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        ContainerDTO validatedContainerDTO = warehouseValidatedEntities.getContainerDTO();
        String containerId = validatedContainerDTO.getContainerId();
        String warehouseId = validatedContainerDTO.getWarehouseId();
        String inboundId = warehouseValidatedEntities.getFgInboundDTO().getInboundId();
        Integer maxCapacity = validatedContainerDTO.getSkuCodeWisePredefinedCapacity().get(inboundReq.getSkuCode());

        List<List<String>> uniqueProductIdsSubList = Lists.partition(uniqueProductIdsToInbound, INBOUND_SUBLIST_SIZE);
        List<InventoryInboundRequest> partitionedInventoryInboundRequests = uniqueProductIdsSubList.stream().map(list -> InventoryInboundRequest.builder().uniqueProductIds(list)
                .inventoryStatus(inboundReq.getInventoryStatus()).containerId(containerId)
                .warehouseId(warehouseId).containerMaxCapacity(maxCapacity).inboundId(inboundId)
                .skuCode(warehouseValidatedEntities.getSku().getSkuCode()).build()).collect(Collectors.toList());
        partitionedInventoryInboundRequests.forEach(partitionedInventoryInboundRequest -> inventoryDAO.inbound(partitionedInventoryInboundRequest));
    }

    public void outbound(InventoryOutboundRequest outboundReq) {

        Preconditions.checkArgument(outboundReq != null, "outboundReq cannot be null");

        List<String> uniqueProductIdsToOutbound = outboundReq.getUniqueProductIds();
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(INVENTORY_OUTBOUND)
                .outboundId(outboundReq.getOutboundId()).capacityToOutbound(uniqueProductIdsToOutbound.size()).orderId(outboundReq.getOrderId())
                .companyId(outboundReq.getCompanyId()).containerId(outboundReq.getContainerId()).skuCode(outboundReq.getSkuCode()).build();
        WarehouseValidatedEntities warehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        ContainerDTO validatedContainerDTO = warehouseValidatedEntities.getContainerDTO();
        String containerId = validatedContainerDTO.getContainerId();
        Integer maxCapacity = validatedContainerDTO.getSkuCodeWisePredefinedCapacity().get(warehouseValidatedEntities.getSku().getSkuCode());
        String warehouseId = validatedContainerDTO.getWarehouseId();

        List<List<String>> uniqueProductIdsSubList = Lists.partition(uniqueProductIdsToOutbound, FULFILL_SUBLIST_SIZE);
        List<InventoryOutboundRequest> partitionedInventoryOutboundRequests = uniqueProductIdsSubList.stream().map(list -> InventoryOutboundRequest.builder()
                .uniqueProductIds(list)
                .inventoryStatus(outboundReq.getInventoryStatus()).containerId(containerId)
                .warehouseId(warehouseId).containerMaxCapacity(maxCapacity).outboundId(outboundReq.getOutboundId())
                .orderId(outboundReq.getOrderId()).skuCode(outboundReq.getSkuCode()).build()).collect(Collectors.toList());
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
