package org.habitbev.warehousemanagement.helpers.validators;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.dao.InboundDAO;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.sku.SKUDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestInboundIdValidator {

    public static final WarehouseActionValidationRequest WAREHOUSE_ACTION_VALIDATION_REQUEST = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).build();
    public static final String INBOUND_1 = "inbound-1";
    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();
    public static final String USER_1 = "user-1";
    public static final String SKU_CODE = "skuCode";

    @Mock
    InboundDAO inboundDAO;

    InboundIdValidator inboundIdValidator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        inboundIdValidator = new InboundIdValidator(inboundDAO);
    }


    @Test
    public void test_validate_input_null() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundIdValidator.validate(null, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("input cannot be null");
        verifyZeroInteractions(inboundDAO);
    }

    @Test
    public void test_validate_builder_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundIdValidator.validate(WAREHOUSE_ACTION_VALIDATION_REQUEST, null))
                .withMessageContaining("gatheredWarehouseEntities cannot be null");
        verifyZeroInteractions(inboundDAO);
    }

    @Test
    public void test_inboundId_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundIdValidator.validate(WAREHOUSE_ACTION_VALIDATION_REQUEST, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("inboundId cannot be blank");
        verifyZeroInteractions(inboundDAO);
    }

    @Test
    public void test_warehouseId_null() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).inboundId(INBOUND_1).build();
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> inboundIdValidator.validate(request, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("warehouseId cannot be blank");
        verifyZeroInteractions(inboundDAO);
    }

    @Test
    public void test_validate_success_inbound_present_active() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).inboundId(INBOUND_1).warehouseId(WAREHOUSE_1).build();
        FinishedGoodsInbound finishedGoodsInbound = FinishedGoodsInbound.builder().inboundId(INBOUND_1).inboundStatus(new Active()).warehouseId(WAREHOUSE_1).userId(USER_1).startTime(EPOCH_MILLI).endTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(inboundDAO.get(eq(request.getWarehouseId()), eq(request.getInboundId()))).thenReturn(Optional.of(finishedGoodsInbound));
        WarehouseValidatedEntities.Builder actualBuilderOutput = inboundIdValidator.validate(request, inputBuilder);
        WarehouseValidatedEntities.Builder expectedBuilderOutput = inputBuilder.fgInboundDTO(FGInboundDTO.fromDbEntity(finishedGoodsInbound));
        new ObjectAssert<>(actualBuilderOutput).usingRecursiveComparison().isEqualTo(expectedBuilderOutput);
        verify(inboundDAO).get(eq(request.getWarehouseId()), eq(request.getInboundId()));
    }

    @Test
    public void test_validate_inbound_not_present() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).inboundId(INBOUND_1).warehouseId(WAREHOUSE_1).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(inboundDAO.get(eq(request.getWarehouseId()), eq(request.getInboundId()))).thenReturn(Optional.empty());
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(()->inboundIdValidator.validate(request, inputBuilder)).withCauseExactlyInstanceOf(ResourceNotAvailableException.class);
        verify(inboundDAO).get(eq(request.getWarehouseId()), eq(request.getInboundId()));
        verifyNoMoreInteractions(inboundDAO);
    }

    @Test
    public void test_validate_inbound_present_closed() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).inboundId(INBOUND_1).warehouseId(WAREHOUSE_1).build();
        FinishedGoodsInbound finishedGoodsInbound = FinishedGoodsInbound.builder().inboundId(INBOUND_1).inboundStatus(new Closed()).warehouseId(WAREHOUSE_1).userId(USER_1).startTime(EPOCH_MILLI).endTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(inboundDAO.get(eq(request.getWarehouseId()), eq(request.getInboundId()))).thenReturn(Optional.of(finishedGoodsInbound));
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(()->inboundIdValidator.validate(request, inputBuilder));
        verify(inboundDAO).get(eq(request.getWarehouseId()), eq(request.getInboundId()));
        verifyNoMoreInteractions(inboundDAO);

    }


    @Test
    public void test_validate_inbound_inconsistent_state_exception() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).inboundId(INBOUND_1).warehouseId(WAREHOUSE_1).build();
        FinishedGoodsInbound finishedGoodsInbound = FinishedGoodsInbound.builder().inboundId(INBOUND_1).inboundStatus(new Active()).warehouseId(WAREHOUSE_1).userId(USER_1).startTime(EPOCH_MILLI).endTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(inboundDAO.get(eq(request.getWarehouseId()), eq(request.getInboundId()))).thenThrow(new InconsistentStateException("state exception"));
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(()->inboundIdValidator.validate(request, inputBuilder)).withCauseExactlyInstanceOf(InconsistentStateException.class);
        verify(inboundDAO).get(eq(request.getWarehouseId()), eq(request.getInboundId()));
    }
}
