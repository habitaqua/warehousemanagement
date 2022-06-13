package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import lombok.Builder;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;

import java.util.ArrayList;
import java.util.List;

public class WarehouseActionValidatorChain {

    List<WarehouseActionEntitiesValidator> inventoryInboundValidators;

    List<WarehouseActionEntitiesValidator> inventoryOutboundValidators;

    List<WarehouseActionEntitiesValidator> startInboundValidators;
    List<WarehouseActionEntitiesValidator> endInboundValidators;

    List<WarehouseActionEntitiesValidator> startOutboundValidators;
    List<WarehouseActionEntitiesValidator> endOutboundValidators;


    List<WarehouseActionEntitiesValidator> skuBarcodesGenerationValidators;

    @Builder
    private WarehouseActionValidatorChain(List<WarehouseActionEntitiesValidator> inventoryInboundValidators,
                                          List<WarehouseActionEntitiesValidator> inventoryOutboundValidators,
                                          List<WarehouseActionEntitiesValidator> startInboundValidators,
                                          List<WarehouseActionEntitiesValidator> endInboundValidators,
                                          List<WarehouseActionEntitiesValidator> startOutboundValidators,
                                          List<WarehouseActionEntitiesValidator> endOutboundValidators,
                                          List<WarehouseActionEntitiesValidator> skuBarcodesGenerationValidators) {

        Preconditions.checkArgument(inventoryInboundValidators != null, "inventoryInboundValidators not initialised");
        Preconditions.checkArgument(inventoryOutboundValidators != null, "inventoryOutboundValidators not initialised");
        Preconditions.checkArgument(startInboundValidators != null, "startInboundValidators not initialised");
        Preconditions.checkArgument(endInboundValidators != null, "endInboundValidators not initialised");
        Preconditions.checkArgument(startOutboundValidators != null, "startInboundValidators not initialised");
        Preconditions.checkArgument(endOutboundValidators != null, "endInboundValidators not initialised");
        Preconditions.checkArgument(skuBarcodesGenerationValidators != null, "skuBarcodesGenerationValidators not initialised");

        this.inventoryInboundValidators = inventoryInboundValidators;
        this.inventoryOutboundValidators = inventoryOutboundValidators;
        this.startInboundValidators = startInboundValidators;
        this.endInboundValidators = endInboundValidators;
        this.startOutboundValidators = startOutboundValidators;
        this.endOutboundValidators = endOutboundValidators;
        this.skuBarcodesGenerationValidators = skuBarcodesGenerationValidators;
    }

    public WarehouseValidatedEntities execute(WarehouseActionValidationRequest warehouseActionValidationRequest) {

        List<WarehouseActionEntitiesValidator> validators = new ArrayList<>();
        WarehouseAction warehouseAction = warehouseActionValidationRequest.getWarehouseAction();
        switch (warehouseAction) {
            case START_INBOUND:
                validators = startInboundValidators;
                break;
            case END_INBOUND:
                validators = endInboundValidators;
                break;
            case START_OUTBOUND:
                validators = startOutboundValidators;
                break;
            case END_OUTBOUND:
                validators = startOutboundValidators;
                break;
            case INVENTORY_INBOUND:
                validators = inventoryInboundValidators;
                break;
            case INVENTORY_OUTBOUND:
                validators = inventoryOutboundValidators;
                break;
            case SKU_BARCODE_GENERATION:
                validators = skuBarcodesGenerationValidators;
                break;
        }
        WarehouseValidatedEntities.Builder warehouseEntitiesBuilder = new WarehouseValidatedEntities.Builder();
        for (WarehouseActionEntitiesValidator validator : validators) {
            warehouseEntitiesBuilder = validator.validate(warehouseActionValidationRequest, warehouseEntitiesBuilder);
        }
        WarehouseValidatedEntities warehouseValidatedEntities = warehouseEntitiesBuilder.build();
        return warehouseValidatedEntities;
    }
}
