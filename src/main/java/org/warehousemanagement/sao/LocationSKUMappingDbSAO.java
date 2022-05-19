package org.warehousemanagement.sao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.warehousemanagement.entities.SKUCategory;
import org.warehousemanagement.entities.SKUType;
import org.warehousemanagement.entities.dynamodb.LocationSKUMapping;
import org.warehousemanagement.entities.locationskumapping.GetAllLocationSKUMappingsRequest;
import org.warehousemanagement.entities.locationskumapping.GetLocationSKUMappingRequest;
import org.warehousemanagement.entities.locationskumapping.UpdateLocationSKUMappingRequest;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocationSKUMappingDbSAO {

    private static final String DELIMITER = "<%>";
    DynamoDBMapper locationSKUCountDynamoDbMapper;
    Clock clock;

    @Inject
    public LocationSKUMappingDbSAO( DynamoDBMapper locationSKUCountDynamoDbMapper,
                                   Clock clock) {
        this.locationSKUCountDynamoDbMapper = locationSKUCountDynamoDbMapper;
        this.clock = clock;
    }

    public void updateLocationSKUCount(UpdateLocationSKUMappingRequest updateLocationSKUMappingRequest) {

        String locationId = updateLocationSKUMappingRequest.getLocationId();
        String warehouseId = updateLocationSKUMappingRequest.getWarehouseId();
        SKUCategory skuCategory = updateLocationSKUMappingRequest.getSkuCategory();
        SKUType skuType = updateLocationSKUMappingRequest.getSkuType();
        GetLocationSKUMappingRequest getLocationSKUMappingRequest =
                GetLocationSKUMappingRequest.builder().locationId(locationId).warehouseId(warehouseId)
                        .skuCategory(skuCategory).skuType(skuType).build();
        Optional<LocationSKUMapping> currentLocationSKUCountOptional = getLocationSKUCount(getLocationSKUMappingRequest);
        if (currentLocationSKUCountOptional.isPresent()) {
            LocationSKUMapping currentLocationSKUMapping = currentLocationSKUCountOptional.get();
            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Integer currentQuantity = currentLocationSKUMapping.getQuantity();
            Map<String, ExpectedAttributeValue> expectedAttributes =
                    ImmutableMap.<String, ExpectedAttributeValue>builder()
                            .put("quantity",
                                    new ExpectedAttributeValue(true).withValue(new AttributeValue()
                                            .withN(String.valueOf(currentQuantity))))
                            .put("rangeKey",
                                    new ExpectedAttributeValue(true)
                                            .withValue(new AttributeValue()
                                                    .withS(currentLocationSKUMapping.getUom().toString()))).build();
            dynamoDBSaveExpression.setExpected(expectedAttributes);

            int newQuantity = currentQuantity + updateLocationSKUMappingRequest.getDeltaCapacity().getQty();
            LocationSKUMapping updatedLocationSKUMapping =
                    LocationSKUMapping.builder().warehouseLocationId(currentLocationSKUMapping.getWarehouseLocationId())
                            .skuCategoryAndType(currentLocationSKUMapping.getSkuCategoryAndType())
                            .warehouseId(currentLocationSKUMapping.getWarehouseId()).quantity(newQuantity)
                            .uom(currentLocationSKUMapping.getUom()).build();
            try {
                locationSKUCountDynamoDbMapper.save(updatedLocationSKUMapping, dynamoDBSaveExpression);
            } catch (ConditionalCheckFailedException ce) {
                throw  new RuntimeException("updating location sku count failed", ce);
            }
        }
    }

    public List<LocationSKUMapping> getAllLocationSKUMappings (GetAllLocationSKUMappingsRequest getAllLocationSKUMappingsRequest) {
        
        String hashKey = String.join(DELIMITER, getAllLocationSKUMappingsRequest.getWarehouseId(), getAllLocationSKUMappingsRequest.getLocationId());
        LocationSKUMapping locationSKUMapping = LocationSKUMapping.builder().warehouseLocationId(hashKey).build();
        DynamoDBQueryExpression<LocationSKUMapping> locationSKUMappingDynamoDBQueryExpression = new 
                DynamoDBQueryExpression<LocationSKUMapping>().withHashKeyValues(locationSKUMapping);
        PaginatedQueryList<LocationSKUMapping> paginatedQueryResponse = locationSKUCountDynamoDbMapper
                .query(LocationSKUMapping.class, locationSKUMappingDynamoDBQueryExpression);
        List<LocationSKUMapping> response = paginatedQueryResponse.stream().collect(Collectors.toList());
        return response;


    }
    public Optional<LocationSKUMapping> getLocationSKUCount(GetLocationSKUMappingRequest getLocationSKUMappingRequest) {

        String hashKey = String.join(DELIMITER, getLocationSKUMappingRequest.getWarehouseId(),
                getLocationSKUMappingRequest.getLocationId());
        String rangeKey = String.join(DELIMITER, getLocationSKUMappingRequest.getSkuCategory().getValue(),
                getLocationSKUMappingRequest.getSkuType().getValue());
        LocationSKUMapping currentLocationSKUMapping = locationSKUCountDynamoDbMapper.load(LocationSKUMapping.class,
                hashKey, rangeKey);
        return Optional.of(currentLocationSKUMapping);

    }
}
