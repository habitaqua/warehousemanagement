package org.warehousemanagement.entities;

import lombok.Builder;
import lombok.Value;

@Value
public class Capacity {

    int qty;
    UOM uom;

    @Builder
    public Capacity(int qty, UOM uom) {
        this.qty = qty;
        this.uom = uom;
    }
}
