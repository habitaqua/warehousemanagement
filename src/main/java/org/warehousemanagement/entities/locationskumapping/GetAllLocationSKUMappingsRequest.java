package org.warehousemanagement.entities.locationskumapping;

import lombok.Builder;
import lombok.Value;

@Value
public class GetAllLocationSKUMappingsRequest {

    String warehouseId;
    String locationId;

    @Builder
    public GetAllLocationSKUMappingsRequest(String warehouseId, String locationId) {
        this.warehouseId = warehouseId;
        this.locationId = locationId;
    }


}
