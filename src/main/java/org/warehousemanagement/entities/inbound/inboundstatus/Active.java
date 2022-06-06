package org.warehousemanagement.entities.inbound.inboundstatus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

public class Active implements InboundStatus{
    @Override
    public List<InboundStatus> previousStates() {
        return ImmutableList.of(new Active());
    }

    @Override
    public String getStatus() {
        return "ACTIVE";
    }
}
