package org.habitbev.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;

public class OutboundStatusTypeConvertor implements DynamoDBTypeConverter<String, OutboundStatus> {

    @Override
    public String convert(OutboundStatus outboundStatus) {
        return outboundStatus.toString();
    }
    @Override
    public OutboundStatus unconvert(String outboundStatusString) {
        switch (outboundStatusString) {
            case "ACTIVE":
                return new Active();
            case "CLOSED":
                return new Closed();

            default:
                throw new UnsupportedOperationException("No outbound status configured for " + outboundStatusString);
        }
    }


}
