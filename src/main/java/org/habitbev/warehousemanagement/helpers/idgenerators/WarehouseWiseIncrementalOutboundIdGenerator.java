package org.habitbev.warehousemanagement.helpers.idgenerators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.OutboundDAO;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.outbound.StartOutboundRequest;

import java.util.Optional;

public class WarehouseWiseIncrementalOutboundIdGenerator implements OutboundIdGenerator<StartOutboundRequest> {

    public static final String OUTBOUND = "OUTBOUND-";
    public static final String FIRST_OUTBOUND_ID = "OUTBOUND-1";
    OutboundDAO outboundDAO;

    @Inject
    public WarehouseWiseIncrementalOutboundIdGenerator(@Named("dynamoDbImpl") OutboundDAO outboundDAO) {
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
