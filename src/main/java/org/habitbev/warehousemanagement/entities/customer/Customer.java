package org.habitbev.warehousemanagement.entities.customer;

import lombok.Builder;
import lombok.Value;

@Value
public class Customer {

    private String id;

    private String companyId;
    private String name;
    private String description;

    @Builder
    private Customer(String id, String companyId, String name, String description) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.description = description;
    }
}
