package org.habitbev.warehousemanagement.entities.company;


import lombok.Builder;
import lombok.Value;

@Value
public class CompanyDTO {


    private String id;
    private String name;
    private String description;

    @Builder
    private CompanyDTO(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static CompanyDTO fromCompany(Company company) {
        return CompanyDTO.builder().id(company.getId()).name(company.getName())
                .description(company.getDescription()).build();
    }
}
