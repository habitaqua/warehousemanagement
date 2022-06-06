package org.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.warehousemanagement.dao.InboundDAO;
import org.warehousemanagement.dao.InboundDynamoDAOImpl;
import org.warehousemanagement.entities.inbound.EndInboundRequest;
import org.warehousemanagement.entities.inbound.FGInboundDTO;
import org.warehousemanagement.entities.inbound.StartInboundRequest;
import org.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.warehousemanagement.helpers.idgenerators.InboundIdGenerator;

import java.time.Clock;

public class InboundService {

    private InboundDAO inboundDAO;
    private InboundIdGenerator<StartInboundRequest> inboundIdGenerator;
    private Clock clock;

    @Inject
    public InboundService(InboundDynamoDAOImpl inboundSAO, InboundIdGenerator inboundIdGenerator, Clock clock) {
        this.inboundDAO = inboundSAO;
        this.inboundIdGenerator = inboundIdGenerator;
        this.clock = clock;
    }

    /**
     * Starts an inbound
     * Acquires a lock at warehouse level to get incremented inboundId
     * @param startInboundRequest
     * @return
     */

    public String startInbound(StartInboundRequest startInboundRequest) {
        Preconditions.checkArgument(startInboundRequest != null, "startInboundRequest cannot be null");
        String warehouseId = startInboundRequest.getWarehouseId();
        synchronized (warehouseId) {
            String newInboundId = inboundIdGenerator.generate(startInboundRequest);
            FGInboundDTO FGInboundDTO = org.warehousemanagement.entities.inbound.FGInboundDTO.builder().inboundId(newInboundId)
                    .warehouseId(warehouseId).status(new Active()).userId(startInboundRequest.getUserId())
                    .startTime(clock.millis()).build();
            inboundDAO.add(FGInboundDTO);
            return newInboundId;
        }
    }

    public void endInbound(EndInboundRequest endInboundRequest) {

        FGInboundDTO fgInboundDTO = org.warehousemanagement.entities.inbound.FGInboundDTO.builder().inboundId(endInboundRequest.getInboundId())
                .warehouseId(endInboundRequest.getWarehouseId()).status(new Closed()).endTime(clock.millis()).build();
        inboundDAO.update(fgInboundDTO);
    }
}
