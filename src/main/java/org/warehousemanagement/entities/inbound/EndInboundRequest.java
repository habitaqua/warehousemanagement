package org.warehousemanagement.entities.inbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

/**
 * Ends the given inbound id at the given warehouseId
 */
@Value
public class EndInboundRequest {

    private String inboundId;
    private String warehouseId;

    @Builder
    private EndInboundRequest(String inboundId, String warehouseId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId),"inboundId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId),"WarehouseId can't be blank");

        this.inboundId = inboundId;
        this.warehouseId = warehouseId;
    }
}
