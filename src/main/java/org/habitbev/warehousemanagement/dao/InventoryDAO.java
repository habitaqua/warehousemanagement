package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.habitbev.warehousemanagement.entities.inventory.InventoryInboundRequestDTO;
import org.habitbev.warehousemanagement.entities.inventory.InventoryOutboundRequestDTO;
import org.habitbev.warehousemanagement.entities.inventory.MoveInventoryRequest;

import java.util.List;

public interface InventoryDAO {



    List<String> add(InventoryAddRequest inventoryAddRequest);

    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param inventoryInboundRequestDTO
     */
     void inbound(InventoryInboundRequestDTO inventoryInboundRequestDTO);

    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param inventoryOutboundRequestDTO
     */
     void outbound(InventoryOutboundRequestDTO inventoryOutboundRequestDTO);


     void move(MoveInventoryRequest moveInventoryRequest);


}
