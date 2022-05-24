package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.inject.Inject;
import com.jcabi.aspects.RetryOnFailure;
import lombok.extern.slf4j.Slf4j;
import org.warehousemanagement.entities.container.containerstatus.Available;
import org.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.warehousemanagement.entities.container.containerstatus.Filled;
import org.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.RetriableException;
import org.warehousemanagement.entities.inventory.AddInventoryRequest;
import org.warehousemanagement.entities.inventory.FulfillInventoryRequest;
import org.warehousemanagement.entities.inventory.MoveInventoryRequest;
import org.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InventoryDynamoDbImpl implements InventoryDAO{

    private static final int RETRY_ATTEMPS = 3;
    private static final int RETRY_DELAY = 5;
    AmazonDynamoDBClient amazonDynamoDBClient;
    DynamoDBMapper inventoryDynamoDbMapper;
    ContainerCapacityDAO containerCapacityDAO;
    Clock clock;

    @Inject
    public InventoryDynamoDbImpl(AmazonDynamoDBClient amazonDynamoDBClient, DynamoDBMapper inventoryDynamoDbMapper,
                                 ContainerCapacityDAO containerCapacityDAO, Clock clock) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.inventoryDynamoDbMapper = inventoryDynamoDbMapper;
        this.containerCapacityDAO = containerCapacityDAO;
        this.clock = clock;
    }


    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param addInventoryRequest
     */
    @RetryOnFailure(attempts = RETRY_ATTEMPS, delay = RETRY_DELAY)
    public void add(AddInventoryRequest addInventoryRequest) {

        String containerId = addInventoryRequest.getContainerId();
        String warehouseId = addInventoryRequest.getWarehouseId();

        List<String> uniqueProductIds = addInventoryRequest.getUniqueProductIds();
        List<TransactWriteItem> transactWrites = uniqueProductIds.stream()
                .map(itemId -> new TransactWriteItem().withPut(constructPutExpression(itemId, addInventoryRequest))).collect(Collectors.toList());
        synchronized (warehouseId + containerId) {
            Optional<ContainerCapacity> containerCapacityOptional = containerCapacityDAO.get(warehouseId, containerId);
            int existingQuantity = 0;
            if (!containerCapacityOptional.isPresent()) {
                ContainerCapacity containerCapacity = containerCapacityOptional.get();
                existingQuantity = containerCapacity.getCurrentCapacity();
            }
            int newCapacity = existingQuantity + uniqueProductIds.size();
            Update updateCapacityExpression = constructUpdateContainerCapacityExpression(warehouseId, containerId, existingQuantity, newCapacity
                    , addInventoryRequest.getContainerMaxCapacity());

            transactWrites.add(new TransactWriteItem().withUpdate(updateCapacityExpression));


            TransactWriteItemsRequest addInventoryTransaction = new TransactWriteItemsRequest()
                    .withTransactItems(transactWrites).withClientRequestToken(uniqueProductIds.get(0))
                    .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

            try {
                amazonDynamoDBClient.transactWriteItems(addInventoryTransaction);

            } catch (ConditionalCheckFailedException cce) {
                log.warn("Conditional check failed", cce);
            } catch (Exception e) {
                throw new RetriableException("Exception occurred", e);
            }
        }
    }

    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param fulfillInventoryRequest
     */
    public void fulfill(FulfillInventoryRequest fulfillInventoryRequest) {

        String containerId = fulfillInventoryRequest.getContainerId();
        String warehouseId = fulfillInventoryRequest.getWarehouseId();

        List<TransactWriteItem> transactWrites = fulfillInventoryRequest.getUniqueProductIds().stream()
                .map(itemId -> new TransactWriteItem().withUpdate(constructUpdateExpression(itemId, fulfillInventoryRequest))).collect(Collectors.toList());

        synchronized (warehouseId + containerId) {
            Optional<ContainerCapacity> containerCapacityOptional = containerCapacityDAO.get(warehouseId, containerId);
            int existingQuantity = 0;
            if (!containerCapacityOptional.isPresent()) {
                ContainerCapacity containerCapacity = containerCapacityOptional.get();
                existingQuantity = containerCapacity.getCurrentCapacity();
            }
            int newQuantity = existingQuantity - fulfillInventoryRequest.getUniqueProductIds().size();
            Update updateCapacityExpression = constructUpdateContainerCapacityExpression(warehouseId, containerId,
                    existingQuantity, newQuantity, fulfillInventoryRequest.getContainerMaxCapacity());

            transactWrites.add(new TransactWriteItem().withUpdate(updateCapacityExpression));


            TransactWriteItemsRequest addInventoryTransaction = new TransactWriteItemsRequest()
                    .withTransactItems(transactWrites)
                    .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

            try {
                amazonDynamoDBClient.transactWriteItems(addInventoryTransaction);

            } catch (ConditionalCheckFailedException cce) {
                log.warn("Conditional check failed", cce);
            } catch (Exception e) {
                throw new RetriableException("Exception occurred", e);
            }
        }
    }


    public void move(MoveInventoryRequest moveInventoryRequest) {
        List<String> uniqueProductIds = moveInventoryRequest.getUniqueProductIds();
        String destinationContainerId = moveInventoryRequest.getDestinationContainerId();
        String sourceContainerId = moveInventoryRequest.getSourceContainerId();
        String warehouseId = moveInventoryRequest.getWarehouseId();

        synchronized (warehouseId + sourceContainerId) {
            synchronized (warehouseId + destinationContainerId) {

                List<TransactWriteItem> transactWrites = uniqueProductIds.stream()
                        .map(itemId -> new TransactWriteItem().withUpdate(constructUpdateExpression(itemId, moveInventoryRequest)))
                        .collect(Collectors.toList());


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

                Update sourceContainerUpdate = constructUpdateContainerCapacityExpression(warehouseId, sourceContainerId, existingSourceContainerCapacity, sourceContainerNewCapacity, moveInventoryRequest.getSourceContainerMaxCapacity());
                Update destinationContainerUpdate = constructUpdateContainerCapacityExpression(warehouseId, destinationContainerId, existingDestinationContainerCapacity, destinationContainerNewCapacity, moveInventoryRequest.getDestinationContainerMaxCapacity());
                transactWrites.add(new TransactWriteItem().withUpdate(sourceContainerUpdate));
                transactWrites.add(new TransactWriteItem().withUpdate(destinationContainerUpdate));
                TransactWriteItemsRequest moveInventoryTransaction = new TransactWriteItemsRequest()
                        .withTransactItems(transactWrites)
                        .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);


                try {
                    amazonDynamoDBClient.transactWriteItems(moveInventoryTransaction);

                } catch (ConditionalCheckFailedException cce) {
                    log.warn("Conditional check failed", cce);
                } catch (Exception e) {
                    throw new RetriableException("Exception occurred", e);
                }
            }
        }
    }

    private Update constructUpdateExpression(String itemId, MoveInventoryRequest moveInventoryRequest) {

        Map<String, AttributeValue> inventoryKey = new HashMap<>();
        inventoryKey.put("uniqueProductId", new AttributeValue().withS(itemId));
        String destinationContainerId = moveInventoryRequest.getDestinationContainerId();
        String sourceContainerId = moveInventoryRequest.getSourceContainerId();

        Map<String, AttributeValue> updatedAttributes = new HashMap<>();

        updatedAttributes.put("new_container_id", new AttributeValue(destinationContainerId));
        long currentTime = clock.millis();
        updatedAttributes.put("modified_time", new AttributeValue().withN(String.valueOf(currentTime)));
        updatedAttributes.put("existing_container_id", new AttributeValue(sourceContainerId));


        Update update = new Update().withTableName("sku-inventory").withKey(inventoryKey)
                .withExpressionAttributeValues(updatedAttributes)
                .withUpdateExpression("SET containerId = :new_container_id AND modifiedTime= : modified_time")
                .withConditionExpression("containerId = :existing_container_id");
        return update;
    }

    private Update constructUpdateExpression(String itemId, FulfillInventoryRequest fulfillInventoryRequest) {

        Map<String, AttributeValue> inventoryKey = new HashMap<>();
        inventoryKey.put("uniqueProductId", new AttributeValue().withS(itemId));
        InventoryStatus newInventoryStatus = fulfillInventoryRequest.getInventoryStatus();
        Set<InventoryStatus> possibleCurrentStatuses = newInventoryStatus.previousStates();

        AttributeValue possibleCurrentStatusList = new AttributeValue().withL(possibleCurrentStatuses.stream()
                .map(s -> new AttributeValue(s.getStatus())).collect(Collectors.toList()));


        Map<String, AttributeValue> updatedAttributes = new HashMap<>();

        updatedAttributes.put(":new_status", new AttributeValue(newInventoryStatus.getStatus()));
        updatedAttributes.put("outbound_id", new AttributeValue(fulfillInventoryRequest.getOutboundId()));
        updatedAttributes.put("order_id", new AttributeValue(fulfillInventoryRequest.getOrderId()));
        updatedAttributes.put("containerId", new AttributeValue(fulfillInventoryRequest.getContainerId()));
        long currentTime = clock.millis();
        updatedAttributes.put("modified_time", new AttributeValue().withN(String.valueOf(currentTime)));
        updatedAttributes.put("existing_status", possibleCurrentStatusList);


        Update update = new Update().withTableName("sku-inventory").withKey(inventoryKey)
                .withExpressionAttributeValues(updatedAttributes)
                .withUpdateExpression("SET status = :new_status AND orderId = :order_id AND outboundId =:outbound_id AND " +
                        "modifiedTime= : modified_time")
                .withConditionExpression("status IN :existing_status AND containerId = :containerId");
        return update;
    }

    private Put constructPutExpression(String itemId, AddInventoryRequest addInventoryRequest) {
        Map<String, AttributeValue> inventoryItem = new HashMap<>();

        inventoryItem.put("uniqueItemId", new AttributeValue(itemId));
        inventoryItem.put("warehouseLocationId",
                new AttributeValue(String.join("<%>", addInventoryRequest.getWarehouseId(), addInventoryRequest.getContainerId())));
        inventoryItem.put("skuCode", new AttributeValue(addInventoryRequest.getSkuCode()));
        inventoryItem.put("status", new AttributeValue(addInventoryRequest.getInventoryStatus().getStatus()));
        inventoryItem.put("inboundId", new AttributeValue(addInventoryRequest.getInboundId()));
        long currentTime = clock.millis();
        inventoryItem.put("creationTime", new AttributeValue().withN(String.valueOf(currentTime)));
        inventoryItem.put("modifiedTime", new AttributeValue().withN(String.valueOf(currentTime)));

        Put put = new Put().withTableName("sku-inventory").withItem(inventoryItem)
                .withConditionExpression("attribute_not_exists(uniqueItemId)");
        return put;
    }


    private Update constructUpdateContainerCapacityExpression(String warehouseId, String containerId, int existingCapacity,
                                                              int newCapacity, int maxCapacity) {

        ContainerStatus newContainerStatus = new Available();
        if (newCapacity == 0) {
            newContainerStatus = new Available();
        } else if (newCapacity == maxCapacity) {
            newContainerStatus = new Filled();
        } else if (newCapacity < maxCapacity) {
            newContainerStatus = new PartiallyFilled();
        }
        Set<ContainerStatus> possibleCurrentStatuses = newContainerStatus.previousStates();

        AttributeValue possibleCurrentStatusList = new AttributeValue().withL(possibleCurrentStatuses.stream()
                .map(s -> new AttributeValue(s.getStatus())).collect(Collectors.toList()));

        Map<String, AttributeValue> containerCapacityTableKey = new HashMap<>();
        containerCapacityTableKey
                .put("warehouseId", new AttributeValue().withS(warehouseId));
        containerCapacityTableKey
                .put("containerId", new AttributeValue().withS(containerId));


        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":new_quantity",
                new AttributeValue().withN(String.valueOf(newCapacity)));
        expressionAttributeValues
                .put(":existing_Quantity", new AttributeValue().withN(String.valueOf(existingCapacity)));
        expressionAttributeValues.put(":new_status", new AttributeValue().withS(newContainerStatus.getStatus()));
        expressionAttributeValues.put("existing_status", possibleCurrentStatusList);
        expressionAttributeValues.put("modified_time", new AttributeValue().withN(String.valueOf(clock.millis())));



        Update update = new Update()
                .withTableName("container_capacity")
                .withKey(containerCapacityTableKey)
                .withUpdateExpression("SET currentCapacity = :new_quantity AND status= :new_status AND modifiedTime = :modified_time")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConditionExpression("currentCapacity = :expected_quantity and status IN :=existing_status");
        return update;
    }
}
