package org.habitbev.warehousemanagement.entities.inbound;

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
public class EndInboundRequest {

    @JsonProperty("inboundId")
    private String inboundId;
    @JsonProperty("warehouseId")
    private String warehouseId;

    @Builder
    private EndInboundRequest(String inboundId, String warehouseId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId),"inboundId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId),"WarehouseId can't be blank");

        this.inboundId = inboundId;
        this.warehouseId = warehouseId;
    }
}
