package org.habitbev.warehousemanagement.entities.company;

import lombok.Builder;
import lombok.Value;

@Value
public class Company {

    private String id;
    private String name;
    private String description;

    @Builder
    private Company(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
