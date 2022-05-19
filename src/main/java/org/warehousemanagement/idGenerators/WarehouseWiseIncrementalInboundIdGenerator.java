package org.warehousemanagement.idGenerators;

import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.Inbound;
import org.warehousemanagement.entities.dynamodb.Location;
import org.warehousemanagement.entities.inbound.AddInboundRequest;
import org.warehousemanagement.sao.InboundDbSAO;

import java.util.Optional;

public class WarehouseWiseIncrementalInboundIdGenerator implements InboundIdGenerator<AddInboundRequest>{

    public static final String INBOUND = "INBOUND-" ;
    public static final String FIRST_INBOUND_ID = "INBOUND-1";
    InboundDbSAO inboundDbSAO;

    @Inject
    public WarehouseWiseIncrementalInboundIdGenerator(InboundDbSAO inboundDbSAO) {
        this.inboundDbSAO = inboundDbSAO;
    }

    @Override
    public String generate(AddInboundRequest addInboundRequest) {

        String warehouseId = addInboundRequest.getWarehouseId();
        Optional<Inbound> lastInbound = inboundDbSAO.getLastInbound(warehouseId);
        if(lastInbound.isPresent())
        {
            String locationId = lastInbound.get().getInboundId();
            Integer number = Integer.valueOf(locationId.split(INBOUND)[1]);
            return INBOUND + (number + 1);
        }
        return FIRST_INBOUND_ID;

    }
}
