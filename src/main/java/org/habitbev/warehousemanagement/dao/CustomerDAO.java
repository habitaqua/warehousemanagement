package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.customer.CustomerDTO;

import java.util.Optional;

public interface CustomerDAO {

    Optional<CustomerDTO> getCustomer(String customerId, String companyId);
    Optional<CustomerDTO> getCustomer(String customerId);
}
