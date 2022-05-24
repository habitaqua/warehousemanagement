package org.warehousemanagement.idgenerators;

import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.warehousemanagement.dao.OutboundDynamoDAOImpl;

import java.util.Optional;

public class WarehouseWiseIncrementalOutboundIdGenerator implements OutboundIdGenerator<StartOutboundRequest> {

    public static final String OUTBOUND = "OUTBOUND-";
    public static final String FIRST_OUTBOUND_ID = "OUTBOUND-1";
    OutboundDynamoDAOImpl outboundDbSAO;

    @Inject
    public WarehouseWiseIncrementalOutboundIdGenerator(OutboundDynamoDAOImpl outboundDbSAO) {
        this.outboundDbSAO = outboundDbSAO;
    }

    @Override
    public String generate(StartOutboundRequest startOutboundRequest) {

        String warehouseId = startOutboundRequest.getWarehouseId();
        Optional<FinishedGoodsOutbound> lastOutbound = outboundDbSAO.getLastOutbound(warehouseId);
        if (lastOutbound.isPresent()) {
            String locationId = lastOutbound.get().getOutboundId();
            Integer number = Integer.valueOf(locationId.split(OUTBOUND)[1]);
            return OUTBOUND + (number + 1);
        }
        return FIRST_OUTBOUND_ID;

    }
}
