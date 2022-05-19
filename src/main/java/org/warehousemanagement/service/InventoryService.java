package org.warehousemanagement.service;

import com.google.inject.Inject;
import org.warehousemanagement.entities.InboundRequest;
import org.warehousemanagement.entities.inventory.InventoryDTO;
import org.warehousemanagement.sao.InventoryDbSAO;
import org.warehousemanagement.sao.LocationSKUMappingDbSAO;

public class InventoryService {

    InventoryDbSAO inventoryDbSAO;
    LocationSKUMappingDbSAO locationSKUMappingDbSAO;

    @Inject
    public InventoryService(InventoryDbSAO inventoryDbSAO, LocationSKUMappingDbSAO locationSKUMappingDbSAO) {
        this.inventoryDbSAO = inventoryDbSAO;
        this.locationSKUMappingDbSAO = locationSKUMappingDbSAO;
    }

    public void add(InboundRequest inboundRequest) {

        InventoryDTO inventoryDTO = InventoryDTO.builder().skuId(inboundRequest.getSkuId())
                .locationId(inboundRequest.getLocationId()).warehouseId(inboundRequest.getWarehouseId())
                .skuCategory(inboundRequest.getSkuCategory()).skuType(inboundRequest.getSkuType())
                .companyId(inboundRequest.getCompanyId()).build();
        inventoryDbSAO.add(inventoryDTO);

    }
}
