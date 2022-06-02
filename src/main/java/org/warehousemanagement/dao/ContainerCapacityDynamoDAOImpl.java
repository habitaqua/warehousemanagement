package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.jcabi.aspects.RetryOnFailure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.container.containerstatus.Available;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.warehousemanagement.entities.exceptions.RetriableException;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ContainerCapacityDynamoDAOImpl implements ContainerCapacityDAO {

    private static final String DELIMITER = "<%>";
    DynamoDBMapper containerCapacityDynamoDbMapper;

    Clock clock;

    @Inject
    public ContainerCapacityDynamoDAOImpl(DynamoDBMapper containerCapacityDynamoDbMapper, Clock clock) {
        this.containerCapacityDynamoDbMapper = containerCapacityDynamoDbMapper;
        this.clock = clock;
    }

    public Optional<ContainerCapacity> get(String warehouseId, String containerId) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank or null");
            Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank or null");
            String hashKey = String.join(DELIMITER, warehouseId, containerId);
            ContainerCapacity containerCapacity = containerCapacityDynamoDbMapper.load(ContainerCapacity.class, hashKey);
            return Optional.ofNullable(containerCapacity);
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while getting last inbound", e);
            throw new RetriableException(e);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while getting last inbound", e);
            throw new NonRetriableException(e);
        }
    }

    public int getExistingQuantity(String warehouseId, String containerId) {
        Optional<ContainerCapacity> containerCapacityOptional = this.get(warehouseId, containerId);
        int existingQuantity = 0;
        if (containerCapacityOptional.isPresent()) {
            ContainerCapacity containerCapacity = containerCapacityOptional.get();
            existingQuantity = containerCapacity.getCurrentCapacity();
        }
        return existingQuantity;
    }

    public void init(String warehouseId, String containerId) {

        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank or null");
            Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank or null");

            String warehouseContainerId = String.join(DELIMITER, warehouseId, containerId);
            long time = clock.millis();
            ContainerCapacity containerCapacity = ContainerCapacity.builder().containerStatus(new Available())
                    .currentCapacity(0).warehouseContainerId(warehouseContainerId).creationTime(time).modifiedTime(time).build();
            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Map expected = new HashMap();
            expected.put("warehouseContainerId", new ExpectedAttributeValue().withExists(false));
            dynamoDBSaveExpression.setExpected(expected);
            containerCapacityDynamoDbMapper.save(containerCapacity, dynamoDBSaveExpression);
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while starting inbound", e);
            throw new RetriableException(e);
        } catch (ConditionalCheckFailedException ce) {
            log.error("container id", containerId, " already initialised warehouse", warehouseId, ce);
            throw new ResourceAlreadyExistsException(ce);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while starting inbound", e);
            throw new NonRetriableException(e);
        }

    }
}
