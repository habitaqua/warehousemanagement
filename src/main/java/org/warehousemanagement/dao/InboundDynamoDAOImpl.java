package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.warehousemanagement.entities.inbound.FGInboundDTO;
import org.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

public class InboundDynamoDAOImpl implements InboundDAO {

    DynamoDBMapper inboundDynamoDbMapper;
    Clock clock;

    @Inject
    public InboundDynamoDAOImpl(DynamoDBMapper inboundDynamoDbMapper, Clock clock) {
        this.inboundDynamoDbMapper = inboundDynamoDbMapper;
        this.clock = clock;
    }

    /**
     * Add the given inbound details only if there is no inbound with same id in the warehouse
     * @param fgInboundDTO
     */
    public void add(FGInboundDTO fgInboundDTO) {
        FinishedGoodsInbound finishedGoodsInbound = fgInboundDTO.toDbEntity();
        DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
        expected.put("inboundId", new ExpectedAttributeValue().withExists(false));
        dynamoDBSaveExpression.setExpected(expected);
        inboundDynamoDbMapper.save(finishedGoodsInbound, dynamoDBSaveExpression);
    }

    public void update(FGInboundDTO fgInboundDTO){
        InboundStatus newInboundStatus = fgInboundDTO.getStatus();

        DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue().withExists(true));
        expected.put("inboundId", new ExpectedAttributeValue().withExists(true));
        List<AttributeValue> allowedStatuses = newInboundStatus.previousStates()
                .stream().map(v -> new AttributeValue().withS(v.getStatus())).collect(Collectors.toList());
        expected.put("status", new ExpectedAttributeValue().withComparisonOperator(ComparisonOperator.IN)
                .withAttributeValueList(allowedStatuses).withExists(true));

        dynamoDBSaveExpression.setExpected(expected);
        inboundDynamoDbMapper.save(fgInboundDTO.toDbEntity(), dynamoDBSaveExpression);
    }

    /**
     * gets last inbound details at warehouselevel. Used mainly to identify next Id to generate
     * @param warehouseId
     * @return
     */
    public Optional<FinishedGoodsInbound> getLastInbound(String warehouseId) {
        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(warehouseId));
        DynamoDBQueryExpression<FinishedGoodsInbound> dynamoDBQueryExpression = new DynamoDBQueryExpression<FinishedGoodsInbound>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
        PaginatedQueryList<FinishedGoodsInbound> queryResponse = inboundDynamoDbMapper.query(FinishedGoodsInbound.class, dynamoDBQueryExpression);
        Optional<FinishedGoodsInbound> latestInbound = queryResponse.stream().findAny();
        if (latestInbound.isPresent()) {
            return latestInbound;
        } else {
            return Optional.empty();
        }
    }
}
