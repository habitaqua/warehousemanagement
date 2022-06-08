package org.habitbev.warehousemanagement.helpers.idgenerators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.InboundDAO;
import org.habitbev.warehousemanagement.entities.inbound.StartInboundRequest;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;

import java.util.Optional;

public class WarehouseWiseIncrementalInboundIdGenerator implements InboundIdGenerator<StartInboundRequest> {

    public static final String INBOUND = "INBOUND-";
    public static final String FIRST_INBOUND_ID = "INBOUND-1";
    InboundDAO inboundDAO;

    @Inject
    public WarehouseWiseIncrementalInboundIdGenerator(@Named("dynamoDbImpl") InboundDAO inboundDAO) {
        this.inboundDAO = inboundDAO;
    }

    @Override
    public String generate(StartInboundRequest startInboundRequest) {

        Preconditions.checkArgument(startInboundRequest != null, "startInboudrequest cannot be null");
        String warehouseId = startInboundRequest.getWarehouseId();
        Optional<FinishedGoodsInbound> lastInbound = inboundDAO.getLastInbound(warehouseId);
        if (lastInbound.isPresent()) {
            String locationId = lastInbound.get().getInboundId();
            Integer number = Integer.valueOf(locationId.split(INBOUND)[1]);
            return INBOUND + (number + 1);
        }
        return FIRST_INBOUND_ID;

    }
}
