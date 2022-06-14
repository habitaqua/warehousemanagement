package org.habitbev.warehousemanagement.dao;

import com.google.common.collect.ImmutableMap;
import org.habitbev.warehousemanagement.entities.warehouse.Warehouse;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;

import java.util.Map;
import java.util.Optional;

public class ConfigWarehouseDAOImpl implements WarehouseDAO {

    Map<String, Warehouse> warehouseConfig = ImmutableMap.of("WAREHOUSE-1", Warehouse.builder().id("WAREHOUSE1")
            .companyId("VIVALA-BEVERAGES").name("WAREHOUSE-1").description("WAREHOUSE-1").build());


    @Override
    public Optional<WarehouseDTO> getWarehouse(String warehouseId, String companyId) {
        Warehouse warehouse = warehouseConfig.get(warehouseId);
        if(warehouse!=null && warehouse.getCompanyId().equals(companyId)) {
            return Optional.of(WarehouseDTO.fromWarehouse(warehouse));
        }
        return Optional.empty();
    }

    @Override
    public Optional<WarehouseDTO> getWarehouse(String warehouseId) {
        Warehouse warehouse = warehouseConfig.get(warehouseId);
        if(warehouse!=null) {
            return Optional.of(WarehouseDTO.fromWarehouse(warehouse));
        }
        return Optional.empty();
    }


}
