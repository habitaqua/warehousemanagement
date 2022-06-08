package org.habitbev.warehousemanagement.entities.inbound;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor
public class StartInboundRequest {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("warehouseId")
    private String warehouseId;

    @Builder
    private StartInboundRequest(String userId, String warehouseId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(userId),"UserId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(userId),"WarehouseId can't be blank");

        this.userId = userId;
        this.warehouseId = warehouseId;
    }
}
