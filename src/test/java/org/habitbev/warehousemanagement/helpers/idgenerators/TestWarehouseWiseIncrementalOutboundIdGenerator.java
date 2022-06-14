package org.habitbev.warehousemanagement.helpers.idgenerators;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.outbound.StartOutboundRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.habitbev.warehousemanagement.dao.OutboundDAO;

import java.util.Optional;

import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class TestWarehouseWiseIncrementalOutboundIdGenerator {

    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final String USER_1 = "user-1";
    public static final String CUSTOMER_1 = "customer-1";
    public static final String COMPANY_1 = "company-1";
    private static String OUTBOUND_1 = "OUTBOUND-1";
    private static String OUTBOUND_2 = "OUTBOUND-2";

    WarehouseWiseIncrementalOutboundIdGenerator warehouseWiseIncrementalOutboundIdGenerator;
    @Mock
    OutboundDAO outboundDAO;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        warehouseWiseIncrementalOutboundIdGenerator = new WarehouseWiseIncrementalOutboundIdGenerator(outboundDAO);
    }

    @Test
    public void test_input_null_illegal_argument_excpetion() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> warehouseWiseIncrementalOutboundIdGenerator.generate(null));
        Mockito.verifyZeroInteractions(outboundDAO);
    }

    @Test
    public void test_success_no_outbound_from_dao() {
        StartOutboundRequest startOutboundRequest = StartOutboundRequest.builder().companyId(COMPANY_1).warehouseId(WAREHOUSE_1).userId(USER_1).customerId(CUSTOMER_1).build();
        Mockito.when(outboundDAO.getLastOutbound(eq(startOutboundRequest.getWarehouseId()))).thenReturn(Optional.empty());
        String actualGeneratedId = warehouseWiseIncrementalOutboundIdGenerator.generate(startOutboundRequest);
        Mockito.verify(outboundDAO).getLastOutbound(eq(startOutboundRequest.getWarehouseId()));
        new StringAssert(actualGeneratedId).isEqualTo(OUTBOUND_1);
        Mockito.verifyNoMoreInteractions(outboundDAO);

    }

    @Test
    public void test_success_already_existing_outbound() {
        StartOutboundRequest startOutboundRequest = StartOutboundRequest.builder().companyId(COMPANY_1).customerId(CUSTOMER_1).warehouseId(WAREHOUSE_1).userId(USER_1).build();
        Mockito.when(outboundDAO.getLastOutbound(eq(startOutboundRequest.getWarehouseId()))).thenReturn(Optional.ofNullable(FinishedGoodsOutbound.builder().outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1).build()));
        String actualGeneratedId = warehouseWiseIncrementalOutboundIdGenerator.generate(startOutboundRequest);
        Mockito.verify(outboundDAO).getLastOutbound(eq(startOutboundRequest.getWarehouseId()));
        new StringAssert(actualGeneratedId).isEqualTo(OUTBOUND_2);
        Mockito.verifyNoMoreInteractions(outboundDAO);
    }
}
