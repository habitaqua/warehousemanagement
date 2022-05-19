package org.warehousemanagement.entities.location.locationstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Filled  implements LocationStatus {
    @Override
    public Set<LocationStatus> previousStates() {
        return ImmutableSet.of(new PartiallyFilled());
    }

    @Override
    public String getStatus() {
        return "FILLED";
    }
}
