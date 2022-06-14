package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.customer.CustomerDTO;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.service.CompanyService;
import org.habitbev.warehousemanagement.service.CustomerService;

import java.util.Optional;

public class CustomerIdValidator implements WarehouseActionEntitiesValidator {

    CustomerService customerService;

    @Inject
    public CustomerIdValidator(CustomerService customerService) {
        this.customerService = customerService;
    }


    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder gatheredWarehouseEntities) {
        Preconditions.checkArgument(input != null, "input cannot be null");
        Preconditions.checkArgument(gatheredWarehouseEntities != null, "gatherredWarehouseEntities cannot be null");

        String companyId = input.getCompanyId();
        String customerId = input.getCustomerId();
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(customerId), "customerId cannot be blank");

        Optional<CustomerDTO> customerDTO = customerService.getCustomer(customerId, companyId);
        if (!customerDTO.isPresent()) {
            String message = String.format("customerId %s and companyId %s mapping does not exist", customerId, companyId);
            throw new ResourceNotAvailableException(message);
        }
        return gatheredWarehouseEntities.customerDTO(customerDTO.get());
    }
}
