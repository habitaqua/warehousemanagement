package org.habitbev.warehousemanagement.lambda.container;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.habitbev.warehousemanagement.service.ContainerService;
import org.habitbev.warehousemanagement.entities.container.AddContainerRequest;
import org.habitbev.warehousemanagement.guice.MainModule;

import java.util.Map;

/**
 * Takes in add location request
 * uypdates location database and auto assigns the location Id
 */
@Slf4j
public class AddContainerHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private ContainerService containerService;
    private ObjectMapper objectMapper;

    private Injector injector;

    public AddContainerHandler() {
        this.injector = Guice.createInjector(new MainModule());
        System.out.println("injection started1");
        this.containerService = injector.getInstance(ContainerService.class);
        System.out.println("injection started2");
        this.objectMapper = injector.getInstance(ObjectMapper.class);
        System.out.println("injection started3");

    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {

        try {
            AddContainerRequest addContainerRequest = objectMapper.readValue(String.valueOf(input.get("body")),
                    AddContainerRequest.class);
            String locationId = containerService.add(addContainerRequest);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(locationId)
                    .withIsBase64Encoded(false);
        } catch (IllegalArgumentException e) {
            log.error("invalid input", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withIsBase64Encoded(false);
        } catch (Exception e) {
            log.error("Exception occurred while adding location", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withIsBase64Encoded(false);
        }
    }
}
