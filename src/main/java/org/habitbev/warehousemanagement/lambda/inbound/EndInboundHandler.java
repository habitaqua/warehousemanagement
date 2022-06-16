package org.habitbev.warehousemanagement.lambda.inbound;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.habitbev.warehousemanagement.entities.inbound.EndInboundRequest;
import org.habitbev.warehousemanagement.guice.MainModule;
import org.habitbev.warehousemanagement.service.InboundService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EndInboundHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private InboundService inboundService;
    private ObjectMapper objectMapper;
    private Injector injector;

    public EndInboundHandler() {
        this.injector = Guice.createInjector(new MainModule());
        this.inboundService = injector.getInstance(InboundService.class);
        this.objectMapper = injector.getInstance(ObjectMapper.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        try {
            Preconditions.checkArgument(input != null, "EndInboundHandler.input cannot be null");
            EndInboundRequest endInboundRequest = objectMapper.readValue(String.valueOf(input.get("body")),
                    EndInboundRequest.class);
            inboundService.endInbound(endInboundRequest);
            Map<String, String> response = new HashMap<>();
            response.put("inboundId", endInboundRequest.getInboundId());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(response))
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
