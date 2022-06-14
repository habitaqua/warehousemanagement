package org.habitbev.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.helpers.idgenerators.OutboundIdGenerator;
import org.habitbev.warehousemanagement.dao.OutboundDAO;
import org.habitbev.warehousemanagement.entities.outbound.EndOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseAction;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseActionValidatorChain;

import java.time.Clock;
import java.util.Optional;

public class OutboundService {

    private OutboundDAO outboundDAO;
    private OutboundIdGenerator<StartOutboundRequest> outboundIdGenerator;
    private Clock clock;
    private WarehouseActionValidatorChain warehouseActionValidatorChain;


    @Inject
    public OutboundService(@Named("dynamoDbImpl") OutboundDAO outboundDAO, @Named("warehouseWiseIncrementalOutboundIdGenerator") OutboundIdGenerator<StartOutboundRequest> outboundIdGenerator,
                           Clock clock, WarehouseActionValidatorChain warehouseActionValidatorChain) {
        this.outboundDAO = outboundDAO;
        this.outboundIdGenerator = outboundIdGenerator;
        this.clock = clock;
        this.warehouseActionValidatorChain = warehouseActionValidatorChain;
    }

    public String startOutbound(StartOutboundRequest startOutboundRequest) {
        Preconditions.checkArgument(startOutboundRequest != null, "startOutboundRequest cannot be null");
        String warehouseId = startOutboundRequest.getWarehouseId();
        String userId = startOutboundRequest.getUserId();
        String customerId = startOutboundRequest.getCustomerId();
        String companyId = startOutboundRequest.getCompanyId();
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder()
                .userId(userId).warehouseId(warehouseId).customerId(customerId).companyId(companyId).warehouseAction(WarehouseAction.START_OUTBOUND).build();
        WarehouseValidatedEntities validatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);

        synchronized (warehouseId) {
            String newOutboundId = outboundIdGenerator.generate(startOutboundRequest);
            OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(newOutboundId).startTime(clock.millis())
                    .warehouseId(warehouseId).customerId(customerId).status(new Active()).userId(userId).build();
            outboundDAO.add(outboundDTO);
            return newOutboundId;
        }
    }

    public void endOutbound(EndOutboundRequest endOutboundRequest) {
        Preconditions.checkArgument(endOutboundRequest != null, "endOutboundRequest cannot be null");
        String warehouseId = endOutboundRequest.getWarehouseId();
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder()
                .warehouseId(warehouseId).outboundId(endOutboundRequest.getOutboundId()).warehouseAction(WarehouseAction.END_OUTBOUND).build();
        WarehouseValidatedEntities validatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(endOutboundRequest.getOutboundId())
                .warehouseId(endOutboundRequest.getWarehouseId()).status(new Closed()).endTime(clock.millis()).build();
        outboundDAO.update(outboundDTO);
    }
}
