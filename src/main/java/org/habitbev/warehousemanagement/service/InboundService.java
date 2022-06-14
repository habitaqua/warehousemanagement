package org.habitbev.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.InboundDAO;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.inbound.EndInboundRequest;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.StartInboundRequest;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.helpers.idgenerators.InboundIdGenerator;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseAction;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseActionValidatorChain;

import java.time.Clock;
import java.util.Optional;

public class InboundService {

    private InboundDAO inboundDAO;
    private InboundIdGenerator<StartInboundRequest> inboundIdGenerator;
    private Clock clock;

    WarehouseActionValidatorChain warehouseActionValidatorChain;

    @Inject
    public InboundService(@Named("dynamoDbImpl") InboundDAO inboundDAO,
                          @Named("warehouseWiseIncrementalInboundIdGenerator") InboundIdGenerator<StartInboundRequest> inboundIdGenerator, Clock clock,
                          WarehouseActionValidatorChain warehouseActionValidatorChain) {
        this.inboundDAO = inboundDAO;
        this.inboundIdGenerator = inboundIdGenerator;
        this.clock = clock;
        this.warehouseActionValidatorChain = warehouseActionValidatorChain;
    }


    public String startInbound(StartInboundRequest startInboundRequest) {
        Preconditions.checkArgument(startInboundRequest != null, "startInboundRequest cannot be null");
        String warehouseId = startInboundRequest.getWarehouseId();
        String userId = startInboundRequest.getUserId();
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder()
                .userId(userId).warehouseId(warehouseId).warehouseAction(WarehouseAction.START_INBOUND).build();
        WarehouseValidatedEntities validatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        synchronized (warehouseId) {
            String newInboundId = inboundIdGenerator.generate(startInboundRequest);
            FGInboundDTO FGInboundDTO = org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO.builder().inboundId(newInboundId)
                    .warehouseId(warehouseId).status(new Active()).userId(userId)
                    .startTime(clock.millis()).build();
            inboundDAO.add(FGInboundDTO);
            return newInboundId;
        }
    }

    public void endInbound(EndInboundRequest endInboundRequest) {

        Preconditions.checkArgument(endInboundRequest != null, "endInboundRequest cannot be null");
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder()
                .inboundId(endInboundRequest.getInboundId()).warehouseId(endInboundRequest.getWarehouseId()).warehouseAction(WarehouseAction.END_INBOUND).build();
        WarehouseValidatedEntities validatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        FGInboundDTO existingInbound = validatedEntities.getFgInboundDTO();

        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(existingInbound.getInboundId())
                .warehouseId(endInboundRequest.getWarehouseId()).status(new Closed()).endTime(clock.millis()).build();
        inboundDAO.update(fgInboundDTO);
    }
}
