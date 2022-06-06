package org.warehousemanagement.service;

import com.google.inject.Inject;
import org.warehousemanagement.dao.OutboundDAO;
import org.warehousemanagement.dao.OutboundDynamoDAOImpl;
import org.warehousemanagement.entities.outbound.EndOutboundRequest;
import org.warehousemanagement.entities.outbound.OutboundDTO;
import org.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.warehousemanagement.helpers.idgenerators.OutboundIdGenerator;

import java.time.Clock;

public class OutboundService {

    private OutboundDAO outboundDAO;
    private OutboundIdGenerator<StartOutboundRequest> outboundIdGenerator;
    private Clock clock;

    @Inject
    public OutboundService(OutboundDynamoDAOImpl outboundDAO, OutboundIdGenerator outboundIdGenerator, Clock clock) {
        this.outboundDAO = outboundDAO;
        this.outboundIdGenerator = outboundIdGenerator;
        this.clock = clock;
    }

    public String startNewOutbound(StartOutboundRequest startOutboundRequest) {
        String warehouseId = startOutboundRequest.getWarehouseId();
        synchronized (warehouseId) {
            String newOutboundId = outboundIdGenerator.generate(startOutboundRequest);
            OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(newOutboundId)
                    .warehouseId(warehouseId).status(new Active()).userId(startOutboundRequest.getUserId()).build();
            outboundDAO.add(outboundDTO);
            return newOutboundId;
        }
    }

    public void endOutbound(EndOutboundRequest endOutboundRequest) {

        OutboundDTO outboundDTO = org.warehousemanagement.entities.outbound.OutboundDTO.builder().outboundId(endOutboundRequest.getOutboundId())
                .warehouseId(endOutboundRequest.getWarehouseId()).status(new Closed()).endTime(clock.millis()).build();
        outboundDAO.update(outboundDTO);
    }
}
