package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BooleanAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.warehousemanagement.entities.container.ContainerDTO;
import org.warehousemanagement.entities.container.containerstatus.Available;
import org.warehousemanagement.entities.dynamodb.Container;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.warehousemanagement.entities.exceptions.RetriableException;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//TODO getContainers test case
@RunWith(MockitoJUnitRunner.class)
public class TestContainerCapacityDynamoDAOImpl {


    private static final String CONTAINER_1 = "CONTAINER-1";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";

    private static final long TIME_NOW = Instant.now().toEpochMilli();
    public static final String DELIMITER = "<%>";


    ContainerCapacityDynamoDAOImpl containerCapacityDynamoDAO;
    @Mock
    DynamoDBMapper dynamoDBMapper;

    @Captor
    ArgumentCaptor<DynamoDBSaveExpression> dynamoDBSaveExpressionCaptor;

    @Captor
    ArgumentCaptor<ContainerCapacity> containerCapacityArgumentCaptor;

    @Mock
    Clock clock;


    @Before
    public void setupClass() {
        MockitoAnnotations.initMocks(this);
        containerCapacityDynamoDAO = new ContainerCapacityDynamoDAOImpl(dynamoDBMapper, clock);
        Mockito.when(clock.millis()).thenReturn(TIME_NOW);
    }

    @Test
    public void test_get_exists_success() {
        String hashKey = String.join(DELIMITER, WAREHOUSE_1, CONTAINER_1);

        ContainerCapacity expectedEntity = ContainerCapacity.builder().warehouseContainerId(hashKey).currentCapacity(0)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).containerStatus(new Available()).build();
        Mockito.when(dynamoDBMapper.load(ContainerCapacity.class, hashKey)).thenReturn(expectedEntity);
        Optional<ContainerCapacity> containerCapacityOp = containerCapacityDynamoDAO.get(WAREHOUSE_1, CONTAINER_1);
        new BooleanAssert(containerCapacityOp.isPresent()).isEqualTo(true);
        Assertions.assertThat(expectedEntity).usingRecursiveComparison().isEqualTo(containerCapacityOp.get());
        Mockito.verify(dynamoDBMapper).load(Mockito.any(), Mockito.eq(hashKey));
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyZeroInteractions(clock);
    }

    @Test
    public void test_get_input_warehouseId_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.get(null, CONTAINER_1))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("warehouseId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper, clock);
    }


    @Test
    public void test_get_input_containerId_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.get(WAREHOUSE_1, null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("containerId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper, clock);
    }
    @Test
    public void test_init_success() {
        containerCapacityDynamoDAO.init(WAREHOUSE_1, CONTAINER_1);
        Mockito.verify(clock).millis();
        Mockito.verify(dynamoDBMapper).save(containerCapacityArgumentCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyInit(WAREHOUSE_1, CONTAINER_1);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(clock);
    }

    @Test
    public void test_init_already_existing() {
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey rannge key already exists")).when(dynamoDBMapper)
                .save(Mockito.any(ContainerCapacity.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.init(WAREHOUSE_1,CONTAINER_1))
                .withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
        Mockito.verify(clock).millis();
        Mockito.verify(dynamoDBMapper).save(containerCapacityArgumentCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyInit(WAREHOUSE_1, CONTAINER_1);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(clock);
    }

    @Test
    public void test_init_input_warehouseId_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.init(null, CONTAINER_1))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("warehouseId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper, clock);
    }


    @Test
    public void test_init_input_containerId_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.init(WAREHOUSE_1, null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("containerId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper, clock);
    }


    @Test
    public void test_init_retriable_exception() {
        Mockito.doThrow(new InternalServerErrorException("exception ")).when(dynamoDBMapper)
                .save(Mockito.any(ContainerCapacity.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.init(WAREHOUSE_1, CONTAINER_1))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(clock).millis();
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(containerCapacityArgumentCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyInit(WAREHOUSE_1, CONTAINER_1);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(clock);
    }


    private void captorVerifyInit(String warehouseId, String containerId) {
        String hashKey = String.join(DELIMITER, warehouseId, containerId);
        ContainerCapacity actualEntity = containerCapacityArgumentCaptor.getValue();
        DynamoDBSaveExpression actualDdbSaveExpression = dynamoDBSaveExpressionCaptor.getValue();

        DynamoDBSaveExpression expectedDdbSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseContainerId", new ExpectedAttributeValue().withExists(false));
        expectedDdbSaveExpression.withExpected(expected);
        ContainerCapacity expectedEntity = ContainerCapacity.builder().warehouseContainerId(hashKey).currentCapacity(0)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).containerStatus(new Available()).build();

        Assertions.assertThat(actualDdbSaveExpression).usingRecursiveComparison().isEqualTo(expectedDdbSaveExpression);
        Assertions.assertThat(actualEntity).usingRecursiveComparison().isEqualTo(expectedEntity);
    }


}

