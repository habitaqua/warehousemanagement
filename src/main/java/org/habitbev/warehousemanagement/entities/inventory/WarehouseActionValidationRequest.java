package org.habitbev.warehousemanagement.entities.inventory;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseAction;

@Value
public class WarehouseActionValidationRequest {


    WarehouseAction warehouseAction;
    String skuCode;
    String inboundId;
    String outboundId;
    String containerId;
    String orderId;
    String customerId;

    String userId;
    String warehouseId;
    String companyId;
    Integer capacityToInbound;
    Integer capacityToOutbound;


    @Builder
    private WarehouseActionValidationRequest(WarehouseAction warehouseAction, String skuCode, String inboundId, String outboundId, String containerId, String orderId,
                                             String customerId, String userId, String warehouseId, String companyId, Integer capacityToInbound, Integer capacityToOutbound) {

        Preconditions.checkArgument(warehouseAction!=null, "warehouseAction cannot be null");
        this.warehouseAction = warehouseAction;
        this.skuCode = skuCode;
        this.inboundId = inboundId;
        this.outboundId = outboundId;
        this.containerId = containerId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.userId = userId;
        this.warehouseId = warehouseId;
        this.companyId = companyId;
        this.capacityToInbound = capacityToInbound;
        this.capacityToOutbound = capacityToOutbound;
    }
}
