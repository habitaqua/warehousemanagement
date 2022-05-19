package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.dynamodb.typeconvertors.InboundStatusTypeConvertor;
import org.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

@Getter
@DynamoDBTable(tableName = "inbounds")
@NoArgsConstructor
public class Inbound {

    @DynamoDBHashKey
    private String warehouseId;
    @DynamoDBRangeKey
    private String inboundId;
    @DynamoDBTypeConverted(converter = InboundStatusTypeConvertor.class)
    @DynamoDBAttribute
    private InboundStatus status;
    @DynamoDBAttribute
    private String userId;
    @DynamoDBAttribute
    private long startTime;
    @DynamoDBAttribute
    private long endTime;
    @DynamoDBAttribute
    private long modifiedTime;

    @Builder
    private Inbound(String warehouseId,String inboundId, InboundStatus status, String userId, Long startTime, Long endTime, Long modifiedTime) {

        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(userId), "userId cannot be blank");
        Preconditions.checkArgument(startTime != null, "startTime cannot be null");

        this.warehouseId = warehouseId;
        this.inboundId = inboundId;
        this.status = status;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.modifiedTime = modifiedTime;
    }
}
