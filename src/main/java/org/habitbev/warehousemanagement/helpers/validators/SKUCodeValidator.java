package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.service.SKUService;

public class SKUCodeValidator implements WarehouseActionEntitiesValidator {

    SKUService skuService;

    @Inject
    public SKUCodeValidator(SKUService skuService) {
        this.skuService = skuService;
    }


    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder gatheredWarehouseEntities) {
        Preconditions.checkArgument(input != null, "WarehouseActionValidationRequest cannot be null");
        String skuCode = input.getSkuCode();
        String companyId = input.getCompanyId();
        SKU sku = skuService.get(companyId, skuCode);
        return gatheredWarehouseEntities.sku(sku);
    }
}
