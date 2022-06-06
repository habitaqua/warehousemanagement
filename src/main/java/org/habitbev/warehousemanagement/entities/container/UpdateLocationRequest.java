package org.habitbev.warehousemanagement.entities.container;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.Capacity;

@Value
public class UpdateLocationRequest {

    String warehouseId;

    String locationId;

    int deltaCapacity;

    Capacity totalCapacity;


    @Builder
    private UpdateLocationRequest(String warehouseId, String locationId, Integer deltaCapacity, Capacity totalCapacity) {

        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(locationId), "locationId cannot be blank");

        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.deltaCapacity = deltaCapacity == null ? 0 : deltaCapacity;
        this.totalCapacity = totalCapacity;
    }
}
