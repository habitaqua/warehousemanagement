package org.habitbev.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BooleanAssert;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.dynamodb.Container;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//TODO getContainers test case
@RunWith(MockitoJUnitRunner.class)
public class TestContainerDynamoDAOImpl {


    private static final String CONTAINER_1 = "CONTAINER-1";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";

    private static final Map<String, Integer> PREDEFINED_CAPACITY = ImmutableMap.of("sku1", 20);
    private static final long TIME_NOW = Instant.now().toEpochMilli();


    ContainerDynamoDAOImpl containerDynamoDAO;
    @Mock
    DynamoDBMapper dynamoDBMapper;

    @Mock
    PaginatedQueryList<Container> paginatedQueryList;

    @Captor
    ArgumentCaptor<DynamoDBSaveExpression> dynamoDBSaveExpressionCaptor;

    @Captor
    ArgumentCaptor<DynamoDBQueryExpression> dynamoDBQueryExpressionCaptor;

    @Captor
    ArgumentCaptor<Container> containerArgumentCaptor;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Clock clock;


    @Before
    public void setupClass() {
        MockitoAnnotations.initMocks(this);
        containerDynamoDAO = new ContainerDynamoDAOImpl(dynamoDBMapper, clock, objectMapper);
        Mockito.when(clock.millis()).thenReturn(TIME_NOW);
    }

    @Test
    public void test_add_success() {

        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).predefinedCapacity(PREDEFINED_CAPACITY).build();

        containerDynamoDAO.add(containerDTO);
        Mockito.verify(clock).millis();
        Mockito.verify(dynamoDBMapper).save(containerArgumentCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(containerDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(clock);
        Mockito.verifyZeroInteractions(objectMapper);


    }

    @Test
    public void test_add_already_existing() {
        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).predefinedCapacity(PREDEFINED_CAPACITY).build();
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey rannge key already exists")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsInbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> containerDynamoDAO.add(containerDTO))
                .withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
        Mockito.verify(clock).millis();
        Mockito.verify(dynamoDBMapper).save(containerArgumentCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(containerDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(clock);
        Mockito.verifyZeroInteractions(objectMapper);
    }

    @Test
    public void test_add_input_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> containerDynamoDAO.add(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("containerdto cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper,clock, objectMapper);


    }


    @Test
    public void test_add_throws_retriable_xception() {


        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).predefinedCapacity(PREDEFINED_CAPACITY).build();
        Mockito.doThrow(new InternalServerErrorException("internal server exception")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsInbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> containerDynamoDAO.add(containerDTO))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(clock).millis();
        Mockito.verify(dynamoDBMapper).save(containerArgumentCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(containerDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper,clock);
        Mockito.verifyZeroInteractions(objectMapper);
    }

    @Test
    public void test_get_last_container_input_null() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> containerDynamoDAO.getLastAddedContainer(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class);
        Mockito.verifyZeroInteractions(dynamoDBMapper,clock, objectMapper);
    }

    @Test
    public void test_get_last_container_success() {

        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).predefinedCapacity(PREDEFINED_CAPACITY).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        Container expectedEntity = containerDTO.toDbEntity();
        Mockito.when(paginatedQueryList.stream()).thenReturn(ImmutableList.of(expectedEntity).stream());
        Optional<ContainerDTO> lastAddedContainerOp = containerDynamoDAO.getLastAddedContainer(containerDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(Mockito.eq(Container.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastAddedContainerOp.isPresent()).isEqualTo(true);
        Assertions.assertThat(expectedEntity).usingRecursiveComparison().isEqualTo(lastAddedContainerOp.get());
        captorVerifyQueryForGetLastContainer(containerDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper,paginatedQueryList);
        Mockito.verifyZeroInteractions(objectMapper,clock);


    }

    @Test
    public void test_get_last_container_none_success() {
        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).predefinedCapacity(PREDEFINED_CAPACITY).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Optional<ContainerDTO> lastAddedContainerOp = containerDynamoDAO.getLastAddedContainer(containerDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(Mockito.eq(Container.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastAddedContainerOp.isPresent()).isEqualTo(false);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(paginatedQueryList);
    }

    @Test
    public void test_get_last_container_internal_server_exception() {
        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).predefinedCapacity(PREDEFINED_CAPACITY).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenThrow(new InternalServerErrorException("exception"));
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(() -> containerDynamoDAO.getLastAddedContainer(containerDTO.getWarehouseId())).withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper).query(Mockito.eq(Container.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verifyZeroInteractions(paginatedQueryList);
        captorVerifyQueryForGetLastContainer(containerDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_last_container_exception() {
        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).predefinedCapacity(PREDEFINED_CAPACITY).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenThrow(new RuntimeException("exception"));
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> containerDynamoDAO.getLastAddedContainer(containerDTO.getWarehouseId())).withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper).query(Mockito.eq(Container.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verifyZeroInteractions(paginatedQueryList);
        captorVerifyQueryForGetLastContainer(containerDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }


    private void captorVerifyAdd(ContainerDTO containerDTO) {
        Container actualEntity = containerArgumentCaptor.getValue();
        DynamoDBSaveExpression actualDdbSaveExpression = dynamoDBSaveExpressionCaptor.getValue();

        DynamoDBSaveExpression expectedDdbSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
        expected.put("containerId", new ExpectedAttributeValue().withExists(false));
        expectedDdbSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
        Container expectedEntity = containerDTO.toDbEntity();

        Assertions.assertThat(actualDdbSaveExpression).usingRecursiveComparison().isEqualTo(expectedDdbSaveExpression);
        Assertions.assertThat(actualEntity).usingRecursiveComparison().isEqualTo(expectedEntity);
    }

    private void captorVerifyQueryForGetLastContainer(ContainerDTO containerDTO) {

        DynamoDBQueryExpression actualDdbQueryExpression = dynamoDBQueryExpressionCaptor.getValue();


        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(containerDTO.getWarehouseId()));
        DynamoDBQueryExpression<FinishedGoodsInbound> expectedDynamoDBQueryExpression = new DynamoDBQueryExpression<FinishedGoodsInbound>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);

        Assertions.assertThat(actualDdbQueryExpression).usingRecursiveComparison().isEqualTo(expectedDynamoDBQueryExpression);
    }

}

