package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BooleanAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.warehousemanagement.entities.exceptions.RetriableException;
import org.warehousemanagement.entities.inbound.FGInboundDTO;
import org.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.warehousemanagement.entities.inbound.inboundstatus.Closed;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class TestInboundDynamoDAOImpl {


    private static final String INBOUND_1 = "INBOUND-1";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";

    private static final String USER_ID = "user-1";

    InboundDynamoDAOImpl inboundDbSAO;
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

    Clock clock;


    @Before
    public void setupClass() {
        MockitoAnnotations.initMocks(this);
        clock = Clock.systemUTC();
        inboundDbSAO = new InboundDynamoDAOImpl(dynamoDBMapper);
    }

    @Test
    public void test_add_success() {
        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDbSAO.add(fgInboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_add_already_existing() {
        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey rannge key already exists")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsInbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> inboundDbSAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_null_non_retriable_exception() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDbSAO.add(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("fgInboundDTO cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_status_null_non_retriable_exception() {
        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDbSAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("fgInboundDTO.status cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_starttime_null_non_retriable_exception() {
        long startTime = clock.millis();

        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .modifiedTime(startTime).userId(USER_ID).build();

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDbSAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("fgInboundDTO.startTime cannot be null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_input_userId_null_non_retriable_exception() {
        long startTime = clock.millis();

        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).build();

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDbSAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class).withMessageContaining("fgInboundDTO.userId cannot be blank or null");
        Mockito.verifyZeroInteractions(dynamoDBMapper);


    }

    @Test
    public void test_add_throws_retriable_xception() {
        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        Mockito.doThrow(new InternalServerErrorException("internal server exception")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsInbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> inboundDbSAO.add(fgInboundDTO))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyAdd(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_update_input_null() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDbSAO.update(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class);
        Mockito.verifyZeroInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_success() {
        long startTime = clock.millis();
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        inboundDbSAO.update(fgInboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_partial_update_success() {
        long startTime = clock.millis();
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).warehouseId(WAREHOUSE_1)
                .modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        inboundDbSAO.update(fgInboundDTO);
        Mockito.verify(dynamoDBMapper).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_update_no_key_found() {
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .userId(USER_ID).build();
        Mockito.doThrow(new ConditionalCheckFailedException("Hashkey range key is not found ")).when(dynamoDBMapper)
                .save(Mockito.any(FinishedGoodsInbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDbSAO.update(fgInboundDTO))
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
                .save(Mockito.any(FinishedGoodsInbound.class), Mockito.any(DynamoDBSaveExpression.class));

        Assertions.assertThatExceptionOfType(RetriableException.class)
                .isThrownBy(() -> inboundDbSAO.update(fgInboundDTO))
                .withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).save(finishedGoodsInboundCaptor.capture(), dynamoDBSaveExpressionCaptor.capture());
        captorVerifyUpdate(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);

    }

    @Test
    public void test_get_last_inbound_input_null() {
        Assertions.assertThatExceptionOfType(NonRetriableException.class)
                .isThrownBy(() -> inboundDbSAO.getLastInbound(null))
                .withCauseExactlyInstanceOf(IllegalArgumentException.class);
        Mockito.verifyZeroInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_last_inbound_success() {
        long startTime = clock.millis();
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        FinishedGoodsInbound expectedEntity = fgInboundDTO.toDbEntity();
        Mockito.when(paginatedQueryList.stream()).thenReturn(ImmutableList.of(expectedEntity).stream());
        Optional<FinishedGoodsInbound> lastInboundOp = inboundDbSAO.getLastInbound(fgInboundDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(Mockito.eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastInboundOp.isPresent()).isEqualTo(true);
        Assertions.assertThat(expectedEntity).usingRecursiveComparison().isEqualTo(lastInboundOp.get());
        captorVerifyQuery(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(paginatedQueryList);


    }

    @Test
    public void test_get_last_inbound_none_success() {
        long startTime = clock.millis();
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Optional<FinishedGoodsInbound> lastInboundOp = inboundDbSAO.getLastInbound(fgInboundDTO.getWarehouseId());
        Mockito.verify(dynamoDBMapper).query(Mockito.eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verify(paginatedQueryList).stream();
        new BooleanAssert(lastInboundOp.isPresent()).isEqualTo(false);
        captorVerifyQuery(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
        Mockito.verifyNoMoreInteractions(paginatedQueryList);
    }
    @Test
    public void test_get_last_inbound_internal_server_exception() {
        long startTime = clock.millis();
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenThrow(new InternalServerErrorException("exception"));
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(RetriableException.class).isThrownBy(() -> inboundDbSAO.getLastInbound(fgInboundDTO.getWarehouseId())).withCauseExactlyInstanceOf(InternalServerErrorException.class);
        Mockito.verify(dynamoDBMapper, Mockito.times(1)).query(Mockito.eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
        Mockito.verifyZeroInteractions(paginatedQueryList);
        captorVerifyQuery(fgInboundDTO);
        Mockito.verifyNoMoreInteractions(dynamoDBMapper);
    }

    @Test
    public void test_get_last_inbound_exception() {
        long startTime = clock.millis();
        long modifiedTime = startTime + 10;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(modifiedTime).endTime(modifiedTime).userId(USER_ID).build();
        Mockito.when(dynamoDBMapper.query(Mockito.any(), Mockito.any(DynamoDBQueryExpression.class))).thenThrow(new RuntimeException("exception"));
        Mockito.when(paginatedQueryList.stream()).thenReturn(Collections.EMPTY_LIST.stream());
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> inboundDbSAO.getLastInbound(fgInboundDTO.getWarehouseId())).withCauseExactlyInstanceOf(RuntimeException.class);
        Mockito.verify(dynamoDBMapper).query(Mockito.eq(FinishedGoodsInbound.class), dynamoDBQueryExpressionCaptor.capture());
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
                    .stream().map(v -> new AttributeValue().withS(v.getStatus())).collect(Collectors.toList());
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

}

