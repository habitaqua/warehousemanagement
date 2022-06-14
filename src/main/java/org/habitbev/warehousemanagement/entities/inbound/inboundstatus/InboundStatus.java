package org.habitbev.warehousemanagement.entities.inbound.inboundstatus;

import lombok.EqualsAndHashCode;

import java.util.List;


public interface InboundStatus {


    List<InboundStatus> previousStates();

    @Override
    String toString();

}
