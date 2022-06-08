package org.habitbev.warehousemanagement.service;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.dao.ContainerCapacityDAO;
import org.habitbev.warehousemanagement.dao.ContainerDAO;
import org.habitbev.warehousemanagement.entities.container.AddContainerRequest;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.habitbev.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.habitbev.warehousemanagement.helpers.idgenerators.ContainerIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class TestContainerService {

    public static final String CONTAINER_1 = "container-1";
    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();
    private static final Map<String, Integer> PREDEFINED_CAPACITY = ImmutableMap.of("sku1", 20);


    ContainerService containerService;
    @Mock
    ContainerIdGenerator<AddContainerRequest> containerIdGenerator;
    @Mock
    ContainerCapacityService containerCapacityService;
    @Mock
    ContainerDAO containerDAO;

    @Captor
    ArgumentCaptor<AddContainerRequest> addContainerRequestArgumentCaptor;
    @Captor
    ArgumentCaptor<ContainerDTO> containerDTOArgumentCaptor;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        containerService = new ContainerService(containerDAO, containerCapacityService, containerIdGenerator);
    }

    @Test
    public void test_add_input_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerService.add(null)).withMessageContaining("addContainerRequest cannot be null");
        Mockito.verifyZeroInteractions(containerDAO, containerCapacityService, containerIdGenerator);
    }


    @Test
    public void test_add_success() {
        AddContainerRequest addContainerRequest = AddContainerRequest.builder().warehouseId(WAREHOUSE_1).skuCodeWisePredefinedCapacity(PREDEFINED_CAPACITY).build();
        Mockito.when(containerIdGenerator.generate(eq(addContainerRequest))).thenReturn(CONTAINER_1);
        String actualContainerId = containerService.add(addContainerRequest);
        Mockito.verify(containerIdGenerator).generate(eq(addContainerRequest));
        Mockito.verify(containerDAO).add(containerDTOArgumentCaptor.capture());
        ContainerDTO actualContainerDTO = containerDTOArgumentCaptor.getValue();
        ContainerDTO expectedContainerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(addContainerRequest.getSkuCodeWisePredefinedCapacity()).build();
        new ObjectAssert<>(actualContainerDTO).usingRecursiveComparison().isEqualTo(expectedContainerDTO);
        new StringAssert(actualContainerId).isEqualTo(CONTAINER_1);
        Mockito.verify(containerCapacityService).initialize(eq(WAREHOUSE_1), eq(CONTAINER_1));
        Mockito.verifyNoMoreInteractions(containerDAO, containerCapacityService, containerIdGenerator);
    }
}
