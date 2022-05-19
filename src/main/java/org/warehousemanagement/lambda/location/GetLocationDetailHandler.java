package org.warehousemanagement.lambda.location;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.warehousemanagement.entities.location.AddLocationRequest;
import org.warehousemanagement.entities.location.GetLocationRequest;
import org.warehousemanagement.entities.location.LocationDTO;
import org.warehousemanagement.guice.MainModule;
import org.warehousemanagement.service.location.LocationService;

import java.util.Map;
import java.util.Optional;

public class GetLocationDetailHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private LocationService locationService;
    private ObjectMapper objectMapper;

    private Injector injector;

    public GetLocationDetailHandler() {
        this.injector = Guice.createInjector(new MainModule());
        this.locationService = injector.getInstance(LocationService.class);
        this.objectMapper = injector.getInstance(ObjectMapper.class);

    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        try {
            GetLocationRequest getLocationRequest = objectMapper.readValue(String.valueOf(input.get("body")),
                    GetLocationRequest.class);
            Optional<LocationDTO> locationDTO = locationService.getLocation(getLocationRequest);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(locationDTO))
                    .withIsBase64Encoded(false);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
