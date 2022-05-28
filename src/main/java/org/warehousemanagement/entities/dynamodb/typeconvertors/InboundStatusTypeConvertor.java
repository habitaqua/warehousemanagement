package org.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

public class InboundStatusTypeConvertor implements DynamoDBTypeConverter<String, InboundStatus> {

    @Override
    public String convert(InboundStatus inboundStatus) {
        return inboundStatus.getStatus();
    }
    @Override
    public InboundStatus unconvert(String inboundStatusString) {
        switch (inboundStatusString) {
            case "ACTIVE":
                return new Active();
            case "CLOSED":
                return new Closed();

            default:
                throw new UnsupportedOperationException("No inbound status configured for " + inboundStatusString);
        }
    }


}
