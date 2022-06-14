package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.customer.CustomerDTO;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;
import org.habitbev.warehousemanagement.service.CustomerService;
import org.habitbev.warehousemanagement.service.WarehouseService;

import java.util.Optional;

public class WarehouseIdValidator implements WarehouseActionEntitiesValidator {

    WarehouseService warehouseService;

    @Inject
    public WarehouseIdValidator(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }


    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder gatheredWarehouseEntities) {
        Preconditions.checkArgument(input != null, "input cannot be null");
        Preconditions.checkArgument(gatheredWarehouseEntities != null, "gatherredWarehouseEntities cannot be null");

        String companyId = input.getCompanyId();
        String warehouseId = input.getWarehouseId();
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");

        Optional<WarehouseDTO> warehouseDTO = warehouseService.getWarehouse(warehouseId);
        if (!warehouseDTO.isPresent()) {
            String message = String.format("warehouseId %s and companyId %s mapping does not exist", warehouseId, companyId);
            throw new ResourceNotAvailableException(message);
        }
        return gatheredWarehouseEntities.warehouseDTO(warehouseDTO.get());
    }
}
