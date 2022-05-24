package org.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;

public class OutboundStatusTypeConvertor implements DynamoDBTypeConverter<OutboundStatus, String> {

    @Override
    public OutboundStatus convert(String outboundStatusString) {
        switch (outboundStatusString) {
            case "ACTIVE":
                return new Active();
            case "CLOSED":
                return new Closed();

            default:
                throw new UnsupportedOperationException("No outbound status configured for " + outboundStatusString);
        }
    }

    @Override
    public String unconvert(OutboundStatus outboundStatus) {
        return outboundStatus.getStatus();
    }
}
