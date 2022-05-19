package org.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

public class InboundStatusTypeConvertor implements DynamoDBTypeConverter<InboundStatus, String> {

    @Override
    public InboundStatus convert(String inboundStatusString) {
        switch (inboundStatusString) {
            case "ACTIVE":
                return new Active();
            case "CLOSED":
                return new Closed();

            default:
                throw new UnsupportedOperationException("No inbound status configured for " + inboundStatusString);
        }
    }

    @Override
    public String unconvert(InboundStatus inboundStatus) {
        return inboundStatus.getStatus();
    }
}
