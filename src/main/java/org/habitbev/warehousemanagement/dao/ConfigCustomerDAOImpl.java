package org.habitbev.warehousemanagement.dao;

import com.google.common.collect.ImmutableMap;
import org.habitbev.warehousemanagement.entities.company.Company;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.customer.Customer;
import org.habitbev.warehousemanagement.entities.customer.CustomerDTO;

import java.util.Map;
import java.util.Optional;

public class ConfigCustomerDAOImpl implements CustomerDAO {

    Map<String, Customer> customerConfig = ImmutableMap.of("CUSTOMER-1", Customer.builder().id("CUSTOMER-1")
            .companyId("VIVALA-BEVERAGES").name("CUSTOMER-1").description("CUSTOMER-1").build());


    @Override
    public Optional<CustomerDTO> getCustomer(String customerId, String companyId) {
        Customer  customer = customerConfig.get(customerId);
        if(customer!=null && customer.getCompanyId().equals(companyId)) {
            return Optional.of(CustomerDTO.fromCustomer(customer));
        }
        return Optional.empty();
    }

    @Override
    public Optional<CustomerDTO> getCustomer(String customerId) {
        Customer  customer = customerConfig.get(customerId);
        if(customer!=null) {
            return Optional.of(CustomerDTO.fromCustomer(customer));
        }
        return Optional.empty();
    }


}
