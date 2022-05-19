package org.warehousemanagement.lambda.location;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.warehousemanagement.entities.location.AddLocationRequest;
import org.warehousemanagement.guice.MainModule;
import org.warehousemanagement.service.location.LocationService;

import java.util.Map;

/**
 * Takes in add location request
 * uypdates location database and auto assigns the location Id
 */
@Slf4j
public class AddLocationHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private LocationService locationService;
    private ObjectMapper objectMapper;

    private Injector injector;

    public AddLocationHandler() {
        this.injector = Guice.createInjector(new MainModule());
        System.out.println("injection started1");
        this.locationService = injector.getInstance(LocationService.class);
        System.out.println("injection started2");
        this.objectMapper = injector.getInstance(ObjectMapper.class);
        System.out.println("injection started3");

    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {

        try {
            AddLocationRequest addLocationRequest = objectMapper.readValue(String.valueOf(input.get("body")),
                    AddLocationRequest.class);
            String locationId = locationService.add(addLocationRequest);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(locationId)
                    .withIsBase64Encoded(false);
        } catch (Exception e) {
            log.error("Exception occurred while adding location", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withIsBase64Encoded(false);
        }
    }
}
