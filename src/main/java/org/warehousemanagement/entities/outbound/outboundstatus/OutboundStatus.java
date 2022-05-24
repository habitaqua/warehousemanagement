package org.warehousemanagement.entities.outbound.outboundstatus;

import java.util.Set;

public interface OutboundStatus {


    Set<OutboundStatus> previousStates();

    String getStatus();
}
