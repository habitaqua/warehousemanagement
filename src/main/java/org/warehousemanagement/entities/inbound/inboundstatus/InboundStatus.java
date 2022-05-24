package org.warehousemanagement.entities.inbound.inboundstatus;

import java.util.Set;

public interface InboundStatus {


    Set<InboundStatus> previousStates();

    String getStatus();
}
