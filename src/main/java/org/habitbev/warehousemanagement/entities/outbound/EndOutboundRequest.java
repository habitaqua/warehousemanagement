package org.habitbev.warehousemanagement.entities.outbound;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Ends the given inbound id at the given warehouseId
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor
public class EndOutboundRequest {

    @JsonProperty("outboundId")
    private String outboundId;

    @JsonProperty("warehouseId")
    private String warehouseId;

    @Builder
    private EndOutboundRequest(String outboundId, String warehouseId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "WarehouseId can't be blank");

        this.outboundId = outboundId;
        this.warehouseId = warehouseId;
    }
}
