package org.habitbev.warehousemanagement.lambda.inventory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public class MoveInventoryHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> stringObjectMap, Context context) {
        return null;
    }
}
