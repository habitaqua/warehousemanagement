package org.warehousemanagement.service;

import com.google.inject.Inject;
import org.warehousemanagement.entities.inventory.InventoryDTO;
import org.warehousemanagement.sao.LocationSKUMappingDbSAO;

public class LocationSKUCountService {

    LocationSKUMappingDbSAO locationSKUMappingDbSAO;

    @Inject
    public LocationSKUCountService(LocationSKUMappingDbSAO locationSKUMappingDbSAO) {
        this.locationSKUMappingDbSAO = locationSKUMappingDbSAO;
    }

    public void updateLocationSKUCount(InventoryDTO inventoryDTO) {

        String locationId = inventoryDTO.getLocationId();
        String warehouseId = inventoryDTO.getWarehouseId();


       // locationSKUCountDbSAO.updateLocationSKUCount();


    }
}
