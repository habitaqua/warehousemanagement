package org.habitbev.warehousemanagement.entities.inbound;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

@Value
public class FGInboundDTO {

    private String warehouseId;
    private String inboundId;
    private InboundStatus status;
    private String userId;

    private Long startTime;

    private Long endTime;

    private Long modifiedTime;

    @Builder
    public FGInboundDTO(String warehouseId, String inboundId, InboundStatus status, String userId,
                        Long startTime, Long endTime, Long modifiedTime) {
        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");

        this.warehouseId = warehouseId;
        this.inboundId = inboundId;
        this.status = status;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.modifiedTime = modifiedTime;
    }

    public static FGInboundDTO fromDbEntity(FinishedGoodsInbound db) {
        Preconditions.checkArgument(db != null, "db entory cannot be null");
        return FGInboundDTO.builder().inboundId(db.getInboundId()).warehouseId(db.getWarehouseId())
                .userId(db.getUserId()).status(db.getInboundStatus()).modifiedTime(db.getModifiedTime())
                .startTime(db.getStartTime()).endTime(db.getEndTime()).build();

    }

    public FinishedGoodsInbound toDbEntity() {
        FinishedGoodsInbound inbound = FinishedGoodsInbound.builder().warehouseId(this.getWarehouseId())
                .inboundId(this.getInboundId()).startTime(this.getStartTime()).endTime(this.getEndTime())
                .modifiedTime(this.modifiedTime).inboundStatus(this.getStatus()).userId(this.getUserId()).build();
        return inbound;
    }
}
