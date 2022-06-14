package org.habitbev.warehousemanagement.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.dao.OutboundDAO;
import org.habitbev.warehousemanagement.entities.outbound.EndOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.habitbev.warehousemanagement.helpers.idgenerators.OutboundIdGenerator;
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
public class TestOutboundService {

    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();
    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final String USER_1 = "user-1";
    public static final String OUTBOUND_1 = "OUTBOUND_1";
    public static final String CUSTOMER_1 = "customer-1";
    OutboundService outboundService;
    @Mock
    OutboundDAO outboundDAO;
    @Mock
    OutboundIdGenerator<StartOutboundRequest> outboundIdGenerator;
    @Mock
    Clock clock;
    @Captor
    ArgumentCaptor<OutboundDTO> outboundDTOArgumentCaptor;

    @Mock
    WarehouseActionValidatorChain warehouseActionValidatorChain;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        outboundService = new OutboundService(outboundDAO, outboundIdGenerator, clock, warehouseActionValidatorChain);
        Mockito.when(clock.millis()).thenReturn(EPOCH_MILLI);
    }


    @Test
    public void test_start_outbound_input_null() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> outboundService.startOutbound(null)).withMessageContaining("startOutboundRequest cannot be null");
        Mockito.verifyZeroInteractions(outboundDAO, outboundIdGenerator, clock);
    }

    @Test
    public void test_startOutbound_success() {

        StartOutboundRequest startOutboundRequest = StartOutboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).customerId(CUSTOMER_1).build();
        Mockito.when(outboundIdGenerator.generate(eq(startOutboundRequest))).thenReturn(OUTBOUND_1);
        String actualOutboundId = outboundService.startOutbound(startOutboundRequest);
        new StringAssert(actualOutboundId).isEqualTo(OUTBOUND_1);
        Mockito.verify(outboundDAO).add(outboundDTOArgumentCaptor.capture());
        OutboundDTO actualOutboundDTO = outboundDTOArgumentCaptor.getValue();
        OutboundDTO expectedOutboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).startTime(EPOCH_MILLI).customerId(CUSTOMER_1).status(new Active()).userId(USER_1).warehouseId(WAREHOUSE_1).build();
        new ObjectAssert<>(expectedOutboundDTO).usingRecursiveComparison().isEqualTo(actualOutboundDTO);
        Mockito.verify(outboundIdGenerator).generate(eq(startOutboundRequest));
        Mockito.verify(clock).millis();
        Mockito.verifyNoMoreInteractions(outboundDAO, outboundIdGenerator, clock);
    }


    @Test
    public void test_startOutbound_outbound_id_generator_throws_exception() {
        StartOutboundRequest startOutboundRequest = StartOutboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).customerId(CUSTOMER_1).build();
        Mockito.when(outboundIdGenerator.generate(eq(startOutboundRequest))).thenThrow(new RuntimeException());
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> outboundService.startOutbound(startOutboundRequest));
        Mockito.verify(outboundIdGenerator).generate(eq(startOutboundRequest));
        Mockito.verifyZeroInteractions(clock, outboundDAO);
        Mockito.verifyNoMoreInteractions(outboundIdGenerator);
    }

    @Test
    public void test_startOutbound_outbound_dao_throws_exception() {
        StartOutboundRequest startOutboundRequest = StartOutboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).customerId(CUSTOMER_1).build();
        Mockito.when(outboundIdGenerator.generate(eq(startOutboundRequest))).thenReturn(OUTBOUND_1);
        Mockito.doThrow(new IllegalArgumentException("illegal argument exception")).when(outboundDAO).add(outboundDTOArgumentCaptor.capture());
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> outboundService.startOutbound(startOutboundRequest));
        Mockito.verify(outboundDAO).add(outboundDTOArgumentCaptor.capture());
        OutboundDTO actualOutboundDTO = outboundDTOArgumentCaptor.getValue();
        OutboundDTO expectedOutboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).startTime(EPOCH_MILLI).customerId(CUSTOMER_1).status(new Active()).userId(USER_1).warehouseId(WAREHOUSE_1).build();
        new ObjectAssert<>(expectedOutboundDTO).usingRecursiveComparison().isEqualTo(actualOutboundDTO);
        Mockito.verify(outboundIdGenerator).generate(eq(startOutboundRequest));
        Mockito.verify(clock).millis();
        Mockito.verifyNoMoreInteractions(outboundDAO, outboundIdGenerator, clock);

    }



    @Test
    public void test_end_outbound_success() {

        EndOutboundRequest endOutboundRequest = EndOutboundRequest.builder().warehouseId(WAREHOUSE_1).outboundId(OUTBOUND_1).build();
        outboundService.endOutbound(endOutboundRequest);
        Mockito.verify(outboundDAO).update(outboundDTOArgumentCaptor.capture());
        OutboundDTO actualOutboundDTO = outboundDTOArgumentCaptor.getValue();
        OutboundDTO expectedOutboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).endTime(EPOCH_MILLI).status(new Closed()).warehouseId(WAREHOUSE_1).build();
        new ObjectAssert<>(expectedOutboundDTO).usingRecursiveComparison().isEqualTo(actualOutboundDTO);
        Mockito.verify(clock).millis();
        Mockito.verifyNoMoreInteractions(outboundDAO, outboundIdGenerator, clock);
    }

    @Test
    public void test_end_outbound_input_null() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> outboundService.endOutbound(null)).withMessageContaining("endOutboundRequest cannot be null");
        Mockito.verifyZeroInteractions(outboundDAO, outboundIdGenerator, clock);
    }



    @Test
    public void test_end_outbound_exception() {

        EndOutboundRequest endOutboundRequest = EndOutboundRequest.builder().warehouseId(WAREHOUSE_1).outboundId(OUTBOUND_1).build();
        Mockito.doThrow(RuntimeException.class).when(outboundDAO).update(any(OutboundDTO.class));
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(()->outboundService.endOutbound(endOutboundRequest));
        Mockito.verify(outboundDAO).update(outboundDTOArgumentCaptor.capture());
        OutboundDTO actualOutboundDTO = outboundDTOArgumentCaptor.getValue();
        OutboundDTO expectedOutboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).endTime(EPOCH_MILLI).status(new Closed()).warehouseId(WAREHOUSE_1).build();
        new ObjectAssert<>(expectedOutboundDTO).usingRecursiveComparison().isEqualTo(actualOutboundDTO);
        Mockito.verify(clock).millis();
        Mockito.verifyNoMoreInteractions(outboundDAO, outboundIdGenerator, clock);
    }

}
