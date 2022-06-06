package org.habitbev.warehousemanagement.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.dao.ContainerCapacityDAO;
import org.habitbev.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.habitbev.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class TestContainerCapacityService {

    public static final String CONTAINER_1 = "container-1";
    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();
    ContainerCapacityService containerCapacityService;

    @Mock
    ContainerCapacityDAO containerCapacityDAO;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        containerCapacityService = new ContainerCapacityService(containerCapacityDAO);
    }

    @Test
    public void test_get_warehouseid_blank() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerCapacityService.get(" ", CONTAINER_1))
                .withMessageContaining("warehouseId cannot be blank");
        Mockito.verifyZeroInteractions(containerCapacityDAO);
    }

    @Test
    public void test_get_container_id_blank() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerCapacityService.get(WAREHOUSE_1, " "))
                .withMessageContaining("containerId cannot be blank");
        Mockito.verifyZeroInteractions(containerCapacityDAO);
    }

    @Test
    public void test_get_success() {
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(0).warehouseContainerId(CONTAINER_1).creationTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
        Mockito.when(containerCapacityDAO.get(eq(WAREHOUSE_1), eq(CONTAINER_1))).thenReturn(Optional.of(containerCapacityExpected));

        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityService.get(WAREHOUSE_1, CONTAINER_1);
        Mockito.verify(containerCapacityDAO).get(eq(WAREHOUSE_1), eq(CONTAINER_1));
        new ObjectAssert<>(containerCapacityActualOp.get()).usingRecursiveComparison().isEqualTo(containerCapacityExpected);
        Mockito.verifyNoMoreInteractions(containerCapacityDAO);
    }

    @Test
    public void test_get_dao_throws_exception() {
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().containerStatus(new PartiallyFilled())
                .currentCapacity(0).warehouseContainerId(CONTAINER_1).creationTime(EPOCH_MILLI).modifiedTime(EPOCH_MILLI).build();
        Mockito.when(containerCapacityDAO.get(eq(WAREHOUSE_1), eq(CONTAINER_1))).thenThrow(new RuntimeException("exception"));

        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> containerCapacityService.get(WAREHOUSE_1, CONTAINER_1));
        Mockito.verify(containerCapacityDAO).get(eq(WAREHOUSE_1), eq(CONTAINER_1));
        Mockito.verifyNoMoreInteractions(containerCapacityDAO);
    }

    @Test
    public void test_init_warehouseid_blank() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerCapacityService.initialize(" ", CONTAINER_1))
                .withMessageContaining("warehouseId cannot be blank");
        Mockito.verifyZeroInteractions(containerCapacityDAO);
    }

    @Test
    public void test_init_container_id_blank() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerCapacityService.initialize(WAREHOUSE_1, " "))
                .withMessageContaining("containerId cannot be blank");
        Mockito.verifyZeroInteractions(containerCapacityDAO);
    }

    @Test
    public void test_init_success() {

        containerCapacityService.initialize(WAREHOUSE_1, CONTAINER_1);
        Mockito.verify(containerCapacityDAO).init(eq(WAREHOUSE_1), eq(CONTAINER_1));
        Mockito.verifyNoMoreInteractions(containerCapacityDAO);

    }

    @Test
    public void test_init_dao_throws_exception() {
        Mockito.doThrow(new RuntimeException("exception")).when(containerCapacityDAO).init(eq(WAREHOUSE_1),eq(CONTAINER_1));
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy( ()->containerCapacityService.initialize(WAREHOUSE_1, CONTAINER_1));
        Mockito.verify(containerCapacityDAO).init(eq(WAREHOUSE_1), eq(CONTAINER_1));
        Mockito.verifyNoMoreInteractions(containerCapacityDAO);
    }
}
