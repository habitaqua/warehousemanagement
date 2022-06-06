package org.habitbev.warehousemanagement.helpers;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.entities.container.containerstatus.Available;
import org.habitbev.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.habitbev.warehousemanagement.entities.container.containerstatus.Filled;
import org.habitbev.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.junit.Before;
import org.junit.Test;

public class TestContainerStatusDeterminer {


    ContainerStatusDeterminer containerStatusDeterminer;

    @Before
    public void setup() {
        containerStatusDeterminer = new ContainerStatusDeterminer();
    }

    @Test
    public void test_determine_status_new_capacity_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerStatusDeterminer.determineStatus(null, 5))
                .withMessageContaining("newCapacity cannot be out of bounds");
    }

    @Test
    public void test_determine_status_max_capacity_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerStatusDeterminer.determineStatus(2, null))
                .withMessageContaining("maxCapacity cannot be out of bounds");
    }

    @Test
    public void test_determine_status_new_capacity_zero() {
        ContainerStatus containerStatus = containerStatusDeterminer.determineStatus(0, 5);
        new StringAssert(containerStatus.getStatus()).isEqualTo(new Available().getStatus());
    }

    @Test
    public void test_determine_status_new_capacity_equals_max() {
        ContainerStatus containerStatus = containerStatusDeterminer.determineStatus(5, 5);
        new StringAssert(containerStatus.getStatus()).isEqualTo(new Filled().getStatus());
    }

    @Test
    public void test_determine_status_new_capacity_less_than_max() {
        ContainerStatus containerStatus = containerStatusDeterminer.determineStatus(4, 5);
        new StringAssert(containerStatus.getStatus()).isEqualTo(new PartiallyFilled().getStatus());
    }

}
