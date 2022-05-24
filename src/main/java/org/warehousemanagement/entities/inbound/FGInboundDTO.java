package org.warehousemanagement.entities.inbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

@Value
public class FGInboundDTO {

    private String warehouseId;
    private String inboundId;
    private InboundStatus status;
    private String userId;

    private long startTime;

    private long endTime;

    @Builder
    public FGInboundDTO(String warehouseId, String inboundId, InboundStatus status, String userId,
                        Long startTime, Long endTime) {
        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");

        this.warehouseId = warehouseId;
        this.inboundId = inboundId;
        this.status = status;
        this.userId = userId;
        this.startTime =startTime;
        this.endTime = endTime;
    }

   public FinishedGoodsInbound toDbEntity() {
        FinishedGoodsInbound inbound = FinishedGoodsInbound.builder().warehouseId(this.getWarehouseId())
                .inboundId(this.getInboundId()).startTime(this.getStartTime()).modifiedTime(this.getEndTime())
                .status(this.getStatus()).userId(this.getUserId()).build();
        return inbound;
    }
}
