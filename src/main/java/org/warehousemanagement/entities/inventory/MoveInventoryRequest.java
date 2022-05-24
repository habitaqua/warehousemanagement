package org.warehousemanagement.entities.inventory;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Value
public class MoveInventoryRequest {

    List<String> uniqueProductIds;
    String sourceContainerId;

    String destinationContainerId;

    String warehouseId;

    String skuCode;

    int sourceContainerMaxCapacity;
    int destinationContainerMaxCapacity;


    @Builder
    private MoveInventoryRequest(List<String> uniqueProductIds, String sourceContainerId,String skuCode,
                                 String destinationContainerId, String warehouseId, Integer sourceContainerMaxCapacity, Integer destinationContainerMaxCapacity) {

        Preconditions.checkArgument(CollectionUtils.isNotEmpty(uniqueProductIds), "uniqueProductIds cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(sourceContainerId), "sourceLocationId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(destinationContainerId), "destinationLocationId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "locationId cannot be blank");
        Preconditions.checkArgument(sourceContainerMaxCapacity != null && sourceContainerMaxCapacity> 0 , "sourceContainerMaxCapacity cannot be < 0");
        Preconditions.checkArgument(destinationContainerMaxCapacity != null && destinationContainerMaxCapacity> 0 , "destinationContainerMaxCapacity cannot be < 0");


        this.uniqueProductIds = uniqueProductIds;
        this.sourceContainerId = sourceContainerId;
        this.destinationContainerId = destinationContainerId;
        this.warehouseId = warehouseId;
        this.skuCode = skuCode;
        this.sourceContainerMaxCapacity = sourceContainerMaxCapacity;
        this.destinationContainerMaxCapacity = destinationContainerMaxCapacity;
    }
}
