package org.warehousemanagement.entities.location.locationstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Discontinued implements LocationStatus {
    @Override
    public Set<LocationStatus> previousStates() {
        return ImmutableSet.of(new Available());
    }

    @Override
    public String getStatus() {
        return "DISCONTINUED";
    }
}