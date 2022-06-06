package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.inventory.InventoryAddRequest;
import org.habitbev.warehousemanagement.entities.inventory.InventoryInboundRequest;
import org.habitbev.warehousemanagement.entities.inventory.InventoryOutboundRequest;
import org.habitbev.warehousemanagement.entities.inventory.MoveInventoryRequest;

import java.util.List;

public interface InventoryDAO {



    List<String> add(InventoryAddRequest inventoryAddRequest);

    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param inventoryInboundRequest
     */
     void inbound(InventoryInboundRequest inventoryInboundRequest);

    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param inventoryOutboundRequest
     */
     void outbound(InventoryOutboundRequest inventoryOutboundRequest);


     void move(MoveInventoryRequest moveInventoryRequest);


}
