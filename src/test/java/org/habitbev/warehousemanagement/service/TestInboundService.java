package org.habitbev.warehousemanagement.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.dao.InboundDAO;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.inbound.EndInboundRequest;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.StartInboundRequest;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.helpers.idgenerators.InboundIdGenerator;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseAction;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseActionValidatorChain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class TestInboundService {

    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();
    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final String USER_1 = "user-1";
    public static final String INBOUND_1 = "INBOUND_1";
    InboundService inboundService;
    @Mock
    InboundDAO inboundDAO;
    @Mock
    InboundIdGenerator<StartInboundRequest> inboundIdGenerator;
    @Mock
    Clock clock;

    @Mock
    WarehouseActionValidatorChain warehouseActionValidatorChain;
    @Captor
    ArgumentCaptor<FGInboundDTO> fgInboundDTOArgumentCaptor;

    @Captor
    ArgumentCaptor<WarehouseActionValidationRequest> warehouseActionValidationRequestArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        inboundService = new InboundService(inboundDAO, inboundIdGenerator, clock, warehouseActionValidatorChain);
        Mockito.when(clock.millis()).thenReturn(EPOCH_MILLI);
    }


    @Test
    public void test_start_intbound_input_null() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> inboundService.startInbound(null)).withMessageContaining("startInboundRequest cannot be null");
        Mockito.verifyZeroInteractions(inboundDAO, inboundIdGenerator, clock);
    }

    @Test
    public void test_startInbound_success() {

        StartInboundRequest startInboundRequest = StartInboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).build();
        Mockito.when(inboundIdGenerator.generate(eq(startInboundRequest))).thenReturn(INBOUND_1);
        String actualInboundId = inboundService.startInbound(startInboundRequest);
        new StringAssert(actualInboundId).isEqualTo(INBOUND_1);
        Mockito.verify(inboundDAO).add(fgInboundDTOArgumentCaptor.capture());
        FGInboundDTO actualInboundDTO = fgInboundDTOArgumentCaptor.getValue();
        FGInboundDTO expectedInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).startTime(EPOCH_MILLI).status(new Active()).userId(USER_1).warehouseId(WAREHOUSE_1).build();
        new ObjectAssert<>(expectedInboundDTO).usingRecursiveComparison().isEqualTo(actualInboundDTO);
        Mockito.verify(inboundIdGenerator).generate(eq(startInboundRequest));
        Mockito.verify(clock).millis();
        Mockito.verifyNoMoreInteractions(inboundDAO, inboundIdGenerator, clock);
    }


    @Test
    public void test_startInbound_inbound_id_generator_throws_exception() {
        StartInboundRequest startInboundRequest = StartInboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).build();
        Mockito.when(inboundIdGenerator.generate(eq(startInboundRequest))).thenThrow(new RuntimeException());
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> inboundService.startInbound(startInboundRequest));
        Mockito.verify(inboundIdGenerator).generate(eq(startInboundRequest));
        Mockito.verifyZeroInteractions(clock, inboundDAO);
        Mockito.verifyNoMoreInteractions(inboundIdGenerator);
    }

   @Test
    public void test_startOutbound_outbound_dao_throws_exception() {
       StartInboundRequest startInboundRequest = StartInboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).build();
        Mockito.when(inboundIdGenerator.generate(eq(startInboundRequest))).thenReturn(INBOUND_1);
        Mockito.doThrow(new IllegalArgumentException("illegal argument exception")).when(inboundDAO).add(fgInboundDTOArgumentCaptor.capture());
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> inboundService.startInbound(startInboundRequest));
        Mockito.verify(inboundDAO).add(fgInboundDTOArgumentCaptor.capture());
       FGInboundDTO actualInboundDTO = fgInboundDTOArgumentCaptor.getValue();
       FGInboundDTO expectedInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).startTime(EPOCH_MILLI).status(new Active()).userId(USER_1).warehouseId(WAREHOUSE_1).build();
       new ObjectAssert<>(expectedInboundDTO).usingRecursiveComparison().isEqualTo(actualInboundDTO);
       Mockito.verify(inboundIdGenerator).generate(eq(startInboundRequest));
       Mockito.verify(clock).millis();
       Mockito.verifyNoMoreInteractions(inboundDAO, inboundIdGenerator, clock);

   }



    @Test
    public void test_end_inbound_success() {

        EndInboundRequest endInboundRequest = EndInboundRequest.builder().warehouseId(WAREHOUSE_1).inboundId(INBOUND_1).build();
        WarehouseActionValidationRequest expectedWarehouseActionValidationRequest = WarehouseActionValidationRequest.builder()
                .inboundId(endInboundRequest.getInboundId()).warehouseId(endInboundRequest.getWarehouseId()).warehouseAction(WarehouseAction.END_INBOUND).build();
        FGInboundDTO expectedInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).endTime(EPOCH_MILLI).status(new Closed()).warehouseId(WAREHOUSE_1).build();
        WarehouseValidatedEntities warehouseValidatedEntities = new WarehouseValidatedEntities.Builder().fgInboundDTO(expectedInboundDTO).build();
        Mockito.when(warehouseActionValidatorChain.execute(any(WarehouseActionValidationRequest.class))).thenReturn(warehouseValidatedEntities);

        inboundService.endInbound(endInboundRequest);
        Mockito.verify(inboundDAO).update(fgInboundDTOArgumentCaptor.capture());
        FGInboundDTO actualInboundDTO = fgInboundDTOArgumentCaptor.getValue();
        new ObjectAssert<>(expectedInboundDTO).usingRecursiveComparison().isEqualTo(actualInboundDTO);
        Mockito.verify(warehouseActionValidatorChain).execute(warehouseActionValidationRequestArgumentCaptor.capture());
        WarehouseActionValidationRequest actualWarehouseActionValidationRequest = warehouseActionValidationRequestArgumentCaptor.getValue();
        new ObjectAssert<>(actualWarehouseActionValidationRequest).usingRecursiveComparison().isEqualTo(expectedWarehouseActionValidationRequest);
        Mockito.verify(clock).millis();
        Mockito.verifyNoMoreInteractions(inboundDAO, clock);
        Mockito.verifyZeroInteractions(inboundIdGenerator);

    }

    @Test
    public void test_end_inbound_input_null() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> inboundService.endInbound(null)).withMessageContaining("endInboundRequest cannot be null");
        Mockito.verifyZeroInteractions(inboundDAO, inboundIdGenerator, clock);
    }



    @Test
    public void test_end_inbound_exception() {

        EndInboundRequest endInboundRequest = EndInboundRequest.builder().warehouseId(WAREHOUSE_1).inboundId(INBOUND_1).build();
        WarehouseActionValidationRequest expectedWarehouseActionValidationRequest = WarehouseActionValidationRequest.builder()
                .inboundId(endInboundRequest.getInboundId()).warehouseId(endInboundRequest.getWarehouseId()).warehouseAction(WarehouseAction.END_INBOUND).build();
        FGInboundDTO expectedInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).endTime(EPOCH_MILLI).status(new Closed()).warehouseId(WAREHOUSE_1).build();
        WarehouseValidatedEntities warehouseValidatedEntities = new WarehouseValidatedEntities.Builder().fgInboundDTO(expectedInboundDTO).build();
        Mockito.when(warehouseActionValidatorChain.execute(any(WarehouseActionValidationRequest.class))).thenReturn(warehouseValidatedEntities);

        Mockito.doThrow(RuntimeException.class).when(inboundDAO).update(any(FGInboundDTO.class));
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(()-> inboundService.endInbound(endInboundRequest));
        Mockito.verify(inboundDAO).update(fgInboundDTOArgumentCaptor.capture());
        FGInboundDTO actualInboundDTO = fgInboundDTOArgumentCaptor.getValue();
        new ObjectAssert<>(actualInboundDTO).usingRecursiveComparison().isEqualTo(expectedInboundDTO);
        Mockito.verify(clock).millis();
        Mockito.verifyNoMoreInteractions(inboundDAO, clock);
        Mockito.verifyZeroInteractions(inboundIdGenerator);

    }

}
