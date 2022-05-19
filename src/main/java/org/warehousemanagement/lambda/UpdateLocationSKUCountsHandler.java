package org.warehousemanagement.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

public class UpdateLocationSKUCountsHandler implements RequestHandler<DynamodbEvent, Object> {


    @Override
    public Object handleRequest(DynamodbEvent dynamodbEvent, Context context) {
        return null;
    }
}
