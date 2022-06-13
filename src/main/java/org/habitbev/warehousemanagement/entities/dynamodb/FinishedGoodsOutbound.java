package org.habitbev.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.dynamodb.typeconvertors.OutboundStatusTypeConvertor;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;

/**
 * Outbound Details. An outbound can have orders of only one customer.
 * outbound to customer mapping is 1:1
 * @author moduludu
 */
@Data
@DynamoDBTable(tableName = "fg-outbounds")
@NoArgsConstructor
public class FinishedGoodsOutbound {

    @DynamoDBHashKey
    private String warehouseId;
    @DynamoDBRangeKey
    private String outboundId;
    @DynamoDBTypeConverted(converter = OutboundStatusTypeConvertor.class)
    @DynamoDBAttribute
    private OutboundStatus outboundStatus;

    @DynamoDBAttribute
    private String customerId;
    @DynamoDBAttribute
    private String userId;
    @DynamoDBAttribute
    private Long startTime;
    @DynamoDBAttribute
    private Long endTime;
    @DynamoDBAttribute
    private Long modifiedTime;

    @Builder
    private FinishedGoodsOutbound(String warehouseId, String outboundId, String customerId, OutboundStatus outboundStatus, String userId, Long startTime, Long endTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");

        this.warehouseId = warehouseId;
        this.outboundId = outboundId;
        this.customerId = customerId;
        this.outboundStatus = outboundStatus;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.modifiedTime = modifiedTime;
    }
}
