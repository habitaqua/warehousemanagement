package org.warehousemanagement.entities.location;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.Capacity;
import org.warehousemanagement.entities.UOM;
import org.warehousemanagement.entities.dynamodb.Location;
import org.warehousemanagement.entities.location.locationstatus.LocationStatus;

import java.util.HashMap;
import java.util.Map;

@Data
public class LocationDTO {

    String locationId;
    String warehouseId;
    int totalCapacity;
    int currentCapacity;
    UOM uom;
    LocationStatus status;
    Map<String, Capacity> locationSKUDetails;


    @Builder
    private LocationDTO(String locationId, String warehouseId, Integer totalCapacity, Integer currentCapacity,
                        UOM uom, LocationStatus status, Map<String, Capacity> locationSKUDetails) {

        Preconditions.checkArgument(StringUtils.isNotBlank(locationId), "id cannot be empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");
        Preconditions.checkArgument(totalCapacity > 0, "total capacity cannot be zero");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");
        Preconditions.checkArgument(currentCapacity <= totalCapacity && currentCapacity >= 0,
                "current capacity not in range");
        Preconditions.checkArgument(status != null, "location status cannot be null");
        Preconditions.checkArgument(uom != null, "uom  cannot be null");

        this.locationId = locationId;
        this.warehouseId = warehouseId;
        this.totalCapacity = totalCapacity;
        this.currentCapacity = currentCapacity;
        this.uom = uom;
        this.status = status;
        this.locationSKUDetails = locationSKUDetails == null? new HashMap<>(): locationSKUDetails;
    }

    public static LocationDTO fromLocation(Location location) {
        return LocationDTO.builder().locationId(location.getLocationId()).warehouseId(location.getWarehouseId())
                        .currentCapacity(location.getCurrentCapacity()).totalCapacity(location.getTotalCapacity())
                        .uom(location.getUom()).status(location.getStatus()).build();
    }
}
