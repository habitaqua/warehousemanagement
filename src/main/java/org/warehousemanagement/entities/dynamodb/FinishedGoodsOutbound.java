package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.dynamodb.typeconvertors.OutboundStatusTypeConvertor;
import org.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;

/**
 * Outbound Details. An outbound can have orders of only one customer.
 * outbound to customer mapping is 1:1
 * @author moduludu
 */
@Getter
@DynamoDBTable(tableName = "fg_outbounds")
@NoArgsConstructor
public class FinishedGoodsOutbound {

    @DynamoDBHashKey
    private String warehouseId;
    @DynamoDBRangeKey
    private String outboundId;
    @DynamoDBTypeConverted(converter = OutboundStatusTypeConvertor.class)
    @DynamoDBAttribute
    private OutboundStatus status;

    @DynamoDBAttribute
    private String customerId;
    @DynamoDBAttribute
    private String userId;
    @DynamoDBAttribute
    private long startTime;
    @DynamoDBAttribute
    private long endTime;
    @DynamoDBAttribute
    private long modifiedTime;

    @Builder
    private FinishedGoodsOutbound(String warehouseId, String outboundId, String customerId, OutboundStatus status, String userId, Long startTime, Long endTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(customerId), "customerId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(userId), "userId cannot be blank");
        Preconditions.checkArgument(startTime != null, "startTime cannot be null");

        this.warehouseId = warehouseId;
        this.outboundId = outboundId;
        this.customerId = customerId;
        this.status = status;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.modifiedTime = modifiedTime;
    }
}
