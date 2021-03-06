package org.habitbev.warehousemanagement.lambda.container;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.habitbev.warehousemanagement.guice.MainModule;
import org.habitbev.warehousemanagement.service.ContainerService;

import java.util.Map;

public class GetContainerIdsHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {


    private ContainerService containerService;
    private ObjectMapper objectMapper;
    private Injector injector;

    public GetContainerIdsHandler(ContainerService containerService, ObjectMapper objectMapper, Injector injector) {
        this.injector = Guice.createInjector(new MainModule());
        this.containerService = injector.getInstance(ContainerService.class);
        this.objectMapper = injector.getInstance(ObjectMapper.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> stringObjectMap, Context context) {
        return null;
    }
}
