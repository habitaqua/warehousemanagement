package org.warehousemanagement.dao.inmemoryDb;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.*;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.warehousemanagement.dao.ContainerCapacityDAO;
import org.warehousemanagement.dao.ContainerCapacityDynamoDAOImpl;
import org.warehousemanagement.dao.InventoryDynamoDAOImpl;
import org.warehousemanagement.entities.container.containerstatus.Available;
import org.warehousemanagement.entities.container.containerstatus.Filled;
import org.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.dynamodb.Inventory;
import org.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.warehousemanagement.entities.inventory.InventoryInboundRequest;
import org.warehousemanagement.entities.inventory.InventoryOutboundRequest;
import org.warehousemanagement.entities.inventory.inventorystatus.Inbound;
import org.warehousemanagement.entities.inventory.inventorystatus.Outbound;
import org.warehousemanagement.entities.inventory.inventorystatus.Production;
import org.warehousemanagement.testutils.LocalDbCreationRule;
import org.warehousemanagement.testutils.Utilities;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

import static org.warehousemanagement.testutils.Utilities.*;


public class TestInmemoryDbInventoryDynamoDAOImpl {


    public static final String SKU_TYPE = "sku-type";
    private static final String INBOUND_1 = "INBOUND-1";
    private static final String INBOUND_2 = "INBOUND-2";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";
    private static final String COMPANY_1 = "COMPANY-1";
    private static final String COMPANY_2 = "COMPANY-2";


    private static final String CONTAINER_1 = "CONTAINER-1";
    public static final String SKU_CODE = "sku-code";
    public static final String SKU_CATEGORY = "sku-category";
    public static final ImmutableList<String> UNIQUE_PRODUCT_IDS_1 = ImmutableList.of("1", "2", "3", "4");
    public static final ImmutableList<String> UNIQUE_PRODUCT_IDS_2 = ImmutableList.of("5", "6", "7", "8");

    public static final String INVENTORY_TABLE_NAME = "inventory";
    public static final String DELIMITER = "<%>";
    public static final String OUTBOUND_1 = "OUTBOUND-1";
    public static final int CONTAINER_MAX_CAPACITY = 5;
    public static final String ORDER_1 = "ORDER-1";

    @ClassRule
    public static LocalDbCreationRule dynamoDB = new LocalDbCreationRule();
    InventoryDynamoDAOImpl inventoryDynamoDAO;
    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDBClient amazonDynamoDB;

    ContainerCapacityDAO containerCapacityDAO;

    Clock clock;

    DynamoDBMapperConfig dynamoDBMapperConfig;


