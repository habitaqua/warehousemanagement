package org.warehousemanagement.helpers.idgenerators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.warehousemanagement.dao.OutboundDAO;
import org.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.warehousemanagement.dao.OutboundDynamoDAOImpl;

import java.util.Optional;

public class WarehouseWiseIncrementalOutboundIdGenerator implements OutboundIdGenerator<StartOutboundRequest> {

    public static final String OUTBOUND = "OUTBOUND-";
    public static final String FIRST_OUTBOUND_ID = "OUTBOUND-1";
    OutboundDAO outboundDAO;

    @Inject
    public WarehouseWiseIncrementalOutboundIdGenerator(OutboundDAO outboundDAO) {
        this.outboundDAO = outboundDAO;
    }

    @Override
    public String generate(StartOutboundRequest startOutboundRequest) {

        Preconditions.checkArgument(startOutboundRequest != null,
                "warehouseWiseIncrementalOutboundIdGenerator.startOutboundRequest cannot be null");
        String warehouseId = startOutboundRequest.getWarehouseId();
        Optional<FinishedGoodsOutbound> lastOutbound = outboundDAO.getLastOutbound(warehouseId);
        if (lastOutbound.isPresent()) {
            String locationId = lastOutbound.get().getOutboundId();
            Integer number = Integer.valueOf(locationId.split(OUTBOUND)[1]);
            return OUTBOUND + (number + 1);
        }
        return FIRST_OUTBOUND_ID;

    }
}
