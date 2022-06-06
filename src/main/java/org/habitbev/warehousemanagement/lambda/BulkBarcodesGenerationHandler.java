package org.habitbev.warehousemanagement.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.habitbev.warehousemanagement.entities.SKUBarcodesGenerationRequest;
import org.habitbev.warehousemanagement.service.barcodes.SKUBulkBarcodesCreationService;
import org.habitbev.warehousemanagement.guice.MainModule;

import java.util.Map;

public class BulkBarcodesGenerationHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private org.habitbev.warehousemanagement.service.barcodes.SKUBulkBarcodesCreationService SKUBulkBarcodesCreationService;
    private ObjectMapper objectMapper;
    private Injector injector;


    public BulkBarcodesGenerationHandler() {
        System.out.println("injection started");
        this.injector = Guice.createInjector(new MainModule());
        System.out.println("injection started1");
        this.SKUBulkBarcodesCreationService = injector.getInstance(SKUBulkBarcodesCreationService.class);
        System.out.println("injection started2");
        this.objectMapper = injector.getInstance(ObjectMapper.class);
        System.out.println("injection started3");


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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}