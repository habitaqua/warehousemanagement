package org.warehousemanagement.entities.inbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class AddInboundRequest {

    private String userId;
    private String warehouseId;

    @Builder
    private AddInboundRequest(String userId, String warehouseId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(userId),"UserId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(userId),"WarehouseId can't be blank");

        this.userId = userId;
        this.warehouseId = warehouseId;
    }
}
