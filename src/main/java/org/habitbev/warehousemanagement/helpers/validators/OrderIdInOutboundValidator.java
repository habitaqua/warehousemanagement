package org.habitbev.warehousemanagement.helpers.validators;

import com.google.inject.Inject;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;

public class OrderIdInOutboundValidator implements WarehouseActionEntitiesValidator {
    @Inject
    public OrderIdInOutboundValidator() {
    }

    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder builder) {
        return builder;
    }
}
