package org.habitbev.warehousemanagement.helpers.idgenerators;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.dao.InboundDAO;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.inbound.StartInboundRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class TestCustomerWiseIncrementalInboundIdGenerator {

    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final String USER_1 = "user-1";
    private static String INBOUND_1 = "INBOUND-1";
    private static String INBOUND_2 = "INBOUND-2";

    WarehouseWiseIncrementalInboundIdGenerator warehouseWiseIncrementalInboundIdGenerator;
    @Mock
    InboundDAO inboundDAO;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        warehouseWiseIncrementalInboundIdGenerator = new WarehouseWiseIncrementalInboundIdGenerator(inboundDAO);
    }

    @Test
    public void test_input_null_illegal_argument_excpetion() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> warehouseWiseIncrementalInboundIdGenerator.generate(null));
        Mockito.verifyZeroInteractions(inboundDAO);
    }

    @Test
    public void test_success_no_inbound_from_dao() {
        StartInboundRequest startInboundRequest = StartInboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).build();
        Mockito.when(inboundDAO.getLastInbound(eq(startInboundRequest.getWarehouseId()))).thenReturn(Optional.empty());
        String actualGeneratedId = warehouseWiseIncrementalInboundIdGenerator.generate(startInboundRequest);
        Mockito.verify(inboundDAO).getLastInbound(eq(startInboundRequest.getWarehouseId()));
        new StringAssert(actualGeneratedId).isEqualTo(INBOUND_1);
        Mockito.verifyNoMoreInteractions(inboundDAO);

    }

    @Test
    public void test_success_already_existing_inbound() {
        StartInboundRequest startInboundRequest = StartInboundRequest.builder().warehouseId(WAREHOUSE_1).userId(USER_1).build();
        Mockito.when(inboundDAO.getLastInbound(eq(startInboundRequest.getWarehouseId()))).thenReturn(Optional.ofNullable(FinishedGoodsInbound.builder().inboundId(INBOUND_1).warehouseId(WAREHOUSE_1).build()));
        String actualGeneratedId = warehouseWiseIncrementalInboundIdGenerator.generate(startInboundRequest);
        Mockito.verify(inboundDAO).getLastInbound(eq(startInboundRequest.getWarehouseId()));
        new StringAssert(actualGeneratedId).isEqualTo(INBOUND_2);
        Mockito.verifyNoMoreInteractions(inboundDAO);
    }
}
