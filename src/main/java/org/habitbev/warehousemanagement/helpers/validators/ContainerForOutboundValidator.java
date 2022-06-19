package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.service.ContainerService;

public class ContainerForOutboundValidator implements WarehouseActionEntitiesValidator {

    ContainerService containerService;

    @Inject
    public ContainerForOutboundValidator(ContainerService containerService) {
        this.containerService = containerService;
    }

    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder warehouseEntityBuilder) {

        Preconditions.checkArgument(input != null, "inboundIdExistenceValidator.input cannot be null");
        Preconditions.checkArgument(warehouseEntityBuilder != null, "warehouseEntityBuilder cannot be null");

        String containerId = input.getContainerId();
        String warehouseId = input.getWarehouseId();
        Integer capacityToOutbound = input.getCapacityToOutbound();
        String skuCode = input.getSkuCode();
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");
        Preconditions.checkArgument(capacityToOutbound != null && capacityToOutbound >= 0, "capacityToOutbound cannot be < 0 or null");
        try {
            ContainerDTO containerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(containerId)
                    .warehouseId(warehouseId).build());

            int existingCapacity = containerDTO.getCurrentCapacity();
            if (capacityToOutbound > existingCapacity) {
                String message = String.format("Outbounding more than container existing capacity, " +
                        "warehouseId %s , outboundId %s, outbound size %s, containerId %s, existing %d", warehouseId, input.getOutboundId(), capacityToOutbound, containerId, existingCapacity);
                throw new WarehouseActionValidationException(message);
            }
            return warehouseEntityBuilder.containerDTO(containerDTO);
        } catch (IllegalArgumentException | ResourceNotAvailableException | InconsistentStateException e) {
            throw new WarehouseActionValidationException(e);
        }

    }
}
