package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.dao.InboundDAO;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.service.InboundService;

import java.util.Optional;

public class InboundIdValidator implements WarehouseActionEntitiesValidator {

    InboundDAO inboundDAO;

    @Inject
    public InboundIdValidator(@Named("dynamoDbImpl") InboundDAO inboundDAO) {
        this.inboundDAO = inboundDAO;
    }

    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder warehouseEntityBuilder) {
        try {
            Preconditions.checkArgument(input != null, "inboundIdExistenceValidator.input cannot be null");
            String inboundId = input.getInboundId();
            String warehouseId = input.getWarehouseId();
            Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
            Optional<FinishedGoodsInbound> finishedGoodsInboundOp = inboundDAO.get(warehouseId, inboundId);
            if (!finishedGoodsInboundOp.isPresent()) {
                String message = String.format("InboundId %s in warehouseid %s does not exist", inboundId, warehouseId);
                throw new ResourceNotAvailableException(message);
            }
            FinishedGoodsInbound finishedGoodsInbound = finishedGoodsInboundOp.get();
            if(new Closed().equals(finishedGoodsInbound.getInboundStatus())) {
                String message = String.format("InboundId %s in warehouseId %s is already closed",inboundId, warehouseId);
                throw  new WarehouseActionValidationException(message);
            }
            return warehouseEntityBuilder.fgInboundDTO(FGInboundDTO.fromDbEntity(finishedGoodsInbound));
        } catch (IllegalArgumentException |ResourceNotAvailableException |InconsistentStateException e) {
            throw new WarehouseActionValidationException(e);
        }

    }
}
