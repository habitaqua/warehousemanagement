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

public class ContainerForInboundValidator implements WarehouseActionEntitiesValidator {

    ContainerService containerService;

    @Inject
    public ContainerForInboundValidator(ContainerService containerService) {
        this.containerService = containerService;
    }

    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder warehouseEntityBuilder) {
        Preconditions.checkArgument(input != null, "inboundIdExistenceValidator.input cannot be null");
        Preconditions.checkArgument(warehouseEntityBuilder != null, "warehouseEntityBuilder cannot be null");
        String containerId = input.getContainerId();
        String warehouseId = input.getWarehouseId();
        Integer capacityToInbound = input.getCapacityToInbound();
        String skuCode = input.getSkuCode();
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");
        Preconditions.checkArgument(capacityToInbound != null && capacityToInbound >= 0, "capacityToInbound cannot be < 0 or null");
        try {

            ContainerDTO containerDTO = containerService.getContainer(GetContainerRequest.builder().containerId(containerId)
                    .warehouseId(warehouseId).build());

            int existingCapacity = containerDTO.getCurrentCapacity();
            int maxCapacity = containerDTO.getSkuCodeWisePredefinedCapacity().get(skuCode);
            if (existingCapacity + capacityToInbound > maxCapacity) {
                String message = String.format("Inbounding more than container max capacity, " +
                        "warehouseId %s , inboundId %s, inbound size %s, containerId %s, maxCapacity %d", warehouseId, input, capacityToInbound, containerId, maxCapacity);
                throw new WarehouseActionValidationException(message);
            }
            return warehouseEntityBuilder.containerDTO(containerDTO);
        } catch (IllegalArgumentException | ResourceNotAvailableException | InconsistentStateException e) {
            throw new WarehouseActionValidationException(e);
        }

    }
}
