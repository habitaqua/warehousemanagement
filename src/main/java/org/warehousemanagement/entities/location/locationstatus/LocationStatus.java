package org.warehousemanagement.entities.location.locationstatus;

import java.util.Set;

public interface LocationStatus {


    Set<LocationStatus> previousStates();

    String getStatus();
}
