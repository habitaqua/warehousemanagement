package org.habitbev.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestInboundDynamoDAOImpl {


    private static final String INBOUND_1 = "INBOUND-1";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";

    private static final String USER_ID = "user-1";
    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();


    InboundDynamoDAOImpl inboundDynamoDAO;
    @Mock
    DynamoDBMapper dynamoDBMapper;

    @Mock
    PaginatedQueryList<FinishedGoodsInbound> paginatedQueryList;

    @Captor
    ArgumentCaptor<DynamoDBSaveExpression> dynamoDBSaveExpressionCaptor;

    @Captor
    ArgumentCaptor<DynamoDBQueryExpression> dynamoDBQueryExpressionCaptor;

    @Captor
    ArgumentCaptor<FinishedGoodsInbound> finishedGoodsInboundCaptor;




    @Before
    public void setupClass() {
        MockitoAnnotations.initMocks(this);
        inboundDynamoDAO = new InboundDynamoDAOImpl(dynamoDBMapper);
    }

    @Test
    public void test_add_success() {
        long startTime = EPOCH_MILLI;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDynamoDAO.add(fgInboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_add_already_existing() {
        long startTime = EPOCH_MILLI;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey rannge key already exists")).when(dynamoDBMapper)
                .save(any(FinishedGoodsInbound.class), any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> inboundDynamoDAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundDynamoDAO.add(null))
                .withMessageContaining("fgInboundDTO cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_status_null_illegal_argument_exception() {
        long startTime = EPOCH_MILLI;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundDynamoDAO.add(fgInboundDTO))
                .withMessageContaining("fgInboundDTO.status cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_starttime_null_illegal_argument_exception() {
        long startTime = EPOCH_MILLI;

        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .modifiedTime(startTime).userId(USER_ID).build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundDynamoDAO.add(fgInboundDTO))
                .withMessageContaining("fgInboundDTO.startTime cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_userId_null_illegal_argument_exception() {
        long startTime = EPOCH_MILLI;

        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundDynamoDAO.add(fgInboundDTO))
                .withMessageContaining("fgInboundDTO.userId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_throws_retriable_xception() {
        long startTime = EPOCH_MILLI;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        Mockito.doThrow(new InternalServerErrorException("internal server exception")).when(dynamoDBMapper)
                .save(any(FinishedGoodsInbound.class), any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> inboundDynamoDAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }
    @Test
    public void test_add_throws_non_retriable_xception() {
        long startTime = EPOCH_MILLI;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        Mockito.doThrow(new RuntimeException("run time exception")).when(dynamoDBMapper)
                .save(any(FinishedGoodsInbound.class), any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDynamoDAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_update_input_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundDynamoDAO.update(null)).withMessageContaining("fgInboundDTO cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        inboundDynamoDAO.update(fgInboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_partial_update_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).warehouseId(WAREHOUSE_1)
                .modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        inboundDynamoDAO.update(fgInboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_no_key_found() {
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .userId(USER_ID).build();
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey range key is not found ")).when(dynamoDBMapper)
                .save(any(FinishedGoodsInbound.class), any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDynamoDAO.update(fgInboundDTO))
                .withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }


    @Test
    public void test_update_internal_server_exception() {
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .userId(USER_ID).build();
        Mockito.doThrow(new InternalServerErrorException("exception")).when(dynamoDBMapper)
                .save(any(FinishedGoodsInbound.class), any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> inboundDynamoDAO.update(fgInboundDTO))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_runtime_exception() {
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .userId(USER_ID).build();
        Mockito.doThrow(new RuntimeException("exception")).when(dynamoDBMapper)
                .save(any(FinishedGoodsInbound.class), any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDynamoDAO.update(fgInboundDTO))
                .withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }
    @Test
    public void test_get_last_inbound_input_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundDynamoDAO.getLastInbound(null));
        Mockito.verifyZeroInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_last_inbound_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        when(dynamoDBMapper.query(any(), any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        FinishedGoodsInbound expectedEntity = fgInboundDTO.toDbEntity();
        when(paginatedQueryList.stream()).thenReturn(ImmutableList.of(expectedEntity).stream());
        Optional<FinishedGoodsInbound> lastInboundOp = inboundDynamoDAO.getLastInbound(fgInboundDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastInboundOp.isPresent()).isEqualTo(true);
        Assertions.assertThat(expectedEntity).usingRecursiveComparison().isEqualTo(lastInboundOp.get());
        captorVerifyQuery(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(paginatedQueryList);


    }

    @Test
    public void test_get_last_inbound_none_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        when(dynamoDBMapper.query(any(), any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Optional<FinishedGoodsInbound> lastInboundOp = inboundDynamoDAO.getLastInbound(fgInboundDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastInboundOp.isPresent()).isEqualTo(false);
        captorVerifyQuery(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(paginatedQueryList);
    }
    @Test
    public void test_get_last_inbound_internal_server_exception() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        when(dynamoDBMapper.query(any(), any(DynamoDBQueryExpression.class))).thenThrow(new InternalServerErrorException("exception"));
        when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(() -> inboundDynamoDAO.getLastInbound(fgInboundDTO.getWarehouseId())).withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).query(eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verifyZeroInteractions(paginatedQueryList);
        captorVerifyQuery(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_last_inbound_exception() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        when(dynamoDBMapper.query(any(), any(DynamoDBQueryExpression.class))).thenThrow(new RuntimeException("exception"));
        when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> inboundDynamoDAO.getLastInbound(fgInboundDTO.getWarehouseId())).withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper).query(eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verifyZeroInteractions(paginatedQueryList);
        captorVerifyQuery(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }


    private void captorVerifyAdd(FGInboundDTO fgInboundDTO) {
        FinishedGoodsInbound actualEntity = finishedGoodsInboundCaptor.getValue();
        DynamoDBSaveExpression actualDdbSaveExpression = dynamoDBSaveExpressionCaptor.getValue();

        DynamoDBSaveExpression expectedDdbSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
        expected.put("inboundId", new ExpectedAttributeValue().withExists(false));
        expectedDdbSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
        FinishedGoodsInbound expectedEntity = fgInboundDTO.toDbEntity();
        Assertions.assertThat(actualDdbSaveExpression).usingRecursiveComparison().isEqualTo(expectedDdbSaveExpression);
        Assertions.assertThat(actualEntity).usingRecursiveComparison().isEqualTo(expectedEntity);
    }

    private void captorVerifyUpdate(FGInboundDTO fgInboundDTO) {
        FinishedGoodsInbound actualEntity = finishedGoodsInboundCaptor.getValue();
        DynamoDBSaveExpression actualDdbSaveExpression = dynamoDBSaveExpressionCaptor.getValue();

        DynamoDBSaveExpression expectedDdbSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue(new AttributeValue(fgInboundDTO.getWarehouseId())));
        expected.put("inboundId", new ExpectedAttributeValue(new AttributeValue(fgInboundDTO.getInboundId())));
        if (fgInboundDTO.getStatus() != null) {
            List<AttributeValue> allowedStatuses = fgInboundDTO.getStatus().previousStates()
                    .stream().map(v -> new AttributeValue().withS(v.toString())).collect(Collectors.toList());
            expected.put("inboundStatus", new ExpectedAttributeValue().withComparisonOperator(ComparisonOperator.IN)
                    .withAttributeValueList(allowedStatuses));
        }

        expectedDdbSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
        FinishedGoodsInbound expectedEntity = fgInboundDTO.toDbEntity();
        Assertions.assertThat(actualDdbSaveExpression).usingRecursiveComparison().isEqualTo(expectedDdbSaveExpression);
        Assertions.assertThat(actualEntity).usingRecursiveComparison().isEqualTo(expectedEntity);
    }

    private void captorVerifyQuery(FGInboundDTO fgInboundDTO) {

        DynamoDBQueryExpression actualDdbQueryExpression = dynamoDBQueryExpressionCaptor.getValue();


        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(fgInboundDTO.getWarehouseId()));
        DynamoDBQueryExpression<FinishedGoodsInbound> expectedDynamoDBQueryExpression = new DynamoDBQueryExpression<FinishedGoodsInbound>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);

        Assertions.assertThat(actualDdbQueryExpression).usingRecursiveComparison().isEqualTo(expectedDynamoDBQueryExpression);
    }

    @Test
    public void test_get_inbound_exists_success() {

        FinishedGoodsInbound expectedFinishedGoodsInbound = FinishedGoodsInbound.builder().inboundId(INBOUND_1).inboundStatus(new Active())
                .warehouseId(WAREHOUSE_1).modifiedTime(EPOCH_MILLI).startTime(EPOCH_MILLI).userId(USER_ID).build();
        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1),eq(INBOUND_1))).thenReturn(expectedFinishedGoodsInbound);
        Optional<FinishedGoodsInbound> finishedGoodsInboundOp = inboundDynamoDAO.get(WAREHOUSE_1, INBOUND_1);
        new BooleanAssert(finishedGoodsInboundOp.isPresent()).isEqualTo(true);
        new ObjectAssert<>(finishedGoodsInboundOp.get()).usingRecursiveComparison().isEqualTo(expectedFinishedGoodsInbound);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1),eq(INBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_inbound_not_exists_success() {

        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1),eq(INBOUND_1))).thenReturn(null);
        Optional<FinishedGoodsInbound> finishedGoodsInboundOp = inboundDynamoDAO.get(WAREHOUSE_1, INBOUND_1);
        new BooleanAssert(finishedGoodsInboundOp.isPresent()).isEqualTo(false);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1),eq(INBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_get_inbound_internal_server_exception() {

        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1),eq(INBOUND_1))).thenThrow(new InternalServerErrorException("exception"));
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(()->inboundDynamoDAO.get(WAREHOUSE_1, INBOUND_1)).withCauseExactlyInstanceOf(InternalServerErrorException.class);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1),eq(INBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_inbound_run_time_exception() {

        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1),eq(INBOUND_1))).thenThrow(new RuntimeException("exception"));
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(()->inboundDynamoDAO.get(WAREHOUSE_1, INBOUND_1)).withCauseExactlyInstanceOf(RuntimeException.class);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1),eq(INBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);
    }
}

