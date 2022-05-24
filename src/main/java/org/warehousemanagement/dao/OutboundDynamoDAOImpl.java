package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.warehousemanagement.entities.outbound.OutboundDTO;
import org.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OutboundDynamoDAOImpl implements OutboundDAO {

    DynamoDBMapper outboundDynamoDbMapper;
    Clock clock;

    @Inject
    public OutboundDynamoDAOImpl(DynamoDBMapper outboundDynamoDbMapper, Clock clock) {
        this.outboundDynamoDbMapper = outboundDynamoDbMapper;
        this.clock = clock;
    }

    public void add(OutboundDTO outboundDTO) {
        long startTime = clock.millis();
        FinishedGoodsOutbound finishedGoodsOutbound = FinishedGoodsOutbound.builder().warehouseId(outboundDTO.getWarehouseId())
                .outboundId(outboundDTO.getOutboundId()).startTime(startTime).modifiedTime(startTime)
                .status(outboundDTO.getStatus()).userId(outboundDTO.getUserId()).build();
        DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
        expected.put("outboundId", new ExpectedAttributeValue().withExists(false));
        dynamoDBSaveExpression.setExpected(expected);
        outboundDynamoDbMapper.save(finishedGoodsOutbound, dynamoDBSaveExpression);
    }

    public void update(OutboundDTO outboundDTO){
        OutboundStatus newOutboundStatus = outboundDTO.getStatus();

        DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue().withExists(true));
        expected.put("outboundId", new ExpectedAttributeValue().withExists(true));
        List<AttributeValue> allowedStatuses = newOutboundStatus.previousStates()
                .stream().map(v -> new AttributeValue().withS(v.getStatus())).collect(Collectors.toList());
        expected.put("status", new ExpectedAttributeValue().withComparisonOperator(ComparisonOperator.IN)
                .withAttributeValueList(allowedStatuses).withExists(true));

        dynamoDBSaveExpression.setExpected(expected);
        outboundDynamoDbMapper.save(outboundDTO.toDbEntity(), dynamoDBSaveExpression);
    }

    public Optional<FinishedGoodsOutbound> getLastOutbound(String warehouseId) {
        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(warehouseId));
        DynamoDBQueryExpression<FinishedGoodsOutbound> dynamoDBQueryExpression = new DynamoDBQueryExpression<FinishedGoodsOutbound>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
        PaginatedQueryList<FinishedGoodsOutbound> queryResponse = outboundDynamoDbMapper.query(FinishedGoodsOutbound.class, dynamoDBQueryExpression);
        Optional<FinishedGoodsOutbound> latestOutbound = queryResponse.stream().findAny();
        if (latestOutbound.isPresent()) {
            return latestOutbound;
        } else {
            return Optional.empty();
        }
    }
}
