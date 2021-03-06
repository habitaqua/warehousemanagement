package org.habitbev.warehousemanagement.lambda.outbound;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.habitbev.warehousemanagement.entities.outbound.EndOutboundRequest;
import org.habitbev.warehousemanagement.guice.MainModule;
import org.habitbev.warehousemanagement.service.OutboundService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EndOutboundHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private OutboundService outboundService;
    private ObjectMapper objectMapper;
    private Injector injector;

    public EndOutboundHandler() {
        this.injector = Guice.createInjector(new MainModule());
        this.outboundService = injector.getInstance(OutboundService.class);
        this.objectMapper = injector.getInstance(ObjectMapper.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        try {
            Preconditions.checkArgument(input != null, "EndOutboundHandler.input cannot be null");
            EndOutboundRequest endOutboundRequest = objectMapper.readValue(String.valueOf(input.get("body")),
                    EndOutboundRequest.class);
            outboundService.endOutbound(endOutboundRequest);
            Map<String, String> response = new HashMap<>();
            response.put("outboundId", endOutboundRequest.getOutboundId());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(response))
                    .withIsBase64Encoded(false);
        } catch (IllegalArgumentException e) {
            log.error("invalid input for end outbound request", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withIsBase64Encoded(false);
        } catch (Exception e) {
            log.error("Exception occurred while adding location", e);
            return new APIGatewayProxyResponseEvent()
                    .withBody(e.getMessage())
                    .withStatusCode(500)
                    .withIsBase64Encoded(false);
        }
    }
}
