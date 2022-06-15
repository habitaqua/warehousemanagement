package org.habitbev.warehousemanagement.guice.providers;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.habitbev.warehousemanagement.helpers.validators.*;
import org.habitbev.warehousemanagement.service.ContainerService;

import java.util.ArrayList;
import java.util.List;

public class WarehouseActionValidatorChainProvider implements Provider<WarehouseActionValidatorChain> {

    ContainerForInboundValidator containerForInboundValidator;
    ContainerForOutboundValidator containerForOutboundValidator;
    InboundIdValidator inboundIdValidator;
    OutboundIdValidator outboundIdValidator;
    CompanyIdValidator companyIdValidator;
    CustomerIdValidator customerIdValidator;
    WarehouseIdValidator warehouseIdValidator;
    SKUCodeValidator skuCodeValidator;

    @Inject
    public WarehouseActionValidatorChainProvider(ContainerForInboundValidator containerForInboundValidator, ContainerForOutboundValidator containerForOutboundValidator,
                                                 InboundIdValidator inboundIdValidator, OutboundIdValidator outboundIdValidator, SKUCodeValidator skuCodeValidator,
                                                 CompanyIdValidator companyIdValidator, CustomerIdValidator customerIdValidator, WarehouseIdValidator warehouseIdValidator) {
        this.containerForInboundValidator = containerForInboundValidator;
        this.containerForOutboundValidator = containerForOutboundValidator;
        this.inboundIdValidator = inboundIdValidator;
        this.outboundIdValidator = outboundIdValidator;
        this.skuCodeValidator = skuCodeValidator;
        this.customerIdValidator = customerIdValidator;
        this.warehouseIdValidator = warehouseIdValidator;
        this.companyIdValidator = companyIdValidator;
    }

    @Override
    public WarehouseActionValidatorChain get() {
        List<WarehouseActionEntitiesValidator> inventoryInboundValidators = ImmutableList.of(warehouseIdValidator, skuCodeValidator,inboundIdValidator, containerForInboundValidator);

        List<WarehouseActionEntitiesValidator> inventoryOutboundValidators = ImmutableList.of(warehouseIdValidator, skuCodeValidator, outboundIdValidator, containerForOutboundValidator);
        List<WarehouseActionEntitiesValidator> skuBarcodeGenerationValidators = ImmutableList.of(warehouseIdValidator, skuCodeValidator);
        List<WarehouseActionEntitiesValidator> startInboundValidators = ImmutableList.of(warehouseIdValidator);
        List<WarehouseActionEntitiesValidator> endInboundValidators = ImmutableList.of(inboundIdValidator);
        List<WarehouseActionEntitiesValidator> startOutboundValidators = ImmutableList.of(warehouseIdValidator, customerIdValidator);
        List<WarehouseActionEntitiesValidator> endOutboundValidators = ImmutableList.of(outboundIdValidator);

        WarehouseActionValidatorChain chain = WarehouseActionValidatorChain.builder()
                .startInboundValidators(startInboundValidators)
                .endInboundValidators(endInboundValidators)
                .inventoryInboundValidators(inventoryInboundValidators)
                .inventoryOutboundValidators(inventoryOutboundValidators)
                .skuBarcodesGenerationValidators(skuBarcodeGenerationValidators)
                .startOutboundValidators(startOutboundValidators)
                .endOutboundValidators(endOutboundValidators).build();
        return chain;

    }
}
