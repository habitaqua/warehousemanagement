package org.warehousemanagement.entities.inbound.inboundstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Closed implements InboundStatus{

    @Override
    public Set<InboundStatus> previousStates() {
        return ImmutableSet.of(new Active());
    }

    @Override
    public String getStatus() {
        return "CLOSED";
    }
}