package org.warehousemanagement.sao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.Inbound;
import org.warehousemanagement.entities.dynamodb.Location;
import org.warehousemanagement.entities.inbound.InboundDTO;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InboundDbSAO {

    DynamoDBMapper inboundDynamoDbMapper;
    Clock clock;

    @Inject
    public InboundDbSAO(DynamoDBMapper inboundDynamoDbMapper, Clock clock) {
        this.inboundDynamoDbMapper = inboundDynamoDbMapper;
        this.clock = clock;
    }

    public void add(InboundDTO inboundDTO) {
        long startTime = clock.millis();
        Inbound inbound = Inbound.builder().warehouseId(inboundDTO.getWarehouseId())
                .inboundId(inboundDTO.getInboundId()).startTime(startTime).modifiedTime(startTime)
                .status(inboundDTO.getStatus()).userId(inboundDTO.getUserId()).build();
        inboundDynamoDbMapper.save(inbound);
    }

    public Optional<Inbound> getLastInbound(String warehouseId) {
        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(warehouseId));
        DynamoDBQueryExpression<Inbound> dynamoDBQueryExpression = new DynamoDBQueryExpression<Inbound>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
        PaginatedQueryList<Inbound> queryResponse = inboundDynamoDbMapper.query(Inbound.class, dynamoDBQueryExpression);
        Optional<Inbound> latestInbound = queryResponse.stream().findAny();
        if (latestInbound.isPresent()) {
            return latestInbound;
        } else {
            return Optional.empty();
        }
    }

}
