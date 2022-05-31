package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.dynamodb.typeconvertors.SKUWiseCapacityConvertor;
import org.warehousemanagement.utils.Utilities;

import java.util.Map;

@Data
@NoArgsConstructor
@DynamoDBTable(tableName = "containers")
public class Container {

    @DynamoDBHashKey
    String warehouseId;
    @DynamoDBRangeKey
    String containerId;
    @DynamoDBAttribute(attributeName = "totalCapacity")
    @DynamoDBTypeConverted(converter = SKUWiseCapacityConvertor.class)
    Map<String, Integer> skuCodeWisePredefinedCapacity;
    @DynamoDBAttribute(attributeName = "creationTime")
    long creationTime;
    @DynamoDBAttribute(attributeName = "modifiedTime")
    long modifiedTime;


    @Builder
    private Container(String warehouseId, String containerId, Map<String, Integer> skuCodeWisePredefinedCapacity, Long creationTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        Preconditions.checkArgument(Utilities.validateContainerPredefinedCapacities(skuCodeWisePredefinedCapacity), "total capacity cannot be zero or empty");

        this.warehouseId = warehouseId;
        this.containerId = containerId;
        this.skuCodeWisePredefinedCapacity = skuCodeWisePredefinedCapacity;
        this.creationTime = creationTime;
        this.modifiedTime = modifiedTime;
    }
}
