package org.habitbev.warehousemanagement.helpers.validators;

import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;

public interface WarehouseActionEntitiesValidator {

    WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder gatheredWarehouseEntities);
}
