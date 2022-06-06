package org.habitbev.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.habitbev.warehousemanagement.dao.InboundDAO;
import org.habitbev.warehousemanagement.entities.inbound.StartInboundRequest;
import org.habitbev.warehousemanagement.dao.InboundDynamoDAOImpl;
import org.habitbev.warehousemanagement.entities.inbound.EndInboundRequest;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.habitbev.warehousemanagement.helpers.idgenerators.InboundIdGenerator;

import java.time.Clock;

public class InboundService {

    private InboundDAO inboundDAO;
    private InboundIdGenerator<StartInboundRequest> inboundIdGenerator;
    private Clock clock;

    @Inject
    public InboundService(InboundDAO inboundSAO, InboundIdGenerator inboundIdGenerator, Clock clock) {
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
            FGInboundDTO FGInboundDTO = org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO.builder().inboundId(newInboundId)
                    .warehouseId(warehouseId).status(new Active()).userId(startInboundRequest.getUserId())
                    .startTime(clock.millis()).build();
            inboundDAO.add(FGInboundDTO);
            return newInboundId;
        }
    }

    public void endInbound(EndInboundRequest endInboundRequest) {

        Preconditions.checkArgument(endInboundRequest != null, "endInboundRequest cannot be null");

        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(endInboundRequest.getInboundId())
                .warehouseId(endInboundRequest.getWarehouseId()).status(new Closed()).endTime(clock.millis()).build();
        inboundDAO.update(fgInboundDTO);
    }
}
