package org.habitbev.warehousemanagement.helpers.validators;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.dao.OutboundDAO;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inventory.inventorystatus.Outbound;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
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
public class TestOutboundIdValidator {

    public static final WarehouseActionValidationRequest WAREHOUSE_ACTION_VALIDATION_REQUEST = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).build();
    public static final String OUTBOUND_1 = "outbound-1";
    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();
    public static final String USER_1 = "user-1";
    public static final String SKU_CODE = "skuCode";

    @Mock
    OutboundDAO outboundDAO;

    OutboundIdValidator outboundIdValidator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        outboundIdValidator = new OutboundIdValidator(outboundDAO);
    }


    @Test
    public void test_validate_input_null() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundIdValidator.validate(null, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("input cannot be null");
        verifyZeroInteractions(outboundDAO);
    }

    @Test
    public void test_validate_builder_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundIdValidator.validate(WAREHOUSE_ACTION_VALIDATION_REQUEST, null))
                .withMessageContaining("gatheredWarehouseEntities cannot be null");
        verifyZeroInteractions(outboundDAO);
    }

    @Test
    public void test_outboundId_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundIdValidator.validate(WAREHOUSE_ACTION_VALIDATION_REQUEST, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("outboundId cannot be blank");
        verifyZeroInteractions(outboundDAO);
    }

    @Test
    public void test_warehouseId_null() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).outboundId(OUTBOUND_1).build();
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> outboundIdValidator.validate(request, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("warehouseId cannot be blank");
        verifyZeroInteractions(outboundDAO);
    }

    @Test
    public void test_validate_success_outbound_present_active() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1).build();
        FinishedGoodsOutbound finishedGoodsOutbound = FinishedGoodsOutbound.builder().outboundId(OUTBOUND_1).outboundStatus(new Active()).warehouseId(WAREHOUSE_1).userId(USER_1).startTime(EPOCH_MILLI).endTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(outboundDAO.get(eq(request.getWarehouseId()), eq(request.getOutboundId()))).thenReturn(Optional.of(finishedGoodsOutbound));
        WarehouseValidatedEntities.Builder actualBuilderOutput = outboundIdValidator.validate(request, inputBuilder);
        WarehouseValidatedEntities.Builder expectedBuilderOutput = inputBuilder.outboundDTO(OutboundDTO.fromDbEntity(finishedGoodsOutbound));
        new ObjectAssert<>(actualBuilderOutput).usingRecursiveComparison().isEqualTo(expectedBuilderOutput);
        verify(outboundDAO).get(eq(request.getWarehouseId()), eq(request.getOutboundId()));
    }

    @Test
    public void test_validate_inbound_not_present() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(outboundDAO.get(eq(request.getWarehouseId()), eq(request.getOutboundId()))).thenReturn(Optional.empty());
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(()-> outboundIdValidator.validate(request, inputBuilder)).withCauseExactlyInstanceOf(ResourceNotAvailableException.class);
        verify(outboundDAO).get(eq(request.getWarehouseId()), eq(request.getOutboundId()));
        verifyNoMoreInteractions(outboundDAO);
    }

    @Test
    public void test_validate_inbound_present_closed() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1).build();
        FinishedGoodsOutbound finishedGoodsOutbound = FinishedGoodsOutbound.builder().outboundId(OUTBOUND_1).outboundStatus(new Closed()).warehouseId(WAREHOUSE_1).userId(USER_1).startTime(EPOCH_MILLI).endTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(outboundDAO.get(eq(request.getWarehouseId()), eq(request.getOutboundId()))).thenReturn(Optional.of(finishedGoodsOutbound));
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(()-> outboundIdValidator.validate(request, inputBuilder));
        verify(outboundDAO).get(eq(request.getWarehouseId()), eq(request.getOutboundId()));
        verifyNoMoreInteractions(outboundDAO);
    }

    @Test
    public void test_validate_outbound_inconsistent_state_exception() {
        WarehouseActionValidationRequest request = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1).build();
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        when(outboundDAO.get(eq(request.getWarehouseId()), eq(request.getOutboundId()))).thenThrow(new InconsistentStateException("state exception"));
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(()-> outboundIdValidator.validate(request, inputBuilder)).withCauseExactlyInstanceOf(InconsistentStateException.class);
        verify(outboundDAO).get(eq(request.getWarehouseId()), eq(request.getOutboundId()));
    }
}
