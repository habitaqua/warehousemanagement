package org.habitbev.warehousemanagement.entities.warehouse;


import lombok.Builder;
import lombok.Value;

@Value
public class WarehouseDTO {


    private String id;

    private String companyId;
    private String name;
    private String description;

    @Builder
    private WarehouseDTO(String id, String companyId, String name, String description) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.description = description;
    }

    public static WarehouseDTO fromWarehouse(Warehouse warehouse) {
        return WarehouseDTO.builder().id(warehouse.getId()).name(warehouse.getName()).companyId(warehouse.getCompanyId())
                .description(warehouse.getDescription()).build();
    }
}
