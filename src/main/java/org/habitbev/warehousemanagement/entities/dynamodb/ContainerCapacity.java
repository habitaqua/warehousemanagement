package org.habitbev.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.habitbev.warehousemanagement.entities.dynamodb.typeconvertors.ContainerCapacityStatusTypeConvertor;

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

    @DynamoDBAttribute(attributeName = "containerStatus")
    @DynamoDBTypeConverted(converter = ContainerCapacityStatusTypeConvertor.class)
    ContainerStatus containerStatus;

    @DynamoDBAttribute(attributeName = "creationTime")
    long creationTime;
    @DynamoDBAttribute(attributeName = "modifiedTime")
    long modifiedTime;

    @Builder
    public ContainerCapacity(String warehouseContainerId, int currentCapacity,
                             ContainerStatus containerStatus, Long creationTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseContainerId), "warehouseContainerId cannot be blank");
        Preconditions.checkArgument(currentCapacity >= 0,
                "current capacity not in range");
        Preconditions.checkArgument(containerStatus != null, "container capacity status cannot be null");
        Preconditions.checkArgument(creationTime != null, "creationTime cannot be null");
        Preconditions.checkArgument(modifiedTime != null, "modified cannot be null");


        this.warehouseContainerId = warehouseContainerId;
        this.currentCapacity = currentCapacity;
        this.containerStatus = containerStatus;
        this.creationTime = creationTime;
        this.modifiedTime = modifiedTime;
    }
}
