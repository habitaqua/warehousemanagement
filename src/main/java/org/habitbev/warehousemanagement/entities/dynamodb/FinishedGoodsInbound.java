package org.habitbev.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.dynamodb.typeconvertors.InboundStatusTypeConvertor;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

/**
 * @author moduludu
 * Finished Goods Inbound Details
 */
@Data
@DynamoDBTable(tableName = "fg-inbounds")
@NoArgsConstructor
public class FinishedGoodsInbound {

    @DynamoDBHashKey
    private String warehouseId;
    @DynamoDBRangeKey
    private String inboundId;
    @DynamoDBTypeConverted(converter = InboundStatusTypeConvertor.class)
    @DynamoDBAttribute
    private InboundStatus inboundStatus;
    @DynamoDBAttribute
    private String userId;
    @DynamoDBAttribute
    private Long startTime;
    @DynamoDBAttribute
    private Long endTime;
    @DynamoDBAttribute
    private Long modifiedTime;

    @Builder
    private FinishedGoodsInbound(String warehouseId, String inboundId, InboundStatus inboundStatus, String userId, Long startTime, Long endTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");

        this.warehouseId = warehouseId;
        this.inboundId = inboundId;
        this.inboundStatus = inboundStatus;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.modifiedTime = modifiedTime;
    }
}
