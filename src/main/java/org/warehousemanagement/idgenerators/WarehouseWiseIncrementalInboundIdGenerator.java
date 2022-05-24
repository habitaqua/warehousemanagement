package org.warehousemanagement.idgenerators;

import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.warehousemanagement.entities.inbound.StartInboundRequest;
import org.warehousemanagement.dao.InboundDynamoDAOImpl;

import java.util.Optional;

public class WarehouseWiseIncrementalInboundIdGenerator implements InboundIdGenerator<StartInboundRequest>{

    public static final String INBOUND = "INBOUND-" ;
    public static final String FIRST_INBOUND_ID = "INBOUND-1";
    InboundDynamoDAOImpl inboundDbSAO;

    @Inject
    public WarehouseWiseIncrementalInboundIdGenerator(InboundDynamoDAOImpl inboundDbSAO) {
        this.inboundDbSAO = inboundDbSAO;
    }

    @Override
    public String generate(StartInboundRequest startInboundRequest) {

        String warehouseId = startInboundRequest.getWarehouseId();
        Optional<FinishedGoodsInbound> lastInbound = inboundDbSAO.getLastInbound(warehouseId);
        if(lastInbound.isPresent())
        {
            String locationId = lastInbound.get().getInboundId();
            Integer number = Integer.valueOf(locationId.split(INBOUND)[1]);
            return INBOUND + (number + 1);
        }
        return FIRST_INBOUND_ID;

    }
}
