package org.habitbev.warehousemanagement.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.WarehouseDAO;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;

import java.util.Optional;

public class WarehouseService {


    WarehouseDAO warehouseDAO;

    @Inject
    public WarehouseService(@Named("configWarehouseDAOImpl") WarehouseDAO warehouseDAO) {
        this.warehouseDAO = warehouseDAO;
    }


    public Optional<WarehouseDTO> getWarehouse(String warehouseId, String companyId) {
        return warehouseDAO.getWarehouse(warehouseId, companyId);
    }

    public Optional<WarehouseDTO> getWarehouse(String warehouseId) {
        return warehouseDAO.getWarehouse(warehouseId);
    }
}

