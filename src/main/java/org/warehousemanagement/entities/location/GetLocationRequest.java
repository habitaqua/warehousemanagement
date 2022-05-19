package org.warehousemanagement.entities.location;

import lombok.Builder;
import lombok.Value;

@Value
public class GetLocationRequest {

    String warehouseId;
    String locationId;

    @Builder
    public GetLocationRequest(String warehouseId, String locationId) {
        this.warehouseId = warehouseId;
        this.locationId = locationId;
    }
}
