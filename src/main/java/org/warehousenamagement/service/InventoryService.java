package org.warehousenamagement.service;

import com.google.inject.Inject;
import org.warehousenamagement.entities.inventory.AddInventoryRequest;
import org.warehousenamagement.entities.inventory.InventoryDTO;
import org.warehousenamagement.sao.InventoryDbSAO;

public class InventoryService {

    InventoryDbSAO inventoryDbSAO;

    @Inject
    public InventoryService(InventoryDbSAO inventoryDbSAO) {
        this.inventoryDbSAO = inventoryDbSAO;
    }

    public void add(AddInventoryRequest addInventoryRequest) {

        InventoryDTO inventoryDTO = InventoryDTO.builder().skuId(addInventoryRequest.getSkuId())
                .locationId(addInventoryRequest.getLocationId()).warehouseId(addInventoryRequest.getWarehouseId())
                .skuCategory(addInventoryRequest.getSkuCategory()).skuType(addInventoryRequest.getSkuType())
                .companyId(addInventoryRequest.getCompanyId()).build();
        inventoryDbSAO.add(inventoryDTO);

    }
}
