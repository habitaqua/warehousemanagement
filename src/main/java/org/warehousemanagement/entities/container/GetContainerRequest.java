package org.warehousemanagement.entities.container;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class GetContainerRequest {

    String warehouseId;
    String containerId;

    @Builder
    public GetContainerRequest(String warehouseId, String containerId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");

        this.warehouseId = warehouseId;
        this.containerId = containerId;
    }
}
