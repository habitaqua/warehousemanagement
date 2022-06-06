package org.habitbev.warehousemanagement.entities.outbound.outboundstatus;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Active implements OutboundStatus {
    @Override
    public Set<OutboundStatus> previousStates() {
        return ImmutableSet.of(new Active());
    }

    @Override
    public String toString() {
        return "ACTIVE";
    }


}
