package org.habitbev.warehousemanagement.entities.outbound;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor
public class StartOutboundRequest {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("companyId")
    private String companyId;
    @JsonProperty("warehouseId")
    private String warehouseId;
    @JsonProperty("customerId")
    private String customerId;

    @Builder
    private StartOutboundRequest(String userId, String warehouseId, String customerId, String companyId) {

        Preconditions.checkArgument(StringUtils.isNotBlank(userId),"UserId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId),"WarehouseId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(customerId),"customerId can't be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId),"companyId can't be blank");



        this.userId = userId;
        this.warehouseId = warehouseId;
        this.customerId = customerId;
        this.companyId = companyId;
    }
}
