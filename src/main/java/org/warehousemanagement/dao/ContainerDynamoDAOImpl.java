package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.warehousemanagement.entities.PaginatedResponse;
import org.warehousemanagement.entities.container.AddContainerRequest;
import org.warehousemanagement.entities.container.ContainerDTO;
import org.warehousemanagement.entities.container.GetContainerRequest;
import org.warehousemanagement.entities.container.GetContainersRequest;
import org.warehousemanagement.entities.dynamodb.Container;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.exceptions.NonRetriableException;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is the DAO for Container Table. Move to master.
 *
 * @author Laasya
 */
@Slf4j
public class ContainerDynamoDAOImpl implements ContainerDAO{


    DynamoDBMapper containerDynamoDbMapper;

    ContainerCapacityDynamoDAOImpl containerCapacityDynamoDAOImpl;
    ObjectMapper objectMapper;
    Clock clock;

    @Inject
    public ContainerDynamoDAOImpl(DynamoDBMapper containerDynamoDbMapper, ContainerCapacityDynamoDAOImpl containerCapacityDynamoDAOImpl,
                                  Clock clock, ObjectMapper mapper) {
        this.containerDynamoDbMapper = containerDynamoDbMapper;
        this.containerCapacityDynamoDAOImpl = containerCapacityDynamoDAOImpl;
        this.clock = clock;
        this.objectMapper = mapper;
    }


    public Optional<ContainerDTO> getContainer(GetContainerRequest getContainerRequest) {

        String warehouseId = getContainerRequest.getWarehouseId();
        String containerId = getContainerRequest.getContainerId();
        Container container = containerDynamoDbMapper.load(Container.class, warehouseId, containerId);
        if(container == null) {
           return Optional.empty();
        }
        ContainerDTO.Builder containerDTOBuilder = new ContainerDTO.Builder().containerDetails(container);
        Optional<ContainerCapacity> containerCapacity = containerCapacityDynamoDAOImpl.get(warehouseId, containerId);
        if(containerCapacity.isPresent()) {
            containerDTOBuilder.currentCapacityDetails(containerCapacity.get());
        }
        return Optional.ofNullable(containerDTOBuilder.build());
    }

    /**
     * gets container details without current capacity
     * @param getContainersRequest
     * @return
     */
    public PaginatedResponse<ContainerDTO> getContainers(GetContainersRequest getContainersRequest) {

        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(getContainersRequest.getWarehouseId()));
        DynamoDBQueryExpression<Container> dynamoDBQueryExpression = new DynamoDBQueryExpression<Container>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withLimit(getContainersRequest.getLimit());

        try {
            if (getContainersRequest.getPageToken().isPresent()) {

                Map<String, AttributeValue> exclusiveStartKey = objectMapper.readValue(getContainersRequest.getPageToken().get(), Map.class);
                dynamoDBQueryExpression.withExclusiveStartKey(exclusiveStartKey);

            }
            QueryResultPage<Container> locationQueryResultPage = containerDynamoDbMapper.queryPage(Container.class,
                    dynamoDBQueryExpression);
            List<Container> containers = locationQueryResultPage.getResults();
            List<ContainerDTO> containerDTOS = containers.stream().map(container -> new ContainerDTO.Builder().containerDetails(container).build())
                    .collect(Collectors.toList());
            String nextPageToken = objectMapper.writeValueAsString(locationQueryResultPage.getLastEvaluatedKey());
            PaginatedResponse<ContainerDTO> paginatedResponse = PaginatedResponse.<ContainerDTO>builder()
                    .items(containerDTOS).nextPageToken(nextPageToken).build();
            return paginatedResponse;
        } catch (JsonProcessingException e) {
            log.error("error while de serialising pagetoken ", getContainersRequest.getPageToken().get(), e);
            throw new NonRetriableException(e);
        }
    }


    /**
     * creates new location at the given warehouse id.
     * location id at context of a warehouse is auto incremented.
     * At a warehouse level addition of location is synchronized . This helps in keeping location id counter incremental
     *
     * @return
     */
    public void add(ContainerDTO containerDTO) {

        String containerId = containerDTO.getContainerId();
        String warehouseId = containerDTO.getWarehouseId();
        long time = clock.millis();
        Container container = Container.builder().containerId(containerId)
                .warehouseId(warehouseId).skuCodeWisePredefinedCapacity(containerDTO.getSkuCodeWisePredefinedCapacity())
                .creationTime(time).modifiedTime(time).build();
        DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
        Map expected = new HashMap();
        expected.put("warehouseId", new ExpectedAttributeValue(new AttributeValue().withS(warehouseId)).withExists(false));
        expected.put("containerId", new ExpectedAttributeValue(new AttributeValue().withS(containerId)).withExists(false));
        dynamoDBSaveExpression.setExpected(expected);
        containerDynamoDbMapper.save(container,dynamoDBSaveExpression);
    }

    public Optional<Container> getLastAddedContainer(String warehouseId) {
        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(warehouseId));
        DynamoDBQueryExpression<Container> dynamoDBQueryExpression = new DynamoDBQueryExpression<Container>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
        PaginatedQueryList<Container> queryResponse = containerDynamoDbMapper.query(Container.class, dynamoDBQueryExpression);
        Optional<Container> latestAddedContainer = queryResponse.stream().findAny();
        if (latestAddedContainer.isPresent()) {
            return latestAddedContainer;
        } else {
            return Optional.empty();
        }
    }

}
