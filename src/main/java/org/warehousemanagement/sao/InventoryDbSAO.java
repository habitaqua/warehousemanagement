package org.warehousemanagement.sao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.Put;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.amazonaws.services.dynamodbv2.model.TransactionCanceledException;
import com.amazonaws.services.dynamodbv2.model.Update;
import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.Inventory;
import org.warehousemanagement.entities.dynamodb.LocationSKUMapping;
import org.warehousemanagement.entities.inventory.InventoryDTO;
import org.warehousemanagement.entities.locationskumapping.GetLocationSKUMappingRequest;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryDbSAO {

    AmazonDynamoDBClient amazonDynamoDBClient;
    DynamoDBMapper inventoryDynamoDbMapper;
    LocationSKUMappingDbSAO locationSKUMappingDbSAO;
    Clock clock;

    @Inject
    public InventoryDbSAO(AmazonDynamoDBClient amazonDynamoDBClient, DynamoDBMapper inventoryDynamoDbMapper,
                          LocationSKUMappingDbSAO locationSKUMappingDbSAO, Clock clock) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.inventoryDynamoDbMapper = inventoryDynamoDbMapper;
        this.locationSKUMappingDbSAO = locationSKUMappingDbSAO;
        this.clock = clock;
    }

    //TODO
    public void batchSave(List<InventoryDTO> skuInventoryDTOList) {

        List<Inventory> inventoryList = skuInventoryDTOList.stream().map(this::toInventoryData)
                .collect(Collectors.toList());
        List<DynamoDBMapper.FailedBatch> failedBatches = inventoryDynamoDbMapper.batchSave(inventoryList);
        if (failedBatches.size() > 0) {
            System.out.println(failedBatches.get(0).getException().getMessage());
            throw new RuntimeException(failedBatches.get(0).getException().getCause());
        }
    }

    public void add(InventoryDTO inventoryDTO) {

        Map<String, AttributeValue> inventoryItemToSave = constructInventoryItem(inventoryDTO);
        Optional<LocationSKUMapping> locationSKUCountOptional = getLocationSKUCount(inventoryDTO);
        Put inventoryPut = new Put().withTableName("sku-inventory").withItem(inventoryItemToSave);
        Collection<TransactWriteItem> transactWrite = Arrays.asList(new TransactWriteItem().withPut(inventoryPut));

        if (locationSKUCountOptional.isPresent()) {

            int existingQuantity = locationSKUCountOptional.get().getQuantity();
            Map<String, AttributeValue> locationSKUCountKey = new HashMap<>();
            locationSKUCountKey
                    .put("locationWarehouseId", new AttributeValue(String.join("<%>",
                            inventoryDTO.getLocationId(), inventoryDTO.getWarehouseId())));
            locationSKUCountKey
                    .put("skuCategoryAndType", new AttributeValue(String.join("<%>",
                            inventoryDTO.getSkuCategory().getValue(), inventoryDTO.getSkuType().getValue())));

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":new_quantity",
                    new AttributeValue().withN(String.valueOf(existingQuantity + 1)));
            expressionAttributeValues
                    .put(":existing_Quantity", new AttributeValue().withN(String.valueOf(existingQuantity)));

            Update incrementLocationSKUCount = new Update()
                    .withTableName("location_sku_count")
                    .withKey(locationSKUCountKey)
                    .withUpdateExpression("SET quantity = :new_quantity")
                    .withExpressionAttributeValues(expressionAttributeValues)
                    .withConditionExpression("quantity = :expected_quantity");

            transactWrite.add(new TransactWriteItem().withUpdate(incrementLocationSKUCount));

        } else {

            Map<String, AttributeValue> locationSKUCountItem = constructLocationSKUCountItem(inventoryDTO);
            Put locationSKUCountItemPut = new Put().withTableName("location-sku-count").withItem(locationSKUCountItem);

            transactWrite.add(new TransactWriteItem().withPut(locationSKUCountItemPut));
        }

        TransactWriteItemsRequest addInventoryTransaction = new TransactWriteItemsRequest()
                .withTransactItems(transactWrite)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        try {
            amazonDynamoDBClient.transactWriteItems(addInventoryTransaction);

        } catch (ResourceNotFoundException rnf) {
            System.err.println("One of the table involved in the transaction is not found" + rnf.getMessage());
        } catch (InternalServerErrorException ise) {
            System.err.println("Internal Server Error" + ise.getMessage());
        } catch (TransactionCanceledException tce) {
            System.out.println("Transaction Canceled " + tce.getMessage());
        }
    }



    public void fulfillSKU(InventoryDTO inventoryDTO) {

        Map<String, AttributeValue> inventoryItemToSave = constructInventoryItem(inventoryDTO);
        Optional<LocationSKUMapping> locationSKUCountOptional = getLocationSKUCount(inventoryDTO);
        Put inventoryPut = new Put().withTableName("sku-inventory").withItem(inventoryItemToSave);
        Collection<TransactWriteItem> transactWrite = Arrays.asList(new TransactWriteItem().withPut(inventoryPut));

        if (locationSKUCountOptional.isPresent()) {

            int existingQuantity = locationSKUCountOptional.get().getQuantity();
            Map<String, AttributeValue> locationSKUCountKey = new HashMap<>();
            locationSKUCountKey
                    .put("locationWarehouseId", new AttributeValue(String.join("<%>",
                            inventoryDTO.getLocationId(), inventoryDTO.getWarehouseId())));
            locationSKUCountKey
                    .put("skuCategoryAndType", new AttributeValue(String.join("<%>",
                            inventoryDTO.getSkuCategory().getValue(), inventoryDTO.getSkuType().getValue())));

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":new_quantity",
                    new AttributeValue().withN(String.valueOf(existingQuantity + 1)));
            expressionAttributeValues
                    .put(":existing_Quantity", new AttributeValue().withN(String.valueOf(existingQuantity)));

            Update incrementLocationSKUCount = new Update()
                    .withTableName("location_sku_count")
                    .withKey(locationSKUCountKey)
                    .withUpdateExpression("SET quantity = :new_quantity")
                    .withExpressionAttributeValues(expressionAttributeValues)
                    .withConditionExpression("quantity = :expected_quantity");

            transactWrite.add(new TransactWriteItem().withUpdate(incrementLocationSKUCount));

        } else {

            Map<String, AttributeValue> locationSKUCountItem = constructLocationSKUCountItem(inventoryDTO);
            Put locationSKUCountItemPut = new Put().withTableName("location-sku-count").withItem(locationSKUCountItem);

            transactWrite.add(new TransactWriteItem().withPut(locationSKUCountItemPut));
        }

        TransactWriteItemsRequest addInventoryTransaction = new TransactWriteItemsRequest()
                .withTransactItems(transactWrite)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        try {
            amazonDynamoDBClient.transactWriteItems(addInventoryTransaction);

        } catch (ResourceNotFoundException rnf) {
            System.err.println("One of the table involved in the transaction is not found" + rnf.getMessage());
        } catch (InternalServerErrorException ise) {
            System.err.println("Internal Server Error" + ise.getMessage());
        } catch (TransactionCanceledException tce) {
            System.out.println("Transaction Canceled " + tce.getMessage());
        }
    }

    private Optional<LocationSKUMapping> getLocationSKUCount(InventoryDTO inventoryDTO) {
        GetLocationSKUMappingRequest getLocationSKUMappingRequest =
                GetLocationSKUMappingRequest.builder().locationId(inventoryDTO.getLocationId())
                        .warehouseId(inventoryDTO.getWarehouseId()).skuCategory(inventoryDTO.getSkuCategory())
                        .skuType(inventoryDTO.getSkuType()).build();
        Optional<LocationSKUMapping> locationSKUCountOptional = locationSKUMappingDbSAO
                .getLocationSKUCount(getLocationSKUMappingRequest);
        return locationSKUCountOptional;
    }

    private Map<String, AttributeValue> constructLocationSKUCountItem(InventoryDTO inventoryDTO) {
        Map<String, AttributeValue> locationSKUCountItem = new HashMap<>();
        long currentTime = clock.millis();

        String warehouseId = inventoryDTO.getWarehouseId();
        locationSKUCountItem.put("warehouseLocationId", new AttributeValue(String.join("<%>",
                warehouseId, inventoryDTO.getLocationId())));
        locationSKUCountItem.put("skuCategoryAndType", new AttributeValue(String.join("<%>",
                inventoryDTO.getSkuCategory().getValue(), inventoryDTO.getSkuType().getValue())));
        locationSKUCountItem.put("warehouseId", new AttributeValue(warehouseId));
        locationSKUCountItem.put("creationTime", new AttributeValue().withN(String.valueOf(currentTime)));
        locationSKUCountItem.put("modifiedTime", new AttributeValue().withN(String.valueOf(currentTime)));
        locationSKUCountItem.put("quantity", new AttributeValue().withN(String.valueOf(1)));
        locationSKUCountItem.put("uom", new AttributeValue(inventoryDTO.getSkuType().getUom().toString()));
        return locationSKUCountItem;
    }

    private Map<String, AttributeValue> constructInventoryItem(InventoryDTO inventoryDTO) {
        Map<String, AttributeValue> inventoryItem = new HashMap<>();

        inventoryItem.put("skuId", new AttributeValue(inventoryDTO.getSkuId()));
        inventoryItem.put("warehouseId", new AttributeValue(inventoryDTO.getWarehouseId()));
        inventoryItem.put("locationId", new AttributeValue(inventoryDTO.getLocationId()));
        inventoryItem.put("skuType", new AttributeValue(inventoryDTO.getSkuType().getValue()));
        inventoryItem.put("skuCategory", new AttributeValue(inventoryDTO.getSkuCategory().getValue()));
        inventoryItem.put("companyId", new AttributeValue(inventoryDTO.getCompanyId()));
        inventoryItem.put("status", new AttributeValue(inventoryDTO.getInventoryStatus().getStatus()));
        long currentTime = clock.millis();
        inventoryItem.put("creationTime", new AttributeValue().withN(String.valueOf(currentTime)));
        inventoryItem.put("modifiedTime", new AttributeValue().withN(String.valueOf(currentTime)));
        return inventoryItem;
    }

    private Inventory toInventoryData(InventoryDTO inventoryDTO) {

        long time = clock.instant().toEpochMilli();
        Inventory inventory = Inventory.builder().skuId(inventoryDTO.getSkuId()).companyId(inventoryDTO.getCompanyId())
                .warehouseId(inventoryDTO.getWarehouseId()).creationTime(time).modifiedTime(time)
                .skuCategory(inventoryDTO.getSkuCategory()).skuType(inventoryDTO.getSkuType())
                .build();
        return inventory;
    }
}
