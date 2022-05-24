package org.warehousemanagement.dao;

import org.warehousemanagement.entities.inventory.AddInventoryRequest;
import org.warehousemanagement.entities.inventory.FulfillInventoryRequest;
import org.warehousemanagement.entities.inventory.MoveInventoryRequest;

public interface InventoryDAO {




    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param addInventoryRequest
     */
     void add(AddInventoryRequest addInventoryRequest);

    /**
     * idempotent for upi using InventoryStatus
     * transact write of adding inventory item and changing location capacity.
     *
     * @param fulfillInventoryRequest
     */
     void fulfill(FulfillInventoryRequest fulfillInventoryRequest);


     void move(MoveInventoryRequest moveInventoryRequest);


}
