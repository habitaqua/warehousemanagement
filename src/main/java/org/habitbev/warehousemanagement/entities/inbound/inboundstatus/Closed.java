package org.habitbev.warehousemanagement.entities.inbound.inboundstatus;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Closed implements InboundStatus{

    @Override
    public List<InboundStatus> previousStates() {
        return ImmutableList.of(new Active());
    }

    @Override
    public String getStatus() {
        return "CLOSED";
    }
}
