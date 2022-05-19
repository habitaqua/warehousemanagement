package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.UOM;
import org.warehousemanagement.entities.dynamodb.typeconvertors.LocationStatusTypeConvertor;
import org.warehousemanagement.entities.location.locationstatus.LocationStatus;

@DynamoDBTable(tableName = "locations")
@Data
@NoArgsConstructor
public class Location {

    @DynamoDBHashKey(attributeName = "warehouseId")
    String warehouseId;
    @DynamoDBRangeKey(attributeName = "locationsId")
    String locationId;

    @DynamoDBAttribute(attributeName = "totalCapacity")
    int totalCapacity;

    @DynamoDBAttribute(attributeName = "currentCapacity")
    int currentCapacity;

    @DynamoDBAttribute(attributeName = "uom")
    @DynamoDBTypeConvertedEnum
    UOM uom;
    @DynamoDBAttribute(attributeName = "status")
    @DynamoDBTypeConverted(converter = LocationStatusTypeConvertor.class)
    LocationStatus status;

    @DynamoDBAttribute(attributeName = "creationTime")
    long creationTime;
    @DynamoDBAttribute(attributeName = "modifiedTime")
    long modifiedTime;

    @Builder
    public Location(String warehouseId, String locationId, int totalCapacity, int currentCapacity, UOM uom,
                    LocationStatus status, Long creationTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(locationId), "locationId cannot be blank");
        Preconditions.checkArgument(totalCapacity > 0, "total capacity cannot be zero or less");
        Preconditions.checkArgument(currentCapacity <= totalCapacity && currentCapacity >= 0,
                "current capacity not in range");
        Preconditions.checkArgument(status != null, "location status cannot be null");
        Preconditions.checkArgument(uom != null, "uom  cannot be null");

        Preconditions.checkArgument(creationTime != null, "creationTime cannot be null");
        Preconditions.checkArgument(modifiedTime != null, "modified cannot be null");


        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.totalCapacity = totalCapacity;
        this.currentCapacity = currentCapacity;
        this.uom = uom;
        this.status = status;
        this.creationTime = creationTime;
        this.modifiedTime = modifiedTime;
    }
}
