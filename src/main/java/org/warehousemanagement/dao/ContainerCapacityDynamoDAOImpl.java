package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ContainerCapacityDynamoDAOImpl implements ContainerCapacityDAO {

    public static final String DELIMITER = "<%>";
    DynamoDBMapper containerCapacityDynamoDbMapper;

    Clock clock;

    @Inject
    public ContainerCapacityDynamoDAOImpl(DynamoDBMapper containerCapacityDynamoDbMapper, Clock clock) {
        this.containerCapacityDynamoDbMapper = containerCapacityDynamoDbMapper;
        this.clock = clock;
    }

    public Optional<ContainerCapacity> get(String warehouseId, String containerId) {
        ContainerCapacity containerCapacity = containerCapacityDynamoDbMapper.load(ContainerCapacity.class, warehouseId, containerId);
        return Optional.ofNullable(containerCapacity);
    }


    public void init(String warehouseId, String containerId) {

        String warehouseContainerId = String.join(DELIMITER, warehouseId, containerId);
        ContainerCapacity containerCapacity = ContainerCapacity.builder()
                .currentCapacity(0).warehouseContainerId(warehouseContainerId).creationTime(clock.millis()).build();
        DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseContainerId", new ExpectedAttributeValue().withExists(false));
        dynamoDBSaveExpression.setExpected(expected);
        containerCapacityDynamoDbMapper.save(containerCapacity);

    }
}
