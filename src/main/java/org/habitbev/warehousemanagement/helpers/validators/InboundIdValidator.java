package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.service.InboundService;

public class InboundIdValidator implements WarehouseActionEntitiesValidator {

    InboundService inboundService;

    @Inject
    public InboundIdValidator(InboundService inboundService) {
        this.inboundService = inboundService;
    }

    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder warehouseEntityBuilder) {
        try {
            Preconditions.checkArgument(input != null, "inboundIdExistenceValidator.input cannot be null");
            String inboundId = input.getInboundId();
            String warehouseId = input.getWarehouseId();
            Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
            FGInboundDTO fgInboundDTO = inboundService.getInbound(warehouseId, inboundId);
            if(new Closed().equals(fgInboundDTO.getStatus())) {
                String message = String.format("InboundId %s in warehouseId %s is already closed",inboundId, warehouseId);
                throw  new WarehouseActionValidationException(message);
            }
            return warehouseEntityBuilder.fgInboundDTO(fgInboundDTO);
        } catch (IllegalArgumentException |ResourceNotAvailableException |InconsistentStateException e) {
            throw new WarehouseActionValidationException(e.getCause());
        }

    }
}
