package org.habitbev.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.PaginatedResponse;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.habitbev.warehousemanagement.entities.container.GetContainersRequest;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.dynamodb.Container;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;

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
public class ContainerDynamoDAOImpl implements ContainerDAO {


    DynamoDBMapper containerDynamoDbMapper;

    ObjectMapper objectMapper;
    Clock clock;

    @Inject
    public ContainerDynamoDAOImpl(DynamoDBMapper containerDynamoDbMapper, Clock clock, ObjectMapper mapper) {
        this.containerDynamoDbMapper = containerDynamoDbMapper;
        this.clock = clock;
        this.objectMapper = mapper;
    }


    public Optional<ContainerDTO> getContainer(GetContainerRequest getContainerRequest) {

        Preconditions.checkArgument(getContainerRequest != null, "getContainerRequest cannot be null");
        try {
            String warehouseId = getContainerRequest.getWarehouseId();
            String containerId = getContainerRequest.getContainerId();
            Container container = containerDynamoDbMapper.load(Container.class, warehouseId, containerId);
            if (container == null) {
                return Optional.empty();
            }
            ContainerDTO.Builder containerDTOBuilder = new ContainerDTO.Builder().containerDetails(container);
            return Optional.ofNullable(containerDTOBuilder.build());
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while starting inbound", e);
            throw new RetriableException(e);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while starting inbound", e);
            throw new NonRetriableException(e);
        }
    }

    /**
     * gets container details without current capacity
     *
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
            QueryResultPage<Container> containerQueryResultPage = containerDynamoDbMapper.queryPage(Container.class,
                    dynamoDBQueryExpression);
            List<Container> containers = containerQueryResultPage.getResults();
            List<ContainerDTO> containerDTOS = containers.stream().map(container -> new ContainerDTO.Builder().containerDetails(container).build())
                    .collect(Collectors.toList());
            String nextPageToken = objectMapper.writeValueAsString(containerQueryResultPage.getLastEvaluatedKey());
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

        try {
            Preconditions.checkArgument(containerDTO != null, "containerdto cannot be null");
            String containerId = containerDTO.getContainerId();
            String warehouseId = containerDTO.getWarehouseId();
            long time = clock.millis();
            Container container = Container.builder().containerId(containerId)
                    .warehouseId(warehouseId).skuCodeWisePredefinedCapacity(containerDTO.getSkuCodeWisePredefinedCapacity())
                    .creationTime(time).modifiedTime(time).build();
            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Map expected = new HashMap();
            expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
            expected.put("containerId", new ExpectedAttributeValue().withExists(false));
            dynamoDBSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
            containerDynamoDbMapper.save(container, dynamoDBSaveExpression);
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while starting inbound", e);
            throw new RetriableException(e);
        } catch (ConditionalCheckFailedException ce) {
            log.error("container id", containerDTO.getContainerId(), " already exist in given warehouse",
                    containerDTO.getWarehouseId(), ce);
            throw new ResourceAlreadyExistsException(ce);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while starting inbound", e);
            throw new NonRetriableException(e);
        }
    }

    public Optional<ContainerDTO> getLastAddedContainer(String warehouseId) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be null");
            Map<String, AttributeValue> eav = new HashMap();
            eav.put(":val1", new AttributeValue().withS(warehouseId));
            DynamoDBQueryExpression<Container> dynamoDBQueryExpression = new DynamoDBQueryExpression<Container>()
                    .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                    .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
            PaginatedQueryList<Container> queryResponse = containerDynamoDbMapper.query(Container.class, dynamoDBQueryExpression);
            Optional<Container> latestAddedContainer = queryResponse.stream().findAny();
            if (latestAddedContainer.isPresent()) {
                return Optional.of(new ContainerDTO.Builder().containerDetails(latestAddedContainer.get()).build());

            } else {
                return Optional.empty();
            }
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while getting last inbound", e);
            throw new RetriableException(e);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while getting last inbound", e);
            throw new NonRetriableException(e);
        }
    }

}
