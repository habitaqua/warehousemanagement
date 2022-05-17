package org.warehousenamagement.sao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Named;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;
import org.warehousenamagement.entities.dynamodb.LocationSKUCount;
import org.warehousenamagement.entities.location.GetLocationSKUCountRequest;
import org.warehousenamagement.entities.location.UpdateLocationSKUCountRequest;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

public class LocationSKUCountDbSAO {

    private static final String DELIMITER = "<%>";
    DynamoDBMapper locationSKUCountDynamoDbMapper;
    Clock clock;

    public LocationSKUCountDbSAO(@Named("locationSKUCountDynamoDbMapper") DynamoDBMapper locationSKUCountDynamoDbMapper,
            Clock clock) {
        this.locationSKUCountDynamoDbMapper = locationSKUCountDynamoDbMapper;
        this.clock = clock;
    }

    public void updateLocationSKUCount(UpdateLocationSKUCountRequest updateLocationSKUCountRequest) {

        String locationId = updateLocationSKUCountRequest.getLocationId();
        String warehouseId = updateLocationSKUCountRequest.getWarehouseId();
        SKUCategory skuCategory = updateLocationSKUCountRequest.getSkuCategory();
        SKUType skuType = updateLocationSKUCountRequest.getSkuType();
        GetLocationSKUCountRequest getLocationSKUCountRequest =
                GetLocationSKUCountRequest.builder().locationId(locationId).warehouseId(warehouseId)
                        .skuCategory(skuCategory).skuType(skuType).build();
        Optional<LocationSKUCount> currentLocationSKUCountOptional = getLocationSKUCount(getLocationSKUCountRequest);
        if (currentLocationSKUCountOptional.isPresent()) {
            LocationSKUCount currentLocationSKUCount = currentLocationSKUCountOptional.get();
            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Integer currentQuantity = currentLocationSKUCount.getQuantity();
            Map<String, ExpectedAttributeValue> expectedAttributes =
                    ImmutableMap.<String, ExpectedAttributeValue>builder()
                            .put("quantity",
                                    new ExpectedAttributeValue(true).withValue(new AttributeValue()
                                            .withN(String.valueOf(currentQuantity))))
                            .put("rangeKey",
                                    new ExpectedAttributeValue(true)
                                            .withValue(new AttributeValue()
                                                    .withS(currentLocationSKUCount.getUom().toString()))).build();
            dynamoDBSaveExpression.setExpected(expectedAttributes);

            int newQuantity = currentQuantity + updateLocationSKUCountRequest.getDeltaCapacity().getQty();
            LocationSKUCount updatedLocationSKUCount =
                    LocationSKUCount.builder().warehouseLocationId(currentLocationSKUCount.getWarehouseLocationId())
                            .skuCategoryAndType(currentLocationSKUCount.getSkuCategoryAndType())
                            .warehouseId(currentLocationSKUCount.getWarehouseId()).quantity(newQuantity)
                            .uom(currentLocationSKUCount.getUom()).build();
            try {
                locationSKUCountDynamoDbMapper.save(updatedLocationSKUCount, dynamoDBSaveExpression);
            } catch (ConditionalCheckFailedException ce) {
                throw  new RuntimeException("updating loction sku count failed", ce);
            }
        }
    }

    public Optional<LocationSKUCount> getLocationSKUCount(GetLocationSKUCountRequest getLocationSKUCountRequest) {

        String hashKey = String.join(DELIMITER, getLocationSKUCountRequest.getWarehouseId(),
                getLocationSKUCountRequest.getLocationId());
        String rangeKey = String.join(DELIMITER, getLocationSKUCountRequest.getSkuCategory().getValue(),
                getLocationSKUCountRequest.getSkuType().getValue());
        LocationSKUCount currentLocationSKUCount = locationSKUCountDynamoDbMapper.load(LocationSKUCount.class,
                getLocationSKUCountRequest.getLocationId(), rangeKey);
        return Optional.of(currentLocationSKUCount);

    }
}
