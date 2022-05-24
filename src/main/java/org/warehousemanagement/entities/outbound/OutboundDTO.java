package org.warehousemanagement.entities.outbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;

@Value
public class OutboundDTO {

    private String warehouseId;
    private String outboundId;
    private OutboundStatus status;
    private String userId;

    private long startTime;

    private long endTime;

    @Builder
    public OutboundDTO(String warehouseId, String outboundId, OutboundStatus status, String userId, Long startTime, Long endTime) {
        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(userId), "userId cannot be blank");
        Preconditions.checkArgument(startTime!=null, "startTime cannot be null");
        Preconditions.checkArgument(endTime!=null, "endTime cannot be null");



        this.warehouseId = warehouseId;
        this.outboundId = outboundId;
        this.status = status;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public FinishedGoodsOutbound toDbEntity() {
        FinishedGoodsOutbound outbound = FinishedGoodsOutbound.builder().warehouseId(this.getWarehouseId())
                .outboundId(this.getOutboundId()).startTime(this.getStartTime()).modifiedTime(this.getEndTime())
                .status(this.getStatus()).userId(this.getUserId()).build();
        return outbound;
    }
}