    @Before
    public void setup() {
        Properties testProperties = Utilities.getTestProperties();
        String amazonAWSAccessKey = testProperties.getProperty(AWS_ACCESSKEY);
        String amazonAWSSecretKey = testProperties.getProperty(AWS_SECRETKEY);
        String amazonDynamoDBEndpoint = testProperties.getProperty(DYNAMODB_ENDPOINT);

        amazonDynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey));
        amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
        dynamoDBMapperConfig = DynamoDBMapperConfig.builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES).build();
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
        clock = Clock.systemUTC();
        containerCapacityDAO = new ContainerCapacityDynamoDAOImpl(dynamoDBMapper, clock);
        inventoryDynamoDAO = new InventoryDynamoDAOImpl(amazonDynamoDB, dynamoDBMapper, containerCapacityDAO, clock);

        try {
            CreateTableRequest tableRequestInventory = dynamoDBMapper.generateCreateTableRequest(Inventory.class);

            tableRequestInventory.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));

            amazonDynamoDB.createTable(tableRequestInventory);

            CreateTableRequest tableRequestContainerCapacity = dynamoDBMapper.generateCreateTableRequest(ContainerCapacity.class);

            tableRequestContainerCapacity.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));

            amazonDynamoDB.createTable(tableRequestContainerCapacity);
        } catch (ResourceInUseException e) {
        }
        containerCapacityDAO.init(WAREHOUSE_1, CONTAINER_1);
    }

    @After
    public void teardown() {
        DeleteTableRequest deleteTableRequestInventory = dynamoDBMapper.generateDeleteTableRequest(Inventory.class, dynamoDBMapperConfig);
        amazonDynamoDB.deleteTable(deleteTableRequestInventory);

        DeleteTableRequest deleteTableRequestContainerCapacity = dynamoDBMapper.generateDeleteTableRequest(ContainerCapacity.class, dynamoDBMapperConfig);
        amazonDynamoDB.deleteTable(deleteTableRequestContainerCapacity);
    }

    @Test
    public void test_add_success() {
        long productionTime = clock.millis();
        InventoryAddRequest request = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(productionTime).build();

        inventoryDynamoDAO.add(request);
        List<KeyPair> keyPairsToLoad = UNIQUE_PRODUCT_IDS_1.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());

        Map<String, List<Object>> inventoryObjects = dynamoDBMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
        List<Object> objects = inventoryObjects.get(INVENTORY_TABLE_NAME);
        List<Inventory> actualInventory = objects.stream().map(object -> (Inventory) object).collect(Collectors.toList());
        String skuCategoryAndType = String.join(DELIMITER, request.getSkuCategory(), request.getSkuType());
        List<Inventory> expectedInventory = UNIQUE_PRODUCT_IDS_1.stream().map(id -> Inventory.builder().uniqueProductId(id).warehouseId(request.getWarehouseId()).companyId(request.getCompanyId())
                .skuCategoryType(skuCategoryAndType).skuCode(request.getSkuCode()).creationTime(clock.millis()).modifiedTime(clock.millis())
                .productionTime(request.getProductionTime()).inventoryStatus(request.getInventoryStatus()).build()).collect(Collectors.toList());
        new ListAssert(actualInventory).usingRecursiveFieldByFieldElementComparatorIgnoringFields("creationTime", "modifiedTime")
                .containsExactlyInAnyOrderElementsOf(expectedInventory);
    }

    @Test
    public void test_add_already_existing() {
        long productionTime = clock.millis();
        InventoryAddRequest request = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(productionTime).build();

        inventoryDynamoDAO.add(request);
        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class).isThrownBy(() -> inventoryDynamoDAO.add(request));
    }


    @Test
    public void test_inbound_success() {
        long productionTime = clock.millis();
        InventoryAddRequest request = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(productionTime).build();

        inventoryDynamoDAO.add(request);

        InventoryInboundRequest inventoryInboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        inventoryDynamoDAO.inbound(inventoryInboundRequest);
        String warehouseContainerId = String.join(DELIMITER, inventoryInboundRequest.getWarehouseId(), inventoryInboundRequest.getContainerId());


        List<KeyPair> keyPairsToLoad = UNIQUE_PRODUCT_IDS_1.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());

        Map<String, List<Object>> inventoryObjects = dynamoDBMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
        List<Object> objects = inventoryObjects.get(INVENTORY_TABLE_NAME);
        List<Inventory> actualInventory = objects.stream().map(object -> (Inventory) object).collect(Collectors.toList());
        String skuCategoryAndType = String.join(DELIMITER, request.getSkuCategory(), request.getSkuType());
        List<Inventory> expectedInventory = UNIQUE_PRODUCT_IDS_1.stream().map(id -> Inventory.builder().uniqueProductId(id)
                .warehouseId(request.getWarehouseId()).companyId(request.getCompanyId()).containerId(inventoryInboundRequest.getContainerId())
                .inboundId(inventoryInboundRequest.getInboundId()).skuCategoryType(skuCategoryAndType).skuCode(request.getSkuCode()).creationTime(clock.millis()).modifiedTime(clock.millis())
                .productionTime(request.getProductionTime()).inventoryStatus(inventoryInboundRequest.getInventoryStatus()).build()).collect(Collectors.toList());
        new ListAssert(actualInventory).usingRecursiveFieldByFieldElementComparatorIgnoringFields("creationTime", "modifiedTime")
                .containsExactlyInAnyOrderElementsOf(expectedInventory);


        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDAO.get(inventoryInboundRequest.getWarehouseId(), inventoryInboundRequest.getContainerId());
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(true);
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(inventoryInboundRequest.getUniqueProductIds().size()).warehouseContainerId(warehouseContainerId)
                .creationTime(clock.millis()).modifiedTime(clock.millis()).build();
        RecursiveComparisonConfiguration ignoreFields = RecursiveComparisonConfiguration.builder().withIgnoredFields("creationTime", "modifiedTime").build();
        new ObjectAssert<ContainerCapacity>(containerCapacityActualOp.get()).usingRecursiveComparison(ignoreFields).isEqualTo(containerCapacityExpected);
    }

    @Test
    public void test_inbound_non_generated_ids() {
        InventoryInboundRequest inventoryInboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        Assertions.assertThatExceptionOfType(InconsistentStateException.class).isThrownBy(() -> inventoryDynamoDAO.inbound(inventoryInboundRequest));
        String warehouseContainerId = String.join(DELIMITER, inventoryInboundRequest.getWarehouseId(), inventoryInboundRequest.getContainerId());
        List<KeyPair> keyPairsToLoad = UNIQUE_PRODUCT_IDS_1.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());

        Map<String, List<Object>> inventoryObjects = dynamoDBMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
        List<Object> objects = inventoryObjects.get(INVENTORY_TABLE_NAME);
        new BooleanAssert(objects.size() == 0).isEqualTo(true);

        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDAO.get(inventoryInboundRequest.getWarehouseId(), inventoryInboundRequest.getContainerId());
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(true);
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(0).warehouseContainerId(warehouseContainerId)
                .creationTime(clock.millis()).modifiedTime(clock.millis()).build();
        RecursiveComparisonConfiguration ignoreFields = RecursiveComparisonConfiguration.builder().withIgnoredFields("creationTime", "modifiedTime").build();
        new ObjectAssert<ContainerCapacity>(containerCapacityActualOp.get()).usingRecursiveComparison(ignoreFields).isEqualTo(containerCapacityExpected);
    }

    @Test
    public void test_inbound_different_company_generated_ids() {
        long productionTime = clock.millis();
        InventoryAddRequest addRequest = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_2)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(productionTime).build();

        inventoryDynamoDAO.add(addRequest);

        InventoryInboundRequest inventoryInboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        Assertions.assertThatExceptionOfType(InconsistentStateException.class).isThrownBy(() -> inventoryDynamoDAO.inbound(inventoryInboundRequest));

        String warehouseContainerId = String.join(DELIMITER, inventoryInboundRequest.getWarehouseId(), inventoryInboundRequest.getContainerId());
        List<KeyPair> keyPairsToLoad = UNIQUE_PRODUCT_IDS_1.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());

        Map<String, List<Object>> inventoryObjects = dynamoDBMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
        List<Object> objects = inventoryObjects.get(INVENTORY_TABLE_NAME);
        List<Inventory> actualInventory = objects.stream().map(object -> (Inventory) object).collect(Collectors.toList());
        String skuCategoryAndType = String.join(DELIMITER, addRequest.getSkuCategory(), addRequest.getSkuType());
        List<Inventory> expectedInventory = UNIQUE_PRODUCT_IDS_1.stream().map(id -> Inventory.builder().uniqueProductId(id)
                .warehouseId(addRequest.getWarehouseId()).companyId(addRequest.getCompanyId())
                .skuCategoryType(skuCategoryAndType).skuCode(addRequest.getSkuCode()).creationTime(clock.millis()).modifiedTime(clock.millis())
                .productionTime(addRequest.getProductionTime()).inventoryStatus(addRequest.getInventoryStatus()).build()).collect(Collectors.toList());
        new ListAssert(actualInventory).usingRecursiveFieldByFieldElementComparatorIgnoringFields("creationTime", "modifiedTime")
                .containsExactlyInAnyOrderElementsOf(expectedInventory);


        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDAO.get(inventoryInboundRequest.getWarehouseId(), inventoryInboundRequest.getContainerId());
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(true);
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(0).warehouseContainerId(warehouseContainerId)
                .creationTime(clock.millis()).modifiedTime(clock.millis()).build();
        RecursiveComparisonConfiguration ignoreFields = RecursiveComparisonConfiguration.builder().withIgnoredFields("creationTime", "modifiedTime").build();
        new ObjectAssert<ContainerCapacity>(containerCapacityActualOp.get()).usingRecursiveComparison(ignoreFields).isEqualTo(containerCapacityExpected);
    }

    @Test
    public void test_outbound_success() {
        long productionTime = clock.millis();
        InventoryAddRequest request = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(productionTime).build();

        inventoryDynamoDAO.add(request);

        InventoryInboundRequest inboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        inventoryDynamoDAO.inbound(inboundRequest);

        List<String> outboundIds = UNIQUE_PRODUCT_IDS_1.subList(0, 3);
        InventoryOutboundRequest outboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).companyId(COMPANY_1)
                .inventoryStatus(new Outbound()).containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .orderId(ORDER_1).skuCode(SKU_CODE).uniqueProductIds(outboundIds).build();
        inventoryDynamoDAO.outbound(outboundRequest);

        String warehouseContainerId = String.join(DELIMITER, inboundRequest.getWarehouseId(), inboundRequest.getContainerId());
        List<KeyPair> keyPairsToLoad = UNIQUE_PRODUCT_IDS_1.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());

        Map<String, List<Object>> inventoryObjects = dynamoDBMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
        List<Object> objects = inventoryObjects.get(INVENTORY_TABLE_NAME);
        List<Inventory> actualInventory = objects.stream().map(object -> (Inventory) object).collect(Collectors.toList());

        String skuCategoryAndType = String.join(DELIMITER, request.getSkuCategory(), request.getSkuType());
        List<Inventory> expectedInventory = outboundIds.stream().map(id -> Inventory.builder().uniqueProductId(id)
                .warehouseId(request.getWarehouseId()).companyId(request.getCompanyId()).containerId(inboundRequest.getContainerId())
                .inboundId(inboundRequest.getInboundId()).outboundId(outboundRequest.getOutboundId()).orderId(outboundRequest.getOrderId()).skuCategoryType(skuCategoryAndType).skuCode(request.getSkuCode()).creationTime(clock.millis()).modifiedTime(clock.millis())
                .productionTime(request.getProductionTime()).inventoryStatus(outboundRequest.getInventoryStatus()).build()).collect(Collectors.toList());
        expectedInventory.add(Inventory.builder().uniqueProductId("4")
                .warehouseId(request.getWarehouseId()).companyId(request.getCompanyId()).containerId(inboundRequest.getContainerId())
                .inboundId(inboundRequest.getInboundId()).skuCategoryType(skuCategoryAndType).skuCode(request.getSkuCode()).creationTime(clock.millis()).modifiedTime(clock.millis())
                .productionTime(request.getProductionTime()).inventoryStatus(inboundRequest.getInventoryStatus()).build());
        new ListAssert(actualInventory).usingRecursiveFieldByFieldElementComparatorIgnoringFields("creationTime", "modifiedTime")
                .containsExactlyInAnyOrderElementsOf(expectedInventory);


        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDAO.get(inboundRequest.getWarehouseId(), inboundRequest.getContainerId());
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(true);
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(inboundRequest.getUniqueProductIds().size() - outboundRequest.getUniqueProductIds().size()).warehouseContainerId(warehouseContainerId)
                .creationTime(clock.millis()).modifiedTime(clock.millis()).build();
        RecursiveComparisonConfiguration ignoreFields = RecursiveComparisonConfiguration.builder().withIgnoredFields("creationTime", "modifiedTime").build();
        new ObjectAssert<ContainerCapacity>(containerCapacityActualOp.get()).usingRecursiveComparison(ignoreFields).isEqualTo(containerCapacityExpected);
    }

    @Test
    public void test_outbound_non_generated_ids() {
        InventoryOutboundRequest outboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).companyId(COMPANY_1)
                .inventoryStatus(new Outbound()).containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .orderId(ORDER_1).skuCode(SKU_CODE).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).build();
        Assertions.assertThatExceptionOfType(InconsistentStateException.class).isThrownBy(() -> inventoryDynamoDAO.outbound(outboundRequest));
        String warehouseContainerId = String.join(DELIMITER, outboundRequest.getWarehouseId(), outboundRequest.getContainerId());
        List<KeyPair> keyPairsToLoad = UNIQUE_PRODUCT_IDS_1.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());

        Map<String, List<Object>> inventoryObjects = dynamoDBMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
        List<Object> objects = inventoryObjects.get(INVENTORY_TABLE_NAME);
        new BooleanAssert(objects.size() == 0).isEqualTo(true);

        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDAO.get(outboundRequest.getWarehouseId(), outboundRequest.getContainerId());
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(true);
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(0).warehouseContainerId(warehouseContainerId)
                .creationTime(clock.millis()).modifiedTime(clock.millis()).build();
        RecursiveComparisonConfiguration ignoreFields = RecursiveComparisonConfiguration.builder().withIgnoredFields("creationTime", "modifiedTime").build();
        new ObjectAssert<ContainerCapacity>(containerCapacityActualOp.get()).usingRecursiveComparison(ignoreFields).isEqualTo(containerCapacityExpected);
    }

    @Test
    public void test_outbound_different_company_generated_ids() {
        long productionTime = clock.millis();
        InventoryAddRequest addRequest = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(productionTime).build();

        inventoryDynamoDAO.add(addRequest);
        InventoryInboundRequest inboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        inventoryDynamoDAO.inbound(inboundRequest);

        InventoryOutboundRequest outboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).companyId(COMPANY_2)
                .inventoryStatus(new Outbound()).containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .orderId(ORDER_1).skuCode(SKU_CODE).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).build();
        Assertions.assertThatExceptionOfType(InconsistentStateException.class).isThrownBy(() -> inventoryDynamoDAO.outbound(outboundRequest));

        String warehouseContainerId = String.join(DELIMITER, outboundRequest.getWarehouseId(), outboundRequest.getContainerId());
        List<KeyPair> keyPairsToLoad = UNIQUE_PRODUCT_IDS_1.stream().map(id -> new KeyPair().withHashKey(id)).collect(Collectors.toList());

        Map<String, List<Object>> inventoryObjects = dynamoDBMapper.batchLoad(ImmutableMap.of(Inventory.class, keyPairsToLoad));
        List<Object> objects = inventoryObjects.get(INVENTORY_TABLE_NAME);
        List<Inventory> actualInventory = objects.stream().map(object -> (Inventory) object).collect(Collectors.toList());
        String skuCategoryAndType = String.join(DELIMITER, addRequest.getSkuCategory(), addRequest.getSkuType());
        List<Inventory> expectedInventory = UNIQUE_PRODUCT_IDS_1.stream().map(id -> Inventory.builder().uniqueProductId(id).containerId(inboundRequest.getContainerId())
                .warehouseId(addRequest.getWarehouseId()).companyId(addRequest.getCompanyId()).inboundId(inboundRequest.getInboundId())
                .skuCategoryType(skuCategoryAndType).skuCode(addRequest.getSkuCode()).creationTime(clock.millis()).modifiedTime(clock.millis())
                .productionTime(addRequest.getProductionTime()).inventoryStatus(inboundRequest.getInventoryStatus()).build()).collect(Collectors.toList());
        new ListAssert(actualInventory).usingRecursiveFieldByFieldElementComparatorIgnoringFields("creationTime", "modifiedTime")
                .containsExactlyInAnyOrderElementsOf(expectedInventory);


        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDAO.get(inboundRequest.getWarehouseId(), inboundRequest.getContainerId());
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(true);
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(inboundRequest.getUniqueProductIds().size()).warehouseContainerId(warehouseContainerId)
                .creationTime(clock.millis()).modifiedTime(clock.millis()).build();
        RecursiveComparisonConfiguration ignoreFields = RecursiveComparisonConfiguration.builder().withIgnoredFields("creationTime", "modifiedTime").build();
        new ObjectAssert<ContainerCapacity>(containerCapacityActualOp.get()).usingRecursiveComparison(ignoreFields).isEqualTo(containerCapacityExpected);
    }

    @Test
    public void test_container_status_and_capacities_across_inventory_changes() {
        long productionTime = clock.millis();
        InventoryAddRequest addRequest1 = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(productionTime).build();

        InventoryAddRequest addRequest2 = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_2).productionTime(productionTime).build();

        inventoryDynamoDAO.add(addRequest1);
        inventoryDynamoDAO.add(addRequest2);

        Optional<ContainerCapacity> containerCapacityAfterAdd = containerCapacityDAO.get(addRequest1.getWarehouseId(), CONTAINER_1);
        new StringAssert(containerCapacityAfterAdd.get().getContainerStatus().getStatus()).isEqualTo(new Available().getStatus());
        new IntegerAssert(containerCapacityAfterAdd.get().getCurrentCapacity()).isEqualTo(0);


        InventoryInboundRequest inboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        inventoryDynamoDAO.inbound(inboundRequest);


        Optional<ContainerCapacity> containerCapacityAfterPartialInbound = containerCapacityDAO.get(addRequest1.getWarehouseId(), CONTAINER_1);
        new StringAssert(containerCapacityAfterPartialInbound.get().getContainerStatus().getStatus()).isEqualTo(new PartiallyFilled().getStatus());
        new IntegerAssert(containerCapacityAfterPartialInbound.get().getCurrentCapacity()).isEqualTo(4);



        InventoryInboundRequest fullInboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(ImmutableList.of("5")).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        inventoryDynamoDAO.inbound(fullInboundRequest);


        Optional<ContainerCapacity> containerCapacityAfterFullInbound = containerCapacityDAO.get(addRequest1.getWarehouseId(), CONTAINER_1);
        new StringAssert(containerCapacityAfterFullInbound.get().getContainerStatus().getStatus()).isEqualTo(new Filled().getStatus());
        new IntegerAssert(containerCapacityAfterFullInbound.get().getCurrentCapacity()).isEqualTo(5);

        InventoryOutboundRequest partialOutboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).companyId(COMPANY_1)
                .inventoryStatus(new Outbound()).containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .orderId(ORDER_1).skuCode(SKU_CODE).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).build();

        inventoryDynamoDAO.outbound(partialOutboundRequest);
        Optional<ContainerCapacity> containerCapacityAfterPartialOutbound = containerCapacityDAO.get(addRequest1.getWarehouseId(), CONTAINER_1);
        new StringAssert(containerCapacityAfterPartialOutbound.get().getContainerStatus().getStatus()).isEqualTo(new PartiallyFilled().getStatus());
        new IntegerAssert(containerCapacityAfterPartialOutbound.get().getCurrentCapacity()).isEqualTo(1);


        InventoryOutboundRequest fullOutboundrequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).companyId(COMPANY_1)
                .inventoryStatus(new Outbound()).containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .orderId(ORDER_1).skuCode(SKU_CODE).uniqueProductIds(ImmutableList.of("5")).build();

        inventoryDynamoDAO.outbound(fullOutboundrequest);
        Optional<ContainerCapacity> containerCapacityAfterFullOutbound = containerCapacityDAO.get(addRequest1.getWarehouseId(), CONTAINER_1);
        new StringAssert(containerCapacityAfterFullOutbound.get().getContainerStatus().getStatus()).isEqualTo(new Available().getStatus());
        new IntegerAssert(containerCapacityAfterFullOutbound.get().getCurrentCapacity()).isEqualTo(0);
    }



}
