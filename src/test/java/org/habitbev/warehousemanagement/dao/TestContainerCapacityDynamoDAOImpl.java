package org.habitbev.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.IntegerAssert;
import org.habitbev.warehousemanagement.entities.container.containerstatus.Available;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.habitbev.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;

import java.time.Clock;
import java.time.Instant;
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
    public void test_get_internal_server_exception() {
        String hashKey = String.join(DELIMITER, WAREHOUSE_1, CONTAINER_1);
        Mockito.when(dynamoDBMapper.load(ContainerCapacity.class, hashKey)).thenThrow(new InternalServerErrorException("internal server exception"));
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(() -> containerCapacityDynamoDAO.get(WAREHOUSE_1, CONTAINER_1)).withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper).load(Mockito.any(), Mockito.eq(hashKey));
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyZeroInteractions(clock);
    }

    @Test
    public void test_get_run_time_exception() {
        String hashKey = String.join(DELIMITER, WAREHOUSE_1, CONTAINER_1);
        Mockito.when(dynamoDBMapper.load(ContainerCapacity.class, hashKey)).thenThrow(new RuntimeException("internal server exception"));
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> containerCapacityDynamoDAO.get(WAREHOUSE_1, CONTAINER_1)).withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper).load(Mockito.any(), Mockito.eq(hashKey));
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyZeroInteractions(clock);
    }

    @Test
    public void test_get_input_warehouseId_null_illegal_argument_exception() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.get(null, CONTAINER_1))
                .withMessageContaining("warehouseId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper, clock);
    }


    @Test
    public void test_get_input_containerId_null_illegal_argument_exception() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> containerCapacityDynamoDAO.get(WAREHOUSE_1, null))
                .withMessageContaining("containerId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper, clock);
    }

    @Test
    public void test_get_existing_quantity_entity_present_success() {
        String hashKey = String.join(DELIMITER, WAREHOUSE_1, CONTAINER_1);

        ContainerCapacity expectedEntity = ContainerCapacity.builder().warehouseContainerId(hashKey).currentCapacity(0)
                .creationTime(TIME_NOW).modifiedTime(TIME_NOW).containerStatus(new Available()).build();
        Mockito.when(dynamoDBMapper.load(ContainerCapacity.class, hashKey)).thenReturn(expectedEntity);
        int existingQuantity = containerCapacityDynamoDAO.getExistingQuantity(WAREHOUSE_1, CONTAINER_1);
        new IntegerAssert(existingQuantity).isEqualTo(expectedEntity.getCurrentCapacity());
        Mockito.verify(dynamoDBMapper).load(Mockito.any(), Mockito.eq(hashKey));
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyZeroInteractions(clock);
    }

    @Test
    public void test_get_existing_quantity_entity_not_present_success() {
        String hashKey = String.join(DELIMITER, WAREHOUSE_1, CONTAINER_1);
        Mockito.when(dynamoDBMapper.load(ContainerCapacity.class, hashKey)).thenReturn(null);
        int existingQuantity = containerCapacityDynamoDAO.getExistingQuantity(WAREHOUSE_1, CONTAINER_1);
        new IntegerAssert(existingQuantity).isEqualTo(0);
        Mockito.verify(dynamoDBMapper).load(Mockito.any(), Mockito.eq(hashKey));
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyZeroInteractions(clock);
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
                .isThrownBy(() -> containerCapacityDynamoDAO.init(WAREHOUSE_1, CONTAINER_1))
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

