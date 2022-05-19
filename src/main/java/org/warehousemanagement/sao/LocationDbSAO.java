package org.warehousemanagement.sao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.warehousemanagement.entities.PaginatedResponse;
import org.warehousemanagement.entities.dynamodb.Location;
import org.warehousemanagement.entities.exceptions.NonRetriableException;
import org.warehousemanagement.entities.location.*;
import org.warehousemanagement.entities.location.locationstatus.Available;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is the DAO for Location Table
 *
 * @author Laasya
 */
@Slf4j
public class LocationDbSAO {


    DynamoDBMapper locationDynamoDbMapper;
    ObjectMapper objectMapper;
    Clock clock;

    @Inject
    public LocationDbSAO(DynamoDBMapper locationDynamoDbMapper, Clock clock, ObjectMapper mapper) {
        this.locationDynamoDbMapper = locationDynamoDbMapper;
        this.clock = clock;
        this.objectMapper = mapper;
    }


    public Optional<Location> getLocation(GetLocationRequest getLocationRequest) {
        Location location = locationDynamoDbMapper.load(Location.class, getLocationRequest.getWarehouseId(), getLocationRequest.getLocationId());
        return Optional.ofNullable(location);
    }

    public PaginatedResponse<Location> getLocations(GetLocationsRequest getLocationsRequest) {

        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(getLocationsRequest.getWarehouseId()));
        DynamoDBQueryExpression<Location> dynamoDBQueryExpression = new DynamoDBQueryExpression<Location>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withLimit(getLocationsRequest.getLimit());

        try {
            if (getLocationsRequest.getPageToken().isPresent()) {

                Map<String, AttributeValue> exclusiveStartKey = objectMapper.readValue(getLocationsRequest.getPageToken().get(), Map.class);
                dynamoDBQueryExpression.withExclusiveStartKey(exclusiveStartKey);

            }
            QueryResultPage<Location> locationQueryResultPage = locationDynamoDbMapper.queryPage(Location.class,
                    dynamoDBQueryExpression);
            List<Location> locations = locationQueryResultPage.getResults();
            String nextPageToken = objectMapper.writeValueAsString(locationQueryResultPage.getLastEvaluatedKey());
            PaginatedResponse<Location> paginatedResponse = PaginatedResponse.<Location>builder()
                    .items(locations).nextPageToken(nextPageToken).build();
            return paginatedResponse;
        } catch (JsonProcessingException e) {
            log.error("error while de serialising pagetoken ", getLocationsRequest.getPageToken().get(), e);
            throw new NonRetriableException(e);
        }
    }


    public void alterAvailableCapacity(UpdateLocationRequest updateLocationRequest) {
        int deltaCapacity = updateLocationRequest.getDeltaCapacity();
        if (deltaCapacity == 0)
            return;

    }

    /**
     * creates new location at the given warehouse id.
     * location id at context of a warehouse is autoincremented.
     * At a warehouse level addition of location is synchronized . This helps in keeping location id counter incremental
     *
     * @param locationDTO
     * @return
     */
    public void add(LocationDTO locationDTO) {

        long time = clock.millis();
        Location location = Location.builder().locationId(locationDTO.getLocationId())
                .warehouseId(locationDTO.getWarehouseId())
                .totalCapacity(locationDTO.getTotalCapacity())
                .uom(locationDTO.getUom())
                .currentCapacity(locationDTO.getCurrentCapacity())
                .status(locationDTO.getStatus())
                .creationTime(time).modifiedTime(time).build();
        locationDynamoDbMapper.save(location);
    }

    public Optional<Location> getLastAddedLocation(String warehouseId) {
        Map<String, AttributeValue> eav = new HashMap();
        eav.put(":val1", new AttributeValue().withS(warehouseId));
        DynamoDBQueryExpression<Location> dynamoDBQueryExpression = new DynamoDBQueryExpression<Location>()
                .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
        PaginatedQueryList<Location> queryResponse = locationDynamoDbMapper.query(Location.class, dynamoDBQueryExpression);
        Optional<Location> latestAddedLocation = queryResponse.stream().findAny();
        if (latestAddedLocation.isPresent()) {
            return latestAddedLocation;
        } else {
            return Optional.empty();
        }
    }

}
