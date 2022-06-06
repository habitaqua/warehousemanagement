package org.habitbev.warehousemanagement.helpers.idgenerators;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.habitbev.warehousemanagement.dao.ContainerDAO;
import org.habitbev.warehousemanagement.entities.container.AddContainerRequest;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class TestWarehouseWiseIncrementalContainerIdGenerator {

    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final ImmutableMap<String, Integer> SKU_CODE_WISE_PREDEFINED_CAPACITY = ImmutableMap.of("sku-code", 10);
    private static String CONTAINER_1 = "CONTAINER-1";
    private static String CONTAINER_2 = "CONTAINER-2";

    WarehouseWiseIncrementalContainerIdGenerator warehouseWiseIncrementalContainerIdGenerator;
    @Mock
    ContainerDAO containerDAO;
    @Captor
    ArgumentCaptor<GetContainerRequest> getContainerRequestArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        warehouseWiseIncrementalContainerIdGenerator = new WarehouseWiseIncrementalContainerIdGenerator(containerDAO);
    }

    @Test
    public void test_input_null_illegal_argument_excpetion() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> warehouseWiseIncrementalContainerIdGenerator.generate(null));
        Mockito.verifyZeroInteractions(containerDAO);
    }

    @Test
    public void test_success_no_container_from_dao() {
        AddContainerRequest addContainerRequest = AddContainerRequest.builder().warehouseId(WAREHOUSE_1).skuCodeWisePredefinedCapacity(SKU_CODE_WISE_PREDEFINED_CAPACITY).build();
        Mockito.when(containerDAO.getLastAddedContainer(eq(addContainerRequest.getWarehouseId()))).thenReturn(Optional.empty());
        String actualGeneratedId = warehouseWiseIncrementalContainerIdGenerator.generate(addContainerRequest);
        Mockito.verify(containerDAO).getLastAddedContainer(eq(addContainerRequest.getWarehouseId()));
        new StringAssert(actualGeneratedId).isEqualTo(CONTAINER_1);
        Mockito.verifyNoMoreInteractions(containerDAO);

    }

    @Test
    public void test_success_already_existing_container() {
        AddContainerRequest addContainerRequest = AddContainerRequest.builder().warehouseId(WAREHOUSE_1).skuCodeWisePredefinedCapacity(SKU_CODE_WISE_PREDEFINED_CAPACITY).build();
        Mockito.when(containerDAO.getLastAddedContainer(eq(addContainerRequest.getWarehouseId()))).thenReturn(Optional.ofNullable(new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).predefinedCapacity(SKU_CODE_WISE_PREDEFINED_CAPACITY).build()));
        String actualGeneratedId = warehouseWiseIncrementalContainerIdGenerator.generate(addContainerRequest);
        Mockito.verify(containerDAO).getLastAddedContainer(eq(addContainerRequest.getWarehouseId()));
        new StringAssert(actualGeneratedId).isEqualTo(CONTAINER_2);
        Mockito.verifyNoMoreInteractions(containerDAO);
    }
}
