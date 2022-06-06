package org.habitbev.warehousemanagement.entities.outbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class StartOutboundRequest {

    private String userId;
    private String warehouseId;

    private String customerId;

    @Builder
    private StartOutboundRequest(String userId, String warehouseId, String customerId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(userId),"UserId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId),"WarehouseId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(customerId),"customerId can't be blank");


        this.userId = userId;
        this.warehouseId = warehouseId;
        this.customerId = customerId;
    }
}
