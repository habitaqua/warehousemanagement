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
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestOutboundDynamoDAOImpl {


    private static final String OUTBOUND_1 = "INBOUND-1";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";

    private static final String USER_ID = "user-1";

    private static final String CUSTOMER_ID = "customer-1";
    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();

    OutboundDynamoDAOImpl outboundDynamoDAO;
    @Mock
    DynamoDBMapper dynamoDBMapper;

    @Mock
    PaginatedQueryList<FinishedGoodsOutbound> paginatedQueryList;

    @Captor
    ArgumentCaptor<DynamoDBSaveExpression> dynamoDBSaveExpressionCaptor;

    @Captor
    ArgumentCaptor<DynamoDBQueryExpression> dynamoDBQueryExpressionCaptor;

    @Captor
    ArgumentCaptor<FinishedGoodsOutbound> finishedGoodsOutboundCaptor;


    @Before
    public void setupClass() {
        MockitoAnnotations.initMocks(this);
        outboundDynamoDAO = new OutboundDynamoDAOImpl(dynamoDBMapper);
    }

    @Test
    public void test_get_exists_success() {
        FinishedGoodsOutbound finishedGoodsOutbound = FinishedGoodsOutbound.builder().outboundId(OUTBOUND_1).outboundStatus(new Active()).warehouseId(WAREHOUSE_1)
                .customerId(CUSTOMER_ID).endTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).startTime(EPOCH_MILLI).userId(USER_ID).build();

        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1))).thenReturn(finishedGoodsOutbound);
        Optional<FinishedGoodsOutbound> finishedGoodsOutboundActualOp = outboundDynamoDAO.get(WAREHOUSE_1, OUTBOUND_1);
        new BooleanAssert(finishedGoodsOutboundActualOp.isPresent()).isEqualTo(true);
        new ObjectAssert<>(finishedGoodsOutboundActualOp.get()).usingRecursiveComparison().isEqualTo(finishedGoodsOutbound);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_not_exists_success() {
        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1))).thenReturn(null);
        Optional<FinishedGoodsOutbound> finishedGoodsOutboundActualOp = outboundDynamoDAO.get(WAREHOUSE_1, OUTBOUND_1);
        new BooleanAssert(finishedGoodsOutboundActualOp.isPresent()).isEqualTo(false);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_retriable_exception() {
        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1))).thenThrow(new InternalServerErrorException("exception"));
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(() -> outboundDynamoDAO.get(WAREHOUSE_1, OUTBOUND_1)).withCauseExactlyInstanceOf(InternalServerErrorException.class);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_non_retriable() {
        when(dynamoDBMapper.load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1))).thenThrow(new RuntimeException("exception"));
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> outboundDynamoDAO.get(WAREHOUSE_1, OUTBOUND_1)).withCauseExactlyInstanceOf(RuntimeException.class);
        verify(dynamoDBMapper).load(any(), eq(WAREHOUSE_1), eq(OUTBOUND_1));
        verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_input_null_illegal_argument() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> outboundDynamoDAO.get(null, null));
        verifyZeroInteractions(dynamoDBMapper);
    }

    @Test
    public void test_add_success() {
        long startTime = EPOCH_MILLI;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).customerId(CUSTOMER_ID).modifiedTime(startTime).userId(USER_ID).build();
        outboundDynamoDAO.add(outboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_add_already_existing() {
        long startTime = EPOCH_MILLI;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey rannge key already exists")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsOutbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO))
                .withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(null))
                .withMessageContaining("outboundDTO cannot be null");
        verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_status_null_non_retriable_exception() {
        long startTime = EPOCH_MILLI;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO))
                .withMessageContaining("outboundDTO.status cannot be null");
        verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_starttime_null_non_retriable_exception() {
        long startTime = EPOCH_MILLI;

        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO))
                .withMessageContaining("outboundDTO.startTime cannot be null");
        verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_userId_null_non_retriable_exception() {
        long startTime = EPOCH_MILLI;

        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).customerId(CUSTOMER_ID).build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO)).withMessageContaining("outboundDTO.userId cannot be blank or null");
        verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_customerId_null_non_retriable_exception() {
        long startTime = EPOCH_MILLI;

        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO))
                .withMessageContaining("outboundDTO.customerId cannot be blank or null");
        verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_throws_retriable_xception() {
        long startTime = EPOCH_MILLI;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        Mockito.doThrow(new InternalServerErrorException("internal server exception")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsOutbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_add_throws_nonretriable_xception() {
        long startTime = EPOCH_MILLI;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        Mockito.doThrow(new RuntimeException("runtime exception")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsOutbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO))
                .withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_update_input_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundDynamoDAO.update(null));
        verifyZeroInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        outboundDynamoDAO.update(outboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_partial_update_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1)
                .modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        outboundDynamoDAO.update(outboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_no_key_found() {
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .userId(USER_ID).build();
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey range key is not found ")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsOutbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> outboundDynamoDAO.update(outboundDTO))
                .withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }


    @Test
    public void test_update_internal_server_exception() {
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .userId(USER_ID).build();
        Mockito.doThrow(new InternalServerErrorException("exception")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsOutbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> outboundDynamoDAO.update(outboundDTO))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_runtime_exception() {
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .userId(USER_ID).build();
        Mockito.doThrow(new RuntimeException("exception")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsOutbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> outboundDynamoDAO.update(outboundDTO))
                .withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(finishedGoodsOutboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_get_last_outbound_input_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundDynamoDAO.getLastOutbound(null));
        verifyZeroInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_last_outbound_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        FinishedGoodsOutbound expectedEntity = outboundDTO.toDbEntity();
        Mockito.when(paginatedQueryList.stream()).thenReturn(ImmutableList.of(expectedEntity).stream());
        Optional<FinishedGoodsOutbound> lastOutboundOp = outboundDynamoDAO.getLastOutbound(outboundDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(eq(FinishedGoodsOutbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastOutboundOp.isPresent()).isEqualTo(true);
        Assertions.assertThat(expectedEntity).usingRecursiveComparison().isEqualTo(lastOutboundOp.get());
        captorVerifyQuery(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(paginatedQueryList);


    }

    @Test
    public void test_get_last_outbound_none_success() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Optional<FinishedGoodsOutbound> lastOutboundOp = outboundDynamoDAO.getLastOutbound(outboundDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(eq(FinishedGoodsOutbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastOutboundOp.isPresent()).isEqualTo(false);
        captorVerifyQuery(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(paginatedQueryList);
    }

    @Test
    public void test_get_last_outbound_internal_server_exception() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenThrow(new InternalServerErrorException("exception"));
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(() -> outboundDynamoDAO.getLastOutbound(outboundDTO.getWarehouseId())).withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).query(eq(FinishedGoodsOutbound.class), dynamoDBQueryExpressionCaptor.capture());
        verifyZeroInteractions(paginatedQueryList);
        captorVerifyQuery(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_last_outbound_exception() {
        long startTime = EPOCH_MILLI;
        long modifiedTime = startTime + 10;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenThrow(new RuntimeException("exception"));
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> outboundDynamoDAO.getLastOutbound(outboundDTO.getWarehouseId())).withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper).query(eq(FinishedGoodsOutbound.class), dynamoDBQueryExpressionCaptor.capture());
        verifyZeroInteractions(paginatedQueryList);
        captorVerifyQuery(outboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }


    private void captorVerifyAdd(OutboundDTO outboundDTO) {
        FinishedGoodsOutbound actualEntity = finishedGoodsOutboundCaptor.getValue();
        DynamoDBSaveExpression actualDdbSaveExpression = dynamoDBSaveExpressionCaptor.getValue();

        DynamoDBSaveExpression expectedDdbSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
        expected.put("outboundId", new ExpectedAttributeValue().withExists(false));
        expectedDdbSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
        FinishedGoodsOutbound expectedEntity = outboundDTO.toDbEntity();
        Assertions.assertThat(actualDdbSaveExpression).usingRecursiveComparison().isEqualTo(expectedDdbSaveExpression);
        Assertions.assertThat(actualEntity).usingRecursiveComparison().isEqualTo(expectedEntity);
    }

    private void captorVerifyUpdate(OutboundDTO outboundDTO) {
        FinishedGoodsOutbound actualEntity = finishedGoodsOutboundCaptor.getValue();
        DynamoDBSaveExpression actualDdbSaveExpression = dynamoDBSaveExpressionCaptor.getValue();

        DynamoDBSaveExpression expectedDdbSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue(new AttributeValue(outboundDTO.getWarehouseId())));
        expected.put("outboundId", new ExpectedAttributeValue(new AttributeValue(outboundDTO.getOutboundId())));
        if (outboundDTO.getStatus() != null) {
            List<AttributeValue> allowedStatuses = outboundDTO.getStatus().previousStates()
                    .stream().map(v -> new AttributeValue().withS(v.toString())).collect(Collectors.toList());
            expected.put("outboundStatus", new ExpectedAttributeValue().withComparisonOperator(ComparisonOperator.IN)
                    .withAttributeValueList(allowedStatuses));
        }

        expectedDdbSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
        FinishedGoodsOutbound expectedEntity = outboundDTO.toDbEntity();
        Assertions.assertThat(actualDdbSaveExpression).usingRecursiveComparison().isEqualTo(expectedDdbSaveExpression);
        Assertions.assertThat(actualEntity).usingRecursiveComparison().isEqualTo(expectedEntity);
    }

    private void captorVerifyQuery(OutboundDTO outboundDTO) {

        DynamoDBQueryExpression actualDdbQueryExpression = dynamoDBQueryExpressionCaptor.getValue();


        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(outboundDTO.getWarehouseId()));
        DynamoDBQueryExpression<FinishedGoodsInbound> expectedDynamoDBQueryExpression = new DynamoDBQueryExpression<FinishedGoodsInbound>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);

        Assertions.assertThat(actualDdbQueryExpression).usingRecursiveComparison().isEqualTo(expectedDynamoDBQueryExpression);
    }

}

