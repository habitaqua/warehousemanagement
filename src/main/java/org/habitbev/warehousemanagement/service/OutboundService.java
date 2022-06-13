package org.habitbev.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.helpers.idgenerators.OutboundIdGenerator;
import org.habitbev.warehousemanagement.dao.OutboundDAO;
import org.habitbev.warehousemanagement.entities.outbound.EndOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;

import java.time.Clock;
import java.util.Optional;

public class OutboundService {

    private OutboundDAO outboundDAO;
    private OutboundIdGenerator<StartOutboundRequest> outboundIdGenerator;
    private Clock clock;

    @Inject
    public OutboundService(@Named("dynamoDbImpl") OutboundDAO outboundDAO, @Named("warehouseWiseIncrementalOutboundIdGenerator") OutboundIdGenerator<StartOutboundRequest> outboundIdGenerator, Clock clock) {
        this.outboundDAO = outboundDAO;
        this.outboundIdGenerator = outboundIdGenerator;
        this.clock = clock;
    }

    public OutboundDTO get(String warehouseId, String outboundId) {
        Optional<FinishedGoodsOutbound> finishedGoodsOutboundOp = outboundDAO.get(warehouseId, outboundId);
        if (!finishedGoodsOutboundOp.isPresent()) {
            String message = String.format("OutboundId %s in warehouseid %s does not exist", outboundId, warehouseId);
            throw new ResourceNotAvailableException(message);
        }
        return OutboundDTO.fromDbEntity(finishedGoodsOutboundOp.get());
    }

    public String startOutbound(StartOutboundRequest startOutboundRequest) {
        Preconditions.checkArgument(startOutboundRequest != null, "startOutboundRequest cannot be null");
        String warehouseId = startOutboundRequest.getWarehouseId();
        synchronized (warehouseId) {
            String newOutboundId = outboundIdGenerator.generate(startOutboundRequest);
            OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(newOutboundId).startTime(clock.millis())
                    .warehouseId(warehouseId).customerId(startOutboundRequest.getCustomerId()).status(new Active()).userId(startOutboundRequest.getUserId()).build();
            outboundDAO.add(outboundDTO);
            return newOutboundId;
        }
    }

    public void endOutbound(EndOutboundRequest endOutboundRequest) {
        Preconditions.checkArgument(endOutboundRequest != null, "endOutboundRequest cannot be null");
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(endOutboundRequest.getOutboundId())
                .warehouseId(endOutboundRequest.getWarehouseId()).status(new Closed()).endTime(clock.millis()).build();
        outboundDAO.update(outboundDTO);
    }
}
