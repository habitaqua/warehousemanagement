package org.warehousenamagement.entities.location;

import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

import java.util.Set;

public class AddLocationRequest {

    String id;
    String warehouseId;
    int quantity;
    Set<SKUType> supportedSKUTypes;
    Set<SKUCategory> supportedSKUCategories;
    boolean allSKUSupported;

    public AddLocationRequest(String id, String warehouseId, int quantity,
            Set<SKUType> supportedSKUTypes, Set<SKUCategory> supportedSKUCategories, boolean allSKUSupported) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.supportedSKUTypes = supportedSKUTypes;
        this.supportedSKUCategories = supportedSKUCategories;
        this.allSKUSupported = allSKUSupported;
    }
}
