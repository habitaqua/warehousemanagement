package org.habitbev.warehousemanagement.entities.outbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;

@Value
public class OutboundDTO {

    private String warehouseId;
    private String outboundId;
    private OutboundStatus status;
    private String userId;

    private String customerId;

    private Long startTime;

    private Long endTime;

    private Long modifiedTime;

    @Builder
    public OutboundDTO(String warehouseId, String outboundId, OutboundStatus status, String userId, String customerId, Long startTime, Long endTime, Long modifiedTime) {
        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");

        this.warehouseId = warehouseId;
        this.outboundId = outboundId;
        this.status = status;
        this.userId = userId;
        this.customerId = customerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.modifiedTime = modifiedTime;
    }

    public FinishedGoodsOutbound toDbEntity() {
        FinishedGoodsOutbound outbound = FinishedGoodsOutbound.builder().warehouseId(this.getWarehouseId())
                .outboundId(this.getOutboundId()).customerId(this.getCustomerId()).startTime(this.getStartTime())
                .modifiedTime(this.getModifiedTime()).endTime(this.getEndTime()).outboundStatus(this.getStatus())
                .userId(this.getUserId()).build();
        return outbound;
    }
}
