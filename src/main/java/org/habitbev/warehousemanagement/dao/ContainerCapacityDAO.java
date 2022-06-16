package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.dynamodb.ContainerCapacity;

import java.util.Optional;

public interface ContainerCapacityDAO {


    Optional<ContainerCapacity> get(String warehouseId, String containerId);

     int getExistingQuantity(String warehouseId, String containerId);

    void init(String warehouseId, String containerId);

}
