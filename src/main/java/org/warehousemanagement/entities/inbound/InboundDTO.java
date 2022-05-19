package org.warehousemanagement.entities.inbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

@Value
public class InboundDTO {

    private String warehouseId;
    private String inboundId;
    private InboundStatus status;
    private String userId;

    @Builder
    public InboundDTO(String warehouseId, String inboundId, InboundStatus status, String userId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(userId), "userId cannot be blank");

        this.warehouseId = warehouseId;
        this.inboundId = inboundId;
        this.status = status;
        this.userId = userId;
    }
}
