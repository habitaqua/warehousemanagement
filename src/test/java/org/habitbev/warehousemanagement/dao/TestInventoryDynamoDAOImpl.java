package org.habitbev.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IntegerAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.habitbev.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.habitbev.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.dynamodb.Inventory;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.habitbev.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.habitbev.warehousemanagement.entities.inventory.InventoryInboundRequest;
import org.habitbev.warehousemanagement.entities.inventory.InventoryOutboundRequest;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Inbound;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.InventoryStatus;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Outbound;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Production;
import org.habitbev.warehousemanagement.helpers.ContainerStatusDeterminer;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestInventoryDynamoDAOImpl {


    private static final String SKU_TYPE = "sku-type";
    private static final String INBOUND_1 = "INBOUND-1";
    private static final String INBOUND_2 = "INBOUND-2";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";
    private static final String COMPANY_1 = "COMPANY-1";
    private static final String COMPANY_2 = "COMPANY-2";


    private static final String CONTAINER_1 = "CONTAINER-1";
    private static final String SKU_CODE = "sku-code";

    private static final long EPOCH_MILLI = Instant.now().toEpochMilli();

    private static final String SKU_CATEGORY = "sku-category";
    private static final String INVENTORY_TABLE_NAME = "inventory";

    private static final String CONTAINER_CAPACITY_TABLE_NAME = "container-capacity";

    private static Inventory INVENTORY = Inventory.builder().uniqueProductId("1")
            .warehouseId(WAREHOUSE_1).skuCode(SKU_CODE).skuCategoryType(SKU_CATEGORY).companyId(COMPANY_1).inventoryStatus(new Production())
            .creationTime(EPOCH_MILLI).productionTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
    private static final Map<String, List<Object>> INVENTORY_OBJECTS = ImmutableMap.of(INVENTORY_TABLE_NAME, ImmutableList.of(INVENTORY));

    private static final Map<String, List<Object>> EMPTY_INVENTORY_OBJECTS = ImmutableMap.of("inventory", ImmutableList.of());
    private static final ImmutableList<String> UNIQUE_PRODUCT_IDS_1 = ImmutableList.of("1", "2", "3", "4");
    private static final ImmutableList<String> UNIQUE_PRODUCT_IDS_2 = ImmutableList.of("5", "6", "7", "8");

    private static final String DELIMITER = "<%>";
    public static final String SKUCATEGORY_TYPE = String.join(DELIMITER, SKU_CATEGORY, SKU_TYPE);
    private static final String OUTBOUND_1 = "OUTBOUND-1";
    private static final int CONTAINER_MAX_CAPACITY = 5;
    private static final String ORDER_1 = "ORDER-1";


    InventoryDynamoDAOImpl inventoryDynamoDAO;
    @Mock
    DynamoDBMapper inventoryDynamoDbMapper;

    @Mock
    AmazonDynamoDBClient amazonDynamoDBClient;

    @Mock
    ContainerCapacityDAO containerCapacityDAO;

    @Mock
    ContainerStatusDeterminer containerStatusDeterminer;


    @Captor
    ArgumentCaptor<ImmutableMap<Class<?>, List<KeyPair>>> batchLoadArgumentCaptor;

    @Captor
    ArgumentCaptor<List<Inventory>> batchSaveArgumentCaptor;


    @Captor
    ArgumentCaptor<TransactWriteItemsRequest> transactWriteItemsRequestArgumentCaptor;

    @Captor
    ArgumentCaptor<FinishedGoodsInbound> finishedGoodsInboundCaptor;

    @Mock
    Clock clock;


    @Before
    public void setupClass() {
        MockitoAnnotations.initMocks(this);
        when(clock.millis()).thenReturn(EPOCH_MILLI);
        inventoryDynamoDAO = new InventoryDynamoDAOImpl(amazonDynamoDBClient, inventoryDynamoDbMapper, containerStatusDeterminer, containerCapacityDAO, clock);
    }

    @Test
    public void test_add_input_null() {

        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> inventoryDynamoDAO.add(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class);
        verifyZeroInteractions(amazonDynamoDBClient, inventoryDynamoDbMapper, containerCapacityDAO, clock);
    }

    @Test
    public void test_add_resource_already_exist_exception() {

        InventoryAddRequest addRequest = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(EPOCH_MILLI).build();
        when(inventoryDynamoDbMapper.batchLoad(anyMap())).thenReturn(INVENTORY_OBJECTS);

        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class).isThrownBy(() -> inventoryDynamoDAO.add(addRequest));
        verify(inventoryDynamoDbMapper).batchLoad(batchLoadArgumentCaptor.capture());
        assertInventoryBatchLoadArguments(addRequest);
        verify(inventoryDynamoDbMapper, never()).batchSave(anyList());
        verify(clock, never()).millis();
        verifyZeroInteractions(containerCapacityDAO, amazonDynamoDBClient);
    }


    @Test
    public void test_add_success_no_failed_items() {
        InventoryAddRequest addRequest = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(EPOCH_MILLI).build();
        when(inventoryDynamoDbMapper.batchLoad(anyMap())).thenReturn(EMPTY_INVENTORY_OBJECTS);
        when(inventoryDynamoDbMapper.batchSave(anyList())).thenReturn(ImmutableList.of());
        List<String> successfulProductIds = inventoryDynamoDAO.add(addRequest);
        verify(inventoryDynamoDbMapper).batchLoad(batchLoadArgumentCaptor.capture());
        verify(inventoryDynamoDbMapper).batchSave(batchSaveArgumentCaptor.capture());
        assertInventoryBatchLoadArguments(addRequest);
        assertInventoryBatchSaveArguments(addRequest);
        verify(clock).millis();
        verify(inventoryDynamoDbMapper, never()).marshallIntoObject(any(), anyMap());
        verifyZeroInteractions(containerCapacityDAO, amazonDynamoDBClient);
        verifyNoMoreInteractions(inventoryDynamoDbMapper);
        new ListAssert<String>(successfulProductIds).containsExactlyInAnyOrderElementsOf(addRequest.getUniqueProductIds());
    }

    @Test
    public void test_add_success_one_partial_failed_items() {
        InventoryAddRequest addRequest = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(EPOCH_MILLI).build();
        List<String> uniqueProductIds = addRequest.getUniqueProductIds();
        when(inventoryDynamoDbMapper.batchLoad(anyMap())).thenReturn(EMPTY_INVENTORY_OBJECTS);
        ImmutableList<String> failedIds = ImmutableList.of("1");
        when(inventoryDynamoDbMapper.marshallIntoObject(any(), anyMap())).thenReturn(INVENTORY);
        when(inventoryDynamoDbMapper.batchSave(anyList())).thenReturn(getFailedBatches(failedIds));

        List<String> successfulProductIds = inventoryDynamoDAO.add(addRequest);

        verify(inventoryDynamoDbMapper).batchLoad(batchLoadArgumentCaptor.capture());
        verify(inventoryDynamoDbMapper).batchSave(batchSaveArgumentCaptor.capture());
        verify(inventoryDynamoDbMapper).marshallIntoObject(any(), anyMap());
        assertInventoryBatchLoadArguments(addRequest);
        assertInventoryBatchSaveArguments(addRequest);
        verify(clock).millis();
        verifyZeroInteractions(containerCapacityDAO, amazonDynamoDBClient);
        verifyNoMoreInteractions(inventoryDynamoDbMapper, clock);
        new IntegerAssert(successfulProductIds.size()).isEqualTo(uniqueProductIds.size() - failedIds.size());
        new ListAssert<String>(successfulProductIds).doesNotContainAnyElementsOf(failedIds);
    }

    @Test
    public void test_add_internal_server_exception() {
        InventoryAddRequest addRequest = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(EPOCH_MILLI).build();
        when(inventoryDynamoDbMapper.batchLoad(anyMap())).thenThrow(new InternalServerErrorException("internal servicer exception"));

        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(() -> inventoryDynamoDAO.add(addRequest))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        verify(inventoryDynamoDbMapper).batchLoad(batchLoadArgumentCaptor.capture());
        assertInventoryBatchLoadArguments(addRequest);
        verify(inventoryDynamoDbMapper, never()).batchSave(anyList());
        verify(inventoryDynamoDbMapper, never()).marshallIntoObject(any(), anyMap());
        verify(clock, never()).millis();
        verifyZeroInteractions(containerCapacityDAO, amazonDynamoDBClient);
    }


    @Test
    public void test_add_exception() {
        InventoryAddRequest addRequest = InventoryAddRequest.builder().warehouseId(WAREHOUSE_1).companyId(COMPANY_1)
                .inventoryStatus(new Production()).skuType(SKU_TYPE).skuCode(SKU_CODE).skuCategory(SKU_CATEGORY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).productionTime(EPOCH_MILLI).build();
        when(inventoryDynamoDbMapper.batchLoad(anyMap())).thenReturn(EMPTY_INVENTORY_OBJECTS);
        when(inventoryDynamoDbMapper.batchSave(anyList())).thenThrow(new RuntimeException("exception"));
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inventoryDynamoDAO.add(addRequest)).withCauseExactlyInstanceOf(RuntimeException.class);
        verify(inventoryDynamoDbMapper).batchLoad(batchLoadArgumentCaptor.capture());
        verify(inventoryDynamoDbMapper).batchSave(batchSaveArgumentCaptor.capture());
        assertInventoryBatchLoadArguments(addRequest);
        assertInventoryBatchSaveArguments(addRequest);
        verify(clock).millis();
        verify(inventoryDynamoDbMapper, never()).marshallIntoObject(any(), anyMap());
        verifyZeroInteractions(containerCapacityDAO, amazonDynamoDBClient);
        verifyNoMoreInteractions(inventoryDynamoDbMapper);
    }

    @Test
    public void test_inbound_input_null() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> inventoryDynamoDAO.inbound(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class);
        verifyZeroInteractions(amazonDynamoDBClient, inventoryDynamoDbMapper, containerCapacityDAO, clock);
    }

    @Test
    public void test_inbound_success() {

        InventoryInboundRequest inboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        int assumedExistingCapacity = 1;
        when(containerCapacityDAO.getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()))).thenReturn(assumedExistingCapacity);
        int newCapacity = assumedExistingCapacity+inboundRequest.getUniqueProductIds().size();
        when(containerStatusDeterminer.determineStatus(eq(newCapacity), eq(inboundRequest.getContainerMaxCapacity()))).thenReturn(new PartiallyFilled());
        TransactWriteItemsRequest expectedTransactWriteItemsRequest = getExpectedTransactWriteItemsRequest(inboundRequest, assumedExistingCapacity, newCapacity);
        inventoryDynamoDAO.inbound(inboundRequest);
        verify(amazonDynamoDBClient).transactWriteItems(transactWriteItemsRequestArgumentCaptor.capture());
        TransactWriteItemsRequest actualTransactWriteItemsRequest = transactWriteItemsRequestArgumentCaptor.getValue();
        new ObjectAssert(expectedTransactWriteItemsRequest).usingRecursiveComparison().isEqualTo(actualTransactWriteItemsRequest);
        verify(containerStatusDeterminer).determineStatus(eq(newCapacity), eq(inboundRequest.getContainerMaxCapacity()));
        verify(containerCapacityDAO).getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()));
        verify(clock, times(5)).millis();
        verifyNoMoreInteractions(clock, containerCapacityDAO, containerStatusDeterminer, amazonDynamoDBClient);
        verifyZeroInteractions(inventoryDynamoDbMapper);
    }


    @Test
    public void test_inbound_transaction_cancelled_exception() {
        InventoryInboundRequest inboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        int assumedExistingCapacity = 1;
        when(containerCapacityDAO.getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()))).thenReturn(assumedExistingCapacity);
        int newCapacity = assumedExistingCapacity+inboundRequest.getUniqueProductIds().size();
        when(containerStatusDeterminer.determineStatus(eq(newCapacity), eq(inboundRequest.getContainerMaxCapacity()))).thenReturn(new PartiallyFilled());
        when(amazonDynamoDBClient.transactWriteItems(transactWriteItemsRequestArgumentCaptor.capture())).thenThrow(new TransactionCanceledException("transaction cancelled"));
        TransactWriteItemsRequest expectedTransactWriteItemsRequest = getExpectedTransactWriteItemsRequest(inboundRequest, assumedExistingCapacity, newCapacity);
        Assertions.assertThatExceptionOfType(InconsistentStateException.class).isThrownBy(()->inventoryDynamoDAO.inbound(inboundRequest))
                .withCauseExactlyInstanceOf(TransactionCanceledException.class);
        verify(amazonDynamoDBClient).transactWriteItems(transactWriteItemsRequestArgumentCaptor.capture());
        TransactWriteItemsRequest actualTransactWriteItemsRequest = transactWriteItemsRequestArgumentCaptor.getValue();
        new ObjectAssert(expectedTransactWriteItemsRequest).usingRecursiveComparison().isEqualTo(actualTransactWriteItemsRequest);
        verify(containerStatusDeterminer).determineStatus(eq(newCapacity), eq(inboundRequest.getContainerMaxCapacity()));
        verify(containerCapacityDAO).getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()));
        verify(clock, times(5)).millis();
        verifyNoMoreInteractions(clock, containerCapacityDAO, containerStatusDeterminer, amazonDynamoDBClient);
        verifyZeroInteractions(inventoryDynamoDbMapper);
    }


    @Test
    public void test_inbound_internal_server_exception() {
        InventoryInboundRequest inboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        when(containerCapacityDAO.getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()))).thenThrow(new InternalServerErrorException("internal server exception"));
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(()->inventoryDynamoDAO.inbound(inboundRequest))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        verify(amazonDynamoDBClient, never()).transactWriteItems(any(TransactWriteItemsRequest.class));
        verify(containerStatusDeterminer, never()).determineStatus(anyInt(), anyInt());
        verify(containerCapacityDAO).getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()));
        verify(clock, never()).millis();
        verifyNoMoreInteractions(containerCapacityDAO);
        verifyZeroInteractions(inventoryDynamoDbMapper);
    }

    @Test
    public void test_inbound_exception() {
        InventoryInboundRequest inboundRequest = InventoryInboundRequest.builder().inboundId(INBOUND_1).inventoryStatus(new Inbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY).uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        when(containerCapacityDAO.getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()))).thenThrow(new RuntimeException("runtime exception"));
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(()->inventoryDynamoDAO.inbound(inboundRequest))
                .withCauseExactlyInstanceOf(RuntimeException.class);
        verify(amazonDynamoDBClient, never()).transactWriteItems(any(TransactWriteItemsRequest.class));
        verify(containerStatusDeterminer, never()).determineStatus(anyInt(), anyInt());
        verify(containerCapacityDAO).getExistingQuantity(eq(inboundRequest.getWarehouseId()), eq(inboundRequest.getContainerId()));
        verify(clock, never()).millis();
        verifyNoMoreInteractions(containerCapacityDAO);
        verifyZeroInteractions(inventoryDynamoDbMapper);
    }

    @Test
    public void test_outbound_input_null() {

        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> inventoryDynamoDAO.outbound(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class);
        verifyZeroInteractions(amazonDynamoDBClient, inventoryDynamoDbMapper, containerCapacityDAO, clock);
    }

    @Test
    public void test_outbound_success() {
        InventoryOutboundRequest outboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).inventoryStatus(new Outbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).orderId(ORDER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        int assumedExistingCapacity = 5;
        when(containerCapacityDAO.getExistingQuantity(eq(outboundRequest.getWarehouseId()), eq(outboundRequest.getContainerId()))).thenReturn(assumedExistingCapacity);
        int newCapacity = assumedExistingCapacity-outboundRequest.getUniqueProductIds().size();
        when(containerStatusDeterminer.determineStatus(eq(newCapacity), eq(outboundRequest.getContainerMaxCapacity()))).thenReturn(new PartiallyFilled());
        TransactWriteItemsRequest expectedTransactWriteItemsRequest = getExpectedTransactWriteItemsRequest(outboundRequest, assumedExistingCapacity, newCapacity);
        inventoryDynamoDAO.outbound(outboundRequest);
        verify(amazonDynamoDBClient).transactWriteItems(transactWriteItemsRequestArgumentCaptor.capture());
        TransactWriteItemsRequest actualTransactWriteItemsRequest = transactWriteItemsRequestArgumentCaptor.getValue();
        new ObjectAssert(expectedTransactWriteItemsRequest).usingRecursiveComparison().isEqualTo(actualTransactWriteItemsRequest);
        verify(containerStatusDeterminer).determineStatus(eq(newCapacity), eq(outboundRequest.getContainerMaxCapacity()));
        verify(containerCapacityDAO).getExistingQuantity(eq(outboundRequest.getWarehouseId()), eq(outboundRequest.getContainerId()));
        verify(clock, times(5)).millis();
        verifyNoMoreInteractions(clock, containerCapacityDAO, containerStatusDeterminer, amazonDynamoDBClient);
        verifyZeroInteractions(inventoryDynamoDbMapper);

    }

    @Test
    public void test_outbound_transaction_cancelled() {
        InventoryOutboundRequest outboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).inventoryStatus(new Outbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).orderId(ORDER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();
        int assumedExistingCapacity = 5;
        when(containerCapacityDAO.getExistingQuantity(eq(outboundRequest.getWarehouseId()), eq(outboundRequest.getContainerId()))).thenReturn(assumedExistingCapacity);
        int newCapacity = assumedExistingCapacity-outboundRequest.getUniqueProductIds().size();
        when(containerStatusDeterminer.determineStatus(eq(newCapacity), eq(outboundRequest.getContainerMaxCapacity()))).thenReturn(new PartiallyFilled());
        when(amazonDynamoDBClient.transactWriteItems(transactWriteItemsRequestArgumentCaptor.capture())).thenThrow(new TransactionCanceledException("transaction cancelled"));
        TransactWriteItemsRequest expectedTransactWriteItemsRequest = getExpectedTransactWriteItemsRequest(outboundRequest, assumedExistingCapacity, newCapacity);
        Assertions.setMaxStackTraceElementsDisplayed(100);
        Assertions.assertThatExceptionOfType(InconsistentStateException.class).isThrownBy(()->inventoryDynamoDAO.outbound(outboundRequest))
                .withCauseExactlyInstanceOf(TransactionCanceledException.class);
        verify(amazonDynamoDBClient).transactWriteItems(transactWriteItemsRequestArgumentCaptor.capture());
        TransactWriteItemsRequest actualTransactWriteItemsRequest = transactWriteItemsRequestArgumentCaptor.getValue();
        new ObjectAssert(expectedTransactWriteItemsRequest).usingRecursiveComparison().isEqualTo(actualTransactWriteItemsRequest);
        verify(containerStatusDeterminer).determineStatus(eq(newCapacity), eq(outboundRequest.getContainerMaxCapacity()));
        verify(containerCapacityDAO).getExistingQuantity(eq(outboundRequest.getWarehouseId()), eq(outboundRequest.getContainerId()));
        verify(clock, times(5)).millis();
        verifyNoMoreInteractions(clock, containerCapacityDAO, containerStatusDeterminer, amazonDynamoDBClient);
        verifyZeroInteractions(inventoryDynamoDbMapper);
    }


    @Test
    public void test_outbound_internal_server_exception() {
        InventoryOutboundRequest outboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).inventoryStatus(new Outbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).orderId(ORDER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();  when(containerCapacityDAO.getExistingQuantity(eq(outboundRequest.getWarehouseId()),
                eq(outboundRequest.getContainerId()))).thenThrow(new InternalServerErrorException("internal server exception"));
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(()->inventoryDynamoDAO.outbound(outboundRequest))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        verify(amazonDynamoDBClient, never()).transactWriteItems(any(TransactWriteItemsRequest.class));
        verify(containerStatusDeterminer, never()).determineStatus(anyInt(), anyInt());
        verify(containerCapacityDAO).getExistingQuantity(eq(outboundRequest.getWarehouseId()), eq(outboundRequest.getContainerId()));
        verify(clock, never()).millis();
        verifyNoMoreInteractions(containerCapacityDAO);
        verifyZeroInteractions(inventoryDynamoDbMapper);
    }


    @Test
    public void test_outbound_exception() {
        InventoryOutboundRequest outboundRequest = InventoryOutboundRequest.builder().outboundId(OUTBOUND_1).inventoryStatus(new Outbound())
                .skuCode(SKU_CODE).containerId(CONTAINER_1).orderId(ORDER_1).containerMaxCapacity(CONTAINER_MAX_CAPACITY)
                .uniqueProductIds(UNIQUE_PRODUCT_IDS_1).companyId(COMPANY_1).warehouseId(WAREHOUSE_1).build();  when(containerCapacityDAO.getExistingQuantity(eq(outboundRequest.getWarehouseId()),
                eq(outboundRequest.getContainerId()))).thenThrow(new RuntimeException("runtime exception"));
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(()->inventoryDynamoDAO.outbound(outboundRequest))
                .withCauseExactlyInstanceOf(RuntimeException.class);
        verify(amazonDynamoDBClient, never()).transactWriteItems(any(TransactWriteItemsRequest.class));
        verify(containerStatusDeterminer, never()).determineStatus(anyInt(), anyInt());
        verify(containerCapacityDAO).getExistingQuantity(eq(outboundRequest.getWarehouseId()), eq(outboundRequest.getContainerId()));
        verify(clock, never()).millis();
        verifyNoMoreInteractions(containerCapacityDAO);
        verifyZeroInteractions(inventoryDynamoDbMapper);
    }

    @Test
    public void test_move_inventory_input_null() {

    }

    @Test
    public void test_move_inventory_success() {

    }

    @Test
    public void test_move_inventory_transaction_cancelled() {

    }


    @Test
    public void test_move_inventory_internal_server_exception() {

    }


    @Test
    public void test_move_inventory_exception() {

    }

    private void assertInventoryBatchLoadArguments(InventoryAddRequest addRequest) {
        ImmutableMap<Class<?>, List<KeyPair>> actualBatchLoadArgument = batchLoadArgumentCaptor.getValue();
        List<KeyPair> keyPairsToLoad = addRequest.getUniqueProductIds().stream().map(id -> new KeyPair().withHashKey(id).withRangeKey(addRequest.getCompanyId())).collect(Collectors.toList());
        ImmutableMap<Class<?>, List<KeyPair>> expectedBatchLoadArgument = ImmutableMap.of(Inventory.class, keyPairsToLoad);
        new IntegerAssert(actualBatchLoadArgument.keySet().size()).isEqualTo(expectedBatchLoadArgument.keySet().size());
        new IntegerAssert(actualBatchLoadArgument.values().size()).isEqualTo(expectedBatchLoadArgument.values().size());
    }


    private void assertInventoryBatchSaveArguments(InventoryAddRequest addRequest) {
        List<Inventory> expectedBatchSaveArgument = addRequest.getUniqueProductIds().stream().map(productId -> Inventory.builder().uniqueProductId(productId)
                .companyId(addRequest.getCompanyId()).warehouseId(addRequest.getWarehouseId())
                .inventoryStatus(new Production()).creationTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI)
                .productionTime(addRequest.getProductionTime()).skuCode(addRequest.getSkuCode())
                .skuCategoryType(SKUCATEGORY_TYPE).build()).collect(toList());

        List<Inventory> actualBatchSaveArgument = batchSaveArgumentCaptor.getValue();
        new ListAssert<Inventory>(expectedBatchSaveArgument).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrderElementsOf(actualBatchSaveArgument);
    }

    private List<DynamoDBMapper.FailedBatch> getFailedBatches(List<String> failedIds) {
        return failedIds.stream().map(failedId -> getFailedBatch(failedId)).collect(toList());
    }

    private DynamoDBMapper.FailedBatch getFailedBatch(String failedId) {
        DynamoDBMapper.FailedBatch failedBatch = new DynamoDBMapper.FailedBatch();
        PutRequest putRequest = new PutRequest();
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("uniqueProductId", new AttributeValue(failedId));
        item.put("warehouseId", new AttributeValue(WAREHOUSE_1));
        item.put("companyId", new AttributeValue(COMPANY_1));
        item.put("inventoryStatus", new AttributeValue(new Production().getStatus()));
        item.put("skuCode", new AttributeValue(SKU_CODE));
        item.put("skuCategoryType", new AttributeValue(SKUCATEGORY_TYPE));
        item.put("creationTime", new AttributeValue().withN(String.valueOf(EPOCH_MILLI)));
        item.put("modifiedTime", new AttributeValue().withN(String.valueOf(EPOCH_MILLI)));
        item.put("productionTime", new AttributeValue().withN(String.valueOf(EPOCH_MILLI)));
        putRequest.setItem(item);
        WriteRequest writeRequest = new WriteRequest().withPutRequest(putRequest);
        failedBatch.setUnprocessedItems(ImmutableMap.of(INVENTORY_TABLE_NAME, ImmutableList.of(writeRequest)));
        return failedBatch;
    }


    private TransactWriteItemsRequest getExpectedTransactWriteItemsRequest(InventoryInboundRequest inboundRequest, int existingCapacity, int newCapacity) {
        List<TransactWriteItem> transactWrites = inboundRequest.getUniqueProductIds().stream()
                .map(itemId -> new TransactWriteItem().withUpdate(constructUpdateExpression(itemId, inboundRequest))).collect(toList());
        Update updateCapacityExpression = constructUpdateContainerCapacityExpression(inboundRequest.getWarehouseId(), inboundRequest.getContainerId(), existingCapacity, newCapacity
                , inboundRequest.getContainerMaxCapacity());
        transactWrites.add(new TransactWriteItem().withUpdate(updateCapacityExpression));
        TransactWriteItemsRequest addInventoryTransaction = new TransactWriteItemsRequest()
                .withTransactItems(transactWrites)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        return addInventoryTransaction;
    }

    private TransactWriteItemsRequest getExpectedTransactWriteItemsRequest(InventoryOutboundRequest outboundRequest, int existingCapacity, int newCapacity) {
        List<TransactWriteItem> transactWrites = outboundRequest.getUniqueProductIds().stream()
                .map(itemId -> new TransactWriteItem().withUpdate(constructUpdateExpression(itemId, outboundRequest))).collect(toList());
        Update updateCapacityExpression = constructUpdateContainerCapacityExpression(outboundRequest.getWarehouseId(), outboundRequest.getContainerId(), existingCapacity, newCapacity
                , outboundRequest.getContainerMaxCapacity());
        transactWrites.add(new TransactWriteItem().withUpdate(updateCapacityExpression));
        TransactWriteItemsRequest addInventoryTransaction = new TransactWriteItemsRequest()
                .withTransactItems(transactWrites)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        return addInventoryTransaction;
    }

    private Update constructUpdateExpression(String itemId, InventoryOutboundRequest inventoryOutboundRequest) {

        Map<String, AttributeValue> inventoryKey = new HashMap<>();
        inventoryKey.put("uniqueProductId", new AttributeValue().withS(itemId));
        inventoryKey.put("companyId", new AttributeValue().withS(inventoryOutboundRequest.getCompanyId()));

        InventoryStatus newInventoryStatus = inventoryOutboundRequest.getInventoryStatus();

        Map<String, AttributeValue> updatedAttributes = new HashMap<>();

        updatedAttributes.put(":new_status", new AttributeValue(newInventoryStatus.getStatus()));
        updatedAttributes.put(":outbound_id", new AttributeValue(inventoryOutboundRequest.getOutboundId()));
        updatedAttributes.put(":order_id", new AttributeValue(inventoryOutboundRequest.getOrderId()));
        updatedAttributes.put(":container_id", new AttributeValue(inventoryOutboundRequest.getContainerId()));
        updatedAttributes.put(":warehouse_id", new AttributeValue(inventoryOutboundRequest.getWarehouseId()));

        long currentTime = EPOCH_MILLI;
        updatedAttributes.put(":modified_time", new AttributeValue().withN(String.valueOf(currentTime)));
        String previousStatus = getAppendedStatusString(newInventoryStatus, updatedAttributes);

        Update update = new Update().withTableName(INVENTORY_TABLE_NAME).withKey(inventoryKey)
                .withExpressionAttributeValues(updatedAttributes)
                .withUpdateExpression("SET inventoryStatus = :new_status , orderId = :order_id , outboundId =:outbound_id , " +
                        "modifiedTime= :modified_time")
                .withConditionExpression("inventoryStatus IN (" + previousStatus + ") AND containerId = :container_id" +
                        " AND warehouseId = :warehouse_id");
        return update;
    }

    private Update constructUpdateContainerCapacityExpression(String warehouseId, String containerId, int existingCapacity,
                                                              int newCapacity, int maxCapacity) {

        ContainerStatus newContainerStatus = new PartiallyFilled();
        String warehouseContainerId = String.join(DELIMITER, warehouseId, containerId);
        Map<String, AttributeValue> containerCapacityTableKey = new HashMap<>();
        containerCapacityTableKey
                .put("warehouseContainerId", new AttributeValue().withS(warehouseContainerId));
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":new_quantity",
                new AttributeValue().withN(String.valueOf(newCapacity)));
        expressionAttributeValues
                .put(":existing_quantity", new AttributeValue().withN(String.valueOf(existingCapacity)));
        expressionAttributeValues.put(":new_status", new AttributeValue().withS(newContainerStatus.toString()));
        expressionAttributeValues.put(":modified_time", new AttributeValue().withN(String.valueOf(EPOCH_MILLI)));
        String previousStatus = getAppendedStatusString(newContainerStatus, expressionAttributeValues);


        Update update = new Update()
                .withTableName(CONTAINER_CAPACITY_TABLE_NAME)
                .withKey(containerCapacityTableKey)
                .withUpdateExpression("SET currentCapacity = :new_quantity , containerStatus= :new_status , modifiedTime = :modified_time")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConditionExpression("currentCapacity = :existing_quantity and containerStatus IN (" + previousStatus + ")");
        return update;
    }

    private Update constructUpdateExpression(String itemId, InventoryInboundRequest inventoryInboundRequest) {

        Map<String, AttributeValue> inventoryKey = new HashMap<>();
        inventoryKey.put("uniqueProductId", new AttributeValue().withS(itemId));
        inventoryKey.put("companyId", new AttributeValue().withS(inventoryInboundRequest.getCompanyId()));

        InventoryStatus newInventoryStatus = inventoryInboundRequest.getInventoryStatus();
        Map<String, AttributeValue> updatedAttributes = new HashMap<>();
        updatedAttributes.put(":new_status", new AttributeValue(newInventoryStatus.getStatus()));
        updatedAttributes.put(":inbound_id", new AttributeValue(inventoryInboundRequest.getInboundId()));
        updatedAttributes.put(":container_id", new AttributeValue(inventoryInboundRequest.getContainerId()));
        updatedAttributes.put(":warehouse_id", new AttributeValue(inventoryInboundRequest.getWarehouseId()));
        updatedAttributes.put(":modified_time", new AttributeValue().withN(String.valueOf(EPOCH_MILLI)));

        String previousStatus = getAppendedStatusString(newInventoryStatus, updatedAttributes);


        Update update = new Update().withTableName(INVENTORY_TABLE_NAME).withKey(inventoryKey)
                .withExpressionAttributeValues(updatedAttributes)
                .withUpdateExpression("SET inventoryStatus = :new_status , inboundId = :inbound_id , containerId =:container_id , " +
                        "modifiedTime= :modified_time")
                .withConditionExpression("inventoryStatus IN (" + previousStatus + ") AND warehouseId = :warehouse_id");
        return update;
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
                    new AttributeValue().withS(containerStatus.previousStates().get(i).toString()));
            previousValues.add(key);
        }
        String previousStatus = String.join(", ", previousValues);
        return previousStatus;
    }
}


