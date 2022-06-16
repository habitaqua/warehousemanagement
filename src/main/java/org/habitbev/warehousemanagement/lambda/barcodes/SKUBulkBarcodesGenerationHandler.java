package org.habitbev.warehousemanagement.lambda.barcodes;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.habitbev.warehousemanagement.entities.SKUBarcodesGenerationRequest;
import org.habitbev.warehousemanagement.service.SKUBulkBarcodesCreationService;
import org.habitbev.warehousemanagement.guice.MainModule;

import java.util.Map;

@Slf4j
public class SKUBulkBarcodesGenerationHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private org.habitbev.warehousemanagement.service.SKUBulkBarcodesCreationService SKUBulkBarcodesCreationService;
    private ObjectMapper objectMapper;
    private Injector injector;


    public SKUBulkBarcodesGenerationHandler() {
        this.injector = Guice.createInjector(new MainModule());
        this.SKUBulkBarcodesCreationService = injector.getInstance(SKUBulkBarcodesCreationService.class);
        this.objectMapper = injector.getInstance(ObjectMapper.class);


    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {

        try {
            SKUBarcodesGenerationRequest request = objectMapper.readValue(String.valueOf(input.get("body")),
                    SKUBarcodesGenerationRequest.class);

            String downloadURL = SKUBulkBarcodesCreationService.generate(request);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(downloadURL)
                    .withIsBase64Encoded(false);
        } catch (IllegalArgumentException e) {
            log.error("invalid input for end inbound request", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(e.getMessage())
                    .withIsBase64Encoded(false);
        } catch (Exception e) {
            log.error("Exception occurred while inbounding", e);
            String causeMessage = e.getCause()!=null?e.getCause().getMessage(): "";
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody(e.getMessage()+causeMessage)
                    .withIsBase64Encoded(false);
        }
    }
}