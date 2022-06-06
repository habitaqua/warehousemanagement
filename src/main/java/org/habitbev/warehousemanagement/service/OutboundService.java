package org.habitbev.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.habitbev.warehousemanagement.helpers.idgenerators.OutboundIdGenerator;
import org.habitbev.warehousemanagement.dao.OutboundDAO;
import org.habitbev.warehousemanagement.entities.outbound.EndOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;

import java.time.Clock;

public class OutboundService {

    private OutboundDAO outboundDAO;
    private OutboundIdGenerator<StartOutboundRequest> outboundIdGenerator;
    private Clock clock;

    @Inject
    public OutboundService(OutboundDAO outboundDAO, OutboundIdGenerator outboundIdGenerator, Clock clock) {
        this.outboundDAO = outboundDAO;
        this.outboundIdGenerator = outboundIdGenerator;
        this.clock = clock;
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
