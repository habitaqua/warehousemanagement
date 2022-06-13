package org.habitbev.warehousemanagement.lambda.inbound;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.habitbev.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.habitbev.warehousemanagement.guice.MainModule;
import org.habitbev.warehousemanagement.service.OutboundService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StartOutboundHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {
    private OutboundService outboundService;
    private ObjectMapper objectMapper;
    private Injector injector;

    public StartOutboundHandler() {
        this.injector = Guice.createInjector(new MainModule());
        this.outboundService = injector.getInstance(OutboundService.class);
        this.objectMapper = injector.getInstance(ObjectMapper.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        try {
            Preconditions.checkArgument(input != null , "StartOutboundHandler.input cannot be null");
            StartOutboundRequest startOutboundRequest = objectMapper.readValue(String.valueOf(input.get("body")),
                    StartOutboundRequest.class);
            String outboundId = outboundService.startOutbound(startOutboundRequest);
            Map<String, String> response = new HashMap<>();
            response.put("outboundId", outboundId);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(response))
                    .withIsBase64Encoded(false);
        } catch (IllegalArgumentException e) {
            log.error("invalid input for start inbound request", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(e.getMessage())
                    .withIsBase64Encoded(false);
        } catch (Exception e) {
            log.error("Exception occurred while adding location", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody(e.getMessage())
                    .withIsBase64Encoded(false);
        }
    }
}
