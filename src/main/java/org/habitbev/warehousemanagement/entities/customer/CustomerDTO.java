package org.habitbev.warehousemanagement.entities.customer;


import lombok.Builder;
import lombok.Value;

@Value
public class CustomerDTO {


    private String id;

    private String companyId;
    private String name;
    private String description;

    @Builder
    private CustomerDTO(String id, String companyId, String name, String description) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.description = description;
    }

    public static CustomerDTO fromCustomer(Customer customer) {
        return CustomerDTO.builder().id(customer.getId()).name(customer.getName()).companyId(customer.getCompanyId())
                .description(customer.getDescription()).build();
    }
}
