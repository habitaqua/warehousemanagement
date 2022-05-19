package org.warehousemanagement.idGenerators;

import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.Location;
import org.warehousemanagement.entities.location.AddLocationRequest;
import org.warehousemanagement.sao.LocationDbSAO;

import java.util.Optional;

/**
 * generates location id incrementally location wise
 */
public class WarehouseWiseIncrementalLocationIdGenerator implements LocationIdGenerator<AddLocationRequest> {
    private static final String LOC = "LOC-";
    private static final String FIRST_LOCATION_ID = LOC + 1;
    LocationDbSAO locationDbSAO;

    @Inject
    public WarehouseWiseIncrementalLocationIdGenerator(LocationDbSAO locationDbSAO) {
        this.locationDbSAO = locationDbSAO;
    }

    @Override
    public String generate(AddLocationRequest addLocationRequest) {
        String warehouseId = addLocationRequest.getWarehouseId();
        Optional<Location> lastAddedLocation = locationDbSAO.getLastAddedLocation(warehouseId);
        if(lastAddedLocation.isPresent())
        {
            String locationId = lastAddedLocation.get().getLocationId();
            Integer number = Integer.valueOf(locationId.split(LOC)[1]);
            return LOC + (number + 1);
        }
        return FIRST_LOCATION_ID;
    }
}
