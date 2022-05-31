package org.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.warehousemanagement.entities.container.containerstatus.*;

public class ContainerCapacityStatusTypeConvertor implements DynamoDBTypeConverter<String, ContainerStatus> {
    @Override
    public ContainerStatus unconvert(String locationStatusString) {
        switch (locationStatusString) {
            case "AVAILABLE":
                return new Available();
            case "FILLED":
                return new Filled();
            case "PARTIALLY_FILLED":
                return new PartiallyFilled();
            case "DISCONTINUED":
                return new Discontinued();
            default:
                throw  new UnsupportedOperationException("No location status configured for "+ locationStatusString);
        }
    }

    @Override
    public String convert(ContainerStatus containerStatus) {
        return containerStatus.getStatus();
    }
}
