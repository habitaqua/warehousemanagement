package org.warehousemanagement.entities.outbound.outboundstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Active implements OutboundStatus {
    @Override
    public Set<OutboundStatus> previousStates() {
        return ImmutableSet.of();
    }

    @Override
    public String getStatus() {
        return "ACTIVE";
    }
}
