package org.habitbev.warehousemanagement.entities.inbound.inboundstatus;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class Closed implements InboundStatus{

    @Override
    public List<InboundStatus> previousStates() {
        return ImmutableList.of(new Active());
    }

    @Override
    public String toString() {
        return "CLOSED";
    }


}
