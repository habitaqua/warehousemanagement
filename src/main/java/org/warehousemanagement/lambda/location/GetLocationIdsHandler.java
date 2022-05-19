package org.warehousemanagement.lambda.location;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.warehousemanagement.guice.MainModule;
import org.warehousemanagement.service.location.LocationService;

import java.util.Map;

public class GetLocationIdsHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {


    private LocationService locationService;
    private ObjectMapper objectMapper;
    private Injector injector;

    public GetLocationIdsHandler(LocationService locationService, ObjectMapper objectMapper, Injector injector) {
        this.injector = Guice.createInjector(new MainModule());
        this.locationService = injector.getInstance(LocationService.class);
        this.objectMapper = injector.getInstance(ObjectMapper.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> stringObjectMap, Context context) {
        return null;
    }
}
