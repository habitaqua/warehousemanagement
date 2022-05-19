package org.warehousemanagement.entities.location.locationstatus;

import com.google.common.collect.ImmutableSet;
import org.warehousemanagement.entities.dynamodb.Location;

import java.util.Set;

public class Available implements LocationStatus {
    @Override
    public Set<LocationStatus> previousStates() {
        return ImmutableSet.of(new Discontinued(), new Filled(), new PartiallyFilled());
    }

    @Override
    public String getStatus() {
        return "AVAILABLE";
    }
}
