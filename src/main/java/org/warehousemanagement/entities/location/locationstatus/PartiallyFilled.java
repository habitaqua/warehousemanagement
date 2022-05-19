package org.warehousemanagement.entities.location.locationstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class PartiallyFilled implements LocationStatus {
    @Override
    public Set<LocationStatus> previousStates() {
        return ImmutableSet.of(new Available(), new Filled());
    }

    @Override
    public String getStatus() {
        return "PARTIALLY_FILLED";
    }
}