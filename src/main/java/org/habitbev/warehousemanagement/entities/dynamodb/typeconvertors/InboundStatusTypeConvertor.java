package org.habitbev.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

public class InboundStatusTypeConvertor implements DynamoDBTypeConverter<String, InboundStatus> {

    @Override
    public String convert(InboundStatus inboundStatus) {
        return inboundStatus.toString();
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
