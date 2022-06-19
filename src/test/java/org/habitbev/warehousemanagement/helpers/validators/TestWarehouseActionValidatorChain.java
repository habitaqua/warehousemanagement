package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestWarehouseActionValidatorChain {

    public static final String INBOUND_1 = "inbound-1";
    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final String OUTBOUND_1 = "outbound-1";
    public static final String COMPANY_1 = "company-1";
    @Mock
    WarehouseActionValidatorChain warehouseActionValidatorChain;
    @Mock
    InboundIdValidator inboundIdValidator;
    @Mock
    WarehouseIdValidator warehouseIdValidator;

    @Captor
    ArgumentCaptor<WarehouseValidatedEntities.Builder> builderCaptor;
    @Captor
    ArgumentCaptor<WarehouseActionValidationRequest> validationRequestCaptor;

    FGInboundDTO fgInboundDTO;

    WarehouseDTO warehouseDTO;
    List<WarehouseActionEntitiesValidator> inventoryInboundValidators;
    List<WarehouseActionEntitiesValidator> inventoryOutboundValidators;
    List<WarehouseActionEntitiesValidator> startInboundValidators;
    List<WarehouseActionEntitiesValidator> endInboundValidators;
    List<WarehouseActionEntitiesValidator> startOutboundValidators;
    List<WarehouseActionEntitiesValidator> endOutboundValidators;
    List<WarehouseActionEntitiesValidator> skuBarcodesGenerationValidators;


    @Before
    public void setUp() throws Exception {
        List<WarehouseActionEntitiesValidator> warehouseActionEntitiesValidators = ImmutableList.of(inboundIdValidator, warehouseIdValidator);
        inventoryInboundValidators = warehouseActionEntitiesValidators;
        inventoryOutboundValidators = warehouseActionEntitiesValidators;
        startInboundValidators = warehouseActionEntitiesValidators;
        endInboundValidators = warehouseActionEntitiesValidators;
        startOutboundValidators = warehouseActionEntitiesValidators;
        endOutboundValidators = warehouseActionEntitiesValidators;
        skuBarcodesGenerationValidators = warehouseActionEntitiesValidators;

        warehouseActionValidatorChain = WarehouseActionValidatorChain.builder().inventoryInboundValidators(inventoryInboundValidators)
                .inventoryOutboundValidators(inventoryOutboundValidators).startInboundValidators(startInboundValidators).endInboundValidators(endInboundValidators)
                .startOutboundValidators(startOutboundValidators).endOutboundValidators(endOutboundValidators).skuBarcodesGenerationValidators(skuBarcodesGenerationValidators)
                .build();
        fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).warehouseId(WAREHOUSE_1).build();
        warehouseDTO = WarehouseDTO.builder().id(WAREHOUSE_1).companyId(COMPANY_1).build();


        when(inboundIdValidator.validate(any(WarehouseActionValidationRequest.class), any(WarehouseValidatedEntities.Builder.class))).thenReturn(new WarehouseValidatedEntities.Builder().fgInboundDTO(fgInboundDTO));
        when(warehouseIdValidator.validate(any(WarehouseActionValidationRequest.class), any(WarehouseValidatedEntities.Builder.class))).thenReturn(new WarehouseValidatedEntities.Builder().fgInboundDTO(fgInboundDTO).warehouseDTO(warehouseDTO));
    }

    @Test
    public void test_inventoryInboundValidators_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WarehouseActionValidatorChain.builder()
                .inventoryOutboundValidators(inventoryOutboundValidators).startInboundValidators(startInboundValidators).endInboundValidators(endInboundValidators)
                .startOutboundValidators(startOutboundValidators).endOutboundValidators(endOutboundValidators).skuBarcodesGenerationValidators(skuBarcodesGenerationValidators)
                .build()).withMessageContaining("inventoryInboundValidators not initialised");
        verifyZeroInteractions(inboundIdValidator, warehouseIdValidator);
    }

    @Test
    public void test_inventoryOutboundValidators_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WarehouseActionValidatorChain.builder()
                .inventoryInboundValidators(inventoryInboundValidators).startInboundValidators(startInboundValidators).endInboundValidators(endInboundValidators)
                .startOutboundValidators(startOutboundValidators).endOutboundValidators(endOutboundValidators).skuBarcodesGenerationValidators(skuBarcodesGenerationValidators)
                .build()).withMessageContaining("inventoryOutboundValidators not initialised");
        verifyZeroInteractions(inboundIdValidator, warehouseIdValidator);

    }

    @Test
    public void test_startInboundValidators_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WarehouseActionValidatorChain.builder()
                .inventoryInboundValidators(inventoryInboundValidators).inventoryOutboundValidators(inventoryOutboundValidators).endInboundValidators(endInboundValidators)
                .startOutboundValidators(startOutboundValidators).endOutboundValidators(endOutboundValidators).skuBarcodesGenerationValidators(skuBarcodesGenerationValidators)
                .build()).withMessageContaining("startInboundValidators not initialised");
        verifyZeroInteractions(inboundIdValidator, warehouseIdValidator);

    }

    @Test
    public void test_endInboundValidators_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WarehouseActionValidatorChain.builder()
                .inventoryInboundValidators(inventoryInboundValidators).inventoryOutboundValidators(inventoryOutboundValidators).startInboundValidators(startInboundValidators)
                .startOutboundValidators(startOutboundValidators).endOutboundValidators(endOutboundValidators).skuBarcodesGenerationValidators(skuBarcodesGenerationValidators)
                .build()).withMessageContaining("endInboundValidators not initialised");
        verifyZeroInteractions(inboundIdValidator, warehouseIdValidator);

    }

    @Test
    public void test_startOutboundValidators_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WarehouseActionValidatorChain.builder()
                .inventoryInboundValidators(inventoryInboundValidators).inventoryOutboundValidators(inventoryOutboundValidators).startInboundValidators(startInboundValidators)
                .endInboundValidators(endInboundValidators).endOutboundValidators(endOutboundValidators).skuBarcodesGenerationValidators(skuBarcodesGenerationValidators)
                .build()).withMessageContaining("startOutboundValidators not initialised");
        verifyZeroInteractions(inboundIdValidator, warehouseIdValidator);

    }

    @Test
    public void test_endOutboundValidators_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WarehouseActionValidatorChain.builder()
                .inventoryInboundValidators(inventoryInboundValidators).inventoryOutboundValidators(inventoryOutboundValidators).startInboundValidators(startInboundValidators)
                .endInboundValidators(endInboundValidators).startOutboundValidators(startOutboundValidators).skuBarcodesGenerationValidators(skuBarcodesGenerationValidators)
                .build()).withMessageContaining("endOutboundValidators not initialised");
        verifyZeroInteractions(inboundIdValidator, warehouseIdValidator);

    }

    @Test
    public void test_skuBarcodesGenerationValidators_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WarehouseActionValidatorChain.builder()
                .inventoryInboundValidators(inventoryInboundValidators).inventoryOutboundValidators(inventoryOutboundValidators).startInboundValidators(startInboundValidators)
                .endInboundValidators(endInboundValidators).startOutboundValidators(startOutboundValidators).endOutboundValidators(endOutboundValidators)
                .build()).withMessageContaining("skuBarcodesGenerationValidators not initialised");
        verifyZeroInteractions(inboundIdValidator, warehouseIdValidator);

    }

    @Test
    public void test_start_inbound_action() {
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).build();
        WarehouseValidatedEntities expectedWarehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        verifyOutputs(warehouseActionValidationRequest, expectedWarehouseValidatedEntities);
    }


    @Test
    public void test_end_inbound_action() {
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.END_INBOUND).build();
        WarehouseValidatedEntities expectedWarehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        verifyOutputs(warehouseActionValidationRequest, expectedWarehouseValidatedEntities);

    }

    @Test
    public void test_start_outbound_action() {
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_OUTBOUND).build();
        WarehouseValidatedEntities expectedWarehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        verifyOutputs(warehouseActionValidationRequest, expectedWarehouseValidatedEntities);

    }

    @Test
    public void test_end_outbound_action() {
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.END_OUTBOUND).build();
        WarehouseValidatedEntities expectedWarehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        verifyOutputs(warehouseActionValidationRequest, expectedWarehouseValidatedEntities);

    }

    @Test
    public void test_inventory_inbound_action() {
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.INVENTORY_INBOUND).build();
        WarehouseValidatedEntities expectedWarehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        verifyOutputs(warehouseActionValidationRequest, expectedWarehouseValidatedEntities);

    }

    @Test
    public void test_inventory_outbound_action() {
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.INVENTORY_OUTBOUND).build();
        WarehouseValidatedEntities expectedWarehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        verifyOutputs(warehouseActionValidationRequest, expectedWarehouseValidatedEntities);

    }

    @Test
    public void test_sku_barcode_generation_action() {
        WarehouseActionValidationRequest warehouseActionValidationRequest = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.SKU_BARCODE_GENERATION).build();
        WarehouseValidatedEntities expectedWarehouseValidatedEntities = warehouseActionValidatorChain.execute(warehouseActionValidationRequest);
        verifyOutputs(warehouseActionValidationRequest, expectedWarehouseValidatedEntities);

    }

    private void verifyOutputs(WarehouseActionValidationRequest warehouseActionValidationRequest, WarehouseValidatedEntities expectedWarehouseValidatedEntities) {
        InOrder inOrder = inOrder(inboundIdValidator, warehouseIdValidator);
        inOrder.verify(inboundIdValidator).validate(validationRequestCaptor.capture(), builderCaptor.capture());
        inOrder.verify(warehouseIdValidator).validate(validationRequestCaptor.capture(), builderCaptor.capture());
        List<WarehouseActionValidationRequest> validationRequestValues = validationRequestCaptor.getAllValues();
        List<WarehouseValidatedEntities.Builder> builderValues = builderCaptor.getAllValues();
        WarehouseActionValidationRequest inboundIdValidatorRequest = validationRequestValues.get(0);
        WarehouseActionValidationRequest warehouseValidationRequest = validationRequestValues.get(1);

        new ObjectAssert<>(inboundIdValidatorRequest).usingRecursiveComparison().isEqualTo(warehouseActionValidationRequest);
        new ObjectAssert<>(warehouseValidationRequest).usingRecursiveComparison().isEqualTo(warehouseActionValidationRequest);

        WarehouseValidatedEntities.Builder inboundValidatorBuilderRequest = builderValues.get(0);
        WarehouseValidatedEntities.Builder warehouseValidatorBuilderRequest = builderValues.get(1);

        new ObjectAssert<>(inboundValidatorBuilderRequest).usingRecursiveComparison().isEqualTo(new WarehouseValidatedEntities.Builder());
        new ObjectAssert<>(warehouseValidatorBuilderRequest).usingRecursiveComparison().isEqualTo(new WarehouseValidatedEntities.Builder().fgInboundDTO(fgInboundDTO));

        new ObjectAssert<>(expectedWarehouseValidatedEntities).usingRecursiveComparison().isEqualTo(new WarehouseValidatedEntities.Builder().fgInboundDTO(fgInboundDTO).warehouseDTO(warehouseDTO));
    }
}
