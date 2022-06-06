package org.warehousemanagement.entities.inbound.inboundstatus;

import java.util.List;

public interface InboundStatus {


    List<InboundStatus> previousStates();

    String getStatus();
}
