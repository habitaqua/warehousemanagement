package org.warehousemanagement.entities.outbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

/**
 * Ends the given inbound id at the given warehouseId
 */
@Value
public class EndOutboundRequest {

    private String outboundId;
    private String warehouseId;

    @Builder
    private EndOutboundRequest(String outboundId, String warehouseId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId),"outboundId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId),"WarehouseId can't be blank");

        this.outboundId = outboundId;
        this.warehouseId = warehouseId;
    }
}
