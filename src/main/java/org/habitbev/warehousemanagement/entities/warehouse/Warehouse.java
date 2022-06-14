package org.habitbev.warehousemanagement.entities.warehouse;

import lombok.Builder;
import lombok.Value;

@Value
public class Warehouse {

    private String id;

    private String companyId;
    private String name;
    private String description;

    @Builder
    private Warehouse(String id, String companyId, String name, String description) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.description = description;
    }
}
