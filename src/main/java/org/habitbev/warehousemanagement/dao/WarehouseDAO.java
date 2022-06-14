package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;

import java.util.Optional;

public interface WarehouseDAO {

    Optional<WarehouseDTO> getWarehouse(String warehouseId, String companyId);
    Optional<WarehouseDTO> getWarehouse(String warehouseId);
}
