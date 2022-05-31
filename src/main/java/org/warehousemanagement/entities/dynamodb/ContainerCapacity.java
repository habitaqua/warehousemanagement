package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.dynamodb.typeconvertors.ContainerCapacityStatusTypeConvertor;
import org.warehousemanagement.entities.container.containerstatus.ContainerStatus;

/**
 * Dynamodb entory for a container which can hold the skus.
 * Crate /bins etc are all abstracted as containers.
 *
 * @author moduludu
 */
@DynamoDBTable(tableName = "container-capacity")
@Data
@NoArgsConstructor
public class ContainerCapacity {

    @DynamoDBHashKey(attributeName = "warehouseContainerId")
    String warehouseContainerId;



    @DynamoDBAttribute(attributeName = "currentCapacity")
    int currentCapacity;

    @DynamoDBAttribute(attributeName = "status")
    @DynamoDBTypeConverted(converter = ContainerCapacityStatusTypeConvertor.class)
    ContainerStatus status;

    @DynamoDBAttribute(attributeName = "creationTime")
    long creationTime;
    @DynamoDBAttribute(attributeName = "modifiedTime")
    long modifiedTime;

    @Builder
    public ContainerCapacity(String warehouseContainerId,int currentCapacity,
                             ContainerStatus status, Long creationTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseContainerId), "warehouseContainerId cannot be blank");
        Preconditions.checkArgument(currentCapacity >= 0,
                "current capacity not in range");
        Preconditions.checkArgument(status != null, "container capacity status cannot be null");
        Preconditions.checkArgument(creationTime != null, "creationTime cannot be null");
        Preconditions.checkArgument(modifiedTime != null, "modified cannot be null");


        this.warehouseContainerId = warehouseContainerId;
        this.currentCapacity = currentCapacity;
        this.status = status;
        this.creationTime = creationTime;
        this.modifiedTime = modifiedTime;
    }
}
