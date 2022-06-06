package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.container.containerstatus.Available;
import org.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.warehousemanagement.entities.container.containerstatus.Filled;
import org.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.dynamodb.Inventory;
import org.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.warehousemanagement.entities.exceptions.RetriableException;
import org.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.warehousemanagement.entities.inventory.InventoryInboundRequest;
import org.warehousemanagement.entities.inventory.InventoryOutboundRequest;
import org.warehousemanagement.entities.inventory.MoveInventoryRequest;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;
import org.warehousemanagement.entities.inventory.inventorystatus.Production;
import org.warehousemanagement.helpers.ContainerStatusDeterminer;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
public class InventoryDynamoDAOImpl implements InventoryDAO {

    private static final String DELIMITER = "<%>";
    private static final String INVENTORY_TABLE_NAME = "inventory";
    private static final String CONTAINER_CAPACITY_TABLE_NAME = "container-capacity";
    private static final String COMMA = ",";
    AmazonDynamoDBClient amazonDynamoDBClient;
    DynamoDBMapper inventoryDynamoDbMapper;
    ContainerCapacityDAO containerCapacityDAO;

    ContainerStatusDeterminer containerStatusDeterminer;
    Clock clock;

    @Inject
    public InventoryDynamoDAOImpl(AmazonDynamoDBClient amazonDynamoDBClient, DynamoDBMapper inventoryDynamoDbMapper,
                                  ContainerStatusDeterminer containerStatusDeterminer, ContainerCapacityDAO containerCapacityDAO,
                                  Clock clock) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.inventoryDynamoDbMapper = inventoryDynamoDbMapper;
        this.containerCapacityDAO = containerCapacityDAO;
        this.containerStatusDeterminer = containerStatusDeterminer;
        this.clock = clock;
    }


    @Override
    public List<String> add(InventoryAddRequest addRequest) {

        try {
            Preconditions.checkArgument(addRequest != null, "inventoryAddRequest cannot be null");
            List<String> uniqueProductIds = addRequest.getUniqueProductIds();
            List<KeyPair> keyPairsToLoad = uniqueProductIds.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());
            Map<String, List<Object>> inventoryObjects = inventoryDynamoDbMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
            if (!inventoryObjects.get(INVENTORY_TABLE_NAME).isEmpty()) {
                String message = String.format("Some or All of given ids %s already exist for companyId %s and warehouse Id %s", StringUtils.join(uniqueProductIds, COMMA),
                        addRequest.getCompanyId(), addRequest.getWarehouseId());
                throw new ResourceAlreadyExistsException(message);
            }
            long creationTime = clock.millis();
            String skuCategoryAndType = String.join(DELIMITER, addRequest.getSkuCategory(), addRequest.getSkuType());
            List<Inventory> newInventories = uniqueProductIds.stream().map(productId -> Inventory.builder().uniqueProductId(productId)
                    .companyId(addRequest.getCompanyId()).warehouseId(addRequest.getWarehouseId())
                    .inventoryStatus(new Production()).creationTime(creationTime).modifiedTime(creationTime)
                    .productionTime(addRequest.getProductionTime()).skuCode(addRequest.getSkuCode())
                    .skuCategoryType(skuCategoryAndType).build()).collect(toList());
            List<DynamoDBMapper.FailedBatch> failedBatches = inventoryDynamoDbMapper.batchSave(newInventories);
            if (!failedBatches.isEmpty()) {
                List<Inventory> failedItems = failedBatches.stream()
                        .map(failedBatch -> getFailedBatchListItems(failedBatch)).collect(toList()).stream().flatMap(Collection::stream)
                        .collect(toList());
                List<String> failedIds = failedItems.stream().map(failedItem -> failedItem.getUniqueProductId()).collect(toList());
                List<String> successfulProductIds = new ArrayList<>(uniqueProductIds);
                successfulProductIds.removeAll(failedIds);
                return successfulProductIds;
            }

            return uniqueProductIds;
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while saving ids ", e);
            throw new RetriableException(e);
        } catch (ResourceAlreadyExistsException re) {
            log.error(re.getMessage());
            throw re;
        } catch (Exception e) {
            log.error("Non Retriable Error occured while saving ids", e);
            throw new NonRetriableException(e);
        }
    }

    /**
     * idempotent for upi using InventoryStaus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param inboundRequest
     */
    public void inbound(InventoryInboundRequest inboundRequest) {
        try {
            Preconditions.checkArgument(inboundRequest != null, "inboundRequest cannot be null");

            String containerId = inboundRequest.getContainerId();
            String warehouseId = inboundRequest.getWarehouseId();

            List<String> uniqueProductIds = inboundRequest.getUniqueProductIds();
            synchronized (warehouseId + containerId) {
                int existingCapacity = containerCapacityDAO.getExistingQuantity(warehouseId, containerId);
                List<TransactWriteItem> transactWrites = uniqueProductIds.stream()
                        .map(itemId -> new TransactWriteItem().withUpdate(constructUpdateExpression(itemId, inboundRequest))).collect(toList());

                int newCapacity = existingCapacity + uniqueProductIds.size();
                Update updateCapacityExpression = constructUpdateContainerCapacityExpression(warehouseId, containerId, existingCapacity, newCapacity
                        , inboundRequest.getContainerMaxCapacity());

                transactWrites.add(new TransactWriteItem().withUpdate(updateCapacityExpression));


                TransactWriteItemsRequest inboundInventoryTransaction = new TransactWriteItemsRequest()
                        .withTransactItems(transactWrites)
                        .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);


                amazonDynamoDBClient.transactWriteItems(inboundInventoryTransaction);
            }
        } catch (TransactionCanceledException tce) {
            List<CancellationReason> cancellationReasons = tce.getCancellationReasons();
            log.error("transaction cancelled exception for inbounding ", inboundRequest.getInboundId(), " with cancellation reasons ",
                    StringUtils.join(cancellationReasons, COMMA));
            String message = String.format("Inconsistent state occurred in data layer, either of the following is true \n" +
                            "1) Container %s reached its capacity \n" +
                            "2) ProductIds are never generated \n" +
                            "3) ProductIds are not generated for the given companyId %s and warehouseId %s", inboundRequest.getContainerId(),
                    inboundRequest.getCompanyId(), inboundRequest.getWarehouseId());
            throw new InconsistentStateException(message, tce);
        } catch (InternalServerErrorException e) {
            throw new RetriableException("Exception occurred", e);
        } catch (Exception e) {
            throw new NonRetriableException("Exception occurred", e);
        }
    }


    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param outboundRequest
     */
    public void outbound(InventoryOutboundRequest outboundRequest) {
        try {
            Preconditions.checkArgument(outboundRequest!= null, "outboundRequest cannot be null");
            List<String> uniqueProductIds = outboundRequest.getUniqueProductIds();
            Preconditions.checkArgument(outboundRequest != null, "outboundRequest cannot be null");
            String containerId = outboundRequest.getContainerId();
            String warehouseId = outboundRequest.getWarehouseId();
            synchronized (warehouseId + containerId) {
                int existingQuantity = containerCapacityDAO.getExistingQuantity(warehouseId, containerId);

                int newQuantity = existingQuantity - uniqueProductIds.size();
                Update updateCapacityExpression = constructUpdateContainerCapacityExpression(warehouseId, containerId,
                        existingQuantity, newQuantity, outboundRequest.getContainerMaxCapacity());
                List<TransactWriteItem> transactWrites = uniqueProductIds.stream()
                        .map(itemId -> new TransactWriteItem().withUpdate(constructUpdateExpression(itemId, outboundRequest))).collect(toList());
                transactWrites.add(new TransactWriteItem().withUpdate(updateCapacityExpression));


                TransactWriteItemsRequest outboundInventoryTransaction = new TransactWriteItemsRequest()
                        .withTransactItems(transactWrites)
                        .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);


                amazonDynamoDBClient.transactWriteItems(outboundInventoryTransaction);

            }
        } catch (TransactionCanceledException tce) {
            List<CancellationReason> cancellationReasons = tce.getCancellationReasons();
            log.error("transaction cancelled exception for outbounding ", outboundRequest.getOutboundId(), " with cancellation reasons ",
                    StringUtils.join(cancellationReasons, COMMA));
            String message = String.format("Inconsistent state occurred in data layer, either of the following is true \n" +
                            "1) Container %s reached its minimal capacity \n" +
                            "2) ProductIds are never generated \n" +
                            "3) ProductIds are not generated for the given companyId %s and warehouseId %s", outboundRequest.getContainerId(),
                    outboundRequest.getCompanyId(), outboundRequest.getWarehouseId());
            throw new InconsistentStateException(message, tce);
        } catch (InternalServerErrorException e) {
            throw new RetriableException("Exception occurred", e);
        } catch (Exception e) {
            throw new NonRetriableException("Exception occurred", e);
        }
    }


    public void move(MoveInventoryRequest moveRequest) {
        try {
            Preconditions.checkArgument(moveRequest != null, "moveRequest cannot be null");
            List<String> uniqueProductIds = moveRequest.getUniqueProductIds();
            String destinationContainerId = moveRequest.getDestinationContainerId();
            String sourceContainerId = moveRequest.getSourceContainerId();
            String warehouseId = moveRequest.getWarehouseId();

            synchronized (warehouseId + sourceContainerId) {
                synchronized (warehouseId + destinationContainerId) {

                    List<TransactWriteItem> transactWrites = uniqueProductIds.stream()
                            .map(itemId -> new TransactWriteItem().withUpdate(constructUpdateExpression(itemId, moveRequest)))
                            .collect(toList());


                    Optional<ContainerCapacity> sourceContainerCapacityOp = containerCapacityDAO.get(warehouseId, sourceContainerId);
                    if (!sourceContainerCapacityOp.isPresent()) {
                        throw new NonRetriableException("inconsistent source container state");
                    }
                    int existingSourceContainerCapacity = sourceContainerCapacityOp.get().getCurrentCapacity();
                    int existingDestinationContainerCapacity = 0;
                    Optional<ContainerCapacity> destinationContainerOp = containerCapacityDAO.get(warehouseId, destinationContainerId);
                    if (destinationContainerOp.isPresent()) {
                        existingDestinationContainerCapacity = destinationContainerOp.get().getCurrentCapacity();
                    }
                    int sourceContainerNewCapacity = existingSourceContainerCapacity - uniqueProductIds.size();
                    int destinationContainerNewCapacity = existingDestinationContainerCapacity + uniqueProductIds.size();

                    Update sourceContainerUpdate = constructUpdateContainerCapacityExpression(warehouseId, sourceContainerId, existingSourceContainerCapacity, sourceContainerNewCapacity, moveRequest.getSourceContainerMaxCapacity());
                    Update destinationContainerUpdate = constructUpdateContainerCapacityExpression(warehouseId, destinationContainerId, existingDestinationContainerCapacity, destinationContainerNewCapacity, moveRequest.getDestinationContainerMaxCapacity());
                    transactWrites.add(new TransactWriteItem().withUpdate(sourceContainerUpdate));
                    transactWrites.add(new TransactWriteItem().withUpdate(destinationContainerUpdate));
                    TransactWriteItemsRequest moveInventoryTransaction = new TransactWriteItemsRequest()
                            .withTransactItems(transactWrites)
                            .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);


                    amazonDynamoDBClient.transactWriteItems(moveInventoryTransaction);

                }
            }
        } catch (TransactionCanceledException tce) {
            List<CancellationReason> cancellationReasons = tce.getCancellationReasons();
            log.error("transaction cancelled exception for moving from container ", moveRequest.getSourceContainerId(), "to destination container id",
                    moveRequest.getDestinationContainerId(), " with cancellation reasons ",
                    StringUtils.join(cancellationReasons, COMMA));
            String message = String.format("Inconsistent state occurred in data layer, either of the following is true \n" +
                            "1) Source Container %s reached its minimal capacity \n" +
                            "2) Destination Container %s reached its maximal capacity \n" +
                            "2) ProductIds are never generated \n" +
                            "3) ProductIds are not generated for the given companyId %s and warehouseId %s", moveRequest.getSourceContainerId()
                    , moveRequest.getDestinationContainerId(), moveRequest.getCompanyId(), moveRequest.getWarehouseId());
            throw new InconsistentStateException(message, tce);
        } catch (InternalServerErrorException e) {
            throw new RetriableException("Exception occurred", e);
        } catch (Exception e) {
            throw new NonRetriableException("Exception occurred", e);
        }
    }


    private Update constructUpdateExpression(String itemId, InventoryInboundRequest inventoryInboundRequest) {

        Map<String, AttributeValue> inventoryKey = new HashMap<>();
        inventoryKey.put("uniqueProductId", new AttributeValue().withS(itemId));
        InventoryStatus newInventoryStatus = inventoryInboundRequest.getInventoryStatus();
        Map<String, AttributeValue> updatedAttributes = new HashMap<>();
        long currentTime = clock.millis();
        updatedAttributes.put(":new_status", new AttributeValue(newInventoryStatus.getStatus()));
        updatedAttributes.put(":inbound_id", new AttributeValue(inventoryInboundRequest.getInboundId()));
        updatedAttributes.put(":container_id", new AttributeValue(inventoryInboundRequest.getContainerId()));
        updatedAttributes.put(":warehouse_id", new AttributeValue(inventoryInboundRequest.getWarehouseId()));
        updatedAttributes.put(":company_id", new AttributeValue(inventoryInboundRequest.getCompanyId()));
        updatedAttributes.put(":modified_time", new AttributeValue().withN(String.valueOf(currentTime)));

        String previousStatus = getAppendedStatusString(newInventoryStatus, updatedAttributes);


        Update update = new Update().withTableName(INVENTORY_TABLE_NAME).withKey(inventoryKey)
                .withExpressionAttributeValues(updatedAttributes)
                .withUpdateExpression("SET inventoryStatus = :new_status , inboundId = :inbound_id , containerId =:container_id , " +
                        "modifiedTime= :modified_time")
                .withConditionExpression("inventoryStatus IN (" + previousStatus + ") AND companyId = :company_id AND warehouseId = :warehouse_id");
        return update;
    }


    private Update constructUpdateExpression(String itemId, InventoryOutboundRequest inventoryOutboundRequest) {

        Map<String, AttributeValue> inventoryKey = new HashMap<>();
        inventoryKey.put("uniqueProductId", new AttributeValue().withS(itemId));
        InventoryStatus newInventoryStatus = inventoryOutboundRequest.getInventoryStatus();

        Map<String, AttributeValue> updatedAttributes = new HashMap<>();

        updatedAttributes.put(":new_status", new AttributeValue(newInventoryStatus.getStatus()));
        updatedAttributes.put(":outbound_id", new AttributeValue(inventoryOutboundRequest.getOutboundId()));
        updatedAttributes.put(":order_id", new AttributeValue(inventoryOutboundRequest.getOrderId()));
        updatedAttributes.put(":container_id", new AttributeValue(inventoryOutboundRequest.getContainerId()));
        updatedAttributes.put(":company_id", new AttributeValue(inventoryOutboundRequest.getCompanyId()));
        updatedAttributes.put(":warehouse_id", new AttributeValue(inventoryOutboundRequest.getWarehouseId()));

        long currentTime = clock.millis();
        updatedAttributes.put(":modified_time", new AttributeValue().withN(String.valueOf(currentTime)));
        String previousStatus = getAppendedStatusString(newInventoryStatus, updatedAttributes);

        Update update = new Update().withTableName(INVENTORY_TABLE_NAME).withKey(inventoryKey)
                .withExpressionAttributeValues(updatedAttributes)
                .withUpdateExpression("SET inventoryStatus = :new_status , orderId = :order_id , outboundId =:outbound_id , " +
                        "modifiedTime= :modified_time")
                .withConditionExpression("inventoryStatus IN (" + previousStatus + ") AND containerId = :container_id " +
                        "AND companyId = :company_id AND warehouseId = :warehouse_id");
        return update;
    }

    private Update constructUpdateExpression(String itemId, MoveInventoryRequest moveInventoryRequest) {

        Map<String, AttributeValue> inventoryKey = new HashMap<>();
        inventoryKey.put("uniqueProductId", new AttributeValue().withS(itemId));
        String destinationContainerId = moveInventoryRequest.getDestinationContainerId();
        String sourceContainerId = moveInventoryRequest.getSourceContainerId();

        Map<String, AttributeValue> updatedAttributes = new HashMap<>();

        updatedAttributes.put(":new_container_id", new AttributeValue(destinationContainerId));
        long currentTime = clock.millis();
        updatedAttributes.put(":modified_time", new AttributeValue().withN(String.valueOf(currentTime)));
        updatedAttributes.put(":existing_container_id", new AttributeValue(sourceContainerId));
        updatedAttributes.put(":company_id", new AttributeValue(moveInventoryRequest.getCompanyId()));
        updatedAttributes.put(":warehouse_id", new AttributeValue(moveInventoryRequest.getWarehouseId()));



        Update update = new Update().withTableName(INVENTORY_TABLE_NAME).withKey(inventoryKey)
                .withExpressionAttributeValues(updatedAttributes)
                .withUpdateExpression("SET containerId = :new_container_id , modifiedTime= :modified_time")
                .withConditionExpression("containerId = :existing_container_id AND companyId = :company_id AND warehouseId = :warehouse_id");
        return update;
    }

    private Update constructUpdateContainerCapacityExpression(String warehouseId, String containerId, int existingCapacity,
                                                              int newCapacity, int maxCapacity) {

        ContainerStatus newContainerStatus = containerStatusDeterminer.determineStatus(newCapacity, maxCapacity);
        String warehouseContainerId = String.join(DELIMITER, warehouseId, containerId);
        Map<String, AttributeValue> containerCapacityTableKey = new HashMap<>();
        containerCapacityTableKey
                .put("warehouseContainerId", new AttributeValue().withS(warehouseContainerId));
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":new_quantity",
                new AttributeValue().withN(String.valueOf(newCapacity)));
        expressionAttributeValues
                .put(":existing_quantity", new AttributeValue().withN(String.valueOf(existingCapacity)));
        expressionAttributeValues.put(":new_status", new AttributeValue().withS(newContainerStatus.getStatus()));
        long modifiedTime = clock.millis();
        expressionAttributeValues.put(":modified_time", new AttributeValue().withN(String.valueOf(modifiedTime)));
        String previousStatus = getAppendedStatusString(newContainerStatus, expressionAttributeValues);


        Update update = new Update()
                .withTableName(CONTAINER_CAPACITY_TABLE_NAME)
                .withKey(containerCapacityTableKey)
                .withUpdateExpression("SET currentCapacity = :new_quantity , containerStatus= :new_status , modifiedTime = :modified_time")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConditionExpression("currentCapacity = :existing_quantity and containerStatus IN (" + previousStatus + ")");
        return update;
    }

    private List<Inventory> getFailedBatchListItems(final DynamoDBMapper.FailedBatch failedBatch) {
        return failedBatch.getUnprocessedItems()
                .values()
                .stream()
                .flatMap(List::stream)
                .map(WriteRequest::getPutRequest)
                .map(PutRequest::getItem)
                .map(e -> inventoryDynamoDbMapper.marshallIntoObject(Inventory.class, e))
                .collect(toList());
    }


    private String getAppendedStatusString(InventoryStatus newInventoryStatus, Map<String, AttributeValue> updatedAttributes) {
        List<String> previousValues = new ArrayList<>();
        for (int i = 0; i < newInventoryStatus.previousStates().size(); i++) {
            String key = ":is" + (i + 1);
            updatedAttributes.put(key,
                    new AttributeValue().withS(newInventoryStatus.previousStates().get(i).getStatus()));
            previousValues.add(key);
        }
        String previousStatus = String.join(", ", previousValues);
        return previousStatus;
    }

    private String getAppendedStatusString(ContainerStatus containerStatus, Map<String, AttributeValue> updatedAttributes) {
        List<String> previousValues = new ArrayList<>();
        for (int i = 0; i < containerStatus.previousStates().size(); i++) {
            String key = ":is" + (i + 1);
            updatedAttributes.put(key,
                    new AttributeValue().withS(containerStatus.previousStates().get(i).getStatus()));
            previousValues.add(key);
        }
        String previousStatus = String.join(", ", previousValues);
        return previousStatus;
    }
}
