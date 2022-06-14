package org.habitbev.warehousemanagement.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.CompanyDAO;
import org.habitbev.warehousemanagement.dao.CustomerDAO;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.customer.CustomerDTO;

import java.util.Optional;

public class CustomerService {


    CustomerDAO customerDAO;

    @Inject
    public CustomerService(@Named("configCustomerDAOImpl") CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }


    public Optional<CustomerDTO> getCustomer(String customerId, String companyId) {
        return customerDAO.getCustomer(customerId, companyId);
    }
}
