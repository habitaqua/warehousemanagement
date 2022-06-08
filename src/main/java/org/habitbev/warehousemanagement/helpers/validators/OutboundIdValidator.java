package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.service.InboundService;
import org.habitbev.warehousemanagement.service.OutboundService;

public class OutboundIdValidator implements WarehouseActionEntitiesValidator {

    OutboundService outboundService;

    @Inject
    public OutboundIdValidator(OutboundService outboundService) {
        this.outboundService = outboundService;
    }

    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder warehouseEntityBuilder) {
        try {
            Preconditions.checkArgument(input != null, "inboundIdExistenceValidator.input cannot be null");
            String outboundId = input.getOutboundId();
            String warehouseId = input.getWarehouseId();
            Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
            OutboundDTO outboundDTO = outboundService.get(warehouseId, outboundId);
            if(new Closed().equals(outboundDTO.getStatus())) {
                String message = String.format("OutboundId %s in warehouseId %s is already closed",outboundId, warehouseId);
                throw  new WarehouseActionValidationException(message);
            }
            return warehouseEntityBuilder.outboundDTO(outboundDTO);
        } catch (IllegalArgumentException |ResourceNotAvailableException |InconsistentStateException e) {
            throw new WarehouseActionValidationException(e.getCause());
        }

    }
}
