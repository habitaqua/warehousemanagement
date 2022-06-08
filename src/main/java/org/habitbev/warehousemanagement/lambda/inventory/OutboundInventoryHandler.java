package org.habitbev.warehousemanagement.lambda.inventory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.habitbev.warehousemanagement.entities.inventory.InventoryOutboundRequest;
import org.habitbev.warehousemanagement.guice.MainModule;
import org.habitbev.warehousemanagement.service.InventoryService;

import java.util.Map;

@Slf4j
public class OutboundInventoryHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private InventoryService inventoryService;
    private ObjectMapper objectMapper;

    private Injector injector;

    public OutboundInventoryHandler() {
        this.injector = Guice.createInjector(new MainModule());
        System.out.println("injection started1");
        this.inventoryService = injector.getInstance(InventoryService.class);
        System.out.println("injection started2");
        this.objectMapper = injector.getInstance(ObjectMapper.class);
        System.out.println("injection started3");

    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        try {
            InventoryOutboundRequest inventoryOutboundRequest = objectMapper.readValue(String.valueOf(input.get("body")),
                    InventoryOutboundRequest.class);
            inventoryService.outbound(inventoryOutboundRequest);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody("success")
                    .withIsBase64Encoded(false);
        } catch (IllegalArgumentException e) {
            log.error("invalid input", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(e.getMessage())
                    .withIsBase64Encoded(false);
        } catch (Exception e) {
            log.error("Exception occurred while adding location", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody(e.getCause().getMessage())
                    .withIsBase64Encoded(false);
        }
    }

}
