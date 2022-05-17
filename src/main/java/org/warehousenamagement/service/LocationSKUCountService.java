package org.warehousenamagement.service;

import com.google.inject.Inject;
import org.warehousenamagement.entities.inventory.InventoryDTO;
import org.warehousenamagement.sao.LocationSKUCountDbSAO;

public class LocationSKUCountService {

    LocationSKUCountDbSAO locationSKUCountDbSAO;

    @Inject
    public LocationSKUCountService(LocationSKUCountDbSAO locationSKUCountDbSAO) {
        this.locationSKUCountDbSAO = locationSKUCountDbSAO;
    }

    public void updateLocationSKUCount(InventoryDTO inventoryDTO) {

        String locationId = inventoryDTO.getLocationId();
        String warehouseId = inventoryDTO.getWarehouseId();


       // locationSKUCountDbSAO.updateLocationSKUCount();


    }
}
