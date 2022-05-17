package org.warehousenamagement.entities.location;

import org.warehousenamagement.entities.Capacity;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

import java.util.Set;

public class UpdateLocationRequest {

    String id;
    Capacity capacity;
    Set<SKUType> supportedSKUTypes;
    Set<SKUCategory> supportedSKUCategories;
    Set<SKUType> removedSKUTypes;
    Set<SKUCategory> removedSKUCategories;
    boolean allSKUSupported;

    public UpdateLocationRequest(String id, Capacity capacity, Set<SKUType> supportedSKUTypes,
            Set<SKUCategory> supportedSKUCategories, Set<SKUType> removedSKUTypes,
            Set<SKUCategory> removedSKUCategories, boolean allSKUSupported) {
        this.id = id;
        this.capacity = capacity;
        this.supportedSKUTypes = supportedSKUTypes;
        this.supportedSKUCategories = supportedSKUCategories;
        this.removedSKUTypes = removedSKUTypes;
        this.removedSKUCategories = removedSKUCategories;
        this.allSKUSupported = allSKUSupported;
    }
}
