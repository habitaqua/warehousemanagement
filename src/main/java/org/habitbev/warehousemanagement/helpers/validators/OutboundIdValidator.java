package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.dao.OutboundDAO;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.service.InboundService;
import org.habitbev.warehousemanagement.service.OutboundService;

import java.util.Optional;

public class OutboundIdValidator implements WarehouseActionEntitiesValidator {

    OutboundDAO outboundDAO;

    @Inject
    public OutboundIdValidator(OutboundDAO outboundDAO) {
        this.outboundDAO = outboundDAO;
    }

    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder warehouseEntityBuilder) {
        try {
            Preconditions.checkArgument(input != null, "inboundIdExistenceValidator.input cannot be null");
            String outboundId = input.getOutboundId();
            String warehouseId = input.getWarehouseId();
            Preconditions.checkArgument(StringUtils.isNotBlank(outboundId), "outboundId cannot be blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
            Optional<FinishedGoodsOutbound> finishedGoodsOutboundOp = outboundDAO.get(warehouseId, outboundId);
            if (!finishedGoodsOutboundOp.isPresent()) {
                String message = String.format("OutboundId %s in warehouseid %s does not exist", outboundId, warehouseId);
                throw new ResourceNotAvailableException(message);
            }
            OutboundDTO outboundDTO = OutboundDTO.fromDbEntity(finishedGoodsOutboundOp.get());
            if (new Closed().equals(outboundDTO.getStatus())) {
                String message = String.format("OutboundId %s in warehouseId %s is already closed", outboundId, warehouseId);
                throw new WarehouseActionValidationException(message);
            }
            return warehouseEntityBuilder.outboundDTO(outboundDTO);
        } catch (IllegalArgumentException | ResourceNotAvailableException | InconsistentStateException e) {
            throw new WarehouseActionValidationException(e.getCause());
        }

    }
}
